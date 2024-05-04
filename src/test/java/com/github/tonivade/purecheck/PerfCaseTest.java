/*
 * Copyright (c) 2020-2023, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import static com.github.tonivade.purecheck.PerfCase.ioPerfCase;
import static com.github.tonivade.purefun.core.Validator.lowerThan;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purecheck.PerfCase.Stats;
import com.github.tonivade.purecheck.spec.IOPerfCase;
import com.github.tonivade.purecheck.spec.IOTestSpec;
import com.github.tonivade.purefun.core.Producer;
import com.github.tonivade.purefun.data.Range;

class PerfCaseTest extends IOTestSpec<String> {

  final IOPerfCase<Integer> task = ioPerfCase("test", task()).warmup(10);

  @Test
  void perfTest() {
    var suite = suite("stats test",
        it.should("do some work")
          .given(1000)
          .whenK(task::run)
          .then(lowerThan(Duration.ofMillis(1), () -> "total time less than 1ms").compose(PerfCase.Stats::total)));

    var error = assertThrows(AssertionError.class, () -> suite.run().assertion());

    error.printStackTrace();
  }

  @Test
  void testName() {
    Stats stats = task.run(1000).unsafeRunSync();

    System.out.println(stats);
  }

  private Producer<Integer> task() {
    return () -> Range.of(1, 100).collect().foldLeft(0, Integer::sum);
  }
}
