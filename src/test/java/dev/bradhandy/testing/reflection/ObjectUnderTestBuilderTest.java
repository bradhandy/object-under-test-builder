package dev.bradhandy.testing.reflection;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectUnderTestBuilderTest {

  @Test
  void ruleCanBeCreatedWithExistingObject() {
    SomeClass someInstance = new SomeClass("test");
    ObjectUnderTestBuilder objectUnderTestBuilder = ObjectUnderTestBuilder.using(someInstance);

    assertThat(objectUnderTestBuilder.<Object>build()).isSameAs(someInstance);
  }

  @Test
  void ruleCanBeCreatedWithSupplier() {
    SomeClass someInstance = new SomeClass("test");
    ObjectUnderTestBuilder objectUnderTestBuilder =
        ObjectUnderTestBuilder.suppliedBy(() -> someInstance);

    assertThat(objectUnderTestBuilder.<Object>build()).isSameAs(someInstance);
  }

  @Test
  void ruleWithCustomSupplierCreatesNewInstanceEveryTime() {
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
  void ruleCanCreateProxyToExecutePrivateMethods() {
    SomeClass someInstance = new SomeClass("test");
    ObjectUnderTestBuilder objectUnderTestBuilder =
        ObjectUnderTestBuilder.using(someInstance).conformingTo(MethodExposingInterface.class);

    MethodExposingInterface objectUnderTest = objectUnderTestBuilder.build();
    assertThat(objectUnderTest.privateMethodToInvoke()).isEqualTo("invokedPrivateMethod");
  }

  @Test
  void ruleCanCreateProxyToExecuteStaticMethods() {
    MethodExposingInterface classUnderTestProxy =
        ObjectUnderTestBuilder.using(SomeClass.class)
            .conformingTo(MethodExposingInterface.class)
            .build();

    assertThat(classUnderTestProxy.privateStaticMethodToInvoke("someValue"))
        .isEqualTo("someValueAltered");
  }

  private interface MethodExposingInterface {
    String privateMethodToInvoke();

    String privateStaticMethodToInvoke(String value);
  }

  private static class SomeClass {

    private final String value;

    public SomeClass(String value) {
      this.value = value;
    }

    private static String privateStaticMethodToInvoke(String value) {
      return value + "Altered";
    }

    public String getValue() {
      return value;
    }

    private String privateMethodToInvoke() {
      return "invokedPrivateMethod";
    }
  }
}
