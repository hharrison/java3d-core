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

// ----------------------------------------------------------------------
//
// The reference to Fast Industrial Strength Triangulation (FIST) code
// in this release by Sun Microsystems is related to Sun's rewrite of
// an early version of FIST. FIST was originally created by Martin
// Held and Joseph Mitchell at Stony Brook University and is
// incorporated by Sun under an agreement with The Research Foundation
// of SUNY (RFSUNY). The current version of FIST is available for
// commercial use under a license agreement with RFSUNY on behalf of
// the authors and Stony Brook University.  Please contact the Office
// of Technology Licensing at Stony Brook, phone 631-632-9009, for
// licensing information.
//
// ----------------------------------------------------------------------

package com.sun.j3d.utils.geometry;

import com.sun.j3d.utils.geometry.GeometryInfo;
import java.util.LinkedList;
import java.util.ArrayList;
import com.sun.j3d.internal.J3dUtilsI18N;

/**
 * The Stripifier utility will change the primitive of the GeometryInfo
 * object to Triangle Strips.  The strips are made by analyzing the
 * triangles in the original data and connecting them together.<p>
 * <p>
 * Normal Generation should be performed on the GeometryInfo object
 * <i>before</i> Stripification, for best results.  Example:<p>
 * <p>
 * <pre>
 *   GeometryInfo gi = new GeometryInfo(TRIANGLE_ARRAY);
 *   gi.setCoordinates(coordinateData);
 *
 *   NormalGenerator ng = new NormalGenerator();
 *   ng.generateNormals(gi);
 *
 *   Stripifier st = new Stripifier()
 *   st.stripify(gi);
 *
 *   Shape3D part = new Shape3D();
 *   part.setAppearance(appearance);
 *   part.setGeometry(gi.getGeometryArray());
 *   </pre>
 */
public class Stripifier {

    final boolean DEBUG = false;
    final boolean CHECK_ORIENT = false;

    static final int EMPTY = -1;

    boolean hasNormals = false;
    boolean hasTextures = false;
    int texSetCount = 0;
    boolean hasColors = false;
    boolean colorStrips = false;

    StripifierStats stats;

    int[] numNhbrs;

    /**
     * Indicates to the stripifier to collect statistics on the data
     */
    public static final int COLLECT_STATS = 0x01;

    /**
     * Creates the Stripifier object.
     */
    public Stripifier() {
    }

    /**
     * Creates the Stripifier object.
     * @param flags Flags
     * @since Java 3D 1.2.1
     */
    public Stripifier(int flags) {
	if ((flags & COLLECT_STATS) != 0) {
	    stats = new StripifierStats();
	}
    }

    /**
     * Converts the geometry contained in the GeometryInfo object into an
     * array of triangle strips.
     */
    public void stripify(GeometryInfo gi) {
	//    	System.out.println("stripify");
	long time = System.currentTimeMillis();
	// setup
	gi.convertToIndexedTriangles();
	gi.forgetOldPrim();

	// write out the gi object
	//  	System.out.println("write out the object");
	//     	gi.writeObj();

	Face[] faces = createFaceArray(gi);
	Edge[] edges = createEdgeArray(faces);
	buildAdjacencies(edges, faces);

	// print out the adjacency information
 	if (DEBUG) {
 	    for (int i = 0; i < faces.length; i++) {
 		faces[i].printVertices();
 	    }
 	    System.out.println("");
 	    for (int i = 0; i < faces.length; i++) {
 		faces[i].printAdjacency();
 	    }
 	    System.out.println("");
 	}

	Node[] faceNodes = new Node[faces.length];
	// 	Node[] queue = hybridSearch(faces, faceNodes);
	Node[] queue = dfSearch(faces, faceNodes);

	// print out the queue
 	if (DEBUG) {
 	    for (int i = 0; i < queue.length; i++) {
 		queue[i].print();
 	    }
 	    System.out.println("");
 	}

	// int "pointers" for the numbers of strips and patches from
	// hamiliton
	int[] ns = new int[1];
	int[] np = new int[1];
	ArrayList hamiltons = hamilton(queue, ns, np);
	int numStrips = ns[0];
	int numPatches = np[0];

	// print out the hamiltonians
 	if (DEBUG) {
 	    for (int i = 0; i < hamiltons.size(); i++) {
 		System.out.println("Hamiltonian: " + i);
 		ArrayList list = (ArrayList)hamiltons.get(i);
 		for (int j = 0; j < list.size(); j++) {
 		    Face face = (Face)list.get(j);
 		    face.printVertices();
 		}
 		System.out.println("");
 	    }
 	}

	// now make strips out of the hamiltonians
	ArrayList strips = stripe(hamiltons);

	// print out the strips
	if (DEBUG) {
	    for (int i = 0; i < strips.size(); i++) {
		System.out.println("Strip: " + i);
		Istream istream = (Istream)strips.get(i);
		for (int j = 0; j < istream.length; j++) {
		    System.out.println("vertex: " + istream.istream[j].index);
		}
		System.out.println("");
	    }
	}

	// concatenate the strips
     	concatenate(strips, faces);

	// print out the new strips
	if (DEBUG) {
	    System.out.println("");
	    System.out.println("concatenated strips: (" +
			       (strips.size()) + ")");
	    System.out.println("");
	    for (int i = 0; i < strips.size(); i++) {
		System.out.println("Strip: " + i);
		Istream istream = (Istream)strips.get(i);
		for (int j = 0; j < istream.length; j++) {
		    System.out.println("vertex: " + istream.istream[j].index);
		}
		System.out.println("");
	    }
	}

	// put the stripified data into the GeometryInfo object
	putBackData(gi, strips);

	// 	System.out.println("time: " + (System.currentTimeMillis()-time));
	// 	System.out.println("");

	// add to stats
	if (stats != null) {
	    stats.updateInfo(System.currentTimeMillis()-time, strips,
			     faces.length);
	}

	// 	Stat.printInfo();

	// print out strip count info
	// 	System.out.println("numStrips = " + strips.size());
	// 	System.out.println("stripCounts:");
	// 	int avg = 0;
	// 	for (int i = 0; i < strips.size(); i++) {
	// 	    System.out.print(((Istream)strips.get(i)).length + " ");
	// 	    avg += ((Istream)strips.get(i)).length;
	// 	}
	// 	System.out.println("Avg: " + ((double)avg/(double)strips.size()));
    }

    /**
     * Prints out statistical information for the stripifier: the number of
     * original triangles, the number of original vertices, the number of
     * strips created, the number of vertices, the total number of triangles,
     * the minimum strip length (in # of tris) the maximum strip length
     * (in number of tris), the average strip length (in # of tris), the
     * average number of vertices per triangle, the total time it took to
     * stripify, and the strip length (how many strips of a given length.
     * The data is cumulative over all the times the stripifier is called
     * until the stats are printed, and then they are reset.
     */
    //     public static void printStats() {
    // // 	stats.toString();
    //     }

    /**
     * Returns the stripifier stats object.
     * @exception IllegalStateException if the Stripfier has not
     * been constructed
     * with the COLLECT_STATS flag
     * @since Java 3D 1.2.1
     */
    public StripifierStats getStripifierStats() {
	if (stats == null) {
	    throw new IllegalStateException(J3dUtilsI18N.getString("Stripifier0"));
	}
	return stats;
    }

    /**
     * Creates an array of faces from the geometry in the GeometryInfo object.
     */
    Face[] createFaceArray(GeometryInfo gi) {
	int[] vertices = gi.getCoordinateIndices();
	int[] normals = gi.getNormalIndices();

	int[][] textures = null;
	int[] t1 = null;
	int[] t2 = null;
	int[] t3 = null;
	texSetCount = gi.getTexCoordSetCount();
	if (texSetCount > 0) {
	    hasTextures = true;
	    textures = new int[texSetCount][];
	    for (int i = 0; i < texSetCount; i++) {
		textures[i] = gi.getTextureCoordinateIndices(i);
	    }
	    t1 = new int[texSetCount];
	    t2 = new int[texSetCount];
	    t3 = new int[texSetCount];
	} else hasTextures = false;

	int[] colors = gi.getColorIndices();
	Face[] faces = new Face[vertices.length/3];
	int n1, n2, n3, c1, c2, c3;
	Vertex v1, v2, v3;
	int count = 0;
	for (int i = 0; i < vertices.length;) {
	    if (normals != null) {
		// 		System.out.println("hasNormals");
		hasNormals = true;
		n1 = normals[i];
		n2 = normals[i+1];
		n3 = normals[i+2];
	    }
	    else {
		// 		System.out.println("doesn't have normals");
		hasNormals = false;
		n1 = EMPTY;
		n2 = EMPTY;
		n3 = EMPTY;
	    }
	    if (hasTextures) {
		for (int j = 0; j < texSetCount; j++) {
		    t1[j] = textures[j][i];
		    t2[j] = textures[j][(i+1)];
		    t3[j] = textures[j][(i+2)];
		}
	    }
	    if (colors != null) {
		hasColors = true;
		c1 = colors[i];
		c2 = colors[i+1];
		c3 = colors[i+2];
	    }
	    else {
		hasColors = false;
		c1 = EMPTY;
		c2 = EMPTY;
		c3 = EMPTY;
	    }
	    v1 = new Vertex(vertices[i], n1, texSetCount, t1, c1);
	    v2 = new Vertex(vertices[i+1], n2, texSetCount, t2, c2);
	    v3 = new Vertex(vertices[i+2], n3, texSetCount, t3, c3);
	    if (!v1.equals(v2) && !v2.equals(v3) && !v3.equals(v1)) {
		faces[count] = new Face(count, v1, v2, v3);
		count++;
	    }
	    i+=3;
	}

	if (faces.length > count) {
	    Face[] temp = faces;
	    faces = new Face[count];
	    System.arraycopy(temp, 0, faces, 0, count);
	}
	return faces;
    }

