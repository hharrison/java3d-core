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

package com.sun.j3d.utils.compression;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.media.j3d.CompressedGeometry;
import javax.media.j3d.CompressedGeometryHeader;

//
// The compressed geometry file format supported by this class has a 32
// byte header followed by multiple compressed geometry objects.
//
// Each object consists of a block of compressed data and an 8-byte
// individual block header describing its contents.
//
// The file ends with a directory data structure used for random access,
// containing a 64-bit offset for each object in the order in which it
// appears in the file.  This is also used to find the size of the largest
// object in the file and must be present.
//

/**
 * This class provides methods to read and write compressed geometry resource
 * files.  These files usually end with the .cg extension and support
 * sequential as well as random access to multiple compressed geometry
 * objects.
 *
 * @deprecated As of Java 3D 1.5, replaced by
 * com.sun.j3d.utils.geometry.compression.{@link com.sun.j3d.utils.geometry.compression.CompressedGeometryFile}.
 */
public class CompressedGeometryFile {
    private static final boolean print = false ;
    private static final boolean benchmark = false ;

    /**
     * The magic number which identifies the compressed geometry file type.
     */
    static final int MAGIC_NUMBER = 0xbaddfab4 ;

    /**
     * Byte offset of the magic number from start of file.
     */
    static final int MAGIC_NUMBER_OFFSET = 0 ;

    /**
     * Byte offset of the major version number from start of file.
     */
    static final int MAJOR_VERSION_OFFSET = 4 ;

    /**
     * Byte offset of the minor version number from start of file.
     */
    static final int MINOR_VERSION_OFFSET = 8 ;

    /**
     * Byte offset of the minor minor version number from start of file.
     */
    static final int MINOR_MINOR_VERSION_OFFSET = 12 ;

    /**
     * Byte offset of the number of objects from start of file.
     */
    static final int OBJECT_COUNT_OFFSET  = 16 ;

    /**
     * Byte offset of the directory offset from start of file.
     * This offset is long word aligned since the directory offset is a long.
     */
    static final int DIRECTORY_OFFSET_OFFSET = 24 ;

    /**
     * File header total size in bytes.
     */
    static final int HEADER_SIZE = 32 ;

    /**
     * Byte offset of the object size from start of individual compressed
     * geometry block.
     */
    static final int OBJECT_SIZE_OFFSET = 0 ;

    /**
     * Byte offset of the compressed geometry data descriptor from start of
     * individual compressed geometry block.
     */
    static final int GEOM_DATA_OFFSET = 4 ;

    /**
     * Bits in compressed geometry data descriptor which encode the buffer type.
     */
    static final int TYPE_MASK = 0x03 ;

    /**
     * Bit in compressed geometry data descriptor encoding presence of normals.
     */
    static final int NORMAL_PRESENT_MASK = 0x04 ;

    /**
     * Bit in compressed geometry data descriptor encoding presence of colors.
     */
    static final int COLOR_PRESENT_MASK = 0x08 ;

    /**
     * Bit in compressed geometry data descriptor encoding presence of alphas.
     */
    static final int ALPHA_PRESENT_MASK = 0x10 ;

    /**
     * Value in compressed geometry data descriptor for a point buffer type.
     */
    static final int TYPE_POINT = 1 ;

    /**
     * Value in compressed geometry data descriptor for a line buffer type.
     */
    static final int TYPE_LINE = 2 ;

    /**
     * Value in compressed geometry data descriptor for a triangle buffer type.
     */
    static final int TYPE_TRIANGLE = 3 ;

    /**
     * Block header total size in bytes.
     */
    static final int BLOCK_HEADER_SIZE = 8 ;

    // The name of the compressed geometry resource file.
    String fileName = null ;

    // The major, minor, and subminor version number of the most recent
    // compressor used to compress any of the objects in the compressed
    // geometry resource file.
    int majorVersionNumber ;
    int minorVersionNumber ;
    int minorMinorVersionNumber ;

    // The number of objects in the compressed geometry resource file.
    int objectCount ;

    // The index of the current object in the file.
    int objectIndex = 0 ;

    // The random access file associated with this instance.
    RandomAccessFile cgFile = null ;

