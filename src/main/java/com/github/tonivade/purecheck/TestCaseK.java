/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import static com.github.tonivade.purecheck.TestResult.disabled;
import static com.github.tonivade.purecheck.TestResult.error;
import static com.github.tonivade.purecheck.TestResult.failure;
import static com.github.tonivade.purecheck.TestResult.success;
import static com.github.tonivade.purefun.Function1.identity;
import static com.github.tonivade.purefun.Precondition.checkNonEmpty;
import static com.github.tonivade.purefun.Precondition.checkNonNull;

import java.time.Duration;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.HigherKind;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Producer;
import com.github.tonivade.purefun.Tuple2;
import com.github.tonivade.purefun.Validator;
import com.github.tonivade.purefun.Witness;
import com.github.tonivade.purefun.type.Either;
import com.github.tonivade.purefun.type.Validation.Result;
import com.github.tonivade.purefun.typeclasses.MonadDefer;

/**
 * It defines a test case, given an operation that eventually returns a value, then
 * it will apply some validations to check if the result is correct.
 * 
 * @author tonivade
 *
 * @param <F> type of the test case
 * @param <E> type of error generated
 * @param <T> type of the result returned by the operation
 */
@HigherKind(sealed = true)
public interface TestCaseK<F extends Witness, E, T> extends TestCaseKOf<F, E, T> {

  String name();

  Kind<F, TestResult<E, T>> runIO();

  TestCaseK<F, E, T> disable(String reason);

  TestCaseK<F, E, Tuple2<Duration, T>> timed();

  TestCaseK<F, E, T> retryOnFailure(int times);

  TestCaseK<F, E, T> retryOnError(int times);

  TestCaseK<F, E, T> repeat(int times);

  /**
   * It returns a builder to create a new test case
   *
   * @param name name of the test case
   * @return a new test case
   */
  static <F extends Witness> GivenStep<F> test(MonadDefer<F> monad, String name) {
    return new GivenStep<>(monad, name);
  }

  final class GivenStep<F extends Witness> {

    private final MonadDefer<F> monad;
    private final String name;

    private GivenStep(MonadDefer<F> monad, String name) {
      this.monad = monad;
      this.name = name;
    }

    public <T> WhenStep<F, T> given(T given) {
      return new WhenStep<>(monad, name, given);
    }

    public <T> WhenStep<F, T> givenNull() {
      return given(null);
    }

    public <T> WhenStep<F, T> noGiven() {
      return given(null);
    }
  }

  final class WhenStep<F extends Witness, T> {

    private final MonadDefer<F> monad;
    private final String name;
    private final T given;

    private WhenStep(MonadDefer<F> monad, String name, T given) {
      this.monad = monad;
      this.name = name;
      this.given = given;
    }

    public <R> ThenStep<F, T, R> run(Function1<T, ? extends Kind<F, R>> when) {
      return new ThenStep<>(monad, name, given, when);
    }

    public <R> ThenStep<F, T, R> when(Function1<T, R> when) {
      return run(when.liftTry().andThen(result -> result.fold(monad::raiseError, monad::pure)));
    }

    public ThenStep<F, T, T> noop() {
      return when(identity());
    }

    public <R> ThenStep<F, T, R> when(Kind<F, R> when) {
      return run(ignore -> when);
    }

    public <R> ThenStep<F, T, R> when(Producer<R> when) {
      return when(monad.later(when));
    }

    public <R> ThenStep<F, T, R> error(Throwable error) {
      return when(monad.raiseError(error));
    }
  }

  final class ThenStep<F extends Witness, T, R> {

    private final MonadDefer<F> monad;
    private final String name;
    private final T given;
    private final Function1<T, ? extends Kind<F, R>> when;

    private ThenStep(MonadDefer<F> monad, String name, T given, Function1<T, ? extends Kind<F, R>> when) {
      this.monad = monad;
      this.name = name;
      this.given = given;
      this.when = when;
    }

    public <E> TestCaseK<F, E, R> then(Either<Validator<Result<E>, Throwable>, Validator<Result<E>, R>> then) {
      return new TestCaseImpl<>(monad, name, monad.defer(() -> when.apply(given)), then);
    }