    /**
     * Creates an array of edges from the Face array.
     */
    Edge[] createEdgeArray(Face[] faces) {
	Edge[] edges = new Edge[faces.length*3];
	Face face;
	for (int i = 0; i < faces.length; i++) {
	    face = faces[i];
	    edges[i*3] = new Edge(face.verts[0], face.verts[1], face.key);
	    edges[i*3+1] = new Edge(face.verts[1], face.verts[2], face.key);
	    edges[i*3+2] = new Edge(face.verts[2], face.verts[0], face.key);
	}
	return edges;
    }

    /**
     * Builds the adjacency graph by finding the neighbors of the edges
     */
    void buildAdjacencies(Edge[] edges, Face[] faces) {
	// 	sortEdges(edges);
	quickSortEdges(edges, 0, edges.length-1);
	// 	int i = 1;

	// set up the edge list of each face
	Edge edge;
	Face face;
	Vertex[] verts;
	boolean flag;
	int k;
	for (int i = 0; i < edges.length; i++) {
	    // edges are kept in order s.t. the ith edge is the opposite
	    // edge of the ith vertex
	    edge = edges[i];
	    face = faces[edge.face];
	    verts = face.verts;

	    flag = true;
 	    if ((!verts[0].equals(edge.v1)) && (!verts[0].equals(edge.v2))) {
		face.edges[0] = edge;
		face.numNhbrs--;
		flag = false;
	    }
 	    else if ((!verts[1].equals(edge.v1)) &&
 		     (!verts[1].equals(edge.v2))) {
		face.edges[1] = edge;
		face.numNhbrs--;
		flag = false;
	    }
 	    else if ((!verts[2].equals(edge.v1)) &&
 		     (!verts[2].equals(edge.v2))) {
		face.edges[2] = edge;
		face.numNhbrs--;
		flag = false;
	    }
	    else {
		if (DEBUG) System.out.println("error!!!  Stripifier.buildAdj");
	    }

	    // handle degenerencies
	    if (flag) {
		Vertex i1;
		// triangle degenerated to a point
 		if ((edge.v1).equals(edge.v2)) {
		    face.edges[--face.numNhbrs] = edge;
		}
		// triangle degenerated to an edge
		else {
 		    if (verts[0].equals(verts[1])) {
			i1 = verts[1];
		    }
		    else {
			i1 = verts[2];
		    }
 		    if (verts[0].equals(i1) && face.edges[0] == null) {
			face.edges[0] = edge;
			face.numNhbrs--;
		    }
 		    else if (verts[1].equals(i1) && face.edges[1] == null) {
			face.edges[1] = edge;
			face.numNhbrs--;
		    }
		    else {
			face.edges[2] = edge;
			face.numNhbrs--;
		    }
		}
	    }
	}

	// build the adjacency information by pairing up every two triangles
	// that share the same edge
	int i = 0; int j = 0;
	int j1, j2;
	while (i < (edges.length-1)) {
	    j = i+1;
	    if (edges[i].equals(edges[j])) {
		// determine the orientations of the common edge in the two
		// adjacent triangles.  Only set them to be adjacent if they
		// are opposite
		j1 = edges[i].face;
		j2 = edges[j].face;
		if (j1 != j2) { // set up the two faces as neighbors
		    edge = edges[i];
		    face = faces[j1];
		    k = face.getEdgeIndex(edge);
		    if ((edge.v1.equals(face.verts[(k+1)%3])) &&
			(edge.v2.equals(face.verts[(k+2)%3]))) {
			flag = false;
		    }
		    else flag = true;

		    edge = edges[j];
		    face = faces[j2];
		    k = face.getEdgeIndex(edge);
		    if ((edge.v1.equals(face.verts[(k+1)%3])) &&
			(edge.v2.equals(face.verts[(k+2)%3]))) {
			flag = flag;
		    }
		    else flag = (!flag);

		    if (flag) {
			edges[i].face = j2;
			edges[j].face = j1;
			(faces[j1].numNhbrs)++;
			(faces[j2].numNhbrs)++;
			j++;
		    }
		    else edges[i].face = EMPTY;
		}
		else edges[i].face = EMPTY;
	    }
	    else edges[i].face = EMPTY;
	    i=j;
	}
	if (i <= (edges.length-1)) edges[i].face = EMPTY;

	// check, for each face, if it is duplicated.  For a face that
	// neighbors its duplicate in the adjacency graph, it's possible
	// that two or more of its neighbors are the same (the duplicate).
	// This will be corrected to avoid introducing redundant faces
	// later on

	for (i = 0; i < faces.length; i++) {
	    face = faces[i];
 	    if (face.numNhbrs == 3) {
		if ((j1 = face.edges[1].face) == face.edges[0].face) {
		    face.edges[1].face = EMPTY;
		    face.numNhbrs--;
		    faces[j1].counterEdgeDel(face.edges[1]);
		}
		if ((j2 = face.edges[2].face) == face.edges[0].face) {
		    face.edges[2].face = EMPTY;
		    face.numNhbrs--;
		    faces[j2].counterEdgeDel(face.edges[2]);
		}
 		if ((face.edges[1].face != EMPTY) && (j1 == j2)) {
		    face.edges[2].face = EMPTY;
		    face.numNhbrs--;
		    faces[j1].counterEdgeDel(face.edges[2]);
		}
	    }
	}
    }

    /**
     * Sorts the edges using BubbleSort
     */
    void sortEdges(Edge[] edges) {
	int i = edges.length;
	boolean sorted = false;
	Edge temp = null;
	while ((i > 1) && !sorted) {
	    sorted = true;
	    for (int j = 1; j < i; j++) {
		if (edges[j].lessThan(edges[j-1])) {
		    temp = edges[j-1];
		    edges[j-1] = edges[j];
		    edges[j] = temp;
		    sorted = false;
		}
	    }
	    i--;
	}
    }

    /**
     * uses quicksort to sort the edges
     */
    void quickSortEdges(Edge[] edges, int l, int r) {
	if (edges.length > 0) {
	    int i = l;
	    int j = r;
	    Edge k = edges[(l+r) / 2];

	    do {
		while (edges[i].lessThan(k)) i++;
		while (k.lessThan(edges[j])) j--;
		if (i <= j) {
		    Edge tmp = edges[i];
		    edges[i] = edges[j];
		    edges[j] = tmp;
		    i++;
		    j--;
		}
	    } while (i <= j);

	    if (l < j) quickSortEdges(edges, l, j);
	    if (l < r) quickSortEdges(edges, i, r);
	}
    }

    /**
     * Takes a list of faces as input and performs a hybrid search, a
     * variated depth first search that returns to the highest level node
     * not yet fully explored.  Returns an array of pointers to the faces
     * found in order from the search.  The faceNodes parameter is an
     * array of the Nodes created for the faces.
     */
    Node[] hybridSearch(Face[] faces, Node[] faceNodes) {

	int numFaces = faces.length;
	int i = 0, j = 0, k = 0, ind = 0;

	// keep # of faces with certain # of neighbors
	int[] count = {0, 0, 0, 0};

	// faces sorted by number of neighbors
	int[] index = new int[numFaces];
	// the index of a certain face in the sorted array
	int[] rindex = new int[numFaces];

	// Control list pop up operation
	boolean popFlag = false;

	// queue of pointers to faces found in search
	Node[] queue = new Node[numFaces];
	// root of depth first tree
	Node source;
	// for the next node
	Node nnode;
	// a face
	Face face;
	// starting position for insertion into the list
	int start = 0;
	// list for search
	SortedList dlist;

	// count how many faces have a certain # of neighbors and
	// create a Node for each face
	for (i = 0; i < numFaces; i++) {
	    j = faces[i].numNhbrs;
	    count[j]++;
	    faceNodes[i] = new Node(faces[i]);
	}

	// to help with sorting
	for (i = 1; i < 4; i++) {
	    count[i] += count[i-1];
	}

	// decreasing i to make sorting stable
	for (i = numFaces - 1; i >= 0; i--) {
	    j = faces[i].numNhbrs;
	    count[j]--;
	    index[count[j]] = i;
	    rindex[i] = count[j];
	}

	// start the hybrid search
	for (i = 0; i < numFaces; i++) {
	    if (index[i] != EMPTY) {
		dlist = new SortedList();
		source = faceNodes[index[i]];
		source.setRoot();
		queue[ind] = source;
		ind++;
		index[i] = EMPTY;

		while (source != null) {
		    nnode = null;
		    // use the first eligible for continuing search
		    face = source.face;
		    for (j = 0; j < 3; j++) {
			k = face.getNeighbor(j);
			if ((k != EMPTY) &&
			    (faceNodes[k].notAccessed())) {
			    nnode = faceNodes[k];
			    break;
			}
		    }

		    if (nnode != null) {
			// insert the new node
			nnode.insert(source);
			if (!popFlag) {
			    start = dlist.sortedInsert(source, start);
			}
			else popFlag = false;
			source = nnode;
			queue[ind] = source;
			ind++;
			index[rindex[k]] = EMPTY;
		    }
		    else {
			source.processed();
			source = dlist.pop();
			popFlag = true;
			start = 0;
		    }
		} // while -- does popFlag need to be set to false here?
	    }
	}
	return queue;
    }

