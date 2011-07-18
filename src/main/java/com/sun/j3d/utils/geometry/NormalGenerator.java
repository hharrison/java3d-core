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

import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.EdgeTable;
import java.util.ArrayList;
import javax.vecmath.Vector3f;
import javax.vecmath.Point3f;

/**
 * The NormalGenerator utility will calculate and fill in the normals
 * of a GeometryInfo object.  The calculated normals are estimated based
 * on an analysis of the indexed coordinate information. If your data
 * isn't indexed, index lists will be created.<p>
 * <p>
 * If two (or more) triangles in the model share the same coordinate
 * index then the normal generator will attempt to generate one normal
 * for the vertex, resulting in a "smooth" looking surface.  If two
 * coordinates don't have the same index then they will have two
 * separate normals, even if they have the same position.  This will
 * result in a "crease" in your object.  If you suspect that your
 * data isn't properly indexed, call GeometryInfo.recomputeIndexes(). <p>
 * <p>
 * Of course, sometimes your model *has* a crease in it.  That's what
 * creaseAngle is.  If two triangles' normals differ by more than
 * creaseAngle, then the vertex will get two separate normals, creating a 
 * discontinuous crease in the model.  This is perfect for the edge
 * of a table or the corner of a cube, for instance.
 */

public class NormalGenerator {

  private double creaseAngle;
  private Vector3f facetNorms[];
  private ArrayList tally;
  private GeometryInfo gi;
  private int coordInds[];
  private int normalInds[];
  private int colorInds[];
  private int texInds[][];
  private int stripCounts[];
  private static long t1=0, t2=0, t3=0, t4=0, t5=0, t6=0;
  private Triangulator tr = null;
  private int numTexSets;


  // 0 - No debug info
  // 1 - Facet Normals
  // 2 - Connection info
  // 4 - Normals
  // 8 - StripCounts
  // 16 - Timing
  // 32 - Hard edges
  // 64 - Coordinate and normal indices
  // 128 - Vertex normal calculation info
  private static final int DEBUG = 0;



  // Calculate the normal of each triangle in the list by finding
  // the cross product
  private void calculatefacetNorms()
  {
    Point3f coordinates[] = gi.getCoordinates();
    facetNorms = new Vector3f[coordInds.length / 3];
    Vector3f a = new Vector3f();
    Vector3f b = new Vector3f();
    if ((DEBUG & 1) != 0) System.out.println("Facet normals:");

    if (gi.getOldPrim() != gi.QUAD_ARRAY) {
      for (int t = 0 ; t < coordInds.length ; t += 3) {
	a.sub(coordinates[coordInds[t + 2]], coordinates[coordInds[t + 1]]);
	b.sub(coordinates[coordInds[t + 0]], coordinates[coordInds[t + 1]]);
	facetNorms[t / 3] = new Vector3f();
	facetNorms[t / 3].cross(a, b);
	facetNorms[t / 3].normalize();

	if (Float.isNaN(facetNorms[t / 3].x)) {
	  // Normal isn't valid
	  facetNorms[t / 3].x = 1.0f;
	  facetNorms[t / 3].y = facetNorms[t / 3].z = 0.0f;
	}
	if ((DEBUG & 1) != 0) {
	  System.out.println("  " + (t/3) + " " + facetNorms[t / 3]);
	}
      }
    } else {
      // For quads, the facet normal of both triangles is the cross 
      // product of the two vectors that make an 'X' across the quad.
      for (int t = 0 ; t < coordInds.length ; t += 6) {
	a.sub(coordinates[coordInds[t + 2]], coordinates[coordInds[t + 0]]);
	b.sub(coordinates[coordInds[t + 5]], coordinates[coordInds[t + 1]]);
	facetNorms[t / 3] = new Vector3f();
	facetNorms[t / 3].cross(a, b);
	facetNorms[t / 3].normalize();

	if (Float.isNaN(facetNorms[t / 3].x)) {
	  // Normal isn't valid
	  facetNorms[t / 3].x = 1.0f;
	  facetNorms[t / 3].y = facetNorms[t / 3].z = 0.0f;
	}

        // Second triangle of quad
	facetNorms[t / 3 + 1] = new Vector3f(facetNorms[t / 3]);

	if ((DEBUG & 1) != 0) {
	  System.out.println("  " + (t/3) + "&" + (t/3 + 1) + " " +
	    facetNorms[t / 3]);
	}
      }
    }
  } // End of calculatefacetNorms



