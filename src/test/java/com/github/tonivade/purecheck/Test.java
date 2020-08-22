package com.github.tonivade.purecheck;

import static com.github.tonivade.purefun.Validator.combine;
import static com.github.tonivade.purefun.Validator.endsWith;
import static com.github.tonivade.purefun.Validator.startsWith;

import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.type.Validation;
import com.github.tonivade.purefun.type.Validation.Result;

public class Test {
  
  static IO<String> hello(String name) {
    return IO.task(() -> "Hello " + name);
  }
  
  public static void main(String[] args) {
    String name = "Toni";

    TestCase<String, String> test = 
        TestCase.<String, String>test()
          .when(hello(name))
          .then(combine(startsWith("Hello"), endsWith(name)));
    
    Validation<Result<String>, String> validation = test.unsafeRun();
    
    System.out.println(validation);
  }
}
