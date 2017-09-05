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
package jetbrains.jetpad.geometry;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DistanceTest {
  @Test
  public void simpleDistance() {
    double dist = new DoubleSegment(new DoubleVector(0, 0), new DoubleVector(0, 25)).distance(new DoubleVector(20, 30));
    double dist2 = new DoubleSegment(new DoubleVector(0, 25), new DoubleVector(0, 50))
        .distance(new DoubleVector(20, 30));
    assertTrue(dist2 < dist);
    double dist3 = new DoubleRectangle(new DoubleVector(50, 10), new DoubleVector(601, 25))
        .distance(new DoubleVector(676, 42));
    double dist4 = new DoubleRectangle(new DoubleVector(50, 35), new DoubleVector(601, 42))
        .distance(new DoubleVector(676, 42));
    assertTrue(dist4 < dist3);
  }
}