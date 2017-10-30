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

import jetbrains.jetpad.model.transform.Transformers;

class ItemMapper extends Mapper<Item, Item> {
  private SimpleRoleSynchronizer<Item, Item> mySimpleRole;

  ItemMapper(Item item) {
    super(item, new Item());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    conf.add(Synchronizers.forObservableRole(
        this, getSource().getObservableChildren(), getTarget().getObservableChildren(), createMapperFactory()));
    conf.add(Synchronizers.forObservableRole(
        this, getSource().getTransformedChildren(), Transformers.<Item>identityList(),
        getTarget().getTransformedChildren(), createMapperFactory()));
    conf.add(Synchronizers.forSingleRole(
        this, getSource().getSingleChild(), getTarget().getSingleChild(), createMapperFactory()));
    conf.add(mySimpleRole = Synchronizers.forSimpleRole(
        this, getSource().getChildren(), getTarget().getChildren(), createMapperFactory()));
    conf.add(Synchronizers.forPropsTwoWay(getSource().getName(), getTarget().getName()));
  }

  public void refreshSimpleRole() {
    mySimpleRole.refresh();
  }

  protected MapperFactory<Item, Item> createMapperFactory() {
    return new MapperFactory<Item, Item>() {
      @Override
      public Mapper<? extends Item, ? extends Item> createMapper(Item source) {
        return new ItemMapper(source);
      }
    };
  }
}