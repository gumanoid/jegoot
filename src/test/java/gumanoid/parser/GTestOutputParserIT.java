package gumanoid.parser;

import com.google.common.collect.ImmutableList;
import gumanoid.event.GTestOutputEvent;
import org.mockito.InOrder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import rx.Notification;
import rx.observers.TestObserver;
import rx.subjects.BehaviorSubject;

import java.util.Optional;

import static gumanoid.event.GTestOutputEvent.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

/**
 * Created by Gumanoid on 08.01.2016.
 */
@Test
public class GTestOutputParserIT {
    BehaviorSubject<String> input;
    TestObserver<GTestOutputEvent> output;

    @BeforeMethod
    void resetListener() {
        input = BehaviorSubject.create();
        output = new TestObserver<>();
        input.lift(new GTestOutputParser()).subscribe(output);
    }

    @Test
    void emptySuite() throws Exception {
        input.onNext("[==========] Running 0 tests from 0 test cases.");
        input.onNext("[==========] 0 tests from 0 test cases ran. (1 ms total)");
        input.onNext("[  PASSED  ] 0 tests.");
        input.onCompleted();

        assertEquals(output.getOnNextEvents(), ImmutableList.of(
                new SuiteStart("[==========] Running 0 tests from 0 test cases.", 0, 0),
                new SuiteEnd("[==========] 0 tests from 0 test cases ran. (1 ms total)", 0, 0),
                new PassedTestsSummary("[  PASSED  ] 0 tests.", 0)
        ));
        assertEquals(output.getOnCompletedEvents(), ImmutableList.of(Notification.createOnCompleted()));
        assertEquals(output.getOnErrorEvents(), ImmutableList.of());
    }

    @Test
    void singlePassingTest() throws Exception {
        input.onNext("[==========] Running 1 test from 1 test case.");
        input.onNext("[----------] Global test environment set-up.");
        input.onNext("[----------] 1 test from SomeGroup");
        input.onNext("[ RUN      ] SomeGroup.TestIsTrue");
        input.onNext("[       OK ] SomeGroup.TestIsTrue (0 ms)");
        input.onNext("[----------] 1 test from SomeGroup (0 ms total)");
        input.onNext("");
        input.onNext("[----------] Global test environment tear-down");
        input.onNext("[==========] 1 test from 1 test case ran. (2 ms total)");
        input.onNext("[  PASSED  ] 1 test.");
        input.onCompleted();

        assertEquals(output.getOnNextEvents(), ImmutableList.of(
                new SuiteStart("[==========] Running 1 test from 1 test case.", 1, 1),
                new TestOutput("[----------] Global test environment set-up.", Optional.empty(), Optional.empty()),
                new GroupStart("[----------] 1 test from SomeGroup", "SomeGroup", 1),
                new TestStart("[ RUN      ] SomeGroup.TestIsTrue", "SomeGroup", "TestIsTrue"),
                new TestPassed("[       OK ] SomeGroup.TestIsTrue (0 ms)", "SomeGroup", "TestIsTrue"),
                new GroupEnd("[----------] 1 test from SomeGroup (0 ms total)", "SomeGroup", 1),
                new TestOutput("", Optional.empty(), Optional.empty()),
                new TestOutput("[----------] Global test environment tear-down", Optional.empty(), Optional.empty()),
                new SuiteEnd("[==========] 1 test from 1 test case ran. (2 ms total)", 1, 1),
                new PassedTestsSummary("[  PASSED  ] 1 test.", 1)
        ));
        assertEquals(output.getOnCompletedEvents(), ImmutableList.of(Notification.createOnCompleted()));
        assertEquals(output.getOnErrorEvents(), ImmutableList.of());
    }

