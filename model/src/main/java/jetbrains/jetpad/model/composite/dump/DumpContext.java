package jetbrains.jetpad.model.composite.dump;

public interface DumpContext {
  void withIndent(Runnable r);
  void println(String text);
}