    // The magic number identifying the file type.
    int magicNumber ;

    // These fields are set from each individual block of compressed geometry.
    byte cgBuffer[] ;
    int geomSize ;
    int geomStart ;
    int geomDataType ;

    // The directory of object offsets is read from the end of the file.
    long directory[] ;
    long directoryOffset ;

    // The object sizes are computed from the directory offsets.  These are
    // used to allocate a buffer large enough to hold the largest object and
    // to determine how many consecutive objects can be read into that buffer.
    int objectSizes[] ;
    int bufferObjectStart ;
    int bufferObjectCount ;
    int bufferNextObjectCount ;
    int bufferNextObjectOffset ;

    // The shared compressed geometry header object.
    CompressedGeometryHeader cgh ;

    // Flag indicating file update.
    boolean fileUpdate = false ;

    /**
     * Construct a new CompressedGeometryFile instance associated with the
     * specified file.  An attempt is made to open the file with read-only
     * access; if this fails then a FileNotFoundException is thrown.
     *
     * @param file path to the compressed geometry resource file
     * @exception FileNotFoundException if file doesn't exist or
     * cannot be read
     * @exception IllegalArgumentException if the file is not a compressed
     * geometry resource file
     * @exception IOException if there is a header or directory read error
     */
    public CompressedGeometryFile(String file) throws IOException {
	this(file, false) ;
    }

    /**
     * Construct a new CompressedGeometryFile instance associated with the
     * specified file.
     *
     * @param file path to the compressed geometry resource file
     * @param rw if true, opens the file for read and write access or attempts
     * to create one if it doesn't exist; if false, opens the file with
     * read-only access
     * @exception FileNotFoundException if file doesn't exist or
     * access permissions disallow access
     * @exception IllegalArgumentException if the file is not a compressed
     * geometry resource file
     * @exception IOException if there is a header or directory read error
     */
    public CompressedGeometryFile(String file, boolean rw) throws IOException {
	// Open the file and read the file header.
	open(file, rw) ;

	// Copy the file name.
	fileName = new String(file) ;

	// Set up the file fields.
	initialize() ;
    }

    /**
     * Construct a new CompressedGeometryFile instance associated with a
     * currently open RandomAccessFile.
     *
     * @param file currently open RandomAccessFile
     * @exception IllegalArgumentException if the file is not a compressed
     * geometry resource file
     * @exception IOException if there is a header or directory read error
     */
    public CompressedGeometryFile(RandomAccessFile file) throws IOException {
	// Copy the file reference.
	cgFile = file ;
	
	// Set up the file fields.
	initialize() ;
    }

    /**
     * Delete all compressed objects from this instance.  This method may only
     * be called after successfully creating a CompressedGeometryFile instance
     * with read-write access, so a corrupted or otherwise invalid resource
     * must be removed manually before it can be rewritten.  The close()
     * method must be called sometime after invoking clear() in order to write
     * out the new directory structure.
     * 
     * @exception IOException if clear fails
     */
    public void clear() throws IOException {
	// Truncate the file.
	cgFile.setLength(0) ;

	// Set up the file fields.
	initialize() ;
    }

    /**
     * Return a string containing the file name associated with this instance
     * or null if there is none.
     *
     * @return file name associated with this instance or null if there is
     * none
     */
    public String getFileName() {
	return fileName ;
    }

    /**
     * Return the major version number of the most recent compressor used to
     * compress any of the objects in this instance.
     *
     * @return major version number
     */
    public int getMajorVersionNumber() {
	return majorVersionNumber ;
    }

    /**
     * Return the minor version number of the most recent compressor used to
     * compress any of the objects in this instance.
     *
     * @return minor version number
     */
    public int getMinorVersionNumber() {
	return minorVersionNumber ;
    }

    /**
     * Return the subminor version number of the most recent compressor used to
     * compress any of the objects in this instance.
     *
     * @return subminor version number
     */
    public int getMinorMinorVersionNumber() {
	return minorMinorVersionNumber ;
    }

    /**
     * Return the number of compressed objects in this instance.
     *
     * @return number of compressed objects
     */
    public int getObjectCount() {
	return objectCount ;
    }

