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

import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.internal.FastVector;
import java.awt.Font;
import java.awt.font.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import javax.vecmath.*;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.util.*;

/**
 * The Font3D object is used to store extruded 2D glyphs.  These
 * 3D glyphs can then be used to construct Text3D NodeComponent
 * objects.
 * <P>
 * A 3D Font consists of a Java 2D font, a tesellation tolerance,
 * and an extrusion path.  The extrusion
 * path creates depth by describing how the edge of a glyph varies
 * in the Z axis.
 * <P>
 * The construction of a Text3D object requires a Font3D object.
 * The Font3D object describes the style of the text, such as its
 * depth. The text also needs other classes, such as java.awt.Font and
 * FontExtrusion. The Font object describes the font name (Helvetica,
 * Courier, etc.), the font style (bold, Italic, etc.), and point
 * size. The FontExtrusion object extends Font3D by describing
 * the extrusion path for the Font3D object (how the edge of the
 * font glyph varies in the Z axis).
 *<P>
 * To ensure correct rendering, the 2D Font object should be created
 * with the default AffineTransform. The point size of the 2D font will
 * be used as a rough measure of how fine a tesselation to use when
 * creating the Font3D object: the larger the point size, in
 * general, the finer the tesselation.
 * <P>
 * Custom 3D fonts as well as methods to store 3D fonts
 * to disk will be addressed in a future release.
 *
 * @see java.awt.Font
 * @see FontExtrusion
 * @see Text3D
 */
public class Font3D extends NodeComponent {

    Font              font;
    double            tessellationTolerance;
    FontExtrusion     fontExtrusion;
    FontRenderContext frc;
    // Used by triangulateGlyphs method to split contour data into islands.
    final static float EPS = 0.000001f;

    // Map glyph code to GeometryArrayRetained
    Hashtable geomHash = new Hashtable(20);

    /**
     * Constructs a Font3D object from the specified Font and
     * FontExtrusion objects, using the default value for the
     * tessellation tolerance.  The default value is as follows:
     *
     * <ul>
     * tessellation tolerance : 0.01<br>
     * </ul>
     * <P>
     * The FontExtrusion object contains the extrusion path to use on
     * the 2D Font glyphs.  To ensure correct rendering the font must
     * be created with the default AffineTransform.  Passing null for
     * the FontExtrusion parameter results in no extrusion being done.
     *
     * @param font the Java 2D font used to create the 3D font object
     * @param extrudePath the extrusion path used to describe how
     * the edge of the font varies along the Z axis
     */
    public Font3D(Font font, FontExtrusion extrudePath) {
	this(font, 0.01, extrudePath);
    }

    /**
     * Constructs a Font3D object from the specified Font and
     * FontExtrusion objects, using the specified tessellation
     * tolerance.
     * The FontExtrusion object contains the extrusion path to use on
     * the 2D Font glyphs.  To ensure correct rendering, the font must
     * be created with the default AffineTransform.  Passing null for
     * the FontExtrusion parameter results in no extrusion being done.
     *
     * @param font the Java 2D font used to create the 3D font object.
     * @param tessellationTolerance the tessellation tolerance value
     * used in tessellating the glyphs of the 2D Font.
     * This corresponds to the <code>flatness</code> parameter in
     * the <code>java.awt.Shape.getPathIterator</code> method.
     * @param extrudePath the extrusion path used to describe how
     * the edge of the font varies along the Z axis.
     *
     * @since Java 3D 1.2
     */
    public Font3D(Font font,
		  double tessellationTolerance,
		  FontExtrusion extrudePath) {

      this.font = font;
      this.tessellationTolerance = tessellationTolerance;
      this.fontExtrusion = extrudePath;
      this.frc = new FontRenderContext(new AffineTransform(),
				       true, true);
    }

    /**
     * Returns the Java 2D Font used to create this Font3D object.
     * @return Font object used by this Font3D
     */
    public Font getFont() {
      return this.font;
    }


    /**
     * Returns the tessellation tolerance with which this Font3D was
     * created.
     * @return the tessellation tolerance used by this Font3D
     *
     * @since Java 3D 1.2
     */
    public double getTessellationTolerance() {
	return tessellationTolerance;
    }


    /**
     * Copies the FontExtrusion object used to create this Font3D object
     * into the specified parameter.
     *
     * @param extrudePath object that will receive the
     * FontExtrusion information for this Font3D object
     */
    public void getFontExtrusion(FontExtrusion extrudePath) {
      extrudePath = this.fontExtrusion;
    }

