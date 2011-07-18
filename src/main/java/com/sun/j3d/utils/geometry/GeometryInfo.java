/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 */

package com.sun.j3d.utils.geometry;

import java.util.HashMap;

import javax.media.j3d.GeometryArray;
import javax.media.j3d.IndexedGeometryArray;
import javax.media.j3d.IndexedQuadArray;
import javax.media.j3d.IndexedTriangleArray;
import javax.media.j3d.IndexedTriangleFanArray;
import javax.media.j3d.IndexedTriangleStripArray;
import javax.media.j3d.QuadArray;
import javax.media.j3d.TriangleArray;
import javax.media.j3d.TriangleFanArray;
import javax.media.j3d.TriangleStripArray;
import javax.vecmath.Color3b;
import javax.vecmath.Color3f;
import javax.vecmath.Color4b;
import javax.vecmath.Color4f;
import javax.vecmath.Point2f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.TexCoord3f;
import javax.vecmath.TexCoord4f;
import javax.vecmath.Tuple2f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Tuple4f;
import javax.vecmath.Vector3f;

import com.sun.j3d.internal.ByteBufferWrapper;
import com.sun.j3d.internal.ByteOrderWrapper;
import com.sun.j3d.internal.FloatBufferWrapper;
import com.sun.j3d.internal.J3dUtilsI18N;

/**
 * The GeometryInfo object holds data for processing by the Java3D geometry
 * utility tools.<p><blockquote>
 * 
 *         The NormalGenerator adds normals to geometry without normals.<p>
 * 
 *         The Stripifier combines adjacent triangles into triangle strips for
 *         more efficent rendering.<p></blockquote>
 * 
 * Also, the GeometryCompressor can take a set of GeometryInfo objects in a
 * CompressionSteam and generate a CompressedGeometry object from the
 * geometry.<p>
 *         Geometry is loaded into a GeometryInfo in a manner similar to the
 * <a href="../../../../../javax/media/j3d/GeometryArray.html">
 * GeometryArray</a> methods.  The constructor for the GeometryInfo takes a flag
 * that specifies the kind of data being loaded.  The vertex data is
 * specified using methods that are similar to the GeometryArray methods, but
 * with fewer variations.<p>
 *         The major difference between GeometryInfo and GeometryArray is
 * that the number of vertices, vertex format, and other data are specified
 * implictly, rather than as part of the constructor.  The number of verticies
 * comes from the number of coordinates passed to the setCoordinates()
 * method.  The format comes from the set of data components that are
 * specified.  For example, calling the setCoordinates(), setColors3() and
 * setTextureCoordinatesParames(1, 2) methods implies a
 * format of COORDINATES | COLOR_3
 * | TEXTURE_COORDINATE_2.  Indexed representation is specified by calling
 * the methods that specify the indices, for example
 * setCoordinateIndices().<p>
 *         Stripped primitives are loaded using the TRIANGLE_FAN_ARRAY or
 * TRIANGLE_STRIP_ARRAY flags to the constructor.  The setStripCounts()
 * method specifies the length of each strip.<p>
 *         A set of complex polygons is loaded using the POLYGON_ARRAY
 * flag to the constructor.  The setStripCounts() method specifies the length
 * of each contour of the polygons.  The setContourCounts() method specifies
 * the number of countours in each polygon. For example, a triangle with a
 * triangular hole would have strip counts [3, 3] (indicating two contours of
 * three points) and contour counts [2] (indicating a single polygon with two
 * contours).<p>
 *         GeometryInfo itelf contains some simple utilities, such as
 * calculating indices for non-indexed data ("indexifying") and getting rid
 * of unused data in your indexed geometry ("compacting").<p>
 *         The geometry utility tools modify the contents of the
 * GeometryInfo.  After processing, the resulting geometry can be extracted
 * from the GeometryInfo by calling getGeometryArray().  If multiple tools
 * are used, the order of processing should be: generate normals, then
 * stripify.  For example, to convert a general mesh of polygons without
 * normals into an optimized mesh call:
 * <pre><blockquote>
 *         GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
 *         // initialize the geometry info here
 *         // generate normals
 *         NormalGenerator ng = new NormalGenerator();
 *         ng.generateNormals(gi);
 *         // stripify
 *         Stripifier st = new Stripifier();
 *         st.stripify(gi);
 *         GeometryArray result = gi.getGeometryArray();
 * </blockquote></pre>
 * 
 * @see NormalGenerator
 * @see Stripifier
 * @see com.sun.j3d.utils.compression.CompressionStream
 * @see com.sun.j3d.utils.compression.GeometryCompressor
 * @see javax.media.j3d.GeometryArray
 */

public class GeometryInfo {

  /**
   * Send to the constructor to inform that the data will be arranged so
   * that each set of three vertices form an independent triangle
   */
  public static final int TRIANGLE_ARRAY = 1;

  /**
   * Send to the constructor to inform that the data will be arranged so
   * that each set of four vertices form an independent quad
   */
  public static final int QUAD_ARRAY = 2;

  /**
   * Send to the constructor to inform that the data will be arranged so
   * that the stripCounts array indicates how many vertices to use
   * for each triangle fan.
   */
  public static final int TRIANGLE_FAN_ARRAY = 3;

  /**
   * Send to the constructor to inform that the data will be arranged so
   * that the stripCounts array indicates how many vertices to use
   * for each triangle strip.
   */
  public static final int TRIANGLE_STRIP_ARRAY = 4;

  /**
   * Send to the constructor to inform that the data is arranged as
   * possibly multi-contour, possible non-planar polygons.
   * The stripCounts array indicates how many vertices to use
   * for each contour, and the contourCounts array indicates how many
   * stripCounts entries to use for each polygon.  The first 
   * contour is the bounding polygon, and subsequent contours are
   * "holes."  If contourCounts is left null, the default is
   * one contour per polygon.
   */
  public static final int POLYGON_ARRAY = 5;

  private int prim;
  
  // 1 Show indexification details
  private static final int DEBUG = 0;
  
  private Point3f coordinates[] = null;
  private Color3f colors3[] = null;
  private Color4f colors4[] = null;
  private Vector3f normals[] = null;
  private Object texCoordSets[][] = null;		

  private int coordinateIndices[] = null;
  private int colorIndices[] = null;
  private int normalIndices[] = null;
  private int texCoordIndexSets[][] = null;

  private int[] texCoordSetMap = null;
  private int texCoordSetCount = 0;
  private int texCoordDim = 0;
  
  private int stripCounts[] = null;
  private int contourCounts[] = null;

  private Triangulator tr = null;
  private NormalGenerator ng = null;

  private int oldPrim = 0;
  private int oldStripCounts[] = null;

  private boolean coordOnly = false;



  /**
   * Constructor.
   * Creates an empty GeometryInfo object.  
   * @param primitive Tells the GeometryInfo object the type of
   * primitive data to be stored
   * in it, so it will know the format of the data. It can be one of
   * TRIANGLE_ARRAY, 
   * QUAD_ARRAY, TRIANGLE_FAN_ARRAY, TRIANGLE_STRIP_ARRAY, or POLYGON_ARRAY.
   */
  public GeometryInfo(int primitive)
  {
      if ((primitive >= TRIANGLE_ARRAY) && (primitive <= POLYGON_ARRAY)) {
	  prim = primitive;
      } else {
	  throw new IllegalArgumentException(
	      J3dUtilsI18N.getString("GeometryInfo0"));
      }
  } // End of GeometryInfo(int)



  /**
   * Contructor.  Populates the GeometryInfo with the geometry from
   * the GeometryArray.<p>
   * If the GeometryArray uses the <code>Initial</code> and 
   * <code>Valid</code> GeometryArray methods (<code>
   * setInitialVertexIndex()</code> and <code>setValidVertexCount()
   * </code> and their cousins) then only the needed geometry
   * is copied into the GeometryInfo.
   */
  public GeometryInfo(GeometryArray ga)
  {
    GeometryInfoGenerator.create(this, ga);
  } // End of GeometryInfo(GeometryArray)



  /**
   * Removes all data from the GeometryInfo and resets the primitive.
   * After a call to reset(), the GeometryInfo object will be just like
   * it was when it was newly constructed.
   * @param primitive Either TRIANGLE_ARRAY, QUAD_ARRAY,
   * TRIANGLE_FAN_ARRAY, TRIANGLE_STRIP_ARRAY, or POLYGON_ARRAY.
   * Tells the GeometryInfo object the type of primitive data to be stored
   * in it, so it will know the format of the data.
   */
  public void reset(int primitive)
  {
      if ((primitive >= TRIANGLE_ARRAY) && (primitive <= POLYGON_ARRAY)) {
	  prim = primitive;
      } else {
	  throw new IllegalArgumentException(
	      J3dUtilsI18N.getString("GeometryInfo0"));
      }
      
      coordinates = null;
      colors3 = null;
      colors4 = null;
      normals = null;
      
      coordinateIndices = null;
      colorIndices = null;
      normalIndices = null;
      
      stripCounts = null;
      contourCounts = null;
      
      oldPrim = 0;
      oldStripCounts = null;
      
      texCoordDim = 0;
      texCoordSetCount = 0;
      texCoordSets = null;
      texCoordIndexSets = null;
      texCoordSetMap = null;

      coordOnly = false;
      
  } // End of reset(int)



  /**
   * Removes all data from this GeometryInfo and populates it with
   * the geometry from the GeometryArray.
   */
  public void reset(GeometryArray ga)
  {
    GeometryInfoGenerator.create(this, ga);
  } // End of reset(GeometryArray)



  // This method takes an indexed quad array and expands it to
  // a list of indexed triangles.  It is used for the Coordinate
  // indices as well as the color and texture indices.
  private int[] expandQuad(int indices[])
  {
      int triangles[] = new int[indices.length / 4 * 6];
      
      for (int i = 0 ; i < indices.length / 4 ; i++ ) {
	  triangles[i * 6 + 0] = indices[i * 4];
	  triangles[i * 6 + 1] = indices[i * 4 + 1];
	  triangles[i * 6 + 2] = indices[i * 4 + 2];
	  triangles[i * 6 + 3] = indices[i * 4];
	  triangles[i * 6 + 4] = indices[i * 4 + 2];
	  triangles[i * 6 + 5] = indices[i * 4 + 3];
      }
      
      return triangles;
  } // End of expandQuad



  // This method takes an indexed triangle fan and expands it to
  // a list of indexed triangles.  It is used for the Coordinate
  // indices as well as the color and texture indices.
  private int[] expandTriFan(int numTris, int indices[])
  {
      int triangles[] = new int[numTris * 3];
      int p = 0;
      int base = 0;
      for (int f = 0 ; f < stripCounts.length ; f++) {
	  for (int t = 0 ; t < stripCounts[f] - 2 ; t++) {
	      triangles[p++] = indices[base];
	      triangles[p++] = indices[base + t + 1];
	      triangles[p++] = indices[base + t + 2];
	  }
	  base += stripCounts[f];
      }
      return triangles;
  } // End of expandTriFan



  // This method takes an indexed triangle strip and expands it to
  // a list of indexed triangles.  It is used for the Coordinate
  // indices as well as the color and texture indices.
  private int[] expandTriStrip(int numTris, int indices[])
  {
      int triangles[] = new int[numTris * 3];
      
      int p = 0;
      int base = 0;
      for (int s = 0 ; s < stripCounts.length ; s++) {
	  for (int t = 0 ; t < stripCounts[s] - 2 ; t++) {
	      
	      // Use a ping-ponging algorithm to reverse order on every other
	      // triangle to preserve winding
	      if (t % 2 == 0) {
		  triangles[p++] = indices[base + t + 0];
		  triangles[p++] = indices[base + t + 1];
		  triangles[p++] = indices[base + t + 2];
	      } else {
		  triangles[p++] = indices[base + t + 0];
		  triangles[p++] = indices[base + t + 2];
		  triangles[p++] = indices[base + t + 1];
	      }
	  }
	  base += stripCounts[s];
      }
      
      return triangles;
  } // End of expandTriStrip



  // Used by the NormalGenerator utility.  Informs the GeometryInfo object
  // to remember its current primitive and stripCounts arrays so that
  // they can be used to convert the object back to its original 
  // primitive
  void rememberOldPrim()
  {
      oldPrim = prim;
      oldStripCounts = stripCounts;
  } // End of rememberOldPrim



  // The NormalGenerator needs to know the original primitive for
  // facet normal generation for quads
  int getOldPrim()
  {
      return oldPrim;
  } // End of getOldPrim



