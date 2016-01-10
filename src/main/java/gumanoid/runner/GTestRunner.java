package gumanoid.runner;

import com.google.common.base.Joiner;
import gumanoid.parser.GTestOutputParser;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Starts test executable, feeds it's output to parser, reports progress
 * (in a form of pairs done/total), and returns list of failed tests
 *
 * Created by Gumanoid on 10.01.2016.
 */
public abstract class GTestRunner extends SwingWorker<GTestRunner.SuiteResult, GTestRunner.SuiteProgress> {
    protected static class SuiteProgress {
        public final int finishedTests;
        public final int totalTests;

        public SuiteProgress(int finishedTests, int totalTests) {
            this.finishedTests = finishedTests;
            this.totalTests = totalTests;
        }
    }

    protected static class SuiteResult {
        public final int exitCode;
        public final Collection<String> failedTests;

        public SuiteResult(int exitCode, Collection<String> failedTests) {
            this.exitCode = exitCode;
            this.failedTests = failedTests;
        }
    }

    private final ProcessBuilder testProcess;
    private final GTestOutputParser parser;

    private final Collection<String> failedTests = new LinkedList<>();
    private int exitCode;

    public GTestRunner(String testExePath, GTestOutputParser.EventListener outputListener) {
        this.testProcess = new ProcessBuilder(testExePath).redirectErrorStream(true);
        this.parser = new GTestOutputParser(createTestStatsListener(outputListener));
    }

    public GTestRunner(String testExePath, Collection<String> failedTests, GTestOutputParser.EventListener outputListener) {
        this.testProcess = new ProcessBuilder(testExePath, createTestFilter(failedTests)).redirectErrorStream(true);
        this.parser = new GTestOutputParser(createTestStatsListener(outputListener));
    }

    @Override
    protected final SuiteResult doInBackground() throws Exception {
        Process testProcess = this.testProcess.start();

        String charset = "CP866"; //todo platform-specific encoding

        try (BufferedReader testOutput = new BufferedReader(new InputStreamReader(testProcess.getInputStream(), charset))) {
            for(;;) {
                if (isCancelled()) {
                    testProcess.destroyForcibly().waitFor();
                    return null;
                }

                String nextLine = testOutput.readLine();
                if (nextLine == null) {
                    break;
                }

                parser.onNextLine(nextLine);
            }
        }

        exitCode = testProcess.waitFor();
        return new SuiteResult(exitCode, failedTests);
    }

    private InvokeLaterProxyListener createTestStatsListener(GTestOutputParser.EventListener outputListener) {
        return new InvokeLaterProxyListener(outputListener) {
            private int totalTests = 0;
            private int finishedTests = 0;

            @Override
            public void suiteStart(int testCount, int testGroupCount) {
                super.suiteStart(testCount, testGroupCount);
                totalTests = testCount;
            }

            @Override
            public void testPassed(String groupName, String testName) {
                super.testPassed(groupName, testName);
                publish(new SuiteProgress(++finishedTests, totalTests));
            }

            @Override
            public void testFailed(String groupName, String testName) {
                super.testFailed(groupName, testName);
                publish(new SuiteProgress(++finishedTests, totalTests));
                failedTests.add(groupName + "." + testName);
            }
        };
    }

    @Override
    protected final void process(List<SuiteProgress> chunks) {
        SuiteProgress lastValue = chunks.get(chunks.size() - 1);
        onProgress(lastValue);
    }

    @Override
    protected final void done() {
        if (!isCancelled()) {
            onFinish(new SuiteResult(exitCode, failedTests));
        }
    }

    protected abstract void onProgress(SuiteProgress progress);
    protected abstract void onFinish(SuiteResult result);

    //todo check what will happen if both cmd line param and env var will be set to different values
    //(e. g. which one has priority)
    private static String createTestFilter(Collection<String> testsToInclude) {
        return "--gtest_filter=" + Joiner.on(":").join(testsToInclude);
    }
}
