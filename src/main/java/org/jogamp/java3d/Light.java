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

package org.jogamp.java3d;

import java.util.Enumeration;

import org.jogamp.vecmath.Color3f;

/**
 * The Light leaf node is an abstract class that defines a set of
 * parameters common to all
 * types of light.  These parameters include the light color, an enable
 * flag, and a region of influence in which this Light node is active.
 * A Light node also contains a list of Group nodes that specifies the
 * hierarchical scope of this Light.  If the scope list is empty,
 * the Light node has universe scope: all nodes within the region of
 * influence are affected by this Light node.  If the scope list is
 * non-empty, only those Leaf nodes under the Group nodes in the
 * scope list are affected by this Light node (subject to the
 * influencing bounds).
 * <p>
 * The light in a scene may come from several light sources that can
 * be individually defined. Some of the light in a scene may
 * come from a specific direction, known as a directional light,
 * from a specific position, known as a point light, or
 * from no particular direction or source as with ambient light.
 * <p>
 * Java 3D supports an arbitrary number of lights. However, the number
 * of lights that can be active within the region of influence is
 * implementation-dependent and cannot be defined here.
 * <p>
 * <b>Light Color</b>
 * <p>
 * The Java 3D lighting model approximates the way light works in
 * the real world. Light is defined in terms of the red, green, and
 * blue components that combine to create the light color. The
 * three color components represent the amount of light emitted
 * by the source.
 * <p>
 * Each of the three colors is represented by a
 * floating point value that ranges from 0.0 to 1.0. A combination
 * of the three colors such as (1.0, 1.0, 1.0), representing
 * the red, green, and blue color values respectively, creates a white
 * light with maximum brightness. A combination such as (0.0, 0.0,
 * 0.0) creates no light (black). Values between the minimum and
 * maximum values of the range produce corresponding brightness
 * and colors. For example, a combination of (0.5, 0.5, 0.5)
 * produces a 50% grey light. A combination of (1.0, 1.0, 0.0),
 * red and green but no blue, produces a yellow light.
 * <p>
 * If a scene has multiple lights and all lights illuminate an object,
 * the effect of the light on the object is the sum of the
 * lights. For example, in a scene with two lights, if the first
 * light emits (R<sub>1</sub>, G<sub>1</sub>, B<sub>1</sub>) and
 * the second light emits (R<sub>2</sub>, G<sub>2</sub>,
 * B<sub>2</sub>), the components are added together giving
 * (R<sub>1</sub>+R<sub>2</sub>, G<sub>1</sub>+G<sub>2</sub>,
 * B<sub>1</sub>+B<sub>2</sub>).
 * If the sums of any of the color values is greater than 1.0,
 * brighter than the maximum brightness permitted, the color value is
 * clamped to 1.0.
 * <p>
 * <b>Material Colors</b>
 * <p>
 * In the Java 3D lighting model, the light sources have an effect
 * on the scene only when there are object surfaces to absorb or
 * reflect the light. Java 3D approximates an object's color
 * by calculating the percentage of red, green, and blue light
 * the object reflects. An object with a surface color of pure green
 * absorbs all of the red and blue light that strikes it and
 * reflects all of the green light. Viewing the object in a
 * white light, the green color is reflected and you see a green
 * object. However, if the green object is viewed in a red light,
 * all of the red light is absorbed and the object appears black.
 * <p>
 * The surface of each object in the scene has
 * certain material properties that define how light affects its
 * appearance. The object might reflect light in various ways,
 * depending on the object's surface type. The object
 * might even emit its own light. The Java 3D lighting model specifies
 * these material properties as five independent components: emitted
 * color, ambient color, diffuse color, specular color, and shininess.
 * All of these properties are computed independently, then added
 * together to define how the surface of the object appears under
 * light (an exception is Ambient light, which does not contribute
 * to specular reflection). The material properties are defined
 * in the Material class.
 * <p>
 * <b>Influencing Bounds</b>
 * <p>
 * Since a scene may be quite large, as large as the universe for
 * example, it is often reasonable to limit the influence of lighting
 * to a region that is within viewing range. There is no reason
 * to waste all that computing power on illuminating objects that
 * are too far away to be viewed. In Java 3D, the influencing bounds
 * is defined by a Bounds object or a BoundingLeaf object. It should
 * be noted that a BoundingLeaf object overrides a Bounds object,
 * should both objects be set.
 * <p>
 * A Bounds object represents a convex, closed volume. Bounds
 * defines three different types of containing
 * volumes: an axis-aligned-box volume, a spherical volume, and a
 * bounding polytope. A BoundingLeaf object also specifies a region
 * of influence, but allows an application to specify a bounding
 * region in one coordinate system (the local coordinate system of
 * the BoundingLeaf node) other than the local coordinate
 * system of the node that references the bounds (the Light).
 * <p>
 * <b>Limiting the Scope</b>
 * <p>
 * In addition to limiting the lighting calculations to a given
 * region of a scene, lighting can also be limited to groups of
 * nodes, defined by a Group object. This is known as "scoping."
 * All nodes attached to a Group node define a <i>list of scopes</i>.
 * Methods in the Light class permit the setting, addition, insertion,
 * removal, and enumeration of nodes in the list of scopes.
 * <p>
 * <b>Two-sided Lighting of Polygons</b>
 * <p>
 * Java 3D performs lighting calculations for all polygons, whether
 * they are front-facing or back-facing. Since most polygon objects
 * are constructed with the front face in mind, the back-facing
 * portion may not be correctly illuminated. For example, a sphere
 * with part of the face cut away so you can see its inside.
 * You might want to have the inside surface lit as well as the
 * outside surface and you mught also want to define a different
 * Material description to reduce shininess, specular color, etc.
 * <p>
 * For more information, see the "Face culling" and "Back-facing
 * normal flip" descriptions in the PolygonAttributes class
 * description.
 * <p>
 * <b>Turning on the Lights</b>
 * <p>
 * Lighting needs to be explicitly enabled with the setEnable method
 * or with the lightOn parameter in the constructor
 * before any of the child light sources have any effect on illuminating
 * the scene. The child lights may also be enabled or disabled individually.
 * <p>
 * If lighting is not enabled, the current color of an
 * object in the scene is simply mapped onto the object, and none of
 * the lighting equations regarding Material properties, such as ambient
 * color, diffuse color, specular color, and shininess, are performed.
 * However, an object's emitted color, if specified and enabled, will
 * still affect that object's appearance.
 * <p>
 * To disable lighting, call setEnable with <code>false</code> as
 * the argument.
 *
 * @see Material
 * @see Bounds
 * @see BoundingLeaf
 * @see Group
 * @see PolygonAttributes
 */

