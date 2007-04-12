/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import javax.vecmath.*;
import java.awt.font.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * Implements Text3D class.
 */
class Text3DRetained extends GeometryRetained {
    /**
     * Packaged scope variables needed for implementation
     */
    Font3D      font3D = null;
    String      string = null;
    Point3f     position = new Point3f(0.0f, 0.0f, 0.0f);
    int         alignment = Text3D.ALIGN_FIRST, path = Text3D.PATH_RIGHT;
    float       charSpacing = 0.0f;
    int         numChars = 0;
    static final int  targetThreads = (J3dThread.UPDATE_TRANSFORM |
				       J3dThread.UPDATE_GEOMETRY |
				       J3dThread.UPDATE_RENDER);
    /**
     * The temporary transforms for this Text3D
     */
    Transform3D[] charTransforms = new Transform3D[0];
      
    /**
     * A cached list of geometry arrays for the current settings
     */
    GeometryArrayRetained[] geometryList = new GeometryArrayRetained[0];
    GlyphVector[] glyphVecs = new GlyphVector[0];

    /**
     * Bounding box data for this text string.
     */
    Point3d lower = new Point3d();
    Point3d upper = new Point3d();


    /**
     * An Array list used for messages
     */
    ArrayList newGeometryAtomList = new ArrayList();
    ArrayList oldGeometryAtomList = new ArrayList();


    /**
     * temporary model view matrix for immediate mode only
     */
    Transform3D vpcToEc;
    Transform3D drawTransform;


    Text3DRetained(){
       this.geoType = GEO_TYPE_TEXT3D;
    }


    synchronized void computeBoundingBox() {
	Point3d l = new Point3d();
	Point3d u = new Point3d();
        Vector3f location = new Vector3f(this.position);
        int i, k=0, numTotal=0;
        double width = 0, height = 0;
        Rectangle2D bounds;

        //Reset bounds data 
        l.set(location);
        u.set(location);

	if (numChars != 0) {
	    // Set loop counters based on path type
	    if (path == Text3D.PATH_RIGHT || path == Text3D.PATH_UP) {
		k = 0; 
		numTotal = numChars + 1;
	    } else if (path == Text3D.PATH_LEFT || path == Text3D.PATH_DOWN) {
		k = 1;
		numTotal = numChars;
		// Reset bounds to bounding box if first character
		bounds = glyphVecs[0].getVisualBounds();
		u.x += bounds.getWidth();
		u.y += bounds.getHeight();
	    }

	    for (i=1; i<numTotal; i++, k++) {
		width = glyphVecs[k].getLogicalBounds().getWidth();
		bounds = glyphVecs[k].getVisualBounds();
		// 'switch' could be outside loop with little hacking,
		width += charSpacing;
		height = bounds.getHeight();

		switch (this.path) {
		case Text3D.PATH_RIGHT:
		    u.x    += (width);
		    if (u.y < (height + location.y)) {
			u.y = location.y + height;
		    }
		    break;
		case Text3D.PATH_LEFT:
		    l.x    -= (width);
		    if (u.y < ( height + location.y)) {
			u.y = location.y + height;
		    }
		    break;
		case Text3D.PATH_UP:
		    u.y    += height;
		    if (u.x < (bounds.getWidth() + location.x)) {
			u.x = location.x + bounds.getWidth();
		    }
		    break;
		case Text3D.PATH_DOWN:
		    l.y    -= height;
		    if (u.x < (bounds.getWidth() + location.x)) {
			u.x = location.x + bounds.getWidth();
		    }
		    break;
		}
	    }
	    
	    // Handle string alignment. ALIGN_FIRST is handled by default 
	    if (alignment != Text3D.ALIGN_FIRST) {
		double cx = (u.x - l.x);
		double cy = (u.y - l.y);

		if (alignment == Text3D.ALIGN_CENTER) {
		    cx *= .5;
		    cy *= .5;
		}
		switch (path) {
		case Text3D.PATH_RIGHT:
		    l.x -= cx;
		    u.x -= cx;
		    break;
		case Text3D.PATH_LEFT:
		    l.x += cx;
		    u.x += cx;
		    break;
		case Text3D.PATH_UP:
		    l.y -= cy;
		    u.y -= cy;
		    break;
		case Text3D.PATH_DOWN:
		    l.y += cy;
		    u.y += cy;
		    break;

		}
	    }
	}

        l.z = 0.0f;
        if ((font3D == null) || (font3D.fontExtrusion == null)) {
	    u.z = l.z;
        } else {
	    u.z = l.z + font3D.fontExtrusion.length;
	}
    }

