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

import javax.media.j3d.Font3D;
import javax.media.j3d.FontExtrusion;
import javax.media.j3d.SceneGraphObject;
import java.awt.Font;
import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;
import java.awt.Shape;
import java.lang.String;
import java.lang.Integer;
import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;
import java.awt.geom.PathIterator;
import java.awt.geom.GeneralPath;

public class Font3DState extends NodeComponentState {

    private Font font = null;
    private double tesselationTolerance = 0.0D;
    private FontExtrusion extrudePath = null;

    public Font3DState(SymbolTableData symbol, Controller control) {
        super(symbol, control);
    }

    public void writeConstructorParams(DataOutput out) throws IOException {
        super.writeConstructorParams(out);

        // issue 483: init the node
        Font3D font3D = (Font3D) node;
        font = font3D.getFont();

        out.writeUTF(font.getFontName());
        out.writeInt(font.getStyle());
        out.writeInt(font.getSize());
        out.writeDouble(font3D.getTessellationTolerance());

        // issue 483
        extrudePath = new FontExtrusion();
        font3D.getFontExtrusion(extrudePath);
        if (extrudePath.getExtrusionShape() == null) {
            extrudePath = null;
        }

        if (extrudePath != null) {
            Shape shape = extrudePath.getExtrusionShape();
            if (shape != null) {
                PathIterator shapePath = shape.getPathIterator(null);
                float[] coords = new float[6];
                int segType;
                int points;
                while (!(shapePath.isDone())) {
                    // Get type of current path segment and associated
                    // coordinates
                    segType = shapePath.currentSegment(coords);
                    out.writeInt(segType);

                    // Write out relevant coordinates
                    points = 0;
                    if (segType == PathIterator.SEG_MOVETO)
                        points = 1;
                    else if (segType == PathIterator.SEG_LINETO)
                        points = 1;
                    else if (segType == PathIterator.SEG_QUADTO)
                        points = 2;
                    else if (segType == PathIterator.SEG_CUBICTO)
                        points = 3;

                    for (int i = 0; i < points; i++) {
                        out.writeFloat(coords[i * 2 + 0]);
                        out.writeFloat(coords[i * 2 + 1]);
                    }

                    // Next segment
                    if (!(shapePath.isDone()))
                        shapePath.next();
                }
            }
            // Flag for end of path definition
            out.writeInt(Integer.MIN_VALUE);
            out.writeDouble(extrudePath.getTessellationTolerance());

        } else {
            out.writeInt(Integer.MIN_VALUE);
        }
    }

    public void readConstructorParams(DataInput in) throws IOException {
        super.readConstructorParams(in);

        String fontName = in.readUTF();
        int style = in.readInt();
        int size = in.readInt();
        font = new Font(fontName, style, size);

        tesselationTolerance = in.readDouble();

        GeneralPath shape = null;
        int segType = in.readInt();
        while (segType != Integer.MIN_VALUE) {
            if (shape == null)
                shape = new GeneralPath();

            if (segType == PathIterator.SEG_MOVETO) {
                shape.moveTo(in.readFloat(), in.readFloat());
            } else if (segType == PathIterator.SEG_LINETO) {
                shape.lineTo(in.readFloat(), in.readFloat());
            } else if (segType == PathIterator.SEG_QUADTO) {
                shape.quadTo(in.readFloat(), in.readFloat(), in.readFloat(), in
                        .readFloat());
            } else if (segType == PathIterator.SEG_CUBICTO) {
                shape.curveTo(in.readFloat(), in.readFloat(), in.readFloat(),
                        in.readFloat(), in.readFloat(), in.readFloat());
            } else if (segType == PathIterator.SEG_CLOSE) {
                shape.closePath();
            }

            segType = in.readInt();
        }
        if (shape != null)
            extrudePath = new FontExtrusion(shape, in.readDouble());
        else
            extrudePath = null;
    }

    public SceneGraphObject createNode(Class j3dClass) {
        return createNode(j3dClass, new Class[] { Font.class, Double.TYPE,
                FontExtrusion.class }, new Object[] { font,
                new Double(tesselationTolerance), extrudePath });
    }

    protected SceneGraphObject createNode() {
        return new Font3D(font, tesselationTolerance, extrudePath);
    }
}
