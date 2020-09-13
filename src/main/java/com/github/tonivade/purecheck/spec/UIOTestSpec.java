/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck.spec;

import static com.github.tonivade.purefun.concurrent.FutureOf.toFuture;
import static com.github.tonivade.purefun.effect.UIOOf.toUIO;

import java.util.concurrent.Executor;

import com.github.tonivade.purecheck.PureCheck;
import com.github.tonivade.purecheck.TestCase;
import com.github.tonivade.purecheck.TestFactory;
import com.github.tonivade.purecheck.TestSuite;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.data.NonEmptyList;
import com.github.tonivade.purefun.effect.UIO_;
import com.github.tonivade.purefun.instances.FutureInstances;
import com.github.tonivade.purefun.instances.UIOInstances;

/**
 * A silly class to define the test case factory and reuse
 *
 * @author tonivade
 */
public abstract class UIOTestSpec<E> {

  protected final TestFactory<UIO_> it = TestFactory.factory(UIOInstances.monadDefer());
  
  @SafeVarargs
  protected final TestSuite<UIO_, E> suite(
      String name, TestCase<UIO_, E, ?> test, TestCase<UIO_, E, ?>... tests) {
    return new TestSuite<UIO_, E>(UIOInstances.monad(), name, NonEmptyList.of(test, tests)) {
      @Override
      public Report<E> run() {
        return runK().fix(toUIO()).unsafeRunSync();
      }
      
      @Override
      public Future<Report<E>> parRun(Executor executor) {
        return runK().fix(toUIO()).foldMap(FutureInstances.async()).fix(toFuture());
      }
    };
  }
  
  @SafeVarargs
  protected final PureCheck<UIO_, E> pureCheck(
      String name, TestSuite<UIO_, E> suite, TestSuite<UIO_, E>... suites) {
    return new PureCheck<UIO_, E>(UIOInstances.monad(), name, NonEmptyList.of(suite, suites)) {
      @Override
      public PureCheck.Report<E> run() {
        return runK().fix(toUIO()).unsafeRunSync();
      }
      
      @Override
      public Future<PureCheck.Report<E>> parRun(Executor executor) {
        return runK().fix(toUIO()).foldMap(FutureInstances.async(executor)).fix(toFuture());
      }
    };
  }
}
