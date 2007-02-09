/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import javax.vecmath.*;
import java.util.*;

/**
 * The LightBin manages a collection of EnvironmentSet objects.
 * The number of objects managed depends upon the number of Lights
 * in each EnvironmentSet and the number of lights supported by
 * the underlying rendering layer.
 */

class EnvironmentSet extends Object implements ObjectUpdate{
    // A list of pre-defined bits to indicate which component
    // of the rendermolecule changed
    static final int LIGHTENABLE_CHANGED       = 0x01;
    static final int AMBIENT_CHANGED           = 0x02;
    static final int FOG_CHANGED               = 0x04;
    static final int MODELCLIP_CHANGED         = 0x08;

    /**
     * The ArrayList of Lights in this EnvironmentSet
     */
    ArrayList lights = new ArrayList();

    /**
     * The position of the light in the lightbin that the
     * lights in this environment set corresponds to
     */
    int[] ltPos = null;


    /**
     * The arraylist of ambient lights in this env list 
     */
    ArrayList ambLights = new ArrayList();

    /**
     * The LightBin that this EnvironmentSet resides
     */
    LightBin lightBin = null;

    /**
     * The bitmask of light slots that need to be enabled for this
     */
    long enableMask = 0;

    /**
     * The cached scene ambient component for this EnvirionmentSet
     */
    Color3f sceneAmbient = new Color3f();

    /**
     * The RenderBin for this EnvirionmentSet
     */
    RenderBin renderBin = null;

    /**
     * The fog for this EnvironmentSet
     */
    FogRetained fog = null;


    /**
     * The model clip for this EnvironmentSet
     */
    ModelClipRetained modelClip = null;

    /**
     * enable mask for the model clip planes in this environment set
     */
    int enableMCMask = 0;       // enable mask used in modelClip.update()
    int enableMCMaskCache = 0;  // enable mask computed in renderBin that
				// is copied into enableMCMask in updateObject

    /**
     * The references to the next and previous LightBins in the
     * list.
     */
    EnvironmentSet next = null;
    EnvironmentSet prev = null;

    /** 
     * List of attrributeBins to be added next Frame
     */
    ArrayList addAttributeBins = new ArrayList();


    /**
     * Canvas Dirty Mask for
     */
    int canvasDirty = 0;

    /**
     * cached value of enable mask
     */
    long enableMaskCache = 0;

    /**
     *
     */
    boolean onUpdateList = false;

    /**
     * The list of AttributeBins in this EnvironmentSet
     */
    AttributeBin attributeBinList = null;

    EnvironmentSet(RenderAtom ra, LightRetained[] lightList, FogRetained fog, 
			ModelClipRetained modelClip, RenderBin rb) {
	renderBin = rb;
	reset(ra, lightList, fog, modelClip);
    }

    void reset(RenderAtom ra, LightRetained[] lightList, FogRetained fog, 
		ModelClipRetained modelClip) {
	int i;
	LightRetained light;

	prev = null;
	next = null;
	onUpdateList = false;
	attributeBinList = null;
	lights.clear();
	ambLights.clear();
	sceneAmbient.x = 0.0f;
	sceneAmbient.y = 0.0f;
	sceneAmbient.z = 0.0f;
	if (lightList != null) {
	    for (i=0; i<lightList.length; i++) {
		light = lightList[i];
	        if (light.nodeType == NodeRetained.AMBIENTLIGHT) {
		    ambLights.add(light);
		    sceneAmbient.x += light.color.x;
		    sceneAmbient.y += light.color.y;
		    sceneAmbient.z += light.color.z;
	        }
		else {
		    lights.add(light);
		}

		light.environmentSets.add(this);
	    }
	    if (sceneAmbient.x > 1.0f) {
		sceneAmbient.x = 1.0f;
	    }
	    if (sceneAmbient.y > 1.0f) {
		sceneAmbient.y = 1.0f;
	    }
	    if (sceneAmbient.z > 1.0f) {
		sceneAmbient.z = 1.0f;
	    }
	}
	this.fog = fog;
	if (fog != null) {
	    fog.environmentSets.add(this);
	}

        this.modelClip = modelClip;
	enableMCMaskCache = 0;
	if (modelClip != null) {
	    modelClip.environmentSets.add(this);

	    for (i = 0; i < 6; i++) {
		 if (modelClip.enables[i]) 
		     enableMCMaskCache |= 1 << i;
	    }
	    enableMCMask = enableMCMaskCache;
	}

	// Allocate the ltPos array
	ltPos = new int[lights.size()];
	enableMask = 0;
    }

