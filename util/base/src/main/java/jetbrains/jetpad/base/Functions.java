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

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Functions {

  private static final Predicate<?> TRUE_PREDICATE = t -> true;
  private static final Predicate<?> FALSE_PREDICATE = t -> false;

  private Functions() { }

  public static <ItemT> Supplier<ItemT> constantSupplier(ItemT value) {
    return () -> value;
  }

  public static <ItemT> Supplier<ItemT> memorize(Supplier<ItemT> supplier) {
    return new Memo<>(supplier);
  }

  @SuppressWarnings("unchecked")
  public static <ArgT> Predicate<ArgT> alwaysTrue() {
    return (Predicate<ArgT>) TRUE_PREDICATE;
  }

  @SuppressWarnings("unchecked")
  public static <ArgT> Predicate<ArgT> alwaysFalse() {
    return (Predicate<ArgT>) FALSE_PREDICATE;
  }

  public static <ArgT, ResultT> Function<ArgT, ResultT> constant(ResultT result) {
    return a -> result;
  }

  private static class Memo<ItemT> implements Supplier<ItemT> {
    private final Supplier<ItemT> mySupplier;
    private ItemT myCachedValue = null;
    private boolean myCached = false;

    public Memo(Supplier<ItemT> supplier) {
      mySupplier = supplier;
    }

    @Override
    public ItemT get() {
      if (!myCached) {
        myCachedValue = mySupplier.get();
        myCached = true;
      }
      return myCachedValue;
    }
  }
}