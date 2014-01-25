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

import com.google.common.base.Function;
import com.google.common.base.Supplier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Asyncs {

  public static <ValueT> Async<ValueT> constant(ValueT val) {
    final SimpleAsync<ValueT> result = new SimpleAsync<ValueT>();
    result.success(val);
    return result;
  }

  public static <ValueT> Async<ValueT> failure(Throwable t) {
    final SimpleAsync<ValueT> result = new SimpleAsync<ValueT>();
    result.failure(t);
    return result;
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
    final SimpleAsync<TargetT> result = new SimpleAsync<TargetT>();
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
    final SimpleAsync<TargetT> result = new SimpleAsync<TargetT>();
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
    final SimpleAsync<Void> result = new SimpleAsync<Void>();
    final Value<Integer> completed = new Value<Integer>(0);
    final List<Throwable> exceptions = new ArrayList<Throwable>();

    final Runnable checkTermination = new Runnable() {
      @Override
      public void run() {
        if (completed.get() == asyncs.size()) {
          if (!exceptions.isEmpty()) {
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
    final SimpleAsync<ResultT> result = new SimpleAsync<ResultT>();
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
}