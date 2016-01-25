package gumanoid.ui.gtest;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import gumanoid.event.EventDispatcher;
import gumanoid.event.GTestOutputEvent;
import gumanoid.event.GTestOutputEvent.SuiteStart;
import gumanoid.event.GTestOutputEvent.TestFailed;
import gumanoid.event.GTestOutputEvent.TestPassed;
import gumanoid.parser.GTestListParser;
import gumanoid.parser.GTestOutputParser;
import gumanoid.runner.ProcessLaunchesModel;
import gumanoid.ui.gtest.output.GTestOutputViewController;
import rx.schedulers.Schedulers;
import rx.schedulers.SwingScheduler;
import rx.subjects.BehaviorSubject;

import javax.swing.*;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by Gumanoid on 18.01.2016.
 */
public class GTestViewController {
    private static class TestId {
        final String group;
        final String name;

        private TestId(String group, String name) {
            this.group = group;
            this.name = name;
        }

        @Override
        public String toString() {
            return group + "." + name;
        }
    }

    private final GTestView view;

    private final GTestOutputViewController outputController;

    private final ProcessLaunchesModel testEnumerationProcess = new ProcessLaunchesModel();
    private final ProcessLaunchesModel testExecutionProcess = new ProcessLaunchesModel();
    private final String testExePath;

    private final Collection<TestId> failedTests = new LinkedList<>();
    private final Collection<TestId> newFailedTests = new LinkedList<>();

    //todo such bindings are more suitable for VM in MVVM pattern than for controller
    //https://github.com/Petikoch/Java_MVVM_with_Swing_and_RxJava_Examples has some interesting ideas
    private final BehaviorSubject<Void> runTestsTrigger = BehaviorSubject.create();
    private final BehaviorSubject<Collection<TestId>> rerunTestsTrigger = BehaviorSubject.create();

    public GTestViewController(GTestView view, String testExePath) {
        this.view = view;
        this.testExePath = testExePath;
        this.outputController = new GTestOutputViewController(view.getTestOutputView());

        view.getRunTests().addActionListener(e -> runTestsTrigger.onNext(null));
        view.getRerunFailedTests().addActionListener(e -> rerunTestsTrigger.onNext(failedTests));
        view.getCancelTests().addActionListener(e -> this.cancelTests());

        runTestsTrigger.subscribe(x -> outputController.resetState());
        rerunTestsTrigger.subscribe(x -> outputController.resetState());

        runTestsTrigger.observeOn(Schedulers.io())
                .subscribe(x -> {
                    testEnumerationProcess.start(new ProcessBuilder(this.testExePath, "--gtest_list_tests"));
                    testExecutionProcess.start(new ProcessBuilder(this.testExePath));
                });
        rerunTestsTrigger.observeOn(Schedulers.io())
                .subscribe(x -> {
                    testEnumerationProcess.start(new ProcessBuilder(this.testExePath, "--gtest_list_tests", createTestFilter(failedTests)));
                    testExecutionProcess.start(new ProcessBuilder(this.testExePath, createTestFilter(failedTests)));
                });

        EventDispatcher<GTestOutputEvent> eventDispatcher = new EventDispatcher<>(e -> {});

        eventDispatcher.addHandler(SuiteStart.class, this::rememberTestCount);
        eventDispatcher.addHandler(TestFailed.class, this::increaseProgress);
        eventDispatcher.addHandler(TestPassed.class, this::increaseProgress);

        eventDispatcher.addHandler(TestFailed.class, this::rememberFailedTest);

        testEnumerationProcess.onStarted()
                .subscribe(process -> {
                    process.getOutput()
                            .lift(new GTestListParser())
                            .observeOn(SwingScheduler.getInstance())
                            .subscribe(e -> {
                                Preconditions.checkState(SwingUtilities.isEventDispatchThread());
                                outputController.onTestEnumeration(e);
                            });
                });

        testExecutionProcess.onStarted()
                .subscribe(process -> {
                    process.getOutput()
                            .lift(new GTestOutputParser())
                            .observeOn(SwingScheduler.getInstance())
                            .subscribe(e -> {
                                Preconditions.checkState(SwingUtilities.isEventDispatchThread());
                                outputController.onTestOutput(e);
                                eventDispatcher.accept(e);
                            }); //todo also handle error

                    process.getExitCode()
                            .observeOn(SwingScheduler.getInstance())
                            .subscribe(exitCode -> {
                                Preconditions.checkState(SwingUtilities.isEventDispatchThread());
                                outputController.processFinished(exitCode);
                            });
                });

        testExecutionProcess.onStarted()
                .observeOn(SwingScheduler.getInstance())
                .subscribe(p -> {
                    Preconditions.checkState(SwingUtilities.isEventDispatchThread());

                    newFailedTests.clear();

                    view.getTestsProgress().setValue(0);

                    view.getRunTests().setEnabled(false);
                    view.getRerunFailedTests().setEnabled(false);
                    view.getCancelTests().setEnabled(true);
                }); //todo also handle error

        testExecutionProcess.onFinished()
                .observeOn(SwingScheduler.getInstance())
                .subscribe(exitCode -> {
                    Preconditions.checkState(SwingUtilities.isEventDispatchThread());

                    failedTests.clear();
                    failedTests.addAll(newFailedTests);

                    view.getRunTests().setEnabled(true);
                    view.getRerunFailedTests().setEnabled(!newFailedTests.isEmpty());
                    view.getCancelTests().setEnabled(true);
                });
    }

    public void runAllTests() {
        runTestsTrigger.onNext(null);
    }

    public void runFailedTests() {
        rerunTestsTrigger.onNext(failedTests);
    }

    public void cancelTests() {
        testExecutionProcess.cancel();
    }

    private void rememberTestCount(SuiteStart e) {
        view.getTestsProgress().setMaximum(e.testCount);
    }

    private void increaseProgress(GTestOutputEvent e) {
        JProgressBar progress = view.getTestsProgress();
        progress.setValue(progress.getValue() + 1);
    }

    private void rememberFailedTest(TestFailed e) {
        newFailedTests.add(new TestId(e.groupName, e.testName));
    }

    //todo check what will happen if both cmd line param and env var will be set to different values
    //(e. g. which one has priority)
    private static String createTestFilter(Collection<TestId> testsToInclude) {
        return "--gtest_filter=" + Joiner.on(":").join(testsToInclude);
    }
}