    Node[] dfSearch(Face[] faces, Node[] faceNodes) {
	int numFaces = faces.length;
	int i = 0, j = 0, k = 0, ind = 0;

	// keep certain # of faces with certain # of neighbors
	int[] count = {0, 0, 0, 0};

	// faces sorted by # of neighbors
	int[] index = new int[numFaces];
	// index of a certain face in the sorted array
	int[] rindex = new int[numFaces];

	// queue of pointers to faces found in the search
	Node[] queue = new Node[numFaces];
	// root of the depth first tree
	Node source;
	// the current node
	Node node;
	// for the next Node
	Node nnode;
	// a face
	Face face;

	// count how many faces have a certain # of neighbors and create
	// a Node for each face
	for (i = 0; i < numFaces; i++) {
	    j = faces[i].numNhbrs;
	    count[j]++;
	    faceNodes[i] = new Node(faces[i]);
	}

	// to help with sorting
	for (i = 1; i < 4; i++) count[i] += count[i-1];

	// dec i to make sorting stable
	for (i = numFaces-1; i >= 0; i--) {
	    j = faces[i].numNhbrs;
	    count[j]--;
	    index[count[j]] = i;
	    rindex[i] = count[j];
	}

	setNumNhbrs(faces);
	// start the dfs
	for (i = 0; i < numFaces; i++) {
	    if (index[i] != EMPTY) {
		source = faceNodes[index[i]];
		source.setRoot();
		queue[ind] = source;
		ind++;
		index[i] = EMPTY;
		node = source;

		do {
		    // if source has been done, stop
		    if ((node == source) && (node.right != null)) break;

		    nnode = null;
 		    face = node.face;

		    //  		    for (j = 0; j < 3; j++) {
		    //  			if (((k = face.getNeighbor(j)) != EMPTY) &&
		    //  			    (faceNodes[k].notAccessed())) {
		    //  			    nnode = faceNodes[k];
		    //  			    break;
		    //  			}
		    //  		    }

 		    k = findNext(node, faceNodes, faces);
 		    if (k != EMPTY) nnode = faceNodes[k];
 		    if (nnode != null) updateNumNhbrs(nnode);

		    if (nnode != null) {
			// insert new node
			nnode.insert(node);
			node = nnode;
			queue[ind] = node;
			ind++;
			index[rindex[k]] = EMPTY;
		    }
		    else {
			node.processed();
			node = node.parent;
		    }
		} while (node != source.parent);
	    }
	}
	freeNhbrTable();
	return queue;
    }

    int findNext(Node node, Node[] faceNodes, Face[] faces) {
	Face face = node.face;
	// this face has no neighbors so return
	if (face.numNhbrs == 0) return EMPTY;

	int i, j, count;
	int[] n = new int[3];  // num neighbors of neighboring face
	int[] ind = {-1, -1, -1}; // neighboring faces

	// find the number of neighbors for each neighbor
	count = 0;
	for (i = 0; i < 3; i++) {
	    if (((j = face.getNeighbor(i)) != EMPTY) &&
		(faceNodes[j].notAccessed())) {
		ind[count] = j;
		n[count] = numNhbrs[j];
		count++;
	    }
	}

	// this face has no not accessed faces
	if (count == 0) return EMPTY;

	// this face has only one neighbor
	if (count == 1) return ind[0];

	if (count == 2) {
	    // if the number of neighbors are the same, try reseting
	    if ((n[0] == n[1]) && (n[0] != 0)) {
		n[0] = resetNhbr(ind[0], faces, faceNodes);
		n[1] = resetNhbr(ind[1], faces, faceNodes);
	    }
	    // if one neighbor has fewer neighbors, return that neighbor
	    if (n[0] < n[1]) return ind[0];
	    if (n[1] < n[0]) return ind[1];
	    // neighbors tie.  pick the sequential one
	    Node pnode, ppnode;
	    Face pface, ppface;
	    if ((pnode = node.parent) != null) {
		pface = pnode.face;
		i = pface.findSharedEdge(face.key);
		if ((ppnode = pnode.parent) != null) {
		    ppface = ppnode.face;
		    if (pface.getNeighbor((i+1)%3) == ppface.key) {
			j = pface.verts[(i+2)%3].index;
		    }
		    else {
			j = pface.verts[(i+1)%3].index;
		    }
		}
		else {
		    j = pface.verts[(i+1)%3].index;
		}
		i = face.findSharedEdge(ind[0]);
		if (face.verts[i].index == j) return ind[0];
		else return ind[1];
	    }
	    else return ind[0];
	}
	// three neighbors
	else {
	    if ((n[0] < n[1]) && (n[0] < n[2])) return ind[0];
	    else if ((n[1] < n[0]) && (n[1] < n[2])) return ind[1];
	    else if ((n[2] < n[0]) && (n[2] < n[1])) return ind[2];
	    else if ((n[0] == n[1]) && (n[0] < n[2])) {
		if (n[0] != 0) {
		    n[0] = resetNhbr(ind[0], faces, faceNodes);
		    n[1] = resetNhbr(ind[1], faces, faceNodes);
		}
		if (n[0] <= n[1]) return ind[0];
		else return ind[1];
	    }
	    else if ((n[1] == n[2]) && n[1] < n[0]) {
		if (n[1] != 0) {
		    n[1] = resetNhbr(ind[1], faces, faceNodes);
		    n[2] = resetNhbr(ind[2], faces, faceNodes);
		}
		if (n[1] <= n[2]) return ind[1];
		else return ind[2];
	    }
	    else if ((n[2] == n[0]) && (n[2] < n[1])) {
		if (n[0] != 0) {
		    n[0] = resetNhbr(ind[0], faces, faceNodes);
		    n[2] = resetNhbr(ind[2], faces, faceNodes);
		}
		if (n[0] <= n[2]) return ind[0];
		else return ind[2];
	    }
	    else {
		if (n[0] != 0) {
		    n[0] = resetNhbr(ind[0], faces, faceNodes);
		    n[1] = resetNhbr(ind[1], faces, faceNodes);
		    n[2] = resetNhbr(ind[2], faces, faceNodes);
		}
		if ((n[0] <= n[1]) && (n[0] <= n[2])) return ind[0];
		else if (n[1] <= n[2]) return ind[1];
		else return ind[2];
	    }
	}
    }

    void setNumNhbrs(Face[] faces) {
	int numFaces = faces.length;
	numNhbrs = new int[numFaces];
	for (int i = 0; i < numFaces; i++) {
	    numNhbrs[i] = faces[i].numNhbrs;
	}
    }

    void freeNhbrTable() {
	numNhbrs = null;
    }

    void updateNumNhbrs(Node node) {
	Face face = node.face;
	int i;
	if ((i = face.getNeighbor(0)) != EMPTY) numNhbrs[i]--;
	if ((i = face.getNeighbor(1)) != EMPTY) numNhbrs[i]--;
	if ((i = face.getNeighbor(2)) != EMPTY) numNhbrs[i]--;
    }

    int resetNhbr(int y, Face[] faces, Node[] faceNodes) {
	int x = EMPTY;
	Face nface = faces[y];
	int i;
	for (int j = 0; j < 3; j++) {
	    if (((i = nface.getNeighbor(j)) != EMPTY) &&
		(faceNodes[i].notAccessed())) {
		if ((x == EMPTY) || (x > numNhbrs[i])) x = numNhbrs[i];
	    }
	}
	return x;
    }

