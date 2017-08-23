/*
 * Copyright 2012-2017 JetBrains s.r.o
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

import jetbrains.jetpad.base.function.Function;
import jetbrains.jetpad.base.function.Predicate;
import jetbrains.jetpad.base.function.Supplier;

public final class Functions {

  private static final Predicate<?> TRUE_PREDICATE = new Predicate<Object>() {
    @Override
    public boolean test(Object t) {
      return true;
    }
  };
  private static final Predicate<?> FALSE_PREDICATE = new Predicate<Object>() {
    @Override
    public boolean test(Object t) {
      return false;
    }
  };
  public static final Predicate<?> NULL_PREDICATE = new Predicate<Object>() {
    @Override
    public boolean test(Object value) {
      return value == null;
    }
  };
  public static final Predicate<?> NOT_NULL_PREDICATE = new Predicate<Object>() {
    @Override
    public boolean test(Object value) {
      return value != null;
    }
  };

  private Functions() { }

  public static <ItemT> Supplier<ItemT> constantSupplier(final ItemT value) {
    return new Supplier<ItemT>() {
      @Override
      public ItemT get() {
        return value;
      }
    };
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

  public static <ArgT, ResultT> Function<ArgT, ResultT> constant(final ResultT result) {
    return new Function<ArgT, ResultT>() {
      @Override
      public ResultT apply(ArgT a) {
        return result;
      }
    };
  }

  @SuppressWarnings("unchecked")
  public static <ArgT> Predicate<ArgT> isNull() {
    return (Predicate<ArgT>) NULL_PREDICATE;
  }

  @SuppressWarnings("unchecked")
  public static <ArgT> Predicate<ArgT> isNotNull() {
    return (Predicate<ArgT>) NOT_NULL_PREDICATE;
  }

  public static <ValueT> Function<ValueT, ValueT> identity() {
    return new Function<ValueT, ValueT>() {
      @Override
      public ValueT apply(ValueT value) {
        return value;
      }
    };
  }

  private static class Memo<ItemT> implements Supplier<ItemT> {
    private final Supplier<ItemT> mySupplier;
    private ItemT myCachedValue = null;
    private boolean myCached = false;

    Memo(Supplier<ItemT> supplier) {
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