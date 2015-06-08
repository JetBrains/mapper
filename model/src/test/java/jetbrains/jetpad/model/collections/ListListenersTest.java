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
package jetbrains.jetpad.model.collections;

import jetbrains.jetpad.model.collections.list.ObservableArrayList;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class ListListenersTest extends ListenersTestCase {
  @Override
  protected MyCollection createCollection() {
    return new TestObservableArrayList();
  }

  @Override
  protected MyCollection createThrowingOnAddCollection() {
    return new TestObservableArrayList() {
      @Override
      public void add(int index, Integer item) {
        add(index, item, new Runnable() {
          @Override
          public void run() {
            throw new IllegalStateException();
          }
        });
      }
    };
  }

  @Override
  protected MyCollection createThrowingOnRemoveCollection() {
    return new TestObservableArrayList() {
      @Override
      public Integer remove(int index) {
        Integer result = get(index);
        remove(index, result, new Runnable() {
          @Override
          public void run() {
            throw new IllegalStateException();
          }
        });
        return result;
      }
    };
  }

  private static class TestObservableArrayList extends ObservableArrayList<Integer> implements MyCollection {
    private int beforeItemAddedCalled;
    private int afterItemAddedCalled;
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
      assertEquals(afterItemRemovedCalled, beforeItemRemovedCalled);
    }

    @Override
    public int getBeforeItemAddedCallsNumber() {
      return beforeItemAddedCalled;
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