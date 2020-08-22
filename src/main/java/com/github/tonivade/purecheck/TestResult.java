package com.github.tonivade.purecheck;

import static com.github.tonivade.purefun.Precondition.checkNonEmpty;
import static com.github.tonivade.purefun.Precondition.checkNonNull;

import com.github.tonivade.purefun.type.Validation;
import com.github.tonivade.purefun.type.Validation.Result;

/**
 * it defines the result of a test case, given a name, a result and a value.
 * 
 * @author tonivade
 *
 * @param <E> type of the error
 * @param <T> type of the value
 */
public class TestResult<E, T> {
  
  private final String name;
  private final T value;
  private final Validation<Result<E>, T> result;

  /**
   * it will throw a {@code NullPointerException} if any of the params are null
   * and {@code IllegalArgumentException} if name is a empty String.
   * 
   * @param name name of the test, non empty value
   * @param value result of the oeration under test
   * @param result result of the validation applied to the value
   */
  public TestResult(String name, T value, Validation<Result<E>, T> result) {
    this.name = checkNonEmpty(name);
    this.value = checkNonNull(value);
    this.result = checkNonNull(result);
  }
  
  @Override
  public String toString() {
    return result.fold(
        error -> String.format("test '%s' FAILURE: expected '%s' but was '%s'", name, error, value), 
        value -> String.format("test '%s' SUCCESS: '%s'", name, value));
    
  }
}
