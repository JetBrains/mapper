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
package jetbrains.jetpad.model.util;

import com.google.gwt.user.client.ui.DialogBox;
import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;

public class DialogChildList extends ObservableArrayList<DialogBox> {
  public DialogChildList() {
    addListener(new CollectionAdapter<DialogBox>() {
      @Override
      public void onItemAdded(CollectionItemEvent<DialogBox> event) {
        event.getItem().center();
      }

      @Override
      public void onItemRemoved(CollectionItemEvent<DialogBox> event) {
        event.getItem().hide();
      }
    });
  }
}
