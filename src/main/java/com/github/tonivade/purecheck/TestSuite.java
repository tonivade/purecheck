package com.github.tonivade.purecheck;

import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.concurrent.Par;
import com.github.tonivade.purefun.data.ImmutableList;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.type.Validation;
import com.github.tonivade.purefun.type.Validation.Result;

public class TestSuite<E> {

  private final ImmutableList<TestCase<E, ?>> tests;
  
  public TestSuite(Iterable<TestCase<E, ?>> tests) {
    this.tests = ImmutableList.from(tests);
  }
  
  public Sequence<Validation<Result<E>, ?>> run() {
    return tests.map(TestCase::unsafeRun);
  }

  public Sequence<Validation<Result<E>, ?>> parRun() {
    ImmutableList<Par<Validation<Result<E>, ?>>> map = tests.map(TestCase::asyncRun);
    
    Par<Sequence<Validation<Result<E>, ?>>> traverse = Par.traverse(map);
    
    return traverse.run(Future.DEFAULT_EXECUTOR).get().getOrElseThrow(RuntimeException::new),
  }
}