    /**
     * Returns the 3D bounding box of the specified glyph code.
     *
     * @param glyphCode the glyphCode from the original 2D Font
     * @param bounds the 3D glyph's bounds
     */
    public void getBoundingBox(int glyphCode, BoundingBox bounds){
      int[] gCodes = {glyphCode};
      GlyphVector gVec = font.createGlyphVector(frc, gCodes);
      Rectangle2D.Float bounds2d = (Rectangle2D.Float)
	(((GlyphMetrics)(gVec.getGlyphMetrics(0))).getBounds2D());
      
      Point3d lower = new Point3d(bounds2d.x, bounds2d.y, 0.0);
      Point3d upper;
      if (fontExtrusion != null) {
          upper = new Point3d(bounds2d.x + bounds2d.width,
				      bounds2d.y + bounds2d.height,
				      fontExtrusion.length);
      } else {
          upper = new Point3d(bounds2d.x + bounds2d.width,
				      bounds2d.y + bounds2d.height,
				      0.0);
      }
      bounds.setLower(lower);
      bounds.setUpper(upper);
    }


  // Triangulate glyph with 'unicode' if not already done.
    GeometryArrayRetained triangulateGlyphs(GlyphVector gv, char c) {
	Character ch = new Character(c);
	GeometryArrayRetained geo = (GeometryArrayRetained) geomHash.get(ch);

	if (geo == null) {
	  // Font Y-axis is downwards, so send affine transform to flip it.
	    Rectangle2D bnd = gv.getVisualBounds();
	    AffineTransform aTran = new AffineTransform();
	    double tx = bnd.getX() + 0.5 * bnd.getWidth();
	    double ty = bnd.getY() + 0.5 * bnd.getHeight();
	    aTran.setToTranslation(-tx, -ty);
	    aTran.scale(1.0, -1.0);
	    aTran.translate(tx, -ty);
	    Shape shape = gv.getOutline();
	    PathIterator pIt = shape.getPathIterator(aTran, tessellationTolerance);
	    int flag= -1, numContours = 0, numPoints = 0, i, j, k, num=0, vertCnt;
	    UnorderList coords = new UnorderList(100, Point3f.class);
	    float tmpCoords[] = new float[6];
	    float lastX= .0f, lastY= .0f;
	    float firstPntx = Float.MAX_VALUE, firstPnty = Float.MAX_VALUE;
	    GeometryInfo gi = null;
	    NormalGenerator ng = new NormalGenerator();
	    FastVector contours = new FastVector(10);
	    float maxY = -Float.MAX_VALUE;
	    int maxYIndex = 0, beginIdx = 0, endIdx = 0, start = 0;
	    
	    boolean setMaxY = false;


	    while (!pIt.isDone()) {
		Point3f vertex = new Point3f();
		flag = pIt.currentSegment(tmpCoords);
		if (flag == PathIterator.SEG_CLOSE){
		    if (num > 0) {
			if (setMaxY) {
			    // Get Previous point
			    beginIdx = start;
			    endIdx = numPoints-1;
			}
			contours.addElement(num);
			num = 0;
			numContours++;
		    }
		} else if (flag == PathIterator.SEG_MOVETO){
			 vertex.x = tmpCoords[0];
			 vertex.y = tmpCoords[1];
			 lastX = vertex.x;
			 lastY = vertex.y;

			 if ((lastX == firstPntx) && (lastY == firstPnty)) {
			     pIt.next();
			     continue;
			 }
			 setMaxY = false;
			 coords.add(vertex);
			 firstPntx = lastX;
			 firstPnty = lastY;
			 if (num> 0){
			     contours.addElement(num);
			     num = 0;
			     numContours++;
			 }
			 num++;
			 numPoints++;
			 // skip checking of first point,
			 // since the last point will repeat this.
			 start = numPoints ;
		} else if (flag == PathIterator.SEG_LINETO){
			 vertex.x = tmpCoords[0];
			 vertex.y = tmpCoords[1];
			 //Check here for duplicate points. Code
			 //later in this function can not handle
			 //duplicate points.

			 if ((vertex.x == lastX) && (vertex.y == lastY)) {
			     pIt.next();
			     continue;
			 }
			 if (vertex.y > maxY) {
			     maxY = vertex.y;
			     maxYIndex = numPoints;
			     setMaxY = true;
			 }
			 lastX = vertex.x;
			 lastY = vertex.y;
			 coords.add(vertex);
			 num++;
			 numPoints++;
		} 
		pIt.next();
	    }
	    
	    // No data(e.g space, control characters)
	    // Two point can't form a valid contour
	    if (numPoints == 0){
	      return null;
	    }



	    // Determine font winding order use for side triangles
	    Point3f p1 = new Point3f(), p2 = new Point3f(), p3 = new Point3f();
	    boolean flip_side_orient = true;
	    Point3f vertices[] = (Point3f []) coords.toArray(false);

	    if (endIdx - beginIdx > 0) {
		// must be true unless it is a single line
		// define as "MoveTo p1 LineTo p2 Close" which is
		// not a valid font definition.
		
		if (maxYIndex == beginIdx) {
		    p1.set(vertices[endIdx]);		    
		} else {
		    p1.set(vertices[maxYIndex-1]);		    
		}
		p2.set(vertices[maxYIndex]);		    
		if (maxYIndex == endIdx) {
		    p3.set(vertices[beginIdx]);
		} else {
		    p3.set(vertices[maxYIndex+1]);
		}

		if (p3.x != p2.x) {
		    if (p1.x != p2.x) {
			// Use the one with smallest slope
			if (Math.abs((p2.y - p1.y)/(p2.x - p1.x)) >
			    Math.abs((p3.y - p2.y)/(p3.x - p2.x))) {
			    flip_side_orient = (p3.x > p2.x);
			} else {
			    flip_side_orient = (p2.x > p1.x);
			}
		    } else {
			flip_side_orient = (p3.x > p2.x);			
		    }
		} else {
		    // p1.x != p2.x, otherwise all three
		    // point form a straight vertical line with
		    // the middle point the highest. This is not a
		    // valid font definition.
		    flip_side_orient = (p2.x > p1.x);
		}
	    }

	    // Build a Tree of Islands
	    int  startIdx = 0;
	    IslandsNode islandsTree = new IslandsNode(-1, -1);
	    int contourCounts[] = contours.getData();


	    for (i= 0;i < contours.getSize(); i++) {
		endIdx = startIdx + contourCounts[i];
		islandsTree.insert(new IslandsNode(startIdx, endIdx), vertices);
		startIdx = endIdx;
	    }      

	    coords = null;   // Free memory 
	    contours = null;
	    contourCounts = null;

	    // Compute islandCounts[][] and outVerts[][]
	    UnorderList islandsList = new UnorderList(10, IslandsNode.class);
	    islandsTree.collectOddLevelNode(islandsList, 0);
	    IslandsNode nodes[] = (IslandsNode []) islandsList.toArray(false);
	    int islandCounts[][] = new int[islandsList.arraySize()][];
	    Point3f outVerts[][] = new Point3f[islandCounts.length][];
	    int nchild, sum;
	    IslandsNode node;	    

	    for (i=0; i < islandCounts.length; i++) {
		node = nodes[i];
		nchild = node.numChild();
		islandCounts[i] = new int[nchild + 1];
		islandCounts[i][0] = node.numVertices();
		sum = 0;
		sum += islandCounts[i][0];
		for (j=0; j < nchild; j++) {
		    islandCounts[i][j+1] = node.getChild(j).numVertices();
		    sum += islandCounts[i][j+1];
		} 
		outVerts[i] = new Point3f[sum];
		startIdx = 0;
		for (k=node.startIdx; k < node.endIdx; k++) {
		    outVerts[i][startIdx++] = vertices[k];
		}

		for (j=0; j < nchild; j++) {
		    endIdx = node.getChild(j).endIdx;
		    for (k=node.getChild(j).startIdx; k < endIdx; k++) {
			outVerts[i][startIdx++] = vertices[k];
		    }
		}
	    }
	    


	    islandsTree = null; // Free memory 
	    islandsList = null;
	    vertices = null;

	    contourCounts = new int[1];
	    int currCoordIndex = 0, vertOffset = 0;
	    ArrayList triangData = new ArrayList();

	    Point3f q1 = new Point3f(), q2 = new Point3f(), q3 = new Point3f();
	    Vector3f n1 = new Vector3f(), n2 = new Vector3f();	    
	    numPoints = 0;
	    //Now loop thru each island, calling triangulator once per island.
	    //Combine triangle data for all islands together in one object.
	    for (i=0;i < islandCounts.length;i++) {
		contourCounts[0] = islandCounts[i].length;
		numPoints += outVerts[i].length;
		gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
		gi.setCoordinates(outVerts[i]);
		gi.setStripCounts(islandCounts[i]);
		gi.setContourCounts(contourCounts);
		ng.generateNormals(gi);

		GeometryArray ga = gi.getGeometryArray(false, false, false);
		vertOffset += ga.getVertexCount();

		triangData.add(ga);
	      }
	    // Multiply by 2 since we create 2 faces of the font
	    // Second term is for side-faces along depth of the font
	    if (fontExtrusion == null)
	      vertCnt = vertOffset;
	    else{
	      if (fontExtrusion.shape == null)
		vertCnt = vertOffset * 2 + numPoints *6;
	      else{
		vertCnt = vertOffset * 2 + numPoints * 6 *
		  (fontExtrusion.pnts.length -1);
	      }
	    }

	    // TODO: Should use IndexedTriangleArray to avoid 
	    // duplication of vertices. To create triangles for
	    // side faces, every vertex is duplicated currently.
	    TriangleArray triAry = new TriangleArray(vertCnt,
						     GeometryArray.COORDINATES | 
						     GeometryArray.NORMALS);

	    boolean flip_orient[] = new boolean[islandCounts.length];
	    boolean findOrient;
	    // last known non-degenerate normal
	    Vector3f goodNormal = new Vector3f();


	    for (j=0;j < islandCounts.length;j++) {
		GeometryArray ga = (GeometryArray)triangData.get(j);
		vertOffset = ga.getVertexCount();

		findOrient = false;
		
		//Create the triangle array
		for (i= 0; i < vertOffset; i+= 3, currCoordIndex += 3){
		    //Get 3 points. Since triangle is known to be flat, normal
		    // must be same for all 3 points.
		    ga.getCoordinate(i, p1);
		    ga.getNormal(i, n1);
		    ga.getCoordinate(i+1, p2);
		    ga.getCoordinate(i+2, p3);
		    
		    if (!findOrient) {
			//Check here if triangles are wound incorrectly and need
			//to be flipped.
			if (!getNormal(p1,p2, p3, n2)) {
			    continue;
			}
			
			if (n2.z >= EPS) {
			    flip_orient[j] = false;
			} else if (n2.z <= -EPS) {
			    flip_orient[j] = true;
			} else {
			    continue;
			}
			findOrient = true;
		    }
		    if (flip_orient[j]){
			//New Triangulator preserves contour orientation. If contour
			//input is wound incorrectly, swap 2nd and 3rd points to
			//sure all triangles are wound correctly for j3d.
			q1.x = p2.x; q1.y = p2.y; q1.z = p2.z;
			p2.x = p3.x; p2.y = p3.y; p2.z = p3.z;
			p3.x = q1.x; p3.y = q1.y; p3.z = q1.z;
			n1.x = -n1.x; n1.y = -n1.y; n1.z = -n1.z;
		    }
		    
		    
		    if (fontExtrusion != null) {
			n2.x = -n1.x;n2.y = -n1.y;n2.z = -n1.z;
			
			triAry.setCoordinate(currCoordIndex, p1);
			triAry.setNormal(currCoordIndex, n2);
			triAry.setCoordinate(currCoordIndex+1, p3);
			triAry.setNormal(currCoordIndex+1, n2);
			triAry.setCoordinate(currCoordIndex+2, p2);
			triAry.setNormal(currCoordIndex+2, n2);
			
			q1.x = p1.x; q1.y = p1.y; q1.z = p1.z + fontExtrusion.length;
			q2.x = p2.x; q2.y = p2.y; q2.z = p2.z + fontExtrusion.length;
			q3.x = p3.x; q3.y = p3.y; q3.z = p3.z + fontExtrusion.length;
			
			triAry.setCoordinate(currCoordIndex+vertOffset, q1);
			triAry.setNormal(currCoordIndex+vertOffset, n1);
			triAry.setCoordinate(currCoordIndex+1+vertOffset, q2);
			triAry.setNormal(currCoordIndex+1+vertOffset, n1);
			triAry.setCoordinate(currCoordIndex+2+vertOffset, q3);
			triAry.setNormal(currCoordIndex+2+vertOffset, n1);
		    } else {
			triAry.setCoordinate(currCoordIndex, p1);
			triAry.setNormal(currCoordIndex, n1);
			triAry.setCoordinate(currCoordIndex+1, p2);
			triAry.setNormal(currCoordIndex+1, n1);
			triAry.setCoordinate(currCoordIndex+2, p3);
			triAry.setNormal(currCoordIndex+2, n1);
		    }

		}
		if (fontExtrusion != null) {		
		    currCoordIndex += vertOffset;
		}
	    }
		
	    //Now add side triangles in both cases.

	    // Since we duplicated triangles with different Z, make sure
	    // currCoordIndex points to correct location.
	    if (fontExtrusion != null){
		if (fontExtrusion.shape == null){
		    boolean smooth;
		    // we'll put a crease if the angle between the normals is
		    // greater than 44 degrees
		    float threshold = (float) Math.cos(44.0*Math.PI/180.0);
		    float cosine;
		    // need the previous normals to check for smoothing
		    Vector3f pn1 = null, pn2 = null;
		    // need the next normals to check for smoothing
		    Vector3f n3 = new Vector3f(), n4 = new Vector3f();
		    //  store the normals for each point because they are
		    // the same for both triangles
		    Vector3f p1Normal = new Vector3f();
		    Vector3f p2Normal = new Vector3f();
		    Vector3f p3Normal = new Vector3f();
		    Vector3f q1Normal = new Vector3f();
		    Vector3f q2Normal = new Vector3f();
		    Vector3f q3Normal = new Vector3f();
		    
		    for (i=0;i < islandCounts.length;i++){
			for (j=0, k=0, num =0;j < islandCounts[i].length;j++){
			    num += islandCounts[i][j];
			    p1.x = outVerts[i][num - 1].x;
			    p1.y = outVerts[i][num - 1].y;
			    p1.z = 0.0f;
			    q1.x = p1.x; q1.y = p1.y; q1.z = p1.z+fontExtrusion.length;
			    p2.z = 0.0f;
			    q2.z = p2.z+fontExtrusion.length;
			    for (int m=0; m < num;m++) {	      
				p2.x = outVerts[i][m].x;
				p2.y = outVerts[i][m].y;
				q2.x = p2.x; 
				q2.y = p2.y; 
				if (getNormal(p1, q1, p2, n1)) {
				    
				    if (!flip_side_orient) {
					n1.negate();
				    }
				    goodNormal.set(n1);
				    break;
				}
			    }
			    
			    for (;k < num;k++){
				p2.x = outVerts[i][k].x;p2.y = outVerts[i][k].y;p2.z = 0.0f;
				q2.x = p2.x; q2.y = p2.y; q2.z = p2.z+fontExtrusion.length;
				
				if (!getNormal(p1, q1, p2, n1)) {
				    n1.set(goodNormal);
				} else {
				    if (!flip_side_orient) {
					n1.negate();
				    }
				    goodNormal.set(n1);
				}
				
				if (!getNormal(p2, q1, q2, n2)) {
				    n2.set(goodNormal);
				} else {
				    if (!flip_side_orient) {
					n2.negate();
				    }
				    goodNormal.set(n2);
				}
				// if there is a previous normal, see if we need to smooth
				// this normal or make a crease
				
				if (pn1 != null) {
				    cosine = n1.dot(pn2);
				    smooth = cosine > threshold;
				    if (smooth) {
					p1Normal.x = (pn1.x + pn2.x + n1.x);
					p1Normal.y = (pn1.y + pn2.y + n1.y);
					p1Normal.z = (pn1.z + pn2.z + n1.z);
					normalize(p1Normal);
					
					q1Normal.x = (pn2.x + n1.x + n2.x);
					q1Normal.y = (pn2.y + n1.y + n2.y);
					q1Normal.z = (pn2.z + n1.z + n2.z);
					normalize(q1Normal);
				    } // if smooth
				    else {
					p1Normal.x = n1.x; p1Normal.y = n1.y; p1Normal.z = n1.z;
					q1Normal.x = n1.x+n2.x; 
					q1Normal.y = n1.y+n2.y;
					q1Normal.z = n1.z+ n2.z; 
					normalize(q1Normal);
				    } // else
				} // if pn1 != null
				else {
				    pn1 = new Vector3f();
				    pn2 = new Vector3f();
				    p1Normal.x = n1.x;
				    p1Normal.y = n1.y;
				    p1Normal.z = n1.z;
				    
				    q1Normal.x = (n1.x + n2.x);
				    q1Normal.y = (n1.y + n2.y);
				    q1Normal.z = (n1.z + n2.z);
				    normalize(q1Normal);
				} // else
				
				// if there is a next, check if we should smooth normal
				
				if (k+1 < num) {
				    p3.x = outVerts[i][k+1].x; p3.y = outVerts[i][k+1].y; 
				    p3.z = 0.0f;
				    q3.x = p3.x; q3.y = p3.y; q3.z = p3.z + fontExtrusion.length;
				    
				    if (!getNormal(p2, q2, p3, n3)) {
					n3.set(goodNormal);
				    } else {
					if (!flip_side_orient) {
					    n3.negate();
					}
					goodNormal.set(n3);
				    }
				    
				    if (!getNormal(p3, q2, q3, n4)) {
					n4.set(goodNormal);
				    } else {
					if (!flip_side_orient) {
					    n4.negate();
					}
					goodNormal.set(n4);
				    }
				    
				    cosine = n2.dot(n3);
				    smooth = cosine > threshold;
				    
				    if (smooth) {
					p2Normal.x = (n1.x + n2.x + n3.x);
					p2Normal.y = (n1.y + n2.y + n3.y);
					p2Normal.z = (n1.z + n2.z + n3.z);
					normalize(p2Normal);
					
					q2Normal.x = (n2.x + n3.x + n4.x);
					q2Normal.y = (n2.y + n3.y + n4.y);
					q2Normal.z = (n2.z + n3.z + n4.z);
					normalize(q2Normal);
				    } else { // if smooth
					p2Normal.x = n1.x + n2.x;
					p2Normal.y = n1.y + n2.y;
					p2Normal.z = n1.z + n2.z;
					normalize(p2Normal);
					q2Normal.x = n2.x; q2Normal.y = n2.y; q2Normal.z = n2.z;
				    } // else
				} else { // if k+1 < num
				    p2Normal.x = (n1.x + n2.x);
				    p2Normal.y = (n1.y + n2.y);
				    p2Normal.z = (n1.z + n2.z);
				    normalize(p2Normal);
				    
				    q2Normal.x = n2.x;
				    q2Normal.y = n2.y;
				    q2Normal.z = n2.z;
				} // else
				
				// add pts for the 2 tris
				// p1, q1, p2 and p2, q1, q2
				
				if (flip_side_orient) {
				    triAry.setCoordinate(currCoordIndex, p1);
				    triAry.setNormal(currCoordIndex, p1Normal);
				    currCoordIndex++;
		    
				    triAry.setCoordinate(currCoordIndex, q1);
				    triAry.setNormal(currCoordIndex, q1Normal);
				    currCoordIndex++;
				    
				    triAry.setCoordinate(currCoordIndex, p2);
				    triAry.setNormal(currCoordIndex, p2Normal);
				    currCoordIndex++;
				    
				    triAry.setCoordinate(currCoordIndex, p2);
				    triAry.setNormal(currCoordIndex, p2Normal);
				    currCoordIndex++;
				    
				    triAry.setCoordinate(currCoordIndex, q1);
				    triAry.setNormal(currCoordIndex, q1Normal);
				    currCoordIndex++;
				} else {
				    triAry.setCoordinate(currCoordIndex, q1);
				    triAry.setNormal(currCoordIndex, q1Normal);
				    currCoordIndex++;
				    
				    triAry.setCoordinate(currCoordIndex, p1);
				    triAry.setNormal(currCoordIndex, p1Normal);
				    currCoordIndex++;
				    
				    triAry.setCoordinate(currCoordIndex, p2);
				    triAry.setNormal(currCoordIndex, p2Normal);
				    currCoordIndex++;
				    
				    triAry.setCoordinate(currCoordIndex, q1);
				    triAry.setNormal(currCoordIndex, q1Normal);
				    currCoordIndex++;
				    
				    triAry.setCoordinate(currCoordIndex, p2);
				    triAry.setNormal(currCoordIndex, p2Normal);
				    currCoordIndex++;
				}
				triAry.setCoordinate(currCoordIndex, q2);
				triAry.setNormal(currCoordIndex, q2Normal);
				currCoordIndex++;
				pn1.x = n1.x; pn1.y = n1.y; pn1.z = n1.z;
				pn2.x = n2.x; pn2.y = n2.y; pn2.z = n2.z;
				p1.x = p2.x; p1.y = p2.y; p1.z = p2.z;
				q1.x = q2.x; q1.y = q2.y; q1.z = q2.z;
				
			    }// for k
			    
			    // set the previous normals to null when we are done
			    pn1 = null;
			    pn2 = null;
			}// for j
		    }//for i
		} else { // if shape
		    int m, offset=0;
		    Point3f P2 = new Point3f(), Q2 = new Point3f(), P1=new Point3f();
		    Vector3f nn = new Vector3f(), nn1= new Vector3f(), 
			nn2= new Vector3f(), nn3= new Vector3f();
		    Vector3f nna = new Vector3f(), nnb=new Vector3f();
		    float length;
		    boolean validNormal = false;
		    
		    // fontExtrusion.shape is specified, and is NOT straight line
		    for (i=0;i < islandCounts.length;i++){
			for (j=0, k= 0, offset = num =0;j < islandCounts[i].length;j++){
			    num += islandCounts[i][j];

			    p1.x = outVerts[i][num - 1].x;
			    p1.y = outVerts[i][num - 1].y;
			    p1.z = 0.0f;
			    q1.x = p1.x; q1.y = p1.y; q1.z = p1.z+fontExtrusion.length;
			    p3.z = 0.0f;
			    for (m=num-2; m >= 0; m--) {
				p3.x = outVerts[i][m].x;
				p3.y = outVerts[i][m].y;
				
				if (getNormal(p3, q1, p1, nn1)) {
				    if (!flip_side_orient) {
					nn1.negate();
				    }
				    goodNormal.set(nn1);
				    break;
				}
			    }
			    for (;k < num;k++){
				p2.x = outVerts[i][k].x;p2.y = outVerts[i][k].y;p2.z = 0.0f;
				q2.x = p2.x; q2.y = p2.y; q2.z = p2.z+fontExtrusion.length;
				getNormal(p1, q1, p2, nn2);
				
				p3.x = outVerts[i][(k+1)==num ? offset:(k+1)].x;
				p3.y = outVerts[i][(k+1)==num ? offset:(k+1)].y;
				p3.z = 0.0f;
				if (!getNormal(p3,p2,q2, nn3)) {
				    nn3.set(goodNormal);
				} else {
				    if (!flip_side_orient) {
					nn3.negate();
				    }
				    goodNormal.set(nn3);
				}
				
				// Calculate normals at the point by averaging normals
				// of two faces on each side of the point.
				nna.x = (nn1.x+nn2.x);
				nna.y = (nn1.y+nn2.y);
				nna.z = (nn1.z+nn2.z);
				normalize(nna);
				
				nnb.x = (nn3.x+nn2.x);
				nnb.y = (nn3.y+nn2.y);
				nnb.z = (nn3.z+nn2.z);
				normalize(nnb);
				
				P1.x = p1.x;P1.y = p1.y;P1.z = p1.z;
				P2.x = p2.x;P2.y = p2.y; P2.z = p2.z;
				Q2.x = q2.x;Q2.y = q2.y; Q2.z = q2.z;
				for (m=1;m < fontExtrusion.pnts.length;m++){
				    q1.z = q2.z = fontExtrusion.pnts[m].x;
				    q1.x = P1.x + nna.x * fontExtrusion.pnts[m].y;
				    q1.y = P1.y + nna.y * fontExtrusion.pnts[m].y;
				    q2.x = P2.x + nnb.x * fontExtrusion.pnts[m].y;
				    q2.y = P2.y + nnb.y * fontExtrusion.pnts[m].y;
				    
				    if (!getNormal(p1, q1, p2, n1)) {
					n1.set(goodNormal);
				    } else {
					if (!flip_side_orient) {
					    n1.negate();
					}
					goodNormal.set(n1);
				    }
				    
				    if (flip_side_orient) {
					triAry.setCoordinate(currCoordIndex, p1);
					triAry.setNormal(currCoordIndex, n1);
					currCoordIndex++;
					
					triAry.setCoordinate(currCoordIndex, q1);
					triAry.setNormal(currCoordIndex, n1);
					currCoordIndex++;
				    } else  {
					triAry.setCoordinate(currCoordIndex, q1);
					triAry.setNormal(currCoordIndex, n1);
					currCoordIndex++;
					
					triAry.setCoordinate(currCoordIndex, p1);
					triAry.setNormal(currCoordIndex, n1);
					currCoordIndex++;
				    }
				    triAry.setCoordinate(currCoordIndex, p2);
				    triAry.setNormal(currCoordIndex, n1);
				    currCoordIndex++;		      
				    
				    if (!getNormal(p2, q1, q2, n1)) {
					n1.set(goodNormal);
				    } else {
					if (!flip_side_orient) {
					    n1.negate();
					}
					goodNormal.set(n1);
				    }
				    
				    if (flip_side_orient) {
					triAry.setCoordinate(currCoordIndex, p2);
					triAry.setNormal(currCoordIndex, n1);
					currCoordIndex++;
					
					triAry.setCoordinate(currCoordIndex, q1);
					triAry.setNormal(currCoordIndex, n1);
					currCoordIndex++;
				    } else {
					triAry.setCoordinate(currCoordIndex, q1);
					triAry.setNormal(currCoordIndex, n1);
					currCoordIndex++;
					
					triAry.setCoordinate(currCoordIndex, p2);
					triAry.setNormal(currCoordIndex, n1);
					currCoordIndex++;
				    }
				    triAry.setCoordinate(currCoordIndex, q2);
				    triAry.setNormal(currCoordIndex, n1);
				    currCoordIndex++;
				    
				    p1.x = q1.x;p1.y = q1.y;p1.z = q1.z;
				    p2.x = q2.x;p2.y = q2.y;p2.z = q2.z;
				}// for m
				p1.x = P2.x; p1.y = P2.y; p1.z = P2.z;
				q1.x = Q2.x; q1.y = Q2.y; q1.z = Q2.z;
				nn1.x = nn2.x;nn1.y = nn2.y;nn1.z = nn2.z;
			    }// for k
			    offset = num;
			}// for j
		    }//for i
		}// if shape
	    }// if fontExtrusion
	    geo = (GeometryArrayRetained) triAry.retained;
	    geomHash.put(ch, geo);
	} 
	
	return geo;
    }


