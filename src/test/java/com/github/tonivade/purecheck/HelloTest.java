/*
 * Copyright (c) 2020-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
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
import com.github.tonivade.purefun.Producer;
import com.github.tonivade.purefun.Unit;
import com.github.tonivade.purefun.monad.IO;

@ExtendWith(MockitoExtension.class)
class HelloTest extends IOTestSpec<String> {

  @Test
  void testHello() {
    var result =
        suite("some tests suite",

            it.should("say hello")
              .given("Toni")
              .whenK(HelloTest::hello)
              .then(equalsTo("Hello Toni")),

            it.should("don't say goodbye")
              .given("Toni")
              .whenK(HelloTest::hello)
              .then(startsWith("Bye")),

            it.should("catch exceptions")
              .given("Toni")
              .when(error())
              .onSuccess(startsWith("Bye").combine(endsWith("Toni"))),

            it.should("disabled test")
              .given("Toni")
              .when(error())
              .onSuccess(startsWith("Bye").combine(endsWith("Toni")))
              .disable("not working")

          ).parRun().await();

    System.out.println(result.getOrElseThrow());

    assertThrows(AssertionError.class, result.getOrElseThrow()::assertion);
  }

  @Test
  void testOnError() {
    var result = suite("some tests suite",
        it.should("check if it fails")
          .noGiven()
          .when(error())
          .thenThrows(instanceOf(RuntimeException.class))
      ).run();

    System.out.println(result);

    assertDoesNotThrow(result::assertion);
  }

  @Test
  void repeat(@Mock Producer<String> task) {
    when(task.get()).thenReturn("Hello Toni");

    var result =
        suite("some tests suite",
            it.should("reapeat")
              .given(IO.task(task))
              .whenK(identity())
              .then(equalsTo("Hello Toni")).repeat(3)
            ).run();

    verify(task, times(4)).get();

    System.out.println(result);
  }

  @Test
  void retryOnErrorWhenSuccess(@Mock Producer<String> task) {
    when(task.get())
        .thenReturn("Hello Toni");

    IO.task(task);

    var result =
        suite("some tests suite",
            it.should("retry on error")
                .given(IO.task(task))
                .whenK(identity())
                .then(equalsTo("Hello Toni")).retryOnError(3)
        ).run();

    verify(task, times(1)).get();

    System.out.println(result);
  }

  @Test
  void retryOnError(@Mock Producer<String> task) {
    when(task.get())
      .thenThrow(RuntimeException.class)
      .thenThrow(RuntimeException.class)
      .thenThrow(RuntimeException.class)
      .thenReturn("Hello Toni");

    var result =
        suite("some tests suite",
            it.should("retry on error")
              .given(IO.task(task))
              .whenK(identity())
              .then(equalsTo("Hello Toni")).retryOnError(10)
            ).run();

    verify(task, times(4)).get();

    System.out.println(result);
  }

  @Test
  void retryOnFailureWhenSuccess(@Mock Producer<String> task) {
    when(task.get())
        .thenReturn("Hello Toni");

    var result =
        suite("some tests suite",
            it.should("retry on failure")
                .given(IO.task(task))
                .whenK(identity())
                .then(equalsTo("Hello Toni")).retryOnFailure(3)
        ).run();

    verify(task, times(1)).get();

    System.out.println(result);
  }

  @Test
  void retryOnFailure(@Mock Producer<String> task) {
    when(task.get())
      .thenReturn("Hello World")
      .thenReturn("Hello World")
      .thenReturn("Hello World")
      .thenReturn("Hello Toni");

    var result =
        suite("some tests suite",
            it.should("retry on failure")
              .given(IO.task(task))
              .whenK(identity())
              .then(equalsTo("Hello Toni")).retryOnFailure(10)
            ).run();

    verify(task, times(4)).get();

    System.out.println(result);
  }

  @Test
  void timed() {
    var result =
        suite("some tests suite",
            it.should("timed")
              .given("Toni")
              .whenK(name -> hello(name))
              .then(equalsTo("Hello Toni")).timed()
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
