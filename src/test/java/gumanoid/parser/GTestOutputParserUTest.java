package gumanoid.parser;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * Created by Gumanoid on 07.01.2016.
 */
@Test
public class GTestOutputParserUTest {
    //todo check cases when time measuring is suppressed
    //todo looks like test number formatter prints different
    //text for 1 and several tests (e. g. 1 test and 3 tests)

    GTestOutputParser.EventListener listener = mock(GTestOutputParser.EventListener.class);
    GTestOutputParser parser;

    @BeforeMethod void resetListener() {
        reset(listener);
        parser = new GTestOutputParser(listener);
    }

    @Test void suiteStart() throws Exception {
        parser.onNextLine("[==========] Running 3 tests from 2 test cases.");
        verify(listener).suiteStart(3, 2);
        verifyNoMoreInteractions(listener);
    }

    @Test void testEnvSetUp() throws Exception {
        parser.onNextLine("[----------] Global test environment set-up.");
        verifyZeroInteractions(listener);
    }

    @Test void groupStart() throws Exception {
        suiteStart();
        parser.onNextLine("[----------] 2 tests from SomeGroup");
        verify(listener).groupStart("SomeGroup", 2);
        verifyNoMoreInteractions(listener);
    }

    @Test void testStart() throws Exception {
        parser.onNextLine("[ RUN      ] SomeGroup.TestIsTrue");
        verify(listener).testStart("SomeGroup", "TestIsTrue");
        verifyNoMoreInteractions(listener);
    }

    @Test void testPassed() throws Exception {
        suiteStart();

        parser.onNextLine("[       OK ] SomeGroup.TestIsTrue (0 ms)");
        verify(listener).testPassed("SomeGroup", "TestIsTrue");
        verifyNoMoreInteractions(listener);
    }

    @Test void testOutput() throws Exception {
        suiteStart();

        parser.onNextLine("..\\..\\..\\test_samples\\main.cpp(12): error: Value of: 0 == 0");
        verify(listener).testOutput("..\\..\\..\\test_samples\\main.cpp(12): error: Value of: 0 == 0");

        parser.onNextLine("  Actual: true");
        verify(listener).testOutput("  Actual: true");

        parser.onNextLine("Expected: false");
        verify(listener).testOutput("Expected: false");

        verifyNoMoreInteractions(listener);
    }

    @Test void testFailed() throws Exception {
        suiteStart();

        parser.onNextLine("[  FAILED  ] SomeGroup.FailingTest (1 ms)");
        verify(listener).testFailed("SomeGroup", "FailingTest");
        verifyNoMoreInteractions(listener);
    }

    @Test void groupEnd() throws Exception {
        groupStart();
        parser.onNextLine("[----------] 2 tests from SomeGroup (1 ms total)");
        verify(listener).groupEnd("SomeGroup", 2);
        verifyNoMoreInteractions(listener);
    }

    @Test void testEnvTearDown() throws Exception {
        parser.onNextLine("[----------] Global test environment tear-down");
        verifyZeroInteractions(listener);
    }

    @Test void suiteEnd() throws Exception {
        suiteStart();
        parser.onNextLine("[==========] 3 tests from 2 test cases ran. (2005 ms total)");

        verify(listener).suiteEnd(3, 2);
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

        verify(listener).passedTestsSummary(1);
        verify(listener).failedTestsSummary(2);
        verify(listener).failedTestSummary("SomeGroup", "FailingTest");
        verify(listener).failedTestSummary("OtherGroup", "LongTest");
        verify(listener).summaryOutput("");
        verify(listener).summaryOutput(" 2 FAILED TESTS");
        verifyNoMoreInteractions(listener);
    }
}