    static boolean getNormal(Point3f p1, Point3f p2, Point3f p3, Vector3f normal) {
	Vector3f v1 = new Vector3f();
	Vector3f v2 = new Vector3f();
	
	// Must compute normal
	v1.sub(p2, p1);
	v2.sub(p2, p3);
	normal.cross(v1, v2);
	normal.negate();
	
	float length = normal.length();
	
	if (length > 0) {
	    length = 1 / length;
	    normal.x *= length;
	    normal.y *= length;
	    normal.z *= length;
	    return true;
	}
	return false;
    }

    
    // check if 2 contours are inside/outside/intersect one another
    // INPUT:
    // vertCnt1, vertCnt2  - number of vertices in 2 contours
    // begin1, begin2      - starting indices into vertices for 2 contours
    // vertices            - actual vertex data
    // OUTPUT:
    // status == 1   - intersecting contours
    //           2   - first contour inside the second 
    //           3   - second contour inside the first
    //           0   - disjoint contours(2 islands)
    
    static int check2Contours(int begin1, int end1, int begin2, int end2, 
			      Point3f[] vertices) {
	int i, j;
	boolean inside2, inside1;
	
	inside2 = pointInPolygon2D(vertices[begin1].x, vertices[begin1].y, 
				   begin2, end2, vertices);
	
	for (i=begin1+1; i < end1;i++) {
	    if (pointInPolygon2D(vertices[i].x, vertices[i].y, 
				 begin2, end2, vertices) != inside2) {
		return 1;	  //intersecting contours
	    }
	}
	
	// Since we are using point in polygon test and not 
	// line in polygon test. There are cases we miss the interesting
	// if we are not checking the reverse for all points. This happen
	// when two points form a line pass through a polygon but the two
	// points are outside of it.
	
	inside1 = pointInPolygon2D(vertices[begin2].x, vertices[begin2].y, 
				   begin1, end1, vertices);
	
	for (i=begin2+1; i < end2;i++) {
	    if (pointInPolygon2D(vertices[i].x, vertices[i].y, 
				 begin1, end1, vertices) != inside1) { 
		return 1; //intersecting contours
	    }
	}
	
	if (!inside2) {
	    if (!inside1) {  	
		return 0;   // disjoint countours
	    } 
	    // inside2 = false and inside1 = true
	    return 3;  // second contour inside first
	}
	
	// must be inside2 = true and inside1 = false
	// Note that it is not possible inside2 = inside1 = true
	// unless two contour overlap to each others.
	//
	return 2;  // first contour inside second
    }
    
