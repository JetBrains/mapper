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

import com.google.common.collect.Range;

import java.util.List;

import static java.util.Arrays.asList;

public class DoubleRectangle {
  public static DoubleRectangle span(DoubleVector leftTop, DoubleVector rightBottom) {
    return new DoubleRectangle(leftTop, rightBottom.subtract(leftTop));
  }
  public final DoubleVector origin;
  public final DoubleVector dimension;

  public DoubleRectangle(double x, double y, double w, double h) {
    this(new DoubleVector(x, y), new DoubleVector(w, h));
  }

  public DoubleRectangle(DoubleVector origin, DoubleVector dimension) {
    this.origin = origin;
    this.dimension = dimension;
  }

  public DoubleVector getCenter() {
    return origin.add(dimension.mul(0.5));
  }

  public double getLeft() {
    return origin.x;
  }

  public double getRight() {
    return origin.x + dimension.x;
  }

  public double getTop() {
    return origin.y;
  }

  public double getBottom() {
    return origin.y + dimension.y;
  }

  public double getWidth() {
    return dimension.x;
  }

  public double getHeight() {
    return dimension.y;
  }

  public Range<Double> xRange() {
    return Range.closed(origin.x, origin.x + dimension.x);
  }

  public Range<Double> yRange() {
    return Range.closed(origin.y, origin.y + dimension.y);
  }

  public boolean contains(DoubleVector v) {
    return origin.x <= v.x && origin.x + dimension.x >= v.x && origin.y <= v.y && origin.y + dimension.y >= v.y;
  }

  public DoubleRectangle union(DoubleRectangle rect) {
    DoubleVector newOrigin = origin.min(rect.origin);
    DoubleVector corner = origin.add(dimension);
    DoubleVector rectCorner = rect.origin.add(rect.dimension);
    DoubleVector newCorner = corner.max(rectCorner);
    DoubleVector newDimension = newCorner.subtract(newOrigin);
    return new DoubleRectangle(newOrigin, newDimension);
  }

  public boolean intersects(DoubleRectangle rect) {
    DoubleVector t1 = origin;
    DoubleVector t2 = origin.add(dimension);
    DoubleVector r1 = rect.origin;
    DoubleVector r2 = rect.origin.add(rect.dimension);
    return r2.x >= t1.x && t2.x >= r1.x && r2.y >= t1.y && t2.y >= r1.y;
  }

  public DoubleRectangle intersect(DoubleRectangle r) {
    DoubleVector t1 = origin;
    DoubleVector t2 = origin.add(dimension);
    DoubleVector r1 = r.origin;
    DoubleVector r2 = r.origin.add(r.dimension);

    DoubleVector res1 = t1.max(r1);
    DoubleVector res2 = t2.min(r2);

    DoubleVector dim = res2.subtract(res1);

    if (dim.x < 0 || dim.y < 0) {
      return null;
    }

    return new DoubleRectangle(res1, dim);
  }

  public DoubleRectangle add(DoubleVector v) {
    return new DoubleRectangle(origin.add(v), dimension);
  }

  public DoubleRectangle subtract(DoubleVector v) {
    return new DoubleRectangle(origin.subtract(v), dimension);
  }

  public double distance(DoubleVector to) {
    double result = 0.0;
    boolean hasResult = false;
    for (DoubleSegment s : getParts()) {
      if (!hasResult) {
        result = s.distance(to);
        hasResult = true;
      } else {
        double distance = s.distance(to);
        if (distance < result) {
          result = distance;
        }
      }
    }
    return result;
  }

  public DoubleSegment getLeftEdge() {
    return new DoubleSegment(
        origin,
        new DoubleVector(getLeft(), getBottom())
    );
  }

  public DoubleSegment getTopEdge() {
    return new DoubleSegment(
        origin,
        new DoubleVector(getRight(), getTop())
    );
  }

  public DoubleSegment getRightEdge() {
    return new DoubleSegment(
        origin.add(dimension),
        new DoubleVector(getRight(), getTop())
    );
  }

  public DoubleSegment getBottomEdge() {
    return new DoubleSegment(
        origin.add(dimension),
        new DoubleVector(getLeft(), getBottom())
    );
  }

  public List<DoubleSegment> getParts() {
    return asList(getTopEdge(), getLeftEdge(), getRightEdge(), getBottomEdge());
  }

  @Override
  public int hashCode() {
    return origin.hashCode() * 31 + dimension.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof DoubleRectangle)) {
      return false;
    }
    DoubleRectangle r = (DoubleRectangle) o;
    return r.origin.equals(origin) && r.dimension.equals(dimension);
  }

  @Override
  public String toString() {
    return "[rect " + origin + ", " + dimension + "]";
  }
}