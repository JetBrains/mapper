package jetbrains.jetpad.mapper.gwt;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import jetbrains.jetpad.base.animation.Animation;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

abstract class AnimatedList extends AbstractList<Node> {
  private List<Node> myList;
  private List<Boolean> myRemoved = new ArrayList<>();
  private int myRemoveCount;
  private List<Animation> myAnimations = new ArrayList<>();

  AnimatedList(Element target) {
    myList = DomUtil.elementChildren(target);
  }

  abstract Animation addAnimation(Node n);

  abstract Animation removeAnimation(Node n);

  @Override
  public Node get(int index) {
    return myList.get(actualIndex(index));
  }

  @Override
  public Node set(int index, Node n) {
    return myList.set(actualIndex(index), n);
  }

  @Override
  public void add(int index, Node n) {
    int actual = actualIndex(index);
    myList.add(actual, n);
    myRemoved.add(index, false);
    final Animation animation = addAnimation(n);
    animation.whenDone(new Runnable() {
      @Override
      public void run() {
        int index = myAnimations.indexOf(animation);
        myAnimations.set(index, null);
      }
    });
    myAnimations.add(index, animation);
  }

  @Override
  public Node remove(int index) {
    int actual = actualIndex(index);
    myRemoved.set(index, true);
    final Node n = myList.get(actual);
    if (myAnimations.get(actual) != null) {
      myAnimations.get(actual).stop();
    }
    Animation animation = removeAnimation(n);
    animation.whenDone(new Runnable() {
      boolean wasCalled = false;

      @Override
      public void run() {
        if (wasCalled) {
          System.out.println();
        }
        wasCalled = true;
        int index = myList.indexOf(n);
        myList.remove(index);
        myRemoved.remove(index);
        myAnimations.remove(index);
      }
    });
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
    }
  }
}
