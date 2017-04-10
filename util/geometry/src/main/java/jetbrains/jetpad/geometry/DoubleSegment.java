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
package jetbrains.jetpad.geometry;

public class DoubleSegment {
  public final DoubleVector start;
  public final DoubleVector end;

  public DoubleSegment(DoubleVector start, DoubleVector end) {
    this.start = start;
    this.end = end;
  }

  public double distance(DoubleVector v) {
    if (start.equals(end)) {
      return start.subtract(v).length();
    }
    DoubleVector vs = start.subtract(v);
    DoubleVector ve = end.subtract(v);

    if (isDistanceToLineBest(v)) {
      double pVolume = Math.abs(vs.x * ve.y - vs.y * ve.x);
      return pVolume / length();
    } else {
      return Math.min(vs.length(), ve.length());
    }
  }

  private boolean isDistanceToLineBest(DoubleVector v) {
    DoubleVector es = start.subtract(end);
    DoubleVector se = es.negate();
    DoubleVector ev = v.subtract(end);
    DoubleVector sv = v.subtract(start);

    return es.dotProduct(ev) >= 0 && se.dotProduct(sv) >= 0;
  }

  public DoubleVector intersection(DoubleSegment with) {
    DoubleVector o1 = start;
    DoubleVector o2 = with.start;
    DoubleVector d1 = end.subtract(start);
    DoubleVector d2 = with.end.subtract(with.start);

    double td = d1.dotProduct(d2.orthogonal());
    if (td == 0) {
      return null;
    }
    double t = o2.subtract(o1).dotProduct(d2.orthogonal()) / td;
    if (t < 0 || t > 1) {
      return null;
    }

    double sd = d2.dotProduct(d1.orthogonal());
    double s = o1.subtract(o2).dotProduct(d1.orthogonal()) / sd;
    if (s < 0 || s > 1) {
      return null;
    }

    return o1.add(d1.mul(t));
  }

  public double length() {
    return start.subtract(end).length();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof DoubleSegment)) {
      return false;
    }

    DoubleSegment l = (DoubleSegment) o;
    return l.start.equals(start) && l.end.equals(end);
  }

  @Override
  public int hashCode() {
    return start.hashCode() * 31 + end.hashCode();
  }

  @Override
  public String toString() {
    return "[" + start + " -> " + end + "]";
  }
}