/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck.spec;

import static com.github.tonivade.purefun.typeclasses.Instance.applicative;
import static com.github.tonivade.purefun.typeclasses.Instance.monadDefer;
import static com.github.tonivade.purefun.typeclasses.Instance.runtime;

import com.github.tonivade.purecheck.TestSpec;
import com.github.tonivade.purefun.effect.Task_;

/**
 * A silly class to define the test case factory and reuse
 *
 * @author tonivade
 */
public abstract class TaskTestSpec<E> extends TestSpec<Task_, E> {

  public TaskTestSpec() {
    super(runtime(Task_.class), monadDefer(Task_.class), applicative(Task_.class));
  }
}
