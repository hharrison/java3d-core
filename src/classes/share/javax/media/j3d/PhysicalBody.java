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

import javax.vecmath.*;
import java.util.ArrayList;

/**
 * This object contains a specification of the user's head.
 * Attributes of this object are defined in the head coordinate system.
 * The orgin is defined to be halfway between the left and right eye
 * in the plane of the face.
 * The x-axis extends to the right (of the head looking out from the head).
 * The y-axis extends up. The z-axis extends to the rear of the head.
 *
 * @see View
 */

public class PhysicalBody extends Object {
    // The X offset for each eye is 1/2 of the inter-pupilary distance
    // This constant specifies the default IPD.
    private static final double HALF_IPD = 0.033;

    // These offsets specify the default ear positions relative to the
    // "center eye".
    private static final double EAR_X =  0.080;
    private static final double EAR_Y = -0.030;
    private static final double EAR_Z =  0.095;

    /**
     * The user's left eye's position in head coordinates.
     */
    Point3d	leftEyePosition = new Point3d(-HALF_IPD, 0.0, 0.0);

    /**
     * The user's right eye's position in head coordinates.
     */
    Point3d	rightEyePosition = new Point3d(HALF_IPD, 0.0, 0.0);

    /**
     * The user's left ear's position in head coordinates.
     */
    Point3d	leftEarPosition = new Point3d(-EAR_X, EAR_Y, EAR_Z);

    /**
     * The user's right ear's position in head coordinates.
     */
    Point3d	rightEarPosition = new Point3d(EAR_X, EAR_Y, EAR_Z);

    /**
     * The user's nominal eye height as measured
     * from the ground plane.
     */
    double	nominalEyeHeightFromGround = 1.68;

    /**
     * The amount to offset the system's
     * viewpoint from the user's current eye-point.  This offset
     * distance allows an "Over the shoulder" view of the scene
     * as seen by the user.
     *
     * By default, we will use a Z value of 0.4572 meters (18 inches).
     */
    double nominalEyeOffsetFromNominalScreen = 0.4572;

    // Head to head-tracker coordinate system transform.
    // If head tracking is enabled, this transform is a calibration
    // constant.  If head tracking is not enabled, this transform is
    // not used.
    // This is used in both SCREEN_VIEW and HMD_VIEW modes.
    Transform3D headToHeadTracker = new Transform3D();

    // A list of View Objects that refer to this
    ArrayList users = new ArrayList();

    // Mask that indicates this PhysicalBody's view dependence info. has changed,
    // and CanvasViewCache may need to recompute the final view matries.
    int pbDirtyMask = (View.PB_EYE_POSITION_DIRTY
		       | View.PB_EAR_POSITION_DIRTY
		       | View.PB_NOMINAL_EYE_HEIGHT_FROM_GROUND_DIRTY
		       | View.PB_NOMINAL_EYE_OFFSET_FROM_NOMINAL_SCREEN_DIRTY);
    
    /**
     * Constructs a PhysicalBody object with default parameters.
     * The default values are as follows:
     * <ul>
     * left eye position : (-0.033, 0.0, 0.0)<br>
     * right eye position : (0.033, 0.0, 0.0)<br>
     * left ear position : (-0.080, -0.030, 0.095)<br>
     * right ear position : (0.080, -0.030, 0.095)<br>
     * nominal eye height from ground : 1.68<br>
     * nominal eye offset from nominal screen : 0.4572<br>
     * head to head tracker transform : identity<br>
     * </ul>
     */
    public PhysicalBody() {
	// Just use the defaults
	initHeadToHeadTracker();
    }

    // Add a user to the list of users
    synchronized void removeUser(View view) {
	int idx = users.indexOf(view);
	if (idx >= 0) {
	    users.remove(idx);
	}
    }

    // Add a user to the list of users
    synchronized void addUser(View view) {
	int idx = users.indexOf(view);
	if (idx < 0) {
	    users.add(view);
	}
    }

    // Add a user to the list of users
    synchronized void notifyUsers() {
	for (int i=users.size()-1; i>=0; i--) {
	    View view = (View)users.get(i);
            // TODO: notifyUsers should have a parameter denoting field changed
            if (view.soundScheduler != null) {
                view.soundScheduler.setListenerFlag(
                        SoundScheduler.EAR_POSITIONS_CHANGED |
                        SoundScheduler.EYE_POSITIONS_CHANGED   );
	    }
	    view.repaint();
	}
    }

    /**
     * Constructs and initializes a PhysicalBody object from the
     * specified parameters.
     * @param leftEyePosition the user's left eye position
     * @param rightEyePosition the user's right eye position
     */
    public PhysicalBody(Point3d leftEyePosition, Point3d rightEyePosition) {
	this.leftEyePosition.set(leftEyePosition);
	this.rightEyePosition.set(rightEyePosition);
	initHeadToHeadTracker();
    }

    /**
     * Constructs and initializes a PhysicalBody object from the
     * specified parameters.
     * @param leftEyePosition the user's left eye position
     * @param rightEyePosition the user's right eye position
     * @param leftEarPosition the user's left ear position
     * @param rightEarPosition the user's right ear position
     */
    public PhysicalBody(Point3d leftEyePosition,
			Point3d rightEyePosition,
			Point3d leftEarPosition,
			Point3d rightEarPosition) {

	this.leftEyePosition.set(leftEyePosition);
	this.rightEyePosition.set(rightEyePosition);
	this.leftEarPosition.set(leftEarPosition);
	this.rightEarPosition.set(rightEarPosition);
	initHeadToHeadTracker();
    }

    /**
     * Returns a string representation of this PhysicalBody's values.
     */

