package gumanoid.ui.gtest;

import com.google.common.annotations.VisibleForTesting;
import gumanoid.parser.ClassifiedGTestOutputHandler;
import gumanoid.runner.GTestEnumerator;
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
    @VisibleForTesting
    static final String RUN_BUTTON_NAME = "GTest_view_run";
    @VisibleForTesting
    static final String CANCEL_BUTTON_NAME = "GTest_view_cancel";
    @VisibleForTesting
    static final String RERUN_BUTTON_NAME = "GTest_view_rerun";

    private final JButton runTests = new JButton("Run");
    private final JButton cancelTests = new JButton("Cancel");
    private final JButton rerunFailedTests = new JButton("Re-run failed");

    private final JProgressBar testsProgress = new JProgressBar();

    private final GTestOutputView testOutputView = new GTestOutputView();

    private GTestRunner currentRunTask = null;
    private GTestEnumerator currentListTask = null;
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

        runTests.setName(RUN_BUTTON_NAME);
        cancelTests.setName(CANCEL_BUTTON_NAME);
        rerunFailedTests.setName(RERUN_BUTTON_NAME);

        cancelTests.setEnabled(false);
        rerunFailedTests.setEnabled(false);

        runTests.addActionListener(e -> start(
                new Enumerator(testExePath),
                new Runner(testExePath)
        ));

        cancelTests.addActionListener(e -> {
            if (currentListTask != null) {
                currentRunTask.cancel(false);
                currentListTask = null;
            }

            if (currentRunTask != null) {
                currentRunTask.cancel(false);
                currentRunTask = null;
                runnerFinished(null);
            }
        });

        rerunFailedTests.addActionListener(e -> start(
                new Enumerator(testExePath, failedTests),
                new Runner(testExePath, failedTests)
        ));

        SwingUtilities.invokeLater(runTests::doClick);
    }

    private ClassifiedGTestOutputHandler createHandler() {
        return new ClassifiedGTestOutputHandler() { //todo merge with Runner class
            boolean failsInSuite = false;
            boolean failsInGroup = false;

            @Override
            public void outputBeforeSuiteStarted(String outputLine) {
                testOutputView.atRoot().addOutputLine(outputLine);
            }

            @Override
            public void suiteStart(String outputLine, int testCount, int testGroupCount) {
                testOutputView.at("suite").setTextColor(Color.YELLOW);
                testOutputView.at("suite").addOutputLine(outputLine);
            }

            @Override
            public void suiteEnd(String outputLine, int testCount, int testGroupCount) {
                testOutputView.at("suite").addOutputLine(outputLine);

                if (!failsInSuite) {
                    testOutputView.at("suite").setTextColor(Color.GREEN);
                }

                testOutputView.atRoot()
                        .addCollapsible("summary", "Summary")
                        .setTextColor(failsInSuite ? Color.RED : Color.GREEN);
            }

            @Override
            public void groupStart(String outputLine, String groupName, int testsInGroup) {
                failsInGroup = false;
                testOutputView.at("suite").addCollapsible(groupName, groupName + " with " + testsInGroup + " test(s)");
                testOutputView.at("suite", groupName).addOutputLine(outputLine);
            }

            @Override
            public void groupEnd(String outputLine, String groupName, int testsInGroup) {
                if (!failsInGroup) {
                    testOutputView.at("suite", groupName).setTextColor(Color.GREEN);
                }

                if (outputLine != null) { //todo Optional instead of nullable, for consistency?
                    testOutputView.at("suite", groupName).addOutputLine(outputLine);
                }
            }

            @Override
            public void testStart(String outputLine, String groupName, String testName) {
                testOutputView.at("suite", groupName).addCollapsible(testName, testName);
                testOutputView.at("suite", groupName, testName).addOutputLine(outputLine);
            }

            @Override
            public void testOutput(String outputLine, Optional<String> groupName, Optional<String> testName) {
                (
                        groupName.isPresent() ? testName.isPresent()
                                ? testOutputView.at("suite", groupName.get(), testName.get())
                                : testOutputView.at("suite", groupName.get())
                                : testOutputView.at("suite")
                ).addOutputLine(outputLine);
            }

            @Override
            public void testPassed(String outputLine, String groupName, String testName) {
                testOutputView.at("suite", groupName, testName).setTextColor(Color.GREEN);
                testOutputView.at("suite", groupName, testName).addOutputLine(outputLine);
            }

            @Override
            public void testFailed(String outputLine, String groupName, String testName) {
                failsInGroup = true;
                failsInSuite = true;

                testOutputView.at("suite").setTextColor(Color.RED);
                testOutputView.at("suite", groupName).setTextColor(Color.RED);
                testOutputView.at("suite", groupName, testName).setTextColor(Color.RED);
                testOutputView.at("suite", groupName, testName).addOutputLine(outputLine);
            }

            @Override
            public void summaryOutput(String outputLine) {
                testOutputView.at("summary").addOutputLine(outputLine);
            }
        };
    }

    private void start(GTestEnumerator enumerator, GTestRunner testRunner) {
        currentRunTask = testRunner;
        currentListTask = enumerator;

        testOutputView.clear();
        testOutputView.atRoot().addCollapsible("suite", "Suite");

        runTests.setEnabled(false);
        rerunFailedTests.setEnabled(false);
        cancelTests.setEnabled(true);

        testsProgress.setValue(0);

        currentListTask.execute();
        currentRunTask.execute();
    }

    private void listerFinished() {
        currentListTask = null;

        updateButtons();
    }

    private void runnerFinished(Collection<String> failedTests) {
        currentRunTask = null;

        if (failedTests != null) {
            this.failedTests = failedTests;
        }

        updateButtons();
    }

    private void updateButtons() {
        boolean allowNewRun = currentRunTask == null && currentListTask == null;

        runTests.setEnabled(allowNewRun);
        cancelTests.setEnabled(!allowNewRun);

        rerunFailedTests.setEnabled(allowNewRun && !this.failedTests.isEmpty());
    }

    private class Enumerator extends GTestEnumerator {
        String currentTestGroup;

        public Enumerator(String testExePath) {
            super(testExePath);
        }

        public Enumerator(String testExePath, Collection<String> failedTests) {
            super(testExePath, failedTests);
        }

        @Override
        protected void onTestGroup(String testGroupName) {
            currentTestGroup = testGroupName;
            testOutputView.at("suite").addCollapsible(testGroupName);
        }

        @Override
        protected void onTest(String testName) {
            testOutputView.at("suite", currentTestGroup).addCollapsible(testName);
        }

        @Override
        protected void done() {
            listerFinished();
        }
    }

    private class Runner extends GTestRunner {
        public Runner(String testExePath) {
            super(testExePath, createHandler());
        }

        public Runner(String testExePath, Collection<String> failedTests) {
            super(testExePath, failedTests, createHandler());
        }

        @Override
        protected void onProgress(SuiteProgress progress) {
            //todo two-section progress bar, to display failed/passed ratio

            testsProgress.setMaximum(progress.totalTests);
            testsProgress.setValue(progress.finishedTests);
        }

        @Override
        protected void onFinish(SuiteResult result) {
            runnerFinished(result.failedTests);
        }
    }
}
