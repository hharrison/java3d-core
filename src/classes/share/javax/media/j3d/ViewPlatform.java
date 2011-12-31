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


/**
 * The ViewPlatform leaf node object controls the position, orientation
 * and scale of the viewer.  It is the node in the scene graph that a
 * View object connects to. A viewer navigates through the virtual
 * universe by changing the transform in the scene graph hierarchy above
 * the ViewPlatform.
 * <p>
 * <b>The View Attach Policy</b>
 * <p>
 * The actual view that Java 3D's renderer draws depends on the view
 * attach policy specified within the currently attached ViewPlatform.
 * The view attach policy, set by the setViewAttachPolicy
 * method, is one of the following:
 * <p>
 * <UL>
 * <LI>View.NOMINAL_HEAD - ensures that the end-user's nominal eye
 * position in the physical world corresponds to the virtual eye's
 * nominal eye position in the virtual world (the ViewPlatform's origin).
 * In essence, this policy tells Java 3D to position the virtual eyepoint
 * relative to the ViewPlatform origin in the same way as the physical
 * eyepoint is positioned relative to its nominal physical-world
 * origin. Deviations in the physical eye's position and orientation from
 * nominal in the physical world generate corresponding deviations of the
 * virtual eye's position and orientation in the virtual world. This
 * is the default view attach policy.</LI>
 * <p>
 * <LI>View.NOMINAL_FEET - ensures that the end-user's virtual feet
 * always touch the virtual ground. This policy tells Java 3D to compute
 * the physical-to-virtual-world correspondence in a way that enforces
 * this constraint. Java 3D does so by appropriately offsetting the
 * physical eye's position by the end-user's physical height. Java 3D
 * uses the nominalEyeHeightFromGround parameter found in the
 * PhysicalBody object to perform this computation.</LI>
 * <p>
 * <LI>View.NOMINAL_SCREEN - allows an application to always have
 * the virtual eyepoint appear at some "viewable" distance from a point
 * of interest. This policy tells Java 3D to compute the
 * physical-to-virtual-world correspondence in a way
 * that ensures that the renderer moves the nominal virtual eyepoint
 * away from the point of interest by the amount specified by the
 * nominalEyeOffsetFromNominalScreen parameter found in the
 * PhysicalBody object.</LI></UL>
 * <p>
 * <b>Activation Radius</b>
 * <p>
 * The ViewPlatform's activation radius defines an activation
 * volume surrounding the center of the ViewPlatform. This activation
 * volume is a spherical region that intersects with the scheduling regions
 * and application regions
 * of other leaf node objects to determine which of those objects may
 * affect rendering.  Only active view platforms--that is, view platforms
 * attached to a View--will be used to schedule or select other leaf nodes.
 * <p>
 * Different leaf objects interact with the ViewPlatform's activation
 * volume differently. The Background, Clip, and Soundscape leaf objects
 * each define a set of attributes and an application region in which
 * those attributes are applied. If more than one node of a given type
 * (Background, Clip, or Soundscape) intersects an active ViewPlatform's
 * activation volume, the "most appropriate" node is selected for that View.
 * Sound leaf objects and Behavior objects become active when
 * their scheduling region intersects an active ViewPlatform's activation
 * volume.
 * <p>
 * The activation radius is in view platform coordinates. For the
 * default screen scale policy of SCALE_SCREEN_SIZE, the
 * activationRadius parameter value is multiplied by half the
 * monitor screen size to derive the actual activation radius. For example,
 * for the default screen size of 0.35 meters, and the default activation
 * radius value of 62, the actual activation radius would be 10.85
 * meters.
 * <p>
 * <UL>
 * <code>62 * 0.35 / 2 = 10.85</code>
 * </UL>
 * <p>
 *
 * @see View
 */

public class ViewPlatform extends Leaf {

    /**
     * Specifies that the ViewPlatform allows read access to its view
     * attach policy information at runtime.
     */
    public static final int
    ALLOW_POLICY_READ = CapabilityBits.VIEW_PLATFORM_ALLOW_POLICY_READ;

