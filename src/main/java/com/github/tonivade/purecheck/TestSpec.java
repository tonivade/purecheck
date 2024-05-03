/*
 * Copyright (c) 2020-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import static com.github.tonivade.purefun.core.Precondition.checkNonNull;

import java.util.concurrent.Executor;


import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.data.NonEmptyList;
import com.github.tonivade.purefun.typeclasses.Applicative;
import com.github.tonivade.purefun.typeclasses.FunctionK;
import com.github.tonivade.purefun.typeclasses.Instance;
import com.github.tonivade.purefun.typeclasses.Monad;
import com.github.tonivade.purefun.typeclasses.MonadDefer;
import com.github.tonivade.purefun.typeclasses.Parallel;
import com.github.tonivade.purefun.typeclasses.Runtime;

public abstract class TestSpec<F, E> {

  protected final TestFactory<F> it;

  private final Runtime<F> runtime;
  private final Applicative<F> applicative;
  private final Monad<F> monad;

  protected TestSpec(Instance<F> instance) {
    this(instance.runtime(), instance.monadDefer(), instance.applicative());
  }

  protected TestSpec(Runtime<F> runtime, MonadDefer<F> monad, Applicative<F> applicative) {
    this.runtime = checkNonNull(runtime);
    this.applicative = checkNonNull(applicative);
    this.monad = checkNonNull(monad);
    this.it = TestFactory.factory(monad);
  }

  @SafeVarargs
  protected final TestSuite<F, E> suite(
      String name, TestCase<F, E, ?, ?> test, TestCase<F, E, ?, ?>... tests) {
    return new TestSuite<>(parallel(), name, NonEmptyList.of(test, tests)) {
      @Override
      public TestSuite.Report<E> run() {
        return runtime.run(runK());
      }

      @Override
      public Future<TestSuite.Report<E>> parRun(Executor executor) {
        return runtime.parRun(runParK(), executor);
      }
    };
  }

  @SafeVarargs
  protected final PropertyTestSuite<F, E> properties(
      String name, PropertyTestCase<F, E, ?, ?> test, PropertyTestCase<F, E, ?, ?>... tests) {
    return new PropertyTestSuite<>(parallel(), name, NonEmptyList.of(test, tests)) {
      @Override
      public PropertyTestSuite.Report<E> run() {
        return runtime.run(runK());
      }

      @Override
      public Future<PropertyTestSuite.Report<E>> parRun(Executor executor) {
        return runtime.parRun(runParK(), executor);
      }
    };
  }

  @SafeVarargs
  protected final PureCheck<F, E> pureCheck(
      String name, TestSuite<F, E> suite, TestSuite<F, E>... suites) {
    return new PureCheck<>(parallel(), name, NonEmptyList.of(suite, suites)) {
      @Override
      public PureCheck.Report<E> run() {
        return runtime.run(runK());
      }

      @Override
      public Future<PureCheck.Report<E>> parRun(Executor executor) {
        return runtime.parRun(runParK(), executor);
      }
    };
  }

  private Parallel<F, F> parallel() {
    return Parallel.of(monad, applicative, FunctionK.identity(), FunctionK.identity());
  }
}
