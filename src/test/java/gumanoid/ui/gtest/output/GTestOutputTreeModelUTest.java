package gumanoid.ui.gtest.output;

import gumanoid.ui.gtest.output.GTestOutputTreeModel.Node;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.swing.tree.TreeModel;
import java.util.*;
import java.util.function.BiConsumer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

@Test
public class GTestOutputTreeModelUTest {
    static class TestModel extends GTestOutputTreeModel<String> {
        public TestModel() {
            super(null);
        }
    }

    static class SimpleTree {
        final String key;
        Object value;
        final List<SimpleTree> list = new ArrayList<>();
        final Map<String, SimpleTree> map = new HashMap<>();

        SimpleTree(String key) {
            this.key = key;
        }

        SimpleTree setValue(Object value) {
            this.value = value;
            return this;
        }

        int childrenCount() {
            return list.size();
        }

        SimpleTree child(int index) {
            return list.get(index);
        }

        SimpleTree child(String key) {
            return map.get(key);
        }

        SimpleTree add(SimpleTree child) {
            return add(list.size(), child);
        }

        SimpleTree add(int index, SimpleTree child) {
            list.add(index, child);
            map.put(child.key, child);
            return child;
        }

        SimpleTree remove(int index) {
            SimpleTree result = list.remove(index);
            map.remove(result.key);
            return result;
        }

        SimpleTree remove(String key) {
            SimpleTree result = map.remove(key);
            list.remove(result);
            return result;
        }

        SimpleTree moveChild(int fromIndex, int toIndex) {
            SimpleTree result = list.remove(fromIndex);
            list.add(toIndex, result);
            return result;
        }
    }

    interface ScenarioStep extends BiConsumer<TestModel, SimpleTree> {
        default ScenarioStep named(String name) {
            ScenarioStep self = this;
            return new ScenarioStep() {
                @Override
                public void accept(TestModel model, SimpleTree tree) {
                    self.accept(model, tree);
                }

                @Override
                public String toString() {
                    return name;
                }
            };
        }
    }

    @DataProvider(name = "scenarios")
    Object[][] scenarios() {
        return new ScenarioStep[][] {
                {},
                {
                        queueSuite("Suite").named("queue suite")
                },
                {
                        insertSuite(0, "Suite").named("add suite")
                },
                {
                        queueSuite("Suite").named("queue suite"),
                        queueGroup("group1", "Group 1").named("queue group 1"),
                        queueTest("group1", "test1", "Test 1").named("queue test 1"),

                        moveSuite(0, 0, "Suite with 1 test in 1 group").named("move suite"),
                        insertSuiteOutput(0, "Global test environment set-up").named("test env up"),
                        moveGroup(1, 1, "Group with 1 test").named("move group"),
                        moveTest("group1", 0, 0, "Test 1.").named("move test")
                },
                {
                        queueSuite("Suite").named("queue suite"),
                        queueGroup("group1", "Group 1").named("queue group 1"),
                        queueTest("group1", "test1", "Test 1").named("queue test 1"),

                        insertRootOutput(0, "note: --gtest_filter").named("filter note"),
                        moveSuite(1, 1, "Suite with 1 test in 1 group").named("move suite"),
                        insertSuiteOutput(0, "Global test environment set-up").named("test env up"),
                        moveGroup(1, 1, "Group with 1 test").named("move group"),
                        moveTest("group1", 0, 0, "Test 1.").named("move test")
                }
        };
    }

    @Test(dataProvider = "scenarios")
    void testScenario(ScenarioStep... scenario) throws Exception {
        TestModel model = new TestModel();
        SimpleTree tree = new SimpleTree(null);

        for (ScenarioStep step : scenario) {
            System.out.println(step);
            step.accept(model, tree);
            checkInterfaceIntegrity(model);
            checkImplementationIntegrity(model);
            checkEquals(model, tree);
        }
    }

    ScenarioStep queueSuite(String suite) {
        return (model, tree) -> {
            model.queueSuite(suite);
            tree.add(new SimpleTree("suite").setValue(suite));
        };
    }

    ScenarioStep insertSuite(int index, String suite) {
        return (model, tree) -> {
            model.addSuite(suite);
            tree.add(index, new SimpleTree("suite").setValue(suite));
        };
    }

    ScenarioStep moveSuite(int fromIndex, int toIndex, String newValue) {
        return (model, tree) -> {
            model.addSuite(newValue);
            tree.moveChild(fromIndex, toIndex).setValue(newValue);
        };
    }

    ScenarioStep queueGroup(String key, String value) {
        return (model, tree) -> {
            model.queueGroup(key, value);
            tree.child("suite").add(new SimpleTree(key).setValue(value));
        };
    }

    ScenarioStep insertGroup(int index, String key, String value) {
        return (model, tree) -> {
            model.addGroup(key, value);
            tree.child("suite").add(index, new SimpleTree(key).setValue(value));
        };
    }

    ScenarioStep moveGroup(int fromIndex, int toIndex, String newValue) {
        return (model, tree) -> {
            SimpleTree group = tree.child("suite").moveChild(fromIndex, toIndex);
            group.setValue(newValue);
            model.addGroup(group.key, newValue);
        };
    }

