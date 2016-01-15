package gumanoid.parser;

import java.util.Optional;

/**
 * Defines possible kinds of test output lines. Each kind has corresponding methods
 * (i.e. kinds and methods has one-to-one relationship).
 *
 * Implementation should define reactions for line kinds it interested in. Minimal
 * implementation would define only <code>testOutput</code> method. All other methods
 * are defaulting to invoke <code>testOutput</code>.
 *
 * Each method receives, aside from parameters parsed from output line, the output line
 * itself (as a first parameter of each method).
 *
 * Method-per-kind approach was chosen for it's implementation simplicity.
 * A class-per-kind approach is also possible but requires more infrastructure
 * (a bunch of dumb-simple classes, dispatching machinery, etc). It would facilitate
 * intermediate generalized processing (like shifting to Swing EDT), but there is only
 * a little need in such processing in this project.
 *
 * Created by Gumanoid on 13.01.2016.
 */
public interface ClassifiedGTestOutputHandler {
    /**
     * Most basic output kind, which all other kinds are defaulting to
     *
     * @param outputLine raw output line
     * @param groupName test group name, if any; may be empty if output line
     *                  relates to test suite, or if GTest outputs empty line
     *                  between test groups
     * @param testName test name, if any; may be empty if output line relates
     *                 to test suite or test group
     */
    void testOutput(String outputLine, Optional<String> groupName, Optional<String> testName);

    /**
     * Defines a reaction to line received before test suite is started (e. g.
     * GTest notion about command line options used)
     *
     * @param outputLine    raw output line
     */
    default void outputBeforeSuiteStarted(String outputLine) {
        testOutput(outputLine, Optional.empty(), Optional.empty());
    }

    /**
     * Defines a reaction to a line of summary block which is being
     * printed by GTest after all tests in suite are finished
     *
     * @param outputLine    raw output line
     */
    default void summaryOutput(String outputLine) {
        testOutput(outputLine, Optional.empty(), Optional.empty());
    }

    /**
     * Called when GTest says that the suite with N tests in M test groups begins
     *
     * @param outputLine        raw output line
     * @param testCount         how many tests this suite contains
     * @param testGroupCount    how many test groups are in this suite
     */
    default void suiteStart(String outputLine, int testCount, int testGroupCount) {
        testOutput(outputLine, Optional.empty(), Optional.empty());
    }

    /**
     * Called when GTest says that the suite with N tests in M test groups ends.
     *
     * @param outputLine        raw output line, may be null for parser-synthesized call
     * @param testCount         how many tests this suite contains
     * @param testGroupCount    how many test groups are in this suite
     */
    default void suiteEnd(String outputLine, int testCount, int testGroupCount) {
        testOutput(outputLine, Optional.empty(), Optional.empty());
    }

    /**
     * Called when GTest says that the group with N tests begins
     *
     * @param outputLine        raw output line
     * @param groupName         name of test group being started
     * @param testsInGroup      how many test groups are in this group
     */
    default void groupStart(String outputLine, String groupName, int testsInGroup) {
        testOutput(outputLine, Optional.of(groupName), Optional.empty());
    }

    /**
     * Called when GTest says that the group with N tests ends.
     *
     * Although GTest may not print notification about group ending (e. g. if
     * elapsed time measurement is switched of), {@link GTestOutputParser} makes
     * sure to call this method right before next group starts or any summary is
     * printed
     *
     * @param outputLine        raw output line, may be null for parser-synthesized call
     * @param groupName         name of test group being started
     * @param testsInGroup      how many test groups are in this group
     */
    default void groupEnd(String outputLine, String groupName, int testsInGroup) {
        if (outputLine != null) {
            testOutput(outputLine, Optional.of(groupName), Optional.empty());
        }
    }

    /**
     * Called when GTest says that some test is started
     *
     * @param outputLine    raw output line
     * @param groupName     name of the group this test belongs to
     * @param testName      name of the test being started
     */
    default void testStart(String outputLine, String groupName, String testName) {
        testOutput(outputLine, Optional.of(groupName), Optional.of(testName));
    }

    /**
     * Called when GTest says that some test has finished successfully
     *
     * @param outputLine    raw output line
     * @param groupName     name of the group passed test belongs to
     * @param testName      name of the test that has passed
     */
    default void testPassed(String outputLine, String groupName, String testName) {
        testOutput(outputLine, Optional.of(groupName), Optional.of(testName));
    }

    /**
     * Called when GTest says that some test has finished with error
     *
     * @param outputLine    raw output line
     * @param groupName     name of the group failed test belongs to
     * @param testName      name of the test that has failed
     */
    default void testFailed(String outputLine, String groupName, String testName) {
        testOutput(outputLine, Optional.of(groupName), Optional.of(testName));
    }

    /**
     * Called when GTest prints number of passed tests in summary block
     * (after suite has finished). Number of passed tests is printed even
     * if there is no single passed test, i. e. <code>passedTestCount</code>
     * can be zero
     *
     * @param outputLine         raw output line
     * @param passedTestCount    how many tests have passed (may be zero)
     */
    default void passedTestsSummary(String outputLine, int passedTestCount) {
        summaryOutput(outputLine);
    }

    /**
     * Called when GTest prints number of passed tests in summary block
     * (after suite has finished). This method may not be called if there were
     * no failed tests (e. g. all tests have passed or suite is empty)
     *
     * @param outputLine         raw output line
     * @param failedTestCount    how many tests have failed
     */
    default void failedTestsSummary(String outputLine, int failedTestCount) {
        summaryOutput(outputLine);
    }

    /**
     * Called when GTest enumerates failed tests in summary block. This method
     * may not be called if there were no failed tests (e. g. all tests have
     * passed or suite is empty)
     *
     * @param outputLine    raw output line
     * @param groupName     name of the group failed test belongs to
     * @param failedTest    name of the test that has failed
     */
    default void failedTestSummary(String outputLine, String groupName, String failedTest) {
        summaryOutput(outputLine);
    }
}
