package jetbrains.jetpad.model.composite.dump;

import com.google.common.base.Strings;

public class StringBuilderDumpContext implements DumpContext {
  private StringBuilder myResult = new StringBuilder();
  private int myIndent;

  @Override
  public void withIndent(Runnable r) {
    myIndent++;
    try {
      r.run();
    } finally {
      myIndent--;
    }
  }

  @Override
  public void println(String text) {
    myResult.append(Strings.repeat("  ", myIndent)).append(text).append("\n");
  }

  public String toString() {
    return myResult.toString();
  }
}
