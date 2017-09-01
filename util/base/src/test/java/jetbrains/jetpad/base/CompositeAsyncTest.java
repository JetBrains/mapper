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
package jetbrains.jetpad.base;

import jetbrains.jetpad.test.BaseTestCase;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static jetbrains.jetpad.base.AsyncMatchers.failure;
import static jetbrains.jetpad.base.AsyncMatchers.unfinished;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CompositeAsyncTest extends BaseTestCase {
  private SimpleAsync<Integer> first;
  private SimpleAsync<Integer> second;
  private Async<List<Integer>> composite;

  @Before
  public void setup() {
    first = new SimpleAsync<>();
    second = new SimpleAsync<>();
    composite = Asyncs.composite(Arrays.<Async<Integer>>asList(first, second));
  }

  @Test
  public void successOneByOne() {
    first.success(0);
    assertThat(composite, unfinished());

    second.success(1);
    assertThat(composite, AsyncMatchers.<List<Integer>>succeeded());
  }

  @Test
  public void alreadySucceeded() {
    assertThat(Asyncs.composite(Collections.singletonList(Asyncs.constant(0))),
        AsyncMatchers.<List<Integer>>succeeded());
  }

  @Test
  public void emptyRequest() {
    assertThat(Asyncs.composite(Collections.<Async<Integer>>emptyList()),
        AsyncMatchers.<List<Integer>>result(Matchers.hasSize(0)));
  }

  @Test
  public void failWithSingleException() {
    first.success(0);
    assertThat(composite, unfinished());

    IllegalStateException failure = new IllegalStateException("test");
    second.failure(failure);
    assertThat(composite, failure(sameInstance(failure)));
  }

  @Test
  public void failWithSeveralExceptions() {
    first.failure(new RuntimeException("0"));
    second.failure(new RuntimeException("1"));
    assertThat(composite, failure(
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
    second.success(1);
    first.success(0);
    assertThat(composite, AsyncMatchers.<List<Integer>>result(Matchers.contains(0, 1)));
  }
}
