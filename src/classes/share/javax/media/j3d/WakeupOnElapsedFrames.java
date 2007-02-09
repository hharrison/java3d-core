/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

/**
 * Class specifying a wakeup when a specific number of frames have
 * elapsed.  The wakeup criterion can either be passive or
 * non-passive.  If any behavior uses a non-passive
 * WakeupOnElapsedFrames, the rendering system will run continuously.
 *
 * <p>
 * In general, applications cannot count on behavior execution being
 * synchronized with rendering.  Behaviors that use
 * WakeupOnElapsedFrames with a frame count of 0 are an exception to
 * this general rule.  Such behaviors will be executed every frame.
 * Further, all modifications to scene graph objects (not including
 * geometry by-reference or texture by-reference) made from the
 * <code>processStimulus</code> methods of such behaviors are
 * guaranteed to take effect in the same rendering frame.
 */
public final class WakeupOnElapsedFrames extends WakeupCriterion {

    // different types of WakeupIndexedList that use in BehaviorStructure
    static final int COND_IN_BS_LIST = 0;

    // total number of different IndexedUnorderedSet types
    static final int TOTAL_INDEXED_UNORDER_SET_TYPES = 1;

    // Indicates whether the wakeup condition is passive or
    // non-passive.  Only behaviors using a non-passive
    // WakeupOnElapsedFrames will force a continuous traversal.
    boolean passive;

    // Number of frames before wakeup
    int frameCount;

    // When this reaches 0, this criterion is met.
    int countdown;

    /**
     * Constructs a non-passive WakeupOnElapsedFrames criterion.
     *
     * @param frameCount the number of frames that Java 3D should draw
     * before awakening this behavior object; a value of N means
     * wakeup at the end of frame N, where the current frame is zero,
     * a value of zero means wakeup at the end of the current frame.
     *
     * @exception IllegalArgumentException if frameCount is less than zero
     */
    public WakeupOnElapsedFrames(int frameCount) {
	this(frameCount, false);
    }

    /**
     * Constructs a WakeupOnElapsedFrames criterion.
     *
     * @param frameCount the number of frames that Java 3D should draw
     * before awakening this behavior object; a value of N means
     * wakeup at the end of frame N, where the current frame is zero,
     * a value of zero means wakeup at the end of the current frame.
     *
     * @param passive flag indicating whether this behavior is
     * passive; a non-passive behavior will cause the rendering system
     * to run continuously, while a passive behavior will only run
     * when some other event causes a frame to be run.
     *
     * @exception IllegalArgumentException if frameCount is less than zero
     *
     * @since Java 3D 1.2
     */
    public WakeupOnElapsedFrames(int frameCount, boolean passive) {
	if (frameCount < 0)
	    throw new IllegalArgumentException(J3dI18N.getString("WakeupOnElapsedFrames0"));

	this.frameCount = frameCount;
	this.passive = passive;
	WakeupIndexedList.init(this, TOTAL_INDEXED_UNORDER_SET_TYPES);
    }

    /**
     * Retrieves the elapsed frame count that was used when
     * constructing this object.
     *
     * @return the elapsed frame count specified when constructing
     * this object
     */
    public int getElapsedFrameCount() {
	return frameCount;
    }

    /**
     * Retrieves the state of the passive flag that was used when
     * constructing this object.
     *
     * @return true if this wakeup criterion is passive, false otherwise
     *
     * @since Java 3D 1.2
     */
    public boolean isPassive() {
	return passive;
    }

    /**
     * decrement the frame count, and set trigger if 0
     */
    void newFrame() {
	if (this.countdown == 0) {
	    this.setTriggered();
	} else {
	    this.countdown--;
	}
    }
  

   
    /**
     * This is a callback from BehaviorStructure. It is 
     * used to add wakeupCondition to behavior structure.
     */
    void addBehaviorCondition(BehaviorStructure bs) {
	this.countdown = this.frameCount;
	bs.wakeupOnElapsedFrames.add(this);
	if (!passive && (behav != null) && behav.enable) {
	    bs.activeWakeupOnFrameCount++;
	}

	// This is necessary to invoke this condition next time
	// Otherwise jftc won't work for static scene.
	VirtualUniverse.mc.sendRunMessage(bs.universe, 
					  J3dThread.UPDATE_BEHAVIOR);
    }


    /**
     * This is a callback from BehaviorStructure. It is 
     * used to remove wakeupCondition from behavior structure.
     */
    void removeBehaviorCondition(BehaviorStructure bs) {
	bs.wakeupOnElapsedFrames.remove(this);
	if (!passive && (behav != null) && behav.enable) {
	    bs.activeWakeupOnFrameCount--;
	}
    }


    /**
     * Perform task in addBehaviorCondition() that has to be
     * set every time the condition met.
     */
    void resetBehaviorCondition(BehaviorStructure bs) {
	this.countdown = this.frameCount;
    }

}
