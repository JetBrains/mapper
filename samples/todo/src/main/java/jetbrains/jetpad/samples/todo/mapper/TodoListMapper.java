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
package jetbrains.jetpad.samples.todo.mapper;

import com.google.gwt.query.client.Function;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.gwt.WithElement;
import jetbrains.jetpad.samples.todo.model.TodoList;
import jetbrains.jetpad.samples.todo.model.TodoListItem;

import java.util.ArrayList;
import java.util.List;

import static com.google.gwt.query.client.GQuery.$;
import static jetbrains.jetpad.mapper.Synchronizers.forObservableRole;
import static jetbrains.jetpad.mapper.Synchronizers.forProperty;
import static jetbrains.jetpad.mapper.gwt.DomUtil.innerTextOf;
import static jetbrains.jetpad.mapper.gwt.DomUtil.withElementChildren;
import static jetbrains.jetpad.model.property.Properties.size;
import static jetbrains.jetpad.model.property.Properties.toStringOf;

public class TodoListMapper extends Mapper<TodoList, TodoListView> {
  public TodoListMapper(TodoList source) {
    super(source, new TodoListView());

    $(getTarget().addNew).click(new Function() {
      @Override
      public boolean f(Event e) {
        String text = Window.prompt("Enter Task Text", null);
        if (text != null) {
          TodoListItem item = new TodoListItem();
          item.text.set(text);
          getSource().items.add(item);
        }
        return false;
      }
    });

    $(getTarget().clearCompleted).click(new Function() {
      @Override
      public boolean f(Event e) {
        List<TodoListItem> toRemove = new ArrayList<TodoListItem>();
        for (TodoListItem item : getSource().items) {
          if (item.completed.get()) {
            toRemove.add(item);
          }
        }
        for (TodoListItem item : toRemove) {
          item.removeFromParent();
        }
        return false;
      }
    });
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);
    conf.add(forObservableRole(this, getSource().items, withElementChildren(getTarget().children), new MapperFactory<TodoListItem, WithElement>() {
      @Override
      public Mapper<? extends TodoListItem, ? extends WithElement> createMapper(TodoListItem source) {
        return new TodoListItemMapper(source);
      }
    }));

    conf.add(forProperty(toStringOf(size(getSource().items)), innerTextOf(getTarget().count)));
  }
}
