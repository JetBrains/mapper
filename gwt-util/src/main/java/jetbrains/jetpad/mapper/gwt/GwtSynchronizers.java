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
package jetbrains.jetpad.mapper.gwt;

import com.google.gwt.event.shared.HandlerRegistration;
import jetbrains.jetpad.mapper.Synchronizer;
import jetbrains.jetpad.mapper.SynchronizerContext;

import jetbrains.jetpad.base.function.Supplier;

public class GwtSynchronizers {
  public static Synchronizer forRegistration(final Supplier<HandlerRegistration> reg) {
    return new Synchronizer() {
      HandlerRegistration myReg;

      @Override
      public void attach(SynchronizerContext ctx) {
        myReg = reg.get();
      }

      @Override
      public void detach() {
        myReg.removeHandler();
      }
    };
  }

  public static Synchronizer forRegistration(final HandlerRegistration r) {
    return new Synchronizer() {
      @Override
      public void attach(SynchronizerContext ctx) {
      }

      @Override
      public void detach() {
        r.removeHandler();
      }
    };
  }
}