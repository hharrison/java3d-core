/*
 * $RCSfile$
 *
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
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import java.util.Vector;
import java.util.Hashtable;

/**
 * SceneGraphObjectRetained is a superclass, which has things that
 * are common to all retained scene graph component objects.
 */
abstract class SceneGraphObjectRetained extends IndexedObject
	implements Cloneable {

    // The object which created this retained mode object
    SceneGraphObject source;

    // This boolean is true when the object is in a Background BranchGroup
    boolean inBackgroundGroup = false;

    // This boolean is true when the object is in the update list
    boolean onUpdateList = false;

    // A flag to indicate if the node is in setLive, note that
    // since the live is set to true only at the end, this flag
    // is need for scoping to mark the nodes that are inSetLive

    boolean inSetLive = false;

    // A flag used in compile to indicate if this node needs to go
    // through the second pass

    final static int DONT_MERGE = 0;
    final static int MERGE	= 1;
    final static int MERGE_DONE	= 2;

    int mergeFlag = 0;

    /**
     * Caches the source object that created this retained mode object.
     * @param source the object which created this retained mode object.
     */
    void setSource(SceneGraphObject source) {
	this.source = source;
    }

    /**
     * Returns the cached source object that created this retained mode
     * object.
     * @return the object which created this retained mode object.
     */
    SceneGraphObject getSource() {
	return this.source;
    }

    void markAsLive() {
	this.source.setLive();
	inSetLive = false;
    }

    void setLive(boolean inBackgroundGroup) {
	doSetLive(inBackgroundGroup);
	markAsLive();
    }
       boolean isInSetLive() {
	   return inSetLive;
       }
       
    /**
     * Makes the internal node live.
     */
    void doSetLive(boolean inBackgroundGroup) {
	inSetLive = true;
	this.inBackgroundGroup = inBackgroundGroup;
    }

    void setLive(SetLiveState s) {
	doSetLive(s);
	markAsLive();
    }

   /**
     * Makes the internal node live.
     */
    void doSetLive(SetLiveState s) {
	inSetLive = true;
	inBackgroundGroup = s.inBackgroundGroup;
    }

    /**
     * Makes the internal node not live
     */
    void clearLive(VirtualUniverse univ, int index, 
	 	   boolean sharedGroup, HashKey [] keys) {
	inBackgroundGroup = false;
	this.source.clearLive();
    }

    /**
     * Makes the internal node not live
     */
    void clearLive() {
	inBackgroundGroup = false;
	this.source.clearLive();
    }

    /**
     * This marks this object as compiled.
     */
    void setCompiled() {
	this.source.setCompiled();
    }


    /**
     * This is the default compile() method, which just marks the sgo as
     * compiled.
     */
    void compile(CompileState compState) {
	setCompiled();
    }

    void merge(CompileState compState) {
    }

    void mergeTransform(TransformGroupRetained xform) {
    }
  
    void traverse(boolean sameLevel, int level) {

	System.err.println();
        for (int i = 0; i < level; i++) {
             System.err.print(".");
        }
        System.err.print(this);
    }

    /** 
     * true if component can't be read or written after compile or setlive()
     */
    boolean isStatic() {
        return source.capabilityBitsEmpty();
    }

    protected Object clone() {
	try {
	    return super.clone();
	} catch (CloneNotSupportedException e) { 
	    return null;
	}
    }

    void handleFrequencyChange(int bit) {
    }

    VirtualUniverse getVirtualUniverse() {
	return null;
    }

}
