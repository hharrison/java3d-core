/*
 * Copyright 2005-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
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
