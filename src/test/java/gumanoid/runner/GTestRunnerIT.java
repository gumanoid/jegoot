package gumanoid.runner;

import com.google.common.collect.ImmutableList;
import gumanoid.parser.ClassifiedGTestOutputHandler;
import org.mockito.InOrder;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

/**
 * Created by Gumanoid on 10.01.2016.
 */
@Test
public class GTestRunnerIT {
    //todo check case when tests are shuffled
    //todo tests are Windows-only, try to generalize for all platforms
    //fixme tests are failing occasionally

    File testSamplesDir = new File(Paths.get("").toAbsolutePath().toFile(), "test_samples");

    @Test void emptySuite() throws Exception {
        ClassifiedGTestOutputHandler listener = mock(ClassifiedGTestOutputHandler.class);
        InOrder inOrder = inOrder(listener);

        StubRunner runner = new StubRunner("empty_suite.exe", listener);
        runner.execute();
        assertEquals(runner.get().failedTests, ImmutableList.of());

        inOrder.verify(listener).suiteStart("[==========] Running 0 tests from 0 test cases.", 0, 0);
        inOrder.verify(listener).suiteEnd(startsWith("[==========] 0 tests from 0 test cases ran."), eq(0), eq(0));
        inOrder.verify(listener).passedTestsSummary("[  PASSED  ] 0 tests.", 0);
        verifyNoMoreInteractions(listener);
    }

