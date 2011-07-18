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

import java.io.DataOutput;
import java.io.DataInput;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.io.IOException;
import java.awt.Point;
import java.awt.image.*;
import javax.media.j3d.ImageComponent;
import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;
import com.sun.j3d.utils.scenegraph.io.retained.SGIORuntimeException;
import java.awt.color.ColorSpace;
import java.awt.image.DataBuffer;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;

public abstract class ImageComponentState extends NodeComponentState {

    protected int format;
    protected int height;
    protected int width;
    protected boolean byReference;
    protected boolean yUp;

    private static final int DIRECT_COLOR_MODEL = 1;
    
    private static final int SINGLE_PIXEL_PACKED_SAMPLE_MODEL = 1;
    
    private static final int DATA_BUFFER_INT = 1;
    
    /**
     * Do not compress the images
     */
    public static final byte NO_COMPRESSION = 0;
    
    /**
     * Use GZIP to compress images.
     *
     * GZIP decompression is very slow
     */
    public static final byte GZIP_COMPRESSION = 1;      // GZIP is slow to decompress

    /**
     * Use JPEG compression for images
     *
     * JPEG compression is currently the default. The file format
     * supports other compression algorithms but there is currently
     * no API to select the algorithm. This feature is on hold pending
     * imageio in Java 1.4
     */
    public static final byte JPEG_COMPRESSION = 2;
    
    public ImageComponentState( SymbolTableData symbol, Controller control ) {
	super( symbol, control );
    }


    protected void writeConstructorParams( DataOutput out ) throws
							IOException {
        super.writeConstructorParams( out );
	out.writeInt( ((ImageComponent)node).getFormat());
	out.writeInt( ((ImageComponent)node).getHeight());
	out.writeInt( ((ImageComponent)node).getWidth());
        out.writeBoolean( ((ImageComponent)node).isByReference() );
        out.writeBoolean( ((ImageComponent)node).isYUp() );
    }

    protected void readConstructorParams( DataInput in ) throws
							IOException {
        super.readConstructorParams( in );
	format = in.readInt();
	height = in.readInt();
	width = in.readInt();
        byReference = in.readBoolean();
        yUp = in.readBoolean();
    }
    
    protected void writeBufferedImage( DataOutput out,
				       BufferedImage image ) throws IOException {

        int compressionType = control.getImageCompression();
        
        out.writeByte( compressionType );
        
        if (compressionType==NO_COMPRESSION)
            writeBufferedImageNoCompression( out, image );
        else if (compressionType==GZIP_COMPRESSION)
            writeBufferedImageGzipCompression( out, image );
        else if (compressionType==JPEG_COMPRESSION)
            writeBufferedImageJpegCompression( out, image );
    }
    
    private void writeBufferedImageNoCompression( DataOutput out, BufferedImage image ) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream( byteStream );
        ColorModel colorModel = (ColorModel) image.getColorModel();
        
        if (colorModel instanceof ComponentColorModel) {
            ComponentColorModel cm = (ComponentColorModel) colorModel;
            int numComponents = cm.getNumComponents();
            int type;
            switch (numComponents) {
                case 3:
                    type = BufferedImage.TYPE_INT_RGB;
                    break;
                case 4:
                    type = BufferedImage.TYPE_INT_ARGB;
                    break;
                default:
                    throw new SGIORuntimeException("Unsupported ColorModel "+colorModel.getClass().getName() );
                    
            }
            
            BufferedImage tmpBuf = new BufferedImage(image.getWidth(), image.getHeight(), type);
            WritableRaster dstRaster = tmpBuf.getRaster();
            Raster srcRaster = image.getRaster();  
            dstRaster.setRect(srcRaster);           
           image = tmpBuf;
        }
                   
        writeColorModel( dataOut, image.getColorModel() );      
        writeWritableRaster( dataOut, image.getRaster() );
        dataOut.writeBoolean( image.isAlphaPremultiplied() );
        
        dataOut.close();
        
