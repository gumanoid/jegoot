package gumanoid.parser;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Created by Gumanoid on 07.01.2016.
 */
@Test
public class GTestOutputParserUTest {
    GTestOutputParser.EventListener listener = mock(GTestOutputParser.EventListener.class);
    GTestOutputParser parser = new GTestOutputParser(listener);

    @BeforeMethod void resetListener() {
        reset(listener);
    }

    @Test void suiteStart() throws Exception {
        parser.onNextLine("[==========] Running 3 tests from 2 test cases.");
        verify(listener).suiteStart(3, 2);
    }

    @Test void testEnvSetUp() throws Exception {
        parser.onNextLine("[----------] Global test environment set-up.");
        verifyZeroInteractions(listener);
    }

    @Test void groupStart() throws Exception {
        parser.onNextLine("[----------] 2 tests from SomeGroup");
        verify(listener).groupStart("SomeGroup", 2);
    }

    @Test void testStart() throws Exception {
        parser.onNextLine("[ RUN      ] SomeGroup.TestIsTrue");
        verify(listener).testStart("SomeGroup", "TestIsTrue");
    }

    @Test void testPassed() throws Exception {
        parser.onNextLine("[       OK ] SomeGroup.TestIsTrue (0 ms)");
        verify(listener).testPassed("SomeGroup", "TestIsTrue");
    }

    @Test void testOutput() throws Exception {
        parser.onNextLine("..\\..\\..\\test_samples\\main.cpp(12): error: Value of: 0 == 0");
        verify(listener).testOutput("..\\..\\..\\test_samples\\main.cpp(12): error: Value of: 0 == 0");

        parser.onNextLine("  Actual: true");
        verify(listener).testOutput("  Actual: true");

        parser.onNextLine("Expected: false");
        verify(listener).testOutput("Expected: false");
    }

    @Test void testFailed() throws Exception {
        parser.onNextLine("[  FAILED  ] SomeGroup.FailingTest (1 ms)");
        verify(listener).testFailed("SomeGroup", "TestIsTrue");
    }

    @Test void groupEnd() throws Exception {
        parser.onNextLine("[----------] 2 tests from SomeGroup (1 ms total)");
        verify(listener).groupEnd("SomeGroup", 2);
    }

    @Test void emptyLine() throws Exception {
        parser.onNextLine("");
        verifyZeroInteractions(listener);
    }

    @Test void testEnvTearDown() throws Exception {
        parser.onNextLine("[----------] Global test environment tear-down");
        verifyZeroInteractions(listener);
    }

    @Test void suiteEnd() throws Exception {
        parser.onNextLine("[==========] 3 tests from 2 test cases ran. (2005 ms total)");
        parser.onNextLine("[  PASSED  ] 1 test.");
        parser.onNextLine("[  FAILED  ] 2 tests, listed below:");
        parser.onNextLine("[  FAILED  ] SomeGroup.FailingTest");
        parser.onNextLine("[  FAILED  ] OtherGroup.LongTest");
        parser.onNextLine("");
        parser.onNextLine(" 2 FAILED TESTS");

        verify(listener).suiteEnd(3, 2);
        verify(listener).passedTestsSummary(1);
        verify(listener).failedTestsSummary(2);
        verify(listener).failedTestSummary("SomeGroup", "FailingTest");
        verify(listener).failedTestSummary("OtherGroup", "LongTest");
        verifyNoMoreInteractions(listener);
    }
}