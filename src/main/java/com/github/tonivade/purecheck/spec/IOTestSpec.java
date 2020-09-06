/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck.spec;

import com.github.tonivade.purecheck.TestFactory;
import com.github.tonivade.purefun.instances.IOInstances;
import com.github.tonivade.purefun.monad.IO_;

/**
 * A silly class to define the test case factory and reuse
 *
 * @author tonivade
 */
public class IOTestSpec {

  protected final TestFactory<IO_> it = TestFactory.factory(IOInstances.monadDefer());
  
}
