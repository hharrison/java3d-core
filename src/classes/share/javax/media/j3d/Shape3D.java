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

import java.util.Enumeration;


/**
 * The Shape3D leaf node specifies all geometric objects.  It contains
 * a list of one or more Geometry component objects and a single
 * Appearance component object.  The geometry objects define the shape
 * node's geometric data.  The appearance object specifies that
 * object's appearance attributes, including color, material, texture,
 * and so on.
 * <p>
 * The list of geometry objects must all be of the same equivalence
 * class, that is, the same basic type of primitive.  For subclasses
 * of GeometryArray, all point objects are equivalent, all line
 * objects are equivalent, and all polygon objects are equivalent.
 * For other subclasses of Geometry, only objects of the same
 * subclass are equivalent.  The equivalence classes are as follows:
 * <ul>
 * <li>GeometryArray (point): [Indexed]PointArray</li>
 * <li>GeometryArray (line): [Indexed]{LineArray, LineStripArray}</li>
 * <li>GeometryArray (polygon): [Indexed]{TriangleArray, TriangleStripArray,
 * TriangleFanArray, QuadArray}</li>
 * <li>CompressedGeometry</li>
 * <li>Raster</li>
 * <li>Text3D</li>
 * </ul>
 * <p>
 * When Shape3D is used with multiple geometry components, Java 3D may
 * choose to use individual geometry bounds instead of the shape's
 * bounds for region of influence operations, such as lighting.
 * For example, the individual characters of a Text3D shape object
 * may be rendered with a different light set.
 */

public class Shape3D extends Leaf {

    /**
     * Id used in the compile optimization to determine
     * how to get to the geometry in the case of read
     * or picking ..
     */
    int id;

    /**
     * Specifies that the node allows read access to its geometry information.
     */
    public static final int
    ALLOW_GEOMETRY_READ = CapabilityBits.SHAPE3D_ALLOW_GEOMETRY_READ;

    /**
     * Specifies that the node allows write access to its geometry information.
     */
    public static final int
    ALLOW_GEOMETRY_WRITE = CapabilityBits.SHAPE3D_ALLOW_GEOMETRY_WRITE;

    /**
     * Specifies that the node allows read access to its appearance
     * information.
     */
    public static final int
    ALLOW_APPEARANCE_READ = CapabilityBits.SHAPE3D_ALLOW_APPEARANCE_READ;

    /**
     * Specifies that the node allows write access to its appearance
     * information.
     */
    public static final int
    ALLOW_APPEARANCE_WRITE = CapabilityBits.SHAPE3D_ALLOW_APPEARANCE_WRITE;

    /**
     * Specifies that the node allows reading its collision Bounds
     */
    public static final int
    ALLOW_COLLISION_BOUNDS_READ = CapabilityBits.SHAPE3D_ALLOW_COLLISION_BOUNDS_READ;

    /**
     * Specifies the node allows writing its collision Bounds
     */
    public static final int
    ALLOW_COLLISION_BOUNDS_WRITE = CapabilityBits.SHAPE3D_ALLOW_COLLISION_BOUNDS_WRITE;

    /**
     * Specifies that this node allows reading its appearance override
     * enable flag.
     *
     * @since Java 3D 1.2
     */
    public static final int ALLOW_APPEARANCE_OVERRIDE_READ =
	CapabilityBits.SHAPE3D_ALLOW_APPEARANCE_OVERRIDE_READ;

