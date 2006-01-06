/*
 * $RCSfile$
 *
 * Copyright (c) 2006 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;
import java.util.ArrayList;

/**
 * Behavior is an abstract class that contains the framework for all
 * behavioral components in Java 3D.
 */

class BehaviorRetained extends LeafRetained  {
    // These bitmasks are used to quickly tell what conditions this behavior
    // is waiting for. Currently BehaviorStructure only used 4 of them.
    static final int WAKEUP_ACTIVATE_INDEX 	= 0;
    static final int WAKEUP_DEACTIVATE_INDEX 	= 1;
    static final int WAKEUP_VP_ENTRY_INDEX 	= 2;
    static final int WAKEUP_VP_EXIT_INDEX 	= 3;
    static final int WAKEUP_TIME_INDEX          = 4;

    static final int NUM_WAKEUPS		= 5;
    
    static final int WAKEUP_ACTIVATE 	= 0x0001;
    static final int WAKEUP_DEACTIVATE 	= 0x0002;
    static final int WAKEUP_VP_ENTRY 	= 0x0004;
    static final int WAKEUP_VP_EXIT 	= 0x0008;
    static final int WAKEUP_TIME        = 0x0010;
    
    /**
     * The number of scheduling intervals supported by this
     * implementation.  This is fixed for a particular implementation
     * and must be at least 10.
     */
    static final int NUM_SCHEDULING_INTERVALS = 10;

    // different types of IndexedUnorderedSet that use in  BehaviorStructure
    static final int BEHAIVORS_IN_BS_LIST = 0;
    static final int SCHEDULE_IN_BS_LIST  = 1;
    
    // total number of different IndexedUnorderedSet types
    static final int TOTAL_INDEXED_UNORDER_SET_TYPES = 2;

    /**
     * The Boundary object defining the behavior's scheduling region.
     */
    Bounds schedulingRegion = null;
    
    /** 
     * The bounding leaf reference
     */
    BoundingLeafRetained boundingLeaf = null;
    
    /**
     * The current wakeup condition.
     */
    WakeupCondition wakeupCondition = null;

    /**
     * This is the new WakeupCondition to be set in 
     * initialize wakeupOn() 
     */
    WakeupCondition newWakeupCondition = null;

    /**
     * The current view platform for this behavior; this value is
     * false until it comes into range of a view platform.
     */
    ViewPlatformRetained vp = null;
    
    /**
     * The current activation status for this behavior; this value
     * is false until it comes into range of a view platform.
     */
    boolean active = false;

    /**
     * Flag indicating whether the behavior is enabled.
     */
    boolean enable = true;
    
    /**
     * Current scheduling interval.
     */
    int schedulingInterval = NUM_SCHEDULING_INTERVALS / 2;

    /**
     * This is a flag that tells the behavior scheduler whether the
     * user-programmed process stimulus called wakeupOn, if it did
     * not, then the wakeupCondition will be set to null.
     */
    boolean conditionSet = false;
    
    /**
     * This is a flag that indicates whether we are in an initialize or
     * processStimulus callback.  If wakeupOn is called for this behavior
     * when this flag is not set, an exception will be thrown.
     */
    boolean inCallback = false;

    /**
     * This is a flag that indicates whether we are in initialize
     * callback. If wakeupOn is called for this behavior when
     * this flag is true, then its 
     * buildTree() will delay until insert nodes message
     * is get. This is because some localToVworld[] that wakeup
     * depends may not initialize when this behavior setLive().
     */
    boolean inInitCallback = false;

    /**
     * The transformed schedulingRegion
     */
    Bounds transformedRegion = null;
    
    // A bitmask that indicates that the scheduling region has changed.
    int isDirty = 0xffff;
    
    /**
     * A bitmask that represents all conditions that this behavior is waiting on.
     */
    int wakeupMask = 0;
    
    /**
     * An array of ints that count how many of each wakup is present
     */
    int[] wakeupArray = new int[NUM_WAKEUPS];

    // use to post message when bounds change, always point to this
    Object targets[] = new Object[1];

    BehaviorRetained() {
	this.nodeType = NodeRetained.BEHAVIOR;
	localBounds = new BoundingBox();
	((BoundingBox)localBounds).setLower( 1.0, 1.0, 1.0);
	((BoundingBox)localBounds).setUpper(-1.0,-1.0,-1.0);
	targets[0] = this;
	IndexedUnorderSet.init(this, TOTAL_INDEXED_UNORDER_SET_TYPES);
    }
    
    /**
     * Get the Behavior's scheduling region.
     * @return this Behavior's scheduling region information
     */
    Bounds getSchedulingBounds() {
        Bounds b = null;

        if (schedulingRegion != null) {
            b = (Bounds) schedulingRegion.clone();
            if (staticTransform != null) {
                Transform3D invTransform = staticTransform.getInvTransform();
                b.transform(invTransform);
            }
        }
        return b;
    }
    
