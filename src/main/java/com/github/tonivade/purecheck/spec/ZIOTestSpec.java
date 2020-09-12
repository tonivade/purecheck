/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck.spec;

import static com.github.tonivade.purefun.Precondition.checkNonNull;
import static com.github.tonivade.purefun.concurrent.FutureOf.toFuture;
import static com.github.tonivade.purefun.effect.ZIOOf.toZIO;

import java.util.concurrent.Executor;

import com.github.tonivade.purecheck.TestCase;
import com.github.tonivade.purecheck.TestFactory;
import com.github.tonivade.purecheck.TestSuite;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Producer;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.data.NonEmptyList;
import com.github.tonivade.purefun.effect.ZIO_;
import com.github.tonivade.purefun.instances.FutureInstances;
import com.github.tonivade.purefun.instances.ZIOInstances;

/**
 * A silly class to define the test case factory and reuse
 *
 * @author tonivade
 */
public abstract class ZIOTestSpec<R> implements TestSpec<Kind<Kind<ZIO_, R>, Throwable>> {

  protected final TestFactory<Kind<Kind<ZIO_, R>, Throwable>> it = TestFactory.factory(ZIOInstances.monadDefer());
  
  private final Producer<R> factory;
  
  public ZIOTestSpec(Producer<R> factory) {
    this.factory = checkNonNull(factory);
  }

  @Override
  @SafeVarargs
  public final <E> TestSuite<Kind<Kind<ZIO_, R>, Throwable>, E> suite(
      String name, TestCase<Kind<Kind<ZIO_, R>, Throwable>, E, ?> test, TestCase<Kind<Kind<ZIO_, R>, Throwable>, E, ?>... tests) {
    return new TestSuite<Kind<Kind<ZIO_, R>, Throwable>, E>(ZIOInstances.monad(), name, NonEmptyList.of(test, tests)) {
      @Override
      public Report<E> run() {
        return runK().fix(toZIO()).provide(factory.get()).get();
      }
      
      @Override
      public Future<Report<E>> parRun(Executor executor) {
        return runK().fix(toZIO()).foldMap(factory.get(), FutureInstances.async()).fix(toFuture());
      }
    };
  }
}
