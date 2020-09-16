/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import static com.github.tonivade.purefun.Precondition.checkNonNull;

import java.util.concurrent.Executor;

import com.github.tonivade.purefun.Witness;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.data.NonEmptyList;
import com.github.tonivade.purefun.runtimes.Runtime;
import com.github.tonivade.purefun.typeclasses.MonadDefer;

public abstract class TestSpec<F extends Witness, E> {

  protected final TestFactory<F> it;
  
  private final Runtime<F> runtime;
  private final MonadDefer<F> monad;

  public TestSpec(Runtime<F> runtime, MonadDefer<F> monad) {
    this.runtime = checkNonNull(runtime);
    this.monad = checkNonNull(monad);
    this.it = TestFactory.factory(monad);
  }
  
  @SafeVarargs
  protected final TestSuite<F, E> suite(
      String name, TestCase<F, E, ?> test, TestCase<F, E, ?>... tests) {
    return new TestSuite<F, E>(monad, name, NonEmptyList.of(test, tests)) {
      @Override
      public TestSuite.Report<E> run() {
        return runtime.run(runK());
      }
      
      @Override
      public Future<TestSuite.Report<E>> parRun(Executor executor) {
        return runtime.parRun(runK());
      }
    };
  }
  
  @SafeVarargs
  protected final PureCheck<F, E> pureCheck(
      String name, TestSuite<F, E> suite, TestSuite<F, E>... suites) {
    return new PureCheck<F, E>(monad, name, NonEmptyList.of(suite, suites)) {
      @Override
      public PureCheck.Report<E> run() {
        return runtime.run(runK());
      }
      
      @Override
      public Future<PureCheck.Report<E>> parRun(Executor executor) {
        return runtime.parRun(runK());
      }
    };
  }
}
