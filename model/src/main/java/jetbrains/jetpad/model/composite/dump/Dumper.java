package jetbrains.jetpad.model.composite.dump;

import jetbrains.jetpad.model.composite.Composite;

public class Dumper<CompositeT extends Composite<CompositeT>> {
  private Printer<? super CompositeT> myPrinter;

  public Dumper(Printer<? super CompositeT> printer) {
    myPrinter = printer;
  }

  public Dumper() {
    this(Printer.TO_STRING_PRINTER);
  }

  public String dumpToString(CompositeT item) {
    DumpContext ctx = new StringBuilderDumpContext();
    dump(ctx, item);
    return ctx.toString();
  }

  public void dump(CompositeT item) {
    System.out.println(dumpToString(item));
  }

  private void dump(final DumpContext ctx, final CompositeT item) {
    myPrinter.print(ctx, item);
    ctx.withIndent(new Runnable() {
      @Override
      public void run() {
        for (CompositeT c : item.children()) {
          dump(ctx, c);
        }
      }
    });
  }

}
