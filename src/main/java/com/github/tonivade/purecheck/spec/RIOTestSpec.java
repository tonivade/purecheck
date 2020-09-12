/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck.spec;

import static com.github.tonivade.purefun.Precondition.checkNonNull;
import static com.github.tonivade.purefun.concurrent.FutureOf.toFuture;
import static com.github.tonivade.purefun.effect.RIOOf.toRIO;

import java.util.concurrent.Executor;

import com.github.tonivade.purecheck.TestCase;
import com.github.tonivade.purecheck.TestFactory;
import com.github.tonivade.purecheck.TestSuite;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Producer;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.data.NonEmptyList;
import com.github.tonivade.purefun.effect.RIO_;
import com.github.tonivade.purefun.instances.FutureInstances;
import com.github.tonivade.purefun.instances.RIOInstances;

/**
 * A silly class to define the test case factory and reuse
 *
 * @author tonivade
 */
public abstract class RIOTestSpec<R> implements TestSpec<Kind<RIO_, R>> {

  protected final TestFactory<Kind<RIO_, R>> it = TestFactory.factory(RIOInstances.monadDefer());
  
  private final Producer<R> factory;
  
  public RIOTestSpec(Producer<R> factory) {
    this.factory = checkNonNull(factory);
  }
  
  @Override
  @SafeVarargs
  public final <E> TestSuite<Kind<RIO_, R>, E> suite(
      String name, TestCase<Kind<RIO_, R>, E, ?> test, TestCase<Kind<RIO_, R>, E, ?>... tests) {
    return new TestSuite<Kind<RIO_, R>, E>(RIOInstances.monad(), name, NonEmptyList.of(test, tests)) {
      @Override
      public Report<E> run() {
        return runK().fix(toRIO()).safeRunSync(factory.get()).get();
      }
      
      @Override
      public Future<Report<E>> parRun(Executor executor) {
        return runK().fix(toRIO()).foldMap(factory.get(), FutureInstances.async()).fix(toFuture());
      }
    };
  }
}
