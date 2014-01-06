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
package jetbrains.jetpad.samples.todo.mapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import jetbrains.jetpad.mapper.gwt.BaseWithElement;

class TodoListView extends BaseWithElement {
  private static final TodoListViewUiBinder ourUiBinder = GWT.create(TodoListViewUiBinder.class);
  @UiField
  UListElement children;
  @UiField
  SpanElement count;
  @UiField
  AnchorElement addNew;
  @UiField
  AnchorElement clearCompleted;

  public TodoListView() {
    setElement(ourUiBinder.createAndBindUi(this));
  }

  interface TodoListViewUiBinder extends UiBinder<DivElement, TodoListView> {
  }
}