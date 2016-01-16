package gumanoid.ui.gtest;

import com.google.common.annotations.VisibleForTesting;
import gumanoid.parser.ClassifiedGTestOutputHandler;
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

        runTests.setName(RUN_BUTTON_NAME);
        cancelTests.setName(CANCEL_BUTTON_NAME);
        rerunFailedTests.setName(RERUN_BUTTON_NAME);

        cancelTests.setEnabled(false);
        rerunFailedTests.setEnabled(false);

        runTests.addActionListener(e -> start(new Runner(testExePath)));

        cancelTests.addActionListener(e -> {
            if (currentTask != null) {
                currentTask.cancel(false);
                finish(null);
            }
        });

        rerunFailedTests.addActionListener(e -> start(new Runner(testExePath, failedTests)));

        SwingUtilities.invokeLater(runTests::doClick);
    }

    private ClassifiedGTestOutputHandler createHandler() {
        return new ClassifiedGTestOutputHandler() {
            boolean failsInSuite = false;
            boolean failsInGroup = false;

            @Override
            public void outputBeforeSuiteStarted(String outputLine) {
                testOutputView.atRoot().createOutputLine(outputLine);
            }

            @Override
            public void suiteStart(String outputLine, int testCount, int testGroupCount) {
                GTestOutputView.Item suiteNode = testOutputView.atRoot().createCollapsible("suite", "Suite");
                suiteNode.setTextColor(Color.YELLOW);

                testOutputView.at("suite").createOutputLine(outputLine);
            }

            @Override
            public void suiteEnd(String outputLine, int testCount, int testGroupCount) {
                testOutputView.at("suite").createOutputLine(outputLine);

                if (!failsInSuite) {
                    testOutputView.at("suite").setTextColor(Color.GREEN);
                }

                GTestOutputView.Item summaryNode = testOutputView.atRoot().createCollapsible("summary", "Summary");
                summaryNode.setTextColor(failsInSuite? Color.RED : Color.GREEN);
            }

            @Override
            public void groupStart(String outputLine, String groupName, int testsInGroup) {
                failsInGroup = false;
                GTestOutputView.Item groupNode = testOutputView.at("suite")
                        .createCollapsible(groupName, groupName + " with " + testsInGroup + " test(s)");
                groupNode.createOutputLine(outputLine);
            }

            @Override
            public void groupEnd(String outputLine, String groupName, int testsInGroup) {
                GTestOutputView.Item groupNode = testOutputView.at("suite", groupName);

                if (!failsInGroup) {
                    groupNode.setTextColor(Color.GREEN);
                }

                if (outputLine != null) { //todo Optional instead of nullable, for consistency?
                    groupNode.createOutputLine(outputLine);
                }
            }

            @Override
            public void testStart(String outputLine, String groupName, String testName) {
                GTestOutputView.Item groupNode = testOutputView.at("suite", groupName);
                GTestOutputView.Item testNode = groupNode.createCollapsible(testName, testName);
                testNode.createOutputLine(outputLine);
            }

            @Override
            public void testOutput(String outputLine, Optional<String> groupName, Optional<String> testName) {
                (
                        groupName.isPresent() ? testName.isPresent()
                                ? testOutputView.at("suite", groupName.get(), testName.get())
                                : testOutputView.at("suite", groupName.get())
                                : testOutputView.at("suite")
                ).createOutputLine(outputLine);
            }

            @Override
            public void testPassed(String outputLine, String groupName, String testName) {
                GTestOutputView.Item testNode = testOutputView.at("suite", groupName, testName);
                testNode.setTextColor(Color.GREEN);
                testNode.createOutputLine(outputLine);
            }

            @Override
            public void testFailed(String outputLine, String groupName, String testName) {
                failsInGroup = true;
                failsInSuite = true;

                testOutputView.at("suite").setTextColor(Color.RED);
                testOutputView.at("suite", groupName).setTextColor(Color.RED);
                GTestOutputView.Item testNode = testOutputView.at("suite", groupName, testName);
                testNode.setTextColor(Color.RED);
                testNode.createOutputLine(outputLine);
            }

            @Override
            public void summaryOutput(String outputLine) {
                testOutputView.at("summary").createOutputLine(outputLine);
            }
        };
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

    private class Runner extends GTestRunner {
        public Runner(String testExePath, Collection<String> failedTests) {
            super(testExePath, failedTests, createHandler());
        }

        public Runner(String testExePath) {
            super(testExePath, createHandler());
        }

        @Override
        protected void onProgress(SuiteProgress progress) {
            //todo two-section progress bar, to display failed/passed ratio

            testsProgress.setMaximum(progress.totalTests);
            testsProgress.setValue(progress.finishedTests);
        }

        @Override
        protected void onFinish(SuiteResult result) {
            finish(result.failedTests);

            GTestOutputView.Item exitCodeNode = testOutputView.atRoot()
                    .createOutputLine("Test finished with exit code " + result.exitCode);

            if (result.exitCode != 0) {
                exitCodeNode.setTextColor(Color.RED);
            }
        }
    }
}
