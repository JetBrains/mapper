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
import com.google.common.base.Function;
import com.google.common.base.Supplier;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@GwtCompatible
public class Asyncs {
  public static boolean isSucceeded(Async<?> async) {
    final Value<Boolean> succeeded = new Value<>(false);
    async.onSuccess(new Handler<Object>() {
      @Override
      public void handle(Object item) {
        succeeded.set(true);
      }
    }).remove();
    return succeeded.get();
  }

  public static boolean isFinished(Async<?> async) {
    final Value<Boolean> finished = new Value<>(false);
    async.onResult(new Handler<Object>() {
      @Override
      public void handle(Object item) {
        finished.set(true);
      }
    }, new Handler<Throwable>() {
      @Override
      public void handle(Throwable item) {
        finished.set(true);
      }
    }).remove();
    return finished.get();
  }

  public static <ValueT> Async<ValueT> constant(final ValueT val) {
    return new Async<ValueT>() {
      @Override
      public Registration onSuccess(Handler<? super ValueT> successHandler) {
        successHandler.handle(val);
        return Registration.EMPTY;
      }

      @Override
      public Registration onResult(Handler<? super ValueT> successHandler, Handler<Throwable> failureHandler) {
        return onSuccess(successHandler);
      }

      @Override
      public Registration onFailure(Handler<Throwable> failureHandler) {
        return Registration.EMPTY;
      }
    };
  }

  public static <ValueT> Async<ValueT> failure(final Throwable t) {
    return new Async<ValueT>() {
      @Override
      public Registration onSuccess(Handler<? super ValueT> successHandler) {
        return Registration.EMPTY;
      }

      @Override
      public Registration onResult(Handler<? super ValueT> successHandler, Handler<Throwable> failureHandler) {
        return onFailure(failureHandler);
      }

      @Override
      public Registration onFailure(Handler<Throwable> failureHandler) {
        failureHandler.handle(t);
        return Registration.EMPTY;
      }
    };
  }

  public static <ResultT> Async<Void> toVoid(Async<ResultT> a) {
    return map(a, new Function<ResultT, Void>() {
      @Override
      public Void apply(ResultT input) {
        return null;
      }
    });
  }

  public static <SourceT, TargetT, AsyncResultT extends SourceT> Async<TargetT> map(Async<AsyncResultT> async, final Function<SourceT, TargetT> f) {
    final SimpleAsync<TargetT> result = new SimpleAsync<>();
    async.onResult(new Handler<SourceT>() {
      @Override
      public void handle(SourceT item) {
        TargetT apply;
        try {
          apply = f.apply(item);
        } catch (Exception e) {
          result.failure(e);
          return;
        }
        result.success(apply);
      }
    }, new Handler<Throwable>() {
      @Override
      public void handle(Throwable item) {
        result.failure(item);
      }
    });
    return result;
  }

  public static <SourceT, TargetT> Async<TargetT> select(Async<SourceT> async, final Function<? super SourceT, Async<TargetT>> f) {
    final SimpleAsync<TargetT> result = new SimpleAsync<>();
    async.onResult(new Handler<SourceT>() {
      @Override
      public void handle(SourceT item) {
        Async<TargetT> async;
        try {
          async = f.apply(item);
        } catch (Exception e) {
          result.failure(e);
          return;
        }
        if (async == null) {
          result.success(null);
        } else {
          delegate(async, result);
        }
      }
    }, new Handler<Throwable>() {
      @Override
      public void handle(Throwable item) {
        result.failure(item);
      }
    });
    return result;
  }

  public static <FirstT, SecondT> Async<SecondT> seq(Async<FirstT> first, final Async<SecondT> second) {
    return select(first, new Function<FirstT, Async<SecondT>>() {
      @Override
      public Async<SecondT> apply(FirstT input) {
        return second;
      }
    });
  }

  public static Async<Void> parallel(final Async<?>... asyncs) {
    return parallel(Arrays.asList(asyncs));
  }

  public static Async<Void> parallel(final Collection<? extends Async<?>> asyncs) {
    return parallel(asyncs, false);
  }

