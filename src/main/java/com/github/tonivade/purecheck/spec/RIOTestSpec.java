/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck.spec;

import com.github.tonivade.purecheck.TestSpec;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.effect.RIO_;
import com.github.tonivade.purefun.instances.RIOInstances;
import com.github.tonivade.purefun.runtimes.Runtime;

/**
 * A silly class to define the test case factory and reuse
 *
 * @author tonivade
 */
public abstract class RIOTestSpec<R, E> extends TestSpec<Kind<RIO_, R>, E> {
  
  public RIOTestSpec(R env) {
    super(Runtime.rio(env), RIOInstances.monadDefer());
  }
}
