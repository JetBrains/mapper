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

import java.util.logging.Logger;

public final class Json {
  private static final Logger LOG = Logger.getLogger(Json.class.getName());

  private static JsonSupport ourJsonSupport = new DefaultJsonSupport();

  public static JsonValue parse(String input) {
    return ourJsonSupport.parse(input);
  }

  public static String toString(JsonValue value) {
    return ourJsonSupport.toString(value);
  }

  public static void setJsonSupport(JsonSupport parser) {
    ourJsonSupport = parser;
    LOG.info("Set Json Parser to " + parser);
  }

  private Json() {
  }
}