  // The vertex normals will be calculated by averaging the facet normals
  // of groups of triangles sharing the vertex.  At the end of this routine
  // the groups of coordinate indexes will all be made, and the normal
  // indices will point to these groups.
  // 
  // The routine works by going through each vertex of each triangle.
  // Starting at a triangle, we see if the vertex normal can be shared
  // with the neighbor triangle (their facet normals differ by less than
  // creaseAngle).  If they can be shared, then we move from that triangle
  // to the next and the next in a circle around the vertex.
  //
  // If we hit the edge of the model or a Hard Edge (crease) then we stop
  // and then try going around the vertex in the other direction.
  //
  // Each time we step from one triangle to the next around the center
  // vertex, the triangle is added to the group of triangles whose normals
  // will be averaged to make the vertex normal.
  // 
  // Returns the largest number of triangles that share a single normal.
  //
  private int createHardEdges()
  {
    EdgeTable et = new EdgeTable(coordInds);
    tally = new ArrayList();
    int normalMap[] = new int[coordInds.length];
    int maxShare = 1;
    float cosine;
    boolean smooth;
    float threshold = (float)Math.cos(creaseAngle);
    boolean goingRight;

    // Set Normal Indices array values to a flag
    for (int c = 0 ; c < coordInds.length ; c++)
      normalMap[c] = Integer.MAX_VALUE;

    // Cycle through each vertex
    for (int c = 0 ; c < coordInds.length ; c++) {
      // See if this vertex's normal has already been done
      if (normalMap[c] == Integer.MAX_VALUE) {
	if ((DEBUG & 32) != 0) {
	  System.out.println(
	    "Coordinate Index " + c + ": vertex " + coordInds[c]);
	}
	// Create a list of vertices used for calculating this normal
	ArrayList sharers = new ArrayList();
	tally.add(sharers);
	// Put this coordinate in the list
	sharers.add(new Integer(c));
	// Point this coordinate's index at its list
	normalMap[c] = tally.size() - 1;

	// First do right edge
	goingRight = true;
	Edge edge = new Edge(coordInds[c],
			     coordInds[(c + 1) % 3 == 0 ? c - 2 : c + 1]);
	if ((DEBUG & 32) != 0)
	  System.out.println( "  Right edge: " + edge);

	// This is how we'll know we've gone all the way around
	int endVertex = coordInds[c % 3 == 0 ? c + 2 : c - 1];

        // Start at current triangle
        int cur = c;

	// Proceed from one triangle to the next
	do {
	  // Look up edge in Edge Table to find neighbor triangle
	  Integer tableVal = et.get(edge.v2, edge.v1);
	  if ((DEBUG & 32) != 0) {
	    System.out.println(
	      "  Search Edge: " + (new Edge(edge.v2, edge.v1)));
	  }

	  // See if there is no triangle on the other side of this edge
	  if (tableVal == null) {
	    smooth = false;
	    if ((DEBUG & 32) != 0)
	      System.out.println("    No neighboring triangle found.");
	  } else {

	    int n = tableVal.intValue();
	    if ((DEBUG & 32) != 0) {
	      System.out.println(
		"    Table lookup result: " + n + " (vertex " + coordInds[n] +
		")");
	      System.out.print("      Triangles " + (cur/3) + " & " + (n/3) +
		": ");
	    }

	    cosine = facetNorms[cur / 3].dot(facetNorms[n / 3]);
	    smooth = cosine > threshold;
	    if (smooth) {
	      // The center coordinate (c) shares the same normal in these
	      // two triangles.  Find that coordinate and set its index
	      // normalMap[n] = normalMap[cur];
	      int centerv = (((n + 1) % 3) == 0 ? n - 2 : n + 1);
	      if (coordInds[c] != coordInds[centerv]) {
		centerv = ((n % 3) == 0 ? n + 2 : n - 1);
	      }

	      if ((DEBUG & 32) != 0)
		System.out.println("Smooth!  Adding " + centerv);

	      if (normalMap[centerv] != Integer.MAX_VALUE) {
		smooth = false;
		if ((DEBUG & 32) != 0) System.out.println(
		  "    Error:  Coordinate aleady has normal (bad data).");
	      } else {

		normalMap[centerv] = tally.size() - 1;

		// Consider this triangle's facet normal when calculating the
		// vertex's normal
		sharers.add(new Integer(centerv));
		if (sharers.size() > maxShare) maxShare = sharers.size();

		// Continue on around the vertex to the next triangle
		cur = n;
		if (goingRight) edge.v2 = coordInds[cur];
		else edge.v1 = coordInds[cur];
	      }
	    } else if ((DEBUG & 32) != 0) System.out.println("Hard Edge!");
	  }
	  
	  if (!smooth && goingRight) {

	    // We've hit an impasse going right, so now try going left
	    // from the original triangle
	    goingRight = false;
	    smooth = true;		// Trick do loop
	    cur = c;			// Go back to original triangle

	    edge = new Edge(coordInds[(c % 3) == 0 ? c + 2 : c - 1],
			    coordInds[c]);
	    if ((DEBUG & 32) != 0) System.out.println( "  Left edge: " + edge);

	  }
          
	} while (smooth && ((goingRight && (edge.v2 != endVertex)) ||
			    !goingRight));

	if (((DEBUG & 32) != 0) && goingRight && (edge.v2 == endVertex))
	  System.out.println("  Went all the way around!");
      }
    }

    if ((DEBUG & 32) != 0) {
      System.out.println("Tally:");
      for (int i = 0 ; i < tally.size() ; i++) {
	System.out.print("  " + i + ": ");
	ArrayList sharers = (ArrayList)(tally.get(i));
	for (int j = 0 ; j < sharers.size() ; j++) {
	  System.out.print(" " + sharers.get(j));
	}
	System.out.println();
      }

      System.out.println("Normal Indexes:");
      for (int i = 0 ; i < normalMap.length ; i++) {
	System.out.println("  " + i + ": " + normalMap[i]);
      }
    }

    return maxShare;
  } // End of createHardEdges