  // Used by the Utility libraries other than the NormalGenerator.
  // Informs the GeometryInfo object that the geometry need not
  // be converted back to the original primitive before returning.
  // For example, if a list of Fans is sent, converted to Triangles
  // for normal generation, and then stripified by the Stripifyer,
  // we want to make sure that GeometryInfo doesn't convert the
  // geometry *back* to fans before creating the output GeometryArray.
  void forgetOldPrim()
  {
      oldPrim = 0;
      oldStripCounts = null;
  } // End of forgetOldPrim



  // We have changed the user's data from their original primitive
  // type to TRIANGLE_ARRAY.  If this method is being called, it
  // means we need to change it back (to try and hide from the user
  // the fact that we've converted).  This usually happens when
  // the user has used GeometryInfo for generating normals, but
  // they are not Stripifying or Triangulating.  The function is
  // called from getGeometryArray before creating the output data.
  private void changeBackToOldPrim()
  {
      if (oldPrim != 0) {
	  convertToIndexedTriangles();
	  if (ng == null) ng = new NormalGenerator();
	  ng.convertBackToOldPrim(this, oldPrim, oldStripCounts);
	  oldPrim = 0;
	  oldStripCounts = null;
      }
  } // End of changeBackToOldPrim



  /**
   * Convert the GeometryInfo object to have primitive type TRIANGLE_ARRAY
   * and be indexed.
   * @throws IllegalArgumentException if coordinate data is missing,
   * if the index lists aren't all the
   * same length, if an index list is set and the corresponding data
   * list isn't set, if a data list is set and the corresponding
   * index list is unset (unless all index lists are unset or in
   * USE_COORD_INDEX_ONLY format),
   * if StripCounts or ContourCounts is inconsistent with the current
   * primitive, if the sum of the contourCounts array doesn't equal
   * the length of the StripCounts array, or if the number of vertices
   * isn't a multiple of three (for triangles) or four (for quads).
   */
  public void convertToIndexedTriangles()
  {
      int triangles = 0;
      
      // This calls checkForBadData
      indexify();
      
      if (prim == TRIANGLE_ARRAY) return;
      
      switch(prim) {
	  
	  case QUAD_ARRAY:
	      
	      coordinateIndices = expandQuad(coordinateIndices);
	      if (colorIndices != null) colorIndices = expandQuad(colorIndices);
	      if (normalIndices != null)
		  normalIndices = expandQuad(normalIndices);
	      for (int i = 0 ; i < texCoordSetCount ; i++)
		  texCoordIndexSets[i] = expandQuad(texCoordIndexSets[i]);
	      break;
	      
	  case TRIANGLE_FAN_ARRAY:
	      // Count how many triangles are in the object
	      for (int i = 0 ; i < stripCounts.length ; i++) {
		  triangles += stripCounts[i] - 2;
	      }
	      
	      coordinateIndices = expandTriFan(triangles, coordinateIndices);
	      if (colorIndices != null)
		  colorIndices = expandTriFan(triangles, colorIndices);
	      if (normalIndices != null)
		  normalIndices = expandTriFan(triangles, normalIndices);
	      for (int i = 0 ; i < texCoordSetCount ; i++)
		  texCoordIndexSets[i] = expandTriFan(triangles, 
			      texCoordIndexSets[i]);
	      break;
	      
	  case TRIANGLE_STRIP_ARRAY:
	      // Count how many triangles are in the object
	      for (int i = 0 ; i < stripCounts.length ; i++) {
		  triangles += stripCounts[i] - 2;
	      }
	      
	      coordinateIndices = expandTriStrip(triangles, coordinateIndices);
	      if (colorIndices != null)
		  colorIndices = expandTriStrip(triangles, colorIndices);
	      if (normalIndices != null)
		  normalIndices = expandTriStrip(triangles, normalIndices);
	      for (int i = 0 ; i < texCoordSetCount ; i++)
		  texCoordIndexSets[i] = expandTriStrip(triangles, 
			      texCoordIndexSets[i]);
	      break;
	      
	  case POLYGON_ARRAY:
	      if (tr == null) tr = new Triangulator();
	      tr.triangulate(this);
	      break;
      }
      
      prim = TRIANGLE_ARRAY;
      stripCounts = null;
  } // End of convertToIndexedTriangles



  /**
   * Get the current primitive.  Some of the utilities may change the
   * primitive type of the data stored in the GeometryInfo object
   * (for example, the stripifyer will change it to TRIANGLE_STRIP_ARRAY).
   */
  public int getPrimitive()
  {
      return prim;
  } // End of getPrimitive()



  /**
   * Set the current primitive.  Some of the utilities may change the
   * primitive type of the data stored in the GeometryInfo object
   * (for example, the stripifyer will change it to TRIANGLE_STRIP_ARRAY).
   * But the user can't change the primitive type - it is set in the 
   * constructor.  Therefore, this method has package scope.
   */
  void setPrimitive(int primitive)
  {
      if ((prim >= TRIANGLE_ARRAY) && (prim <= POLYGON_ARRAY)) {
	  prim = primitive;
      } else {
	  throw new IllegalArgumentException(
		  J3dUtilsI18N.getString("GeometryInfo0"));
      }
  } // End of setPrimitive()



  /**
   * Sets the coordinates array.  
   * No data copying is done because a reference to user data is used.
   */
  public void setCoordinates(Point3f coordinates[])
  {
      this.coordinates = coordinates;
  } // End of setCoordinates



  /**
   * Sets the coordinates array.
   * The points are copied into the GeometryInfo object.
   */
  public void setCoordinates(Point3d coordinates[])
  {
      if (coordinates == null) this.coordinates = null;
      else {
	  this.coordinates = new Point3f[coordinates.length];
	  for (int i = 0 ; i < coordinates.length ; i++) {
	      this.coordinates[i] = new Point3f(
		      (float)(coordinates[i].x),
		      (float)(coordinates[i].y),
		      (float)(coordinates[i].z));
	  }
      }
  } // End of setCoordinates



  /**
   * Sets the coordinates array.
   * The points are copied into the GeometryInfo object.
   */
  public void setCoordinates(float coordinates[])
  {
      if (coordinates == null) this.coordinates = null;
      else {
	  this.coordinates = new Point3f[coordinates.length / 3];
	  for (int i = 0 ; i < this.coordinates.length ; i++) {
	      this.coordinates[i] = new Point3f(coordinates[i * 3],
		      				coordinates[i * 3 + 1],
		      				coordinates[i * 3 + 2]);
	  }
      }
  } // End of setCoordinates



  /**
   * Sets the coordinates array.
   * The points are copied into the GeometryInfo object.
   */
  public void setCoordinates(double coordinates[])
  {
      if (coordinates == null) this.coordinates = null;
      else {
	  this.coordinates = new Point3f[coordinates.length / 3];
	  for (int i = 0 ; i < coordinates.length / 3 ; i++) {
	      this.coordinates[i] = new Point3f((float)coordinates[i * 3],
					        (float)coordinates[i * 3 + 1],
					        (float)coordinates[i * 3 + 2]);
	  }
      }
  } // End of setCoordinates



  /**
   * Retrieves a reference to the coordinate array.
   */
  public Point3f[] getCoordinates()
  {
      return coordinates;
  } // End of getCoordinates
  


  /**
   * Sets the colors array.
   * No data copying is done because a reference to
   * user data is used.
   */
  public void setColors(Color3f colors[])
  {
      colors3 = colors;
      colors4 = null;
  } // End of setColors
  


  /**
   * Sets the colors array.
   * No data copying is done because a reference to
   * user data is used.
   */
  public void setColors(Color4f colors[])
  {
      colors3 = null;
      colors4 = colors;
  } // End of setColors
 


  /**
   * Sets the colors array.
   * The points are copied into the GeometryInfo object.
   */
  public void setColors(Color3b colors[])
  {
      if (colors == null) {
	  colors3 = null;
	  colors4 = null;
      } else {
	  colors3 = new Color3f[colors.length];
	  colors4 = null;
	  for (int i = 0 ; i < colors.length ; i++) {
	      colors3[i] = new Color3f((float) (colors[i].x & 0xff) / 255.0f,
		      		       (float) (colors[i].y & 0xff) / 255.0f,
		      		       (float) (colors[i].z & 0xff) / 255.0f);
	  }
      }
  } // End of setColors
 


  /**
   * Sets the colors array.
   * The points are copied into the GeometryInfo object.
   */
  public void setColors(Color4b colors[])
  {
      if (colors == null) {
	  colors3 = null;
	  colors4 = null;
      } else {
	  colors3 = null;
	  colors4 = new Color4f[colors.length];
	  for (int i = 0 ; i < colors.length ; i++) {
	      colors4[i] = new Color4f((float) (colors[i].x & 0xff) / 255.0f,
		      		       (float) (colors[i].y & 0xff) / 255.0f,
		      		       (float) (colors[i].z & 0xff) / 255.0f,
		      		       (float) (colors[i].w & 0xff) / 255.0f);
	  }
      }
  } // End of setColors
 


  /**
   * Sets the colors array.
   * The points are copied into the GeometryInfo object, assuming
   * 3 components (R, G, and B) per vertex.
   */
  public void setColors3(float colors[])
  {
      if (colors == null) {
	  colors3 = null;
	  colors4 = null;
      } else {
	  colors3 = new Color3f[colors.length / 3];
	  colors4 = null;
	  for (int i = 0 ; i < colors.length / 3 ; i++) {
	      colors3[i] = new Color3f(colors[i * 3],
				       colors[i * 3 + 1],
				       colors[i * 3 + 2]);
	  }
      }
  } // End of setColors3
 


  /**
   * Sets the colors array.
   * The points are copied into the GeometryInfo object, assuming
   * 4 components (R, G, B, and A) per vertex.
   */
  public void setColors4(float colors[])
  {
      if (colors == null) {
	  colors3 = null;
	  colors4 = null;
      } else {
	  colors3 = null;
	  colors4 = new Color4f[colors.length / 4];
	  for (int i = 0 ; i < colors.length / 4 ; i++) {
	      colors4[i] = new Color4f(colors[i * 4],
		      		       colors[i * 4 + 1],
		      		       colors[i * 4 + 2],
		      		       colors[i * 4 + 3]);
	  }
      }
  } // End of setColors4
 


  /**
   * Sets the colors array.
   * The points are copied into the GeometryInfo object, assuming
   * 3 components (R, G, and B) per vertex.
   */
  public void setColors3(byte colors[])
  {
      if (colors == null) {
	  colors3 = null;
	  colors4 = null;
      } else {
	  colors3 = new Color3f[colors.length / 3];
	  colors4 = null;
	  for (int i = 0 ; i < colors.length / 3 ; i++) {
	      colors3[i] =
		  new Color3f((float)(colors[i * 3] & 0xff) / 255.0f,
		      	      (float)(colors[i * 3 + 1] & 0xff) / 255.0f,
			      (float)(colors[i * 3 + 2] & 0xff) / 255.0f);
	  }
      }
  } // End of setColors3



  /**
   * Sets the colors array.
   * The points are copied into the GeometryInfo object, assuming
   * 4 components (R, G, B, and A) per vertex.
   */
  public void setColors4(byte colors[])
  {
      if (colors == null) {
	  colors3 = null;
	  colors4 = null;
      } else {
	  colors3 = null;
	  colors4 = new Color4f[colors.length / 4];
	  for (int i = 0 ; i < colors.length / 4 ; i++) {
	      colors4[i] =
		  new Color4f((float)(colors[i * 4] & 0xff) / 255.0f,
			      (float)(colors[i * 4 + 1] & 0xff) / 255.0f,
			      (float)(colors[i * 4 + 2] & 0xff) / 255.0f,
			      (float)(colors[i * 4 + 3] & 0xff) / 255.0f);
	  }
      }
  } // End of setColors4



  /**
   * Retrieves a reference to the colors array.  Will be either
   * <code>Color3f[]</code> or <code>Color4f[]</code> depending on
   * the type of the input data.  Call
   * getNumColorComponents() to find out which version is returned.
   */
  public Object[] getColors()
  {
      if (colors3 != null) return colors3;
      else return colors4;
  } // End of getColors



  /**
   * Returns the number of color data components stored per vertex
   * in the current GeometryInfo object (3 for RGB or 4 for RGBA).
   * If no colors are currently defined, 0 is returned.
   */
  public int getNumColorComponents()
  {
      if (colors3 != null) return 3;
      else if (colors4 != null) return 4;
      else return 0;
  } // End of getNumColorComponents



