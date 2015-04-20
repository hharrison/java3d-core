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
 * The BranchGroup serves as a pointer to the root of a
 * scene graph branch; BranchGroup objects are the only objects that
 * can be inserted into a Locale's set of objects. A subgraph, rooted
 * by a BranchGroup node can be thought of as a compile unit. The
 * following things may be done with BranchGroup:
 * <P><UL>
 * <LI>A BranchGroup may be compiled by calling its compile method. This causes the
 * entire subgraph to be compiled. If any BranchGroup nodes are contained within the
 * subgraph, they are compiled as well (along with their descendants).</LI>
 * <p>
 * <LI>A BranchGroup may be inserted into a virtual universe by attaching it to a
 * Locale. The entire subgraph is then said to be live.</LI>
 * <p>
 * <LI>A BranchGroup that is contained within another subgraph may be reparented or
 * detached at run time if the appropriate capabilities are set.</LI>
 * </UL>
 * Note that that if a BranchGroup is included in another subgraph, as a child of
 * some other group node, it may not be attached to a Locale.
 */

public class BranchGroup extends Group {

  /**
   * For BranchGroup nodes, specifies that this BranchGroup allows detaching
   * from its parent.
   */
    public static final int
        ALLOW_DETACH = CapabilityBits.BRANCH_GROUP_ALLOW_DETACH;

    /**
     * Constructs and initializes a new BranchGroup node object.
     */
    public BranchGroup() {
    }

    /**
     * Creates the retained mode BranchGroupRetained object that this
     * BranchGroup component object will point to.
     */
    @Override
    void createRetained() {
	this.retained = new BranchGroupRetained();
	this.retained.setSource(this);
    }


  /**
   * Compiles the source BranchGroup associated with this object and
   * creates and caches a compiled scene graph.
   * @exception SceneGraphCycleException if there is a cycle in the
   * scene graph
   * @exception RestrictedAccessException if the method is called
   * when this object is part of a live scene graph.
   */
    public void compile() {
        if (isLive()) {
	    throw new RestrictedAccessException(
				    J3dI18N.getString("BranchGroup0"));
	}

	if (isCompiled() == false) {
	    // will throw SceneGraphCycleException if there is a cycle
	    // in the scene graph
	    checkForCycle();

	    ((BranchGroupRetained)this.retained).compile();
	}
    }

  /**
   * Detaches this BranchGroup from its parent.
   */
    public void detach() {
	Group parent;

	if (isLiveOrCompiled()) {
	    if(!this.getCapability(ALLOW_DETACH))
		throw new CapabilityNotSetException(J3dI18N.getString("BranchGroup1"));

	    if (((BranchGroupRetained)this.retained).parent != null) {
		parent = (Group)((BranchGroupRetained)this.retained).parent.source;
		if(!parent.getCapability(Group.ALLOW_CHILDREN_WRITE))
		    throw new CapabilityNotSetException(J3dI18N.getString("BranchGroup2"));
	    }
	}

	((BranchGroupRetained)this.retained).detach();
    }


    void validateModeFlagAndPickShape(int mode, int flags, PickShape pickShape) {

        if(isLive()==false) {
	    throw new IllegalStateException(J3dI18N.getString("BranchGroup3"));
        }

        if((mode != PickInfo.PICK_BOUNDS) && (mode != PickInfo.PICK_GEOMETRY)) {

          throw new IllegalArgumentException(J3dI18N.getString("BranchGroup4"));
        }

        if((pickShape instanceof PickPoint) && (mode == PickInfo.PICK_GEOMETRY)) {
          throw new IllegalArgumentException(J3dI18N.getString("BranchGroup5"));
        }

        if(((flags & PickInfo.CLOSEST_GEOM_INFO) != 0) &&
                ((flags & PickInfo.ALL_GEOM_INFO) != 0)) {
            throw new IllegalArgumentException(J3dI18N.getString("BranchGroup6"));
        }

        if((mode == PickInfo.PICK_BOUNDS) &&
                (((flags & (PickInfo.CLOSEST_GEOM_INFO |
                            PickInfo.ALL_GEOM_INFO |
                            PickInfo.CLOSEST_DISTANCE |
                            PickInfo.CLOSEST_INTERSECTION_POINT)) != 0))) {

          throw new IllegalArgumentException(J3dI18N.getString("BranchGroup7"));
        }

        if((pickShape instanceof PickBounds) &&
                (((flags & (PickInfo.CLOSEST_GEOM_INFO |
                            PickInfo.ALL_GEOM_INFO |
                            PickInfo.CLOSEST_DISTANCE |
                            PickInfo.CLOSEST_INTERSECTION_POINT)) != 0))) {

          throw new IllegalArgumentException(J3dI18N.getString("BranchGroup8"));
        }

    }

