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

import java.io.*;
import java.util.Enumeration;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.SceneGraphObject;
import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;

public class GroupState extends NodeState {
    
    protected SceneGraphObjectState[] groupChildren;        // State classes for all the children during load

    public GroupState( SymbolTableData symbol, Controller control ) {
        super(symbol, control);        
    }

  public void writeObject( DataOutput out) throws IOException {
    super.writeObject( out );

    control.writeBounds( out, ((Group)node).getCollisionBounds() );
    
    int numChildren;
    if (checkProcessChildren())
        numChildren = ((Group)node).numChildren();
    else
        numChildren = 0;
    
    out.writeInt( numChildren );
        
    for(int i=0; i<numChildren; i++ ) {
        control.writeObject( out, control.createState( ((Group)node).getChild(i) ) );
    }
    
    out.writeBoolean( ((Group)node).getAlternateCollisionTarget() );
  }

  public void readObject( DataInput in ) throws IOException {
      super.readObject( in );

    ((Group)node).setCollisionBounds( control.readBounds( in ));

    int numChildren = in.readInt();
    groupChildren = new SceneGraphObjectState[ numChildren ];
    for(int i=0; i<numChildren; i++ ) {
        groupChildren[i] = control.readObject( in );
        ((Group)node).addChild( (Node)groupChildren[i].getNode() );
    }
    
    ((Group)node).setAlternateCollisionTarget( in.readBoolean() );
  }

  private boolean checkProcessChildren() {
      if (node instanceof com.sun.j3d.utils.scenegraph.io.SceneGraphIO)
          return ((com.sun.j3d.utils.scenegraph.io.SceneGraphIO)node).saveChildren();
      else
          return processChildren();
  }
  
  /**
   * Returns true if the groups children should be saved.
   *
   * This is overridden by 'black box' groups such a geometry primitives
   *  
   * When users create nodes that implement SceneGraphIO interface then this
   * method is superceded by saveChildren() in the SceneGraphIO interface
   */
  protected boolean processChildren() {
      return true;
  }
  
  public void buildGraph() {
      for(int i=0; i<groupChildren.length; i++) {
          if (!groupChildren[i].getSymbol().graphBuilt) {
              groupChildren[i].getSymbol().graphBuilt = true;
              groupChildren[i].buildGraph();
          }
      }
      super.buildGraph(); // Must be last call in method
  }
  
  public void cleanup() {
      for(int i=0; i<groupChildren.length; i++) {
          groupChildren[i].cleanup();
          groupChildren[i] = null;
      }
      
      groupChildren = null;
      super.cleanup();
  }
    
    protected javax.media.j3d.SceneGraphObject createNode() {
        return new Group();
    }

  
}
