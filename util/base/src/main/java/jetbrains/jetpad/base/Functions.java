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

import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Functions {

  private static final Predicate<?> FALSE_PREDICATE = t -> false;
  private static final Predicate<?> TRUE_PREDICATE = t -> true;

  private Functions() { }

  public static <T> Supplier<T> constantSupplier(T value) {
    return () -> value;
  }

  public static <T> Supplier<T> memorize(Supplier<T> supplier) {
    return new Memo<>(supplier);
  }

  @SuppressWarnings("unchecked")
  public static <T> Predicate<T> alwaysTrue() {
    return (Predicate<T>) TRUE_PREDICATE;
  }

  @SuppressWarnings("unchecked")
  public static <T> Predicate<T> alwaysFalse() {
    return (Predicate<T>) FALSE_PREDICATE;
  }

  private static class Memo<T> implements Supplier<T> {
    private final Supplier<T> mySupplier;
    private T myCachedValue = null;
    private boolean myCached = false;

    public Memo(Supplier<T> supplier) {
      mySupplier = supplier;
    }

    @Override
    public T get() {
      if (!myCached) {
        myCachedValue = mySupplier.get();
        myCached = true;
      }
      return myCachedValue;
    }
  }
}