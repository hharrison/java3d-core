/*
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
 */

package javax.media.j3d;


/**
 * This class provides static methods to support topological
 * transformations on generalized strips.  This is used by the
 * GeometryDecompressor.  These methods only need to look at the
 * vertex replacement flags to determine how the vertices in the strip
 * are connected.  The connections are rearranged in different ways to
 * transform generalized strips to GeometryArray representations.
 *
 * @see GeneralizedStripFlags
 * @see GeneralizedVertexList
 * @see GeometryDecompressor
 */
class GeneralizedStrip {
    private static final boolean debug = false ;

    // Private convenience copies of various constants.
    private static final int CW  =
	GeneralizedStripFlags.FRONTFACE_CW ;
    private static final int CCW =
	GeneralizedStripFlags.FRONTFACE_CCW ;
    private static final int RESTART_CW   =
	GeneralizedStripFlags.RESTART_CW ;
    private static final int RESTART_CCW  =
	GeneralizedStripFlags.RESTART_CCW ;
    private static final int REPLACE_MIDDLE =
	GeneralizedStripFlags.REPLACE_MIDDLE ;
    private static final int REPLACE_OLDEST =
	GeneralizedStripFlags.REPLACE_OLDEST ;

    /**
     * The IntList is like an ArrayList, but avoids the Integer
     * object wrapper and accessor overhead for simple lists of ints.
     */
    static class IntList {
	/**
	 * The array of ints.
	 */
	int ints[] ;

	/**
	 * The number of ints in this instance.
	 */
	int count ;

	/**
	 * Construct a new empty IntList of the given initial size.
	 * @param initialSize initial size of the backing array
	 */
	IntList(int initialSize) {
	    ints = new int[initialSize] ;
	    count = 0 ;
	}

	/**
	 * Constructs an IntList with the given contents.
	 * @param ints the array of ints to use as the contents
	 */
	IntList(int ints[]) {
	    this.ints = ints ;
	    this.count = ints.length ;
	}

	/**
	 * Add a new int to the end of this list.
	 * @param i the int to be appended to this list
	 */
	void add(int i) {
	    if (count == ints.length) {
		int newints[] = new int[2*count] ;
		System.arraycopy(ints, 0, newints, 0, count) ;
		ints = newints ;
		if (debug)
		    System.err.println
			("GeneralizedStrip.IntList: reallocated " +
			 (2*count) + " ints") ;
	    }
	    ints[count++] = i ;
	}

	/**
	 * Trim the backing array to the current count and return the
	 * resulting backing array.
	 */
	int[] trim() {
	    if (count != ints.length) {
		int newints[] = new int[count] ;
		System.arraycopy(ints, 0, newints, 0, count) ;
		ints = newints ;
	    }
	    return ints ;
	}

	/**
	 * Fill the list with consecutive integers starting from 0.
	 */
	void fillAscending() {
	    for (int i = 0 ; i < ints.length ; i++)
		ints[i] = i ;

	    count = ints.length ;
	}

	public String toString() {
	    String s = new String("[") ;
	    for (int i = 0 ; i < count-1 ; i++)
		s = s + Integer.toString(ints[i]) + ", " ;
	    return s + Integer.toString(ints[count-1]) + "]" ;
	}
    }

    /**
     * The StripArray class is used as the output of some conversion methods
     * in the GeneralizedStrip class.
     */
    static class StripArray {
	/**
	 * A list of indices into the vertices of the original generalized
	 * strip.  It specifies the order in which vertices in the original
	 * strip should be followed to build GeometryArray objects.
	 */
	IntList vertices ;

	/**
	 * A list of strip counts.
	 */
	IntList stripCounts ;

	/**
	 * Creates a StripArray with the specified vertices and stripCounts.
	 * @param vertices IntList containing vertex indicies.
	 * @param stripCounts IntList containing strip lengths.
	 */
	StripArray(IntList vertices, IntList stripCounts) {
	    this.vertices = vertices ;
	    this.stripCounts = stripCounts ;
	}
    }

