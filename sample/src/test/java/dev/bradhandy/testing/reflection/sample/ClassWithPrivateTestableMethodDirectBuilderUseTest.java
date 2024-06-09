package dev.bradhandy.testing.reflection.sample;

import dev.bradhandy.testing.reflection.ObjectUnderTestBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClassWithPrivateTestableMethodDirectBuilderUseTest {

  private ClassWithPrivateTestableMethod objectUnderTest;

  @Before
  public void setUpObjectUnderTest() {
    objectUnderTest = new ClassWithPrivateTestableMethod();
  }

  @Test
  public void sumOfFourAndFiveIsNine() {
    ValidateSumInterface proxy = ObjectUnderTestBuilder.using(objectUnderTest)
        .conformingTo(ValidateSumInterface.class)
        .build();

    assertEquals(9, proxy.sum(4, 5), "The sum should have been nine.");
  }

  // Expect a test failure here to verify the method is actually working. Four plus six is ten, but
  // we're expecting eleven here on purpose.
  @Test
  public void sumOfFourAndSixIsTenButExpectEleven() {
    ValidateSumInterface proxy =
        ObjectUnderTestBuilder.suppliedBy(ClassWithPrivateTestableMethod::new)
            .conformingTo(ValidateSumInterface.class)
            .build();

    assertEquals(11, proxy.sum(4, 6),
        "The sum should have been ten, but we expected eleven to induce a failure.");
  }

  private interface ValidateSumInterface {

    int sum(int a, int b);
  }
}