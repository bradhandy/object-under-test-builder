package dev.bradhandy.testing.reflection.sample;

import dev.bradhandy.testing.reflection.TestProxy;
import dev.bradhandy.testing.reflection.runner.ObjectUnderTestProxyRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(ObjectUnderTestProxyRunner.class)
public class ClassWithPrivateTestableMethodJunit4Test {

  private ClassWithPrivateTestableMethod objectUnderTest;

  @Before
  public void setUpObjectUnderTest() {
    objectUnderTest = new ClassWithPrivateTestableMethod();
  }

  @Test
  public void sumOfFourAndFiveIsNine(@TestProxy("objectUnderTest") ValidateSumInterface proxy) {
    assertEquals(9, proxy.sum(4, 5), "The sum should have been nine.");
  }

  // Expect a test failure here to verify the method is actually working. Four plus six is ten, but
  // we're expecting eleven here on purpose.
  @Test
  public void sumOfFourAndSixIsTenButExpectEleven(@TestProxy("objectUnderTest") ValidateSumInterface proxy) {
    assertEquals(11, proxy.sum(4, 6),
        "The sum should have been ten, but we expected eleven to induce a failure.");
  }

  private interface ValidateSumInterface {

    int sum(int a, int b);
  }
}