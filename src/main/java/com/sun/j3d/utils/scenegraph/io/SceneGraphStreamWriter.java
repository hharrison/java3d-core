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
import java.io.DataOutputStream;
import java.util.HashMap;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.CapabilityNotSetException;
import javax.media.j3d.DanglingReferenceException;
import com.sun.j3d.utils.scenegraph.io.retained.StreamControl;
import com.sun.j3d.utils.universe.SimpleUniverse;

/**
 * Writes a Java3D SceneGraph to a Java OutputStream.<p>
 * Using this class to write to a FileOutputStream is not recommended.  Use
 * SceneGraphFileWriter instead to achieve maximum performance and flexibility.
 */
public class SceneGraphStreamWriter extends java.lang.Object {
    
    private StreamControl control;
    private DataOutputStream out;

    /** Creates new SceneGraphStreamWriter that will write to the supplied stream */
    public SceneGraphStreamWriter(java.io.OutputStream outputStream ) throws IOException {
        this.out = new java.io.DataOutputStream( outputStream );
        control = new StreamControl( out );
        control.writeStreamHeader();
    }
    

    /**
     * Write <code>universe</code> to the Stream.<p>
     *
     * If <code>writeContent</code> is true then all BranchGraphs attached to the
     * universe will be saved. If it is false then only the universe
     * data structures will be output (PlatformGeometry, ViewerAvatar, Locales,
     * and the MultiTransformGroup between the ViewingPlatform and the View).<p>
     *
     * If <code>writeContent</code> is true then all the BranchGraphs
     * attached to the Locales of the universe must have the
     * ALLOW_DETACH capability set. If they do not, a <code>CapabilityNotSetException</code>
     * will be thrown<p>
     *
     * @param universe The universe to write
     * @param writeContent Flag enabling the BranchGraphs to be written
     *
     * @exception IOException
     * @exception UnsupportedUniverseException Thrown if the universe class is not
     * supported by this implementation
     */
    public void writeUniverse( SimpleUniverse universe, boolean writeContent ) throws IOException, UnsupportedUniverseException {
        control.writeUniverse( out, universe, writeContent );
    }
    
    /**
     * Write the entire graph to the stream.<p>
     *
     * The API will correctly handle NodeComponents that are shared
     * between seperate graphs. However Nodes cannot be referenced
     * in other Graphs.<p>
     *
     * If a reference to a Node in another graph is encountered a
     * DanglingReferenceException will be thrown.
     *
     * <code>namedObjects</code> can contain a mapping between a key and a SceneGraphObject
     * in the graph. During the read process this can be used to locate nodes
     * in the graph.
     */
    public void writeBranchGraph( BranchGroup graph, HashMap namedObjects ) throws IOException, DanglingReferenceException, NamedObjectException {
        // TODO Add namedObjects to SymbolTable
        control.addNamedObjects( namedObjects );
        control.writeBranchGraph( graph, null );
    }
    
    /** 
     * Close the SceneGraphStreamWriter and the associated stream
     */
    public void close() throws IOException {
        control.close();
        out.close();
    }
}
