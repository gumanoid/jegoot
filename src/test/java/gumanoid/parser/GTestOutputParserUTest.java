package gumanoid.parser;

import com.google.common.collect.ImmutableList;
import gumanoid.event.GTestOutputEvent;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import rx.observers.TestObserver;
import rx.subjects.BehaviorSubject;

import java.util.Optional;

import static gumanoid.event.GTestOutputEvent.*;
import static org.testng.Assert.assertEquals;

/**
 * Created by Gumanoid on 07.01.2016.
 */
@Test
public class GTestOutputParserUTest {
    BehaviorSubject<String> input;
    TestObserver<GTestOutputEvent> output;

    @BeforeMethod void resetListener() {
        input = BehaviorSubject.create();
        output = new TestObserver<>();
        input.lift(new GTestOutputParser()).subscribe(output);
    }

    @Test void suiteStart() throws Exception {
        input.onNext("[==========] Running 3 tests from 2 test cases.");
        
        assertEquals(output.getOnNextEvents(), ImmutableList.of(
                new SuiteStart("[==========] Running 3 tests from 2 test cases.", 3, 2)
        ));
        assertEquals(output.getOnCompletedEvents(), ImmutableList.of());
        assertEquals(output.getOnErrorEvents(), ImmutableList.of());
    }

    @Test void testEnvSetUp() throws Exception {
        input.onNext("[==========] Running 3 tests from 2 test cases.");
        input.onNext("[----------] Global test environment set-up.");
        
        assertEquals(output.getOnNextEvents(), ImmutableList.of(
                new SuiteStart("[==========] Running 3 tests from 2 test cases.", 3, 2),
                new TestOutput("[----------] Global test environment set-up.", Optional.empty(), Optional.empty())
        ));
        assertEquals(output.getOnCompletedEvents(), ImmutableList.of());
        assertEquals(output.getOnErrorEvents(), ImmutableList.of());
    }

    @Test void groupStart() throws Exception {
        input.onNext("[==========] Running 3 tests from 2 test cases.");
        input.onNext("[----------] 2 tests from SomeGroup");
        
        assertEquals(output.getOnNextEvents(), ImmutableList.of(
                new SuiteStart("[==========] Running 3 tests from 2 test cases.", 3, 2),
                new GroupStart("[----------] 2 tests from SomeGroup", "SomeGroup", 2)
        ));
        assertEquals(output.getOnCompletedEvents(), ImmutableList.of());
        assertEquals(output.getOnErrorEvents(), ImmutableList.of());
    }

    @Test void testStart() throws Exception {
        input.onNext("[==========] Running 3 tests from 2 test cases.");
        input.onNext("[----------] 2 tests from SomeGroup");
        input.onNext("[ RUN      ] SomeGroup.TestIsTrue");
        
        assertEquals(output.getOnNextEvents(), ImmutableList.of(
                new SuiteStart("[==========] Running 3 tests from 2 test cases.", 3, 2),
                new GroupStart("[----------] 2 tests from SomeGroup", "SomeGroup", 2),
                new TestStart("[ RUN      ] SomeGroup.TestIsTrue", "SomeGroup", "TestIsTrue")
        ));
        assertEquals(output.getOnCompletedEvents(), ImmutableList.of());
        assertEquals(output.getOnErrorEvents(), ImmutableList.of());
    }

    @Test void testPassed() throws Exception {
        input.onNext("[==========] Running 3 tests from 2 test cases.");
        input.onNext("[----------] 2 tests from SomeGroup");
        input.onNext("[ RUN      ] SomeGroup.TestIsTrue");
        input.onNext("[       OK ] SomeGroup.TestIsTrue (0 ms)");

        assertEquals(output.getOnNextEvents(), ImmutableList.of(
                new SuiteStart("[==========] Running 3 tests from 2 test cases.", 3, 2),
                new GroupStart("[----------] 2 tests from SomeGroup", "SomeGroup", 2),
                new TestStart("[ RUN      ] SomeGroup.TestIsTrue", "SomeGroup", "TestIsTrue"),
                new TestPassed("[       OK ] SomeGroup.TestIsTrue (0 ms)", "SomeGroup", "TestIsTrue")
        ));
        assertEquals(output.getOnCompletedEvents(), ImmutableList.of());
        assertEquals(output.getOnErrorEvents(), ImmutableList.of());
    }

    @Test void testOutput() throws Exception {
        input.onNext("[==========] Running 3 tests from 2 test cases.");
        input.onNext("[----------] 2 tests from SomeGroup");
        input.onNext("[ RUN      ] SomeGroup.TestIsTrue");
        input.onNext("..\\..\\..\\test_samples\\main.cpp(12): error: Value of: 0 == 0");
        input.onNext("  Actual: true");
        input.onNext("Expected: false");

        assertEquals(output.getOnNextEvents(), ImmutableList.of(
                new SuiteStart("[==========] Running 3 tests from 2 test cases.", 3, 2),
                new GroupStart("[----------] 2 tests from SomeGroup", "SomeGroup", 2),
                new TestStart("[ RUN      ] SomeGroup.TestIsTrue", "SomeGroup", "TestIsTrue"),
                new TestOutput("..\\..\\..\\test_samples\\main.cpp(12): error: Value of: 0 == 0", Optional.of("SomeGroup"), Optional.of("TestIsTrue")),
                new TestOutput("  Actual: true", Optional.of("SomeGroup"), Optional.of("TestIsTrue")),
                new TestOutput("Expected: false", Optional.of("SomeGroup"), Optional.of("TestIsTrue"))
        ));
        assertEquals(output.getOnCompletedEvents(), ImmutableList.of());
        assertEquals(output.getOnErrorEvents(), ImmutableList.of());
    }