    /**
     * Interprets the vertex flags associated with a class implementing
     * GeneralizedStripFlags, constructing and returning a 2-element array of
     * StripArray objects.  The first StripArray will contain triangle strips
     * and the second will contain triangle fans.
     *
     * @param vertices an object implementing GeneralizedStripFlags
     * @param frontFace a flag, either GeneralizedStripFlags.FRONTFACE_CW or
     * GeneralizedStripFlags.FRONTFACE_CCW, indicating front face winding
     * @return a 2-element array containing strips in 0 and fans in 1
     */
    static StripArray[] toStripsAndFans(GeneralizedStripFlags vertices,
					int frontFace) {

	int size = vertices.getFlagCount() ;

	// Initialize IntLists to worst-case sizes.
	IntList stripVerts  = new IntList(size*3) ;
	IntList fanVerts    = new IntList(size*3) ;
	IntList stripCounts = new IntList(size) ;
	IntList fanCounts   = new IntList(size) ;

	toStripsAndFans(vertices, frontFace,
			stripVerts, stripCounts, fanVerts, fanCounts) ;

	// Construct the StripArray output.
	StripArray sa[] = new StripArray[2] ;

	if (stripCounts.count > 0)
	    sa[0] = new StripArray(stripVerts, stripCounts) ;

	if (fanCounts.count > 0)
	    sa[1] = new StripArray(fanVerts, fanCounts) ;

	return sa ;
    }

    private static void toStripsAndFans(GeneralizedStripFlags vertices,
					int frontFace,
					IntList stripVerts,
					IntList stripCounts,
					IntList fanVerts,
					IntList fanCounts) {
	int newFlag, curFlag, winding ;
	int v, size, stripStart, stripLength ;
	boolean transition = false ;

	stripStart = 0 ;
	stripLength = 3 ;
	curFlag = vertices.getFlag(0) ;
	winding = (curFlag == RESTART_CW ? CW : CCW) ;
	size = vertices.getFlagCount() ;

	// Vertex replace flags for the first 3 vertices are irrelevant since
	// they can only define a single triangle.  The first meaningful
	// replace flag starts at the 4th vertex.
	v = 3 ;
	if (v < size)
	    curFlag = vertices.getFlag(v) ;

	while (v < size) {
	    newFlag = vertices.getFlag(v) ;

	    if ((newFlag == curFlag) &&
		(newFlag != RESTART_CW) && (newFlag != RESTART_CCW)) {
		// The last flag was the same as this one, and it wasn't a
		// restart: proceed to the next vertex.
		stripLength++ ;
		v++ ;

	    } else {
		// Either this vertex flag changed from the last one, or
		// the flag explicitly specifies a restart: process the
		// last strip and start up a new one.
		if (curFlag == REPLACE_MIDDLE)
		    addFan(fanVerts, fanCounts, stripStart, stripLength,
			   frontFace, winding, transition) ;
		else
		    addStrip(stripVerts, stripCounts, stripStart, stripLength,
			     frontFace, winding) ;

		// Restart: skip to the 4th vertex of the new strip.
		if ((newFlag == RESTART_CW) || (newFlag == RESTART_CCW)) {
		    winding = (newFlag == RESTART_CW ? CW : CCW) ;
		    stripStart = v ;
		    stripLength = 3 ;
		    v += 3 ;
		    transition = false ;
		    if (v < size)
			curFlag = vertices.getFlag(v) ;
		}
		// Strip/fan transition:  decrement start of strip.
		else {
		    if (newFlag == REPLACE_OLDEST) {
			// Flip winding order when transitioning from fans
			// to strips.
			winding = (winding == CW ? CCW : CW) ;
			stripStart = v-2 ;
			stripLength = 3 ;
		    } else {
			// Flip winding order when transitioning from
			// strips to fans only if the preceding strip has
			// an even number of vertices.
			if ((stripLength & 0x01) == 0)
			    winding = (winding == CW ? CCW : CW) ;
			stripStart = v-3 ;
			stripLength = 4 ;
		    }
		    v++ ;
		    transition = true ;
		    curFlag = newFlag ;
		}
	    }
	}

	// Finish off the last strip or fan.
	// If v > size then the strip is degenerate.
	if (v == size)
	    if (curFlag == REPLACE_MIDDLE)
		addFan(fanVerts, fanCounts, stripStart, stripLength,
		       frontFace, winding, transition) ;
	    else
		addStrip(stripVerts, stripCounts, stripStart, stripLength,
			 frontFace, winding) ;
	else
	    throw new IllegalArgumentException
		(J3dI18N.getString("GeneralizedStrip0")) ;

	if (debug) {
	    System.err.println("GeneralizedStrip.toStripsAndFans") ;
	    if (v > size)
		System.err.println(" ended with a degenerate triangle:" +
				   " number of vertices: " + (v-size)) ;

	    System.err.println("\n number of strips: " + stripCounts.count) ;
	    if (stripCounts.count > 0) {
		System.err.println(" number of vertices: " + stripVerts.count) ;
		System.err.println(" vertices/strip: " +
				   (float)stripVerts.count/stripCounts.count) ;
		System.err.println(" strip counts: " + stripCounts.toString()) ;
		// System.err.println(" indices: " + stripVerts.toString()) ;
	    }

	    System.err.println("\n number of fans: " + fanCounts.count) ;
	    if (fanCounts.count > 0) {
		System.err.println(" number of vertices: " + fanVerts.count) ;
		System.err.println(" vertices/strip: " +
				   (float)fanVerts.count/fanCounts.count) ;
		System.err.println(" fan counts: " + fanCounts.toString()) ;
		// System.err.println(" indices: " + fanVerts.toString()) ;
	    }
	    System.err.println("\n total vertices: " +
			       (stripVerts.count + fanVerts.count) +
			       "\n original number of vertices: " + size +
			       "\n") ;
	}
    }

