/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 */

package com.sun.j3d.utils.universe;

import javax.media.j3d.*;
import javax.vecmath.*;

/**
 * A convenience class that effectively creates a series of TransformGroup
 * nodes connected one to another hierarchically. For most applications,
 * creating a MultiTransformGroup containing one transform will suffice.
 * More sophisticated applications that use a complex portal/head tracking
 * viewing system may find that more transforms are needed.
 * <P>
 * When more than one transform is needed, transform[0] is considered the
 * "top most" transform with repsect to the scene graph, (attached to the
 * ViewingPlatform node) and transform[numTransforms - 1] is the "bottom
 * most" transform (the ViewPlatorm object is attached to this transform).
 */
public class MultiTransformGroup {

    // For now just have an array of TransformGroup nodes.
    TransformGroup[] transforms;

    /**
     * Creates a MultiTransformGroup node that contains a single transform.
     * This is effectively equivalent to creating a single TransformGroup
     * node.
     */
    public MultiTransformGroup() {
        this(1);
    }

    /**
     * Creates a MultiTransformGroup node that contains the specified
     * number of transforms.
     * <P>
     * When more than one transform is needed, transform[0] is considered the
     * "top most" transform with repsect to the scene graph, (attached to the
     * ViewingPlatform node) and transform[numTransforms - 1] is the "bottom
     * most" transform (the ViewPlatorm object is attached to this transform).
     *
     * @param numTransforms The number of transforms for this node to
     *  contain. If this number is less than one, one is assumed.
     */
    public MultiTransformGroup(int numTransforms) {
        if (numTransforms < 1)
            numTransforms = 1;

        transforms = new TransformGroup[numTransforms];

        // there is always at least one TransformGroup
        transforms[0] = new TransformGroup();
        transforms[0].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transforms[0].setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        transforms[0].setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
        transforms[0].setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);

        for (int i = 1; i < numTransforms; i++) {
            transforms[i] = new TransformGroup();
            transforms[i].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            transforms[i].setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
            transforms[i].setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
            transforms[i].setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
            transforms[i-1].addChild(transforms[i]);
        }
    }

    /**
     * Returns the selected TransformGroup node.
     *
     * @param transform The index of the transform to return.  The indices
     *  are in the range [0..(n - 1)] - where n was the number of transforms
     *  created.  transform[0] is considered the
     *  "top most" transform with repsect to the scene graph, (attached to the
     *  ViewingPlatform node) and transform[numTransforms - 1] is the "bottom
     *  most" transform (the ViewPlatorm object is attached to this transform).
     *
     * @return The TransformGroup node at the designated index. If an out of
     *  range index is given, null is returned.
     */
    public TransformGroup getTransformGroup(int transform) {
        if (transform >= transforms.length || transform < 0)
            return null;

        return transforms[transform];
    }

    /**
     * Returns the number of transforms in this MultiTransformGroup object.
     *
     * @return The number of transforms in this object.
     */
    public int getNumTransforms() {
        return transforms.length;
    }

}
