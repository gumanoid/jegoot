package gumanoid.ui.gtest.output;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import gumanoid.event.GTestListEvent.GroupAnnounce;
import gumanoid.event.GTestListEvent.TestAnnounce;
import gumanoid.event.GTestOutputEvent.*;
import gumanoid.ui.Animation;
import rx.functions.Action2;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import java.awt.*;

/**
 * Created by Gumanoid on 18.01.2016.
 */
public class GTestOutputViewController {
    private final GTestOutputView view;
    private final GTestOutputTreeModel<GTestOutputRow> model;

    private final Animation<GTestOutputTreeModel.Node<GTestOutputRow>, Icon> currentSuiteIndicator;
    private final Animation<GTestOutputTreeModel.Node<GTestOutputRow>, Icon> currentGroupIndicator;
    private final Animation<GTestOutputTreeModel.Node<GTestOutputRow>, Icon> currentTestIndicator;

    private boolean failsInSuite = false;
    private boolean failsInGroup = false;

    public GTestOutputViewController(GTestOutputView view) {
        this.view = view;
        this.model = view.getModel();

        DefaultListModel<TreePath> breadCrumbsModel = new DefaultListModel<>();

        view.getCrumbs().setModel(breadCrumbsModel);

        Action2<GTestOutputTreeModel.Node<GTestOutputRow>, Icon> updateIcon = (node, icon) -> {
            node.getValue().setIcon(icon);
            model.nodeUpdated(node);
        };

        currentSuiteIndicator = Animation.create(updateIcon);
        currentGroupIndicator = Animation.create(updateIcon);
        currentTestIndicator = Animation.create(updateIcon);

        model.addTreeModelListener(new TreeModelListener() {
            @Override
            public void treeNodesChanged(TreeModelEvent e) {
                if (breadCrumbsModel.contains(e.getTreePath())) {
                    view.getCrumbs().repaint();
                }
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e) {
                TreePath childPath = e.getTreePath().pathByAddingChild(e.getChildren()[0]);
                view.getTree().scrollPathToVisible(childPath);
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
            }

            @Override
            public void treeStructureChanged(TreeModelEvent e) {
            }
        });

        view.getTreeScroll().getViewport().addChangeListener(e -> {
            JViewport viewport = JViewport.class.cast(e.getSource());

//            boolean scrolling = treeScroll.getVerticalScrollBar().isVisible();
//            breadCrumbs.setVisible(scrolling);
//            if (!scrolling) {
//                return;
//            }

            Point upperLeft = viewport.getViewPosition();
            upperLeft = viewport.toViewCoordinates(upperLeft);
            TreePath firstVisibleRow = view.getTree().getClosestPathForLocation(upperLeft.x, upperLeft.y);

            breadCrumbsModel.clear();
            for (TreePath step = firstVisibleRow; step != null; step = step.getParentPath()) {
                if (step.getParentPath() == null) {
                    continue; //skip invisible root element
                }

                GTestOutputTreeModel.Node node = GTestOutputTreeModel.Node.class.cast(step.getLastPathComponent());
                if (node.isLeaf()) {
                    continue; //skip output lines
                }

                breadCrumbsModel.add(0, step);
            }
        });

        view.getCrumbs().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                view.getTree().scrollPathToVisible(view.getCrumbs().getSelectedValue());
            }
        });
    }

    public void resetState() {
        Preconditions.checkState(SwingUtilities.isEventDispatchThread());

        model.clear();
        model.queueSuite(new GTestOutputRow("Suite"));

        failsInGroup = false;
        failsInSuite = false;
    }

    public void processFinished(int exitCode) {
        Preconditions.checkState(SwingUtilities.isEventDispatchThread());

        stopAnimation();

        GTestOutputRow row = new GTestOutputRow("Test finished with exit code " + exitCode);
        if (exitCode != 0) {
            row.setTextColor(GTestOutputRowStyle.COLOR_FAILED);
        }

        model.addOutput(model.rootNode(), row);
    }

    private void stopAnimation() {
        currentSuiteIndicator.stopAnimation();
        currentGroupIndicator.stopAnimation();
        currentTestIndicator.stopAnimation();
    }

    @Subscribe
    public void groupAnnounce(GroupAnnounce e) {
        Preconditions.checkState(SwingUtilities.isEventDispatchThread());

        GTestOutputRow test = new GTestOutputRow(e.groupName);
        test.setTextColor(GTestOutputRowStyle.COLOR_QUEUED);
        model.queueGroup(e.groupName, test);
    }

    @Subscribe
    public void testAnnounce(TestAnnounce e) {
        Preconditions.checkState(SwingUtilities.isEventDispatchThread());

        GTestOutputRow test = new GTestOutputRow(e.testName);
        test.setTextColor(GTestOutputRowStyle.COLOR_QUEUED);
        model.queueTest(e.groupName, e.testName, test);
    }

    @Subscribe
    public void outputBeforeSuiteStarted(OutputBeforeSuiteStarted e) {
        Preconditions.checkState(SwingUtilities.isEventDispatchThread());

        model.addOutput(model.rootNode(), new GTestOutputRow(e.outputLine));
    }

    @Subscribe
    public void suiteStart(SuiteStart e) {
        Preconditions.checkState(SwingUtilities.isEventDispatchThread());

        GTestOutputTreeModel.BranchNode<GTestOutputRow> suiteNode = model.addSuite(new GTestOutputRow("Suite with " + e.testCount + "tests"));
        model.addOutput(suiteNode, new GTestOutputRow(e.outputLine));

        currentSuiteIndicator.animate(suiteNode, GTestOutputRowStyle.GRAY_SPINNER);
    }

    @Subscribe
    public void suiteEnd(SuiteEnd e) {
        Preconditions.checkState(SwingUtilities.isEventDispatchThread());

        GTestOutputRow suite = model.suiteNode().getValue();

        model.addOutput(model.suiteNode(), new GTestOutputRow(e.outputLine));

        currentSuiteIndicator.stopAnimation();
        if (failsInSuite) {
//            suite.setTextColor(GTestOutputRowStyle.COLOR_FAILED);
            suite.setIcon(GTestOutputRowStyle.SUITE_FAILED_ICON);
        } else {
            suite.setTextColor(GTestOutputRowStyle.COLOR_PASSED);
            suite.setIcon(GTestOutputRowStyle.SUITE_PASSED_ICON);
        }
        model.nodeUpdated(model.suiteNode());

        GTestOutputRow summaryNode = new GTestOutputRow("Summary");
        summaryNode.setTextColor(failsInSuite? GTestOutputRowStyle.COLOR_FAILED : GTestOutputRowStyle.COLOR_PASSED);
        model.addSummary(summaryNode);
    }

    @Subscribe
    public void groupStart(GroupStart e) {
        Preconditions.checkState(SwingUtilities.isEventDispatchThread());

        failsInGroup = false;

        GTestOutputTreeModel.BranchNode<GTestOutputRow> groupNode = model.addGroup(e.groupName, new GTestOutputRow(e.groupName + " with " + e.testsInGroup + " test(s)"));
        model.addOutput(groupNode, new GTestOutputRow(e.outputLine));

        groupNode.getValue().setTextColor(GTestOutputRowStyle.COLOR_RUNNING);
        model.nodeUpdated(groupNode);
        currentGroupIndicator.animate(groupNode, GTestOutputRowStyle.GRAY_SPINNER);
    }

    @Subscribe
    public void groupEnd(GroupEnd e) {
        Preconditions.checkState(SwingUtilities.isEventDispatchThread());

        GTestOutputTreeModel.BranchNode<GTestOutputRow> groupNode = model.groupNode(e.groupName);
        GTestOutputRow group = groupNode.getValue();

        currentGroupIndicator.stopAnimation();
        if (failsInGroup) {
//            group.setTextColor(Color.RED);
            group.setIcon(GTestOutputRowStyle.GROUP_FAILED_ICON);
        } else {
            group.setTextColor(GTestOutputRowStyle.COLOR_PASSED);
            group.setIcon(GTestOutputRowStyle.GROUP_PASSED_ICON);
        }

        if (e.outputLine != null) { //todo Optional instead of nullable, for consistency?
            model.addOutput(groupNode, new GTestOutputRow(e.outputLine));
        }
    }

    @Subscribe
    public void testStart(TestStart e) {
        Preconditions.checkState(SwingUtilities.isEventDispatchThread());

        GTestOutputRow test = new GTestOutputRow(e.testName);
        test.setTextColor(GTestOutputRowStyle.COLOR_RUNNING);

        GTestOutputTreeModel.BranchNode<GTestOutputRow> testNode = model.addTest(e.groupName, e.testName, test);
        model.addOutput(testNode, new GTestOutputRow(e.outputLine));

        currentTestIndicator.animate(testNode, GTestOutputRowStyle.GRAY_SPINNER);
    }

    @Subscribe
    public void testOutput(TestOutput e) {
        Preconditions.checkState(SwingUtilities.isEventDispatchThread());

        GTestOutputTreeModel.BranchNode<GTestOutputRow> parentNode =
                e.groupName.isPresent() ? e.testName.isPresent()
                        ? model.testNode(e.groupName.get(), e.testName.get())
                        : model.groupNode(e.groupName.get())
                        : model.suiteNode();

        model.addOutput(parentNode, new GTestOutputRow(e.outputLine));
    }

    @Subscribe
    public void testPassed(TestPassed e) {
        Preconditions.checkState(SwingUtilities.isEventDispatchThread());

        GTestOutputTreeModel.BranchNode<GTestOutputRow> testNode = model.testNode(e.groupName, e.testName);
        GTestOutputRow test = testNode.getValue();
        test.setTextColor(GTestOutputRowStyle.COLOR_PASSED);
        currentTestIndicator.stopAnimation();
        test.setIcon(GTestOutputRowStyle.TEST_PASSED_ICON);
        model.nodeUpdated(testNode);

        model.addOutput(testNode, new GTestOutputRow(e.outputLine));
    }

    @Subscribe
    public void testFailed(TestFailed e) {
        Preconditions.checkState(SwingUtilities.isEventDispatchThread());

        failsInGroup = true;
        failsInSuite = true;

        GTestOutputTreeModel.BranchNode<GTestOutputRow> suiteNode = model.suiteNode();
        GTestOutputTreeModel.BranchNode<GTestOutputRow> groupNode = model.groupNode(e.groupName);
        GTestOutputTreeModel.BranchNode<GTestOutputRow> testNode = model.testNode(e.groupName, e.testName);

        GTestOutputRow suite = suiteNode.getValue();
        GTestOutputRow group = groupNode.getValue();
        GTestOutputRow test = testNode.getValue();

        suite.setTextColor(GTestOutputRowStyle.COLOR_FAILED);
        currentSuiteIndicator.animate(suiteNode, GTestOutputRowStyle.RED_SPINNER);
        group.setTextColor(GTestOutputRowStyle.COLOR_FAILED);
        currentGroupIndicator.animate(groupNode, GTestOutputRowStyle.RED_SPINNER);
        test.setTextColor(GTestOutputRowStyle.COLOR_FAILED);
        currentTestIndicator.stopAnimation();
        test.setIcon(GTestOutputRowStyle.TEST_FAILED_ICON);

        model.addOutput(testNode, new GTestOutputRow(e.outputLine));
    }

    @Subscribe
    public void summaryOutput(SummaryOutput e) {
        Preconditions.checkState(SwingUtilities.isEventDispatchThread());

        model.addOutput(model.summaryNode(), new GTestOutputRow(e.outputLine));
    }

    @Subscribe
    public void summaryOutput(PassedTestsSummary e) {
        Preconditions.checkState(SwingUtilities.isEventDispatchThread());

        model.addOutput(model.summaryNode(), new GTestOutputRow(e.outputLine));
    }

    @Subscribe
    public void summaryOutput(FailedTestsSummary e) {
        Preconditions.checkState(SwingUtilities.isEventDispatchThread());

        model.addOutput(model.summaryNode(), new GTestOutputRow(e.outputLine));
    }

    @Subscribe
    public void summaryOutput(FailedTestSummary e) {
        Preconditions.checkState(SwingUtilities.isEventDispatchThread());

        model.addOutput(model.summaryNode(), new GTestOutputRow(e.outputLine));
    }
}
