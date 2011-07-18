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

import java.io.DataOutput;
import java.io.DataInput;
import java.io.IOException;
import javax.media.j3d.SceneGraphObject;
import com.sun.j3d.utils.scenegraph.io.state.javax.media.j3d.SceneGraphObjectState;

/**
 * Encapsulates all the data for a node which is stored in the Symbol table
 */
public class SymbolTableData extends java.lang.Object {

    public int nodeID;
    public SceneGraphObjectState nodeState;
    public SceneGraphObject j3dNode;
    public int referenceCount;
    public long filePosition;
    public boolean isNodeComponent;
    public int branchGraphID;
    public long branchGraphFilePointer;
    public boolean graphBuilt = false;
    
    /** Creates new SymbolTableData */
    public SymbolTableData( int nodeID, 
                            SceneGraphObject j3dNode,
                            SceneGraphObjectState nodeState,
                            int branchGraphID ) {
        this.nodeID = nodeID;
        this.j3dNode = j3dNode;
        this.nodeState = nodeState;
        this.branchGraphID = branchGraphID;
        this.referenceCount = 1;
        this.isNodeComponent = false;
    }

    public void writeObject( DataOutput out ) throws IOException {
        out.writeInt( nodeID );
        out.writeInt( referenceCount );
        out.writeLong( filePosition );
        out.writeBoolean( isNodeComponent );
        out.writeInt( branchGraphID );
        out.writeLong( branchGraphFilePointer );
    }
    
    public void readObject( DataInput in ) throws IOException {
        nodeID = in.readInt();
        referenceCount = in.readInt();
        filePosition = in.readLong();
        isNodeComponent = in.readBoolean();
        branchGraphID = in.readInt();
        branchGraphFilePointer = in.readLong();
    }
    
    public final int getNodeID() {
        return nodeID;
    }
    
    public final SceneGraphObjectState getNodeState() {
        return nodeState;
    }
    
    public final void setNodeState( SceneGraphObjectState state ) {
        nodeState = state;
    }
    
    public final SceneGraphObject getJ3dNode() {
        return j3dNode;
    }
    
    public final long getFilePosition() {
        return filePosition;
    }
    
    public final int getReferenceCount() {
        return referenceCount;
    }
    
    public final void incrementReferenceCount() {
        referenceCount++;
    }
    
    public final boolean isNodeComponent() {
        return isNodeComponent;
    }
    
    public String toString() {
        return new String(nodeID +" "+ filePosition+"  "+j3dNode);
    }
}
