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

import java.util.Hashtable;
import javax.vecmath.*;

/**
 * The Morph leaf node permits an application to morph between
 * multiple GeometryArrays.  The Morph node contains a single
 * Appearance node, an array of GeometryArray objects, and an array of
 * corresponding weights. The Morph node combines these GeometryArrays
 * into an aggregate shape based on each GeometryArray's corresponding
 * weight. Typically, Behavior nodes will modify the weights to
 * achieve various morphing effects.
 *
 * <p>
 * The following restrictions apply to each GeometryArray object
 * in the specified array of GeometryArray objects:
 *
 * <ul>
 * <li>
 * All <i>N</i> geometry arrays must be of the
 * same type (that is, the same subclass of GeometryArray).
 * </li>
 *
 * <p>
 * <li>
 * The vertexFormat, texCoordSetCount, and validVertexCount must be
 * the same for all <i>N</i> geometry arrays.
 * </li>
 *
 * <p>
 * <li>
 * The texCoordSetMap array must be identical (element-by-element) for
 * all <i>N</i> geometry arrays.
 * </li>
 *
 * <p>
 * <li>
 * For IndexedGeometryArray objects, the validIndexCount must be the same
 * for all <i>N</i> geometry arrays.
 * </li>
 *
 * <p>
 * <li>
 * For GeometryStripArray objects, the stripVertexCounts array must
 * be identical (element-by-element) for all <i>N</i> geometry arrays.
 * </li>
 *
 * <p>
 * <li>
 * For IndexedGeometryStripArray objects, the stripIndexCounts array must
 * be identical (element-by-element) for all <i>N</i> geometry arrays.
 * </li>
 *
 * <p>
 * <li>
 * For indexed geometry by-reference, the array lengths of each
 * enabled vertex component (coord, color, normal, texcoord)
 * must be the same for all <i>N</i> geometry arrays.
 * </li>
 * </ul>
 *
 * <p>
 * For IndexedGeometryArray objects, the vertex arrays are morphed
 * <i>before</i> the indexes are applied.  Only the indexes in the
 * first geometry array (geometry[0]) are used when rendering the
 * geometry.
 *
 * @deprecated As of Java 3D version 1.4.
 */

public class Morph extends Leaf {

    /**
     * Specifies that the node allows read access to its geometry information.
     */
    public static final int
    ALLOW_GEOMETRY_ARRAY_READ = CapabilityBits.MORPH_ALLOW_GEOMETRY_ARRAY_READ;

    /**
     * Specifies that the node allows write access to its geometry information.
     */
    public static final int
    ALLOW_GEOMETRY_ARRAY_WRITE = CapabilityBits.MORPH_ALLOW_GEOMETRY_ARRAY_WRITE;

    /**
     * Specifies that the node allows read access to its appearance information.
     */
    public static final int
    ALLOW_APPEARANCE_READ = CapabilityBits.MORPH_ALLOW_APPEARANCE_READ;

    /**
     * Specifies that the node allows write access to its appearance information.
     */
    public static final int
    ALLOW_APPEARANCE_WRITE = CapabilityBits.MORPH_ALLOW_APPEARANCE_WRITE;

    /**
     * Specifies that the node allows read access to its morph
     * weight vector.
     */
    public static final int
    ALLOW_WEIGHTS_READ = CapabilityBits.MORPH_ALLOW_WEIGHTS_READ;

    /**
     * Specifies that the node allows write access to its morph
     * weight vector.
     */
    public static final int
    ALLOW_WEIGHTS_WRITE = CapabilityBits.MORPH_ALLOW_WEIGHTS_WRITE;

    /**
     * Specifies that the node allows reading its collision Bounds.
     */
    public static final int
    ALLOW_COLLISION_BOUNDS_READ = CapabilityBits.MORPH_ALLOW_COLLISION_BOUNDS_READ;

    /**
     * Specifies the node allows writing its collision Bounds.
     */
    public static final int
    ALLOW_COLLISION_BOUNDS_WRITE = CapabilityBits.MORPH_ALLOW_COLLISION_BOUNDS_WRITE;

    /**
     * Specifies that this node allows reading its appearance override
     * enable flag.
     *
     * @since Java 3D 1.2
     */
    public static final int ALLOW_APPEARANCE_OVERRIDE_READ =
	CapabilityBits.MORPH_ALLOW_APPEARANCE_OVERRIDE_READ;

