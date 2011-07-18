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

/**
 * Implement this interface in any classes that subclass a Java3D SceneGraphObject
 * in order to have your class handled correctly by scenegraph.io.
 *
 * More information and example code is provided <A href="doc-files/extensibility.html">here.</a>
 *
 * Classes that implement this interface MUST have a no-arg constructor
 */
public interface SceneGraphIO {

    /**
     * The method is called before writeSGObject and gives the user the chance
     * to create references to other Nodes and NodeComponents.
     *
     * References take the form of a nodeID, of type integer. Every SceneGraphObject
     * is assigned a unique ID.
     *
     * The user must save the reference information in writeSGObject
     *
     * @param ref provides methods to create references to a SceneGraphObject
     */
    public void createSceneGraphObjectReferences( SceneGraphObjectReferenceControl ref );
    
    /**
     * Within this method the user should restore references to the SceneGraphObjects
     * whose nodeID's were created with createSceneGraphObjectReferences
     * This method is called once the all objects in the scenegraph have been loaded.
     *
     *
     * @param ref provides methods to resolve references to a SceneGraphObject
     */
    public void restoreSceneGraphObjectReferences( SceneGraphObjectReferenceControl ref );
    
    /**
     * This method should store all the local state of the object and any references
     * to other SceneGraphObjects into <code>out</code>.
     *
     * This is called after data for the parent SceneGraphObject has been written to
     * the <code>out</code>. 
     *
     * @param out the output stream
     */
    public void writeSceneGraphObject( java.io.DataOutput out ) throws java.io.IOException;
    
    /**
     * This is called after the object has been constructed and the superclass SceneGraphObject
     * data has been read from <code>in</code>.
     *
     * The user should restore all state infomation written in writeSGObject
     *
     * @param in the input stream
     */
    public void readSceneGraphObject( java.io.DataInput in ) throws java.io.IOException;
    
    /**
     * Flag indicating for children of this object should be saved
     *
     * This method only has an effect if this is a subclass of Group.
     *
     * If this returns true then all children of this Group will be saved.
     *
     * If it returns false then the children are not saved.
     */
    public boolean saveChildren();
}

