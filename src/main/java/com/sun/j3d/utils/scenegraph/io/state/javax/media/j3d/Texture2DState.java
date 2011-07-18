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
import javax.media.j3d.Texture2D;
import javax.media.j3d.ImageComponent2D;

public class Texture2DState extends TextureState {

    private int detailImage=0;
    
    public Texture2DState( SymbolTableData symbol, Controller control ) {
        super(symbol, control);

	// Set up references during save
	if ( node!=null ) {
	    Texture2D t = (Texture2D)node;
	    detailImage = control.getSymbolTable().addReference( t.getDetailImage() );
	}
    }

    public void writeObject( DataOutput out ) throws IOException {
	super.writeObject( out );

	out.writeInt( detailImage );

	out.writeInt( ((Texture2D)node).getDetailTextureMode() );
	out.writeInt( ((Texture2D)node).getDetailTextureLevel() );

	int num = ((Texture2D)node).getDetailTextureFuncPointsCount();
	out.writeInt( num );
	float[] lod = new float[ num ];
	float[] pts = new float[ num ];
	((Texture2D)node).getDetailTextureFunc( lod, pts );
	for (int i = 0 ; i < num ; i++) {
	    out.writeFloat( lod[ i ] );
	    out.writeFloat( pts[ i ] );
	}
    }

    public void readObject( DataInput in ) throws IOException {
	super.readObject( in );

	detailImage = in.readInt();

	((Texture2D)node).setDetailTextureMode( in.readInt() );
	((Texture2D)node).setDetailTextureLevel( in.readInt() );
	int num = in.readInt();
	float[] lod = new float[ num ];
	float[] pts = new float[ num ];
	for (int i = 0 ; i < num ; i++) {
	    lod[ i ] = in.readFloat();
	    pts[ i ] = in.readFloat();
	}
	((Texture2D)node).setDetailTextureFunc( lod, pts );
    }

    /**
     * Called when this component reference count is incremented.
     * Allows this component to update the reference count of any components
     * that it references.
     */
    public void addSubReference() {
	control.getSymbolTable().incNodeComponentRefCount( detailImage );
    }

    // Set up references during load
    public void buildGraph() {
	Texture2D t = (Texture2D)node;
	t.setDetailImage( (ImageComponent2D)control.getSymbolTable().getJ3dNode( detailImage ) );
	super.buildGraph();  // Must be last call in method
    }
       
    public SceneGraphObject createNode( Class j3dClass ) {
        return createNode( j3dClass, new Class[] { Integer.TYPE,
                                                   Integer.TYPE,
                                                   Integer.TYPE,
                                                   Integer.TYPE,
						   Integer.TYPE },
                                     new Object[] { new Integer( mipMapMode ),
                                                    new Integer( format ),
                                                    new Integer( width ),
                                                    new Integer( height ),
						    new Integer( boundaryWidth ) } );
    }
    
    protected javax.media.j3d.SceneGraphObject createNode() {
        return new Texture2D( mipMapMode, format, width, height, boundaryWidth );
    }

    
}
