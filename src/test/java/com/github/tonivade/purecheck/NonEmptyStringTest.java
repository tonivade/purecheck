/*
 * Copyright (c) 2020-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import static com.github.tonivade.purefun.core.Validator.equalsTo;
import static com.github.tonivade.purefun.core.Validator.notEqualsTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purecheck.spec.UIOTestSpec;
import com.github.tonivade.purefun.data.NonEmptyString;
import com.github.tonivade.purefun.effect.UIO;

class NonEmptyStringTest extends UIOTestSpec<String> {

  private final TestSuite<UIO<?>, String> suite = suite("NonEmptyString",

      it.should("not accept null")
          .<String>givenNull()
          .when(NonEmptyString::of)
          .thenThrows(IllegalArgumentException.class),

      it.should("not accept empty string")
          .given("")
          .when(NonEmptyString::of)
          .thenThrows(IllegalArgumentException.class),

      it.should("contains a non empty string")
          .given("hola mundo")
          .when(NonEmptyString::of)
          .then(equalsTo("hola mundo").compose(NonEmptyString::get)),

      it.should("map inner value")
          .given(NonEmptyString.of("hola mundo"))
          .when(hello -> hello.map(String::toUpperCase))
          .then(equalsTo("HOLA MUNDO").compose(NonEmptyString::get)),

      it.should("transform inner value")
          .given(NonEmptyString.of("hola mundo"))
          .when(hello -> hello.transform(String::toUpperCase))
          .then(equalsTo("HOLA MUNDO")),

      it.should("be equals to other string `hola mundo`")
          .given(NonEmptyString.of("hola mundo"))
          .noop()
          .then(equalsTo(NonEmptyString.of("hola mundo"))),

      it.should("not be equals to other string different to `hola mundo`")
          .given(NonEmptyString.of("hola mundo"))
          .noop()
          .then(notEqualsTo(NonEmptyString.of("HOLA MUNDO")))

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
    suite.run().assertion();

    System.out.println(suite.run());
  }

  @Test
  void parallel() {
    suite.parRun().await().onSuccess(TestSuite.Report::assertion);
  }
}