public abstract class Light extends Leaf {
    /**
     * Specifies that this Light allows read access to its current state
     * information at runtime.
     */
    public static final int
    ALLOW_STATE_READ = CapabilityBits.LIGHT_ALLOW_STATE_READ;

    /**
     * Specifies that this Light allows write access to its current state
     * information at runtime.
     */
    public static final int
    ALLOW_STATE_WRITE = CapabilityBits.LIGHT_ALLOW_STATE_WRITE;

    /**
     * Specifies that this Light allows read access to its color
     * information at runtime.
     */
    public static final int
    ALLOW_COLOR_READ = CapabilityBits.LIGHT_ALLOW_COLOR_READ;

    /**
     * Specifies that this Light allows write access to its color
     * information at runtime.
     */
    public static final int
    ALLOW_COLOR_WRITE = CapabilityBits.LIGHT_ALLOW_COLOR_WRITE;

    /**
     * Specifies that this Light allows read access to its
     * influencing bounds and bounds leaf information.
     */
    public static final int
    ALLOW_INFLUENCING_BOUNDS_READ = CapabilityBits.LIGHT_ALLOW_INFLUENCING_BOUNDS_READ;

    /**
     * Specifies that this Light allows write access to its
     * influencing bounds and bounds leaf information.
     */
    public static final int
    ALLOW_INFLUENCING_BOUNDS_WRITE = CapabilityBits.LIGHT_ALLOW_INFLUENCING_BOUNDS_WRITE;

