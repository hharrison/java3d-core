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

import java.util.ArrayList;

/**
 * The LightBin manages a collection of EnvironmentSet objects.
 * The number of objects managed depends upon the number of Lights
 * in each EnvironmentSet and the number of lights supported by
 * the underlying rendering layer.
 */

class LightBin extends Object implements ObjectUpdate {

    /**
     * The maximum number of lights in a LightBin
     */
    int maxLights = -1;

    /**
     * The Array of Light references in this LightBin.
     * This array is always maxLights in length.
     */
    LightRetained[] lights = null;

    /**
     * An Array of reference counts for shared lights in
     * among EnvirionmentSets
     */
    int[] lightsRef = null;

    /**
     * The number of empty light slots in this LightBin
     */
    int numEmptySlots = -1;

    /**
     * The RenderBin for this object
     */
    RenderBin renderBin = null;

    /**
     * The references to the next and previous LightBins in the
     * list.
     */
    LightBin next = null;
    LightBin prev = null;

    /**
     * The list of EnvironmentSets in this LightBin.
     */
    EnvironmentSet environmentSetList = null;

/**
 * List of envSet to be added for the next iteration
 */
ArrayList<EnvironmentSet> insertEnvSet = new ArrayList<EnvironmentSet>();


    /**
     * cache of the canvasDirty
     */
    int canvasDirty = 0;

    /**
     * lightDirty Mask  cache , used to
     * mark the lightdirty bits for next frame
     */
    int lightDirtyMaskCache = 0;


    /**
     * lightDirty Mask used during rendering
     */
    int lightDirtyMask = 0;

/**
 * List of pointLts in this lightbin Need to reload these lights when vworld
 * scale changes
 */
ArrayList<PointLightRetained> pointLts = new ArrayList<PointLightRetained>();
    int[] pointLtsSlotIndex;

    // OrderedGroup info
    OrderedCollection orderedCollection = null;

    boolean onUpdateList = false;

    // background node that contains geometry
    BackgroundRetained geometryBackground = null;



    LightBin(int maxLights, RenderBin rb, boolean isOpaque) {
        this.maxLights = maxLights;
        this.numEmptySlots = maxLights;
	lights = new LightRetained[maxLights];
	lightsRef = new int[maxLights];
	renderBin = rb;
    }

    void reset(boolean inOpaque) {
	prev = null;
	next = null;
        orderedCollection = null;
	environmentSetList = null;
	onUpdateList = false;
        geometryBackground = null;
	// No need to reset the lights and lightRef
        if (J3dDebug.devPhase && J3dDebug.debug) {
	    for (int i=0; i<maxLights; i++) {
	        J3dDebug.doAssert(lights[i] == null, "lights[i] == null");
	        J3dDebug.doAssert(lightsRef[i] == 0, "lightsRef[i] == 0");
	    }
        }
    }

    void setOrderedInfo(OrderedCollection oc) {
        orderedCollection = oc;
    }

    /**
     * Checks to see if an EnvironmentSet will fit into
     * this LightBin.  It takes into account shared lights.
     */
    boolean willEnvironmentSetFit(EnvironmentSet e) {
	int i, j, numEsLights, slotsNeeded;
	LightRetained light;

        numEsLights = e.lights.size();
	slotsNeeded = numEsLights;
	for (i=0; i<numEsLights; i++) {
		light = e.lights.get(i);
	    if (light instanceof AmbientLightRetained) {
		continue;
	    }
	    for (j=0; j<maxLights; j++) {
		if (lights[j] == light) {
		    slotsNeeded--;
		    break;
		}
	    }
	}
	if (slotsNeeded > numEmptySlots) {
	    return (false);
	} else {
	    return (true);
	}
    }