    //
    // Java 3D specifies that the vertices of front-facing polygons
    // have counter-clockwise (CCW) winding order when projected to
    // the view surface. Polygons with clockwise (CW) vertex winding
    // will be culled as back-facing by default.
    //
    // Generalized triangle strips can flip the orientation of their
    // triangles with the RESTART_CW and RESTART_CCW vertex flags.
    // Strips flagged with an orientation opposite to what has been
    // specified as front-facing must have their windings reversed in
    // order to have the correct face orientation when represented as
    // GeometryArray objects.
    //
    private static void addStrip(IntList stripVerts,
				 IntList stripCounts,
				 int start, int length,
				 int frontFace, int winding) {
	int vindex = start ;

	if (winding == frontFace) {
	    // Maintain original order.
	    stripCounts.add(length) ;
	    while (vindex < start + length) {
		stripVerts.add(vindex++) ;
	    }
	} else if ((length & 0x1) == 1) {
	    // Reverse winding order if number of vertices is odd.
	    stripCounts.add(length) ;
	    vindex += length-1 ;
	    while (vindex >= start) {
		stripVerts.add(vindex--) ;
	    }
	} else if (length == 4) {
	    // Swap middle vertices.
	    stripCounts.add(4) ;
	    stripVerts.add(vindex) ;
	    stripVerts.add(vindex+2) ;
	    stripVerts.add(vindex+1) ;
	    stripVerts.add(vindex+3) ;
	} else {
	    // Make the 1st triangle a singleton with reverse winding.
	    stripCounts.add(3) ;
	    stripVerts.add(vindex) ;
	    stripVerts.add(vindex+2) ;
	    stripVerts.add(vindex+1) ;
	    if (length > 3) {
		// Copy the rest of the vertices in original order.
		vindex++ ;
		stripCounts.add(length-1) ;
		while (vindex < start + length) {
		    stripVerts.add(vindex++) ;
		}
	    }
	}
    }

    private static void addFan(IntList fanVerts,
			       IntList fanCounts,
			       int start, int length,
			       int frontFace, int winding,
			       boolean transition) {
	int vindex = start ;
	fanVerts.add(vindex++) ;

	if (winding == frontFace) {
	    if (transition) {
		// Skip 1st triangle if this is the result of a transition.
		fanCounts.add(length-1) ;
		vindex++ ;
	    } else {
		fanCounts.add(length) ;
		fanVerts.add(vindex++) ;
	    }
	    while (vindex < start + length) {
		fanVerts.add(vindex++) ;
	    }
	} else {
	    // Reverse winding order.
	    vindex += length-2 ;
	    while (vindex > start+1) {
		fanVerts.add(vindex--) ;
	    }
	    if (transition) {
		// Skip 1st triangle if this is the result of a transition.
		fanCounts.add(length-1) ;
	    } else {
		fanCounts.add(length) ;
		fanVerts.add(vindex) ;
	    }
	}
    }

