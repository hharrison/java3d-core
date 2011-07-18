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

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;
import javax.media.j3d.Shape3D;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.Geometry;
import javax.media.j3d.Appearance;
import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;

public class Shape3DState extends LeafState {

    private int[] geometry;
    private int appearance;
    
    public Shape3DState( SymbolTableData symbol, Controller control ) {
        super( symbol, control );
        
        if (node!=null) {
            appearance = control.getSymbolTable().addReference( ((Shape3D)node).getAppearance() );
            int length = ((Shape3D)node).numGeometries();
            geometry = new int[length];
            for(int i=0; i<length; i++)
               geometry[i] = control.getSymbolTable().addReference( ((Shape3D)node).getGeometry(i) );
        }
    }
    
    public void writeObject( DataOutput out ) throws IOException {
        super.writeObject( out );
        
        control.writeBounds( out, ((Shape3D)node).getCollisionBounds() );
        
        out.writeInt( appearance );
        
        out.writeInt( geometry.length );
        for(int i=0; i<geometry.length; i++)
           out.writeInt( geometry[i] );
    }
    
    public void readObject( DataInput in ) throws IOException {
        super.readObject( in );
        ((Shape3D)node).setCollisionBounds( control.readBounds( in ));
        appearance = in.readInt();
        
        geometry = new int[ in.readInt() ];
        for(int i=0; i<geometry.length; i++) {
            geometry[i] = in.readInt();
        }
    }

    /**
     * Called when this component reference count is incremented.
     * Allows this component to update the reference count of any components
     * that it references.
     */
    public void addSubReference() {
        control.getSymbolTable().incNodeComponentRefCount( appearance );
    }
    
    public void buildGraph() {
        ((Shape3D)node).setAppearance( (Appearance)control.getSymbolTable().getJ3dNode( appearance ) );
        
        ((Shape3D)node).setGeometry( (Geometry)control.getSymbolTable().getJ3dNode( geometry[0] ) );
        for(int i=1; i<geometry.length; i++) {
            ((Shape3D)node).addGeometry( (Geometry)control.getSymbolTable().getJ3dNode( geometry[i] ) );
        }
        super.buildGraph(); // Must be last call in method
    }
    
    protected javax.media.j3d.SceneGraphObject createNode() {
        return new Shape3D();
    }


}
