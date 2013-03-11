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

import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.Registration;

import java.util.List;

class ObservableCollectionRoleSynchronizer<
    SourceT,
    TargetT> extends BaseCollectionRoleSynchronizer<SourceT, TargetT> {

  private ObservableList<SourceT> mySource;
  private List<? super TargetT> myTarget;
  private Registration myCollectionRegistration;

  ObservableCollectionRoleSynchronizer(
    Mapper<?, ?> mapper,
    ObservableList<SourceT> source,
    List<? super TargetT> target) {
    super(mapper);

    mySource = source;
    myTarget = target;
  }

  protected void onAttach() {
    super.onAttach();

    new MapperUpdater().update(mySource);
    for (Mapper<? extends SourceT, ? extends TargetT> m : getModifiableMappers()) {
      myTarget.add(m.getTarget());
    }

    myCollectionRegistration = mySource.addListener(new CollectionAdapter<SourceT>() {
      @Override
      public void onItemAdded(CollectionItemEvent<SourceT> event) {
        Mapper<? extends SourceT, ? extends TargetT> mapper = createMapper(event.getItem());
        getModifiableMappers().add(event.getIndex(), mapper);
        myTarget.add(event.getIndex(), mapper.getTarget());
        processMapper(mapper);
      }

      @Override
      public void onItemRemoved(CollectionItemEvent<SourceT> event) {
        getModifiableMappers().remove(event.getIndex());
        myTarget.remove(event.getIndex());
      }
    });
  }

  protected void onDetach() {
    super.onDetach();
    myCollectionRegistration.remove();
  }
}
