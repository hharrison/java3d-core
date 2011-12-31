/*
 * $RCSfile$
 *
 * Copyright 1999-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import javax.vecmath.*;

/**
 * The OrientedShape3D leaf node is a Shape3D node that is oriented
 * along a specified axis or about a specified point.  It defines an
 * alignment mode and a rotation point or axis.  This will cause
 * the local +<i>z</i> axis of the object to point at the viewer's eye
 * position. This is done regardless of the transforms above this
 * OrientedShape3D node in the scene graph.  It optionally defines a
 * scale value along with a constant scale enable flag that causes
 * this node to be scale invariant, subject only to its scale.
 *
 * <p>
 * OrientedShape3D is similar in functionality to the Billboard
 * behavior, but OrientedShape3D nodes will orient themselves
 * correctly for each view, and they can be used within a SharedGroup.
 *
 * <p>
 * If the alignment mode is ROTATE_ABOUT_AXIS, then the rotation will be
 * around the specified axis.  If the alignment mode is
 * ROTATE_ABOUT_POINT, then the rotation will be about the specified
 * point, with an additional rotation to align the +<i>y</i> axis of the
 * TransformGroup with the +<i>y</i> axis in the View.
 *
 * <p>
 * If the constant scale enable flag is set, the object will be drawn
 * the same size in absolute screen coordinates (meters), regardless
 * of the following: any transforms above this OrientedShape3D node in
 * the scene graph, the view scale, the window scale, or the effects
 * of perspective correction.  This is done by scaling the geometry
 * about the local origin of this node, such that 1 unit in local
 * coordinates is equal to the number of meters specified by this
 * node's scale value.  If the constant scale enable flag is set to
 * false, then the scale is not used.  The default scale is 1.0
 * meters.
 *
 * <p>
 * OrientedShape3D nodes are ideal for drawing screen-aligned text or
 * for drawing roughly-symmetrical objects.  A typical use might
 * consist of a quadrilateral that contains a texture of a tree.
 *
 * <p>
 * Note that in a multiple View system, picking and interestion test
 * is done with the primary View only.
 *
 * @see Billboard
 *
 * @since Java 3D 1.2
 */

public class OrientedShape3D extends Shape3D {

    /**
     * Specifies that rotation should be about the specified axis.
     * @see #setAlignmentMode
     */
    public static final int ROTATE_ABOUT_AXIS = 0;

    /**
     * Specifies that rotation should be about the specified point and
     * that the children's Y-axis should match the view object's Y-axis.
     * @see #setAlignmentMode
     */
    public static final int ROTATE_ABOUT_POINT = 1;

    /**
     * Specifies that no rotation is done.  The OrientedShape3D will
     * not be aligned to the view.
     * @see #setAlignmentMode
     *
     * @since Java 3D 1.3
     */
    public static final int ROTATE_NONE = 2;


    /**
     * Specifies that this OrientedShape3D node
     * allows reading its alignment mode information.
     */
    public static final int ALLOW_MODE_READ =
	CapabilityBits.ORIENTED_SHAPE3D_ALLOW_MODE_READ;

    /**
     * Specifies that this OrientedShape3D node
     * allows writing its alignment mode information.
     */
    public static final int ALLOW_MODE_WRITE =
	CapabilityBits.ORIENTED_SHAPE3D_ALLOW_MODE_WRITE;

    /**
     * Specifies that this OrientedShape3D node
     * allows reading its alignment axis information.
     */
    public static final int ALLOW_AXIS_READ =
	CapabilityBits.ORIENTED_SHAPE3D_ALLOW_AXIS_READ;

    /**
     * Specifies that this OrientedShape3D node
     * allows writing its alignment axis information.
     */
    public static final int ALLOW_AXIS_WRITE =
	CapabilityBits.ORIENTED_SHAPE3D_ALLOW_AXIS_WRITE;

    /**
     * Specifies that this OrientedShape3D node
     * allows reading its rotation point information.
     */
    public static final int ALLOW_POINT_READ =
	CapabilityBits.ORIENTED_SHAPE3D_ALLOW_POINT_READ;

    /**
     * Specifies that this OrientedShape3D node
     * allows writing its rotation point information.
     */
    public static final int ALLOW_POINT_WRITE =
	CapabilityBits.ORIENTED_SHAPE3D_ALLOW_POINT_WRITE;

