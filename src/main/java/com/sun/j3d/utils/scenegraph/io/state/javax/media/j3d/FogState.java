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
import javax.media.j3d.Fog;
import javax.media.j3d.BoundingLeaf;
import javax.media.j3d.Group;
import javax.media.j3d.NodeComponent;
import javax.media.j3d.SceneGraphObject;
import javax.vecmath.Color3f;

import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;

public abstract class FogState extends SceneGraphObjectState {

    protected int[] scopes;
    protected int boundingLeaf;
    
    public FogState(SymbolTableData symbol,Controller control) {
        super(symbol, control);
    }
    
    public void writeObject( DataOutput out ) throws 
							IOException {

        super.writeObject( out );
        
        control.writeBounds( out, ((Fog)node).getInfluencingBounds() );     
        
        out.writeInt( ((Fog)node).numScopes() );
        for(int i=0; i<((Fog)node).numScopes(); i++)
            out.writeInt( control.getSymbolTable().addReference( ((Fog)node).getScope(i) ) );

        out.writeInt( control.getSymbolTable().addReference( ((Fog)node).getInfluencingBoundingLeaf() ));

        Color3f clr = new Color3f();
       ((Fog)node).getColor( clr );
       
       control.writeColor3f( out, clr );
    }

    public void readObject( DataInput in ) throws
							IOException {
       super.readObject(in);

       ((Fog)node).setInfluencingBounds( control.readBounds(in) );
       scopes = new int[ in.readInt() ];
       for(int i=0; i<scopes.length; i++)
           scopes[i] = in.readInt();
       
       boundingLeaf = in.readInt();
       ((Fog)node).setColor( control.readColor3f(in) );
    }
    
    public void buildGraph() {
        for(int i=0; i<scopes.length; i++)
            ((Fog)node).addScope( (Group)control.getSymbolTable().getJ3dNode( scopes[i] ) );

        ((Fog)node).setInfluencingBoundingLeaf( (BoundingLeaf)control.getSymbolTable().getJ3dNode( boundingLeaf ) );
        super.buildGraph();     // Must be last call in method
    }

}