    /**
     * generates hamiltonian strips from the derived binary spanning tree
     * using the path peeling algorithm to peel off any node wiht double
     * children in a bottom up fashion.  Returns a Vector of strips.  Also
     * return the number of strips and patches in the numStrips and
     * numPatches "pointers"
     */
    ArrayList hamilton(Node[] sTree, int[] numStrips, int[] numPatches) {
	// the number of nodes in the tree
	int numNodes = sTree.length;
	// number of strips
	int ns = 0;
	// number of patches
	int np = 0;
	// some tree node variables
	Node node, pnode, cnode;
	// the Vector of strips
	ArrayList strips = new ArrayList();
	// the current strip
	ArrayList currStrip;

	// the tree nodes are visited in such a bottom-up fashion that
	// any node is visited prior to its parent
	for (int i = numNodes - 1; i >= 0; i--) {
	    cnode = sTree[i];

	    // if cnode is the root of a tree create a strip
	    if (cnode.isRoot()) {
		// each patch is a single tree
		np++;
		// create a new strip
		currStrip = new ArrayList();
		// insert the current node into the list
		currStrip.add(0, cnode.face);

		// add the left "wing" of the parent node to the strip
		node = cnode.left;
		while (node != null) {
		    currStrip.add(0, node.face);
		    node = node.left;
		}

		// add the right "wing" of the parent node to the strip
		node = cnode.right;
		while (node != null) {
		    currStrip.add(currStrip.size(), node.face);
		    node = node.left;
		}

		// increase the number of strips
		ns++;
		// add the strip to the Vector
		strips.add(currStrip);
	    }

	    // if the number of children of this node is 2, create a strip
	    else if (cnode.numChildren == 2) {
		// if the root has a single child with double children, it
		// could be left over as a singleton.  However, the following
		// rearrangement reduces the chances
  		pnode = cnode.parent;
  		if (pnode.isRoot() && (pnode.numChildren == 1)) {
  		    pnode = cnode.right;
  		    if (pnode.left != null) cnode = pnode;
  		    else cnode = cnode.left;
  		}

		// handle non-root case

 		// remove the node
 		cnode.remove();

		// create a new strip
		currStrip = new ArrayList();
		// insert the current node into the list
		currStrip.add(0, cnode.face);

		// add the left "wing" of cnode to the list
		node = cnode.left;
		while (node != null) {
		    currStrip.add(0, node.face);
		    node = node.left;
		}

		// add the right "wing" of cnode to the list
		node = cnode.right;
		while (node != null) {
		    currStrip.add(currStrip.size(), node.face);
		    node = node.left;
		}

		// increase the number of strips
		ns++;
		// add the strip to the Vector
		strips.add(currStrip);
	    }
	}

	// put the ns and np in the "pointers to return
	numStrips[0] = ns;
	numPatches[0] = np;

	// return the strips
	return strips;
    }

    /**
     * creates the triangle strips
     */
    ArrayList stripe(ArrayList strips) {
	int numStrips = strips.size();  // the number of strips
	int count;  // where we are in the hamiltonian
	Face face;  // the face we are adding to the stream
	Face prev;  // the previous face added to the stream
	boolean done;  // whether we are done with the current strip
	boolean cont;  // whether we should continue the current stream
	ArrayList currStrip;  // the current hamiltonian
	Istream currStream;  // the stream we are building
	ArrayList istreams = new ArrayList();  // the istreams to return
	boolean ccw = true;;  // counter-clockwise
	int share;  // the shared edge
	Vertex[] buf = new Vertex[4]; // a vertex array to start the stream

	// create streams for each hamiltonian
	for (int i = 0; i < numStrips; i++) {
	    currStrip = (ArrayList)strips.get(i);
	    count = 0;
	    done = false;
	    face = getNextFace(currStrip, count++);

	    // while we are not done with the current hamiltonian
	    while (!done) {
		cont = true;

		// if the current face is the only one left in the current
		// hamiltonian
		if (stripDone(currStrip, count)) {
		    // create a new istream with the current face
		    currStream = new Istream(face.verts, 3, false);
		    // set the head of the strip to this face
		    currStream.head = face.key;
		    done = true;
		    // since we are done with the strip, set the tail to this
		    // face
		    currStream.tail = face.key;
		}

		else {
		    prev = face;
		    face = getNextFace(currStrip, count++);

		    // put the prev vertices in the correct order
		    // to add the next tri on
		    share = prev.findSharedEdge(face.key);
		    buf[0] = prev.verts[share];
		    buf[1] = prev.verts[(share+1)%3];
		    buf[2] = prev.verts[(share+2)%3];

		    // find the fourth vertex
		    if (CHECK_ORIENT) {
			// check for clockwise orientation
			if (checkOrientCWSeq(buf[2], buf[1], face)) {
			    share = face.findSharedEdge(prev.key);
			    buf[3] = face.verts[share];
			    currStream = new Istream(buf, 4, false);
			    // set the head of this strip to the prev face
			    currStream.head = prev.key;
			    // if this was the last tri in the strip, then
			    // we are done
			    if (stripDone(currStrip, count)) {
				done = true;
				// set the tail for the strip to current face
				currStream.tail = face.key;
			    }
			}
			else {
			    cont = false;
			    currStream = new Istream(buf, 3, false);
			    // set the head to the prev face
			    currStream.head = prev.key;
			    // since we are not continuing, set
			    // the tail to prev also
			    currStream.tail = prev.key;
			}

			// orientation starts counter-clockwise for 3rd face
			ccw = true;
		    }
		    else {
			share = face.findSharedEdge(prev.key);
			buf[3] = face.verts[share];
			currStream = new Istream(buf, 4, false);
			// set the head of this strip to the prev face
			currStream.head = prev.key;
			// if this was the last tri in the strip, then
			// we are done
			if (stripDone(currStrip, count)) {
			    done = true;
			    // set the tail for the strip to current face
			    currStream.tail = face.key;
			}
		    }

		    // while continue and the strip isn't finished
		    // add more faces to the stream
		    while (cont && !stripDone(currStrip, count)) {
			prev = face;
			face = getNextFace(currStrip, count++);
			share = face.findSharedEdge(prev.key);

			// if we can add the face without adding any
			// zero area triangles
			if (seq(currStream, face, share)) {
			    if (CHECK_ORIENT) {
				// if we can add the next face with the correct
				// orientation
				if (orientSeq(ccw, currStream, face)) {
				    // append the vertex opposite the
				    //shared edge
				    currStream.append(face.verts[share]);
				    // next face must have opposite orientation
				    ccw = (!ccw);
				    // if this was the last tri in the
				    //strip, then we are done
				    if (stripDone(currStrip, count)) {
					done = true;
					// since we are done with this strip,
					// set the tail to the current face
					currStream.tail = face.key;
				    }
				}
				// if we cannot add the face with the correct
				// orientation, do not continue with this
				// stream
				else {
				    cont = false;
				    // since we cannot continue with this strip
				    // set the tail to prev
				    currStream.tail = prev.key;
				}
			    }
			    else {
				// append the vertex opposite the
				//shared edge
				currStream.append(face.verts[share]);
				// if this was the last tri in the
				//strip, then we are done
				if (stripDone(currStrip, count)) {
				    done = true;
				    // since we are done with this strip,
				    // set the tail to the current face
				    currStream.tail = face.key;
				}
			    }
			}

			// need zero area tris to add continue the strip
			else {
			    if (CHECK_ORIENT) {
				// check the orientation for adding a zero
				// area tri and this face
				if (orientZAT(ccw, currStream, face)) {
				    // swap the end of the current stream to
				    // add a zero area triangle
				    currStream.swapEnd();
				    // append the vertex opposite the
				    // shared edge
				    currStream.append(face.verts[share]);
				    // if this was the last tri in the
				    // strip then we are done
				    if (stripDone(currStrip, count)) {
					done = true;
					// set the tail because we are done
					currStream.tail = face.key;
				    }
				}
				// if we cannot add the face with the correct
				// orientation, do not continue with this
				// stream
				else {
				    cont = false;
				    // since we cannot continue with this face,
				    // set the tail to the prev face
				    currStream.tail = prev.key;
				}
			    }
			    else {
				// swap the end of the current stream to
				// add a zero area triangle
				currStream.swapEnd();
				// append the vertex opposite the
				// shared edge
				currStream.append(face.verts[share]);
				// if this was the last tri in the
				// strip then we are done
				if (stripDone(currStrip, count)) {
				    done = true;
				    // set the tail because we are done
				    currStream.tail = face.key;
				}
			    }
			}
		    } // while (cont && !stripDone)
		} // else

		// add the current strip to the strips to be returned
		istreams.add(currStream);
	    } // while !done
	} // for each hamiltonian
	return istreams;
    } // stripe

    boolean stripDone(ArrayList strip, int count) {
	if (count < strip.size()) {
	    return false;
	}
	else return true;
    }

    boolean seq(Istream stream, Face face, int share) {
	int length = stream.length;
	Vertex v1 = face.edges[share].v1;
	Vertex v2 = face.edges[share].v2;
	Vertex last = stream.istream[length-1];
	Vertex prev = stream.istream[length-2];
	if (((v1.equals(prev)) && (v2.equals(last))) ||
	    ((v1.equals(last)) && (v2.equals(prev)))) {
	    return true;
	}
	else return false;
    }

    boolean orientSeq(boolean ccw, Istream stream, Face face) {
	int length = stream.length;
	Vertex last = stream.istream[length-1];
	Vertex prev = stream.istream[length-2];
	if ((ccw && checkOrientCCWSeq(last, prev, face)) ||
	    ((!ccw) && checkOrientCWSeq(last, prev, face))) {
	    return true;
	}
	else return false;
    }

    boolean orientZAT(boolean ccw, Istream stream, Face face) {
	int length = stream.length;
	Vertex last = stream.istream[length-1];
	Vertex swap = stream.istream[length-3];
	if ((ccw && checkOrientCWSeq(last, swap, face)) ||
	    ((!ccw) && checkOrientCCWSeq(last, swap, face))) {
	    return true;
	}
	else return false;
    }

