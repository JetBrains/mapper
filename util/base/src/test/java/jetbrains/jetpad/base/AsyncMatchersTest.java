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

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static jetbrains.jetpad.base.AsyncMatchers.failed;
import static jetbrains.jetpad.base.AsyncMatchers.result;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public final class AsyncMatchersTest {

  @Test
  public void resultSucceeded() {
    try {
      assertThat(Asyncs.constant(239), result(is(238)));
      fail();
    } catch (AssertionError e) {
      assertThat(e.getMessage(), is(
          "\n" +
              "Expected: a successful async which result is <238>\n" +
              "     but: result was <239>"
      ));
    }
  }

  @Test
  public void resultFailed() {
    try {
      assertThat(Asyncs.<Integer>failure(new Throwable()), result(is(0)));
      fail();
    } catch (AssertionError e) {
      assertThat(e.getMessage(), is(
          "\n" +
              "Expected: a successful async which result is <0>\n" +
              "     but: failed with exception: <java.lang.Throwable>"
      ));
    }
  }

  @Test
  public void resultUnfinished() {
    SimpleAsync<Integer> first = new SimpleAsync<>();
    SimpleAsync<Integer> second = new SimpleAsync<>();
    Async<List<Integer>> composite = Asyncs.composite(Arrays.<Async<Integer>>asList(first, second));
    first.success(0);
    try {
      assertThat(composite, AsyncMatchers.<List<Integer>>succeeded());
      fail();
    } catch (AssertionError e) {
      assertThat(e.getMessage(), is(
          "\n" +
              "Expected: a successful async which result ANYTHING\n" +
              "     but: isn't finished yet"
      ));
    }
  }

  @Test
  public void failureSucceeded() {
    try {
      assertThat(Asyncs.constant(239), failed());
      fail();
    } catch (AssertionError e) {
      assertThat(e.getMessage(), is(
          "\n" +
              "Expected: a failed async which failure ANYTHING\n" +
              "     but: was a successful async with value: <239>"
      ));
    }
  }

}
