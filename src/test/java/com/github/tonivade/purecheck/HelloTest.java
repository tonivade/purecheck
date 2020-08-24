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
import static java.lang.Thread.currentThread;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.Unit;
import com.github.tonivade.purefun.monad.IO;

public class HelloTest extends TestSpec<String> {

  @Test
  public void testHello() {
    String name = "Toni";

    TestReport<String> result =
        suite("some tests suite",

            it.<String>should("say hello")
              .when(hello(name))
              .thenCheck(equalsTo("Hello Toni")),

            it.<String>should("don't say goodbye")
              .when(hello(name))
              .thenCheck(startsWith("Bye")),

            it.<String>should("catch exceptions")
              .when(error())
              .onSuccess(startsWith("Bye").combine(endsWith(name)))

          ).run();
    
    System.out.println(result);
    
    assertThrows(AssertionError.class, result::assertion);
  }
  
  @Test
  public void testOnError() {
    TestReport<String> result = suite("some tests suite", 
        it.<String>should("check if it fails")
          .when(error())
          .thenError(instanceOf(RuntimeException.class))
      ).run();
    
    System.out.println(result);
    
    assertDoesNotThrow(result::assertion);
  }

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
}