  // Now take all of the triangles who share a vertex (who have
  // been grouped by the hard edge process) and average their facet
  // normals to get the vertex normal
  //
  // This routine has something of a hack in it.  We found that our
  // method of breaking up data into individual triangles before
  // calculating normals was causing a bug.  If a polygon was broken
  // into two triangles at a particular vertex, then that facet's 
  // normal would get averaged into the vertex normal *twice*,
  // skewing the normal toward the decomposed facet.  So what we did
  // was to check for duplicate facet normals as we're averaging,
  // not allowing the same facet normal to be counted twice.  
  // 
  // What should be done is to put the facets' normals into a separate,
  // indexed, table.  That way, to tell if two triangles have the
  // same normal, we just need to compare indexes.  This would speed up
  // the process of checking for duplicates.
  private void calculateVertexNormals(int maxShare)
  {
    Vector3f normals[];
    ArrayList sharers;
    int triangle;
    Vector3f fn[];	// Normals of facets joined by this vertex
    int fnsize;		// Number of elements currently ised in fn

    if (creaseAngle != 0.0) {
      fn = new Vector3f[maxShare];
      normals = new Vector3f[tally.size()];
      normalInds = new int[coordInds.length];
      for (int n = 0 ; n < tally.size() ; n++) {
	sharers = (ArrayList)(tally.get(n));
	if ((DEBUG & 128) != 0) {
	  System.out.println(n + ": " + sharers.size() +
	    " triangles:");
	}
	fnsize = 0;
	normals[n] = new Vector3f();
	for (int t = 0 ; t < sharers.size() ; t++) {
	  int v = ((Integer)sharers.get(t)).intValue();
	  // See if index removed by hard edge process
	  if (v != -1) {
	    triangle = v / 3;
	    if (!Float.isNaN(facetNorms[triangle].x)) {

	      int f;
	      // Don't add the same facet normal twice
	      for (f = 0 ; f < fnsize ; f++) {
		if (fn[f].equals(facetNorms[triangle])) break;
	      }

	      normalInds[v] = n;
	      if (f == fnsize) {
		// Didn't find this triangle's normal already in the list
		normals[n].add(facetNorms[triangle]);
		fn[fnsize++] = facetNorms[triangle];
	      } else if ((DEBUG & 128) != 0) {
		System.out.println("  triangle " + t + " ignored.");
	      }
	    }
	  }
	}
	normals[n].normalize();
	if (Float.isNaN(normals[n].x)) {
	  // Normal isn't valid
	  normals[n].x = 1.0f; normals[n].y = normals[n].z = 0.0f;
	}
	if ((DEBUG & 128) != 0) {
	  for (int t = 0 ; t < sharers.size() ; t++) {
	    int v = ((Integer)sharers.get(t)).intValue();
	    if (v != -1) {
	      triangle = v / 3;
	      System.out.println("  " + facetNorms[triangle]);
	    }
	  }
	  System.out.println("  Result: " + normals[n]);
	  System.out.println();
	}
      }
    } else {
      // This code renders the facet normals
      normals = facetNorms;

      normalInds = new int[facetNorms.length * 3];
      for (int i = 0 ; i < facetNorms.length ; i++) {
	normalInds[i * 3 + 0] = i;
	normalInds[i * 3 + 1] = i;
	normalInds[i * 3 + 2] = i;
      }
    }
    gi.setNormals(normals);

    if ((DEBUG & 4) != 0) {
      System.out.println("Normals:");
      for (int i = 0 ; i < normals.length ; i++) {
	System.out.println("  " + i + " " + normals[i]);
      }
      System.out.println("Indices:");
      for (int i = 0 ; i < normalInds.length ; i++) {
	System.out.println("  " + i + " " + normalInds[i]);
      }
    }
  } // End of calculateVertexNormals



