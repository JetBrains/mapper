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
package jetbrains.jetpad.mapper;

import jetbrains.jetpad.base.Disposable;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public final class ForDisposableTest {

  private final DisposableObject myDisposable1 = new DisposableObject();
  private final DisposableObject myDisposable2 = new DisposableObject();

  @Test
  public void forDisposable() {
    Synchronizer synchronizer = Synchronizers.forDisposable(myDisposable1);
    synchronizer.detach();
    myDisposable1.assertDisposed();
  }

  @Test
  public void forDisposables() {
    Synchronizer synchronizer = Synchronizers.forDisposables(myDisposable1, myDisposable2);
    synchronizer.detach();
    myDisposable1.assertDisposed();
    myDisposable2.assertDisposed();
  }

  private static final class DisposableObject implements Disposable {

    private boolean isDisposed;

    private DisposableObject() {
    }

    @Override
    public void dispose() {
      isDisposed = true;
    }

    private void assertDisposed() {
      assertTrue(isDisposed);
    }

  }

}
