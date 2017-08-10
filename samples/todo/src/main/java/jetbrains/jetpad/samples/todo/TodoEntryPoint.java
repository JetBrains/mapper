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
package jetbrains.jetpad.samples.todo;

import com.google.gwt.core.client.EntryPoint;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.gwt.WithElement;
import jetbrains.jetpad.samples.todo.mapper.TodoListMapper;
import jetbrains.jetpad.samples.todo.model.TodoList;
import jetbrains.jetpad.samples.todo.model.TodoListItem;

import static com.google.gwt.query.client.GQuery.$;

public class TodoEntryPoint implements EntryPoint {
  @Override
  public void onModuleLoad() {
    TodoList model = createModel();

    Mapper<TodoList, ? extends WithElement> mapper = new TodoListMapper(model);
    mapper.attachRoot();

    $("#wrapper").append(mapper.getTarget().getElement());
  }

  private TodoList createModel() {
    TodoList result = new TodoList();
    result.items.add(createTodoItem("Clean up dependencies", true));
    result.items.add(createTodoItem("Write javadoc", false));
    result.items.add(createTodoItem("Write documentation", true));
    result.items.add(createTodoItem("Create samples", false));
    return result;
  }

  private TodoListItem createTodoItem(String text, boolean completed) {
    TodoListItem result = new TodoListItem();
    result.text.set(text);
    result.completed.set(completed);
    return result;
  }
}