/*
 * $RCSfile$
 *
 * Copyright 1996-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
 * Group node that contains a transform.
 */

class TransformGroupRetained extends GroupRetained implements TargetsInterface
{

    /**
     * The Transform value for the TransformGroup.
     */
    Transform3D transform = new Transform3D();

    /**
     * The inverse of the transform
     */
    Transform3D invTransform = null;

    /**
     * The transpose of the inverse of the transform
     */
    Transform3D normalTransform = null;

    /**
     * The Transform value currently being used internally
     */
    Transform3D currentTransform = new Transform3D();

    /**
     * localVworld values for children of this TG
     */
    Transform3D[][] childLocalToVworld = null;
    int[][] 	    childLocalToVworldIndex = null;

    // working variable for children transforms
    Transform3D[][] childTrans = null;
    int[][] 	    childTransIndex = null;


    /**
     * A bitmask of the types in targets
     */
    int localTargetThreads = 0;

    // combined localTargetThreads and decendants' localTargetThreads
    int targetThreads = 0;

    /**
     * A list of WakeupOnTransformChange conditions for this Transform
     */
    WakeupIndexedList transformChange = null;

    // The current list of child transform group nodes or link nodes
    // under a transform group
    ArrayList childTransformLinks = new ArrayList(1);

    // working area while compile
    boolean needNormalsTransform = false; // true if normals transformation
					  // is needed to push this
					  // transform down to geometry

    // key which identifies a unique path from a
    // locale to this transform group
    HashKey currentKey = new HashKey();

    boolean aboveAViewPlatform = false;

    // maximum transform level of all shared path
    int maxTransformLevel = -1;

    // List of transform level, one per shared path
    int transformLevels[] = null;

    // J3d copy.
    CachedTargets[] j3dCTs = null;

    // User copy.
    CachedTargets[] cachedTargets = null;

    // Contains per path data, XXXX: move to NodeRetained
    TransformGroupData[] perPathData = null;


    /**
     * The constructor
     */
    TransformGroupRetained() {
        this.nodeType = NodeRetained.TRANSFORMGROUP;
    }

  /**
   * Sets the transform component of this TransformGroup to the value of
   * the passed transform.
   * @param t1 the transform to be copied
   */
  void setTransform(Transform3D t1) {
      J3dMessage tchangeMessage = null;
      int i, j;
      Transform3D trans = null;

      if (staticTransform != null) {
	  // this writeable transformGroup has a static transform
	  // merged into this node

      	  trans = new Transform3D(staticTransform.transform);
	  trans.mul(t1);

      	  transform.setWithLock(trans);

      } else {
      	  trans = new Transform3D(t1);
      	  transform.setWithLock(t1);
      }

      if (transformChange != null) {
	  notifyConditions();
      }

      if (source.isLive()) {

	  if (aboveAViewPlatform && !t1.isCongruent()) {
	      throw new BadTransformException(J3dI18N.getString("ViewPlatformRetained0"));
	  }

	  tchangeMessage = new J3dMessage();
	  tchangeMessage.type = J3dMessage.TRANSFORM_CHANGED;
	  tchangeMessage.threads = targetThreads;
	  tchangeMessage.args[1] = this;
	  tchangeMessage.args[2] = trans;

	  tchangeMessage.universe = universe;
	  //System.err.println("TransformGroupRetained --- TRANSFORM_CHANGED " + this);
	  VirtualUniverse.mc.processMessage(tchangeMessage);
      }
      dirtyBoundsCache();
  }

    /**
     * Copies the transform component of this TransformGroup into
     * the passed transform object.
     * @param t1 the transform object to be copied into
     */
    void getTransform(Transform3D t1) {
	transform.getWithLock(t1);

        // if staticTransform exists for this node, need to
        // redetermine the original user specified transform

        if (staticTransform != null) {
            Transform3D invTransform = staticTransform.getInvTransform();
            t1.mul(invTransform, t1);
        }
    }


    // get the inverse of the transform -- note: this method only
    // supports static transform

    Transform3D getInvTransform() {
        if (invTransform == null) {
            invTransform = new Transform3D(transform);
            invTransform.invert();
        }
	return invTransform;
    }


    // get the inverse of the transpose -- note: this method only
    // supports static transform, the translation component will
    // not transform
    Transform3D getNormalTransform() {
	if (normalTransform == null) {
            normalTransform = new Transform3D(transform);
	    normalTransform.invert();
	    normalTransform.transpose();
        }
	return normalTransform;
    }

