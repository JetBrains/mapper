package jetbrains.jetpad.base.animation;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public abstract class AnimatedList<ElementT> extends AbstractList<ElementT> {
  private List<ElementT> myList;
  private List<Boolean> myRemoved = new ArrayList<>();
  private int myRemovedCount;
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
    int actual = actualIndex(index);
    ElementT result = myList.set(actual, e);
    final Animation animation = addAnimation(e);
    myAnimations.set(actual, animation);
    animation.whenDone(new Runnable() {
      @Override
      public void run() {
        myAnimations.set(myAnimations.indexOf(animation), null);
      }
    });
    return result;
  }

  @Override
  public void add(int index, ElementT e) {
    int actual = actualIndex(index);
    myList.add(actual, e);
    myRemoved.add(actual, false);
    final Animation animation = addAnimation(e);
    myAnimations.add(actual, animation);
    animation.whenDone(new Runnable() {
      @Override
      public void run() {
        myAnimations.set(myAnimations.indexOf(animation), null);
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
    myAnimations.set(actual, animation);
    myRemovedCount++;
    animation.whenDone(new Runnable() {
      @Override
      public void run() {
        int index = myList.indexOf(n);
        myList.remove(index);
        myRemoved.remove(index);
        myAnimations.remove(index);
        myRemovedCount--;
      }
    });
    return n;
  }

  @Override
  public int size() {
    return myList.size() - myRemovedCount;
  }

  private int actualIndex(int index) {
    if (myRemovedCount == 0) {
      return index;
    }

    if (index == size()) {
      return size() + myRemovedCount;
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
