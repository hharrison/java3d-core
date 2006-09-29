/*
 * $RCSfile$
 *
 * Copyright (c) 2006 Sun Microsystems, Inc. All rights reserved.
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
     * The RenderBin for this object
     */
    RenderBin renderBin = null;

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
     * The list of ShaderBins in this AttributeBin
     */
    ShaderBin shaderBinList = null;

    /**
     *  List of shaderBins to be added next frame
     */
    ArrayList addShaderBins = new ArrayList();

    /**
     * If the RenderingAttribute component of the appearance will be changed
     * frequently, then confine it to a separate bin
     */
    boolean soleUser = false;
    AppearanceRetained app = null;

    int onUpdateList = 0;
    static int ON_OBJ_UPDATE_LIST = 0x1;
    static int ON_CHANGED_FREQUENT_UPDATE_LIST = 0x2;

    // Cache it outside, to avoid the "if" check in renderMethod
    // for whether the definingRendering attrs is non-null;
    boolean ignoreVertexColors = false;

    // XXXX: use definingMaterial etc. instead of these
    // when sole user is completely implement
    RenderingAttributesRetained renderingAttrs;

    int numEditingShaderBins = 0;

    AttributeBin(AppearanceRetained app, RenderingAttributesRetained renderingAttributes, RenderBin rBin) {

	reset(app, renderingAttributes, rBin);
    }

    void reset(AppearanceRetained app, RenderingAttributesRetained renderingAttributes, RenderBin rBin) {
	prev = null;
	next = null;
	shaderBinList = null;
	onUpdateList = 0;
	numEditingShaderBins = 0;
        renderingAttrs = renderingAttributes;

	renderBin = rBin;

        // Issue 249 - check for sole user only if property is set
        soleUser = false;
        if (VirtualUniverse.mc.allowSoleUser) {
            if (app != null) {
                soleUser = ((app.changedFrequent & AppearanceRetained.RENDERING) != 0);
            }
        }

        //System.out.println("soleUser = "+soleUser+" renderingAttributes ="+renderingAttributes);
	// Set the appearance only for soleUser case
	if (soleUser)
	    this.app = app;
	else
	    app = null;
	
	if (renderingAttributes != null) {
	    if (renderingAttributes.changedFrequent != 0) {
		definingRenderingAttributes = renderingAttributes;
		if ((onUpdateList & ON_CHANGED_FREQUENT_UPDATE_LIST) == 0 ) {
		    renderBin.aBinUpdateList.add(this);
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
	if (soleUser || (ra.geometryAtom.source.appearance != null &&
			 ((ra.geometryAtom.source.appearance.changedFrequent & 
			   AppearanceRetained.RENDERING) != 0))) {
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

                if (numEditingShaderBins == 0) {
		    if ((onUpdateList & ON_CHANGED_FREQUENT_UPDATE_LIST) == 0) {
			renderBin.aBinUpdateList.add(this);
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
			    renderBin.aBinUpdateList.add(this);
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
	ShaderBin sb;
	TextureBin t;
	int i, size;
	
	size = addShaderBins.size();
	if (size > 0) {
	    sb = (ShaderBin)addShaderBins.get(0);
	    if (shaderBinList == null) {
		shaderBinList = sb;
	    }
	    else {
		sb.next = shaderBinList;
		shaderBinList.prev = sb;
		shaderBinList = sb;
	    }
	    	    
	    for (i = 1; i < size ; i++) {
		sb = (ShaderBin)addShaderBins.get(i);
		sb.next = shaderBinList;
		shaderBinList.prev = sb;
		shaderBinList = sb;
	    }
	}
	addShaderBins.clear();
	onUpdateList &= ~ON_OBJ_UPDATE_LIST;
    }


    /**
     * Adds the given shaderBin to this AttributeBin.
     */
    void addShaderBin(ShaderBin sb, RenderBin rb, ShaderAppearanceRetained sApp) {

	sb.attributeBin = this;

	if(sApp != null) {
	    // ShaderBin should reference to the mirror components. -- JADA.
	    // System.out.println("AttributeBin : sApp.isMirror = " + sApp.isMirror);
	    assert(sApp.isMirror);
	    sb.shaderProgram = sApp.shaderProgram;
	    sb.shaderAttributeSet = sApp.shaderAttributeSet;
	}
	sb.shaderAppearance = sApp;
	
	// TODO : JADA - sort by ShaderProgram to avoid state trashing.
	addShaderBins.add(sb);
	if ((onUpdateList & ON_OBJ_UPDATE_LIST) == 0) {
	    onUpdateList |= ON_OBJ_UPDATE_LIST;
	    rb.objUpdateList.add(this);
	}

    }


    /**
     * Removes the given shaderBin from this AttributeBin.
     */
    void removeShaderBin(ShaderBin sb) {
	
	// If the shaderBin being remove is contained in addShaderBins, 
	// then remove the shadereBin from the addList
	if (addShaderBins.contains(sb)) {
	    addShaderBins.remove(addShaderBins.indexOf(sb));
	}
	else {
	    if (sb.prev == null) { // At the head of the list
		shaderBinList = sb.next;
		if (sb.next != null) {
		    sb.next.prev = null;
		}
	    } else { // In the middle or at the end.
		sb.prev.next = sb.next;
		if (sb.next != null) {
		    sb.next.prev = sb.prev;
		}
	    }
	}

	sb.clear();

	if (shaderBinList == null && addShaderBins.size() == 0 ) {
	    // Note: Removal of this attributebin as a user of the rendering
	    // atttrs is done during removeRenderAtom() in RenderMolecule.java
	    environmentSet.removeAttributeBin(this);
	}
    }

    /**
     * Renders this AttributeBin
     */
    void render(Canvas3D cv) {

	ShaderBin sb;
	
	boolean visible = (definingRenderingAttributes == null || 
	    		       definingRenderingAttributes.visible);

	if ( (renderBin.view.viewCache.visibilityPolicy
			== View.VISIBILITY_DRAW_VISIBLE && !visible) ||
	     (renderBin.view.viewCache.visibilityPolicy
			== View.VISIBILITY_DRAW_INVISIBLE && visible)) {
	    return;
	}
	        	

        // include this AttributeBin to the to-be-updated list in Canvas
        cv.setStateToUpdate(Canvas3D.ATTRIBUTEBIN_BIT, this);

	sb = shaderBinList;
	while (sb != null) {
	    sb.render(cv);
	    sb = sb.next;
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
				    cv,
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
				        cv,
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

    void incrActiveShaderBin() {
	numEditingShaderBins++;
    }

    void decrActiveShaderBin() {
	numEditingShaderBins--;
    }

    void updateFromShaderBin(RenderAtom ra) {

	AppearanceRetained raApp = ra.geometryAtom.source.appearance;
	RenderingAttributesRetained rAttrs = 
	    (raApp == null)? null : raApp.renderingAttributes;

 	if (!soleUser && renderingAttrs != rAttrs) {
	    // no longer sole user
	    renderingAttrs = definingRenderingAttributes;
 	}
    }
}
