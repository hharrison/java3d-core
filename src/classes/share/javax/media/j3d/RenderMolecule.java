/*
 * Copyright 1999-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import javax.vecmath.Vector3d;

/**
 * The RenderMolecule manages a collection of RenderAtoms.
 */

class RenderMolecule extends IndexedObject implements ObjectUpdate, NodeComponentUpdate {


    // different types of IndexedUnorderedSet that store RenderMolecule
    static final int REMOVE_RENDER_ATOM_IN_RM_LIST = 0;
    static final int RENDER_MOLECULE_LIST = 1;

    // total number of different IndexedUnorderedSet types
    static final int TOTAL_INDEXED_UNORDER_SET_TYPES = 2;

    /**
     * Values for the geometryType field
     */
    static final int POINT      = 0x01;
    static final int LINE       = 0x02;
    static final int SURFACE    = 0x04;
    static final int RASTER     = 0x08;
    static final int COMPRESSED = 0x10;

    static int RM_COMPONENTS = (AppearanceRetained.POLYGON |
				AppearanceRetained.LINE |
				AppearanceRetained.POINT |
				AppearanceRetained.MATERIAL |
				AppearanceRetained.TRANSPARENCY|
				AppearanceRetained.COLOR);

    // XXXX: use definingMaterial etc. instead of these
    // when sole user is completely implement
    PolygonAttributesRetained polygonAttributes = null;
    LineAttributesRetained lineAttributes = null;
    PointAttributesRetained pointAttributes = null;
    MaterialRetained material = null;
    ColoringAttributesRetained coloringAttributes = null;
    TransparencyAttributesRetained transparency = null;

    // Use Object instead of AppearanceRetained class for
    // state caching optimation memory performance

    boolean normalPresent = true;


    // Equivalent bits
    static final int POINTATTRS_DIRTY        = AppearanceRetained.POINT;
    static final int LINEATTRS_DIRTY         = AppearanceRetained.LINE;
    static final int POLYGONATTRS_DIRTY      = AppearanceRetained.POLYGON;
    static final int MATERIAL_DIRTY          = AppearanceRetained.MATERIAL;
    static final int TRANSPARENCY_DIRTY      = AppearanceRetained.TRANSPARENCY;
    static final int COLORINGATTRS_DIRTY     = AppearanceRetained.COLOR;

    static final int ALL_DIRTY_BITS    = POINTATTRS_DIRTY | LINEATTRS_DIRTY | POLYGONATTRS_DIRTY | MATERIAL_DIRTY | TRANSPARENCY_DIRTY | COLORINGATTRS_DIRTY;

    /**
     * bit mask of all attr fields that are equivalent across
     * renderMolecules
     */
    int dirtyAttrsAcrossRms = ALL_DIRTY_BITS;


    // Mask set to true is any of the component have changed
    int soleUserCompDirty = 0;

    /**
     * The PolygonAttributes for this RenderMolecule
     */
    PolygonAttributesRetained definingPolygonAttributes = null;

    /**
     * The LineAttributes for this RenderMolecule
     */
    LineAttributesRetained definingLineAttributes = null;

    /**
     * The PointAttributes for this RenderMolecule
     */
    PointAttributesRetained definingPointAttributes = null;

    /**
     * The TextureBin that this RenderMolecule resides
     */
    TextureBin textureBin = null;

    /**
     * The localToVworld for this RenderMolecule
     */
    Transform3D[] localToVworld = null;
    int[] localToVworldIndex = null;

    /**
     * The Material reference for this RenderMolecule
     */
    MaterialRetained definingMaterial = null;


    /**
     * The ColoringAttribute reference for this RenderMolecule
     */
    ColoringAttributesRetained definingColoringAttributes = null;


    /**
     * The Transparency reference for this RenderMolecule
     */
    TransparencyAttributesRetained definingTransparency = null;

    /**
     * Transform3D - point to the right one based on bg or not
     */
    Transform3D[] trans = null;


    /**
     * specify whether scale is nonuniform
     */
    boolean isNonUniformScale = false;

    /**
     * number of renderAtoms to be rendered in this RenderMolecule
     */
    int numRenderAtoms = 0;

    /**
     * number of render atoms, used during the renderBin update time
     */
    int numEditingRenderAtoms = 0;

    RenderAtom addRAs = null;
    RenderAtom removeRAs = null;

    /**
     * The cached ColoringAttributes color value.  It is
     * 1.0, 1.0, 1.0 if there is no ColoringAttributes.
     */
    float red = 1.0f;
    float green = 1.0f;
    float blue = 1.0f;


    /**
     * Cached diffuse color value
     */
    float dRed = 1.0f;
    float dGreen = 1.0f;
    float dBlue = 1.0f;



    /**
     * The cached TransparencyAttributes transparency value.  It is
     * 0.0 if there is no TransparencyAttributes.
     */
    float alpha = 0.0f;

    /**
     * The geometry type for this RenderMolecule
     */
    int geometryType = -1;

    /**
     * A boolean indicating whether or not lighting should be on.
     */
    boolean enableLighting = false;

    /**
     * A boolean indicating whether or not this molecule rendered Text3D
     */

    int primaryMoleculeType = 0;
    static int COMPRESSED_MOLECULE 	= 0x1;
    static int TEXT3D_MOLECULE     	= 0x2;
    static int DLIST_MOLECULE      	= 0x4;
    static int RASTER_MOLECULE     	= 0x8;
    static int ORIENTEDSHAPE3D_MOLECULE	= 0x10;
    static int SEPARATE_DLIST_PER_RINFO_MOLECULE = 0x20;


    /**
     * Cached values for polygonMode, line antialiasing, and point antialiasing
     */
    int polygonMode = PolygonAttributes.POLYGON_FILL;
    boolean lineAA = false;
    boolean pointAA = false;

    /**
     * The vertex format for this RenderMolecule.  Only looked
     * at for GeometryArray and CompressedGeometry objects.
     */
    int vertexFormat = -1;

    /**
     * The texCoordSetMap length for this RenderMolecule.
     */
    int texCoordSetMapLen = 0;

    /**
     * The primary renderMethod object for this RenderMolecule
     * this is either a Text3D, display list, or compressed geometry renderer.
     */
    RenderMethod primaryRenderMethod = null;

    /**
     * The secondary renderMethod object for this RenderMolecule
     * this is used for geometry that is shared
     */
    RenderMethod secondaryRenderMethod = null;

    /**
     * The RenderBino for this molecule
     */
    RenderBin renderBin = null;

    /**
     * The references to the next and previous RenderMolecule in the
     * list.
     */
    RenderMolecule next = null;
    RenderMolecule prev = null;


    /**
     * The list of RenderAtoms in this RenderMolecule that are not using
     * vertex arrays.
     */
    RenderAtomListInfo primaryRenderAtomList = null;


    /**
     * The list of RenderAtoms in this RenderMolecule that are using
     * separte dlist .
     */
    RenderAtomListInfo separateDlistRenderAtomList = null;


    /**
     * The list of RenderAtoms in this RenderMolecule that are using vertex
     * arrays.
     */
    RenderAtomListInfo vertexArrayRenderAtomList = null;

    /**
     * This BoundingBox is used for View Frustum culling on the primary
     * list
     */
    BoundingBox vwcBounds = null;


    /**
     * If this is end of the linked list for this xform, then
     * this field is non-null, if there is a map after this
     */
    RenderMolecule nextMap = null;
    RenderMolecule prevMap = null;

    /**
     * If the any of the node component of the appearance in RM will be changed
     * frequently, then confine it to a separate bin
     */
    boolean soleUser = false;
    Object appHandle = null;


    VertexArrayRenderMethod cachedVertexArrayRenderMethod =
	(VertexArrayRenderMethod)
	VirtualUniverse.mc.getVertexArrayRenderMethod();

    // In D3D separate Quad/Triangle Geometry with others in RenderMolecule
    // Since we need to dynamically switch whether to use DisplayList
    // or not in render() as a group.
    boolean isQuadGeometryArray = false;
    boolean isTriGeometryArray = false;

    // display list id, valid id starts from 1
    int displayListId = 0;
    Integer displayListIdObj = null;

    int onUpdateList = 0;
    static int NEW_RENDERATOMS_UPDATE = 0x1;
    static int BOUNDS_RECOMPUTE_UPDATE = 0x2;
    static int LOCALE_TRANSLATION = 0x4;
    static int UPDATE_BACKGROUND_TRANSFORM = 0x8;
    static int IN_DIRTY_RENDERMOLECULE_LIST = 0x10;
    static int LOCALE_CHANGED = 0x20;
    static int ON_UPDATE_CHECK_LIST = 0x40;


    // background geometry rendering
    boolean doInfinite;
    Transform3D[] infLocalToVworld;

    // Whether alpha is used in this renderMolecule
    boolean useAlpha = false;

    // Support for multiple locales
    Locale locale = null;

    // Transform when locale is different from the view's locale
    Transform3D[] localeLocalToVworld = null;

    // Vector used for locale translation
    Vector3d localeTranslation = null;

    boolean primaryChanged = false;

    boolean isOpaqueOrInOG = true;
    boolean inOrderedGroup = false;


    // closest switch parent
    SwitchRetained  closestSwitchParent = null;

    // the child index from the closest switch parent
    int closestSwitchIndex = -1;


    RenderMolecule(GeometryAtom ga,
		   PolygonAttributesRetained polygonAttributes,
		   LineAttributesRetained lineAttributes,
		   PointAttributesRetained pointAttributes,
		   MaterialRetained material,
		   ColoringAttributesRetained coloringAttributes,
		   TransparencyAttributesRetained transparency,
		   RenderingAttributesRetained renderAttrs,
		   TextureUnitStateRetained[] texUnits,
		   Transform3D[] transform, int[] transformIndex,
		   RenderBin rb) {
	renderBin = rb;
	IndexedUnorderSet.init(this, TOTAL_INDEXED_UNORDER_SET_TYPES);

	reset(ga, polygonAttributes, lineAttributes, pointAttributes,
	      material, coloringAttributes, transparency, renderAttrs,
	      texUnits, transform,
	      transformIndex);
    }

