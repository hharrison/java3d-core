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

package com.sun.j3d.loaders.lw3d;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;
import java.util.Date;
import java.util.Enumeration;
import com.sun.j3d.loaders.lw3d.LWOBFileReader;
import com.sun.j3d.internal.J3dUtilsI18N;
import java.net.*;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.IncorrectFormatException;


/**
 * This class is responsible for parsing a binary Object file and storing
 * the data. This class is not called directly, but is the parent class of
 * J3dLwoObject. The subclass calls this class to parse the file, then it
 * turns the resulting data into Java3D objects. LwoObject instantiates
 * an LWOBFileReader object to parse the data. Then the class creates a
 * list of ShapeHolder objects to hold all of the vertex/facet data and
 * surface references and creates a list of LwoSurface objects to hold
 * the data for each surface.<BR>
 * Rather than describe in detail how the file is parsed for each method,
 * I advise the user of this code to understand the lw3d file format 
 * specs, which are pretty clear.
 */

class LwoParser extends ParserObject {
	
    LWOBFileReader theReader;
    int currLength;
    float coordsArray[];
    float normalCoordsArray[];
    int facetIndicesArray[];
    int facetSizesArray[];
    int normalIndicesArray[];
    int red = 255, green = 255, blue = 255;
    float diffuse = 0.0f, specular = 0.0f, transparency = 0.0f, luminosity = 0.0f;
    int gloss = 128;
    Vector surfNameList = null;
    Vector surfaceList = new Vector(200);	
    Vector shapeList = new Vector(200);

	/**
	* Constructor: Creates file reader and calls parseFile() to actually
	* read the file and grab the data
	*/
    LwoParser(String fileName, int debugVals)
	throws FileNotFoundException {

	super(debugVals);
	debugOutputLn(TRACE, "parser()");
	long start = System.currentTimeMillis();
	theReader = new LWOBFileReader(fileName);
	debugOutputLn(TIME, " file opened in " +
		      (System.currentTimeMillis() - start));
	parseFile();
    }

  LwoParser(URL url, int debugVals)
    throws FileNotFoundException {
      super(debugVals);
      debugOutputLn(TRACE, "parser()");
      try {
	long start = System.currentTimeMillis();
	theReader = new LWOBFileReader(url);
	debugOutputLn(TIME, " file opened in " + 
		      (System.currentTimeMillis() - start));
      }
      catch (IOException ex) {
	throw new FileNotFoundException(url.toString());
      }
      parseFile();
  }
		

	/**
	* Detail polygons are currently not implemented by this loader.  Their
	* structure in geometry files is a bit complex, so there's this separate
	* method for simply parsing through and ignoring the data for detail
	* polygons
	*/
    int skipDetailPolygons(int numPolys) throws ParsingErrorException {
	debugOutputLn(TRACE, "skipDetailPolygons(), numPolys = " + numPolys);
	int lengthRead = 0;
	int vert;
	
	try {
	    for (int polyNum = 0; polyNum < numPolys; ++polyNum) {
		debugOutputLn(VALUES, "polyNum = " + polyNum);
		int numVerts = theReader.getShortInt();
		theReader.skip(numVerts * 2 + 2);  // skip indices plus surf
		lengthRead += (numVerts * 2) + 4;  // increment counter
	    }
	}
	catch (IOException e) {
	    debugOutputLn(EXCEPTION, "Exception in reading detail polys: " + e);
	    throw new ParsingErrorException(e.getMessage());
	}
	return lengthRead;
    }

