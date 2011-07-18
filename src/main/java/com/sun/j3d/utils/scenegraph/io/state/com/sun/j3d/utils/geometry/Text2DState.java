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
import com.sun.j3d.utils.geometry.Sphere;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.Shape3D;
import javax.vecmath.Color3f;
import com.sun.j3d.utils.geometry.Text2D;
import com.sun.j3d.utils.scenegraph.io.state.javax.media.j3d.LeafState;
import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;

public class Text2DState extends LeafState {

    // Text2D is really a subclass of Shape3D, however we don't want
    // the Shape3DState class to save the geometry or appearance data
    // so this class is a subclass of LeafState and we handle
    // the CollisionBounds in the read/write Object methods.
    
    private String text;
    private Color3f color;
    private String fontName;
    private int fontSize;
    private int fontStyle;
    
    public Text2DState(SymbolTableData symbol,Controller control) {
	super( symbol, control );
        
        if (node!=null) {
            Text2D t = (Text2D)node;
            text = t.getString();
            color = t.getColor();
            fontName = t.getFontName();
            fontSize = t.getFontSize();
            fontStyle = t.getFontStyle();
        }
    }

    public void writeObject( DataOutput out ) throws IOException {
	super.writeObject( out );
        control.writeBounds( out, ((Shape3D)node).getCollisionBounds() );
    }

    public void readObject( DataInput in ) throws IOException {
       super.readObject(in);
        ((Shape3D)node).setCollisionBounds( control.readBounds( in ));
    }
    
    public void writeConstructorParams( DataOutput out ) throws IOException {
	super.writeConstructorParams( out );
        
        out.writeUTF( text );
        control.writeColor3f( out, color );
        out.writeUTF( fontName );
        out.writeInt( fontSize );
        out.writeInt( fontStyle );
    }

    public void readConstructorParams( DataInput in ) throws IOException {
       super.readConstructorParams(in);
       
       text = in.readUTF();
       color = control.readColor3f( in );
       fontName = in.readUTF();
       fontSize = in.readInt();
       fontStyle = in.readInt();
    }

    public SceneGraphObject createNode( Class j3dClass ) {
        // Create the node with a null appearance, we will add the appearance
        // during build graph
        Text2D text2D = (Text2D)createNode( j3dClass, new Class[] {
                                                String.class,
                                                Color3f.class,
                                                String.class,
                                                Integer.TYPE,
                                                Integer.TYPE },
                                        new Object[] {
                                                text,
                                                color,
                                                fontName,
                                                new Integer( fontSize ),
                                                new Integer( fontStyle )
                                                } );
                                                
        return text2D;
    }
    
    protected javax.media.j3d.SceneGraphObject createNode() {
        return new Text2D( text, color, fontName, fontSize, fontStyle );
    }

}
