/*
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
 */

package javax.media.j3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * The TextureBin manages a collection of TextureSetting objects.
 * All objects in the TextureBin share the same Texture reference.
 */


//class TextureBin extends Object implements ObjectUpdate, NodeComponentUpdate {
class TextureBin extends Object implements ObjectUpdate {

    TextureUnitStateRetained [] texUnitState = null;

    // last active texture unit
    private int lastActiveTexUnitIndex;

    // number of active texture unit
    private int numActiveTexUnit;

    /**
     * The RenderBin for this object
     */
    RenderBin renderBin = null;

    /**
     * The EnvironmentSet that this TextureBin resides
     */
    EnvironmentSet environmentSet = null;

    /**
     * The AttributeBin that this TextureBin resides
     */
    AttributeBin attributeBin = null;

    /**
     * The ShaderBin that this TextureBin resides
     */
    ShaderBin shaderBin = null;

    /**
     * The references to the next and previous TextureBins in the
     * list.
     */
    TextureBin next = null;
    TextureBin prev = null;

    /**
     * Oring of the equivalence bits for all appearance attrs under
     * this renderBin
     */
    int equivalent = 0;

    /**
     * If any of the texture reference in an appearance is frequently
     * changable, then a separate TextureBin will be created for this
     * appearance, and this TextureBin is marked as the sole user of
     * this appearance, and app will be pointing to the appearance
     * being referenced by this TextureBin. Otherwise, app is null
     */
    AppearanceRetained app = null;


    /**
     * Sole user node component dirty mask.  The first bit is reserved
     * for node component reference dirty bit. It is set if any of the
     * texture related node component reference in the appearance is
     * being modified. The second bit onwords are for the individual
     * TextureUnitState dirty bit. The ith bit set means the (i-1)
     * texture unit state is modified. Note, this mask only supports
     * 30 texture unit states. If the appearance uses more than 31
     * texture unit states, then the modification of the 32nd texture
     * unit state and up will have the first bit set, that means
     * the TextureBin will be reset, rather than only the particular
     * texture unit state will be reset.
     */
    int soleUserCompDirty;

    static final int SOLE_USER_DIRTY_REF 		= 0x1;
    static final int SOLE_USER_DIRTY_TA 		= 0x2;
    static final int SOLE_USER_DIRTY_TC 		= 0x4;
    static final int SOLE_USER_DIRTY_TEXTURE		= 0x8;
    static final int SOLE_USER_DIRTY_TUS		= 0x10;


/**
 * The hashMap of RenderMolecules in this TextureBin this is used in rendering,
 * the key used is localToVworld
 */
HashMap<Transform3D[], ArrayList<RenderMolecule>> addOpaqueRMs = new HashMap<Transform3D[], ArrayList<RenderMolecule>>();
HashMap<Transform3D[], ArrayList<RenderMolecule>> addTransparentRMs = new HashMap<Transform3D[], ArrayList<RenderMolecule>>();

// A hashmap based on localToVworld for fast
// insertion of new renderMolecules
HashMap<Transform3D[], RenderMolecule> opaqueRenderMoleculeMap = new HashMap<Transform3D[], RenderMolecule>();
HashMap<Transform3D[], RenderMolecule> transparentRenderMoleculeMap = new HashMap<Transform3D[], RenderMolecule>();

    // List of renderMolecules  - used in rendering ..
    RenderMolecule opaqueRMList = null;

    RenderMolecule transparentRMList = null;
    TransparentRenderingInfo parentTInfo;

    int numRenderMolecules = 0;
    int numEditingRenderMolecules = 0;

    int tbFlag = 0;	// a general bitmask for TextureBin

    // Following are the bits used in flag

    final static int ON_RENDER_BIN_LIST   	= 0x0001;
    final static int ON_UPDATE_LIST 		= 0x0002;
    final static int SOLE_USER			= 0x0004;
    final static int CONTIGUOUS_ACTIVE_UNITS    = 0x0008;
    final static int RESORT    			= 0x0010;
    final static int ON_UPDATE_CHECK_LIST	= 0x0020;

    final static int USE_DISPLAYLIST = -2;
    final static int USE_VERTEXARRAY = -1;

    TextureBin(TextureUnitStateRetained[] state, AppearanceRetained app,
			RenderBin rb) {
        renderBin = rb;
	tbFlag = 0;
	reset(state, app);
    }


    /**
     * For now, clone everything just like the other NodeComponent
     */
    void reset(TextureUnitStateRetained[] state, AppearanceRetained app) {

	prev = null;
	next = null;
	opaqueRMList = null;
	transparentRMList = null;
	numEditingRenderMolecules = 0;

        // Issue 249 - check for sole user only if property is set
	// determine if this appearance is a sole user of this
	// TextureBin
        tbFlag &= ~TextureBin.SOLE_USER;
        if (VirtualUniverse.mc.allowSoleUser) {
            if ((app != null) &&
                 (app.changedFrequent &
                    (AppearanceRetained.TEXTURE |
                     AppearanceRetained.TEXCOORD_GEN |
                     AppearanceRetained.TEXTURE_ATTR |
                     AppearanceRetained.TEXTURE_UNIT_STATE)) != 0) {
                tbFlag |= TextureBin.SOLE_USER;

            }
	}

        if ((tbFlag & TextureBin.SOLE_USER) != 0) {
	    this.app = app;
        } else {
	    this.app = null;
        }

	resetTextureState(state);

	if ((tbFlag & TextureBin.ON_RENDER_BIN_LIST) == 0) {
	    renderBin.addTextureBin(this);
	    tbFlag |= TextureBin.ON_RENDER_BIN_LIST;
	}

    }

