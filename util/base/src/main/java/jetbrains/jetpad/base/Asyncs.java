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
package jetbrains.jetpad.base;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Function;
import com.google.common.base.Supplier;

import java.util.*;
import java.util.concurrent.CountDownLatch;

@GwtCompatible
public class Asyncs {
  public static boolean isLoaded(Async<?> async) {
    final Value<Boolean> loaded = new Value<>(false);
    async.onSuccess(new Handler<Object>() {
      @Override
      public void handle(Object item) {
        loaded.set(true);
      }
    });
    return loaded.get();
  }

  public static <ValueT> Async<ValueT> constant(ValueT val) {
    return new InstantAsync<>(val);
  }

  public static <ValueT> Async<ValueT> failure(Throwable t) {
    return new FailureAsync<>(t);
  }

  public static <ResultT> Async<Void> toVoid(Async<ResultT> a) {
    return map(a, new Function<ResultT, Void>() {
      @Override
      public Void apply(ResultT input) {
        return null;
      }
    });
  }

  public static <SourceT, TargetT> Async<TargetT> map(Async<SourceT> async, final Function<SourceT, TargetT> f) {
    final SimpleAsync<TargetT> result = new SimpleAsync<>();
    async
      .onSuccess(new Handler<SourceT>() {
        @Override
        public void handle(SourceT item) {
          result.success(f.apply(item));
        }
      })
      .onFailure(new Handler<Throwable>() {
        @Override
        public void handle(Throwable item) {
          result.failure(item);
        }
      });
    return result;
  }

  public static <SourceT, TargetT> Async<TargetT> select(Async<SourceT> async, final Function<SourceT, Async<TargetT>> f) {
    final SimpleAsync<TargetT> result = new SimpleAsync<>();
    async
      .onSuccess(new Handler<SourceT>() {
        @Override
        public void handle(SourceT item) {
          Async<TargetT> async = f.apply(item);
          if (async == null) {
            result.success(null);
          } else {
            async
              .onSuccess(new Handler<TargetT>() {
                @Override
                public void handle(TargetT item) {
                  result.success(item);
                }
              })
              .onFailure(new Handler<Throwable>() {
                @Override
                public void handle(Throwable item) {
                  result.failure(item);
                }
              });
          }
        }
      })
      .onFailure(new Handler<Throwable>() {
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

  public static Async<Void> parallel(final Collection<? extends Async<?>> asyncs, final boolean alwaysSucceed) {
    final SimpleAsync<Void> result = new SimpleAsync<>();
    final Value<Integer> completed = new Value<>(0);
    final List<Throwable> exceptions = new ArrayList<>();

    final Runnable checkTermination = new Runnable() {
      @Override
      public void run() {
        if (completed.get() == asyncs.size()) {
          if (!exceptions.isEmpty() && !alwaysSucceed) {
            result.failure(new ThrowableCollectionException(exceptions));
          } else {
            result.success(null);
          }
        }
      }
    };

    for (Async<?> a : asyncs) {
      a.onFailure(new Handler<Throwable>() {
        @Override
        public void handle(Throwable item) {
          completed.set(completed.get() + 1);
          exceptions.add(item);
          checkTermination.run();
        }
      });
      a.onSuccess(new Handler<Object>() {
        @Override
        public void handle(Object item) {
          completed.set(completed.get() + 1);
          checkTermination.run();
        }
      });
    }
    return result;
  }

  public static <ResultT> Async<ResultT> untilSuccess(final Supplier<Async<ResultT>> s) {
    final SimpleAsync<ResultT> result = new SimpleAsync<>();
    Async<ResultT> async = s.get();
    async.onSuccess(new Handler<ResultT>() {
      @Override
      public void handle(ResultT item) {
        result.success(item);
      }
    }).onFailure(new Handler<Throwable>() {
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

  @GwtIncompatible("Uses threading primitives")
  public static <ResultT> ResultT get(Async<ResultT> async) {
    try {
      final CountDownLatch latch = new CountDownLatch(1);

      final Value<ResultT> result = new Value<>();
      final Value<Throwable> error = new Value<>();

      async.onSuccess(new Handler<ResultT>() {
        @Override
        public void handle(ResultT item) {
          result.set(item);
          latch.countDown();
        }
      }).onFailure(new Handler<Throwable>() {
        @Override
        public void handle(Throwable item) {
          error.set(item);
          latch.countDown();
        }
      });

      latch.await();

      if (error.get() != null) {
        throw new RuntimeException(error.get());
      } else {
        return result.get();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private static class InstantAsync<ValueT> implements Async<ValueT> {
    final ValueT myValue;

    public InstantAsync(ValueT value) {
      myValue = value;
    }

    public Async<ValueT> onSuccess(Handler<? super ValueT> successHandler) {
      successHandler.handle(myValue);
      return this;
    }

    public Async<ValueT> onFailure(Handler<Throwable> failureHandler) {
      return this;
    }

    public Registration handle(Handler<? super ValueT> successHandler) {
      successHandler.handle(myValue);
      return Registration.EMPTY;
    }

    public Registration handle(Handler<? super ValueT> successHandler, Handler<Throwable> failureHandler) {
      return handle(successHandler);
    }

    public Registration handleFailure(Handler<Throwable> failureHandler) {
      return Registration.EMPTY;
    }
  }

  private static class FailureAsync<ValueT> implements Async<ValueT> {
    final Throwable myThrowable;

    public FailureAsync(Throwable throwable) {
      myThrowable = throwable;
    }

    public Async<ValueT> onSuccess(Handler<? super ValueT> successHandler) {
      return this;
    }

    public Async<ValueT> onFailure(Handler<Throwable> failureHandler) {
      failureHandler.handle(myThrowable);
      return this;
    }

    public Registration handle(Handler<? super ValueT> successHandler) {
      return Registration.EMPTY;
    }

    public Registration handle(Handler<? super ValueT> successHandler, Handler<Throwable> failureHandler) {
      return handleFailure(failureHandler);
    }

    public Registration handleFailure(Handler<Throwable> failureHandler) {
      failureHandler.handle(myThrowable);
      return Registration.EMPTY;
    }
  }
}