  /**
   * Sets the normals array.
   * No data copying is done because a reference to
   * user data is used.
   */
  public void setNormals(Vector3f normals[])
  {
      this.normals = normals;
  } // End of setNormals



  /**
   * Sets the normals array.
   * The points are copied into the GeometryInfo object.
   */
  public void setNormals(float normals[])
  {
      if (normals == null) this.normals = null;
      else {
	  this.normals = new Vector3f[normals.length / 3];
	  for (int i = 0 ; i < this.normals.length ; i++) {
	      this.normals[i] = new Vector3f(normals[i * 3],
		      			     normals[i * 3 + 1],
		      			     normals[i * 3 + 2]);
	  }
      }
  } // End of setNormals(float[])



  /**
   * Retrieves a reference to the normal array.
   */
  public Vector3f[] getNormals()
  {
      return normals;
  } // End of getNormals



  /**
   * This method is used to specify the number of texture coordinate sets  
   * and the dimensionality of the texture coordinates.
   * The number of texture coordinate sets must be specified to the GeometryInfo
   * class before any of the sets are specified. The dimensionality of the 
   * texture coordinates may be 2, 3, or 4, corresponding to 2D, 3D, or 4D 
   * texture coordinates respectively.(All sets must have the same 
   * dimensionality.) The default is zero, 2D texture coordinate sets. 
   * This method should be called before any texture coordinate sets are 
   * specified because <b>calling this method will delete all previously
   * specified texture coordinate and texture coordinate index arrays</b> 
   * associated with this GeometryInfo.  For example: 
   * <blockquote><pre>
   *	geomInfo.setTextureCoordinateParams(2, 3);
   *	geomInfo.setTextureCoordinates(0, tex0);
   *	geomInfo.setTextureCoordinates(1, tex1);
   *	geomInfo.setTextureCoordinateParams(1, 2);
   *	geomInfo.getTexCoordSetCount();
   * </blockquote></pre>
   * The second call to <code>setTextureCoordinateParams</code> will erase all 
   * the texture coordinate arrays, so the subsequent call to <code>
   * getTexCoordSetCount</code> will return 1.
   * @param numSets The number of texture coordinate sets that will be 
   * specified for this GeometryInfo object.
   * @param dim The dimensionality of the texture coordinates. Has to be 2, 3 
   * or 4.
   * @throws IllegalArgumentException if the dimensionality of the texture 
   * coordinates is not one of 2, 3 or 4.
   */
  public void setTextureCoordinateParams(int numSets, int dim)
  {
      if (dim == 2) {
	  texCoordSets = new TexCoord2f[numSets][];
      } else if (dim == 3) {
	  texCoordSets = new TexCoord3f[numSets][];
      } else if (dim == 4) {
	  texCoordSets = new TexCoord4f[numSets][];
      } else {
          throw new IllegalArgumentException(
	      J3dUtilsI18N.getString("GeometryInfo9"));
      }
      texCoordIndexSets = new int[numSets][];
      texCoordDim = dim;
      texCoordSetCount = numSets;
  } // End of setTextureCoordinateParams
  


  /**
   * Returns the number of texture coordinate sets in this GeometryInfo.
   * This value is set with setTextureCoordinateParams().
   * If setTextureCoordinateParams()
   * has not been called, 0 is returned unless one of the deprecated
   * texture coordinate methods has been called.  Calling one of the
   * deprecated texture coordinate methods sets the count to 1.
   * The deprecated texture coordinate methods are those that don't
   * take texCoordSet as the first parameter.
   * @return the number of texture coordinate sets in this
   * GeometryInfo.
   */
  public int getTexCoordSetCount() {
      return texCoordSetCount;
  }



  /**
   * Returns the number of texture coordinate components that are stored
   * per vertex.  Returns 2 for ST (2D), 3 for STR (3D),
   * or 4 for STRQ (4D), aslo known as the "dimensionality" of the
   * coordinates.  This value is set with 
   * setTextureCoordinateParams().  If setTextureCoordinateParams()
   * has not been called, 0 is returned unless one of the deprecated
   * texture coordinate methods has been called.  Calling one of the
   * deprecated texture coordinate methods sets the dimensionality
   * explicitly (if you called setTextureCoordinates(Point2f[]) then
   * 2 is returned).
   * The deprecated texture coordinate methods are those that don't
   * take texCoordSet as the first parameter.
   */
  public int getNumTexCoordComponents()
  {
      return texCoordDim;
  } // End of getNumTexCoordComponents



  /**
   * Sets the mapping between texture coordinate sets and texture units.
   * See the 
   * <a href="../../../../../javax/media/j3d/GeometryArray.html#texCoordSetMap">
   * GeometryArray constructor </a> for further details.
   * <p> <b>Note:</b> If the texCoordSetMap is not set, multi-texturing is 
   * turned off. Only the texture coordinate set at index 0 (if set) will be 
   * used. Any other sets specified by the GeometryInfo.setTextureCoordinate* 
   * methods will be ignored.
   */
  public void setTexCoordSetMap(int map[]) {
      texCoordSetMap = map;
  }



  /**
   * Returns a reference to the texture coordinate set map.
   * See the 
   * <a href="../../../../../javax/media/j3d/GeometryArray.html#texCoordSetMap">
   * GeometryArray constructor </a> for further details.
   */
  public int[] getTexCoordSetMap() {
      return texCoordSetMap;
  }
 


  /**
   * Sets the 2D texture coordinates for the specified set.
   * No data copying is done - a reference to user data is used. 
   * @param texCoordSet The texture coordinate set for which these 
   * coordinates are being specified.
   * @param texCoords Array of 2D texture coordinates.
   * @throws IllegalArgumentException if <code>texCoordSet </code> < 0 or
   * <code>texCoordSet >= texCoordSetCount</code>,
   * or the texture coordinate parameters were not previously set by
   * calling <code>setTextureCoordinateParams(texCoordSetCount, 2)</code>.
   */
  public void setTextureCoordinates(int texCoordSet, TexCoord2f texCoords[])
  {
      if (texCoordDim != 2)
	  throw new IllegalArgumentException(
		  J3dUtilsI18N.getString("GeometryInfo15"));
      if ((texCoordSet >= texCoordSetCount) || (texCoordSet < 0))
	  throw new IllegalArgumentException(
		  J3dUtilsI18N.getString("GeometryInfo18"));

      texCoordSets[texCoordSet] = texCoords;
  } // End of setTextureCoordinates(int, TexCoord3f[])
  
 

  /**
   * Sets the TextureCoordinates array by copying the data
   * into the GeometryInfo object.
   * This method sets the number of texture coordinate sets to 1,
   * sets the dimensionality of the texture coordinates to 2,
   * and sets the coordinates for texture coordinate set 0.
   * @deprecated As of Java 3D 1.3 replaced by 
   * <code>setTextureCoordinates(int texCoordSet, TexCoord2f coords[])</code>
   */
  public void setTextureCoordinates(Point2f texCoords[])
  {
      texCoordSetCount = 1;
      texCoordDim = 2;
      texCoordSets = new TexCoord2f[1][];
      if (texCoords != null) {
	TexCoord2f[] tex = new TexCoord2f[texCoords.length];
	for (int i = 0 ; i < texCoords.length ; i++) 
	    tex[i] = new TexCoord2f(texCoords[i]);
	texCoordSets[0] = tex;
      }
  } // End of setTextureCoordinates(Point2f[])
 


  /**
   * Sets the texture coordinates array for the specified set.
   * No data copying is done - a reference to user data is used. 
   * @param texCoordSet The texture coordinate set for which these coordinates 
   * are being specified.
   * @param texCoords Array of 3D texture coordinates.
   * @throws IllegalArgumentException if <code> texCoordSet </code> < 0 or
   * <code>texCoordSet >= texCoordSetCount</code>,
   * or the texture coordinate parameters were not previously set by
   * calling <code>setTextureCoordinateParams(texCoordSetCount, 3)</code>.
   */
  public void setTextureCoordinates(int texCoordSet, TexCoord3f texCoords[])
  {
      if (texCoordDim != 3)
	  throw new IllegalArgumentException(
		  J3dUtilsI18N.getString("GeometryInfo16"));
      if ((texCoordSet >= texCoordSetCount) || (texCoordSet < 0))
	  throw new IllegalArgumentException(
		  J3dUtilsI18N.getString("GeometryInfo18"));
      
      texCoordSets[texCoordSet] = texCoords;
  } // End of setTextureCoordinates(int, TexCoord3f[])



  /**
   * Sets the TextureCoordinates array by copying the data
   * into the GeometryInfo object.
   * This method sets the number of texture coordinate sets to 1,
   * sets the dimensionality of the texture coordinates to 3,
   * and sets the coordinates for texture coordinate set 0.
   * @deprecated As of Java 3D 1.3 replaced by 
   * <code>setTextureCoordinates(int texCoordSet, TexCoord3f coords[])</code>
   */
  public void setTextureCoordinates(Point3f texCoords[])
  {
      texCoordSetCount = 1;
      texCoordDim = 3;
      texCoordSets = new TexCoord3f[1][];
      if (texCoords != null) {
	TexCoord3f[] tex = new TexCoord3f[texCoords.length];
	for (int i = 0 ; i < texCoords.length ; i++) 
	    tex[i] = new TexCoord3f(texCoords[i]);
	texCoordSets[0] = tex;
      }
  } // End of setTextureCoordinates(Point3f[])
 


  /**
   * Sets the texture coordinates array for the specified set.
   * No data copying is done - a reference to user data is used. 
   * @param texCoordSet The texture coordinate set for which these coordinates 
   * are being specified.
   * @param texCoords Array of 4D texture coordinates.
   * @throws IllegalArgumentException if <code> texCoordSet </code> < 0 or
   * <code>texCoordSet >= texCoordSetCount</code>,
   * or the texture coordinate parameters were not previously set by
   * calling <code>setTextureCoordinateParams(texCoordSetCount, 4)</code>.
   */
  public void setTextureCoordinates(int texCoordSet, TexCoord4f texCoords[]) {
      if (texCoordDim != 4)
	  throw new IllegalArgumentException(
		  J3dUtilsI18N.getString("GeometryInfo17"));
      if ((texCoordSet >= texCoordSetCount) || (texCoordSet < 0))
	  throw new IllegalArgumentException(
		  J3dUtilsI18N.getString("GeometryInfo18"));
      
      texCoordSets[texCoordSet] = texCoords;
  } // End of setTextureCoordinates(int, TexCoord4f[])
 


  /**
   * Sets the texture coordinates array by copying the data into the
   * GeometryInfo object.  The number of sets and dimensionality of
   * the sets must have been set previously with 
   * setTextureCoordinateParams(texCoordSetCount, dim).
   * @param texCoordSet The texture coordinate set for which these coordinates 
   * are being specified.
   * @param texCoords The float array of texture coordinates. For n texture 
   * coordinates with dimensionality d, there must be d*n floats in the array.
   * @throws IllegalArgumentException if <code>texCoordSet </code> < 0 or
   * <code>texCoordSet >= texCoordSetCount</code>,
   * or the texture coordinate parameters were not previously set by
   * calling <code>setTextureCoordinateParams</code>.
   */
  public void setTextureCoordinates(int texCoordSet, float texCoords[])
  {
      if ((texCoords.length % texCoordDim) != 0)
	  throw new IllegalArgumentException(
		  J3dUtilsI18N.getString("GeometryInfo2"));
      
      // Copy the texCoords into this GeometryInfo object
      if (texCoordDim == 2) {
	TexCoord2f tcoords[] = new TexCoord2f[texCoords.length / 2];
	for (int i = 0 ; i < tcoords.length ; i++) 
	    tcoords[i] = new TexCoord2f(texCoords[i * 2],
					texCoords[i * 2 + 1]);
	setTextureCoordinates(texCoordSet, tcoords);
      } else if (texCoordDim == 3) {
	TexCoord3f tcoords[] = new TexCoord3f[texCoords.length / 3];
	for (int i = 0 ; i < tcoords.length ; i++) 
	    tcoords[i] = new TexCoord3f(texCoords[i * 3],
					texCoords[i * 3 + 1],
					texCoords[i * 3 + 2]);
	setTextureCoordinates(texCoordSet, tcoords);
      } else if (texCoordDim == 4) {
	TexCoord4f tcoords[] = new TexCoord4f[texCoords.length / 4];
	for (int i = 0 ; i < tcoords.length ; i++) 
	    tcoords[i] = new TexCoord4f(texCoords[i * 4],
					texCoords[i * 4 + 1],
					texCoords[i * 4 + 2],
					texCoords[i * 4 + 3]);
	setTextureCoordinates(texCoordSet, tcoords);
      } else {
	  throw new IllegalArgumentException(
		  J3dUtilsI18N.getString("GeometryInfo21"));
      }
  } // End of setTextureCoordinates(int, float[])



