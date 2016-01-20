package gumanoid.ui.gtest;

import com.google.common.base.Joiner;
import gumanoid.event.EventDispatcher;
import gumanoid.event.GTestOutputEvent;
import gumanoid.event.GTestOutputEvent.SuiteStart;
import gumanoid.event.GTestOutputEvent.TestFailed;
import gumanoid.event.GTestOutputEvent.TestPassed;
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
    //todo this class is too big, decompose?

    private static class TestId {
        public final String group;
        public final String name;

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

    private final ProcessLaunchesModel testProcess = new ProcessLaunchesModel();
    private final String testExePath;

    private final EventDispatcher<GTestOutputEvent> eventDispatcher;

    private final Collection<TestId> failedTests = new LinkedList<>();
    private final Collection<TestId> newFailedTests = new LinkedList<>();

    //todo it's ugly to have BehaviorSubjects just to shift execution to another thread
    private final BehaviorSubject<Void> runTestsTrigger = BehaviorSubject.create();
    private final BehaviorSubject<Collection<TestId>> rerunTestsTrigger = BehaviorSubject.create();

    public GTestViewController(GTestView view, String testExePath) {
        this.view = view;
        this.testExePath = testExePath;
        this.outputController = new GTestOutputViewController(view.getTestOutputView());
        this.eventDispatcher = new EventDispatcher<>(e -> {});

        testProcess.onStarted()
                .subscribe(process -> {
                    process.getOutput()
                            .lift(new GTestOutputParser())
                            .observeOn(SwingScheduler.getInstance())
                            .doOnNext(outputController::accept)
                            .doOnNext(eventDispatcher::accept)
                            .subscribe(); //todo also handle error

                    process.getExitCode()
                            .observeOn(SwingScheduler.getInstance())
                            .subscribe(outputController::processFinished);
                });

        testProcess.onStarted()
                .observeOn(SwingScheduler.getInstance())
                .subscribe(p -> {
                    newFailedTests.clear();

                    view.getTestsProgress().setValue(0);

                    view.getRunTests().setEnabled(false);
                    view.getRerunFailedTests().setEnabled(false);
                    view.getCancelTests().setEnabled(true);

                    outputController.processStarted();
                }); //todo also handle error

        testProcess.onFinished()
                .observeOn(SwingScheduler.getInstance())
                .subscribe(exitCode -> {
                    failedTests.clear();
                    failedTests.addAll(newFailedTests);

                    view.getRunTests().setEnabled(true);
                    view.getRerunFailedTests().setEnabled(!newFailedTests.isEmpty());
                    view.getCancelTests().setEnabled(true);
                });

        view.getRunTests().addActionListener(e -> runTestsTrigger.onNext(null));
        view.getRerunFailedTests().addActionListener(e -> rerunTestsTrigger.onNext(failedTests));
        view.getCancelTests().addActionListener(e -> this.cancelTests());

        runTestsTrigger.observeOn(Schedulers.io())
                .subscribe(x -> testProcess.start(new ProcessBuilder(this.testExePath)));
        rerunTestsTrigger.observeOn(Schedulers.io())
                .subscribe(x -> testProcess.start(new ProcessBuilder(this.testExePath, createTestFilter(failedTests))));

        eventDispatcher.addHandler(SuiteStart.class, this::rememberTestCount);
        eventDispatcher.addHandler(TestFailed.class, this::increaseProgress);
        eventDispatcher.addHandler(TestPassed.class, this::increaseProgress);

        eventDispatcher.addHandler(TestFailed.class, this::rememberFailedTest);
    }

    public void runAllTests() {
        runTestsTrigger.onNext(null);
    }

    public void runFailedTests() {
        rerunTestsTrigger.onNext(failedTests);
    }

    public void cancelTests() {
        testProcess.cancel();
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
