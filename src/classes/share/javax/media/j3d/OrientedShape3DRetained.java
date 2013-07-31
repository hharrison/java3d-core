/*
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
 */

package javax.media.j3d;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4d;

class OrientedShape3DRetained extends Shape3DRetained {

    static final int ALIGNMENT_CHANGED          = LAST_DEFINED_BIT << 1;
    static final int AXIS_CHANGED               = LAST_DEFINED_BIT << 2;
    static final int ROTATION_CHANGED           = LAST_DEFINED_BIT << 3;
    static final int CONSTANT_SCALE_CHANGED     = LAST_DEFINED_BIT << 4;
    static final int SCALE_FACTOR_CHANGED       = LAST_DEFINED_BIT << 5;


    int mode = OrientedShape3D.ROTATE_ABOUT_AXIS;

    // Axis about which to rotate.
    Vector3f axis = new Vector3f(0.0f, 1.0f, 0.0f);
    Point3f rotationPoint = new Point3f(0.0f, 0.0f, 1.0f);
    private Vector3d nAxis = new Vector3d(0.0, 1.0, 0.0); // normalized axis

    // reused temporaries
    private Point3d viewPosition = new Point3d();
    private Point3d yUpPoint = new Point3d();

    private Vector3d eyeVec = new Vector3d();
    private Vector3d yUp = new Vector3d();
    private Vector3d zAxis  = new Vector3d();
    private Vector3d yAxis  = new Vector3d();
    private Vector3d vector = new Vector3d();

    private AxisAngle4d aa = new AxisAngle4d();

    private Transform3D xform = new Transform3D(); // used several times
    private Transform3D zRotate = new Transform3D();

    // For scale invariant mode
    boolean constantScale = false;
    double scaleFactor = 1.0;

    // Frequently used variables for scale invariant computation
    // Left and right Vworld to Clip coordinates transforms
    private Transform3D left_xform = new Transform3D();
    private Transform3D right_xform = new Transform3D();

    // Transform for scaling the OrientedShape3D to correct for
    // perspective foreshortening
    Transform3D scaleXform = new Transform3D();

    // Variables for converting between clip to local world coords
    private Vector4d im_vec[] = {new Vector4d(), new Vector4d()};
    private Vector4d lvec = new Vector4d();

    boolean orientedTransformDirty = true;

    Transform3D[] orientedTransforms = new Transform3D[1];
    static final double EPSILON = 1.0e-6;


    /**
     * Constructs a OrientedShape3D node with default parameters.
     * The default values are as follows:
     * <ul>
     * alignment mode : ROTATE_ABOUT_AXIS<br>
     * alignment axis : Y-axis (0,1,0)<br>
     * rotation point : (0,0,1)<br>
     *</ul>
     */
    public OrientedShape3DRetained() {
	super();
        this.nodeType = NodeRetained.ORIENTEDSHAPE3D;
    }

    // initializes alignment mode
    void initAlignmentMode(int mode) {
	this.mode = mode;
    }

    /**
     * Sets the alignment mode.
     * @param mode one of: ROTATE_ABOUT_AXIS or ROTATE_ABOUT_POINT
     */
    void setAlignmentMode(int mode) {
	if (this.mode != mode) {
	    initAlignmentMode(mode);
	    sendChangedMessage(ALIGNMENT_CHANGED, new Integer(mode));
	}
    }

    /**
     * Retrieves the alignment mode.
     * @return one of: ROTATE_ABOUT_AXIS or ROTATE_ABOUT_POINT
     */
    int getAlignmentMode() {
	return(mode);
    }

    // initializes alignment axis
    void initAlignmentAxis(Vector3f axis) {
	initAlignmentAxis(axis.x, axis.y, axis.z);
    }

    // initializes alignment axis
    void initAlignmentAxis(float x, float y, float z) {
        this.axis.set(x,y,z);
        double invMag;
        invMag =  1.0/Math.sqrt(axis.x*axis.x + axis.y*axis.y + axis.z*axis.z);
        nAxis.x = (double)axis.x*invMag;
        nAxis.y = (double)axis.y*invMag;
        nAxis.z = (double)axis.z*invMag;
    }

