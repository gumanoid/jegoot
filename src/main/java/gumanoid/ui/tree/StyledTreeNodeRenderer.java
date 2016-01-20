package gumanoid.ui.tree;

import gumanoid.ui.tree.StyledTreeNode;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * Created by Gumanoid on 13.01.2016.
 */
public class StyledTreeNodeRenderer extends DefaultTreeCellRenderer {
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        StyledTreeNode node = StyledTreeNode.class.cast(value);
        if (node.getIcon() != null) {
            setIcon(node.getIcon());
        }
        if (node.getTextColor() != null) {
            setForeground(node.getTextColor());
        }

        return this;
    }
}
