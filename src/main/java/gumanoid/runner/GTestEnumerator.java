package gumanoid.runner;

import javax.swing.*;
import java.util.Collection;

/**
 * Created by Gumanoid on 15.01.2016.
 */
public abstract class GTestEnumerator extends GTestOutputParsingTask<Void, Void> {
    public GTestEnumerator(String testExePath) {
        super(testExePath, "--gtest_list_tests");
    }

    public GTestEnumerator(String testExePath, Collection<String> failedTests) {
        super(testExePath, "--gtest_list_tests", filterTestOption(failedTests));
    }

    @Override
    protected final void onNextOutputLine(String outputLine) {
        if (outputLine.startsWith("  ")) {
            SwingUtilities.invokeLater(() -> onTest(outputLine));
        } else {
            String groupName = outputLine.substring(0, outputLine.length() - 1);
            SwingUtilities.invokeLater(() -> onTestGroup(groupName));
        }
    }

    @Override
    protected Void createResult(boolean cancelled, int exitCode) {
        return null;
    }

    protected abstract void onTestGroup(String testGroupName);
    protected abstract void onTest(String testName);
}