    /**
     * Specifies that the ViewPlatform allows write access to its view
     * attach policy information at runtime.
     */
    public static final int
    ALLOW_POLICY_WRITE = CapabilityBits.VIEW_PLATFORM_ALLOW_POLICY_WRITE;

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_POLICY_READ
    };

    /**
     * Constructs a ViewPlatform object with default parameters.
     * The default values are as follows:
     * <ul>
     * view attach policy : View.NOMINAL_HEAD<br>
     * activation radius : 62<br>
     * </ul>
     */
    public ViewPlatform() {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Creates the retained mode ViewPlatformRetained object that this
     * ViewPlatform component object will point to.
     */
    void createRetained() {
	this.retained = new ViewPlatformRetained();
	this.retained.setSource(this);
    }


    /**
     * Sets the view attach policy that determines the coexistence center
     * in the virtual world. This policy determines how Java 3D places the
     * view platform relative to the position of the user's head, one of
     * View.NOMINAL_SCREEN, View.NOMINAL_HEAD, or View.NOMINAL_FEET.
     * The default policy is View.NOMINAL_HEAD.
     * @param policy the new policy
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @see View#NOMINAL_SCREEN
     * @see View#NOMINAL_HEAD
     * @see View#NOMINAL_FEET
     */
    public void setViewAttachPolicy(int policy) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_POLICY_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("ViewPlatform0"));

	switch (policy) {
	case View.NOMINAL_SCREEN:
	case View.NOMINAL_HEAD:
	case View.NOMINAL_FEET:
	    break;

	default:
	    throw new IllegalArgumentException(J3dI18N.getString("ViewPlatform1"));
	}

	((ViewPlatformRetained)this.retained).setViewAttachPolicy(policy);
    }

    /**
     * Returns the current coexistence center in virtual-world policy.
     * @return one of: View.NOMINAL_SCREEN, View.NOMINAL_HEAD, or
     * View.NOMINAL_FEET
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public int getViewAttachPolicy() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_POLICY_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("ViewPlatform2"));

	return ((ViewPlatformRetained)this.retained).getViewAttachPolicy();
    }

    /**
     * Set the ViewPlatform's activation radius which defines an activation
     * volume around the view platform.
     * @param activationRadius the new activation radius
     */
    public void setActivationRadius(float activationRadius) {
	((ViewPlatformRetained)this.retained).setActivationRadius(activationRadius);
    }

    /**
     * Get the ViewPlatform's activation radius.
     * @return the ViewPlatform activation radius
     */
    public float getActivationRadius() {
	return ((ViewPlatformRetained)this.retained).getActivationRadius();
    }


    /**
     * Used to create a new instance of the node.  This routine is called
     * by <code>cloneTree</code> to duplicate the current node.
     * <code>cloneNode</code> should be overridden by any user subclassed
     * objects.  All subclasses must have their <code>cloneNode</code>
     * method consist of the following lines:
     * <P><blockquote><pre>
     *     public Node cloneNode(boolean forceDuplicate) {
     *         UserSubClass usc = new UserSubClass();
     *         usc.duplicateNode(this, forceDuplicate);
     *         return usc;
     *     }
     * </pre></blockquote>
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @see Node#cloneTree
     * @see Node#duplicateNode
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public Node cloneNode(boolean forceDuplicate) {
        ViewPlatform v = new ViewPlatform();
        v.duplicateNode(this, forceDuplicate);
        return v;
    }


    /**
     * Copies all ViewPlatform information from <code>originalNode</code> into
     * the current node.  This method is called from the
     * <code>duplicateNode</code> method. This routine does
     * the actual duplication of all "local data" (any data defined in
     * this object).  It then will duplicate the retained side of the
     * tree if this method was called from its own 2 parameter
     * <code>duplicateNode</code> method.  This is designate by setting the
     * <code>duplicateRetained</code> flag to <code>true</code>.
     * Without this flag a <code>duplicateNode</code> method would not
     * whether or not to duplicate the retained side of the object.
     *
     * @param originalNode the original node to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     * @param duplicateRetained set to <code>true</code> when this
     *  method is should initiate the duplicateRetained call.  This
     *  call walks up a nodes superclasses so it should only be called
     *  once from the class of the original node.
     *
     * @see Node#duplicateNode
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */

    void duplicateAttributes(Node originalNode, boolean forceDuplicate) {
	super.duplicateAttributes(originalNode, forceDuplicate);

	ViewPlatformRetained attr =
	        (ViewPlatformRetained) originalNode.retained;
	ViewPlatformRetained rt = (ViewPlatformRetained) retained;

	rt.setActivationRadius(attr.getActivationRadius());
	rt.setViewAttachPolicy(attr.getViewAttachPolicy());
    }

}
