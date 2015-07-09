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

public class BufferingTaskManager extends RunningTaskManager {
  public BufferingTaskManager() {
    super();
  }

  public BufferingTaskManager(String name) {
    super(name);
  }

  @Override
  public void doSchedule(Runnable r) {
    addTaskToQueue(r);
  }

  public void flush() {
    flushAll();
  }

  public void flush(final int number) {
    flush(new Flusher() {
      @Override
      public int getLimit() {
        return number;
      }
    });
  }
}
