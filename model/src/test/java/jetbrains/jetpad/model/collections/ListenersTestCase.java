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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class ListenersTestCase {

  protected abstract MyCollection createCollection();
  protected abstract MyCollection createThrowingOnAddCollection();
  protected abstract MyCollection createThrowingOnRemoveCollection();

  @Test
  public void beforeAfterAreCalled() {
    MyCollection c = createCollection();
    c.add(0);
    c.remove(0);
    assertTrue(c.isEmpty());
    assertEquals(1, c.getBeforeItemAddedCallsNumber());
    assertEquals(1, c.getBeforeItemRemovedCallsNumber());
    c.verifyBeforeAfter();
  }

  @Test(expected = RuntimeException.class)
  public void beforeAfterOnAddArentAffectedByListenerExceptions() {
    MyCollection c = createCollection();
    c.addListener(createThrowingListener());
    try {
      c.add(0);
    } finally {
      c.verifyBeforeAfter();
      c.verifyLastSuccess(true);
      c.assertContentEquals(0);
    }
  }

  @Test(expected = RuntimeException.class)
  public void beforeAfterOnRemoveArentAffectedByListenerExceptions() {
    MyCollection c = createCollection();
    c.add(0);
    c.addListener(createThrowingListener());
    try {
      c.remove(0);
    } finally {
      c.verifyBeforeAfter();
      c.verifyLastSuccess(true);
      assertTrue(c.isEmpty());
    }
  }

  @Test(expected = IllegalStateException.class)
  public void addFailureDoesntAffectBeforeAfter() {
    MyCollection c = createThrowingOnAddCollection();
    c.addListener(createThrowingListener());
    try {
      c.add(0);
    } finally {
      c.verifyBeforeAfter();
      c.verifyLastSuccess(false);
      assertTrue(c.isEmpty());
    }
  }

  @Test(expected = IllegalStateException.class)
  public void removeFailureDoesntAffectBeforeAfter() {
    MyCollection c = createThrowingOnRemoveCollection();
    c.add(0);
    c.addListener(createThrowingListener());
    try {
      c.remove(0);
    } finally {
      c.verifyBeforeAfter();
      c.verifyLastSuccess(false);
      c.assertContentEquals(0);
    }
  }

  private CollectionListener<Integer> createThrowingListener() {
    return new CollectionListener<Integer>() {
      @Override
      public void onItemAdded(CollectionItemEvent<Integer> event) {
        throw new RuntimeException();
      }
      @Override
      public void onItemRemoved(CollectionItemEvent<Integer> event) {
        throw new RuntimeException();
      }
    };
  }

  protected static interface MyCollection extends ObservableCollection<Integer> {
    void verifyLastSuccess(boolean expected);
    void verifyBeforeAfter();
    int getBeforeItemAddedCallsNumber();
    int getBeforeItemRemovedCallsNumber();
    void assertContentEquals(Integer... expected);
  }
}