    /**
     * This tests if the qiven lights and fog match this EnviornmentSet
     */
    boolean equals(RenderAtom ra, LightRetained[] lights, FogRetained fog, 
			ModelClipRetained modelClip) {
	int i;


	// First see if the lights match.
	if (lights == null && ambLights == null) {
	    if (this.lights.size() == 0) {
		if (this.fog == fog) {
		    return (true);
		} else {
		    return (false);
		}
	    } else {
		return (false);
	    }
	}

	if ((this.lights.size() + this.ambLights.size())!= lights.length) {
	    return (false);
	}

	for (i=0; i<lights.length; i++) {
	    if (lights[i].nodeType == LightRetained.AMBIENTLIGHT) {
		if (!this.ambLights.contains(lights[i])) {
		    return (false);
		}
	    }
	    else {
		if (!this.lights.contains(lights[i])) {
		    return (false);
		}
	    }
	}

	// Now check fog
	if (this.fog != fog) {
	    return (false);
	}

        // Now check model clip
	if (this.modelClip != modelClip) {
	    return (false);
	}

	return (true);
    }


    /**
     * This tests if the qiven lights match this EnviornmentSet
     */
    boolean equalLights(LightRetained[] lights) {
	int i;

	// First see if the lights match.
	if (lights == null && ambLights == null) {
	    if (this.lights.size() == 0) {
		return (true);
	    }
	}

	if ((this.lights.size() + this.ambLights.size())!= lights.length) {
	    return (false);
	}

	for (i=0; i<lights.length; i++) {
	    if (lights[i].nodeType == LightRetained.AMBIENTLIGHT) {
		if (!this.ambLights.contains(lights[i])) {
		    return (false);
		}
	    }
	    else {
		if (!this.lights.contains(lights[i])) {
		    return (false);
		}
	    }
	}

	return (true);
    }


    public void updateObject() {
	int i;
	AttributeBin a;

	if (addAttributeBins.size() > 0) {
	    a = (AttributeBin)addAttributeBins.get(0);
	    if (attributeBinList == null) {
		attributeBinList = a;

	    }
	    else {
		a.next = attributeBinList;
		attributeBinList.prev = a;
		attributeBinList = a;
	    }
	    for (i = 1; i < addAttributeBins.size() ; i++) {
		a = (AttributeBin) addAttributeBins.get(i);
		a.next = attributeBinList;
		attributeBinList.prev = a;
		attributeBinList = a;
	    }
	}

	addAttributeBins.clear();

	if (canvasDirty != 0) {
	    Canvas3D canvases[] = renderBin.view.getCanvases();

	    for (i = 0; i < canvases.length; i++) {
		canvases[i].canvasDirty |= canvasDirty;
	    }

	    if ((canvasDirty & Canvas3D.AMBIENTLIGHT_DIRTY) != 0) {
		updateSceneAmbient();
	    } 

	    if ((canvasDirty & Canvas3D.LIGHTENABLES_DIRTY) != 0) { 
		enableMask = enableMaskCache;
	    }

	    if ((canvasDirty & Canvas3D.MODELCLIP_DIRTY) != 0) { 
		enableMCMask = enableMCMaskCache;
	    }

	    canvasDirty = 0;
	}
	onUpdateList = false;
    }

    /**
     * Adds the given AttributeBin to this EnvironmentSet.
     */
    void addAttributeBin(AttributeBin a, RenderBin rb) {
	a.environmentSet = this;
	addAttributeBins.add(a);
	if (!onUpdateList) {
	    rb.objUpdateList.add(this);
	    onUpdateList = true;
	}
    }

    /**
     * Removes the given AttributeBin from this EnvironmentSet.
     */
    void removeAttributeBin(AttributeBin a) {
	LightRetained light;
	int i;

	a.environmentSet = null;
	// If the attributeBin being remove is contained in addAttributeBins, then
	// remove the attributeBin from the addList
	if (addAttributeBins.contains(a)) {
	    addAttributeBins.remove(addAttributeBins.indexOf(a));
	}
	else {
	    if (a.prev == null) { // At the head of the list
		attributeBinList = a.next;
		if (a.next != null) {
		    a.next.prev = null;
		}
	    } else { // In the middle or at the end.
		a.prev.next = a.next;
		if (a.next != null) {
		    a.next.prev = a.prev;
		}
	    }
	}
	a.prev = null;
	a.next = null;

	if (a.definingRenderingAttributes != null &&
	    (a.definingRenderingAttributes.changedFrequent != 0))
	    a.definingRenderingAttributes = null;
	a.onUpdateList &= ~AttributeBin.ON_CHANGED_FREQUENT_UPDATE_LIST;

	if (attributeBinList == null && addAttributeBins.size() == 0) {
	    // Now remove this environment set from all the lights and fogs
	    // that use this
	    int sz = lights.size();
	    for (i=0; i < sz; i++) {
		((LightRetained) lights.get(i)).environmentSets.remove(this);
	    }
	    sz = ambLights.size();
	    for (i = 0; i < sz; i++) {
		((LightRetained) ambLights.get(i)).environmentSets.remove(this);
	    }
	    if (fog != null) {
		fog.environmentSets.remove(this);
	    }
	    lightBin.removeEnvironmentSet(this);
	}
    }