    void reset(GeometryAtom ga,
	       PolygonAttributesRetained polygonAttributes,
	       LineAttributesRetained lineAttributes,
	       PointAttributesRetained pointAttributes,
	       MaterialRetained material,
	       ColoringAttributesRetained coloringAttributes,
	       TransparencyAttributesRetained transparency,
	       RenderingAttributesRetained renderAttrs,
	       TextureUnitStateRetained[] texUnits,
	       Transform3D[] transform, int[] transformIndex) {
	primaryMoleculeType = 0;
	numRenderAtoms = 0;
        numEditingRenderAtoms = 0;
	onUpdateList = 0;
	dirtyAttrsAcrossRms = ALL_DIRTY_BITS;
	primaryRenderMethod = null;
	isNonUniformScale = false;
	primaryChanged = false;
        this.material = material;
        this.polygonAttributes = polygonAttributes;
        this.lineAttributes = lineAttributes;
        this.pointAttributes = pointAttributes;
        this.coloringAttributes = coloringAttributes;
        this.transparency = transparency;

        closestSwitchParent = ga.source.closestSwitchParent;
        closestSwitchIndex = ga.source.closestSwitchIndex;

	// Find the first non-null geometey
	GeometryRetained geo = null;
	int k = 0;
	isOpaqueOrInOG = true;
	inOrderedGroup = false;
	while (geo == null && (k < ga.geometryArray.length)) {
	    geo = ga.geometryArray[k];
	    k++;
	}

        // Issue 249 - check for sole user only if property is set
        soleUser = false;
        if (VirtualUniverse.mc.allowSoleUser) {
            if (ga.source.appearance != null) {
                soleUser = ((ga.source.appearance.changedFrequent & RM_COMPONENTS) != 0);
            }
	}

        // Set the appearance only for soleUser case
	if (soleUser)
	    appHandle = ga.source.appearance;
	else
		appHandle = this;

	// If its of type GeometryArrayRetained
	if (ga.geoType <= GeometryRetained.GEO_TYPE_GEOMETRYARRAY ||
	    ga.geoType == GeometryRetained.GEO_TYPE_TEXT3D) {

            if (ga.source instanceof OrientedShape3DRetained) {
                primaryRenderMethod =
                    VirtualUniverse.mc.getOrientedShape3DRenderMethod();
                primaryMoleculeType = ORIENTEDSHAPE3D_MOLECULE;
	    } else if (ga.geoType == GeometryRetained.GEO_TYPE_TEXT3D) {
	        primaryRenderMethod =
		    VirtualUniverse.mc.getText3DRenderMethod();
		primaryMoleculeType = TEXT3D_MOLECULE;
	    } else {
		// Make determination of dlist or not during addRenderAtom
		secondaryRenderMethod = cachedVertexArrayRenderMethod;
	    }
	}
	else {
	    if (ga.geoType == GeometryRetained.GEO_TYPE_COMPRESSED) {
		primaryRenderMethod =
		    VirtualUniverse.mc.getCompressedGeometryRenderMethod();
		primaryMoleculeType = COMPRESSED_MOLECULE;
	    }
	    else if (geo instanceof RasterRetained) {
		primaryRenderMethod =
		    VirtualUniverse.mc.getDefaultRenderMethod();
		primaryMoleculeType = RASTER_MOLECULE;
	    }
	}

	prev = null;
	next = null;
	prevMap = null;
	nextMap = null;

	primaryRenderAtomList = null;
	vertexArrayRenderAtomList = null;



	switch (ga.geoType) {
	case GeometryRetained.GEO_TYPE_POINT_SET:
	case GeometryRetained.GEO_TYPE_INDEXED_POINT_SET:
	    this.geometryType = POINT;
	    break;
	case GeometryRetained.GEO_TYPE_LINE_SET:
	case GeometryRetained.GEO_TYPE_LINE_STRIP_SET:
	case GeometryRetained.GEO_TYPE_INDEXED_LINE_SET:
	case GeometryRetained.GEO_TYPE_INDEXED_LINE_STRIP_SET:
	    this.geometryType = LINE;
	    break;
	case GeometryRetained.GEO_TYPE_RASTER:
	    this.geometryType = RASTER;
	    break;
	case GeometryRetained.GEO_TYPE_COMPRESSED:
	    this.geometryType = COMPRESSED;

	    switch (((CompressedGeometryRetained)geo).getBufferType()) {
	    case CompressedGeometryHeader.POINT_BUFFER:
		this.geometryType |= POINT ;
		break ;
	    case CompressedGeometryHeader.LINE_BUFFER:
		this.geometryType |= LINE ;
		break ;
	    default:
	    case CompressedGeometryHeader.TRIANGLE_BUFFER:
		this.geometryType |= SURFACE ;
		if (polygonAttributes != null) {
		    if (polygonAttributes.polygonMode ==
			PolygonAttributes.POLYGON_POINT) {
			this.geometryType |= POINT;
		    } else if (polygonAttributes.polygonMode ==
			       PolygonAttributes.POLYGON_LINE) {
			this.geometryType |= LINE;
		    }
		}
		break ;
	    }
	    break;
	default:
	    this.geometryType = SURFACE;
	    if (polygonAttributes != null) {
		if (polygonAttributes.polygonMode ==
		    PolygonAttributes.POLYGON_POINT) {
		    this.geometryType |= POINT;
		} else if (polygonAttributes.polygonMode ==
			   PolygonAttributes.POLYGON_LINE) {
		    this.geometryType |= LINE;
		}
	    }
	    break;
	}

	isQuadGeometryArray = (geo.getClassType() ==
			       GeometryRetained.QUAD_TYPE);
	isTriGeometryArray = (geo.getClassType() ==
			      GeometryRetained.TRIANGLE_TYPE);

	this.localToVworld = transform;
	this.localToVworldIndex = transformIndex;
        doInfinite = ga.source.inBackgroundGroup;
        if (doInfinite) {
	    if (infLocalToVworld == null) {
		infLocalToVworld = new Transform3D[2];
		infLocalToVworld[0] = infLocalToVworld[1] = new Transform3D();
	    }
            localToVworld[0].getRotation(infLocalToVworld[0]);
        }
	int mask = 0;
	if (polygonAttributes != null) {
	    if (polygonAttributes.changedFrequent != 0) {
		definingPolygonAttributes = polygonAttributes;

		mask |= POLYGONATTRS_DIRTY;
	    }
	    else {
		if (definingPolygonAttributes != null) {
		    definingPolygonAttributes.set(polygonAttributes);
		}
		else {
		    definingPolygonAttributes = (PolygonAttributesRetained)polygonAttributes.clone();
		}
	    }
	    polygonMode = definingPolygonAttributes.polygonMode;
	} else {
	    polygonMode = PolygonAttributes.POLYGON_FILL;
	    definingPolygonAttributes = null;
	}

	if (lineAttributes != null) {
	    if (lineAttributes.changedFrequent != 0) {
		definingLineAttributes = lineAttributes;
		mask |= LINEATTRS_DIRTY;
	    }
	    else {
		if (definingLineAttributes != null) {
		    definingLineAttributes.set(lineAttributes);
		}
		else {
		    definingLineAttributes = (LineAttributesRetained)lineAttributes.clone();
		}
	    }
	    lineAA = definingLineAttributes.lineAntialiasing;
	} else {
	    lineAA = false;
	    definingLineAttributes = null;
	}

	if (pointAttributes != null) {
	    if (pointAttributes.changedFrequent != 0) {
		definingPointAttributes = pointAttributes;
		mask |= POINTATTRS_DIRTY;

	    }
	    else {
		if (definingPointAttributes != null) {
		    definingPointAttributes.set(pointAttributes);
		}
		else {
		    definingPointAttributes = (PointAttributesRetained)pointAttributes.clone();
		}
	    }
	    pointAA = definingPointAttributes.pointAntialiasing;
	} else {
	    pointAA = false;
	    definingPointAttributes = null;
	}

	normalPresent = true;
	if (geo instanceof GeometryArrayRetained) {
	    GeometryArrayRetained gr = (GeometryArrayRetained)geo;
	    this.vertexFormat = gr.vertexFormat;

	    if (gr.texCoordSetMap != null) {
	        this.texCoordSetMapLen = gr.texCoordSetMap.length;
	    } else {
	        this.texCoordSetMapLen = 0;
	    }

	    // Throw an exception if lighting is enabled, but no normals defined
	    if ((vertexFormat & GeometryArray.NORMALS) == 0) {
		// Force lighting to false
		normalPresent = false;
	    }

	}
	else if (geo instanceof CompressedGeometryRetained) {
	    this.vertexFormat =
		((CompressedGeometryRetained)geo).getVertexFormat();
	    // Throw an exception if lighting is enabled, but no normals defined
	    if ((vertexFormat & GeometryArray.NORMALS) == 0) {
		// Force lighting to false
		normalPresent = false;
	    }

	    this.texCoordSetMapLen = 0;

	} else {
	    this.vertexFormat = -1;
	    this.texCoordSetMapLen = 0;
	}

	if (material != null) {
	    if (material.changedFrequent != 0) {
		definingMaterial = material;
		mask |= MATERIAL_DIRTY;
	    }
	    else {
		if (definingMaterial != null)
		    definingMaterial.set(material);
		else {
		    definingMaterial = (MaterialRetained)material.clone();
		}
	    }

	}
	else {
	    definingMaterial = null;
	}
	evalMaterialCachedState();
	if (coloringAttributes != null) {
	    if (coloringAttributes.changedFrequent != 0) {
		definingColoringAttributes = coloringAttributes;
		mask |= COLORINGATTRS_DIRTY;
	    }
	    else {
		if (definingColoringAttributes != null) {
		    definingColoringAttributes.set(coloringAttributes);
		}
		else {
		    definingColoringAttributes = (ColoringAttributesRetained)coloringAttributes.clone();
		}
	    }
	    red = coloringAttributes.color.x;
	    green = coloringAttributes.color.y;
	    blue = coloringAttributes.color.z;
	} else {
	    red = 1.0f;
	    green = 1.0f;
	    blue = 1.0f;
	    definingColoringAttributes = null;
	}

	if (transparency != null) {

	    if (transparency.changedFrequent != 0) {
		definingTransparency = transparency;
		mask |= TRANSPARENCY_DIRTY;
	    }
	    else {
		if (definingTransparency != null) {
		    definingTransparency.set(transparency);
		}
		else {
		    definingTransparency =
			(TransparencyAttributesRetained)transparency.clone();
		}
	    }
	    alpha = 1.0f - transparency.transparency;

	} else {
	    alpha = 1.0f;
	    definingTransparency = null;

	}

	locale = ga.source.locale;
	if (locale != renderBin.locale) {
	    if (localeLocalToVworld == null) {
		localeLocalToVworld = new Transform3D[2];
	    }
	    localeLocalToVworld[0] = new Transform3D();
	    localeLocalToVworld[1] = new Transform3D();
	    localeTranslation = new Vector3d();
	    ga.locale.hiRes.difference(renderBin.locale.hiRes, localeTranslation);
	    translate();
	}
	else {
	    localeLocalToVworld = localToVworld;
	}

	if (doInfinite) {
	    trans = infLocalToVworld;
	}
	else {
	    trans = localeLocalToVworld;
	}

	evalAlphaUsage(renderAttrs, texUnits);
	isOpaqueOrInOG = isOpaque() || (ga.source.orderedPath != null);
	inOrderedGroup = (ga.source.orderedPath != null);
	//	System.err.println("isOpaque = "+isOpaque() +" OrInOG = "+isOpaqueOrInOG);
	if (mask != 0) {
	    if ((soleUserCompDirty& ALL_DIRTY_BITS) == 0 ) {
		renderBin.rmUpdateList.add(this);
	    }
	    soleUserCompDirty |= mask;
	}
    }