    /**
     * Set the Behavior's scheduling region.
     * @param region a region that contains the Behavior's new scheduling
     * bounds
     */
    synchronized void setSchedulingBounds(Bounds region) {

	if (region != null) {
	    schedulingRegion = (Bounds) region.clone();
	    if (staticTransform != null) {
		schedulingRegion.transform(staticTransform.transform);
	    }
	} else {
	    schedulingRegion = null;
	}

	if (source != null && source.isLive()) {
	    sendMessage(J3dMessage.REGION_BOUND_CHANGED);
	}
    }

    /**
     * Set the Sound's scheduling region to the specified Leaf node.
     */  
    synchronized void setSchedulingBoundingLeaf(BoundingLeaf region) {

	if (source != null && source.isLive()) {
	    if (boundingLeaf != null)
		boundingLeaf.mirrorBoundingLeaf.removeUser(this);
	}

	if (region != null) {
	    boundingLeaf = (BoundingLeafRetained)region.retained;
	} else {
	    boundingLeaf = null;
	}

	if (source != null && source.isLive()) {
	    if (boundingLeaf != null)
		boundingLeaf.mirrorBoundingLeaf.addUser(this);
	    sendMessage(J3dMessage.REGION_BOUND_CHANGED);
	}
    }

    /**
     * Enables or disables this Behavior.  The default state is enabled.
     * @param  state  true or false to enable or disable this Behavior
     */
    void setEnable(boolean state) {
	if (enable != state) {
	    enable = state;
	    if (source != null && source.isLive()) {
		sendMessage(state ? J3dMessage.BEHAVIOR_ENABLE:
			            J3dMessage.BEHAVIOR_DISABLE);
	    }
	}
    }


    /**
     * Retrieves the state of the Behavior enable flag.
     * @return the Behavior enable state
     */
    boolean getEnable() {
	return enable;
    }


    /**
     * Sets the scheduling interval of this Behavior node to the
     * specified value.
     * @param schedulingInterval the new scheduling interval
     */
    void setSchedulingInterval(int schedulingInterval) {

	if ((source != null) && source.isLive()
	    && !inCallback) {
	    // avoid MT safe problem when user thread setting
	    // this while behavior scheduling using this.
	    sendMessage(J3dMessage.SCHEDULING_INTERVAL_CHANGED,
			new Integer(schedulingInterval));
	} else {
	    // garantee this setting reflect in next frame
	    this.schedulingInterval = schedulingInterval;
	}
    }


    /**
     * Retrieves the current scheduling interval of this Behavior
     * node.
     *
     * @return the current scheduling interval
     */
    int getSchedulingInterval() {
	return schedulingInterval;
    }


    /**
     * Get the Behavior's scheduling region
     */  
    BoundingLeaf getSchedulingBoundingLeaf() {
	return (boundingLeaf != null ?
		(BoundingLeaf)boundingLeaf.source : null);
    }

  /**
   * This setLive routine first calls the superclass's method, then
   * it activates all canvases that are associated with the attached
   * view.
   */
    synchronized void setLive(SetLiveState s) {

	super.doSetLive(s);
	if (inBackgroundGroup) {
	    throw new
		IllegalSceneGraphException(J3dI18N.getString("BehaviorRetained0"));
	}
	if (inSharedGroup) {
	    throw new
		IllegalSharingException(J3dI18N.getString("BehaviorRetained1"));
	}

	s.nodeList.add(this);
        s.behaviorNodes.add(this);
	s.notifyThreads |= J3dThread.UPDATE_BEHAVIOR;
        // process switch leaf
        if (s.switchTargets != null &&
                        s.switchTargets[0] != null) {
            s.switchTargets[0].addNode(this, Targets.BEH_TARGETS);
        }
        switchState = (SwitchState)s.switchStates.get(0);

	if (boundingLeaf != null) {
	    boundingLeaf.mirrorBoundingLeaf.addUser(this);
	}
	if (s.transformTargets != null && s.transformTargets[0] != null) {
            s.transformTargets[0].addNode(this, Targets.BEH_TARGETS);
	    s.notifyThreads |= J3dThread.UPDATE_TRANSFORM;
	}
	super.markAsLive();
    }
    
    /**
     * This clearLive routine first calls the superclass's method, then
     * it deactivates all canvases that are associated with the attached
     * view.
     */
    synchronized void clearLive(SetLiveState s) {
	super.clearLive(s);
	s.nodeList.add(this);
        if (s.transformTargets != null && s.transformTargets[0] != null) {
            s.transformTargets[0].addNode(this, Targets.BEH_TARGETS);
	    s.notifyThreads |= J3dThread.UPDATE_TRANSFORM;
        }
	s.notifyThreads |= J3dThread.UPDATE_BEHAVIOR;
        if (s.switchTargets != null &&
                        s.switchTargets[0] != null) {
            s.switchTargets[0].addNode(this, Targets.BEH_TARGETS);
        }
	if (boundingLeaf != null) {
	    boundingLeaf.mirrorBoundingLeaf.removeUser(this);
	}
	// BehaviorStructure removeBehavior() will do the
	// wakeupCondition.cleanTree() over there.
    }

