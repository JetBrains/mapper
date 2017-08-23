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

public class DefautJsonSupport implements JsonSupport {
  public DefautJsonSupport() {
  }

  @Override
  public JsonValue parse(String input) {
    try {
      JsonLexer lexer = new StringJsonLexer(input);
      JsonValue result = parseValue(lexer);
      if (lexer.tokenKind() != null) {
        throw new JsonParsingException();
      }
      return result;
    } catch (RuntimeException e) {
      throw new JsonParsingException("Failed to parse json=" + input, e);
    }
  }

  @Override
  public String toString(JsonValue value) {
    return value.toString();
  }

  private JsonValue parseValue(JsonLexer lexer) {
    switch (lexer.tokenKind()) {
      case STRING:
        String sv = lexer.literalTokenText();
        lexer.next();
        return new JsonString(sv);
      case LEFT_BRACKET:
        return parseArray(lexer);
      case NUMBER:
        double dv = Double.parseDouble(lexer.tokenText());
        lexer.next();
        return new JsonNumber(dv);
      case LEFT_BRACE:
        return parseObject(lexer);
      case NULL:
        lexer.next();
        return new JsonNull();
      case TRUE:
        lexer.next();
        return new JsonBoolean(true);
      case FALSE:
        lexer.next();
        return new JsonBoolean(false);
    }


    throw new JsonParsingException();
  }

  JsonObject parseObject(JsonLexer lexer) {
    JsonObject result = new JsonObject();
    lexer.readToken(JsonTokenKind.LEFT_BRACE);

    while (lexer.tokenKind() != JsonTokenKind.RIGHT_BRACE) {
      if (!result.getKeys().isEmpty()) {
        lexer.readToken(JsonTokenKind.COMMA);
      }

      if (lexer.tokenKind() != JsonTokenKind.STRING) {
        throw new JsonParsingException();
      }
      String key = lexer.literalTokenText();
      lexer.next();

      lexer.readToken(JsonTokenKind.COLON);

      result.put(key, parseValue(lexer));
    }


    lexer.readToken(JsonTokenKind.RIGHT_BRACE);
    return result;
  }

  JsonArray parseArray(JsonLexer lexer) {
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
}