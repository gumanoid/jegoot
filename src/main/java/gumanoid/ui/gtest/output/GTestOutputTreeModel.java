package gumanoid.ui.gtest.output;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Model of GTest output
 *
 * Created by Gumanoid on 24.01.2016.
 */
public class GTestOutputTreeModel<T> implements TreeModel {
    public interface Node<T> {
        Node<T> getParent();
        T getValue();
        boolean isLeaf();
        int childCount();
        Node<T> childAt(int index);
        int indexOf(Node<T> child);
    }

    public interface BranchNode<T> extends Node<T> {}

    private final List<TreeModelListener> listeners = new ArrayList<>();

    private final Map<String, BranchNodeImpl<T>> structureIndex = new HashMap<>();

    private final BranchNodeImpl<T> root;
    private BranchNodeWithQueue<T> suite;
    private BranchNodeImpl<T> summary;

    public GTestOutputTreeModel(T rootValue) {
        root = new BranchNodeImpl<>(null, rootValue);
    }

    public void clear() {
        root.children.clear();
        structureIndex.clear();
        suite = null;
        summary = null;

        fireEvent(new TreeModelEvent(this, new Object[] { root }), TreeModelListener::treeStructureChanged);
    }

    public BranchNode<T> rootNode() {
        return root;
    }

    public BranchNode<T> suiteNode() {
        return suite;
    }

    public BranchNode<T> summaryNode() {
        return summary;
    }

    public BranchNode<T> groupNode(String groupKey) {
        return structureIndex.get(groupKey);
    }

    public BranchNode<T> testNode(String groupKey, String testKey) {
        return structureIndex.get(groupKey + "." + testKey);
    }

    public void queueGroup(String groupKey, T groupValue) {
        if (!structureIndex.containsKey(groupKey)) {
            BranchNodeWithQueue<T> group = new BranchNodeWithQueue<>(suite, groupValue);
            structureIndex.put(groupKey, group);
            queueNode(suite, group);
        }
    }

    public void queueTest(String groupKey, String testKey, T testValue) {
        String key = groupKey + "." + testKey;
        if (!structureIndex.containsKey(key)) {
            BranchNodeWithQueue<T> group = (BranchNodeWithQueue<T>) structureIndex.get(groupKey);
            BranchNodeWithQueue<T> test = new BranchNodeWithQueue<>(group, testValue);
            structureIndex.put(key, test);
            queueNode(group, test);
        }
    }

    public BranchNode<T> addSuite(T suiteValue) {
        suite = new BranchNodeWithQueue<>(root, suiteValue);
        appendNode(root, suite);
        return suite;
    }

    public BranchNode<T> addSummary(T summaryValue) {
        summary = new BranchNodeImpl<>(root, summaryValue);
        appendNode(root, summary);
        return summary;
    }

    public BranchNode<T> addGroup(String groupKey, T groupValue) {
        return addOrMoveBranch(groupKey, suite, groupValue);
    }

    public BranchNode<T> addTest(String groupKey, String testKey, T testValue) {
        String key = groupKey + "." + testKey;
        BranchNodeImpl<T> group = structureIndex.get(groupKey);
        return addOrMoveBranch(key, group, testValue);
    }

    public Node<T> addOutput(BranchNode<T> parent, T output) {
        BranchNodeImpl<T> impl = (BranchNodeImpl<T>) parent;
        LeafNode<T> leaf = new LeafNode<>(impl, output);
        appendNode(impl, leaf);
        return leaf;
    }

    public static <T> Node<T> node(Object rawNode) {
        //noinspection unchecked
        return Node.class.cast(rawNode);
    }

    private BranchNode<T> addOrMoveBranch(String key, BranchNodeImpl<T> parent, T value) {
        BranchNodeImpl<T> test = structureIndex.get(key);
        if (test != null) {
            removeNode(parent, test);
            test.setValue(value);
            appendNode(parent, test);
        } else {
            test = new BranchNodeWithQueue<>(parent, value);
            structureIndex.put(key, test);
            appendNode(parent, test);
        }
        return test;
    }

