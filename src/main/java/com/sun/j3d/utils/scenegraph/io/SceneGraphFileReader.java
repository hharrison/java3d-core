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

import javax.media.j3d.VirtualUniverse;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.Canvas3D;
import com.sun.j3d.utils.universe.ConfiguredUniverse;

import com.sun.j3d.utils.scenegraph.io.retained.RandomAccessFileControl;

/**
 * Read Java3D BranchGraphs and/or Universe from a file. Individual branchgraphs or an
 * entire Universe can be read and references (shared nodes and components)
 * between the graphs are handled correctly.
 */
public class SceneGraphFileReader extends java.lang.Object {

    private RandomAccessFileControl fileControl;
    
    /**
     * Creates new SceneGraphFileReader.
     */
    public SceneGraphFileReader( java.io.File file ) throws IOException {
        fileControl = new RandomAccessFileControl();
        fileControl.openFile( file );
    }

    /**
     * Create and return a ConfiguredUniverse with the PlatformGeometry, ViewerAvatar,
     * and Locales saved in the file.  The MultiTransformGroup between the ViewingPlatform
     * and the View is also restored.  Universe configuration information is retrieved
     * via <code>ConfiguredUniverse.getConfigURL()</code>.<p>
     * If the file does not contain universe information, null is returned.<p>
     *
     * @param attachBranchGraphs load and attach all the branchgraphs
     * to the universe.
     * @see ConfiguredUniverse#getConfigURL
     */
    public ConfiguredUniverse readUniverse(boolean attachBranchGraphs ) throws IOException {
        return fileControl.readUniverse( attachBranchGraphs, null );
    }

    /** 
      * Set the ClassLoader used to load the scene graph objects and
      * deserialize user data
      */
    public void setClassLoader( ClassLoader classLoader ) {
        fileControl.setClassLoader( classLoader );
    }

    /** 
      * Get the ClassLoader used to load the scene graph objects and
      * deserialize user data
      */
    public ClassLoader getClassLoader() {
        return fileControl.getClassLoader();
    }
    
    /**
     * Create and return a ConfiguredUniverse with the PlatformGeometry, ViewerAvatar,
     * and Locales saved in the file.  The MultiTransformGroup between the ViewingPlatform
     * and the View is also restored.<p>
     * If the file does not contain universe information, null is returned.<p>
     *
     * @param attachBranchGraphs load and attach all the branchgraphs
     * to the universe.
     * @param canvas The canvas to be associated with the Universe.
     */
    public ConfiguredUniverse readUniverse(boolean attachBranchGraphs,
					   Canvas3D canvas) throws IOException {
        return fileControl.readUniverse( attachBranchGraphs, canvas );
    }
    
    /**
     * Get the UserData in the File header
     */
    public Object readUserData() throws IOException {
        return fileControl.getUserData();
    }
    
    /**
     * Get the Description of this file's contents
     */
    public String readDescription() throws IOException {
        return fileControl.readFileDescription();
    }
    
    /**
     * Return the number of BranchGraphs in the file
     */
    public int getBranchGraphCount() {
        return fileControl.getBranchGraphCount();
    }
    
    /**
     * Read the BranchGraph at index in the file. If the graph
     * contains references to nodes in other BranchGraphs that have not already been
     * loaded, they will also be loaded and returned.<p>
     *
     * The requested graph will always be the first element in the array.<p>
     *
     * The file index of all the Graphs can be discovered using <code>getBranchGraphPosition</code>.<p>
     *
     * @param index The index of the Graph in the file. First graph is at index 0
     *
     * @see #getBranchGraphPosition( BranchGroup graph )
     *
     */
    public BranchGroup[] readBranchGraph(int index) throws IOException {
        return fileControl.readBranchGraph( index );
    }
    
    /**
     * Read and return all the branchgraphs in the file
     */
    public BranchGroup[] readAllBranchGraphs() throws IOException {
        return fileControl.readAllBranchGraphs();
    }
    
    /**
     * Remove the IO system's reference to this branchgraph and all its nodes.<p>
     *
     * References to all loaded graphs are maintained by the IO system in
     * order to facilitate node and component sharing between the graphs.<p>
     *
     * This call removes the references to graph <code>index</code><p>
     *
     * NOT CURRENTLY IMPLEMENTED
     */
    public void dereferenceBranchGraph( BranchGroup graph ) {
        throw new RuntimeException("Not implemented");
    }
    
    /**
     * Given a BranchGraph that has been loaded return the index of the
     * graph in the file.  The the Branchgroup isn't found, -1 is returned.
     */
    public int getBranchGraphPosition( BranchGroup graph ) {
	return fileControl.getBranchGraphPosition( graph );
    }
    
    /**
     * Read the userdata for the branchgraph at 'index' in the file
     *
     * @param index the index of the graph in the file
     */
    public Object readBranchGraphUserData( int index ) throws IOException {
        return fileControl.readBranchGraphUserData( index );
    }
    
    /**
     * Return the names of all the named objects
     */
    public String[] getNames() {
        return fileControl.getNames();
    }
    
    /**
     * Return the named object. 
     *
     * @param name The name of the object
     *
     * @exception NamedObjectException is thrown if the name is not known to the system
     * @exception ObjectNotLoadedException is thrown if the named object has not been loaded yet
     */
    public SceneGraphObject getNamedObject( String name ) throws NamedObjectException, ObjectNotLoadedException {
        return fileControl.getNamedObject( name );
    }
    
    /**
     * Close the file and cleanup internal data structures
     */
    public void close() throws IOException {
        fileControl.close();
    }
    
}
