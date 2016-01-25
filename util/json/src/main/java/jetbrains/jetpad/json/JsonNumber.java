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
package jetbrains.jetpad.json;

public class JsonNumber extends JsonValue {
  private double myValue;

  public JsonNumber(double value) {
    myValue = value;
  }

  public int getIntValue() {
    return (int) myValue;
  }

  public long getLongValue() {
    return (long) myValue;
  }

  public double getDoubleValue() {
    return myValue;
  }

  protected void toString(IndentBuilder builder) {
    if (Math.floor(myValue) == myValue) {
      builder.append((long) myValue);
    } else {
      builder.append(myValue);
    }
  }
}