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
import java.io.DataInput;
import java.io.DataOutput;
import javax.vecmath.Point3d;
import javax.media.j3d.CompressedGeometry;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.CompressedGeometryHeader;
import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;

public class CompressedGeometryState extends GeometryState {
    
    private byte[] bytes;
    private boolean isByReference;
    private CompressedGeometryHeader header;
    
    public CompressedGeometryState(SymbolTableData symbol,Controller control) {
        super( symbol, control );
        
    }
    
    public void writeConstructorParams( DataOutput out ) throws IOException {
        super.writeConstructorParams( out );
        
        out.writeBoolean( ((CompressedGeometry)node).isByReference() );
        
        int size = ((CompressedGeometry)node).getByteCount();
        out.writeInt( size );
        bytes = new byte[ size ];
        ((CompressedGeometry)node).getCompressedGeometry( bytes );
        out.write( bytes );
        
        header = new CompressedGeometryHeader();
        ((CompressedGeometry)node).getCompressedGeometryHeader( header );
        writeCompressedGeometryHeader( out );
    }
    
    public void readConstructorParams( DataInput in ) throws IOException {
        super.readConstructorParams( in );
        
        isByReference = in.readBoolean();
        bytes = new byte[ in.readInt() ];
        in.readFully( bytes );
        
        header = new CompressedGeometryHeader();
        readCompressedGeometryHeader( in );
    }
    
    private void writeCompressedGeometryHeader( DataOutput out ) throws IOException {
        out.writeInt( header.majorVersionNumber );
        out.writeInt( header.minorVersionNumber );
        out.writeInt( header.minorMinorVersionNumber );
        out.writeInt( header.bufferType );
        out.writeInt( header.bufferDataPresent );
        out.writeInt( header.size );
        out.writeInt( header.start );
	if (header.lowerBound == null) {
	    control.writePoint3d(out, new Point3d(-1, -1, -1));
	} else {
	    control.writePoint3d( out, header.lowerBound );
	}
	if (header.upperBound == null) {
	    control.writePoint3d(out, new Point3d(1, 1, 1));
	} else {
	    control.writePoint3d( out, header.upperBound );
	}
    }
    
    private void readCompressedGeometryHeader( DataInput in ) throws IOException {
        header.majorVersionNumber = in.readInt();
        header.minorVersionNumber = in.readInt();
        header.minorMinorVersionNumber = in.readInt();
        header.bufferType = in.readInt();
        header.bufferDataPresent = in.readInt();
        header.size = in.readInt();
        header.start = in.readInt();
        header.lowerBound = control.readPoint3d( in );
	if ((header.lowerBound.x == -1) &&
	    (header.lowerBound.y == -1) &&
	    (header.lowerBound.z == -1)) {
	    header.lowerBound = null;
	}
        header.upperBound = control.readPoint3d( in );
	if ((header.upperBound.x == 1) &&
	    (header.upperBound.y == 1) &&
	    (header.upperBound.z == 1)) {
	    header.upperBound = null;
	}
    }
     
    public SceneGraphObject createNode( Class j3dClass ) {
        
        return createNode( j3dClass, new Class[] { CompressedGeometryHeader.class,
                                                    bytes.getClass(),
                                                    Boolean.TYPE },
                                      new Object[] { header,
                                                     bytes,
                                                     new Boolean(isByReference) } );
    }
    
    protected SceneGraphObject createNode() {
        return new CompressedGeometry( header, bytes, isByReference );
    }

}
