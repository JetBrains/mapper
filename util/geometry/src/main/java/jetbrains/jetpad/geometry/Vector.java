/*
 * Copyright 2012-2015 JetBrains s.r.o
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

public class Vector {
  public static final Vector ZERO = new Vector(0, 0);

  public final int x;
  public final int y;

  public Vector(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public Vector add(Vector v) {
    return new Vector(x + v.x, y + v.y);
  }

  public Vector sub(Vector v) {
    return add(v.negate());
  }

  public Vector negate() {
    return new Vector(-x, -y);
  }

  public Vector max(Vector v) {
    return new Vector(Math.max(x, v.x), Math.max(y, v.y));
  }

  public Vector min(Vector v) {
    return new Vector(Math.min(x, v.x), Math.min(y, v.y));
  }

  public Vector mul(int i) {
    return new Vector(x * i, y * i);
  }

  public Vector div(int i) {
    return new Vector(x / i, y / i);
  }

  public int dotProduct(Vector v) {
    return x * v.x + y * v.y;
  }

  public double length() {
    return Math.sqrt(x * x + y * y);
  }

  public DoubleVector toDoubleVector() {
    return new DoubleVector(x, y);
  }

  public Vector abs() {
    return new Vector(Math.abs(x), Math.abs(y));
  }

  public boolean isParallel(Vector to) {
    return x * to.y - to.x * y == 0;
  }

  public Vector orthogonal() {
    return new Vector(-y, x);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Vector)) return false;

    Vector otherVector = (Vector) obj;
    return x == otherVector.x && y == otherVector.y;
  }

  @Override
  public int hashCode() {
    return x * 31 + y;
  }

  @Override
  public String toString() {
    return "(" + x + ", " + y + ")";
  }
}