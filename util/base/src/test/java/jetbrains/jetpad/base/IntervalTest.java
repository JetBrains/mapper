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

import static org.junit.Assert.assertEquals;

public final class IntervalTest {

  private static Interval i(int lower, int upper) {
    return new Interval(lower, upper);
  }

  private final Interval i_1_4 = i(1, 4);

  @Test
  public void contains() {
    checkContains(i_1_4);
    checkContains(2, 3);
    checkContains(1, 2);
    checkContains(3, 4);

    checkNotContains(0, 4);
    checkNotContains(1, 5);
    checkNotContains(0, 5);
    checkNotContains(4, 10);
  }

  @Test
  public void intersects() {
    checkIntersects(i_1_4);
    checkIntersects(2, 3);
    checkIntersects(1, 2);
    checkIntersects(3, 4);

    checkIntersects(0, 4);
    checkIntersects(0, 1);
    checkIntersects(2, 5);
    checkIntersects(4, 5);

    checkIntersects(0, 5);

    checkNotIntersects(-1, 0);
    checkNotIntersects(5, 6);
  }

  @Test
  public void union() {
    checkUnion(i(1, 2), i(3, 4));
    checkUnion(i(1, 3), i(2, 4));

    checkUnion(i(3, 4), i(1, 2));
    checkUnion(i(2, 4), i(1, 3));

    checkUnion(i_1_4, i(2, 3));
    checkUnion(i(2, 3), i_1_4);
  }

  @Test
  public void add() {
    checkAdd(i(0, 3), 1);
    checkAdd(i_1_4, 0);
    checkAdd(i(2, 5), -1);
  }

  @Test
  public void sub() {
    checkSub(i(0, 3), -1);
    checkSub(i_1_4, 0);
    checkSub(i(2, 5), 1);
  }

  private void checkContains(Interval interval, boolean expected) {
    assertEquals(expected, i_1_4.contains(interval));
  }

  private void checkContains(Interval interval) {
    checkContains(interval, true);
  }

  private void checkContains(int lower, int upper, boolean expected) {
    checkContains(i(lower, upper), expected);
  }

  private void checkContains(int lower, int upper) {
    checkContains(lower, upper, true);
  }

  private void checkNotContains(int lower, int upper) {
    checkContains(lower, upper, false);
  }

  private void checkIntersects(Interval interval, boolean expected) {
    assertEquals(expected, i_1_4.intersects(interval));
  }

  private void checkIntersects(Interval interval) {
    checkIntersects(interval, true);
  }

  private void checkIntersects(int lower, int upper) {
    checkIntersects(i(lower, upper));
  }

  private void checkNotIntersects(int lower, int upper) {
    checkIntersects(i(lower, upper), false);
  }

  private void assertInterval(Interval interval) {
    assertEquals(i_1_4, interval);
  }

  private void checkUnion(Interval i1, Interval i2) {
    assertInterval(i1.union(i2));
  }

  private void checkAdd(Interval interval, int delta) {
    assertInterval(interval.add(delta));
  }

  private void checkSub(Interval interval, int delta) {
    assertInterval(interval.sub(delta));
  }

}
