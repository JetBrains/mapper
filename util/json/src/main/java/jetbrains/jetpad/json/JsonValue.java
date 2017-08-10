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
package jetbrains.jetpad.json;

public abstract class JsonValue {
  public JsonValue() {
  }

  public String toString(int indent) {
    IndentBuilder builder = new IndentBuilder(indent);
    toString(builder);
    return builder.toString();
  }

  protected abstract void toString(IndentBuilder builder);

  @Override
  public String toString() {
    return toString(0);
  }

  public String toPrettyString() {
    return toString(2);
  }
}