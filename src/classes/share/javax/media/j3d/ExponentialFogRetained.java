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

import javax.vecmath.Color3f;
import java.util.ArrayList;

/**
 * The ExponentialFog leaf node defines distance parameters for
 * exponential fog.
 */
class ExponentialFogRetained extends FogRetained {
    // Fog density
    float density = 1.0f;

    // dirty bits for ExponentialFog
    static final int DENSITY_CHANGED	= FogRetained.LAST_DEFINED_BIT << 1;


    ExponentialFogRetained() {
        this.nodeType = NodeRetained.EXPONENTIALFOG;
    }

    /**
     * initializes fog density
     */
    void initDensity(float density){
	this.density = density;
    }

    /**
     * Sets fog density and send a message
     */
    void setDensity(float density){
	this.density = density;
	J3dMessage createMessage = VirtualUniverse.mc.getMessage();
	createMessage.threads = targetThreads;
	createMessage.type = J3dMessage.FOG_CHANGED;
	createMessage.universe = universe;
	createMessage.args[0] = this;
	createMessage.args[1]= new Integer(DENSITY_CHANGED);
	createMessage.args[2] = new Float(density);
	VirtualUniverse.mc.processMessage(createMessage);
    }

    /**
     * Gets fog density
     */
    float getDensity(){
	return this.density;
    }


    void setLive(SetLiveState s) {
	super.setLive(s);
	GroupRetained group;
	
	// Initialize the mirror object, this needs to be done, when
	// renderBin is not accessing any of the fields
	J3dMessage createMessage = VirtualUniverse.mc.getMessage();
	createMessage.threads = J3dThread.UPDATE_RENDERING_ENVIRONMENT;
	createMessage.universe = universe;
	createMessage.type = J3dMessage.FOG_CHANGED;
	createMessage.args[0] = this;
	// a snapshot of all attributes that needs to be initialized
	// in the mirror object
	createMessage.args[1]= new Integer(INIT_MIRROR);
	ArrayList addScopeList = new ArrayList();
	for (int i = 0; i < scopes.size(); i++) {
	    group = (GroupRetained)scopes.get(i);
	    tempKey.reset();
	    group.addAllNodesForScopedFog(mirrorFog, addScopeList, tempKey);
	}
	Object[] scopeInfo = new Object[2];
	scopeInfo[0] = ((scopes.size() > 0) ? Boolean.TRUE:Boolean.FALSE);
	scopeInfo[1] = addScopeList;
	createMessage.args[2] = scopeInfo;
	Color3f clr = new Color3f(color);
	createMessage.args[3] = clr;

	Object[] obj = new Object[5];
	obj[0] = boundingLeaf;
	obj[1] = (regionOfInfluence != null?regionOfInfluence.clone():null);
	obj[2] = (inBackgroundGroup? Boolean.TRUE:Boolean.FALSE);
	obj[3] = geometryBackground;
	obj[4] = new Float(density);
	
	createMessage.args[4] = obj;
	VirtualUniverse.mc.processMessage(createMessage);

    }
    
    
    /** 
     * This method and its native counterpart update the native context
     * fog values.
     */
    native void update(long ctx, float red, float green, float blue, float density);

    void update(long ctx, double scale) {
	update(ctx, color.x, color.y, color.z, density);
    }



    // The update Object function.
    // Note : if you add any more fields here , you need to update
    // updateFog() in RenderingEnvironmentStructure
    void updateMirrorObject(Object[] objs) {

	int component = ((Integer)objs[1]).intValue();


	if ((component & DENSITY_CHANGED) != 0)
	    ((ExponentialFogRetained)mirrorFog).density = ((Float)objs[2]).floatValue();

	if ((component & INIT_MIRROR) != 0) {
	    ((ExponentialFogRetained)mirrorFog).density = ((Float)((Object[])objs[4])[4]).floatValue();
	    
	}

	super.updateMirrorObject(objs);
    }


    // Clone the retained side only, internal use only	
    protected Object clone() {
	ExponentialFogRetained efr =
             (ExponentialFogRetained)super.clone();
 
         efr.initDensity(getDensity());
 
         return efr;
    }
  

}
