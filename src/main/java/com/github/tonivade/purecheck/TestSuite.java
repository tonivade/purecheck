package com.github.tonivade.purecheck;

import static com.github.tonivade.purefun.Precondition.checkNonNull;

import java.util.concurrent.Executor;

import com.github.tonivade.purefun.concurrent.Par;
import com.github.tonivade.purefun.data.NonEmptyList;
import com.github.tonivade.purefun.data.Sequence;

/**
 * It defines a test suite that is composed by a non empty collection of test cases
 * 
 * It allows to run the tests serialized one by one with {@code #run()} of in parallel with {@code #parRun(Executor)}
 * 
 * @author tonivade
 *
 * @param <E> type of the error
 */
public class TestSuite<E> {

  private final NonEmptyList<TestCase<E, Object>> tests;
  
  /**
   * It will throw {@code NullPointerException} if the tests is null
   * 
   * @param tests list of tests
   */
  private TestSuite(NonEmptyList<TestCase<E, Object>> tests) {
    this.tests = checkNonNull(tests);
  }
  
  public TestSuite<E> addAll(TestSuite<E> other) {
    return new TestSuite<>(tests.appendAll(other.tests));
  }
  
  /**
   * It runs the suite one by one
   * 
   * @return the result of the suite
   */
  public Sequence<TestResult<E, Object>> run() {
    return tests.map(TestCase::unsafeRun);
  }

  /**
   * It runs the suite in parallel using the given {@code Executor}
   * 
   * @param executor
   * @return the result of the suite
   */
  public Sequence<TestResult<E, Object>> parRun(Executor executor) {
    NonEmptyList<Par<TestResult<E, Object>>> map = tests.map(TestCase::asyncRun);
    
    Par<Sequence<TestResult<E, Object>>> traverse = Par.traverse(map);
    
    return traverse.run(executor).get().getOrElseThrow(RuntimeException::new);
  }
  
  @SuppressWarnings("unchecked")
  @SafeVarargs
  public static <E> TestSuite<E> suite(TestCase<E, ?> test, TestCase<E, ?>... tests) {
    return new TestSuite<>(NonEmptyList.class.cast(NonEmptyList.of(test, tests)));
  }
}