    // synchronized with TransformStructure
    synchronized void setNodeData(SetLiveState s) {
	int i;

        super.setNodeData(s);

        childTrans = new Transform3D[s.currentTransforms.length][2];
        childTransIndex = new int[s.currentTransforms.length][2];

        for (i=0; i< s.currentTransforms.length; i++) {
            childTrans[i][0] = new Transform3D();

            childTrans[i][0].mul(s.currentTransforms[i]
                                   [s.currentTransformsIndex[i]
                                   [CURRENT_LOCAL_TO_VWORLD]], currentTransform);
            childTrans[i][1] = new Transform3D(childTrans[i][0]);
            childTransIndex[i][0] = 0;
            childTransIndex[i][1] = 0;

        }

        if (!s.inSharedGroup) {
            s.transformLevels[0] += 1;
            maxTransformLevel = s.transformLevels[0];
        } else {
            for (i=0; i<s.keys.length; i++) {
                s.transformLevels[i] += 1;
                if (s.transformLevels[i] > maxTransformLevel) {
                    maxTransformLevel = s.transformLevels[i];
                }
            }
        }

	if (!inSharedGroup) {
	    if (childLocalToVworld == null) {
		// If the node is a transformGroup then need to keep
		// the child transforms as well
		childLocalToVworld = new Transform3D[1][];
		childLocalToVworldIndex = new int[1][];
		transformLevels = new int[1];
		// Use by TransformStructure
                cachedTargets = new CachedTargets[1];
                perPathData = new TransformGroupData[1];
	    }
	    childLocalToVworld[0] = childTrans[0];
	    childLocalToVworldIndex[0] = childTransIndex[0];
            transformLevels[0] = s.transformLevels[0];

	    setAuxData(s, 0, 0);
	} else {

	    // For inSharedGroup case.
	    int j, len;

	    if (childLocalToVworld == null) {
	        childLocalToVworld = new Transform3D[s.keys.length][];
	        childLocalToVworldIndex = new int[s.keys.length][];
                transformLevels = new int[s.keys.length];
                cachedTargets = new CachedTargets[s.keys.length];
                perPathData = new TransformGroupData[s.keys.length];
	        len=0;
	    } else {

	        len = localToVworld.length - s.keys.length;

	        int newLen = localToVworld.length;

	        Transform3D newChildTList[][] = new Transform3D[newLen][];
	        int newChildIndexList[][] = new int[newLen][];
	        int newTransformLevels[] = new int[newLen];
                CachedTargets newTargets[] = new CachedTargets[newLen];
                TransformGroupData newPerPathData[] = new TransformGroupData[newLen];

	        System.arraycopy(childLocalToVworld, 0,
			     newChildTList, 0, childLocalToVworld.length);
	        System.arraycopy(childLocalToVworldIndex, 0,
			     newChildIndexList, 0, childLocalToVworldIndex.length);
	        System.arraycopy(transformLevels, 0,
			     newTransformLevels, 0, transformLevels.length);

                System.arraycopy(cachedTargets, 0,
                             newTargets, 0, cachedTargets.length);

                System.arraycopy(perPathData, 0,
                             newPerPathData, 0, perPathData.length);

	        childLocalToVworld = newChildTList;
	        childLocalToVworldIndex = newChildIndexList;
	        transformLevels = newTransformLevels;
                cachedTargets = newTargets;
		perPathData = newPerPathData;
	    }

	    int hkIndex;
	    int hkIndexPlus1, blkSize;

	    for(i=len, j=0; i<localToVworld.length; i++, j++) {
	        hkIndex = s.keys[j].equals(localToVworldKeys, 0,
						localToVworldKeys.length);

	        if(hkIndex < 0) {
		    MasterControl.getCoreLogger().severe("Can't Find matching hashKey in setNodeData.");
		    break;
	        } else if(hkIndex >= i) { // Append to last.
		    childLocalToVworld[i] = childTrans[j];
		    childLocalToVworldIndex[i] = childTransIndex[j];
		    transformLevels[i] = s.transformLevels[j];
	        } else {
		    hkIndexPlus1 = hkIndex + 1;
		    blkSize = i - hkIndex;

		    System.arraycopy(childLocalToVworld, hkIndex,
				 childLocalToVworld, hkIndexPlus1, blkSize);

		    System.arraycopy(childLocalToVworldIndex, hkIndex,
				 childLocalToVworldIndex, hkIndexPlus1, blkSize);

		    System.arraycopy(transformLevels, hkIndex,
				 transformLevels, hkIndexPlus1, blkSize);

                    System.arraycopy(cachedTargets, hkIndex,
                                 cachedTargets, hkIndexPlus1, blkSize);

                    System.arraycopy(perPathData, hkIndex,
                                 perPathData, hkIndexPlus1, blkSize);

		    childLocalToVworld[hkIndex] = childTrans[j];
		    childLocalToVworldIndex[hkIndex] = childTransIndex[j];
		    transformLevels[hkIndex] = s.transformLevels[j];
	        }

	        setAuxData(s, j, hkIndex);
	    }
	}
        if (s.childTransformLinks != null) {
            // do not duplicate shared nodes
            synchronized(s.childTransformLinks) {
                if(!inSharedGroup || !s.childTransformLinks.contains(this)) {
                    s.childTransformLinks.add(this);
                }
            }
        }

	s.localToVworld = childLocalToVworld;
	s.localToVworldIndex = childLocalToVworldIndex;
        s.currentTransforms = childTrans;
        s.currentTransformsIndex = childTransIndex;

        s.childTransformLinks = childTransformLinks;
        s.parentTransformLink = this;
    }

