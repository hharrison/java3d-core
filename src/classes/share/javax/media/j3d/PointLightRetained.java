/*
 * $RCSfile$
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All rights reserved.
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
 * A Retained PointLight source.
 */
class PointLightRetained extends LightRetained {
    static final int POSITION_CHANGED               = LAST_DEFINED_BIT << 1;
    static final int ATTENUATION_CHANGED            = LAST_DEFINED_BIT << 2;
    static final int LAST_POINTLIGHT_DEFINED_BIT    = ATTENUATION_CHANGED;

    /**
     * The attenuation vector consisting of 
     * constant, linear, and quadratic coefficients.
     */
    Point3f attenuation = new Point3f(1.0f, 0.0f, 0.0f); 

    // The position at which this light source exists.
    Point3f position = new Point3f();

    // The transformed position of this light
    Point3f xformPosition = new Point3f();

    // local to vworld scale for attenuation
    double localToVworldScale;

    // scaled linearAttenuation from lc to ec
    float linearAttenuationInEc;

    // scaled quadraticAttenuation from lc to ec
    float quadraticAttenuationInEc;

    PointLightRetained() {
        this.nodeType = NodeRetained.POINTLIGHT;
	lightType = 3;
	localBounds = new BoundingBox();
	((BoundingBox)localBounds).setLower( 1.0, 1.0, 1.0);
	((BoundingBox)localBounds).setUpper(-1.0,-1.0,-1.0);
    }

    /**
     * Initializes this light's position from the vector provided.
     * @param position the new position
     */
    void initPosition(Point3f position) {
	this.position.set(position);

        if (staticTransform != null) {
            staticTransform.transform.transform(this.position, this.position);
        }
    }

    /**
     * Sets this light's position from the vector provided.
     * @param position the new position
     */
    void setPosition(Point3f position) {
	 initPosition(position);
	 sendMessage(POSITION_CHANGED, new Point3f(position));
    }


    /**
     * Initializes this light's position from the three values provided.
     * @param x the new x position
     * @param y the new y position
     * @param z the new z position
     */
    void initPosition(float x, float y, float z) {
	this.position.x = x;
	this.position.y = y;
	this.position.z = z;

        if (staticTransform != null) {
            staticTransform.transform.transform(this.position, this.position);
        }
    }


    /**
     * Sets this light's position from the three values provided.
     * @param x the new x position
     * @param y the new y position
     * @param z the new z position
     */
    void setPosition(float x, float y, float z) {
	 setPosition(new Point3f(x, y, z));
     }


    /**
     * Retrieves this light's position and places it in the
     * vector provided.
     * @param position the variable to receive the position vector
     */
     void getPosition(Point3f position) {
        position.set(this.position);

        if (staticTransform != null) {
            Transform3D invTransform = staticTransform.getInvTransform();
            invTransform.transform(position, position);
        }
    }


    /**
     * Initializes the point light's attenuation constants.
     * @param attenuation a vector consisting of constant, linear, and quadratic coefficients
     */
    void initAttenuation(Point3f attenuation) {
	 this.attenuation.set(attenuation);
     }

    /**
     * Sets the point light's attenuation constants.
     * @param attenuation a vector consisting of constant, linear, and quadratic coefficients
     */
    void setAttenuation(Point3f attenuation) {
	 initAttenuation(attenuation);
	 sendMessage(ATTENUATION_CHANGED, new Point3f(attenuation));
     }

    /**
     * Sets the point light's attenuation.
     * @param constant the point light's constant attenuation
     * @param linear the linear attenuation of the light
     * @param quadratic the quadratic attenuation of the light
     */
    void initAttenuation(float constant,
				    float linear,
				    float quadratic)  {
	this.attenuation.x = constant;
	this.attenuation.y = linear;
	this.attenuation.z = quadratic;
    }

    /**
     * Sets the point light's attenuation.
     * @param constant the point light's constant attenuation
     * @param linear the linear attenuation of the light
     * @param quadratic the quadratic attenuation of the light
     */
    void setAttenuation(float constant,
				      float linear,
				      float quadratic) {
	 setAttenuation(new Point3f(constant, linear, quadratic));
     }

    /**
     * Retrieves the light's attenuation and places the value in the parameter
     * specified.
     * @param attenuation the variable that will contain the attenuation
     */
     void getAttenuation(Point3f attenuation) {
        attenuation.set(this.attenuation);
     }

    /** 
     * This update function, and its native counterpart,
     * updates a point light.  This includes its color, attenuation,
     * and its transformed position.
     */
    native void updateLight(long ctx, int lightSlot, float red, float green,
			    float blue, float ax, float ay, float az,
			    float px, float py, float pz);

