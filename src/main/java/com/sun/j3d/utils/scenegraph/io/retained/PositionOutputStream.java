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

class PositionOutputStream extends java.io.OutputStream {
    
    private long pos = 0;
    private java.io.OutputStream stream;
    
    public PositionOutputStream( java.io.OutputStream stream ) {
        this.stream = stream;
    }
    
    public void write(int p1) throws IOException {
        pos++;
        stream.write(p1);
    }
    
    public void write( byte[] b ) throws IOException {
        pos+= b.length;
        stream.write( b );
    }
    
    public void write( byte[] b, int off, int len ) throws IOException {
        pos+= len;
        stream.write( b, off, len );
    }
    
    /**
     * Move the file pointer to the specified position.
     * The position MUST be greater or equal to the current position
     */
    public void seekForward( long position ) throws IOException {
        if (pos>position)
            throw new SGIORuntimeException( "Seeking Backward "+pos +"  "+position );
        else
            for(int i=0; i< (int)(position-pos); i++)
                stream.write(0);
        
        pos = position;
    }
    
    public long getFilePointer() {
        return pos;
    }
}