    void resetTextureState(TextureUnitStateRetained[] state) {

        int i, j;
	boolean foundDisableUnit = false;
	numActiveTexUnit = 0;
	lastActiveTexUnitIndex = 0;
	boolean soleUser = ((tbFlag & TextureBin.SOLE_USER) != 0);
	TextureRetained prevFirstTexture = null;
	TextureRetained tex;

	tbFlag |= TextureBin.CONTIGUOUS_ACTIVE_UNITS;

	if (state != null) {

	    foundDisableUnit = false;

	    if (texUnitState == null || (texUnitState.length != state.length)) {
		texUnitState = new TextureUnitStateRetained[state.length];
	    } else if (texUnitState.length > 0 && texUnitState[0] != null) {
		prevFirstTexture = texUnitState[0].texture;
	    }

	    for (i = 0; i < state.length; i++) {
		if (state[i] == null) {
		    texUnitState[i] = null;
		    foundDisableUnit = true;
		} else {

		    // create a clone texture unit state
		    if (texUnitState[i] == null) {
		        texUnitState[i] = new TextureUnitStateRetained();
		    }

		    // for sole user TextureUnitState, save
		    // the node component reference in the mirror reference
		    // of the cloned copy for equal test, and
		    // for native download optimization
		    if (soleUser || state[i].changedFrequent != 0) {
			texUnitState[i].mirror = state[i];
		    }

		    // for the lowest level of node component in
		    // TextureBin, clone it only if it is not
		    // changedFrequent; in other words, if the
		    // lowest level of texture related node components
		    // such as TextureAttributes & TexCoordGen is
		    // changedFrequent, have the cloned texUnitState
		    // reference the mirror of those node components
		    // directly. For Texture, we'll always reference
		    // the mirror.

		    // decrement the TextureBin ref count of the previous
		    // texture
		    tex = texUnitState[i].texture;
		    if (tex != null) {
			tex.decTextureBinRefCount(this);
			if (soleUser &&
			    (tex.getTextureBinRefCount(this) == 0) &&
			    (tex != state[i].texture)) {
			    // In this case texture change but
			    // TextureBin will not invoke clear() to reset.
			    // So we need to free the texture resource here.
                            renderBin.addTextureResourceFreeList(tex);
			}
		    }

		    texUnitState[i].texture = state[i].texture;

		    // increment the TextureBin ref count of the new
		    // texture

		    if (texUnitState[i].texture != null) {
			texUnitState[i].texture.incTextureBinRefCount(this);
		    }

		    if (state[i].texAttrs != null) {

			if (state[i].texAttrs.changedFrequent != 0) {
			    texUnitState[i].texAttrs = state[i].texAttrs;

			} else {

			    // need to check for texAttrs.source because
			    // texAttrs could be pointing to the mirror
			    // in the last frame, so don't want to
			    // overwrite the mirror

			    if (texUnitState[i].texAttrs == null ||
				  texUnitState[i].texAttrs.source != null) {
			 	texUnitState[i].texAttrs =
				    new TextureAttributesRetained();
			    }
			    texUnitState[i].texAttrs.set(
						state[i].texAttrs);
			    texUnitState[i].texAttrs.mirrorCompDirty = true;

			    // for sole user TextureBin, we are saving
			    // the mirror node component in the mirror
			    // reference in the clone object. This
			    // will be used in state download to
			    // avoid redundant download

			    if (soleUser) {
				texUnitState[i].texAttrs.mirror =
					state[i].texAttrs;
			    } else {
				texUnitState[i].texAttrs.mirror = null;
			    }

			}
		    } else {
			texUnitState[i].texAttrs = null;
		    }


		    if (state[i].texGen != null) {
			if (state[i].texGen.changedFrequent != 0) {
			    texUnitState[i].texGen = state[i].texGen;
			} else {

			    // need to check for texGen.source because
			    // texGen could be pointing to the mirror
			    // in the last frame, so don't want to
			    // overwrite the mirror

			    if (texUnitState[i].texGen == null ||
				  texUnitState[i].texGen.source != null) {
			 	texUnitState[i].texGen =
				    new TexCoordGenerationRetained();
			    }

			    texUnitState[i].texGen.set(state[i].texGen);
			    texUnitState[i].texGen.mirrorCompDirty = true;


                            // for sole user TextureBin, we are saving
                            // the mirror node component in the mirror
                            // reference in the clone object. This
                            // will be used in state download to
                            // avoid redundant download

			    if (soleUser) {
				texUnitState[i].texGen.mirror = state[i].texGen;
			    } else {
				texUnitState[i].texGen.mirror = null;
			    }
			}
		    } else {
			texUnitState[i].texGen = null;
		    }


		    // Track the last active texture unit and the total number
                    // of active texture units. Note that this total number
                    // now includes disabled units so that there is always
                    // a one-to-one mapping. We no longer remap texture units.
		    if (texUnitState[i].isTextureEnabled()) {
			lastActiveTexUnitIndex = i;
                        numActiveTexUnit = i + 1;

			if (foundDisableUnit) {

			    // mark that active texture units are not
			    // contiguous
			    tbFlag &= ~TextureBin.CONTIGUOUS_ACTIVE_UNITS;
			}
		    } else {
			foundDisableUnit = true;
		    }
		}
	    }

	    // check to see if the TextureBin sorting criteria is
	    // modified for this textureBin; if yes, mark that
	    // resorting is needed

	    if ((texUnitState[0] == null && prevFirstTexture != null) ||
		(texUnitState[0] != null &&
			texUnitState[0].texture != prevFirstTexture)) {
		tbFlag |= TextureBin.RESORT;
	    }

	} else {

	    // check to see if the TextureBin sorting criteria is
	    // modified for this textureBin; if yes, mark that
	    // resorting is needed

	    if (texUnitState != null && texUnitState[0].texture != null) {
		tbFlag |= TextureBin.RESORT;
	    }
	    texUnitState = null;
	}

	soleUserCompDirty = 0;
    }


    /**
     * The TextureBin is to be removed from RenderBin,
     * do the proper unsetting of any references
     */
    void clear() {

        // make sure there is no reference to the scenegraph
        app = null;

        // for each texture referenced in the texture units, decrement
	// the reference count. If the reference count == 0, tell
	// the renderer to free up the resource
        if (texUnitState != null) {

            TextureRetained tex;

            for (int i = 0; i < texUnitState.length; i++) {
                if (texUnitState[i] != null) {
                    if (texUnitState[i].texture != null) {
                        tex = texUnitState[i].texture;
                        tex.decTextureBinRefCount(this);

                        if (tex.getTextureBinRefCount(this) == 0) {
                            renderBin.addTextureResourceFreeList(tex);
                        }

			texUnitState[i].texture = null;
                    }

                    // make sure there is no more reference to the scenegraph

                    texUnitState[i].mirror = null;
                    texUnitState[i].texture = null;
                    if (texUnitState[i].texAttrs != null &&
                            texUnitState[i].texAttrs.source != null) {
                        texUnitState[i].texAttrs = null;
                    }
                    if (texUnitState[i].texGen != null &&
                            texUnitState[i].texGen.source != null) {
                        texUnitState[i].texGen = null;
                    }
                }
            }
        }
    }



