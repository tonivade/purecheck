package com.github.tonivade.purecheck.spec;

import com.github.tonivade.purecheck.PerfCase;
import com.github.tonivade.purecheck.PerfCase.Stats;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.monad.IOOf;
import com.github.tonivade.purefun.typeclasses.Instances;

public final class IOPerfCase<T> {

  private final PerfCase<IO<?>, T> perfCase;

  public IOPerfCase(String name, IO<T> task) {
    this.perfCase = new PerfCase<>(name, Instances.monadDefer(), task, IO.unit());
  }

  private IOPerfCase(PerfCase<IO<?>, T> perfCase) {
    this.perfCase = perfCase;
  }

  public IOPerfCase<T> warmup(int times) {
    return new IOPerfCase<>(perfCase.warmup(times));
  }

  public IO<Stats> run(int times) {
    return perfCase.run(times).fix(IOOf.toIO());
  }
}