   /**
    * This routine execute the user's initialize method
    */
    void executeInitialize() {

      synchronized (this) {
          boolean inCallbackSaved = inCallback;
          boolean inInitCallbackSaved = inInitCallback;

          inCallback = true;
          inInitCallback = true;
          try {
              ((Behavior)this.source).initialize();
          }
          catch (RuntimeException e) {
              inCallback = inCallbackSaved;
              inInitCallback = inInitCallbackSaved;
              System.err.println("Exception occurred during Behavior initialization:");
              e.printStackTrace();
          }
          inCallback = inCallbackSaved;
          inInitCallback = inInitCallbackSaved;
      }
    }

    /**
     * Defines this behavior's wakeup criteria.
     * @param criteria The wakeup criterion for this object
     */
    void wakeupOn(WakeupCondition criteria) {
	// If not call by initialize(), buildTree will
	// delay until insertNodes in BehaviorStructure
	// Otherwise BehaviorScheduler will invoke
	// handleLastWakeupOn()
	if (criteria == null) {
	    throw new NullPointerException(J3dI18N.getString("BehaviorRetained2"));
	}

	if (!inInitCallback) {
	    conditionSet = true;
	    wakeupCondition = criteria;
	} else {
	    // delay setting wakeup condition in BehaviorStructure
	    // activateBehaviors(). This is because there may have
	    // previously wakeupCondition attach to it and
	    // scheduling even after clearLive() due to message
	    // delay processing. It is not MT safe to set it
	    // in user thread.
	    newWakeupCondition = criteria;
	}

    }

    // The above wakeupOn() just remember the reference
    // We only need to handle (and ignore the rest) the
    // last wakeupOn() condition set in the behavior.
    // This handle the case when multiple wakeupOn() 
    // are invoked in the same processStimulus()
    void handleLastWakeupOn(WakeupCondition prevWakeupCond,
			    BehaviorStructure bs) {

	if (bs == universe.behaviorStructure) {
	    if (wakeupCondition == prevWakeupCond) {
		// reuse the same wakeupCondition
		wakeupCondition.resetTree();
	    } else {
		if (prevWakeupCond != null) {
		    prevWakeupCond.cleanTree(bs);
		}
		wakeupCondition.buildTree(null, 0, this);
	    }
	} else {
	    // No need to do prevWakeupCond.cleanTree(bs)
	    // since removeBehavior() will do so
	}
    }

    
    /** 
     * Returns this behavior's wakeup criteria.
     * @return criteria The wakeup criteria of this object
     */
    WakeupCondition getWakeupCondition() {
	return wakeupCondition;
    }
    
    /**
     * Post the specified Id.  Behaviors use this method to cause sequential
     * scheduling of other behavior object.
     * @param postId The Id being posted
     */
    
    void postId(int postId){
	if (source != null && source.isLive()) {
	    universe.behaviorStructure.handleBehaviorPost((Behavior) source, postId);
	}
    }
    
    protected View getView() {
	return (universe != null ? 
		universe.getCurrentView() : null);
    }

    synchronized void updateTransformRegion(Bounds bound) {
	if (boundingLeaf == null) {
	    updateTransformRegion();
	} else {
	    if (bound == null) {
		transformedRegion = null;
	    } else {
		transformedRegion = (Bounds) bound.clone();
		transformedRegion.transform(
		  boundingLeaf.mirrorBoundingLeaf.getCurrentLocalToVworld());
	    }
	}
    }

    synchronized void updateTransformRegion() {
	if (boundingLeaf == null ||
		!boundingLeaf.mirrorBoundingLeaf.switchState.currentSwitchOn) {
	    if (schedulingRegion == null) {
		transformedRegion = null;
	    } else {
		// use schedulingRegion
		if (transformedRegion != null) {
		    transformedRegion.set(schedulingRegion);
		} else {
		    transformedRegion = (Bounds) schedulingRegion.clone();
		}
		transformedRegion.transform(getCurrentLocalToVworld());

	    }
	} else {
	    // use boundingLeaf
	    transformedRegion =
		boundingLeaf.mirrorBoundingLeaf.transformedRegion;
	    
	}
    }


    // Note: This routine will only to update the object's
    // transformed region
    void updateBoundingLeaf(long refTime) {
	transformedRegion = (Bounds)boundingLeaf.mirrorBoundingLeaf.transformedRegion;
    }


    void addWakeupCondition() {}

    final void sendMessage(int mtype, Object arg) {
	J3dMessage createMessage = VirtualUniverse.mc.getMessage();
	createMessage.threads = J3dThread.UPDATE_BEHAVIOR;
	createMessage.type = mtype;
        createMessage.universe = universe;
	createMessage.args[0] = targets;
	createMessage.args[1]= this;
	createMessage.args[2]= arg;
	VirtualUniverse.mc.processMessage(createMessage);
    }

    final void sendMessage(int mtype) {
	sendMessage(mtype, null);
    }

    void mergeTransform(TransformGroupRetained xform) {
        super.mergeTransform(xform);
        if (schedulingRegion != null) {
            schedulingRegion.transform(xform.transform);
        }
        if (source instanceof DistanceLOD) {
	    ((DistanceLOD)source).mergeTransform(xform);
        }
    }
}
