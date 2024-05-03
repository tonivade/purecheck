/*
 * Copyright (c) 2020-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;


import com.github.tonivade.purefun.typeclasses.MonadDefer;

public interface TestFactory<F> {
  
  static <F> TestFactory<F> factory(MonadDefer<F> monad) {
    return () -> monad;
  }
  
  MonadDefer<F> monad();

  default TestCase.GivenStep<F> should(String name) {
    return TestCase.test(monad(), name);
  }
}