    boolean checkOrientCWSeq(Vertex last, Vertex prev, Face face) {
	System.out.println("checkOrientCWSeq");
	System.out.println("last = " + last.index);
	System.out.println("prev = " + prev.index);
	System.out.print("face = ");
	face.printVertices();
	if (last.equals(face.verts[0])) {
	    if (!prev.equals(face.verts[1])) {
		if (DEBUG) System.out.println("ORIENTATION PROBLEM!");
		return false;
	    }
	}
	else if (last.equals(face.verts[1])) {
	    if (!prev.equals(face.verts[2])) {
		if (DEBUG) System.out.println("ORIENTATION PROBLEM!");
		return false;
	    }
	}
	else if (last.equals(face.verts[2])) {
	    if (!prev.equals(face.verts[0])) {
		if (DEBUG) System.out.println("ORIENTATION PROBLEM!");
		return false;
	    }
	}
	return true;
    }

    boolean checkOrientCCWSeq(Vertex last, Vertex prev, Face face) {
	System.out.println("checkOrientCCWSeq");
	System.out.println("last = " + last.index);
	System.out.println("prev = " + prev.index);
	System.out.print("face = ");
	face.printVertices();
	if (prev.equals(face.verts[0])) {
	    if (!last.equals(face.verts[1])) {
		System.out.println("ORIENTATION PROBLEM!");
		return false;
	    }
	}
	else if (prev.equals(face.verts[1])) {
	    if (!last.equals(face.verts[2])) {
		System.out.println("ORIENTATION PROBLEM!");
		return false;
	    }
	}
	else if (prev.equals(face.verts[2])) {
	    if (!last.equals(face.verts[0])) {
		System.out.println("ORIENTATION PROBLEM!");
		return false;
	    }
	}
	return true;
    }

    Face getNextFace(ArrayList currStrip, int index) {
	if (currStrip.size() > index) return (Face)currStrip.get(index);
	else return null;
    }

    /**
     * joins tristrips if their end triangles neighbor each other.  The
     * priority is performed in three stages: strips are concatenated to
     * save 2, 1, or no vertices
     */
    void concatenate(ArrayList strips, Face[] faces) {
	int numFaces = faces.length;
	int[] faceTable = new int[numFaces];
	Istream strm;

	// initialize the face table to empty
	for (int i = 0; i < numFaces; i++) {
	    faceTable[i] = EMPTY;
	}

	// set up the faceTable so that a face index relates to a strip
	// that owns the face as one of its end faces
	for (int i = 0; i < strips.size(); i++) {
	    strm = (Istream)strips.get(i);
	    faceTable[strm.head] = i;
	    faceTable[strm.tail] = i;
	}

	if (DEBUG) {
	    System.out.println("");
	    System.out.println("faceTable:");
	    for (int i = 0; i < faceTable.length; i++) {
		System.out.println(faceTable[i]);
	    }
	    System.out.println("");
	}

 	reduceCostByTwo(strips, faces, faceTable);
   	reduceCostByOne(strips, faces, faceTable);
   	reduceCostByZero(strips, faces, faceTable);
    }

    /**
     * find all the links that reduce the cost by 2
     */
    void reduceCostByTwo(ArrayList strips, Face[] faces, int[] faceTable) {
	//  	System.out.println("reduceCostByTwo");
	// number of faces in the face array
	int numFaces = faces.length;
	// possible adjacent strips
	int id, id1, id2;
	// Istreams
	Istream strm, strm1;
	// the length of the Istrem
	int len, len1;
	// vertex sequences for tristrips
	Vertex[] seq, seq1;
	// a face
	Face face;
	// the list of vertices for the face
	Vertex[] verts;
	// used to syncronize the orientation
	boolean sync, sync1;
	// a swap variable
	Vertex swap;

	for (int i = 0; i < numFaces; i++) {
	    id = faceTable[i];
	    if (id != EMPTY) {
		sync = false; sync1 = false;
		strm = (Istream)strips.get(id);
		len = strm.length;
		seq = strm.istream;
		face = faces[i];
		verts = face.verts;

		// sequential strips
		if (!strm.fan) {

		    // a singleton strip
		    if (len == 3) {

			// check all three neighbors
			for (int j = 0; j < 3; j++) {
			    int k = face.getNeighbor(j);
			    if ((k != EMPTY) &&
				((id1 = faceTable[k]) != EMPTY) &&
				(id1 != id)) {
				// reassign the sequence
				seq[0] = verts[j];
				seq[1] = verts[(j+1)%3];
				seq[2] = verts[(j+2)%3];

				// the neighboring stream
				strm1 = (Istream)strips.get(id1);
				len1 = strm1.length;
				if (k != strm1.head) {
				    strm1.invert();
				    // if the length is odd set sync1 to true
				    if ((len1 % 2) != 0) sync1 = true;
				}
				seq1 = strm1.istream;

				// append a singleton strip
				if (len1 == 3) {
				    //   				    System.out.println("reduce2");
				    int m = faces[k].findSharedEdge(i);
				    strm.append(faces[k].verts[m]);
				    strm1.length = 0;
				    strm1.istream = null;
				    strm.tail = k;
				    faceTable[k] = id;
				    i--;
				    break;
				}

				// append a strip of length over 2
				else {
				    if ((len1 == 4) &&
					(seq[1].index == seq1[0].index) &&
					(seq[2].index == seq1[2].index)) {

					// swap seq1[1] and seq1[2] so that
					// seq[1] == seq1[0] and
					// seq[1] == seq1[1]
					swap = seq1[1];
					seq1[1] = seq1[2];
					seq1[2] = swap;
				    }

				    // see if we can join the strips
				    if ((seq[1].index == seq1[0].index) &&
					(seq[2].index == seq1[1].index)) {
					//   					System.out.println("reduce2");
					// add the stream in
					strm.addStream(strm1);
					faceTable[k] = EMPTY;
					faceTable[strm.tail] = id;

					i--;
					break;
				    }
				    else if (sync1) {
					strm1.invert();
					sync1 = false;
				    }
				}
			    }
			}
		    }

		    // not a singleton strip

		    // can append a stream where the current face is the tail
		    // or is an even length so we can invert it
		    else if ((i == strm.tail) || ((len % 2) == 0)) {
			// if the current face isn't the tail, then
			// have to invert the strip
			if (i != strm.tail) {
			    strm.invert();
			    seq = strm.istream;
			}

			// 			System.out.println("seq.length = " + seq.length);
			// 			System.out.println("len = " + len);
			// 			System.out.print("seq = ");
			// 			for (int l = 0; l < seq.length; l++) {
			// 			    if (seq[l] == null) System.out.print(" null");
			// 			    else System.out.print(" " + seq[l].index);
			// 			}
			// 			System.out.println("");

			swap = seq[len - 3];

			// find the neighboring strip
			int m = EMPTY;
			if (verts[0].index == swap.index) m = 0;
			else if (verts[1].index == swap.index) m = 1;
			else if (verts[2].index == swap.index) m = 2;
			if (m == EMPTY) {
			    if (DEBUG) System.out.println("problem finding neighbor strip");
			}
			int j = face.getNeighbor(m);
			if (j == EMPTY) id1 = j;
			else id1 = faceTable[j];
			if ((id1 != EMPTY) &&
			    (((Istream)strips.get(id1)).fan !=
			     strm.fan)) {
			    id1 = EMPTY;
			}

			if ((id1 != EMPTY) && (id1 != id)) {
			    strm1 = (Istream)strips.get(id1);
			    len1 = strm1.length;

			    // if the shared face isn't the head, invert
			    // the stream
			    if (j != strm1.head) {
				strm1.invert();
				// set the sync var if the length is odd
				if ((len1 % 2) != 0) sync1 = true;
			    }
			    seq1 = strm1.istream;

			    // append a singleton strip
			    if (len1 == 3) {
				//   				System.out.println("reduce2");
				m = faces[j].findSharedEdge(i);
				strm.append(faces[j].verts[m]);
				strm1.length = 0;
				strm1.istream = null;
				strm.tail = j;
				faceTable[i] = EMPTY;
				faceTable[j] = id;
			    }

			    // append a non-singleton strip
			    else {
				if ((len1 == 4) &&
				    (seq[len-2].index == seq1[0].index) &&
				    (seq[len-1].index == seq1[2].index)) {

				    // swap seq1[1] and seq1[2] so that
				    // seq[len-2] == seq1[0] and
				    // seq[len-1] == seq1[1]
				    swap = seq1[1];
				    seq1[1] = seq1[2];
				    seq1[2] = swap;
				}

				// see if we can append the strip
				if ((seq[len-2].index == seq1[0].index) &&
				    (seq[len-1].index == seq1[1].index)) {
				    //   				    System.out.println("reduce2");
				    strm.addStream(strm1);
				    faceTable[i] = EMPTY;
				    faceTable[strm.tail] = id;
				    faceTable[j] = EMPTY;
				}
				else if (sync1) strm1.invert();
			    }
			}
		    }
		}
	    }
	}
    }

