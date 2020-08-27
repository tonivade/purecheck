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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.tonivade.purefun.Unit;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.type.Try;

@ExtendWith(MockitoExtension.class)
public class HelloTest extends TestSpec<String> {

  @Test
  public void testHello() {
    String name = "Toni";

    Try<TestReport<String>> result =
        suite("some tests suite",

            it.<String>should("say hello")
              .when(hello(name))
              .thenCheck(equalsTo("Hello Toni")),

            it.<String>should("don't say goodbye")
              .when(hello(name))
              .thenCheck(startsWith("Bye")),

            it.<String>should("catch exceptions")
              .when(error())
              .onSuccess(startsWith("Bye").combine(endsWith(name))),

            it.<String>should("disabled test")
              .when(error())
              .onSuccess(startsWith("Bye").combine(endsWith(name)))
              .disable("not working")

          ).parRun(Future.DEFAULT_EXECUTOR).await();
    
    System.out.println(result.get());
    
    assertThrows(AssertionError.class, result.get()::assertion);
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
  
  @Test
  public void repeat(@Mock IO<String> task) {
    when(task.attempt()).thenCallRealMethod();
    when(task.unsafeRunSync()).thenReturn("Hello Toni");
    
    TestReport<String> result =
        suite("some tests suite",
            it.<String>should("reapeat")
              .when(task)
              .thenCheck(equalsTo("Hello Toni")).repeat(3)
            ).run();
    
    verify(task, times(4)).unsafeRunSync();
    
    System.out.println(result);
  }
  
  @Test
  public void retryOnError(@Mock IO<String> task) {
    when(task.attempt()).thenCallRealMethod();
    when(task.unsafeRunSync())
      .thenThrow(RuntimeException.class)
      .thenReturn("Hello Toni");
    
    TestReport<String> result =
        suite("some tests suite",
            it.<String>should("retry on error")
              .when(task)
              .thenCheck(equalsTo("Hello Toni")).retryOnError(3)
            ).run();
    
    verify(task, times(2)).unsafeRunSync();
    
    System.out.println(result);
  }
  
  @Test
  public void retryOnFailure(@Mock IO<String> task) {
    when(task.attempt()).thenCallRealMethod();
    when(task.unsafeRunSync())
      .thenReturn("Hello World")
      .thenReturn("Hello Toni");
    
    TestReport<String> result =
        suite("some tests suite",
            it.<String>should("retry on failure")
              .when(task)
              .thenCheck(equalsTo("Hello Toni")).retryOnFailure(3)
            ).run();
    
    verify(task, times(2)).unsafeRunSync();
    
    System.out.println(result);
  }
  
  @Test
  public void timed() {
    TestReport<String> result =
        suite("some tests suite",
            it.<String>should("timed")
              .when(() -> "Hello Toni")
              .thenCheck(equalsTo("Hello Toni")).timed()
            ).run();

    System.out.println(result);
  }

  private static IO<String> hello(String name) {
    return printThreadName().andThen(IO.sleep(Duration.ofSeconds(1)))
            .andThen(IO.task(() -> "Hello " + name));
  }

  private static IO<String> error() {
    return printThreadName().andThen(IO.sleep(Duration.ofSeconds(1)))
            .andThen(IO.raiseError(new RuntimeException()));
  }

  private static IO<Unit> printThreadName() {
    return IO.exec(() -> System.out.println(currentThread().getName()));
  }
}
