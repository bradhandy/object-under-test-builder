package dev.bradhandy.testing.reflection.sample;

import dev.bradhandy.testing.reflection.TestProxy;
import dev.bradhandy.testing.reflection.extension.ObjectUnderTestProxyResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

class ClassWithPrivateTestableMethodJunit5Test {

  private ClassWithPrivateTestableMethod objectUnderTest;

  @BeforeEach
  void setUpObjectUnderTest() {
    objectUnderTest = new ClassWithPrivateTestableMethod();
  }

  @Test
  @ExtendWith(ObjectUnderTestProxyResolver.class)
  void sumOfFourAndFiveIsNine(@TestProxy("objectUnderTest") ValidateSumInterface proxy) {
    assertEquals(9, proxy.sum(4, 5), "The sum should have been nine.");
  }

  // Expect a test failure here to verify the method is actually working. Four plus six is ten, but
  // we're expecting eleven here on purpose.
  @Test
  @ExtendWith(ObjectUnderTestProxyResolver.class)
  void sumOfFourAndSixIsTenButExpectEleven(@TestProxy("objectUnderTest") ValidateSumInterface proxy) {
    assertEquals(11, proxy.sum(4, 6),
        "The sum should have been ten, but we expected eleven to induce a failure.");
  }

  private interface ValidateSumInterface {

    int sum(int a, int b);
  }
}