    /**
     * This tests if the qiven textureUnitState matches this TextureBin
     */
    boolean equals(TextureUnitStateRetained state[], RenderAtom ra) {

	int i, j, k = 0;
	TextureRetained texture;

	// if this TextureBin is a soleUser case or the incoming
	// app has changedFrequent bit set for any of the texture
	// related component, then either the current TextureBin
	// or the incoming app requires the same app match
	if (((tbFlag & TextureBin.SOLE_USER) != 0) ||
		((ra.app != null) &&
             		 (ra.app.changedFrequent &
                	    (AppearanceRetained.TEXTURE |
                 	     AppearanceRetained.TEXCOORD_GEN |
                 	     AppearanceRetained.TEXTURE_ATTR |
                 	     AppearanceRetained.TEXTURE_UNIT_STATE)) != 0)) {

	    if (app == ra.app) {

		// if this textureBin is currently on a zombie state,
		// we'll need to put it on the update list to reevaluate
		// the state, because while it is on a zombie state,
		// texture state could have been changed. Example,
		// application could have detached an appearance,
		// made changes to the texture references, and then
		// reattached the appearance. In this case, the texture
		// changes would not have reflected to the textureBin

		if (numEditingRenderMolecules == 0) {

		    //System.err.println("===> TB in zombie state  " + this);

            	    if (soleUserCompDirty == 0) {
                        this.renderBin.tbUpdateList.add(this);
            	    }
            	    soleUserCompDirty |= TextureBin.SOLE_USER_DIRTY_REF;
		}
		return true;

	    } else {
		return false;
	    }
	}

	if (texUnitState == null && state == null)
	    return (true);

	if (texUnitState == null || state == null)
	    return (false);

	if (state.length != texUnitState.length)
	    return (false);

	for (i = 0; i < texUnitState.length; i++) {
	    // If texture Unit State is null
	    if (texUnitState[i] == null) {
		if (state[i] != null)
		    return (false);
	    }
	    else {
		if (!texUnitState[i].equivalent(state[i])) {
		    return (false);
		}
	    }
	}

	// Check if the image component has changed(may be a clearLive texture
	// change img component. setLive case)
	//
	if ((tbFlag & TextureBin.ON_RENDER_BIN_LIST) == 0) {
	    renderBin.addTextureBin(this);
	    tbFlag |= TextureBin.ON_RENDER_BIN_LIST;
	}

	return (true);

    }


    /*
    // updateNodeComponentCheck is called for each soleUser TextureBin
    // into which new renderAtom has been added. This method is called before
    // updateNodeComponent() to allow TextureBin to catch any node
    // component changes that have been missed because the changes
    // come when there is no active renderAtom associated with the
    // TextureBin. See bug# 4503926 for details.
    public void updateNodeComponentCheck() {

	//System.err.println("TextureBin.updateNodeComponentCheck()");

	tbFlag &= ~TextureBin.ON_UPDATE_CHECK_LIST;

	if ((soleUserCompDirty & SOLE_USER_DIRTY_REF) != 0) {
	    return ;
	}

       	if ((app.compChanged & (AppearanceRetained.TEXTURE |
                 		AppearanceRetained.TEXCOORD_GEN |
                 		AppearanceRetained.TEXTURE_ATTR |
                 		AppearanceRetained.TEXTURE_UNIT_STATE)) != 0) {
            if (soleUserCompDirty == 0) {
                this.renderBin.tbUpdateList.add(this);
            }
            soleUserCompDirty |= TextureBin.SOLE_USER_DIRTY_REF;

        } else if (app.texUnitState != null) {

	    // if one texture unit state has to be reevaluated, then
	    // it's enough update checking because reevaluating texture unit
	    // state will automatically take care of its node component
	    // updates.

	    boolean done = false;

            for (int i = 0; i < app.texUnitState.length && !done; i++) {
                if (app.texUnitState[i] != null) {
		    if (app.texUnitState[i].compChanged != 0) {
 		        if (soleUserCompDirty == 0) {
			    this.renderBin.tbUpdateList.add(this);
		        }
		        soleUserCompDirty |= TextureBin.SOLE_USER_DIRTY_TUS;
			done = true;
		    } else {
			if (app.texUnitState[i].texAttrs != null &&
				app.texUnitState[i].texAttrs.compChanged != 0) {
			    if (soleUserCompDirty == 0) {
				this.renderBin.tbUpdateList.add(this);
			    }
			    soleUserCompDirty |= TextureBin.SOLE_USER_DIRTY_TA;
			}
			if (app.texUnitState[i].texGen != null &&
				app.texUnitState[i].texGen.compChanged != 0) {
			    if (soleUserCompDirty == 0) {
				this.renderBin.tbUpdateList.add(this);
			    }
			    soleUserCompDirty |= TextureBin.SOLE_USER_DIRTY_TC;
			}
			if (app.texUnitState[i].texture != null &&
				((app.texUnitState[i].texture.compChanged &
				    TextureRetained.ENABLE_CHANGED) != 0)) {
			    if (soleUserCompDirty == 0) {
				this.renderBin.tbUpdateList.add(this);
			    }
			    soleUserCompDirty |= TextureBin.SOLE_USER_DIRTY_TEXTURE;
			}
		    }
		}
	    }
	}
    }
     */




