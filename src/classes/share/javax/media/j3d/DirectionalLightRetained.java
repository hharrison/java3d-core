/*
 * $RCSfile$
 *
 * Copyright (c) 2006 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import javax.vecmath.*;

/**
 * An infinite directional light source object.
 */

class DirectionalLightRetained extends LightRetained
{
    static final int DIRECTION_CHANGED        = LAST_DEFINED_BIT << 1;

    // The direction in which this light source is pointing.
    Vector3f direction = new Vector3f(0.0f, 0.0f, -1.0f);

    // The transformed direction
    Vector3f xformDirection = new Vector3f(0.0f, 0.0f, -1.0f);

    DirectionalLightRetained() {
        this.nodeType = NodeRetained.DIRECTIONALLIGHT;
	lightType = 2;
	localBounds = new BoundingBox();
	((BoundingBox)localBounds).setLower( 1.0, 1.0, 1.0);
	((BoundingBox)localBounds).setUpper(-1.0,-1.0,-1.0);
    }

    /**
     * Initializes this light's direction from the vector provided.
     * @param direction the new direction
     */
     void initDirection(Vector3f direction) {
	this.direction.set(direction);
	if (staticTransform != null) {
	    staticTransform.transform.transform(
			this.direction, this.direction);
	}
    }

    /**
     * Sets this light's direction from the vector provided.
     * and sends a message
     * @param direction the new direction
     */
     void setDirection(Vector3f direction) {
	 initDirection(direction);
	 J3dMessage createMessage = new J3dMessage();
	 createMessage.threads = targetThreads;
	 createMessage.type = J3dMessage.LIGHT_CHANGED;
	 createMessage.universe = universe;
	 createMessage.args[0] = this;
	 createMessage.args[1]= new Integer(DIRECTION_CHANGED);
	 if (inSharedGroup)
	     createMessage.args[2] = new Integer(numMirrorLights);
	 else
	     createMessage.args[2] = new Integer(1);
	 createMessage.args[3] = mirrorLights.clone();
	 createMessage.args[4] = new Vector3f(direction);
	 VirtualUniverse.mc.processMessage(createMessage);

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
	    staticTransform.transform.transform(
			this.direction, this.direction);
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



    void setLive(SetLiveState s) {
	super.setLive(s);
	J3dMessage createMessage = super.initMessage(8);
	Object[] objs = (Object[])createMessage.args[4];
	objs[7] = new Vector3f(direction);
	VirtualUniverse.mc.processMessage(createMessage);

    }
    
    /** 
     * This update function, and its native counterpart,
     * updates a directional light.  This includes its
     * color and its transformed direction.
     */
    // Note : if you add any more fields here , you need to update
    // updateLight() in RenderingEnvironmentStructure
    void updateMirrorObject(Object[] objs) {
	int i;
	int component = ((Integer)objs[1]).intValue();
	Transform3D trans;
	int numLgts =  ((Integer)objs[2]).intValue();
	  
	LightRetained[] mLgts = (LightRetained[]) objs[3];	
	DirectionalLightRetained ml;
	if ((component & DIRECTION_CHANGED) != 0) {
	    
	    for (i = 0; i < numLgts; i++) {
		if (mLgts[i].nodeType == NodeRetained.DIRECTIONALLIGHT) {
		    ml = (DirectionalLightRetained) mLgts[i];
		    ml.direction = (Vector3f)objs[4];
		    ml.getLastLocalToVworld().transform(ml.direction, 
						      ml.xformDirection);
		    ml.xformDirection.normalize();
		}
	    }
	}

	if ((component & INIT_MIRROR) != 0) {
	    for (i = 0; i < numLgts; i++) {
		if (mLgts[i].nodeType == NodeRetained.DIRECTIONALLIGHT) {
		    ml = (DirectionalLightRetained) mLgts[i];
		    ml.direction = (Vector3f)((Object[])objs[4])[7];
		    ml.getLastLocalToVworld().transform(ml.direction, 
						      ml.xformDirection);
		    ml.xformDirection.normalize();
		}
	    }
	}
	// call the parent's mirror object update routine
	super.updateMirrorObject(objs);
    }

      
    void update(Context ctx, int lightSlot, double scale) { 
        Pipeline.getPipeline().updateDirectionalLight(ctx,
                lightSlot, color.x, color.y, color.z,
                xformDirection.x, xformDirection.y,
                xformDirection.z);
    }

    // Clones only the retained side, internal use only
     protected Object clone() {
         DirectionalLightRetained dr =
            (DirectionalLightRetained)super.clone();
         dr.direction = new Vector3f(direction);
         dr.xformDirection = new Vector3f(0.0f, 0.0f, -1.0f);
         return dr;
     }   

    
    // Called on the mirror object
    void updateTransformChange() {
	super.updateTransformChange();

	getLastLocalToVworld().transform(direction, xformDirection);
	xformDirection.normalize();

    }

    void mergeTransform(TransformGroupRetained xform) {
	super.mergeTransform(xform);
        xform.transform.transform(direction, direction);
    }
}
