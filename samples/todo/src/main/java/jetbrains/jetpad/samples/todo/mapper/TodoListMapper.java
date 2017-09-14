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

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.query.client.Function;
import com.google.gwt.user.client.Event;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.mapper.gwt.WithElement;
import jetbrains.jetpad.model.collections.ObservableCollection;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.Properties;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.model.transform.Transformer;
import jetbrains.jetpad.model.transform.Transformers;
import jetbrains.jetpad.samples.todo.model.TodoList;
import jetbrains.jetpad.samples.todo.model.TodoListItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.gwt.query.client.GQuery.$;
import static jetbrains.jetpad.mapper.Synchronizers.forObservableRole;
import static jetbrains.jetpad.mapper.gwt.DomUtil.checkbox;
import static jetbrains.jetpad.mapper.gwt.DomUtil.innerTextOf;
import static jetbrains.jetpad.mapper.gwt.DomUtil.visibilityOf;
import static jetbrains.jetpad.mapper.gwt.DomUtil.withElementChildren;
import static jetbrains.jetpad.model.property.Properties.ifProp;
import static jetbrains.jetpad.model.property.Properties.not;
import static jetbrains.jetpad.model.property.Properties.notEquals;
import static jetbrains.jetpad.model.property.Properties.size;
import static jetbrains.jetpad.model.property.Properties.toStringOf;

public class TodoListMapper extends Mapper<TodoList, TodoListView> {
  private final Property<Boolean> toggleAll;
  public TodoListMapper(TodoList source) {
    super(source, new TodoListView());

    toggleAll = checkbox(getTarget().toggleAll);

    // adding new task
    $(getTarget().addNew).keypress(new Function() {
      @Override
      public boolean f(Event e) {
        if ( e.getKeyCode() != KeyCodes.KEY_ENTER)
          return true;
        String text = getTarget().addNew.getValue();
        getTarget().addNew.setValue("");
        if (text != null) {
          TodoListItem item = new TodoListItem();
          item.text.set(text);
          getSource().items.add(item);
        }
        return false;
      }
    });

    // event handler for remove completed tasks
    $(getTarget().clearCompleted).click(new Function() {
      @Override
      public boolean f(Event e) {
        List<TodoListItem> toRemove = new ArrayList<>();
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


    // create event handlers for selecting filters
    // TODO: this can probably be cleaned up using synchronizers

    List<UListElement> allElements = Arrays.asList(getTarget().children, getTarget().active, getTarget().complete);
    setActive(getTarget().showAll, getTarget().children, allElements);
    setActive(getTarget().showActive, getTarget().active, allElements);
    setActive(getTarget().showComplete, getTarget().complete, allElements);

    $(getTarget().toggleAll).click(new Function() {
      @Override
      public boolean f(Event e) {
        boolean checked = toggleAll.get();
        for (TodoListItem tli : getSource().items)
          tli.completed.set(checked);
        return true;
      }
    });

  }

  private void setActive(final AnchorElement element, final UListElement active, final List<UListElement> allElements) {
    $(element).click(new Function() {
      @Override
      public boolean f(Event e) {
        $("a.inline").removeClass("selected");
        $(element).addClass("selected");
        for (UListElement otherElement : allElements) {
          if (otherElement != active) {
            $(otherElement).hide();
          }
        }
        $(active).show();
        return false;
      }
    });
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    // create filters
    Transformer<ObservableCollection<TodoListItem>, ObservableList<TodoListItem>> xfnActive;
    Transformer<ObservableCollection<TodoListItem>, ObservableList<TodoListItem>> xfnComplete;

    xfnComplete = Transformers.listFilter(new jetbrains.jetpad.base.function.Function<TodoListItem, ReadableProperty<Boolean>>() {
      @Override
      public ReadableProperty<Boolean> apply(TodoListItem f) {
        return f.completed;
      }
    });
    xfnActive = Transformers.listFilter(new jetbrains.jetpad.base.function.Function<TodoListItem, ReadableProperty<Boolean>>() {
      @Override
      public ReadableProperty<Boolean> apply(TodoListItem f) {
        return not(f.completed);
      }
    });

    // we don't want to create a new synchronizer every time a different filter is applied,
    // so create all of them at once, and dynamically select which one to display

    // unfiltered list
    conf.add(forObservableRole(this, getSource().items, withElementChildren(getTarget().children),
            new MapperFactory<TodoListItem, WithElement>() {
              @Override
              public Mapper<? extends TodoListItem, ? extends WithElement> createMapper(TodoListItem source) {
                return new TodoListItemMapper(source);
              }
            }));

    // list filtered by active
    conf.add(forObservableRole(this, getSource().items, xfnActive, withElementChildren(getTarget().active),
            new MapperFactory<TodoListItem, WithElement>() {
              @Override
              public Mapper<? extends TodoListItem, ? extends WithElement> createMapper(TodoListItem source) {
                return new TodoListItemMapper(source);
              }
            }));

    // list filtered by complete
    conf.add(forObservableRole(this, getSource().items, xfnComplete, withElementChildren(getTarget().complete),
            new MapperFactory<TodoListItem, WithElement>() {
              @Override
              public Mapper<? extends TodoListItem, ? extends WithElement> createMapper(TodoListItem source) {
                return new TodoListItemMapper(source);
              }
            }));

    // explicitly define filtered lists from model
    ObservableList<TodoListItem> completedList = xfnComplete.transform(getSource().items).getTarget();
    ObservableList<TodoListItem> activeList = xfnActive.transform(getSource().items).getTarget();

    // show "Clear completed" only if there is a completed task
    conf.add(Synchronizers.forPropsOneWay(notEquals(size(completedList), 0), visibilityOf(getTarget().clearCompleted)));

    // show number of active tasks
    conf.add(Synchronizers.forPropsOneWay(toStringOf(size(activeList)), innerTextOf(getTarget().count)));

    // use correct singular/plural on "n item(s) left"
    conf.add(Synchronizers.forPropsOneWay(ifProp(Properties.equals(size(activeList), 1), "item", "items"),
            innerTextOf(getTarget().remainingTasks)));

    // toggle all is checked iff all items are complete
    conf.add(Synchronizers.forPropsOneWay(Properties.equals(size(activeList), 0), toggleAll));

  }
}