/*
 * $RCSfile$
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import javax.vecmath.*;
import java.util.ArrayList;

/**
 * The AttributeBin manages a collection of TextureBin objects.
 * All objects in the AttributeBin share the same RenderingAttributes
 */

class AttributeBin extends Object implements ObjectUpdate {

    /**
     * The RenderingAttributes for this AttributeBin
     */
    RenderingAttributesRetained definingRenderingAttributes = null;

    /**
     * The EnvirionmentSet that this AttributeBin resides
     */
    EnvironmentSet environmentSet = null;

    /**
     * The references to the next and previous AttributeBins in the
     * list.
     */
    AttributeBin next = null;
    AttributeBin prev = null;

    /**
     * The list of TextureBins in this AttributeBin
     */
    TextureBin textureBinList = null;


    /**
     * The list of TextureBins to be added for the next frame
     */
    ArrayList addTextureBins = new ArrayList();

    /**
     *  List of TextureBins to be added next frame
     */
    ArrayList addTBs = new ArrayList();


    /**
     * If the RenderingAttribute component of the appearance will be changed
     * frequently, then confine it to a separate bin
     */
    boolean soleUser = false;
    AppearanceRetained app = null;

    /**
     *  List of TextureBins to be removeded next frame
     */
    ArrayList removeTBs = new ArrayList();

    int onUpdateList = 0;
    static int ON_OBJ_UPDATE_LIST = 0x1;
    static int ON_CHANGED_FREQUENT_UPDATE_LIST = 0x2;

    // Cache it outside, to avoid the "if" check in renderMethod
    // for whether the definingRendering attrs is non-null;
    boolean ignoreVertexColors = false;

    // TODO: use definingMaterial etc. instead of these
    // when sole user is completely implement
    RenderingAttributesRetained renderingAttrs;

    int numEditingTextureBins = 0;



    AttributeBin(AppearanceRetained app, RenderingAttributesRetained renderingAttributes, RenderBin rBin) {
	reset(app, renderingAttributes, rBin);
    }

    void reset(AppearanceRetained app, RenderingAttributesRetained renderingAttributes, RenderBin rBin) {
	prev = null;
	next = null;
	textureBinList = null;
	onUpdateList = 0;
	numEditingTextureBins = 0;
        renderingAttrs = renderingAttributes;

	if (app != null) {
	    soleUser = ((app.changedFrequent & AppearanceRetained.RENDERING) != 0);
	}
	else {
	    soleUser = false;
	}
	//	System.out.println("soleUser = "+soleUser+" renderingAttributes ="+renderingAttributes);
	// Set the appearance only for soleUser case
	if (soleUser)
	    this.app = app;
	else
	    app = null;

	if (renderingAttributes != null) {
	    if (renderingAttributes.changedFrequent != 0) {
		definingRenderingAttributes = renderingAttributes;
		if ((onUpdateList & ON_CHANGED_FREQUENT_UPDATE_LIST) == 0 ) {
		    rBin.aBinUpdateList.add(this);
		    onUpdateList |= AttributeBin.ON_CHANGED_FREQUENT_UPDATE_LIST;
		}
	    }
	    else {
		if (definingRenderingAttributes != null) {
		    definingRenderingAttributes.set(renderingAttributes);
		}
		else {
		    definingRenderingAttributes = (RenderingAttributesRetained)renderingAttributes.clone();
		}
	    }
	    ignoreVertexColors = definingRenderingAttributes.ignoreVertexColors;
	} else {
	    definingRenderingAttributes = null;
	    ignoreVertexColors = false;
	}
    }


