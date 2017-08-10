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