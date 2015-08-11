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
package jetbrains.jetpad.model.transform;

import jetbrains.jetpad.base.Disposable;

public abstract class Transformation<SourceT, TargetT> implements Disposable {
  private boolean myDisposed;

  public abstract SourceT getSource();
  public abstract TargetT getTarget();

  @Override
  public final void dispose() {
    if (myDisposed) {
      throw new IllegalStateException("Already disposed");
    }
    try {
      doDispose();
    } finally {
      myDisposed = true;
    }
  }

  protected void doDispose() {
  }
}