package gumanoid.runner;

import gumanoid.parser.ClassifiedGTestOutputHandler;

import java.util.Optional;

import static javax.swing.SwingUtilities.invokeLater;

/**
 * GTest output handler whose only purpose is to transfer
 * event processing into Swing Event Dispatcher thread
 *
 * Created by Gumanoid on 10.01.2016.
 */
class InvokeLaterProxyHandler implements ClassifiedGTestOutputHandler {
    private final ClassifiedGTestOutputHandler actualListener;

    public InvokeLaterProxyHandler(ClassifiedGTestOutputHandler actualListener) {
        this.actualListener = actualListener;
    }

    @Override
    public void outputBeforeSuiteStarted(String outputLine) {
        invokeLater(() -> actualListener.outputBeforeSuiteStarted(outputLine));
    }

    @Override
    public void suiteStart(String outputLine, int testCount, int testGroupCount) {
        invokeLater(() -> actualListener.suiteStart(outputLine, testCount, testGroupCount));
    }

    @Override
    public void suiteEnd(String outputLine, int testCount, int testGroupCount) {
        invokeLater(() -> actualListener.suiteEnd(outputLine, testCount, testGroupCount));
    }

    @Override
    public void groupStart(String outputLine, String groupName, int testsInGroup) {
        invokeLater(() -> actualListener.groupStart(outputLine, groupName, testsInGroup));
    }

    @Override
    public void groupEnd(String outputLine, String groupName, int testsInGroup) {
        invokeLater(() -> actualListener.groupEnd(outputLine, groupName, testsInGroup));
    }

    @Override
    public void testStart(String outputLine, String groupName, String testName) {
        invokeLater(() -> actualListener.testStart(outputLine, groupName, testName));
    }

    @Override
    public void testOutput(String outputLine, Optional<String> groupName, Optional<String> testName) {
        invokeLater(() -> actualListener.testOutput(outputLine, groupName, testName));
    }

    @Override
    public void testPassed(String outputLine, String groupName, String testName) {
        invokeLater(() -> actualListener.testPassed(outputLine, groupName, testName));
    }

    @Override
    public void testFailed(String outputLine, String groupName, String testName) {
        invokeLater(() -> actualListener.testFailed(outputLine, groupName, testName));
    }

    @Override
    public void passedTestsSummary(String outputLine, int passedTestCount) {
        invokeLater(() -> actualListener.passedTestsSummary(outputLine, passedTestCount));
    }

    @Override
    public void failedTestsSummary(String outputLine, int failedTestCount) {
        invokeLater(() -> actualListener.failedTestsSummary(outputLine, failedTestCount));
    }

    @Override
    public void failedTestSummary(String outputLine, String groupName, String failedTest) {
        invokeLater(() -> actualListener.failedTestSummary(outputLine, groupName, failedTest));
    }

    @Override
    public void summaryOutput(String outputLine) {
        invokeLater(() -> actualListener.summaryOutput(outputLine));
    }
}
