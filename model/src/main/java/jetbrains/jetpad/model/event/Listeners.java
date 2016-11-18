/*
 * Copyright 2012-2016 JetBrains s.r.o
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
package jetbrains.jetpad.model.event;


import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.ThrowableHandlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Reusable container for listeners.
 * It supports:
 * - managing listeners
 * - firing events
 */
public class Listeners<ListenerT> {
  private List<Object> myListeners = null;
  private int myFireDepth;
  private int myListenersCount;

  public boolean isEmpty() {
    return myListeners == null || myListeners.isEmpty();
  }

  public Registration add(final ListenerT l) {
    if (isEmpty()) {
      beforeFirstAdded();
    }

    if (myFireDepth > 0) {
      myListeners.add(new ListenerOp<>(l, true));
    } else {
      if (myListeners == null) {
        myListeners = new ArrayList<>(1);
      }
      myListeners.add(l);
      myListenersCount++;
    }
    return new Registration() {
      @Override
      protected void doRemove() {
        if (myFireDepth > 0) {
          myListeners.add(new ListenerOp<>(l, false));
        } else {
          myListeners.remove(l);
          myListenersCount--;
        }

        if (isEmpty()) {
          afterLastRemoved();
        }
      }
    };
  }

  public void fire(final ListenerCaller<ListenerT> h) {
    if (isEmpty()) return;
    beforeFire();
    try {
      Iterator<ListenerT> it = listenersIterator();
      while (it.hasNext()) {
        ListenerT l = it.next();
        try {
          h.call(l);
        } catch (Throwable t) {
          ThrowableHandlers.handle(t);
        }
      }
    } finally {
      afterFire();
    }
  }

  private Iterator<ListenerT> listenersIterator() {
    final int size = myListenersCount;
    return new Iterator<ListenerT>() {
      private int myIndex = 0;

      private void moveToNext() {
        while (myIndex < size && isRemoved((ListenerT) myListeners.get(myIndex))) {
          myIndex++;
        }
      }

      @Override
      public boolean hasNext() {
        moveToNext();
        return myIndex < size;
      }

      @Override
      public ListenerT next() {
        if (myIndex >= size) {
          throw new IllegalStateException();
        }
        return (ListenerT) myListeners.get(myIndex++);
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  private boolean isRemoved(ListenerT l) {
    int size = myListeners.size();
    for (int i = myListenersCount; i < size; i++) {
      ListenerOp<ListenerT> op = (ListenerOp<ListenerT>) myListeners.get(i);
      if (!op.add && op.listener == l) return true;
    }
    return false;
  }

  protected void beforeFirstAdded() {
  }

  protected void afterLastRemoved() {
  }

  private void beforeFire() {
    myFireDepth++;
  }

  private void afterFire() {
    myFireDepth--;
    if (myFireDepth == 0) {
      List<Object> opsList = myListeners.subList(myListenersCount, myListeners.size());
      Object[] ops = opsList.toArray();
      opsList.clear();
      for (Object o : ops) {
        ListenerOp<ListenerT> op = (ListenerOp<ListenerT>) o;
        if (op.add) {
          myListeners.add(op.listener);
          myListenersCount++;
        } else {
          myListeners.remove(op.listener);
          myListenersCount--;
        }
      }
    }
  }

  int size() {
    return myListeners == null ? 0 : myListeners.size();
  }

  private static class ListenerOp<ListenerT> {
    final ListenerT listener;
    final boolean add;

    ListenerOp(ListenerT listener, boolean add) {
      this.listener = listener;
      this.add = add;
    }
  }
}