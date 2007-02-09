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

package javax.media.j3d ;

/**
 * The RenderMethod interface is used to create various ways to render
 * different geometries.
 */

class CompressedGeometryRenderMethod implements RenderMethod {

    /**
     * The actual rendering code for this RenderMethod.
     */
    public boolean render(RenderMolecule rm, Canvas3D cv,
			  RenderAtomListInfo ra, int dirtyBits) {

	CompressedGeometryRetained cgr ;

	if (rm.doInfinite) {
	    cv.updateState(dirtyBits);
	    while (ra != null) {
		renderCompressedGeo(ra, rm, cv);
		ra = ra.next;
	    }
	    return true;
	}
	
	boolean isVisible = false; // True if any of the RAs is visible.

	while (ra != null) {
	    if (cv.ra == ra.renderAtom) {
		if (cv.raIsVisible) {
		    cv.updateState(dirtyBits);
		    renderCompressedGeo(ra, rm, cv);
		    isVisible = true;
		}
	    }
	    else {
		if (!VirtualUniverse.mc.viewFrustumCulling ||
		    ra.renderAtom.localeVwcBounds.intersect(cv.viewFrustum)) {
		    cv.updateState(dirtyBits);
		    cv.raIsVisible = true;
		    renderCompressedGeo(ra, rm, cv);
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

    void renderCompressedGeo(RenderAtomListInfo ra, RenderMolecule rm, Canvas3D cv) {
	
        boolean useAlpha ;
	CompressedGeometryRetained cgr ;
	useAlpha = rm.useAlpha ;
	    
	cgr = (CompressedGeometryRetained)ra.renderAtom.geometryAtom.geometryArray[ra.index];

	/* force_decompression  if lighting is disabled and
	 * ignoreVertexColors is TRUE, since there is no way for openGL
	 * to ignore vertexColors in this case, force decompression
	 */
	if (rm.textureBin.attributeBin.ignoreVertexColors && rm.enableLighting == false && cgr.mirrorGeometry == null) {
	    cgr.mirrorGeometry = cgr.getGeometry(true, cv) ;
	}
	else if (cgr.mirrorGeometry == null) {
	    // cgr.getGeometry() will decompress in software and return a
	    // GeometryRetained if hardware decompression isn't available,
	    // otherwise it just returns cgr.
	    cgr.mirrorGeometry = cgr.getGeometry(false, cv) ;
	    if (cgr.mirrorGeometry == null)
		// decompressor error
		return ;
	}
	      
	cgr.mirrorGeometry.execute(cv, ra.renderAtom, rm.isNonUniformScale,
	     (useAlpha && ra.geometry().noAlpha), rm.alpha,
	     cv.screen.screen,
	     rm.textureBin.attributeBin.ignoreVertexColors);
    }
}
