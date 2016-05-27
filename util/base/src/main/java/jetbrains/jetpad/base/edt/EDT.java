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
package jetbrains.jetpad.base.edt;

import jetbrains.jetpad.base.Value;

public class Edt {
  public static Runnable validateInEdt(final EventDispatchThread validateOn, final Runnable validator) {
    final Value<Boolean> valid = new Value<>(true);

    final Runnable edtValidator = new Runnable() {
      @Override
      public void run() {
        if (valid.get()) return;
        validator.run();
        valid.set(true);
      }
    };

    return new Runnable() {
      @Override
      public void run() {
        valid.set(false);
        validateOn.schedule(edtValidator);
      }
    };
  }
}