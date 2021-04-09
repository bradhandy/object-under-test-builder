package dev.bradhandy.testing.reflection.runner;

import dev.bradhandy.testing.reflection.TestProxy;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
