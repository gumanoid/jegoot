package gumanoid.ui.gtest.output;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Map;

/**
 * Created by Gumanoid on 09.01.2016.
 */
public class GTestOutputView extends JScrollPane {
    private final DefaultMutableTreeNode root;

    public GTestOutputView() {
        super(new JTree(new DefaultMutableTreeNode(new GTestOutputTreeIndex(""))));
        JTree tree = (JTree) getViewport().getView();
        tree.setName("GTest_output_tree");
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setFont(new Font("monospaced", Font.PLAIN, 12));
        root = (DefaultMutableTreeNode) tree.getModel().getRoot();
    }

    public void clear() {
        JTree tree = (JTree) getViewport().getView();
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        root.removeAllChildren();
        model.nodeStructureChanged(root);
    }

    public void appendCollapsible(String displayName, String... pathKeys) {
        String newBranchKey = pathKeys[pathKeys.length - 1];
        DefaultMutableTreeNode newBranch = new DefaultMutableTreeNode(new GTestOutputTreeIndex(displayName));

        DefaultMutableTreeNode node = root;
        for (int i = 0; i + 1 < pathKeys.length; i++) {
            node = childrenIndex(node).get(pathKeys[i]);
        }

        childrenIndex(node).put(newBranchKey, newBranch);
        addChildNode(node, newBranch);
    }

    public void append(String outputLine, String... pathKeys) {
        DefaultMutableTreeNode node = root;
        for (int i = 0; i < pathKeys.length; i++) {
            node = childrenIndex(node).get(pathKeys[i]);
        }
        addChildNode(node, new DefaultMutableTreeNode(outputLine));
    }

    private Map<String, DefaultMutableTreeNode> childrenIndex(DefaultMutableTreeNode node) {
        return GTestOutputTreeIndex.class.cast(node.getUserObject())
                .getChildrenByKey();
    }

    private void addChildNode(DefaultMutableTreeNode parent, DefaultMutableTreeNode child) {
        JTree tree = (JTree) getViewport().getView();
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.insertNodeInto(child, parent, parent.getChildCount());
        tree.scrollPathToVisible(new TreePath(child.getPath()));
    }
}
