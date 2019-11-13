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
import jetbrains.jetpad.base.Runnables;
import jetbrains.jetpad.base.ThrowableHandlers;
import jetbrains.jetpad.base.function.Consumer;
import jetbrains.jetpad.base.function.Supplier;
import jetbrains.jetpad.test.BaseTestCase;
import jetbrains.jetpad.test.Slow;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static jetbrains.jetpad.base.edt.EdtTestUtil.assertAsyncFulfilled;
import static jetbrains.jetpad.base.edt.EdtTestUtil.assertAsyncRejected;
import static jetbrains.jetpad.base.edt.EdtTestUtil.assertFlatAsyncFulfilled;
import static jetbrains.jetpad.base.edt.EdtTestUtil.getDefaultSupplier;
import static jetbrains.jetpad.base.edt.EdtTestUtil.killAndAssertFailure;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Slow
public class ExecutorEdtManagerTest extends BaseTestCase {

  @Test(expected = RuntimeException.class)
  public void testSubmitAfterShutdown() {
    EdtManager manager = new ExecutorEdtManager("MyEdt1");
    EventDispatchThread edt = manager.getEdt();
    edt.schedule(new Runnable() {
      @Override
      public void run() {
        ThrowableHandlers.addHandler(new Consumer<Throwable>() {
          @Override
          public void accept(Throwable event) {
            throw (event instanceof RuntimeException) ? (RuntimeException) event : new RuntimeException(event);
          }
        });
      }
    });
    manager.finish();
    final AtomicBoolean executed = new AtomicBoolean(false);
    edt.schedule(new Runnable() {
      @Override
      public void run() {
        executed.set(true);
      }
    });
    assertFalse(executed.get());
  }

  @Test
  public void finishFromItself() {
    shutdownFromItself(new Consumer<EdtManager>() {
      @Override
      public void accept(EdtManager edtManager) {
        edtManager.finish();
      }
    });
  }

  @Test
  public void killFromItself() {
    shutdownFromItself(new Consumer<EdtManager>() {
      @Override
      public void accept(EdtManager edtManager) {
        edtManager.kill();
      }
    });
  }

  @Test
  public void killFailsRunningAsyncs() {
    scheduleInfiniteAndKill();
  }

  @Test
  public void killFailsNotStartedAsyncs() {
    scheduleInfiniteAndKill(getDefaultSupplier());
  }

  private void scheduleInfiniteAndKill(Supplier<?>... restTasks) {
    ExecutorEdtManager manager = new ExecutorEdtManager("Manager");
    final CountDownLatch runnableStartedLatch = new CountDownLatch(1);

    Async<?>[] asyncs = new Async<?>[restTasks.length + 1];

    asyncs[0] = scheduleInfiniteTask(manager, runnableStartedLatch);
    for (int i = 0; i < restTasks.length; i++) {
      asyncs[i + 1] = manager.schedule(restTasks[i]);
    }
    await(runnableStartedLatch);

    killAndAssertFailure(manager, asyncs);
  }

  private Async<Integer> scheduleInfiniteTask(ExecutorEdtManager manager, final CountDownLatch runnableStartedLatch) {
    return manager.schedule(new Supplier<Integer>() {
      @Override
      public Integer get() {
        runnableStartedLatch.countDown();
        try {
          new CountDownLatch(1).await();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        return 42;
      }
    });
  }

  private void shutdownFromItself(final Consumer<EdtManager> shutdowner) {
    final EdtManager manager = new ExecutorEdtManager("MyEdt2");
    final AtomicBoolean caught = new AtomicBoolean(false);
    final CountDownLatch latch = new CountDownLatch(1);
    manager.getEdt().schedule(new Runnable() {
      @Override
      public void run() {
        try {
          shutdowner.accept(manager);
        } catch (IllegalStateException e) {
          caught.set(true);
        } finally {
          latch.countDown();
        }
      }
    });

    await(latch);
    assertTrue(caught.get());
    assertFalse(manager.isStopped());
    manager.finish();
  }

  private void await(CountDownLatch latch) {
    try {
      if (!latch.await(100, TimeUnit.MILLISECONDS)) {
        Assert.fail("Timeout exceeded");
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void finishFromThreadWithEqualName() {
    final EdtManager manager = new ExecutorEdtManager("MyEdt3");
    manager.getEdt().schedule(Runnables.EMPTY); // force thread creation

    final CountDownLatch latch = new CountDownLatch(1);
    Thread other = new Thread(new Runnable() {
      @Override
      public void run() {
        manager.finish();
        latch.countDown();
      }
    }, "MyEdt3");
    other.start();
    await(latch);
    other.interrupt();

    assertTrue(manager.isStopped());
  }

  @Test
  public void asyncFulfilled() {
    ExecutorEdtManager edtManager = new ExecutorEdtManager("EdtManager");
    assertAsyncFulfilled(edtManager, awaitExecution(edtManager));
  }

  @Test
  public void flatAsyncFulfilled() {
    ExecutorEdtManager edtManager = new ExecutorEdtManager("EdtManager");
    assertFlatAsyncFulfilled(edtManager, awaitExecution(edtManager));
  }

  @Test
  public void asyncRejected() {
    ExecutorEdtManager edtManager = new ExecutorEdtManager("EdtManager");
    assertAsyncRejected(edtManager, awaitExecution(edtManager));
  }

  private Runnable awaitExecution(final ExecutorEdtManager edtManager) {
    return new Runnable() {
      @Override
      public void run() {
        final CountDownLatch latch = new CountDownLatch(1);
        edtManager.schedule(new Runnable() {
          @Override
          public void run() {
            latch.countDown();
          }
        });
        await(latch);
      }
    };
  }
}
