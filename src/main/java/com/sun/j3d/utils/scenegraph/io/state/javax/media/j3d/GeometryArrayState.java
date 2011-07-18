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

import java.io.*;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.IndexedGeometryArray;
import javax.media.j3d.GeometryStripArray;
import javax.media.j3d.SceneGraphObject;
import javax.vecmath.*;
import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;
import javax.media.j3d.J3DBuffer;
import com.sun.j3d.internal.ByteBufferWrapper;
import com.sun.j3d.internal.BufferWrapper;
import com.sun.j3d.internal.FloatBufferWrapper;
import com.sun.j3d.internal.DoubleBufferWrapper;
import com.sun.j3d.internal.ByteOrderWrapper;

public abstract class GeometryArrayState extends GeometryState {

    protected int vertexFormat;
    protected int vertexCount;
    protected int texCoordSetCount;
    protected int[] texCoordSetMap;

    private static final int FORMAT_NULL = 0;
    private static final int FORMAT_BYTE = 1;
    private static final int FORMAT_FLOAT = 2;
    private static final int FORMAT_DOUBLE = 3;
    private static final int FORMAT_3B = 4;
    private static final int FORMAT_4B = 5;
    private static final int FORMAT_2F = 6;
    private static final int FORMAT_3F = 7;
    private static final int FORMAT_4F = 8;
    private static final int FORMAT_2D = 9;
    private static final int FORMAT_3D = 10;
    
    public GeometryArrayState( SymbolTableData symbol, Controller control ) {
	super( symbol, control );
    }

