package com.github.tonivade.purecheck.spec;

import com.github.tonivade.purecheck.PerfCase;
import com.github.tonivade.purecheck.PerfCase.Stats;
import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.effect.UIOOf;
import com.github.tonivade.purefun.effect.UIO_;
import com.github.tonivade.purefun.typeclasses.Instances;

public final class UIOPerfCase<T> {
  
  private final PerfCase<UIO_, T> perfCase;

  public UIOPerfCase(String name, UIO<T> task) {
    this.perfCase = new PerfCase<>(name, Instances.monadDefer(UIO_.class), task, UIO.unit());
  }
  
  private UIOPerfCase(PerfCase<UIO_, T> perfCase) {
    this.perfCase = perfCase;
  }
  
  public UIOPerfCase<T> warmup(int times) {
    return new UIOPerfCase<>(perfCase.warmup(times));
  }
  
  public UIO<Stats> run(int times) {
    return perfCase.run(times).fix(UIOOf.toUIO());
  }
}
