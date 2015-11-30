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

package org.jogamp.java3d;

/**
 * The RenderMethod interface is used to create various ways to render
 * different geometries.
 */

class VertexArrayRenderMethod implements RenderMethod {


    @Override
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
