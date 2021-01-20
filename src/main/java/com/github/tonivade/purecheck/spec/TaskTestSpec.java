/*
 * Copyright (c) 2020-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck.spec;

import com.github.tonivade.purecheck.TestSpec;
import com.github.tonivade.purefun.effect.Task_;

/**
 * A silly class to define the test case factory and reuse
 *
 * @author tonivade
 */
public abstract class TaskTestSpec<E> extends TestSpec<Task_, E> {

  protected TaskTestSpec() {
    super(Task_.class);
  }
}
