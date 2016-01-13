package gumanoid.ui.gtest.output;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

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
    //todo also add raw output lines under collapsibles

    @VisibleForTesting
    static final String TREE_NAME = "GTest_output_tree";

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
    }

    public void clear() {
        root.removeAllChildren();
        childrenIndex(root).clear();
        model.nodeStructureChanged(root);
    }

    public class StyleOpsAtPath {
        protected final StyledTreeNode node;

        public StyleOpsAtPath(StyledTreeNode node) {
            this.node = node;
        }

        public StyleOpsAtPath setIcon(Icon icon) {
            node.setIcon(icon);
            model.nodeChanged(node);

            return this;
        }

        public StyleOpsAtPath setTextColor(Color color) {
            node.setTextColor(color);
            model.nodeChanged(node);

            return this;
        }
    }

    public class OpsAtPath extends StyleOpsAtPath {
        public OpsAtPath(StyledTreeNode node) {
            super(node);
        }

        public StyleOpsAtPath addCollapsible(String pathKey, String displayName) {
            StyledTreeNode newBranch = new StyledTreeNode(displayName);
            newBranch.setUserObject(new HashMap<String, StyledTreeNode>());
            childrenIndex(node).put(pathKey, newBranch);
            addChildNode(node, newBranch);

            return new StyleOpsAtPath(newBranch);
        }

        public StyleOpsAtPath addOutputLine(String outputLine) {
            StyledTreeNode newLeaf = new StyledTreeNode(outputLine);
            addChildNode(node, newLeaf);

            return new StyleOpsAtPath(newLeaf);
        }
    }

    public OpsAtPath atRoot() {
        return new OpsAtPath(root);
    }

    public OpsAtPath at(String... pathKeys) {
        StyledTreeNode node = root;
        for (String pathKey : pathKeys) {
            node = childrenIndex(node).get(pathKey);
            Preconditions.checkState(node != null);
        }
        return new OpsAtPath(node);
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
