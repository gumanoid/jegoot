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

        runTests.addActionListener(e -> start(new GTestRunner(testExePath, createHandler()) {
            @Override
            protected void onProgress(SuiteProgress progress) {
                //todo two-section progress bar, to display failed/passed ratio

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

        rerunFailedTests.addActionListener(e -> start(new GTestRunner(testExePath, failedTests, createHandler()) {
            @Override
            protected void onProgress(SuiteProgress progress) {
                //todo two-section progress bar, to display failed/passed ratio

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

    private GTestOutputParser.EventListener createHandler() {
        return new GTestOutputParser.EventListener() {
            boolean failsInSuite = false;
            boolean failsInGroup = false;

            @Override
            public void outputBeforeSuiteStarted(String outputLine) {
                testOutputView.atRoot().addOutputLine(outputLine);
            }

            @Override
            public void suiteStart(int testCount, int testGroupCount) {
                testOutputView.atRoot()
                        .addCollapsible("suite", "Suite")
                        .setTextColor(Color.YELLOW);
            }

            @Override
            public void suiteEnd(int testCount, int testGroupCount) {
                testOutputView.atRoot()
                        .addCollapsible("summary", "Summary")
                        .setTextColor(failsInSuite? Color.RED : Color.GREEN);

                if (!failsInSuite) {
                    testOutputView.at("suite").setTextColor(Color.GREEN);
                }
            }

            @Override
            public void groupStart(String groupName, int testsInGroup) {
                failsInGroup = false;
                testOutputView.at("suite").addCollapsible(groupName, groupName + " with " + testsInGroup + " test(s)");
            }

            @Override
            public void groupEnd(String groupName, int testsInGroup) {
                if (!failsInGroup) {
                    testOutputView.at("suite", groupName).setTextColor(Color.GREEN);
                }
            }

            @Override
            public void testStart(String groupName, String testName) {
                testOutputView.at("suite", groupName).addCollapsible(testName, testName);
            }

            @Override
            public void testOutput(Optional<String> groupName, Optional<String> testName, String outputLine) {
                if (groupName.isPresent()) {
                    if (testName.isPresent()) {
                        testOutputView.at("suite", groupName.get(), testName.get()).addOutputLine(outputLine);
                    } else {
                        testOutputView.at("suite", groupName.get()).addOutputLine(outputLine);
                    }
                } else {
                    testOutputView.at("suite").addOutputLine(outputLine);
                }
            }

            @Override
            public void testPassed(String groupName, String testName) {
                testOutputView.at("suite", groupName, testName).setTextColor(Color.GREEN);
            }

            @Override
            public void testFailed(String groupName, String testName) {
                failsInGroup = true;
                failsInSuite = true;

                testOutputView.at("suite").setTextColor(Color.RED);
                testOutputView.at("suite", groupName).setTextColor(Color.RED);
                testOutputView.at("suite", groupName, testName).setTextColor(Color.RED);
            }

            @Override
            public void passedTestsSummary(int passedTestCount) {
                testOutputView.at("summary").addOutputLine("Passed test(s): " + passedTestCount);
            }

            @Override
            public void failedTestsSummary(int failedTestCount) {
                testOutputView.at("summary").addOutputLine("Failed test(s): " + failedTestCount);
            }

            @Override
            public void failedTestSummary(String groupName, String failedTest) {
                testOutputView.at("summary").addOutputLine("Failed test: " + failedTest + " in group " + groupName);
            }

            @Override
            public void summaryOutput(String outputLine) {
                testOutputView.at("summary").addOutputLine(outputLine);
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
}