    @Test
    void singleFailingTest() throws Exception {
        input.onNext("[==========] Running 1 test from 1 test case.");
        input.onNext("[----------] Global test environment set-up.");
        input.onNext("[----------] 1 test from SomeGroup");
        input.onNext("[ RUN      ] SomeGroup.FailingTest");
        input.onNext("..\\..\\..\\test_samples\\main.cpp(12): error: Value of: 0 == 0");
        input.onNext("Actual: true");
        input.onNext("Expected: false");
        input.onNext("[  FAILED  ] SomeGroup.FailingTest (1 ms)");
        input.onNext("[----------] 1 test from SomeGroup (2 ms total)");
        input.onNext("");
        input.onNext("[----------] Global test environment tear-down");
        input.onNext("[==========] 1 test from 1 test case ran. (4 ms total)");
        input.onNext("[  PASSED  ] 0 tests.");
        input.onNext("[  FAILED  ] 1 test, listed below:");
        input.onNext("[  FAILED  ] SomeGroup.FailingTest");
        input.onNext("");
        input.onNext(" 1 FAILED TEST");
        input.onCompleted();

        assertEquals(output.getOnNextEvents(), ImmutableList.of(
                new SuiteStart("[==========] Running 1 test from 1 test case.", 1, 1),
                new TestOutput("[----------] Global test environment set-up.", Optional.empty(), Optional.empty()),
                new GroupStart("[----------] 1 test from SomeGroup", "SomeGroup", 1),
                new TestStart("[ RUN      ] SomeGroup.FailingTest", "SomeGroup", "FailingTest"),
                new TestOutput("..\\..\\..\\test_samples\\main.cpp(12): error: Value of: 0 == 0", Optional.of("SomeGroup"), Optional.of("FailingTest")),
                new TestOutput("Actual: true", Optional.of("SomeGroup"), Optional.of("FailingTest")),
                new TestOutput("Expected: false", Optional.of("SomeGroup"), Optional.of("FailingTest")),
                new TestFailed("[  FAILED  ] SomeGroup.FailingTest (1 ms)", "SomeGroup", "FailingTest"),
                new GroupEnd("[----------] 1 test from SomeGroup (2 ms total)", "SomeGroup", 1),
                new TestOutput("", Optional.empty(), Optional.empty()),
                new TestOutput("[----------] Global test environment tear-down", Optional.empty(), Optional.empty()),
                new SuiteEnd("[==========] 1 test from 1 test case ran. (4 ms total)", 1, 1),
                new PassedTestsSummary("[  PASSED  ] 0 tests.", 0),
                new FailedTestsSummary("[  FAILED  ] 1 test, listed below:", 1),
                new FailedTestSummary("[  FAILED  ] SomeGroup.FailingTest", "SomeGroup", "FailingTest"),
                new SummaryOutput(""),
                new SummaryOutput(" 1 FAILED TEST")
        ));
        assertEquals(output.getOnCompletedEvents(), ImmutableList.of(Notification.createOnCompleted()));
        assertEquals(output.getOnErrorEvents(), ImmutableList.of());
    }

    @Test
    void twoTestsInOneGroup() throws Exception {
        input.onNext("[==========] Running 2 tests from 1 test case.");
        input.onNext("[----------] Global test environment set-up.");
        input.onNext("[----------] 2 tests from SomeGroup");
        input.onNext("[ RUN      ] SomeGroup.TestIsTrue");
        input.onNext("[       OK ] SomeGroup.TestIsTrue (0 ms)");
        input.onNext("[ RUN      ] SomeGroup.FailingTest");
        input.onNext("..\\..\\..\\test_samples\\main.cpp(12): error: Value of: 0 == 0");
        input.onNext("  Actual: true");
        input.onNext("Expected: false");
        input.onNext("[  FAILED  ] SomeGroup.FailingTest (1 ms)");
        input.onNext("[----------] 2 tests from SomeGroup (2 ms total)");
        input.onNext("");
        input.onNext("[----------] Global test environment tear-down");
        input.onNext("[==========] 2 tests from 1 test case ran. (5 ms total)");
        input.onNext("[  PASSED  ] 1 test.");
        input.onNext("[  FAILED  ] 1 test, listed below:");
        input.onNext("[  FAILED  ] SomeGroup.FailingTest");
        input.onNext("");
        input.onNext(" 1 FAILED TEST");
        input.onCompleted();

        assertEquals(output.getOnNextEvents(), ImmutableList.of(
                new SuiteStart("[==========] Running 2 tests from 1 test case.", 2, 1),
                new TestOutput("[----------] Global test environment set-up.", Optional.empty(), Optional.empty()),
                new GroupStart("[----------] 2 tests from SomeGroup", "SomeGroup", 2),
                new TestStart("[ RUN      ] SomeGroup.TestIsTrue", "SomeGroup", "TestIsTrue"),
                new TestPassed("[       OK ] SomeGroup.TestIsTrue (0 ms)", "SomeGroup", "TestIsTrue"),
                new TestStart("[ RUN      ] SomeGroup.FailingTest", "SomeGroup", "FailingTest"),
                new TestOutput("..\\..\\..\\test_samples\\main.cpp(12): error: Value of: 0 == 0", Optional.of("SomeGroup"), Optional.of("FailingTest")),
                new TestOutput("  Actual: true", Optional.of("SomeGroup"), Optional.of("FailingTest")),
                new TestOutput("Expected: false", Optional.of("SomeGroup"), Optional.of("FailingTest")),
                new TestFailed("[  FAILED  ] SomeGroup.FailingTest (1 ms)", "SomeGroup", "FailingTest"),
                new GroupEnd("[----------] 2 tests from SomeGroup (2 ms total)", "SomeGroup", 2),
                new TestOutput("", Optional.empty(), Optional.empty()),
                new TestOutput("[----------] Global test environment tear-down", Optional.empty(), Optional.empty()),
                new SuiteEnd("[==========] 2 tests from 1 test case ran. (5 ms total)", 2, 1),
                new PassedTestsSummary("[  PASSED  ] 1 test.", 1),
                new FailedTestsSummary("[  FAILED  ] 1 test, listed below:", 1),
                new FailedTestSummary("[  FAILED  ] SomeGroup.FailingTest", "SomeGroup", "FailingTest"),
                new SummaryOutput(""),
                new SummaryOutput(" 1 FAILED TEST")
        ));
        assertEquals(output.getOnCompletedEvents(), ImmutableList.of(Notification.createOnCompleted()));
        assertEquals(output.getOnErrorEvents(), ImmutableList.of());
    }

