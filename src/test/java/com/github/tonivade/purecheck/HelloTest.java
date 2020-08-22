package com.github.tonivade.purecheck;

import static com.github.tonivade.purecheck.TestSuite.suite;
import static com.github.tonivade.purefun.Validator.combine;
import static com.github.tonivade.purefun.Validator.endsWith;
import static com.github.tonivade.purefun.Validator.startsWith;
import static com.github.tonivade.purefun.concurrent.Future.DEFAULT_EXECUTOR;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.monad.IO;

public class HelloTest {

  private IO<String> hello(String name) {
    return IO.task(() -> "Hello " + name);
  }
  
  @Test
  public void test() {
    String name = "Toni";

    TestCase<String, String> test1 = TestCase.<String, String>test("say hello")
        .when(hello(name))
        .then(combine(startsWith("Hello"), endsWith(name)));

    TestCase<String, String> test2 = TestCase.<String, String>test("don't say goodbye")
        .when(hello(name))
        .then(combine(startsWith("Bye"), endsWith(name)));

    Sequence<TestResult<String, Object>> parRun = suite(test1, test2).parRun(DEFAULT_EXECUTOR);
    
    assertThrows(AssertionError.class, () -> parRun.forEach(TestResult::assertion));
  }
}
