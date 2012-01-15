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
import java.util.ArrayList;

/**
 * A Link leaf node consisting of a reference to a SharedGroup node.
 */

class LinkRetained extends LeafRetained {
    /**
     * The SharedGroup component of the link node.
     */
    SharedGroupRetained sharedGroup;

    static String plus = "+";

    // This is used when setLive to check for cycle scene graph
    boolean visited = false;

    LinkRetained() {
	this.nodeType = NodeRetained.LINK;
	localBounds = new BoundingBox((Bounds)null);
    }

    /**
     * Sets the SharedGroup reference.
     * @param sharedGroup the SharedGroup node
     */
    void setSharedGroup(SharedGroup sharedGroup) {
	// Note that it is possible that the sharedGroup pass
	// in already link to another link and live.
	HashKey newKeys[] = null;
	boolean abort = false;

	if (source.isLive()) {
	    // bug 4370407: if sharedGroup is a parent, then don't do anything
	    if (sharedGroup != null) {
	        synchronized(universe.sceneGraphLock) {
	            NodeRetained pa;
	            for (pa = parent; pa != null; pa = pa.parent) {
	                if (pa == (NodeRetained)sharedGroup.retained) {
			    abort = true;
                            throw new SceneGraphCycleException(J3dI18N.getString("LinkRetained1"));
	                }
	            }
	        }
	        if (abort)
		    return;
	    }

	    newKeys = getNewKeys(locale.nodeId, localToVworldKeys);

	    if (this.sharedGroup != null) {
		((GroupRetained) parent).checkClearLive(this.sharedGroup,
							newKeys, true, null,
							0, 0, this);
		this.sharedGroup.parents.removeElement(this);
	    }
	}

	if (sharedGroup != null) {
	    this.sharedGroup =
		(SharedGroupRetained)sharedGroup.retained;
	}  else {
	    this.sharedGroup = null;
	}

      if (source.isLive() && (sharedGroup != null)) {

	  this.sharedGroup.parents.addElement(this);
	  visited = true;
	  try {
	      int ci = ((GroupRetained) parent).indexOfChild((Node)this.sharedGroup.source);
	      ((GroupRetained) parent).checkSetLive(this.sharedGroup,  ci,
						    newKeys, true, null,
						    0, this);
	  } catch (SceneGraphCycleException e) {
	      throw e;
          } finally {
	      visited = false;
	  }
      }

    }

  /**
   * Retrieves the SharedGroup reference.
   * @return the SharedGroup node
   */
    SharedGroup getSharedGroup() {
	return (sharedGroup != null ?
		(SharedGroup)this.sharedGroup.source : null);
    }

    void computeCombineBounds(Bounds bounds) {

	if (boundsAutoCompute) {
	    sharedGroup.computeCombineBounds(bounds);
	} else {
	    // Should this be lock too ? ( MT safe  ? )
	    synchronized(localBounds) {
		bounds.combine(localBounds);
	    }
	}
    }


    /**
     * Gets the bounding object of a node.
     * @return the node's bounding object
     */
    Bounds getBounds() {
	return (boundsAutoCompute ?
	        (Bounds)sharedGroup.getBounds().clone() :
	        super.getBounds());
    }


    /**
     * assign a name to this node when it is made live.
     */
    void setLive(SetLiveState s) {

        super.doSetLive(s);

        if (inBackgroundGroup) {
            throw new
               IllegalSceneGraphException(J3dI18N.getString("LinkRetained0"));
        }

	if (nodeId == null) {
            nodeId = universe.getNodeId();
	}

	if (sharedGroup != null) {
	    this.sharedGroup.parents.addElement(this);
	    HashKey newKeys[] = getNewKeys(s.locale.nodeId, s.keys);
	    HashKey oldKeys[] = s.keys;
	    s.keys = newKeys;
	    s.inSharedGroup = true;
	    if (visited) {
		throw new SceneGraphCycleException(J3dI18N.getString("LinkRetained1"));
	    }
	    visited = true;
	    try {
		this.sharedGroup.setLive(s);
	    } catch (SceneGraphCycleException e) {
		throw e;
	    } finally {
		visited = false;
	    }

	    s.inSharedGroup = inSharedGroup;
	    s.keys = oldKeys;

	    localBounds.setWithLock(this.sharedGroup.localBounds);
	}

	super.markAsLive();
    }

