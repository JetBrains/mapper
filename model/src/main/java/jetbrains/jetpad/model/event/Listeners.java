/*
 * Copyright 2012-2013 JetBrains s.r.o
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

  public void fire(final ListenerCaller<ListenerT> h) {
    if (myListeners == null) return;

    try {
      if (myFireData == null) {
        myFireData = new FireData<ListenerT>();
      }
      myFireData.myDepth++;
      Callbacks.call(myListeners, new Callbacks.Caller<ListenerT>() {
        @Override
        public void call(ListenerT callback) {
          if (myFireData.myToRemove != null && myFireData.myToRemove.contains(callback)) return;
          h.call(callback);
        }
      });
    } finally {
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
  }

  private static class FireData<ListenerT> {
    //todo this is hack. nice to provide a better way of firing such events
    //todo in case of nested events, we might send a merge events from several parts
    private int myDepth;
    private List<ListenerT> myToRemove;
    private List<ListenerT> myToAdd;
  }
}
