package gumanoid.ui.gtest.output;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import gumanoid.ui.Icons;
import gumanoid.ui.tree.StyledTreeNode;
import gumanoid.ui.tree.StyledTreeNodeRenderer;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gumanoid on 09.01.2016.
 */
public class GTestOutputView extends JScrollPane {
    //todo breadcrumbs
    //todo highlight background of children of selected branch
    //to simplify figuring out when test output is finished

    @VisibleForTesting
    public static final String TREE_NAME = "GTest_output_tree";

    static final Icon TEST_PASSED_ICON = Icons.load("test_passed.png");
    static final Icon TEST_FAILED_ICON = Icons.load("test_failed.png");
    static final Icon GROUP_PASSED_ICON = Icons.load("group_passed.png");
    static final Icon GROUP_FAILED_ICON = Icons.load("group_failed.png");
    static final Icon SUITE_PASSED_ICON = Icons.load("suite_passed.png");
    static final Icon SUITE_FAILED_ICON = Icons.load("suite_failed.png");

    private final StyledTreeNode root;
    private final DefaultTreeModel model;
    private final JTree tree;

    public GTestOutputView() {
        super(new JTree(new StyledTreeNode("")));

        tree = JTree.class.cast(getViewport().getView());
        model = DefaultTreeModel.class.cast(tree.getModel());
        root = StyledTreeNode.class.cast(model.getRoot());

        root.setUserObject(new HashMap<String, StyledTreeNode>());

        tree.setName(TREE_NAME);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.putClientProperty("JTree.lineStyle", "None");
        tree.setFont(new Font("monospaced", Font.PLAIN, 12));
        tree.setCellRenderer(new StyledTreeNodeRenderer());

        tree.addTreeSelectionListener(e -> {

        });
    }

    public void clear() {
        root.removeAllChildren();
        childrenIndex(root).clear();
        model.nodeStructureChanged(root);
    }

    public void setHighlighted(Item item, boolean isHighlighted) {

    }

    public class Item {
        //todo naming

        protected final StyledTreeNode node;

        public Item(StyledTreeNode node) {
            this.node = node;
        }

        public Item createCollapsible(String pathKey, String displayName) {
            StyledTreeNode newBranch = new StyledTreeNode(displayName);
            newBranch.setUserObject(new HashMap<String, StyledTreeNode>());
            childrenIndex(node).put(pathKey, newBranch);
            addChildNode(node, newBranch);

            return new Item(newBranch);
        }

        public Item createOutputLine(String outputLine) {
            StyledTreeNode newLeaf = new StyledTreeNode(outputLine);
            addChildNode(node, newLeaf);

            return new Item(newLeaf);
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

    public Item atRoot() {
        return new Item(root);
    }

    public Item at(String... pathKeys) {
        StyledTreeNode node = root;
        for (String pathKey : pathKeys) {
            node = childrenIndex(node).get(pathKey);
            Preconditions.checkState(node != null);
        }
        return new Item(node);
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
