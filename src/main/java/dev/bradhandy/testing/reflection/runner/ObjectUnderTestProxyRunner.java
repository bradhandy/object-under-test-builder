package dev.bradhandy.testing.reflection.runner;

import dev.bradhandy.testing.reflection.TestProxy;
import org.junit.Test;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

/**
 * Extension of {@link BlockJUnit4ClassRunner} to allow for a single annotated parameter. The
 * parameter value will be a proxy generated to wrap the object under test using an interface. The
 * methods of the interface should mirror the definition of private and other methods normally not
 * accessible from compiled code so they can be called indirectly via reflection.
 *
 * <p>Example:
 *
 * <pre>
 *   public class SomeClass {
 *     // other methods
 *
 *     private void someMethodToTest() {
 *
 *     }
 *   }
 *
 *   &#64;RunWith(ObjectUnderTestProxyRunner.class)
 *   public class SomeClassTest {
 *
 *     private SomeClass someClassInstance;
 *
 *     &#64;Before
 *     public void setUp() {
 *       // set up the state of the test class and the object under test.
 *     }
 *
 *     &#64;Test
 *     public void someMethodToTest_canBeCalled(
 *         &#64;TestProxy("someClassInstance") SomeMethodToTestInterface objectUnderTest) {
 *
 *       // calls the private method on SomeClass
 *       objectUnderTest.someMethodToTest();
 *     }
 *
 *     /&#42;
 *      &#42; Create an interface to identify which inaccessible methods will be under test.
 *      &#42; The proxy uses the name of the executed method and the argument type list to
 *      &#42; find the correct method to execute on the object under test.
 *      &#42;/
 *     interface SomeMethodToTestInterface {
 *       void someMethodToTest();
 *     }
 *   }
 * </pre>
 */
public class ObjectUnderTestProxyRunner extends BlockJUnit4ClassRunner {

  public ObjectUnderTestProxyRunner(Class<?> testClass) throws InitializationError {
    super(testClass);
  }

  @Override
  protected Statement methodInvoker(FrameworkMethod method, Object test) {
    return new InvokeMethodWithParametersStatement(method, test);
  }

  @Override
  protected void validateTestMethods(List<Throwable> errors) {
    List<FrameworkMethod> testMethods = getTestClass().getAnnotatedMethods(Test.class);

    for (FrameworkMethod frameworkMethod : testMethods) {
      frameworkMethod.validatePublicVoid(false, errors);
      validateMethodHasOneArgument(frameworkMethod, errors);
      validateMethodHasOnlyInterfaceTypeArgument(frameworkMethod, errors);
      validateTestProxyAnnotation(frameworkMethod, errors);
    }
  }

  private void validateMethodHasOneArgument(
      FrameworkMethod frameworkMethod, List<Throwable> errors) {
    int parameterCount = frameworkMethod.getMethod().getParameterCount();
    if (parameterCount > 1) {
      errors.add(
          new Exception(
              String.format(
                  "Method %s has too many parameters (%d) when only one is allowed.",
                  frameworkMethod.getName(), parameterCount)));
    }
  }

  private void validateMethodHasOnlyInterfaceTypeArgument(
      FrameworkMethod frameworkMethod, List<Throwable> errors) {
    Method method = frameworkMethod.getMethod();
    if (method.getParameterCount() == 0) {
      return;
    }

    Class<?> onlyParameterType = method.getParameterTypes()[0];
    if (!onlyParameterType.isInterface()) {
      errors.add(
          new Exception(
              String.format(
                  "Method %s has parameter whose type is not an interface (%s).",
                  frameworkMethod.getName(), onlyParameterType.getName())));
    }
  }

  private void validateTestProxyAnnotation(
      FrameworkMethod frameworkMethod, List<Throwable> errors) {
    Method method = frameworkMethod.getMethod();
    if (method.getParameterCount() == 0) {
      return;
    }

    Parameter parameter = method.getParameters()[0];
    TestProxy testProxyAnnotation = parameter.getAnnotation(TestProxy.class);
    if (testProxyAnnotation == null) {
      errors.add(
          new Exception(
              String.format(
                  "Method %s has parameter without @%s.",
                  frameworkMethod.getName(), TestProxy.class.getSimpleName())));
    } else {
      Class<?> testClass = getTestClass().getJavaClass();
      try {
        testClass.getDeclaredField(testProxyAnnotation.value());
      } catch (NoSuchFieldException e) {
        errors.add(
            new Exception(
                String.format(
                    "Test Class %s has no field with name %s.",
                    testClass.getSimpleName(), testProxyAnnotation.value())));
      }
    }
  }
}
