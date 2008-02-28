/*
 * $RCSfile$
 *
 * Copyright 1998-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.util.*;
import javax.vecmath.*;

/**
 * Texture2D is a subclass of Texture class. It extends Texture
 * class by adding a constructor and a mutator method for
 * setting a 2D texture image.
 */
class Texture2DRetained extends TextureRetained {

    // Note : There is hardly any HW vendor supports detail Image. 
    //        Detail Image operation is simply no-op in 1.5.
    
    // currently detail image is only applicable to 2D texture
    // detail texture info
    
   // These members are unused except for public set and get methods.
    private ImageComponent2DRetained detailImage = null;
    private int detailTextureMode = Texture2D.DETAIL_MODULATE;
    private int detailTextureLevel = 2;
    private int numDetailTextureFuncPts = 0;
    private float detailTextureFuncPts[] = null;  // array of pairs of floats
                                           // first value for LOD
                                           // second value for the fcn value

    /**
     * Set detail texture image
     */
    final void initDetailImage(ImageComponent2D image) {
	if (image == null) {
	    detailImage = null;
	} else {
	    detailImage = (ImageComponent2DRetained)image.retained;
	}
    }


    /**
     * Get detail texture image
     */
    final ImageComponent2D getDetailImage() {
	if (detailImage != null) {
	    return (ImageComponent2D)detailImage.source;
	} else {
	    return null;
	}
    }


    /**
     * Set detail texture mode
     */
    final void initDetailTextureMode(int mode) {
	detailTextureMode = mode;
    }


    /**
     * Get detail texture mode
     */
    final int getDetailTextureMode() {
	return detailTextureMode;
    }


    /**
     * Set detail texture level
     */
    final void initDetailTextureLevel(int level) {
	detailTextureLevel = level;
    }


    /**
     * Get detail texture level
     */
    final int getDetailTextureLevel() {
	return detailTextureLevel;
    }


    /**
     * Set detail texture function
     */
    final void initDetailTextureFunc(float[] lod, float[] pts) {
        if (lod == null) {  // pts will be null too.
            detailTextureFuncPts = null;
	    numDetailTextureFuncPts = 0;
        } else {
	    numDetailTextureFuncPts = lod.length;
            if ((detailTextureFuncPts == null) ||
                    (detailTextureFuncPts.length != lod.length * 2)) {
                detailTextureFuncPts = new float[lod.length * 2];
            }
            for (int i = 0, j = 0; i < lod.length; i++) {
                detailTextureFuncPts[j++] = lod[i];
                detailTextureFuncPts[j++] = pts[i];
            }
        }
    }

    final void initDetailTextureFunc(Point2f[] pts) {
        if (pts == null) {
            detailTextureFuncPts = null;
	    numDetailTextureFuncPts = 0;
        } else {
	    numDetailTextureFuncPts = pts.length;
            if ((detailTextureFuncPts == null) ||
                    (detailTextureFuncPts.length != pts.length * 2)) {
                detailTextureFuncPts = new float[pts.length * 2];
            }
            for (int i = 0, j = 0; i < pts.length; i++) {
                detailTextureFuncPts[j++] = pts[i].x;
                detailTextureFuncPts[j++] = pts[i].y;
            }
        }
    }

    final void initDetailTextureFunc(float[] pts) {
        if (pts == null) {
            detailTextureFuncPts = null;
	    numDetailTextureFuncPts = 0;
        } else {
	    numDetailTextureFuncPts = pts.length / 2;
            if ((detailTextureFuncPts == null) ||
                    (detailTextureFuncPts.length != pts.length)) {
                detailTextureFuncPts = new float[pts.length];
            }
            for (int i = 0; i < pts.length; i++) {
                detailTextureFuncPts[i] = pts[i];
            }
        }
    }

    /**
     * Get number of points in the detail texture LOD function
     */
    final int getDetailTextureFuncPointsCount() {
        return numDetailTextureFuncPts;
    }


    /**
     * Copies the array of detail texture LOD function points into the
     * specified arrays
     */
    final void getDetailTextureFunc(float[] lod, float[] pts) {
        if (detailTextureFuncPts != null) {
            for (int i = 0, j = 0; i < numDetailTextureFuncPts; i++) {
                lod[i] = detailTextureFuncPts[j++];
                pts[i] = detailTextureFuncPts[j++];
            }
        }
    }

    final void getDetailTextureFunc(Point2f[] pts) {
        if (detailTextureFuncPts != null) {
            for (int i = 0, j = 0; i < numDetailTextureFuncPts; i++) {
                pts[i].x = detailTextureFuncPts[j++];
                pts[i].y = detailTextureFuncPts[j++]; 
	    } 
	}
    }


    /**
     * internal method only -- returns the detail texture LOD function
     */
    final float[] getDetailTextureFunc() {
        return detailTextureFuncPts;
    }


}

