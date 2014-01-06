/*
 * Copyright 2012-2014 JetBrains s.r.o
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

import jetbrains.jetpad.model.collections.set.ObservableHashSet;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class SetListenersTest extends ListenersTestCase {
  @Override
  protected MyCollection createCollection() {
    return new TestObservableHashSet();
  }

  @Override
  protected MyCollection createThrowingOnAddCollection() {
    return new TestObservableHashSet() {
        @Override
        public boolean add(Integer integer) {
          add(integer, new Runnable() {
            @Override
            public void run() {
              throw new IllegalStateException();
            }
          });
          return true;
        }
      };
  }

  @Override
  protected MyCollection createThrowingOnRemoveCollection() {
    return new TestObservableHashSet() {
      @Override
      public boolean remove(Object integer) {
        remove((Integer) integer, new Runnable() {
          @Override
          public void run() {
            throw new IllegalStateException();
          }
        });
        return true;
      }
    };
  }

  private static class TestObservableHashSet extends ObservableHashSet<Integer> implements MyCollection {
    private int beforeItemAddedCalled;
    private int afterItemAddedCalled;
    private int beforeItemRemovedCalled;
    private int afterItemRemovedCalled;
    private boolean successful;

    @Override
    protected void beforeItemAdded(Integer item) {
      beforeItemAddedCalled++;
    }

    @Override
    protected void afterItemAdded(Integer item, boolean success) {
      afterItemAddedCalled++;
      successful = success;
    }

    @Override
    protected void beforeItemRemoved(Integer item) {
      beforeItemRemovedCalled++;
    }

    @Override
    protected void afterItemRemoved(Integer item, boolean success) {
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
      assertEquals(new HashSet<Integer>(Arrays.asList(expected)), this);
    }
  }
}