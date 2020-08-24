/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import static com.github.tonivade.purefun.Precondition.checkNonEmpty;
import static com.github.tonivade.purefun.Precondition.checkNonNull;

import java.util.concurrent.Executor;

import com.github.tonivade.purefun.Function2;
import com.github.tonivade.purefun.concurrent.Par;
import com.github.tonivade.purefun.concurrent.Promise;
import com.github.tonivade.purefun.data.ImmutableList;
import com.github.tonivade.purefun.data.NonEmptyList;
import com.github.tonivade.purefun.data.Sequence;
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
public class TestSuite<E> {

  private final String name;
  private final NonEmptyList<TestCase<E, ?>> tests;
  
  /**
   * It will throw {@code NullPointerException} if the tests is null
   * 
   * @param tests list of tests
   */
  private TestSuite(String name, NonEmptyList<TestCase<E, ?>> tests) {
    this.name = checkNonEmpty(name);
    this.tests = checkNonNull(tests);
  }
  
  public TestSuite<E> addAll(TestSuite<E> other) {
    return new TestSuite<>(
        this.name + " and " + other.name, 
        this.tests.appendAll(other.tests));
  }

  public IO<TestReport<E>> runIO() {
    NonEmptyList<IO<TestResult<E, ?>>> map = (NonEmptyList) tests.map(TestCase::runIO);

    return traverse(map).map(xs -> new TestReport<>(name, xs));
  }

  /**
   * It runs the suite one by one
   * 
   * @return the result of the suite
   */
  public TestReport<E> run() {
    NonEmptyList<TestResult<E, ?>> map = tests.map(TestCase::run);

    return new TestReport<>(name, map);
  }

  /**
   * It runs the suite in parallel using the given {@code Executor}
   * 
   * @param executor executor on which the suite is going to be executed
   * @return a promise with the result of the suite
   */
  public Promise<TestReport<E>> parRun(Executor executor) {
    NonEmptyList<Par<TestResult<E, ?>>> map = (NonEmptyList) tests.map(TestCase::parRun);

    return Par.traverse(map).map(xs -> new TestReport<>(name, xs)).run(executor);
  }
  
  @SafeVarargs
  public static <E> TestSuite<E> suite(String name, TestCase<E, ?> test, TestCase<E, ?>... tests) {
    return new TestSuite<>(name, NonEmptyList.of(test, tests));
  }

  // TODO: move to IO
  private static <A> IO<Sequence<A>> traverse(Sequence<IO<A>> sequence) {
    ImmutableList<A> empty = ImmutableList.empty();
    return sequence.foldLeft(IO.pure(empty), (xs, a) -> map2(xs, a, Sequence::append));
  }

  // TODO: move to IO
  private static <A, B, C> IO<C> map2(IO<A> fa, IO<B> fb, Function2<A, B, C> mapper) {
    return fa.flatMap(a -> fb.map(b -> mapper.apply(a, b)));
  }
}