    void setNodeData(SetLiveState s) {

        super.setNodeData(s);

        // add this node to parentTransformLink's childTransformLink
        if (s.childTransformLinks != null) {
            // do not duplicate shared nodes
            synchronized(s.childTransformLinks) {
                if(!inSharedGroup || !s.childTransformLinks.contains(this)) {
                    s.childTransformLinks.add(this);
                }
            }
        }

        // add this node to parentSwitchLink's childSwitchLink
        if (s.childSwitchLinks != null) {
            if(!inSharedGroup ||
                        // only add if not already added in sharedGroup
                        !s.childSwitchLinks.contains(this)) {
                s.childSwitchLinks.add(this);
            }
        }
    }


    void recombineAbove() {
        localBounds.setWithLock(sharedGroup.localBounds);
	parent.recombineAbove();
    }

    /**
     * assign a name to this node when it is made live.
     */
    void clearLive(SetLiveState s) {

	if (sharedGroup != null) {
	    HashKey newKeys[] = getNewKeys(s.locale.nodeId, s.keys);
            super.clearLive(s);
	    HashKey oldKeys[] = s.keys;
	    s.keys = newKeys;
	    s.inSharedGroup = true;
	    this.sharedGroup.parents.removeElement(this);
	    this.sharedGroup.clearLive(s);
	    s.inSharedGroup = inSharedGroup;
	    s.keys = oldKeys;
	} else {
            super.clearLive(s);
	}
    }

    void removeNodeData(SetLiveState s) {
        if(refCount <= 0) {
	    // either not in sharedGroup or last instance in sharedGroup
            // remove this node from parentTransformLink's childTransformLink
            if (parentTransformLink != null) {
                ArrayList obj;
                if (parentTransformLink instanceof TransformGroupRetained) {
                    obj = ((TransformGroupRetained)
                        parentTransformLink).childTransformLinks;
                } else {
                    obj = ((SharedGroupRetained)
                        parentTransformLink).childTransformLinks;
                }
                synchronized(obj) {
                    obj.remove(this);
                }
            }

            // remove this node from parentSwitchLink's childSwitchLink
            if (parentSwitchLink != null) {
                ArrayList switchLinks;
                for(int i=0; i<parentSwitchLink.childrenSwitchLinks.size();i++){
                    switchLinks = (ArrayList)
                                parentSwitchLink.childrenSwitchLinks.get(i);
                    if (switchLinks.contains(this)) {
                        switchLinks.remove(this);
                        break;
                    }
                }
            }
        }
	super.removeNodeData(s);
    }


    void updatePickable(HashKey keys[], boolean pick[]) {
	super.updatePickable(keys, pick);

	if (sharedGroup != null) {
	    HashKey newKeys[] = getNewKeys(locale.nodeId, keys);
	    sharedGroup.updatePickable(newKeys, pick);
	}
    }

    void updateCollidable(HashKey keys[], boolean collide[]) {
	 super.updateCollidable(keys, collide);

	 if (sharedGroup != null) {
	     HashKey newKeys[] = getNewKeys(locale.nodeId, keys);
	     sharedGroup.updateCollidable(newKeys, collide);
	 }
    }

    void setBoundsAutoCompute(boolean autoCompute) {
        super.setBoundsAutoCompute(autoCompute);
        if (!autoCompute) {
            localBounds = getBounds();
        }
    }

    void setCompiled() {
	super.setCompiled();
	if (sharedGroup != null) {
	    sharedGroup.setCompiled();
	}
    }

    void compile(CompileState compState) {

	super.compile(compState);

        // XXXX: for now keep the static transform in the parent tg
        compState.keepTG = true;

        // don't remove this group node
        mergeFlag = SceneGraphObjectRetained.DONT_MERGE;

        if (J3dDebug.devPhase && J3dDebug.debug) {
            compState.numLinks++;
        }
    }

    HashKey[] getNewKeys(String localeNodeId,  HashKey oldKeys[]) {
	HashKey newKeys[];

	if (!inSharedGroup) {
	    newKeys = new HashKey[1];
	    newKeys[0] = new HashKey(localeNodeId);
	    newKeys[0].append(plus + nodeId);
	} else {
	    // Need to append this link node id to all keys passed in.
	    newKeys = new HashKey[oldKeys.length];
	    for (int i=oldKeys.length-1; i>=0; i--) {
		newKeys[i] = new HashKey(oldKeys[i].toString()
					 + plus + nodeId);
	    }
	}
	return newKeys;
    }

    void searchGeometryAtoms(UnorderList list) {
	if (sharedGroup != null) {
	    sharedGroup.searchGeometryAtoms(list);
	}
    }
}