    /**
     * Interprets the vertex flags associated with a class implementing
     * GeneralizedStripFlags, constructing and returning a StripArray containing
     * exclusively strips.
     *
     * @param vertices an object implementing GeneralizedStripFlags
     * @param frontFace a flag, either GeneralizedStripFlags.FRONTFACE_CW or
     * GeneralizedStripFlags.FRONTFACE_CCW, indicating front face winding
     * @return a StripArray containing the converted strips
     */
    static StripArray toTriangleStrips(GeneralizedStripFlags vertices,
				       int frontFace) {

	int size = vertices.getFlagCount() ;

	// initialize lists to worst-case sizes.
	IntList stripVerts  = new IntList(size*3) ;
	IntList fanVerts    = new IntList(size*3) ;
	IntList stripCounts = new IntList(size) ;
	IntList fanCounts   = new IntList(size) ;

	toStripsAndFans(vertices, frontFace,
			stripVerts, stripCounts, fanVerts, fanCounts) ;

	if (fanCounts.count == 0)
	    if (stripCounts.count > 0)
		return new StripArray(stripVerts, stripCounts) ;
	    else
		return null ;

	// convert each fan to one or more strips
	int i, v = 0 ;
	for (i = 0 ; i < fanCounts.count ; i++) {
	    fanToStrips(v, fanCounts.ints[i], fanVerts.ints,
			stripVerts, stripCounts, false) ;
	    v += fanCounts.ints[i] ;
	}

	// create the StripArray output
	StripArray sa = new StripArray(stripVerts, stripCounts) ;

	if (debug) {
	    System.err.println("GeneralizedStrip.toTriangleStrips" +
			       "\n number of strips: " +
			       sa.stripCounts.count) ;
	    if (sa.stripCounts.count > 0) {
		System.err.println(" number of vertices: " +
				   sa.vertices.count +
				   "\n vertices/strip: " +
				   ((float)sa.vertices.count /
				    (float)sa.stripCounts.count)) ;
		System.err.print(" strip counts: [") ;
		for (i = 0 ; i < sa.stripCounts.count-1 ; i++)
		    System.err.print(sa.stripCounts.ints[i] + ", ") ;
		System.err.println(sa.stripCounts.ints[i] + "]") ;
	    }
	    System.err.println() ;
	}
	return sa ;
    }

    private static void fanToStrips(int v, int length, int fans[],
				    IntList stripVerts,
				    IntList stripCounts,
				    boolean convexPlanar) {
	if (convexPlanar) {
	    // Construct a strip by criss-crossing across the interior.
	    stripCounts.add(length) ;
	    stripVerts.add(fans[v]) ;

	    int j = v + 1 ;
	    int k = v + (length - 1) ;
	    while (j <= k) {
		stripVerts.add(fans[j++]) ;
		if (j > k) break ;
		stripVerts.add(fans[k--]) ;
	    }
	} else {
	    // Traverse non-convex or non-planar fan, biting off 3-triangle
	    // strips or less.  First 5 vertices produce 1 strip of 3
	    // triangles, and every 4 vertices after that produce another
	    // strip of 3 triangles.  Each remaining strip adds 2 vertices.
	    int fanStart = v ;
	    v++ ;
	    while (v+4 <= fanStart + length) {
		stripVerts.add(fans[v]) ;
		stripVerts.add(fans[v+1]) ;
		stripVerts.add(fans[fanStart]) ;
		stripVerts.add(fans[v+2]) ;
		stripVerts.add(fans[v+3]) ;
		stripCounts.add(5) ;
		v += 3 ;
	    }

	    // Finish off the fan.
	    if (v+1 < fanStart + length) {
		stripVerts.add(fans[v]) ;
		stripVerts.add(fans[v+1]) ;
		stripVerts.add(fans[fanStart]) ;
		v++ ;

		if (v+1 < fanStart + length) {
		    stripVerts.add(fans[v+1]) ;
		    stripCounts.add(4) ;
		}
		else
		    stripCounts.add(3) ;
	    }
	}
    }

