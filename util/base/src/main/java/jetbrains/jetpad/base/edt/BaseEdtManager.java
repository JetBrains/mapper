/*
 * Copyright 2012-2015 JetBrains s.r.o
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
package jetbrains.jetpad.base.edt;

abstract class BaseEdtManager implements EventDispatchThreadManager, EventDispatchThread {
  private final String myName;

  BaseEdtManager(String name) {
    myName = name;
  }

  @Override
  public final EventDispatchThread getEDT() {
    return this;
  }

  public String getName() {
    return myName;
  }

  protected String wrapMessage(String message) {
    return this + ": " + message;
  }

  @Override
  public String toString() {
    String name = getClass().getName();
    int dotIndex = name.lastIndexOf('.');
    String className = dotIndex == 1 ? name : name.substring(dotIndex + 1);
    return className + "@" + Integer.toHexString(hashCode()) + ("".equals(myName) ? "" : " (" + getName() + ")");
  }

}
