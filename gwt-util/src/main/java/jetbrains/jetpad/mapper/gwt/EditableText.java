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
package jetbrains.jetpad.mapper.gwt;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.query.client.Function;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

import static com.google.gwt.query.client.GQuery.$;

public class EditableText implements Property<String> {
  private Element mySource;
  private String myValue;
  private boolean myEnabled = true;
  private boolean myEditing = false;
  private Listeners<EventHandler<? super PropertyChangeEvent<String>>> myListeners = new Listeners<>();
  private InputElement myEditor = DOM.createInputText().cast();

  public EditableText(Element element) {
    mySource = element;

    $(mySource).click(new Function() {
      @Override
      public boolean f(Event e) {
        if (myEnabled) {
          startEditing();
        }
        return false;
      }
    });

    $(myEditor).keydown(new Function() {
      @Override
      public boolean f(Event e) {
        if (e.getKeyCode() == KeyCodes.KEY_ENTER) {
          set(myEditor.getValue());
          stopEditing();
          return false;
        }

        if (e.getKeyCode() == KeyCodes.KEY_ESCAPE) {
          stopEditing();
          return false;
        }

        return true;
      }
    });

    $(myEditor).blur(new Function() {
      @Override
      public boolean f(Event e) {
        if (myEditing) {
          stopEditing();
        }
        return false;
      }
    });
  }

  public void setEnabled(boolean enabled) {
    myEnabled = enabled;
    if (!enabled && myEditing) {
      stopEditing();
    }
  }

  private void startEditing() {
    if (myEditing) {
      throw new IllegalStateException();
    }
    myEditing = true;

    mySource.setInnerText("");
    mySource.appendChild(myEditor);
    myEditor.setValue(myValue);
    myEditor.focus();
  }

  private void stopEditing() {
    if (!myEditing) {
      throw new IllegalStateException();
    }
    myEditing = false;

    myEditor.removeFromParent();
    mySource.setInnerText(myValue);
  }

  @Override
  public String get() {
    return myValue;
  }

  @Override
  public void set(String value) {
    if (java.util.Objects.equals(myValue, value)) return;

    String oldValue = myValue;
    myValue = value;
    if (myEditing) {
      myEditor.setValue(value);
    } else {
      mySource.setInnerText(value);
    }

    final PropertyChangeEvent<String> event = new PropertyChangeEvent<>(oldValue,  value);
    myListeners.fire(new ListenerCaller<EventHandler<? super PropertyChangeEvent<String>>>() {
      @Override
      public void call(EventHandler<? super PropertyChangeEvent<String>> l) {
        l.onEvent(event);
      }
    });
  }

  @Override
  public Registration addHandler(EventHandler<? super PropertyChangeEvent<String>> handler) {
    return myListeners.add(handler);
  }

  @Override
  public String getPropExpr() {
    return "editableTextOf(" + mySource + ")";
  }
}