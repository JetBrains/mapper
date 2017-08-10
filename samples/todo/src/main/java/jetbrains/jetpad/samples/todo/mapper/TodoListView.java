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
package jetbrains.jetpad.samples.todo.mapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import jetbrains.jetpad.mapper.gwt.BaseWithElement;

class TodoListView extends BaseWithElement {
  private static final TodoListViewUiBinder ourUiBinder = GWT.create(TodoListViewUiBinder.class);
  @UiField
  InputElement addNew;
  @UiField
  InputElement toggleAll;
  @UiField
  UListElement children;
  @UiField
  UListElement active;
  @UiField
  UListElement complete;
  @UiField
  Element count;
  @UiField
  SpanElement remainingTasks;
  @UiField
  AnchorElement showAll;
  @UiField
  AnchorElement showActive;
  @UiField
  AnchorElement showComplete;
  @UiField
  ButtonElement clearCompleted;

  public TodoListView() {
    setElement(ourUiBinder.createAndBindUi(this));
    getElement().addClassName("centered");
  }

  interface TodoListViewUiBinder extends UiBinder<DivElement, TodoListView> {
  }
}