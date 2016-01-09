package gumanoid.parser;

import org.mockito.InOrder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Created by Gumanoid on 08.01.2016.
 */
@Test
public class GTestOutputParserIT {
    //todo check case when tests are shuffled

    GTestOutputParser.EventListener listener = mock(GTestOutputParser.EventListener.class);
    InOrder inOrder = inOrder(listener);
    GTestOutputParser parser;

    @BeforeMethod
    void resetListener() {
        reset(listener);
        parser = new GTestOutputParser(listener);
    }

    @Test void emptySuite() throws Exception {
        parser.onNextLine("[==========] Running 0 tests from 0 test cases.");
        parser.onNextLine("[==========] 0 tests from 0 test cases ran. (1 ms total)");
        parser.onNextLine("[  PASSED  ] 0 tests.");

        inOrder.verify(listener).suiteStart(0, 0);
        inOrder.verify(listener).suiteEnd(0, 0);
        inOrder.verify(listener).passedTestsSummary(0);
        verifyNoMoreInteractions(listener);
    }

    @Test void singlePassingTest() throws Exception {
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

        inOrder.verify(listener).suiteStart(1, 1);
        inOrder.verify(listener).groupStart("SomeGroup", 1);
        inOrder.verify(listener).testStart("SomeGroup", "TestIsTrue");
        inOrder.verify(listener).testPassed("SomeGroup", "TestIsTrue");
        inOrder.verify(listener).groupEnd("SomeGroup", 1);
        inOrder.verify(listener).testOutput(Optional.empty(), Optional.empty(), "");
        inOrder.verify(listener).suiteEnd(1, 1);
        inOrder.verify(listener).passedTestsSummary(1);
        verifyNoMoreInteractions(listener);
    }

    @Test void singleFailingTest() throws Exception {
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

        verify(listener).suiteStart(1, 1);
        verify(listener).groupStart("SomeGroup", 1);
        verify(listener).testStart("SomeGroup", "FailingTest");
        verify(listener).testOutput(Optional.of("SomeGroup"), Optional.of("FailingTest"), "..\\..\\..\\test_samples\\main.cpp(12): error: Value of: 0 == 0");
        verify(listener).testOutput(Optional.of("SomeGroup"), Optional.of("FailingTest"), "Actual: true");
        verify(listener).testOutput(Optional.of("SomeGroup"), Optional.of("FailingTest"), "Expected: false");
        verify(listener).testFailed("SomeGroup", "FailingTest");
        verify(listener).groupEnd("SomeGroup", 1);
        verify(listener).testOutput(Optional.empty(), Optional.empty(), "");
        verify(listener).suiteEnd(1, 1);
        verify(listener).passedTestsSummary(0);
        verify(listener).failedTestsSummary(1);
        verify(listener).failedTestSummary("SomeGroup", "FailingTest");
        verify(listener).summaryOutput("");
        verify(listener).summaryOutput(" 1 FAILED TEST");
        verifyNoMoreInteractions(listener);
    }

    @Test void twoTestsInOneGroup() throws Exception {
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

        inOrder.verify(listener).suiteStart(2, 1);
        inOrder.verify(listener).groupStart("SomeGroup", 2);
        inOrder.verify(listener).testStart("SomeGroup", "TestIsTrue");
        inOrder.verify(listener).testPassed("SomeGroup", "TestIsTrue");
        inOrder.verify(listener).testStart("SomeGroup", "FailingTest");
        inOrder.verify(listener).testOutput(Optional.of("SomeGroup"), Optional.of("FailingTest"), "..\\..\\..\\test_samples\\main.cpp(12): error: Value of: 0 == 0");
        inOrder.verify(listener).testOutput(Optional.of("SomeGroup"), Optional.of("FailingTest"), "  Actual: true");
        inOrder.verify(listener).testOutput(Optional.of("SomeGroup"), Optional.of("FailingTest"), "Expected: false");
        inOrder.verify(listener).testFailed("SomeGroup", "FailingTest");
        inOrder.verify(listener).groupEnd("SomeGroup", 2);
        inOrder.verify(listener).testOutput(Optional.empty(), Optional.empty(), "");
        inOrder.verify(listener).suiteEnd(2, 1);
        inOrder.verify(listener).passedTestsSummary(1);
        inOrder.verify(listener).failedTestsSummary(1);
        inOrder.verify(listener).failedTestSummary("SomeGroup", "FailingTest");
        inOrder.verify(listener).summaryOutput("");
        inOrder.verify(listener).summaryOutput(" 1 FAILED TEST");
        verifyNoMoreInteractions(listener);
    }

