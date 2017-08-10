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
package jetbrains.jetpad.model.property;

/**
 * implementations of {@link ReadableProperty#get()} method shouldn't return null
 */
public interface TextProperty extends ReadableProperty<String> {
  /**
   * @throws IllegalArgumentException in the following cases:
   *         {@param text} is null
   *         {@param index} is negative
   *         {@param index} is greater than property text length
   */
  void insert(int index, String text);

  /**
   * @throws IllegalArgumentException in the following cases:
   *         {@param index} is negative
   *         {@param length} is negative
   *         {@param index} + {@param length} is greater than property text length
   */
  void delete(int index, int length);
}