  // The original data was in quads and we converted it to triangles to
  // calculate the normals.  Now we are converting it back to quads.
  // It's a very simple algorithm.
  // Since both sub-triangles of a quad have the same facet normal,
  // there should never be a hard edge down the middle of the quad.
  // Therefore, the vertices of the shared edge of the two subtriangles
  // should have the same normal index in both triangles.
  private int[] triToQuadIndices(int oldList[])
  {
    if (oldList == null) return null;

    int newList[] = new int[oldList.length / 6 * 4];
						// index list to pass back
    // Cycle through each pair of triangles and put them together
    for (int q = 0 ; q < oldList.length / 6 ; q++) {
      newList[q * 4 + 0] = oldList[q * 6 + 0];
      newList[q * 4 + 1] = oldList[q * 6 + 1];
      newList[q * 4 + 2] = oldList[q * 6 + 2];
      newList[q * 4 + 3] = oldList[q * 6 + 5];
    }

    return newList;
  } // End of triToQuadIndices



  // The original data was in quads.  We converted it to triangles to
  // calculate normals.  Now we need to convert it back to quads. 
  private void convertTriToQuad(GeometryInfo geom)
  {
    // Create the new arrays
    geom.setCoordinateIndices(
      triToQuadIndices(geom.getCoordinateIndices()));
    geom.setColorIndices(triToQuadIndices(geom.getColorIndices()));
    geom.setNormalIndices(triToQuadIndices(geom.getNormalIndices()));
    int num = geom.getTexCoordSetCount();
    for (int i = 0 ; i < num ; i++) {
      geom.setTextureCoordinateIndices(i,
	triToQuadIndices(geom.getTextureCoordinateIndices(i)));
    }
    geom.setPrimitive(gi.QUAD_ARRAY);
  } // End of convertTriToQuad()



