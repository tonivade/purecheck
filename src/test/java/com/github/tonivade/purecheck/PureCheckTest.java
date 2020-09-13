/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import static com.github.tonivade.purefun.Validator.equalsTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purecheck.spec.IOTestSpec;
import com.github.tonivade.purefun.monad.IO_;

public class PureCheckTest extends IOTestSpec<String> {
  
  @Test
  void test() {
    assertThrows(AssertionError.class, 
        () -> pureCheck("test", hello(), bye()).run().assertion());
  }
  
  TestSuite<IO_, String> hello() {
    return suite("suite 1", 
        it.should("say hello")
          .given("Toni")
          .when(name -> "Hello " + name)
          .thenMustBe(equalsTo("Hello Toni"))
        );
  }
  
  TestSuite<IO_, String> bye() {
    return suite("suite 2", 
        it.should("say goodbye")
          .given("Toni")
          .when(name -> "Goodbye " + name)
          .thenMustBe(equalsTo("Bye Toni"))
        );
  }
}
