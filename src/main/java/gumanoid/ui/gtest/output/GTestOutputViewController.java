package gumanoid.ui.gtest.output;

import gumanoid.event.EventDispatcher;
import gumanoid.event.GTestOutputEvent;
import gumanoid.event.GTestOutputEvent.*;

import java.awt.*;
import java.util.function.Consumer;

/**
 * Created by Gumanoid on 18.01.2016.
 */
public class GTestOutputViewController implements Consumer<GTestOutputEvent> {
    private final GTestOutputView tree;
    private final EventDispatcher<GTestOutputEvent> eventDispatcher;

    private boolean failsInSuite = false;
    private boolean failsInGroup = false;

    public GTestOutputViewController(GTestOutputView tree) {
        this.tree = tree;
        this.eventDispatcher = new EventDispatcher<>(this::defaultEventHandler);

        eventDispatcher.addHandler(OutputBeforeSuiteStarted.class, this::outputBeforeSuiteStarted);
        eventDispatcher.addHandler(SuiteStart.class, this::suiteStart);
        eventDispatcher.addHandler(SuiteEnd.class, this::suiteEnd);
        eventDispatcher.addHandler(GroupStart.class, this::groupStart);
        eventDispatcher.addHandler(GroupEnd.class, this::groupEnd);
        eventDispatcher.addHandler(TestStart.class, this::testStart);
        eventDispatcher.addHandler(TestOutput.class, this::testOutput);
        eventDispatcher.addHandler(TestPassed.class, this::testPassed);
        eventDispatcher.addHandler(TestFailed.class, this::testFailed);
        eventDispatcher.addHandler(SummaryOutput.class, this::summaryOutput);
        eventDispatcher.addHandler(FailedTestsSummary.class, this::summaryOutput);
        eventDispatcher.addHandler(FailedTestSummary.class, this::summaryOutput);
        eventDispatcher.addHandler(PassedTestsSummary.class, this::summaryOutput);
    }

    public void processStarted() {
        tree.clear();
        failsInGroup = false;
        failsInSuite = false;
    }

    public void processFinished(int exitCode) {
        stopAnimation();

        GTestOutputView.Item exitCodeNode = tree.atRoot()
                .createOutputLine("Test finished with exit code " + exitCode);

        if (exitCode != 0) {
            exitCodeNode.setTextColor(Color.RED);
        }
    }

    private void stopAnimation() {
        tree.getCurrentSuiteNodeIcon().stopAnimation();
        tree.getCurrentGroupNodeIcon().stopAnimation();
        tree.getCurrentTestNodeIcon().stopAnimation();
    }

    public void accept(GTestOutputEvent e) {
        eventDispatcher.accept(e);
    }

    private void defaultEventHandler(GTestOutputEvent e) {
        tree.atRoot().createOutputLine(e.outputLine);
    }

    private void outputBeforeSuiteStarted(OutputBeforeSuiteStarted e) {
        tree.atRoot().createOutputLine(e.outputLine);
    }

    private void suiteStart(SuiteStart e) {
        GTestOutputView.Item suiteNode = tree.atRoot().createCollapsible("suite", "Suite");

        tree.getCurrentSuiteNodeIcon().animate(suiteNode, GTestOutputView.GRAY_SPINNER);

        tree.at("suite").createOutputLine(e.outputLine);
    }

    private void suiteEnd(SuiteEnd e) {
        GTestOutputView.Item suiteNode = tree.at("suite");

        suiteNode.createOutputLine(e.outputLine);

        tree.getCurrentSuiteNodeIcon().stopAnimation();
        if (failsInSuite) {
//            suiteNode.setTextColor(Color.RED);
            suiteNode.setIcon(GTestOutputView.SUITE_FAILED_ICON);
        } else {
            suiteNode.setTextColor(Color.GREEN);
            suiteNode.setIcon(GTestOutputView.SUITE_PASSED_ICON);
        }

        GTestOutputView.Item summaryNode = tree.atRoot().createCollapsible("summary", "Summary");
        summaryNode.setTextColor(failsInSuite? Color.RED : Color.GREEN);
    }

    private void groupStart(GroupStart e) {
        failsInGroup = false;

        GTestOutputView.Item groupNode = tree.at("suite")
                .createCollapsible(e.groupName, e.groupName + " with " + e.testsInGroup + " test(s)");
        groupNode.createOutputLine(e.outputLine);

        groupNode.setTextColor(Color.BLUE);
        tree.getCurrentGroupNodeIcon().animate(groupNode, GTestOutputView.GRAY_SPINNER);
    }

    private void groupEnd(GroupEnd e) {
        GTestOutputView.Item groupNode = tree.at("suite", e.groupName);

        tree.getCurrentGroupNodeIcon().stopAnimation();
        if (failsInGroup) {
//            groupNode.setTextColor(Color.RED);
            groupNode.setIcon(GTestOutputView.GROUP_FAILED_ICON);
        } else {
            groupNode.setTextColor(Color.GREEN);
            groupNode.setIcon(GTestOutputView.GROUP_PASSED_ICON);
        }

        if (e.outputLine != null) { //todo Optional instead of nullable, for consistency?
            groupNode.createOutputLine(e.outputLine);
        }
    }

    private void testStart(TestStart e) {
        GTestOutputView.Item groupNode = tree.at("suite", e.groupName);
        GTestOutputView.Item testNode = groupNode.createCollapsible(e.testName, e.testName);
        testNode.createOutputLine(e.outputLine);

        testNode.setTextColor(Color.BLUE);
        tree.getCurrentTestNodeIcon().animate(testNode, GTestOutputView.GRAY_SPINNER);
    }

    private void testOutput(TestOutput e) {
        (
                e.groupName.isPresent() ? e.testName.isPresent()
                        ? tree.at("suite", e.groupName.get(), e.testName.get())
                        : tree.at("suite", e.groupName.get())
                        : tree.at("suite")
        ).createOutputLine(e.outputLine);
    }

    private void testPassed(TestPassed e) {
        GTestOutputView.Item testNode = tree.at("suite", e.groupName, e.testName);
        testNode.setTextColor(Color.GREEN);
        tree.getCurrentTestNodeIcon().stopAnimation();
        testNode.setIcon(GTestOutputView.TEST_PASSED_ICON);
        testNode.createOutputLine(e.outputLine);
    }

    private void testFailed(TestFailed e) {
        failsInGroup = true;
        failsInSuite = true;

        GTestOutputView.Item suiteNode = tree.at("suite");
        GTestOutputView.Item groupNode = tree.at("suite", e.groupName);
        GTestOutputView.Item testNode = tree.at("suite", e.groupName, e.testName);

        suiteNode.setTextColor(Color.RED);
        tree.getCurrentSuiteNodeIcon().animate(suiteNode, GTestOutputView.RED_SPINNER);
        groupNode.setTextColor(Color.RED);
        tree.getCurrentGroupNodeIcon().animate(groupNode, GTestOutputView.RED_SPINNER);
        testNode.setTextColor(Color.RED);
        tree.getCurrentTestNodeIcon().stopAnimation();
        testNode.setIcon(GTestOutputView.TEST_FAILED_ICON);

        testNode.createOutputLine(e.outputLine);
    }

    private void summaryOutput(GTestOutputEvent e) {
        tree.at("summary").createOutputLine(e.outputLine);
    }
}
