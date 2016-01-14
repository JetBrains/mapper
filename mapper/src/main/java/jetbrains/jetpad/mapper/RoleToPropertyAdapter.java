package jetbrains.jetpad.mapper;

import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.transform.Transformation;
import jetbrains.jetpad.model.transform.Transformers;

public abstract class RoleToPropertyAdapter<SourceT, TargetT> implements Synchronizer {
  private final ObservableList<SourceT> mySourceList;
  private Transformation<ObservableList<SourceT>, ObservableList<Property<SourceT>>> myTransformation;
  private RoleSynchronizer<Property<SourceT>, TargetT> myRoleSync;

  public RoleToPropertyAdapter(ObservableList<SourceT> sourceList) {
    mySourceList = sourceList;
  }

  @Override
  public void attach(SynchronizerContext ctx) {
    myTransformation = Transformers.<SourceT>toPropsListTwoWay().transform(mySourceList);
    myRoleSync = createRoleSynchronizer(myTransformation.getTarget());
    myRoleSync.attach(ctx);
  }

  @Override
  public void detach() {
    myRoleSync.detach();
    myTransformation.dispose();
  }

  public abstract RoleSynchronizer<Property<SourceT>, TargetT> createRoleSynchronizer(ObservableList<Property<SourceT>> propsSourceList);

  public RoleSynchronizer<Property<SourceT>, TargetT> getRoleSynchronizer() {
    return myRoleSync;
  }
}
