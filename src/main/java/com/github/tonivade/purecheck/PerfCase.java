/*
 * Copyright (c) 2020-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import static com.github.tonivade.purefun.core.Precondition.checkNonEmpty;
import static com.github.tonivade.purefun.core.Precondition.checkNonNull;
import static com.github.tonivade.purefun.core.Unit.unit;
import static com.github.tonivade.purefun.data.Sequence.listOf;

import java.time.Duration;

import com.github.tonivade.purecheck.spec.IOPerfCase;
import com.github.tonivade.purecheck.spec.TaskPerfCase;
import com.github.tonivade.purecheck.spec.UIOPerfCase;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Witness;
import com.github.tonivade.purefun.core.Producer;
import com.github.tonivade.purefun.core.Tuple;
import com.github.tonivade.purefun.core.Tuple2;
import com.github.tonivade.purefun.core.Unit;
import com.github.tonivade.purefun.data.ImmutableArray;
import com.github.tonivade.purefun.data.ImmutableMap;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.effect.Task;
import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.typeclasses.MonadDefer;
import com.github.tonivade.purefun.typeclasses.Schedule;
import com.github.tonivade.purefun.typeclasses.Schedule.ScheduleOf;

public final class PerfCase<F extends Witness, T> {

  private final String name;
  private final MonadDefer<F> monad;
  private final Kind<F, T> task;
  private final Kind<F, Unit> warmup;

  public PerfCase(String name, MonadDefer<F> monad, Kind<F, T> task, Kind<F, Unit> warmup) {
    this.name = checkNonEmpty(name);
    this.monad = checkNonNull(monad);
    this.task = checkNonNull(task);
    this.warmup = checkNonNull(warmup);
  }

  public PerfCase<F, T> warmup(int times) {
    return new PerfCase<>(name, monad, task, monad.repeat(task, this.<T>recurs(times).unit()));
  }

  public Kind<F, Stats> run(int times) {
    var timed = monad.map(monad.timed(task), Tuple2::get1);
    var repeat = monad.repeat(timed, recursAndCollect(times));
    return monad.andThen(warmup, () -> monad.map(repeat, this::stats));
  }

  private Stats stats(Sequence<Duration> results) {
    ImmutableArray<Duration> array = results.asArray().sort(Duration::compareTo);
    Duration total = array.reduce(Duration::plus).getOrElseThrow();
    return new Stats(
        name,
        total,
        min(array),
        max(array),
        mean(array, total),
        median(array),
        ImmutableMap.from(listOf(
            percentile(50, array),
            percentile(90, array),
            percentile(95, array),
            percentile(99, array))));
  }

  private Schedule<F, Duration, Sequence<Duration>> recursAndCollect(int times) {
    ScheduleOf<F> scheduleOf = monad.scheduleOf();
    return this.<Duration>recurs(times).zipRight(scheduleOf.identity()).collectAll();
  }

  private <A> Schedule<F, A, Integer> recurs(int times) {
    ScheduleOf<F> scheduleOf = monad.scheduleOf();
    return scheduleOf.recurs(times);
  }

  private static Duration mean(ImmutableArray<Duration> array, Duration total) {
    return total.dividedBy(array.size());
  }

  private static Duration median(ImmutableArray<Duration> array) {
    int median = array.size() / 2;
    if (array.size() % 2 == 0) {
      return array.get(median).plus(array.get(median + 1)).dividedBy(2);
    }
    return array.get(median);
  }

  private static Duration max(ImmutableArray<Duration> array) {
    return array.foldLeft(Duration.ZERO, (d1, d2) -> d1.compareTo(d2) > 0 ? d1 : d2);
  }

  private static Duration min(ImmutableArray<Duration> array) {
    return array.foldLeft(Duration.ofDays(365), (d1, d2) -> d1.compareTo(d2) > 0 ? d2 : d1);
  }

  private static Tuple2<Double, Duration> percentile(double percentile, ImmutableArray<Duration> array) {
    return Tuple.of(percentile, array.get((int) Math.round(percentile / 100.0 * (array.size() - 1))));
  }

  public static <T> IOPerfCase<T> ioPerfCase(String name, Producer<T> task) {
    return new IOPerfCase<>(name, IO.task(task));
  }

  public static <T> IOPerfCase<T> ioPerfCase(String name, IO<T> task) {
    return new IOPerfCase<>(name, task);
  }

  public static <T> UIOPerfCase<T> uioPerfCase(String name, Producer<T> task) {
    return new UIOPerfCase<>(name, UIO.task(task));
  }

  public static <T> UIOPerfCase<T> uioPerfCase(String name, UIO<T> task) {
    return new UIOPerfCase<>(name, task);
  }

  public static <T> TaskPerfCase<T> taskPerfCase(String name, Producer<T> task) {
    return new TaskPerfCase<>(name, Task.task(task));
  }

  public static <T> TaskPerfCase<T> taskPerfCase(String name, Task<T> task) {
    return new TaskPerfCase<>(name, task);
  }

  public static <F extends Witness, T> PerfCase<F, T> perfCase(String name, MonadDefer<F> monad, Producer<T> task) {
    return perfCase(name, monad, monad.later(task));
  }

  public static <F extends Witness, T> PerfCase<F, T> perfCase(String name, MonadDefer<F> monad, Kind<F, T> task) {
    return new PerfCase<>(name, monad, task, monad.pure(unit()));
  }

  public record Stats(
    String name,
    Duration total,
    Duration min,
    Duration max,
    Duration mean,
    Duration median,
    ImmutableMap<Double, Duration> percentiles) {

    public Stats {
      checkNonEmpty(name);
      checkNonNull(total);
      checkNonNull(min);
      checkNonNull(max);
      checkNonNull(mean);
      checkNonNull(median);
      checkNonNull(percentiles);
    }

    public Option<Duration> getPercentile(double percentile) {
      return percentiles.get(percentile);
    }

    @Override
    public String toString() {
      return String.format("Stats[name=%s,total=%s,min=%s,max=%s,mean=%s,median=%s,%s]",
          name, total, min, max, mean, median, percentiles.entries().map(
              t -> String.format("p%s=%s", t.get1(), t.get2())).join(","));
    }
  }
}