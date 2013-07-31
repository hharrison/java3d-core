/*
 * Copyright 1996-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
 * The DecalGroup node is an ordered group node used for defining decal
 * geometry on top of other geometry. The DecalGroup node specifies that
 * its children should be rendered in index order and that they generate
 * coplanar objects. Examples of this include: painted decals or text on
 * surfaces, a checkerboard layered on top of a table, etc.
 * <p>
 * The first child, at index 0, defines the surface on top of which all
 * other children are rendered. The geometry of this child must encompass
 * all other children, otherwise incorrect rendering may result. The
 * polygons contained within each of the children must be facing the same
 * way. If the polygons defined by the first child are front facing, then
 * all other surfaces should be front facing. In this case, the polygons
 * are rendered in order. The renderer can use knowledge of the coplanar
 * nature of the surfaces to avoid
 * Z-buffer collisions. If the main surface is back facing then all other
 * surfaces should be back facing, and need not be rendered (even if back
 * face culling is disabled).
 * <p>
 * Note that using the DecalGroup node does not guarantee that Z-buffer
 * collisions are avoided. An implementation of Java 3D may fall back to
 * treating DecalGroup node as an OrderedGroup node.
 */
public class DecalGroup extends OrderedGroup {

    /**
     * Constructs and initializes a new DecalGroup node object.
     */
    public DecalGroup() {
    }


    /**
     * Creates the retained mode DecalGroupRetained object that this
     * DecalGroup component object will point to.
     */
    @Override
    void createRetained() {
	this.retained = new DecalGroupRetained();
	this.retained.setSource(this);
    }


    /**
     * Used to create a new instance of the node.  This routine is called
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
     @Override
     public Node cloneNode(boolean forceDuplicate) {
	 DecalGroup dg = new DecalGroup();
	 dg.duplicateNode(this, forceDuplicate);
	 return dg;
    }

}
