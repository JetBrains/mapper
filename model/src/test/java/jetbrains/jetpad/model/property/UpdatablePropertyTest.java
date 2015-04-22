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
package jetbrains.jetpad.model.property;

import jetbrains.jetpad.model.event.EventHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class UpdatablePropertyTest {
  private String value;
  private UpdatableProperty<String> property;

  @Before
  public void init() {
    property = new UpdatableProperty<String>() {
      @Override
      protected String doGet() {
        return value;
      }

      @Override
      protected void doAddListeners() {
      }

      @Override
      protected void doRemoveListeners() {
      }
    };
  }

  @Test
  public void simpleGet() {
    value = "z";

    assertEquals("z", property.get());
  }

  @Test
  public void getWithListenersDoesntGetWithoutUpdate() {
    value = "a";
    property.addHandler(Mockito.mock(EventHandler.class));
    value = "b";

    assertEquals("a", property.get());
  }

  @Test
  public void updateFiresEvent() {
    EventHandler<? super PropertyChangeEvent<String>> handler = Mockito.mock(EventHandler.class);
    property.addHandler(handler);
    value = "z";

    property.update();

    Mockito.verify(handler).onEvent(new PropertyChangeEvent<>(null, "z"));
  }

  @Test
  public void updateWithoutChangeDoesntFireEvent() {
    EventHandler<? super PropertyChangeEvent<String>> handler = Mockito.mock(EventHandler.class);
    property.addHandler(handler);

    property.update();

    Mockito.verifyNoMoreInteractions(handler);

  }

  @Test
  public void removeAllListenersReturnsToSimpleMode() {
    property.addHandler(Mockito.mock(EventHandler.class)).dispose();

    value = "c";

    assertEquals("c", property.get());
  }

}