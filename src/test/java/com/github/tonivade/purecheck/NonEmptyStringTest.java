package com.github.tonivade.purecheck;

import static com.github.tonivade.purecheck.TestSuite.suite;
import static com.github.tonivade.purefun.Validator.equalsTo;
import static com.github.tonivade.purefun.Validator.instanceOf;
import static com.github.tonivade.purefun.Validator.notEqualsTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tonivade.purefun.Producer;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.data.NonEmptyString;
import org.junit.jupiter.api.Test;

public class NonEmptyStringTest extends TestSpec<String> {

  private final TestSuite<String> suite = suite("NonEmptyString",

      it.<NonEmptyString>should("not accept null")
          .when(sayHello(null))
          .thenError(instanceOf(IllegalArgumentException.class)),

      it.<NonEmptyString>should("not accept empty string")
          .when(sayHello(""))
          .thenError(instanceOf(IllegalArgumentException.class)),

      it.<NonEmptyString>should("contains a non empty string")
          .when(sayHello("hola mundo"))
          .thenCheck(equalsTo("hola mundo").compose(NonEmptyString::get)),

      it.<NonEmptyString>should("map inner value")
          .when(sayHello("hola mundo").map(string -> string.map(String::toUpperCase)))
          .thenCheck(equalsTo("HOLA MUNDO").compose(NonEmptyString::get)),

      it.<String>should("transform inner value")
          .when(sayHello("hola mundo").map(string -> string.transform(String::toUpperCase)))
          .thenCheck(equalsTo("HOLA MUNDO")),

      it.<NonEmptyString>should("be equals to `hola mundo`")
          .when(sayHello("hola mundo"))
          .thenCheck(equalsTo(NonEmptyString.of("hola mundo"))),

      it.<NonEmptyString>should("be not equals to `HOLA MUNDO`")
          .when(sayHello("hola mundo"))
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
    suite.run().assertion();
  }

  @Test
  public void parallel() {
    suite.parRun(Future.DEFAULT_EXECUTOR).get().onSuccess(TestReport::assertion);
  }

  private Producer<NonEmptyString> sayHello(String s) {
    return () -> NonEmptyString.of(s);
  }
}