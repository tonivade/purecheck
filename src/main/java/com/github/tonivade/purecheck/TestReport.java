package com.github.tonivade.purecheck;

import static com.github.tonivade.purefun.Precondition.checkNonNull;

import com.github.tonivade.purefun.Consumer1;
import com.github.tonivade.purefun.data.Sequence;

public class TestReport<E> {

  private final Sequence<TestResult<E, ?>> results;

  public TestReport(Sequence<TestResult<E, ?>> results) {
    this.results = checkNonNull(results);
  }
  
  @Override
  public String toString() {
    return results.join("\n");
  }

  public void forEach(Consumer1<? super TestResult<E, ?>> consumer) {
    results.forEach(consumer::accept);
  }
}
