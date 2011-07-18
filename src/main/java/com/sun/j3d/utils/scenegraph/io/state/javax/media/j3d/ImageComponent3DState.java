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
import javax.media.j3d.ImageComponent3D;

import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;

import javax.swing.*;
import java.awt.*;

public class ImageComponent3DState extends ImageComponentState {

    private BufferedImage[] bufferedImages;
    
    public ImageComponent3DState(SymbolTableData symbol,Controller control) {
	super( symbol, control );
    }

    public void writeConstructorParams( DataOutput out ) throws 
							IOException {
        super.writeConstructorParams( out );

	ImageComponent3D ic = ((ImageComponent3D)node);

        // If the BufferedImages are associated with the ImageComponent3D by
        // reference then we don't know much about them, so it'd be hard to
        // save.  So we copy them into an ImageComponent3D and then copy them
        // out.  It comes out in a known format so it's easier to save.
	if ( ic.isByReference() ) {
	    ImageComponent3D noByRef = new ImageComponent3D(
		ic.getFormat(), ic.getRenderedImage(), false, ic.isYUp() );
	    bufferedImages = noByRef.getImage();
	} else bufferedImages = ic.getImage();

	out.writeInt( bufferedImages.length );

        for(int i=0; i<bufferedImages.length; i++) {
            writeBufferedImage( out, bufferedImages[i] );
	}
    }

    public void readConstructorParams( DataInput in ) throws
							IOException {

        super.readConstructorParams( in );  
       
        bufferedImages = new BufferedImage[ in.readInt() ];
        for(int i=0; i<bufferedImages.length; i++)
            bufferedImages[i] = readBufferedImage( in );
       
       /*
	// Debug code to show the original images
	JFrame f = new JFrame();
	JPanel p = new JPanel() {
	    public void paint(Graphics g ) {
		super.paint(g);
		g.drawImage( bufferedImage, 10, 10, null );
	    }

	};

	f.getContentPane().add(p);
	f.setSize( 200, 200 );
	f.show();
        **/
    }

    protected SceneGraphObject createNode( Class j3dClass ) {
	return super.createNode( j3dClass, new Class[] { Integer.TYPE,
                                                          bufferedImages.getClass(),
                                                          Boolean.TYPE,
                                                          Boolean.TYPE },
                                            new Object[] { new Integer(format),
                                                           bufferedImages,
                                                           new Boolean( byReference ),
                                                           new Boolean( yUp ) } );
    }
    
    
    protected javax.media.j3d.SceneGraphObject createNode() {
        return new ImageComponent3D( format, bufferedImages, byReference, yUp );
    }


}