    /**
     * updateNodeComponent is called from RenderBin to update the
     * clone copy of the sole user node component in TextureBin when the
     * corresponding node component is being modified
     */
    public void updateNodeComponent() {

	// don't bother to update if the TextureBin is already
	// removed from RenderBin

	if ((tbFlag & TextureBin.ON_RENDER_BIN_LIST) == 0)
	    return;

	// if any of the texture reference in the appearance referenced
	// by a sole user TextureBin is being modified, just do a reset

	if (((tbFlag & TextureBin.SOLE_USER) != 0) &&
		((soleUserCompDirty & TextureBin.SOLE_USER_DIRTY_REF) != 0)) {

	    resetTextureState(app.texUnitState);
	    return;
	}

	if (texUnitState == null)  {
	    soleUserCompDirty = 0;
	    return;
	}

	if ((soleUserCompDirty & TextureBin.SOLE_USER_DIRTY_TUS) != 0) {

	    // Now take care of the Texture Unit State changes
	    TextureUnitStateRetained tus, mirrorTUS = null;
	    boolean soleUser = ((tbFlag & TextureBin.SOLE_USER) != 0);

	    for (int i = 0; i < texUnitState.length; i++) {
	        tus = texUnitState[i];
	        if (tus != null) {
		    if (tus.mirror != null) {

		        mirrorTUS = (TextureUnitStateRetained)tus.mirror;

			if (tus.texture != mirrorTUS.texture) {
			    if (tus.texture != null) {
				tus.texture.decTextureBinRefCount(this);
			    }
		            tus.texture = mirrorTUS.texture;
			    if (tus.texture != null) {
				tus.texture.incTextureBinRefCount(this);
			    }

			    // the first texture (TextureBin sorting
			    // criteria) is modified, so needs to resort

			    if (i == 0) {
				tbFlag |= TextureBin.RESORT;
			    }
			}


		        if (mirrorTUS.texAttrs != null) {
			    if (mirrorTUS.texAttrs.changedFrequent != 0) {
			        tus.texAttrs = mirrorTUS.texAttrs;
			    } else {
			        if (tus.texAttrs == null ||
				  	tus.texAttrs.source != null) {
				    tus.texAttrs =
					new TextureAttributesRetained();
			        }
			        tus.texAttrs.set(mirrorTUS.texAttrs);
				tus.texAttrs.mirrorCompDirty = true;

				if (soleUser) {
				    tus.texAttrs.mirror = mirrorTUS.texAttrs;
				} else {
				    tus.texAttrs.mirror = null;
				}
			    }
		        } else {
			    tus.texAttrs = null;
		        }

		        if (mirrorTUS.texGen != null) {
			    if (mirrorTUS.texGen.changedFrequent != 0) {
			        tus.texGen = mirrorTUS.texGen;
			    } else {
			        if (tus.texGen == null ||
				  	tus.texGen.source != null) {
				    tus.texGen =
					    new TexCoordGenerationRetained();
			        }
			        tus.texGen.set(mirrorTUS.texGen);
				tus.texGen.mirrorCompDirty = true;

				if (soleUser) {
				    tus.texGen.mirror = mirrorTUS.texGen;
				} else {
				    tus.texGen.mirror = null;
				}
			    }
		        } else {
			    tus.texGen = null;
		        }
		    }
	        }
	    }

	    // need to reEvaluate # of active textures after the update
	    soleUserCompDirty |= TextureBin.SOLE_USER_DIRTY_TEXTURE;

	    // TextureUnitState update automatically taken care of
	    // TextureAttributes & TexCoordGeneration update

	    soleUserCompDirty &= ~(TextureBin.SOLE_USER_DIRTY_TA |
					TextureBin.SOLE_USER_DIRTY_TC);
	}

	if ((soleUserCompDirty & TextureBin.SOLE_USER_DIRTY_TEXTURE) != 0) {



	    boolean foundDisableUnit = false;

	    numActiveTexUnit = 0;
	    lastActiveTexUnitIndex = 0;
	    tbFlag |= TextureBin.CONTIGUOUS_ACTIVE_UNITS;
	    for (int i = 0; i < texUnitState.length; i++) {

                // Track the last active texture unit and the total number
                // of active texture units. Note that this total number
                // now includes disabled units so that there is always
                // a one-to-one mapping. We no longer remap texture units.
                if (texUnitState[i] != null &&
			texUnitState[i].isTextureEnabled()) {
                    lastActiveTexUnitIndex = i;
                    numActiveTexUnit = i + 1;

                    if (foundDisableUnit) {

                        // mark that active texture units are not
                        // contiguous
                        tbFlag &= ~TextureBin.CONTIGUOUS_ACTIVE_UNITS;
                    }
                } else {
                    foundDisableUnit = true;
                }
	    }
	}

	if ((soleUserCompDirty & TextureBin.SOLE_USER_DIRTY_TA) != 0) {
	    for (int i = 0; i < texUnitState.length; i++) {
		if (texUnitState[i] != null &&
			texUnitState[i].texAttrs != null &&
			texUnitState[i].texAttrs.mirror != null &&
		        texUnitState[i].texAttrs.mirror.changedFrequent != 0) {
		    texUnitState[i].texAttrs = (TextureAttributesRetained)
				texUnitState[i].texAttrs.mirror;
		}
	    }
	}

	if ((soleUserCompDirty & TextureBin.SOLE_USER_DIRTY_TC) != 0) {
	    for (int i = 0; i < texUnitState.length; i++) {
		if (texUnitState[i] != null &&
			texUnitState[i].texGen != null &&
			texUnitState[i].texGen.mirror != null &&
		        texUnitState[i].texGen.mirror.changedFrequent != 0) {
		    texUnitState[i].texGen = (TexCoordGenerationRetained)
				texUnitState[i].texGen.mirror;
		}
	    }
	}

	soleUserCompDirty = 0;
    }

    public void updateObject() {
	if (!addOpaqueRMs.isEmpty()) {
	    opaqueRMList = addAll(opaqueRenderMoleculeMap, addOpaqueRMs,
					opaqueRMList, true);
	}
	if (!addTransparentRMs.isEmpty()) {
	    // If transparent and not in bg geometry and inodepth
	    // sorted transparency
	    if (transparentRMList == null &&
		(renderBin.transpSortMode == View.TRANSPARENCY_SORT_NONE ||
		environmentSet.lightBin.geometryBackground != null)) {
		//		System.err.println("========> addTransparentTextureBin "+this);
		transparentRMList = addAll(transparentRenderMoleculeMap,
				addTransparentRMs, transparentRMList, false);
		// Eventhough we are adding to transparentList , if all the RMS
		// have been switched already due to changeLists, then there is
		// nothing to add, and TBIN does not have any transparentRMList
		if (transparentRMList != null) {
		    renderBin.addTransparentObject(this);
		}

	    }
	    else {
		transparentRMList = addAll(transparentRenderMoleculeMap,
				addTransparentRMs, transparentRMList, false);
	    }
	}
        tbFlag &= ~TextureBin.ON_UPDATE_LIST;

    }


