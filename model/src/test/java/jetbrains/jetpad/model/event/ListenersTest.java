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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ListenersTest {
  private Listeners<Listener> myListeners;
  private boolean myInnerListenerCalled;

  @Before
  public void setup() {
    myListeners = new Listeners<Listener>();
    myInnerListenerCalled = false;
  }

  @Test
  public void addAndRemoveRegistrationInFire() {
    myListeners.add(new Listener() {
      @Override
      public void act() {
        myListeners.add(createInnerListener()).remove();
      }
    });
    fireAndCheck(1);
  }

  @Test
  public void addRemoveAddInFire() {
    myListeners.add(new Listener() {
      @Override
      public void act() {
        Listener l = createInnerListener();
        myListeners.add(l).remove();
        myListeners.add(l);
      }
    });
    fireAndCheck(2);
  }

  private void fireAndCheck(int expectedListenersSize) {
    assertEquals(1, myListeners.size());
    myListeners.fire(new ListenerCaller<Listener>() {
      @Override
      public void call(Listener l) {
        l.act();
      }
    });
    assertEquals(expectedListenersSize, myListeners.size());
    assertFalse(myInnerListenerCalled);
  }

  private Listener createInnerListener() {
    return new Listener() {
      @Override
      public void act() {
        myInnerListenerCalled = true;
      }
    };
  }

  private static interface Listener {
    void act();
  }
}