  /**
   * Sets the texture coordinates array by copying the data
   * into the GeometryInfo object, assuming two numbers
   * (S and T) per vertex.
   * This method sets the number of texture coordinate sets to 1,
   * sets the dimensionality of the texture coordinates to 2,
   * and sets the coordinates for texture coordinate set 0.
   * @deprecated As of Java 3D 1.3 replaced by 
   * <code>setTextureCoordinates(int texCoordSet, float texCoords[])</code>
   */
  public void setTextureCoordinates2(float texCoords[])
  {
      texCoordSetCount = 1;
      texCoordDim = 2;
      texCoordSets = new TexCoord2f[1][];
      setTextureCoordinates(0, texCoords);
  } // End of setTextureCoordinates2(float[])
 


  /**
   * Sets the TextureCoordinates array by copying the data
   * into the GeometryInfo object, assuming three numbers
   * (S, T, &amp; R) per vertex.
   * This method sets the number of texture coordinate sets to 1,
   * sets the dimensionality of the texture coordinates to 3,
   * and sets the coordinates for texture coordinate set 0.
   * @deprecated As of Java 3D 1.3 replaced by 
   * <code>setTextureCoordinates(int texCoordSet, float texCoords[])</code>
   */
  public void setTextureCoordinates3(float texCoords[])
  {
      texCoordSetCount = 1;
      texCoordDim = 3;
      texCoordSets = new TexCoord3f[1][];
      setTextureCoordinates(0, texCoords);
  } // End of setTextureCoordinates3(float[])



  /**
   * Returns a reference to the indicated texture coordinate array.
   * The return type will be <code>TexCoord2f[]</code>, <code>TexCoord3f[]
   * </code>, or <code>TexCoord4f[]</code> depending on the 
   * current dimensionality of the texture coordinates in the GeometryInfo
   * object.  Use <code>getNumTexCoordComponents()</code> to find out which 
   * version is returned.
   * @param texCoordSet The index of the texture coordinate set to
   * retrieve.
   * @return An array of texture coordinates at the specified index
   * @throws IllegalArgumentException If <code> texCoordSet</code> < 0
   * or <code>texCoordSet >= texCoordSetCount</code>
   */
  public Object[] getTextureCoordinates(int texCoordSet)
  {
      if ((texCoordSet >= texCoordSetCount) || (texCoordSet < 0))
	  throw new IllegalArgumentException(
	      J3dUtilsI18N.getString("GeometryInfo18"));
      return texCoordSets[texCoordSet];
  } // End of getTextureCoordinates(int)
  
 

  /**
   * Retrieves a reference to texture coordinate set 0.
   * The return type will be <code>TexCoord2f[]</code>, <code>TexCoord3f[]
   * </code>, or <code>TexCoord4f[]</code> depending on the 
   * current dimensionality of the texture coordinates in the GeometryInfo
   * object.  Use <code>getNumTexCoordComponents()</code> to find out which 
   * version is returned.  Equivalent to <code>getTextureCoordinates(0)</code>.
   * @return An array of texture coordinates for set 0.
   * @deprecated As of Java 3D 1.3 replaced by 
   * <code>getTextureCoordinates(int texCoordSet)</code>
   */
  public Object[] getTextureCoordinates()
  {
      return texCoordSets[0];
  } // End of getTextureCoordinates()
  


  /**
   * Sets the array of indices into the Coordinate array.
   * No data copying is done - a reference to user data is used. 
   */
  public void setCoordinateIndices(int coordinateIndices[])
  {
      this.coordinateIndices = coordinateIndices;
  } // End of setCoordinateIndices



  /**
   * Retrieves a reference to the array of indices into the
   * coordinate array.</p>
   *
   * This method should be considered for advanced users only.
   * Novice users should just use getGeometryArray() to retrieve
   * their data so that the internal format of GeometryInfo is
   * of no concern.</p>
   *
   * Depending on which of the utility routines you've called
   * on your GeometryInfo object, the results may not be what you
   * expect.  If you've called the Stripifier, your GeometryInfo
   * object's Primitive has been changed to indexed TRIANGLE_STRIP_ARRAY
   * and your data will be formatted accordingly.  Similarly, if
   * you've called the Triangulator, your data is in indexed
   * TRIANGLE_ARRAY format.  Generating normals with the NormalGenerator
   * utility will convert your data to indexed TRIANGLE_ARRAY also,
   * but if you call getGeometryArray without calling the Stripifier or
   * Triangulator, your data will be converted back to the original
   * primitive type when creating the GeometryArray object to pass
   * back.  However, if your creaseAngle was not Math.PI (no creases -
   * smooth shading), then the introduction of
   * creases into your model may have split primitives, lengthening 
   * the StripCounts and index arrays from your original data.
   */
  public int[] getCoordinateIndices()
  {
      return coordinateIndices;
  } // End of getCoordinateIndices



  /**
   * Sets the array of indices into the Color array.
   * No data copying is done - a reference to user data is used. 
   */
  public void setColorIndices(int colorIndices[])
  {
      this.colorIndices = colorIndices;
  } // End of setColorIndices



  /**
   * Retrieves a reference to the array of indices into the
   * color array.</p>
   *
   * This method should be considered for advanced users only.
   * Novice users should just use getGeometryArray() to retrieve
   * their data so that the internal format of GeometryInfo is
   * of no concern.</p>
   *
   * Depending on which of the utility routines you've called
   * on your GeometryInfo object, the results may not be what you
   * expect.  If you've called the Stripifier, your GeometryInfo
   * object's Primitive has been changed to indexed TRIANGLE_STRIP_ARRAY
   * and your data will be formatted accordingly.  Similarly, if
   * you've called the Triangulator, your data is in indexed
   * TRIANGLE_ARRAY format.  Generating normals with the NormalGenerator
   * utility will convert your data to indexed TRIANGLE_ARRAY also,
   * but if you call getGeometryArray without calling the Stripifier or
   * Triangulator, your data will be converted back to the original
   * primitive type when creating the GeometryArray object to pass
   * back.  However, if your creaseAngle was not Math.PI (no creases -
   * smooth shading), then the introduction of
   * creases into your model may have split primitives, lengthening
   * the StripCounts and index arrays from your original data.
   */
  public int[] getColorIndices()
  {
      return colorIndices;
  } // End of getColorIndices



  /**
   * Sets the array of indices into the Normal array.
   * No data copying is done - a reference to user data is used. 
   */
  public void setNormalIndices(int normalIndices[])
  {
      this.normalIndices = normalIndices;
      
  } // End of setNormalIndices



  /**
   * Retrieves a reference to the array of indices into the
   * Normal array.</p>
   *
   * This method should be considered for advanced users only.
   * Novice users should just use getGeometryArray() to retrieve
   * their data so that the internal format of GeometryInfo is
   * of no concern.</p>
   *
   * Depending on which of the utility routines you've called
   * on your GeometryInfo object, the results may not be what you
   * expect.  If you've called the Stripifier, your GeometryInfo
   * object's Primitive has been changed to indexed TRIANGLE_STRIP_ARRAY
   * and your data will be formatted accordingly.  Similarly, if
   * you've called the Triangulator, your data is in indexed
   * TRIANGLE_ARRAY format.  Generating normals with the NormalGenerator
   * utility will convert your data to indexed TRIANGLE_ARRAY also,
   * but if you call getGeometryArray without calling the Stripifier or
   * Triangulator, your data will be converted back to the original
   * primitive type when creating the GeometryArray object to pass
   * back.  However, if your creaseAngle was not Math.PI (no creases -
   * smooth shading), then the introduction of
   * creases into your model may have split primitives, lengthening
   * the StripCounts and index arrays from your original data.
   */
  public int[] getNormalIndices()
  {
      return normalIndices;
  } // End of getNormalIndices
  


  /**
   * Sets one of the texture coordinate index arrays.
   * No data copying is done - a reference to user data is used. 
   * @param texCoordSet The texture coordinate set for which these coordinate 
   * indices are being specified.
   * @param texIndices The integer array of indices into the specified texture 
   * coordinate set
   * @throws IllegalArgumentException If <code> texCoordSet</code> < 0 or
   * <code>texCoordSet >= texCoordSetCount</code>.
   */
  public void setTextureCoordinateIndices(int texCoordSet, int texIndices[])
  {
      if ((texCoordSet >= texCoordSetCount) || (texCoordSet < 0))
	  throw new IllegalArgumentException(
		  J3dUtilsI18N.getString("GeometryInfo18"));
      
      // Texture coordinates are indexed 
      texCoordIndexSets[texCoordSet] = texIndices;
  } // End of setTextureCoordinateIndices(int, int[])



  /**
   * Sets the array of indices into texture coordinate set 0.  Do not
   * call this method if you are using more than one set of texture
   * coordinates.
   * No data is copied - a reference to the user data is used.
   * @deprecated As of Java 3D 1.3 replaced by 
   * <code>setTextureCoordinateIndices(int texCoordSet, int indices[])</code>
   * @throws IllegalArgumentException If <code>texCoordSetCount > 1</code>.
   */
  public void setTextureCoordinateIndices(int texIndices[])
  {
      if (texCoordSetCount > 1) 
	  throw new IllegalArgumentException(
		  J3dUtilsI18N.getString("GeometryInfo1"));
      texCoordIndexSets = new int[1][];
      texCoordIndexSets[0] = texIndices;
  } // End of setTextureCoordinateIndices(int[])



  /**
   * Retrieves a reference to the specified array of texture
   * coordinate indices.<p>
   *
   * This method should be considered for advanced users only.
   * Novice users should just use getGeometryArray() to retrieve
   * their data so that the internal format of GeometryInfo is
   * of no concern.</p>
   *
   * Depending on which of the utility routines you've called
   * on your GeometryInfo object, the results may not be what you
   * expect.  If you've called the Stripifier, your GeometryInfo
   * object's Primitive has been changed to indexed TRIANGLE_STRIP_ARRAY
   * and your data will be formatted accordingly.  Similarly, if
   * you've called the Triangulator, your data is in indexed
   * TRIANGLE_ARRAY format.  Generating normals with the NormalGenerator
   * utility will convert your data to indexed TRIANGLE_ARRAY also,
   * but if you call getGeometryArray without calling the Stripifier or
   * Triangulator, your data will be converted back to the original
   * primitive type when creating the GeometryArray object to pass
   * back.  However, if your creaseAngle was not Math.PI (no creases -
   * smooth shading), then the introduction of
   * creases into your model may have split primitives, lengthening
   * the StripCounts and index arrays from your original data.
   * @param texCoordSet The texture coordinate index set to be
   * retrieved.
   * @return Integer array of the texture coordinate indices for the specified
   * set.
   */
  public int[] getTextureCoordinateIndices(int texCoordSet) {
      return texCoordIndexSets[texCoordSet];
  }


  /**
   * Returns a reference to texture coordinate index set 0.
   * Equivalent to
   * <code>getTextureCoordinateIndices(0)</code>.
   * @deprecated As of Java 3D 1.3 replaced by 
   * <code>int[] getTextureCoordinateIndices(int texCoordSet) </code>
   * @return Integer array of the texture coordinate indices for set 0
   */
  public int[] getTextureCoordinateIndices()
  {
      if (texCoordIndexSets == null) return null;
      return texCoordIndexSets[0];
  } // End of getTextureCoordinateIndices()
 

  
  /**
   * Sets the array of strip counts.  If index lists have been set for
   * this GeomteryInfo object then the data is indexed and the stripCounts
   * are like stripIndexCounts.  If no index lists have been set then
   * the data is non-indexed and the stripCounts are like 
   * stripVertexCounts.
   * @see GeometryStripArray#GeometryStripArray(int, int,
   * int[] stripVertexCounts)
   * @see IndexedGeometryStripArray#IndexedGeometryStripArray(int, int, int,
   * int[] stripIndexCounts)
   */
  public void setStripCounts(int stripCounts[])
  {
      this.stripCounts = stripCounts;
  } // End of setStripCounts
 


