/*
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.util.ArrayList;

/**
 * Retained version of NodeComponent
 */

class NodeComponentRetained extends SceneGraphObjectRetained {

    // duplicate or make a reference when cloneTree() is called
    //  on this object.
    boolean duplicateOnCloneTree = false;

    // This keeps track of how many times this NodeComponent is referenced in
    // the Scene Graph
    int refCount = 0;		// this is used in setLive
    int refCnt = 0;		// this is used in compile

    // This is true when this appearance is referenced in an immediate mode context
    private boolean inImmCtx = false;

    // A list of NodeRetained Objects that refer, directly or indirectly, to this
    // NodeComponentRetained
    ArrayList users = new ArrayList(1);

    // Mirror object of this node compoenent object
    NodeComponentRetained mirror = null;

    // Sole User FrequencyBit
    // In the case of Appearance, its a bitmask of all components
    int changedFrequent = 0;
    int compChanged = 0;

    // Increment the refcount.  If this is the first, mark it as live.
    void doSetLive(boolean inBackgroundGroup, int refCount) {
	int oldRefCount = this.refCount;
	this.refCount += refCount;
	if (oldRefCount <= 0) {
	    super.doSetLive(inBackgroundGroup);

	    // Create and init a mirror object if not already there
	    // The two procedures is combined since it is redunctant to
	    // call initMirrorObject() if mirror == this (static object).
	    createMirrorObject();
	}
    }

    void setLive(boolean inBackgroundGroup, int refCount) {
	int oldRefCount = this.refCount;
	doSetLive(inBackgroundGroup, refCount);
	if (oldRefCount <= 0) {
	    super.markAsLive();
	}
    }



    // Decrement the refcount.  If this is the last, mark it as not live.
    void clearLive(int refCount) {
	this.refCount -= refCount;

	if (this.refCount <= 0) {
	    super.clearLive();
	}
    }

    // increment the compile reference count
    synchronized void incRefCnt() {
	refCnt++;
    }

    // decrement the compile reference count
    synchronized void decRefCnt() {
	refCnt--;
    }

    // remove mirror shape from the list of users
    void removeAMirrorUser(Shape3DRetained ms) {
	synchronized(mirror.users) {
	    mirror.users.remove(ms);
	}
    }

    // Add a mirror shape to the list of users
    void addAMirrorUser(Shape3DRetained ms) {
	synchronized(mirror.users) {
	    mirror.users.add(ms);
	}
    }

    // Copy the list of useres passed in into this
    void copyMirrorUsers(NodeComponentRetained node) {
	synchronized(mirror.users) {
	  synchronized(node.mirror.users) {
	      int size = node.mirror.users.size();
	      for (int i=0; i<size ; i++) {
		  mirror.users.add(node.mirror.users.get(i));
	      }
	    }
	}
    }


    // Remove the users of "node" from "this" node compoenent
    void removeMirrorUsers(NodeComponentRetained node) {

	synchronized(mirror.users) {
	  synchronized(node.mirror.users) {
	      for (int i=node.mirror.users.size()-1; i>=0; i--) {
		  mirror.users.remove(mirror.users.indexOf(node.mirror.users.get(i)));
	      }
	    }
	}
    }

    // Add a user to the list of users
    synchronized void removeUser(NodeRetained node) {
	if (node.source.isLive())
	    users.remove(users.indexOf(node));
    }

    // Add a user to the list of users
    synchronized void addUser(NodeRetained node) {
	if (node.source.isLive())
	    users.add(node);
    }


    // Add a user to the list of users
    synchronized void notifyUsers() {

	if (source == null || !source.isLive()) {
	    return;
	}

	for (int i=users.size()-1; i >=0; i--) {
	    ((NodeRetained)users.get(i)).notifySceneGraphChanged(false);
	}
    }

    /**
     * This sets the immedate mode context flag
     */
    void setInImmCtx(boolean inCtx) {
        inImmCtx = inCtx;
    }

    /**
     * This gets the immedate mode context flag
     */
    boolean getInImmCtx() {
        return (inImmCtx);
    }

    /**
     * Sets this node's duplicateOnCloneTree value.  The
     * <i>duplicateOnCloneTree</i> value is used to determine if NodeComponent
     * objects are to be duplicated or referenced during a
     * <code>cloneTree</code> operation. A value of <code>true</code> means
     *  that this NodeComponent object should be duplicated, while a value
     *  of <code>false</code> indicates that this NodeComponent object's
     *  reference will be copied into the newly cloned object.  This value
     *  can be overriden via the <code>forceDuplicate</code> parameter of
     *  the <code>cloneTree</code> method.
     * @param duplicate the value to set.
     * @see Node#cloneTree
     */
    void setDuplicateOnCloneTree(boolean duplicate) {
	duplicateOnCloneTree = duplicate;
    }

    /**
     * Returns this node's duplicateOnCloneTree value. The
     * <i>duplicateOnCloneTree</i> value is used to determine if NodeComponent
     * objects are to be duplicated or referenced during a
     * <code>cloneTree</code> operation. A value of <code>true</code> means
     *  that this NodeComponent object should be duplicated, while a value
     *  of <code>false</code> indicates that this NodeComponent object's
     *  reference will be copied into the newly cloned object.  This value
     *  can be overriden via the <code>forceDuplicate</code> parameter of
     *  the <code>cloneTree</code> method.
     * @return the value of this node's duplicateOnCloneTree
     * @see Node#cloneTree
     */
    boolean getDuplicateOnCloneTree() {
	return duplicateOnCloneTree;
    }


    void initMirrorObject() {
    }

    void updateMirrorObject(int component, Object obj) {
    }

    void createMirrorObject() {
	// Overridden by appearance and other classes
	initMirrorObject();
	mirror = null;
    }

    void setFrequencyChangeMask(int bit, int mask) {
	if (source.getCapabilityIsFrequent(bit))
	    changedFrequent |= mask;
	else if (!source.isLive()) {
            // Record the freq->infreq change only for non-live node components
	    changedFrequent &= ~mask;
	}
    }

     @Override
     protected Object clone() {
         NodeComponentRetained ncr = (NodeComponentRetained)super.clone();
	 ncr.changedFrequent = changedFrequent;
	 return ncr;
     }

    protected void set(NodeComponentRetained nc) {
	changedFrequent = nc.changedFrequent;
    }
}
