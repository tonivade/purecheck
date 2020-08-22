package com.github.tonivade.purecheck;

import static com.github.tonivade.purefun.Precondition.checkNonEmpty;
import static com.github.tonivade.purefun.Precondition.checkNonNull;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.Objects;

import com.github.tonivade.purefun.Equal;
import com.github.tonivade.purefun.HigherKind;
import com.github.tonivade.purefun.Recoverable;
import com.github.tonivade.purefun.type.Validation.Result;

/**
 * it defines the result of a test case, given a name, a result and a value.
 * 
 * @author tonivade
 *
 * @param <E> type of the error
 * @param <T> type of the value
 */
@HigherKind(sealed = true)
public interface TestResult<E, T> {

  boolean isSuccess();

  boolean isFailure();

  boolean isError();

  void assertion();

  final class Success<E, T> implements SealedTestResult<E, T>, Serializable {

    private static final long serialVersionUID = 2612477493587755025L;

    private static final Equal<Success<?, ?>> EQUAL = Equal.<Success<?, ?>>of()
        .comparing(x -> x.name)
        .comparing(x -> x.value);

    private final String name;
    private final T value;

    /**
     * it will throw a {@code NullPointerException} if any of the params are null
     * and {@code IllegalArgumentException} if name is a empty String.
     * 
     * @param name name of the test, non empty value
     * @param value result of the oeration under test
     */
    protected Success(String name, T value) {
      this.name = checkNonEmpty(name);
      this.value = checkNonNull(value);
    }

    public boolean isSuccess() {
      return true;
    }

    public boolean isFailure() {
      return false;
    }
    
    @Override
    public boolean isError() {
      return false;
    }

    public void assertion() { }

    @Override
    public boolean equals(Object obj) {
      return EQUAL.applyTo(this, obj);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, value);
    }

    @Override
    public String toString() {
      return String.format("test '%s' SUCCESS: '%s'", name, value);
    }
  }

  final class Failure<E, T> implements SealedTestResult<E, T>, Serializable {

    private static final long serialVersionUID = 4834239536246492448L;

    private static final Equal<Failure<?, ?>> EQUAL = Equal.<Failure<?, ?>>of()
        .comparing(x -> x.name)
        .comparing(x -> x.value)
        .comparing(x -> x.result);

    private final String name;
    private final T value;
    private final Result<E> result;

    /**
     * it will throw a {@code NullPointerException} if any of the params are null
     * and {@code IllegalArgumentException} if name is a empty String.
     * 
     * @param name name of the test, non empty value
     * @param value result of the oeration under test
     * @param result result of the validation applied to the value
     */
    protected Failure(String name, T value, Result<E> result) {
      this.name = checkNonEmpty(name);
      this.value = checkNonNull(value);
      this.result = checkNonNull(result);
    }

    public boolean isSuccess() {
      return false;
    }

    public boolean isFailure() {
      return true;
    }
    
    @Override
    public boolean isError() {
      return false;
    }

    public void assertion() {
      throw new AssertionError(toString());
    }

    @Override
    public boolean equals(Object obj) {
      return EQUAL.applyTo(this, obj);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, value, result);
    }

    @Override
    public String toString() {
      return String.format("test '%s' FAILURE: expected '%s' but was '%s'", name, result.join(","), value);
    }
  }

  final class Error<E, T> implements SealedTestResult<E, T>, Recoverable, Serializable {

    private static final long serialVersionUID = 4181923995414226773L;

    private static final Equal<Error<?, ?>> EQUAL = Equal.<Error<?, ?>>of()
        .comparing(x -> x.name)
        .comparingArray(x -> x.error.getStackTrace());

    private final String name;
    private final Throwable error;

    /**
     * it will throw a {@code NullPointerException} if any of the params are null
     * and {@code IllegalArgumentException} if name is a empty String.
     * 
     * @param name name of the test, non empty value
     * @param value result of the oeration under test
     * @param result result of the validation applied to the value
     */
    protected Error(String name, Throwable error) {
      this.name = checkNonEmpty(name);
      this.error = checkNonNull(error);
    }

    public boolean isSuccess() {
      return false;
    }

    public boolean isFailure() {
      return false;
    }
    
    @Override
    public boolean isError() {
      return true;
    }

    public void assertion() {
      sneakyThrow(error);
    }

    @Override
    public boolean equals(Object obj) {
      return EQUAL.applyTo(this, obj);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, error);
    }

    @Override
    public String toString() {
      return String.format("test '%s' ERROR: %s", name, full(error));
    }

    private static String full(Throwable error) {
      StringBuilder message = new StringBuilder(String.valueOf(error.getMessage())).append('\n');
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        error.printStackTrace(new PrintStream(baos));
        message.append(new String(baos.toByteArray(), UTF_8));
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
      return message.toString();
    }
  }
}
