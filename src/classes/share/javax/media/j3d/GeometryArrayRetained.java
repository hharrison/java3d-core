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

import com.sun.j3d.internal.Distance;
import javax.vecmath.*;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Vector;
import java.util.Enumeration;
import com.sun.j3d.internal.ByteBufferWrapper;
import com.sun.j3d.internal.BufferWrapper;
import com.sun.j3d.internal.FloatBufferWrapper;
import com.sun.j3d.internal.DoubleBufferWrapper;


/**
 * The GeometryArray object contains arrays of positional coordinates,
 * colors, normals and/or texture coordinates that describe
 * point, line, or surface geometry.  It is extended to create
 * the various primitive types (e.g., lines, triangle_strips, etc.)
 */

abstract class GeometryArrayRetained extends GeometryRetained{

    // XXXX: Memory footprint reduction. Should have separate object to
    //       to contain specific data such as a ByRef object for
    //       all ByRef related data. So that incases where no
    //       ByRef is needed, the ByRef object reference is
    //       set to null. Hence saving memory!
    //       Need object such as Texture, D3d and ByRef ...
    // 


    // Contains a bitset indicating which components are present
    int vertexFormat;

    // Whether this geometry was ever rendered as transparent
    int c4fAllocated =  0;

    // Total Number of vertices
    int vertexCount;

    // number of vertices used in rendering
    int validVertexCount;

    // The vertex data in packed format
    float vertexData[];

    // vertex data in packed format for each screen in multi-screen situation
    // if alpha values of each vertex are to be updated
    float mvertexData[][];

    //
    // The following offset/stride values are internally computed
    // from the format
    //

    // Stride (in words) from one vertex to the next
    int stride;

    // Stride (in words) from one texture coordinate to the next
    int texCoordStride;   

    // Offset (in words) within each vertex of the coordinate position
    int coordinateOffset;

    // Offset (in words) within each vertex of the normal
    int normalOffset;

    // Offset (in words) within each vertex of the color
    int colorOffset;

    // Offset (in words) within each vertex of the texture coordinate
    int textureOffset;

    // Offset (in words) within each vertex of each vertex attribute
    int[] vertexAttrOffsets;
    
    // Stride (size) of all vertex attributes
    int vertexAttrStride;

    // alpha value for transparency and texture blending
    float[] lastAlpha = new float[1];
    float lastScreenAlpha = -1;

    int colorChanged = 0;

    // true if alpha value from transparencyAttrubute has changed
    boolean alphaChanged = false;
    
    // byte to float scale factor
    static final float ByteToFloatScale = 1.0f/255.0f;

    // float to byte scale factor
    static final float FloatToByteScale = 255.0f;

    // Set flag indicating that we are in the updater.  This flag
    // can be used by the various setRef methods to inhibit any
    // update messages
    boolean inUpdater = false;

    // Array List used for messages
    ArrayList gaList = new ArrayList(1);


    // Target threads to be notified when morph changes
    static final int targetThreads = (J3dThread.UPDATE_RENDER |
				      J3dThread.UPDATE_GEOMETRY);

    // used for byReference geometry
    float[] floatRefCoords = null;
    double[] doubleRefCoords = null;
    Point3d[] p3dRefCoords = null;
    Point3f[] p3fRefCoords = null;

    // Used for NIO buffer geometry
    J3DBuffer coordRefBuffer = null;
    FloatBufferWrapper floatBufferRefCoords = null;
    DoubleBufferWrapper doubleBufferRefCoords = null;

    // Initial index to use for rendering
    int initialCoordIndex = 0;
    int initialColorIndex = 0;
    int initialNormalIndex = 0;
    int[] initialTexCoordIndex = null;
    int[] initialVertexAttrIndex = null;
    int initialVertexIndex = 0;


    // used for byReference colors
    float[] floatRefColors = null;
    byte[] byteRefColors = null;
    Color3f[] c3fRefColors = null;
    Color4f[] c4fRefColors = null;
    Color3b[] c3bRefColors = null;
    Color4b[] c4bRefColors = null;

    // Used for NIO buffer colors
    J3DBuffer colorRefBuffer = null;
    FloatBufferWrapper floatBufferRefColors = null;
    ByteBufferWrapper byteBufferRefColors = null;

    // flag to indicate if the "by reference" component is already set
    int vertexType = 0;
    static  final int PF    = 0x1;
    static  final int PD    = 0x2;
    static  final int P3F   = 0x4;
    static  final int P3D   = 0x8;
    static final int VERTEX_DEFINED = PF | PD | P3F | P3D;


    static final int CF  = 0x10;
    static final int CUB = 0x20;
    static final int C3F = 0x40;
    static final int C4F = 0x80;
    static final int C3UB  = 0x100;
    static final int C4UB = 0x200;
    static final int COLOR_DEFINED = CF | CUB | C3F | C4F| C3UB | C4UB;
    
    static final int NF = 0x400;
    static final int N3F = 0x800;
    static final int NORMAL_DEFINED = NF | N3F;
    
    static final int TF = 0x1000;
    static final int T2F = 0x2000;
    static final int T3F = 0x4000;
    static final int TEXCOORD_DEFINED = TF | T2F | T3F;
    
    static final int AF = 0x8000;
    static final int VATTR_DEFINED = AF;
    
    // Flag word indicating the type of by-ref texCoord. We will copy this to
    // the vertexType field only when the references for all texture coordinate
    // sets are set to non-null values.
    private int texCoordType = 0;
    
    // Flag word indicating the type of by-ref vertex attr. We will copy this to
    // the vertexType field only when the references for all vertex attrs
    // are set to non-null values.
    private int vertexAttrType = 0;

    // flag for execute geometry array when by reference
    static final int COORD_FLOAT  = 0x01;
    static final int COORD_DOUBLE = 0x02;
    static final int COLOR_FLOAT  = 0x04;
    static final int COLOR_BYTE   = 0x08;
    static final int NORMAL_FLOAT = 0x10;
    static final int TEXCOORD_FLOAT = 0x20; 
    static final int VATTR_FLOAT = 0x40;


    // used by "by reference" normals
    float[] floatRefNormals = null;
    Vector3f[] v3fRefNormals = null;

    // Used for NIO buffer normals
    J3DBuffer normalRefBuffer = null;
    FloatBufferWrapper floatBufferRefNormals = null;

    // used for "by reference" vertex attrs
    float[][] floatRefVertexAttrs = null;

    // Used for NIO buffer vertex attrs
    J3DBuffer[] vertexAttrsRefBuffer = null;
    FloatBufferWrapper[] floatBufferRefVertexAttrs = null;
    Object[] nioFloatBufferRefVertexAttrs = null;

    // used by "by reference" tex coords
    Object[] refTexCoords = null;
    TexCoord2f[] t2fRefTexCoords = null;
    TexCoord3f[] t3fRefTexCoords = null;

    // Used for NIO buffer tex coords
    Object[] refTexCoordsBuffer = null;
    //FloatBufferWrapper[] floatBufferRefTexCoords = null;


    // used by interleaved array
    float[] interLeavedVertexData = null;

    // used by interleaved NIO buffer
    J3DBuffer interleavedVertexBuffer = null;
    FloatBufferWrapper interleavedFloatBufferImpl = null;

    // pointers used, when transparency is turned on
    // or when its an object such as C3F, P3F etc ..
    float[] mirrorFloatRefCoords = null;
    double[] mirrorDoubleRefCoords = null;
    float[] mirrorFloatRefNormals = null;
    float[][] mirrorFloatRefVertexAttrs = null;
    float[] mirrorFloatRefTexCoords = null;
    Object[] mirrorRefTexCoords = null;

    float[][] mirrorFloatRefColors = new float[1][];
    byte[][] mirrorUnsignedByteRefColors= new byte[1][];
    float[][] mirrorInterleavedColorPointer = null;

    // boolean to determine if a mirror was allocated
    int mirrorVertexAllocated = 0;
    int mirrorColorAllocated = 0;
    boolean mirrorNormalAllocated = false;

    // Some dirty bits for GeometryArrays
    static final int COORDINATE_CHANGED 	= 0x01;
    static final int NORMAL_CHANGED 		= 0x02;
    static final int COLOR_CHANGED 		= 0x04;
    static final int TEXTURE_CHANGED 		= 0x08;
    static final int BOUNDS_CHANGED 		= 0x10;
    static final int INDEX_CHANGED 		= 0x20;    
    static final int STRIPCOUNT_CHANGED 	= 0x40;
    static final int VATTR_CHANGED 		= 0x80;
    static final int VERTEX_CHANGED             = COORDINATE_CHANGED |
                                                  NORMAL_CHANGED |
                                                  COLOR_CHANGED |
                                                  TEXTURE_CHANGED |
                                                  VATTR_CHANGED;

    static final int defaultTexCoordSetMap[] = {0};
    int texCoordSetCount = 0;
    int [] texCoordSetMap = null;

    // this array contains offset to the texCoord data for each
    // texture unit.  -1 means no corresponding texCoord data offset
    int [] texCoordSetMapOffset = null;

    // Vertex attribute information
    int vertexAttrCount = 0;
    int[] vertexAttrSizes = null;


    // This point to a list of VertexBuffers in a Vector structure
    // Each element correspond to a D3D context that create this VB.
    // Note that this GeometryArray can be used by multiple ctx.
    long pVertexBuffers = 0;
    int dirtyFlag;

    // each bit corresponds to a unique renderer if shared context
    // or a unique canvas otherwise
    int resourceCreationMask = 0x0;

    // Fix for Issue 5
    //
    // Replace the per-canvas reference count with a per-RenderBin set
    // of users.  The per-RenderBin set of users of this display list
    // is defined as a HashMap where:
    //
    //   key   = the RenderBin
    //   value = a set of RenderAtomListInfo objects using this
    //           geometry array for display list purposes
    private HashMap dlistUsers = null;

    // timestamp used to create display list. This is either
    // one per renderer for useSharedCtx, or one per Canvas for non-shared
    // ctx
    private long[] timeStampPerDlist = new long[2];

    // Unique display list Id, if this geometry is shared
    int dlistId = -1;
    Integer dlistObj = null;
    
    // A list of pre-defined bits to indicate which component
    // in this Texture object changed.
    //    static final int DLIST_CREATE_CHANGED      = 0x01;
    static final int INIT_MIRROR_GEOMETRY      = 0x02;


    // A list of Universes that this Geometry is referenced in Morph from
    ArrayList morphUniverseList = null;

    // A list of ArrayLists which contain all the MorphRetained objects
    // refering to this geometry.  Each list corresponds to the universe
    // above.
    ArrayList morphUserLists = null;

    // The following variables are only used in compile mode

    // Offset of a geometry array into the merged array
    int[] geoOffset;    

    // vertexcount of a geometry array in a merge array
    int[] compileVcount;

    boolean isCompiled = false;

    boolean isShared = false;

    IndexedGeometryArrayRetained cloneSourceArray = null;

//     private MemoryFreeList pickVectorFreelist =
//     FreeListManager.getFreeList(FreeListManager.PICKVECTOR);

    static final double EPS = 1.0e-13;

    void freeD3DArray(boolean deleteVB) {
        assert VirtualUniverse.mc.isD3D();
        Pipeline.getPipeline().freeD3DArray(this, deleteVB);
    }

    GeometryArrayRetained() {
	dirtyFlag = INDEX_CHANGED|VERTEX_CHANGED; 
        lastAlpha[0] = 1.0f;
    }


    void setLive(boolean inBackgroundGroup, int refCount) {
	dirtyFlag = VERTEX_CHANGED|INDEX_CHANGED;	    
        isEditable = !isWriteStatic();
        super.doSetLive(inBackgroundGroup, refCount);
	super.markAsLive();
	// Send message to RenderingAttribute structure to obtain a dlistId
	//	System.out.println("Geometry - "+this+"refCount = "+this.refCount);
	if (this.refCount > 1) {
	    // Send to rendering attribute structure,
	    /*
	    J3dMessage createMessage = VirtualUniverse.mc.getMessage();
	    createMessage.threads = J3dThread.UPDATE_RENDERING_ATTRIBUTES;
	    createMessage.type = J3dMessage.GEOMETRYARRAY_CHANGED;
	    createMessage.universe = null;
	    createMessage.args[0] = this;
	    createMessage.args[1]= new Integer(DLIST_CREATE_CHANGED);
	    VirtualUniverse.mc.processMessage(createMessage);	    
	    */
	    isShared = true;
	} // Clone geometry only for the first setLive
	else {
	    // If geometry is indexed and use_index_coord is false, unindexify
	    // otherwise, set mirrorGeometry to null (from previous clearLive)
	    if (this instanceof IndexedGeometryArrayRetained) { 
		// Send to rendering attribute structure,
		J3dMessage createMessage = VirtualUniverse.mc.getMessage();
		createMessage.threads = J3dThread.UPDATE_RENDERING_ATTRIBUTES;
		createMessage.type = J3dMessage.GEOMETRY_CHANGED;
		createMessage.universe = null;
		createMessage.args[0] = null;
		createMessage.args[1]= this;
		createMessage.args[2]= new Integer(INIT_MIRROR_GEOMETRY);
		VirtualUniverse.mc.processMessage(createMessage);
	    }
	}
	    
    }

    void clearLive(int refCount) {
	super.clearLive(refCount);

	if (this.refCount <= 0) {
	    if (pVertexBuffers != 0) {
		J3dMessage renderMessage = VirtualUniverse.mc.getMessage();
		renderMessage.threads = J3dThread.RENDER_THREAD;
		renderMessage.type = J3dMessage.RENDER_IMMEDIATE;
		renderMessage.universe = null;
		renderMessage.view = null;
		renderMessage.args[0] = null;
		renderMessage.args[1] = this;
		// Any one renderer is fine since VB store the ctx
		// where it is created.
		Enumeration e = Screen3D.deviceRendererMap.elements();
		Renderer rdr = (Renderer) e.nextElement();
		rdr.rendererStructure.addMessage(renderMessage);
		VirtualUniverse.mc.setWorkForRequestRenderer();
	    }
	    isShared = false;
	}
    }

    void computeBoundingBox() {

	//	System.out.println("computeBoundingBox ....");

	if ((vertexFormat & GeometryArray.BY_REFERENCE) == 0) {
	    // by copy	    
	    computeBoundingBox(initialVertexIndex, vertexData);
	    
	} else if ((vertexFormat & GeometryArray.USE_NIO_BUFFER) != 0) { // USE_NIO_BUFFER 
	    //System.out.println("vertexFormat & GeometryArray.USE_NIO_BUFFER");
	    if((vertexFormat & GeometryArray.INTERLEAVED) != 0) {
		computeBoundingBox(initialCoordIndex, interleavedFloatBufferImpl);
	    } else if((vertexType & PF) != 0) {
		computeBoundingBox(floatBufferRefCoords);
	    } else if((vertexType & PD) != 0) {
		computeBoundingBox(doubleBufferRefCoords);
	    }
	    
	} else if ((vertexFormat & GeometryArray.INTERLEAVED) != 0) {
	    //System.out.println("vertexFormat & GeometryArray.INTERLEAVED");
	    computeBoundingBox(initialCoordIndex, interLeavedVertexData);
	} else if ((vertexType & PF) != 0) {
	    //System.out.println("vertexType & PF");
	    computeBoundingBox(floatRefCoords);
	} else if ((vertexType & P3F) != 0) {
	    //System.out.println("vertexType & P3F");
	    computeBoundingBox(p3fRefCoords);
	} else if ((vertexType & P3D) != 0) {
	    //System.out.println("vertexType & P3D");
	    computeBoundingBox(p3dRefCoords);
	} else if ((vertexType & PD) != 0) {
	    //System.out.println("vertexType & PD");
	    computeBoundingBox(doubleRefCoords);
	}
    }


    // NullGeometry is true only for byRef case
    void processCoordsChanged(boolean nullGeo) {
	
	/*
	  System.out.println("processCoordsChanged : nullGeo " + nullGeo);
	  System.out.println("Before :processCoordsChanged : geoBounds ");
	  System.out.println(geoBounds);
	*/
	if (nullGeo) {
	    synchronized(geoBounds) {
		geoBounds.setLower(-1.0, -1.0, -1.0);
		geoBounds.setUpper(1.0, 1.0, 1.0);
		boundsDirty = false;
	    }
	    synchronized(centroid) {
		recompCentroid = false;
		this.centroid.set(geoBounds.getCenter());
	    }

	}
	else {
	    // re-compute centroid if used 
	    synchronized(centroid) {
		recompCentroid = true;
	    }
	    
	    synchronized(geoBounds) {
		boundsDirty = true;
		computeBoundingBox();
	    }
	   
	    /*
	      System.out.println("After :processCoordsChanged : geoBounds ");
	      System.out.println(geoBounds);
	    */
	}
    }

    
    void computeBoundingBox(int vIndex, float[] vdata) {
	int i, offset;
	double xmin, xmax, ymin, ymax, zmin, zmax;


	//System.out.println("Before : computeBoundingBox : geoBounds ");
	//  System.out.println(geoBounds);

	synchronized(geoBounds) {

	    // If autobounds compute is false  then return
	    // It is possible that user call getBounds() before
	    // this Geometry add to live scene graph.
	    if ((computeGeoBounds == 0) && (refCount > 0)) {
	    	return;
	    }
	    if (!boundsDirty)
		return;

	    // Initial offset
	    offset = vIndex * stride+coordinateOffset;
	    // Compute the bounding box
	    xmin = xmax = vdata[offset];
	    ymin = ymax = vdata[offset+1];
	    zmin = zmax = vdata[offset+2];
	    offset += stride;
	    for (i=1; i<validVertexCount; i++) {
		if (vdata[offset] > xmax)
		    xmax = vdata[offset];
		if (vdata[offset] < xmin)
		    xmin = vdata[offset];
      
		if (vdata[offset+1] > ymax)
		    ymax = vdata[offset+1];
		if (vdata[offset+1] < ymin)
		    ymin = vdata[offset+1];
   
		if (vdata[offset+2] > zmax)
		    zmax = vdata[offset+2];
		if (vdata[offset+2] < zmin)
		    zmin = vdata[offset+2];
      
		offset += stride;
	    }

	    geoBounds.setUpper(xmax, ymax, zmax);
	    geoBounds.setLower(xmin, ymin, zmin);
	    boundsDirty = false; 
	}
	/*
	  System.out.println("After : computeBoundingBox : geoBounds ");
	  System.out.println(geoBounds);
	*/	
    }

    // Compute boundingbox for interleaved nio buffer
    void computeBoundingBox(int vIndex,   FloatBufferWrapper vdata) {
	int i, offset;
	double xmin, xmax, ymin, ymax, zmin, zmax;


	synchronized(geoBounds) {
	    // If autobounds compute is false  then return
	    if ((computeGeoBounds == 0) && (refCount > 0)) {
		return;
	    }

	    if (!boundsDirty)
		return;

	    // Initial offset	    
	    offset = vIndex * stride+coordinateOffset;
	    // Compute the bounding box
	    xmin = xmax = vdata.get(offset);
	    ymin = ymax = vdata.get(offset+1);
	    zmin = zmax = vdata.get(offset+2);
	    offset += stride;
	    for (i=1; i<validVertexCount; i++) {
		if (vdata.get(offset) > xmax)
		    xmax = vdata.get(offset);
		if (vdata.get(offset) < xmin)
		    xmin = vdata.get(offset);
      
		if (vdata.get(offset+1) > ymax)
		    ymax = vdata.get(offset+1);
		if (vdata.get(offset+1) < ymin)
		    ymin = vdata.get(offset+1);
   
		if (vdata.get(offset+2) > zmax)
		    zmax = vdata.get(offset+2);
		if (vdata.get(offset+2) < zmin)
		    zmin = vdata.get(offset+2);
      
		offset += stride;
	    }

	    geoBounds.setUpper(xmax, ymax, zmax);
	    geoBounds.setLower(xmin, ymin, zmin);
	    boundsDirty = false; 
	}
    }


    // compute bounding box for coord with noi buffer
    void computeBoundingBox( DoubleBufferWrapper buffer) {
	int i, j, k, sIndex;
	double xmin, xmax, ymin, ymax, zmin, zmax;
	
	synchronized(geoBounds) {
	    // If autobounds compute is false  then return
	    if ((computeGeoBounds == 0) && (refCount > 0)) {
		return;
	    }

	    if (!boundsDirty) 
		return;
	    
	    sIndex = initialCoordIndex;
	    int maxIndex = 3*validVertexCount;

	    // Compute the bounding box
	    xmin = xmax = buffer.get(sIndex++);
	    ymin = ymax = buffer.get(sIndex++);
	    zmin = zmax = buffer.get(sIndex++);
	
	    for (i=sIndex; i<maxIndex; i+=3) {
		j = i + 1;
		k = i + 2;
		
		if (buffer.get(i) > xmax)
		    xmax = buffer.get(i);
		if (buffer.get(i) < xmin)
		    xmin = buffer.get(i);
	    
		if (buffer.get(j) > ymax)
		    ymax = buffer.get(j);
		if (buffer.get(j) < ymin)
		    ymin = buffer.get(j);
   
		if (buffer.get(k) > zmax)
		    zmax = buffer.get(k);
		if (buffer.get(k) < zmin)
		    zmin = buffer.get(k);
      
	    }
	    geoBounds.setUpper(xmax, ymax, zmax);
	    geoBounds.setLower(xmin, ymin, zmin);
	    boundsDirty = false; 
	}
    }

    // compute bounding box for coord with noi buffer
    void computeBoundingBox( FloatBufferWrapper buffer) {
	int i, j, k, sIndex;
	double xmin, xmax, ymin, ymax, zmin, zmax;


	synchronized(geoBounds) {
	    // If autobounds compute is false  then return
	    if ((computeGeoBounds == 0) && (refCount > 0)) {
		return;
	    }

	    if (!boundsDirty)
		return;


	    sIndex = initialCoordIndex;
	    int maxIndex = 3*validVertexCount;

	    // Compute the bounding box
	    xmin = xmax = buffer.get(sIndex++);
	    ymin = ymax = buffer.get(sIndex++);
	    zmin = zmax = buffer.get(sIndex++);
	
	    for (i=sIndex; i<maxIndex; i+=3) {
		j = i + 1;
		k = i + 2;
		
		if (buffer.get(i) > xmax)
		    xmax = buffer.get(i);
		if (buffer.get(i) < xmin)
		    xmin = buffer.get(i);
	    
		if (buffer.get(j) > ymax)
		    ymax = buffer.get(j);
		if (buffer.get(j) < ymin)
		    ymin = buffer.get(j);
   
		if (buffer.get(k) > zmax)
		    zmax = buffer.get(k);
		if (buffer.get(k) < zmin)
		    zmin = buffer.get(k);
      
	    }
	    geoBounds.setUpper(xmax, ymax, zmax);
	    geoBounds.setLower(xmin, ymin, zmin);
	    boundsDirty = false; 
	}
    }

    void computeBoundingBox(float[] coords) {
	// System.out.println("GeometryArrayRetained : computeBoundingBox(float[] coords)"); 
	int i, j, k, sIndex;
	double xmin, xmax, ymin, ymax, zmin, zmax;

	synchronized(geoBounds) {
	    // If autobounds compute is false  then return
	    if ((computeGeoBounds == 0) && (refCount > 0)) {
		return;
	    }

	    if (!boundsDirty)
		return;

	    sIndex = initialCoordIndex;
	    int maxIndex = 3*validVertexCount;
	
	// Compute the bounding box
	    xmin = xmax = coords[sIndex++];
	    ymin = ymax = coords[sIndex++];
	    zmin = zmax = coords[sIndex++];
	
	    for (i=sIndex; i<maxIndex; i+=3) {
		j = i + 1;
		k = i + 2;
		
		if (coords[i] > xmax)
		    xmax = coords[i];
		if (coords[i] < xmin)
		    xmin = coords[i];
	    
		if (coords[j] > ymax)
		    ymax = coords[j];
		if (coords[j] < ymin)
		    ymin = coords[j];
   
		if (coords[k] > zmax)
		    zmax = coords[k];
		if (coords[k] < zmin)
		    zmin = coords[k];
      
	    }
	    geoBounds.setUpper(xmax, ymax, zmax);
	    // System.out.println("max(" + xmax + ", " + ymax + ", " + zmax + ")"); 
	    geoBounds.setLower(xmin, ymin, zmin);
	    // System.out.println("min(" + xmin + ", " + ymin + ", " + zmin + ")"); 

	    boundsDirty = false; 
	}

    }

    void computeBoundingBox(double[] coords) {
	int i, j, k, sIndex;
	double xmin, xmax, ymin, ymax, zmin, zmax;

	synchronized(geoBounds) {
	    // If autobounds compute is false  then return
	    if ((computeGeoBounds == 0) && (refCount > 0)) {
		return;
	    }

	    if (!boundsDirty)
		return;


	    sIndex = initialCoordIndex;
	    int maxIndex = 3*validVertexCount;
	    
	    // Compute the bounding box
	    xmin = xmax = coords[sIndex++];
	    ymin = ymax = coords[sIndex++];
	    zmin = zmax = coords[sIndex++];
	
	    for (i=sIndex; i<maxIndex; i+=3) {
		j = i + 1;
		k = i + 2;
		
		if (coords[i] > xmax)
		    xmax = coords[i];
		if (coords[i] < xmin)
		    xmin = coords[i];
	    
		if (coords[j] > ymax)
		    ymax = coords[j];
		if (coords[j] < ymin)
		    ymin = coords[j];
   
		if (coords[k] > zmax)
		    zmax = coords[k];
		if (coords[k] < zmin)
		    zmin = coords[k];
      
	    }
	    geoBounds.setUpper(xmax, ymax, zmax);
	    geoBounds.setLower(xmin, ymin, zmin);
	    boundsDirty = false; 
	}

    }

    void computeBoundingBox(Point3f[] coords) {
    
	double xmin, xmax, ymin, ymax, zmin, zmax;
	Point3f p;

	synchronized(geoBounds) {
	    // If autobounds compute is false  then return
	    if ((computeGeoBounds == 0) && (refCount > 0)) {
		return;
	    }

	    if (!boundsDirty)
		return;



	// Compute the bounding box
	    xmin = xmax = coords[initialCoordIndex].x;
	    ymin = ymax = coords[initialCoordIndex].y;
	    zmin = zmax = coords[initialCoordIndex].z;

	    for (int i=initialCoordIndex+1; i<validVertexCount; i++) {
		p = coords[i];
		if (p.x > xmax) xmax = p.x;
		if (p.x < xmin) xmin = p.x;
      
		if (p.y > ymax) ymax = p.y;
		if (p.y < ymin) ymin = p.y;
   
		if (p.z > zmax) zmax = p.z;
		if (p.z < zmin) zmin = p.z;
      
	    }
	    geoBounds.setUpper(xmax, ymax, zmax);
	    geoBounds.setLower(xmin, ymin, zmin);
	    boundsDirty = false; 
	}

    }

    void computeBoundingBox(Point3d[] coords) {
    
	double xmin, xmax, ymin, ymax, zmin, zmax;
	Point3d p;

	synchronized(geoBounds) {
	    // If autobounds compute is false  then return
	    if ((computeGeoBounds == 0) && (refCount > 0)) {
		return;
	    }

	    if (!boundsDirty)
		return;


	// Compute the bounding box
	    xmin = xmax = coords[initialCoordIndex].x;
	    ymin = ymax = coords[initialCoordIndex].y;
	    zmin = zmax = coords[initialCoordIndex].z;

	    for (int i=initialCoordIndex+1; i<validVertexCount; i++) {
		p = coords[i];
		if (p.x > xmax) xmax = p.x;
		if (p.x < xmin) xmin = p.x;
      
		if (p.y > ymax) ymax = p.y;
		if (p.y < ymin) ymin = p.y;
   
		if (p.z > zmax) zmax = p.z;
		if (p.z < zmin) zmin = p.z;
      
	    }
	    geoBounds.setUpper(xmax, ymax, zmax);
	    geoBounds.setLower(xmin, ymin, zmin);
	    boundsDirty = false; 
	}

    }
    
    
    synchronized void update() {
    }
    
    void setupMirrorVertexPointer(int vType) {
	int i, index;
	
	switch (vType) { 
	case PF:
	    if (floatRefCoords == null) {
		if ((vertexType & VERTEX_DEFINED) == PF) {
		    vertexType &= ~PF;
		    mirrorFloatRefCoords = null;
		    mirrorVertexAllocated &= ~PF;
		}
	    }
	    else {
		vertexType |= PF;
		mirrorFloatRefCoords = floatRefCoords;
		mirrorVertexAllocated &= ~PF;
	    }

	    break;
	case PD:
	    if (doubleRefCoords == null) {
		if ((vertexType & VERTEX_DEFINED) == PD) {
		    mirrorDoubleRefCoords = null;
		    mirrorVertexAllocated &= ~PD;
		    vertexType &= ~PD;
		}
		vertexType &= ~PD;
	    }
	    else {
		vertexType |= PD;
		mirrorDoubleRefCoords = doubleRefCoords;
		mirrorVertexAllocated &= ~PD;
	    }

	    break;
	case P3F:
	    if (p3fRefCoords == null) {
		vertexType &= ~P3F;
		// Don't set the mirrorFloatRefCoords to null,
		// may be able to re-use
		//	    mirrorFloatRefCoords = null;
	    }
	    else {
		vertexType |= P3F;
		
		if ((mirrorVertexAllocated & PF) == 0) {
		    mirrorFloatRefCoords = new float[vertexCount * 3];
		    mirrorVertexAllocated |= PF;
		}

		index = initialCoordIndex * 3;
		for ( i=initialCoordIndex; i<validVertexCount; i++) {
		    mirrorFloatRefCoords[index++] = p3fRefCoords[i].x;
		    mirrorFloatRefCoords[index++] = p3fRefCoords[i].y;
		    mirrorFloatRefCoords[index++] = p3fRefCoords[i].z;
		}
	    }
	    break;
	case P3D:
	    if (p3dRefCoords == null) {
		vertexType &= ~P3D;
		// Don't set the mirrorDoubleRefCoords to null,
		// may be able to re-use
		//	    mirrorDoubleRefCoords = null;
	    }
	    else {
		vertexType |= P3D;

		if ((mirrorVertexAllocated & PD) == 0) {
		    mirrorDoubleRefCoords = new double[vertexCount * 3];
		    mirrorVertexAllocated |= PD;
		}

		index = initialCoordIndex * 3;
		for ( i=initialCoordIndex; i<validVertexCount; i++) {
		    mirrorDoubleRefCoords[index++] = p3dRefCoords[i].x;
		    mirrorDoubleRefCoords[index++] = p3dRefCoords[i].y;
		    mirrorDoubleRefCoords[index++] = p3dRefCoords[i].z;
		}
	    }
	    break;
	default:
	    break;
	    
	}

    }

    // If turned transparent the first time, then force it to allocate
    void setupMirrorInterleavedColorPointer(boolean force) {
	int index, length, offset;
	int i;

	if (force || (c4fAllocated != 0)) { // Color is present
	    
	    length = 4 * vertexCount;
	    
	    if (mirrorInterleavedColorPointer == null) {
		mirrorInterleavedColorPointer = new float[1][length];
	    }
	    
	    index = 4 * initialVertexIndex;
	    offset = stride * initialVertexIndex + colorOffset;
	    
	    if ((vertexFormat & GeometryArray.USE_NIO_BUFFER) == 0 && 
		interLeavedVertexData != null ) { // java array	    
		if ((vertexFormat  & GeometryArray.WITH_ALPHA) != 0) {

		    for (i = initialVertexIndex; i < validVertexCount; i++) {
			mirrorInterleavedColorPointer[0][index++] = 
			    interLeavedVertexData[offset];
			mirrorInterleavedColorPointer[0][index++] = 
			    interLeavedVertexData[offset+1];
			mirrorInterleavedColorPointer[0][index++] = 
			    interLeavedVertexData[offset+2];
			mirrorInterleavedColorPointer[0][index++] = 
			    interLeavedVertexData[offset+3];
			offset += stride;
		    }
		}
		else {
		    for (i = initialVertexIndex; i < validVertexCount; i++) {
			mirrorInterleavedColorPointer[0][index++] = 
			    interLeavedVertexData[offset];
			mirrorInterleavedColorPointer[0][index++] = 
			    interLeavedVertexData[offset+1];
			mirrorInterleavedColorPointer[0][index++] = 
			    interLeavedVertexData[offset+2];
			mirrorInterleavedColorPointer[0][index++] = 1.0f;
			offset += stride;
		    }
		}

	    } else { // NIO BUFFER
		if ((vertexFormat  & GeometryArray.WITH_ALPHA) != 0 &&
		    interleavedFloatBufferImpl != null) {
		    for (i = initialVertexIndex; i < validVertexCount; i++) {
			interleavedFloatBufferImpl.position(offset); 
			interleavedFloatBufferImpl.get(mirrorInterleavedColorPointer[0],
						       index , 4);
			index += 4;
			offset += stride;
		    }
		}
		else {
		    for (i = initialVertexIndex; i < validVertexCount; i++) {
			interleavedFloatBufferImpl.position(offset);
			interleavedFloatBufferImpl.get(mirrorInterleavedColorPointer[0],
						       index, 3);
			mirrorInterleavedColorPointer[0][index+3] = 1.0f;
			index += 4;
			offset += stride;
			
		    }
		}		
	    }
	    c4fAllocated = GeometryArray.WITH_ALPHA;
	}
    }
    
    // If turned transparent the first time, then force it to allocate
    void setupMirrorColorPointer(int ctype, boolean force) {
	int i, srcIndex = 0, dstIndex = 0;
	int multiplier;

 	if (c4fAllocated == 0 && !force) {
	    multiplier = 3;
	} else {
		
	    // If the first time, we are forced to allocate 4f, then
	    // we need to force the allocation of the colors again
	    // for the case when allocation has previously occurred
	    // only for RGB
	    if (force && (c4fAllocated == 0) &&
		(vertexFormat & GeometryArray.WITH_ALPHA) == 0)  {
		mirrorColorAllocated = 0;
	    }
	    c4fAllocated = GeometryArray.WITH_ALPHA;
	    multiplier = 4;
	}

	if ((vertexFormat & GeometryArray.USE_NIO_BUFFER) == 0) { // java array
	    switch (ctype) { 
	    case CF:
		if (floatRefColors == null) {
		    if ((c4fAllocated == 0) && !force &&
			(vertexType & COLOR_DEFINED) == CF) {
			mirrorFloatRefColors[0] = null;
			mirrorColorAllocated &= ~CF;
		    }
		    vertexType &= ~CF;
		    return;
		}
	
		vertexType |= CF;
		if (c4fAllocated == 0 && !force) {
		    mirrorFloatRefColors[0] = floatRefColors;
		    mirrorColorAllocated &= ~CF;
		}
		else {
 		    if ((mirrorColorAllocated & CF) == 0) {
			mirrorFloatRefColors[0] = new float[4 * vertexCount];
			mirrorColorAllocated |= CF;
		    }

		    if ((vertexFormat & GeometryArray.WITH_ALPHA) == 0) {

			srcIndex = initialColorIndex * 3;
			dstIndex = initialColorIndex * 4;
			
			for (i = initialColorIndex; i < validVertexCount; i++) {
			    mirrorFloatRefColors[0][dstIndex++] = 
				floatRefColors[srcIndex++];
			    mirrorFloatRefColors[0][dstIndex++] = 
				floatRefColors[srcIndex++];
			    mirrorFloatRefColors[0][dstIndex++] = 
				floatRefColors[srcIndex++];
			    mirrorFloatRefColors[0][dstIndex++] = 1.0f;
			}
		    
		    }
		    else {
			srcIndex = initialColorIndex * 4;
			System.arraycopy(floatRefColors, srcIndex, 
					 mirrorFloatRefColors[0], srcIndex, 
					 (4*validVertexCount));
		    }
		}
		break;
	    case CUB: 
		if (byteRefColors == null) {
		    if (c4fAllocated == 0 && !force &&
			((vertexType & COLOR_DEFINED) == CUB) ) {
			mirrorUnsignedByteRefColors[0] = null;
			mirrorColorAllocated &= ~CUB;
		    }
		    vertexType &= ~CUB;
		    return;
		}
		vertexType |= CUB;
		if (c4fAllocated == 0 && !force) {
		    mirrorUnsignedByteRefColors[0] = byteRefColors;
		    mirrorColorAllocated &= ~CUB;;
		}
		else {
		    if ((mirrorColorAllocated & CUB) == 0) {
			mirrorUnsignedByteRefColors[0] = new byte[4 * vertexCount];
			mirrorColorAllocated |= CUB;
		    }
		    if ((vertexFormat & GeometryArray.WITH_ALPHA) == 0) {

			srcIndex = initialColorIndex * 3;
			dstIndex = initialColorIndex * 4;
			
			for (i = initialColorIndex; i < validVertexCount; i++) {
			    mirrorUnsignedByteRefColors[0][dstIndex++] =
				byteRefColors[srcIndex++];
			    mirrorUnsignedByteRefColors[0][dstIndex++] =
				byteRefColors[srcIndex++];
			    mirrorUnsignedByteRefColors[0][dstIndex++] =
				byteRefColors[srcIndex++];
			    mirrorUnsignedByteRefColors[0][dstIndex++] =
				(byte)(255.0);
			}
		    }
		    else {
			srcIndex = initialColorIndex * 4;
			System.arraycopy(byteRefColors, srcIndex, 
					 mirrorUnsignedByteRefColors[0], srcIndex,
					 (4*validVertexCount));
		    }
		}
		
		break;
	    case C3F:
		if (c3fRefColors == null) {
		    vertexType &= ~C3F;
		    return;
		}
		vertexType |=C3F ;

		if ((mirrorColorAllocated & CF) == 0) {
		    mirrorFloatRefColors[0] = new float[vertexCount * multiplier];
		    mirrorColorAllocated |= CF;
		}
		if ((c4fAllocated & GeometryArray.WITH_ALPHA) == 0) {

		    dstIndex = initialColorIndex * 3;
		    for (i = initialColorIndex; i < validVertexCount; i++) {
			mirrorFloatRefColors[0][dstIndex++] = c3fRefColors[i].x;
			mirrorFloatRefColors[0][dstIndex++] = c3fRefColors[i].y;
			mirrorFloatRefColors[0][dstIndex++] = c3fRefColors[i].z;
		    }
		} else {

		    dstIndex = initialColorIndex * 4;
		    for (i = initialColorIndex; i < validVertexCount; i++) {
			mirrorFloatRefColors[0][dstIndex++] = c3fRefColors[i].x;
			mirrorFloatRefColors[0][dstIndex++] = c3fRefColors[i].y;
			mirrorFloatRefColors[0][dstIndex++] = c3fRefColors[i].z;
			mirrorFloatRefColors[0][dstIndex++] = 1.0f;
		    }
		}

		break;
	    case C4F: 
		if (c4fRefColors == null) {
		    vertexType &= ~C4F;
		    return;
		}
		vertexType |=C4F ;

		if ((mirrorColorAllocated & CF) == 0) {
		    mirrorFloatRefColors[0] = new float[vertexCount << 2];
		    mirrorColorAllocated |= CF;
		}

		dstIndex = initialColorIndex * 4;
		for (i = initialColorIndex; i < validVertexCount; i++) {
		    mirrorFloatRefColors[0][dstIndex++] = c4fRefColors[i].x;
		    mirrorFloatRefColors[0][dstIndex++] = c4fRefColors[i].y;
		    mirrorFloatRefColors[0][dstIndex++] = c4fRefColors[i].z;
		    mirrorFloatRefColors[0][dstIndex++] = c4fRefColors[i].w;
		}
		break;
	    case C3UB: 
		if (c3bRefColors == null) {
		    vertexType &= ~C3UB;
		    return;
		}
		vertexType |=C3UB ;

		if ((mirrorColorAllocated & CUB) == 0) {
		    mirrorUnsignedByteRefColors[0] = 
			new byte[vertexCount * multiplier];
		    mirrorColorAllocated |= CUB;
		}
		if ((c4fAllocated & GeometryArray.WITH_ALPHA) == 0) {
		    dstIndex = initialColorIndex * 3;
		    for (i = initialColorIndex; i < validVertexCount; i++) {
			mirrorUnsignedByteRefColors[0][dstIndex++] = c3bRefColors[i].x;
			mirrorUnsignedByteRefColors[0][dstIndex++] = c3bRefColors[i].y;
			mirrorUnsignedByteRefColors[0][dstIndex++] = c3bRefColors[i].z;
		    }
		} else {
		    dstIndex = initialColorIndex * 4;
		    for (i = initialColorIndex; i < validVertexCount; i++) {
			mirrorUnsignedByteRefColors[0][dstIndex++] = c3bRefColors[i].x;
			mirrorUnsignedByteRefColors[0][dstIndex++] = c3bRefColors[i].y;
			mirrorUnsignedByteRefColors[0][dstIndex++] = c3bRefColors[i].z;
			mirrorUnsignedByteRefColors[0][dstIndex++] = (byte)255;
		    }
		}
		break;
	    case C4UB: 
		if (c4bRefColors == null) {
		    vertexType &= ~C4UB;
		    return;
		}
		vertexType |=C4UB ;
		if ((mirrorColorAllocated & CUB) == 0) {
		    mirrorUnsignedByteRefColors[0] = new byte[vertexCount << 2];
		    mirrorColorAllocated |= CUB;
		}

		dstIndex = initialColorIndex * 4;
		for (i = initialColorIndex; i < validVertexCount; i++) {
		    mirrorUnsignedByteRefColors[0][dstIndex++] = c4bRefColors[i].x;
		    mirrorUnsignedByteRefColors[0][dstIndex++] = c4bRefColors[i].y;
		    mirrorUnsignedByteRefColors[0][dstIndex++] = c4bRefColors[i].z;
		    mirrorUnsignedByteRefColors[0][dstIndex++] = c4bRefColors[i].w;
		}
		break;
	    default:
		break;
	    }
	}
	else {  //USE_NIO_BUFFER is set
	    if(	colorRefBuffer == null) {
		if (c4fAllocated == 0 && !force &&
		    (vertexType & COLOR_DEFINED) == CF) {
		    mirrorFloatRefColors[0] = null;
		    mirrorColorAllocated &= ~CF;
		}
		vertexType &= ~CF;
		
		if (c4fAllocated == 0 && !force &&
		    ((vertexType & COLOR_DEFINED) == CUB) ) {
		    mirrorUnsignedByteRefColors[0] = null;
		    mirrorColorAllocated &= ~CUB;
		}
		vertexType &= ~CUB;
		return;
		
	    } else if( floatBufferRefColors != null) {
		vertexType |= CF;
		vertexType &= ~CUB;
		if (c4fAllocated == 0 && !force) {
		    // NOTE: make suren mirrorFloatRefColors[0] is set right
		    mirrorFloatRefColors[0] = null; 
		    mirrorColorAllocated &= ~CF;
		}
		else {
		    if ((mirrorColorAllocated & CF) == 0) {
			mirrorFloatRefColors[0] = new float[4 * vertexCount];
			mirrorColorAllocated |= CF;
		    }
		    floatBufferRefColors.rewind();
		    if ((vertexFormat & GeometryArray.WITH_ALPHA) == 0) {
			srcIndex = initialColorIndex * 3;
			dstIndex = initialColorIndex * 4;
			floatBufferRefColors.position(srcIndex);
			
			for (i = initialColorIndex; i < validVertexCount; i++) {
			    floatBufferRefColors.get(mirrorFloatRefColors[0], dstIndex, 3);
			    mirrorFloatRefColors[0][dstIndex+3] = 1.0f;
			    dstIndex += 4;
			}
		    }
		    else {

			srcIndex = initialColorIndex * 4;
			dstIndex = initialColorIndex * 4;
			floatBufferRefColors.position(srcIndex);
			for (i = initialColorIndex; i < validVertexCount; i++) {
			    floatBufferRefColors.get(mirrorFloatRefColors[0], dstIndex, 4); 
			    dstIndex+= 4;
			}
		    }
		}
	    } else if ( byteBufferRefColors != null) {
		vertexType |= CUB;
		vertexType &= ~CF;
		if (c4fAllocated == 0 && !force) {
		    // NOTE: make sure mirrorUnsignedByteRefColors[0] is set right
		    mirrorUnsignedByteRefColors[0] = null;
		    mirrorColorAllocated &= ~CUB;;
		}
		else {
		    if ((mirrorColorAllocated & CUB) == 0) {
			mirrorUnsignedByteRefColors[0] = new byte[4 * vertexCount];
			mirrorColorAllocated |= CUB;
		    }
		    
		    byteBufferRefColors.rewind();
		    if ((vertexFormat & GeometryArray.WITH_ALPHA) == 0) {
			srcIndex = initialColorIndex * 3;
			dstIndex = initialColorIndex * 4;
			byteBufferRefColors.position(srcIndex);
			for (i = initialColorIndex; i < validVertexCount; i++) {
			    byteBufferRefColors.get(mirrorUnsignedByteRefColors[0],
						    dstIndex, 3);
			    mirrorUnsignedByteRefColors[0][dstIndex+3] = (byte)(255.0);
			    dstIndex += 4;
			}
		    }
		    else {
			srcIndex = initialColorIndex * 4;
			dstIndex = initialColorIndex * 4;
			byteBufferRefColors.position(srcIndex);
			for (i = initialColorIndex; i < validVertexCount; i++) {
			    byteBufferRefColors.get(mirrorUnsignedByteRefColors[0], dstIndex, 4);
			    dstIndex+= 4;
			}
		    }
		} // end of else 
	    }//end of else if ( byteBufferRefColors != null)
	}//end of NIO BUFFER case
	
	colorChanged = 0xffff;
    }


    void setupMirrorNormalPointer(int ntype) {
	int i, index;

	switch (ntype) { 
	case NF: 
	    if (floatRefNormals == null) {
		if ((vertexType & NORMAL_DEFINED) == NF) {
		    vertexType &= ~NF;
		    mirrorFloatRefNormals = null;
		    mirrorNormalAllocated = false;
		}
	    }
	    else {
		vertexType |= NF;
		mirrorFloatRefNormals = floatRefNormals;
		mirrorNormalAllocated = false;
	    }
	    break;
	case N3F:
	    if (v3fRefNormals == null) {
		if ((vertexType & NORMAL_DEFINED) == N3F) {
		    vertexType &= ~N3F;
		}
		return;
	    }
	    else {
		vertexType |= N3F;
	    }
	    if (!mirrorNormalAllocated) { 
		mirrorFloatRefNormals = new float[vertexCount * 3];
		mirrorNormalAllocated = true;
	    }
	    
	    index = initialNormalIndex * 3;
	    for (i = initialNormalIndex; i < validVertexCount; i++) {
		mirrorFloatRefNormals[index++] = v3fRefNormals[i].x;
		mirrorFloatRefNormals[index++] = v3fRefNormals[i].y;
		mirrorFloatRefNormals[index++] = v3fRefNormals[i].z;
	    }
	    break;
	default:
	    break;	}
    }

    void setupMirrorTexCoordPointer(int type) {
	for (int i = 0; i < texCoordSetCount; i++) {
	     doSetupMirrorTexCoordPointer(i, type);
	}

        validateTexCoordPointerType();
    }
    
    void setupMirrorTexCoordPointer(int texCoordSet, int type) {
        doSetupMirrorTexCoordPointer(texCoordSet, type);
        validateTexCoordPointerType();
    }

    // If all texCoord pointers are set to a non-null value, then set the
    // texcoord type in the vertexType flag word, else clear the texcoord type
    private void validateTexCoordPointerType() {
        boolean allNonNull = true;
        boolean allNull = true;
        for (int i = 0; i < texCoordSetCount; i++) {
            if (refTexCoords[i] == null) {
                allNonNull = false;
            } else {
                allNull = false;
            }
        }

        // Reset texCoordType if all references are null
        if (allNull) {
            texCoordType = 0;
        }

        // Copy texCoordType to vertexType if all references are non-null
        vertexType &= ~TEXCOORD_DEFINED;
        if (allNonNull) {
            vertexType |= texCoordType;
        }
    }
    
    private void doSetupMirrorTexCoordPointer(int texCoordSet, int type) {
	int i, index;

        switch (type) { 
	case TF:
            texCoordType = TF;
            mirrorRefTexCoords[texCoordSet] = refTexCoords[texCoordSet];
	    break;

        case T2F:
            texCoordType = T2F;
	    t2fRefTexCoords = (TexCoord2f[])refTexCoords[texCoordSet];

	    if (t2fRefTexCoords == null) {
                mirrorRefTexCoords[texCoordSet] = null;
		break;
	    }

            mirrorFloatRefTexCoords = (float[])mirrorRefTexCoords[texCoordSet];
            if (mirrorFloatRefTexCoords != null) {
                if (mirrorFloatRefTexCoords.length < (vertexCount * 2))
                    mirrorRefTexCoords[texCoordSet] =
                        mirrorFloatRefTexCoords = new float[vertexCount * 2];
            }
            else {
                mirrorRefTexCoords[texCoordSet] =
                        mirrorFloatRefTexCoords = new float[vertexCount * 2];
            }
	    
	    index = initialTexCoordIndex[texCoordSet] * 2;	    
	    for (i = initialTexCoordIndex[texCoordSet]; i < validVertexCount; i++) {
		mirrorFloatRefTexCoords[index++] = t2fRefTexCoords[i].x;
		mirrorFloatRefTexCoords[index++] = t2fRefTexCoords[i].y;
	    }
	    break;

	case T3F:
            texCoordType = T3F;
	    t3fRefTexCoords = (TexCoord3f[])refTexCoords[texCoordSet];

            if (t3fRefTexCoords == null) {
                mirrorRefTexCoords[texCoordSet] = null;
		break;
	    }

            mirrorFloatRefTexCoords = (float[])mirrorRefTexCoords[texCoordSet];
            if (mirrorFloatRefTexCoords != null) {
                if (mirrorFloatRefTexCoords.length < (vertexCount * 3))
                    mirrorRefTexCoords[texCoordSet] =
                        mirrorFloatRefTexCoords = new float[vertexCount * 3];
            }
            else {
                mirrorRefTexCoords[texCoordSet] =
                    mirrorFloatRefTexCoords = new float[vertexCount * 3];
            }

	    index =  initialTexCoordIndex[texCoordSet] * 3;
	    for (i = initialTexCoordIndex[texCoordSet]; i < validVertexCount; i++) {
		mirrorFloatRefTexCoords[index++] = t3fRefTexCoords[i].x;
		mirrorFloatRefTexCoords[index++] = t3fRefTexCoords[i].y;
		mirrorFloatRefTexCoords[index++] = t3fRefTexCoords[i].z;
	    }
	    break;

	default:
	    break;
	}
    }

    void setupMirrorVertexAttrPointer(int type) {
        for (int i = 0; i < vertexAttrCount; i++) {
            doSetupMirrorVertexAttrPointer(i, type);
        }

        validateVertexAttrPointerType();
    }
    
    void setupMirrorVertexAttrPointer(int vertexAttrNum, int type) {
        doSetupMirrorVertexAttrPointer(vertexAttrNum, type);
        validateVertexAttrPointerType();
    }
    
    // If all vertex attr pointers are set to a non-null value, then set the
    // vertex attr type in the vertexType flag word, else clear the
    // vertex attr type
    private void validateVertexAttrPointerType() {
        boolean allNonNull = true;
        boolean allNull = true;

        if ((vertexFormat & GeometryArray.USE_NIO_BUFFER) == 0) {
            for (int i = 0; i < vertexAttrCount; i++) {
                if (floatRefVertexAttrs[i] == null) {
                    allNonNull = false;
                } else {
                    allNull = false;
                }
            }
        } else {
            for (int i = 0; i < vertexAttrCount; i++) {
                if (nioFloatBufferRefVertexAttrs[i] == null) {
                    allNonNull = false;
                } else {
                    allNull = false;
                }
            }
        }

        // Reset vertexAttrType if all references are null
        if (allNull) {
            vertexAttrType = 0;
        }

        // Copy vertexAttrType to vertexType if all references are non-null
        vertexType &= ~VATTR_DEFINED;
        if (allNonNull) {
            vertexType |= vertexAttrType;
        }
    }

    private void doSetupMirrorVertexAttrPointer(int vertexAttrNum, int type) {
        switch (type) {
        case AF:
            vertexAttrType = AF;
            mirrorFloatRefVertexAttrs[vertexAttrNum] =
                floatRefVertexAttrs[vertexAttrNum];
            break;
        default:
            break;
        }
    }


    void createGeometryArrayData(int vertexCount, int vertexFormat) {
	if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
	    createGeometryArrayData(vertexCount, vertexFormat, 1, 
				    defaultTexCoordSetMap);
	} else {
	    createGeometryArrayData(vertexCount, vertexFormat, 0, null);
	}
    }

    void createGeometryArrayData(int vertexCount, int vertexFormat,
				 int texCoordSetCount, int[] texCoordSetMap) {

	createGeometryArrayData(vertexCount, vertexFormat,
				texCoordSetCount, texCoordSetMap,
				0, null);
    }

    void createGeometryArrayData(int vertexCount, int vertexFormat,
				 int texCoordSetCount, int[] texCoordSetMap,
				 int vertexAttrCount, int[] vertexAttrSizes) {
	this.vertexFormat = vertexFormat;
	this.vertexCount = vertexCount;
	this.validVertexCount = vertexCount;

	this.texCoordSetCount = texCoordSetCount;
	if (texCoordSetMap == null) {
	    this.texCoordSetMap = null;
	}
	else {
	    this.texCoordSetMap = (int[])texCoordSetMap.clone();
	}

        this.vertexAttrCount = vertexAttrCount;
	if (vertexAttrSizes == null) {
	    this.vertexAttrSizes = null;
	}
	else {
	    this.vertexAttrSizes = (int[])vertexAttrSizes.clone();
	}

        this.vertexAttrStride = this.vertexAttrStride();
	this.stride = this.stride();

	this.vertexAttrOffsets = this.vertexAttrOffsets();
	this.texCoordSetMapOffset = this.texCoordSetMapOffset();
	this.textureOffset = this.textureOffset();
	this.colorOffset = this.colorOffset();
	this.normalOffset = this.normalOffset();
	this.coordinateOffset = this.coordinateOffset();

	if ((vertexFormat & GeometryArray.BY_REFERENCE) == 0) {
	    this.vertexData = new float[this.vertexCount * this.stride];
	}
	else { // By reference geometry
	    this.vertexData = null;
            if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
                this.mirrorRefTexCoords = new Object[texCoordSetCount];
                this.refTexCoords = new Object[texCoordSetCount]; // keep J3DBufferImp object in nio buffer case
		if((vertexFormat & GeometryArray.USE_NIO_BUFFER) != 0 )
		    this.refTexCoordsBuffer = new Object[texCoordSetCount]; // keep J3DBuffer object
	    }
            if ((vertexFormat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
                this.floatRefVertexAttrs = new float[vertexAttrCount][];
                this.mirrorFloatRefVertexAttrs = new float[vertexAttrCount][];
		if ((vertexFormat & GeometryArray.USE_NIO_BUFFER) != 0) {
		    this.vertexAttrsRefBuffer = new J3DBuffer[vertexAttrCount];
                    this.floatBufferRefVertexAttrs = new FloatBufferWrapper[vertexAttrCount];
                    this.nioFloatBufferRefVertexAttrs = new Object[vertexAttrCount];
                }
	    }
	}
        if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
            this.initialTexCoordIndex = new int[texCoordSetCount];
        }
        if ((vertexFormat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
            this.initialVertexAttrIndex = new int[vertexAttrCount];
        }
	noAlpha = ((vertexFormat & GeometryArray.WITH_ALPHA) == 0);
	lastAlpha[0] = 1.0f;

    }

    void setVertexFormat(boolean useAlpha, boolean ignoreVC, Context ctx) {
	Pipeline.getPipeline().setVertexFormat(ctx,
                this, vertexFormat, useAlpha, ignoreVC);
    }
    
    void disableGlobalAlpha(Context ctx, boolean useAlpha, boolean ignoreVC) {
	// If global alpha was turned on, then disable it
	Pipeline.getPipeline().disableGlobalAlpha(ctx,
                this, vertexFormat, useAlpha, ignoreVC);
    }


    float[] updateAlphaInFloatRefColors(Canvas3D cv, int screen, float alpha) {

	//System.out.println("updateAlphaInFloatRefColors  screen = " + screen +
	//		   " alpha " + alpha );
	
	// no need to update alpha values if canvas supports global alpha
	if (cv.supportGlobalAlpha()) {
	    cv.setGlobalAlpha(cv.ctx, alpha);
	    return mirrorFloatRefColors[0];
	}

	// Issue 113
	// TODO: Fix this for screen > 0, for now just ignore transparency
	if (screen > 0) {
	    return mirrorFloatRefColors[0];
	}

	// update alpha only if vertex format includes alpha
	if (((vertexFormat | c4fAllocated) & GeometryArray.WITH_ALPHA) == 0)
	    return mirrorFloatRefColors[0];

	// if alpha is smaller than EPSILON, set it to EPSILON, so that
	// even if alpha is equal to 0, we will not completely lose
	// the original alpha value
	if (alpha <= EPSILON) {
	    alpha = (float)EPSILON;
	}

        // allocate an entry for the last alpha of the screen if needed
	if (lastAlpha == null) {
	    lastAlpha = new float[screen + 1];
	    lastAlpha[screen] = 1.0f;
	} else if (lastAlpha.length <= screen) {
	    float[] la = new float[screen + 1];
	    for (int i = 0; i < lastAlpha.length; i++) {
		la[i] = lastAlpha[i];
	    }
	    lastAlpha = la;
	    lastAlpha[screen] = 1.0f;
	}

	//System.out.println("updateAlphaInFloatRefColors screen is " + screen 
	//		     + " mirrorFloatRefColors.length " + 
	//		     mirrorFloatRefColors.length);

	// allocate a copy of the color data for the screen if needed.
	// this piece of code is mainly for multi-screens case
	if (mirrorFloatRefColors.length <= screen) {
	    float[][] cfData = new float[screen + 1][];
	    float[] cdata;
	    int refScreen = -1;

	    for (int i = 0; i < mirrorFloatRefColors.length; i++) {
		cfData[i] = mirrorFloatRefColors[i];
		if (Math.abs(lastAlpha[i] - alpha) < EPSILON) {
		    refScreen = i;
		}
	    }
	    cdata = cfData[screen] = new float[4 * vertexCount];

	    // copy the data from a reference screen which has the closest
	    // alpha values
	    if (refScreen >= 0) {
		System.arraycopy(cfData[refScreen], 0, cdata, 0, 
				 4 * vertexCount);
		lastAlpha[screen] = lastAlpha[refScreen];
	    } else {
		float m = alpha / lastAlpha[0];
		float[] sdata = cfData[0];

		int j = initialColorIndex * 4;
		for (int i = initialColorIndex; i < validVertexCount; i++) {
		    cdata[j] = sdata[j++];
		    cdata[j] = sdata[j++];
		    cdata[j] = sdata[j++];
		    cdata[j] = sdata[j++] * m;
		}
		lastAlpha[screen] = alpha;
	    }
	    mirrorFloatRefColors = cfData;

	    // reset the colorChanged bit
	    colorChanged &= ~(1 << screen);
	    dirtyFlag |= COLOR_CHANGED;
	    
	    return cdata;	
	}

	/*
	  System.out.println("updateAlphaInFloatRefColors ** : lastAlpha[screen] " +
			     lastAlpha[screen]);
	  
	  System.out.println("((colorChanged & (1<<screen)) == 0) " +
			     ((colorChanged & (1<<screen)) == 0));
	*/

	if ((colorChanged & (1<<screen)) == 0) {
	    // color data is not modified
	    if (Math.abs(lastAlpha[screen] - alpha) < EPSILON) {
		// and if alpha is the same as the last one,
		// just return the data
		//System.out.println("updateAlphaInFloatRefColors 0 : alpha is the same as the last one " + alpha);

	        return mirrorFloatRefColors[screen];
	    } else {
	
		// if alpha is different, update the alpha values
		//System.out.println("updateAlphaInFloatRefColors 1 : alpha is different, update the alpha values " + alpha);

		float m = alpha / lastAlpha[screen];

		float[] cdata = mirrorFloatRefColors[screen];
		
		// We've to traverse the whole due to BugId : 4676483
		for (int i = 0, j = 0; i < vertexCount; i++, j+=4) {
	    	    cdata[j+3] = cdata[j+3] * m;
		}
	    }
	} else {
	    // color data is modified
	    if (screen == 0) {
		
		// just update alpha values since screen 0 data is
		// already updated in setupMirrorColorPointer
		
		//System.out.println("updateAlphaInFloatRefColors 2 : just update alpha = " + alpha);
		
		float[] cdata = mirrorFloatRefColors[screen];
		

		// This part is also incorrect due to BugId : 4676483
		// But traversing the whole array doesn't help either, as there
		// isn't a mechanism to indicate the the alpha component has
		// not changed by user.
		int j = initialColorIndex * 4;
		for (int i = initialColorIndex; i < validVertexCount; i++, j+=4) {
	    	    cdata[j+3] = cdata[j+3] * alpha;
		}
	    } else {
		// update color values from screen 0 data
		//System.out.println("updateAlphaInFloatRefColors 3 : update color values from screen 0 data " + alpha);

		float m;

		if ((colorChanged & 1) == 0) {
		    // alpha is up to date in screen 0
		    m = alpha / lastAlpha[0];
		} else {
		    m = alpha;
		}

		float[] sdata = mirrorFloatRefColors[0];
		float[] cdata = mirrorFloatRefColors[screen];

		int j = initialColorIndex * 4;
		for (int i = initialColorIndex; i < validVertexCount; i++) {
		    cdata[j] = sdata[j++];
		    cdata[j] = sdata[j++];
		    cdata[j] = sdata[j++];
		    cdata[j] = sdata[j++] * m;
		}
	    }
	}

	lastAlpha[screen] = alpha;
	colorChanged &= ~(1 << screen);
	dirtyFlag |= COLOR_CHANGED;
	return mirrorFloatRefColors[screen];
    }


    byte[] updateAlphaInByteRefColors(Canvas3D cv, int screen, float alpha) {

	/*
	  System.out.println("updateAlphaInByteRefColors  screen = " + screen +
	  " alpha " + alpha );
	*/

	// no need to update alpha values if canvas supports global alpha
	
	if (cv.supportGlobalAlpha()) { 
	    cv.setGlobalAlpha(cv.ctx, alpha);
	    return mirrorUnsignedByteRefColors[0];
	}

	// Issue 113
	// TODO: Fix this for screen > 0, for now just ignore transparency
	if (screen > 0) {
	    return mirrorUnsignedByteRefColors[0];
	}

	// update alpha only if vertex format includes alpha
	if (((vertexFormat | c4fAllocated) & GeometryArray.WITH_ALPHA) == 0)
	    return mirrorUnsignedByteRefColors[0];

	// if alpha is smaller than EPSILON, set it to EPSILON, so that
	// even if alpha is equal to 0, we will not completely lose
	// the original alpha value
	if (alpha <= EPSILON) {
	    alpha = (float)EPSILON;
	}

	// allocate an entry for the last alpha of the screen if needed
	if (lastAlpha == null) {
	    lastAlpha = new float[screen + 1];
	    lastAlpha[screen] = -1.0f;
	} else if (lastAlpha.length <= screen) {
	    float[] la = new float[screen + 1];
	    for (int i = 0; i < lastAlpha.length; i++) {
		la[i] = lastAlpha[i];
	    }
	    lastAlpha = la;
	    lastAlpha[screen] = -1.0f;
	}

	// allocate a copy of the color data for the screen if needed.
	// this piece of code is mainly for multi-screens case
	if (mirrorUnsignedByteRefColors.length <= screen) {
	    byte[][] cfData = new byte[screen + 1][];
	    byte[] cdata;
	    int refScreen = -1;
	    for (int i = 0; i < mirrorUnsignedByteRefColors.length; i++) {
		cfData[i] = mirrorUnsignedByteRefColors[i];
		if (Math.abs(lastAlpha[i] - alpha) < EPSILON) {
		    refScreen = i;
		}
	    }
	    cdata = cfData[screen] = new byte[4 * vertexCount];

	    // copy the data from a reference screen which has the closest
	    // alpha values
	    if (refScreen >= 0) {
		System.arraycopy(cfData[refScreen], 0, cdata, 0, 
					4 * vertexCount);
		lastAlpha[screen] = lastAlpha[refScreen];
	    } else {
		float m = alpha / lastAlpha[0];
		byte[] sdata = cfData[0];

		int j = initialColorIndex * 4;
		for (int i = initialColorIndex; i < validVertexCount; i++) {
		    cdata[j] = sdata[j++];
		    cdata[j] = sdata[j++];
		    cdata[j] = sdata[j++];
		    cdata[j] = (byte)(((int)sdata[j++]& 0xff) * m);
		}
		lastAlpha[screen] = alpha;
	    }
	    mirrorUnsignedByteRefColors = cfData;
	    colorChanged &= ~(1 << screen);
	    dirtyFlag |= COLOR_CHANGED;
	    return cdata;	
	}

	/*
	System.out.println("updateAlphaInByteRefColors ## : lastAlpha[screen] " +
			   lastAlpha[screen]);
	
	System.out.println("((colorChanged & (1<<screen)) == 0) " +
			   ((colorChanged & (1<<screen)) == 0));
	*/

        if ((colorChanged & (1<<screen)) == 0) {	    
            // color data is not modified
            if (Math.abs(lastAlpha[screen] - alpha) < EPSILON) {
                // and if alpha is the same as the last one,
                // just return the data
		//System.out.println("updateAlphaInByteRefColors 0 : alpha is the same as the last one " + alpha);

                return mirrorUnsignedByteRefColors[screen];
            } else {
                // if alpha is different, update the alpha values

		//System.out.println("updateAlphaInByteRefColors 1 : alpha is different, update the alpha values " + alpha);
		
                float m = alpha / lastAlpha[screen];

                byte[] cdata = mirrorUnsignedByteRefColors[screen];

		// We've to traverse the whole due to BugId : 4676483
		for (int i = 0, j = 0; i < vertexCount; i++, j+=4) {
                    cdata[j+3] = (byte)( ((int)cdata[j+3] & 0xff) * m);
                }
            }
        } else {
            // color data is modified
            if (screen == 0) {
		//System.out.println("updateAlphaInByteRefColors 2 : just update alpha =" + alpha);
		
                // just update alpha values since screen 0 data is
                // already updated in setupMirrorColorPointer

                byte[] cdata = mirrorUnsignedByteRefColors[screen];
		
		// This part is also incorrect due to BugId : 4676483
		// But traversing the whole array doesn't help either, as there
		// isn't a mechanism to indicate the the alpha component has
		// not changed by user.
		int j = initialColorIndex * 4;
		for (int i = initialColorIndex; i < validVertexCount; i++, j+=4) {
                    cdata[j+3] = (byte)(((int)cdata[j+3] & 0xff) * alpha);
                }
            } else {
                // update color values from screen 0 data
                float m;

		//System.out.println("updateAlphaInByteRefColors 3 : update color values from screen 0 data " + alpha);

                if ((colorChanged & 1) == 0) {
                    // alpha is up to date in screen 0
                    m = alpha / lastAlpha[0];
                } else {
                    m = alpha;
                }		
                byte[] sdata = mirrorUnsignedByteRefColors[0];
                byte[] cdata = mirrorUnsignedByteRefColors[screen];

		int j = initialColorIndex * 4;
		for (int i = initialColorIndex; i < validVertexCount; i++) {
		    cdata[j] = sdata[j++];
		    cdata[j] = sdata[j++];
		    cdata[j] = sdata[j++];
		    cdata[j] = (byte)(((int)sdata[j++]& 0xff) * m);
                }
            }
        }

        lastAlpha[screen] = alpha;
        colorChanged &= ~(1 << screen);
	dirtyFlag |= COLOR_CHANGED;
        return mirrorUnsignedByteRefColors[screen];
    }


    Object[] updateAlphaInVertexData(Canvas3D cv, int screen, float alpha) {

	Object[] retVal = new Object[2];
	retVal[0] = Boolean.FALSE;

	// no need to update alpha values if canvas supports global alpha
	if (cv.supportGlobalAlpha()) {
	    cv.setGlobalAlpha(cv.ctx, alpha);
	    retVal[1] = vertexData;
	    return retVal;
	}

	// Issue 113
	// TODO: Fix this for screen > 0, for now just ignore transparency
	if (screen > 0) {
	    retVal[1] = vertexData;
	    return retVal;
	}

	// update alpha only if vertex format includes alpha
	if ((vertexFormat & GeometryArray.COLOR) == 0) {
	    retVal[1] = vertexData;
	    return retVal;
	}

	// if alpha is smaller than EPSILON, set it to EPSILON, so that
	// even if alpha is equal to 0, we will not completely lose
	// the original alpha value
	if (alpha <= EPSILON) {
	    alpha = (float)EPSILON;
	}
	retVal[0] = Boolean.TRUE;

	// allocate an entry for the last alpha of the screen if needed
	if (lastAlpha == null) {
	    lastAlpha = new float[screen + 1];
	    lastAlpha[screen] = 1.0f;
	} else if (lastAlpha.length <= screen) {
	    float[] la = new float[screen + 1];
	    for (int i = 0; i < lastAlpha.length; i++) {
		la[i] = lastAlpha[i];
	    }
	    lastAlpha = la;
	    lastAlpha[screen] = 1.0f;
	}

	// allocate a copy of the vertex data for the screen if needed.
	// this piece of code is mainly for multi-screens case
	// NOTE: this might not too much data for just to update alpha
	if (mvertexData == null || mvertexData.length <= screen) {

	    float[][] cfData = new float[screen + 1][];
	    float[] cdata;
	    int refScreen = -1;

	    if (mvertexData != null) {
	        for (int i = 0; i < mvertexData.length; i++) {
		    cfData[i] = mvertexData[i];
		    if (Math.abs(lastAlpha[i] - alpha) < EPSILON) {
		        refScreen = i;
		    }
		}
	    }

	    if (cfData[0] == null)  {
		cfData[screen] = vertexData;
	    }

	    if (screen > 0) 
	        cfData[screen] = new float[stride * vertexCount];
	    
	    cdata = cfData[screen];

	    // copy the data from a reference screen which has the closest
	    // alpha values
	    if (refScreen >= 0) {
		System.arraycopy(cfData[refScreen], 0, cdata, 0, 
					stride * vertexCount);
		lastAlpha[screen] = lastAlpha[refScreen];
	    } else {
		float m = alpha / lastAlpha[0];
		float[] sdata = cfData[0];

		/*
		// screen 0 data is always up-to-date
		if (screen > 0) {
		    System.arraycopy(cfData[0], 0, cdata, 0, 
					stride * vertexCount);
		}
		*/

		for (int i = 0, j = colorOffset; i < vertexCount; 
					i++, j+=stride) {
		    cdata[j+3] = sdata[j+3] * m;
		}
		lastAlpha[screen] = alpha;
	    }
	    mvertexData = cfData;
	    dirtyFlag |= COLOR_CHANGED;
	    // reset the colorChanged bit
	    colorChanged &= ~(1 << screen);
	    retVal[1] = cdata;
	    return retVal;	
	}

	if ((colorChanged & (1<<screen)) == 0) {
	    // color data is not modified
	    if (Math.abs(lastAlpha[screen] - alpha) < EPSILON) {
		// and if alpha is the same as the last one,
		// just return the data
		retVal[1] = mvertexData[screen];
	        return retVal;
	    } else {
		// if alpha is different, update the alpha values
		float m = alpha / lastAlpha[screen];

		float[] cdata = mvertexData[screen];
		for (int i = 0, j = colorOffset; i < vertexCount; 
					i++, j+=stride) {
	    	    cdata[j+3] *= m;
		}
	    }
	} else {
	    // color data is modified
	    if (screen == 0) {
		// just update alpha values since screen 0 data is
		// already updated in setupMirrorColorPointer

		float[] cdata = mvertexData[screen];
		double m = alpha / lastAlpha[0];	       

		for (int i = 0, j = colorOffset; i < vertexCount; 
					i++, j+=stride) {
	    	    cdata[j+3] *= m;
		}
	    } else {
		// update color values from screen 0 data

		float m = alpha / lastAlpha[0];
		float[] sdata = mvertexData[0];
		float[] cdata = mvertexData[screen];

		for (int i = 0, j = colorOffset; i < vertexCount; 
					i++, j+=stride) {
		    System.arraycopy(sdata, j, cdata, j, 3);
		    cdata[j+3] = sdata[j+3] * m;
		}
	    }
	}

	lastAlpha[screen] = alpha;
	colorChanged &= ~(1 << screen);
	dirtyFlag |= COLOR_CHANGED;
	retVal[1] = mvertexData[screen];
	return retVal;
    }

    Object[] updateAlphaInInterLeavedData(Canvas3D cv, int screen, float alpha) {

	Object[] retVal = new Object[2];
	retVal[0] = Boolean.FALSE;

	// no need to update alpha values if canvas supports global alpha
	if (cv.supportGlobalAlpha()) {
	    cv.setGlobalAlpha(cv.ctx, alpha);
	    retVal[1] = null;
	    return retVal;
	}

	// Issue 113
	// TODO: Fix this for screen > 0, for now just ignore transparency
	if (screen > 0) {
	    retVal[1] = null;
	    return retVal;
	}

	// update alpha only if vertex format includes alpha
	if (((vertexFormat | c4fAllocated) & GeometryArray.COLOR) == 0) {
	    retVal[1] = mirrorInterleavedColorPointer[0];
	    return retVal;
	}
        int coffset = initialColorIndex << 2; // Each color is 4 floats

	// if alpha is smaller than EPSILON, set it to EPSILON, so that
	// even if alpha is equal to 0, we will not completely lose
	// the original alpha value
	if (alpha <= EPSILON) {
	    alpha = (float)EPSILON;
	}
	retVal[0] = Boolean.TRUE;

	// allocate an entry for the last alpha of the screen if needed
	if (lastAlpha == null) {
	    lastAlpha = new float[screen + 1];
	    lastAlpha[screen] = 1.0f;
	} else if (lastAlpha.length <= screen) {
	    float[] la = new float[screen + 1];
	    for (int i = 0; i < lastAlpha.length; i++) {
		la[i] = lastAlpha[i];
	    }
	    lastAlpha = la;
	    lastAlpha[screen] = 1.0f;
	}

	// allocate a copy of the vertex data for the screen if needed.
	// this piece of code is mainly for multi-screens case
	// NOTE: this might not too much data for just to update alpha
	if (mirrorInterleavedColorPointer.length <= screen) {

	    float[][] cfData = new float[screen + 1][];
	    float[] cdata;
	    int refScreen = -1;

	    for (int i = 0; i < mirrorInterleavedColorPointer.length; i++) {
		cfData[i] = mirrorInterleavedColorPointer[i];
		if (Math.abs(lastAlpha[i] - alpha) < EPSILON) {
		    refScreen = i;
		}
	    }

	    //cdata = cfData[screen] = new float[stride * vertexCount];
	    cdata = cfData[screen] = new float[4 * vertexCount];
	    
	    // copy the data from a reference screen which has the closest
	    // alpha values
	    if (refScreen >= 0) {
		System.arraycopy(cfData[refScreen], 0, cdata, 0, 
					4 * vertexCount);
		lastAlpha[screen] = lastAlpha[refScreen];
	    } else {
		float m = alpha / lastAlpha[0];
		float[] sdata = cfData[0];

		for (int i = coffset; i < coffset + (vertexCount << 2); i+=4) {
		    cdata[i+3] = sdata[i+3] * m;
		}

		lastAlpha[screen] = alpha;
	    }
	    mirrorInterleavedColorPointer = cfData;

	    // reset the colorChanged bit
	    colorChanged &= ~(1 << screen);
	    dirtyFlag |= COLOR_CHANGED;
	    retVal[1] = cdata;
	    return retVal;	
	}

	if ((colorChanged & (1<<screen)) == 0) {
	    // color data is not modified
	    if (Math.abs(lastAlpha[screen] - alpha) < EPSILON) {
		// and if alpha is the same as the last one,
		// just return the data
		retVal[1] = mirrorInterleavedColorPointer[screen];
	        return retVal;
	    } else {

		// if alpha is different, update the alpha values

		float m = alpha / lastAlpha[screen];

		float[] cdata = mirrorInterleavedColorPointer[screen];

		coffset = initialColorIndex << 2;
		for (int i = coffset; i < coffset + (vertexCount << 2); i+=4) {
	    	    cdata[i+3] = cdata[i+3] * m;
		}
	    }
	} else {
	    // color data is modified
	    if (screen == 0) {

		// just update alpha values since screen 0 data is
		// already updated in setupMirrorInterleavedColorPointer

		float[] cdata = mirrorInterleavedColorPointer[screen];

		for (int i = coffset; i < coffset + (vertexCount << 2); i+=4) {
	    	    cdata[i+3] = cdata[i+3] * alpha;
		}
	    } else {
		// update color values from screen 0 data

		float m;

		if ((colorChanged & 1) == 0) {
		    // alpha is up to date in screen 0
		    m = alpha / lastAlpha[0];
		} else {
		    m = alpha;
		}

		float[] sdata = mirrorInterleavedColorPointer[0];
		float[] cdata = mirrorInterleavedColorPointer[screen];

		for (int i = coffset; i < coffset + (vertexCount << 2);) {
		    // System.arraycopy(sdata, i, cdata, i, 3);
		    cdata[i] = sdata[i++];
		    cdata[i] = sdata[i++];
		    cdata[i] = sdata[i++];
		    cdata[i] = sdata[i++] * m;
		}
	    }
	}

	lastAlpha[screen] = alpha;
	colorChanged &= ~(1 << screen);
	dirtyFlag |= COLOR_CHANGED;
	retVal[1] = mirrorInterleavedColorPointer[screen];
	return retVal;
    }


    // pass < 0  implies underlying library supports multiTexture, so
    // 		 use the multiTexture extension to send all texture units 
    //		 data in one pass
    // pass >= 0 implies one pass for one texture unit state

    void execute(Canvas3D cv, RenderAtom ra, boolean isNonUniformScale, 
		 boolean updateAlpha, float alpha,
		 boolean multiScreen, int screen,
                 boolean ignoreVertexColors, int pass) {

        assert pass < 0;

	int cdirty;
	boolean useAlpha = false;
	Object[] retVal;

        // Check for by-copy case
	if ((vertexFormat & GeometryArray.BY_REFERENCE) == 0) {
            float[] vdata;

	    synchronized (this) {
		cdirty = dirtyFlag;
		if (updateAlpha && !ignoreVertexColors) {
		    // update the alpha values
		    retVal = updateAlphaInVertexData(cv, screen, alpha);
		    useAlpha = (retVal[0] == Boolean.TRUE);
		    vdata = (float[])retVal[1];

		    // D3D only
		    if (alpha != lastScreenAlpha) {
			// handle multiple screen case
			lastScreenAlpha = alpha;
			cdirty |= COLOR_CHANGED;
		    }
		} else {
		    vdata = vertexData;
		    // if transparency switch between on/off
		    if (lastScreenAlpha != -1) {
			lastScreenAlpha = -1;
			cdirty |= COLOR_CHANGED;
		    }
		}
		// geomLock is get in MasterControl when
		// RenderBin render the geometry. So it is safe
		// just to set the dirty flag here
		dirtyFlag = 0;
	    }

	    Pipeline.getPipeline().execute(cv.ctx,
                    this, geoType, isNonUniformScale,
		    useAlpha,
		    multiScreen,
		    ignoreVertexColors,
		    initialVertexIndex, 
		     validVertexCount, 
		    ((vertexFormat & GeometryArray.COLOR) != 0)?(vertexFormat|GeometryArray.COLOR_4):vertexFormat,
                    texCoordSetCount, texCoordSetMap,
                    (texCoordSetMap == null) ? 0 : texCoordSetMap.length,
                    texCoordSetMapOffset, 
		    cv.numActiveTexUnit, cv.texUnitStateMap, 
                    vertexAttrCount, vertexAttrSizes,
                    vdata, null,
                    pass, cdirty);
	}

	//By reference with java array
	else if ((vertexFormat & GeometryArray.USE_NIO_BUFFER) == 0) {
	    // interleaved data
	    if ((vertexFormat & GeometryArray.INTERLEAVED) != 0) {
		if(interLeavedVertexData == null)
		    return;
		
		float[] cdata = null;

		synchronized (this) {
		    cdirty = dirtyFlag;
		    if (updateAlpha && !ignoreVertexColors) {
			// update the alpha values
			retVal = updateAlphaInInterLeavedData(cv, screen, alpha);
			useAlpha = (retVal[0] == Boolean.TRUE);
			cdata = (float[])retVal[1];
			if (alpha != lastScreenAlpha) { 
			    lastScreenAlpha = alpha;
			    cdirty |= COLOR_CHANGED;
			}
		    } else {
			// if transparency switch between on/off
			if (lastScreenAlpha != -1) {
			    lastScreenAlpha = -1;
			    cdirty |= COLOR_CHANGED;
			}
		    }
		    dirtyFlag = 0;
		}

		Pipeline.getPipeline().execute(cv.ctx,
                        this, geoType, isNonUniformScale,
			useAlpha,
			multiScreen,
			ignoreVertexColors,
			initialVertexIndex, 
			validVertexCount, 
			vertexFormat, 
			texCoordSetCount, texCoordSetMap,
			(texCoordSetMap == null) ? 0 : texCoordSetMap.length,
			texCoordSetMapOffset, 
			cv.numActiveTexUnit, cv.texUnitStateMap,
                        vertexAttrCount, vertexAttrSizes,
                        interLeavedVertexData, cdata,
			pass, cdirty);

	    } // end of interleaved case
	    
	    // non interleaved data
	    else {

		// Check if a vertexformat is set, but the array is null
		// if yes, don't draw anything
		if ((vertexType == 0) ||
		    ((vertexType & VERTEX_DEFINED) == 0) ||
		    (((vertexFormat & GeometryArray.COLOR) != 0) &&
		     (vertexType & COLOR_DEFINED) == 0) ||
		    (((vertexFormat & GeometryArray.NORMALS) != 0) &&
		     (vertexType & NORMAL_DEFINED) == 0) ||
		    (((vertexFormat & GeometryArray.VERTEX_ATTRIBUTES) != 0) &&
		     (vertexType & VATTR_DEFINED) == 0) ||
		    (((vertexFormat& GeometryArray.TEXTURE_COORDINATE) != 0) &&
		     (vertexType & TEXCOORD_DEFINED) == 0)) {
		    return;  
		} else {
		    byte[] cbdata = null;
		    float[] cfdata = null;
		    
		    if ((vertexType & (CF | C3F | C4F )) != 0) {

			synchronized (this) {
			    cdirty = dirtyFlag;
			    if (updateAlpha && !ignoreVertexColors) {
				cfdata = updateAlphaInFloatRefColors(cv, screen, alpha);
				if (alpha != lastScreenAlpha) {
				    lastScreenAlpha = alpha;
				    cdirty |= COLOR_CHANGED;
				}
			    } else {
				cfdata = mirrorFloatRefColors[0];
				// if transparency switch between on/off
				if (lastScreenAlpha != -1) {
				    lastScreenAlpha = -1;
				    cdirty |= COLOR_CHANGED;
				}
			    
			    }
			    dirtyFlag = 0;
			}
		    } // end of color in float format
		    else if ((vertexType & (CUB| C3UB | C4UB)) != 0) {
			synchronized (this) {
			    cdirty = dirtyFlag;
			    if (updateAlpha && !ignoreVertexColors) {
				cbdata = updateAlphaInByteRefColors(cv, screen, alpha);
				if (alpha != lastScreenAlpha) {
				    lastScreenAlpha = alpha;
				    cdirty |= COLOR_CHANGED;
				}
			    } else {
				cbdata = mirrorUnsignedByteRefColors[0];
				// if transparency switch between on/off
				if (lastScreenAlpha != -1) {
				    lastScreenAlpha = -1;
				    cdirty |= COLOR_CHANGED;
				}
			    }
			    dirtyFlag = 0;
			}
		    } // end of color in byte format
		    else {
			cdirty = dirtyFlag;
		    }
		    // setup vdefined to passed to native code
		    int vdefined = 0;
		    if((vertexType & (PF | P3F)) != 0)
			vdefined |= COORD_FLOAT;
		    if((vertexType & (PD | P3D)) != 0)
			vdefined |= COORD_DOUBLE;
		    if((vertexType & (CF | C3F | C4F)) != 0)
			vdefined |= COLOR_FLOAT;
		    if((vertexType & (CUB| C3UB | C4UB)) != 0)
			vdefined |= COLOR_BYTE;
		    if((vertexType & NORMAL_DEFINED) != 0)
			vdefined |= NORMAL_FLOAT;
		    if((vertexType & VATTR_DEFINED) != 0)
			vdefined |= VATTR_FLOAT;
		    if((vertexType & TEXCOORD_DEFINED) != 0)
			vdefined |= TEXCOORD_FLOAT;

                    Pipeline.getPipeline().executeVA(cv.ctx,
                            this, geoType, isNonUniformScale,
                            multiScreen,
                            ignoreVertexColors,
                            validVertexCount,
                            (vertexFormat | c4fAllocated),
                            vdefined,
                            initialCoordIndex,
                            mirrorFloatRefCoords, mirrorDoubleRefCoords,
                            initialColorIndex, cfdata, cbdata,
                            initialNormalIndex, mirrorFloatRefNormals,
                            vertexAttrCount, vertexAttrSizes,
                            initialVertexAttrIndex, mirrorFloatRefVertexAttrs,
                            pass,
                            ((texCoordSetMap == null) ? 0:texCoordSetMap.length),
                            texCoordSetMap,
                            cv.numActiveTexUnit,
                            cv.texUnitStateMap,
                            initialTexCoordIndex,texCoordStride,
                            mirrorRefTexCoords, cdirty);
		}// end of all vertex data being set
	    }// end of non interleaved case
	}// end of by reference with java array

	//By reference with nio buffer
	else  {
	    // interleaved data
	    if ((vertexFormat & GeometryArray.INTERLEAVED) != 0) {

		if ( interleavedFloatBufferImpl == null)
		    return;
		
		float[] cdata = null; 
		synchronized (this) {
		    cdirty = dirtyFlag;
		    if (updateAlpha && !ignoreVertexColors) {
			// update the alpha values
			// XXXX: to handle alpha case
			retVal = updateAlphaInInterLeavedData(cv, screen, alpha);
			useAlpha = (retVal[0] == Boolean.TRUE);
			cdata = (float[])retVal[1];
			
			if (alpha != lastScreenAlpha) { 
			    lastScreenAlpha = alpha;
			    cdirty |= COLOR_CHANGED;
			}
		    } else {
			// XXXX: to handle alpha case
			cdata = null;
			// if transparency switch between on/off
			if (lastScreenAlpha != -1) {
			    lastScreenAlpha = -1;
			    cdirty |= COLOR_CHANGED;
			}
		    }
		    dirtyFlag = 0;
		}

                Pipeline.getPipeline().executeInterleavedBuffer(cv.ctx,
                        this, geoType, isNonUniformScale,
                        useAlpha,
                        multiScreen,
                        ignoreVertexColors,
                        initialVertexIndex,
                        validVertexCount,
                        vertexFormat,
                        texCoordSetCount, texCoordSetMap,
                        (texCoordSetMap == null) ? 0 : texCoordSetMap.length,
                        texCoordSetMapOffset,
                        cv.numActiveTexUnit, cv.texUnitStateMap,
                        interleavedFloatBufferImpl.getBufferAsObject(), cdata,
                        pass, cdirty);

	    } // end of interleaved case

	    // non interleaved data
	    else {

		// Check if a vertexformat is set, but the array is null
		// if yes, don't draw anything
		if ((vertexType == 0) ||
		    ((vertexType & VERTEX_DEFINED) == 0) ||
		    (((vertexFormat & GeometryArray.COLOR) != 0) &&
		     (vertexType & COLOR_DEFINED) == 0) ||
		    (((vertexFormat & GeometryArray.NORMALS) != 0) &&
		     (vertexType & NORMAL_DEFINED) == 0) ||
		    (((vertexFormat & GeometryArray.VERTEX_ATTRIBUTES) != 0) &&
		     (vertexType & VATTR_DEFINED) == 0) ||
		    (((vertexFormat& GeometryArray.TEXTURE_COORDINATE) != 0) &&
		     (vertexType & TEXCOORD_DEFINED) == 0)) {
		    return;  
		} else {
		    byte[] cbdata = null;
		    float[] cfdata = null;

		    if ((vertexType & CF ) != 0) {
			synchronized (this) {
			    cdirty = dirtyFlag;
			    if (updateAlpha && !ignoreVertexColors) {
				cfdata = updateAlphaInFloatRefColors(cv,
								     screen, alpha);
				if (alpha != lastScreenAlpha) {
				    lastScreenAlpha = alpha;
				    cdirty |= COLOR_CHANGED;
				}
			    } else {
				// XXXX: handle transparency case
				//cfdata = null;
				cfdata = mirrorFloatRefColors[0]; 
				// if transparency switch between on/off
				if (lastScreenAlpha != -1) {
				    lastScreenAlpha = -1;
				    cdirty |= COLOR_CHANGED;
				}
			    
			    }
			    dirtyFlag = 0;
			}
		    } // end of color in float format
		    else if ((vertexType & CUB) != 0) {
			synchronized (this) {
			    cdirty = dirtyFlag;
			    if (updateAlpha && !ignoreVertexColors) {
				cbdata = updateAlphaInByteRefColors(
								    cv, screen, alpha); 
				if (alpha != lastScreenAlpha) {
				    lastScreenAlpha = alpha;
				    cdirty |= COLOR_CHANGED;
				}
			    } else {
				// XXXX: handle transparency case
				//cbdata = null;
				cbdata = mirrorUnsignedByteRefColors[0]; 
				// if transparency switch between on/off
				if (lastScreenAlpha != -1) {
				    lastScreenAlpha = -1;
				    cdirty |= COLOR_CHANGED;
				}
			    }
			    dirtyFlag = 0;
			}
		    } // end of color in byte format
		    else {
			cdirty = dirtyFlag;
		    }

		    Object vcoord = null, cdataBuffer=null, normal=null;
		    
		    int vdefined = 0;
		    if((vertexType & PF)  != 0) {
			vdefined |= COORD_FLOAT;
			vcoord = floatBufferRefCoords.getBufferAsObject();
		    } else if((vertexType & PD ) != 0) {
			vdefined |= COORD_DOUBLE;
			vcoord = doubleBufferRefCoords.getBufferAsObject();
		    }
		    
		    if((vertexType & CF ) != 0) {
			vdefined |= COLOR_FLOAT;
			cdataBuffer = floatBufferRefColors.getBufferAsObject();
		    } else if((vertexType & CUB) != 0) {
			vdefined |= COLOR_BYTE;
			cdataBuffer = byteBufferRefColors.getBufferAsObject();
		    }
		    
		    if((vertexType & NORMAL_DEFINED) != 0) {
			vdefined |= NORMAL_FLOAT;
			normal = floatBufferRefNormals.getBufferAsObject();
                    }

                    if ((vertexType & VATTR_DEFINED) != 0) {
                        vdefined |= VATTR_FLOAT;
                    }

                    if((vertexType & TEXCOORD_DEFINED) != 0)
		       vdefined |= TEXCOORD_FLOAT;

                    Pipeline.getPipeline().executeVABuffer(cv.ctx,
                            this, geoType, isNonUniformScale,
                            multiScreen,
                            ignoreVertexColors,
                            validVertexCount,
                            (vertexFormat | c4fAllocated),
                            vdefined,
                            initialCoordIndex,
                            vcoord,
                            initialColorIndex,
                            cdataBuffer,
                            cfdata, cbdata,
                            initialNormalIndex,
                            normal,
                            vertexAttrCount, vertexAttrSizes,
                            initialVertexAttrIndex,
                            nioFloatBufferRefVertexAttrs,
                            pass,
                            ((texCoordSetMap == null) ? 0:texCoordSetMap.length),
                            texCoordSetMap,
                            cv.numActiveTexUnit,
                            cv.texUnitStateMap,
                            initialTexCoordIndex,texCoordStride,
                            refTexCoords, cdirty);
		}// end of all vertex data being set
	    }// end of non interleaved case
	}// end of by reference with nio-buffer case
    }

    void buildGA(Canvas3D cv, RenderAtom ra, boolean isNonUniformScale, 
		 boolean updateAlpha, float alpha, boolean ignoreVertexColors,
		 Transform3D xform, Transform3D nxform) {

        float[] vdata = null;

        // NIO buffers are no longer supported in display lists
        assert (vertexFormat & GeometryArray.USE_NIO_BUFFER) == 0;

	if ((vertexFormat & GeometryArray.BY_REFERENCE) == 0) {
	    vdata = vertexData;
	}
	else if ((vertexFormat & GeometryArray.INTERLEAVED) != 0 &&
		 ((vertexFormat & GeometryArray.USE_NIO_BUFFER) == 0)) {
	    vdata = interLeavedVertexData;
	}
	if (vdata != null) {
	    /*
	     System.out.println("calling native buildGA()");
	     System.out.println("geoType = "+geoType+" initialVertexIndex = "+initialVertexIndex+" validVertexCount = "+validVertexCount+" vertexFormat = "+vertexFormat+"  vertexData = "+vertexData);
	     */
	    Pipeline.getPipeline().buildGA(cv.ctx,
                    this, geoType, isNonUniformScale, 
		    updateAlpha, alpha, ignoreVertexColors,
		    initialVertexIndex,
		    validVertexCount, vertexFormat, 
		    texCoordSetCount, texCoordSetMap,
		    (texCoordSetMap == null) ? 0 : texCoordSetMap.length,
		    texCoordSetMapOffset, 
                    vertexAttrCount, vertexAttrSizes,
                    (xform == null) ? null : xform.mat,
		    (nxform == null) ? null : nxform.mat,
		    vdata);
	}
	else {
            // Check if a vertexformat is set, but the array is null
            // if yes, don't draw anything
            if ((vertexType == 0) ||
		((vertexType & VERTEX_DEFINED) == 0) ||
		(((vertexFormat & GeometryArray.COLOR) != 0) &&
		 (vertexType & COLOR_DEFINED) == 0) ||
		(((vertexFormat & GeometryArray.NORMALS) != 0) &&
		 (vertexType & NORMAL_DEFINED) == 0) ||
		(((vertexFormat & GeometryArray.VERTEX_ATTRIBUTES) != 0) &&
		 (vertexType & VATTR_DEFINED) == 0) ||
		(((vertexFormat& GeometryArray.TEXTURE_COORDINATE) != 0) &&
		 (vertexType & TEXCOORD_DEFINED) == 0)) {

                return;
            }

            // Either non-interleaved, by-ref or nio buffer
	    if ((vertexFormat & GeometryArray.USE_NIO_BUFFER) == 0) {
                // Java array case
		    // setup vdefined to passed to native code
		    int vdefined = 0;
		    if((vertexType & (PF | P3F)) != 0)
			vdefined |= COORD_FLOAT;
		    if((vertexType & (PD | P3D)) != 0)
			vdefined |= COORD_DOUBLE;
		    if((vertexType & (CF | C3F | C4F)) != 0)
			vdefined |= COLOR_FLOAT;
		    if((vertexType & (CUB| C3UB | C4UB)) != 0)
			vdefined |= COLOR_BYTE;
		    if((vertexType & NORMAL_DEFINED) != 0)
			vdefined |= NORMAL_FLOAT;
		    if((vertexType & VATTR_DEFINED) != 0)
			vdefined |= VATTR_FLOAT;
		    if((vertexType & TEXCOORD_DEFINED) != 0)
			vdefined |= TEXCOORD_FLOAT;

		    Pipeline.getPipeline().buildGAForByRef(cv.ctx,
                            this, geoType, isNonUniformScale,
			    updateAlpha, alpha,
			    ignoreVertexColors,
			    validVertexCount,
			    vertexFormat,
			    vdefined,
			    initialCoordIndex,
			    mirrorFloatRefCoords, mirrorDoubleRefCoords,
			    initialColorIndex, mirrorFloatRefColors[0], mirrorUnsignedByteRefColors[0],
			    initialNormalIndex, mirrorFloatRefNormals,
			    vertexAttrCount, vertexAttrSizes,
                            initialVertexAttrIndex, mirrorFloatRefVertexAttrs,
			    ((texCoordSetMap == null) ? 0:texCoordSetMap.length),
			    texCoordSetMap,
			    initialTexCoordIndex,texCoordStride,
			    mirrorRefTexCoords,
			    (xform == null) ? null : xform.mat,
			    (nxform == null) ? null : nxform.mat);
	    }
            /*
            // NOTE: NIO buffers are no longer supported in display lists.
            // This was never enabled by default anyway (only when the
            // optimizeForSpace property was set to false), so it wasn't
            // well-tested. If future support is desired, we will need to
            // add vertex attributes to buildGAForBuffer. There are no plans
            // to ever do this.
	    else {
                // NIO Buffer case
                Object vcoord = null, cdataBuffer=null, normal=null;

                int vdefined = 0;
                if((vertexType & PF)  != 0) {
                    vdefined |= COORD_FLOAT;
                    vcoord = floatBufferRefCoords.getBufferAsObject();
                } else if((vertexType & PD ) != 0) {
                    vdefined |= COORD_DOUBLE;
                    vcoord = doubleBufferRefCoords.getBufferAsObject();
                }

                if((vertexType & CF ) != 0) {
                    vdefined |= COLOR_FLOAT;
                    cdataBuffer = floatBufferRefColors.getBufferAsObject();
                } else if((vertexType & CUB) != 0) {
                    vdefined |= COLOR_BYTE;
                    cdataBuffer = byteBufferRefColors.getBufferAsObject();
                }

                if((vertexType & NORMAL_DEFINED) != 0) {
                    vdefined |= NORMAL_FLOAT;
                    normal = floatBufferRefNormals.getBufferAsObject();
                }

                if((vertexType & TEXCOORD_DEFINED) != 0)
                    vdefined |= TEXCOORD_FLOAT;
                // NOTE : need to add vertex attrs
                Pipeline.getPipeline().buildGAForBuffer(cv.ctx,
                        this, geoType, isNonUniformScale,
                        updateAlpha, alpha,
                        ignoreVertexColors,
                        validVertexCount,
                        vertexFormat,
                        vdefined,
                        initialCoordIndex,
                        vcoord,
                        initialColorIndex,cdataBuffer,
                        initialNormalIndex, normal,
                        ((texCoordSetMap == null) ? 0:texCoordSetMap.length),
                        texCoordSetMap,
                        initialTexCoordIndex,texCoordStride,
                        refTexCoords,
                        (xform == null) ? null : xform.mat,
                        (nxform == null) ? null : nxform.mat);
	    }
            */

	}
      
    }

    void unIndexify(IndexedGeometryArrayRetained src) {
	if ((src.vertexFormat & GeometryArray.USE_NIO_BUFFER) == 0) {
	    unIndexifyJavaArray(src);
	}
	else {
	    unIndexifyNIOBuffer(src);
	}
    }

    private void unIndexifyJavaArray(IndexedGeometryArrayRetained src) {
//        System.err.println("unIndexifyJavaArray");

        int vOffset = 0, srcOffset, tOffset = 0;
        int index, colorStride = 0;
	float[] vdata = null;
        int i;
	int start, end;
	start = src.initialIndexIndex;
	end = src.initialIndexIndex + src.validIndexCount;
	// If its either "normal" data or interleaved data then ..
	if (((src.vertexFormat & GeometryArray.BY_REFERENCE) == 0) ||
	    ((src.vertexFormat & GeometryArray.INTERLEAVED) != 0)) {

	    if ((src.vertexFormat & GeometryArray.BY_REFERENCE) == 0) {
		vdata = src.vertexData;
		if ((src.vertexFormat & GeometryArray.COLOR) != 0)
		    colorStride = 4;
	    }
	    else if ((src.vertexFormat & GeometryArray.INTERLEAVED) != 0) {
		vdata = src.interLeavedVertexData;
		if ((src.vertexFormat & GeometryArray.WITH_ALPHA) != 0)
		    colorStride = 4;
		else if ((src.vertexFormat & GeometryArray.COLOR) != 0)
		    colorStride = 3;
	    }

	    //	    System.out.println("===> start = "+start+" end = "+end);
	    for (index= start; index < end; index++) {
		if ((vertexFormat & GeometryArray.NORMALS) != 0){
		    System.arraycopy(vdata,
			src.indexNormal[index]*src.stride + src.normalOffset,
			vertexData, vOffset + normalOffset, 3);
		}
		if (colorStride == 4){
		    //		    System.out.println("===> copying color3");
		    System.arraycopy(vdata,
			src.indexColor[index]*src.stride + src.colorOffset,
			vertexData, vOffset + colorOffset, colorStride);
		} else if (colorStride == 3) {
		    //		    System.out.println("===> copying color4");
		    System.arraycopy(vdata,
			src.indexColor[index]*src.stride + src.colorOffset,
			vertexData, vOffset + colorOffset, colorStride);
		    vertexData[vOffset + colorOffset + 3] = 1.0f;
		}
		
		if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
		    int tcOffset = vOffset + textureOffset;
                    int interleavedOffset = 0;

		    for (i = 0; i < texCoordSetCount; 
				i++, tcOffset += texCoordStride) {

                        if ((src.vertexFormat & GeometryArray.INTERLEAVED) != 0) {
                            interleavedOffset = i * texCoordStride;
                        }

			 System.arraycopy(vdata,
			    (src.indexTexCoord[i][index])*src.stride + src.textureOffset + interleavedOffset,
			    vertexData, tcOffset, texCoordStride);
		    }
		}

                if ((vertexFormat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
                    // vertex attributes can't be interleaved
                    assert (src.vertexFormat & GeometryArray.INTERLEAVED) == 0;

		    for (i = 0; i < vertexAttrCount; i++) {
                        int vaOffset = vOffset + vertexAttrOffsets[i];

			 System.arraycopy(vdata,
			    (src.indexVertexAttr[i][index])*src.stride + src.vertexAttrOffsets[i],
			    vertexData, vaOffset, vertexAttrSizes[i]);
		    }
                }

		if ((vertexFormat & GeometryArray.COORDINATES) != 0){
		    //		    System.out.println("===> copying coords");
		    System.arraycopy(vdata,
			src.indexCoord[index]*src.stride 
				+ src.coordinateOffset,
			vertexData, 
			vOffset + coordinateOffset, 3);
		}
		vOffset += stride;
	    }

	} else {
	    if ((vertexFormat & GeometryArray.NORMALS) != 0) {
		vOffset = normalOffset;
		switch ((src.vertexType & NORMAL_DEFINED)) { 
		case NF: 
		    for (index=start; index < end; index++) {
			System.arraycopy(src.floatRefNormals,
					 src.indexNormal[index]*3,
					 vertexData,
					 vOffset, 3);
			vOffset += stride;
		    }
		    break;
		case N3F: 
		    for (index=start; index < end; index++) {
			srcOffset = src.indexNormal[index];
			vertexData[vOffset] = src.v3fRefNormals[srcOffset].x;
			vertexData[vOffset+1] = src.v3fRefNormals[srcOffset].y;
			vertexData[vOffset+2] = src.v3fRefNormals[srcOffset].z;
			vOffset += stride;
		    }
		    break;
		default:
		    break;
		}
	    }

	    if ((vertexFormat & GeometryArray.COLOR) != 0) {
		vOffset = colorOffset;
		int multiplier = 3;
		if ((src.vertexFormat & GeometryArray.WITH_ALPHA) != 0)
		    multiplier = 4;
		
		switch ((src.vertexType & COLOR_DEFINED)) { 
		case CF:
		    for (index=start; index < end; index++) {
			if ((src.vertexFormat & GeometryArray.WITH_ALPHA) != 0) {
			    System.arraycopy(src.floatRefColors,
					     src.indexColor[index]*multiplier,
					     vertexData,
					     vOffset, 4);
			}
			else {
			    System.arraycopy(src.floatRefColors,
					     src.indexColor[index]*multiplier,
					     vertexData,
					     vOffset, 3);
			    vertexData[vOffset+3] = 1.0f;
			}
			vOffset += stride;
		    }
		    break;
		case CUB: 
		    for (index=start; index < end; index++) {
			srcOffset = src.indexColor[index] * multiplier;
			vertexData[vOffset] = (src.byteRefColors[srcOffset] & 0xff) * ByteToFloatScale;
			vertexData[vOffset+1] = (src.byteRefColors[srcOffset+1] & 0xff) * ByteToFloatScale;
			vertexData[vOffset+2] = (src.byteRefColors[srcOffset+2] & 0xff) * ByteToFloatScale;
			if ((src.vertexFormat & GeometryArray.WITH_ALPHA) != 0) {
			    vertexData[vOffset+3] = (src.byteRefColors[srcOffset+3] & 0xff) * ByteToFloatScale;
			}
			else {
			    vertexData[vOffset+3] = 1.0f;
			}
			vOffset += stride;
		    }
		    break;
		case C3F: 
		    for (index=start; index < end; index++) {
			srcOffset = src.indexColor[index];
			vertexData[vOffset] = src.c3fRefColors[srcOffset].x;
			vertexData[vOffset+1] = src.c3fRefColors[srcOffset].y;
			vertexData[vOffset+2] = src.c3fRefColors[srcOffset].z;
			vertexData[vOffset+3] = 1.0f;
			vOffset += stride;
		    }
		    break;
		case C4F: 
		    for (index=start; index < end; index++) {
			srcOffset = src.indexColor[index];
			vertexData[vOffset] = src.c4fRefColors[srcOffset].x;
			vertexData[vOffset+1] = src.c4fRefColors[srcOffset].y;
			vertexData[vOffset+2] = src.c4fRefColors[srcOffset].z;
			vertexData[vOffset+3] = src.c4fRefColors[srcOffset].w;
			vOffset += stride;
		    }
		    break;
		case C3UB: 
		    for (index=start; index < end; index++) {
			srcOffset = src.indexColor[index];
			vertexData[vOffset] = (src.c3bRefColors[srcOffset].x & 0xff) * ByteToFloatScale;
			vertexData[vOffset+1] = (src.c3bRefColors[srcOffset].y & 0xff) * ByteToFloatScale;
			vertexData[vOffset+2] = (src.c3bRefColors[srcOffset].z & 0xff) * ByteToFloatScale;
			vertexData[vOffset+3] = 1.0f;
			vOffset += stride;
		    }
		    break;
		case C4UB: 
		    for (index=start; index < end; index++) {
			srcOffset = src.indexColor[index];
			vertexData[vOffset] = (src.c4bRefColors[srcOffset].x & 0xff) * ByteToFloatScale;
			vertexData[vOffset+1] = (src.c4bRefColors[srcOffset].y & 0xff) * ByteToFloatScale;
			vertexData[vOffset+2] = (src.c4bRefColors[srcOffset].z & 0xff) * ByteToFloatScale;
			vertexData[vOffset+3] = (src.c4bRefColors[srcOffset].w & 0xff) * ByteToFloatScale;
			vOffset += stride;
		    }
		    break;
		default:
		    break;
		}
	    }

	    if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
		vOffset = textureOffset;
		switch ((src.vertexType & TEXCOORD_DEFINED)) {
		case TF:
		    for (index=start; index < end; index++) {
			for (i = 0, tOffset = vOffset; 
				i < texCoordSetCount; i++) {
			    System.arraycopy(src.refTexCoords[i],
				src.indexTexCoord[i][index]*texCoordStride,
				vertexData, tOffset, texCoordStride);
			    tOffset += texCoordStride;
			}
			vOffset += stride;
		    }
		    break;
		case T2F: 
		    for (index=start; index < end; index++) {
			for (i = 0, tOffset = vOffset;
			        i < texCoordSetCount; i++) {
			     srcOffset = src.indexTexCoord[i][index];
			     vertexData[tOffset] = 
			      ((TexCoord2f[])src.refTexCoords[i])[srcOffset].x;
			     vertexData[tOffset+1] = 
			      ((TexCoord2f[])src.refTexCoords[i])[srcOffset].y;
			     tOffset += texCoordStride;
			}
			vOffset += stride;
		    }
		    break;
		case T3F: 
		    for (index=start; index < end; index++) {
			for (i = 0, tOffset = vOffset;
			        i < texCoordSetCount; i++) {
			     srcOffset = src.indexTexCoord[i][index];
			     vertexData[tOffset] = 
			      ((TexCoord3f[])src.refTexCoords[i])[srcOffset].x;
			     vertexData[tOffset+1] = 
			      ((TexCoord3f[])src.refTexCoords[i])[srcOffset].y;
			     vertexData[tOffset+2] = 
			      ((TexCoord3f[])src.refTexCoords[i])[srcOffset].z;
			     tOffset += texCoordStride;
			}
			vOffset += stride;
		    }
		    break;
		default:
		    break;
		}
	    }

            if ((vertexFormat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
		vOffset = 0;
		switch (src.vertexType & VATTR_DEFINED) {
		case AF:
		    for (index=start; index < end; index++) {
			for (i = 0; i < vertexAttrCount; i++) {
                            int vaOffset = vOffset + vertexAttrOffsets[i];
			    System.arraycopy(src.floatRefVertexAttrs[i],
				src.indexVertexAttr[i][index]*vertexAttrSizes[i],
				vertexData, vaOffset, vertexAttrSizes[i]);
			}
			vOffset += stride;
		    }
		    break;
		}
            }

	    if ((vertexFormat & GeometryArray.COORDINATES) != 0) {
		vOffset = coordinateOffset;
		switch ((src.vertexType & VERTEX_DEFINED)) {
		case PF:
		    for (index=start; index < end; index++) {
			System.arraycopy(src.floatRefCoords,
					 src.indexCoord[index]*3,
					 vertexData,
					 vOffset, 3);
			vOffset += stride;
		    }
		    break;
		case PD: 
		    for (index=start; index < end; index++) {
			srcOffset = src.indexCoord[index] * 3;
			vertexData[vOffset] = (float)src.doubleRefCoords[srcOffset];
			vertexData[vOffset+1] = (float)src.doubleRefCoords[srcOffset+1];
			vertexData[vOffset+2] = (float)src.doubleRefCoords[srcOffset+2];
			vOffset += stride;
		    }
		    break;
		case P3F: 
		    for (index=start; index < end; index++) {
			srcOffset = src.indexCoord[index];
			vertexData[vOffset] = src.p3fRefCoords[srcOffset].x;
			vertexData[vOffset+1] = src.p3fRefCoords[srcOffset].y;
			vertexData[vOffset+2] = src.p3fRefCoords[srcOffset].z;
			vOffset += stride;
		    }
		    break;
		case P3D: 
		    for (index=start; index < end; index++) {
			srcOffset = src.indexCoord[index];
			vertexData[vOffset] = (float)src.p3dRefCoords[srcOffset].x;
			vertexData[vOffset+1] = (float)src.p3dRefCoords[srcOffset].y;
			vertexData[vOffset+2] = (float)src.p3dRefCoords[srcOffset].z;
			vOffset += stride;
		    }
		    break;
		default:
		    break;
		}
	    }		

	}
    }


    private void unIndexifyNIOBuffer(IndexedGeometryArrayRetained src) {
//        System.err.println("unIndexifyNIOBuffer");

        int vOffset = 0, srcOffset, tOffset = 0;
        int index, colorStride = 0;
	float[] vdata = null;
        int i;
	int start, end;
	start = src.initialIndexIndex;
	end = src.initialIndexIndex + src.validIndexCount;
	// If its interleaved data then ..
	if ((src.vertexFormat & GeometryArray.INTERLEAVED) != 0) {
	    if ((src.vertexFormat & GeometryArray.WITH_ALPHA) != 0)
		colorStride = 4;
	    else if ((src.vertexFormat & GeometryArray.COLOR) != 0)
		colorStride = 3;

	    //	    System.out.println("===> start = "+start+" end = "+end);
	    for (index= start; index < end; index++) {
		if ((vertexFormat & GeometryArray.NORMALS) != 0){
		    src.interleavedFloatBufferImpl.position(src.indexNormal[index]*src.stride + src.normalOffset);
		    src.interleavedFloatBufferImpl.get(vertexData, vOffset + normalOffset, 3);
		}
		
		if (colorStride == 4){
		    src.interleavedFloatBufferImpl.position(src.indexColor[index]*src.stride + src.colorOffset);
		    src.interleavedFloatBufferImpl.get(vertexData, vOffset + colorOffset, colorStride);
		} else if (colorStride == 3) {
		    src.interleavedFloatBufferImpl.position(src.indexColor[index]*src.stride + src.colorOffset);
		    src.interleavedFloatBufferImpl.get(vertexData, vOffset + colorOffset, colorStride);
		    vertexData[vOffset + colorOffset + 3] = 1.0f;
		}
		
		if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
		    int tcOffset = vOffset + textureOffset;
		    for (i = 0; i < texCoordSetCount; 
				i++, tcOffset += texCoordStride) {
			
			src.interleavedFloatBufferImpl.position((src.indexTexCoord[i][index])*src.stride +
							    src.textureOffset);
			src.interleavedFloatBufferImpl.get(vertexData, tcOffset, texCoordStride);
		    }
		}
		if ((vertexFormat & GeometryArray.COORDINATES) != 0){
		    src.interleavedFloatBufferImpl.position(src.indexCoord[index]*src.stride + src.coordinateOffset );
		    src.interleavedFloatBufferImpl.get(vertexData, vOffset + coordinateOffset, 3);
		}
		vOffset += stride;
	    }

	} else {
	    if ((vertexFormat & GeometryArray.NORMALS) != 0){
		vOffset = normalOffset;
		if ((src.vertexType & NORMAL_DEFINED) != 0) {	
		    for (index=start; index < end; index++) {
			src.floatBufferRefNormals.position(src.indexNormal[index]*3);
			src.floatBufferRefNormals.get(vertexData, vOffset, 3);
			vOffset += stride;
		    }
		}
	    }

            if ((vertexFormat & GeometryArray.COLOR) != 0){
		vOffset = colorOffset;
		int multiplier = 3;
		if ((src.vertexFormat & GeometryArray.WITH_ALPHA) != 0)
		    multiplier = 4;
		
		switch ((src.vertexType & COLOR_DEFINED)) { 
		case CF:
		    for (index=start; index < end; index++) {
			if ((src.vertexFormat & GeometryArray.WITH_ALPHA) != 0) {
			    src.floatBufferRefColors.position(src.indexColor[index]*multiplier);
			    src.floatBufferRefColors.get(vertexData, vOffset, 4);
			}
			else {
			    src.floatBufferRefColors.position(src.indexColor[index]*multiplier);
			    src.floatBufferRefColors.get(vertexData, vOffset, 3);
			    vertexData[vOffset+3] = 1.0f;
			}
			vOffset += stride;
		    }
		    break;
		case CUB: 
		    for (index=start; index < end; index++) {
			srcOffset = src.indexColor[index] * multiplier;
			vertexData[vOffset] = (src.byteBufferRefColors.get(srcOffset) & 0xff) * ByteToFloatScale;
			vertexData[vOffset+1] = (src.byteBufferRefColors.get(srcOffset+1) & 0xff) * ByteToFloatScale;
			vertexData[vOffset+2] = (src.byteBufferRefColors.get(srcOffset+2) & 0xff) * ByteToFloatScale;

			if ((src.vertexFormat & GeometryArray.WITH_ALPHA) != 0) {
			    vertexData[vOffset+3] = (src.byteBufferRefColors.get(srcOffset+3) & 0xff) * ByteToFloatScale;
			}
			else {
			    vertexData[vOffset+3] = 1.0f;
			}
			vOffset += stride;
		    }
		    break;
		default:
		    break;
		}
	    }

            if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
		vOffset = textureOffset;
		 FloatBufferWrapper texBuffer;
		if ((src.vertexType & TEXCOORD_DEFINED) != 0) {
		    for (index=start; index < end; index++) {
			for (i = 0, tOffset = vOffset; 
				i < texCoordSetCount; i++) {
			    texBuffer = (FloatBufferWrapper)(((J3DBuffer) (src.refTexCoordsBuffer[i])).getBufferImpl());
			    texBuffer.position(src.indexTexCoord[i][index]*texCoordStride);
			    texBuffer.get(vertexData, tOffset, texCoordStride);
			    tOffset += texCoordStride;
			}
			vOffset += stride;
		    }
		}
	    }

            if ((vertexFormat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
		vOffset = 0;
		if ((src.vertexType & VATTR_DEFINED) == AF) {
		    for (index=start; index < end; index++) {
			for (i = 0; i < vertexAttrCount; i++) {
                            int vaOffset = vOffset + vertexAttrOffsets[i];
			    FloatBufferWrapper vaBuffer = src.floatBufferRefVertexAttrs[i];
			    vaBuffer.position(src.indexVertexAttr[i][index]*vertexAttrSizes[i]);
			    vaBuffer.get(vertexData, vaOffset, vertexAttrSizes[i]);
			}
			vOffset += stride;
		    }
		}
	    }

            if ((vertexFormat & GeometryArray.COORDINATES) != 0){
		vOffset = coordinateOffset;
		switch ((src.vertexType & VERTEX_DEFINED)) {
		case PF:
		    for (index=start; index < end; index++) {
			src.floatBufferRefCoords.position(src.indexCoord[index]*3);
			src.floatBufferRefCoords.get(vertexData, vOffset, 3);
			vOffset += stride;
		    }
		    break;
		case PD: 
		    for (index=start; index < end; index++) {
			srcOffset = src.indexCoord[index] * 3;
			vertexData[vOffset] = (float)src.doubleBufferRefCoords.get(srcOffset);
			vertexData[vOffset+1] = (float)src.doubleBufferRefCoords.get(srcOffset+1);
			vertexData[vOffset+2] = (float)src.doubleBufferRefCoords.get(srcOffset+2);
			vOffset += stride;
		    }
		    break;
		default:
		    break;
		}
	    }		

	}
    }


    /**
     * Returns the vertex stride in numbers of floats as a function 
     * of the vertexFormat.
     * @return the stride in floats for this vertex array
     */
    int stride()
    {
	int stride = 0;

	if((this.vertexFormat & GeometryArray.COORDINATES) != 0) stride += 3;
	if((this.vertexFormat & GeometryArray.NORMALS) != 0) stride += 3;

	if ((this.vertexFormat & GeometryArray.COLOR) != 0) {
	    if ((this.vertexFormat & GeometryArray.BY_REFERENCE) == 0) {
		// By copy
		stride += 4;
	    } else {
		if ((this.vertexFormat & GeometryArray.WITH_ALPHA) == 0) {
		    stride += 3;
		}
		else {
		    stride += 4;
		}
	    }
	}
	    
	if ((this.vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {

	    if ((this.vertexFormat & 
			GeometryArray.TEXTURE_COORDINATE_2) != 0) {
	        texCoordStride = 2;
	    } else if ((this.vertexFormat & 
			GeometryArray.TEXTURE_COORDINATE_3) != 0) {
		texCoordStride = 3;
	    } else if ((this.vertexFormat & 
			GeometryArray.TEXTURE_COORDINATE_4) != 0) {
		texCoordStride = 4;
	    }

	    stride += texCoordStride * texCoordSetCount;
	}

	if ((this.vertexFormat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
	    stride += vertexAttrStride;
	}

	//System.err.println("stride() = " + stride);
	return stride;
    }

    int[] texCoordSetMapOffset()
    {
	if (texCoordSetMap == null)
	    return null;
	
        texCoordSetMapOffset = new int[texCoordSetMap.length];
	for (int i = 0; i < texCoordSetMap.length; i++) {
	     if (texCoordSetMap[i] == -1) {
		 texCoordSetMapOffset[i] = -1;
	     } else {
	         texCoordSetMapOffset[i] = texCoordSetMap[i] * texCoordStride;
	     }
	}
	return texCoordSetMapOffset;
    }

    /**
     * Returns the stride of the set of vertex attributes. This is the
     * sum of the sizes of each vertex attribute.
     * @return the stride of the vertex attribute data
     */
    int vertexAttrStride() {
        int sum = 0;
        for (int i = 0; i < vertexAttrCount; i++) {
            sum += vertexAttrSizes[i];
        }
        return sum;
    }
    
    /**
     * Returns the offset in number of floats from the start of a vertex to
     * each per-vertex vertex attribute.
     * @return array of offsets in floats vertex start to the vertex attribute data
     */
    int[] vertexAttrOffsets() {
	int[] offsets;
        
        // Create array of offsets to the start of each vertex attribute.
        // The offset of the first attribute is always 0. If no vertex attributes exist,
        // then we will allocate an array of length 1 to avoid some checking elsewhere.
        if (vertexAttrCount > 0) {
            offsets = new int[vertexAttrCount];
        }
        else {
            offsets = new int[1];
        }
        offsets[0] = 0;
        for (int i = 1; i < vertexAttrCount; i++) {
            offsets[i] = offsets[i-1] + vertexAttrSizes[i-1];
        }
        
        return offsets;
    }

    /**
     * Returns the offset in number of floats from the start of a vertex to
     * the per-vertex texture coordinate data.
     * texture coordinate data always follows vertex attribute data
     * @return the offset in floats vertex start to the tetxure data
     */
    int textureOffset()
    {
	int offset = vertexAttrOffsets[0];

	if ((this.vertexFormat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
	    offset += vertexAttrStride;
	}

	return offset;
    }

    /**
     * Returns the offset in number of floats from the start of a vertex to
     * the per-vertex color data.
     * color data always follows texture data
     * @param vertexFormat the vertex format for this array
     * @return the offset in floats vertex start to the color data
     */
    int colorOffset()
    {
	int offset = textureOffset;

	if((this.vertexFormat & GeometryArray.TEXTURE_COORDINATE_2) != 0)
	    offset += 2 * texCoordSetCount;
	else if((this.vertexFormat & GeometryArray.TEXTURE_COORDINATE_3) != 0)
	    offset += 3 * texCoordSetCount;
	else if((this.vertexFormat & GeometryArray.TEXTURE_COORDINATE_4) != 0)
	    offset += 4 * texCoordSetCount;

	return offset;
    }

    /**
     * Returns the offset in number of floats from the start of a vertex to
     * the per-vertex normal data.
     * normal data always follows color data
     * @return the offset in floats from the start of a vertex to the normal
     */
    int normalOffset()
    {
	int offset = colorOffset;

	if ((this.vertexFormat & GeometryArray.COLOR) != 0) {
	    if ((this.vertexFormat & GeometryArray.BY_REFERENCE) == 0) {
		offset += 4;
	    } else {
		if ((this.vertexFormat & GeometryArray.WITH_ALPHA) == 0) {
		    offset += 3;
		}
		else {
		    offset += 4;
		}
	    }
	}
	return offset;
    }

    /**
     * Returns the offset in number of floats from the start of a vertex to
     * the per vertex coordinate data.
     * @return the offset in floats vertex start to the coordinate data
     */
    int coordinateOffset()
    {
	int offset = normalOffset;

	if ((this.vertexFormat & GeometryArray.NORMALS) != 0) offset += 3;
	return offset;
    }

    /**
     * Returns number of vertices in the GeometryArray
     * @return vertexCount number of vertices in the GeometryArray
     */
    int getVertexCount(){
	return vertexCount;
    }

    /**
     * Returns vertexFormat in the GeometryArray
     * @return vertexFormat format of vertices in the GeometryArray
     */
    int getVertexFormat(){
	return vertexFormat;
    }

    /**
     * Retrieves the number of vertex attributes in this GeometryArray
     * object.
     *
     * @return the number of vertex attributes in this GeometryArray
     * object
     */
    int getVertexAttrCount() {
        return vertexAttrCount;
    }


    /**
     * Retrieves the vertex attribute sizes array from this
     * GeometryArray object.
     *
     * @param vertexAttrSizes an array that will receive a copy of
     * the vertex attribute sizes array.  The array must hold at least
     * <code>vertexAttrCount</code> elements.
     */
    void getVertexAttrSizes(int[] vertexAttrSizes) {
        for (int i = 0; i < vertexAttrCount; i++) {
            vertexAttrSizes[i] = this.vertexAttrSizes[i];
        }
    }



    void sendDataChangedMessage(boolean coordinatesChanged) {
	J3dMessage[] m;
	int i, j, k, index, numShapeMessages, numMorphMessages;
	ArrayList shapeList;
	Shape3DRetained s;
	ArrayList morphList;
	MorphRetained morph;

	synchronized(liveStateLock) {
	    if (source != null && source.isLive()) {		
		// System.out.println("In GeometryArrayRetained - "); 

		// Send a message to renderBin to rebuild the display list or
		// process the vertex array accordingly
		// XXXX: Should I send one per universe, isn't display list
		// shared by all context/universes?
		int threads = J3dThread.UPDATE_RENDER;
		// If the geometry type is Indexed then we need to clone the geometry
		// We also need to update the cachedChangedFrequent flag
		threads |= J3dThread.UPDATE_RENDERING_ATTRIBUTES;

		synchronized (universeList) {
		    numShapeMessages = universeList.size();
		    m = new J3dMessage[numShapeMessages];

		    k = 0;

		    for (i = 0; i < numShapeMessages; i++, k++) {
			gaList.clear();

			shapeList = (ArrayList)userLists.get(i);
			for (j=0; j<shapeList.size(); j++) {
			    s = (Shape3DRetained)shapeList.get(j);
			    LeafRetained src = (LeafRetained)s.sourceNode;
			    // Should only need to update distinct localBounds.
			    if (coordinatesChanged && src.boundsAutoCompute) {
				src.boundsDirty = true;
			    }
			}
		
			for (j=0; j<shapeList.size(); j++) {
			    s = (Shape3DRetained)shapeList.get(j);
			    LeafRetained src = (LeafRetained)s.sourceNode;
			    if (src.boundsDirty) {
				// update combine bounds of mirrorShape3Ds. So we need to
				// use its bounds and not localBounds.
				// bounds is actually a reference to
				// mirrorShape3D.source.localBounds.
				src.updateBounds();
				src.boundsDirty = false;
			    }
			    gaList.add(Shape3DRetained.getGeomAtom(s));
			}

			m[k] = VirtualUniverse.mc.getMessage();
		
			m[k].type = J3dMessage.GEOMETRY_CHANGED;
			// Who to send this message to ?	
			m[k].threads = threads;
			m[k].args[0] = gaList.toArray();
			m[k].args[1] = this;
			m[k].args[2]= null;
			m[k].args[3] = new Integer(changedFrequent);
			m[k].universe=(VirtualUniverse)universeList.get(i);
		    }
		    VirtualUniverse.mc.processMessage(m);  
		}

		if (morphUniverseList != null) {
		    synchronized (morphUniverseList) {		
			numMorphMessages = morphUniverseList.size();

			// take care of morph that is referencing this geometry
			if (numMorphMessages > 0) {
			    synchronized (morphUniverseList) {
				for (i = 0; i < numMorphMessages; i++, k++) {
				    morphList = (ArrayList)morphUserLists.get(i);
				    for (j=0; j<morphList.size(); j++) {
					morph = (MorphRetained)morphList.get(j);
					morph.updateMorphedGeometryArray(this, coordinatesChanged);
				    }
				}
			    }
			}
		    }
		}
	    }
	}
	
    }
    /**
     * Sets the coordinate associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param coordinate an array of 3 values containing the new coordinate
     */
    void setCoordinate(int index, float coordinate[]) {
	int offset = this.stride * index + coordinateOffset;

	geomLock.getLock();
	dirtyFlag |= COORDINATE_CHANGED;

	this.vertexData[offset]  = coordinate[0];
	this.vertexData[offset+1]= coordinate[1];
	this.vertexData[offset+2]= coordinate[2];
    
	geomLock.unLock();
    
    	if (inUpdater || (source == null)) {
	    return;
	}
	if (!source.isLive()) {
	    boundsDirty = true;
	    return;
	}

	// Compute geo's bounds
	processCoordsChanged(false);
	sendDataChangedMessage(true);
    
    }

    /**
     * Sets the coordinate associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param coordinate an array of 3 values containing the new coordinate
     */
    void setCoordinate(int index, double coordinate[]) {
	int offset = this.stride * index + coordinateOffset;
    
    
    
	geomLock.getLock();
	dirtyFlag |= COORDINATE_CHANGED;
	this.vertexData[offset]  = (float)coordinate[0];
	this.vertexData[offset+1]= (float)coordinate[1];
	this.vertexData[offset+2]= (float)coordinate[2];
	geomLock.unLock();

    	if (inUpdater || (source == null)) {
	    return;
	}
	if (!source.isLive()) {
	    boundsDirty = true;
	    return;
	}    
    
	// Compute geo's bounds
	processCoordsChanged(false);
	sendDataChangedMessage(true);
    }

    /**
     * Sets the coordinate associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param coordinate a vector containing the new coordinate
     */
    void setCoordinate(int index, Point3f coordinate) {
	int offset = this.stride * index + coordinateOffset;
    
	geomLock.getLock();
	dirtyFlag |= COORDINATE_CHANGED;
	this.vertexData[offset]  = coordinate.x;
	this.vertexData[offset+1]= coordinate.y;
	this.vertexData[offset+2]= coordinate.z;
    
	geomLock.unLock();

    	if (inUpdater || (source == null)) {
	    return;
	}
	if (!source.isLive()) {
	    boundsDirty = true;
	    return;
	}

	// Compute geo's bounds
	processCoordsChanged(false);
	sendDataChangedMessage(true);
    }

    /**
     * Sets the coordinate associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param coordinate a vector containing the new coordinate
     */
    void setCoordinate(int index, Point3d coordinate) {
	int offset = this.stride * index + coordinateOffset;
    
	geomLock.getLock();    

	dirtyFlag |= COORDINATE_CHANGED;
	this.vertexData[offset]  = (float)coordinate.x;
	this.vertexData[offset+1]= (float)coordinate.y;
	this.vertexData[offset+2]= (float)coordinate.z;
    
	geomLock.unLock();

	if (inUpdater ||source == null ) {
	    return;
	}
	if (!source.isLive()) {
	    boundsDirty = true;
	    return;
	}

    
	// Compute geo's bounds
	processCoordsChanged(false);
	sendDataChangedMessage(true);
    }

    /**
     * Sets the coordinates associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param coordinates an array of 3*n values containing n new coordinates
     */
    void setCoordinates(int index, float coordinates[]) {
	int offset = this.stride * index + coordinateOffset;
	int i, j, num = coordinates.length;
    
	geomLock.getLock();
	dirtyFlag |= COORDINATE_CHANGED;

	for (i=0, j= offset;i < num; i+=3, j+= this.stride)
	    {
		this.vertexData[j]  = coordinates[i];
		this.vertexData[j+1]= coordinates[i+1];
		this.vertexData[j+2]= coordinates[i+2];
	    }

	geomLock.unLock();
	if (inUpdater ||source == null ) {
	    return;
	}
	if (!source.isLive()) {
	    boundsDirty = true;
	    return;
	}

	// Compute geo's bounds
	processCoordsChanged(false);

	sendDataChangedMessage(true);

    }

    /**
     * Sets the coordinates associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param coordinates an array of 3*n values containing n new coordinates
     */
    void setCoordinates(int index, double coordinates[]) {
	int offset = this.stride * index + coordinateOffset;
	int i, j, num = coordinates.length;
    
	geomLock.getLock();
	dirtyFlag |= COORDINATE_CHANGED;

	for (i=0, j= offset;i < num; i+=3, j+= this.stride)
	    {
		this.vertexData[j]  = (float)coordinates[i];
		this.vertexData[j+1]= (float)coordinates[i+1];
		this.vertexData[j+2]= (float)coordinates[i+2];
	    }
    
	geomLock.unLock();

	if (inUpdater ||source == null ) {
	    return;
	}
	if (!source.isLive()) {
	    boundsDirty = true;
	    return;
	}

    
	// Compute geo's bounds
	processCoordsChanged(false);

	sendDataChangedMessage(true);
    }

    /**
     * Sets the coordinates associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param coordinates an array of vectors containing new coordinates
     */
    void setCoordinates(int index, Point3f coordinates[]) {
	int offset = this.stride * index + coordinateOffset;
	int i, j, num = coordinates.length;
    
	geomLock.getLock();
	dirtyFlag |= COORDINATE_CHANGED;

	for (i=0, j= offset;i < num; i++, j+= this.stride)
	    {
		this.vertexData[j]  = coordinates[i].x;
		this.vertexData[j+1]= coordinates[i].y;
		this.vertexData[j+2]= coordinates[i].z;
	    }
    
	geomLock.unLock();

	if (inUpdater ||source == null ) {
	    return;
	}
	if (!source.isLive()) {
	    boundsDirty = true;
	    return;
	}

	// Compute geo's bounds
	processCoordsChanged(false);

	sendDataChangedMessage(true);
    
    }

    /**
     * Sets the coordinates associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param coordinates an array of vectors containing new coordinates
     */
    void setCoordinates(int index, Point3d coordinates[]) {
	int offset = this.stride * index + coordinateOffset;
	int i, j, num = coordinates.length;
    
	geomLock.getLock();
	dirtyFlag |= COORDINATE_CHANGED;

	for (i=0, j= offset;i < num; i++, j+= this.stride)
	    {
		this.vertexData[j]  = (float)coordinates[i].x;
		this.vertexData[j+1]= (float)coordinates[i].y;
		this.vertexData[j+2]= (float)coordinates[i].z;
	    }

	geomLock.unLock();

    	if (inUpdater ||source == null ) {
	    return;
	}
	if (!source.isLive()) {
	    boundsDirty = true;
	    return;
	}

	// Compute geo's bounds
	processCoordsChanged(false);

	sendDataChangedMessage(true);
    }


    /** 
     * Sets the coordinates associated with the vertices starting at 
     * the specified index for this object using coordinate data starting 
     * from vertex index <code>start</code> for <code>length</code> vertices. 
     * @param index the vertex index 
     * @param coordinates an array of vectors containing new coordinates 
     * @param start starting vertex index of data in <code>coordinates</code>  .
     * @param length number of vertices to be copied. 
     */ 
    void setCoordinates(int index, float coordinates[], int start, int length) {
	int offset = this.stride * index + coordinateOffset;
	int i, j;
    
	geomLock.getLock();
	dirtyFlag |= COORDINATE_CHANGED;
	for (i= start * 3, j= offset; i < (start+length) * 3; 
	     i+=3, j+= this.stride) {
	    this.vertexData[j]  = coordinates[i];
	    this.vertexData[j+1]= coordinates[i+1];
	    this.vertexData[j+2]= coordinates[i+2];
	}
    
	geomLock.unLock();
	if (inUpdater ||source == null ) {
	    return;
	}
	if (!source.isLive()) {
	    boundsDirty = true;
	    return;
	}

	// Compute geo's bounds
	processCoordsChanged(false);

	sendDataChangedMessage(true);
    }     

    /**
     * Sets the coordinates associated with the vertices starting at
     * the specified index for this object  using coordinate data starting
     * from vertex index <code>start</code> for <code>length</code> vertices.
     * @param index the vertex index
     * @param coordinates an array of 3*n values containing n new coordinates
     * @param start starting vertex index of data in <code>coordinates</code>  .
     * @param length number of vertices to be copied.
     */
    void setCoordinates(int index, double coordinates[], int start, int length) { 
	int offset = this.stride * index + coordinateOffset; 
	int i, j;

	geomLock.getLock();
	dirtyFlag |= COORDINATE_CHANGED;

	for (i= start*3, j= offset; i < (start+length)*3; 		
	     i+=3, j+= this.stride) {
	    this.vertexData[j]  = (float)coordinates[i]; 
	    this.vertexData[j+1]= (float)coordinates[i+1]; 
	    this.vertexData[j+2]= (float)coordinates[i+2]; 
	} 

	geomLock.unLock();

    	if (inUpdater || (source == null)) {
	    return;
	}
	if (!source.isLive()) {
	    boundsDirty = true;
	    return;
	}    

    
	// Compute geo's bounds
	processCoordsChanged(false);

	sendDataChangedMessage(true);
    } 

    /**
     * Sets the coordinates associated with the vertices starting at
     * the specified index for this object using coordinate data starting
     * from vertex index <code>start</code> for <code>length</code> vertices.
     * @param index the vertex index
     * @param coordinates an array of vectors containing new coordinates
     * @param start starting vertex index of data in <code>coordinates</code>  .
     * @param length number of vertices to be copied.
     */
    void setCoordinates(int index, Point3f coordinates[], int start, 
			int length) {
	int offset = this.stride * index + coordinateOffset;
	int i, j;
    
	geomLock.getLock();
	dirtyFlag |= COORDINATE_CHANGED;

	for (i=start, j= offset;i < start + length; i++, j+= this.stride) {
	    this.vertexData[j]  = coordinates[i].x;
	    this.vertexData[j+1]= coordinates[i].y;
	    this.vertexData[j+2]= coordinates[i].z;
	}

	geomLock.unLock();


    	if (inUpdater || (source == null)) {
	    return;
	}
	if (!source.isLive()) {
	    boundsDirty = true;
	    return;
	}    

    
	// Compute geo's bounds
	processCoordsChanged(false);

	sendDataChangedMessage(true);
    }
  
    /** 
     * Sets the coordinates associated with the vertices starting at 
     * the specified index for this object using coordinate data starting 
     * from vertex index <code>start</code> for <code>length</code> vertices. 
     * @param index the vertex index 
     * @param coordinates an array of vectors containing new coordinates 
     * @param start starting vertex index of data in <code>coordinates</code>  .
     * @param length number of vertices to be copied. 
     */ 
    void setCoordinates(int index, Point3d coordinates[], int start, 
			int length) {
	int offset = this.stride * index + coordinateOffset;
	int i, j;
    
	geomLock.getLock();
	dirtyFlag |= COORDINATE_CHANGED;

	for (i=start, j= offset;i < start + length; i++, j+= this.stride) {
	    this.vertexData[j]  = (float)coordinates[i].x;
	    this.vertexData[j+1]= (float)coordinates[i].y;
	    this.vertexData[j+2]= (float)coordinates[i].z;
	}
	
	geomLock.unLock();


    	if (inUpdater || (source == null)) {
	    return;
	}
	if (!source.isLive()) {
	    boundsDirty = true;
	    return;
	}    
    
	// Compute geo's bounds
	processCoordsChanged(false);

	sendDataChangedMessage(true);
    }    

    /**
     * Sets the color associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param color an array of 3 or 4 values containing the new color
     */
    void setColor(int index, float color[]) {
	int offset = this.stride*index + colorOffset;
    
	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;
	this.vertexData[offset]   = color[0];
	this.vertexData[offset+1] = color[1];
	this.vertexData[offset+2] = color[2];
	if ((this.vertexFormat & GeometryArray.WITH_ALPHA) != 0)
	    this.vertexData[offset+3] = color[3]*lastAlpha[0];
	else
	    this.vertexData[offset+3] = lastAlpha[0];

	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);

    }

    /**
     * Sets the color associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param color an array of 3 or 4 values containing the new color
     */
    void setColor(int index, byte color[]) {
	int offset = this.stride*index + colorOffset;

	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;
	this.vertexData[offset]   = (color[0] & 0xff) * ByteToFloatScale;
	this.vertexData[offset+1] = (color[1] & 0xff) * ByteToFloatScale;
	this.vertexData[offset+2] = (color[2] & 0xff) * ByteToFloatScale;
	if ((this.vertexFormat & GeometryArray.WITH_ALPHA) != 0)
	    this.vertexData[offset+3] = ((color[3] & 0xff)* ByteToFloatScale)*lastAlpha[0];
        else
	    this.vertexData[offset+3] = lastAlpha[0];

	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);

    }

    /**
     * Sets the color associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param color a vector containing the new color
     */
    void setColor(int index, Color3f color) {
	int offset = this.stride*index + colorOffset;

	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;
	this.vertexData[offset]   = color.x;
	this.vertexData[offset+1] = color.y;
	this.vertexData[offset+2] = color.z;
        this.vertexData[offset+3] = lastAlpha[0];

	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);

    }

    /**
     * Sets the color associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param color a vector containing the new color
     */
    void setColor(int index, Color4f color) {
	int offset = this.stride*index + colorOffset;

	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;
	this.vertexData[offset]   = color.x;
	this.vertexData[offset+1] = color.y;
	this.vertexData[offset+2] = color.z;
	this.vertexData[offset+3] = color.w*lastAlpha[0];

	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}
	geomLock.unLock();
	sendDataChangedMessage(false);

    }

    /**
     * Sets the color associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param color a vector containing the new color
     */
    void setColor(int index, Color3b color) {
	int offset = this.stride*index + colorOffset;

	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;
	this.vertexData[offset]   = (color.x & 0xff) * ByteToFloatScale;
	this.vertexData[offset+1] = (color.y & 0xff) * ByteToFloatScale;
	this.vertexData[offset+2] = (color.z & 0xff) * ByteToFloatScale;
        this.vertexData[offset+3] = lastAlpha[0];

	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);

    }

    /**
     * Sets the color associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param color a vector containing the new color
     */
    void setColor(int index, Color4b color) {
	int offset = this.stride*index + colorOffset;

	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;
	this.vertexData[offset]   = (color.x * 0xff) * ByteToFloatScale;
	this.vertexData[offset+1] = (color.y * 0xff) * ByteToFloatScale;
	this.vertexData[offset+2] = (color.z * 0xff) * ByteToFloatScale;
	this.vertexData[offset+3] = ((color.w & 0xff) * ByteToFloatScale)*lastAlpha[0];
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);

    }

    /**
     * Sets the colors associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param colors an array of 3*n or 4*n values containing n new colors
     */
    void setColors(int index, float colors[]) {
	int offset = this.stride*index + colorOffset;
	int i, j, num = colors.length;

	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;

	if ((this.vertexFormat & GeometryArray.WITH_ALPHA) != 0)
	    {
		for (i=0, j= offset;i < num; i+= 4, j+= this.stride)
		    {
			this.vertexData[j]   = colors[i];
			this.vertexData[j+1] = colors[i+1];
			this.vertexData[j+2] = colors[i+2];
			this.vertexData[j+3] = colors[i+3]*lastAlpha[0];
		    }
	    }
	else
	    {
		for (i=0, j= offset;i < num; i+= 3, j+= this.stride)
		    {
			this.vertexData[j]   = colors[i];
			this.vertexData[j+1] = colors[i+1];
			this.vertexData[j+2] = colors[i+2];
			this.vertexData[j+3] = lastAlpha[0];
		    }
	    }
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);

    }

    /**
     * Sets the colors associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param colors an array of 3*n or 4*n values containing n new colors
     */
    void setColors(int index, byte colors[]) {
	int offset = this.stride*index + colorOffset;
	int i, j, num = colors.length;

	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;

	if ((this.vertexFormat & GeometryArray.WITH_ALPHA) != 0)
	    {
		for (i=0, j= offset;i < num; i+= 4, j+= this.stride)
		    {
			this.vertexData[j]   = (colors[i] & 0xff) * ByteToFloatScale;
			this.vertexData[j+1] = (colors[i+1] & 0xff) * ByteToFloatScale;
			this.vertexData[j+2] = (colors[i+2] & 0xff) * ByteToFloatScale;
			this.vertexData[j+3] = ((colors[i+3] & 0xff) * ByteToFloatScale)*lastAlpha[0];
		    }
	    }
	else
	    {
		for (i=0, j= offset;i < num; i+= 3, j+= this.stride)
		    {
			this.vertexData[j]   = (colors[i] & 0xff) * ByteToFloatScale;
			this.vertexData[j+1] = (colors[i+1] & 0xff) * ByteToFloatScale;
			this.vertexData[j+2] = (colors[i+2] & 0xff) * ByteToFloatScale;
			this.vertexData[j+3] = lastAlpha[0];
		    }
	    }
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);

    }

    /**
     * Sets the colors associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param colors an array of vectors containing new colors
     */
    void setColors(int index, Color3f colors[]) {
	int offset = this.stride*index + colorOffset;
	int i, j, num = colors.length;

	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;

	for (i=0, j= offset;i < num; i++, j+= this.stride)
	    {
		this.vertexData[j]   = colors[i].x;
		this.vertexData[j+1] = colors[i].y;
		this.vertexData[j+2] = colors[i].z;
		this.vertexData[j+3] = lastAlpha[0];
	    }
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);

    }

    /**
     * Sets the colors associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param colors an array of vectors containing new colors
     */
    void setColors(int index, Color4f colors[]) {
	int offset = this.stride*index + colorOffset;
	int i, j, num = colors.length;

	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;

	for (i=0, j= offset;i < num; i++, j+= this.stride)
	    {
		this.vertexData[j]   = colors[i].x;
		this.vertexData[j+1] = colors[i].y;
		this.vertexData[j+2] = colors[i].z;
		this.vertexData[j+3] = colors[i].w*lastAlpha[0];
	    }
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);

    }

    /**
     * Sets the colors associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param colors an array of vectors containing new colors
     */
    void setColors(int index, Color3b colors[]) {
	int offset = this.stride*index + colorOffset;
	int i, j, num = colors.length;

	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;
	for (i=0, j= offset;i < num; i++, j+= this.stride)
	    {
		this.vertexData[j]   = (colors[i].x & 0xff) * ByteToFloatScale;
		this.vertexData[j+1] = (colors[i].y & 0xff) * ByteToFloatScale;
		this.vertexData[j+2] = (colors[i].z & 0xff) * ByteToFloatScale;
		this.vertexData[j+3] = lastAlpha[0];
	    }
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);
    }


    /**
     * Sets the colors associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param colors an array of vectors containing new colors
     */
    void setColors(int index, Color4b colors[]) {
	int offset = this.stride*index + colorOffset;
	int i, j, num = colors.length;

	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;

	for (i=0, j= offset;i < num; i++, j+= this.stride)
	    {
		this.vertexData[j]   = (colors[i].x & 0xff) * ByteToFloatScale;
		this.vertexData[j+1] = (colors[i].y & 0xff) * ByteToFloatScale;
		this.vertexData[j+2] = (colors[i].z & 0xff) * ByteToFloatScale;
		this.vertexData[j+3] = ((colors[i].w & 0xff) * ByteToFloatScale)*lastAlpha[0];
	    }
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);
    
    }

    /**
     * Sets the colors associated with the vertices starting at
     * the specified index for this object using data in <code>color</code>s
     * starting at index <code>start</code> for <code>length</code> colors.
     * @param index the vertex index
     * @param colors an array of 3*n or 4*n values containing n new colors
     * @param start starting color index of data in <code>colors</code>.
     * @param length number of colors to be copied.
     */
    void setColors(int index, float colors[], int start, int length) {
        int offset = this.stride*index + colorOffset;
        int i, j;

	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;

	if ((this.vertexFormat & GeometryArray.WITH_ALPHA) != 0) {
            for (i = start * 4, j = offset; i < (start + length) * 4;
		 i += 4, j += this.stride) {
                this.vertexData[j]   = colors[i];
                this.vertexData[j+1] = colors[i+1];
                this.vertexData[j+2] = colors[i+2];
                this.vertexData[j+3] = colors[i+3]*lastAlpha[0];
            }
        } else {
            for (i = start * 3, j = offset; i < (start + length) * 3;
		 i += 3, j += this.stride) {
                this.vertexData[j]   = colors[i];
                this.vertexData[j+1] = colors[i+1];
                this.vertexData[j+2] = colors[i+2];
                this.vertexData[j+3] = lastAlpha[0];
            }
        }
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);

    }

    /**
     * Sets the colors associated with the vertices starting at
     * the specified index for this object using data in <code>color</code>s
     * starting at index <code>start</code> for <code>length</code> colors.
     * @param index the vertex index
     * @param colors an array of 3*n or 4*n values containing n new colors
     * @param start starting color index of data in <code>colors</code>.
     * @param length number of colors to be copied.
     */
    void setColors(int index, byte colors[], int start, int length) {
        int offset = this.stride*index + colorOffset;
        int i, j;
 
	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;

	if ((this.vertexFormat & GeometryArray.WITH_ALPHA) != 0) {
            for (i = start * 4, j = offset; i < (start + length) * 4; 
		 i += 4, j += this.stride) { 
                this.vertexData[j]   = (colors[i] & 0xff) * ByteToFloatScale;
                this.vertexData[j+1] = (colors[i+1] & 0xff) * ByteToFloatScale;
                this.vertexData[j+2] = (colors[i+2] & 0xff) * ByteToFloatScale;
                this.vertexData[j+3] = ((colors[i+3] & 0xff) * ByteToFloatScale)*lastAlpha[0];
            }
        } else { 
            for (i = start * 3, j = offset; i < (start + length) * 3;
		 i += 3, j += this.stride) {
                this.vertexData[j]   = (colors[i] & 0xff) * ByteToFloatScale;
                this.vertexData[j+1] = (colors[i+1] & 0xff) * ByteToFloatScale;
                this.vertexData[j+2] = (colors[i+2] & 0xff) * ByteToFloatScale;
                this.vertexData[j+3] = lastAlpha[0];
            }
        }
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);

    }

    /** 
     * Sets the colors associated with the vertices starting at
     * the specified index for this object using data in <code>color</code>s 
     * starting at index <code>start</code> for <code>length</code> colors. 
     * @param index the vertex index 
     * @param colors an array of 3*n or 4*n values containing n new colors 
     * @param start starting color index of data in <code>colors</code>. 
     * @param length number of colors to be copied. 
     */ 
    void setColors(int index, Color3f colors[], int start, int length) { 
        int offset = this.stride*index + colorOffset;
        int i, j;
 
	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;

	for (i = start, j = offset; i < start+length; i++, j += this.stride) {
            this.vertexData[j]   = colors[i].x;
            this.vertexData[j+1] = colors[i].y;
            this.vertexData[j+2] = colors[i].z;
            this.vertexData[j+3] = lastAlpha[0];
        }
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);

    }      

    /**
     * Sets the colors associated with the vertices starting at
     * the specified index for this object using data in <code>color</code>s
     * starting at index <code>start</code> for <code>length</code> colors.
     * @param index the vertex index
     * @param colors an array of 3*n or 4*n values containing n new colors
     * @param start starting color index of data in <code>colors</code>.
     * @param length number of colors to be copied.
     */
    void setColors(int index, Color4f colors[], int start, int length) {
        int offset = this.stride*index + colorOffset;
        int i, j;
 
	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;

	for (i = start, j = offset; i < start+length; i++, j += this.stride) {
            this.vertexData[j]   = colors[i].x;
            this.vertexData[j+1] = colors[i].y;
            this.vertexData[j+2] = colors[i].z;
            this.vertexData[j+3] = colors[i].w*lastAlpha[0];
        }
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);

    }      

    /**
     * Sets the colors associated with the vertices starting at
     * the specified index for this object using data in <code>color</code>s
     * starting at index <code>start</code> for <code>length</code> colors.
     * @param index the vertex index
     * @param colors an array of 3*n or 4*n values containing n new colors
     * @param start starting color index of data in <code>colors</code>.
     * @param length number of colors to be copied.
     */
    void setColors(int index, Color3b colors[], int start, int length) {
        int offset = this.stride*index + colorOffset;
        int i, j;

	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;

	for (i = start, j = offset; i < start+length; i++, j += this.stride) { 
            this.vertexData[j]   = (colors[i].x & 0xff) * ByteToFloatScale;
            this.vertexData[j+1] = (colors[i].y & 0xff) * ByteToFloatScale;
            this.vertexData[j+2] = (colors[i].z & 0xff) * ByteToFloatScale;
            this.vertexData[j+3] = lastAlpha[0];
        }
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);

    } 

    /** 
     * Sets the colors associated with the vertices starting at
     * the specified index for this object using data in <code>color</code>s 
     * starting at index <code>start</code> for <code>length</code> colors. 
     * @param index the vertex index 
     * @param colors an array of 3*n or 4*n values containing n new colors 
     * @param start starting color index of data in <code>colors</code>. 
     * @param length number of colors to be copied. 
     */   
    void setColors(int index, Color4b colors[], int start, int length) { 
        int offset = this.stride*index + colorOffset; 
        int i, j;
 
	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;

	for (i = start, j = offset; i < start+length; i++, j += this.stride) {
	    this.vertexData[j]   = (colors[i].x & 0xff) * ByteToFloatScale; 
	    this.vertexData[j+1] = (colors[i].y & 0xff) * ByteToFloatScale; 
	    this.vertexData[j+2] = (colors[i].z & 0xff) * ByteToFloatScale; 
	    this.vertexData[j+3] = ((colors[i].w & 0xff) * ByteToFloatScale)*lastAlpha[0];
        } 
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);

    }  

    /**
     * Sets the normal associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param normal the new normal
     */
    void setNormal(int index, float normal[]) {
	int offset = this.stride*index + normalOffset;

	geomLock.getLock();


	this.vertexData[offset]   = normal[0];
	this.vertexData[offset+1] = normal[1];
	this.vertexData[offset+2] = normal[2];
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);

    }

    /**
     * Sets the normal associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param normal the vector containing the new normal
     */
    void setNormal(int index, Vector3f normal) {
	int offset = this.stride*index + normalOffset;

	geomLock.getLock();

	dirtyFlag |= NORMAL_CHANGED;
	this.vertexData[offset]   = normal.x;
	this.vertexData[offset+1] = normal.y;
	this.vertexData[offset+2] = normal.z;
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);

    }

    /**
     * Sets the normals associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param normals the new normals
     */
    void setNormals(int index, float normals[]) {
	int offset = this.stride*index + normalOffset;
	int i, j, num = normals.length;

	geomLock.getLock();

	dirtyFlag |= NORMAL_CHANGED;
	for (i=0, j= offset;i < num;i += 3, j+= this.stride)
	    {
		this.vertexData[j]   = normals[i];
		this.vertexData[j+1] = normals[i+1];
		this.vertexData[j+2] = normals[i+2];
	    }
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);

    }

    /**
     * Sets the normals associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param normals the vector containing the new normals
     */
    void setNormals(int index, Vector3f normals[]) {
	int offset = this.stride*index + normalOffset;
	int i, j, num = normals.length;

	geomLock.getLock();

	dirtyFlag |= NORMAL_CHANGED;
	for (i=0, j= offset;i < num;i++, j+= this.stride)
	    {
		this.vertexData[j]   = normals[i].x;
		this.vertexData[j+1] = normals[i].y;
		this.vertexData[j+2] = normals[i].z;
	    }
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);

    }

    /**
     * Sets the normals associated with the vertices starting at
     * the specified index for this object using data in <code>normals</code>
     * starting at index <code>start</code> and  ending at index <code>start+length</code>.
     * @param index the vertex index
     * @param normals the new normals
     * @param start starting normal index of data in <code>colors</code>  .
     * @param length number of normals to be copied.
     */
    void setNormals(int index, float normals[], int start, int length) {
        int offset = this.stride*index + normalOffset;
        int i, j;

	geomLock.getLock();

	dirtyFlag |= NORMAL_CHANGED;
	for (i = start * 3, j = offset; i < (start + length) * 3;
	     i+=3, j += this.stride) {
	    this.vertexData[j]   = normals[i];
	    this.vertexData[j+1] = normals[i+1];
	    this.vertexData[j+2] = normals[i+2];
        }
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);

    }

    /**
     * Sets the normals associated with the vertices starting at
     * the specified index for this object using data in <code>normals</code>
     * starting at index <code>start</code> and  ending at index <code>start+length</code>.
     * @param index the vertex index
     * @param normals the new normals
     * @param start starting normal index of data in <code>colors</code>  .
     * @param length number of normals to be copied.
     */
    void setNormals(int index, Vector3f normals[], int start, int length) {
        int offset = this.stride*index + normalOffset;
        int i, j;

	geomLock.getLock();

	dirtyFlag |= NORMAL_CHANGED;
	for (i = start, j = offset; i < start+length; i++, j += this.stride) {
	    this.vertexData[j]   = normals[i].x;
	    this.vertexData[j+1] = normals[i].y;
	    this.vertexData[j+2] = normals[i].z;
        }
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);

    }


    /**
     * Sets the texture coordinates associated with the vertices starting at
     * the specified index for this object using data in <code>texCoords</code>
     * starting at index <code>start</code> and ending at index <code>start+length</code>.
     * @param index the vertex index
     * @param texCoords the new texture coordinates
     * @param start starting texture coordinate index of data in <code>texCoords</code>  .
     * @param length number of texture Coordinates to be copied.
     */
    void setTextureCoordinates(int texCoordSet, int index, float texCoords[], 
				int start, int length) {

	if ((this.vertexFormat & GeometryArray.BY_REFERENCE) != 0)
            throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

        if ((this.vertexFormat & GeometryArray.TEXTURE_COORDINATE ) == 0)
            throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray79"));

	int offset = this.stride*index + textureOffset + 
			texCoordSet * texCoordStride;
        int i, j, k;

	geomLock.getLock();
	dirtyFlag |= TEXTURE_CHANGED;

	if ((this.vertexFormat & GeometryArray.TEXTURE_COORDINATE_4) != 0) {
            for (i = start * 4, j = offset, k = 0; k < length;
		 	j += this.stride, k++) {
                this.vertexData[j]   = texCoords[i++];
                this.vertexData[j+1] = texCoords[i++];
                this.vertexData[j+2] = texCoords[i++];
                this.vertexData[j+3] = texCoords[i++];
            }
	} else if ((this.vertexFormat & 
			GeometryArray.TEXTURE_COORDINATE_3) != 0) {
            for (i = start * 3, j = offset, k = 0; k < length;
		 	j += this.stride, k++) {
                this.vertexData[j]   = texCoords[i++];
                this.vertexData[j+1] = texCoords[i++];
                this.vertexData[j+2] = texCoords[i++];
            }
        } else {
            for (i = start * 2, j = offset, k = 0; k < length;
		 	j += this.stride, k++) {
                this.vertexData[j]   = texCoords[i++];
                this.vertexData[j+1] = texCoords[i++];
            }
        }
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);

    }

    /**
     * Sets the texture coordinates associated with the vertices starting at
     * the specified index for this object using data in <code>texCoords</code>
     * starting at index <code>start</code> and ending at index <code>start+length</code>.
     * @param index the vertex index
     * @param texCoords the new texture coordinates
     * @param start starting texture coordinate index of data in <code>texCoords</code>  .
     * @param length number of texture Coordinates to be copied.
     */
    void setTextureCoordinates(int texCoordSet, int index, Point2f texCoords[],
				int start, int length) {

	if ((this.vertexFormat & GeometryArray.BY_REFERENCE) != 0)
            throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

        if ((this.vertexFormat & GeometryArray.TEXTURE_COORDINATE ) == 0)
            throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray79"));

	int offset = this.stride*index + textureOffset + 
			texCoordSet * texCoordStride;
        int i, j;

	geomLock.getLock();
	dirtyFlag |= TEXTURE_CHANGED;

	for (i = start, j = offset; i < start+length; i++, j += this.stride) {
            this.vertexData[j]   = texCoords[i].x;
            this.vertexData[j+1] = texCoords[i].y;
        }
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);

    }

    /**
     * Sets the texture coordinates associated with the vertices starting at
     * the specified index for this object using data in <code>texCoords</code>
     * starting at index <code>start</code> and ending at index <code>start+length</code>.
     * @param index the vertex index
     * @param texCoords the new texture coordinates
     * @param start starting texture coordinate index of data in <code>texCoords</code>  .
     * @param length number of texture Coordinates to be copied.
     */
    void setTextureCoordinates(int texCoordSet, int index, Point3f texCoords[],
				int start, int length) {

	if ((this.vertexFormat & GeometryArray.BY_REFERENCE) != 0)
            throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

        if ((this.vertexFormat & GeometryArray.TEXTURE_COORDINATE ) == 0)
            throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray79"));

	int offset = this.stride*index + textureOffset + 
			texCoordSet * texCoordStride;
        int i, j;
 
	geomLock.getLock();
	dirtyFlag |= TEXTURE_CHANGED;

	for (i = start, j = offset; i < start+length; i++, j += this.stride) {
            this.vertexData[j]   = texCoords[i].x;
            this.vertexData[j+1] = texCoords[i].y;
            this.vertexData[j+2] = texCoords[i].z;
        }
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}
	
	geomLock.unLock();
	sendDataChangedMessage(false);

    }      

    /**
     * Sets the texture coordinates associated with the vertices starting at
     * the specified index for this object using data in <code>texCoords</code>
     * starting at index <code>start</code> and ending at index <code>start+length</code>.
     * @param index the vertex index
     * @param texCoords the new texture coordinates
     * @param start starting texture coordinate index of data in <code>texCoords</code>  .
     * @param length number of texture Coordinates to be copied.
     */
    void setTextureCoordinates(int texCoordSet, int index, TexCoord2f texCoords[],
				int start, int length) {

	geomLock.getLock();
	dirtyFlag |= TEXTURE_CHANGED;

	if ((this.vertexFormat & GeometryArray.BY_REFERENCE) != 0)
            throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

        if ((this.vertexFormat & GeometryArray.TEXTURE_COORDINATE ) == 0)
            throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray79"));

	int offset = this.stride*index + textureOffset + 
			texCoordSet * texCoordStride;
        int i, j;

	for (i = start, j = offset; i < start+length; i++, j += this.stride) {
            this.vertexData[j]   = texCoords[i].x;
            this.vertexData[j+1] = texCoords[i].y;
        }
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);

    }

    /**
     * Sets the texture coordinates associated with the vertices starting at
     * the specified index for this object using data in <code>texCoords</code>
     * starting at index <code>start</code> and ending at index <code>start+length</code>.
     * @param index the vertex index
     * @param texCoords the new texture coordinates
     * @param start starting texture coordinate index of data in <code>texCoords</code>  .
     * @param length number of texture Coordinates to be copied.
     */
    void setTextureCoordinates(int texCoordSet, int index, 
				TexCoord3f texCoords[],
				int start, int length) {

	geomLock.getLock();
	dirtyFlag |= TEXTURE_CHANGED;

	if ((this.vertexFormat & GeometryArray.BY_REFERENCE) != 0)
            throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

        if ((this.vertexFormat & GeometryArray.TEXTURE_COORDINATE ) == 0)
            throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray79"));

	int offset = this.stride*index + textureOffset + 
			texCoordSet * texCoordStride;
        int i, j;
 
	for (i = start, j = offset; i < start+length; i++, j += this.stride) {
            this.vertexData[j]   = texCoords[i].x;
            this.vertexData[j+1] = texCoords[i].y;
            this.vertexData[j+2] = texCoords[i].z;
        }
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}
	
	geomLock.unLock();
	sendDataChangedMessage(false);
    }      

    /**
     * Sets the texture coordinates associated with the vertices starting at
     * the specified index for this object using data in <code>texCoords</code>
     * starting at index <code>start</code> and ending at index <code>start+length</code>.
     * @param index the vertex index
     * @param texCoords the new texture coordinates
     * @param start starting texture coordinate index of data in <code>texCoords</code>  .
     * @param length number of texture Coordinates to be copied.
     */
    void setTextureCoordinates(int texCoordSet, int index, 
				TexCoord4f texCoords[],
				int start, int length) {

	geomLock.getLock();
	dirtyFlag |= TEXTURE_CHANGED;

	if ((this.vertexFormat & GeometryArray.BY_REFERENCE) != 0)
            throw new IllegalStateException(J3dI18N.getString("GeometryArray82"));

        if ((this.vertexFormat & GeometryArray.TEXTURE_COORDINATE ) == 0)
            throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray79"));

	int offset = this.stride*index + textureOffset + 
			texCoordSet * texCoordStride;
        int i, j;
 
	for (i = start, j = offset; i < start+length; i++, j += this.stride) {
            this.vertexData[j]   = texCoords[i].x;
            this.vertexData[j+1] = texCoords[i].y;
            this.vertexData[j+2] = texCoords[i].z;
            this.vertexData[j+3] = texCoords[i].w;
        }
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}
	
	geomLock.unLock();
	sendDataChangedMessage(false);
    }


    /**
     * Sets the vertex attribute associated with the vertex at the
     * specified index in the specified vertex attribute number for
     * this object.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index destination vertex index in this geometry array
     * @param vertexAttr the Point2f containing the new vertex attribute
     */
    void setVertexAttr(int vertexAttrNum, int index,
		       Point2f vertexAttr) {

	int offset = this.stride*index + vertexAttrOffsets[vertexAttrNum];

	geomLock.getLock();
	dirtyFlag |= VATTR_CHANGED;

	this.vertexData[offset] = vertexAttr.x;
	this.vertexData[offset+1] = vertexAttr.y;
        
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);
    }

    /**
     * Sets the vertex attribute associated with the vertex at the
     * specified index in the specified vertex attribute number for
     * this object.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index destination vertex index in this geometry array
     * @param vertexAttr the Point3f containing the new vertex attribute
     */
    void setVertexAttr(int vertexAttrNum, int index,
		       Point3f vertexAttr) {

	int offset = this.stride*index + vertexAttrOffsets[vertexAttrNum];

	geomLock.getLock();
	dirtyFlag |= VATTR_CHANGED;

	this.vertexData[offset] = vertexAttr.x;
	this.vertexData[offset+1] = vertexAttr.y;
	this.vertexData[offset+2] = vertexAttr.z;
        
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);
    }

    /**
     * Sets the vertex attribute associated with the vertex at the
     * specified index in the specified vertex attribute number for
     * this object.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index destination vertex index in this geometry array
     * @param vertexAttr the Point4f containing the new vertex attribute
     */
    void setVertexAttr(int vertexAttrNum, int index,
		       Point4f vertexAttr) {

	int offset = this.stride*index + vertexAttrOffsets[vertexAttrNum];

	geomLock.getLock();
	dirtyFlag |= VATTR_CHANGED;

	this.vertexData[offset] = vertexAttr.x;
	this.vertexData[offset+1] = vertexAttr.y;
	this.vertexData[offset+2] = vertexAttr.z;
	this.vertexData[offset+3] = vertexAttr.w;
        
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);
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
     */
    void setVertexAttrs(int vertexAttrNum, int index,
			float[] vertexAttrs,
			int start, int length) {

	int offset = this.stride*index + vertexAttrOffsets[vertexAttrNum];
        int size = vertexAttrSizes[vertexAttrNum];
        int i, j, k;

	geomLock.getLock();
	dirtyFlag |= VATTR_CHANGED;

        for (i = start * size, j = offset, k = 0; k < length; i += size, j += this.stride, k++) {
            for (int ii = 0; ii < size; ii++) {
                this.vertexData[j+ii] = vertexAttrs[i+ii];
            }
        }
        
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);
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
     */
    void setVertexAttrs(int vertexAttrNum, int index,
			Point2f[] vertexAttrs,
			int start, int length) {

	int offset = this.stride*index + vertexAttrOffsets[vertexAttrNum];
        int i, j, k;

	geomLock.getLock();
	dirtyFlag |= VATTR_CHANGED;

        for (i = start, j = offset, k = 0; k < length; i++, j += this.stride, k++) {
	    this.vertexData[j] = vertexAttrs[i].x;
	    this.vertexData[j+1] = vertexAttrs[i].y;
        }
        
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);
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
     */
    void setVertexAttrs(int vertexAttrNum, int index,
			Point3f[] vertexAttrs,
			int start, int length) {

	int offset = this.stride*index + vertexAttrOffsets[vertexAttrNum];
        int i, j, k;

	geomLock.getLock();
	dirtyFlag |= VATTR_CHANGED;

        for (i = start, j = offset, k = 0; k < length; i++, j += this.stride, k++) {
	    this.vertexData[j] = vertexAttrs[i].x;
	    this.vertexData[j+1] = vertexAttrs[i].y;
	    this.vertexData[j+2] = vertexAttrs[i].z;
        }
        
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);
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
     */
    void setVertexAttrs(int vertexAttrNum, int index,
			Point4f[] vertexAttrs,
			int start, int length) {

	int offset = this.stride*index + vertexAttrOffsets[vertexAttrNum];
        int i, j, k;

	geomLock.getLock();
	dirtyFlag |= VATTR_CHANGED;

        for (i = start, j = offset, k = 0; k < length; i++, j += this.stride, k++) {
	    this.vertexData[j] = vertexAttrs[i].x;
	    this.vertexData[j+1] = vertexAttrs[i].y;
	    this.vertexData[j+2] = vertexAttrs[i].z;
	    this.vertexData[j+3] = vertexAttrs[i].w;
        }
        
	if (source == null || !source.isLive()) {
	    geomLock.unLock();
	    return;
	}

	geomLock.unLock();
	sendDataChangedMessage(false);
    }


    /**
     * Gets the coordinate associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param coordinate an array of 3 values that will receive the new coordinate
     */
    void getCoordinate(int index, float coordinate[]) {
	int offset = this.stride*index + coordinateOffset;

	coordinate[0]= this.vertexData[offset];
	coordinate[1]= this.vertexData[offset+1];
	coordinate[2]= this.vertexData[offset+2];
    }

    /**
     * Gets the coordinate associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param coordinate an array of 3 values that will receive the new coordinate
     */
    void getCoordinate(int index, double coordinate[]) {
	int offset = this.stride*index + coordinateOffset;

	coordinate[0]= (double)this.vertexData[offset];
	coordinate[1]= (double)this.vertexData[offset+1];
	coordinate[2]= (double)this.vertexData[offset+2];
    }

    /**
     * Gets the coordinate associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param coordinate a vector that will receive the new coordinate
     */
    void getCoordinate(int index, Point3f coordinate) {
	int offset = this.stride*index + coordinateOffset;

	coordinate.x = this.vertexData[offset];
	coordinate.y = this.vertexData[offset+1];
	coordinate.z = this.vertexData[offset+2];
    }

    /**
     * Gets the coordinate associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param coordinate a vector that will receive the new coordinate
     */
    void getCoordinate(int index, Point3d coordinate) {
	int offset = this.stride*index + coordinateOffset;

	coordinate.x = (double)this.vertexData[offset];
	coordinate.y = (double)this.vertexData[offset+1];
	coordinate.z = (double)this.vertexData[offset+2];
    }

    /**
     * Gets the coordinates associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param coordinates an array of 3*n values that will receive new coordinates
     */
    void getCoordinates(int index, float coordinates[]) {
	int offset = this.stride*index + coordinateOffset;
	int i, j, num = coordinates.length;

	for (i=0,j= offset;i < num;i +=3, j += this.stride)
	    {
		coordinates[i]  = this.vertexData[j];
		coordinates[i+1]= this.vertexData[j+1];
		coordinates[i+2]= this.vertexData[j+2];
	    }
    }

    
    /**
     * Gets the coordinates associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param coordinates an array of 3*n values that will receive new coordinates
     */
    void getCoordinates(int index, double coordinates[]) {
	int offset = this.stride*index + coordinateOffset;
	int i, j, num = coordinates.length;

	for (i=0,j= offset;i < num;i +=3, j += this.stride)
	    {
		coordinates[i]  = (double)this.vertexData[j];
		coordinates[i+1]= (double)this.vertexData[j+1];
		coordinates[i+2]= (double)this.vertexData[j+2];
	    }
    }

    /**
     * Gets the coordinates associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param coordinates an array of vectors that will receive new coordinates
     */
    void getCoordinates(int index, Point3f coordinates[]) {
	int offset = this.stride*index + coordinateOffset;
	int i, j, num = coordinates.length;

	for (i=0,j= offset;i < num;i++, j += this.stride)
	    {
		coordinates[i].x  = this.vertexData[j];
		coordinates[i].y  = this.vertexData[j+1];
		coordinates[i].z  = this.vertexData[j+2];
	    }
    }

    /**
     * Gets the coordinates associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param coordinates an array of vectors that will receive new coordinates
     */
    void getCoordinates(int index, Point3d coordinates[]) {
	int offset = this.stride*index + coordinateOffset;
	int i, j, num = coordinates.length;

	for (i=0,j= offset;i < num;i++, j += this.stride)
	    {
		coordinates[i].x  = (double)this.vertexData[j];
		coordinates[i].y  = (double)this.vertexData[j+1];
		coordinates[i].z  = (double)this.vertexData[j+2];
	    }
    }

    /**
     * Gets the color associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param color an array of 3 or 4 values that will receive the new color
     */
    void getColor(int index, float color[]) {
	int offset = this.stride*index + colorOffset;

	color[0]= this.vertexData[offset];
	color[1]= this.vertexData[offset+1];
	color[2]= this.vertexData[offset+2];
	if ((this.vertexFormat & GeometryArray.WITH_ALPHA) != 0)
	    color[3]= this.vertexData[offset+3]/lastAlpha[0];
    }

    /**
     * Gets the color associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param color an array of 3 or 4 values that will receive the new color
     */
    void getColor(int index, byte color[]) {
	int offset = this.stride*index + colorOffset;

	color[0]= (byte)(this.vertexData[offset] * FloatToByteScale);
	color[1]= (byte)(this.vertexData[offset+1] * FloatToByteScale);
	color[2]= (byte)(this.vertexData[offset+2] * FloatToByteScale);
	if ((this.vertexFormat & GeometryArray.WITH_ALPHA) != 0)
	    color[3]= (byte)((this.vertexData[offset+3]/lastAlpha[0]) * FloatToByteScale);
    }

    /**
     * Gets the color associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param color a vector that will receive the new color
     */
    void getColor(int index, Color3f color) {
	int offset = this.stride*index + colorOffset;

	color.x = this.vertexData[offset];
	color.y = this.vertexData[offset+1];
	color.z = this.vertexData[offset+2];
    }

    /**
     * Gets the color associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param color a vector that will receive the new color
     */
    void getColor(int index, Color4f color) {
	int offset = this.stride*index + colorOffset;

	color.x = this.vertexData[offset];
	color.y = this.vertexData[offset+1];
	color.z = this.vertexData[offset+2];
	color.w= this.vertexData[offset+3]/lastAlpha[0];
    }

    /**
     * Gets the color associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param color a vector that will receive the new color
     */
    void getColor(int index, Color3b color) {
	int offset = this.stride*index + colorOffset;

	color.x = (byte)(this.vertexData[offset] * FloatToByteScale);
	color.y = (byte)(this.vertexData[offset+1] * FloatToByteScale);
	color.z = (byte)(this.vertexData[offset+2] * FloatToByteScale);
    }

    /**
     * Gets the color associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param color a vector that will receive the new color
     */
    void getColor(int index, Color4b color) {
	int offset = this.stride*index + colorOffset;

	color.x = (byte)(this.vertexData[offset] * FloatToByteScale);
	color.y = (byte)(this.vertexData[offset+1] * FloatToByteScale);
	color.z = (byte)(this.vertexData[offset+2] * FloatToByteScale);
	color.w = (byte)((this.vertexData[offset+3]/lastAlpha[0]) * FloatToByteScale);
    }

    /**
     * Gets the colors associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param colors an array of 3*n or 4*n values that will receive n new colors
     */
    void getColors(int index, float colors[]) {
	int offset = this.stride*index + colorOffset;
	int i, j, num = colors.length;
	float val = 1.0f/lastAlpha[0];

	if ((this.vertexFormat & GeometryArray.WITH_ALPHA) != 0)
	    {
		for (i=0, j= offset;i < num; i+= 4, j+= this.stride)
		    {
			colors[i]  = this.vertexData[j];
			colors[i+1]= this.vertexData[j+1];
			colors[i+2]= this.vertexData[j+2];
			colors[i+3]= this.vertexData[j+3] * val;
		    }
	    }
	else
	    {
		for (i=0, j= offset;i < num; i+= 3, j+= this.stride)
		    {
			colors[i]  = this.vertexData[j];
			colors[i+1]= this.vertexData[j+1];
			colors[i+2]= this.vertexData[j+2];
		    }
	    }
    }

    /**
     * Gets the colors associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param colors an array of 3*n or 4*n values that will receive new colors
     */
    void getColors(int index, byte colors[]) {
	int offset = this.stride*index + colorOffset;
	int i, j, num = colors.length;
	float val = 1.0f/lastAlpha[0];
	

	if ((this.vertexFormat & GeometryArray.WITH_ALPHA) != 0)
	    {
		for (i=0, j= offset;i < num; i+= 4, j+= this.stride)
		    {
			colors[i]  = (byte)(this.vertexData[j] * FloatToByteScale);
			colors[i+1]= (byte)(this.vertexData[j+1] * FloatToByteScale);
			colors[i+2]= (byte)(this.vertexData[j+2] * FloatToByteScale);
			colors[i+3]= (byte)((this.vertexData[j+3] * val) * FloatToByteScale);
		    }
	    }
	else
	    {
		for (i=0, j= offset;i < num; i+= 3, j+= this.stride)
		    {
			colors[i]  = (byte)(this.vertexData[j] * FloatToByteScale);
			colors[i+1]= (byte)(this.vertexData[j+1] * FloatToByteScale);
			colors[i+2]= (byte)(this.vertexData[j+2] * FloatToByteScale);
		    }
	    }
    }

    /**
     * Gets the colors associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param colors an array of vectors that will receive new colors
     */
    void getColors(int index, Color3f colors[]) {
	int offset = this.stride*index + colorOffset;
	int i, j, num = colors.length;

	for (i=0, j= offset;i < num; i++, j+= this.stride)
	    {
		colors[i].x  = this.vertexData[j];
		colors[i].y  = this.vertexData[j+1];
		colors[i].z  = this.vertexData[j+2];
	    }
    }

    /**
     * Gets the colors associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param colors an array of vectors that will receive new colors
     */
    void getColors(int index, Color4f colors[]) {
	int offset = this.stride*index + colorOffset;
	int i, j, num = colors.length;
	float val = 1.0f/lastAlpha[0];

	for (i=0, j= offset;i < num; i++, j+= this.stride)
	    {
		colors[i].x  = this.vertexData[j];
		colors[i].y  = this.vertexData[j+1];
		colors[i].z  = this.vertexData[j+2];
		colors[i].w  = this.vertexData[j+3] * val;
	    }
    }

    /**
     * Gets the colors associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param colors an array of vectors that will receive new colors
     */
    void getColors(int index, Color3b colors[]) {
	int offset = this.stride*index + colorOffset;
	int i, j, num = colors.length;

	for (i=0, j= offset;i < num; i++, j+= this.stride)
	    {
		colors[i].x  = (byte)(this.vertexData[j] * FloatToByteScale);
		colors[i].y  = (byte)(this.vertexData[j+1] * FloatToByteScale);
		colors[i].z  = (byte)(this.vertexData[j+2] * FloatToByteScale);
	    }
    }

    /**
     * Gets the colors associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param colors an array of vectors that will receive new colors
     */
    void getColors(int index, Color4b colors[]) {
	int offset = this.stride*index + colorOffset;
	int i, j, num = colors.length;
	float val = 1.0f/lastAlpha[0];

	for (i=0, j= offset;i < num; i++, j+= this.stride)
	    {
		colors[i].x  = (byte)(this.vertexData[j] * FloatToByteScale);
		colors[i].y  = (byte)(this.vertexData[j+1] * FloatToByteScale);
		colors[i].z  = (byte)(this.vertexData[j+2] * FloatToByteScale);
		colors[i].w  = (byte)(this.vertexData[j+3] * val * FloatToByteScale);
	    }
    }

    /**
     * Gets the normal associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param normal array that will receive the new normal
     */
    void getNormal(int index, float normal[]) {
	int offset = this.stride*index + normalOffset;

	normal[0]= this.vertexData[offset];
	normal[1]= this.vertexData[offset+1];
	normal[2]= this.vertexData[offset+2];
    }

    /**
     * Gets the normal associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param normal the vector that will receive the new normal
     */
    void getNormal(int index, Vector3f normal) {
	int offset = this.stride*index + normalOffset;

	normal.x= this.vertexData[offset];
	normal.y= this.vertexData[offset+1];
	normal.z= this.vertexData[offset+2];
    }

    /**
     * Gets the normals associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param normals array that will receive the new normals
     */
    void getNormals(int index, float normals[]) {
	int offset = this.stride*index + normalOffset;
	int i, j, num = normals.length;
	
	for (i=0, j= offset;i < num;i+=3, j+= this.stride)
	    {
		normals[i]  = this.vertexData[j];
		normals[i+1]= this.vertexData[j+1];
		normals[i+2]= this.vertexData[j+2];
	    }
    }

    /**
     * Gets the normals associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param normals the vector that will receive the new normals
     */
    void getNormals(int index, Vector3f normals[]) {
	int offset = this.stride*index + normalOffset;
	int i, j, num = normals.length;
	
	for (i=0, j= offset;i < num;i++, j+= this.stride)
	    {
		normals[i].x= this.vertexData[j];
		normals[i].y= this.vertexData[j+1];
		normals[i].z= this.vertexData[j+2];
	    }
    }

    /**
     * Gets the texture co-ordinate associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param texCoord array that will receive the new texture co-ordinate
     */
    void getTextureCoordinate(int texCoordSet, int index, float texCoord[]) {
	int offset = this.stride*index + textureOffset +
			texCoordSet * texCoordStride;

	texCoord[0]= this.vertexData[offset];
	texCoord[1]= this.vertexData[offset+1];
	if ((this.vertexFormat & GeometryArray.TEXTURE_COORDINATE_3) != 0) {
	    texCoord[2]= this.vertexData[offset+2];

	} else if ((this.vertexFormat & GeometryArray.TEXTURE_COORDINATE_4) 
				!= 0) {
	    texCoord[2]= this.vertexData[offset+2];
	    texCoord[3]= this.vertexData[offset+3];
	}
    }

    /**
     * Gets the texture co-ordinate associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param texCoord the vector that will receive the new texture co-ordinates
     */
    void getTextureCoordinate(int texCoordSet, int index, TexCoord2f texCoord) {
	int offset = this.stride*index + textureOffset +
			texCoordSet * texCoordStride;

	texCoord.x= this.vertexData[offset];
	texCoord.y= this.vertexData[offset+1];
    }

    /**
     * Gets the texture co-ordinate associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param texCoord the vector that will receive the new texture co-ordinates
     */
    void getTextureCoordinate(int texCoordSet, int index, TexCoord3f texCoord) {
	int offset = this.stride*index + textureOffset +
			texCoordSet * texCoordStride;

	texCoord.x= this.vertexData[offset];
	texCoord.y= this.vertexData[offset+1];
	texCoord.z= this.vertexData[offset+2];
    }

    /**
     * Gets the texture co-ordinate associated with the vertex at
     * the specified index.
     * @param index the vertex index
     * @param texCoord the vector that will receive the new texture co-ordinates
     */
    void getTextureCoordinate(int texCoordSet, int index, TexCoord4f texCoord) {
	int offset = this.stride*index + textureOffset +
			texCoordSet * texCoordStride;

	texCoord.x= this.vertexData[offset];
	texCoord.y= this.vertexData[offset+1];
	texCoord.z= this.vertexData[offset+2];
	texCoord.w= this.vertexData[offset+3];
    }

    /**
     * Gets the texture co-ordinates associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param texCoords array that will receive the new texture co-ordinates
     */
    void getTextureCoordinates(int texCoordSet, int index, float texCoords[]) {
	int offset = this.stride*index + textureOffset +
			texCoordSet * texCoordStride;
	int i, j, num = texCoords.length;

	if ((this.vertexFormat & GeometryArray.TEXTURE_COORDINATE_4) != 0) {
		for (i=0, j= offset;i < num;i+=4, j+= this.stride)
		    {
			texCoords[i]= this.vertexData[j];
			texCoords[i+1]= this.vertexData[j+1];
			texCoords[i+2]= this.vertexData[j+2];
			texCoords[i+3]= this.vertexData[j+3];
		    }
	} else if ((this.vertexFormat & GeometryArray.TEXTURE_COORDINATE_3) 
				!= 0) {
		for (i=0, j= offset;i < num;i+=3, j+= this.stride)
		    {
			texCoords[i]= this.vertexData[j];
			texCoords[i+1]= this.vertexData[j+1];
			texCoords[i+2]= this.vertexData[j+2];
		    }
	} else {
		for (i=0, j= offset;i < num;i+=2, j+= this.stride)
		    {
			texCoords[i]= this.vertexData[j];
			texCoords[i+1]= this.vertexData[j+1];
		    }
	}
    }

    /**
     * Gets the texture co-ordinates associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param texCoords the vector that will receive the new texture co-ordinates
     */
    void getTextureCoordinates(int texCoordSet, int index, 
					TexCoord2f texCoords[]) {
	int offset = this.stride*index + textureOffset +
			texCoordSet * texCoordStride;
	int i, j, num = texCoords.length;

	for (i=0, j= offset;i < num;i++, j+= this.stride)
	    {
		texCoords[i].x= this.vertexData[j];
		texCoords[i].y= this.vertexData[j+1];
	    }
    }

    /**
     * Gets the texture co-ordinates associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param texCoords the vector that will receive the new texture co-ordinates
     */
    void getTextureCoordinates(int texCoordSet, int index, TexCoord3f texCoords[]) {
	int offset = this.stride*index + textureOffset +
			texCoordSet * texCoordStride;
	int i, j, num = texCoords.length;

	for (i=0, j= offset;i < num;i++, j+= this.stride)
	    {
		texCoords[i].x= this.vertexData[j];
		texCoords[i].y= this.vertexData[j+1];
		texCoords[i].z= this.vertexData[j+2];
	    }
    }

    /**
     * Gets the texture co-ordinates associated with the vertices starting at
     * the specified index.
     * @param index the vertex index
     * @param texCoords the vector that will receive the new texture co-ordinates
     */
    void getTextureCoordinates(int texCoordSet, int index, TexCoord4f texCoords[]) {
	int offset = this.stride*index + textureOffset +
			texCoordSet * texCoordStride;
	int i, j, num = texCoords.length;

	for (i=0, j= offset;i < num;i++, j+= this.stride)
	    {
		texCoords[i].x= this.vertexData[j];
		texCoords[i].y= this.vertexData[j+1];
		texCoords[i].z= this.vertexData[j+2];
		texCoords[i].w= this.vertexData[j+3];
	    }
    }

    void getTextureCoordinates(int texCoordSet, int index,
                                        Point2f texCoords[]) {
        int offset = this.stride*index + textureOffset +
                        texCoordSet * texCoordStride;
        int i, j, num = texCoords.length;

        for (i=0, j= offset;i < num;i++, j+= this.stride)
            {
                texCoords[i].x= this.vertexData[j];
                texCoords[i].y= this.vertexData[j+1];
            }
    }

    void getTextureCoordinates(int texCoordSet, int index, Point3f texCoords[]) {
        int offset = this.stride*index + textureOffset +
                        texCoordSet * texCoordStride;
        int i, j, num = texCoords.length;

        for (i=0, j= offset;i < num;i++, j+= this.stride)
            {
                texCoords[i].x= this.vertexData[j];
                texCoords[i].y= this.vertexData[j+1];
                texCoords[i].z= this.vertexData[j+2];
            }
    }


    /**
     * Gets the vertex attribute associated with the vertex at
     * the specified index in the specified vertex attribute number
     * for this object.
     */
    public void getVertexAttr(int vertexAttrNum, int index,
			      float[] vertexAttr) {

	int offset = this.stride*index + vertexAttrOffsets[vertexAttrNum];
        int size = vertexAttrSizes[vertexAttrNum];

	for (int i = 0; i < size; i++) {
	    vertexAttr[i] = this.vertexData[offset+i];
	    
        }

    }

    /**
     * Gets the vertex attribute associated with the vertex at
     * the specified index in the specified vertex attribute number
     * for this object.
     */
    public void getVertexAttr(int vertexAttrNum, int index,
			      Point2f vertexAttr) {

	int offset = this.stride*index + vertexAttrOffsets[vertexAttrNum];

	vertexAttr.x = this.vertexData[offset];
	vertexAttr.y = this.vertexData[offset+1];

    }

    /**
     * Gets the vertex attribute associated with the vertex at
     * the specified index in the specified vertex attribute number
     * for this object.
     */
    public void getVertexAttr(int vertexAttrNum, int index,
			      Point3f vertexAttr) {

	int offset = this.stride*index + vertexAttrOffsets[vertexAttrNum];

	vertexAttr.x = this.vertexData[offset];
	vertexAttr.y = this.vertexData[offset+1];
	vertexAttr.z = this.vertexData[offset+2];

    }

    /**
     * Gets the vertex attribute associated with the vertex at
     * the specified index in the specified vertex attribute number
     * for this object.
     */
    public void getVertexAttr(int vertexAttrNum, int index,
			      Point4f vertexAttr) {

	int offset = this.stride*index + vertexAttrOffsets[vertexAttrNum];

	vertexAttr.x = this.vertexData[offset];
	vertexAttr.y = this.vertexData[offset+1];
	vertexAttr.z = this.vertexData[offset+2];
	vertexAttr.w = this.vertexData[offset+3];

    }

    /**
     * Gets the vertex attributes associated with the vertices starting at
     * the specified index in the specified vertex attribute number
     * for this object.
     */
    public void getVertexAttrs(int vertexAttrNum, int index,
			       float[] vertexAttrs) {

	int offset = this.stride*index + vertexAttrOffsets[vertexAttrNum];
        int size = vertexAttrSizes[vertexAttrNum];
        int i, j, k;

        for (i = 0, j = offset; 
	     ((i < vertexAttrs.length) && (j < this.vertexData.length)) ; 
	     i += size, j += this.stride) {
            for (k = 0; k < size; k++) {
                vertexAttrs[i+k] = this.vertexData[j+k];
            }
        }

    }

    /**
     * Gets the vertex attributes associated with the vertices starting at
     * the specified index in the specified vertex attribute number
     * for this object.
     */
    public void getVertexAttrs(int vertexAttrNum, int index,
			       Point2f[] vertexAttrs) {

	int offset = this.stride*index + vertexAttrOffsets[vertexAttrNum];
        int i, j;

        for (i = 0, j = offset; 
	     ((i < vertexAttrs.length) && (j < this.vertexData.length)) ; 
	     i++, j += this.stride) {
	    vertexAttrs[i].x = this.vertexData[j];
	    vertexAttrs[i].y = this.vertexData[j+1];
        }

    }

    /**
     * Gets the vertex attributes associated with the vertices starting at
     * the specified index in the specified vertex attribute number
     * for this object.
     */
    public void getVertexAttrs(int vertexAttrNum, int index,
			       Point3f[] vertexAttrs) {

	int offset = this.stride*index + vertexAttrOffsets[vertexAttrNum];
        int i, j;

        for (i = 0, j = offset; 
	     ((i < vertexAttrs.length) && (j < this.vertexData.length)) ; 
	     i++, j += this.stride) {
	    vertexAttrs[i].x = this.vertexData[j];
	    vertexAttrs[i].y = this.vertexData[j+1];
	    vertexAttrs[i].z = this.vertexData[j+2];
        }

    }

    /**
     * Gets the vertex attributes associated with the vertices starting at
     * the specified index in the specified vertex attribute number
     * for this object.
     */
    public void getVertexAttrs(int vertexAttrNum, int index,
			       Point4f[] vertexAttrs) {

	int offset = this.stride*index + vertexAttrOffsets[vertexAttrNum];
        int i, j;

        for (i = 0, j = offset; 
	     ((i < vertexAttrs.length) && (j < this.vertexData.length)) ; 
	     i++, j += this.stride) {
	    vertexAttrs[i].x = this.vertexData[j];
	    vertexAttrs[i].y = this.vertexData[j+1];
	    vertexAttrs[i].z = this.vertexData[j+2];
	    vertexAttrs[i].w = this.vertexData[j+3];
        }

    }


    /**
     * Updates geometry array data.
     */
    void updateData(GeometryUpdater updater) {
	boolean nullGeo = false;

 	// Add yourself to obtain the geometry lock
 	// and Thread.currentThread().sleep until you get the lock
 	geomLock.getLock();

	inUpdater = true;
	updater.updateData((Geometry)source);
	inUpdater = false;
	if ((vertexFormat & GeometryArray.BY_REFERENCE) != 0) {
	    if((vertexFormat & GeometryArray.USE_NIO_BUFFER) != 0) {
		// XXXX: handle the nio buffer
		if (!(this instanceof IndexedGeometryArrayRetained) ||
		    (vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) != 0) {
		    if (((vertexFormat & GeometryArray.INTERLEAVED) != 0)) {
			setupMirrorInterleavedColorPointer(false);
			nullGeo = (interleavedFloatBufferImpl == null);
		    }
		    else {
			setupMirrorColorPointer((vertexType & COLOR_DEFINED), false);
			nullGeo = ((vertexType & GeometryArrayRetained.VERTEX_DEFINED) == 0);
		    }
		}
	    }
	    else {
		if (!(this instanceof IndexedGeometryArrayRetained) ||
		    (vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) != 0) {
		    if (((vertexFormat & GeometryArray.INTERLEAVED) != 0)) {
			setupMirrorInterleavedColorPointer(false);
			nullGeo = (interLeavedVertexData == null);
		    }
		    else {
			setupMirrorVertexPointer(vertexType & VERTEX_DEFINED);
			setupMirrorColorPointer((vertexType & COLOR_DEFINED), false);
			setupMirrorNormalPointer(vertexType & NORMAL_DEFINED);
			setupMirrorTexCoordPointer(texCoordType);
                        setupMirrorVertexAttrPointer(vertexAttrType);
			nullGeo = ((vertexType & GeometryArrayRetained.VERTEX_DEFINED) == 0);
		    }
		}
	    }
	}
	dirtyFlag |= VERTEX_CHANGED; 
	colorChanged = 0xffff;
	geomLock.unLock();

	if (source != null && source.isLive()) {
	    processCoordsChanged(nullGeo);
	    sendDataChangedMessage(true);
	}
    }

    boolean intersectBoundingBox( Point3d coordinates[], 
				  BoundingBox box,
				  double dist[],
				  Point3d iPnt) {
	int i, j;
	int out[] = new int[6];      
       
	//Do trivial vertex test.
	for(i=0; i<6; i++)
	    out[i] = 0;
	for(i=0; i<coordinates.length; i++) {
	    if((coordinates[i].x >= box.lower.x) && (coordinates[i].x <= box.upper.x) &&
	       (coordinates[i].y >= box.lower.y) && (coordinates[i].y <= box.upper.y) &&
	       (coordinates[i].z >= box.lower.z) && (coordinates[i].z <= box.upper.z))
		// We're done! It's inside the boundingbox.
		return true;	  
	    else {
		if(coordinates[i].x < box.lower.x)
		    out[0]++; // left
		if(coordinates[i].y < box.lower.y)
		    out[1]++; // bottom
		if(coordinates[i].z < box.lower.z)
		    out[2]++; // back
		if(coordinates[i].x > box.upper.x)
		    out[3]++; // right
		if(coordinates[i].y > box.upper.y)
		    out[4]++; // top
		if(coordinates[i].z > box.upper.z)
		    out[5]++; // front	  
	    }
      
	}
      
	if((out[0] == coordinates.length) || (out[1] == coordinates.length) ||
	   (out[2] == coordinates.length) || (out[3] == coordinates.length) ||
	   (out[4] == coordinates.length) || (out[5] == coordinates.length))	 
	    // we're done. primitive is outside of boundingbox.
	    return false;
      
	// Setup bounding planes.
	Point3d pCoor[] = new Point3d[4];
	for(i=0; i<4; i++)
	    pCoor[i] = getPoint3d();
      
	// left plane.
	pCoor[0].set(box.lower.x, box.lower.y, box.lower.z);
	pCoor[1].set(box.lower.x, box.lower.y, box.upper.z);
	pCoor[2].set(box.lower.x, box.upper.y, box.upper.z);
	pCoor[3].set(box.lower.x, box.upper.y, box.lower.z);
	
 
	if (intersectPolygon(pCoor, coordinates)) {
	    if (dist != null) {
		computeMinDistance(pCoor, box.getCenter(), 
				   null,
				   dist, iPnt);
	    }
	    // free points
	    for (i=0; i<4; i++) freePoint3d(pCoor[i]);
	    return true;
	}
	
	// right plane.
	pCoor[0].set(box.upper.x, box.lower.y, box.lower.z);
	pCoor[1].set(box.upper.x, box.upper.y, box.lower.z);
	pCoor[2].set(box.upper.x, box.upper.y, box.upper.z);
	pCoor[3].set(box.upper.x, box.lower.y, box.upper.z);
	if (intersectPolygon(pCoor, coordinates)) {
	    if (dist != null) {
		computeMinDistance(pCoor, box.getCenter(), 
				   null,
				   dist, iPnt);
	    }
	    for (i=0; i<4; i++) freePoint3d(pCoor[i]);
	    return true;
	}

	// bottom plane.
	pCoor[0].set(box.upper.x, box.lower.y, box.upper.z);
	pCoor[1].set(box.lower.x, box.lower.y, box.upper.z);
	pCoor[2].set(box.lower.x, box.lower.y, box.lower.z);
	pCoor[3].set(box.upper.x, box.lower.y, box.lower.z);
	if (intersectPolygon(pCoor, coordinates)) {
	    if (dist != null) {
		computeMinDistance(pCoor, box.getCenter(), 
				   null,
				   dist, iPnt);
	    }
	    for (i=0; i<4; i++) freePoint3d(pCoor[i]);
	    return true;
	}
	// top plane.
	pCoor[0].set(box.upper.x, box.upper.y, box.upper.z);
	pCoor[1].set(box.upper.x, box.upper.y, box.lower.z);
	pCoor[2].set(box.lower.x, box.upper.y, box.lower.z);
	pCoor[3].set(box.lower.x, box.upper.y, box.upper.z);
	if (intersectPolygon(pCoor, coordinates)) {
	    if (dist != null) {
		computeMinDistance(pCoor, box.getCenter(),
				   null,
				   dist, iPnt);
	    }
	    for (i=0; i<4; i++) freePoint3d(pCoor[i]);
	    return true;
	}

	// front plane.
	pCoor[0].set(box.upper.x, box.upper.y, box.upper.z);
	pCoor[1].set(box.lower.x, box.upper.y, box.upper.z);
	pCoor[2].set(box.lower.x, box.lower.y, box.upper.z);
	pCoor[3].set(box.upper.x, box.lower.y, box.upper.z);
	if (intersectPolygon(pCoor, coordinates)) {
	    if (dist != null) {
		computeMinDistance(pCoor, box.getCenter(), 
				   null,
				   dist, iPnt);
	    }
	    for (i=0; i<4; i++) freePoint3d(pCoor[i]);
	    return true;
	}
      
	// back plane.
	pCoor[0].set(box.upper.x, box.upper.y, box.lower.z);
	pCoor[1].set(box.upper.x, box.lower.y, box.lower.z);
	pCoor[2].set(box.lower.x, box.lower.y, box.lower.z);
	pCoor[3].set(box.lower.x, box.upper.y, box.lower.z);
	if (intersectPolygon(pCoor, coordinates)) {
	    if (dist != null) {
		computeMinDistance(pCoor, box.getCenter(), 
				   null,
				   dist, iPnt);
	    }
	    for (i=0; i<4; i++) freePoint3d(pCoor[i]);
	    return true;
	}

	for (i=0; i<4; i++) freePoint3d(pCoor[i]);
	return false;
    }


    boolean intersectBoundingSphere(Point3d coordinates[], 
				    BoundingSphere sphere,
				    double dist[],
				    Point3d iPnt) 
    {
	int i, j;
	Vector3d tempV3D = getVector3d();
	boolean esFlag;

	//Do trivial vertex test.

	for (i=0; i<coordinates.length; i++) {
	    tempV3D.x = coordinates[i].x - sphere.center.x;
	    tempV3D.y = coordinates[i].y - sphere.center.y;
	    tempV3D.z = coordinates[i].z - sphere.center.z;
	
	    if (tempV3D.length() <= sphere.radius) {
		// We're done! It's inside the boundingSphere.
		if (dist != null) {
		    computeMinDistance(coordinates, 
				       sphere.getCenter(), 
				       null, dist, iPnt);
		}

		freeVector3d(tempV3D);
		return true;
	    }
	}

	for (i=0; i<coordinates.length; i++) {
	    if (i < (coordinates.length-1))
		esFlag = edgeIntersectSphere(sphere, coordinates[i], 
					     coordinates[i+1]);
	    else
		esFlag = edgeIntersectSphere(sphere, coordinates[i], 
					     coordinates[0]);
	    if (esFlag == true) {
		if (dist != null) {
		    computeMinDistance(coordinates,
				       sphere.getCenter(), 
				       null,
				       dist, iPnt);
		}

		freeVector3d(tempV3D);
		return true;
	    }
	}
      

	if (coordinates.length < 3) {

	    freeVector3d(tempV3D);
	    return false; // We're done with line.
	}

	    // Find rho.
	    // Compute plane normal.
	    Vector3d vec0 = getVector3d(); // Edge vector from point 0 to point 1;
	    Vector3d vec1 = getVector3d(); // Edge vector from point 0 to point 2 or 3;
	    Vector3d pNrm = getVector3d();
	    Vector3d pa = getVector3d();
	    Point3d q = getPoint3d();
	    double nLenSq, pqLen, pNrmDotPa, tq;

	    // compute plane normal for coordinates.
	    for(i=0; i<coordinates.length-1;) {
		vec0.x = coordinates[i+1].x - coordinates[i].x;
		vec0.y = coordinates[i+1].y - coordinates[i].y;
		vec0.z = coordinates[i+1].z - coordinates[i++].z;
		if(vec0.length() > 0.0)
		    break;
	    }
        
	    for(j=i; j<coordinates.length-1; j++) {
		vec1.x = coordinates[j+1].x - coordinates[j].x;
		vec1.y = coordinates[j+1].y - coordinates[j].y;
		vec1.z = coordinates[j+1].z - coordinates[j].z;
		if(vec1.length() > 0.0)
		    break;
	    }
      
	    if(j == (coordinates.length-1)) {
		// System.out.println("(1) Degenerate polygon.");
		freeVector3d(tempV3D);
		freeVector3d(vec0);
		freeVector3d(vec1);
		freeVector3d(pNrm);
		freeVector3d(pa);
		freePoint3d(q);
		return false;  // Degenerate polygon.
	    }

	    /*
	      for(i=0; i<coordinates.length; i++) 
	      System.out.println("coordinates P" + i + " " + coordinates[i]);
	      for(i=0; i<coord2.length; i++) 
	      System.out.println("coord2 P" + i + " " + coord2[i]);
	      */
      
	    pNrm.cross(vec0,vec1);
      
	    nLenSq = pNrm.lengthSquared(); 
	    if( nLenSq == 0.0) {
		// System.out.println("(2) Degenerate polygon.");
		freeVector3d(tempV3D);
		freeVector3d(vec0);
		freeVector3d(vec1);
		freeVector3d(pNrm);
		freeVector3d(pa);
		freePoint3d(q);
		return false;  // Degenerate polygon.
	    }

	    pa.x = coordinates[0].x - sphere.center.x;
	    pa.y = coordinates[0].y - sphere.center.y;
	    pa.z = coordinates[0].z - sphere.center.z;

	    pNrmDotPa = pNrm.dot(pa);
      
	    pqLen = Math.sqrt(pNrmDotPa * pNrmDotPa/ nLenSq);
      
	    if(pqLen > sphere.radius) {
		freeVector3d(tempV3D);
		freeVector3d(vec0);
		freeVector3d(vec1);
		freeVector3d(pNrm);
		freeVector3d(pa);
		freePoint3d(q);
		return false;
	    }

	    tq = pNrmDotPa / nLenSq;

	    q.x = sphere.center.x + tq * pNrm.x;
	    q.y = sphere.center.y + tq * pNrm.y;
	    q.z = sphere.center.z + tq * pNrm.z;

	    // PolyPnt2D Test.
	    if (pointIntersectPolygon2D( pNrm, coordinates, q)) {
		if (dist != null) {
		    computeMinDistance(coordinates,
				       sphere.getCenter(), 
				       pNrm,
				       dist, iPnt);
		}		
		freeVector3d(tempV3D);
		freeVector3d(vec0);
		freeVector3d(vec1);
		freeVector3d(pNrm);
		freeVector3d(pa);
		freePoint3d(q);
		return true;
	    }
	    freeVector3d(tempV3D);
	    freeVector3d(vec0);
	    freeVector3d(vec1);
	    freeVector3d(pNrm);
	    freeVector3d(pa);
	    freePoint3d(q);
	    return false;

    }


    boolean intersectBoundingPolytope(Point3d coordinates[], 
				      BoundingPolytope polytope,
				      double dist[],
				      Point3d iPnt)
    {
	boolean debug = false;    
    
	Point4d tP4d = new Point4d();

	// this is a multiplier to the halfplane distance coefficients
	double distanceSign = -1.0;
      
	    if(coordinates.length == 2) {
		// we'll handle line separately.
		if (polytope.intersect( coordinates[0],
					coordinates[1], tP4d)) {
		    if (dist != null) {
			iPnt.x = tP4d.x;
			iPnt.y = tP4d.y;
			iPnt.z = tP4d.z;
			Point3d pc = polytope.getCenter();
			double x = iPnt.x - pc.x; 
			double y = iPnt.y - pc.y; 
			double z = iPnt.z - pc.z; 
			dist[0] = Math.sqrt(x*x + y*y + z*z);
		    }
		    return true;
		} 
		return false;
	    }

	    // It is a triangle or a quad.
      
	    // first test to see if any of the coordinates are all inside of the
	    // intersection polytope's half planes
	    // essentially do a matrix multiply of the constraintMatrix K*3 with
	    // the input coordinates 3*1 = K*1 vector

	    if (debug) { 
		System.out.println("The value of the input vertices are: ");
		for(int i=0; i < coordinates.length; i++) {
		    System.out.println("The " +i+ " th vertex is: " + coordinates[i]);
		}
      
		System.out.println("The value of the input bounding Polytope's planes =");
		for(int i=0; i < polytope.planes.length; i++) {
		    System.out.println("The " +i+ " th plane is: " + polytope.planes[i]);
		}
      
	    }
    
	    // the direction for the intersection cost function
	    double centers[] = new double[4];
	    centers[0] = 0.8; centers[1] = 0.9; centers[2] = 1.1; centers[3] = 1.2;
      
	    boolean intersection = true;
	    boolean PreTest = false;
    
	    if(PreTest) {
		// for each coordinate, test it with each half plane
		for( int i=0; i < coordinates.length; i++) {
		    for (int j=0; j < polytope.planes.length; j++) {
			if ( ( polytope.planes[j].x * coordinates[i].x +
			       polytope.planes[j].y * coordinates[i].y +
			       polytope.planes[j].z*coordinates[i].z) <= 
			     (distanceSign)*polytope.planes[j].w){
			    // the point satisfies this particular hyperplane
			    intersection = true;
			} else {
			    // the point fails this hyper plane try with a new hyper plane
			    intersection = false;
			    break;
			}
		    }
		    if(intersection) {
			// a point was found to be completely inside the bounding hull
			if (dist != null) {
			    computeMinDistance(coordinates,
					       polytope.getCenter(), 
					       null,
					       dist, iPnt);
			}			
			return true;
		    }
		}
	    }  // end of pretest
    
	    // at this point all points are outside of the bounding hull
	    // build the problem tableau for the linear program
    
	    int numberCols = polytope.planes.length + 2 + coordinates.length + 1;
	    int numberRows = 1 + coordinates.length;
    
	    double problemTableau[][] = new double[numberRows][numberCols];
    
	    // compute -Mtrans = -A*P
    
	    for( int i = 0; i < polytope.planes.length; i++) {
		for( int j=0; j < coordinates.length;  j++) {
		    problemTableau[j][i] = (-1.0)* (polytope.planes[i].x*coordinates[j].x+
						    polytope.planes[i].y*coordinates[j].y+
						    polytope.planes[i].z*coordinates[j].z);
		}
	    }
    
	    // add the other rows
	    for(int i = 0; i < coordinates.length; i++) {
		problemTableau[i][polytope.planes.length] = -1.0;
		problemTableau[i][polytope.planes.length + 1] =  1.0;
      
		for(int j=0; j < coordinates.length; j++) {
		    if ( i==j ) {
			problemTableau[i][j + polytope.planes.length + 2] = 1.0;
		    } else {
			problemTableau[i][j + polytope.planes.length + 2] = 0.0;
		    }
	
		    // place the last column elements the Ci's
		    problemTableau[i][numberCols - 1] = centers[i];
		}
	    }
    
	    // place the final rows value
	    for(int j = 0; j < polytope.planes.length; j++) {
		problemTableau[numberRows - 1][j] = 
		    (distanceSign)*polytope.planes[j].w;
	    }
	    problemTableau[numberRows - 1][polytope.planes.length] =  1.0;
	    problemTableau[numberRows - 1][polytope.planes.length+1] = -1.0;
	    for(int j = 0; j < coordinates.length; j++) {
		problemTableau[numberRows - 1][polytope.planes.length+2+j] = 0.0;
	    }
    
	    if(debug) {
		System.out.println("The value of the problem tableau is: " );
		for(int i=0; i < problemTableau.length; i++) {
		    for(int j=0; j < problemTableau[0].length; j++) {
			System.out.print(problemTableau[i][j] + "  ");
		    }
		    System.out.println();
		}
	    }
    
	    double distance = generalStandardSimplexSolver(problemTableau, 
							   Float.NEGATIVE_INFINITY);
	    if(debug) {
		System.out.println("The value returned by the general standard simplex = " +
				   distance);
	    }
	    if (distance == Float.POSITIVE_INFINITY) {
		return false;
	    } 
	    if (dist != null) {
		computeMinDistance(coordinates,
				   polytope.getCenter(), 
				   null,
				   dist, iPnt);
	    }			
	    return true;

	}


    // optimized version using arrays of doubles, but using the standard simplex
    // method to solve the LP tableau.  This version has not been optimized to
    // work with a particular size input tableau and is much slower than some
    // of the other variants...supposedly
    double generalStandardSimplexSolver(double problemTableau[][], 
					double stopingValue) {
	boolean debug = false;
	int numRow = problemTableau.length;
	int numCol = problemTableau[0].length;
	boolean optimal = false;
	int i, pivotRowIndex, pivotColIndex;
	double maxElement, element, endElement, ratio, prevRatio;
	int count = 0;
	double multiplier;
    
	if(debug) {
	    System.out.println("The number of rows is : " + numRow);
	    System.out.println("The number of columns is : " + numCol);
	}
    
	// until the optimal solution is found continue to do
	// iterations of the simplex method
	while(!optimal) {

	    if(debug) {
		System.out.println("input problem tableau is:");
		for(int k=0; k < numRow; k++) {
		    for(int j=0; j < numCol; j++) {
			System.out.println("kth, jth value is:" +k+" "+j+" : " +
					   problemTableau[k][j]);
		    }
		}
	    }
      
	    // test to see if the current solution is optimal
	    // check all bottom row elements except the right most one and
	    // if all positive or zero its optimal
	    for(i = 0, maxElement = 0, pivotColIndex = -1; i < numCol - 1; i++) {
		// a bottom row element
		element = problemTableau[numRow - 1][i];
		if( element < maxElement) {
		    maxElement = element;
		    pivotColIndex = i;
		}
	    }
      
	    // if there is no negative non-zero element then we
	    // have found an optimal solution (the last row of the tableau)
	    if(pivotColIndex == -1) {
		// found an optimal solution
		//System.out.println("Found an optimal solution");
		optimal = true;
	    }
      
	    //System.out.println("The value of maxElement is:" + maxElement);
      
	    if(!optimal) {
		// Case when the solution is not optimal but not known to be
		// either unbounded or infeasable
	
		// from the above we have found the maximum negative element in
		// bottom row, we have also found the column for this value
		// the pivotColIndex represents this
	
		// initialize the values for the algorithm, -1 for pivotRowIndex
		// indicates no solution
	
		prevRatio = Float.POSITIVE_INFINITY;
		ratio = 0.0;
		pivotRowIndex = -1;
	
		// note if all of the elements in the pivot column are zero or
		// negative the problem is unbounded.
		for(i = 0; i < numRow - 1; i++) {
		    element = problemTableau[i][pivotColIndex]; // r value
		    endElement = problemTableau[i][numCol-1]; // s value

		    // pivot according to the rule that we want to choose the row
		    // with smallest s/r ratio see third case
		    // currently we ignore valuse of r==0 (case 1) and cases where the
		    // ratio is negative, i.e. either r or s are negative (case 2)
		    if(element == 0) {
			if(debug) {
			    System.out.println("Division by zero has occurred");
			    System.out.println("Within the linear program solver");
			    System.out.println("Ignoring the zero as a potential pivot");
			}
		    } else if ( (element < 0.0) || (endElement < 0.0) ){
			if(debug) {
			    System.out.println("Ignoring cases where element is negative");
			    System.out.println("The value of element is: " + element);
			    System.out.println("The value of end Element is: " + endElement);
			}
		    } else {
			ratio = endElement/element;  // should be s/r
			if(debug) {
			    System.out.println("The value of element is: " + element);
			    System.out.println("The value of endElement is: " + endElement);
			    System.out.println("The value of ratio is: " + ratio);
			    System.out.println("The value of prevRatio is: " + prevRatio);
			    System.out.println("Value of ratio <= prevRatio is :" + 
					       (ratio <= prevRatio));
			}
			if(ratio <= prevRatio) {
			    if(debug) {
				System.out.println("updating prevRatio with ratio");
			    }
			    prevRatio = ratio;
			    pivotRowIndex = i;
			}
		    }
		}
	
		// if the pivotRowIndex is still -1 then we know the pivotColumn
		// has no viable pivot points and the solution is unbounded or
		// infeasable (all pivot elements were either zero or negative or
		// the right most value was negative (the later shouldn't happen?)
		if(pivotRowIndex == -1) {
		    if(debug) {
			System.out.println("UNABLE TO FIND SOLUTION");
			System.out.println("The system is infeasable or unbounded");
		    }
		    return(Float.POSITIVE_INFINITY);
		}
	
		// we now have the pivot row and col all that remains is
		// to divide through by this value and subtract the appropriate
		// multiple of the pivot row from all other rows to obtain
		// a tableau which has a column of all zeros and one 1 in the
		// intersection of pivot row and col
	
		// divide through by the pivot value
		double pivotValue = problemTableau[pivotRowIndex][pivotColIndex];
	
		if(debug) {
		    System.out.println("The value of row index is: " + pivotRowIndex);
		    System.out.println("The value of col index is: " + pivotColIndex);
		    System.out.println("The value of pivotValue is: " + pivotValue);
		}
		// divide through by s on the pivot row to obtain a 1 in pivot col
		for(i = 0; i < numCol; i++) {
		    problemTableau[pivotRowIndex][i] =
			problemTableau[pivotRowIndex][i] / pivotValue;
		}
	
		// subtract appropriate multiple of pivot row from all other rows
		// to zero out all rows except the final row and the pivot row
		for(i = 0; i < numRow; i++) {
		    if(i != pivotRowIndex) {
			multiplier = problemTableau[i][pivotColIndex];
			for(int j=0; j < numCol; j++) {
			    problemTableau[i][j] = problemTableau[i][j] -
				multiplier * problemTableau[pivotRowIndex][j];
			}
		    }
		}
	    }
	    // case when the element is optimal
	}
	return(problemTableau[numRow - 1][numCol - 1]);
    }



    boolean edgeIntersectSphere(BoundingSphere sphere, Point3d start, 
				Point3d end)
	{ 
	    double abLenSq, acLenSq, apLenSq, abDotAp, radiusSq;
	    Vector3d ab = getVector3d();
	    Vector3d ap = getVector3d();
	    
	    ab.x = end.x - start.x;
	    ab.y = end.y - start.y;
	    ab.z = end.z - start.z;
      
	    ap.x = sphere.center.x - start.x;
	    ap.y = sphere.center.y - start.y;
	    ap.z = sphere.center.z - start.z;
      
	    abDotAp = ab.dot(ap);
      
	    if(abDotAp < 0.0) {
		freeVector3d(ab);
		freeVector3d(ap);
		return false; // line segment points away from sphere.
	    }

	    abLenSq = ab.lengthSquared();
	    acLenSq = abDotAp * abDotAp / abLenSq;

	    if(acLenSq < abLenSq) {
		freeVector3d(ab);
		freeVector3d(ap);
		return false; // C doesn't lies between end points of edge.
	    }

	    radiusSq = sphere.radius * sphere.radius;
	    apLenSq = ap.lengthSquared();
     
	    if((apLenSq - acLenSq) <= radiusSq) {
		freeVector3d(ab);
		freeVector3d(ap);
		return true;
	    }

	    freeVector3d(ab);
	    freeVector3d(ap);
	    return false;

	}


    double det2D(Point2d a, Point2d b, Point2d p)
	{
	    return (((p).x - (a).x) * ((a).y - (b).y) + 
		    ((a).y - (p).y) * ((a).x - (b).x));
	}

    // Assume coord is CCW.
    boolean pointIntersectPolygon2D(Vector3d normal, Point3d[] coord, 
				    Point3d point)
	{

	    double  absNrmX, absNrmY, absNrmZ;
	    Point2d coord2D[] = new Point2d[coord.length];
	    Point2d pnt = new Point2d();

	    int i, j, axis;
      
	    // Project 3d points onto 2d plane.
	    // Note : Area of polygon is not preserve in this projection, but
	    // it doesn't matter here. 
    
	    // Find the axis of projection.
	    absNrmX = Math.abs(normal.x);
	    absNrmY = Math.abs(normal.y);
	    absNrmZ = Math.abs(normal.z);
      
	    if(absNrmX > absNrmY)
		axis = 0;
	    else 
		axis = 1;
      
	    if(axis == 0) {
		if(absNrmX < absNrmZ)
		    axis = 2;
	    }    
	    else if(axis == 1) {
		if(absNrmY < absNrmZ)
		    axis = 2;
	    }    
    
	    // System.out.println("Normal " + normal + " axis " + axis );
     	
	    for(i=0; i<coord.length; i++) {
		coord2D[i] = new Point2d();
	
		switch (axis) {
		case 0:
		    coord2D[i].x = coord[i].y;
		    coord2D[i].y = coord[i].z;
		    break;
	
		case 1:
		    coord2D[i].x = coord[i].x;
		    coord2D[i].y = coord[i].z;
		    break;
	
		case 2:
		    coord2D[i].x = coord[i].x;
		    coord2D[i].y = coord[i].y;
		    break;      
		} 
    
		// System.out.println("i " + i + " u " + uCoor[i] + " v " + vCoor[i]); 
	    }


	    switch (axis) {
	    case 0:
		pnt.x = point.y;
		pnt.y = point.z;
		break;
	
	    case 1:
		pnt.x = point.x;
		pnt.y = point.z;
		break;
	
	    case 2:
		pnt.x = point.x;
		pnt.y = point.y;
		break;      
	    }

	    // Do determinant test.
	    for(j=0; j<coord.length; j++) {
		if(j<(coord.length-1))
		    if(det2D(coord2D[j], coord2D[j+1], pnt)>0.0)
			;
		    else
			return false;
		else
		    if(det2D(coord2D[j], coord2D[0], pnt)>0.0)
			;
		    else
			return false;
	    }
      
	    return true;

	}


    boolean edgeIntersectPlane(Vector3d normal, Point3d pnt, Point3d start,
			       Point3d end, Point3d iPnt)
	{
      
	    Vector3d tempV3d = getVector3d();
	    Vector3d direction = getVector3d();
	    double pD, pNrmDotrDir, tr;
      
	    // Compute plane D.
	    tempV3d.set((Tuple3d) pnt);
	    pD = normal.dot(tempV3d);
      
	    direction.x = end.x - start.x;
	    direction.y = end.y - start.y;
	    direction.z = end.z - start.z;

	    pNrmDotrDir = normal.dot(direction);
    
	    // edge is parallel to plane. 
	    if(pNrmDotrDir== 0.0) {
		// System.out.println("Edge is parallel to plane.");
		freeVector3d(tempV3d);
		freeVector3d(direction);
		return false;        
	    }

	    tempV3d.set((Tuple3d) start);
      
	    tr = (pD - normal.dot(tempV3d))/ pNrmDotrDir;
      
	    // Edge intersects the plane behind the edge's start.
	    // or exceed the edge's length.
	    if((tr < 0.0 ) || (tr > 1.0 )) {
		// System.out.println("Edge intersects the plane behind the start or exceed end.");
		freeVector3d(tempV3d);
		freeVector3d(direction);
		return false;
	    }

	    iPnt.x = start.x + tr * direction.x;
	    iPnt.y = start.y + tr * direction.y;
	    iPnt.z = start.z + tr * direction.z;

	    freeVector3d(tempV3d);
	    freeVector3d(direction);
	    return true;

	}

    // Assume coord is CCW.
    boolean edgeIntersectPolygon2D(Vector3d normal, Point3d[] coord, 
				   Point3d[] seg)
	{

	    double  absNrmX, absNrmY, absNrmZ;
	    Point2d coord2D[] = new Point2d[coord.length];
	    Point2d seg2D[] = new Point2d[2];

	    int i, j, axis;
      
	    // Project 3d points onto 2d plane.
	    // Note : Area of polygon is not preserve in this projection, but
	    // it doesn't matter here. 
    
	    // Find the axis of projection.
	    absNrmX = Math.abs(normal.x);
	    absNrmY = Math.abs(normal.y);
	    absNrmZ = Math.abs(normal.z);
      
	    if(absNrmX > absNrmY)
		axis = 0;
	    else 
		axis = 1;
      
	    if(axis == 0) {
		if(absNrmX < absNrmZ)
		    axis = 2;
	    }    
	    else if(axis == 1) {
		if(absNrmY < absNrmZ)
		    axis = 2;
	    }    
    
	    // System.out.println("Normal " + normal + " axis " + axis );
     	
	    for(i=0; i<coord.length; i++) {
		coord2D[i] = new Point2d();
	
		switch (axis) {
		case 0:
		    coord2D[i].x = coord[i].y;
		    coord2D[i].y = coord[i].z;
		    break;
	
		case 1:
		    coord2D[i].x = coord[i].x;
		    coord2D[i].y = coord[i].z;
		    break;
	
		case 2:
		    coord2D[i].x = coord[i].x;
		    coord2D[i].y = coord[i].y;
		    break;      
		} 
    
		// System.out.println("i " + i + " u " + uCoor[i] + " v " + vCoor[i]); 
	    }

	    for(i=0; i<2; i++) {
		seg2D[i] = new Point2d();
		switch (axis) {
		case 0:
		    seg2D[i].x = seg[i].y;
		    seg2D[i].y = seg[i].z;
		    break;
	
		case 1:
		    seg2D[i].x = seg[i].x;
		    seg2D[i].y = seg[i].z;
		    break;
	
		case 2:
		    seg2D[i].x = seg[i].x;
		    seg2D[i].y = seg[i].y;
		    break;      
		} 
    
		// System.out.println("i " + i + " u " + uSeg[i] + " v " + vSeg[i]); 
	    }

	    // Do determinant test.
	    boolean pntTest[][] = new boolean[2][coord.length];
	    boolean testFlag;

	    for(j=0; j<coord.length; j++) {
		for(i=0; i<2; i++) {
		    if(j<(coord.length-1))
			pntTest[i][j] = (det2D(coord2D[j], coord2D[j+1], seg2D[i])<0.0);
		    else
			pntTest[i][j] = (det2D(coord2D[j], coord2D[0], seg2D[i])<0.0);
		}

		if((pntTest[0][j]==false) && (pntTest[1][j]==false))
		    return false;
	    }
      
	    testFlag = true;
	    for(i=0; i<coord.length; i++) {
		if(pntTest[0][i]==false) {
		    testFlag = false;
		    break;
		}
	    }
      
	    if(testFlag == true)
		return true; // start point is inside polygon.

	    testFlag = true;
	    for(i=0; i<coord.length; i++) {
		if(pntTest[1][i]==false) {
		    testFlag = false;
		    break;
		}
	    }
      
	    if(testFlag == true)
		return true; // end point is inside polygon.
      

	    int cnt = 0;
	    for(i=0; i<coord.length; i++) {
		if(det2D(seg2D[0], seg2D[1], coord2D[i])<0.0)
		    cnt++;
	    }

	    if((cnt==0)||(cnt==coord.length))
		return false;

	    return true;

	}


    // New stuffs .....
    double getCompValue(Point3d v, int i) {
	switch (i) {
	case 0: return v.x;
	case 1: return v.y;
	}
	// Has to return something, so set the default to z component.
	return v.z;
    }
    
    double getCompValue(Point3d v0, Point3d v1, int i) {
	switch (i) {
	case 0: return (v0.x - v1.x);
	case 1: return (v0.y - v1.y);
	}
	// Has to return some, so set the default to z component.
	return (v0.z - v1.z);
    }


    boolean pointInTri(Point3d v0, Point3d u0, Point3d u1, Point3d u2,
		       Vector3d normal) {

	double nAbsX, nAbsY, nAbsZ;
	int i0, i1;

	// first project onto an axis-aligned plane, that maximizes the area
	// of the triangles, compute indices i0, i1.
	nAbsX = Math.abs(normal.x);
	nAbsY = Math.abs(normal.y);
	nAbsZ = Math.abs(normal.z);

	if (nAbsX > nAbsY) {
	    if(nAbsX > nAbsZ) {
		i0 = 1; //  nAbsX is greatest.
		i1 = 2;
	    }
	    else {
		i0 = 0; //  nAbsZ is greatest.
		i1 = 1;
	    }
	} else { // nAbsX <= nAbsY
	    if(nAbsZ > nAbsY) {
		i0 = 0;  //  nAbsZ is greatest.
		i1 = 1;
	    }
	    else {
		i0 = 0; //  nAbsY is greatest.
		i1 = 2;
	    }
	}
	return pointInTri(v0, u0,  u1, u2, i0,  i1);
    }

    boolean pointInTri(Point3d v0, Point3d u0, Point3d u1, Point3d u2,
		       int i0, int i1) {

	double a, b, c, d0, d1, d2;
	// is T1 completely inside T2 ?
	// check if v0 is inside tri(u0,u1,u2)

	a = getCompValue(u1, u0, i1);
	b = -(getCompValue(u1, u0, i0));
	c = -a * getCompValue(u0, i0) - b * getCompValue(u0, i1); 
	d0 = a * getCompValue(v0, i0) + b * getCompValue(v0, i1) + c;

	a = getCompValue(u2, u1, i1);
	b = -(getCompValue(u2, u1, i0));
	c = -a * getCompValue(u1, i0) - b * getCompValue(u1, i1); 
	d1 = a * getCompValue(v0, i0) + b * getCompValue(v0, i1) + c;

	a = getCompValue(u0, u2, i1);
	b = -(getCompValue(u0, u2, i0));
	c = -a * getCompValue(u2, i0) - b * getCompValue(u2, i1); 
	d2 = a * getCompValue(v0, i0) + b * getCompValue(v0, i1) + c;

	if(d0*d1>0.0) {
	    if(d0*d2>0.0) {
		return true;
	    }
	}
	return false;
    }

    
    // this edge to edge test is based on Franlin Antonio's gem:
    // "Faster line segment intersection", in Graphics Gems III, pp 199-202
    boolean edgeAgainstEdge(Point3d v0, Point3d u0, Point3d u1, double aX, double aY,
			    int i0, int i1) {
	double bX, bY, cX, cY, e, d, f;

	bX = getCompValue(u0, u1,i0);
	bY = getCompValue(u0, u1, i1);
	cX = getCompValue(v0, u0, i0);
	cY = getCompValue(v0, u0, i1);

	f = aY * bX - aX * bY;
	d = bY * cX - bX * cY;
	if((f>0 && d>=0 && d<=f) || (f<0 && d<=0 && d>=f)) {
	    e = aX * cY - aY * cX;
	    if(f>0) {
		if(e>=0 && e<=f)
		    return true;
	    }
	    else {
		if(e<=0 && e>=f)
		    return true;
	    }
	}
	
	return false;
    }

    
    boolean edgeAgainstTriEdges(Point3d v0, Point3d v1, Point3d u0,
				Point3d u1, Point3d u2, int i0, int i1) {
	double aX, aY;

	// aX = v1[i0] - v0[i0];
	// aY = v1[i1] - v0[i1];
	aX = getCompValue(v1, v0, i0);
	aY = getCompValue(v1, v0, i1);
	
	// test edge u0, u1 against v0, v1
	if(edgeAgainstEdge(v0, u0, u1, aX, aY, i0, i1))
	    return true;
	// test edge u1, u2 against v0, v1
	if(edgeAgainstEdge(v0, u1, u2, aX, aY, i0, i1))
	    return true;
	// test edge u2, u0 against v0, v1
	if(edgeAgainstEdge(v0, u2, u0, aX, aY, i0, i1))
	    return true;

	return false;

    }
    
    boolean coplanarTriTri(Vector3d normal, Point3d v0, Point3d v1, Point3d v2,
			   Point3d u0, Point3d u1, Point3d u2) {

	double nAbsX, nAbsY, nAbsZ;
	int i0, i1;
	
	// first project onto an axis-aligned plane, that maximizes the area
	// of the triangles, compute indices i0, i1.
	nAbsX = Math.abs(normal.x);
	nAbsY = Math.abs(normal.y);
	nAbsZ = Math.abs(normal.z);

	if(nAbsX > nAbsY) {
	    if(nAbsX > nAbsZ) {
		i0 = 1; //  nAbsX is greatest.
		i1 = 2;
	    }
	    else {
		i0 = 0; //  nAbsZ is greatest.
		i1 = 1;
	    }
	}
	else { // nAbsX <= nAbsY
	    if(nAbsZ > nAbsY) {
		i0 = 0;  //  nAbsZ is greatest.
		i1 = 1;
	    }
	    else {
		i0 = 0; //  nAbsY is greatest.
		i1 = 2;
	    }
	}

	// test all edges of triangle 1 against the edges of triangle 2
	if(edgeAgainstTriEdges(v0, v1, u0, u1, u2, i0, i1))
	    return true;
	
	if(edgeAgainstTriEdges(v1, v2, u0, u1, u2, i0, i1))
	    return true;
	
	if(edgeAgainstTriEdges(v2, v0, u0, u1, u2, i0, i1))
	    return true;

	// finally, test if tri1 is totally contained in tri2 or vice versa.
	if(pointInTri(v0, u0, u1, u2, i0, i1))
	    return true;
	
	if(pointInTri(u0, v0, v1, v2, i0, i1))
	    return true;
	
	return false;
    }
    




    boolean intersectTriPnt(Point3d v0, Point3d v1, Point3d v2, Point3d u) {
	
	Vector3d e1 = getVector3d();
	Vector3d e2 = getVector3d();
	Vector3d n1 = getVector3d();
	Vector3d tempV3d = getVector3d();
	
	double d1, du;
	
	// compute plane equation of triange(coord1)
	e1.x = v1.x - v0.x;
	e1.y = v1.y - v0.y;
	e1.z = v1.z - v0.z;
	
	e2.x = v2.x - v0.x;
	e2.y = v2.y - v0.y;
	e2.z = v2.z - v0.z;
	
	n1.cross(e1,e2);
	
	if(n1.length() == 0.0) {
	    // System.out.println("(1) Degenerate triangle.");
	    freeVector3d(e1);
	    freeVector3d(e2);
	    freeVector3d(n1);
	    freeVector3d(tempV3d);
	    return false;  // Degenerate triangle.
	}
	
	tempV3d.set((Tuple3d) v0);
	d1 = - n1.dot(tempV3d); // plane equation 1: n1.x + d1 = 0 
	
	// put u to compute signed distance to the plane.
	tempV3d.set((Tuple3d) u);
	du = n1.dot(tempV3d) + d1;
	
	// coplanarity robustness check
	if(Math.abs(du)<EPS) du = 0.0;
	
	// no intersection occurs
	if(du != 0.0) {
	    freeVector3d(e1);
	    freeVector3d(e2);
	    freeVector3d(n1);
	    freeVector3d(tempV3d);
	    return false;
	}

	double nAbsX, nAbsY, nAbsZ;
	int i0, i1;
	
	// first project onto an axis-aligned plane, that maximizes the area
	// of the triangles, compute indices i0, i1.
	nAbsX = Math.abs(n1.x);
	nAbsY = Math.abs(n1.y);
	nAbsZ = Math.abs(n1.z);

	if(nAbsX > nAbsY) {
	    if(nAbsX > nAbsZ) {
		i0 = 1; //  nAbsX is greatest.
		i1 = 2;
	    }
	    else {
		i0 = 0; //  nAbsZ is greatest.
		i1 = 1;
	    }
	}
	else { // nAbsX <= nAbsY
	    if(nAbsZ > nAbsY) {
		i0 = 0;  //  nAbsZ is greatest.
		i1 = 1;
	    }
	    else {
		i0 = 0; //  nAbsY is greatest.
		i1 = 2;
	    }
	}
	
	
	// finally, test if u is totally contained in tri.	
	if(pointInTri(u, v0, v1, v2, i0, i1)) {
	    freeVector3d(e1);
	    freeVector3d(e2);
	    freeVector3d(n1);
	    freeVector3d(tempV3d);
	    return true;
	}
	
	freeVector3d(e1);
	freeVector3d(e2);
	freeVector3d(n1);
	freeVector3d(tempV3d);
	return false;
    }

    
    /**
     * Return flag indicating whether two triangles intersect.  This
     * uses Tomas Moller's code for fast triangle-triangle
     * intersection from his "Real-Time Rendering" book.
     *
     * The code is now divisionless. It tests for separating by planes
     * parallel to either triangle.  If neither separate the
     * triangles, then two cases are considered. First case is if the
     * normals to the triangles are parallel. In that case, the
     * triangles are coplanar and a sequence of tests are made to see
     * if edges of each triangle intersect the other triangle. If the
     * normals are not parallel, then the two triangles can intersect
     * only on the line of intersection of the two planes. The
     * intervals of intersection of the triangles with the line of
     * intersection of the two planes are computed and tested for
     * overlap.
     */
    boolean intersectTriTri(Point3d v0, Point3d v1, Point3d v2,
			    Point3d u0, Point3d u1, Point3d u2) {

	// System.out.println("In intersectTriTri ...");
	Vector3d e1 = getVector3d();
	Vector3d e2 = getVector3d();
	Vector3d n1 = getVector3d();
	Vector3d n2 = getVector3d();
	Vector3d tempV3d = getVector3d();
	
	double d1, d2;
	double du0, du1, du2, dv0, dv1, dv2;
	double du0du1, du0du2, dv0dv1, dv0dv2;
	int index;
	double vp0=0.0, vp1=0.0, vp2=0.0;
	double up0=0.0, up1=0.0, up2=0.0;
	double bb, cc, max;
	
	// compute plane equation of triange(coord1)
	e1.x = v1.x - v0.x;
	e1.y = v1.y - v0.y;
	e1.z = v1.z - v0.z;
	
	e2.x = v2.x - v0.x;
	e2.y = v2.y - v0.y;
	e2.z = v2.z - v0.z;
	
	n1.cross(e1,e2);
	
	if(n1.length() == 0.0) {
	    // System.out.println("(1) Degenerate triangle.");
	    freeVector3d(e1);
	    freeVector3d(e2);
	    freeVector3d(n1);
	    freeVector3d(n2);
	    freeVector3d(tempV3d);
	    return false;  // Degenerate triangle.
	}
	
	tempV3d.set((Tuple3d) v0);
	d1 = - n1.dot(tempV3d); // plane equation 1: n1.x + d1 = 0 
	
	// put u0, u1, and u2 into plane equation 1
	// to compute signed distance to the plane.
	tempV3d.set((Tuple3d) u0);
	du0 = n1.dot(tempV3d) + d1;
	tempV3d.set((Tuple3d) u1);
	du1 = n1.dot(tempV3d) + d1;
	tempV3d.set((Tuple3d) u2);
	du2 = n1.dot(tempV3d) + d1;
	
	// coplanarity robustness check
	if(Math.abs(du0)<EPS) du0 = 0.0;
	if(Math.abs(du1)<EPS) du1 = 0.0;
	if(Math.abs(du2)<EPS) du2 = 0.0;
	
	du0du1 = du0 * du1;
	du0du2 = du0 * du2;
	
	// same sign on all of them + not equal 0 ?
	// no intersection occurs
	if(du0du1>0.0 && du0du2>0.0) {
	    // System.out.println("In intersectTriTri : du0du1>0.0 && du0du2>0.0");
	    freeVector3d(e1);
	    freeVector3d(e2);
	    freeVector3d(n1);
	    freeVector3d(n2);
	    freeVector3d(tempV3d);
	    return false;
	}
	
	// compute plane of triangle(coord2)
	e1.x = u1.x - u0.x;
	e1.y = u1.y - u0.y;
	e1.z = u1.z - u0.z;
	
	e2.x = u2.x - u0.x;
	e2.y = u2.y - u0.y;
	e2.z = u2.z - u0.z;
	
	n2.cross(e1,e2);
	
	if(n2.length() == 0.0) {
	    // System.out.println("(2) Degenerate triangle.");
	    freeVector3d(e1);
	    freeVector3d(e2);
	    freeVector3d(n1);
	    freeVector3d(n2);
	    freeVector3d(tempV3d);
	    return false;  // Degenerate triangle.
	}
	
	tempV3d.set((Tuple3d) u0);
	d2 = - n2.dot(tempV3d); // plane equation 2: n2.x + d2 = 0 

	// put v0, v1, and v2 into plane equation 2
	// to compute signed distance to the plane.
	tempV3d.set((Tuple3d) v0);
	dv0 = n2.dot(tempV3d) + d2;
	tempV3d.set((Tuple3d) v1);
	dv1 = n2.dot(tempV3d) + d2;
	tempV3d.set((Tuple3d) v2);
	dv2 = n2.dot(tempV3d) + d2;
	
	// coplanarity robustness check
	if(Math.abs(dv0)<EPS) dv0 = 0.0;
	if(Math.abs(dv1)<EPS) dv1 = 0.0;
	if(Math.abs(dv2)<EPS) dv2 = 0.0;
	 
	dv0dv1 = dv0 * dv1;
	dv0dv2 = dv0 * dv2;
	
	// same sign on all of them + not equal 0 ?
	// no intersection occurs
	if(dv0dv1>0.0 && dv0dv2>0.0) {
	    // System.out.println("In intersectTriTri : dv0dv1>0.0 && dv0dv2>0.0");
	    freeVector3d(e1);
	    freeVector3d(e2);
	    freeVector3d(n1);
	    freeVector3d(n2);
	    freeVector3d(tempV3d);
	    return false;
	}
	// compute direction of intersection line.
	tempV3d.cross(n1, n2);

	// compute and index to the largest component of tempV3d.
	max = Math.abs(tempV3d.x);
	index = 0;
	bb = Math.abs(tempV3d.y);
	cc = Math.abs(tempV3d.z);
	if(bb>max) {
	    max=bb;
	    index=1;
	}
	if(cc>max) {
	    max=cc;
	    index=2;
	}

	// this is the simplified projection onto L.

	switch (index) {
	case 0:
	    vp0 = v0.x;
	    vp1 = v1.x;
	    vp2 = v2.x;
	    
	    up0 = u0.x;
	    up1 = u1.x;
	    up2 = u2.x;
	    break;
	case 1:
	    vp0 = v0.y;
	    vp1 = v1.y;
	    vp2 = v2.y;
	    
	    up0 = u0.y;
	    up1 = u1.y;
	    up2 = u2.y;
	    break;
	case 2:
	    vp0 = v0.z;
	    vp1 = v1.z;
	    vp2 = v2.z;
	    
	    up0 = u0.z;
	    up1 = u1.z;
	    up2 = u2.z;
	    break;
	}
	
	// compute intereval for triangle 1.
	double a=0.0, b=0.0, c=0.0, x0=0.0, x1=0.0;
	if(dv0dv1>0.0) {
	    // here we know that dv0dv2 <= 0.0 that is dv0 and dv1 are on the same side,
	    // dv2 on the other side or on the plane.
	    a = vp2; b = (vp0 - vp2) * dv2; c = (vp1 - vp2) * dv2;
	    x0 = dv2 - dv0; x1 = dv2 - dv1;
	}
	else if(dv0dv2>0.0) {
	    // here we know that dv0dv1<=0.0
	    a = vp1; b = (vp0 - vp1) * dv1; c = (vp2 - vp1) * dv1;
	    x0 = dv1 - dv0; x1 = dv1 - dv2;
	}
	else if((dv1*dv2>0.0) || (dv0 != 0.0)) {
	    // here we know that dv0vd1<=0.0 or that dv0!=0.0
	    a = vp0; b = (vp1 - vp0) * dv0; c = (vp2 - vp0) * dv0;
	    x0 = dv0 - dv1; x1 = dv0 - dv2;
	}
	else if(dv1 != 0.0) {
	    a = vp1; b = (vp0 - vp1) * dv1; c = (vp2 - vp1) * dv1;
	    x0 = dv1 - dv0; x1 = dv1 - dv2;
	}
	else if(dv2 != 0.0) {
	    a = vp2; b = (vp0 - vp2) * dv2; c = (vp1 - vp2) * dv2;
	    x0 = dv2 - dv0; x1 = dv2 - dv1;
	}	    
	else {
	    // triangles are coplanar
	    boolean toreturn = coplanarTriTri(n1, v0, v1, v2, u0, u1, u2);
	    freeVector3d(e1);
	    freeVector3d(e2);
	    freeVector3d(n1);
	    freeVector3d(n2);
	    freeVector3d(tempV3d);
	    return toreturn;
	}


	// compute intereval for triangle 2.
	double d=0.0, e=0.0, f=0.0, y0=0.0, y1=0.0;
	if(du0du1>0.0) {
	    // here we know that du0du2 <= 0.0 that is du0 and du1 are on the same side,
	    // du2 on the other side or on the plane.
	    d = up2; e = (up0 - up2) * du2; f = (up1 - up2) * du2;
	    y0 = du2 - du0; y1 = du2 - du1;
	}
	else if(du0du2>0.0) {
	    // here we know that du0du1<=0.0
	    d = up1; e = (up0 - up1) * du1; f = (up2 - up1) * du1;
	    y0 = du1 - du0; y1 = du1 - du2;
	}
	else if((du1*du2>0.0) || (du0 != 0.0)) {
	    // here we know that du0du1<=0.0 or that D0!=0.0
	    d = up0; e = (up1 - up0) * du0; f = (up2 - up0) * du0;
	    y0 = du0 - du1; y1 = du0 - du2;
	}
	else if(du1 != 0.0) {
	    d = up1; e = (up0 - up1) * du1; f = (up2 - up1) * du1;
	    y0 = du1 - du0; y1 = du1 - du2;
	}
	else if(du2 != 0.0) {
	    d = up2; e = (up0 - up2) * du2; f = (up1 - up2) * du2;
	    y0 = du2 - du0; y1 = du2 - du1;
	}	    
	else {
	    // triangles are coplanar
	    //	    System.out.println("In intersectTriTri : coplanarTriTri test 2");
	    boolean toreturn =  coplanarTriTri(n2, v0, v1, v2, u0, u1, u2);
	    freeVector3d(e1);
	    freeVector3d(e2);
	    freeVector3d(n1);
	    freeVector3d(n2);
	    freeVector3d(tempV3d);
	    return toreturn;
	}

	double xx, yy, xxyy, tmp, isect1S, isect1E, isect2S, isect2E;
	xx = x0 * x1;
	yy = y0 * y1;
	xxyy = xx * yy;

	tmp = a * xxyy;
	isect1S = tmp + b * x1 * yy;
	isect1E = tmp + c * x0 * yy;

	tmp = d * xxyy;
	isect2S = tmp + e * y1 * xx;
	isect2E = tmp + f * y0 * xx;

	// sort so that isect1S <= isect1E
	if(isect1S > isect1E) {
	    tmp = isect1S;
	    isect1S = isect1E;
	    isect1E = tmp;
	}
	
	// sort so that isect2S <= isect2E
	if(isect2S > isect2E) {
	    tmp = isect2S;
	    isect2S = isect2E;
	    isect2E = tmp;
	}

	if(isect1E<isect2S || isect2E<isect1S) {
	    // System.out.println("In intersectTriTri :isect1E<isect2S || isect2E<isect1S");
	    // System.out.println("In intersectTriTri : return false");
	    freeVector3d(e1);
	    freeVector3d(e2);
	    freeVector3d(n1);
	    freeVector3d(n2);
	    freeVector3d(tempV3d);
	    return false;
	}

	//	 System.out.println("In intersectTriTri : return true");
	freeVector3d(e1);
	freeVector3d(e2);
	freeVector3d(n1);
	freeVector3d(n2);
	freeVector3d(tempV3d);
	return true;
	
    }
    
    

    boolean intersectPolygon(Point3d coord1[], Point3d coord2[]) {
	int i, j;
	Vector3d vec0 = getVector3d(); // Edge vector from point 0 to point 1;
	Vector3d vec1 = getVector3d(); // Edge vector from point 0 to point 2 or 3;
	Vector3d pNrm = getVector3d();
	boolean epFlag;


	// compute plane normal for coord1.
	for(i=0; i<coord1.length-1;) {
	    vec0.x = coord1[i+1].x - coord1[i].x;
	    vec0.y = coord1[i+1].y - coord1[i].y;
	    vec0.z = coord1[i+1].z - coord1[i++].z;
	    if(vec0.length() > 0.0)
		break;
	}
        
	for(j=i; j<coord1.length-1; j++) {
	    vec1.x = coord1[j+1].x - coord1[j].x;
	    vec1.y = coord1[j+1].y - coord1[j].y;
	    vec1.z = coord1[j+1].z - coord1[j].z;
	    if(vec1.length() > 0.0)
		break;
	}
      
	if(j == (coord1.length-1)) {
	    // System.out.println("(1) Degenerate polygon.");
	    freeVector3d(vec0);
	    freeVector3d(vec1);
	    freeVector3d(pNrm);
	    return false;  // Degenerate polygon.
	}

	/*
	  for(i=0; i<coord1.length; i++) 
	  System.out.println("coord1 P" + i + " " + coord1[i]);
	  for(i=0; i<coord2.length; i++) 
	  System.out.println("coord2 P" + i + " " + coord2[i]);
	  */
      
	pNrm.cross(vec0,vec1);
      
	if(pNrm.length() == 0.0) {
	    // System.out.println("(2) Degenerate polygon.");
	    freeVector3d(vec0);
	    freeVector3d(vec1);
	    freeVector3d(pNrm);
	    return false;  // Degenerate polygon.
	}

	j = 0;      
	Point3d seg[] = new Point3d[2];
	seg[0] = getPoint3d();
	seg[1] = getPoint3d();

	for(i=0; i<coord2.length; i++) {
	    if(i < (coord2.length-1))
		epFlag = edgeIntersectPlane(pNrm, coord1[0], coord2[i], 
					    coord2[i+1], seg[j]);
	    else
		epFlag = edgeIntersectPlane(pNrm, coord1[0], coord2[i], 
					    coord2[0], seg[j]);
	    if (epFlag) {
		if(++j>1) {
		    break;
		}
	    }
	}

	if (j==0) {
	    freeVector3d(vec0);
	    freeVector3d(vec1);
	    freeVector3d(pNrm);
	    freePoint3d(seg[0]);
	    freePoint3d(seg[1]);
	    return false;
	}

	if (coord2.length < 3) {
	    boolean toreturn = pointIntersectPolygon2D(pNrm, coord1, seg[0]);
	    freeVector3d(vec0);
	    freeVector3d(vec1);
	    freeVector3d(pNrm);
	    freePoint3d(seg[0]);
	    freePoint3d(seg[1]);
	    return toreturn;
	} else {
	    boolean toreturn = edgeIntersectPolygon2D(pNrm, coord1, seg);
	    freeVector3d(vec0);
	    freeVector3d(vec1);
	    freeVector3d(pNrm);
	    freePoint3d(seg[0]);
	    freePoint3d(seg[1]);
	    return toreturn;
	}
    }

        
    /**
     * Return true if triangle or quad intersects with ray and the
     * distance is stored in dist[0] and the intersect point in iPnt
     * (if iPnt is not null).
     */
    boolean intersectRay(Point3d coordinates[], PickRay ray, double dist[],
			 Point3d iPnt) {
	
	return intersectRayOrSegment(coordinates, ray.direction, ray.origin, 
				     dist, iPnt, false);

    }

    /**
     * Return true if triangle or quad intersects with segment and
     * the distance is stored in dist[0].
     */
    boolean intersectSegment( Point3d coordinates[], Point3d start, Point3d end,
			      double dist[], Point3d iPnt ) {
	boolean result;
	Vector3d direction = getVector3d();
	direction.x = end.x - start.x;
	direction.y = end.y - start.y;
	direction.z = end.z - start.z;
	result = intersectRayOrSegment(coordinates, direction, start, dist, iPnt, true);
	freeVector3d(direction);
	return result;
    }
    


    /**
     *  Return true if triangle or quad intersects with ray and the distance is 
     *  stored in pr.
     */
    boolean intersectRayOrSegment(Point3d coordinates[], 
				  Vector3d direction, Point3d origin,
				  double dist[], Point3d iPnt, boolean isSegment) {
	Vector3d vec0, vec1, pNrm, tempV3d;
	vec0 = getVector3d();
	vec1 = getVector3d();
	pNrm = getVector3d();

	double  absNrmX, absNrmY, absNrmZ, pD = 0.0;
	double pNrmDotrDir = 0.0; 

	boolean isIntersect = false;
	int i, j, k=0, l = 0;

	// Compute plane normal.
	for (i=0; i<coordinates.length; i++) {
	    if (i != coordinates.length-1) {
		l = i+1;
	    } else {
		l = 0;
	    }
	    vec0.x = coordinates[l].x - coordinates[i].x;
	    vec0.y = coordinates[l].y - coordinates[i].y;
	    vec0.z = coordinates[l].z - coordinates[i].z;
	    if (vec0.length() > 0.0) {
		break;
	    }
	}
		

	for (j=l; j<coordinates.length; j++) {
	    if (j != coordinates.length-1) {
		k = j+1;
	    } else {
		k = 0;
	    }
	    vec1.x = coordinates[k].x - coordinates[j].x;
	    vec1.y = coordinates[k].y - coordinates[j].y;
	    vec1.z = coordinates[k].z - coordinates[j].z;
	    if (vec1.length() > 0.0) {
		break;
	    }
	}		

	pNrm.cross(vec0,vec1);

	if ((vec1.length() == 0) || (pNrm.length() == 0)) {
	    // degenerate to line if vec0.length() == 0
	    // or vec0.length > 0 and vec0 parallel to vec1
	    k = (l == 0 ? coordinates.length-1: l-1);
	    isIntersect = intersectLineAndRay(coordinates[l],
					      coordinates[k],
					      origin,
					      direction, 
					      dist,
					      iPnt);

	    // put the Vectors on the freelist
	    freeVector3d(vec0);
	    freeVector3d(vec1);
	    freeVector3d(pNrm);
	    return isIntersect;
	}

	// It is possible that Quad is degenerate to Triangle 
	// at this point

	pNrmDotrDir = pNrm.dot(direction);

    	// Ray is parallel to plane. 
	if (pNrmDotrDir == 0.0) {
	    // Ray is parallel to plane
	    // Check line/triangle intersection on plane.
	    for (i=0; i < coordinates.length ;i++) {
		if (i != coordinates.length-1) {
		    k = i+1;
		} else {
		    k = 0;
		}
		if (intersectLineAndRay(coordinates[i],
					coordinates[k],
					origin,
					direction, 
					dist,
					iPnt)) {
		    isIntersect = true;
		    break;
		}
	    }
	    freeVector3d(vec0);
	    freeVector3d(vec1);
	    freeVector3d(pNrm);
	    return isIntersect;
	}

	// Plane equation: (p - p0)*pNrm = 0 or p*pNrm = pD;
	tempV3d = getVector3d();
	tempV3d.set((Tuple3d) coordinates[0]);
	pD = pNrm.dot(tempV3d);
	tempV3d.set((Tuple3d) origin);

	// Substitute Ray equation:
	// p = origin + pi.distance*direction
	// into the above Plane equation 

	dist[0] = (pD - pNrm.dot(tempV3d))/ pNrmDotrDir;

	// Ray intersects the plane behind the ray's origin.
	if ((dist[0] < -EPS ) ||
	    (isSegment && (dist[0] > 1.0+EPS))) {
	    // Ray intersects the plane behind the ray's origin
	    // or intersect point not fall in Segment 
	    freeVector3d(vec0);
	    freeVector3d(vec1);
	    freeVector3d(pNrm);
	    freeVector3d(tempV3d);
	    return false;
	}

	// Now, one thing for sure the ray intersect the plane.
	// Find the intersection point.
	if (iPnt == null) {
	    iPnt = new Point3d();
	}
	iPnt.x = origin.x + direction.x * dist[0];
	iPnt.y = origin.y + direction.y * dist[0];
	iPnt.z = origin.z + direction.z * dist[0];

	// Project 3d points onto 2d plane 
	// Find the axis so that area of projection is maximize.
	absNrmX = Math.abs(pNrm.x);
	absNrmY = Math.abs(pNrm.y);
	absNrmZ = Math.abs(pNrm.z);

	// All sign of (y - y0) (x1 - x0) - (x - x0) (y1 - y0)
	// must agree. 
	double sign, t, lastSign = 0;
	Point3d p0 = coordinates[coordinates.length-1];
	Point3d p1 = coordinates[0];

	isIntersect = true;

	if (absNrmX > absNrmY) {
	    if (absNrmX < absNrmZ) {
		for (i=0; i < coordinates.length; i++) {
		    p0 = coordinates[i];
		    p1 = (i != coordinates.length-1 ? coordinates[i+1]: coordinates[0]);
 		    sign = (iPnt.y - p0.y)*(p1.x - p0.x) - 
			   (iPnt.x - p0.x)*(p1.y - p0.y);
		    if (isNonZero(sign)) {
			if (sign*lastSign < 0) {
			    isIntersect = false;
			    break;
			}
			lastSign = sign;
		    } else { // point on line, check inside interval
			t = p1.y - p0.y;
			if (isNonZero(t)) {
			    t = (iPnt.y - p0.y)/t;
			    isIntersect = ((t > -EPS) && (t < 1+EPS));
			    break;
			} else {
			    t = p1.x - p0.x;
			    if (isNonZero(t)) {
				t = (iPnt.x - p0.x)/t;
				isIntersect = ((t > -EPS) && (t < 1+EPS));
				break;
			    } else {
    // Ignore degenerate line=>point happen when Quad => Triangle. 
    // Note that by next round sign*lastSign = 0 so it will
    // not pass the interest test. This should only happen once in the
    // loop because we already check for degenerate geometry before.
			    }
			}
		    }
		} 
	    } else {
		for (i=0; i<coordinates.length; i++) {
		    p0 = coordinates[i];
		    p1 = (i != coordinates.length-1 ? coordinates[i+1]: coordinates[0]);
		    sign = (iPnt.y - p0.y)*(p1.z - p0.z) - 
			   (iPnt.z - p0.z)*(p1.y - p0.y);
		    if (isNonZero(sign)) {
			if (sign*lastSign < 0) {
			    isIntersect = false;
			    break;
			}
			lastSign = sign;
		    } else { // point on line, check inside interval
			t = p1.y - p0.y;

			if (isNonZero(t)) {
			    t = (iPnt.y - p0.y)/t;
			    isIntersect = ((t > -EPS) && (t < 1+EPS));
			    break;

			} else {
			    t = p1.z - p0.z;
			    if (isNonZero(t)) {
				t = (iPnt.z - p0.z)/t;
				isIntersect = ((t > -EPS) && (t < 1+EPS));
				break;
			    } else {
				//degenerate line=>point
			    }
			}
		    }
		} 
	    }
	} else {
	    if (absNrmY < absNrmZ) {
		for (i=0; i<coordinates.length; i++) {
		    p0 = coordinates[i];
		    p1 = (i != coordinates.length-1 ? coordinates[i+1]: coordinates[0]);
		    sign = (iPnt.y - p0.y)*(p1.x - p0.x) - 
			   (iPnt.x - p0.x)*(p1.y - p0.y);
		    if (isNonZero(sign)) {
			if (sign*lastSign < 0) {
			    isIntersect = false;
			    break;
			}
			lastSign = sign;
		    } else { // point on line, check inside interval
			t = p1.y - p0.y;
			if (isNonZero(t)) {
			    t = (iPnt.y - p0.y)/t;
			    isIntersect = ((t > -EPS) && (t < 1+EPS));
			    break;
			} else {
			    t = p1.x - p0.x;
			    if (isNonZero(t)) {
				t = (iPnt.x - p0.x)/t;
				isIntersect = ((t > -EPS) && (t < 1+EPS));
				break;
			    } else {
				//degenerate line=>point
			    }
			}
		    }
		}
	    } else {
		for (i=0; i<coordinates.length; i++) {
		    p0 = coordinates[i];
		    p1 = (i != coordinates.length-1 ? coordinates[i+1]: coordinates[0]);
		    sign = (iPnt.x - p0.x)*(p1.z - p0.z) - 
			   (iPnt.z - p0.z)*(p1.x - p0.x);
		    if (isNonZero(sign)) {
			if (sign*lastSign < 0) {
			    isIntersect = false;
			    break;
			}
			lastSign = sign;
		    } else { // point on line, check inside interval
			t = p1.x - p0.x;
			if (isNonZero(t)) {
			    t = (iPnt.x - p0.x)/t;
			    isIntersect = ((t > -EPS) && (t < 1+EPS));
			    break;
			} else {
			    t = p1.z - p0.z;
			    if (isNonZero(t)) {
				t = (iPnt.z - p0.z)/t;
				isIntersect = ((t > -EPS) && (t < 1+EPS));
				break;
			    } else {
				//degenerate line=>point
			    }
			}
		    }
		}
	    }
	}
	
	if (isIntersect) {
	    dist[0] *= direction.length();
	}
	freeVector3d(vec0);
	freeVector3d(vec1);
	freeVector3d(pNrm);
	freeVector3d(tempV3d);
	return isIntersect;
    }



    static final boolean isNonZero(double v) {
	return ((v > EPS) || (v < -EPS));
	
    }

    /**
     * Return true if point is on the inside of halfspace test. The
     * halfspace is partition by the plane of triangle or quad.
     */
    boolean inside( Point3d coordinates[], PickPoint point, int ccw ) {
    
	Vector3d vec0 = new Vector3d(); // Edge vector from point 0 to point 1;
	Vector3d vec1 = new Vector3d(); // Edge vector from point 0 to point 2 or 3;
	Vector3d pNrm = new Vector3d();
	double  absNrmX, absNrmY, absNrmZ, pD = 0.0;
	Vector3d tempV3d = new Vector3d();
	double pNrmDotrDir = 0.0; 
    
	double tempD;

	int i, j;

	// Compute plane normal.
	for(i=0; i<coordinates.length-1;) {
	    vec0.x = coordinates[i+1].x - coordinates[i].x;
	    vec0.y = coordinates[i+1].y - coordinates[i].y;
	    vec0.z = coordinates[i+1].z - coordinates[i++].z;
	    if(vec0.length() > 0.0)
		break;
	}
        
	for(j=i; j<coordinates.length-1; j++) {
	    vec1.x = coordinates[j+1].x - coordinates[j].x;
	    vec1.y = coordinates[j+1].y - coordinates[j].y;
	    vec1.z = coordinates[j+1].z - coordinates[j].z;
	    if(vec1.length() > 0.0)
		break;
	}
    
	if(j == (coordinates.length-1)) {
	    // System.out.println("(1) Degenerate polygon.");
	    return false;  // Degenerate polygon.
	}

	/* 
	   System.out.println("Ray orgin : " + ray.origin + " dir " + ray.direction);
	   System.out.println("Triangle/Quad :");
	   for(i=0; i<coordinates.length; i++) 
	   System.out.println("P" + i + " " + coordinates[i]);
	   */

	if( ccw == 0x1)
	    pNrm.cross(vec0,vec1);
	else
	    pNrm.cross(vec1,vec0);
    
	if(pNrm.length() == 0.0) {
	    // System.out.println("(2) Degenerate polygon.");
	    return false;  // Degenerate polygon.
	}
	// Compute plane D.
	tempV3d.set((Tuple3d) coordinates[0]);
	pD = pNrm.dot(tempV3d);
	tempV3d.set((Tuple3d) point.location);

	return ((pD - pNrm.dot(tempV3d)) <= 0);
    }

    boolean intersectPntAndPnt( Point3d pnt1, Point3d pnt2 ) {
	return ((pnt1.x == pnt2.x) && 
		(pnt1.y == pnt2.y) && 
		(pnt1.z == pnt2.z));
    }

    boolean intersectPntAndRay(Point3d pnt, Point3d ori, Vector3d dir, 
			       double dist[]) {
	int flag = 0;
	double temp;

	if(dir.x != 0.0) {
	    flag = 0;
	    dist[0] = (pnt.x - ori.x)/dir.x;
	}
	else if(dir.y != 0.0) {
	    if(pnt.x != ori.x)
		return false;
	    flag = 1;
	    dist[0] = (pnt.y - ori.y)/dir.y;
	}
	else if(dir.z != 0.0) {
	    if((pnt.x != ori.x)||(pnt.y != ori.y))
		return false;
	    flag = 2;
	    dist[0] = (pnt.z - ori.z)/dir.z;
           
	}
	else
	    return false;

	if(dist[0] < 0.0)
	    return false;

	if(flag == 0) {
	    temp = ori.y + dist[0] * dir.y;
	    if((pnt.y < (temp - EPS)) || (pnt.y > (temp + EPS)))
		return false;    
	}
    
	if(flag < 2) {
	    temp = ori.z + dist[0] * dir.z;
	    if((pnt.z < (temp - EPS)) || (pnt.z > (temp + EPS)))
		return false;
	}

	return true;
    
    }
    
    boolean intersectLineAndRay(Point3d start, Point3d end, 
				Point3d ori, Vector3d dir, double dist[],
				Point3d iPnt) {
    
	double m00, m01, m10, m11;
	double mInv00, mInv01, mInv10, mInv11;
	double dmt, t, s, tmp1, tmp2;
	Vector3d lDir;

	//     System.out.println("GeometryArrayRetained : intersectLineAndRay");
	//     System.out.println("start " + start + " end " + end );
	//     System.out.println("ori " + ori + " dir " + dir);
    
	lDir = getVector3d();
	lDir.x = (end.x - start.x);
	lDir.y = (end.y - start.y);
	lDir.z = (end.z - start.z);
    
	m00 = lDir.x;
	m01 = -dir.x;
	m10 = lDir.y;
	m11 = -dir.y;

	// Get the determinant.
	dmt = (m00 * m11) - (m10 * m01);

	if (dmt==0.0) { // No solution, check degenerate line
	    boolean isIntersect = false;
	    if ((lDir.x == 0) && (lDir.y == 0) && (lDir.z == 0)) {
		isIntersect = intersectPntAndRay(start, ori, dir, dist);
		if (isIntersect && (iPnt != null)) {
		    iPnt.set(start);
		}
	    }
	    freeVector3d(lDir);
	    return isIntersect;
	}
	// Find the inverse.
	tmp1 = 1/dmt;

	mInv00 = tmp1 * m11;
	mInv01 = tmp1 * (-m01);
	mInv10 = tmp1 * (-m10);
	mInv11 = tmp1 * m00;

	tmp1 = ori.x - start.x;
	tmp2 = ori.y - start.y;

	t = mInv00 * tmp1 + mInv01 * tmp2;
	s = mInv10 * tmp1 + mInv11 * tmp2;
    
	if(s<0.0) { // Before the origin of ray.
	    // System.out.println("Before the origin of ray " + s);
	    freeVector3d(lDir);
	    return false;
	}
	if((t<0)||(t>1.0)) {// Before or after the end points of line.
	    // System.out.println("Before or after the end points of line. " + t);
	    freeVector3d(lDir);
	    return false;
	}

	tmp1 = ori.z + s * dir.z;
	tmp2 = start.z + t * lDir.z;
  
	if((tmp1 < (tmp2 - EPS)) || (tmp1 > (tmp2 + EPS))) {
	    // System.out.println("No intersection : tmp1 " + tmp1 + " tmp2 " + tmp2);
	    freeVector3d(lDir);
	    return false;
	}

	dist[0] = s;

	if (iPnt != null) {
	    // compute point of intersection.
	    iPnt.x = ori.x + dir.x * dist[0];
	    iPnt.y = ori.y + dir.y * dist[0];
	    iPnt.z = ori.z + dir.z * dist[0];
	}
	
	// System.out.println("Intersected : tmp1 " + tmp1 + " tmp2 " + tmp2);
	freeVector3d(lDir);
	return true;
    }

    /**
      Return true if triangle or quad intersects with cylinder. The 
      distance is stored in dist.
      */
    boolean intersectCylinder(Point3d coordinates[], PickCylinder cyl,
			      double dist[], Point3d iPnt) {
	
	Point3d origin = getPoint3d();
	Point3d end = getPoint3d();
	Vector3d direction = getVector3d();
	Point3d iPnt1 = getPoint3d();
	Vector3d originToIpnt = getVector3d();

	if (iPnt == null) {
	    iPnt = new Point3d();
	}

	// Get cylinder information
	cyl.getOrigin (origin);
	cyl.getDirection (direction);
	double radius = cyl.getRadius ();

	if (cyl instanceof PickCylinderSegment) {
	    ((PickCylinderSegment)cyl).getEnd (end);
	}

	// If the ray intersects, we're good (do not do this if we only have
	// a segment
	if (coordinates.length > 2) {
	    if (cyl instanceof PickCylinderRay) {
		if (intersectRay(coordinates, 
				 new PickRay(origin, direction),
				 dist, iPnt)) {

		    freePoint3d(origin);
		    freePoint3d(end);
		    freeVector3d(direction);
		    freeVector3d(originToIpnt);
		    freePoint3d(iPnt1);
		    
		    return true;
		}
	    }
	    else {
		if (intersectSegment(coordinates, origin, end, dist, iPnt)) {
		    freePoint3d(origin);
		    freePoint3d(end);
		    freeVector3d(direction);
		    freeVector3d(originToIpnt);
		    freePoint3d(iPnt1);
		    return true;
		}
	    }
	}
	
	// Ray doesn't intersect, check distance to edges
	double sqDistToEdge;
	int j;
	for (int i=0; i<coordinates.length;i++) {
	    j = (i < coordinates.length-1 ? i+1: 0);
	    if (cyl instanceof PickCylinderSegment) {
		sqDistToEdge = 
		    Distance.segmentToSegment(origin, end, 
					      coordinates[i], coordinates[j],
					      iPnt1, iPnt, null);
	    }
	    else {
		sqDistToEdge = 
		    Distance.rayToSegment(origin, direction, 
					  coordinates[i], coordinates[j],
					  iPnt1, iPnt, null);
	    }
	    if (sqDistToEdge <= radius*radius) {
		originToIpnt.sub (iPnt1, origin);
		dist[0] = originToIpnt.length();
		freePoint3d(origin);
		freePoint3d(end);
		freeVector3d(direction);
		freeVector3d(originToIpnt);
		freePoint3d(iPnt1);
		return true;
	    }
	}
	freePoint3d(origin);
	freePoint3d(end);
	freeVector3d(direction);
	freeVector3d(originToIpnt);
	freePoint3d(iPnt1);
	return false;
    }
    
    /**
      Return true if triangle or quad intersects with cone. The 
      distance is stored in dist.
      */
    boolean intersectCone(Point3d coordinates[], PickCone cone,
			  double[] dist, Point3d iPnt) {
	
	Point3d origin = getPoint3d();
	Point3d end = getPoint3d();
	Vector3d direction = getVector3d();
	Vector3d originToIpnt = getVector3d();
	double distance;
    
	Point3d iPnt1 = getPoint3d();
	Vector3d vector = getVector3d();

	if (iPnt == null) {
	    iPnt = new Point3d();
	}
	// Get cone information
	cone.getOrigin (origin);
	cone.getDirection (direction);
	double radius;

	if (cone instanceof PickConeSegment) {
	    ((PickConeSegment)cone).getEnd (end);
	}

	// If the ray intersects, we're good (do not do this if we only have
	// a segment
	if (coordinates.length > 2) {
	    if (cone instanceof PickConeRay) {
		if (intersectRay(coordinates, 
				 new PickRay (origin, direction), 
				 dist, iPnt)) {
		    freePoint3d(origin);
		    freePoint3d(end);
		    freePoint3d(iPnt1);
		    freeVector3d(direction);
		    freeVector3d(originToIpnt);
		    freeVector3d(vector);
		    return true;
		}
	    }
	    else {
		if (intersectSegment(coordinates, origin, end, dist, iPnt)) {
		    freePoint3d(origin);
		    freePoint3d(end);
		    freePoint3d(iPnt1);
		    freeVector3d(direction);
		    freeVector3d(originToIpnt);
		    freeVector3d(vector);
		    return true;
		}
	    }
	}

	// Ray doesn't intersect, check distance to edges
	double sqDistToEdge;
	int j = 0;
	for (int i=0; i<coordinates.length;i++) {
	    j = (i < coordinates.length-1 ? i+1: 0);
	    if (cone instanceof PickConeSegment) {
		sqDistToEdge = 
		    Distance.segmentToSegment (origin, end, 
					       coordinates[i], coordinates[j],
					       iPnt1, iPnt, null);
	    }
	    else {
		sqDistToEdge = 
		    Distance.rayToSegment (origin, direction, 
					   coordinates[i], coordinates[j],
					   iPnt1, iPnt, null);
	    }
	    originToIpnt.sub(iPnt1, origin);      
	    distance = originToIpnt.length();
	    radius = Math.tan (cone.getSpreadAngle()) * distance;
	    if (sqDistToEdge <= radius*radius) {
		//	System.out.println ("intersectCone: edge "+i+" intersected");
		dist[0] = distance;
		freePoint3d(origin);
		freePoint3d(end);
		freePoint3d(iPnt1);
		freeVector3d(direction);
		freeVector3d(originToIpnt);
		freeVector3d(vector);
		return true;
	    }
	}
	freePoint3d(origin);
	freePoint3d(end);
	freePoint3d(iPnt1);
	freeVector3d(direction);
	freeVector3d(originToIpnt);
	freeVector3d(vector);
	return false;
    }

    
    /**
      Return true if point intersects with cylinder and the distance is
      stored in dist.
      */
    boolean intersectCylinder(Point3d pt, PickCylinder cyl,
			      double[] dist) {
	
	Point3d origin = getPoint3d();
	Point3d end = getPoint3d();
	Vector3d direction = getVector3d();
	Point3d iPnt = getPoint3d();
	Vector3d originToIpnt = getVector3d();

	// Get cylinder information
	cyl.getOrigin (origin);
	cyl.getDirection (direction);
	double radius = cyl.getRadius ();
	double sqDist;

	if (cyl instanceof PickCylinderSegment) {
	    ((PickCylinderSegment)cyl).getEnd (end);
	    sqDist = Distance.pointToSegment(pt, origin, end, iPnt, null);
	}
	else {
	    sqDist = Distance.pointToRay(pt, origin, direction, iPnt, null);
	}
	if (sqDist <= radius*radius) {
	    originToIpnt.sub (iPnt, origin);
	    dist[0] = originToIpnt.length();
	    freePoint3d(origin);
	    freePoint3d(end);
	    freePoint3d(iPnt);
	    freeVector3d(originToIpnt);
	    freeVector3d(direction);
	    return true;
	}
	freePoint3d(origin);
	freePoint3d(end);
	freePoint3d(iPnt);
	freeVector3d(originToIpnt);
	freeVector3d(direction);
	return false;
    }
    
    /**
      Return true if point intersects with cone and the 
      distance is stored in pi.
      */
    boolean intersectCone(Point3d pt, PickCone cone, double[] dist) 
    {
	Point3d origin = getPoint3d();
	Point3d end = getPoint3d();
	Vector3d direction = getVector3d();
        Point3d iPnt = getPoint3d();
	Vector3d originToIpnt = getVector3d();

	// Get cone information
	cone.getOrigin (origin);
	cone.getDirection (direction);
	double radius;
	double distance;
	double sqDist;

	if (iPnt == null) {
	    iPnt = new Point3d();
	}

	if (cone instanceof PickConeSegment) {
	    ((PickConeSegment)cone).getEnd (end);
	    sqDist = Distance.pointToSegment (pt, origin, end, iPnt, null);
	}
	else {
	    sqDist = Distance.pointToRay (pt, origin, direction, iPnt, null);
	}
	originToIpnt.sub(iPnt, origin);
	distance = originToIpnt.length();
	radius = Math.tan (cone.getSpreadAngle()) * distance;
	if (sqDist <= radius*radius) {
	    dist[0] = distance;
	    freePoint3d(origin);
	    freePoint3d(end);
	    freePoint3d(iPnt);
	    freeVector3d(direction);
	    freeVector3d(originToIpnt);
	    return true;
	}
	return false;
    }


    void setCoordRefBuffer(J3DBuffer coords) {
	if (coords != null) {
	    switch (coords.getBufferType()) {
	    case J3DBuffer.TYPE_FLOAT:
		assert ((FloatBufferWrapper)coords.getBufferImpl()).isDirect();
		break;
	    case J3DBuffer.TYPE_DOUBLE:
		assert ((DoubleBufferWrapper)coords.getBufferImpl()).isDirect();
		break;
	    case J3DBuffer.TYPE_NULL:
		throw new IllegalArgumentException(J3dI18N.getString("GeometryArray115"));

	    default:
		throw new IllegalArgumentException(J3dI18N.getString("GeometryArray116"));
	    }
	    
	    if (this instanceof IndexedGeometryArrayRetained) {
		IndexedGeometryArrayRetained idx = (IndexedGeometryArrayRetained)this;
		if (3 * idx.maxCoordIndex >= coords.getBufferImpl().limit()) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray23"));
		}
	    } else if (coords.getBufferImpl().limit() < (3*(initialCoordIndex+validVertexCount))) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray99"));
	    }
	}

	// lock the geometry and start to do real work
	geomLock.getLock();
	dirtyFlag |= COORDINATE_CHANGED;
	coordRefBuffer = coords;
	if(coords == null) {
	    floatBufferRefCoords = null;
	    doubleBufferRefCoords = null;
	    // XXXX: if not mix java array with nio buffer
	    // vertexType can be used as vertexTypeBuffer 
	    vertexType &= ~PD;
	    vertexType &= ~PF;
	}else {
	    switch (coords.getBufferType()) {
	    case J3DBuffer.TYPE_FLOAT:
		floatBufferRefCoords =
		    (FloatBufferWrapper)coords.getBufferImpl();
		doubleBufferRefCoords = null;
		vertexType |= PF;
		vertexType &= ~PD;
		break;
	    case J3DBuffer.TYPE_DOUBLE:
		floatBufferRefCoords = null;
		doubleBufferRefCoords =
		    (DoubleBufferWrapper)coords.getBufferImpl();
		vertexType |= PD;
		vertexType &= ~PF;
		break;
	    default:
		break;
	    }
	}

	// need not call setupMirrorVertexPointer() since
	// we are not going to set mirror in NIO buffer case
	// XXXX: if we need to mix java array with buffer,
	//        we may need to consider setupMirrorVertexPointer()

	geomLock.unLock();
	
	if (!inUpdater && source != null) {
	    if (source.isLive()) {
		processCoordsChanged((coords == null));
		sendDataChangedMessage(true);
	    } else {
		boundsDirty = true;
	    }
	}
	
    }


    J3DBuffer getCoordRefBuffer() {
	return coordRefBuffer;
    }


    void setCoordRefFloat(float[] coords) {
	
	// If non-null coordinate and vertType is either  defined
	// to be something other than PF, then issue an error
	if (coords != null) {
	    if ((vertexType & VERTEX_DEFINED) != 0 &&
		(vertexType & VERTEX_DEFINED) != PF) {
		throw new IllegalArgumentException(J3dI18N.getString("GeometryArray98"));
	    }


	    if (this instanceof IndexedGeometryArrayRetained) {
		IndexedGeometryArrayRetained idx = (IndexedGeometryArrayRetained)this;

		if (3 * idx.maxCoordIndex >= coords.length) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray23"));
		} 
	    } else if (coords.length < 3 * (initialCoordIndex+validVertexCount)) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray99"));
	    }
	}

	geomLock.getLock();
	dirtyFlag |= COORDINATE_CHANGED;

	floatRefCoords = coords;
	if (inUpdater || (this instanceof IndexedGeometryArrayRetained &&
			  ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0))) {
	    if (coords == null)
		vertexType &= ~PF;
	    else
		vertexType |= PF;
	}
	else {
	    setupMirrorVertexPointer(PF);
	}
	
	geomLock.unLock();
	
	if (!inUpdater && source != null) {
	    if (source.isLive()) {
		processCoordsChanged(coords == null);
		sendDataChangedMessage(true);
	    } else {
		boundsDirty = true;
	    }
	}
    }


    float[] getCoordRefFloat() {
	return floatRefCoords;
    }


    void setCoordRefDouble(double[] coords) {

	if (coords != null) {
	    if ((vertexType & VERTEX_DEFINED) != 0 &&
		(vertexType & VERTEX_DEFINED) != PD) {
		throw new IllegalArgumentException(J3dI18N.getString("GeometryArray98"));
	    }

	    if (this instanceof IndexedGeometryArrayRetained) {
		IndexedGeometryArrayRetained idx = (IndexedGeometryArrayRetained)this;
		if (3 * idx.maxCoordIndex >= coords.length) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray23"));
		} 
	    } else if (coords.length < 3 * (initialCoordIndex+validVertexCount)) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray99"));
	    }
	}


	geomLock.getLock();
	dirtyFlag |= COORDINATE_CHANGED;
	doubleRefCoords = coords;
	if (inUpdater || (this instanceof IndexedGeometryArrayRetained &&
			  ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0))) {
	    if (coords == null)
		vertexType &= ~PD;
	    else
		vertexType |= PD;
	}
	else {
	    setupMirrorVertexPointer(PD);
	}
	geomLock.unLock();
	
	if (!inUpdater && source != null) {
	    if (source.isLive()) {
		processCoordsChanged(coords == null);    
		sendDataChangedMessage(true);	
	    } else {
		boundsDirty = true;
	    }
	}
    }

    double[] getCoordRefDouble() {
	return doubleRefCoords;
    }

    void setCoordRef3f(Point3f[] coords) {

	if (coords != null) {
	    if ((vertexType & VERTEX_DEFINED) != 0 &&
		(vertexType & VERTEX_DEFINED) != P3F) {
		throw new IllegalArgumentException(J3dI18N.getString("GeometryArray98"));
	    }

	    if (this instanceof IndexedGeometryArrayRetained) {
		IndexedGeometryArrayRetained idx = (IndexedGeometryArrayRetained)this;

		if (idx.maxCoordIndex >= coords.length) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray23"));
		} 
	    } else if (coords.length < (initialCoordIndex+validVertexCount) ) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray99"));
	    }
	}
	
	geomLock.getLock();
	dirtyFlag |= COORDINATE_CHANGED;
	p3fRefCoords = coords;
	if (inUpdater || (this instanceof IndexedGeometryArrayRetained &&
			  ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0))) {
	    if (coords == null)
		vertexType &= ~P3F;
	    else
		vertexType |= P3F;
	}
	else {
	    setupMirrorVertexPointer(P3F);
	}
	geomLock.unLock();
	
	if (!inUpdater && source != null) {
	    if (source.isLive()) {
		processCoordsChanged(coords == null);
		sendDataChangedMessage(true);
	    } else {
		boundsDirty = true;
	    }
	}
    }

    Point3f[] getCoordRef3f() {
	return p3fRefCoords;

    }

    void setCoordRef3d(Point3d[] coords) {

	if (coords != null) {
	    if ((vertexType & VERTEX_DEFINED) != 0 &&
		(vertexType & VERTEX_DEFINED) != P3D) {
		throw new IllegalArgumentException(J3dI18N.getString("GeometryArray98"));
	    }

	    if (this instanceof IndexedGeometryArrayRetained) {
		IndexedGeometryArrayRetained idx = (IndexedGeometryArrayRetained)this;

		if (idx.maxCoordIndex >= coords.length) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray23"));
		} 
	    } else if (coords.length <  (initialCoordIndex+validVertexCount) ) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray99"));
	    }
	}
	geomLock.getLock();
	dirtyFlag |= COORDINATE_CHANGED;
	p3dRefCoords = coords;
	if (inUpdater || (this instanceof IndexedGeometryArrayRetained &&
			  ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0))) {
	    if (coords == null)
		vertexType &= ~P3D;
	    else
		vertexType |= P3D;	
	} else {
	    setupMirrorVertexPointer(P3D);
	}
	geomLock.unLock();
	
	if (!inUpdater && source != null) {
	    if (source.isLive()) {
		processCoordsChanged(coords == null);    
		sendDataChangedMessage(true);
	    } else {
		boundsDirty = true;
	    }
	}
    }

    Point3d[] getCoordRef3d() {
	return p3dRefCoords;
    }

    void setColorRefFloat(float[] colors) {

	if (colors != null) {
	    if ((vertexType & COLOR_DEFINED) != 0 &&
		(vertexType & COLOR_DEFINED) != CF) {
		throw new IllegalArgumentException(J3dI18N.getString("GeometryArray98"));
	    }

	    if ((vertexFormat & GeometryArray.COLOR) == 0) {
		throw new IllegalStateException(J3dI18N.getString("GeometryArray123"));
	    }

	    if (this instanceof IndexedGeometryArrayRetained) {
		IndexedGeometryArrayRetained idx = (IndexedGeometryArrayRetained)this;

		if (getColorStride() * idx.maxColorIndex >= colors.length) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray24"));
		}
	    } else  if (colors.length < getColorStride() * (initialColorIndex+ validVertexCount) ) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
	    }
	}

	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;
	floatRefColors = colors;
	if (inUpdater || (this instanceof IndexedGeometryArrayRetained &&
			  ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0))) {
	    if (colors == null)
		vertexType &= ~CF;
	    else
		vertexType |= CF;
	}
	else {
	    setupMirrorColorPointer(CF, false);
	}
	geomLock.unLock();
	if (!inUpdater && source != null && source.isLive()) {
	    sendDataChangedMessage(false);
	}

    }

    float[] getColorRefFloat() {
	return floatRefColors;
    }

    
    // set the color with nio buffer
    void setColorRefBuffer(J3DBuffer colors) {
	if (colors != null) {
	    switch(colors.getBufferType()) {
	    case J3DBuffer.TYPE_FLOAT:
		assert ((FloatBufferWrapper)colors.getBufferImpl()).isDirect();
		break;
	    case J3DBuffer.TYPE_BYTE:
		assert ((ByteBufferWrapper)colors.getBufferImpl()).isDirect();
		break;
	    case J3DBuffer.TYPE_NULL:
		throw new IllegalArgumentException(J3dI18N.getString("GeometryArray115"));

	    default:
		throw new IllegalArgumentException(J3dI18N.getString("GeometryArray116"));
	    }

	    if ((vertexFormat & GeometryArray.COLOR) == 0) {
		throw new IllegalStateException(J3dI18N.getString("GeometryArray123"));
	    }

	    if (this instanceof IndexedGeometryArrayRetained) {
		IndexedGeometryArrayRetained idx = (IndexedGeometryArrayRetained)this;

		if (getColorStride() * idx.maxColorIndex >= colors.getBufferImpl().limit()) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray24"));
		}
	    } else if (colors.getBufferImpl().limit() < 
		       getColorStride() * (initialColorIndex+validVertexCount)) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
	    }
	}

	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;
	colorRefBuffer = colors;
	if(colors == null) {
	    floatBufferRefColors = null;
	    byteBufferRefColors = null;
	} else {
	    switch (colors.getBufferType()) {
	    case J3DBuffer.TYPE_FLOAT:
		floatBufferRefColors = (FloatBufferWrapper)colors.getBufferImpl();
		byteBufferRefColors = null; 
		break;

	    case J3DBuffer.TYPE_BYTE:
		byteBufferRefColors = (ByteBufferWrapper)colors.getBufferImpl();
		floatBufferRefColors = null; 
		break;
	    default:
		break;
	    }
	}
	if (inUpdater || (this instanceof IndexedGeometryArrayRetained &&
			  ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0))) {
	    if(colors == null) {
		vertexType &= ~CF;
		vertexType &= ~CUB;
	    } else {
		switch (colors.getBufferType()) {
		case J3DBuffer.TYPE_FLOAT:
		    vertexType |= CF;
		    vertexType &= ~CUB;
		    break;

		case J3DBuffer.TYPE_BYTE:
		    vertexType |= CUB;
		    vertexType &= ~CF;
		    break;
		default:
		    break;
		}
	    }
	}
	else {
	    setupMirrorColorPointer(CF|CUB, false);
	}
	geomLock.unLock();
	if (!inUpdater && source != null && source.isLive()) {
	    sendDataChangedMessage(false);
	}
    }

    // return the color data in nio buffer format
    J3DBuffer getColorRefBuffer() {
	return colorRefBuffer;
    }
    
    void setColorRefByte(byte[] colors) {

	if (colors != null) {
	    if ((vertexType & COLOR_DEFINED) != 0 &&
		(vertexType & COLOR_DEFINED) != CUB) {
		throw new IllegalArgumentException(J3dI18N.getString("GeometryArray98"));
	    }

	    if ((vertexFormat & GeometryArray.COLOR) == 0) {
		throw new IllegalStateException(J3dI18N.getString("GeometryArray123"));
	    }

	    if (this instanceof IndexedGeometryArrayRetained) {
		IndexedGeometryArrayRetained idx = (IndexedGeometryArrayRetained)this;

		if (getColorStride() * idx.maxColorIndex >= colors.length) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray24"));
		}
	    } else if (colors.length < getColorStride() * (initialColorIndex + validVertexCount)) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
	    }
	}

	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;
	byteRefColors = colors;
	if (inUpdater || (this instanceof IndexedGeometryArrayRetained &&
			  ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0))) {
	    if (colors == null)
		vertexType &= ~CUB;
	    else
		vertexType |= CUB;
	}
	else {
	    setupMirrorColorPointer(CUB, false);
	}
	geomLock.unLock();
	if (!inUpdater && source != null && source.isLive()) {
	    sendDataChangedMessage(false);
	}

    }
    
    byte[] getColorRefByte() {
	return byteRefColors;
    }

    void setColorRef3f(Color3f[] colors) {

	if (colors != null) {
	    if ((vertexType & COLOR_DEFINED) != 0 &&
		(vertexType & COLOR_DEFINED) != C3F) {
		throw new IllegalArgumentException(J3dI18N.getString("GeometryArray98"));
	    }

	    if ((vertexFormat & GeometryArray.COLOR_3) == 0) {
		throw new IllegalStateException(J3dI18N.getString("GeometryArray92"));
	    }

	    if (this instanceof IndexedGeometryArrayRetained) {
		IndexedGeometryArrayRetained idx = (IndexedGeometryArrayRetained)this;
		if (idx.maxColorIndex >= colors.length) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray24"));
		}
	    } else if (colors.length < (initialColorIndex + validVertexCount) ) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
	    } 
	}


	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;
	c3fRefColors = colors;
	if (inUpdater || (this instanceof IndexedGeometryArrayRetained &&
			  ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0))) {
	    if (colors == null)
		vertexType &= ~C3F;
	    else
		vertexType |= C3F;
	}
	else {
	    setupMirrorColorPointer(C3F, false);
	}
	geomLock.unLock();
	if (!inUpdater && source != null && source.isLive()) {
	    sendDataChangedMessage(false);
	}

    }
    
    Color3f[] getColorRef3f() {
	return c3fRefColors;
    }


    void setColorRef4f(Color4f[] colors) {

	if (colors != null) {
	    if ((vertexType & COLOR_DEFINED) != 0 &&
		(vertexType & COLOR_DEFINED) != C4F) {
		throw new IllegalArgumentException(J3dI18N.getString("GeometryArray98"));
	    }
	    if ((vertexFormat & GeometryArray.COLOR_4) == 0) {
		throw new IllegalStateException(J3dI18N.getString("GeometryArray93"));
	    }

	    if (this instanceof IndexedGeometryArrayRetained) {
		IndexedGeometryArrayRetained idx = (IndexedGeometryArrayRetained)this;
		if (idx.maxColorIndex >= colors.length) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray24"));
		}
	    } else if (colors.length < (initialColorIndex + validVertexCount) ) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
	    }
	}

	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;
	c4fRefColors = colors;
	if (inUpdater || (this instanceof IndexedGeometryArrayRetained &&
			  ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0))) {
	    if (colors == null)
		vertexType &= ~C4F;
	    else
		vertexType |= C4F;
	}
	else {
	    setupMirrorColorPointer(C4F, false);
	}
	geomLock.unLock();
	if (!inUpdater && source != null && source.isLive()) {
	    sendDataChangedMessage(false);
	}
    }
    
    Color4f[] getColorRef4f() {
	return c4fRefColors;
    }


    void setColorRef3b(Color3b[] colors) {

	if (colors != null) {
	
	    if ((vertexType & COLOR_DEFINED) != 0 &&
		(vertexType & COLOR_DEFINED) != C3UB) {
		throw new IllegalArgumentException(J3dI18N.getString("GeometryArray98"));
	    }
	    
	    if ((vertexFormat & GeometryArray.COLOR_3) == 0) {
		throw new IllegalStateException(J3dI18N.getString("GeometryArray92"));
	    }

	    if (this instanceof IndexedGeometryArrayRetained) {
		IndexedGeometryArrayRetained idx = (IndexedGeometryArrayRetained)this;

		if (idx.maxColorIndex >= colors.length) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray24"));
		}
	    } else if (colors.length < (initialColorIndex + validVertexCount)) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
	    }
	}
	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;
	c3bRefColors = colors;	    
	if (inUpdater || (this instanceof IndexedGeometryArrayRetained &&
			  ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0))) {
	    if (colors == null)
		vertexType &= ~C3UB;
	    else
		vertexType |= C3UB;
	}
	else {
	    setupMirrorColorPointer(C3UB, false);
	}
	
	geomLock.unLock();
	if (!inUpdater && source != null && source.isLive()) {
	    sendDataChangedMessage(false);
	}
    }

    
    Color3b[] getColorRef3b() {
	return c3bRefColors;
    }        

    void setColorRef4b(Color4b[] colors) {

	if (colors != null) {
	    if ((vertexType & COLOR_DEFINED) != 0 &&
		(vertexType & COLOR_DEFINED) != C4UB) {
		throw new IllegalArgumentException(J3dI18N.getString("GeometryArray98"));
	    }
	    
	    if ((vertexFormat & GeometryArray.COLOR_4) == 0) {
		throw new IllegalStateException(J3dI18N.getString("GeometryArray93"));
	    }

	    if (this instanceof IndexedGeometryArrayRetained) {
		IndexedGeometryArrayRetained idx = (IndexedGeometryArrayRetained) this;

		if (idx.maxColorIndex >= colors.length) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray24"));
		}
	    } else if (colors.length < (initialColorIndex + validVertexCount) ) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
	    }
	}
	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;
	c4bRefColors = colors;
	if (inUpdater || (this instanceof IndexedGeometryArrayRetained &&
			  ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0))) {
	    if (colors == null)
		vertexType &= ~C4UB;
	    else
		vertexType |= C4UB;
	}
	else {
	    setupMirrorColorPointer(C4UB, false);
	}
	geomLock.unLock();
	if (!inUpdater && source != null && source.isLive()) {
	    sendDataChangedMessage(false);
	}
    }
    
    
    Color4b[] getColorRef4b() {
	return c4bRefColors;
    }

    void setNormalRefFloat(float[] normals) {

	if (normals != null) {
	    if ((vertexType & NORMAL_DEFINED) != 0 &&
		(vertexType & NORMAL_DEFINED) != NF) {
		throw new IllegalArgumentException(J3dI18N.getString("GeometryArray98"));
	    }

	    if ((vertexFormat & GeometryArray.NORMALS) == 0) {
		throw new IllegalStateException(J3dI18N.getString("GeometryArray122"));
	    }

	    if (this instanceof IndexedGeometryArrayRetained) {
		IndexedGeometryArrayRetained idx = (IndexedGeometryArrayRetained)this;
		
		if (idx.maxNormalIndex*3 >= normals.length) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray26"));
		}
	    } else if (normals.length < 3 * (initialNormalIndex + validVertexCount )) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray111"));
	    }
	}
	geomLock.getLock();
	dirtyFlag |= NORMAL_CHANGED;
	floatRefNormals = normals;
	if (inUpdater || (this instanceof IndexedGeometryArrayRetained &&
			  ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0))) {
	    if (normals == null)
		vertexType &= ~NF;
	    else
		vertexType |= NF;
	}
	else {
	    setupMirrorNormalPointer(NF);
	}
	geomLock.unLock();

	if (!inUpdater && source != null && source.isLive()) {
		sendDataChangedMessage(false);
	}

    }
    
    float[] getNormalRefFloat() {
	return floatRefNormals;
    }

    // setup the normal with nio buffer
    void setNormalRefBuffer(J3DBuffer normals) {

	FloatBufferWrapper bufferImpl = null;

	if (normals != null) {
	    if(normals.getBufferType() != J3DBuffer.TYPE_FLOAT)
		throw new IllegalArgumentException(J3dI18N.getString("GeometryArray116"));
	    
	    bufferImpl = (FloatBufferWrapper)normals.getBufferImpl();

	    assert bufferImpl.isDirect();

	    if ((vertexFormat & GeometryArray.NORMALS) == 0) {
		throw new IllegalStateException(J3dI18N.getString("GeometryArray122"));
	    }

	    if (this instanceof IndexedGeometryArrayRetained) {
		IndexedGeometryArrayRetained idx = (IndexedGeometryArrayRetained)this;
		if (idx.maxNormalIndex * 3 >= 
		    ((FloatBufferWrapper)normals.getBufferImpl()).limit()) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray26"));
		}
	    } else if (bufferImpl.limit() < 3 * (initialNormalIndex + validVertexCount )) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray111"));
	    }
	}
	geomLock.getLock();
	dirtyFlag |= NORMAL_CHANGED;
	normalRefBuffer = normals;
	
	if (normals == null) {
	    vertexType &= ~NF;
	    floatBufferRefNormals = null; 
	}
	else {
	    vertexType |= NF;
	    floatBufferRefNormals = bufferImpl;
	}
	geomLock.unLock();

	if (!inUpdater && source != null && source.isLive()) {
		sendDataChangedMessage(false);
	}
    }

    J3DBuffer getNormalRefBuffer() {
	return normalRefBuffer;
    }

    void setNormalRef3f(Vector3f[] normals) {

	if (normals != null) {
	    if ((vertexType & NORMAL_DEFINED) != 0 &&
		(vertexType & NORMAL_DEFINED) != N3F) {
		throw new IllegalArgumentException(J3dI18N.getString("GeometryArray98"));
	    }

	    if ((vertexFormat & GeometryArray.NORMALS) == 0) {
		throw new IllegalStateException(J3dI18N.getString("GeometryArray122"));
	    }

	    if (this instanceof IndexedGeometryArrayRetained) {
		IndexedGeometryArrayRetained idx = (IndexedGeometryArrayRetained)this;
		if (idx.maxNormalIndex >= normals.length) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray26"));
		}
	    } else if (normals.length < (initialNormalIndex + validVertexCount) ) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray111"));
	    }
	}
	geomLock.getLock();
	dirtyFlag |= NORMAL_CHANGED;
	v3fRefNormals = normals;
	if (inUpdater || (this instanceof IndexedGeometryArrayRetained &&
			  ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0))) {
	    if (normals == null)
		vertexType &= ~N3F;
	    else
		vertexType |= N3F;
	}
	else {
	    setupMirrorNormalPointer(N3F);
	}
	geomLock.unLock();
	if (!inUpdater && source != null && source.isLive()) {
	    sendDataChangedMessage(false);
	}
    }
    
    Vector3f[] getNormalRef3f() {
	return v3fRefNormals;
    }        

    final int getColorStride() {
	return ((vertexFormat & GeometryArray.WITH_ALPHA) != 0 ? 4 : 3);
    }

    final int getTexStride() {
	if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
	    return 2;
	} 
	if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_3) != 0) { 
	    return 3;
	} 
	if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_4) != 0) {
	    return 4;
	}

	throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray121"));
    }

    void setTexCoordRefFloat(int texCoordSet, float[] texCoords) {

        if (texCoordType != 0 && texCoordType != TF) {
            if (texCoords != null) {
                throw new IllegalArgumentException(
                        J3dI18N.getString("GeometryArray98"));
            }
            return;
        }

        if (texCoords != null) {

            int ts = getTexStride();

	    if (this instanceof IndexedGeometryArrayRetained) {
		IndexedGeometryArrayRetained idx = (IndexedGeometryArrayRetained)this;

		if (idx.maxTexCoordIndices[texCoordSet]*ts >= texCoords.length) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray25"));
		}
	    } else if (texCoords.length < ts*(initialTexCoordIndex[texCoordSet]+validVertexCount)) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray113"));
	    }
	}

	geomLock.getLock();
	dirtyFlag |= TEXTURE_CHANGED;
	refTexCoords[texCoordSet] = texCoords;
	if (inUpdater || (this instanceof IndexedGeometryArrayRetained &&
			  ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0))) {
	    texCoordType = TF;
            validateTexCoordPointerType();
	}
	else {
	    setupMirrorTexCoordPointer(texCoordSet, TF);
	}
	geomLock.unLock();
	if (!inUpdater && source != null && source.isLive()) {
	    sendDataChangedMessage(false);
	}
    }


    float[] getTexCoordRefFloat(int texCoordSet) {
	return ((float[])refTexCoords[texCoordSet]);
    }

    // set the tex coord with nio buffer
    void setTexCoordRefBuffer(int texCoordSet, J3DBuffer texCoords) {

	FloatBufferWrapper bufferImpl = null;

	if (texCoords != null) {
	    if(texCoords.getBufferType() != J3DBuffer.TYPE_FLOAT)
		throw new IllegalArgumentException(J3dI18N.getString("GeometryArray116"));

	    bufferImpl = (FloatBufferWrapper)texCoords.getBufferImpl();
	    int bufferSize = bufferImpl.limit();

	    assert bufferImpl.isDirect();

	    int ts = getTexStride();

	    if (this instanceof IndexedGeometryArrayRetained) {
		IndexedGeometryArrayRetained idx = (IndexedGeometryArrayRetained)this;
		if (idx.maxTexCoordIndices[texCoordSet] * ts >= bufferSize) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray25"));
		}
	    } else if (bufferSize < ts*(initialTexCoordIndex[texCoordSet] + validVertexCount)) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray113"));
	    }
	}

	geomLock.getLock();
	dirtyFlag |= TEXTURE_CHANGED;
	// refTexCoordsBuffer contains J3DBuffer object for tex coord
	refTexCoordsBuffer[texCoordSet] = texCoords;
	if (texCoords == null) {
	    refTexCoords[texCoordSet] = null;
	}
	else {
	    // refTexCoords contains NIOBuffer object for tex coord
	    refTexCoords[texCoordSet] = bufferImpl.getBufferAsObject();
	}
        texCoordType = TF;
        validateTexCoordPointerType();
	geomLock.unLock();
	if (!inUpdater && source != null && source.isLive()) {
	    sendDataChangedMessage(false);
	}
    }

    J3DBuffer getTexCoordRefBuffer(int texCoordSet) {
	return (J3DBuffer)(refTexCoordsBuffer[texCoordSet]);
    }

    void setTexCoordRef2f(int texCoordSet, TexCoord2f[] texCoords) {

        if (texCoordType != 0 && texCoordType != T2F) {
            if (texCoords != null) {
                throw new IllegalArgumentException(
                        J3dI18N.getString("GeometryArray98"));
            }
            return;
        }

        if (texCoords != null) {
	    if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_2) == 0) {
		throw new IllegalStateException(
				J3dI18N.getString("GeometryArray94"));
	    }

	    if (this instanceof IndexedGeometryArrayRetained) {
		IndexedGeometryArrayRetained idx = (IndexedGeometryArrayRetained)this;

		if (idx.maxTexCoordIndices[texCoordSet] >= texCoords.length) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray25"));
		}
	    } else if (texCoords.length < (initialTexCoordIndex[texCoordSet] + validVertexCount) ) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray113"));
	    }

	}

	geomLock.getLock();
	dirtyFlag |= TEXTURE_CHANGED;
	refTexCoords[texCoordSet] = texCoords;	
	if (inUpdater || (this instanceof IndexedGeometryArrayRetained &&
			  ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0))) {
	    texCoordType = T2F;
            validateTexCoordPointerType();
	}
	else {
	    setupMirrorTexCoordPointer(texCoordSet, T2F);
	}
	geomLock.unLock();
	if (!inUpdater && source != null && source.isLive()) {
		sendDataChangedMessage(false);
	}
    }

    
    TexCoord2f[] getTexCoordRef2f(int texCoordSet) {
	if (refTexCoords != null && refTexCoords[texCoordSet] != null &&
		refTexCoords[texCoordSet] instanceof TexCoord2f[]) { 
	    return ((TexCoord2f[])refTexCoords[texCoordSet]);
	} else {
	    return null;
	}
    }


    void setTexCoordRef3f(int texCoordSet, TexCoord3f[] texCoords) {

        if (texCoordType != 0 && texCoordType != T3F) {
            if (texCoords != null) {
                throw new IllegalArgumentException(
                        J3dI18N.getString("GeometryArray98"));
            }
            return;
        }

	if (texCoords != null) {

	    if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_3) == 0) {
		throw new IllegalStateException(
				J3dI18N.getString("GeometryArray95"));
	    }

	    if (this instanceof IndexedGeometryArrayRetained) {
		IndexedGeometryArrayRetained idx = (IndexedGeometryArrayRetained)this;

		if (idx.maxTexCoordIndices[texCoordSet] >= texCoords.length) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray25"));
		}

	    } else if (texCoords.length < (initialTexCoordIndex[texCoordSet] + validVertexCount) ) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray113"));
	    }

	}

	geomLock.getLock();
	dirtyFlag |= TEXTURE_CHANGED;
	refTexCoords[texCoordSet] = texCoords;
	if (inUpdater || (this instanceof IndexedGeometryArrayRetained &&
			  ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0))) {
	    texCoordType = T3F;
            validateTexCoordPointerType();
	}
	else {
	    setupMirrorTexCoordPointer(texCoordSet, T3F);
	}
	geomLock.unLock();
	if (!inUpdater && source != null && source.isLive()) {
	    sendDataChangedMessage(false);
	}
    }

    
    TexCoord3f[] getTexCoordRef3f(int texCoordSet) {
	if (refTexCoords != null && refTexCoords[texCoordSet] != null &&
		refTexCoords[texCoordSet] instanceof TexCoord3f[]) { 
	    return ((TexCoord3f[])refTexCoords[texCoordSet]);
	} else {
	    return null;
	}
    }    


    /**
     * Sets the float vertex attribute array reference for the
     * specified vertex attribute number to the specified array.
     */
    void setVertexAttrRefFloat(int vertexAttrNum, float[] vertexAttrs) {

        // XXXX: Add the following test if we ever add double-precision types
        /*
        if (vertexAttrType != 0 && vertexAttrType != AF) {
            if (vertexAttrs != null) {
                // XXXX: new exception string
                throw new IllegalArgumentException(
                        J3dI18N.getString("GeometryArray98-XXX"));
            }
            return;
        }
        */

        if (vertexAttrs != null) {
            int sz = vertexAttrSizes[vertexAttrNum];

            if (this instanceof IndexedGeometryArrayRetained) {
                IndexedGeometryArrayRetained idx = (IndexedGeometryArrayRetained)this;

		if (sz*idx.maxVertexAttrIndices[vertexAttrNum] >= vertexAttrs.length) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray30"));
		}

	    } else if (vertexAttrs.length < sz*(initialVertexAttrIndex[vertexAttrNum] + validVertexCount) ) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray129"));
	    }
	}

	geomLock.getLock();
	dirtyFlag |= VATTR_CHANGED;
	floatRefVertexAttrs[vertexAttrNum] = vertexAttrs;
	if (inUpdater || (this instanceof IndexedGeometryArrayRetained &&
			  ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0))) {
	    vertexAttrType = AF;
            validateVertexAttrPointerType();
	}
	else {
	    setupMirrorVertexAttrPointer(vertexAttrNum, AF);
	}
	geomLock.unLock();
	if (!inUpdater && source != null && source.isLive()) {
	    sendDataChangedMessage(false);
	}
    }

    /**
     * Gets the float vertex attribute array reference for the specified
     * vertex attribute number.
     */
    float[] getVertexAttrRefFloat(int vertexAttrNum) {
        return floatRefVertexAttrs[vertexAttrNum];
    }


    /**
     * Sets the vertex attribute buffer reference for the specified
     * vertex attribute number to the specified buffer object.
     */
    void setVertexAttrRefBuffer(int vertexAttrNum, J3DBuffer vertexAttrs) {

	FloatBufferWrapper bufferImpl = null;

	if (vertexAttrs != null) {
	    if(vertexAttrs.getBufferType() != J3DBuffer.TYPE_FLOAT)
		throw new IllegalArgumentException(J3dI18N.getString("GeometryArray116"));

	    bufferImpl = (FloatBufferWrapper)vertexAttrs.getBufferImpl();
	    int bufferSize = bufferImpl.limit();

	    assert bufferImpl.isDirect();

	    int sz = vertexAttrSizes[vertexAttrNum];

            if (this instanceof IndexedGeometryArrayRetained) {
		IndexedGeometryArrayRetained idx = (IndexedGeometryArrayRetained)this;

		if (idx.maxVertexAttrIndices[vertexAttrNum] * sz >= bufferSize) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray30"));
		}
	    } else if (bufferSize < sz*(initialVertexAttrIndex[vertexAttrNum] + validVertexCount)) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray129"));
            }
        }

        geomLock.getLock();
        dirtyFlag |= VATTR_CHANGED;
        vertexAttrsRefBuffer[vertexAttrNum] = vertexAttrs;
        if (vertexAttrs == null) {
            floatBufferRefVertexAttrs[vertexAttrNum] = null;
            nioFloatBufferRefVertexAttrs[vertexAttrNum] = null;
        }
        else {
            floatBufferRefVertexAttrs[vertexAttrNum] = bufferImpl;
            nioFloatBufferRefVertexAttrs[vertexAttrNum] =
                bufferImpl.getBufferAsObject();
        }
        vertexAttrType = AF;
        validateVertexAttrPointerType();
        geomLock.unLock();
        if (!inUpdater && source != null && source.isLive()) {
            sendDataChangedMessage(false);
        }

    }

    /**
     * Gets the vertex attribute array buffer reference for the specified
     * vertex attribute number.
     */
    J3DBuffer getVertexAttrRefBuffer(int vertexAttrNum) {
	return vertexAttrsRefBuffer[vertexAttrNum];
    }


    void setInterleavedVertices(float[] vertexData) {
	if (vertexData != null) {

	    if (this instanceof IndexedGeometryArrayRetained) {
		IndexedGeometryArrayRetained idx = (IndexedGeometryArrayRetained)this;

		if (stride * idx.maxCoordIndex >= vertexData.length) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray23"));
		}

		if ((this.vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
		    for (int i = 0; i < texCoordSetCount; i++) {
			if (stride * idx.maxTexCoordIndices[i] >= vertexData.length) {
			    throw new ArrayIndexOutOfBoundsException(
                                      J3dI18N.getString("IndexedGeometryArray25"));
			}
		    }
		}
		
		if (((this.vertexFormat & GeometryArray.COLOR) != 0) &&
		    (stride * idx.maxColorIndex >= vertexData.length)) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray24"));
		}

		if (((this.vertexFormat & GeometryArray.NORMALS) != 0) &&
		    (stride * idx.maxNormalIndex >= vertexData.length)) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray26"));
		}
	    } else {
		if (vertexData.length < (stride * (initialVertexIndex+validVertexCount)))
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray114"));		
	    }
	}	

	// If the geometry has been rendered transparent, then make a copy
	// of the color pointer with 4f
	geomLock.getLock();
	dirtyFlag |= VERTEX_CHANGED;
	colorChanged = 0xffff;
	interLeavedVertexData = vertexData;
	if (inUpdater || (this instanceof IndexedGeometryArrayRetained &&
			  ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0))) {
	    setupMirrorInterleavedColorPointer(false);
	}
	geomLock.unLock();
	if (!inUpdater && source != null && source.isLive()) {
	    processCoordsChanged(vertexData == null);    
	    sendDataChangedMessage(true);
	}
    }

    // set the interleaved vertex with NIO buffer
    void setInterleavedVertexBuffer(J3DBuffer vertexData) {

	FloatBufferWrapper bufferImpl = null;

	if (vertexData != null ){

	    if (vertexData.getBufferType() != J3DBuffer.TYPE_FLOAT)
		throw new IllegalArgumentException(J3dI18N.getString("GeometryArray116"));
	    
	    bufferImpl = (FloatBufferWrapper)vertexData.getBufferImpl();

            assert bufferImpl.isDirect();

	    int bufferSize = bufferImpl.limit();

	    if (this instanceof IndexedGeometryArrayRetained) {
		IndexedGeometryArrayRetained idx = (IndexedGeometryArrayRetained)this;

		if (stride * idx.maxCoordIndex >= bufferSize) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray23"));
		}

		if ((this.vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
		    for (int i = 0; i < texCoordSetCount; i++) {
			if (stride * idx.maxTexCoordIndices[i] >= bufferSize) {
			    throw new ArrayIndexOutOfBoundsException(
                                  J3dI18N.getString("IndexedGeometryArray25"));
			}
		    }
		}
		
		if (((this.vertexFormat & GeometryArray.COLOR) != 0) &&
		    (stride * idx.maxColorIndex >= bufferSize)) {
		    	throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray24"));
		}

		if (((this.vertexFormat & GeometryArray.NORMALS) != 0) &&
		    (stride * idx.maxNormalIndex >= bufferSize)) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray23"));
		}
	    } else {
		if (bufferSize < (stride * (initialVertexIndex+validVertexCount)))
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray114"));
	    }
	}
	// If the geometry has been rendered transparent, then make a copy
	// of the color pointer with 4f
	geomLock.getLock();
	dirtyFlag |= VERTEX_CHANGED;
	colorChanged = 0xffff;
	interleavedVertexBuffer = vertexData;

	if(vertexData == null) 
	    interleavedFloatBufferImpl = null;
	else
	    interleavedFloatBufferImpl = bufferImpl;
	
	if (inUpdater || (this instanceof IndexedGeometryArrayRetained &&
			  ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0))) {
	    setupMirrorInterleavedColorPointer(false); 
	}
	geomLock.unLock();
	if (!inUpdater && source != null && source.isLive()) {
	    processCoordsChanged(vertexData == null);    
	    sendDataChangedMessage(true);
	}
    }

    float[] getInterleavedVertices() {
	return interLeavedVertexData;
    }

    J3DBuffer getInterleavedVertexBuffer() {
	return interleavedVertexBuffer; 
    }
    
    void setValidVertexCount(int validVertexCount) {

	boolean nullGeo = false;
	if (validVertexCount < 0) {
	    throw new IllegalArgumentException(J3dI18N.getString("GeometryArray110"));
	}

        if ((initialVertexIndex + validVertexCount) > vertexCount) {
            throw new IllegalArgumentException(J3dI18N.getString("GeometryArray100"));
        }

        if ((vertexFormat & GeometryArray.INTERLEAVED) != 0) {
            // Interleaved, by-ref

            // use nio buffer for interleaved data
	    if(( vertexFormat & GeometryArray.USE_NIO_BUFFER) != 0 && interleavedFloatBufferImpl != null){
		if(interleavedFloatBufferImpl.limit() <  stride * (initialVertexIndex + validVertexCount)) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray114"));
		}	
	    }
	    //use java array for interleaved data
	    else if( interLeavedVertexData != null) {
		if(interLeavedVertexData.length < stride * (initialVertexIndex + validVertexCount)) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray114"));
		}
	    }
	    else {
		nullGeo = true;
	    }
	} else if ((vertexFormat & GeometryArray.BY_REFERENCE) != 0) {
            // Non-interleaved, by-ref

            if ((initialCoordIndex + validVertexCount) > vertexCount) {
                throw new IllegalArgumentException(J3dI18N.getString("GeometryArray104"));
            }
            if ((initialColorIndex + validVertexCount) > vertexCount) {
                throw new IllegalArgumentException(J3dI18N.getString("GeometryArray101"));
            }
            if ((initialNormalIndex + validVertexCount) > vertexCount) {
                throw new IllegalArgumentException(J3dI18N.getString("GeometryArray102"));
            }

            if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
                for (int i = 0; i < texCoordSetCount; i++) {
                    if ((initialTexCoordIndex[i] + validVertexCount) > vertexCount) {
                        throw new IllegalArgumentException(J3dI18N.getString(
                                "GeometryArray103"));
                    }
                }
            }

            if ((vertexFormat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
                for (int i = 0; i < vertexAttrCount; i++) {
                    if ((initialVertexAttrIndex[i] + validVertexCount) > vertexCount) {
                        throw new IllegalArgumentException(J3dI18N.getString(
                                "GeometryArray130"));
                    }
                }
            }

            if ((vertexType & GeometryArrayRetained.VERTEX_DEFINED) == 0) {
		nullGeo = true;
            }

	    if (( vertexFormat & GeometryArray.USE_NIO_BUFFER) != 0) {
		// by reference with nio buffer
		switch ((vertexType & GeometryArrayRetained.VERTEX_DEFINED)) {
		case PF:
		    if(floatBufferRefCoords.limit() < 3 * (initialCoordIndex+validVertexCount) ) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray99"));
		    }
		    break;
		case PD:
		    if(doubleBufferRefCoords.limit() < 3 * (initialCoordIndex+validVertexCount) ) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray99"));
		    }
		    break;
		}
		
		switch ((vertexType & COLOR_DEFINED)) {
		case CF:
		    if ((vertexFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_3) {
			if (floatBufferRefColors.limit() < 3 * (initialColorIndex+validVertexCount)) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
			} 
		    }
		    else if ((vertexFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_4) {
			if (floatBufferRefColors.limit() < 4 * (initialColorIndex+validVertexCount)) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
			} 
		    }
		    break;
		case CUB: 
		    if ((vertexFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_3) {
			if (byteBufferRefColors.limit() < 3 * (initialColorIndex + validVertexCount)) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
			}
		    }
		    else if ((vertexFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_4) {
			if (byteBufferRefColors.limit() < 4 * (initialColorIndex + validVertexCount) ) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
			}
		    }
		    break;
		}
		switch ((vertexType & GeometryArrayRetained.TEXCOORD_DEFINED)) {
		case TF:
		    FloatBufferWrapper texBuffer;
		    for (int i = 0; i < texCoordSetCount; i++) {
			texBuffer = (FloatBufferWrapper)(((J3DBuffer)refTexCoordsBuffer[i]).getBufferImpl());
			if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
			    if (texBuffer.limit() <  2 * (initialTexCoordIndex[i] + validVertexCount) ) {
				throw new ArrayIndexOutOfBoundsException(
									 J3dI18N.getString("GeometryArray113"));
			    }
			} else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_3) != 0) {
			    if (texBuffer.limit() < 3 * (initialTexCoordIndex[i] + validVertexCount) ) {
				throw new ArrayIndexOutOfBoundsException(
									 J3dI18N.getString("GeometryArray113"));
			    }
			} else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_4) != 0) {
			    if (texBuffer.limit() < 4 * (initialTexCoordIndex[i] + validVertexCount)) {
				throw new ArrayIndexOutOfBoundsException(
									 J3dI18N.getString("GeometryArray113"));
			    }
			}
		    }
		    break;
		}
		switch ((vertexType & GeometryArrayRetained.NORMAL_DEFINED)) {
		case NF: 
		    if (floatBufferRefNormals.limit() < 3 * (initialNormalIndex + validVertexCount )) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray111"));
		    }
		    break;
		}
		switch ((vertexType & GeometryArrayRetained.VATTR_DEFINED)) {
		case AF:
                    for (int i = 0; i < vertexAttrCount; i++) {
                        int sz = vertexAttrSizes[i];
                        if (floatBufferRefVertexAttrs[i].limit() <
                                (sz * (initialVertexAttrIndex[i] + validVertexCount)) ) {
                            throw new ArrayIndexOutOfBoundsException(
                                    J3dI18N.getString("GeometryArray129"));
                        }
                    }
		    break;
		}
	    }
	    // By reference with java array
	    else { 
		switch ((vertexType & GeometryArrayRetained.VERTEX_DEFINED)) {
		case PF: 
		    if (floatRefCoords.length < 3 * (initialCoordIndex+validVertexCount)) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray99"));
		    }
		    break;
		case PD:
		    if (doubleRefCoords.length < 3 * (initialCoordIndex+validVertexCount)) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray99"));
		    }
		    break;
		case P3F:
		    if (p3fRefCoords.length < (initialCoordIndex+validVertexCount) ) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray99"));
		    }
		    break;
		case P3D:
		    if (p3dRefCoords.length <  (initialCoordIndex+validVertexCount) ) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray99"));
		    }
		    break;
		}
		switch ((vertexType & COLOR_DEFINED)) {
		case CF:
		    if ((vertexFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_3) {
			if (floatRefColors.length < 3 * (initialColorIndex+validVertexCount)) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
			}
		    }
		    else if ((vertexFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_4) {
			if (floatRefColors.length < 4 * (initialColorIndex+ validVertexCount) ) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
			}
		    }
		    break;
		case CUB: 
		    if ((vertexFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_3) {
			if (byteRefColors.length < 3 * (initialColorIndex + validVertexCount)) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
			}
		    }
		    else if ((vertexFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_4) {
			if (byteRefColors.length < 4 * (initialColorIndex + validVertexCount) ) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
			}
		    }
		    break;
		case C3F: 
		    if (c3fRefColors.length < (initialColorIndex + validVertexCount) ) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
		    }
		    break;
		case C4F: 
		    if (c4fRefColors.length < (initialColorIndex + validVertexCount) ) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
		    }
		    break;
		case C3UB: 
		    if (c3bRefColors.length < (initialColorIndex + validVertexCount)) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
		    }
		    break;
		case C4UB: 
		    if (c4bRefColors.length < (initialColorIndex + validVertexCount) ) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
		    }
		    break;
		}
		switch ((vertexType & GeometryArrayRetained.TEXCOORD_DEFINED)) {
		case TF:
		    for (int i = 0; i < texCoordSetCount; i++) {
			if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
			    if (((float[])refTexCoords[i]).length < 2 * (initialTexCoordIndex[i] + validVertexCount) ) {
				throw new ArrayIndexOutOfBoundsException(
									 J3dI18N.getString("GeometryArray113"));
			    }
			} else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_3) != 0) {
			    if (((float[])refTexCoords[i]).length < 3 * (initialTexCoordIndex[i] + validVertexCount) ) {
				throw new ArrayIndexOutOfBoundsException(
									 J3dI18N.getString("GeometryArray113"));
			    } else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_4) != 0) {
				if (((float[])refTexCoords[i]).length < 4 * (initialTexCoordIndex[i] + validVertexCount)) {
				    throw new ArrayIndexOutOfBoundsException(
									     J3dI18N.getString("GeometryArray113"));
				}
			    }
			}
		    }
		    break;
		case T2F:
		    for (int i = 0; i < texCoordSetCount; i++) {
			if (((TexCoord2f[])refTexCoords[i]).length < (initialTexCoordIndex[i] + validVertexCount) ) {
			    throw new ArrayIndexOutOfBoundsException(
								     J3dI18N.getString("GeometryArray113"));
			}
		    }
		    break;
		case T3F: 
		    for (int i = 0; i < texCoordSetCount; i++) {
			if (((TexCoord3f[])refTexCoords[i]).length < (initialTexCoordIndex[i] + validVertexCount) ) {
			    throw new ArrayIndexOutOfBoundsException(
								     J3dI18N.getString("GeometryArray113"));
			}
		    }
		    break;
		}
		switch ((vertexType & GeometryArrayRetained.NORMAL_DEFINED)) {
		case NF: 
		    if (floatRefNormals.length < 3 * (initialNormalIndex + validVertexCount )) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray111"));
		    }
		    break;
		case N3F: 
		    if (v3fRefNormals.length < (initialNormalIndex + validVertexCount) ) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray111"));
		    }
		}
		switch ((vertexType & GeometryArrayRetained.VATTR_DEFINED)) {
		case AF:
                    for (int i = 0; i < vertexAttrCount; i++) {
                        int sz = vertexAttrSizes[i];
                        if (floatRefVertexAttrs[i].length <
                                (sz * (initialVertexAttrIndex[i] + validVertexCount)) ) {
                            throw new ArrayIndexOutOfBoundsException(
                                    J3dI18N.getString("GeometryArray129"));
                        }
                    }
		    break;
		}
	    }
	}

	geomLock.getLock();
	dirtyFlag |= VERTEX_CHANGED;
	this.validVertexCount = validVertexCount;
	geomLock.unLock();
	if (!inUpdater && source != null && source.isLive()) {
	    processCoordsChanged(nullGeo);    
	    sendDataChangedMessage(true);
	}
    }


    int getValidVertexCount() {
	return validVertexCount;
    }

    //Used for interleaved data (array or nio buffer)
    void setInitialVertexIndex(int initialVertexIndex) {
	boolean nullGeo = false;
	
	if ((initialVertexIndex + validVertexCount) > vertexCount) {
	    throw new IllegalArgumentException(J3dI18N.getString("GeometryArray100"));
	}
	 
	if((vertexFormat & GeometryArray.USE_NIO_BUFFER) != 0 && interleavedFloatBufferImpl != null) {
	    if(interleavedFloatBufferImpl.limit() <  stride * (initialVertexIndex + validVertexCount)) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray114"));
	    }
	}
	// interleaved data using java array
	else if(interLeavedVertexData != null) {
		if (interLeavedVertexData.length < stride * (initialVertexIndex + validVertexCount)) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray114"));
	    }
	}
	else {
	    nullGeo = (vertexFormat & GeometryArray.INTERLEAVED) != 0; // Only for byRef
	}
	geomLock.getLock();
	dirtyFlag |= VERTEX_CHANGED;
	this.initialVertexIndex = initialVertexIndex;
	geomLock.unLock();
	if (!inUpdater&& source != null && source.isLive()) {
	    processCoordsChanged(nullGeo);
	    sendDataChangedMessage(true);
	}
    }

    int getInitialVertexIndex() {
	return initialVertexIndex;
    }

    void setInitialCoordIndex(int initialCoordIndex) {
	if ((initialCoordIndex + validVertexCount) > vertexCount) {
	    throw new IllegalArgumentException(J3dI18N.getString("GeometryArray104"));
	}
	// use NIO buffer 
	if((vertexFormat & GeometryArray.USE_NIO_BUFFER) != 0){
	    switch ((vertexType & GeometryArrayRetained.VERTEX_DEFINED)) {
	    case PF:
		if(floatBufferRefCoords.limit() < (initialCoordIndex+validVertexCount) ) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray99"));
		}
		break;
	    case PD:
		if(doubleBufferRefCoords.limit() < (initialCoordIndex+validVertexCount) ) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray99"));
		}
		break;
	    }
	} else {
	    switch ((vertexType & GeometryArrayRetained.VERTEX_DEFINED)) {
	    case PF: 
		if (floatRefCoords.length < 3 * (initialCoordIndex+validVertexCount)) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray99"));
		}
		break;
	    case PD:
		if (doubleRefCoords.length < 3 * (initialCoordIndex+validVertexCount)) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray99"));
		}
		break;
	    case P3F:
		if (p3fRefCoords.length < (initialCoordIndex+validVertexCount) ) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray99"));
		}
		break;
	    case P3D:
		if (p3dRefCoords.length <  (initialCoordIndex+validVertexCount) ) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray99"));
		}
		break;
	    }
	}
	geomLock.getLock();
	dirtyFlag |= COORDINATE_CHANGED;
	this.initialCoordIndex = initialCoordIndex;
	dirtyFlag |= COORDINATE_CHANGED;
	geomLock.unLock();
	// Send a message, since bounds changed
	if (!inUpdater && source != null && source.isLive()) {
	    processCoordsChanged((vertexType & GeometryArrayRetained.VERTEX_DEFINED) == 0);
	    sendDataChangedMessage(true);
	}
    }

    int getInitialCoordIndex() {
	return initialCoordIndex;
    }

    void setInitialColorIndex(int initialColorIndex) {
	if ((initialColorIndex + validVertexCount) > vertexCount) {
	    throw new IllegalArgumentException(J3dI18N.getString("GeometryArray101"));
	}
	// NIO BUFFER CASE
	if((vertexFormat & GeometryArray.USE_NIO_BUFFER) != 0){
	    switch ((vertexType & COLOR_DEFINED)) {
	    case CF:
		if ((vertexFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_3) {
		    if (floatBufferRefColors.limit() < 3 * (initialColorIndex+validVertexCount)) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
		    } 
		}
		else if ((vertexFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_4) {
		    if (floatBufferRefColors.limit() < 4 * (initialColorIndex+validVertexCount)) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
		    } 
		}
		break;

	    case CUB:
		if ((vertexFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_3) {
		    if (byteBufferRefColors.limit() < 3 * (initialColorIndex + validVertexCount)) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
		    }
		}
		else if ((vertexFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_4) {
		    if (byteBufferRefColors.limit() < 4 * (initialColorIndex + validVertexCount) ) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
		    }
		}
		break;
	    }
	}
	// Java ARRAY CASE
	else {
	    switch ((vertexType & COLOR_DEFINED)) {
	    case CF:
		if ((vertexFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_3) {
		    if (floatRefColors.length < 3 * (initialColorIndex+validVertexCount)) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
		    }
		}
		else if ((vertexFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_4) {
		    if (floatRefColors.length < 4 * (initialColorIndex+ validVertexCount) ) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
		    }
		}
		break;
	    case CUB: 
		if ((vertexFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_3) {
		    if (byteRefColors.length < 3 * (initialColorIndex + validVertexCount)) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
		    }
		}
		else if ((vertexFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_4) {
		    if (byteRefColors.length < 4 * (initialColorIndex + validVertexCount) ) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
		    }
		}
		break;
	    case C3F: 
		if (c3fRefColors.length < (initialColorIndex + validVertexCount) ) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
		}
		break;
	    case C4F: 
		if (c4fRefColors.length < (initialColorIndex + validVertexCount) ) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
		}
		break;
	    case C3UB: 
		if (c3bRefColors.length < (initialColorIndex + validVertexCount)) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
		}
		break;
	    case C4UB: 
		if (c4bRefColors.length < (initialColorIndex + validVertexCount) ) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray112"));
		}
		break;
	    }
	}
	geomLock.getLock();
	dirtyFlag |= COLOR_CHANGED;
	colorChanged = 0xffff;
	this.initialColorIndex = initialColorIndex;
	geomLock.unLock();
	// There is no need to send message for by reference, since we
	// use VA
	
    }

    int getInitialColorIndex() {
	return initialColorIndex;
    }

    void setInitialNormalIndex(int initialNormalIndex) {
	if ((initialNormalIndex + validVertexCount) > vertexCount) {
	    throw new IllegalArgumentException(J3dI18N.getString("GeometryArray102"));
	}
	if((vertexFormat & GeometryArray.USE_NIO_BUFFER) != 0){
	    if((vertexType & NORMAL_DEFINED) == NF){
		if (floatBufferRefNormals.limit() < 3 * (initialNormalIndex + validVertexCount )) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray111"));
		}	
	    }
	} else {
	    switch((vertexType & NORMAL_DEFINED)){
	    case NF: 
		if (floatRefNormals.length < 3 * (initialNormalIndex + validVertexCount )) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray111"));
		}
		break;
	    case N3F: 
		if (v3fRefNormals.length < (initialNormalIndex + validVertexCount) ) {
		    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("GeometryArray111"));
		}
	    }
	}
	geomLock.getLock();
	dirtyFlag |= NORMAL_CHANGED;
	this.initialNormalIndex = initialNormalIndex;
	geomLock.unLock();
	// There is no need to send message for by reference, since we
	// use VA
    }

    int getInitialNormalIndex() {
	return initialNormalIndex;
    }

    /**
     * Sets the initial vertex attribute index for the specified
     * vertex attribute number for this GeometryArray object.
     */
    void setInitialVertexAttrIndex(int vertexAttrNum,
            int initialVertexAttrIndex) {

        if ((initialVertexAttrIndex + validVertexCount) > vertexCount) {
            throw new IllegalArgumentException(J3dI18N.getString("GeometryArray130"));
        }

        int sz = vertexAttrSizes[vertexAttrNum];
        int minLength = sz * (initialVertexAttrIndex + validVertexCount);
        if ((vertexType & VATTR_DEFINED) == AF) {
            if ((vertexFormat & GeometryArray.USE_NIO_BUFFER) != 0) {
                if (floatBufferRefVertexAttrs[vertexAttrNum].limit() < minLength) {
                    throw new ArrayIndexOutOfBoundsException(
                            J3dI18N.getString("GeometryArray129"));
                }
            } else {
                if (floatRefVertexAttrs[vertexAttrNum].length < minLength ) {
                    throw new ArrayIndexOutOfBoundsException(
                            J3dI18N.getString("GeometryArray129"));
                }
            }
        }
        geomLock.getLock();
        dirtyFlag |= VATTR_CHANGED;
        this.initialVertexAttrIndex[vertexAttrNum] = initialVertexAttrIndex;
        geomLock.unLock();
        // There is no need to send message for by reference, since we
        // use VA
    }


    /**
     * Gets the initial vertex attribute index for the specified
     * vertex attribute number for this GeometryArray object.
     */
    int getInitialVertexAttrIndex(int vertexAttrNum) {
        return initialVertexAttrIndex[vertexAttrNum];
    }

    void setInitialTexCoordIndex(int texCoordSet, int initialTexCoordIndex) {	
	if ((initialTexCoordIndex + validVertexCount) > vertexCount) {
	    throw new IllegalArgumentException(J3dI18N.getString("GeometryArray103"));
	}

	if((vertexFormat & GeometryArray.USE_NIO_BUFFER) != 0){
	    if((vertexType & TEXCOORD_DEFINED) == TF) {
		FloatBufferWrapper texBuffer = (FloatBufferWrapper)(((J3DBuffer) refTexCoordsBuffer[texCoordSet]).getBufferImpl());
		if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
		    if (texBuffer.limit() < 2 * (initialTexCoordIndex+ validVertexCount) ) {
			throw new ArrayIndexOutOfBoundsException(
								 J3dI18N.getString("GeometryArray113"));
		    }
		} else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_3) != 0) {
		    if (texBuffer.limit() < 3 * (initialTexCoordIndex + validVertexCount) ) {
			throw new ArrayIndexOutOfBoundsException(
								 J3dI18N.getString("GeometryArray113"));
		    }
		} else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_4) != 0) {
		    if (texBuffer.limit() < 4 * (initialTexCoordIndex + validVertexCount)) {
			throw new ArrayIndexOutOfBoundsException(
								 J3dI18N.getString("GeometryArray113"));
		    }
		}
	    }
	} else {
	    switch ((vertexType & TEXCOORD_DEFINED)) {
	    case TF:
		if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
		    if (((float[])refTexCoords[texCoordSet]).length < 2 * (initialTexCoordIndex+ validVertexCount) ) {
			throw new ArrayIndexOutOfBoundsException(
								 J3dI18N.getString("GeometryArray113"));
		    }
		} else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_3) != 0) {
		    if (((float[])refTexCoords[texCoordSet]).length < 3 * (initialTexCoordIndex + validVertexCount) ) {
			throw new ArrayIndexOutOfBoundsException(
								 J3dI18N.getString("GeometryArray113"));
		    }
		} else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_4) != 0) {
		    if (((float[])refTexCoords[texCoordSet]).length < 4 * (initialTexCoordIndex + validVertexCount)) {
			throw new ArrayIndexOutOfBoundsException(
								 J3dI18N.getString("GeometryArray113"));
		    }
		}
		break;
	    case T2F:
		if (((TexCoord2f[])refTexCoords[texCoordSet]).length < (initialTexCoordIndex+ validVertexCount) ) {
		    throw new ArrayIndexOutOfBoundsException(
							     J3dI18N.getString("GeometryArray113"));
		}
		break;
	    case T3F: 
		if (((TexCoord3f[])refTexCoords[texCoordSet]).length < (initialTexCoordIndex+ validVertexCount) ) {
		    throw new ArrayIndexOutOfBoundsException(
							     J3dI18N.getString("GeometryArray113"));
		}
		break;
	    }
	}
	geomLock.getLock();
	dirtyFlag |= TEXTURE_CHANGED;
	this.initialTexCoordIndex[texCoordSet] = initialTexCoordIndex;
	geomLock.unLock();
	// There is no need to send message for by reference, since we
	// use VA
    }

    int getInitialTexCoordIndex(int texCoordSet) {
	return initialTexCoordIndex[texCoordSet];
    }


    int getTexCoordSetCount() {
	return this.texCoordSetCount;
    }

    int getTexCoordSetMapLength() {
	if (this.texCoordSetMap != null)
	    return this.texCoordSetMap.length;
	else
	    return 0;
    }

    void getTexCoordSetMap(int [] texCoordSetMap) {
	
	if (this.texCoordSetMap!=null) {
	    for (int i = 0; i < this.texCoordSetMap.length; i++) {
	         texCoordSetMap[i] = this.texCoordSetMap[i];
	    }
	}
    }

    protected void finalize() {
	// For Pure immediate mode, there is no clearLive so
	// surface will free when JVM do GC 
	if (pVertexBuffers != 0) {
	    // memory not yet free for immediate mode rendering
	    // It is thread safe since D3D only free surface in
	    // the next swapBuffer() call which must be in the
	    // same renderer threads
	    freeD3DArray(true);
	}
    }

    void freeDlistId() {
	if (dlistId != -1) {
	    VirtualUniverse.mc.freeDisplayListId(dlistObj);
	    dlistId = -1;
	}
    }

    void assignDlistId() {
	if (dlistId == -1) {
	    dlistObj = VirtualUniverse.mc.getDisplayListId();
	    dlistId = dlistObj.intValue();
	}
    }

    // Add the specified render atom as a user of this geometry array
    // (for the specified render bin)
    void addDlistUser(RenderBin renderBin, RenderAtomListInfo ra) {
	if (dlistUsers == null) {
	    dlistUsers = new HashMap(2, 1.0f);
	}

	Set raSet = (Set)dlistUsers.get(renderBin);
	if (raSet == null) {
	    raSet = new HashSet();
	    dlistUsers.put(renderBin, raSet);
	}
	raSet.add(ra);
    }

    // Remove the specified render atom from the set of users of this
    // geometry array (for the specified render bin)
    void removeDlistUser(RenderBin renderBin, RenderAtomListInfo ra) {
	if (dlistUsers == null) {
	    // Nothing to do
	    return;
	}

	Set raSet = (Set)dlistUsers.get(renderBin);
	if (raSet == null) {
	    // Nothing to do
	    return;
	}
	raSet.remove(ra);
    }

    // Returns true if the set of render atoms using this geometry
    // array in the specified render bin is empty.
    boolean isDlistUserSetEmpty(RenderBin renderBin) {
	if (dlistUsers == null) {
	    return true;
	}

	Set raSet = (Set)dlistUsers.get(renderBin);
	if (raSet == null) {
	    return true;
	}
	return raSet.isEmpty();
    }

    // This method is used for debugging only
    int numDlistUsers(RenderBin renderBin) {
	if (isDlistUserSetEmpty(renderBin)) {
	    return 0;
	}
	Set raSet = (Set)dlistUsers.get(renderBin);
	return raSet.size();
    }

    void setDlistTimeStamp(int rdrBit, long timeStamp) {
	int index = getIndex(rdrBit);
	if (index >= timeStampPerDlist.length) {
	    long[] newList = new long[index * 2];
	    for (int i = 0; i < timeStampPerDlist.length; i++) {
		 newList[i] = timeStampPerDlist[i];
	    }
	    timeStampPerDlist = newList;
	} 
	timeStampPerDlist[index] = timeStamp;
    }

    long getDlistTimeStamp(int rdrBit) {
	int index = getIndex(rdrBit);
       // If index is greater than what currently exists, increase
       // the array and return zero
       if (index >= timeStampPerDlist.length) {
           setDlistTimeStamp(rdrBit, 0);
       }
       return timeStampPerDlist[index];
    }

    int getIndex(int bit) {
	int num = 0;
	
	while (bit > 0) {
	    num++;
	    bit >>= 1;
	}
	return num;
    }


    boolean isWriteStatic() {

	if (source.getCapability(GeometryArray.ALLOW_COORDINATE_WRITE ) ||
	    source.getCapability(GeometryArray.ALLOW_COLOR_WRITE) || 
	    source.getCapability(GeometryArray.ALLOW_NORMAL_WRITE) ||
	    source.getCapability(GeometryArray.ALLOW_TEXCOORD_WRITE) ||
            source.getCapability(GeometryArray.ALLOW_VERTEX_ATTR_WRITE) ||
	    source.getCapability(GeometryArray.ALLOW_COUNT_WRITE) ||
	    source.getCapability(GeometryArray.ALLOW_REF_DATA_WRITE))
	    return false;

	return true;
    }

    /**
     * The functions below are only used in compile mode
     */
    void setCompiled(ArrayList curList) {
	int i;
	int num = curList.size();
	int offset = 0;
	geoOffset = new int[num];
	compileVcount = new int[num];
	int vcount = 0, vformat = 0;
	vcount = 0;
	isCompiled = true;
	
	if (num > 0)
	    source = ((SceneGraphObjectRetained)curList.get(0)).source;
	for (i = 0; i < num; i++) {
	    // Build the back mapping
	    GeometryArrayRetained geo = (GeometryArrayRetained)curList.get(i);
	    ((GeometryArray)geo.source).retained = this;
	    compileVcount[i] = geo.getValidVertexCount();
	    vcount += geo.getValidVertexCount();
	    geoOffset[i] = offset;
	    offset += geo.stride() * compileVcount[i];
	    vformat = geo.getVertexFormat();
	}
	createGeometryArrayData(vcount, vformat);

	// Assign the initial and valid fields
	validVertexCount = vcount;
	initialVertexIndex = 0;
	
	mergeGeometryArrays(curList);

    }

    /*
    // Ununsed
    int getVertexCount(int index) {
	return compileVcount[index];
    }


    int getValidVertexCount(int index) {
	return compileVcount[index];
    }	


    int getInitialVertexIndex(int index) {
	return 0;
    }
    */

    void mergeGeometryArrays(ArrayList list) {
	float[] curVertexData;
	int length, srcOffset;
	int curOffset = 0;
	// We only merge if the texCoordSetCount is 1 and there are no
        // vertex attrs
	if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
	    texCoordSetCount = 1;
	    texCoordSetMap = new int[1];
	    texCoordSetMap[0] = 1;
	}
	for (int i = 0; i < list.size(); i++) {
	    GeometryArrayRetained geo = (GeometryArrayRetained)list.get(i);
	    // Take into account the validVertexCount and initialVertexIndex
	    curVertexData = geo.vertexData;
	    length = geo.validVertexCount * stride;
	    srcOffset = geo.initialVertexIndex * stride;
	    System.arraycopy(curVertexData, srcOffset, this.vertexData, curOffset,
			     length);
	    curOffset += length;

	    // assign geoBounds
	    geoBounds.combine(geo.geoBounds);
	    
	}
	this.centroid.set(geoBounds.getCenter());
    }

    boolean isMergeable() {
	
	// For now, turn off by ref geometry
	if ((vertexFormat & GeometryArray.BY_REFERENCE) != 0) 
	    return false;

	if (!isStatic())
	    return false;
	
	// If there is more than one set of texture coordinate set defined
	// then don't merge geometry (we avoid dealing with texCoordSetMap
	if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0 &&
	    (texCoordSetCount > 1 ||
	     texCoordSetMap != null && texCoordSetMap.length > 1)) {
	    return false;
	}
        
        // We will avoid merging geometry if there are any vertex attributes.
        if ((vertexFormat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
            return false;
        }

	// If intersect is allowed turn off merging
	if (source.getCapability(Geometry.ALLOW_INTERSECT))
	    return false;

	return true;
    }

    void compile(CompileState compState) {
        super.compile(compState);

	if ((vertexFormat & GeometryArray.NORMALS) != 0) {
	    compState.needNormalsTransform = true;
	}
    }

    void mergeTransform(TransformGroupRetained xform) {
	if (geoBounds != null) {
	    geoBounds.transform(xform.transform);
	}
    }

    // This adds a MorphRetained to the list of users of this geometry
    void addMorphUser(MorphRetained m) {
        int index;
        ArrayList morphList;

	if(morphUniverseList == null) {
	    morphUniverseList = new ArrayList(1);
	    morphUserLists = new ArrayList(1);
	}
        synchronized (morphUniverseList) {
            if (morphUniverseList.contains(m.universe)) {
                index = morphUniverseList.indexOf(m.universe);
                morphList = (ArrayList)morphUserLists.get(index);
                morphList.add(m);
            } else {
                morphUniverseList.add(m.universe);
                morphList = new ArrayList(5);
                morphList.add(m);
                morphUserLists.add(morphList);
            }
        }
    }

    // This adds a MorphRetained to the list of users of this geometry
    void removeMorphUser(MorphRetained m) {
        int index;
        ArrayList morphList;

	if(morphUniverseList == null)
	    return;
	
        synchronized (morphUniverseList) {
            index = morphUniverseList.indexOf(m.universe);
            morphList = (ArrayList)morphUserLists.get(index);
            morphList.remove(morphList.indexOf(m));
            if (morphList.size() == 0) {
                morphUserLists.remove(index);
                morphUniverseList.remove(index);
            }
        }
    }
    // Initialize mirror object when geometry is first setLived
    void initMirrorGeometry() {
	geomLock.getLock();
	if (this instanceof IndexedGeometryArrayRetained) {
	    if ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0) {
		mirrorGeometry =
		    ((IndexedGeometryArrayRetained)this).cloneNonIndexedGeometry();
	    }
	    else {
		mirrorGeometry = null;
	    }
	}
	geomLock.unLock();
	
    }

    // Update Mirror Object in response to change in geometry
    void updateMirrorGeometry() {
	geomLock.getLock();
	if (this instanceof IndexedGeometryArrayRetained) {
	    if (mirrorGeometry != null) {
		mirrorGeometry =
		    ((IndexedGeometryArrayRetained)this).cloneNonIndexedGeometry();
	    }
	}
	geomLock.unLock();
	
    }
    

    // Used by the picking intersect routines 
    void getVertexData(int i, Point3d pnts) {
	int offset;
	if ((vertexFormat & GeometryArray.BY_REFERENCE) == 0) {
	    offset = stride * i + coordinateOffset;    
	    pnts.x = this.vertexData[offset];
	    pnts.y = this.vertexData[offset+1];
	    pnts.z = this.vertexData[offset+2];
	    return;
	}
	
	if ((vertexFormat & GeometryArray.USE_NIO_BUFFER) == 0 ) {
	    if ((vertexFormat & GeometryArray.INTERLEAVED) != 0) {
		offset = stride * i + coordinateOffset;    
		pnts.x = this.interLeavedVertexData[offset];
		pnts.y = this.interLeavedVertexData[offset+1];
		pnts.z = this.interLeavedVertexData[offset+2];
	    }
	    else {
		switch ((vertexType & GeometryArrayRetained.VERTEX_DEFINED)) {
		case GeometryArrayRetained.PF:
		    offset = i*3;
		    pnts.x = this.floatRefCoords[offset];
		    pnts.y = this.floatRefCoords[offset+1];
		    pnts.z = this.floatRefCoords[offset+2];
		    break;
		case GeometryArrayRetained.PD:
		    offset = i*3;
		    pnts.x = this.doubleRefCoords[offset];
		    pnts.y = this.doubleRefCoords[offset+1];
		    pnts.z = this.doubleRefCoords[offset+2];
		    break;
		case GeometryArrayRetained.P3F:
		    pnts.x = this.p3fRefCoords[i].x;
		    pnts.y = this.p3fRefCoords[i].y;
		    pnts.z = this.p3fRefCoords[i].z;
		    break;
		case GeometryArrayRetained.P3D:
		    pnts.x = this.p3dRefCoords[i].x;
		    pnts.y = this.p3dRefCoords[i].y;
		    pnts.z = this.p3dRefCoords[i].z;
		    break;
		}
	    }
	}// end of non nio buffer
	else { // NIO BUFFER
	    if ((vertexFormat & GeometryArray.INTERLEAVED) != 0) {
		offset = stride * i + coordinateOffset;    
		pnts.x = this.interleavedFloatBufferImpl.get(offset);
		pnts.y = this.interleavedFloatBufferImpl.get(offset+1);
		pnts.z = this.interleavedFloatBufferImpl.get(offset+2);
	    }
	    else {
		switch ((vertexType & GeometryArrayRetained.VERTEX_DEFINED)) {
		case GeometryArrayRetained.PF:
		    offset = i*3;
		    pnts.x = this.floatBufferRefCoords.get(offset);
		    pnts.y = this.floatBufferRefCoords.get(offset+1);
		    pnts.z = this.floatBufferRefCoords.get(offset+2);
		    break;
		case GeometryArrayRetained.PD:
		    offset = i*3;
		    pnts.x = this.doubleBufferRefCoords.get(offset);
		    pnts.y = this.doubleBufferRefCoords.get(offset+1);
		    pnts.z = this.doubleBufferRefCoords.get(offset+2);
		    break;
		}
	    }	
	} // end of nio buffer
    }

    void getCrossValue(Point3d p1, Point3d p2, Vector3d value) {
        value.x += p1.y*p2.z - p1.z*p2.y;
	value.y += p2.x*p1.z - p2.z*p1.x;
        value.z += p1.x*p2.y - p1.y*p2.x;
    }


    boolean intersect(Transform3D thisLocalToVworld, 
		      Transform3D otherLocalToVworld, GeometryRetained  geom) {
	
	Transform3D tg =  VirtualUniverse.mc.getTransform3D(null);
	boolean isIntersect = false;

	if (geom instanceof GeometryArrayRetained ) {
	    GeometryArrayRetained geomArray = (GeometryArrayRetained)  geom;

	    if (geomArray.validVertexCount >= validVertexCount) {
		tg.invert(otherLocalToVworld);
		tg.mul(thisLocalToVworld);
		isIntersect = intersect(tg, geom);
	    } else {
		tg.invert(thisLocalToVworld);
		tg.mul(otherLocalToVworld);
		isIntersect = geomArray.intersect(tg, this);	    
	    }
	} else {
		tg.invert(thisLocalToVworld);
		tg.mul(otherLocalToVworld);
		isIntersect = geom.intersect(tg, this);	    
	}

	FreeListManager.freeObject(FreeListManager.TRANSFORM3D, tg);
	return isIntersect;
    }

    int getNumCoordCount() {
	int count = 0;
	if ((vertexFormat & GeometryArray.COORDINATES) != 0){
	    if ((vertexFormat & GeometryArray.BY_REFERENCE) == 0){
		count = vertexCount;
		return count;
	    }
	    
	    if ((vertexFormat & GeometryArray.USE_NIO_BUFFER) == 0) {
		if ((vertexFormat & GeometryArray.INTERLEAVED) == 0){
		    switch ((vertexType & GeometryArrayRetained.VERTEX_DEFINED)) {
		    case PF:
			count =  floatRefCoords.length/3;
			break;
		    case PD:
			count = doubleRefCoords.length/3;
			break;
		    case P3F:
			count = p3fRefCoords.length;
			break;
		    case P3D:
			count = p3dRefCoords.length;
			break;
		    }
		}
		else {
		    count = interLeavedVertexData.length/stride;
		}
	    }
	    else { // nio buffer
		if ((vertexFormat & GeometryArray.INTERLEAVED) == 0){
		    switch ((vertexType & GeometryArrayRetained.VERTEX_DEFINED)) {
		    case PF:
			count =  floatBufferRefCoords.limit()/3; // XXXX: limit or capacity?
			break;
		    case PD:
			count = doubleBufferRefCoords.limit()/3;
			break;
		    }
		}
		else {
		    count = interleavedFloatBufferImpl.limit()/stride;
		}
	    }
	}
	return count;
    }

    int getNumColorCount() {
	int count = 0;
	if ((vertexFormat & GeometryArray.COLOR) != 0){
	    if ((vertexFormat & GeometryArray.BY_REFERENCE) == 0){
		count = vertexCount;
		return count;
	    }
	    if ((vertexFormat & GeometryArray.USE_NIO_BUFFER) == 0) {
		if ((vertexFormat & GeometryArray.INTERLEAVED) == 0){
		    switch ((vertexType & GeometryArrayRetained.COLOR_DEFINED)) {
		    case CF:
			if ((vertexFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_3) {
			    count =  floatRefColors.length/3;
			}
			else if ((vertexFormat & GeometryArray.COLOR_4)== GeometryArray.COLOR_4){
			    count =  floatRefColors.length/4;
			}
			break;
		    case CUB: 
			if ((vertexFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_3) {
			    count =  byteRefColors.length/3;
			}
			else if ((vertexFormat & GeometryArray.COLOR_4)== GeometryArray.COLOR_4){
			    count =  byteRefColors.length/4;
			}
			break;
		    case C3F: 
			count = c3fRefColors.length;
			break;
		    case C4F: 
			count = c4fRefColors.length;
			break;
		    case C3UB:
			count = c3bRefColors.length;
			break;
		    case C4UB: 
			count = c4bRefColors.length;
			break;
		    }
		}
		else {
		    count = interLeavedVertexData.length/stride;
		}
	    } // end of non nio buffer
	    else {
		if ((vertexFormat & GeometryArray.INTERLEAVED) == 0){
		    switch ((vertexType & GeometryArrayRetained.COLOR_DEFINED)) {
		    case CF:
			if ((vertexFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_3) {
			    count =  floatBufferRefColors.limit()/3;
			}
			else if ((vertexFormat & GeometryArray.COLOR_4)== GeometryArray.COLOR_4){
			    count =  floatBufferRefColors.limit()/4;
			}
			break;
		    case CUB: 
			if ((vertexFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_3) {
			    count =  byteBufferRefColors.limit()/3;
			}
			else if ((vertexFormat & GeometryArray.COLOR_4)== GeometryArray.COLOR_4){
			    count =  byteBufferRefColors.limit()/4;
			}
			break;
		    }
		}
		else {
		    count = interleavedFloatBufferImpl.limit()/stride;
		}		    
	    } // end of nio buffer
	}
	return count;
    }

    int getNumNormalCount() {
	int count = 0;
	if ((vertexFormat & GeometryArray.NORMALS) != 0){
	    if ((vertexFormat & GeometryArray.BY_REFERENCE) == 0){
		count = vertexCount;
		return count;
	    }
	    
	    if ((vertexFormat & GeometryArray.USE_NIO_BUFFER) == 0) {
		if ((vertexFormat & GeometryArray.INTERLEAVED) == 0){
		    switch ((vertexType & NORMAL_DEFINED)) {
		    case NF:
			count =  floatRefNormals.length/3;
			break;
		    case N3F:
			count = v3fRefNormals.length;
			break;
		    }
		}
		else {
		    count = interLeavedVertexData.length/stride;
		}
	    } // end of non nio buffer
	    else {
		if ((vertexFormat & GeometryArray.INTERLEAVED) == 0){
		    if ((vertexType & NORMAL_DEFINED) == NF ) {
			count =  floatBufferRefNormals.limit()/3;
		    }
		}
		else {
		    count = interleavedFloatBufferImpl.limit()/stride;
		}
	    }
	}
	return count;
    }

    int getNumTexCoordCount(int i) {
	int count = 0;
	if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0){
	    if ((vertexFormat & GeometryArray.BY_REFERENCE) == 0){
		count = vertexCount;
		return count;
	    }
	   
	    if ((vertexFormat & GeometryArray.USE_NIO_BUFFER) == 0) {
		if ((vertexFormat & GeometryArray.INTERLEAVED) == 0){
		    switch ((vertexType & TEXCOORD_DEFINED)) {
		    case TF:
			if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
			    count = ((float[])refTexCoords[i]).length/2;
			} else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_3) != 0) {
			    count = ((float[])refTexCoords[i]).length/3;
			} else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_4) != 0) {
			    count = ((float[])refTexCoords[i]).length/4;
			}

			break;
		    case T2F:
			count = ((TexCoord2f[])refTexCoords[i]).length;
			break;
		    case T3F:
			count = ((TexCoord3f[])refTexCoords[i]).length;
		    }
		}
		else {
		    count = interLeavedVertexData.length/stride;
		}
	    }
	    else { // nio buffer
		if ((vertexFormat & GeometryArray.INTERLEAVED) == 0){
		    if ((vertexType & TEXCOORD_DEFINED) == TF) {
			FloatBufferWrapper texBuffer = (FloatBufferWrapper)(((J3DBuffer) refTexCoordsBuffer[i]).getBufferImpl());
			if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
			    count = texBuffer.limit()/2;
			} else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_3) != 0) {
			    count = texBuffer.limit()/3;
			} else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_4) != 0) {
			    count = texBuffer.limit()/4;
			}
		    }
		}
		else {
		    count = interleavedFloatBufferImpl.limit()/stride;
		}
	    }
	}
	return count;
    }

    // NOTE: we don't need a getNumVertexAttrCount method, since getNum*Count
    // is only called by Morph, which doesn't support vertex attrs


    // Found the min distance from center to the point/line/tri/quad
    // form by dist[]
    void computeMinDistance(Point3d coordinates[], Point3d center,
			    Vector3d normal,
			    double dist[], Point3d iPnt) {
	double x, y, z;
	int i, j;

	if (coordinates.length == 1) {
	    // a point
	    iPnt.x = coordinates[0].x;
	    iPnt.y = coordinates[0].y;
	    iPnt.z = coordinates[0].z;
	    x = iPnt.x - center.x;
	    y = iPnt.y - center.y;
	    z = iPnt.z - center.z;
	    dist[0] = Math.sqrt(x*x + y*y + z*z);	    
	    return;
	}


	if (coordinates.length == 2) {
	    // a line
	    dist[0] = Math.sqrt(Distance.pointToSegment(center, 
							coordinates[0], 
							coordinates[1], 
							iPnt, null));
	    return;
	}

	double normalLen = 0;

	if (normal == null) {
	    Vector3d vec0 = new Vector3d();
	    Vector3d vec1 = new Vector3d();
	    normal = new Vector3d();
	    // compute plane normal for coordinates.
	    for (i=0; i<coordinates.length-1;) {
		vec0.x = coordinates[i+1].x - coordinates[i].x;
		vec0.y = coordinates[i+1].y - coordinates[i].y;
		vec0.z = coordinates[i+1].z - coordinates[i++].z;
		if(vec0.length() > 0.0)
		    break;
	    }
        
	    for (j=i; j<coordinates.length-1; j++) {
		vec1.x = coordinates[j+1].x - coordinates[j].x;
		vec1.y = coordinates[j+1].y - coordinates[j].y;
		vec1.z = coordinates[j+1].z - coordinates[j].z;
		if(vec1.length() > 0.0)
		    break;
	    }
      
	    if (j == (coordinates.length-1)) {
		// Degenerate polygon, check with edge only
		normal = null;
	    } else {
		normal.cross(vec0,vec1);
	    }
	}

	if (normal != null) {
	    normalLen = normal.length(); 
	    if ( normalLen == 0.0) {
		// Degenerate polygon, check with edge only
		normal = null;
	    }
	}


	if (coordinates.length == 3) {
	    // a triangle
	    if (normal != null) {
		double d = -(normal.x*coordinates[0].x +
			     normal.y*coordinates[0].y +
			     normal.z*coordinates[0].z);
		dist[0] = (normal.x*center.x + normal.y*center.y +
			   normal.z*center.z +
			   d)/normalLen;
		iPnt.x = center.x - dist[0]*normal.x/normalLen;
		iPnt.y = center.y - dist[0]*normal.y/normalLen;
		iPnt.z = center.z - dist[0]*normal.z/normalLen;

		 if (pointInTri(iPnt, coordinates[0], coordinates[1],
				coordinates[2], normal)) {
		     return;
		 }
	    }

	    // checking point to line distance
	    double minDist;
	    Point3d minPnt = new Point3d();

	    dist[0] = Distance.pointToSegment(center, coordinates[0], 
					      coordinates[1], iPnt, null);
	    minDist = Distance.pointToSegment(center, coordinates[1], 
					      coordinates[2], minPnt, null);
	    if (minDist < dist[0]) {
		dist[0] = minDist;
		iPnt.x = minPnt.x;
		iPnt.y = minPnt.y;
		iPnt.z = minPnt.z;
	    }
	    minDist = Distance.pointToSegment(center, coordinates[2], 
					      coordinates[0], minPnt, null);    
	    if (minDist < dist[0]) {
		dist[0] = minDist;
		iPnt.x = minPnt.x;
		iPnt.y = minPnt.y;
		iPnt.z = minPnt.z;
	    }	    
	    dist[0] = Math.sqrt(dist[0]);
	    return;
	}

	// a quad
	if (normal != null) {
	    double d = -(normal.x*coordinates[0].x +
			 normal.y*coordinates[0].y +
			 normal.z*coordinates[0].z);
	    dist[0] = (normal.x*center.x + normal.y*center.y +
		       normal.z*center.z +
		       d)/normalLen;
	    iPnt.x = center.x - dist[0]*normal.x/normalLen;
	    iPnt.y = center.y - dist[0]*normal.y/normalLen;
	    iPnt.z = center.z - dist[0]*normal.z/normalLen;
	    
	    if (pointInTri(iPnt, coordinates[0], coordinates[1],
			   coordinates[2], normal) ||
		pointInTri(iPnt, coordinates[1], coordinates[2],
			   coordinates[3], normal)) {
		return;
	    }
	}

	// checking point to line distance
	double minDist;
	Point3d minPnt = new Point3d();

	dist[0] = Distance.pointToSegment(center, coordinates[0], 
					  coordinates[1], iPnt, null);
	minDist = Distance.pointToSegment(center, coordinates[1], 
					  coordinates[2], minPnt, null);
	if (minDist < dist[0]) {
	    dist[0] = minDist;
	    iPnt.x = minPnt.x;
	    iPnt.y = minPnt.y;
	    iPnt.z = minPnt.z;
	}
	minDist = Distance.pointToSegment(center, coordinates[2], 
					  coordinates[3], minPnt, null);    
	if (minDist < dist[0]) {
	    dist[0] = minDist;
	    iPnt.x = minPnt.x;
	    iPnt.y = minPnt.y;
	    iPnt.z = minPnt.z;
	}	    

	minDist = Distance.pointToSegment(center, coordinates[3], 
					  coordinates[0], minPnt, null);    
	if (minDist < dist[0]) {
	    dist[0] = minDist;
	    iPnt.x = minPnt.x;
	    iPnt.y = minPnt.y;
	    iPnt.z = minPnt.z;
	}	    

	dist[0] = Math.sqrt(dist[0]);
    }

    Vector3d getVector3d() {
	return (Vector3d)FreeListManager.getObject(FreeListManager.VECTOR3D);
    }

    void freeVector3d(Vector3d v) {
	FreeListManager.freeObject(FreeListManager.VECTOR3D, v);
    }

    Point3d getPoint3d() {
	return (Point3d)FreeListManager.getObject(FreeListManager.POINT3D);
    }

    void freePoint3d(Point3d p) {
	FreeListManager.freeObject(FreeListManager.POINT3D, p);
    }


    void handleFrequencyChange(int bit) {
	int mask = 0;
	if ((vertexFormat & GeometryArray.BY_REFERENCE) == 0) {
	    if ((bit == GeometryArray.ALLOW_COORDINATE_WRITE) ||
		(((vertexFormat & GeometryArray.COLOR) != 0) &&
		 bit == GeometryArray.ALLOW_COLOR_WRITE)||
		(((vertexFormat & GeometryArray.NORMALS) != 0) &&
		 bit == GeometryArray.ALLOW_NORMAL_WRITE) ||
		(((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) &&
		 bit == GeometryArray.ALLOW_TEXCOORD_WRITE) ||
		(((vertexFormat & GeometryArray.VERTEX_ATTRIBUTES) != 0) &&
		 bit == GeometryArray.ALLOW_VERTEX_ATTR_WRITE) ||
		(bit == GeometryArray.ALLOW_COUNT_WRITE)) {
		mask = 1;
	    }
	}
	else {
	    if (bit == GeometryArray.ALLOW_REF_DATA_WRITE)
		mask = 1;
	}
	if (mask != 0) {
	    setFrequencyChangeMask(bit, mask);
	}
    }

    int getTexCoordType() {
        return texCoordType;
    }

    int getVertexAttrType() {
        return vertexAttrType;
    }

}