    /**
     * Specifies that this node allows writing its appearance override
     * enable flag.
     *
     * @since Java 3D 1.2
     */
    public static final int ALLOW_APPEARANCE_OVERRIDE_WRITE =
	CapabilityBits.MORPH_ALLOW_APPEARANCE_OVERRIDE_WRITE;

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_GEOMETRY_ARRAY_READ,
        ALLOW_APPEARANCE_READ,
        ALLOW_WEIGHTS_READ,        
        ALLOW_COLLISION_BOUNDS_READ,
        ALLOW_APPEARANCE_OVERRIDE_READ
    };
                
    // non public default constructor
    Morph() {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);        
    }

    /**
     * Constructs and initializes a Morph node with the specified array
     * of GeometryArray objects.  Default values are used for all other
     * parameters as follows:
     * <ul>
     * appearance : null<br>
     * weights : [1, 0, 0, 0, ...]<br>
     * collision bounds : null<br>
     * appearance override enable : false<br>
     * </ul><P>
     * A null appearance object specifies that default values are used
     * for all appearance attributes.
     *
     * @param geometryArrays the geometry components of the morph;
     * a null or zero-length array of GeometryArray objects is
     * permitted, and specifies that no geometry is drawn.  In this case,
     * the array of weights is initialized to a zero-length array.
     *
     * @exception IllegalArgumentException if any of the specified
     * geometry array objects differ from each other in any of the
     * following ways:
     * <ul>
     * <li>Type of geometry array object (subclass of GeometryArray)</li>
     * <li>vertexFormat</li>
     * <li>texCoordSetCount</li>
     * <li>texCoordSetMap</li>
     * <li>validVertexCount</li>
     * <li>validIndexCount, for IndexedGeometryArray objects</li>
     * <li>stripVertexCounts array, for GeometryStripArray objects</li>
     * <li>stripIndexCounts array, for IndexedGeometryStripArray objects</li>
     * <li>the array lengths of each enabled vertex component
     * (coord, color, normal, texcoord),
     * for indexed geometry by-reference</li>
     * </ul>
     *
     * @exception UnsupportedOperationException if the specified
     * geometry arrays contain vertex attributes (that is, if their
     * vertexFormat includes the <code>VERTEX_ATTRIBUTES</code> flag).
     */
    public Morph(GeometryArray geometryArrays[]) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
        
	((MorphRetained)retained).setGeometryArrays(geometryArrays);
    }

    /**
     * Constructs and initializes a Morph node with the specified array
     * of GeometryArray objects and the specified appearance object.
     *
     * @param geometryArrays the geometry components of the Morph node
     * a null or zero-length array of GeometryArray objects is
     * permitted, and specifies that no geometry is drawn.  In this case,
     * the array of weights is initialized to a zero-length array.
     * @param appearance the appearance component of the Morph node
     *
     * @exception IllegalArgumentException if any of the specified
     * geometry array objects differ from each other in any of the
     * following ways:
     * <ul>
     * <li>Type of geometry array object (subclass of GeometryArray)</li>
     * <li>vertexFormat</li>
     * <li>texCoordSetCount</li>
     * <li>texCoordSetMap</li>
     * <li>validVertexCount</li>
     * <li>validIndexCount, for IndexedGeometryArray objects</li>
     * <li>stripVertexCounts array, for GeometryStripArray objects</li>
     * <li>stripIndexCounts array, for IndexedGeometryStripArray objects</li>
     * <li>the array lengths of each enabled vertex component
     * (coord, color, normal, texcoord),
     * for indexed geometry by-reference</li>
     * </ul>
     *
     * @exception UnsupportedOperationException if the specified
     * geometry arrays contain vertex attributes (that is, if their
     * vertexFormat includes the <code>VERTEX_ATTRIBUTES</code> flag).
     */
    public Morph(GeometryArray geometryArrays[], Appearance appearance) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((MorphRetained)retained).setGeometryArrays(geometryArrays);
	((MorphRetained)this.retained).setAppearance(appearance);
    }

    /**
     * Creates the retained mode MorphRetained object that this
     * Morph object will point to.
     */
    void createRetained() {
	retained = new MorphRetained();
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
	    if(!this.getCapability(ALLOW_COLLISION_BOUNDS_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Morph0"));
	
	((MorphRetained)this.retained).setCollisionBounds(bounds);
    }

    /**
     * Returns the collision bounding object of this node.
     * @return the node's collision bounding object
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public Bounds getCollisionBounds() {

	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COLLISION_BOUNDS_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Morph1"));
	
	return ((MorphRetained)this.retained).getCollisionBounds();
    }

 
    /**
     * Sets the geometryArrays component of the Morph node.
     *
     * If the current array of GeometryArrays in this Morph object is
     * non-null with a length greater than 0, the specified array of
     * GeometryArrays must be the same length as the current array.
     * If the current array of GeometryArrays in this Morph object is
     * null or has a length of 0, and the specified array of
     * GeometryArrays is non-null with a length greater than 0, the
     * length of the incoming array defines the number of the geometry
     * objects that will be morphed.  In this case, the weights array
     * is allocated to be of the same length as the geometry array;
     * the first element (weights[0]) is initialized to 1.0 and all of
     * the other weights are initialized to 0.0.
     *
     * @param geometryArrays the new geometryArrays component
     * for the Morph node.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * <p>
     *
     * @exception IllegalArgumentException if the length of the
     * specified array of geometry arrays is not equal to the length
     * of this Morph node's current array of geometry arrays (and the
     * current array's length is non-zero), or if any of the specified
     * geometry array objects differ from each other in any of the
     * following ways:
     * <ul>
     * <li>Type of geometry array object (subclass of GeometryArray)</li>
     * <li>vertexFormat</li>
     * <li>texCoordSetCount</li>
     * <li>texCoordSetMap</li>
     * <li>validVertexCount</li>
     * <li>validIndexCount, for IndexedGeometryArray objects</li>
     * <li>stripVertexCounts array, for GeometryStripArray objects</li>
     * <li>stripIndexCounts array, for IndexedGeometryStripArray objects</li>
     * <li>the array lengths of each enabled vertex component
     * (coord, color, normal, texcoord),
     * for indexed geometry by-reference</li>
     * </ul>
     *
     * @exception UnsupportedOperationException if the specified
     * geometry arrays contain vertex attributes (that is, if their
     * vertexFormat includes the <code>VERTEX_ATTRIBUTES</code> flag).
     */
    public void setGeometryArrays(GeometryArray geometryArrays[]) {

	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_GEOMETRY_ARRAY_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Morph2"));
	
	((MorphRetained)this.retained).setGeometryArrays(geometryArrays);
    }

    /**
     * Retrieves the geometryArray component of this Morph node.
     * @param index the index of GeometryArray to be returned
     * @return the geometryArray component of this Morph node
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public GeometryArray getGeometryArray(int index) {

	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_GEOMETRY_ARRAY_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Morph3"));
	
	return ((MorphRetained)this.retained).getGeometryArray(index);
    }

    /**
     * Sets the appearance component of this Morph node.  A null
     * appearance component specifies that default values are used for all
     * appearance attributes.
     * @param appearance the new appearance component for this Morph node
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setAppearance(Appearance appearance) {

	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_APPEARANCE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Morph4"));
	
	((MorphRetained)this.retained).setAppearance(appearance);
    }

    /**
     * Retrieves the appearance component of this morph node.
     * @return the appearance component of this morph node
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public Appearance getAppearance() {

	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_APPEARANCE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Morph5"));
	
	return ((MorphRetained)this.retained).getAppearance();
    }


    /**
     * Checks whether the geometry in this morph node intersects with
     * the specified pickShape.
     *
     * @param path the SceneGraphPath to this morph node
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
     * referred to by this morph node.
     */
    public boolean intersect(SceneGraphPath path, PickShape pickShape) {
	return intersect(path, pickShape, null);
    }


    /**
     * Checks whether the geometry in this morph node intersects with
     * the specified pickRay.
     *
     * @param path the SceneGraphPath to this morph node
     * @param pickRay the PickRay to be intersected
     * @param dist the closest distance of the intersection
     *
     * @return true if the pick shape intersects this node; false
     * otherwise.  If true, dist contains the closest distance of
     * intersection.
     *
     * @exception CapabilityNotSetException if the Geometry.ALLOW_INTERSECT
     * capability bit is not set in all of the Geometry objects
     * referred to by this morph node.
     */
    public boolean intersect(SceneGraphPath path,
			     PickRay pickRay,
			     double[] dist) {

	if (isLiveOrCompiled()) {
	    checkForAllowIntersect();
	}

	return ((MorphRetained)this.retained).intersect(path, pickRay, dist);
    }


    /**
     * Checks whether the geometry in this morph node intersects with
     * the specified pickShape.
     *
     * @param path the SceneGraphPath to this morph node
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
     * referred to by this morph node.
     *
     * @since Java 3D 1.3
     */
    public boolean intersect(SceneGraphPath path,
			     PickShape pickShape,
			     double[] dist) {

	if (isLiveOrCompiled()) {
	    checkForAllowIntersect();
	}

	if (pickShape instanceof PickPoint) {
	    throw new IllegalArgumentException(J3dI18N.getString("Morph10"));
	}

	return ((MorphRetained)this.retained).intersect(path, pickShape, dist);
    }


    /**
     * Sets this Morph node's morph weight vector. The Morph node "weights"
     * the corresponding GeometryArray by the amount specified. 
     * The weights apply a morph weight vector component that creates
     * the desired morphing effect.
     * The length
     * of the <code>weights</code> parameter must be equal to the length
     * of the array with which this Morph node was created, otherwise
     * an IllegalArgumentException is thown.
     * @param weights the morph weight vector that the morph node will
     * use in combining the node's geometryArrays.  The morph node will "weight"
     * the corresponding GeometryArray by the amount specified.
     *  N.B.: the sum of the weights should equal 1.0
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception IllegalArgumentException if sum of all 'weights' is 
     * NOT 1.0 or number of weights is NOT exqual to number of GeometryArrays.
     */
    public void setWeights(double weights[]) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_WEIGHTS_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Morph8"));
	
	((MorphRetained)this.retained).setWeights(weights);
    }

    /**
     * Retrieves the Morph node's morph weight vector.
     * @return the morph weight vector component of this morph node
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public double[] getWeights() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_WEIGHTS_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Morph9"));
	
	return ((MorphRetained)this.retained).getWeights();
    }
    
    /**
     * Sets a flag that indicates whether this node's appearance can
     * be overridden.  If the flag is true, this node's
     * appearance may be overridden by an AlternateAppearance leaf
     * node, regardless of the value of the ALLOW_APPEARANCE_WRITE
     * capability bit.
     * The default value is false.
     *
     * @param flag the apperance override enable flag
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
		throw new CapabilityNotSetException(J3dI18N.getString("Morph11"));

	((MorphRetained)this.retained).setAppearanceOverrideEnable(flag);
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
		throw new CapabilityNotSetException(J3dI18N.getString("Morph12"));

	return ((MorphRetained)this.retained).getAppearanceOverrideEnable();
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
        Morph m = new Morph();
        m.duplicateNode(this, forceDuplicate);
        return m;
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
     *  <code>Morph</code>
     *
     * @see Node#cloneTree
     * @see Node#cloneNode
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public void duplicateNode(Node originalNode, boolean forceDuplicate) {
	checkDuplicateNode(originalNode, forceDuplicate);
    }

   /**
     * Copies all Morph information from
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
	
	MorphRetained attr = (MorphRetained) originalNode.retained;
	MorphRetained rt = (MorphRetained) retained;

	Hashtable hashtable = originalNode.nodeHashtable;

	double weights[] = attr.getWeights();

	rt.setCollisionBounds(attr.getCollisionBounds());
	rt.setAppearance((Appearance) getNodeComponent(
				       attr.getAppearance(),
				       forceDuplicate,
				       hashtable));

	GeometryArray ga[] = new GeometryArray[weights.length];

	for (int i=weights.length-1; i>=0; i--) {
	    ga[i] = (GeometryArray) getNodeComponent(
					     attr.getGeometryArray(i),
					     forceDuplicate,
					     hashtable);
	}
	rt.setGeometryArrays(ga);
	rt.setWeights(weights);
    }
   
    // Method to check whether all geometries have allow intersect
    // capability bit set; it will throw an exception if any don't
    // have the bit set.
    private void checkForAllowIntersect() {
	MorphRetained morphR = ((MorphRetained)this.retained);
	int numGeometryArrays = morphR.getNumGeometryArrays();
	for (int i = 0; i < numGeometryArrays; i++) {
	    if (!morphR.geometryArrays[i].source.
		getCapability(Geometry.ALLOW_INTERSECT)) {

		throw new CapabilityNotSetException(J3dI18N.getString("Morph6"));
	    }
	}
    }

}
