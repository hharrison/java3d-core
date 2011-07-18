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
import javax.media.j3d.ConeSound;
import javax.media.j3d.SceneGraphObject;
import javax.vecmath.Vector3f;
import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;

public class ConeSoundState extends PointSoundState {

    public ConeSoundState(SymbolTableData symbol,Controller control) {
        super( symbol, control );
        
    }
    
    public void writeObject( DataOutput out ) throws IOException {
        super.writeObject( out );
        
        float[] distanceAtten = new float[ ((ConeSound)node).getAngularAttenuationLength() ];
        float[] gainAtten = new float[ distanceAtten.length ];
        float[] filterAtten = new float[ distanceAtten.length ];
        float[] backDistance = new float[ distanceAtten.length ];
        float[] backGain = new float[ distanceAtten.length ];
        float[] frontDistance = new float[ distanceAtten.length ];
        float[] frontGain = new float[ distanceAtten.length ];
        
        ((ConeSound)node).getDistanceGain( frontDistance, frontGain, backDistance, backGain );
        ((ConeSound)node).getAngularAttenuation( distanceAtten, gainAtten, filterAtten );
        out.writeInt( distanceAtten.length );
        for(int i=0; i<distanceAtten.length; i++) {
            out.writeFloat( distanceAtten[i] );
            out.writeFloat( gainAtten[i] );
            out.writeFloat( filterAtten[i] );
            out.writeFloat( backDistance[i] );
            out.writeFloat( backGain[i] );
            
            // We don't need to write the front distance or gain as these
            // will be handled by the superclass
        }
        
        Vector3f direction = new Vector3f();
        
        ((ConeSound)node).getDirection( direction );
        control.writeVector3f( out, direction );
    }
    
    public void readObject( DataInput in ) throws IOException {
        super.readObject( in );
                    
        float[] distanceAtten = new float[ in.readInt() ];
        float[] gainAtten = new float[ distanceAtten.length ];
        float[] filterAtten = new float[ distanceAtten.length ];
        float[] backDistance = new float[ distanceAtten.length ];
        float[] backGain = new float[ distanceAtten.length ];
        
        for(int i=0; i<distanceAtten.length; i++) {
            distanceAtten[i] = in.readFloat();
            gainAtten[i] = in.readFloat();
            filterAtten[i] = in.readFloat();
            backDistance[i] = in.readFloat();
            backGain[i] = in.readFloat();
        }
        
        ((ConeSound)node).setBackDistanceGain( backDistance, backGain );
        ((ConeSound)node).setAngularAttenuation( distanceAtten, gainAtten, filterAtten );
        
        ((ConeSound)node).setDirection( control.readVector3f( in ));
    }
    
    protected SceneGraphObject createNode() {
        return new ConeSound();
    }

    
}
