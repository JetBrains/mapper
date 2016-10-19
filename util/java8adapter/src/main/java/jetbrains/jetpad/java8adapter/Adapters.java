package jetbrains.jetpad.java8adapter;

import jetbrains.jetpad.base.function.Consumer;
import jetbrains.jetpad.base.function.Function;
import jetbrains.jetpad.base.function.Predicate;

public class Adapters {
  public static <ValueT> Predicate<ValueT> adapter(java.util.function.Predicate<ValueT> predicate) {
    return predicate::test;
  }

  public static <ValueT> java.util.function.Predicate<ValueT> adapter(Predicate<ValueT> predicate) {
    return predicate::test;
  }

  public static <ValueT> Consumer<ValueT> adapter(java.util.function.Consumer<ValueT> consumer) {
    return consumer::accept;
  }

  public static <ValueT> java.util.function.Consumer<ValueT> adapter(Consumer<ValueT> consumer) {
    return consumer::accept;
  }

  public static <ValueT, ResultT> Function<ValueT, ResultT> adapter(java.util.function.Function<ValueT, ResultT> f) {
    return f::apply;
  }

  public static <ValueT, ResultT> java.util.function.Function<ValueT, ResultT> adapter(Function<ValueT, ResultT> f) {
    return f::apply;
  }
}
