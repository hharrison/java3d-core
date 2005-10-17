/*
 * $RCSfile$
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

/**
 * Listener interface for monitoring structural changes to live scene
 * graphs. BranchGroup additions, removals and moves are reported.
 *
 * @see VirtualUniverse#addGraphStructureChangeListener
 *
 * @since Java 3D 1.4
 */
public interface GraphStructureChangeListener {
    /**
     * Invoked when a branch group is added.
     * Called just before the child is added to the parent.
     * Parent can be either a BranchGroup or a Locale.
     *
     * @param parent the parent of the child being added
     * @param child the child being added
     */
    public void branchGroupAdded(Object parent, BranchGroup child);
    
    /**
     * Invoked when a branch group is removed.
     * Called just after the child has been removed from the parent.
     * Parent can be either a BranchGroup or a Locale.
     *
     * @param parent the parent of the child being added
     * @param child the child being added
     */
    public void branchGroupRemoved(Object parent, BranchGroup child);
    
    /**
     * Invoked when a branch group is moved.
     * Called after a child has been moved to it's new parent. This call differs
     * from the other methods in that the child is live when this method is called.
     *
     * @param oldParent the original parent of the child being moved
     * @param newParent the new parent of the child being moved
     * @param child the child being moved
     */
    public void branchGroupMoved(Object oldParent, Object newParent, BranchGroup child);
}