    /**
     * Adds the new EnvironmentSet to this LightBin.
     */
    void addEnvironmentSet(EnvironmentSet e, RenderBin rb) {
	int i, j, numEsLights;
	LightRetained light;

	numEsLights = e.lights.size();
	for (i=0; i<numEsLights; i++) {
		light = e.lights.get(i);
	    if (light instanceof AmbientLightRetained) {
		continue;
	    }
	    for (j=0; j<maxLights; j++) {
		if (lights[j] == light) {
		    if (light.lightOn) {
			e.enableMask |= 1<<j;
		    }
		    lightsRef[j]++;
		    // Keep a reference to the position of the light
		    // in the light bin that this light in the envSet
		    // refers
		    e.ltPos[i] = j;
		    break;
		}
	    }
	    if (j==maxLights) {
		// Find an empty slot
		for (j=0; j<maxLights; j++) {
		    if (lights[j] == null) {
			lights[j] = light;
			lightsRef[j] = 1;
			if (light instanceof PointLightRetained) {
			    pointLts.add((PointLightRetained)light);

			    // save the destinated light slot for point
			    // so that point light can be updated without
			    // referencing the lights list
			    int pointLtsSlotIndexLen = 0;
			    if (pointLtsSlotIndex != null)
				pointLtsSlotIndexLen = pointLtsSlotIndex.length;
			    if (pointLtsSlotIndexLen < pointLts.size()) {

				int[] newIndexList =
					new int[pointLtsSlotIndexLen + 8];
				for (int x = 0; x < pointLtsSlotIndexLen; x++) {
				    newIndexList[x] = pointLtsSlotIndex[x];
				}
				pointLtsSlotIndex = newIndexList;
			    }
			    pointLtsSlotIndex[pointLts.size() - 1] = j;
			}
			if (light.lightOn) {
			    e.enableMask |= 1<<j;
			}
			// Keep a reference to the position of the light
			// in the light bin that this light in the envSet
			// refers
			e.ltPos[i] = j;
			numEmptySlots--;
			break;
		    }
		}
	    }
	}
	e.lightBin = this;
	e.enableMaskCache = e.enableMask;
	insertEnvSet.add(e);
	if (!onUpdateList) {
	    rb.objUpdateList.add(this);
	    onUpdateList = true;
	}

    }

    @Override
    public void updateObject() {
	int i;
	EnvironmentSet e ;


	// Handle insertion
	if (insertEnvSet.size() > 0) {
		e = insertEnvSet.get(0);
	    if (environmentSetList == null) {
		environmentSetList = e;
	    }
	    else {
		e.next = environmentSetList;
		environmentSetList.prev = e;
		environmentSetList = e;
	    }
	    for (i = 1; i < insertEnvSet.size(); i++) {
			e = insertEnvSet.get(i);
		e.next = environmentSetList;
		environmentSetList.prev = e;
		environmentSetList = e;
	    }
	}


	insertEnvSet.clear();
	if (canvasDirty != 0) {

	    Canvas3D canvases[] = renderBin.view.getCanvases();
	    for (i = 0; i < canvases.length; i++) {
		canvases[i].canvasDirty |= canvasDirty;
	    }
	    lightDirtyMask = lightDirtyMaskCache;
	    canvasDirty = 0;
	    lightDirtyMaskCache = 0;
	}
	onUpdateList = false;
    }



    /**
     * Removes the given EnvironmentSet from this LightBin.
     */
    void removeEnvironmentSet(EnvironmentSet e) {
	int i, j, numEsLights;
	LightRetained light;

	e.lightBin = null;
	// If envSet being remove is contained in envSet, then
	// remove the envSet from the addList
	if (insertEnvSet.contains(e)) {
	    insertEnvSet.remove(insertEnvSet.indexOf(e));
	}
	else {
	    numEsLights = e.lights.size();
	    for (i=0; i<numEsLights; i++) {
			light = e.lights.get(i);
		for (j=0; j<maxLights; j++) {
		    if (lights[j] == light) {
			lightsRef[j]--;
			if (lightsRef[j] == 0) {
			    if (light instanceof PointLightRetained)
				pointLts.remove(pointLts.indexOf(light));
			    lights[j] = null;
			    // If the lightBin is dirty unset the mask
			    lightDirtyMaskCache &= ~(1 << j);
			    // since the canvas may already be updated,
			    lightDirtyMask &= ~(1 << j);
			    numEmptySlots++;
			}
			break;
		    }
		}
	    }

	    if (e.prev == null) { // At the head of the list
		environmentSetList = e.next;
		if (e.next != null) {
		    e.next.prev = null;
		}
	    } else { // In the middle or at the end.
		e.prev.next = e.next;
		if (e.next != null) {
		    e.next.prev = e.prev;
		}
	    }

	    // Mark all canvases that uses this environment set as
	    Canvas3D canvases[] = renderBin.view.getCanvases();
	    for (i = 0; i < canvases.length; i++) {
		// Mark the environmentSet cached by all the canvases as null
		// to force to reEvaluate when it comes back from the freelist
		// During envset::render(), we only check for the pointers not
		// being the same, so we need to take care of the env set
		// gotten from the freelist from one frame to another
		canvases[i].environmentSet = null;
	    }

	}
	e.prev = null;
	e.next = null;

	if (environmentSetList == null && insertEnvSet.size() == 0) {
	    renderBin.removeLightBin(this);
	    geometryBackground = null;
	}

    }

