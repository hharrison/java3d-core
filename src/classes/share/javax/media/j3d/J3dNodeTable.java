/*
 * $RCSfile$
 *
 * Copyright 1998-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import javax.vecmath.Color3f;
import javax.vecmath.Tuple3f;

/**
 * The J3dNodeTable object provides utilities for the save/load
 * methods in the Java3d nodes.  Specifically, it holds an enumerated
 * list of the Java3D node types and their respective Class names.
 * It keeps these lists in a Hashtable and an array and allows
 * other classes to get an enumerated value associated with an Object
 * type or an instance of an Object associated with an enumerated value.
 *
 */
class J3dNodeTable {

    // nodeTable holds the enumerated value/Classname pairs.  This is
    // used to look up enumerated values given a Class name.
    Hashtable nodeTable = new Hashtable();

    // nodeArray holds the value/Classname pairs also, but allows lookups
    // in the other direction (given a value, what's the Class name?)
    String nodeArray[];

    // Following is a list of the current scene graph objects of java3D.
    // In order to make later node insertion easier, and to add some logic to
    // this arbitrary list, the groups of nodes are grouped in terms of
    // similar functionality of node types.

    // Maximum number of nodes
    static final int MAX_NUM_NODES = 200;

    // Nothing occupies slot 0 - thus we can return 0 from this class'
    // methods to denote failure.
    static final int NOTHING = 0;

    // 1 - 9: Groups
    static final int GROUP = 1;
    static final int TRANSFORM_GROUP = 2;
    static final int SWITCH_GROUP = 3;
    static final int ORDERED_GROUP = 4;
    static final int BRANCH_GROUP = 5;
    static final int ENDGROUP = 9;		// denotes done with group

    // 10 - 19: Shape3D (in a class by itself)
    static final int SHAPE3D = 10;

    // 20 - 49: Appearance and all of its attributes
    static final int APPEARANCE = 20;
    static final int MATERIAL = 21;
    static final int TEXTURE = 22;
    static final int TEX_COORD_GENERATION = 23;
    static final int TEXTURE_ATTRIBUTES = 24;
    static final int COLORING_ATTRIBUTES = 25;
    static final int TRANSPARENCY_ATTRIBUTES = 26;
    static final int RENDERING_ATTRIBUTES = 27;
    static final int POLYGON_ATTRIBUTES = 28;
    static final int LINE_ATTRIBUTES = 29;
    static final int POINT_ATTRIBUTES = 30;
    static final int TEXTURE_2D = 31;
    static final int TEXTURE_3D = 32;
    static final int IMAGE_COMPONENT = 33;
    static final int IMAGE_COMPONENT_2D = 34;
    static final int IMAGE_COMPONENT_3D = 35;
    static final int ENDAPPEARANCE = 49;

    // 100 - 149: All Geometry types
    static final int GEOMETRY = 100;
    static final int COMPRESSED_GEOMETRY = 101;
    static final int GEOMETRY_ARRAY = 102;
    static final int GEOMETRY_STRIP_ARRAY = 103;
    static final int INDEXED_GEOMETRY_ARRAY = 104;
    static final int INDEXED_GEOMETRY_STRIP_ARRAY = 105;
    static final int INDEXED_LINE_ARRAY = 106;
    static final int INDEXED_LINE_STRIP_ARRAY = 107;
    static final int INDEXED_POINT_ARRAY = 108;
    static final int INDEXED_QUAD_ARRAY = 109;
    static final int INDEXED_TRIANGLE_ARRAY = 110;
    static final int INDEXED_TRIANGLE_FAN_ARRAY = 111;
    static final int INDEXED_TRIANGLE_STRIP_ARRAY = 112;
    static final int LINE_ARRAY = 113;
    static final int LINE_STRIP_ARRAY = 114;
    static final int POINT_ARRAY = 115;
    static final int QUAD_ARRAY = 116;
    static final int TRIANGLE_ARRAY = 117;
    static final int TRIANGLE_FAN_ARRAY = 118;
    static final int TRIANGLE_STRIP_ARRAY = 119;
    static final int BACKGROUND_SOUND = 120;
    static final int POINT_SOUND = 121;
    static final int CONE_SOUND = 122;
    static final int MEDIA_CONTAINER = 123;