  /**
   * Retrieves a reference to the array of stripCounts.</p>
   *
   * This method should be considered for advanced users only.
   * Novice users should just use getGeometryArray() to retrieve
   * their data so that the internal format of GeometryInfo is
   * of no concern.</p>
   *
   * Depending on which of the utility routines you've called
   * on your GeometryInfo object, the results may not be what you
   * expect.  If you've called the Stripifier, your GeometryInfo
   * object's Primitive has been changed to indexed TRIANGLE_STRIP_ARRAY
   * and your data will be formatted accordingly.  Similarly, if
   * you've called the Triangulator, your data is in indexed
   * TRIANGLE_ARRAY format.  Generating normals with the NormalGenerator
   * utility will convert your data to indexed TRIANGLE_ARRAY also,
   * but if you call getGeometryArray without calling the Stripifier or
   * Triangulator, your data will be converted back to the original
   * primitive type when creating the GeometryArray object to pass
   * back.  However, if your creaseAngle was not Math.PI (no creases -
   * smooth shading), then the introduction of
   * creases into your model may have split primitives, lengthening
   * the StripCounts and index arrays from your original data.
   */
  public int[] getStripCounts()
  {
      return stripCounts;
  } // End of getStripCounts
 


  /**
   * Sets the list of contour counts.  Only used with the POLYGON_ARRAY
   * primitive.  Polygons can be made of several vertex lists 
   * called contours.  The first list is the polygon, and 
   * subsequent lists are "holes" that are removed from the
   * polygon.  All of the holes must be contained entirely
   * within the polygon.
   */
  public void setContourCounts(int contourCounts[])
  {
      this.contourCounts = contourCounts;
  } // End of setContourCounts



  /**
   * Retrieves a reference to the array of contourCounts.
   */
  public int[] getContourCounts()
  {
      return contourCounts;
  } // End of getContourCounts



  /*
   * This routine will return an index list for any array of objects.
   */
  int[] getListIndices(Object list[])
  {
      // Create list of indices to return
      int indices[] = new int[list.length];
      
      // Create hash table with initial capacity equal to the number
      // of components (assuming about half will be duplicates)
      HashMap table = new HashMap(list.length);
      
      Integer idx;
      for (int i = 0 ; i < list.length ; i++) {
	  
	  // Find index associated with this object
	  idx = (Integer)table.get(list[i]);
	  
	  if (idx == null) {
	      // We haven't seen this object before
	      indices[i] = i;
	      
	      // Put into hash table and remember the index
	      table.put(list[i], new Integer(i));
	      
	  } else {
	      // We've seen this object
	      indices[i] = idx.intValue();
	  }
      }
      
      return indices;
  } // End of getListIndices



  // Class to hash 'size' integers
  private class IndexRow {
    int[] val;
    int size;
    private static final int HASHCONST = 0xBABEFACE;

    public int hashCode()
    {
      int bits = 0;
      for (int i = 0 ; i < size ; i++) {
	bits ^= (bits * HASHCONST) << 2;
      }
      return bits;
    } // End of IndexRow.hashCode

    public boolean equals(Object obj)
    {
      for (int i = 0 ; i < size ; i++) {
	if (((IndexRow)obj).get(i) != val[i]) return false;
      }
      return true;
    } // End of IndexRow.equals()

    public int get(int index)
    {
      return val[index];
    } // End of IndexRow.get

    public void set(int index, int value)
    {
      val[index] = value;
    } // End of IndexRow.set

    IndexRow(int numColumns)
    {
      size = numColumns;
      val = new int[size];
    } // End of IndexRow constructor
  } // End of class IndexRow
 


  /**
   * Create index lists for all data lists.
   * Identical data entries are guaranteed to
   * use the same index value.  Does not remove unused data values
   * from the object - call compact() to do this.
   * @param useCoordIndexOnly Reformat the data into the
   * GeometryArray.USE_COORD_INDEX_ONLY format where there is only
   * one index list.  If the data is already in the USE_COORD_INDEX_ONLY
   * format, sending false (or calling indexify()) will change
   * it to the normal indexed format.
   * @throws IllegalArgumentException if coordinate data is missing,
   * if the index lists aren't all the
   * same length, if an index list is set and the corresponding data
   * list isn't set, if a data list is set and the corresponding
   * index list is unset (unless all index lists are unset or in
   * USE_COORD_INDEX_ONLY format),
   * if StripCounts or ContourCounts is inconsistent with the current
   * primitive, if the sum of the contourCounts array doesn't equal
   * the length of the StripCounts array, or if the number of vertices
   * isn't a multiple of three (for triangles) or four (for quads).
   */
  public void indexify(boolean useCoordIndexOnly)
  {
      checkForBadData();

      if (useCoordIndexOnly) {
	// Return if already in this format
	if (coordOnly) return;

	// Start from normal indexed format
	indexify(false);

	// Reformat data to USE_COORD_INDEX_ONLY format
	// Need to make an index into the index lists using each
	// row of indexes as one value

	// First, find out how many index lists there are;
	int numLists = 1;		// Always have coordinates
	if (colorIndices != null) numLists++;
        if (normalIndices != null) numLists++;
	numLists += texCoordSetCount;

	// Make single array containing all indices
	int n = coordinateIndices.length;
	IndexRow[] ir = new IndexRow[n];
	int j;
	for (int i = 0 ; i < n ; i++) {
	  ir[i] = new IndexRow(numLists);
	  j = 0;
	  ir[i].set(j++, coordinateIndices[i]);
	  if (colorIndices != null) ir[i].set(j++, colorIndices[i]);
	  if (normalIndices != null) ir[i].set(j++, normalIndices[i]);
	  for (int k = 0 ; k < texCoordSetCount ; k++) {
	    ir[i].set(j++, texCoordIndexSets[k][i]);
	  }
	}

	// Get index into that array
	int[] coordOnlyIndices = getListIndices(ir);

	// Get rid of duplicate rows
	int newInd[] = new int[coordOnlyIndices.length];
	ir = (IndexRow[])compactData(coordOnlyIndices, ir, newInd);
	coordOnlyIndices = newInd;

	// Reformat data lists to correspond to new index

	// Allocate arrays to hold reformatted data
	Point3f[] newCoords = new Point3f[ir.length];
	Color3f[] newColors3 = null;
	Color4f[] newColors4 = null;
	Vector3f[] newNormals = null;
	Object newTexCoordSets[][] = null;
	if (colors3 != null) newColors3 = new Color3f[ir.length];
	else if (colors4 != null) newColors4 = new Color4f[ir.length];
	if (normals != null) newNormals = new Vector3f[ir.length];
	for (int i = 0 ; i < texCoordSetCount ; i++) {
	  if (texCoordDim == 2) {
	    if (i == 0) newTexCoordSets = new TexCoord2f[texCoordSetCount][];
	    newTexCoordSets[i] = new TexCoord2f[ir.length];
	  } else if (texCoordDim == 3) {
	    if (i == 0) newTexCoordSets = new TexCoord3f[texCoordSetCount][];
	    newTexCoordSets[i] = new TexCoord3f[ir.length];
	  } else if (texCoordDim == 4) {
	    if (i == 0) newTexCoordSets = new TexCoord4f[texCoordSetCount][];
	    newTexCoordSets[i] = new TexCoord4f[ir.length];
	  }
	}
	
	// Copy data into new arrays
	n = ir.length;
	for (int i = 0 ; i < n ; i++) {
	  j = 0;
	  newCoords[i] = coordinates[(ir[i]).get(j++)];
	  if (colors3 != null) {
	    newColors3[i] = colors3[(ir[i]).get(j++)];
	  } else if (colors4 != null) {
	    newColors4[i] = colors4[(ir[i]).get(j++)];
	  }
	  if (normals != null) newNormals[i] = normals[(ir[i]).get(j++)];
	  for (int k = 0 ; k < texCoordSetCount ; k++) {
	    newTexCoordSets[k][i] = texCoordSets[k][(ir[i]).get(j++)];
	  }
	}

	// Replace old arrays with new arrays
        coordinates = newCoords;
	colors3 = newColors3;
	colors4 = newColors4;
	normals = newNormals;
	texCoordSets = newTexCoordSets;
	coordinateIndices = coordOnlyIndices;
	colorIndices = null;
	normalIndices = null;
	texCoordIndexSets = new int[texCoordSetCount][];

	coordOnly = true;
      } else if (coordOnly) {
	// Need to change from useCoordIndexOnly format to normal
	// indexed format.  Should make a more efficient implementation
	// later.

	int n = coordinateIndices.length;
	if ((colors3 != null) || (colors4 != null)) {
	  colorIndices = new int[n];
	  for (int i = 0 ; i < n ; i++) colorIndices[i] = coordinateIndices[i];
	}
	if (normals != null) {
	  normalIndices = new int[n];
	  for (int i = 0 ; i < n ; i++) normalIndices[i] = coordinateIndices[i];
	}
	texCoordIndexSets = new int[texCoordSetCount][];
	for (int i = 0 ; i < texCoordSetCount ; i++) {
	  texCoordIndexSets[i] = new int[n];
	  for (int j = 0 ; j < n ; j++) {
	    texCoordIndexSets[i][j] = coordinateIndices[j];
	  }
	}
	coordOnly = false;
      } else {

	// No need to indexify if already indexed
	if (coordinateIndices != null) return;

	coordinateIndices = getListIndices(coordinates);
	
	if (colors3 != null) colorIndices = getListIndices(colors3);
	else if (colors4 != null) colorIndices = getListIndices(colors4);
	
	if (normals != null) normalIndices = getListIndices(normals);
	
	texCoordIndexSets = new int[texCoordSetCount][];
	for(int i = 0 ; i < texCoordSetCount ; i++) {
	    texCoordIndexSets[i] = getListIndices(texCoordSets[i]);
	}

	coordOnly = false;
      }
      
      if ((DEBUG & 1) == 1) {
	  System.out.println("Coordinate Array:");
	  for (int i = 0 ; i < coordinates.length ; i++) {
	      System.out.println("  " + i + " " + coordinates[i] +
		      " " + coordinates[i].hashCode());
	  }
	  System.out.println("Index array:");
	  for (int i = 0 ; i < coordinateIndices.length ; i++) {
	      System.out.println("  " + i + " " + coordinateIndices[i]);
	  }
      }
      
  } // End of indexify



  public void indexify()
  {
    indexify(false);
  } // End of indexify()
 


  /**
   * Allocates an array of the same type as the input type. This allows us to 
   * use a generic compactData method.
   *
   * @param data Array of coordinate, color, normal or texture coordinate data
   * The data can be in one of the following formats - Point3f, Color3f, 
   * Color4f, TexCoord2f, TexCoord3f, TexCoord4f.
   *
   * @param num The size of the array to be allocated
   *
   * @return An array of size num of the same type as the input type 
   *
   * @exception IllegalArgumentException if the input array is not one of the 
   * types listed above.
   */
  Object[] allocateArray(Object data[], int num) {
      Object newData[] = null;
      if (data instanceof javax.vecmath.Point3f[]) {
	  newData = new Point3f[num];
      } else if (data instanceof javax.vecmath.Vector3f[]) {
	  newData = new Vector3f[num];
      } else if (data instanceof javax.vecmath.Color3f[]) {
	  newData = new Color3f[num];
      } else if (data instanceof javax.vecmath.Color4f[]) {
	  newData = new Color4f[num];
      } else if (data instanceof javax.vecmath.TexCoord2f[]) {
	  newData = new TexCoord2f[num];
      } else if (data instanceof javax.vecmath.TexCoord3f[]) {
	  newData = new TexCoord3f[num];
      } else if (data instanceof javax.vecmath.TexCoord4f[]) {
	  newData = new TexCoord4f[num];
      } else if (data instanceof IndexRow[]) {
	  // Hack so we can use compactData for coordIndexOnly
	  newData = new IndexRow[num];
      } else throw new IllegalArgumentException(
	  J3dUtilsI18N.getString("GeometryInfo9"));
      return newData;
  } // End of allocateArray
 


