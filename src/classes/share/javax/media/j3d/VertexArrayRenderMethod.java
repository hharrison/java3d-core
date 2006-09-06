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

class VertexArrayRenderMethod implements RenderMethod {

  
    public boolean render(RenderMolecule rm, Canvas3D cv, 
			  RenderAtomListInfo ra, int dirtyBits) {

        GeometryArrayRetained geo = (GeometryArrayRetained)ra.geometry();
        geo.setVertexFormat((rm.useAlpha && ((geo.vertexFormat & 
					      GeometryArray.COLOR) != 0)), 
			    rm.textureBin.attributeBin.ignoreVertexColors, cv.ctx);
	
	if (rm.doInfinite) {
	    cv.updateState(dirtyBits);
	    while (ra != null) {
		renderGeo(ra, rm, cv);
		ra = ra.next;
	    }
	    return true;
	}

	boolean isVisible = false; // True if any of the RAs is visible.
	while (ra != null) {
	    if (cv.ra == ra.renderAtom) {
		if (cv.raIsVisible) {
		    cv.updateState(dirtyBits);
		    renderGeo(ra, rm, cv);
		    isVisible = true;
		}
	    }
	    else {
		if (!VirtualUniverse.mc.viewFrustumCulling ||
		    ra.renderAtom.localeVwcBounds.intersect(cv.viewFrustum)) {
		    cv.updateState(dirtyBits);
		    cv.raIsVisible = true;
		    renderGeo(ra, rm, cv);
		    isVisible = true;
		}
		else {
		    cv.raIsVisible = false;
		}
		cv.ra = ra.renderAtom;
	    }
	    
	    ra = ra.next;
	}
        geo.disableGlobalAlpha(cv.ctx, 
			       (rm.useAlpha && ((geo.vertexFormat & 
						 GeometryArray.COLOR) != 0)), 
			       rm.textureBin.attributeBin.ignoreVertexColors);
	return isVisible;
    }

    void renderGeo(RenderAtomListInfo ra, RenderMolecule rm, Canvas3D cv) {
	GeometryArrayRetained geo;
        boolean useAlpha;
	
	useAlpha = rm.useAlpha;
	
	geo = (GeometryArrayRetained)ra.geometry();


	geo.execute(cv, ra.renderAtom, rm.isNonUniformScale,
		    (useAlpha && ((geo.vertexFormat & GeometryArray.COLOR) != 0)) ,
		    rm.alpha,
		    cv.screen.screen,
		    rm.textureBin.attributeBin.ignoreVertexColors);
    }
}