    // 150 - 169: Behaviors
    static final int ROTATION_INTERPOLATOR = 150;
    static final int ROTPOSSCALEPATH_INTERPOLATOR = 151;
    static final int ROTATIONPATH_INTERPOLATOR = 152;
    static final int POSITIONPATH_INTERPOLATOR = 153;
    static final int ROTPOSPATH_INTERPOLATOR = 154;
    static final int POSITION_INTERPOLATOR = 155;
    static final int SWITCHVALUE_INTERPOLATOR = 156;
    static final int COLOR_INTERPOLATOR = 157;
    static final int SCALE_INTERPOLATOR = 158;
    // Problem: these next two are non j3d-core items
    static final int SOUND_PLAYER = 159;
    static final int SOUND_FADER = 160;

    // 170 - 189: Various utility nodes
    static final int BOUNDS = 170;
    static final int BOUNDING_SPHERE = 171;
    static final int BOUNDING_BOX = 172;
    static final int BOUNDING_POLYTOPE = 173;
    static final int TRANSFORM3D = 180;
    static final int BACKGROUND = 181;

    // 190 - 199: Lights
    static final int LIGHT = 190;
    static final int POINT_LIGHT = 191;
    static final int SPOT_LIGHT = 192;
    static final int DIRECTIONAL_LIGHT = 193;
    static final int AMBIENT_LIGHT = 194;


    /**
     * Constructs this Object, which initializes the array and Hashtable
     */
    J3dNodeTable() {
	nodeArray = new String[MAX_NUM_NODES];
	//Initialize all table entries to null
	for (int i = 0; i < MAX_NUM_NODES; ++i)
	    nodeArray[i] = null;
	// Setup slots of nodeArray where there should be valid values
	nodeArray[GROUP] = "Group";
	nodeArray[TRANSFORM_GROUP] = "TransformGroup";
	nodeArray[SWITCH_GROUP] = "Switch";
	nodeArray[ORDERED_GROUP] = "OrderedGroup";
	nodeArray[BRANCH_GROUP] = "BranchGroup";

	nodeArray[SHAPE3D] = "Shape3D";

	nodeArray[APPEARANCE] = "Appearance";
	nodeArray[MATERIAL] = "Material";
	nodeArray[TEXTURE] = "Texture";
	nodeArray[TEXTURE_2D] = "Texture2D";
	nodeArray[TEXTURE_3D] = "Texture3D";
	nodeArray[IMAGE_COMPONENT] = "ImageComponent";
	nodeArray[IMAGE_COMPONENT_2D] = "ImageComponent2D";
	nodeArray[IMAGE_COMPONENT_3D] = "ImageComponent3D";
	nodeArray[TRANSPARENCY_ATTRIBUTES] = "TransparencyAttributes";

	nodeArray[GEOMETRY] = "Geometry";
	nodeArray[COMPRESSED_GEOMETRY] = "CompressedGeometry";
	nodeArray[GEOMETRY_ARRAY] = "GeometryArray";
	nodeArray[GEOMETRY_STRIP_ARRAY] = "GeometryStripArray";
	nodeArray[INDEXED_GEOMETRY_ARRAY] = "IndexedGeometryArray";
	nodeArray[INDEXED_GEOMETRY_STRIP_ARRAY] = "IndexedGeometryStripArray";
	nodeArray[INDEXED_LINE_ARRAY] = "IndexedLineArray";
	nodeArray[INDEXED_LINE_STRIP_ARRAY] = "IndexedLineStripArray";
	nodeArray[INDEXED_POINT_ARRAY] = "IndexedPointArray";
	nodeArray[INDEXED_QUAD_ARRAY] = "IndexedQuadArray";
	nodeArray[INDEXED_TRIANGLE_ARRAY] = "IndexedTriangleArray";
	nodeArray[INDEXED_TRIANGLE_FAN_ARRAY] = "IndexedTriangleFanArray";
	nodeArray[INDEXED_TRIANGLE_STRIP_ARRAY] = "indexedTriangleStripArray";
	nodeArray[LINE_ARRAY] = "LineArray";
	nodeArray[LINE_STRIP_ARRAY] = "LineStripArray";
	nodeArray[POINT_ARRAY] = "PointArray";
	nodeArray[QUAD_ARRAY] = "QuadArray";
	nodeArray[TRIANGLE_ARRAY] = "TriangleArray";
	nodeArray[TRIANGLE_FAN_ARRAY] = "TriangleFanArray";
	nodeArray[TRIANGLE_STRIP_ARRAY] = "TriangleStripArray";
	nodeArray[BACKGROUND_SOUND] = "BackgroundSound";
	nodeArray[POINT_SOUND] = "PointSound";
	nodeArray[CONE_SOUND] = "ConeSound";
	nodeArray[MEDIA_CONTAINER] = "MediaContainer";

	nodeArray[ROTATION_INTERPOLATOR] = "RotationInterpolator";
	nodeArray[ROTPOSSCALEPATH_INTERPOLATOR] = "RotPosScalePathInterpolator";
	nodeArray[ROTATIONPATH_INTERPOLATOR] = "RotationPathInterpolator";
	nodeArray[POSITIONPATH_INTERPOLATOR] = "PositionPathInterpolator";
	nodeArray[ROTPOSPATH_INTERPOLATOR] = "RotPosPathInterpolator";
	nodeArray[POSITION_INTERPOLATOR] = "PositionInterpolator";
	nodeArray[SWITCHVALUE_INTERPOLATOR] = "SwitchValueInterpolator";
	nodeArray[COLOR_INTERPOLATOR] = "ColorInterpolator";
	nodeArray[SCALE_INTERPOLATOR] = "ScaleInterpolator";
	nodeArray[SOUND_PLAYER] = "SoundPlayer";
	nodeArray[SOUND_FADER] = "SoundFader";

	nodeArray[BOUNDS] = "Bounds";
	nodeArray[BOUNDING_SPHERE] = "BoundingSphere";
	nodeArray[BOUNDING_BOX] = "BoundingBox";
	nodeArray[BOUNDING_POLYTOPE] = "BoundingPolytope";
	nodeArray[TRANSFORM3D] = "Transform3D";
	nodeArray[BACKGROUND] = "Background";

	nodeArray[LIGHT] = "Light";
	nodeArray[POINT_LIGHT] = "PointLight";
	nodeArray[SPOT_LIGHT] = "SpotLight";
	nodeArray[DIRECTIONAL_LIGHT] = "DirectionalLight";
	nodeArray[AMBIENT_LIGHT] = "AmbientLight";

	for (int i = 0; i < MAX_NUM_NODES; ++i) {
	    if (nodeArray[i] != null)
		nodeTable.put(nodeArray[i], new Integer(i));
	}

    }