  /**
   * Generic method that compacts (ie removes unreferenced/duplicate data)
   * any type of indexed data. 
   * Used to compact coordinate, color, normal and texture coordinate data.
   * @param indices Array of indices
   * @param data Array of coordinate, color, normal or texture coordinate data
   * The data can be in one of the following formats - Point3f, Color3f, 
   * Color4f, TexCoord2f, TexCoord3f, TexCoord4f. 
   * @param newInd The new array of indexes after the data has been compacted.
   * This must be allocated by the calling method. On return, this array will 
   * contain the new index data. The size of this array must be equal to 
   * indices.length
   * @return Array of the data with unreferenced and duplicate entries removed. 
   * The return type will be the same as the type that was passed in data. 
   */
   // TODO:  Remove duplicate entries in data lists.
  private Object[] compactData(int indices[], Object data[], int newInd[]) {
      Object newData[] = null;
      /*
       * This is a three step process.
       * First, find out how many unique indexes are used.  This
       * will be the size of the new data array.
       */
      int numUnique = 0;
      int translationTable[] = new int[data.length];
      for (int i = 0 ; i < indices.length ; i++) {
	  if (translationTable[indices[i]] == 0) {
	      
	      numUnique++;
	      translationTable[indices[i]] = 1;
	  }
      }
      /*
       * Second, build the new data list.  Remember the new indexes so
       * we can use the table to translate the old indexes to the new
       */
      newData = allocateArray(data, numUnique);
      int newIdx = 0;
      for (int i = 0 ; i < translationTable.length ; i++) {
	  if (translationTable[i] != 0) {
	      newData[newIdx] = data[i];
	      translationTable[i] = newIdx++;
	  }
      }
      /*
       * Third, make the new index list
       */
      for (int i = 0 ; i < indices.length ; i++) {
	  newInd[i] = translationTable[indices[i]];
      }
      return newData;
  } // End of compactData


  
  /**
   * Remove unused data from an indexed dataset.
   * Indexed data may contain data entries that are never referenced by
   * the dataset.  This routine will remove those entries where 
   * appropriate and renumber the indices to match the new values.
   * @throws IllegalArgumentException if coordinate data is missing,
   * if the index lists aren't all the
   * same length, if an index list is set and the corresponding data
   * list isn't set, if a data list is set and the corresponding
   * index list is unset (unless all index lists are unset or in
   * USE_COORD_INDEX_ONLY format),
   * if StripCounts or ContourCounts is inconsistent with the current
   * primitive, if the sum of the contourCounts array doesn't equal
   * the length of the StripCounts array, or if the number of vertices
   * isn't a multiple of three (for triangles) or four (for quads).
   */
  public void compact()
  {
      checkForBadData();

      // Only usable on indexed data
      if (coordinateIndices == null) return;

      // USE_COORD_INDEX_ONLY never has unused data
      if (coordOnly) return;

      int newInd[] = new int[coordinateIndices.length];
      coordinates =
	  (Point3f[])compactData(coordinateIndices, coordinates, newInd);
      coordinateIndices = newInd;

      if (colorIndices != null) {
	  newInd = new int[colorIndices.length];
	  if (colors3 != null) 
	      colors3 = (Color3f[])compactData(colorIndices, colors3, newInd);
	  else if (colors4 != null) 
	      colors4 = (Color4f[])compactData(colorIndices, colors4, newInd);
	  colorIndices = newInd;
      }

      if (normalIndices != null) {
	  newInd = new int[normalIndices.length];
	  normals = (Vector3f[])compactData(normalIndices, normals, newInd);
	  normalIndices = newInd;
      }

      for (int i = 0 ; i < texCoordSetCount ; i++) {
	  newInd = new int[texCoordIndexSets[i].length];
	  texCoordSets[i] = compactData(texCoordIndexSets[i],
					  texCoordSets[i], newInd);
	  texCoordIndexSets[i] = newInd;
      }
  } // End of compact



  /**
   * Check the data to make sure everything's consistent.
   */
  private void checkForBadData() {
      boolean badData = false;

      //
      // Coordinates are required
      //
      if (coordinates == null) {
	  throw new IllegalArgumentException(
		  J3dUtilsI18N.getString("GeometryInfo3"));
      }
      
      //
      // Check for indices with no data
      //
      if ((colors3 == null) && (colors4 == null) && (colorIndices != null))
	  throw new IllegalArgumentException(
		  J3dUtilsI18N.getString("GeometryInfo4"));
      if ((normals == null) && (normalIndices != null))
	  throw new IllegalArgumentException(
		  J3dUtilsI18N.getString("GeometryInfo11"));
      
      //
      // Make sure all TextureCoordinate data is set (indices or not)
      //
      for (int i = 0 ; i < texCoordSetCount ; i++) {
	if (texCoordSets[i] == null)
	  throw new IllegalArgumentException(
		  J3dUtilsI18N.getString("GeometryInfo10"));
      }
      
      //
      // Check for Missing Index lists
      //
      boolean texInds = false;	// Indicates whether we have texcoord indices
      if (texCoordIndexSets != null) {
	for (int i = 0 ; i < texCoordSetCount ; i++) {
	  if (texCoordIndexSets[i] != null) texInds = true;
	}
      }
      if ((coordinateIndices != null) ||
	  (colorIndices != null) ||
	  (normalIndices != null) ||
	  texInds) {
	// At least one index list is present, so they all must be
	// present (unless coordOnly)
	if (coordinateIndices == null) badData = true;
	else if (coordOnly) {
	  if ((colorIndices != null) ||
	      (normalIndices != null) ||
	      (texInds == true)) {
	    throw new IllegalArgumentException(
	      J3dUtilsI18N.getString("GeometryInfo20"));
	  }
	} else if (((colors3 != null) || (colors4 != null)) &&
	         (colorIndices == null)) badData = true;
	else if ((normals != null) && (normalIndices == null)) badData = true;
	else if ((texCoordSetCount > 0) && !texInds) badData = true;
	if (badData) throw new
	  IllegalArgumentException(J3dUtilsI18N.getString("GeometryInfo19"));
      }

      //
      // Make sure index lists are all the same length
      //
      if ((coordinateIndices != null) && (!coordOnly)) {
	  if (((colors3 != null) || (colors4 != null)) &&
	      (colorIndices.length != coordinateIndices.length)) 
	    badData = true;
	  else if ((normals != null) &&
	           (normalIndices.length != coordinateIndices.length)) 
	    badData = true;
	  else {
	    //Check all texCoord indices have the same length
	    for (int i = 0 ; i < texCoordSetCount ; i++) {
	      if (texCoordIndexSets[i].length != coordinateIndices.length) {
		badData = true;
		break;
	      }
	    }
	  }
	  if (badData) {
	      throw new IllegalArgumentException(
		      J3dUtilsI18N.getString("GeometryInfo5"));
	  }
      }
      
      //
      // For stripped primitives, make sure we have strip counts
      //
      if ((prim == TRIANGLE_STRIP_ARRAY) ||
	  (prim == TRIANGLE_FAN_ARRAY) ||
	  (prim == POLYGON_ARRAY)) {
	  if (stripCounts == null) badData = true;
      } else if (stripCounts != null) badData = true;
      if (badData) {
	  throw new IllegalArgumentException(
		  J3dUtilsI18N.getString("GeometryInfo6"));
      }
      
      // Find out how much data we have
      int count;
      if (coordinateIndices == null) count = coordinates.length;
      else count = coordinateIndices.length;
      
      //
      // Make sure sum of strip counts equals indexCount (or vertexCount)
      // and check to make sure triangles and quads have the right number
      // of vertices
      //
      if ((prim == TRIANGLE_STRIP_ARRAY) ||
	  (prim == TRIANGLE_FAN_ARRAY) ||
	  (prim == POLYGON_ARRAY)) {
	  int sum = 0;
	  for (int i = 0 ; i < stripCounts.length ; i++) {
	      sum += stripCounts[i];
	  }
	  if (sum != count) {
	      throw new IllegalArgumentException(
		      J3dUtilsI18N.getString("GeometryInfo7"));
	  }
      } else if (prim == TRIANGLE_ARRAY) {
	  if (count % 3 != 0) {
	      throw new IllegalArgumentException(
		      J3dUtilsI18N.getString("GeometryInfo12")); 
	  }
      } else if (prim == QUAD_ARRAY) {
	  if (count % 4 != 0) {
	      throw new IllegalArgumentException(
		      J3dUtilsI18N.getString("GeometryInfo13")); 
	  }
      }
      
      //
      // For polygons, make sure the contours add up.
      //
      if (prim == POLYGON_ARRAY) {
	  if (contourCounts != null) {
	      int c = 0;
	      for (int i = 0 ; i < contourCounts.length ; i++) 
		  c += contourCounts[i];
	      if (c != stripCounts.length) {
		  throw new IllegalArgumentException(
			  J3dUtilsI18N.getString("GeometryInfo8"));
	      }
	  }
      } else {
	  if (contourCounts != null) {
	      throw new IllegalArgumentException(
		      J3dUtilsI18N.getString("GeometryInfo14"));
	  }
      }
  } // End of checkForBadData
 


  /**
   * Get rid of index lists by reorganizing data into an un-indexed
   * format.  Does nothing if no index lists are set.
   * @throws IllegalArgumentException if coordinate data is missing,
   * if the index lists aren't all the
   * same length, if an index list is set and the corresponding data
   * list isn't set, if a data list is set and the corresponding
   * index list is unset (unless all index lists are unset or in
   * USE_COORD_INDEX_ONLY format),
   * if StripCounts or ContourCounts is inconsistent with the current
   * primitive, if the sum of the contourCounts array doesn't equal
   * the length of the StripCounts array, or if the number of vertices
   * isn't a multiple of three (for triangles) or four (for quads).
   */
  public void unindexify() {
      checkForBadData();
      if (coordinateIndices != null) {
	  // Switch from USE_COORD_INDEX_ONLY format
	  if (coordOnly) indexify(false);

	  coordinates =
	    (Point3f[])unindexifyData(coordinates, coordinateIndices);
	  coordinateIndices = null;

	  if (colors3 != null) {
	      colors3 = (Color3f[])unindexifyData(colors3, colorIndices);
	  } else if (colors4 != null) {
	      colors4 = (Color4f[])unindexifyData(colors4, colorIndices);
	  }
	  colorIndices = null;

          if (normals != null) {
	    normals = (Vector3f[])unindexifyData(normals, normalIndices);
	    normalIndices = null;
	  }

	  for (int i = 0 ; i < texCoordSetCount ; i++) 
	    texCoordSets[i] = unindexifyData(texCoordSets[i], 
					     texCoordIndexSets[i]);
	  texCoordIndexSets = new int[texCoordSetCount][];
      }
  } // End of unindexify
 


  /**
   * Generic unindexify method. Can unindex data in any of the following 
   * formats Point3f, Color3f, Color4f, Vector3f, TexCoord2f, TexCoord3f, 
   * TexCoord4f.
   */
  private Object[] unindexifyData(Object data[], int index[])
  {
      Object newData[] = allocateArray(data, index.length);
      for (int i = 0 ; i < index.length ; i++) {
	  newData[i] = data[index[i]];
      }
      return newData;
  } // End of unindexifyData
 


  /**
   * Calculate vertexFormat based on data.
   */
  private int getVertexFormat()
  {
      int vertexFormat = GeometryArray.COORDINATES;
      
      if (colors3 != null) vertexFormat |= GeometryArray.COLOR_3;
      else if (colors4 != null) vertexFormat |= GeometryArray.COLOR_4;
      
      if (normals != null) vertexFormat |= GeometryArray.NORMALS;
      
      if (texCoordDim == 2)
	  vertexFormat |= GeometryArray.TEXTURE_COORDINATE_2;
      else if (texCoordDim == 3)
	  vertexFormat |= GeometryArray.TEXTURE_COORDINATE_3;
      else if (texCoordDim == 4)
	  vertexFormat |= GeometryArray.TEXTURE_COORDINATE_4;
      
      return vertexFormat;
  } // End of getVertexFormat
 


  /**
   * Calculate vertexCount based on data
   */
  private int getVertexCount()
  {
      int vertexCount = coordinates.length;
      
      if (colors3 != null) {
	  if (colors3.length > vertexCount) vertexCount = colors3.length;
      } else if (colors4 != null) {
	  if (colors4.length > vertexCount) vertexCount = colors4.length;
      }
      
      if (normals != null) {
	  if (normals.length > vertexCount) vertexCount = normals.length;
      }
      
      // Find max length tex coord set
      for (int i = 0 ; i < texCoordSetCount ; i++) {
	  if (texCoordSets[i].length > vertexCount)
	      vertexCount = texCoordSets[i].length; 
      }
	
      return vertexCount;
  } // End of getVertexCount



