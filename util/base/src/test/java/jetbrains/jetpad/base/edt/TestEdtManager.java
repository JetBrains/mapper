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

public class TestEdtManager implements EdtManager {
  private TestEventDispatchThread myEdt;

  public TestEdtManager() {
    this("");
  }

  public TestEdtManager(String name) {
    myEdt = new TestEventDispatchThread(name);
  }

  @Override
  public TestEventDispatchThread getEdt() {
    return myEdt;
  }

  @Override
  public void finish() {
    myEdt.finish();
  }

  @Override
  public void kill() {
    myEdt.kill();
  }

  @Override
  public boolean isStopped() {
    return myEdt.isFinished();
  }

  @Override
  public String toString() {
    return "manager for " + myEdt;
  }
}
