/*
 * Copyright (c) 2020-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck.spec;

import com.github.tonivade.purecheck.TestSpec;
import com.github.tonivade.purefun.monad.IO_;

/**
 * A silly class to define the test case factory and reuse
 *
 * @author tonivade
 */
public abstract class IOTestSpec<E> extends TestSpec<IO_, E> {

  protected IOTestSpec() {
    super(IO_.class);
  }
}
