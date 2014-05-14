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

public class DoubleVector {
  public static final DoubleVector ZERO = new DoubleVector(0.0, 0.0);

  public final double x;
  public final double y;

  public DoubleVector(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public DoubleVector add(DoubleVector v) {
    return new DoubleVector(x + v.x, y + v.y);
  }

  public DoubleVector subtract(DoubleVector v) {
    return new DoubleVector(x - v.x, y - v.y);
  }

  public DoubleVector max(DoubleVector v) {
    return new DoubleVector(Math.max(x, v.x), Math.max(y, v.y));
  }

  public DoubleVector min(DoubleVector v) {
    return new DoubleVector(Math.min(x, v.x), Math.min(y, v.y));
  }

  public DoubleVector mul(double value) {
    return new DoubleVector(x * value, y * value);
  }

  public double dotProduct(DoubleVector v) {
    return x * v.x + y * v.y;
  }

  public DoubleVector negate() {
    return new DoubleVector(-x, -y);
  }

  public DoubleVector orthogonal() {
    return new DoubleVector(-y, x);
  }

  public double length() {
    return Math.sqrt(x * x + y * y);
  }

  public DoubleVector normalize() {
    return mul(1 / length());
  }

  public DoubleVector rotate(double phi) {
    double x = this.x * Math.cos(phi) - y * Math.sin(phi);
    double y = this.x * Math.sin(phi) + this.y * Math.cos(phi);
    return new DoubleVector(x, y);
  }

  public boolean equals(Object o) {
    if (!(o instanceof DoubleVector)) {
      return false;
    }
    DoubleVector v = (DoubleVector) o;
    return v.x == x && v.y == y;
  }

  public int hashCode() {
    return new Double(x).hashCode() + 31 * new Double(y).hashCode();
  }

  public String toString() {
    return "(" + x + ", " + y + ")";
  }
}