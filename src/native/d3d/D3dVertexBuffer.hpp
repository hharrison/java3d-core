/*
 * $RCSfile$
 *
 * Copyright 2000-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

#if !defined(D3DVERTEXBUFFER_H)
#define D3DVERTEXBUFFER_H

#include "StdAfx.h"

class D3dCtx;

class D3dVertexBuffer;

typedef D3dVertexBuffer* LPD3DVERTEXBUFFER;
typedef vector<LPD3DVERTEXBUFFER> D3dVertexBufferVector;

//ISSUE 135 iterator for D3dVertexBuffer
typedef vector<LPD3DVERTEXBUFFER>::iterator ITER_LPD3DVERTEXBUFFER;

class D3dVertexBuffer {
public:

    // Actual buffer memory to hold all the vertices
    LPDIRECT3DVERTEXBUFFER9 buffer;

    // Indexed buffer for rendering IndexedGeometry
    LPDIRECT3DINDEXBUFFER9 indexBuffer;

    // D3D type of this Vertex Buffer
    D3DPRIMITIVETYPE        primitiveType;

    // Length of following numVertices array allocate
    UINT                    numVerticesLen;

    // Store the number of vertices for each strip
    USHORT                  *numVertices;

    // It true when QuadArray is used or 
    // indexGeometry is used.
    BOOL                    isIndexPrimitive;

    // If D3DUSAGE_POINTS flag is used to 
    // create this VertexBuffer 
    BOOL                    isPointFlagUsed;
 
    // Flexible vertex format for this VB
    DWORD                   vertexFormat;

    // Stride of each vertex in the buffer
    // compute from above vertexFormat
    UINT                    stride;

	UINT                    numCoorTexSupported;




    // Number of strips used for StripGeometryArray
    // For each strip i the number of vertex is
    // numVertices[i]
    UINT                    stripLen;

    // Point to next overflow VB when size > VB limit
    LPD3DVERTEXBUFFER       nextVB;

    // content that this buffer belongs, only the first node set this one.
    D3dCtx                  *ctx; 

    // vcount is the number of vertex that this buffer
    // can hold. vcount*stride is always equal to 
    // current VB size
    UINT                    vcount;

    // indexCount is the number of index that this buffer
    // can hold. indexCount*indexSize is always equal to 
    // current index VB size.
    UINT                    indexCount;

    // Vertex count of all VBs link by nextVB, 
    // only the first node need to remember this info.
    // The other overflow buffer always set it to zero. 
    DWORD                   totalVertexCount;

    // Same as above, except for indexBuffer
    DWORD                   totalIndexCount;

    // This is a list of VB remember by D3dCtx
    // so that it can release() all surface when canvas resize
    // Only the header D3dVertexBuffer contains non-null entry
    LPD3DVERTEXBUFFER        next;
    LPD3DVERTEXBUFFER        previous;

    // Pointer back to the GeometryArrayRetained pVertexBuffers
    // This is used to remove itself from pVertexBuffers table
    // when ctx destroy
    D3dVertexBufferVector* vbVector; 

    // Last texture coordinate position  =
    //   (i) textureCoordSetMap[pass]    if executeVA() 
    //   (ii) texCoordSetMapOffset[pass] if execute() or buildGA()
    //   (iii) TEX_EYE_LINEAR/TEX_SPHERE_MAP/TEX_OBJ_LINEAR/TEX_REFLECT_MAP 
    //                     if automatic texture generation is used
    // This is used for VertexBuffer to know whether Texture
    // coordinate need to copy or not in case texture unit swap
    // or texture unit change from automatic texture generation
    // to use coordinate index specifies by user.
    int texCoordPosition[D3DDP_MAXTEXCOORD];

    // Max vertex limit allow for this primitive type
    // This is used for display list optimization without
    // recompute it again.
    int maxVertexLimit;

    D3dVertexBuffer();
    ~D3dVertexBuffer();    

    VOID release();
    VOID render(D3dCtx *d3dCtx);
    BOOL initializeNumVertices(int len);
    VOID addStride(int len);
    VOID addStrides(jint stripLen, jint *strips);
    VOID appendStrides(jint stripLen, USHORT *strips);
};


#endif
