package gumanoid.ui.gtest.output;

import gumanoid.event.EventDispatcher;
import gumanoid.event.GTestOutputEvent;
import gumanoid.event.GTestOutputEvent.*;
import gumanoid.ui.Animation;
import rx.functions.Action2;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Created by Gumanoid on 18.01.2016.
 */
public class GTestOutputViewController implements Consumer<GTestOutputEvent> {
    private final GTestOutputView view;
    private final GTestOutputTreeModel<GTestOutputRow> model;

    private final Animation<GTestOutputTreeModel.Node<GTestOutputRow>, Icon> currentSuiteIndicator;
    private final Animation<GTestOutputTreeModel.Node<GTestOutputRow>, Icon> currentGroupIndicator;
    private final Animation<GTestOutputTreeModel.Node<GTestOutputRow>, Icon> currentTestIndicator;

    private final EventDispatcher<GTestOutputEvent> eventDispatcher;

    private boolean failsInSuite = false;
    private boolean failsInGroup = false;

    public GTestOutputViewController(GTestOutputView view) {
        this.view = view;
        this.model = new GTestOutputTreeModel<>(new GTestOutputRow(null));
        this.eventDispatcher = new EventDispatcher<>(this::defaultEventHandler);

        view.getTree().setModel(model);

        Action2<GTestOutputTreeModel.Node<GTestOutputRow>, Icon> updateIcon = (node, icon) -> {
            node.getValue().setIcon(icon);
            model.nodeUpdated(node);
        };

        currentSuiteIndicator = Animation.create(updateIcon);
        currentGroupIndicator = Animation.create(updateIcon);
        currentTestIndicator = Animation.create(updateIcon);

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
        model.clear();
        failsInGroup = false;
        failsInSuite = false;
    }

    public void processFinished(int exitCode) {
        stopAnimation();

        GTestOutputRow row = new GTestOutputRow("Test finished with exit code " + exitCode);
        if (exitCode != 0) {
            row.setTextColor(Color.RED);
        }

        model.addOutput(model.rootNode(), row);
    }

    private void stopAnimation() {
        currentSuiteIndicator.stopAnimation();
        currentGroupIndicator.stopAnimation();
        currentTestIndicator.stopAnimation();
    }

    public void accept(GTestOutputEvent e) {
        eventDispatcher.accept(e);
    }

    private void defaultEventHandler(GTestOutputEvent e) {
        model.addOutput(model.suiteNode(), new GTestOutputRow(e.outputLine));
    }

    private void outputBeforeSuiteStarted(OutputBeforeSuiteStarted e) {
        model.addOutput(model.suiteNode(), new GTestOutputRow(e.outputLine));
    }

    private void suiteStart(SuiteStart e) {
        GTestOutputTreeModel.BranchNode<GTestOutputRow> suiteNode = model.addSuite(new GTestOutputRow("Suite"));
        model.addOutput(suiteNode, new GTestOutputRow(e.outputLine));

        currentSuiteIndicator.animate(suiteNode, GTestOutputView.GRAY_SPINNER);
    }

    private void suiteEnd(SuiteEnd e) {
        GTestOutputRow suite = model.suiteNode().getValue();

        model.addOutput(model.suiteNode(), new GTestOutputRow(e.outputLine));

        currentSuiteIndicator.stopAnimation();
        if (failsInSuite) {
//            suite.setTextColor(Color.RED);
            suite.setIcon(GTestOutputView.SUITE_FAILED_ICON);
        } else {
            suite.setTextColor(Color.GREEN);
            suite.setIcon(GTestOutputView.SUITE_PASSED_ICON);
        }
        model.nodeUpdated(model.suiteNode());

        GTestOutputRow summaryNode = new GTestOutputRow("Summary");
        summaryNode.setTextColor(failsInSuite? Color.RED : Color.GREEN);
        model.addSummary(summaryNode);
    }

