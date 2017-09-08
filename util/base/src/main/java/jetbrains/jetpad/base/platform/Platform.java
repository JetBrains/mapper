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
package jetbrains.jetpad.base.platform;

public final class Platform {
  private static final ThreadLocal<PlatformType> PLATFORM = new ThreadLocal<PlatformType>();

  public static PlatformType getPlatform() {
    PlatformType platform = PLATFORM.get();
    if (platform == null) {
      throw new IllegalStateException("Platform was not set");
    }
    return platform;
  }

  public static void setPlatform(PlatformType platform) {
    if (platform == null) {
      throw new IllegalArgumentException("Null new platform value");
    }
    PlatformType oldValue = PLATFORM.get();
    if (oldValue != null && oldValue != platform) {
      throw new IllegalStateException("Overwrite platform value: current=" + oldValue + ", new=" + platform);
    }
    PLATFORM.set(platform);
  }

  static void reset() {
    PLATFORM.set(null);
  }

  private Platform() {
  }
}
