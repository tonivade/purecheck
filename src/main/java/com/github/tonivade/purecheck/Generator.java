/*
 * Copyright (c) 2020-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import java.util.concurrent.ThreadLocalRandom;

import com.github.tonivade.purefun.core.Producer;

public interface Generator<T> extends Producer<T> {

  static Generator<Integer> randomInt() {
    return () -> ThreadLocalRandom.current().nextInt();
  }
}
