package gumanoid.runner;

import com.google.common.base.Joiner;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;

/**
 * Starts GTest process, reads it's output and feeds it into implementation of {@link #onNextOutputLine(String)}.
 * Waits for process termination before return result. Terminates process on cancel
 *
 * Created by Gumanoid on 15.01.2016.
 */
public abstract class GTestOutputParsingTask<TResult, TProgress> extends SwingWorker<TResult, TProgress> {
    private final ProcessBuilder testProcess;

    public GTestOutputParsingTask(String... executableAndArgs) {
        this.testProcess = new ProcessBuilder(executableAndArgs).redirectErrorStream(true);
    }

    //todo check what will happen if both cmd line param and env var will be set to different values
    //(e. g. which one has priority)
    protected static String filterTestOption(Collection<String> testsToInclude) {
        return "--gtest_filter=" + Joiner.on(":").join(testsToInclude);
    }

    @Override
    protected final TResult doInBackground() throws Exception {
        Process testProcess = this.testProcess.start();

        String charset = "CP866"; //todo platform-specific encoding

        try (BufferedReader testOutput = new BufferedReader(new InputStreamReader(testProcess.getInputStream(), charset))) {
            //Stream#takeWhile() is unavailable until Java 9, so we have to use anyMatch as work-around
            boolean cancelled = testOutput.lines().peek(this::onNextOutputLine).anyMatch(o -> isCancelled());
            if (cancelled) {
                testProcess.destroyForcibly();
            }
            return createResult(cancelled, testProcess.waitFor());
        }
    }

    protected abstract void onNextOutputLine(String outputLine);
    protected abstract TResult createResult(boolean cancelled, int exitCode);
}