    /**
     * Return the current object index associated with this instance.  This is
     * the index of the object that would be returned by an immediately
     * following call to the readNext() method.  Its initial value is 0; -1
     * is returned if the last object has been read.
     *
     * @return current object index, or -1 if at end
     */
    public int getCurrentIndex() {
	if (objectIndex == objectCount)
	    return -1 ;
	else
	    return objectIndex ;
    }

    /**
     * Read the next compressed geometry object in the instance.  This is
     * initially the first object (index 0) in the instance; otherwise, it is
     * whatever object is next after the last one read.  The current object
     * index is incremented by 1 after the read.  When the last object is read
     * the index becomes invalid and an immediately subsequent call to
     * readNext() returns null.
     * 
     * @return a CompressedGeometry node component, or null if the last object
     * has been read
     * @exception IOException if read fails
     */
    public CompressedGeometry readNext() throws IOException {
	return readNext(cgBuffer.length) ;
    }

    /**
     * Read all compressed geometry objects contained in the instance.  The
     * current object index becomes invalid; an immediately following call
     * to readNext() will return null.
     *
     * @return an array of CompressedGeometry node components.
     * @exception IOException if read fails
     */
    public CompressedGeometry[] read() throws IOException {
	long startTime = 0 ;
	CompressedGeometry cg[] = new CompressedGeometry[objectCount] ;
	
	if (benchmark)
	    startTime = System.currentTimeMillis() ;

	objectIndex = 0 ;
	setFilePointer(directory[0]) ;
	bufferNextObjectCount = 0 ;

	for (int i = 0 ; i < objectCount ; i++)
	    cg[i] = readNext(cgBuffer.length) ;

	if (benchmark) {
	    long t = System.currentTimeMillis() - startTime ;
	    System.out.println("read " + objectCount +
			       " objects " + cgFile.length() +
			       " bytes in " + (t/1000f) + " sec.") ;
	    System.out.println((cgFile.length()/(float)t) + " Kbytes/sec.") ;
	}

	return cg ;
    }

    /**
     * Read the compressed geometry object at the specified index.  The
     * current object index is set to the subsequent object unless the last
     * object has been read, in which case the index becomes invalid and an
     * immediately following call to readNext() will return null.
     *
     * @param index compressed geometry object to read
     * @return a CompressedGeometry node component
     * @exception IndexOutOfBoundsException if object index is
     * out of range
     * @exception IOException if read fails
     */
    public CompressedGeometry read(int index) throws IOException {
	objectIndex = index ;

	if (objectIndex < 0) {
	    throw new IndexOutOfBoundsException
		("\nobject index must be >= 0") ;
	}
	if (objectIndex >= objectCount) {
	    throw new IndexOutOfBoundsException
		("\nobject index must be < " + objectCount) ;
	}

	// Check if object is in cache.
	if ((objectIndex >= bufferObjectStart) &&
	    (objectIndex <  bufferObjectStart + bufferObjectCount)) {
	    if (print) System.out.println("\ngetting object from cache\n") ;

	    bufferNextObjectOffset = (int)
		(directory[objectIndex] - directory[bufferObjectStart]) ;

	    bufferNextObjectCount =
		bufferObjectCount - (objectIndex - bufferObjectStart) ;

	    return readNext() ;

	} else {
	    // Move file pointer to correct offset.
	    setFilePointer(directory[objectIndex]) ;

	    // Force a read from current offset.  Disable cache read-ahead
	    // since cache hits are unlikely with random access.
	    bufferNextObjectCount = 0 ;
	    return readNext(objectSizes[objectIndex]) ;
	}
    }


