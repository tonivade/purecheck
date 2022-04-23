/*
 * Copyright (c) 2020-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import static com.github.tonivade.purefun.Precondition.checkNonEmpty;
import static com.github.tonivade.purefun.Precondition.checkNonNull;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.lang.StackWalker.StackFrame;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.HigherKind;
import com.github.tonivade.purefun.Recoverable;
import com.github.tonivade.purefun.type.Either;
import com.github.tonivade.purefun.type.Validation.Result;

/**
 * it defines the result of a test case, given a name, a result and a value.
 * 
 * @author tonivade
 *
 * @param <E> type of the error
 * @param <T> type of the value
 */
@HigherKind
public sealed interface TestResult<E, T> extends TestResultOf<E, T> {

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

  <R> TestResult<E, R> map(Function1<T, R> mapper);
  
  static <E, T> TestResult<E, T> success(String name, T value) {
    return new Success<>(name, Either.right(value));
  }
  
  static <E, T> TestResult<E, T> success(String name, Throwable error) {
    return new Success<>(name, Either.left(error));
  }
  
  static <E, T> TestResult<E, T> failure(String name, StackFrame caller, T value, Result<E> result) {
    return new Failure<>(name, caller, Either.right(value), result);
  }
  
  static <E, T> TestResult<E, T> failure(String name, StackFrame caller, Throwable error, Result<E> result) {
    return new Failure<>(name, caller, Either.left(error), result);
  }
  
  static <E, T> TestResult<E, T> error(String name, StackFrame caller, Throwable error) {
    return new Error<>(name, caller, Either.right(error));
  }
  
  static <E, T> TestResult<E, T> error(String name, StackFrame caller, T error) {
    return new Error<>(name, caller, Either.left(error));
  }

  static <E, T> TestResult<E, T> disabled(String name, String reason) {
    return new Disabled<>(name, reason);
  }

  record Success<E, T>(String name, Either<Throwable, T> value) implements TestResult<E, T>, Serializable {

    private static final long serialVersionUID = 2612477493587755025L;

    /**
     * it will throw a {@code NullPointerException} if any of the params are null
     * and {@code IllegalArgumentException} if name is a empty String.
     * 
     * @param name name of the test, non empty value
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
    public <R> TestResult<E, R> map(Function1<T, R> mapper) {
      return new Success<>(name, value.map(mapper::apply));
    }

    @Override
    public String toString() {
      return String.format("it should '%s' SUCCESS: '%s'", 
          name, value.fold(Object::toString, Object::toString));
    }
  }

  record Failure<E, T>(String name, StackFrame caller, Either<Throwable, T> value, Result<E> result) implements TestResult<E, T>, Serializable {

    private static final long serialVersionUID = 4834239536246492448L;

    /**
     * it will throw a {@code NullPointerException} if any of the params are null
     * and {@code IllegalArgumentException} if name is a empty String.
     * 
     * @param name name of the test, non empty value
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
    public <R> TestResult<E, R> map(Function1<T, R> mapper) {
      return new Failure<>(name, caller, value.map(mapper::apply), result);
    }

    @Override
    public String toString() {
      return String.format("test '%s' at '%s' FAILURE: expected '%s' but was '%s'", 
          name, caller, result.join(","), value.fold(Object::toString, Object::toString));
    }
  }

  record Error<E, T>(String name, StackFrame caller, Either<T, Throwable> error) implements TestResult<E, T>, Recoverable, Serializable {

    private static final long serialVersionUID = 4181923995414226773L;

    /**
     * it will throw a {@code NullPointerException} if any of the params are null
     * and {@code IllegalArgumentException} if name is a empty String.
     * 
     * @param name name of the test, non empty value
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
    public <R> TestResult<E, R> map(Function1<T, R> mapper) {
      return new Error<>(name, caller, error.mapLeft(mapper::apply));
    }

    @Override
    public String toString() {
      return String.format("test '%s' at '%s' ERROR: %s", 
          name, caller, error.fold(Object::toString, Error::full));
    }

    private static String full(Throwable error) {
      StringBuilder message = new StringBuilder(String.valueOf(error.getMessage())).append('\n');
      try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
        error.printStackTrace(new PrintStream(output));
        message.append(new String(output.toByteArray(), UTF_8));
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
      return message.toString();
    }
  }

  record Disabled<E, T>(String name, String reason) implements TestResult<E, T>, Serializable {

    private static final long serialVersionUID = -8661817362831938094L;

    /**
     * it will throw a {@code NullPointerException} if any of the params are null
     * and {@code IllegalArgumentException} if name is a empty String.
     *
     * @param name name of the test, non empty value
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
    public <R> TestResult<E, R> map(Function1<T, R> mapper) {
      return new Disabled<>(name, reason);
    }

    @Override
    public String toString() {
      return String.format("test '%s' DISABLED: %s", name, reason);
    }
  }
}
