package jetbrains.jetpad.model.collections;

public abstract class RelayCollectionListener<SourceItemT, TargetItemT> implements CollectionListener<SourceItemT> {
  private CollectionListener<TargetItemT> myRelayTo;

  public RelayCollectionListener(CollectionListener relayTo) {
    myRelayTo = relayTo;
  }

  @Override
  public void onItemAdded(CollectionItemEvent<? extends SourceItemT> event) {
    myRelayTo.onItemAdded(transform(event));
  }

  @Override
  public void onItemSet(CollectionItemEvent<? extends SourceItemT> event) {
    myRelayTo.onItemSet(transform(event));
  }

  @Override
  public void onItemRemoved(CollectionItemEvent<? extends SourceItemT> event) {
    myRelayTo.onItemRemoved(transform(event));
  }

  protected abstract CollectionItemEvent<? extends TargetItemT> transform(CollectionItemEvent<? extends SourceItemT> event);


}
