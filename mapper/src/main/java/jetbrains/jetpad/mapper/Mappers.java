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

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.collections.list.ObservableList;

public class Mappers {
  public static boolean isDescendant(Mapper<?, ?> ancestor, Mapper<?, ?> descendant) {
    if (descendant == ancestor) return true;
    Mapper<?, ?> parent = descendant.getParent();
    if (parent == null) return false;
    return isDescendant(ancestor, parent);
  }

  public static Mapper<?, ?> getRoot(Mapper<?, ?> mapper) {
    Mapper<?, ?> parent = mapper.getParent();
    if (parent == null) return mapper;
    return getRoot(parent);
  }

  public static Registration attachRoot(final Mapper<?, ?> mapper) {
    mapper.attachRoot();
    return new Registration() {
      @Override
      protected void doDispose() {
        mapper.detachRoot();
      }
    };
  }

  public static <SourceT, Target1T, Target2T> MapperFactory<SourceT, Target2T> compose(final MapperFactory<SourceT, Target1T> f1, final MapperFactory<Target1T, Target2T> f2) {
    return new MapperFactory<SourceT, Target2T>() {
      @Override
      public Mapper<? extends SourceT, ? extends Target2T> createMapper(SourceT source) {
        final Mapper<? extends SourceT, ? extends Target1T> m1 = f1.createMapper(source);
        final Mapper<? extends Target1T, ? extends Target2T> m2 = f2.createMapper(m1.getTarget());
        return new Mapper<SourceT, Target2T>(m1.getSource(), m2.getTarget()) {
          ObservableList<Mapper<?, ?>> children = createChildList();
          {
            children.add(m1);
            children.add(m2);
          }
        };
      }
    };
  }
}