/*
 * $RCSfile$
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;
import java.util.*;

/**
 * Class specifying a wakeup when the specified object
 * no longer collides with any other object in the scene graph.
 */
public final class WakeupOnCollisionExit extends WakeupCriterion {

  // different types of WakeupIndexedList that use in GeometryStructure
    static final int COND_IN_GS_LIST = 0;
    static final int COLLIDEEXIT_IN_BS_LIST = 1;

    // total number of different IndexedUnorderedSet types
    static final int TOTAL_INDEXED_UNORDER_SET_TYPES = 2;

    /**
     * Use geometry in computing collisions.
     */
    public static final int USE_GEOMETRY = WakeupOnCollisionEntry.USE_GEOMETRY;

    /**
     * Use geometric bounds as an approximation in computing collisions.
     */
    public static final int USE_BOUNDS = WakeupOnCollisionEntry.USE_BOUNDS;

    /**
     * Accuracy mode one of USE_GEOMETRY or USE_BOUNDS
     */
    int accuracyMode;

    // Cached the arming Node being used when it is not BOUND
    NodeRetained armingNode;

    // A transformed Bounds of Group/Bounds 
    // use by BOUND, GROUP
    Bounds vwcBounds;


    // use by GROUP to cache local bounds
    Bounds localBounds = null;

    // Use by BoundingLeaf, point to mirror BoundingLeaf
    // transformedRegion under this leaf is used.
    BoundingLeafRetained boundingLeaf = null;

    /**
     * Geometry atoms that this wakeup condition refer to.
     * Only use by SHAPE, MORPH, GROUP, ORIENTEDSHAPE
     */
    UnorderList geometryAtoms = null;
 
    // one of GROUP, BOUNDINGLEAF, SHAPE, MORPH, BOUND
    int nodeType;

    SceneGraphPath armingPath = null;
    Bounds armingBounds = null;

    // the following two references are set only after a collision
    // has occurred
    SceneGraphPath collidingPath = null;
    Bounds collidingBounds = null;

    /**
     * Constructs a new WakeupOnCollisionExit criterion.
     * @param armingPath the path used to <em>arm</em> collision
     * detection
     * @exception IllegalArgumentException if object associated with the
     * SceneGraphPath is other than a Group, Shape3D, Morph, or BoundingLeaf node.
     */
    public WakeupOnCollisionExit(SceneGraphPath armingPath) {
	this(armingPath, USE_BOUNDS);
    }

    /**
     * Constructs a new WakeupOnCollisionExit criterion.
     * @param armingPath the path used to <em>arm</em> collision
     * detection
     * @param speedHint one of USE_GEOMETRY or USE_BOUNDS, specifies how
     * accurately Java 3D will perform collision detection
     * @exception IllegalArgumentException if hint is not one of
     * USE_GEOMETRY or USE_BOUNDS.
     * @exception IllegalArgumentException if object associated with the
     * SceneGraphPath is other than a Group, Shape3D, Morph, or BoundingLeaf node.
     */
    public WakeupOnCollisionExit(SceneGraphPath armingPath, int speedHint) {
	this(new SceneGraphPath(armingPath), speedHint, null);
    }

    /**
     * Constructs a new WakeupOnCollisionExit criterion.
     * @param armingNode the Group, Shape, or Morph node used to
     * <em>arm</em> collision detection
     * @exception IllegalArgumentException if object is under a
     * SharedGroup node or object is other than a Group, Shape3D,
     * Morph or BoundingLeaf node. 
     */
    public WakeupOnCollisionExit(Node armingNode) {
	this(armingNode, USE_BOUNDS);
    }

    /**
     * Constructs a new WakeupOnCollisionExit criterion.
     * @param armingNode the Group, Shape, or Morph node used to
     * <em>arm</em> collision detection
     * @param speedHint one of USE_GEOMETRY or USE_BOUNDS, specifies how
     * accurately Java 3D will perform collision detection
     * @exception IllegalArgumentException if hint is not one of
     * USE_GEOMETRY or USE_BOUNDS.
     * @exception IllegalArgumentException if object is under a
     * SharedGroup node or object is other than a Group, Shape3D,
     * Morph or BoundingLeaf node. 
     */
    public WakeupOnCollisionExit(Node armingNode, int speedHint) {
	this(new SceneGraphPath(null, armingNode), speedHint, null);
    }


    /**
     * Constructs a new WakeupOnCollisionExit criterion.
     * @param armingBounds the bounds object used to <em>arm</em> collision
     * detection
     */
    public WakeupOnCollisionExit(Bounds armingBounds) {
	this(null, USE_BOUNDS,  (Bounds) armingBounds.clone());
    }

