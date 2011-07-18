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
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.vecmath.Color4f;
import javax.vecmath.Matrix4d;

public class TextureAttributesState extends NodeComponentState {

    private static final int MAX_COLOR_OPERANDS = 2;
    
    public TextureAttributesState( SymbolTableData symbol, Controller control ) {
        super(symbol, control);
    }
    
    public void writeObject( DataOutput out ) throws IOException {
        super.writeObject( out );
        TextureAttributes attr = (TextureAttributes)node;
        Color4f clr = new Color4f();
        Matrix4d mat = new Matrix4d();
        Transform3D trans = new Transform3D();
        int tableComponents = attr.getNumTextureColorTableComponents();
        int tableSize = attr.getTextureColorTableSize();
        int[][] colorTable = new int[tableComponents][tableSize];
        
        out.writeInt( attr.getPerspectiveCorrectionMode() );
        attr.getTextureBlendColor( clr );
        control.writeColor4f( out, clr );
        out.writeInt( tableComponents );
        out.writeInt( tableSize );
        attr.getTextureColorTable( colorTable ); 
        for(int i=0; i<tableComponents; i++)
            for(int j=0; j<tableSize; j++)
                out.writeInt( colorTable[i][j] );
        
        out.writeInt( attr.getTextureMode() );
        attr.getTextureTransform( trans );
        trans.get( mat );
        control.writeMatrix4d( out, mat );

	out.writeInt( attr.getCombineRgbMode() );
	out.writeInt( attr.getCombineAlphaMode() );
	for (int i = 0 ; i < MAX_COLOR_OPERANDS ; i++) {
	    out.writeInt( attr.getCombineRgbSource( i ) );
	    out.writeInt( attr.getCombineAlphaSource( i ) );
	    out.writeInt( attr.getCombineRgbFunction( i ) );
	    out.writeInt( attr.getCombineAlphaFunction( i ) );
	}
	out.writeInt( attr.getCombineRgbScale() );
	out.writeInt( attr.getCombineAlphaScale() );
    }
    
    public void readObject( DataInput in ) throws IOException {
        super.readObject( in );
        TextureAttributes attr = (TextureAttributes)node;
        attr.setPerspectiveCorrectionMode( in.readInt() );
        attr.setTextureBlendColor( control.readColor4f(in) );
        int tableComponents = in.readInt();
        int tableSize = in.readInt();
        int[][] colorTable = new int[tableComponents][tableSize];
        for(int i=0; i<tableComponents; i++)
            for(int j=0; j<tableSize; j++)
                colorTable[i][j] = in.readInt();
        if (tableComponents!=0)
            attr.setTextureColorTable( colorTable );
        attr.setTextureMode( in.readInt() );
        Matrix4d mat = control.readMatrix4d( in );
        Transform3D trans = new Transform3D( mat );
        attr.setTextureTransform( trans );

	attr.setCombineRgbMode( in.readInt() );
	attr.setCombineAlphaMode( in.readInt() );
	for (int i = 0 ; i < MAX_COLOR_OPERANDS ; i++ ) {
	    attr.setCombineRgbSource( i, in.readInt() );
	    attr.setCombineAlphaSource( i, in.readInt() );
	    attr.setCombineRgbFunction( i, in.readInt() );
	    attr.setCombineAlphaFunction( i, in.readInt() );
	}
	attr.setCombineRgbScale( in.readInt() );
	attr.setCombineAlphaScale( in.readInt() );
    }
    
    protected javax.media.j3d.SceneGraphObject createNode() {
        return new TextureAttributes();
    }

    
}