    void update() {}


    /**
     * Returns the Font3D objects used by this Text3D NodeComponent object.
     *
     * @return the Font3D object of this Text3D node - null if no Font3D
     *  has been associated with this node.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    final Font3D getFont3D() {
        return this.font3D;
    }

    /**
     * Sets the Font3D object used by this Text3D NodeComponent object.
     *
     * @param font3d the Font3D object to associate with this Text3D node.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    final void setFont3D(Font3D font3d) {
	geomLock.getLock();
	this.font3D = font3d;
	updateCharacterData();
	geomLock.unLock();
	sendDataChangedMessage();
    }

    /**
     * Copies the character string used in the construction of the
     * Text3D node into the supplied parameter.
     *
     * @return a copy of the String object in this Text3D node.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    final String getString() {
	return this.string;
    }

    /**
     * Copies the character string from the supplied parameter into Tex3D
     * node.
     *
     * @param string the String object to recieve the Text3D node's string.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    final void setString(String string) {
	geomLock.getLock();
	this.string = string;
	if (string == null) {
	    numChars = 0;
	} else {
	    numChars = string.length();
	}
	updateCharacterData();
	geomLock.unLock();
	sendDataChangedMessage();
    }

    /**
     * Copies the node's <code>position</code> field into the supplied
     * parameter.  The <code>position</code> is used to determine the
     * initial placement of the Text3D string.  The position, combined with
     * the path and alignment control how the text is displayed.
     *
     * @param position the point to position the text.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @see #getAlignment
     * @see #getPath
     */
    final void getPosition(Point3f position) {
        position.set(this.position);
    }

    /**
     * Sets the node's <code>position</code> field to the supplied
     * parameter.  The <code>position</code> is used to determine the
     * initial placement of the Text3D string.  The position, combined with
     * the path and alignment control how the text is displayed.
     *
     * @param position the point to position the text.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @see #getAlignment
     * @see #getPath
     */
    final void setPosition(Point3f position) {
	geomLock.getLock();
        this.position.set(position);
        updateTransformData();
	geomLock.unLock();
        sendTransformChangedMessage();
    }

    /**
     * Retrieves the text alignment policy for this Text3D NodeComponent
     * object. The <code>alignment</code> is used to specify how
     * glyphs in the string are placed in relation to the
     * <code>position</code> field.  Valid values for this field
     * are:
     * <UL>
     * <LI> ALIGN_CENTER - the center of the string is placed on the
     *  <code>position</code> point.
     * <LI> ALIGN_FIRST - the first character of the string is placed on
     *   the <code>position</code> point.
     * <LI> ALIGN_LAST - the last character of the string is placed on the
     *   <code>position</code> point.
     * </UL>
     * The default value of this field is <code>ALIGN_FIRST</code>.
     *
     * @return the current alingment policy for this node.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @see #getPosition
     */
    final int getAlignment() {
        return alignment;
    }

    /**
     * Sets the text alignment policy for this Text3D NodeComponent
     * object. The <code>alignment</code> is used to specify how
     * glyphs in the string are placed in relation to the
     * <code>position</code> field.  Valid values for this field
     * are:
     * <UL>
     * <LI> ALIGN_CENTER - the center of the string is placed on the
     *  <code>position</code> point.
     * <LI> ALIGN_FIRST - the first character of the string is placed on
     *   the <code>position</code> point.
     * <LI> ALIGN_LAST - the last character of the string is placed on the
     *   <code>position</code> point.
     * </UL>
     * The default value of this field is <code>ALIGN_FIRST</code>.
     *
     * @return the current alingment policy for this node.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * 
     * @see #getPosition
     */
    final void setAlignment(int alignment) {
	geomLock.getLock();
        this.alignment = alignment;
        updateTransformData();
	geomLock.unLock();
        sendTransformChangedMessage();
    }

