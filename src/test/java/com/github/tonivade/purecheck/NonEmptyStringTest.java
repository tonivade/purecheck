package com.github.tonivade.purecheck;

import static com.github.tonivade.purecheck.TestSuite.suite;
import static com.github.tonivade.purefun.Validator.equalsTo;
import static com.github.tonivade.purefun.Validator.instanceOf;
import static com.github.tonivade.purefun.Validator.notEqualsTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.data.NonEmptyString;

public class NonEmptyStringTest extends TestSpec<String, NonEmptyString> {

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
    
    TestReport<String> report = suite("NonEmptyString",

        it.should("not accept null")
          .when(() -> NonEmptyString.of(null))
          .thenError(instanceOf(IllegalArgumentException.class)),

        it.should("not accept empty string")
          .when(() -> NonEmptyString.of(""))
          .thenError(instanceOf(IllegalArgumentException.class)),

        it.should("contains a non empty string")
          .when(() -> NonEmptyString.of("hola mundo"))
          .thenCheck(equalsTo("hola mundo").compose(NonEmptyString::get)),
          
        it.should("map inner value with a function")
          .when(() -> NonEmptyString.of("hola mundo").map(String::toUpperCase))
          .thenCheck(equalsTo("HOLA MUNDO").compose(NonEmptyString::get)),
          
        it.should("be equals to `hola mundo`")
          .when(() -> NonEmptyString.of("hola mundo"))
          .thenCheck(equalsTo(NonEmptyString.of("hola mundo"))),
          
        it.should("be not equals to `HOLA MUNDO`")
          .when(() -> NonEmptyString.of("hola mundo"))
          .thenCheck(notEqualsTo(NonEmptyString.of("HOLA MUNDO")))

        ).run();
    
    System.out.println(report);
    
    report.assertion();
  }
}