  /**
   * Returns an array referencing all the items that are pickable below this
   * <code>BranchGroup</code> that intersect with PickShape.
   * The resultant array is unordered.
   *
   * @param pickShape the PickShape object
   *
   * @see SceneGraphPath
   * @see Locale#pickAll
   * @see PickShape
   * @exception IllegalStateException if BranchGroup is not live.
   *
   */
    public SceneGraphPath[] pickAll( PickShape pickShape ) {

        if(isLive()==false)
	    throw new IllegalStateException(J3dI18N.getString("BranchGroup3"));

        return ((BranchGroupRetained)this.retained).pickAll(pickShape);

 }

    /**
     * Returns an array unsorted references to all the PickInfo objects that are
     * pickable  below this <code>BranchGroup</code> that intersect with PickShape.
     * The accuracy of the pick is set by the pick mode. The mode include :
     * PickInfo.PICK_BOUNDS and PickInfo.PICK_GEOMETRY. The amount of information returned
     * is specified via a masked variable, flags, indicating which components are
     * present in each returned PickInfo object.
     *
     * @param mode  picking mode, one of <code>PickInfo.PICK_BOUNDS</code> or <code>PickInfo.PICK_GEOMETRY</code>.
     *
     * @param flags a mask indicating which components are present in each PickInfo object.
     * This is specified as one or more individual bits that are bitwise "OR"ed together to
     * describe the PickInfo data. The flags include :
     * <ul>
     * <code>PickInfo.SCENEGRAPHPATH</code> - request for computed SceneGraphPath.<br>
     * <code>PickInfo.NODE</code> - request for computed intersected Node.<br>
     * <code>PickInfo.LOCAL_TO_VWORLD</code> - request for computed local to virtual world transform.<br>
     * <code>PickInfo.CLOSEST_INTERSECTION_POINT</code> - request for closest intersection point.<br>
     * <code>PickInfo.CLOSEST_DISTANCE</code> - request for the distance of closest intersection.<br>
     * <code>PickInfo.CLOSEST_GEOM_INFO</code> - request for only the closest intersection geometry information.<br>
     * <code>PickInfo.ALL_GEOM_INFO</code> - request for all intersection geometry information.<br>
     * </ul>
     *
     * @param pickShape the description of this picking volume or area.
     *
     * @exception IllegalArgumentException if flags contains both CLOSEST_GEOM_INFO and
     * ALL_GEOM_INFO.
     *
     * @exception IllegalArgumentException if pickShape is a PickPoint and pick mode
     * is set to PICK_GEOMETRY.
     *
     * @exception IllegalArgumentException if pick mode is neither PICK_BOUNDS
     * nor PICK_GEOMETRY.
     *
     * @exception IllegalArgumentException if pick mode is PICK_BOUNDS
     * and flags includes any of CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE,
     * CLOSEST_GEOM_INFO or ALL_GEOM_INFO.
     *
     * @exception IllegalArgumentException if pickShape is PickBounds
     * and flags includes any of CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE,
     * CLOSEST_GEOM_INFO or ALL_GEOM_INFO.
     *
     * @exception IllegalStateException if BranchGroup is not live.
     *
     * @exception CapabilityNotSetException if the mode is
     * PICK_GEOMETRY and the Geometry.ALLOW_INTERSECT capability bit
     * is not set in any Geometry objects referred to by any shape
     * node whose bounds intersects the PickShape.
     *
     * @exception CapabilityNotSetException if flags contains any of
     * CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE, CLOSEST_GEOM_INFO
     * or ALL_GEOM_INFO, and the capability bits that control reading of
     * coordinate data are not set in any GeometryArray object referred
     * to by any shape node that intersects the PickShape.
     * The capability bits that must be set to avoid this exception are as follows :
     * <ul>
     * <li>By-copy geometry : GeometryArray.ALLOW_COORDINATE_READ</li>
     * <li>By-reference geometry : GeometryArray.ALLOW_REF_DATA_READ</li>
     * <li>Indexed geometry : IndexedGeometryArray.ALLOW_COORDINATE_INDEX_READ
     * (in addition to one of the above)</li>
     * </ul>
     *
     * @see Locale#pickAll(int,int,javax.media.j3d.PickShape)
     * @see PickInfo
     *
     * @since Java 3D 1.4
     *
     */

