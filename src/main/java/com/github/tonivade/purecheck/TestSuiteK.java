/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import static com.github.tonivade.purefun.Precondition.checkNonEmpty;
import static com.github.tonivade.purefun.Precondition.checkNonNull;

import java.util.concurrent.Executor;

import com.github.tonivade.purefun.Witness;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.concurrent.ParOf;
import com.github.tonivade.purefun.data.NonEmptyList;
import com.github.tonivade.purefun.instances.ParInstances;
import com.github.tonivade.purefun.monad.IO;

/**
 * It defines a test suite that is composed by a non empty collection of test cases
 * 
 * It allows to run the tests serialized one by one with {@code #run()} of in parallel with {@code #parRun(Executor)}
 * 
 * @author tonivade
 *
 * @param <E> type of the error
 */
public class TestSuiteK<F extends Witness, E> {

  private final String name;
  private final NonEmptyList<TestCaseK<F, E, ?>> tests;
  
  /**
   * It will throw {@code NullPointerException} if the tests is null
   * 
   * @param tests list of tests
   */
  private TestSuiteK(String name, NonEmptyList<TestCaseK<F, E, ?>> tests) {
    this.name = checkNonEmpty(name);
    this.tests = checkNonNull(tests);
  }
  
  public TestSuiteK<F, E> addAll(TestSuiteK<F, E> other) {
    return new TestSuiteK<>(
        this.name + " and " + other.name, 
        this.tests.appendAll(other.tests));
  }

  public IO<TestReport<E>> runIO() {
    NonEmptyList<IO<TestResult<E, ?>>> map = (NonEmptyList) tests.map(TestCaseK::runIO);

    return IO.traverse(map).map(xs -> new TestReport<>(name, xs));
  }

  /**
   * It runs the suite one by one
   * 
   * @return the result of the suite
   */
  public TestReport<E> run() {
    return runIO().unsafeRunSync();
  }

  /**
   * It runs the suite in parallel using the given {@code Executor}
   * 
   * @param executor executor on which the suite is going to be executed
   * @return a promise with the result of the suite
   */
  public Future<TestReport<E>> parRun(Executor executor) {
    return runIO().foldMap(ParInstances.monadDefer()).fix(ParOf::narrowK).apply(executor);
  }
  
  @SafeVarargs
  public static <F extends Witness, E> TestSuiteK<F, E> suite(String name, TestCaseK<F, E, ?> test, TestCaseK<F, E, ?>... tests) {
    return new TestSuiteK<>(name, NonEmptyList.of(test, tests));
  }
}
