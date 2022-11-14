/*
 * Copyright (c) 2020-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
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
import static com.github.tonivade.purefun.Producer.cons;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.time.Duration;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.Matcher2;
import com.github.tonivade.purefun.Producer;
import com.github.tonivade.purefun.Tuple;
import com.github.tonivade.purefun.Tuple2;
import com.github.tonivade.purefun.Validator;
import com.github.tonivade.purefun.Witness;
import com.github.tonivade.purefun.type.Either;
import com.github.tonivade.purefun.type.Validation;
import com.github.tonivade.purefun.type.Validation.Result;
import com.github.tonivade.purefun.typeclasses.Monad;
import com.github.tonivade.purefun.typeclasses.MonadDefer;

/**
 * It defines a test case, given an operation that eventually returns a value, then
 * it will apply some validations to check if the result is correct.
 *
 * @author tonivade
 *
 * @param <F> type of the test case
 * @param <E> type of error generated
 * @param <T> type of the input value
 * @param <R> type of the result returned by the operation
 */
public sealed interface TestCase<F extends Witness, E, T, R> {

  String name();

  Kind<F, TestResult<E, T, R>> run();

  TestCase<F, E, T, R> disable(String reason);

  TestCase<F, E, T, Tuple2<Duration, R>> timed();

  TestCase<F, E, T, R> retryOnError(int times);

  TestCase<F, E, T, R> retryOnFailure(int times);

  TestCase<F, E, T, R> repeat(int times);

  /**
   * It returns a builder to create a new test case
   *
   * @param monad monad instance for F
   * @param name name of the test case
   * @param <F> type of the test case
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
      return given(cons(given));
    }

    public <T> WhenStep<F, T> given(Producer<T> given) {
      return new WhenStep<>(monad, name, given);
    }

    public <T> WhenStep<F, T> givenNull() {
      return given((T) null);
    }

    public <T> WhenStep<F, T> noGiven() {
      return given((T) null);
    }
  }

  final class WhenStep<F extends Witness, T> {

    private final MonadDefer<F> monad;
    private final String name;
    private final Producer<T> given;

    private WhenStep(MonadDefer<F> monad, String name, Producer<T> given) {
      this.monad = monad;
      this.name = name;
      this.given = given;
    }

    public <R> ThenStep<F, T, R> whenK(Function1<T, ? extends Kind<F, R>> when) {
      return new ThenStep<>(monad, name, given, when);
    }

    public <R> ThenStep<F, T, R> when(Function1<T, ? extends R> when) {
      return whenK(when.liftTry().andThen(monad::fromTry));
    }

    public ThenStep<F, T, T> noop() {
      return when(identity());
    }

    public <R> ThenStep<F, T, R> when(Kind<F, R> when) {
      return whenK(ignore -> when);
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
    private final Producer<T> given;
    private final Function1<T, ? extends Kind<F, R>> when;

    private ThenStep(MonadDefer<F> monad, String name, Producer<T> given, Function1<T, ? extends Kind<F, R>> when) {
      this.monad = checkNonNull(monad);
      this.name = checkNonEmpty(name);
      this.given = checkNonNull(given);
      this.when = checkNonNull(when);
    }

    public <E> TestCase<F, E, T, R> onSuccess(Validator<Result<E>, R> validator) {
      return validate(Either.right(tuple -> {
        Validation<Result<E>, R> validate = validator.validate(tuple.get2());
        return validate.map(value -> Tuple.of(tuple.get1(), value));
      }));
    }

    public <E> TestCase<F, E, T, R> onFailure(Validator<Result<E>, Throwable> validator) {
      return validate(Either.left(validator));
    }

    public <E> TestCase<F, E, T, R> then(Validator<E, R> validator) {
      return onSuccess(validator.mapError(Result::of));
    }

    public TestCase<F, String, T, R> thenThrows(Class<? extends Throwable> clazz) {
      return thenThrows(Validator.from(Matcher1.instanceOf(clazz), () -> "required exception of type: " + clazz));
    }

    public <E> TestCase<F, E, T, R> thenThrows(Validator<E, Throwable> validator) {
      return onFailure(validator.mapError(Result::of));
    }

    public TestCase<F, String, T, R> verify(Matcher2<T, R> matcher) {
      return validate(Either.right(Validator.from(tuple -> matcher.match(tuple.get1(), tuple.get2()), () -> Result.of("does not match"))));
    }

    public <E> TestCase<F, E, T, R> verify(Matcher2<T, R> matcher, Producer<E> message) {
      return validate(Either.right(Validator.from(tuple -> matcher.match(tuple.get1(), tuple.get2()), () -> Result.of(message.get()))));
    }

    private <E> TestCase<F, E, T, R> validate(Either<Validator<Result<E>, Throwable>, Validator<Result<E>, Tuple2<T, R>>> then) {
      var caller = getCaller();
      return new TestCaseImpl<>(monad, name, caller, given, when, then);
    }

    private StackFrame getCaller() {
      return StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
        .walk(stream -> stream.dropWhile(frame -> frame.getDeclaringClass() == this.getClass()).findFirst()).orElseThrow();
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
final class TestCaseImpl<F extends Witness, E, T, R> implements TestCase<F, E, T, R> {

  private final MonadDefer<F> monad;
  private final String name;
  private final StackFrame caller;

  private final Producer<T> given;
  private final Function1<T, ? extends Kind<F, R>> when;
  private final Either<Validator<Result<E>, Throwable>, Validator<Result<E>, Tuple2<T, R>>> then;

  /**
   * It will throw {@code IllegalArgumentException} if parameters are null or if name is a empty string
   *
   * @param monad monad instance for the type F
   * @param name name of the test case
   * @paran given value of the input value to the test
   * @param caller stack frame of the caller
   * @param when operation under test thar returns a value {@code R}
   * @param then Validation to apply to the result generated by operation
   */
  protected TestCaseImpl(
      MonadDefer<F> monad,
      String name,
      StackFrame caller,
      Producer<T> given,
      Function1<T, ? extends Kind<F, R>> when,
      Either<Validator<Result<E>, Throwable>, Validator<Result<E>, Tuple2<T, R>>> then) {
    this.monad = checkNonNull(monad);
    this.name = checkNonEmpty(name);
    this.caller = checkNonNull(caller);
    this.given = checkNonNull(given);
    this.when = checkNonNull(when);
    this.then = checkNonNull(then);
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
  public Kind<F, TestResult<E, T, R>> run() {
    var input = given.get();
    return monad.map(when.andThen(monad::attempt).apply(input), result -> fold(name, input, caller, result, then));
  }

  @Override
  public TestCase<F, E, T, R> disable(String reason) {
    return new TestCaseDisabled<>(monad, name, reason);
  }

  @Override
  public TestCase<F, E, T, Tuple2<Duration, R>> timed() {
    return new TestCaseImpl<>(monad, name, caller, given, when.andThen(monad::timed), then.map(validator -> value -> {
      Tuple2<T, R> tuple = Tuple.of(value.get1(), value.get2().get2());
      Validation<Result<E>, Tuple2<T, R>> validate = validator.validate(tuple);
      return validate.map(Function1.cons(value));
    }));
  }

  @Override
  public TestCase<F, E, T, R> retryOnError(int times) {
    Kind<F, TestResult<E, T, R>> test = run();
    monad.flatMap(test, result -> result.isFailure() ? monad.retry(test, monad.scheduleOf().recurs(times)) : monad.pure(result));
    return new TestCaseEnd<>(monad, name, test);
  }

  @Override
  public TestCase<F, E, T, R> retryOnFailure(int times) {
    Kind<F, TestResult<E, T, R>> test = run();
    monad.flatMap(test, result -> result.isError() ? monad.retry(test, monad.scheduleOf().recurs(times)) : monad.pure(result));
    return new TestCaseEnd<>(monad, name, test);
  }

  @Override
  public TestCase<F, E, T, R> repeat(int times) {
    var repeat = when.andThen(test -> monad.repeat(test, monad.scheduleOf().<R>recurs(times).zipRight(monad.scheduleOf().identity())));
    return new TestCaseImpl<>(monad, name, caller, given, repeat, then);
  }

  private static <E, T, R> TestResult<E, T, R> fold(String name, T input, StackFrame caller,
      Either<Throwable, R> result, Either<Validator<Result<E>, Throwable>, Validator<Result<E>, Tuple2<T, R>>> then) {
    return then.fold(

        onFailure -> result.fold(
            error -> onFailure.validate(error).fold(
                r -> failure(name, input, caller, error, r), t -> success(name, input, error)),
            value -> error(name, input, caller, value)),

        onSuccess -> result.fold(
            error -> error(name, input, caller, error),
            value -> onSuccess.validate(Tuple.of(input, value)).fold(
                r -> failure(name, input, caller, value, r), t -> success(name, input, value))));
  }
}

final class TestCaseDisabled<F extends Witness, E, T, R> implements TestCase<F, E, T, R> {

