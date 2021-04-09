package dev.bradhandy.testing.reflection.extension;

import dev.bradhandy.testing.reflection.TestProxy;
import dev.bradhandy.testing.reflection.util.MethodUnderTestInvocationHandler;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * Implementation of {@link ParameterResolver} to create proxies wrapping an object under test in
 * order to invoke an inaccessible method directly.
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
 *   public class SomeClassTest {
 *
 *     private SomeClass someClassInstance;
 *
 *     &#64;BeforeEach
 *     void setUp() {
 *       // set up the state of the test class and the object under test.
 *     }
 *
 *     &#64;Test
 *     &#64;ExtendWith(ObjectUnderTestProxyResolver.class)
 *     void someMethodToTest_canBeCalled(
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
public class ObjectUnderTestProxyResolver implements ParameterResolver {

  @Override
  public boolean supportsParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return parameterContext.findAnnotation(TestProxy.class).isPresent();
  }

  @Override
  public Object resolveParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    Class<?> parameterType = parameterContext.getParameter().getType();
    if (!parameterType.isInterface()) {
      throw new ParameterResolutionException("@TestProxy parameter type must be an interface.");
    }

    TestProxy testProxyAnnotation =
        parameterContext
            .findAnnotation(TestProxy.class)
            .orElseThrow(() -> new ParameterResolutionException("Missing @TestProxy annotation."));
    Object testInstance = extensionContext.getRequiredTestInstance();
    Class<?> testClass = testInstance.getClass();

    try {
      Field objectUnderTestField = testClass.getDeclaredField(testProxyAnnotation.value());
      objectUnderTestField.setAccessible(true);

      Object objectUnderTest = objectUnderTestField.get(testInstance);
      return Proxy.newProxyInstance(
          Thread.currentThread().getContextClassLoader(),
          new Class<?>[] {parameterType},
          new MethodUnderTestInvocationHandler(objectUnderTest));
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new ParameterResolutionException(
          String.format(
              "Unable to resolve field %s on %s.",
              testProxyAnnotation.value(), testClass.getSimpleName()),
          e);
    }
  }
}
