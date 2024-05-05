/*
 * Copyright (c) 2020-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck.spec;

import com.github.tonivade.purecheck.TestSpec;
import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.typeclasses.Instance;

/**
 * A silly class to define the test case factory and reuse
 *
 * @author tonivade
 */
public abstract class UIOTestSpec<E> extends TestSpec<UIO<?>, E> {

  protected UIOTestSpec() {
    super(new Instance<UIO<?>>() {});
  }
}
