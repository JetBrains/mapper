package jetbrains.jetpad.base.edt;

public interface TaskManagerFactory {
  TaskManager createTaskManager(String taskManagerName);
}