    public String toString() {
	return "eyePosition = (" + this.leftEyePosition + ", " +
		this.rightEyePosition + ")\n" +
		"earPosition = (" + this.leftEarPosition + ", " +
		this.rightEarPosition + ")";
    }

    /**
     * Retrieves the user head object's left eye position and places
     * that value in the specified object.
     * @param position the object that will receive the left-eye's position 
     * in head coordinates
     */
    public void getLeftEyePosition(Point3d position) {
	position.set(this.leftEyePosition);
    }

    /**
     * Sets the user head object's left eye position.
     * @param position the left-eye's position in head coordinates
     */
    public void setLeftEyePosition(Point3d position) {
	synchronized(this) {
	    this.leftEyePosition.set(position);
	    pbDirtyMask |= View.PB_EYE_POSITION_DIRTY;
	}
	notifyUsers();
    }

    /**
     * Retrieves the user head object's right eye position and places 
     * that value in the specified object.
     * @param position the object that will receive the right-eye's position 
     * in head coordinates
     */
    public void getRightEyePosition(Point3d position) {
	position.set(this.rightEyePosition);
    }

    /**
     * Sets the user head object's right eye position.
     * @param position the right-eye's position in head coordinates
     */
    public void setRightEyePosition(Point3d position) {
	synchronized(this) {
	    this.rightEyePosition.set(position);
	    pbDirtyMask |= View.PB_EYE_POSITION_DIRTY;
	}
	notifyUsers();
    }

    /**
     * Retrieves the user head object's left ear position and places
     * that value in the specified object.
     * @param position the object that will receive the left-ear's position 
     * in head coordinates
     */
    public void getLeftEarPosition(Point3d position) {
	position.set(this.leftEarPosition);
    }

    /**
     * Sets the user head object's left ear position.
     * @param position the left-ear's position in head coordinates
     */
    public void setLeftEarPosition(Point3d position) {
	synchronized(this) {
	    this.leftEarPosition.set(position);
	    pbDirtyMask |= View.PB_EAR_POSITION_DIRTY;
	}
	notifyUsers();
    }

    /**
     * Retrieves the user head object's right ear position and places 
     * that value in the specified object.
     * @param position the object that will receive the right-ear's position 
     * in head coordinates
     */
    public void getRightEarPosition(Point3d position) {
	position.set(this.rightEarPosition);
    }

    /**
     * Sets the user head object's right ear position.
     * @param position the right-ear's position in head coordinates
     */
    public void setRightEarPosition(Point3d position) {
	synchronized(this) {
	    this.rightEarPosition.set(position);
	    pbDirtyMask |= View.PB_EAR_POSITION_DIRTY;
	}
	notifyUsers();
    }

    /**
     * Sets the nominal eye height from the ground plane.
     * This parameter defines
     * the distance from the origin of the user's head (the eyepoint) to
     * the ground.
     * It is used when the view attach policy is NOMINAL_FEET.
     * @param height the nominal height of the eye above the ground plane
     */
    public void setNominalEyeHeightFromGround(double height) {
	synchronized(this) {
	    nominalEyeHeightFromGround = height;
	    pbDirtyMask |= View.PB_NOMINAL_EYE_HEIGHT_FROM_GROUND_DIRTY;
	}
	notifyUsers();
    }

    /**
     * Retrieves the nominal eye height from the ground plane.
     * @return the current nominal eye height above the ground plane
     */
    public double getNominalEyeHeightFromGround() {
	return nominalEyeHeightFromGround;
    }

    /**
     * Sets the nominal eye offset from the display screen.
     * This parameter defines
     * the distance from the origin of the user's head (the eyepoint), in it's
     * nominal position, to
     * the screen.
     * It is used when the view attach policy is NOMINAL_HEAD or NOMINAL_FEET.
     * This value is overridden to be the actual eyepoint when the window
     * eyepoint policy is RELATIVE_TO_FIELD_OF_VIEW.
     * @param offset the nominal offset from the eye to the screen
     */
    public void setNominalEyeOffsetFromNominalScreen(double offset) {
	synchronized(this) {
	    nominalEyeOffsetFromNominalScreen = offset;
	    pbDirtyMask |= View.PB_NOMINAL_EYE_OFFSET_FROM_NOMINAL_SCREEN_DIRTY;
	}
	notifyUsers();
    }

    /**
     * Retrieves the nominal eye offset from the display screen.
     * @return the current nominal offset from the eye to the display screen
     */
    public double getNominalEyeOffsetFromNominalScreen() {
	return nominalEyeOffsetFromNominalScreen;
    }

    /**
     * Sets the head to head-tracker coordinate system transform.
     * If head tracking is enabled, this transform is a calibration
     * constant.  If head tracking is not enabled, this transform is
     * not used.
     * This is used in both SCREEN_VIEW and HMD_VIEW modes.
     * @param t the new transform
     * @exception BadTransformException if the transform is not rigid
     */
    public void setHeadToHeadTracker(Transform3D t) {
	if (!t.isRigid()) {
	    throw new BadTransformException(J3dI18N.getString("PhysicalBody0"));
	}
	headToHeadTracker.setWithLock(t);
	notifyUsers();
    }

    /**
     * Retrieves the head to head-tracker coordinate system transform.
     * @param t the object that will receive the transform
     */
    public void getHeadToHeadTracker(Transform3D t) {
	t.set(headToHeadTracker);
    }

    // Initialize the head to head-tracker transform
    private void initHeadToHeadTracker() {
	// By default the center of the crystal eyes tracker is 20mm down
	// and 35 mm closer to the screen from the origin of head coordinates
	// (the center eye).
	Vector3d v = new Vector3d(0.0, 0.020, 0.035);
	headToHeadTracker.set(v);
    }
}
