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
package jetbrains.jetpad.mapper.gwt;

import com.google.common.base.Supplier;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.query.client.css.TakesCssValue;
import com.google.gwt.user.client.Timer;
import jetbrains.jetpad.base.BaseRegistration;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.base.animation.AnimatedList;
import jetbrains.jetpad.base.animation.Animation;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;
import jetbrains.jetpad.model.property.*;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import static com.google.gwt.query.client.GQuery.$;

public class DomUtil {
  public static List<Node> elementChildren(final Element e) {
    return new AbstractList<Node>() {
      @Override
      public Node get(int index) {
        return e.getChild(index);
      }

      @Override
      public Node set(int index, Node element) {
        if (element.getParentElement() != null) {
          throw new IllegalStateException();
        }

        Node child = get(index);
        e.replaceChild(child, element);
        return child;
      }

      @Override
      public void add(int index, Node element) {
        if (element.getParentElement() != null) {
          throw new IllegalStateException();
        }

        if (index == 0) {
          e.insertFirst(element);
        } else {
          Node prev = e.getChild(index - 1);
          e.insertAfter(element, prev);
        }
      }

      @Override
      public Node remove(int index) {
        Node child = e.getChild(index);
        e.removeChild(child);
        return child;
      }

      @Override
      public int size() {
        return e.getChildCount();
      }
    };
  }

  public static List<Node> animatedChildren(Element e, final int delay) {
    return new AnimatedList<Node>(elementChildren(e)) {
      @Override
      public Animation addAnimation(Node n) {
        return DomAnimations.fadeIn(n, delay);
      }

      @Override
      public Animation removeAnimation(Node n) {
        return DomAnimations.fadeOut(n, delay);
      }
    };
  }

  public static List<Node> animatedChildren(Element e) {
    return animatedChildren(e, 300);
  }

  public static List<Node> animatedChildren(Element e, final com.google.common.base.Function<Node, Animation> add, final com.google.common.base.Function<Node, Animation> remove) {
    return new AnimatedList<Node>(elementChildren(e)) {
      @Override
      public Animation addAnimation(Node n) {
        return add.apply(n);
      }

      @Override
      public Animation removeAnimation(Node n) {
        return remove.apply(n);
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

  public static List<WithElement> withAnimatedElementChildren(Element e) {
    return withElementChildren(animatedChildren(e));
  }

  public static List<WithElement> withAnimatedElementChildren(Element e, int delay) {
    return withElementChildren(animatedChildren(e, delay));
  }

  public static List<WithElement> withAnimatedElementChildren(Element e, final com.google.common.base.Function<Node, Animation> add, final com.google.common.base.Function<Node, Animation> remove) {
    return withElementChildren(animatedChildren(e, add, remove));
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
                myListeners.fire(new ListenerCaller<EventHandler<? super PropertyChangeEvent<Boolean>>>() {
                  @Override
                  public void call(EventHandler<? super PropertyChangeEvent<Boolean>> l) {
                    l.onEvent(new PropertyChangeEvent<>(value.get(), currentValue));
                  }
                });
                value.set(currentValue);
              }
            }
          };
          timer.scheduleRepeating(100);
          myTimerRegistration = new BaseRegistration() {
            @Override
            protected void doRemove() {
              timer.cancel();
            }
          };
        }
        final Registration reg = myListeners.add(handler);
        return new BaseRegistration() {
          @Override
          protected void doRemove() {
            reg.remove();
            myTimerRegistration.remove();
            myTimerRegistration = null;
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

  public static WritableProperty<Boolean> cssValue(final Element el, final TakesCssValue<?> css, String value) {
    return new WritableProperty<Boolean>() {
      @Override
      public void set(Boolean value) {
        $(el).css(css, value);
      }
    };
  }

  public static WritableProperty<Boolean> hasCssValue(final Element el, final TakesCssValue<?> css, final String value) {
    return new WritableProperty<Boolean>() {
      @Override
      public void set(Boolean val) {
        if (val) {
          $(el).css(css, value);
        } else {
          $(el).css(css, null);
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