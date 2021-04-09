package dev.bradhandy.testing.reflection.runner;

import dev.bradhandy.testing.reflection.TestProxy;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.model.InvalidTestClassError;

import java.util.EventListener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

public class ObjectUnderTestProxyRunnerTest {

  @Test
  public void methodsWithMultipleParametersFailValidation() {
    InvalidTestClassError validationError =
        assertThrows(
            InvalidTestClassError.class,
            () -> new ObjectUnderTestProxyRunner(TestCaseWithMultipleParameters.class));

    assertThat(validationError.getCauses())
        .hasSize(1)
        .element(0)
        .hasFieldOrPropertyWithValue(
            "message",
            "Method testMethodWithMultipleParameters has too many parameters (2) when only one is allowed.");
  }

  @Test
  public void methodsWithOneParameterWithoutCorrectAnnotationFailValidation() {
    InvalidTestClassError validationError =
        assertThrows(
            InvalidTestClassError.class,
            () ->
                new ObjectUnderTestProxyRunner(TestCaseWithSingleArgumentWithoutAnnotation.class));

    assertThat(validationError.getCauses())
        .hasSize(1)
        .element(0)
        .hasFieldOrPropertyWithValue(
            "message",
            "Method testMethodWithSingleArgumentWithoutAnnotation has parameter without @TestProxy.");
  }

  @Test
  public void methodsWithNonExistentObjectUnderTestFailValidation() throws Exception {
    InvalidTestClassError validationError =
        assertThrows(
            InvalidTestClassError.class,
            () -> new ObjectUnderTestProxyRunner(TestCaseWithNonExistentTestTarget.class));

    assertThat(validationError.getCauses())
        .hasSize(1)
        .element(0)
        .hasFieldOrPropertyWithValue(
            "message",
            "Test Class TestCaseWithNonExistentTestTarget has no field with name nonExistent.");
  }

  @Test
  public void methodsWithNonInterfaceParametersFailValidation() throws Exception {
    InvalidTestClassError validationError =
        assertThrows(
            InvalidTestClassError.class,
            () -> new ObjectUnderTestProxyRunner(TestCaseWithNonInterfaceArgument.class));

    assertThat(validationError.getCauses())
        .hasSize(1)
        .element(0)
        .hasFieldOrPropertyWithValue(
            "message",
            "Method testMethodWithNonInterfaceArgument has parameter whose type is not an interface (java.lang.Object).");
  }

  private interface Excluded {

  }

  public static class TestCaseWithMultipleParameters {

    private Object objectUnderTest;

    @Test
    @Category(Excluded.class)
    public void testMethodWithMultipleParameters(
        @TestProxy("objectUnderTest") EventListener argumentOne, EventListener argumentTwo) {}
  }

  public static class TestCaseWithSingleArgumentWithoutAnnotation {

    private Object objectUnderTest;

    @Test
    @Category(Excluded.class)
    public void testMethodWithSingleArgumentWithoutAnnotation(EventListener someArgument) {}
  }

  public static class TestCaseWithNonExistentTestTarget {

    @Test
    @Category(Excluded.class)
    public void testMethodWithNonExistentObjectUnderTest(
        @TestProxy("nonExistent") EventListener eventListener) {}
  }

  public static class TestCaseWithNonInterfaceArgument {

    private Object objectUnderTest;

    @Test
    @Category(Excluded.class)
    public void testMethodWithNonInterfaceArgument(@TestProxy("objectUnderTest") Object argument) {}
  }
}
