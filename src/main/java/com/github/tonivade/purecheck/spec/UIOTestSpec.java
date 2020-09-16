/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck.spec;

import com.github.tonivade.purecheck.TestSpec;
import com.github.tonivade.purefun.effect.UIO_;
import com.github.tonivade.purefun.instances.UIOInstances;
import com.github.tonivade.purefun.runtimes.Runtime;

/**
 * A silly class to define the test case factory and reuse
 *
 * @author tonivade
 */
public abstract class UIOTestSpec<E> extends TestSpec<UIO_, E> {

  public UIOTestSpec() {
    super(Runtime.uio(), UIOInstances.monadDefer());
  }
}