    /**
     * Retrieves the node's <code>path</code> field.  This field
     * is used  to specify how succeeding
     * glyphs in the string are placed in relation to the previous glyph.
     * Valid values for this field are:
     * <UL>
     * <LI> PATH_LEFT: - succeeding glyphs are placed to the left of the
     *  current glyph.
     * <LI> PATH_RIGHT: - succeeding glyphs are placed to the right of the
     *  current glyph.
     * <LI> PATH_UP: - succeeding glyphs are placed above the current glyph.
     * <LI> PATH_DOWN: - succeeding glyphs are placed below the current glyph.
     * </UL>
     * The default value of this field is <code>PATH_RIGHT</code>.
     *
     * @return the current alingment policy for this node.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    final int getPath() {
        return this.path;
    }

    /**
     * Sets the node's <code>path</code> field.  This field
     * is used  to specify how succeeding
     * glyphs in the string are placed in relation to the previous glyph.
     * Valid values for this field are:
     * <UL>
     * <LI> PATH_LEFT - succeeding glyphs are placed to the left of the
     *  current glyph.
     * <LI> PATH_RIGHT - succeeding glyphs are placed to the right of the
     *  current glyph.
     * <LI> PATH_UP - succeeding glyphs are placed above the current glyph.
     * <LI> PATH_DOWN - succeeding glyphs are placed below the current glyph.
     * </UL>
     * The default value of this field is <code>PATH_RIGHT</code>.
     *
     * @param path the value to set the path to.
     *
     * @return the current alingment policy for this node.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    final void setPath(int path) {
        this.path = path;
        updateTransformData();
        sendTransformChangedMessage();
    }

    /**
     * Retrieves the 3D bounding box that encloses this Text3D object.
     *
     * @param bounds the object to copy the bounding information to.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @see BoundingBox
     */
    final void getBoundingBox(BoundingBox bounds) {
	synchronized (this) {
            bounds.setLower(lower);
            bounds.setUpper(upper);
	}
    }