    /**
     * Each list of renderMoledule with the same localToVworld
     * is connect by rm.next and rm.prev.
     * At the end of the list (i.e. rm.next = null) the field
     * rm.nextMap is link to another list (with the same
     * localToVworld). So during rendering it will traverse
     * rm.next until this is null, then follow the .nextMap
     * to access another list and use rm.next to continue
     * until both rm.next and rm.nextMap are null.
     *
     * renderMoleculeMap is use to assist faster location of
     * renderMolecule List with the same localToVWorld. The
     * start of renderMolecule in the list with same
     * localToVworld is insert in renderMoleculeMap. This
     * map is clean up at removeRenderMolecule(). TextureBin
     * also use the map for quick location of renderMolecule
     * with the same localToVworld and attributes in
     * findRenderMolecule().
     */
RenderMolecule addAll(HashMap<Transform3D[], RenderMolecule> renderMoleculeMap,
                      HashMap<Transform3D[], ArrayList<RenderMolecule>> addRMs,
                      RenderMolecule startList, boolean opaqueList) {
        int i;
	Collection<ArrayList<RenderMolecule>> c = addRMs.values();
	Iterator<ArrayList<RenderMolecule>> listIterator = c.iterator();
	RenderMolecule renderMoleculeList, head;

	while (listIterator.hasNext()) {
	    boolean changed = false;
		ArrayList<RenderMolecule> curList = listIterator.next();
		RenderMolecule r = curList.get(0);
	    // If this is a opaque one , but has been switched to a transparentList or
	    // vice-versa (dur to changeLists function called before this), then
	    // do nothing!
	    // For changedFrequent case: Consider the case when a RM is added
	    // (so is in the addRM list) and then
	    // a change  in transparent value occurs that make it from opaque to
	    // transparent (the switch is handled before this function is called)
	    if (r.isOpaqueOrInOG != opaqueList) {
		continue;
	    }
	    // Get the list of renderMolecules for this transform
	    renderMoleculeList = renderMoleculeMap.get(r.localToVworld);
            if (renderMoleculeList == null) {
                renderMoleculeList = r;
		renderMoleculeMap.put(r.localToVworld, renderMoleculeList);
		// Add this renderMolecule at the beginning of RM list
		if (startList == null) {
		    startList = r;
		    r.nextMap = null;
		    r.prevMap = null;
		    startList.dirtyAttrsAcrossRms = RenderMolecule.ALL_DIRTY_BITS;
		}
		else {
		    r.nextMap = startList;
		    startList.prevMap = r;
		    startList = r;
		    startList.nextMap.checkEquivalenceWithLeftNeighbor(r,
						RenderMolecule.ALL_DIRTY_BITS);
		}

            }
            else {
                // Insert the renderMolecule next to a RM that has equivalent
		// texture unit state
                if ((head = insertRenderMolecule(r, renderMoleculeList)) != null) {
		    if (renderMoleculeList.prevMap != null) {
			renderMoleculeList.prevMap.nextMap = head;
		    }
		    head.prevMap = renderMoleculeList.prevMap;
		    renderMoleculeList.prevMap = null;
		    renderMoleculeList = head;
		    changed = true;
		}
            }
	    for (i = 1; i < curList.size(); i++) {
			r = curList.get(i);
	       // If this is a opaque one , but has been switched to a transparentList or
	       // vice-versa (dur to changeLists function called before this), then
	       // do nothing!
	       // For changedFrequent case: Consider the case when a RM is added
	       // (so is in the addRM list) and then
	       // a change  in transparent value occurs that make it from opaque to
	       // transparent (the switch is handled before this function is called)
	       if (r.isOpaqueOrInOG != opaqueList)
		   continue;
	       if ((head = insertRenderMolecule(r, renderMoleculeList)) != null) {
		    if (renderMoleculeList.prevMap != null) {
			renderMoleculeList.prevMap.nextMap = head;
		    }
		    head.prevMap = renderMoleculeList.prevMap;
		    renderMoleculeList.prevMap = null;
		    renderMoleculeList = head;
		    changed = true;
	       }

	    }
	    if (changed) {
		renderMoleculeMap.put(r.localToVworld, renderMoleculeList);
		if (renderMoleculeList.prevMap != null) {
		    renderMoleculeList.checkEquivalenceWithLeftNeighbor(
					renderMoleculeList.prevMap,
					RenderMolecule.ALL_DIRTY_BITS);
		}
		else {
		    startList = renderMoleculeList;
		    startList.dirtyAttrsAcrossRms =
					RenderMolecule.ALL_DIRTY_BITS;
		}
	    }
	}

        addRMs.clear();
	return startList;
    }


    // XXXX: Could the analysis be done during insertRenderMolecule?
    // Return the head of the list,
    // if the insertion occurred at beginning of the list
    RenderMolecule insertRenderMolecule(RenderMolecule r,
				RenderMolecule renderMoleculeList) {
        RenderMolecule rm, retval;

        // Look for a RM that has an equivalent material
        rm = renderMoleculeList;
        while (rm != null) {
            if (rm.material == r.material ||
                (rm.definingMaterial != null &&
                 rm.definingMaterial.equivalent(r.definingMaterial))) {
                // Put it here
                r.next = rm;
                r.prev = rm.prev;
                if (rm.prev == null) {
                    renderMoleculeList = r;
		    retval = renderMoleculeList;
                } else {
                    rm.prev.next = r;
		    retval = null;
                }
                rm.prev = r;
                r.checkEquivalenceWithBothNeighbors(RenderMolecule.ALL_DIRTY_BITS);
                return retval;
            }
	    // If they are not equivalent, then skip to the first one
	    // that has a different material using the dirty bits
	    else {
		rm = rm.next;
		while (rm != null &&
		       ((rm.dirtyAttrsAcrossRms & RenderMolecule.MATERIAL_DIRTY) == 0)) {
		    rm = rm.next;
		}
	    }
        }
        // Just put it up front
        r.next = renderMoleculeList;
        renderMoleculeList.prev = r;
        renderMoleculeList = r;
        r.checkEquivalenceWithBothNeighbors(RenderMolecule.ALL_DIRTY_BITS);
	return renderMoleculeList;
    }


    /**
     * Adds the given RenderMolecule to this TextureBin
     */
    void addRenderMolecule(RenderMolecule r, RenderBin rb) {
        r.textureBin = this;

	HashMap<Transform3D[], ArrayList<RenderMolecule>> map;
	if (r.isOpaqueOrInOG)
	    map = addOpaqueRMs;
	else
	    map = addTransparentRMs;

	ArrayList<RenderMolecule> list = map.get(r.localToVworld);
	if (list == null) {
		list = new ArrayList<RenderMolecule>();
		map.put(r.localToVworld, list);
	}
	list.add(r);

        if ((tbFlag & TextureBin.ON_UPDATE_LIST) == 0) {
            tbFlag |= TextureBin.ON_UPDATE_LIST;
            rb.objUpdateList.add(this);
        }
    }

