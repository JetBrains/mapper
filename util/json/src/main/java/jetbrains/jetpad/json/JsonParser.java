/*
 * Copyright 2012-2014 JetBrains s.r.o
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

public class JsonParser {
  public JsonParser() {
  }

  public static JsonValue parse(String input) {
    JsonLexer lexer = new StringJsonLexer(input);
    JsonValue result = parseValue(lexer);
    if (lexer.tokenKind() != null) {
      throw new JsonParsingException();
    }
    return result;
  }

  private static JsonValue parseValue(JsonLexer lexer) {
    if (lexer.tokenKind() == JsonTokenKind.STRING) {
      String value = lexer.tokenText();
      lexer.next();
      return new JsonString(getLiteralText(value));
    }

    if (lexer.tokenKind() == JsonTokenKind.LEFT_BRACKET) {
      return parseArray(lexer);
    }

    if (lexer.tokenKind() == JsonTokenKind.NUMBER) {
      double value = Double.parseDouble(lexer.tokenText());
      lexer.next();
      return new JsonNumber(value);
    }

    if (lexer.tokenKind() == JsonTokenKind.LEFT_BRACE) {
      return parseObject(lexer);
    }

    if (lexer.tokenKind() == JsonTokenKind.NULL) {
      lexer.next();
      return new JsonNull();
    }

    if (lexer.tokenKind() == JsonTokenKind.TRUE) {
      lexer.next();
      return new JsonBoolean(true);
    }

    if (lexer.tokenKind() == JsonTokenKind.FALSE) {
      lexer.next();
      return new JsonBoolean(false);
    }

    throw new JsonParsingException();
  }

  static JsonObject parseObject(JsonLexer lexer) {
    JsonObject result = new JsonObject();
    lexer.readToken(JsonTokenKind.LEFT_BRACE);

    while (lexer.tokenKind() != JsonTokenKind.RIGHT_BRACE) {
      if (result.getKeys().size() > 0) {
        lexer.readToken(JsonTokenKind.COMMA);
      }

      if (lexer.tokenKind() != JsonTokenKind.STRING) {
        throw new JsonParsingException();
      }
      String key = getLiteralText(lexer.tokenText());
      lexer.next();

      lexer.readToken(JsonTokenKind.COLON);

      result.put(key, parseValue(lexer));
    }


    lexer.readToken(JsonTokenKind.RIGHT_BRACE);
    return result;
  }

  static JsonArray parseArray(JsonLexer lexer) {
    JsonArray result = new JsonArray();
    lexer.readToken(JsonTokenKind.LEFT_BRACKET);

    while (lexer.tokenKind() != JsonTokenKind.RIGHT_BRACKET) {
      if (result.size() > 0) {
        lexer.readToken(JsonTokenKind.COMMA);
      }
      result.add(parseValue(lexer));
    }

    lexer.readToken(JsonTokenKind.RIGHT_BRACKET);
    return result;
  }

  private static String getLiteralText(String literal) {
    return JsonUtil.unescape(literal.substring(1, literal.length() - 1));
  }
}