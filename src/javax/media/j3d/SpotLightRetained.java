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

import javax.vecmath.*;

/**
 * A local spot light source object.
 */

class SpotLightRetained extends PointLightRetained {
    static final int DIRECTION_CHANGED      = LAST_POINTLIGHT_DEFINED_BIT << 1;
    static final int ANGLE_CHANGED          = LAST_POINTLIGHT_DEFINED_BIT << 2;
    static final int CONCENTRATION_CHANGED  = LAST_POINTLIGHT_DEFINED_BIT << 3;

    /**
     * The spot light's direction.
     */
    Vector3f	direction = new Vector3f(0.0f, 0.0f, -1.0f);

    // The transformed direction of this light
    Vector3f xformDirection = new Vector3f(0.0f, 0.0f, -1.0f);

    /**
     * The spot light's spread angle.
     */
    float	spreadAngle = (float)Math.PI;

    /**
     * The spot light's concentration.
     */
    float	concentration = 0.0f;


    SpotLightRetained() {
        this.nodeType = NodeRetained.SPOTLIGHT;
	lightType = 4;
    }

    /**
     * Initializes the spot light's spread angle.
     * @param spreadAngle the light's spread angle
     */
    void initSpreadAngle(float spreadAngle) {
	if (spreadAngle < 0.0) {
	    this.spreadAngle = 0.0f;
	}
	else if (spreadAngle > (float) Math.PI * 0.5f) {
	    this.spreadAngle = (float)Math.PI;
	}
	else {
	    this.spreadAngle = spreadAngle;
	}
    }


    void setLive(SetLiveState s) {
	super.doSetLive(s);
	J3dMessage createMessage = super.initMessage(12);
	Object[] objs = (Object[])createMessage.args[4];
	objs[9] = new Float(spreadAngle);
	objs[10] = new Float(concentration) ;
	objs[11] = new Vector3f(direction);
	VirtualUniverse.mc.processMessage(createMessage);
    }

    /**
     * Sets the spot light's spread angle.
     * @param spreadAngle the light's spread angle
     */
    void setSpreadAngle(float spreadAngle) {
	 initSpreadAngle(spreadAngle);
	 sendMessage(ANGLE_CHANGED, new Float(this.spreadAngle));
     }

    /**
     * Returns the spot light's spread angle.
     * @return the spread angle of the light
     */
     float getSpreadAngle() {
	return this.spreadAngle;
     }

    /**
     * Initializes the spot light's concentration.
     * @param concentration the concentration of the light
     */
    void initConcentration(float concentration) {
	this.concentration = concentration;
     }

    /**
     * Sets the spot light's concentration.
     * @param concentration the concentration of the light
     */
    void setConcentration(float concentration) {
	 initConcentration(concentration);
	 sendMessage(CONCENTRATION_CHANGED, new Float(concentration));
     }

    /**
     * Retrieves the spot light's concentration.
     * @return the concentration of the light
     */
     float getConcentration() {
	return this.concentration;
     }

    /**
     * Initializes the spot light's direction from the vector provided.
     * @param direction the new direction of the light
     */
    void initDirection(Vector3f direction) {
	this.direction.set(direction);

        if (staticTransform != null) {
            staticTransform.transform.transform(this.direction, this.direction);
        }
     }

    /**
     * Sets the spot light's direction from the vector provided.
     * @param direction the new direction of the light
     */
    void setDirection(Vector3f direction) {
	initDirection(direction);
	sendMessage(DIRECTION_CHANGED, new Vector3f(direction));
     }


    /**
     * Initializes this light's direction from the three values provided.
     * @param x the new x direction
     * @param y the new y direction
     * @param z the new z direction
     */
    void initDirection(float x, float y, float z) {
	this.direction.x = x;
	this.direction.y = y;
	this.direction.z = z;
        if (staticTransform != null) {
            staticTransform.transform.transform(this.direction, this.direction);
        }
     }
    
    /**
     * Sets this light's direction from the three values provided.
     * @param x the new x direction
     * @param y the new y direction
     * @param z the new z direction
     */
    void setDirection(float x, float y, float z) {
	setDirection(new Vector3f(x, y, z));
    }


    /**
     * Retrieves this light's direction and places it in the
     * vector provided.
     * @param direction the variable to receive the direction vector
     */
     void getDirection(Vector3f direction) {
	 direction.set(this.direction);
         if (staticTransform != null) {
            Transform3D invTransform = staticTransform.getInvTransform();
            invTransform.transform(direction, direction);
         }
     }

