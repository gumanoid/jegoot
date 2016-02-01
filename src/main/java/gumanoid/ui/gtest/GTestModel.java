package gumanoid.ui.gtest;

import com.google.common.base.Joiner;
import gumanoid.event.GTestListEvent;
import gumanoid.event.GTestOutputEvent;
import gumanoid.parser.GTestListParser;
import gumanoid.parser.GTestOutputParser;
import gumanoid.runner.ProcessLaunchesModel;
import gumanoid.runner.ProcessModel;
import rx.Observable;

import java.util.Collection;

/**
 * Takes care of executable events sequence: notifies subscribers
 * when tests are started, finished, cancelled; reports tests output
 * and enumeration events; reports exit code of tests process
 *
 * Created by Gumanoid on 31.01.2016.
 */
public class GTestModel {
    //todo failing tests are out of scope of this class
    public static class TestId {
        public final String group;
        public final String name;

        public TestId(String group, String name) {
            this.group = group;
            this.name = name;
        }

        @Override
        public String toString() {
            return group + "." + name;
        }
    }

    private final ProcessLaunchesModel testEnumerationProcess = new ProcessLaunchesModel();
    private final ProcessLaunchesModel testExecutionProcess = new ProcessLaunchesModel();
    private final String testExePath;

    public GTestModel(String testExePath) {
        this.testExePath = testExePath;
    }

    public void runTests() {
        testEnumerationProcess.start(new ProcessBuilder(this.testExePath, "--gtest_list_tests"));
        testExecutionProcess.start(new ProcessBuilder(this.testExePath));
    }

    public void rerunFailedTests(Collection<TestId> failedTests) {
        String filter = createTestFilter(failedTests);

        testEnumerationProcess.start(new ProcessBuilder(this.testExePath, "--gtest_list_tests", filter));
        testExecutionProcess.start(new ProcessBuilder(this.testExePath, filter));
    }

    public void cancelTests() {
        testEnumerationProcess.cancel();
        testExecutionProcess.cancel();
    }

    public Observable<GTestOutputEvent> testsOutput() {
        return testExecutionProcess.onStarted()
                .switchMap(p -> p.getOutput().lift(new GTestOutputParser()));
    }

    public Observable<GTestListEvent> testsEnumeration() {
        return testEnumerationProcess.onStarted()
                .switchMap(p -> p.getOutput().lift(new GTestListParser()));
    }

    public Observable<Integer> testsExitCode() {
        return testExecutionProcess.onStarted()
                .switchMap(ProcessModel::getExitCode);
    }

    public Observable<Void> testsStarted() {
        return testExecutionProcess.onStarted()
                .map(x -> null);
    }

    public Observable<Void> testsComplete() {
        return testExecutionProcess.onFinished()
                .filter(p -> !p.isCancelled())
                .map(x -> null);
    }

    public Observable<Void> testsCancelled() {
        return testExecutionProcess.onFinished()
                .filter(ProcessModel::isCancelled)
                .map(x -> null);
    }

    //todo check what will happen if both cmd line param and env var will be set to different values
    //(e. g. which one has priority)
    private static String createTestFilter(Collection<TestId> testsToInclude) {
        return "--gtest_filter=" + Joiner.on(":").join(testsToInclude);
    }
}
