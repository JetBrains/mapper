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
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.samples.todo.model.TodoListItem;

import static com.google.gwt.query.client.GQuery.$;
import static jetbrains.jetpad.mapper.Synchronizers.forProps1Way;
import static jetbrains.jetpad.mapper.gwt.DomUtil.*;

class TodoListItemMapper extends Mapper<TodoListItem, TodoListItemView> {
  TodoListItemMapper(TodoListItem source) {
    super(source, new TodoListItemView());

    $(getTarget().delete).click(new Function() {
      @Override
      public boolean f(Event e) {
        if (Window.confirm("Are you Sure")) {
          getSource().removeFromParent();
        }
        return false;
      }
    });
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forProps2Way(getSource().text, editableTextOf(getTarget().text)));
    conf.add(Synchronizers.forProps2Way(getSource().completed, checkbox(getTarget().checkbox)));
    conf.add(forProps1Way(getSource().completed, hasClass(getTarget().text, "completed")));
  }
}
