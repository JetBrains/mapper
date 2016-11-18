/*
 * Copyright 2012-2016 JetBrains s.r.o
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
package jetbrains.jetpad.mapper.gwt;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.Timer;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.ThrowableHandlers;
import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.model.property.UpdatableProperty;
import jetbrains.jetpad.model.property.WritableProperty;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import jetbrains.jetpad.base.function.Supplier;

import static com.google.gwt.query.client.GQuery.$;

public class DomUtil {
  public static List<Node> elementChildren(final Element e) {
    return nodeChildren(e);
  }

  public static List<Node> nodeChildren(final Node n) {
    return new AbstractList<Node>() {
      @Override
      public Node get(int index) {
        return n.getChild(index);
      }

      @Override
      public Node set(int index, Node element) {
        if (element.getParentElement() != null) {
          throw new IllegalStateException();
        }

        Node child = get(index);
        n.replaceChild(child, element);
        return child;
      }

      @Override
      public void add(int index, Node element) {
        if (element.getParentElement() != null) {
          throw new IllegalStateException();
        }

        if (index == 0) {
          n.insertFirst(element);
        } else {
          Node prev = n.getChild(index - 1);
          n.insertAfter(element, prev);
        }
      }

      @Override
      public Node remove(int index) {
        Node child = n.getChild(index);
        n.removeChild(child);
        return child;
      }

      @Override
      public int size() {
        return n.getChildCount();
      }
    };
  }

  public static List<WithElement> withElementChildren(final Element e) {
    return withElementChildren(elementChildren(e));
  }

  static List<WithElement> withElementChildren(final List<Node> base) {
    final List<WithElement> items = new ArrayList<>();

    return new AbstractList<WithElement>() {
      @Override
      public WithElement get(int index) {
        return items.get(index);
      }

      @Override
      public WithElement set(int index, WithElement element) {
        WithElement result = items.set(index, element);
        base.set(index, result.getElement());
        return result;
      }

      @Override
      public void add(int index, WithElement element) {
        items.add(index, element);
        base.add(index, element.getElement());
      }

      @Override
      public WithElement remove(int index) {
        WithElement result = items.remove(index);
        base.remove(index);
        return result;
      }

      @Override
      public int size() {
        return items.size();
      }
    };
  }

  public static WritableProperty<String> innerTextOf(final Element e) {
    return new WritableProperty<String>() {
      @Override
      public void set(String value) {
        e.setInnerText(value);
      }
    };
  }

  public static EditableText editableTextOf(Element element) {
    return new EditableText(element);
  }

  public static Property<Boolean> checkbox(final InputElement element) {
    return new Property<Boolean>() {
      private Registration myTimerRegistration;
      private Listeners<EventHandler<? super PropertyChangeEvent<Boolean>>> myListeners = new Listeners<>();

      @Override
      public Boolean get() {
        return element.isChecked();
      }

      @Override
      public void set(Boolean value) {
        element.setChecked(value);
      }

      @Override
      public Registration addHandler(final EventHandler<? super PropertyChangeEvent<Boolean>> handler) {
        if (myListeners.isEmpty()) {
          final Value<Boolean> value = new Value<>(element.isChecked());
          final Timer timer = new Timer() {
            @Override
            public void run() {
              final boolean currentValue = element.isChecked();
              if (currentValue != value.get()) {
                try (Listeners.Firing<EventHandler<? super PropertyChangeEvent<Boolean>>> firing = myListeners.fire()) {
                  for (EventHandler<? super PropertyChangeEvent<Boolean>> l : firing) {
                    try {
                      l.onEvent(new PropertyChangeEvent<>(value.get(), currentValue));
                    } catch (Throwable t) {
                      ThrowableHandlers.handle(t);
                    }
                  }
                }
                value.set(currentValue);
              }
            }
          };
          timer.scheduleRepeating(100);
          myTimerRegistration = new Registration() {
            @Override
            protected void doRemove() {
              timer.cancel();
            }
          };
        }
        final Registration reg = myListeners.add(handler);
        return new Registration() {
          @Override
          protected void doRemove() {
            reg.remove();
            if (myListeners.isEmpty()) {
              myTimerRegistration.remove();
              myTimerRegistration = null;
            }
          }
        };
      }

      @Override
      public String getPropExpr() {
        return "checkbox(" + element + ")";
      }
    };
  }

  public static WritableProperty<Boolean> hasClass(final Element el, final String cls) {
    return new WritableProperty<Boolean>() {
      private boolean myValue;

      @Override
      public void set(Boolean value) {
        if (myValue == value) return;
        if (value) {
          el.addClassName(cls);
        } else {
          el.removeClassName(cls);
        }
        myValue = value;
      }
    };
  }

  public static WritableProperty<String> attribute(final Element el, final String attr) {
    return new WritableProperty<String>() {
      @Override
      public void set(String value) {
        $(el).attr(attr, value);
      }
    };
  }

  public static WritableProperty<Boolean> hasAttribute(final Element el, final String attr, final String value) {
    return new WritableProperty<Boolean>() {
      @Override
      public void set(Boolean val) {
        if (val) {
          $(el).attr(attr, value);
        } else {
          $(el).removeAttr(attr);
        }
      }
    };
  }

  public static WritableProperty<Boolean> visibilityOf(final Element el) {
    return new WritableProperty<Boolean>() {
      @Override
      public void set(Boolean value) {
        if (value) {
          $(el).show();
        } else {
          $(el).hide();
        }
      }
    };
  }

  public static WritableProperty<WithElement> withElementOneChild(final Element el) {
    return new WritableProperty<WithElement>() {
      @Override
      public void set(WithElement value) {
        el.removeAllChildren();
        if (value != null && value.getElement() != null) {
          el.appendChild(value.getElement());
        }
      }
    };
  }

  public static WritableProperty<Element> elementOneChild(final Element el) {
    return new WritableProperty<Element>() {
      @Override
      public void set(Element value) {
        el.removeAllChildren();
        if (value != null) {
          el.appendChild(value);
        }
      }
    };
  }

  public static ReadableProperty<Vector> dimension(final Element el) {
    return timerBasedProperty(new Supplier<Vector>() {
      @Override
      public Vector get() {
        return new Vector(el.getClientWidth(), el.getClientHeight());
      }
    }, 200);
  }

  public static <ValueT> ReadableProperty<ValueT> timerBasedProperty(final Supplier<ValueT> supplier, final int period) {
    return new UpdatableProperty<ValueT>() {
      private Timer myTimer = new Timer() {
        @Override
        public void run() {
          update();
        }
      };

      @Override
      protected void doAddListeners() {
        myTimer.scheduleRepeating(period);
      }

      @Override
      protected void doRemoveListeners() {
        myTimer.cancel();
      }

      @Override
      protected ValueT doGet() {
        return supplier.get();
      }
    };

  }

}