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

package com.sun.j3d.utils.behaviors.keyboard;

import java.io.*;
import java.util.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.awt.event.*;

/**
 * This is the KeyNavigator class.  It accumulates AWT key events (key
 * press and key release) and computes a new transform based on the
 * accumulated events and elapsed time.
 */
public class KeyNavigator {

    private	Vector3d	navVec;
    private	long		time;

    private	Vector3d	fwdAcc;
    private	Vector3d	bwdAcc;
    private	Vector3d	leftAcc;
    private	Vector3d	rightAcc;
    private	Vector3d	upAcc;
    private	Vector3d	downAcc;

    private	Vector3d	fwdDrag;
    private	Vector3d	bwdDrag;
    private	Vector3d	leftDrag;
    private	Vector3d	rightDrag;
    private	Vector3d	upDrag;
    private	Vector3d	downDrag;

    private	double		fwdVMax;
    private	double		bwdVMax;
    private	double		leftVMax;
    private	double		rightVMax;
    private	double		upVMax;
    private	double		downVMax;

    private	float		leftRotAngle;
    private	float		rightRotAngle;
    private	float		upRotAngle;
    private	float		downRotAngle;

    private	double		mmx;

    private	Vector3d	a	= new Vector3d();
    private	Vector3d	dv	= new Vector3d();
    private	Point3d		dp	= new Point3d();
    private	Quat4d		udQuat	= new Quat4d();
    private	Quat4d		lrQuat	= new Quat4d();
    private	Vector3d	vpPos	= new Vector3d();
    private	double		vpScale;
    private	Quat4d		vpQuat	= new Quat4d();
    private	Matrix4d	vpMatrix = new Matrix4d();
    private	Transform3D	vpTrans	= new Transform3D();
    private	Matrix4d	mat	= new Matrix4d();
    private	Vector3d	nda	= new Vector3d();
    private	Vector3d	temp	= new Vector3d();
    private	Transform3D	nominal = new Transform3D();
    private	TransformGroup	targetTG;

    private	static	final	int	UP_ARROW	= (1<<0);
    private	static	final	int	DOWN_ARROW	= (1<<1);
    private	static	final	int	LEFT_ARROW	= (1<<2);
    private	static	final	int	RIGHT_ARROW	= (1<<3);
    private	static	final	int	PLUS_SIGN	= (1<<4);
    private	static	final	int	MINUS_SIGN	= (1<<5);
    private	static	final	int	PAGE_UP		= (1<<6);
    private	static	final	int	PAGE_DOWN	= (1<<7);
    private	static	final	int	HOME_DIR	= (1<<8);
    private	static	final	int	HOME_NOMINAL	= (1<<9);

    private	static	final	int	SHIFT		= (1<<10);
    private	static	final	int	ALT		= (1<<11);
    private	static	final	int	META		= (1<<12);

    private	static	final	int	KEY_UP		= (1<<13);
    private	static	final	int	KEY_DOWN	= (1<<14);

    private	int	key_state = 0;
    private	int	modifier_key_state = 0;


    /**
     * Constructs a new key navigator object that operates on the specified
     * transform group.  All parameters are set to their default, idle state.
     * @param targetTG the target transform group
     */
    public KeyNavigator(TransformGroup targetTG) {
	this.targetTG = targetTG;
	targetTG.getTransform(nominal);

	mmx = 128.0;
	navVec =    new Vector3d(0.0,0.0,0.0);

	fwdAcc =     new Vector3d( 0.0, 0.0,-mmx);
	bwdAcc =     new Vector3d( 0.0, 0.0, mmx);
	leftAcc =    new Vector3d(-mmx, 0.0, 0.0);
	rightAcc =   new Vector3d( mmx, 0.0, 0.0);
	upAcc =      new Vector3d( 0.0, mmx, 0.0);
	downAcc =    new Vector3d( 0.0,-mmx, 0.0);

	fwdDrag =    new Vector3d( 0.0, 0.0, mmx);
	bwdDrag =    new Vector3d( 0.0, 0.0,-mmx);
	leftDrag =   new Vector3d( mmx, 0.0, 0.0);
	rightDrag =  new Vector3d(-mmx, 0.0, 0.0);
	upDrag =     new Vector3d( 0.0,-mmx, 0.0);
	downDrag =   new Vector3d( 0.0, mmx, 0.0);

	fwdVMax   = -mmx;
	bwdVMax   = mmx;
	leftVMax  = -mmx;
	rightVMax = mmx;
	upVMax    = mmx;
	downVMax  = -mmx;

	leftRotAngle = (float) (-Math.PI*2.0/3.0);
	rightRotAngle = (float) (Math.PI*2.0/3.0);
	upRotAngle = (float) (Math.PI*2.0/3.0);
	downRotAngle = (float) (-Math.PI*2.0/3.0);

	// Create Timer here.
	time = System.currentTimeMillis();

    }


