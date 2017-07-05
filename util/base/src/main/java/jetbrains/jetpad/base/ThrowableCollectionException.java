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
package jetbrains.jetpad.base;

import java.util.ArrayList;
import java.util.List;

public class ThrowableCollectionException extends RuntimeException {
  private List<Throwable> myThrowables = new ArrayList<>();

  public ThrowableCollectionException(List<Throwable> throwables) {
    super("size=" + throwables.size(), throwables.get(0));
    myThrowables.addAll(throwables);
  }

  public ThrowableCollectionException(String message, List<Throwable> throwables) {
    super(message + "; size=" + throwables.size(), throwables.get(0));
    myThrowables.addAll(throwables);
  }

  public ThrowableCollectionException() {
  }

  public List<Throwable> getThrowables() {
    return myThrowables;
  }
}