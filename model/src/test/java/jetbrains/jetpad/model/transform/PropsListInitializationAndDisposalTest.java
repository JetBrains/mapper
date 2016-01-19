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

import static jetbrains.jetpad.model.transform.Transformers.toPropsListTwoWay;
import static org.junit.Assert.assertEquals;

public class PropsListInitializationAndDisposalTest extends BasePropsListTestCase {
  private boolean disposeNeeded = false;

  @Override
  public void initLists() {
    from = new ObservableArrayList<>();
    reference = new ArrayList<>();
  }

  @Override
  public void dispose() {
    if (disposeNeeded) {
      transformation.dispose();
    }
  }

  @Test
  public void useNonemptySourceList() {
    fillFromAndReference();
    transformation = toPropsListTwoWay().transform(from);
    to = transformation.getTarget();
    verifyListsAgainstReference();
    disposeNeeded = true;
  }

  @Test(expected = IllegalArgumentException.class)
  public void useNonemptyTargetList() {
    to = new ObservableArrayList<>();
    to.add(new ValueProperty<>(new Object()));
    try {
      toPropsListTwoWay().transform(from, to);
    } finally {
      disposeNeeded = false;
    }
  }

  @Test
  public void addRemoveAfterDisposal() {
    fillAndDispose();
    from.add(new Object());
    to.remove(LAST);
    assertEquals(FILL_SIZE + 1, from.size());
    assertEquals(FILL_SIZE - 1, to.size());
  }

  @Test
  public void setForwardAfterUnbinding() {
    fillAndDispose();
    Property<Object> oldProperty = to.get(MIDDLE);
    Object oldItem = oldProperty.get();
    from.set(MIDDLE, new Object());
    assertEquals(oldProperty, to.get(MIDDLE));
    assertEquals(oldItem, to.get(MIDDLE).get());
  }

  @Test
  public void setBackwardValueAfterUnbinding() {
    fillAndDispose();
    Object oldItem = from.get(MIDDLE);
    to.get(MIDDLE).set(new Object());
    assertEquals(oldItem, from.get(MIDDLE));
  }

  @Test
  public void setBackwardPropertyAfterUnbinding() {
    fillAndDispose();
    Object oldItem = from.get(MIDDLE);
    to.set(MIDDLE, new ValueProperty<>(new Object()));
    assertEquals(oldItem, from.get(MIDDLE));
  }

  private void fillAndDispose() {
    transformation = toPropsListTwoWay().transform(from);
    to = transformation.getTarget();
    fillFrom();
    transformation.dispose();
    disposeNeeded = false;
  }
}
