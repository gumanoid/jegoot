package gumanoid.ui.gtest;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import gumanoid.event.GTestOutputEvent.SuiteStart;
import gumanoid.event.GTestOutputEvent.TestFailed;
import gumanoid.event.GTestOutputEvent.TestPassed;
import gumanoid.parser.GTestListParser;
import gumanoid.parser.GTestOutputParser;
import gumanoid.runner.ProcessLaunchesModel;
import gumanoid.ui.gtest.output.GTestOutputViewController;
import rx.Observable;
import rx.observables.SwingObservable;
import rx.schedulers.Schedulers;
import rx.schedulers.SwingScheduler;
import rx.subjects.BehaviorSubject;

import javax.swing.*;
import java.awt.event.ActionEvent;
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

    private final EventBus eventBus = new EventBus((t, ctx) -> {
        t.printStackTrace();
    });

    private final ProcessLaunchesModel testEnumerationProcess = new ProcessLaunchesModel();
    private final ProcessLaunchesModel testExecutionProcess = new ProcessLaunchesModel();
    private final String testExePath;

    private final BehaviorSubject<Collection<TestId>> failedTests = BehaviorSubject.create();

    private Collection<TestId> newFailedTests = null;

    //todo such bindings are more suitable for VM in MVVM pattern than for controller
    //https://github.com/Petikoch/Java_MVVM_with_Swing_and_RxJava_Examples has some interesting ideas

    public GTestViewController(GTestView view, String testExePath) {
        this.view = view;
        this.testExePath = testExePath;
        this.outputController = new GTestOutputViewController(view.getTestOutputView());

        eventBus.register(this);
        eventBus.register(outputController);

        Observable<ActionEvent> runTests = SwingObservable.fromButtonAction(view.getRunTests());
        Observable<ActionEvent> rerunTests = SwingObservable.fromButtonAction(view.getRerunFailedTests());
        Observable<ActionEvent> cancelTests = SwingObservable.fromButtonAction(view.getCancelTests());

        Observable<Boolean> testsAreRunning = Observable.merge(
                testExecutionProcess.onStarted().map(x -> true),
                testExecutionProcess.onFinished().map(x -> false)
        ).observeOn(SwingScheduler.getInstance());

        Observable.merge(runTests, rerunTests)
                .subscribe(x -> {
                    view.getRunTests().setEnabled(false);
                    view.getRerunFailedTests().setEnabled(false);
                });

        testsAreRunning.subscribe(r -> {
            view.getRunTests().setEnabled(!r);
            view.getCancelTests().setEnabled(r);
        });

        cancelTests.subscribe(x -> view.getCancelTests().setEnabled(false));

        Observable.combineLatest(
                testsAreRunning,
                failedTests,
                (r, f) -> !r && !f.isEmpty()
        ).subscribe(view.getRerunFailedTests()::setEnabled);

        runTests.observeOn(Schedulers.io())
                .subscribe(x -> {
                    testEnumerationProcess.start(new ProcessBuilder(this.testExePath, "--gtest_list_tests"));
                    testExecutionProcess.start(new ProcessBuilder(this.testExePath));
                });

        rerunTests.observeOn(Schedulers.io())
                .flatMap(x -> failedTests)
                .map(GTestViewController::createTestFilter)
                .subscribe(filter -> {
                    testEnumerationProcess.start(new ProcessBuilder(this.testExePath, "--gtest_list_tests", filter));
                    testExecutionProcess.start(new ProcessBuilder(this.testExePath, filter));
                });

        cancelTests.observeOn(Schedulers.io())
                .subscribe(x -> {
                    testEnumerationProcess.cancel();
                    testExecutionProcess.cancel();
                });

        testEnumerationProcess.onStarted()
                .subscribe(process -> {
                    process.getOutput()
                            .lift(new GTestListParser())
                            .observeOn(SwingScheduler.getInstance())
//                            .doOnNext(System.out::println)
                            .subscribe(e -> {
                                Preconditions.checkState(SwingUtilities.isEventDispatchThread());
                                eventBus.post(e);
                            });
                });

        testExecutionProcess.onStarted()
                .subscribe(process -> {
                    process.getOutput()
                            .lift(new GTestOutputParser())
                            .observeOn(SwingScheduler.getInstance())
//                            .doOnNext(System.out::println)
                            .subscribe(e -> {
                                Preconditions.checkState(SwingUtilities.isEventDispatchThread());
                                eventBus.post(e);
                            }); //todo also handle error

                    process.getExitCode()
                            .observeOn(SwingScheduler.getInstance())
                            .subscribe(exitCode -> {
                                Preconditions.checkState(SwingUtilities.isEventDispatchThread());
                                outputController.processFinished(exitCode);
                            });
                });

        testExecutionProcess.onFinished()
                .observeOn(SwingScheduler.getInstance())
                .subscribe(x -> {
                    failedTests.onNext(newFailedTests);
                });

        Observable.merge(runTests, rerunTests)
                .observeOn(SwingScheduler.getInstance())
                .subscribe(x -> {
                    newFailedTests = new LinkedList<>();
                    view.getTestsProgress().setValue(0);
                    outputController.resetState();
                });
    }

    public void runAllTests() {
        view.getRunTests().doClick();
    }

    public void runFailedTests() {
        view.getRerunFailedTests().doClick();
    }

    public void cancelTests() {
        view.getCancelTests().doClick();
    }

    @Subscribe
    public void rememberTestCount(SuiteStart e) {
        view.getTestsProgress().setMaximum(e.testCount);
    }

    @Subscribe
    public void increaseProgress(TestFailed e) {
        JProgressBar progress = view.getTestsProgress();
        progress.setValue(progress.getValue() + 1);
    }

    @Subscribe
    public void increaseProgress(TestPassed e) {
        JProgressBar progress = view.getTestsProgress();
        progress.setValue(progress.getValue() + 1);
    }

    @Subscribe
    public void rememberFailedTest(TestFailed e) {
        newFailedTests.add(new TestId(e.groupName, e.testName));
    }

    @Subscribe
    public void onDeadEvent(DeadEvent e) {
        System.out.println("Unhandled event: " + e.getEvent());
    }

    //todo check what will happen if both cmd line param and env var will be set to different values
    //(e. g. which one has priority)
    private static String createTestFilter(Collection<TestId> testsToInclude) {
        return "--gtest_filter=" + Joiner.on(":").join(testsToInclude);
    }
}
