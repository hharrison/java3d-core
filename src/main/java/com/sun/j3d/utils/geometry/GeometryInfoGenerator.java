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

import javax.media.j3d.GeometryArray;
import javax.media.j3d.GeometryStripArray;
import javax.media.j3d.TriangleFanArray;
import javax.media.j3d.TriangleStripArray;
import javax.media.j3d.TriangleArray;
import javax.media.j3d.QuadArray;
import javax.media.j3d.IndexedGeometryArray;
import javax.media.j3d.IndexedGeometryStripArray;
import javax.media.j3d.IndexedQuadArray;
import javax.media.j3d.IndexedTriangleArray;
import javax.media.j3d.IndexedTriangleFanArray;
import javax.media.j3d.IndexedTriangleStripArray;
import javax.vecmath.*;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.internal.J3dUtilsI18N;
import com.sun.j3d.internal.BufferWrapper;
import com.sun.j3d.internal.ByteBufferWrapper;
import com.sun.j3d.internal.FloatBufferWrapper;
import com.sun.j3d.internal.DoubleBufferWrapper;
import javax.media.j3d.J3DBuffer;



/** 
 * Populate a GeometryInfo object from the Geometry provided.  Used
 * by GeometryInfo.
 */
class GeometryInfoGenerator extends Object {

  public static void create(GeometryInfo geomInfo, GeometryArray geomArray)
  {
    if (geomArray instanceof GeometryStripArray) 
      create(geomInfo, (GeometryStripArray)geomArray);
    else if (geomArray instanceof TriangleArray) {
      geomInfo.reset(GeometryInfo.TRIANGLE_ARRAY);
      processGeometryArray(geomInfo, geomArray);
    } else if (geomArray instanceof QuadArray) {
      geomInfo.reset(GeometryInfo.QUAD_ARRAY);
      processGeometryArray(geomInfo, geomArray);
    } else if (geomArray instanceof IndexedGeometryArray)
      create(geomInfo, (IndexedGeometryArray)geomArray);
    else throw new IllegalArgumentException(
      J3dUtilsI18N.getString("GeometryInfoGenerator0"));
  } // End of create(GeometryInfo, GeometryArray)



  private static void create(GeometryInfo geomInfo,
			     GeometryStripArray geomArray)
  {
    if (geomArray instanceof TriangleFanArray) {
      geomInfo.reset(GeometryInfo.TRIANGLE_FAN_ARRAY);
    } else if (geomArray instanceof TriangleStripArray) {
      geomInfo.reset(GeometryInfo.TRIANGLE_STRIP_ARRAY);
    } else throw new IllegalArgumentException(
      J3dUtilsI18N.getString("GeometryInfoGenerator0"));
    
    processGeometryArray(geomInfo, geomArray); 
    processStripArray(geomInfo, geomArray);
  } // End of create(GeometryInfo, GeometryStripArray)



  private static void create(GeometryInfo geomInfo,
			     IndexedGeometryArray geomArray)
  {
    if (geomArray instanceof IndexedQuadArray) {
      geomInfo.reset(GeometryInfo.QUAD_ARRAY);
    } else if (geomArray instanceof IndexedTriangleArray) {
      geomInfo.reset(GeometryInfo.TRIANGLE_ARRAY);
    } else if (geomArray instanceof IndexedTriangleFanArray) {
      geomInfo.reset(GeometryInfo.TRIANGLE_FAN_ARRAY);
      processIndexStripArray(geomInfo, (IndexedGeometryStripArray)geomArray);
    } else if (geomArray instanceof IndexedTriangleStripArray) {
      geomInfo.reset(GeometryInfo.TRIANGLE_STRIP_ARRAY);
      processIndexStripArray(geomInfo, (IndexedGeometryStripArray)geomArray);
    }
    
    processGeometryArray(geomInfo, geomArray);
    processIndexedArray(geomInfo, geomArray);
  } // End of create(GeometryInfo, IndexedGeometryArray)



