package jetbrains.jetpad.base.edt;

public class RunningTaskManagerFactory implements TaskManagerFactory {
  @Override
  public TaskManager createTaskManager(String taskManagerName) {
    return new RunningTaskManager(taskManagerName);
  }
}
