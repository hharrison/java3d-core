/*
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
 */

package javax.media.j3d;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;


/**
 * RotPosPathInterpolator behavior.  This class defines a behavior that
 * modifies the rotational and translational components of its target
 * TransformGroup by linearly interpolating among a series of predefined
 * knot/positon and knot/orientation pairs (using the value generated
 * by the specified Alpha object).  The interpolated position and
 * orientation are used to generate a transform in the local coordinate
 * system of this interpolator.
 */

public class RotPosPathInterpolator extends PathInterpolator {
    private Transform3D rotation = new Transform3D();

    private Vector3f pos = new Vector3f();
    private Quat4f tQuat = new Quat4f();
    private Matrix4d tMat = new Matrix4d();

    // Arrays of quaternions and positions at each knot
    private Quat4f quats[];
    private Point3f positions[];
    private float prevInterpolationValue = Float.NaN;

    // We can't use a boolean flag since it is possible
    // that after alpha change, this procedure only run
    // once at alpha.finish(). So the best way is to
    // detect alpha value change.
    private float prevAlphaValue = Float.NaN;
    private WakeupCriterion passiveWakeupCriterion =
    (WakeupCriterion) new WakeupOnElapsedFrames(0, true);

    // non-public, default constructor used by cloneNode
    RotPosPathInterpolator() {
    }


    /**
     * Constructs a new interpolator that varies the rotation and translation
     * of the target TransformGroup's transform.
     * @param alpha the alpha object for this interpolator
     * @param target the TransformGroup node affected by this translator
     * @param axisOfTransform the transform that defines the local coordinate
     * system in which this interpolator operates
     * @param knots an array of knot values that specify interpolation points.
     * @param quats an array of quaternion values at the knots.
     * @param positions an array of position values at the knots.
     * @exception IllegalArgumentException if the lengths of the
     * knots, quats, and positions arrays are not all the same.
     */
    public RotPosPathInterpolator(Alpha alpha,
				  TransformGroup target,
				  Transform3D axisOfTransform,
				  float[] knots,
				  Quat4f[] quats,
				  Point3f[] positions) {
	super(alpha, target, axisOfTransform, knots);

	if (knots.length != positions.length)
	    throw new IllegalArgumentException(J3dI18N.getString("RotPosPathInterpolator0"));

	if (knots.length != quats.length)
	    throw new IllegalArgumentException(J3dI18N.getString("RotPosPathInterpolator0"));

	setPathArrays(quats, positions);
    }


    /**
     * Sets the quat at the specified index for this interpolator.
     * @param index the index to be changed
     * @param quat the new quat value
     */
    public void setQuat(int index, Quat4f quat) {
	this.quats[index].set(quat);
    }


    /**
     * Retrieves the quat value at the specified index.
     * @param index the index of the value requested
     * @param quat the quat to receive the quat value at the index
     */
    public void getQuat(int index, Quat4f quat) {
	quat.set(this.quats[index]);
    }


    /**
     * Sets the position at the specified index for this
     * interpolator.
     * @param index the index to be changed
     * @param position the new position value
     */
    public void setPosition(int index, Point3f position) {
	this.positions[index].set(position);
    }


    /**
     * Retrieves the position value at the specified index.
     * @param index the index of the value requested
     * @param position the position to receive the position value at the index
     */
    public void getPosition(int index, Point3f position) {
	position.set(this.positions[index]);
    }


    /**
     * Replaces the existing arrays of knot values, quaternion
     * values, and position values with the specified arrays.
     * The arrays of knots, quats, and positions are copied
     * into this interpolator object.
     * @param knots a new array of knot values that specify
     * interpolation points.
     * @param quats a new array of quaternion values at the knots.
     * @param positions a new array of position values at the knots.
     * @exception IllegalArgumentException if the lengths of the
     * knots, quats, and positions arrays are not all the same.
     *
     * @since Java 3D 1.2
     */
    public void setPathArrays(float[] knots,
			      Quat4f[] quats,
			      Point3f[] positions) {

	if (knots.length != quats.length)
	    throw new IllegalArgumentException(J3dI18N.getString("RotPosPathInterpolator0"));

	if (knots.length != positions.length)
	    throw new IllegalArgumentException(J3dI18N.getString("RotPosPathInterpolator0"));

	setKnots(knots);
	setPathArrays(quats, positions);
    }


    // Set the specific arrays for this path interpolator
    private void setPathArrays(Quat4f[] quats,
			       Point3f[] positions) {

	this.quats = new Quat4f[quats.length];
	for(int i = 0; i < quats.length; i++) {
	    this.quats[i] = new Quat4f();
	    this.quats[i].set(quats[i]);
	}

	this.positions = new Point3f[positions.length];
	for(int i = 0; i < positions.length; i++) {
	    this.positions[i] = new Point3f();
	    this.positions[i].set(positions[i]);
	}
    }


    /**
     * Copies the array of quaternion values from this interpolator
     * into the specified array.
     * The array must be large enough to hold all of the quats.
     * The individual array elements must be allocated by the caller.
     * @param quats array that will receive the quats.
     *
     * @since Java 3D 1.2
     */
    public void getQuats(Quat4f[] quats) {
	for (int i = 0; i < this.quats.length; i++)  {
	    quats[i].set(this.quats[i]);
	}
    }