    /**
     * Sets the new alignment axis.  This is the ray about which this
     * OrientedShape3D rotates when the mode is ROTATE_ABOUT_AXIS.
     * @param axis the new alignment axis
     */
    void setAlignmentAxis(Vector3f axis) {
        setAlignmentAxis(axis.x, axis.y, axis.z);
    }

    /**
     * Sets the new alignment axis.  This is the ray about which this
     * OrientedShape3D rotates when the mode is ROTATE_ABOUT_AXIS.
     * @param x the x component of the alignment axis
     * @param y the y component of the alignment axis
     * @param z the z component of the alignment axis
     */
    void setAlignmentAxis(float x, float y, float z) {
        initAlignmentAxis(x,y,z);

	if (mode == OrientedShape3D.ROTATE_ABOUT_AXIS) {
	    sendChangedMessage(AXIS_CHANGED, new Vector3f(x,y,z));
	}
    }

    /**
     * Retrieves the alignment axis of this OrientedShape3D node,
     * and copies it into the specified vector.
     * @param axis the vector that will contain the alignment axis
     */
    void getAlignmentAxis(Vector3f axis)  {
        axis.set(this.axis);
    }

    // initializes rotation point
    void initRotationPoint(Point3f point) {
	rotationPoint.set(point);
    }

    // initializes rotation point
    void initRotationPoint(float x, float y, float z) {
	rotationPoint.set(x,y,z);
    }

    /**
     * Sets the new rotation point.  This is the point about which the
     * OrientedShape3D rotates when the mode is ROTATE_ABOUT_POINT.
     * @param point the new rotation point
     */
    void setRotationPoint(Point3f point) {
	setRotationPoint(point.x, point.y, point.z);
    }

    /**
     * Sets the new rotation point.  This is the point about which the
     * OrientedShape3D rotates when the mode is ROTATE_ABOUT_POINT.
     * @param x the x component of the rotation point
     * @param y the y component of the rotation point
     * @param z the z component of the rotation point
     */
    void setRotationPoint(float x, float y, float z) {
	initRotationPoint(x,y,z);

	if (mode == OrientedShape3D.ROTATE_ABOUT_POINT) {
	    sendChangedMessage(ROTATION_CHANGED, new Point3f(x,y,z));
	}
    }

    /**
     * Retrieves the rotation point of this OrientedShape3D node,
     * and copies it into the specified vector.
     * @param axis the point that will contain the rotation point
     */
    void getRotationPoint(Point3f point)  {
        point.set(rotationPoint);
    }

    void setConstantScaleEnable(boolean enable) {
	if(constantScale != enable) {
	    initConstantScaleEnable(enable);
	    sendChangedMessage(CONSTANT_SCALE_CHANGED, new Boolean(enable));
	}
    }

    boolean getConstantScaleEnable() {
	return constantScale;
    }

    void initConstantScaleEnable(boolean cons_scale) {
	constantScale = cons_scale;
    }

    void setScale(double scale) {
	initScale(scale);
	if(constantScale)
	    sendChangedMessage(SCALE_FACTOR_CHANGED, new Double(scale));
    }

    void initScale(double scale) {
	scaleFactor = scale;
    }

    double getScale() {
	return scaleFactor;
    }

    void sendChangedMessage(int component, Object attr) {
        J3dMessage changeMessage = new J3dMessage();
        changeMessage.type = J3dMessage.ORIENTEDSHAPE3D_CHANGED;
        changeMessage.threads = targetThreads ;
        changeMessage.universe = universe;
        changeMessage.args[0] = getGeomAtomsArray(mirrorShape3D);
	changeMessage.args[1] = new Integer(component);
	changeMessage.args[2] = attr;
	OrientedShape3DRetained[] o3dArr =
	    new OrientedShape3DRetained[mirrorShape3D.size()];
	mirrorShape3D.toArray(o3dArr);
	changeMessage.args[3] = o3dArr;
	changeMessage.args[4] = this;
        VirtualUniverse.mc.processMessage(changeMessage);
    }

