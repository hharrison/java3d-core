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
import javax.media.j3d.RotPosPathInterpolator;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;

public class RotPosPathInterpolatorState extends PathInterpolatorState {

    private Point3f[] positions;
    private Quat4f[] quats;
    
    public RotPosPathInterpolatorState(SymbolTableData symbol,Controller control) {
        super( symbol, control );
    }
    
    public void writeConstructorParams( DataOutput out ) throws IOException {
        super.writeConstructorParams( out );
        
        positions = new Point3f[ knots.length ];
        quats = new Quat4f[ knots.length ];
        for(int i=0; i<positions.length; i++) {
            positions[i] = new Point3f();
            quats[i] = new Quat4f();
        }

        ((RotPosPathInterpolator)node).getPositions( positions );
        ((RotPosPathInterpolator)node).getQuats( quats );
        for(int i=0; i<positions.length; i++) {
            control.writePoint3f( out, positions[i] );
            control.writeQuat4f( out, quats[i] );
        }
    }
    
    public void readConstructorParams( DataInput in ) throws IOException {
        super.readConstructorParams( in );
        
        positions = new Point3f[ knots.length ];
        quats = new Quat4f[ knots.length ];
        for(int i=0; i<positions.length; i++) {
            positions[i] = control.readPoint3f( in );
            quats[i] = control.readQuat4f( in );
        }
    }

    public SceneGraphObject createNode( Class j3dClass ) {
        return createNode( j3dClass, new Class[] { javax.media.j3d.Alpha.class,
                                                    TransformGroup.class,
                                                    Transform3D.class,
                                                    knots.getClass(),
                                                    quats.getClass(),
                                                    positions.getClass() },
                                      new Object[] { null,
                                                     null,
                                                     new Transform3D(),
                                                     knots,
                                                     quats,
                                                     positions } );
                                                    
    }
    
    protected javax.media.j3d.SceneGraphObject createNode() {
        return new RotPosPathInterpolator( null, null, new Transform3D(), knots, quats, positions );
    }


}