    /**
     * Renders this LightBin
     */
    void render(Canvas3D cv) {
	EnvironmentSet e;

	// include this LightBin to the to-be-updated list in Canvas
        cv.setStateToUpdate(Canvas3D.LIGHTBIN_BIT, this);

	e = environmentSetList;
	while (e != null) {
	    e.render(cv);
	    e = e.next;
	}
    }

    void updateAttributes(Canvas3D cv) {
	int i;
	double scale;

	int frameCount = VirtualUniverse.mc.frameCount;

	// TODO: When working on issue 15 and 88, we realise that the
	// logic in this method flaw. As we are ready into 1.3.2beta1
	// phase, and there isn't an existing issue related to the logic
	// error in method, we decided not to fix it for now. This method
	// should have the logic as in EnvironmentSet.updateAttributes();
	// The fix to issue 15 and 88.

	// within frames
	if (cv.lightBin != this) {

            if (geometryBackground == null) {
		scale = cv.canvasViewCache.getVworldToCoexistenceScale();
                cv.setModelViewMatrix(cv.ctx, cv.vpcToEc.mat,
				      renderBin.vworldToVpc);
	    } else {
		scale = cv.canvasViewCache.getInfVworldToCoexistenceScale();
                cv.setModelViewMatrix(cv.ctx, cv.vpcToEc.mat,
				      renderBin.infVworldToVpc);
	    }


	    for (i=0; i<maxLights; i++) {
		if (lights[i] != null) {
                    if (cv.lights[i] != lights[i] ||
				cv.frameCount[i] != frameCount) {
                        cv.lights[i] = lights[i];
                        cv.frameCount[i] = frameCount;
                        lights[i].update(cv.ctx, i,scale);
                    }
		}
	    }
	    cv.lightBin = this;
	    cv.canvasDirty &= ~Canvas3D.LIGHTBIN_DIRTY;
	    // invalidate canvas cached enableMask
	    cv.enableMask = -1;
	}
	// across frames
	else if ((cv.canvasDirty & Canvas3D.LIGHTBIN_DIRTY) != 0) {
	    // Just update the dirty lights
            if (geometryBackground == null) {
		scale = cv.canvasViewCache.getVworldToCoexistenceScale();
                cv.setModelViewMatrix(cv.ctx, cv.vpcToEc.mat,
				      renderBin.vworldToVpc);
	    } else {
		scale = cv.canvasViewCache.getInfVworldToCoexistenceScale();
                cv.setModelViewMatrix(cv.ctx, cv.vpcToEc.mat,
				      renderBin.infVworldToVpc);
	    }
	    i = 0;
	    int mask = lightDirtyMask;
	    while (mask != 0) {
		if ((mask & 1) != 0) {
		    lights[i].update(cv.ctx, i, scale);
                    cv.lights[i] = lights[i];
                    cv.frameCount[i] = frameCount;
		}
		mask >>= 1;
		i++;
	    }

	    cv.canvasDirty &= ~Canvas3D.LIGHTBIN_DIRTY;
	}
	else if ((pointLts.size() > 0) && ((cv.canvasDirty & Canvas3D.VIEW_MATRIX_DIRTY) != 0 )) {
            if (geometryBackground == null) {
		scale = cv.canvasViewCache.getVworldToCoexistenceScale();
                cv.setModelViewMatrix(cv.ctx, cv.vpcToEc.mat,
				      renderBin.vworldToVpc);
	    } else {
		scale = cv.canvasViewCache.getInfVworldToCoexistenceScale();
                cv.setModelViewMatrix(cv.ctx, cv.vpcToEc.mat,
				      renderBin.infVworldToVpc);
	    }
	    for (i = 0; i < pointLts.size(); i++) {
			PointLightRetained lt = pointLts.get(i);
		lt.update(cv.ctx, pointLtsSlotIndex[i], scale);
                cv.lights[pointLtsSlotIndex[i]] = lt;
                cv.frameCount[pointLtsSlotIndex[i]] = frameCount;
	    }
	}
    }
}
