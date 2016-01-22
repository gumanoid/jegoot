package gumanoid.ui.gtest.output;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import gumanoid.ui.Animation;
import gumanoid.ui.Icons;
import gumanoid.ui.tree.StyledTreeNode;
import gumanoid.ui.tree.StyledTreeNodeRenderer;
import rx.Observable;
import rx.schedulers.SwingScheduler;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Created by Gumanoid on 09.01.2016.
 */
public class GTestOutputView extends JPanel {
    //todo decompose this class, it is starting to smell like GodObject

    @VisibleForTesting
    public static final String TREE_NAME = "GTest_output_tree";

    @VisibleForTesting
    public static final String CRUMBS_NAME = "GTest_output_tree_crumbs";

    static final Icon TEST_PASSED_ICON = Icons.load("test_passed.png");
    static final Icon TEST_FAILED_ICON = Icons.load("test_failed.png");
    static final Icon GROUP_PASSED_ICON = Icons.load("group_passed.png");
    static final Icon GROUP_FAILED_ICON = Icons.load("group_failed.png");
    static final Icon SUITE_PASSED_ICON = Icons.load("suite_passed.png");
    static final Icon SUITE_FAILED_ICON = Icons.load("suite_failed.png");

    static final Observable<Icon> GRAY_SPINNER = Observable.range(1, 8)
            .map(i -> Icons.load("spinner_gray_" + i + ".png"))
            .toList()
            .flatMap(frames -> Observable.interval(125, MILLISECONDS, SwingScheduler.getInstance())
                    .map(index -> frames.get((int) (index % frames.size())))
            );

    static final Observable<Icon> RED_SPINNER = Observable.range(1, 8)
            .map(i -> Icons.load("spinner_red_" + i + ".png"))
            .toList()
            .flatMap(frames -> Observable.interval(125, MILLISECONDS, SwingScheduler.getInstance())
                    .map(index -> frames.get((int) (index % frames.size())))
            );

    private final StyledTreeNode root;
    private final DefaultTreeModel model;
    private final JTree tree;

    private Animation<Node, Icon> currentSuiteNodeIcon = Animation.create(Node::setIcon);
    private Animation<Node, Icon> currentGroupNodeIcon = Animation.create(Node::setIcon);
    private Animation<Node, Icon> currentTestNodeIcon = Animation.create(Node::setIcon);

    public GTestOutputView() {
        root = new StyledTreeNode("");
        root.setUserObject(new HashMap<String, StyledTreeNode>());

        model = new DefaultTreeModel(root);

        tree = new JTree(model);
        tree.setName(TREE_NAME);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
        tree.putClientProperty("JTree.lineStyle", "None");
        tree.setFont(new Font("monospaced", Font.PLAIN, 12));
        tree.setCellRenderer(new StyledTreeNodeRenderer());

        //todo extract crumbs-related code
        //todo left align instead of centering
        //todo scroll on press on crumb
        DefaultListModel<Object> breadCrumbsModel = new DefaultListModel<>();
        JList<Object> breadCrumbs = new JList<>(breadCrumbsModel);
        breadCrumbs.setName(CRUMBS_NAME);
        breadCrumbs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        breadCrumbs.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        breadCrumbs.setVisibleRowCount(1);
        breadCrumbs.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                StyledTreeNode node = StyledTreeNode.class.cast(value);
                super.getListCellRendererComponent(list, node, index, isSelected, cellHasFocus);

                setText(node.getDisplayName());

                if (node.getIcon() != null) {
                    setIcon(node.getIcon());
                }
                if (node.getTextColor() != null) {
                    setForeground(node.getTextColor());
                }

                return this;
            }
        });

        model.addTreeModelListener(new TreeModelListener() {
            @Override
            public void treeNodesChanged(TreeModelEvent e) {
                //todo redraw only if node is actually contained in bread crumbs
                breadCrumbs.repaint();
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e) {

            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {

            }

            @Override
            public void treeStructureChanged(TreeModelEvent e) {

            }
        });

        JScrollPane treeScroll = new JScrollPane(tree);
        treeScroll.getViewport().addChangeListener(e -> {
            JViewport viewport = JViewport.class.cast(e.getSource());

//            boolean scrolling = treeScroll.getVerticalScrollBar().isVisible();
//            breadCrumbs.setVisible(scrolling);
//            if (!scrolling) {
//                return;
//            }

            Point upperLeft = viewport.getViewPosition();
            upperLeft = viewport.toViewCoordinates(upperLeft);
            TreePath firstVisibleRow = tree.getClosestPathForLocation(upperLeft.x, upperLeft.y);

            breadCrumbsModel.clear();
            for (TreePath step = firstVisibleRow; step != null; step = step.getParentPath()) {
                if (step.getParentPath() == null) {
                    continue; //skip invisible root element
                }

                StyledTreeNode node = StyledTreeNode.class.cast(step.getLastPathComponent());
                if (node.isLeaf()) {
                    continue; //skip output lines
                }

                breadCrumbsModel.add(0, node);
            }
        });

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(breadCrumbs);
        add(treeScroll);
    }

    public void clear() {
        root.removeAllChildren();
        childrenIndex(root).clear();
        model.nodeStructureChanged(root);
    }

    public Animation<Node, Icon> getCurrentSuiteNodeIcon() {
        return currentSuiteNodeIcon;
    }

    public Animation<Node, Icon> getCurrentGroupNodeIcon() {
        return currentGroupNodeIcon;
    }

    public Animation<Node, Icon> getCurrentTestNodeIcon() {
        return currentTestNodeIcon;
    }

    public class Node {
        protected final StyledTreeNode node;

        public Node(StyledTreeNode node) {
            this.node = node;
        }

        public Node createCollapsible(String pathKey, String displayName) {
            StyledTreeNode newBranch = new StyledTreeNode(displayName);
            newBranch.setUserObject(new HashMap<String, StyledTreeNode>());
            childrenIndex(node).put(pathKey, newBranch);
            addChildNode(node, newBranch);

            return new Node(newBranch);
        }

        public Node createOutputLine(String outputLine) {
            StyledTreeNode newLeaf = new StyledTreeNode(outputLine);
            addChildNode(node, newLeaf);

            return new Node(newLeaf);
        }

        public void setDisplayName(String displayName) {
            node.setDisplayName(displayName);
            model.nodeChanged(node);
        }

        public void setIcon(Icon icon) {
            node.setIcon(icon);
            model.nodeChanged(node);
        }

        public void setTextColor(Color color) {
            node.setTextColor(color);
            model.nodeChanged(node);
        }
    }

    public Node atRoot() {
        return new Node(root);
    }

    public Node at(String... pathKeys) {
        StyledTreeNode node = root;
        for (String pathKey : pathKeys) {
            node = childrenIndex(node).get(pathKey);
            Preconditions.checkState(node != null);
        }
        return new Node(node);
    }

    private Map<String, StyledTreeNode> childrenIndex(StyledTreeNode node) {
        //noinspection unchecked
        return (Map<String, StyledTreeNode>) node.getUserObject();
    }

    private void addChildNode(StyledTreeNode parent, StyledTreeNode child) {
        model.insertNodeInto(child, parent, parent.getChildCount());
        tree.scrollPathToVisible(new TreePath(child.getPath()));
    }
}
