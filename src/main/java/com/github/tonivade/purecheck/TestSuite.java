/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import static com.github.tonivade.purefun.Precondition.checkNonEmpty;
import static com.github.tonivade.purefun.Precondition.checkNonNull;
import static com.github.tonivade.purefun.data.ImmutableList.empty;
import java.util.concurrent.Executor;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Witness;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.data.NonEmptyList;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.typeclasses.Applicative;

/**
 * It defines a test suite that is composed by a non empty collection of test cases
 * 
 * It allows to run the tests serialized one by one with {@code #run()} of in parallel with {@code #parRun(Executor)}
 * 
 * @author tonivade
 *
 * @param <F> type of the kind
 * @param <E> type of the error
 */
public abstract class TestSuite<F extends Witness, E> {

  private final Applicative<F> applicative;
  private final String name;
  private final NonEmptyList<TestCase<F, E, ?>> tests;
  
  /**
   * It will throw {@code NullPointerException} if the tests is null
   * 
   * @param applicative applicative instance for type F
   * @param name name of the suite
   * @param tests list of tests
   */
  public TestSuite(Applicative<F> applicative, String name, NonEmptyList<TestCase<F, E, ?>> tests) {
    this.applicative = checkNonNull(applicative);
    this.name = checkNonEmpty(name);
    this.tests = checkNonNull(tests);
  }

  /**
   * It runs the suite in the given effect of the test and creates a test results
   * 
   * @return the result of the suite
   */
  public Kind<F, TestReport<E>> runK() {
    Kind<F, Sequence<TestResult<E, ?>>> results = traverse(tests.map(TestCase::run));

    return applicative.map(results, xs -> new TestReport<>(name, xs));
  }

  /**
   * It runs the suite one by one
   * 
   * @return the result of the suite
   */
  public abstract TestReport<E> run();

  /**
   * It runs the suite in parallel using the default executor
   * 
   * @return a future with the result of the suite
   */
  public Future<TestReport<E>> parRun() {
    return parRun(Future.DEFAULT_EXECUTOR);
  }

  /**
   * It runs the suite in parallel using the given {@code Executor}
   * 
   * @param executor executor on which the suite is going to be executed
   * @return a future with the result of the suite
   */
  public abstract Future<TestReport<E>> parRun(Executor executor);

  private <T> Kind<F, Sequence<T>> traverse(Sequence<Kind<F, ? extends T>> list) {
    return list.foldLeft(applicative.pure(empty()), 
        (xs, a) -> applicative.mapN(xs, a, (l, e) -> l.append(e)));
  }
}
