package dev.bradhandy.testing.reflection.runner;

import dev.bradhandy.testing.reflection.TestProxy;
import dev.bradhandy.testing.reflection.util.MethodUnderTestInvocationHandler;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;

/**
 * Implementation of Statement to create a Proxy around the object under test. The proxy's
 * InvocationHandler uses Reflection to identify the method to be invoked based on the name and
 * argument list of the called method.
 *
 * @author bhandy
 */
public class InvokeMethodWithParametersStatement extends Statement {

  private final FrameworkMethod frameworkMethod;
  private final Object target;

  public InvokeMethodWithParametersStatement(FrameworkMethod frameworkMethod, Object target) {
    this.frameworkMethod = frameworkMethod;
    this.target = target;
  }

  @Override
  public void evaluate() throws Throwable {
    Parameter[] frameworkMethodParameters = frameworkMethod.getMethod().getParameters();
    Object[] arguments = new Object[frameworkMethodParameters.length];
    if (frameworkMethodParameters.length == 1) {
      arguments[0] = createProxyForParameter(frameworkMethodParameters[0]);
    }

    frameworkMethod.invokeExplosively(target, arguments);
  }

  private Object createProxyForParameter(Parameter proxyParameter)
      throws NoSuchFieldException, IllegalAccessException {
    TestProxy testProxyAnnotation = proxyParameter.getAnnotation(TestProxy.class);
    Field fieldUnderTest = target.getClass().getDeclaredField(testProxyAnnotation.value());
    fieldUnderTest.setAccessible(true);

    Object objectUnderTest = fieldUnderTest.get(target);
    return Proxy.newProxyInstance(
        Thread.currentThread().getContextClassLoader(),
        new Class<?>[] {proxyParameter.getType()},
        new MethodUnderTestInvocationHandler(objectUnderTest));
  }
}