    /**
     * Add a compressed geometry node component to the end of the instance.
     * The current object index becomes invalid; an immediately following call
     * to readNext() will return null.  The close() method must be called at
     * some later time in order to create a valid compressed geometry file.
     *
     * @param cg a compressed geometry node component
     * @exception CapabilityNotSetException if unable to get compressed
     * geometry data from the node component
     * @exception IOException if write fails
     */
    public void write(CompressedGeometry cg) throws IOException {
	CompressedGeometryHeader cgh = new CompressedGeometryHeader() ;
	cg.getCompressedGeometryHeader(cgh) ;

	// Update the read/write buffer size if necessary.
	if (cgh.size + BLOCK_HEADER_SIZE > cgBuffer.length) {
	    cgBuffer = new byte[cgh.size + BLOCK_HEADER_SIZE] ;
	    if (print) System.out.println("\ncgBuffer: reallocated " +
					  (cgh.size+BLOCK_HEADER_SIZE) +
					  " bytes") ;
	}
	
	cg.getCompressedGeometry(cgBuffer) ;
	write(cgh, cgBuffer) ;
    }

    /**
     * Add a buffer of compressed geometry data to the end of the
     * resource. The current object index becomes invalid; an immediately
     * following call to readNext() will return null. The close() method must
     * be called at some later time in order to create a valid compressed
     * geometry file.
     *
     * @param cgh a CompressedGeometryHeader object describing the data.
     * @param geometry the compressed geometry data
     * @exception IOException if write fails
     */
    public void write(CompressedGeometryHeader cgh, byte geometry[])
	throws IOException {

	// Update the read/write buffer size if necessary.  It won't be used
	// in this method, but should be big enough to read any object in
	// the file, including the one to be written.
	if (cgh.size + BLOCK_HEADER_SIZE > cgBuffer.length) {
	    cgBuffer = new byte[cgh.size + BLOCK_HEADER_SIZE] ;
	    if (print) System.out.println("\ncgBuffer: reallocated " +
					  (cgh.size+BLOCK_HEADER_SIZE) +
					  " bytes") ;
	}
	
	// Assuming backward compatibility, the version number of the file
	// should be the maximum of all individual compressed object versions.
	if ((cgh.majorVersionNumber > majorVersionNumber)
	    ||
	    ((cgh.majorVersionNumber == majorVersionNumber) &&
	     (cgh.minorVersionNumber > minorVersionNumber))
	    ||
	    ((cgh.majorVersionNumber == majorVersionNumber) &&
	     (cgh.minorVersionNumber == minorVersionNumber) &&
	     (cgh.minorMinorVersionNumber > minorMinorVersionNumber))) {

	    majorVersionNumber = cgh.majorVersionNumber ;
	    minorVersionNumber = cgh.minorVersionNumber ;
	    minorMinorVersionNumber = cgh.minorMinorVersionNumber ;

	    this.cgh.majorVersionNumber = cgh.majorVersionNumber ;
	    this.cgh.minorVersionNumber = cgh.minorVersionNumber ;
	    this.cgh.minorMinorVersionNumber = cgh.minorMinorVersionNumber ;
	}

	// Get the buffer type and see what vertex components are present.
	int geomDataType = 0 ;

	switch (cgh.bufferType) {
	case CompressedGeometryHeader.POINT_BUFFER:
	    geomDataType = TYPE_POINT ;
	    break ;
	case CompressedGeometryHeader.LINE_BUFFER:
	    geomDataType = TYPE_LINE ;
	    break ;
	case CompressedGeometryHeader.TRIANGLE_BUFFER:
	    geomDataType = TYPE_TRIANGLE ;
	    break ;
	}

	if ((cgh.bufferDataPresent &
	     CompressedGeometryHeader.NORMAL_IN_BUFFER) != 0)
	    geomDataType |= NORMAL_PRESENT_MASK ;

	if ((cgh.bufferDataPresent &
	     CompressedGeometryHeader.COLOR_IN_BUFFER) != 0)
	    geomDataType |= COLOR_PRESENT_MASK ;

	if ((cgh.bufferDataPresent &
	     CompressedGeometryHeader.ALPHA_IN_BUFFER) != 0)
	    geomDataType |= ALPHA_PRESENT_MASK ;

	// Allocate new directory and object size arrays if necessary.
	if (objectCount == directory.length) {
	    long newDirectory[] = new long[2*objectCount] ;
	    int newObjectSizes[] = new int[2*objectCount] ;
	    
	    System.arraycopy(directory, 0,
			     newDirectory, 0, objectCount) ;
	    System.arraycopy(objectSizes, 0,
			     newObjectSizes, 0, objectCount) ;
	    
	    directory = newDirectory ;
	    objectSizes = newObjectSizes ;

	    if (print)
		System.out.println("\ndirectory and size arrays: reallocated " +
				   (2*objectCount) + " entries") ;
	}

	// Update directory and object size array.
	directory[objectCount] = directoryOffset ;
	objectSizes[objectCount] = cgh.size + BLOCK_HEADER_SIZE ;
	objectCount++ ;

	// Seek to the directory and overwrite from there.
	setFilePointer(directoryOffset) ;
	cgFile.writeInt(cgh.size) ;
	cgFile.writeInt(geomDataType) ;
	cgFile.write(geometry, 0, cgh.size) ;
	if (print)
	    System.out.println("\nwrote " + cgh.size +
			       " byte compressed object to " + fileName +
			       "\nfile offset " + directoryOffset) ;

	// Update the directory offset.
	directoryOffset += cgh.size + BLOCK_HEADER_SIZE ;

	// Return end-of-file on next read.
	objectIndex = objectCount ;

	// Flag file update so close() will write out the directory.
	fileUpdate = true ;
    }