    /**
     * Retrieves the character spacing used to construct the Text3D string.
     * This spacing is in addition to the regular spacing between glyphs as
     * defined in the Font object.  1.0 in this space is measured as the
     * width of the largest glyph in the 2D Font.  The default value is
     * 0.0.
     *
     * @return the current character spacing value
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    final float getCharacterSpacing() {
	return charSpacing;
    }

    /**
     * Sets the character spacing used hwne constructing the Text3D string.
     * This spacing is in addition to the regular spacing between glyphs as
     * defined in the Font object.  1.0 in this space is measured as the
     * width of the largest glyph in the 2D Font.  The default value is
     * 0.0.
     *
     * @param characterSpacing the new character spacing value
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    final void setCharacterSpacing(float characterSpacing) {
	geomLock.getLock();
	this.charSpacing = characterSpacing;
        updateTransformData();
	geomLock.unLock();
        sendTransformChangedMessage();
    }


    final void sendDataChangedMessage() {
	J3dMessage[] m;
	int i, j, k, kk, numMessages;
	int gSize;
	ArrayList shapeList, gaList;
	Shape3DRetained s;
	GeometryAtom[] newGeometryAtoms;
	ArrayList tiArrList = new ArrayList();
	ArrayList newCtArrArrList = new ArrayList();

	synchronized(liveStateLock) {
	    if (source.isLive()) {
		synchronized (universeList) {
		    numMessages = universeList.size();
		    m = new J3dMessage[numMessages];
		    for (i=0; i<numMessages; i++) {
			m[i] = new J3dMessage();
			m[i].type = J3dMessage.TEXT3D_DATA_CHANGED;
			m[i].threads = targetThreads;
			shapeList = (ArrayList)userLists.get(i);
			newGeometryAtomList.clear();
			oldGeometryAtomList.clear();

			for (j=0; j<shapeList.size(); j++) {
			    s = (Shape3DRetained)shapeList.get(j);
			    if (s.boundsAutoCompute) {
				// update combine bounds of mirrorShape3Ds. So we need to
				// use its bounds and not localBounds.
				// bounds is actually a reference to
				// mirrorShape3D.source.localBounds.
				// XXXX : Should only need to update distinct localBounds.
				s.getCombineBounds((BoundingBox)s.bounds);
			    }

			    gSize = s.geometryList.size();
			    
			    GeometryAtom oldGA = Shape3DRetained.getGeomAtom(s);
			    GeometryAtom newGA = new GeometryAtom();
			    
			    int geometryCnt = 0;
			    for(k = 0; k<gSize; k++) {
				GeometryRetained geomRetained =
				    (GeometryRetained) s.geometryList.get(k);
				if(geomRetained != null) {
				    Text3DRetained tempT3d = (Text3DRetained)geomRetained;
				    geometryCnt += tempT3d.numChars;
				}
				else {
				    // Slightly wasteful, but not quite worth to optimize yet. 
				    geometryCnt++;
				}
			    }
			    
			    newGA.geometryArray = new GeometryRetained[geometryCnt];
			    newGA.lastLocalTransformArray = new Transform3D[geometryCnt];
			    // Reset geometryCnt;
			    geometryCnt = 0;
			    
			    newGA.locale = s.locale; 
			    newGA.visible = s.visible;
			    newGA.source = s;
			    int gaCnt=0;
			    GeometryRetained geometry = null;
			    for(; gaCnt<gSize; gaCnt++) {
				geometry = (GeometryRetained) s.geometryList.get(gaCnt);
				if(geometry != null) {
				    newGA.geoType = geometry.geoType;
				    newGA.alphaEditable = s.isAlphaEditable(geometry);
				    break;
				}
			    }
			    
			    for(; gaCnt<gSize; gaCnt++) {
				geometry = (GeometryRetained) s.geometryList.get(gaCnt);
				if(geometry == null) {
				    newGA.geometryArray[gaCnt] = null;
				}
				else {
				    Text3DRetained t = (Text3DRetained)geometry;
				    GeometryRetained geo;
				    for (k=0; k<t.numChars; k++, geometryCnt++) {
					geo = t.geometryList[k];
					if (geo != null) {
					    newGA.geometryArray[geometryCnt] = geo;
					    newGA.lastLocalTransformArray[geometryCnt] =
						t.charTransforms[k];
					    
					} else {
					    newGA.geometryArray[geometryCnt] = null;
					    newGA.lastLocalTransformArray[geometryCnt] = null;
					}
					
				    }
				    
				}
			    }
			    
			    oldGeometryAtomList.add(oldGA);
			    newGeometryAtomList.add(newGA);
			    Shape3DRetained.setGeomAtom(s, newGA);
			}
			
			Object[] oldGAArray = oldGeometryAtomList.toArray();
			Object[] newGAArray = newGeometryAtomList.toArray();
			ArrayList uniqueList = getUniqueSource(shapeList);
			int numSrc = uniqueList.size();
			int numMS3D;
			Shape3DRetained ms, src;

            		for (j=0; j<numSrc; j++) {
            		    CachedTargets[] newCtArr = null;
			    src = (Shape3DRetained)uniqueList.get(j);
			    numMS3D = src.mirrorShape3D.size();
			    
        		    TargetsInterface ti = ((GroupRetained)src.
				parent).getClosestTargetsInterface(
                                        TargetsInterface.TRANSFORM_TARGETS);
			    
        		    if (ti != null) {
            		        CachedTargets ct;
            		        newCtArr = new CachedTargets[numMS3D];

				for (k=0; k<numMS3D; k++) {
				    ms = (Shape3DRetained)src.mirrorShape3D.get(k);

				    GeometryAtom ga = 
					Shape3DRetained.getGeomAtom(ms);
				    for(kk=0; kk<newGAArray.length; kk++) {
					if(ga == newGAArray[kk]) {
					    break;
					}
				    }

				    if(kk==newGAArray.length) {
					System.err.println("Text3DRetained : Problem !!! Can't find matching geomAtom"); 
				    }

				    ct = ti.getCachedTargets(TargetsInterface.
							     TRANSFORM_TARGETS, k, -1);
				    if (ct != null) {
					newCtArr[k] = new CachedTargets();
					newCtArr[k].copy(ct);
					newCtArr[k].replace((NnuId)oldGAArray[kk], 
							    (NnuId)newGAArray[kk], 
							    Targets.GEO_TARGETS);
				    } else {
					newCtArr[k] = null;
				    }

				}

            			ti.resetCachedTargets(
				  TargetsInterface.TRANSFORM_TARGETS, newCtArr, -1);

				tiArrList.add(ti);
				newCtArrArrList.add(newCtArr);

			    }
			    
			}
			
			m[i].args[0] = oldGAArray;
			m[i].args[1] = newGAArray;
			m[i].universe = (VirtualUniverse)universeList.get(i);

			if(tiArrList.size() > 0) {
			    m[i].args[2] = tiArrList.toArray();
			    m[i].args[3] = newCtArrArrList.toArray();
			}
			
			tiArrList.clear();
			newCtArrArrList.clear();

		    }
		    VirtualUniverse.mc.processMessage(m);
		}
		
	    }
	}
    }
	

    final void sendTransformChangedMessage() {
	J3dMessage[] m;
	int i, j, numMessages, sCnt;
	ArrayList shapeList;
	ArrayList gaList = new ArrayList();
	Shape3DRetained s;
	GeometryRetained geomR;
	synchronized(liveStateLock) {
	    if (source.isLive()) {
		synchronized (universeList) {
		    numMessages = universeList.size();
		    m = new J3dMessage[numMessages];
		    for (i=0; i<numMessages; i++) {
			m[i] = new J3dMessage();
			m[i].type = J3dMessage.TEXT3D_TRANSFORM_CHANGED;
			m[i].threads = targetThreads;
			shapeList = (ArrayList)userLists.get(i);
			// gaList = new GeometryAtom[shapeList.size() * numChars];
			for (j=0; j<shapeList.size(); j++) {
			    s = (Shape3DRetained)shapeList.get(j);

			    // Find the right geometry.
			    for(sCnt=0; sCnt<s.geometryList.size(); sCnt++) {
				geomR = (GeometryRetained) s.geometryList.get(sCnt);
				if(geomR == this) {
				    break;
				}
			    }
			
			    if(sCnt < s.geometryList.size()) 
				gaList.add(Shape3DRetained.getGeomAtom(s));

			}
			m[i].args[0] = gaList.toArray();
			m[i].args[1] = charTransforms;
			m[i].universe = (VirtualUniverse)universeList.get(i);
		    }
		    VirtualUniverse.mc.processMessage(m);
		}
	    }
	}
    }

    /**
     * Update internal reprsentation of tranform matrices and geometry.
     * This method will be called whenever string or font3D change.
     */
    final void updateCharacterData() {
	char c[] = new char[1];

	if (geometryList.length != numChars) {
	    geometryList = new GeometryArrayRetained[numChars];
	    glyphVecs = new GlyphVector[numChars];
	}

	if (font3D != null) {
            for (int i=0; i<numChars; i++) {
		c[0] = string.charAt(i);
		glyphVecs[i] = font3D.font.createGlyphVector(font3D.frc, c);
	        geometryList[i] = font3D.triangulateGlyphs(glyphVecs[i], c[0]);
            }
	}
	
        updateTransformData();
    }

