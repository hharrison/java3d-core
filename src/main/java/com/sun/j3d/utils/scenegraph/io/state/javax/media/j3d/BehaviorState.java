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
import javax.media.j3d.Behavior;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.BoundingLeaf;
import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;

public class BehaviorState extends LeafState {

    private int boundingLeaf;
    
    public BehaviorState(SymbolTableData symbol,Controller control) {
        super( symbol, control );
        
    }
    
    protected SceneGraphObject createNode( String className ) {
        SceneGraphObject ret;
        try {
            ret = super.createNode( className );
        } catch( com.sun.j3d.utils.scenegraph.io.retained.SGIORuntimeException e ) {
            ret = new com.sun.j3d.utils.scenegraph.io.UnresolvedBehavior();
        }
        
        return ret;
    }

    public void writeObject( DataOutput out ) throws IOException {
        super.writeObject( out );
        Behavior beh = (Behavior)node;

        out.writeBoolean( beh.getEnable() );
        out.writeInt( control.getSymbolTable().addReference( beh.getSchedulingBoundingLeaf() ) );
        
        control.writeBounds( out, beh.getSchedulingBounds() );
        
	// We had a lot of dicussion about this - may want to expand support
	// in future versions, but for now just save and restore scheduling
	// interval
	out.writeInt( beh.getSchedulingInterval() );
    }
    
    public void readObject( DataInput in ) throws IOException {
        super.readObject( in );
        Behavior beh = (Behavior)node;
        
        beh.setEnable( in.readBoolean() );
        boundingLeaf = in.readInt();
        beh.setSchedulingBounds( control.readBounds( in ) );
	beh.setSchedulingInterval( in.readInt() );
    }
    
    public void buildGraph() {
        ((Behavior)node).setSchedulingBoundingLeaf( (BoundingLeaf)control.getSymbolTable().getJ3dNode( boundingLeaf ));
        super.buildGraph(); // Must be last call in method
    }

}
