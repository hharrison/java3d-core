/*
 * $RCSfile$
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

/**
 * A 2D array of depth (Z) values stored in the most efficient format for a
 * particular device.  Values are not accessible by the user and may only be
 * used to read the Z values and subsequently write them back.
 */

class DepthComponentNativeRetained extends DepthComponentRetained {
    // Change this to whatever native format is best...
    int depthData[];

    /**
     * Constructs a new native depth (z-buffer) component object with the
     * specified width and height.
     * @param width the width of the array of depth values
     * @param height the height of the array of depth values
     */
    void initialize(int width, int height) {
        type = DEPTH_COMPONENT_TYPE_NATIVE;
        depthData = new int[width * height]; 
        this.width = width; 
        this.height = height; 
    }

    /**
     * Copies the depth data from this object to the specified array.
     * @param depthData array of ints that will receive a copy of
     * the depth data
     */
    void getDepthData(int[] depthData) {
        int i;
        for (i = 0; i < this.depthData.length; i++)
            depthData[i] = this.depthData[i];
    }

    /**
     * retrieve depth data from input buffer
     */
    final void retrieveDepth(int[] buf, int wRead, int hRead) {
        int srcOffset, dstOffset, i;
 
        // Yup -> Ydown
        for (srcOffset = (hRead - 1) * wRead, dstOffset = 0,
                i = 0; i < hRead; i++,
             srcOffset -= wRead, dstOffset += width) {
 
            System.arraycopy(buf, srcOffset, depthData, dstOffset, wRead);
        }
    }   
}
