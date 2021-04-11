package dev.bradhandy.testing.reflection.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.assertThat;

class MethodUnderTestInvocationHandlerTest {

  @Test
  void privateInstanceMethodInvoked() {
    SomeClass someInstance = new SomeClass();
    MethodExposingInterface objectUnderTest =
        (MethodExposingInterface)
            Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[] {MethodExposingInterface.class},
                new MethodUnderTestInvocationHandler(someInstance));

    objectUnderTest.instanceMethodWithoutArguments();
    assertThat(someInstance.wasPrivateInstanceMethodInvoked()).isTrue();
  }

  @Test
  void privateInstanceMethodWithArgumentsInvoked() {
    SomeClass someInstance = new SomeClass();
    MethodExposingInterface objectUnderTest =
        (MethodExposingInterface)
            Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[] {MethodExposingInterface.class},
                new MethodUnderTestInvocationHandler(someInstance));

    objectUnderTest.instanceMethodWithArguments("someValue");
    assertThat(someInstance.getValue()).isEqualTo("someValue");
  }

  @Test
  void privateInstanceMethodWithReturnValue() {
    SomeClass someInstance = new SomeClass();
    MethodExposingInterface objectUnderTest =
        (MethodExposingInterface)
            Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[] {MethodExposingInterface.class},
                new MethodUnderTestInvocationHandler(someInstance));

    assertThat(objectUnderTest.instanceMethodWithReturnValue()).isEqualTo("someReturnValue");
  }

  @Test
  void privateStaticMethod() {
    MethodExposingInterface objectUnderTest =
        (MethodExposingInterface)
            Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[] {MethodExposingInterface.class},
                new MethodUnderTestInvocationHandler(SomeClass.class));

    objectUnderTest.privateStaticMethod();
    assertThat(SomeClass.wasPrivateStaticMethodInvoked()).isTrue();
  }

  interface MethodExposingInterface {

    void instanceMethodWithoutArguments();

    void instanceMethodWithArguments(String value);

    String instanceMethodWithReturnValue();

    void privateStaticMethod();
  }

  static class SomeClass {

    private static boolean privateStaticMethodInvoked = false;

    private boolean privateInstanceMethodInvoked;
    private String privateInstanceMethodWithArgumentsValue;

    private static void privateStaticMethod() {
      privateStaticMethodInvoked = true;
    }

    public static boolean wasPrivateStaticMethodInvoked() {
      return privateStaticMethodInvoked;
    }

    private void instanceMethodWithoutArguments() {
      privateInstanceMethodInvoked = true;
    }

    private void instanceMethodWithArguments(String value) {
      privateInstanceMethodWithArgumentsValue = value;
    }

    private String instanceMethodWithReturnValue() {
      return "someReturnValue";
    }

    public boolean wasPrivateInstanceMethodInvoked() {
      return privateInstanceMethodInvoked;
    }

    public String getValue() {
      return privateInstanceMethodWithArgumentsValue;
    }
  }
}
