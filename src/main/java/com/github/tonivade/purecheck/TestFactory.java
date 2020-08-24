/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import com.github.tonivade.purecheck.TestCase.WhenStep;

public interface TestFactory<E> {
  
  @SuppressWarnings("unchecked")
  static <E> TestFactory<E> factory() {
    return (TestFactory<E>) TestFactoryModule.INSTANCE;
  }

  default <T> WhenStep<E, T> should(String name) {
    return TestCase.test(name);
  }
}

interface TestFactoryModule {

  @SuppressWarnings("rawtypes")
  TestFactory<?> INSTANCE = new TestFactory() { };
  
}