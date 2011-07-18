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
import javax.media.j3d.NodeComponent;
import javax.media.j3d.Appearance;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Material;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.TexCoordGeneration;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.TextureUnitState;
import javax.media.j3d.TransparencyAttributes;

public class AppearanceState extends NodeComponentState {
    
    private int polygonAttributes=0;
    private int renderingAttributes=0;
    private int coloringAttributes=0;
    private int lineAttributes=0;
    private int material=0;
    private int pointAttributes=0;
    private int texCoordGeneration=0;
    private int texture=0;
    private int textureAttributes=0;
    private int[] textureUnitState;
    private int transparencyAttributes=0;
    
    public AppearanceState( SymbolTableData symbol, Controller control ) {
        super(symbol, control);
        
        if (node!=null) {       // Node is null during load
            Appearance app = (Appearance)node;
            polygonAttributes = control.getSymbolTable().addReference( app.getPolygonAttributes() );
            renderingAttributes = control.getSymbolTable().addReference( app.getRenderingAttributes() );
            coloringAttributes = control.getSymbolTable().addReference( app.getColoringAttributes() );
            lineAttributes = control.getSymbolTable().addReference( app.getLineAttributes() );
            material = control.getSymbolTable().addReference( app.getMaterial() );
            pointAttributes = control.getSymbolTable().addReference( app.getPointAttributes() );
            texCoordGeneration = control.getSymbolTable().addReference( app.getTexCoordGeneration() );
            texture = control.getSymbolTable().addReference( app.getTexture() );
            textureAttributes = control.getSymbolTable().addReference( app.getTextureAttributes() );

            TextureUnitState[] texUnitState = app.getTextureUnitState();
            if (texUnitState!=null) {
                textureUnitState = new int[ texUnitState.length ];
                for(int i=0; i<texUnitState.length; i++)
                    textureUnitState[i] = control.getSymbolTable().addReference( texUnitState[i] );            
            } else
                textureUnitState = new int[ 0 ];

            transparencyAttributes = control.getSymbolTable().addReference( app.getTransparencyAttributes() );
        }
    }
    
    public void writeObject( DataOutput out ) throws IOException {
        super.writeObject( out );

        out.writeInt( polygonAttributes );
        out.writeInt( renderingAttributes );
        out.writeInt( coloringAttributes );
        out.writeInt( lineAttributes );
        out.writeInt( material );
        out.writeInt( pointAttributes );
        out.writeInt( texCoordGeneration );
        out.writeInt( texture );
        out.writeInt( textureAttributes );
        out.writeInt( textureUnitState.length );
        for(int i=0; i<textureUnitState.length; i++)
            out.writeInt( textureUnitState[i] );
        out.writeInt( transparencyAttributes );
    }
    
    public void readObject( DataInput in ) throws IOException {
        super.readObject( in );
        polygonAttributes = in.readInt();
        renderingAttributes = in.readInt();
        coloringAttributes = in.readInt();
        lineAttributes = in.readInt();
        material = in.readInt();
        pointAttributes = in.readInt();
        texCoordGeneration = in.readInt();
        texture = in.readInt();
        textureAttributes = in.readInt();
        textureUnitState = new int[in.readInt()];
        for( int i=0; i<textureUnitState.length; i++)
            textureUnitState[i] = in.readInt();
        transparencyAttributes = in.readInt();
    }
    
    /**
     * Called when this component reference count is incremented.
     * Allows this component to update the reference count of any components
     * that it references.
     */
    public void addSubReference() {
        control.getSymbolTable().incNodeComponentRefCount( polygonAttributes );
        control.getSymbolTable().incNodeComponentRefCount( renderingAttributes );
        control.getSymbolTable().incNodeComponentRefCount( coloringAttributes );
        control.getSymbolTable().incNodeComponentRefCount( lineAttributes );
        control.getSymbolTable().incNodeComponentRefCount( material );
        control.getSymbolTable().incNodeComponentRefCount( pointAttributes );
        control.getSymbolTable().incNodeComponentRefCount( texCoordGeneration );
        control.getSymbolTable().incNodeComponentRefCount( textureAttributes );
        control.getSymbolTable().incNodeComponentRefCount( texture );
        for(int i=0; i<textureUnitState.length; i++)
            control.getSymbolTable().incNodeComponentRefCount( textureUnitState[i] );
        control.getSymbolTable().incNodeComponentRefCount( transparencyAttributes );
    }
    
    public void buildGraph() {        
        Appearance app = (Appearance)node;
        app.setPolygonAttributes( (PolygonAttributes)control.getSymbolTable().getJ3dNode(polygonAttributes) );
        app.setRenderingAttributes( (RenderingAttributes)control.getSymbolTable().getJ3dNode(renderingAttributes) );
        app.setColoringAttributes( (ColoringAttributes)control.getSymbolTable().getJ3dNode(coloringAttributes) );
        app.setLineAttributes( (LineAttributes)control.getSymbolTable().getJ3dNode(lineAttributes) );
        app.setMaterial( (Material)control.getSymbolTable().getJ3dNode(material) );
        app.setPointAttributes( (PointAttributes)control.getSymbolTable().getJ3dNode(pointAttributes) );
        app.setTexCoordGeneration( (TexCoordGeneration)control.getSymbolTable().getJ3dNode(texCoordGeneration) );
        app.setTextureAttributes( (TextureAttributes)control.getSymbolTable().getJ3dNode(textureAttributes) );
        app.setTexture( (Texture)control.getSymbolTable().getJ3dNode(texture) );
        
        TextureUnitState[] texUnitState = new TextureUnitState[ textureUnitState.length ];
        for(int i=0; i<textureUnitState.length; i++)
            texUnitState[i] = (TextureUnitState)control.getSymbolTable().getJ3dNode(textureUnitState[i]);
        
        if (texUnitState.length>0)          // TODO - remove if, workaround for bug in daily
            app.setTextureUnitState( texUnitState );
        
        app.setTransparencyAttributes( (TransparencyAttributes)control.getSymbolTable().getJ3dNode(transparencyAttributes) );

        super.buildGraph(); // Must be last call in method
     }
     
    protected javax.media.j3d.SceneGraphObject createNode() {
        return new Appearance();
    }

}

