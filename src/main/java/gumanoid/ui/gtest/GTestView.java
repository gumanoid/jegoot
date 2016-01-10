package gumanoid.ui.gtest;

import gumanoid.parser.GTestOutputParser;
import gumanoid.runner.GTestRunner;
import gumanoid.ui.gtest.output.GTestOutputView;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Created by Gumanoid on 10.01.2016.
 */
public class GTestView extends JPanel { //todo this class needs more expressive name
    private final JButton runTests = new JButton("Run");
    private final JButton cancelTests = new JButton("Cancel");
    private final JButton rerunFailedTests = new JButton("Re-run failed");

    private final JProgressBar testsProgress = new JProgressBar();

    private final GTestOutputView testOutputView = new GTestOutputView();

    GTestOutputParser.EventListener outputListener = new GTestOutputParser.EventListener() {
        @Override
        public void outputBeforeSuiteStarted(String outputLine) {
            testOutputView.append(outputLine);
        }

        @Override
        public void suiteStart(int testCount, int testGroupCount) {
            testOutputView.appendCollapsible("Suite", "suite");
        }

        @Override
        public void suiteEnd(int testCount, int testGroupCount) {
            testOutputView.appendCollapsible("Summary", "summary");
        }

        @Override
        public void groupStart(String groupName, int testsInGroup) {
            testOutputView.appendCollapsible(groupName + " with " + testsInGroup + " test(s)", "suite", groupName);
        }

        @Override
        public void groupEnd(String groupName, int testsInGroup) {

        }

        @Override
        public void testStart(String groupName, String testName) {
            testOutputView.appendCollapsible(testName, "suite", groupName, testName);
        }

        @Override
        public void testOutput(Optional<String> groupName, Optional<String> testName, String outputLine) {
            if (groupName.isPresent()) {
                if (testName.isPresent()) {
                    testOutputView.append(outputLine, "suite", groupName.get(), testName.get());
                } else {
                    testOutputView.append(outputLine, "suite", groupName.get());
                }
            } else {
                testOutputView.append(outputLine, "suite");
            }
        }

        @Override
        public void testPassed(String groupName, String testName) {
            testOutputView.append("Passed", "suite", groupName, testName);
        }

        @Override
        public void testFailed(String groupName, String testName) {
            testOutputView.append("Failed", "suite", groupName, testName);
        }

        @Override
        public void passedTestsSummary(int passedTestCount) {
            testOutputView.append("Passed test(s): " + passedTestCount, "summary");
        }

        @Override
        public void failedTestsSummary(int failedTestCount) {
            testOutputView.append("Failed test(s): " + failedTestCount, "summary");
        }

        @Override
        public void failedTestSummary(String groupName, String failedTest) {
            testOutputView.append("Failed test: " + failedTest + " in group " + groupName, "summary");
        }

        @Override
        public void summaryOutput(String outputLine) {
            testOutputView.append(outputLine, "summary");
        }
    };

    private GTestRunner currentTask = null;
    private Collection<String> failedTests = Collections.emptyList();

    public GTestView(String testExePath) {
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.LINE_AXIS));
        controls.add(Box.createRigidArea(new Dimension(10, 0)));
        controls.add(runTests);
        controls.add(Box.createRigidArea(new Dimension(10, 0)));
        controls.add(rerunFailedTests);
        controls.add(Box.createRigidArea(new Dimension(10, 0)));
        controls.add(cancelTests);
        controls.add(Box.createRigidArea(new Dimension(10, 0)));
        controls.add(testsProgress);
        controls.add(Box.createRigidArea(new Dimension(10, 0)));

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(controls);
        add(testOutputView);

        cancelTests.setEnabled(false);
        rerunFailedTests.setEnabled(false);

        runTests.addActionListener(e -> start(new GTestRunner(testExePath, outputListener) {
            @Override
            protected void onProgress(SuiteProgress progress) {
                testsProgress.setMaximum(progress.totalTests);
                testsProgress.setValue(progress.finishedTests);
            }

            @Override
            protected void onFinish(SuiteResult result) {
                finish(result.failedTests);
            }
        }));

        cancelTests.addActionListener(e -> {
            if (currentTask != null) {
                currentTask.cancel(false);
                finish(null);
            }
        });

        rerunFailedTests.addActionListener(e -> start(new GTestRunner(testExePath, failedTests, outputListener) {
            @Override
            protected void onProgress(SuiteProgress progress) {
                testsProgress.setMaximum(progress.totalTests);
                testsProgress.setValue(progress.finishedTests);
            }

            @Override
            protected void onFinish(SuiteResult result) {
                finish(result.failedTests);
            }
        }));

        SwingUtilities.invokeLater(runTests::doClick);
    }

    private void start(GTestRunner testRunner) {
        currentTask = testRunner;

        testOutputView.clear();

        runTests.setEnabled(false);
        rerunFailedTests.setEnabled(false);
        cancelTests.setEnabled(true);

        testsProgress.setValue(0);

        currentTask.execute();
    }

    private void finish(Collection<String> failedTests) {
        currentTask = null;

        cancelTests.setEnabled(false);
        runTests.setEnabled(true);

        if (failedTests != null) {
            this.failedTests = failedTests;
        }
        rerunFailedTests.setEnabled(!this.failedTests.isEmpty());
    }
}