  // The original data was in fans and we converted it to triangles to
  // calculate the normals.  Now we are converting it back to fans.
  // We have already calculated the new stripCounts, so now we need
  // to change the index lists so they match up with the stripCounts.
  // It's a very simple algorithm.  The paramater oldList is the 
  // index list being compressed back into fans (could be coordinate,
  // color, normal, or texCoord indices) and numVerts is the pre-
  // calculated total of all entries of the stripCounts array.
  private int[] triToFanIndices(int sc[], int oldList[], int numVerts)
  {
    if (oldList == null) return null;

    int newList[] = new int[numVerts];		// index list to pass back
    int vert1 = 0;				// 1st vertex of triangle
    int n = 0;					// index into newList
    // Cycle through each fan in the new list
    for (int f = 0 ; f < sc.length ; f++) {
      // Copy entire first triangle into new array
      newList[n++] = oldList[vert1++];
      newList[n++] = oldList[vert1++];
      newList[n++] = oldList[vert1++];
      // Each additional triangle in the fan only needs one vertex
      for (int t = 3 ; t < sc[f] ; t++) {
	newList[n++] = oldList[vert1 + 2];
	vert1 += 3;
      }
    }
    return newList;
  } // End of triToFanIndices



  //
  // The original data was in fans.  We converted it to triangles to
  // calculate normals.  Now we need to convert it back to fans. 
  // The hard part is that, if we found a hard edge in the middle of
  // a fan, we need to split the fan into two.  To tell if there's
  // a hard edge there, we compare the normal indices of both
  // vertices comprising the edge.
  private void convertTriToFan(GeometryInfo geom, int oldStripCounts[])
  {
    int ni[] = geom.getNormalIndices();

    //
    // Calculate new stripCounts array
    //
    int tri = 0;	// Which triangle currently being converted
    ArrayList newStripCounts;
    newStripCounts = new ArrayList(oldStripCounts.length + 100);

    // Use the original stripCounts array
    for (int f = 0 ; f < oldStripCounts.length ; f++) {
      int stripCount = 3;

      // Cycle through each triangle in the fan, comparing to the
      // next triangle in the fan.  Compare the normal indices of
      // both vertices of the edge to see if the two triangles
      // can be mated
      for (int t = 0 ; t < oldStripCounts[f] - 3 ; t++) {
	// The first vertex of this triangle must match the first
	// vertex of the next, AND the third vertex of this
	// triangle must match the second vertex of the next.
	if ((ni[tri * 3] == ni[(tri+1) * 3]) &&
	    (ni[tri * 3 + 2] == ni[(tri+1) * 3 + 1])) {
	  // OK to extend fan
	  stripCount++;
	} else {
	  // hard edge within fan
	  newStripCounts.add(new Integer(stripCount));
	  stripCount = 3;
	}
	tri++;
      }
      tri++;
      newStripCounts.add(new Integer(stripCount));
    }

    // Convert from ArrayList to int[]
    int sc[] = new int[newStripCounts.size()];
    for (int i = 0 ; i < sc.length ; i++)
      sc[i] = ((Integer)newStripCounts.get(i)).intValue();
    newStripCounts = null;

    //
    // Change the index lists so they match up with the new stripCounts
    //

    // See how many vertices we'll need
    int c = 0;
    for (int i = 0 ; i < sc.length ; i++) c += sc[i];

    // Create the new arrays
    geom.setCoordinateIndices(
      triToFanIndices(sc, geom.getCoordinateIndices(), c));
    geom.setColorIndices(triToFanIndices(sc, geom.getColorIndices(), c));
    geom.setNormalIndices(triToFanIndices(sc, geom.getNormalIndices(), c));
    int num = geom.getTexCoordSetCount();
    for (int i = 0 ; i < num ; i++) {
      geom.setTextureCoordinateIndices(i, 
	triToFanIndices(sc, geom.getTextureCoordinateIndices(i), c));
    }

    if ((DEBUG & 8) != 0) {
      System.out.print("Old stripCounts:");
      for (int i = 0 ; i < oldStripCounts.length ; i++) {
	System.out.print(" " + oldStripCounts[i]);
      }
      System.out.println();
      System.out.print("New stripCounts:");
      for (int i = 0 ; i < sc.length ; i++) {
	System.out.print(" " + sc[i]);
      }
      System.out.println();
    }

    geom.setStripCounts(sc);
    geom.setPrimitive(gi.TRIANGLE_FAN_ARRAY);
  } // End of convertTriToFan()



