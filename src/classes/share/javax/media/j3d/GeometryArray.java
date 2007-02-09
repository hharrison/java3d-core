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


/**
 * The GeometryArray object contains separate arrays of positional
 * coordinates, colors, normals, texture coordinates, and vertex
 * attributes that
 * describe point, line, or polygon geometry.  This class is extended
 * to create the various primitive types (such as lines,
 * triangle strips, etc.).
 * Vertex data may be passed to this geometry array in one of two
 * ways: by copying the data into the array using the existing
 * methods, or by passing a reference to the data.
 * <p>
 * <ul>
 * <li>
 * <b>By Copying:</b>
 * The existing methods for setting positional coordinates, colors,
 * normals, texture coordinates, and vertex attributes
 * (such as <code>setCoordinate</code>,
 * <code>setColors</code>, etc.)  copy the data into this
 * GeometryArray.  This is appropriate for many applications and
 * offers an application much flexibility in organizing its data.
 * This is the default mode.
 * </li>
 * <li><b>By Reference:</b>
 * A new set of methods in Java 3D version 1.2 allows data to be
 * accessed by reference, directly from the user's arrays.  To use
 * this feature, set the <code>BY_REFERENCE</code> bit in the
 * <code>vertexFormat</code> field of the constructor for this
 * GeometryArray.  In this mode, the various set methods for
 * coordinates, normals, colors, texture coordinates, and vertex attributes
 * are not used.
 * Instead, new methods are used to set a reference to user-supplied
 * coordinate, color, normal, texture coordinate, and vertex attribute
 * arrays (such as
 * <code>setCoordRefFloat</code>, <code>setColorRefFloat</code>,
 * etc.).  Data in any array that is referenced by a live or compiled
 * GeometryArray object may only be modified via the
 * <code>updateData</code> method (subject to the
 * <code>ALLOW_REF_DATA_WRITE</code> capability bit).  Applications
 * must exercise care not to violate this rule.  If any referenced
 * geometry data is modified outside of the <code>updateData</code>
 * method, the results are undefined.
 * </li>
 * </ul>
 * <p>
 * All colors used in the geometry array object must be in the range [0.0,1.0].
 * Values outside this range will cause undefined results.
 * All normals used in the geometry array object must be unit length
 * vectors.  That is their geometric length must be 1.0.  Normals that
 * are not unit length vectors will cause undefined results.
 * <p>
 * Note that the term <i>coordinate</i>, as used in the method names
 * and method descriptions, actually refers to a set of <i>x</i>,
 * <i>y</i>, and <i>z</i> coordinates representing the position of a
 * single vertex.  The term <i>coordinates</i> (plural) is used to
 * indicate sets of <i>x</i>, <i>y</i>, and <i>z</i> coordinates for
 * multiple vertices.  This is somewhat at odds with the mathematical
 * definition of a coordinate, but is used as a convenient shorthand.
 * Similarly, the term <i>texture coordinate</i> is used to indicate a
 * set of texture coordinates for a single vertex, while the term
 * <i>texture coordinates</i> (plural) is used to indicate sets of
 * texture coordinates for multiple vertices.
 */

public abstract class GeometryArray extends Geometry {

  /**
   * Specifies that this GeometryArray allows reading the array of
   * coordinates.
   */
  public static final int
    ALLOW_COORDINATE_READ = CapabilityBits.GEOMETRY_ARRAY_ALLOW_COORDINATE_READ;

  /**
   * Specifies that this GeometryArray allows writing the array of
   * coordinates.
   */
  public static final int
    ALLOW_COORDINATE_WRITE = CapabilityBits.GEOMETRY_ARRAY_ALLOW_COORDINATE_WRITE;

  /**
   * Specifies that this GeometryArray allows reading the array of
   * colors.
   */
  public static final int
    ALLOW_COLOR_READ = CapabilityBits.GEOMETRY_ARRAY_ALLOW_COLOR_READ;

  /**
   * Specifies that this GeometryArray allows writing the array of
   * colors.
   */
  public static final int
    ALLOW_COLOR_WRITE = CapabilityBits.GEOMETRY_ARRAY_ALLOW_COLOR_WRITE;

  /**
   * Specifies that this GeometryArray allows reading the array of
   * normals.
   */
  public static final int
    ALLOW_NORMAL_READ = CapabilityBits.GEOMETRY_ARRAY_ALLOW_NORMAL_READ;

  /**
   * Specifies that this GeometryArray allows writing the array of
   * normals.
   */
  public static final int
    ALLOW_NORMAL_WRITE = CapabilityBits.GEOMETRY_ARRAY_ALLOW_NORMAL_WRITE;

  /**
   * Specifies that this GeometryArray allows reading the array of
   * texture coordinates.
   */
  public static final int
    ALLOW_TEXCOORD_READ = CapabilityBits.GEOMETRY_ARRAY_ALLOW_TEXCOORD_READ;

  /**
   * Specifies that this GeometryArray allows writing the array of
   * texture coordinates.
   */
  public static final int
    ALLOW_TEXCOORD_WRITE = CapabilityBits.GEOMETRY_ARRAY_ALLOW_TEXCOORD_WRITE;

  /**
   * Specifies that this GeometryArray allows reading the array of
   * vertex attributes.
   *
   * @since Java 3D 1.4
   */
  public static final int
    ALLOW_VERTEX_ATTR_READ = CapabilityBits.GEOMETRY_ARRAY_ALLOW_VERTEX_ATTR_READ;

  /**
   * Specifies that this GeometryArray allows writing the array of
   * vertex attributes.
   *
   * @since Java 3D 1.4
   */
  public static final int
    ALLOW_VERTEX_ATTR_WRITE = CapabilityBits.GEOMETRY_ARRAY_ALLOW_VERTEX_ATTR_WRITE;

  /**
   * Specifies that this GeometryArray allows reading the count or
   * initial index information for this object.
   */
  public static final int
    ALLOW_COUNT_READ = CapabilityBits.GEOMETRY_ARRAY_ALLOW_COUNT_READ;

  /**
   * Specifies that this GeometryArray allows writing the count or
   * initial index information for this object.
   *
   * @since Java 3D 1.2
   */
  public static final int
    ALLOW_COUNT_WRITE = CapabilityBits.GEOMETRY_ARRAY_ALLOW_COUNT_WRITE;

  /**
   * Specifies that this GeometryArray allows reading the vertex format
   * information for this object.
   */
  public static final int
    ALLOW_FORMAT_READ = CapabilityBits.GEOMETRY_ARRAY_ALLOW_FORMAT_READ;

  /**
   * Specifies that this GeometryArray allows reading the geometry
   * data reference information for this object.  This is only used in
   * by-reference geometry mode.
   *
   * @since Java 3D 1.2
   */
  public static final int
    ALLOW_REF_DATA_READ = CapabilityBits.GEOMETRY_ARRAY_ALLOW_REF_DATA_READ;

  private static final int J3D_1_2_ALLOW_REF_DATA_READ =
    CapabilityBits.J3D_1_2_GEOMETRY_ARRAY_ALLOW_REF_DATA_READ;

  /**
   * Specifies that this GeometryArray allows writing the geometry
   * data reference information for this object.  It also enables
   * writing the referenced data itself, via the GeometryUpdater
   * interface.  This is only used in by-reference geometry mode.
   *
   * @since Java 3D 1.2
   */
  public static final int
    ALLOW_REF_DATA_WRITE = CapabilityBits.GEOMETRY_ARRAY_ALLOW_REF_DATA_WRITE;

  /**
   * Specifies that this GeometryArray contains an array of coordinates.
   * This bit must be set.
   */
  public static final int COORDINATES = 0x01;

  /**
   * Specifies that this GeometryArray contains an array of normals.
   */
  public static final int NORMALS = 0x02;

  /**
   * Specifies that this GeometryArray contains an array of colors.
   */
  static final int COLOR = 0x04;

  /**
   * Specifies that this GeometryArray's colors contain alpha.
   */
  static final int WITH_ALPHA = 0x08;

  /**
   * Specifies that this GeometryArray contains an array of colors without alpha.
   */
  public static final int COLOR_3 = COLOR;

  /**
   * Specifies that this GeometryArray contains an array of colors with alpha.
   * This takes precedence over COLOR_3.
   */
  public static final int COLOR_4 = COLOR | WITH_ALPHA;

  /**
   * Specifies that this GeometryArray contains one or more arrays of
   * 2D texture coordinates.
   */
  public static final int TEXTURE_COORDINATE_2 = 0x20;

  /**
   * Specifies that this GeometryArray contains one or more arrays of
   * 3D texture coordinates.
   * This takes precedence over TEXTURE_COORDINATE_2.
   */
  public static final int TEXTURE_COORDINATE_3 = 0x40;


  /**
   * Specifies that this GeometryArray contains one or more arrays of
   * 4D texture coordinates.
   * This takes precedence over TEXTURE_COORDINATE_2 and TEXTURE_COORDINATE_3.
   *
   * @since Java 3D 1.3
   */
  public static final int TEXTURE_COORDINATE_4 = 0x400;


  static final int TEXTURE_COORDINATE = TEXTURE_COORDINATE_2 |
                                        TEXTURE_COORDINATE_3 |
					TEXTURE_COORDINATE_4;

    /**
     * Specifies that the position, color, normal, and texture coordinate
     * data for this GeometryArray are accessed by reference.
     *
     * @since Java 3D 1.2
     */
    public static final int BY_REFERENCE = 0x80;


    /**
     * Specifies that the position, color, normal, and texture
     * coordinate data for this GeometryArray are accessed via a single
     * interleaved, floating-point array reference.  All of the data
     * values for each vertex are stored in consecutive memory
     * locations.  This flag is only valid in conjunction with the
     * <code>BY_REFERENCE</code> flag.
     *
     * @since Java 3D 1.2
     */
    public static final int INTERLEAVED = 0x100;

    /**
     * Specifies that geometry by-reference data for this
     * GeometryArray, whether interleaved or non-interleaved, is
     * accessed via J3DBuffer objects that wrap NIO Buffer objects,
     * rather than float, double, byte, or TupleXX arrays.  This flag
     * is only valid in conjunction with the <code>BY_REFERENCE</code>
     * flag.
     *
     * @see J3DBuffer
     * @see #setCoordRefBuffer(J3DBuffer)
     * @see #setColorRefBuffer(J3DBuffer)
     * @see #setNormalRefBuffer(J3DBuffer)
     * @see #setTexCoordRefBuffer(int,J3DBuffer)
     * @see #setVertexAttrRefBuffer(int,J3DBuffer)
     * @see #setInterleavedVertexBuffer(J3DBuffer)
     *
     * @since Java 3D 1.3
     */
    public static final int USE_NIO_BUFFER = 0x800;

    /**
     * Specifies that only the coordinate indices are used for indexed
     * geometry arrays.  In this mode, the values from the coordinate
     * index array are used as a single set of index values to access
     * the vertex data for all five vertex components (coord, color,
     * normal, texCoord, and vertexAttr).  The color, normal, texCoord,
     * and vertexAttr index arrays are neither allocated nor used. Any
     * attempt to access the color, normal, texCoord,
     * or vertexAttr index arrays will result in a NullPointerException.
     * This flag is only valid for indexed geometry arrays
     * (subclasses of IndexedGeometryArray).
     *
     * @since Java 3D 1.3
     */
    public static final int USE_COORD_INDEX_ONLY = 0x200;

    /**
     * Specifies that this GeometryArray contains one or more arrays of
     * vertex attributes. These attributes are used in programmable
     * shading.
     *
     * @since Java 3D 1.4
     */
    public static final int VERTEX_ATTRIBUTES = 0x1000;

    //NVaidya
    /**
     * Specifies that the indices in this GeometryArray 
     * are accessed by reference. This flag is only valid for 
     * indexed geometry arrays (subclasses of IndexedGeometryArray) and only
     * when used in conjunction with the <code>BY_REFERENCE</code> and  
     * <code>USE_COORD_INDEX_ONLY</code> flags.
     *
     * @since Java 3D 1.5
     */
    public static final int BY_REFERENCE_INDICES = 0x2000;

    // Used to keep track of the last bit (for adding new bits only)
    private static final int LAST_FORMAT_BIT = 0x2000;

    // Scratch arrays for converting Point[234]f to TexCoord[234]f
    private TexCoord2f [] texCoord2fArray = null;
    private TexCoord3f [] texCoord3fArray = null;
    private TexCoord4f [] texCoord4fArray = null;
    private TexCoord2f texCoord2fScratch = null;
    private TexCoord3f texCoord3fScratch = null;
    
