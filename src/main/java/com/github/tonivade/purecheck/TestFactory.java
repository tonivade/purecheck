/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import com.github.tonivade.purefun.Witness;
import com.github.tonivade.purefun.typeclasses.MonadDefer;

public interface TestFactory<F extends Witness> {
  
  static <F extends Witness> TestFactory<F> factory(MonadDefer<F> monad) {
    return () -> monad;
  }
  
  MonadDefer<F> monad();

  default TestCaseK.GivenStep<F> should(String name) {
    return TestCaseK.test(monad(), name);
  }
}