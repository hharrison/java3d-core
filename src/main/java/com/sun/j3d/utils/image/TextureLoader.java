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

package com.sun.j3d.utils.image;

import javax.media.j3d.*;
import java.awt.Image;
import java.awt.Component;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.lang.reflect.Method;
import javax.imageio.ImageIO;

/**
 * This class is used for loading a texture from an Image or BufferedImage.
 * The Image I/O API is used to load the images.  (If the JAI IIO Tools
 * package is available, a larger set of formats can be loaded, including
 * TIFF, JPEG2000, and so on.)
 *
 * Methods are provided to retrieve the Texture object and the associated
 * ImageComponent object or a scaled version of the ImageComponent object.
 *
 * Default format is RGBA. Other legal formats are: RGBA, RGBA4, RGB5_A1, 
 * RGB, RGB4, RGB5, R3_G3_B2, LUM8_ALPHA8, LUM4_ALPHA4, LUMINANCE and ALPHA
 */
public class TextureLoader extends Object {

    /**
     * Optional flag - specifies that mipmaps are generated for all levels 
     */
    public static final int GENERATE_MIPMAP =  0x01;

    /**
     * Optional flag - specifies that the ImageComponent2D will
     * access the image data by reference
     *
     * @since Java 3D 1.2
     */
    public static final int BY_REFERENCE = 0x02;
    
    /**
     * Optional flag - specifies that the ImageComponent2D will
     * have a y-orientation of y up, meaning the origin of the image is the
     * lower left
     *
     * @since Java 3D 1.2
     */
    public static final int Y_UP = 0x04;

    /**
     * Optional flag - specifies that the ImageComponent2D is allowed
     * to have dimensions that are not a power of two. If this flag is set,
     * TextureLoader will not perform any scaling of images. If this flag
     * is not set, images will be scaled to the nearest power of two. This is
     * the default mode.
     * <p>
     * Note that non-power-of-two textures may not be supported by all graphics
     * cards. Applications should check whether a particular Canvas3D supports
     * non-power-of-two textures by calling the {@link Canvas3D#queryProperties}
     * method, and checking whether the
     * <code>textureNonPowerOfTwoAvailable</code> property is set to true.
     *
     * @since Java 3D 1.5
     */
    public static final int ALLOW_NON_POWER_OF_TWO = 0x08;