    public PickInfo[] pickAll( int mode, int flags, PickShape pickShape ) {

        validateModeFlagAndPickShape(mode, flags, pickShape);
        return ((BranchGroupRetained)this.retained).pickAll(mode, flags, pickShape);

    }


  /**
   * Returns a sorted array of references to all the Pickable items that
   * intersect with the pickShape. Element [0] references the item closest
   * to <i>origin</i> of PickShape successive array elements are further
   * from the <i>origin</i>
   *
   * Note: If pickShape is of type PickBounds, the resulting array
   * is unordered.
   * @param pickShape the PickShape object
   *
   * @see SceneGraphPath
   * @see Locale#pickAllSorted
   * @see PickShape
   * @exception IllegalStateException if BranchGroup is not live.
   *
   */
    public SceneGraphPath[] pickAllSorted( PickShape pickShape ) {

        if(isLive()==false)
	    throw new IllegalStateException(J3dI18N.getString("BranchGroup3"));

        return ((BranchGroupRetained)this.retained).pickAllSorted(pickShape);

    }


    /**
     * Returns a sorted array of PickInfo references to all the pickable
     * items that intersect with the pickShape. Element [0] references
     * the item closest to <i>origin</i> of PickShape successive array
     * elements are further from the <i>origin</i>
     * The accuracy of the pick is set by the pick mode. The mode include :
     * PickInfo.PICK_BOUNDS and PickInfo.PICK_GEOMETRY. The amount of information returned
     * is specified via a masked variable, flags, indicating which components are
     * present in each returned PickInfo object.
     *
     * @param mode  picking mode, one of <code>PickInfo.PICK_BOUNDS</code> or <code>PickInfo.PICK_GEOMETRY</code>.
     *
     * @param flags a mask indicating which components are present in each PickInfo object.
     * This is specified as one or more individual bits that are bitwise "OR"ed together to
     * describe the PickInfo data. The flags include :
     * <ul>
     * <code>PickInfo.SCENEGRAPHPATH</code> - request for computed SceneGraphPath.<br>
     * <code>PickInfo.NODE</code> - request for computed intersected Node.<br>
     * <code>PickInfo.LOCAL_TO_VWORLD</code> - request for computed local to virtual world transform.<br>
     * <code>PickInfo.CLOSEST_INTERSECTION_POINT</code> - request for closest intersection point.<br>
     * <code>PickInfo.CLOSEST_DISTANCE</code> - request for the distance of closest intersection.<br>
     * <code>PickInfo.CLOSEST_GEOM_INFO</code> - request for only the closest intersection geometry information.<br>
     * <code>PickInfo.ALL_GEOM_INFO</code> - request for all intersection geometry information.<br>
     * </ul>
     *
     * @param pickShape the description of this picking volume or area.
     *
     * @exception IllegalArgumentException if flags contains both CLOSEST_GEOM_INFO and
     * ALL_GEOM_INFO.
     *
     * @exception IllegalArgumentException if pickShape is a PickPoint and pick mode
     * is set to PICK_GEOMETRY.
     *
     * @exception IllegalArgumentException if pick mode is neither PICK_BOUNDS
     * nor PICK_GEOMETRY.
     *
     * @exception IllegalArgumentException if pick mode is PICK_BOUNDS
     * and flags includes any of CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE,
     * CLOSEST_GEOM_INFO or ALL_GEOM_INFO.
     *
     * @exception IllegalArgumentException if pickShape is PickBounds
     * and flags includes any of CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE,
     * CLOSEST_GEOM_INFO or ALL_GEOM_INFO.
     *
     * @exception IllegalStateException if BranchGroup is not live.
     *
     * @exception CapabilityNotSetException if the mode is
     * PICK_GEOMETRY and the Geometry.ALLOW_INTERSECT capability bit
     * is not set in any Geometry objects referred to by any shape
     * node whose bounds intersects the PickShape.
     *
     * @exception CapabilityNotSetException if flags contains any of
     * CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE, CLOSEST_GEOM_INFO
     * or ALL_GEOM_INFO, and the capability bits that control reading of
     * coordinate data are not set in any GeometryArray object referred
     * to by any shape node that intersects the PickShape.
     * The capability bits that must be set to avoid this exception are as follows :
     * <ul>
     * <li>By-copy geometry : GeometryArray.ALLOW_COORDINATE_READ</li>
     * <li>By-reference geometry : GeometryArray.ALLOW_REF_DATA_READ</li>
     * <li>Indexed geometry : IndexedGeometryArray.ALLOW_COORDINATE_INDEX_READ
     * (in addition to one of the above)</li>
     * </ul>
     *
     * @see Locale#pickAllSorted(int,int,javax.media.j3d.PickShape)
     * @see PickInfo
     *
     * @since Java 3D 1.4
     *
     */
    public PickInfo[] pickAllSorted( int mode, int flags, PickShape pickShape ) {

        validateModeFlagAndPickShape(mode, flags, pickShape);
        return ((BranchGroupRetained)this.retained).pickAllSorted(mode, flags, pickShape);

    }

