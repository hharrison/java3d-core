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
import javax.media.j3d.IndexedGeometryArray;
import javax.media.j3d.IndexedGeometryStripArray;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.SceneGraphObject;
import javax.vecmath.*;
import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;

public abstract class IndexedGeometryArrayState extends GeometryArrayState {

    protected int indexCount;
    
    public IndexedGeometryArrayState(SymbolTableData symbol,Controller control) {
	super( symbol, control );
    }

    public void writeObject( DataOutput out ) throws IOException {
        super.writeObject( out );
        
        int[] indices = new int[ ((IndexedGeometryArray)node).getIndexCount() ];

	boolean coordOnly = (vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) !=0;
        
	if ( (((vertexFormat & GeometryArray.COLOR_3)!=0) ||
              ((vertexFormat & GeometryArray.COLOR_4)!=0)) && !coordOnly ) {
	    ((IndexedGeometryArray)node).getColorIndices( 0, indices );
	    writeIntArray( out, indices );
	}

	if ( (vertexFormat & GeometryArray.COORDINATES)!=0 ) {
	    ((IndexedGeometryArray)node).getCoordinateIndices( 0, indices );
	    writeIntArray( out, indices );
	}

	if ( ((vertexFormat & GeometryArray.NORMALS)!=0) && !coordOnly) {
	    ((IndexedGeometryArray)node).getNormalIndices( 0, indices );
	    writeIntArray( out, indices );
	}

	if ( (((vertexFormat & GeometryArray.TEXTURE_COORDINATE_2)!=0) ||
	      ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_3)!=0) ||
              ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_4)!=0)) && !coordOnly ) {            
            for(int i=0; i<((IndexedGeometryArray)node).getTexCoordSetCount(); i++) {
	        ((IndexedGeometryArray)node).getTextureCoordinateIndices( i, 0, indices );
	        writeIntArray( out, indices );
            }
	}

	if ( !(node instanceof IndexedGeometryStripArray) ) {
	  out.writeInt( ((IndexedGeometryArray)node).getValidIndexCount() );
	}
	out.writeInt( ((IndexedGeometryArray)node).getInitialIndexIndex() );
    }

    public void readObject( DataInput in ) throws IOException {
        super.readObject( in );
      
        int[] indices = new int[indexCount];

	boolean coordOnly = (vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) != 0;
	  
	if ( (((vertexFormat & GeometryArray.COLOR_3)!=0) ||
	      ((vertexFormat & GeometryArray.COLOR_4)!=0)) && !coordOnly ) {
	    readIntArray( in, indices );
	    ((IndexedGeometryArray)node).setColorIndices( 0, indices );
	}

	if ( (vertexFormat & GeometryArray.COORDINATES)!=0 ) {
	    readIntArray( in, indices );
	    ((IndexedGeometryArray)node).setCoordinateIndices( 0, indices );
	}

	if ( ((vertexFormat & GeometryArray.NORMALS)!=0) && !coordOnly ) {
	    readIntArray( in, indices );
	    ((IndexedGeometryArray)node).setNormalIndices( 0, indices );
	}

	if ( (((vertexFormat & GeometryArray.TEXTURE_COORDINATE_2)!=0) ||
	      ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_3)!=0) ||
	      ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_4)!=0)) && !coordOnly ) {
	    for(int i=0; i<texCoordSetCount; i++) {
		 readIntArray( in, indices );
		 ((IndexedGeometryArray)node).setTextureCoordinateIndices( i, 0, indices );
	    }
	}

	if ( !(node instanceof IndexedGeometryStripArray) ) {
	    ((IndexedGeometryArray)node).setValidIndexCount( in.readInt() );
	}
	((IndexedGeometryArray)node).setInitialIndexIndex( in.readInt() );
    }

    protected void writeConstructorParams( DataOutput out ) throws
								IOException {
        super.writeConstructorParams( out );
        out.writeInt( ((IndexedGeometryArray)node).getIndexCount() );
    }

    protected void readConstructorParams( DataInput in ) throws
							IOException {
        super.readConstructorParams( in );
        indexCount = in.readInt();
    }
    
    protected void writeIntArray( DataOutput out, int[] array ) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream( byteStream );

        for(int i=0; i<array.length; i++)
            dataOut.writeInt( array[i] );
        dataOut.close();
        
        out.writeInt( byteStream.size() );
        out.write( byteStream.toByteArray() );
    }
    
    private void readIntArray( DataInput in, int[] array ) throws IOException {
        byte[] buffer = new byte[ in.readInt() ];
        in.readFully( buffer );
        ByteArrayInputStream byteStream = new ByteArrayInputStream( buffer );
        DataInputStream dataIn = new DataInputStream( byteStream );
        
        for(int i=0; i<array.length; i++)
            array[i] = dataIn.readInt();
        
        dataIn.close();
    }

}
