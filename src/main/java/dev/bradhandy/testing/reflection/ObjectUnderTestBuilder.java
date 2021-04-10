package dev.bradhandy.testing.reflection;

import dev.bradhandy.testing.reflection.util.MethodUnderTestInvocationHandler;

import java.lang.reflect.Proxy;
import java.util.function.Supplier;

public class ObjectUnderTestBuilder {

  private final Supplier<?> objectUnderTest;

  private ObjectUnderTestBuilder(Supplier<?> objectUnderTest) {
    this.objectUnderTest = objectUnderTest;
  }

  public static ObjectUnderTestBuilder using(Object staticInstance) {
    return new ObjectUnderTestBuilder(() -> staticInstance);
  }

  public static ObjectUnderTestBuilder suppliedBy(Supplier<?> objectUnderTestSupplier) {
    return new ObjectUnderTestBuilder(objectUnderTestSupplier);
  }

  public ObjectUnderTestBuilder conformingTo(Class<?>... interfaces) {
    final Class<?>[] copyOfInterfaces = new Class<?>[interfaces.length];
    System.arraycopy(interfaces, 0, copyOfInterfaces, 0, interfaces.length);

    final Supplier<?> originalSupplier = objectUnderTest;
    return new ObjectUnderTestBuilder(
        () ->
            Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                copyOfInterfaces,
                new MethodUnderTestInvocationHandler(originalSupplier.get())));
  }

  public <T> T build() {
    return (T) objectUnderTest.get();
  }
}
