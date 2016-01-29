package gumanoid.ui.gtest;

import gumanoid.ui.gtest.output.GTestOutputView;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JTreeFixture;
import org.testng.annotations.*;
import sun.awt.OSInfo;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Paths;

import static java.lang.Thread.sleep;
import static org.testng.Assert.fail;

/**
 * Created by Gumanoid on 13.01.2016.
 */
@Test
public class GTestViewIT {
    File testSamplesDir = new File(Paths.get("").toAbsolutePath().toFile(), "test_samples");

    FrameFixture window;
    JFrame ui;
    GTestView testView;

    @BeforeClass
    void initFrameFixture() {
        ui = new JFrame("Title");

        window = new FrameFixture(ui);
        window.show();
    }

    @AfterMethod
    void clearOutputView() {
        ui.getContentPane().remove(testView);
    }

    @AfterClass
    void cleanUpFrameFixture() {
        window.cleanUp();
    }

    String exeName(String name) {
        return OSInfo.getOSType() == OSInfo.OSType.WINDOWS ? name + ".exe" : name;
    }

    @DataProvider(name = "testSamplesOutput")
    Object[][] testSamplesOutput() {
        return new Object[][] {
                {
                        exeName("empty_suite"),
                        new String[] {
                                "Suite",
                                "[==========] Running 0 tests from 0 test cases.",
                                "[==========] 0 tests from 0 test cases ran.",
                                "Summary",
                                "[  PASSED  ] 0 tests."
                        }
                },
                {
                        exeName("single_passing"),
                        new String[] {
                                "Suite",
                                "[==========] Running 1 test from 1 test case.",
                                "[----------] Global test environment set-up.",
                                "SomeGroup with 1 test(s)",
                                "[----------] 1 test from SomeGroup",
                                "TestIsTrue",
                                "[ RUN      ] SomeGroup.TestIsTrue",
                                "[       OK ] SomeGroup.TestIsTrue",
                                "[----------] 1 test from SomeGroup",
                                "",
                                "[----------] Global test environment tear-down",
                                "[==========] 1 test from 1 test case ran.",
                                "Summary",
                                "[  PASSED  ] 1 test."
                        }
                },
                {
                        exeName("single_failing"),
                        new String[] {
                                "Suite",
                                "[==========] Running 1 test from 1 test case.",
                                "[----------] Global test environment set-up.",
                                "SomeGroup with 1 test(s)",
                                "[----------] 1 test from SomeGroup",
                                "FailingTest",
                                "[ RUN      ] SomeGroup.FailingTest",
                                "..\\..\\test_samples\\main.cpp(14): error: Value of: 0 == 0",
                                "  Actual: true",
                                "Expected: false",
                                "[  FAILED  ] SomeGroup.FailingTest",
                                "[----------] 1 test from SomeGroup",
                                "",
                                "[----------] Global test environment tear-down",
                                "[==========] 1 test from 1 test case ran.",
                                "Summary",
                                "[  PASSED  ] 0 tests.",
                                "[  FAILED  ] 1 test, listed below:",
                                "[  FAILED  ] SomeGroup.FailingTest",
                                "",
                                " 1 FAILED TEST"
                        }
                },
                {
                        exeName("two_tests_in_one_group"),
                        new String[] {
                                "Suite",
                                "[==========] Running 2 tests from 1 test case.",
                                "[----------] Global test environment set-up.",
                                "SomeGroup with 2 test(s)",
                                "[----------] 2 tests from SomeGroup",
                                "TestIsTrue",
                                "[ RUN      ] SomeGroup.TestIsTrue",
                                "[       OK ] SomeGroup.TestIsTrue",
                                "FailingTest",
                                "[ RUN      ] SomeGroup.FailingTest",
                                "..\\..\\test_samples\\main.cpp(14): error: Value of: 0 == 0",
                                "  Actual: true",
                                "Expected: false",
                                "[  FAILED  ] SomeGroup.FailingTest",
                                "[----------] 2 tests from SomeGroup",
                                "",
                                "[----------] Global test environment tear-down",
                                "[==========] 2 tests from 1 test case ran.",
                                "Summary",
                                "[  PASSED  ] 1 test.",
                                "[  FAILED  ] 1 test, listed below:",
                                "[  FAILED  ] SomeGroup.FailingTest",
                                "",
                                " 1 FAILED TEST",
                        }
                },
                {
                        exeName("two_groups_with_one_test_each"),
                        new String[] {
                                "Suite",
                                "[==========] Running 2 tests from 2 test cases.",
                                "[----------] Global test environment set-up.",
                                "SomeGroup with 1 test(s)",
                                "[----------] 1 test from SomeGroup",
                                "TestIsTrue",
                                "[ RUN      ] SomeGroup.TestIsTrue",
                                "[       OK ] SomeGroup.TestIsTrue",
                                "[----------] 1 test from SomeGroup",
                                "",
                                "OtherGroup with 1 test(s)",
                                "[----------] 1 test from OtherGroup",
                                "ExpectTest",
                                "[ RUN      ] OtherGroup.ExpectTest",
                                "..\\..\\test_samples\\main.cpp(19): error: Value of: 2",
                                "Expected: 1",
                                "..\\..\\test_samples\\main.cpp(20): error: Value of: 3",
                                "Expected: 2",
                                "[  FAILED  ] OtherGroup.ExpectTest",
                                "[----------] 1 test from OtherGroup",
                                "",
                                "[----------] Global test environment tear-down",
                                "[==========] 2 tests from 2 test cases ran.",
                                "Summary",
                                "[  PASSED  ] 1 test.",
                                "[  FAILED  ] 1 test, listed below:",
                                "[  FAILED  ] OtherGroup.ExpectTest",
                                "",
                                " 1 FAILED TEST"
                        }
                },
                {
                        exeName("elapsed_time_off"),
                        new String[] {
                                "Suite",
                                "[==========] Running 2 tests from 2 test cases.",
                                "[----------] Global test environment set-up.",
                                "SomeGroup with 1 test(s)",
                                "[----------] 1 test from SomeGroup",
                                "TestIsTrue",
                                "[ RUN      ] SomeGroup.TestIsTrue",
                                "[       OK ] SomeGroup.TestIsTrue",
                                "OtherGroup with 1 test(s)",
                                "[----------] 1 test from OtherGroup",
                                "ExpectTest",
                                "[ RUN      ] OtherGroup.ExpectTest",
                                "..\\..\\test_samples\\main.cpp(19): error: Value of: 2",
                                "Expected: 1",
                                "..\\..\\test_samples\\main.cpp(20): error: Value of: 3",
                                "Expected: 2",
                                "[  FAILED  ] OtherGroup.ExpectTest",
                                "[----------] Global test environment tear-down",
                                "[==========] 2 tests from 2 test cases ran.",
                                "Summary",
                                "[  PASSED  ] 1 test.",
                                "[  FAILED  ] 1 test, listed below:",
                                "[  FAILED  ] OtherGroup.ExpectTest",
                                "",
                                " 1 FAILED TEST"
                        }
                }
        };
    }

    @Test(dataProvider = "testSamplesOutput")
    void checkTestSamplesOutput(String testExeName, String[] expectedOutput) throws Exception {
        String testExePath = new File(testSamplesDir, testExeName).getAbsolutePath();

        ui.getContentPane().add(testView = new GTestView(), BorderLayout.CENTER);
        JTreeFixture tree = window.tree(GTestOutputView.TREE_NAME);
        new GTestViewController(testView, testExePath).runAllTests(); //todo it's UI test, it should press buttons, not call controller methods

        sleep(500);

        for (int i = 0; i < expectedOutput.length; i++) {
            String actual = tree.valueAt(i);
            String expected = expectedOutput[i];

            if (!actual.startsWith(expected)) {
                //equality check is impossible because of elapsed time output
                fail("Discrepancy at row #" + i + "\nExpected: " + expected + "\nActual  : " + actual + "\n");
            }
        }
    }
}