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
import javax.media.j3d.Sound;
import javax.media.j3d.MediaContainer;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.BoundingLeaf;
import javax.media.j3d.SceneGraphObject;
import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;

public abstract class SoundState extends LeafState {

    private int boundingLeaf;
    private int mediaContainer;
    
    public SoundState(SymbolTableData symbol,Controller control) {
        super( symbol, control );
        
        if (node!=null) {
            boundingLeaf = control.getSymbolTable().addReference( ((Sound)node).getSchedulingBoundingLeaf() );
            mediaContainer = control.getSymbolTable().addReference( ((Sound)node).getSoundData() );
        }
    }
    
    public void writeObject( DataOutput out ) throws IOException {
        super.writeObject( out );
        
        Sound sound = (Sound)node;
        
        out.writeBoolean( sound.getContinuousEnable() );
        out.writeBoolean( sound.getEnable() );
        out.writeFloat( sound.getInitialGain() );
        out.writeInt( sound.getLoop() );
        out.writeFloat( sound.getPriority() );
        out.writeBoolean( sound.getReleaseEnable() );
        out.writeInt( boundingLeaf );
        control.writeBounds( out, sound.getSchedulingBounds() );
        out.writeInt( mediaContainer );
	out.writeBoolean( sound.getMute() );
	out.writeBoolean( sound.getPause() );
	out.writeFloat( sound.getRateScaleFactor() );
    }
    
    public void readObject( DataInput in ) throws IOException {
        super.readObject( in );
                        
        Sound sound = (Sound)node;
        
        sound.setContinuousEnable( in.readBoolean() );
        sound.setEnable( in.readBoolean() );
        sound.setInitialGain( in.readFloat() );
        sound.setLoop( in.readInt() );
        sound.setPriority( in.readFloat() );
        sound.setReleaseEnable( in.readBoolean() );
        boundingLeaf = in.readInt();
        sound.setSchedulingBounds( control.readBounds( in ));
        mediaContainer = in.readInt();
	sound.setMute( in.readBoolean() );
	sound.setPause( in.readBoolean() );
	sound.setRateScaleFactor( in.readFloat() );
    }
    
    /**
     * Called when this component reference count is incremented.
     * Allows this component to update the reference count of any components
     * that it references.
     */
    public void addSubReference() {
        control.getSymbolTable().incNodeComponentRefCount( mediaContainer );
    }
    
    public void buildGraph() {
        
        ((Sound)node).setSchedulingBoundingLeaf( (BoundingLeaf)control.getSymbolTable().getJ3dNode( boundingLeaf ));
        ((Sound)node).setSoundData( (MediaContainer)control.getSymbolTable().getJ3dNode( mediaContainer ));
        super.buildGraph(); // Must be last call in method
    }
    

}