    void setAuxData(SetLiveState s, int index, int hkIndex) {
        super.setAuxData(s, index, hkIndex);
        perPathData[hkIndex] = new TransformGroupData();
        perPathData[hkIndex].switchState =
                                (SwitchState)s.switchStates.get(hkIndex);
    }


    // Add a WakeupOnTransformChange to the list
    void removeCondition(WakeupOnTransformChange wakeup) {
	synchronized (transformChange) {
	    transformChange.remove(wakeup);
	}
    }

    // Add a WakeupOnTransformChange to the list
    void addCondition(WakeupOnTransformChange wakeup) {
	synchronized (transformChange) {
	    transformChange.add(wakeup);
	}
    }

    void notifyConditions() {
	synchronized (transformChange) {
	    WakeupOnTransformChange list[] = (WakeupOnTransformChange [])
		transformChange.toArray(false);
	    for (int i=transformChange.size()-1; i >=0; i--) {
		list[i].setTriggered();
	    }
	}
    }

    boolean isStatic() {
	if (!super.isStatic() ||
	    source.getCapability(TransformGroup.ALLOW_TRANSFORM_READ) ||
	    source.getCapability(TransformGroup.ALLOW_TRANSFORM_WRITE)) {
	    return false;
	} else {
	    return true;
	}
    }

    void mergeTransform(TransformGroupRetained xform) {
	super.mergeTransform(xform);
	transform.mul(xform.transform, transform);
    }

    void traverse(boolean sameLevel, int level) {

	System.err.println();
	for (int i = 0; i < level; i++) {
	     System.err.print(".");
	}
	System.err.print(this);

	if (isStatic()) {
	    System.err.print(" (s)");
	} else {
	    System.err.print(" (w)");
	}
	System.err.println();
	System.err.println(transform.toString());
	super.traverse(true, level);
    }

    void compile(CompileState compState) {

	// save and reset the keepTG and needNormalsTransform flags

	boolean saveKeepTG = compState.keepTG;
	compState.keepTG = false;

	boolean saveNeedNormalsTransform = compState.needNormalsTransform;
        compState.needNormalsTransform = false;

	super.compile(compState);

	if (compState.keepTG) {
	    // keep this transform group, don't merge it

	    mergeFlag = SceneGraphObjectRetained.DONT_MERGE;
	}

	if (J3dDebug.devPhase && J3dDebug.debug) {
	    compState.numTransformGroups++;
	    if (isStatic())
		compState.numStaticTransformGroups++;
	    if (mergeFlag == SceneGraphObjectRetained.MERGE)
		compState.numMergedTransformGroups++;
        }

	if (mergeFlag == SceneGraphObjectRetained.DONT_MERGE) {
	    // a non-mergeable TG will trigger a merge of its subtree

	    compState.staticTransform = null;
	    compState.parentGroup = null;
	    super.merge(compState);

	} else {
	    // flag this TG as to be merged later on
	    mergeFlag = SceneGraphObjectRetained.MERGE;
	}

	// restore compile state
	compState.keepTG = saveKeepTG;
	this.needNormalsTransform = compState.needNormalsTransform;
        compState.needNormalsTransform = saveNeedNormalsTransform;
    }

    void merge(CompileState compState) {

	TransformGroupRetained saveStaticTransform;

	// merge the transforms
	if (compState.staticTransform != null) {
	    staticTransform = compState.staticTransform;
	    mergeTransform(compState.staticTransform);
	}

	if (mergeFlag == SceneGraphObjectRetained.MERGE) {

	    // before we push down the static transform, check
	    // to see if the transform will be pushed down to shapes
	    // with geometry_with_normals and if so, check if
            // the normal transform has uniform scale or not. If
	    // it doesn't, don't push it down.

	    if (this.needNormalsTransform) {
		Transform3D normalXform = this.getNormalTransform();
		if (!normalXform.isCongruent()) {
		    mergeFlag = SceneGraphObjectRetained.DONT_MERGE;
		}
	    }
	}

	if (mergeFlag == SceneGraphObjectRetained.MERGE) {
	    saveStaticTransform = compState.staticTransform;
	    compState.staticTransform = this;

	    // go to the merge method of the group node to start
	    // pushing down the static transform until it hits
	    // a leaf or a subtree which is already merged.
	    super.merge(compState);

	    // reset the compile state
	    compState.staticTransform = saveStaticTransform;

	} else {
	    compState.parentGroup.compiledChildrenList.add(this);
	    parent = compState.parentGroup;
	}

	mergeFlag = SceneGraphObjectRetained.MERGE_DONE;
    }

