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

package com.sun.j3d.utils.scenegraph.io.state.com.sun.j3d.utils.image;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.awt.Point;
import java.awt.image.*;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.ImageComponent2D;

import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;
import com.sun.j3d.utils.scenegraph.io.state.javax.media.j3d.ImageComponentState;

import javax.swing.*;
import java.awt.*;

public class ImageComponent2DURLState extends ImageComponentState {

    private static ImageComponent2DURLIOListener listener = new DefaultListener();
    private java.net.URL url;
    
    public ImageComponent2DURLState(SymbolTableData symbol,Controller control) {
	super( symbol, control );
    }

    public void writeConstructorParams( DataOutput out ) throws 
							IOException {
        super.writeConstructorParams( out );    
        
        out.writeUTF( ((ImageComponent2DURL)node).getURL().toExternalForm() );        
    }

    public void readConstructorParams( DataInput in ) throws
							IOException {

       super.readConstructorParams( in ); 
       
       String urlString = (String)in.readUTF();
       
       try {
           url = new java.net.URL(urlString);
       } catch( java.net.MalformedURLException e ) {
           throw new RuntimeException("Bad URL in ImageComponent2DURL "+urlString);
       }       
    }

    protected SceneGraphObject createNode( Class j3dClass ) {
        //System.out.println( "createNode 1" );
        //Thread.dumpStack();
        return listener.createImageComponent( format, width, height, byReference, yUp, url );
    }
    
    
    protected javax.media.j3d.SceneGraphObject createNode() {
        return listener.createImageComponent( format, width, height, byReference, yUp, url );
    }

    /**
     * Set the listener which will handle the creation of this ImageComponent
     * from it's URL.
     *
     * Only a single listener can be set for all ImageComponent2DURL in the
     * scenegraph.
     */
    public static void setLoadListener( ImageComponent2DURLIOListener loadListener ) {
        listener = loadListener;
    }

    static class DefaultListener implements ImageComponent2DURLIOListener {
	public ImageComponent2DURL createImageComponent(
		int format, int width, int height,
		boolean byReference, boolean yUp,
		java.net.URL url ) {

	    System.out.println("Default ImageComponent2DURL loader not implemented "+url );
	    //System.out.println();

	    return new ImageComponent2DURL( format, width, height );
	}
    }

}
