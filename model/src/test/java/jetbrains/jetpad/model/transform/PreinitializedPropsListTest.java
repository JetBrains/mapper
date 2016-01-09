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
package jetbrains.jetpad.model.transform;

import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;

public class PreinitializedPropsListTest extends BasePropsListTestCase {
  @Override
  public void initLists() {
    from = new ObservableArrayList<>();
    to = new ObservableArrayList<>();
    reference = new ArrayList<>();
    transformation = Transformers.toPropsListTwoWay().transform(from, to);
  }

  @Override
  public void dispose() {
    transformation.dispose();
  }

  @Test
  public void verifyTransformation() {
    assertTrue(transformation.getSource() == from);
    assertTrue(transformation.getTarget() == to);
  }

  @Test
  public void fillForwardWithUniqueItems() {
    fillFromAndReference(CREATE_NEW_EACH_TIME);
    verifyListsAgainstReference();
  }

  @Test
  public void fillBackwardWithUniqueItems() {
    fillToAndReference(CREATE_NEW_EACH_TIME);
    verifyListsAgainstReference();
  }

  @Test
  public void fillForwardWithRepeats() {
    fillFromAndReference(REUSE_NEXT_BUT_ONE);
    verifyListsAgainstReference();
  }

  @Test
  public void fillBackwardWithRepeats() {
    fillToAndReference(REUSE_NEXT_BUT_ONE);
    verifyListsAgainstReference();
  }

  @Test
  public void setForward() {
    testItemsSet(new ItemSetter() {
      @Override
      public void doSet(int index, Object newItem) {
        from.set(index, newItem);
      }
      @Override
      public boolean newPropertyMustBeCreated() {
        return false;
      }
    });
  }

  @Test
  public void setBackwardValue() {
    testItemsSet(new ItemSetter() {
      @Override
      public void doSet(int index, Object newItem) {
        to.get(index).set(newItem);
      }
      @Override
      public boolean newPropertyMustBeCreated() {
        return false;
      }
    });
  }

  @Test
  public void setBackwardProperty() {
    testItemsSet(new ItemSetter() {
      @Override
      public void doSet(int index, Object newItem) {
        to.set(index, new ValueProperty<>(newItem));
      }
      @Override
      public boolean newPropertyMustBeCreated() {
        return true;
      }
    });
  }

  private void testItemsSet(ItemSetter itemSetter) {
    fillFrom(REUSE_NEXT_BUT_ONE);
    // Set first, two adjacent items in the middle (unique and non-unique), and last
    for (int index : new int[] { 0, MIDDLE, MIDDLE + 1, LAST}) {
      Property<Object> oldProperty = to.get(index);
      Object newItem = new Object();
      itemSetter.doSet(index, newItem);
      Property<Object> newProperty = to.get(index);
      if (itemSetter.newPropertyMustBeCreated()) {
        assertTrue(oldProperty != newProperty);
      } else {
        assertTrue(oldProperty == newProperty);
      }
      assertEquals(newItem, from.get(index));
      assertEquals(newItem, newProperty.get());
    }
  }

  @Test
  public void removeForward() {
    testItemsRemove(new ItemRemover() {
      @Override
      public void doRemove(int index) {
        from.remove(index);
      }
    });
  }

  @Test
  public void removeBackward() {
    testItemsRemove(new ItemRemover() {
      @Override
      public void doRemove(int index) {
        to.remove(index);
      }
    });
  }

  private void testItemsRemove(ItemRemover itemRemover) {
    fillFromAndReference(REUSE_NEXT_BUT_ONE);
    // Remove last, twice from the middle (unique and non-unique), and first
    for (int index : new int[] { LAST, MIDDLE, MIDDLE, 0 }) {
      itemRemover.doRemove(index);
      reference.remove(index);
      verifyListsAgainstReference();
    }
  }

  @Test
  public void clearForward() {
    fillFrom(REUSE_NEXT_BUT_ONE);
    from.clear();
    checkBothListsEmpty();
  }

  @Test
  public void clearBackward() {
    fillFrom(REUSE_NEXT_BUT_ONE);
    to.clear();
    checkBothListsEmpty();
  }

  private void checkBothListsEmpty() {
    assertTrue(from.isEmpty());
    assertTrue(to.isEmpty());
  }

  @Test(expected = IllegalArgumentException.class)
  public void addSamePropertyTwice() {
    Property<Object> prop = new ValueProperty<>(new Object());
    to.add(prop);
    to.add(prop);
  }

  private interface ItemSetter {
    void doSet(int index, Object newItem);
    boolean newPropertyMustBeCreated();
  }

  private interface ItemRemover {
    void doRemove(int index);
  }
}