    /**
     * This setlive simply concatinates it's transform onto all the ones
     * passed in.
     */
    void setLive(SetLiveState s) {
      int i,j;
      Transform3D trans = null;
      Targets[] newTargets = null;
      Targets[] savedTransformTargets = null;
      int oldTraverseFlags = 0;
      int len;
      Object obj;

      // XXXX - optimization for targetThreads computation, require
      // cleanup in GroupRetained.doSetLive()
      //int savedTargetThreads = 0;
      //savedTargetThreads = s.transformTargetThreads;
      //s.transformTargetThreads = 0;

      oldTraverseFlags = s.traverseFlags;

      savedTransformTargets = s.transformTargets;

      int numPaths = (s.inSharedGroup)? s.keys.length : 1;
      newTargets = new Targets[numPaths];
      for(i=0; i<numPaths; i++) {
          newTargets[i] = new Targets();
      }

      s.transformTargets = newTargets;
      s.traverseFlags = 0;

      // This is needed b/c super.setlive is called after inSharedGroup check.
      inSharedGroup = s.inSharedGroup;

      trans = new Transform3D();
      transform.getWithLock(trans);
      currentTransform.set(trans);


      ArrayList savedChildTransformLinks = s.childTransformLinks;
      GroupRetained savedParentTransformLink = s.parentTransformLink;
      Transform3D[][] oldCurrentList = s.currentTransforms;
      int[][] oldCurrentIndexList = s.currentTransformsIndex;


      super.doSetLive(s);


      if (! inSharedGroup) {
          if (s.transformTargets[0] != null) {
              cachedTargets[0] = s.transformTargets[0].snapShotInit();
          }
          if (s.switchTargets != null &&
                        s.switchTargets[0] != null) {
              s.switchTargets[0].addNode(this, Targets.GRP_TARGETS);
	  }
      } else {
          int hkIndex;
          for(i=0; i<numPaths; i++) {
              if (s.transformTargets[i] != null) {
                  hkIndex = s.keys[i].equals(localToVworldKeys, 0,
                                                localToVworldKeys.length);
                  cachedTargets[hkIndex] = s.transformTargets[i].snapShotInit();
              }
              if (s.switchTargets != null &&
                        s.switchTargets[i] != null) {
                  s.switchTargets[i].addNode(this, Targets.GRP_TARGETS);
              }
          }
      }

      // Assign data in cachedTargets to j3dCTs.
      j3dCTs = new CachedTargets[cachedTargets.length];
      copyCachedTargets(TargetsInterface.TRANSFORM_TARGETS, j3dCTs);

      computeTargetThreads(TargetsInterface.TRANSFORM_TARGETS, cachedTargets);

      // restore setLiveState from it's local variables.
      // setNodeData did keep a reference to these variables.
      s.localToVworld = localToVworld;
      s.localToVworldIndex = localToVworldIndex;
      s.currentTransforms = oldCurrentList;
      s.currentTransformsIndex = oldCurrentIndexList;

      s.childTransformLinks = savedChildTransformLinks;
      s.parentTransformLink = savedParentTransformLink;

      s.transformTargets = savedTransformTargets;

      if (!s.inSharedGroup) {
          s.transformLevels[0] -= 1;
      } else {
          for (i=0; i<s.keys.length; i++) {
              s.transformLevels[i] -= 1;
          }
      }


      if ((s.traverseFlags & NodeRetained.CONTAINS_VIEWPLATFORM) != 0) {
          aboveAViewPlatform = true;
      }
      s.traverseFlags |= oldTraverseFlags;

      if (aboveAViewPlatform && !trans.isCongruent()) {
	  throw new BadTransformException(J3dI18N.getString("ViewPlatformRetained0"));
      }

      super.markAsLive();
    }