    private long getDeltaTime() {
	long newTime = System.currentTimeMillis();
	long deltaTime = newTime - time;
	time = newTime;
	if (deltaTime > 2000) return 0;
	else return deltaTime;
    }

    /* Generate a quaterion as a rotation of radians av about 0/x 1/y 2/z axis */
    private void genRotQuat(double av, int axis, Quat4d q) {
	double b;

	q.x = q.y = q.z = 0.0;
	q.w = Math.cos(av/2.0);

	b = 1.0 - q.w*q.w;

	if (b > 0.0)
	    b = Math.sqrt(b);
	else
	    return;

	if (av < 0.0)
	    b = -b;
	if (axis == 0)
	    q.x = b;
	else if (axis == 1)
	    q.y = b;
	else
	    q.z = b;

    }

    private void accKeyAdd(Vector3d a, Vector3d da, Vector3d drag, double scaleVel) {

	/* Scaling of acceleration due to modification keys */
	nda.scale(scaleVel, da);
	/* Addition of sufficent acceleration to counteract drag */
	nda.sub(drag);

	/* Summing into overall acceleration */
	a.add(nda);

    }


    /**
     * Computes a new transform for the next frame based on
     * the current transform, accumulated keyboard inputs, and
     * elapsed time.  This new transform is written into the target
     * transform group.
     * This method should be called once per frame.
     */
    public void integrateTransformChanges() {
	double scaleVel, scaleRot, scaleScale, pre;
	double udAng, lrAng, r;

	// Get the current View Platform transform into a transform3D object.
	targetTG.getTransform(vpTrans);
	// Extract the position, quaterion, and scale from the transform3D.
	vpScale = vpTrans.get(vpQuat, vpPos);


	double deltaTime = (double) getDeltaTime();
	deltaTime *= 0.001;

	/* Calculate scale due to modification keys */
	if ((modifier_key_state & SHIFT) != 0 &&
	    (modifier_key_state & META) == 0) {
	    scaleVel = 3.0; scaleRot = 2.0; scaleScale = 4.0;
	}
	else if ((modifier_key_state & SHIFT) == 0 &&
	         (modifier_key_state & META) != 0) {
	    scaleVel = 0.1; scaleRot = 0.1; scaleScale = 0.1;
	}
	else if ((modifier_key_state & SHIFT) != 0 &&
	         (modifier_key_state & META) != 0) {
	    scaleVel = 0.3; scaleRot = 0.5; scaleScale = 0.1;
	}
	else {
	    scaleRot = scaleVel = 1.0; scaleScale = 4.0;
	}

	/*
	 *  Processing of rectiliear motion keys.
	 */

	a.x = a.y = a.z = 0.0;  /* acceleration initially 0 */

	/* Acceleration due to keys being down */
	if ((key_state & UP_ARROW) != 0 && (key_state & DOWN_ARROW) == 0)
	    accKeyAdd(a, fwdAcc, fwdDrag, scaleVel);
	else
	if ((key_state & UP_ARROW) == 0 && (key_state & DOWN_ARROW) != 0)
	    accKeyAdd(a, bwdAcc, bwdDrag, scaleVel);

	if (((modifier_key_state & ALT) != 0) &&
	    (key_state & LEFT_ARROW) != 0 && (key_state & RIGHT_ARROW) == 0) {
	    accKeyAdd(a, leftAcc, leftDrag, scaleVel);
	} else
	if (((modifier_key_state & ALT) != 0) &&
	    (key_state & LEFT_ARROW) == 0 && (key_state & RIGHT_ARROW) != 0)
	    accKeyAdd(a, rightAcc, rightDrag, scaleVel);

	if (((modifier_key_state & ALT) != 0) &&
	    (key_state & PAGE_UP) != 0 && (key_state & PAGE_DOWN) == 0)
	    accKeyAdd(a, upAcc, upDrag, scaleVel);
	else
	if (((modifier_key_state & ALT) != 0) &&
	    (key_state & PAGE_UP) == 0 && (key_state & PAGE_DOWN) != 0)
	    accKeyAdd(a, downAcc, downDrag, scaleVel);
 

	/*
	 *  Drag due to new or existing motion
	 */
	pre = navVec.z + a.z * deltaTime;
	if (pre < 0.0) {
	    if (pre + fwdDrag.z * deltaTime < 0.0)
		a.add(fwdDrag);
	    else
		a.z -= pre/deltaTime;
	} else if (pre > 0.0) {
	    if (pre + bwdDrag.z * deltaTime > 0.0)
		a.add(bwdDrag);
	    else
		a.z -= pre/deltaTime;
	}

	pre = navVec.x + a.x * deltaTime;
	if (pre < 0.0) {
	    if (pre + leftDrag.x * deltaTime < 0.0)
		a.add(leftDrag);
	    else
		a.x -= pre/deltaTime;
	} else if (pre > 0.0) {
	    if (pre + rightDrag.x * deltaTime > 0.0)
		a.add(rightDrag);
	    else
		a.x -= pre/deltaTime;
	}

	pre = navVec.y + a.y * deltaTime;
	if (pre < 0.0) {
	    if (pre + downDrag.y * deltaTime < 0.0)
		a.add(downDrag);
	    else
		a.y -= pre/deltaTime;
	} else if (pre > 0.0) {
	    if (pre + upDrag.y * deltaTime > 0.0)
		a.add(upDrag);
	    else
		a.y -= pre/deltaTime;
	}

	/* Integration of acceleration to velocity */
	dv.scale(deltaTime, a);
	navVec.add(dv);

	/* Speed limits */
	if (navVec.z < scaleVel * fwdVMax) navVec.z = scaleVel * fwdVMax;
	if (navVec.z > scaleVel * bwdVMax) navVec.z = scaleVel * bwdVMax;
	if (navVec.x < scaleVel * leftVMax) navVec.x = scaleVel * leftVMax;
	if (navVec.x > scaleVel * rightVMax) navVec.x = scaleVel* rightVMax;
	if (navVec.y > scaleVel * upVMax) navVec.y = scaleVel * upVMax;
	if (navVec.y < scaleVel * downVMax) navVec.y = scaleVel * downVMax;

	/* Integration of velocity to distance */
	dp.scale(deltaTime, navVec);

	/* Scale our motion to the current avatar scale */
	// 1.0 eventually needs to be a more complex value (see hs).
	//      r = workplace_coexistence_to_vworld_ori.scale/
	//	one_to_one_coexistence_to_vworld_ori.scale;
	r = vpScale/1.0;
	dp.scale(r, dp);

	/*
	 *  Processing of rotation motion keys.
	 */
	udAng = lrAng = 0.0;

	/* Rotation due to keys being down */
	if (((modifier_key_state & ALT) == 0) &&
	    (key_state & LEFT_ARROW) != 0 && (key_state & RIGHT_ARROW) == 0)
	    lrAng = (double) leftRotAngle;
	else if (((modifier_key_state & ALT) == 0) &&
	    (key_state & LEFT_ARROW) == 0 && (key_state & RIGHT_ARROW) != 0)
	    lrAng =  (double) rightRotAngle;

	if (((modifier_key_state & ALT) == 0) &&
	    (key_state & PAGE_UP) != 0 && (key_state & PAGE_DOWN) == 0)
	    udAng = (double) upRotAngle;
	else if (((modifier_key_state & ALT) == 0) &&
	    	 (key_state & PAGE_UP) == 0 && (key_state & PAGE_DOWN) != 0)
	    udAng = (double) downRotAngle;

	lrAng *= scaleRot;
	udAng *= scaleRot;

	/* Scaling of angle change to delta time */
	lrAng *= deltaTime;
	udAng *= deltaTime;


	/* Addition to existing orientation */
	// vr_quat_inverse(&workplace_coexistence_to_vworld_ori.quat, &vpQuat);
	// vpQuat gotten at top of method.
	vpQuat.inverse();

	if(lrAng != 0.0) {
	    genRotQuat(lrAng, 1, lrQuat);
	    vpQuat.mul(lrQuat, vpQuat);
	}

	if(udAng != 0.0) {
	    genRotQuat(udAng, 0, udQuat);
	    vpQuat.mul(udQuat, vpQuat);
	}

	/* Rotation of distance vector */
	vpQuat.inverse();
	vpQuat.normalize();  /* Improvment over HoloSketch */
	mat.set(vpQuat);
	mat.transform(dp);

	/* Processing of scale */
	if ((key_state & PLUS_SIGN) != 0) {
	    vpScale *= (1.0 + (scaleScale*deltaTime));
	    if (vpScale > 10e+14) vpScale = 1.0;
	} else if ((key_state & MINUS_SIGN) != 0) {
	    vpScale /= (1.0 + (scaleScale*deltaTime));
	    if (vpScale < 10e-14) vpScale = 1.0;
	}

	// add dp into current vp position.
	vpPos.add(dp);

	if ((key_state & HOME_NOMINAL) != 0) {
	    resetVelocity();
	    // Extract the position, quaterion, and scale from the nominal
	    // transform
	    vpScale = nominal.get(vpQuat, vpPos);
	}


	/* Final update of view platform */
	// Put the transform back into the transform group.
	vpTrans.set(vpQuat, vpPos, vpScale);
	targetTG.setTransform(vpTrans);
    }


