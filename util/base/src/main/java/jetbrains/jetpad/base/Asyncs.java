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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import jetbrains.jetpad.base.function.Consumer;
import jetbrains.jetpad.base.function.Function;
import jetbrains.jetpad.base.function.Supplier;

@GwtCompatible
public class Asyncs {
  public static boolean isSucceeded(Async<?> async) {
    final Value<Boolean> succeeded = new Value<>(false);
    async.onSuccess(new Consumer<Object>() {
      @Override
      public void accept(Object value) {
        succeeded.set(true);
      }
    }).remove();
    return succeeded.get();
  }

  public static boolean isFailed(Async<?> async) {
    final Value<Boolean> failed = new Value<>(false);
    async.onFailure(new Consumer<Throwable>() {
      @Override
      public void accept(Throwable t) {
        failed.set(true);
      }
    }).remove();
    return failed.get();
  }

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
        return Asyncs.map(this, success);
      }

      @Override
      public <ResultT> Async<ResultT> flatMap(Function<? super ValueT, Async<ResultT>> success) {
        return Asyncs.select(this, success);
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
        return Asyncs.map(this, success);
      }

      @Override
      public <ResultT> Async<ResultT> flatMap(Function<? super ValueT, Async<ResultT>> success) {
        return Asyncs.select(this, success);
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

  @Deprecated
  public static <SourceT, TargetT, AsyncResultT extends SourceT> Async<TargetT> map(Async<AsyncResultT> async, final Function<SourceT, ? extends TargetT> f) {
    final SimpleAsync<TargetT> result = new SimpleAsync<>();
    async.onResult(new Consumer<AsyncResultT>() {
        @Override
        public void accept(AsyncResultT item) {
          TargetT apply;
          try {
            apply = f.apply(item);
          } catch (Exception e) {
            result.failure(e);
            return;
          }
          result.success(apply);
        }
      },
      new Consumer<Throwable>() {
        @Override
        public void accept(Throwable throwable) {
          result.failure(throwable);
        }
      });
    return result;
  }

  @Deprecated
  public static <SourceT, TargetT> Async<TargetT> select(Async<SourceT> async, final Function<? super SourceT, Async<TargetT>> f) {
    final SimpleAsync<TargetT> result = new SimpleAsync<>();
    async.onResult(new Consumer<SourceT>() {
        @Override
        public void accept(SourceT item) {
          Async<TargetT> async1;
          try {
            async1 = f.apply(item);
          } catch (Exception e) {
            result.failure(e);
            return;
          }
          if (async1 == null) {
            result.success(null);
          } else {
            delegate(async1, result);
          }
        }
      },
      new Consumer<Throwable>() {
        @Override
        public void accept(Throwable throwable) {
          result.failure(throwable);
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
      a.onResult(new Consumer<Object>() {
          @Override
          public void accept(Object item) {
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

  public static <ValueT> Registration delegate(Async<? extends ValueT> from, final SimpleAsync<? super ValueT> to) {
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
}