    @Test void singlePassingTest() throws Exception {
        ClassifiedGTestOutputHandler listener = mock(ClassifiedGTestOutputHandler.class);
        InOrder inOrder = inOrder(listener);

        StubRunner runner = new StubRunner("single_passing.exe", listener);
        runner.execute();
        assertEquals(runner.get().failedTests, ImmutableList.of());

        inOrder.verify(listener).suiteStart("[==========] Running 1 test from 1 test case.", 1, 1);
        inOrder.verify(listener).testOutput("[----------] Global test environment set-up.", Optional.empty(), Optional.empty());
        inOrder.verify(listener).groupStart("[----------] 1 test from SomeGroup", "SomeGroup", 1);
        inOrder.verify(listener).testStart("[ RUN      ] SomeGroup.TestIsTrue", "SomeGroup", "TestIsTrue");
        inOrder.verify(listener).testPassed(startsWith("[       OK ] SomeGroup.TestIsTrue"), eq("SomeGroup"), eq("TestIsTrue"));
        inOrder.verify(listener).groupEnd(startsWith("[----------] 1 test from SomeGroup"), eq("SomeGroup"), eq(1));
        inOrder.verify(listener).testOutput("", Optional.empty(), Optional.empty());
        inOrder.verify(listener).testOutput("[----------] Global test environment tear-down", Optional.empty(), Optional.empty());
        inOrder.verify(listener).suiteEnd(startsWith("[==========] 1 test from 1 test case ran."), eq(1), eq(1));
        inOrder.verify(listener).passedTestsSummary("[  PASSED  ] 1 test.", 1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test void singleFailingTest() throws Exception {
        ClassifiedGTestOutputHandler listener = mock(ClassifiedGTestOutputHandler.class);
        InOrder inOrder = inOrder(listener);

        StubRunner runner = new StubRunner("single_failing.exe", listener);
        runner.execute();
        assertEquals(runner.get().failedTests, ImmutableList.of("SomeGroup.FailingTest"));

        inOrder.verify(listener).suiteStart("[==========] Running 1 test from 1 test case.", 1, 1);
        inOrder.verify(listener).testOutput("[----------] Global test environment set-up.", Optional.empty(), Optional.empty());
        inOrder.verify(listener).groupStart("[----------] 1 test from SomeGroup", "SomeGroup", 1);
        inOrder.verify(listener).testStart("[ RUN      ] SomeGroup.FailingTest", "SomeGroup", "FailingTest");
        inOrder.verify(listener).testOutput("..\\..\\test_samples\\main.cpp(14): error: Value of: 0 == 0", Optional.of("SomeGroup"), Optional.of("FailingTest"));
        inOrder.verify(listener).testOutput("  Actual: true", Optional.of("SomeGroup"), Optional.of("FailingTest"));
        inOrder.verify(listener).testOutput("Expected: false", Optional.of("SomeGroup"), Optional.of("FailingTest"));
        inOrder.verify(listener).testFailed(startsWith("[  FAILED  ] SomeGroup.FailingTest"), eq("SomeGroup"), eq("FailingTest"));
        inOrder.verify(listener).groupEnd(startsWith("[----------] 1 test from SomeGroup"), eq("SomeGroup"), eq(1));
        inOrder.verify(listener).testOutput("", Optional.empty(), Optional.empty());
        inOrder.verify(listener).testOutput("[----------] Global test environment tear-down", Optional.empty(), Optional.empty());
        inOrder.verify(listener).suiteEnd(startsWith("[==========] 1 test from 1 test case ran."), eq(1), eq(1));
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

        StubRunner runner = new StubRunner("two_tests_in_one_group.exe", listener);
        runner.execute();
        assertEquals(runner.get().failedTests, ImmutableList.of("SomeGroup.FailingTest"));

        inOrder.verify(listener).suiteStart("[==========] Running 2 tests from 1 test case.", 2, 1);
        inOrder.verify(listener).testOutput("[----------] Global test environment set-up.", Optional.empty(), Optional.empty());
        inOrder.verify(listener).groupStart("[----------] 2 tests from SomeGroup", "SomeGroup", 2);
        inOrder.verify(listener).testStart("[ RUN      ] SomeGroup.TestIsTrue", "SomeGroup", "TestIsTrue");
        inOrder.verify(listener).testPassed(startsWith("[       OK ] SomeGroup.TestIsTrue"), eq("SomeGroup"), eq("TestIsTrue"));
        inOrder.verify(listener).testStart("[ RUN      ] SomeGroup.FailingTest", "SomeGroup", "FailingTest");
        inOrder.verify(listener).testOutput("..\\..\\test_samples\\main.cpp(14): error: Value of: 0 == 0", Optional.of("SomeGroup"), Optional.of("FailingTest"));
        inOrder.verify(listener).testOutput("  Actual: true", Optional.of("SomeGroup"), Optional.of("FailingTest"));
        inOrder.verify(listener).testOutput("Expected: false", Optional.of("SomeGroup"), Optional.of("FailingTest"));
        inOrder.verify(listener).testFailed(startsWith("[  FAILED  ] SomeGroup.FailingTest"), eq("SomeGroup"), eq("FailingTest"));
        inOrder.verify(listener).groupEnd(startsWith("[----------] 2 tests from SomeGroup"), eq("SomeGroup"), eq(2));
        inOrder.verify(listener).testOutput("", Optional.empty(), Optional.empty());
        inOrder.verify(listener).testOutput("[----------] Global test environment tear-down", Optional.empty(), Optional.empty());
        inOrder.verify(listener).suiteEnd(startsWith("[==========] 2 tests from 1 test case ran."), eq(2), eq(1));
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

        StubRunner runner = new StubRunner("two_groups_with_one_test_each.exe", listener);
        runner.execute();
        assertEquals(runner.get().failedTests, ImmutableList.of("OtherGroup.ExpectTest"));

        inOrder.verify(listener).suiteStart("[==========] Running 2 tests from 2 test cases.", 2, 2);
        inOrder.verify(listener).testOutput("[----------] Global test environment set-up.", Optional.empty(), Optional.empty());
        inOrder.verify(listener).groupStart("[----------] 1 test from SomeGroup", "SomeGroup", 1);
        inOrder.verify(listener).testStart("[ RUN      ] SomeGroup.TestIsTrue", "SomeGroup", "TestIsTrue");
        inOrder.verify(listener).testPassed(startsWith("[       OK ] SomeGroup.TestIsTrue"), eq("SomeGroup"), eq("TestIsTrue"));
        inOrder.verify(listener).groupEnd(startsWith("[----------] 1 test from SomeGroup"), eq("SomeGroup"), eq(1));
        inOrder.verify(listener).testOutput("", Optional.empty(), Optional.empty());
        inOrder.verify(listener).groupStart("[----------] 1 test from OtherGroup", "OtherGroup", 1);
        inOrder.verify(listener).testStart("[ RUN      ] OtherGroup.ExpectTest", "OtherGroup", "ExpectTest");
        inOrder.verify(listener).testOutput("..\\..\\test_samples\\main.cpp(19): error: Value of: 2", Optional.of("OtherGroup"), Optional.of("ExpectTest"));
        inOrder.verify(listener).testOutput("Expected: 1", Optional.of("OtherGroup"), Optional.of("ExpectTest"));
        inOrder.verify(listener).testOutput("..\\..\\test_samples\\main.cpp(20): error: Value of: 3", Optional.of("OtherGroup"), Optional.of("ExpectTest"));
        inOrder.verify(listener).testOutput("Expected: 2", Optional.of("OtherGroup"), Optional.of("ExpectTest"));
        inOrder.verify(listener).testFailed(startsWith("[  FAILED  ] OtherGroup.ExpectTest"), eq("OtherGroup"), eq("ExpectTest"));
        inOrder.verify(listener).groupEnd(startsWith("[----------] 1 test from OtherGroup"), eq("OtherGroup"), eq(1));
        inOrder.verify(listener).testOutput("", Optional.empty(), Optional.empty());
        inOrder.verify(listener).testOutput("[----------] Global test environment tear-down", Optional.empty(), Optional.empty());
        inOrder.verify(listener).suiteEnd(startsWith("[==========] 2 tests from 2 test cases ran."), eq(2), eq(2));
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

        StubRunner runner = new StubRunner("elapsed_time_off.exe", listener);
        runner.execute();
        assertEquals(runner.get().failedTests, ImmutableList.of("OtherGroup.ExpectTest"));

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

    @Test void rerunFailedTests() throws Exception {
        ClassifiedGTestOutputHandler listener = mock(ClassifiedGTestOutputHandler.class);
        InOrder inOrder = inOrder(listener);

        StubRunner runner = new StubRunner("two_groups_with_one_test_each.exe", listener);
        runner.execute();
        Collection<String> failedTests = runner.get().failedTests;
        assertEquals(failedTests, ImmutableList.of("OtherGroup.ExpectTest"));

        inOrder.verify(listener).suiteStart("[==========] Running 2 tests from 2 test cases.", 2, 2);
        inOrder.verify(listener).testOutput("[----------] Global test environment set-up.", Optional.empty(), Optional.empty());
        inOrder.verify(listener).groupStart("[----------] 1 test from SomeGroup", "SomeGroup", 1);
        inOrder.verify(listener).testStart("[ RUN      ] SomeGroup.TestIsTrue", "SomeGroup", "TestIsTrue");
        inOrder.verify(listener).testPassed(startsWith("[       OK ] SomeGroup.TestIsTrue"), eq("SomeGroup"), eq("TestIsTrue"));
        inOrder.verify(listener).groupEnd(startsWith("[----------] 1 test from SomeGroup"), eq("SomeGroup"), eq(1));
        inOrder.verify(listener).testOutput("", Optional.empty(), Optional.empty());
        inOrder.verify(listener).groupStart("[----------] 1 test from OtherGroup", "OtherGroup", 1);
        inOrder.verify(listener).testStart("[ RUN      ] OtherGroup.ExpectTest", "OtherGroup", "ExpectTest");
        inOrder.verify(listener).testOutput("..\\..\\test_samples\\main.cpp(19): error: Value of: 2", Optional.of("OtherGroup"), Optional.of("ExpectTest"));
        inOrder.verify(listener).testOutput("Expected: 1", Optional.of("OtherGroup"), Optional.of("ExpectTest"));
        inOrder.verify(listener).testOutput("..\\..\\test_samples\\main.cpp(20): error: Value of: 3", Optional.of("OtherGroup"), Optional.of("ExpectTest"));
        inOrder.verify(listener).testOutput("Expected: 2", Optional.of("OtherGroup"), Optional.of("ExpectTest"));
        inOrder.verify(listener).testFailed(startsWith("[  FAILED  ] OtherGroup.ExpectTest"), eq("OtherGroup"), eq("ExpectTest"));
        inOrder.verify(listener).groupEnd(startsWith("[----------] 1 test from OtherGroup"), eq("OtherGroup"), eq(1));
        inOrder.verify(listener).testOutput("", Optional.empty(), Optional.empty());
        inOrder.verify(listener).testOutput("[----------] Global test environment tear-down", Optional.empty(), Optional.empty());
        inOrder.verify(listener).suiteEnd(startsWith("[==========] 2 tests from 2 test cases ran."), eq(2), eq(2));
        inOrder.verify(listener).passedTestsSummary("[  PASSED  ] 1 test.", 1);
        inOrder.verify(listener).failedTestsSummary("[  FAILED  ] 1 test, listed below:", 1);
        inOrder.verify(listener).failedTestSummary("[  FAILED  ] OtherGroup.ExpectTest", "OtherGroup", "ExpectTest");
        inOrder.verify(listener).summaryOutput("");
        inOrder.verify(listener).summaryOutput(" 1 FAILED TEST");
        inOrder.verifyNoMoreInteractions();

        runner = new StubRunner("two_groups_with_one_test_each.exe", failedTests, listener);
        runner.execute();
        assertEquals(runner.get().failedTests, failedTests);

        inOrder.verify(listener).outputBeforeSuiteStarted("Note: Google Test filter = OtherGroup.ExpectTest");
        inOrder.verify(listener).suiteStart("[==========] Running 1 test from 1 test case.", 1, 1);
        inOrder.verify(listener).testOutput("[----------] Global test environment set-up.", Optional.empty(), Optional.empty());
        inOrder.verify(listener).groupStart("[----------] 1 test from OtherGroup", "OtherGroup", 1);
        inOrder.verify(listener).testStart("[ RUN      ] OtherGroup.ExpectTest", "OtherGroup", "ExpectTest");
        inOrder.verify(listener).testOutput("..\\..\\test_samples\\main.cpp(19): error: Value of: 2", Optional.of("OtherGroup"), Optional.of("ExpectTest"));
        inOrder.verify(listener).testOutput("Expected: 1", Optional.of("OtherGroup"), Optional.of("ExpectTest"));
        inOrder.verify(listener).testOutput("..\\..\\test_samples\\main.cpp(20): error: Value of: 3", Optional.of("OtherGroup"), Optional.of("ExpectTest"));
        inOrder.verify(listener).testOutput("Expected: 2", Optional.of("OtherGroup"), Optional.of("ExpectTest"));
        inOrder.verify(listener).testFailed(startsWith("[  FAILED  ] OtherGroup.ExpectTest"), eq("OtherGroup"), eq("ExpectTest"));
        inOrder.verify(listener).groupEnd(startsWith("[----------] 1 test from OtherGroup"), eq("OtherGroup"), eq(1));
        inOrder.verify(listener).testOutput("", Optional.empty(), Optional.empty());
        inOrder.verify(listener).testOutput("[----------] Global test environment tear-down", Optional.empty(), Optional.empty());
        inOrder.verify(listener).suiteEnd(startsWith("[==========] 1 test from 1 test case ran."), eq(1), eq(1));
        inOrder.verify(listener).passedTestsSummary("[  PASSED  ] 0 tests.", 0);
        inOrder.verify(listener).failedTestsSummary("[  FAILED  ] 1 test, listed below:", 1);
        inOrder.verify(listener).failedTestSummary("[  FAILED  ] OtherGroup.ExpectTest", "OtherGroup", "ExpectTest");
        inOrder.verify(listener).summaryOutput("");
        inOrder.verify(listener).summaryOutput(" 1 FAILED TEST");
        inOrder.verifyNoMoreInteractions();
    }

    //todo test task cancellation

    private class StubRunner extends GTestRunner {
        public StubRunner(String testExeName, ClassifiedGTestOutputHandler listener) {
            super(new File(testSamplesDir, testExeName).getAbsolutePath(), listener);
        }

        public StubRunner(String testExeName, Collection<String> failedTests, ClassifiedGTestOutputHandler listener) {
            super(new File(testSamplesDir, testExeName).getAbsolutePath(), failedTests, listener);
        }

        @Override
        protected void onProgress(SuiteProgress progress) {
            //do nothing
        }

        @Override
        protected void onFinish(SuiteResult result) {
            //do nothing
        }
    }
}