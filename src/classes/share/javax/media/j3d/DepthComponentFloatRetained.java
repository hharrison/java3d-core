/*
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
 */

package javax.media.j3d;


/**
 * A 2D array of depth (Z) values in floating point format in the range [0,1].
 * A value of 0.0 indicates the closest Z value to the user while a value of
 * 1.0 indicates the farthest Z value.
 */

class DepthComponentFloatRetained extends DepthComponentRetained {
    float depthData[];

    /**
     * Constructs a new floating-point depth (z-buffer) component object with
     * the specified width and height.
     * @param width the width of the array of depth values
     * @param height the height of the array of depth values
     */
    void initialize(int width, int height) {
        type = DEPTH_COMPONENT_TYPE_FLOAT;
        depthData = new float[width * height];
	this.width = width;
	this.height = height;
    }

    /**
     * Copies the specified depth data to this object.
     * @param depthData array of floats containing the depth data
     */
    void setDepthData(float[] depthData) {
	int i;
	for (i = 0; i < depthData.length; i++)
	    this.depthData[i] = depthData[i];
    }


    /**
     * Copies the depth data from this object to the specified array.
     * @param depthData array of floats that will receive a copy of
     * the depth data
     */
    void getDepthData(float[] depthData) {
	int i;
	for (i = 0; i < this.depthData.length; i++)
	    depthData[i] = this.depthData[i];
    }

    /*
     * retrieve depth data from input buffer
     */
    final void retrieveDepth(float[] buf, int wRead, int hRead) {
        int srcOffset, dstOffset, i;

	// Yup -> Ydown
        for (srcOffset = (hRead - 1) * wRead, dstOffset = 0,
                i = 0; i < hRead; i++,
             srcOffset -= wRead, dstOffset += width) {

            System.arraycopy(buf, srcOffset, depthData, dstOffset, wRead);
        }
    }
}
