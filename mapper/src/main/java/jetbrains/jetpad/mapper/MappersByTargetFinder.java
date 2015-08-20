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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import jetbrains.jetpad.base.Registration;

import java.util.Collection;

public class MappersByTargetFinder {
  private Registration myRegistration;

  private Multimap<Object, Mapper<?, ?>> myTargetToMappers = HashMultimap.create();

  public MappersByTargetFinder(MappingContext ctx) {
    for (Mapper<?, ?> mapper : ctx.getMappers()) {
      myTargetToMappers.put(mapper.getTarget(), mapper);
    }

    myRegistration = ctx.addListener(new MappingContextListener() {
      @Override
      public void onMapperRegistered(Mapper<?, ?> mapper) {
        myTargetToMappers.put(mapper.getTarget(), mapper);
      }

      @Override
      public void onMapperUnregistered(Mapper<?, ?> mapper) {
        Object target = mapper.getTarget();
        if (!myTargetToMappers.get(target).contains(mapper)) {
          throw new IllegalStateException("unregistered unknown mapper " + mapper + " with target " + target);
        }
        myTargetToMappers.get(target).remove(mapper);
      }
    });
  }

  public Collection<Mapper<?, ?>> getMappers(Object target) {
    return myTargetToMappers.get(target);
  }

  public void dispose() {
    myRegistration.remove();
  }
}
