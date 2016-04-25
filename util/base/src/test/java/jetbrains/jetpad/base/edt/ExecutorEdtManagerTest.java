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

import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.base.ThrowableHandlers;
import jetbrains.jetpad.test.BaseTestCase;
import jetbrains.jetpad.test.Slow;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Slow
public class ExecutorEdtManagerTest extends BaseTestCase {
  private boolean mySuccess;

  @Test(expected = RuntimeException.class)
  public void testSubmitAfterShutdown() {
    EdtManager manager = new ExecutorEdtManager("MyEdt");
    EventDispatchThread edt = manager.getEdt();
    edt.schedule(new Runnable() {
      @Override
      public void run() {
        ThrowableHandlers.addHandler(new Handler<Throwable>() {
          @Override
          public void handle(Throwable event) {
            throw (event instanceof RuntimeException) ? (RuntimeException) event : new RuntimeException(event);
          }
        });
      }
    });
    manager.finish();
    edt.schedule(new Runnable() {
      @Override
      public void run() {
        mySuccess = true;
      }
    });
    assertFalse(mySuccess);
  }

  @Test
  public void finishFromItself() {
    shutdownFromItself(new Handler<EdtManager>() {
      @Override
      public void handle(EdtManager manager) {
        manager.finish();
      }
    });
  }

  @Test
  public void killFromItself() {
    shutdownFromItself(new Handler<EdtManager>() {
      @Override
      public void handle(EdtManager manager) {
        manager.kill();
      }
    });
  }

  private void shutdownFromItself(final Handler<EdtManager> shutdowner) {
    final EdtManager manager = new ExecutorEdtManager("MyEdt");
    final AtomicBoolean caught = new AtomicBoolean(false);
    final CountDownLatch latch = new CountDownLatch(1);
    manager.getEdt().schedule(new Runnable() {
      @Override
      public void run() {
        try {
          shutdowner.handle(manager);
        } catch (IllegalStateException e) {
          caught.set(true);
        } finally {
          latch.countDown();
        }
      }
    });

    try {
      if (!latch.await(100, TimeUnit.MILLISECONDS)) {
        Assert.fail("Timeout exceeded");
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    assertTrue(caught.get());
    assertFalse(manager.isStopped());
    manager.finish();
  }
}