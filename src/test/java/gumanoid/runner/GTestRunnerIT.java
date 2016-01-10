package gumanoid.runner;

import com.google.common.collect.ImmutableList;
import gumanoid.parser.GTestOutputParser;
import org.mockito.InOrder;
import org.testng.annotations.BeforeMethod;
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
    //todo tests are Windows-only, try to generalize for all platforms
    //todo tests are failing occasionally, looks like race condition
    GTestOutputParser.EventListener listener = mock(GTestOutputParser.EventListener.class);
    InOrder inOrder = inOrder(listener);

    File testSamplesDir = new File(Paths.get("").toAbsolutePath().toFile(), "test_samples");

    @BeforeMethod
    void resetListener() {
        reset(listener);
    }

    @Test void emptySuite() throws Exception {
        StubRunner runner = new StubRunner("empty_suite.exe");
        runner.execute();
        assertEquals(runner.get().failedTests, ImmutableList.of());

        inOrder.verify(listener).suiteStart(0, 0);
        inOrder.verify(listener).suiteEnd(0, 0);
        inOrder.verify(listener).passedTestsSummary(0);
        verifyNoMoreInteractions(listener);
    }

    @Test void singlePassingTest() throws Exception {
        StubRunner runner = new StubRunner("single_passing.exe");
        runner.execute();
        assertEquals(runner.get().failedTests, ImmutableList.of());

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
        StubRunner runner = new StubRunner("single_failing.exe");
        runner.execute();
        assertEquals(runner.get().failedTests, ImmutableList.of("SomeGroup.FailingTest"));

        verify(listener).suiteStart(1, 1);
        verify(listener).groupStart("SomeGroup", 1);
        verify(listener).testStart("SomeGroup", "FailingTest");
        verify(listener).testOutput(Optional.of("SomeGroup"), Optional.of("FailingTest"), "..\\..\\test_samples\\main.cpp(14): error: Value of: 0 == 0");
        verify(listener).testOutput(Optional.of("SomeGroup"), Optional.of("FailingTest"), "  Actual: true");
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
        StubRunner runner = new StubRunner("two_tests_in_one_group.exe");
        runner.execute();
        assertEquals(runner.get().failedTests, ImmutableList.of("SomeGroup.FailingTest"));

        inOrder.verify(listener).suiteStart(2, 1);
        inOrder.verify(listener).groupStart("SomeGroup", 2);
        inOrder.verify(listener).testStart("SomeGroup", "TestIsTrue");
        inOrder.verify(listener).testPassed("SomeGroup", "TestIsTrue");
        inOrder.verify(listener).testStart("SomeGroup", "FailingTest");
        inOrder.verify(listener).testOutput(Optional.of("SomeGroup"), Optional.of("FailingTest"), "..\\..\\test_samples\\main.cpp(14): error: Value of: 0 == 0");
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
        StubRunner runner = new StubRunner("two_groups_with_one_test_each.exe");
        runner.execute();
        assertEquals(runner.get().failedTests, ImmutableList.of("OtherGroup.ExpectTest"));

        inOrder.verify(listener).suiteStart(2, 2);
        inOrder.verify(listener).groupStart("SomeGroup", 1);
        inOrder.verify(listener).testStart("SomeGroup", "TestIsTrue");
        inOrder.verify(listener).testPassed("SomeGroup", "TestIsTrue");
        inOrder.verify(listener).groupEnd("SomeGroup", 1);
        inOrder.verify(listener).testOutput(Optional.empty(), Optional.empty(), "");
        inOrder.verify(listener).groupStart("OtherGroup", 1);
        inOrder.verify(listener).testStart("OtherGroup", "ExpectTest");
        inOrder.verify(listener).testOutput(Optional.of("OtherGroup"), Optional.of("ExpectTest"), "..\\..\\test_samples\\main.cpp(19): error: Value of: 2");
        inOrder.verify(listener).testOutput(Optional.of("OtherGroup"), Optional.of("ExpectTest"), "Expected: 1");
        inOrder.verify(listener).testOutput(Optional.of("OtherGroup"), Optional.of("ExpectTest"), "..\\..\\test_samples\\main.cpp(20): error: Value of: 3");
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
        StubRunner runner = new StubRunner("elapsed_time_off.exe");
        runner.execute();
        assertEquals(runner.get().failedTests, ImmutableList.of("OtherGroup.ExpectTest"));

        inOrder.verify(listener).suiteStart(2, 2);
        inOrder.verify(listener).groupStart("SomeGroup", 1);
        inOrder.verify(listener).testStart("SomeGroup", "TestIsTrue");
        inOrder.verify(listener).testPassed("SomeGroup", "TestIsTrue");
        inOrder.verify(listener).groupEnd("SomeGroup", 1);
        inOrder.verify(listener).groupStart("OtherGroup", 1);
        inOrder.verify(listener).testStart("OtherGroup", "ExpectTest");
        inOrder.verify(listener).testOutput(Optional.of("OtherGroup"), Optional.of("ExpectTest"), "..\\..\\test_samples\\main.cpp(19): error: Value of: 2");
        inOrder.verify(listener).testOutput(Optional.of("OtherGroup"), Optional.of("ExpectTest"), "Expected: 1");
        inOrder.verify(listener).testOutput(Optional.of("OtherGroup"), Optional.of("ExpectTest"), "..\\..\\test_samples\\main.cpp(20): error: Value of: 3");
        inOrder.verify(listener).testOutput(Optional.of("OtherGroup"), Optional.of("ExpectTest"), "Expected: 2");
        inOrder.verify(listener).testFailed("OtherGroup", "ExpectTest");
        inOrder.verify(listener).groupEnd("OtherGroup", 1);
        inOrder.verify(listener).suiteEnd(2, 2);
        inOrder.verify(listener).passedTestsSummary(1);
        inOrder.verify(listener).failedTestsSummary(1);
        inOrder.verify(listener).failedTestSummary("OtherGroup", "ExpectTest");
        inOrder.verify(listener).summaryOutput("");
        inOrder.verify(listener).summaryOutput(" 1 FAILED TEST");
        verifyNoMoreInteractions(listener);
    }

    @Test void rerunFailedTests() throws Exception {
        StubRunner runner = new StubRunner("two_groups_with_one_test_each.exe");
        runner.execute();
        Collection<String> failedTests = runner.get().failedTests;
        assertEquals(failedTests, ImmutableList.of("OtherGroup.ExpectTest"));

        inOrder.verify(listener).suiteStart(2, 2);
        inOrder.verify(listener).groupStart("SomeGroup", 1);
        inOrder.verify(listener).testStart("SomeGroup", "TestIsTrue");
        inOrder.verify(listener).testPassed("SomeGroup", "TestIsTrue");
        inOrder.verify(listener).groupEnd("SomeGroup", 1);
        inOrder.verify(listener).testOutput(Optional.empty(), Optional.empty(), "");
        inOrder.verify(listener).groupStart("OtherGroup", 1);
        inOrder.verify(listener).testStart("OtherGroup", "ExpectTest");
        inOrder.verify(listener).testOutput(Optional.of("OtherGroup"), Optional.of("ExpectTest"), "..\\..\\test_samples\\main.cpp(19): error: Value of: 2");
        inOrder.verify(listener).testOutput(Optional.of("OtherGroup"), Optional.of("ExpectTest"), "Expected: 1");
        inOrder.verify(listener).testOutput(Optional.of("OtherGroup"), Optional.of("ExpectTest"), "..\\..\\test_samples\\main.cpp(20): error: Value of: 3");
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

        runner = new StubRunner("two_groups_with_one_test_each.exe", failedTests);
        runner.execute();
        assertEquals(runner.get().failedTests, failedTests);

        inOrder.verify(listener).outputBeforeSuiteStarted("Note: Google Test filter = OtherGroup.ExpectTest");
        inOrder.verify(listener).suiteStart(1, 1);
        inOrder.verify(listener).groupStart("OtherGroup", 1);
        inOrder.verify(listener).testStart("OtherGroup", "ExpectTest");
        inOrder.verify(listener).testOutput(Optional.of("OtherGroup"), Optional.of("ExpectTest"), "..\\..\\test_samples\\main.cpp(19): error: Value of: 2");
        inOrder.verify(listener).testOutput(Optional.of("OtherGroup"), Optional.of("ExpectTest"), "Expected: 1");
        inOrder.verify(listener).testOutput(Optional.of("OtherGroup"), Optional.of("ExpectTest"), "..\\..\\test_samples\\main.cpp(20): error: Value of: 3");
        inOrder.verify(listener).testOutput(Optional.of("OtherGroup"), Optional.of("ExpectTest"), "Expected: 2");
        inOrder.verify(listener).testFailed("OtherGroup", "ExpectTest");
        inOrder.verify(listener).groupEnd("OtherGroup", 1);
        inOrder.verify(listener).testOutput(Optional.empty(), Optional.empty(), "");
        inOrder.verify(listener).suiteEnd(1, 1);
        inOrder.verify(listener).passedTestsSummary(0);
        inOrder.verify(listener).failedTestsSummary(1);
        inOrder.verify(listener).failedTestSummary("OtherGroup", "ExpectTest");
        inOrder.verify(listener).summaryOutput("");
        inOrder.verify(listener).summaryOutput(" 1 FAILED TEST");
        verifyNoMoreInteractions(listener);
    }

    //todo test task cancellation

    private class StubRunner extends GTestRunner {
        public StubRunner(String testExeName) {
            super(new File(testSamplesDir, testExeName).getAbsolutePath(), listener);
        }

        public StubRunner(String testExeName, Collection<String> failedTests) {
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