    void update(long ctx, int lightSlot, double scale) {
	validateAttenuationInEc(scale);
	updateLight(ctx, lightSlot, color.x, color.y, color.z,
		    attenuation.x, linearAttenuationInEc,
		    quadraticAttenuationInEc,
		    xformPosition.x, xformPosition.y,
		    xformPosition.z);
	
    }

    void setLive(SetLiveState s) {
	super.setLive(s);
	J3dMessage createMessage = super.initMessage(9);
	Object[] objs = (Object[])createMessage.args[4];
	objs[7] = new Point3f(position);
	objs[8] = new Point3f(attenuation);

	VirtualUniverse.mc.processMessage(createMessage);
    }

    // This is called only from SpotLightRetained, so as
    // to not create a message for initialization. for spotlight
    // the initialization of the message is done by SpotLightRetained
    void doSetLive(SetLiveState s) {
	super.setLive(s);
    }

    J3dMessage initMessage(int num) {
	J3dMessage createMessage = super.initMessage(num);
	Object[] objs = (Object[])createMessage.args[4];
	objs[7] = new Point3f(position);
	objs[8] = new Point3f(attenuation);
	return createMessage;
    }
	

    // Note : if you add any more fields here , you need to update
    // updateLight() in RenderingEnvironmentStructure
    void updateMirrorObject(Object[] objs) {

	int component = ((Integer)objs[1]).intValue();
	Transform3D mlLastLocalToVworld;
	int i;
	int numLgts =  ((Integer)objs[2]).intValue();
	LightRetained[] mLgts = (LightRetained[]) objs[3];


	if ((component & POSITION_CHANGED) != 0) {
	    for (i = 0; i < numLgts; i++) {
		if (mLgts[i] instanceof PointLightRetained) {
		    PointLightRetained ml = (PointLightRetained)mLgts[i];
		    mlLastLocalToVworld = ml.getLastLocalToVworld();
		    ml.position = (Point3f) objs[4];
		    mlLastLocalToVworld.transform(ml.position, 
						      ml.xformPosition);
		    ml.localToVworldScale = 
			mlLastLocalToVworld.getDistanceScale();
		}
	    }
	}
	else if ((component & ATTENUATION_CHANGED) != 0) {
	    for (i = 0; i < numLgts; i++) {
		if (mLgts[i] instanceof PointLightRetained) {
		    PointLightRetained ml = (PointLightRetained)mLgts[i];
		    ml.attenuation.set((Point3f)objs[4]);
		}
	    }
	}
	else if ((component & INIT_MIRROR) != 0) {
	    for (i = 0; i < numLgts; i++) {
		if (mLgts[i] instanceof PointLightRetained) {
		    PointLightRetained ml = (PointLightRetained)mirrorLights[i];
		    ml.position = (Point3f)((Object[]) objs[4])[7];
		    ml.attenuation.set((Point3f)((Object[]) objs[4])[8]);
		    mlLastLocalToVworld = ml.getLastLocalToVworld();
		    mlLastLocalToVworld.transform(ml.position, 
						      ml.xformPosition);
		    ml.localToVworldScale = 
			mlLastLocalToVworld.getDistanceScale();
		}
	    }
	}

	// call the parent's mirror object update routine
	super.updateMirrorObject(objs);
    }

    void validateAttenuationInEc(double vworldToCoexistenceScale) {
        double localToEcScale = localToVworldScale * vworldToCoexistenceScale;
 
        linearAttenuationInEc = (float)(attenuation.y / localToEcScale);
        quadraticAttenuationInEc =
                (float)(attenuation.z / (localToEcScale * localToEcScale));
    }


    
    // Clones only the retained side, internal use only
     protected Object clone() {
         PointLightRetained pr =
            (PointLightRetained)super.clone();

         pr.attenuation = new Point3f(attenuation);
         pr.position = new Point3f(position);
         pr.xformPosition = new Point3f();
         return pr;
     }   


    // Called on the mirror object
    void updateTransformChange() {
	super.updateTransformChange();

	Transform3D lastLocalToVworld = getLastLocalToVworld();

	lastLocalToVworld.transform(position, xformPosition);
	localToVworldScale = lastLocalToVworld.getDistanceScale();

	validateAttenuationInEc(0.0861328125);
    }

    void sendMessage(int attrMask, Object attr) {

	J3dMessage createMessage = VirtualUniverse.mc.getMessage();
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
        xform.transform.transform(position, position);
    }
}
