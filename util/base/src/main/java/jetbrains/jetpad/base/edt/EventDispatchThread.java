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

import jetbrains.jetpad.base.Registration;

public interface EventDispatchThread {

  long getCurrentTimeMillis();

  /**
   * @param r Runnable to be scheduled
   * @throws EdtException in case of failure, e.g. if the thread has been shutdown
   */
  void schedule(Runnable r);

  /**
   * @param delay in milliseconds for the initial delay
   * @param r Runnable to be scheduled
   * @throws EdtException in case of failure, e.g. if the thread has been shutdown
   */
  Registration schedule(int delay, Runnable r);

  /**
   * @param period in milliseconds for the initial and between-event delay
   * @param r Runnable to be scheduled
   * @throws EdtException in case of failure, e.g. if the thread has been shutdown
   */
  Registration scheduleRepeating(int period, Runnable r);
}