    /**
     * Update per character transform based on Text3D location,
     * per character size and path. 
     *
     * WARNING: Caller of this method must make sure SceneGraph is live,
     * else exceptions may be thrown.
     */
    final void updateTransformData(){
        int i, k=0, numTotal=0;
        double width = 0, height = 0;
        Vector3f location = new Vector3f(this.position);
        Rectangle2D bounds;

        //Reset bounds data 
        lower.set(location);
        upper.set(location);

	charTransforms = new Transform3D[numChars];
	for (i=0; i<numChars; i++) {
	    charTransforms[i] = new Transform3D();
	}

	if (numChars != 0) {
	    charTransforms[0].set(location);

	    // Set loop counters based on path type
	    if (path == Text3D.PATH_RIGHT || path == Text3D.PATH_UP) {
		k = 0; 
		numTotal = numChars + 1;
	    } else if (path == Text3D.PATH_LEFT || path == Text3D.PATH_DOWN) {
		k = 1;
		numTotal = numChars;
		// Reset bounds to bounding box if first character
		bounds = glyphVecs[0].getVisualBounds();
		upper.x += bounds.getWidth();
		upper.y += bounds.getHeight();
	    }

	    for (i=1; i<numTotal; i++, k++) {
		width = glyphVecs[k].getLogicalBounds().getWidth();
		bounds = glyphVecs[k].getVisualBounds();
		// 'switch' could be outside loop with little hacking,
		width += charSpacing;
		height = bounds.getHeight();

		switch (this.path) {
		case Text3D.PATH_RIGHT:
		    location.x += width;
		    upper.x    += (width);
		    if (upper.y < (height + location.y)) {
			upper.y = location.y + height;
		    }
		    break;
		case Text3D.PATH_LEFT:
		    location.x -= width; 
		    lower.x    -= (width);
		    if (upper.y < ( height + location.y)) {
			upper.y = location.y + height;
		    }
		    break;
		case Text3D.PATH_UP:
		    location.y += height; 
		    upper.y    += height;
		    if (upper.x < (bounds.getWidth() + location.x)) {
			upper.x = location.x + bounds.getWidth();
		    }
		    break;
		case Text3D.PATH_DOWN:
		    location.y -= height; 
		    lower.y    -= height;
		    if (upper.x < (bounds.getWidth() + location.x)) {
			upper.x = location.x + bounds.getWidth();
		    }
		    break;
		}
		if (i < numChars) {
		    charTransforms[i].set(location);
		}
	    }
	    
	    // Handle string alignment. ALIGN_FIRST is handled by default 
	    if (alignment != Text3D.ALIGN_FIRST) {
		double cx = (upper.x - lower.x);
		double cy = (upper.y - lower.y);

		if (alignment == Text3D.ALIGN_CENTER) {
		    cx *= .5;
		    cy *= .5;
		}
		switch (path) {
		case Text3D.PATH_RIGHT:
		    for (i=0;i < numChars;i++) {
			charTransforms[i].mat[3] -= cx;
		    }
		    lower.x -= cx;
		    upper.x -= cx;
		    break;
		case Text3D.PATH_LEFT:
		    for (i=0;i < numChars;i++) {
			charTransforms[i].mat[3] += cx;
		    }
		    lower.x += cx;
		    upper.x += cx;
		    break;

		case Text3D.PATH_UP:
		    for (i=0;i < numChars;i++) {
			charTransforms[i].mat[7] -=cy;
		    }
		    lower.y -= cy;
		    upper.y -= cy;
		    break;
		case Text3D.PATH_DOWN:
		    for (i=0;i < numChars;i++) {
			charTransforms[i].mat[7] +=cy;
		    }
		    lower.y += cy;
		    upper.y += cy;
		    break;

		}
	    }
	}

        lower.z = 0.0f;
        if ((font3D == null) || (font3D.fontExtrusion == null)) {
	    upper.z = lower.z;
        } else {
	    upper.z = lower.z + font3D.fontExtrusion.length;
	}

        // update geoBounds
        getBoundingBox(geoBounds);
    }