    void updateSceneAmbient() 
    {
	int i;
	sceneAmbient.x = 0.0f;
	sceneAmbient.y = 0.0f;
	sceneAmbient.z = 0.0f;
	for (i=0; i<ambLights.size(); i++) {
	    LightRetained aL = (LightRetained) ambLights.get(i);
	    if (aL.lightOn) {
		sceneAmbient.x += aL.color.x;
		sceneAmbient.y += aL.color.y;
		sceneAmbient.z += aL.color.z;
	    }
	}
	if (sceneAmbient.x > 1.0f) {
	    sceneAmbient.x = 1.0f;
	}
	if (sceneAmbient.y > 1.0f) {
	    sceneAmbient.y = 1.0f;
	}
	if (sceneAmbient.z > 1.0f) {
	    sceneAmbient.z = 1.0f;
	}
    }

    /**
     * Renders this EnvironmentSet
     */
    void render(Canvas3D cv) {
	AttributeBin a;

        // include this EnvironmentSet to the to-be-updated list in Canvas
        cv.setStateToUpdate(Canvas3D.ENVIRONMENTSET_BIT, this);

	a = attributeBinList;
	while (a != null) {
	    a.render(cv);
	    a = a.next;
	}
    }


    void updateAttributes(Canvas3D cv) {
	LightRetained light;
	int i, numLights;
	float red, green, blue;
	double scale;
	boolean updateSceneAmbient = false, updateLightEnables = false;
	boolean updateModelClip = false, updateFog = false;
	// within frame
	if (cv.environmentSet != this ) {
            if (cv.enableMask != enableMask) {
		updateLightEnables = true;
	    }

            if (cv.sceneAmbient.x != sceneAmbient.x ||
                cv.sceneAmbient.y != sceneAmbient.y ||
                cv.sceneAmbient.z != sceneAmbient.z ) {
		updateSceneAmbient = true;
            }

            if (cv.fog != fog) {
		updateFog = true;
            }

            if (cv.modelClip != modelClip) {
		updateModelClip = true;
            }
	} 

	// Check for dirtybit.
	if ((cv.canvasDirty & (Canvas3D.LIGHTENABLES_DIRTY| 
			       Canvas3D.AMBIENTLIGHT_DIRTY|
			       Canvas3D.FOG_DIRTY|
			       Canvas3D.MODELCLIP_DIRTY|
			       Canvas3D.VIEW_MATRIX_DIRTY)) != 0)  {
	    
	    if ((cv.canvasDirty & Canvas3D.LIGHTENABLES_DIRTY) != 0) {
		updateLightEnables = true;
	    }
	    
	    if ((cv.canvasDirty & Canvas3D.AMBIENTLIGHT_DIRTY) != 0) {
		updateSceneAmbient = true;
	    }
	    
	    if ((cv.canvasDirty & Canvas3D.FOG_DIRTY) != 0) {
		updateFog = true;
	    }
	    
	    if ((cv.canvasDirty & Canvas3D.MODELCLIP_DIRTY) != 0) {
		updateModelClip = true;
	    }

	    if ((cv.canvasDirty &  Canvas3D.VIEW_MATRIX_DIRTY) != 0) {	
		updateFog = true;
		updateModelClip = true;
	    }	    
	}

	// do states update here.
	if (updateLightEnables) {
	    cv.setLightEnables(cv.ctx, enableMask, renderBin.maxLights);
	    cv.enableMask = enableMask;
	}

	if (updateSceneAmbient) {
	    cv.setSceneAmbient(cv.ctx, sceneAmbient.x,
			       sceneAmbient.y, sceneAmbient.z);
	    cv.sceneAmbient.set(sceneAmbient);
	}

	if (updateFog) {
	    if (fog != null) {
		scale = lightBin.geometryBackground == null?
		    cv.canvasViewCache.getVworldToCoexistenceScale():
		    cv.canvasViewCache.getInfVworldToCoexistenceScale();
		fog.update(cv.ctx, scale);
	    } else {
		cv.disableFog(cv.ctx);
	    }
	    cv.fog = fog;
	}

	if (updateModelClip) {
	    if (modelClip != null) {
		modelClip.update(cv, enableMCMask);
	    } else {
		cv.disableModelClip(cv.ctx);
	    }
	    cv.modelClip = modelClip;
	}

	cv.canvasDirty &= ~(Canvas3D.LIGHTENABLES_DIRTY|
			    Canvas3D.AMBIENTLIGHT_DIRTY |
			    Canvas3D.FOG_DIRTY |
			    Canvas3D.MODELCLIP_DIRTY |
			    Canvas3D.VIEW_MATRIX_DIRTY);
	
	cv.environmentSet = this;
	
    }
}