    // Test if 2D point (x,y) lies inside polygon represented by verts.
    // z-value of polygon vertices is ignored. Sent only to avoid data-copy.
    // Uses ray-shooting algorithm to compute intersections along +X axis.
    // This algorithm works for all polygons(concave, self-intersecting) and
    // is best solution here due to large number of polygon vertices.
    // Point is INSIDE if number of intersections is odd, OUTSIDE if number
    // of intersections is even.
    static boolean pointInPolygon2D(float x, float y, int begIdx, int endIdx, 
				    Point3f[] verts){
	
	int i, num_intersections = 0;
	float xi;

	for (i=begIdx;i < endIdx-1;i++) {
	    if ((verts[i].y >= y && verts[i+1].y >= y) ||
		(verts[i].y <  y && verts[i+1].y <  y))
		continue;
	    
	    xi = verts[i].x + (verts[i].x - verts[i+1].x)*(y - verts[i].y)/
		(verts[i].y - verts[i+1].y);
	    
	    if (x < xi) num_intersections++;
	}
	
	// Check for segment from last vertex to first vertex.
	
	if (!((verts[i].y >= y && verts[begIdx].y >= y) ||
	      (verts[i].y <  y && verts[begIdx].y <  y))) {
		xi = verts[i].x + (verts[i].x - verts[begIdx].x)*(y - verts[i].y)/
		    (verts[i].y - verts[begIdx].y);
		
		if (x < xi) num_intersections++;
	    }
	
	return ((num_intersections % 2) != 0);
    }
    
    
    static final boolean normalize(Vector3f v) {
	float len = v.length();
	
	if (len > 0) {
	    len = 1.0f/len;
	    v.x *= len;
	    v.y *= len;
	    v.z *= len;
	    return true;
	} 
	return false;
    }    