    /**
     * Release the resources associated with this instance.
     * Write out final header and directory if contents were updated.
     * This method must be called in order to create a valid compressed
     * geometry resource file if any updates were made.
     */
    public void close() {
	if (cgFile != null) {
	    try {
		if (fileUpdate) {
		    writeFileDirectory() ;
		    writeFileHeader() ;
		}
		cgFile.close() ;
	    }
	    catch (IOException e) {
		// Don't propagate this exception.
		System.out.println("\nException: " + e.getMessage()) ;
		System.out.println("failed to close " + fileName) ;
	    }
	}
	cgFile = null ;
	cgBuffer = null ;
	directory = null ;
	objectSizes = null ;
    }


    //
    // Open the file.  Specifying a non-existent file creates a new one if
    // access permissions allow.
    //
    void open(String fname, boolean rw)
	throws FileNotFoundException, IOException {

	cgFile = null ;
	String mode ;

	if (rw)
	    mode = "rw" ;
	else
	    mode = "r" ;

	try {
	    cgFile = new RandomAccessFile(fname, mode) ;
	    if (print) System.out.println("\n" + fname +
					  ": opened mode " + mode) ;
	}
	catch (FileNotFoundException e) {
	    // N.B. this exception is also thrown on access permission errors
	    throw new FileNotFoundException(e.getMessage() + "\n" + fname +
					    ": open mode " + mode + " failed") ;
	}
    }

    //
    // Seek to the specified offset in the file.
    //
    void setFilePointer(long offset) throws IOException {
	cgFile.seek(offset) ;

	// Reset number of objects that can be read sequentially from cache.
	bufferNextObjectCount = 0 ;
    }
    
    //
    // Initialize directory, object size array, read/write buffer, and the
    // shared compressed geometry header.
    //
    void initialize() throws IOException {
	int maxSize = 0 ;

	if (cgFile.length() == 0) {
	    // New file for writing: allocate nominal initial sizes for arrays.
	    objectCount = 0 ;
	    cgBuffer = new byte[32768] ;
	    directory = new long[16] ;
	    objectSizes = new int[directory.length] ;

	    // Set fields as if they have been read.
	    magicNumber = MAGIC_NUMBER ;
	    majorVersionNumber = 1 ;
	    minorVersionNumber = 0 ;
	    minorMinorVersionNumber = 0 ;
	    directoryOffset = HEADER_SIZE ;

	    // Write the file header.
	    writeFileHeader() ;

	} else {
	    // Read the file header.
	    readFileHeader() ;

	    // Check file type.
	    if (magicNumber != MAGIC_NUMBER) {
		close() ;
		throw new IllegalArgumentException
		    ("\n" + fileName + " is not a compressed geometry file") ;
	    }

	    // Read the directory and determine object sizes.
	    directory = new long[objectCount] ;
	    readDirectory(directoryOffset, directory) ;

	    objectSizes = new int[objectCount] ;
	    for (int i = 0 ; i < objectCount-1 ; i++) {
		objectSizes[i] = (int)(directory[i+1] - directory[i]) ;
		if (objectSizes[i] > maxSize) maxSize = objectSizes[i] ;
	    }

	    if (objectCount > 0) {
		objectSizes[objectCount-1] =
		    (int)(directoryOffset - directory[objectCount-1]) ;

		if (objectSizes[objectCount-1] > maxSize)
		    maxSize = objectSizes[objectCount-1] ;
	    }

	    // Allocate a buffer big enough to read the largest object.
	    cgBuffer = new byte[maxSize] ;

	    // Move to the first object.
	    setFilePointer(HEADER_SIZE) ;
	}

	// Set up common parts of the compressed geometry object header.
	cgh = new CompressedGeometryHeader() ;
	cgh.majorVersionNumber = this.majorVersionNumber ;
	cgh.minorVersionNumber = this.minorVersionNumber ;
	cgh.minorMinorVersionNumber = this.minorMinorVersionNumber ;

	if (print) {
	    System.out.println(fileName + ": " + objectCount + " objects") ;
	    System.out.println("magic number 0x" +
			       Integer.toHexString(magicNumber) +
			       ", version number " + majorVersionNumber +
			       "." + minorVersionNumber +
			       "." + minorMinorVersionNumber) ;
	    System.out.println("largest object is " + maxSize + " bytes") ;
	}
    }