    /**
     * remove the localToVworld transform for a  transformGroup
     */
    void removeNodeData(SetLiveState s) {

	synchronized (this) { // synchronized with TransformStructure

	    if (refCount <= 0) {
		childLocalToVworld = null;
		childLocalToVworldIndex = null;
		transformLevels = null;
		// only use by TransformStruct.
                cachedTargets = null;
                perPathData = null;
                targetThreads = 0;


                if (parentTransformLink != null) {
                    ArrayList obj;
                    if (parentTransformLink
                        instanceof TransformGroupRetained) {
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
	        aboveAViewPlatform = false;
	    }
	    else {
		int i, index, len;
		// Remove the localToVworld key
		int newLen = localToVworld.length - s.keys.length;

		Transform3D[][] newChildTList = new Transform3D[newLen][];
		int[][] newChildIndexList = new int[newLen][];
		int[] newTransformLevels = new int[newLen];
		ArrayList[] newChildPTG = new ArrayList[newLen];
		CachedTargets[] newTargets = new CachedTargets[newLen];
                TransformGroupData[] newPerPathData = new TransformGroupData[newLen];

		int[] tempIndex = new int[s.keys.length];
		int curStart =0, newStart =0;
		boolean found = false;
		for(i=0;i<s.keys.length;i++) {
		    index = s.keys[i].equals(localToVworldKeys, 0, localToVworldKeys.length);

		    tempIndex[i] = index;

		    if(index >= 0) {
			found = true;

			if(index == curStart) {
			    curStart++;
			}
			else {
			    len = index - curStart;
			    System.arraycopy(childLocalToVworld, curStart, newChildTList,
					     newStart, len);
			    System.arraycopy(childLocalToVworldIndex, curStart, newChildIndexList,
					     newStart, len);
			    System.arraycopy(transformLevels, curStart, newTransformLevels,
					     newStart, len);
			    System.arraycopy(cachedTargets, curStart, newTargets, newStart, len);
                            System.arraycopy(perPathData, curStart,
                                                newPerPathData, newStart, len);

			    curStart = index+1;
			    newStart = newStart + len;
			}
		    }
		    else {
			found = false;
			MasterControl.getCoreLogger().severe("TG.removeNodeData-Can't find matching hashKey.");
		    }
		}

		if((found == true) && (curStart < localToVworld.length)) {
		    len = localToVworld.length - curStart;
		    System.arraycopy(childLocalToVworld, curStart, newChildTList,
				     newStart, len);
		    System.arraycopy(childLocalToVworldIndex, curStart, newChildIndexList,
				     newStart, len);
		    System.arraycopy(transformLevels, curStart, newTransformLevels,
				     newStart, len);
		    System.arraycopy(cachedTargets, curStart, newTargets, newStart, len);
                    System.arraycopy(perPathData, curStart,
                                                newPerPathData, newStart, len);
		}

		childLocalToVworld = newChildTList;
		childLocalToVworldIndex = newChildIndexList;
		transformLevels = newTransformLevels;
		cachedTargets = newTargets;
                perPathData = newPerPathData;
	    }
            super.removeNodeData(s);
	    // Set it back to its parent localToVworld data.
	    // This is b/c the parent has changed it localToVworld data arrays.
	    s.localToVworld = childLocalToVworld;
	    s.localToVworldIndex = childLocalToVworldIndex;
	}
    }

    void clearLive(SetLiveState s) {

        Targets[] savedTransformTargets = null;

        savedTransformTargets = s.transformTargets;
	// no need to gather targets from tg in clear live
        s.transformTargets = null;

	super.clearLive(s);

        // restore setLiveState from it's local variables.
        // removeNodeData has altered these variables.
        s.localToVworld = localToVworld;
        s.localToVworldIndex = localToVworldIndex;
        s.transformTargets = savedTransformTargets;

	synchronized (this) { // synchronized with TransformStructure
	if (inSharedGroup) {
            if (transformLevels != null) {
                maxTransformLevel = transformLevels[0];
                for (int i=1; i<transformLevels.length; i++) {
                   if (transformLevels[i] > maxTransformLevel) {
                       maxTransformLevel = transformLevels[i];
                   }
               }
            } else {
		maxTransformLevel = -1;
	    }

            if (s.switchTargets != null) {
                for (int i=0; i<s.switchTargets.length; i++) {
                    if (s.switchTargets[i] != null) {
                        s.switchTargets[i].addNode(this, Targets.GRP_TARGETS);
                    }
                }
            }

        } else {
	    maxTransformLevel = -1;
            if (s.switchTargets != null &&
                        s.switchTargets[0] != null) {
                s.switchTargets[0].addNode(this, Targets.GRP_TARGETS);
            }
        }
        }
	// XXXX: recontruct targetThreads
    }


    void computeCombineBounds(Bounds bounds) {
        // Issue 514 : NPE in Wonderland : triggered in cached bounds computation
        if (validCachedBounds && boundsAutoCompute) {
            Bounds b = (Bounds) cachedBounds.clone();
            // Should this be lock too ? ( MT safe  ? )
            // Thoughts :
            // Make a temp copy with lock :  transform.getWithLock(trans);, but this will cause gc ...
            synchronized(transform) {
                b.transform(transform);
            }
            bounds.combine(b);
            return;
        }

	NodeRetained child;
        //issue 544
        Bounds boundingObject = null;
        if (VirtualUniverse.mc.useBoxForGroupBounds) {
            boundingObject = new BoundingBox((Bounds) null);
        } else {
		boundingObject = new BoundingSphere((Bounds)null);
        }
	if(boundsAutoCompute) {
	    for (int i=children.size()-1; i>=0; i--) {
		child = (NodeRetained)children.get(i);
		if(child != null)
		    child.computeCombineBounds(boundingObject);
	    }

            if (VirtualUniverse.mc.cacheAutoComputedBounds) {
                cachedBounds = (Bounds) boundingObject.clone();
            }
	}
	else {
	    // Should this be lock too ? ( MT safe  ? )
	    synchronized(localBounds) {
		boundingObject.set(localBounds);
	    }
	}

        // Should this be lock too ? ( MT safe  ? )
	// Thoughts :
	// Make a temp copy with lock :  transform.getWithLock(trans);, but this will cause gc ...
	synchronized(transform) {
	    boundingObject.transform(transform);
	}
	bounds.combine(boundingObject);

    }

    void processChildLocalToVworld(ArrayList dirtyTransformGroups,
                                                ArrayList keySet,
						UpdateTargets targets,
						ArrayList blUsers) {

	synchronized(this) { // sync with setLive/clearLive

	    if (inSharedGroup) {
		if (localToVworldKeys != null) {
		    for(int j=0; j<localToVworldKeys.length; j++) {
			if (perPathData[j].markedDirty) {
			    updateChildLocalToVworld(localToVworldKeys[j], j,
						     dirtyTransformGroups,
						     keySet, targets,
						     blUsers);
			} else {
			    //System.err.println("tg.procChild markedDiry skip");
			}
		    }
		}
	    } else {
		if (perPathData != null && perPathData[0].markedDirty) {
		    updateChildLocalToVworld(dirtyTransformGroups, keySet,
						     targets, blUsers);
		} else {
		    //System.err.println("tg.procChild markedDiry skip");
		}
	    }
	}
    }

    // for shared case
    void updateChildLocalToVworld(HashKey key, int index,
				  ArrayList dirtyTransformGroups,
				  ArrayList keySet,
				  UpdateTargets targets,
				  ArrayList blUsers) {

	int i, j;
	Object obj;
	Transform3D lToVw, childLToVw;
	TransformGroupRetained tg;
	LinkRetained ln;
	CachedTargets ct;

	synchronized(this) { // sync with setLive/clearLive

	    if (localToVworld != null) {
		perPathData[index].markedDirty = false;
		// update immediate child's localToVworld

		if (perPathData[index].switchState.currentSwitchOn ) {
		    lToVw = getCurrentLocalToVworld(index);
		    childLToVw = getUpdateChildLocalToVworld(index);
		    childLToVw.mul(lToVw, currentTransform);
		    dirtyTransformGroups.add(this);
		    keySet.add(key);
                    ct = j3dCTs[index];
                    if (ct != null) {
                        targets.addCachedTargets(ct);
                        if (ct.targetArr[Targets.BLN_TARGETS] != null) {
                            gatherBlUsers(blUsers,
					ct.targetArr[Targets.BLN_TARGETS]);
                        }
                    }
		} else {
		    perPathData[index].switchDirty = true;
		    //System.err.println("tg.updateChild skip");
		}



		// update child's localToVworld of its children
		// transformLink may contain link nodes
		synchronized(childTransformLinks) {
		    for (i=0; i<childTransformLinks.size(); i++) {
			obj = childTransformLinks.get(i);

			if (obj instanceof TransformGroupRetained) {
			    tg = (TransformGroupRetained)obj;
			    tg.updateChildLocalToVworld(
					tg.localToVworldKeys[index],
					index, dirtyTransformGroups, keySet,
					targets, blUsers);
			} else { // LinkRetained
			    ln = (LinkRetained)obj;
			    currentKey.set(localToVworldKeys[index]);
			    currentKey.append(LinkRetained.plus).append(ln.nodeId);
			    if ((ln.sharedGroup != null) &&
				(ln.sharedGroup.localToVworldKeys != null)) {
				j = currentKey.equals(ln.sharedGroup.localToVworldKeys,0,
						      ln.sharedGroup.localToVworldKeys.length);
				if(j < 0) {
				    System.err.
					println("TransformGroupRetained : Can't find hashKey");
				}

				if (j < ln.sharedGroup.localToVworldKeys.length) {
				    ln.sharedGroup.
					updateChildLocalToVworld(ln.sharedGroup.
						 localToVworldKeys[j], j,
						dirtyTransformGroups, keySet,
						targets, blUsers);
				}
			    }
			}
		    }
		}
	    }
	}
    }

    // for non-shared case
    void updateChildLocalToVworld(ArrayList dirtyTransformGroups,
                                  ArrayList keySet,
				  UpdateTargets targets,
				  ArrayList blUsers) {
	int i, j;
	Object obj;
	Transform3D lToVw, childLToVw;
	TransformGroupRetained tg;
        LinkRetained ln;
	CachedTargets ct;

	synchronized(this) { // sync with setLive/clearLive

	    if (localToVworld != null) {
		perPathData[0].markedDirty = false;
		// update immediate child's localToVworld

		if (perPathData[0].switchState.currentSwitchOn ) {
		    lToVw = getCurrentLocalToVworld(0);
		    childLToVw = getUpdateChildLocalToVworld(0);
		    childLToVw.mul(lToVw, currentTransform);
		    dirtyTransformGroups.add(this);
                    ct = j3dCTs[0];
		    if (ct != null) {
		        targets.addCachedTargets(ct);
		        if (ct.targetArr[Targets.BLN_TARGETS] != null) {
                            gatherBlUsers(blUsers,
					ct.targetArr[Targets.BLN_TARGETS]);
		        }
		    }
		} else {
		    perPathData[0].switchDirty = true;
		    //System.err.println("tg.updateChild skip");
		}


		// update child's localToVworld of its children
		// transformLink contains top level transform group nodes
		// and link nodes
		synchronized(childTransformLinks) {
		    for (i=0; i<childTransformLinks.size(); i++) {
			obj = childTransformLinks.get(i);

			if (obj instanceof TransformGroupRetained) {
			    tg = (TransformGroupRetained)obj;
			    tg.updateChildLocalToVworld(dirtyTransformGroups,
						keySet, targets, blUsers);

			} else { // LinkRetained
			    ln = (LinkRetained)obj;
			    currentKey.reset();
			    currentKey.append(locale.nodeId);
			    currentKey.append(LinkRetained.plus).append(ln.nodeId);
			    if ((ln.sharedGroup != null) &&
				(ln.sharedGroup.localToVworldKeys != null)) {
				j = currentKey.equals(ln.sharedGroup.localToVworldKeys,0,
						      ln.sharedGroup.localToVworldKeys.length);
				if(j < 0) {
				    System.err.
					println("TransformGroupRetained : Can't find hashKey");
				}

				if (j<ln.sharedGroup.localToVworldKeys.length) {
				    ln.sharedGroup.
					updateChildLocalToVworld(
						ln.sharedGroup.
							localToVworldKeys[j],
						j, dirtyTransformGroups,
						keySet, targets, blUsers);
				}
			    }
			}
		    }
		}
	    }
	}
    }

    /**
     * Transform the input bound by the current LocalToVWorld, this
     * one overwrite the one defined in NodeRetained since for
     * TransformGroup, it has to use currentChildLocalToVworld
     * instead of currentLocalToVworld
     */
    void transformBounds(SceneGraphPath path, Bounds bound) {
	if (!((NodeRetained) path.item.retained).inSharedGroup) {
	    bound.transform(getCurrentChildLocalToVworld());
	} else {
	    HashKey key = new HashKey("");
	    path.getHashKey(key);
	    bound.transform(getCurrentChildLocalToVworld(key));
	}
    }


    /**
     * get the to be updated child localToVworld
     */
    Transform3D getUpdateChildLocalToVworld(int index) {
	int currentIndex = childLocalToVworldIndex[index][NodeRetained.CURRENT_LOCAL_TO_VWORLD];

	if (currentIndex == childLocalToVworldIndex[index][NodeRetained.LAST_LOCAL_TO_VWORLD]) {
	    currentIndex = currentIndex ^ 1;
	    childLocalToVworldIndex[index][NodeRetained.CURRENT_LOCAL_TO_VWORLD] = currentIndex;
	}
	return childLocalToVworld[index][currentIndex];
    }


    /**
     * Get the current child localToVworld transform for a node
     */
    Transform3D getCurrentChildLocalToVworld() {
	return getCurrentChildLocalToVworld(0);
    }

    Transform3D getCurrentChildLocalToVworld(int index) {
        return childLocalToVworld[index][childLocalToVworldIndex[index][NodeRetained.CURRENT_LOCAL_TO_VWORLD]];
    }

    Transform3D getCurrentChildLocalToVworld(HashKey key) {
	if (!inSharedGroup) {
            return childLocalToVworld[0][childLocalToVworldIndex[0][NodeRetained.CURRENT_LOCAL_TO_VWORLD]];
        } else {
	    int i = key.equals(localToVworldKeys, 0, localToVworldKeys.length);
	    if(i>= 0) {
		return childLocalToVworld[i]
		    [childLocalToVworldIndex[i][NodeRetained.CURRENT_LOCAL_TO_VWORLD]];
	    }
	}
        return new Transform3D();
    }


    /**
     * Get the last child localToVworld transform for a node
     */
    Transform3D getLastChildLocalToVworld(HashKey key) {

        if (!inSharedGroup) {
            return childLocalToVworld[0][childLocalToVworldIndex[0][NodeRetained.LAST_LOCAL_TO_VWORLD]];
        } else {
	    int i = key.equals(localToVworldKeys, 0, localToVworldKeys.length);
	    if(i>= 0) {
		return childLocalToVworld[i]
		    [childLocalToVworldIndex[i][NodeRetained.LAST_LOCAL_TO_VWORLD]];
            }
        }
        return new Transform3D();
    }

    // ****************************
    // TargetsInterface methods
    // ****************************

    public int getTargetThreads(int type) {
        // type is ignored here, only need for SharedGroup
        if (type == TargetsInterface.TRANSFORM_TARGETS) {
	    return targetThreads;
	} else {
	    System.err.println("getTargetsThreads: wrong arguments");
	    return -1;
	}
    }

    public CachedTargets getCachedTargets(int type, int index, int child) {
        // type is ignored here, only need for SharedGroup
        // child is ignored here
        if (type == TargetsInterface.TRANSFORM_TARGETS) {
	    return cachedTargets[index];
	} else {
	    System.err.println("getCachedTargets: wrong arguments");
	    return null;
	}
    }

    TargetsInterface getClosestTargetsInterface(int type) {
        return (type == TargetsInterface.TRANSFORM_TARGETS)?
                (TargetsInterface)this:
                (TargetsInterface)parentSwitchLink;
    }

    // re-evalute localTargetThreads using newCachedTargets and
    // re-evaluate targetThreads
    public void computeTargetThreads(int type,
				CachedTargets[] newCachedTargets) {

        // type is ignored here, only need for SharedGroup
        if (type == TargetsInterface.TRANSFORM_TARGETS) {
            localTargetThreads = J3dThread.UPDATE_TRANSFORM;

	    for(int i=0; i<newCachedTargets.length; i++) {
	        if (newCachedTargets[i] != null) {
		    localTargetThreads |= newCachedTargets[i].computeTargetThreads();
		}
	    }
	    targetThreads = localTargetThreads;

            int numLinks = childTransformLinks.size();
            TargetsInterface childLink;
 	    NodeRetained node;
            for(int i=0; i<numLinks; i++) {

                node = (NodeRetained)childTransformLinks.get(i);
    	        if (node.nodeType == NodeRetained.LINK) {
		    childLink = (TargetsInterface)
				((LinkRetained)node).sharedGroup;
	        } else {
		    childLink = (TargetsInterface) node;
	        }
		if (childLink != null) {
                    targetThreads |=
			childLink.getTargetThreads(TargetsInterface.TRANSFORM_TARGETS);
		}
	    }
        } else {
	    System.err.println("computeTargetsThreads: wrong arguments");
        }

    }

    // re-compute localTargetThread, targetThreads and
    // propagate changes to ancestors
    public void updateTargetThreads(int type,
				CachedTargets[] newCachedTargets) {
        // type is ignored here, only need for SharedGroup
        if (type == TargetsInterface.TRANSFORM_TARGETS) {
            computeTargetThreads(type, newCachedTargets);
            if (parentTransformLink != null) {
                TargetsInterface pti = (TargetsInterface)parentTransformLink;
                pti.propagateTargetThreads(TargetsInterface.TRANSFORM_TARGETS,
                                        targetThreads);
            }
        } else {
            System.err.println("updateTargetThreads: wrong arguments");
        }
    }

    // re-evaluate targetThreads using childTargetThreads and
    // propagate changes to ancestors
    public void propagateTargetThreads(int type, int childTargetThreads) {
        // type is ignored here, only need for SharedGroup

        if (type == TargetsInterface.TRANSFORM_TARGETS) {
	    // XXXX : For now we'll OR more than exact.
	    //targetThreads = localTargetThreads | childTargetThreads;
	    targetThreads = targetThreads | childTargetThreads;
            if (parentTransformLink != null) {
                TargetsInterface pti = (TargetsInterface)parentTransformLink;
                pti.propagateTargetThreads(TargetsInterface.TRANSFORM_TARGETS,
                                        targetThreads);
            }
        } else {
            System.err.println("propagateTargetThreads: wrong arguments");
        }
    }

    public void updateCachedTargets(int type, CachedTargets[] newCt) {
        // type is ignored here, only need for SharedGroup
        if (type == TargetsInterface.TRANSFORM_TARGETS) {
	    j3dCTs = newCt;
        } else {
	    System.err.println("updateCachedTargets: wrong arguments");
	}
    }

    public void copyCachedTargets(int type, CachedTargets[] newCt) {
        // type is ignored here, only need for SharedGroup
        if (type == TargetsInterface.TRANSFORM_TARGETS) {
	    int size = cachedTargets.length;
	    for (int i=0; i<size; i++) {
	        newCt[i] = cachedTargets[i];
            }
        } else {
	    System.err.println("copyCachedTargets: wrong arguments");
        }
    }

    public void resetCachedTargets(int type,
				CachedTargets[] newCtArr, int child) {
        // type is ignored here, only need for SharedGroup
        // child is ignored here
        if (type == TargetsInterface.TRANSFORM_TARGETS) {
            cachedTargets = newCtArr;
	} else {
	    System.err.println("resetCachedTargets: wrong arguments");
	}
    }

    public ArrayList getTargetsData(int type, int index) {
        // not used
	return null;
    }

    void childCheckSetLive(NodeRetained child, int childIndex,
                                SetLiveState s, NodeRetained linkNode) {
        s.currentTransforms = childLocalToVworld;
        s.currentTransformsIndex = childLocalToVworldIndex;
        s.parentTransformLink = this;
        s.childTransformLinks = childTransformLinks;
        s.localToVworld = s.currentTransforms;
        s.localToVworldIndex = s.currentTransformsIndex;

        child.setLive(s);
    }
}
