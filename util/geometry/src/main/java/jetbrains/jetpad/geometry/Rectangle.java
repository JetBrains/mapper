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

import com.google.common.collect.Range;

public class Rectangle {
  public final Vector origin;
  public final Vector dimension;

  public Rectangle(Vector origin, Vector dimension) {
    this.origin = origin;
    this.dimension = dimension;
  }

  public Rectangle(int x, int y, int width, int height) {
    this(new Vector(x, y), new Vector(width, height));
  }

  public Rectangle add(Vector v) {
    return new Rectangle(origin.add(v), dimension);
  }

  public Rectangle sub(Vector v) {
    return new Rectangle(origin.sub(v), dimension);
  }

  public boolean contains(Rectangle r) {
    return contains(r.origin) && contains(r.origin.add(r.dimension));
  }

  public boolean contains(Vector v) {
    return origin.x <= v.x && origin.x + dimension.x >= v.x && origin.y <= v.y && origin.y + dimension.y >= v.y;
  }

  public Rectangle union(Rectangle rect) {
    Vector newOrigin = origin.min(rect.origin);
    Vector corner = origin.add(dimension);
    Vector rectCorner = rect.origin.add(rect.dimension);
    Vector newCorner = corner.max(rectCorner);
    Vector newDimension = newCorner.sub(newOrigin);
    return new Rectangle(newOrigin, newDimension);
  }

  public boolean intersects(Rectangle rect) {
    Vector t1 = origin;
    Vector t2 = origin.add(dimension);
    Vector r1 = rect.origin;
    Vector r2 = rect.origin.add(rect.dimension);
    return r2.x >= t1.x && t2.x >= r1.x && r2.y >= t1.y && t2.y >= r1.y;
  }

  public Rectangle intersect(Rectangle r) {
    if (!intersects(r)) throw new IllegalStateException();

    Vector to = origin;
    Vector ro = r.origin;
    Vector io = to.max(ro);

    Vector too = origin.add(dimension);
    Vector roo = r.origin.add(r.dimension);
    Vector ioo = too.min(roo);

    return new Rectangle(io, ioo.sub(io));
  }

  public boolean innerIntersects(Rectangle rect) {
    Vector t1 = origin;
    Vector t2 = origin.add(dimension);
    Vector r1 = rect.origin;
    Vector r2 = rect.origin.add(rect.dimension);
    return r2.x > t1.x && t2.x > r1.x && r2.y > t1.y && t2.y > r1.y;
  }

  public Rectangle changeDimension(Vector dim) {
    return new Rectangle(origin, dim);
  }

  public double distance(Vector to) {
    return toDoubleRectangle().distance(to.toDoubleVector());
  }

  public Range<Integer> xRange() {
    return Range.closed(origin.x, origin.x + dimension.x);
  }

  public Range<Integer> yRange() {
    return Range.closed(origin.y, origin.y + dimension.y);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Rectangle)) return false;

    Rectangle otherRect = (Rectangle) obj;
    return origin.equals(otherRect.origin) && dimension.equals(otherRect.dimension);
  }

  public DoubleRectangle toDoubleRectangle() {
    return new DoubleRectangle(origin.toDoubleVector(), dimension.toDoubleVector());
  }

  public Vector center() {
    return origin.add(new Vector(dimension.x / 2, dimension.y / 2));
  }

  public Segment[] getBoundSegments() {
    Vector[] p = getBoundPoints();
    return new Segment[]{new Segment(p[0], p[1]), new Segment(p[1], p[2]), new Segment(p[2], p[3]), new Segment(p[3], p[0])};
  }

  public Vector[] getBoundPoints() {
    return new Vector[]{origin, origin.add(new Vector(dimension.x, 0)), origin.add(dimension), origin.add(new Vector(0, dimension.y))};
  }

  @Override
  public String toString() {
    return origin + " - " + dimension;
  }

}