    /**
     * Specifies that this Light allows read access to its scope
     * information at runtime.
     */
    public static final int
    ALLOW_SCOPE_READ = CapabilityBits.LIGHT_ALLOW_SCOPE_READ;

    /**
     * Specifies that this Light allows write access to its scope
     * information at runtime.
     */
    public static final int
    ALLOW_SCOPE_WRITE = CapabilityBits.LIGHT_ALLOW_SCOPE_WRITE;

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_STATE_READ,
        ALLOW_COLOR_READ,
        ALLOW_INFLUENCING_BOUNDS_READ,
        ALLOW_SCOPE_READ
    };

    /**
     * Constructs a Light node with default parameters.  The default
     * values are as follows:
     * <ul>
     * enable flag : true<br>
     * color : white (1,1,1)<br>
     * scope : empty (universe scope)<br>
     * influencing bounds : null<br>
     * influencing bounding leaf : null<br>
     * </ul>
     */
    public Light() {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Constructs and initializes a Light node using the specified color.
     * @param color the color of the light source
     */
    public Light(Color3f color) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((LightRetained)this.retained).initColor(color);
    }

    /**
     * Constructs and initializes a Light node using the specified enable
     * flag and color.
     * @param lightOn flag indicating whether this light is on or off
     * @param color the color of the light source
     */
    public Light(boolean lightOn, Color3f color) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((LightRetained)this.retained).initEnable(lightOn);
	((LightRetained)this.retained).initColor(color);
    }

    /**
     * Turns the light on or off.
     * @param state true or false to set light on or off
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setEnable(boolean state) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_STATE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Light0"));

	if (isLive())
	    ((LightRetained)this.retained).setEnable(state);
	else
	    ((LightRetained)this.retained).initEnable(state);
    }

    /**
     * Retrieves this Light's current state (on/off).
     * @return this node's current state (on/off)
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public boolean getEnable() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_STATE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Light1"));

	return ((LightRetained)this.retained).getEnable();
    }

    /**
     * Sets the Light's current color.
     * @param color the value of this node's new color
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setColor(Color3f color) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COLOR_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Light2"));

	if (isLive())
	    ((LightRetained)this.retained).setColor(color);
	else
	    ((LightRetained)this.retained).initColor(color);
    }

    /**
     * Gets this Light's current color and places it in the parameter specified.
     * @param color the vector that will receive this node's color
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void getColor(Color3f color) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COLOR_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Light3"));

	((LightRetained)this.retained).getColor(color);
    }

    /**
     * Replaces the node at the specified index in this Light node's
     * list of scopes with the specified Group node.
     * By default, Light nodes are scoped only by their influencing
     * bounds.  This allows them to be further scoped by a list of
     * nodes in the hierarchy.
     * @param scope the Group node to be stored at the specified index.
     * @param index the index of the Group node to be replaced.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception RestrictedAccessException if the specified group node
     * is part of a compiled scene graph
     */
    public void setScope(Group scope, int index) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Light4"));

	if (isLive())
	    ((LightRetained)this.retained).setScope(scope, index);
	else
	    ((LightRetained)this.retained).initScope(scope, index);
    }


    /**
     * Retrieves the Group node at the specified index from this Light node's
     * list of scopes.
     * @param index the index of the Group node to be returned.
     * @return the Group node at the specified index.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public Group getScope(int index) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Light5"));

	return ((LightRetained)this.retained).getScope(index);
    }


    /**
     * Inserts the specified Group node into this Light node's
     * list of scopes at the specified index.
     * By default, Light nodes are scoped only by their influencing
     * bounds.  This allows them to be further scoped by a list of
     * nodes in the hierarchy.
     * @param scope the Group node to be inserted at the specified index.
     * @param index the index at which the Group node is inserted.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception RestrictedAccessException if the specified group node
     * is part of a compiled scene graph
     */
    public void insertScope(Group scope, int index) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Light6"));

	if (isLive())
	    ((LightRetained)this.retained).insertScope(scope, index);
	else
	    ((LightRetained)this.retained).initInsertScope(scope, index);
    }


    /**
     * Removes the node at the specified index from this Light node's
     * list of scopes.  If this operation causes the list of scopes to
     * become empty, then this Light will have universe scope: all nodes
     * within the region of influence will be affected by this Light node.
     * @param index the index of the Group node to be removed.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception RestrictedAccessException if the group node at the
     * specified index is part of a compiled scene graph
     */
    public void removeScope(int index) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Light7"));

	if (isLive())
	    ((LightRetained)this.retained).removeScope(index);
	else
	    ((LightRetained)this.retained).initRemoveScope(index);
    }


