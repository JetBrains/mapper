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
package jetbrains.jetpad.base;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import jetbrains.jetpad.base.function.Consumer;
import jetbrains.jetpad.base.function.Function;
import jetbrains.jetpad.base.function.Supplier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@GwtCompatible
public class Asyncs {
  public static boolean isFinished(Async<?> async) {
    final Value<Boolean> finished = new Value<>(false);
    async.onResult(new Consumer<Object>() {
        @Override
        public void accept(Object item) {
          finished.set(true);
        }
      },
      new Consumer<Throwable>() {
        @Override
        public void accept(Throwable failure) {
          finished.set(true);
        }
      }).remove();
    return finished.get();
  }

  public static <ValueT> Async<ValueT> constant(final ValueT val) {
    return new Async<ValueT>() {
      @Override
      public Registration onSuccess(Consumer<? super ValueT> successHandler) {
        successHandler.accept(val);
        return Registration.EMPTY;
      }

      @Override
      public Registration onResult(Consumer<? super ValueT> successHandler, Consumer<Throwable> failureHandler) {
        return onSuccess(successHandler);
      }

      @Override
      public Registration onFailure(Consumer<Throwable> failureHandler) {
        return Registration.EMPTY;
      }

      @Override
      public <ResultT> Async<ResultT> map(final Function<? super ValueT, ? extends ResultT> success) {
        ResultT result;
        try {
          result = success.apply(val);
        } catch (Throwable t) {
          return Asyncs.failure(t);
        }
        return Asyncs.constant(result);
      }

      @Override
      public <ResultT> Async<ResultT> flatMap(Function<? super ValueT, Async<ResultT>> success) {
        Async<ResultT> result;
        try {
          result = success.apply(val);
        } catch (Throwable t) {
          return Asyncs.failure(t);
        }
        return result == null ? Asyncs.<ResultT>constant(null) : result;
      }
    };
  }

  public static <ValueT> Async<ValueT> failure(final Throwable t) {
    return new Async<ValueT>() {
      @Override
      public Registration onSuccess(Consumer<? super ValueT> successHandler) {
        return Registration.EMPTY;
      }

      @Override
      public Registration onResult(Consumer<? super ValueT> successHandler, Consumer<Throwable> failureHandler) {
        return onFailure(failureHandler);
      }

      @Override
      public Registration onFailure(Consumer<Throwable> failureHandler) {
        failureHandler.accept(t);
        return Registration.EMPTY;
      }

      @Override
      public <ResultT> Async<ResultT> map(final Function<? super ValueT, ? extends ResultT> success) {
        return Asyncs.failure(t);
      }

      @Override
      public <ResultT> Async<ResultT> flatMap(Function<? super ValueT, Async<ResultT>> success) {
        return Asyncs.failure(t);
      }
    };
  }

  public static <ResultT> Async<Void> toVoid(Async<ResultT> a) {
    return map(a, new Function<ResultT, Void>() {
      @Override
      public Void apply(ResultT input) {
        return null;
      }
    }, new SimpleAsync<Void>());
  }

  static <SourceT, TargetT, AsyncResultT extends SourceT> Async<TargetT> map(Async<AsyncResultT> async,
      final Function<SourceT, ? extends TargetT> f, final ResolvableAsync<TargetT> resultAsync) {
    async.onResult(new Consumer<AsyncResultT>() {
        @Override
        public void accept(AsyncResultT item) {
          TargetT apply;
          try {
            apply = f.apply(item);
          } catch (Exception e) {
            resultAsync.failure(e);
            return;
          }
          resultAsync.success(apply);
        }
      },
      new Consumer<Throwable>() {
        @Override
        public void accept(Throwable throwable) {
          resultAsync.failure(throwable);
          }
        });
    return resultAsync;
  }

  static <SourceT, TargetT> Async<TargetT> select(Async<SourceT> async,
      final Function<? super SourceT, Async<TargetT>> f, final ResolvableAsync<TargetT> resultAsync) {
    async.onResult(new Consumer<SourceT>() {
        @Override
        public void accept(SourceT item) {
          Async<TargetT> async1;
          try {
            async1 = f.apply(item);
          } catch (Exception e) {
            resultAsync.failure(e);
            return;
          }
          if (async1 == null) {
            resultAsync.success(null);
          } else {
            delegate(async1, resultAsync);
          }
        }
      },
      new Consumer<Throwable>() {
        @Override
        public void accept(Throwable throwable) {
          resultAsync.failure(throwable);
        }
      });
    return resultAsync;
  }

  public static <FirstT, SecondT> Async<SecondT> seq(Async<FirstT> first, final Async<SecondT> second) {
    return select(first, new Function<FirstT, Async<SecondT>>() {
      @Override
      public Async<SecondT> apply(FirstT input) {
        return second;
      }
    }, new SimpleAsync<SecondT>());
  }

  public static Async<Void> parallel(final Async<?>... asyncs) {
    return parallel(Arrays.asList(asyncs));
  }

