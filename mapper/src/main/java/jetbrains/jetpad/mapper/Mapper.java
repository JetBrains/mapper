/*
 * Copyright 2012-2015 JetBrains s.r.o
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

/**
 * Mapper is an
 */
public abstract class Mapper<SourceT, TargetT> {
  private static final Object[] EMPTY_PARTS = new Object[0];

  private SourceT mySource;
  private TargetT myTarget;
  private MappingContext myMappingContext;
  private boolean myDetached;

  private Object[] myParts = EMPTY_PARTS;
  private Mapper<?, ?> myParent;

  /**
   * Construct a mapper with SourceT source and TargetT target
   *
   * NB: DO NOT create disposable resources in constructors. Use either registerSynchronizers or onAttach method.
   */
  public Mapper(SourceT source, TargetT target) {
    mySource = source;
    myTarget = target;
  }

  public final Mapper<?, ?> getParent() {
    return myParent;
  }

  protected boolean isFindable() {
    return true;
  }

  protected void registerSynchronizers(SynchronizersConfiguration conf) {
  }

  private void instantiateSynchronizers() {
    registerSynchronizers(new SynchronizersConfiguration() {
      @Override
      public void add(Synchronizer sync) {
        addPart(sync);
      }
    });
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
    attachRoot(new MappingContext());
  }

  public final void attachRoot(MappingContext ctx) {
    if (myMappingContext != null) {
      throw new IllegalStateException();
    }
    if (myParent != null) {
      throw new IllegalStateException();
    }
    attach(ctx);
  }

  public final void detachRoot() {
    if (myMappingContext == null) {
      throw new IllegalStateException();
    }
    if (myParent != null) {
      throw new IllegalStateException("Dispose can be called only on the root mapper");
    }
    detach();
  }

  final void attach(MappingContext ctx) {
    if (myMappingContext != null) {
      throw new IllegalStateException("Mapper is already attached");
    }
    if (myDetached) {
      throw new IllegalStateException("Mapper can't be reused because it was already detached");
    }

    onBeforeAttach(ctx);

    myMappingContext = ctx;

    instantiateSynchronizers();

    myMappingContext.register(this);

    for (Object part : myParts) {
      if (part instanceof Synchronizer) {
        Synchronizer s = (Synchronizer) part;
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

    for (Object part : myParts) {
      if (part instanceof Synchronizer) {
        Synchronizer s = (Synchronizer) part;
        s.detach();
      }
      if (part instanceof ChildContainer) {
        ChildContainer cc = (ChildContainer) part;
        for (Mapper<?, ?> m : cc.getChildren()) {
          m.detach();
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

  private void addPart(Object o) {
    Object[] newParts = new Object[myParts.length + 1];
    System.arraycopy(myParts, 0, newParts, 0, myParts.length);
    newParts[newParts.length - 1] = o;
    myParts = newParts;
  }

  private void removePart(Object o) {
    int index = Arrays.asList(myParts).indexOf(o);
    Object[] newParts = new Object[myParts.length - 1];
    System.arraycopy(myParts, 0, newParts, 0, index);
    System.arraycopy(myParts, index + 1, newParts, index, myParts.length - index - 1);
    myParts = newParts;
  }

  public final <MapperT extends Mapper<?, ?>> ObservableList<MapperT> createChildList() {
    return new ChildList<>();
  }

  public final <MapperT extends Mapper<?, ?>> ObservableSet<MapperT> createChildSet() {
    return new ChildSet<>();
  }

  public final <MapperT extends Mapper<?, ?>> Property<MapperT> createChildProperty() {
    return new ChildProperty<>();
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
    if (item.myParent != null) {
      throw new IllegalArgumentException();
    }
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
        addPart(this);
      }
      if (get() != null && value == null) {
        removePart(this);
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
    protected void checkAdd(int index, MapperT item) {
      Mapper.this.checkCanAdd(item);

      super.checkAdd(index, item);
    }

    @Override
    protected void checkRemove(int index, MapperT item) {
      Mapper.this.checkCanRemove(item);

      super.checkRemove(index, item);
    }

    @Override
    protected void beforeItemAdded(int index, MapperT item) {
      if (isEmpty()) {
        addPart(this);
      }
      addChild(item);
      super.beforeItemAdded(index, item);
    }

    @Override
    protected void beforeItemRemoved(int index, MapperT item) {
      removeChild(item);
      super.beforeItemRemoved(index, item);
    }

    @Override
    protected void afterItemRemoved(int index, MapperT item, boolean success) {
      if (isEmpty()) {
        removePart(this);
      }
      super.afterItemRemoved(index, item, success);
    }

    @Override
    public List<Mapper<?, ?>> getChildren() {
      return new ArrayList<Mapper<?, ?>>(this);
    }
  }

  private class ChildSet<MapperT extends Mapper<?, ?>> extends ObservableHashSet<MapperT> implements ChildContainer {
    @Override
    protected void checkAdd(MapperT item) {
      Mapper.this.checkCanAdd(item);

      super.checkAdd(item);
    }

    @Override
    protected void checkRemove(MapperT item) {
      Mapper.this.checkCanRemove(item);

      super.checkRemove(item);
    }

    @Override
    protected void beforeItemAdded(MapperT item) {
      if (isEmpty()) {
        addPart(this);
      }

      addChild(item);

      super.beforeItemAdded(item);
    }

    @Override
    protected void beforeItemRemoved(MapperT item) {
      removeChild(item);
      super.beforeItemRemoved(item);
    }

    @Override
    protected void afterItemRemoved(MapperT item, boolean success) {
      if (isEmpty()) {
        removePart(this);
      }
      super.afterItemRemoved(item, success);
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