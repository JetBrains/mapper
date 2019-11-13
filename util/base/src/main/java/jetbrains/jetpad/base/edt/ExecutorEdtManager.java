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
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.ThrowableHandlers;
import jetbrains.jetpad.base.function.Supplier;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public final class ExecutorEdtManager implements EdtManager, EventDispatchThread {
  private static final Logger LOG = Logger.getLogger(ExecutorEdtManager.class.getName());
  private static final int BIG_TIMEOUT_DAYS = 1;
  private final NamedThreadFactory myThreadFactory;
  private final ExecutorEdt myEdt;

  public ExecutorEdtManager() {
    this("");
  }

  public ExecutorEdtManager(String name) {
    myThreadFactory = new NamedThreadFactory(name);
    myEdt = new ExecutorEdt(myThreadFactory);
    LOG.info("Created " + this);
  }

  @Override
  public EventDispatchThread getEdt() {
    return this;
  }

  @Override
  public void finish() {
    ensureCanShutdown();
    myEdt.getExecutor().shutdown();
    waitTermination();
  }

  @Override
  public void kill() {
    ensureCanShutdown();
    myEdt.kill();
    waitTermination();
  }

  private void ensureCanShutdown() {
    if (myThreadFactory.inProducedThread()) {
      throw new IllegalStateException(this + ": cannot kill or finish from its own thread");
    }
  }

  private void waitTermination() {
    LOG.info("Start termination " + this);
    boolean terminated = false;
    try {
      terminated = myEdt.getExecutor().awaitTermination(BIG_TIMEOUT_DAYS, TimeUnit.DAYS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    if (!terminated) {
      ThrowableHandlers.handle(new RuntimeException(this
          + ": failed to terminate in " + BIG_TIMEOUT_DAYS + " days"));
    }
    LOG.info("Terminated " + this);
  }

  @Override
  public boolean isStopped() {
    return isThreadInactive();
  }

  @Override
  public long getCurrentTimeMillis() {
    return myEdt.getCurrentTimeMillis();
  }

  @Override
  public <ResultT> Async<ResultT> schedule(Supplier<ResultT> s) {
    try {
      return myEdt.schedule(s);
    } catch (RejectedExecutionException e) {
      throw new EdtException(e);
    }
  }

  @Override
  public <ResultT> Async<ResultT> flatSchedule(Supplier<Async<ResultT>> s) {
    try {
      return myEdt.flatSchedule(s);
    } catch (RejectedExecutionException e) {
      throw new EdtException(e);
    }
  }

  @Override
  public Async<Void> schedule(Runnable runnable) {
    try {
      return myEdt.schedule(runnable);
    } catch (RejectedExecutionException e) {
      throw new EdtException(e);
    }
  }

  private boolean isThreadInactive() {
    return myEdt.getExecutor().isShutdown();
  }

  @Override
  public Registration schedule(int delay, Runnable r) {
    Registration reg;
    try {
      reg = myEdt.schedule(delay, r);
    } catch (RejectedExecutionException e) {
      throw new EdtException(e);
    }
    return reg;
  }

  @Override
  public Registration scheduleRepeating(int period, Runnable r) {
    Registration reg;
    try {
      reg = myEdt.scheduleRepeating(period, r);
    } catch (RejectedExecutionException e) {
      throw new EdtException(e);
    }
    return reg;
  }

  @Override
  public String toString() {
    return "ExecutorEdtManager@" + Integer.toHexString(hashCode()) + myThreadFactory.getPrintName();
  }

  private static class ExecutorEdt extends DefaultAsyncEdt {
    private final ScheduledExecutorService myExecutor;
    private final ConcurrentMap<Integer, RunnableWithAsync<?>> myUnresolvedAsyncs = new ConcurrentHashMap<>();
    private final AtomicInteger myCounter = new AtomicInteger(0);

    ExecutorEdt(ThreadFactory threadFactory) {
      myExecutor = Executors.newSingleThreadScheduledExecutor(threadFactory);
    }

    @Override
    public long getCurrentTimeMillis() {
      return System.currentTimeMillis();
    }

    @Override
    protected <ResultT> Async<ResultT> asyncSchedule(final RunnableWithAsync<ResultT> task) {
      final int i = myCounter.incrementAndGet();
      myUnresolvedAsyncs.put(i, task);
      myExecutor.submit(new Runnable() {
        @Override
        public void run() {
          task.run();
          myUnresolvedAsyncs.remove(i);
        }
      });
      return task;
    }

    @Override
    public Registration schedule(int delay, Runnable r) {
      return new FutureRegistration(myExecutor.schedule(handleFailure(r), delay, TimeUnit.MILLISECONDS));
    }

    @Override
    public Registration scheduleRepeating(int period, Runnable r) {
      return new FutureRegistration(
          myExecutor.scheduleAtFixedRate(handleFailure(r), period, period, TimeUnit.MILLISECONDS));
    }

    private Runnable handleFailure(final Runnable r) {
      return new Runnable() {
        @Override
        public void run() {
          try {
            r.run();
          } catch (Throwable t) {
            ThrowableHandlers.handle(t);
          }
        }
      };
    }

    // do not use it directly, only for state checks and finalization.
    ExecutorService getExecutor() {
      return myExecutor;
    }

    @Override
    public String toString() {
      return "ExecutorEdt@" + Integer.toHexString(hashCode()) + " " + Thread.currentThread().getName();
    }

    private void kill() {
      myExecutor.shutdownNow();
      for (RunnableWithAsync<?> async : myUnresolvedAsyncs.values()) {
        async.fail();
      }
    }
  }

  private static class FutureRegistration extends Registration {
    private final Future<?> myFuture;

    FutureRegistration(Future<?> future) {
      myFuture = future;
    }

    @Override
    protected void doRemove() {
      myFuture.cancel(false);
    }
  }

  private static class NamedThreadFactory implements ThreadFactory {
    private final Object myId = new Object();
    private final String myName;

    NamedThreadFactory(String name) {
      if (name == null) {
        throw new IllegalArgumentException();
      }
      myName = name;
    }

    @Override
    public Thread newThread(Runnable r) {
      return new MyThread(myId, r, myName);
    }

    boolean inProducedThread() {
      Thread currentThread = Thread.currentThread();
      return currentThread instanceof MyThread && myId == ((MyThread) currentThread).getEdtId();
    }

    String getPrintName() {
      return "".equals(myName) ? "" : " (" + myName + ")";
    }
  }

  private static class MyThread extends Thread {
    private final Object myEdtId;

    MyThread(Object edtId, Runnable target, String name) {
      super(target, name);
      myEdtId = edtId;
    }

    Object getEdtId() {
      return myEdtId;
    }
  }
}
