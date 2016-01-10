package gumanoid.runner;

import gumanoid.parser.GTestOutputParser;

import java.util.Optional;

import static javax.swing.SwingUtilities.invokeLater;

/**
 * GTest output parser listener whose only purpose is to transfer
 * event processing into Swing Event handler thread
 *
 * Created by Gumanoid on 10.01.2016.
 */
class InvokeLaterProxyListener implements GTestOutputParser.EventListener {
    private final GTestOutputParser.EventListener actualListener;

    public InvokeLaterProxyListener(GTestOutputParser.EventListener actualListener) {
        this.actualListener = actualListener;
    }

    @Override
    public void outputBeforeSuiteStarted(String outputLine) {
        invokeLater(() -> actualListener.outputBeforeSuiteStarted(outputLine));
    }

    @Override
    public void suiteStart(int testCount, int testGroupCount) {
        invokeLater(() -> actualListener.suiteStart(testCount, testGroupCount));
    }

    @Override
    public void suiteEnd(int testCount, int testGroupCount) {
        invokeLater(() -> actualListener.suiteEnd(testCount, testGroupCount));
    }

    @Override
    public void groupStart(String groupName, int testsInGroup) {
        invokeLater(() -> actualListener.groupStart(groupName, testsInGroup));
    }

    @Override
    public void groupEnd(String groupName, int testsInGroup) {
        invokeLater(() -> actualListener.groupEnd(groupName, testsInGroup));
    }

    @Override
    public void testStart(String groupName, String testName) {
        invokeLater(() -> actualListener.testStart(groupName, testName));
    }

    @Override
    public void testOutput(Optional<String> groupName, Optional<String> testName, String outputLine) {
        invokeLater(() -> actualListener.testOutput(groupName, testName, outputLine));
    }

    @Override
    public void testPassed(String groupName, String testName) {
        invokeLater(() -> actualListener.testPassed(groupName, testName));
    }

    @Override
    public void testFailed(String groupName, String testName) {
        invokeLater(() -> actualListener.testFailed(groupName, testName));
    }

    @Override
    public void passedTestsSummary(int passedTestCount) {
        invokeLater(() -> actualListener.passedTestsSummary(passedTestCount));
    }

    @Override
    public void failedTestsSummary(int failedTestCount) {
        invokeLater(() -> actualListener.failedTestsSummary(failedTestCount));
    }

    @Override
    public void failedTestSummary(String groupName, String failedTest) {
        invokeLater(() -> actualListener.failedTestSummary(groupName, failedTest));
    }

    @Override
    public void summaryOutput(String outputLine) {
        invokeLater(() -> actualListener.summaryOutput(outputLine));
    }
}
