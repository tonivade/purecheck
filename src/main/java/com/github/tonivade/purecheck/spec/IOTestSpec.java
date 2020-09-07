/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck.spec;

import static com.github.tonivade.purefun.concurrent.FutureOf.toFuture;
import static com.github.tonivade.purefun.monad.IOOf.toIO;
import java.util.concurrent.Executor;
import com.github.tonivade.purecheck.TestCase;
import com.github.tonivade.purecheck.TestFactory;
import com.github.tonivade.purecheck.TestReport;
import com.github.tonivade.purecheck.TestSuite;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.data.NonEmptyList;
import com.github.tonivade.purefun.instances.FutureInstances;
import com.github.tonivade.purefun.instances.IOInstances;
import com.github.tonivade.purefun.monad.IO_;

/**
 * A silly class to define the test case factory and reuse
 *
 * @author tonivade
 */
public abstract class IOTestSpec {

  protected final TestFactory<IO_> it = TestFactory.factory(IOInstances.monadDefer());
  
  @SafeVarargs
  public static <E> TestSuite<IO_, E> suite(
      String name, TestCase<IO_, E, ?> test, TestCase<IO_, E, ?>... tests) {
    return new TestSuite<IO_, E>(IOInstances.monad(), name, NonEmptyList.of(test, tests)) {
      @Override
      public TestReport<E> run() {
        return runK().fix(toIO()).unsafeRunSync();
      }
      
      @Override
      public Future<TestReport<E>> parRun(Executor executor) {
        return runK().fix(toIO()).foldMap(FutureInstances.monadDefer()).fix(toFuture());
      }
    };
  }
}