  /**
   * Converts an array of Tuple2f, Tuple3f, or Tuple4f values into
   * an array of floats.  Assumes array is not null.  Returns null
   * if array is not Tuple2f, Tuple3f, or Tuple4f.  Used by fillIn()
   * for BY_REFERENCE not INTERLEAVED geometry.
   */
  private float[] vecmathToFloat(Object[] ar)
  {
    if (ar[0] instanceof Tuple2f) {
      float[] p = new float[ar.length * 2];
      Tuple2f[] a = (Tuple2f[])ar;
      for (int i = 0 ; i < ar.length ; i++) {
	p[i * 2]     = a[i].x;
	p[i * 2 + 1] = a[i].y;
      }
      return p;
    } else if (ar[0] instanceof Tuple3f) {
      float[] p = new float[ar.length * 3];
      Tuple3f[] a = (Tuple3f[])ar;
      for (int i = 0 ; i < ar.length ; i++) {
	p[i * 3]     = a[i].x;
	p[i * 3 + 1] = a[i].y;
	p[i * 3 + 2] = a[i].z;
      }
      return p;
    } else if (ar[0] instanceof Tuple4f) {
      float[] p = new float[ar.length * 4];
      Tuple4f[] a = (Tuple4f[])ar;
      for (int i = 0 ; i < ar.length ; i++) {
	p[i * 4]     = a[i].x;
	p[i * 4 + 1] = a[i].y;
	p[i * 4 + 2] = a[i].z;
	p[i * 4 + 3] = a[i].w;
      }
      return p;
    }
    return null;
  } // End of vecmathToFloat



  /**
   * Fill in the GeometryArray object.  Used by getGeometryArray and
   * getIndexedGeometryArray.  checkForBadData has already been called.
   */
  private void fillIn(GeometryArray ga, boolean byRef, boolean interleaved,
		      boolean nio)
  {
      if (interleaved) {
	// Calculate number of words per vertex
	int wpv = 3;                      // Always have coordinate data
	if (normals != null) wpv += 3;
	if (colors3 != null) wpv += 3;
	else if (colors4 != null) wpv += 4;
	wpv += (texCoordSetCount * texCoordDim);

        // Build array of interleaved data
	float[] d = new float[wpv * coordinates.length];

	// Fill in the array
	int offset = 0;
	for (int i = 0 ; i < coordinates.length ; i++) {
	  if (texCoordDim == 2) {
	    for (int j = 0 ; j < texCoordSetCount ; j++) {
	      d[offset++] = ((TexCoord2f)texCoordSets[j][i]).x;
	      d[offset++] = ((TexCoord2f)texCoordSets[j][i]).y;
	    }
	  } else if (texCoordDim == 3) {
	    for (int j = 0 ; j < texCoordSetCount ; j++) {
	      d[offset++] = ((TexCoord3f)texCoordSets[j][i]).x;
	      d[offset++] = ((TexCoord3f)texCoordSets[j][i]).y;
	      d[offset++] = ((TexCoord3f)texCoordSets[j][i]).z;
	    }
	  } else if (texCoordDim == 4) {
	    for (int j = 0 ; j < texCoordSetCount ; j++) {
	      d[offset++] = ((TexCoord4f)texCoordSets[j][i]).x;
	      d[offset++] = ((TexCoord4f)texCoordSets[j][i]).y;
	      d[offset++] = ((TexCoord4f)texCoordSets[j][i]).z;
	      d[offset++] = ((TexCoord4f)texCoordSets[j][i]).w;
	    }
	  }

	  if (colors3 != null) {
	    d[offset++] = colors3[i].x;
	    d[offset++] = colors3[i].y;
	    d[offset++] = colors3[i].z;
	  } else if (colors4 != null) {
	    d[offset++] = colors4[i].x;
	    d[offset++] = colors4[i].y;
	    d[offset++] = colors4[i].z;
	    d[offset++] = colors4[i].w;
	  }

	  if (normals != null) {
	    d[offset++] = normals[i].x;
	    d[offset++] = normals[i].y;
	    d[offset++] = normals[i].z;
	  }

	  d[offset++] = coordinates[i].x;
	  d[offset++] = coordinates[i].y;
	  d[offset++] = coordinates[i].z;
	}
	// Register reference to array of interleaved data
	if (nio) {
	  ByteBufferWrapper b = ByteBufferWrapper.allocateDirect(d.length * 4);
	  FloatBufferWrapper f = 
	    b.order( ByteOrderWrapper.nativeOrder() ).asFloatBuffer();
	  f.put(d);
	  ga.setInterleavedVertexBuffer(f.getJ3DBuffer());
	} else ga.setInterleavedVertices(d);
      } else if (nio) {

	ByteBufferWrapper b =
	  ByteBufferWrapper.allocateDirect(coordinates.length * 4 * 3);
	FloatBufferWrapper f = 
	  b.order( ByteOrderWrapper.nativeOrder() ).asFloatBuffer();
	f.put(vecmathToFloat(coordinates));
	ga.setCoordRefBuffer(f.getJ3DBuffer());

	if (colors3 != null) {
	  b = ByteBufferWrapper.allocateDirect(colors3.length * 4 * 3);
	  f = b.order( ByteOrderWrapper.nativeOrder() ).asFloatBuffer();
	  f.put(vecmathToFloat(colors3));
	  ga.setColorRefBuffer(f.getJ3DBuffer());
	} else if (colors4 != null) {
	  b = ByteBufferWrapper.allocateDirect(colors4.length * 4 * 4);
	  f = b.order( ByteOrderWrapper.nativeOrder() ).asFloatBuffer();
	  f.put(vecmathToFloat(colors4));
	  ga.setColorRefBuffer(f.getJ3DBuffer());
	}

	if (normals != null) {
	  b = ByteBufferWrapper.allocateDirect(normals.length * 4 * 3);
	  f = b.order( ByteOrderWrapper.nativeOrder() ).asFloatBuffer();
	  f.put(vecmathToFloat(normals));
	  ga.setNormalRefBuffer(f.getJ3DBuffer());
	}

	for (int i = 0 ; i < texCoordSetCount ; i++) {
	  b = ByteBufferWrapper.allocateDirect(
	    texCoordSets[i].length * 4 * texCoordDim);
	  f = b.order( ByteOrderWrapper.nativeOrder() ).asFloatBuffer();
	  f.put(vecmathToFloat(texCoordSets[i]));
	  ga.setTexCoordRefBuffer(i, f.getJ3DBuffer());
	}
      } else if (byRef) {
	// Need to copy the data into float arrays - GeometryArray
	// prefers them over the vecmath types
	ga.setCoordRefFloat(vecmathToFloat(coordinates));
	if (colors3 != null) ga.setColorRefFloat(vecmathToFloat(colors3));
	else if (colors4 != null) ga.setColorRefFloat(vecmathToFloat(colors4));
	if (normals != null) ga.setNormalRefFloat(vecmathToFloat(normals));
	for (int i = 0 ; i < texCoordSetCount ; i++) {
	    ga.setTexCoordRefFloat(i, vecmathToFloat(texCoordSets[i]));
	}
      } else {
	ga.setCoordinates(0, coordinates);
	if (colors3 != null) ga.setColors(0, colors3);
	else if (colors4 != null) ga.setColors(0, colors4);
	if (normals != null) ga.setNormals(0, normals);
	for (int i = 0 ; i < texCoordSetCount ; i++) {
	  if (texCoordDim == 2) {
	    ga.setTextureCoordinates(i, 0, (TexCoord2f[])texCoordSets[i]);
	  } else if (texCoordDim == 3) {
	    ga.setTextureCoordinates(i, 0, (TexCoord3f[])texCoordSets[i]);
	  } else if (texCoordDim == 4) {
	    ga.setTextureCoordinates(i, 0, (TexCoord4f[])texCoordSets[i]);
	  }
	}
      }

      if (coordinateIndices != null) {
	IndexedGeometryArray iga = null;
	iga = (IndexedGeometryArray)ga;
	iga.setCoordinateIndices(0, coordinateIndices);
	if (!coordOnly) {
	  if (colorIndices != null) iga.setColorIndices(0, colorIndices);
	  if (normalIndices != null) iga.setNormalIndices(0, normalIndices);
	  for (int i = 0 ; i < texCoordSetCount ; i++)
	    iga.setTextureCoordinateIndices(i, 0, texCoordIndexSets[i]);
	}
      }
  } // End of fillIn



  /**
   * Redo indexes to guarantee connection information.
   * Use this routine if your original data is in indexed format, but
   * you don't trust that the indexing is correct.  After this 
   * routine it is guaranteed that two points with the same
   * position will have the same coordinate index (for example).  
   * Try this if you see
   * glitches in your normals or stripification, to rule out
   * bad indexing as the source of the problem.  Works with normal
   * indexed format or USE_COORD_INDEX_ONLY format.
   * @throws IllegalArgumentException if coordinate data is missing,
   * if the index lists aren't all the
   * same length, if an index list is set and the corresponding data
   * list isn't set, if a data list is set and the corresponding
   * index list is unset (unless all index lists are unset or in
   * USE_COORD_INDEX_ONLY format),
   * if StripCounts or ContourCounts is inconsistent with the current
   * primitive, if the sum of the contourCounts array doesn't equal
   * the length of the StripCounts array, or if the number of vertices
   * isn't a multiple of three (for triangles) or four (for quads).
   */
  public void recomputeIndices()
  {
      boolean remember = coordOnly;

      // Can make more efficient implementation later
      unindexify();
      indexify(remember);
  } // End of recomputeIndices



  /**
   * Reverse the order of an array of ints (computer class homework
   * problem).
   */
  private void reverseList(int list[])
  {
      int t;
      
      if (list == null) return;
      
      for (int i = 0 ; i < list.length / 2 ; i++) {
	  t = list[i];
	  list[i] = list[list.length - i - 1];
	  list[list.length - i - 1] = t;
      }
  } // End of reverseList



  /**
   * Reverse the order of all lists.  If your polygons are formatted with 
   * clockwise winding, you will always see the back and never the front.
   * (Java 3D always wants vertices specified with a counter-clockwise
   * winding.)
   * This method will (in effect) reverse the winding of your data by
   * inverting all of the index lists and the stripCounts
   * and contourCounts lists.
   * @throws IllegalArgumentException if coordinate data is missing,
   * if the index lists aren't all the
   * same length, if an index list is set and the corresponding data
   * list isn't set, if a data list is set and the corresponding
   * index list is unset (unless all index lists are unset or in
   * USE_COORD_INDEX_ONLY format),
   * if StripCounts or ContourCounts is inconsistent with the current
   * primitive, if the sum of the contourCounts array doesn't equal
   * the length of the StripCounts array, or if the number of vertices
   * isn't a multiple of three (for triangles) or four (for quads).
   */
  public void reverse()
  {
      indexify();
      reverseList(stripCounts);
      reverseList(oldStripCounts);
      reverseList(contourCounts);
      reverseList(coordinateIndices);
      reverseList(colorIndices);
      reverseList(normalIndices);
      for (int i = 0 ; i < texCoordSetCount ; i++) 
	  reverseList(texCoordIndexSets[i]);
  } // End of reverse



  /**
   * Returns true if the data in this GeometryInfo is currently 
   * formatted in the USE_COORD_INDEX_ONLY format where a single
   * index list is used to index into all data lists.
   * @see GeometryInfo#indexify(boolean)
   * @see GeometryInfo#getIndexedGeometryArray(boolean, boolean, boolean,
   * boolean, boolean)
   */
  public boolean getUseCoordIndexOnly()
  {
    return coordOnly;
  } // End of getUseCoordIndexOnly



  /**
   * Tells the GeometryInfo that its data is formatted in the
   * USE_COORD_INDEX_ONLY format with a single index list
   * (the coordinate index list) that indexes into all data
   * lists (coordinates, normals, colors, and texture 
   * coordinates).  NOTE: this will not convert the data
   * for you.  This method is for when you are sending in
   * data useng the setCoordinates, setNormals, setColors,
   * and/or setTextureCoordinates methods, and you are only
   * setting one index using setCoordinateIndices().  If
   * you want GeometryInfo to convert your data to the
   * USE_COORD_INDEX_ONLY format, use indexify(true) or
   * getIndexedGeometryArray with the useCoordIndexOnly
   * parameter set to true.
   * @see GeometryInfo#indexify(boolean)
   * @see GeometryInfo#getIndexedGeometryArray(boolean, boolean, boolean,
   * boolean, boolean)
   */
  public void setUseCoordIndexOnly(boolean useCoordIndexOnly)
  {
    coordOnly = useCoordIndexOnly;
  } // End of setUseCoordIndexOnly



