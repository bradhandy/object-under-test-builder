package dev.bradhandy.testing.reflection.extension;

import dev.bradhandy.testing.reflection.TestProxy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ObjectUnderTestProxyResolverTest {

  private ObjectUnderTestProxyResolver objectUnderTestProxyResolver =
      new ObjectUnderTestProxyResolver();

  @Test
  void parameterWithoutAnnotationIsNotSupported(@Mock ParameterContext parameterContext)
      throws NoSuchMethodException {
    TestClassWithParameterMissingAnnotation testInstance =
        new TestClassWithParameterMissingAnnotation();
    setUpMockParameterContextForTestAndMethod(
        parameterContext,
        testInstance,
        "parameterMissingAnnotation",
        SomeTestMethodInterface.class);

    assertFalse(
        objectUnderTestProxyResolver.supportsParameter(parameterContext, null),
        "A parameter missing @TestProxy should not be supported.");
  }

  @Test
  void parameterWithAnnotationIsSupported(@Mock ParameterContext parameterContext)
      throws NoSuchMethodException {
    TestClassWithUnresolvedObjectUnderTest testInstance =
        new TestClassWithUnresolvedObjectUnderTest();
    setUpMockParameterContextForTestAndMethod(
        parameterContext,
        testInstance,
        "failWithUnresolvedObjectUnderTest",
        SomeTestMethodInterface.class);

    assertTrue(
        objectUnderTestProxyResolver.supportsParameter(parameterContext, null),
        "A parameter with @TestProxy should be supported.");
  }

  @Test
  void unresolvedObjectUnderTestThrowsException(
      @Mock ParameterContext parameterContext, @Mock ExtensionContext extensionContext)
      throws NoSuchMethodException {
    TestClassWithUnresolvedObjectUnderTest testInstance =
        new TestClassWithUnresolvedObjectUnderTest();
    setUpMockParameterContextForTestAndMethod(
        parameterContext,
        testInstance,
        "failWithUnresolvedObjectUnderTest",
        SomeTestMethodInterface.class);
    setUpMockExecutionContextForTestClassAndMethod(
        extensionContext,
        testInstance,
        "failWithUnresolvedObjectUnderTest",
        SomeTestMethodInterface.class);

    assertThrows(
        ParameterResolutionException.class,
        () -> objectUnderTestProxyResolver.resolveParameter(parameterContext, extensionContext));
  }

  @Test
  void nonInterfaceTypeForProxyParameterThrowsException(
      @Mock ParameterContext parameterContext, @Mock ExtensionContext extensionContext)
      throws NoSuchMethodException {
    TestClassWithNonInterfaceProxyTypeTest testInstance =
        new TestClassWithNonInterfaceProxyTypeTest();
    setUpMockParameterContextForTestAndMethod(
        parameterContext, testInstance, "failWithNonInterfaceProxyType", Object.class);

    assertThrows(
        ParameterResolutionException.class,
        () -> objectUnderTestProxyResolver.resolveParameter(parameterContext, extensionContext));
  }

  @Test
  void parameterWithoutAnnotationThrowsException(
      @Mock ParameterContext parameterContext, @Mock ExtensionContext extensionContext)
      throws NoSuchMethodException {
    TestClassWithParameterMissingAnnotation testInstance =
        new TestClassWithParameterMissingAnnotation();
    setUpMockParameterContextForTestAndMethod(
        parameterContext,
        testInstance,
        "parameterMissingAnnotation",
        SomeTestMethodInterface.class);
    setUpMockExecutionContextForTestClassAndMethod(
        extensionContext,
        testInstance,
        "parameterMissingAnnotation",
        SomeTestMethodInterface.class);

    assertThrows(
        ParameterResolutionException.class,
        () -> objectUnderTestProxyResolver.resolveParameter(parameterContext, extensionContext));
  }

  @Test
  void validProxyCreated(
      @Mock ParameterContext parameterContext, @Mock ExtensionContext extensionContext)
      throws NoSuchMethodException {
    TestClassWithValidSetup testInstance = new TestClassWithValidSetup();
    setUpMockParameterContextForTestAndMethod(
        parameterContext,
        testInstance,
        "parameterMissingAnnotation",
        SomeTestMethodInterface.class);
    setUpMockExecutionContextForTestClassAndMethod(
        extensionContext,
        testInstance,
        "parameterMissingAnnotation",
        SomeTestMethodInterface.class);

    SomeTestMethodInterface someTestMethodInterface =
        (SomeTestMethodInterface)
            objectUnderTestProxyResolver.resolveParameter(parameterContext, extensionContext);
    testInstance.parameterMissingAnnotation(someTestMethodInterface);

    assertTrue(
        testInstance.getObjectUnderTest().wasMethodInvoked(),
        "The method should have been invoked.");
  }

  private void setUpMockParameterContextForTestAndMethod(
      ParameterContext parameterContext,
      Object testInstance,
      String methodName,
      Class<?>... argumentTypes)
      throws NoSuchMethodException {
    Class<?> testClass = testInstance.getClass();
    Method testMethod = testClass.getDeclaredMethod(methodName, argumentTypes);

    for (Parameter parameter : testMethod.getParameters()) {
      doReturn(parameter).when(parameterContext).getParameter();
      doAnswer(
              mockInvocation -> {
                Class<?> annotationType = mockInvocation.getArgument(0, Class.class);
                if (Annotation.class.isAssignableFrom(annotationType)) {
                  return parameter.getAnnotation((Class<? extends Annotation>) annotationType)
                      != null;
                }
                return false;
              })
          .when(parameterContext)
          .isAnnotated(any());

      doAnswer(
              mockInvocation -> {
                Class<?> annotationType = mockInvocation.getArgument(0, Class.class);
                if (Annotation.class.isAssignableFrom(annotationType)) {
                  return Optional.ofNullable(
                      parameter.getAnnotation((Class<? extends Annotation>) annotationType));
                }
                return Optional.empty();
              })
          .when(parameterContext)
          .findAnnotation(any());
    }
  }

  private void setUpMockExecutionContextForTestClassAndMethod(
      ExtensionContext extensionContext,
      Object testInstance,
      String methodName,
      Class<?>... argumentTypes)
      throws NoSuchMethodException {
    Class<?> testClass = testInstance.getClass();
    Method testMethod = testClass.getDeclaredMethod(methodName, argumentTypes);

    doReturn(testInstance).when(extensionContext).getRequiredTestInstance();
    doReturn(testMethod).when(extensionContext).getRequiredTestMethod();
  }

  private interface SomeTestMethodInterface {
    void inaccessibleMethodToInvoke();
  }

  private static class SomeClass {

    private boolean methodInvoked;

    private void inaccessibleMethodToInvoke() {
      methodInvoked = true;
    }

    public boolean wasMethodInvoked() {
      return methodInvoked;
    }
  }

  private static class TestClassWithUnresolvedObjectUnderTest {

    private SomeClass anObjectBySomeOtherNameIsUnresolved = new SomeClass();

    @Test
    void failWithUnresolvedObjectUnderTest(
        @TestProxy("objectUnderTest") SomeTestMethodInterface objectUnderTest) {}
  }

  private static class TestClassWithNonInterfaceProxyTypeTest {

    private SomeClass objectUnderTest = new SomeClass();

    @Test
    void failWithNonInterfaceProxyType(@TestProxy("objectUnderTest") Object objectUnderTest) {}
  }

  private static class TestClassWithParameterMissingAnnotation {

    private SomeClass objectUnderTest = new SomeClass();

    @Test
    void parameterMissingAnnotation(SomeTestMethodInterface objectUnderTest) {}
  }

  private static class TestClassWithValidSetup {

    private SomeClass objectUnderTest = new SomeClass();

    @Test
    void parameterMissingAnnotation(
        @TestProxy("objectUnderTest") SomeTestMethodInterface objectUnderTest) {
      objectUnderTest.inaccessibleMethodToInvoke();
    }

    public SomeClass getObjectUnderTest() {
      return objectUnderTest;
    }
  }
}
