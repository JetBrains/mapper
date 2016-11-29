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
package jetbrains.jetpad.model.transform;

import jetbrains.jetpad.base.Functions;
import jetbrains.jetpad.base.Objects;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.function.Function;
import jetbrains.jetpad.base.function.Supplier;
import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.collections.ObservableCollection;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.MultiWaySync;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.model.property.ValueProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class Transformers {
  public static <ItemT> Transformer<ItemT, ItemT> identity() {
    return coerce();
  }

  public static <TargetT, SourceT extends TargetT> Transformer<SourceT, TargetT> coerce() {
    return new BaseTransformer<SourceT, TargetT>() {
      @Override
      public Transformation<SourceT, TargetT> transform(final SourceT from) {
        return new Transformation<SourceT, TargetT>() {
          @Override
          public SourceT getSource() {
            return from;
          }

          @Override
          public TargetT getTarget() {
            return from;
          }
        };
      }

      @Override
      public Transformation<SourceT, TargetT> transform(SourceT from, TargetT to) {
        throw new UnsupportedOperationException();
      }
    };
  }

  public static <ItemT> Transformer<ObservableList<ItemT>, ObservableList<? extends ItemT>> coerceList() {
    return new BaseTransformer<ObservableList<ItemT>, ObservableList<? extends ItemT>>() {
      @Override
      public Transformation<ObservableList<ItemT>, ObservableList<? extends ItemT>> transform(final ObservableList<ItemT> from) {
        return new Transformation<ObservableList<ItemT>, ObservableList<? extends ItemT>>() {
          @Override
          public ObservableList<ItemT> getSource() {
            return from;
          }

          @Override
          public ObservableList<? extends ItemT> getTarget() {
            return from;
          }
        };

      }

      @Override
      public Transformation<ObservableList<ItemT>, ObservableList<? extends ItemT>> transform(ObservableList<ItemT> from, ObservableList<? extends ItemT> to) {
        throw new UnsupportedOperationException();
      }
    };
  }

  public static <SourceT, TargetT> Transformer<SourceT, TargetT> fromFun(final Function<SourceT, TargetT> f) {
    return new BaseTransformer<SourceT, TargetT>() {
      @Override
      public Transformation<SourceT, TargetT> transform(final SourceT from) {
        final TargetT target = f.apply(from);
        return new Transformation<SourceT, TargetT>() {
          @Override
          public SourceT getSource() {
            return from;
          }

          @Override
          public TargetT getTarget() {
            return target;
          }
        };
      }

      @Override
      public Transformation<SourceT, TargetT> transform(SourceT from, TargetT to) {
        throw new UnsupportedOperationException();
      }
    };
  }

  public static <SourceT, TargetT>
  Transformer<ObservableList<SourceT>, ObservableList<TargetT>> listMap(final Transformer<SourceT, TargetT> transformer) {
    return new BaseTransformer<ObservableList<SourceT>, ObservableList<TargetT>>() {
      @Override
      public Transformation<ObservableList<SourceT>, ObservableList<TargetT>> transform(ObservableList<SourceT> from) {
        return transform(from, new ObservableArrayList<TargetT>());
      }

      @Override
      public Transformation<ObservableList<SourceT>, ObservableList<TargetT>> transform(final ObservableList<SourceT> from, final ObservableList<TargetT> to) {
        final List<Registration> itemRegistrations = new ArrayList<>();

        final CollectionListener<SourceT> listener = new CollectionListener<SourceT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<? extends SourceT> event) {
            final Transformation<SourceT, TargetT> transformation = transformer.transform(event.getNewItem());
            to.add(event.getIndex(), transformation.getTarget());
            itemRegistrations.add(event.getIndex(), Registration.from(transformation));
          }

          @Override
          public void onItemSet(CollectionItemEvent<? extends SourceT> event) {
            final Transformation<SourceT, TargetT> transformation = transformer.transform(event.getNewItem());
            to.set(event.getIndex(), transformation.getTarget());
            itemRegistrations.set(event.getIndex(), Registration.from(transformation))
              .remove();
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<? extends SourceT> event) {
            to.remove(event.getIndex());
            itemRegistrations.remove(event.getIndex()).remove();
          }
        };


        for (int i = 0; i < from.size(); i++) {
          listener.onItemAdded(new CollectionItemEvent<>(null, from.get(i), i, CollectionItemEvent.EventType.ADD));
        }

        final Registration reg = from.addListener(listener);
        return new SimpleTransformation<>(from, to, new Registration() {
          @Override
          protected void doRemove() {
            for (Registration r : itemRegistrations) {
              r.remove();
            }
            reg.remove();
          }
        });
      }
    };
  }
  public static <SourceT, TargetT>
  Transformer<ObservableList<SourceT>, ObservableList<TargetT>> listMap(final Function<SourceT, TargetT> f) {
    return listMap(fromFun(f));
  }

  public static <SpecItemT, ItemT extends SpecItemT, ValueT extends Comparable<ValueT>, CollectionT extends ObservableCollection<ItemT>>
  Transformer<CollectionT, ObservableList<ItemT>> sortBy(final Function<SpecItemT, ? extends ReadableProperty<ValueT>> propSpec) {
    return sortBy(propSpec, Order.ASCENDING);
  }

  public static <SpecItemT, ItemT extends SpecItemT, ValueT extends Comparable<ValueT>, CollectionT extends ObservableCollection<ItemT>>
  Transformer<CollectionT, ObservableList<ItemT>> sortBy(final Function<SpecItemT, ? extends ReadableProperty<ValueT>> propSpec, final Order order) {
    return sortBy(propSpec, new Comparator<ValueT>() {
      @Override
      public int compare(ValueT o1, ValueT o2) {
        if (order == Order.DESCENDING) {
          return -o1.compareTo(o2);
        }
        return o1.compareTo(o2);
      }
    });
  }

  public static <SpecItemT, ItemT extends SpecItemT, ValueT, CollectionT extends ObservableCollection<ItemT>>
  Transformer<CollectionT, ObservableList<ItemT>> sortBy(final Function<SpecItemT, ? extends ReadableProperty<ValueT>> propSpec, final Comparator<ValueT> cmp) {
    final Comparator<ItemT> comparator = new Comparator<ItemT>() {
      @Override
      public int compare(ItemT i1, ItemT i2) {
        ReadableProperty<ValueT> p1 = propSpec.apply(i1);
        ReadableProperty<ValueT> p2 = propSpec.apply(i2);

        if (p1 == null || p2 == null) {
          throw new NullPointerException();
        }

        ValueT v1 = p1.get();
        ValueT v2 = p2.get();

        if (v1 == null || v2 == null) {
          return compareNulls(v1, v2);
        }

        return cmp.compare(v1, v2);
      }
    };

    return new BaseTransformer<CollectionT, ObservableList<ItemT>>() {
      @Override
      public Transformation<CollectionT, ObservableList<ItemT>> transform(CollectionT from) {
        return transform(from, new ObservableArrayList<ItemT>());
      }

      @Override
      public Transformation<CollectionT, ObservableList<ItemT>> transform(final CollectionT from, final ObservableList<ItemT> to) {
        return new Transformation<CollectionT, ObservableList<ItemT>>() {
          Registration myCollectionReg;
          CollectionListener<ItemT> myCollectionListener;
          Map<ItemT, Registration> myListeners = new HashMap<>();

          {
            myCollectionReg = from.addListener(myCollectionListener = new CollectionAdapter<ItemT>() {
              @Override
              public void onItemAdded(CollectionItemEvent<? extends ItemT> event) {
                ItemT item = event.getNewItem();
                watch(item, to);

                int pos = Collections.binarySearch(to, item, comparator);
                int insertIndex = pos >= 0 ? pos + 1 : -(pos + 1);
                to.add(insertIndex, item);
              }

              @Override
              public void onItemRemoved(CollectionItemEvent<? extends ItemT> event) {
                ItemT item = event.getOldItem();

                int sortedIndex = to.indexOf(item);
                if (sortedIndex == -1) {
                  throw new IllegalStateException();
                }

                to.remove(sortedIndex);
                unwatch(item);
              }
            });


            for (ItemT item : from) {
              watch(item, to);
              to.add(item);
            }
            Collections.sort(to, comparator);
          }


          @Override
          public CollectionT getSource() {
            return from;
          }

          @Override
          public ObservableList<ItemT> getTarget() {
            return to;
          }

          @Override
          protected void doDispose() {
            myCollectionReg.remove();
            for (ItemT item : from) {
              unwatch(item);
            }
          }

          private void watch(final ItemT item, final ObservableList<ItemT> to) {
            ReadableProperty<ValueT> property = propSpec.apply(item);
            if (property == null) {
              throw new NullPointerException();
            }
            myListeners.put(item, property.addHandler(new EventHandler<PropertyChangeEvent<ValueT>>() {
              @Override
              public void onEvent(PropertyChangeEvent<ValueT> event) {
                boolean needMove = false;
                int sortedIndex = to.indexOf(item);
                if (sortedIndex > 0) {
                  ItemT before = to.get(sortedIndex - 1);
                  if (comparator.compare(before, item) > 0) {
                    needMove = true;
                  }
                }
                if (sortedIndex < to.size() - 1) {
                  ItemT after = to.get(sortedIndex + 1);
                  if (comparator.compare(item, after) > 0) {
                    needMove = true;
                  }
                }
                if (needMove) {
                  myCollectionListener.onItemSet(new CollectionItemEvent<>(item, item, -1, CollectionItemEvent.EventType.SET));
                }
              }
            }));
          }

          private void unwatch(ItemT item) {
            myListeners.remove(item).remove();
          }
        };
      }
    };
  }

  public static <SpecItemT, ItemT extends SpecItemT, ValueT, CollectionT extends ObservableCollection<ItemT>>
  Transformer<CollectionT, ObservableList<ItemT>> sortByConstant(final Function<SpecItemT, ? extends ValueT> propSpec, final Comparator<ValueT> cmp) {
    final Comparator<ItemT> comparator = new Comparator<ItemT>() {
      @Override
      public int compare(ItemT i1, ItemT i2) {
        ValueT v1 = propSpec.apply(i1);
        ValueT v2 = propSpec.apply(i2);

        if (v1 == null || v2 == null) {
          return compareNulls(v1, v2);
        }

        return cmp.compare(v1, v2);
      }
    };

    return new BaseTransformer<CollectionT, ObservableList<ItemT>>() {
      @Override
      public Transformation<CollectionT, ObservableList<ItemT>> transform(CollectionT from) {
        //tree list has much better asymptotics of insert
        return transform(from, new ObservableArrayList<ItemT>());
      }

      @Override
      public Transformation<CollectionT, ObservableList<ItemT>> transform(final CollectionT from, final ObservableList<ItemT> to) {
        return new Transformation<CollectionT, ObservableList<ItemT>>() {
          Registration myCollectionReg;
          CollectionListener<ItemT> myCollectionListener;

          {
            myCollectionReg = from.addListener(myCollectionListener = new CollectionAdapter<ItemT>() {
              @Override
              public void onItemAdded(CollectionItemEvent<? extends ItemT> event) {
                ItemT item = event.getNewItem();

                int pos = Collections.binarySearch(to, item, comparator);
                int insertIndex = pos >= 0 ? pos + 1 : -(pos + 1);
                to.add(insertIndex, item);
              }

              @Override
              public void onItemRemoved(CollectionItemEvent<? extends ItemT> event) {
                ItemT item = event.getOldItem();

                int sortedIndex = to.indexOf(item);
                if (sortedIndex == -1) {
                  throw new IllegalStateException();
                }

                to.remove(sortedIndex);
              }
            });


            for (ItemT item : from) {
              to.add(item);
            }
            Collections.sort(to, comparator);
          }


          @Override
          public CollectionT getSource() {
            return from;
          }

          @Override
          public ObservableList<ItemT> getTarget() {
            return to;
          }

          @Override
          protected void doDispose() {
            myCollectionReg.remove();
            getTarget().clear();
          }
        };
      }
    };
  }

  private static int compareNulls(Object o1, Object o2) {
    if (o1 == o2) return 0;
    if (o1 == null) {
      return -1;
    } else {
      return 1;
    }
  }


  public static <ItemT, CollectionT extends ObservableCollection<ItemT>>
  Transformer<CollectionT, ObservableList<ItemT>> listFilter(final Function<ItemT, ReadableProperty<Boolean>> filterBy) {
    return new BaseFilterTransformer<ItemT, CollectionT, ObservableList<ItemT>>(filterBy) {
      @Override
      protected void add(ItemT item, CollectionT from, ObservableList<ItemT> to) {
        Iterator<ItemT> fromItr = from.iterator();
        int index = 0;
        boolean foundItem = false;
        for (ItemT curTo: to) {
          while (fromItr.hasNext()) {
            ItemT curFrom = fromItr.next();
            if (curFrom == curTo) {
              break;
            }
            if (curFrom == item) {
              foundItem = true;
              break;
            }
          }
          if (foundItem) {
            break;
          }
          index++;
        }
        if (!fromItr.hasNext() && !foundItem) {
          throw new IllegalStateException("item " + item + " has not been found in " + from);
        }
        to.add(index, item);
      }
      @Override
      protected ObservableList<ItemT> createTo() {
        return new ObservableArrayList<>();
      }
    };
  }

  public static <ItemT, CollectionT extends ObservableCollection<ItemT>>
  Transformer<CollectionT, ObservableCollection<ItemT>> filter(final Function<ItemT, ReadableProperty<Boolean>> filterBy) {
    return new BaseFilterTransformer<ItemT, CollectionT, ObservableCollection<ItemT>>(filterBy) {
      @Override
      protected void add(ItemT item, CollectionT from, ObservableCollection<ItemT> to) {
        to.add(item);
      }
      @Override
      protected ObservableCollection<ItemT> createTo() {
        return new ObservableHashSet<>();
      }
    };
  }

  public static <ItemT, CollectionT extends ObservableCollection<ItemT>>
  Transformer<CollectionT, ObservableCollection<ItemT>> filterByConstant(final Function<ItemT, Boolean> filterBy) {

    return new BaseTransformer<CollectionT, ObservableCollection<ItemT>>() {
      @Override
      public Transformation<CollectionT, ObservableCollection<ItemT>> transform(CollectionT from) {
        return transform(from, new ObservableArrayList<ItemT>());
      }

      @Override
      public Transformation<CollectionT, ObservableCollection<ItemT>> transform(final CollectionT from, final ObservableCollection<ItemT> to) {
        return new Transformation<CollectionT, ObservableCollection<ItemT>>() {
          private Registration myReg;

          {
            for (ItemT item : from) {
              if (filterBy.apply(item)) {
                to.add(item);
              }
            }

            myReg = from.addListener(new CollectionAdapter<ItemT>() {
              @Override
              public void onItemAdded(CollectionItemEvent<? extends ItemT> event) {
                if (filterBy.apply(event.getNewItem())) {
                  to.add(event.getNewItem());
                }
              }

              @Override
              public void onItemRemoved(CollectionItemEvent<? extends ItemT> event) {
                if (filterBy.apply(event.getOldItem())) {
                  to.remove(event.getOldItem());
                }
              }
            });
          }

          @Override
          protected void doDispose() {
            myReg.remove();
            getTarget().clear();
            super.doDispose();
          }

          @Override
          public CollectionT getSource() {
            return from;
          }

          @Override
          public ObservableCollection<ItemT> getTarget() {
            return to;
          }
        };
      }
    };
  }


  public static <SourceT, TargetT>
  Transformer<ObservableCollection<SourceT>, ObservableCollection<TargetT>> oneToOne(final Function<SourceT, TargetT> converter, final Function<TargetT, SourceT> checker) {
    return new BaseTransformer<ObservableCollection<SourceT>, ObservableCollection<TargetT>>() {
      @Override
      public Transformation<ObservableCollection<SourceT>, ObservableCollection<TargetT>> transform(ObservableCollection<SourceT> from) {
        return transform(from, new ObservableHashSet<TargetT>());
      }

      @Override
      public Transformation<ObservableCollection<SourceT>, ObservableCollection<TargetT>> transform(final ObservableCollection<SourceT> from, final ObservableCollection<TargetT> to) {
        return new Transformation<ObservableCollection<SourceT>, ObservableCollection<TargetT>>() {
          private Registration myCollectionRegistration;

          {
            for (SourceT item: from) {
              add(item);
            }

            myCollectionRegistration = from.addListener(new CollectionAdapter<SourceT>() {
              @Override
              public void onItemAdded(CollectionItemEvent<? extends SourceT> event) {
                add(event.getNewItem());
              }

              @Override
              public void onItemRemoved(CollectionItemEvent<? extends SourceT> event) {
                SourceT item = event.getOldItem();
                if (!exists(item)) return;
                for (Iterator<TargetT> i = to.iterator(); i.hasNext(); ) {
                  TargetT t = i.next();
                  if (Objects.equal(checker.apply(t), item)) {
                    i.remove();
                    return;
                  }
                }
              }
            });
          }

          @Override
          public ObservableCollection<SourceT> getSource() {
            return from;
          }

          @Override
          public ObservableCollection<TargetT> getTarget() {
            return to;
          }

          @Override
          protected void doDispose() {
            myCollectionRegistration.remove();
          }

          private boolean exists(SourceT item) {
            for (TargetT t: to) {
              if (Objects.equal(checker.apply(t), item)) return true;
            }
            return false;
          }

          private void add(SourceT item) {
            if (!exists(item)) {
              to.add(converter.apply(item));
            }
          }
        };
      }
    };
  }

  // Non-unique values not supported: for all possible pairs equals() must return false. Null items also not supported.
  public static <ItemT>
  Transformer<ObservableList<ItemT>, ObservableList<Property<ItemT>>> toPropsListTwoWay() {
    return new BaseTransformer<ObservableList<ItemT>, ObservableList<Property<ItemT>>>() {
      @Override
      public Transformation<ObservableList<ItemT>, ObservableList<Property<ItemT>>> transform(ObservableList<ItemT> from) {
        return transform(from, new ObservableArrayList<Property<ItemT>>());
      }

      @Override
      public Transformation<ObservableList<ItemT>, ObservableList<Property<ItemT>>> transform(final ObservableList<ItemT> from, final ObservableList<Property<ItemT>> to) {
        if (!to.isEmpty()) {
          throw new IllegalArgumentException("'To' list must be empty");
        }
        return new Transformation<ObservableList<ItemT>, ObservableList<Property<ItemT>>>() {
          private final Registration myListsReg;
          private final List<Registration> myPropertiesRegs = new ArrayList<>();
          private final MultiWaySync mySyncing = new MultiWaySync();

          {
            final EventHandler<PropertyChangeEvent<ItemT>> propertyChangePropagator = new EventHandler<PropertyChangeEvent<ItemT>>() {
              @Override
              public void onEvent(PropertyChangeEvent<ItemT> event) {
                int index = from.indexOf(event.getOldValue());
                if (!to.get(index).get().equals(event.getNewValue())) {
                  throw new IllegalStateException("Duplicate detected, first entry index=" + index
                      + ", value=" + to.get(index).get());
                }
                from.set(index, event.getNewValue());
              }
            };

            final EventHandler<CollectionItemEvent<? extends ItemT>> forwardListener = new EventHandler<CollectionItemEvent<? extends ItemT>>() {
              @Override
              public void onEvent(CollectionItemEvent<? extends ItemT> event) {
                switch (event.getType()) {
                  case ADD:
                    Property<ItemT> newProperty = new ValueProperty<ItemT>(event.getNewItem());
                    Registration newPropertyReg = mySyncing.inSync(newProperty).addHandler(propertyChangePropagator);
                    myPropertiesRegs.add(event.getIndex(), newPropertyReg);
                    to.add(event.getIndex(), newProperty);
                    break;
                  case REMOVE:
                    to.remove(event.getIndex());
                    myPropertiesRegs.remove(event.getIndex()).remove();
                    break;
                  case SET:
                    to.get(event.getIndex()).set(event.getNewItem());
                    break;
                }
              }
            };

            EventHandler<CollectionItemEvent<? extends Property<ItemT>>> backwardListener = new EventHandler<CollectionItemEvent<? extends Property<ItemT>>>() {
              @Override
              public void onEvent(CollectionItemEvent<? extends Property<ItemT>> event) {
                switch (event.getType()) {
                  case ADD:
                    Property<ItemT> newProperty = event.getNewItem();
                    Registration newPropertyReg = mySyncing.inSync(newProperty).addHandler(propertyChangePropagator);
                    myPropertiesRegs.add(event.getIndex(), newPropertyReg);
                    from.add(event.getIndex(), newProperty.get());
                    break;
                  case SET:
                    Property<ItemT> setProperty = event.getNewItem();
                    Registration setPropertyReg = mySyncing.inSync(setProperty).addHandler(propertyChangePropagator);
                    Registration oldPropertyReg = myPropertiesRegs.set(event.getIndex(), setPropertyReg);
                    oldPropertyReg.remove();
                    from.set(event.getIndex(), setProperty.get());
                    break;
                  case REMOVE:
                    from.remove(event.getIndex());
                    myPropertiesRegs.remove(event.getIndex()).remove();
                    break;
                }
              }
            };

            myListsReg = new CompositeRegistration(
              mySyncing.inSync(from).addHandler(forwardListener),
              mySyncing.inSync(to).addHandler(backwardListener));

            for (final ListIterator<ItemT> i = from.listIterator(); i.hasNext(); ) {
              final int index = i.nextIndex();
              mySyncing.sync(new Runnable() {
                @Override
                public void run() {
                  forwardListener.onEvent(new CollectionItemEvent<>(null, i.next(), index, CollectionItemEvent.EventType.ADD));
                }
              });
            }
          }

          @Override
          public ObservableList<ItemT> getSource() {
            return from;
          }

          @Override
          public ObservableList<Property<ItemT>> getTarget() {
            return to;
          }

          @Override
          protected void doDispose() {
            for (Registration reg : myPropertiesRegs) {
              reg.remove();
            }
            myPropertiesRegs.clear();
            myListsReg.remove();
          }
        };
      }
    };
  }

  public static <ItemT> Transformer<ObservableList<ItemT>, ObservableList<ItemT>> firstN(final ReadableProperty<Integer> value) {
    return new BaseTransformer<ObservableList<ItemT>, ObservableList<ItemT>>() {
      @Override
      public Transformation<ObservableList<ItemT>, ObservableList<ItemT>> transform(ObservableList<ItemT> from) {
        return transform(from, new ObservableArrayList<ItemT>());
      }

      @Override
      public Transformation<ObservableList<ItemT>, ObservableList<ItemT>> transform(final ObservableList<ItemT> from, final ObservableList<ItemT> to) {
        final Registration fromReg = from.addListener(new CollectionListener<ItemT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<? extends ItemT> event) {
            int n = value.get();
            if (event.getIndex() >= n) return;
            if (to.size() == n) {
              to.remove(n - 1);
            }
            to.add(event.getIndex(), event.getNewItem());
          }

          @Override
          public void onItemSet(CollectionItemEvent<? extends ItemT> event) {
            if (event.getIndex() >= value.get()) return;
            to.set(event.getIndex(), event.getNewItem());
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<? extends ItemT> event) {
            int n = value.get();
            if (event.getIndex() >= n) return;
            to.remove(event.getIndex());
            if (from.size() >= n) {
              to.add(from.get(n - 1));
            }
          }
        });

        final Registration propReg = value.addHandler(new EventHandler<PropertyChangeEvent<Integer>>() {
          @Override
          public void onEvent(PropertyChangeEvent<Integer> event) {
            int n = event.getNewValue();
            if (event.getNewValue() > event.getOldValue()) {
              int maxItem = Math.min(n, from.size());
              for (int i = event.getOldValue(); i < maxItem; i++) {
                to.add(from.get(i));
              }
            } else {
              if (to.size() > n) {
                for (int i = to.size() - 1; i >= n; i--) {
                  to.remove(i);
                }
              }
            }
          }
        });

        int n = value.get();
        int maxItem = Math.min(n, from.size());
        for (int i = 0; i < maxItem; i++) {
          to.add(from.get(i));
        }

        return new SimpleTransformation<>(from, to, new CompositeRegistration(fromReg, propReg));
      }
    };
  }

  public static <ItemT>
  Transformer<ObservableList<ObservableList<? extends ItemT>>, ObservableList<ItemT>> flattenList() {
    return flattenList(Functions.<ObservableList<? extends ItemT>>identity());
  }

  public static <SourceT, TargetT>
  Transformer<ObservableList<SourceT>, ObservableList<TargetT>> flattenList(final Function<SourceT, ? extends ObservableList<? extends TargetT>> f) {
    return Transformers.flattenList(f, Transformers.<ObservableList<? extends TargetT>>identity());
  }

  public static <SourceT, SelectedT, ResultT>
  Transformer<ObservableList<SourceT>, ObservableList<ResultT>> flattenList(final Function<SourceT, ? extends SelectedT> f, final Transformer<SelectedT, ? extends ObservableList<? extends ResultT>> t) {
    return new BaseTransformer<ObservableList<SourceT>, ObservableList<ResultT>>() {
      @Override
      public Transformation<ObservableList<SourceT>, ObservableList<ResultT>> transform(ObservableList<SourceT> from) {
        return transform(from, new ObservableArrayList<ResultT>());
      }

      @Override
      public Transformation<ObservableList<SourceT>, ObservableList<ResultT>> transform(final ObservableList<SourceT> from, final ObservableList<ResultT> to) {
        final Map<SourceT, Registration> registrations = new IdentityHashMap<>();
        final Map<SourceT, Integer> sizes = new IdentityHashMap<>();

        CollectionAdapter<SourceT> sourceListener = new CollectionAdapter<SourceT>() {
          @Override
          public void onItemAdded(final CollectionItemEvent<? extends SourceT> event) {
            SelectedT selected = f.apply(event.getNewItem());
            final Transformation<SelectedT, ? extends ObservableList<? extends ResultT>> transform = t.transform(selected);
            ObservableList<? extends ResultT> target = transform.getTarget();

            int startIndex = getStartResultIndex(event.getNewItem(), from, sizes);
            sizes.put(event.getNewItem(), target.size());
            for (ResultT r: target) {
              to.add(startIndex++, r);
            }

            final Registration reg = watch(event.getNewItem(), target);

            registrations.put(event.getNewItem(), new Registration() {
              @Override
              protected void doRemove() {
                reg.remove();
                transform.dispose();
              }
            });
          }

          private <ItemT extends ResultT> Registration watch(final SourceT container, ObservableList<ItemT> list) {
            return list.addListener(new CollectionAdapter<ItemT>() {
              @Override
              public void onItemAdded(CollectionItemEvent<? extends ItemT> event) {
                int startIndex = getStartResultIndex(container, from, sizes);
                to.add(startIndex + event.getIndex(), event.getNewItem());
                sizes.put(container, sizes.get(container) + 1);
              }

              @Override
              public void onItemRemoved(CollectionItemEvent<? extends ItemT> event) {
                to.remove(event.getOldItem());
                sizes.put(container, sizes.get(container) - 1);
              }
            });
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<? extends SourceT> event) {
            SelectedT selected = f.apply(event.getOldItem());
            Transformation<SelectedT, ? extends ObservableList<? extends ResultT>> transformation = t.transform(selected);

            to.removeAll(transformation.getTarget());
            sizes.remove(event.getOldItem());

            transformation.dispose();
            registrations.remove(event.getOldItem()).remove();
          }
        };

        final Registration sourceRegistration = from.addListener(sourceListener);
        int index = 0;
        for (SourceT s : from) {
          sourceListener.onItemAdded(new CollectionItemEvent<>(null, s, index++, CollectionItemEvent.EventType.ADD));
        }

        return new SimpleTransformation<>(from, to, new Registration() {
          @Override
          protected void doRemove() {
            for (SourceT s : from) {
              registrations.remove(s).remove();
            }
            sourceRegistration.remove();
          }
        });
      }

      private int getStartResultIndex(SourceT event, ObservableList<SourceT> sourceList, Map<SourceT, Integer> sizes) {
        int resultIndex = 0;
        Iterator<SourceT> iterator = sourceList.iterator();
        SourceT current = iterator.next();
        while (current != event) {
          resultIndex += sizes.get(current);
          current = iterator.next();
        }
        return resultIndex;
      }
    };
  }

  public static <ValueT, PropertyT extends ReadableProperty<ValueT>>
  Transformer<ObservableList<PropertyT>, ObservableList<ValueT>> flattenPropertyList() {
    return new BaseTransformer<ObservableList<PropertyT>, ObservableList<ValueT>>() {
      @Override
      public Transformation<ObservableList<PropertyT>, ObservableList<ValueT>> transform(ObservableList<PropertyT> from) {
        return transform(from, new ObservableArrayList<ValueT>());
      }

      @Override
      public Transformation<ObservableList<PropertyT>, ObservableList<ValueT>> transform(final ObservableList<PropertyT> from, final ObservableList<ValueT> to) {
        final List<Registration> propRegistrations = new ArrayList<>();
        CollectionAdapter<PropertyT> listener = new CollectionAdapter<PropertyT>() {
          @Override
          public void onItemAdded(final CollectionItemEvent<? extends PropertyT> listEvent) {
            propRegistrations.add(listEvent.getIndex(), listEvent.getNewItem().addHandler(new EventHandler<PropertyChangeEvent<ValueT>>() {
              @Override
              public void onEvent(PropertyChangeEvent<ValueT> propEvent) {
                int index = from.indexOf(listEvent.getNewItem());
                to.set(index, propEvent.getNewValue());
              }
            }));
            to.add(listEvent.getIndex(), listEvent.getNewItem().get());
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<? extends PropertyT> listEvent) {
            propRegistrations.remove(listEvent.getIndex()).remove();
            to.remove(listEvent.getIndex());
          }
        };

        for (int i = 0; i < from.size(); i++) {
          listener.onItemAdded(new CollectionItemEvent<>(null, from.get(i), i, CollectionItemEvent.EventType.ADD));
        }

        final Registration reg = from.addListener(listener);
        return new SimpleTransformation<>(from, to, new Registration() {
          @Override
          protected void doRemove() {
            reg.remove();
            for (Registration r : propRegistrations) {
              r.remove();
            }
            propRegistrations.clear();
            to.clear();
          }
        });
      }
    };
  }

  /**
   * Select only those with the highest priority. Null items are not allowed.
   * Warning: target collection is not protected from outside writes.
   * @param getPriority The greater is number, the higher is priority. Null priority
   * is not allowed. The same priority must always be returned for the same element.
   */
  public static <ItemT>
  Transformer<ObservableCollection<ItemT>, ObservableCollection<ItemT>> highestPriority(
      final Function<ItemT, Integer> getPriority) {
    if (getPriority == null) {
      throw new IllegalArgumentException("Null getPriority is not allowed");
    }
    return new BaseTransformer<ObservableCollection<ItemT>, ObservableCollection<ItemT>>() {
      @Override
      public Transformation<ObservableCollection<ItemT>, ObservableCollection<ItemT>> transform(
          ObservableCollection<ItemT> from) {
        return transform(from, new ObservableHashSet<ItemT>());
      }

      @Override
      public Transformation<ObservableCollection<ItemT>, ObservableCollection<ItemT>> transform(
          final ObservableCollection<ItemT> from, final ObservableCollection<ItemT> to) {
        abstract class FromCollectionAdapter extends CollectionAdapter<ItemT> {
          abstract void initToCollection();
        }

        FromCollectionAdapter listener = new FromCollectionAdapter() {
          private int myHighestPriority;

          @Override
          void initToCollection() {
            myHighestPriority = Integer.MIN_VALUE;
            for (ItemT item : from) {
              insertToToCollection(item);
            }
          }

          private void insertToToCollection(ItemT item) {
            if (item == null) {
              throw new IllegalArgumentException("Null items are not allowed");
            }
            //noinspection ConstantConditions
            Integer newItemPrio = getPriority.apply(item);
            if (newItemPrio == null) {
              throw new IllegalArgumentException("Null priorities are not allowed, item=" + item);
            } else if (newItemPrio > myHighestPriority) {
              to.clear();
              to.add(item);
              myHighestPriority = newItemPrio;
            } else if (newItemPrio == myHighestPriority) {
              to.add(item);
            }
          }

          @Override
          public void onItemAdded(CollectionItemEvent<? extends ItemT> event) {
            insertToToCollection(event.getNewItem());
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<? extends ItemT> event) {
            ItemT oldItem = event.getOldItem();
            //noinspection ConstantConditions
            Integer oldItemPrio = getPriority.apply(oldItem);
            if (oldItemPrio == null) {
              throw new IllegalStateException("Old item priority unexpectedly got null, item=" + oldItem);
            } else if (oldItemPrio > myHighestPriority) {
              // Can happen only in case of getPriority or concurrency issue
              throw new IllegalStateException("Abnormal state: found missed high-priority item " + oldItem
                  + ", oldItemPrio=" + oldItemPrio + ", myHighestPriority=" + myHighestPriority);
            } else if (oldItemPrio == myHighestPriority) {
              to.remove(oldItem);
              if (to.isEmpty()) {
                initToCollection();
              }
            }
          }
        };

        listener.initToCollection();

        return new SimpleTransformation<>(from, to, from.addListener(listener));
      }
    };
  }

  public static <SourceT, TargetT>
  Transformer<ObservableCollection<SourceT>, ObservableCollection<TargetT>> flatten(final Function<SourceT, ObservableCollection<TargetT>> f) {
    return Transformers.flatten(f, Transformers.<ObservableCollection<TargetT>>identity());
  }

  public static <SourceT, SelectedT, ResultT>
  Transformer<ObservableCollection<SourceT>, ObservableCollection<ResultT>> flatten(final Function<SourceT, SelectedT> f, final Transformer<SelectedT, ? extends ObservableCollection<ResultT>> t) {
    return new BaseTransformer<ObservableCollection<SourceT>, ObservableCollection<ResultT>>() {
      @Override
      public Transformation<ObservableCollection<SourceT>, ObservableCollection<ResultT>> transform(ObservableCollection<SourceT> source) {
        return transform(source, new ObservableHashSet<ResultT>());
      }

      @Override
      public Transformation<ObservableCollection<SourceT>, ObservableCollection<ResultT>> transform(final ObservableCollection<SourceT> from, final ObservableCollection<ResultT> to) {
        final CollectionListener<ResultT> nestedListener = new CollectionAdapter<ResultT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<? extends ResultT> event) {
            to.add(event.getNewItem());
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<? extends ResultT> event) {
            to.remove(event.getOldItem());
          }
        };

        final Map<SourceT, Registration> registrations = new HashMap<>();
        CollectionAdapter<SourceT> sourceListener = new CollectionAdapter<SourceT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<? extends SourceT> event) {
            SelectedT subcollection = f.apply(event.getNewItem());
            final Transformation<SelectedT, ? extends ObservableCollection<ResultT>> transform = t.transform(subcollection);
            ObservableCollection<ResultT> target = transform.getTarget();
            to.addAll(target);
            final Registration reg = target.addListener(nestedListener);
            registrations.put(event.getNewItem(), new Registration() {
              @Override
              protected void doRemove() {
                reg.remove();
                transform.dispose();
              }
            });
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<? extends SourceT> event) {
            SelectedT selected = f.apply(event.getOldItem());
            Transformation<SelectedT, ? extends ObservableCollection<ResultT>> transformation = t.transform(selected);
            to.removeAll(transformation.getTarget());
            transformation.dispose();
            registrations.remove(event.getOldItem()).remove();
          }
        };
        final Registration sourceRegistration = from.addListener(sourceListener);
        for (SourceT s : from) {
          sourceListener.onItemAdded(new CollectionItemEvent<>(null, s, -1, CollectionItemEvent.EventType.ADD));
        }

        return new SimpleTransformation<>(from, to, new Registration() {
          @Override
          protected void doRemove() {
            for (SourceT s : from) {
              registrations.remove(s).remove();
            }
            sourceRegistration.remove();
          }
        });
      }
    };
  }

  public static <ItemT> Transformer<ObservableList<ItemT>, ObservableList<ItemT>> identityList() {
    return new BaseTransformer<ObservableList<ItemT>, ObservableList<ItemT>>() {
      @Override
      public Transformation<ObservableList<ItemT>, ObservableList<ItemT>> transform(ObservableList<ItemT> from) {
        return transform(from, new ObservableArrayList<ItemT>());
      }

      @Override
      public Transformation<ObservableList<ItemT>, ObservableList<ItemT>> transform(final ObservableList<ItemT> from, final ObservableList<ItemT> to) {
        final Registration registration = from.addListener(new CollectionAdapter<ItemT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<? extends ItemT> event) {
            to.add(event.getIndex(), event.getNewItem());
          }

          @Override
          public void onItemSet(CollectionItemEvent<? extends ItemT> event) {
            to.set(event.getIndex(), event.getNewItem());
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<? extends ItemT> event) {
            to.remove(event.getIndex());
          }
        });
        to.addAll(from);

        return new SimpleTransformation<>(from, to, registration);
      }
    };
  }
  public static <ItemT> Transformer<ObservableCollection<ItemT>, ObservableCollection<ItemT>> identityCollection() {
    return new BaseTransformer<ObservableCollection<ItemT>, ObservableCollection<ItemT>>() {
      @Override
      public Transformation<ObservableCollection<ItemT>, ObservableCollection<ItemT>> transform(ObservableCollection<ItemT> from) {
        return transform(from, new ObservableHashSet<ItemT>());
      }

      @Override
      public Transformation<ObservableCollection<ItemT>, ObservableCollection<ItemT>> transform(final ObservableCollection<ItemT> from, final ObservableCollection<ItemT> to) {
        final Registration registration = from.addListener(new CollectionAdapter<ItemT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<? extends ItemT> event) {
            to.add(event.getNewItem());
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<? extends ItemT> event) {
            to.remove(event.getOldItem());
          }
        });
        to.addAll(from);

        return new SimpleTransformation<>(from, to, registration);
      }
    };
  }

  public static <TargetT, SourceT extends TargetT, ItemT extends TargetT>
  Transformer<ObservableList<SourceT>, ObservableList<TargetT>> addFirst(final ItemT item) {
    return new BaseTransformer<ObservableList<SourceT>, ObservableList<TargetT>>() {
      @Override
      public Transformation<ObservableList<SourceT>, ObservableList<TargetT>> transform(ObservableList<SourceT> from) {
        return transform(from, new ObservableArrayList<TargetT>());
      }

      @Override
      public Transformation<ObservableList<SourceT>, ObservableList<TargetT>> transform(final ObservableList<SourceT> from, final ObservableList<TargetT> to) {
        final CollectionListener<SourceT> fromListener = new CollectionAdapter<SourceT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<? extends SourceT> event) {
            int pos = event.getIndex();
            to.add(pos + 1, event.getNewItem());
          }

          @Override
          public void onItemSet(CollectionItemEvent<? extends SourceT> event) {
            int pos = event.getIndex();
            to.set(pos + 1, event.getNewItem());
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<? extends SourceT> event) {
            int pos = event.getIndex();
            to.remove(pos + 1);
          }
        };

        to.add(item);
        to.addAll(from);

        return new SimpleTransformation<>(from, to, from.addListener(fromListener));
      }
    };
  }

  public static <TargetT, SourceT extends TargetT, ItemT extends TargetT>
  Transformer<ObservableList<SourceT>, ObservableList<TargetT>> merge(final ObservableList<ItemT> items) {
    return new BaseTransformer<ObservableList<SourceT>, ObservableList<TargetT>>() {
      int fromSize = 0;
      @Override
      public Transformation<ObservableList<SourceT>, ObservableList<TargetT>> transform(ObservableList<SourceT> from) {
        return transform(from, new ObservableArrayList<TargetT>());
      }

      @Override
      public Transformation<ObservableList<SourceT>, ObservableList<TargetT>> transform(final ObservableList<SourceT> from, final ObservableList<TargetT> to) {
        final CollectionListener<SourceT> fromListener = new CollectionAdapter<SourceT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<? extends SourceT> event) {
            int pos = event.getIndex();
            to.add(pos, event.getNewItem());
            fromSize += 1;
          }

          @Override
          public void onItemSet(CollectionItemEvent<? extends SourceT> event) {
            int pos = event.getIndex();
            to.set(pos, event.getNewItem());
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<? extends SourceT> event) {
            int pos = event.getIndex();
            to.remove(pos);
            fromSize -= 1;
          }
        };

        final CollectionListener<ItemT> itemsListener = new CollectionAdapter<ItemT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<? extends ItemT> event) {
            int pos = event.getIndex();
            to.add(pos + fromSize, event.getNewItem());
          }

          @Override
          public void onItemSet(CollectionItemEvent<? extends ItemT> event) {
            int pos = event.getIndex();
            to.set(pos + fromSize, event.getNewItem());
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<? extends ItemT> event) {
            int pos = event.getIndex();
            to.remove(fromSize + pos);
          }
        };

        to.addAll(from);
        fromSize = from.size();
        to.addAll(items);

        return new SimpleTransformation<>(from, to,
            new CompositeRegistration(from.addListener(fromListener), items.addListener(itemsListener)));
      }
    };
  }

  public static <TargetT, SourceT extends TargetT, ItemT extends TargetT>
  Transformer<ObservableList<SourceT>, ObservableList<TargetT>> addFirstWithCondition(final ItemT item, final ReadableProperty<Boolean> condition) {
    return Transformers.<TargetT, SourceT, ItemT>addFirstWithCondition(jetbrains.jetpad.base.Functions.constantSupplier(item), condition);
  }

  public static <TargetT, SourceT extends TargetT, ItemT extends TargetT>
  Transformer<ObservableList<SourceT>, ObservableList<TargetT>> addFirstWithCondition(final Supplier<ItemT> item, final ReadableProperty<Boolean> condition) {
    final Supplier<ItemT> memoizedItem = jetbrains.jetpad.base.Functions.memorize(item);
    return new BaseTransformer<ObservableList<SourceT>, ObservableList<TargetT>>() {
      @Override
      public Transformation<ObservableList<SourceT>, ObservableList<TargetT>> transform(ObservableList<SourceT> from) {
        return transform(from, new ObservableArrayList<TargetT>());
      }

      @Override
      public Transformation<ObservableList<SourceT>, ObservableList<TargetT>> transform(final ObservableList<SourceT> from, final ObservableList<TargetT> to) {
        final CollectionListener<SourceT> fromListener = new CollectionAdapter<SourceT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<? extends SourceT> event) {
            int pos = event.getIndex();
            if (condition.get()) {
              pos += 1;
            }
            to.add(pos, event.getNewItem());
          }

          @Override
          public void onItemSet(CollectionItemEvent<? extends SourceT> event) {
            int pos = event.getIndex();
            if (condition.get()) {
              pos += 1;
            }
            to.set(pos, event.getNewItem());
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<? extends SourceT> event) {
            int pos = event.getIndex();
            if (condition.get()) {
              pos += 1;
            }
            to.remove(pos);
          }
        };

        final EventHandler<PropertyChangeEvent<Boolean>> conditionHandler = new EventHandler<PropertyChangeEvent<Boolean>>() {
          @Override
          public void onEvent(PropertyChangeEvent<Boolean> event) {
            if (event.getNewValue()) {
              to.add(0, memoizedItem.get());
            } else {
              to.remove(0);
            }
          }
        };

        if (condition.get()) {
          to.add(memoizedItem.get());
        }
        to.addAll(from);

        return new SimpleTransformation<>(from, to,
            new CompositeRegistration(from.addListener(fromListener), condition.addHandler(conditionHandler)));
      }
    };
  }


  public static <TargetT, SourceT extends TargetT, ItemT extends TargetT>
  Transformer<ObservableCollection<SourceT>, ObservableCollection<TargetT>> addWithCondition(final ItemT item, final ReadableProperty<Boolean> condition) {
    return Transformers.<TargetT, SourceT, ItemT>addWithCondition(jetbrains.jetpad.base.Functions.constantSupplier(item), condition);
  }

  public static <TargetT, SourceT extends TargetT, ItemT extends TargetT>
  Transformer<ObservableCollection<SourceT>, ObservableCollection<TargetT>> addWithCondition(final Supplier<ItemT> item, final ReadableProperty<Boolean> condition) {
    final Supplier<ItemT> memoizedItem = jetbrains.jetpad.base.Functions.memorize(item);
    return new BaseTransformer<ObservableCollection<SourceT>, ObservableCollection<TargetT>>() {
      @Override
      public Transformation<ObservableCollection<SourceT>, ObservableCollection<TargetT>> transform(ObservableCollection<SourceT> from) {
        return transform(from, new ObservableHashSet<TargetT>());
      }

      @Override
      public Transformation<ObservableCollection<SourceT>, ObservableCollection<TargetT>> transform(final ObservableCollection<SourceT> from, final ObservableCollection<TargetT> to) {
        final CollectionListener<SourceT> fromListener = new CollectionAdapter<SourceT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<? extends SourceT> event) {
            to.add(event.getNewItem());
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<? extends SourceT> event) {
            to.remove(event.getOldItem());
          }
        };

        final EventHandler<PropertyChangeEvent<Boolean>> conditionHandler = new EventHandler<PropertyChangeEvent<Boolean>>() {
          @Override
          public void onEvent(PropertyChangeEvent<Boolean> event) {
            if (event.getNewValue()) {
              to.add(memoizedItem.get());
            } else {
              to.remove(memoizedItem.get());
            }
          }
        };

        if (condition.get()) {
          to.add(memoizedItem.get());
        }
        to.addAll(from);

        return new SimpleTransformation<>(from, to,
            new CompositeRegistration(from.addListener(fromListener), condition.addHandler(conditionHandler)));
      }
    };
  }

  public static <ItemT>
  Transformer<ObservableList<ItemT>, List<ItemT>> withPlaceHoldersIfEmpty(final Supplier<ItemT> placeholder) {
    return new BaseTransformer<ObservableList<ItemT>, List<ItemT>>() {
      @Override
      public Transformation<ObservableList<ItemT>, List<ItemT>> transform(ObservableList<ItemT> from) {
        return transform(from, new ArrayList<ItemT>());
      }

      @Override
      public Transformation<ObservableList<ItemT>, List<ItemT>> transform(final ObservableList<ItemT> from, final List<ItemT> to) {
        final Registration fromRegistration = from.addListener(new CollectionAdapter<ItemT>() {
          private ItemT myPlaceholder;

          {
            if (from.isEmpty()) {
              to.add(myPlaceholder = placeholder.get());
            }
          }

          @Override
          public void onItemAdded(CollectionItemEvent<? extends ItemT> event) {
            if (myPlaceholder != null) {
              to.remove(myPlaceholder);
              myPlaceholder = null;
            }
            to.add(event.getIndex(), event.getNewItem());
          }

          @Override
          public void onItemSet(CollectionItemEvent<? extends ItemT> event) {
            if (myPlaceholder != null) {
              throw new IllegalStateException();
            }
            to.set(event.getIndex(), event.getNewItem());
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<? extends ItemT> event) {
            to.remove(event.getIndex());

            if (to.isEmpty()) {
              if (myPlaceholder != null) {
                throw new IllegalStateException();
              }
              to.add(myPlaceholder = placeholder.get());
            }
          }
        });

        return new SimpleTransformation<>(from, to, fromRegistration);
      }
    };
  }

  public static <ItemT>
  Transformer<ReadableProperty<ItemT>, ObservableList<ItemT>> propertyToList() {
    return new BaseTransformer<ReadableProperty<ItemT>, ObservableList<ItemT>>() {
      @Override
      public Transformation<ReadableProperty<ItemT>, ObservableList<ItemT>> transform(ReadableProperty<ItemT> from) {
        return transform(from, new ObservableArrayList<ItemT>());
      }

      @Override
      public Transformation<ReadableProperty<ItemT>, ObservableList<ItemT>> transform(final ReadableProperty<ItemT> from, final ObservableList<ItemT> to) {
        if (!to.isEmpty()) {
          throw new IllegalStateException();
        }

        final Runnable sync = new Runnable() {
          @Override
          public void run() {
            if (!to.isEmpty() && from.get() == to.get(0)) return;
            to.clear();
            if (from.get() != null) {
              to.add(from.get());
            }
          }
        };
        sync.run();

        return new SimpleTransformation<>(from, to, from.addHandler(new EventHandler<PropertyChangeEvent<ItemT>>() {
          @Override
          public void onEvent(PropertyChangeEvent<ItemT> event) {
            sync.run();
          }
        }));
      }
    };
  }


  public static <ItemT>
  Transformer<ReadableProperty<ItemT>, ObservableCollection<ItemT>> propertyToCollection() {
    return new BaseTransformer<ReadableProperty<ItemT>, ObservableCollection<ItemT>>() {
      @Override
      public Transformation<ReadableProperty<ItemT>, ObservableCollection<ItemT>> transform(ReadableProperty<ItemT> from) {
        return transform(from, new ObservableHashSet<ItemT>());
      }

      @Override
      public Transformation<ReadableProperty<ItemT>, ObservableCollection<ItemT>> transform(final ReadableProperty<ItemT> from, final ObservableCollection<ItemT> to) {
        if (!to.isEmpty()) {
          throw new IllegalStateException();
        }

        final Runnable sync = new Runnable() {
          @Override
          public void run() {
            if (!to.isEmpty() && from.get() == to.iterator().next()) return;
            to.clear();
            if (from.get() != null) {
              to.add(from.get());
            }
          }
        };
        sync.run();

        return new SimpleTransformation<>(from, to, from.addHandler(new EventHandler<PropertyChangeEvent<ItemT>>() {
          @Override
          public void onEvent(PropertyChangeEvent<ItemT> event) {
            sync.run();
          }
        }));
      }
    };
  }

  public static <TargetT, SourceT>
  Transformer<ObservableCollection<SourceT>, ObservableCollection<TargetT>> select(final Function<SourceT, TargetT> function) {
    return new BaseTransformer<ObservableCollection<SourceT>, ObservableCollection<TargetT>>() {
      @Override
      public Transformation<ObservableCollection<SourceT>, ObservableCollection<TargetT>> transform(ObservableCollection<SourceT> from) {
        return transform(from, new ObservableHashSet<TargetT>());
      }

      @Override
      public Transformation<ObservableCollection<SourceT>, ObservableCollection<TargetT>> transform(final ObservableCollection<SourceT> from, final ObservableCollection<TargetT> to) {
        final CollectionListener<SourceT> fromListener = new CollectionAdapter<SourceT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<? extends SourceT> event) {
            to.add(function.apply(event.getNewItem()));
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<? extends SourceT> event) {
            to.remove(function.apply(event.getOldItem()));
          }
        };

        for (SourceT source: from) {
          to.add(function.apply(source));
        }

        return new SimpleTransformation<>(from, to, from.addListener(fromListener));
      }
    };
  }

  public static <TargetT, SourceT>
  Transformer<ObservableList<SourceT>, ObservableList<TargetT>> selectList(final Function<SourceT, TargetT> function) {
    return new BaseTransformer<ObservableList<SourceT>, ObservableList<TargetT>>() {
      @Override
      public Transformation<ObservableList<SourceT>, ObservableList<TargetT>> transform(ObservableList<SourceT> from) {
        return transform(from, new ObservableArrayList<TargetT>());
      }

      @Override
      public Transformation<ObservableList<SourceT>, ObservableList<TargetT>> transform(final ObservableList<SourceT> from, final ObservableList<TargetT> to) {
        final CollectionListener<SourceT> fromListener = new CollectionAdapter<SourceT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<? extends SourceT> event) {
            to.add(event.getIndex(), function.apply(event.getNewItem()));
          }

          @Override
          public void onItemSet(CollectionItemEvent<? extends SourceT> event) {
            to.set(event.getIndex(), function.apply(event.getNewItem()));
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<? extends SourceT> event) {
            to.remove(event.getIndex());
          }
        };

        for (SourceT source: from) {
          to.add(function.apply(source));
        }

        return new SimpleTransformation<>(from, to, from.addListener(fromListener));
      }
    };
  }

  public static <SourceT> Transformer<ObservableList<SourceT>, ObservableList<SourceT>> reverse() {
    return new BaseTransformer<ObservableList<SourceT>, ObservableList<SourceT>>() {
      @Override
      public Transformation<ObservableList<SourceT>, ObservableList<SourceT>> transform(ObservableList<SourceT> from) {
        return transform(from, new ObservableArrayList<SourceT>());
      }

      @Override
      public Transformation<ObservableList<SourceT>, ObservableList<SourceT>> transform(
          final ObservableList<SourceT> from, final ObservableList<SourceT> to) {
        if (!to.isEmpty()) {
          throw new IllegalStateException("'to' list should be empty: " + to);
        }
        final Registration fromRegistration = from.addListener(new CollectionAdapter<SourceT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<? extends SourceT> event) {
            int index = from.size() - event.getIndex() - 1;
            to.add(index, event.getNewItem());
          }

          @Override
          public void onItemSet(CollectionItemEvent<? extends SourceT> event) {
            int index = to.size() - event.getIndex() - 1;
            to.set(index, event.getNewItem());
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<? extends SourceT> event) {
            int index = to.size() - event.getIndex() - 1;
            to.remove(index);
          }
        });
        for (ListIterator<SourceT> i = from.listIterator(from.size()); i.hasPrevious(); ) {
          to.add(i.previous());
        }
        return new SimpleTransformation<>(from, to, fromRegistration);
      }
    };
  }

  private static class SimpleTransformation<SourceT, TargetT> extends Transformation<SourceT, TargetT> {
    private final SourceT mySource;
    private final TargetT myTarget;
    private final Registration myDisposeRegistration;

    SimpleTransformation(SourceT source, TargetT target, Registration disposeRegistration) {
      mySource = source;
      myTarget = target;
      myDisposeRegistration = disposeRegistration;
    }

    @Override
    public SourceT getSource() {
      return mySource;
    }

    @Override
    public TargetT getTarget() {
      return myTarget;
    }

    @Override
    protected void doDispose() {
      myDisposeRegistration.remove();
    }
  }
}
