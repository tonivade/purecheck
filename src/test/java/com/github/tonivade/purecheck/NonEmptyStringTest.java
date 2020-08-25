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
import com.github.tonivade.purefun.type.Try;
import org.junit.jupiter.api.Test;

public class NonEmptyStringTest extends TestSpec<String> {

  @Test
  public void test() {
    NonEmptyString nonEmptyString = NonEmptyString.of("hola mundo");

    assertAll(
        () -> assertThrows(IllegalArgumentException.class, () -> NonEmptyString.of(null)),
        () -> assertThrows(IllegalArgumentException.class, () -> NonEmptyString.of("")),
        () -> assertDoesNotThrow(() -> NonEmptyString.of("hola mundo")),
        () -> assertEquals("hola mundo", nonEmptyString.get()),
        () -> assertEquals("HOLA MUNDO", nonEmptyString.transform(String::toUpperCase)),
        () -> assertEquals(NonEmptyString.of("HOLA MUNDO"), nonEmptyString.map(String::toUpperCase)),
        () -> assertEquals(NonEmptyString.of("hola mundo"), NonEmptyString.of("hola mundo"))
    );
  }
  
  @Test
  public void other() {
    Try<TestReport<String>> report = suite("NonEmptyString",

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

        ).parRun(Future.DEFAULT_EXECUTOR).get();
    
    report.onSuccess(System.out::println).onSuccess(TestReport::assertion);
  }

  private Producer<NonEmptyString> sayHello(String s) {
    return () -> NonEmptyString.of(s);
  }
}