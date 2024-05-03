package com.github.tonivade.purecheck.spec;

import com.github.tonivade.purecheck.PerfCase;
import com.github.tonivade.purecheck.PerfCase.Stats;
import com.github.tonivade.purefun.effect.Task;
import com.github.tonivade.purefun.effect.TaskOf;
import com.github.tonivade.purefun.typeclasses.Instances;

public final class TaskPerfCase<T> {

  private final PerfCase<Task<?>, T> perfCase;

  public TaskPerfCase(String name, Task<T> task) {
    this.perfCase = new PerfCase<>(name, Instances.monadDefer(), task, Task.unit());
  }

  private TaskPerfCase(PerfCase<Task<?>, T> perfCase) {
    this.perfCase = perfCase;
  }

  public TaskPerfCase<T> warmup(int times) {
    return new TaskPerfCase<>(perfCase.warmup(times));
  }

  public Task<Stats> run(int times) {
    return perfCase.run(times).fix(TaskOf.toTask());
  }
}