    /**
     * find all links that reduce cost by 1
     */
    void reduceCostByOne(ArrayList strips, Face[] faces, int[] faceTable) {
	//  	System.out.println("reduceCostByOne");
	// number of faces in the face array
	int numFaces = faces.length;
	// possible adjacent strips
	int id, id1, id2;
	// Istreams
	Istream strm, strm1;
	// the length of the Istream
	int len, len1;
	// vertex sequences for tristrips
	Vertex[] seq, seq1;
	// a face
	Face face;
	// the list of vertices for the face
	Vertex[] verts;
	// used to synchronize the orientation
	boolean sync, sync1;
	// a swap variable
	Vertex swap;

	for (int i = 0; i < numFaces; i++) {
	    id = faceTable[i];
	    if ((id != EMPTY) && !((Istream)strips.get(id)).fan) {
		sync = false;
		strm = (Istream)strips.get(id);
		seq = strm.istream;
		face = faces[i];
		verts = face.verts;
		len = strm.length;

		// a singleton strip
		if (len == 3) {

		    // consider the three neighboring triangles
		    for (int j = 0; j < 3; j++) {
			int k = face.getNeighbor(j);
			if ((k != EMPTY) &&
			    ((id1 = faceTable[k]) != EMPTY) &&
			    (id1 != id) &&
			    (!((Istream)strips.get(id1)).fan)) {

			    // reassign the sequence
			    seq[0] = verts[j];
			    seq[1] = verts[(j+1)%3];
			    seq[2] = verts[(j+2)%3];

			    // the neighboring stream
			    strm1 = (Istream)strips.get(id1);
			    len1 = strm1.length;
			    if (k != strm1.head) {
				strm1.invert();
				if ((len1 % 2) != 0) sync = true;
			    }
			    seq1 = strm1.istream;

			    // see if we can join the strips

			    if ((len1 == 4) &&
				(((seq[1].index == seq1[2].index) &&
				  (seq[2].index == seq1[0].index)) ||
				 ((seq[1].index == seq1[0].index) &&
				  (seq[2].index == seq1[2].index)))) {
				swap = seq1[1];
				seq1[1] = seq1[2];
				seq1[2] = swap;
			    }

			    if ((seq[1].index == seq1[0].index) &&
				(seq[2].index == seq1[1].index)) {
				//  				System.out.println("reduce1");
				strm.addStream(strm1);
				faceTable[k] = EMPTY;
				faceTable[strm.tail] = id;
				i--;
				break;
			    }

			    if ((seq[1].index == seq1[1].index) &&
				(seq[2].index == seq1[0].index)) {
				//   				System.out.println("reduce1");
				strm.append(seq1[1]);
				strm.addStream(strm1);
				faceTable[k] = EMPTY;
				faceTable[strm.tail] = id;
				i--;
				break;
			    }

			    if ((seq[1].index == seq1[0].index) &&
				(seq[2].index == seq1[2].index)) {
				//   				System.out.println("reduce1");
				seq1[0] = seq1[2];
				strm.append(seq1[1]);
				strm.addStream(strm1);
				faceTable[k] = EMPTY;
				faceTable[strm.tail] = id;
				i--;
				break;
			    }

			    if (sync) {
				strm1.invert();
				sync = false;
			    }
			}
		    }
		}

		// non-singleton strip
		else if ((i == strm.tail) || ((len % 2) == 0)) {

		    // make sure the face i ends the id-th strip
		    if (i != strm.tail) {
			strm.invert();
			seq = strm.istream;
		    }

		    swap = seq[len-3];

		    // find the neighboring strip
		    int m = EMPTY;
		    if (verts[0].index == swap.index) m = 0;
		    else if (verts[1].index == swap.index) m = 1;
		    else if (verts[2].index == swap.index) m = 2;
		    if (m == EMPTY) {
			if (DEBUG) System.out.println("problem finding neighbor strip");
		    }
		    int j = face.getNeighbor(m);
		    if (j == EMPTY) id1 = j;
		    else id1 = faceTable[j];
		    if ((id1 != EMPTY) &&
			(((Istream)strips.get(id1)).fan != strm.fan)) {
			id1 = EMPTY;
		    }

		    // find another neighboring strip
		    swap = seq[len-2];
		    m = EMPTY;
		    if (verts[0].index == swap.index) m = 0;
		    else if (verts[1].index == swap.index) m = 1;
		    else if (verts[2].index == swap.index) m = 2;
		    if (m == EMPTY) {
			if (DEBUG) System.out.println("problem finding neighbor strip.");
		    }
		    int k = face.getNeighbor(m);
		    if (k == EMPTY) id2 = k;
		    else id2 = faceTable[k];
		    if ((id2 != EMPTY) &&
			(((Istream)strips.get(id2)).fan != strm.fan)) {
			id2 = EMPTY;
		    }

		    // consider strip id1
		    boolean success = false;
		    if ((id1 != EMPTY) && (id1 != id)) {
			strm1 = (Istream)strips.get(id1);
			len1 = strm1.length;
			if (j != strm1.head) {
			    strm1.invert();
			    if ((len1 % 2) != 0) sync = true;
			}
			seq1 = strm1.istream;

			if ((len1 == 4) &&
			    (((seq[len-2].index == seq1[2].index) &&
			      (seq[len-1].index == seq1[0].index)) ||
			     (seq[len-2].index == seq1[0].index) &&
			     (seq[len-1].index == seq1[2].index))) {
			    swap = seq1[1];
			    seq1[1] = seq1[2];
			    seq1[2] = swap;
			}

			// find matches
			if ((seq[len-2].index == seq1[0].index) &&
			    (seq[len-1].index == seq1[1].index)) {
			    //   			    System.out.println("reduce1");
			    strm.addStream(strm1);
			    faceTable[i] = EMPTY;
			    faceTable[strm.tail] = id;
			    faceTable[j] = EMPTY;
			    success = true;
			}

			else if ((seq[len-2].index == seq1[1].index) &&
				 (seq[len-1].index == seq1[0].index)) {
			    //   			    System.out.println("reduce1");
			    strm.append(seq1[1]);
			    strm.addStream(strm1);
			    faceTable[i] = EMPTY;
			    faceTable[strm.tail] = id;
			    faceTable[j] = EMPTY;
			    success = true;
			}

			else if ((seq[len-2].index == seq1[0].index) &&
				 (seq[len-1].index == seq1[2].index)) {
			    //   			    System.out.println("reduce1");
			    seq1[0] = seq1[2];
			    strm.append(seq1[1]);
			    strm.addStream(strm1);
			    faceTable[i] = EMPTY;
			    faceTable[strm.tail] = id;
			    faceTable[j] = EMPTY;
			    success = true;
			}
			else if (sync) {
			    strm1.invert();
			    sync = false;
			}
		    }

		    // now consider strip id2
		    if (!success &&
			(id2 != EMPTY) && (id2 != id)) {
			strm1 = (Istream)strips.get(id2);
			len1 = strm1.length;
			if (k != strm1.head) {
			    strm1.invert();
			    if ((len1 % 2) != 0) sync = true;
			}
			seq1 = strm1.istream;

			if ((len1 == 4) &&
			    (seq[len-3].index == seq1[0].index) &&
			    (seq[len-1].index == seq1[2].index)) {
			    swap = seq1[1];
			    seq1[1] = seq1[2];
			    seq1[2] = swap;
			}

			// find matches

			if ((seq[len-3].index == seq1[0].index) &&
			    (seq[len-1].index == seq1[1].index)) {
			    //   			    System.out.println("reduce1");
			    strm.swapEnd();
			    strm.addStream(strm1);
			    faceTable[i] = EMPTY;
			    faceTable[strm.tail] = id;
			    faceTable[k] = EMPTY;
			    success = true;
			}
			if (!success && sync) strm1.invert();
		    }
		}
	    }
	}
    }

