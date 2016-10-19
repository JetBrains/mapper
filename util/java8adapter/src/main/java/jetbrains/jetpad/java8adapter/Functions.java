package jetbrains.jetpad.java8adapter;


import java.util.function.Predicate;

public final class Functions {
  private static final Predicate<?> TRUE_PREDICATE = t -> true;
  private static final Predicate<?> FALSE_PREDICATE = t -> false;

  private Functions() { }

  @SuppressWarnings("unchecked")
  public static <ArgT> Predicate<ArgT> alwaysTrue() {
    return (Predicate<ArgT>) TRUE_PREDICATE;
  }

  @SuppressWarnings("unchecked")
  public static <ArgT> Predicate<ArgT> alwaysFalse() {
    return (Predicate<ArgT>) FALSE_PREDICATE;
  }
}