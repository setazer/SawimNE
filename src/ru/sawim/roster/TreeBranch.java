
package ru.sawim.roster;

public abstract class TreeBranch implements TreeNode {

    public TreeBranch() {
    }

    private boolean expanded = false;

    public final boolean isExpanded() {
        return expanded;
    }
    /**
     * Expand or collapse tree node.
     * 
     * NOTE: this is not recursive operation!
     */
    public final void setExpandFlag(boolean value) {
        expanded = value;
        sort();
    }

    public void sort() {
    }
    public abstract boolean isEmpty();
}