    /**
     * This tests if the given attributes matches this TextureBin
     */
    boolean equals(RenderAtom ra,
		   PolygonAttributesRetained polygonAttributes,
		   LineAttributesRetained lineAttributes,
		   PointAttributesRetained pointAttributes,
		   MaterialRetained material,
		   ColoringAttributesRetained coloringAttributes,
		   TransparencyAttributesRetained transparency,
		   Transform3D[] transform) {
	int geoType = 0;
	GeometryAtom ga = ra.geometryAtom;

	if (this.localToVworld != transform) {
	    return (false);
	}

	if (locale != ra.geometryAtom.source.locale) {
	    return (false);
	}

	if (ra.geometryAtom.source.closestSwitchParent != closestSwitchParent ||
	    ra.geometryAtom.source.closestSwitchIndex != closestSwitchIndex) {
	    return (false);
	}

	// Find the first non-null geometey
	GeometryRetained geo = null;
	int k = 0;
	while (geo == null && (k < ga.geometryArray.length)) {
	    geo = ga.geometryArray[k];
	    k++;
	}

	// XXXX: Add tags
	switch (ga.geoType) {
	case GeometryRetained.GEO_TYPE_POINT_SET:
	case GeometryRetained.GEO_TYPE_INDEXED_POINT_SET:
	    geoType = POINT;
	    break;
	case GeometryRetained.GEO_TYPE_LINE_SET:
	case GeometryRetained.GEO_TYPE_LINE_STRIP_SET:
	case GeometryRetained.GEO_TYPE_INDEXED_LINE_SET:
	case GeometryRetained.GEO_TYPE_INDEXED_LINE_STRIP_SET:
	    geoType = LINE;
	    break;
	case GeometryRetained.GEO_TYPE_RASTER:
	    geoType = RASTER;
	    break;
	case GeometryRetained.GEO_TYPE_COMPRESSED:
	    geoType = COMPRESSED;
	    switch (((CompressedGeometryRetained)geo).getBufferType()) {
	    case CompressedGeometryHeader.POINT_BUFFER:
		geoType |= POINT ;
		break ;
	    case CompressedGeometryHeader.LINE_BUFFER:
		geoType |= LINE ;
		break ;
	    default:
	    case CompressedGeometryHeader.TRIANGLE_BUFFER:
		geoType |= SURFACE ;
		break ;
	    }
	    break;
	default:
	    geoType = SURFACE;
	    if (polygonAttributes != null) {
		if (polygonAttributes.polygonMode ==
		    PolygonAttributes.POLYGON_POINT) {
		    geoType |= POINT;
		} else if (polygonAttributes.polygonMode ==
			   PolygonAttributes.POLYGON_LINE) {
		    geoType |= LINE;
		}
	    }
	    break;
	}

	if (this.geometryType != geoType) {
	    return (false);
	}
	/*
	// XXXX : Check this
	if (useDisplayList &&
	    (ga.geometry.isEditable ||
	     ga.geometry.refCount > 1 ||
	     ((GroupRetained)ga.source.parent).switchLevel >= 0 ||
	     ga.alphaEditable)) {
	    return (false);
	}
	*/
	if (ga.geoType == GeometryRetained.GEO_TYPE_TEXT3D &&
	    primaryMoleculeType != 0 &&
	    ((primaryMoleculeType & TEXT3D_MOLECULE) == 0)) {
	    return (false);
	}


	if(!(ra.geometryAtom.source instanceof OrientedShape3DRetained)
	   && ((primaryMoleculeType & ORIENTEDSHAPE3D_MOLECULE) != 0)) {
	    //System.err.println("RA's NOT a OrientedShape3DRetained and RM is a ORIENTEDSHAPE3D_MOLECULE ");

	    return (false);
	}

	// XXXX: Its is necessary to have same vformat for dl,
	// Howabout iteration, should we have 2 vformats in rm?
	if (geo instanceof GeometryArrayRetained) {
	    GeometryArrayRetained gr = (GeometryArrayRetained)geo;
	    if (this.vertexFormat != gr.vertexFormat) {
	        return (false);
	    }


	    // we are requiring that texCoordSetMap length to be the same
	    // so that we can either put all multi-tex ga to a display list,
	    // or punt all to vertex array. And we don't need to worry
	    // about some of the ga can be in display list for this canvas,
	    // and some other can be in display list for the other canvas.
	    if (((gr.texCoordSetMap != null) &&
		 (this.texCoordSetMapLen != gr.texCoordSetMap.length)) ||
		((gr.texCoordSetMap == null) && (this.texCoordSetMapLen != 0))) {
		return (false);
	    }

	} else if (geo instanceof CompressedGeometryRetained) {
	    if (this.vertexFormat !=
		((CompressedGeometryRetained)geo).getVertexFormat()) {
	        return (false);
	    }
	} else {
            //XXXX: compare isEditable
	    if (this.vertexFormat != -1) {
	        return (false);
	    }
	}

	// If the any reference to the appearance components  that is cached renderMolecule
	// can change frequently, make a separate bin
	if (soleUser || (ra.geometryAtom.source.appearance != null &&
			 ((ra.geometryAtom.source.appearance.changedFrequent & RM_COMPONENTS) != 0))) {
		if (appHandle == ra.geometryAtom.source.appearance) {

                // if this RenderMolecule is currently on a zombie state,
                // we'll need to put it on the update list to reevaluate
                // the state, because while it is on a zombie state,
                // state could have been changed. Example,
                // application could have detached an appearance,
                // made changes to the appearance state, and then
                // reattached the appearance. In this case, the
                // changes would not have reflected to the RenderMolecule

                if (numEditingRenderAtoms == 0) {

		    if ((soleUserCompDirty& ALL_DIRTY_BITS) == 0 ) {
			renderBin.rmUpdateList.add(this);
		    }
		    soleUserCompDirty |= ALL_DIRTY_BITS;
		}
		return true;
	    }
	    else {
		return false;
	    }

	}
	// Assign the cloned value as the original value

	// Either a changedFrequent or a null case
	// and the incoming one is not equal or null
	// then return;
	// This check also handles null == null case
	if (definingPolygonAttributes != null) {
	    if ((this.definingPolygonAttributes.changedFrequent != 0) ||
		(polygonAttributes !=null && polygonAttributes.changedFrequent != 0))
		if (definingPolygonAttributes == polygonAttributes) {
		    if (definingPolygonAttributes.compChanged != 0) {
			if ((soleUserCompDirty& ALL_DIRTY_BITS) == 0 ) {
			    renderBin.rmUpdateList.add(this);
			}
			soleUserCompDirty |= POLYGONATTRS_DIRTY;
		    }
		}
		else {
		    return false;
		}
	    else if (!definingPolygonAttributes.equivalent(polygonAttributes)) {
		return false;
	    }
	}
	else if (polygonAttributes != null) {
	    return false;
	}

	if (definingLineAttributes != null) {
	    if ((this.definingLineAttributes.changedFrequent != 0) ||
		(lineAttributes !=null && lineAttributes.changedFrequent != 0))
		if (definingLineAttributes == lineAttributes) {
		    if (definingLineAttributes.compChanged != 0) {
			if ((soleUserCompDirty& ALL_DIRTY_BITS) == 0 ) {
			    renderBin.rmUpdateList.add(this);
			}
			soleUserCompDirty |= LINEATTRS_DIRTY;
		    }
		}
		else {
		    return false;
		}
	    else if (!definingLineAttributes.equivalent(lineAttributes)) {
		return false;
	    }
	}
	else if (lineAttributes != null) {
	    return false;
	}


	if (definingPointAttributes != null) {
	    if ((this.definingPointAttributes.changedFrequent != 0) ||
		(pointAttributes !=null && pointAttributes.changedFrequent != 0))
		if (definingPointAttributes == pointAttributes) {
		    if (definingPointAttributes.compChanged != 0) {
			if ((soleUserCompDirty& ALL_DIRTY_BITS) == 0 ) {
			    renderBin.rmUpdateList.add(this);
			}
			soleUserCompDirty |= POINTATTRS_DIRTY;
		    }
		}
		else {
		    return false;
		}
	    else if (!definingPointAttributes.equivalent(pointAttributes)) {
		return false;
	    }
	}
	else if (pointAttributes != null) {
	    return false;
	}




	if (definingMaterial != null) {
	    if ((this.definingMaterial.changedFrequent != 0) ||
		(material !=null && material.changedFrequent != 0))
		if (definingMaterial == material) {
		    if (definingMaterial.compChanged != 0) {
			if ((soleUserCompDirty& ALL_DIRTY_BITS) == 0 ) {
			    renderBin.rmUpdateList.add(this);
			}
			soleUserCompDirty |= MATERIAL_DIRTY;
		    }
		}
		else {
		    return false;
		}
	    else if (!definingMaterial.equivalent(material)) {
		return false;
	    }
	}
	else if (material != null) {
	    return false;
	}



	if (definingColoringAttributes != null) {
	    if ((this.definingColoringAttributes.changedFrequent != 0) ||
		(coloringAttributes !=null && coloringAttributes.changedFrequent != 0))
		if (definingColoringAttributes == coloringAttributes) {
		    if (definingColoringAttributes.compChanged != 0) {
			if ((soleUserCompDirty& ALL_DIRTY_BITS) == 0 ) {
			    renderBin.rmUpdateList.add(this);
			}
			soleUserCompDirty |= COLORINGATTRS_DIRTY;
		    }
		}
		else {
		    return false;
		}
	    else if (!definingColoringAttributes.equivalent(coloringAttributes)) {
		return false;
	    }
	}
	else if (coloringAttributes != null) {
	    return false;
	}

	// if the definingTransparency is a non cloned values and the incoming
	// one is equivalent, then check if the component is dirty
	// this happens when all the RAs from this RM have been removed
	// but new ones are not added yet (rbin visibility) not run yet
	// and when there is a change in nc based on the new RA, we wil;
	// miss the change, doing this check will catch the change durin
	// new RAs insertRenderAtom
	if (definingTransparency != null) {
	    if ((this.definingTransparency.changedFrequent != 0) ||
		(transparency !=null && transparency.changedFrequent != 0))
		if (definingTransparency == transparency) {
		    if (definingTransparency.compChanged != 0) {
			if ((soleUserCompDirty& ALL_DIRTY_BITS) == 0 ) {
			    renderBin.rmUpdateList.add(this);
			}
			soleUserCompDirty |= TRANSPARENCY_DIRTY;
		    }
		}
		else {
		    return false;
		}
	    else if (!definingTransparency.equivalent(transparency)) {
		return false;
	    }
	}
	else if (transparency != null) {
	    return false;
	}

	return (true);
    }

    public void updateRemoveRenderAtoms() {
	RenderAtom r;
	RenderAtomListInfo rinfo;

	// Check if this renderMolecule was created and destroyed this frame.
	// so, no display list was created
	if (numRenderAtoms == 0 && removeRAs == null && addRAs == null) {
	    textureBin.removeRenderMolecule(this);
	    return;
	}

	while (removeRAs != null) {
		r = removeRAs;
	    r.removed = null;
	    numRenderAtoms--;

	    // Loop thru all geometries in the renderAtom, they could
	    // potentially be in different buckets in the rendermoleulce
	    for (int index = 0; index < r.rListInfo.length; index++) {
		rinfo = r.rListInfo[index];
		// Don't remove  null geo
		if (rinfo.geometry() == null)
		    continue;

		if ((rinfo.groupType & RenderAtom.PRIMARY) != 0) {
		    primaryChanged = true;
		    if (rinfo.prev == null) { // At the head of the list
			primaryRenderAtomList = rinfo.next;
			if (rinfo.next != null) {
			    rinfo.next.prev = null;
			}
		    } else { // In the middle or at the end.
			rinfo.prev.next = rinfo.next;
			if (rinfo.next != null) {
			    rinfo.next.prev = rinfo.prev;
			}
		    }

		    // If the molecule type is Raster, then add it to the lock list
		    if (primaryMoleculeType == RASTER) {
			RasterRetained geo = (RasterRetained)rinfo.geometry();
			renderBin.removeGeometryFromLockList(geo);
			if (geo.image != null)
				renderBin.removeNodeComponent(geo.image);

		    }
		    else if ((rinfo.groupType & RenderAtom.SEPARATE_DLIST_PER_RINFO) != 0) {
			if (!rinfo.renderAtom.inRenderBin()) {
			    renderBin.removeDlistPerRinfo.add(rinfo);
			}
		    }
		}
		else if ((rinfo.groupType & RenderAtom.SEPARATE_DLIST_PER_GEO) != 0) {
		    if (rinfo.prev == null) { // At the head of the list
			separateDlistRenderAtomList = rinfo.next;
			if (rinfo.next != null) {
			    rinfo.next.prev = null;
			}
		    } else { // In the middle or at the end.
			rinfo.prev.next = rinfo.next;
			if (rinfo.next != null) {
			    rinfo.next.prev = rinfo.prev;
			}
		    }
		    renderBin.removeGeometryDlist(rinfo);

		}
		else {
		    if (rinfo.prev == null) { // At the head of the list
			vertexArrayRenderAtomList = rinfo.next;
			if (rinfo.next != null) {
			    rinfo.next.prev = null;
			}
		    } else { // In the middle or at the end.
			rinfo.prev.next = rinfo.next;
			if (rinfo.next != null) {
			    rinfo.next.prev = rinfo.prev;
			}
		    }
		    // For indexed geometry there is no need to lock since
		    // the mirror is changed only when the renderer is not
		    // running
		    // For indexed geometry, if use_coord is set, then either we
		    // are using the index geometry as is or we will be unindexifying
		    // on the fly, so its better to lock
		    GeometryArrayRetained geo = (GeometryArrayRetained)rinfo.geometry();
		    if (!(geo instanceof IndexedGeometryArrayRetained) ||
			((geo.vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) != 0)) {
			renderBin.removeGeometryFromLockList(geo);
		    }
		}
		rinfo.prev = null;
		rinfo.next = null;
	    }
	    removeRAs = removeRAs.nextRemove;
	    r.nextRemove = null;
	    r.prevRemove = null;
	    if (r.isOriented()) {
		renderBin.orientedRAs.remove(renderBin.orientedRAs.indexOf(r));
	    }

	    if ((textureBin.environmentSet.lightBin.geometryBackground == null) &&
		!isOpaqueOrInOG && renderBin.transpSortMode == View.TRANSPARENCY_SORT_GEOMETRY) {
		renderBin.removeTransparentObject(r);
	    }
	}
	// If this renderMolecule will not be touched for adding new RenderAtoms
	// then ..
	if (addRAs == null) {
	    // If there are no more renderAtoms and there will be no more
	    // renderatoms added to this renderMolecule , then remove
	    if (numRenderAtoms == 0) {
		// If both lists are empty remove this renderMolecule
		if ((primaryMoleculeType &DLIST_MOLECULE) != 0) {
		    renderBin.addDisplayListResourceFreeList(this);
		    vwcBounds.set(null);
		    displayListId = 0;
		    displayListIdObj = null;
		}
		if (locale != renderBin.locale) {
		    localeLocalToVworld = null;
		}
		textureBin.removeRenderMolecule(this);
	    } else {
		if ((primaryMoleculeType &DLIST_MOLECULE) != 0 && primaryChanged) {

		    // If a renderAtom is added to the display list
		    // structure then add this to the dirty list of rm
		    // for which the display list needs to be recreated
		    renderBin.addDirtyRenderMolecule(this);
		    vwcBounds.set(null);
		    rinfo = primaryRenderAtomList;
		    while (rinfo != null) {
			vwcBounds.combine(rinfo.renderAtom.localeVwcBounds);
			rinfo = rinfo.next;
		    }
		    primaryChanged = false;
		}
	    }
	}
	numEditingRenderAtoms = numRenderAtoms;
    }

