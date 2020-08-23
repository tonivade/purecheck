/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import static com.github.tonivade.purecheck.TestSuite.suite;
import static com.github.tonivade.purefun.Validator.endsWith;
import static com.github.tonivade.purefun.Validator.equalsTo;
import static com.github.tonivade.purefun.Validator.instanceOf;
import static com.github.tonivade.purefun.Validator.startsWith;
import static com.github.tonivade.purefun.concurrent.Future.DEFAULT_EXECUTOR;
import static java.lang.Thread.currentThread;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.Unit;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.type.Try;

public class HelloTest extends TestSpec<String, String> {
  
  private static IO<String> hello(String name) {
    return printThreadName()
        .andThen(IO.task(() -> "Hello " + name));
  }

  private static IO<String> error() {
    return printThreadName()
        .andThen(IO.raiseError(new RuntimeException()));
  }

  private static IO<Unit> printThreadName() {
    return IO.exec(() -> System.out.println(currentThread().getName()));
  }
  
  @Test
  public void testHello() {
    String name = "Toni";

    Try<TestReport<String>> result = 
        suite("some tests suite", 
            it.should("say hello")
              .when(hello(name))
              .thenCheck(equalsTo("Hello Toni")), 
            it.should("don't say goodbye")
              .when(hello(name))
              .thenCheck(startsWith("Bye")), 
            it.should("catch exceptions")
              .when(error())
              .onSuccess(startsWith("Bye").combine(endsWith(name)))
          ).parRun(DEFAULT_EXECUTOR).get();
    
    System.out.println(result.get());
    
    assertThrows(AssertionError.class, result.get()::assertion);
  }
  
  @Test
  public void testOnError() {
    TestReport<String> result = suite("some tests suite", 
        it.should("check if it fails")
          .when(error())
          .thenError(instanceOf(RuntimeException.class))
      ).run();
    
    System.out.println(result);
    
    assertDoesNotThrow(result::assertion);
  }
}
