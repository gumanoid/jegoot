package gumanoid.ui.gtest.output;

import gumanoid.ui.gtest.output.GTestOutputTreeModel.Node;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.swing.tree.TreeModel;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
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
        public Object value;
        public final List<SimpleTree> children = new ArrayList<>();
    }

    interface ScenarioStep extends BiConsumer<TestModel, SimpleTree> {}

    @DataProvider(name = "scenarios")
    Object[][] scenarios() {
        return new ScenarioStep[][] {
                {  }
        };
    }

    @Test(dataProvider = "scenarios")
    void testScenario(ScenarioStep... scenario) throws Exception {
        TestModel model = new TestModel();
        SimpleTree tree = new SimpleTree();

        for (ScenarioStep step : scenario) {
            step.accept(model, tree);
            checkInterfaceIntegrity(model);
            checkImplementationIntegrity(model);
            checkEquals(model, tree);
        }
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
            assertEquals(model.getIndexOfChild(node, model.getChild(node, i)), i);
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

            for (int i = 0; i < treeNode.children.size(); ++i) {
                treeNodes.push(treeNode.children.get(i));
            }
        } while (!modelNodes.isEmpty());
    }

    void checkEquals(Node modelNode, SimpleTree treeNode) {
        assertEquals(modelNode.getValue(), treeNode.value);
        assertEquals(modelNode.childCount(), treeNode.children.size());
    }
}