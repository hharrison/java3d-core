/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 */

package com.sun.j3d.utils.pickfast;

import java.awt.event.*;
import javax.vecmath.*;
import javax.media.j3d.*;
import com.sun.j3d.utils.geometry.*; // Cone, Cylinder

/**
 * A subclass of PickTool, simplifies picking using mouse events from a canvas. 
 * This class allows picking using canvas x,y locations by generating the 
 * appropriate pick shape. 
 * <p>
 * The pick tolerance specifies the distance from the
 * pick center to include in the pick shape.  A tolerance of 0.0 may speedup 
 * picking slightly, but also make it very difficult to pick points and lines.
 * <p> 
 * The pick canvas can be used to make a series of picks.  For example, to 
 * initialize the pick canvas:
 * <blockquote><pre>
 *     PickCanvas pickCanvas = new PickCanvas(canvas, scene);
 *     pickCanvas.setMode(PickInfo.PICK_GEOMETRY); 
 *     pickCanvas.setFlags(PickInfo.NODE | PickInfo.CLOSEST_INTERSECTION_POINT); 
 *     pickCanvas.setTolerance(4.0f);
 * </pre></blockquote>
 * <p>
 * Then for each mouse event:
 * <blockquote><pre>
 *     pickCanvas.setShapeLocation(mouseEvent);
 *     PickInfo[] pickInfos = pickCanvas.pickAll();
 * </pre></blockquote>
 * <p>
 * NOTE: For the pickAllSorted or pickClosest methods, the picks will be sorted
 * by the distance from the ViewPlatform to the intersection point.
 * @see PickTool
 */
public class PickCanvas extends PickTool {

    /* OPEN ISSUES:
       -- Should restrict the pick shape to the front/back clip plane
     */


    /** The canvas we are picking into */
    Canvas3D canvas;

    /* the pick tolerance, default to 2.0 */
    float tolerance = 2.0f;
    int save_xpos;
    int save_ypos;
    
    /** Constructor with Canvas3D for mouse events and BranchGroup to be picked.
     */
    public PickCanvas (Canvas3D c, BranchGroup b) {
	super (b);
	canvas = c;
    }

    /** Constructor with Canvas3D for mouse events and Locale to be picked.
     */
    public PickCanvas (Canvas3D c, Locale l) {
	super (l);
	canvas = c;
    }

    /** Inquire the canvas to be used for picking operations.
	@return the canvas.
     */
    public Canvas3D getCanvas() {
	return canvas;
    }

    /** Set the picking tolerance.  Objects within this distance
     * (in pixels) 
     * to the mouse x,y location will be picked.  The default tolerance is 2.0.
     * @param t The tolerance
     * @exception IllegalArgumentException if the tolerance is less than 0.
     */
    public void setTolerance(float t) {
	if (t < 0.0f) {
	    throw new IllegalArgumentException();
	}
	tolerance = t;

	if ((pickShape != null) && (!userDefineShape)) {
	    // reset pickShape
	    pickShape = null;
	    setShapeLocation(save_xpos, save_ypos);
	} 
    }

    /** Get the pick tolerance. 
     */
    public float getTolerance() {
	return tolerance;
    }

    /** Set the pick location. Defines the location on the canvas where the
       pick is to be performed.
      @param mevent The MouseEvent for the picking point
    */
    public void setShapeLocation(MouseEvent mevent) {
	setShapeLocation(mevent.getX(), mevent.getY());
    }
    /** Set the pick location. Defines the location on the canvas where the
        pick is to be performed (upper left corner of canvas is 0,0).
	@param xpos the X position of the picking point
	@param ypos the Y position of the picking point
    */
    public void setShapeLocation (int xpos, int ypos) {
	Transform3D motion = new Transform3D();
	Point3d eyePosn = new Point3d();
	Point3d mousePosn = new Point3d();
	Vector3d mouseVec = new Vector3d();
	boolean isParallel = false;
	double radius = 0.0;
	double spreadAngle = 0.0;
	
	this.save_xpos = xpos;
	this.save_ypos = ypos;
	canvas.getCenterEyeInImagePlate(eyePosn);
	canvas.getPixelLocationInImagePlate(xpos,ypos,mousePosn);

	if ((canvas.getView() != null) &&
	    (canvas.getView().getProjectionPolicy() ==
				    View.PARALLEL_PROJECTION)) {
	    // Correct for the parallel projection: keep the eye's z
	    // coordinate, but make x,y be the same as the mouse, this
	    // simulates the eye being at "infinity"
	    eyePosn.x = mousePosn.x;
	    eyePosn.y = mousePosn.y;
	    isParallel = true;
	}

	// Calculate radius for PickCylinderRay and spread angle for PickConeRay
	Vector3d eyeToCanvas = new Vector3d();
	eyeToCanvas.sub (mousePosn, eyePosn);
	double distanceEyeToCanvas = eyeToCanvas.length();

	Point3d deltaImgPlate = new Point3d();
	canvas.getPixelLocationInImagePlate (xpos+1, ypos, deltaImgPlate);

	Vector3d ptToDelta = new Vector3d();
	ptToDelta.sub (mousePosn, deltaImgPlate);
	double distancePtToDelta = ptToDelta.length();
	distancePtToDelta *= tolerance;

	canvas.getImagePlateToVworld(motion);

	/*
	System.out.println("mouse position " + xpos + " " + ypos);
	System.out.println("before, mouse " + mousePosn + " eye " + eyePosn);
	*/

	motion.transform(eyePosn);
	start = new Point3d (eyePosn); // store the eye position
	motion.transform(mousePosn);
	mouseVec.sub(mousePosn, eyePosn);
	mouseVec.normalize();

	/*
	System.out.println(motion + "\n");
	System.out.println("after, mouse " + mousePosn + " eye " + eyePosn + 
		 " mouseVec " + mouseVec);
		 */

	if (tolerance == 0.0) {
	    if ((pickShape != null) && (pickShape instanceof PickRay)) {
		((PickRay)pickShape).set (eyePosn, mouseVec);
	    } else {
		pickShape = (PickShape) new PickRay (eyePosn, mouseVec);
	    }
	    //      pickShape = (PickShape) new PickConeRay (eyePosn,
	    //		mouseVec,1.0*Math.PI/180.0);
	} else {
	    if (isParallel) {
		// Parallel projection, use a PickCylinderRay
	        distancePtToDelta *= motion.getScale();
		if ((pickShape != null) && 
				(pickShape instanceof PickCylinderRay)) {
		    ((PickCylinderRay)pickShape).set (eyePosn, mouseVec, 
						distancePtToDelta);
		} else {
		    pickShape = (PickShape) new PickCylinderRay (eyePosn, 
						mouseVec, distancePtToDelta);
		}
	    } else {
		// Perspective projection, use a PickConeRay

		// Calculate spread angle
		spreadAngle = Math.atan (distancePtToDelta/distanceEyeToCanvas);

		if ((pickShape != null) && 
				(pickShape instanceof PickConeRay)) {
		    ((PickConeRay)pickShape).set (eyePosn, mouseVec, 
							spreadAngle);
		} else {
		    pickShape = (PickShape) new PickConeRay (eyePosn, mouseVec, 
						       spreadAngle); 
		}
	    }
	}
    }
} // PickCanvas


