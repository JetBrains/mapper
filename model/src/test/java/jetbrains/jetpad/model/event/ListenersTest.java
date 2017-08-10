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
package jetbrains.jetpad.model.event;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.Value;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class ListenersTest {
  private Listeners<Listener> myListeners;
  private boolean myInnerListenerCalled;

  @Before
  public void setup() {
    myListeners = new Listeners<>();
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

  @Test
  public void exceptionInListenerAndInThrowableHandlers() {
    Registration addReg = myListeners.add(new Listener() {
      @Override
      public void act() {
        throw new RuntimeException();
      }
    });

    final Value<RuntimeException> exception = new Value<>(null);

    //default ThrowableHandlers test handler throws exception
    try {
      myListeners.fire(new ListenerCaller<Listener>() {
        @Override
        public void call(Listener l) {
          l.act();
        }
      });
    } catch (RuntimeException e) {
      exception.set(e);
    }

    addReg.remove();

    assertNotNull(exception.get());
    assertEquals(0, myListeners.size());
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

  private interface Listener {
    void act();
  }
}