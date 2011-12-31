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


import java.util.Vector;

/**
 * Interpolator is an abstract class that extends Behavior to provide
 * common methods used by various interpolation subclasses.  These
 * include methods to convert a time value into an alpha value (A
 * value in the range 0 to 1) and a method to initialize the behavior.
 * Subclasses provide the methods that convert alpha values into
 * values within that subclass' output range.
 */

public abstract class Interpolator extends Behavior {

    // This interpolator's alpha generator
    Alpha alpha;


    /**
     * Default WakeupCondition for all interpolators. The
     * wakeupOn method of Behavior, which takes a WakeupCondition as
     * the method parameter, will need to be called at the end
     * of the processStimulus method of any class that subclasses
     * Interpolator; this can be done with the following method call:
     * wakeupOn(defaultWakeupCriterion).
     */
    protected WakeupCriterion defaultWakeupCriterion =
	(WakeupCriterion) new WakeupOnElapsedFrames(0);


    /**
     * Constructs an Interpolator node with a null alpha value.
     */
    public Interpolator() {
    }


    /**
     * Constructs an Interpolator node with the specified alpha value.
     * @param alpha the alpha object used by this interpolator.
     * If it is null, then this interpolator will not run.
     */
    public Interpolator(Alpha alpha){
	this.alpha = alpha;
    }


    /**
      * Retrieves this interpolator's alpha object.
      * @return this interpolator's alpha object
      */
    public Alpha getAlpha() {
	return this.alpha;
    }


    /**
     * Set this interpolator's alpha to the specified alpha object.
     * @param alpha the new alpha object.  If set to null,
     * then this interpolator will stop running.
     */
    public void setAlpha(Alpha alpha) {
	this.alpha = alpha;
	VirtualUniverse.mc.sendRunMessage(J3dThread.RENDER_THREAD);
    }


    /**
     * This is the default Interpolator behavior initialization routine.
     * It schedules the behavior to awaken at the next frame.
     */
    public void initialize() {
	// Reset alpha
	//alpha.setStartTime(J3dClock.currentTimeMillis());

	// Insert wakeup condition into queue
	wakeupOn(defaultWakeupCriterion);
    }


   /**
     * Copies all Interpolator information from
     * <code>originalNode</code> into
     * the current node.  This method is called from the
     * <code>cloneNode</code> method which is, in turn, called by the
     * <code>cloneTree</code> method.<P>
     *
     * @param originalNode the original node to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @exception RestrictedAccessException if this object is part of a live
     *  or compiled scenegraph.
     *
     * @see Node#duplicateNode
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    void duplicateAttributes(Node originalNode, boolean forceDuplicate) {
        super.duplicateAttributes(originalNode, forceDuplicate);

	Interpolator it = (Interpolator) originalNode;

	Alpha a = it.getAlpha();
	if (a != null) {
  	    setAlpha(a.cloneAlpha());
	}
    }
}
