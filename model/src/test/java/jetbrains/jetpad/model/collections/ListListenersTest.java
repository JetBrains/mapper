/*
 * Copyright 2012-2017 JetBrains s.r.o
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
package jetbrains.jetpad.model.collections;

import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ListListenersTest extends ListenersTestCase {
  @Override
  protected MyList createCollection() {
    return new TestObservableArrayList();
  }

  @Override
  protected MyList createThrowingOnAddCollection() {
    return new TestObservableArrayList() {
      @Override
      protected void doAdd(int index, Integer item) {
        throw new IllegalStateException();
      }
    };
  }

  protected MyList createThrowingOnSetCollection() {
    return new TestObservableArrayList() {
      @Override
      protected void doSet(int index, Integer item) {
        throw new IllegalStateException();
      }
    };
  }

  @Override
  protected MyList createThrowingOnRemoveCollection() {
    return new TestObservableArrayList() {
      @Override
      protected void doRemove(int index) {
        throw new IllegalStateException();
      }
    };
  }

  @Test
  public void beforeAfterAreCalledOnSet() {
    MyList c = createCollection();
    c.add(0);
    c.set(0, 4);
    c.remove(0);
    assertTrue(c.isEmpty());
    assertEquals(1, c.getBeforeItemAddedCallsNumber());
    assertEquals(1, c.getBeforeItemSetCallsNumber());
    assertEquals(1, c.getBeforeItemRemovedCallsNumber());
    c.verifyBeforeAfter();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void beforeAfterOnSetArentAffectedByListenerExceptions() {
    MyList c = createCollection();
    c.add(0);
    c.addListener(createThrowingListener());
    try {
      c.set(0, 5);
    } finally {
      c.verifyBeforeAfter();
      c.verifyLastSuccess(true);
      c.assertContentEquals(5);
    }
  }

  @Test(expected = IllegalStateException.class)
  public void setFailureDoesntAffectBeforeAfter() {
    MyList c = createThrowingOnSetCollection();
    c.add(0);
    c.addListener(createThrowingListener());
    try {
      c.set(0, 5);
    } finally {
      c.verifyBeforeAfter();
      c.verifyLastSuccess(false);
      c.assertContentEquals(0);
    }
  }

  protected interface MyList extends MyCollection, ObservableList<Integer> {
    int getBeforeItemSetCallsNumber();
  }

  private static class TestObservableArrayList extends ObservableArrayList<Integer> implements MyList {
    private int beforeItemAddedCalled;
    private int afterItemAddedCalled;
    private int beforeItemSetCalled;
    private int afterItemSetCalled;
    private int beforeItemRemovedCalled;
    private int afterItemRemovedCalled;
    private boolean successful;

    @Override
    protected void beforeItemAdded(int index, Integer item) {
      beforeItemAddedCalled++;
    }

    @Override
    protected void afterItemAdded(int index, Integer item, boolean success) {
      afterItemAddedCalled++;
      successful = success;
    }

    @Override
    protected void beforeItemSet(int index, Integer oldItem, Integer newItem) {
      beforeItemSetCalled++;
    }

    @Override
    protected void afterItemSet(int index, Integer oldItem, Integer newItem, boolean success) {
      afterItemSetCalled++;
      successful = success;
    }

    @Override
    protected void beforeItemRemoved(int index, Integer item) {
      beforeItemRemovedCalled++;
    }

    @Override
    protected void afterItemRemoved(int index, Integer item, boolean success) {
      afterItemRemovedCalled++;
      successful = success;
    }

    @Override
    public void verifyLastSuccess(boolean expected) {
      assertEquals(expected, successful);
    }

    @Override
    public void verifyBeforeAfter() {
      assertEquals(afterItemAddedCalled, beforeItemAddedCalled);
      assertEquals(afterItemSetCalled, beforeItemSetCalled);
      assertEquals(afterItemRemovedCalled, beforeItemRemovedCalled);
    }

    @Override
    public int getBeforeItemAddedCallsNumber() {
      return beforeItemAddedCalled;
    }

    @Override
    public int getBeforeItemSetCallsNumber() {
      return beforeItemSetCalled;
    }

    @Override
    public int getBeforeItemRemovedCallsNumber() {
      return beforeItemRemovedCalled;
    }

    @Override
    public void assertContentEquals(Integer... expected) {
      assertEquals(Arrays.asList(expected), this);
    }
  }
}