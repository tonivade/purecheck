/*
 * Copyright (c) 2020-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import static com.github.tonivade.purefun.Function1.identity;
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

import com.github.tonivade.purecheck.spec.IOTestSpec;
import com.github.tonivade.purefun.Unit;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.type.Try;

@ExtendWith(MockitoExtension.class)
class HelloTest extends IOTestSpec<String> {

  @Test
  void testHello() {
    Try<TestSuite.Report<String>> result =
        suite("some tests suite",

            it.should("say hello")
              .given("Toni")
              .run(HelloTest::hello)
              .thenMustBe(equalsTo("Hello Toni")),

            it.should("don't say goodbye")
              .given("Toni")
              .run(HelloTest::hello)
              .thenMustBe(startsWith("Bye")),

            it.should("catch exceptions")
              .given("Toni")
              .when(error())
              .thenOnSuccess(startsWith("Bye").combine(endsWith("Toni"))),

            it.should("disabled test")
              .given("Toni")
              .when(error())
              .thenOnSuccess(startsWith("Bye").combine(endsWith("Toni")))
              .disable("not working")

          ).parRun(Future.DEFAULT_EXECUTOR).await();
    
    System.out.println(result.get());
    
    assertThrows(AssertionError.class, result.get()::assertion);
  }

  @Test
  void testOnError() {
    TestSuite.Report<String> result = suite("some tests suite", 
        it.should("check if it fails")
          .noGiven()
          .when(error())
          .thenThrows(instanceOf(RuntimeException.class))
      ).run();
    
    System.out.println(result);
    
    assertDoesNotThrow(result::assertion);
  }
  
  @Test
  void repeat(@Mock IO<String> task) {
    when(task.unsafeRunSync()).thenReturn("Hello Toni");
    
    TestSuite.Report<String> result =
        suite("some tests suite",
            it.should("reapeat")
              .given(task)
              .run(identity())
              .thenMustBe(equalsTo("Hello Toni")).repeat(3)
            ).run();
    
    verify(task, times(4)).unsafeRunSync();
    
    System.out.println(result);
  }

  @Test
  void retryOnErrorWhenSuccess(@Mock IO<String> task) {
    when(task.unsafeRunSync())
        .thenReturn("Hello Toni");

    TestSuite.Report<String> result =
        suite("some tests suite",
            it.should("retry on error")
                .given(task)
                .run(identity())
                .thenMustBe(equalsTo("Hello Toni")).retryOnError(3)
        ).run();

    verify(task, times(1)).unsafeRunSync();

    System.out.println(result);
  }

  @Test
  void retryOnError(@Mock IO<String> task) {
    when(task.unsafeRunSync())
      .thenThrow(RuntimeException.class)
      .thenReturn("Hello Toni");
    
    TestSuite.Report<String> result =
        suite("some tests suite",
            it.should("retry on error")
              .given(task)
              .run(identity())
              .thenMustBe(equalsTo("Hello Toni")).retryOnError(3)
            ).run();
    
    verify(task, times(2)).unsafeRunSync();
    
    System.out.println(result);
  }

  @Test
  void retryOnFailureWhenSuccess(@Mock IO<String> task) {
    when(task.unsafeRunSync())
        .thenReturn("Hello Toni");

    TestSuite.Report<String> result =
        suite("some tests suite",
            it.should("retry on failure")
                .given(task)
                .run(identity())
                .thenMustBe(equalsTo("Hello Toni")).retryOnFailure(3)
        ).run();

    verify(task, times(1)).unsafeRunSync();

    System.out.println(result);
  }

  @Test
  void retryOnFailure(@Mock IO<String> task) {
    when(task.unsafeRunSync())
      .thenReturn("Hello World")
      .thenReturn("Hello Toni");
    
    TestSuite.Report<String> result =
        suite("some tests suite",
            it.should("retry on failure")
              .given(task)
              .run(identity())
              .thenMustBe(equalsTo("Hello Toni")).retryOnFailure(3)
            ).run();
    
    verify(task, times(2)).unsafeRunSync();
    
    System.out.println(result);
  }
  
  @Test
  void timed() {
    TestSuite.Report<String> result =
        suite("some tests suite",
            it.should("timed")
              .given("Toni")
              .run(name -> hello(name))
              .thenMustBe(equalsTo("Hello Toni")).timed()
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
