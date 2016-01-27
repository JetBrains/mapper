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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import jetbrains.jetpad.base.Disposable;
import jetbrains.jetpad.base.Registration;

import java.util.Collection;

public class MappingIndex implements Disposable {
  private Multimap<Object, Mapping<?, ?>> mySourceIndex = HashMultimap.create();
  private Multimap<Object, Mapping<?, ?>> myTargetIndex = HashMultimap.create();
  private Registration myRegistration;

  public MappingIndex(MappingContext ctx) {
    for (Mapping<?, ?> mapping : ctx.getMappings()) {
      add(mapping);
    }

    myRegistration = ctx.addListener(new MappingsListener() {
      @Override
      public void onMappingRegistered(Mapping<?, ?> mapping) {
        add(mapping);
      }

      @Override
      public void onMappingUnregistered(Mapping<?, ?> mapping) {
        remove(mapping);
      }
    });
  }

  public Collection<Mapping<?, ?>> getBySource(Object source) {
    return mySourceIndex.get(source);
  }

  public Collection<Mapping<?, ?>> getByTarget(Object target) {
    return myTargetIndex.get(target);
  }

  public void dispose() {
    myRegistration.remove();
  }

  private void remove(Mapping<?, ?> mapping) {
    Object source = mapping.getSource();
    if (!mySourceIndex.get(source).remove(mapping)) {
      throw new IllegalStateException("unregistered unknown mapping " + mapping + " with source " + source);
    }
    Object target = mapping.getTarget();
    if (!myTargetIndex.get(target).remove(mapping)) {
      throw new IllegalStateException("unregistered unknown mapping " + mapping + " with target " + target);
    }
  }

  private void add(Mapping<?, ?> mapping) {
    mySourceIndex.put(mapping.getSource(), mapping);
    myTargetIndex.put(mapping.getTarget(), mapping);
  }
}
