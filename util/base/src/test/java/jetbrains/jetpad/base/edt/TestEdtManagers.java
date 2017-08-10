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

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestEdtManagers implements EdtManagerFactory {
  public static void flush(TestEdtManagers... managers) {
    while (isSomethingScheduled(managers)) {
      for (TestEdtManagers edtManagers : managers) {
        edtManagers.flush();
      }
    }
  }

  private static boolean isSomethingScheduled(TestEdtManagers... managers) {
    for (TestEdtManagers edtManagers : managers) {
      if (!edtManagers.nothingScheduled(0)) {
        return true;
      }
    }
    return false;
  }

  private final List<TestEdtManager> managers = new ArrayList<>();

  @Override
  public TestEdtManager createEdtManager(String name) {
    final Value<Registration> removeReg = new Value<>(null);
    final TestEdtManager manager = new TestEdtManager(name) {
      @Override
      public void finish() {
        super.finish();
        removeReg.get().remove();
      }

      @Override
      public void kill() {
        super.kill();
        removeReg.get().remove();
      }
    };

    managers.add(manager);
    removeReg.set(new Registration() {
      @Override
      protected void doRemove() {
        managers.remove(manager);
      }
    });

    return manager;
  }

  public void flush() {
    flush(false);
  }

  public void flush(int passedTime) {
    for (int i = 0; i < passedTime; i++) {
      flush(true);
    }
  }

  public boolean nothingScheduled(int time) {
    for (TestEdtManager manager : managers) {
      if (!manager.getEdt().nothingScheduled(time)) {
        return false;
      }
    }
    return true;
  }

  public TestEdtManager getManager(String namePart) {
    TestEdtManager result = null;
    for (TestEdtManager edtManager : managers) {
      if (edtManager.getEdt().getName().contains(namePart)) {
        if (result != null) {
          throw new IllegalStateException("two managers contain '" + namePart + "' in their names: "
              + result + " and " + edtManager);
        }
        result = edtManager;
      }
    }
    if (result == null) {
      throw new IllegalStateException("no manager contains '" + namePart + "' in its name among " + managers);
    } else {
      return result;
    }
  }

  public TestEventDispatchThread getEdt(String namePart) {
    return getManager(namePart).getEdt();
  }

  List<TestEdtManager> getManagers() {
    return Collections.unmodifiableList(managers);
  }

  private void flush(boolean incTime) {
    int passedTime = incTime ? 1 : 0;
    boolean finished = false;
    while (!finished) {
      finished = true;
      int size = managers.size();
      List<TestEdtManager> copy = new ArrayList<>(managers);
      for (TestEdtManager manager : copy) {
        int runCommandNum = manager.getEdt().executeUpdates(passedTime);
        finished &= runCommandNum == 0;
      }
      if (managers.size() != size) {
        finished = false;
      }
      //we should increase time only once
      passedTime = 0;
    }
  }
}