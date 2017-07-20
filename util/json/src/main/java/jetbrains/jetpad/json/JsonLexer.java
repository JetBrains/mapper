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

abstract class JsonLexer {
  private JsonTokenKind myTokenKind;
  private final boolean myIsExhaustive;
  private boolean myDeferred;

  JsonLexer(boolean exhaustive) {
    myIsExhaustive = exhaustive;
    myDeferred = !exhaustive;
  }

  protected abstract String tokenText();

  protected abstract String literalTokenText();

  JsonTokenKind tokenKind() {
    if (myDeferred) {
      myDeferred = false;
      next();
    }
    return myTokenKind;
  }

  protected abstract void setTokenStart();

  void next() {
    skipSpaces();

    if (getCurrent() == -1) {
      myTokenKind = null;
      return;
    }

    setTokenStart();

    if (getCurrent() == '"') {
      readString();
      myTokenKind = JsonTokenKind.STRING;
      return;
    }

    if (getCurrent() == ',') {
      myTokenKind = JsonTokenKind.COMMA;
      advance();
      return;
    }

    if (getCurrent() == '[') {
      myTokenKind = JsonTokenKind.LEFT_BRACKET;
      advance();
      return;
    }

    if (getCurrent() == ']') {
      myTokenKind = JsonTokenKind.RIGHT_BRACKET;
      advance();
      return;
    }

    if (isDigit(getCurrent()) || getCurrent() == '-') {
      readNumber();
      myTokenKind = JsonTokenKind.NUMBER;
      return;
    }

    if (getCurrent() == '{') {
      myTokenKind = JsonTokenKind.LEFT_BRACE;
      advance();
      return;
    }
    if (getCurrent() == '}') {
      myTokenKind = JsonTokenKind.RIGHT_BRACE;
      advance();
      return;
    }

    if (getCurrent() == ':') {
      myTokenKind = JsonTokenKind.COLON;
      advance();
      return;
    }

    if (readString("null")) {
      myTokenKind = JsonTokenKind.NULL;
      return;
    }
    if (readString("true")) {
      myTokenKind = JsonTokenKind.TRUE;
      return;
    }
    if (readString("false")) {
      myTokenKind = JsonTokenKind.FALSE;
      return;
    }

    throw new JsonParsingException();
  }

  private void readNumber() {
    if (getCurrent() == '-') {
      advance();
    }

    while (isDigit(getCurrent())) {
      advance();
    }

    if (getCurrent() == '.') {
      advance();
      if (!(isDigit(getCurrent()))) {
        throw new JsonParsingException();
      }
      while (isDigit(getCurrent())) {
        advance();
      }
    }

    if (readExponent()) {
      if (!(isDigit(getCurrent()))) {
        throw new JsonParsingException();
      }
      advance();

      while (isDigit(getCurrent())) {
        advance();
      }
    }
  }

  private boolean readExponent() {
    if (getCurrent() == 'E' || getCurrent() == 'e') {
      advance();
      if (getCurrent() == '+' || getCurrent() == '-') {
        advance();
      }
      return true;
    }
    return false;
  }

  private void readString() {
    if (!(readString("\""))) {
      throw new JsonParsingException();
    }

    while (getCurrent() != '"' && getCurrent() != -1) {
      if (getCurrent() == '\\') {
        advance();
        if (JsonUtil.isUnescapedSpecialChar((char) getCurrent())) {
          advance();
        } else if (getCurrent() == 'u') {
          advance();
          for (int i = 0; i < 4; i++) {
            if (!(isHexDigit(getCurrent()))) {
              throw new JsonParsingException();
            }
            advance();
          }
        } else {
          throw new RuntimeException();
        }
      } else if (JsonUtil.isControlChar((char) getCurrent())) {
        throw new JsonParsingException();
      } else {
        advance();
      }
    }

    if (!(readString("\""))) {
      throw new JsonParsingException();
    }
  }

  protected abstract boolean readString(String s);

  protected abstract void advance();

  private void skipSpaces() {
    while (getCurrent() != -1 && isWhiteSpace(getCurrent())) {
      advance();
    }
  }

  void readToken(JsonTokenKind kind) {
    if (myDeferred) {
      myDeferred = false;
      next();
    }
    if (myTokenKind != kind) {
      throw new JsonParsingException();
    }
    if (myIsExhaustive) {
      next();
    } else {
      myDeferred = true;
    }
  }

  private boolean isWhiteSpace(int ch) {
    return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';
  }

  private boolean isDigit(int ch) {
    return ch >= '0' && ch <= '9';
  }

  private boolean isHexDigit(int ch) {
    return isDigit(ch) || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F');
  }

  protected abstract int getCurrent();
}