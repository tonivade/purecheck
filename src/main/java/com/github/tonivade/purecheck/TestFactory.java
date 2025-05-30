/*
 * Copyright (c) 2020-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.typeclasses.MonadDefer;

public interface TestFactory<F extends Kind<F, ?>> {

  static <F extends Kind<F, ?>> TestFactory<F> factory(MonadDefer<F> monad) {
    return () -> monad;
  }

  MonadDefer<F> monad();

  default TestCase.GivenStep<F> should(String name) {
    return TestCase.test(monad(), name);
  }
}