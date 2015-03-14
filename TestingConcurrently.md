## Introduction ##

To reduce the execution time of your tests, it can be useful to run the test methods of a given class concurrently on multiple threads.

For capturing sporadic effects or measuring performance, you may want to run a test method a given number of times.

Combining these features, you may want to run the same test method a given number of times on a given number of concurrent threads to detect synchronization problems in your classes under test.

**Note:** This page and the `jeeunit-concurrent` library is completely independent of Java EE.

## Repeating Tests ##

Run your tests with the `ConcurrentRunner` and use the `@Repeat` annotation on the class or on a method to specify the number of times the method will run. The class value, if present, is the default value for each method. Otherwise, the default is 1.

```
@RunWith(ConcurrentRunner.class)
@Repeat(times = 2)
public class RepeatedTest {

    @Test
    public void repeatedMethod() {
        // test code
    }

    @Test
    @Repeat(times = 3)
    public void anotherRepeatedMethod() {
        // test code
    }
}
```

## Concurrent Tests ##

To run your test methods concurrently with the `ConcurrentRunner`, specify the number of threads with the `@Concurrent` annotation on the class:

```
@RunWith(ConcurrentRunner.class)
@Concurrent(threads = 5)
public class ConcurrentTest {

    // a number of test methods
}
```

Of course you can combine `@Concurrent` with `@Repeat`, e.g. to run a test method 100 times on 5 threads.

## Concurrent Parameterized Tests ##

To run a parameterized test concurrently, the `@ConcurrentParameterized` runner adds concurrency to JUnit's `@Parameterized` runner.

```
@RunWith(ConcurrentParameterized.class)
@Concurrent(threads = 4)
public class MultithreadedParameterizedTest {

    public MultithreadedParameterizedTest(int parameter) {
        this.parameter = parameter;
    }

    @Parameters
    public static Collection<Object[]> getParameters() {
        List<Object[]> parameters = new ArrayList<Object[]>(10);
        for (int i = 1; i <= 10; i++) {
            parameters.add(new Object[] { i });
        }
        return parameters;
    }

    @Test
    public void myTest() {
        // test code
    }
```

## Using the Custom Runners ##

Have a look at the [sample test code](http://code.google.com/p/jeeunit/source/browse/#hg%2Fjeeunit-concurrent%2Fsrc%2Ftest%2Fjava%2Fcom%2Fgooglecode%2Fjeeunit%2Fconcurrent%2Ftest).

For using the custom runners in your own test cases, all the code you need from jeeunit is in the jeeunit-concurrent library. For Maven builds, simply add the following dependency to your POM:

```
    <dependency>
      <groupId>com.googlecode.jeeunit</groupId>
      <artifactId>jeeunit-concurrent</artifactId>
      <version>${jeeunit.version}</version>
    </dependency>  
```