    @Test
    void twoGroupsWithOneTestEach() throws Exception {
        input.onNext("[==========] Running 2 tests from 2 test cases.");
        input.onNext("[----------] Global test environment set-up.");
        input.onNext("[----------] 1 test from SomeGroup");
        input.onNext("[ RUN      ] SomeGroup.TestIsTrue");
        input.onNext("[       OK ] SomeGroup.TestIsTrue (0 ms)");
        input.onNext("[----------] 1 test from SomeGroup (1 ms total)");
        input.onNext("");
        input.onNext("[----------] 1 test from OtherGroup");
        input.onNext("[ RUN      ] OtherGroup.ExpectTest");
        input.onNext("..\\..\\..\\test_samples\\main.cpp(17): error: Value of: 2");
        input.onNext("Expected: 1");
        input.onNext("..\\..\\..\\test_samples\\main.cpp(18): error: Value of: 3");
        input.onNext("Expected: 2");
        input.onNext("[  FAILED  ] OtherGroup.ExpectTest (12 ms)");
        input.onNext("[----------] 1 test from OtherGroup (13 ms total)");
        input.onNext("");
        input.onNext("[----------] Global test environment tear-down");
        input.onNext("[==========] 2 tests from 2 test cases ran. (15 ms total)");
        input.onNext("[  PASSED  ] 1 test.");
        input.onNext("[  FAILED  ] 1 test, listed below:");
        input.onNext("[  FAILED  ] OtherGroup.ExpectTest");
        input.onNext("");
        input.onNext(" 1 FAILED TEST");
        input.onCompleted();

        assertEquals(output.getOnNextEvents(), ImmutableList.of(
                new SuiteStart("[==========] Running 2 tests from 2 test cases.", 2, 2),
                new TestOutput("[----------] Global test environment set-up.", Optional.empty(), Optional.empty()),
                new GroupStart("[----------] 1 test from SomeGroup", "SomeGroup", 1),
                new TestStart("[ RUN      ] SomeGroup.TestIsTrue", "SomeGroup", "TestIsTrue"),
                new TestPassed("[       OK ] SomeGroup.TestIsTrue (0 ms)", "SomeGroup", "TestIsTrue"),
                new GroupEnd("[----------] 1 test from SomeGroup (1 ms total)", "SomeGroup", 1),
                new TestOutput("", Optional.empty(), Optional.empty()),
                new GroupStart("[----------] 1 test from OtherGroup", "OtherGroup", 1),
                new TestStart("[ RUN      ] OtherGroup.ExpectTest", "OtherGroup", "ExpectTest"),
                new TestOutput("..\\..\\..\\test_samples\\main.cpp(17): error: Value of: 2", Optional.of("OtherGroup"), Optional.of("ExpectTest")),
                new TestOutput("Expected: 1", Optional.of("OtherGroup"), Optional.of("ExpectTest")),
                new TestOutput("..\\..\\..\\test_samples\\main.cpp(18): error: Value of: 3", Optional.of("OtherGroup"), Optional.of("ExpectTest")),
                new TestOutput("Expected: 2", Optional.of("OtherGroup"), Optional.of("ExpectTest")),
                new TestFailed("[  FAILED  ] OtherGroup.ExpectTest (12 ms)", "OtherGroup", "ExpectTest"),
                new GroupEnd("[----------] 1 test from OtherGroup (13 ms total)", "OtherGroup", 1),
                new TestOutput("", Optional.empty(), Optional.empty()),
                new TestOutput("[----------] Global test environment tear-down", Optional.empty(), Optional.empty()),
                new SuiteEnd("[==========] 2 tests from 2 test cases ran. (15 ms total)", 2, 2),
                new PassedTestsSummary("[  PASSED  ] 1 test.", 1),
                new FailedTestsSummary("[  FAILED  ] 1 test, listed below:", 1),
                new FailedTestSummary("[  FAILED  ] OtherGroup.ExpectTest", "OtherGroup", "ExpectTest"),
                new SummaryOutput(""),
                new SummaryOutput(" 1 FAILED TEST")
        ));
        assertEquals(output.getOnCompletedEvents(), ImmutableList.of(Notification.createOnCompleted()));
        assertEquals(output.getOnErrorEvents(), ImmutableList.of());
    }

