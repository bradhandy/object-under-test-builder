# Problem
Testing inaccessible methods is a necessary evil in certain
circumstances. Legacy code is not always written with
testing in mind. The amount of work required to test
inaccessible methods is just boilerplate. Now it doesn't have
to be.

# Solution
The Object Under Test Proxy library provides a stand-alone builder for
creating proxies providing access to private method matching the
signatures of a provided interface. The builder is used by a custom
JUnit 4 Runner, and a JUnit 5 ParameterResolver. Both component support
creating proxies on-the-fly according to the annotated parameter's type.

# Usage

## ObjectUnderTestBuilder
The `ObjectUnderTestBuilder` can use an existing object instance or one
created by a `Supplier`. The object can be retrieved directly without a
proxy, or you may specify one or more interfaces using the `confirmingTo`
method. This returns a builder capable of creating proxies conforming to
the configured interfaces.

### Examples
```java
public class SomeClass {

  // other methods

  private void methodToBeInvoked() {
    // method's logic.
  }
}

public interface MethodExposingInterface {
  void methodToBeInvoked();
}

// Return object under test without a proxy
SomeClass someInstance = ObjectUnderTestBuilder.using(new SomeClass()).build();

// Return a supplier object under test.
SomeClass someInstance = ObjectUnderTestBuilder.suppliedBy(() -> new SomeClass()).build();

// Return a proxy conforming to one or more interfaces.
MethodExposingInterface objectUnderTest = 
    ObjectUnderTestBuilder
        .using(new SomeClass())
        .conformingTo(MethodExposingInterface.class)
        .build();
```

## JUnit 4 Test Runner

### Custom Test Runner
JUnit 4's default test runner forbids arguments in @Test
annotated methods. JUnit 4 test classes will need to use the
custom runner (`ObjectUnderTestProxyRunner`) in order to create
proxies.

The custom runner allows a single annotated parameter. All
no-argument test methods will execute as they would under the
default JUnit 4 runner.

```java
@RunWith(ObjectUnderTestProxyRunner.class)
public class RunnerExampleTest {

}
```

### Initializing the Object Under Test
Some test methods may create instances of the class under test
within the method itself. In order to use the custom runner you
will need to create a property and initialize it with a `@Before`
method or when it's declared.

```java
@RunWith(ObjectUnderTestProxyRunner.class)
public class RunnerExampleTest {
  
  private SomeClass myObjectUnderTest = new SomeClass();

  @Before
  public void setUp() {
    myObject = new ObjectUnderTest(...);
  }
  
}
```

### Creating a Throw-Away Interface
Since the methods we want to test are private, we can't call them
directly on the class under test. We need to create a throw-away
interface with methods matching the signature of the private
methods we want to verify.

```java
public class SomeClass {
  // other methods
  
  private void methodToBeInvoked() {
    // method's logic.
  }
}

@RunWith(ObjectUnderTestProxyRunner.class)
public class RunnerExampleTest {
  
  // test methods and other declarations.
  
  private interface MethodExposingInterface {
    void methodToBeInvoked();
  }
}
```

### Tying Everything Together
It is important to match the name and argument types in the signature
in order, otherwise the method will not be identified correctly.

Next we can create our test method accepting an argument of our
throw-away interface.

```java
@RunWith(ObjectUnderTestProxyRunner.class)
public class RunnerExampleTest {

  private SomeClass myObjectUnderTest = new SomeClass();

  @Test // import org.junit.Test;
  public void privateMethodInvoked(@TestProxy("myObjectUnderTest") MethodExposingInterface proxy) {
    assertFalse(
        "The method should not have been invoked yet.", myObjectUnderTest.wasMethodInvoked());

    proxy.methodToBeInvoked();
    assertTrue("The method should have been invoked.", myObjectUnderTest.wasMethodInvoked());
  }

  private interface MethodExposingInterface {
    void methodToBeInvoked();
  }

}
```

Here is a larger example using JUnit 4 along with calling methods
accepting arguments and having return values.