    /**
     * find all the links that reduce the cost by 0
     */
    void reduceCostByZero(ArrayList strips, Face[] faces, int[] faceTable) {
	//  	System.out.println("reduceCostByZero");
	// number of faces in the face array
	int numFaces = faces.length;
	// possible adjacent strips
	int id, id1, id2;
	// Istreams
	Istream strm, strm1;
	// the length of the Istream
	int len, len1;
	// vertex sequences for tristrips
	Vertex[] seq, seq1;
	// a face
	Face face;
	// the list of vertices for the face
	Vertex[] verts;
	// used to synchronize the orientation
	boolean sync, sync1;
	// a swap variable
	Vertex swap;

	for (int i = 0; i < numFaces; i++) {
	    id = faceTable[i];
	    if ((id != EMPTY) && !((Istream)strips.get(id)).fan) {
		sync = false;
		strm = (Istream)strips.get(id);
		seq = strm.istream;
		len = strm.length;
		face = faces[i];
		verts = face.verts;

		if (len == 3) {
		    for (int j = 0; j < 3; j++) {
			int k = face.getNeighbor(j);
			if ((k != EMPTY) && ((id1 = faceTable[k]) != EMPTY) &&
			    (id1 != id) &&
			    !((Istream)strips.get(id1)).fan) {
			    // reassign the sequence
			    seq[0] = verts[j];
			    seq[1] = verts[(j+1)%3];
			    seq[2] = verts[(j+2)%3];

			    // the neighboring stream
			    strm1 = (Istream)strips.get(id1);
			    len1 = strm1.length;
			    if (k != strm1.head) {
				strm1.invert();
				if ((len1 % 2) != 0) sync = true;
			    }
			    seq1 = strm1.istream;

			    // see if we can join the strips
			    if ((seq[1].index == seq1[2].index) &&
				(seq[2].index == seq1[0].index)) {
				//   				System.out.println("reduce0");
				seq1[0] = seq1[2];
				strm.append(seq1[0]);
				strm.append(seq1[1]);
				strm.addStream(strm1);
				faceTable[k] = EMPTY;
				faceTable[strm.tail] = id;
				i--;
				break;
			    }
			    else if (sync) {
				strm1.invert();
				sync = false;
			    }
			}
		    }
		}
		else if ((i == strm.tail) || ((len % 2) == 0)) {
		    if (i != strm.tail) {
			strm.invert();
			seq = strm.istream;
		    }

		    swap = seq[len-3];

		    // find neighboring strip
		    int m = EMPTY;
		    if (verts[0].index == swap.index) m = 0;
		    else if (verts[1].index == swap.index) m = 1;
		    else if (verts[2].index == swap.index) m = 2;
		    if (m == EMPTY) {
			if (DEBUG) System.out.println("problem finding neighbor strip");
		    }
		    int j = face.getNeighbor(m);
		    if (j == EMPTY) id1 = j;
		    else id1 = faceTable[j];
		    if ((id1 != EMPTY) &&
			(((Istream)strips.get(id1)).fan != strm.fan)) {
			id1 = EMPTY;
		    }

		    // find another neighboring strip
		    swap = seq[len-2];
		    m = EMPTY;
		    if (verts[0].index == swap.index) m = 0;
		    else if (verts[1].index == swap.index) m = 1;
		    else if (verts[2].index == swap.index) m = 2;
		    if (m == EMPTY) {
			if (DEBUG) System.out.println("problem finding neighbor strip.");
		    }
		    int k = face.getNeighbor(m);
		    if (k == EMPTY) id2 = k;
		    else id2 = faceTable[k];
		    if ((id2 != EMPTY) &&
			(((Istream)strips.get(id2)).fan != strm.fan)) {
			id2 = EMPTY;
		    }

		    // consider strip id1
		    boolean success = false;
		    if ((id1 != EMPTY) && (id1 != id)) {
			strm1 = (Istream)strips.get(id1);
			len1 = strm1.length;
			if (j != strm1.head) {
			    strm1.invert();
			    if ((len1 % 2) != 0) sync = true;
			}
			seq1 = strm1.istream;

			// find matches
			if ((seq[len-2].index == seq1[2].index) &&
			    (seq[len-1].index == seq1[0].index)) {
			    //   			    System.out.println("reduce0");
			    seq1[0] = seq1[2];
			    strm.append(seq1[0]);
			    strm.append(seq1[1]);
			    strm.addStream(strm1);
			    faceTable[i] = EMPTY;
			    faceTable[strm.tail] = id;
			    faceTable[j] = EMPTY;
			    success = true;
			}
			else if (sync) {
			    strm1.invert();
			    sync = false;
			}
		    }

		    // consider strip id2
		    if (!success && (id2 != EMPTY) && (id2 != id)) {
			strm1 = (Istream)strips.get(id2);
			len1 = strm1.length;
			if (k != strm1.head) {
			    strm1.invert();
			    if ((len1 % 2) != 0) sync = true;
			}
			seq1 = strm1.istream;

			if ((len1 == 4) &&
			    (((seq[len-3].index == seq1[2].index) &&
			      (seq[len-1].index == seq1[0].index)) ||
			     ((seq[len-3].index == seq1[0].index) &&
			      (seq[len-1].index == seq1[2].index)))) {

			    swap = seq1[1];
			    seq1[1] = seq1[2];
			    seq1[2] = swap;
			}

			// find matches
			if ((seq[len-3].index == seq1[1].index) &&
			    (seq[len-1].index == seq1[0].index)) {
			    //   			    System.out.println("reduce0");
			    strm.swapEnd();
			    strm.append(seq1[1]);
			    strm.addStream(strm1);
			    faceTable[i] = EMPTY;
			    faceTable[strm.tail] = id;
			    faceTable[k] = EMPTY;
			}
			else if ((seq[len-3].index == seq1[0].index) &&
				 (seq[len-1].index == seq1[2].index)) {
			    //   			    System.out.println("reduce0");
			    seq1[0] = seq1[2];
			    strm.swapEnd();
			    strm.append(seq1[1]);
			    strm.addStream(strm1);
			    faceTable[i] = EMPTY;
			    faceTable[strm.tail] = id;
			    faceTable[k] = EMPTY;
			}
			else if ((seq[len-3].index == seq1[0].index) &&
				 (seq[len-1].index == seq1[1].index)) {
			    //   			    System.out.println("reduce0");
			    strm.swapEnd();
			    strm.addStream(strm1);
			    faceTable[i] = EMPTY;
			    faceTable[strm.tail] = id;
			    faceTable[k] = EMPTY;
			}
			else if (sync) strm1.invert();
		    }
		}
	    }
	}
    }

    /**
     * puts the stripified data back into the GeometryInfo object
     */
    void putBackData(GeometryInfo gi, ArrayList strips) {
	int[] tempStripCounts = new int[strips.size()];
	int ciSize = 0;
	int stripLength;
	for (int i = 0; i < strips.size();) {
	    stripLength = ((Istream)strips.get(i)).length;
 	    if (stripLength != 0) {
		tempStripCounts[i] = stripLength;
		ciSize += stripLength;
		i++;
	    }
	    else {
		strips.remove(i);
	    }
	}
	if (ciSize > 3) {
	    gi.setPrimitive(gi.TRIANGLE_STRIP_ARRAY);
	    int[] stripCounts = new int[strips.size()];
	    System.arraycopy(tempStripCounts, 0, stripCounts, 0, strips.size());
	    gi.setStripCounts(stripCounts);

	    // create one array with all the strips
	    int[] coords = new int[ciSize];

	    // create arrays for normals, textures and colors if necessary
	    int[] normals = null;
	    int[][] textures = null;
	    int[] colors = null;
	    javax.vecmath.Color3b[] stripColors = null;
	    if (hasNormals) normals = new int[ciSize];
	    if (hasTextures) {
		textures = new int[texSetCount][ciSize];
	    }
	    if (hasColors) colors = new int[ciSize];
	    if (colorStrips) {
		stripColors = new javax.vecmath.Color3b[ciSize];
		colors = new int[ciSize];
	    }
	    int count = 0;
	    Istream currStrip;
	    for (int i = 0; i < strips.size(); i++) {
		currStrip = (Istream)strips.get(i);

		if (currStrip.length < 3) {
		    throw new RuntimeException("currStrip.length = " +
					       currStrip.length);
		}

		java.awt.Color stripColor = null;
		if (colorStrips) {
		    int r = ((int)(Math.random()*1000))%255;
		    int g = ((int)(Math.random()*1000))%255;
		    int b = ((int)(Math.random()*1000))%255;
		    stripColor = new java.awt.Color(r, g, b);
		}

		for (int j = 0; j < currStrip.length; j++) {
		    coords[count] = currStrip.istream[j].index;
		    if (hasNormals) normals[count] = currStrip.istream[j].normal;
		    if (hasTextures) {
			for (int k = 0; k < texSetCount; k++) {
			    textures[k][count] =
				currStrip.istream[j].texture[k];
			}
		    }
		    if (hasColors) colors[count] = currStrip.istream[j].color;
		    if (colorStrips) stripColors[count] =
					 new javax.vecmath.Color3b(stripColor);
		    count++;
		}
	    }
	    gi.setCoordinateIndices(coords);
	    if (hasNormals) gi.setNormalIndices(normals);
	    if (hasTextures) {
		for (int i = 0; i < texSetCount; i++) {
		    gi.setTextureCoordinateIndices(i, textures[i]);
		}
	    }
	    if (hasColors) gi.setColorIndices(colors);
	    if (colorStrips) {
		gi.setColors(stripColors);
		colors =  gi.getListIndices(stripColors);
		gi.setColorIndices(colors);
	    }
	}
    }

    /**
     * Stores the infomration about a vertex
     */
    class Vertex {

	int index;
	int normal = EMPTY;
	int numTexSets = 0;
	int[] texture = null;
	int color = EMPTY;

	Vertex(int vertIndex) {
	    this(vertIndex, EMPTY, 0, null, EMPTY);
	}

	Vertex(int vertIndex, int vertNormal,
	       int vertNumTexSets, int[] vertTexture, int vertColor) {
	    index = vertIndex;
	    normal = vertNormal;
	    numTexSets = vertNumTexSets;
	    if (numTexSets > 0) {
		texture = new int[numTexSets];
		System.arraycopy(vertTexture, 0, texture, 0, numTexSets);
	    }
	    color = vertColor;
	}

	boolean equals(Vertex v) {
	    for (int i = 0; i < numTexSets; i++) {
		if (texture[i] != v.texture[i]) {
		    return false;
		}
	    }
	    return ((v.index == index) &&
		    (v.normal == normal) &&
		    (v.color == color));
	}

	// will this yield same results as c code ???
	boolean lessThan(Vertex v) {
	    if (index < v.index) return true;
	    if (index > v.index) return false;
	    if (normal < v.normal) return true;
	    if (normal > v.normal) return false;
	    for (int i = 0; i < numTexSets; i++) {
		if (texture[i] < v.texture[i]) return true;
		if (texture[i] > v.texture[i]) return false;
	    }
	    if (color < v.color) return true;
	    if (color > v.color) return false;
	    return false;
	}
    }

