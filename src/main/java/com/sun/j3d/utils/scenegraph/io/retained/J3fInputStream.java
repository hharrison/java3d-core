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

import java.io.DataInputStream;
import java.io.IOException;

/**
 * J3fInputStream class for SceneGraph I/O.
 */
public class J3fInputStream implements java.io.DataInput {

    private PositionInputStream positionInputStream;
    private DataInputStream dataInputStream;
    
    /** Creates new J3fInputStream */
    public J3fInputStream( java.io.InputStream stream ) {
        positionInputStream = new PositionInputStream( stream );
        dataInputStream = new DataInputStream( positionInputStream );
    }
    
    /**
     * Move the file pointer to the specified position.
     * The position MUST be greater or equal to the current position
     */
    public void seekForward( long position ) throws IOException {
        positionInputStream.seekForward( position );
    }
    
    public long getFilePointer() {
        return positionInputStream.getFilePointer();
    }

    public int readUnsignedShort() throws java.io.IOException {
        return dataInputStream.readUnsignedShort();
    }
    
    public void readFully(byte[] p1) throws java.io.IOException {
        dataInputStream.readFully(p1);
    }
    
    public char readChar() throws java.io.IOException {
        return dataInputStream.readChar();
    }
    
    public int readUnsignedByte() throws java.io.IOException {
        return dataInputStream.readUnsignedByte();
    }
    
    public int readInt() throws java.io.IOException {
        return dataInputStream.readInt();
    }
    
    public short readShort() throws java.io.IOException {
        return dataInputStream.readShort();
    }
    
    public float readFloat() throws java.io.IOException {
        return dataInputStream.readFloat();
    }
    
    public void readFully(byte[] p1,int p2,int p3) throws java.io.IOException {
        dataInputStream.readFully( p1, p2, p3 );
    }
    
    public boolean readBoolean() throws java.io.IOException {
        return dataInputStream.readBoolean();
    }
    
    public int skipBytes(int p1) throws java.io.IOException {
        return dataInputStream.skipBytes(p1);
    }
    
    public double readDouble() throws java.io.IOException {
        return dataInputStream.readDouble();
    }
    
    public long readLong() throws java.io.IOException {
        return dataInputStream.readLong();
    }
    
    public java.lang.String readLine() throws java.io.IOException {
        return dataInputStream.readLine();
    }
    
    public byte readByte() throws java.io.IOException {
        return dataInputStream.readByte();
    }
    
    public java.lang.String readUTF() throws java.io.IOException {
        return dataInputStream.readUTF();
    }
    
}
