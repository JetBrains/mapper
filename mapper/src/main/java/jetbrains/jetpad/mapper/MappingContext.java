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
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;

import java.util.*;

public final class MappingContext {
  private Map<Object, Object> myMappers = new HashMap<>();
  private Map<Mapper<?, ?>, Runnable> myOnDispose = new HashMap<>();
  private Listeners<MappingContextListener> myListeners = new Listeners<>();

  public MappingContext() {
  }

  public Registration addListener(MappingContextListener l) {
    return myListeners.add(l);
  }

  protected void register(final Mapper<?, ?> mapper) {
    if (mapper.isFindable()) {
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
          Set<Mapper<?, ?>> mappers = new HashSet<>();
          mappers.add(m);
          mappers.add(mapper);
          myMappers.put(source, mappers);
        }
      }
    }

    myListeners.fire(new ListenerCaller<MappingContextListener>() {
      @Override
      public void call(MappingContextListener l) {
        l.onMapperRegistered(mapper);
      }
    });
  }

  protected void unregister(final Mapper<?, ?> mapper) {
    if (!mapper.isFindable()) return;

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
      if (ms != mapper) {
        throw new IllegalStateException();
      }
      myMappers.remove(source);
    }

    Runnable onDispose = myOnDispose.remove(mapper);
    if (onDispose != null) {
      onDispose.run();
    }

    myListeners.fire(new ListenerCaller<MappingContextListener>() {
      @Override
      public void call(MappingContextListener l) {
        l.onMapperUnregistered(mapper);
      }
    });
  }

  /**
   * Try using this method as little as possible
   */
  public <S> Mapper<? super S, ?> getMapper(Mapper<?, ?> ancestor, S source) {
    Set<Mapper<? super S, ?>> result = getMappers(ancestor, source);
    if (result.isEmpty()) return null;
    if (result.size() > 1) {
      throw new IllegalStateException("There are more than one mapper for " + source);
    }
    return result.iterator().next();
  }

  /**
   * Try using this method as little as possible. Nice to use method which returns one mapper instead
   */
  public <S> Set<Mapper<? super S, ?>> getMappers(Mapper<?, ?> ancestor, S source) {
    Set<Mapper<? super S, ?>> mappers = getMappers(source);
    Set<Mapper<? super S, ?>> result = null;
    for (Mapper<? super S, ?> m : mappers) {
      if (Mappers.isDescendant(ancestor, m)) {
        if (result == null) {
          if (mappers.size() == 1) {
            return Collections.<Mapper<? super S, ?>>singleton(m);
          } else {
            result = new HashSet<>();
          }
        }
        result.add(m);
      }
    }
    if (result == null) {
      return Collections.emptySet();
    }
    return result;
  }

  private <S> Set<Mapper<? super S, ?>> getMappers(S source) {
    if (!(myMappers.containsKey(source))) {
      return Collections.emptySet();
    }
    Object mappers = myMappers.get(source);
    if (mappers instanceof Mapper) {
      return Collections.<Mapper<? super S, ?>>singleton((Mapper<? super S, ?>) mappers);
    } else {
      Set<Mapper<? super S, ?>> result = new HashSet<>();
      for (Mapper<?, ?> m : (Set<Mapper<? super S, ?>>) mappers) {
        result.add((Mapper<? super S, ?>) m);
      }
      return result;
    }
  }
}