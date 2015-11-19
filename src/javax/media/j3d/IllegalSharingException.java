/*
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
