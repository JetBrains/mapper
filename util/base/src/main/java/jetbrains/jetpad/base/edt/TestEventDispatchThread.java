/*
 * Copyright 2012-2014 JetBrains s.r.o
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

import java.util.ArrayList;
import java.util.List;

public class TestEventDispatchThread implements EventDispatchThread {
  private List<Runnable> myRunnables = new ArrayList<>();

  public void executeUpdates() {
    while (!myRunnables.isEmpty()) {
      List<Runnable> toExecute = new ArrayList<>(myRunnables);
      myRunnables.clear();
      for (Runnable runnable : toExecute) {
        runnable.run();
      }
    }
  }

  @Override
  public void schedule(Runnable r) {
    myRunnables.add(r);
  }
}