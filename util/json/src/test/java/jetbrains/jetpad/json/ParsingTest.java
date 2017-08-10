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

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParsingTest {

  @Test
  public void emptyObject() {
    JsonObject object = (JsonObject) Json.parse("{}");
    assertTrue(object.getKeys().isEmpty());
  }

  @Test(expected = JsonParsingException.class)
  public void extraSymbols() {
    Json.parse("{} {");
  }

  @Test(expected = JsonParsingException.class)
  public void unknownSymbols() {
    Json.parse("{} @");
  }

  @Test
  public void emptyArray() {
    JsonArray array = (JsonArray) Json.parse("[]");

    assertTrue(array.size() == 0);
  }

  @Test
  public void nullValue() {
    assertTrue(Json.parse("null") instanceof JsonNull);
  }

  @Test
  public void trueValue() {
    JsonBoolean trueValue = (JsonBoolean) Json.parse("true");

    assertTrue(trueValue.getBooleanValue());
  }

  @Test
  public void falseValue() {
    JsonBoolean falseValue = (JsonBoolean) Json.parse("false");

    Assert.assertFalse(falseValue.getBooleanValue());
  }

  @Test
  public void multiElementArray() {
    JsonArray array = (JsonArray) Json.parse("[true, false]");

    assertEquals(2, array.size());
  }

  @Test
  public void simpleNumber() {
    JsonNumber n = (JsonNumber) Json.parse("239");

    assertEquals(239.0, n.getDoubleValue(), 0.000001f);
  }

  @Test
  public void simpleNegativeNumber() {
    JsonNumber n = (JsonNumber) Json.parse("-239");

    assertEquals(-239.0, n.getDoubleValue(), 0.000001f);
  }

  @Test
  public void simpleNumberWithDot() {
    JsonNumber n = (JsonNumber) Json.parse("2.39");

    assertEquals(2.39, n.getDoubleValue(), 0.000001f);
  }

  @Test
  public void numberWithExponent() {
    JsonNumber n = (JsonNumber) Json.parse("2e1");

    assertEquals(20.0, n.getDoubleValue(), 0.000001f);
  }

  @Test
  public void fractionalNumberWithExponent() {
    JsonNumber n = (JsonNumber) Json.parse("2.39e1");

    assertEquals(23.9, n.getDoubleValue(), 0.000001f);
  }

  @Test
  public void simpleString() {
    JsonString s = (JsonString) Json.parse("\"abc\"");

    assertEquals("abc", s.getStringValue());
  }

  @Test
  public void simpleEscapedChars() {
    JsonString s = (JsonString) Json.parse("\" \\\\ \\n \\f \\t \\b \\r \"");

    assertEquals(" \\ \n \f \t \b \r ", s.getStringValue());
  }

  @Test
  public void unicodeEscapeSequence() {
    JsonString s = (JsonString) Json.parse("\"\\u000aabc\"");

    assertEquals("\nabc", s.getStringValue());
  }

  @Test
  public void objectParsing() {
    JsonObject obj = (JsonObject) Json.parse("{\"a\":239}");
    JsonNumber number = (JsonNumber) obj.get("a");

    assertEquals(239.0, number.getDoubleValue(), 0.000001f);
  }

  @Test(expected = JsonParsingException.class)
  public void newLineInString() {
    Json.parse("\"a\nb\"");
  }

  @Test(expected = JsonParsingException.class)
  public void tabInString() {
    Json.parse("\"a\tb\"");
  }

  @Test
  public void forwardSlash() {
    // By spec both escaped and non-escaped forms are valid
    JsonString value = (JsonString) Json.parse("\" / \\/ \"");
    assertEquals(" / / ", value.getStringValue());
  }

}