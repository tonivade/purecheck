/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

public interface TestFactory {
  
  @SuppressWarnings("unchecked")
  static <E> TestFactory factory() {
    return TestFactoryModule.INSTANCE;
  }

  default TestCase.GivenStep should(String name) {
    return TestCase.test(name);
  }
}

interface TestFactoryModule {

  TestFactory INSTANCE = new TestFactory() { };
  
}