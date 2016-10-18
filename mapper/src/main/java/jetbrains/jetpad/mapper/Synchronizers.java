/*
 * Copyright 2012-2016 JetBrains s.r.o
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

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.ObservableCollection;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.EventSource;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyBinding;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.model.transform.Transformer;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Utility class for synchronizer creation
 */
public class Synchronizers {
  private static final Logger LOG = Logger.getLogger(Synchronizers.class.getName());

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
  Synchronizer forPropsOneWay(final ReadableProperty<ValueT> source, final WritableProperty<ValueT> target) {
    return new RegistrationSynchronizer() {
      @Override
      protected Registration doAttach(SynchronizerContext ctx) {
        target.set(source.get());
        return source.addHandler(event -> target.set(event.getNewValue()));
      }
    };
  }

  public static <ValueT>
  Synchronizer forPropsTwoWay(final Property<ValueT> source, final Property<ValueT> target) {
    if (source == null || target == null) {
      throw new NullPointerException();
    }

    return new Synchronizer() {
      private ValueT myOldValue;
      private Registration myRegistration;

      @Override
      public void attach(SynchronizerContext ctx) {
        myOldValue = source.get();
        myRegistration = PropertyBinding.bindTwoWay(source, target);
      }

      @Override
      public void detach() {
        myRegistration.remove();
        target.set(myOldValue);
      }
    };
  }

  /**
   * Creates a synchronizer which invokes the specified runnable on changes to the property
   */
  public static <ValueT> Synchronizer forProperty(final ReadableProperty<ValueT> property, final Runnable sync) {
    return forEventSource(property, sync);
  }

  /**
   * Creates a synchronizer which invokes the specified runnable on changes to the collection
   */
  public static <ElementT> Synchronizer forCollection(
      final ObservableCollection<ElementT> collection, final Runnable sync) {
    return new RegistrationSynchronizer() {
      @Override
      protected Registration doAttach(SynchronizerContext ctx) {
        Registration r = collection.addListener(new CollectionAdapter<ElementT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<? extends ElementT> event) {
            sync.run();
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<? extends ElementT> event) {
            sync.run();
          }
        });
        sync.run();
        return r;
      }
    };
  }

  public static Synchronizer forRegistration(final Supplier<Registration> reg) {
    return new RegistrationSynchronizer() {
      @Override
      protected Registration doAttach(SynchronizerContext ctx) {
        return reg.get();
      }
    };
  }

  public static Synchronizer forRegistration(final Registration r) {
    return new Synchronizer() {
      @Override
      public void attach(SynchronizerContext ctx) {}

      @Override
      public void detach() {
        r.remove();
      }
    };
  }

  /**
   * Compose a list of synchronizer into one. Synchronizers are attached
   * in the order in which they are passed and detached in the reverse order
   */
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
        for (int i = syncs.length - 1; i >= 0; i--) {
          syncs[i].detach();
        }
      }
    };
  }

  /**
   * Creates a synchronizer which invokes the specified runnable on an event from the passed {@link EventSource}
   */
  public static Synchronizer forEventSource(final EventSource<?> src, final Runnable r) {
    return new RegistrationSynchronizer() {
      @Override
      protected Registration doAttach(SynchronizerContext ctx) {
        r.run();
        return src.addHandler((EventHandler<Object>) event -> r.run());
      }
    };
  }

  /**
   * Creates a synchronizer which invokes a handler with an event as a parameter when such an event happens on
   * the passed {@link EventSource}
   *
   * NB: It isn't called on attach
   */
  public static <EventT> Synchronizer forEventSource(final EventSource<EventT> src, final Consumer<EventT> h) {
    return new RegistrationSynchronizer() {
      @Override
      protected Registration doAttach(SynchronizerContext ctx) {
        return src.addHandler(h::accept);
      }
    };
  }

  public static Synchronizer measuringSynchronizer(String name, Synchronizer sync) {
    return new Synchronizer() {
      @Override
      public void attach(SynchronizerContext ctx) {
        long start = System.currentTimeMillis();
        sync.attach(ctx);
        log("attach", start);
      }

      @Override
      public void detach() {
        long start = System.currentTimeMillis();
        sync.detach();
        log("detach", start);
      }

      private void log(String event, long start) {
        LOG.info(name + ": " + event + " in " + (System.currentTimeMillis() - start) + " ms");
      }
    };
  }
}