    ScenarioStep queueTest(String groupKey, String testKey, String value) {
        return (model, tree) -> {
            model.queueTest(groupKey, testKey, value);
            tree.child("suite").child(groupKey).add(new SimpleTree(testKey).setValue(value));
        };
    }

    ScenarioStep insertTest(int index, String groupKey, String testKey, String value) {
        return (model, tree) -> {
            model.addTest(groupKey, testKey, value);
            tree.child("suite").child(groupKey).add(index, new SimpleTree(testKey).setValue(value));
        };
    }

    ScenarioStep moveTest(String groupKey, int fromIndex, int toIndex, String newValue) {
        return (model, tree) -> {
            SimpleTree test = tree.child("suite").child(groupKey).moveChild(fromIndex, toIndex);
            test.setValue(newValue);
            model.addTest(groupKey, test.key, newValue);
        };
    }

    ScenarioStep addSummary(String summary) {
        return (model, tree) -> {
            tree.add(new SimpleTree("summary").setValue(summary));
        };
    }

    ScenarioStep insertRootOutput(int index, String output) {
        return (model, tree) -> {
            model.addOutput(model.rootNode(), output);
            String key = String.valueOf(tree.childrenCount());
            tree.add(index, new SimpleTree(key).setValue(output));
        };
    }

    ScenarioStep insertSuiteOutput(int index, String output) {
        return (model, tree) -> {
            model.addOutput(model.suiteNode(), output);
            SimpleTree parent = tree.child("suite");
            String key = String.valueOf(parent.childrenCount());
            parent.add(index, new SimpleTree(key).setValue(output));
        };
    }

    ScenarioStep addSummaryOutput(int index, String output) {
        return (model, tree) -> {
            model.addOutput(model.summaryNode(), output);
            SimpleTree parent = tree.child("summary");
            String key = String.valueOf(parent.childrenCount());
            parent.add(index, new SimpleTree(key).setValue(output));
        };
    }

    ScenarioStep insertGroupOutput(String groupKey, int index, String output) {
        return (model, tree) -> {
            model.addOutput(model.suiteNode(), output);
            SimpleTree parent = tree.child("suite").child(groupKey);
            String key = String.valueOf(parent.childrenCount());
            parent.add(index, new SimpleTree(key).setValue(output));
        };
    }

    ScenarioStep insertTestOutput(String groupKey, String testKey, int index, String output) {
        return (model, tree) -> {
            model.addOutput(model.suiteNode(), output);
            SimpleTree parent = tree.child("suite").child(groupKey).child(testKey);
            String key = String.valueOf(parent.childrenCount());
            parent.add(index, new SimpleTree(key).setValue(output));
        };
    }

    void checkInterfaceIntegrity(TreeModel model) {
        Deque<Object> deque = new LinkedList<>();
        deque.push(model.getRoot());

        do {
            Object node = deque.poll();
            checkInterfaceIntegrity(node, model);
            for (int i = 0; i < model.getChildCount(node); ++i) {
                deque.push(model.getChild(node, i));
            }
        } while (!deque.isEmpty());
    }

    void checkInterfaceIntegrity(Object node, TreeModel model) {
        assertEquals(model.isLeaf(node), model.getChildCount(node) == 0);
        for (int i = 0; i < model.getChildCount(node); ++i) {
            Object child = model.getChild(node, i);
            assertEquals(model.getIndexOfChild(node, child), i, "Node: " + Node.class.cast(child).getValue().toString());
        }
    }

    void checkImplementationIntegrity(TestModel model) {
        Deque<Node> deque = new LinkedList<>();
        deque.push(model.rootNode());

        do {
            Node node = deque.poll();
            checkImplementationIntegrity(node);
            for (int i = 0; i < node.childCount(); ++i) {
                deque.push(node.childAt(i));
            }
        } while (!deque.isEmpty());
    }

    void checkImplementationIntegrity(Node node) {
        assertEquals(node.isLeaf(), node.childCount() == 0);
        for (int i = 0; i < node.childCount(); ++i) {
            assertEquals(node.indexOf(node.childAt(i)), i);
        }

        Node parent = node.getParent();
        if (null != parent) {
            assertSame(parent.childAt(parent.indexOf(node)), node);
        }
    }

    void checkEquals(TestModel model, SimpleTree tree) {
        Deque<Node> modelNodes = new LinkedList<>();
        modelNodes.push(model.rootNode());

        Deque<SimpleTree> treeNodes = new LinkedList<>();
        treeNodes.push(tree);

        do {
            Node modelNode = modelNodes.poll();
            SimpleTree treeNode = treeNodes.poll();

            checkEquals(modelNode, treeNode);

            for (int i = 0; i < modelNode.childCount(); ++i) {
                modelNodes.push(modelNode.childAt(i));
            }

            for (int i = 0; i < treeNode.childrenCount(); ++i) {
                treeNodes.push(treeNode.child(i));
            }
        } while (!modelNodes.isEmpty());
    }

    void checkEquals(Node modelNode, SimpleTree treeNode) {
        assertEquals(modelNode.getValue(), treeNode.value);
        assertEquals(modelNode.childCount(), treeNode.childrenCount());
    }
}