    /**
     * Returns the enumerated value associated with an Object.  This
     * method retrieves the base class name (with no package name and
     * with no "Retained" portion (if it's part of the Object's name);
     * we're just looking for the base Java3d node type here.
     */
    int getNodeValue(Object object) {
	Integer i;
	String fullName = object.getClass().getName();
	int firstIndex;
	int lastIndex ;
	if ((firstIndex = fullName.lastIndexOf(".")) == -1)
	    firstIndex = 0;
	else
	    firstIndex++;
	if ((lastIndex = fullName.lastIndexOf("Retained")) == -1)
	    lastIndex = fullName.length();
	String nodeName = fullName.substring(firstIndex, lastIndex);
	if ((i = (Integer)nodeTable.get(nodeName))
	    != null) {
	    return i.intValue();
	}
	else {
	    // This conditional exists because if a group node is
	    // actually a subclass of some standard Java3d Group type
	    // (e.g., VrmlParser), then we'll just save it and reload
	    // it as a Group.
	    if (object instanceof TransformGroup ||
		   object instanceof TransformGroupRetained)
		return TRANSFORM_GROUP;
	    else if (object instanceof BranchGroup ||
			object instanceof BranchGroupRetained)
		return BRANCH_GROUP;
	    else if (object instanceof Switch ||
			object instanceof SwitchRetained)
		return SWITCH_GROUP;
	    else if (object instanceof Group ||
			object instanceof GroupRetained)
		return GROUP;
	    else if (object instanceof Shape3D)
		return SHAPE3D;  // For Text2D object in particular
	    else {
		System.err.println("Warning: Don't know how to save object of type " + object);
		return 0;
	    }
	}
    }

    /**
     * Returns new instance of an object with the Class name
     * associated with the given enumerated value.
     */
    Object getObject(int nodeValue) {
	try {
	    if (nodeArray[nodeValue] != null) {
		String nodeName = "javax.media.j3d." + nodeArray[nodeValue];
		return Class.forName(nodeName).newInstance();
	    }
	}
	catch (Exception e) {
	    throw new RuntimeException("Exception creating object for nodeValue " +
				       nodeValue + "\n  nodeName = javax.media.j3d." +
				       nodeArray[nodeValue]);
	}
	return null;
    }


}
