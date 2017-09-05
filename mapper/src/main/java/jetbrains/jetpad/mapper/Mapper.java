/*
 * Copyright 2012-2017 JetBrains s.r.o
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

import com.google.common.collect.Iterators;
import jetbrains.jetpad.base.ThrowableHandlers;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import jetbrains.jetpad.model.collections.set.ObservableSet;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Mapper is an object encapsulating a mapping (usually UI related) from source to target.
 *
 * Responsibilities of a Mapper:
 *  - create and configure view
 *  - create and configure {@link Synchronizer}s
 *  - configure listeners and handlers on the view
 *
 * Mapper can be in one of three states:
 *  - not attached
 *  - attaching synchronizers
 *  - attaching children
 *  - attached
 *  - detached
 *
 * not attached -> attaching synchronizers
 *  - Mapper is not attached
 *  - onBeforeAttach()
 *
 * attaching synchronizers -> attaching children
 *  - registerSynchronizers()
 *
 *  attaching children -> attached
 *  - attaching children
 *  - Mapper is attached
 *  - onAttach()
 *
 * attached -> detached
 *  - attached
 *  - onDetach()
 *  - detached
 *
 * @param <SourceT> - source object
 * @param <TargetT> - target object. Usually it's some kind of view
 */
public abstract class Mapper<SourceT, TargetT> {
  private static final Object[] EMPTY_PARTS = new Object[0];

  private SourceT mySource;
  private TargetT myTarget;
  private MappingContext myMappingContext;
  private State myState = State.NOT_ATTACHED;

  private Object[] myParts = EMPTY_PARTS;
  private Mapper<?, ?> myParent;

  /**
   * Construct a mapper with SourceT source and TargetT target
   * NB: DO NOT create disposable resources in constructors. Use either registerSynchronizers or onAttach method.
   */
  public Mapper(SourceT source, TargetT target) {
    mySource = source;
    myTarget = target;
  }

  public final Mapper<?, ?> getParent() {
    return myParent;
  }

  /**
   * @return Whether this mapper should be findable in {@link MappingContext}
   */
  protected boolean isFindable() {
    return true;
  }

  /**
   * Lifecycle method to register {@link Synchronizer}s in this mapper
   */
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
    if (myState != State.NOT_ATTACHED) {
      throw new IllegalStateException("Mapper can't be reused because it was already detached");
    }

    try {
      onBeforeAttach(ctx);
    } catch (Throwable t) {
      ThrowableHandlers.handle(t);
    }

    myState = State.ATTACHING_SYNCHRONIZERS;
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

    myState = State.ATTACHING_CHILDREN;
    for (Object part : myParts) {
      if (part instanceof ChildContainer) {
        for (Mapper<?, ?> m : (ChildContainer<?>) part) {
          m.attach(ctx);
        }
      }
    }

    myState = State.ATTACHED;

