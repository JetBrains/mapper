/*
 * Copyright 2012-2015 JetBrains s.r.o
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