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
package jetbrains.jetpad.base.props;

public final class AppProperties {
  private static PropertyProvider ourPropertyProvider = new EmptyPropertyProvider();

  public static void setProvider(PropertyProvider provider) {
    ourPropertyProvider = provider;
  }

  public static String get(String key, String defaultValue) {
    String value = get(key);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  public static String get(String key) {
    return ourPropertyProvider.get(key);
  }

  private AppProperties() {
  }
}