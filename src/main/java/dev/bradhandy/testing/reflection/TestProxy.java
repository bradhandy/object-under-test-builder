package dev.bradhandy.testing.reflection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a method parameter to create a proxy around the named object under test. The name refers
 * to a field within the test holding a reference to the object under test.
 *
 * @author bhandy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface TestProxy {
  String value();
}
