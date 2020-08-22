package com.github.tonivade.purecheck;

import com.github.tonivade.purecheck.TestCase.WhenStep;

public interface TestFactory<E, T> {

  default WhenStep<E, T> should(String name) {
    return TestCase.test(name);
  }
}