    @Test
    void elapsedTimeTurnedOff() throws Exception {
        input.onNext("[==========] Running 2 tests from 2 test cases.");
        input.onNext("[----------] Global test environment set-up.");
        input.onNext("[----------] 1 test from SomeGroup");
        input.onNext("[ RUN      ] SomeGroup.TestIsTrue");
        input.onNext("[       OK ] SomeGroup.TestIsTrue");
        input.onNext("[----------] 1 test from OtherGroup");
        input.onNext("[ RUN      ] OtherGroup.ExpectTest");
        input.onNext("..\\..\\test_samples\\main.cpp(19): error: Value of: 2");
        input.onNext("Expected: 1");
        input.onNext("..\\..\\test_samples\\main.cpp(20): error: Value of: 3");
        input.onNext("Expected: 2");
        input.onNext("[  FAILED  ] OtherGroup.ExpectTest");
        input.onNext("[----------] Global test environment tear-down");
        input.onNext("[==========] 2 tests from 2 test cases ran.");
        input.onNext("[  PASSED  ] 1 test.");
        input.onNext("[  FAILED  ] 1 test, listed below:");
        input.onNext("[  FAILED  ] OtherGroup.ExpectTest");
        input.onNext("");
        input.onNext(" 1 FAILED TEST");
        input.onCompleted();

        assertEquals(output.getOnNextEvents(), ImmutableList.of(
                new SuiteStart("[==========] Running 2 tests from 2 test cases.", 2, 2),
                new TestOutput("[----------] Global test environment set-up.", Optional.empty(), Optional.empty()),
                new GroupStart("[----------] 1 test from SomeGroup", "SomeGroup", 1),
                new TestStart("[ RUN      ] SomeGroup.TestIsTrue", "SomeGroup", "TestIsTrue"),
                new TestPassed("[       OK ] SomeGroup.TestIsTrue", "SomeGroup", "TestIsTrue"),
                new GroupEnd(null, "SomeGroup", 1),
                new GroupStart("[----------] 1 test from OtherGroup", "OtherGroup", 1),
                new TestStart("[ RUN      ] OtherGroup.ExpectTest", "OtherGroup", "ExpectTest"),
                new TestOutput("..\\..\\test_samples\\main.cpp(19): error: Value of: 2", Optional.of("OtherGroup"), Optional.of("ExpectTest")),
                new TestOutput("Expected: 1", Optional.of("OtherGroup"), Optional.of("ExpectTest")),
                new TestOutput("..\\..\\test_samples\\main.cpp(20): error: Value of: 3", Optional.of("OtherGroup"), Optional.of("ExpectTest")),
                new TestOutput("Expected: 2", Optional.of("OtherGroup"), Optional.of("ExpectTest")),
                new TestFailed("[  FAILED  ] OtherGroup.ExpectTest", "OtherGroup", "ExpectTest"),
                new GroupEnd(null, "OtherGroup", 1),
                new TestOutput("[----------] Global test environment tear-down", Optional.empty(), Optional.empty()),
                new SuiteEnd("[==========] 2 tests from 2 test cases ran.", 2, 2),
                new PassedTestsSummary("[  PASSED  ] 1 test.", 1),
                new FailedTestsSummary("[  FAILED  ] 1 test, listed below:", 1),
                new FailedTestSummary("[  FAILED  ] OtherGroup.ExpectTest", "OtherGroup", "ExpectTest"),
                new SummaryOutput(""),
                new SummaryOutput(" 1 FAILED TEST")
       ));
        assertEquals(output.getOnCompletedEvents(), ImmutableList.of(Notification.createOnCompleted()));
        assertEquals(output.getOnErrorEvents(), ImmutableList.of());
    }
}