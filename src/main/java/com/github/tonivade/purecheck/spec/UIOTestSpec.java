/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck.spec;

import com.github.tonivade.purecheck.TestFactory;
import com.github.tonivade.purefun.effect.UIO_;
import com.github.tonivade.purefun.instances.UIOInstances;

/**
 * A silly class to define the test case factory and reuse
 *
 * @author tonivade
 */
public abstract class UIOTestSpec {

  protected final TestFactory<UIO_> it = TestFactory.factory(UIOInstances.monadDefer());
  
}