    /**
     * This method is called when the SceneGraph becomes live. All characters
     * used by this.string are tesselated in this method, to avoid wait during
     * traversal and rendering.
     */
    void setLive(boolean inBackgroundGroup, int refCount) {
      // Tesselate all character data and update character transforms
      updateCharacterData();
      super.doSetLive(inBackgroundGroup, refCount);
      super.markAsLive();
    }

    // TODO -- Need to rethink. Might have to consider charTransform[] in returns pickInfo.
    boolean intersect(PickShape pickShape, PickInfo pickInfo, int flags, Point3d iPnt,
                      GeometryRetained geom, int geomIndex) {
	Transform3D tempT3D = new Transform3D();
	GeometryArrayRetained geo = null;
	int sIndex = -1;
	PickShape newPS;
	double minDist = Double.MAX_VALUE;
        double distance =0.0;
        Point3d closestIPnt = new Point3d();
        
	for (int i=0; i < numChars; i++) {
	    geo= geometryList[i];
	    if (geo != null) {
		tempT3D.invert(charTransforms[i]);
		newPS = pickShape.transform(tempT3D);
		if (geo.intersect(newPS, pickInfo, flags, iPnt, geom,  geomIndex)) {
		    if (flags == 0) {
			return true;
		    }
                    distance = newPS.distance(iPnt);
		    if (distance < minDist) {
			sIndex = i;
			minDist = distance;
                        closestIPnt.set(iPnt);
		    }    
		}
	    }
	}
	
	if (sIndex >= 0) {
	    // We need to transform iPnt to the vworld to compute the actual distance.
	    // In this method we'll transform iPnt by its char. offset. Shape3D will
	    // do the localToVworld transform.
	    iPnt.set(closestIPnt);
	    charTransforms[sIndex].transform(iPnt);
	    return true;
	} 
	return false;    
    }