  // The original data was in strips and we converted it to triangles to
  // calculate the normals.  Now we are converting it back to strips.
  // We have already calculated the new stripCounts, so now we need
  // to change the index lists so they match up with the stripCounts.
  // It's a very simple algorithm.  The paramater oldList is the 
  // index list being compressed back into strips (could be coordinate,
  // color, normal, or texCoord indices) and numVerts is the pre-
  // calculated total of all entries of the stripCounts array.
  private int[] triToStripIndices(int sc[], int oldList[], int numVerts)
  {
    if (oldList == null) return null;

    int newList[] = new int[numVerts];		// index list to pass back
    int vert1 = 0;				// 1st vertex of triangle
    int n = 0;					// index into newList
    // Cycle through each strip in the new list
    for (int f = 0 ; f < sc.length ; f++) {
      // Copy entire first triangle into new array
      newList[n++] = oldList[vert1++];
      newList[n++] = oldList[vert1++];
      newList[n++] = oldList[vert1++];
      // Each additional triangle in the fan only needs one vertex
      for (int t = 3 ; t < sc[f] ; t++) {
	// Every other triangle has been reversed to preserve winding
	newList[n++] = oldList[vert1 + 2 - (t % 2)];
	vert1 += 3;
      }
    }
    return newList;
  } // End of triToStripIndices



