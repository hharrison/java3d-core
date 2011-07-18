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
import javax.media.j3d.ImageComponent;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureCubeMap;
import javax.vecmath.Color4f;
import javax.vecmath.Point3f;

public abstract class TextureState extends NodeComponentState {
    
    private int[] imageComponents;
    protected int width;
    protected int height;
    protected int format;
    protected int mipMapMode;
    protected int boundaryWidth;
    
    
    public TextureState( SymbolTableData symbol, Controller control ) {
        super(symbol, control);
        
        if (node!=null) {       // Node is null during a load
	    if ( !(node instanceof TextureCubeMap) ) {
		ImageComponent[] images = ((Texture)node).getImages();
		imageComponents = new int[ images.length ];
		for(int i=0; i<images.length; i++) {
		    imageComponents[i] = control.getSymbolTable().addReference( images[i] );
		}
	    }
        }
        
    }
    
    public void writeConstructorParams( DataOutput out ) throws IOException {
        super.writeConstructorParams( out );
        out.writeInt( ((Texture)node).getMipMapMode() );
        out.writeInt( ((Texture)node).getWidth() );
        out.writeInt( ((Texture)node).getHeight() );
        out.writeInt( ((Texture)node).getFormat() );
        out.writeInt( ((Texture)node).getBoundaryWidth() );
    }
    
    public void readConstructorParams( DataInput in ) throws IOException {
        super.readConstructorParams( in );
        
        mipMapMode = in.readInt();
        width = in.readInt();
        height = in.readInt();
        format = in.readInt();
	boundaryWidth = in.readInt();
    }
    
    public void writeObject( DataOutput out ) throws IOException {
        super.writeObject( out );
        Texture attr = (Texture)node;
        Color4f clr = new Color4f();
        attr.getBoundaryColor( clr );
        control.writeColor4f( out, clr );
        out.writeInt( attr.getBoundaryModeS() );
        out.writeInt( attr.getBoundaryModeT() );
        out.writeBoolean( attr.getEnable() );

        out.writeInt( imageComponents.length );
        for(int i=0; i<imageComponents.length; i++)
            out.writeInt( imageComponents[i] );
        
        out.writeInt( attr.getMagFilter() );
        out.writeInt( attr.getMinFilter() );
	out.writeInt( attr.getBaseLevel() );
	out.writeInt( attr.getMaximumLevel() );
	out.writeFloat( attr.getMinimumLOD() );
	out.writeFloat( attr.getMaximumLOD() );

	Point3f lodOffset = new Point3f();
	attr.getLodOffset( lodOffset );
	control.writePoint3f( out, lodOffset );

	out.writeInt( attr.getAnisotropicFilterMode() );
	out.writeFloat( attr.getAnisotropicFilterDegree() );

	int points = attr.getSharpenTextureFuncPointsCount();
	out.writeInt( points );
	if ( points>0 ) {
	    float[] lod = new float[ points ];
	    float[] pts = new float[ points ];
	    attr.getSharpenTextureFunc( lod, pts );
	    for (int i = 0 ; i < points ; i++) {
		out.writeFloat( lod[i] );
		out.writeFloat( pts[i] );
	    }
	}

	points = attr.getFilter4FuncPointsCount();
	out.writeInt( points );
	if ( points>=4 ) {
	    float[] weights = new float[ points ];
	    attr.getFilter4Func( weights );
	    for (int i = 0 ; i < points ; i++) {
		out.writeFloat( weights[i] );
	    }
	}
    }
    
    public void readObject( DataInput in ) throws IOException {
        super.readObject( in );
        Texture attr = (Texture)node;
        attr.setBoundaryColor( control.readColor4f( in ));
        attr.setBoundaryModeS( in.readInt() );
        attr.setBoundaryModeT( in.readInt() );
        attr.setEnable( in.readBoolean() );

        imageComponents = new int[ in.readInt() ];
        for(int i=0; i<imageComponents.length; i++)
            imageComponents[i] = in.readInt();
        
        int mag = in.readInt();
        try {
            attr.setMagFilter( mag );
        } catch(IllegalArgumentException e ) {
            // The OpenFLT loader sets erroneous values for
            // mag filter which will cause an exception with
            // Java3D 1.3, handle this gracefully....
            if (mag==Texture.MULTI_LEVEL_LINEAR)
                attr.setMagFilter( Texture.BASE_LEVEL_LINEAR );
            else if (mag==Texture.MULTI_LEVEL_POINT)
                attr.setMagFilter( Texture.BASE_LEVEL_POINT );
            else 
                attr.setMagFilter( Texture.FASTEST );
        }
        
        attr.setMinFilter( in.readInt() );

	attr.setBaseLevel( in.readInt() );
	attr.setMaximumLevel( in.readInt() );
	attr.setMinimumLOD( in.readFloat() );
	attr.setMaximumLOD( in.readFloat() );
	attr.setLodOffset( control.readPoint3f( in ) );
	attr.setAnisotropicFilterMode( in.readInt() );
	attr.setAnisotropicFilterDegree( in.readFloat() );

	int points = in.readInt();
	if ( points>0 ) {
	    float[] lod = new float[ points ];
	    float[] pts = new float[ points ];
	    for (int i = 0 ; i < points ; i++) {
		lod[i] = in.readFloat();
		pts[i] = in.readFloat();
	    }
	    attr.setSharpenTextureFunc( lod, pts );
	}

	points = in.readInt();
	if ( points >= 4 ) {
	    float[] weights = new float[ points ];
	    for (int i = 0 ; i < points ; i++) {
		weights[i] = in.readFloat();
	    }
	    attr.setFilter4Func( weights );
	}
    }
    
    public void addSubReference() {
	if ( !(node instanceof TextureCubeMap) ) {
	    for( int i=0; i<imageComponents.length; i++)
		control.getSymbolTable().incNodeComponentRefCount( imageComponents[i] );
	}
    }
    
    public void buildGraph() {
	if ( !(node instanceof TextureCubeMap) ) {
	    for(int i=0; i<imageComponents.length; i++) {
		((Texture)node).setImage( i,
		    (ImageComponent)control.getSymbolTable().getJ3dNode( imageComponents[i] ));
	    }
	}

        super.buildGraph(); // Must be last call in method
    }
    
}