    @Override
    void updateImmediateMirrorObject(Object[] args) {
	int component = ((Integer)args[1]).intValue();
	if ((component & (ALIGNMENT_CHANGED      |
			  AXIS_CHANGED           |
			  ROTATION_CHANGED       |
			  CONSTANT_SCALE_CHANGED |
			  SCALE_FACTOR_CHANGED)) != 0) {
	    OrientedShape3DRetained[] msArr = (OrientedShape3DRetained[])args[3];
	    Object obj = args[2];
	    if ((component & ALIGNMENT_CHANGED) != 0) {
		int mode = ((Integer)obj).intValue();
		for (int i=0; i< msArr.length; i++) {
		    msArr[i].initAlignmentMode(mode);
		}
	    }
	    else if ((component & AXIS_CHANGED) != 0) {
		Vector3f axis =(Vector3f) obj;
		for (int i=0; i< msArr.length; i++) {
		     msArr[i].initAlignmentAxis(axis);
		}
	    }
	    else if ((component & ROTATION_CHANGED) != 0) {
		Point3f point =(Point3f) obj;
		for (int i=0; i< msArr.length; i++) {
		    msArr[i].initRotationPoint(point);
		}
	    }
	    else if((component & CONSTANT_SCALE_CHANGED) != 0) {
		boolean bool = ((Boolean)obj).booleanValue();
		for (int i=0; i< msArr.length; i++) {
		    msArr[i].initConstantScaleEnable(bool);
		}
	    }
	    else if((component & SCALE_FACTOR_CHANGED) != 0) {
		double scale = ((Double)obj).doubleValue();
		for (int i=0; i< msArr.length; i++) {
		    msArr[i].initScale(scale);
		}
	    }
	}
	else {
	    super.updateImmediateMirrorObject(args);
	}
    }


    Transform3D getOrientedTransform(int viewIndex) {
	synchronized(orientedTransforms) {
	    if (viewIndex >= orientedTransforms.length) {
		Transform3D xform = new Transform3D();
		Transform3D[] newList = new Transform3D[viewIndex+1];
		for (int i = 0; i < orientedTransforms.length; i++) {
		    newList[i] = orientedTransforms[i];
		}
		newList[viewIndex] = xform;
		orientedTransforms = newList;
	    }
	    else {
		if (orientedTransforms[viewIndex] == null) {
		    orientedTransforms[viewIndex] = new Transform3D();
		}
	    }
	}
	return orientedTransforms[viewIndex];
    }