  /**
   * Creates and returns a non-indexed Java 3D GeometryArray object
   * based on the data in the GeometryInfo object.  This object is
   * suitable to be attached to a Shape3D node for rendering.
   * @param byRef Use geometry BY_REFERENCE
   * @param interleaved Use INTERLEAVED geometry.  Implies byRef is
   * true as well.
   * @param nio Create GeometryArray using java.nio.Buffer for
   * geometry arrays.  Only usable on JDK 1.4 or higher.  Implies
   * byRef is true as well.
   * @throws IllegalArgumentException if coordinate data is missing,
   * if the index lists aren't all the
   * same length, if an index list is set and the corresponding data
   * list isn't set, if a data list is set and the corresponding
   * index list is unset (unless all index lists are unset or in
   * USE_COORD_INDEX_ONLY format),
   * if StripCounts or ContourCounts is inconsistent with the current
   * primitive, if the sum of the contourCounts array doesn't equal
   * the length of the StripCounts array, or if the number of vertices
   * isn't a multiple of three (for triangles) or four (for quads).
   */
  public GeometryArray getGeometryArray(boolean byRef, boolean interleaved,
					boolean nio)
  {
      checkForBadData();
      
      if (prim == POLYGON_ARRAY) {
	  if (tr == null) tr = new Triangulator();
	  tr.triangulate(this);
      } else changeBackToOldPrim();
      
      unindexify();
      
      int vertexFormat = getVertexFormat();
      if (nio) vertexFormat |= (GeometryArray.BY_REFERENCE |
				GeometryArray.USE_NIO_BUFFER);
      if (interleaved) vertexFormat |= (GeometryArray.BY_REFERENCE |
					GeometryArray.INTERLEAVED);
      if (byRef) vertexFormat |= GeometryArray.BY_REFERENCE;

      int vertexCount = coordinates.length;
      
      // If the texCoordSetMap hasn't been set, assume one set of 
      // texture coordinates only and one texture state unit 
      if ((texCoordSetCount > 0) && (texCoordSetMap == null)) {
	  texCoordSetCount = 1;
	  texCoordSetMap = new int[1];
	  texCoordSetMap[0] = 0;
      }

      // Create the GeometryArray objectfillIn
      GeometryArray ga = null;
      switch (prim) {
	  case TRIANGLE_ARRAY:
	      TriangleArray ta = new TriangleArray(vertexCount, vertexFormat, 
		      texCoordSetCount, texCoordSetMap);
	      ga = (GeometryArray)ta;
	      break;
	      
	  case QUAD_ARRAY:
	      QuadArray qa = new QuadArray(vertexCount, vertexFormat, 
		      texCoordSetCount, texCoordSetMap);
	      ga = (GeometryArray)qa;
	      break;
	      
	  case TRIANGLE_STRIP_ARRAY:
	      TriangleStripArray tsa = new TriangleStripArray(vertexCount, 
		      vertexFormat, texCoordSetCount, texCoordSetMap,
		      stripCounts);
	      ga = (GeometryArray)tsa;
	      break;
	      
	  case TRIANGLE_FAN_ARRAY:
	      TriangleFanArray tfa = new TriangleFanArray(vertexCount, 
		      vertexFormat, texCoordSetCount, texCoordSetMap,
		      stripCounts);
	      ga = (GeometryArray)tfa;
	      break;
      }
      
      fillIn(ga, byRef, interleaved, nio);
      
      return ga;
  } // End of getGeometryArray(int, int)
 


  /**
   * Creates and returns a non-indexed Java 3D GeometryArray object
   * based on the data in the GeometryInfo object.  This object is
   * suitable to be attached to a Shape3D node for rendering.
   * The geometry is <b>not</b> created using data BY_REFERENCE,
   * INTERLEAVED, or USE_NIO_BUFFER.
   * @throws IllegalArgumentException if coordinate data is missing,
   * if the index lists aren't all the
   * same length, if an index list is set and the corresponding data
   * list isn't set, if a data list is set and the corresponding
   * index list is unset (unless all index lists are unset or in
   * USE_COORD_INDEX_ONLY format),
   * if StripCounts or ContourCounts is inconsistent with the current
   * primitive, if the sum of the contourCounts array doesn't equal
   * the length of the StripCounts array, or if the number of vertices
   * isn't a multiple of three (for triangles) or four (for quads).
   */
  public GeometryArray getGeometryArray()
  {
    return getGeometryArray(false, false, false);
  } // End of getGeometryArray()



  /**
   * Creates and returns a IndexedGeometryArray
   * based on the data in the GeometryInfo object.  This object is
   * suitable to be attached to a Shape3D node for rendering.
   * @param compact Remove Coordinates, Colors, Normals, and 
   * TextureCoordinates that aren't referenced by any indices.
   * @param byRef Create the IndexedGeometryArray using geometry
   * BY_REFERENCE.
   * @param interleaved Use INTERLEAVED geometry.  Implies byRef is
   * true as well.
   * @param nio Create GeometryArray using java.nio.Buffer for
   * geometry arrays.  Only usable on JDK 1.4 or higher.  Implies
   * byRef is true as well.
   * @param useCoordIndexOnly Create the IndexedGeometryArray using
   * USE_COORD_INDEX_ONLY.  Values from the coordinate index array
   * are used as a single set of indices into all vertex 
   * component arrays (coord, color, normal, and texCoord).
   * @throws IllegalArgumentException if coordinate data is missing,
   * if the index lists aren't all the
   * same length, if an index list is set and the corresponding data
   * list isn't set, if a data list is set and the corresponding
   * index list is unset (unless all index lists are unset or in
   * USE_COORD_INDEX_ONLY format),
   * if StripCounts or ContourCounts is inconsistent with the current
   * primitive, if the sum of the contourCounts array doesn't equal
   * the length of the StripCounts array, or if the number of vertices
   * isn't a multiple of three (for triangles) or four (for quads).
   */
  public IndexedGeometryArray getIndexedGeometryArray(boolean compact,
 						      boolean byRef,
						      boolean interleaved,
						      boolean useCoordIndexOnly,
						      boolean nio)
  {
      indexify(useCoordIndexOnly);
      
      if (compact) compact();

      if (prim == POLYGON_ARRAY) {
	  if (tr == null) tr = new Triangulator();
	  tr.triangulate(this);
      } else changeBackToOldPrim();

       if (useCoordIndexOnly && coordOnly == false) {
           // Check to see if we can optimize for USE_COORD_INDEX_ONLY
	   int i, j;
           boolean canUseCoordIndexOnly = true;

           if (coordinateIndices != null) {
             // See if all the array lengths are the same
             if (colorIndices != null && 
                 colorIndices.length != coordinateIndices.length) {
                 canUseCoordIndexOnly = false;
             }
             if (normalIndices != null && 
                 normalIndices.length != coordinateIndices.length) {
                 canUseCoordIndexOnly = false;
             }
             for (i = 0 ; i < texCoordSetCount ; i++) {
                 if (texCoordIndexSets[i] != null && 
                     texCoordIndexSets[i].length != coordinateIndices.length) {
                     canUseCoordIndexOnly = false;
                     break;
                 }
             }
             if (canUseCoordIndexOnly && 
		 ((colorIndices != null) || 
		  (normalIndices != null) ||
		  (texCoordSetCount > 0))) { 
                 // All array lengths are the same.  Check their contents

                 for (i=0; i<coordinateIndices.length; i++) {
                     int indexValue = coordinateIndices[i];
		     

                     if (colorIndices != null && 
                         colorIndices[i] != indexValue) {
                         canUseCoordIndexOnly = false;
                         break;
                     }
                     if (normalIndices != null && 
                         normalIndices[i] != indexValue) {
                         canUseCoordIndexOnly = false;
                         break;
                     }
                     for (j=0; j<texCoordSetCount; j++) {
                         if (texCoordIndexSets[j] != null && 
                             texCoordIndexSets[j][i] != indexValue) {
                             canUseCoordIndexOnly = false;
                             break;
                         }
                     }
                 }
             }
           }
         coordOnly = canUseCoordIndexOnly;
       }

      int vertexFormat = getVertexFormat();
      if (nio) vertexFormat |= (GeometryArray.BY_REFERENCE |
				GeometryArray.USE_NIO_BUFFER);
      if (interleaved) vertexFormat |= (GeometryArray.BY_REFERENCE |
					GeometryArray.INTERLEAVED);
      if (byRef) vertexFormat |= GeometryArray.BY_REFERENCE;
      if (coordOnly) vertexFormat |= GeometryArray.USE_COORD_INDEX_ONLY;

      int vertexCount = getVertexCount();
      
      if ((texCoordSetCount > 0) && (texCoordSetMap == null)) {
	  texCoordSetCount = 1;
	  texCoordSetMap = new int[1];
	  texCoordSetMap[0] = 0;
      }
      
      //
      // Create the IndexedGeometryArray object
      //
      
      IndexedGeometryArray ga = null;

      switch (prim) {
	  case TRIANGLE_ARRAY:
	      IndexedTriangleArray ta = new IndexedTriangleArray(vertexCount, 
		      vertexFormat, texCoordSetCount, texCoordSetMap, 
		      coordinateIndices.length);
	      ga = (IndexedGeometryArray)ta;
	      break;
	      
	  case QUAD_ARRAY:
	      IndexedQuadArray qa = new IndexedQuadArray(vertexCount, 
		      vertexFormat, texCoordSetCount, texCoordSetMap, 
		      coordinateIndices.length);
	      ga = (IndexedGeometryArray)qa;
	      break;
	  case TRIANGLE_STRIP_ARRAY:
	      IndexedTriangleStripArray tsa = new IndexedTriangleStripArray(
		      vertexCount, vertexFormat, texCoordSetCount,
		      texCoordSetMap, coordinateIndices.length, stripCounts);
	      ga = (IndexedGeometryArray)tsa;
	      break;
	      
	  case TRIANGLE_FAN_ARRAY:
	      IndexedTriangleFanArray tfa = new IndexedTriangleFanArray(
		      vertexCount, vertexFormat, texCoordSetCount,
		      texCoordSetMap, coordinateIndices.length, stripCounts);
	      ga = (IndexedGeometryArray)tfa;
	      break;
      }

      // Fill in the GeometryArray object
      fillIn(ga, byRef, interleaved, nio);
      
      return ga;
  } // End of getIndexedGeometryArray(bool, bool, bool, bool, bool)
 


  /**
   * Creates and returns an IndexedGeometryArray
   * based on the data in the GeometryInfo object.  This object is
   * suitable to be attached to a Shape3D node for rendering.
   * Equivalent to <code>getIndexedGeometryArray(compact, false, 
   * false, false, false)</code>.
   * @param compact Remove Coordinates, Colors, Normals, and 
   * TextureCoordinates that aren't referenced by any indices.
   * @throws IllegalArgumentException if coordinate data is missing,
   * if the index lists aren't all the
   * same length, if an index list is set and the corresponding data
   * list isn't set, if a data list is set and the corresponding
   * index list is unset (unless all index lists are unset or in
   * USE_COORD_INDEX_ONLY format),
   * if StripCounts or ContourCounts is inconsistent with the current
   * primitive, if the sum of the contourCounts array doesn't equal
   * the length of the StripCounts array, or if the number of vertices
   * isn't a multiple of three (for triangles) or four (for quads).
   */
  public IndexedGeometryArray getIndexedGeometryArray(boolean compact)
  {
    return getIndexedGeometryArray(compact, false, false, false, false);
  } // End of getIndexedGeometryArray(boolean)
 


  /**
   * Creates and returns an IndexedGeometryArray
   * based on the data in the GeometryInfo object.  This object is
   * suitable to be attached to a Shape3D node for rendering.
   * Equivalent to <code>getIndexedGeometryArray(false, false, 
   * false, false, false)</code>.
   * @throws IllegalArgumentException if coordinate data is missing,
   * if the index lists aren't all the
   * same length, if an index list is set and the corresponding data
   * list isn't set, if a data list is set and the corresponding
   * index list is unset (unless all index lists are unset or in
   * USE_COORD_INDEX_ONLY format),
   * if StripCounts or ContourCounts is inconsistent with the current
   * primitive, if the sum of the contourCounts array doesn't equal
   * the length of the StripCounts array, or if the number of vertices
   * isn't a multiple of three (for triangles) or four (for quads).
   */
  public IndexedGeometryArray getIndexedGeometryArray()
  {
      return getIndexedGeometryArray(false, false, false, false, false);
  } // End of getIndexedGeometryArray()
  
} // End of class GeometryInfo

// End of file GeometryInfo.java