  /**
   * Returns a SceneGraphPath that references the pickable item
   * closest to the origin of <code>pickShape</code>.
   *
   * Note: If pickShape is of type PickBounds, the return is any pickable node
   * below this BranchGroup.
   * @param pickShape the PickShape object
   *
   * @see SceneGraphPath
   * @see Locale#pickClosest
   * @see PickShape
   * @exception IllegalStateException if BranchGroup is not live.
   *
   */
    public SceneGraphPath pickClosest( PickShape pickShape ) {

        if(isLive()==false)
	    throw new IllegalStateException(J3dI18N.getString("BranchGroup3"));

        return ((BranchGroupRetained)this.retained).pickClosest(pickShape);

    }

    /**
     * Returns a PickInfo which references the pickable item
     * which is closest to the origin of <code>pickShape</code>.
     * The accuracy of the pick is set by the pick mode. The mode include :
     * PickInfo.PICK_BOUNDS and PickInfo.PICK_GEOMETRY. The amount of information returned
     * is specified via a masked variable, flags, indicating which components are
     * present in each returned PickInfo object.
     *
     * @param mode  picking mode, one of <code>PickInfo.PICK_BOUNDS</code> or <code>PickInfo.PICK_GEOMETRY</code>.
     *
     * @param flags a mask indicating which components are present in each PickInfo object.
     * This is specified as one or more individual bits that are bitwise "OR"ed together to
     * describe the PickInfo data. The flags include :
     * <ul>
     * <code>PickInfo.SCENEGRAPHPATH</code> - request for computed SceneGraphPath.<br>
     * <code>PickInfo.NODE</code> - request for computed intersected Node.<br>
     * <code>PickInfo.LOCAL_TO_VWORLD</code> - request for computed local to virtual world transform.<br>
     * <code>PickInfo.CLOSEST_INTERSECTION_POINT</code> - request for closest intersection point.<br>
     * <code>PickInfo.CLOSEST_DISTANCE</code> - request for the distance of closest intersection.<br>
     * <code>PickInfo.CLOSEST_GEOM_INFO</code> - request for only the closest intersection geometry information.<br>
     * <code>PickInfo.ALL_GEOM_INFO</code> - request for all intersection geometry information.<br>
     * </ul>
     *
     * @param pickShape the description of this picking volume or area.
     *
     * @exception IllegalArgumentException if flags contains both CLOSEST_GEOM_INFO and
     * ALL_GEOM_INFO.
     *
     * @exception IllegalArgumentException if pickShape is a PickPoint and pick mode
     * is set to PICK_GEOMETRY.
     *
     * @exception IllegalArgumentException if pick mode is neither PICK_BOUNDS
     * nor PICK_GEOMETRY.
     *
     * @exception IllegalArgumentException if pick mode is PICK_BOUNDS
     * and flags includes any of CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE,
     * CLOSEST_GEOM_INFO or ALL_GEOM_INFO.
     *
     * @exception IllegalArgumentException if pickShape is PickBounds
     * and flags includes any of CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE,
     * CLOSEST_GEOM_INFO or ALL_GEOM_INFO.
     *
     * @exception IllegalStateException if BranchGroup is not live.
     *
     * @exception CapabilityNotSetException if the mode is
     * PICK_GEOMETRY and the Geometry.ALLOW_INTERSECT capability bit
     * is not set in any Geometry objects referred to by any shape
     * node whose bounds intersects the PickShape.
     *
     * @exception CapabilityNotSetException if flags contains any of
     * CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE, CLOSEST_GEOM_INFO
     * or ALL_GEOM_INFO, and the capability bits that control reading of
     * coordinate data are not set in any GeometryArray object referred
     * to by any shape node that intersects the PickShape.
     * The capability bits that must be set to avoid this exception are as follows :
     * <ul>
     * <li>By-copy geometry : GeometryArray.ALLOW_COORDINATE_READ</li>
     * <li>By-reference geometry : GeometryArray.ALLOW_REF_DATA_READ</li>
     * <li>Indexed geometry : IndexedGeometryArray.ALLOW_COORDINATE_INDEX_READ
     * (in addition to one of the above)</li>
     * </ul>
     *
     * @see Locale#pickClosest(int,int,javax.media.j3d.PickShape)
     * @see PickInfo
     *
     * @since Java 3D 1.4
     *
     */
    public PickInfo pickClosest( int mode, int flags, PickShape pickShape ) {

        validateModeFlagAndPickShape(mode, flags, pickShape);
        return ((BranchGroupRetained)this.retained).pickClosest(mode, flags, pickShape);

    }