    public void updateObject() {
	int i;
	RenderAtom renderAtom;
	RenderAtomListInfo r;
	if (textureBin == null) {
	    return;
	}

	if (addRAs != null) {
	    while (addRAs != null) {

		numRenderAtoms++;
			renderAtom = addRAs;
		renderAtom.renderMolecule = this;
		renderAtom.added = null;
		for (int j = 0; j < renderAtom.rListInfo.length; j++) {
				r = renderAtom.rListInfo[j];
		    // Don't add null geo
		    if (r.geometry() == null)
			continue;
		    r.groupType = evalRinfoGroupType(r);
		    if ((r.groupType & RenderAtom.PRIMARY) != 0) {
			if ((r.groupType & RenderAtom.DLIST) != 0 && primaryRenderMethod == null) {
			    primaryMoleculeType = DLIST_MOLECULE;
			    renderBin.renderMoleculeList.add(this);

			    if (vwcBounds == null)
				vwcBounds = new BoundingBox((BoundingBox)null);
			    primaryRenderMethod =
				VirtualUniverse.mc.getDisplayListRenderMethod();
			    // Assign a displayListId for this renderMolecule
			    if (displayListId == 0) {
				displayListIdObj = VirtualUniverse.mc.getDisplayListId();
				displayListId =  displayListIdObj.intValue();
			    }
			}
			else if ((r.groupType & RenderAtom.SEPARATE_DLIST_PER_RINFO) != 0 &&
				 primaryRenderMethod == null) {
			    primaryMoleculeType = SEPARATE_DLIST_PER_RINFO_MOLECULE;
			    renderBin.renderMoleculeList.add(this);
			    primaryRenderMethod =
				VirtualUniverse.mc.getDisplayListRenderMethod();

			}
			primaryChanged = true;
			if (primaryRenderAtomList == null) {
			    primaryRenderAtomList = r;
			}
			else {
			    r.next = primaryRenderAtomList;
			    primaryRenderAtomList.prev = r;
			    primaryRenderAtomList = r;
			}
			if (primaryMoleculeType == SEPARATE_DLIST_PER_RINFO_MOLECULE) {
			    if (r.renderAtom.dlistIds == null) {
				r.renderAtom.dlistIds = new int[r.renderAtom.rListInfo.length];

				for (int k = 0; k < r.renderAtom.dlistIds.length; k++) {
				    r.renderAtom.dlistIds[k] = -1;
				}
			    }
			    if (r.renderAtom.dlistIds[r.index] == -1) {
				r.renderAtom.dlistIds[r.index] = VirtualUniverse.mc.getDisplayListId().intValue();
				renderBin.addDlistPerRinfo.add(r);
			    }
			}

			// If the molecule type is Raster, then add it to the lock list
			if (primaryMoleculeType == RASTER) {
			    RasterRetained geo = (RasterRetained)r.geometry();
			    renderBin.addGeometryToLockList(geo);
			    if (geo.image != null)
				renderBin.addNodeComponent(geo.image);
			}
		    }
		    else if ((r.groupType & RenderAtom.SEPARATE_DLIST_PER_GEO) != 0) {
			if (separateDlistRenderAtomList == null) {
			    separateDlistRenderAtomList = r;
			}
			else {
			    r.next = separateDlistRenderAtomList;
			    separateDlistRenderAtomList.prev = r;
			    separateDlistRenderAtomList = r;
			}
			((GeometryArrayRetained)r.geometry()).assignDlistId();
			renderBin.addGeometryDlist(r);
		    }
		    else {
			if (secondaryRenderMethod == null)
			    secondaryRenderMethod = cachedVertexArrayRenderMethod;
			if (vertexArrayRenderAtomList == null) {
			    vertexArrayRenderAtomList = r;
			}
			else {
			    r.next = vertexArrayRenderAtomList;
			    vertexArrayRenderAtomList.prev = r;
			    vertexArrayRenderAtomList = r;
			}
			// For indexed geometry there is no need to lock since
			// the mirror is changed only when the renderer is not
			// running
			// For indexed geometry, if use_coord is set, then either we
			// are using the index geometry as is or we will be unindexifying
			// on the fly, so its better to loc
			GeometryArrayRetained geo = (GeometryArrayRetained)r.geometry();
			if (!(geo instanceof IndexedGeometryArrayRetained) ||
			    ((geo.vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) != 0)) {
			    renderBin.addGeometryToLockList(geo);
			    // Add the geometry to the dirty list only if the geometry is by
			    // refernce and there is color and we need to use alpha
                            // Issue 113 - ignore multiScreen
			    if ((( geo.vertexFormat & GeometryArray.BY_REFERENCE)!=0) &&
				(geo.c4fAllocated == 0) &&
				((geo.vertexFormat & GeometryArray.COLOR) != 0) &&
				useAlpha) {
				renderBin.addDirtyReferenceGeometry(geo);
			    }
			}
		    }
		}
		addRAs = addRAs.nextAdd;
		renderAtom.nextAdd = null;
		renderAtom.prevAdd = null;
		if (renderAtom.isOriented()) {
		    renderBin.orientedRAs.add(renderAtom);

		}
		// If transparent and not in bg geometry and is depth sorted transparency
		if (!isOpaqueOrInOG && (textureBin.environmentSet.lightBin.geometryBackground == null)&&
		    (renderBin.transpSortMode == View.TRANSPARENCY_SORT_GEOMETRY)) {
		    GeometryRetained geo = null;
		    int k = 0;
		    while (geo == null && k < renderAtom.rListInfo.length) {
			geo = renderAtom.rListInfo[k].geometry();
			k++;
		    }
		    if (geo != null) {
			if (renderAtom.parentTInfo != null && renderAtom.parentTInfo[k-1] != null) {
			    renderBin.updateTransparentInfo(renderAtom);
			}
			// Newly added renderAtom
			else {
			    renderBin.addTransparentObject(renderAtom);
			}
		    }
		    // Moving within the renderBin

		}
	    }

	    if ((primaryMoleculeType &DLIST_MOLECULE) != 0 && primaryChanged) {

		// If a renderAtom is added to the display list
		// structure then add this to the dirty list of rm
		// for which the display list needs to be recreated
		renderBin.addDirtyRenderMolecule(this);
		vwcBounds.set(null);
		r = primaryRenderAtomList;
		while (r != null) {
		    vwcBounds.combine(r.renderAtom.localeVwcBounds);
		    r = r.next;
		}
		primaryChanged = false;
	    }


	    if ((onUpdateList & LOCALE_CHANGED) != 0) {
		handleLocaleChange();
	    }

	    if (locale != renderBin.locale) {
		translate();
	    }
	}
	else {
	    // The flag LOCALE_CHANGED only gets sets when there is a new additon
	    // There are cases when RM updateObject() get called (due to addition
	    // in renderBin - see processTransformChanged()), we need to
	    // evaluate locale change for this  case as well
	    if (renderBin.localeChanged) {
		handleLocaleChange();
	    }

	    if (locale != renderBin.locale) {
		translate();
	    }

            if ((onUpdateList & UPDATE_BACKGROUND_TRANSFORM) != 0) {
                i = localToVworldIndex[NodeRetained.LAST_LOCAL_TO_VWORLD];
                localeLocalToVworld[i].getRotation(infLocalToVworld[i]);
            }

	    // No new renderAtoms were added, but need to
	    // recompute vwcBounds in response to xform change
	    if ((onUpdateList & BOUNDS_RECOMPUTE_UPDATE) != 0) {
		vwcBounds.set(null);
		r = primaryRenderAtomList;
		while (r != null) {
		    vwcBounds.combine(r.renderAtom.localeVwcBounds);
		    r = r.next;
		}
	    }
	}

	// Clear all bits except the IN_DIRTY_LIST
	onUpdateList &= IN_DIRTY_RENDERMOLECULE_LIST;

	numEditingRenderAtoms = numRenderAtoms;
    }

    boolean canBeInDisplayList(GeometryRetained geo, GeometryAtom ga) {
        if (ga.source.sourceNode instanceof MorphRetained) {
            return false;
        }

	return geo.canBeInDisplayList(ga.alphaEditable);
    }

    // If dlist will be altered due to alpha or ignoreVertexColors, then don't
    // put in a separate dlist that can be shared ...
    final boolean geoNotAltered(GeometryArrayRetained geo) {
	return !(((geo.vertexFormat & GeometryArray.COLOR) != 0) &&
		 (textureBin.attributeBin.ignoreVertexColors || useAlpha));
    }

    int evalRinfoGroupType(RenderAtomListInfo r) {
	int groupType = 0;

	GeometryRetained geo = r.geometry();
	if (geo == null)
	    return groupType;

	if ((primaryMoleculeType & (COMPRESSED_MOLECULE |
				    RASTER_MOLECULE     |
				    TEXT3D_MOLECULE	|
				    ORIENTEDSHAPE3D_MOLECULE)) != 0) {
	    groupType = RenderAtom.OTHER;
	}
	else if (canBeInDisplayList(geo, r.renderAtom.geometryAtom)) {
	    // if geometry is under share group we immediate set the
	    // dlistID to something other than -1
	    if ( !((GeometryArrayRetained)geo).isShared ||
		// if we do a compiled and push the transform down to
		// Geometry, we can't share the displayList
		(r.renderAtom.geometryAtom.source.staticTransform != null)) {
		// If the molecule is already defined to be SEPARATE_DLIST_PER_RINFO_MOLECULE
		// continue adding in that mode even if it was switched back to
		// no depth sorted mode
		//		System.err.println("isOpaqueOrInOG ="+isOpaqueOrInOG+" primaryMoleculeType ="+primaryMoleculeType+" renderBin.transpSortMode ="+renderBin.transpSortMode);
		if (primaryMoleculeType == SEPARATE_DLIST_PER_RINFO_MOLECULE) {
		    groupType = RenderAtom.SEPARATE_DLIST_PER_RINFO;
		}
		else {
		    if (isOpaqueOrInOG ||
			renderBin.transpSortMode == View.TRANSPARENCY_SORT_NONE) {
			groupType = RenderAtom.DLIST;
		    }
		    else {
			groupType = RenderAtom.SEPARATE_DLIST_PER_RINFO;
		    }
		}

	    } else if (geoNotAltered((GeometryArrayRetained)r.geometry()) ) {
		groupType = RenderAtom.SEPARATE_DLIST_PER_GEO;
	    }
	    else {
		groupType = RenderAtom.VARRAY;
	    }
	}
	else {
	    groupType = RenderAtom.VARRAY;
	}
	return groupType;
    }

    /**
     * Adds the given RenderAtom to this RenderMolecule.
     */
    void addRenderAtom(RenderAtom renderAtom, RenderBin rb) {
	int i;

	renderAtom.envSet = textureBin.environmentSet;
	renderAtom.renderMolecule = this;
	renderAtom.dirtyMask &= ~RenderAtom.NEED_SEPARATE_LOCALE_VWC_BOUNDS;

        AppearanceRetained raApp = renderAtom.geometryAtom.source.appearance;

	MaterialRetained mat = (raApp == null)? null : raApp.material;
        if (!soleUser && material != mat) {
	    // no longer sole user
            material = definingMaterial;
        }

        if ((geometryType & SURFACE) != 0) {
	    PolygonAttributesRetained pgAttrs =
		(raApp == null)? null : raApp.polygonAttributes;
            if (!soleUser && polygonAttributes != pgAttrs) {
	        // no longer sole user
                polygonAttributes = definingPolygonAttributes;
            }

        }
	if ((geometryType & LINE) != 0) {
	    LineAttributesRetained lnAttrs =
		(raApp == null)? null : raApp.lineAttributes;
            if (!soleUser && lineAttributes != lnAttrs) {
	        // no longer sole user
                lineAttributes = definingLineAttributes;
            }

        }
	if ((geometryType & POINT) != 0) {
	    PointAttributesRetained pnAttrs =
		(raApp == null)? null : raApp.pointAttributes;
	    if (!soleUser && pointAttributes != pnAttrs) {
	        // no longer sole user
                pointAttributes = definingPointAttributes;
            }
        }

	ColoringAttributesRetained coAttrs =
	    (raApp == null)? null : raApp.coloringAttributes;
        if (!soleUser && coloringAttributes != coAttrs) {
	    // no longer sole user
            coloringAttributes = definingColoringAttributes;
        }

	TransparencyAttributesRetained trAttrs =
	    (raApp == null)? null : raApp.transparencyAttributes;
        if (!soleUser && transparency != trAttrs) {
	    // no longer sole user
            transparency = definingTransparency;
        }



	// If the renderAtom is being inserted first time, then evaluate
	// the groupType to determine if need separate localeVwcBounds
	if (!renderAtom.inRenderBin()) {
	    for (i = 0; i < renderAtom.rListInfo.length; i++) {
		if (renderAtom.rListInfo[i].geometry() == null)
		    continue;
		int groupType = evalRinfoGroupType(renderAtom.rListInfo[i]);
		if (groupType != RenderAtom.DLIST) {
		    renderAtom.dirtyMask |= RenderAtom.NEED_SEPARATE_LOCALE_VWC_BOUNDS;
		}
	    }
	}
	if (renderAtom.removed == this) {
	    // Remove the renderAtom from the list of removeRAs
	    // If this is at the head of the list
	    if (renderAtom == removeRAs) {
		removeRAs = renderAtom.nextRemove;
		if (removeRAs != null)
		    removeRAs.prevRemove = null;
		renderAtom.nextRemove = null;
		renderAtom.prevRemove = null;
	    }
	    // Somewhere in the middle
	    else {
		renderAtom.prevRemove.nextRemove = renderAtom.nextRemove;
		if (renderAtom.nextRemove != null)
		    renderAtom.nextRemove.prevRemove = renderAtom.prevRemove;
		renderAtom.nextRemove = null;
		renderAtom.prevRemove = null;
	    }

	    renderAtom.removed = null;
	    // Redo any dlist etc, because it has been added
	    for ( i = 0; i < renderAtom.rListInfo.length; i++) {
		if (renderAtom.rListInfo[i].geometry() == null)
		    continue;
		if ((renderAtom.rListInfo[i].groupType & RenderAtom.DLIST) != 0)
		    renderBin.addDirtyRenderMolecule(this);
		else if ((renderAtom.rListInfo[i].groupType & RenderAtom.SEPARATE_DLIST_PER_RINFO) != 0) {
		    renderBin.addDlistPerRinfo.add(renderAtom.rListInfo[i]);
		}
		else if ((renderAtom.rListInfo[i].groupType & RenderAtom.SEPARATE_DLIST_PER_GEO) != 0)
		    renderBin.addGeometryDlist(renderAtom.rListInfo[i]);

	    }
	    if (removeRAs == null)
		rb.removeRenderAtomInRMList.remove(this);
	}
	else {
	    // Add this renderAtom to the addList
	    if (addRAs == null) {
		addRAs = renderAtom;
		renderAtom.nextAdd = null;
		renderAtom.prevAdd = null;
	    }
	    else {
		renderAtom.nextAdd = addRAs;
		renderAtom.prevAdd = null;
		addRAs.prevAdd = renderAtom;
		addRAs = renderAtom;
	    }
	    renderAtom.added = this;
	    if (onUpdateList == 0)
		rb.objUpdateList.add(this);
	    onUpdateList |= NEW_RENDERATOMS_UPDATE;

	}
	if (renderBin.localeChanged && !doInfinite) {
	    if (onUpdateList == 0)
		rb.objUpdateList.add(this);
	    onUpdateList |= LOCALE_CHANGED;
	}

	// inform the texture bin that this render molecule is no longer
	// in zombie state

	if (numEditingRenderAtoms == 0) {
	    textureBin.incrActiveRenderMolecule();
	}
	numEditingRenderAtoms++;
    }

