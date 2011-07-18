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
import com.sun.j3d.utils.geometry.Cone;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.Appearance;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.AmbientLight;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Material;
import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;

public class ConeState extends PrimitiveState {

    private float radius=1f;
    private float height=2f;
    private int xdivision=15;
    private int ydivision=1;
    private int bodyAppearance=0;
    private int capAppearance=0;
    
    public ConeState(SymbolTableData symbol,Controller control) {
	super( symbol, control );
        
        if (node!=null) {
            bodyAppearance = control.getSymbolTable().addReference( ((Cone)node).getShape( Cone.BODY ).getAppearance() );
            capAppearance = control.getSymbolTable().addReference( ((Cone)node).getShape( Cone.CAP ).getAppearance() );
        }
    }

    public void writeObject( DataOutput out ) throws IOException {
	super.writeObject( out );
        out.writeInt( bodyAppearance );
        out.writeInt( capAppearance );
    }

    public void readObject( DataInput in ) throws IOException {
       super.readObject(in);
       bodyAppearance = in.readInt();
       capAppearance = in.readInt();
    }
    
    public void writeConstructorParams( DataOutput out ) throws IOException {
	super.writeConstructorParams( out );
        
        out.writeFloat( ((Cone)node).getRadius() );
        out.writeFloat( ((Cone)node).getHeight() );
        out.writeInt( ((Cone)node).getXdivisions() );
        out.writeInt( ((Cone)node).getYdivisions() );
    }

    public void readConstructorParams( DataInput in ) throws IOException {
       super.readConstructorParams(in);
       
       radius = in.readFloat();
       height = in.readFloat();
       xdivision = in.readInt();
       ydivision = in.readInt();
    }
    
    public void buildGraph() {

        if (bodyAppearance == capAppearance ) {
            ((Cone)node).setAppearance( (Appearance)control.getSymbolTable().getJ3dNode( bodyAppearance ));
        } else {
            ((Cone)node).setAppearance( Cone.BODY, (Appearance)control.getSymbolTable().getJ3dNode( bodyAppearance ));
            ((Cone)node).setAppearance( Cone.CAP, (Appearance)control.getSymbolTable().getJ3dNode( capAppearance ));
        }
        
        super.buildGraph(); // This must be the last call in the method
    }
    
    public SceneGraphObject createNode( Class j3dClass ) {
        // Create the node with a null appearance, we will add the appearance
        // during build graph
        Cone cone = (Cone)createNode( j3dClass, new Class[] {
                                                Float.TYPE,
                                                Float.TYPE,
                                                Integer.TYPE,
                                                Integer.TYPE,
                                                Integer.TYPE,
                                                Appearance.class },
                                        new Object[] {
                                                new Float( radius ),
                                                new Float( height ),
                                                new Integer( primflags ),
                                                new Integer( xdivision ),
                                                new Integer( ydivision ),
                                                null } );
                                                
        return cone;
    }
    
    protected javax.media.j3d.SceneGraphObject createNode() {
        return new Cone( radius, height, primflags, xdivision, ydivision, null );
    }

    
}