    /**
     * Removes the given RenderMolecule from this TextureBin
     */
    void removeRenderMolecule(RenderMolecule r) {
	int index;
	boolean found = false;
	RenderMolecule rmlist;
	HashMap<Transform3D[], ArrayList<RenderMolecule>> addMap;
	HashMap<Transform3D[], RenderMolecule> allMap;
        r.textureBin = null;

	if (r.isOpaqueOrInOG) {
	    rmlist = opaqueRMList;
	    allMap = opaqueRenderMoleculeMap;
	    addMap = addOpaqueRMs;
	}
	else {
	    rmlist = transparentRMList;
	    allMap = transparentRenderMoleculeMap;
	    addMap = addTransparentRMs;
	}
	// If the renderMolecule being remove is contained in addRMs, then
	// remove the renderMolecule from the addList
	ArrayList<RenderMolecule> list = addMap.get(r.localToVworld);
	if (list != null) {
	    if ((index = list.indexOf(r)) != -1) {
		list.remove(index);
		// If this was the last element for this localToVworld, then remove
		// the entry from the addRMs list
		if (list.isEmpty()) {
		    addMap.remove(r.localToVworld);
		}

		r.prev = null;
		r.next = null;
		found = true;
	    }
	}
	if (!found) {
	    RenderMolecule head = removeOneRM(r, allMap, rmlist);

	    r.soleUserCompDirty = 0;
	    r.onUpdateList = 0;
	    if (r.definingPolygonAttributes != null &&
		(r.definingPolygonAttributes.changedFrequent != 0))
		r.definingPolygonAttributes = null;

	    if (r.definingLineAttributes != null &&
		(r.definingLineAttributes.changedFrequent != 0))
		r.definingLineAttributes = null;

	    if (r.definingPointAttributes != null &&
		(r.definingPointAttributes.changedFrequent != 0))
		r.definingPointAttributes = null;

	    if (r.definingMaterial != null &&
		(r.definingMaterial.changedFrequent != 0))
		r.definingMaterial = null;

	    if (r.definingColoringAttributes != null &&
		(r.definingColoringAttributes.changedFrequent != 0))
		r.definingColoringAttributes = null;

	    if (r.definingTransparency != null &&
		(r.definingTransparency.changedFrequent != 0))
		r.definingTransparency = null;

	    renderBin.removeRenderMolecule(r);
	    if (r.isOpaqueOrInOG) {
		opaqueRMList = head;
	    }
	    else {
		transparentRMList = head;
	    }

	}
	// If the renderMolecule removed is not opaque then ..
	if (!r.isOpaqueOrInOG && transparentRMList == null && (renderBin.transpSortMode == View.TRANSPARENCY_SORT_NONE ||
							       environmentSet.lightBin.geometryBackground != null)) {
	    renderBin.removeTransparentObject(this);
	}
	// If the rm removed is the one that is referenced in the tinfo
	// then change this reference
	else if (parentTInfo != null && parentTInfo.rm == r) {
	    parentTInfo.rm = transparentRMList;
	}
        // Removal of this texture setting from the texCoordGenartion
        // is done during the removeRenderAtom routine in RenderMolecule.java
        // Only remove this texture bin if there are no more renderMolcules
        // waiting to be added
        if (opaqueRenderMoleculeMap.isEmpty() && addOpaqueRMs.isEmpty() &&
	    transparentRenderMoleculeMap.isEmpty() &&  addTransparentRMs.isEmpty()) {
	    if ((tbFlag & TextureBin.ON_RENDER_BIN_LIST) != 0) {
	        tbFlag &= ~TextureBin.ON_RENDER_BIN_LIST;
		renderBin.removeTextureBin(this);
	    }

            shaderBin.removeTextureBin(this);
	    texUnitState = null;
        }
    }

    /**
     * This method is called to update the state for this
     * TextureBin. This is only applicable in the single-pass case.
     * Multi-pass render will have to take care of its own
     * state update.
     */
    void updateAttributes(Canvas3D cv) {

        boolean dirty = ((cv.canvasDirty & (Canvas3D.TEXTUREBIN_DIRTY|
					    Canvas3D.TEXTUREATTRIBUTES_DIRTY)) != 0);

	if (cv.textureBin == this  && !dirty) {
	    return;
	}

	cv.textureBin = this;

	// save the current number of active texture unit so as
        // to be able to reset the one that is not enabled in this bin

	int lastActiveTexUnitIdx = -1;

        // Get the number of available texture units; this depends on
        // whether or not shaders are being used.
        boolean useShaders = (shaderBin.shaderProgram != null);
        int availableTextureUnits =
                useShaders ? cv.maxTextureImageUnits : cv.maxTextureUnits;

        // If the number of active texture units is greater than the number of
        // supported units, then we
        // need to set a flag indicating that the texture units are invalid.
        boolean disableTexture = false;

        if (numActiveTexUnit > availableTextureUnits) {
            disableTexture = true;
//            System.err.println("*** TextureBin : number of texture units exceeded");
        }

        // set the number active texture unit in Canvas3D
        if (disableTexture) {
            cv.setNumActiveTexUnit(0);
        }
        else {
            cv.setNumActiveTexUnit(numActiveTexUnit);
        }

	// state update
	if (numActiveTexUnit <= 0 || disableTexture) {
            if (cv.getLastActiveTexUnit() >= 0) {
	        // no texture units enabled

	        // when the canvas supports multi texture units,
		// we'll need to reset texture for all texture units
                if (cv.multiTexAccelerated) {
                    for (int i = 0; i <= cv.getLastActiveTexUnit(); i++) {
                        cv.resetTexture(cv.ctx, i);
                    }
                    // set the active texture unit back to 0
		    cv.setNumActiveTexUnit(0);
		    cv.activeTextureUnit(cv.ctx, 0);
                } else {
                    cv.resetTexture(cv.ctx, -1);
                }
		cv.setLastActiveTexUnit(-1);
	    }
	} else {

            int j = 0;

	    for (int i = 0; i < texUnitState.length; i++) {

		if (j >= cv.texUnitState.length) {
		    // We finish enabling the texture state.
		    // Note that it is possible
		    // texUnitState.length > cv.texUnitState.length

		    break;
		}

		if ((texUnitState[i] != null) &&
			      texUnitState[i].isTextureEnabled()) {
		    if (dirty ||
			 cv.texUnitState[j].mirror == null ||
			 cv.texUnitState[j].mirror != texUnitState[i].mirror) {
		        // update the texture unit state
		        texUnitState[i].updateNative(j, cv, false, false);
			cv.texUnitState[j].mirror = texUnitState[i].mirror;
		    }

		    // create a mapping that maps an active texture
		    // unit to a texture unit state

		    lastActiveTexUnitIdx = j;
		} else {
		    if (j <= cv.getLastActiveTexUnit()) {
			cv.resetTexture(cv.ctx, j);
		    }
		}

                j++;
	    }

            // make sure to disable the remaining texture units
            // since they could have been enabled from the previous
            // texture bin

            for (int i = j; i <= cv.getLastActiveTexUnit(); i++) {
		cv.resetTexture(cv.ctx, i);
            }

	    cv.setLastActiveTexUnit(lastActiveTexUnitIdx);

            // set the active texture unit back to 0
            cv.activeTextureUnit(cv.ctx, 0);

	}
	cv.canvasDirty &= ~Canvas3D.TEXTUREBIN_DIRTY;
    }