    //
    // Read the file header.
    // 
    void readFileHeader() throws IOException {
	byte header[] = new byte[HEADER_SIZE] ;

	try {
	    setFilePointer(0) ;
	    if (cgFile.read(header) != HEADER_SIZE) {
		close() ;
		throw new IOException("failed header read") ;
	    }
	}
	catch (IOException e) {
	    if (cgFile != null) {
		close() ;
	    }
	    throw e ;
	}

	magicNumber =
	    ((header[MAGIC_NUMBER_OFFSET+0] & 0xff) << 24) |
	    ((header[MAGIC_NUMBER_OFFSET+1] & 0xff) << 16) |
	    ((header[MAGIC_NUMBER_OFFSET+2] & 0xff) <<  8) |
	    ((header[MAGIC_NUMBER_OFFSET+3] & 0xff)) ;

	majorVersionNumber =
	    ((header[MAJOR_VERSION_OFFSET+0] & 0xff) << 24) |
	    ((header[MAJOR_VERSION_OFFSET+1] & 0xff) << 16) |
	    ((header[MAJOR_VERSION_OFFSET+2] & 0xff) <<  8) |
	    ((header[MAJOR_VERSION_OFFSET+3] & 0xff)) ;

	minorVersionNumber =
	    ((header[MINOR_VERSION_OFFSET+0] & 0xff) << 24) |
	    ((header[MINOR_VERSION_OFFSET+1] & 0xff) << 16) |
	    ((header[MINOR_VERSION_OFFSET+2] & 0xff) <<  8) |
	    ((header[MINOR_VERSION_OFFSET+3] & 0xff)) ;

	minorMinorVersionNumber =
	    ((header[MINOR_MINOR_VERSION_OFFSET+0] & 0xff) << 24) |
	    ((header[MINOR_MINOR_VERSION_OFFSET+1] & 0xff) << 16) |
	    ((header[MINOR_MINOR_VERSION_OFFSET+2] & 0xff) <<  8) |
	    ((header[MINOR_MINOR_VERSION_OFFSET+3] & 0xff)) ;

	objectCount =
	    ((header[OBJECT_COUNT_OFFSET+0] & 0xff) << 24) |
	    ((header[OBJECT_COUNT_OFFSET+1] & 0xff) << 16) |
	    ((header[OBJECT_COUNT_OFFSET+2] & 0xff) <<  8) |
	    ((header[OBJECT_COUNT_OFFSET+3] & 0xff)) ;

	directoryOffset =
	    ((long)(header[DIRECTORY_OFFSET_OFFSET+0] & 0xff) << 56) |
	    ((long)(header[DIRECTORY_OFFSET_OFFSET+1] & 0xff) << 48) |
	    ((long)(header[DIRECTORY_OFFSET_OFFSET+2] & 0xff) << 40) |
	    ((long)(header[DIRECTORY_OFFSET_OFFSET+3] & 0xff) << 32) |
	    ((long)(header[DIRECTORY_OFFSET_OFFSET+4] & 0xff) << 24) |
	    ((long)(header[DIRECTORY_OFFSET_OFFSET+5] & 0xff) << 16) |
	    ((long)(header[DIRECTORY_OFFSET_OFFSET+6] & 0xff) <<  8) |
	    ((long)(header[DIRECTORY_OFFSET_OFFSET+7] & 0xff)) ;
    }

