package com.github.tonivade.purecheck;

import static com.github.tonivade.purecheck.TestSuite.suite;
import static com.github.tonivade.purefun.Validator.combine;
import static com.github.tonivade.purefun.Validator.endsWith;
import static com.github.tonivade.purefun.Validator.startsWith;
import static com.github.tonivade.purefun.concurrent.Future.DEFAULT_EXECUTOR;

import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.monad.IO;

public class Test {

  static IO<String> hello(String name) {
    return IO.task(() -> "Hello " + name);
  }

  public static void main(String[] args) {
    String name = "Toni";

    TestCase<String, String> test1 = TestCase.<String, String>test("say hello").when(hello(name))
        .then(combine(startsWith("Hello"), endsWith(name)));

    TestCase<String, String> test2 = TestCase.<String, String>test("don't say goodbye").when(hello(name))
        .then(combine(startsWith("Bye"), endsWith(name)));

    Sequence<TestResult<String, Object>> parRun = suite(test1, test2).parRun(DEFAULT_EXECUTOR);
    
    System.out.println(parRun);
  }
}
