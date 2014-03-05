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
package jetbrains.jetpad.model.transform;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.collections.ObservableCollection;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ReadableProperty;

import java.util.*;

public class Transformers {
  public static <ItemT> Transformer<ItemT, ItemT> identity() {
    return new BaseTransformer<ItemT, ItemT>() {
      @Override
      public Transformation<ItemT, ItemT> transform(final ItemT from) {
        return new Transformation<ItemT, ItemT>() {
          @Override
          public ItemT getSource() {
            return from;
          }

          @Override
          public ItemT getTarget() {
            return from;
          }

          @Override
          public void dispose() {
          }
        };
      }

      @Override
      public Transformation<ItemT, ItemT> transform(ItemT from, ItemT to) {
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

          @Override
          public void dispose() {
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

        final Registration reg = from.addListener(new CollectionListener<SourceT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<SourceT> event) {
            final Transformation<SourceT, TargetT> transformation = transformer.transform(event.getItem());
            to.add(event.getIndex(), transformation.getTarget());
            itemRegistrations.add(event.getIndex(), new Registration() {
              @Override
              public void remove() {
                transformation.dispose();
              }
            });
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<SourceT> event) {
            to.remove(event.getIndex());
            itemRegistrations.remove(event.getIndex()).remove();
          }
        });

        return new Transformation<ObservableList<SourceT>, ObservableList<TargetT>>() {
          @Override
          public ObservableList<SourceT> getSource() {
            return from;
          }

          @Override
          public ObservableList<TargetT> getTarget() {
            return to;
          }

          @Override
          public void dispose() {
            for (Registration r : itemRegistrations) {
              r.remove();
            }
            reg.remove();
          }
        };
      }
    };
  }
  public static <SourceT, TargetT>
  Transformer<ObservableList<SourceT>, ObservableList<TargetT>> listMap(final Function<SourceT, TargetT> f) {
    return listMap(fromFun(f));
  }

  public static <SpecItemT, ItemT extends SpecItemT, ValueT extends Comparable<ValueT>>
  Transformer<ObservableCollection<ItemT>, ObservableList<ItemT>> sortBy(final Function<SpecItemT, ReadableProperty<ValueT>> propSpec) {
    return sortBy(propSpec, Order.ASCENDING);
  }

  public static <SpecItemT, ItemT extends SpecItemT, ValueT extends Comparable<ValueT>>
  Transformer<ObservableCollection<ItemT>, ObservableList<ItemT>> sortBy(final Function<SpecItemT, ReadableProperty<ValueT>> propSpec, final Order order) {
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

  public static <SpecItemT, ItemT extends SpecItemT, ValueT>
  Transformer<ObservableCollection<ItemT>, ObservableList<ItemT>> sortBy(final Function<SpecItemT, ReadableProperty<ValueT>> propSpec, final Comparator<ValueT> cmp) {
    final Comparator<ItemT> comparator = new Comparator<ItemT>() {
      @Override
      public int compare(ItemT i1, ItemT i2) {
        ReadableProperty<ValueT> p1 = propSpec.apply(i1);
        ReadableProperty<ValueT> p2 = propSpec.apply(i2);

        if (p1 == null || p2 == null) throw new NullPointerException();

        ValueT v1 = p1.get();
        ValueT v2 = p2.get();

        if (v1 == null || v2 == null) {
          if (v1 == v2) return 0;
          if (v1 == null) {
            return -1;
          } else {
            return 1;
          }
        }

        return cmp.compare(v1, v2);
      }
    };

    return new BaseTransformer<ObservableCollection<ItemT>, ObservableList<ItemT>>() {
      private Map<ItemT, Registration> myListeners = new HashMap<>();
      private Registration myCollectionRegistration;
      private CollectionListener<ItemT> myCollectionListener;

      private void watch(final ItemT item, final ObservableList<ItemT> to) {
        ReadableProperty<ValueT> property = propSpec.apply(item);
        if (property == null) throw new NullPointerException();
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
              myCollectionListener.onItemRemoved(new CollectionItemEvent<>(item, -1, false));
              myCollectionListener.onItemAdded(new CollectionItemEvent<>(item, -1, true));
            }
          }
        }));
      }

      private void unwatch(ItemT item) {
        myListeners.remove(item).remove();
      }

      @Override
      public Transformation<ObservableCollection<ItemT>, ObservableList<ItemT>> transform(ObservableCollection<ItemT> from) {
        return transform(from, new ObservableArrayList<ItemT>());
      }

      @Override
      public Transformation<ObservableCollection<ItemT>, ObservableList<ItemT>> transform(final ObservableCollection<ItemT> from, final ObservableList<ItemT> to) {
        myCollectionListener = new CollectionAdapter<ItemT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<ItemT> event) {
            ItemT item = event.getItem();
            watch(item, to);

            int pos = Collections.binarySearch(to, item, comparator);
            int insertIndex = pos >= 0 ? pos + 1 : -(pos + 1);
            to.add(insertIndex, item);
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<ItemT> event) {
            ItemT item = event.getItem();

            int sortedIndex = to.indexOf(item);
            if (sortedIndex == -1) {
              throw new IllegalStateException();
            }

            to.remove(sortedIndex);
            unwatch(item);
          }
        };

        for (ItemT item : from) {
          watch(item, to);
          to.add(item);
        }
        Collections.sort(to, comparator);

        myCollectionRegistration = from.addListener(myCollectionListener);

        return new Transformation<ObservableCollection<ItemT>, ObservableList<ItemT>>() {
          @Override
          public ObservableCollection<ItemT> getSource() {
            return from;
          }

          @Override
          public ObservableList<ItemT> getTarget() {
            return to;
          }

          @Override
          public void dispose() {
            for (ItemT item : from) {
              unwatch(item);
            }
            myCollectionRegistration.remove();
          }
        };
      }
    };
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
        if (!fromItr.hasNext() && !foundItem) throw new IllegalStateException();
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
        if (filter(item)) {
          to.add(item);
        }
      }
      @Override
      protected ObservableCollection<ItemT> createTo() {
        return new ObservableHashSet<>();
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
              public void onItemAdded(CollectionItemEvent<SourceT> event) {
                add(event.getItem());
              }

              @Override
              public void onItemRemoved(CollectionItemEvent<SourceT> event) {
                SourceT item = event.getItem();
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
          public void dispose() {
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
          public void onItemAdded(CollectionItemEvent<ItemT> event) {
            int n = value.get();
            if (event.getIndex() >= n) return;
            to.add(event.getIndex(), event.getItem());
            if (to.size() == n + 1) {
              to.remove(n);
            }
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<ItemT> event) {
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

        return new Transformation<ObservableList<ItemT>, ObservableList<ItemT>>() {
          @Override
          public ObservableList<ItemT> getSource() {
            return from;
          }

          @Override
          public ObservableList<ItemT> getTarget() {
            return to;
          }

          @Override
          public void dispose() {
            fromReg.remove();
            propReg.remove();
          }
        };
      }
    };
  }

  public static <SourceT, TargetT>
  Transformer<ObservableList<SourceT>, ObservableList<TargetT>> flattenList(final Function<SourceT, ObservableList<TargetT>> f) {
    return Transformers.<SourceT, ObservableList<TargetT>, TargetT>flattenList(f, Transformers.<ObservableList<TargetT>>identity());
  }

  public static <SourceT, SelectedT, ResultT>
  Transformer<ObservableList<SourceT>, ObservableList<ResultT>> flattenList(final Function<SourceT, SelectedT> f, final Transformer<SelectedT, ? extends ObservableList<ResultT>> t) {
    return new BaseTransformer<ObservableList<SourceT>, ObservableList<ResultT>>() {
      @Override
      public Transformation<ObservableList<SourceT>, ObservableList<ResultT>> transform(ObservableList<SourceT> from) {
        return transform(from, new ObservableArrayList<ResultT>());
      }

      @Override
      public Transformation<ObservableList<SourceT>, ObservableList<ResultT>> transform(final ObservableList<SourceT> from, final ObservableList<ResultT> to) {
        final Map<SourceT, Registration> registrations = new HashMap<>();
        final Map<SourceT, Integer> sizes = new HashMap<>();

        CollectionAdapter<SourceT> sourceListener = new CollectionAdapter<SourceT>() {
          @Override
          public void onItemAdded(final CollectionItemEvent<SourceT> event) {
            SelectedT selected = f.apply(event.getItem());
            final Transformation<SelectedT, ? extends ObservableList<ResultT>> transform = t.transform(selected);
            ObservableList<ResultT> target = transform.getTarget();

            int startIndex = getStartResultIndex(event.getItem(), from, sizes);
            sizes.put(event.getItem(), target.size());
            for (ResultT r: target) {
              to.add(startIndex++, r);
            }

            final Registration reg = target.addListener(new CollectionListener<ResultT>() {
              @Override
              public void onItemAdded(CollectionItemEvent<ResultT> nestedEvent) {
                int startIndex = getStartResultIndex(event.getItem(), from, sizes);
                to.add(startIndex + nestedEvent.getIndex(), nestedEvent.getItem());
                sizes.put(event.getItem(), sizes.get(event.getItem()) + 1);
              }

              @Override
              public void onItemRemoved(CollectionItemEvent<ResultT> nestedEvent) {
                to.remove(nestedEvent.getItem());
                sizes.put(event.getItem(), sizes.get(event.getItem()) - 1);
              }
            });

            registrations.put(event.getItem(), new Registration() {
              @Override
              public void remove() {
                reg.remove();
                transform.dispose();
              }
            });
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<SourceT> event) {
            SelectedT selected = f.apply(event.getItem());
            Transformation<SelectedT, ? extends ObservableCollection<ResultT>> transformation = t.transform(selected);

            to.removeAll(transformation.getTarget());
            sizes.remove(event.getItem());

            transformation.dispose();
            registrations.remove(event.getItem()).remove();
          }
        };

        final Registration sourceRegistration = from.addListener(sourceListener);
        int index = 0;
        for (SourceT s : from) {
          sourceListener.onItemAdded(new CollectionItemEvent<>(s, index++, true));
        }

        return new Transformation<ObservableList<SourceT>, ObservableList<ResultT>>() {
          @Override
          public ObservableList<SourceT> getSource() {
            return from;
          }

          @Override
          public ObservableList<ResultT> getTarget() {
            return to;
          }

          @Override
          public void dispose() {
            for (SourceT s : from) {
              registrations.remove(s).remove();
            }
            sourceRegistration.remove();
          }
        };
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

  public static <ValueT>
  Transformer<ObservableList<Property<ValueT>>, ObservableList<ValueT>> flattenPropertyList() {
    return new BaseTransformer<ObservableList<Property<ValueT>>, ObservableList<ValueT>>() {
      @Override
      public Transformation<ObservableList<Property<ValueT>>, ObservableList<ValueT>> transform(ObservableList<Property<ValueT>> from) {
        return transform(from, new ObservableArrayList<ValueT>());
      }

      @Override
      public Transformation<ObservableList<Property<ValueT>>, ObservableList<ValueT>> transform(final ObservableList<Property<ValueT>> from, final ObservableList<ValueT> to) {
        final List<Registration> propRegistrations = new ArrayList<>();
        CollectionAdapter<Property<ValueT>> listener = new CollectionAdapter<Property<ValueT>>() {
          @Override
          public void onItemAdded(final CollectionItemEvent<Property<ValueT>> listEvent) {
            propRegistrations.add(listEvent.getIndex(), listEvent.getItem().addHandler(new EventHandler<PropertyChangeEvent<ValueT>>() {
              @Override
              public void onEvent(PropertyChangeEvent<ValueT> propEvent) {
                int index = from.indexOf(listEvent.getItem());
                to.set(index, propEvent.getNewValue());
              }
            }));
            to.add(listEvent.getIndex(), listEvent.getItem().get());
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<Property<ValueT>> listEvent) {
            propRegistrations.remove(listEvent.getIndex()).remove();
            to.remove(listEvent.getIndex());
          }
        };

        for (int i = 0; i < from.size(); i++) {
          listener.onItemAdded(new CollectionItemEvent<>(from.get(i), i, true));
        }

        final Registration reg = from.addListener(listener);
        return new Transformation<ObservableList<Property<ValueT>>, ObservableList<ValueT>>() {
          @Override
          public ObservableList<Property<ValueT>> getSource() {
            return from;
          }

          @Override
          public ObservableList<ValueT> getTarget() {
            return to;
          }

          @Override
          public void dispose() {
            reg.remove();
            for (Registration r : propRegistrations) {
              r.remove();
            }
            propRegistrations.clear();
            to.clear();
          }
        };
      }
    };
  }

  public static <SourceT, TargetT>
  Transformer<ObservableCollection<SourceT>, ObservableCollection<TargetT>> flatten(final Function<SourceT, ObservableCollection<TargetT>> f) {
    return Transformers.<SourceT, ObservableCollection<TargetT>, TargetT>flatten(f, Transformers.<ObservableCollection<TargetT>>identity());
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
          public void onItemAdded(CollectionItemEvent<ResultT> event) {
            to.add(event.getItem());
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<ResultT> event) {
            to.remove(event.getItem());
          }
        };

        final Map<SourceT, Registration> registrations = new HashMap<>();
        CollectionAdapter<SourceT> sourceListener = new CollectionAdapter<SourceT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<SourceT> event) {
            SelectedT subcollection = f.apply(event.getItem());
            final Transformation<SelectedT, ? extends ObservableCollection<ResultT>> transform = t.transform(subcollection);
            ObservableCollection<ResultT> target = transform.getTarget();
            to.addAll(target);
            final Registration reg = target.addListener(nestedListener);
            registrations.put(event.getItem(), new Registration() {
              @Override
              public void remove() {
                reg.remove();
                transform.dispose();
              }
            });
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<SourceT> event) {
            SelectedT selected = f.apply(event.getItem());
            Transformation<SelectedT, ? extends ObservableCollection<ResultT>> transformation = t.transform(selected);
            to.removeAll(transformation.getTarget());
            transformation.dispose();
            registrations.remove(event.getItem()).remove();
          }
        };
        final Registration sourceRegistration = from.addListener(sourceListener);
        for (SourceT s : from) {
          sourceListener.onItemAdded(new CollectionItemEvent<>(s, -1, true));
        }

        return new Transformation<ObservableCollection<SourceT>, ObservableCollection<ResultT>>() {
          @Override
          public ObservableCollection<SourceT> getSource() {
            return from;
          }

          @Override
          public ObservableCollection<ResultT> getTarget() {
            return to;
          }

          @Override
          public void dispose() {
            for (SourceT s : from) {
              registrations.remove(s).remove();
            }
            sourceRegistration.remove();
          }
        };
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
          public void onItemAdded(CollectionItemEvent<ItemT> event) {
            to.add(event.getIndex(), event.getItem());
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<ItemT> event) {
            to.remove(event.getIndex());
          }
        });
        to.addAll(from);

        return new Transformation<ObservableList<ItemT>, ObservableList<ItemT>>() {
          @Override
          public ObservableList<ItemT> getSource() {
            return from;
          }

          @Override
          public ObservableList<ItemT> getTarget() {
            return to;
          }

          @Override
          public void dispose() {
            registration.remove();
          }
        };
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
          public void onItemAdded(CollectionItemEvent<ItemT> event) {
            to.add(event.getItem());
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<ItemT> event) {
            to.remove(event.getItem());
          }
        });
        to.addAll(from);

        return new Transformation<ObservableCollection<ItemT>, ObservableCollection<ItemT>>() {
          @Override
          public ObservableCollection<ItemT> getSource() {
            return from;
          }

          @Override
          public ObservableCollection<ItemT> getTarget() {
            return to;
          }

          @Override
          public void dispose() {
            registration.remove();
          }
        };
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
          public void onItemAdded(CollectionItemEvent<SourceT> event) {
            int pos = event.getIndex();
            to.add(pos + 1, event.getItem());
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<SourceT> event) {
            int pos = event.getIndex();
            to.remove(pos + 1);
          }
        };

        to.add(item);
        to.addAll(from);

        final Registration fromRegistration = from.addListener(fromListener);

        return new Transformation<ObservableList<SourceT>, ObservableList<TargetT>>() {
          @Override
          public ObservableList<SourceT> getSource() {
            return from;
          }

          @Override
          public ObservableList<TargetT> getTarget() {
            return to;
          }

          @Override
          public void dispose() {
            fromRegistration.remove();
          }
        };
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
          public void onItemAdded(CollectionItemEvent<SourceT> event) {
            int pos = event.getIndex();
            to.add(pos, event.getItem());
            fromSize += 1;
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<SourceT> event) {
            int pos = event.getIndex();
            to.remove(pos);
            fromSize -= 1;
          }
        };

        final CollectionListener<ItemT> itemsListener = new CollectionAdapter<ItemT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<ItemT> event) {
            int pos = event.getIndex();
            to.add(pos + fromSize, event.getItem());
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<ItemT> event) {
            int pos = event.getIndex();
            to.remove(fromSize + pos);
          }
        };

        to.addAll(from);
        fromSize = from.size();
        to.addAll(items);

        final Registration fromRegistration = from.addListener(fromListener);
        final Registration itemsRegistration = items.addListener(itemsListener);

        return new Transformation<ObservableList<SourceT>, ObservableList<TargetT>>() {
          @Override
          public ObservableList<SourceT> getSource() {
            return from;
          }

          @Override
          public ObservableList<TargetT> getTarget() {
            return to;
          }

          @Override
          public void dispose() {
            fromRegistration.remove();
            itemsRegistration.remove();
          }
        };
      }
    };
  }

  public static <TargetT, SourceT extends TargetT, ItemT extends TargetT>
  Transformer<ObservableList<SourceT>, ObservableList<TargetT>> addFirstWithCondition(final Supplier<ItemT> item, final ReadableProperty<Boolean> condition) {
    final Supplier<ItemT> memoizedItem = Suppliers.memoize(item);
    return new BaseTransformer<ObservableList<SourceT>, ObservableList<TargetT>>() {
      @Override
      public Transformation<ObservableList<SourceT>, ObservableList<TargetT>> transform(ObservableList<SourceT> from) {
        return transform(from, new ObservableArrayList<TargetT>());
      }

      @Override
      public Transformation<ObservableList<SourceT>, ObservableList<TargetT>> transform(final ObservableList<SourceT> from, final ObservableList<TargetT> to) {
        final CollectionListener<SourceT> fromListener = new CollectionAdapter<SourceT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<SourceT> event) {
            int pos = event.getIndex();
            if (condition.get()) {
              pos += 1;
            }
            to.add(pos, event.getItem());
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<SourceT> event) {
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

        final Registration fromRegistration = from.addListener(fromListener);
        final Registration conditionRegistration = condition.addHandler(conditionHandler);

        return new Transformation<ObservableList<SourceT>, ObservableList<TargetT>>() {
          @Override
          public ObservableList<SourceT> getSource() {
            return from;
          }

          @Override
          public ObservableList<TargetT> getTarget() {
            return to;
          }

          @Override
          public void dispose() {
            fromRegistration.remove();
            conditionRegistration.remove();
          }
        };
      }
    };
  }


  public static <TargetT, SourceT extends TargetT, ItemT extends TargetT>
  Transformer<ObservableCollection<SourceT>, ObservableCollection<TargetT>> addWithCondition(final ItemT item, final ReadableProperty<Boolean> condition) {
    return Transformers.<TargetT, SourceT, ItemT>addWithCondition(Suppliers.ofInstance(item), condition);
  }

  public static <TargetT, SourceT extends TargetT, ItemT extends TargetT>
  Transformer<ObservableCollection<SourceT>, ObservableCollection<TargetT>> addWithCondition(final Supplier<ItemT> item, final ReadableProperty<Boolean> condition) {
    final Supplier<ItemT> memoizedItem = Suppliers.memoize(item);
    return new BaseTransformer<ObservableCollection<SourceT>, ObservableCollection<TargetT>>() {
      @Override
      public Transformation<ObservableCollection<SourceT>, ObservableCollection<TargetT>> transform(ObservableCollection<SourceT> from) {
        return transform(from, new ObservableHashSet<TargetT>());
      }

      @Override
      public Transformation<ObservableCollection<SourceT>, ObservableCollection<TargetT>> transform(final ObservableCollection<SourceT> from, final ObservableCollection<TargetT> to) {
        final CollectionListener<SourceT> fromListener = new CollectionAdapter<SourceT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<SourceT> event) {
            to.add(event.getItem());
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<SourceT> event) {
            to.remove(event.getItem());
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

        final Registration fromRegistration = from.addListener(fromListener);
        final Registration conditionRegistration = condition.addHandler(conditionHandler);

        return new Transformation<ObservableCollection<SourceT>, ObservableCollection<TargetT>>() {
          @Override
          public ObservableCollection<SourceT> getSource() {
            return from;
          }

          @Override
          public ObservableCollection<TargetT> getTarget() {
            return to;
          }

          @Override
          public void dispose() {
            fromRegistration.remove();
            conditionRegistration.remove();
          }
        };
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
          public void onItemAdded(CollectionItemEvent<ItemT> event) {
            if (myPlaceholder != null) {
              to.remove(myPlaceholder);
              myPlaceholder = null;
            }
            to.add(event.getIndex(), event.getItem());
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<ItemT> event) {
            to.remove(event.getIndex());

            if (to.isEmpty()) {
              if (myPlaceholder != null) {
                throw new IllegalStateException();
              }
              to.add(myPlaceholder = placeholder.get());
            }
          }
        });

        return new Transformation<ObservableList<ItemT>, List<ItemT>>() {
          @Override
          public ObservableList<ItemT> getSource() {
            return from;
          }

          @Override
          public List<ItemT> getTarget() {
            return to;
          }

          @Override
          public void dispose() {
            fromRegistration.remove();
          }
        };
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
        if (!to.isEmpty()) throw new IllegalStateException();

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

        final Registration r = from.addHandler(new EventHandler<PropertyChangeEvent<ItemT>>() {
          @Override
          public void onEvent(PropertyChangeEvent<ItemT> event) {
            sync.run();
          }
        });

        return new Transformation<ReadableProperty<ItemT>, ObservableList<ItemT>>() {
          @Override
          public ReadableProperty<ItemT> getSource() {
            return from;
          }

          @Override
          public ObservableList<ItemT> getTarget() {
            return to;
          }

          @Override
          public void dispose() {
            r.remove();
          }
        };
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
        if (!to.isEmpty()) throw new IllegalStateException();

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

        final Registration r = from.addHandler(new EventHandler<PropertyChangeEvent<ItemT>>() {
          @Override
          public void onEvent(PropertyChangeEvent<ItemT> event) {
            sync.run();
          }
        });

        return new Transformation<ReadableProperty<ItemT>, ObservableCollection<ItemT>>() {
          @Override
          public ReadableProperty<ItemT> getSource() {
            return from;
          }

          @Override
          public ObservableCollection<ItemT> getTarget() {
            return to;
          }

          @Override
          public void dispose() {
            r.remove();
          }
        };
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
          public void onItemAdded(CollectionItemEvent<SourceT> event) {
            to.add(function.apply(event.getItem()));
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<SourceT> event) {
            to.remove(function.apply(event.getItem()));
          }
        };

        for (SourceT source: from) {
          to.add(function.apply(source));
        }
        final Registration fromRegistration = from.addListener(fromListener);

        return new Transformation<ObservableCollection<SourceT>, ObservableCollection<TargetT>>() {
          @Override
          public ObservableCollection<SourceT> getSource() {
            return from;
          }

          @Override
          public ObservableCollection<TargetT> getTarget() {
            return to;
          }

          @Override
          public void dispose() {
            fromRegistration.remove();
          }
        };
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
          public void onItemAdded(CollectionItemEvent<SourceT> event) {
            to.add(event.getIndex(), function.apply(event.getItem()));
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<SourceT> event) {
            to.remove(event.getIndex());
          }
        };

        for (SourceT source: from) {
          to.add(function.apply(source));
        }
        final Registration fromRegistration = from.addListener(fromListener);

        return new Transformation<ObservableList<SourceT>, ObservableList<TargetT>>() {
          @Override
          public ObservableList<SourceT> getSource() {
            return from;
          }

          @Override
          public ObservableList<TargetT> getTarget() {
            return to;
          }

          @Override
          public void dispose() {
            fromRegistration.remove();
          }
        };
      }
    };
  }
}