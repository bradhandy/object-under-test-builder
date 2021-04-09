package dev.bradhandy.testing.reflection.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Implementation of {@link InvocationHandler} to locate and execute a method with the same name
 * and argument types within the target object type. The method's accessibility is updated to allow
 * execution by code normally unauthorized to call the method. Then the method is executed and any
 * available return value is returned to the caller.
 *
 * @author bhandy
 */
public final class MethodUnderTestInvocationHandler implements InvocationHandler {

  private final Object objectUnderTest;

  public MethodUnderTestInvocationHandler(Object objectUnderTest) {
    this.objectUnderTest = objectUnderTest;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Method targetMethod =
        objectUnderTest.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
    targetMethod.setAccessible(true);

    return targetMethod.invoke(objectUnderTest, args);
  }
}
