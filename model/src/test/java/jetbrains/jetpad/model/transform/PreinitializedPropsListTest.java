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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
  public void fillForward() {
    fillFromAndReference();
    verifyListsAgainstReference();
  }

  @Test
  public void fillBackward() {
    fillToAndReference();
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
    fillFrom();
    for (int index : new int[] { 0, MIDDLE, LAST}) {
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
    fillFromAndReference();
    for (int index : new int[] { LAST, MIDDLE, 0 }) {
      itemRemover.doRemove(index);
      reference.remove(index);
      verifyListsAgainstReference();
    }
  }

  @Test
  public void clearForward() {
    fillFrom();
    from.clear();
    checkBothListsEmpty();
  }

  @Test
  public void clearBackward() {
    fillFrom();
    to.clear();
    checkBothListsEmpty();
  }

  @Test(expected = IllegalStateException.class)
  public void duplicatesDetection() {
    fillFrom();
    Object duplicate = new Object();
    from.set(MIDDLE, duplicate);
    from.set(MIDDLE + 1, duplicate);
    to.get(MIDDLE + 1).set(new Object());
  }

  @Test(expected = NullPointerException.class)
  public void nullValuesNotSupported() {
    from.add(new Object());
    to.get(0).set(null);
  }

  @Test(expected = NullPointerException.class)
  public void nullPropertiesNotSupported() {
    to.add(null);
  }

  private void checkBothListsEmpty() {
    assertTrue(from.isEmpty());
    assertTrue(to.isEmpty());
  }

  private interface ItemSetter {
    void doSet(int index, Object newItem);
    boolean newPropertyMustBeCreated();
  }

  private interface ItemRemover {
    void doRemove(int index);
  }
}
