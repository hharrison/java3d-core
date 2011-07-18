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

package com.sun.j3d.utils.scenegraph.io.state.javax.media.j3d;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.media.j3d.SceneGraphObject;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector4d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Tuple4d;
import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;

public class NullSceneGraphObjectState extends SceneGraphObjectState {
    
    SymbolTableData symbolTableData;
    
    /**
     * Dummy class to represent a null object in the scene graph
     *
     */
    public NullSceneGraphObjectState(SymbolTableData symbol,Controller control) {
        super( null, control );
        symbolTableData = new SymbolTableData( -1, null, this, -1 );
    }
    
    /**
     * DO NOT call symbolTable.addReference in writeObject as this (may)
     * result in a concurrentModificationException.
     *
     * All references should be created in the constructor
     */
    public void writeObject( DataOutput out ) throws IOException {  
    }

    public void readObject( DataInput in ) throws IOException {
    }
    
    public SceneGraphObject getNode() {
        return null;
    }
    
    public int getNodeID() {
        return -1;
    }
    
    public SymbolTableData getSymbol() {
        return symbolTableData;
    }
        
    
    protected javax.media.j3d.SceneGraphObject createNode() {
        return null;
    }


}
