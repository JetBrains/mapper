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
package java.util.concurrent.atomic;

import java.io.Serializable;
import java.util.Objects;

/**
 * 'super-source'
 * Partial implementation of java.util.concurrent.atomic.AtomicReference, which is missing in GWT emulator library.
 * This way we can use AtomicReference class in GWT client code and in pure java.
 * This implementation is not thread safe because browser js interpreters are single-threaded.
 */

public class AtomicReference<ItemT> implements Serializable {
  private ItemT value;

  public AtomicReference(ItemT initialValue) {
    value = initialValue;
  }

  public AtomicReference() {
  }

  public final ItemT get() {
    return value;
  }

  public final void set(ItemT newValue) {
    value = newValue;
  }

  public final boolean compareAndSet(ItemT expect, ItemT update) {
    if (Objects.equals(value, expect)) {
      value = update;
      return true;
    }
    return false;
  }

  public final ItemT getAndSet(ItemT newValue) {
    ItemT current = this.value;
    value = newValue;
    return current;
  }

  public String toString() {
    return String.valueOf(get());
  }
}