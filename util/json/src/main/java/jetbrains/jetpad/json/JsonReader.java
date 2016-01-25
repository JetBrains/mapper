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

import java.io.IOException;
import java.io.InputStream;

public class JsonReader {
  private JsonLexer myJsonLexer;

  public JsonReader(InputStream inputStream) {
    myJsonLexer = new InputStreamJsonLexer(inputStream);
  }

  public JsonObject readObject() throws IOException {
    JsonObject object;
    try {
      object = JsonParser.parseObject(myJsonLexer);
    } catch (RuntimeIOException e) {
      throw e.getOrigin();
    }
    return object;
  }

  public JsonArray readArray() throws IOException {
    JsonArray array;
    try {
      array = JsonParser.parseArray(myJsonLexer);
    } catch (RuntimeIOException e) {
      throw e.getOrigin();
    }
    return array;
  }
}