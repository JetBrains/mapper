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
package jetbrains.jetpad.model.transform;

/**
 * Factory for a dynamic transformation from a mutable object of type SourceT to a mutable object of TargetT.
 *
 * Such transformations involve listening to the state of object, so we should dispose of them. That's why
 * normal function will not work.
 *
 * You can chain such factories with the andThen method.
 */
public interface Transformer<SourceT, TargetT> {
  Transformation<SourceT, TargetT> transform(SourceT from);
  Transformation<SourceT, TargetT> transform(SourceT from, TargetT to);

  <ParameterTargetT> Transformer<SourceT, ParameterTargetT> andThen(Transformer<TargetT, ParameterTargetT> transformer);
}