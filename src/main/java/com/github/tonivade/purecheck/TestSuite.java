/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import static com.github.tonivade.purefun.Precondition.checkNonEmpty;
import static com.github.tonivade.purefun.Precondition.checkNonNull;
import static com.github.tonivade.purefun.concurrent.FutureOf.toFuture;
import static com.github.tonivade.purefun.data.ImmutableList.empty;
import static com.github.tonivade.purefun.effect.UIOOf.toUIO;
import static com.github.tonivade.purefun.monad.IOOf.toIO;

import java.util.concurrent.Executor;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Witness;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.data.NonEmptyList;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.effect.UIO_;
import com.github.tonivade.purefun.instances.FutureInstances;
import com.github.tonivade.purefun.instances.IOInstances;
import com.github.tonivade.purefun.instances.UIOInstances;
import com.github.tonivade.purefun.monad.IO_;
import com.github.tonivade.purefun.typeclasses.Applicative;

/**
 * It defines a test suite that is composed by a non empty collection of test cases
 * 
 * It allows to run the tests serialized one by one with {@code #run()} of in parallel with {@code #parRun(Executor)}
 * 
 * @author tonivade
 *
 * @param <E> type of the kind
 * @param <E> type of the error
 */
public abstract class TestSuite<F extends Witness, E> {

  private final String name;
  private final Applicative<F> applicative;
  private final NonEmptyList<TestCase<F, E, ?>> tests;
  
  /**
   * It will throw {@code NullPointerException} if the tests is null
   * 
   * @param tests list of tests
   */
  public TestSuite(Applicative<F> applicative, String name, NonEmptyList<TestCase<F, E, ?>> tests) {
    this.applicative = checkNonNull(applicative);
    this.name = checkNonEmpty(name);
    this.tests = checkNonNull(tests);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public Kind<F, TestReport<E>> runK() {
    NonEmptyList<Kind<F, TestResult<E, ?>>> list = (NonEmptyList) tests.map(TestCase::run);
    
    Kind<F, Sequence<TestResult<E, ?>>> foldLeft = list.foldLeft(applicative.pure(empty()), 
        (xs, a) -> applicative.map2(xs, a, (l, e) -> l.append(e)));

    return applicative.map(foldLeft, xs -> new TestReport<>(name, xs));
  }

  /**
   * It runs the suite one by one
   * 
   * @return the result of the suite
   */
  public abstract TestReport<E> run();

  /**
   * It runs the suite in parallel using the given {@code Executor}
   * 
   * @param executor executor on which the suite is going to be executed
   * @return a promise with the result of the suite
   */
  public abstract Future<TestReport<E>> parRun(Executor executor);
  
  @SafeVarargs
  public static <E> TestSuite<IO_, E> suiteIO(
      String name, TestCase<IO_, E, ?> test, TestCase<IO_, E, ?>... tests) {
    return new TestSuite<IO_, E>(IOInstances.monad(), name, NonEmptyList.of(test, tests)) {
      @Override
      public TestReport<E> run() {
        return runK().fix(toIO()).unsafeRunSync();
      }
      
      @Override
      public Future<TestReport<E>> parRun(Executor executor) {
        return runK().fix(toIO()).foldMap(FutureInstances.monadDefer()).fix(toFuture());
      }
    };
  }
  
  @SafeVarargs
  public static <E> TestSuite<UIO_, E> suiteUIO(
      String name, TestCase<UIO_, E, ?> test, TestCase<UIO_, E, ?>... tests) {
    return new TestSuite<UIO_, E>(UIOInstances.monad(), name, NonEmptyList.of(test, tests)) {
      @Override
      public TestReport<E> run() {
        return runK().fix(toUIO()).unsafeRunSync();
      }
      
      @Override
      public Future<TestReport<E>> parRun(Executor executor) {
        return runK().fix(toUIO()).foldMap(FutureInstances.monadDefer()).fix(toFuture());
      }
    };
  }
}
