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

public enum PlatformType {
  MAC("MacOS"),
  LINUX("Linux"),
  WINDOWS("Windows"),
  UNKNOWN("Unknown");

  private static final String WINDOWS_NAME = "win";
  private static final String LINUX_NAME = "linux";
  private static final String MAC_NAME = "mac";

  public static PlatformType fromName(String name) {
    String lowerCaseName = name.toLowerCase();
    if (lowerCaseName.contains(MAC_NAME)) {
      return MAC;
    }
    if (lowerCaseName.contains(WINDOWS_NAME)) {
      return WINDOWS;
    }
    if (lowerCaseName.contains(LINUX_NAME)) {
      return LINUX;
    }
    return UNKNOWN;
  }

  private String myName;

  PlatformType(String name) {
    myName = name;
  }

  public String getName() {
    return myName;
  }
}