    private static final int[] defTexCoordMap = { 0 };

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_COLOR_READ,
        ALLOW_COORDINATE_READ,
        ALLOW_COUNT_READ,
        ALLOW_FORMAT_READ,
        ALLOW_NORMAL_READ,
        ALLOW_REF_DATA_READ,
        ALLOW_TEXCOORD_READ,
        ALLOW_VERTEX_ATTR_READ        
    };
    

    // non-public, no parameter constructor
    GeometryArray() {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    //NVaidya
    /**
     * Constructs an empty GeometryArray object with the specified
     * number of vertices and vertex format.  Defaults are used
     * for all other parameters.  The default values are as follows:
     * <ul>
     * texCoordSetCount : 1<br>
     * texCoordSetMap : { 0 }<br>
     * vertexAttrCount : 0<br>
     * vertexAttrSizes : null<br>
     * validVertexCount : vertexCount<br>
     * initialVertexIndex : 0<br>
     * initialCoordIndex : 0<br>
     * initialColorIndex : 0<br>
     * initialNormalIndex : 0<br>
     * initialTexCoordIndex : 0<br>
     * initialVertexAttrIndex : 0<br>
     * all data array values : 0.0<br>
     * all data array references : null<br>
     * </ul>
     *
     * @param vertexCount the number of vertex elements in this GeometryArray
     * @param vertexFormat a mask indicating which components are
     * present in each vertex.  This is specified as one or more
     * individual flags that are bitwise "OR"ed together to describe
     * the per-vertex data.
     * The flags include: <code>COORDINATES</code>, to signal the inclusion of
     * vertex positions--always present; <code>NORMALS</code>, to signal
     * the inclusion of per vertex normals; one of <code>COLOR_3</code> or
     * <code>COLOR_4</code>, to signal the inclusion of per vertex
     * colors (without or with alpha information); one of
     * <code>TEXTURE_COORDINATE_2</code>, <code>TEXTURE_COORDINATE_3</code>
     * or <code>TEXTURE_COORDINATE_4</code>,
     * to signal the
     * inclusion of per-vertex texture coordinates (2D, 3D or 4D);
     * <code>BY_REFERENCE</code>, to indicate that the data is passed
     * by reference
     * rather than by copying; <code>INTERLEAVED</code>, to indicate
     * that the referenced
     * data is interleaved in a single array;
     * <code>USE_NIO_BUFFER</code>, to indicate that the referenced data
     * is accessed via a J3DBuffer object that wraps an NIO buffer;
     * <code>USE_COORD_INDEX_ONLY</code>,
     * to indicate that only the coordinate indices are used for indexed
     * geometry arrays;
     * <code>BY_REFERENCE_INDICES</code>, to indicate
     * that the indices are accessed by reference in indexed
     * geometry arrays.<p>
     *
     * @exception IllegalArgumentException if vertexCount &lt; 0
     *
     * @exception IllegalArgumentException if vertexFormat does <b>not</b>
     * include <code>COORDINATES</code>
     *
     * @exception IllegalArgumentException if the <code>USE_COORD_INDEX_ONLY</code>
     * bit or the <code>BY_REFERENCE_INDICES</code> bit is set for 
     * non-indexed geometry arrays (that is, GeometryArray objects
     * that are not a subclass of IndexedGeometryArray)
     *
     * @exception IllegalArgumentException if the <code>INTERLEAVED</code>
     * bit is set without the <code>BY_REFERENCE</code> bit being set
     *
     * @exception IllegalArgumentException if the <code>USE_NIO_BUFFER</code>
     * bit is set without the <code>BY_REFERENCE</code> bit being set
     *
     * @exception IllegalArgumentException if the <code>INTERLEAVED</code>
     * bit and the <code>VERTEX_ATTRIBUTES</code> bit are both set
     *
     * @exception IllegalArgumentException if the 
     * <code>BY_REFERENCE_INDICES</code>
     * bit is set without the <code>BY_REFERENCE</code> and 
     * <code>USE_COORD_INDEX_ONLY</code> bits being set
     */
    public GeometryArray(int vertexCount, int vertexFormat) {
        this(vertexCount, vertexFormat,
            ((vertexFormat & TEXTURE_COORDINATE) != 0 ? 1 : 0),
            ((vertexFormat & TEXTURE_COORDINATE) != 0 ? defTexCoordMap : null));
    }


    //NVaidya
    /**
     * Constructs an empty GeometryArray object with the specified
     * number of vertices, vertex format, number of texture coordinate
     * sets, and texture coordinate mapping array.  Defaults are used
     * for all other parameters.
     *
     * @param vertexCount the number of vertex elements in this
     * GeometryArray<p>
     *
     * @param vertexFormat a mask indicating which components are
     * present in each vertex.  This is specified as one or more
     * individual flags that are bitwise "OR"ed together to describe
     * the per-vertex data.
     * The flags include: <code>COORDINATES</code>, to signal the inclusion of
     * vertex positions--always present; <code>NORMALS</code>, to signal
     * the inclusion of per vertex normals; one of <code>COLOR_3</code> or
     * <code>COLOR_4</code>, to signal the inclusion of per vertex
     * colors (without or with alpha information); one of
     * <code>TEXTURE_COORDINATE_2</code> or <code>TEXTURE_COORDINATE_3</code>
     * or <code>TEXTURE_COORDINATE_4</code>,
     * to signal the
     * inclusion of per-vertex texture coordinates (2D , 3D or 4D);
     * <code>BY_REFERENCE</code>, to indicate that the data is passed
     * by reference
     * rather than by copying; <code>INTERLEAVED</code>, to indicate
     * that the referenced
     * data is interleaved in a single array;
     * <code>USE_NIO_BUFFER</code>, to indicate that the referenced data
     * is accessed via a J3DBuffer object that wraps an NIO buffer;
     * <code>USE_COORD_INDEX_ONLY</code>,
     * to indicate that only the coordinate indices are used for indexed
     * geometry arrays;
     * <code>BY_REFERENCE_INDICES</code>, to indicate
     * that the indices are accessed by reference in indexed
     * geometry arrays.<p>
     *
     * @param texCoordSetCount the number of texture coordinate sets
     * in this GeometryArray object.  If <code>vertexFormat</code>
     * does not include one of <code>TEXTURE_COORDINATE_2</code> or
     * <code>TEXTURE_COORDINATE_3</code>, the
     * <code>texCoordSetCount</code> parameter is not used.<p>
     *
     * <a name="texCoordSetMap">
     * @param texCoordSetMap an array that maps texture coordinate
     * sets to texture units.  The array is indexed by texture unit
     * number for each texture unit in the associated Appearance
     * object.  The values in the array specify the texture coordinate
     * set within this GeometryArray object that maps to the
     * corresponding texture
     * unit.  All elements within the array must be less than
     * <code>texCoordSetCount</code>.  A negative value specifies that
     * no texture coordinate set maps to the texture unit
     * corresponding to the index.  If there are more texture units in
     * any associated Appearance object than elements in the mapping
     * array, the extra elements are assumed to be -1.  The same
     * texture coordinate set may be used for more than one texture
     * unit.  Each texture unit in every associated Appearance must
     * have a valid source of texture coordinates: either a
     * non-negative texture coordinate set must be specified in the
     * mapping array or texture coordinate generation must be enabled.
     * Texture coordinate generation will take precedence for those
     * texture units for which a texture coordinate set is specified
     * and texture coordinate generation is enabled.  If
     * <code>vertexFormat</code> does not include one of
     * <code>TEXTURE_COORDINATE_2</code> or
     * <code>TEXTURE_COORDINATE_3</code> or
     * <code>TEXTURE_COORDINATE_4</code>, the
     * <code>texCoordSetMap</code> array is not used.  The following example
     * illustrates the use of the <code>texCoordSetMap</code> array.
     *
     * <p>
     * <ul>
     * <table BORDER=1 CELLSPACING=2 CELLPADDING=2>
     * <tr>
     * <td><center><b>Index</b></center></td>
     * <td><center><b>Element</b></center></td>
     * <td><b>Description</b></td>
     * </tr>
     * <tr>
     * <td><center>0</center></td>
     * <td><center>1</center></td>
     * <td>Use tex coord set 1 for tex unit 0</td>
     * </tr>
     * <tr>
     * <td><center>1</center></td>
     * <td><center>-1</center></td>
     * <td>Use no tex coord set for tex unit 1</td>
     * </tr>
     * <tr>
     * <td><center>2</center></td>
     * <td><center>0</center></td>
     * <td>Use tex coord set 0 for tex unit 2</td>
     * </tr>
     * <tr>
     * <td><center>3</center></td>
     * <td><center>1</center></td>
     * <td>Reuse tex coord set 1 for tex unit 3</td>
     * </tr>
     * </table>
     * </ul>
     * <p>
     *
     * @exception IllegalArgumentException if vertexCount &lt; 0
     *
     * @exception IllegalArgumentException if vertexFormat does <b>not</b>
     * include <code>COORDINATES</code>
     *
     * @exception IllegalArgumentException if the <code>USE_COORD_INDEX_ONLY</code>
     * bit or the <code>BY_REFERENCE_INDICES</code> bit is set for 
     * non-indexed geometry arrays (that is, GeometryArray objects
     * that are not a subclass of IndexedGeometryArray)
     *
     * @exception IllegalArgumentException if the <code>INTERLEAVED</code>
     * bit is set without the <code>BY_REFERENCE</code> bit being set
     *
     * @exception IllegalArgumentException if the <code>USE_NIO_BUFFER</code>
     * bit is set without the <code>BY_REFERENCE</code> bit being set
     *
     * @exception IllegalArgumentException if the <code>INTERLEAVED</code>
     * bit and the <code>VERTEX_ATTRIBUTES</code> bit are both set
     *
     * @exception IllegalArgumentException if the 
     * <code>BY_REFERENCE_INDICES</code>
     * bit is set without the <code>BY_REFERENCE</code> and 
     * <code>USE_COORD_INDEX_ONLY</code> bits being set
     *
     * @exception IllegalArgumentException if
     * <code>texCoordSetCount&nbsp;&lt;&nbsp;0</code>
     *
     * @exception IllegalArgumentException if any element in
     * <code>texCoordSetMap[]&nbsp;&gt;=&nbsp;texCoordSetCount</code>.
     *
     * @since Java 3D 1.2
     */
    public GeometryArray(int vertexCount,
			 int vertexFormat,
			 int texCoordSetCount,
			 int[] texCoordSetMap) {
	this(vertexCount, vertexFormat, texCoordSetCount, texCoordSetMap, 0, null);
    }


    //NVaidya
    /**
     * Constructs an empty GeometryArray object with the specified
     * number of vertices, vertex format, number of texture coordinate
     * sets, texture coordinate mapping array, vertex attribute count,
     * and vertex attribute sizes array.
     *
     * @param vertexCount the number of vertex elements in this
     * GeometryArray<p>
     *
     * @param vertexFormat a mask indicating which components are
     * present in each vertex.  This is specified as one or more
     * individual flags that are bitwise "OR"ed together to describe
     * the per-vertex data.
     * The flags include: <code>COORDINATES</code>, to signal the inclusion of
     * vertex positions--always present; <code>NORMALS</code>, to signal
     * the inclusion of per vertex normals; one of <code>COLOR_3</code> or
     * <code>COLOR_4</code>, to signal the inclusion of per vertex
     * colors (without or with alpha information); one of
     * <code>TEXTURE_COORDINATE_2</code> or <code>TEXTURE_COORDINATE_3</code>
     * or <code>TEXTURE_COORDINATE_4</code>,
     * to signal the
     * inclusion of per-vertex texture coordinates (2D , 3D or 4D);
     * <code>VERTEX_ATTRIBUTES</code>, to signal
     * the inclusion of one or more arrays of vertex attributes;
     * <code>BY_REFERENCE</code>, to indicate that the data is passed
     * by reference
     * rather than by copying; <code>INTERLEAVED</code>, to indicate
     * that the referenced
     * data is interleaved in a single array;
     * <code>USE_NIO_BUFFER</code>, to indicate that the referenced data
     * is accessed via a J3DBuffer object that wraps an NIO buffer;
     * <code>USE_COORD_INDEX_ONLY</code>,
     * to indicate that only the coordinate indices are used for indexed
     * geometry arrays;
     * <code>BY_REFERENCE_INDICES</code>, to indicate
     * that the indices are accessed by reference in indexed
     * geometry arrays.<p>
     *
     * @param texCoordSetCount the number of texture coordinate sets
     * in this GeometryArray object.  If <code>vertexFormat</code>
     * does not include one of <code>TEXTURE_COORDINATE_2</code> or
     * <code>TEXTURE_COORDINATE_3</code>, the
     * <code>texCoordSetCount</code> parameter is not used.<p>
     *
     * <a name="texCoordSetMap">
     * @param texCoordSetMap an array that maps texture coordinate
     * sets to texture units.  The array is indexed by texture unit
     * number for each texture unit in the associated Appearance
     * object.  The values in the array specify the texture coordinate
     * set within this GeometryArray object that maps to the
     * corresponding texture
     * unit.  All elements within the array must be less than
     * <code>texCoordSetCount</code>.  A negative value specifies that
     * no texture coordinate set maps to the texture unit
     * corresponding to the index.  If there are more texture units in
     * any associated Appearance object than elements in the mapping
     * array, the extra elements are assumed to be -1.  The same
     * texture coordinate set may be used for more than one texture
     * unit.  Each texture unit in every associated Appearance must
     * have a valid source of texture coordinates: either a
     * non-negative texture coordinate set must be specified in the
     * mapping array or texture coordinate generation must be enabled.
     * Texture coordinate generation will take precedence for those
     * texture units for which a texture coordinate set is specified
     * and texture coordinate generation is enabled.  If
     * <code>vertexFormat</code> does not include one of
     * <code>TEXTURE_COORDINATE_2</code> or
     * <code>TEXTURE_COORDINATE_3</code> or
     * <code>TEXTURE_COORDINATE_4</code>, the
     * <code>texCoordSetMap</code> array is not used.  The following example
     * illustrates the use of the <code>texCoordSetMap</code> array.
     *
     * <p>
     * <ul>
     * <table BORDER=1 CELLSPACING=2 CELLPADDING=2>
     * <tr>
     * <td><center><b>Index</b></center></td>
     * <td><center><b>Element</b></center></td>
     * <td><b>Description</b></td>
     * </tr>
     * <tr>
     * <td><center>0</center></td>
     * <td><center>1</center></td>
     * <td>Use tex coord set 1 for tex unit 0</td>
     * </tr>
     * <tr>
     * <td><center>1</center></td>
     * <td><center>-1</center></td>
     * <td>Use no tex coord set for tex unit 1</td>
     * </tr>
     * <tr>
     * <td><center>2</center></td>
     * <td><center>0</center></td>
     * <td>Use tex coord set 0 for tex unit 2</td>
     * </tr>
     * <tr>
     * <td><center>3</center></td>
     * <td><center>1</center></td>
     * <td>Reuse tex coord set 1 for tex unit 3</td>
     * </tr>
     * </table>
     * </ul>
     * <p>
     *
     * @param vertexAttrCount the number of vertex attributes
     * in this GeometryArray object. If <code>vertexFormat</code>
     * does not include <code>VERTEX_ATTRIBUTES</code>, the
     * <code>vertexAttrCount</code> parameter must be 0.<p>
     *
     * @param vertexAttrSizes is an array that specifes the size of
     * each vertex attribute. Each element in the array specifies the
     * number of components in the attribute, from 1 to 4. The length
     * of the array must be equal to <code>vertexAttrCount</code>.<p>
     *
     * @exception IllegalArgumentException if vertexCount &lt; 0
     *
     * @exception IllegalArgumentException if vertexFormat does <b>not</b>
     * include <code>COORDINATES</code>
     *
     * @exception IllegalArgumentException if the <code>USE_COORD_INDEX_ONLY</code>
     * bit or the <code>BY_REFERENCE_INDICES</code> bit is set for 
     * non-indexed geometry arrays (that is, GeometryArray objects
     * that are not a subclass of IndexedGeometryArray)
     *
     * @exception IllegalArgumentException if the <code>INTERLEAVED</code>
     * bit is set without the <code>BY_REFERENCE</code> bit being set
     *
     * @exception IllegalArgumentException if the <code>USE_NIO_BUFFER</code>
     * bit is set without the <code>BY_REFERENCE</code> bit being set
     *
     * @exception IllegalArgumentException if the <code>INTERLEAVED</code>
     * bit and the <code>VERTEX_ATTRIBUTES</code> bit are both set
     *
     * @exception IllegalArgumentException if the 
     * <code>BY_REFERENCE_INDICES</code>
     * bit is set without the <code>BY_REFERENCE</code> and 
     * <code>USE_COORD_INDEX_ONLY</code> bits being set
     *
     * @exception IllegalArgumentException if
     * <code>texCoordSetCount&nbsp;&lt;&nbsp;0</code>
     *
     * @exception IllegalArgumentException if any element in
     * <code>texCoordSetMap[]&nbsp;&gt;=&nbsp;texCoordSetCount</code>.
     *
     * @exception IllegalArgumentException if
     * <code>vertexAttrCount&nbsp;&gt;&nbsp;0</code> and the
     * <code>VERTEX_ATTRIBUTES</code> bit is not set
     *
     * @exception IllegalArgumentException if
     * <code>vertexAttrCount&nbsp;&lt;&nbsp;0</code>
     *
     * @exception IllegalArgumentException if
     * <code>vertexAttrSizes.length&nbsp;!=&nbsp;vertexAttrCount</code>
     *
     * @exception IllegalArgumentException if any element in
     * <code>vertexAttrSizes[]</code> is <code>&lt; 1</code> or
     * <code>&gt; 4</code>.
     *
     * @since Java 3D 1.4
     */
    public GeometryArray(int vertexCount,
			 int vertexFormat,
			 int texCoordSetCount,
			 int[] texCoordSetMap,
			 int vertexAttrCount,
			 int[] vertexAttrSizes) {

        if (vertexCount < 0)
            throw new IllegalArgumentException(J3dI18N.getString("GeometryArray96"));

        if (texCoordSetCount < 0)
            throw new IllegalArgumentException(J3dI18N.getString("GeometryArray124"));

        if (vertexAttrCount < 0)
            throw new IllegalArgumentException(J3dI18N.getString("GeometryArray125"));

        if ((vertexFormat & COORDINATES) == 0)
          throw new IllegalArgumentException(J3dI18N.getString("GeometryArray0"));

        if ((vertexFormat & INTERLEAVED) != 0 &&
            (vertexFormat & BY_REFERENCE) == 0)
            throw new IllegalArgumentException(J3dI18N.getString("GeometryArray80"));

        if ((vertexFormat & INTERLEAVED) != 0 &&
                (vertexFormat & VERTEX_ATTRIBUTES) != 0) {
            throw new IllegalArgumentException(J3dI18N.getString("GeometryArray128"));
        }

        if ((vertexFormat & USE_COORD_INDEX_ONLY) != 0 &&
                !(this instanceof IndexedGeometryArray)) {
            throw new IllegalArgumentException(J3dI18N.getString("GeometryArray135"));
        }

        //NVaidya
        if ((vertexFormat & BY_REFERENCE_INDICES) != 0) {
            if (!(this instanceof IndexedGeometryArray))
                throw new IllegalArgumentException(J3dI18N.getString("GeometryArray136"));
            if ((vertexFormat & BY_REFERENCE) == 0)
                throw new IllegalArgumentException(J3dI18N.getString("GeometryArray137"));
            if ((vertexFormat & USE_COORD_INDEX_ONLY) == 0)
                throw new IllegalArgumentException(J3dI18N.getString("GeometryArray138"));
        }

        if ((vertexFormat & USE_NIO_BUFFER) != 0 &&
            (vertexFormat & BY_REFERENCE) == 0)
	    throw new IllegalArgumentException(J3dI18N.getString("GeometryArray117"));

	if ((vertexFormat & TEXTURE_COORDINATE) != 0) {
	    if (texCoordSetMap == null)
                throw new IllegalArgumentException(J3dI18N.getString("GeometryArray106"));

	    if (texCoordSetCount == 0)
                throw new IllegalArgumentException(J3dI18N.getString("GeometryArray107"));

	    for (int i = 0; i < texCoordSetMap.length; i++) {
	         if (texCoordSetMap[i] >= texCoordSetCount)
                     throw new IllegalArgumentException(J3dI18N.getString("GeometryArray108"));
	    }

	    if ((vertexFormat & TEXTURE_COORDINATE_2) != 0) {
	        texCoord2fArray = new TexCoord2f[1];
	        texCoord2fScratch = new TexCoord2f();
	    }
	    else if ((vertexFormat & TEXTURE_COORDINATE_3) != 0) {
	        texCoord3fArray = new TexCoord3f[1];
	        texCoord3fScratch = new TexCoord3f();
	    }
	    else if ((vertexFormat & TEXTURE_COORDINATE_4) != 0) {
	        texCoord4fArray = new TexCoord4f[1];
	    }
	}
        
        if ((vertexFormat & VERTEX_ATTRIBUTES) != 0) {
            if (vertexAttrCount > 0) {
                if (vertexAttrCount != vertexAttrSizes.length) {
                    throw new IllegalArgumentException(J3dI18N.getString("GeometryArray132"));
                }
                
                for (int i = 0; i < vertexAttrSizes.length; i++) {
                    if (vertexAttrSizes[i] < 1 || vertexAttrSizes[i] > 4) {
                        throw new IllegalArgumentException(J3dI18N.getString("GeometryArray133"));
                    }
                }
            } else {
                if (vertexAttrSizes != null && vertexAttrSizes.length != 0) {
                    throw new IllegalArgumentException(J3dI18N.getString("GeometryArray132"));
                }
            }
        } else {
            if (vertexAttrCount > 0) {
                throw new IllegalArgumentException(J3dI18N.getString("GeometryArray131"));
            }
        }
        
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
        
        ((GeometryArrayRetained)this.retained).createGeometryArrayData(
	    vertexCount, vertexFormat,
	    texCoordSetCount, texCoordSetMap,
	    vertexAttrCount, vertexAttrSizes);

    }


    //------------------------------------------------------------------
    // Common methods
    //------------------------------------------------------------------

    /**
     * Retrieves the number of vertices in this GeometryArray
     * @return number of vertices in this GeometryArray
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     */
    public int getVertexCount() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COUNT_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray1"));

	return ((GeometryArrayRetained)this.retained).getVertexCount();
    }

    /**
     * Retrieves the vertexFormat of this GeometryArray
     * @return format of vertices in this GeometryArray
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     */
    public int getVertexFormat() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_FORMAT_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray2"));

	return ((GeometryArrayRetained)this.retained).getVertexFormat();
    }


    /**
     * Retrieves the number of texture coordinate sets in this
     * GeometryArray object.
     *
     * @return the number of texture coordinate sets
     * in this GeometryArray object
     *
     * @since Java 3D 1.2
     */
    public int getTexCoordSetCount() {
	return ((GeometryArrayRetained)this.retained).getTexCoordSetCount();
    }


    /**
     * Retrieves the length of the texture coordinate set mapping
     * array of this GeometryArray object.
     *
     * @return the length of the texture coordinate set mapping
     * array of this GeometryArray object
     *
     * @since Java 3D 1.2
     */
    public int getTexCoordSetMapLength() {
	return ((GeometryArrayRetained)this.retained).getTexCoordSetMapLength();
    }


    /**
     * Retrieves the texture coordinate set mapping
     * array from this GeometryArray object.
     *
     * @param texCoordSetMap an array that will receive a copy of the
     * texture coordinate set mapping array.  The array must be large
     * enough to hold all entries of the texture coordinate set
     * mapping array.
     *
     * @since Java 3D 1.2
     */
    public void getTexCoordSetMap(int[] texCoordSetMap) {
        ((GeometryArrayRetained)this.retained).getTexCoordSetMap(texCoordSetMap);
    }


    /**
     * Retrieves the number of vertex attributes in this GeometryArray
     * object.
     *
     * @return the number of vertex attributes in this GeometryArray
     * object
     *
     * @since Java 3D 1.4
     */
    public int getVertexAttrCount() {
        return ((GeometryArrayRetained)this.retained).getVertexAttrCount();
    }


    /**
     * Retrieves the vertex attribute sizes array from this
     * GeometryArray object.
     *
     * @param vertexAttrSizes an array that will receive a copy of
     * the vertex attribute sizes array.  The array must hold at least
     * <code>vertexAttrCount</code> elements.
     *
     * @since Java 3D 1.4
     */
    public void getVertexAttrSizes(int[] vertexAttrSizes) {
        ((GeometryArrayRetained)this.retained).getVertexAttrSizes(vertexAttrSizes);
    }


    /**
     * Updates geometry array data that is accessed by reference.
     * This method calls the updateData method of the specified
     * GeometryUpdater object to synchronize updates to vertex
     * data that is referenced by this GeometryArray object.
     * Applications that wish to modify such data must perform all
     * updates via this method.
     * <p>
     * This method may also be used to atomically set multiple
     * references (for example, to coordinate and color arrays)
     * or to atomically
     * change multiple data values through the geometry data copying
     * methods.
     *
     * @param updater object whose updateData callback method will be
     * called to update the data referenced by this GeometryArray.
     * @exception CapabilityNotSetException if the appropriate capability
     * is not set, the vertex data mode is <code>BY_REFERENCE</code>, and this
     * object is part of a live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public void updateData(GeometryUpdater updater) {
	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0 &&
	    isLiveOrCompiled() &&
	    !this.getCapability(ALLOW_REF_DATA_WRITE)) {

	    throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray81"));
	}

	((GeometryArrayRetained)this.retained).updateData(updater);
    }


    /**
     * Sets the valid vertex count for this GeometryArray object.
     * This count specifies the number of vertices actually used in
     * rendering or other operations such as picking and collision.
     * This attribute is initialized to <code>vertexCount</code>.
     *
     * @param validVertexCount the new valid vertex count.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * <p>
     * @exception IllegalArgumentException if any of the following are
     * true:
     * <ul>
     * <code>validVertexCount &lt; 0</code>,<br>
     * <code>initialVertexIndex + validVertexCount &gt; vertexCount</code>,<br>
     * <code>initialCoordIndex + validVertexCount &gt; vertexCount</code>,<br>
     * <code>initialColorIndex + validVertexCount &gt; vertexCount</code>,<br>
     * <code>initialNormalIndex + validVertexCount &gt; vertexCount</code>,<br>
     * <code>initialTexCoordIndex + validVertexCount &gt; vertexCount</code>,<br>
     * <code>initialVertexAttrIndex + validVertexCount &gt; vertexCount</code>
     * </ul>
     * <p>
     * @exception ArrayIndexOutOfBoundsException if the geometry data format
     * is <code>BY_REFERENCE</code> and any the following
     * are true for non-null array references:
     * <ul>
     * <code>CoordRef.length</code> &lt; <i>num_words</i> *
     * (<code>initialCoordIndex + validVertexCount</code>),<br>
     * <code>ColorRef.length</code> &lt; <i>num_words</i> *
     * (<code>initialColorIndex + validVertexCount</code>),<br>
     * <code>NormalRef.length</code> &lt; <i>num_words</i> *
     * (<code>initialNormalIndex + validVertexCount</code>),<br>
     * <code>TexCoordRef.length</code> &lt; <i>num_words</i> *
     * (<code>initialTexCoordIndex + validVertexCount</code>),<br>
     * <code>VertexAttrRef.length</code> &lt; <i>num_words</i> *
     * (<code>initialVertexAttrIndex + validVertexCount</code>),<br>
     * <code>InterleavedVertices.length</code> &lt; <i>words_per_vertex</i> *
     * (<code>initialVertexIndex + validVertexCount</code>)<br>
     * </ul>
     * where <i>num_words</i> depends on which variant of
     * <code>set</code><i>Array</i><code>Ref</code> is used, and
     * <i>words_per_vertex</i> depends on which vertex formats are enabled
     * for interleaved arrays.
     *
     * @since Java 3D 1.2
     */
    public void setValidVertexCount(int validVertexCount) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COUNT_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray88"));

        if (validVertexCount < 0)
	    throw new IllegalArgumentException(J3dI18N.getString("GeometryArray96"));

	((GeometryArrayRetained)this.retained).setValidVertexCount(validVertexCount);
	// NOTE: the checks for initial*Index + validVertexCount &gt;
	// vertexCount need to be done in the retained method
    }


    /**
     * Gets the valid vertex count for this GeometryArray object.
     * For geometry strip primitives (subclasses of GeometryStripArray),
     * the valid vertex count is defined to be the sum of the
     * stripVertexCounts array.
     * @return the current valid vertex count
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public int getValidVertexCount() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COUNT_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray89"));

	return ((GeometryArrayRetained)this.retained).getValidVertexCount();
    }


    /**
     * Copies all node information from <code>originalNodeComponent</code>
     * into the current node.  This method is called from the
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
        // vertexFormat and vertexCount are copied in subclass when constructor
        //  public GeometryArray(int vertexCount, int vertexFormat) is used
        // in cloneNodeComponent()
        GeometryArrayRetained src = (GeometryArrayRetained) originalNodeComponent.retained;
        GeometryArrayRetained dst = (GeometryArrayRetained) retained;
        int format = src.getVertexFormat();
        if ((format & BY_REFERENCE) == 0) {
            System.arraycopy(src.vertexData, 0, dst.vertexData, 0,
                    src.vertexData.length);
            dst.setInitialVertexIndex(src.getInitialVertexIndex());

        } else {
            dst.setInitialCoordIndex(src.getInitialCoordIndex());
            dst.setInitialColorIndex(src.getInitialColorIndex());
            dst.setInitialNormalIndex(src.getInitialNormalIndex());
            int setCount = src.getTexCoordSetCount();
            int vAttrCount = src.getVertexAttrCount();
            for (int i=0; i < setCount; i++) {
                dst.setInitialTexCoordIndex(i, src.getInitialTexCoordIndex(i));
            }
            if ((format & INTERLEAVED) == 0) {
                if ((format & USE_NIO_BUFFER) == 0) {
                    // Java arrays
                    dst.setCoordRefFloat(src.getCoordRefFloat());
                    dst.setCoordRefDouble(src.getCoordRefDouble());
                    dst.setCoordRef3f(src.getCoordRef3f());
                    dst.setCoordRef3d(src.getCoordRef3d());
                    dst.setColorRefFloat(src.getColorRefFloat());
                    dst.setColorRefByte(src.getColorRefByte());
                    if ((format & WITH_ALPHA) == 0) {
                        dst.setColorRef3f(src.getColorRef3f());
                        dst.setColorRef3b(src.getColorRef3b());
                    } else {
                        dst.setColorRef4f(src.getColorRef4f());
                        dst.setColorRef4b(src.getColorRef4b());
                    }
                    dst.setNormalRefFloat(src.getNormalRefFloat());
                    dst.setNormalRef3f(src.getNormalRef3f());

                    switch (src.getVertexAttrType()) {
                    case GeometryArrayRetained.AF:
                        for (int i=0; i < vAttrCount; i++) {
                            dst.setVertexAttrRefFloat(i, src.getVertexAttrRefFloat(i));
                        }
                        break;
                    }

                    switch (src.getTexCoordType()) {
                    case GeometryArrayRetained.TF:
                        for (int i=0; i < setCount; i++) {
                            dst.setTexCoordRefFloat(i, src.getTexCoordRefFloat(i));
                        }
                        break;
                    case GeometryArrayRetained.T2F:
                        for (int i=0; i < setCount; i++) {
                            dst.setTexCoordRef2f(i, src.getTexCoordRef2f(i));
                        }
                        break;
                    case GeometryArrayRetained.T3F:
                        for (int i=0; i < setCount; i++) {
                            dst.setTexCoordRef3f(i, src.getTexCoordRef3f(i));
                        }
                        break;
                    }
                } else {
                    // NIO buffer
                    dst.setCoordRefBuffer(src.getCoordRefBuffer());
                    dst.setColorRefBuffer(src.getColorRefBuffer());
                    dst.setNormalRefBuffer(src.getNormalRefBuffer());

                    switch (src.getVertexAttrType()) {
                    case GeometryArrayRetained.AF:
                        for (int i=0; i < vAttrCount; i++) {
                            dst.setVertexAttrRefBuffer(i, src.getVertexAttrRefBuffer(i));
                        }
                        break;
                    }

                    switch (src.getTexCoordType()) {
                    case GeometryArrayRetained.TF:
                        for (int i=0; i < setCount; i++) {
                            dst.setTexCoordRefBuffer(i, src.getTexCoordRefBuffer(i));
                        }
                        break;
                    }
                }
            } else {
                dst.setInterleavedVertices(src.getInterleavedVertices());
            }
        }
    }


    //------------------------------------------------------------------
    // By-copying methods
    //------------------------------------------------------------------

    /**
     * Sets the initial vertex index for this GeometryArray object.
     * This index specifies the first vertex within this geometry
     * array that is actually used in rendering or other operations
     * such as picking and collision.  This attribute is initialized
     * to 0.
     * This attribute is only used when the data mode for this
     * geometry array object is not <code>BY_REFERENCE</code>
     * or when the data mode is <code>INTERLEAVED</code>.
     *
     * @param initialVertexIndex the new initial vertex index.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalArgumentException if either of the following are
     * true:
     * <ul>
     * <code>initialVertexIndex &lt; 0</code> or<br>
     * <code>initialVertexIndex + validVertexCount &gt; vertexCount</code><br>
     * </ul>
     *
     * @exception ArrayIndexOutOfBoundsException if the geometry data format
     * is <code>INTERLEAVED</code>, the InterleavedVertices array is
     * non-null, and:
     * <ul>
     * <code>InterleavedVertices.length</code> &lt; <i>num_words</i> *
     * (<code>initialVertexIndex + validVertexCount</code>)<br>
     * </ul>
     * where <i>num_words</i> depends on which vertex formats are enabled.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code> and is <i>not</i>
     * <code>INTERLEAVED</code>.
     *
     * @since Java 3D 1.2
     */
    public void setInitialVertexIndex(int initialVertexIndex) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COUNT_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray90"));

        if (initialVertexIndex < 0)
	    throw new IllegalArgumentException(J3dI18N.getString("GeometryArray97"));
	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0 && (format & INTERLEAVED) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray105"));

	((GeometryArrayRetained)this.retained).setInitialVertexIndex(initialVertexIndex);
	// NOTE: the check for initialVertexIndex + validVertexCount >
	// vertexCount is done in the retained method
    }


    /**
     * Gets the initial vertex index for this GeometryArray object.
     * This attribute is only used when the data mode for this
     * geometry array object is <i>not</i> <code>BY_REFERENCE</code>
     * or when the data mode is <code>INTERLEAVED</code>.
     * @return the current initial vertex index for this GeometryArray object.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public int getInitialVertexIndex() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COUNT_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray91"));

	return ((GeometryArrayRetained)this.retained).getInitialVertexIndex();
    }


  /**
   * Sets the coordinate associated with the vertex at
   * the specified index for this object.
   * @param index destination vertex index in this geometry array
   * @param coordinate source array of 3 values containing the new coordinate
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void setCoordinate(int index, float coordinate[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COORDINATE_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray3"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    ((GeometryArrayRetained)this.retained).setCoordinate(index, coordinate);
  }

  /**
   * Sets the coordinate associated with the vertex at
   * the specified index.
   * @param index destination vertex index in this geometry array
   * @param coordinate source array of 3 values containing the new coordinate
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void setCoordinate(int index, double coordinate[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COORDINATE_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray3"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    ((GeometryArrayRetained)this.retained).setCoordinate(index, coordinate);
  }

  /**
   * Sets the coordinate associated with the vertex at
   * the specified index for this object.
   * @param index destination vertex index in this geometry array
   * @param coordinate a point containing the new coordinate
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void setCoordinate(int index, Point3f coordinate) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COORDINATE_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray3"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    ((GeometryArrayRetained)this.retained).setCoordinate(index, coordinate);
  }

  /**
   * Sets the coordinate associated with the vertex at
   * the specified index for this object.
   * @param index destination vertex index in this geometry array
   * @param coordinate a point containing the new coordinate
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void setCoordinate(int index, Point3d coordinate) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COORDINATE_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray3"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    ((GeometryArrayRetained)this.retained).setCoordinate(index, coordinate);
  }

  /**
   * Sets the coordinates associated with the vertices starting at
   * the specified index for this object.  The entire source array is
   * copied to this geometry array.
   * @param index starting destination vertex index in this geometry array
   * @param coordinates source array of 3*n values containing n new coordinates
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void setCoordinates(int index, float coordinates[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COORDINATE_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray7"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    ((GeometryArrayRetained)this.retained).setCoordinates(index, coordinates);
  }

  /**
   * Sets the coordinates associated with the vertices starting at
   * the specified index for this object.  The entire source array is
   * copied to this geometry array.
   * @param index starting destination vertex index in this geometry array
   * @param coordinates source array of 3*n values containing n new coordinates
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void setCoordinates(int index, double coordinates[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COORDINATE_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray7"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    ((GeometryArrayRetained)this.retained).setCoordinates(index, coordinates);
  }

  /**
   * Sets the coordinates associated with the vertices starting at
   * the specified index for this object.  The entire source array is
   * copied to this geometry array.
   * @param index starting destination vertex index in this geometry array
   * @param coordinates source array of points containing new coordinates
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void setCoordinates(int index, Point3f coordinates[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COORDINATE_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray7"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    ((GeometryArrayRetained)this.retained).setCoordinates(index, coordinates);
  }

  /**
   * Sets the coordinates associated with the vertices starting at
   * the specified index for this object.  The entire source array is
   * copied to this geometry array.
   * @param index starting destination vertex index in this geometry array
   * @param coordinates source array of points containing new coordinates
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void setCoordinates(int index, Point3d coordinates[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COORDINATE_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray7"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    ((GeometryArrayRetained)this.retained).setCoordinates(index, coordinates);
  }

  /**
   * Sets the coordinates associated with the vertices starting at
   * the specified index for this object using coordinate data starting
   * from vertex index <code>start</code> for <code>length</code> vertices.
   * @param index starting destination vertex index in this geometry array
   * @param coordinates source array of 3*n values containing n new coordinates
   * @param start starting source vertex index in <code>coordinates</code> array.
   * @param length number of vertices to be copied.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void setCoordinates(int index, float coordinates[],
				   int start, int length) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COORDINATE_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray7"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    ((GeometryArrayRetained)this.retained).setCoordinates(index, coordinates,
                                                start, length);
  }

  /**
   * Sets the coordinates associated with the vertices starting at
   * the specified index for this object  using coordinate data starting
   * from vertex index <code>start</code> for <code>length</code> vertices.
   * @param index starting destination vertex index in this geometry array
   * @param coordinates source array of 3*n values containing n new coordinates
   * @param start starting source vertex index in <code>coordinates</code> array.
   * @param length number of vertices to be copied.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void setCoordinates(int index, double coordinates[],
				   int start, int length) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COORDINATE_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray7"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    ((GeometryArrayRetained)this.retained).setCoordinates(index, coordinates,
                                                start, length);
  }

  /**
   * Sets the coordinates associated with the vertices starting at
   * the specified index for this object using coordinate data starting
   * from vertex index <code>start</code> for <code>length</code> vertices.
   * @param index starting destination vertex index in this geometry array
   * @param coordinates source array of points containing new coordinates
   * @param start starting source vertex index in <code>coordinates</code> array.
   * @param length number of vertices to be copied.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void setCoordinates(int index, Point3f coordinates[],
				   int start, int length) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COORDINATE_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray7"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    ((GeometryArrayRetained)this.retained).setCoordinates(index, coordinates,
                                                start, length);
  }

  /**
   * Sets the coordinates associated with the vertices starting at
   * the specified index for this object using coordinate data starting
   * from vertex index <code>start</code> for <code>length</code> vertices.
   * @param index starting destination vertex index in this geometry array
   * @param coordinates source array of points containing new coordinates
   * @param start starting source vertex index in <code>coordinates</code> array.
   * @param length number of vertices to be copied.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void setCoordinates(int index, Point3d coordinates[],
				   int start, int length) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COORDINATE_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray7"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    ((GeometryArrayRetained)this.retained).setCoordinates(index, coordinates,
                                                start, length);
  }

  /**
   * Sets the color associated with the vertex at
   * the specified index for this object.
   * @param index destination vertex index in this geometry array
   * @param color source array of 3 or 4 values containing the new color
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception ArrayIndexOutOfBoundsException if COLOR bit NOT set in
   * constructor <code>vertexFormat</code> or array index for element is out of bounds.
   * @exception IllegalStateException if the data mode for this geometry
   * array object is <code>BY_REFERENCE</code>.
   */
  public void setColor(int index, float color[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray15"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    ((GeometryArrayRetained)this.retained).setColor(index, color);
  }

  /**
   * Sets the color associated with the vertex at
   * the specified index for this object.
   * @param index destination vertex index in this geometry array
   * @param color source array of 3 or 4 values containing the new color
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception ArrayIndexOutOfBoundsException if COLOR bit NOT set in
   * constructor <code>vertexFormat</code> or array index for element is out of bounds.
   * @exception IllegalStateException if the data mode for this geometry
   * array object is <code>BY_REFERENCE</code>.
   */
  public void setColor(int index, byte color[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray15"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    ((GeometryArrayRetained)this.retained).setColor(index, color);
  }

  /**
   * Sets the color associated with the vertex at
   * the specified index for this object.
   * @param index destination vertex index in this geometry array
   * @param color a Color3f containing the new color
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception ArrayIndexOutOfBoundsException if COLOR bit NOT set in
   * constructor <code>vertexFormat</code> or array index for element is out of bounds.
   * @exception IllegalStateException if the data mode for this geometry
   * array object is <code>BY_REFERENCE</code>.
   * @exception IllegalStateException if COLOR_4 is specified in the vertex 
   * format
   */
  public void setColor(int index, Color3f color) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray15"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    if ((format & WITH_ALPHA) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray92"));
    
    ((GeometryArrayRetained)this.retained).setColor(index, color);
  }

  /**
   * Sets the color associated with the vertex at
   * the specified index for this object.
   * @param index destination vertex index in this geometry array
   * @param color a Color4f containing the new color
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception ArrayIndexOutOfBoundsException if COLOR bit NOT set in
   * constructor <code>vertexFormat</code> or array index for element is out of bounds.
   * @exception IllegalStateException if the data mode for this geometry
   * array object is <code>BY_REFERENCE</code>.
   * @exception IllegalStateException if COLOR_3 is specified in the vertex 
   * format
   */
  public void setColor(int index, Color4f color) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray15"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    if ((format & WITH_ALPHA) == 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray93"));

    ((GeometryArrayRetained)this.retained).setColor(index, color);
  }

  /**
   * Sets the color associated with the vertex at
   * the specified index for this object.
   * @param index destination vertex index in this geometry array
   * @param color a Color3b containing the new color
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception ArrayIndexOutOfBoundsException if COLOR bit NOT set in
   * constructor <code>vertexFormat</code> or array index for element is out of bounds.
   * @exception IllegalStateException if the data mode for this geometry
   * array object is <code>BY_REFERENCE</code>.
   * @exception IllegalStateException if COLOR_4 is specified in the vertex 
   * format
   */
  public void setColor(int index, Color3b color) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray15"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    if ((format & WITH_ALPHA) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray92"));

    ((GeometryArrayRetained)this.retained).setColor(index, color);
  }

  /**
   * Sets the color associated with the vertex at
   * the specified index for this object.
   * @param index destination vertex index in this geometry array
   * @param color a Color4b containing the new color
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception ArrayIndexOutOfBoundsException if COLOR bit NOT set in
   * constructor <code>vertexFormat</code> or array index for element is out of bounds.
   * @exception IllegalStateException if the data mode for this geometry
   * array object is <code>BY_REFERENCE</code>.
   * @exception IllegalStateException if COLOR_3 is specified in the vertex 
   * format
   */
  public void setColor(int index, Color4b color) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray15"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    if ((format & WITH_ALPHA) == 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray93"));

    ((GeometryArrayRetained)this.retained).setColor(index, color);
  }

  /**
   * Sets the colors associated with the vertices starting at
   * the specified index for this object.  The entire source array is
   * copied to this geometry array.
   * @param index starting destination vertex index in this geometry array
   * @param colors source array of 3*n or 4*n values containing n new colors
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception ArrayIndexOutOfBoundsException if COLOR bit NOT set in
   * constructor <code>vertexFormat</code> or array index for element is out of bounds.
   * @exception IllegalStateException if the data mode for this geometry
   * array object is <code>BY_REFERENCE</code>.
   */
  public void setColors(int index, float colors[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray21"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    ((GeometryArrayRetained)this.retained).setColors(index, colors);
  }

  /**
   * Sets the colors associated with the vertices starting at
   * the specified index for this object.  The entire source array is
   * copied to this geometry array.
   * @param index starting destination vertex index in this geometry array
   * @param colors source array of 3*n or 4*n values containing n new colors
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception ArrayIndexOutOfBoundsException if COLOR bit NOT set in
   * constructor <code>vertexFormat</code> or array index for element is out of bounds.
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void setColors(int index, byte colors[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray21"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    ((GeometryArrayRetained)this.retained).setColors(index, colors);
  }

  /**
   * Sets the colors associated with the vertices starting at
   * the specified index for this object.  The entire source array is
   * copied to this geometry array.
   * @param index starting destination vertex index in this geometry array
   * @param colors source array of Color3f objects containing new colors
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception ArrayIndexOutOfBoundsException if COLOR bit NOT set in
   * constructor <code>vertexFormat</code> or array index for element is out of bounds.
   * @exception IllegalStateException if the data mode for this geometry
   * array object is <code>BY_REFERENCE</code>.
   * @exception IllegalStateException if COLOR_4 is specified in vertex format
   */
  public void setColors(int index, Color3f colors[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray21"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    if ((format & WITH_ALPHA) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray92"));

    ((GeometryArrayRetained)this.retained).setColors(index, colors);
  }

  /**
   * Sets the colors associated with the vertices starting at
   * the specified index for this object.  The entire source array is
   * copied to this geometry array.
   * @param index starting destination vertex index in this geometry array
   * @param colors source array of Color4f objects containing new colors
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception ArrayIndexOutOfBoundsException if COLOR bit NOT set in
   * constructor <code>vertexFormat</code> or array index for element is out of bounds.
   * @exception IllegalStateException if the data mode for this geometry
   * array object is <code>BY_REFERENCE</code>.
   * @exception IllegalStateException if COLOR_3 is specified in vertex format
   */
  public void setColors(int index, Color4f colors[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray21"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    if ((format & WITH_ALPHA) == 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray93"));

    ((GeometryArrayRetained)this.retained).setColors(index, colors);
  }

  /**
   * Sets the colors associated with the vertices starting at
   * the specified index for this object.  The entire source array is
   * copied to this geometry array.
   * @param index starting destination vertex index in this geometry array
   * @param colors source array of Color3b objects containing new colors
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception ArrayIndexOutOfBoundsException if COLOR bit NOT set in
   * constructor <code>vertexFormat</code> or array index for element is out of bounds.
   * @exception IllegalStateException if the data mode for this geometry
   * array object is <code>BY_REFERENCE</code>.
   * @exception IllegalStateException if COLOR_4 is specified in vertex format
   */
  public void setColors(int index, Color3b colors[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray21"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    if ((format & WITH_ALPHA) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray92"));

    ((GeometryArrayRetained)this.retained).setColors(index, colors);
  }

  /**
   * Sets the colors associated with the vertices starting at
   * the specified index for this object.  The entire source array is
   * copied to this geometry array.
   * @param index starting destination vertex index in this geometry array
   * @param colors source array of Color4b objects containing new colors
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception ArrayIndexOutOfBoundsException if COLOR bit NOT set in
   * constructor <code>vertexFormat</code> or array index for element is out of bounds.
   * @exception IllegalStateException if the data mode for this geometry
   * array object is <code>BY_REFERENCE</code>.
   * @exception IllegalStateException if COLOR_3 is specified in vertex format
   */
  public void setColors(int index, Color4b colors[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray21"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    if ((format & WITH_ALPHA) == 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray93"));

    ((GeometryArrayRetained)this.retained).setColors(index, colors);
  }

  /**
   * Sets the colors associated with the vertices starting at
   * the specified index for this object using data in <code>colors</code>
   * starting at index <code>start</code> for <code>length</code> colors.
   * @param index starting destination vertex index in this geometry array
   * @param colors source array of 3*n or 4*n values containing n new colors
   * @param start starting source vertex index in <code>colors</code> array.
   * @param length number of colors to be copied.
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception ArrayIndexOutOfBoundsException if COLOR bit NOT set in
   * constructor <code>vertexFormat</code> or array index for element is out of bounds.
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void setColors(int index, float colors[],
				   int start, int length) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray21"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    ((GeometryArrayRetained)this.retained).setColors(index, colors, start,
                                                length);
  }

  /**
   * Sets the colors associated with the vertices starting at
   * the specified index for this object using data in <code>colors</code>
   * starting at index <code>start</code> for <code>length</code> colors.
   * @param index starting destination vertex index in this geometry array
   * @param colors source array of 3*n or 4*n values containing n new colors
   * @param start starting source vertex index in <code>colors</code> array.
   * @param length number of colors to be copied.
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception ArrayIndexOutOfBoundsException if COLOR bit NOT set in
   * constructor <code>vertexFormat</code> or array index for element is out of bounds.
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void setColors(int index, byte colors[],
				   int start, int length) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray21"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    ((GeometryArrayRetained)this.retained).setColors(index, colors, start,
                                                length);
  }

  /**
   * Sets the colors associated with the vertices starting at
   * the specified index for this object using data in <code>colors</code>
   * starting at index <code>start</code> for <code>length</code> colors.
   * @param index starting destination vertex index in this geometry array
   * @param colors source array of Color3f objects containing new colors
   * @param start starting source vertex index in <code>colors</code> array.
   * @param length number of colors to be copied.
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception ArrayIndexOutOfBoundsException if COLOR bit NOT set in
   * constructor <code>vertexFormat</code> or array index for element is out of bounds.
   * @exception IllegalStateException if the data mode for this geometry
   * array object is <code>BY_REFERENCE</code>.
   * @exception IllegalStateException if COLOR_4 is specified in vertex format
   */
  public void setColors(int index, Color3f colors[],
				   int start, int length) {
   if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray21"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    if ((format & WITH_ALPHA) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray92"));

    ((GeometryArrayRetained)this.retained).setColors(index, colors, start,
                                                length);
  }

  /**
   * Sets the colors associated with the vertices starting at
   * the specified index for this object using data in <code>colors</code>
   * starting at index <code>start</code> for <code>length</code> colors.
   * @param index starting destination vertex index in this geometry array
   * @param colors source array of Color4f objects containing new colors
   * @param start starting source vertex index in <code>colors</code> array.
   * @param length number of colors to be copied.
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception ArrayIndexOutOfBoundsException if COLOR bit NOT set in
   * constructor <code>vertexFormat</code> or array index for element is out of bounds.
   * @exception IllegalStateException if the data mode for this geometry
   * array object is <code>BY_REFERENCE</code>.
   * @exception IllegalStateException if COLOR_3 is specified in vertex format
   */
  public void setColors(int index, Color4f colors[],
				   int start, int length) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray21"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    if ((format & WITH_ALPHA) == 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray93"));

    ((GeometryArrayRetained)this.retained).setColors(index, colors, start,
                                                length);
  }

  /**
   * Sets the colors associated with the vertices starting at
   * the specified index for this object using data in <code>colors</code>
   * starting at index <code>start</code> for <code>length</code> colors.
   * @param index starting destination vertex index in this geometry array
   * @param colors source array of Color3b objects containing new colors
   * @param start starting source vertex index in <code>colors</code> array.
   * @param length number of colors to be copied.
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception ArrayIndexOutOfBoundsException if COLOR bit NOT set in
   * constructor <code>vertexFormat</code> or array index for element is out of bounds.
   * @exception IllegalStateException if the data mode for this geometry
   * array object is <code>BY_REFERENCE</code>.
   * @exception IllegalStateException if COLOR_4 is specified in vertex format
   */
  public void setColors(int index, Color3b colors[],
				   int start, int length) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray21"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    if ((format & WITH_ALPHA) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray92"));

    ((GeometryArrayRetained)this.retained).setColors(index, colors, start,
                                                length);
  }

  /**
   * Sets the colors associated with the vertices starting at
   * the specified index for this object using data in <code>colors</code>
   * starting at index <code>start</code> for <code>length</code> colors.
   * @param index starting destination vertex index in this geometry array
   * @param colors source array of Color4b objects containing new colors
   * @param start starting source vertex index in <code>colors</code> array.
   * @param length number of colors to be copied.
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception ArrayIndexOutOfBoundsException if COLOR bit NOT set in
   * constructor <code>vertexFormat</code> or array index for element is out of bounds.
   * @exception IllegalStateException if the data mode for this geometry
   * array object is <code>BY_REFERENCE</code>.
   * @exception IllegalStateException if COLOR_3 is specified in vertex format
   */
  public void setColors(int index, Color4b colors[],
				   int start, int length) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray21"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    if ((format & WITH_ALPHA) == 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray93"));

    ((GeometryArrayRetained)this.retained).setColors(index, colors, start,
                                                length);
  }

  /**
   * Sets the normal associated with the vertex at
   * the specified index for this object.
   * @param index destination vertex index in this geometry array
   * @param normal source array of 3 values containing the new normal
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception ArrayIndexOutOfBoundsException if NORMALS bit NOT set in
   * constructor <code>vertexFormat</code> or array index for element is out of bounds.
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void setNormal(int index, float normal[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_NORMAL_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray33"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & NORMALS ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray77"));

    ((GeometryArrayRetained)this.retained).setNormal(index, normal);
  }

  /**
   * Sets the normal associated with the vertex at
   * the specified index for this object.
   * @param index destination vertex index in this geometry array
   * @param normal the vector containing the new normal
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception ArrayIndexOutOfBoundsException if NORMALS bit NOT set in
   * constructor <code>vertexFormat</code> or array index for element is out of bounds.
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void setNormal(int index, Vector3f normal) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_NORMAL_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray33"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & NORMALS ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray77"));

    ((GeometryArrayRetained)this.retained).setNormal(index, normal);
  }

  /**
   * Sets the normals associated with the vertices starting at
   * the specified index for this object.  The entire source array is
   * copied to this geometry array.
   * @param index starting destination vertex index in this geometry array
   * @param normals source array of 3*n values containing n new normals
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception ArrayIndexOutOfBoundsException if NORMALS bit NOT set in
   * constructor <code>vertexFormat</code> or array index for element is out of bounds.
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void setNormals(int index, float normals[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_NORMAL_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray35"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & NORMALS ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray77"));

    ((GeometryArrayRetained)this.retained).setNormals(index, normals);
  }

  /**
   * Sets the normals associated with the vertices starting at
   * the specified index for this object.  The entire source array is
   * copied to this geometry array.
   * @param index starting destination vertex index in this geometry array
   * @param normals source array of vectors containing new normals
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception ArrayIndexOutOfBoundsException if NORMALS bit NOT set in
   * constructor <code>vertexFormat</code> or array index for element is out of bounds.
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void setNormals(int index, Vector3f normals[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_NORMAL_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray35"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & NORMALS ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray77"));

    ((GeometryArrayRetained)this.retained).setNormals(index, normals);
  }

  /**
   * Sets the normals associated with the vertices starting at
   * the specified index for this object using data in <code>normals</code>
   * starting at index <code>start</code> and  ending at index <code>start+length</code>.
   * @param index starting destination vertex index in this geometry array
   * @param normals source array of 3*n values containing n new normals
   * @param start starting source vertex index in <code>normals</code> array.
   * @param length number of normals to be copied.
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception ArrayIndexOutOfBoundsException if NORMALS bit NOT set in
   * constructor <code>vertexFormat</code> or array index for element is out of bounds.
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void setNormals(int index, float normals[],
				   int start, int length) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_NORMAL_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray35"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & NORMALS ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray77"));

    ((GeometryArrayRetained)this.retained).setNormals(index, normals, start,                                                    length);
  }

  /**
   * Sets the normals associated with the vertices starting at
   * the specified index for this object using data in <code>normals</code>
   * starting at index <code>start</code> and  ending at index <code>start+length</code>.
   * @param index starting destination vertex index in this geometry array
   * @param normals source array of vectors containing new normals
   * @param start starting source vertex index in <code>normals</code> array.
   * @param length number of normals to be copied.
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception ArrayIndexOutOfBoundsException if NORMALS bit NOT set in
   * constructor <code>vertexFormat</code> or array index for element is out of bounds.
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void setNormals(int index, Vector3f normals[],
				   int start, int length) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_NORMAL_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray35"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & NORMALS ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray77"));

    ((GeometryArrayRetained)this.retained).setNormals(index, normals, start,                                                    length);
  }


    /**
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>setTextureCoordinate(int texCoordSet,  ...)</code>
     */
    public void setTextureCoordinate(int index, float texCoord[]) {
	setTextureCoordinate(0, index, texCoord);
    }

    /**
     * Sets the texture coordinate associated with the vertex at the
     * specified index in the specified texture coordinate set for
     * this object.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param index destination vertex index in this geometry array
     * @param texCoord source array of 2, 3 or 4 values containing the new
     * texture coordinate
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if the index or
     * texCoordSet is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @since Java 3D 1.2
     */
    public void setTextureCoordinate(int texCoordSet,
				     int index, float texCoord[]) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray39"));

	((GeometryArrayRetained)this.retained).setTextureCoordinates(
	   	texCoordSet, index, texCoord, 0, 1);
    }

    /**
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>setTextureCoordinate(int texCoordSet, TexCoord2f texCoord)</code>
     */
    public void setTextureCoordinate(int index, Point2f texCoord) {
	texCoord2fScratch.set(texCoord);
	setTextureCoordinate(0, index, texCoord2fScratch);
    }

    /**
     * Sets the texture coordinate associated with the vertex at
     * the specified index in the specified texture coordinate set
     * for this object.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param index destination vertex index in this geometry array
     * @param texCoord the TexCoord2f containing the new texture coordinate
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if the index or
     * texCoordSet is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if TEXTURE_COORDINATE_3 or
     * TEXTURE_COORDINATE_4 is specified in vertex format
     *
     * @since Java 3D 1.2
     */
    public void setTextureCoordinate(int texCoordSet,
				     int index, TexCoord2f texCoord) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray39"));

	if (((((GeometryArrayRetained)this.retained).vertexFormat) & 
		(TEXTURE_COORDINATE_3 | TEXTURE_COORDINATE_4)) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray94"));

	texCoord2fArray[0] = texCoord;
	((GeometryArrayRetained)this.retained).setTextureCoordinates(
		texCoordSet, index, texCoord2fArray, 0, 1);
    }

    /**
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>setTextureCoordinate(int texCoordSet, TexCoord3f texCoord)</code>
     */
    public void setTextureCoordinate(int index, Point3f texCoord) {
	texCoord3fScratch.set(texCoord);
	setTextureCoordinate(0, index, texCoord3fScratch);
    }

    /**
     * Sets the texture coordinate associated with the vertex at
     * the specified index in the specified texture coordinate set
     * for this object.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param index destination vertex index in this geometry array
     * @param texCoord the TexCoord3f containing the new texture coordinate
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if the index or
     * texCoordSet is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if TEXTURE_COORDINATE_2 or
     * TEXTURE_COORDINATE_4 is specified in vertex format
     *
     * @since Java 3D 1.2
     */
    public void setTextureCoordinate(int texCoordSet,
				     int index, TexCoord3f texCoord) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray39"));

	if (((((GeometryArrayRetained)this.retained).vertexFormat) & 
		(TEXTURE_COORDINATE_2 | TEXTURE_COORDINATE_4)) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray95"));

	texCoord3fArray[0] = texCoord;
	((GeometryArrayRetained)this.retained).setTextureCoordinates(
		texCoordSet, index, texCoord3fArray, 0, 1);
    }

    /**
     * Sets the texture coordinate associated with the vertex at
     * the specified index in the specified texture coordinate set
     * for this object.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param index destination vertex index in this geometry array
     * @param texCoord the TexCoord4f containing the new texture coordinate
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if the index or
     * texCoordSet is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if TEXTURE_COORDINATE_2 or
     * TEXTURE_COORDINATE_3 is specified in vertex format
     *
     * @since Java 3D 1.3
     */
    public void setTextureCoordinate(int texCoordSet,
				     int index, TexCoord4f texCoord) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray39"));

	if (((((GeometryArrayRetained)this.retained).vertexFormat) & 
		(TEXTURE_COORDINATE_2 | TEXTURE_COORDINATE_3)) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray109"));

	texCoord4fArray[0] = texCoord;
	((GeometryArrayRetained)this.retained).setTextureCoordinates(
		texCoordSet, index, texCoord4fArray, 0, 1);
    }

    /**
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>setTextureCoordinates(int texCoordSet, ...)</code>
     */
    public void setTextureCoordinates(int index, float texCoords[]) {
	setTextureCoordinates(0, index, texCoords);
    }

    /**
     * Sets the texture coordinates associated with the vertices starting at
     * the specified index in the specified texture coordinate set
     * for this object.  The entire source array is
     * copied to this geometry array.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param index starting destination vertex index in this geometry array
     * @param texCoords source array of 2*n, 3*n or 4*n values containing n new
     * texture coordinates
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if the index or
     * texCoordSet is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @since Java 3D 1.2
     */
    public void setTextureCoordinates(int texCoordSet,
				      int index, float texCoords[]) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray42"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & GeometryArray.TEXTURE_COORDINATE_2) != 0)
	    ((GeometryArrayRetained)this.retained).setTextureCoordinates(
		texCoordSet, index, texCoords, 0, texCoords.length / 2);
	else if ((format & GeometryArray.TEXTURE_COORDINATE_3) != 0)
	    ((GeometryArrayRetained)this.retained).setTextureCoordinates(
		texCoordSet, index, texCoords, 0, texCoords.length / 3);
	else 
	    ((GeometryArrayRetained)this.retained).setTextureCoordinates(
		texCoordSet, index, texCoords, 0, texCoords.length / 4);
    }

    /**
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>setTextureCoordinates(int texCoordSet, TexCoord2f texCoords[])</code>
     */
    public void setTextureCoordinates(int index, Point2f texCoords[]) {

	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray42"));

	((GeometryArrayRetained)this.retained).setTextureCoordinates(
		0, index, texCoords, 0, texCoords.length);
    }

    /**
     * Sets the texture coordinates associated with the vertices starting at
     * the specified index in the specified texture coordinate set
     * for this object.  The entire source array is
     * copied to this geometry array.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param index starting destination vertex index in this geometry array
     * @param texCoords source array of TexCoord2f objects containing new
     * texture coordinates
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if the index or
     * texCoordSet is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if TEXTURE_COORDINATE_3 or
     * TEXTURE_COORDINATE_4 is specified in vertex format
     *
     * @since Java 3D 1.2
     */
    public void setTextureCoordinates(int texCoordSet,
				      int index, TexCoord2f texCoords[]) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray42"));

	if (((((GeometryArrayRetained)this.retained).vertexFormat) & 
		(TEXTURE_COORDINATE_3 | TEXTURE_COORDINATE_4)) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray94"));

	((GeometryArrayRetained)this.retained).setTextureCoordinates(
		texCoordSet, index, texCoords, 0, texCoords.length);
    }

    /**
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>setTextureCoordinates(int texCoordSet, TexCoord3f texCoords[])</code>
     */
    public void setTextureCoordinates(int index, Point3f texCoords[]) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray42"));

	((GeometryArrayRetained)this.retained).setTextureCoordinates(
		0, index, texCoords, 0, texCoords.length);
    }

    /**
     * Sets the texture coordinates associated with the vertices starting at
     * the specified index in the specified texture coordinate set
     * for this object.  The entire source array is
     * copied to this geometry array.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param index starting destination vertex index in this geometry array
     * @param texCoords source array of TexCoord3f objects containing new
     * texture coordinates
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if the index or
     * texCoordSet is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if TEXTURE_COORDINATE_2 or
     * TEXTURE_COORDINATE_4 is specified in vertex format
     *
     * @since Java 3D 1.2
     */
    public void setTextureCoordinates(int texCoordSet,
				      int index, TexCoord3f texCoords[]) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray42"));


	if (((((GeometryArrayRetained)this.retained).vertexFormat) & 
		(TEXTURE_COORDINATE_2 | TEXTURE_COORDINATE_4)) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray95"));

	((GeometryArrayRetained)this.retained).setTextureCoordinates(
		texCoordSet, index, texCoords, 0, texCoords.length);
    }

    /**
     * Sets the texture coordinates associated with the vertices starting at
     * the specified index in the specified texture coordinate set
     * for this object.  The entire source array is
     * copied to this geometry array.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param index starting destination vertex index in this geometry array
     * @param texCoords source array of TexCoord4f objects containing new
     * texture coordinates
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if the index or
     * texCoordSet is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if TEXTURE_COORDINATE_2 or
     * TEXTURE_COORDINATE_3 is specified in vertex format
     *
     * @since Java 3D 1.3
     */
    public void setTextureCoordinates(int texCoordSet,
				      int index, TexCoord4f texCoords[]) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray42"));


	if (((((GeometryArrayRetained)this.retained).vertexFormat) & 
		(TEXTURE_COORDINATE_2 | TEXTURE_COORDINATE_3)) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray109"));

	((GeometryArrayRetained)this.retained).setTextureCoordinates(
		texCoordSet, index, texCoords, 0, texCoords.length);
    }

    /**
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>setTextureCoordinates(int texCoordSet, ...)</code>
     */
    public void setTextureCoordinates(int index, float texCoords[],
				      int start, int length) {
	setTextureCoordinates(0, index, texCoords, start, length);
    }

    /**
     * Sets the texture coordinates associated with the vertices
     * starting at the specified index in the specified texture
     * coordinate set for this object using data in
     * <code>texCoords</code> starting at index <code>start</code> and
     * ending at index <code>start+length</code>.
     *
     * @param index starting destination vertex index in this geometry array
     * @param texCoords source array of 2*n , 3*n or 4*n values containing 
     * n new texture coordinates
     * @param start starting source vertex index in <code>texCoords</code>
     * array.
     * @param length number of texture Coordinates to be copied.
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if the index or
     * texCoordSet is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @since Java 3D 1.2
     */
    public void setTextureCoordinates(int texCoordSet,
				      int index, float texCoords[],
				      int start, int length) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray42"));

	((GeometryArrayRetained)this.retained).setTextureCoordinates(
		texCoordSet, index, texCoords, start, length);
    }

    /**
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>setTextureCoordinates(int texCoordSet, TexCoord2f texCoords[], ...)</code>
     */
    public void setTextureCoordinates(int index, Point2f texCoords[],
				      int start, int length) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray42"));

	((GeometryArrayRetained)this.retained).setTextureCoordinates(
		0, index, texCoords, start, length);
    }

    /**
     * Sets the texture coordinates associated with the vertices
     * starting at the specified index in the specified texture
     * coordinate set for this object using data in
     * <code>texCoords</code> starting at index <code>start</code> and
     * ending at index <code>start+length</code>.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param index starting destination vertex index in this geometry array
     * @param texCoords source array of TexCoord2f objects containing new
     * texture coordinates
     * @param start starting source vertex index in <code>texCoords</code>
     * array.
     * @param length number of texture Coordinates to be copied.
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if the index or
     * texCoordSet is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if TEXTURE_COORDINATE_3 or 
     * TEXTURE_COORDINATE_4 is specified in vertex format
     *
     * @since Java 3D 1.2
     */
    public void setTextureCoordinates(int texCoordSet,
				      int index, TexCoord2f texCoords[],
				      int start, int length) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray42"));

	if (((((GeometryArrayRetained)this.retained).vertexFormat) & 
		(TEXTURE_COORDINATE_3 | TEXTURE_COORDINATE_4)) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray94"));

	((GeometryArrayRetained)this.retained).setTextureCoordinates(
		texCoordSet, index, texCoords, start, length);
    }

    /**
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>setTextureCoordinates(int texCoordSet, TexCoord3f texCoords[], ...)</code>
     */
    public void setTextureCoordinates(int index, Point3f texCoords[],
				      int start, int length) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray42"));

	((GeometryArrayRetained)this.retained).setTextureCoordinates(
		0, index, texCoords, start, length);
    }

    /**
     * Sets the texture coordinates associated with the vertices
     * starting at the specified index in the specified texture
     * coordinate set for this object.  starting at index
     * <code>start</code> and ending at index <code>start+length</code>.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param index starting destination vertex index in this geometry array
     * @param texCoords source array of TexCoord3f objects containing new
     * texture coordinates
     * @param start starting source vertex index in <code>texCoords</code>
     * array.
     * @param length number of texture Coordinates to be copied.
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if the index or
     * texCoordSet is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if TEXTURE_COORDINATE_2 or
     * TEXTURE_COORDINATE_4 is specified in vertex format
     *
     * @since Java 3D 1.2
     */
    public void setTextureCoordinates(int texCoordSet,
				      int index, TexCoord3f texCoords[],
				      int start, int length) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray42"));

	if (((((GeometryArrayRetained)this.retained).vertexFormat) & 
		(TEXTURE_COORDINATE_2 | TEXTURE_COORDINATE_4)) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray95"));

	((GeometryArrayRetained)this.retained).setTextureCoordinates(
		texCoordSet, index, texCoords, start, length);
    }

    /**
     * Sets the texture coordinates associated with the vertices
     * starting at the specified index in the specified texture
     * coordinate set for this object.  starting at index
     * <code>start</code> and ending at index <code>start+length</code>.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param index starting destination vertex index in this geometry array
     * @param texCoords source array of TexCoord4f objects containing new
     * texture coordinates
     * @param start starting source vertex index in <code>texCoords</code>
     * array.
     * @param length number of texture Coordinates to be copied.
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if the index or
     * texCoordSet is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if TEXTURE_COORDINATE_2 or
     * TEXTURE_COORDINATE_3 is specified in vertex format
     *
     * @since Java 3D 1.3
     */
    public void setTextureCoordinates(int texCoordSet,
				      int index, TexCoord4f texCoords[],
				      int start, int length) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray42"));

	if (((((GeometryArrayRetained)this.retained).vertexFormat) & 
		(TEXTURE_COORDINATE_2 | TEXTURE_COORDINATE_3)) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray109"));

	((GeometryArrayRetained)this.retained).setTextureCoordinates(
		texCoordSet, index, texCoords, start, length);
    }


    /**
     * Sets the vertex attribute associated with the vertex at the
     * specified index in the specified vertex attribute number for
     * this object.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index destination vertex index in this geometry array
     * @param vertexAttr source array of 1, 2, 3 or 4 values containing
     * the new vertex attribute
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if the index or
     * vertexAttrNum is out of range, or if the vertexAttr array is
     * too small.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @since Java 3D 1.4
     */
    public void setVertexAttr(int vertexAttrNum, int index,
			      float[] vertexAttr) {

	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_VERTEX_ATTR_WRITE)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray126"));
	    }
	}

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));
	}

	((GeometryArrayRetained)this.retained).setVertexAttrs(
		vertexAttrNum, index, vertexAttr, 0, 1);
    }

    /**
     * Sets the vertex attribute associated with the vertex at the
     * specified index in the specified vertex attribute number for
     * this object.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index destination vertex index in this geometry array
     * @param vertexAttr the Point2f containing the new vertex attribute
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if the index or
     * vertexAttrNum is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if the size of the specified
     * vertex attribute number is not 2.
     *
     * @since Java 3D 1.4
     */
    public void setVertexAttr(int vertexAttrNum, int index,
			      Point2f vertexAttr) {

	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_VERTEX_ATTR_WRITE)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray126"));
	    }
	}

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));
	}

	int size = ((GeometryArrayRetained)this.retained).vertexAttrSizes[vertexAttrNum];
	if (size != 2) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray134"));
	}

	((GeometryArrayRetained)this.retained).setVertexAttr(
		vertexAttrNum, index, vertexAttr);
    }

    /**
     * Sets the vertex attribute associated with the vertex at the
     * specified index in the specified vertex attribute number for
     * this object.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index destination vertex index in this geometry array
     * @param vertexAttr the Point3f containing the new vertex attribute
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if the index or
     * vertexAttrNum is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if the size of the specified
     * vertex attribute number is not 3.
     *
     * @since Java 3D 1.4
     */
    public void setVertexAttr(int vertexAttrNum, int index,
			      Point3f vertexAttr) {

	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_VERTEX_ATTR_WRITE)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray126"));
	    }
	}

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));
	}

	int size = ((GeometryArrayRetained)this.retained).vertexAttrSizes[vertexAttrNum];
	if (size != 3) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray134"));
	}

	((GeometryArrayRetained)this.retained).setVertexAttr(
		vertexAttrNum, index, vertexAttr);
    }

    /**
     * Sets the vertex attribute associated with the vertex at the
     * specified index in the specified vertex attribute number for
     * this object.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index destination vertex index in this geometry array
     * @param vertexAttr the Point4f containing the new vertex attribute
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if the index or
     * vertexAttrNum is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if the size of the specified
     * vertex attribute number is not 4.
     *
     * @since Java 3D 1.4
     */
    public void setVertexAttr(int vertexAttrNum, int index,
			      Point4f vertexAttr) {

	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_VERTEX_ATTR_WRITE)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray126"));
	    }
	}

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));
	}

	int size = ((GeometryArrayRetained)this.retained).vertexAttrSizes[vertexAttrNum];
	if (size != 4) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray134"));
	}

	((GeometryArrayRetained)this.retained).setVertexAttr(
		vertexAttrNum, index, vertexAttr);
    }

    /**
     * Sets the vertex attributes associated with the vertices starting at
     * the specified index in the specified vertex attribute number
     * for this object.  The entire source array is copied to this
     * geometry array.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index starting destination vertex index in this geometry array
     * @param vertexAttrs source array of 1*n, 2*n, 3*n, or 4*n values
     * containing n new vertex attributes
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if the index or
     * vertexAttrNum is out of range, or if the vertexAttr array is
     * too large.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @since Java 3D 1.4
     */
    public void setVertexAttrs(int vertexAttrNum, int index,
			       float[] vertexAttrs) {
	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_VERTEX_ATTR_WRITE)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray126"));
	    }
	}

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));
	}

	int size = ((GeometryArrayRetained)this.retained).vertexAttrSizes[vertexAttrNum];
	((GeometryArrayRetained)this.retained).setVertexAttrs(
		vertexAttrNum, index, vertexAttrs, 0, vertexAttrs.length / size);
    }

    /**
     * Sets the vertex attributes associated with the vertices starting at
     * the specified index in the specified vertex attribute number
     * for this object.  The entire source array is copied to this
     * geometry array.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index starting destination vertex index in this geometry array
     * @param vertexAttrs source array of Point2f objects containing new
     * vertex attributes
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if the index or
     * vertexAttrNum is out of range, or if the vertexAttr array is
     * too large.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if the size of the specified
     * vertex attribute number is not 2.
     *
     * @since Java 3D 1.4
     */
    public void setVertexAttrs(int vertexAttrNum, int index,
			       Point2f[] vertexAttrs) {

	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_VERTEX_ATTR_WRITE)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray126"));
	    }
	}

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));
	}

	int size = ((GeometryArrayRetained)this.retained).vertexAttrSizes[vertexAttrNum];
	if (size != 2) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray134"));
	}

	((GeometryArrayRetained)this.retained).setVertexAttrs(
		vertexAttrNum, index, vertexAttrs, 0, vertexAttrs.length);
    }

    /**
     * Sets the vertex attributes associated with the vertices starting at
     * the specified index in the specified vertex attribute number
     * for this object.  The entire source array is copied to this
     * geometry array.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index starting destination vertex index in this geometry array
     * @param vertexAttrs source array of Point3f objects containing new
     * vertex attributes
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if the index or
     * vertexAttrNum is out of range, or if the vertexAttr array is
     * too large.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if the size of the specified
     * vertex attribute number is not 3.
     *
     * @since Java 3D 1.4
     */
    public void setVertexAttrs(int vertexAttrNum, int index,
			       Point3f[] vertexAttrs) {

	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_VERTEX_ATTR_WRITE)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray126"));
	    }
	}

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));
	}

	int size = ((GeometryArrayRetained)this.retained).vertexAttrSizes[vertexAttrNum];
	if (size != 3) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray134"));
	}

	((GeometryArrayRetained)this.retained).setVertexAttrs(
		vertexAttrNum, index, vertexAttrs, 0, vertexAttrs.length);
    }

    /**
     * Sets the vertex attributes associated with the vertices starting at
     * the specified index in the specified vertex attribute number
     * for this object.  The entire source array is copied to this
     * geometry array.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index starting destination vertex index in this geometry array
     * @param vertexAttrs source array of Point4f objects containing new
     * vertex attributes
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if the index or
     * vertexAttrNum is out of range, or if the vertexAttr array is
     * too large.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if the size of the specified
     * vertex attribute number is not 4.
     *
     * @since Java 3D 1.4
     */
    public void setVertexAttrs(int vertexAttrNum, int index,
			       Point4f[] vertexAttrs) {

	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_VERTEX_ATTR_WRITE)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray126"));
	    }
	}

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));
	}

	int size = ((GeometryArrayRetained)this.retained).vertexAttrSizes[vertexAttrNum];
	if (size != 4) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray134"));
	}

	((GeometryArrayRetained)this.retained).setVertexAttrs(
		vertexAttrNum, index, vertexAttrs, 0, vertexAttrs.length);
    }

    /**
     * Sets the vertex attributes associated with the vertices
     * starting at the specified index in the specified vertex
     * attribute number for this object using data in
     * <code>vertexAttrs</code> starting at index <code>start</code> and
     * ending at index <code>start+length</code>.
     *
     * @param index starting destination vertex index in this geometry array
     * @param vertexAttrs source array of 1*n, 2*n, 3*n, or 4*n values
     * containing n new vertex attributes
     * @param start starting source vertex index in <code>vertexAttrs</code>
     * array.
     * @param length number of vertex attributes to be copied.
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if any of index,
     * (index+length), or vertexAttrNum are out of range, or if
     * vertexAttrs is too small.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @since Java 3D 1.4
     */
    public void setVertexAttrs(int vertexAttrNum, int index,
			       float[] vertexAttrs,
			       int start, int length) {

	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_VERTEX_ATTR_WRITE)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray126"));
	    }
	}

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));
	}

	((GeometryArrayRetained)this.retained).setVertexAttrs(
		vertexAttrNum, index, vertexAttrs, start, length);
    }

    /**
     * Sets the vertex attributes associated with the vertices
     * starting at the specified index in the specified vertex
     * attribute number for this object using data in
     * <code>vertexAttrs</code> starting at index <code>start</code> and
     * ending at index <code>start+length</code>.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index starting destination vertex index in this geometry array
     * @param vertexAttrs source array of Point2f objects containing new
     * vertex attributes
     * @param start starting source vertex index in <code>vertexAttrs</code>
     * array.
     * @param length number of vertex attributes to be copied.
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if any of index,
     * (index+length), or vertexAttrNum are out of range, or if
     * vertexAttrs is too small.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if the size of the specified
     * vertex attribute number is not 2.
     *
     * @since Java 3D 1.4
     */
    public void setVertexAttrs(int vertexAttrNum, int index,
			       Point2f[] vertexAttrs,
			       int start, int length) {

	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_VERTEX_ATTR_WRITE)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray126"));
	    }
	}

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));
	}

	int size = ((GeometryArrayRetained)this.retained).vertexAttrSizes[vertexAttrNum];
	if (size != 2) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray134"));
	}

	((GeometryArrayRetained)this.retained).setVertexAttrs(
		vertexAttrNum, index, vertexAttrs, start, length);
    }

    /**
     * Sets the vertex attributes associated with the vertices
     * starting at the specified index in the specified vertex
     * attribute number for this object using data in
     * <code>vertexAttrs</code> starting at index <code>start</code> and
     * ending at index <code>start+length</code>.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index starting destination vertex index in this geometry array
     * @param vertexAttrs source array of Point3f objects containing new
     * vertex attributes
     * @param start starting source vertex index in <code>vertexAttrs</code>
     * array.
     * @param length number of vertex attributes to be copied.
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if any of index,
     * (index+length), or vertexAttrNum are out of range, or if
     * vertexAttrs is too small.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if the size of the specified
     * vertex attribute number is not 3.
     *
     * @since Java 3D 1.4
     */
    public void setVertexAttrs(int vertexAttrNum, int index,
			       Point3f[] vertexAttrs,
			       int start, int length) {

	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_VERTEX_ATTR_WRITE)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray126"));
	    }
	}

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));
	}

	int size = ((GeometryArrayRetained)this.retained).vertexAttrSizes[vertexAttrNum];
	if (size != 3) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray134"));
	}

	((GeometryArrayRetained)this.retained).setVertexAttrs(
		vertexAttrNum, index, vertexAttrs, start, length);
    }

    /**
     * Sets the vertex attributes associated with the vertices
     * starting at the specified index in the specified vertex
     * attribute number for this object using data in
     * <code>vertexAttrs</code> starting at index <code>start</code> and
     * ending at index <code>start+length</code>.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index starting destination vertex index in this geometry array
     * @param vertexAttrs source array of Point4f objects containing new
     * vertex attributes
     * @param start starting source vertex index in <code>vertexAttrs</code>
     * array.
     * @param length number of vertex attributes to be copied.
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if any of index,
     * (index+length), or vertexAttrNum are out of range, or if
     * vertexAttrs is too small.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if the size of the specified
     * vertex attribute number is not 4.
     *
     * @since Java 3D 1.4
     */
    public void setVertexAttrs(int vertexAttrNum, int index,
			       Point4f[] vertexAttrs,
			       int start, int length) {

	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_VERTEX_ATTR_WRITE)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray126"));
	    }
	}

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));
	}

	int size = ((GeometryArrayRetained)this.retained).vertexAttrSizes[vertexAttrNum];
	if (size != 4) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray134"));
	}

	((GeometryArrayRetained)this.retained).setVertexAttrs(
		vertexAttrNum, index, vertexAttrs, start, length);
    }


  /**
   * Gets the coordinate associated with the vertex at
   * the specified index for this object using data in <code>texCoords</code>
   * @param index source vertex index in this geometry array
   * @param coordinate destination array of 3 values that will receive the coordinate
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void getCoordinate(int index, float coordinate[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COORDINATE_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray48"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    ((GeometryArrayRetained)this.retained).getCoordinate(index, coordinate);
  }

  /**
   * Gets the coordinate associated with the vertex at
   * the specified index for this object.
   * @param index source vertex index in this geometry array
   * @param coordinate destination array of 3 values that will receive the coordinate
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void getCoordinate(int index, double coordinate[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COORDINATE_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray48"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    ((GeometryArrayRetained)this.retained).getCoordinate(index, coordinate);
  }

  /**
   * Gets the coordinate associated with the vertex at
   * the specified index for this object.
   * @param index source vertex index in this geometry array
   * @param coordinate a vector that will receive the coordinate
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void getCoordinate(int index, Point3f coordinate) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COORDINATE_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray48"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    ((GeometryArrayRetained)this.retained).getCoordinate(index, coordinate);
  }

  /**
   * Gets the coordinate associated with the vertex at
   * the specified index for this object.
   * @param index source vertex index in this geometry array
   * @param coordinate a vector that will receive the coordinate
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void getCoordinate(int index, Point3d coordinate) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COORDINATE_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray48"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    ((GeometryArrayRetained)this.retained).getCoordinate(index, coordinate);
  }

  /**
   * Gets the coordinates associated with the vertices starting at
   * the specified index for this object.  The length of the destination
   * array determines the number of vertices copied.
   * A maximum of <code>vertexCount-index</code> coordinates
   * are copied.  If the destination array is larger than is needed
   * to hold the coordinates, the excess locations in the
   * array are not modified.  If the destination array is smaller
   * than is needed to hold the coordinates, only as
   * many coordinates as the array will hold are copied.
   *
   * @param index starting source vertex index in this geometry array
   * @param coordinates destination array of 3*n values that will receive new coordinates
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void getCoordinates(int index, float coordinates[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COORDINATE_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray52"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    ((GeometryArrayRetained)this.retained).getCoordinates(index, coordinates);
  }

  /**
   * Gets the coordinates associated with the vertices starting at
   * the specified index for this object.  The length of the destination
   * array determines the number of vertices copied.
   * A maximum of <code>vertexCount-index</code> coordinates
   * are copied.  If the destination array is larger than is needed
   * to hold the coordinates, the excess locations in the
   * array are not modified.  If the destination array is smaller
   * than is needed to hold the coordinates, only as
   * many coordinates as the array will hold are copied.
   *
   * @param index starting source vertex index in this geometry array
   * @param coordinates destination array of 3*n values that will receive new coordinates
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void getCoordinates(int index, double coordinates[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COORDINATE_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray52"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    ((GeometryArrayRetained)this.retained).getCoordinates(index, coordinates);
  }

  /**
   * Gets the coordinates associated with the vertices starting at
   * the specified index for this object.  The length of the destination
   * array determines the number of vertices copied.
   * A maximum of <code>vertexCount-index</code> coordinates
   * are copied.  If the destination array is larger than is needed
   * to hold the coordinates, the excess locations in the
   * array are not modified.  If the destination array is smaller
   * than is needed to hold the coordinates, only as
   * many coordinates as the array will hold are copied.
   *
   * @param index starting source vertex index in this geometry array
   * @param coordinates destination array of points that will receive new coordinates
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void getCoordinates(int index, Point3f coordinates[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COORDINATE_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray52"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    ((GeometryArrayRetained)this.retained).getCoordinates(index, coordinates);
  }

  /**
   * Gets the coordinates associated with the vertices starting at
   * the specified index for this object.  The length of the destination
   * array determines the number of vertices copied.
   * A maximum of <code>vertexCount-index</code> coordinates
   * are copied.  If the destination array is larger than is needed
   * to hold the coordinates, the excess locations in the
   * array are not modified.  If the destination array is smaller
   * than is needed to hold the coordinates, only as
   * many coordinates as the array will hold are copied.
   *
   * @param index starting source vertex index in this geometry array
   * @param coordinates destination array of points that will receive new coordinates
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void getCoordinates(int index, Point3d coordinates[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COORDINATE_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray52"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    ((GeometryArrayRetained)this.retained).getCoordinates(index, coordinates);
  }

  /**
   * Gets the color associated with the vertex at
   * the specified index for this object. The color is copied into the
   * specified array. The array must be large enough to hold all 
   * of the colors.
   * @param index source vertex index in this geometry array
   * @param color destination array of 3 or 4 values that will receive the color
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void getColor(int index, float color[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray56"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    ((GeometryArrayRetained)this.retained).getColor(index, color);
  }

  /**
   * Gets the color associated with the vertex at
   * the specified index for this object. The color is copied into the
   * specified array. The array must be large enough to hold all of
   * the colors.
   * @param index source vertex index in this geometry array
   * @param color destination array of 3 or 4 values that will receive the color
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void getColor(int index, byte color[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray56"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    ((GeometryArrayRetained)this.retained).getColor(index, color);
  }

  /**
   * Gets the color associated with the vertex at
   * the specified index for this object.
   * @param index source vertex index in this geometry array
   * @param color a vector that will receive the color
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception IllegalStateException if the data mode for this geometry
   * array object is <code>BY_REFERENCE</code>.
   * @exception IllegalStateException if COLOR_4 is specified in the vertex
   * format
   */
  public void getColor(int index, Color3f color) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray56"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    if ((format & WITH_ALPHA) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray92"));

    ((GeometryArrayRetained)this.retained).getColor(index, color);
  }

  /**
   * Gets the color associated with the vertex at
   * the specified index for this object.
   * @param index source vertex index in this geometry array
   * @param color a vector that will receive the color
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception IllegalStateException if the data mode for this geometry
   * array object is <code>BY_REFERENCE</code>.
   * @exception IllegalStateException if COLOR_3 is specified in the vertex
   * format
   */
  public void getColor(int index, Color4f color) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray56"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    if ((format & WITH_ALPHA) == 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray93"));

    ((GeometryArrayRetained)this.retained).getColor(index, color);
  }

  /**
   * Gets the color associated with the vertex at
   * the specified index for this object.
   * @param index source vertex index in this geometry array
   * @param color a vector that will receive the color
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception IllegalStateException if the data mode for this geometry
   * array object is <code>BY_REFERENCE</code>.
   * @exception IllegalStateException if COLOR_4 is specified in the vertex
   * format
   */
  public void getColor(int index, Color3b color) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray56"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    if ((format & WITH_ALPHA) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray92"));

    ((GeometryArrayRetained)this.retained).getColor(index, color);
  }

  /**
   * Gets the color associated with the vertex at
   * the specified index for this object.
   * @param index source vertex index in this geometry array
   * @param color a vector that will receive the color
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception IllegalStateException if the data mode for this geometry
   * array object is <code>BY_REFERENCE</code>.
   * @exception IllegalStateException if COLOR_3 is specified in the vertex
   * format
   */
  public void getColor(int index, Color4b color) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray56"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    if ((format & WITH_ALPHA) == 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray93"));

    ((GeometryArrayRetained)this.retained).getColor(index, color);
  }

  /**
   * Gets the colors associated with the vertices starting at
   * the specified index for this object.  The color is copied into the
   * specified array. The length of the destination
   * array determines the number of colors copied.
   * A maximum of <code>vertexCount-index</code> colors
   * are copied.  If the destination array is larger than is needed
   * to hold the colors, the excess locations in the
   * array are not modified.  If the destination array is smaller
   * than is needed to hold the colors, only as
   * many colors as the array will hold are copied.
   *
   * @param index starting source vertex index in this geometry array
   * @param colors destination array of 3*n or 4*n values that will 
   * receive n new colors
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void getColors(int index, float colors[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray62"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    ((GeometryArrayRetained)this.retained).getColors(index, colors);
  }

  /**
   * Gets the colors associated with the vertices starting at
   * the specified index for this object.  The color is copied into the
   * specified array. The length of the destination
   * array determines the number of colors copied.
   * A maximum of <code>vertexCount-index</code> colors
   * are copied.  If the destination array is larger than is needed
   * to hold the colors, the excess locations in the
   * array are not modified.  If the destination array is smaller
   * than is needed to hold the colors, only as
   * many colors as the array will hold are copied.
   *
   * @param index starting source vertex index in this geometry array
   * @param colors destination array of 3*n or 4*n values that will 
   * receive new colors
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void getColors(int index, byte colors[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray62"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    ((GeometryArrayRetained)this.retained).getColors(index, colors);
  }

  /**
   * Gets the colors associated with the vertices starting at
   * the specified index for this object.  The color is copied into the
   * specified array. The length of the destination
   * array determines the number of colors copied.
   * A maximum of <code>vertexCount-index</code> colors
   * are copied.  If the destination array is larger than is needed
   * to hold the colors, the excess locations in the
   * array are not modified.  If the destination array is smaller
   * than is needed to hold the colors, only as
   * many colors as the array will hold are copied.
   *
   * @param index starting source vertex index in this geometry array
   * @param colors destination array of Color3f objects that will receive new colors
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception IllegalStateException if the data mode for this geometry
   * array object is <code>BY_REFERENCE</code>.
   * @exception IllegalStateException if COLOR_4 is specified in the vertex
   * format
   */
  public void getColors(int index, Color3f colors[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray62"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    if ((format & WITH_ALPHA) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray92"));

    ((GeometryArrayRetained)this.retained).getColors(index, colors);
  }

  /**
   * Gets the colors associated with the vertices starting at
   * the specified index for this object.  The color is copied into the
   * specified array. The length of the destination
   * array determines the number of colors copied.
   * A maximum of <code>vertexCount-index</code> colors
   * are copied.  If the destination array is larger than is needed
   * to hold the colors, the excess locations in the
   * array are not modified.  If the destination array is smaller
   * than is needed to hold the colors, only as
   * many colors as the array will hold are copied.
   *
   * @param index starting source vertex index in this geometry array
   * @param colors destination array of Color4f objects that will receive new colors
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception IllegalStateException if the data mode for this geometry
   * array object is <code>BY_REFERENCE</code>.
   * @exception IllegalStateException if COLOR_3 is specified in the vertex
   * format
   */
  public void getColors(int index, Color4f colors[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray62"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    if ((format & WITH_ALPHA) == 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray93"));

    ((GeometryArrayRetained)this.retained).getColors(index, colors);
  }

  /**
   * Gets the colors associated with the vertices starting at
   * the specified index for this object.  The color is copied into the
   * specified array. The length of the destination
   * array determines the number of colors copied.
   * A maximum of <code>vertexCount-index</code> colors
   * are copied.  If the destination array is larger than is needed
   * to hold the colors, the excess locations in the
   * array are not modified.  If the destination array is smaller
   * than is needed to hold the colors, only as
   * many colors as the array will hold are copied.
   *
   * @param index starting source vertex index in this geometry array
   * @param colors destination array of Color3b objects that will receive new colors
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception IllegalStateException if the data mode for this geometry
   * array object is <code>BY_REFERENCE</code>.
   * @exception IllegalStateException if COLOR_4 is specified in the vertex
   * format
   */
  public void getColors(int index, Color3b colors[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray62"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    if ((format & WITH_ALPHA) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray92"));

    ((GeometryArrayRetained)this.retained).getColors(index, colors);
  }

  /**
   * Gets the colors associated with the vertices starting at
   * the specified index for this object.  The color is copied into the
   * specified array. The length of the destination
   * array determines the number of colors copied.
   * A maximum of <code>vertexCount-index</code> colors
   * are copied.  If the destination array is larger than is needed
   * to hold the colors, the excess locations in the
   * array are not modified.  If the destination array is smaller
   * than is needed to hold the colors, only as
   * many colors as the array will hold are copied.
   *
   * @param index starting source vertex index in this geometry array
   * @param colors destination array of Color4b objects that will receive new colors
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
   * @exception IllegalStateException if the data mode for this geometry
   * array object is <code>BY_REFERENCE</code>.
   * @exception IllegalStateException if COLOR_3 is specified in the vertex
   * format
   */
  public void getColors(int index, Color4b colors[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray62"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & COLOR ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray76"));

    if ((format & WITH_ALPHA) == 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray93"));

    ((GeometryArrayRetained)this.retained).getColors(index, colors);
  }

  /**
   * Gets the normal associated with the vertex at
   * the specified index for this object. The normal is copied into
   * the specified array.
   * @param index source vertex index in this geometry array
   * @param normal destination array of 3 values that will receive the normal
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void getNormal(int index, float normal[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_NORMAL_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray68"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & NORMALS ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray77"));

    ((GeometryArrayRetained)this.retained).getNormal(index, normal);
  }

  /**
   * Gets the normal associated with the vertex at
   * the specified index for this object.
   * @param index source vertex index in this geometry array
   * @param normal the vector that will receive the normal
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void getNormal(int index, Vector3f normal) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_NORMAL_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray68"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & NORMALS ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray77"));

    ((GeometryArrayRetained)this.retained).getNormal(index, normal);
  }

  /**
   * Gets the normals associated with the vertices starting at
   * the specified index for this object.  The length of the destination
   * array determines the number of normals copied.
   * A maximum of <code>vertexCount-index</code> normals
   * are copied.  If the destination array is larger than is needed
   * to hold the normals, the excess locations in the
   * array are not modified.  If the destination array is smaller
   * than is needed to hold the normals, only as
   * many normals as the array will hold are copied.
   *
   * @param index starting source vertex index in this geometry array
   * @param normals destination array of 3*n values that will receive the normal
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void getNormals(int index, float normals[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_NORMAL_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray70"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & NORMALS ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray77"));

    ((GeometryArrayRetained)this.retained).getNormals(index, normals);
  }

  /**
   * Gets the normals associated with the vertices starting at
   * the specified index for this object.  The length of the destination
   * array determines the number of normals copied.
   * A maximum of <code>vertexCount-index</code> normals
   * are copied.  If the destination array is larger than is needed
   * to hold the normals, the excess locations in the
   * array are not modified.  If the destination array is smaller
   * than is needed to hold the normals, only as
   * many normals as the array will hold are copied.
   *
   * @param index starting source vertex index in this geometry array
   * @param normals destination array of vectors that will receive the normals
   * @exception CapabilityNotSetException if the appropriate capability is
   * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
   */
  public void getNormals(int index, Vector3f normals[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_NORMAL_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray70"));

    int format = ((GeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE) != 0)
      throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

    if ((format & NORMALS ) == 0)
      throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray77"));

    ((GeometryArrayRetained)this.retained).getNormals(index, normals);
  }

    /**
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>getTextureCoordinate(int texCoordSet, ...)</code>
     */
    public void getTextureCoordinate(int index, float texCoord[]) {
	getTextureCoordinate(0, index, texCoord);
    }

    /**
     * Gets the texture coordinate associated with the vertex at
     * the specified index in the specified texture coordinate set
     * for this object.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param index source vertex index in this geometry array
     * @param texCoord array of 2, 3 or 4 values that will receive the
     * texture coordinate
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if the index or
     * texCoordSet is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @since Java 3D 1.2
     */
    public void getTextureCoordinate(int texCoordSet,
				     int index, float texCoord[]) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray72"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

	if ((format & TEXTURE_COORDINATE ) == 0)
	    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray79"));

	((GeometryArrayRetained)this.retained).getTextureCoordinate(
				texCoordSet, index, texCoord);
    }

    /**
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>getTextureCoordinate(int texCoordSet, TexCoord2f texCoord)</code>
     */
    public void getTextureCoordinate(int index, Point2f texCoord) {
	getTextureCoordinate(0, index, texCoord2fArray[0]);
	texCoord.set(texCoord2fArray[0]);
    }

    /**
     * Gets the texture coordinate associated with the vertex at
     * the specified index in the specified texture coordinate set
     * for this object.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param index source vertex index in this geometry array
     * @param texCoord the vector that will receive the texture coordinates
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if the index or
     * texCoordSet is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if TEXTURE_COORDINATE_3 or
     * TEXTURE_COORDINATE_4 is specified in vertex format
     *
     * @since Java 3D 1.2
     */
    public void getTextureCoordinate(int texCoordSet,
				     int index, TexCoord2f texCoord) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray72"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

	if ((format & TEXTURE_COORDINATE ) == 0)
	    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray79"));

        if (((((GeometryArrayRetained)this.retained).vertexFormat) &
                (TEXTURE_COORDINATE_3 | TEXTURE_COORDINATE_4)) != 0)
            throw new IllegalStateException(J3dI18N.getString("GeometryArray94"));

	((GeometryArrayRetained)this.retained).getTextureCoordinate(
				texCoordSet, index, texCoord);
    }

    /**
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>getTextureCoordinate(int texCoordSet, TexCoord3f texCoord)</code>
     */
    public void getTextureCoordinate(int index, Point3f texCoord) {
	getTextureCoordinate(0, index, texCoord3fArray[0]);
	texCoord.set(texCoord3fArray[0]);
    }

    /**
     * Gets the texture coordinate associated with the vertex at
     * the specified index in the specified texture coordinate set
     * for this object.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param index source vertex index in this geometry array
     * @param texCoord the vector that will receive the texture coordinates
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if the index or
     * texCoordSet is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if TEXTURE_COORDINATE_2 or
     * TEXTURE_COORDINATE_4 is specified in vertex format
     *
     * @since Java 3D 1.2
     */
    public void getTextureCoordinate(int texCoordSet,
				     int index, TexCoord3f texCoord) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray72"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

	if ((format & TEXTURE_COORDINATE ) == 0)
	    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray79"));

        if (((((GeometryArrayRetained)this.retained).vertexFormat) &
                (TEXTURE_COORDINATE_2 | TEXTURE_COORDINATE_4)) != 0)
            throw new IllegalStateException(J3dI18N.getString("GeometryArray95"));
	((GeometryArrayRetained)this.retained).getTextureCoordinate(
				texCoordSet, index, texCoord);
    }

    /**
     * Gets the texture coordinate associated with the vertex at
     * the specified index in the specified texture coordinate set
     * for this object.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param index source vertex index in this geometry array
     * @param texCoord the vector that will receive the texture coordinates
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if the index or
     * texCoordSet is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if TEXTURE_COORDINATE_2 or
     * TEXTURE_COORDINATE_3 is specified in vertex format
     *
     * @since Java 3D 1.3
     */
    public void getTextureCoordinate(int texCoordSet,
				     int index, TexCoord4f texCoord) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray72"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

	if ((format & TEXTURE_COORDINATE ) == 0)
	    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray79"));

        if (((((GeometryArrayRetained)this.retained).vertexFormat) &
                (TEXTURE_COORDINATE_2 | TEXTURE_COORDINATE_3)) != 0)
            throw new IllegalStateException(J3dI18N.getString("GeometryArray109"));
	((GeometryArrayRetained)this.retained).getTextureCoordinate(
				texCoordSet, index, texCoord);
    }


    /**
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>getTextureCoordinates(int texCoordSet, ...)</code>
     */
    public void getTextureCoordinates(int index, float texCoords[]) {
	getTextureCoordinates(0, index, texCoords);
    }

    /**
     * Gets the texture coordinates associated with the vertices starting at
     * the specified index in the specified texture coordinate set
     * for this object.  The length of the destination
     * array determines the number of texture coordinates copied.
     * A maximum of <code>vertexCount-index</code> texture coordinates
     * are copied.  If the destination array is larger than is needed
     * to hold the texture coordinates, the excess locations in the
     * array are not modified.  If the destination array is smaller
     * than is needed to hold the texture coordinates, only as
     * many texture coordinates as the array will hold are copied.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param index starting source vertex index in this geometry array
     * @param texCoords destination array of 2*n , 3*n or 4*n values that
     * will receive n new texture coordinates
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if the index or
     * texCoordSet is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @since Java 3D 1.2
     */
    public void getTextureCoordinates(int texCoordSet,
				      int index, float texCoords[]) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray75"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

	if ((format & TEXTURE_COORDINATE ) == 0)
	    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray79"));

	((GeometryArrayRetained)this.retained).getTextureCoordinates(
				texCoordSet, index, texCoords);
    }

    /**
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>getTextureCoordinates(int texCoordSet, TexCoord2f texCoords[])</code>
     */
    public void getTextureCoordinates(int index, Point2f texCoords[]) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray75"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

	if ((format & TEXTURE_COORDINATE ) == 0)
	    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray79"));

	((GeometryArrayRetained)this.retained).getTextureCoordinates(
				0, index, texCoords);
    }

    /**
     * Gets the texture coordinates associated with the vertices starting at
     * the specified index in the specified texture coordinate set
     * for this object.  The length of the destination
     * array determines the number of texture coordinates copied.
     * A maximum of <code>vertexCount-index</code> texture coordinates
     * are copied.  If the destination array is larger than is needed
     * to hold the texture coordinates, the excess locations in the
     * array are not modified.  If the destination array is smaller
     * than is needed to hold the texture coordinates, only as
     * many texture coordinates as the array will hold are copied.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param index starting source vertex index in this geometry array
     * @param texCoords destination array of TexCoord2f objects that will
     * receive the texture coordinates
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if the index or
     * texCoordSet is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if TEXTURE_COORDINATE_3 or
     * TEXTURE_COORDINATE_4 is specified in vertex format
     *
     * @since Java 3D 1.2
     */
    public void getTextureCoordinates(int texCoordSet,
				      int index, TexCoord2f texCoords[]) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray75"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

	if ((format & TEXTURE_COORDINATE ) == 0)
	    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray79"));

        if (((((GeometryArrayRetained)this.retained).vertexFormat) &
                (TEXTURE_COORDINATE_3 | TEXTURE_COORDINATE_4)) != 0)
            throw new IllegalStateException(J3dI18N.getString("GeometryArray94"));
	((GeometryArrayRetained)this.retained).getTextureCoordinates(
				texCoordSet, index, texCoords);
    }

    /**
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>getTextureCoordinates(int texCoordSet, TexCoord3f texCoords[])</code>
     */
    public void getTextureCoordinates(int index, Point3f texCoords[]) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray75"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

	if ((format & TEXTURE_COORDINATE ) == 0)
	    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray79"));

	((GeometryArrayRetained)this.retained).getTextureCoordinates(
				0, index, texCoords);
    }

    /**
     * Gets the texture coordinates associated with the vertices starting at
     * the specified index in the specified texture coordinate set
     * for this object.  The length of the destination
     * array determines the number of texture coordinates copied.
     * A maximum of <code>vertexCount-index</code> texture coordinates
     * are copied.  If the destination array is larger than is needed
     * to hold the texture coordinates, the excess locations in the
     * array are not modified.  If the destination array is smaller
     * than is needed to hold the texture coordinates, only as
     * many texture coordinates as the array will hold are copied.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param index starting source vertex index in this geometry array
     * @param texCoords destination array of TexCoord3f objects that will
     * receive the texture coordinates
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if the index or
     * texCoordSet is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if TEXTURE_COORDINATE_2 or
     * TEXTURE_COORDINATE_4 is specified in vertex format
     *
     * @since Java 3D 1.2
     */
    public void getTextureCoordinates(int texCoordSet,
				      int index, TexCoord3f texCoords[]) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray75"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

	if ((format & TEXTURE_COORDINATE ) == 0)
	    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray79"));

        if (((((GeometryArrayRetained)this.retained).vertexFormat) &
                (TEXTURE_COORDINATE_2 | TEXTURE_COORDINATE_4)) != 0)
            throw new IllegalStateException(J3dI18N.getString("GeometryArray95"));
	((GeometryArrayRetained)this.retained).getTextureCoordinates(
					texCoordSet, index, texCoords);
    }


    /**
     * Gets the texture coordinates associated with the vertices starting at
     * the specified index in the specified texture coordinate set
     * for this object.  The length of the destination
     * array determines the number of texture coordinates copied.
     * A maximum of <code>vertexCount-index</code> texture coordinates
     * are copied.  If the destination array is larger than is needed
     * to hold the texture coordinates, the excess locations in the
     * array are not modified.  If the destination array is smaller
     * than is needed to hold the texture coordinates, only as
     * many texture coordinates as the array will hold are copied.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param index starting source vertex index in this geometry array
     * @param texCoords destination array of TexCoord4f objects that will
     * receive the texture coordinates
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if the index or
     * texCoordSet is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if TEXTURE_COORDINATE_2 or
     * TEXTURE_COORDINATE_3 is specified in vertex format
     *
     * @since Java 3D 1.3
     */
    public void getTextureCoordinates(int texCoordSet,
				      int index, TexCoord4f texCoords[]) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray75"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

	if ((format & TEXTURE_COORDINATE ) == 0)
	    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray79"));

        if (((((GeometryArrayRetained)this.retained).vertexFormat) &
                (TEXTURE_COORDINATE_2 | TEXTURE_COORDINATE_3)) != 0)
            throw new IllegalStateException(J3dI18N.getString("GeometryArray109"));

	((GeometryArrayRetained)this.retained).getTextureCoordinates(
					texCoordSet, index, texCoords);
    }

    /**
     * Gets the vertex attribute associated with the vertex at
     * the specified index in the specified vertex attribute number
     * for this object.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index source vertex index in this geometry array
     * @param vertexAttr array of 1, 2, 3 or 4 values that will receive the
     * vertex attribute
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if the index or
     * vertexAttrNum is out of range, or if the vertexAttr array is
     * too small.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @since Java 3D 1.4
     */
    public void getVertexAttr(int vertexAttrNum, int index,
			      float[] vertexAttr) {
	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_VERTEX_ATTR_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray127"));
	    }
	}

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));
	}

	((GeometryArrayRetained)this.retained).getVertexAttr(
				vertexAttrNum, index, vertexAttr);
    }

    /**
     * Gets the vertex attribute associated with the vertex at
     * the specified index in the specified vertex attribute number
     * for this object.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index source vertex index in this geometry array
     * @param vertexAttr the vector that will receive the vertex attributes
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if the index or
     * vertexAttrNum is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if the size of the specified
     * vertex attribute number is not 2.
     *
     * @since Java 3D 1.4
     */
    public void getVertexAttr(int vertexAttrNum, int index,
			      Point2f vertexAttr) {
	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_VERTEX_ATTR_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray127"));
	    }
	}

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));
	}

	int size = ((GeometryArrayRetained)this.retained).vertexAttrSizes[vertexAttrNum];
	if (size != 2) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray134"));
	}

	((GeometryArrayRetained)this.retained).getVertexAttr(
				vertexAttrNum, index, vertexAttr);
    }

    /**
     * Gets the vertex attribute associated with the vertex at
     * the specified index in the specified vertex attribute number
     * for this object.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index source vertex index in this geometry array
     * @param vertexAttr the vector that will receive the vertex attributes
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if the index or
     * vertexAttrNum is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if the size of the specified
     * vertex attribute number is not 3.
     *
     * @since Java 3D 1.4
     */
    public void getVertexAttr(int vertexAttrNum, int index,
			      Point3f vertexAttr) {
	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_VERTEX_ATTR_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray127"));
	    }
	}

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));
	}

	int size = ((GeometryArrayRetained)this.retained).vertexAttrSizes[vertexAttrNum];
	if (size != 3) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray134"));
	}

	((GeometryArrayRetained)this.retained).getVertexAttr(
				vertexAttrNum, index, vertexAttr);
    }

    /**
     * Gets the vertex attribute associated with the vertex at
     * the specified index in the specified vertex attribute number
     * for this object.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index source vertex index in this geometry array
     * @param vertexAttr the vector that will receive the vertex attributes
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if the index or
     * vertexAttrNum is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if the size of the specified
     * vertex attribute number is not 4.
     *
     * @since Java 3D 1.4
     */
    public void getVertexAttr(int vertexAttrNum, int index,
			      Point4f vertexAttr) {
	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_VERTEX_ATTR_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray127"));
	    }
	}

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));
	}

	int size = ((GeometryArrayRetained)this.retained).vertexAttrSizes[vertexAttrNum];
	if (size != 4) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray134"));
	}

	((GeometryArrayRetained)this.retained).getVertexAttr(
				vertexAttrNum, index, vertexAttr);
    }

    /**
     * Gets the vertex attributes associated with the vertices starting at
     * the specified index in the specified vertex attribute number
     * for this object.  The length of the destination
     * array determines the number of vertex attributes copied.
     * A maximum of <code>vertexCount-index</code> vertex attributes
     * are copied.  If the destination array is larger than is needed
     * to hold the vertex attributes, the excess locations in the
     * array are not modified.  If the destination array is smaller
     * than is needed to hold the vertex attributes, only as
     * many vertex attributes as the array will hold are copied.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index starting source vertex index in this geometry array
     * @param vertexAttrs destination array of 1*n, 2*n, 3*n, or 4*n values
     * that will receive n new vertex attributes
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if the index or
     * vertexAttrNum is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @since Java 3D 1.4
     */
    public void getVertexAttrs(int vertexAttrNum, int index,
			       float[] vertexAttrs) {

	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_VERTEX_ATTR_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray127"));
	    }
	}

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));
	}

	((GeometryArrayRetained)this.retained).getVertexAttrs(
				vertexAttrNum, index, vertexAttrs);
    }

    /**
     * Gets the vertex attributes associated with the vertices starting at
     * the specified index in the specified vertex attribute number
     * for this object.  The length of the destination
     * array determines the number of vertex attributes copied.
     * A maximum of <code>vertexCount-index</code> vertex attributes
     * are copied.  If the destination array is larger than is needed
     * to hold the vertex attributes, the excess locations in the
     * array are not modified.  If the destination array is smaller
     * than is needed to hold the vertex attributes, only as
     * many vertex attributes as the array will hold are copied.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index starting source vertex index in this geometry array
     * @param vertexAttrs destination array of Point2f objects that will
     * receive the vertex attributes
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if the index or
     * vertexAttrNum is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if the size of the specified
     * vertex attribute number is not 2.
     *
     * @since Java 3D 1.4
     */
    public void getVertexAttrs(int vertexAttrNum, int index,
			       Point2f[] vertexAttrs) {
	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_VERTEX_ATTR_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray127"));
	    }
	}

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));
	}

	int size = ((GeometryArrayRetained)this.retained).vertexAttrSizes[vertexAttrNum];
	if (size != 2) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray134"));
	}

	((GeometryArrayRetained)this.retained).getVertexAttrs(
				vertexAttrNum, index, vertexAttrs);
    }

    /**
     * Gets the vertex attributes associated with the vertices starting at
     * the specified index in the specified vertex attribute number
     * for this object.  The length of the destination
     * array determines the number of vertex attributes copied.
     * A maximum of <code>vertexCount-index</code> vertex attributes
     * are copied.  If the destination array is larger than is needed
     * to hold the vertex attributes, the excess locations in the
     * array are not modified.  If the destination array is smaller
     * than is needed to hold the vertex attributes, only as
     * many vertex attributes as the array will hold are copied.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index starting source vertex index in this geometry array
     * @param vertexAttrs destination array of Point3f objects that will
     * receive the vertex attributes
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if the index or
     * vertexAttrNum is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if the size of the specified
     * vertex attribute number is not 3.
     *
     * @since Java 3D 1.4
     */
    public void getVertexAttrs(int vertexAttrNum, int index,
			       Point3f[] vertexAttrs) {
	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_VERTEX_ATTR_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray127"));
	    }
	}

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));
	}

	int size = ((GeometryArrayRetained)this.retained).vertexAttrSizes[vertexAttrNum];
	if (size != 3) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray134"));
	}

	((GeometryArrayRetained)this.retained).getVertexAttrs(
				vertexAttrNum, index, vertexAttrs);
    }

    /**
     * Gets the vertex attributes associated with the vertices starting at
     * the specified index in the specified vertex attribute number
     * for this object.  The length of the destination
     * array determines the number of vertex attributes copied.
     * A maximum of <code>vertexCount-index</code> vertex attributes
     * are copied.  If the destination array is larger than is needed
     * to hold the vertex attributes, the excess locations in the
     * array are not modified.  If the destination array is smaller
     * than is needed to hold the vertex attributes, only as
     * many vertex attributes as the array will hold are copied.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index starting source vertex index in this geometry array
     * @param vertexAttrs destination array of Point4f objects that will
     * receive the vertex attributes
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if the index or
     * vertexAttrNum is out of range.
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE</code>.
     *
     * @exception IllegalStateException if the size of the specified
     * vertex attribute number is not 4.
     *
     * @since Java 3D 1.4
     */
    public void getVertexAttrs(int vertexAttrNum, int index,
			       Point4f[] vertexAttrs) {
	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_VERTEX_ATTR_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray127"));
	    }
	}

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));
	}

	int size = ((GeometryArrayRetained)this.retained).vertexAttrSizes[vertexAttrNum];
	if (size != 4) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray134"));
	}

	((GeometryArrayRetained)this.retained).getVertexAttrs(
				vertexAttrNum, index, vertexAttrs);
    }


    //------------------------------------------------------------------
    // By-reference methods
    //------------------------------------------------------------------

    /**
     * Sets the initial coordinate index for this GeometryArray object.
     * This index specifies the first coordinate within the array of
     * coordinates referenced by this geometry
     * array that is actually used in rendering or other operations
     * such as picking and collision.  This attribute is initialized
     * to 0.
     * This attribute is only used when the data mode for this
     * geometry array object is <code>BY_REFERENCE</code>
     * and is <i>not</i> </code>INTERLEAVED</code>.
     *
     * @param initialCoordIndex the new initial coordinate index.
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * <p>
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code> or if the data mode
     * is <code>INTERLEAVED</code>.
     * <p>
     * @exception IllegalArgumentException if either of the following are
     * true:
     * <ul>
     * <code>initialCoordIndex &lt; 0</code> or<br>
     * <code>initialCoordIndex + validVertexCount &gt; vertexCount</code><br>
     * </ul>
     * <p>
     * @exception ArrayIndexOutOfBoundsException if
     * the CoordRef array is non-null and:
     * <ul>
     * <code>CoordRef.length</code> &lt; <i>num_words</i> *
     * (<code>initialCoordIndex + validVertexCount</code>)<br>
     * </ul>
     * where <i>num_words</i> depends on which variant of
     * <code>setCoordRef</code> is used.
     *
     * @since Java 3D 1.2
     */
    public void setInitialCoordIndex(int initialCoordIndex) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COUNT_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray90"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
        if (initialCoordIndex < 0)
	    throw new IllegalArgumentException(J3dI18N.getString("GeometryArray97"));
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));	

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	((GeometryArrayRetained)this.retained).setInitialCoordIndex(initialCoordIndex);
	// NOTE: the check for initialCoordIndex + validVertexCount >
	// vertexCount needs to be done in the retained method
    }


    /**
     * Gets the initial coordinate index for this GeometryArray object.
     * This attribute is only used when the data mode for this
     * geometry array object is <code>BY_REFERENCE</code>
     * and is <i>not</i> </code>INTERLEAVED</code>.
     * @return the current initial coordinate index for this
     * GeometryArray object.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public int getInitialCoordIndex() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COUNT_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray91"));

	return ((GeometryArrayRetained)this.retained).getInitialCoordIndex();
    }


    /**
     * Sets the initial color index for this GeometryArray object.
     * This index specifies the first color within the array of
     * colors referenced by this geometry
     * array that is actually used in rendering or other operations
     * such as picking and collision.  This attribute is initialized
     * to 0.
     * This attribute is only used when the data mode for this
     * geometry array object is <code>BY_REFERENCE</code>
     * and is <i>not</i> </code>INTERLEAVED</code>.
     *
     * @param initialColorIndex the new initial color index.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * <p>
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code> or if the data mode
     * is <code>INTERLEAVED</code>.
     * <p>
     * @exception IllegalArgumentException if either of the following are
     * true:
     * <ul>
     * <code>initialColorIndex &lt; 0</code> or<br>
     * <code>initialColorIndex + validVertexCount &gt; vertexCount</code><br>
     * </ul>
     * <p>
     * @exception ArrayIndexOutOfBoundsException if
     * the ColorRef array is non-null and:
     * <ul>
     * <code>ColorRef.length</code> &lt; <i>num_words</i> *
     * (<code>initialColorIndex + validVertexCount</code>)<br>
     * </ul>
     * where <i>num_words</i> depends on which variant of
     * <code>setColorRef</code> is used.
     *
     * @since Java 3D 1.2
     */
    public void setInitialColorIndex(int initialColorIndex) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COUNT_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray90"));

        if (initialColorIndex < 0)
	    throw new IllegalArgumentException(J3dI18N.getString("GeometryArray97"));
	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));	

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	((GeometryArrayRetained)this.retained).setInitialColorIndex(initialColorIndex);
	// NOTE: the check for initialColorIndex + validVertexCount >
	// vertexCount needs to be done in the retained method
    }


    /**
     * Gets the initial color index for this GeometryArray object.
     * This attribute is only used when the data mode for this
     * geometry array object is <code>BY_REFERENCE</code>
     * and is <i>not</i> </code>INTERLEAVED</code>.
     * @return the current initial color index for this
     * GeometryArray object.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public int getInitialColorIndex() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COUNT_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray91"));

	return ((GeometryArrayRetained)this.retained).getInitialColorIndex();
    }


    /**
     * Sets the initial normal index for this GeometryArray object.
     * This index specifies the first normal within the array of
     * normals referenced by this geometry
     * array that is actually used in rendering or other operations
     * such as picking and collision.  This attribute is initialized
     * to 0.
     * This attribute is only used when the data mode for this
     * geometry array object is <code>BY_REFERENCE</code>
     * and is <i>not</i> </code>INTERLEAVED</code>.
     *
     * @param initialNormalIndex the new initial normal index.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * <p>
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code> or if the data mode
     * is <code>INTERLEAVED</code>.
     * <p>
     * @exception IllegalArgumentException if either of the following are
     * true:
     * <ul>
     * <code>initialNormalIndex &lt; 0</code> or<br>
     * <code>initialNormalIndex + validVertexCount &gt; vertexCount</code><br>
     * </ul>
     * <p>
     * @exception ArrayIndexOutOfBoundsException if normals
     * the NormalRef array is non-null and:
     * <ul>
     * <code>NormalRef.length</code> &lt; <i>num_words</i> *
     * (<code>initialNormalIndex + validVertexCount</code>)<br>
     * </ul>
     * where <i>num_words</i> depends on which variant of
     * <code>setNormalRef</code> is used.
     *
     * @since Java 3D 1.2
     */
    public void setInitialNormalIndex(int initialNormalIndex) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COUNT_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray90"));

        if (initialNormalIndex < 0)
	    throw new IllegalArgumentException(J3dI18N.getString("GeometryArray97"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	((GeometryArrayRetained)this.retained).setInitialNormalIndex(initialNormalIndex);
	// NOTE: the check for initialNormalIndex + validVertexCount >
	// vertexCount needs to be done in the retained method
    }


    /**
     * Gets the initial normal index for this GeometryArray object.
     * This attribute is only used when the data mode for this
     * geometry array object is <code>BY_REFERENCE</code>
     * and is <i>not</i> </code>INTERLEAVED</code>.
     * @return the current initial normal index for this
     * GeometryArray object.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public int getInitialNormalIndex() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COUNT_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray91"));

	return ((GeometryArrayRetained)this.retained).getInitialNormalIndex();
    }


    /**
     * Sets the initial texture coordinate index for the specified
     * texture coordinate set for this GeometryArray object.  This
     * index specifies the first texture coordinate within the array
     * of texture coordinates referenced by this geometry array that
     * is actually used in rendering or other operations such as
     * picking and collision.  This attribute is initialized to 0.
     * This attribute is only used when the data mode for this
     * geometry array object is <code>BY_REFERENCE</code>
     * and is <i>not</i> </code>INTERLEAVED</code>.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param initialTexCoordIndex the new initial texture coordinate index.
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * <p>
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code> or if the data mode
     * is <code>INTERLEAVED</code>.
     * <p>
     * @exception IllegalArgumentException if either of the following are
     * true:
     * <ul>
     * <code>initialTexCoordIndex &lt; 0</code> or<br>
     * <code>initialTexCoordIndex + validVertexCount &gt; vertexCount</code><br>
     * </ul>
     * <p>
     * @exception ArrayIndexOutOfBoundsException if
     * the TexCoordRef array is non-null and:
     * <ul>
     * <code>TexCoordRef.length</code> &lt; <i>num_words</i> *
     * (<code>initialTexCoordIndex + validVertexCount</code>)<br>
     * </ul>
     * where <i>num_words</i> depends on which variant of
     * <code>setTexCoordRef</code> is used.
     * <p>
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if texCoordSet is out of range.
     *
     * @since Java 3D 1.2
     */
    public void setInitialTexCoordIndex(int texCoordSet,
					int initialTexCoordIndex) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COUNT_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray90"));

        if (initialTexCoordIndex < 0)
	    throw new IllegalArgumentException(J3dI18N.getString("GeometryArray97"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	((GeometryArrayRetained)this.retained).setInitialTexCoordIndex(
			texCoordSet, initialTexCoordIndex);

	// NOTE: the check for initialTexCoordIndex + validVertexCount >
	// vertexCount needs to be done in the retained method
    }


    /**
     * Gets the initial texture coordinate index for the specified
     * texture coordinate set for this GeometryArray object.
     * This attribute is only used when the data mode for this
     * geometry array object is <code>BY_REFERENCE</code>
     * and is <i>not</i> </code>INTERLEAVED</code>.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     *
     * @return the current initial texture coordinate index for the specified
     * texture coordinate set
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if texCoordSet is out of range.
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public int getInitialTexCoordIndex(int texCoordSet) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COUNT_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray91"));

	return ((GeometryArrayRetained)this.retained).getInitialTexCoordIndex(
							texCoordSet);
    }


    /**
     * Sets the initial vertex attribute index for the specified
     * vertex attribute number for this GeometryArray object.  This
     * index specifies the first vertex attribute within the array
     * of vertex attributes referenced by this geometry array that
     * is actually used in rendering or other operations such as
     * picking and collision.  This attribute is initialized to 0.
     * This attribute is only used when the data mode for this
     * geometry array object is <code>BY_REFERENCE</code>
     * and is <i>not</i> </code>INTERLEAVED</code>.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param initialVertexAttrIndex the new initial vertex attribute index.
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * <p>
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code> or if the data mode
     * is <code>INTERLEAVED</code>.
     * <p>
     * @exception IllegalArgumentException if either of the following are
     * true:
     * <ul>
     * <code>initialVertexAttrIndex &lt; 0</code> or<br>
     * <code>initialVertexAttrIndex + validVertexCount &gt; vertexCount</code><br>
     * </ul>
     * <p>
     * @exception ArrayIndexOutOfBoundsException if
     * the VertexAttrRef array is non-null and:
     * <ul>
     * <code>VertexAttrRef.length</code> &lt; <i>num_words</i> *
     * (<code>initialVertexAttrIndex + validVertexCount</code>)<br>
     * </ul>
     * where <i>num_words</i> is the size of the specified
     * vertexAttrNum (1, 2, 3, or 4).
     * <p>
     * @exception ArrayIndexOutOfBoundsException if vertexAttrNum is
     * out of range.
     *
     * @since Java 3D 1.4
     */
    public void setInitialVertexAttrIndex(int vertexAttrNum,
					  int initialVertexAttrIndex) {
	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_COUNT_WRITE)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray90"));
	    }
	}

        if (initialVertexAttrIndex < 0) {
	    throw new IllegalArgumentException(J3dI18N.getString("GeometryArray97"));
	}

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));
	}

	if ((format & INTERLEAVED) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));
	}

	((GeometryArrayRetained)this.retained).setInitialVertexAttrIndex(
		vertexAttrNum, initialVertexAttrIndex);

	// NOTE: the check for initialVertexAttrIndex + validVertexCount >
	// vertexCount needs to be done in the retained method
    }


    /**
     * Gets the initial vertex attribute index for the specified
     * vertex attribute number for this GeometryArray object.
     * This attribute is only used when the data mode for this
     * geometry array object is <code>BY_REFERENCE</code>
     * and is <i>not</i> </code>INTERLEAVED</code>.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     *
     * @return the current initial vertex attribute index for the specified
     * vertex attribute number
     *
     * @exception ArrayIndexOutOfBoundsException if vertexAttrNum is
     * out of range.
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @since Java 3D 1.4
     */
    public int getInitialVertexAttrIndex(int vertexAttrNum) {
	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_COUNT_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray91"));
	    }
	}

	return ((GeometryArrayRetained)this.retained).getInitialVertexAttrIndex(
		vertexAttrNum);
    }


    /**
     * Sets the coordinate buffer reference to the specified
     * buffer object.  The buffer contains either a java.nio.FloatBuffer
     * or java.nio.DoubleBuffer object containing single or double
     * precision floating-point <i>x</i>, <i>y</i>,
     * and <i>z</i> values for each vertex (for a total of 3*<i>n</i>
     * values, where <i>n</i> is the number of vertices).
     * If the coordinate buffer
     * reference is null, the entire geometry array object is
     * treated as if it were null--any Shape3D or Morph node that uses
     * this geometry array will not be drawn.
     *
     * @param coords a J3DBuffer object to which a reference will be set.
     * The buffer contains an NIO buffer of 3*<i>n</i> float or
     * double values.
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code>,
     * is not <code>USE_NIO_BUFFER</code>, or is <code>INTERLEAVED</code>.
     *
     * @exception IllegalArgumentException if the java.nio.Buffer
     * contained in the specified J3DBuffer is not a
     * java.nio.FloatBuffer or a java.nio.DoubleBuffer object.
     *
     * @exception ArrayIndexOutOfBoundsException if
     * <code>coords.getBuffer().limit() &lt;
     * 3 * (initialCoordIndex + validVertexCount)</code>.
     *
     * @exception ArrayIndexOutOfBoundsException if this GeometryArray
     * object is a subclass of IndexedGeometryArray, and any element
     * in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * in the coordinate index array is greater than or equal to the
     * number of vertices defined by the coords object,
     * <code>coords.getBuffer().limit() / 3</code>.
     *
     * @since Java 3D 1.3
     */
    public void setCoordRefBuffer(J3DBuffer coords) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray86"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	
	if ((format & USE_NIO_BUFFER) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray118"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	((GeometryArrayRetained)this.retained).setCoordRefBuffer(coords);
    }


    /**
     * Gets the coordinate array buffer reference.
     * @return the current coordinate array buffer reference.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code>,
     * is not <code>USE_NIO_BUFFER</code>, or is <code>INTERLEAVED</code>.
     *
     * @since Java 3D 1.3
     */
    public J3DBuffer getCoordRefBuffer() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_READ) &&
		   !this.getCapability(J3D_1_2_ALLOW_REF_DATA_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray87"));
	    }

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & USE_NIO_BUFFER) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray118"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	return ((GeometryArrayRetained)this.retained).getCoordRefBuffer();
    }


    /**
     * Sets the float coordinate array reference to the specified
     * array.  The array contains floating-point <i>x</i>, <i>y</i>,
     * and <i>z</i> values for each vertex (for a total of 3*<i>n</i>
     * values, where <i>n</i> is the number of vertices).  Only one of
     * <code>coordRefFloat</code>, <code>coordRefDouble</code>,
     * <code>coordRef3f</code>, or <code>coordRef3d</code> may be
     * non-null (or they may all be null).  An attempt to set more
     * than one of these attributes to a non-null reference will
     * result in an exception being thrown.  If all coordinate array
     * references are null, the entire geometry array object is
     * treated as if it were null--any Shape3D or Morph node that uses
     * this geometry array will not be drawn.
     *
     * @param coords an array of 3*<i>n</i> values to which a
     * reference will be set.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code>,
     * is <code>USE_NIO_BUFFER</code>, or is <code>INTERLEAVED</code>.
     * @exception IllegalArgumentException if the specified array is
     * non-null and any other coordinate reference is also non-null.
     * @exception ArrayIndexOutOfBoundsException if
     * <code>coords.length &lt; 3 * (initialCoordIndex + validVertexCount)</code>.
     *
     * @exception ArrayIndexOutOfBoundsException if this GeometryArray
     * object is a subclass of IndexedGeometryArray, and any element
     * in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * in the coordinate index array is greater than or equal to the
     * number of vertices defined by the coords array,
     * <code>coords.length / 3</code>.
     *
     * @since Java 3D 1.2
     */
    public void setCoordRefFloat(float[] coords) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray86"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	((GeometryArrayRetained)this.retained).setCoordRefFloat(coords);

    }


    /**
     * Gets the float coordinate array reference.
     * @return the current float coordinate array reference.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code>,
     * is <code>USE_NIO_BUFFER</code>, or is <code>INTERLEAVED</code>.
     *
     * @since Java 3D 1.2
     */
    public float[] getCoordRefFloat() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_READ) &&
		   !this.getCapability(J3D_1_2_ALLOW_REF_DATA_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray87"));
	    }

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	return ((GeometryArrayRetained)this.retained).getCoordRefFloat();
    }


    /**
     * Sets the double coordinate array reference to the specified
     * array.  The array contains double-precision
     * floating-point <i>x</i>, <i>y</i>,
     * and <i>z</i> values for each vertex (for a total of 3*<i>n</i>
     * values, where <i>n</i> is the number of vertices).  Only one of
     * <code>coordRefFloat</code>, <code>coordRefDouble</code>,
     * <code>coordRef3f</code>, or <code>coordRef3d</code> may be
     * non-null (or they may all be null).  An attempt to set more
     * than one of these attributes to a non-null reference will
     * result in an exception being thrown.  If all coordinate array
     * references are null, the entire geometry array object is
     * treated as if it were null--any Shape3D or Morph node that uses
     * this geometry array will not be drawn.
     *
     * @param coords an array of 3*<i>n</i> values to which a
     * reference will be set.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code>,
     * is <code>USE_NIO_BUFFER</code>, or is <code>INTERLEAVED</code>.
     * @exception IllegalArgumentException if the specified array is
     * non-null and any other coordinate reference is also non-null.
     * @exception ArrayIndexOutOfBoundsException if
     * <code>coords.length &lt; 3 * (initialCoordIndex + validVertexCount)</code>.
     *
     * @exception ArrayIndexOutOfBoundsException if this GeometryArray
     * object is a subclass of IndexedGeometryArray, and any element
     * in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * in the coordinate index array is greater than or equal to the
     * number of vertices defined by the coords array,
     * <code>coords.length / 3</code>.
     *
     * @since Java 3D 1.2
     */
    public void setCoordRefDouble(double[] coords) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray86"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	((GeometryArrayRetained)this.retained).setCoordRefDouble(coords);

    }


    /**
     * Gets the double coordinate array reference.
     * @return the current double coordinate array reference.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code>,
     * is <code>USE_NIO_BUFFER</code>, or is <code>INTERLEAVED</code>.
     *
     * @since Java 3D 1.2
     */
    public double[] getCoordRefDouble() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_READ) &&
		   !this.getCapability(J3D_1_2_ALLOW_REF_DATA_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray87"));
	    }

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	return ((GeometryArrayRetained)this.retained).getCoordRefDouble();
    }


    /**
     * @deprecated As of Java 3D version 1.3, use geometry by-copy
     * for Point3f arrays
     *
     * @since Java 3D 1.2
     */
    public void setCoordRef3f(Point3f[] coords) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray86"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	((GeometryArrayRetained)this.retained).setCoordRef3f(coords);


    }


    /**
     * @deprecated As of Java 3D version 1.3, use geometry by-copy
     * for Point3f arrays
     *
     * @since Java 3D 1.2
     */
    public Point3f[] getCoordRef3f() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_READ) &&
		   !this.getCapability(J3D_1_2_ALLOW_REF_DATA_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray87"));
	    }

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	return ((GeometryArrayRetained)this.retained).getCoordRef3f();
    }


    /**
     * @deprecated As of Java 3D version 1.3, use geometry by-copy
     * for Point3d arrays
     *
     * @since Java 3D 1.2
     */
    public void setCoordRef3d(Point3d[] coords) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray86"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	((GeometryArrayRetained)this.retained).setCoordRef3d(coords);

    }


    /**
     * @deprecated As of Java 3D version 1.3, use geometry by-copy
     * for Point3d arrays
     *
     * @since Java 3D 1.2
     */
    public Point3d[] getCoordRef3d() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_READ) &&
		   !this.getCapability(J3D_1_2_ALLOW_REF_DATA_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray87"));
	    }

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	return ((GeometryArrayRetained)this.retained).getCoordRef3d();
    }


    /**
     * Sets the color buffer reference to the specified
     * buffer object.  The buffer contains either a java.nio.FloatBuffer
     * or java.nio.ByteBuffer object containing floating-point
     * or byte <i>red</i>, <i>green</i>,
     * <i>blue</i>, and, optionally, <i>alpha</i> values for each
     * vertex (for a total of 3*<i>n</i> or 4*<i>n</i> values, where
     * <i>n</i> is the number of vertices).
     * If the color buffer reference is null and colors are enabled
     * (that is, the vertexFormat includes either <code>COLOR_3</code> or
     * <code>COLOR_4</code>), the entire geometry array object is
     * treated as if it were null--any Shape3D or Morph node that uses
     * this geometry array will not be drawn.
     *
     * @param colors a J3DBuffer object to which a reference will be set.
     * The buffer contains an NIO buffer of 3*<i>n</i> or 4*<i>n</i>
     * float or byte values.
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code>,
     * is not <code>USE_NIO_BUFFER</code>, or is <code>INTERLEAVED</code>.
     *
     * @exception IllegalArgumentException if the java.nio.Buffer
     * contained in the specified J3DBuffer is not a
     * java.nio.FloatBuffer or a java.nio.ByteBuffer object.
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>COLOR</code> bits are set in the
     * <code>vertexFormat</code>, or if
     * <code>colors.getBuffer().limit() &lt; </code> <i>num_words</i> <code> *
     * (initialColorIndex + validVertexCount)</code>,
     * where <i>num_words</i> is 3 or 4 depending on the vertex color format.
     *
     * @exception ArrayIndexOutOfBoundsException if this GeometryArray
     * object is a subclass of IndexedGeometryArray, and any element
     * in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * in the color index array is greater than or equal to the
     * number of vertices defined by the colors object,
     * <code>colors.getBuffer().limit() / </code> <i>num_words</i>.
     *
     * @since Java 3D 1.3
     */
    public void setColorRefBuffer(J3DBuffer colors) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray86"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;

	if ((format & USE_NIO_BUFFER) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray118"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	((GeometryArrayRetained)this.retained).setColorRefBuffer(colors);
	
    }


    /**
     * Gets the color array buffer reference.
     * @return the current color array buffer reference.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code>,
     * is not <code>USE_NIO_BUFFER</code>, or is <code>INTERLEAVED</code>.
     *
     * @since Java 3D 1.3
     */
    public J3DBuffer getColorRefBuffer() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_READ) &&
		   !this.getCapability(J3D_1_2_ALLOW_REF_DATA_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray87"));
	    }

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;

	if ((format & USE_NIO_BUFFER) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray118"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	return ((GeometryArrayRetained)this.retained).getColorRefBuffer();	
    }


    /**
     * Sets the float color array reference to the specified array.
     * The array contains floating-point <i>red</i>, <i>green</i>,
     * <i>blue</i>, and, optionally, <i>alpha</i> values for each
     * vertex (for a total of 3*<i>n</i> or 4*<i>n</i> values, where
     * <i>n</i> is the number of vertices).  Only one of
     * <code>colorRefFloat</code>, <code>colorRefByte</code>,
     * <code>colorRef3f</code>, <code>colorRef4f</code>,
     * <code>colorRef3b</code>, or <code>colorRef4b</code> may be
     * non-null (or they may all be null).  An attempt to set more
     * than one of these attributes to a non-null reference will
     * result in an exception being thrown.  If all color array
     * references are null and colors are enabled (that is, the
     * vertexFormat includes either <code>COLOR_3</code> or
     * <code>COLOR_4</code>), the entire geometry array object is
     * treated as if it were null--any Shape3D or Morph node that uses
     * this geometry array will not be drawn.
     *
     * @param colors an array of 3*<i>n</i> or 4*<i>n</i> values to which a
     * reference will be set.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code>,
     * is <code>USE_NIO_BUFFER</code>, or is <code>INTERLEAVED</code>.
     * @exception IllegalArgumentException if the specified array is
     * non-null and any other color reference is also non-null.
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>COLOR</code> bits are set in the
     * <code>vertexFormat</code>, or if
     * <code>colors.length &lt; </code> <i>num_words</i> <code> *
     * (initialColorIndex + validVertexCount)</code>,
     * where <i>num_words</i> is 3 or 4 depending on the vertex color format.
     *
     * @exception ArrayIndexOutOfBoundsException if this GeometryArray
     * object is a subclass of IndexedGeometryArray, and any element
     * in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * in the color index array is greater than or equal to the
     * number of vertices defined by the colors array,
     * <code>colors.length / </code> <i>num_words</i>.
     *
     * @since Java 3D 1.2
     */
    public void setColorRefFloat(float[] colors) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray86"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	((GeometryArrayRetained)this.retained).setColorRefFloat(colors);

    }


    /**
     * Gets the float color array reference.
     * @return the current float color array reference.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code>,
     * is <code>USE_NIO_BUFFER</code>, or is <code>INTERLEAVED</code>.
     *
     * @since Java 3D 1.2
     */
    public float[] getColorRefFloat() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_READ) &&
		   !this.getCapability(J3D_1_2_ALLOW_REF_DATA_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray87"));
	    }

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	return ((GeometryArrayRetained)this.retained).getColorRefFloat();
    }


    /**
     * Sets the byte color array reference to the specified array.
     * The array contains <i>red</i>, <i>green</i>,
     * <i>blue</i>, and, optionally, <i>alpha</i> values for each
     * vertex (for a total of 3*<i>n</i> or 4*<i>n</i> values, where
     * <i>n</i> is the number of vertices).  Only one of
     * <code>colorRefFloat</code>, <code>colorRefByte</code>,
     * <code>colorRef3f</code>, <code>colorRef4f</code>,
     * <code>colorRef3b</code>, or <code>colorRef4b</code> may be
     * non-null (or they may all be null).  An attempt to set more
     * than one of these attributes to a non-null reference will
     * result in an exception being thrown.  If all color array
     * references are null and colors are enabled (that is, the
     * vertexFormat includes either <code>COLOR_3</code> or
     * <code>COLOR_4</code>), the entire geometry array object is
     * treated as if it were null--any Shape3D or Morph node that uses
     * this geometry array will not be drawn.
     *
     * @param colors an array of 3*<i>n</i> or 4*<i>n</i> values to which a
     * reference will be set.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code>,
     * is <code>USE_NIO_BUFFER</code>, or is <code>INTERLEAVED</code>.
     * @exception IllegalArgumentException if the specified array is
     * non-null and any other color reference is also non-null.
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>COLOR</code> bits are set in the
     * <code>vertexFormat</code>, or if
     * <code>colors.length &lt; </code> <i>num_words</i> <code> *
     * (initialColorIndex + validVertexCount)</code>,
     * where <i>num_words</i> is 3 or 4 depending on the vertex color format.
     *
     * @exception ArrayIndexOutOfBoundsException if this GeometryArray
     * object is a subclass of IndexedGeometryArray, and any element
     * in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * in the color index array is greater than or equal to the
     * number of vertices defined by the colors array,
     * <code>colors.length / </code> <i>num_words</i>.
     *
     * @since Java 3D 1.2
     */
    public void setColorRefByte(byte[] colors) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray86"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	((GeometryArrayRetained)this.retained).setColorRefByte(colors);

	// NOTE: the checks for multiple non-null references, and the
	// array length check need to be done in the retained method
    }


    /**
     * Gets the byte color array reference.
     * @return the current byte color array reference.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code>,
     * is <code>USE_NIO_BUFFER</code>, or is <code>INTERLEAVED</code>.
     *
     * @since Java 3D 1.2
     */
    public byte[] getColorRefByte() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_READ) &&
		   !this.getCapability(J3D_1_2_ALLOW_REF_DATA_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray87"));
	    }

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	return ((GeometryArrayRetained)this.retained).getColorRefByte();
    }


    /**
     * @deprecated As of Java 3D version 1.3, use geometry by-copy
     * for Color3f arrays
     *
     * @since Java 3D 1.2
     */
    public void setColorRef3f(Color3f[] colors) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray86"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	if ((format & WITH_ALPHA) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray92"));

	((GeometryArrayRetained)this.retained).setColorRef3f(colors);

	// NOTE: the checks for multiple non-null references, and the
	// array length check need to be done in the retained method
    }


    /**
     * @deprecated As of Java 3D version 1.3, use geometry by-copy
     * for Color3f arrays
     *
     * @since Java 3D 1.2
     */
    public Color3f[] getColorRef3f() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_READ) &&
		   !this.getCapability(J3D_1_2_ALLOW_REF_DATA_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray87"));
	    }

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	return ((GeometryArrayRetained)this.retained).getColorRef3f();
    }


    /**
     * @deprecated As of Java 3D version 1.3, use geometry by-copy
     * for Color4f arrays
     *
     * @since Java 3D 1.2
     */
    public void setColorRef4f(Color4f[] colors) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray86"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	if ((format & WITH_ALPHA) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray93"));

	((GeometryArrayRetained)this.retained).setColorRef4f(colors);

	// NOTE: the checks for multiple non-null references, and the
	// array length check need to be done in the retained method
    }


    /**
     * @deprecated As of Java 3D version 1.3, use geometry by-copy
     * for Color4f arrays
     *
     * @since Java 3D 1.2
     */
    public Color4f[] getColorRef4f() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_READ) &&
		   !this.getCapability(J3D_1_2_ALLOW_REF_DATA_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray87"));
	    }

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	return ((GeometryArrayRetained)this.retained).getColorRef4f();
    }


    /**
     * @deprecated As of Java 3D version 1.3, use geometry by-copy
     * for Color3b arrays
     *
     * @since Java 3D 1.2
     */
    public void setColorRef3b(Color3b[] colors) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray86"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	if ((format & WITH_ALPHA) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray92"));

	((GeometryArrayRetained)this.retained).setColorRef3b(colors);

	// NOTE: the checks for multiple non-null references, and the
	// array length check need to be done in the retained method
    }


    /**
     * @deprecated As of Java 3D version 1.3, use geometry by-copy
     * for Color3b arrays
     *
     * @since Java 3D 1.2
     */
    public Color3b[] getColorRef3b() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_READ) &&
		   !this.getCapability(J3D_1_2_ALLOW_REF_DATA_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray87"));
	    }

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	return ((GeometryArrayRetained)this.retained).getColorRef3b();
    }


    /**
     * @deprecated As of Java 3D version 1.3, use geometry by-copy
     * for Color4b arrays
     *
     * @since Java 3D 1.2
     */
    public void setColorRef4b(Color4b[] colors) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray86"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	if ((format & WITH_ALPHA) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray93"));

	((GeometryArrayRetained)this.retained).setColorRef4b(colors);

	// NOTE: the checks for multiple non-null references, and the
	// array length check need to be done in the retained method
    }


    /**
     * @deprecated As of Java 3D version 1.3, use geometry by-copy
     * for Color4b arrays
     *
     * @since Java 3D 1.2
     */
    public Color4b[] getColorRef4b() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_READ) &&
		   !this.getCapability(J3D_1_2_ALLOW_REF_DATA_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray87"));
	    }

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	return ((GeometryArrayRetained)this.retained).getColorRef4b();
    }


    /**
     * Sets the normal buffer reference to the specified
     * buffer object.  The buffer contains a java.nio.FloatBuffer
     * object containing <i>nx</i>, <i>ny</i>,
     * and <i>nz</i> values for each vertex (for a total of 3*<i>n</i>
     * values, where <i>n</i> is the number of vertices).
     * If the normal buffer reference is null and normals are enabled
     * (that is, the vertexFormat includes <code>NORMAL</code>), the
     * entire geometry array object is treated as if it were null--any
     * Shape3D or Morph node that uses this geometry array will not be
     * drawn.
     *
     * @param normals a J3DBuffer object to which a reference will be set.
     * The buffer contains an NIO buffer of 3*<i>n</i> float values.
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code>,
     * is not <code>USE_NIO_BUFFER</code>, or is <code>INTERLEAVED</code>.
     *
     * @exception IllegalArgumentException if the java.nio.Buffer
     * contained in the specified J3DBuffer is not a
     * java.nio.FloatBuffer object.
     *
     * @exception ArrayIndexOutOfBoundsException if
     * <code>NORMALS</code> bit is not set in the
     * <code>vertexFormat</code>, or if
     * <code>normals.getBuffer().limit() &lt;
     * 3 * (initialNormalIndex + validVertexCount)</code>.
     *
     * @exception ArrayIndexOutOfBoundsException if this GeometryArray
     * object is a subclass of IndexedGeometryArray, and any element
     * in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * in the normal index array is greater than or equal to the
     * number of vertices defined by the normals object,
     * <code>normals.getBuffer().limit() / 3</code>.
     *
     * @since Java 3D 1.3
     */
    public void setNormalRefBuffer(J3DBuffer normals) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray86"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & USE_NIO_BUFFER) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray118"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	((GeometryArrayRetained)this.retained).setNormalRefBuffer(normals);
    }


    /**
     * Gets the normal array buffer reference.
     * @return the current normal array buffer reference.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code>,
     * is not <code>USE_NIO_BUFFER</code>, or is <code>INTERLEAVED</code>.
     *
     * @since Java 3D 1.3
     */
    public J3DBuffer getNormalRefBuffer() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_READ) &&
		   !this.getCapability(J3D_1_2_ALLOW_REF_DATA_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray87"));
	    }

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;

	if ((format & USE_NIO_BUFFER) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray118"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	return ((GeometryArrayRetained)this.retained).getNormalRefBuffer();	
    }


    /**
     * Sets the float normal array reference to the specified
     * array.  The array contains floating-point <i>nx</i>, <i>ny</i>,
     * and <i>nz</i> values for each vertex (for a total of 3*<i>n</i>
     * values, where <i>n</i> is the number of vertices).  Only one of
     * <code>normalRefFloat</code> or <code>normalRef3f</code> may be
     * non-null (or they may all be null).  An attempt to set more
     * than one of these attributes to a non-null reference will
     * result in an exception being thrown.  If all normal array
     * references are null and normals are enabled (that is, the
     * vertexFormat includes
     * <code>NORMAL</code>), the entire geometry array object is
     * treated as if it were null--any Shape3D or Morph node that uses
     * this geometry array will not be drawn.
     *
     * @param normals an array of 3*<i>n</i> values to which a
     * reference will be set.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code>,
     * is <code>USE_NIO_BUFFER</code>, or is <code>INTERLEAVED</code>.
     * @exception IllegalArgumentException if the specified array is
     * non-null and any other normal reference is also non-null.
     * @exception ArrayIndexOutOfBoundsException if
     * <code>NORMALS</code> bit is not set in the
     * <code>vertexFormat</code>, or if
     * <code>normals.length &lt; 3 * (initialNormalIndex + validVertexCount)</code>.
     *
     * @exception ArrayIndexOutOfBoundsException if this GeometryArray
     * object is a subclass of IndexedGeometryArray, and any element
     * in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * in the normal index array is greater than or equal to the
     * number of vertices defined by the normals array,
     * <code>normals.length / 3</code>.
     *
     * @since Java 3D 1.2
     */
    public void setNormalRefFloat(float[] normals) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray86"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	((GeometryArrayRetained)this.retained).setNormalRefFloat(normals);

	// NOTE: the checks for multiple non-null references, and the
	// array length check need to be done in the retained method
    }


    /**
     * Gets the float normal array reference.
     * @return the current float normal array reference.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code>,
     * is <code>USE_NIO_BUFFER</code>, or is <code>INTERLEAVED</code>.
     *
     * @since Java 3D 1.2
     */
    public float[] getNormalRefFloat() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_READ) &&
		   !this.getCapability(J3D_1_2_ALLOW_REF_DATA_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray87"));
	    }

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	return ((GeometryArrayRetained)this.retained).getNormalRefFloat();
    }


    /**
     * @deprecated As of Java 3D version 1.3, use geometry by-copy
     * for Vector3f arrays
     *
     * @since Java 3D 1.2
     */
    public void setNormalRef3f(Vector3f[] normals) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray86"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	((GeometryArrayRetained)this.retained).setNormalRef3f(normals);

	// NOTE: the checks for multiple non-null references, and the
	// array length check need to be done in the retained method
    }


    /**
     * @deprecated As of Java 3D version 1.3, use geometry by-copy
     * for Vector3f arrays
     *
     * @since Java 3D 1.2
     */
    public Vector3f[] getNormalRef3f() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_READ) &&
		   !this.getCapability(J3D_1_2_ALLOW_REF_DATA_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray87"));
	    }

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	return ((GeometryArrayRetained)this.retained).getNormalRef3f();
    }


    /**
     * Sets the texture coordinate buffer reference for the specified
     * texture coordinate set to the
     * specified buffer object.  The buffer contains a java.nio.FloatBuffer
     * object containing <i>s</i>,
     * <i>t</i>, and, optionally, <i>r</i> and <i>q</i> values for each 
     * vertex (for
     * a total of 2*<i>n</i> , 3*<i>n</i> or 4*<i>n</i> values, 
     * where <i>n</i> is
     * the number of vertices).
     * If the texCoord buffer reference is null and texture
     * coordinates are enabled (that is, the vertexFormat includes
     * <code>TEXTURE_COORDINATE_2</code>,
     * <code>TEXTURE_COORDINATE_3</code>, or
     * <code>TEXTURE_COORDINATE_4</code>), the entire geometry
     * array object is treated as if it were null--any Shape3D or
     * Morph node that uses this geometry array will not be drawn.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param texCoords a J3DBuffer object to which a reference will be set.
     * The buffer contains an NIO buffer of 2*<i>n</i>, 3*<i>n</i> or
     * 4*<i>n</i> float values.
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code>,
     * is not <code>USE_NIO_BUFFER</code>, or is <code>INTERLEAVED</code>.
     *
     * @exception IllegalArgumentException if the java.nio.Buffer
     * contained in the specified J3DBuffer is not a
     * java.nio.FloatBuffer object.
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code>, or if texCoordSet is out of range,
     * or if
     * <code>texCoords.getBuffer().limit() &lt; </code> <i>num_words</i>
     * <code> * (initialTexCoordIndex + validVertexCount)</code>,
     * where <i>num_words</i> is 2, 3, or 4 depending on the vertex
     * texture coordinate format.
     *
     * @exception ArrayIndexOutOfBoundsException if this GeometryArray
     * object is a subclass of IndexedGeometryArray, and any element
     * in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * in the texture coordinate index array is greater than or equal to the
     * number of vertices defined by the texCoords object,
     * <code>texCoords.getBuffer().limit() / </code> <i>num_words</i>.
     *
     * @since Java 3D 1.3
     */
    public void setTexCoordRefBuffer(int texCoordSet, J3DBuffer texCoords) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray86"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;

	if ((format & USE_NIO_BUFFER) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray118"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	((GeometryArrayRetained)this.retained).setTexCoordRefBuffer(
			texCoordSet, texCoords);

    }


    /**
     * Gets the texture coordinate array buffer reference for the specified
     * texture coordinate set.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     *
     * @return the current texture coordinate array buffer reference
     * for the specified texture coordinate set
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code>,
     * is not <code>USE_NIO_BUFFER</code>, or is <code>INTERLEAVED</code>.
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or texCoordSet is out of range.
     *
     * @since Java 3D 1.3
     */
    public J3DBuffer getTexCoordRefBuffer(int texCoordSet) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_READ) &&
		   !this.getCapability(J3D_1_2_ALLOW_REF_DATA_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray87"));
	    }

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;

	if ((format & USE_NIO_BUFFER) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray118"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	return ((GeometryArrayRetained)this.retained).getTexCoordRefBuffer(texCoordSet);	
    }


    /**
     * Sets the float texture coordinate array reference for the specified
     * texture coordinate set to the
     * specified array.  The array contains floating-point <i>s</i>,
     * <i>t</i>, and, optionally, <i>r</i> and <i>q</i> values for each 
     * vertex (for
     * a total of 2*<i>n</i> , 3*<i>n</i> or 4*<i>n</i> values, 
     * where <i>n</i> is
     * the number of vertices).  Only one of
     * <code>texCoordRefFloat</code>, <code>texCoordRef2f</code>, or
     * <code>texCoordRef3f</code> may be non-null (or they may all be
     * null).  An attempt to set more than one of these attributes to
     * a non-null reference will result in an exception being thrown.
     * If all texCoord array references are null and texture
     * coordinates are enabled (that is, the vertexFormat includes
     * <code>TEXTURE_COORDINATE_2</code>,
     * <code>TEXTURE_COORDINATE_3</code>, or
     * <code>TEXTURE_COORDINATE_4</code>), the entire geometry
     * array object is treated as if it were null--any Shape3D or
     * Morph node that uses this geometry array will not be drawn.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param texCoords an array of 2*<i>n</i>, 3*<i>n</i> or
     * 4*<i>n</i> values to
     * which a reference will be set.
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code>,
     * is <code>USE_NIO_BUFFER</code>, or is <code>INTERLEAVED</code>.
     * @exception IllegalArgumentException if the specified array is
     * non-null and any other texCoord reference is also non-null.
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code>, or if texCoordSet is out of range,
     * or if
     * <code>texCoords.length &lt; </code> <i>num_words</i> <code> *
     * (initialTexCoordIndex + validVertexCount)</code>,
     * where <i>num_words</i> is 2, 3, or 4 depending on the vertex
     * texture coordinate format.
     *
     * @exception ArrayIndexOutOfBoundsException if this GeometryArray
     * object is a subclass of IndexedGeometryArray, and any element
     * in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * in the texture coordinate index array is greater than or equal to the
     * number of vertices defined by the texCoords array,
     * <code>texCoords.length / </code> <i>num_words</i>.
     *
     * @since Java 3D 1.2
     */
    public void setTexCoordRefFloat(int texCoordSet, float[] texCoords) {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray86"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	((GeometryArrayRetained)this.retained).setTexCoordRefFloat(
			texCoordSet, texCoords);

	// NOTE: the checks for multiple non-null references, and the
	// array length check need to be done in the retained method
    }


    /**
     * Gets the float texture coordinate array reference for the specified
     * texture coordinate set.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     *
     * @return the current float texture coordinate array reference
     * for the specified texture coordinate set
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code>,
     * is <code>USE_NIO_BUFFER</code>, or is <code>INTERLEAVED</code>.
     *
     * @exception ArrayIndexOutOfBoundsException if none of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or texCoordSet is out of range.
     *
     * @since Java 3D 1.2
     */
    public float[] getTexCoordRefFloat(int texCoordSet) {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_READ) &&
		   !this.getCapability(J3D_1_2_ALLOW_REF_DATA_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray87"));
	    }

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	return ((GeometryArrayRetained)this.retained).getTexCoordRefFloat(
							texCoordSet);
    }


    /**
     * @deprecated As of Java 3D version 1.3, use geometry by-copy
     * for TexCoord2f arrays
     *
     * @since Java 3D 1.2
     */
    public void setTexCoordRef2f(int texCoordSet, TexCoord2f[] texCoords) {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray86"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	if ((format & (TEXTURE_COORDINATE_3 | TEXTURE_COORDINATE_4)) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray94"));

	((GeometryArrayRetained)this.retained).setTexCoordRef2f(
					texCoordSet, texCoords);

	// NOTE: the checks for multiple non-null references, and the
	// array length check need to be done in the retained method
    }


    /**
     * @deprecated As of Java 3D version 1.3, use geometry by-copy
     * for TexCoord2f arrays
     *
     * @since Java 3D 1.2
     */
    public TexCoord2f[] getTexCoordRef2f(int texCoordSet) {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_READ) &&
		   !this.getCapability(J3D_1_2_ALLOW_REF_DATA_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray87"));
	    }

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	return ((GeometryArrayRetained)this.retained).getTexCoordRef2f(
							texCoordSet);
    }


    /**
     * @deprecated As of Java 3D version 1.3, use geometry by-copy
     * for TexCoord3f arrays
     *
     * @since Java 3D 1.2
     */
    public void setTexCoordRef3f(int texCoordSet, TexCoord3f[] texCoords) {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray86"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	if ((format & (TEXTURE_COORDINATE_2 | TEXTURE_COORDINATE_4)) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray95"));

	((GeometryArrayRetained)this.retained).setTexCoordRef3f(
					texCoordSet, texCoords);

	// NOTE: the checks for multiple non-null references, and the
	// array length check need to be done in the retained method
    }


    /**
     * @deprecated As of Java 3D version 1.3, use geometry by-copy
     * for TexCoord3f arrays
     *
     * @since Java 3D 1.2
     */
    public TexCoord3f[] getTexCoordRef3f(int texCoordSet) {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_READ) &&
		   !this.getCapability(J3D_1_2_ALLOW_REF_DATA_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray87"));
	    }

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	if ((format & INTERLEAVED) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));

	return ((GeometryArrayRetained)this.retained).getTexCoordRef3f(
							texCoordSet);
    }


    /**
     * Sets the vertex attribute buffer reference for the specified
     * vertex attribute number to the specified buffer object. The
     * buffer contains a java.nio.FloatBuffer object containing 1, 2,
     * 3, or 4 values for each vertex (for a total of 1*<i>n</i>,
     * 2*<i>n</i>, 3*<i>n</i>, or 4*<i>n</i> values, where <i>n</i> is
     * the number of vertices).
     * If the vertexAttr buffer reference is null and vertex
     * attributes are enabled (that is, the vertexFormat includes
     * <code>VERTEX_ATTRIBUTES</code>), the entire geometry array
     * object is treated as if it were null--any Shape3D node that
     * uses this geometry array will not be drawn.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     *
     * @param vertexAttrs a J3DBuffer object to which a reference will
     * be set.  The buffer contains an NIO buffer of 1*<i>n</i>,
     * 2*<i>n</i>, 3*<i>n</i>, or 4*<i>n</i> float values.
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code>,
     * is not <code>USE_NIO_BUFFER</code>, or is <code>INTERLEAVED</code>.
     *
     * @exception IllegalArgumentException if the java.nio.Buffer
     * contained in the specified J3DBuffer is not a
     * java.nio.FloatBuffer object.
     *
     * @exception ArrayIndexOutOfBoundsException if vertexAttrNum is out of
     * range, or if
     * <code>vertexAttrs.getBuffer().limit() &lt; </code> <i>num_words</i>
     * <code> * (initialVertexAttrIndex + validVertexCount)</code>,
     * where <i>num_words</i> is the size of the specified
     * vertexAttrNum (1, 2, 3, or 4).
     *
     * @exception ArrayIndexOutOfBoundsException if this GeometryArray
     * object is a subclass of IndexedGeometryArray, and any element
     * in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * in the vertex attribute index array is greater than or equal to the
     * number of vertices defined by the vertexAttrs object,
     * <code>vertexAttrs.getBuffer().limit() / </code> <i>num_words</i>.
     *
     * @since Java 3D 1.4
     */
    public void setVertexAttrRefBuffer(int vertexAttrNum, J3DBuffer vertexAttrs) {
	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_REF_DATA_WRITE)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray86"));
	    }
	}

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;

	if ((format & USE_NIO_BUFFER) == 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray118"));
	}

	if ((format & INTERLEAVED) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));
	}

	((GeometryArrayRetained)this.retained).setVertexAttrRefBuffer(
		vertexAttrNum, vertexAttrs);
    }


    /**
     * Gets the vertex attribute array buffer reference for the specified
     * vertex attribute number.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     *
     * @return the current vertex attribute array buffer reference
     * for the specified vertex attribute number
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code>,
     * is not <code>USE_NIO_BUFFER</code>, or is <code>INTERLEAVED</code>.
     *
     * @exception ArrayIndexOutOfBoundsException if vertexAttrNum is out
     *  of range.
     *
     * @since Java 3D 1.4
     */
    public J3DBuffer getVertexAttrRefBuffer(int vertexAttrNum) {
	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_REF_DATA_READ) &&
		!this.getCapability(J3D_1_2_ALLOW_REF_DATA_READ)) {

		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray87"));
	    }
	}

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;

	if ((format & USE_NIO_BUFFER) == 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray118"));
	}

	if ((format & INTERLEAVED) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));
	}

	return ((GeometryArrayRetained)this.retained).getVertexAttrRefBuffer(vertexAttrNum);
    }


    /*
     * XXXX: add the following to the javadoc if we ever add double-precision
     * methods for vertex attribtues.
     *
     *-----------------------------------------------------------------
     * Only one of <code>vertexAttrRefFloat</code>, or
     * <code>vertexAttrRefDouble</code> may be non-null (or they may
     * all be null).  An attempt to set more than one of these
     * attributes to a non-null reference will result in an exception
     * being thrown.
     *
     * If all vertexAttr array references are null and vertex
     * ...
     * @exception IllegalArgumentException if the specified array is
     * non-null and any other vertexAttr reference is also non-null.
     * ...
     *-----------------------------------------------------------------
     */

    /**
     * Sets the float vertex attribute array reference for the
     * specified vertex attribute number to the specified array.  The
     * array contains 1, 2, 3, or 4 floating-point values for each
     * vertex (for a total of 1*<i>n</i>, 2*<i>n</i>, 3*<i>n</i>, or
     * 4*<i>n</i> values, where <i>n</i> is the number of vertices).
     *
     * If the vertexAttr array reference is null and vertex
     * attributes are enabled (that is, the vertexFormat includes
     * <code>VERTEX_ATTRIBUTES</code>), the entire geometry array
     * object is treated as if it were null--any Shape3D node that
     * uses this geometry array will not be drawn.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     *
     * @param vertexAttrs an array of 1*<i>n</i>, 2*<i>n</i>,
     * 3*<i>n</i>, or 4*<i>n</i> values to which a reference will be
     * set.
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code>,
     * is <code>USE_NIO_BUFFER</code>, or is <code>INTERLEAVED</code>.
     *
     * @exception ArrayIndexOutOfBoundsException if vertexAttrNum is
     * out of range, or if
     * <code>vertexAttrs.length &lt; </code> <i>num_words</i> <code> *
     * (initialVertexAttrIndex + validVertexCount)</code>,
     * where <i>num_words</i> is the size of the specified
     * vertexAttrNum (1, 2, 3, or 4).
     *
     * @exception ArrayIndexOutOfBoundsException if this GeometryArray
     * object is a subclass of IndexedGeometryArray, and any element
     * in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * in the vertex attribute index array is greater than or equal to the
     * number of vertices defined by the vertexAttrs array,
     * <code>vertexAttrs.length / </code> <i>num_words</i>.
     *
     * @since Java 3D 1.4
     */
    public void setVertexAttrRefFloat(int vertexAttrNum, float[] vertexAttrs) {

	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_REF_DATA_WRITE)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray86"));
	    }
	}

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));
	}

	if ((format & USE_NIO_BUFFER) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));
	}

	if ((format & INTERLEAVED) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));
	}

	((GeometryArrayRetained)this.retained).setVertexAttrRefFloat(
		vertexAttrNum, vertexAttrs);

	// NOTE: the checks for multiple non-null references, and the
	// array length check need to be done in the retained method
    }


    /**
     * Gets the float vertex attribute array reference for the specified
     * vertex attribute number.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     *
     * @return the current float vertex attribute array reference
     * for the specified vertex attribute number
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE</code>,
     * is <code>USE_NIO_BUFFER</code>, or is <code>INTERLEAVED</code>.
     *
     * @exception ArrayIndexOutOfBoundsException if vertexAttrNum is
     * out of range.
     *
     * @since Java 3D 1.4
     */
    public float[] getVertexAttrRefFloat(int vertexAttrNum) {

	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_REF_DATA_READ) &&
		!this.getCapability(J3D_1_2_ALLOW_REF_DATA_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray87"));
	    }
	}

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE) == 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray83"));
	}

	if ((format & USE_NIO_BUFFER) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));
	}

	if ((format & INTERLEAVED) != 0) {
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray84"));
	}

	return ((GeometryArrayRetained)this.retained).getVertexAttrRefFloat(
		vertexAttrNum);
    }


    /**
     * Sets the interleaved vertex array reference to the specified
     * array.  The vertex components must be stored in a predetermined
     * order in the array.  The order is: texture coordinates, colors,
     * normals, and positional coordinates.
     * Vertex attributes are not supported in interleaved mode.
     * In the case of texture
     * coordinates, the values for each texture coordinate set
     * are stored in order from 0 through texCoordSetCount-1.  Only those
     * components that are enabled appear in the vertex.  The number
     * of words per vertex depends on which vertex components are
     * enabled.  Texture coordinates, if enabled, use 2 words per
     * texture coordinate set per vertex for
     * <code>TEXTURE_COORDINATE_2</code>, 3 words per texture
     * coordinate set per vertex for
     * <code>TEXTURE_COORDINATE_3</code> or 4 words per texture
     * coordinate set per vertex for
     * <code>TEXTURE_COORDINATE_4</code>.  Colors, if enabled, use 3
     * words per vertex for <code>COLOR_3</code> or 4 words per vertex
     * for <code>COLOR_4</code>.  Normals, if enabled, use 3 words per
     * vertex.  Positional coordinates, which are always enabled, use
     * 3 words per vertex.  For example, the format of interleaved
     * data for a GeometryArray object whose vertexFormat includes
     * <code>COORDINATES</code>, <code>COLOR_3</code>, and
     * <code>NORMALS</code> would be: <i>red</i>, <i>green</i>,
     * <i>blue</i>, <i>Nx</i>, <i>Ny</i>, <i>Nz</i>, <i>x</i>,
     * <i>y</i>, <i>z</i>.  All components of a vertex are stored in
     * adjacent memory locations.  The first component of vertex 0 is
     * stored beginning at index 0 in the array.  The first component
     * of vertex 1 is stored beginning at index
     * <i>words_per_vertex</i> in the array.  The total number of
     * words needed to store <i>n</i> vertices is
     * <i>words_per_vertex</i>*<i>n</i>.
     *
     * @param vertexData an array of vertex values to which a
     * reference will be set.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>INTERLEAVED</code>
     * or is <code>USE_NIO_BUFFER</code>.
     *
     * @exception ArrayIndexOutOfBoundsException if
     * <code>vertexData.length</code> &lt; <i>words_per_vertex</i> *
     * (<code>initialVertexIndex + validVertexCount</code>),
     * where <i>words_per_vertex</i> depends on which formats are enabled.
     *
     * @exception ArrayIndexOutOfBoundsException if this GeometryArray
     * object is a subclass of IndexedGeometryArray, and any element
     * in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * in the index array associated with any of the enabled vertex
     * components (coord, color, normal, texcoord) is greater than or
     * equal to the number of vertices defined by the vertexData
     * array,
     * <code>vertexData.length / </code> <i>words_per_vertex</i>.
     *
     * @since Java 3D 1.2
     */
    public void setInterleavedVertices(float[] vertexData) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray86"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & INTERLEAVED) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray85"));

	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	((GeometryArrayRetained)this.retained).setInterleavedVertices(vertexData);

	// NOTE: the array length check needs to be done in the retained method
    }


    /**
     * Gets the interleaved vertices array reference.
     * @return the current interleaved vertices array reference.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>INTERLEAVED</code>
     * or is <code>USE_NIO_BUFFER</code>.
     *
     * @since Java 3D 1.2
     */
    public float[] getInterleavedVertices() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_READ) &&
		   !this.getCapability(J3D_1_2_ALLOW_REF_DATA_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray87"));
	    }

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & INTERLEAVED) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray85"));


	if ((format & USE_NIO_BUFFER) != 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray119"));

	return ((GeometryArrayRetained)this.retained).getInterleavedVertices();
    }

    /**
     * Sets the interleaved vertex buffer reference to the specified
     * buffer object. The buffer must contain a java.nio.FloatBuffer object.
     * The vertex components must be stored in a predetermined
     * order in the buffer.  The order is: texture coordinates, colors,
     * normals, and positional coordinates.
     * Vertex attributes are not supported in interleaved mode.
     * In the case of texture
     * coordinates, the values for each texture coordinate set
     * are stored in order from 0 through texCoordSetCount-1.  Only those
     * components that are enabled appear in the vertex.  The number
     * of words per vertex depends on which vertex components are
     * enabled.  Texture coordinates, if enabled, use 2 words per
     * texture coordinate set per vertex for
     * <code>TEXTURE_COORDINATE_2</code>, 3 words per texture
     * coordinate set per vertex for
     * <code>TEXTURE_COORDINATE_3</code> or 4 words per texture
     * coordinate set per vertex for
     * <code>TEXTURE_COORDINATE_4</code>.  Colors, if enabled, use 3
     * words per vertex for <code>COLOR_3</code> or 4 words per vertex
     * for <code>COLOR_4</code>.  Normals, if enabled, use 3 words per
     * vertex.  Positional coordinates, which are always enabled, use
     * 3 words per vertex.  For example, the format of interleaved
     * data for a GeometryArray object whose vertexFormat includes
     * <code>COORDINATES</code>, <code>COLOR_3</code>, and
     * <code>NORMALS</code> would be: <i>red</i>, <i>green</i>,
     * <i>blue</i>, <i>Nx</i>, <i>Ny</i>, <i>Nz</i>, <i>x</i>,
     * <i>y</i>, <i>z</i>.  All components of a vertex are stored in
     * adjacent memory locations.  The first component of vertex 0 is
     * stored beginning at index 0 in the buffer.  The first component
     * of vertex 1 is stored beginning at index
     * <i>words_per_vertex</i> in the buffer.  The total number of
     * words needed to store <i>n</i> vertices is
     * <i>words_per_vertex</i>*<i>n</i>.
     *
     * @param vertexData a J3DBuffer object to which a reference will be set.
     * The buffer contains an NIO float buffer of
     * <i>words_per_vertex</i>*<i>n</i> values.
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>INTERLEAVED</code>
     * or is not <code>USE_NIO_BUFFER</code>.
     *
     * @exception IllegalArgumentException if the java.nio.Buffer
     * contained in the specified J3DBuffer is not a
     * java.nio.FloatBuffer object.
     *
     * @exception ArrayIndexOutOfBoundsException if
     * <code>vertexData.getBuffer().limit()</code> &lt; <i>words_per_vertex</i> *
     * (<code>initialVertexIndex + validVertexCount</code>),
     * where <i>words_per_vertex</i> depends on which formats are enabled.
     *
     * @exception ArrayIndexOutOfBoundsException if this GeometryArray
     * object is a subclass of IndexedGeometryArray, and any element
     * in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * in the index array associated with any of the enabled vertex
     * components (coord, color, normal, texcoord) is greater than or
     * equal to the number of vertices defined by the vertexData
     * object,
     * <code>vertexData.getBuffer().limit() / </code> <i>words_per_vertex</i>.
     *
     * @since Java 3D 1.3
     */
    public void setInterleavedVertexBuffer(J3DBuffer vertexData) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray86"));

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & INTERLEAVED) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray85"));


	if ((format & USE_NIO_BUFFER) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray118"));

	((GeometryArrayRetained)this.retained).setInterleavedVertexBuffer(vertexData);

    }


    /**
     * Gets the interleaved vertex array buffer reference.
     * @return the current interleaved vertex array buffer reference.
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>INTERLEAVED</code>
     * or is not <code>USE_NIO_BUFFER</code>.
     *
     * @since Java 3D 1.3
     */
    public J3DBuffer getInterleavedVertexBuffer() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_READ) &&
		   !this.getCapability(J3D_1_2_ALLOW_REF_DATA_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray87"));
	    }

	int format = ((GeometryArrayRetained)this.retained).vertexFormat;
	if ((format & INTERLEAVED) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray85"));

	if ((format & USE_NIO_BUFFER) == 0)
	    throw new IllegalStateException(J3dI18N.getString("GeometryArray118"));

	return ((GeometryArrayRetained)this.retained).getInterleavedVertexBuffer();

    }
}