    private void groupStart(GroupStart e) {
        failsInGroup = false;

        GTestOutputTreeModel.BranchNode<GTestOutputRow> groupNode = model.addGroup(e.groupName, new GTestOutputRow(e.groupName + " with " + e.testsInGroup + " test(s)"));
        model.addOutput(groupNode, new GTestOutputRow(e.outputLine));

        groupNode.getValue().setTextColor(Color.BLUE);
        model.nodeUpdated(groupNode);
        currentGroupIndicator.animate(groupNode, GTestOutputView.GRAY_SPINNER);
    }

    private void groupEnd(GroupEnd e) {
        GTestOutputTreeModel.BranchNode<GTestOutputRow> groupNode = model.groupNode(e.groupName);
        GTestOutputRow group = groupNode.getValue();

        currentGroupIndicator.stopAnimation();
        if (failsInGroup) {
//            group.setTextColor(Color.RED);
            group.setIcon(GTestOutputView.GROUP_FAILED_ICON);
        } else {
            group.setTextColor(Color.GREEN);
            group.setIcon(GTestOutputView.GROUP_PASSED_ICON);
        }

        if (e.outputLine != null) { //todo Optional instead of nullable, for consistency?
            model.addOutput(groupNode, new GTestOutputRow(e.outputLine));
        }
    }

    private void testStart(TestStart e) {
        GTestOutputRow test = new GTestOutputRow(e.testName);
        test.setTextColor(Color.BLUE);

        GTestOutputTreeModel.BranchNode<GTestOutputRow> testNode = model.addTest(e.groupName, e.testName, test);
        model.addOutput(testNode, new GTestOutputRow(e.outputLine));

        currentTestIndicator.animate(testNode, GTestOutputView.GRAY_SPINNER);
    }

    private void testOutput(TestOutput e) {
        GTestOutputTreeModel.BranchNode<GTestOutputRow> parentNode =
                e.groupName.isPresent() ? e.testName.isPresent()
                        ? model.testNode(e.groupName.get(), e.testName.get())
                        : model.groupNode(e.groupName.get())
                        : model.suiteNode();

        model.addOutput(parentNode, new GTestOutputRow(e.outputLine));
    }

    private void testPassed(TestPassed e) {
        GTestOutputTreeModel.BranchNode<GTestOutputRow> testNode = model.testNode(e.groupName, e.testName);
        GTestOutputRow test = testNode.getValue();
        test.setTextColor(Color.GREEN);
        currentTestIndicator.stopAnimation();
        test.setIcon(GTestOutputView.TEST_PASSED_ICON);
        model.nodeUpdated(testNode);

        model.addOutput(testNode, new GTestOutputRow(e.outputLine));
    }

    private void testFailed(TestFailed e) {
        failsInGroup = true;
        failsInSuite = true;

        GTestOutputTreeModel.BranchNode<GTestOutputRow> suiteNode = model.suiteNode();
        GTestOutputTreeModel.BranchNode<GTestOutputRow> groupNode = model.groupNode(e.groupName);
        GTestOutputTreeModel.BranchNode<GTestOutputRow> testNode = model.testNode(e.groupName, e.testName);

        GTestOutputRow suite = suiteNode.getValue();
        GTestOutputRow group = groupNode.getValue();
        GTestOutputRow test = testNode.getValue();

        suite.setTextColor(Color.RED);
        currentSuiteIndicator.animate(suiteNode, GTestOutputView.RED_SPINNER);
        group.setTextColor(Color.RED);
        currentGroupIndicator.animate(groupNode, GTestOutputView.RED_SPINNER);
        test.setTextColor(Color.RED);
        currentTestIndicator.stopAnimation();
        test.setIcon(GTestOutputView.TEST_FAILED_ICON);

        model.addOutput(testNode, new GTestOutputRow(e.outputLine));
    }

    private void summaryOutput(GTestOutputEvent e) {
        model.addOutput(model.summaryNode(), new GTestOutputRow(e.outputLine));
    }
}
