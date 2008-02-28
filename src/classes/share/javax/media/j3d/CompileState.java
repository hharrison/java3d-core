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

import java.util.*;
import java.security.*;

/**
 * The CompileState holds information used during a compile.  It is
 * passed to each SceneGraphObject (SGO) during the compile.  Each SGO
 * modifies the CompileState as necessary and passes the CompileState
 * to its children (if any).
 *
 * The CompileState currently has two functions: appearance mapping
 * and shape merging.
 *
 * Appearance mapping maintains a list of the unique appearances seen
 * during the compile.  getAppearance() is used to turn multiple,
 * equivalent and static appearances into a single shared appearance.
 *
 * The shape mergings collects shapes that are potentially mergable
 * during a compile.  The shapes are sorted into a Map of Lists of
 * shapes, using the shape's appearance as the key.  After a subtree
 * is traversed, the shapes are merged and added to the Group.
 */

class CompileState {
    // Appearance mapping stuff:
    HashMap knownAppearances = new HashMap();
    int	    numAppearances = 0;
    int	    numShared = 0;
    int numShapes = 0;

    // Shape merging stuff:
    HashMap shapeLists = null; // top entry in shapeListStack
    int	numMergeSets = 0;
    int	numMergeShapes = 0;
    boolean compileVerbose = false;


    static final int BOUNDS_READ		= 0x00001;
    static final int GEOMETRY_READ		= 0x00002;

    
    // scene graph flattening

    boolean keepTG = false;	// used to force the immediate transform
				// group to stay around

    boolean needNormalsTransform = false;  // true if the current transform 
		 			   // needs to push down normals
					   // transform to geometries
				 
				// the current static transform group
    TransformGroupRetained staticTransform = null;
    
				// parent group
    GroupRetained parentGroup = null;

				// list of transform group
				// for the current transform group
    ArrayList transformGroupChildrenList = null; 

				// list of objects that have a static
				// transform that can be deferenced
				// after compile
    ArrayList staticTransformObjects = new ArrayList(1);

    int numTransformGroups = 0;
    int numStaticTransformGroups = 0;
    int numMergedTransformGroups = 0;
    int numGroups = 0;
    int numMergedGroups = 0;
    int numShapesWSharedGeom = 0;
    int numShapesWStaticTG = 0;
    int numLinks = 0;
    int numSwitches = 0;
    int numOrderedGroups = 0;
    int numMorphs = 0;

    CompileState() {
	try {
	    compileVerbose = Boolean.getBoolean("javax.media.j3d.compileVerbose");
	} catch (AccessControlException e) {
	    compileVerbose = false;
	}
	initShapeMerge();
    } 

    // Appearance mapping:
    /**
     * Returns an unique appearance which equals app.  If appearance does not
     * equal any previously found, the appearance will be added to the known
     * appearances and be returned.  If the apperance equals a previously known
     * appearance, then the prevously known apperance will be returned
     */
    AppearanceRetained getAppearance(AppearanceRetained app) {
        AppearanceRetained retval;

        // see if the appearance has allready been classified
        if (app.map == this) {
            if (app.mapAppearance != null) {
                numShared++;
                return app.mapAppearance;
            }
        }

        // check if this appearance equals one one in the Map
        if ((retval = (AppearanceRetained)knownAppearances.get(app)) != null) {
            numShared++;
        } else {
             // not found, put this appearance in the map
             knownAppearances.put(app, app);
             numAppearances++;
	     numShared++; // sharing with self...
             retval = app;
        }

        // cache this result on the appearance in case it appears again
        app.map = this;
        app.mapAppearance = retval;

        return retval;
    }

    // Shape Merging:
    private void initShapeMerge() {
	shapeLists = new HashMap();

    }

    void addShape(Shape3DRetained shape) {
	if (parentGroup != null) {
	    // sort the shapes into lists with equivalent appearances
	    Vector list;
	    if ((list = (Vector)shapeLists.get(shape.appearance)) == null) {
		list = new Vector();
		shapeLists.put(shape.appearance, list);
	    }
	    // Add the shape to the list only if at its parent level
	    // no children can be added or removed ..
	    // Look for the first non-null geometry, there should be atleast
	    // one otherwise it wouldn't come here
	    GeometryRetained geometry = null;
	    int i = 0;
	    while (geometry == null && i < shape.geometryList.size()) {
		geometry = (GeometryRetained) shape.geometryList.get(i);
		i++;
	    }	    
	    if (shape.parent instanceof GroupRetained && ((GroupRetained)shape.parent).isStaticChildren() && geometry.geoType < GeometryArrayRetained.GEO_TYPE_RASTER) {
		list.add(shape);
	    }

	}
    }


    void printStats() {
	System.err.println("numTransformGroups= " + numTransformGroups);
	System.err.println("numStaticTransformGroups= " + numStaticTransformGroups);
	System.err.println("numMergedTransformGroups= " + numMergedTransformGroups);
	System.err.println("numGroups= " + numGroups);
	System.err.println("numMergedGroups= " + numMergedGroups);
	System.err.println("numShapes= " + numShapes);
	System.err.println("numShapesWStaticTG= " + numShapesWStaticTG);
	System.err.println("numMergeShapes= " + numMergeShapes);
	System.err.println("numMergeSets= " + numMergeSets);
	System.err.println("numLinks= " + numLinks);
	System.err.println("numSwitches= " + numSwitches);
	System.err.println("numOrderedGroups= " + numOrderedGroups);
	System.err.println("numMorphs= " + numMorphs);
    }

