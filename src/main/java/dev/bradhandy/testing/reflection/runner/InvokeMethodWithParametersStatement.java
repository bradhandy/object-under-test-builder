package dev.bradhandy.testing.reflection.runner;

import dev.bradhandy.testing.reflection.ObjectUnderTestBuilder;
import dev.bradhandy.testing.reflection.TestProxy;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Parameter;

/**
 * Implementation of {@link Statement} to create a Proxy around the object under test. The proxy's
 * {@link InvocationHandler} uses Reflection to identify the method to be invoked based on the name
 * and argument list of the called method.
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

    // retrieve the parameter list from the method to be executed. we'll check if there is an
    // annotated argument for we'll create a proxy. otherwise, we'll do nothing with the argument
    // list. finally, we'll invoke the method with the argument list.
    Parameter[] frameworkMethodParameters = frameworkMethod.getMethod().getParameters();
    Object[] arguments = new Object[frameworkMethodParameters.length];
    if (frameworkMethodParameters.length == 1) {
      arguments[0] = createProxyForParameter(frameworkMethodParameters[0]);
    }

    frameworkMethod.invokeExplosively(target, arguments);
  }

  private Object createProxyForParameter(Parameter proxyParameter)
      throws NoSuchFieldException, IllegalAccessException {

    // look up the field on the current test class, and make sure the field's accessibility is
    // updated so we can retrieve the value.
    TestProxy testProxyAnnotation = proxyParameter.getAnnotation(TestProxy.class);
    Field fieldUnderTest = target.getClass().getDeclaredField(testProxyAnnotation.value());
    fieldUnderTest.setAccessible(true);

    // retrieve the value of the referenced field, then create a Proxy using the interface type of
    // the expected parameter. the InvocationHandle we provide will receive the value of the field
    // as the target object for invoking the private method matching the interface definition.
    Object objectUnderTest = fieldUnderTest.get(target);
    return ObjectUnderTestBuilder.using(objectUnderTest)
        .conformingTo(proxyParameter.getType())
        .build();
  }
}
