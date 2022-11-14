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
