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
package jetbrains.jetpad.samples.todo.mapper;

import com.google.gwt.aria.client.ListitemRole;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import jetbrains.jetpad.mapper.gwt.BaseWithElement;

class TodoListItemView extends BaseWithElement {
  private static final TodoListItemViewUiBinder ourUiBinder = GWT.create(TodoListItemViewUiBinder.class);
  @UiField
  Element listItem;
  @UiField
  LabelElement text;
  @UiField
  ButtonElement delete;
  @UiField
  InputElement checkbox;

  TodoListItemView() {
    setElement(ourUiBinder.createAndBindUi(this));
  }

  interface TodoListItemViewUiBinder extends UiBinder<LIElement, TodoListItemView> {
  }
}