    /*
     * Private declaration for BufferedImage allocation
     */
    private static ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB); 
    private static int[] nBits = {8, 8, 8, 8};
    private static int[] bandOffset = { 0, 1, 2, 3};
    private static ComponentColorModel colorModel = new ComponentColorModel(cs, nBits, true, false, Transparency.TRANSLUCENT, 0);

    private Texture2D tex = null;
    private BufferedImage bufferedImage = null;
    private ImageComponent2D imageComponent = null;
    private int textureFormat = Texture.RGBA;
    private int imageComponentFormat = ImageComponent.FORMAT_RGBA;
    private int flags;
    private boolean byRef = false;
    private boolean yUp = false;
    private boolean forcePowerOfTwo = true;

    /**
     * Contructs a TextureLoader object using the specified BufferedImage 
     * and default format RGBA
     * @param bImage The BufferedImage used for loading the texture 
     *
     * @exception NullPointerException if bImage is null
     */
    public TextureLoader(BufferedImage bImage) {
        this(bImage, null, 0);
    }

    /**
     * Contructs a TextureLoader object using the specified BufferedImage 
     * and format
     * @param bImage The BufferedImage used for loading the texture 
     * @param format The format specifies which channels to use
     *
     * @exception NullPointerException if bImage is null
     */
    public TextureLoader(BufferedImage bImage, String format) {
        this(bImage, format, 0);
    }

    /**
     * Contructs a TextureLoader object using the specified BufferedImage,
     * option flags and default format RGBA
     * @param bImage The BufferedImage used for loading the texture
     * @param flags The flags specify what options to use in texture loading (generate mipmap etc)
     *
     * @exception NullPointerException if bImage is null
     */
    public TextureLoader(BufferedImage bImage, int flags) {
        this(bImage, null, flags);
    }

    /**
     * Contructs a TextureLoader object using the specified BufferedImage,
     * format and option flags 
     * @param bImage The BufferedImage used for loading the texture 
     * @param format The format specifies which channels to use
     * @param flags The flags specify what options to use in texture loading (generate mipmap etc)
     *
     * @exception NullPointerException if bImage is null
     */
    public TextureLoader(BufferedImage bImage, String format, int flags) {
        if (bImage == null) {
            throw new NullPointerException();
        }

	parseFormat(format);
	this.flags = flags;
	bufferedImage = bImage;
        if (format==null)
            chooseFormat(bufferedImage);
        
	if ((flags & BY_REFERENCE) != 0) {
	    byRef = true;
	}
	if ((flags & Y_UP) != 0) {
	    yUp = true;
	}
	if ((flags & ALLOW_NON_POWER_OF_TWO) != 0) {
	    forcePowerOfTwo = false;
	}
    }

    /**
     * Contructs a TextureLoader object using the specified Image 
     * and default format RGBA
     * @param image The Image used for loading the texture
     * @param observer The associated image observer
     *
     * @exception NullPointerException if image is null
     * @exception ImageException if there is a problem loading the image
     */
    public TextureLoader(Image image, Component observer) {
	this(image, null, 0, observer);
    }

    /**
     * Contructs a TextureLoader object using the specified Image 
     * and format
     * @param image The Image used for loading the texture 
     * @param format The format specifies which channels to use
     * @param observer The associated image observer
     *
     * @exception NullPointerException if image is null
     * @exception ImageException if there is a problem loading the image
     */
    public TextureLoader(Image image, String format, Component observer) {
	this(image, format, 0, observer);
    }

    /**
     * Contructs a TextureLoader object using the specified Image 
     * flags and default format RGBA
     * @param image The Image used for loading the texture 
     * @param flags The flags specify what options to use in texture loading (generate mipmap etc)
     * @param observer The associated image observer
     *
     * @exception NullPointerException if image is null
     * @exception ImageException if there is a problem loading the image
     */
    public TextureLoader(Image image, int flags, Component observer) {
	this(image, null, flags, observer);
    }
 
    /**
     * Contructs a TextureLoader object using the specified Image 
     * format and option flags 
     * @param image The Image used for loading the texture 
     * @param format The format specifies which channels to use
     * @param flags The flags specify what options to use in texture loading (generate mipmap etc)
     * @param observer The associated image observer
     *
     * @exception NullPointerException if image is null
     * @exception ImageException if there is a problem loading the image
     */
    public TextureLoader(Image image, String format, int flags, 
                         Component observer) {

        if (image == null) {
            throw new NullPointerException();
        }

	if (observer == null) {
            observer = new java.awt.Container();
	}

	parseFormat(format);
	this.flags = flags;
	bufferedImage = createBufferedImage(image, observer);

        if (bufferedImage==null) {
            throw new ImageException("Error loading image: " + image.toString());
        }

        if (format==null)
            chooseFormat(bufferedImage);
        
	if ((flags & BY_REFERENCE) != 0) {
	    byRef = true;
	}
	if ((flags & Y_UP) != 0) {
	    yUp = true;
	}
	if ((flags & ALLOW_NON_POWER_OF_TWO) != 0) {
	    forcePowerOfTwo = false;
	}
    }

    /**
     * Contructs a TextureLoader object using the specified file 
     * and default format RGBA
     * @param fname The file that specifies an Image to load the texture with
     * @param observer The associated image observer
     *
     * @exception ImageException if there is a problem reading the image
     */
    public TextureLoader(String fname, Component observer) {
        this(fname, null, 0, observer);
    }

    /**
     * Contructs a TextureLoader object using the specified file,
     * and format 
     * @param fname The file that specifies an Image to load the texture with
     * @param format The format specifies which channels to use
     * @param observer The associated image observer
     *
     * @exception ImageException if there is a problem reading the image
     */
    public TextureLoader(String fname, String format, Component observer) {
        this(fname, format, 0, observer);
    }

    /**
     * Contructs a TextureLoader object using the specified file, 
     * option flags and default format RGBA
     * @param fname The file that specifies an Image to load the texture with
     * @param flags The flags specify what options to use in texture loading (generate mipmap etc)
     * @param observer The associated image observer
     *
     * @exception ImageException if there is a problem reading the image
     */
    public TextureLoader(String fname, int flags, Component observer) {
        this(fname, null, flags, observer);
    }

    /**
     * Contructs a TextureLoader object using the specified file, 
     * format and option flags 
     * @param fname The file that specifies an Image to load the texture with
     * @param format The format specifies which channels to use
     * @param flags The flags specify what options to use in texture loading (generate mipmap etc)
     * @param observer The associated image observer
     *
     * @exception ImageException if there is a problem reading the image
     */
    public TextureLoader(final String fname, String format, int flags,
			 Component observer) {

        if (observer == null) {
            observer = new java.awt.Container();
        }

        bufferedImage = (BufferedImage)
            java.security.AccessController.doPrivileged(
 	        new java.security.PrivilegedAction() {
                    public Object run() {
                        try {
                            return ImageIO.read(new File(fname));
                        } catch (IOException e) {
			    throw new ImageException(e);
                        }
                    }
                }
            );

        if (bufferedImage==null) {
            throw new ImageException("Error loading image: " + fname);
        }

        parseFormat(format);
        this.flags = flags;

        if (format==null)
            chooseFormat(bufferedImage);

	if ((flags & BY_REFERENCE) != 0) {
	    byRef = true;
	}
	if ((flags & Y_UP) != 0) {
	    yUp = true;
	}
	if ((flags & ALLOW_NON_POWER_OF_TWO) != 0) {
	    forcePowerOfTwo = false;
	}
    }

    /**
     * Contructs a TextureLoader object using the specified URL 
     * and default format RGBA
     * @param url The URL that specifies an Image to load the texture with
     * @param observer The associated image observer
     *
     * @exception ImageException if there is a problem reading the image
     */
    public TextureLoader(URL url, Component observer) {
        this(url, null, 0, observer);
    }

    /**
     * Contructs a TextureLoader object using the specified URL,
     * and format 
     * @param url The URL that specifies an Image to load the texture with
     * @param format The format specifies which channels to use
     * @param observer The associated image observer
     *
     * @exception ImageException if there is a problem reading the image
     */
    public TextureLoader(URL url, String format, Component observer) {
        this(url, format, 0, observer);
    }

    /**
     * Contructs a TextureLoader object using the specified URL, 
     * option flags and default format RGBA
     * @param url The URL that specifies an Image to load the texture with
     * @param flags The flags specify what options to use in texture loading (generate mipmap etc)
     * @param observer The associated image observer
     *
     * @exception ImageException if there is a problem reading the image
     */
    public TextureLoader(URL url, int flags, Component observer) {
        this(url, null, flags, observer);
    }
    /**
     * Contructs a TextureLoader object using the specified URL, 
     * format and option flags 
     * @param url The url that specifies an Image to load the texture with
     * @param format The format specifies which channels to use
     * @param flags The flags specify what options to use in texture loading (generate mipmap etc)
     * @param observer The associated image observer
     *
     * @exception ImageException if there is a problem reading the image
     */
    public TextureLoader(final URL url, String format, int flags,
                         Component observer) {

        if (observer == null) {
            observer = new java.awt.Container();
        }

        bufferedImage = (BufferedImage)
            java.security.AccessController.doPrivileged(
 	        new java.security.PrivilegedAction() {
                    public Object run() {
                        try {
                            return ImageIO.read(url);
                        } catch (IOException e) {
			    throw new ImageException(e);
                        }
                    }
                }
            );

        if (bufferedImage==null) {
            throw new ImageException("Error loading image: " + url.toString());
        }

        parseFormat(format);
        this.flags = flags;

        if (format==null)
            chooseFormat(bufferedImage);

	if ((flags & BY_REFERENCE) != 0) {
	    byRef = true;
	}
	if ((flags & Y_UP) != 0) {
	    yUp = true;
	}
	if ((flags & ALLOW_NON_POWER_OF_TWO) != 0) {
	    forcePowerOfTwo = false;
	}
    }


    /**
     * Returns the associated ImageComponent2D object
     *   
     * @return The associated ImageComponent2D object
     */  
    public ImageComponent2D getImage() {
	if (imageComponent == null) 
            imageComponent = new ImageComponent2D(imageComponentFormat, 
						  bufferedImage, byRef, yUp);
        return imageComponent;
    }

    /**
     * Returns the scaled ImageComponent2D object
     *   
     * @param xScale The X scaling factor
     * @param yScale The Y scaling factor
     *
     * @return The scaled ImageComponent2D object
     */  
    public ImageComponent2D getScaledImage(float xScale, float yScale) {
	if (xScale == 1.0f && yScale == 1.0f)
	    return getImage();
	else
	    return(new ImageComponent2D(imageComponentFormat, 
					getScaledImage(bufferedImage,
						       xScale, yScale),
					byRef, yUp));
    }

    /**
     * Returns the scaled ImageComponent2D object
     *   
     * @param width The desired width
     * @param height The desired height
     *
     * @return The scaled ImageComponent2D object
     */
    public ImageComponent2D getScaledImage(int width, int height) {

	if (bufferedImage.getWidth() == width && 
	    	bufferedImage.getHeight() == height) 
	    return getImage();
        else 
	    return(new ImageComponent2D(imageComponentFormat, 
					getScaledImage(bufferedImage,
						       width, height),
					byRef, yUp));
    }

    /**
     * Returns the associated Texture object.
     *   
     * @return The associated Texture object
     */
    public Texture getTexture() {
	ImageComponent2D[] scaledImageComponents = null;
	BufferedImage[] scaledBufferedImages = null;
        if (tex == null) {

          int width;
          int height;

          if (forcePowerOfTwo) {
              width = getClosestPowerOf2(bufferedImage.getWidth());
              height = getClosestPowerOf2(bufferedImage.getHeight());
	  } else {
              width = bufferedImage.getWidth();
              height = bufferedImage.getHeight();
	  }

	  if ((flags & GENERATE_MIPMAP) != 0) {
      
	    BufferedImage origImage = bufferedImage;
	    int newW = width;
	    int newH = height;
	    int level = Math.max(computeLog(width), computeLog(height)) + 1;
	    scaledImageComponents = new ImageComponent2D[level];
	    scaledBufferedImages = new BufferedImage[level];
            tex = new Texture2D(tex.MULTI_LEVEL_MIPMAP, textureFormat,
                width, height);

            for (int i = 0; i < level; i++) {
                scaledBufferedImages[i] = getScaledImage(origImage, newW, newH);
                scaledImageComponents[i] =  new ImageComponent2D(
			imageComponentFormat, scaledBufferedImages[i],
			byRef, yUp);
	
                tex.setImage(i, scaledImageComponents[i]);
		if (forcePowerOfTwo) {
		    if (newW > 1) newW >>= 1;
		    if (newH > 1) newH >>= 1;
		} else {
		    if (newW > 1) {
			newW = (int) Math.floor(newW / 2.0);
		    }
		    if (newH > 1) {
			newH = (int) Math.floor(newH / 2.0);
		    }
		}
	        origImage = scaledBufferedImages[i];
            }

          } else {
	    scaledImageComponents = new ImageComponent2D[1];
	    scaledBufferedImages = new BufferedImage[1];

            // Create texture from image
            scaledBufferedImages[0] = getScaledImage(bufferedImage, 
			width, height);
            scaledImageComponents[0] = new ImageComponent2D(
			imageComponentFormat, scaledBufferedImages[0],
			byRef, yUp);

            tex = new Texture2D(tex.BASE_LEVEL, textureFormat, width, height);

            tex.setImage(0, scaledImageComponents[0]);
          }
          tex.setMinFilter(tex.BASE_LEVEL_LINEAR);
          tex.setMagFilter(tex.BASE_LEVEL_LINEAR);
        }

	return tex;
    }

    // create a BufferedImage from an Image object
    private BufferedImage createBufferedImage(Image image,
                                              Component observer) {

	int status;
	
        observer.prepareImage(image, null);
        while(true) {
	    status = observer.checkImage(image, null);
            if ((status & ImageObserver.ERROR) != 0) {
                return null;
            } else if ((status & ImageObserver.ALLBITS) != 0) {
                break;
            } 
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
        }

        int width = image.getWidth(observer);
        int height = image.getHeight(observer);      
        
	WritableRaster wr =
            java.awt.image.Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
                                           width, height,
                                           width * 4, 4,
                                           bandOffset, null);
	BufferedImage bImage = new BufferedImage(colorModel, wr, false, null);
	
   	java.awt.Graphics g = bImage.getGraphics();
   	g.drawImage(image, 0, 0, observer);
	
	return bImage;
    }

     /**
     * Choose the correct ImageComponent and Texture format for the given
     * image
     */
    private void chooseFormat(BufferedImage image) {
        switch (image.getType()) {
            case BufferedImage.TYPE_4BYTE_ABGR :  
            case BufferedImage.TYPE_INT_ARGB :
                imageComponentFormat = ImageComponent.FORMAT_RGBA;
                textureFormat = Texture.RGBA;
                break;
            case BufferedImage.TYPE_3BYTE_BGR :
            case BufferedImage.TYPE_INT_BGR:
            case BufferedImage.TYPE_INT_RGB:                
                imageComponentFormat = ImageComponent.FORMAT_RGB;
                textureFormat = Texture.RGB;
                break;
            case BufferedImage.TYPE_CUSTOM:
                if (is4ByteRGBAOr3ByteRGB(image)) {
                    SampleModel sm = image.getSampleModel();
                    if (sm.getNumBands() == 3) {
                        //System.out.println("ChooseFormat Custom:TYPE_4BYTE_ABGR");
                        imageComponentFormat = ImageComponent.FORMAT_RGB;
                        textureFormat = Texture.RGB;
                    }
                    else {
                        imageComponentFormat = ImageComponent.FORMAT_RGBA;
                        //System.out.println("ChooseFormat Custom:TYPE_3BYTE_BGR");
                        textureFormat = Texture.RGBA;
                    }
                }
                break;
            default :
                // System.err.println("Unoptimized Image Type "+image.getType());
                imageComponentFormat = ImageComponent.FORMAT_RGBA;
                textureFormat = Texture.RGBA;
                break;
        }
    }   

    private boolean is4ByteRGBAOr3ByteRGB(RenderedImage ri) {
        boolean value = false;
        int i;
        int biType = getImageType(ri);
        if (biType != BufferedImage.TYPE_CUSTOM)
            return false;
        ColorModel cm = ri.getColorModel();
        ColorSpace cs = cm.getColorSpace();
        SampleModel sm = ri.getSampleModel();
        boolean isAlphaPre = cm.isAlphaPremultiplied();
        int csType = cs.getType();
        if ( csType == ColorSpace.TYPE_RGB) {
            int numBands = sm.getNumBands();
            if (sm.getDataType() == DataBuffer.TYPE_BYTE) {
                if (cm instanceof ComponentColorModel &&
                    sm instanceof PixelInterleavedSampleModel) {
                    PixelInterleavedSampleModel csm = 
				(PixelInterleavedSampleModel) sm;
                    int[] offs = csm.getBandOffsets();
                    ComponentColorModel ccm = (ComponentColorModel)cm;
                    int[] nBits = ccm.getComponentSize();
                    boolean is8Bit = true;
                    for (i=0; i < numBands; i++) {
                        if (nBits[i] != 8) {
                            is8Bit = false;
                            break;
                        }
                    }
                    if (is8Bit &&
                        offs[0] == 0 &&
                        offs[1] == 1 &&
                        offs[2] == 2) {
                        if (numBands == 3) {
                            value = true;
                        }
                        else if (offs[3] == 3 && !isAlphaPre) {
                            value = true;
                        }
                    }
                }
            }
        }
        return value;
    }
    
     private int getImageType(RenderedImage ri) {
        int imageType = BufferedImage.TYPE_CUSTOM;
        int i;

        if (ri instanceof BufferedImage) {
            return ((BufferedImage)ri).getType();
        }
        ColorModel cm = ri.getColorModel();
        ColorSpace cs = cm.getColorSpace();
        SampleModel sm = ri.getSampleModel();
        int csType = cs.getType();
        boolean isAlphaPre = cm.isAlphaPremultiplied();
        if ( csType != ColorSpace.TYPE_RGB) {
            if (csType == ColorSpace.TYPE_GRAY &&
                cm instanceof ComponentColorModel) {
                if (sm.getDataType() == DataBuffer.TYPE_BYTE) {
                    imageType = BufferedImage.TYPE_BYTE_GRAY;
                } else if (sm.getDataType() == DataBuffer.TYPE_USHORT) {
                    imageType = BufferedImage.TYPE_USHORT_GRAY;
                }
            }
        }
        // RGB , only interested in BYTE ABGR and BGR for now
        // all others will be copied to a buffered image
        else {
            int numBands = sm.getNumBands();
            if (sm.getDataType() == DataBuffer.TYPE_BYTE) {
                if (cm instanceof ComponentColorModel &&
                    sm instanceof PixelInterleavedSampleModel) {
                    PixelInterleavedSampleModel csm = 
				(PixelInterleavedSampleModel) sm;
                    int[] offs = csm.getBandOffsets();
                    ComponentColorModel ccm = (ComponentColorModel)cm;
                    int[] nBits = ccm.getComponentSize();
                    boolean is8Bit = true;
                    for (i=0; i < numBands; i++) {
                        if (nBits[i] != 8) {
                            is8Bit = false;
                            break;
                        }
                    }
                    if (is8Bit &&
                        offs[0] == numBands-1 &&
                        offs[1] == numBands-2 &&
                        offs[2] == numBands-3) {
                        if (numBands == 3) {
                            imageType = BufferedImage.TYPE_3BYTE_BGR;
                        }
                        else if (offs[3] == 0) {
                            imageType = (isAlphaPre
                                         ? BufferedImage.TYPE_4BYTE_ABGR_PRE
                                         : BufferedImage.TYPE_4BYTE_ABGR);
                        }
                    }
                }
            }
        }
        return imageType;
    }
         
    // initialize appropriate format for ImageComponent and Texture
    private void parseFormat(String format) {
         if (format==null)
            return;
         
        if (format.equals("RGBA")) {
            imageComponentFormat = ImageComponent.FORMAT_RGBA;
            textureFormat = Texture.RGBA;

        } else if (format.equals("RGBA4")) {                                  
            imageComponentFormat = ImageComponent.FORMAT_RGBA4;
            textureFormat = Texture.RGBA;

        } else if (format.equals("RGB5_A1")) {                                 
            imageComponentFormat = ImageComponent.FORMAT_RGB5_A1;
            textureFormat = Texture.RGBA;

        } else if (format.equals("RGB")) { 
            imageComponentFormat = ImageComponent.FORMAT_RGB;
            textureFormat = Texture.RGB;

        } else if (format.equals("RGB4")) {
            imageComponentFormat = ImageComponent.FORMAT_RGB4;
            textureFormat = Texture.RGB;

        } else if (format.equals("RGB5")) {                                  
            imageComponentFormat = ImageComponent.FORMAT_RGB5;
            textureFormat = Texture.RGB;

        } else if (format.equals("R3_G3_B2")) {                              
            imageComponentFormat = ImageComponent.FORMAT_R3_G3_B2;
            textureFormat = Texture.RGB;
 
        } else if (format.equals("LUM8_ALPHA8")) {
            imageComponentFormat = ImageComponent.FORMAT_LUM8_ALPHA8;
            textureFormat = Texture.LUMINANCE_ALPHA;

        } else if (format.equals("LUM4_ALPHA4")) {
            imageComponentFormat = ImageComponent.FORMAT_LUM4_ALPHA4;
            textureFormat = Texture.LUMINANCE_ALPHA;
       
        } else if (format.equals("LUMINANCE")) {
            imageComponentFormat = ImageComponent.FORMAT_CHANNEL8;
            textureFormat = Texture.LUMINANCE;
 
        } else if (format.equals("ALPHA")) {
            imageComponentFormat = ImageComponent.FORMAT_CHANNEL8;
            textureFormat = Texture.ALPHA;
        }
    }

    // return a scaled image of given width and height
    private BufferedImage getScaledImage(BufferedImage origImage,
                                         int width, int height) {

        int origW = origImage.getWidth();
        int origH = origImage.getHeight();
        float xScale = (float)width/(float)origW;
        float yScale = (float)height/(float)origH;

	return (getScaledImage(origImage, xScale, yScale));
    }

    // return a scaled image of given x and y scale
    private BufferedImage getScaledImage(BufferedImage origImage,
                                         float xScale, float yScale) {


        // System.err.println("(1) origImage " + origImage);
        // If the image is already the requested size, no need to scale
        if (xScale == 1.0f && yScale == 1.0f)
            return origImage;
        else {
            int scaleW = (int)(origImage.getWidth() * xScale + 0.5);
            int scaleH = (int)(origImage.getHeight() * yScale + 0.5);

            int origImageType = origImage.getType();
            BufferedImage scaledImage;
            WritableRaster wr;

            if (origImageType != BufferedImage.TYPE_CUSTOM) {
                WritableRaster origWr = origImage.getRaster();
                wr = origWr.createCompatibleWritableRaster(0, 0, scaleW, scaleH);
                scaledImage = new BufferedImage(scaleW, scaleH, origImageType);
            } else {
                int numComponents = origImage.getSampleModel().getNumBands();
                int[] bandOffset = new int[numComponents];
                int[] nBits = new int[numComponents];
                for (int ii=0; ii < numComponents; ii++) {
                    bandOffset[ii] = ii;
                    nBits[ii] = 8;
                }
                
                wr = java.awt.image.Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
                        scaleW, scaleH,
                        scaleW * numComponents, numComponents,
                        bandOffset, null);

                int imageType;
                
                switch (numComponents) {
                    case 1:
                        imageType = BufferedImage.TYPE_BYTE_GRAY;
                        break;
                    case 3:
                        imageType = BufferedImage.TYPE_3BYTE_BGR;
                        break;
                    case 4:
                        imageType = BufferedImage.TYPE_4BYTE_ABGR;
                        break;
                    default:
                        throw new ImageException("Illegal number of bands : " + numComponents);
                        
                }
                
                scaledImage = new BufferedImage(scaleW, scaleH, imageType);
            }
            
            scaledImage.setData(wr);
            java.awt.Graphics2D g2 = scaledImage.createGraphics();
            AffineTransform at = AffineTransform.getScaleInstance(xScale,
                    yScale);
            g2.transform(at);
            g2.drawImage(origImage, 0, 0, null);

            return scaledImage;
        }
    }

    private int computeLog(int value) {
        int i = 0;

        if (value == 0) return -1;
        for (;;) {
            if (value == 1) 
                return i;
            value >>= 1;
	    i++;
        }
    }

    private int getClosestPowerOf2(int value) {

	if (value < 1)
	    return value;
	
	int powerValue = 1;
	for (;;) {
	    powerValue *= 2;
	    if (value < powerValue) {
		// Found max bound of power, determine which is closest
		int minBound = powerValue/2;
		if ((powerValue - value) >
		    (value - minBound))
		    return minBound;
		else
		    return powerValue;
	    }
	}
    }
}
