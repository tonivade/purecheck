# purecheck

The idea of this project is try to implement unit testing thinking in tests like pure values, 
instead of statements, no annotations, no runtime bytecode manipulation.

Pure values have some improvements, for example, it's pretty easy to retry in case of some
flaky tests, or repeat the test, or measure the time the computation it takes.

So, a test case can be defined as a function that eventually returns a result, like this:

```scala
  type TestCase[E, T] = IO[TestResult[E, T]]
```

E is the type of the error generated by the test, and T the value generated by the computation
under test.

But in Java there are no type aliases, though we can create a class `TestCase` and wrap the value:

```java
  public class TestCase<E, T> {
    
    private final IO<TestResult<E, T>> test;
    
    //...
  }
```

`TestResult<E, T>` can have 4 different values:

 - `Success` when everything is ok, 
 - `Failure` when the validations fail, 
 - `Error` when the execution of the computation throws an error, 
 - `Disabled` when the test is disabled.
 
This a simple example:
 
 ```java
  TestSuite<String> suite = suite("NonEmptyString",

      it.should("not accept null")
          .<String>givenNull()
          .when(NonEmptyString::of)
          .thenThrows(instanceOf(IllegalArgumentException.class)),

      it.should("not accept empty string")
          .given("")
          .when(NonEmptyString::of)
          .thenThrows(instanceOf(IllegalArgumentException.class)),

      it.should("contains a non empty string")
          .given("hola mundo")
          .when(NonEmptyString::of)
          .then(equalsTo("hola mundo").compose(NonEmptyString::get)),

      it.should("map inner value")
          .given(NonEmptyString.of("hola mundo"))
          .when(hello -> hello.map(String::toUpperCase))
          .then(equalsTo("HOLA MUNDO").compose(NonEmptyString::get)),

      it.should("transform inner value")
          .given(NonEmptyString.of("hola mundo"))
          .when(hello -> hello.transform(String::toUpperCase))
          .then(equalsTo("HOLA MUNDO")),

      it.should("be equals to other string `hola mundo`")
          .given(NonEmptyString.of("hola mundo"))
          .noop()
          .then(equalsTo(NonEmptyString.of("hola mundo"))),

      it.should("not be equals to other string different to `hola mundo`")
          .given(NonEmptyString.of("hola mundo"))
          .noop()
          .then(notEqualsTo(NonEmptyString.of("HOLA MUNDO")))
  );
 ```
 
 Then you can run the suite and generate a test report:
 
 ```java
  TestReport<String> report = suite.run();
 ```
 
 And the report generated looks like this:
 
 ```
  NonEmptyString {
    - it should 'not accept null' SUCCESS: 'java.lang.IllegalArgumentException: require non null'
    - it should 'not accept empty string' SUCCESS: 'java.lang.IllegalArgumentException: require non empty string'
    - it should 'contains a non empty string' SUCCESS: 'NonEmptyString(hola mundo)'
    - it should 'map inner value' SUCCESS: 'NonEmptyString(HOLA MUNDO)'
    - it should 'transform inner value' SUCCESS: 'HOLA MUNDO'
    - it should 'be equals to other string `hola mundo`' SUCCESS: 'NonEmptyString(hola mundo)'
    - it should 'not be equals to other string different to `hola mundo`' SUCCESS: 'NonEmptyString(hola mundo)'
  }
 ```