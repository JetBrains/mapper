/*
 * Copyright 2012-2017 JetBrains s.r.o
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

import jetbrains.jetpad.base.Async;
import jetbrains.jetpad.base.Asyncs;
import jetbrains.jetpad.base.Registration;

public final class NullEventDispatchThread extends DefaultAsyncEdt {
  @Override
  public long getCurrentTimeMillis() {
    return 0L;
  }

  @Override
  protected <ResultT> Async<ResultT> asyncSchedule(RunnableWithAsync<ResultT> runnableWithAsync) {
    return Asyncs.constant(null);
  }

  @Override
  public Registration schedule(int delay, Runnable r) {
    return Registration.EMPTY;
  }

  @Override
  public Registration scheduleRepeating(int period, Runnable r) {
    return Registration.EMPTY;
  }
}
