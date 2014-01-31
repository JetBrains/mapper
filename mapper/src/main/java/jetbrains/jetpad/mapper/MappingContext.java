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
package jetbrains.jetpad.mapper;

import java.util.*;

public final class MappingContext {
  private Map<Object, Object> myMappers = new HashMap<Object, Object>();
  private Map<Mapper<?, ?>, Runnable> myOnDispose = new HashMap<Mapper<?, ?>, Runnable>();

  public MappingContext() {
  }

  protected void register(Mapper<?, ?> mapper) {
    Object source = mapper.getSource();
    if (!(myMappers.containsKey(source))) {
      myMappers.put(source, mapper);
    } else {
      Object ms = myMappers.get(source);
      if (ms instanceof Set) {
        Set<Mapper<?, ?>> mappers = (Set<Mapper<?, ?>>) ms;
        mappers.add(mapper);
      } else {
        Mapper<?, ?> m = (Mapper<?, ?>) ms;
        Set<Mapper<?, ?>> mappers = new HashSet<Mapper<?, ?>>();
        mappers.add(m);
        mappers.add(mapper);
        myMappers.put(source, mappers);
      }
    }
  }

  protected void unregister(Mapper<?, ?> mapper) {
    Object source = mapper.getSource();
    if (!myMappers.containsKey(source)) {
      throw new IllegalStateException();
    }
    Object ms = myMappers.get(source);
    if (ms instanceof Set) {
      Set<Mapper<?, ?>> mappers = (Set<Mapper<?, ?>>) ms;
      mappers.remove(mapper);
      if (mappers.size() == 1) {
        myMappers.put(source, mappers.iterator().next());
      }
    } else {
      if (ms != mapper) throw new IllegalStateException();
      myMappers.remove(source);
    }

    Runnable onDispose = myOnDispose.remove(mapper);
    if (onDispose != null) {
      onDispose.run();
    }
  }

  /**
   * Try using this method as little as possible
   */
  public <S> Mapper<? super S, ?> getMapper(Mapper<?, ?> ancestor, S source) {
    Set<Mapper<? super S, ?>> result = getMappers(ancestor, source);
    if (result.isEmpty()) return null;
    if (result.size() > 1) throw new IllegalStateException();
    return result.iterator().next();
  }

  /**
   * Try using this method as little as possible
   */
  public <S> Set<Mapper<? super S, ?>> getMappers(Mapper<?, ?> ancestor, S source) {
    Set<Mapper<? super S, ?>> result = new HashSet<Mapper<? super S, ?>>();
    for (Mapper<? super S, ?> m : getMappers(source)) {
      if (Mappers.isDescendant(ancestor, m)) {
        result.add(m);
      }
    }
    return result;
  }

  /**
   * This method is for tests only. Usage of this method in mappers will lead to code which
   * arbitrarily selects a mapper. Doing so if we have several mappers of the same kind for
   * the same node, will lead to non determinism and hinder mapper reusability.
   */
  <S> Set<Mapper<? super S, ?>> getMappers(S source) {
    if (!(myMappers.containsKey(source))) {
      return Collections.emptySet();
    }
    Object mappers = myMappers.get(source);
    Set<Mapper<? super S, ?>> result = new HashSet<Mapper<? super S, ?>>();
    if (mappers instanceof Mapper) {
      result.add((Mapper<? super S, ?>) mappers);
    } else {
      for (Mapper<?, ?> m : (Set<Mapper<? super S, ?>>) mappers) {
        result.add((Mapper<? super S, ?>) m);
      }
    }
    return result;
  }
}