    boolean intersect(Point3d[] pnts) {
	Transform3D tempT3D = new Transform3D();
	GeometryArrayRetained ga;
	boolean isIntersect = false;
	Point3d transPnts[] = new Point3d[pnts.length];
	for (int j=pnts.length-1; j >= 0; j--) {
	    transPnts[j] = new Point3d();
	}

	for (int i=numChars-1; i >= 0;  i--) {
	    ga = geometryList[i];
	    if ( ga != null) {
		tempT3D.invert(charTransforms[i]);
		for (int j=pnts.length-1; j >= 0; j--) {
		    tempT3D.transform(pnts[j], transPnts[j]);
		}
		if (ga.intersect(transPnts)) {
		    isIntersect = true;
		    break;
		}
	    }
	}
	return isIntersect;
    }


    boolean intersect(Transform3D thisToOtherVworld, GeometryRetained geom) {
	GeometryArrayRetained ga;

	for (int i=numChars-1; i >=0; i--) {
	    ga = geometryList[i];
	    if ((ga != null) && ga.intersect(thisToOtherVworld, geom)) {
		return true;
	    }
	}
	
	return false;
    }

    boolean intersect(Bounds targetBound) {
	GeometryArrayRetained ga;

	for (int i=numChars-1; i >=0; i--) {
	    ga = geometryList[i];
	    if ((ga != null) && ga.intersect(targetBound)) {
		return true;
	    }
	}
	
	return false;
	
    }

    void setModelViewMatrix(Transform3D vpcToEc, Transform3D drawTransform) {
	this.vpcToEc = vpcToEc;
	this.drawTransform = drawTransform;
    }


    void execute(Canvas3D cv, RenderAtom ra, boolean isNonUniformScale, 
		 boolean updateAlpha, float alpha,
		 int screen, 
		 boolean ignoreVertexColors) { 

	Transform3D trans = new Transform3D();

	for (int i = 0; i < geometryList.length; i++) {
	    trans.set(drawTransform);
	    trans.mul(charTransforms[i]);
	    cv.setModelViewMatrix(cv.ctx, vpcToEc.mat, trans);
	    geometryList[i].execute(cv, ra, isNonUniformScale, updateAlpha, alpha,
				    screen, ignoreVertexColors);
	}
    }

    int getClassType() {
	return TEXT3D_TYPE;
    }


    ArrayList getUniqueSource(ArrayList shapeList) {
	ArrayList uniqueList = new ArrayList();
	int size = shapeList.size();
	Object src;
	int i, index;

	for (i=0; i<size; i++) {
	    src = ((Shape3DRetained)shapeList.get(i)).sourceNode;
            index = uniqueList.indexOf(src);
            if (index == -1) {
                uniqueList.add(src);
            }
        }
	return uniqueList;
    }
}

