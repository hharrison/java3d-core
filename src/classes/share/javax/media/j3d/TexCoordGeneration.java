/*
 * $RCSfile$
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import javax.vecmath.Vector4f;

/**
 * The TexCoordGeneration object contains all parameters needed for 
 * automatic texture coordinate generation.  It is included as part 
 * of an Appearance component object.
 * <p>
 * Texture coordinates determine which texel in the texture map is
 * assigned to a given vertex. Texture coordinates are interpolated
 * between vertices, similarly to how colors are interpolated between
 * two vertices of lines and polygons.
 * <p>
 * Texture coordinates consist of two, three or four coordinates.
 * These coordinates
 * are referred to as the <i>S</i>, <i>T</i>, <i>R</i>, and <i>Q</i>
 * coordinates.
 * 2D textures use the <i>S</i> and <i>T</i> coordinates. 3D textures
 * use the <i>S</i>, <i>T</i> and <i>R</i> coordinates. The <i>Q</i>
 * coordinate, similar to the <i>w</i> coordinate of the <i>(x, y, z, w)</i>
 * object coordinates, is used to create homogeneous coordinates.
 * <p>
 * Rather than the programmer having to explicitly assign texture 
 * coordinates, Java 3D can automatically generate the texture
 * coordinates to achieve texture mapping onto contours.
 * The TexCoordGeneration attributes specify the functions for automatically
 * generating texture coordinates. The texture attributes that can be 
 * defined are:
 * <p><ul>
 * <li>Texture format - defines whether the generated texture 
 * coordinates are 2D, 3D, or 4D:<p>
 * <ul>
 * <li>TEXTURE_COORDINATE_2 - generates 2D texture coordinates 
 * (S and T).<p>
 * <li>TEXTURE_COORDINATE_3 - generates 3D texture coordinates
 * (S, T, and R).<p>
 * <li>TEXTURE_COORDINATE_4 - generates 4D texture coordinates
 * (S, T, R, and Q).<p>
 * </ul>
 * <li>Texture generation mode - defines how the texture coordinates
 * are generated:<p>
 * <ul>
 * <li>OBJECT_LINEAR - texture coordinates are generated as a linear
 * function in object coordinates. The function used is:<p>
 * <ul>
 * <code>g = p<sub>1</sub>x<sub>o</sub> + p<sub>2</sub>y<sub>o</sub> + p<sub>3</sub>z<sub>o</sub> + p<sub>4</sub>w<sub>o</sub></code>
 * <p>
 * where<br>
 * <ul><code>g</code> is the value computed for the coordinate.<br>
 * <code>p<sub>1</sub></code>, <code>p<sub>2</sub></code>,
 * <code>p<sub>3</sub></code>, and <code>p<sub>4</sub></code>
 * are the plane equation coefficients (described below).<br>
 * x<sub>o</sub>, y<sub>o</sub>, z<sub>o</sub>, and w<sub>o</sub> are
 * the object coordinates of the vertex.<p>
 * </ul></ul>
 * <li>EYE_LINEAR - texture coordinates are generated as a linear
 * function in eye coordinates. The function used is:<p>
 * <ul>
 * <code>g = p<sub>1</sub>'x<sub>e</sub> + p<sub>2</sub>'y<sub>e</sub> + p<sub>3</sub>'z<sub>e</sub> + p<sub>4</sub>'w<sub>e</sub></code>
 * <p>
 * where<br>
 * <ul><code>x<sub>e</sub></code>, <code>y<sub>e</sub></code>,
 * <code>z<sub>e</sub></code>, and w<sub>e</sub></code> are the eye
 * coordinates of the vertex.<br>
 * <code>p<sub>1</sub>'</code>, <code>p<sub>2</sub>'</code>,
 * <code>p<sub>3</sub>'</code>, and <code>p<sub>4</sub>'</code>
 * are the plane equation coefficients transformed into eye
 * coordinates.<p>
 * </ul></ul>
 * 
 * <li>SPHERE_MAP - texture coordinates are generated using 
 * spherical reflection mapping in eye coordinates. Used to simulate
 * the reflected image of a spherical environment onto a polygon.<p>
 *
 * <li>NORMAL_MAP - texture coordinates are generated to match 
 * vertices' normals in eye coordinates. This is only available if
 * TextureCubeMap is available.
 * </li><p>
 *
 * <li>REFLECTION_MAP - texture coordinates are generated to match
 * vertices' reflection vectors in eye coordinates. This is only available
 * if TextureCubeMap is available.
 * </li><p>
 * </ul>
 * <li>Plane equation coefficients - defines the coefficients for the 
 * plane equations used to generate the coordinates in the 
 * OBJECT_LINEAR and EYE_LINEAR texture generation modes.
 * The coefficients define a reference plane in either object coordinates
 * or in eye coordinates, depending on the texture generation mode.
 * <p>
 * The equation coefficients are set by the <code>setPlaneS</code>,
 * <code>setPlaneT</code>, <code>setPlaneR</code>, and <code>setPlaneQ</code>
 * methods for each of the S, T, R, and Q coordinate functions, respectively.
 * By default the equation coefficients are set as follows:<p>
 * <ul>
 * plane S = (1.0, 0.0, 0.0, 0.0)<br>
 * plane T = (0.0, 1.0, 0.0, 0.0)<br>
 * plane R = (0.0, 0.0, 0.0, 0.0)<br>
 * plane Q = (0.0, 0.0, 0.0, 0.0)<p>
 * </ul></ul>
 * Texture coordinate generation is enabled or disabled by the
 * <code>setEnable</code> method. When enabled, the specified
 * texture coordinate is computed according to the generating function
 * associated with the coordinate. When disabled, subsequent vertices
 * take the specified texture coordinate from the current set of
 * texture coordinates.<p>
 *
 * @see Canvas3D#queryProperties
 */