        byte[] buffer = byteStream.toByteArray();
        out.writeInt( buffer.length );
        out.write( buffer );
    }
    
    private void writeBufferedImageGzipCompression( DataOutput out, BufferedImage image ) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipStream = new GZIPOutputStream( byteStream );
        DataOutputStream dataOut = new DataOutputStream( gzipStream );
        
        writeColorModel( dataOut, image.getColorModel() );
        writeWritableRaster( dataOut, image.getRaster() );
        dataOut.writeBoolean( image.isAlphaPremultiplied() );
        
        dataOut.flush();
        gzipStream.finish();
        
        
        byte[] buffer = byteStream.toByteArray();
        
        out.writeInt( buffer.length );
        out.write( buffer);
        dataOut.close();        
    }
    
    private void writeBufferedImageJpegCompression( DataOutput out, BufferedImage image ) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder( byteStream );

        encoder.encode( image );
        byteStream.close();
        
        byte[] buffer = byteStream.toByteArray();
        out.writeInt( buffer.length );
        out.write( buffer );
    }
    
    protected BufferedImage readBufferedImage( DataInput in ) throws IOException {
        byte compression = in.readByte();
        
        if (compression==NO_COMPRESSION)
            return readBufferedImageNoCompression( in );
        else if (compression==GZIP_COMPRESSION)
            return readBufferedImageGzipCompression( in );
        else if (compression==JPEG_COMPRESSION)
            return readBufferedImageJpegCompression( in );
	throw new SGIORuntimeException("Unknown Image Compression");
    }
        
    private BufferedImage readBufferedImageNoCompression( DataInput in ) throws IOException {  
        int size = in.readInt();
        byte[] buffer = new byte[ size ];
        in.readFully( buffer );
        ByteArrayInputStream byteIn = new ByteArrayInputStream( buffer );
        DataInputStream dataIn = new DataInputStream( byteIn );
        
        ColorModel colorModel = readColorModel( dataIn );
        WritableRaster raster = readWritableRaster( dataIn );
        boolean alphaPreMult = dataIn.readBoolean();
        dataIn.close();
        
        return new BufferedImage( colorModel, raster, alphaPreMult, null );
    }
    
    private BufferedImage readBufferedImageGzipCompression( DataInput in ) throws IOException {  
        int size = in.readInt();
        byte[] buffer = new byte[ size ];
        in.readFully( buffer );
        ByteArrayInputStream byteIn = new ByteArrayInputStream( buffer );
        GZIPInputStream gzipIn = new GZIPInputStream( byteIn );
        DataInputStream dataIn = new DataInputStream( gzipIn );
        
        ColorModel colorModel = readColorModel( dataIn );
        WritableRaster raster = readWritableRaster( dataIn );
        boolean alphaPremult = dataIn.readBoolean();
        dataIn.close();
        
        return new BufferedImage( colorModel, raster, alphaPremult, null );
    }
    
    private BufferedImage readBufferedImageJpegCompression( DataInput in ) throws IOException {  
        int size = in.readInt();
        byte[] buffer = new byte[ size ];
        in.readFully( buffer );
        ByteArrayInputStream byteStream = new ByteArrayInputStream( buffer );
        
        JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder( byteStream );
        byteStream.close();
        
        return decoder.decodeAsBufferedImage();
    }
    
    private void writeColorModel( DataOutput out, ColorModel colorModel ) throws IOException {
        if (colorModel instanceof DirectColorModel) {
            out.writeInt( DIRECT_COLOR_MODEL );
            writeDirectColorModel( out, (DirectColorModel)colorModel );
        } 
        else
            throw new SGIORuntimeException("Unsupported ColorModel "+colorModel.getClass().getName() );
    }
    
    private ColorModel readColorModel( DataInput in ) throws IOException {
        switch( in.readInt() ) {
            case DIRECT_COLOR_MODEL:
                return readDirectColorModel( in );
        }
        
        throw new SGIORuntimeException( "Invalid ColorModel - File corrupt" );
    }

    private void writeDirectColorModel( DataOutput out,
					DirectColorModel colorModel ) throws IOException {
        out.writeInt( colorModel.getPixelSize() );
        out.writeInt( colorModel.getRedMask() );
        out.writeInt( colorModel.getGreenMask() );
        out.writeInt( colorModel.getBlueMask() );
        out.writeInt( colorModel.getAlphaMask() );
    }
    
    private DirectColorModel readDirectColorModel( DataInput in ) throws IOException {
        return new DirectColorModel( in.readInt(),
                                     in.readInt(),
                                     in.readInt(),
                                     in.readInt(),
                                     in.readInt() );
    }
    
    private void writeWritableRaster( DataOutput out, WritableRaster raster ) throws IOException{
        writeSampleModel( out, raster.getSampleModel() );
        writeDataBuffer( out, raster.getDataBuffer() );
        Point origin = new Point();
        // TODO Get the origin of the raster - seems to be missing from the raster API
        out.writeInt( origin.x );
        out.writeInt( origin.y );
    }
    
    private WritableRaster readWritableRaster( DataInput in ) throws IOException {
        return Raster.createWritableRaster( readSampleModel( in ),
                                   readDataBuffer( in ),
                                   new Point( in.readInt(), in.readInt() ));
    }
    
    private void writeSampleModel( DataOutput out, SampleModel model ) throws IOException {
        if (model instanceof SinglePixelPackedSampleModel) {
            out.writeInt( SINGLE_PIXEL_PACKED_SAMPLE_MODEL );
            writeSinglePixelPackedSampleModel( out, (SinglePixelPackedSampleModel)model );
        } else
            throw new SGIORuntimeException("Unsupported SampleModel "+model.getClass().getName() );
    }
    
    private SampleModel readSampleModel( DataInput in ) throws IOException {
        switch( in.readInt() ) {
            case SINGLE_PIXEL_PACKED_SAMPLE_MODEL:
                return readSinglePixelPackedSampleModel( in );
        }
        
        throw new SGIORuntimeException("Invalid SampleModel - file corrupt");
    }
    
    private void writeSinglePixelPackedSampleModel( DataOutput out,
        SinglePixelPackedSampleModel model ) throws IOException {

        int[] masks = model.getBitMasks();
        out.writeInt( masks.length );
        for(int i=0; i<masks.length; i++)
            out.writeInt( masks[i] );
        
        out.writeInt( model.getDataType() );
        out.writeInt( model.getWidth() );
        out.writeInt( model.getHeight() );
        out.writeInt( model.getScanlineStride() );
        
    }
    
    private SinglePixelPackedSampleModel readSinglePixelPackedSampleModel( DataInput in )
	throws IOException {

        int masks[] = new int[ in.readInt() ];
        for(int i=0; i<masks.length; i++)
            masks[i] = in.readInt();
        
        return new SinglePixelPackedSampleModel( in.readInt(),
                                                 in.readInt(),
                                                 in.readInt(),
                                                 in.readInt(),
                                                 masks );
    }
    
    private void writeDataBuffer( DataOutput out, DataBuffer buffer ) throws IOException {
        if (buffer instanceof DataBufferInt) {
            out.writeInt( DATA_BUFFER_INT );
            writeDataBufferInt( out, (DataBufferInt)buffer );
        } else
            throw new SGIORuntimeException("Unsupported DataBuffer "+buffer.getClass().getName() );
    }
    
    private DataBuffer readDataBuffer( DataInput in ) throws IOException {
        switch( in.readInt() ) {
            case DATA_BUFFER_INT:
                return readDataBufferInt( in );
        }
        
        throw new SGIORuntimeException("Incorrect DataBuffer - file corrupt");
    }
    
    private void writeDataBufferInt( DataOutput out, DataBufferInt buffer ) throws IOException {
        int[][] data = buffer.getBankData();
        out.writeInt( data.length );
        for(int i=0; i<data.length; i++) {
            out.writeInt( data[i].length );
            for( int j=0; j<data[i].length; j++)
                out.writeInt( data[i][j] );
        }
        
        out.writeInt( buffer.getSize() );
        
        // TODO Handle DataBufferInt offsets
            
    }
    
    private DataBufferInt readDataBufferInt( DataInput in ) throws IOException {
        int[][] data = new int[in.readInt()][];
        for(int i=0; i<data.length; i++) {
            data[i] = new int[ in.readInt() ];
            for( int j=0; j<data[i].length; j++)
                data[i][j] = in.readInt();
        }
        
        
        return new DataBufferInt( data, in.readInt() );
    }
}