    // A Tree of islands form based on contour, each parent's contour 
    // enclosed all the child. We built this since Triangular fail to
    // handle the case of multiple concentrated contours. i.e. if
    // 4 contours A > B > C > D. Triangular will fail recongized
    // two island, one form by A & B and the other by C & D.
    // Using this tree we can separate out every 2 levels and pass
    // in to triangular to workaround its limitation.
    static private class IslandsNode {

	private ArrayList islandsList = null;
	int startIdx, endIdx;

	IslandsNode(int startIdx, int endIdx) {
	    this.startIdx = startIdx;
	    this.endIdx = endIdx;
	    islandsList = null;
	}

	void addChild(IslandsNode node) {

	    if (islandsList == null) {
		islandsList = new ArrayList(5);
	    }
	    islandsList.add(node);
	}

	void removeChild(IslandsNode node) {
	    islandsList.remove(islandsList.indexOf(node));
	}

	IslandsNode getChild(int idx) {
	    return (IslandsNode) islandsList.get(idx);
	}

	int numChild() {
	    return (islandsList == null ? 0 : islandsList.size());
	}

	int numVertices() {
	    return endIdx - startIdx;
	}

	void insert(IslandsNode newNode, Point3f[] vertices) {
	    boolean createNewLevel = false;
	    
	    if (islandsList != null) {
		IslandsNode childNode;
		int status;
		
		for (int i=numChild()-1; i>=0; i--) {
		    childNode = getChild(i);
		    status = check2Contours(newNode.startIdx, newNode.endIdx,
					    childNode.startIdx, childNode.endIdx,
					    vertices);
		    switch (status) {
		    case 2: // newNode inside childNode, go down recursively
			childNode.insert(newNode, vertices);
			return;
		    case 3:// childNode inside newNode, 
			// continue to search other childNode also
			// inside this one and group them together.
			newNode.addChild(childNode);
			createNewLevel = true;
			break;
		    default: // intersecting or disjoint
			
		    }		
		}
	    }

	    if (createNewLevel) {
		// Remove child in newNode from this
		for (int i=newNode.numChild()-1; i>=0; i--) {
		    removeChild(newNode.getChild(i));
		}
		// Add the newNode to parent 
	    } 
	    addChild(newNode);
	}

	// Return a list of node with odd number of level
	void collectOddLevelNode(UnorderList list, int level) {
	    if ((level % 2) == 1) {
		list.add(this);
	    }
	    if (islandsList != null) {
		level++;
		for (int i=numChild()-1; i>=0; i--) {
		    getChild(i).collectOddLevelNode(list, level);
		}
	    }
	}
    }
}
