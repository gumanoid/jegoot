package gumanoid.ui.gtest.output;

import com.google.common.annotations.VisibleForTesting;
import gumanoid.ui.Icons;
import rx.Observable;
import rx.schedulers.SwingScheduler;

import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

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

    private final JTree tree;

    public GTestOutputView() {
        tree = new JTree();
        tree.setName(TREE_NAME);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
        tree.putClientProperty("JTree.lineStyle", "None");
        tree.setFont(new Font("monospaced", Font.PLAIN, 12));
        tree.setCellRenderer(new GTestOutputRowRenderer());

        //TODO extract crumbs-related code
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
                GTestOutputTreeModel.Node<GTestOutputRow> node = GTestOutputTreeModel.node(value);
                GTestOutputRow row = node.getValue();

                super.getListCellRendererComponent(list, row.getDisplayName(), index, isSelected, cellHasFocus);

                setText(row.getDisplayName());

                if (row.getIcon() != null) {
                    setIcon(row.getIcon());
                }
                if (row.getTextColor() != null) {
                    setForeground(row.getTextColor());
                }

                return this;
            }
        });

//        model.addTreeModelListener(new TreeModelListener() {
//            @Override
//            public void treeNodesChanged(TreeModelEvent e) {
//                //todo redraw only if node is actually contained in bread crumbs
//                breadCrumbs.repaint();
//            }
//
//            @Override
//            public void treeNodesInserted(TreeModelEvent e) {
//                tree.scrollPathToVisible(e.getPath());
//            }
//
//            @Override
//            public void treeNodesRemoved(TreeModelEvent e) {
//
//            }
//
//            @Override
//            public void treeStructureChanged(TreeModelEvent e) {
//
//            }
//        });

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

                GTestOutputTreeModel.Node node = GTestOutputTreeModel.Node.class.cast(step.getLastPathComponent());
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

    public JTree getTree() {
        return tree;
    }
}
