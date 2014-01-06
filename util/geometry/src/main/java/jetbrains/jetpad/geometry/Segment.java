/*
 * Copyright 2012-2014 JetBrains s.r.o
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

public class Segment {
  public final Vector start;
  public final Vector end;

  public Segment(Vector start, Vector end) {
    this.start = start;
    this.end = end;
  }

  public double distance(Vector v) {
    Vector vs = start.sub(v);
    Vector ve = end.sub(v);

    if (isDistanceToLineBest(v)) {
      double pVolume = Math.abs(vs.x * ve.y - vs.y * ve.x);
      return pVolume / length();
    } else {
      return Math.min(vs.toDoubleVector().length(), ve.toDoubleVector().length());
    }
  }

  private boolean isDistanceToLineBest(Vector v) {
    Vector es = start.sub(end);
    Vector se = es.negate();
    Vector ev = v.sub(end);
    Vector sv = v.sub(start);

    return es.dotProduct(ev) >= 0 && se.dotProduct(sv) >= 0;
  }

  public DoubleSegment toDoubleSegment() {
    return new DoubleSegment(start.toDoubleVector(), end.toDoubleVector());
  }

  public DoubleVector intersection(Segment with) {
    return toDoubleSegment().intersection(with.toDoubleSegment());
  }

  public double length() {
    return start.sub(end).length();
  }

  public boolean contains(Vector v) {
    Vector p1 = v.sub(start);
    Vector p2 = v.sub(end);
    if (p1.isParallel(p2)) {
      return p1.dotProduct(p2) <= 0;
    }
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Segment)) {
      return false;
    }

    Segment l = (Segment) o;
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