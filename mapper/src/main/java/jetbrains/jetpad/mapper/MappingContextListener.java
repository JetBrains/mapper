package jetbrains.jetpad.mapper;

public interface MappingContextListener {
  void onMapperRegistered(Mapper<?, ?> mapper);
  void onMapperUnregistered(Mapper<?, ?> mapper);
}
