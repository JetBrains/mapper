package jetbrains.jetpad.base.animation;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AnimatedListTest {
  Item i1 = new Item("i1");
  Item i2 = new Item("i2");
  Item i3 = new Item("i3");
  List<Item> baseList = new ArrayList<>();
  AnimatedItemList animatedList = new AnimatedItemList(baseList);


  @Test
  public void add() {
    init(i1, i2);

    animatedList.add(i3);

    assertNotNull(i3.animation);

    assertEquals(baseList, animatedList);
  }

  @Test
  public void addWhenEverythingRemoved() {
    init(i1);
    animatedList.remove(i1);
    animatedList.add(i2);

    assertEquals(Arrays.asList(i2), animatedList);
  }

  @Test
  public void singleRemove() {
    init(i1, i2);

    animatedList.remove(i1);

    assertNotNull(i1.animation);
    assertEquals(2, baseList.size());
    assertEquals(1, animatedList.size());
  }

  @Test
  public void doubleRemove() {
    init(i1, i2, i3);

    animatedList.remove(i1);
    animatedList.remove(i3);

    assertEquals(Arrays.asList(i1, i2, i3), baseList);
    assertEquals(Arrays.asList(i2), animatedList);
  }

  @Test
  public void removeUnfinishedAdd() {
    init(i1, i2);

    animatedList.add(i3);
    animatedList.remove(i3);

    assertNotNull(i3.animation);
    assertEquals(Arrays.asList(i1, i2), animatedList);
    assertEquals(Arrays.asList(i1, i2, i3), baseList);
  }

  private void init(Item... items) {
    animatedList.addAll(Arrays.asList(items));
    finishAllAnimations();
  }



  private void finishAllAnimations() {
    for (Item i : allItems()) {
      if (i.animation != null) {
        i.animation.done();
      }
    }

  }

  private List<Item> allItems() {
    return Arrays.asList(i1, i2, i3);
  }


  static class AnimatedItemList extends AnimatedList<Item> {
    AnimatedItemList(List<Item> list) {
      super(list);
    }

    @Override
    public Animation addAnimation(Item e) {
      e.recreateAnimation();
      return e.animation;
    }

    @Override
    public Animation removeAnimation(Item e) {
      e.recreateAnimation();
      return e.animation;
    }
  }

  static class Item {
    DefaultAnimation animation;
    String name;

    Item(String name) {
      this.name = name;
      recreateAnimation();
    }

    void clearAnimation() {
      animation = null;
    }

    void recreateAnimation() {
      animation = new DefaultAnimation() {
        @Override
        protected void doStop() {
          animation = null;
        }
      };
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
