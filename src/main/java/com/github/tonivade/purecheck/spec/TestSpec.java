/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck.spec;

import com.github.tonivade.purecheck.TestCase;
import com.github.tonivade.purecheck.TestSuite;
import com.github.tonivade.purefun.Witness;

interface TestSpec<F extends Witness> {

  @SuppressWarnings("unchecked")
  <E> TestSuite<F, E> suite(
      String name, TestCase<F, E, ?> test, TestCase<F, E, ?>... tests);

}