    /**
     * Constructs a new WakeupOnCollisionExit criterion.
     * @param armingPath the path used to <em>arm</em> collision
     * detection
     * @param speedHint one of USE_GEOMETRY or USE_BOUNDS, specifies how
     * accurately Java 3D will perform collision detection
     * @param armingBounds the bounds object used to <em>arm</em> collision
     * detection
     * @exception IllegalArgumentException if hint is not one of
     * USE_GEOMETRY or USE_BOUNDS.
     * @exception IllegalArgumentException if object associated with the
     * SceneGraphPath is other than a Group, Shape3D, Morph, or BoundingLeaf node.
     */
    WakeupOnCollisionExit(SceneGraphPath armingPath, 
			   int speedHint, Bounds armingBounds) {
	if (armingPath != null) {
	    this.armingNode = (NodeRetained) armingPath.getObject().retained;
	    nodeType = WakeupOnCollisionEntry.getNodeType(armingNode, armingPath,
					       "WakeupOnCollisionExit");
	    this.armingPath = armingPath;
	    WakeupOnCollisionEntry.validateSpeedHint(speedHint,
						     "WakeupOnCollisionExit4");
	} else {
	    this.armingBounds = armingBounds;	
	    nodeType = WakeupOnCollisionEntry.BOUND;    
	}
	accuracyMode = speedHint;
	WakeupIndexedList.init(this, TOTAL_INDEXED_UNORDER_SET_TYPES);
    }

    /**
     * Returns the path used in specifying the collision condition.
     * @return the SceneGraphPath object generated when arming this
     * criterion---null implies that a bounds object armed this criteria
     */
    public SceneGraphPath getArmingPath() {
	return (armingPath != null ? 
		new SceneGraphPath(armingPath) : null);
    }

    /**
     * Returns the bounds object used in specifying the collision condition.
     * @return the Bounds object generated when arming this
     * criterion---null implies that a SceneGraphPath armed this criteria
     */
    public Bounds getArmingBounds() {
	return (armingBounds != null ? 
		(Bounds)armingBounds.clone() : null);
    }

    /**
     * Retrieves the path describing the object causing the collision.
     * @return the SceneGraphPath that describes the triggering object.
     * @exception IllegalStateException if not called from within the 
     * a behavior's processStimulus method which was awoken by a collision.
     */
    public SceneGraphPath getTriggeringPath() {
	if (behav == null) {
	    throw new IllegalStateException(J3dI18N.getString("WakeupOnCollisionExit5"));
	}

	synchronized (behav) {
	    if (!behav.inCallback) {
		throw new IllegalStateException
		    (J3dI18N.getString("WakeupOnCollisionExit5"));
	    }
	}
	return (collidingPath != null ?
		new SceneGraphPath(collidingPath): null);
    }

    /**
     * Retrieves the Bounds object that caused the collision
     * @return the colliding Bounds object.
     * @exception IllegalStateException if not called from within the 
     * a behavior's processStimulus method which was awoken by a collision.
     */
    public Bounds getTriggeringBounds() {
	if (behav == null) {
	    throw new IllegalStateException(J3dI18N.getString("WakeupOnCollisionExit6"));
	}

	synchronized (behav) {
	    if (!behav.inCallback) {
		throw new IllegalStateException
		    (J3dI18N.getString("WakeupOnCollisionExit6"));
	    }
	}
	return (collidingBounds != null ?
		(Bounds)(collidingBounds.clone()): null);
    }

