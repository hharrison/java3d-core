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

import java.util.*;

/**
 * Class specifying a wakeup when the specified object
 * collides with any other object in the scene graph.  
 *
 */
public final class WakeupOnCollisionEntry extends WakeupCriterion {

  // different types of WakeupIndexedList that use in GeometryStructure
    static final int COND_IN_GS_LIST = 0;
    static final int COLLIDEENTRY_IN_BS_LIST = 1;

    // total number of different IndexedUnorderedSet types
    static final int TOTAL_INDEXED_UNORDER_SET_TYPES = 2;

    /**
     * Use geometry in computing collisions.
     */
    public static final int USE_GEOMETRY = 10;

    /**
     * Use geometric bounds as an approximation in computing collisions.
     */
    public static final int USE_BOUNDS = 11;

    static final int GROUP = NodeRetained.GROUP;
    static final int BOUNDINGLEAF = NodeRetained.BOUNDINGLEAF;
    static final int SHAPE = NodeRetained.SHAPE;
    static final int MORPH = NodeRetained.MORPH;
    static final int ORIENTEDSHAPE3D = NodeRetained.ORIENTEDSHAPE3D;
    static final int BOUND = 0;

    /**
     * Accuracy mode one of USE_GEOMETRY or USE_BOUNDS
     */
    int accuracyMode;

    // Cached the arming Node being used when it is not BOUND
    NodeRetained armingNode;

    // A transformed Bounds of Group/Bounds, use by 
    // BOUND, GROUP
    Bounds vwcBounds = null;

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
    Bounds collidingBounds = null;
    SceneGraphPath collidingPath = null;

    /**
     * Constructs a new WakeupOnCollisionEntry criterion with 
     * USE_BOUNDS for a speed hint.
     * @param armingPath the path used to <em>arm</em> collision
     * detection
     * @exception IllegalArgumentException if object associated with the 
     * SceneGraphPath is other than a Group, Shape3D, Morph, or 
     * BoundingLeaf node.
     */
    public WakeupOnCollisionEntry(SceneGraphPath armingPath) {
	this(armingPath, USE_BOUNDS);
    }

    /**
     * Constructs a new WakeupOnCollisionEntry criterion.
     * @param armingPath the path used to <em>arm</em> collision
     * detection
     * @param speedHint one of USE_GEOMETRY or USE_BOUNDS, specifies how
     * accurately Java 3D will perform collision detection
     * @exception IllegalArgumentException if hint is not one of
     * USE_GEOMETRY or USE_BOUNDS.
     * @exception IllegalArgumentException if object associated with the 
     * SceneGraphPath is other than a Group, Shape3D, Morph, or 
     * BoundingLeaf node.
     */
    public WakeupOnCollisionEntry(SceneGraphPath armingPath, 
				  int speedHint) {
	this(new SceneGraphPath(armingPath), speedHint, null);
    }

    /**
     * Constructs a new WakeupOnCollisionEntry criterion.
     * @param armingNode the Group, Shape, or Morph node used to
     * <em>arm</em> collision detection
     * @exception IllegalArgumentException if object is under a
     * SharedGroup node or object is other than a Group, Shape3D,
     * Morph or BoundingLeaf node. 
     */
    public WakeupOnCollisionEntry(Node armingNode) {
	this(armingNode, USE_BOUNDS);
    }

    /**
     * Constructs a new WakeupOnCollisionEntry criterion.
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
    public WakeupOnCollisionEntry(Node armingNode, int speedHint) {
	this(new SceneGraphPath(null, armingNode), speedHint, null);
    }


    /**
     * Constructs a new WakeupOnCollisionEntry criterion.
     * @param armingBounds the bounds object used to <em>arm</em> collision
     * detection
     */
    public WakeupOnCollisionEntry(Bounds armingBounds) {
	this(null, USE_BOUNDS,  (Bounds) armingBounds.clone());
    }