	/**
	* Returns already-existing ShapeHolder if one exists with the same
	* surface and the same geometry type (point, line, or poly)
	*/
    ShapeHolder getAppropriateShape(int numSurf, int numVerts) {
	for (Enumeration e = shapeList.elements();
	     e.hasMoreElements() ;) {
	    ShapeHolder shape = (ShapeHolder)e.nextElement();
	    if (shape.numSurf == numSurf)
		if (shape.numVerts == numVerts ||
		    (shape.numVerts > 3 &&
		     numVerts > 3))
		    return shape;
	}
	return null;
    }

   
	/**
	* Parse the file for all the data for a POLS object (polygon 
	* description)
	*/ 
    void getPols(int length) {
	debugOutputLn(TRACE, "getPols(len), len = " + length);
	int vert;
	int lengthRead = 0;
	int prevNumVerts = -1;
	int prevNumSurf = 0;
	Vector facetSizesList;
	int facetIndicesArray[];
	facetSizesList =
	    new Vector(length/6);  // worst case size (every poly one vert)
		// Note that our array sizes are hardcoded because we don't
		// know until we're done how large they will be
	facetIndicesArray = new int[length/2];
	ShapeHolder shape = new ShapeHolder(debugPrinter.getValidOutput());
	debugOutputLn(VALUES, "new shape = " + shape);
	shape.coordsArray = coordsArray;
	shape.facetSizesList = facetSizesList;
	//shape.facetIndicesList = facetIndicesList;
	shape.facetIndicesArray = facetIndicesArray;
	shapeList.addElement(shape);
		
    //long startTime = (new Date()).getTime();
	boolean firstTime = true;
	while (lengthRead < length) {
	    int numVerts = theReader.getShortInt();
	    lengthRead += 2;
	    int intArray[] = new int[numVerts];
	    for (int i = 0; i < numVerts; ++i) {
		intArray[i] = theReader.getShortInt();
		lengthRead += 2;
	    }

	    int numSurf = theReader.getShortInt();
	    lengthRead += 2;
	    long startTimeBuff = 0, startTimeList = 0;
	    if (!firstTime &&
		(numSurf != prevNumSurf ||
		 ((numVerts != prevNumVerts) &&
		  ((prevNumVerts < 3) ||
		   (numVerts < 3))))) {
		// If above true, then start new shape
		shape = getAppropriateShape(numSurf, numVerts);
		if (shape == null) {
		    //debugOutputLn(LINE_TRACE, "Starting new shape");
		    facetSizesList = new Vector(length/6);
		    facetIndicesArray = new int[length/2];
		    shape = new ShapeHolder(debugPrinter.getValidOutput());
		    shape.coordsArray = coordsArray;
		    shape.facetSizesList = facetSizesList;
		    //shape.facetIndicesList = facetIndicesList;
		    shape.facetIndicesArray = facetIndicesArray;
		    shape.numSurf = numSurf;
		    shape.numVerts = numVerts;
		    shapeList.addElement(shape);
		    }
		else {
		    facetSizesList = shape.facetSizesList;
		    facetIndicesArray = shape.facetIndicesArray;
		}
	    }
	    else {
		shape.numSurf = numSurf;
		shape.numVerts = numVerts;
	    }
	    prevNumVerts = numVerts;
	    prevNumSurf = numSurf;
	    facetSizesList.addElement(new Integer(numVerts));

	    int currPtr = 0;
	    System.arraycopy(intArray, 0,
			     facetIndicesArray, shape.currentNumIndices,
			     numVerts);
	    shape.currentNumIndices += numVerts;
	    if (numSurf < 0) {   // neg number means detail poly
		int numPolys = theReader.getShortInt();
		lengthRead += skipDetailPolygons(numPolys);
		shape.numSurf = ~shape.numSurf & 0xffff;
		if (shape.numSurf == 0)
		    shape.numSurf = 1;  // Can't have surface = 0
	    }
	    firstTime = false;
	}
    }

	/**
	* Parses file to get the names of all surfaces.  Each polygon will
	* be associated with a particular surface number, which is the index
	* number of these names
	*/
    void getSrfs(int length) {
	String surfName = new String();
    surfNameList = new Vector(length/2);  // worst case size (each name 2 chars long)
	int lengthRead = 0;
	int stopMarker = theReader.getMarker() + length;

        int surfIndex = 0;
	while (theReader.getMarker() < stopMarker) {
	    debugOutputLn(VALUES, "marker, stop = " +
			  theReader.getMarker() + ", " + stopMarker);
	    debugOutputLn(LINE_TRACE, "About to call getString");
	    surfName = theReader.getString();
	    debugOutputLn(VALUES, "Surfname = " + surfName);
	    surfNameList.addElement(surfName);
	}
    }
		
	/**
	* Parses file to get all vertices
	*/
    void getPnts(int length) throws ParsingErrorException {
	int numVerts = length / 12;
	float x, y, z;

	coordsArray = new float[numVerts*3];
	theReader.getVerts(coordsArray, numVerts);
    }

	/**
	* Creates new LwoSurface object that parses file and gets all
	* surface parameters for a particular surface
	*/
    void getSurf(int length) throws FileNotFoundException {
	debugOutputLn(TRACE, "getSurf()");
	
	// Create LwoSurface object to read and hold each surface, then
	// store that surface in a vector of all surfaces.

	LwoSurface surf = new LwoSurface(theReader, length,
		debugPrinter.getValidOutput());
	surfaceList.addElement(surf);
    }