  public static Async<Void> parallel(Collection<? extends Async<?>> asyncs, final boolean alwaysSucceed) {
    final SimpleAsync<Void> result = new SimpleAsync<>();
    final Value<Integer> inProgress = new Value<>(asyncs.size());
    final List<Throwable> exceptions = new ArrayList<>();

    final Runnable checkTermination = new Runnable() {
      @Override
      public void run() {
        if (inProgress.get() == 0) {
          if (!exceptions.isEmpty() && !alwaysSucceed) {
            result.failure(new ThrowableCollectionException(exceptions));
          } else {
            result.success(null);
          }
        }
      }
    };

    for (Async<?> a : asyncs) {
      a.onResult(new Handler<Object>() {
        @Override
        public void handle(Object item) {
          inProgress.set(inProgress.get() - 1);
          checkTermination.run();
        }
      }, new Handler<Throwable>() {
        @Override
        public void handle(Throwable item) {
          exceptions.add(item);
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
      async.onResult(new Handler<ItemT>() {
        @Override
        public void handle(ItemT item) {
          succeeded.put(counter, item);
          inProgress.set(inProgress.get() - 1);
          checkTermination.run();
        }
      }, new Handler<Throwable>() {
        @Override
        public void handle(Throwable item) {
          exceptions.add(item);
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
    try {
      async = s.get();
    } catch (Exception e) {
      untilSuccess(s).onSuccess(new Handler<ResultT>() {
        @Override
        public void handle(ResultT item) {
          result.success(item);
        }
      });
      return result;
    }

    async.onResult(new Handler<ResultT>() {
      @Override
      public void handle(ResultT item) {
        result.success(item);
      }
    }, new Handler<Throwable>() {
      @Override
      public void handle(Throwable item) {
        untilSuccess(s).onSuccess(new Handler<ResultT>() {
          @Override
          public void handle(ResultT item) {
            result.success(item);
          }
        });
      }
    });
    return result;
  }

  public static <ValueT> Registration delegate(Async<? extends ValueT> from, final SimpleAsync<? super ValueT> to) {
    return from.onResult(new Handler<ValueT>() {
      @Override
      public void handle(ValueT item) {
        to.success(item);
      }
    }, new Handler<Throwable>() {
      @Override
      public void handle(Throwable item) {
        to.failure(item);
      }
    });
  }

  public static <FirstT, SecondT> Async<Pair<FirstT, SecondT>> pair(final Async<FirstT> first, Async<SecondT> second) {
    final SimpleAsync<Pair<FirstT, SecondT>> res = new SimpleAsync<>();
    SimpleAsync<Void> proxy = new SimpleAsync<>();
    final PairedAsync<FirstT> firstPaired = new PairedAsync<>(first);
    final PairedAsync<SecondT> secondPaired = new PairedAsync<>(second);
    proxy.onResult(new Handler<Void>() {
      @Override
      public void handle(Void item) {
        if (firstPaired.mySucceeded && secondPaired.mySucceeded) {
          res.success(new Pair<>(firstPaired.myItem, secondPaired.myItem));
        } else {
          res.failure(new Throwable("internal error in pair async"));
        }
      }
    }, new Handler<Throwable>() {
      @Override
      public void handle(Throwable item) {
        res.failure(item);
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
    async.onResult(new Handler<ResultT>() {
      @Override
      public void handle(ResultT item) {
        result.set(item);
        latch.countDown();
      }
    }, new Handler<Throwable>() {
      @Override
      public void handle(Throwable item) {
        error.set(item);
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
      throw new RuntimeException(error.get());
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
      myReg = myAsync.onResult(new Handler<ItemT>() {
        @Override
        public void handle(ItemT item) {
          myItem = item;
          mySucceeded = true;
          if (anotherInfo.mySucceeded) {
            async.success(null);
          }
        }
      }, new Handler<Throwable>() {
        @Override
        public void handle(Throwable item) {
          //reg == null can happen in case if myAsync fails instantly
          if (anotherInfo.myReg != null) {
            anotherInfo.myReg.remove();
          }
          async.failure(item);
        }
      });
    }
  }
}