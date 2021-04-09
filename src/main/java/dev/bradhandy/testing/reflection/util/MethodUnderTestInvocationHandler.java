package dev.bradhandy.testing.reflection.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

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