    /**
     * Renders this TextureBin
     */
    void render(Canvas3D cv) {
	render(cv, (Object) opaqueRMList);
    }

    void render(Canvas3D cv, Object rlist) {
	/*
	System.err.println("TextureBin/render " + this +
		" numActiveTexUnit= " + numActiveTexUnit +
		" maxTextureUnits= " + cv.maxTextureUnits);
	*/

	// include this TextureBin to the to-be-updated state set in canvas
	cv.setStateToUpdate(Canvas3D.TEXTUREBIN_BIT, this);

        renderList(cv, USE_DISPLAYLIST, rlist);
    }


    /**
     * render a render list
     */
    void renderList(Canvas3D cv, int pass, Object rlist) {
        assert pass < 0;

	if (rlist instanceof RenderMolecule) {
	    renderList(cv, pass, (RenderMolecule) rlist);
	} else if (rlist instanceof TransparentRenderingInfo) {
	    renderList(cv, pass, (TransparentRenderingInfo) rlist);
	}
    }


    /**
     * render list of RenderMolecule
     */
    void renderList(Canvas3D cv, int pass, RenderMolecule rlist) {
        assert pass < 0;

        // bit mask of all attr fields that are equivalent across
	// renderMolecules thro. ORing of invisible RMs.
	int combinedDirtyBits = 0;
	boolean rmVisible = true;
        RenderMolecule rm = rlist;

        while (rm != null) {
	    if(rmVisible) {
		combinedDirtyBits = rm.dirtyAttrsAcrossRms;
	    }
	    else {
		combinedDirtyBits |= rm.dirtyAttrsAcrossRms;
	    }

	    rmVisible = rm.render(cv, pass, combinedDirtyBits);


            // next render molecule or the nextmap
            if (rm.next == null) {
                rm = rm.nextMap;
            }
            else {
                rm = rm.next;
            }
        }
    }


    /**
     * render sorted transparent list
     */
    void renderList(Canvas3D cv, int pass, TransparentRenderingInfo tinfo) {
        assert pass < 0;

	RenderMolecule rm = tinfo.rm;
	if (rm.isSwitchOn()) {
	    rm.transparentSortRender(cv, pass, tinfo);
	}
    }


    void changeLists(RenderMolecule r) {
	RenderMolecule renderMoleculeList, rmlist = null, head;
	HashMap<Transform3D[], RenderMolecule> allMap = null;
	ArrayList list;
	int index;
	boolean newRM = false;
	//	System.err.println("changeLists r = "+r+" tBin = "+this);
	// If its a new RM then do nothing, otherwise move lists
	if (r.isOpaqueOrInOG) {
	    if (opaqueRMList == null &&
		(r.prev == null && r.prevMap == null && r.next == null &&
		r.nextMap == null)) {
		newRM = true;
	    }
	    else {
		rmlist = opaqueRMList;
		allMap = opaqueRenderMoleculeMap;
	    }

	}
	else {
	    if (transparentRMList == null &&
		(r.prev == null && r.prevMap == null && r.next == null &&
		r.nextMap == null) ){
		newRM = true;
	    }
	    else {
		rmlist = transparentRMList;
		allMap = transparentRenderMoleculeMap;
	    }
	}
	if (!newRM) {
	    head = removeOneRM(r, allMap, rmlist);

	    if (r.isOpaqueOrInOG) {
		opaqueRMList = head;
	    }
	    else {
		transparentRMList = head;
		if (transparentRMList == null &&
		    (renderBin.transpSortMode == View.TRANSPARENCY_SORT_NONE ||
		     environmentSet.lightBin.geometryBackground != null)) {
		    renderBin.removeTransparentObject(this);
		}
		// Issue 129: remove the RM's render atoms from the
		// list of transparent render atoms
		if ((renderBin.transpSortMode == View.TRANSPARENCY_SORT_GEOMETRY) &&
		    (environmentSet.lightBin.geometryBackground == null)) {
		    r.addRemoveTransparentObject(renderBin, false);
		}
	    }
	}
	HashMap<Transform3D[], RenderMolecule> renderMoleculeMap;
	RenderMolecule startList;

	// Now insert in the other bin
	r.evalAlphaUsage(shaderBin.attributeBin.definingRenderingAttributes, texUnitState);
	r.isOpaqueOrInOG = r.isOpaque() ||r.inOrderedGroup;
	if (r.isOpaqueOrInOG) {
	    startList = opaqueRMList;
	    renderMoleculeMap = opaqueRenderMoleculeMap;
	    markDlistAsDirty(r);
	}
	else {
	    startList = transparentRMList;
	    renderMoleculeMap = transparentRenderMoleculeMap;
	    if ((r.primaryMoleculeType &RenderMolecule.DLIST_MOLECULE) != 0 &&
		renderBin.transpSortMode != View.TRANSPARENCY_SORT_NONE) {
		renderBin.addDisplayListResourceFreeList(r);
		renderBin.removeDirtyRenderMolecule(r);

		r.vwcBounds.set(null);
		r.displayListId = 0;
		r.displayListIdObj = null;
		// Change the group type for all the rlistInfo in the primaryList
		RenderAtomListInfo rinfo = r.primaryRenderAtomList;
		while (rinfo != null) {
		    rinfo.groupType = RenderAtom.SEPARATE_DLIST_PER_RINFO;
		    if (rinfo.renderAtom.dlistIds == null) {
			rinfo.renderAtom.dlistIds = new int[rinfo.renderAtom.rListInfo.length];

			for (int k = 0; k < rinfo.renderAtom.dlistIds.length; k++) {
			    rinfo.renderAtom.dlistIds[k] = -1;
			}
		    }
		    if (rinfo.renderAtom.dlistIds[rinfo.index] == -1) {
			rinfo.renderAtom.dlistIds[rinfo.index] = VirtualUniverse.mc.getDisplayListId().intValue();
			renderBin.addDlistPerRinfo.add(rinfo);
		    }
		    rinfo = rinfo.next;
		}
		r.primaryMoleculeType = RenderMolecule.SEPARATE_DLIST_PER_RINFO_MOLECULE;
	    }
	    else {
		markDlistAsDirty(r);
	    }

	}
	renderMoleculeList = renderMoleculeMap.get(r.localToVworld);

	if (renderMoleculeList == null) {
	    renderMoleculeList = r;
	    renderMoleculeMap.put(r.localToVworld, renderMoleculeList);
	    // Add this renderMolecule at the beginning of RM list
	    if (startList == null) {
		startList = r;
		r.nextMap = null;
		r.prevMap = null;
	    }
	    else {
		r.nextMap = startList;
		startList.prevMap = r;
		startList = r;
		startList.nextMap.checkEquivalenceWithLeftNeighbor(r,RenderMolecule.ALL_DIRTY_BITS);
	    }
            // Issue 67 : since we are adding the new RM at the head, we must
            // set all dirty bits unconditionally
            startList.dirtyAttrsAcrossRms = RenderMolecule.ALL_DIRTY_BITS;
	}
	else {
	    // Insert the renderMolecule next to a RM that has equivalent
	    // texture unit state
	    if ((head = insertRenderMolecule(r, renderMoleculeList)) != null) {
		if (renderMoleculeList.prevMap != null) {
		    renderMoleculeList.prevMap.nextMap = head;
		}
		head.prevMap = renderMoleculeList.prevMap;
		renderMoleculeList.prevMap = null;
		renderMoleculeList = head;
		renderMoleculeMap.put(r.localToVworld, renderMoleculeList);
		if (renderMoleculeList.prevMap != null) {
		    renderMoleculeList.checkEquivalenceWithLeftNeighbor(renderMoleculeList.prevMap,
									RenderMolecule.ALL_DIRTY_BITS);
		}
		else {
		    startList.dirtyAttrsAcrossRms = RenderMolecule.ALL_DIRTY_BITS;
		    startList = renderMoleculeList;
		}
	    }

	}
	if (r.isOpaqueOrInOG) {
	    opaqueRMList = startList;
	}
	else {
	    // If transparent and not in bg geometry and inodepth sorted transparency
	    if (transparentRMList == null&&
		(renderBin.transpSortMode == View.TRANSPARENCY_SORT_NONE ||
		 environmentSet.lightBin.geometryBackground != null)) {
		transparentRMList = startList;
		renderBin.addTransparentObject(this);
	    }
	    else {
		transparentRMList = startList;
	    }
	    // Issue 129: add the RM's render atoms to the list of
	    // transparent render atoms
	    // XXXX: do we need to resort the list after the add???
	    if ((renderBin.transpSortMode == View.TRANSPARENCY_SORT_GEOMETRY) &&
		(environmentSet.lightBin.geometryBackground == null)) {
		r.addRemoveTransparentObject(renderBin, true);
	    }
	}
    }

