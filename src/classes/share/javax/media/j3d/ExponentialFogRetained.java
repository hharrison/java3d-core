/*
 * $RCSfile$
 *
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
    private float density = 1.0f;

    // Issue 144: density in Eye Coordinates (EC)
    private float densityInEc;

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
	J3dMessage createMessage = new J3dMessage();
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
	J3dMessage createMessage = new J3dMessage();
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
    void update(Context ctx, double scale) {
        // Issue 144: recompute the density in EC, and send it to native code
	validateDistancesInEc(scale);
	Pipeline.getPipeline().updateExponentialFog(ctx, color.x, color.y, color.z, densityInEc);
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
        // Issue 144: store the local to vworld scale used to transform the density
	((ExponentialFogRetained)mirrorFog).setLocalToVworldScale(getLastLocalToVworld().getDistanceScale());	

	super.updateMirrorObject(objs);
    }


    // Clone the retained side only, internal use only	
    protected Object clone() {
	ExponentialFogRetained efr =
             (ExponentialFogRetained)super.clone();
 
         efr.initDensity(getDensity());
 
         return efr;
    }

    // Issue 144: method to recompute the density in EC by multiplying the specified
    // density by the inverse of the local to EC scale
    /**
     * Scale distances from local to eye coordinate.
     */
    protected void validateDistancesInEc(double vworldToCoexistenceScale) {
        // vworldToCoexistenceScale can be used here since
        // CoexistenceToEc has a unit scale
        double localToEcScale = getLocalToVworldScale() * vworldToCoexistenceScale;

        densityInEc = (float)(density / localToEcScale);
    }

}
