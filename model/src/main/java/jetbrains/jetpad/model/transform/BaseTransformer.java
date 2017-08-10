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

public abstract class BaseTransformer<SourceT, TargetT> implements Transformer<SourceT, TargetT> {
  @Override
  public <ParameterTargetT> Transformer<SourceT, ParameterTargetT> andThen(final Transformer<TargetT, ParameterTargetT> transformer) {
    final Transformer<SourceT, TargetT> firstTransformer = this;
    return new BaseTransformer<SourceT, ParameterTargetT>() {
      @Override
      public Transformation<SourceT, ParameterTargetT> transform(SourceT source) {
        final Transformation<SourceT, TargetT> tn1 = firstTransformer.transform(source);
        final Transformation<TargetT, ParameterTargetT> tn2 = transformer.transform(tn1.getTarget());
        return createTransformation(tn1, tn2);
      }

      @Override
      public Transformation<SourceT, ParameterTargetT> transform(SourceT source, ParameterTargetT to) {
        final Transformation<SourceT, TargetT> tn1 = firstTransformer.transform(source);
        final Transformation<TargetT, ParameterTargetT> tn2 = transformer.transform(tn1.getTarget(), to);
        return createTransformation(tn1, tn2);
      }

      private Transformation<SourceT, ParameterTargetT> createTransformation(final Transformation<SourceT, TargetT> tn1, final Transformation<TargetT, ParameterTargetT> tn2) {
        return new Transformation<SourceT, ParameterTargetT>() {
          @Override
          public SourceT getSource() {
            return tn1.getSource();
          }

          @Override
          public ParameterTargetT getTarget() {
            return tn2.getTarget();
          }

          @Override
          protected void doDispose() {
            tn1.dispose();
            tn2.dispose();
          }
        };
      }
    };
  }
}