    /**
     * Specifies that this node allows writing its appearance override
     * enable flag.
     *
     * @since Java 3D 1.2
     */
    public static final int ALLOW_APPEARANCE_OVERRIDE_WRITE =
	CapabilityBits.SHAPE3D_ALLOW_APPEARANCE_OVERRIDE_WRITE;

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_GEOMETRY_READ,
        ALLOW_APPEARANCE_READ,
        ALLOW_COLLISION_BOUNDS_READ,
        ALLOW_APPEARANCE_OVERRIDE_READ
    };

    /**
     * Constructs a Shape3D node with default parameters.  The default
     * values are as follows:
     * <ul>
     * appearance : null<br>
     * geometry : { null }<br>
     * collision bounds : null<br>
     * appearance override enable : false<br>
     * </ul>
     * The list of geometry components is initialized with a null
     * geometry component as the single element with an index of 0.
     * A null geometry component specifies
     * that no geometry is drawn. A null appearance component specifies
     * that default values are used for all appearance attributes.
     */
    public Shape3D() {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Constructs and initializes a Shape3D node with the specified
     * geometry component and a null appearance component.
     * The list of geometry components is initialized with the
     * specified geometry component as the single element with an
     * index of 0.
     * A null appearance component specifies that default values are
     * used for all appearance attributes.
     * @param geometry the geometry component with which to initialize
     * this shape node.
     */
    public Shape3D(Geometry geometry) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((Shape3DRetained)retained).setGeometry(geometry, 0);
    }

    /**
     * Constructs and initializes a Shape3D node with the specified
     * geometry and appearance components.
     * The list of geometry components is initialized with the
     * specified geometry component as the single element with an
     * index of 0.
     * @param geometry the geometry component with which to initialize
     * this shape node
     * @param appearance the appearance component of the shape node
     */
    public Shape3D(Geometry geometry, Appearance appearance) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((Shape3DRetained)retained).setGeometry(geometry, 0);
	((Shape3DRetained)retained).setAppearance(appearance);
    }

    /**
     * Creates the retained mode Shape3DRetained object that this
     * Shape3D object will point to.
     */
    void createRetained() {
	retained = new Shape3DRetained();
	retained.setSource(this);
    }

    /**
      * Sets the collision bounds of a node.
      * @param bounds the collision bounding object for a node
      * @exception CapabilityNotSetException if appropriate capability is
      * not set and this object is part of live or compiled scene graph
      */
    public void setCollisionBounds(Bounds bounds) {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_COLLISION_BOUNDS_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Shape3D0"));

	((Shape3DRetained)this.retained).setCollisionBounds(bounds);
    }

    /**
      * Returns the collision bounding object of this node.
      * @return the node's collision bounding object
      * @exception CapabilityNotSetException if appropriate capability is
      * not set and this object is part of live or compiled scene graph
      */
    public Bounds getCollisionBounds() {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_COLLISION_BOUNDS_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Shape3D1"));

	return ((Shape3DRetained)this.retained).getCollisionBounds(id);
    }


    /**
     * Replaces the geometry component at index 0 in this Shape3D node's
     * list of geometry components with the specified geometry component.
     * If there are existing geometry components in the list (besides
     * the one being replaced), the new geometry component must be of
     * the same equivalence class (point, line, polygon, CompressedGeometry,
     * Raster, Text3D) as the others.
     * @param geometry the geometry component to be stored at index 0.
     * @exception IllegalArgumentException if the new geometry
     * component is not of of the same equivalence class as the
     * existing geometry components.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setGeometry(Geometry geometry) {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_GEOMETRY_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Shape3D2"));

	((Shape3DRetained)retained).setGeometry(geometry, 0);
    }

    /**
     * Retrieves the geometry component at index 0 from this Shape3D node's
     * list of geometry components.
     * @return the geometry component at index 0.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public Geometry getGeometry() {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_GEOMETRY_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Shape3D3"));

	return ((Shape3DRetained)retained).getGeometry(0, id);
    }


    /**
     * Replaces the geometry component at the specified index in this
     * Shape3D node's list of geometry components with the specified
     * geometry component.
     * If there are existing geometry components in the list (besides
     * the one being replaced), the new geometry component must be of
     * the same equivalence class (point, line, polygon, CompressedGeometry,
     * Raster, Text3D) as the others.
     * @param geometry the geometry component to be stored at the
     * specified index.
     * @param index the index of the geometry component to be replaced.
     * @exception IllegalArgumentException if the new geometry
     * component is not of of the same equivalence class as the
     * existing geometry components.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public void setGeometry(Geometry geometry, int index) {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_GEOMETRY_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Shape3D2"));

	((Shape3DRetained)retained).setGeometry(geometry, index);
    }


    /**
     * Retrieves the geometry component at the specified index from
     * this Shape3D node's list of geometry components.
     * @param index the index of the geometry component to be returned.
     * @return the geometry component at the specified index.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public Geometry getGeometry(int index) {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_GEOMETRY_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Shape3D3"));

	return ((Shape3DRetained)retained).getGeometry(index, id);
    }


    /**
     * Inserts the specified geometry component into this Shape3D
     * node's list of geometry components at the specified index.
     * If there are existing geometry components in the list, the new
     * geometry component must be of the same equivalence class
     * (point, line, polygon, CompressedGeometry, Raster, Text3D) as
     * the others.
     * @param geometry the geometry component to be inserted at the
     * specified index.
     * @param index the index at which the geometry component is inserted.
     * @exception IllegalArgumentException if the new geometry
     * component is not of of the same equivalence class as the
     * existing geometry components.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public void insertGeometry(Geometry geometry, int index) {

	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_GEOMETRY_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Shape3D2"));

	((Shape3DRetained)retained).insertGeometry(geometry, index);
    }


    /**
     * Removes the geometry component at the specified index from
     * this Shape3D node's list of geometry components.
     * @param index the index of the geometry component to be removed.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public void removeGeometry(int index) {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_GEOMETRY_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Shape3D2"));

	((Shape3DRetained)retained).removeGeometry(index);
    }


    /**
     * Returns an enumeration of this Shape3D node's list of geometry
     * components.
     * @return an Enumeration object containing all geometry components in
     * this Shape3D node's list of geometry components.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public Enumeration getAllGeometries() {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_GEOMETRY_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Shape3D3"));

	return ((Shape3DRetained)retained).getAllGeometries(id);
    }


    /**
     * Appends the specified geometry component to this Shape3D
     * node's list of geometry components.
     * If there are existing geometry components in the list, the new
     * geometry component must be of the same equivalence class
     * (point, line, polygon, CompressedGeometry, Raster, Text3D) as
     * the others.
     * @param geometry the geometry component to be appended.
     * @exception IllegalArgumentException if the new geometry
     * component is not of of the same equivalence class as the
     * existing geometry components.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public void addGeometry(Geometry geometry) {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_GEOMETRY_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Shape3D2"));

	((Shape3DRetained)retained).addGeometry(geometry);
    }


    /**
     * Returns the number of geometry components in this Shape3D node's
     * list of geometry components.
     * @return the number of geometry components in this Shape3D node's
     * list of geometry components.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public int numGeometries() {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_GEOMETRY_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Shape3D3"));
	return ((Shape3DRetained)retained).numGeometries(id);
    }


    /**
     * Retrieves the index of the specified geometry component in
     * this Shape3D node's list of geometry components.
     *
     * @param geometry the geometry component to be looked up.
     * @return the index of the specified geometry component;
     * returns -1 if the object is not in the list.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public int indexOfGeometry(Geometry geometry) {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_GEOMETRY_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Shape3D3"));
	return ((Shape3DRetained)retained).indexOfGeometry(geometry);
    }


    /**
     * Removes the specified geometry component from this
     * Shape3D node's list of geometry components.
     * If the specified object is not in the list, the list is not modified.
     *
     * @param geometry the geometry component to be removed.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public void removeGeometry(Geometry geometry) {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_GEOMETRY_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Shape3D2"));
	((Shape3DRetained)retained).removeGeometry(geometry);
    }


    /**
     * Removes all geometry components from this Shape3D node.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public void removeAllGeometries() {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_GEOMETRY_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Shape3D2"));
	((Shape3DRetained)retained).removeAllGeometries();
    }


    /**
     * Sets the appearance component of this Shape3D node.  Setting it to null
     * specifies that default values are used for all appearance attributes.
     * @param appearance the new appearance component for this shape node
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setAppearance(Appearance appearance) {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_APPEARANCE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Shape3D4"));

	((Shape3DRetained)this.retained).setAppearance(appearance);
    }

    /**
     * Retrieves the appearance component of this shape node.
     * @return the appearance component of this shape node
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
     public Appearance getAppearance() {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_APPEARANCE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Shape3D5"));

	return ((Shape3DRetained)this.retained).getAppearance();
    }


    /**
     * Checks whether the geometry in this shape node intersects with
     * the specified pickShape.
     *
     * @param path the SceneGraphPath to this shape node
     * @param pickShape the PickShape to be intersected
     *
     * @return true if the pick shape intersects this node; false
     * otherwise.
     *
     * @exception IllegalArgumentException if pickShape is a PickPoint.
     * Java 3D doesn't have spatial information of the surface.
     * Use PickBounds with BoundingSphere and a small radius, instead.
     *
     * @exception CapabilityNotSetException if the Geometry.ALLOW_INTERSECT
     * capability bit is not set in all of the Geometry objects
     * referred to by this shape node.
     */
    public boolean intersect(SceneGraphPath path, PickShape pickShape) {
	return intersect(path, pickShape, null);
    }


    /**
     * Checks whether the geometry in this shape node intersects with
     * the specified pickRay.
     *
     * @param path the SceneGraphPath to this shape node
     * @param pickRay the PickRay to be intersected
     * @param dist the closest distance of the intersection
     *
     * @return true if the pick shape intersects this node; false
     * otherwise.  If true, dist contains the closest distance of
     * intersection.
     *
     * @exception CapabilityNotSetException if the Geometry.ALLOW_INTERSECT
     * capability bit is not set in all of the Geometry objects
     * referred to by this shape node.
     */
    public boolean intersect(SceneGraphPath path,
			     PickRay pickRay,
			     double[] dist) {

	if (isLiveOrCompiled()) {
	    if (!((Shape3DRetained)retained).allowIntersect())
		throw new CapabilityNotSetException(J3dI18N.getString("Shape3D6"));
	}
	return ((Shape3DRetained)this.retained).intersect(path, pickRay, dist);

    }

    /**
     * Checks whether the geometry in this shape node intersects with
     * the specified pickShape.
     *
     * @param path the SceneGraphPath to this shape node
     * @param pickShape the PickShape to be intersected
     * @param dist the closest distance of the intersection
     *
     * @return true if the pick shape intersects this node; false
     * otherwise.  If true, dist contains the closest distance of
     * intersection.
     *
     * @exception IllegalArgumentException if pickShape is a PickPoint.
     * Java 3D doesn't have spatial information of the surface.
     * Use PickBounds with BoundingSphere and a small radius, instead.
     *
     * @exception CapabilityNotSetException if the Geometry.ALLOW_INTERSECT
     * capability bit is not set in all of the Geometry objects
     * referred to by this shape node.
     *
     * @since Java 3D 1.3
     */
    public boolean intersect(SceneGraphPath path,
			     PickShape pickShape,
			     double[] dist) {

	if (isLiveOrCompiled()) {
	    if (!((Shape3DRetained)retained).allowIntersect())
		throw new CapabilityNotSetException(J3dI18N.getString("Shape3D6"));
	}

	if (pickShape instanceof PickPoint) {
	    throw new IllegalArgumentException(J3dI18N.getString("Shape3D7"));
	}

	return ((Shape3DRetained)this.retained).intersect(path, pickShape, dist);
    }


    /**
     * Sets a flag that indicates whether this node's appearance can
     * be overridden.  If the flag is true, then this node's
     * appearance may be overridden by an AlternateAppearance leaf
     * node, regardless of the value of the ALLOW_APPEARANCE_WRITE
     * capability bit.
     * The default value is false.
     *
     * @param flag the apperance override enable flag.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @see AlternateAppearance
     *
     * @since Java 3D 1.2
     */
    public void setAppearanceOverrideEnable(boolean flag) {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_APPEARANCE_OVERRIDE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Shape3D8"));

	((Shape3DRetained)this.retained).setAppearanceOverrideEnable(flag);
    }

    /**
     * Retrieves the appearanceOverrideEnable flag for this node.
     * @return true if the appearance can be overridden; false
     * otherwise.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public boolean getAppearanceOverrideEnable() {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_APPEARANCE_OVERRIDE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Shape3D9"));

	return ((Shape3DRetained)this.retained).getAppearanceOverrideEnable();
    }

    /**
     * Used to create a new instance of the node.  This routine is called
     * by <code>cloneTree</code> to duplicate the current node.
     * <code>cloneNode</code> should be overridden by any user subclassed
     * objects.  All subclasses must have their <code>cloneNode</code>
     * method consist of the following lines:
     * <P><blockquote><pre>
     *     public Node cloneNode(boolean forceDuplicate) {
     *         UserSubClass usc = new UserSubClass();
     *         usc.duplicateNode(this, forceDuplicate);
     *         return usc;
     *     }
     * </pre></blockquote>
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @see Node#cloneTree
     * @see Node#duplicateNode
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public Node cloneNode(boolean forceDuplicate) {
        Shape3D s = new Shape3D();
        s.duplicateNode(this, forceDuplicate);
        return s;
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
     *  <code>Shape3D</code>
     *
     * @see Node#cloneTree
     * @see Node#cloneNode
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public void duplicateNode(Node originalNode, boolean forceDuplicate) {
	checkDuplicateNode(originalNode, forceDuplicate);
    }



   /**
     * Copies all Shape3D information from
     * <code>originalNode</code> into
     * the current node.  This method is called from the
     * <code>cloneNode</code> method which is, in turn, called by the
     * <code>cloneTree</code> method.<P>
     *
     * @param originalNode the original node to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @exception RestrictedAccessException if this object is part of a live
     *  or compiled scenegraph.
     *
     * @see Node#duplicateNode
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    void duplicateAttributes(Node originalNode, boolean forceDuplicate) {

	super.duplicateAttributes(originalNode, forceDuplicate);

	Shape3DRetained attr = (Shape3DRetained) originalNode.retained;
	Shape3DRetained rt = (Shape3DRetained) retained;

	rt.setAppearance((Appearance) getNodeComponent(
						       attr.getAppearance(),
						       forceDuplicate,
						       originalNode.nodeHashtable));
	int num = attr.numGeometries(id);
	if (num > 0) {
	    rt.setGeometry((Geometry) getNodeComponent(
					       attr.getGeometry(0, id),
					       forceDuplicate,
					       originalNode.nodeHashtable), 0);
	    for(int i=1; i< num; i++) {
		rt.addGeometry((Geometry) getNodeComponent(
						   attr.getGeometry(i, id),
						   forceDuplicate,
						   originalNode.nodeHashtable));
	    }
	}

	rt.setCollisionBounds(attr.getCollisionBounds(id));
    }

    /**
     * See parent class for the documentation on getBounds().
     */
    public Bounds getBounds() {
	if (isLiveOrCompiled()) {
	    if(!this.getCapability(ALLOW_BOUNDS_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("Node2"));
	    }
	} else {
	    // this will throw a SceneGraphCycleException if there is
	    // a cycle
	    checkForCycle();
	}

	return ((Shape3DRetained)this.retained).getBounds();
    }


}