    RenderMolecule removeOneRM(RenderMolecule r, HashMap<Transform3D[], RenderMolecule> allMap, RenderMolecule list) {
	RenderMolecule rmlist = list;
	// In the middle, just remove and update
	if (r.prev != null && r.next != null) {
	    r.prev.next = r.next;
	    r.next.prev = r.prev;
	    r.next.checkEquivalenceWithLeftNeighbor(r.prev,RenderMolecule.ALL_DIRTY_BITS);
	}
	// If whats is removed is at the end of an entry
	else if (r.prev != null && r.next == null) {
	    r.prev.next = r.next;
	    r.prev.nextMap = r.nextMap;
	    if (r.nextMap != null) {
		r.nextMap.prevMap = r.prev;
		r.nextMap.checkEquivalenceWithLeftNeighbor(r.prev,RenderMolecule.ALL_DIRTY_BITS);
	    }
	}
	else if (r.prev == null && r.next != null) {
	    r.next.prev = null;
	    r.next.prevMap = r.prevMap;
	    if (r.prevMap != null) {
		r.prevMap.nextMap = r.next;
		r.next.checkEquivalenceWithLeftNeighbor(r.prevMap,RenderMolecule.ALL_DIRTY_BITS);
	    }
	    // Head of the rmList
	    else {
		rmlist = r.next;
		rmlist.dirtyAttrsAcrossRms = RenderMolecule.ALL_DIRTY_BITS;
	    }
	    allMap.put(r.localToVworld, r.next);
	}
	// Update the maps and remove this entry from the map list
	else if (r.prev == null && r.next == null) {
	    if (r.prevMap != null) {
		r.prevMap.nextMap = r.nextMap;

	    }
	    else {
		rmlist = r.nextMap;
		if (r.nextMap != null) {
		    rmlist.dirtyAttrsAcrossRms = RenderMolecule.ALL_DIRTY_BITS;
		}
	    }
	    if (r.nextMap != null) {
		r.nextMap.prevMap = r.prevMap;
		if (r.prevMap != null) {
		    r.nextMap.checkEquivalenceWithLeftNeighbor(r.prevMap,RenderMolecule.ALL_DIRTY_BITS);
		}

	    }

	    allMap.remove(r.localToVworld);


	}
	r.prev = null;
	r.next = null;
	r.prevMap = null;
	r.nextMap = null;
	return rmlist;
    }

    void markDlistAsDirty(RenderMolecule r) {

	if (r.primaryMoleculeType == RenderMolecule.DLIST_MOLECULE) {
	    renderBin.addDirtyRenderMolecule(r);
	}
	else if (r.primaryMoleculeType == RenderMolecule.SEPARATE_DLIST_PER_RINFO_MOLECULE) {
	    RenderAtomListInfo ra = r.primaryRenderAtomList;
	    while (ra != null) {
		renderBin.addDlistPerRinfo.add(ra);
		ra = ra.next;
	    }
	}
    }


    void decrActiveRenderMolecule() {
	numEditingRenderMolecules--;

	if (numEditingRenderMolecules == 0) {

	    // if number of editing renderMolecules goes to 0,
	    // inform the shaderBin that this textureBin goes to
	    // zombie state

	    shaderBin.decrActiveTextureBin();
	}
    }

    void incrActiveRenderMolecule() {

	if (numEditingRenderMolecules == 0) {

	    // if this textureBin is in zombie state, inform
	    // the shaderBin that this textureBin is activated again.

	    shaderBin.incrActiveTextureBin();
	}

	numEditingRenderMolecules++;
    }
}
