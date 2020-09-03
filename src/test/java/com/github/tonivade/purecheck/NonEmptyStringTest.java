/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import static com.github.tonivade.purecheck.TestSuite.suite;
import static com.github.tonivade.purefun.Validator.equalsTo;
import static com.github.tonivade.purefun.Validator.instanceOf;
import static com.github.tonivade.purefun.Validator.notEqualsTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tonivade.purefun.Function1;
import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.data.NonEmptyString;

public class NonEmptyStringTest extends TestSpec<String> {

  private final TestSuite<String> suite = suite("NonEmptyString",

      it.<String, NonEmptyString>should("not accept null")
          .given(null)
          .when(NonEmptyString::of)
          .thenError(instanceOf(IllegalArgumentException.class)),

      it.<String, NonEmptyString>should("not accept empty string")
          .given("")
          .when(NonEmptyString::of)
          .thenError(instanceOf(IllegalArgumentException.class)),

      it.<String, NonEmptyString>should("contains a non empty string")
          .given("hola mundo")
          .when(NonEmptyString::of)
          .thenCheck(equalsTo("hola mundo").compose(NonEmptyString::get)),

      it.<NonEmptyString, NonEmptyString>should("map inner value")
          .given(NonEmptyString.of("hola mundo"))
          .when(hello -> hello.map(String::toUpperCase))
          .thenCheck(equalsTo("HOLA MUNDO").compose(NonEmptyString::get)),

      it.<NonEmptyString, String>should("transform inner value")
          .given(NonEmptyString.of("hola mundo"))
          .when(hello -> hello.transform(String::toUpperCase))
          .thenCheck(equalsTo("HOLA MUNDO")),

      it.<NonEmptyString, NonEmptyString>should("be equals to other string `hola mundo`")
          .given(NonEmptyString.of("hola mundo"))
          .when(Function1.identity())
          .thenCheck(equalsTo(NonEmptyString.of("hola mundo"))),

      it.<NonEmptyString, NonEmptyString>should("not be equals to other string different to `hola mundo`")
          .given(NonEmptyString.of("hola mundo"))
          .when(Function1.identity())
          .thenCheck(notEqualsTo(NonEmptyString.of("HOLA MUNDO")))
  );

  @Test
  public void junit5() {
    assertAll(
        () -> assertThrows(IllegalArgumentException.class, () -> NonEmptyString.of(null)),
        () -> assertThrows(IllegalArgumentException.class, () -> NonEmptyString.of("")),
        () -> assertDoesNotThrow(() -> NonEmptyString.of("hola mundo")),
        () -> assertEquals("hola mundo", NonEmptyString.of("hola mundo").get()),
        () -> assertEquals("HOLA MUNDO", NonEmptyString.of("hola mundo").transform(String::toUpperCase)),
        () -> assertEquals(NonEmptyString.of("HOLA MUNDO"), NonEmptyString.of("hola mundo").map(String::toUpperCase)),
        () -> assertEquals(NonEmptyString.of("hola mundo"), NonEmptyString.of("hola mundo"))
    );
  }

  @Test
  public void serial() {
    TestReport<String> run = suite.run();

    run.assertion();
    
    System.out.println(run);
  }

  @Test
  public void parallel() {
    suite.parRun(Future.DEFAULT_EXECUTOR).await().onSuccess(TestReport::assertion);
  }
}