    /** 
     * This update function, and its native counterpart,
     * updates a spot light.  This includes its color, attenuation,
     * transformed position, spread angle, concentration,
     * and its transformed position.
     */
    void update(Context ctx, int lightSlot, double scale) {
	validateAttenuationInEc(scale);
        Pipeline.getPipeline().updateSpotLight(ctx,
                lightSlot, color.x, color.y, color.z,
                attenuation.x, linearAttenuationInEc,
                quadraticAttenuationInEc,
                xformPosition.x, xformPosition.y,
                xformPosition.z, spreadAngle, concentration,
                xformDirection.x, xformDirection.y,
                xformDirection.z);
    }


    /** 
     * This update function, and its native counterpart,
     * updates a directional light.  This includes its
     * color and its transformed direction.
     */
    // Note : if you add any more fields here , you need to update
    // updateLight() in RenderingEnvironmentStructure
    void updateMirrorObject(Object[] objs) {

	int component = ((Integer)objs[1]).intValue();
	Transform3D trans;
	int i;
	int numLgts = ((Integer)objs[2]).intValue();
	LightRetained[] mLgts = (LightRetained[]) objs[3];
	if ((component & DIRECTION_CHANGED) != 0) {
	    for (i = 0; i < numLgts; i++) {
		if (mLgts[i].nodeType == NodeRetained.SPOTLIGHT) {
		    SpotLightRetained ml = (SpotLightRetained)mLgts[i];
		    ml.direction = (Vector3f)objs[4];
		    ml.getLastLocalToVworld().transform(ml.direction, 
						      ml.xformDirection);
		    ml.xformDirection.normalize();
		}
	    }
	}
	else if ((component & ANGLE_CHANGED) != 0) {
	    for (i = 0; i < numLgts; i++) {
		if (mLgts[i].nodeType == NodeRetained.SPOTLIGHT) {
		    SpotLightRetained ml = (SpotLightRetained)mLgts[i];
		    ml.spreadAngle = ((Float)objs[4]).floatValue();
		}
	    }
	    
	}
	else if ((component & CONCENTRATION_CHANGED) != 0) {
	    
	    for (i = 0; i < numLgts; i++) {
		if (mLgts[i].nodeType == NodeRetained.SPOTLIGHT) {
		    SpotLightRetained ml = (SpotLightRetained)mLgts[i];
		    ml.concentration = ((Float)objs[4]).floatValue();
		}
	    }
	}
	else if ((component & INIT_MIRROR) != 0) {
	    for (i = 0; i < numLgts; i++) {
		if (mLgts[i].nodeType == NodeRetained.SPOTLIGHT) {
		    SpotLightRetained ml = (SpotLightRetained)mLgts[i];
		    ml.spreadAngle = ((Float)((Object[])objs[4])[9]).floatValue();
		    ml.concentration = ((Float)((Object[])objs[4])[10]).floatValue();
		    ml.direction = (Vector3f)((Object[])objs[4])[11];
		    ml.getLastLocalToVworld().transform(ml.direction, 
							ml.xformDirection);
		    ml.xformDirection.normalize();
		}
	    }
	}

	// call the parent's mirror object update routine
	super.updateMirrorObject(objs);
    }


    // Clones only the retained side, internal use only
     protected Object clone() {
         SpotLightRetained sr = (SpotLightRetained)super.clone();
         sr.direction = new Vector3f(direction);
         sr.xformDirection = new Vector3f();
         return sr;
     }   



    // Called on the mirror object
    void updateTransformChange() {
	super.updateTransformChange();

	getLastLocalToVworld().transform(direction, xformDirection);
	xformDirection.normalize();

    }

    final void sendMessage(int attrMask, Object attr) {
	J3dMessage createMessage = new J3dMessage();
	createMessage.threads = targetThreads;
	createMessage.universe = universe;
	createMessage.type = J3dMessage.LIGHT_CHANGED;
	createMessage.args[0] = this;
	createMessage.args[1]= new Integer(attrMask);
	 if (inSharedGroup)
	     createMessage.args[2] = new Integer(numMirrorLights);
	 else
	     createMessage.args[2] = new Integer(1);
	createMessage.args[3] = mirrorLights.clone();
	createMessage.args[4] = attr;
	VirtualUniverse.mc.processMessage(createMessage);
    }


    void mergeTransform(TransformGroupRetained xform) {
	super.mergeTransform(xform);
        xform.transform.transform(direction, direction);
    }
}