    // called on the parent object
    // Should be synchronized so that the user thread does not modify the
    // OrientedShape3D params while computing the transform
    synchronized void updateOrientedTransform(Canvas3D canvas, int viewIndex) {
        double angle = 0.0;
        double sign;
	boolean status;

	Transform3D orientedxform = getOrientedTransform(viewIndex);
	//  get viewplatforms's location in virutal world
        if (mode == OrientedShape3D.ROTATE_ABOUT_AXIS) {   // rotate about axis
	    canvas.getCenterEyeInImagePlate(viewPosition);
	    canvas.getImagePlateToVworld(xform); // xform is imagePlateToLocal
	    xform.transform(viewPosition);

	    // get billboard's transform
	    xform.set(getCurrentLocalToVworld());
	    xform.invert(); // xform is now vWorldToLocal

	    // transform the eye position into the billboard's coordinate system
	    xform.transform(viewPosition);


	    // eyeVec is a vector from the local origin to the eye pt in local
	    eyeVec.set(viewPosition);
	    eyeVec.normalize();

	    // project the eye into the rotation plane
	    status = projectToPlane(eyeVec, nAxis);

	    if (status) {
		// project the z axis into the rotation plane
		zAxis.x = 0.0;
		zAxis.y = 0.0;
		zAxis.z = 1.0;
		status = projectToPlane(zAxis, nAxis);
	    }
	    if (status) {

		// compute the sign of the angle by checking if the cross product
		// of the two vectors is in the same direction as the normal axis
		vector.cross(eyeVec, zAxis);
		if (vector.dot(nAxis) > 0.0) {
		    sign = 1.0;
		} else {
		    sign = -1.0;
		}

		// compute the angle between the projected eye vector and the
		// projected z

		double dot = eyeVec.dot(zAxis);
		if (dot > 1.0f) {
		    dot = 1.0f;
		} else if (dot < -1.0f) {
		    dot = -1.0f;
		}

		angle = sign*Math.acos(dot);

		// use -angle because xform is to *undo* rotation by angle
		aa.x = nAxis.x;
		aa.y = nAxis.y;
		aa.z = nAxis.z;
		aa.angle = -angle;
		orientedxform.set(aa);
	    }
	    else {
		orientedxform.setIdentity();
	    }

        } else if(mode == OrientedShape3D.ROTATE_ABOUT_POINT ){ // rotate about point
	    // Need to rotate Z axis to point to eye, and Y axis to be
	    // parallel to view platform Y axis, rotating around rotation pt

	    // get the eye point
	    canvas.getCenterEyeInImagePlate(viewPosition);

	    // derive the yUp point
	    yUpPoint.set(viewPosition);
	    yUpPoint.y += 0.01; // one cm in Physical space

	    // transform the points to the Billboard's space
	    canvas.getImagePlateToVworld(xform); // xform is ImagePlateToVworld
	    xform.transform(viewPosition);
	    xform.transform(yUpPoint);

	    // get billboard's transform
	    xform.set(getCurrentLocalToVworld());
	    xform.invert(); // xform is vWorldToLocal

	    // transfom points to local coord sys
	    xform.transform(viewPosition);
	    xform.transform(yUpPoint);

	    // Make a vector from viewPostion to 0,0,0 in the BB coord sys
	    eyeVec.set(viewPosition);
	    eyeVec.normalize();

	    // create a yUp vector
	    yUp.set(yUpPoint);
	    yUp.sub(viewPosition);
	    yUp.normalize();


	    // find the plane to rotate z
	    zAxis.x = 0.0;
	    zAxis.y = 0.0;
	    zAxis.z = 1.0;

	    // rotation axis is cross product of eyeVec and zAxis
	    vector.cross(eyeVec, zAxis); // vector is cross product

	    // if cross product is non-zero, vector is rotation axis and
	    // rotation angle is acos(eyeVec.dot(zAxis)));
	    double length = vector.length();
	    if (length > 0.0001) {
		double dot = eyeVec.dot(zAxis);
		if (dot > 1.0f) {
		    dot = 1.0f;
		} else if (dot < -1.0f) {
		    dot = -1.0f;
		}
		angle = Math.acos(dot);
		aa.x = vector.x;
		aa.y = vector.y;
		aa.z = vector.z;
		aa.angle = -angle;
		zRotate.set(aa);
	    } else {
		// no rotation needed, set to identity (scale = 1.0)
		zRotate.set(1.0);
	    }

	    // Transform the yAxis by zRotate
	    yAxis.x = 0.0;
	    yAxis.y = 1.0;
	    yAxis.z = 0.0;
	    zRotate.transform(yAxis);

	    // project the yAxis onto the plane perp to the eyeVec
	    status = projectToPlane(yAxis, eyeVec);


	    if (status) {
		// project the yUp onto the plane perp to the eyeVec
		status = projectToPlane(yUp, eyeVec);
	    }

	    if (status) {
		// rotation angle is acos(yUp.dot(yAxis));
		double dot = yUp.dot(yAxis);

		// Fix numerical error, otherwise acos return NULL
		if (dot > 1.0f) {
		    dot = 1.0f;
		} else if (dot < -1.0f) {
		    dot = -1.0f;
		}

		angle = Math.acos(dot);

		// check the sign by looking a the cross product vs the eyeVec
		vector.cross(yUp, yAxis);  // vector is cross product
		if (eyeVec.dot(vector) < 0) {
		    angle *= -1;
		}
		aa.x = eyeVec.x;
		aa.y = eyeVec.y;
		aa.z = eyeVec.z;
		aa.angle = -angle;
		xform.set(aa); // xform is now yRotate

		// rotate around the rotation point
		vector.x = rotationPoint.x;
		vector.y = rotationPoint.y;
		vector.z = rotationPoint.z;     // vector to translate to RP
		orientedxform.set(vector);  // translate to RP
		orientedxform.mul(xform);   // yRotate
		orientedxform.mul(zRotate); // zRotate
		vector.scale(-1.0);   	    // vector to translate back
		xform.set(vector);    	    // xform to translate back
		orientedxform.mul(xform);   // translate back
	    }
	    else {
		orientedxform.setIdentity();
	    }

	}
	//Scale invariant computation
	if(constantScale) {
	    // Back Xform a unit vector to local world coords
	    canvas.getInverseVworldProjection(left_xform, right_xform);

	    // the two endpts of the vector have to be transformed
	    // individually because the Xform is not affine
	    im_vec[0].set(0.0, 0.0, 0.0, 1.0);
	    im_vec[1].set(1.0, 0.0, 0.0, 1.0);
	    left_xform.transform(im_vec[0]);
	    left_xform.transform(im_vec[1]);

	    left_xform.set(getCurrentLocalToVworld());
	    left_xform.invert();
	    left_xform.transform(im_vec[0]);
	    left_xform.transform(im_vec[1]);
	    lvec.set(im_vec[1]);
	    lvec.sub(im_vec[0]);

	    // We simply need the direction of this vector
	    lvec.normalize();
	    im_vec[0].set(0.0, 0.0, 0.0, 1.0);
	    im_vec[1].set(lvec);
	    im_vec[1].w = 1.0;

	    // Forward Xfrom to clip coords
	    left_xform.set(getCurrentLocalToVworld());
	    left_xform.transform(im_vec[0]);
	    left_xform.transform(im_vec[1]);

	    canvas.getVworldProjection(left_xform, right_xform);
	    left_xform.transform(im_vec[0]);
	    left_xform.transform(im_vec[1]);

	    // Perspective division
	    im_vec[0].x /= im_vec[0].w;
	    im_vec[0].y /= im_vec[0].w;
	    im_vec[0].z /= im_vec[0].w;

	    im_vec[1].x /= im_vec[1].w;
	    im_vec[1].y /= im_vec[1].w;
	    im_vec[1].z /= im_vec[1].w;

	    lvec.set(im_vec[1]);
	    lvec.sub(im_vec[0]);

	    // Use the length of this vector to determine the scaling
	    // factor
	    double scale = 1/lvec.length();

	    // Convert to meters
	    scale *= scaleFactor*canvas.getPhysicalWidth()/2;

	    // Scale object so that it appears the same size
	    scaleXform.setScale(scale);
	    orientedxform.mul(scaleXform);
	}

    }


    private boolean projectToPlane(Vector3d projVec, Vector3d planeVec)  {
        double dis = planeVec.dot(projVec);
        projVec.x = projVec.x - planeVec.x*dis;
        projVec.y = projVec.y - planeVec.y*dis;
        projVec.z = projVec.z - planeVec.z*dis;

        double length = projVec.length();
        if (length < EPSILON) { // projVec is parallel to planeVec
	    return false;
        }
        projVec.scale(1 / length);
	return true;
    }

    @Override
    void compile(CompileState compState) {

	super.compile(compState);

        mergeFlag = SceneGraphObjectRetained.DONT_MERGE;

	// don't push the static transform to orientedShape3D
        // because orientedShape3D is rendered using vertex array;
	// it's not worth pushing the transform here

        compState.keepTG = true;
    }

    @Override
    void searchGeometryAtoms(UnorderList list) {
	list.add(getGeomAtom(getMirrorShape(key)));
    }
}