    public <E> TestCaseK<F, E, R> thenOnSuccess(Validator<Result<E>, R> validator) {
      return then(Either.right(validator));
    }

    public <E> TestCaseK<F, E, R> thenOnFailure(Validator<Result<E>, Throwable> validator) {
      return then(Either.left(validator));
    }

    public <E> TestCaseK<F, E, R> thenMustBe(Validator<E, R> validator) {
      return thenOnSuccess(validator.mapError(Result::of));
    }

    public <E> TestCaseK<F, E, R> thenThrows(Validator<E, Throwable> validator) {
      return thenOnFailure(validator.mapError(Result::of));
    }
  }
}

/**
 * Implementation of the test case
 * 
 * @author tonivade
 *
 * @param <F> type of the test case
 * @param <E> type of error generated
 * @param <T> type of the result returned by the operation
 */
final class TestCaseImpl<F extends Witness, E, T> implements SealedTestCaseK<F, E, T> {
  
  private final String name;
  private final Kind<F, TestResult<E, T>> test;
  private final MonadDefer<F> monad;
  
  /**
   * It will throw {@code IllegalArgumentException} if parameters are null or if name is a empty string
   * 
   * @param monad monad instance for the type F
   * @param name name of the test case
   * @param when operation under test thar returns a value {@code T}
   * @param then Validation to apply to the result generated by operation
   */
  protected TestCaseImpl(MonadDefer<F> monad, String name, Kind<F, T> when, Either<Validator<Result<E>, Throwable>, Validator<Result<E>, T>> then) {
    this.monad = checkNonNull(monad);
    checkNonNull(when);
    checkNonNull(then);
    this.name = checkNonEmpty(name);
    this.test = monad.map(monad.attempt(when), result -> fold(name, result, then));
  }

  /**
   * Private constructor used mainly for method decorators, like retry or repeat
   * 
   * @param monad monad instance for the type F
   * @param name name of the test case
   * @param test test and validation to be executed
   */
  private TestCaseImpl(MonadDefer<F> monad, String name, Kind<F, TestResult<E, T>> test) {
    this.monad  = checkNonNull(monad);
    this.name = checkNonEmpty(name);
    this.test = checkNonNull(test);
  }

  @Override
  public String name() {
    return name;
  }

  /**
   * It describes the execution of the operation and the validation without executing it yet
   *
   * @return the validation result
   */
  @Override
  public Kind<F, TestResult<E, T>> runIO() {
    return test;
  }

  @Override
  public TestCaseK<F, E, T> disable(String reason) {
    return new TestCaseImpl<>(monad, name, monad.pure(disabled(name, reason)));
  }

  @Override
  public TestCaseK<F, E, Tuple2<Duration, T>> timed() {
//    return new TestCaseImpl<>(monad, scheduleOfF, name, test.timed().map(
//        tuple -> tuple.applyTo((duration, result) -> result.map(value -> Tuple.of(duration, value)))));
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public TestCaseK<F, E, T> retryOnFailure(int times) {
    return new TestCaseImpl<>(monad, name, 
      monad.flatMap(test, result -> result.isFailure() ? monad.retry(test, monad.scheduleOf().recurs(times)) : monad.pure(result)));
  }

  @Override
  public TestCaseK<F, E, T> retryOnError(int times) {
    return new TestCaseImpl<>(monad, name, 
      monad.flatMap(test, result -> result.isError() ? monad.retry(test, monad.scheduleOf().recurs(times)) : monad.pure(result)));
  }

  @Override
  public TestCaseK<F, E, T> repeat(int times) {
    return new TestCaseImpl<>(monad, name, 
      monad.repeat(test, monad.scheduleOf().<TestResult<E, T>>recurs(times).zipRight(monad.scheduleOf().identity())));
  }

  private static <E, T> TestResult<E, T> fold(String name, Either<Throwable, T> result, Either<Validator<Result<E>, Throwable>, Validator<Result<E>, T>> then) {
    return then.fold(

        onFailure -> result.fold(
            error -> onFailure.validate(error).fold(
                r -> failure(name, error, r), t -> success(name, error)),
            value -> error(name, value)),

        onSuccess -> result.fold(
            error -> error(name, error),
            value -> onSuccess.validate(value).fold(
                r -> failure(name, value, r), t -> success(name, value))));
  }
}
