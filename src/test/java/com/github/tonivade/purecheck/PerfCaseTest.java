/*
 * Copyright (c) 2020-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import static com.github.tonivade.purecheck.PerfCase.ioPerfCase;
import static com.github.tonivade.purefun.Validator.lowerThan;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purecheck.spec.IOTestSpec;
import com.github.tonivade.purefun.Producer;
import com.github.tonivade.purefun.data.Range;
import com.github.tonivade.purefun.monad.IO_;

class PerfCaseTest extends IOTestSpec<String> {

  @Test
  void perfTest() {
    TestSuite<IO_, String> suite = suite("stats test", 
        it.should("do some work")
          .given(1000)
          .run(ioPerfCase("test", task()).warmup(10)::run)
          .thenMustBe(lowerThan(Duration.ofMillis(1), () -> "total time less than 1ms").compose(PerfCase.Stats::getTotal)));
    
    AssertionError error = assertThrows(AssertionError.class, () -> suite.run().assertion());
    
    error.printStackTrace();
  }

  private Producer<Integer> task() {
    return () -> Range.of(1, 100).collect().foldLeft(0, Integer::sum);
  }
}
