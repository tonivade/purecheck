package com.github.tonivade.purecheck;

import static com.github.tonivade.purefun.Precondition.checkNonEmpty;
import static com.github.tonivade.purefun.Precondition.checkNonNull;

import com.github.tonivade.purefun.type.Validation;
import com.github.tonivade.purefun.type.Validation.Result;

public class TestResult<E, T> {
  
  private final String name;
  private final Validation<Result<E>, T> result;

  public TestResult(String name, Validation<Result<E>, T> result) {
    this.name = checkNonEmpty(name);
    this.result = checkNonNull(result);
  }
  
  @Override
  public String toString() {
    return String.format("%s -> %s", name, result);
  }
}