/**
 * Returns an enumeration of this Light node's list of scopes.
 * @return an Enumeration object containing all nodes in this Light node's
 * list of scopes.
 * @exception CapabilityNotSetException if appropriate capability is
 * not set and this object is part of live or compiled scene graph
 */
public Enumeration<Group> getAllScopes() {
if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_SCOPE_READ))
	throw new CapabilityNotSetException(J3dI18N.getString("Light8"));

return ((LightRetained)this.retained).getAllScopes();
}


    /**
     * Appends the specified Group node to this Light node's list of scopes.
     * By default, Light nodes are scoped only by their influencing
     * bounds.  This allows them to be further scoped by a list of
     * nodes in the hierarchy.
     * @param scope the Group node to be appended.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception RestrictedAccessException if the specified group node
     * is part of a compiled scene graph
     */
    public void addScope(Group scope) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Light9"));

	if (isLive())
	    ((LightRetained)this.retained).addScope(scope);
	else
	    ((LightRetained)this.retained).initAddScope(scope);
    }


    /**
     * Returns the number of nodes in this Light node's list of scopes.
     * If this number is 0, then the list of scopes is empty and this
     * Light node has universe scope: all nodes within the region of
     * influence are affected by this Light node.
     * @return the number of nodes in this Light node's list of scopes.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public int numScopes() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Light8"));

	return ((LightRetained)this.retained).numScopes();
    }


    /**
     * Retrieves the index of the specified Group node in this
     * Light node's list of scopes.
     *
     * @param scope the Group node to be looked up.
     * @return the index of the specified Group node;
     * returns -1 if the object is not in the list.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public int indexOfScope(Group scope) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Light8"));

	return ((LightRetained)this.retained).indexOfScope(scope);
    }


    /**
     * Removes the specified Group node from this Light
     * node's list of scopes.  If the specified object is not in the
     * list, the list is not modified.  If this operation causes the
     * list of scopes to become empty, then this Light
     * will have universe scope: all nodes within the region of
     * influence will be affected by this Light node.
     *
     * @param scope the Group node to be removed.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception RestrictedAccessException if the specified group node
     * is part of a compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public void removeScope(Group scope) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Light7"));

	if (isLive())
	    ((LightRetained)this.retained).removeScope(scope);
	else
	    ((LightRetained)this.retained).initRemoveScope(scope);
    }


    /**
     * Removes all Group nodes from this Light node's
     * list of scopes.  The Light node will then have
     * universe scope: all nodes within the region of influence will
     * be affected by this Light node.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception RestrictedAccessException if any group node in this
     * node's list of scopes is part of a compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public void removeAllScopes() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Light7"));

	if (isLive())
	    ((LightRetained)this.retained).removeAllScopes();
	else
	    ((LightRetained)this.retained).initRemoveAllScopes();
    }


    /**
     * Sets the Light's influencing region to the specified bounds.
     * This is used when the influencing bounding leaf is set to null.
     * @param region the bounds that contains the Light's new influencing
     * region.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setInfluencingBounds(Bounds region) {
    	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_INFLUENCING_BOUNDS_WRITE))
	    	throw new CapabilityNotSetException(J3dI18N.getString("Light11"));

	if (isLive())
	    ((LightRetained)this.retained).setInfluencingBounds(region);
	else
	    ((LightRetained)this.retained).initInfluencingBounds(region);
    }

    /**
     * Retrieves the Light node's influencing bounds.
     * @return this Light's influencing bounds information
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public Bounds getInfluencingBounds() {
    	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_INFLUENCING_BOUNDS_READ))
	    	throw new CapabilityNotSetException(J3dI18N.getString("Light12"));

	return ((LightRetained)this.retained).getInfluencingBounds();
    }

    /**
     * Sets the Light's influencing region to the specified bounding leaf.
     * When set to a value other than null, this overrides the influencing
     * bounds object.
     * @param region the bounding leaf node used to specify the Light
     * node's new influencing region.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setInfluencingBoundingLeaf(BoundingLeaf region) {
    	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_INFLUENCING_BOUNDS_WRITE))
	    	throw new CapabilityNotSetException(J3dI18N.getString("Light11"));

	if (isLive())
	    ((LightRetained)this.retained).setInfluencingBoundingLeaf(region);
	else
	    ((LightRetained)this.retained).initInfluencingBoundingLeaf(region);
    }

    /**
     * Retrieves the Light node's influencing bounding leaf.
     * @return this Light's influencing bounding leaf information
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public BoundingLeaf getInfluencingBoundingLeaf() {
    	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_INFLUENCING_BOUNDS_READ))
	    	throw new CapabilityNotSetException(J3dI18N.getString("Light12"));

	return ((LightRetained)this.retained).getInfluencingBoundingLeaf();
    }



   /**
     * Copies all Light information from
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
    @Override
    void duplicateAttributes(Node originalNode, boolean forceDuplicate) {
        super.duplicateAttributes(originalNode, forceDuplicate);

	LightRetained attr = (LightRetained) originalNode.retained;
	LightRetained rt = (LightRetained) retained;

	Color3f c = new Color3f();
	attr.getColor(c);
	rt.initColor(c);
	rt.initInfluencingBounds(attr.getInfluencingBounds());

	Enumeration<Group> elm = attr.getAllScopes();
	while (elm.hasMoreElements()) {
	  // this reference will set correctly in updateNodeReferences() callback
	    rt.initAddScope(elm.nextElement());
	}

	  // this reference will set correctly in updateNodeReferences() callback
	rt.initInfluencingBoundingLeaf(attr.getInfluencingBoundingLeaf());

	rt.initEnable(attr.getEnable());
    }

    /**
     * Callback used to allow a node to check if any scene graph objects
     * referenced
     * by that node have been duplicated via a call to <code>cloneTree</code>.
     * This method is called by <code>cloneTree</code> after all nodes in
     * the sub-graph have been duplicated. The cloned Leaf node's method
     * will be called and the Leaf node can then look up any object references
     * by using the <code>getNewObjectReference</code> method found in the
     * <code>NodeReferenceTable</code> object.  If a match is found, a
     * reference to the corresponding object in the newly cloned sub-graph
     * is returned.  If no corresponding reference is found, either a
     * DanglingReferenceException is thrown or a reference to the original
     * object is returned depending on the value of the
     * <code>allowDanglingReferences</code> parameter passed in the
     * <code>cloneTree</code> call.
     * <p>
     * NOTE: Applications should <i>not</i> call this method directly.
     * It should only be called by the cloneTree method.
     *
     * @param referenceTable a NodeReferenceTableObject that contains the
     *  <code>getNewObjectReference</code> method needed to search for
     *  new object instances.
     * @see NodeReferenceTable
     * @see Node#cloneTree
     * @see DanglingReferenceException
     */
    @Override
    public void updateNodeReferences(NodeReferenceTable referenceTable) {


	LightRetained rt = (LightRetained) retained;
        BoundingLeaf bl = rt.getInfluencingBoundingLeaf();

        if (bl != null) {
            Object o = referenceTable.getNewObjectReference(bl);
            rt.initInfluencingBoundingLeaf((BoundingLeaf)o);
        }

	int num = rt.numScopes();
	for (int i=0; i < num; i++) {
	  rt.initScope((Group) referenceTable.
		       getNewObjectReference(rt.getScope(i)), i);
	}
    }

}
