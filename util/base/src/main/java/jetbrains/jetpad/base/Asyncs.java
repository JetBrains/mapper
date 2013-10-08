package jetbrains.jetpad.base;

import com.google.common.base.Function;

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

}
