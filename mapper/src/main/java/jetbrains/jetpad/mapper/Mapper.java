/*
 * Copyright 2012-2013 JetBrains s.r.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.jetpad.mapper;

import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import jetbrains.jetpad.model.collections.set.ObservableSet;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class Mapper<SourceT, TargetT> {
  private static final ChildContainer[] EMPTY_CONTAINERS = new ChildContainer[0];

  private SourceT mySource;
  private TargetT myTarget;
  private MappingContext myMappingContext;
  private boolean myDetached;
  private Synchronizer[] mySynchronizers;
  private Mapper<?, ?> myParent;
  private ChildContainer[] myChildContainers = EMPTY_CONTAINERS;

  public Mapper(SourceT source, TargetT target) {
    mySource = source;
    myTarget = target;
  }

  public final Mapper<?, ?> getParent() {
    return myParent;
  }

  protected void registerSynchronizers(SynchronizersConfiguration conf) {
  }

  private void instantiateSynchronizers() {
    final List<Synchronizer> synchronizers = new ArrayList<Synchronizer>();
    registerSynchronizers(new SynchronizersConfiguration() {
      @Override
      public void add(Synchronizer sync) {
        synchronizers.add(sync);
      }
    });
    if (!synchronizers.isEmpty()) {
      mySynchronizers = synchronizers.toArray(new Synchronizer[synchronizers.size()]);
    }
  }

  public final boolean isAttached() {
    return myMappingContext != null;
  }

  public final MappingContext getMappingContext() {
    return myMappingContext;
  }

  public final <S> Mapper<? super S, ?> getDescendantMapper(S source) {
    return getMappingContext().getMapper(this, source);
  }

  public final SourceT getSource() {
    return mySource;
  }

  public final TargetT getTarget() {
    return myTarget;
  }

  public final void attachRoot() {
    if (myMappingContext != null) throw new IllegalStateException();
    if (myParent != null) throw new IllegalStateException();
    attach(new MappingContext());
  }

  public final void detachRoot() {
    if (myMappingContext == null) throw new IllegalStateException();
    if (myParent != null) throw new IllegalStateException("Dispose can be called only on the root mapper");
    detach();
  }

  final void attach(MappingContext ctx) {
    if (myMappingContext != null) throw new IllegalStateException("Mapper is already attached");
    if (myDetached) throw new IllegalStateException("Mapper can't be reused because it was already detached");

    onBeforeAttach(ctx);

    myMappingContext = ctx;

    instantiateSynchronizers();

    myMappingContext.register(this);

    if (mySynchronizers != null) {
      for (final Synchronizer s : mySynchronizers) {
        s.attach(new SynchronizerContext() {
          @Override
          public MappingContext getMappingContext() {
            return myMappingContext;
          }

          @Override
          public Mapper<?, ?> getMapper() {
            return Mapper.this;
          }
        });
      }
    }

    onAttach(ctx);
  }

  final void detach() {
    if (myMappingContext == null) {
      throw new IllegalStateException();
    }

    onDetach();

    if (mySynchronizers != null) {
      for (final Synchronizer s : mySynchronizers) {
        s.detach();
      }
    }

    if (myChildContainers != null) {
      for (ChildContainer c : myChildContainers) {
        for (Mapper<?, ?> cc : c.getChildren()) {
          cc.detach();
        }
      }
    }

    myMappingContext.unregister(this);

    myMappingContext = null;
    myDetached = true;
  }

  protected void onBeforeAttach(MappingContext ctx) {
  }

  protected void onAttach(MappingContext ctx) {
  }

  protected void onDetach() {
  }

  private void registerChildContainer(ChildContainer c) {
    ChildContainer[] newChildContainers = new ChildContainer[myChildContainers.length + 1];
    System.arraycopy(myChildContainers, 0, newChildContainers, 0, myChildContainers.length);
    newChildContainers[newChildContainers.length - 1] = c;
    myChildContainers = newChildContainers;
  }

  private void unregisterChildContainer(ChildContainer c) {
    int index = Arrays.asList(myChildContainers).indexOf(c);
    ChildContainer[] newContainer = new ChildContainer[myChildContainers.length - 1];
    System.arraycopy(myChildContainers, 0, newContainer, 0, index);
    System.arraycopy(myChildContainers, index + 1, newContainer, index, myChildContainers.length - index - 1);
    myChildContainers = newContainer;
  }

  public final <MapperT extends Mapper<?, ?>> ObservableList<MapperT> createChildList() {
    return new ChildList<MapperT>();
  }

  public final <MapperT extends Mapper<?, ?>> ObservableSet<MapperT> createChildSet() {
    return new ChildSet<MapperT>();
  }

  public final <MapperT extends Mapper<?, ?>> Property<MapperT> createChildProperty() {
    return new ChildProperty<MapperT>();
  }

  private void addChild(Mapper<?, ?> child) {
    child.myParent = Mapper.this;
    child.attach(myMappingContext);
  }

  private void removeChild(Mapper<?, ?> child) {
    child.detach();
    child.myParent = null;
  }

  private void checkCanAdd(Mapper<?, ?> item) {
    if (item.myParent != null) throw new IllegalArgumentException();
  }

  private void checkCanRemove(Mapper<?, ?> item) {
    if (item.myParent != this) {
      throw new IllegalArgumentException();
    }
  }

  private class ChildProperty<MapperT extends Mapper<?, ?>> extends ValueProperty<MapperT> implements ChildContainer {
    @Override
    public void set(MapperT value) {
      if (get() == null && value != null) {
        registerChildContainer(this);
      }
      if (get() != null && value == null) {
        unregisterChildContainer(this);
      }

      MapperT oldValue = get();
      if (oldValue != null) {
        checkCanRemove(oldValue);
        removeChild(oldValue);
      }
      super.set(value);
      if (value != null) {
        checkCanAdd(value);
        addChild(value);
      }
    }

    @Override
    public List<Mapper<?, ?>> getChildren() {
      if (get() == null) return Collections.emptyList();
      return Collections.<Mapper<?, ?>>singletonList(get());
    }
  }

  private class ChildList<MapperT extends Mapper<?, ?>> extends ObservableArrayList<MapperT> implements ChildContainer {
    @Override
    public void add(int index, final MapperT item) {
      checkCanAdd(item);

      if (isEmpty()) {
        registerChildContainer(this);
      }

      super.add(index, item);
      addChild(item);
    }

    @Override
    public MapperT remove(int index) {
      MapperT item = get(index);
      checkCanRemove(item);
      super.remove(index);
      removeChild(item);

      if (isEmpty()) {
        unregisterChildContainer(this);
      }

      return item;
    }

    @Override
    public List<Mapper<?, ?>> getChildren() {
      return new ArrayList<Mapper<?, ?>>(this);
    }
  }

  private class ChildSet<MapperT extends Mapper<?, ?>> extends ObservableHashSet<MapperT> implements ChildContainer {
    @Override
    public boolean add(MapperT item) {
      if (contains(item)) return false;

      if (isEmpty()) {
        registerChildContainer(this);
      }

      checkCanAdd(item);
      addChild(item);
      return super.add(item);
    }

    @Override
    public boolean remove(Object item) {
      if (!contains(item)) return false;
      checkCanRemove((Mapper<?, ?>) item);
      removeChild((Mapper<?, ?>) item);

      if (isEmpty()) {
        registerChildContainer(this);
      }

      return super.remove(item);
    }

    @Override
    public List<Mapper<?, ?>> getChildren() {
      return new ArrayList<Mapper<?, ?>>(this);
    }
  }

  private interface ChildContainer {
    List<Mapper<?, ?>> getChildren();
  }

  public interface SynchronizersConfiguration {
    void add(Synchronizer sync);
  }
}