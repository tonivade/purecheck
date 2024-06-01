/*
 * Copyright (c) 2020-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import static com.github.tonivade.purefun.core.Function1.identity;
import static com.github.tonivade.purefun.core.Precondition.checkNonEmpty;
import static com.github.tonivade.purefun.core.Precondition.checkNonNull;
import static com.github.tonivade.purefun.data.Sequence.listOf;
import com.github.tonivade.purefun.Kind;

import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.typeclasses.Monad;

public sealed interface PropertyTestCase<F extends Kind<F, ?>, E, T, R> permits PropertyTestCaseImpl {

  String name();

  Kind<F, TestSuite.Report<E>> run();

  PropertyTestCase<F, E, T, R> disable(String reason);
}

final class PropertyTestCaseImpl<F extends Kind<F, ?>, E, T, R> implements PropertyTestCase<F, E, T, R> {

  private final Monad<F> monad;
  private final String name;
  private final Kind<F, Sequence<TestResult<E, T, R>>> test;

  public PropertyTestCaseImpl(Monad<F> monad, String name, Kind<F, Sequence<TestResult<E, T, R>>> test) {
    this.monad = checkNonNull(monad);
    this.name = checkNonEmpty(name);
    this.test = checkNonNull(test);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Kind<F, TestSuite.Report<E>> run() {
    return monad.map(test, xs -> new TestSuite.Report<>(name, xs.map(identity())));
  }

  @Override
  public PropertyTestCase<F, E, T, R> disable(String reason) {
    return new PropertyTestCaseImpl<>(monad, name, monad.pure(listOf(TestResult.disabled(name, reason))));
  }
}
