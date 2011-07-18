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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.Alpha;

public class AlphaState extends NodeComponentState {
    
    public AlphaState(SymbolTableData symbol,Controller control) {
        super(symbol, control);
    }
    
    public void writeObject( DataOutput out ) throws IOException {
        super.writeObject( out );
        Alpha alpha = (Alpha)node;
        out.writeLong( alpha.getAlphaAtOneDuration() );
        out.writeLong( alpha.getAlphaAtZeroDuration() );
        out.writeLong( alpha.getDecreasingAlphaDuration() );
        out.writeLong( alpha.getDecreasingAlphaRampDuration() );
        out.writeLong( alpha.getIncreasingAlphaDuration() );
        out.writeLong( alpha.getIncreasingAlphaRampDuration() );
        out.writeInt( alpha.getLoopCount() );
        out.writeInt( alpha.getMode() );
        out.writeLong( alpha.getPhaseDelayDuration() );
        out.writeLong( alpha.getStartTime() );
        out.writeLong( alpha.getTriggerTime() );
	out.writeLong( alpha.getPauseTime() );
    }
    
    public void readObject( DataInput in ) throws IOException {
        super.readObject( in );
        Alpha alpha = (Alpha)node;
        alpha.setAlphaAtOneDuration( in.readLong() );
        alpha.setAlphaAtZeroDuration( in.readLong() );
        alpha.setDecreasingAlphaDuration( in.readLong() );
        alpha.setDecreasingAlphaRampDuration( in.readLong() );
        alpha.setIncreasingAlphaDuration( in.readLong() );
        alpha.setIncreasingAlphaRampDuration( in.readLong() );
        alpha.setLoopCount( in.readInt() );
        alpha.setMode( in.readInt() );
        alpha.setPhaseDelayDuration( in.readLong() );
        alpha.setStartTime( in.readLong() );
        alpha.setTriggerTime( in.readLong() );
	long x = in.readLong();
	if (x!=0L) alpha.pause(x);
    }
    
    protected SceneGraphObject createNode() {
        return new Alpha();
    }
}

