package com.github.tonivade.purecheck;

import static com.github.tonivade.purecheck.TestSuite.suite;
import static com.github.tonivade.purefun.Validator.combine;
import static com.github.tonivade.purefun.Validator.endsWith;
import static com.github.tonivade.purefun.Validator.startsWith;
import static com.github.tonivade.purefun.concurrent.Future.DEFAULT_EXECUTOR;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.monad.IO;

public class HelloTest extends TestSpec<String, String> {
  
  private IO<String> hello(String name) {
    return IO.task(() -> "Hello " + name);
  }
  
  @Test
  public void testHello() {
    String name = "Toni";

    TestCase<String, String> test1 = it.should("say hello")
        .when(hello(name))
        .then(combine(startsWith("Hello"), endsWith(name)));

    TestCase<String, String> test2 = it.should("don't say goodbye")
        .when(hello(name))
        .then(combine(startsWith("Bye"), endsWith(name)));

    TestCase<String, String> test3 = it.should("catch exceptions")
        .when(IO.raiseError(new RuntimeException()))
        .then(combine(startsWith("Bye"), endsWith(name)));

    TestReport<String> result = suite(test1, test2, test3).parRun(DEFAULT_EXECUTOR);
    
    System.out.println(result);
    
    assertThrows(AssertionError.class, () -> result.forEach(TestResult::assertion));
  }
}
