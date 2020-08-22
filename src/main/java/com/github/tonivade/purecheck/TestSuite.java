package com.github.tonivade.purecheck;

import java.util.concurrent.Executor;

import com.github.tonivade.purefun.concurrent.Par;
import com.github.tonivade.purefun.data.ImmutableList;
import com.github.tonivade.purefun.data.Sequence;

public class TestSuite<E> {

  private final ImmutableList<TestCase<E, Object>> tests;
  
  public TestSuite(Iterable<TestCase<E, Object>> tests) {
    this.tests = ImmutableList.from(tests);
  }
  
  public Sequence<TestResult<E, Object>> run() {
    return tests.map(TestCase::unsafeRun);
  }

  public Sequence<TestResult<E, Object>> parRun(Executor executor) {
    ImmutableList<Par<TestResult<E, Object>>> map = tests.map(TestCase::asyncRun);
    
    Par<Sequence<TestResult<E, Object>>> traverse = Par.traverse(map);
    
    return traverse.run(executor).get().getOrElseThrow(RuntimeException::new);
  }
}
