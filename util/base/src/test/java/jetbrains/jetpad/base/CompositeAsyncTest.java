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
package jetbrains.jetpad.base;

import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CompositeAsyncTest {
  private static final int SIZE = 2;

  @Test
  public void successOneByOne() {
    List<Async<Integer>> asyncs = new ArrayList<>(SIZE);
    for (int i = 0; i < SIZE; i++) {
      asyncs.add(new SimpleAsync<Integer>());
    }
    Async<List<Integer>> async = Asyncs.composite(asyncs);

    for (int i = 0; i < asyncs.size() - 1; i++) {
      ((SimpleAsync<Integer>)asyncs.get(i)).success(i);
    }
    assertThat(async, AsyncMatchers.<List<Integer>>unfinished());

    ((SimpleAsync<Integer>)asyncs.get(SIZE - 1)).success(SIZE - 1);
    assertThat(async, AsyncMatchers.<List<Integer>>succeeded());
  }

  @Test
  public void alreadySucceeded() {
    List<Async<Integer>> asyncs = new ArrayList<>(SIZE);
    for (int i = 0; i < SIZE; i++) {
      asyncs.add(Asyncs.constant(i));
    }
    assertThat(Asyncs.composite(asyncs), AsyncMatchers.<List<Integer>>succeeded());
  }

  @Test
  public void emptyRequest() {
    assertThat(
        Asyncs.composite(new ArrayList<Async<Integer>>(0)),
        AsyncMatchers.<List<Integer>>result(Matchers.hasSize(0)));
  }

  @Test
  public void partialFailureSingleException() {
    List<Async<Integer>> asyncs = new ArrayList<>(SIZE);
    for (int i = 0; i < SIZE; i++) {
      asyncs.add(new SimpleAsync<Integer>());
    }
    Async<List<Integer>> async = Asyncs.composite(asyncs);

    for (int i = 0; i < asyncs.size() - 1; i++) {
      ((SimpleAsync<Integer>)asyncs.get(i)).success(i);
    }
    assertThat(async, AsyncMatchers.<List<Integer>>unfinished());

    IllegalStateException failure = new IllegalStateException("test");
    ((SimpleAsync<Integer>)asyncs.get(SIZE - 1)).failure(failure);
    assertThat(async, AsyncMatchers.<List<Integer>, IllegalStateException>failure(sameInstance(failure)));
  }

  @Test
  public void partialFailureSeveralExceptions() {
    List<Async<Integer>> asyncs = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      asyncs.add(new SimpleAsync<Integer>());
    }
    Async<List<Integer>> async = Asyncs.composite(asyncs);

    for (int i = 0; i < asyncs.size() - 1; i++) {
      ((SimpleAsync<Integer>)asyncs.get(i)).failure(new IllegalStateException("" + i));
    }
    assertThat(async, AsyncMatchers.<List<Integer>>unfinished());

    ((SimpleAsync<Integer>)asyncs.get(2)).success(2);
    assertThat(async, AsyncMatchers.<List<Integer>, ThrowableCollectionException>failure(
        new CustomTypeSafeMatcher<ThrowableCollectionException>("collection of throwables") {
          @Override
          protected boolean matchesSafely(ThrowableCollectionException failure) {
            List<Throwable> throwables = failure.getThrowables();
            assertEquals(2, throwables.size());
            List<String> expected = Arrays.asList("0", "1");
            List<String> actual = Arrays.asList(throwables.get(0).getMessage(), throwables.get(1).getMessage());
            return expected.equals(actual);
          }
        }
    ));
  }

  @Test
  public void resultListOrder() {
    List<Async<Integer>> asyncs = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      asyncs.add(new SimpleAsync<Integer>());
    }

    ((SimpleAsync<Integer>)asyncs.get(1)).success(1);
    ((SimpleAsync<Integer>)asyncs.get(2)).success(2);
    ((SimpleAsync<Integer>)asyncs.get(0)).success(0);

    assertThat(Asyncs.composite(asyncs),
        AsyncMatchers.<List<Integer>>result(Matchers.contains(0, 1, 2)));
  }
}