  /**
   * Returns a reference to any item that is Pickable below this BranchGroup that
   * intersects with <code>pickShape</code>.
   * @param pickShape the PickShape object
   *
   * @see SceneGraphPath
   * @see Locale#pickAny
   * @see PickShape
   * @exception IllegalStateException if BranchGroup is not live.
   *
   */
    public SceneGraphPath pickAny( PickShape pickShape ) {

        if(isLive()==false)
	    throw new IllegalStateException(J3dI18N.getString("BranchGroup3"));

        return ((BranchGroupRetained)this.retained).pickAny(pickShape);

    }

    /**
     * Returns a PickInfo which references the pickable item  below this
     * BranchGroup that intersects with <code>pickShape</code>.
     * The accuracy of the pick is set by the pick mode. The mode include :
     * PickInfo.PICK_BOUNDS and PickInfo.PICK_GEOMETRY. The amount of information returned
     * is specified via a masked variable, flags, indicating which components are
     * present in each returned PickInfo object.
     *
     * @param mode  picking mode, one of <code>PickInfo.PICK_BOUNDS</code> or <code>PickInfo.PICK_GEOMETRY</code>.
     *
     * @param flags a mask indicating which components are present in each PickInfo object.
     * This is specified as one or more individual bits that are bitwise "OR"ed together to
     * describe the PickInfo data. The flags include :
     * <ul>
     * <code>PickInfo.SCENEGRAPHPATH</code> - request for computed SceneGraphPath.<br>
     * <code>PickInfo.NODE</code> - request for computed intersected Node.<br>
     * <code>PickInfo.LOCAL_TO_VWORLD</code> - request for computed local to virtual world transform.<br>
     * <code>PickInfo.CLOSEST_INTERSECTION_POINT</code> - request for closest intersection point.<br>
     * <code>PickInfo.CLOSEST_DISTANCE</code> - request for the distance of closest intersection.<br>
     * <code>PickInfo.CLOSEST_GEOM_INFO</code> - request for only the closest intersection geometry information.<br>
     * <code>PickInfo.ALL_GEOM_INFO</code> - request for all intersection geometry information.<br>
     * </ul>
     *
     * @param pickShape the description of this picking volume or area.
     *
     * @exception IllegalArgumentException if flags contains both CLOSEST_GEOM_INFO and
     * ALL_GEOM_INFO.
     *
     * @exception IllegalArgumentException if pickShape is a PickPoint and pick mode
     * is set to PICK_GEOMETRY.
     *
     * @exception IllegalArgumentException if pick mode is neither PICK_BOUNDS
     * nor PICK_GEOMETRY.
     *
     * @exception IllegalArgumentException if pick mode is PICK_BOUNDS
     * and flags includes any of CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE,
     * CLOSEST_GEOM_INFO or ALL_GEOM_INFO.
     *
     * @exception IllegalArgumentException if pickShape is PickBounds
     * and flags includes any of CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE,
     * CLOSEST_GEOM_INFO or ALL_GEOM_INFO.
     *
     * @exception IllegalStateException if BranchGroup is not live.
     *
     * @exception CapabilityNotSetException if the mode is
     * PICK_GEOMETRY and the Geometry.ALLOW_INTERSECT capability bit
     * is not set in any Geometry objects referred to by any shape
     * node whose bounds intersects the PickShape.
     *
     * @exception CapabilityNotSetException if flags contains any of
     * CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE, CLOSEST_GEOM_INFO
     * or ALL_GEOM_INFO, and the capability bits that control reading of
     * coordinate data are not set in any GeometryArray object referred
     * to by any shape node that intersects the PickShape.
     * The capability bits that must be set to avoid this exception are as follows :
     * <ul>
     * <li>By-copy geometry : GeometryArray.ALLOW_COORDINATE_READ</li>
     * <li>By-reference geometry : GeometryArray.ALLOW_REF_DATA_READ</li>
     * <li>Indexed geometry : IndexedGeometryArray.ALLOW_COORDINATE_INDEX_READ
     * (in addition to one of the above)</li>
     * </ul>
     *
     * @see Locale#pickAny(int,int,javax.media.j3d.PickShape)
     * @see PickInfo
     *
     * @since Java 3D 1.4
     *
     */
    public PickInfo pickAny( int mode, int flags, PickShape pickShape ) {

        validateModeFlagAndPickShape(mode, flags, pickShape);
        return ((BranchGroupRetained)this.retained).pickAny(mode, flags, pickShape);

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
    @Override
    public Node cloneNode(boolean forceDuplicate) {

        BranchGroup bg = new BranchGroup();
	bg.duplicateNode(this, forceDuplicate);
	return bg;

    }

}
