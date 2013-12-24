package jetbrains.jetpad.model.composite.dump;

public interface Printer<ItemT> {
  public static final Printer<Object> TO_STRING_PRINTER = new Printer<Object>() {
    @Override
    public void print(DumpContext ctx, Object item) {
      ctx.println(item.toString());
    }
  };

  void print(DumpContext ctx, ItemT item);
}
