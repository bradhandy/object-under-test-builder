package dev.bradhandy.testing.reflection.extension;

import dev.bradhandy.testing.reflection.ObjectUnderTestBuilder;
import dev.bradhandy.testing.reflection.TestProxy;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Field;

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
 *
 * @author bhandy
 */
public class ObjectUnderTestProxyResolver implements ParameterResolver {

  @Override
  public boolean supportsParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {

    // if the @TestProxy annotation is present, then we support the Parameter. any issues later will
    // throw a ParameterResolutionException.
    return parameterContext.findAnnotation(TestProxy.class).isPresent();
  }

  @Override
  public Object resolveParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {

    // Proxies only work if the expected type is an interface. if not, then it's an error.
    Class<?> parameterType = parameterContext.getParameter().getType();
    if (!parameterType.isInterface()) {
      throw new ParameterResolutionException("@TestProxy parameter type must be an interface.");
    }

    // when retrieving the TextProxy annotation, we use "orElseThrow" as the alternative, because
    // it seemed more appropriate than returning null. hwoever, the implementation of "supports"
    // should remove any concern of the exception being thrown.
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
      return ObjectUnderTestBuilder.using(objectUnderTest).conformingTo(parameterType).build();
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new ParameterResolutionException(
          String.format(
              "Unable to resolve field %s on %s.",
              testProxyAnnotation.value(), testClass.getSimpleName()),
          e);
    }
  }
}