    //
    // Write the file header based on current field values.
    //
    void writeFileHeader() throws IOException {
	setFilePointer(0) ;
	try {
	    cgFile.writeInt(MAGIC_NUMBER) ;
	    cgFile.writeInt(majorVersionNumber) ;
	    cgFile.writeInt(minorVersionNumber) ;
	    cgFile.writeInt(minorMinorVersionNumber) ;
	    cgFile.writeInt(objectCount) ;
	    cgFile.writeInt(0) ; // long word alignment
	    cgFile.writeLong(directoryOffset) ;
	    if (print)
		System.out.println("wrote file header for " + fileName) ;
	}
	catch (IOException e) {
	    throw new IOException
		(e.getMessage() + 
		 "\ncould not write file header for " + fileName) ;
	}
    }

    //
    // Read the directory of compressed geometry object offsets.
    //
    void readDirectory(long offset, long[] directory)
	throws IOException {

	byte buff[] = new byte[directory.length * 8] ;
	setFilePointer(offset) ;

	try {
	    cgFile.read(buff) ;
	    if (print)
		System.out.println("read " + buff.length + " byte directory") ;
	}
	catch (IOException e) {
	    throw new IOException
		(e.getMessage() +
		 "\nfailed to read " + buff.length +
		 " byte directory, offset " + offset + " in file " + fileName) ;
	}

	for (int i = 0 ; i < directory.length ; i++) {
	    directory[i] = 
		((long)(buff[i*8+0] & 0xff) << 56) |
		((long)(buff[i*8+1] & 0xff) << 48) |
		((long)(buff[i*8+2] & 0xff) << 40) |
		((long)(buff[i*8+3] & 0xff) << 32) |
		((long)(buff[i*8+4] & 0xff) << 24) |
		((long)(buff[i*8+5] & 0xff) << 16) |
		((long)(buff[i*8+6] & 0xff) <<  8) |
		((long)(buff[i*8+7] & 0xff)) ;
	}
    }

    //
    // Write the file directory.
    //
    void writeFileDirectory() throws IOException {
	setFilePointer(directoryOffset) ;

	int directoryAlign = (int)(directoryOffset % 8) ;
	if (directoryAlign != 0) {
	    // Align to long word before writing directory of long offsets.
	    byte bytes[] = new byte[8-directoryAlign] ;

	    try {
		cgFile.write(bytes) ;
		if (print)
		    System.out.println ("wrote " + (8-directoryAlign) +
					" bytes long alignment") ;
	    }
	    catch (IOException e) {
		throw new IOException
		    (e.getMessage() +
		     "\ncould not write " + directoryAlign +
		     " bytes to long word align directory for " + fileName) ;
	    }
	    directoryOffset += 8-directoryAlign ;
	}
	
	try {
	    for (int i = 0 ; i < objectCount ; i++)
		cgFile.writeLong(directory[i]) ;

	    if (print)
		System.out.println("wrote file directory for " + fileName) ;
	}
	catch (IOException e) {
	    throw new IOException
		(e.getMessage() +
		 "\ncould not write directory for " + fileName) ;
	}
    }

