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

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.Canvas3D;
import com.sun.j3d.utils.scenegraph.io.retained.StreamControl;
import com.sun.j3d.utils.universe.ConfiguredUniverse;

/**
 * Read and create a (set) of Java3D BranchGraphs or Universe from a Java Stream.
 */
public class SceneGraphStreamReader extends java.lang.Object {
    
    private StreamControl control;
    private DataInputStream in;

    /** Creates new SceneGraphStreamReader and reads the file header information */
    public SceneGraphStreamReader( InputStream stream ) throws IOException {
        in = new DataInputStream( stream );
        control = new StreamControl( in );
        control.readStreamHeader();
    }
    
    /**
     * Read and create the universe. If the BranchGraphs were written then
     * they will be added to the universe before it is returned.
     */
    public ConfiguredUniverse readUniverse() throws IOException {
        return control.readUniverse(in, true, null);
    }
    
    /**
     * Read and create the universe. If the BranchGraphs were written then
     * they will be added to the universe before it is returned.
     * @param canvas The Canvas3D to associate with the universe.
     */
    public ConfiguredUniverse readUniverse(Canvas3D canvas) throws IOException {
        return control.readUniverse(in, true, canvas);
    }
    
    /**
     * Read and return the graph from the stream.
     * <code>namedObjects</code> map will be updated with any objects that
     * were named during the write process
     */
    public BranchGroup readBranchGraph( HashMap namedObjects ) throws IOException {
        return control.readBranchGraph( namedObjects );
    }

    /** 
      * Set the ClassLoader used to load the scene graph objects and
      * deserialize user data
      */
    public void setClassLoader( ClassLoader classLoader ) {
        control.setClassLoader( classLoader );
    }

    /** 
      * Get the ClassLoader used to load the scene graph objects and
      * deserialize user data
      */
    public ClassLoader getClassLoader() {
        return control.getClassLoader();
    }
 
    /**
     * Close the SceneGraphStreamReader stream
     *
     * @since Java 3D 1.5.1
     */
    public void close() throws IOException {
        in.close();
        control.close();
    }
  
}