    @Test void twoGroupsWithOneTestEach() throws Exception {
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

        inOrder.verify(listener).suiteStart(2, 2);
        inOrder.verify(listener).groupStart("SomeGroup", 1);
        inOrder.verify(listener).testStart("SomeGroup", "TestIsTrue");
        inOrder.verify(listener).testPassed("SomeGroup", "TestIsTrue");
        inOrder.verify(listener).groupEnd("SomeGroup", 1);
        inOrder.verify(listener).testOutput(Optional.empty(), Optional.empty(), "");
        inOrder.verify(listener).groupStart("OtherGroup", 1);
        inOrder.verify(listener).testStart("OtherGroup", "ExpectTest");
        inOrder.verify(listener).testOutput(Optional.of("OtherGroup"), Optional.of("ExpectTest"), "..\\..\\..\\test_samples\\main.cpp(17): error: Value of: 2");
        inOrder.verify(listener).testOutput(Optional.of("OtherGroup"), Optional.of("ExpectTest"), "Expected: 1");
        inOrder.verify(listener).testOutput(Optional.of("OtherGroup"), Optional.of("ExpectTest"), "..\\..\\..\\test_samples\\main.cpp(18): error: Value of: 3");
        inOrder.verify(listener).testOutput(Optional.of("OtherGroup"), Optional.of("ExpectTest"), "Expected: 2");
        inOrder.verify(listener).testFailed("OtherGroup", "ExpectTest");
        inOrder.verify(listener).groupEnd("OtherGroup", 1);
        inOrder.verify(listener).testOutput(Optional.empty(), Optional.empty(), "");
        inOrder.verify(listener).suiteEnd(2, 2);
        inOrder.verify(listener).passedTestsSummary(1);
        inOrder.verify(listener).failedTestsSummary(1);
        inOrder.verify(listener).failedTestSummary("OtherGroup", "ExpectTest");
        inOrder.verify(listener).summaryOutput("");
        inOrder.verify(listener).summaryOutput(" 1 FAILED TEST");
        verifyNoMoreInteractions(listener);
    }

    @Test void elapsedTimeTurnedOff() throws Exception {
        parser.onNextLine("[==========] Running 2 tests from 2 test cases.");
        parser.onNextLine("[----------] Global test environment set-up.");
        parser.onNextLine("[----------] 1 test from SomeGroup");
        parser.onNextLine("[ RUN      ] SomeGroup.TestIsTrue");
        parser.onNextLine("[       OK ] SomeGroup.TestIsTrue");
        parser.onNextLine("[----------] 1 test from OtherGroup");
        parser.onNextLine("[ RUN      ] OtherGroup.ExpectTest");
        parser.onNextLine("..\\..\\..\\test_samples\\main.cpp(19): error: Value of: 2");
        parser.onNextLine("Expected: 1");
        parser.onNextLine("..\\..\\..\\test_samples\\main.cpp(20): error: Value of: 3");
        parser.onNextLine("Expected: 2");
        parser.onNextLine("[  FAILED  ] OtherGroup.ExpectTest");
        parser.onNextLine("[----------] Global test environment tear-down");
        parser.onNextLine("[==========] 2 tests from 2 test cases ran.");
        parser.onNextLine("[  PASSED  ] 1 test.");
        parser.onNextLine("[  FAILED  ] 1 test, listed below:");
        parser.onNextLine("[  FAILED  ] OtherGroup.ExpectTest");
        parser.onNextLine("");
        parser.onNextLine(" 1 FAILED TEST");

        inOrder.verify(listener).suiteStart(2, 2);
        inOrder.verify(listener).groupStart("SomeGroup", 1);
        inOrder.verify(listener).testStart("SomeGroup", "TestIsTrue");
        inOrder.verify(listener).testPassed("SomeGroup", "TestIsTrue");
        inOrder.verify(listener).groupEnd("SomeGroup", 1);
        inOrder.verify(listener).testOutput(Optional.empty(), Optional.empty(), "");
        inOrder.verify(listener).groupStart("OtherGroup", 1);
        inOrder.verify(listener).testStart("OtherGroup", "ExpectTest");
        inOrder.verify(listener).testOutput(Optional.of("OtherGroup"), Optional.of("ExpectTest"), "..\\..\\..\\test_samples\\main.cpp(17): error: Value of: 2");
        inOrder.verify(listener).testOutput(Optional.of("OtherGroup"), Optional.of("ExpectTest"), "Expected: 1");
        inOrder.verify(listener).testOutput(Optional.of("OtherGroup"), Optional.of("ExpectTest"), "..\\..\\..\\test_samples\\main.cpp(18): error: Value of: 3");
        inOrder.verify(listener).testOutput(Optional.of("OtherGroup"), Optional.of("ExpectTest"), "Expected: 2");
        inOrder.verify(listener).testFailed("OtherGroup", "ExpectTest");
        inOrder.verify(listener).groupEnd("OtherGroup", 1);
        inOrder.verify(listener).testOutput(Optional.empty(), Optional.empty(), "");
        inOrder.verify(listener).suiteEnd(2, 2);
        inOrder.verify(listener).passedTestsSummary(1);
        inOrder.verify(listener).failedTestsSummary(1);
        inOrder.verify(listener).failedTestSummary("OtherGroup", "ExpectTest");
        inOrder.verify(listener).summaryOutput("");
        inOrder.verify(listener).summaryOutput(" 1 FAILED TEST");
        verifyNoMoreInteractions(listener);
    }
}