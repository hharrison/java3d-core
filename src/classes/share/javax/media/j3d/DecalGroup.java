/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
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
     public Node cloneNode(boolean forceDuplicate) {
	 DecalGroup dg = new DecalGroup();
	 dg.duplicateNode(this, forceDuplicate);
	 return dg;
    }

}
