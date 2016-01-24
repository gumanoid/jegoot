package gumanoid.ui.gtest.output;

import com.google.common.annotations.VisibleForTesting;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

/**
 * Created by Gumanoid on 09.01.2016.
 */
public class GTestOutputView extends JPanel {
    @VisibleForTesting
    public static final String TREE_NAME = "GTest_output_tree";

    @VisibleForTesting
    public static final String CRUMBS_NAME = "GTest_output_tree_crumbs";

    private final JTree tree;
    private final JScrollPane treeScroll;
    private final JList<TreePath> crumbs;

    public GTestOutputView() {
        tree = new JTree();
        tree.setName(TREE_NAME);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
        tree.putClientProperty("JTree.lineStyle", "None");
        tree.setFont(new Font("monospaced", Font.PLAIN, 12));
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            {
                setLeafIcon(null);
                setClosedIcon(null);
                setOpenIcon(null);
            }

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                GTestOutputTreeModel.Node<GTestOutputRow> node = GTestOutputTreeModel.node(value);
                GTestOutputRow style = node.getValue();

                //todo sometimes text is ellipsed, why?
                super.getTreeCellRendererComponent(tree, style.getDisplayName(), sel, expanded, leaf, row, hasFocus);

                setText(style.getDisplayName());

                if (style.getIcon() != null) {
                    setIcon(style.getIcon());
                }
                if (style.getTextColor() != null) {
                    setForeground(style.getTextColor());
                }

                return this;
            }
        });

        //todo provide size hint so that crumb is fit to it contents
        //todo add spacers between items
        crumbs = new JList<>();
        crumbs.setName(CRUMBS_NAME);
        crumbs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        crumbs.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        crumbs.setVisibleRowCount(1);
        crumbs.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                TreePath path = TreePath.class.cast(value);
                GTestOutputTreeModel.Node<GTestOutputRow> node = GTestOutputTreeModel.node(path.getLastPathComponent());
                GTestOutputRow row = node.getValue();

                super.getListCellRendererComponent(list, row.getDisplayName(), index, isSelected, cellHasFocus);

                setText(row.getDisplayName());

                if (row.getIcon() != null) {
                    setIcon(row.getIcon());
                }
                if (row.getTextColor() != null) {
                    setForeground(row.getTextColor());
                }

                setSize(getPreferredSize());

                return this;
            }
        });

        treeScroll = new JScrollPane(tree);

        setLayout(new BorderLayout());
        add(crumbs, BorderLayout.NORTH);
        add(treeScroll, BorderLayout.CENTER);
    }

    JTree getTree() {
        return tree;
    }

    JScrollPane getTreeScroll() {
        return treeScroll;
    }

    JList<TreePath> getCrumbs() {
        return crumbs;
    }
}