    try {
      onAttach(ctx);
    } catch (Throwable t) {
      ThrowableHandlers.handle(t);
    }
  }

  final void detach() {
    if (myMappingContext == null) {
      throw new IllegalStateException();
    }

    try {
      onDetach();
    } catch (Throwable t) {
      ThrowableHandlers.handle(t);
    }

    for (Object part : myParts) {
      if (part instanceof Synchronizer) {
        Synchronizer s = (Synchronizer) part;
        try {
          s.detach();
        } catch (Throwable t) {
          ThrowableHandlers.handle(t);
        }
      }
      if (part instanceof ChildContainer) {
        for (Mapper<?, ?> m : (ChildContainer<?>) part) {
          m.detach();
        }
      }
    }

    myMappingContext.unregister(this);

    myMappingContext = null;
    myState = State.DETACHED;
    myParts = null;
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

  public final Iterable<Synchronizer> synchronizers() {
    return new Iterable<Synchronizer>() {
      @Override
      public Iterator<Synchronizer> iterator() {
        return new PartsIterator<Synchronizer>() {
          @Override
          protected Synchronizer getNext() {
            return (Synchronizer) myParts[myIndex];
          }

          @Override
          protected int toNext(int index) {
            for (; index < myParts.length; index++) {
              if (myParts[index] instanceof Synchronizer) {
                break;
              }
            }
            return index;
          }
        };
      }
    };
  }

  public final Iterable<Mapper<?, ?>> children() {
    return new Iterable<Mapper<?, ?>>() {
      @Override
      public Iterator<Mapper<?, ?>> iterator() {
        return new PartsIterator<Mapper<?,?>>() {
          private Iterator<? extends Mapper<?, ?>> myChildContainerIterator;

          @Override
          protected Mapper<?, ?> getNext() {
            return myChildContainerIterator.next();
          }

          @Override
          protected int toNext(int index) {
            if (myChildContainerIterator != null && myChildContainerIterator.hasNext()) {
              return index;
            }
            for (; index < myParts.length; index++) {
              if (myParts[index] instanceof ChildContainer) {
                myChildContainerIterator = ((ChildContainer<?>) myParts[index]).iterator();
                break;
              }
            }
            return index;
          }
        };
      }
    };
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
    if (myState != State.ATTACHING_SYNCHRONIZERS && myState != State.ATTACHING_CHILDREN && myState != State.ATTACHED) {
      throw new IllegalStateException("State =  " + myState);
    }

    child.myParent = this;
    if (myState != State.ATTACHING_SYNCHRONIZERS) {
      child.attach(myMappingContext);
    }
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

  private class ChildProperty<MapperT extends Mapper<?, ?>>
      extends ValueProperty<MapperT> implements ChildContainer<MapperT> {
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
    public Iterator<MapperT> iterator() {
      MapperT value = get();
      if (value == null) return Collections.emptyIterator();
      return Iterators.singletonIterator(value);
    }
  }

  private class ChildList<MapperT extends Mapper<?, ?>>
      extends ObservableArrayList<MapperT> implements ChildContainer<MapperT> {
    @Override
    protected void checkAdd(int index, MapperT item) {
      checkCanAdd(item);

      super.checkAdd(index, item);
    }

    @Override
    protected void checkSet(int index, MapperT oldItem, MapperT newItem) {
      checkCanRemove(oldItem);
      checkCanAdd(newItem);

      super.checkSet(index, oldItem, newItem);
    }

    @Override
    protected void checkRemove(int index, MapperT item) {
      checkCanRemove(item);

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
    protected void beforeItemSet(int index, MapperT oldItem, MapperT newItem) {
      removeChild(oldItem);
      addChild(newItem);
      super.beforeItemSet(index, oldItem, newItem);
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
  }

  private class ChildSet<MapperT extends Mapper<?, ?>>
      extends ObservableHashSet<MapperT> implements ChildContainer<MapperT> {
    @Override
    protected void checkAdd(MapperT item) {
      checkCanAdd(item);

      super.checkAdd(item);
    }

    @Override
    protected void checkRemove(MapperT item) {
      checkCanRemove(item);

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
  }

  private interface ChildContainer<MapperT extends Mapper<?, ?>> extends Iterable<MapperT> {
  }

  public interface SynchronizersConfiguration {
    void add(Synchronizer sync);
  }

  private abstract class PartsIterator<ItemT> implements Iterator<ItemT> {
    int myIndex = toNext(0);

    protected abstract ItemT getNext();
    protected abstract int toNext(int index);

    @Override
    public boolean hasNext() {
      return myIndex < myParts.length;
    }

    @Override
    public ItemT next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      ItemT next = getNext();
      myIndex = toNext(myIndex + 1);
      return next;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  private enum State {
    NOT_ATTACHED,
    ATTACHING_SYNCHRONIZERS,
    ATTACHING_CHILDREN,
    ATTACHED,
    DETACHED
  }
}