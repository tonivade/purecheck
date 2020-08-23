package com.github.tonivade.purecheck;

import static com.github.tonivade.purecheck.TestSuite.suite;
import static com.github.tonivade.purefun.Validator.combine;
import static com.github.tonivade.purefun.Validator.endsWith;
import static com.github.tonivade.purefun.Validator.equalsTo;
import static com.github.tonivade.purefun.Validator.startsWith;
import static com.github.tonivade.purefun.concurrent.Future.DEFAULT_EXECUTOR;
import static java.lang.Thread.currentThread;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.Unit;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.type.Try;

public class HelloTest extends TestSpec<String, String> {
  
  private IO<String> hello(String name) {
    return printThreadName()
        .andThen(IO.task(() -> "Hello " + name));
  }

  private IO<String> error() {
    return printThreadName()
        .andThen(IO.raiseError(new RuntimeException()));
  }

  private IO<Unit> printThreadName() {
    return IO.exec(() -> System.out.println(currentThread().getName()));
  }
  
  @Test
  public void testHello() {
    String name = "Toni";

    TestCase<String, String> test1 = it.should("say hello")
        .when(hello(name))
        .check(equalsTo("Hello Toni"));

    TestCase<String, String> test2 = it.should("don't say goodbye")
        .when(hello(name))
        .check(startsWith("Bye"));

    TestCase<String, String> test3 = it.should("catch exceptions")
        .when(error())
        .then(combine(startsWith("Bye"), endsWith(name)));

    Try<TestReport<String>> result = 
        suite("some tests suite", test1, test2, test3).parRun(DEFAULT_EXECUTOR).get();
    
    System.out.println(result.get());
    
    assertThrows(AssertionError.class, () -> result.get().forEach(TestResult::assertion));
  }
}
