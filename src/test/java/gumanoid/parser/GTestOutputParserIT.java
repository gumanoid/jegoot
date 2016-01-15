package gumanoid.parser;

import org.mockito.InOrder;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

/**
 * Created by Gumanoid on 08.01.2016.
 */
@Test
public class GTestOutputParserIT {
    @Test void emptySuite() throws Exception {
        ClassifiedGTestOutputHandler listener = mock(ClassifiedGTestOutputHandler.class);
        InOrder inOrder = inOrder(listener);
        GTestOutputParser parser = new GTestOutputParser(listener);

        parser.onNextLine("[==========] Running 0 tests from 0 test cases.");
        parser.onNextLine("[==========] 0 tests from 0 test cases ran. (1 ms total)");
        parser.onNextLine("[  PASSED  ] 0 tests.");

        inOrder.verify(listener).suiteStart("[==========] Running 0 tests from 0 test cases.", 0, 0);
        inOrder.verify(listener).suiteEnd("[==========] 0 tests from 0 test cases ran. (1 ms total)", 0, 0);
        inOrder.verify(listener).passedTestsSummary("[  PASSED  ] 0 tests.", 0);
        inOrder.verifyNoMoreInteractions();
    }

    @Test void singlePassingTest() throws Exception {
        ClassifiedGTestOutputHandler listener = mock(ClassifiedGTestOutputHandler.class);
        InOrder inOrder = inOrder(listener);
        GTestOutputParser parser = new GTestOutputParser(listener);

        parser.onNextLine("[==========] Running 1 test from 1 test case.");
        parser.onNextLine("[----------] Global test environment set-up.");
        parser.onNextLine("[----------] 1 test from SomeGroup");
        parser.onNextLine("[ RUN      ] SomeGroup.TestIsTrue");
        parser.onNextLine("[       OK ] SomeGroup.TestIsTrue (0 ms)");
        parser.onNextLine("[----------] 1 test from SomeGroup (0 ms total)");
        parser.onNextLine("");
        parser.onNextLine("[----------] Global test environment tear-down");
        parser.onNextLine("[==========] 1 test from 1 test case ran. (2 ms total)");
        parser.onNextLine("[  PASSED  ] 1 test.");

        inOrder.verify(listener).suiteStart("[==========] Running 1 test from 1 test case.", 1, 1);
        inOrder.verify(listener).testOutput("[----------] Global test environment set-up.", Optional.empty(), Optional.empty());
        inOrder.verify(listener).groupStart("[----------] 1 test from SomeGroup", "SomeGroup", 1);
        inOrder.verify(listener).testStart("[ RUN      ] SomeGroup.TestIsTrue", "SomeGroup", "TestIsTrue");
        inOrder.verify(listener).testPassed("[       OK ] SomeGroup.TestIsTrue (0 ms)", "SomeGroup", "TestIsTrue");
        inOrder.verify(listener).groupEnd("[----------] 1 test from SomeGroup (0 ms total)", "SomeGroup", 1);
        inOrder.verify(listener).testOutput("", Optional.empty(), Optional.empty());
        inOrder.verify(listener).testOutput("[----------] Global test environment tear-down", Optional.empty(), Optional.empty());
        inOrder.verify(listener).suiteEnd("[==========] 1 test from 1 test case ran. (2 ms total)", 1, 1);
        inOrder.verify(listener).passedTestsSummary("[  PASSED  ] 1 test.", 1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test void singleFailingTest() throws Exception {
        ClassifiedGTestOutputHandler listener = mock(ClassifiedGTestOutputHandler.class);
        InOrder inOrder = inOrder(listener);
        GTestOutputParser parser = new GTestOutputParser(listener);

        parser.onNextLine("[==========] Running 1 test from 1 test case.");
        parser.onNextLine("[----------] Global test environment set-up.");
        parser.onNextLine("[----------] 1 test from SomeGroup");
        parser.onNextLine("[ RUN      ] SomeGroup.FailingTest");
        parser.onNextLine("..\\..\\..\\test_samples\\main.cpp(12): error: Value of: 0 == 0");
        parser.onNextLine("Actual: true");
        parser.onNextLine("Expected: false");
        parser.onNextLine("[  FAILED  ] SomeGroup.FailingTest (1 ms)");
        parser.onNextLine("[----------] 1 test from SomeGroup (2 ms total)");
        parser.onNextLine("");
        parser.onNextLine("[----------] Global test environment tear-down");
        parser.onNextLine("[==========] 1 test from 1 test case ran. (4 ms total)");
        parser.onNextLine("[  PASSED  ] 0 tests.");
        parser.onNextLine("[  FAILED  ] 1 test, listed below:");
        parser.onNextLine("[  FAILED  ] SomeGroup.FailingTest");
        parser.onNextLine("");
        parser.onNextLine(" 1 FAILED TEST");

        inOrder.verify(listener).suiteStart("[==========] Running 1 test from 1 test case.", 1, 1);
        inOrder.verify(listener).testOutput("[----------] Global test environment set-up.", Optional.empty(), Optional.empty());
        inOrder.verify(listener).groupStart("[----------] 1 test from SomeGroup", "SomeGroup", 1);
        inOrder.verify(listener).testStart("[ RUN      ] SomeGroup.FailingTest", "SomeGroup", "FailingTest");
        inOrder.verify(listener).testOutput("..\\..\\..\\test_samples\\main.cpp(12): error: Value of: 0 == 0", Optional.of("SomeGroup"), Optional.of("FailingTest"));
        inOrder.verify(listener).testOutput("Actual: true", Optional.of("SomeGroup"), Optional.of("FailingTest"));
        inOrder.verify(listener).testOutput("Expected: false", Optional.of("SomeGroup"), Optional.of("FailingTest"));
        inOrder.verify(listener).testFailed("[  FAILED  ] SomeGroup.FailingTest (1 ms)", "SomeGroup", "FailingTest");
        inOrder.verify(listener).groupEnd("[----------] 1 test from SomeGroup (2 ms total)", "SomeGroup", 1);
        inOrder.verify(listener).testOutput("", Optional.empty(), Optional.empty());
        inOrder.verify(listener).testOutput("[----------] Global test environment tear-down", Optional.empty(), Optional.empty());
        inOrder.verify(listener).suiteEnd("[==========] 1 test from 1 test case ran. (4 ms total)", 1, 1);
        inOrder.verify(listener).passedTestsSummary("[  PASSED  ] 0 tests.", 0);
        inOrder.verify(listener).failedTestsSummary("[  FAILED  ] 1 test, listed below:", 1);
        inOrder.verify(listener).failedTestSummary("[  FAILED  ] SomeGroup.FailingTest", "SomeGroup", "FailingTest");
        inOrder.verify(listener).summaryOutput("");
        inOrder.verify(listener).summaryOutput(" 1 FAILED TEST");
        inOrder.verifyNoMoreInteractions();
    }

    @Test void twoTestsInOneGroup() throws Exception {
        ClassifiedGTestOutputHandler listener = mock(ClassifiedGTestOutputHandler.class);
        InOrder inOrder = inOrder(listener);
        GTestOutputParser parser = new GTestOutputParser(listener);

        parser.onNextLine("[==========] Running 2 tests from 1 test case.");
        parser.onNextLine("[----------] Global test environment set-up.");
        parser.onNextLine("[----------] 2 tests from SomeGroup");
        parser.onNextLine("[ RUN      ] SomeGroup.TestIsTrue");
        parser.onNextLine("[       OK ] SomeGroup.TestIsTrue (0 ms)");
        parser.onNextLine("[ RUN      ] SomeGroup.FailingTest");
        parser.onNextLine("..\\..\\..\\test_samples\\main.cpp(12): error: Value of: 0 == 0");
        parser.onNextLine("  Actual: true");
        parser.onNextLine("Expected: false");
        parser.onNextLine("[  FAILED  ] SomeGroup.FailingTest (1 ms)");
        parser.onNextLine("[----------] 2 tests from SomeGroup (2 ms total)");
        parser.onNextLine("");
        parser.onNextLine("[----------] Global test environment tear-down");
        parser.onNextLine("[==========] 2 tests from 1 test case ran. (5 ms total)");
        parser.onNextLine("[  PASSED  ] 1 test.");
        parser.onNextLine("[  FAILED  ] 1 test, listed below:");
        parser.onNextLine("[  FAILED  ] SomeGroup.FailingTest");
        parser.onNextLine("");
        parser.onNextLine(" 1 FAILED TEST");

        inOrder.verify(listener).suiteStart("[==========] Running 2 tests from 1 test case.", 2, 1);
        inOrder.verify(listener).testOutput("[----------] Global test environment set-up.", Optional.empty(), Optional.empty());
        inOrder.verify(listener).groupStart("[----------] 2 tests from SomeGroup", "SomeGroup", 2);
        inOrder.verify(listener).testStart("[ RUN      ] SomeGroup.TestIsTrue", "SomeGroup", "TestIsTrue");
        inOrder.verify(listener).testPassed("[       OK ] SomeGroup.TestIsTrue (0 ms)", "SomeGroup", "TestIsTrue");
        inOrder.verify(listener).testStart("[ RUN      ] SomeGroup.FailingTest", "SomeGroup", "FailingTest");
        inOrder.verify(listener).testOutput("..\\..\\..\\test_samples\\main.cpp(12): error: Value of: 0 == 0", Optional.of("SomeGroup"), Optional.of("FailingTest"));
        inOrder.verify(listener).testOutput("  Actual: true", Optional.of("SomeGroup"), Optional.of("FailingTest"));
        inOrder.verify(listener).testOutput("Expected: false", Optional.of("SomeGroup"), Optional.of("FailingTest"));
        inOrder.verify(listener).testFailed("[  FAILED  ] SomeGroup.FailingTest (1 ms)", "SomeGroup", "FailingTest");
        inOrder.verify(listener).groupEnd("[----------] 2 tests from SomeGroup (2 ms total)", "SomeGroup", 2);
        inOrder.verify(listener).testOutput("", Optional.empty(), Optional.empty());
        inOrder.verify(listener).testOutput("[----------] Global test environment tear-down", Optional.empty(), Optional.empty());
        inOrder.verify(listener).suiteEnd("[==========] 2 tests from 1 test case ran. (5 ms total)", 2, 1);
        inOrder.verify(listener).passedTestsSummary("[  PASSED  ] 1 test.", 1);
        inOrder.verify(listener).failedTestsSummary("[  FAILED  ] 1 test, listed below:", 1);
        inOrder.verify(listener).failedTestSummary("[  FAILED  ] SomeGroup.FailingTest", "SomeGroup", "FailingTest");
        inOrder.verify(listener).summaryOutput("");
        inOrder.verify(listener).summaryOutput(" 1 FAILED TEST");
        inOrder.verifyNoMoreInteractions();
    }

    @Test void twoGroupsWithOneTestEach() throws Exception {
        ClassifiedGTestOutputHandler listener = mock(ClassifiedGTestOutputHandler.class);
        InOrder inOrder = inOrder(listener);
        GTestOutputParser parser = new GTestOutputParser(listener);

        parser.onNextLine("[==========] Running 2 tests from 2 test cases.");
        parser.onNextLine("[----------] Global test environment set-up.");
        parser.onNextLine("[----------] 1 test from SomeGroup");
        parser.onNextLine("[ RUN      ] SomeGroup.TestIsTrue");
        parser.onNextLine("[       OK ] SomeGroup.TestIsTrue (0 ms)");
        parser.onNextLine("[----------] 1 test from SomeGroup (1 ms total)");
        parser.onNextLine("");
        parser.onNextLine("[----------] 1 test from OtherGroup");
        parser.onNextLine("[ RUN      ] OtherGroup.ExpectTest");
        parser.onNextLine("..\\..\\..\\test_samples\\main.cpp(17): error: Value of: 2");
        parser.onNextLine("Expected: 1");
        parser.onNextLine("..\\..\\..\\test_samples\\main.cpp(18): error: Value of: 3");
        parser.onNextLine("Expected: 2");
        parser.onNextLine("[  FAILED  ] OtherGroup.ExpectTest (12 ms)");
        parser.onNextLine("[----------] 1 test from OtherGroup (13 ms total)");
        parser.onNextLine("");
        parser.onNextLine("[----------] Global test environment tear-down");
        parser.onNextLine("[==========] 2 tests from 2 test cases ran. (15 ms total)");
        parser.onNextLine("[  PASSED  ] 1 test.");
        parser.onNextLine("[  FAILED  ] 1 test, listed below:");
        parser.onNextLine("[  FAILED  ] OtherGroup.ExpectTest");
        parser.onNextLine("");
        parser.onNextLine(" 1 FAILED TEST");

        inOrder.verify(listener).suiteStart("[==========] Running 2 tests from 2 test cases.", 2, 2);
        inOrder.verify(listener).testOutput("[----------] Global test environment set-up.", Optional.empty(), Optional.empty());
        inOrder.verify(listener).groupStart("[----------] 1 test from SomeGroup", "SomeGroup", 1);
        inOrder.verify(listener).testStart("[ RUN      ] SomeGroup.TestIsTrue", "SomeGroup", "TestIsTrue");
        inOrder.verify(listener).testPassed("[       OK ] SomeGroup.TestIsTrue (0 ms)", "SomeGroup", "TestIsTrue");
        inOrder.verify(listener).groupEnd("[----------] 1 test from SomeGroup (1 ms total)", "SomeGroup", 1);
        inOrder.verify(listener).testOutput("", Optional.empty(), Optional.empty());
        inOrder.verify(listener).groupStart("[----------] 1 test from OtherGroup", "OtherGroup", 1);
        inOrder.verify(listener).testStart("[ RUN      ] OtherGroup.ExpectTest", "OtherGroup", "ExpectTest");
        inOrder.verify(listener).testOutput("..\\..\\..\\test_samples\\main.cpp(17): error: Value of: 2", Optional.of("OtherGroup"), Optional.of("ExpectTest"));
        inOrder.verify(listener).testOutput("Expected: 1", Optional.of("OtherGroup"), Optional.of("ExpectTest"));
        inOrder.verify(listener).testOutput("..\\..\\..\\test_samples\\main.cpp(18): error: Value of: 3", Optional.of("OtherGroup"), Optional.of("ExpectTest"));
        inOrder.verify(listener).testOutput("Expected: 2", Optional.of("OtherGroup"), Optional.of("ExpectTest"));
        inOrder.verify(listener).testFailed("[  FAILED  ] OtherGroup.ExpectTest (12 ms)", "OtherGroup", "ExpectTest");
        inOrder.verify(listener).groupEnd("[----------] 1 test from OtherGroup (13 ms total)", "OtherGroup", 1);
        inOrder.verify(listener).testOutput("", Optional.empty(), Optional.empty());
        inOrder.verify(listener).testOutput("[----------] Global test environment tear-down", Optional.empty(), Optional.empty());
        inOrder.verify(listener).suiteEnd("[==========] 2 tests from 2 test cases ran. (15 ms total)", 2, 2);
        inOrder.verify(listener).passedTestsSummary("[  PASSED  ] 1 test.", 1);
        inOrder.verify(listener).failedTestsSummary("[  FAILED  ] 1 test, listed below:", 1);
        inOrder.verify(listener).failedTestSummary("[  FAILED  ] OtherGroup.ExpectTest", "OtherGroup", "ExpectTest");
        inOrder.verify(listener).summaryOutput("");
        inOrder.verify(listener).summaryOutput(" 1 FAILED TEST");
        inOrder.verifyNoMoreInteractions();
    }

    @Test void elapsedTimeTurnedOff() throws Exception {
        ClassifiedGTestOutputHandler listener = mock(ClassifiedGTestOutputHandler.class);
        InOrder inOrder = inOrder(listener);
        GTestOutputParser parser = new GTestOutputParser(listener);

        parser.onNextLine("[==========] Running 2 tests from 2 test cases.");
        parser.onNextLine("[----------] Global test environment set-up.");
        parser.onNextLine("[----------] 1 test from SomeGroup");
        parser.onNextLine("[ RUN      ] SomeGroup.TestIsTrue");
        parser.onNextLine("[       OK ] SomeGroup.TestIsTrue");
        parser.onNextLine("[----------] 1 test from OtherGroup");
        parser.onNextLine("[ RUN      ] OtherGroup.ExpectTest");
        parser.onNextLine("..\\..\\test_samples\\main.cpp(19): error: Value of: 2");
        parser.onNextLine("Expected: 1");
        parser.onNextLine("..\\..\\test_samples\\main.cpp(20): error: Value of: 3");
        parser.onNextLine("Expected: 2");
        parser.onNextLine("[  FAILED  ] OtherGroup.ExpectTest");
        parser.onNextLine("[----------] Global test environment tear-down");
        parser.onNextLine("[==========] 2 tests from 2 test cases ran.");
        parser.onNextLine("[  PASSED  ] 1 test.");
        parser.onNextLine("[  FAILED  ] 1 test, listed below:");
        parser.onNextLine("[  FAILED  ] OtherGroup.ExpectTest");
        parser.onNextLine("");
        parser.onNextLine(" 1 FAILED TEST");

        inOrder.verify(listener).suiteStart("[==========] Running 2 tests from 2 test cases.", 2, 2);
        inOrder.verify(listener).testOutput("[----------] Global test environment set-up.", Optional.empty(), Optional.empty());
        inOrder.verify(listener).groupStart("[----------] 1 test from SomeGroup", "SomeGroup", 1);
        inOrder.verify(listener).testStart("[ RUN      ] SomeGroup.TestIsTrue", "SomeGroup", "TestIsTrue");
        inOrder.verify(listener).testPassed("[       OK ] SomeGroup.TestIsTrue", "SomeGroup", "TestIsTrue");
        inOrder.verify(listener).groupEnd(null, "SomeGroup", 1);
        inOrder.verify(listener).groupStart("[----------] 1 test from OtherGroup", "OtherGroup", 1);
        inOrder.verify(listener).testStart("[ RUN      ] OtherGroup.ExpectTest", "OtherGroup", "ExpectTest");
        inOrder.verify(listener).testOutput("..\\..\\test_samples\\main.cpp(19): error: Value of: 2", Optional.of("OtherGroup"), Optional.of("ExpectTest"));
        inOrder.verify(listener).testOutput("Expected: 1", Optional.of("OtherGroup"), Optional.of("ExpectTest"));
        inOrder.verify(listener).testOutput("..\\..\\test_samples\\main.cpp(20): error: Value of: 3", Optional.of("OtherGroup"), Optional.of("ExpectTest"));
        inOrder.verify(listener).testOutput("Expected: 2", Optional.of("OtherGroup"), Optional.of("ExpectTest"));
        inOrder.verify(listener).testFailed("[  FAILED  ] OtherGroup.ExpectTest", "OtherGroup", "ExpectTest");
        inOrder.verify(listener).groupEnd(null, "OtherGroup", 1);
        inOrder.verify(listener).testOutput("[----------] Global test environment tear-down", Optional.empty(), Optional.empty());
        inOrder.verify(listener).suiteEnd("[==========] 2 tests from 2 test cases ran.", 2, 2);
        inOrder.verify(listener).passedTestsSummary("[  PASSED  ] 1 test.", 1);
        inOrder.verify(listener).failedTestsSummary("[  FAILED  ] 1 test, listed below:", 1);
        inOrder.verify(listener).failedTestSummary("[  FAILED  ] OtherGroup.ExpectTest", "OtherGroup", "ExpectTest");
        inOrder.verify(listener).summaryOutput("");
        inOrder.verify(listener).summaryOutput(" 1 FAILED TEST");
        inOrder.verifyNoMoreInteractions();
    }
}