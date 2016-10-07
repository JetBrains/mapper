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

import com.google.common.base.Strings;

import java.util.function.Predicate;

public class Validators {
  private static final Predicate<String> IDENTIFIER = input -> {
    if (input == null) return false;
    if (input.isEmpty()) return false;

    for (int i = 0; i < input.length(); i++) {
      char ch = input.charAt(i);
      if (i == 0 && Character.isDigit(ch)) return false;
      if (!Character.isLetter(ch) && !Character.isDigit(ch) && ch != '_') return false;
    }

    return true;
  };

  private static final Predicate<String> UNSINGED_INTEGER = input -> {
    if (Strings.isNullOrEmpty(input)) return false;
    for (int i = 0; i < input.length(); i++) {
      if (!Character.isDigit(input.charAt(i))) return false;
    }
    try {
      Integer.parseInt(input);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  };

  private static final Predicate<String> INTEGER = input -> {
    if (Strings.isNullOrEmpty(input)) return false;
    try {
      Integer.parseInt(input);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  };

  private static final Predicate<String> BOOL = input -> "true".equals(input) || "false".equals(input);

  public static Predicate<String> identifier() {
    return IDENTIFIER;
  }

  public static Predicate<String> unsignedInteger() {
    return UNSINGED_INTEGER;
  }

  public static Predicate<String> integer() {
    return INTEGER;
  }

  public static Predicate<String> bool() {
    return BOOL;
  }

  public static <T> Predicate<T> equalsTo(final T value) {
    return input -> Objects.equal(input, value);
  }
}