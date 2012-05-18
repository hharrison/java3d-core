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
 * A BackgroundSound node defines an unattenuated, nonspatialized sound
 * source that has no position or direction. It has the same attributes as a
 * Sound node. This type of sound is simply added to the sound mix without
 * modification and is useful for playing a mono or stereo music track, or an
 * ambient sound effect. Unlike a Background (visual) node, more than one
 * BackgroundSound node can be simultaneously enabled and active.
 */
public class BackgroundSound extends Sound {
    /**
     * Constructs a new BackgroundSound node using the default parameters
     * for Sound nodes.
     */
    public BackgroundSound() {
        /**
         * Uses default values defined in SoundRetained.java
         */
    }

    /**
     * Constructs a BackgroundSound node object using only the provided
     * parameter values for sound data and sample gain. The remaining fields
     * are set to the default values for a Sound node.
     * @param soundData sound data associated with this sound source node
     * @param initialGain amplitude scale factor applied to sound source
     */
    public BackgroundSound(MediaContainer soundData, float initialGain ) {
        super(soundData, initialGain);
    }

    /**
     * Constructs a BackgroundSound object accepting all the parameters
     * associated with a Sound node.
     * @param soundData sound data associated with this sound source node
     * @param initialGain amplitude scale factor applied to sound source
     * @param loopCount number of times loop is looped
     * @param release flag denoting playing sound data to end
     * @param continuous denotes that sound silently plays when disabled
     * @param enable sound switched on/off
     * @param region scheduling bounds
     * @param priority playback ranking value
     */
    public BackgroundSound(MediaContainer soundData,
                           float initialGain,
                           int loopCount,
                           boolean release,
                           boolean continuous,
                           boolean enable,
                           Bounds  region,
                           float   priority) {

        super(soundData, initialGain, loopCount, release, continuous,
                   enable, region, priority );
    }


    /**
     * Creates the retained mode BackgroundSoundRetained object that this
     * BackgroundSound component object will point to.
     */
    void createRetained() {
	this.retained = new BackgroundSoundRetained();
	this.retained.setSource(this);
    }


    /**
     * Creates a new instance of the node.  This routine is called
     * by <code>cloneTree</code> to duplicate the current node.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @see Node#cloneTree
     * @see Node#cloneNode
     * @see Node#duplicateNode
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public Node cloneNode(boolean forceDuplicate) {
        BackgroundSound b = new BackgroundSound();
        b.duplicateNode(this, forceDuplicate);
        return b;
    }

    /**
     * Copies all node information from <code>originalNode</code> into
     * the current node.  This method is called from the
     * <code>cloneNode</code> method which is, in turn, called by the
     * <code>cloneTree</code> method.
     * <P>
     * For any <code>NodeComponent</code> objects
     * contained by the object being duplicated, each <code>NodeComponent</code>
     * object's <code>duplicateOnCloneTree</code> value is used to determine
     * whether the <code>NodeComponent</code> should be duplicated in the new node
     * or if just a reference to the current node should be placed in the
     * new node.  This flag can be overridden by setting the
     * <code>forceDuplicate</code> parameter in the <code>cloneTree</code>
     * method to <code>true</code>.
     *
     * <br>
     * NOTE: Applications should <i>not</i> call this method directly.
     * It should only be called by the cloneNode method.
     *
     * @param originalNode the original node to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     * @exception ClassCastException if originalNode is not an instance of
     *  <code>Sound</code>
     *
     * @see Node#cloneTree
     * @see Node#cloneNode
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public void duplicateNode(Node originalNode, boolean forceDuplicate) {
	checkDuplicateNode(originalNode, forceDuplicate);
    }
}
