/*
 * $RCSfile$
 *
 * Copyright 1999-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import java.util.ArrayList;

class TextureUnitStateRetained extends NodeComponentRetained {

    static final int TEXTURE_CHANGED		= 0x0001;
    static final int TEXTURE_ATTRS_CHANGED 	= 0x0002;
    static final int TEXCOORD_GEN_CHANGED	= 0x0004;
    static final int ALL_STATE_CHANGED		= 0x0008;

    TextureRetained texture = null;
    TextureAttributesRetained texAttrs = null;
    TexCoordGenerationRetained texGen = null;

    /**
     * An abstract method to validate the texture unit state component
     */
    final void setTextureUnitStateComponent(NodeComponent comp,
					    NodeComponentRetained thisComp,
					    int messageOp) {
	if (source.isLive()) {

	    if ((comp == null && thisComp == null) ||
		(comp != null && comp.retained == thisComp))
		return;

	    if (thisComp != null) {
		thisComp.clearLive(refCount);
		thisComp.removeMirrorUsers(this);
	    }

	    if (comp != null) {
		((NodeComponentRetained)comp.retained).setLive(inBackgroundGroup, refCount);
		// If texture unit is live, then copy all the users of this
		// texture unit state as users of this texture component
		((NodeComponentRetained)comp.retained).copyMirrorUsers(this);
	    }

	    if (messageOp != -1) {
	        sendMessage(messageOp,
		    (comp == null ? null :
		        ((NodeComponentRetained)comp.retained).mirror));
	    }

	}
    }

    final void initTextureUnitState(Texture texture,
              TextureAttributes texAttrs,
              TexCoordGeneration texGen) {

	initTexture(texture);
	initTextureAttributes(texAttrs);
	initTexCoordGeneration(texGen);
    }

    final void setTextureUnitState(Texture texture,
              TextureAttributes texAttrs,
              TexCoordGeneration texGen) {

	setTextureUnitStateComponent(texture, this.texture, -1);
	setTextureUnitStateComponent(texAttrs, this.texAttrs, -1);
	setTextureUnitStateComponent(texGen, this.texGen, -1);


	// send all changes to the target threads in one
	// message to avoid modifying the renderBin repeatedly

	Object args[] = new Object[3];
	args[0] = (texture == null ? null :
			((TextureRetained)texture.retained).mirror);
	args[1] = (texAttrs == null ? null :
			((TextureAttributesRetained)texAttrs.retained).mirror);
	args[2] = (texGen == null ? null :
			((TexCoordGenerationRetained)texGen.retained).mirror);

	sendMessage(ALL_STATE_CHANGED, args);

	initTextureUnitState(texture, texAttrs, texGen);
    }

    final void initTexture(Texture texture) {
	if (texture == null)
	    this.texture = null;
	else
	    this.texture = (TextureRetained)texture.retained;
    }

    final void setTexture(Texture texture) {
	setTextureUnitStateComponent(texture, this.texture, TEXTURE_CHANGED);
	initTexture(texture);
    }

    final void initTextureAttributes(TextureAttributes texAttrs) {
	if (texAttrs == null)
	    this.texAttrs = null;
	else
	    this.texAttrs = (TextureAttributesRetained)texAttrs.retained;
    }

    final void setTextureAttributes(TextureAttributes texAttrs) {
	setTextureUnitStateComponent(texAttrs, this.texAttrs,
					TEXTURE_ATTRS_CHANGED);
	initTextureAttributes(texAttrs);
    }

    final void initTexCoordGeneration(TexCoordGeneration texGen) {
	if (texGen == null)
	    this.texGen = null;
	else
	    this.texGen = (TexCoordGenerationRetained)texGen.retained;
    }

    final void setTexCoordGeneration(TexCoordGeneration texGen) {
	setTextureUnitStateComponent(texGen, this.texGen, TEXCOORD_GEN_CHANGED);
	initTexCoordGeneration(texGen);
    }

    Texture getTexture() {
	return (texture == null ? null : (Texture)texture.source);
    }

    TextureAttributes getTextureAttributes() {
	return (texAttrs == null ? null : (TextureAttributes)texAttrs.source);
    }

    TexCoordGeneration getTexCoordGeneration() {
	return (texGen == null ? null : (TexCoordGeneration)texGen.source);
    }

    void updateNative(int unitIndex, Canvas3D cv,
			boolean reload, boolean simulate) {

	//System.err.println("TextureUnitState/updateNative: unitIndex= " + unitIndex + " reload= " + reload + " simulate= " + simulate);

	// unitIndex can be -1 for the single texture case, so
	// can't use unitIndex to index into the cv.texUnitState;
	// in this case, use index 0

	int index = unitIndex;

	if (index < 0)
	    index = 0;


	boolean dirty = ((cv.canvasDirty & (Canvas3D.TEXTUREATTRIBUTES_DIRTY|Canvas3D.TEXTUREBIN_DIRTY)) != 0);

        if (this.texture == null) {
	    // if texture is null, then texture mapped is
	    // disabled for this texture unit; and no more
	    // state update is needed

	    //System.err.println("texture is null");

	    if (cv.texUnitState[index].texture != null) {
	        cv.resetTexture(cv.ctx, unitIndex);
		cv.texUnitState[index].texture = null;
	    }
	    cv.canvasDirty &= ~Canvas3D.TEXTUREATTRIBUTES_DIRTY;
	    return;
        } else {

	    Pipeline.getPipeline().updateTextureUnitState(cv.ctx, unitIndex, true);
        }

        // reload is needed in a multi-texture case to bind the
	// texture parameters to the texture unit state

	if (reload || dirty || cv.texUnitState[index].texture != this.texture) {

	    // texture cannot be null at this point because it is
	    // already checked above
            this.texture.updateNative(cv);

	    cv.texUnitState[index].texture = this.texture;

        }

        if (this.texAttrs == null) {
	    if (reload || dirty || cv.texUnitState[index].texAttrs != null) {
                cv.resetTextureAttributes(cv.ctx);
		if (VirtualUniverse.mc.isD3D() &&
		    (texGen != null) &&
		    ((texGen.genMode == TexCoordGeneration.EYE_LINEAR) ||
		    ((texGen.genMode == TexCoordGeneration.SPHERE_MAP)))) {
		    // We need to reload tex since eye linear
		    // and sphere map in D3D will change the
		    // texture transform matrix also.
		    dirty = true;
		}
		cv.setBlendFunc(cv.ctx, TransparencyAttributes.BLEND_ONE,
				TransparencyAttributes.BLEND_ZERO);
	    	cv.texUnitState[index].texAttrs = null;
	    }
        } else {

	    TextureAttributesRetained mTexAttrs;
	    if (this.texAttrs.mirror == null) {
		mTexAttrs = this.texAttrs;
	    } else {
		mTexAttrs = (TextureAttributesRetained) this.texAttrs.mirror;
	    }


	    if (mTexAttrs.mirrorCompDirty) {
		// This happen when canvas reference is same as
		// texUnitState.texAttrs and we update the later without
		// notify cache.
		cv.texUnitState[index].texAttrs = null;
		mTexAttrs.mirrorCompDirty = false;
	    }

	    if (reload || dirty || cv.texUnitState[index].texAttrs != mTexAttrs) {
                this.texAttrs.updateNative(cv, simulate, texture.format);
		if (VirtualUniverse.mc.isD3D() &&
		    (texGen != null) &&
		    ((texGen.genMode == TexCoordGeneration.EYE_LINEAR) ||
		    ((texGen.genMode == TexCoordGeneration.SPHERE_MAP)))) {
		    dirty = true;
		}
	    	cv.texUnitState[index].texAttrs = mTexAttrs;
	    }
        }

	if (this.texGen == null) {
	    if (reload || dirty || cv.texUnitState[index].texGen != null) {
		cv.resetTexCoordGeneration(cv.ctx);
		cv.texUnitState[index].texGen = null;
	    }
        } else {
	    TexCoordGenerationRetained mTexGen;

	    if (this.texGen.mirror == null) {
		mTexGen = this.texGen;
	    } else {
		mTexGen = (TexCoordGenerationRetained)this.texGen.mirror;
	    }

	    if (mTexGen.mirrorCompDirty) {
		// This happen when canvas reference is same as
		// texUnitState.texGen and we update the later without
		// notify cache.
		cv.texUnitState[index].texGen = null;
		mTexGen.mirrorCompDirty = false;
	    }

	    if (reload || dirty || cv.texUnitState[index].texGen != mTexGen) {
		this.texGen.updateNative(cv);
		cv.texUnitState[index].texGen = mTexGen;
            }
        }
	cv.canvasDirty &= ~Canvas3D.TEXTUREATTRIBUTES_DIRTY;
    }


   /**
    * Creates and initializes a mirror object, point the mirror object
    * to the retained object if the object is not editable
    */
    synchronized void createMirrorObject() {

	if (mirror == null) {
	    TextureUnitStateRetained mirrorTus  =
			new TextureUnitStateRetained();
	    mirror = mirrorTus;
        }
	mirror.source = source;
	initMirrorObject();

    }

    synchronized void initMirrorObject() {

	TextureUnitStateRetained mirrorTus =
		(TextureUnitStateRetained)mirror;

	if (this.texture != null)
	    mirrorTus.texture = (TextureRetained)this.texture.mirror;
	else
	    mirrorTus.texture = null;

	if (this.texAttrs != null)
	    mirrorTus.texAttrs =
		(TextureAttributesRetained)this.texAttrs.mirror;
	else
	    mirrorTus.texAttrs = null;

	if (this.texGen != null)
	    mirrorTus.texGen = (TexCoordGenerationRetained)this.texGen.mirror;
	else
	    mirrorTus.texGen = null;
    }


    /** Update the "component" field of the mirror object with the
     *  given "value"
     */
    synchronized void updateMirrorObject(int component, Object value) {

	TextureUnitStateRetained mirrorTus = (TextureUnitStateRetained)mirror;

	if ((component & TEXTURE_CHANGED) != 0) {
	    mirrorTus.texture = (TextureRetained)value;
	}
	else if ((component & TEXTURE_ATTRS_CHANGED) != 0) {
	    mirrorTus.texAttrs = (TextureAttributesRetained)value;
	}
	else if ((component & TEXCOORD_GEN_CHANGED) != 0) {
	    mirrorTus.texGen = (TexCoordGenerationRetained)value;
	}
	else if ((component & ALL_STATE_CHANGED) != 0) {
	    Object [] args = (Object []) value;
	    mirrorTus.texture = (TextureRetained)args[0];
	    mirrorTus.texAttrs = (TextureAttributesRetained)args[1];
	    mirrorTus.texGen = (TexCoordGenerationRetained)args[2];
	}
    }


    boolean equivalent(TextureUnitStateRetained tr) {

	if (tr == null) {
	    return (false);

	} else if ((this.changedFrequent != 0) || (tr.changedFrequent != 0)) {
	    return (this.mirror == tr);

	} else {

	    if (this.texture != tr.texture) {
	        return false;
	    }

	    if (this.texAttrs != null &&
		    !this.texAttrs.equivalent(tr.texAttrs)) {
		return false;
	    }

	    if (this.texGen != null &&
		    !this.texGen.equivalent(tr.texGen)) {
		return false;
	    }
	}

	return true;
    }

    protected Object clone() {
	TextureUnitStateRetained tr = (TextureUnitStateRetained)super.clone();

	// the cloned object is used for RenderBin only.
	// In most cases, it will duplicate all attributes in the RenderBin
	// so that updating a mirror object in one level will not affect the
	// entire structure of the RenderBin, but will affect only those bins
	// that got affected by the modified mirror object
	if (this.texAttrs != null)
	    tr.texAttrs = (TextureAttributesRetained)this.texAttrs.clone();

	if (this.texGen != null)
	    tr.texGen = (TexCoordGenerationRetained)this.texGen.clone();

	return tr;
    }


    /**
     * set the texture unit state according to the specified texture
     * unit state
     */
    protected void set(TextureUnitStateRetained tr) {
	super.set(tr);
	this.texture = tr.texture;

	if (tr.texAttrs == null) {
	    this.texAttrs = null;
	} else {
	    if (this.texAttrs == null) {
		this.texAttrs = (TextureAttributesRetained)tr.texAttrs.clone();
	    } else {
		this.texAttrs.set(tr.texAttrs);
	    }
	}

	if (tr.texGen == null) {
	    this.texGen = null;
	} else {
	    if (this.texGen == null) {
		this.texGen = (TexCoordGenerationRetained)tr.texGen.clone();
	    } else {
		this.texGen.set(tr.texGen);
	    }
	}
    }

    protected void set(TextureRetained texture,
			TextureAttributesRetained texAttrs,
			TexCoordGenerationRetained texGen) {
	this.texture = texture;
	this.texAttrs = texAttrs;
	this.texGen = texGen;
    }

    synchronized void addAMirrorUser(Shape3DRetained shape) {

	super.addAMirrorUser(shape);

        if (texture != null)
            texture.addAMirrorUser(shape);
        if (texAttrs != null)
            texAttrs.addAMirrorUser(shape);
        if (texGen != null)
            texGen.addAMirrorUser(shape);
    }

    synchronized void removeAMirrorUser(Shape3DRetained shape) {
	super.removeAMirrorUser(shape);

	if (texture != null)
	    texture.removeAMirrorUser(shape);
	if (texAttrs != null)
	    texAttrs.removeAMirrorUser(shape);
	if (texGen != null)
	    texGen.removeAMirrorUser(shape);
    }

    synchronized void removeMirrorUsers(NodeComponentRetained node) {
	super.removeMirrorUsers(node);

	if (texture != null)
	    texture.removeMirrorUsers(node);
	if (texAttrs != null)
	    texAttrs.removeMirrorUsers(node);
	if (texGen != null)
	    texGen.removeMirrorUsers(node);
    }

    synchronized void copyMirrorUsers(NodeComponentRetained node) {
	super.copyMirrorUsers(node);

	if (texture != null)
	    texture.copyMirrorUsers(node);
	if (texAttrs != null)
	    texAttrs.copyMirrorUsers(node);
	if (texGen != null)
	    texGen.copyMirrorUsers(node);
    }


    void setLive(boolean backgroundGroup, int refCount) {
	if (texture != null)
	    texture.setLive(backgroundGroup, refCount);

	if (texAttrs != null)
	    texAttrs.setLive(backgroundGroup, refCount);

	if (texGen != null)
	    texGen.setLive(backgroundGroup, refCount);

        // Increment the reference count and initialize the textureUnitState
        // mirror object
        super.doSetLive(backgroundGroup, refCount);
	super.markAsLive();

    }


    void clearLive(int refCount) {
	super.clearLive(refCount);

	if (texture != null)
	    texture.clearLive(refCount);
	if (texAttrs != null)
	    texAttrs.clearLive(refCount);
	if (texGen != null)
	    texGen.clearLive(refCount);
    }

    boolean isStatic() {

	return (source.capabilityBitsEmpty() &&
		((texture == null) || (texture.isStatic())) &&
		((texAttrs == null) || (texAttrs.isStatic())) &&
		((texGen == null) || (texGen.isStatic())));
    }

    // Issue 209 - enable this method (was previously commented out)
    // Simply pass along to the NodeComponent
    void compile (CompileState compState) {
	setCompiled();

	if (texture != null)
	    texture.compile(compState);
	if (texAttrs != null)
	    texAttrs.compile(compState);
	if (texGen != null)
	    texGen.compile(compState);
    }

    boolean equals(TextureUnitStateRetained ts) {
        return ((ts == this) ||
		(ts != null) &&
		((texture == ts.texture) ||
		 ((texture != null) && (texture.equals(ts.texture)))) &&
		((texAttrs == ts.texAttrs) ||
		 ((texAttrs != null) && (texAttrs.equals(ts.texAttrs)))) &&
		((texGen == ts.texGen) ||
		 ((texGen != null) && (texGen.equals(ts.texGen)))));
    }


    void setInImmCtx(boolean flag) {
        super.setInImmCtx(flag);
	if (texture != null)
	    texture.setInImmCtx(flag);
	if (texAttrs != null)
	    texAttrs.setInImmCtx(flag);
	if (texGen != null)
	    texGen.setInImmCtx(flag);
    }

    boolean getInImmCtx() {
        return (super.getInImmCtx() ||
                ((texture != null) && (texture.getInImmCtx())) ||
                ((texAttrs != null) && (texAttrs.getInImmCtx())) ||
                ((texGen != null) && (texGen.getInImmCtx())));
    }


    boolean isLive() {
	return (source.isLive() ||
		((texture != null) && (texture.source.isLive())) ||
		((texAttrs != null) && (texAttrs.source.isLive())) ||
		((texGen != null) && (texGen.source.isLive())));
    }

    final void sendMessage(int attrMask, Object attr) {
	ArrayList<VirtualUniverse> univList = new ArrayList<VirtualUniverse>();
	ArrayList<ArrayList<GeometryAtom>> gaList = Shape3DRetained.getGeomAtomsList(mirror.users, univList);

	// Send to rendering attribute structure, regardless of
	// whether there are users or not (alternate appearance case ..)
	J3dMessage createMessage = new J3dMessage();
	createMessage.threads = J3dThread.UPDATE_RENDERING_ATTRIBUTES;
	createMessage.type = J3dMessage.TEXTURE_UNIT_STATE_CHANGED;
	createMessage.universe = null;
	createMessage.args[0] = this;
	createMessage.args[1]= new Integer(attrMask);
	createMessage.args[2] = attr;
	createMessage.args[3] = new Integer(changedFrequent);
	VirtualUniverse.mc.processMessage(createMessage);

	// System.err.println("univList.size is " + univList.size());
	for(int i=0; i<univList.size(); i++) {
	    createMessage = new J3dMessage();
	    createMessage.threads = J3dThread.UPDATE_RENDER;
	    createMessage.type = J3dMessage.TEXTURE_UNIT_STATE_CHANGED;

		createMessage.universe = univList.get(i);
	    createMessage.args[0] = this;
	    createMessage.args[1]= new Integer(attrMask);
	    createMessage.args[2] = attr;

		ArrayList<GeometryAtom> gL = gaList.get(i);
	    GeometryAtom[] gaArr = new GeometryAtom[gL.size()];
	    gL.toArray(gaArr);
	    createMessage.args[3] = gaArr;

	    VirtualUniverse.mc.processMessage(createMessage);
	}

    }

    boolean isTextureEnabled() {
	// Check the internal enable , instead of userSpecifiedEnable
	return (texture != null && texture.enable);
    }

    void handleFrequencyChange(int bit) {
        switch (bit) {
        case TextureUnitState.ALLOW_STATE_WRITE: {
            setFrequencyChangeMask(bit, bit);
        }
        default:
            break;
        }
    }
}
