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

import jetbrains.jetpad.base.diff.DifferenceBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class BaseCollectionRoleSynchronizer<SourceT, TargetT> extends BaseRoleSynchronizer<SourceT, TargetT> {
  private MappingContext myMappingContext;
  private List<Mapper<? extends SourceT, ? extends TargetT>> myMappers;

  protected BaseCollectionRoleSynchronizer(Mapper<?, ?> mapper) {
    super();
    myMappers = mapper.createChildList();
  }

  protected final List<Mapper<? extends SourceT, ? extends TargetT>> getModifiableMappers() {
    return myMappers;
  }

  public final List<Mapper<? extends SourceT, ? extends TargetT>> getMappers() {
    return Collections.unmodifiableList(myMappers);
  }

  public final void attach(SynchronizerContext ctx) {
    if (myMappingContext != null) {
      throw new IllegalStateException();
    }

    myMappingContext = ctx.getMappingContext();

    onAttach();
  }

  public final void detach() {
    if (myMappingContext == null) {
      throw new IllegalStateException();
    }

    onDetach();

    myMappingContext = null;
  }

  protected void onAttach() {
  }

  protected void onDetach() {
  }

  protected class MapperUpdater {
    public final void update(List<SourceT> sourceList) {
      List<SourceT> targetContent = new ArrayList<>();
      List<Mapper<? extends SourceT, ? extends TargetT>> mappers = getModifiableMappers();
      for (Mapper<?, ? extends TargetT> m : mappers) {
        targetContent.add((SourceT) m.getSource());
      }

      List<DifferenceBuilder<SourceT>.DifferenceItem> difference = new DifferenceBuilder<>(sourceList, targetContent).build();
      for (DifferenceBuilder<SourceT>.DifferenceItem item : difference) {
        if (item.isAdd) {
          Mapper<? extends SourceT, ? extends TargetT> mapper = createMapper(item.item);
          mappers.add(item.index, mapper);
          mapperAdded(item.index, mapper);
          processMapper(mapper);
        } else {
          Mapper<? extends SourceT, ? extends TargetT> mapper = mappers.remove(item.index);
          mapperRemoved(item.index, mapper);
        }
      }
    }

    protected void mapperAdded(int index, Mapper<? extends SourceT, ? extends TargetT> mapper) {
    }

    protected void mapperRemoved(int index, Mapper<? extends SourceT, ? extends TargetT> mapper) {
    }
  }
}