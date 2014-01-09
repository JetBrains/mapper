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
package jetbrains.jetpad.model.event;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Listeners<ListenerT> {
  private List<ListenerT> myListeners;
  private FireData<ListenerT> myFireData;

  public boolean isEmpty() {
    if (myListeners == null) return true;
    return myListeners.isEmpty();
  }

  public Registration add(final ListenerT l) {
    if (myFireData != null) {
      if (myFireData.myToAdd == null) {
        myFireData.myToAdd = new ArrayList<ListenerT>(1);
      }
      myFireData.myToAdd.add(l);
    } else {
      if (myListeners == null) {
        myListeners = new ArrayList<ListenerT>(1);
      }
      myListeners.add(l);
    }
    return new Registration() {
      @Override
      public void remove() {
        if (myFireData != null) {
          if (myFireData.myToRemove == null) {
            myFireData.myToRemove = new ArrayList<ListenerT>(1);
          }
          myFireData.myToRemove.add(l);
        } else {
          myListeners.remove(l);
        }
      }
    };
  }

  /**
   * Fires events supporting add/remove of listeners during firing process.
   *
   * Warning: if you don't need such support just use listeners. By doing this, you improve performance and reduce
   * stack depth, streamlining debug.
   */
  public void fire(final ListenerCaller<ListenerT> h) {
    if (isEmpty()) return;
    beforeFire();
    try {
      for (ListenerT l : myListeners) {
        if (isRemoved(l)) continue;
        try {
          h.call(l);
        } catch (Throwable t) {
          Callbacks.handleException(t);
        }
      }
    } finally {
      afterFire();
    }
  }

  public Iterable<ListenerT> listeners() {
    if (isEmpty()) return Collections.emptyList();
    return myListeners;
  }

  private boolean isRemoved(ListenerT l) {
    return myFireData.myToRemove != null && myFireData.myToRemove.contains(l);
  }

  private void beforeFire() {
    if (myFireData == null) {
      myFireData = new FireData<ListenerT>();
    }
    myFireData.myDepth++;
  }

  private void afterFire() {
    myFireData.myDepth--;
    if (myFireData.myDepth == 0) {
      if (myFireData.myToRemove != null) {
        myListeners.removeAll(myFireData.myToRemove);
        myFireData.myToRemove = null;
      }
      if (myFireData.myToAdd != null) {
        myListeners.addAll(myFireData.myToAdd);
        myFireData.myToAdd = null;
      }
      myFireData = null;
    }
  }

  private static class FireData<ListenerT> {
    private int myDepth;
    private List<ListenerT> myToRemove;
    private List<ListenerT> myToAdd;
  }
}