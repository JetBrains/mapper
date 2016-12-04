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
    }  }

  private JsonValue parseValue(JsonLexer lexer) {
    switch (lexer.tokenKind()) {
      case STRING:
        String sv = lexer.tokenText();
        lexer.next();
        return new JsonString(getLiteralText(sv));
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

  static String getLiteralText(String literal) {
    return JsonUtil.unescape(literal.substring(1, literal.length() - 1));
  }
}
