package gumanoid.ui.gtest;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import gumanoid.event.GTestOutputEvent.SuiteStart;
import gumanoid.event.GTestOutputEvent.TestFailed;
import gumanoid.event.GTestOutputEvent.TestPassed;
import gumanoid.ui.DoubleProgressBar;
import gumanoid.ui.gtest.output.GTestOutputViewController;
import rx.Observable;
import rx.observables.SwingObservable;
import rx.schedulers.Schedulers;
import rx.schedulers.SwingScheduler;
import rx.subjects.BehaviorSubject;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by Gumanoid on 18.01.2016.
 */
public class GTestViewController {
    private final GTestView view;
    private final GTestModel model;

    private final GTestOutputViewController outputController;

    private final EventBus eventBus = new EventBus((t, ctx) -> {
        t.printStackTrace();
    });

    private final BehaviorSubject<Collection<GTestModel.TestId>> failedTests = BehaviorSubject.create();

    private Collection<GTestModel.TestId> newFailedTests = null;
    private int testsProgress;

    public GTestViewController(GTestView view, String testExePath) {
        this.view = view;
        this.model = new GTestModel(testExePath);
        this.outputController = new GTestOutputViewController(view.getTestOutputView());

        eventBus.register(this);
        eventBus.register(outputController);

        Observable<ActionEvent> runTests = SwingObservable.fromButtonAction(view.getRunTests());
        Observable<ActionEvent> rerunTests = SwingObservable.fromButtonAction(view.getRerunFailedTests());
        Observable<ActionEvent> cancelTests = SwingObservable.fromButtonAction(view.getCancelTests());

        Observable.merge(runTests, rerunTests)
                .observeOn(SwingScheduler.getInstance())
                .subscribe(x -> {
                    view.getRunTests().setEnabled(false);
                    view.getRerunFailedTests().setEnabled(false);
                    resetProgress();
                });

        Observable<Boolean> testsAreRunning = Observable.merge(
                model.testsStarted().map(x -> true),
                model.testsComplete().map(x -> false),
                model.testsCancelled().map(x -> false)
        ).observeOn(SwingScheduler.getInstance());

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
                    model.runTests();
                });

        rerunTests.observeOn(Schedulers.io())
                .flatMap(x -> failedTests.take(1))
                .subscribe(model::rerunFailedTests);

        cancelTests.observeOn(Schedulers.io())
                .subscribe(x -> {
                    model.cancelTests();
                });

        Observable.merge(model.testsEnumeration(), model.testsOutput())
                .observeOn(SwingScheduler.getInstance())
//                .doOnNext(System.out::println)
                .subscribe(e -> {
                    Preconditions.checkState(SwingUtilities.isEventDispatchThread());
                    eventBus.post(e);
                }); //todo also handle error

        model.testsExitCode()
                .observeOn(SwingScheduler.getInstance())
                .subscribe(exitCode -> {
                    Preconditions.checkState(SwingUtilities.isEventDispatchThread());
                    outputController.processFinished(exitCode);
                });

        model.testsComplete()
                .observeOn(SwingScheduler.getInstance())
                .subscribe(x -> {
                    failedTests.onNext(newFailedTests);
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
    public void onSuiteStart(SuiteStart e) {
        view.getTestsProgress().setMaximum(e.testCount);
    }

    @Subscribe
    public void onTestPassed(TestPassed e) {
        ++testsProgress;
        updateProgress();
    }

    @Subscribe
    public void onTestFailed(TestFailed e) {
        ++testsProgress;
        newFailedTests.add(new GTestModel.TestId(e.groupName, e.testName));
        updateProgress();
    }

    @Subscribe
    public void onDeadEvent(DeadEvent e) {
        System.out.println("Unhandled event: " + e.getEvent());
    }

    private void resetProgress() {
        newFailedTests = new LinkedList<>();
        testsProgress = 0;
        view.getTestsProgress().setValue(0);
        view.getTestsSummary().setText("Starting...");
        outputController.resetState();
    }

    private void updateProgress() {
        int passed = testsProgress - newFailedTests.size();

        DoubleProgressBar progress = view.getTestsProgress();
        progress.setValue1(passed);
        progress.setValue2(testsProgress);

        JLabel summary = view.getTestsSummary();
        summary.setText("Passed: " + passed + ". Run: " + testsProgress);
        summary.setForeground(newFailedTests.isEmpty()? GTestViewStyle.COLOR_PASSED : GTestViewStyle.COLOR_FAILED);
    }
}