    /**
     * Specifies that this OrientedShape3D node
     * allows reading its scale and constant scale enable information.
     *
     * @since Java 3D 1.3
     */
    public static final int ALLOW_SCALE_READ =
	CapabilityBits.ORIENTED_SHAPE3D_ALLOW_SCALE_READ;

    /**
     * Specifies that this OrientedShape3D node
     * allows writing its scale and constant scale enable information.
     *
     * @since Java 3D 1.3
     */
    public static final int ALLOW_SCALE_WRITE =
	CapabilityBits.ORIENTED_SHAPE3D_ALLOW_SCALE_WRITE;


   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
	ALLOW_MODE_READ,
	ALLOW_AXIS_READ,
	ALLOW_POINT_READ,
	ALLOW_SCALE_READ
    };

    /**
     * Constructs an OrientedShape3D node with default parameters.
     * The default values are as follows:
     * <ul>
     * alignment mode : ROTATE_ABOUT_AXIS<br>
     * alignment axis : Y-axis (0,1,0)<br>
     * rotation point : (0,0,1)<br>
     * constant scale enable : false<br>
     * scale : 1.0<br>
     *</ul>
     */
    public OrientedShape3D() {
	super();
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }


    /**
     * Constructs an OrientedShape3D node with the specified geometry
     * component, appearance component, mode, and axis.
     * The specified axis must not be parallel to the <i>Z</i>
     * axis--(0,0,<i>z</i>) for any value of <i>z</i>.  It is not
     * possible for the +<i>Z</i> axis to point at the viewer's eye
     * position by rotating about itself.  The target transform will
     * be set to the identity if the axis is (0,0,<i>z</i>).
     *
     * @param geometry the geometry component with which to initialize
     * this shape node
     * @param appearance the appearance component of the shape node
     * @param mode alignment mode, one of: ROTATE_ABOUT_AXIS,
     * ROTATE_ABOUT_POINT, or ROTATE_NONE
     * @param axis the ray about which the OrientedShape3D rotates
     */
    public OrientedShape3D(Geometry geometry,
			   Appearance appearance,
			   int mode,
			   Vector3f axis) {

	super(geometry, appearance);

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((OrientedShape3DRetained)retained).initAlignmentMode(mode);
        ((OrientedShape3DRetained)retained).initAlignmentAxis(axis);
    }

    /**
     * Constructs an OrientedShape3D node with the specified geometry
     * component, appearance component, mode, and rotation point.
     *
     * @param geometry the geometry component with which to initialize
     * this shape node
     * @param appearance the appearance component of the shape node
     * @param mode alignment mode, one of: ROTATE_ABOUT_AXIS,
     * ROTATE_ABOUT_POINT, or ROTATE_NONE
     * @param point the position about which the OrientedShape3D rotates
     */
    public OrientedShape3D(Geometry geometry,
			   Appearance appearance,
			   int mode,
			   Point3f point) {

	super(geometry, appearance);

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((OrientedShape3DRetained)retained).initAlignmentMode(mode);
        ((OrientedShape3DRetained)retained).initRotationPoint(point);

    }


    /**
     * Constructs an OrientedShape3D node with the specified geometry
     * component, appearance component, mode, axis, constant scale
     * enable flag, and scale
     * The specified axis must not be parallel to the <i>Z</i>
     * axis--(0,0,<i>z</i>) for any value of <i>z</i>.  It is not
     * possible for the +<i>Z</i> axis to point at the viewer's eye
     * position by rotating about itself.  The target transform will
     * be set to the identity if the axis is (0,0,<i>z</i>).
     *
     * @param geometry the geometry component with which to initialize
     * this shape node
     * @param appearance the appearance component of the shape node
     * @param mode alignment mode, one of: ROTATE_ABOUT_AXIS,
     * ROTATE_ABOUT_POINT, or ROTATE_NONE
     * @param axis the ray about which the OrientedShape3D rotates
     * @param constantScaleEnable a flag indicating whether to enable
     * constant scale
     * @param scale scale value used when constant scale is enabled
     *
     * @since Java 3D 1.3
     */
    public OrientedShape3D(Geometry geometry,
			   Appearance appearance,
			   int mode,
			   Vector3f axis,
			   boolean constantScaleEnable,
			   double scale) {

	super(geometry, appearance);

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((OrientedShape3DRetained)retained).initAlignmentMode(mode);
        ((OrientedShape3DRetained)retained).initAlignmentAxis(axis);
        ((OrientedShape3DRetained)retained).
	    initConstantScaleEnable(constantScaleEnable);
        ((OrientedShape3DRetained)retained).initScale(scale);
    }

    /**
     * Constructs an OrientedShape3D node with the specified geometry
     * component, appearance component, mode, and rotation point.
     *
     * @param geometry the geometry component with which to initialize
     * this shape node
     * @param appearance the appearance component of the shape node
     * @param mode alignment mode, one of: ROTATE_ABOUT_AXIS,
     * ROTATE_ABOUT_POINT, or ROTATE_NONE
     * @param point the position about which the OrientedShape3D rotates
     * @param constantScaleEnable a flag indicating whether to enable
     * constant scale
     * @param scale scale value used when constant scale is enabled
     *
     * @since Java 3D 1.3
     */
    public OrientedShape3D(Geometry geometry,
			   Appearance appearance,
			   int mode,
			   Point3f point,
			   boolean constantScaleEnable,
			   double scale) {

	super(geometry, appearance);

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((OrientedShape3DRetained)retained).initAlignmentMode(mode);
        ((OrientedShape3DRetained)retained).initRotationPoint(point);
        ((OrientedShape3DRetained)retained).
	    initConstantScaleEnable(constantScaleEnable);
        ((OrientedShape3DRetained)retained).initScale(scale);
    }


    /**
     * Creates the retained mode OrientedShape3DRetained object that this
     * OrientedShape3D object will point to.
     */
    void createRetained() {
        retained = new OrientedShape3DRetained();
        retained.setSource(this);
    }


    /**
     * Sets the alignment mode.
     *
     * @param mode alignment mode, one of: ROTATE_ABOUT_AXIS,
     * ROTATE_ABOUT_POINT, or ROTATE_NONE
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setAlignmentMode(int mode) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_MODE_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("OrientedShape3D0"));
        if (isLive())
            ((OrientedShape3DRetained)retained).setAlignmentMode(mode);
	else
            ((OrientedShape3DRetained)retained).initAlignmentMode(mode);
    }


    /**
     * Retrieves the alignment mode.
     *
     * @return one of: ROTATE_ABOUT_AXIS, ROTATE_ABOUT_POINT,
     * or ROTATE_NONE
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public int getAlignmentMode() {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_MODE_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("OrientedShape3D1"));
        return((OrientedShape3DRetained)retained).getAlignmentMode();
    }


    /**
     * Sets the new alignment axis.  This is the ray about which this
     * OrientedShape3D rotates when the mode is ROTATE_ABOUT_AXIS.
     * The specified axis must not be parallel to the <i>Z</i>
     * axis--(0,0,<i>z</i>) for any value of <i>z</i>.  It is not
     * possible for the +<i>Z</i> axis to point at the viewer's eye
     * position by rotating about itself.  The target transform will
     * be set to the identity if the axis is (0,0,<i>z</i>).
     *
     * @param axis the new alignment axis
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setAlignmentAxis(Vector3f axis) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_AXIS_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("OrientedShape3D2"));
        if (isLive())
            ((OrientedShape3DRetained)retained).setAlignmentAxis(axis);
	else
            ((OrientedShape3DRetained)retained).initAlignmentAxis(axis);
    }


    /**
     * Sets the new alignment axis.  This is the ray about which this
     * OrientedShape3D rotates when the mode is ROTATE_ABOUT_AXIS.
     * The specified axis must not be parallel to the <i>Z</i>
     * axis--(0,0,<i>z</i>) for any value of <i>z</i>.  It is not
     * possible for the +<i>Z</i> axis to point at the viewer's eye
     * position by rotating about itself.  The target transform will
     * be set to the identity if the axis is (0,0,<i>z</i>).
     *
     * @param x the x component of the alignment axis
     * @param y the y component of the alignment axis
     * @param z the z component of the alignment axis
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setAlignmentAxis(float x, float y, float z) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_AXIS_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("OrientedShape3D2"));
        if (isLive())
            ((OrientedShape3DRetained)retained).setAlignmentAxis(x,y,z);
	else
            ((OrientedShape3DRetained)retained).initAlignmentAxis(x,y,z);
    }


    /**
     * Retrieves the alignment axis of this OrientedShape3D node,
     * and copies it into the specified vector.
     *
     * @param axis the vector that will contain the alignment axis
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void getAlignmentAxis(Vector3f axis)  {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_AXIS_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("OrientedShape3D3"));
        ((OrientedShape3DRetained)retained).getAlignmentAxis(axis);
    }

    /**
     * Sets the new rotation point.  This is the point about which the
     * OrientedShape3D rotates when the mode is ROTATE_ABOUT_POINT.
     *
     * @param point the new rotation point
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setRotationPoint(Point3f point) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_POINT_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("OrientedShape3D4"));
        if (isLive())
            ((OrientedShape3DRetained)retained).setRotationPoint(point);
	else
            ((OrientedShape3DRetained)retained).initRotationPoint(point);
    }


    /**
     * Sets the new rotation point.  This is the point about which the
     * OrientedShape3D rotates when the mode is ROTATE_ABOUT_POINT.
     *
     * @param x the x component of the rotation point
     * @param y the y component of the rotation point
     * @param z the z component of the rotation point
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setRotationPoint(float x, float y, float z) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_POINT_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("OrientedShape3D4"));
        if (isLive())
            ((OrientedShape3DRetained)retained).setRotationPoint(x,y,z);
	else
            ((OrientedShape3DRetained)retained).initRotationPoint(x,y,z);
    }


    /**
     * Retrieves the rotation point of this OrientedShape3D node,
     * and copies it into the specified vector.
     *
     * @param point the point that will contain the rotation point
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void getRotationPoint(Point3f point)  {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_POINT_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("OrientedShape3D5"));
        ((OrientedShape3DRetained)retained).getRotationPoint(point);
    }


    /**
     * Sets the constant scale enable flag.
     *
     * @param constantScaleEnable a flag indicating whether to enable
     * constant scale
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public void setConstantScaleEnable(boolean constantScaleEnable) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_SCALE_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("OrientedShape3D6"));

        if (isLive())
	    ((OrientedShape3DRetained)retained).
		setConstantScaleEnable(constantScaleEnable);
	else
	    ((OrientedShape3DRetained)retained).
		initConstantScaleEnable(constantScaleEnable);
    }


    /**
     * Retrieves the constant scale enable flag.
     *
     * @return the current constant scale enable flag
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public boolean getConstantScaleEnable() {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_SCALE_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("OrientedShape3D7"));

	return ((OrientedShape3DRetained)retained).getConstantScaleEnable();
    }


    /**
     * Sets the scale for this OrientedShape3D.  This scale is used when
     * the constant scale enable flag is set to true.
     *
     * @param scale the scale value
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public void setScale(double scale) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_SCALE_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("OrientedShape3D8"));

        if (isLive())
	    ((OrientedShape3DRetained)retained).setScale(scale);
	else
	    ((OrientedShape3DRetained)retained).initScale(scale);
    }


    /**
     * Retrieves the scale value for this OrientedShape3D.
     *
     * @return the current scale value
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public double getScale() {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_SCALE_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("OrientedShape3D9"));

	return ((OrientedShape3DRetained)retained).getScale();
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
        OrientedShape3D s = new OrientedShape3D();
        s.duplicateNode(this, forceDuplicate);
        return s;
    }

    /**
     * Copies all node information from <code>originalNode</code> into
     * the current node.  This method is called from the
     * <code>cloneNode</code> method which is, in turn, called by the
     * <code>cloneTree</code> method.
     * <P>
     * For any <code>NodeComponent</code> objects
     * contained by the object being duplicated, each <code>NodeComponent</code>
     * object's <code>duplicateOnCloneTree</code> value is used to determine
     * whether the <code>NodeComponent</code> should be duplicated in the new node
     * or if just a reference to the current node should be placed in the
     * new node.  This flag can be overridden by setting the
     * <code>forceDuplicate</code> parameter in the <code>cloneTree</code>
     * method to <code>true</code>.
     * <br>
     * NOTE: Applications should <i>not</i> call this method directly.
     * It should only be called by the cloneNode method.
     *
     * @param originalNode the original node to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     * @exception ClassCastException if originalNode is not an instance of
     *  <code>Shape3D</code>
     *
     * @see Node#cloneTree
     * @see Node#cloneNode
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public void duplicateNode(Node originalNode, boolean forceDuplicate) {
	checkDuplicateNode(originalNode, forceDuplicate);
    }



   /**
     * Copies all Shape3D information from
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
	OrientedShape3DRetained attr = (OrientedShape3DRetained)
						originalNode.retained;
	OrientedShape3DRetained rt = (OrientedShape3DRetained) retained;

	rt.setAlignmentMode(attr.getAlignmentMode());
	Vector3f axis = new Vector3f();
	attr.getAlignmentAxis(axis);
	rt.setAlignmentAxis(axis);
	Point3f point = new Point3f();
	attr.getRotationPoint(point);
	rt.setRotationPoint(point);
    }
}