    /**
     * Removes the given RenderAtom from this RenderMolecule.
     */
    void removeRenderAtom(RenderAtom r) {

	r.renderMolecule = null;
	if (r.added == this) {
	    //Remove this renderAtom from the addRAs list

	    // If this is at the head of the list
	    if (r == addRAs) {
		addRAs = r.nextAdd;
		if (addRAs != null)
		    addRAs.prevAdd = null;
		r.nextAdd = null;
		r.prevAdd = null;
	    }
	    // Somewhere in the middle
	    else {
		r.prevAdd.nextAdd = r.nextAdd;
		if (r.nextAdd != null)
		    r.nextAdd.prevAdd = r.prevAdd;
		r.nextAdd = null;
		r.prevAdd = null;
	    }

	    r.added = null;
	    r.envSet = null;
	    // If the number of renderAtoms is zero, and it is on the
	    // update list for adding new renderatroms only (not for
	    // bounds update), then remove this rm from the update list

	    // Might be expensive to remove this entry from the renderBin
	    // objUpdateList, just let it call the renderMolecule
	    /*
	      if (addRAs == null) {
	      if (onUpdateList == NEW_RENDERATOMS_UPDATE){
	      renderBin.objUpdateList.remove(renderBin.objUpdateList.indexOf(this));
	      }
	      onUpdateList &= ~NEW_RENDERATOMS_UPDATE;
	      }
	    */

	}
	else {
	    // Add this renderAtom to the remove list
	    if (removeRAs == null) {
		removeRAs = r;
		r.nextRemove = null;
		r.prevRemove = null;
	    }
	    else {
		r.nextRemove = removeRAs;
		r.prevRemove = null;
		removeRAs.prevRemove = r;
		removeRAs = r;
	    }
	    r.removed = this;
	}

	// Add it to the removeRenderAtom List , in case the renderMolecule
	// needs to be removed
	if (!renderBin.removeRenderAtomInRMList.contains(this)) {
	    renderBin.removeRenderAtomInRMList.add(this);
	}

	// decrement the number of editing render atoms in this render molecule
	numEditingRenderAtoms--;

	// if there is no more editing render atoms, inform the texture bin
	// that this render molecule is going to zombie state

	if (numEditingRenderAtoms == 0) {
	    textureBin.decrActiveRenderMolecule();
	}
    }

    /**
     * Recalculates the vwcBounds for a RenderMolecule
     */
    void recalcBounds() {
	RenderAtomListInfo ra;

	if (primaryRenderMethod ==
	    VirtualUniverse.mc.getDisplayListRenderMethod()) {
	    vwcBounds.set(null);
	    ra = primaryRenderAtomList;
	    while (ra != null) {
		vwcBounds.combine(ra.renderAtom.localeVwcBounds);
		ra = ra.next;
	    }
	}
    }

    void evalAlphaUsage(RenderingAttributesRetained renderAttrs,
			TextureUnitStateRetained[] texUnits) {
        boolean alphaBlend, alphaTest, textureBlend = false;

        alphaBlend =
            definingTransparency != null &&
            definingTransparency.transparencyMode !=
	    TransparencyAttributes.NONE &&
	    (VirtualUniverse.mc.isD3D() ||
	     !VirtualUniverse.mc.isD3D() &&
	     definingTransparency.transparencyMode !=
	     TransparencyAttributes.SCREEN_DOOR);


	if (texUnits != null) {
	    for (int i = 0;
		 textureBlend == false && i < texUnits.length;
		 i++) {
		if (texUnits[i] != null &&
		    texUnits[i].texAttrs != null) {
		    textureBlend = textureBlend ||
            		(texUnits[i].texAttrs.textureMode ==
			 TextureAttributes.BLEND);
		}
	    }
	}

        alphaTest =
            renderAttrs != null && renderAttrs.alphaTestFunction != RenderingAttributes.ALWAYS;

	boolean oldUseAlpha = useAlpha;
        useAlpha = alphaBlend || alphaTest || textureBlend;

	if( !oldUseAlpha && useAlpha) {
	    GeometryArrayRetained geo = null;

	    if(vertexArrayRenderAtomList != null)
		geo = (GeometryArrayRetained)vertexArrayRenderAtomList.geometry();

	    if(geo != null) {
		if (!(geo instanceof IndexedGeometryArrayRetained) ||
		    ((geo.vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) != 0)) {
		    renderBin.addGeometryToLockList(geo);
		    // Add the geometry to the dirty list only if the geometry is by
		    // reference and there is color and we need to use alpha
		    // Issue 113 - ignore multiScreen
		    if ((( geo.vertexFormat & GeometryArray.BY_REFERENCE)!=0) &&
			(geo.c4fAllocated == 0) &&
			((geo.vertexFormat & GeometryArray.COLOR) != 0) &&
			useAlpha) {
			renderBin.addDirtyReferenceGeometry(geo);
		    }
		}
	    }
	}
    }

    final boolean isSwitchOn() {
 	// The switchOn status of the entire RM can be determined
	// by the switchOn status of any renderAtoms below.
        // This is possible because renderAtoms generated from a common
        // switch branch are placed in the same renderMolecule
	if (primaryRenderAtomList != null) {
	    return primaryRenderAtomList.renderAtom.geometryAtom.
		source.switchState.lastSwitchOn;

	}

	if (vertexArrayRenderAtomList != null) {
	    return vertexArrayRenderAtomList.renderAtom.geometryAtom.
		source.switchState.lastSwitchOn;

	}

	if (separateDlistRenderAtomList != null) {
	    return separateDlistRenderAtomList.renderAtom.geometryAtom.
		source.switchState.lastSwitchOn;
	}
	return false;
    }

    /**
     * Renders this RenderMolecule
     */
    boolean render(Canvas3D cv, int pass, int dirtyBits) {
        assert pass < 0;

	boolean isVisible = isSwitchOn();

	if (!isVisible) {
	    return false;
	}

	isVisible = false;

        // include this LightBin to the to-be-updated list in Canvas
        cv.setStateToUpdate(Canvas3D.RENDERMOLECULE_BIT, this);

	boolean modeSupportDL = true;
	isNonUniformScale = !trans[localToVworldIndex[NodeRetained.LAST_LOCAL_TO_VWORLD]].isCongruent();
	// We have to dynamically switch between using displaymode
	// mode or not instead of decide in canBeInDisplayList(),
	// since polygonAttribute can be change by editable Appearance
	// or editable polygonAttribute which mode we can't take
	// advantage of display list mode in many cases just because
	// there are three special cases to handle.

	// Another case for punting to vertex array is if pass specifies
	// something other than -1. That means, we are in the
	// multi-texturing multi-pass case. Then we'll use vertex array
	// instead. Or the length of the texCoordSetMap is greater than
	// the number of texture units supported by the Canvas, then
	// we'll have to punt to vertex array as well.

	if ((pass != TextureBin.USE_DISPLAYLIST) ||
	    (texCoordSetMapLen > cv.maxTexCoordSets) ||
	    (VirtualUniverse.mc.isD3D() &&
	      (((definingPolygonAttributes != null) &&
		((isQuadGeometryArray &&
		  (definingPolygonAttributes.polygonMode ==
		   PolygonAttributes.POLYGON_LINE))||
		 (isTriGeometryArray &&
		  (definingPolygonAttributes.polygonMode ==
		   PolygonAttributes.POLYGON_POINT)))) ||
	       cv.texLinearMode))) {
	    modeSupportDL = false;
	}

	/*
	System.err.println("texCoord " + texCoordSetMapLen + " " +
			   cv.maxTexCoordSets + " " + modeSupportDL);

	System.err.println("primaryMoleculeType = "+primaryMoleculeType+" primaryRenderAtomList ="+primaryRenderAtomList+" separateDlistRenderAtomList ="+separateDlistRenderAtomList+" vertexArrayRenderAtomList ="+vertexArrayRenderAtomList);
	*/
	// Send down the model view only once, if its not of type text
	if ((primaryMoleculeType & (TEXT3D_MOLECULE| ORIENTEDSHAPE3D_MOLECULE)) == 0) {

	    if (primaryRenderAtomList != null) {
		if ((primaryRenderMethod != VirtualUniverse.mc.getDisplayListRenderMethod()) ||
		    modeSupportDL) {
		    if (primaryMoleculeType != SEPARATE_DLIST_PER_RINFO_MOLECULE) {

			if (primaryRenderMethod.render(this, cv, primaryRenderAtomList,dirtyBits))
			    isVisible = true;
		    }
		    else {
			if (renderBin.dlistRenderMethod.renderSeparateDlistPerRinfo(this, cv, primaryRenderAtomList,dirtyBits))
			    isVisible = true;

		    }
		} else {
		    if(cachedVertexArrayRenderMethod.render(this, cv,
							    primaryRenderAtomList,
							    dirtyBits)) {
			isVisible = true;
		    }
		}
	    }
	}
	else {	// TEXT3D or ORIENTEDSHAPE3D

	    if (primaryRenderAtomList != null) {
		if(primaryRenderMethod.render(this, cv, primaryRenderAtomList,
					      dirtyBits)) {
		    isVisible = true;
		}
	    }
	}

	if (separateDlistRenderAtomList != null) {
            if (modeSupportDL) {
                if(renderBin.dlistRenderMethod.renderSeparateDlists(this, cv,
                        separateDlistRenderAtomList,
                        dirtyBits)) {
                    isVisible = true;
                }

	    } else {
		if(cachedVertexArrayRenderMethod.render(this, cv,
							separateDlistRenderAtomList,
							dirtyBits)) {
		    isVisible = true;
		}
	    }

	}

	// XXXX: In the case of independent primitives such as quads,
	// it would still be better to call multi draw arrays
	if (vertexArrayRenderAtomList != null) {
	    if(cachedVertexArrayRenderMethod.render(this, cv,
						    vertexArrayRenderAtomList,
						    dirtyBits)) {
		isVisible = true;
	    }
	}
	return isVisible;
    }

    void updateAttributes(Canvas3D cv, int dirtyBits) {


	boolean setTransparency = false;

	// If this is a beginning of a frame OR diff. geometryType
	// then reload everything for the first rendermolecule
	//	System.err.println("updateAttributes");
	int bitMask = geometryType | Canvas3D.MATERIAL_DIRTY|
	    Canvas3D.COLORINGATTRS_DIRTY|
	    Canvas3D.TRANSPARENCYATTRS_DIRTY;

	// If beginning of a frame then reload all the attributes
	if ((cv.canvasDirty & bitMask) != 0) {
	    if ((geometryType & SURFACE) != 0) {
		if (definingPolygonAttributes == null) {
		    cv.resetPolygonAttributes(cv.ctx);
		} else {
		    definingPolygonAttributes.updateNative(cv.ctx);
		}
		cv.polygonAttributes = polygonAttributes;
	    }
	    if ((geometryType & LINE) != 0) {
		if (definingLineAttributes == null) {
		    cv.resetLineAttributes(cv.ctx);
		} else {
		    definingLineAttributes.updateNative(cv.ctx);
		}
		cv.lineAttributes = lineAttributes;
	    }
	    if ((geometryType & POINT) != 0) {
		if (definingPointAttributes == null) {
		    cv.resetPointAttributes(cv.ctx);
		} else {
		    definingPointAttributes.updateNative(cv.ctx);
		}
		cv.pointAttributes = pointAttributes;
	    }

	    if (definingTransparency == null) {
		cv.resetTransparency(cv.ctx, geometryType,
				     polygonMode, lineAA, pointAA);
	    } else {
		definingTransparency.updateNative(cv.ctx,
						  alpha, geometryType,
						  polygonMode, lineAA,
						  pointAA);
	    }
	    cv.transparency = transparency;

	    if (definingMaterial == null) {
		cv.updateMaterial(cv.ctx, red, green, blue, alpha);
	    } else {
		definingMaterial.updateNative(cv.ctx,
					      red, green, blue, alpha,
					      enableLighting);
	    }
	    cv.material = material;
	    cv.enableLighting = enableLighting;

	    if (definingColoringAttributes == null) {
		cv.resetColoringAttributes(cv.ctx, red, green, blue,
					   alpha, enableLighting);
	    } else {
		definingColoringAttributes.updateNative(cv.ctx,
							dRed,
							dBlue,
							dGreen,alpha,
							enableLighting);
	    }
	    cv.coloringAttributes = coloringAttributes;

            // Use Object instead of AppearanceRetained class for
            // state caching optimation for memory performance
            cv.appHandle = appHandle;
	}

        // assuming neighbor dirty bits ORing is implemented
        // note that we need to set it to ALL_DIRTY at the
        // begining of textureBin first and only do the ORing
        // whenever encounter a non-visible rm

	else if (cv.renderMolecule != this && (dirtyBits != 0)) {

	    // no need to download states if appHandle is the same
	    if (cv.appHandle != appHandle) {

		// Check if the attribute bundle in the canvas is the same
		// as the attribute bundle in this renderMolecule

		if (cv.transparency != transparency &&
		    (dirtyBits & TRANSPARENCY_DIRTY) != 0) {
		    setTransparency = true;
		    if (definingTransparency == null) {

			cv.resetTransparency(cv.ctx, geometryType,
					     polygonMode, lineAA, pointAA);
		    } else {
			definingTransparency.updateNative(cv.ctx, alpha,
							  geometryType, polygonMode,
							  lineAA, pointAA);
		    }
		    cv.transparency = transparency;
		}

		if (setTransparency || ((cv.enableLighting != enableLighting) ||
					(cv.material != material) &&
					(dirtyBits & MATERIAL_DIRTY) != 0)){
		    if (definingMaterial == null) {
			cv.updateMaterial(cv.ctx, red, green, blue, alpha);
		    } else {
			definingMaterial.updateNative(cv.ctx, red, green,
						      blue, alpha,
						      enableLighting);
		    }
		    cv.material = material;
		    cv.enableLighting = enableLighting;
		}

		if (((geometryType & SURFACE) != 0) &&
		    cv.polygonAttributes != polygonAttributes &&
		    (dirtyBits & POLYGONATTRS_DIRTY) != 0) {

		    if (definingPolygonAttributes == null) {
			cv.resetPolygonAttributes(cv.ctx);
		    } else {
			definingPolygonAttributes.updateNative(cv.ctx);
		    }
		    cv.polygonAttributes = polygonAttributes;
		}

		if (((geometryType & LINE) != 0) &&
		    cv.lineAttributes != lineAttributes &&
		    (dirtyBits & LINEATTRS_DIRTY) != 0) {

		    if (definingLineAttributes == null) {
			cv.resetLineAttributes(cv.ctx);
		    } else {
			definingLineAttributes.updateNative(cv.ctx);
		    }
		    cv.lineAttributes = lineAttributes;
		}

		if (((geometryType & POINT) != 0) &&
		    cv.pointAttributes != pointAttributes &&
		    (dirtyBits & POINTATTRS_DIRTY) != 0) {

		    if (definingPointAttributes == null) {
			cv.resetPointAttributes(cv.ctx);
		    } else {
			definingPointAttributes.updateNative(cv.ctx);
		    }
		    cv.pointAttributes = pointAttributes;
		}

		// Use Object instead of AppearanceRetained class for
		// state caching optimation for memory performance
		cv.appHandle = appHandle;
	    }
	    // no state caching for color attrs, which can also be
	    // changed by primitive with colors
	    if(setTransparency || ((dirtyBits & COLORINGATTRS_DIRTY) != 0)) {

		if (definingColoringAttributes == null) {
		    cv.resetColoringAttributes(cv.ctx,
					       red, green, blue, alpha,
					       enableLighting);
		} else {
		    definingColoringAttributes.updateNative(cv.ctx,
							    dRed,
							    dBlue,
							    dGreen,alpha,
							    enableLighting);

		}
                cv.coloringAttributes = coloringAttributes;
	    }

	}

	if ((primaryMoleculeType & (TEXT3D_MOLECULE| ORIENTEDSHAPE3D_MOLECULE)) == 0) {
	    /* System.err.println("updateAttributes  setModelViewMatrix (1)"); */

	    Transform3D modelMatrix =
	        trans[localToVworldIndex[NodeRetained.LAST_LOCAL_TO_VWORLD]];

	    if (cv.modelMatrix != modelMatrix) {
		/* System.err.println("updateAttributes  setModelViewMatrix (2)"); */

		cv.setModelViewMatrix(cv.ctx, cv.vworldToEc.mat,
				      modelMatrix);
	    }
	}

	cv.canvasDirty &= ~bitMask;
	cv.renderMolecule = this;
    }