  private final Monad<F> monad;
  private final String name;
  private final String reason;

  public TestCaseDisabled(Monad<F> monad, String name, String reason) {
    this.monad = checkNonNull(monad);
    this.name = checkNonEmpty(name);
    this.reason = checkNonEmpty(reason);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Kind<F, TestResult<E, T, R>> run() {
    return monad.pure(disabled(name, reason));
  }

  @Override
  public TestCase<F, E, T, R> disable(String reason) {
    return this;
  }

  @Override
  public TestCase<F, E, T, Tuple2<Duration, R>> timed() {
    throw new UnsupportedOperationException();
  }

  @Override
  public TestCase<F, E, T, R> retryOnError(int times) {
    return this;
  }

  @Override
  public TestCase<F, E, T, R> retryOnFailure(int times) {
    return this;
  }

  @Override
  public TestCase<F, E, T, R> repeat(int times) {
    return this;
  }
}

final class TestCaseEnd<F extends Witness, E, T, R> implements TestCase<F, E, T, R> {

  private final Monad<F> monad;
  private final String name;
  private final Kind<F, TestResult<E, T, R>> test;

  public TestCaseEnd(Monad<F> monad, String name, Kind<F, TestResult<E, T, R>> test) {
    this.monad = checkNonNull(monad);
    this.name = checkNonEmpty(name);
    this.test = checkNonNull(test);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Kind<F, TestResult<E, T, R>> run() {
    return test;
  }

  @Override
  public TestCase<F, E, T, R> disable(String reason) {
    return new TestCaseDisabled<>(monad, name, reason);
  }

  @Override
  public TestCase<F, E, T, Tuple2<Duration, R>> timed() {
    throw new UnsupportedOperationException();
  }

  @Override
  public TestCase<F, E, T, R> retryOnError(int times) {
    throw new UnsupportedOperationException();
  }

  @Override
  public TestCase<F, E, T, R> retryOnFailure(int times) {
    throw new UnsupportedOperationException();
  }

  @Override
  public TestCase<F, E, T, R> repeat(int times) {
    throw new UnsupportedOperationException();
  }
}