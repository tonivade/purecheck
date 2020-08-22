package com.github.tonivade.purecheck;

import static com.github.tonivade.purefun.Precondition.checkNonNull;
import static com.github.tonivade.purefun.concurrent.Par.traverse;

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

  private final NonEmptyList<TestCase<E, ?>> tests;
  
  /**
   * It will throw {@code NullPointerException} if the tests is null
   * 
   * @param tests list of tests
   */
  private TestSuite(NonEmptyList<TestCase<E, ?>> tests) {
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
  public TestReport<E> run() {
    return new TestReport<>(tests.map(TestCase::unsafeRun));
  }

  /**
   * It runs the suite in parallel using the given {@code Executor}
   * 
   * @param executor
   * @return the result of the suite
   */
  public TestReport<E> parRun(Executor executor) {
    /*
     * I think that a will never undestand java generics:
     * 
     * purecheck/src/main/java/com/github/tonivade/purecheck/TestSuite.java:53: error: incompatible types: inference variable R has incompatible bounds
     * Sequence<Par<TestResult<E, ?>>> map = tests.map(TestCase::asyncRun);
     *                                             ^
     *   equality constraints: Par<TestResult<E#2,?>>
     *   lower bounds: Par<TestResult<E#2,CAP#1>>
     * where R,E#1,E#2 are type-variables:
     *   R extends Object declared in method <R>map(Function1<E#1,R>)
     *   E#1 extends Object declared in class NonEmptyList
     *   E#2 extends Object declared in class TestSuite
     * where CAP#1 is a fresh type-variable:
     *   CAP#1 extends Object from capture of ?
     * 1 error
     * 
     * Using Class.cast() as workaround
     */
    Sequence<Par<TestResult<E, ?>>> map = Sequence.class.cast(tests.map(TestCase::asyncRun));
    
    return new TestReport<>(traverse(map).run(executor).get().getOrElseThrow(RuntimeException::new));
  }
  
  @SuppressWarnings("unchecked")
  @SafeVarargs
  public static <E> TestSuite<E> suite(TestCase<E, ?> test, TestCase<E, ?>... tests) {
    return new TestSuite<>(NonEmptyList.class.cast(NonEmptyList.of(test, tests)));
  }
}
