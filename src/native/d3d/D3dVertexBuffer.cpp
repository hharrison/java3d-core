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

#include "StdAfx.h"
#include "D3dVertexBuffer.hpp"


D3dVertexBuffer::D3dVertexBuffer()
{
    buffer = NULL;
    indexBuffer = NULL;
    numVertices = NULL;
    numVerticesLen = 0;
    isIndexPrimitive = FALSE;
    nextVB = NULL;
    stripLen = 0;
    totalVertexCount = 0;
    ctx = NULL;
    next = NULL;
    previous = NULL;
    isPointFlagUsed = FALSE;
    primitiveType = D3DPT_FORCE_DWORD;
}

D3dVertexBuffer::~D3dVertexBuffer()
{
    release();
}

VOID D3dVertexBuffer::release()
{
    SafeRelease(buffer);
    SafeRelease(indexBuffer);
    SafeDelete(numVertices);

    numVerticesLen = 0;
    isIndexPrimitive = FALSE;
    isPointFlagUsed = FALSE;
    stripLen = 0;
    totalVertexCount = 0;
    // recursively free the list
    SafeDelete(nextVB);
}


	
VOID D3dVertexBuffer::render(D3dCtx *d3dCtx)
{
    D3DPRIMITIVETYPE oldPrimitiveType;
    BOOL renderPoint = false;
    BOOL restorePointSize = false;
    float oldPointSize = 1.0f;

    if ((buffer != NULL) && (numVertices != NULL)) {
	// device is already check for NULL in callDisplayList
	LPDIRECT3DDEVICE9 device = d3dCtx->pDevice; 
	BOOL setAmbientLight = false;

	if (((vertexFormat & D3DFVF_DIFFUSE) == 0) && 
	     (!d3dCtx->isLightEnable)) {
	    setAmbientLight = true;
	    if (totalVertexCount > 0) {
		// This is the first Node in the list
		d3dCtx->setAmbientLightMaterial();
	    }
	}

	if ((d3dCtx->pointSize > 1) &&
	    ((d3dCtx->fillMode == D3DFILL_POINT) ||
	     (primitiveType == D3DPT_POINTLIST))) {
	    // Some driver may cull the point away if not 
	    // set to CULL_NONE 
	    if (!isPointFlagUsed) {
		// restore point size to 1
		if (debug) {
		    printf("VB render with pointSize %d without D3DPOINT flag set\n", d3dCtx->pointSize);
		}
		device->SetRenderState(D3DRS_POINTSIZE, *((LPDWORD)
							  &oldPointSize));
		restorePointSize = true;
	    } else {
		device->SetRenderState(D3DRS_CULLMODE, D3DCULL_NONE);
		// workaround for driver bug, otherwise you will
		// see four corner points instead of one big point	    
		// if fill mode is POINT
		device->SetRenderState(D3DRS_FILLMODE, D3DFILL_SOLID);
		if (d3dCtx->deviceInfo->maxPointSize < d3dCtx->pointSize) {
		    // Use software vertex processing mode
		    //device->SetRenderState(D3DRS_SOFTWAREVERTEXPROCESSING, TRUE);
			device->SetSoftwareVertexProcessing(TRUE);
		}
		
		oldPrimitiveType = primitiveType;
		// For Polygon D3DFill_POINT mode we need to
		// temporary switch primitive to point list
		primitiveType = D3DPT_POINTLIST;
		renderPoint = true;
	    }
	}

	device->SetStreamSource(0, buffer,0, stride); 
	//device->SetVertexShader(vertexFormat);
	device->SetVertexShader(NULL);
	device->SetFVF(vertexFormat);
	
	int startIdx=0;
	int vc, i;

	if (!isIndexPrimitive ||
	    ((indexBuffer == NULL) && renderPoint)) {
	    for (i = 0; i < stripLen; i++) {
		vc = numVertices[i];
		device->DrawPrimitive(primitiveType,
				      startIdx,
				      getPrimitiveNum(primitiveType,vc));
		startIdx += vc;
	    }
	} else {
	    if (indexBuffer != NULL) {
		device->SetIndices(indexBuffer); 	
		for (i = 0; i < stripLen; i++) {
		    vc = numVertices[i];
		    device->DrawIndexedPrimitive(primitiveType,0,
						 0,
						 vcount,
						 startIdx,
						 getPrimitiveNum(primitiveType, vc));
		    startIdx += vc;
		}
	    } else {
		if (d3dCtx->quadIndexBufferSize > 0) {
		    // Index is successfully set
		    device->SetIndices(d3dCtx->quadIndexBuffer); 	
		    device->DrawIndexedPrimitive(D3DPT_TRIANGLELIST,0,
				         0,
						 numVertices[0],
						 0,
						 numVertices[0] >> 1);
		} 
		// Otherwise not enough memory when index buffer
		// is created, so draw nothing.
	    }
	}

	if (setAmbientLight && (nextVB == NULL)) {
	    // This is the last Node in the list
	    d3dCtx->restoreDefaultLightMaterial();
	}

	if (renderPoint) {
	    device->SetRenderState(D3DRS_CULLMODE, d3dCtx->cullMode);
	    device->SetRenderState(D3DRS_FILLMODE, d3dCtx->fillMode);
        /** device->SetRenderState(D3DRS_SOFTWAREVERTEXPROCESSING,
				   d3dCtx->softwareVertexProcessing);
        **/ 
		device->SetSoftwareVertexProcessing(d3dCtx->softwareVertexProcessing);
	    primitiveType = oldPrimitiveType;
	} else if (restorePointSize) {
	    device->SetRenderState(D3DRS_POINTSIZE, 
				   *((LPDWORD) &d3dCtx->pointSize));
	}
    }

    if (nextVB != NULL) {
	nextVB->render(d3dCtx);
    }
}


