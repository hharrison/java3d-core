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

package com.sun.j3d.utils.scenegraph.io;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.CapabilityNotSetException;
import com.sun.j3d.utils.scenegraph.io.retained.RandomAccessFileControl;
import com.sun.j3d.utils.universe.SimpleUniverse;

/**
 * Write a (set) of Java3D BranchGraphs and/or Universe to a file. The BranchGraphs 
 * are stored in the order in which they are written, they can be read in any order
 * using SceneGraphFileReader.
 *
 * The API handles Nodes and NodeComponents that are shared between seperate 
 * graphs. It will handle all Java3D 1.3 core classes and any user
 * subclass of a Node or NodeComponent that implements the SceneGraphIO
 * interface.
 */
public class SceneGraphFileWriter extends java.lang.Object {

    private RandomAccessFileControl fileControl;
    private File file;
    
    /** Creates new SceneGraphFileWriter and opens the file for writing.
     *
     * <P>Writes the 
     * Java3D Universe structure to the file. This includes the number and position of
     * the Locales, PlatformGeometry, ViewerAvatar, and the MultitransformGroup between
     * the ViewingPlatform and the View. However this
     * call does not write the content of the branch graphs unless writeUniverseContent is true.
     * <code>universe</code> may be null.
     * This call will overwrite any existing universe, fileDescription and
     * userData in the file.</P>
     * 
     * <P>close() MUST be called when IO is complete. If close() is not called
     * the file contents will be undefined.</P>
     *
     * @param file The file to write the data to
     * @param universe The SimpleUniverse to write
     * @param writeUniverseContent If true, the content of the Locales will be written.
     * Otherwise just the universe configuration data will be written.
     * @param fileDescription A description of the file's content
     * @param fileUserData User defined object
     *
     * @exception IOException Thrown if there are any IO errors
     * @exception UnsupportedUniverseException Thrown if <code>universe</code> is not
     * a supported universe class. Currently SimpleUniverse and ConfiguredUniverse
     * are supported.
     */
    public SceneGraphFileWriter( java.io.File file,
                       SimpleUniverse universe,
                       boolean writeUniverseContent,
                       String fileDescription,
                       java.io.Serializable fileUserData) throws IOException, UnsupportedUniverseException {
        fileControl = new RandomAccessFileControl();
        this.file = file;
        file.createNewFile();
        
        if (!file.canWrite())
            throw new IOException( "Can not Write to File" );
        
        fileControl.createFile( file, universe, writeUniverseContent, fileDescription, fileUserData );
    }
    
    /**
     * Write the graph to the end of the file.
     *
     * close() MUST be called when IO is complete. If close() is not called
     * the file contents will be undefined.
     */
    public void writeBranchGraph( BranchGroup graph ) throws IOException {
        writeBranchGraph( graph, null );
    }
    
    /**
     * Write a branch graph and some user associated data to the
     * end of the file.
     *
     * close() MUST be called when IO is complete. If close() is not called
     * the file contents will be undefined.
     */
    public void writeBranchGraph( BranchGroup graph,
                                  java.io.Serializable data ) throws IOException {
        fileControl.writeBranchGraph( graph, data );
    }
    
    /**
     * Add a named reference to a SceneGraphObject in the file.
     *
     * <code>object</code> must have been written to the file before this method is
     * called. If the object is not in the file a NamedObjectException will be thrown.
     *
     * Adding duplicate names will result in the old name being overwritten.
     * Different names can reference the same object
     */
    public void addObjectName( String name, SceneGraphObject object ) throws NamedObjectException {
        fileControl.addNamedObject( name, object );
    }
    
    /**
     * Close the file and cleanup internal data structures.
     */
    public void close() throws IOException {
        fileControl.close();
    }
    
}
