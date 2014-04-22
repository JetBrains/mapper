package jetbrains.jetpad.base.base64;

import java.util.Arrays;

class Base64Table {
  private int myBase = 0;
  private final int[] myChToValue = new int[255];
  private final char[] myValueToCh = new char[255];


  Base64Table(char lastChar) {
    Arrays.fill(myChToValue, 0, myChToValue.length, -1);

    for (char ch = 'A'; ch <= 'Z'; ch++) {
      add(ch);
    }
    for (char ch = 'a'; ch <= 'z'; ch++) {
      add(ch);
    }
    for (char ch = '0'; ch <= '9'; ch++) {
      add(ch);
    }
    add('-');
    add(lastChar);

    if (myBase != 64) throw new IllegalStateException();
  }

  private void add(char ch) {
    int index = myBase++;
    myChToValue[ch] = index;
    myValueToCh[index] = ch;
  }

  int getBase() {
    return myBase;
  }

  int chToValue(char ch) {
    int result = myChToValue[ch];
    if (result == -1) {
      throw new IllegalArgumentException("Invalid base64 char '" + ch + "'");
    }
    return result;
  }

  char valueToCh(int val) {
    if (val >= myBase) {
      throw new IllegalArgumentException();
    }
    return myValueToCh[val];
  }
}
