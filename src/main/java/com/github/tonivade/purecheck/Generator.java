/*
 * Copyright (c) 2020-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import com.github.tonivade.purefun.Producer;
import java.util.concurrent.ThreadLocalRandom;

public interface Generator<T> extends Producer<T> {

  static Generator<Integer> randomInt() {
    return () -> ThreadLocalRandom.current().nextInt();
  }

  static Generator<Integer> randomInt(long seed) {
    return () -> {
      ThreadLocalRandom current = ThreadLocalRandom.current();
      current.setSeed(seed);
      return current.nextInt();
    };
  }
}
