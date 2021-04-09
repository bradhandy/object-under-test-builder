package dev.bradhandy.testing.reflection.runner;

import dev.bradhandy.testing.reflection.TestProxy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class InvokeMethodWithParametersStatementTest {

  @Before
  public void setUpDependencies() throws Exception {}

  @Test
  public void evaluationWithAnArgumentCompletesSuccessfully() throws Throwable {
    TestCase testCase = new TestCase();
    Method testMethod = testCase.getClass().getDeclaredMethod("assertResult", UpdateResult.class);
    InvokeMethodWithParametersStatement statement =
        new InvokeMethodWithParametersStatement(new FrameworkMethod(testMethod), testCase);
    statement.evaluate();
  }

  @Test
  public void evaluationWithoutArgumentsCompletesSuccessfully() throws Throwable {
    TestCase testCase = new TestCase();
    Method testMethod = testCase.getClass().getDeclaredMethod("assertResultIsNull");
    InvokeMethodWithParametersStatement statement =
        new InvokeMethodWithParametersStatement(new FrameworkMethod(testMethod), testCase);
    statement.evaluate();
  }

  private interface UpdateResult {
    void updateResult(String value);
  }

  static class TestCase {

    private final ClassUnderTest classUnderTest = new ClassUnderTest();

    public void assertResult(@TestProxy("classUnderTest") UpdateResult updateResult) {
      updateResult.updateResult("newResult");
      assertEquals("newResult", classUnderTest.getResult());
    }

    public void assertResultIsNull() {
      assertNull(classUnderTest.getResult());
    }

    public void assertsDoNotMatterHere(String one, String two) {}
  }

  static class ClassUnderTest {

    private String result;

    public String getResult() {
      return result;
    }

    private void updateResult(String value) {
      result = value;
    }
  }
}