  private void convertTriToStrip(GeometryInfo geom, int oldStripCounts[])
  {
    int ni[] = geom.getNormalIndices();

    //
    // Calculate new stripCounts array
    //
    int tri = 0;	// Which triangle currently being converted
    ArrayList newStripCounts;
    newStripCounts = new ArrayList(oldStripCounts.length + 100);

    // Use the original stripCounts array
    for (int f = 0 ; f < oldStripCounts.length ; f++) {
      int stripCount = 3;

      // Cycle through each triangle in the strip, comparing to the
      // next triangle in the strip.  Compare the normal indices of
      // both vertices of the edge to see if the two triangles
      // can be mated.
      for (int t = 0 ; t < oldStripCounts[f] - 3 ; t++) {
	// Every other triangle has been reversed to preserve winding
	if (t % 2 == 0) {
	  // The middle vertex of this triangle needs to match the
	  // first vertex of the next, AND the third vertices of
	  // the two triangles must match
	  if ((ni[tri * 3 + 1] == ni[(tri+1) * 3]) &&
	      (ni[tri * 3 + 2] == ni[(tri+1) * 3 + 2])) {
	    // OK to extend strip
	    stripCount++;
	  } else {
	    // hard edge within strip
	    newStripCounts.add(new Integer(stripCount));
	    stripCount = 3;

	    // Can't start a new strip on an odd edge so output
	    // isolated triangle
	    if (t < oldStripCounts[f] - 4) {
	      newStripCounts.add(new Integer(3));
	      t++;
	    }
	  }
	} else {
	  // The middle vertex of this triangle must match the middle 
	  // vertex of the next, AND the third vertex of this triangle
	  // must match the first vertex of the next
	  if ((ni[tri * 3 + 1] == ni[(tri+1) * 3 + 1]) &&
	      (ni[tri * 3 + 2] == ni[(tri+1) * 3])) {
	    // OK to extend strip
	    stripCount++;
	  } else {
	    // hard edge within strip
	    newStripCounts.add(new Integer(stripCount));
	    stripCount = 3;
	  }
	}
	tri++;
      }
      tri++;
      newStripCounts.add(new Integer(stripCount));
    }

    // Convert from ArrayList to int[]
    int sc[] = new int[newStripCounts.size()];
    for (int i = 0 ; i < sc.length ; i++)
      sc[i] = ((Integer)newStripCounts.get(i)).intValue();
    newStripCounts = null;

    //
    // Change the index lists so they match up with the new stripCounts
    //

    // See how many vertices we'll need
    int c = 0;
    for (int i = 0 ; i < sc.length ; i++) c += sc[i];

    // Create the new arrays
    geom.setCoordinateIndices(
      triToStripIndices(sc, geom.getCoordinateIndices(), c));
    geom.setColorIndices(triToStripIndices(sc, geom.getColorIndices(), c));
    geom.setNormalIndices(triToStripIndices(sc, geom.getNormalIndices(), c));
    int num = geom.getTexCoordSetCount();
    for (int i = 0 ; i < num ; i++) {
      geom.setTextureCoordinateIndices(i, 
	triToStripIndices(sc, geom.getTextureCoordinateIndices(i), c));
    }

    if ((DEBUG & 8) != 0) {
      System.out.print("Old stripCounts:");
      for (int i = 0 ; i < oldStripCounts.length ; i++) {
	System.out.print(" " + oldStripCounts[i]);
      }
      System.out.println();
      System.out.print("New stripCounts:");
      for (int i = 0 ; i < sc.length ; i++) {
	System.out.print(" " + sc[i]);
      }
      System.out.println();
    }

    geom.setStripCounts(sc);
    geom.setPrimitive(gi.TRIANGLE_STRIP_ARRAY);
  }// End of convertTriToStrip()



  /**
   * Used when the user calls the NormalGenerator and not
   * the Stripifier or Triangulator.   We had to convert
   * the user's data to indexed triangles before we could
   * generate normals, so now we need to switch back to
   * the original format.
   */
  void convertBackToOldPrim(GeometryInfo geom, int oldPrim,
			    int oldStripCounts[])
  {
    if (oldPrim == geom.TRIANGLE_ARRAY) return;

    switch (oldPrim) {
    case GeometryInfo.QUAD_ARRAY:
      convertTriToQuad(geom);
      break;
    case GeometryInfo.TRIANGLE_FAN_ARRAY:
      convertTriToFan(geom, oldStripCounts);
      break;
    case GeometryInfo.TRIANGLE_STRIP_ARRAY:
      convertTriToStrip(geom, oldStripCounts);
      break;
    }

    if ((DEBUG & 64) != 0) {
      System.out.println("Coordinate and normal indices (original):");
      for (int i = 0 ; i < coordInds.length ; i++) {
	System.out.println(i + "  " + coordInds[i] + "  " + normalInds[i]);
      }
    }
  } // End of convertBackToOldPrim