    /**
     * Interprets the vertex flags associated with a class implementing
     * GeneralizedStripFlags, constructing and returning an array of vertex
     * references representing the original generalized strip as individual
     * triangles.  Each sequence of three consecutive vertex references in the
     * output defines a single triangle.
     *
     * @param vertices an object implementing GeneralizedStripFlags
     * @param frontFace a flag, either GeneralizedStripFlags.FRONTFACE_CW or
     * GeneralizedStripFlags.FRONTFACE_CCW, indicating front face winding
     * @return an array of indices into the original vertex array
     */
    static int[] toTriangles(GeneralizedStripFlags vertices, int frontFace) {

	int vertexCount = 0 ;
	StripArray sa[] = toStripsAndFans(vertices, frontFace) ;

	if (sa[0] != null)
	    vertexCount  = 3 * getTriangleCount(sa[0].stripCounts) ;
	if (sa[1] != null)
	    vertexCount += 3 * getTriangleCount(sa[1].stripCounts) ;

	if (debug)
	    System.err.println("GeneralizedStrip.toTriangles\n" +
			       " number of triangles: " + vertexCount/3 + "\n" +
			       " number of vertices: " + vertexCount + "\n") ;
	int t = 0 ;
	int triangles[] = new int[vertexCount] ;

	if (sa[0] != null)
	    t = stripsToTriangles(t, triangles,
				  0, sa[0].vertices.ints,
				  0, sa[0].stripCounts.ints,
				  sa[0].stripCounts.count) ;
	if (sa[1] != null)
	    t = fansToTriangles(t, triangles,
				0, sa[1].vertices.ints,
				0, sa[1].stripCounts.ints,
				sa[1].stripCounts.count) ;
	return triangles ;
    }

    private static int stripsToTriangles(int tstart, int tbuff[],
					 int vstart, int vertices[],
					 int stripStart, int stripCounts[],
					 int stripCount) {
	int t = tstart ;
	int v = vstart ;
	for (int i = 0 ; i < stripCount ; i++) {
	    for (int j = 0 ; j < stripCounts[i+stripStart] - 2 ; j++) {
		if ((j & 0x01) == 0) {
		    // even-numbered triangles
		    tbuff[t*3 +0] = vertices[v+0] ;
		    tbuff[t*3 +1] = vertices[v+1] ;
		    tbuff[t*3 +2] = vertices[v+2] ;
		} else {
		    // odd-numbered triangles
		    tbuff[t*3 +0] = vertices[v+1] ;
		    tbuff[t*3 +1] = vertices[v+0] ;
		    tbuff[t*3 +2] = vertices[v+2] ;
		}
		t++ ; v++ ;
	    }
	    v += 2 ;
	}
	return t ;
    }

    private static int fansToTriangles(int tstart, int tbuff[],
				       int vstart, int vertices[],
				       int stripStart, int stripCounts[],
				       int stripCount) {
	int t = tstart ;
	int v = vstart ;
	for (int i = 0 ; i < stripCount ; i++) {
	    for (int j = 0 ; j < stripCounts[i+stripStart] - 2 ; j++) {
		tbuff[t*3 +0] = vertices[v] ;
		tbuff[t*3 +1] = vertices[v+j+1] ;
		tbuff[t*3 +2] = vertices[v+j+2] ;
		t++ ;
	    }
	    v += stripCounts[i+stripStart] ;
	}
	return t ;
    }

