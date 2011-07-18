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

public class ImageComponent2DURL extends javax.media.j3d.ImageComponent2D {

    private java.net.URL url = null;
    
    public ImageComponent2DURL( int format, java.awt.image.BufferedImage image ) {
        super( format, image );
    }
    
    public ImageComponent2DURL( int format, java.awt.image.BufferedImage image, java.net.URL url ) {
        super( format, image );
        this.url = url;
    }
    
    public ImageComponent2DURL( int format, java.awt.image.BufferedImage image, 
                                boolean byReference, 
                                boolean yUp ) {
        super( format, image, byReference, yUp );
    }

    public ImageComponent2DURL( int format, java.awt.image.BufferedImage image, 
                                boolean byReference, 
                                boolean yUp, 
                                java.net.URL url ) {
        super( format, image, byReference, yUp );
        this.url = url;
    }
    
    public ImageComponent2DURL( int format, int width, int height ) {
        super( format, width, height );
    }
    
    public ImageComponent2DURL( int format, int width, int height,
                                boolean byReference, boolean yUp ) {
        super( format, width, height, byReference, yUp );
    }
    
    public ImageComponent2DURL( int format, java.awt.image.RenderedImage image ) {
        super( format, image );
    }
    
    public ImageComponent2DURL( int format, java.awt.image.RenderedImage image,
                                java.net.URL url ) {
        super( format, image );
        this.url = url;
    }
    
    public ImageComponent2DURL( int format, java.awt.image.RenderedImage image,
                                boolean byReference, boolean yUp ) {
        super( format, image, byReference, yUp );
    }
    
    public ImageComponent2DURL( int format, java.awt.image.RenderedImage image,
                                boolean byReference, boolean yUp,
                                java.net.URL url ) {
        super( format, image, byReference, yUp );
        this.url = url;
    }
    
    /**
     * Set the URL for this image component
     *
     * @param url The URL for the image component
     */
    public void setURL( java.net.URL url ) {
        this.url = url;
    }
    
    /**
     * Get the URL for this image component
     *
     * @return TheURL for this image component
     */
    public java.net.URL getURL() {
        return url;
    }
}