  private static void processGeometryArray(GeometryInfo geomInfo,
					   GeometryArray geomArray)
  {
    int i, j;
    int vertexFormat = geomArray.getVertexFormat();
    int texSets = geomArray.getTexCoordSetCount();
    int valid;
    
    // Calculate validVertexCount
    if (geomArray instanceof GeometryStripArray) {
      // Does not include IndexedGeometryStripArray
      GeometryStripArray gsa = (GeometryStripArray)geomArray;
      int[] strips = new int[gsa.getNumStrips()];
      gsa.getStripVertexCounts(strips);
      valid = 0;
      for (i = 0 ; i < strips.length ; i++) {
	valid += strips[i];
      }
    } else if (geomArray instanceof IndexedGeometryArray) {
      valid = geomArray.getVertexCount();
    } else valid = geomArray.getValidVertexCount();

    if ((vertexFormat & GeometryArray.INTERLEAVED) != 0) {

      // Calculate words_per_vertex (wpv)
      int wpv = 3;			// Always have coordinate data
      if ((vertexFormat & GeometryArray.NORMALS) != 0) wpv += 3;
      if ((vertexFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_4)
	wpv += 4;
      else if ((vertexFormat & GeometryArray.COLOR_3) != 0) wpv += 3;
      if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_2) != 0) 
	wpv += 2 * texSets;
      else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_3) != 0) 
	wpv += 3 * texSets;
      else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_4) != 0) 
	wpv += 4 * texSets;

      int initial;
      if (!(geomArray instanceof IndexedGeometryArray)) {
	initial = geomArray.getInitialVertexIndex();
      } else initial = 0;

      float[] d;
      if ((vertexFormat & GeometryArray.USE_NIO_BUFFER) != 0) {
	J3DBuffer b = geomArray.getInterleavedVertexBuffer();
	FloatBufferWrapper w = new FloatBufferWrapper(b);
	d = new float[w.limit()];
	w.position( 0 );
	w.get(d);
      } else d = geomArray.getInterleavedVertices();

      int offset = 0;
      if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
	geomInfo.setTextureCoordinateParams(texSets, 2);
	int[] map = new int[geomArray.getTexCoordSetMapLength()];
	geomArray.getTexCoordSetMap(map);
	geomInfo.setTexCoordSetMap(map);
	for (i = 0 ; i < texSets ; i++) {
	  TexCoord2f[] tex = new TexCoord2f[valid];
	  for (j = 0 ; j < valid ; j++) {
	    tex[j] = new TexCoord2f(d[wpv * (j + initial) + offset],
				    d[wpv * (j + initial) + offset + 1]);
	  }
	  geomInfo.setTextureCoordinates(i, tex);
	  offset += 2;
	}
      } else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_3) != 0) {
	geomInfo.setTextureCoordinateParams(texSets, 3);
	int[] map = new int[geomArray.getTexCoordSetMapLength()];
	geomArray.getTexCoordSetMap(map);
	geomInfo.setTexCoordSetMap(map);
	for (i = 0 ; i < texSets ; i++) {
	  TexCoord3f[] tex = new TexCoord3f[valid];
	  for (j = 0 ; j < valid ; j++) {
	    tex[j] = new TexCoord3f(d[wpv * (j + initial) + offset],
				    d[wpv * (j + initial) + offset + 1],
				    d[wpv * (j + initial) + offset + 2]);
	  }
	  geomInfo.setTextureCoordinates(i, tex);
	  offset += 3;
	}
      } else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_4) != 0) {
	geomInfo.setTextureCoordinateParams(texSets, 4);
	int[] map = new int[geomArray.getTexCoordSetMapLength()];
	geomArray.getTexCoordSetMap(map);
	geomInfo.setTexCoordSetMap(map);
	for (i = 0 ; i < texSets ; i++) {
	  TexCoord4f[] tex = new TexCoord4f[valid];
	  for (j = 0 ; j < valid ; j++) {
	    tex[j] = new TexCoord4f(d[wpv * (j + initial) + offset],
				    d[wpv * (j + initial) + offset + 1],
				    d[wpv * (j + initial) + offset + 2],
				    d[wpv * (j + initial) + offset + 3]);
	  }
	  geomInfo.setTextureCoordinates(i, tex);
	  offset += 4;
	}
      }

      if ((vertexFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_4) {
	Color4f[] color = new Color4f[valid];
	for (i = 0 ; i < valid ; i++) {
	  color[i] = new Color4f(d[wpv * (i + initial) + offset],
				 d[wpv * (i + initial) + offset + 1],
				 d[wpv * (i + initial) + offset + 2],
				 d[wpv * (i + initial) + offset + 3]);
	}
	geomInfo.setColors(color);
	offset += 4;
      } else if ((vertexFormat & GeometryArray.COLOR_3) != 0) {
	Color3f[] color = new Color3f[valid];
	for (i = 0 ; i < valid ; i++) {
	  color[i] = new Color3f(d[wpv * (i + initial) + offset],
				 d[wpv * (i + initial) + offset + 1],
				 d[wpv * (i + initial) + offset + 2]);
	}
	geomInfo.setColors(color);
	offset += 3;
      }

      if ((vertexFormat & GeometryArray.NORMALS) != 0) {
	Vector3f[] normals = new Vector3f[valid];
	for (i = 0 ; i < valid ; i++) {
	  normals[i] = new Vector3f(d[wpv * (i + initial) + offset],
				    d[wpv * (i + initial) + offset + 1],
				    d[wpv * (i + initial) + offset + 2]);
	}
	geomInfo.setNormals(normals);
	offset += 3;
      }

      Point3f[] coords = new Point3f[valid];
      for (i = 0 ; i < valid ; i++) {
	coords[i] = new Point3f(d[wpv * (i + initial) + offset],
				d[wpv * (i + initial) + offset + 1],
				d[wpv * (i + initial) + offset + 2]);
      }
      geomInfo.setCoordinates(coords);
    } else {
      // Data is not INTERLEAVED
      boolean byRef = ((vertexFormat & GeometryArray.BY_REFERENCE) != 0 );
      boolean nio = ((vertexFormat & GeometryArray.USE_NIO_BUFFER) != 0 );

      Point3f[] coords = null;
      if (byRef) {

	int initial;
	if (!(geomArray instanceof IndexedGeometryArray)) {
	  initial = geomArray.getInitialCoordIndex();
	} else initial = 0;

	if ( nio ) {
	  J3DBuffer buf = geomArray.getCoordRefBuffer();

	  switch (BufferWrapper.getBufferType(buf)) {

	  case BufferWrapper.TYPE_FLOAT: {
	    FloatBufferWrapper bb = new FloatBufferWrapper(buf);
	    float[] c = new float[valid * 3];
	    bb.position(initial * 3);
	    bb.get(c, 0, valid * 3);
	    coords = new Point3f[valid];
	    for (i = 0 ; i < valid ; i++) {
	      coords[i] = new Point3f(c[i * 3 + 0],
				      c[i * 3 + 1],
				      c[i * 3 + 2]);
	    }
	  }
	  break;

	  case BufferWrapper.TYPE_DOUBLE: {
	    DoubleBufferWrapper bb = new DoubleBufferWrapper( buf );
	    double[] c = new double[valid * 3];
	    bb.position(initial * 3);
	    bb.get(c, 0, valid * 3);
	    coords = new Point3f[valid];
	    for (i = 0 ; i < valid ; i++) {
	      coords[i] = new Point3f((float)(c[i * 3 + 0]),
				      (float)(c[i * 3 + 1]),
				      (float)(c[i * 3 + 2]));
	    }
	  }
	  break;
	  }
	} else if (geomArray.getCoordRef3f() != null) {
	  if (initial != 0) {
	    Point3f[] c = geomArray.getCoordRef3f();
	    coords = new Point3f[valid];
	    for (i = 0 ; i < valid ; i++) {
	      coords[i] = new Point3f(c[i + initial]);
	    }
	  } else coords = geomArray.getCoordRef3f();
	} else if (geomArray.getCoordRef3d() != null) {
	  Point3d[] c = geomArray.getCoordRef3d();
	  coords = new Point3f[valid];
	  for (i = 0 ; i < valid ; i++) {
	    coords[i] = new Point3f(c[i + initial]);
	  }
	} else if (geomArray.getCoordRefFloat() != null) {
	  float[] c = geomArray.getCoordRefFloat();
	  coords = new Point3f[valid];
	  for (i = 0 ; i < valid ; i++) {
	    coords[i] = new Point3f(c[(i + initial) * 3],
				    c[(i + initial) * 3 + 1],
				    c[(i + initial) * 3 + 2]);
	  }
	} else if (geomArray.getCoordRefDouble() != null) {
	  double[] c = geomArray.getCoordRefDouble();
	  coords = new Point3f[valid];
	  for (i = 0 ; i < valid ; i++) {
	    coords[i] = new Point3f((float)(c[(i + initial) * 3]),
				    (float)(c[(i + initial) * 3 + 1]),
				    (float)(c[(i + initial) * 3 + 2]));
	  }
	}
	// No coordinate data - let GeometryInfo handle this.
      } else {
	// Not BY_REFERENCE
	int initial;
	if (!(geomArray instanceof IndexedGeometryArray)) {
	  initial = geomArray.getInitialVertexIndex();
	} else initial = 0;
	coords = new Point3f[valid];
	for (i = 0 ; i < valid ; i++) coords[i] = new Point3f();
	geomArray.getCoordinates(initial, coords);
      }
      geomInfo.setCoordinates(coords);
      
      if ((vertexFormat & GeometryArray.NORMALS) != 0) {
	Vector3f[] normals = null;
	if (byRef) {

	  int initial;
	  if (!(geomArray instanceof IndexedGeometryArray)) {
	    initial = geomArray.getInitialNormalIndex();
	  } else initial = 0;

	  if ( nio ) {
	    J3DBuffer buf = geomArray.getNormalRefBuffer();

	    if (BufferWrapper.getBufferType(buf) == BufferWrapper.TYPE_FLOAT) {
	      FloatBufferWrapper bb = new FloatBufferWrapper(buf);
	      float[] c = new float[valid * 3];
	      bb.position(initial * 3);
	      bb.get(c, 0, valid * 3);
	      normals = new Vector3f[valid];
	      for (i = 0 ; i < valid ; i++) {
		normals[i] = new Vector3f(c[i * 3 + 0],
					  c[i * 3 + 1],
					  c[i * 3 + 2]);
	      }
	    }
	    // Normals were set in vertexFormat but none were set - OK
	  } else if (geomArray.getNormalRef3f() != null) {
	    if (initial != 0) {
	      Vector3f[] n = geomArray.getNormalRef3f();
	      normals = new Vector3f[valid];
	      for (i = 0 ; i < valid ; i++) {
		normals[i] = new Vector3f(n[i + initial]);
	      }
	    } else normals = geomArray.getNormalRef3f();
	  } else if (geomArray.getNormalRefFloat() != null) {
	    float[] n = geomArray.getNormalRefFloat();
	    normals = new Vector3f[valid];
	    for (i = 0 ; i < valid ; i++) {
	      normals[i] = new Vector3f(n[(i + initial) * 3],
					n[(i + initial) * 3 + 1],
					n[(i + initial) * 3 + 2]);
	    }
	  }
	  // Normals were set in vertexFormat but none were set - OK
	} else {
	  // Not BY_REFERENCE
	  int initial;
	  if (!(geomArray instanceof IndexedGeometryArray)) {
	    initial = geomArray.getInitialVertexIndex();
	  } else initial = 0;
	  normals = new Vector3f[valid];
	  for (i = 0 ; i < valid ; i++) normals[i] = new Vector3f();
	  geomArray.getNormals(initial, normals);
	}
	geomInfo.setNormals(normals);
      }
      
      if ((vertexFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_4) {
	Color4f[] colors = null;
	if (byRef) {

	  int initial;
	  if (!(geomArray instanceof IndexedGeometryArray)) {
	    initial = geomArray.getInitialColorIndex();
	  } else initial = 0;

	  if ( nio ) {
	    J3DBuffer buf = geomArray.getColorRefBuffer();

	    switch (BufferWrapper.getBufferType(buf)) {

	    case BufferWrapper.TYPE_FLOAT: {
	      FloatBufferWrapper bb = new FloatBufferWrapper(buf);
	      float[] c = new float[valid * 4];
	      bb.position(initial * 4);
	      bb.get(c, 0, valid * 4);
	      colors = new Color4f[valid];
	      for (i = 0 ; i < valid ; i++) {
		colors[i] = new Color4f(c[i * 4 + 0],
					c[i * 4 + 1],
					c[i * 4 + 2],
					c[i * 4 + 3]);
	      }
	    }
	    break;

	    case BufferWrapper.TYPE_BYTE: {
	      ByteBufferWrapper bb = new ByteBufferWrapper(buf);
	      byte[] c = new byte[valid * 4];
	      bb.position(initial * 4);
	      bb.get(c, 0, valid * 4);
	      colors = new Color4f[valid];
	      for (i = 0 ; i < valid ; i++) {
		colors[i] = new Color4f((float)(c[i * 4 + 0] & 0xff) / 255.0f,
					(float)(c[i * 4 + 1] & 0xff) / 255.0f,
					(float)(c[i * 4 + 2] & 0xff) / 255.0f,
					(float)(c[i * 4 + 3] & 0xff) / 255.0f);
	      }
	    }
	    break;
	    }
	  } else if (geomArray.getColorRef4f() != null) {
	    if (initial != 0) {
	      Color4f[] c = geomArray.getColorRef4f();
	      colors = new Color4f[valid];
	      for (i = 0 ; i < valid ; i++) {
		colors[i] = new Color4f(c[i + initial]);
	      }
	    } else colors = geomArray.getColorRef4f();
	  } else if (geomArray.getColorRefFloat() != null) {
	    float[] c = geomArray.getColorRefFloat();
	    colors = new Color4f[valid];
	    for (i = 0 ; i < valid ; i++) {
	      colors[i] = new Color4f(c[(i + initial) * 4 + 0],
				      c[(i + initial) * 4 + 1],
				      c[(i + initial) * 4 + 2],
				      c[(i + initial) * 4 + 3]);
	    }
	  } else if (geomArray.getColorRefByte() != null) {
	    byte[] c = geomArray.getColorRefByte();
	    colors = new Color4f[valid];
	    for (i = 0 ; i < valid ; i++) {
	      colors[i] = new Color4f((float)(c[(i + initial) * 4 + 0] & 0xff) / 255.0f,
				      (float)(c[(i + initial) * 4 + 1] & 0xff) / 255.0f,
				      (float)(c[(i + initial) * 4 + 2] & 0xff) / 255.0f,
				      (float)(c[(i + initial) * 4 + 3] & 0xff) / 255.0f);
	    }
	  } else if (geomArray.getColorRef4b() != null) {
	    Color4b[] c = geomArray.getColorRef4b();
	    colors = new Color4f[valid];
	    for (i = 0 ; i < valid ; i++) {
	      colors[i] = new Color4f((float)(c[i + initial].x & 0xff) / 255.0f,
				      (float)(c[i + initial].y & 0xff) / 255.0f,
				      (float)(c[i + initial].z & 0xff) / 255.0f,
				      (float)(c[i + initial].w & 0xff) / 255.0f);
	    }
	  }
	  // Colors4 were set in vertexFormat but none were set - OK
	} else {
	  // Not BY_REFERENCE
	  int initial;
	  if (!(geomArray instanceof IndexedGeometryArray)) {
	    initial = geomArray.getInitialVertexIndex();
	  } else initial = 0;
	  colors = new Color4f[valid];
	  for (i = 0 ; i < valid ; i++) colors[i] = new Color4f();
	  geomArray.getColors(initial, colors);
	}
	geomInfo.setColors(colors);
      } else if ((vertexFormat & GeometryArray.COLOR_3) != 0) {
	Color3f[] colors = null;
	if (byRef) {

	  int initial;
	  if (!(geomArray instanceof IndexedGeometryArray)) {
	    initial = geomArray.getInitialColorIndex();
	  } else initial = 0;

	  if ( nio ) {
	    J3DBuffer buf = geomArray.getColorRefBuffer();

	    switch (BufferWrapper.getBufferType(buf)) {

	    case BufferWrapper.TYPE_FLOAT: {
	      FloatBufferWrapper bb = new FloatBufferWrapper(buf);
	      float[] c = new float[valid * 3];
	      bb.position(initial * 3);
	      bb.get(c, 0, valid * 3);
	      colors = new Color3f[valid];
	      for (i = 0 ; i < valid ; i++) {
		colors[i] = new Color3f(c[i * 3 + 0],
					c[i * 3 + 1],
					c[i * 3 + 2]);
	      }
	    }
	    break;

	    case BufferWrapper.TYPE_BYTE: {
	      ByteBufferWrapper bb = new ByteBufferWrapper(buf);
	      byte[] c = new byte[valid * 3];
	      bb.position(initial * 3);
	      bb.get(c, 0, valid * 3);
	      colors = new Color3f[valid];
	      for (i = 0 ; i < valid ; i++) {
		colors[i] = new Color3f((float)(c[i * 3 + 0] & 0xff) / 255.0f,
					(float)(c[i * 3 + 1] & 0xff) / 255.0f,
					(float)(c[i * 3 + 2] & 0xff) / 255.0f);
	      }
	    }
	    break;
	    }
	  } else if (geomArray.getColorRef3f() != null) {
	    if (initial != 0) {
	      Color3f[] c = geomArray.getColorRef3f();
	      colors = new Color3f[valid];
	      for (i = 0 ; i < valid ; i++) {
		colors[i] = new Color3f(c[i + initial]);
	      }
	    } else colors = geomArray.getColorRef3f();
	  } else if (geomArray.getColorRefFloat() != null) {
	    float[] c = geomArray.getColorRefFloat();
	    colors = new Color3f[valid];
	    for (i = 0 ; i < valid ; i++) {
	      colors[i] = new Color3f(c[(i + initial) * 3 + 0],
				      c[(i + initial) * 3 + 1],
				      c[(i + initial) * 3 + 2]);
	    }
	  } else if (geomArray.getColorRefByte() != null) {
	    byte[] c = geomArray.getColorRefByte();
	    colors = new Color3f[valid];
	    for (i = 0 ; i < valid ; i++) {
	      colors[i] = new Color3f((float)(c[(i + initial) * 3 + 0] & 0xff) / 255.0f,
				      (float)(c[(i + initial) * 3 + 1] & 0xff) / 255.0f,
				      (float)(c[(i + initial) * 3 + 2] & 0xff) / 255.0f);
	    }
	  } else if (geomArray.getColorRef3b() != null) {
	    Color3b[] c = geomArray.getColorRef3b();
	    colors = new Color3f[valid];
	    for (i = 0 ; i < valid ; i++) {
	      colors[i] = new Color3f((float)(c[i + initial].x & 0xff) / 255.0f,
				      (float)(c[i + initial].y & 0xff) / 255.0f,
				      (float)(c[i + initial].z & 0xff) / 255.0f);
	    }
	  }
	  // Colors3 were set in vertexFormat but none were set - OK
	} else {
	  // Not BY_REFERENCE
	  int initial;
	  if (!(geomArray instanceof IndexedGeometryArray)) {
	    initial = geomArray.getInitialVertexIndex();
	  } else initial = 0;
	  colors = new Color3f[valid];
	  for (i = 0 ; i < valid ; i++) colors[i] = new Color3f();
	  geomArray.getColors(initial, colors);
	}
	geomInfo.setColors(colors);
      }
      
      if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_4) != 0) {
	geomInfo.setTextureCoordinateParams(texSets, 4);
	for (i = 0 ; i < texSets ; i++) {
	  TexCoord4f[] tex = null;
	  if (byRef) {

	    int initial;
	    if (!(geomArray instanceof IndexedGeometryArray)) {
	      initial = geomArray.getInitialTexCoordIndex(i);
	    } else initial = 0;

	    if (nio) {
	      J3DBuffer buf = geomArray.getTexCoordRefBuffer(i);

	      if (BufferWrapper.getBufferType(buf) == BufferWrapper.TYPE_FLOAT) {
		FloatBufferWrapper bb = new FloatBufferWrapper(buf);
		float[] c = new float[valid * 4];
		bb.position(initial * 4);
		bb.get(c, 0, valid * 4);
		tex = new TexCoord4f[valid];
		for (j = 0 ; j < valid ; j++) {
		  tex[j] = new TexCoord4f(c[j * 4 + 0],
					  c[j * 4 + 1],
					  c[j * 4 + 2],
					  c[j * 4 + 3]);
		}
	      }
	      // TexCoords4 were set in vertexFormat but none were set - OK
	    } else {
	      // There if no TexCoordRef4f, so we know it's float
	      float[] t = geomArray.getTexCoordRefFloat(i);
	      tex = new TexCoord4f[valid];
	      for (j = 0 ; j < valid ; j++) {
		tex[j] = new TexCoord4f(t[(j + initial) * 4],
					t[(j + initial) * 4 + 1],
					t[(j + initial) * 4 + 2],
					t[(j + initial) * 4 + 3]);
	      }
	    }
	  } else {
	    // Not BY_REFERENCE
	    int initial;
	    if (!(geomArray instanceof IndexedGeometryArray)) {
	      initial = geomArray.getInitialVertexIndex();
	    } else initial = 0;
	    tex = new TexCoord4f[valid];
	    for (j = 0 ; j < valid ; j++) tex[j] = new TexCoord4f();
	    geomArray.getTextureCoordinates(i, initial, tex);
	  }
	  geomInfo.setTextureCoordinates(i, tex);
	}
	int[] map = new int[geomArray.getTexCoordSetMapLength()];
	geomArray.getTexCoordSetMap(map);
	geomInfo.setTexCoordSetMap(map);
      } else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_3) != 0) {
	geomInfo.setTextureCoordinateParams(texSets, 3);
	for (i = 0 ; i < texSets ; i++) {
	  TexCoord3f[] tex = null;
	  if (byRef) {

	    int initial;
	    if (!(geomArray instanceof IndexedGeometryArray)) {
	      initial = geomArray.getInitialTexCoordIndex(i);
	    } else initial = 0;

	    if (nio) {
	      J3DBuffer buf = geomArray.getTexCoordRefBuffer(i);

	      if (BufferWrapper.getBufferType(buf) == BufferWrapper.TYPE_FLOAT) {
		FloatBufferWrapper bb = new FloatBufferWrapper(buf);
		float[] c = new float[valid * 3];
		bb.position(initial * 3);
		bb.get(c, 0, valid * 3);
		tex = new TexCoord3f[valid];
		for (j = 0 ; j < valid ; j++) {
		  tex[j] = new TexCoord3f(c[j * 3 + 0],
					  c[j * 3 + 1],
					  c[j * 3 + 2]);
		}
	      }
	      // TexCoords3 were set in vertexFormat but none were set - OK
	    } else if (geomArray.getTexCoordRef3f(i) != null) {
	      if (initial != 0) {
		TexCoord3f[] t = geomArray.getTexCoordRef3f(i);
		tex = new TexCoord3f[valid];
		for (j = 0 ; j < valid ; j++) {
		  tex[j] = new TexCoord3f(t[j + initial]);
		}
	      } else tex = geomArray.getTexCoordRef3f(i);
	    } else if (geomArray.getTexCoordRefFloat(i) != null) {
	      float[] t = geomArray.getTexCoordRefFloat(i);
	      tex = new TexCoord3f[valid];
	      for (j = 0 ; j < valid ; j++) {
		tex[j] = new TexCoord3f(t[(j + initial) * 3],
					t[(j + initial) * 3 + 1],
					t[(j + initial) * 3 + 2]);
	      }
	    }
	    // TexCoords3 were set in vertexFormat but none were set - OK
	  } else {
	    // Not BY_REFERENCE
	    int initial;
	    if (!(geomArray instanceof IndexedGeometryArray)) {
	      initial = geomArray.getInitialVertexIndex();
	    } else initial = 0;
	    tex = new TexCoord3f[valid];
	    for (j = 0 ; j < valid ; j++) tex[j] = new TexCoord3f();
	    geomArray.getTextureCoordinates(i, initial, tex);
	  }
	  geomInfo.setTextureCoordinates(i, tex);
	}
	int[] map = new int[geomArray.getTexCoordSetMapLength()];
	geomArray.getTexCoordSetMap(map);
	geomInfo.setTexCoordSetMap(map);
      } else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_2) != 0 ) {
	geomInfo.setTextureCoordinateParams(texSets, 2);
	for (i = 0 ; i < texSets ; i++) {
	  TexCoord2f[] tex = null;
	  if (byRef) {

	    int initial;
	    if (!(geomArray instanceof IndexedGeometryArray)) {
	      initial = geomArray.getInitialTexCoordIndex(i);
	    } else initial = 0;

	    if (nio) {
	      J3DBuffer buf = geomArray.getTexCoordRefBuffer(i);

	      if (BufferWrapper.getBufferType(buf) == BufferWrapper.TYPE_FLOAT) {
		FloatBufferWrapper bb = new FloatBufferWrapper(buf);
		float[] c = new float[valid * 2];
		bb.position(initial * 2);
		bb.get(c, 0, valid * 2);
		tex = new TexCoord2f[valid];
		for (j = 0 ; j < valid ; j++) {
		  tex[j] = new TexCoord2f(c[j * 2 + 0],
					  c[j * 2 + 1]);
		}
	      }
	      // TexCoords2 were set in vertexFormat but none were set - OK
	    } else if (geomArray.getTexCoordRefFloat(i) != null) {
	      float[] t = geomArray.getTexCoordRefFloat(i);
	      tex = new TexCoord2f[valid];
	      for (j = 0 ; j < valid ; j++) {
		tex[j] = new TexCoord2f(t[(j + initial) * 2 + 0],
					t[(j + initial) * 2 + 1]);
	      }
	    } else if (geomArray.getTexCoordRef2f(i) != null) {
	      if (initial != 0) {
		TexCoord2f[] t = geomArray.getTexCoordRef2f(i);
		tex = new TexCoord2f[valid];
		for (j = 0 ; j < valid ; j++) {
		  tex[j] = new TexCoord2f(t[j + initial]);
		}
	      } else tex = geomArray.getTexCoordRef2f(i);
	    }
	    // TexCoords2 were set in vertexFormat but none were set - OK
	  } else {
	    // Not BY_REFERENCE
	    int initial;
	    if (!(geomArray instanceof IndexedGeometryArray)) {
	      initial = geomArray.getInitialVertexIndex();
	    } else initial = 0;
	    tex = new TexCoord2f[valid];
	    for (j = 0 ; j < valid ; j++) tex[j] = new TexCoord2f();
	    geomArray.getTextureCoordinates(i, initial, tex);
	  }
	  geomInfo.setTextureCoordinates(i, tex);
	}
	int[] map = new int[geomArray.getTexCoordSetMapLength()];
	geomArray.getTexCoordSetMap(map);
	geomInfo.setTexCoordSetMap(map);
      }
    }
  } // End of processGeometryArray
 


  private static void processIndexedArray(GeometryInfo geomInfo,
					  IndexedGeometryArray geomArray)
  {
    int initial = geomArray.getInitialIndexIndex();
    int vertexFormat = geomArray.getVertexFormat();
    int texSets = geomArray.getTexCoordSetCount();

    int valid;
    if (geomArray instanceof IndexedGeometryStripArray) {
      IndexedGeometryStripArray igsa = (IndexedGeometryStripArray)geomArray;
      int[] strips = new int[igsa.getNumStrips()];
      igsa.getStripIndexCounts(strips);
      valid = 0;
      for (int i = 0 ; i < strips.length ; i++) {
	valid += strips[i];
      }
    } else {
      valid = geomArray.getValidIndexCount();
    }

    int[] coordI = new int[valid];
    geomArray.getCoordinateIndices(initial, coordI);
    geomInfo.setCoordinateIndices(coordI);

    if ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) != 0) {
      if ((vertexFormat & GeometryArray.NORMALS) != 0)
	geomInfo.setNormalIndices(coordI);
      if (((vertexFormat & GeometryArray.COLOR_3) != 0) ||
	  ((vertexFormat & GeometryArray.COLOR_4) != 0))
	geomInfo.setColorIndices(coordI);
      if (((vertexFormat & GeometryArray.TEXTURE_COORDINATE_2) != 0) ||
	  ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_3) != 0) ||
	  ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_4) != 0)) {
	for (int i = 0 ; i < texSets ; i++) {
	  geomInfo.setTextureCoordinateIndices(i, coordI);
	}
      }
    } else {
      if ((vertexFormat & GeometryArray.NORMALS) != 0) {
	int[] normalI = new int[valid];
	geomArray.getNormalIndices(initial, normalI);
	geomInfo.setNormalIndices(normalI);
      }
      
      if (((vertexFormat & GeometryArray.COLOR_3) != 0) ||
	  ((vertexFormat & GeometryArray.COLOR_4) != 0)) {
	int[] colorI = new int[valid];
	geomArray.getColorIndices(initial, colorI);
	geomInfo.setColorIndices(colorI);
      }
      
      if (((vertexFormat & GeometryArray.TEXTURE_COORDINATE_2) != 0) ||
	  ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_3) != 0) ||
	  ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_4) != 0)) {
	for (int i = 0 ; i < texSets ; i++) {
	  int[] texI = new int[valid];
	  geomArray.getTextureCoordinateIndices(i, initial, texI);
	  geomInfo.setTextureCoordinateIndices(i, texI);
	}
      }
    }
  } // End of processIndexedArray
 


  private static void processStripArray(GeometryInfo geomInfo, 
					GeometryStripArray geomArray)
  {
    int[] strips = new int[geomArray.getNumStrips()];
    geomArray.getStripVertexCounts(strips);
    geomInfo.setStripCounts(strips);
  } // End of processStripArray
 


  private static void processIndexStripArray(
    GeometryInfo geomInfo, IndexedGeometryStripArray geomArray)
  {
    int[] strips = new int[geomArray.getNumStrips()];
    geomArray.getStripIndexCounts(strips);
    geomInfo.setStripCounts(strips);
  } // End of processIndexStripArray

} // End of class GeometryInfoGenerator
