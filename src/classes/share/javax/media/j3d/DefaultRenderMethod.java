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

/**
 * The RenderMethod interface is used to create various ways to render
 * different geometries.
 */

class DefaultRenderMethod implements RenderMethod {

    boolean geometryIsLocked = false;

    /**
     * The actual rendering code for this RenderMethod
     */
    public boolean render(RenderMolecule rm, Canvas3D cv,
			  RenderAtomListInfo ra, int dirtyBits) {
        
	boolean isVisible = false; // True if any of the RAs is visible.
	
	while (ra != null) {
	    if (cv.ra == ra.renderAtom) {
		if (cv.raIsVisible) {
		    cv.updateState(dirtyBits);
		    ra.geometry().execute(cv, ra.renderAtom, 
					  rm.isNonUniformScale,
					  rm.useAlpha, rm.alpha,
					  cv.screen.screen,
					  rm.textureBin.attributeBin.
					  ignoreVertexColors);
		    isVisible = true;
		}
	    }
	    else {
		if (!VirtualUniverse.mc.viewFrustumCulling ||
		    ra.renderAtom.localeVwcBounds.intersect(cv.viewFrustum)) {
		    cv.raIsVisible = true;
		    cv.updateState(dirtyBits);
		    ra.geometry().execute(cv, ra.renderAtom, rm.isNonUniformScale,
					  rm.useAlpha, rm.alpha,
					  cv.screen.screen,
					  rm.textureBin.attributeBin.
					  ignoreVertexColors);
		    isVisible = true;
		}
		else {
		    cv.raIsVisible = false;
		}
		cv.ra = ra.renderAtom;
	    }
	    ra = ra.next;
	}
	
	return isVisible;
	
    }
    

    
}