    /**
     * Constructs a new WakeupOnCollisionEntry criterion.
     * @param armingPath the path used to <em>arm</em> collision
     * detection
     * @param speedHint one of USE_GEOMETRY or USE_BOUNDS, specifies how
     * accurately Java 3D will perform collision detection
     * @param armingBounds the bounds object used to <em>arm</em> collision
     * detection
     * @exception IllegalArgumentException if hint is not one of
     * USE_GEOMETRY or USE_BOUNDS.
     * @exception IllegalArgumentException if object associated with the 
     * SceneGraphPath is other than a Group, Shape3D, Morph, or 
     * BoundingLeaf node.
     */
    WakeupOnCollisionEntry(SceneGraphPath armingPath, 
			   int speedHint, Bounds armingBounds) {
	if (armingPath != null) {
	    this.armingNode = (NodeRetained) armingPath.getObject().retained;
	    nodeType = getNodeType(armingNode, armingPath, 
				   "WakeupOnCollisionEntry");  
	    this.armingPath = armingPath;
	    validateSpeedHint(speedHint, "WakeupOnCollisionEntry4");
	} else {
	    this.armingBounds = armingBounds;
	    nodeType = BOUND;
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
	    throw new IllegalStateException(J3dI18N.getString("WakeupOnCollisionEntry5"));
	}

	synchronized (behav) {
	    if (!behav.inCallback) {
		throw new IllegalStateException
		    (J3dI18N.getString("WakeupOnCollisionEntry5"));
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
	    throw new IllegalStateException(J3dI18N.getString("WakeupOnCollisionEntry6"));
	}

	synchronized (behav) {
	    if (!behav.inCallback) {
		throw new IllegalStateException
		    (J3dI18N.getString("WakeupOnCollisionEntry6"));
	    }
	}
	return (collidingBounds != null ?
		(Bounds)(collidingBounds.clone()): null);
    }


    /**
     * Node legality checker
     * throw Exception if node is not legal.
     * @return nodeType
     */
    static int getNodeType(NodeRetained armingNode, 
			   SceneGraphPath armingPath, String s)
	throws IllegalArgumentException {

	// check if SceneGraphPath is unique 
	// Note that graph may not live at this point so we
	// can't use node.inSharedGroup.
	if (!armingPath.validate()) {
	    throw new IllegalArgumentException(J3dI18N.getString(s + "7"));
	}

	if (armingNode.inBackgroundGroup) {
	    throw new IllegalArgumentException(J3dI18N.getString(s + "1"));
	}
	
	// This should come before Shape3DRetained check
	if (armingNode instanceof OrientedShape3DRetained) {
	    return ORIENTEDSHAPE3D;
	}

	if (armingNode instanceof Shape3DRetained) {
	    return SHAPE;
	}

	if (armingNode instanceof MorphRetained) {
	    return MORPH;
	}

	if (armingNode instanceof GroupRetained) {
	    return GROUP;
	}
	
	if (armingNode instanceof BoundingLeafRetained) {
	    return BOUNDINGLEAF;
	}

	throw new IllegalArgumentException(J3dI18N.getString(s + "0"));
    }

    /**
     * speedHint legality checker
     * throw Exception if speedHint is not legal
     */
    static void validateSpeedHint(int speedHint, String s) 
	throws IllegalArgumentException {
	if ((speedHint != USE_GEOMETRY) && (speedHint != USE_BOUNDS)) {
	    throw new IllegalArgumentException(J3dI18N.getString(s));
	}

    }


    /**
     * This is a callback from BehaviorStructure. It is 
     * used to add wakeupCondition to behavior structure.
     */
    void addBehaviorCondition(BehaviorStructure bs) {
	
	switch (nodeType) {
	  case SHAPE:  // Use geometryAtoms[].collisionBounds
	  case ORIENTEDSHAPE3D:
	      if (!armingNode.source.isLive()) {
		  return;
	      }
	      if (geometryAtoms == null) {
		  geometryAtoms = new UnorderList(1, GeometryAtom.class);
	      }
	      Shape3DRetained shape = (Shape3DRetained) armingNode; 
	      geometryAtoms.add(Shape3DRetained.getGeomAtom(shape.getMirrorShape(armingPath)));
	      break;
	  case MORPH:  // Use geometryAtoms[].collisionBounds
	      if (!armingNode.source.isLive()) {
		  return;
	      }
	      if (geometryAtoms == null) {
		  geometryAtoms = new UnorderList(1, GeometryAtom.class);
	      }
	      MorphRetained morph = (MorphRetained) armingNode;
	      geometryAtoms.add(Shape3DRetained.getGeomAtom(morph.getMirrorShape(armingPath)));
	      break;
 	  case BOUNDINGLEAF:  // use BoundingLeaf.transformedRegion
	      if (!armingNode.source.isLive()) {
		  return;
	      }
	      this.boundingLeaf = ((BoundingLeafRetained)  armingNode).mirrorBoundingLeaf;
	      break;
	  case BOUND: // use this.vwcBounds
	      vwcBounds = (Bounds) armingBounds.clone();
	      this.armingNode = behav;
	      break;
	  case GROUP:
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

	    path = getSceneGraphPath(shape.sourceNode, 
				     shape.key,
				     shape.getCurrentLocalToVworld(0));
	    bound = getTriggeringBounds(shape);
						       
	} else {
	    // Find the triggered Path & Bounds for this alternative
	    // collision target
	    GroupRetained  group = (GroupRetained) leaf;
	    path = getSceneGraphPath(group);
	    bound = getTriggeringBounds(group);
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
	if (nodeType == GROUP) {
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
	} else if (nodeType == BOUND) {
	    vwcBounds.transform(armingBounds, behav.getCurrentLocalToVworld());
	}
	
	if (reEvaluateGAs && 
	    (nodeType == GROUP) && 
	    (accuracyMode == USE_GEOMETRY)) {
	    geometryAtoms.clear();
	    ((GroupRetained) armingNode).searchGeometryAtoms(geometryAtoms);	    
	}
    }   
    

    /**
     * Return the TriggeringBounds for node
     */
    static Bounds getTriggeringBounds(Shape3DRetained mirrorShape) {
	NodeRetained node = mirrorShape.sourceNode;

	if (node instanceof Shape3DRetained) {
	    Shape3DRetained shape = (Shape3DRetained) node;
	    if (shape.collisionBound == null) {
		// TODO: get bounds by copy 
		return shape.getEffectiveBounds();
	    } 
	    return shape.collisionBound;		
	} 


	MorphRetained morph = (MorphRetained) node;
	if (morph.collisionBound == null) {
	    // TODO: get bounds by copy 
	    return morph.getEffectiveBounds();
	} 
	return morph.collisionBound;		
    }


    /**
     * Return the TriggeringBounds for node
     */
    static Bounds getTriggeringBounds(GroupRetained group) {
	if (group.collisionBound == null) {
	    // TODO: get bounds by copy 
	    return group.getEffectiveBounds();
	} 
	return group.collisionBound;		
    }

    static SceneGraphPath getSceneGraphPath(GroupRetained group) {
	// Find the transform base on the key
	Transform3D transform = null;
	GroupRetained srcGroup = group.sourceNode;

	synchronized (srcGroup.universe.sceneGraphLock) {
	    if (group.key == null) {
		transform = srcGroup.getCurrentLocalToVworld();
	    } else {
		HashKey keys[] = srcGroup.localToVworldKeys;
		if (keys == null) {
		    // the branch is already detach when
		    // Collision got this message
		    return null;
		}
		transform = srcGroup.getCurrentLocalToVworld(group.key);
	    }
	    return getSceneGraphPath(srcGroup, group.key, transform);
	}

    }

    /**
     * return the SceneGraphPath of the geomAtom. 
     * Find the alternative Collision target closest to the locale.
     */
    static SceneGraphPath getSceneGraphPath(NodeRetained startNode,
					    HashKey key,
					    Transform3D transform) {
	synchronized (startNode.universe.sceneGraphLock) {
	    NodeRetained target = startNode;

	    UnorderList path = new UnorderList(5, Node.class);
	    NodeRetained nodeR = target;
	    Locale locale = nodeR.locale;
	    String nodeId;
	    Vector parents;
	    NodeRetained linkR;
	
	    if (nodeR.inSharedGroup) {
		// getlastNodeId() will destroy this key
		if (key != null) {
		    key = new HashKey(key);
		} else {
		    key = new HashKey(startNode.localToVworldKeys[0]);
		}
	    }
	    
	    do {
		if (nodeR.source.getCapability(Node.ENABLE_COLLISION_REPORTING)){
		    path.add(nodeR.source);  
		}

		if (nodeR instanceof SharedGroupRetained) {
		    
		    // retrieve the last node ID
		    nodeId = key.getLastNodeId();
		    parents = ((SharedGroupRetained) nodeR).parents;
		    NodeRetained prevNodeR = nodeR;
		    for(int i=parents.size()-1; i >=0; i--) {
			linkR = (NodeRetained) parents.elementAt(i);
			if (linkR.nodeId.equals(nodeId)) {
			    nodeR = linkR;
			    break;
			}
		    }
		    if (nodeR == prevNodeR) {
			// the branch is already detach when
			// Collision got this message
			return null;
		    }
		} else if ((nodeR instanceof GroupRetained) &&
			   ((GroupRetained) nodeR).collisionTarget) {
		    // we need to find the collision target closest to the
		    // root of tree
		    target = nodeR;

		    if (key == null) {
			transform = nodeR.getCurrentLocalToVworld(null);
		    } else {
			transform = nodeR.getCurrentLocalToVworld(key);
		    }
		}
		nodeR = nodeR.parent;
	    } while (nodeR != null); // reach Locale
	    
	    Node nodes[];
	    if (target == startNode) { // in most case
		nodes = (Node []) path.toArray(false);
	    } else { // alternativeCollisionTarget is set
		nodes = (Node []) path.toArray(target);
	    }
	    SceneGraphPath sgpath = new SceneGraphPath(locale, 
						       nodes,
						       (Node) target.source);
	    sgpath.setTransform(transform);
	    return sgpath;
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
