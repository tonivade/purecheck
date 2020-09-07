/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import static com.github.tonivade.purefun.Validator.equalsTo;
import static com.github.tonivade.purefun.Validator.instanceOf;
import static com.github.tonivade.purefun.Validator.notEqualsTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purecheck.spec.UIOTestSpec;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.data.NonEmptyString;
import com.github.tonivade.purefun.effect.UIO_;

class NonEmptyStringTest extends UIOTestSpec {

  private final TestSuite<UIO_, String> suite = suite("NonEmptyString",

      it.should("not accept null")
          .<String>givenNull()
          .when(NonEmptyString::of)
          .thenThrows(instanceOf(IllegalArgumentException.class)),

      it.should("not accept empty string")
          .given("")
          .when(NonEmptyString::of)
          .thenThrows(instanceOf(IllegalArgumentException.class)),

      it.should("contains a non empty string")
          .given("hola mundo")
          .when(NonEmptyString::of)
          .thenMustBe(equalsTo("hola mundo").compose(NonEmptyString::get)),

      it.should("map inner value")
          .given(NonEmptyString.of("hola mundo"))
          .when(hello -> hello.map(String::toUpperCase))
          .thenMustBe(equalsTo("HOLA MUNDO").compose(NonEmptyString::get)),

      it.should("transform inner value")
          .given(NonEmptyString.of("hola mundo"))
          .when(hello -> hello.transform(String::toUpperCase))
          .thenMustBe(equalsTo("HOLA MUNDO")),

      it.should("be equals to other string `hola mundo`")
          .given(NonEmptyString.of("hola mundo"))
          .noop()
          .thenMustBe(equalsTo(NonEmptyString.of("hola mundo"))),

      it.should("not be equals to other string different to `hola mundo`")
          .given(NonEmptyString.of("hola mundo"))
          .noop()
          .thenMustBe(notEqualsTo(NonEmptyString.of("HOLA MUNDO")))
  );

  @Test
  void junit5() {
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
  void serial() {
    TestReport<String> run = suite.run();

    run.assertion();
    
    System.out.println(run);
  }

  @Test
  void parallel() {
    suite.parRun(Future.DEFAULT_EXECUTOR).await().onSuccess(TestReport::assertion);
  }
}