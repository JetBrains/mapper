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
package jetbrains.jetpad.model.event;

import jetbrains.jetpad.base.function.Function;
import jetbrains.jetpad.base.function.Predicate;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EventSourceTest extends BaseTestCase {
  private final EventSource<String> letters = EventSources.of("a", "b", "c", "d", "e");


  @Test
  public void map() {
    assertEvents(Arrays.asList("A", "B", "C", "D", "E"),
      EventSources.map(letters, new Function<String, String>() {
        @Override
        public String apply(String s) {
          return s.toUpperCase();
        }
      }));
  }

  @Test
  public void filter() {
    assertEvents(Arrays.asList("a", "e"),
      EventSources.filter(letters, new Predicate<String>() {
        @Override
        public boolean test(String s) {
          return "a".equals(s) || "e".equals(s);
        }
      }));
  }

  private void assertEvents(List<String> events, EventSource<String> es) {
    LoggingEventHandler<String> handler = new LoggingEventHandler<>();
    es.addHandler(handler);

    assertEquals(events, handler.getEvents());
  }

}