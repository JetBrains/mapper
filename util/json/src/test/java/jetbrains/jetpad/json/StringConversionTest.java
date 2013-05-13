/*
 * Copyright 2012-2013 JetBrains s.r.o
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

import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class StringConversionTest extends BaseTestCase {
  @Test
  public void emptyObject() {
    assertToString(new JsonObject(), "{}");
  }

  @Test
  public void number() {
    assertToString(new JsonNumber(23.9), "23.9");
  }

  @Test
  public void string() {
    assertToString(new JsonString("abc"), "\"abc\"");
  }

  @Test
  public void escape() {
    for (int i = 0; i < JsonUtil.SPECIAL_CHARS.size(); i++) {
      convertForthAndBack("" + JsonUtil.SPECIAL_CHARS.get(i), "\"" + JsonUtil.ESCAPED_SPECIAL_CHARS.get(i) + "\"");
    }
  }

  @Test
  public void unicodeChar() {
    String data = "\u0125";
    assertEquals(1, data.length());

    JsonString value = new JsonString(data);
    assertEquals(3, value.toString().length());
    JsonValue parsed = JsonParser.parse(value.toString());
    assertTrue(parsed instanceof JsonString);
    assertEquals(data, ((JsonString) parsed).getStringValue());
  }

  @Test
  public void unsecapeUnicode() {
    JsonValue parsed = JsonParser.parse("\"\\u0125\"");
    assertTrue(parsed instanceof JsonString);
    assertEquals(3, parsed.toString().length());
  }

  @Test
  public void emptyArray() {
    assertToString(new JsonArray(), "[]");
  }

  @Test
  public void nullValue() {
    assertToString(new JsonNull(), "null");
  }

  @Test
  public void boolValue() {
    assertToString(new JsonBoolean(false), "false");
  }

  @Test
  public void nonEmptyArray() {
    JsonArray array = new JsonArray();
    array.add(new JsonNumber(1));
    array.add(new JsonNumber(2));
    assertToString(array, "[1,2]");
  }

  @Test
  public void nonEmptyArrayWithIndent() {
    JsonArray array = new JsonArray();
    array.add(new JsonNumber(1));
    array.add(new JsonNumber(2));
    assertToStringWithIndent(array, "[\n  1,\n  2\n]");
  }

  @Test
  public void nonEmptyObject() {
    JsonObject obj = new JsonObject();
    obj.put("a", new JsonNumber(2));
    obj.put("b", new JsonNumber(3));
    assertToString(obj, "{\"a\":2,\"b\":3}");
  }

  @Test
  public void nonEmptyObjectWithIndent() {
    JsonObject obj = new JsonObject();
    obj.put("a", new JsonNumber(2));
    assertToStringWithIndent(obj, "{\n  \"a\":2\n}");
  }

  public void assertToString(JsonValue value, String stringValue) {
    assertEquals(stringValue, value.toString());
  }

  public void assertToStringWithIndent(JsonValue value, String stringValue) {
    assertEquals(stringValue, value.toString(2));
  }

  private void convertForthAndBack(String data, String expectedJson) {
    JsonString value = new JsonString(data);
    assertToString(value, expectedJson);
    JsonValue parsed = JsonParser.parse(expectedJson);
    assertTrue(parsed instanceof JsonString);
    assertEquals(data, ((JsonString) parsed).getStringValue());
  }
}
