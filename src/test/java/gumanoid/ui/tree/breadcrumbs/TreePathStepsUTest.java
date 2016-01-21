package gumanoid.ui.tree.breadcrumbs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

@Test
public class TreePathStepsUTest {
    @DataProvider(name = "dataForCreate")
    Object[][] dataForCreate() {
        return new Object[][]{
                { },
                { "foo" },
                { "foo", "bar" },
                { "foo", "bar", "baz" },
                { "foo", "bar", "baz", "qux" },
        };
    }

    @Test(dataProvider = "dataForCreate")
    void testCreate(Object... source) throws Exception {
        TreePath treePath = source.length == 0 ? null : new TreePath(source);
        TreePathSteps steps = new TreePathSteps(treePath);
        Object[] result = new Object[steps.getStepCount()];
        for (int i = 0; i < steps.getStepCount(); ++i) {
            result[i] = steps.getPathAt(i).getLastPathComponent();
        }
        assertEquals(result, source);
    }

    @DataProvider(name = "dataForDiff")
    Object[][] dataForDiff() {
        return new Object[][]{
                {
                        ImmutableList.of(),
                        ImmutableList.of(),
                        0
                },
                {
                        ImmutableList.of("foo"),
                        ImmutableList.of(),
                        0
                },
                {
                        ImmutableList.of(),
                        ImmutableList.of("foo"),
                        0
                },
                {
                        ImmutableList.of("foo"),
                        ImmutableList.of("foo"),
                        1
                },
                {
                        ImmutableList.of("foo"),
                        ImmutableList.of("bar"),
                        0
                },
                {
                        ImmutableList.of("foo", "bar"),
                        ImmutableList.of("foo"),
                        1
                },
                {
                        ImmutableList.of("foo"),
                        ImmutableList.of("foo", "bar"),
                        1
                },
                {
                        ImmutableList.of("foo", "bar"),
                        ImmutableList.of("baz"),
                        0
                },
                {
                        ImmutableList.of("foo"),
                        ImmutableList.of("bar", "baz"),
                        0
                },
                {
                        ImmutableList.of("foo", "bar"),
                        ImmutableList.of("foo", "bar"),
                        2
                },
                {
                        ImmutableList.of("foo", "bar"),
                        ImmutableList.of("foo", "baz"),
                        1
                },
                {
                        ImmutableList.of("foo", "bar", "baz"),
                        ImmutableList.of("foo", "bar"),
                        2
                },
                {
                        ImmutableList.of("foo", "bar"),
                        ImmutableList.of("foo", "bar", "baz"),
                        2
                },
                {
                        ImmutableList.of("foo", "bar"),
                        ImmutableList.of("foo", "baz", "qux"),
                        1
                },
                {
                        ImmutableList.of("foo", "bar", "baz"),
                        ImmutableList.of("foo", "baz", "foobar"),
                        1
                },
        };
    }

    @Test(dataProvider = "dataForDiff")
    void testGetDiffWith(List<String> steps, List<String> anotherSteps, int expectedMismatchIndex) throws Exception {
        TreePath leftPath = steps.isEmpty() ? null : new TreePath(steps.toArray());
        TreePath rightPath = anotherSteps.isEmpty() ? null : new TreePath(anotherSteps.toArray());

        TreePathSteps left = new TreePathSteps(leftPath);
        TreePathSteps right = new TreePathSteps(rightPath);

        TreePathSteps.Diff diff = left.getDiffWith(right);
        List<Object> toRemove = Lists.transform(diff.remove, TreePath::getLastPathComponent);
        List<Object> toAdd = Lists.transform(diff.add, TreePath::getLastPathComponent);

        List<Object> list = new ArrayList<>(steps);
        for (int i = 0; !list.isEmpty() && i < toRemove.size(); i++) {
            list.remove(list.size() - 1);
        }
        list.addAll(toAdd);

        //straightforward check
        assertEquals(list, anotherSteps, list + " != " + anotherSteps);

        //additional check
        assertEquals(toRemove, steps.subList(steps.size() - toRemove.size(), steps.size()));
        assertEquals(toAdd, anotherSteps.subList(anotherSteps.size() - toAdd.size(), anotherSteps.size()));

        //check test correctness
        assertEquals(expectedMismatchIndex, steps.size() - toRemove.size(), "Incorrect test");
        assertEquals(expectedMismatchIndex, anotherSteps.size() - toAdd.size(), "Incorrect test");
    }

    @Test(dataProvider = "dataForDiff")
    void testGetFirstMismatchWith(List<String> steps, List<String> anotherSteps, int expectedMismatchIndex) throws Exception {
        TreePathSteps left = new TreePathSteps(steps.isEmpty() ? null : new TreePath(steps.toArray()));
        TreePathSteps right = new TreePathSteps(anotherSteps.isEmpty() ? null : new TreePath(anotherSteps.toArray()));
        int actualMismatchIndex = left.getFirstMismatchWith(right);

        List<String> list = new ArrayList<>(steps);
        while (list.size() > actualMismatchIndex) {
            list.remove(list.size() - 1);
        }
        for (int i = actualMismatchIndex; i < anotherSteps.size(); ++i) {
            list.add(anotherSteps.get(i));
        }

        //straightforward check
        assertEquals(list, anotherSteps, list + " != " + anotherSteps);

        //check test correctness
        assertEquals(actualMismatchIndex, expectedMismatchIndex, "Incorrect test");
    }
}