    /**
     * This tests if the given attributes match this AttributeBin
     */
    boolean equals(RenderingAttributesRetained renderingAttributes, RenderAtom ra) {

	// If the any reference to the appearance components  that is cached renderMolecule
	// can change frequently, make a separate bin
	// If the any reference to the appearance components  that is cached renderMolecule
	// can change frequently, make a separate bin
	if (soleUser || (ra.geometryAtom.source.appearance != null &&
			 ((ra.geometryAtom.source.appearance.changedFrequent & AppearanceRetained.RENDERING) != 0))) {
	    if (app == (Object)ra.geometryAtom.source.appearance) {

		// if this AttributeBin is currently on a zombie state,
                // we'll need to put it on the update list to reevaluate
                // the state, because while it is on a zombie state,
                // rendering attributes reference could have been changed. 
	        // Example, application could have detached an appearance,
                // made changes to the reference, and then
                // reattached the appearance. In this case, the rendering
                // attributes reference change would not have reflected to 
	        // the AttributeBin

                if (numEditingTextureBins == 0) {
		    if ((onUpdateList & ON_CHANGED_FREQUENT_UPDATE_LIST) == 0) {
			environmentSet.renderBin.aBinUpdateList.add(this);
			onUpdateList |= 
				AttributeBin.ON_CHANGED_FREQUENT_UPDATE_LIST;
		    }
		}
		return true;
	    }
	    else {
		return false;
	    }
	    
	}
	// Either a changedFrequent or a null case
	// and the incoming one is not equal or null
	// then return;	
	// This check also handles null == null case
	if (definingRenderingAttributes != null) {
	    if ((this.definingRenderingAttributes.changedFrequent != 0) ||
		(renderingAttributes !=null && renderingAttributes.changedFrequent != 0))
		if (definingRenderingAttributes == renderingAttributes) {
		    if (definingRenderingAttributes.compChanged != 0) {
			if ((onUpdateList & ON_CHANGED_FREQUENT_UPDATE_LIST) == 0 ) {
			    environmentSet.renderBin.aBinUpdateList.add(this);
			    onUpdateList |= AttributeBin.ON_CHANGED_FREQUENT_UPDATE_LIST;
			}
		    }
		}
		else {
		    return false;
		}
	    else if (!definingRenderingAttributes.equivalent(renderingAttributes)) {
		return false;
	    }
	}
	else if (renderingAttributes != null) {
	    return false;
	}



	return (true);
    }


    public void updateObject() {
	TextureBin t;
	int i;
	
	if (addTBs.size() > 0) {
	    t = (TextureBin)addTBs.get(0);
	    if (textureBinList == null) {
		textureBinList = t;

	    }
	    else {
		// Look for a TextureBin that has the same texture
		insertTextureBin(t);	
	    }	    
	    for (i = 1; i < addTBs.size() ; i++) {
		t = (TextureBin)addTBs.get(i);
		// Look for a TextureBin that has the same texture
		insertTextureBin(t);

	    }
	}
	addTBs.clear();
	onUpdateList &= ~ON_OBJ_UPDATE_LIST;
    }
    
    void insertTextureBin(TextureBin t) {
	TextureBin tb;
	int i;
	TextureRetained texture = null;

	if (t.texUnitState != null && t.texUnitState.length > 0) {
	    if (t.texUnitState[0] != null) {
	        texture = t.texUnitState[0].texture;
	    }
	}

	// use the texture in the first texture unit as the sorting criteria
	if (texture != null) {
	    tb = textureBinList; 
	    while (tb != null) { 
		if (tb.texUnitState == null || tb.texUnitState[0] == null ||
			tb.texUnitState[0].texture != texture) {
		    tb = tb.next;
		} else {
		    // put it here  
		    t.next = tb; 
		    t.prev = tb.prev; 
		    if (tb.prev == null) { 
		        textureBinList = t; 
		    } 
		    else { 
		        tb.prev.next = t; 
		    } 
		    tb.prev = t; 
		    return; 
	        } 
	    }
	} 
	// Just put it up front
	t.prev = null;
	t.next = textureBinList;
	textureBinList.prev = t;
	textureBinList = t;

	t.tbFlag &= ~TextureBin.RESORT;
    }


    /**
     * reInsert textureBin if the first texture is different from
     * the previous bin and different from the next bin
     */
    void reInsertTextureBin(TextureBin tb) {

        TextureRetained texture = null,
                        prevTexture = null,
                        nextTexture = null;

        if (tb.texUnitState != null && tb.texUnitState[0] != null) {
            texture = tb.texUnitState[0].texture;
        }

        if (tb.prev != null && tb.prev.texUnitState != null) {
            prevTexture = tb.prev.texUnitState[0].texture;
        }

        if (texture != prevTexture) {
            if (tb.next != null && tb.next.texUnitState != null) {
                nextTexture = tb.next.texUnitState[0].texture;
            }
            if (texture != nextTexture) {
                if (tb.prev != null && tb.next != null) {
                    tb.prev.next = tb.next;
		    tb.next.prev = tb.prev;
                    insertTextureBin(tb);
                }
            }
        }
    }


    /**
     * Adds the given TextureBin to this AttributeBin.
     */
    void addTextureBin(TextureBin t, RenderBin rb, RenderAtom ra) {
	int i;
	t.attributeBin = this;
        AppearanceRetained raApp = ra.geometryAtom.source.appearance;
	RenderingAttributesRetained rAttrs = 
		(raApp == null)? null : raApp.renderingAttributes;
	if (!soleUser && renderingAttrs != rAttrs) {
	    // no longer sole user
            renderingAttrs = definingRenderingAttributes;
	}
	addTBs.add(t);
	if ((onUpdateList & ON_OBJ_UPDATE_LIST) == 0) {
	    onUpdateList |= ON_OBJ_UPDATE_LIST;
	    rb.objUpdateList.add(this);
	}

    }

