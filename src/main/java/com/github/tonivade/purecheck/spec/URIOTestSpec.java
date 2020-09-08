/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck.spec;

import static com.github.tonivade.purefun.concurrent.FutureOf.toFuture;
import static com.github.tonivade.purefun.effect.URIOOf.toURIO;
import java.util.concurrent.Executor;
import com.github.tonivade.purecheck.TestCase;
import com.github.tonivade.purecheck.TestFactory;
import com.github.tonivade.purecheck.TestReport;
import com.github.tonivade.purecheck.TestSuite;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.data.NonEmptyList;
import com.github.tonivade.purefun.effect.URIO_;
import com.github.tonivade.purefun.instances.FutureInstances;
import com.github.tonivade.purefun.instances.URIOInstances;

/**
 * A silly class to define the test case factory and reuse
 *
 * @author tonivade
 */
public abstract class URIOTestSpec<R> {

  protected final TestFactory<Kind<URIO_, R>> it = TestFactory.factory(URIOInstances.monadDefer());
  
  @SafeVarargs
  protected final <E> TestSuite<Kind<URIO_, R>, E> suite(
      R env, String name, TestCase<Kind<URIO_, R>, E, ?> test, TestCase<Kind<URIO_, R>, E, ?>... tests) {
    return new TestSuite<Kind<URIO_, R>, E>(URIOInstances.monad(), name, NonEmptyList.of(test, tests)) {
      @Override
      public TestReport<E> run() {
        return runK().fix(toURIO()).unsafeRunSync(env);
      }
      
      @Override
      public Future<TestReport<E>> parRun(Executor executor) {
        return runK().fix(toURIO()).foldMap(env, FutureInstances.async()).fix(toFuture());
      }
    };
  }
}
