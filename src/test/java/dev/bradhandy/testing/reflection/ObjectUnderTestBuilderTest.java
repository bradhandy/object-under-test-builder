package dev.bradhandy.testing.reflection;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

public class ObjectUnderTestBuilderTest {

  @Test
  public void ruleCanBeCreatedWithExistingObject() {
    SomeClass someInstance = new SomeClass("test");
    ObjectUnderTestBuilder objectUnderTestBuilder = ObjectUnderTestBuilder.using(someInstance);

    assertThat(objectUnderTestBuilder.<Object>build()).isSameAs(someInstance);
  }

  @Test
  public void ruleCanBeCreatedWithSupplier() {
    SomeClass someInstance = new SomeClass("test");
    ObjectUnderTestBuilder objectUnderTestBuilder =
        ObjectUnderTestBuilder.suppliedBy(() -> someInstance);

    assertThat(objectUnderTestBuilder.<Object>build()).isSameAs(someInstance);
  }

  @Test
  public void ruleWithCustomSupplierCreatesNewInstanceEveryTime() {
    final AtomicReference<String> initializationParameter = new AtomicReference<>("test");
    ObjectUnderTestBuilder objectUnderTestBuilder =
        ObjectUnderTestBuilder.suppliedBy(() -> new SomeClass(initializationParameter.get()));
    SomeClass testSomeClass = objectUnderTestBuilder.build();
    assertThat(testSomeClass.getValue()).isEqualTo("test");

    initializationParameter.set("newValue");
    SomeClass newValueClass = objectUnderTestBuilder.build();
    assertThat(newValueClass.getValue()).isEqualTo("newValue");
  }

  @Test
  public void ruleCanCreateProxyToExecutePrivateMethods() {
    SomeClass someInstance = new SomeClass("test");
    ObjectUnderTestBuilder objectUnderTestBuilder =
        ObjectUnderTestBuilder.using(someInstance).conformingTo(MethodExposingInterface.class);

    MethodExposingInterface objectUnderTest = objectUnderTestBuilder.build();
    assertThat(objectUnderTest.privateMethodToInvoke()).isEqualTo("invokedPrivateMethod");
  }

  private interface MethodExposingInterface {
    String privateMethodToInvoke();
  }

  private class SomeClass {

    private final String value;

    public SomeClass(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    private String privateMethodToInvoke() {
      return "invokedPrivateMethod";
    }
  }
}
