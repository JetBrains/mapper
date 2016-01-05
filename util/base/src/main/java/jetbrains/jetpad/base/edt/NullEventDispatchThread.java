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
package jetbrains.jetpad.base.edt;

import jetbrains.jetpad.base.Registration;

public final class NullEventDispatchThread implements EventDispatchThread {
  @Override
  public long getCurrentTimeMillis() {
    return 0L;
  }

  @Override
  public void schedule(Runnable r) {
  }

  @Override
  public Registration schedule(int delayMillis, Runnable r) {
    return Registration.EMPTY;
  }

  @Override
  public Registration scheduleRepeating(int periodMillis, Runnable r) {
    return Registration.EMPTY;
  }
}