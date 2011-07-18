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

import javax.media.j3d.Font3D;
import javax.media.j3d.Text3D;
import javax.vecmath.Point3f;

import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;

public class Text3DState extends GeometryState {

    private int font3d;
    private String string;

    public Text3DState(SymbolTableData symbol, Controller control) {
        super(symbol, control);

        if (node != null) {
            font3d = control.getSymbolTable().addReference(
                    ((Text3D) node).getFont3D());
        }
    }

    public void writeObject(DataOutput out) throws IOException {
        super.writeObject(out);

        out.writeInt(((Text3D) node).getAlignment());
        out.writeFloat(((Text3D) node).getCharacterSpacing());
        out.writeInt(font3d);

        out.writeInt(((Text3D) node).getPath());

        Point3f pos = new Point3f();
        ((Text3D) node).getPosition(pos);
        control.writePoint3f(out, pos);

        out.writeUTF(((Text3D) node).getString());
    }

    public void readObject(DataInput in) throws IOException {
        super.readObject(in);

        ((Text3D) node).setAlignment(in.readInt());
        ((Text3D) node).setCharacterSpacing(in.readFloat());
        font3d = in.readInt();
        ((Text3D) node).setPath(in.readInt());
        ((Text3D) node).setPosition(control.readPoint3f(in));
        // issue 483: must wait until the Font3D is set in buildGraph()
        string = in.readUTF();
        // old: ((Text3D)node).setString( in.readUTF() );
    }

    /**
     * Called when this component reference count is incremented. Allows this
     * component to update the reference count of any components that it
     * references.
     */
    public void addSubReference() {
        control.getSymbolTable().incNodeComponentRefCount(font3d);
    }

    public void buildGraph() {
        ((Text3D) node).setFont3D(((Font3D) control.getSymbolTable()
                .getJ3dNode(font3d)));
        // issue 483
        ((Text3D) node).setString(string);

        super.buildGraph(); // Must be last call in method
    }

    protected javax.media.j3d.SceneGraphObject createNode() {
        return new Text3D();
    }

}
