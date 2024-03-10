/*
 * Copyright (c) 2020-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import static com.github.tonivade.purefun.core.Precondition.checkNonEmpty;
import static com.github.tonivade.purefun.core.Precondition.checkNonNull;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serial;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.lang.StackWalker.StackFrame;

import com.github.tonivade.purefun.core.Function1;
import com.github.tonivade.purefun.core.Recoverable;
import com.github.tonivade.purefun.type.Either;
import com.github.tonivade.purefun.type.Validation.Result;

/**
 * it defines the result of a test case, given a name, a result and a value.
 *
 * @author tonivade
 *
 * @param <E> type of the error
 * @param <T> type of the input value
 * @param <R> type of the output value
 */
public sealed interface TestResult<E, T, R> {

  default boolean isSuccess() {
    return false;
  }

  default boolean isFailure() {
    return false;
  }

  default boolean isError() {
    return false;
  }

  default boolean isDisabled() {
    return false;
  }

  void assertion();

  <S> TestResult<E, T, S> map(Function1<R, S> mapper);

  static <E, T, R> TestResult<E, T, R> success(String name, T input, R value) {
    return new Success<>(name, input, Either.right(value));
  }

  static <E, T, R> TestResult<E, T, R> success(String name, T input, Throwable error) {
    return new Success<>(name, input, Either.left(error));
  }

  static <E, T, R> TestResult<E, T, R> failure(String name, T input, StackFrame caller, R value, Result<E> result) {
    return new Failure<>(name, input, caller, Either.right(value), result);
  }

  static <E, T, R> TestResult<E, T, R> failure(String name, T input, StackFrame caller, Throwable error, Result<E> result) {
    return new Failure<>(name, input, caller, Either.left(error), result);
  }

  static <E, T, R> TestResult<E, T, R> error(String name, T input, StackFrame caller, Throwable error) {
    return new Error<>(name, input, caller, Either.right(error));
  }

  static <E, T, R> TestResult<E, T, R> error(String name, T input, StackFrame caller, R error) {
    return new Error<>(name, input, caller, Either.left(error));
  }

  static <E, T, R> TestResult<E, T, R> disabled(String name, String reason) {
    return new Disabled<>(name, reason);
  }

  record Success<E, T, R>(String name, T input, Either<Throwable, R> value) implements TestResult<E, T, R>, Serializable {

    @Serial
    private static final long serialVersionUID = 2612477493587755025L;

    /**
     * it will throw a {@code NullPointerException} if any of the params are null
     * and {@code IllegalArgumentException} if name is an empty String.
     *
     * @param name name of the test, non-empty value
     * @param value result of the operation under test
     */
    public Success {
      checkNonEmpty(name);
      checkNonNull(value);
    }

    @Override
    public boolean isSuccess() {
      return true;
    }

    @Override
    public void assertion() {
      // nothing to do
    }

    @Override
    public <S> TestResult<E, T, S> map(Function1<R, S> mapper) {
      return new Success<>(name, input, value.map(mapper));
    }

    @Override
    public String toString() {
      return String.format("it should '%s' with input '%s' SUCCESS: '%s'",
          name, input, value.fold(Object::toString, Object::toString));
    }
  }

  record Failure<E, T, R>(String name, T input, StackFrame caller, Either<Throwable, R> value, Result<E> result) implements TestResult<E, T, R>, Serializable {

    @Serial
    private static final long serialVersionUID = 4834239536246492448L;

    /**
     * it will throw a {@code NullPointerException} if any of the params are null
     * and {@code IllegalArgumentException} if name is an empty String.
     *
     * @param name name of the test, non-empty value
     * @param caller stack frame of the caller
     * @param value result of the operation under test
     * @param result result of the validation applied to the value
     */
    public Failure {
      checkNonEmpty(name);
      checkNonNull(caller);
      checkNonNull(value);
      checkNonNull(result);
    }

    @Override
    public boolean isFailure() {
      return true;
    }

    @Override
    public void assertion() {
      if (value.isLeft()) {
        throw new AssertionError(toString(), value.getLeft());
      }
      throw new AssertionError(toString());
    }

    @Override
    public <S> TestResult<E, T, S> map(Function1<R, S> mapper) {
      return new Failure<>(name, input, caller, value.map(mapper), result);
    }

    @Override
    public String toString() {
      return String.format("test '%s' at '%s' with input '%s' FAILURE: expected '%s' but was '%s'",
          name, caller, input, result.join(","), value.fold(Object::toString, Object::toString));
    }
  }

  record Error<E, T, R>(String name, T input, StackFrame caller, Either<R, Throwable> error) implements TestResult<E, T, R>, Recoverable, Serializable {

    @Serial
    private static final long serialVersionUID = 4181923995414226773L;

    /**
     * it will throw a {@code NullPointerException} if any of the params are null
     * and {@code IllegalArgumentException} if name is an empty String.
     *
     * @param name name of the test, non-empty value
     * @param caller stack frame of the caller
     * @param error error captured by the test
     */
    public Error {
      checkNonEmpty(name);
      checkNonNull(caller);
      checkNonNull(error);
    }

    @Override
    public boolean isError() {
      return true;
    }

    @Override
    public void assertion() {
      error.fold(AssertionError::new, this::sneakyThrow);
    }

    @Override
    public <S> TestResult<E, T, S> map(Function1<R, S> mapper) {
      return new Error<>(name, input, caller, error.mapLeft(mapper));
    }

    @Override
    public String toString() {
      return String.format("test '%s' at '%s' with input '%s' ERROR: %s",
          name, caller, input, error.fold(Object::toString, Error::full));
    }

    private static String full(Throwable error) {
      StringBuilder message = new StringBuilder(String.valueOf(error.getMessage())).append('\n');
      try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
        error.printStackTrace(new PrintStream(output));
        message.append(output.toString(UTF_8));
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
      return message.toString();
    }
  }

  record Disabled<E, T, R>(String name, String reason) implements TestResult<E, T, R>, Serializable {

    @Serial
    private static final long serialVersionUID = -8661817362831938094L;

    /**
     * it will throw a {@code NullPointerException} if any of the params are null
     * and {@code IllegalArgumentException} if name is an empty String.
     *
     * @param name name of the test, non-empty value
     * @param reason description
     */
    public Disabled {
      checkNonEmpty(name);
      checkNonEmpty(reason);
    }

    @Override
    public boolean isDisabled() {
      return true;
    }

    @Override
    public void assertion() {
      // nothing to do
    }

    @Override
    public <S> TestResult<E, T, S> map(Function1<R, S> mapper) {
      return new Disabled<>(name, reason);
    }

    @Override
    public String toString() {
      return String.format("test '%s' DISABLED: %s", name, reason);
    }
  }
}
