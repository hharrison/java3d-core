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

import java.util.ArrayList;

/**
 * This class collects statistics on the Stripifier.  The statistics
 * are cumulative over all calls to stripify() until clearData() is called.
 *
 * @since Java 3D 1.2.1
 */

public class StripifierStats {

    int numStrips = 0;
    int numVerts = 0;
    int minStripLen = 10000;
    int maxStripLen = 0;
    int totalTris = 0;
    int numFaces = 0;
    long time = 0;
    int[] counts = new int[14];

    boolean noData = true;

    /**
     * Returns the number of triangles in the original, un-stripified data.
     * @since Java 3D 1.2.1
     */
    public int getNumOrigTris() {
	return numFaces;
    }

    /**
     * Returns the number of vertices in the original, un-stripified data
     * @since Java 3D 1.2.1
     */
    public int getNumOrigVerts() {
	return (numFaces * 3);
    }

    /**
     * Returns the number of strips created by the stripifier.
     * @since Java 3D 1.2.1
     */
    public int getNumStrips() {
	return numStrips;
    }

    /**
     * Returns the number of vertices in the stripified data.
     * @since Java 3D 1.2.1
     */
    public int getNumVerts() {
	return numVerts;
    }

    /**
     * Returns the number of triangles in the stripified data.
     * @since Java 3D 1.2.1
     */
    public int getTotalTris() {
	return totalTris;
    }

    /**
     * Returns the length in triangles of the shortest strip
     * created by the stripifier.
     * @since Java 3D 1.2.1
     */
    public int getMinStripLength() {
	return minStripLen;
    }

    /**
     * Returns the length in triangles of the longest strip
     * created by the stripifier.
     * @since Java 3D 1.2.1
     */
    public int getMaxStripLength() {
	return maxStripLen;
    }

    /**
     * Return the average length of the strips created by the stripifier
     * @since Java 3D 1.2.1
     */
    public double getAvgStripLength() {
	return ((double)totalTris/(double)numStrips);
    }

    /**
     * Returns the average number of vertices per triangle in the stripified
     * data
     * @since Java 3D 1.2.1
     */
    public double getAvgNumVertsPerTri() {
	return ((double)numVerts/(double)totalTris);
    }

    /**
     * Returns the total time spent in the stripify() method
     * @since Java 3D 1.2.1
     */
    public long getTotalTime() {
	return time;
    }

    /**
     * Returns an array of length 14 that contains the number of strips of
     * a given length created by the stripifier.  Spots 0-8 of the array
     * represent lengths 1-9, 9 is lengths 10-19, 10 is lengths 20-49,
     * 11 is lengths 50-99, 12 is lengths 100-999 and 13 is lengths 1000
     * or more.
     * @since Java 3D 1.2.1
     */
    public int[] getStripLengthCounts() {
	return counts;
    }

    /**
     * Returns a formated String that can be used to print out
     * the Stripifier stats.
     * @since Java 3D 1.2.1
     */

    public String toString() {
	StringBuffer str = new StringBuffer(
					    "num orig tris:        " + numFaces + "\n" +
					    "num orig vertices:    " + (numFaces*3) + "\n" +
					    "number of strips:     " + numStrips + "\n" +
					    "number of vertices:   " + numVerts + "\n" +
					    "total tris:           " + totalTris + "\n" +
					    "min strip length:     " + minStripLen + "\n" +
					    "max strip length:     " + maxStripLen + "\n" +
					    "avg strip length:     " + ((double)totalTris/
									(double)numStrips) + "\n" +
					    "avg num verts/tri:    " + ((double)numVerts/
									(double)totalTris) + "\n" +
					    "total time:           " + time + "\n" +
					    "strip length distribution:\n");
	for (int i = 0; i < 9; i++){
	    str.append("  " + (i+1) + "=" + counts[i]);
	}
	str.append("  10-19=" + counts[9]);
	str.append("  20-49=" + counts[10]);
	str.append("  50-99=" + counts[11]);
	str.append("  100-999=" + counts[12]);
	str.append("  1000 or more=" + counts[13] + "\n");

	return str.toString();
    }

    /**
     * Clears the statistical data
     */
    public void clearData() {
	noData = true;

 	numStrips = 0;
 	numVerts = 0;
 	minStripLen = 10000;
 	maxStripLen = 0;
 	totalTris = 0;
 	numFaces = 0;
 	time = 0;
 	counts = new int[14];
    }

