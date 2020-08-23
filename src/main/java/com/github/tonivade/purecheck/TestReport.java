/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import static com.github.tonivade.purefun.Precondition.checkNonEmpty;
import static com.github.tonivade.purefun.Precondition.checkNonNull;

import com.github.tonivade.purefun.data.Sequence;

public class TestReport<E> {

  private final String name;
  private final Sequence<TestResult<E, ?>> results;

  public TestReport(String name, Sequence<TestResult<E, ?>> results) {
    this.name = checkNonEmpty(name);
    this.results = checkNonNull(results);
  }
  
  @Override
  public String toString() {
    return results.join("\n  - ", "test suite '" + name + "' {\n  - ", "}");
  }

  public void assertion() {
    results.forEach(TestResult::assertion);
  }
}