    /**
     * This is a callback from BehaviorStructure. It is 
     * used to add wakeupCondition to behavior structure.
     */
    void addBehaviorCondition(BehaviorStructure bs) {

	switch (nodeType) {
	  case WakeupOnCollisionEntry.SHAPE:  // Use geometryAtoms[].collisionBounds
	  case WakeupOnCollisionEntry.ORIENTEDSHAPE3D:
	      if (!armingNode.source.isLive()) {
		  return;
	      }
	      if (geometryAtoms == null) {
		  geometryAtoms = new UnorderList(1, GeometryAtom.class);
	      }
	      Shape3DRetained shape = (Shape3DRetained) armingNode; 
	      geometryAtoms.add(Shape3DRetained.getGeomAtom(shape.getMirrorShape(armingPath)));
	      break;
	  case WakeupOnCollisionEntry.MORPH:  // Use geometryAtoms[].collisionBounds
	      if (!armingNode.source.isLive()) {
		  return;
	      }
	      if (geometryAtoms == null) {
		  geometryAtoms = new UnorderList(1, GeometryAtom.class);
	      }
	      MorphRetained morph = (MorphRetained) armingNode;
	      geometryAtoms.add(Shape3DRetained.getGeomAtom(morph.getMirrorShape(armingPath)));
	      break;
 	  case WakeupOnCollisionEntry.BOUNDINGLEAF:  // use BoundingLeaf.transformedRegion
	      if (!armingNode.source.isLive()) {
		  return;
	      }
	      this.boundingLeaf = ((BoundingLeafRetained)  armingNode).mirrorBoundingLeaf;
	      break;
	  case WakeupOnCollisionEntry.BOUND: // use this.vwcBounds
	      vwcBounds = (Bounds) armingBounds.clone();
	      this.armingNode = behav;
	      break;
	  case WakeupOnCollisionEntry.GROUP:
	      if (!armingNode.source.isLive()) {
		  return;
	      }
	      if (accuracyMode == USE_GEOMETRY) {
		  if (geometryAtoms == null) {
		      geometryAtoms = new UnorderList(1, GeometryAtom.class);
		  }
		  ((GroupRetained) armingNode).searchGeometryAtoms(geometryAtoms);
	      } 
	      // else use this.vwcBounds
	  default: 
	}

	behav.universe.geometryStructure.addWakeupOnCollision(this);
    }


    /**
     * This is a callback from BehaviorStructure. It is 
     * used to remove wakeupCondition from behavior structure.
     */
    void removeBehaviorCondition(BehaviorStructure bs) {
	vwcBounds = null;
	if (geometryAtoms != null) {
	    geometryAtoms.clear();
	}
	boundingLeaf = null;
	behav.universe.geometryStructure.removeWakeupOnCollision(this);
    }



    // Set collidingPath & collidingBounds 
    void setTarget(BHLeafInterface leaf) {
	SceneGraphPath path;
	Bounds bound;

	if (leaf instanceof GeometryAtom) {
	    // Find the triggered Path & Bounds for this geometry Atom
	    GeometryAtom geomAtom = (GeometryAtom) leaf;
	    Shape3DRetained shape = geomAtom.source;
	    path = WakeupOnCollisionEntry.getSceneGraphPath(
			shape.sourceNode, 
			shape.key,
			shape.getCurrentLocalToVworld(0));
	    bound = WakeupOnCollisionEntry.getTriggeringBounds(shape);

	    
	} else {
	    // Find the triggered Path & Bounds for this alternative
	    // collision target
	    GroupRetained group = (GroupRetained) leaf;
	    path = WakeupOnCollisionEntry.getSceneGraphPath(group);
	    bound = WakeupOnCollisionEntry.getTriggeringBounds(group);
	}

	if (path != null) {
	    // colliding path may be null when branch detach before
	    // user behavior retrieve the previous colliding path
	    collidingPath = path;
	    collidingBounds = bound;
	}

    }

    // Invoke from GeometryStructure  to update vwcBounds of GROUP
    void updateCollisionBounds(boolean reEvaluateGAs){
	if (nodeType == WakeupOnCollisionEntry.GROUP) {
	    GroupRetained group = (GroupRetained) armingNode;
	    if (group.collisionBound != null) {
		vwcBounds = (Bounds) group.collisionBound.clone();
	    } else {
		// this may involve recursive tree traverse if
		// BoundsAutoCompute is true, we can't avoid
		// since the bound under it may change by transform
		vwcBounds = group.getEffectiveBounds(); 
	    }
	    group.transformBounds(armingPath, vwcBounds);
	} else if (nodeType == WakeupOnCollisionEntry.BOUND) {
	    vwcBounds.transform(armingBounds, behav.getCurrentLocalToVworld());
	}
	
	if (reEvaluateGAs && 
	    (nodeType == WakeupOnCollisionEntry.GROUP) && 
	    (accuracyMode == USE_GEOMETRY)) {
	    geometryAtoms.clear();
	    ((GroupRetained) armingNode).searchGeometryAtoms(geometryAtoms);	    
	}
    }   
    
   void setTriggered(){
	// if path not set, probably the branch is just detach.
	if (collidingPath != null) {
	    super.setTriggered();
	}
    }


    /**
     * Perform task in addBehaviorCondition() that has to be
     * set every time the condition met.
     */
    void resetBehaviorCondition(BehaviorStructure bs) {
	// The reference geometryAtom will not change once
	// Shape3D create so there is no need to set this.
    }
}
