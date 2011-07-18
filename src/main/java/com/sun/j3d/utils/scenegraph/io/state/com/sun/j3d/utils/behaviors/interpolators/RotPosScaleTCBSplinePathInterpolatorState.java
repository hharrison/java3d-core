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

package com.sun.j3d.utils.scenegraph.io.state.com.sun.j3d.utils.behaviors.interpolators;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;
import com.sun.j3d.utils.scenegraph.io.state.javax.media.j3d.InterpolatorState;
import com.sun.j3d.utils.behaviors.interpolators.TCBSplinePathInterpolator;
import com.sun.j3d.utils.behaviors.interpolators.RotPosScaleTCBSplinePathInterpolator;
import com.sun.j3d.utils.scenegraph.io.state.javax.media.j3d.TransformInterpolatorState;
import com.sun.j3d.utils.behaviors.interpolators.TCBKeyFrame;

public class RotPosScaleTCBSplinePathInterpolatorState extends TransformInterpolatorState {

    private TCBKeyFrame[] keyFrames;
    private Transform3D axisOfTranslation;
    
    public RotPosScaleTCBSplinePathInterpolatorState(SymbolTableData symbol,Controller control) {
        super( symbol, control );
    }
    
    public void writeConstructorParams( DataOutput out ) throws IOException {
        super.writeConstructorParams( out );
        
        control.writeTransform3D( out, ((RotPosScaleTCBSplinePathInterpolator)node).getAxisOfRotPosScale() );

        int length = ((RotPosScaleTCBSplinePathInterpolator)node).getArrayLength();
        out.writeInt( length );
        
        for(int i=0; i<length; i++) {
            TCBKeyFrame keyFrame = ((RotPosScaleTCBSplinePathInterpolator)node).getKeyFrame( i );
            out.writeFloat( keyFrame.knot );
            out.writeInt( keyFrame.linear );
            control.writePoint3f( out, keyFrame.position );
            control.writeQuat4f( out, keyFrame.quat );
            control.writePoint3f( out, keyFrame.scale );
            out.writeFloat( keyFrame.tension );
            out.writeFloat( keyFrame.continuity );
            out.writeFloat( keyFrame.bias );
        }
    }
    
    public void readConstructorParams( DataInput in ) throws IOException {
        super.readConstructorParams( in );
        
        axisOfTranslation =  control.readTransform3D( in );

        keyFrames = new TCBKeyFrame[in.readInt() ];
        for(int i=0; i<keyFrames.length; i++) {
            keyFrames[i] = new TCBKeyFrame( in.readFloat(),
                                            in.readInt(),
                                            control.readPoint3f( in ),
					    control.readQuat4f( in ),
					    control.readPoint3f( in ),
                                            in.readFloat(),
                                            in.readFloat(),
                                            in.readFloat() );
        }
    }
    
    public SceneGraphObject createNode( Class j3dClass ) {
        return createNode( j3dClass, new Class[] { javax.media.j3d.Alpha.class,
                                                    TransformGroup.class,
                                                    Transform3D.class,
                                                    keyFrames.getClass() },
                                      new Object[] { null,
                                                     null,
                                                     axisOfTranslation,
                                                     keyFrames } );
                                                    
    }
    
    protected javax.media.j3d.SceneGraphObject createNode() {
        return new RotPosScaleTCBSplinePathInterpolator( null, null, axisOfTranslation, keyFrames );
    }


}
