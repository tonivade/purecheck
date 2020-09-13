/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck.spec;

import static com.github.tonivade.purefun.concurrent.FutureOf.toFuture;
import static com.github.tonivade.purefun.monad.IOOf.toIO;

import java.util.concurrent.Executor;

import com.github.tonivade.purecheck.PureCheck;
import com.github.tonivade.purecheck.TestCase;
import com.github.tonivade.purecheck.TestFactory;
import com.github.tonivade.purecheck.TestSuite;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.data.NonEmptyList;
import com.github.tonivade.purefun.instances.FutureInstances;
import com.github.tonivade.purefun.instances.IOInstances;
import com.github.tonivade.purefun.monad.IO_;

/**
 * A silly class to define the test case factory and reuse
 *
 * @author tonivade
 */
public abstract class IOTestSpec<E> {

  protected final TestFactory<IO_> it = TestFactory.factory(IOInstances.monadDefer());
  
  @SafeVarargs
  protected final TestSuite<IO_, E> suite(
      String name, TestCase<IO_, E, ?> test, TestCase<IO_, E, ?>... tests) {
    return new TestSuite<IO_, E>(IOInstances.monad(), name, NonEmptyList.of(test, tests)) {
      @Override
      public TestSuite.Report<E> run() {
        return runK().fix(toIO()).unsafeRunSync();
      }
      
      @Override
      public Future<TestSuite.Report<E>> parRun(Executor executor) {
        return runK().fix(toIO()).foldMap(FutureInstances.async(executor)).fix(toFuture());
      }
    };
  }
  
  @SafeVarargs
  protected final PureCheck<IO_, E> pureCheck(
      String name, TestSuite<IO_, E> suite, TestSuite<IO_, E>... suites) {
    return new PureCheck<IO_, E>(IOInstances.monad(), name, NonEmptyList.of(suite, suites)) {
      @Override
      public PureCheck.Report<E> run() {
        return runK().fix(toIO()).unsafeRunSync();
      }
      
      @Override
      public Future<PureCheck.Report<E>> parRun(Executor executor) {
        return runK().fix(toIO()).foldMap(FutureInstances.async(executor)).fix(toFuture());
      }
    };
  }
}
