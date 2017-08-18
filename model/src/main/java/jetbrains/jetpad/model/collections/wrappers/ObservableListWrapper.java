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
package jetbrains.jetpad.model.collections.wrappers;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.function.Function;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.collections.RelayCollectionListener;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.EventHandler;

import java.util.AbstractList;

import static jetbrains.jetpad.model.collections.wrappers.Events.wrapEvent;

public class ObservableListWrapper<SourceItemT, TargetItemT> extends AbstractList<TargetItemT> implements ObservableList<TargetItemT> {
  private final ObservableList<SourceItemT> mySource;
  private final Function<SourceItemT, TargetItemT> myStoT;
  private final Function<TargetItemT, SourceItemT> myTtoS;

  public ObservableListWrapper(ObservableList<SourceItemT> source, Function<SourceItemT, TargetItemT> toTarget, Function<TargetItemT, SourceItemT> toSource) {
    mySource = source;
    myStoT = toTarget;
    myTtoS = toSource;
  }

  @Override
  public Registration addListener(final CollectionListener<TargetItemT> l) {
    return mySource.addListener(new RelayCollectionListener<SourceItemT, TargetItemT>(l) {
      @Override
      protected CollectionItemEvent<? extends TargetItemT> transform(CollectionItemEvent<? extends SourceItemT> event) {
        return wrapEvent(event, myStoT);
      }
    });
  }

  @Override
  public Registration addHandler(final EventHandler<? super CollectionItemEvent<? extends TargetItemT>> handler) {
    return mySource.addHandler(new EventHandler<CollectionItemEvent<? extends SourceItemT>>() {
      @Override
      public void onEvent(CollectionItemEvent<? extends SourceItemT> event) {
        handler.onEvent(wrapEvent(event, myStoT));
      }
    });
  }

  @Override
  public void add(int index, TargetItemT element) {
    mySource.add(index, myTtoS.apply(element));
  }

  @Override
  public TargetItemT set(int index, TargetItemT element) {
    SourceItemT old = mySource.set(index, myTtoS.apply(element));
    return old != null ? myStoT.apply(old) : null;
  }

  @Override
  public TargetItemT get(int index) {
    return myStoT.apply(mySource.get(index));
  }

  @Override
  public TargetItemT remove(int index) {
    SourceItemT old = mySource.remove(index);
    return old != null ? myStoT.apply(old) : null;
  }

  @Override
  public int size() {
    return mySource.size();
  }
}