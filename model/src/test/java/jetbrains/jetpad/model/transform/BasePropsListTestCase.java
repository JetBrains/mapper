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

import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;
import org.junit.After;
import org.junit.Before;

import java.util.List;

import static junit.framework.TestCase.assertEquals;

public abstract class BasePropsListTestCase {
  protected static final int FILL_SIZE = 8;
  protected static final int LAST = FILL_SIZE - 1;
  protected static final int MIDDLE = FILL_SIZE / 2;
  protected static final ObjectsFactory CREATE_NEW_EACH_TIME = new ObjectsFactory() {
    @Override
    public Object create() {
      return new Object();
    }
  };
  protected static final ObjectsFactory REUSE_NEXT_BUT_ONE = new ObjectsFactory() {
    private Object myReusable = new Object();
    private boolean myReuseNextTime = true;
    @Override
    public Object create() {
      Object product = myReuseNextTime ? myReusable : new Object();
      myReuseNextTime = !myReuseNextTime;
      return product;
    }
  };

  protected ObservableList<Object> from;
  protected ObservableList<Property<Object>> to;
  protected List<Object> reference;
  protected Transformation<ObservableList<Object>, ObservableList<Property<Object>>> transformation;

  @Before
  public void setup() {
    initLists();
  }

  @After
  public void shutdown() {
    dispose();
  }

  public abstract void initLists();

  public abstract void dispose();

  protected void fillFromAndReference(ObjectsFactory factory) {
    for (int i = 0; i < FILL_SIZE; i++) {
      Object item = factory.create();
      from.add(item);
      reference.add(item);
    }
  }

  protected void fillToAndReference(ObjectsFactory factory) {
    for (int i = 0; i < FILL_SIZE; i++) {
      Object item = factory.create();
      to.add(new ValueProperty<>(item));
      reference.add(item);
    }
  }

  protected void fillFrom(ObjectsFactory factory) {
    for (int i = 0; i < FILL_SIZE; i++) {
      from.add(factory.create());
    }
  }

  protected void verifyListsAgainstReference() {
    verifyFromAgainstReference();
    verifyToAgainstReference();
  }

  protected void verifyFromAgainstReference() {
    assertEquals(reference.size(), from.size());
    for (int i = 0; i < reference.size(); i++) {
      assertEquals(reference.get(i), from.get(i));
    }
  }

  protected void verifyToAgainstReference() {
    assertEquals(reference.size(), to.size());
    for (int i = 0; i < reference.size(); i++) {
      assertEquals(reference.get(i), to.get(i).get());
    }
  }

  private interface ObjectsFactory {
    Object create();
  }
}