    /**
     * Interprets the vertex flags associated with a class implementing
     * GeneralizedStripFlags, constructing and returning a 2-element array of
     * StripArray objects.  The first StripArray will contain triangle strips
     * and the second will contain individual triangles in the vertices
     * field.  Short strips will be converted to individual triangles.
     *
     * @param vertices an object implementing GeneralizedStripFlags
     * @param frontFace a flag, either GeneralizedStripFlags.FRONTFACE_CW or
     * GeneralizedStripFlags.FRONTFACE_CCW, indicating front face winding
     * @param shortStripSize strips this size or less will be converted to
     * individual triangles if there are more than maxShortStrips of them
     * @param maxShortStrips maximum number of short strips allowed before
     * creating individual triangles
     * @return a 2-element array containing strips in 0 and triangles in 1
     */
    static StripArray[] toStripsAndTriangles(GeneralizedStripFlags vertices,
					     int frontFace, int shortStripSize,
					     int maxShortStrips) {
	int longStripCount = 0 ;
	int longStripVertexCount = 0 ;
	int shortStripCount = 0 ;
	int triangleCount = 0 ;

	StripArray sa[] = new StripArray[2] ;
	StripArray ts = toTriangleStrips(vertices, frontFace) ;

	for (int i = 0 ; i < ts.stripCounts.count ; i++)
	    if (ts.stripCounts.ints[i] <= shortStripSize) {
		shortStripCount++ ;
		triangleCount += ts.stripCounts.ints[i] - 2 ;
	    } else {
		longStripCount++ ;
		longStripVertexCount += ts.stripCounts.ints[i] ;
	    }

	if (debug)
	    System.err.print("GeneralizedStrip.toStripsAndTriangles\n" +
			     " short strip size: " + shortStripSize +
			     " short strips tolerated: " + maxShortStrips +
			     " number of short strips: " + shortStripCount +
			     "\n\n") ;

	if (shortStripCount <= maxShortStrips) {
	    sa[0] = ts ;
	    sa[1] = null ;
	} else {
	    int si = 0 ; int newStripVerts[] = new int[longStripVertexCount] ;
	    int ci = 0 ; int newStripCounts[] = new int[longStripCount] ;
	    int ti = 0 ; int triangles[] = new int[3*triangleCount] ;
	    int vi = 0 ;

	    for (int i = 0 ; i < ts.stripCounts.count ; i++) {
		if (ts.stripCounts.ints[i] <= shortStripSize) {
		    ti = stripsToTriangles(ti, triangles,
					   vi, ts.vertices.ints,
					    i, ts.stripCounts.ints, 1) ;
		    vi += ts.stripCounts.ints[i] ;
		} else {
		    newStripCounts[ci++] = ts.stripCounts.ints[i] ;
		    for (int j = 0 ; j < ts.stripCounts.ints[i] ; j++)
			newStripVerts[si++] = ts.vertices.ints[vi++] ;
		}
	    }

	    if (longStripCount > 0)
		sa[0] = new StripArray(new IntList(newStripVerts),
				       new IntList(newStripCounts)) ;
	    else
		sa[0] = null ;

	    sa[1] = new StripArray(new IntList(triangles), null) ;

	    if (debug) {
		System.err.println(" triangles separated: " + triangleCount) ;
		if (longStripCount > 0) {
		    System.err.println
			(" new vertices/strip: " +
			 ((float)longStripVertexCount/(float)longStripCount)) ;

		    System.err.print(" long strip counts: [") ;
		    for (int i = 0 ; i < longStripCount-1 ; i++)
			System.err.print(newStripCounts[i++] + ", ") ;

		    System.err.println
			(newStripCounts[longStripCount-1] + "]\n") ;
		}
	    }
	}
	return sa ;
    }

    /**
     * Interprets the vertex flags associated with a class implementing
     * GeneralizedStripFlags, constructing and returning a StripArray.
     *
     * RESTART_CW and RESTART_CCW are treated as equivalent, as are
     * REPLACE_MIDDLE and REPLACE_OLDEST.
     *
     * @param vertices an object implementing GeneralizedStripFlags
     * @return a StripArray representing an array of line strips
     */
     static StripArray toLineStrips(GeneralizedStripFlags vertices) {
	int v, size, stripStart, stripLength, flag ;

	stripStart = 0 ;
	stripLength = 2 ;
	size = vertices.getFlagCount() ;

	// Initialize IntLists to worst-case sizes.
	IntList stripVerts  = new IntList(size*2) ;
	IntList stripCounts = new IntList(size) ;

	// Vertex replace flags for the first two vertices are irrelevant.
	v = 2 ;
	while (v < size) {
	    flag = vertices.getFlag(v) ;

	    if ((flag != RESTART_CW) && (flag != RESTART_CCW)) {
		// proceed to the next vertex.
		stripLength++ ;
		v++ ;

	    } else {
		// Record the last strip.
		stripCounts.add(stripLength) ;
		for (int i = stripStart ; i < stripStart+stripLength ; i++)
		    stripVerts.add(i) ;

		// Start a new strip and skip to its 3rd vertex.
		stripStart = v ;
		stripLength = 2 ;
		v += 2 ;
	    }
	}

	// Finish off the last strip.
	// If v > size then the strip is degenerate.
	if (v == size) {
	    stripCounts.add(stripLength) ;
	    for (int i = stripStart ; i < stripStart+stripLength ; i++)
		stripVerts.add(i) ;
	} else
	    throw new IllegalArgumentException
		(J3dI18N.getString("GeneralizedStrip0")) ;

	if (debug) {
	    System.err.println("GeneralizedStrip.toLineStrips\n") ;
	    if (v > size)
		System.err.println(" ended with a degenerate line") ;

	    System.err.println(" number of strips: " + stripCounts.count) ;
	    if (stripCounts.count > 0) {
		System.err.println(" number of vertices: " + stripVerts.count) ;
		System.err.println(" vertices/strip: " +
				   (float)stripVerts.count/stripCounts.count) ;
		System.err.println(" strip counts: " + stripCounts.toString()) ;
		// System.err.println(" indices: " + stripVerts.toString()) ;
	    }
	    System.err.println() ;
	}

	if (stripCounts.count > 0)
	    return new StripArray(stripVerts, stripCounts) ;
	else
	    return null ;
    }