    /**
     * parses entire file.
     * return -1 on error or 0 on completion
     */
    int parseFile() throws FileNotFoundException, IncorrectFormatException {
	debugOutputLn(TRACE, "parseFile()");
	int length = 0;
	int lengthRead = 0;
	int fileLength = 100000;
	
	long loopStartTime = System.currentTimeMillis();
	// Every parsing unit begins with a four character string
	String tokenString = theReader.getToken();
		    
	while (!(tokenString == null) &&
		  lengthRead < fileLength) {
	    long startTime = System.currentTimeMillis();
	    // Based on value of tokenString, go to correct parsing method
	    length = theReader.getInt();

	    lengthRead += 4;
	    //debugOutputLn(VALUES, "length, lengthRead, fileLength = " +
	    //	      length + ", " + lengthRead + ", " + fileLength);
	    //debugOutputLn(VALUES, "LWOB marker is at: " + theReader.getMarker());

	    if (tokenString.equals("FORM")) {
		//debugOutputLn(LINE_TRACE, "got a form");
		fileLength = length + 4;
		length = 0;
		tokenString = theReader.getToken();
		lengthRead += 4;
		if (!tokenString.equals("LWOB"))
		    throw new IncorrectFormatException(
			"File not of FORM-length-LWOB format");
	    }
	    else if (tokenString.equals("PNTS")) {
		//debugOutputLn(LINE_TRACE, "PNTS");
		getPnts(length);
		debugOutputLn(TIME, "done with " + tokenString + " in " +
			      (System.currentTimeMillis() - startTime));
	    }
	    else if (tokenString.equals("POLS")) {
		//debugOutputLn(LINE_TRACE, "POLS");
		getPols(length);
		debugOutputLn(TIME, "done with " + tokenString + " in " +
			      (System.currentTimeMillis() - startTime));
	    }
	    else if (tokenString.equals("SRFS")) {
		//debugOutputLn(LINE_TRACE, "SRFS");
		getSrfs(length);
		    debugOutputLn(TIME, "done with " + tokenString + " in " +
		    (System.currentTimeMillis() - startTime));
	    }
	    else if (tokenString.equals("CRVS")) {
		//debugOutputLn(LINE_TRACE, "CRVS");
		theReader.skipLength(length);
		    //debugOutputLn(TIME, "done with " + tokenString + " in " +
		    //	(System.currentTimeMillis() - startTime));
	    }
	    else if (tokenString.equals("PCHS")) {
		//debugOutputLn(LINE_TRACE, "PCHS");
		theReader.skipLength(length);
		    //debugOutputLn(TIME, "done with " + tokenString + " in " +
		    //	(System.currentTimeMillis() - startTime));
	    }
	    else if (tokenString.equals("SURF")) {
		//debugOutputLn(LINE_TRACE, "SURF");
		getSurf(length);
		//debugOutputLn(VALUES, "Done with SURF, marker = " + theReader.getMarker());
		debugOutputLn(TIME, "done with " + tokenString + " in " +
			      (System.currentTimeMillis() - startTime));
	    }
	    else if (tokenString.equals("LWOB")) {
		//debugOutputLn(LINE_TRACE, "LWOB");
	    }
	    else {
		//debugOutputLn(LINE_TRACE, "Unknown object = " + tokenString);
		theReader.skipLength(length);
		    //debugOutputLn(TIME, "done with " + tokenString + " in " +
		    //	(System.currentTimeMillis() - startTime));
	    }
	    lengthRead += length;
	    if (lengthRead < fileLength) {
		//debugOutputLn(VALUES, "end of parseFile, length, lengthRead = " +
		    //	  length + ", " + lengthRead);
		tokenString = theReader.getToken();
		lengthRead += 4;
		//debugOutputLn(VALUES, "just got tokenString = " + tokenString);
	    }
	}
	debugOutputLn(TIME, "done with parseFile in " +
		      (System.currentTimeMillis() - loopStartTime));
	return 0;
    }

	/**
	* This method is used only for testing
	*/
    static void main(String[] args) {
	String fileName;
	if (args.length == 0)
	    fileName = "cube.obj";
	else
	    fileName = args[0];

        try {
	  LwoParser theParser = new LwoParser(fileName, 0);
	}
	catch (FileNotFoundException e) {
	    System.err.println(e.getMessage());
	    e.printStackTrace();
	    System.exit(1);
	}
    }
}

 


