package dev.bradhandy.testing.reflection.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Implementation of {@link InvocationHandler} to locate and execute a method with the same name and
 * argument types within the target object type. The method's accessibility is updated to allow
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

  /**
   * Called when an interface method for a configured proxy is called.
   *
   * <p>Look up the method identified by {@code method.getName()} and whose arguments have the same
   * types as returned by {@code method.getParameterTypes()}. The method must be declared by the
   * target class.
   *
   * <p>In the case of static method lookups, the target will be the Class object of the declaring
   * type.
   *
   * @param proxy The object which was the target of the method call within the code.
   * @param method The method called on the proxy.
   * @param args The arguments passed to the called method.
   * @return The return value, if any, of the original method.
   * @throws Throwable if an error occurs while calling the original method.
   */
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      Class<?> targetClass =
          (objectUnderTest instanceof Class)
              ? (Class<?>) objectUnderTest
              : objectUnderTest.getClass();
      Method targetMethod =
          targetClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
      targetMethod.setAccessible(true);

      return targetMethod.invoke(objectUnderTest, args);
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }
}
