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
package jetbrains.jetpad.model.collections.set;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.collections.ObservableCollection;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.function.Function;

public class MirrorObservableSet<SourceT, TargetT> extends AbstractSet<TargetT> implements ObservableSet<TargetT> {
  private ObservableCollection<SourceT> myBaseCollection;
  private Function<SourceT, TargetT> myTargetSupplier;
  private Function<TargetT, SourceT> mySourceSupplier;
  private Listeners<CollectionListener<TargetT>> myListeners = new Listeners<>();

  public MirrorObservableSet(ObservableCollection<SourceT> baseSet, Function<SourceT, TargetT> targetSupplier, Function<TargetT, SourceT> sourceSupplier) {
    myBaseCollection = baseSet;
    myTargetSupplier = targetSupplier;
    mySourceSupplier = sourceSupplier;
    myBaseCollection.addListener(new CollectionAdapter<SourceT>() {
      @Override
      public void onItemAdded(final CollectionItemEvent<? extends SourceT> event) {
        myListeners.fire(new ListenerCaller<CollectionListener<TargetT>>() {
          @Override
          public void call(CollectionListener<TargetT> l) {
            l.onItemAdded(new CollectionItemEvent<>(null, myTargetSupplier.apply(event.getNewItem()), -1, CollectionItemEvent.EventType.ADD));
          }
        });
      }

      @Override
      public void onItemRemoved(final CollectionItemEvent<? extends SourceT> event) {
        myListeners.fire(new ListenerCaller<CollectionListener<TargetT>>() {
          @Override
          public void call(CollectionListener<TargetT> l) {
            l.onItemRemoved(new CollectionItemEvent<>(myTargetSupplier.apply(event.getOldItem()), null, -1, CollectionItemEvent.EventType.REMOVE));
          }
        });
      }
    });
  }

  @Override
  public Iterator<TargetT> iterator() {
    final Iterator<SourceT> baseIterator = myBaseCollection.iterator();
    return new Iterator<TargetT>() {
      @Override
      public boolean hasNext() {
        return baseIterator.hasNext();
      }

      @Override
      public TargetT next() {
        return myTargetSupplier.apply(baseIterator.next());
      }

      @Override
      public void remove() {
        baseIterator.remove();
      }
    };
  }

  @Override
  public boolean add(TargetT targetT) {
    return myBaseCollection.add(mySourceSupplier.apply(targetT));
  }

  @Override
  public int size() {
    return myBaseCollection.size();
  }

  @Override
  public Registration addListener(CollectionListener<TargetT> l) {
    return myListeners.add(l);
  }

  @Override
  public Registration addHandler(EventHandler<? super CollectionItemEvent<? extends TargetT>> collectionItemEventEventHandler) {
    throw new UnsupportedOperationException();
  }
}