### Full Example
```java
@RunWith(ObjectUnderTestProxyRunner.class)
public class RunnerExampleTest {

  private SomeClass myObjectUnderTest = new SomeClass();

  @Test
  public void privateMethodInvoked(@TestProxy("myObjectUnderTest") MethodExposingInterface proxy) {
    assertFalse(
        "The method should not have been invoked yet.", myObjectUnderTest.wasMethodInvoked());

    proxy.methodToBeInvoked();
    assertTrue("The method should have been invoked.", myObjectUnderTest.wasMethodInvoked());
  }

  @Test
  public void privateMethodWithReturnValueInvoked(
      @TestProxy("myObjectUnderTest") MethodExposingInterface proxy) {
    String alteredValue = proxy.customReturnValue("myValue");
    assertEquals("The value returned does not match.", "myValueAltered", alteredValue);
  }

  @Test
  public void privateMethodWithParametersInvoked(
      @TestProxy("myObjectUnderTest") MethodExposingInterface proxy) {
    assertNull("The value should be null.", myObjectUnderTest.getCustomValue());

    proxy.setCustomValue("someWeirdCustomValue");
    assertEquals(
        "The value does not match.", "someWeirdCustomValue", myObjectUnderTest.getCustomValue());
  }

  private interface MethodExposingInterface {
    void methodToBeInvoked();

    void setCustomValue(String customValue);

    String customReturnValue(String valueToAlter);
  }

  private static class SomeClass {

    private boolean methodInvoked;
    private String customValue;

    private void methodToBeInvoked() {
      methodInvoked = true;
    }

    private String customReturnValue(String valueToAlter) {
      return valueToAlter + "Altered";
    }

    public boolean wasMethodInvoked() {
      return methodInvoked;
    }

    public String getCustomValue() {
      return customValue;
    }

    private void setCustomValue(String customValue) {
      this.customValue = customValue;
    }
  }
}
```

## JUnit 5

### Custom Extension
JUnit 5 has been engineered from the beginning to allow parameters
within test methods as long as there is a `ParameterResolver` to
provide the value for the argument. The JUnit 5 extension provided
will create proxies for all annotated parameters. Although, it may
be more effective to isolate a single object under test in any
specific test method.

To use the extension, annotate the entire test class or specific
test methods with `@ExtendWith(ObjectUnderTestProxyResolver.class)`.
The resolver will be executed by JUnit 5's Jupiter engine and provide
a Proxy wrapping the object under test conforming to the throw-away
interface.

```java
public class ExtensionExampleTest {

  private SomeClass myObjectUnderTest = new SomeClass();

  @Test // org.junit.jupiter.api.Test
  @ExtendWith(ObjectUnderTestProxyResolver.class)
  void privateMethodInvoked(@TestProxy("myObjectUnderTest") MethodExposingInterface proxy) {
    assertFalse(
        "The method should not have been invoked yet.", myObjectUnderTest.wasMethodInvoked());
    proxy.methodToBeInvoked();
    assertTrue("The method should have been invoked.", myObjectUnderTest.wasMethodInvoked());
  }
}
```


### Full Example
```java
@ExtendWith(ObjectUnderTestProxyResolver.class)
public class ExtensionExampleTest {

  private SomeClass myObjectUnderTest = new SomeClass();

  @Test
  void privateMethodInvoked(@TestProxy("myObjectUnderTest") MethodExposingInterface proxy) {
    assertFalse(
        "The method should not have been invoked yet.", myObjectUnderTest.wasMethodInvoked());

    proxy.methodToBeInvoked();
    assertTrue("The method should have been invoked.", myObjectUnderTest.wasMethodInvoked());
  }

  @Test
  void privateMethodWithReturnValueInvoked(
      @TestProxy("myObjectUnderTest") MethodExposingInterface proxy) {
    String alteredValue = proxy.customReturnValue("myValue");
    assertEquals("The value returned does not match.", "myValueAltered", alteredValue);
  }

  @Test
  void privateMethodWithParametersInvoked(
      @TestProxy("myObjectUnderTest") MethodExposingInterface proxy) {
    assertNull("The value should be null.", myObjectUnderTest.getCustomValue());

    proxy.setCustomValue("someWeirdCustomValue");
    assertEquals(
        "The value does not match.", "someWeirdCustomValue", myObjectUnderTest.getCustomValue());
  }

  private interface MethodExposingInterface {
    void methodToBeInvoked();

    void setCustomValue(String customValue);

    String customReturnValue(String valueToAlter);
  }

  private static class SomeClass {

    private boolean methodInvoked;
    private String customValue;

    private void methodToBeInvoked() {
      methodInvoked = true;
    }

    private String customReturnValue(String valueToAlter) {
      return valueToAlter + "Altered";
    }

    public boolean wasMethodInvoked() {
      return methodInvoked;
    }

    public String getCustomValue() {
      return customValue;
    }

    private void setCustomValue(String customValue) {
      this.customValue = customValue;
    }
  }
}
```
