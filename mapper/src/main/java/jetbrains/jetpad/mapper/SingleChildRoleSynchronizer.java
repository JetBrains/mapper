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

import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.model.property.WritableProperty;

import java.util.Collections;
import java.util.List;

class SingleChildRoleSynchronizer<SourceT, TargetT> extends BaseRoleSynchronizer<SourceT, TargetT> {
  private Property<Mapper<? extends SourceT, ? extends TargetT>> myTargetMapper;
  private ReadableProperty<SourceT> myChildProperty;
  private WritableProperty<TargetT> myTargetProperty;
  private Registration myChildRegistration;

  SingleChildRoleSynchronizer(Mapper<?, ?> mapper, ReadableProperty<SourceT> childProperty, final WritableProperty<TargetT> targetProperty, MapperFactory<SourceT, TargetT> factory) {
    if (childProperty == null || targetProperty == null) {
      throw new NullPointerException();
    }

    myTargetMapper = mapper.createChildProperty();
    myChildProperty = childProperty;
    myTargetProperty = targetProperty;

    addMapperFactory(factory);
  }

  @Override
  public List<Mapper<? extends SourceT, ? extends TargetT>> getMappers() {
    if (myTargetMapper.get() == null) {
      return Collections.emptyList();
    }
    return Collections.<Mapper<? extends SourceT, ? extends TargetT>>singletonList(myTargetMapper.get());
  }


  @Override
  public void attach(SynchronizerContext ctx) {
    sync();
    myChildRegistration = myChildProperty.addHandler(new EventHandler<PropertyChangeEvent<SourceT>>() {
      @Override
      public void onEvent(PropertyChangeEvent<SourceT> event) {
        sync();
      }
    });
  }

  @Override
  public void detach() {
    myChildRegistration.remove();
    myTargetProperty.set(null);
    myTargetMapper.set(null);
  }

  private void sync() {
    SourceT modelValue = myChildProperty.get();
    SourceT viewValue = myTargetMapper.get() == null ? null : myTargetMapper.get().getSource();
    if (modelValue == viewValue) return;

    if (modelValue != null) {
      Mapper<? extends SourceT, ? extends TargetT> mapper = createMapper(modelValue);
      myTargetMapper.set(mapper);
      myTargetProperty.set(mapper.getTarget());
      processMapper(mapper);
    } else {
      myTargetMapper.set(null);
      myTargetProperty.set(null);
    }
  }

}