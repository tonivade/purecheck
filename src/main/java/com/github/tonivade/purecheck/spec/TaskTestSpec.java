/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck.spec;

import static com.github.tonivade.purefun.concurrent.FutureOf.toFuture;
import static com.github.tonivade.purefun.effect.TaskOf.toTask;

import java.util.concurrent.Executor;

import com.github.tonivade.purecheck.PureCheck;
import com.github.tonivade.purecheck.TestCase;
import com.github.tonivade.purecheck.TestFactory;
import com.github.tonivade.purecheck.TestSuite;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.data.NonEmptyList;
import com.github.tonivade.purefun.effect.Task_;
import com.github.tonivade.purefun.instances.FutureInstances;
import com.github.tonivade.purefun.instances.TaskInstances;

/**
 * A silly class to define the test case factory and reuse
 *
 * @author tonivade
 */
public abstract class TaskTestSpec<E> {

  protected final TestFactory<Task_> it = TestFactory.factory(TaskInstances.monadDefer());
  
  @SafeVarargs
  protected final TestSuite<Task_, E> suite(
      String name, TestCase<Task_, E, ?> test, TestCase<Task_, E, ?>... tests) {
    return new TestSuite<Task_, E>(TaskInstances.monad(), name, NonEmptyList.of(test, tests)) {
      @Override
      public Report<E> run() {
        return runK().fix(toTask()).safeRunSync().get();
      }
      
      @Override
      public Future<Report<E>> parRun(Executor executor) {
        return runK().fix(toTask()).foldMap(FutureInstances.async()).fix(toFuture());
      }
    };
  }
  
  @SafeVarargs
  protected final PureCheck<Task_, E> pureCheck(
      String name, TestSuite<Task_, E> suite, TestSuite<Task_, E>... suites) {
    return new PureCheck<Task_, E>(TaskInstances.monad(), name, NonEmptyList.of(suite, suites)) {
      @Override
      public PureCheck.Report<E> run() {
        return runK().fix(toTask()).safeRunSync().get();
      }
      
      @Override
      public Future<PureCheck.Report<E>> parRun(Executor executor) {
        return runK().fix(toTask()).foldMap(FutureInstances.async(executor)).fix(toFuture());
      }
    };
  }
}
