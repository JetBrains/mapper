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

import java.io.IOException;
import java.io.InputStream;

class InputStreamJsonLexer extends JsonLexer {
  private final InputStream myInputStream;
  private StringBuilder myTokenString = new StringBuilder();
  private int myCurrent;
  private boolean hasReadCurrent;

  InputStreamJsonLexer(InputStream input) {
    super(false);
    myInputStream = input;
  }

  @Override
  protected int getCurrent() {
    if (!hasReadCurrent) {
      myCurrent = readCurrent();
      hasReadCurrent = true;
    }
    return myCurrent;
  }

  @Override
  protected String tokenText() {
    return myTokenString.toString();
  }

  @Override
  protected String literalTokenText() {
    String tokenText = tokenText();
    return JsonUtil.unescape(tokenText, 1, tokenText.length());
  }

  @Override
  protected void setTokenStart() {
    myTokenString.delete(0, myTokenString.length());
  }

  private int readCurrent() {
    int read;
    try {
      read = myInputStream.read();
    } catch (IOException e) {
      throw new RuntimeIOException(e);
    }
    return read;
  }

  @Override
  protected boolean readString(String s) {
    if (isEndOfStream()) {
      return false;
    }
    int i = 0;
    char ch = (char) getCurrent();

    while (i < s.length() && !isEndOfStream()) {
      if (ch != s.charAt(i++)) {
        return false;
      }
      advance();
      ch = (char) getCurrent();
    }

    return i == s.length();
  }

  @Override
  protected void advance() {
    if (!isEndOfStream()) {
      myTokenString.append((char) getCurrent());
    }
    hasReadCurrent = false;
  }

  private boolean isEndOfStream() {
    return getCurrent() == -1;
  }
}