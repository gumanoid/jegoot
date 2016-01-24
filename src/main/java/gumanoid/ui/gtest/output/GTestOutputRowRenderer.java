package gumanoid.ui.gtest.output;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * Created by Gumanoid on 13.01.2016.
 */
public class GTestOutputRowRenderer extends DefaultTreeCellRenderer {
    public GTestOutputRowRenderer() {
        setLeafIcon(null);
        setClosedIcon(null);
        setOpenIcon(null);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        GTestOutputTreeModel.Node<GTestOutputRow> node = GTestOutputTreeModel.node(value);
        GTestOutputRow style = node.getValue();

        super.getTreeCellRendererComponent(tree, style.getDisplayName(), sel, expanded, leaf, row, hasFocus);

        if (style.getIcon() != null) {
            setIcon(style.getIcon());
        }
        if (style.getTextColor() != null) {
            setForeground(style.getTextColor());
        }

        return this;
    }
}