    void updateInfo(long ntime, ArrayList strips,
		    int nNumFaces) {
	noData = false;

	time += ntime;
	numStrips += strips.size();
	int nv = 0;
	int mnsl = 10000;
	int mxsl = 0;
	int tt = 0;
	for (int i = 0; i < strips.size(); i++) {
	    Stripifier.Istream strm = (Stripifier.Istream)strips.get(i);
	    int len = strm.length;
	    int trilen = (len-2);
	    nv += len;
	    if (trilen < mnsl) mnsl = trilen;
	    if (trilen > mxsl) mxsl = trilen;
	    tt += trilen;

	    // add to counts
	    // how many strips are length 1-9
	    if (trilen <= 9) counts[trilen-1] += 1;
	    // how many strips are length 10-19
	    else if (trilen < 20) counts[9] += 1;
	    // how many strips are length 20-49
	    else if (trilen < 50) counts[10] += 1;
	    // how many strips are length 50-99
	    else if (trilen < 100) counts[11] += 1;
	    // how many strips are length 100-1000
	    else if (trilen < 1000) counts[12] += 1;
	    // how many strips are length > 1000
	    else counts[13] += 1;
	}
	numVerts += nv;
	if (mnsl < minStripLen) minStripLen = mnsl;
	if (mxsl > maxStripLen) maxStripLen = mxsl;
	totalTris += tt;
	numFaces += nNumFaces;
    }

    void updateInfo(long ntime, int scLen, int sc[],
		    int nNumFaces) {

	noData = false;

	time += ntime;
	numStrips += scLen;
	int nv = 0;
	int mnsl = 10000;
	int mxsl = 0;
	int tt = 0;
	for (int i = 0; i < scLen; i++) {
	    int len = sc[i];
	    int trilen = (len-2);
	    numVerts += len;
	    if (trilen < mnsl) mnsl = trilen;
	    if (trilen > mxsl) mxsl = trilen;
	    totalTris += trilen;

	    // add to counts
	    // how many strips are length 1-9
	    if (trilen <= 9) counts[trilen-1] += 1;
	    // how many strips are length 10-19
	    else if (trilen < 20) counts[9] += 1;
	    // how many strips are length 20-49
	    else if (trilen < 50) counts[10] += 1;
	    // how many strips are length 50-99
	    else if (trilen < 100) counts[11] += 1;
	    // how many strips are length 100-1000
	    else if (trilen < 1000) counts[12] += 1;
	    // how many strips are length > 1000
	    else counts[13] += 1;
	}
	numVerts += nv;
	if (mnsl < minStripLen) minStripLen = mnsl;
	if (mxsl > maxStripLen) maxStripLen = mxsl;
	totalTris += tt;
	numFaces += nNumFaces;
    }

    //     void printInfo() {
    // 	System.out.println("num orig tris:        " + numFaces);
    // 	System.out.println("num orig vertices:    " + (numFaces*3));
    // 	System.out.println("number of strips:     " + numStrips);
    // 	System.out.println("number of vertices:   " + numVerts);
    // 	System.out.println("total tris:           " + totalTris);
    // 	System.out.println("min strip length:     " + minStripLen);
    // 	System.out.println("max strip length:     " + maxStripLen);
    // 	System.out.println("avg strip length:     " + ((double)totalTris/
    // 						       (double)numStrips));
    // 	System.out.println("avg num verts/tri:    " + ((double)numVerts/
    // 						       (double)totalTris));
    // 	System.out.println("total time:           " + time);
    // 	System.out.println("strip length distribution:");
    // 	for (int i = 0; i < 9; i++){
    // 	    System.out.print("  " + (i+1) + "=" + counts[i]);
    // 	}
    // 	System.out.print("  10-19=" + counts[9]);
    // 	System.out.print("  20-49=" + counts[10]);
    // 	System.out.print("  50-99=" + counts[11]);
    // 	System.out.print("  100-999=" + counts[12]);
    // 	System.out.println("  1000 or more=" + counts[13]);

    // 	// reset info after printing data
    // 	numStrips = 0;
    // 	numVerts = 0;
    // 	minStripLen = 10000;
    // 	maxStripLen = 0;
    // 	totalTris = 0;
    // 	numFaces = 0;
    // 	time = 0;
    // 	counts = new int[14];
    //     }

    StripifierStats() {
    }

}