  public static Async<Void> parallel(final Collection<? extends Async<?>> asyncs) {
    return parallel(asyncs, false);
  }

  public static Async<Void> parallel(Collection<? extends Async<?>> asyncs, boolean alwaysSucceed) {
    return parallel(asyncs, alwaysSucceed, new SimpleParallelData(asyncs.size()));
  }

  @GwtIncompatible
  public static Async<Void> threadSafeParallel(Collection<? extends Async<?>> asyncs) {
    return parallel(asyncs, false, new ThreadSafeParallelData(asyncs.size()));
  }

  public static Async<Void> parallel(Collection<? extends Async<?>> asyncs, final boolean alwaysSucceed,
      final ParallelData parallelData) {
    final ResolvableAsync<Void> result = parallelData.getResultAsync();
    final Runnable checkTermination = new Runnable() {
      @Override
      public void run() {
        if (parallelData.decrementInProgressAndGet() == 0) {
          List<Throwable> exceptions = parallelData.getExceptions();
          if (!exceptions.isEmpty() && !alwaysSucceed) {
            result.failure(new ThrowableCollectionException(exceptions));
          } else {
            result.success(null);
          }
        }
      }
    };

    for (Async<?> a : asyncs) {
      a.onResult(new Consumer<Object>() {
          @Override
          public void accept(Object item) {
            checkTermination.run();
          }
        },
        new Consumer<Throwable>() {
          @Override
          public void accept(Throwable failure) {
            parallelData.addException(failure);
            checkTermination.run();
          }
        });
    }

    if (asyncs.isEmpty()) {
      return Asyncs.constant(null);
    }

    return result;
  }

  public static <ItemT> Async<List<ItemT>> composite(List<Async<ItemT>> asyncs) {
    final SimpleAsync<List<ItemT>> result = new SimpleAsync<>();
    final SortedMap<Integer, ItemT> succeeded = new TreeMap<>();
    final List<Throwable> exceptions = new ArrayList<>(0);
    final Value<Integer> inProgress = new Value<>(asyncs.size());

    final Runnable checkTermination = new Runnable() {
      @Override
      public void run() {
        if (inProgress.get() == 0) {
          if (exceptions.isEmpty()) {
            result.success(new ArrayList<>(succeeded.values()));
          } else {
            if (exceptions.size() == 1) {
              result.failure(exceptions.get(0));
            } else {
              result.failure(new ThrowableCollectionException(exceptions));
            }
          }
        }
      }
    };

    int i = 0;
    for (Async<ItemT> async : asyncs) {
      final int counter = i++;
      async.onResult(
        new Consumer<ItemT>() {
          @Override
          public void accept(ItemT item) {
            succeeded.put(counter, item);
            inProgress.set(inProgress.get() - 1);
            checkTermination.run();
          }
        },
        new Consumer<Throwable>() {
          @Override
          public void accept(Throwable failure) {
            exceptions.add(failure);
            inProgress.set(inProgress.get() - 1);
            checkTermination.run();
          }
        });
    }

    if (asyncs.isEmpty()) {
      checkTermination.run();
    }

    return result;
  }

  public static <ResultT> Async<ResultT> untilSuccess(final Supplier<Async<ResultT>> s) {
    final SimpleAsync<ResultT> result = new SimpleAsync<>();
    Async<ResultT> async;
    final Consumer<ResultT> successConsumer = new Consumer<ResultT>() {
      @Override
      public void accept(ResultT item) {
        result.success(item);
      }
    };
    try {
      async = s.get();
    } catch (Exception e) {
      untilSuccess(s).onSuccess(successConsumer);
      return result;
    }

    async.onResult(successConsumer,
      new Consumer<Throwable>() {
        @Override
        public void accept(Throwable failure) {
          untilSuccess(s).onSuccess(successConsumer);
        }
      });
    return result;
  }

  public static <ValueT> Registration delegate(Async<? extends ValueT> from, final AsyncResolver<? super ValueT> to) {
    return from.onResult(new Consumer<ValueT>() {
        @Override
        public void accept(ValueT item) {
          to.success(item);
        }
      }, new Consumer<Throwable>() {
        @Override
        public void accept(Throwable throwable) {
          to.failure(throwable);
        }
      });
  }

  public static <FirstT, SecondT> Async<Pair<FirstT, SecondT>> pair(final Async<FirstT> first, Async<SecondT> second) {
    final SimpleAsync<Pair<FirstT, SecondT>> res = new SimpleAsync<>();
    SimpleAsync<Void> proxy = new SimpleAsync<>();
    final PairedAsync<FirstT> firstPaired = new PairedAsync<>(first);
    final PairedAsync<SecondT> secondPaired = new PairedAsync<>(second);
    proxy.onResult(
        new Consumer<Void>() {
          @Override
          public void accept(Void item) {
            if (firstPaired.mySucceeded && secondPaired.mySucceeded) {
              res.success(new Pair<>(firstPaired.myItem, secondPaired.myItem));
            } else {
              res.failure(new Throwable("internal error in pair async"));
            }
          }
        },
        new Consumer<Throwable>() {
          @Override
          public void accept(Throwable throwable) {
            res.failure(throwable);
          }
        });
    firstPaired.pair(secondPaired, proxy);
    secondPaired.pair(firstPaired, proxy);
    return res;
  }

