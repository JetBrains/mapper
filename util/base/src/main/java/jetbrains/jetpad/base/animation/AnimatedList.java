package jetbrains.jetpad.base.animation;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public abstract class AnimatedList<ElementT> extends AbstractList<ElementT> {
  private List<ElementT> myList;
  private List<Boolean> myRemoved = new ArrayList<>();
  private int myRemoveCount;
  private List<Animation> myAnimations = new ArrayList<>();

  public AnimatedList(List<ElementT> list) {
    myList = list;
  }

  public abstract Animation addAnimation(ElementT e);

  public abstract Animation removeAnimation(ElementT e);

  @Override
  public ElementT get(int index) {
    return myList.get(actualIndex(index));
  }

  @Override
  public ElementT set(int index, ElementT e) {
    return myList.set(actualIndex(index), e);
  }

  @Override
  public void add(int index, ElementT e) {
    int actual = actualIndex(index);
    myList.add(actual, e);
    myRemoved.add(index, false);
    final Animation animation = addAnimation(e);
    myAnimations.add(index, animation);
    animation.whenDone(new Runnable() {
      @Override
      public void run() {
        int index = myAnimations.indexOf(animation);
        myAnimations.set(index, null);
      }
    });
  }

  @Override
  public ElementT remove(int index) {
    int actual = actualIndex(index);
    myRemoved.set(actual, true);
    final ElementT n = myList.get(actual);
    if (myAnimations.get(actual) != null) {
      myAnimations.get(actual).stop();
    }
    Animation animation = removeAnimation(n);
    animation.whenDone(new Runnable() {
      boolean wasCalled = false;

      @Override
      public void run() {
        wasCalled = true;
        int index = myList.indexOf(n);
        myList.remove(index);
        myRemoved.remove(index);
        myAnimations.remove(index);
        myRemoveCount--;
      }
    });
    myRemoveCount++;
    myAnimations.set(actual, animation);
    return n;
  }

  @Override
  public int size() {
    return myList.size() - myRemoveCount;
  }

  private int actualIndex(int index) {
    if (myRemoveCount == 0) {
      return index;
    }

    int current = 0;
    int actual = 0;
    while (true) {
      while (myRemoved.get(current)) {
        current++;
      }
      if (actual == index) {
        return current;
      }
      actual++;
      current++;
    }
  }
}
