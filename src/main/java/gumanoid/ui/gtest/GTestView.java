package gumanoid.ui.gtest;

import com.google.common.annotations.VisibleForTesting;
import gumanoid.runner.GTestRunner;
import gumanoid.ui.gtest.output.GTestOutputView;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

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

//        setupActions(testExePath);
//
//        SwingUtilities.invokeLater(runTests::doClick);
    }

    public JButton getRunTests() {
        return runTests;
    }

    public JButton getCancelTests() {
        return cancelTests;
    }

    public JButton getRerunFailedTests() {
        return rerunFailedTests;
    }

    public JProgressBar getTestsProgress() {
        return testsProgress;
    }

    public GTestOutputView getTestOutputView() {
        return testOutputView;
    }

//    private void setupActions(String testExePath) {
//        runTests.addActionListener(e -> start(new Runner(testExePath)));
//
//        cancelTests.addActionListener(e -> {
//            if (currentTask != null) {
//                currentTask.cancel(false);
//                finish(null);
//            }
//        });
//
//        rerunFailedTests.addActionListener(e -> start(new Runner(testExePath, failedTests)));
//    }
//
//    private void start(GTestRunner testRunner) {
//        currentTask = testRunner;
//
//        testOutputView.clear();
//
//        runTests.setEnabled(false);
//        rerunFailedTests.setEnabled(false);
//        cancelTests.setEnabled(true);
//
//        testsProgress.setValue(0);
//
//        currentTask.execute();
//    }
//
//    private void finish(Collection<String> failedTests) {
//        currentTask = null;
//
//        cancelTests.setEnabled(false);
//        runTests.setEnabled(true);
//
//        if (failedTests != null) {
//            this.failedTests = failedTests;
//        }
//        rerunFailedTests.setEnabled(!this.failedTests.isEmpty());
//    }
//
//    private class Runner extends GTestRunner {
//        public Runner(String testExePath, Collection<String> failedTests) {
//            super(testExePath, failedTests, createHandler());
//        }
//
//        public Runner(String testExePath) {
//            super(testExePath, createHandler());
//        }
//
//        @Override
//        protected void onProgress(SuiteProgress progress) {
//            //todo two-section progress bar, to display failed/passed ratio
//
//            testsProgress.setMaximum(progress.totalTests);
//            testsProgress.setValue(progress.finishedTests);
//        }
//
//        @Override
//        protected void onFinish(SuiteResult result) {
//            finish(result.failedTests);
//
//            GTestOutputView.Item exitCodeNode = testOutputView.atRoot()
//                    .createOutputLine("Test finished with exit code " + result.exitCode);
//
//            if (result.exitCode != 0) {
//                exitCodeNode.setTextColor(Color.RED);
//            }
//        }
//    }
}
