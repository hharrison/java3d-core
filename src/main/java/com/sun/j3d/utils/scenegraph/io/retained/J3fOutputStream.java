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

package com.sun.j3d.utils.scenegraph.io.retained;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * J3fOutputStream class for SceneGraph I/O.
 */
public class J3fOutputStream implements java.io.DataOutput {

    private PositionOutputStream positionOutputStream;
    private DataOutputStream dataOutputStream;
    
    /** Creates new J3fInputStream */
    public J3fOutputStream( java.io.OutputStream stream ) {
        positionOutputStream = new PositionOutputStream( stream );
        dataOutputStream = new DataOutputStream( positionOutputStream );
    }
    
    /**
     * Move the file pointer to the specified position.
     * The position MUST be greater or equal to the current position
     */
    public void seekForward( long position ) throws IOException {
        positionOutputStream.seekForward( position );
    }
    
    public long getFilePointer() {
        return positionOutputStream.getFilePointer();
    }

    public void write(byte[] p1,int p2,int p3) throws java.io.IOException {
        dataOutputStream.write( p1, p2, p3 );
    }    

    public void writeFloat(float p1) throws java.io.IOException {
        dataOutputStream.writeFloat(p1);
    }    
    
    public void write(int p1) throws java.io.IOException {
        dataOutputStream.write(p1 );
    }
    
    public void writeShort(int p1) throws java.io.IOException {
        dataOutputStream.writeShort( p1 );
    }
    
    public void writeBytes(java.lang.String p1) throws java.io.IOException {
        dataOutputStream.writeBytes( p1 );
    }
    
    public void writeChar(int p1) throws java.io.IOException {
        dataOutputStream.writeChar( p1 );
    }
    
    public void writeByte(int p1) throws java.io.IOException {
        dataOutputStream.writeByte( p1 );
    }
    
    public void writeLong(long p1) throws java.io.IOException {
        dataOutputStream.writeLong( p1 );
    }
    
    public void writeBoolean(boolean p1) throws java.io.IOException {
        dataOutputStream.writeBoolean( p1 );
    }
    
    public void writeUTF(java.lang.String p1) throws java.io.IOException {
        dataOutputStream.writeUTF( p1 );
    }
    
    public void writeInt(int p1) throws java.io.IOException {
        dataOutputStream.writeInt( p1 );
    }
    
    public void writeChars(java.lang.String p1) throws java.io.IOException {
        dataOutputStream.writeChars( p1 );
    }
    
    public void write(byte[] p1) throws java.io.IOException {
        dataOutputStream.write( p1 );
    }
    
    public void writeDouble(double p1) throws java.io.IOException {
        dataOutputStream.writeDouble( p1 );
    }
    
}