VOID D3dVertexBuffer::addStride(int len)
{
    if (numVerticesLen <= stripLen) {
	if (numVerticesLen == 0) {
	    numVertices = new USHORT[1];
	    if (numVertices == NULL) {
		D3dCtx::d3dWarning(OUTOFMEMORY);
		return;
	    }
	    numVerticesLen = 1;
	} else {
	    int size = numVerticesLen << 1;
	    USHORT *p = new USHORT[size];
	    if (p == NULL) {
		D3dCtx::d3dWarning(OUTOFMEMORY);
		return;
	    }
	    CopyMemory(p, numVertices, numVerticesLen*sizeof(USHORT));
	    delete numVertices;
	    numVertices = p;
	    numVerticesLen = size;
	}
    }
    numVertices[stripLen++] = len;
}


/* 
 * This is used by Strip GeometryArray 
 * Replace all previously define stripLen by this one. 
 */
VOID D3dVertexBuffer::addStrides(jint len, jint* strips)
{
    int i = len;

    if (numVerticesLen < len) {
	if (numVertices) {
	    delete numVertices;
	}
	numVertices = new USHORT[len];
	numVerticesLen = len;
    }

    USHORT *q = numVertices;

    while (--i >= 0) {
	*q++ = *strips++;
    }
    stripLen = len;
}


/* 
 * This is used by D3dDisplayList optimize()
 * Append this one to the current strip define.
 */
VOID D3dVertexBuffer::appendStrides(jint len, USHORT* strips)
{
    int i;
    USHORT *oldVertices;

    if (numVerticesLen < stripLen + len) {
	oldVertices = numVertices;
	numVertices = new USHORT[len + stripLen];
	numVerticesLen = len + stripLen;
    }

    USHORT *q = numVertices;
    USHORT *p = oldVertices;

    if (oldVertices != NULL) { 
	i = stripLen;
	while (--i >= 0) {
	    *q++ = *p++;
	}
	delete oldVertices;
    }

    i = len;
    while (--i >= 0) {
	*q++ = *strips++;
    }

    stripLen = numVerticesLen;
}

