package gumanoid.ui.gtest;

import com.google.common.annotations.VisibleForTesting;
import gumanoid.ui.DoubleProgressBar;
import gumanoid.ui.gtest.output.GTestOutputView;

import javax.swing.*;
import java.awt.*;

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

    static final Dimension SPACER_SIZE = new Dimension(10, 0);

    private final JButton runTests = new JButton("Run");
    private final JButton cancelTests = new JButton("Cancel");
    private final JButton rerunFailedTests = new JButton("Re-run failed");

    private final DoubleProgressBar testsProgress = new DoubleProgressBar();

    private final GTestOutputView testOutputView = new GTestOutputView();

    public GTestView() {
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.LINE_AXIS));
        controls.add(Box.createRigidArea(SPACER_SIZE));
        controls.add(runTests);
        controls.add(Box.createRigidArea(SPACER_SIZE));
        controls.add(rerunFailedTests);
        controls.add(Box.createRigidArea(SPACER_SIZE));
        controls.add(cancelTests);
        controls.add(Box.createRigidArea(SPACER_SIZE));
        controls.add(testsProgress);
        controls.add(Box.createRigidArea(SPACER_SIZE));

        testsProgress.setColor1(Color.GREEN);
        testsProgress.setColor2(Color.RED);

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(controls);
        add(testOutputView);

        runTests.setName(RUN_BUTTON_NAME);
        cancelTests.setName(CANCEL_BUTTON_NAME);
        rerunFailedTests.setName(RERUN_BUTTON_NAME);

        cancelTests.setEnabled(false);
        rerunFailedTests.setEnabled(false);
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

    public DoubleProgressBar getTestsProgress() {
        return testsProgress;
    }

    public GTestOutputView getTestOutputView() {
        return testOutputView;
    }
}
