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
import javax.media.j3d.Light;
import javax.media.j3d.BoundingLeaf;
import javax.media.j3d.Group;
import javax.vecmath.Color3f;
import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;

public abstract class LightState extends LeafState {

    private int boundingLeaf = 0;
    private int[] scope;

    public LightState( SymbolTableData symbol, Controller control ) {
	super( symbol, control );
        
    }

    public void writeObject( DataOutput out ) throws IOException {
	super.writeObject( out );
        
	scope = new int[ ((Light)node).numScopes() ];
	for(int i=0; i<((Light)node).numScopes(); i++) {
	    scope[i] = control.getSymbolTable().addReference( ((Light)node).getScope(i) );;
	}
        boundingLeaf = control.getSymbolTable().addReference( ((Light)node).getInfluencingBoundingLeaf() );
        
	Color3f color = new Color3f();
	((Light)node).getColor( color );
	control.writeColor3f( out, color );

	out.writeBoolean( ((Light)node).getEnable() );
        
        out.writeInt( boundingLeaf );
        control.writeBounds( out, ((Light)node).getInfluencingBounds() );
        
        out.writeInt( scope.length );
        for( int i=0; i<scope.length; i++) {
           out.writeInt( scope[i] );
        }
    }

    public void readObject( DataInput in ) throws IOException {
       super.readObject(in);
       ((Light)node).setColor( control.readColor3f(in) );
       ((Light)node).setEnable( in.readBoolean() );

       boundingLeaf = in.readInt();

       ((Light)node).setInfluencingBounds( control.readBounds(in) );

        scope = new int[in.readInt()];
        for(int i=0; i<scope.length; i++) {
            scope[i] = in.readInt();
        }

    }
    
    public void buildGraph() {
        ((Light)node).setInfluencingBoundingLeaf( (BoundingLeaf)control.getSymbolTable().getJ3dNode( boundingLeaf ));
        for(int i=0; i<scope.length; i++) {
            ((Light)node).addScope( (Group)control.getSymbolTable().getJ3dNode( scope[i] ));
        }
        super.buildGraph(); // Must be last call in method
    }
    
}
