package gumanoid.ui.gtest.output;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gumanoid on 09.01.2016.
 */
class GTestOutputTreeIndex {
    private final String displayName;
    private final Map<String, DefaultMutableTreeNode> childrenByKey;

    public GTestOutputTreeIndex(String displayName) {
        this(displayName, new HashMap<>());
    }

    public GTestOutputTreeIndex(String displayName, Map<String, DefaultMutableTreeNode> childrenByKey) {
        this.displayName = displayName;
        this.childrenByKey = childrenByKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Map<String, DefaultMutableTreeNode> getChildrenByKey() {
        return childrenByKey;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