    void transparentSortRender(Canvas3D cv, int pass, TransparentRenderingInfo tinfo) {
        assert pass < 0;

	Transform3D modelMatrix =
	    trans[localToVworldIndex[NodeRetained.LAST_LOCAL_TO_VWORLD]];

        // include this LightBin to the to-be-updated list in Canvas
        cv.setStateToUpdate(Canvas3D.RENDERMOLECULE_BIT, this);


	boolean modeSupportDL = true;

	// We have to dynamically switch between using displaymode
	// mode or not instead of decide in canBeInDisplayList(),
	// since polygonAttribute can be change by editable Appearance
	// or editable polygonAttribute which mode we can't take
	// advantage of display list mode in many cases just because
	// there are three special cases to handle.

        // Another case for punting to vertex array is if pass specifies
        // something other than -1. That means, we are in the
        // multi-texturing multi-pass case. Then we'll use vertex array
        // instead.

	if ((pass != TextureBin.USE_DISPLAYLIST) ||
	    (texCoordSetMapLen > cv.maxTexCoordSets) ||
                       	     (VirtualUniverse.mc.isD3D() &&
			     (((definingPolygonAttributes != null) &&
			       ((isQuadGeometryArray &&
				 (definingPolygonAttributes.polygonMode ==
				  PolygonAttributes.POLYGON_LINE)) ||
				(isTriGeometryArray &&
				 (definingPolygonAttributes.polygonMode ==
				  PolygonAttributes.POLYGON_POINT))))
			      ||
			      cv.texLinearMode))) {
	    modeSupportDL = false;
	}

	//	System.err.println("r.isOpaque = "+isOpaque+" rinfo = "+tinfo.rInfo+" groupType = "+tinfo.rInfo.groupType);
	// Only support individual dlist or varray
	// If this rInfo is a part of a bigger dlist, render as VA
	// XXXX: What to do with Text3D, Raster, CG?
	if ((tinfo.rInfo.groupType & RenderAtom.SEPARATE_DLIST_PER_RINFO) != 0) {
	    RenderAtomListInfo save= tinfo.rInfo.next;
	    // Render only one geometry
	    tinfo.rInfo.next = null;
	    //	    System.err.println("cachedVertexArrayRenderMethod = "+cachedVertexArrayRenderMethod);
	    //	    System.err.println("tinfo.rInfo = "+tinfo.rInfo);
	    if (modeSupportDL) {
		renderBin.dlistRenderMethod.renderSeparateDlistPerRinfo(this, cv,
									tinfo.rInfo,
									ALL_DIRTY_BITS);
	    }
	    else {
		cachedVertexArrayRenderMethod.render(this, cv, tinfo.rInfo,ALL_DIRTY_BITS);
	    }
	    tinfo.rInfo.next = save;
	}
	else if ((tinfo.rInfo.groupType & (RenderAtom.VARRAY| RenderAtom.DLIST)) != 0) {
	    RenderAtomListInfo save= tinfo.rInfo.next;
	    // Render only one geometry
	    tinfo.rInfo.next = null;
	    //	    System.err.println("cachedVertexArrayRenderMethod = "+cachedVertexArrayRenderMethod);
	    //	    System.err.println("tinfo.rInfo = "+tinfo.rInfo);
	    cachedVertexArrayRenderMethod.render(this, cv, tinfo.rInfo,
						 ALL_DIRTY_BITS);
	    tinfo.rInfo.next = save;
	}

	// Only support individual dlist or varray
	else if ((tinfo.rInfo.groupType & RenderAtom.SEPARATE_DLIST_PER_GEO) != 0) {
	    RenderAtomListInfo save= tinfo.rInfo.next;
	    tinfo.rInfo.next = null;
	    if (modeSupportDL) {
		renderBin.dlistRenderMethod.renderSeparateDlists(this, cv,
								 tinfo.rInfo,
								 ALL_DIRTY_BITS);
	    }
	    else {
		cachedVertexArrayRenderMethod.render(this, cv, tinfo.rInfo,
						     ALL_DIRTY_BITS);
	    }
	    tinfo.rInfo.next = save;
	}
	else {
	    RenderAtomListInfo save= tinfo.rInfo.next;
	    primaryRenderMethod.render(this, cv, primaryRenderAtomList,
				       ALL_DIRTY_BITS);
	    tinfo.rInfo.next = save;
	}

    }


    /**
     * This render method is used to render the transparency attributes.
     * It is used in the multi-texture multi-pass case to reset the
     * transparency attributes to what it was
     */
    void updateTransparencyAttributes(Canvas3D cv) {
	if (definingTransparency == null) {
	    cv.resetTransparency(cv.ctx, geometryType, polygonMode,
				 lineAA, pointAA);
	} else {
	    definingTransparency.updateNative(cv.ctx, alpha, geometryType,
					      polygonMode, lineAA, pointAA);
	}
    }

    void updateDisplayList(Canvas3D cv) {
	// This function only gets called when primaryRenderAtomsList are
	if (primaryRenderAtomList != null) {
	    ((DisplayListRenderMethod)primaryRenderMethod).buildDisplayList(this, cv);
	}
    }

    void releaseAllPrimaryDisplayListID() {

	if (primaryRenderAtomList != null) {
	    if (primaryMoleculeType == SEPARATE_DLIST_PER_RINFO_MOLECULE) {
		RenderAtomListInfo ra = primaryRenderAtomList;
		int id;

		while (ra != null) {
		    id = ra.renderAtom.dlistIds[ra.index];

		    if (id > 0) {
			VirtualUniverse.mc.freeDisplayListId(new Integer(id));
			ra.renderAtom.dlistIds[ra.index] = -1;
		    }
		    ra = ra.next;
		}
	    }
	    else if (primaryMoleculeType == DLIST_MOLECULE) {
		if (displayListIdObj != null) {
		    VirtualUniverse.mc.freeDisplayListId(displayListIdObj);
		    displayListIdObj = null;
		    displayListId = -1;
		}
	    }
	}

    }

    void releaseAllPrimaryDisplayListResources(Canvas3D cv, Context ctx) {
	if (primaryRenderAtomList != null) {
	    if (primaryMoleculeType == SEPARATE_DLIST_PER_RINFO_MOLECULE) {
		RenderAtomListInfo ra = primaryRenderAtomList;
		int id;
		while (ra != null) {
		    id = ra.renderAtom.dlistIds[ra.index];
		    if (id > 0) {
			cv.freeDisplayList(ctx, id);
		    }
		    ra = ra.next;
		}
	    }
	    else if (primaryMoleculeType == DLIST_MOLECULE) {
		if (displayListId > 0) {
		    cv.freeDisplayList(ctx, displayListId);
		}
	    }
	}
    }

    void updateAllPrimaryDisplayLists(Canvas3D cv) {
	// This function only gets called when primaryRenderAtomsList are
	if (primaryRenderAtomList != null) {
	    if (primaryMoleculeType == SEPARATE_DLIST_PER_RINFO_MOLECULE) {
		RenderAtomListInfo ra = primaryRenderAtomList;
		while (ra != null) {
		    renderBin.dlistRenderMethod.buildDlistPerRinfo(ra, this, cv);
		    ra = ra.next;
		}
	    }
	    else if(primaryMoleculeType == DLIST_MOLECULE) {
		((DisplayListRenderMethod)primaryRenderMethod).buildDisplayList(this, cv);
	    }
	}
    }

    void checkEquivalenceWithBothNeighbors(int dirtyBits) {
	dirtyAttrsAcrossRms = ALL_DIRTY_BITS;

	if (prev != null) {
	    checkEquivalenceWithLeftNeighbor(prev, dirtyBits);
	}
	if (next != null) {
	    next.checkEquivalenceWithLeftNeighbor(this, dirtyBits);
	}
    }

    boolean reloadColor(RenderMolecule rm) {
	if (((rm.vertexFormat & GeometryArray.COLOR) == 0) ||
	    (((rm.vertexFormat & GeometryArray.COLOR) != 0) &&
	     (vertexFormat & GeometryArray.COLOR) != 0)) {
	    return false;
	}
	return true;
    }

    void checkEquivalenceWithLeftNeighbor(RenderMolecule rm, int dirtyBits) {
	boolean reload_color = reloadColor(rm);
	// XXXX : For now ignore the dirtyBits being sent in
	dirtyAttrsAcrossRms = ALL_DIRTY_BITS ;



	// There is some interdepenency between the different components
	// in the way it is sent down to the native code
	// Material is affected by transparency and coloring attrs
	// Transparency is affected by poly/line/pointAA
	// ColoringAttrs is affected by material and transaparency
	int materialColoringDirty = (MATERIAL_DIRTY |
				     TRANSPARENCY_DIRTY |
				     COLORINGATTRS_DIRTY);

	int transparencyDirty = (TRANSPARENCY_DIRTY|
				 POLYGONATTRS_DIRTY |
				 LINEATTRS_DIRTY |
				 POINTATTRS_DIRTY);

	if ((dirtyAttrsAcrossRms & POLYGONATTRS_DIRTY) != 0) {
	    if (rm.geometryType == geometryType &&
		(rm.polygonAttributes == polygonAttributes ||
		 ((rm.definingPolygonAttributes != null) &&
		  (rm.definingPolygonAttributes.equivalent(definingPolygonAttributes)))))
		dirtyAttrsAcrossRms &= ~POLYGONATTRS_DIRTY;

	}

	if ((dirtyAttrsAcrossRms & POINTATTRS_DIRTY) != 0) {
	    if (rm.geometryType == geometryType &&
		((rm.pointAttributes == pointAttributes) ||
		 ((rm.definingPointAttributes != null) &&
		  (rm.definingPointAttributes.equivalent(definingPointAttributes)))))
		dirtyAttrsAcrossRms &= ~POINTATTRS_DIRTY;

	}

	if ((dirtyAttrsAcrossRms & LINEATTRS_DIRTY) != 0) {
	    if (rm.geometryType == geometryType &&
		((rm.lineAttributes == lineAttributes) ||
		 ((rm.definingLineAttributes != null) &&
		  (rm.definingLineAttributes.equivalent(definingLineAttributes)))))
		dirtyAttrsAcrossRms &= ~LINEATTRS_DIRTY;
	}

	if ((dirtyAttrsAcrossRms & materialColoringDirty) != 0) {
	    if (materialEquivalent(rm, reload_color)) {
		dirtyAttrsAcrossRms &= ~MATERIAL_DIRTY;
	    }
	    else {
		dirtyAttrsAcrossRms |= MATERIAL_DIRTY;
	    }
	}




	if ((dirtyAttrsAcrossRms & materialColoringDirty) != 0) {
	    if (coloringEquivalent(rm, reload_color)) {
		dirtyAttrsAcrossRms &= ~COLORINGATTRS_DIRTY;
	    }
	    else {
		dirtyAttrsAcrossRms |= COLORINGATTRS_DIRTY;
	    }
	}

	if ((dirtyAttrsAcrossRms & transparencyDirty) != 0) {
	    if (transparencyEquivalent(rm)) {
		dirtyAttrsAcrossRms &= ~TRANSPARENCY_DIRTY;
	    }
	    else {
		dirtyAttrsAcrossRms |= TRANSPARENCY_DIRTY;
	    }
	}
    }
    void translate() {
	//	System.err.println("onUpdateList = "+onUpdateList+" renderBin.localeChanged = "+renderBin.localeChanged+" rm = "+this);
	int i = localToVworldIndex[NodeRetained.LAST_LOCAL_TO_VWORLD];

	localeLocalToVworld[i].mat[0] = localToVworld[i].mat[0];
	localeLocalToVworld[i].mat[1] = localToVworld[i].mat[1];
	localeLocalToVworld[i].mat[2] = localToVworld[i].mat[2];
	localeLocalToVworld[i].mat[3] = localToVworld[i].mat[3] + localeTranslation.x ;
	localeLocalToVworld[i].mat[4] = localToVworld[i].mat[4];
	localeLocalToVworld[i].mat[5] = localToVworld[i].mat[5];
	localeLocalToVworld[i].mat[6] = localToVworld[i].mat[6];
	localeLocalToVworld[i].mat[7] = localToVworld[i].mat[7]+ localeTranslation.y;
	localeLocalToVworld[i].mat[8] = localToVworld[i].mat[8];
	localeLocalToVworld[i].mat[9] = localToVworld[i].mat[9];
	localeLocalToVworld[i].mat[10] = localToVworld[i].mat[10];
	localeLocalToVworld[i].mat[11] = localToVworld[i].mat[11]+ localeTranslation.z;
	localeLocalToVworld[i].mat[12] = localToVworld[i].mat[12];
	localeLocalToVworld[i].mat[13] = localToVworld[i].mat[13];
	localeLocalToVworld[i].mat[14] = localToVworld[i].mat[14];
	localeLocalToVworld[i].mat[15] = localToVworld[i].mat[15];
	//	System.err.println("rm = "+this+" localTovworld = "+localeLocalToVworld[i]+" localeTranslation = "+localeTranslation);
    }


