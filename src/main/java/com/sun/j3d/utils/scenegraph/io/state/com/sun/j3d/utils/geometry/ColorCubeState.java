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

package com.sun.j3d.utils.scenegraph.io.state.com.sun.j3d.utils.geometry;

import java.io.*;
import com.sun.j3d.utils.geometry.ColorCube;
import javax.media.j3d.Shape3D;
import javax.media.j3d.SceneGraphObject;
import javax.vecmath.Color3f;
import com.sun.j3d.utils.scenegraph.io.state.javax.media.j3d.Shape3DState;
import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;

public class ColorCubeState extends Shape3DState {
    
    private double scale=1.0;

    public ColorCubeState(SymbolTableData symbol,Controller control) {
	super( symbol, control );
        
    }

    public void writeConstructorParams( DataOutput out ) throws IOException {
	super.writeConstructorParams( out );
        
        out.writeDouble( ((ColorCube)node).getScale() );
        
    }

    public void readConstructorParams( DataInput in ) throws IOException {
       super.readConstructorParams(in);
       
       scale = in.readDouble();
    }
    
    /**
    * Returns true if the groups children should be saved.
    *
    * This is overridden by 'black box' groups such a geometry primitives
    */
    protected boolean processChildren() {
        return false;
    }
  
    public SceneGraphObject createNode( Class j3dClass ) {
        Shape3D shape = (Shape3D) createNode( j3dClass, new Class[]{ Double.TYPE }, new Object[] { new Double(scale) } );

        return shape;
    }
    
    protected javax.media.j3d.SceneGraphObject createNode() {
        return new ColorCube( scale );
    }

    
}