  @GwtIncompatible("Uses threading primitives")
  public static <ResultT> ResultT get(Async<ResultT> async) {
    return get(async, new Awaiter() {
      @Override
      public void await(CountDownLatch latch) throws InterruptedException {
        latch.await();
      }
    });
  }

  @GwtIncompatible("Uses threading primitives")
  public static <ResultT> ResultT get(Async<ResultT> async, final long timeout, final TimeUnit timeUnit) {
    return get(async, new Awaiter() {
      @Override
      public void await(CountDownLatch latch) throws InterruptedException {
        if (!latch.await(timeout, timeUnit)) {
          throw new RuntimeException("timeout " + timeout + " " + timeUnit + " exceeded");
        }
      }
    });
  }

  @GwtIncompatible("Uses threading primitives")
  private static <ResultT> ResultT get(Async<ResultT> async, Awaiter awaiter) {
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<ResultT> result = new AtomicReference<>(null);
    final AtomicReference<Throwable> error = new AtomicReference<>(null);
    async.onResult(
        new Consumer<ResultT>() {
          @Override
          public void accept(ResultT item) {
            result.set(item);
            latch.countDown();
          }
        },
        new Consumer<Throwable>() {
          @Override
          public void accept(Throwable failure) {
            error.set(failure);
            latch.countDown();
          }
        });
    try {
      awaiter.await(latch);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      if (error.get() == null) {
        error.set(e);
      }
    }
    if (error.get() != null) {
      if (error.get() instanceof RuntimeException) {
        throw (RuntimeException) error.get();
      } else if (error.get() instanceof Error) {
        throw (Error) error.get();
      } else {
        throw new RuntimeException(error.get());
      }
    } else {
      return result.get();
    }
  }

  @GwtIncompatible("Uses threading primitives")
  private interface Awaiter {
    void await(CountDownLatch latch) throws InterruptedException;
  }

  private static class PairedAsync<ItemT> {
    private Async<ItemT> myAsync;
    private ItemT myItem;
    private Boolean mySucceeded = false;
    private Registration myReg = null;

    public PairedAsync(Async<ItemT> async) {
      myAsync = async;
    }

    private <AnotherItemT> void pair(final PairedAsync<AnotherItemT> anotherInfo, final SimpleAsync<Void> async) {
      if (async.hasSucceeded() || async.hasFailed()) {
        return;
      }
      myReg = myAsync.onResult(
          new Consumer<ItemT>() {
            @Override
            public void accept(ItemT item) {
              myItem = item;
              mySucceeded = true;
              if (anotherInfo.mySucceeded) {
                async.success(null);
              }
            }
          },
          new Consumer<Throwable>() {
            @Override
            public void accept(Throwable failure) {
              //reg == null can happen in case if myAsync fails instantly
              if (anotherInfo.myReg != null) {
                anotherInfo.myReg.remove();
              }
              async.failure(failure);
            }
          });
    }
  }

  private static abstract class ParallelData {
    private final List<Throwable> myExceptions;
    private final ResolvableAsync<Void> myResultAsync;

    ParallelData(List<Throwable> expectionsList, ResolvableAsync<Void> resultAsync) {
      myExceptions = expectionsList;
      myResultAsync = resultAsync;
    }

    /**
     * Decreases amount of asyncs in progress by one and returns remaining amount
     */
    abstract int decrementInProgressAndGet();

    void addException(Throwable t) {
      myExceptions.add(t);
    }

    List<Throwable> getExceptions() {
      return myExceptions;
    }

    ResolvableAsync<Void> getResultAsync() {
      return myResultAsync;
    }
  }

  private static class SimpleParallelData extends ParallelData {
    private final Value<Integer> myInProgress;

    SimpleParallelData(int asyncsCount) {
      super(new ArrayList<Throwable>(), new SimpleAsync<Void>());
      myInProgress = new Value<>(asyncsCount);
    }

    @Override
    int decrementInProgressAndGet() {
      int decreasedValue = myInProgress.get() - 1;
      myInProgress.set(decreasedValue);
      return decreasedValue;
    }
  }

  @GwtIncompatible
  private static class ThreadSafeParallelData extends ParallelData {
    private final AtomicInteger myInProgress;

    ThreadSafeParallelData(int asyncsCount) {
      super(Collections.synchronizedList(new ArrayList<Throwable>()), new ThreadSafeAsync<Void>());
      myInProgress = new AtomicInteger(asyncsCount);
    }

    @Override
    int decrementInProgressAndGet() {
      return myInProgress.decrementAndGet();
    }
  }
}