    boolean isOpaque() {
    if ((geometryType & SURFACE) != 0) {
	if (definingPolygonAttributes != null) {
	    if ((definingPolygonAttributes.polygonMode ==
		 PolygonAttributes.POLYGON_POINT) &&
		(definingPointAttributes != null) &&
		definingPointAttributes.pointAntialiasing) {
		return false;
	    } else if ((definingPolygonAttributes.polygonMode ==
			PolygonAttributes.POLYGON_LINE) &&
		       (definingLineAttributes != null) &&
		       definingLineAttributes.lineAntialiasing) {
		return false;
	    }
	}
        } else if ((geometryType & POINT) != 0) {
	if ((definingPointAttributes != null) &&
	    definingPointAttributes.pointAntialiasing) {
	    return false;
	}
    } else if ((geometryType & LINE) != 0) {
	if ((definingLineAttributes != null) &&
	    definingLineAttributes.lineAntialiasing) {
	    return false;
	}
    }
    return ((definingTransparency == null) ||
	    (definingTransparency.transparencyMode ==
	     TransparencyAttributes.NONE) ||
	    (definingTransparency.transparencyMode ==
	     TransparencyAttributes.SCREEN_DOOR));
    }


    boolean updateNodeComponent() {
	//	System.err.println("soleUser = "+soleUser+" rm = "+this);
	if ((soleUserCompDirty & MATERIAL_DIRTY) != 0) {
	    // Note: this RM is a soleUser(only then this function is called)
	    // and if definingMaterial == material, then the material is freq
	    // changed and therefore is not cloned, only other time it can be
	    // same is when an equivalent material is added in and this can
	    // never be true when a bin is a soleUser of a appearance

	    // Evaluate before replacing the old Value
	    if (soleUser) {
		boolean cloned = definingMaterial != null && definingMaterial != material;
		//		System.err.println("===>Rm = "+this);

		//		System.err.println("===> updating node component, cloned = "+cloned+" material.changedFrequent = "+material.changedFrequent);
		//		System.err.println("===> definingMaterial ="+definingMaterial+" material = "+material);

		material = ((AppearanceRetained)appHandle).material;
		if (material == null)
		    definingMaterial = null;
		else {
		    if (material.changedFrequent != 0) {
			definingMaterial = material;
		    }
		    else {
			// If the one replaced is a cloned copy, then ..
			if (cloned) {
			    definingMaterial.set(material);
			}
			else {
			    definingMaterial = (MaterialRetained)material.clone();
			}
		    }
		}
	    }
	    evalMaterialCachedState();
	}
	if ((soleUserCompDirty & LINEATTRS_DIRTY) != 0) {
	    if (soleUser) {
		// Evaluate before replacing the old Value
		boolean cloned = definingLineAttributes != null && definingLineAttributes != lineAttributes;

		lineAttributes = ((AppearanceRetained)appHandle).lineAttributes;
		if (lineAttributes == null) {
		    lineAA = false;
		    definingLineAttributes = null;
		} else {
		    if (lineAttributes.changedFrequent != 0) {
			definingLineAttributes = lineAttributes;
		    }
		    else {
			// If the one replaced is a cloned copy, then ..
			if (cloned) {
			    definingLineAttributes.set(lineAttributes);
			}
			else {
			    definingLineAttributes = (LineAttributesRetained)lineAttributes.clone();
			}
		    }
		    lineAA = definingLineAttributes.lineAntialiasing;
		}
	    }
	    else {
		lineAA = definingLineAttributes.lineAntialiasing;
	    }
	}
	if ((soleUserCompDirty & POINTATTRS_DIRTY) != 0) {
	    if (soleUser) {
		// Evaluate before replacing the old Value
		boolean cloned = definingPointAttributes != null && definingPointAttributes != pointAttributes;

		pointAttributes = ((AppearanceRetained)appHandle).pointAttributes;
		if (pointAttributes == null) {
		    pointAA = false;
		    definingPointAttributes = null;
		} else {
		    if (pointAttributes.changedFrequent != 0) {
			definingPointAttributes = pointAttributes;
		    }
		    else {
			// If the one replaced is a cloned copy, then ..
			if (cloned) {
			    definingPointAttributes.set(pointAttributes);
			}
			else {
			    definingPointAttributes = (PointAttributesRetained)pointAttributes.clone();
			}
		    }
		    pointAA = definingPointAttributes.pointAntialiasing;
		}
	    }
	    else {
		pointAA = definingPointAttributes.pointAntialiasing;
	    }

	}
	if ((soleUserCompDirty & POLYGONATTRS_DIRTY) != 0) {
	    if (soleUser) {
		// Evaluate before replacing the old Value
		boolean cloned = definingPolygonAttributes != null && definingPolygonAttributes != polygonAttributes;


		polygonAttributes = ((AppearanceRetained)appHandle).polygonAttributes;

		if (polygonAttributes == null) {
		    polygonMode =  PolygonAttributes.POLYGON_FILL;
		    definingPolygonAttributes = null;
		} else {
		    if (polygonAttributes.changedFrequent != 0) {
			definingPolygonAttributes = polygonAttributes;
		    }
		    else {
			// If the one replaced is a cloned copy, then ..
			if (cloned) {
			    definingPolygonAttributes.set(polygonAttributes);
			}
			else {
			    definingPolygonAttributes = (PolygonAttributesRetained)polygonAttributes.clone();
			}
		    }

		    polygonMode = definingPolygonAttributes.polygonMode;
		}
	    }
	    else {
		polygonMode = definingPolygonAttributes.polygonMode;
	    }

	    if (polygonMode == PolygonAttributes.POLYGON_LINE) {
		geometryType |= LINE;
            } else if (polygonMode == PolygonAttributes.POLYGON_POINT) {
		geometryType |= POINT;
	    }
	}

	if ((soleUserCompDirty & TRANSPARENCY_DIRTY) != 0) {
	    if (soleUser) {
		// Evaluate before replacing the old Value
		boolean cloned = definingTransparency != null && definingTransparency != transparency;
		transparency = ((AppearanceRetained)appHandle).transparencyAttributes;

		if (transparency == null) {
		    alpha = 1.0f ;
		    definingTransparency = null;
		} else {
		    if (transparency.changedFrequent != 0) {
			definingTransparency = transparency;
		    }
		    else {
			// If the one replaced is a cloned copy, then ..
			if (cloned) {
			    definingTransparency.set(transparency);
			}
			else {
			    definingTransparency = (TransparencyAttributesRetained)transparency.clone();
			}
		    }

		    alpha = 1.0f - definingTransparency.transparency;
		}
	    }
	    else {
		alpha = 1.0f - definingTransparency.transparency;
	    }
	}

	if ((soleUserCompDirty & COLORINGATTRS_DIRTY) != 0) {
	    if (soleUser) {
		// Evaluate before replacing the old Value
		boolean cloned = definingColoringAttributes != null && definingColoringAttributes != coloringAttributes;

		coloringAttributes = ((AppearanceRetained)appHandle).coloringAttributes;
		//		System.err.println("coloringAttributes and soleUser");
		//		System.err.println("coloringAttributes ="+coloringAttributes);
		if (coloringAttributes == null) {
		    definingColoringAttributes = null;
		    red = 1.0f;
		    green = 1.0f;
		    blue = 1.0f;
		} else {
		    //		    System.err.println("coloringAttributes.changedFrequent  = "+coloringAttributes.changedFrequent );
		    if (coloringAttributes.changedFrequent != 0) {
			definingColoringAttributes = coloringAttributes;
		    }
		    else {
			// If the one replaced is a cloned copy, then ..
			if (cloned) {
			    definingColoringAttributes.set(coloringAttributes);
			}
			else {
			    definingColoringAttributes = (ColoringAttributesRetained)coloringAttributes.clone();
			}
		    }
		    red = definingColoringAttributes.color.x;
		    green = definingColoringAttributes.color.y;
		    blue = definingColoringAttributes.color.z;
		}
	    }
	    else {
		red = definingColoringAttributes.color.x;
		green = definingColoringAttributes.color.y;
		blue = definingColoringAttributes.color.z;
	    }
	}
	//	System.err.println("rm = "+this+"red = "+red+" green = "+green+" blue = "+blue);
	boolean newVal = isOpaque() || inOrderedGroup;
	return (isOpaqueOrInOG != newVal);

    }

    // Issue 129: method to add or remove all rendering atoms in this
    // RenderMolecule to or from the transparent info list when we are
    // in depth sorted transparency mode and the RenderMolecule
    // changes from opaque to transparent or vice versa.
    void addRemoveTransparentObject(RenderBin renderBin, boolean add) {
	addRemoveTransparentObject(renderBin, add, primaryRenderAtomList);
	addRemoveTransparentObject(renderBin, add, separateDlistRenderAtomList);
	addRemoveTransparentObject(renderBin, add, vertexArrayRenderAtomList);
    }

    private void addRemoveTransparentObject(RenderBin renderBin,
					    boolean add,
					    RenderAtomListInfo rinfo) {
	while (rinfo != null) {
	    if (add) {
		renderBin.addTransparentObject(rinfo.renderAtom);
	    }
	    else {
		renderBin.removeTransparentObject(rinfo.renderAtom);
	    }
	    rinfo = rinfo.next;
	}
    }

    void evalMaterialCachedState() {
	if (definingMaterial == null) {
	    enableLighting = false;;
	    definingMaterial = null;
	    dRed = 1.0f;
	    dGreen = 1.0f;
	    dBlue = 1.0f;
	}
	else {
	    if ((geometryType & RASTER) != 0) {
		enableLighting = false;
		dRed = 1.0f;
		dGreen = 1.0f;
		dBlue = 1.0f;
	    } else {
		if (normalPresent)
		    enableLighting = definingMaterial.lightingEnable;
		else
		    enableLighting = false;
		dRed = definingMaterial.diffuseColor.x;
		dGreen = definingMaterial.diffuseColor.y;
		dBlue = definingMaterial.diffuseColor.z;
	    }
	}
    }


    void markBitsAsDirty(int leftBits, int rightBits) {
	if (prev != null) {
	    checkEquivalenceWithLeftNeighbor(prev, leftBits);
	    prev.soleUserCompDirty &= ~ALL_DIRTY_BITS;
	}
	else if (prevMap != null) {
	    checkEquivalenceWithLeftNeighbor(prevMap, leftBits);
	    prevMap.soleUserCompDirty &= ~ALL_DIRTY_BITS;
	}
	if (next != null) {
	    if ((next.soleUserCompDirty & ALL_DIRTY_BITS) == 0) {
		next.checkEquivalenceWithLeftNeighbor(this, rightBits);
	    } else {
		next.soleUserCompDirty = rightBits;
	    }
	}
	else if (nextMap != null) {
	    if ((nextMap.soleUserCompDirty & ALL_DIRTY_BITS) == 0) {
		nextMap.checkEquivalenceWithLeftNeighbor(this, rightBits);
	    } else {
		nextMap.soleUserCompDirty = rightBits;
	    }
	}

    }

