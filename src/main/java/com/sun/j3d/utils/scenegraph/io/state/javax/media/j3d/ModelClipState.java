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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.ModelClip;
import javax.media.j3d.BoundingLeaf;
import javax.media.j3d.Group;
import javax.vecmath.Vector4d;

public class ModelClipState extends LeafState {
    
    private int[] scopes;
    private int influencingBoundingLeaf;
    
    public ModelClipState(SymbolTableData symbol,Controller control) {
        super(symbol, control);
        
        if (node!=null) {
            scopes = new int[ ((ModelClip)node).numScopes() ];
            for(int i=0; i<scopes.length; i++)
                scopes[i] = control.getSymbolTable().addReference( ((ModelClip)node).getScope(i));

            influencingBoundingLeaf = control.getSymbolTable().addReference( ((ModelClip)node).getInfluencingBoundingLeaf());
        }
    }
    
    public void writeObject( DataOutput out ) throws IOException {
        super.writeObject( out );
        
        boolean[] enables = new boolean[6];
        ((ModelClip)node).getEnables( enables );
        
        out.writeInt( scopes.length );
        for(int i=0; i<scopes.length; i++)
            out.writeInt( scopes[i] );
        
        out.writeInt( influencingBoundingLeaf );
        control.writeBounds( out, ((ModelClip)node).getInfluencingBounds() );
        
        Vector4d[] planes = new Vector4d[6];
        ((ModelClip)node).getPlanes( planes );
        
        for(int i=0; i<6; i++) {
            out.writeBoolean( enables[i] );
            control.writeVector4d( out, planes[i] );
        }
    }
    
    public void readObject( DataInput in ) throws IOException {
        super.readObject( in );
        
        scopes = new int[ in.readInt() ];
        for(int i=0; i<scopes.length; i++)
            scopes[i] = in.readInt();
        
        influencingBoundingLeaf = in.readInt();
        
        ((ModelClip)node).setInfluencingBounds( control.readBounds(in));
        
        boolean[] enables = new boolean[6];
        Vector4d[] planes = new Vector4d[6];
        for( int i=0; i<6; i++) {
            enables[i] = in.readBoolean();
            planes[i] = control.readVector4d( in );
        }
        
        ((ModelClip)node).setEnables( enables );
        ((ModelClip)node).setPlanes( planes );
    }
    
    public void buildGraph() {
        for(int i=0; i<scopes.length; i++)
            ((ModelClip)node).addScope( (Group)control.getSymbolTable().getJ3dNode( scopes[i] ));
        
        ((ModelClip)node).setInfluencingBoundingLeaf( (BoundingLeaf)control.getSymbolTable().getJ3dNode( influencingBoundingLeaf ));
 	super.buildGraph();       
    }
    
    protected javax.media.j3d.SceneGraphObject createNode() {
        return new ModelClip();
    }

    
}