    /**
     * Copies the array of position values from this interpolator
     * into the specified array.
     * The array must be large enough to hold all of the positions.
     * The individual array elements must be allocated by the caller.
     * @param positions array that will receive the positions.
     *
     * @since Java 3D 1.2
     */
    public void getPositions(Point3f[] positions) {
	for (int i = 0; i < this.positions.length; i++)  {
	    positions[i].set(this.positions[i]);
	}
    }

    /**
     * @deprecated As of Java 3D version 1.3, replaced by
     * <code>TransformInterpolator.setTransformAxis(Transform3D)</code>
     */

    public void setAxisOfRotPos(Transform3D axisOfRotPos) {
        setTransformAxis(axisOfRotPos);
    }

    /**
     * @deprecated As of Java 3D version 1.3, replaced by
     * <code>TransformInterpolator.getTransformAxis()</code>
     */
    public Transform3D getAxisOfRotPos() {
        return getTransformAxis();
    }


    /**
     * Computes the new transform for this interpolator for a given
     * alpha value.
     *
     * @param alphaValue alpha value between 0.0 and 1.0
     * @param transform object that receives the computed transform for
     * the specified alpha value
     *
     * @since Java 3D 1.3
     */
    public void computeTransform(float alphaValue, Transform3D transform) {
        double quatDot;

	computePathInterpolation(alphaValue);

	if (currentKnotIndex == 0 &&
	    currentInterpolationValue == 0f) {
	    tQuat.x = quats[0].x;
	    tQuat.y = quats[0].y;
	    tQuat.z = quats[0].z;
	    tQuat.w = quats[0].w;
	    pos.x = positions[0].x;
	    pos.y = positions[0].y;
	    pos.z = positions[0].z;
	} else {
	    quatDot = quats[currentKnotIndex].x *
		quats[currentKnotIndex+1].x +
		quats[currentKnotIndex].y *
		quats[currentKnotIndex+1].y +
		quats[currentKnotIndex].z *
		quats[currentKnotIndex+1].z +
		quats[currentKnotIndex].w *
		quats[currentKnotIndex+1].w;
	    if (quatDot < 0) {
		tQuat.x = quats[currentKnotIndex].x +
		    (-quats[currentKnotIndex+1].x -
		     quats[currentKnotIndex].x)*currentInterpolationValue;
		tQuat.y = quats[currentKnotIndex].y +
		    (-quats[currentKnotIndex+1].y -
		     quats[currentKnotIndex].y)*currentInterpolationValue;
		tQuat.z = quats[currentKnotIndex].z +
		    (-quats[currentKnotIndex+1].z -
		     quats[currentKnotIndex].z)*currentInterpolationValue;
		tQuat.w = quats[currentKnotIndex].w +
		    (-quats[currentKnotIndex+1].w -
		     quats[currentKnotIndex].w)*currentInterpolationValue;
	    } else {
		tQuat.x = quats[currentKnotIndex].x +
		    (quats[currentKnotIndex+1].x -
		     quats[currentKnotIndex].x)*currentInterpolationValue;
		tQuat.y = quats[currentKnotIndex].y +
		    (quats[currentKnotIndex+1].y -
		     quats[currentKnotIndex].y)*currentInterpolationValue;
		tQuat.z = quats[currentKnotIndex].z +
		    (quats[currentKnotIndex+1].z -
		     quats[currentKnotIndex].z)*currentInterpolationValue;
		tQuat.w = quats[currentKnotIndex].w +
		    (quats[currentKnotIndex+1].w -
		     quats[currentKnotIndex].w)*currentInterpolationValue;
	    }
	    pos.x = positions[currentKnotIndex].x +
		(positions[currentKnotIndex+1].x -
		 positions[currentKnotIndex].x) * currentInterpolationValue;
	    pos.y = positions[currentKnotIndex].y +
		(positions[currentKnotIndex+1].y -
		 positions[currentKnotIndex].y) * currentInterpolationValue;
	    pos.z = positions[currentKnotIndex].z +
		(positions[currentKnotIndex+1].z -
		 positions[currentKnotIndex].z) * currentInterpolationValue;
	}
	tQuat.normalize();

	// Set the rotation components
	tMat.set(tQuat);

	// Set the translation components.
	tMat.m03 = pos.x;
	tMat.m13 = pos.y;
	tMat.m23 = pos.z;
	rotation.set(tMat);

	// construct a Transform3D from:  axis * rotation * axisInverse
	transform.mul(axis, rotation);
	transform.mul(transform, axisInverse);
    }

    /**
     * Used to create a new instance of the node.  This routine is called
     * by <code>cloneTree</code> to duplicate the current node.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @see Node#cloneTree
     * @see Node#cloneNode
     * @see Node#duplicateNode
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public Node cloneNode(boolean forceDuplicate) {
        RotPosPathInterpolator rppi = new RotPosPathInterpolator();
        rppi.duplicateNode(this, forceDuplicate);
        return rppi;
    }



   /**
     * Copies all RotPosPathInterpolator information from
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

	RotPosPathInterpolator ri = (RotPosPathInterpolator) originalNode;

        int len = ri.getArrayLengths();

	// No API availble to set array size, so explicitly set it here
        positions = new Point3f[len];
        quats = new Quat4f[len];

        Point3f point = new Point3f();
        Quat4f quat = new Quat4f();

        for (int i = 0; i < len; i++) {
            positions[i] = new Point3f();
            ri.getPosition(i, point);
            setPosition(i, point);

            quats[i] = new Quat4f();
            ri.getQuat(i, quat);
            setQuat(i, quat);
        }

    }
}
