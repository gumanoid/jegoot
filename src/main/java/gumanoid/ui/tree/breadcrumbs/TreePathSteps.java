/*
 * Copyright (c) 2008-2016 Maxifier Ltd. All Rights Reserved.
 */
package gumanoid.ui.tree.breadcrumbs;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * BreadCrumbsModel
 *
 * @author Kirill Gamazkov (kirill.gamazkov@maxifier.com) (2016-01-21 17:29)
 */
public class TreePathSteps {
    public class Diff {
        public final int firstMismatchIndex;
        public final List<TreePath> remove;
        public final List<TreePath> add;

        public Diff(int firstMismatchIndex, List<TreePath> remove, List<TreePath> add) {
            this.firstMismatchIndex = firstMismatchIndex;
            this.remove = remove;
            this.add = add;
        }
    }

    /**
     * Materialized path steps in reverse order
     */
    private final List<TreePath> steps;

    public TreePathSteps() {
        steps = Collections.emptyList();
    }

    public TreePathSteps(TreePath pathTip) {
        steps = split(pathTip);
    }

    public TreePath getPathAt(int index) {
        return steps.get(steps.size() - index - 1);
    }

    public int getStepCount() {
        return steps.size();
    }

    /**
     * Returns a difference between own path and <code>another</code>,
     *
     * @param another    path to compare with
     * @return a {@link Diff} object, i. e. range of path steps to be removed and range of path steps to
     * be added in order for own path to be equal to <code>another</code>
     */
    public Diff getDiffWith(TreePathSteps another) {
        int firstMismatch = getFirstMismatchWith(another);
        Diff result = new Diff(
                firstMismatch,
                new ArrayList<>(getStepCount() - firstMismatch),
                new ArrayList<>(another.getStepCount() - firstMismatch)
        );
        for (int i = firstMismatch; i < getStepCount(); ++i) {
            result.remove.add(getPathAt(i));
        }
        for (int i = firstMismatch; i < another.getStepCount(); ++i) {
            result.add.add(another.getPathAt(i));
        }
        return result;
    }

    public int getFirstMismatchWith(TreePathSteps another) {
        int i = 0;
        while (i < getStepCount() && i < another.getStepCount()) {
            boolean mismatch = !Objects.equals(
                    getPathAt(i).getLastPathComponent(),
                    another.getPathAt(i).getLastPathComponent()
            );

            if (mismatch) {
                break;
            } else {
                ++i;
            }
        }
        return i;
    }

    private static List<TreePath> split(final TreePath tip) {
        List<TreePath> result = new ArrayList<>();
        for (TreePath item = tip; item != null; item = item.getParentPath()) {
            result.add(item);
        }
        return result;
    }
}
