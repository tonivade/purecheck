package com.github.tonivade.purecheck;

import static com.github.tonivade.purecheck.TestCase.factory;
import static com.github.tonivade.purecheck.TestSuite.suite;
import static com.github.tonivade.purefun.Validator.combine;
import static com.github.tonivade.purefun.Validator.endsWith;
import static com.github.tonivade.purefun.Validator.startsWith;
import static com.github.tonivade.purefun.concurrent.Future.DEFAULT_EXECUTOR;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.monad.IO;

public class HelloTest {
  
  private final TestFactory<String, String> it = factory();

  private IO<String> hello(String name) {
    return IO.task(() -> "Hello " + name);
  }
  
  @Test
  public void testHello() {
    String name = "Toni";

    var test1 = it.should("say hello")
        .when(hello(name))
        .then(combine(startsWith("Hello"), endsWith(name)));

    var test2 = it.should("don't say goodbye")
        .when(hello(name))
        .then(combine(startsWith("Bye"), endsWith(name)));

    var result = suite(test1, test2).parRun(DEFAULT_EXECUTOR);
    
    assertThrows(AssertionError.class, () -> result.forEach(TestResult::assertion));
  }
}
