package jetbrains.jetpad.model.children;

import java.util.Iterator;

public class Composites {
  public static <CompositeT extends Composite<CompositeT>> boolean isNonCompositeChild(CompositeT c) {
    if (c.parent().get() == null) return false;
    return c.parent().get().children().indexOf(c) == -1;
  }

  public static <CompositeT extends Composite<CompositeT>> CompositeT nextSibling(CompositeT c) {
    CompositeT parent = c.parent().get();
    if (parent == null) return null;
    if (isNonCompositeChild(c)) return null;
    int index = parent.children().indexOf(c);
    if (index + 1 < parent.children().size()) {
      return parent.children().get(index + 1);
    }
    return null;
  }

  public static <CompositeT extends Composite<CompositeT>> CompositeT prevSibling(CompositeT c) {
    CompositeT parent = c.parent().get();
    if (parent == null) return null;
    if (isNonCompositeChild(c)) return null;
    int index = parent.children().indexOf(c);
    if (index > 0) {
      return parent.children().get(index - 1);
    }
    return null;
  }

  public static <CompositeT extends Composite<CompositeT>> CompositeT firstLeaf(CompositeT c) {
    if (c.children().isEmpty()) return c;
    return firstLeaf(c.children().get(0));
  }

  public static <CompositeT extends Composite<CompositeT>> CompositeT lastLeaf(CompositeT c) {
    if (c.children().isEmpty()) return c;
    return lastLeaf(c.children().get(c.children().size() - 1));
  }

  public static <CompositeT extends Composite<CompositeT>> CompositeT nextLeaf(CompositeT c) {
    CompositeT nextSibling = nextSibling(c);
    if (nextSibling != null) {
      return firstLeaf(nextSibling);
    }

    if (isNonCompositeChild(c)) return null;

    CompositeT parent = c.parent().get();
    if (parent == null) return null;
    return nextLeaf(parent);
  }

  public static <CompositeT extends Composite<CompositeT>> CompositeT prevLeaf(CompositeT c) {
    CompositeT prevSibling = prevSibling(c);
    if (prevSibling != null) {
      return lastLeaf(prevSibling);
    }

    if (isNonCompositeChild(c)) return null;

    CompositeT parent = c.parent().get();
    if (parent == null) return null;
    return prevLeaf(parent);
  }

  public static <CompositeT extends Composite<CompositeT>>  Iterable<CompositeT> ancestors(final CompositeT current) {
    return new Iterable<CompositeT>() {
      @Override
      public Iterator<CompositeT> iterator() {
        return new Iterator<CompositeT>() {
          private CompositeT myCurrent = current;

          @Override
          public boolean hasNext() {
            return myCurrent != null;
          }

          @Override
          public CompositeT next() {
            myCurrent = myCurrent.parent().get();
            return myCurrent;
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  public static <CompositeT extends Composite<CompositeT>> Iterable<CompositeT> nextLeaves(final CompositeT current) {
    return new Iterable<CompositeT>() {
      @Override
      public Iterator<CompositeT> iterator() {
        return new Iterator<CompositeT>() {
          private CompositeT myCurrentLeaf = current;
          private CompositeT myNextLeaf = nextLeaf(current);

          @Override
          public boolean hasNext() {
            return myNextLeaf != null;
          }

          @Override
          public CompositeT next() {
            myCurrentLeaf = myNextLeaf;
            myNextLeaf = nextLeaf(myCurrentLeaf);
            return myCurrentLeaf;
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  public static <CompositeT extends Composite<CompositeT>> Iterable<CompositeT> prevLeaves(final CompositeT current) {
    return new Iterable<CompositeT>() {
      @Override
      public Iterator<CompositeT> iterator() {
        return new Iterator<CompositeT>() {
          private CompositeT myCurrentLeaf = current;
          private CompositeT myPrevLeaf = prevLeaf(current);

          @Override
          public boolean hasNext() {
            return myPrevLeaf != null;
          }

          @Override
          public CompositeT next() {
            myCurrentLeaf = myPrevLeaf;
            myPrevLeaf = prevLeaf(myCurrentLeaf);
            return myCurrentLeaf;
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }
}