    void doShapeMerge() {

	//	System.err.println("doShapeMerge, shapeList = "+shapeLists);
	if (shapeLists != null) {
	    // loop over the shapes in each list, creating a single shape 
	    // for each.  Add the shape to the group 
	    Collection lists = shapeLists.values();
	    Iterator listIterator = lists.iterator();
	    Shape3DRetained mergeShape;
	    GeometryRetained firstGeo;
	    int num = 0;
	    int compileFlags = 0;
	    
	    while (listIterator.hasNext()) {
		Vector curList = (Vector)listIterator.next();
		int numShapes = curList.size();
		Shape3DRetained[] shapes = new Shape3DRetained[numShapes];
		curList.copyInto(shapes);
		Shape3DRetained[] toBeMergedShapes = new Shape3DRetained[numShapes];
		for (int i = 0; i < numShapes; i++) {
		    if (shapes[i] == null) {
			continue;
		    }
		    firstGeo = null;
		    num = 0;
		    // Get the first non-null geometry
		    while (firstGeo == null && num < shapes[i].geometryList.size()) {
			firstGeo = (GeometryRetained) shapes[i].geometryList.get(num);
			num++;
		    }

		    if (firstGeo != null && firstGeo instanceof GeometryArrayRetained) {
			int numMerge = 0;
			mergeShape = shapes[i];
			GeometryArrayRetained mergeGeo = (GeometryArrayRetained)firstGeo;
			
			toBeMergedShapes[numMerge++] = mergeShape;
			// Determine if all mergeable shapes have the same boundsCompute
			// and collisionBounds set the same way
			compileFlags = getCompileFlags(mergeShape);
			for (int j = i+1; j < numShapes; j++) {
			    if (shapes[j] == null) {
				continue;
			    }
			    firstGeo = null;
			    num = 0;
			    // Get the first non-null geometry
			    while (firstGeo == null && num < shapes[j].geometryList.size()) {
				firstGeo = (GeometryRetained) shapes[j].geometryList.get(num);
				num++;
			    }

			    // There is a non-null geometry for this shape ..
			    if (firstGeo != null &&
				shapes[j].isEquivalent(mergeShape) &&
				firstGeo.isEquivalenceClass(mergeGeo) &&
				((GeometryArrayRetained)firstGeo).vertexFormat == mergeGeo.vertexFormat) {
				    // got one to merge, add shapes to merge, 
				    toBeMergedShapes[numMerge++] = shapes[j];

				    compileFlags |= getCompileFlags(shapes[j]);
				    
				    // remove from shapes
				    shapes[j] = null;
			    }
			}
			if (numMerge > 1) {

			    // remove the shapes from its parent before merge
			    // They all should
			    GroupRetained group = (GroupRetained)toBeMergedShapes[0].parent;
			    Shape3DRetained s;
			    for (int n = 0; n < numMerge; n++) {
				s = toBeMergedShapes[n];
				boolean found = false;
				int numChilds = group.numChildren();
				for (int k = 0; (k < numChilds && !found); k++) {
				    if (group.getChild(k).retained == s) {
					found = true;
					group.removeChild(k);
				    }
				}
				if (!found) {
				    System.err.println("ShapeSet.add(): Can't remove " +
						       "shape from parent, can't find shape!");
				}				    
				
			    }
				
			    mergeShape = new Shape3DCompileRetained(toBeMergedShapes, numMerge, compileFlags);

			    if (J3dDebug.devPhase && J3dDebug.debug) {
				if (J3dDebug.doDebug(J3dDebug.compileState, J3dDebug.LEVEL_3)) {
				    System.err.println("Dest is "+ parentGroup);
				    System.err.println("Compile Shape "+mergeShape);
				    System.err.println(mergeShape.geometryList.size()+" geoemtryList");
				    for (int j = 0; j < mergeShape.geometryList.size(); j++) {
					GeometryRetained geo = ((GeometryRetained)mergeShape.geometryList.get(j));
					if (geo != null)
					    System.err.println("\t Geo_type = "+geo.geoType);
				    }

				    System.err.println(numMerge+" Shapes were merged ");
				    for (int j = 0; j < numMerge; j++) {
					System.err.println("\t" + toBeMergedShapes[j]);
				    }
				}
			    }

			    // Set the source to one of the merged shape's source
			    mergeShape.setSource(toBeMergedShapes[0].source);
			    numMergeSets++;
			    numMergeShapes += numMerge ;
			    parentGroup.addChild((Node)mergeShape.source);
			}
		    }
		    // add the shape to the dest
		}
	    }
	}

	// Clear the shapelists for the next merge
	shapeLists.clear();

    }


    int getCompileFlags(Shape3DRetained shape) {
	int cflag = 0;

	// If allow intersect is turned on , then geometry is readable
	if (shape.allowIntersect() ||
	    shape.source.getCapability(Shape3D.ALLOW_GEOMETRY_READ)||
	    (shape.boundsAutoCompute &&
	     shape.source.getCapability(Shape3D.ALLOW_BOUNDS_READ)))
	    cflag |= GEOMETRY_READ;
 
	return cflag;
			
    }

}
