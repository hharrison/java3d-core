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
