package gumanoid.parser;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Created by Gumanoid on 07.01.2016.
 */
@Test
public class GTestOutputParserUTest {
    ClassifiedGTestOutputHandler listener = mock(ClassifiedGTestOutputHandler.class);
    GTestOutputParser parser;

    @BeforeMethod void resetListener() {
        reset(listener);
        parser = new GTestOutputParser(listener);
    }

    @Test void suiteStart() throws Exception {
        parser.onNextLine("[==========] Running 3 tests from 2 test cases.");
        verify(listener).suiteStart("[==========] Running 3 tests from 2 test cases.", 3, 2);
        verifyNoMoreInteractions(listener);
    }

    @Test void testEnvSetUp() throws Exception {
        suiteStart();
        parser.onNextLine("[----------] Global test environment set-up.");
        verify(listener).testOutput("[----------] Global test environment set-up.", Optional.empty(), Optional.empty());
        verifyZeroInteractions(listener);
    }

    @Test void groupStart() throws Exception {
        suiteStart();
        parser.onNextLine("[----------] 2 tests from SomeGroup");
        verify(listener).groupStart("[----------] 2 tests from SomeGroup", "SomeGroup", 2);
        verifyNoMoreInteractions(listener);
    }

    @Test void testStart() throws Exception {
        groupStart();

        parser.onNextLine("[ RUN      ] SomeGroup.TestIsTrue");
        verify(listener).testStart("[ RUN      ] SomeGroup.TestIsTrue", "SomeGroup", "TestIsTrue");
        verifyNoMoreInteractions(listener);
    }

    @Test void testPassed() throws Exception {
        testStart();

        parser.onNextLine("[       OK ] SomeGroup.TestIsTrue (0 ms)");
        verify(listener).testPassed("[       OK ] SomeGroup.TestIsTrue (0 ms)", "SomeGroup", "TestIsTrue");
        verifyNoMoreInteractions(listener);
    }

    @Test void testOutput() throws Exception {
        testStart();

        parser.onNextLine("..\\..\\..\\test_samples\\main.cpp(12): error: Value of: 0 == 0");
        verify(listener).testOutput("..\\..\\..\\test_samples\\main.cpp(12): error: Value of: 0 == 0", Optional.of("SomeGroup"), Optional.of("TestIsTrue"));

        parser.onNextLine("  Actual: true");
        verify(listener).testOutput("  Actual: true", Optional.of("SomeGroup"), Optional.of("TestIsTrue"));

        parser.onNextLine("Expected: false");
        verify(listener).testOutput("Expected: false", Optional.of("SomeGroup"), Optional.of("TestIsTrue"));

        verifyNoMoreInteractions(listener);
    }

    @Test void testFailed() throws Exception {
        testStart();

        parser.onNextLine("[  FAILED  ] SomeGroup.TestIsTrue (1 ms)");
        verify(listener).testFailed("[  FAILED  ] SomeGroup.TestIsTrue (1 ms)", "SomeGroup", "TestIsTrue");
        verifyNoMoreInteractions(listener);
    }

    @Test void groupEnd() throws Exception {
        groupStart();
        parser.onNextLine("[----------] 2 tests from SomeGroup (1 ms total)");
        verify(listener).groupEnd("[----------] 2 tests from SomeGroup (1 ms total)", "SomeGroup", 2);
        verifyNoMoreInteractions(listener);
    }

    @Test void testEnvTearDown() throws Exception {
        groupEnd();
        parser.onNextLine("[----------] Global test environment tear-down");
        verify(listener).testOutput("[----------] Global test environment tear-down", Optional.empty(), Optional.empty());
        verifyZeroInteractions(listener);
    }

    @Test void suiteEnd() throws Exception {
        suiteStart();
        parser.onNextLine("[==========] 3 tests from 2 test cases ran. (2005 ms total)");

        verify(listener).suiteEnd("[==========] 3 tests from 2 test cases ran. (2005 ms total)", 3, 2);
        verifyNoMoreInteractions(listener);
    }

    @Test void suiteSummary() throws Exception {
        suiteEnd();

        parser.onNextLine("[  PASSED  ] 1 test.");
        parser.onNextLine("[  FAILED  ] 2 tests, listed below:");
        parser.onNextLine("[  FAILED  ] SomeGroup.FailingTest");
        parser.onNextLine("[  FAILED  ] OtherGroup.LongTest");
        parser.onNextLine("");
        parser.onNextLine(" 2 FAILED TESTS");

        verify(listener).passedTestsSummary("[  PASSED  ] 1 test.", 1);
        verify(listener).failedTestsSummary("[  FAILED  ] 2 tests, listed below:", 2);
        verify(listener).failedTestSummary("[  FAILED  ] SomeGroup.FailingTest", "SomeGroup", "FailingTest");
        verify(listener).failedTestSummary("[  FAILED  ] OtherGroup.LongTest", "OtherGroup", "LongTest");
        verify(listener).summaryOutput("");
        verify(listener).summaryOutput(" 2 FAILED TESTS");
        verifyNoMoreInteractions(listener);
    }
}