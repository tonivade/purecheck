package com.github.tonivade.purecheck;

import com.github.tonivade.purecheck.TestCase.WhenStep;

public interface TestFactory<E, T> {
  
  @SuppressWarnings("unchecked")
  static <E, T> TestFactory<E, T> factory() {
    return (TestFactory<E, T>) TestFactoryModule.INSTANCE;
  }

  default WhenStep<E, T> should(String name) {
    return TestCase.test(name);
  }
}

interface TestFactoryModule {

  @SuppressWarnings("rawtypes")
  TestFactory<?, ?> INSTANCE = new TestFactory() { };
  
}