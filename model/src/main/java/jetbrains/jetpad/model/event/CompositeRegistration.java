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
package jetbrains.jetpad.model.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompositeRegistration implements Registration {
  private List<Registration> myRegistrations;
  private boolean myRemoved;
  private boolean myRemoving;

  public CompositeRegistration(Registration... regs) {
    myRegistrations = new ArrayList<Registration>(regs.length);
    myRegistrations.addAll(Arrays.asList(regs));
  }

  public CompositeRegistration add(Registration r) {
    myRegistrations.add(r);
    return this;
  }

  public CompositeRegistration add(Registration... rs) {
    for (Registration r : rs) {
      add(r);
    }
    return this;
  }

  public boolean isEmpty() {
    return myRegistrations.isEmpty();
  }

  @Override
  public void remove() {
    if (myRemoved || myRemoving) throw new IllegalStateException();

    myRemoving = true;
    try {
      Callbacks.call(myRegistrations, new Callbacks.Caller<Registration>() {
        @Override
        public void call(Registration r) {
          r.remove();
        }
      });
    } finally {
      myRemoving = false;
    }
    myRemoved = true;
  }
}