public class TexCoordGeneration extends NodeComponent {    

    /**
     * Specifies that this TexCoordGeneration object allows reading its
     * enable flag.
     */
    public static final int
    ALLOW_ENABLE_READ = CapabilityBits.TEX_COORD_GENERATION_ALLOW_ENABLE_READ;

    /**
     * Specifies that this TexCoordGeneration object allows writing its
     * enable flag.
     */
    public static final int
    ALLOW_ENABLE_WRITE = CapabilityBits.TEX_COORD_GENERATION_ALLOW_ENABLE_WRITE;

    /**
     * Specifies that this TexCoordGeneration object allows reading its
     * format information.
     */
    public static final int
    ALLOW_FORMAT_READ = CapabilityBits.TEX_COORD_GENERATION_ALLOW_FORMAT_READ;

    /**
     * Specifies that this TexCoordGeneration object allows reading its
     * mode information.
     */
    public static final int
    ALLOW_MODE_READ = CapabilityBits.TEX_COORD_GENERATION_ALLOW_MODE_READ;

    /**
     * Specifies that this TexCoordGeneration object allows reading its
     * planeS, planeR, and planeT component information.
     */
    public static final int
    ALLOW_PLANE_READ = CapabilityBits.TEX_COORD_GENERATION_ALLOW_PLANE_READ;

    /**
     * Specifies that this TexCoordGeneration object allows writing its
     * planeS, planeR, and planeT component information.
     *
     * @since Java 3D 1.3
     */
    public static final int ALLOW_PLANE_WRITE =
    CapabilityBits.TEX_COORD_GENERATION_ALLOW_PLANE_WRITE;