    /**
     * Counts the number of lines defined by arrays of line strips.
     *
     * @param stripCounts array of strip counts, as used by the
     * GeometryStripArray object
     * @return number of lines in the strips
     */
    static int getLineCount(int stripCounts[]) {
	int count = 0 ;
	for (int i = 0 ; i < stripCounts.length ; i++)
	    count += (stripCounts[i] - 1) ;
	return count ;
    }

    /**
     * Counts the number of triangles defined by arrays of
     * triangle strips or fans.
     *
     * @param stripCounts array of strip counts, as used by the
     * GeometryStripArray object
     * @return number of triangles in the strips or fans
     */
    static int getTriangleCount(int stripCounts[]) {
	int count = 0 ;
	for (int i = 0 ; i < stripCounts.length ; i++)
	    count += (stripCounts[i] - 2) ;
	return count ;
    }

    /**
     * Counts the number of triangles defined by arrays of
     * triangle strips or fans.
     *
     * @param stripCounts IntList of strip counts
     * @return number of triangles in the strips or fans
     */
    static int getTriangleCount(IntList stripCounts) {
	int count = 0 ;
	for (int i = 0 ; i < stripCounts.count ; i++)
	    count += (stripCounts.ints[i] - 2) ;
	return count ;
    }

    /**
     * Breaks up triangle strips into separate triangles.
     *
     * @param stripCounts array of strip counts, as used by the
     * GeometryStripArray object
     * @return array of ints which index into the original vertex array; each
     * set of three consecutive vertex indices defines a single triangle
     */
    static int[] stripsToTriangles(int stripCounts[]) {
	int triangleCount = getTriangleCount(stripCounts) ;
	int tbuff[] = new int[3*triangleCount] ;
	IntList vertices = new IntList(triangleCount + 2*stripCounts.length) ;

	vertices.fillAscending() ;
	stripsToTriangles(0, tbuff,
			  0, vertices.ints,
			  0, stripCounts,
			  stripCounts.length) ;
	return tbuff ;
    }

    /**
     * Breaks up triangle fans into separate triangles.
     *
     * @param stripCounts array of strip counts, as used by the
     * GeometryStripArray object
     * @return array of ints which index into the original vertex array; each
     * set of three consecutive vertex indices defines a single triangle
     */
    static int[] fansToTriangles(int stripCounts[]) {
	int triangleCount = getTriangleCount(stripCounts) ;
	int tbuff[] = new int[3*triangleCount] ;
	IntList vertices = new IntList(triangleCount + 2*stripCounts.length) ;

	vertices.fillAscending() ;
	fansToTriangles(0, tbuff,
			0, vertices.ints,
			0, stripCounts,
			stripCounts.length) ;
	return tbuff ;
    }

    /**
     * Takes a fan and converts it to one or more strips.
     *
     * @param v index into the fans array of the first vertex in the fan
     * @param length number of vertices in the fan
     * @param fans array of vertex indices representing one or more fans
     * @param convexPlanar if true indicates that the fan is convex and
     * planar; such fans will always be converted into a single strip
     * @return a StripArray containing the converted strips
     */
    static StripArray fanToStrips(int v, int length, int fans[],
				  boolean convexPlanar) {

	// Initialize IntLists to worst-case sizes.
	IntList stripVerts  = new IntList(length*3) ;
	IntList stripCounts = new IntList(length) ;

	fanToStrips(v, length, fans, stripVerts, stripCounts, convexPlanar) ;
	return new StripArray(stripVerts, stripCounts) ;
    }
}