    @Test void testFailed() throws Exception {
        input.onNext("[==========] Running 3 tests from 2 test cases.");
        input.onNext("[----------] 2 tests from SomeGroup");
        input.onNext("[ RUN      ] SomeGroup.TestIsTrue");
        input.onNext("[  FAILED  ] SomeGroup.TestIsTrue (1 ms)");

        assertEquals(output.getOnNextEvents(), ImmutableList.of(
                new SuiteStart("[==========] Running 3 tests from 2 test cases.", 3, 2),
                new GroupStart("[----------] 2 tests from SomeGroup", "SomeGroup", 2),
                new TestStart("[ RUN      ] SomeGroup.TestIsTrue", "SomeGroup", "TestIsTrue"),
                new TestFailed("[  FAILED  ] SomeGroup.TestIsTrue (1 ms)", "SomeGroup", "TestIsTrue")
        ));
        assertEquals(output.getOnCompletedEvents(), ImmutableList.of());
        assertEquals(output.getOnErrorEvents(), ImmutableList.of());

    }

    @Test void groupEnd() throws Exception {
        input.onNext("[==========] Running 3 tests from 2 test cases.");
        input.onNext("[----------] 2 tests from SomeGroup");
        input.onNext("[----------] 2 tests from SomeGroup (1 ms total)");

        assertEquals(output.getOnNextEvents(), ImmutableList.of(
                new SuiteStart("[==========] Running 3 tests from 2 test cases.", 3, 2),
                new GroupStart("[----------] 2 tests from SomeGroup", "SomeGroup", 2),
                new GroupEnd("[----------] 2 tests from SomeGroup (1 ms total)", "SomeGroup", 2)
        ));
        assertEquals(output.getOnCompletedEvents(), ImmutableList.of());
        assertEquals(output.getOnErrorEvents(), ImmutableList.of());
    }

    @Test void testEnvTearDown() throws Exception {
        input.onNext("[==========] Running 3 tests from 2 test cases.");
        input.onNext("[----------] 2 tests from SomeGroup");
        input.onNext("[----------] 2 tests from SomeGroup (1 ms total)");
        input.onNext("[----------] Global test environment tear-down");

        assertEquals(output.getOnNextEvents(), ImmutableList.of(
                new SuiteStart("[==========] Running 3 tests from 2 test cases.", 3, 2),
                new GroupStart("[----------] 2 tests from SomeGroup", "SomeGroup", 2),
                new GroupEnd("[----------] 2 tests from SomeGroup (1 ms total)", "SomeGroup", 2),
                new TestOutput("[----------] Global test environment tear-down", Optional.empty(), Optional.empty())
        ));
        assertEquals(output.getOnCompletedEvents(), ImmutableList.of());
        assertEquals(output.getOnErrorEvents(), ImmutableList.of());
    }

    @Test void suiteEnd() throws Exception {
        input.onNext("[==========] Running 3 tests from 2 test cases.");
        input.onNext("[==========] 3 tests from 2 test cases ran. (2005 ms total)");

        assertEquals(output.getOnNextEvents(), ImmutableList.of(
                new SuiteStart("[==========] Running 3 tests from 2 test cases.", 3, 2),
                new SuiteEnd("[==========] 3 tests from 2 test cases ran. (2005 ms total)", 3, 2)
        ));
        assertEquals(output.getOnCompletedEvents(), ImmutableList.of());
        assertEquals(output.getOnErrorEvents(), ImmutableList.of());
    }

    @Test void suiteSummary() throws Exception {
        input.onNext("[==========] Running 3 tests from 2 test cases.");
        input.onNext("[==========] 3 tests from 2 test cases ran. (2005 ms total)");

        input.onNext("[  PASSED  ] 1 test.");
        input.onNext("[  FAILED  ] 2 tests, listed below:");
        input.onNext("[  FAILED  ] SomeGroup.FailingTest");
        input.onNext("[  FAILED  ] OtherGroup.LongTest");
        input.onNext("");
        input.onNext(" 2 FAILED TESTS");

        assertEquals(output.getOnNextEvents(), ImmutableList.of(
                new SuiteStart("[==========] Running 3 tests from 2 test cases.", 3, 2),
                new SuiteEnd("[==========] 3 tests from 2 test cases ran. (2005 ms total)", 3, 2),
                new PassedTestsSummary("[  PASSED  ] 1 test.", 1),
                new FailedTestsSummary("[  FAILED  ] 2 tests, listed below:", 2),
                new FailedTestSummary("[  FAILED  ] SomeGroup.FailingTest", "SomeGroup", "FailingTest"),
                new FailedTestSummary("[  FAILED  ] OtherGroup.LongTest", "OtherGroup", "LongTest"),
                new SummaryOutput(""),
                new SummaryOutput(" 2 FAILED TESTS")
        ));
        assertEquals(output.getOnCompletedEvents(), ImmutableList.of());
        assertEquals(output.getOnErrorEvents(), ImmutableList.of());
    }
}