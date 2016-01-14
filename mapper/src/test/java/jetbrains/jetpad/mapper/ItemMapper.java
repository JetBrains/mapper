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
package jetbrains.jetpad.mapper;

import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.transform.Transformers;

class ItemMapper extends Mapper<Item, Item> {
  private SimpleRoleSynchronizer<Item, Item> mySimpleRole;

  ItemMapper(Item item) {
    super(item, new Item());
  }

  ItemMapper(Item source, Item target) {
    super(source, target);
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    conf.add(Synchronizers.forObservableRole(this, getSource().observableChildren, getTarget().observableChildren, createMapperFactory()));
    conf.add(Synchronizers.forObservableRole(this, getSource().transformedChildren, Transformers.<Item>identityList(), getTarget().transformedChildren, createMapperFactory()));
    conf.add(Synchronizers.forSingleRole(this, getSource().singleChild, getTarget().singleChild, createMapperFactory()));
    conf.add(mySimpleRole = Synchronizers.forSimpleRole(this, getSource().children, getTarget().children, createMapperFactory()));
    conf.add(Synchronizers.forPropsTwoWay(getSource().name, getTarget().name));
    conf.add(new RoleToPropertyAdapter<Item, Item>(getSource().observableChildrenForAdapter) {
      @Override
      public RoleSynchronizer<Property<Item>, Item> createRoleSynchronizer(ObservableList<Property<Item>> propsSourceList) {
        return Synchronizers.forObservableRole(ItemMapper.this, propsSourceList, getTarget().observableChildrenForAdapter, createPropertyMapperFactory());
      }
    });
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

  protected MapperFactory<Property<Item>, Item> createPropertyMapperFactory() {
    return new MapperFactory<Property<Item>, Item>() {
      @Override
      public Mapper<? extends Property<Item>, ? extends Item> createMapper(Property<Item> source) {
        return new Mapper<Property<Item>, Item>(source, new Item()) {
          @Override
          protected void onAttach(MappingContext ctx) {
            super.onAttach(ctx);
            createChildList().add(new ItemMapper(getSource().get(), getTarget()));
          }
        };
      }
    };
  }
}