    void handleMaterialEquivalence() {
	// Check if it has equivalent material to any of the "non-dirty"
	// renderMolecules before this one
	RenderMolecule curPrevRm = null;
	RenderMolecule curNextRm = null;
	boolean found = false;
	int leftBits = ALL_DIRTY_BITS;
	int rightBits = ALL_DIRTY_BITS;
	if (prev != null) {
	    curPrevRm = prev.prev;
	    if (materialEquivalent(prev, reloadColor(prev))) {
		found = true;
		leftBits = (((soleUserCompDirty | prev.soleUserCompDirty) &ALL_DIRTY_BITS) & ~MATERIAL_DIRTY);
		rightBits = (soleUserCompDirty & ALL_DIRTY_BITS);
		markBitsAsDirty(leftBits, rightBits);
	    }
	}
	else if (!found && next != null) {
	    curNextRm = next.next;

	    if (materialEquivalent(next, reloadColor(next))) {
		found = true;
		int bits = 0;
		if (prev != null)
		    bits = prev.soleUserCompDirty;
		else if (prevMap != null)
		    bits = prevMap.soleUserCompDirty;

		leftBits = ((soleUserCompDirty |bits) &ALL_DIRTY_BITS);
		rightBits = ((soleUserCompDirty & ALL_DIRTY_BITS)  & ~MATERIAL_DIRTY);
		markBitsAsDirty(leftBits, rightBits);

	    }
	}
	// try place it next to a equivalent material on the left
	while (!found && curPrevRm != null) {
	    if (materialEquivalent(curPrevRm, reloadColor(curPrevRm))) {
		found = true;
		// Remove the renderMolecule from it place
		prev.next = next;
		prev.nextMap = nextMap;
		if (next != null) {
		    next.prev = prev;
		    if ((next.soleUserCompDirty & ALL_DIRTY_BITS) == 0) {
			next.checkEquivalenceWithLeftNeighbor(prev, ALL_DIRTY_BITS);
		    }
		    else {
			next.soleUserCompDirty = ALL_DIRTY_BITS;
		    }
		}
		else if (nextMap != null) {
		    nextMap.prevMap = prev;
		    if ((nextMap.soleUserCompDirty & ALL_DIRTY_BITS) == 0) {
			nextMap.checkEquivalenceWithLeftNeighbor(prev,ALL_DIRTY_BITS);
		    }
		    else {
			nextMap.soleUserCompDirty |= ALL_DIRTY_BITS;
		    }
		}

		// Insert it after the equivalent RM
		next = curPrevRm.next;
		nextMap = curPrevRm.nextMap;
		curPrevRm.nextMap = null;
		if (next != null) {
		    next.prev = this;
		}
		else if (nextMap != null) {
		    nextMap.prevMap = this;
		}
		prev = curPrevRm;
		curPrevRm.next = this;
		leftBits = (ALL_DIRTY_BITS & ~MATERIAL_DIRTY);
		markBitsAsDirty(leftBits, ALL_DIRTY_BITS);
	    }
	    curPrevRm = curPrevRm.prev;
	}

	// Check if it has equivalent material to any of the renderMolecules after
	// this one
	while (!found && curNextRm != null) {
	    if (materialEquivalent(curNextRm, reloadColor(curNextRm))) {
		found = true;
		// switch the pointers
		next.prev = prev;
		next.prevMap = prevMap;
		if (prev != null) {
		    prev.next = next;
		    if ((next.soleUserCompDirty & ALL_DIRTY_BITS) == 0) {
			next.checkEquivalenceWithLeftNeighbor(prev, ALL_DIRTY_BITS);
		    }
		    else {
			next.soleUserCompDirty = ALL_DIRTY_BITS;
		    }
		}
		else if (prevMap != null) {
		    prevMap.nextMap = next;
		    if ((next.soleUserCompDirty & ALL_DIRTY_BITS) == 0) {
			next.checkEquivalenceWithLeftNeighbor(prevMap, ALL_DIRTY_BITS);
		    }
		    else {
			next.soleUserCompDirty = ALL_DIRTY_BITS;
		    }
		}

		// Insert it before the equivalent RM
		prev = curNextRm.prev;
		prevMap = curNextRm.prevMap;
		curNextRm.prevMap = null;
		if (curNextRm.prev != null) {
		    curNextRm.prev.next = this;
		}
		else if (prevMap != null) {
		    prevMap.nextMap = this;
		}
		next = curNextRm;
		curNextRm.prev = this;
		rightBits =  (ALL_DIRTY_BITS & ~MATERIAL_DIRTY);
		markBitsAsDirty(ALL_DIRTY_BITS, rightBits);
	    }
	    curNextRm = curNextRm.next;
	}
	// If there are no equivalent ones, evaluate the dirty bits in the current place
	if (!found) {
	    if (prev != null) {
		leftBits = ((soleUserCompDirty|prev.soleUserCompDirty) & ALL_DIRTY_BITS);
	    }
	    else if (prevMap != null) {
		leftBits = ((soleUserCompDirty|prevMap.soleUserCompDirty) & ALL_DIRTY_BITS);
	    }
	    if (next != null) {
		rightBits = ((soleUserCompDirty|next.soleUserCompDirty) & ALL_DIRTY_BITS);
	    }
	    else if (nextMap != null) {
		rightBits = ((soleUserCompDirty|nextMap.soleUserCompDirty) & ALL_DIRTY_BITS);
	    }
	    markBitsAsDirty(leftBits, rightBits);
	}

    }

    void reEvaluateEquivalence () {
	// If Material changed, reInsert next to a equivalent material under
	// the same transform group
	// to prevent unnecessary material download
	// This RM may have been evaluated due to an other RM is the same list
	// If not, ...
	if ((soleUserCompDirty & ALL_DIRTY_BITS) != 0) {
	    if ((soleUserCompDirty & MATERIAL_DIRTY) != 0) {
		handleMaterialEquivalence();
	    }
	    else {
		int dirtyBits = (soleUserCompDirty & ALL_DIRTY_BITS);
		if (prev != null) {
		    checkEquivalenceWithLeftNeighbor(prev, ((dirtyBits|prev.soleUserCompDirty) & ALL_DIRTY_BITS));
		    prev.soleUserCompDirty = 0;
		} else if (prevMap != null) {
		    checkEquivalenceWithLeftNeighbor(prevMap, ((dirtyBits|prevMap.soleUserCompDirty) & ALL_DIRTY_BITS));
		    prevMap.soleUserCompDirty = 0;
		}
		if (next != null) {
		    next.checkEquivalenceWithLeftNeighbor(this,((next.soleUserCompDirty|soleUserCompDirty) & ALL_DIRTY_BITS));
		} else if (nextMap != null) {
		    nextMap.checkEquivalenceWithLeftNeighbor(this,((nextMap.soleUserCompDirty | soleUserCompDirty) & ALL_DIRTY_BITS));
		}
	    }
	}
	soleUserCompDirty &= ~ALL_DIRTY_BITS;
    }


    boolean materialEquivalent(RenderMolecule rm, boolean reloadColor) {
	if (!reloadColor) {
	    if (((this.material == rm.material) ||
		 ((rm.definingMaterial != null) &&
		  (rm.definingMaterial.equivalent(definingMaterial)))) &&
		rm.alpha == alpha &&
		enableLighting == rm.enableLighting &&
		(enableLighting ||
		 (!enableLighting  &&
		  rm.red ==red &&
		  rm.green == green &&
		  rm.blue == blue))) {
		return true;
	    }
	}
	return false;
    }

    boolean coloringEquivalent(RenderMolecule rm, boolean reload_color) {
	if (!reload_color) {
	    if (((rm.coloringAttributes == coloringAttributes) ||
		 ((rm.definingColoringAttributes != null) &&
		  (rm.definingColoringAttributes.equivalent(definingColoringAttributes)))) &&
		(!enableLighting || (enableLighting && (dRed == rm.dRed && dBlue == rm.dBlue && dGreen == rm.dGreen)))) {
		return true;
	    }
	}
	return false;
    }

    boolean transparencyEquivalent(RenderMolecule rm) {
	if (((rm.transparency == transparency) ||
	     ((rm.definingTransparency != null) &&
	      (rm.definingTransparency.equivalent(definingTransparency))) &&
	     (rm.definingTransparency.transparencyMode < TransparencyAttributes.SCREEN_DOOR &&
	      blendOn() == rm.blendOn()))) {
	    return true;
	}
	return false;
    }

    boolean blendOn() {
	if (lineAA && ((((geometryType & LINE) != 0) ||
			polygonMode == PolygonAttributes.POLYGON_LINE))) {
	    return true;
	}
	if (pointAA && ((((geometryType & POINT) != 0) ||
			 polygonMode == PolygonAttributes.POLYGON_POINT))) {
	    return true;
	}
	return false;
    }

    VirtualUniverse getVirtualUniverse() {
	return null;
    }


    void handleLocaleChange() {
	if (locale == renderBin.locale) {
	    if (localToVworld != localeLocalToVworld) {
		localeLocalToVworld = localToVworld;
		localeTranslation = null;
	    }
	}
	else {
	    // Using the localToVworl then, go back to making a new copy
	    if (localeTranslation == null) {
		localeLocalToVworld = new Transform3D[2];
		localeLocalToVworld[0] = new Transform3D();
		localeLocalToVworld[1] = new Transform3D();

		localeTranslation = new Vector3d();
		locale.hiRes.difference(renderBin.locale.hiRes, localeTranslation);
		translate();
		int i = localToVworldIndex[NodeRetained.CURRENT_LOCAL_TO_VWORLD];

		localeLocalToVworld[i].mat[0] = localToVworld[i].mat[0];
		localeLocalToVworld[i].mat[1] = localToVworld[i].mat[1];
		localeLocalToVworld[i].mat[2] = localToVworld[i].mat[2];
		localeLocalToVworld[i].mat[3] = localToVworld[i].mat[3] + localeTranslation.x ;
		localeLocalToVworld[i].mat[4] = localToVworld[i].mat[4];
		localeLocalToVworld[i].mat[5] = localToVworld[i].mat[5];
		localeLocalToVworld[i].mat[6] = localToVworld[i].mat[6];
		localeLocalToVworld[i].mat[7] = localToVworld[i].mat[7]+ localeTranslation.y;
		localeLocalToVworld[i].mat[8] = localToVworld[i].mat[8];
		localeLocalToVworld[i].mat[9] = localToVworld[i].mat[9];
		localeLocalToVworld[i].mat[10] = localToVworld[i].mat[10];
		localeLocalToVworld[i].mat[11] = localToVworld[i].mat[11]+ localeTranslation.z;
		localeLocalToVworld[i].mat[12] = localToVworld[i].mat[12];
		localeLocalToVworld[i].mat[13] = localToVworld[i].mat[13];
		localeLocalToVworld[i].mat[14] = localToVworld[i].mat[14];
		localeLocalToVworld[i].mat[15] = localToVworld[i].mat[15];
	    }
	}

	trans = localeLocalToVworld;
    }


    /**
     * updateNodeComponentCheck is called for each soleUser RenderMolecule
     * into which new renderAtom has been added. This method is called before
     * updateNodeComponent() to allow RenderMolecule to catch any node
     * component changes that have been missed because the changes
     * come when there is no active renderAtom associated with the
     * TextureBin. See bug# 4503926 for details.
     */
    public void updateNodeComponentCheck() {

	// If the renderMolecule has been removed, do nothing ..
	if ((onUpdateList &ON_UPDATE_CHECK_LIST ) == 0)
	    return;

	onUpdateList &= ~ON_UPDATE_CHECK_LIST;
	NodeComponentRetained nc = (NodeComponentRetained)appHandle;
	if ((nc.compChanged  & RM_COMPONENTS) != 0) {
	    if ((soleUserCompDirty& ALL_DIRTY_BITS) == 0 ) {
		renderBin.rmUpdateList.add(this);
	    }
	    soleUserCompDirty |= (nc.compChanged  & RM_COMPONENTS);
	}
	if (definingPolygonAttributes != null &&
	    definingPolygonAttributes == polygonAttributes) {
	    if (definingPolygonAttributes.compChanged != 0) {
		if ((soleUserCompDirty& ALL_DIRTY_BITS) == 0 ) {
		    renderBin.rmUpdateList.add(this);
		}
		soleUserCompDirty |= POLYGONATTRS_DIRTY;
	    }
	}
	if (definingLineAttributes != null &&
	    definingLineAttributes == lineAttributes) {
	    if (definingLineAttributes.compChanged != 0) {
		if ((soleUserCompDirty& ALL_DIRTY_BITS) == 0 ) {
		    renderBin.rmUpdateList.add(this);
		}
		soleUserCompDirty |= LINEATTRS_DIRTY;
	    }
	}
	if (definingPointAttributes != null &&
	    definingPointAttributes.compChanged != 0) {
	    if ((soleUserCompDirty& ALL_DIRTY_BITS) == 0 ) {
		renderBin.rmUpdateList.add(this);
	    }
	    soleUserCompDirty |= POINTATTRS_DIRTY;
	}

	if (definingMaterial != null &&
	    definingMaterial == material) {
	    if (definingMaterial.compChanged != 0) {
		if ((soleUserCompDirty& ALL_DIRTY_BITS) == 0 ) {
		    renderBin.rmUpdateList.add(this);
		}
		soleUserCompDirty |= MATERIAL_DIRTY;
	    }
	}

	if (definingColoringAttributes != null &&
	    definingColoringAttributes == coloringAttributes) {
	    if (definingColoringAttributes.compChanged != 0) {
		if ((soleUserCompDirty& ALL_DIRTY_BITS) == 0 ) {
		    renderBin.rmUpdateList.add(this);
		}
		soleUserCompDirty |= COLORINGATTRS_DIRTY;
	    }
	}

	if (definingTransparency != null &&
	    definingTransparency == transparency) {
	    if (definingTransparency.compChanged != 0) {
		if ((soleUserCompDirty& ALL_DIRTY_BITS) == 0 ) {
		    renderBin.rmUpdateList.add(this);
		}
		soleUserCompDirty |= TRANSPARENCY_DIRTY;
	    }
	}
    }
}
