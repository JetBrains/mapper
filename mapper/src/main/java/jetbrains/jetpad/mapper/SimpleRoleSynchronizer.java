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

import java.util.List;

public class SimpleRoleSynchronizer<SourceT, TargetT> extends BaseCollectionRoleSynchronizer<SourceT, TargetT> {
  private List<SourceT> mySource;
  private List<TargetT> myTarget;

  SimpleRoleSynchronizer(Mapper<?, ?> mapper, List<SourceT> source, List<TargetT> target, MapperFactory<SourceT, TargetT> factory) {
    super(mapper);
    mySource = source;
    myTarget = target;
    addMapperFactory(factory);
  }

  public void refresh() {
    new MapperUpdater() {
      @Override
      protected void mapperAdded(int index, Mapper<? extends SourceT, ? extends TargetT> mapper) {
        myTarget.add(index, mapper.getTarget());
      }

      @Override
      protected void mapperRemoved(int index, Mapper<? extends SourceT, ? extends TargetT> mapper) {
        myTarget.remove(index);
      }
    }.update(mySource);
  }

  @Override
  protected void onAttach() {
    super.onAttach();
    refresh();
  }

  @Override
  protected void onDetach() {
    super.onDetach();
    myTarget.clear();
  }
}