    public void writeObject( DataOutput out ) throws IOException {
        super.writeObject( out );
        
	boolean nio = (vertexFormat & GeometryArray.USE_NIO_BUFFER) != 0;

	if ( (vertexFormat & GeometryArray.INTERLEAVED)!=0 ) {

	    if ( !(node instanceof IndexedGeometryArray) ) {
		out.writeInt( ((GeometryArray)node).getInitialVertexIndex() );
		if ( !(node instanceof GeometryStripArray) )
		    out.writeInt( ((GeometryArray)node).getValidVertexCount() );
	    }

	    if ( nio ) {
		FloatBufferWrapper x = new FloatBufferWrapper(
		    ((GeometryArray)node).getInterleavedVertexBuffer());
		float[] f = new float[x.limit()];
		x.position( 0 );
		x.get( f );
		writeFloatArray( out, f );
	    } else writeFloatArray( out, ((GeometryArray)node).getInterleavedVertices() );
	} else {
	    boolean byRef = (vertexFormat & GeometryArray.BY_REFERENCE) != 0;

	    if ( !(node instanceof IndexedGeometryArray) ) {
		if ( !byRef )
		    out.writeInt( ((GeometryArray)node).getInitialVertexIndex() );
		if ( !(node instanceof GeometryStripArray) )
		    out.writeInt( ((GeometryArray)node).getValidVertexCount() );
	    }

	    if ( (vertexFormat & GeometryArray.COLOR_4)==GeometryArray.COLOR_4 ) {
		//System.out.println("COLOR 4");
		if ( byRef ) {
		    if ( !(node instanceof IndexedGeometryArray) ) {
			out.writeInt( ((GeometryArray)node).getInitialColorIndex() );
		    }

		    if ( nio ) {
			J3DBuffer buf = ((GeometryArray)node).getColorRefBuffer();
			switch( BufferWrapper.getBufferType( buf ) ) {
			case BufferWrapper.TYPE_BYTE: {
			    out.writeInt( FORMAT_BYTE );
			    ByteBufferWrapper bb = new ByteBufferWrapper( buf );
			    byte[] bytes = new byte[ bb.limit() ];
			    bb.position( 0 );
			    bb.get( bytes );
			    out.writeInt( bytes.length );
			    out.write( bytes );
			}
			break;
			case BufferWrapper.TYPE_FLOAT: {
			    out.writeInt( FORMAT_FLOAT );
			    FloatBufferWrapper bb = new FloatBufferWrapper( buf );
			    float[] floats = new float[ bb.limit() ];
			    bb.position( 0 );
			    bb.get( floats );
			    writeFloatArray( out, floats );
			}
			break;
			case BufferWrapper.TYPE_NULL: {
			    out.writeInt( FORMAT_NULL );
			}
			break;
			}
		    } else if ( ((GeometryArray)node).getColorRef4f()!=null ) {
			out.writeInt( FORMAT_4F );
			Color4f[] colors = ((GeometryArray)node).getColorRef4f();
			float[] data = new float[ colors.length*4 ];
			for (int i = 0 ; i < colors.length ; i++) {
			    data[ i*4+0 ] = colors[i].x;
			    data[ i*4+1 ] = colors[i].y;
			    data[ i*4+2 ] = colors[i].z;
			    data[ i*4+3 ] = colors[i].w;
			}
			writeFloatArray( out, data );
		    } else if ( ((GeometryArray)node).getColorRefFloat()!=null ) {
			out.writeInt( FORMAT_FLOAT );
			writeFloatArray( out, ((GeometryArray)node).getColorRefFloat() );
		    } else if ( ((GeometryArray)node).getColorRefByte()!=null ) {
			out.writeInt( FORMAT_BYTE );
			byte[] colors = ((GeometryArray)node).getColorRefByte();
			out.writeInt( colors.length );
			out.write( colors );
		    } else if ( ((GeometryArray)node).getColorRef4b()!=null ) {
			out.writeInt( FORMAT_4B );
			Color4b[] colors = ((GeometryArray)node).getColorRef4b();
			out.writeInt( colors.length );
			byte[] data = new byte[ colors.length*4 ];
			for (int i = 0 ; i < colors.length ; i++) {
			  data[ i*4+0 ] = colors[i].x;
			  data[ i*4+1 ] = colors[i].y;
			  data[ i*4+2 ] = colors[i].z;
			  data[ i*4+3 ] = colors[i].w;
			}
			out.write( data );
		    } else out.writeInt( FORMAT_NULL );
		} else {
		    byte[] colors = new byte[ vertexCount*4 ];
		    ((GeometryArray)node).getColors( 0, colors );
		    out.write( colors );
		}
	    } else if ((vertexFormat & GeometryArray.COLOR_3)==GeometryArray.COLOR_3 ) {
		//System.out.println("COLOR 3");
		if ( byRef ) {
		    if ( !(node instanceof IndexedGeometryArray) ) {
			out.writeInt( ((GeometryArray)node).getInitialColorIndex() );
		    }

		    if ( nio ) {
			J3DBuffer buf = ((GeometryArray)node).getColorRefBuffer();
			switch( BufferWrapper.getBufferType( buf ) ) {
			case BufferWrapper.TYPE_BYTE: {
			    out.writeInt( FORMAT_BYTE );
			    ByteBufferWrapper bb = new ByteBufferWrapper( buf );
			    byte[] bytes = new byte[ bb.limit() ];
			    bb.position( 0 );
			    bb.get( bytes );
			    out.writeInt( bytes.length );
			    out.write( bytes );
			}
			break;
			case BufferWrapper.TYPE_FLOAT: {
			    out.writeInt( FORMAT_FLOAT );
			    FloatBufferWrapper bb = new FloatBufferWrapper( buf );
			    float[] floats = new float[ bb.limit() ];
			    bb.position( 0 );
			    bb.get( floats );
			    writeFloatArray( out, floats );
			}
			break;
			case BufferWrapper.TYPE_NULL: {
			    out.writeInt( FORMAT_NULL );
			}
			break;
			}
		    } else if ( ((GeometryArray)node).getColorRef3f()!=null ) {
			out.writeInt( FORMAT_3F );
			Color3f[] colors = ((GeometryArray)node).getColorRef3f();
			float[] data = new float[ colors.length*3 ];
			for (int i = 0 ; i < colors.length ; i++) {
			    data[ i*3+0 ] = colors[i].x;
			    data[ i*3+1 ] = colors[i].y;
			    data[ i*3+2 ] = colors[i].z;
			}
			writeFloatArray( out, data );
		    } else if ( ((GeometryArray)node).getColorRefFloat()!=null ) {
			out.writeInt( FORMAT_FLOAT );
			writeFloatArray( out, ((GeometryArray)node).getColorRefFloat() );
		    } else if ( ((GeometryArray)node).getColorRefByte()!=null ) {
			out.writeInt( FORMAT_BYTE );
			byte[] colors = ((GeometryArray)node).getColorRefByte();
			out.writeInt( colors.length );
			out.write( colors );
		    } else if ( ((GeometryArray)node).getColorRef3b()!=null ) {
			out.writeInt( FORMAT_3B );
			Color3b[] colors = ((GeometryArray)node).getColorRef3b();
			out.writeInt( colors.length );
			byte[] data = new byte[ colors.length*3 ];
			for (int i = 0 ; i < colors.length ; i++) {
			    data[ i*3+0 ] = colors[i].x;
			    data[ i*3+1 ] = colors[i].y;
			    data[ i*3+2 ] = colors[i].z;
			}
			out.write( data );
		    } else out.writeInt( FORMAT_NULL );
		} else {
		    byte[] colors3 = new byte[ vertexCount*3 ];
		    ((GeometryArray)node).getColors( 0, colors3 );
		    out.write( colors3 );
		}
	    }


	    if ((vertexFormat & GeometryArray.COORDINATES)!=0 ) {
		//System.out.println("COORDS");
		if ( byRef ) {
		    if ( !(node instanceof IndexedGeometryArray) ) {
			out.writeInt( ((GeometryArray)node).getInitialCoordIndex() );
		    }

		    if ( nio ) {
			J3DBuffer buf = ((GeometryArray)node).getCoordRefBuffer();
			switch( BufferWrapper.getBufferType( buf ) ) {
			case BufferWrapper.TYPE_FLOAT: {
			    out.writeInt( FORMAT_FLOAT );
			    FloatBufferWrapper bb = new FloatBufferWrapper( buf );
			    float[] floats = new float[ bb.limit() ];
			    bb.position( 0 );
			    bb.get( floats );
			    writeFloatArray( out, floats );
			}
			break;
			case BufferWrapper.TYPE_DOUBLE: {
			    out.writeInt( FORMAT_DOUBLE );
			    DoubleBufferWrapper bb = new DoubleBufferWrapper( buf );
			    double[] doubles = new double[ bb.limit() ];
			    bb.position( 0 );
			    bb.get( doubles );
			    writeDoubleArray( out, doubles );
			}
			break;
			case BufferWrapper.TYPE_NULL: {
			    out.writeInt( FORMAT_NULL );
			}
			break;
			}
		    } else if ( ((GeometryArray)node).getCoordRef3f()!=null ) {
			out.writeInt( FORMAT_3F );
			Point3f[] coords = ((GeometryArray)node).getCoordRef3f();
			float[] data = new float[ coords.length*3 ];
			for (int i = 0 ; i < coords.length ; i++) {
			    data[ i*3+0 ] = coords[i].x;
			    data[ i*3+1 ] = coords[i].y;
			    data[ i*3+2 ] = coords[i].z;
			}
			writeFloatArray( out, data );
		    } else if ( ((GeometryArray)node).getCoordRef3d()!=null ) {
			out.writeInt( FORMAT_3D );
			Point3d[] coords = ((GeometryArray)node).getCoordRef3d();
			double[] data = new double[ coords.length*3 ];
			for (int i = 0 ; i < coords.length ; i++) {
			    data[ i*3+0 ] = coords[i].x;
			    data[ i*3+1 ] = coords[i].y;
			    data[ i*3+2 ] = coords[i].z;
			}
			writeDoubleArray( out, data );
		    } else if ( ((GeometryArray)node).getCoordRefFloat()!=null ) {
			out.writeInt( FORMAT_FLOAT );
			writeFloatArray( out, ((GeometryArray)node).getCoordRefFloat() );
		    } else if ( ((GeometryArray)node).getCoordRefDouble()!=null ) {
			out.writeInt( FORMAT_DOUBLE );
			writeDoubleArray( out, ((GeometryArray)node).getCoordRefDouble() );
		    } else out.writeInt( FORMAT_NULL );
		} else {
		    float[] points = new float[ vertexCount*3 ];
		    ((GeometryArray)node).getCoordinates( 0, points );
		    writeFloatArray( out, points );
		}
	    }

	    if ((vertexFormat & GeometryArray.NORMALS)!=0) {
		//System.out.println("NORMALS");
		if ( byRef ) {
		    if ( !(node instanceof IndexedGeometryArray) ) {
			out.writeInt( ((GeometryArray)node).getInitialNormalIndex() );
		    }

		    if ( nio ) {
			J3DBuffer buf = ((GeometryArray)node).getNormalRefBuffer();
			if ( BufferWrapper.getBufferType( buf )==BufferWrapper.TYPE_NULL )
			    out.writeInt( FORMAT_NULL );
			else {
			    out.writeInt( FORMAT_FLOAT );
			    FloatBufferWrapper bb = new FloatBufferWrapper( buf );
			    float[] floats = new float[ bb.limit() ];
			    bb.position( 0 );
			    bb.get( floats );
			    writeFloatArray( out, floats );
			}
		    } else if ( ((GeometryArray)node).getNormalRef3f()!=null ) {
			out.writeInt( FORMAT_3F );
			Vector3f[] norms = ((GeometryArray)node).getNormalRef3f();
			float[] data = new float[ norms.length*3 ];
			for (int i = 0 ; i < norms.length ; i++) {
			    data[ i*3+0 ] = norms[i].x;
			    data[ i*3+1 ] = norms[i].y;
			    data[ i*3+2 ] = norms[i].z;
			}
			writeFloatArray( out, data );
		    } else if ( ((GeometryArray)node).getNormalRefFloat()!=null ) {
		        out.writeInt( FORMAT_FLOAT );
			writeFloatArray( out, ((GeometryArray)node).getNormalRefFloat() );
		    } else out.writeInt( FORMAT_NULL );
		} else {
		    float[] normals = new float[ vertexCount*3 ];
		    ((GeometryArray)node).getNormals( 0, normals );
		    writeFloatArray( out, normals );
		}
	    }

	    if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_2)!=0) {
		//System.out.println("TEXTURE COORDS 2");
		for(int set=0; set<texCoordSetCount; set++) {
		    if ( byRef ) {
			if ( !(node instanceof IndexedGeometryArray) ) {
			    out.writeInt( ((GeometryArray)node).getInitialTexCoordIndex( set ) );
			}

			if ( nio ) {
			    J3DBuffer buf = ((GeometryArray)node).getTexCoordRefBuffer( set );
			    if ( BufferWrapper.getBufferType( buf )==BufferWrapper.TYPE_NULL )
				out.writeInt( FORMAT_NULL );
			    else {
				out.writeInt( FORMAT_FLOAT );
				FloatBufferWrapper bb = new FloatBufferWrapper( buf );
				float[] floats = new float[ bb.limit() ];
				bb.position( 0 );
				bb.get( floats );
				writeFloatArray( out, floats );
			    }
			} else if ( ((GeometryArray)node).getTexCoordRef2f(set)!=null ) {
			    out.writeInt( FORMAT_2F );
			    TexCoord2f[] tcoords = ((GeometryArray)node).getTexCoordRef2f(set);
			    float[] data = new float[ tcoords.length*2 ];
			    for (int i = 0 ; i < tcoords.length ; i++) {
				data[ i*2+0 ] = tcoords[i].x;
				data[ i*2+1 ] = tcoords[i].y;
			    }
			    writeFloatArray( out, data );
			} else if ( ((GeometryArray)node).getTexCoordRefFloat(set)!=null ) {
			    out.writeInt( FORMAT_FLOAT );
			    writeFloatArray( out, ((GeometryArray)node).getTexCoordRefFloat(set) );
			} else out.writeInt( FORMAT_NULL );
		    } else {
			float[] textureCoords = new float[ vertexCount*2 ];
			((GeometryArray)node).getTextureCoordinates( set, 0, textureCoords );
			writeFloatArray( out, textureCoords );
		    }
		}
	    } else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_3)!=0) {
		//System.out.println("TEXTURE COORDS 3");
		for(int set=0; set<texCoordSetCount; set++) {
		    if ( byRef ) {
			if ( !(node instanceof IndexedGeometryArray) ) {
			    out.writeInt( ((GeometryArray)node).getInitialTexCoordIndex( set ) );
			}

			if ( nio ) {
			    J3DBuffer buf = ((GeometryArray)node).getTexCoordRefBuffer( set );
			    if ( BufferWrapper.getBufferType( buf )==BufferWrapper.TYPE_NULL )
				out.writeInt( FORMAT_NULL );
			    else {
				out.writeInt( FORMAT_FLOAT );
				FloatBufferWrapper bb = new FloatBufferWrapper( buf );
				float[] floats = new float[ bb.limit() ];
				bb.position( 0 );
				bb.get( floats );
				writeFloatArray( out, floats );
			    }
			} else if ( ((GeometryArray)node).getTexCoordRef3f(set)!=null ) {
			    out.writeInt( FORMAT_3F );
			    TexCoord3f[] tcoords = ((GeometryArray)node).getTexCoordRef3f(set);
			    float[] data = new float[ tcoords.length*3 ];
			    for (int i = 0 ; i < tcoords.length ; i++) {
				data[ i*3+0 ] = tcoords[i].x;
				data[ i*3+1 ] = tcoords[i].y;
				data[ i*3+2 ] = tcoords[i].z;
			    }
			    writeFloatArray( out, data );
			} else if ( ((GeometryArray)node).getTexCoordRefFloat(set)!=null ) {
			    out.writeInt( FORMAT_FLOAT );
			    writeFloatArray( out, ((GeometryArray)node).getTexCoordRefFloat(set) );
			} else out.writeInt( FORMAT_NULL );
		    } else {
			float[] textureCoords = new float[ vertexCount*3 ];
			((GeometryArray)node).getTextureCoordinates( set, 0, textureCoords );
			writeFloatArray( out, textureCoords );
		    }
		}
	    } else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_4)!=0) {
		//System.out.println("TEXTURE COORDS 4");
		for(int set=0; set<texCoordSetCount; set++) {
		    if ( byRef ) {
			if ( !(node instanceof IndexedGeometryArray) ) {
			    out.writeInt( ((GeometryArray)node).getInitialTexCoordIndex( set ) );
			}

			if ( nio ) {
			    J3DBuffer buf = ((GeometryArray)node).getTexCoordRefBuffer( set );
			    if ( BufferWrapper.getBufferType( buf )==BufferWrapper.TYPE_NULL )
				out.writeInt( FORMAT_NULL );
			    else {
				out.writeInt( FORMAT_FLOAT );
				FloatBufferWrapper bb = new FloatBufferWrapper( buf );
				float[] floats = new float[ bb.limit() ];
				bb.position( 0 );
				bb.get( floats );
				writeFloatArray( out, floats );
			    }
			// There is no TexCoordRef4f
			} else if ( ((GeometryArray)node).getTexCoordRefFloat(set)!=null ) {
			    out.writeInt( FORMAT_FLOAT );
			    writeFloatArray( out, ((GeometryArray)node).getTexCoordRefFloat(set) );
			} else out.writeInt( FORMAT_NULL );
		    } else {
			float[] textureCoords = new float[ vertexCount*4 ];
			((GeometryArray)node).getTextureCoordinates( set, 0, textureCoords );
			writeFloatArray( out, textureCoords );
		    }
		}
	    }
	}
    }

    public void readObject( DataInput in ) throws IOException {
        super.readObject( in );

	boolean nio = (vertexFormat & GeometryArray.USE_NIO_BUFFER) != 0;

	if ( (vertexFormat & GeometryArray.INTERLEAVED)!=0 ) {
	    if ( !(node instanceof IndexedGeometryArray) )  {
		((GeometryArray)node).setInitialVertexIndex( in.readInt() );
		if ( !(node instanceof GeometryStripArray) ) 
		    ((GeometryArray)node).setValidVertexCount( in.readInt() );
	    }
	    if ( nio ) {
		float[] floats = readFloatArray( in );
		ByteBufferWrapper b = 
		    ByteBufferWrapper.allocateDirect( floats.length*4 );
		FloatBufferWrapper f =
		    b.order( ByteOrderWrapper.nativeOrder() ).asFloatBuffer();
		f.put( floats );
		((GeometryArray)node).setInterleavedVertexBuffer( f.getJ3DBuffer() );
	    } else ((GeometryArray)node).setInterleavedVertices( readFloatArray( in ) );
	} else {
	    boolean byRef = (vertexFormat & GeometryArray.BY_REFERENCE) != 0;

	    // We MUST check for COLOR_4 before we check for COLOR_3,
	    // because the COLOR_3 test will pass for COLOR_4 objects
	    
	    if ( !(node instanceof IndexedGeometryArray) ) {
		if ( !byRef )
		    ((GeometryArray)node).setInitialVertexIndex( in.readInt() );
		if ( !(node instanceof GeometryStripArray) )
		    ((GeometryArray)node).setValidVertexCount( in.readInt() );
	    }

	    if ( (vertexFormat & GeometryArray.COLOR_4)==GeometryArray.COLOR_4 ) {
		if ( byRef ) {
		    if ( !(node instanceof IndexedGeometryArray) ) {
			((GeometryArray)node).setInitialColorIndex( in.readInt() );
		    }

		    if ( nio ) {
			switch( in.readInt() ) {
			case FORMAT_BYTE: {
			    byte[] bytes = new byte[ in.readInt() ];
			    in.readFully( bytes );
			    ByteBufferWrapper b =
				ByteBufferWrapper.allocateDirect( bytes.length );
			    b.put( bytes );
			    ((GeometryArray)node).setColorRefBuffer( b.getJ3DBuffer() );
			}
			break;
			case FORMAT_FLOAT: {
			    float[] floats = readFloatArray( in );
			    ByteBufferWrapper b = 
				ByteBufferWrapper.allocateDirect( floats.length*4 );
			    FloatBufferWrapper f =
				b.order( ByteOrderWrapper.nativeOrder() ).asFloatBuffer();
			    f.put( floats );
			    ((GeometryArray)node).setColorRefBuffer( f.getJ3DBuffer() );
			}
			break;
			}
		    } else {
			switch( in.readInt() ) {
			case FORMAT_4F: {
			    float[] data = readFloatArray( in );
			    Color4f[] colors = new Color4f[ data.length/4 ];
			    for (int i = 0 ; i < colors.length ; i++) {
				colors[i].x = data[ i*4+0 ];
				colors[i].y = data[ i*4+1 ];
				colors[i].z = data[ i*4+2 ];
				colors[i].w = data[ i*4+3 ];
			    }
			    ((GeometryArray)node).setColorRef4f( colors );
			}
			break;
			case FORMAT_FLOAT:
			    ((GeometryArray)node).setColorRefFloat( readFloatArray( in ) );
			break;
			case FORMAT_BYTE: {
			    byte[] data = new byte[ in.readInt() ];
			    in.readFully( data );
			    ((GeometryArray)node).setColorRefByte( data );
			}
			break;
			case FORMAT_4B: {
			    Color4b[] colors = new Color4b[ in.readInt() ];
			    byte[] data = new byte[ colors.length*4 ];
			    in.readFully( data );
			    for (int i = 0 ; i < colors.length ; i++) {
				colors[i].x = data[ i*4+0 ];
				colors[i].y = data[ i*4+1 ];
				colors[i].z = data[ i*4+2 ];
				colors[i].w = data[ i*4+3 ];
			    }
			    ((GeometryArray)node).setColorRef4b( colors );
			}
			break;
			}
		    }
		} else {
		    // Not by-reference
		    byte[] colors = new byte[ vertexCount*4 ];
		    in.readFully( colors );
		    ((GeometryArray)node).setColors( 0, colors );
		}
	    } else if ((vertexFormat & GeometryArray.COLOR_3)==GeometryArray.COLOR_3 ) {
		if ( byRef ) {
		    if ( !(node instanceof IndexedGeometryArray) ) {
			((GeometryArray)node).setInitialColorIndex( in.readInt() );
		    }

		    if ( nio ) {
			switch( in.readInt() ) {
			case FORMAT_BYTE: {
			    byte[] bytes = new byte[ in.readInt() ];
			    in.readFully( bytes );
			    ByteBufferWrapper b =
				ByteBufferWrapper.allocateDirect( bytes.length );
			    b.put( bytes );
			    ((GeometryArray)node).setColorRefBuffer( b.getJ3DBuffer() );
			}
			break;
			case FORMAT_FLOAT: {
			    float[] floats = readFloatArray( in );
			    ByteBufferWrapper b = 
				ByteBufferWrapper.allocateDirect( floats.length*4 );
			    FloatBufferWrapper f =
				b.order( ByteOrderWrapper.nativeOrder() ).asFloatBuffer();
			    f.put( floats );
			    ((GeometryArray)node).setColorRefBuffer( f.getJ3DBuffer() );
			}
			break;
			}
		    } else {
			switch( in.readInt() ) {
			case FORMAT_3F: {
			    float[] data = readFloatArray( in );
			    Color3f[] colors = new Color3f[ data.length/3 ];
			    for (int i = 0 ; i < colors.length ; i++) {
				colors[i].x = data[ i*3+0 ];
				colors[i].y = data[ i*3+1 ];
				colors[i].z = data[ i*3+2 ];
			    }
			    ((GeometryArray)node).setColorRef3f( colors );
			}
			break;
			case FORMAT_FLOAT:
			    ((GeometryArray)node).setColorRefFloat( readFloatArray( in ) );
			break;
			case FORMAT_BYTE: {
			    byte[] data = new byte[ in.readInt() ];
			    in.readFully( data );
			    ((GeometryArray)node).setColorRefByte( data );
			}
			break;
			case FORMAT_3B: {
			    Color3b[] colors = new Color3b[ in.readInt() ];
			    byte[] data = new byte[ colors.length*3 ];
			    in.readFully( data );
			    for (int i = 0 ; i < colors.length ; i++) {
				colors[i].x = data[ i*3+0 ];
				colors[i].y = data[ i*3+1 ];
				colors[i].z = data[ i*3+2 ];
			    }
			    ((GeometryArray)node).setColorRef3b( colors );
			}
			break;
			}
		    }
		} else {
		    // Not by-reference
		    byte[] colors = new byte[ vertexCount*3 ];
		    in.readFully( colors );
		    ((GeometryArray)node).setColors( 0, colors );
		}
	    }

	    if ((vertexFormat & GeometryArray.COORDINATES)!=0 ) {
		if ( byRef ) {
		    if ( !(node instanceof IndexedGeometryArray) ) {
			((GeometryArray)node).setInitialCoordIndex( in.readInt() );
		    }

		    if ( nio ) {
			switch( in.readInt() ) {
			case FORMAT_FLOAT: {
			    float[] floats = readFloatArray( in );
			    ByteBufferWrapper b = 
				ByteBufferWrapper.allocateDirect( floats.length*4 );
			    FloatBufferWrapper f =
				b.order( ByteOrderWrapper.nativeOrder() ).asFloatBuffer();
			    f.put( floats );
			    ((GeometryArray)node).setCoordRefBuffer( f.getJ3DBuffer() );
			}
			break;
			case FORMAT_DOUBLE: {
			    double[] doubles = readDoubleArray( in );
			    ByteBufferWrapper b = 
				ByteBufferWrapper.allocateDirect( doubles.length*4 );
			    DoubleBufferWrapper f =
				b.order( ByteOrderWrapper.nativeOrder() ).asDoubleBuffer();
			    f.put( doubles );
			    ((GeometryArray)node).setCoordRefBuffer( f.getJ3DBuffer() );
			}
			break;
			}
		    } else {
			switch( in.readInt() ) {
			case FORMAT_3F: {
			    float[] data = readFloatArray( in );
			    Point3f[] coords = new Point3f[ data.length/3 ];
			    for (int i = 0 ; i < coords.length ; i++) {
				coords[i].x = data[ i*3+0 ];
				coords[i].y = data[ i*3+1 ];
				coords[i].z = data[ i*3+2 ];
			    }
			    ((GeometryArray)node).setCoordRef3f( coords );
			}
			break;
			case FORMAT_3D: {
			    double[] data = readDoubleArray( in );
			    Point3d[] coords = new Point3d[ data.length/3 ];
			    for (int i = 0 ; i < coords.length ; i++) {
				coords[i].x = data[ i*3+0 ];
				coords[i].y = data[ i*3+1 ];
				coords[i].z = data[ i*3+2 ];
			    }
			    ((GeometryArray)node).setCoordRef3d( coords );
			}
			break;
			case FORMAT_FLOAT:
			    ((GeometryArray)node).setCoordRefFloat( readFloatArray( in ) );
			break;
			case FORMAT_DOUBLE:
			    ((GeometryArray)node).setCoordRefDouble( readDoubleArray( in ) );
			break;
			}
		    }
		} else {
		    // Not by-reference
		    float[] points = readFloatArray( in );
		    ((GeometryArray)node).setCoordinates( 0, points );
		}
	    }

	    if ((vertexFormat & GeometryArray.NORMALS)!=0) {
		if ( byRef ) {
		    if ( !(node instanceof IndexedGeometryArray) ) {
			((GeometryArray)node).setInitialNormalIndex( in.readInt() );
		    }

		    if ( nio ) {
			if ( in.readInt() == FORMAT_FLOAT ) {
			    float[] floats = readFloatArray( in );
			    ByteBufferWrapper b = 
				ByteBufferWrapper.allocateDirect( floats.length*4 );
			    FloatBufferWrapper f =
				b.order( ByteOrderWrapper.nativeOrder() ).asFloatBuffer();
			    f.put( floats );
			    ((GeometryArray)node).setNormalRefBuffer( f.getJ3DBuffer() );
			}
		    } else {
			switch( in.readInt() ) {
			case FORMAT_3F: {
			    float[] data = readFloatArray( in );
			    Vector3f[] norms = new Vector3f[ data.length/3 ];
			    for (int i = 0 ; i < norms.length ; i++) {
				norms[i].x = data[ i*3+0 ];
				norms[i].y = data[ i*3+1 ];
				norms[i].z = data[ i*3+2 ];
			    }
			    ((GeometryArray)node).setNormalRef3f( norms );
			}
			break;
			case FORMAT_FLOAT:
			    ((GeometryArray)node).setNormalRefFloat( readFloatArray( in ) );
			break;
			}
		    }
		} else {
		    // Not by-reference
		    float[] normals = readFloatArray( in );
		    ((GeometryArray)node).setNormals( 0, normals );
		}
	    }

	    if (((vertexFormat & GeometryArray.TEXTURE_COORDINATE_2) != 0) ||
		((vertexFormat & GeometryArray.TEXTURE_COORDINATE_3) != 0) ||
		((vertexFormat & GeometryArray.TEXTURE_COORDINATE_4) != 0)) {
		if ( byRef ) {
		    for(int set=0; set<texCoordSetCount; set++) {
			if ( !(node instanceof IndexedGeometryArray) ) {
			    ((GeometryArray)node).setInitialTexCoordIndex( set, in.readInt() );
			}

			if ( nio ) {
			    if ( in.readInt() == FORMAT_FLOAT ) {
				float[] floats = readFloatArray( in );
				ByteBufferWrapper b = 
				    ByteBufferWrapper.allocateDirect( floats.length*4 );
				FloatBufferWrapper f = b.order(
				    ByteOrderWrapper.nativeOrder() ).asFloatBuffer();
				f.put( floats );
				((GeometryArray)node).setTexCoordRefBuffer( set,
				    f.getJ3DBuffer() );
			    }
			} else {
			    switch( in.readInt() ) {
			    case FORMAT_2F: {
				float[] data = readFloatArray( in );
				TexCoord2f[] tcoords = new TexCoord2f[ data.length/2 ];
				for (int i = 0 ; i < tcoords.length ; i++) {
				    tcoords[i].x = data[ i*2+0 ];
				    tcoords[i].y = data[ i*2+1 ];
				}
				((GeometryArray)node).setTexCoordRef2f( set, tcoords );
			    }
			    break;
			    case FORMAT_3F: {
				float[] data = readFloatArray( in );
				TexCoord3f[] tcoords = new TexCoord3f[ data.length/3 ];
				for (int i = 0 ; i < tcoords.length ; i++) {
				    tcoords[i].x = data[ i*3+0 ];
				    tcoords[i].y = data[ i*3+1 ];
				    tcoords[i].z = data[ i*3+2 ];
				}
				((GeometryArray)node).setTexCoordRef3f( set, tcoords );
			    }
			    break;
			    case FORMAT_FLOAT:
				float[] tcoords = readFloatArray( in );
				((GeometryArray)node).setTexCoordRefFloat( set, tcoords );
			    break;
			    }
			}
		    }
		} else {
		    // Not by-reference
		    for(int set=0; set<texCoordSetCount; set++) {
			float[] textureCoords = readFloatArray( in );
			((GeometryArray)node).setTextureCoordinates( set, 0, textureCoords );
		    }
		}
	    }
	}
    }

    protected void writeConstructorParams( DataOutput out ) throws
								IOException {
	vertexCount = ((GeometryArray)node).getVertexCount();
	vertexFormat = ((GeometryArray)node).getVertexFormat();
        texCoordSetCount = ((GeometryArray)node).getTexCoordSetCount();
        texCoordSetMap = new int[ ((GeometryArray)node).getTexCoordSetMapLength() ];
        
        ((GeometryArray)node).getTexCoordSetMap( texCoordSetMap );
        
	out.writeInt(vertexCount);
	out.writeInt(vertexFormat);
        out.writeInt( texCoordSetCount );
        out.writeInt( texCoordSetMap.length );
        for(int i=0; i<texCoordSetMap.length; i++)
            out.writeInt( texCoordSetMap[i] );
        super.writeConstructorParams( out );
    }

    protected void readConstructorParams( DataInput in ) throws
							IOException {
       // Load VertexCount and format first beause
       // SceneGraphObjectState will call createNode which
       // requires them
       vertexCount = in.readInt();
       vertexFormat = in.readInt();
       texCoordSetCount = in.readInt();
       texCoordSetMap = new int[in.readInt()];
       for(int i=0; i<texCoordSetMap.length; i++)
           texCoordSetMap[i] = in.readInt();
       
       super.readConstructorParams( in );
    }
    
    protected void writeDoubleArray( DataOutput out, double[] array ) throws IOException {

        // Writing the array into a ByteArray in memory and then dumping
        // the byte array to DataOutput with a single call is MUCH quicker
        // than writing each double to DataOutput.
        
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream( byteStream );

        dataOut.writeInt( array.length );
        for(int i=0; i<array.length; i++)
            dataOut.writeDouble( array[i] );
        dataOut.close();
        
        out.writeInt( byteStream.size() );
        out.write( byteStream.toByteArray() );
    }
    
    protected double[] readDoubleArray( DataInput in ) throws IOException {
        byte[] buffer = new byte[ in.readInt() ];
        in.readFully( buffer );
        ByteArrayInputStream byteStream = new ByteArrayInputStream( buffer );
        DataInputStream dataIn = new DataInputStream( byteStream );
        
        double[] array = new double[ dataIn.readInt() ];
        for(int i=0; i<array.length; i++)
            array[i] = dataIn.readDouble();
        
        dataIn.close();
        
        return array;
    }
    
    protected void writeFloatArray( DataOutput out, float[] array ) throws IOException {

        // Writing the array into a ByteArray in memory and then dumping
        // the byte array to DataOutput with a single call is MUCH quicker
        // than writing each float to DataOutput.
        
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream( byteStream );

        dataOut.writeInt( array.length );
        for(int i=0; i<array.length; i++)
            dataOut.writeFloat( array[i] );
        dataOut.close();
        
        out.writeInt( byteStream.size() );
        out.write( byteStream.toByteArray() );
    }
    
    protected float[] readFloatArray( DataInput in ) throws IOException {
        byte[] buffer = new byte[ in.readInt() ];
        in.readFully( buffer );
        ByteArrayInputStream byteStream = new ByteArrayInputStream( buffer );
        DataInputStream dataIn = new DataInputStream( byteStream );
        
        float[] array = new float[ dataIn.readInt() ];
        for(int i=0; i<array.length; i++)
            array[i] = dataIn.readFloat();
        
        dataIn.close();
        
        return array;
    }
}