    //
    // Get the next compressed object in the file, either from the read-ahead
    // cache or from the file itself.
    // 
    CompressedGeometry readNext(int bufferReadLimit)
	throws IOException {
	if (objectIndex == objectCount)
	    return null ;

	if (bufferNextObjectCount == 0) {
	    // No valid objects are in the cache.
	    int curSize = 0 ;
	    bufferObjectCount = 0 ;

	    // See how much we have room to read.
	    for (int i = objectIndex ; i < objectCount ; i++) {
		if (curSize + objectSizes[i] > bufferReadLimit) break ;
		curSize += objectSizes[i] ;
		bufferObjectCount++ ;
	    }

	    // Try to read that amount.
	    try {
		int n = cgFile.read(cgBuffer, 0, curSize) ;
		if (print)
		    System.out.println("\nread " + n +
				       " bytes from " + fileName) ;
	    }
	    catch (IOException e) {
		throw new IOException
		    (e.getMessage() + 
		     "\nfailed to read " + curSize +
		     " bytes, object " + objectIndex + " in file " + fileName) ;
	    }

	    // Point at the first object in the buffer.
	    bufferObjectStart = objectIndex ;
	    bufferNextObjectCount = bufferObjectCount ;
	    bufferNextObjectOffset = 0 ;
	}

	// Get block header info.
	geomSize =
	    ((cgBuffer[bufferNextObjectOffset+OBJECT_SIZE_OFFSET+0]&0xff)<<24) |
	    ((cgBuffer[bufferNextObjectOffset+OBJECT_SIZE_OFFSET+1]&0xff)<<16) |
	    ((cgBuffer[bufferNextObjectOffset+OBJECT_SIZE_OFFSET+2]&0xff)<< 8) |
	    ((cgBuffer[bufferNextObjectOffset+OBJECT_SIZE_OFFSET+3]&0xff)) ;

	geomDataType =
	    ((cgBuffer[bufferNextObjectOffset+GEOM_DATA_OFFSET+0]&0xff) << 24) |
	    ((cgBuffer[bufferNextObjectOffset+GEOM_DATA_OFFSET+1]&0xff) << 16) |
	    ((cgBuffer[bufferNextObjectOffset+GEOM_DATA_OFFSET+2]&0xff) <<  8) |
	    ((cgBuffer[bufferNextObjectOffset+GEOM_DATA_OFFSET+3]&0xff)) ;

	// Get offset of compressed geometry data from start of buffer.
	geomStart = bufferNextObjectOffset + BLOCK_HEADER_SIZE ;

	if (print) {
	    System.out.println("\nobject " + objectIndex +
			       "\nfile offset " + directory[objectIndex] +
			       ", buffer offset " + bufferNextObjectOffset) ;
	    System.out.println("size " + geomSize + " bytes, " +
			       "data descriptor 0x" +
			       Integer.toHexString(geomDataType)) ;
	}

	// Update cache info.
	bufferNextObjectOffset += objectSizes[objectIndex] ;
	bufferNextObjectCount-- ;
	objectIndex++ ;

	return newCG(geomSize, geomStart, geomDataType) ;
    }

    
    //
    // Construct and return a compressed geometry node.
    //
    CompressedGeometry newCG(int geomSize,
				       int geomStart,
				       int geomDataType) {
	cgh.size = geomSize ;
	cgh.start = geomStart ;

	if ((geomDataType & TYPE_MASK) == TYPE_POINT)
	    cgh.bufferType = CompressedGeometryHeader.POINT_BUFFER ;
	else if ((geomDataType & TYPE_MASK) == TYPE_LINE)
	    cgh.bufferType = CompressedGeometryHeader.LINE_BUFFER ;
	else if ((geomDataType & TYPE_MASK) == TYPE_TRIANGLE)
	    cgh.bufferType = CompressedGeometryHeader.TRIANGLE_BUFFER ;
		
	cgh.bufferDataPresent = 0 ;

	if ((geomDataType & NORMAL_PRESENT_MASK) != 0)
	    cgh.bufferDataPresent |=
		CompressedGeometryHeader.NORMAL_IN_BUFFER ;
	
	if ((geomDataType & COLOR_PRESENT_MASK) != 0)
	    cgh.bufferDataPresent |=
		CompressedGeometryHeader.COLOR_IN_BUFFER ;
	
	if ((geomDataType & ALPHA_PRESENT_MASK) != 0)
	    cgh.bufferDataPresent |=
		CompressedGeometryHeader.ALPHA_IN_BUFFER ;

	return new CompressedGeometry(cgh, cgBuffer) ;
    }

    /**
     * Release file resources when this object is garbage collected.
     */
    protected void finalize() {
	close() ;
    }
}
