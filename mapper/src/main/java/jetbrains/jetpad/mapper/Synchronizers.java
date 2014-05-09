/*
 * Copyright 2012-2014 JetBrains s.r.o
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

import com.google.common.base.Supplier;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.ObservableCollection;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.EventSource;
import jetbrains.jetpad.model.property.*;
import jetbrains.jetpad.model.transform.Transformer;

import java.util.List;

public class Synchronizers {
  public static <SourceT, TargetT>
  SimpleRoleSynchronizer<SourceT, TargetT> forSimpleRole(
      Mapper<?, ?> mapper,
      List<SourceT> source,
      List<TargetT> target,
      MapperFactory<SourceT, TargetT> factory) {
    return new SimpleRoleSynchronizer<>(mapper, source, target, factory);
  }

  public static <
      SourceT,
      MappedT,
      TargetItemT,
      TargetT extends TargetItemT>
  RoleSynchronizer<MappedT, TargetT> forObservableRole(
      Mapper<?, ?> mapper,
      SourceT source,
      Transformer<SourceT, ObservableList<MappedT>> transformer,
      List<TargetItemT> target,
      MapperFactory<MappedT, TargetT> factory) {
    return new TransformingObservableCollectionRoleSynchronizer<>(mapper, source, transformer, target, factory);
  }

  public static <
      SourceT,
      TargetItemT,
      TargetT extends TargetItemT>
  RoleSynchronizer<SourceT, TargetT> forObservableRole(
      Mapper<?, ?> mapper,
      ObservableList<SourceT> source,
      List<TargetItemT> target,
      MapperFactory<SourceT, TargetT> factory) {
    return new ObservableCollectionRoleSynchronizer<>(mapper, source, target, factory);
  }

  public static <
      SourceT, TargetT, KindTargetT extends TargetT>
  RoleSynchronizer<SourceT, KindTargetT> forConstantRole(
      Mapper<?, ?> mapper,
      final SourceT source,
      final List<TargetT> target,
      MapperFactory<SourceT, KindTargetT> factory) {
    BaseCollectionRoleSynchronizer<SourceT, KindTargetT> result = new BaseCollectionRoleSynchronizer<SourceT, KindTargetT>(mapper) {
      @Override
      protected void onAttach() {
        super.onAttach();
        Mapper<? extends SourceT, ? extends KindTargetT> mapper = createMapper(source);
        getModifiableMappers().add(0, mapper);
        target.add(getModifiableMappers().get(0).getTarget());
        processMapper(mapper);
      }
    };
    result.addMapperFactory(factory);
    return result;
  }

  public static <SourceT, TargetT>
  RoleSynchronizer<SourceT, TargetT> forSingleRole(
      Mapper<?, ?> mapper,
      final ReadableProperty<SourceT> source,
      final WritableProperty<TargetT> target,
      MapperFactory<SourceT, TargetT> factory) {
    return new SingleChildRoleSynchronizer<>(mapper, source, target, factory);
  }

  public static <ValueT>
  Synchronizer forProperty(final ReadableProperty<ValueT> source, final WritableProperty<ValueT> target) {
    return new Synchronizer() {
      private Registration myRegistration;

      @Override
      public void attach(SynchronizerContext ctx) {
        target.set(source.get());
        myRegistration = source.addHandler(new EventHandler<PropertyChangeEvent<ValueT>>() {
          @Override
          public void onEvent(PropertyChangeEvent<ValueT> event) {
            target.set(event.getNewValue());
          }
        });
      }

      @Override
      public void detach() {
        myRegistration.remove();
      }
    };
  }

  public static <ValueT>
  Synchronizer forProperties(final Property<ValueT> source, final Property<ValueT> target) {
    if (source == null || target == null) {
      throw new NullPointerException();
    }

    return new Synchronizer() {
      private ValueT myOldValue;
      private Registration myRegistration;

      @Override
      public void attach(SynchronizerContext ctx) {
        myOldValue = source.get();
        myRegistration = PropertyBinding.bind(source, target);
      }

      @Override
      public void detach() {
        myRegistration.remove();
        target.set(myOldValue);
      }
    };
  }

  public static <ValueT> Synchronizer forProperty(final ReadableProperty<ValueT> property, final Runnable sync) {
    return forEventSource(property, sync);
  }

  public static <ElementT> Synchronizer forCollection(
      final ObservableCollection<ElementT> collection, final Runnable sync) {
    return new Synchronizer() {
      private Registration myCollectionRegistration;

      @Override
      public void attach(SynchronizerContext ctx) {
        myCollectionRegistration = collection.addListener(new CollectionAdapter<ElementT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<ElementT> event) {
            sync.run();
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<ElementT> event) {
            sync.run();
          }
        });
        sync.run();
      }

      @Override
      public void detach() {
        myCollectionRegistration.remove();
      }
    };
  }

  public static Synchronizer forRegistration(final Supplier<Registration> reg) {
    return new Synchronizer() {
      Registration myReg;

      @Override
      public void attach(SynchronizerContext ctx) {
        myReg = reg.get();
      }

      @Override
      public void detach() {
        myReg.remove();
      }
    };
  }

  public static Synchronizer forRegistration(final Registration r) {
    return new Synchronizer() {
      @Override
      public void attach(SynchronizerContext ctx) {
      }

      @Override
      public void detach() {
        r.remove();
      }
    };
  }

  public static Synchronizer composite(final Synchronizer... syncs) {
    return new Synchronizer() {
      @Override
      public void attach(SynchronizerContext ctx) {
        for (Synchronizer s : syncs) {
          s.attach(ctx);
        }
      }

      @Override
      public void detach() {
        for (Synchronizer s : syncs) {
          s.detach();
        }
      }
    };
  }

  public static Synchronizer forEventSource(final EventSource<?> src, final Runnable r) {
    return new Synchronizer() {
      private Registration myReg;

      @Override
      public void attach(SynchronizerContext ctx) {
        r.run();
        myReg = src.addHandler(new EventHandler<Object>() {
          @Override
          public void onEvent(Object event) {
            r.run();
          }
        });
      }

      @Override
      public void detach() {
        myReg.remove();
      }
    };
  }
}