    private void queueNode(BranchNodeWithQueue<T> parent, Node<T> child) {
        int index = parent.childCount();
        TreeModelEvent event = new TreeModelEvent(
                GTestOutputTreeModel.this,
                new Path(parent),
                new int[] { index },
                new Object[] { child }
        );

        parent.queue(child);
        fireEvent(event, TreeModelListener::treeNodesInserted);
    }

    private void appendNode(BranchNodeImpl<T> parent, Node<T> child) {
            int index = parent.childCount();
            TreeModelEvent event = new TreeModelEvent(
                    GTestOutputTreeModel.this,
                    new Path(parent),
                    new int[] { index },
                    new Object[] { child }
            );

            parent.add(child);
            fireEvent(event, TreeModelListener::treeNodesInserted);
    }

    private void removeNode(BranchNodeImpl<T> parent, Node<T> child) {
            int index = BranchNodeWithQueue.class.cast(parent).take(child);
            TreeModelEvent event = new TreeModelEvent(
                    GTestOutputTreeModel.this,
                    new Path(parent),
                    new int[] { index },
                    new Object[] { child }
            );
            fireEvent(event, TreeModelListener::treeNodesRemoved);
    }

    private static class LeafNode<T> implements Node<T> {
        private final Node<T> parent;
        private final T value;

        private LeafNode(Node<T> parent, T value) {
            this.parent = parent;
            this.value = value;
        }

        @Override
        public Node<T> getParent() {
            return parent;
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public boolean isLeaf() {
            return true;
        }

        @Override
        public int childCount() {
            return 0;
        }

        @Override
        public Node<T> childAt(int index) {
            return null;
        }

        @Override
        public int indexOf(Node<T> child) {
            return -1;
        }
    }

    private static class BranchNodeImpl<T> implements BranchNode<T> {
        private final Node<T> parent;
        private T value;
        private final List<Node<T>> children = new ArrayList<>();

        private BranchNodeImpl(Node<T> parent, T value) {
            this.parent = parent;
            this.value = value;
        }

        @Override
        public Node<T> getParent() {
            return parent;
        }

        @Override
        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        @Override
        public int childCount() {
            return children.size();
        }

        @Override
        public Node<T> childAt(int index) {
            return children.get(index);
        }

        @Override
        public int indexOf(Node<T> child) {
            return children.indexOf(child);
        }

        public void add(Node<T> child) {
            children.add(child);
        }
    }

    private static class BranchNodeWithQueue<T> extends BranchNodeImpl<T> {
        private final List<Node<T>> queued = new ArrayList<>();

        private BranchNodeWithQueue(Node<T> parent, T value) {
            super(parent, value);
        }

        @Override
        public int childCount() {
            return super.childCount() + queued.size();
        }

        @Override
        public Node<T> childAt(int index) {
            int count = super.childCount();
            return index < count
                    ? super.childAt(index)
                    : queued.get(index - count);
        }

        @Override
        public int indexOf(Node<T> child) {
            int index = super.indexOf(child);
            return index != -1 ? index : queued.indexOf(child);
        }

        public void queue(Node<T> child) {
            queued.add(child);
        }

        public int take(Node<T> child) {
            int indexInQueue = queued.indexOf(child);
            queued.remove(indexInQueue);
            return indexInQueue + super.childCount();
        }
    }

    private <E> void fireEvent(E event, BiConsumer<TreeModelListener, E> method) {
        for (TreeModelListener listener : listeners) {
            method.accept(listener, event);
        }
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public int getChildCount(Object parent) {
        return node(parent).childCount();
    }

    @Override
    public Object getChild(Object parent, int index) {
        return node(parent).childAt(index);
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        //todo check that parent & child are belong to this model, as documented in TreeModel interface
        if (parent == null) return -1;
        if (child == null) return -1;
        return node(parent).indexOf(node(child));
    }

    @Override
    public boolean isLeaf(Object node) {
        return node(node).isLeaf();
    }

    public void nodeUpdated(Node node) {
        valueForPathChanged(new Path(node), node);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        fireEvent(new TreeModelEvent(this, path), TreeModelListener::treeNodesChanged);
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }

    private static class Path extends TreePath {
        public Path(Node node) {
            super(node.getParent() != null ? new Path(node.getParent()) : null, node);
        }
    }
}