    /**
     * Stores the information about an edge of a triangle
     */
    class Edge {

	Vertex v1, v2;
	int face;

	Edge(Vertex vertex1, Vertex vertex2, int faceIndex) {
	    face = faceIndex;

	    // this could be causing wrapping problem
   	    if (vertex1.lessThan(vertex2)) {
  		v1 = vertex1;
 		v2 = vertex2;
   	    } else {
   		v1 = vertex2;
   		v2 = vertex1;
   	    }
	}

	/**
	 * Determine whether the edges have the same vertices
	 */
	boolean equals(Edge edge) {
 	    return ((v1.equals(edge.v1)) && (v2.equals(edge.v2)));

	}

	/**
	 * Used to sort the edges.  If this is less than the edge parameter,
	 * return true.  First check if vertex1 is less than vertex1 of the
	 * edge provided.  If so, return true.  If the first vertices are equal
	 * then check vertex2.
	 */
	boolean lessThan(Edge edge) {
 	    if (v1.lessThan(edge.v1)) return true;
 	    else if (v1.equals(edge.v1)) return (v2.lessThan(edge.v2));
 	    else return false;
	}
    }

    /**
     * Stores the information about the face of a triangle
     */
    class Face {
	int key;
	int numNhbrs = 0;
	Vertex[] verts = null;
	// edges are kept in order s.t. the ith edge is the opposite
	// edge of the ith vertex
	Edge[] edges = null;

	/**
	 * Creates a new Face with the three given vertices
	 */
	Face(int index, Vertex v1, Vertex v2, Vertex v3) {
	    key = index;

	    verts = new Vertex[3];
	    verts[0] = v1;
	    verts[1] = v2;
	    verts[2] = v3;

	    edges = new Edge[3];
	    edges[0] = null;
	    edges[1] = null;
	    edges[2] = null;
	    numNhbrs = 3;
	}

	/**
	 * returns the index of the face that neighbors the edge supplied
	 * by the parameter
	 */
	int getNeighbor(int edge) {
	    return edges[edge].face;
	}

	/**
	 * returns the index of the edge that is shared by the triangle
	 * specified by the key parameter
	 */
	int findSharedEdge(int key) {
 	    if (edges[0].face == key) return 0;
	    else if (edges[1].face == key) return 1;
	    else if (edges[2].face == key) return 2;
	    else return -1; /* error */
	}

	int getEdgeIndex(Edge edge) {
	    if (edges[0].equals(edge)) return 0;
	    else if (edges[1].equals(edge)) return 1;
	    else return 2;
	}

	void counterEdgeDel(Edge edge) {
	    if (DEBUG) {
		System.out.println("counterEdgeDel");
	    }
	    if ((edges[0]).equals(edge)) {
		edges[0].face = EMPTY;
		numNhbrs--;
	    }
	    else if ((edges[1]).equals(edge)) {
		edges[1].face = EMPTY;
		numNhbrs--;
	    }
	    else if ((edges[2]).equals(edge)) {
		edges[2].face = EMPTY;
		numNhbrs--;
	    }
	    else {
		if (DEBUG) {
		    System.out.println("error in counterEdgeDel");
		}
	    }
	}

	void printAdjacency() {
	    System.out.println("Face " + key + ": ");
	    System.out.println("\t numNhbrs = " + numNhbrs);
	    System.out.println("\t edge 0: Face " + edges[0].face);
	    System.out.println("\t edge 1: Face " + edges[1].face);
	    System.out.println("\t edge 2: Face " + edges[2].face);
	}

	void printVertices() {
	    System.out.println("Face " + key + ": (" + verts[0].index + ", " +
			       verts[1].index + ", " + verts[2].index + ")");
	}
    }

    /**
     * stores the information for a face node
     */
    class Node {
	Face face;        // the data: the face
	Node parent;      // the parent node
	Node left;        // the left child
	Node right;       // the right child
	int depth;        // the topological distance of the node from the root
	int numChildren;  // the number of children
	int attrib;       // characteristic of the node eg. color

	// the attributes - 3 states for the Node
	static final int WHITE = 0;  // not being accessed yet
	static final int GREY = 1;   // being accessed but not done yet
	static final int BLACK = 2;  // done

	Node(Face f) {
	    face = f;
	}

	/**
	 * inserts this node below the parent supplied.
	 */
	void insert(Node p) {
	    parent = p;
	    depth = p.depth + 1;
	    attrib = GREY;

	    if (parent.left == null) parent.left = this;
	    else parent.right = this;
	    (parent.numChildren)++;
	}

	/**
	 * remove this node from its parent
	 */
	void remove() {
	    if (parent != null) {
		if (parent.left == this) {
		    parent.left = parent.right;
		    parent.right = null;
		}
		else {
		    parent.right = null;
		}
		(parent.numChildren)--;
	    }
	}


	/**
	 * sets the depth to 0 and the attrib to GREY
	 */
	void setRoot() {
	    depth = 0;
	    attrib = GREY;
	}

	/**
	 * returns true if the attrib is WHITE
	 */
	boolean notAccessed() {
	    return (attrib == WHITE);
	}

	/**
	 * sets the color to BLACK
	 */
	void processed() {
	    attrib = BLACK;
	}

	/**
	 * a node is the root if it doesn't have a parent
	 */
	boolean isRoot() {
	    return (parent == null);
	}

	/**
	 * prints the information in this Node
	 */
	void print() {
	    System.out.println(this);
	    System.out.println("Node depth: " + depth);
	    face.printVertices();
	    System.out.print("parent: ");
	    if (parent != null) parent.face.printVertices();
	    else System.out.println("null");
	    System.out.print("left: ");
	    if (left != null) left.face.printVertices();
	    else System.out.println("null");
	    System.out.print("right: ");
	    if (right != null) right.face.printVertices();
	    else System.out.println("null");
	    System.out.println("attrib: " + attrib);
	    System.out.println("");
	}
    }

    /**
     * sorts the Nodes by depth
     */
    class SortedList {

	ArrayList list;

	/**
	 * create a new SortedList
	 */
	SortedList() {
	    list = new ArrayList();
	}

	/**
	 * insert into the list sorted by depth.  start looking at start
	 * to save some time.  Returns the index of the next item of the
	 * inserted element
	 */
	int sortedInsert(Node data, int start) {
	    // adjust start to where insert sorted
	    while ((start < list.size()) &&
		   (((Node)list.get(start)).depth <= data.depth)) {
		start++;
	    }

	    // insert at start index
	    list.add(start, data);

	    // return start+1 -- the index of the next element
	    return (start+1);
	}

	/**
	 * remove and return 1st element
	 */
	Node pop() {
	    if (!list.isEmpty()) return (Node)list.remove(0);
	    else return null;
	}
    }

    class Istream {

	// fan encoding
	boolean fan = false;
	// length of the strip
	int length = 0;
	// array that specifies triangle strip
	Vertex[] istream;
	// indices of the head and tail vertices
	int head, tail;

	/**
	 * creates a new Istream to store the triangle strip
	 */
	Istream(Vertex[] list, int size, boolean isFan) {
	    if (size == 0) throw new RuntimeException("size is 0");
	    fan = isFan;
	    length = size;
	    istream = new Vertex[length];
	    int i;
 	    System.arraycopy(list, 0, istream, 0, length);
	}

	/**
	 * adds a new vertex to the end of the stream
	 * makes the int array bigger, if necessary
	 */
	void append(Vertex vertex) {
	    growArray();
	    // add in new vertex
	    istream[length] = vertex;
	    length++;
	}

	/**
	 * turns the encoding (..., -3, -2, -1) into (.... -3, -2, -3, -1)
	 * so that zero-area triangle (-3, -2. -3) is added
	 */
	void swapEnd() {
	    growArray();
	    istream[length] = istream[length-1];
	    istream[length-1] = istream[length-3];
	    length++;
	}

	/**
	 * makes the array bigger, if necessary
	 */
	void growArray() {
	    if (length >= istream.length) {
		Vertex[] old = istream;
		// for now add enough space to add three more vertices
		// may change this later
		istream = new Vertex[length + 3];
		System.arraycopy(old, 0, istream, 0, length);
	    }
	}

	/**
	 * inverts the istream
	 */
	void invert() {
	    Vertex[] tmp = new Vertex[istream.length];
	    // reverse the stream
	    for (int i = 0; i < length; i++) {
		tmp[i] = istream[length - i - 1];
	    }
	    // copy it back
	    System.arraycopy(tmp, 0, istream, 0, istream.length);
	    tmp = null;
	    // swap the head and the tail
	    int swap = head;
	    head = tail;
	    tail = swap;
	}

	/**
	 * concats two streams into one big stream
	 */
	void addStream(Istream strm) {
	    //  	    System.out.println("addStream");
	    int strmLen = strm.length;
	    int size = strmLen + length - 2;

	    // make the istream bigger
	    if (size >= istream.length) {
		Vertex[] old = istream;
		istream = new Vertex[size];
		System.arraycopy(old, 0, istream, 0, length);
	    }

	    // add the strm to istream
	    System.arraycopy(strm.istream, 2, istream, length, strmLen-2);

	    tail = strm.tail;
	    length = size;
	    strm.length = 0;
	    strm.istream = null;
	}
    }
}