    /**
     * Removes the given TextureBin from this AttributeBin.
     */
    void removeTextureBin(TextureBin t) {

	int i;
	TextureRetained tex;
	
	t.attributeBin = null;
	// If the TextureBin being remove is contained in addTBs, then
	// remove the TextureBin from the addList
	if (addTBs.contains(t)) {
	    addTBs.remove(addTBs.indexOf(t));
	}
	else {
	    if (t.prev == null) { // At the head of the list
		textureBinList = t.next;
		if (t.next != null) {
		    t.next.prev = null;
		}
	    } else { // In the middle or at the end.
		t.prev.next = t.next;
		if (t.next != null) {
		    t.next.prev = t.prev;
		}
	    }
	}
	t.prev = null;
	t.next = null;

	t.clear();

	environmentSet.renderBin.textureBinFreelist.add(t);

	if (textureBinList == null && addTBs.size() == 0 ) {
	    // Note: Removal of this attributebin as a user of the rendering
	    // atttrs is done during removeRenderAtom() in RenderMolecule.java
	    environmentSet.removeAttributeBin(this);
	}
    }

    /**
     * Renders this AttributeBin
     */
    void render(Canvas3D cv) {

	TextureBin t;
	
	boolean visible = (definingRenderingAttributes == null || 
	    		       definingRenderingAttributes.visible);

	if ( (environmentSet.renderBin.view.viewCache.visibilityPolicy
			== View.VISIBILITY_DRAW_VISIBLE && !visible) ||
	     (environmentSet.renderBin.view.viewCache.visibilityPolicy
			== View.VISIBILITY_DRAW_INVISIBLE && visible)) {
	    return;
	}
	        	

        // include this AttributeBin to the to-be-updated list in Canvas
        cv.setStateToUpdate(Canvas3D.ATTRIBUTEBIN_BIT, this);

	t = textureBinList;
	while (t != null) {
	    t.render(cv);
	    t = t.next;
	}
    }


    void updateAttributes(Canvas3D cv) {
	if ((cv.canvasDirty & Canvas3D.ATTRIBUTEBIN_DIRTY) != 0) {
	    // Update Attribute Bundles
	    if (definingRenderingAttributes == null) {
	        cv.resetRenderingAttributes(cv.ctx,
					    cv.depthBufferWriteEnableOverride,
					    cv.depthBufferEnableOverride);
	    } else {
	        definingRenderingAttributes.updateNative(
				    cv.ctx,
				    cv.depthBufferWriteEnableOverride,
				    cv.depthBufferEnableOverride);
	    }
	    cv.renderingAttrs = renderingAttrs;
	}

	else if (cv.renderingAttrs != renderingAttrs && 
			cv.attributeBin != this) {
	    // Update Attribute Bundles
	    if (definingRenderingAttributes == null) {
		cv.resetRenderingAttributes(
					cv.ctx,
					cv.depthBufferWriteEnableOverride,
					cv.depthBufferEnableOverride);
	    } else {
		definingRenderingAttributes.updateNative(
				        cv.ctx,
					cv.depthBufferWriteEnableOverride,
				    	cv.depthBufferEnableOverride);
	    }
	    cv.renderingAttrs = renderingAttrs;
	} 
	cv.attributeBin = this;
	cv.canvasDirty &= ~Canvas3D.ATTRIBUTEBIN_DIRTY;
    }

    void updateNodeComponent() {
	// May be in the freelist already (due to freq bit changing)
	// if so, don't update anything
	if ((onUpdateList & ON_CHANGED_FREQUENT_UPDATE_LIST) != 0) {
	    if (soleUser) {
		boolean cloned = definingRenderingAttributes != null && definingRenderingAttributes != renderingAttrs;
		renderingAttrs = app.renderingAttributes;

		if (renderingAttrs == null) {
		    definingRenderingAttributes = null;
		    ignoreVertexColors = false;
		}
		else {
		    if (renderingAttrs.changedFrequent != 0) {
			definingRenderingAttributes = renderingAttrs;
		    }
		    else {
			if (cloned) {
			    definingRenderingAttributes.set(renderingAttrs);
			}
			else {
			    definingRenderingAttributes = (RenderingAttributesRetained)renderingAttrs.clone();
			}
		    }
		    ignoreVertexColors = definingRenderingAttributes.ignoreVertexColors;
		}
	    }
	    else {
		ignoreVertexColors = definingRenderingAttributes.ignoreVertexColors;
	    }
	}

	onUpdateList &= ~ON_CHANGED_FREQUENT_UPDATE_LIST;
    }

    void incrActiveTextureBin() {
	numEditingTextureBins++;
    }

    void decrActiveTextureBin() {
	numEditingTextureBins--;
    }
}