  /**
   * Generate normals for the GeometryInfo object.  If the GeometryInfo
   * object didn't previously contain indexed data, indexes are made
   * by collapsing identical positions into a single index.  Any
   * normal information previously contained in the GeometryInfo 
   * object is lost.  Strips and Fans are converted into individual
   * triangles for Normal generation, but are stitched back together
   * if GeometryInfo.getGeometryArray() (or getIndexedGeometryArray())
   * is called without stripifying first.
   */
  public void generateNormals(GeometryInfo geom)
  {
    gi = geom;
    gi.setNormals((Vector3f[])null);
    gi.setNormalIndices(null);

    long time = 0L;
    if ((DEBUG & 16) != 0) {
      time = System.currentTimeMillis();
    }

    if (gi.getPrimitive() == gi.POLYGON_ARRAY) {
      if (tr == null) tr = new Triangulator();
      tr.triangulate(gi);
    } else {
      // NOTE:  We should calculate facet normals before converting to
      // triangles.
      gi.rememberOldPrim();
      gi.convertToIndexedTriangles();
    }

    // Cache some of the GeometryInfo fields
    coordInds = gi.getCoordinateIndices();
    colorInds = gi.getColorIndices();
    normalInds = gi.getNormalIndices();
    numTexSets = gi.getTexCoordSetCount();
    texInds = new int[numTexSets][];
    for (int i = 0 ; i < numTexSets ; i++) {
      texInds[i] = gi.getTextureCoordinateIndices(i);
    }
    stripCounts = gi.getStripCounts();

    if ((DEBUG & 16) != 0) {
      t1 += System.currentTimeMillis() - time;
      System.out.println("Convert to triangles: " + t1 + " ms");
      time = System.currentTimeMillis();
    }
      
    calculatefacetNorms();
    if ((DEBUG & 16) != 0) {
      t2 += System.currentTimeMillis() - time;
      System.out.println("Calculate Facet Normals: " + t2 + " ms");
      time = System.currentTimeMillis();
    }
      
    int maxShare = createHardEdges();
    if ((DEBUG & 16) != 0) {
      t3 += System.currentTimeMillis() - time;
      System.out.println("Hard Edges: " + t3 + " ms");
      time = System.currentTimeMillis();
    }
      
    calculateVertexNormals(maxShare);
    if ((DEBUG & 16) != 0) {
      t5 += System.currentTimeMillis() - time;
      System.out.println("Vertex Normals: " + t5 + " ms");
      time = System.currentTimeMillis();
    }

    if ((DEBUG & 64) != 0) {
      System.out.println("Coordinate and normal indices (triangles):");
      for (int i = 0 ; i < coordInds.length ; i++) {
	System.out.println(i + "  " + coordInds[i] + "  " + normalInds[i]);
      }
    }
      
    // We have been caching some info from the GeometryInfo, so we need
    // to update it.
    gi.setCoordinateIndices(coordInds);
    gi.setColorIndices(colorInds);
    gi.setNormalIndices(normalInds);
    for (int i = 0 ; i < numTexSets ; i++) {
      gi.setTextureCoordinateIndices(i, texInds[i]);
    }
    gi.setStripCounts(stripCounts);
  } // End of generateNormals



  /**
   * Set the crease angle.  
   * If two triangles' normals differ by more than
   * creaseAngle, then the vertex will get two separate normals, creating a 
   * discontinuous crease in the model.  This is perfect for the edge
   * of a table or the corner of a cube, for instance.  Clamped to
   * 0 <= creaseAngle <= PI.  Optimizations are made for creaseAngle == 0
   * (facet normals) and creaseAngle == PI (smooth shading).
   */
  public void setCreaseAngle(double radians)
  {
    if (radians > Math.PI) radians = Math.PI;
    if (radians < 0.0) radians = 0.0;
    creaseAngle = radians;
  } // End of setCreaseAngle



  /**
   * Returns the current value of the crease angle, in radians.
   */
  public double getCreaseAngle()
  {
    return creaseAngle;
  } // End of getCreaseAngle



  /**
   * Constructor.  Construct a NormalGenerator object with creaseAngle
   * set to the given value.
   */
  public NormalGenerator(double radians)
  {
    creaseAngle = radians;
  } // End of NormalGenerator(double)



  /**
   * Constructor.  Construct a NormalGenerator object with creaseAngle
   * set to 44 degrees (0.767944871 radians).
   */
  public NormalGenerator()
  {
    this(44.0 * Math.PI / 180.0);
  } // End of NormalGenerator()
} // End of class NormalGenerator

// End of file NormalGenerator.java
