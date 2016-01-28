package gumanoid.event;

import com.google.common.base.Objects;
import gumanoid.parser.GTestOutputParser;

import java.util.Optional;

/**
 * Events parsed from GTest output lines
 *
 * Created by Gumanoid on 16.01.2016.
 */
public class GTestOutputEvent {
    /**
     * Raw GTest output line from which this event was constructed
     */
    public final String outputLine;

    public GTestOutputEvent(String outputLine) {
        this.outputLine = outputLine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GTestOutputEvent that = (GTestOutputEvent) o;
        return Objects.equal(outputLine, that.outputLine);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(outputLine);
    }

    @Override
    public String toString() {
        return "GTestOutputEvent{" +
                "outputLine='" + outputLine + '\'' +
                '}';
    }

    public static class TestOutput extends GTestOutputEvent {
        public final Optional<String> groupName;
        public final Optional<String> testName;

        /**
         * Any non-special output which was printed between 'suite start' and
         * 'suite end' events
         *
         * @param outputLine raw output line
         * @param groupName test group name, if any; may be empty if output line
         *                  relates to test suite, or if GTest outputs empty line
         *                  between test groups
         * @param testName test name, if any; may be empty if output line relates
         *                 to test suite or test group
         */
        public TestOutput(String outputLine, Optional<String> groupName, Optional<String> testName) {
            super(outputLine);
            this.groupName = groupName;
            this.testName = testName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            TestOutput that = (TestOutput) o;
            return Objects.equal(groupName, that.groupName) &&
                    Objects.equal(testName, that.testName);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), groupName, testName);
        }

        @Override
        public String toString() {
            return "TestOutput{" +
                    "outputLine='" + outputLine + '\'' +
                    ", groupName=" + groupName +
                    ", testName=" + testName +
                    '}';
        }
    }

    public static class OutputBeforeSuiteStarted extends GTestOutputEvent {
        /**
         * Output line received before test suite is started (e. g.
         * GTest notion about command line options used)
         *
         * @param outputLine    raw output line
         */
        public OutputBeforeSuiteStarted(String outputLine) {
            super(outputLine);
        }

        @Override
        public String toString() {
            return "OutputBeforeSuiteStarted{" +
                    "outputLine='" + outputLine + '\'' +
                    '}';
        }
    }

    public static class SummaryOutput extends GTestOutputEvent {
        /**
         * Output line of summary block printed by GTest after all
         * tests in a suite are finished
         *
         * @param outputLine    raw output line
         */
        public SummaryOutput(String outputLine) {
            super(outputLine);
        }

        @Override
        public String toString() {
            return "SummaryOutput{" +
                    "outputLine='" + outputLine + '\'' +
                    '}';
        }
    }

    public static class SuiteStart extends GTestOutputEvent {
        public final int testCount;
        public final int testGroupCount;

        /**
         * GTest said that the suite with N tests in M test groups begins
         *
         * @param outputLine        raw output line
         * @param testCount         how many tests this suite contains
         * @param testGroupCount    how many test groups are in this suite
         */
        public SuiteStart(String outputLine, int testCount, int testGroupCount) {
            super(outputLine);
            this.testCount = testCount;
            this.testGroupCount = testGroupCount;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            SuiteStart that = (SuiteStart) o;
            return testCount == that.testCount &&
                    testGroupCount == that.testGroupCount;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), testCount, testGroupCount);
        }

        @Override
        public String toString() {
            return "SuiteStart{" +
                    "outputLine='" + outputLine + '\'' +
                    ", testCount=" + testCount +
                    ", testGroupCount=" + testGroupCount +
                    '}';
        }
    }

    public static class SuiteEnd extends GTestOutputEvent {
        public final int testCount;
        public final int testGroupCount;

        /**
         * GTest said that the suite with N tests in M test groups ends.
         *
         * @param outputLine        raw output line, may be null for parser-synthesized call
         * @param testCount         how many tests this suite contains
         * @param testGroupCount    how many test groups are in this suite
         */
        public SuiteEnd(String outputLine, int testCount, int testGroupCount) {
            super(outputLine);
            this.testCount = testCount;
            this.testGroupCount = testGroupCount;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            SuiteEnd suiteEnd = (SuiteEnd) o;
            return testCount == suiteEnd.testCount &&
                    testGroupCount == suiteEnd.testGroupCount;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), testCount, testGroupCount);
        }

        @Override
        public String toString() {
            return "SuiteEnd{" +
                    "outputLine='" + outputLine + '\'' +
                    ", testCount=" + testCount +
                    ", testGroupCount=" + testGroupCount +
                    '}';
        }
    }

    public static class GroupStart extends GTestOutputEvent {
        public final String groupName;
        public final int testsInGroup;

        /**
         * GTest said that the group with N tests begins
         *
         * @param outputLine        raw output line
         * @param groupName         name of test group being started
         * @param testsInGroup      how many test groups are in this group
         */
        public GroupStart(String outputLine, String groupName, int testsInGroup) {
            super(outputLine);
            this.groupName = groupName;
            this.testsInGroup = testsInGroup;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            GroupStart that = (GroupStart) o;
            return testsInGroup == that.testsInGroup &&
                    Objects.equal(groupName, that.groupName);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), groupName, testsInGroup);
        }

        @Override
        public String toString() {
            return "GroupStart{" +
                    "outputLine='" + outputLine + '\'' +
                    ", groupName='" + groupName + '\'' +
                    ", testsInGroup=" + testsInGroup +
                    '}';
        }
    }

    public static class GroupEnd extends GTestOutputEvent {
        public final String groupName;
        public final int testsInGroup;

        /**
         * GTest said that the group with N tests ends.
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
        public GroupEnd(String outputLine, String groupName, int testsInGroup) {
            super(outputLine);
            this.groupName = groupName;
            this.testsInGroup = testsInGroup;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            GroupEnd groupEnd = (GroupEnd) o;
            return testsInGroup == groupEnd.testsInGroup &&
                    Objects.equal(groupName, groupEnd.groupName);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), groupName, testsInGroup);
        }

        @Override
        public String toString() {
            return "GroupEnd{" +
                    "outputLine='" + outputLine + '\'' +
                    ", groupName='" + groupName + '\'' +
                    ", testsInGroup=" + testsInGroup +
                    '}';
        }
    }

    public static class TestStart extends GTestOutputEvent {
        public final String groupName;
        public final String testName;

        /**
         * GTest said that a test is started
         *
         * @param outputLine    raw output line
         * @param groupName     name of the group this test belongs to
         * @param testName      name of the test being started
         */
        public TestStart(String outputLine, String groupName, String testName) {
            super(outputLine);
            this.groupName = groupName;
            this.testName = testName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            TestStart testStart = (TestStart) o;
            return Objects.equal(groupName, testStart.groupName) &&
                    Objects.equal(testName, testStart.testName);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), groupName, testName);
        }

        @Override
        public String toString() {
            return "TestStart{" +
                    "outputLine='" + outputLine + '\'' +
                    ", groupName='" + groupName + '\'' +
                    ", testName='" + testName + '\'' +
                    '}';
        }
    }

    public static class TestPassed extends GTestOutputEvent {
        public final String groupName;
        public final String testName;

        /**
         * GTest said that a test has finished successfully
         *
         * @param outputLine    raw output line
         * @param groupName     name of the group passed test belongs to
         * @param testName      name of the test that has passed
         */
        public TestPassed(String outputLine, String groupName, String testName) {
            super(outputLine);
            this.groupName = groupName;
            this.testName = testName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            TestPassed that = (TestPassed) o;
            return Objects.equal(groupName, that.groupName) &&
                    Objects.equal(testName, that.testName);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), groupName, testName);
        }

        @Override
        public String toString() {
            return "TestPassed{" +
                    "outputLine='" + outputLine + '\'' +
                    ", groupName='" + groupName + '\'' +
                    ", testName='" + testName + '\'' +
                    '}';
        }
    }

    public static class TestFailed extends GTestOutputEvent {
        public final String groupName;
        public final String testName;

        /**
         * GTest said that some test has finished with error
         *
         * @param outputLine    raw output line
         * @param groupName     name of the group failed test belongs to
         * @param testName      name of the test that has failed
         */
        public TestFailed(String outputLine, String groupName, String testName) {
            super(outputLine);
            this.groupName = groupName;
            this.testName = testName;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            TestFailed that = (TestFailed) o;
            return Objects.equal(groupName, that.groupName) &&
                    Objects.equal(testName, that.testName);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), groupName, testName);
        }

        @Override
        public String toString() {
            return "TestFailed{" +
                    "outputLine='" + outputLine + '\'' +
                    ", groupName='" + groupName + '\'' +
                    ", testName='" + testName + '\'' +
                    '}';
        }
    }

    public static class PassedTestsSummary extends GTestOutputEvent {
        public final int passedTestCount;

        /**
         * GTest prints number of passed tests in summary block
         * (after suite has finished). Number of passed tests is printed even
         * if there is no single passed test, i. e. <code>passedTestCount</code>
         * can be zero
         *
         * @param outputLine         raw output line
         * @param passedTestCount    how many tests have passed (may be zero)
         */
        public PassedTestsSummary(String outputLine, int passedTestCount) {
            super(outputLine);
            this.passedTestCount = passedTestCount;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            PassedTestsSummary that = (PassedTestsSummary) o;
            return passedTestCount == that.passedTestCount;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), passedTestCount);
        }

        @Override
        public String toString() {
            return "PassedTestsSummary{" +
                    "outputLine='" + outputLine + '\'' +
                    ", passedTestCount=" + passedTestCount +
                    '}';
        }
    }

    public static class FailedTestsSummary extends GTestOutputEvent {
        public final int failedTestCount;

        /**
         * GTest prints number of passed tests in summary block
         * (after suite has finished). This method may not be called if there were
         * no failed tests (e. g. all tests have passed or suite is empty)
         *
         * @param outputLine         raw output line
         * @param failedTestCount    how many tests have failed
         */
        public FailedTestsSummary(String outputLine, int failedTestCount) {
            super(outputLine);
            this.failedTestCount = failedTestCount;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            FailedTestsSummary that = (FailedTestsSummary) o;
            return failedTestCount == that.failedTestCount;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), failedTestCount);
        }

        @Override
        public String toString() {
            return "FailedTestsSummary{" +
                    "outputLine='" + outputLine + '\'' +
                    ", failedTestCount=" + failedTestCount +
                    '}';
        }
    }

    public static class FailedTestSummary extends GTestOutputEvent {
        public final String groupName;
        public final String failedTest;

        /**
         * GTest enumerates failed tests in summary block. This method
         * may not be called if there were no failed tests (e. g. all tests have
         * passed or suite is empty)
         *
         * @param outputLine    raw output line
         * @param groupName     name of the group failed test belongs to
         * @param failedTest    name of the test that has failed
         */
        public FailedTestSummary(String outputLine, String groupName, String failedTest) {
            super(outputLine);
            this.groupName = groupName;
            this.failedTest = failedTest;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            FailedTestSummary that = (FailedTestSummary) o;
            return Objects.equal(groupName, that.groupName) &&
                    Objects.equal(failedTest, that.failedTest);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), groupName, failedTest);
        }

        @Override
        public String toString() {
            return "FailedTestSummary{" +
                    "outputLine='" + outputLine + '\'' +
                    ", groupName='" + groupName + '\'' +
                    ", failedTest='" + failedTest + '\'' +
                    '}';
        }
    }
}
