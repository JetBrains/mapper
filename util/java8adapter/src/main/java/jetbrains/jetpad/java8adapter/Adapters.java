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
package jetbrains.jetpad.java8adapter;

import jetbrains.jetpad.base.function.Consumer;
import jetbrains.jetpad.base.function.Function;
import jetbrains.jetpad.base.function.Predicate;

public final class Adapters {
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

  private Adapters() {
  }
}