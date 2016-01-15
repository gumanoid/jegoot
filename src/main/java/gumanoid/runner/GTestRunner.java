package gumanoid.runner;

import gumanoid.parser.ClassifiedGTestOutputHandler;
import gumanoid.parser.GTestOutputParser;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Starts test executable, feeds it's output to parser, reports progress
 * (in a form of triples failed/done/total), and returns list of failed tests
 *
 * Created by Gumanoid on 10.01.2016.
 */
public abstract class GTestRunner extends GTestOutputParsingTask<GTestRunner.SuiteResult, GTestRunner.SuiteProgress> {
    protected static class SuiteProgress {
        public final int failedTests;
        public final int finishedTests;
        public final int totalTests;

        public SuiteProgress(int failedTests, int finishedTests, int totalTests) {
            this.failedTests = failedTests;
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

    private final GTestOutputParser parser;

    private final Collection<String> failedTests = new LinkedList<>();

    public GTestRunner(String testExePath, ClassifiedGTestOutputHandler outputListener) {
        super(testExePath);
        this.parser = new GTestOutputParser(createTestResultsCollector(outputListener));
    }

    public GTestRunner(String testExePath, Collection<String> failedTests, ClassifiedGTestOutputHandler outputListener) {
        super(testExePath, filterTestOption(failedTests));
        this.parser = new GTestOutputParser(createTestResultsCollector(outputListener));
    }

    @Override
    protected void onNextOutputLine(String outputLine) {
        parser.onNextLine(outputLine);
    }

    @Override
    protected SuiteResult createResult(boolean cancelled, int exitCode) {
        return cancelled ? null : new SuiteResult(exitCode, failedTests);
    }

    private InvokeLaterProxyHandler createTestResultsCollector(ClassifiedGTestOutputHandler outputHandler) {
        return new InvokeLaterProxyHandler(outputHandler) {
            private int failed = 0;
            private int total = 0;
            private int finished = 0;

            @Override
            public void suiteStart(String outputLine, int testCount, int testGroupCount) {
                super.suiteStart(outputLine, testCount, testGroupCount);
                total = testCount;
            }

            @Override
            public void testPassed(String outputLine, String groupName, String testName) {
                super.testPassed(outputLine, groupName, testName);
                publish(new SuiteProgress(failed, ++finished, total));
            }

            @Override
            public void testFailed(String outputLine, String groupName, String testName) {
                super.testFailed(outputLine, groupName, testName);
                publish(new SuiteProgress(++failed, ++finished, total));
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
            try {
                onFinish(get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected abstract void onProgress(SuiteProgress progress);
    protected abstract void onFinish(SuiteResult result);
}
