/*
 * Copyright 2012-2013 JetBrains s.r.o
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
package jetbrains.jetpad.values;

public class Range<ItemT extends Comparable<ItemT>> {
  public final ItemT start;
  public final ItemT end;

  public Range(ItemT start, ItemT end) {
    if (start.compareTo(end) > 0) throw new IllegalArgumentException();
    this.start = start;
    this.end = end;
  }

  public boolean contains(ItemT t) {
    if (t == null) return false;
    return t.compareTo(start) >= 0 && end.compareTo(t) >= 0;
  }

  public boolean strictlyContains(ItemT t) {
    return t.compareTo(start) > 0 && end.compareTo(t) > 0;
  }

  public boolean contains(Range<ItemT> r) {
    return contains(r.start) && contains(r.end);
  }

  public boolean isEmpty() {
    return start.equals(end);
  }

  public Range<ItemT> intersect(Range<ItemT> r) {
    if (r.equals(this)) {
      return this;
    }
    ItemT start = max(this.start, r.start);
    ItemT end = min(this.end, r.end);
    if (end.compareTo(start) >= 0) {
      return new Range<ItemT>(start, end);
    }
    return null;
  }

  private ItemT max(ItemT t1, ItemT t2) {
    return t1.compareTo(t2) >= 0 ? t1 : t2;
  }

  private ItemT min(ItemT t1, ItemT t2) {
    return t1.compareTo(t2) >= 0 ? t2 : t1;
  }

  public ItemT min() {
    return min(start, end);
  }

  public ItemT max() {
    return max(start, end);
  }

  @Override
  public int hashCode() {
    return start.hashCode() * 31 + end.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof Range)) {
      return false;
    }

    Range other = (Range) object;
    return other.start.equals(start) && other.end.equals(end);
  }

  @Override
  public String toString() {
    return "[" + start + "-" + end + "]";
  }
}