    /**
     * Resets the keyboard navigation velocity to 0.
     */
    private void resetVelocity() {
	navVec.x = navVec.y = navVec.z = 0.0;
    }


    /**
     * Processed a keyboard event.  This routine should be called
     * every time a KEY_PRESSED or KEY_RELEASED event is received.
     * @param keyEvent the AWT key event
     */
    public void processKeyEvent(KeyEvent keyEvent) {
	int keyCode = keyEvent.getKeyCode();
	int keyChar = keyEvent.getKeyChar();

//System.err.println("keyCode " + keyCode + "  keyChar " + keyChar);

	if (keyEvent.getID() == KeyEvent.KEY_RELEASED) {
	    if (keyChar == '+') key_state &= ~PLUS_SIGN;
	    else
	    switch (keyCode) {
	    case KeyEvent.VK_UP:	key_state &= ~UP_ARROW;	   break;
	    case KeyEvent.VK_DOWN:	key_state &= ~DOWN_ARROW;  break;
	    case KeyEvent.VK_LEFT:	key_state &= ~LEFT_ARROW;  break;
	    case KeyEvent.VK_RIGHT:	key_state &= ~RIGHT_ARROW; break;
	    case KeyEvent.VK_PAGE_UP:	key_state &= ~PAGE_UP;	   break;
	    case KeyEvent.VK_PAGE_DOWN: key_state &= ~PAGE_DOWN;   break;
	    case KeyEvent.VK_EQUALS:	key_state &= ~HOME_NOMINAL;break;
	    default: switch(keyChar) {
		case '-': key_state &= ~MINUS_SIGN; break;
		}
	    }
	} else if (keyEvent.getID() == KeyEvent.KEY_PRESSED) {
	    if (keyChar == '+') key_state |=  PLUS_SIGN;
	    switch (keyCode) {
	    case KeyEvent.VK_UP:	key_state |=  UP_ARROW;	   break;
	    case KeyEvent.VK_DOWN:	key_state |=  DOWN_ARROW;  break;
	    case KeyEvent.VK_LEFT:	key_state |=  LEFT_ARROW;  break;
	    case KeyEvent.VK_RIGHT:	key_state |=  RIGHT_ARROW; break;
	    case KeyEvent.VK_PAGE_UP:	key_state |=  PAGE_UP;	   break;
	    case KeyEvent.VK_PAGE_DOWN: key_state |=  PAGE_DOWN;   break;
	    case KeyEvent.VK_EQUALS:	key_state |=  HOME_NOMINAL;break;
	    default: switch(keyChar) {
		case '-': key_state |=  MINUS_SIGN; break;
		}
	    }
	}

	/* Check modifier keys */
	if (keyEvent.isShiftDown())
	    modifier_key_state |=  SHIFT;
	else
	    modifier_key_state &= ~SHIFT;

	if (keyEvent.isMetaDown())
	    modifier_key_state |=  META;
	else
	    modifier_key_state &= ~META;
	
	if (keyEvent.isAltDown())
	    modifier_key_state |=  ALT;
	else
	    modifier_key_state &= ~ALT;

//System.err.println("keyCode " + keyEvent.getKeyCode() + "  modifiers " + keyEvent.getModifiers());
//System.err.println("SHIFT_MASK " + keyEvent.SHIFT_MASK);
//System.err.println("CTRL_MASK " + keyEvent.CTRL_MASK);
//System.err.println("META_MASK " + keyEvent.META_MASK);
//System.err.println("ALT_MASK " + keyEvent.ALT_MASK);

    }
}