    /**
     * Generates texture coordinates as a linear function in
     * object coordinates.
     *
     * @see #setGenMode
     */
    public static final int OBJECT_LINEAR = 0;
    /**
     * Generates texture coordinates as a linear function in
     * eye coordinates.
     *
     * @see #setGenMode
     */
    public static final int EYE_LINEAR    = 1;
    /**
     * Generates texture coordinates using a spherical reflection
     * mapping in eye coordinates.
     *
     * @see #setGenMode
     */
    public static final int SPHERE_MAP    = 2;
    /**
     * Generates texture coordinates that match vertices' normals in
     * eye coordinates.
     *
     * @see #setGenMode
     * @see Canvas3D#queryProperties
     *
     * @since Java 3D 1.3
     */
    public static final int NORMAL_MAP    = 3;
    /**
     * Generates texture coordinates that match vertices' reflection
     * vectors in eye coordinates.
     *
     * @see #setGenMode
     * @see Canvas3D#queryProperties
     *
     * @since Java 3D 1.3
     */
    public static final int REFLECTION_MAP = 4;

    // Definitions for format
    /**
     * Generates 2D texture coordinates (S and T).
     *
     * @see #setFormat
     */
    public static final int TEXTURE_COORDINATE_2 = 0;
    /**
     * Generates 3D texture coordinates (S, T, and R).
     *
     * @see #setFormat
     */
    public static final int TEXTURE_COORDINATE_3 = 1;
    /**
     * Generates 4D texture coordinates (S, T, R, and Q).
     *
     * @see #setFormat
     *
     * @since Java 3D 1.3
     */
    public static final int TEXTURE_COORDINATE_4 = 2;

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_ENABLE_READ,
        ALLOW_FORMAT_READ,
        ALLOW_MODE_READ,
        ALLOW_PLANE_READ        
    };
    
    /**
     * Constructs a TexCoordGeneration object with default parameters.
     * The default values are as follows:
     * <ul>
     * enable flag : true<br>
     * texture generation mode : OBJECT_LINEAR<br>
     * format : TEXTURE_COORDINATE_2<br>
     * plane S : (1,0,0,0)<br>
     * plane T : (0,1,0,0)<br>
     * plane R : (0,0,0,0)<br>
     * plane Q : (0,0,0,0)<br>
     * </ul>
     */
    public TexCoordGeneration() {
	// Just use the defaults
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Constructs a TexCoordGeneration object with the specified genMode and
     * format.
     * Defaults will be used for the rest of the state variables.
     * @param genMode texture generation mode, one of: OBJECT_LINEAR,
     * EYE_LINEAR, SPHERE_MAP, NORMAL_MAP, or REFLECTION_MAP
     * @param format texture format, one of: TEXTURE_COORDINATE_2,
     * TEXTURE_COORDINATE_3, or TEXTURE_COORDINATE_4
     *
     * @see Canvas3D#queryProperties
     */
    public TexCoordGeneration(int genMode, int format) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((TexCoordGenerationRetained)this.retained).initGenMode(genMode);
	((TexCoordGenerationRetained)this.retained).initFormat(format);
    }

    /**
     * Constructs a TexCoordGeneration object with the specified genMode,
     * format, and the S coordinate plane equation.
     * Defaults will be used for the rest of the state variables.
     * @param genMode texture generation mode, one of: OBJECT_LINEAR,
     * EYE_LINEAR, SPHERE_MAP, NORMAL_MAP, or REFLECTION_MAP
     * @param format texture format, one of: TEXTURE_COORDINATE_2,
     * TEXTURE_COORDINATE_3 or TEXTURE_COORDINATE_4
     * @param planeS plane equation for the S coordinate
     *
     * @see Canvas3D#queryProperties
     */
    public TexCoordGeneration(int genMode, int format, Vector4f planeS) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((TexCoordGenerationRetained)this.retained).initGenMode(genMode);
	((TexCoordGenerationRetained)this.retained).initFormat(format);
	((TexCoordGenerationRetained)this.retained).initPlaneS(planeS);
    }

    /**
     * Constructs a TexCoordGeneration object with the specified genMode,
     * format, and the S and T coordinate plane equations.
     * Defaults will be used for the rest of the state variables.
     * @param genMode texture generation mode, one of: OBJECT_LINEAR,
     * EYE_LINEAR, SPHERE_MAP, NORMAL_MAP, or REFLECTION_MAP
     * @param format texture format, one of: TEXTURE_COORDINATE_2,
     * TEXTURE_COORDINATE_3 or TEXTURE_COORDINATE_4
     * @param planeS plane equation for the S coordinate
     * @param planeT plane equation for the T coordinate
     *
     * @see Canvas3D#queryProperties
     */
    public TexCoordGeneration(int genMode, int format, Vector4f planeS, 
			      Vector4f planeT) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((TexCoordGenerationRetained)this.retained).initGenMode(genMode);
	((TexCoordGenerationRetained)this.retained).initFormat(format);
	((TexCoordGenerationRetained)this.retained).initPlaneS(planeS);
	((TexCoordGenerationRetained)this.retained).initPlaneT(planeT);
    }

    /**
     * Constructs a TexCoordGeneration object with the specified genMode,
     * format, and the S, T, and R coordinate plane equations.
     * @param genMode texture generation mode, one of: OBJECT_LINEAR,
     * EYE_LINEAR, SPHERE_MAP, NORMAL_MAP, or REFLECTION_MAP
     * @param format texture format, one of: TEXTURE_COORDINATE_2,
     * TEXTURE_COORDINATE_3 or TEXTURE_COORDINATE_4
     * @param planeS plane equation for the S coordinate
     * @param planeT plane equation for the T coordinate
     * @param planeR plane equation for the R coordinate
     *
     * @see Canvas3D#queryProperties
     */
    public TexCoordGeneration(int genMode, int format, Vector4f planeS, 
			      Vector4f planeT, Vector4f planeR) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((TexCoordGenerationRetained)this.retained).initGenMode(genMode);
	((TexCoordGenerationRetained)this.retained).initFormat(format);
	((TexCoordGenerationRetained)this.retained).initPlaneS(planeS);
	((TexCoordGenerationRetained)this.retained).initPlaneT(planeT);
	((TexCoordGenerationRetained)this.retained).initPlaneR(planeR);
    }

    /**
     * Constructs a TexCoordGeneration object with the specified genMode,
     * format, and the S, T, R, and Q coordinate plane equations.
     * @param genMode texture generation mode, one of: OBJECT_LINEAR,
     * EYE_LINEAR, SPHERE_MAP, NORMAL_MAP, or REFLECTION_MAP
     * @param format texture format, one of: TEXTURE_COORDINATE_2,
     * TEXTURE_COORDINATE_3 or TEXTURE_COORDINATE_4
     * @param planeS plane equation for the S coordinate
     * @param planeT plane equation for the T coordinate
     * @param planeR plane equation for the R coordinate
     * @param planeQ plane equation for the Q coordinate
     *
     * @see Canvas3D#queryProperties
     *
     * @since Java 3D 1.3
     */
    public TexCoordGeneration(int genMode, int format, Vector4f planeS, 
			      Vector4f planeT, Vector4f planeR,
			      Vector4f planeQ) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((TexCoordGenerationRetained)this.retained).initGenMode(genMode);
	((TexCoordGenerationRetained)this.retained).initFormat(format);
	((TexCoordGenerationRetained)this.retained).initPlaneS(planeS);
	((TexCoordGenerationRetained)this.retained).initPlaneT(planeT);
	((TexCoordGenerationRetained)this.retained).initPlaneR(planeR);
	((TexCoordGenerationRetained)this.retained).initPlaneQ(planeQ);
    }

    /**
     * Enables or disables texture coordinate generation for this
     * appearance component object.
     * @param state true or false to enable or disable texture coordinate
     * generation
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setEnable(boolean state) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_ENABLE_WRITE))
              throw new CapabilityNotSetException(J3dI18N.getString("TexCoordGeneration0"));
	if (isLive())
	    ((TexCoordGenerationRetained)this.retained).setEnable(state);
	else
	    ((TexCoordGenerationRetained)this.retained).initEnable(state);
    }

    /**
     * Retrieves the state of the texCoordGeneration enable flag.
     * @return true if texture coordinate generation is enabled,
     * false if texture coordinate generation is disabled
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public boolean getEnable() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_ENABLE_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("TexCoordGeneration1"));
	return ((TexCoordGenerationRetained)this.retained).getEnable();
    }
    /**
     * Sets the TexCoordGeneration format to the specified value.
     * @param format texture format, one of: TEXTURE_COORDINATE_2,
     * TEXTURE_COORDINATE_3 or TEXTURE_COORDINATE_4
     * @exception RestrictedAccessException if the method is called
     * when this object is part of live or compiled scene graph.
     */
    public void setFormat(int format) {
        checkForLiveOrCompiled();
	((TexCoordGenerationRetained)this.retained).initFormat(format);

    }

    /**
     * Retrieves the current TexCoordGeneration format.
     * @return the texture format
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public int getFormat() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_FORMAT_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("TexCoordGeneration2"));
	return ((TexCoordGenerationRetained)this.retained).getFormat();
    }

    /**
     * Sets the TexCoordGeneration generation mode to the specified value.
     * @param genMode texture generation mode, one of: OBJECT_LINEAR,
     * EYE_LINEAR, SPHERE_MAP, NORMAL_MAP, or REFLECTION_MAP.
     * @exception RestrictedAccessException if the method is called
     * when this object is part of live or compiled scene graph.
     *
     * @exception IllegalArgumentException if <code>genMode</code> is
     * a value other than <code>OBJECT_LINEAR</code>, <code>EYE_LINEAR</code>,
     * <code>SPHERE_MAP</code>, <code>NORMAL_MAP</code>, or 
     * <code>REFLECTION_MAP</code>.
     *
     * @see Canvas3D#queryProperties
     */
    public void setGenMode(int genMode) {
        checkForLiveOrCompiled();

	if ((genMode < OBJECT_LINEAR) || (genMode > REFLECTION_MAP)) {
	    throw new IllegalArgumentException(
		J3dI18N.getString("TexCoordGeneration5"));
	}
	((TexCoordGenerationRetained)this.retained).initGenMode(genMode);
    }

    /**
     * Retrieves the current TexCoordGeneration generation mode.
     * @return the texture generation mode
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public int getGenMode() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_MODE_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("TexCoordGeneration3"));
	return ((TexCoordGenerationRetained)this.retained).getGenMode();
    }

    /**
     * Sets the S coordinate plane equation.  This plane equation
     * is used to generate the S coordinate in OBJECT_LINEAR and EYE_LINEAR
     * texture generation modes.
     * @param planeS plane equation for the S coordinate
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public void setPlaneS(Vector4f planeS) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_PLANE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("TexCoordGeneration6"));

	if (isLive())
	    ((TexCoordGenerationRetained)this.retained).setPlaneS(planeS);
	else
	    ((TexCoordGenerationRetained)this.retained).initPlaneS(planeS);
    }

    /**
     * Retrieves a copy of the plane equation used to
     * generate the S coordinate.
     * @param planeS the S coordinate plane equation
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void getPlaneS(Vector4f planeS) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_PLANE_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("TexCoordGeneration4"));
	((TexCoordGenerationRetained)this.retained).getPlaneS(planeS);
    }

    /**
     * Sets the T coordinate plane equation.  This plane equation
     * is used to generate the T coordinate in OBJECT_LINEAR and EYE_LINEAR
     * texture generation modes.
     * @param planeT plane equation for the T coordinate
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public void setPlaneT(Vector4f planeT) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_PLANE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("TexCoordGeneration6"));

	if (isLive())
	    ((TexCoordGenerationRetained)this.retained).setPlaneT(planeT);
	else
	    ((TexCoordGenerationRetained)this.retained).initPlaneT(planeT);
    }

    /**
     * Retrieves a copy of the plane equation used to
     * generate the T coordinate.
     * @param planeT the T coordinate plane equation
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void getPlaneT(Vector4f planeT) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_PLANE_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("TexCoordGeneration4"));
	((TexCoordGenerationRetained)this.retained).getPlaneT(planeT);
    }

    /**
     * Sets the R coordinate plane equation.  This plane equation
     * is used to generate the R coordinate in OBJECT_LINEAR and EYE_LINEAR
     * texture generation modes.
     * @param planeR plane equation for the R coordinate
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public void setPlaneR(Vector4f planeR) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_PLANE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("TexCoordGeneration6"));

	if (isLive())
	    ((TexCoordGenerationRetained)this.retained).setPlaneR(planeR);
	else
	    ((TexCoordGenerationRetained)this.retained).initPlaneR(planeR);
    }

    /**
     * Retrieves a copy of the plane equation used to
     * generate the R coordinate.
     * @param planeR the R coordinate plane equation
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void getPlaneR(Vector4f planeR) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_PLANE_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("TexCoordGeneration4"));
	((TexCoordGenerationRetained)this.retained).getPlaneR(planeR);
    }

    /**
     * Sets the Q coordinate plane equation.  This plane equation
     * is used to generate the Q coordinate in OBJECT_LINEAR and EYE_LINEAR
     * texture generation modes.
     * @param planeQ plane equation for the Q coordinate
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public void setPlaneQ(Vector4f planeQ) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_PLANE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("TexCoordGeneration6"));

	if (isLive())
	    ((TexCoordGenerationRetained)this.retained).setPlaneQ(planeQ);
	else
	    ((TexCoordGenerationRetained)this.retained).initPlaneQ(planeQ);
    }

    /**
     * Retrieves a copy of the plane equation used to
     * generate the Q coordinate.
     * @param planeQ the Q coordinate plane equation
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public void getPlaneQ(Vector4f planeQ) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_PLANE_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("TexCoordGeneration4"));
	((TexCoordGenerationRetained)this.retained).getPlaneQ(planeQ);
    }

    /**
     * Creates a retained mode TexCoordGenerationRetained object that this
     * TexCoordGeneration component object will point to.
     */
    void createRetained() {
	this.retained = new TexCoordGenerationRetained();
	this.retained.setSource(this);
    }

    /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)  
     */
    public NodeComponent cloneNodeComponent() {
        TexCoordGeneration tga = new TexCoordGeneration();
        tga.duplicateNodeComponent(this);
        return tga;
    }


    /**
     * Copies all node information from <code>originalNodeComponent</code> into
     * the current node.  This method is called from the
     * <code>duplicateNode</code> method. This routine does
     * the actual duplication of all "local data" (any data defined in
     * this object). 
     *
     * @param originalNodeComponent the original node to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */

    void duplicateAttributes(NodeComponent originalNodeComponent, 
			     boolean forceDuplicate) { 

	super.duplicateAttributes(originalNodeComponent, forceDuplicate);
      
	TexCoordGenerationRetained tex = (TexCoordGenerationRetained) 
	    originalNodeComponent.retained;
	TexCoordGenerationRetained rt = (TexCoordGenerationRetained) retained;

	Vector4f v = new Vector4f();

	rt.initGenMode(tex.getGenMode());
	tex.getPlaneS(v);
	rt.initPlaneS(v);
	tex.getPlaneT(v);
	rt.initPlaneT(v);
	tex.getPlaneR(v);
	rt.initPlaneR(v);      
	tex.getPlaneQ(v);
	rt.initPlaneQ(v);      
	rt.initFormat(tex.getFormat());
	rt.initEnable(tex.getEnable());
    }
}
