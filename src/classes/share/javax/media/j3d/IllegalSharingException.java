/*
 * $RCSfile$
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

/**
 * Indicates an illegal attempt to share a scene graph object.  For example,
 * the following are illegal:
 * <UL>
 * <LI>referencing a shared subgraph in more than one virtual universe</LI>
 * <LI>using the same node both in the scene graph and in an
 * immediate mode graphics context</LI>
 * <LI>including any of the following unsupported types of leaf node within a shared subgraph:</LI>
 * <UL>
 * <LI>AlternateAppearance</LI>
 * <LI>Background</LI>
 * <LI>Behavior</LI>
 * <LI>BoundingLeaf</LI>
 * <LI>Clip</LI>
 * <LI>Fog</LI>
 * <LI>ModelClip</LI>
 * <LI>Soundscape</LI>
 * <LI>ViewPlatform</LI>
 * </UL>
 * <LI>referencing a BranchGroup node in more than one of the following
 * ways:</LI>
 * <UL>
 * <LI>attaching it to a (single) Locale</LI>
 * <LI>adding it as a child of a Group Node within the scene graph</LI>
 * <LI>referencing it from a (single) Background Leaf Node as
 * background geometry</LI>
 * </UL>
 * </UL>
 */
public class IllegalSharingException extends IllegalSceneGraphException {

    /**
     * Create the exception object with default values.
     */
    public IllegalSharingException() {
    }

    /**
     * Create the exception object that outputs message.
     * @param str the message string to be output.
     */
    public IllegalSharingException(String str) {
	super(str);
    }

}
