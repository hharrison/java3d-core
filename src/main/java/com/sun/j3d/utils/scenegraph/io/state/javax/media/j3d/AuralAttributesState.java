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
import javax.media.j3d.AuralAttributes;
import javax.media.j3d.SceneGraphObject;
import javax.vecmath.Vector3f;
import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;

public class AuralAttributesState extends NodeComponentState {

    public AuralAttributesState(SymbolTableData symbol,Controller control) {
        super( symbol, control );
        
    }
    
    public void writeObject( DataOutput out ) throws IOException {
        super.writeObject( out );
        
        out.writeFloat( ((AuralAttributes)node).getAttributeGain() );
        
        float[] distance = new float[ ((AuralAttributes)node).getDistanceFilterLength() ];
        float[] cutoff = new float[ distance.length ];
        
        ((AuralAttributes)node).getDistanceFilter( distance, cutoff );
        out.writeInt( distance.length );
        for(int i=0; i<distance.length; i++) {
            out.writeFloat( distance[i] );
            out.writeFloat( cutoff[i] );
        }
        
        out.writeFloat( ((AuralAttributes)node).getFrequencyScaleFactor() );
        out.writeFloat( ((AuralAttributes)node).getReflectionCoefficient() );
        control.writeBounds( out, ((AuralAttributes)node).getReverbBounds() );
        out.writeFloat( ((AuralAttributes)node).getReverbDelay() );
        out.writeInt( ((AuralAttributes)node).getReverbOrder() );
        out.writeFloat( ((AuralAttributes)node).getRolloff() );
        out.writeFloat( ((AuralAttributes)node).getVelocityScaleFactor() );
	out.writeFloat( ((AuralAttributes)node).getReflectionDelay() );
	out.writeFloat( ((AuralAttributes)node).getReverbCoefficient() );
	out.writeFloat( ((AuralAttributes)node).getDecayTime() );
	out.writeFloat( ((AuralAttributes)node).getDecayFilter() );
	out.writeFloat( ((AuralAttributes)node).getDiffusion() );
	out.writeFloat( ((AuralAttributes)node).getDensity() );
    }
    
    public void readObject( DataInput in ) throws IOException {
        super.readObject( in );
                    
        ((AuralAttributes)node).setAttributeGain( in.readFloat() );
        
        float[] distance = new float[ in.readInt() ];
        float[] cutoff = new float[ distance.length ];
        for(int i=0; i<distance.length; i++) {
            distance[i] = in.readFloat();
            cutoff[i] = in.readFloat();
        }
        ((AuralAttributes)node).setDistanceFilter( distance, cutoff );
        
        ((AuralAttributes)node).setFrequencyScaleFactor( in.readFloat() );
        ((AuralAttributes)node).setReflectionCoefficient( in.readFloat() );
        ((AuralAttributes)node).setReverbBounds( control.readBounds(in) );
        ((AuralAttributes)node).setReverbDelay( in.readFloat() );
        ((AuralAttributes)node).setReverbOrder( in.readInt() );
        ((AuralAttributes)node).setRolloff( in.readFloat() );
        ((AuralAttributes)node).setVelocityScaleFactor( in.readFloat() );
	((AuralAttributes)node).setReflectionDelay( in.readFloat() );
	((AuralAttributes)node).setReverbCoefficient( in.readFloat() );
	((AuralAttributes)node).setDecayTime( in.readFloat() );
	((AuralAttributes)node).setDecayFilter( in.readFloat() );
	((AuralAttributes)node).setDiffusion( in.readFloat() );
	((AuralAttributes)node).setDensity( in.readFloat() );
    }
    
    protected SceneGraphObject createNode() {
        return new AuralAttributes();
    }
    
}
