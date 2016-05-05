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
package jetbrains.jetpad.model.composite;

/**
 * Composite which supports optimized navigation between siblings.
 *
 * If we store children in an array and a composite has a lot of children, navigation might be
 * expensive because we need to use indexOf which takes O(children). This class is an optimized
 * version of Composite tailored for such cases.
 *
 * Invariants:
 * - prevSibling, nextSibling, firstChild and lastChild must be consistent with the children collection from
 * Composite.
 */
public interface NavComposite<CompositeT extends NavComposite<CompositeT>> extends Composite<CompositeT> {
  CompositeT nextSibling();
  CompositeT prevSibling();

  CompositeT firstChild();
  CompositeT lastChild();
}