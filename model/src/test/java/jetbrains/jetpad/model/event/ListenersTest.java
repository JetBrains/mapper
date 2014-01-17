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

import jetbrains.jetpad.base.Value;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ListenersTest {
  @Test
  public void addAndRemoveRegistrationInFire() {
    final Listeners<Listener> listeners = new Listeners<Listener>();
    final Value<Boolean> innerListenerCalled = new Value<Boolean>(false);

    listeners.add(new Listener() {
      @Override
      public void act() {
        Registration r = listeners.add(new Listener() {
          @Override
          public void act() {
            innerListenerCalled.set(true);
          }
        });
        r.remove();
      }
    });

    assertEquals(1, listeners.size());

    listeners.fire(new ListenerCaller<Listener>() {
      @Override
      public void call(Listener l) {
        l.act();
      }
    });

    assertFalse(innerListenerCalled.get());
    assertEquals(1, listeners.size());
  }

  private static interface Listener {
    void act();
  }
}
