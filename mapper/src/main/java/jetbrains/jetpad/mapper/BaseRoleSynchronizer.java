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

abstract class BaseRoleSynchronizer<SourceT, TargetT> implements RoleSynchronizer<SourceT, TargetT> {
  private MapperFactory[] myMapperFactories = MapperFactory.EMPTY_ARRAY;
  private MapperProcessor[] myMapperProcessors = MapperProcessor.EMPTY_ARRAY;

  protected BaseRoleSynchronizer() {
  }

  @Override
  public final void addMapperFactory(MapperFactory<SourceT, TargetT> factory) {
    MapperFactory[] newMapperFactories = new MapperFactory[myMapperFactories.length + 1];
    System.arraycopy(myMapperFactories, 0, newMapperFactories, 0, myMapperFactories.length);
    newMapperFactories[newMapperFactories.length - 1] = factory;
    myMapperFactories = newMapperFactories;
  }

  public final void addMapperProcessor(MapperProcessor<SourceT, TargetT> processor) {
    MapperProcessor[] newMapperProcessors = new MapperProcessor[myMapperProcessors.length + 1];
    System.arraycopy(myMapperProcessors, 0, newMapperProcessors, 0, myMapperProcessors.length);
    newMapperProcessors[newMapperProcessors.length - 1] = processor;
    myMapperProcessors = newMapperProcessors;
  }

  protected final Mapper<? extends SourceT, ? extends TargetT> createMapper(SourceT source) {
    Mapper<? extends SourceT, ? extends TargetT> result;
    for (MapperFactory<SourceT, TargetT> f : myMapperFactories) {
      result = f.createMapper(source);
      if (result != null) return result;
    }

    throw new IllegalStateException("Can't create a mapper for " + source);
  }

  protected final void processMapper(Mapper<? extends SourceT, ? extends TargetT> mapper) {
    for (MapperProcessor<SourceT, TargetT> p : myMapperProcessors) {
      p.process(mapper);
    }
  }
}