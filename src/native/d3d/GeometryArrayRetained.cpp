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

#include "StdAfx.h"


//#define VBDEBUG 
//#define TEXDEBUG 

#define getVertexFormat(texSize) \
                 (((texSize) << D3DFVF_TEXCOUNT_SHIFT) & D3DFVF_TEXCOUNT_MASK)

/*
 * This correspond to the constant in d3dtypes.h
 * under D3D 7.0/8.0 header and may not portable :
 * Still valid with D3D 9.0 (aces)
 * D3DFVF_TEXTUREFORMAT1 3       
 * D3DFVF_TEXTUREFORMAT2 0       
 * D3DFVF_TEXTUREFORMAT3 1       
 * D3DFVF_TEXTUREFORMAT4 2       
 */
CONST static DWORD TexFormatSizeTable[5] = {0, 3, 0, 1, 2};
static float defaultTexCoord[4] = {0, 0, 0, 0};

typedef struct _D3DDRAWPRIMITIVESTRIDEDDATA
{
    DWORD  positionStride;
    jfloat  *fpositionPtr;
    jdouble *dpositionPtr;
    DWORD  normalStride;
    jfloat *normalPtr;
    DWORD  diffuseStride;
    jfloat  *fdiffusePtr;
    jbyte   *bdiffusePtr;
    jint   *indexPtr;
    jint   initialIndexIndex; 
    DWORD  textureCoordsStride[D3DDP_MAXTEXCOORD];
    jfloat* textureCoordsPtr[D3DDP_MAXTEXCOORD];
    jint texCoordPosition[D3DDP_MAXTEXCOORD];
    boolean useAlpha; 
    boolean modulateAlpha;
    jfloat alpha;
} D3DDRAWPRIMITIVESTRIDEDDATA, *LPD3DDRAWPRIMITIVESTRIDEDDATA;


void copyIndexVertexToVB(D3dCtx *d3dCtx,
			 D3DDRAWPRIMITIVESTRIDEDDATA* strideData,
			 DWORD indexCount,
			 jint cDirty,
			 BOOL insertStrideToVB,
			 BOOL expandQuadIndex)
{
    HRESULT hr;

    if (cDirty & javax_media_j3d_GeometryArrayRetained_INDEX_CHANGED) {
	jint *src = strideData->indexPtr + strideData->initialIndexIndex;
	LPDIRECT3DINDEXBUFFER9 indexBuffer = d3dCtx->pVB->indexBuffer;
	D3DINDEXBUFFER_DESC desc;
	BYTE *bptr;

	indexBuffer->GetDesc(&desc);
	hr = indexBuffer->Lock(0, 0, (VOID**)&bptr,  0);
	if (FAILED(hr)) {
	    D3dCtx::d3dWarning(LOCKINDEXVBFAIL, hr);
	    return;
	}
	
	int i = indexCount;

	if (desc.Format == D3DFMT_INDEX16) {
	    USHORT *dst = (USHORT *) bptr;
	    
	    if (!expandQuadIndex) {
		while (--i >= 0) {
		    *dst++ = *src++;
		}
	    } else {
		USHORT *endptr = dst + (USHORT) 3*indexCount/2;

		while (dst < endptr) {
		    *dst++ = *src;   
		    *dst++ = *(src+1);
		    *dst++ = *(src+2);
		    *dst++ = *src;  
		    src++; 
		    src++; 
		    *dst++ = *src;
		    src++;
		    *dst++ = *src;  
		    src++;
		}
	    }
	} else {
	    UINT *dst = (UINT *) bptr;
	    
	    if (!expandQuadIndex) {
		while (--i >= 0) {
		    *dst++ = *src++;
		}
	    } else {
		UINT *endptr = dst + (UINT) 3*indexCount/2;
		while (dst < endptr) {
		    *dst++ = *src;   
		    *dst++ = *(src+1);
		    *dst++ = *(src+2);
		    *dst++ = *src;  
		    src++; 
		    src++; 
		    *dst++ = *src;
		    src++;
		    *dst++ = *src;  
		    src++;
		}

		dst = (UINT *) bptr;
	    }
	}
	
	indexBuffer->Unlock();
    }

    if (insertStrideToVB) {
	d3dCtx->pVB->addStride(d3dCtx->pVB->indexCount);
    }
}

// This function copy the stride vertex data into Vertex Buffer
// point by vbptr and update vbptr
void copyVertexToVB(D3dCtx *d3dCtx,
		    D3DDRAWPRIMITIVESTRIDEDDATA* strideData,
		    DWORD vcount,
		    float **vbptr,
		    jint cDirty,
		    BOOL insertStrideToVB, 
		    jdouble* xform,
		    jdouble* nxform)
{
    float *dst;
    float *src;
    double *dsrc;
    DWORD i;
    DWORD srcStride;
    DWORD currStride;

    DWORD dstStride = d3dCtx->pVB->stride >> 2;
    DWORD vertexFormat = d3dCtx->pVB->vertexFormat;
    float *endptr;

    // Copy Position
    if (cDirty & javax_media_j3d_GeometryArrayRetained_COORDINATE_CHANGED) {
	dst = *vbptr;
	// Before we call two times src++ in position and
	// normal copy so we only need to add dstStride - 2
	// at the end.
	srcStride = strideData->positionStride - 2;
	endptr = dst + vcount*dstStride;
	dstStride -= 2;
	src = strideData->fpositionPtr;

	if (xform == NULL) {
	    if (src != NULL) {
		while (dst < endptr) {
		    *dst++ = *src++;  // pos x
		    *dst++ = *src++;  // pos y
		    *dst   = *src;    // pos z
		    dst += dstStride;
		    src += srcStride;
		}
	    } else { 
		// double is used for position coordinate in executeVA()
		dsrc = strideData->dpositionPtr;
		while (dst < endptr) {
		    *dst++ = *dsrc++;  // pos x
		    *dst++ = *dsrc++;  // pos y
		    *dst   = *dsrc;    // pos z
		    dst += dstStride;
		    dsrc += srcStride;
		}
	    }
	} else {
	    if (src != NULL) {
		float x, y, z, w;
		while (dst < endptr) {
		    x = *src++;  
		    y = *src++; 
		    z = *src;    // pos z
		    w = 1/(xform[12]*x + xform[13]*y + xform[14]*z + xform[15]);
		    *dst++ = (xform[0]*x + xform[1]*y + xform[2]*z + xform[3])*w;
		    *dst++ = (xform[4]*x + xform[5]*y + xform[6]*z + xform[7])*w;
		    *dst   = (xform[8]*x + xform[9]*y + xform[10]*z + xform[11])*w;
		    dst += dstStride;
		    src += srcStride;
		}
	    } else { 
		double x, y, z, w;
		// double is used for position coordinate in executeVA()
		dsrc = strideData->dpositionPtr;
		while (dst < endptr) {
		    x = *src++;  
		    y = *src++; 
		    z = *src;    // pos z
		    w = 1/(xform[12]*x + xform[13]*y + xform[14]*z + xform[15]);
		    *dst++ = (xform[0]*x + xform[1]*y + xform[2]*z + xform[3])*w;
		    *dst++ = (xform[4]*x + xform[5]*y + xform[6]*z + xform[7])*w;
		    *dst   = (xform[8]*x + xform[9]*y + xform[10]*z + xform[11])*w;
		    dst += dstStride;
		    dsrc += srcStride;
		}
	    }

	}
	// restore value
	dstStride += 2;
    } 

    // Copy Normal
    if (vertexFormat & D3DFVF_NORMAL) { 
	if (cDirty & javax_media_j3d_GeometryArrayRetained_NORMAL_CHANGED) {
	    dst = *vbptr + 3;
	    src = strideData->normalPtr;
	    srcStride = strideData->normalStride - 2;
	    endptr = dst + vcount*dstStride;
	    dstStride -= 2;
	    if (nxform == NULL) {
		while (dst < endptr) {
		    *dst++ = *src++;  // norm x 
		    *dst++ = *src++;  // norm y
		    *dst   = *src;    // norm z
		    dst += dstStride;
		    src += srcStride;
		} 
	    } else {
		float nx, ny, nz, nw;
		while (dst < endptr) {
		    nx = *src++;  
		    ny = *src++; 
		    nz = *src;    // pos z
		    nw = 1/(nxform[12]*nx + nxform[13]*ny + nxform[14]*nz + nxform[15]);
		    *dst++ = (nxform[0]*nx + nxform[1]*ny + nxform[2]*nz + nxform[3])*nw;
		    *dst++ = (nxform[4]*nx + nxform[5]*ny + nxform[6]*nz + nxform[7])*nw;
		    *dst   = (nxform[8]*nx + nxform[9]*ny + nxform[10]*nz + nxform[11])*nw;
		    dst += dstStride;
		    dsrc += srcStride;
		}
	    }
	    // restore value
	    dstStride += 2;
	}
	// nx,ny,nz copy in addtion to x, y, z
	currStride = 6;
    } else {
	// This is used to keep track of the offset
	// from beginning of the current type copy.
	currStride = 3; // x,y,z already copy
    }

    // Copy Diffuse Color (DWORD & float are of the same size)

    if (vertexFormat & D3DFVF_DIFFUSE) {
	if (cDirty & javax_media_j3d_GeometryArrayRetained_COLOR_CHANGED) {

	    DWORD* wdst = (DWORD *) *vbptr + currStride;
	    DWORD* wendptr = wdst + vcount*dstStride;

	    if (strideData->fdiffusePtr) {
		float* wsrc = strideData->fdiffusePtr;
		float r, g, b, a;
		if ((d3dCtx->currDisplayListID <= 0) || 
		    !strideData->modulateAlpha) {
		    // execute() or executeVA()

		    if (strideData->useAlpha) {
			srcStride = strideData->diffuseStride - 3;
			while (wdst < wendptr) {
			    r = *wsrc++;
			    g = *wsrc++;
			    b = *wsrc++;
			    a = *wsrc;
			    *wdst = D3DCOLOR_COLORVALUE(r, g, b, a);
			    wdst += dstStride;
			    wsrc += srcStride;
			}
		    } else {
			srcStride = strideData->diffuseStride - 2;
			while (wdst < wendptr) {
			    r = *wsrc++;
			    g = *wsrc++;
			    b = *wsrc;
			    *wdst = D3DCOLOR_COLORVALUE(r, g, b, 0);
			    wdst += dstStride;
			    wsrc += srcStride;
			}			
		    }
		} else {  
		    // buildGA() & modulateAlpha
		    float alpha = strideData->alpha;
		    if (strideData->useAlpha) {
			srcStride = strideData->diffuseStride - 3;
			while (wdst < wendptr) {
			    r = *wsrc++;
			    g = *wsrc++;
			    b = *wsrc++;
			    a = *wsrc * alpha;
			    *wdst = D3DCOLOR_COLORVALUE(r, g, b, a);
			    wdst += dstStride;
			    wsrc += srcStride;
			}
		    } else {
			srcStride = strideData->diffuseStride - 2;
			while (wdst < wendptr) {
			    r = *wsrc++;
			    g = *wsrc++;
			    b = *wsrc;			    
			    *wdst = D3DCOLOR_COLORVALUE(r, g, b, alpha);
			    wdst += dstStride;
			    wsrc += srcStride;
			}			
		    }
		    
		}
	    } else {  // byte color pointer
		jbyte* wsrc = strideData->bdiffusePtr;
		jbyte r, g, b, a;
		if ((d3dCtx->currDisplayListID <= 0) || 
		    !strideData->modulateAlpha) {
		    // execute() or executeVA()

		    if (strideData->useAlpha) {
			srcStride = strideData->diffuseStride - 3;
			while (wdst < wendptr) {
			    r = *wsrc++;
			    g = *wsrc++;
			    b = *wsrc++;
			    a = *wsrc;			    
			    *wdst = D3DCOLOR_RGBA(r, g, b, a);
			    wdst += dstStride;
			    wsrc += srcStride;
			}
		    } else {
			srcStride = strideData->diffuseStride - 2;
			while (wdst < wendptr) {
			    r = *wsrc++;
			    g = *wsrc++;
			    b = *wsrc;			    
			    *wdst = D3DCOLOR_RGBA(r, g, b, 0);
			    wdst += dstStride;
			    wsrc += srcStride;
			}			
		    }
		} else {  
		    // buildGA() & modeulateAlpha
		    // Currently buildGA() will not use byte color
		    // so this code should never execute.
		    jbyte alpha = (jbyte)(255*strideData->alpha);
		    if (strideData->useAlpha) {
			srcStride = strideData->diffuseStride - 3;
			while (wdst < wendptr) {
			    r = *wsrc++;
			    g = *wsrc++;
			    b = *wsrc++;
			    a = (jbyte)(((int)(*wsrc) & 0xff) * strideData->alpha);
			    *wdst = D3DCOLOR_RGBA(r, g, b, a);
			    wdst += dstStride;
			    wsrc += srcStride;
			}
		    } else {
			srcStride = strideData->diffuseStride - 2;
			while (wdst < wendptr) {
			    r = *wsrc++;
			    g = *wsrc++;
			    b = *wsrc;			    
			    *wdst = D3DCOLOR_RGBA(r, g, b, alpha);
			    wdst += dstStride;
			    wsrc += srcStride;
			}			
		    }
		    
		}
	    }

	}
	
	currStride++; // additional one DWORD of color copy
    }

    // Copy Texture
    int ts;
    int texPos;
    boolean invalidTexCoord;

#ifdef TEXDEBUG
    printf("In copyVertexToVB TexSet Used %d\n", d3dCtx->texSetUsed);
#endif
    for (i=0; i < d3dCtx->texSetUsed; i++) {
	ts = d3dCtx->texStride[i];
	
	// TODO: skip when ts = 0
	if (ts == 0) {
	    continue;
	}
	texPos = strideData->texCoordPosition[i];

	invalidTexCoord = ((texPos != d3dCtx->pVB->texCoordPosition[i]) ||
	                   (texPos == TEX_OBJ_LINEAR));

#ifdef TEXDEBUG
	printf("%d texPos %d, invalidate Cached TexCoord %d, ts %d\n",i,  texPos, invalidTexCoord, ts);
#endif
	if ((cDirty & javax_media_j3d_GeometryArrayRetained_TEXTURE_CHANGED) || 
	    invalidTexCoord) {

	    if (texPos >= 0) {
		dst = *vbptr + currStride;
		src = strideData->textureCoordsPtr[i];
		endptr = dst + vcount*dstStride;
#ifdef TEXDEBUG
		printf("copy directly, ts %d\n", ts);
#endif
		if (ts == 2) {
		    dstStride--;
		    srcStride = strideData->textureCoordsStride[i] - 1;
		    while (dst < endptr) {
			*dst++ = *src++;  // tx
			*dst   = *src;    // ty
			dst += dstStride;
			src += srcStride;
		    }
		    dstStride++;
		} else if (ts == 3) { 
		    dstStride -= 2;
		    srcStride = strideData->textureCoordsStride[i] - 2;
		    while (dst < endptr) {
			*dst++ = *src++;  // tx
			*dst++ = *src++;  // ty
			*dst   = *src;    // tz
			dst += dstStride;
			src += srcStride;
		    }
		    dstStride += 2;
		} else {
		    // ts == 4
		    dstStride -= 3;
		    srcStride = strideData->textureCoordsStride[i] - 3;
		    while (dst < endptr) {
			*dst++ = *src++;  // tx
			*dst++ = *src++;  // ty
			*dst++ = *src++;  // tz
			*dst   = *src;    // tw
			dst += dstStride;
			src += srcStride;
		    }
		    dstStride += 3;
		}

	    } else {
		if (texPos == TEX_OBJ_LINEAR) {
		    // automatic texture generation for Object Linear
		    float *ps = d3dCtx->planeS[i];
		    float *pt = d3dCtx->planeT[i];
		    float *pr = d3dCtx->planeR[i];
		    float *pq = d3dCtx->planeQ[i];
#ifdef TEXDEBUG
		    printf("gen obj linear tex, ts %d\n", ts);
#endif
		    if (strideData->fpositionPtr) {
			float x, y, z;
			dst = *vbptr + currStride;
			endptr = dst + vcount*dstStride;
			src = strideData->fpositionPtr;
			srcStride = strideData->positionStride - 2;
			if (ts == 2) {
			    dstStride--;
			    if (xform == NULL) {
				while (dst < endptr) {
				    x = *src++;
				    y = *src++;
				    z = *src;
				    *dst++ = ps[0]*x + ps[1]*y + ps[2]*z + ps[3];
				    *dst   = pt[0]*x + pt[1]*y + pt[2]*z + pt[3];	
				    dst += dstStride;
				    src += srcStride;
				}
			    } else {
				float tx, ty, tz, tw;
				while (dst < endptr) {
				    tx = *src++;
				    ty = *src++;
				    tz = *src;
				    tw = 1/(xform[12]*x + xform[13]*y + xform[14]*z + xform[15]);
				    x = (xform[0]*tx + xform[1]*ty + xform[2]*tz + xform[3])*tw;
				    y = (xform[4]*tx + xform[5]*ty + xform[6]*tz + xform[7])*tw;
				    z  = (xform[8]*tx + xform[9]*ty + xform[10]*tz + xform[11])*tw;
				    *dst++ = ps[0]*x + ps[1]*y + ps[2]*z + ps[3];
				    *dst   = pt[0]*x + pt[1]*y + pt[2]*z + pt[3];	
				    dst += dstStride;
				    src += srcStride;	
				}			
			    }
			    dstStride++;
			} else if (ts == 3) {
			    dstStride -= 2;
			    if (xform == NULL) {
				while (dst < endptr) {
				    x = *src++;
				    y = *src++;
				    z = *src;
				    *dst++ = ps[0]*x + ps[1]*y + ps[2]*z + ps[3];
				    *dst++ = pt[0]*x + pt[1]*y + pt[2]*z + pt[3];	
				    *dst   = pr[0]*x + pr[1]*y + pr[2]*z + pr[3];
				    dst += dstStride;
				    src += srcStride;
				}
			    } else {
				float tx, ty, tz, tw;
				while (dst < endptr) {
				    tx = *src++;
				    ty = *src++;
				    tz = *src;
				    tw = 1/(xform[12]*x + xform[13]*y + xform[14]*z + xform[15]);
				    x = (xform[0]*tx + xform[1]*ty + xform[2]*tz + xform[3])*tw;
				    y = (xform[4]*tx + xform[5]*ty + xform[6]*tz + xform[7])*tw;
				    z  = (xform[8]*tx + xform[9]*ty + xform[10]*tz + xform[11])*tw;
				    *dst++ = ps[0]*x + ps[1]*y + ps[2]*z + ps[3];
				    *dst++ = pt[0]*x + pt[1]*y + pt[2]*z + pt[3];
				    *dst   = pr[0]*x + pr[1]*y + pr[2]*z + pr[3];	
				    dst += dstStride;
				    src += srcStride;	
				}			
			    }

			    dstStride += 2;
			} else {
			    // ts == 4 
			    dstStride -= 3;

			    if (!d3dCtx->texTransformSet[i]) {
				if (xform == NULL) {
				    while (dst < endptr) {
					x = *src++;
					y = *src++;
					z = *src;
					*dst++ = ps[0]*x + ps[1]*y + ps[2]*z + ps[3];
					*dst++ = pt[0]*x + pt[1]*y + pt[2]*z + pt[3];	
					*dst++ = pr[0]*x + pr[1]*y + pr[2]*z + pr[3];
					*dst   = pq[0]*x + pq[1]*y + pq[2]*z + pq[3];
					dst += dstStride;
					src += srcStride;
				    }
				} else {
				    float tx, ty, tz, tw;
				    while (dst < endptr) {
					tx = *src++;
					ty = *src++;
					tz = *src;
					tw = 1/(xform[12]*x + xform[13]*y + xform[14]*z + xform[15]);
					x = (xform[0]*tx + xform[1]*ty + xform[2]*tz + xform[3])*tw;
					y = (xform[4]*tx + xform[5]*ty + xform[6]*tz + xform[7])*tw;
					z  = (xform[8]*tx + xform[9]*ty + xform[10]*tz + xform[11])*tw;
					*dst++ = ps[0]*x + ps[1]*y + ps[2]*z + ps[3];
					*dst++ = pt[0]*x + pt[1]*y + pt[2]*z + pt[3];
					*dst++ = pr[0]*x + pr[1]*y + pr[2]*z + pr[3];	
					*dst   = pq[0]*x + pq[1]*y + pq[2]*z + pq[3];
					dst += dstStride;
					src += srcStride;	
				    }			
				}
			    } else {
				// do texture transform manually
				D3DXMATRIX *m = &(d3dCtx->texTransform[i]);
				double tx, ty, tz, tw;
				if (xform == NULL) {
				    while (dst < endptr) {
					x = *src++;
					y = *src++;
					z = *src;
					tx = ps[0]*x + ps[1]*y + ps[2]*z + ps[3];
					ty = pt[0]*x + pt[1]*y + pt[2]*z + pt[3];	
					tz = pr[0]*x + pr[1]*y + pr[2]*z + pr[3];
					tw = pq[0]*x + pq[1]*y + pq[2]*z + pq[3];
					*dst++ = (*m)._11*tx + (*m)._21*ty + (*m)._31*tz + (*m)._41*tw;
					*dst++ = (*m)._12*tx + (*m)._22*ty + (*m)._32*tz + (*m)._42*tw;
					*dst++ = (*m)._13*tx + (*m)._23*ty + (*m)._33*tz + (*m)._43*tw;
					*dst =   (*m)._14*tx + (*m)._24*ty + (*m)._34*tz + (*m)._44*tw;
					dst += dstStride;
					src += srcStride;
				    }
				} else {
				    float tx, ty, tz, tw;
				    while (dst < endptr) {
					tx = *src++;
					ty = *src++;
					tz = *src;
					tw = 1/(xform[12]*x + xform[13]*y + xform[14]*z + xform[15]);
					x = (xform[0]*tx + xform[1]*ty + xform[2]*tz + xform[3])*tw;
					y = (xform[4]*tx + xform[5]*ty + xform[6]*tz + xform[7])*tw;
					z  = (xform[8]*tx + xform[9]*ty + xform[10]*tz + xform[11])*tw;
					tx = ps[0]*x + ps[1]*y + ps[2]*z + ps[3];
					ty = pt[0]*x + pt[1]*y + pt[2]*z + pt[3];
					tz = pr[0]*x + pr[1]*y + pr[2]*z + pr[3];	
					tw = pq[0]*x + pq[1]*y + pq[2]*z + pq[3];
					*dst++ = (*m)._11*tx + (*m)._21*ty + (*m)._31*tz + (*m)._41*tw;
					*dst++ = (*m)._12*tx + (*m)._22*ty + (*m)._32*tz + (*m)._42*tw;
					*dst++ = (*m)._13*tx + (*m)._23*ty + (*m)._33*tz + (*m)._43*tw;
					*dst =   (*m)._14*tx + (*m)._24*ty + (*m)._34*tz + (*m)._44*tw;
					dst += dstStride;
					src += srcStride;	
				    }			
				}
			    }
			    dstStride += 3;
			}
		    } else {
			// double type position pointer
			double x, y, z;
			dst = *vbptr + currStride;
			endptr = dst + vcount*dstStride;
			dsrc = strideData->dpositionPtr;
			srcStride = strideData->positionStride - 2;
			if (ts == 2) {
			    dstStride--;
			    if (xform == NULL) {
				while (dst < endptr) {
				    x = *dsrc++;
				    y = *dsrc++;
				    z = *dsrc;
				    *dst++ = ps[0]*x + ps[1]*y + ps[2]*z + ps[3];
				    *dst   = pt[0]*x + pt[1]*y + pt[2]*z + pt[3];	
				    dst += dstStride;
				    dsrc += srcStride;
				}
			    } else {
				double tx, ty, tz, tw;
				while (dst < endptr) {
				    tx = *src++;
				    ty = *src++;
				    tz = *src;
				    tw = 1/(xform[12]*x + xform[13]*y + xform[14]*z + xform[15]);
				    x = (xform[0]*tx + xform[1]*ty + xform[2]*tz + xform[3])*tw;
				    y = (xform[4]*tx + xform[5]*ty + xform[6]*tz + xform[7])*tw;
				    z  = (xform[8]*tx + xform[9]*ty + xform[10]*tz + xform[11])*tw;
				    *dst++ = ps[0]*x + ps[1]*y + ps[2]*z + ps[3];
				    *dst   = pt[0]*x + pt[1]*y + pt[2]*z + pt[3];	
				    dst += dstStride;
				    src += srcStride;	
				}							
			    }
			    dstStride++;
			} else if (ts == 3) {
			    dstStride -= 2;
			    if (xform == NULL) {
				while (dst < endptr) {
				    x = *dsrc++;
				    y = *dsrc++;
				    z = *dsrc;
				    *dst++ = ps[0]*x + ps[1]*y + ps[2]*z + ps[3];
				    *dst++ = pt[0]*x + pt[1]*y + pt[2]*z + pt[3];	
				    *dst   = pr[0]*x + pr[1]*y + pr[2]*z + pr[3];
				    dst += dstStride;
				    dsrc += srcStride;
				}
			    } else {
				double tx, ty, tz, tw;
				while (dst < endptr) {
				    tx = *src++;
				    ty = *src++;
				    tz = *src;
				    tw = 1/(xform[12]*x + xform[13]*y + xform[14]*z + xform[15]);
				    x = (xform[0]*tx + xform[1]*ty + xform[2]*tz + xform[3])*tw;
				    y = (xform[4]*tx + xform[5]*ty + xform[6]*tz + xform[7])*tw;
				    z  = (xform[8]*tx + xform[9]*ty + xform[10]*tz + xform[11])*tw;
				    *dst++ = ps[0]*x + ps[1]*y + ps[2]*z + ps[3];
				    *dst++ = pt[0]*x + pt[1]*y + pt[2]*z + pt[3];
				    *dst   = pr[0]*x + pr[1]*y + pr[2]*z + pr[3];	
				    dst += dstStride;
				    src += srcStride;	
				}				
			    }
			    dstStride += 2;
			} else {
			    // ts == 4 
			    dstStride -= 3;
			    if (!d3dCtx->texTransformSet[i]) {
				if (xform == NULL) {
				    while (dst < endptr) {
					x = *dsrc++;
					y = *dsrc++;
					z = *dsrc;
					*dst++ = ps[0]*x + ps[1]*y + ps[2]*z + ps[3];
					*dst++ = pt[0]*x + pt[1]*y + pt[2]*z + pt[3];	
					*dst++ = pr[0]*x + pr[1]*y + pr[2]*z + pr[3];
					*dst   = pq[0]*x + pq[1]*y + pq[2]*z + pq[3];
					dst += dstStride;
					dsrc += srcStride;
				    }
				} else {
				    double tx, ty, tz, tw;
				    while (dst < endptr) {
					tx = *src++;
					ty = *src++;
					tz = *src;
					tw = 1/(xform[12]*x + xform[13]*y + xform[14]*z + xform[15]);
					x = (xform[0]*tx + xform[1]*ty + xform[2]*tz + xform[3])*tw;
					y = (xform[4]*tx + xform[5]*ty + xform[6]*tz + xform[7])*tw;
					z  = (xform[8]*tx + xform[9]*ty + xform[10]*tz + xform[11])*tw;
					*dst++ = ps[0]*x + ps[1]*y + ps[2]*z + ps[3];
					*dst++ = pt[0]*x + pt[1]*y + pt[2]*z + pt[3];
					*dst++ = pr[0]*x + pr[1]*y + pr[2]*z + pr[3];	
					*dst   = pq[0]*x + pq[1]*y + pq[2]*z + pq[3];
					dst += dstStride;
					src += srcStride;	
				    }						
				}
			    } else {
// do texture transform manually
				D3DXMATRIX *m = &(d3dCtx->texTransform[i]);
				double tx, ty, tz, tw;
				if (xform == NULL) {
				    while (dst < endptr) {
					x = *src++;
					y = *src++;
					z = *src;
					tx = ps[0]*x + ps[1]*y + ps[2]*z + ps[3];
					ty = pt[0]*x + pt[1]*y + pt[2]*z + pt[3];	
					tz = pr[0]*x + pr[1]*y + pr[2]*z + pr[3];
					tw = pq[0]*x + pq[1]*y + pq[2]*z + pq[3];
					*dst++ = ((*m)._11*tx + (*m)._21*ty + (*m)._31*tz + (*m)._41*tw);
					*dst++ = ((*m)._12*tx + (*m)._22*ty + (*m)._32*tz + (*m)._42*tw);
					*dst++ = ((*m)._13*tx + (*m)._23*ty + (*m)._33*tz + (*m)._43*tw);
					*dst = (*m)._14*tx + (*m)._24*ty + (*m)._34*tz + (*m)._44*tw;
					dst += dstStride;
					src += srcStride;
				    }
				} else {
				    float tx, ty, tz, tw;
				    while (dst < endptr) {
					tx = *src++;
					ty = *src++;
					tz = *src;
					tw = 1/(xform[12]*x + xform[13]*y + xform[14]*z + xform[15]);
					x = (xform[0]*tx + xform[1]*ty + xform[2]*tz + xform[3])*tw;
					y = (xform[4]*tx + xform[5]*ty + xform[6]*tz + xform[7])*tw;
					z  = (xform[8]*tx + xform[9]*ty + xform[10]*tz + xform[11])*tw;
					tx = ps[0]*x + ps[1]*y + ps[2]*z + ps[3];
					ty = pt[0]*x + pt[1]*y + pt[2]*z + pt[3];
					tz = pr[0]*x + pr[1]*y + pr[2]*z + pr[3];	
					tw = pq[0]*x + pq[1]*y + pq[2]*z + pq[3];
					*dst++ = ((*m)._11*tx + (*m)._21*ty + (*m)._31*tz + (*m)._41*tw);
					*dst++ = ((*m)._12*tx + (*m)._22*ty + (*m)._32*tz + (*m)._42*tw);
					*dst++ = ((*m)._13*tx + (*m)._23*ty + (*m)._33*tz + (*m)._43*tw);
					*dst = (*m)._14*tx + (*m)._24*ty + (*m)._34*tz + (*m)._44*tw;
					dst += dstStride;
					src += srcStride;	
				    }			
				}
			    }
			    dstStride += 3;
 			}
		    }
		} else if (texPos == TEX_GEN_INVALID) {
		    // application error, disable by setting texCoord to zero
#ifdef TEXDEBUG
		    printf("app error, ts %d\n", ts);
#endif
		    dst = *vbptr + currStride;
		    endptr = dst + vcount*dstStride;
		    if (ts == 2) {
			dstStride--;
			while (dst < endptr) {
			    *dst++ = 0;
			    *dst = 0;
			    dst += dstStride;
			}
			dstStride++;
		    } else if (ts == 3) {
			dstStride -= 2;
			while (dst < endptr) {
			    *dst++ = 0;
			    *dst++ = 0;
			    *dst = 0;
			    dst += dstStride;
			}
			dstStride += 2;			
		    } else {
			// ts == 4
			dstStride -= 3;
			while (dst < endptr) {
			    *dst++ = 0;
			    *dst++ = 0;
			    *dst++ = 0;
			    *dst = 0;
			    dst += dstStride;
			}
			dstStride += 3;						
		    }
		} else {
		    // Other automatic texture generation type handle
		    // by driver
		    //printf("TexStage %d, Tex gen by driver, texPos = %d\n", i, texPos);
		}
	    }
	}
	
	currStride += ts;
    }
        
    if (insertStrideToVB) {
	d3dCtx->pVB->addStride(vcount);
    }
    
    // Adjust VB pointer so that when this function is invoked
    // again, it append to the VB correctly.
    *vbptr += (vcount*dstStride);
}


void copyOneVertexToVB(D3dCtx *d3dCtx,
		       float **vbptr, 
		       D3DDRAWPRIMITIVESTRIDEDDATA* strideData,
		       DWORD idx, 
		       jint cDirty,
		       jdouble* xform,
		       jdouble* nxform)
{
    float *src;
    float *dst = *vbptr;
    DWORD vertexFormat = d3dCtx->pVB->vertexFormat;
    float posX, posY, posZ;

    // Copy Position

    //    if (cDirty & javax_media_j3d_GeometryArrayRetained_COORDINATE_CHANGED) 
    // Set the posX, posY, posZ anyway since TexGeneration will use it
    // if dirty.

    if (strideData->fpositionPtr != NULL) {
	src = strideData->fpositionPtr +
	    idx*strideData->positionStride;
	
	if (xform == NULL) {
	    posX = *src++;     // pos x
	    posY =  *src++;    // pos y
	    posZ = *src;       // pos z
	} else {
	    float x, y, z, w;
	    x = *src++;  
	    y = *src++; 
	    z = *src;    
	    w = 1/(xform[12]*x + xform[13]*y + xform[14]*z + xform[15]);
	    posX =  (xform[0]*x + xform[1]*y + xform[2]*z + xform[3])*w;
	    posY =  (xform[4]*x + xform[5]*y + xform[6]*z + xform[7])*w;
	    posZ = (xform[8]*x + xform[9]*y + xform[10]*z + xform[11])*w;
	}
    } else { 
	// double is used for position coordinate in executeVA()
	double *dsrc = strideData->dpositionPtr + 
	    idx*strideData->positionStride;
	
	if (xform == NULL) {
	    posX = (float) *dsrc++;     // pos x
	    posY = (float) *dsrc++;    // pos y
	    posZ = (float) *dsrc;       // pos z
	} else {
	    double x, y, z, w;
	    x = *dsrc++;  
	    y = *dsrc++; 
	    z = *dsrc;    
	    w = 1/(xform[12]*x + xform[13]*y + xform[14]*z + xform[15]);
	    posX =  (float) (xform[0]*x + xform[1]*y + xform[2]*z + xform[3])*w;
	    posY =  (float) (xform[4]*x + xform[5]*y + xform[6]*z + xform[7])*w;
	    posZ = (float) (xform[8]*x + xform[9]*y + xform[10]*z + xform[11])*w;
	}
    }
    *dst++ = posX;
    *dst++ = posY;
    *dst++ = posZ;

    // Copy Normal
    if (vertexFormat & D3DFVF_NORMAL) { 
	if (cDirty & javax_media_j3d_GeometryArrayRetained_NORMAL_CHANGED) {
	    src = strideData->normalPtr +
		idx*strideData->normalStride;
	    if (nxform == NULL) {
		*dst++ = *src++;    // norm x
		*dst++ = *src++;    // norm y
		*dst++ = *src;      // norm z	    
	    } else {
		float nx, ny, nz, nw;
		nx = *src++;    // norm x
		ny = *src++;    // norm y
		nz = *src;      // norm z	    
		nw = 1/(nxform[12]*nx + nxform[13]*ny + nxform[14]*nz + nxform[15]);
		*dst++ = (nxform[0]*nx + nxform[1]*ny + nxform[2]*nz + nxform[3])*nw;
		*dst++ = (nxform[4]*nx + nxform[5]*ny + nxform[6]*nz + nxform[7])*nw;
		*dst   = (nxform[8]*nx + nxform[9]*ny + nxform[10]*nz + nxform[11])*nw;
	    }
	} 
    }


    // Copy Diffuse Color (DWORD & float are of the same size)
    if (vertexFormat & D3DFVF_DIFFUSE) {
	if (cDirty & javax_media_j3d_GeometryArrayRetained_COLOR_CHANGED) {
	    DWORD* wdst = (DWORD *) dst;
	    if (strideData->fdiffusePtr) {
		src = strideData->fdiffusePtr +
		    idx*strideData->diffuseStride;
		float r, g, b, a;
		if ((d3dCtx->currDisplayListID <= 0) || 
		    !strideData->modulateAlpha) {
		    // execute() or executeVA()
		    if (strideData->useAlpha) {
			r = *src++;
			g = *src++;
			b = *src++;
			a = *src;
		    } else {
			r = *src++;
			g = *src++;
			b = *src;
			a = 0;
		    }
		} else {  
		    // buildGA() & modeulateAlpha
		    if (strideData->useAlpha) {
			r = *src++;
			g = *src++;
			b = *src++;
			a = *src * strideData->alpha;
		    } else {
			r = *src++;
			g = *src++;
			b = *src;			    
			a = strideData->alpha;
		    }
		}
		*wdst = D3DCOLOR_COLORVALUE(r, g, b, a);
	    } else {  // byte color pointer
		jbyte* wsrc = strideData->bdiffusePtr +
		    idx*strideData->diffuseStride;
		jbyte r, g, b, a;
		if ((d3dCtx->currDisplayListID <= 0) || 
		    !strideData->modulateAlpha) {
		    // execute() or executeVA()
		    if (strideData->useAlpha) {
			r = *wsrc++;
			g = *wsrc++;
			b = *wsrc++;
			a = *wsrc;
		    } else {
			r = *wsrc++;
			g = *wsrc++;
			b = *wsrc;
			a = 0;
		    }
		} else {  
		    // buildGA() & modeulateAlpha
		    // Currently buildGA() will not use byte color
		    // so this code should never execute.
		    jbyte alpha = (jbyte) (255*strideData->alpha);
		    if (strideData->useAlpha) {
			r = *wsrc++;
			g = *wsrc++;
			b = *wsrc++;
			a = (jbyte)(((int)(*wsrc) & 0xff) * strideData->alpha);
		    } else {
			r = *wsrc++;
			g = *wsrc++;
			b = *wsrc;		
			a = alpha;

		    }
		}
		*wdst = D3DCOLOR_RGBA(r, g, b, a);
	    }
	}
	dst++; // additional one DWORD of color copy
    }


  // Copy Texture
    int ts;
    int texPos;
    boolean invalidTexCoord;

    for (int i=0; i < d3dCtx->texSetUsed; i++) {
	ts = d3dCtx->texStride[i];
	if (ts == 0) {
	    continue;
	}
	texPos = strideData->texCoordPosition[i];
	
	invalidTexCoord = ((texPos != d3dCtx->pVB->texCoordPosition[i]) ||
			   (texPos == TEX_OBJ_LINEAR));
	
	if ((cDirty & javax_media_j3d_GeometryArrayRetained_TEXTURE_CHANGED) || invalidTexCoord) {
	    if (texPos >= 0) {
		src = strideData->textureCoordsPtr[i] +
		    idx*strideData->textureCoordsStride[i];
		*dst++ = *src++;  // tx
		*dst++ = *src++;  // ty
		if (ts >= 3) { 
		    *dst++ = *src++;  // tx
		    if (ts >= 4) {
			*dst++ = *src;  // tx
		    }
		}
	    } else {
		// automatic texture generation
		if (texPos == TEX_OBJ_LINEAR) {
		    float *ps = d3dCtx->planeS[i];
		    float *pt = d3dCtx->planeT[i];
		    float *pr = d3dCtx->planeR[i];
		    float *pq = d3dCtx->planeQ[i];

		    if ((ts < 4) || (!d3dCtx->texTransformSet[i])) {
			*dst++ = ps[0]*posX + ps[1]*posY + ps[2]*posZ + ps[3];
			*dst++ = pt[0]*posX + pt[1]*posY + pt[2]*posZ + pt[3];	
			if (ts >= 3) {
			    *dst++  = pr[0]*posX + pr[1]*posY + pr[2]*posZ + pr[3];
			    if (ts >= 4) {
				*dst++ = pq[0]*posX + pq[1]*posY + pq[2]*posZ + pq[3];
			    }
			}
		    } else {
			float tx, ty, tz, tw;
			D3DXMATRIX *m = &(d3dCtx->texTransform[i]);
			tx = ps[0]*posX + ps[1]*posY + ps[2]*posZ + ps[3];
			ty = pt[0]*posX + pt[1]*posY + pt[2]*posZ + pt[3];
			tz = pr[0]*posX + pr[1]*posY + pr[2]*posZ + pr[3];				
			tw = pq[0]*posX + pq[1]*posY + pq[2]*posZ + pq[3];
			*dst++ = (*m)._11*tx + (*m)._21*ty + (*m)._31*tz + (*m)._41*tw;
			*dst++ = (*m)._12*tx + (*m)._22*ty + (*m)._32*tz + (*m)._42*tw;
			*dst++ = (*m)._13*tx + (*m)._23*ty + (*m)._33*tz + (*m)._43*tw;
			*dst++ = (*m)._14*tx + (*m)._24*ty + (*m)._34*tz + (*m)._44*tw;
		    }
		} else if (texPos == TEX_GEN_INVALID) {
		    // application error, disable by setting texCoord to zero
		    *dst++ = 0;
		    *dst++ = 0;
		    if (ts >= 3) {
			*dst++ = 0;
			if (ts >= 4) {
			    *dst++ = 0;
			}
		    }
		} else {
		    // should not happen
		    dst += ts;
		}
	    }
	} else {
	    dst += ts;
	}
    }

    *vbptr = dst;
}


float* allocateVB(D3dCtx *d3dCtx, 
		  LPDIRECT3DDEVICE9 device,
		  int vcount,
		  int maxVertexLimit,
		  jint *cdirty)
{
    LPD3DVERTEXBUFFER vb = d3dCtx->pVB->nextVB;
    HRESULT hr;
    float *ptr = NULL;


    if (vcount > maxVertexLimit) {
	vcount = maxVertexLimit;
    }
    
    if ((vb != NULL) && (vb->vcount < vcount)) {
	delete vb;
	d3dCtx->pVB->nextVB = NULL;
	vb = NULL;
    }

    if (vb == NULL) {
	vb = new D3dVertexBuffer();
	if (vb == NULL) {
	    D3dCtx::d3dWarning(OUTOFMEMORY);
	    return NULL;
	}

	vb->stride = d3dCtx->pVB->stride;
	vb->vertexFormat = d3dCtx->pVB->vertexFormat;
	// Don't set totalVertexCount
	vb->isIndexPrimitive = d3dCtx->pVB->isIndexPrimitive;
	vb->primitiveType = d3dCtx->pVB->primitiveType;
	vb->isPointFlagUsed = d3dCtx->pVB->isPointFlagUsed;
	vb->vcount = vcount;
	vb->maxVertexLimit = maxVertexLimit;

#ifdef VBDEBUG
	printf("Create secondary VertexBuffer of size %d, display list ID %d, pointFlag %d\n", 
	       vb->vcount, d3dCtx->currDisplayListID, vb->isPointFlagUsed);
#endif
	if (!vb->isPointFlagUsed) {
	    hr = device->CreateVertexBuffer(vb->stride*vcount,
					    D3DUSAGE_WRITEONLY,
					    vb->vertexFormat,
					    D3DPOOL_DEFAULT,
					    &vb->buffer,
						NULL);
	} else {
	    hr = device->CreateVertexBuffer(vb->stride*vcount,
					    D3DUSAGE_WRITEONLY|D3DUSAGE_POINTS,
					    vb->vertexFormat,
					    D3DPOOL_DEFAULT,
					    &vb->buffer,
						NULL);	    
	}
	if (FAILED(hr)) {
	    vb->buffer = NULL;
	    delete vb;
	    D3dCtx::d3dWarning(CREATEVBFAIL, hr);
	    return NULL;
	}
	d3dCtx->pVB->nextVB = vb;
	*cdirty = javax_media_j3d_GeometryArrayRetained_VERTEX_CHANGED;
    }

    hr = vb->buffer->Lock(0, 0,(VOID**) &ptr,  0);

    if (FAILED(hr)) {
	D3dCtx::d3dWarning(LOCKVBFAIL, hr);
	delete vb;
	d3dCtx->pVB->nextVB = NULL;
	return NULL;
    }
	
    d3dCtx->pVB = vb;    

    vb->stripLen = 0;
    return ptr;
}



BOOL createCopyVBVertex(D3dCtx *d3dCtx,
			LPDIRECT3DDEVICE9 device,
			D3DDRAWPRIMITIVESTRIDEDDATA *strideData,
			int vcount, jint cDirty,
			jdouble* xform,
			jdouble* nxform)
{
    LPD3DVERTEXBUFFER vb = d3dCtx->pVB;   
    float *vbptr;


    if (vb->stripLen > 0) {
	// VertexBuffer already used, create a new one or used
	// the next VB in the list 
	// maxVertexLimit is already check before, so we can
	// pass vcount as maxVertexLimit
	vbptr = allocateVB(d3dCtx, device, vcount, vcount, &cDirty);
	if (vbptr == NULL) {
	    return FALSE;
	}

    } else {
	// use the same VB
	HRESULT hr;
	hr = vb->buffer->Lock(0, 0, (VOID**)&vbptr, 0);

	if (FAILED(hr)) {
	    D3dCtx::d3dWarning(LOCKVBFAIL, hr);
	    return FALSE;
	}
	
    }

    copyVertexToVB(d3dCtx, strideData, vcount, &vbptr, cDirty, true,
		   xform, nxform);

    d3dCtx->pVB->buffer->Unlock();
    return TRUE;
}


/*
 * Draw Primitive with vertexCount > D3DMAXNUMVERTICES.
 * In this case we call the drawing routine multiple times.
 */
void splitVertexToMultipleVB(D3dCtx *d3dCtx,
			     LPD3DDRAWPRIMITIVESTRIDEDDATA strideData,
			     int vcount,
			     int maxVertexLimit,
			     jint cDirty,
			     jdouble* xform,
			     jdouble* nxform)
{
    int i, inc;
    int min = 0;
    int max = 0;
    jfloat *oldfPosition;
    double *olddPosition;
    jfloat *oldNormal;
    jfloat *oldfDiffuse;
    jbyte  *oldbDiffuse;
    float* oldTexCoords[D3DDP_MAXTEXCOORD];
    int vc;
    int texSetUsed = d3dCtx->texSetUsed;
    LPDIRECT3DDEVICE9 device = d3dCtx->pDevice;
    jfloat fr, fg, fb, fa;
    jbyte br, bg, bb, ba;
    boolean success;

    DWORD vertexFormat = d3dCtx->pVB->vertexFormat;

    // save stride pointers since strip set may have
    // multiple call to this procedure.
    oldfPosition = strideData->fpositionPtr;
    olddPosition = strideData->dpositionPtr;
    oldNormal    = strideData->normalPtr;
    oldfDiffuse  = strideData->fdiffusePtr;
    oldbDiffuse  = strideData->bdiffusePtr;    

    for (i=0; i < texSetUsed; i++) {
	oldTexCoords[i] = strideData->textureCoordsPtr[i];
    }



    switch (d3dCtx->pVB->primitiveType) {
        case D3DPT_TRIANGLEFAN:
	  {
	    // Copy the very first vertx and repeat the last vertex
	    jfloat fx, fy, fz, nx, ny, nz;
	    jdouble dx, dy, dz;
	    jfloat tx[D3DDP_MAXTEXCOORD];
	    jfloat ty[D3DDP_MAXTEXCOORD];
	    jfloat tz[D3DDP_MAXTEXCOORD];
	    jfloat tw[D3DDP_MAXTEXCOORD];
	    inc = maxVertexLimit - 2;

	    if (oldfPosition) {
		fx = *oldfPosition;
		fy = *(oldfPosition+1);
		fz = *(oldfPosition+2);	
	    } else {
		// must be double, since this routine will
		// not invoke when there is no position available
		dx = *olddPosition;
		dy = *(olddPosition+1);
		dz = *(olddPosition+2);	
	    }
    
	    if (oldNormal) {
		nx = *oldNormal;
		ny = *(oldNormal+1);
		nz = *(oldNormal+2);	    
	    }
	    if (oldfDiffuse) {
		fr =  *oldfDiffuse;
		fg =  *(oldfDiffuse+1);
		fb =  *(oldfDiffuse+2);
		if (strideData->useAlpha) {
		    fa = *(oldfDiffuse+3);
		}
	    } else if (oldbDiffuse) {
		br =  *oldbDiffuse;
		bg =  *(oldbDiffuse+1);
		bb =  *(oldbDiffuse+2);
		if (strideData->useAlpha) {
		    ba = *(oldbDiffuse+3);
		}		
	    }

	    for (i=0; i < texSetUsed; i++) {	
		tx[i] = *oldTexCoords[i];
		ty[i] = *(oldTexCoords[i]+1);
		if (d3dCtx->texStride[i] > 2) {
		    tz[i] = *(oldTexCoords[i]+2);
		    if (d3dCtx->texStride[i] > 3) {
			tw[i] = *(oldTexCoords[i]+3);
		    }
		}
	    }
	    while (true) {
		vc = (vcount >= maxVertexLimit ? maxVertexLimit : vcount);
		
		success = createCopyVBVertex(d3dCtx, device, strideData,
					     vc, cDirty, xform, nxform);
		// restore old values
		if (oldfPosition) {
		    *(strideData->fpositionPtr)   = fx;
		    *(strideData->fpositionPtr+1) = fy;
		    *(strideData->fpositionPtr+2) = fz;		
		} else {
		    *(strideData->dpositionPtr)   = dx;
		    *(strideData->dpositionPtr+1) = dy;
		    *(strideData->dpositionPtr+2) = dz;		
		}
		if (oldNormal) {
		    *(strideData->normalPtr)   = nx;
		    *(strideData->normalPtr+1) = ny;
		    *(strideData->normalPtr+2) = nz;
		}
		if (oldfDiffuse) {		
		    *(strideData->fdiffusePtr) = fr;
		    *(strideData->fdiffusePtr+1) = fg;
		    *(strideData->fdiffusePtr+2) = fb;
		    if (strideData->useAlpha) {
			*(strideData->fdiffusePtr+3) = fa;
		    }
		} else if (oldbDiffuse) {
		    *(strideData->bdiffusePtr) = br;
		    *(strideData->bdiffusePtr+1) = bg;
		    *(strideData->bdiffusePtr+2) = bb;
		    if (strideData->useAlpha) {
			*(strideData->bdiffusePtr+3) = ba;
		    }
		}
		for (i=0; i < texSetUsed; i++) {	    
		    *(strideData->textureCoordsPtr[i]) = tx[i];
		    *(strideData->textureCoordsPtr[i]+1) = ty[i];
		    if (d3dCtx->texStride[i] > 2) {
			*(strideData->textureCoordsPtr[i]+2) = tz[i];
			if (d3dCtx->texStride[i] > 3) {
			    *(strideData->textureCoordsPtr[i]+3) = tw[i];
			}
		    }
		}
		
		vcount -= inc;
		if (!success || (vcount <= 2)) {
		    break;
		}

		if (oldfPosition) {
		    strideData->fpositionPtr += strideData->positionStride*inc;
		    fx = *strideData->fpositionPtr;
		    *strideData->fpositionPtr = *oldfPosition;
		    fy = *(strideData->fpositionPtr+1);
		    *(strideData->fpositionPtr+1) = *(oldfPosition+1);		
		    fz = *(strideData->fpositionPtr+2);
		    *(strideData->fpositionPtr+2) = *(oldfPosition+2);		
		} else {
		    strideData->dpositionPtr += strideData->positionStride*inc;
		    dx = *strideData->dpositionPtr;
		    *strideData->dpositionPtr = *olddPosition;
		    dy = *(strideData->dpositionPtr+1);
		    *(strideData->dpositionPtr+1) = *(olddPosition+1);		
		    dz = *(strideData->dpositionPtr+2);
		    *(strideData->dpositionPtr+2) = *(olddPosition+2);		
		}


		if (oldNormal) {
		    strideData->normalPtr += strideData->normalStride*inc;
		    nx = *strideData->normalPtr;
		    *strideData->normalPtr = *oldNormal;
		    ny = *(strideData->normalPtr+1);
		    *(strideData->normalPtr+1) = *(oldNormal+1);		
		    nz = *(strideData->normalPtr+2);
		    *(strideData->normalPtr+2) = *(oldNormal+2);		
		}

		if (oldfDiffuse) {
		    strideData->fdiffusePtr += strideData->diffuseStride*inc;
		    fr =  *strideData->fdiffusePtr;
		    *strideData->fdiffusePtr = *oldfDiffuse;
		    fg =  *(strideData->fdiffusePtr+1);
		    *(strideData->fdiffusePtr+1) = *(oldfDiffuse+1);
		    fb =  *(strideData->fdiffusePtr+2);
		    *(strideData->fdiffusePtr+2) = *(oldfDiffuse+2);
		    if (strideData->useAlpha) {
			fa =  *(strideData->fdiffusePtr+3);
			*(strideData->fdiffusePtr+3) = *(oldfDiffuse+3);
		    }
		} else if (oldbDiffuse) {
		    strideData->bdiffusePtr += strideData->diffuseStride*inc;
		    br =  *strideData->bdiffusePtr;
		    *strideData->bdiffusePtr = *oldbDiffuse;
		    bg =  *(strideData->bdiffusePtr+1);
		    *(strideData->bdiffusePtr+1) = *(oldbDiffuse+1);
		    bb =  *(strideData->bdiffusePtr+2);
		    *(strideData->bdiffusePtr+2) = *(oldbDiffuse+2);
		    if (strideData->useAlpha) {
			ba =  *(strideData->bdiffusePtr+3);
			*(strideData->bdiffusePtr+3) = *(oldbDiffuse+3);
		    }
		}

		for (i=0; i < texSetUsed; i++) {
		    strideData->textureCoordsPtr[i] +=
			strideData->textureCoordsStride[i]*inc;

		    tx[i] = *strideData->textureCoordsPtr[i];
		    ty[i] = *(strideData->textureCoordsPtr[i]+1);
		    *(strideData->textureCoordsPtr[i]) = *oldTexCoords[i];
		    *(strideData->textureCoordsPtr[i]+1) = *(oldTexCoords[i]+1);
		    if (d3dCtx->texStride[i] > 2) {
			tz[i] =  *(strideData->textureCoordsPtr[i]+2);
			*(strideData->textureCoordsPtr[i]+2)
			    = *(oldTexCoords[i]+ 2);
			if (d3dCtx->texStride[i] > 3) {
			    tw[i] =  *(strideData->textureCoordsPtr[i]+3);
			    *(strideData->textureCoordsPtr[i]+3)
				= *(oldTexCoords[i]+ 3);
			}
			
		    }
		}

	    }
	    break;
	  }
    case D3DPT_POINTLIST:
	if (max == 0) {
	    max = maxVertexLimit;
	}
	// fall through
    case D3DPT_LINESTRIP:
	if (max == 0) {
	    max = maxVertexLimit;
	    min = 1; // repeat the last vertex;
	}
	// fall through	
    case D3DPT_TRIANGLELIST:
	if (max == 0) {
	    if (d3dCtx->pVB->isIndexPrimitive) {
		// QuadArray
		max = maxVertexLimit - (maxVertexLimit % 4);
	    } else {
		max = maxVertexLimit - (maxVertexLimit % 3);
	    }
	}
	// fall through
    case D3DPT_LINELIST:
	if (max == 0) {
	    max = maxVertexLimit - (maxVertexLimit % 2);
	}
	// fall through
    case D3DPT_TRIANGLESTRIP:
	if (max == 0) {
	    max = maxVertexLimit - (maxVertexLimit % 4);
	    min = 2;  // repeat the last two vertices
	}
	inc = max - min;

	while (true) {
	    vc = (vcount >= max ? max : vcount);

	    if (!createCopyVBVertex(d3dCtx, device, strideData, vc,
				    cDirty, xform, nxform)) {
		break;
	    } 

	    vcount -= inc;
	    if (vcount <= min) {
		break;
	    }
	    if (oldfPosition) {
		strideData->fpositionPtr += strideData->positionStride*inc;
	    } else {
		strideData->dpositionPtr += strideData->positionStride*inc;
	    }

	    if (oldNormal) {
		strideData->normalPtr += strideData->normalStride*inc;
	    }
	    if (oldfDiffuse) {
		strideData->fdiffusePtr += strideData->diffuseStride*inc;
	    } else if (oldbDiffuse) {
		strideData->bdiffusePtr += strideData->diffuseStride*inc;
	    }
	    for (i=0; i < texSetUsed; i++) {
		strideData->textureCoordsPtr[i] +=
		    strideData->textureCoordsStride[i]*inc;
	    }
	}
	break;
    }

    // Restore old pointers;
    strideData->fpositionPtr = oldfPosition;
    strideData->dpositionPtr = olddPosition;
    strideData->normalPtr    = oldNormal;
    strideData->fdiffusePtr  = oldfDiffuse;
    strideData->bdiffusePtr  = oldbDiffuse;

    for (i=0; i < texSetUsed; i++) {
	strideData->textureCoordsPtr[i] = oldTexCoords[i];
    }
}

		
BOOL reIndexifyIndexVertexToVBs(D3dCtx *d3dCtx, 
				D3DDRAWPRIMITIVESTRIDEDDATA* strideData, 
				DWORD indexCount,
				DWORD vcount,
				jint cDirty,
				BOOL expandQuadIndex,
				DWORD maxVertexLimit,
				jdouble* xform,
				jdouble* nxform) 
{
    LPD3DVERTEXBUFFER vb = d3dCtx->pVB;
    HRESULT hr;
    LPDIRECT3DDEVICE9 device = d3dCtx->pDevice;

    int vbSize;

    if (!expandQuadIndex) {
	vbSize = indexCount;
    } else {
	vbSize = (3*indexCount) >> 1;
    }    

    if (vb->stripLen > 0) {
	// VertexBuffer already used, create a new one or used
	// the next VB in the list 
	// maxVertexLimit is already check before, so we can
	// pass indexCount as maxVertexLimit.
	// The maximum vertex that can happens is equal
	// to indexCount so we can just set vcount = indexCount
	vb = vb->nextVB;
	if ((vb != NULL) && (vb->vcount < vbSize)) {
	    delete vb;
	    d3dCtx->pVB->nextVB = NULL;
	    vb = NULL;
	}
	
	if (vb == NULL) {
	     vb = new D3dVertexBuffer();
	     if (vb == NULL) {
		 D3dCtx::d3dWarning(OUTOFMEMORY);
		 return false;
	     }

	     vb->stride = d3dCtx->pVB->stride;
	     vb->vertexFormat = d3dCtx->pVB->vertexFormat;
	     // Don't set totalVertexCount 
	     vb->isIndexPrimitive = d3dCtx->pVB->isIndexPrimitive;
	     vb->primitiveType = d3dCtx->pVB->primitiveType;
	     vb->isPointFlagUsed = d3dCtx->pVB->isPointFlagUsed;
	     vb->vcount = vbSize;
	     vb->maxVertexLimit = maxVertexLimit;
	     
#ifdef VBDEBUG
	     printf("Create secondary VertexBuffer of size %d, display list ID %d, pointFlag %d\n", 
		    vbSize, d3dCtx->currDisplayListID, vb->isPointFlagUsed);
#endif
	     
	     if (!vb->isPointFlagUsed) {
		 hr = device->CreateVertexBuffer(vb->stride*vbSize,
						 D3DUSAGE_WRITEONLY,
						 vb->vertexFormat,
						 D3DPOOL_DEFAULT,
						 &vb->buffer,
						 NULL);
	     } else {
		 hr = device->CreateVertexBuffer(vb->stride*vbSize,
						 D3DUSAGE_WRITEONLY|D3DUSAGE_POINTS,
						 vb->vertexFormat,
						 D3DPOOL_DEFAULT,
						 &vb->buffer,
						 NULL);
		 vb->isPointFlagUsed = true;
	     }
	     
	     if (FAILED(hr)) {
		 vb->buffer = NULL;
		 vb->release();
		 D3dCtx::d3dWarning(CREATEVBFAIL, hr);
		 return false;
	     }
	     d3dCtx->pVB->nextVB = vb;
	     cDirty = javax_media_j3d_GeometryArrayRetained_VERTEX_CHANGED;
	}
    }

    if (vb->indexBuffer == NULL) {
	// No need to set totalIndexCount
	vb->indexCount = vbSize;

	if (indexCount <= 0xffff) {	
	    hr = device->CreateIndexBuffer(vbSize*sizeof(WORD),
					   D3DUSAGE_WRITEONLY,
					   D3DFMT_INDEX16,   
					   D3DPOOL_DEFAULT,
					   &vb->indexBuffer,
					   NULL);
	} else {
	    hr = device->CreateIndexBuffer(vbSize*sizeof(UINT),
					   D3DUSAGE_WRITEONLY,
					   D3DFMT_INDEX32,   
					   D3DPOOL_DEFAULT,
					   &vb->indexBuffer,
					   NULL);
	}
	
	if (FAILED(hr)) {
	    vb->indexBuffer = NULL;
	    vb->release();
	    D3dCtx::d3dWarning(CREATEINDEXVBFAIL, hr);
	    return false;
	}

	cDirty |= javax_media_j3d_GeometryArrayRetained_INDEX_CHANGED;
    }

    float *vbptr;
    // Note that DWORD (use for color) is of same size 
    // as float (use for vertex/normal)
    hr = vb->buffer->Lock(0, 0, (VOID**)&vbptr,  0);
    if (FAILED(hr)) {
	D3dCtx::d3dWarning(LOCKVBFAIL, hr);
	// recreate it next time
	vb->release();
	return false;
    }

    d3dCtx->pVB = vb;

    // The current VB is not yet used. 
    vb->stripLen = 0;
    
   if (cDirty) {
       D3DINDEXBUFFER_DESC desc;
       BYTE *bptr;

       vb->indexBuffer->GetDesc(&desc);
       hr = vb->indexBuffer->Lock(0, 0, (VOID**)&bptr,  0);
       if (FAILED(hr)) {
	   D3dCtx::d3dWarning(LOCKINDEXVBFAIL, hr);
	   vb->buffer->Unlock();
	   return false;
       }

       if (d3dCtx->reIndexifyTable == NULL) {
	   // vcount will not change during renderIndexGeometry
	   // and splitIndexVertex so it is safe not to check
	   // size of reIndexifyTable and recreate a bigger
	   // one.
	   d3dCtx->reIndexifyTable = new DWORD[vcount];
	   if (d3dCtx->reIndexifyTable == NULL) {
	       D3dCtx::d3dWarning(OUTOFMEMORY, hr);
	       vb->release();
	       return false;	       
	   }

       }

       ZeroMemory(d3dCtx->reIndexifyTable, sizeof(DWORD)*vcount);

       DWORD i;
       jint *idxPtr = strideData->indexPtr + strideData->initialIndexIndex;
       USHORT firstEntry = *idxPtr;
       DWORD *table = d3dCtx->reIndexifyTable;

       if (desc.Format == D3DFMT_INDEX16) {
	   USHORT *dst = (USHORT *) bptr;
	   USHORT newIdx, prevIdx = -1, count = 0;
	   USHORT idx[3], vc = 0;
	   
	   for (i=0; i < indexCount; i++) {
	       newIdx = table[*idxPtr];
	       if ((newIdx == 0) && (*idxPtr != firstEntry)) {
		   newIdx = ++count;
		   table[*idxPtr] = newIdx;
	       } 
	       if (!expandQuadIndex) {
		   *dst++ = newIdx;
	       } else {
		   if (vc < 3) {
		       idx[vc++] = newIdx;
		   } else {
		       // vc = 3
		       *dst++ = idx[0];
		       *dst++ = idx[1];
		       *dst++ = idx[2];
		       *dst++ = idx[0];
		       *dst++ = idx[2];
		       *dst++ = newIdx;
		       vc = 0;
		   }
	       }
	       if (newIdx != prevIdx) {
		   copyOneVertexToVB(d3dCtx, &vbptr, strideData,
				     *idxPtr++, cDirty, xform, nxform);
		   prevIdx = newIdx;
	       } else {
		   idxPtr++;
	       }
	       
	   }
       } else {
	   DWORD *dst = (DWORD *) bptr;
	   DWORD newIdx, prevIdx = -1, count = 0;
	   DWORD idx[3], vc = 0;

	   for (i=0; i < indexCount; i++) {
	       newIdx = table[*idxPtr];
	       if ((newIdx == 0) && (*idxPtr != firstEntry)) {
		   newIdx = ++count;
		   table[*idxPtr] = newIdx;
	       } 
	       if (!expandQuadIndex) {
		   *dst++ = newIdx;
	       } else {
		   if (vc < 3) {
		       idx[vc++] = newIdx;
		   } else {
		       // vc = 3
		       *dst++ = idx[0];
		       *dst++ = idx[1];
		       *dst++ = idx[2];
		       *dst++ = idx[0];
		       *dst++ = idx[2];
		       *dst++ = newIdx;
		       vc = 0;
		   }
	       }
	       if (newIdx != prevIdx) {
		   copyOneVertexToVB(d3dCtx, &vbptr, strideData,
				     *idxPtr++, cDirty, xform, nxform);
		   prevIdx = newIdx;
	       } else {
		   idxPtr++;
	       }
	   }
       }
   }


   vb->addStride(vbSize);
   vb->indexBuffer->Unlock();
   vb->buffer->Unlock();
   return true;
}


void splitIndexVertexToMultipleVB(D3dCtx *d3dCtx,
				  LPD3DDRAWPRIMITIVESTRIDEDDATA strideData,
				  int indexCount,
				  int vertexCount,
				  int maxVertexLimit,
				  jint cDirty,
				  BOOL expandQuadIndex,
				  jdouble* xform,
				  jdouble* nxform) 
{
    int vc;
    BOOL success;
    int inc;
    int min = 0;
    int max = 0;
    int initialIdxIdx = strideData->initialIndexIndex;


    switch (d3dCtx->pVB->primitiveType) {
    case D3DPT_TRIANGLEFAN: 
        {
	    jint firstIdx = strideData->indexPtr[initialIdxIdx]; 
	    jint prevIdx = firstIdx;

	    inc = maxVertexLimit - 2;
	    
	    while (true) {
		vc = (indexCount >= maxVertexLimit ? maxVertexLimit : indexCount);
		success = reIndexifyIndexVertexToVBs(d3dCtx,
						     strideData,
						     vc,
						     vertexCount,
						     cDirty,
						     expandQuadIndex,
						     maxVertexLimit,
						     xform, nxform);
		// restore index 
		strideData->indexPtr[strideData->initialIndexIndex] = prevIdx;
		indexCount -= inc;
		
		if (!success || (indexCount <= 2)) {
		    break;
		}
		// repeat the last index
		strideData->initialIndexIndex += (vc - 2); 
		// replace by first index
		prevIdx = strideData->indexPtr[strideData->initialIndexIndex];
		strideData->indexPtr[strideData->initialIndexIndex] = firstIdx;
	    }
        }
	break;
    case D3DPT_POINTLIST:
	if (max == 0) {
	    max = maxVertexLimit;
	}
	// fall through
    case D3DPT_LINESTRIP:
	if (max == 0) {
	    max = maxVertexLimit;
	    min = 1; // repeat the last vertex;
	}
	// fall through	
    case D3DPT_TRIANGLELIST:
	if (max == 0) {
	    if (expandQuadIndex) {
		// QuadArray
		max = maxVertexLimit - (maxVertexLimit % 4);
	    } else {
		max = maxVertexLimit - (maxVertexLimit % 3);
	    }
	}
	// fall through
    case D3DPT_LINELIST:
	if (max == 0) {
	    max = maxVertexLimit - (maxVertexLimit % 2);
	}
	// fall through
    case D3DPT_TRIANGLESTRIP:
	if (max == 0) {
	    max = maxVertexLimit - (maxVertexLimit % 4);
	    min = 2;  // repeat the last two vertices
	}
	inc = max - min;

	while (true) {
	    vc = (indexCount >= max ? max : indexCount);

	    if (!reIndexifyIndexVertexToVBs(d3dCtx,
					    strideData,
					    vc,
					    vertexCount,
					    cDirty,
					    expandQuadIndex,
					    maxVertexLimit,
					    xform, nxform)) {
		break;
	    } 

	    indexCount -= inc;
	    if (indexCount <= min) {
		break;
	    }
	    strideData->initialIndexIndex += inc;
	}
    }
    strideData->initialIndexIndex = initialIdxIdx;
}

// This is used by quad polygon line mode
void DrawPolygonLine(D3dCtx *d3dCtx,
		     LPDIRECT3DDEVICE9 device,
		     DWORD vertexFormat,
		     D3DDRAWPRIMITIVESTRIDEDDATA *strideData)
{
    HRESULT hr;
    float *vbptr;

    hr = d3dCtx->pVB->buffer->Lock(0, 0, (VOID**) &vbptr, 0 );
    if (FAILED(hr)) {
	D3dCtx::d3dWarning(LOCKVBFAIL, hr);
	return;
    }
    // DisplayList will not use in this case, so xform = nxform = NULL
    copyVertexToVB(d3dCtx, strideData, 4, &vbptr,
		           javax_media_j3d_GeometryArrayRetained_VERTEX_CHANGED, true,
		           NULL, NULL);
    d3dCtx->pVB->buffer->Unlock();
    device->SetStreamSource(0, d3dCtx->pVB->buffer, 0,
			                d3dCtx->pVB->stride); 
    device->SetIndices(d3dCtx->lineModeIndexBuffer);
    //device->SetVertexShader(vertexFormat);
	device->SetVertexShader(NULL);
	device->SetFVF(vertexFormat);

    device->DrawIndexedPrimitive(D3DPT_LINESTRIP,0, 0, 4, 0, 4);
}


// This is used by indexed quad polygon line mode
void DrawIndexPolygonLine(D3dCtx *d3dCtx,
			  LPDIRECT3DDEVICE9 device,
			  DWORD vertexFormat,
			  D3DDRAWPRIMITIVESTRIDEDDATA *strideData,
			  jint idx0, jint idx1, jint idx2, jint idx3)
{
    HRESULT hr;
    float *vbptr;

    hr = d3dCtx->pVB->buffer->Lock(0, 0, (VOID**) &vbptr, 0 );
    if (FAILED(hr)) {
	D3dCtx::d3dWarning(LOCKVBFAIL, hr);
	return;
    }

    copyOneVertexToVB(d3dCtx, &vbptr, strideData, idx0, 
		      javax_media_j3d_GeometryArrayRetained_VERTEX_CHANGED, 
		      NULL, NULL);
    copyOneVertexToVB(d3dCtx, &vbptr, strideData, idx1, 
		      javax_media_j3d_GeometryArrayRetained_VERTEX_CHANGED, 
		      NULL, NULL);
    copyOneVertexToVB(d3dCtx, &vbptr, strideData, idx2, 
		      javax_media_j3d_GeometryArrayRetained_VERTEX_CHANGED,
		      NULL, NULL);
    copyOneVertexToVB(d3dCtx, &vbptr, strideData, idx3, 
		      javax_media_j3d_GeometryArrayRetained_VERTEX_CHANGED,
		      NULL, NULL);

    d3dCtx->pVB->buffer->Unlock();
    device->SetStreamSource(0, d3dCtx->pVB->buffer, 0,
			               d3dCtx->pVB->stride); 
    //device->SetVertexShader(vertexFormat);
	device->SetVertexShader(NULL);
	device->SetFVF(vertexFormat);

    device->SetIndices(d3dCtx->lineModeIndexBuffer);

    device->DrawIndexedPrimitive(D3DPT_LINESTRIP,0, 0, 4, 0, 4);
}


void renderGeometry(JNIEnv *env, 
		    D3dCtx *d3dCtx,
		    LPDIRECT3DDEVICE9 device,
		    jobject geo,
		    jint geo_type,
		    D3DDRAWPRIMITIVESTRIDEDDATA *strideData,
		    DWORD vertexFormat,
		    jint vcount,
		    jdouble* xform,
		    jdouble* nxform,
		    jint cDirty)
{
    D3DPRIMITIVETYPE d3dRenderType;
    BOOL renderTypeSet = false;
    int i, j, genMode;
    LPD3DVERTEXBUFFER vb = NULL;
    D3dVertexBufferVector *vbVector;
    float *vbptr;

#ifdef VBDEBUG
    BOOL createNew = false;
#endif

    if (vcount <= 0) {
	return;
    }

    jclass geoClass =  env->GetObjectClass(geo);
    DWORD maxVertexLimit =  d3dCtx->deviceInfo->maxVertexCount[geo_type];
    DWORD texSetUsed = d3dCtx->texSetUsed;
    HRESULT hr;
    BOOL needPointFlag = (geo_type == GEO_TYPE_POINT_SET) ||
	                 ((geo_type != GEO_TYPE_LINE_STRIP_SET) &&
			  (geo_type != GEO_TYPE_LINE_SET) &&
			  (d3dCtx->fillMode == D3DFILL_POINT));

    BOOL buildDL = (d3dCtx->currDisplayListID > 0);

    lockGeometry();

    if (!buildDL) {
	jfieldID fieldID =  env->GetFieldID(geoClass, "pVertexBuffers", "J");
	jobject cloneSource = NULL;

	vbVector = reinterpret_cast<D3dVertexBufferVector *>
	    (env->GetLongField(geo, fieldID));

	if (vbVector == NULL) {
	    // It is possible this is the mirrorGeometry 
	    // from cloneNonIndexGeometry()
	    jfieldID fieldID2 = env->GetFieldID(geoClass,
				      "cloneSourceArray",
				      "Ljavax/media/j3d/IndexedGeometryArrayRetained;");	
	    cloneSource = env->GetObjectField(geo, fieldID2);

	    if (cloneSource != NULL) {
		jclass cloneClass = env->GetObjectClass(cloneSource);
		fieldID =  env->GetFieldID(cloneClass, "pVertexBuffers", "J");

		vbVector = reinterpret_cast<D3dVertexBufferVector *>
		    (env->GetLongField(cloneSource, fieldID));
	    }

	}
	 

	// This is the first time rendering is invoked on the
	// first GeometryArray
	if (vbVector == NULL) {
	    vbVector = new D3dVertexBufferVector();
	    if (vbVector == NULL) {
		D3dCtx::d3dWarning(OUTOFMEMORY);
		unlockGeometry();
		return;
	    }
	    if (cloneSource == NULL) {
		env->SetLongField(geo, fieldID,
				  reinterpret_cast<long>(vbVector));
	    } else {
		env->SetLongField(cloneSource, fieldID,
				  reinterpret_cast<long>(vbVector));
	    }

	} else {
	    // Found the vb in the list of vbVector
	    for (ITER_LPD3DVERTEXBUFFER s = vbVector->begin(); 
		 s != vbVector->end(); ++s) {
		 if ((*s)->ctx == d3dCtx) {
		    vb = *s;
		    break;
		}
	    }
	}
    }

    if (vb == NULL) {
	// This is the first time rendering is invoked
	// using this ctx
	vb = new D3dVertexBuffer();

	if (vb == NULL) {
	    D3dCtx::d3dWarning(OUTOFMEMORY);
	    unlockGeometry();
	    return;
	}
	vb->ctx = d3dCtx;
	vb->maxVertexLimit = maxVertexLimit;

	if (!buildDL) {
	    vb->vbVector = vbVector;
	
	    // add it to the GeometryArray pVertexBuffers list
	    vbVector->push_back(vb);
	
	    // add it to the ctx lists
	    vb->next = 	d3dCtx->vertexBufferTable.next;
	    vb->previous = &(d3dCtx->vertexBufferTable);
	    d3dCtx->vertexBufferTable.next = vb;
	    if (vb->next != NULL) {
		vb->next->previous = vb;
	    }
	}
    }

    if ((vb->buffer != NULL) && 
	((vb->vertexFormat != vertexFormat) ||
	 (vb->totalVertexCount < vcount) ||
	 (cDirty &
	  javax_media_j3d_GeometryArrayRetained_STRIPCOUNT_CHANGED) ||
	 (!vb->isPointFlagUsed  && needPointFlag))) {
	// immediate release VB and reconstruct a new one
	vb->release();
    } 

    if (vb->buffer == NULL) {
	vb->stride = D3DXGetFVFVertexSize(vertexFormat);
	vb->vertexFormat = vertexFormat;
	vb->totalVertexCount = vcount;
	vb->vcount = (vcount >= maxVertexLimit ? maxVertexLimit : vcount); 
#ifdef VBDEBUG
	printf("Create primary VertexBuffer of size %d, display list ID %d, pointFlag %d\n", 
	       vb->vcount, d3dCtx->currDisplayListID, needPointFlag);
#endif
	if (!needPointFlag) {
	    hr = device->CreateVertexBuffer(vb->stride*vb->vcount,
					    D3DUSAGE_WRITEONLY,
					    vertexFormat,
					    D3DPOOL_DEFAULT,
					    &vb->buffer,
						NULL);
	} else {
	    hr = device->CreateVertexBuffer(vb->stride*vb->vcount,
					    D3DUSAGE_WRITEONLY|D3DUSAGE_POINTS,
					    vertexFormat,
					    D3DPOOL_DEFAULT,
					    &vb->buffer,
						NULL);
	    vb->isPointFlagUsed = true;
	}

	if (FAILED(hr)) {
	    vb->buffer = NULL;
	    D3dCtx::d3dWarning(CREATEVBFAIL, hr);
	    unlockGeometry();
	    return;
	}
#ifdef VBDEBUG
	createNew = true;
#endif
	cDirty = javax_media_j3d_GeometryArrayRetained_VERTEX_CHANGED;
    } 

    unlockGeometry();

    if (buildDL) { 
	// In display list mode, add it to the displayList ID table
	d3dCtx->displayListTable[d3dCtx->currDisplayListID]->add(vb);
    } else {

	if (vb->primitiveType == D3DPT_FORCE_DWORD) {
	    // This happens when previous frame use Quad Line
	    // so buffer not yet initialize
	    cDirty = javax_media_j3d_GeometryArrayRetained_VERTEX_CHANGED;
	}

	if (!cDirty &&
	    ((geo_type != GEO_TYPE_QUAD_SET) ||
	     (d3dCtx->fillMode != D3DFILL_WIREFRAME))) {
	    for (i=0; i < d3dCtx->texSetUsed; i++) {
		genMode = strideData->texCoordPosition[i];		
		if ((genMode == TEX_OBJ_LINEAR) ||
		    ((genMode != vb->texCoordPosition[i]) && 
		     (genMode != TEX_GEN_AUTO))) {
		    // For object linear mode user can set the plane
		    // equations so we need to change texture coordinate.
		    break;
		}
	    }
	    if (i == d3dCtx->texSetUsed) {
		vb->render(d3dCtx);
#ifdef TEXDEBUG
		printf("Skip VB Copy\n");
#endif
		return;
	    }
	}
    }
    
    // Note that DWORD (use for color) is of same size 
    // as float (use for vertex/normal)

    hr = vb->buffer->Lock(0, 0, (VOID**)&vbptr,  0);
    if (FAILED(hr)) {
	D3dCtx::d3dWarning(LOCKVBFAIL, hr);
	// recreate it next time
	vb->release();
	return;
    }

    d3dCtx->pVB = vb;    
    // The current VB is not yet used. 
    vb->stripLen = 0;

    switch (geo_type) {
        case GEO_TYPE_TRI_STRIP_SET:
	    d3dRenderType = D3DPT_TRIANGLESTRIP;
	    renderTypeSet = true;
#ifdef VBDEBUG
	    if (createNew) {
		printf("Tri strip set %d\n", vcount);
	    }
#endif
	    // fall through
        case GEO_TYPE_TRI_FAN_SET:
	    if (renderTypeSet == false) {
#ifdef VBDEBUG
		if (createNew) {
		    printf("Tri fan set %d\n", vcount);
		}
#endif
		d3dRenderType = D3DPT_TRIANGLEFAN;
		renderTypeSet = true;
	    }
	    // fall through
        case GEO_TYPE_LINE_STRIP_SET:
	  {
	    if (renderTypeSet == false) {
		d3dRenderType = D3DPT_LINESTRIP;
		renderTypeSet = true;
#ifdef VBDEBUG
		if (createNew) {
		    printf("Tri line set %d \n", vcount);
		}
#endif
	    }

	    jfieldID strip_field = env->GetFieldID(geoClass,
					  "stripVertexCounts", "[I");
	    jarray sarray = (jarray)env->GetObjectField(geo, strip_field);
	    jsize strip_len = (jsize)env->GetArrayLength(sarray);

            jint* strips = (jint *)env->GetPrimitiveArrayCritical(sarray, NULL);

	    int nlastStrip = 0;
	    int totalLen = 0;
	    int oldTotalLen = 0;
	    int vsum = 0;

	    vb->primitiveType = d3dRenderType;

	    if (vcount <= vb->vcount) {
		copyVertexToVB(d3dCtx, strideData, vcount,
			       &vbptr, cDirty, false, xform, nxform);
		vb->addStrides(strip_len, strips);
#ifdef VBDEBUG
	    if (createNew) {
		printf("Strip Length %d : ", strip_len);
		for (int k=0; k < strip_len; k++) {
		    printf("%d ", strips[k]);
		}
		printf("\n");
	    }
#endif
	    } else {
#ifdef VBDEBUG
	    if (createNew) {
		printf("Strip Length %d : ", strip_len);
	    }
#endif

		for (i = 0; i < strip_len; i++) {
		    if (strideData->fpositionPtr) {
			strideData->fpositionPtr +=
			    strideData->positionStride*nlastStrip;
		    } else {
			strideData->dpositionPtr +=
			    strideData->positionStride*nlastStrip;
		    }
		    
		    if (strideData->normalPtr) {
			strideData->normalPtr +=
			    strideData->normalStride*nlastStrip;
		    }
		    
		    if (strideData->fdiffusePtr) {
			strideData->fdiffusePtr  +=
			    strideData->diffuseStride*nlastStrip;
		    } else if (strideData->bdiffusePtr) {
			strideData->bdiffusePtr  +=
			    strideData->diffuseStride*nlastStrip;		    
		    }
		    
		    
		    if (strideData->textureCoordsPtr[0]) {
			for (j=0; j < texSetUsed; j++) {
			    strideData->textureCoordsPtr[j] += 
				strideData->textureCoordsStride[j]* nlastStrip;
			}
		    }
		    
		    
		    nlastStrip = strips[i];
		    oldTotalLen = totalLen;
		    totalLen += nlastStrip;
		    
		    if (totalLen > vcount) {
			// This should not happen since 
			// setValidVertexCount is disable
			// in v1.3. We should always have
			// (sum of strips[] < vcount)
			nlastStrip = (vcount - (totalLen - nlastStrip));
			totalLen = vcount;
		    }
		    
		    if (nlastStrip <= 0) {
			continue;
		    }
		    
		    
		    if (vbptr == NULL) {
			// This happen when the lastStrip copy
			// is greater than maxVertexLimit.
			// So we Unlock the last buffer
			vbptr = allocateVB(d3dCtx, device, 
					   vcount - oldTotalLen,
					   maxVertexLimit, &cDirty);
			vsum = 0;
			if (vbptr == NULL) {
			    break; // render whatever geometry the current VB have
			}
		    }
#ifdef VBDEBUG
		    if (createNew) {
			printf(" %d ", nlastStrip);
		    }
#endif
		    if ((vsum + nlastStrip) <= d3dCtx->pVB->vcount) {
			// There is space available to put in vertices
			vsum += nlastStrip;
			copyVertexToVB(d3dCtx, strideData, nlastStrip,
				       &vbptr, cDirty, true, xform, nxform);
		    } else {
			// Need to create a new VB
			if (nlastStrip <= maxVertexLimit) {
			    // No need to split strip in multiple VB
			    if (d3dCtx->pVB->stripLen > 0) {
				if (vbptr != NULL) {
				    d3dCtx->pVB->buffer->Unlock();
				}
				vbptr = allocateVB(d3dCtx, device, 
						   vcount - oldTotalLen,
						   maxVertexLimit, &cDirty);
				if (vbptr == NULL) {
				    break; 
				}
				vsum = 0;
			    }
			    vsum += nlastStrip;
			    copyVertexToVB(d3dCtx, strideData, nlastStrip,
					   &vbptr, cDirty, true,
					   xform, nxform);
			} else {
			    d3dCtx->pVB->buffer->Unlock();
			    vbptr = NULL;
			    vsum = 0;
			    // Multiple VBs for large vertex size
			    splitVertexToMultipleVB(d3dCtx, strideData,
						    nlastStrip,
						    maxVertexLimit,
						    cDirty, xform, nxform);
			    vbptr = NULL;
			}
		    }
		}
#ifdef VBDEBUG
		if (createNew) {
		    printf("\n");
		}
#endif
	  }
            env->ReleasePrimitiveArrayCritical(sarray, strips, NULL);
	  }
	    break;
        case GEO_TYPE_QUAD_SET:
#ifdef VBDEBUG
		if (createNew) {
		    printf("quad set %d\n", vcount);
		}
#endif

		if (buildDL ||
		    (d3dCtx->fillMode != D3DFILL_WIREFRAME)) {
		    /* 
		     * Note we can't just check
		     * if (d3dCtx->fillMode != D3DFILL_WIREFRAME)
		     * since the fillMode may set back to
		     * non-line mode AFTER build display list
		     * In fact it is gaurantee that when displaylist
		     * mode is used we are not in line mode
		     */

		    // break down solid into two triangles
		    if (createQuadIndices(d3dCtx, vcount)) {
                       // It happens when 
                       // vcount*3/2 != d3dCtx->quadIndexBufferSize
                       // to conform with index buffer Limitation in
                       // graphics card. So we adjust using a smaller
                       // maxVertexLimit when spliting into 
                       // multiple Vertex Buffers.
                       maxVertexLimit = 2*d3dCtx->quadIndexBufferSize/3;
                       vb->maxVertexLimit = maxVertexLimit;
                       vb->vcount = vb->maxVertexLimit;
		    }
		    d3dRenderType = D3DPT_TRIANGLELIST;
		    vb->isIndexPrimitive = true;
		    renderTypeSet = true;
		    // fall through
		} 
		else 
		{ // line mode
		    // we don't want to see extra line appear in the
		    // diagonal of quads if it splits into two
		    // triangles. This is REALLY SLOW !!!
		    int len = vcount >> 2;
		    int offsetPos = 0;
		    int offsetNorm = 0;
		    int offsetColor = 0;
		    int  strideOffsetPos = strideData->positionStride << 2;
		    int  strideOffsetNorm = strideData->normalStride << 2;
		    int  strideOffsetColor = strideData->diffuseStride << 2;
		    jfloat *pdf = strideData->fpositionPtr;
		    jdouble *pdd = strideData->dpositionPtr;
		    jfloat *pn = strideData->normalPtr;
		    jfloat *pcf = strideData->fdiffusePtr;
		    jbyte  *pcb = strideData->bdiffusePtr;
		    jfloat* pt[D3DDP_MAXTEXCOORD];

		    pt[0] = NULL;

		    if (((vertexFormat & D3DFVF_DIFFUSE) == 0) && 
			(!d3dCtx->isLightEnable)) {
			d3dCtx->setAmbientLightMaterial();
		    }

		    vb->buffer->Unlock();		    
		    vbptr = NULL;		
#ifdef VBDEBUG
		    if (createNew) {
			printf("quad set polygon line %d\n", vcount);
		    }
#endif

		    for (i=0; i < texSetUsed; i++) {
			pt[i] = (FLOAT *) strideData->textureCoordsPtr[i];
		    }

		    jfloat *fptr;
		    jdouble *dptr;
		    jbyte *bptr;
		    jfloat *fspt;
		    jdouble *dspt;
		    int posStride = strideData->positionStride;
		    D3DVERTEX worldCoord[3];
		    D3DTLVERTEX screenCoord[3];

		    vb->primitiveType = D3DPT_FORCE_DWORD;

		    if (d3dCtx->lineModeIndexBuffer == NULL) {
			createLineModeIndexBuffer(d3dCtx);
		    }

		    for (i = 0; i < (vcount >> 2); i++) {
			if (pdf) {
			    fspt = fptr  = pdf + offsetPos;			
			    strideData->fpositionPtr = fptr;
			} else {
			    dspt = dptr  = pdd + offsetPos;			
			    strideData->dpositionPtr = dptr;
			}

			if (pn) {
			    fptr = pn + offsetNorm;
			    strideData->normalPtr = fptr;
			}

			if (pcf) {
			    fptr =  pcf + offsetColor;
			    strideData->fdiffusePtr = fptr;
			} else if (pcb) {
			    bptr = pcb + offsetColor;
			    strideData->bdiffusePtr = bptr;
			}

			if (pt[0]) {
			    for (j=0; j < texSetUsed; j++) {
				DWORD stride3 = 3*strideData->textureCoordsStride[j];
				fptr = pt[j] + i*(strideData->textureCoordsStride[j] << 2);
				strideData->textureCoordsPtr[j] = fptr;
			    }
			}
			if (d3dCtx->cullMode != D3DCULL_NONE) {
			// Do back face culling here
			    if (pdf) {
				worldCoord[0].x = fspt[0];
				worldCoord[0].y = fspt[1];
				worldCoord[0].z = fspt[2];
				fspt += posStride;
				worldCoord[1].x = fspt[0]; 
				worldCoord[1].y = fspt[1];
				worldCoord[1].z = fspt[2];
				fspt += posStride;
				worldCoord[2].x = fspt[0];
				worldCoord[2].y = fspt[1];
				worldCoord[2].z = fspt[2];
			    } else {
				worldCoord[0].x = dspt[0];
				worldCoord[0].y = dspt[1];
				worldCoord[0].z = dspt[2];
				dspt += posStride;
				worldCoord[1].x = dspt[0]; 
				worldCoord[1].y = dspt[1];
				worldCoord[1].z = dspt[2];
				dspt += posStride;
				worldCoord[2].x = dspt[0];
				worldCoord[2].y = dspt[1];
				worldCoord[2].z = dspt[2];
			    }
			    d3dCtx->transform(&worldCoord[0], &screenCoord[0]);
			    d3dCtx->transform(&worldCoord[1], &screenCoord[1]);
			    d3dCtx->transform(&worldCoord[2], &screenCoord[2]);
			    screenCoord[0].sx -= screenCoord[1].sx;  
			    screenCoord[0].sy -= screenCoord[1].sy;  
			    screenCoord[2].sx -= screenCoord[1].sx;  
			    screenCoord[2].sy -= screenCoord[1].sy;  
			    if (d3dCtx->cullMode == D3DCULL_CW) {
				// clip back face
				if ((screenCoord[0].sx*screenCoord[2].sy -
				     screenCoord[2].sx*screenCoord[0].sy) >= 0) {
				    DrawPolygonLine(d3dCtx,
						    device,
						    vertexFormat,
						    strideData);
				} 
			    } else { // Clip front face
				if ((screenCoord[0].sx*screenCoord[2].sy -
				     screenCoord[2].sx*screenCoord[0].sy) <= 0) {
				    DrawPolygonLine(d3dCtx,
						    device,
						    vertexFormat,
						    strideData);
				} 
			    }
			} else {
			    // cullMode == D3DCULL_NONE
			    DrawPolygonLine(d3dCtx,
					    device,
					    vertexFormat,
					    strideData);
			}
			offsetPos += strideOffsetPos;
			offsetNorm += strideOffsetNorm;
			offsetColor += strideOffsetColor;
		    }

		    if (((vertexFormat & D3DFVF_DIFFUSE) == 0) && 
			(!d3dCtx->isLightEnable)) {
			d3dCtx->restoreDefaultLightMaterial();
		    }
		    // Don't call vb->Renderer() at the end
		    return;
		}
	    // fallthrough
        case GEO_TYPE_TRI_SET:
	    if (renderTypeSet == false) {
		d3dRenderType = D3DPT_TRIANGLELIST;
		renderTypeSet = true;
#ifdef VBDEBUG
		if (createNew) {
		    printf("tri set %d\n", vcount);
		}
#endif
	    }
	    // fallthrough
        case GEO_TYPE_LINE_SET:
	    if (renderTypeSet == false) {
		d3dRenderType = D3DPT_LINELIST;
		renderTypeSet = true;
#ifdef VBDEBUG
		if (createNew) {
		    printf("line set %d\n", vcount);
		}
#endif
	    }
	    // fallthrough
        case GEO_TYPE_POINT_SET:
	    if (renderTypeSet == false) {
		d3dRenderType = D3DPT_POINTLIST;
#ifdef VBDEBUG
		if (createNew) {
		    printf("point set %d\n", vcount);
		}
#endif
	    }
	    vb->primitiveType = d3dRenderType;

	    if (vcount <= vb->vcount) {
		copyVertexToVB(d3dCtx, strideData, vcount, &vbptr,
			       cDirty, true, xform, nxform);
	    } else {
		if (vbptr != NULL) {
		    vb->buffer->Unlock();
		    vbptr = NULL;
		}

		splitVertexToMultipleVB(d3dCtx, strideData, vcount,
					maxVertexLimit,
					cDirty, xform, nxform);
		vbptr = NULL;
	    }
	    break;
        default:
            printf("GeometryArrayRetained_execute:unknown geo_type %ld \n", geo_type);
    }

    if (vbptr != NULL) {
	// d3dCtx->pVB is the last reference in VB list
	d3dCtx->pVB->buffer->Unlock();
    }

    for (i=0; i < d3dCtx->texSetUsed; i++) {
	 d3dCtx->pVB->texCoordPosition[i] =
	     strideData->texCoordPosition[i];
    }

    if (!buildDL) { 
	// Not in displaylist mode, directly render VB
	// vb is the root reference in VB list
	vb->render(d3dCtx);
    }
}


void renderIndexGeometry(JNIEnv *env, 
			 D3dCtx *d3dCtx,
			 LPDIRECT3DDEVICE9 device,
			 jobject geo,
			 jint geo_type,
			 D3DDRAWPRIMITIVESTRIDEDDATA *strideData,
			 DWORD vertexFormat,
			 jint vcount,
			 jint indexCount,
			 jdouble* xform,
			 jdouble* nxform,
			 jint cDirty)
{
    D3DPRIMITIVETYPE d3dRenderType;
    BOOL renderTypeSet = false;
    BOOL expandQuadIndex = false;
    int i;
    LPD3DVERTEXBUFFER vb = NULL;
    D3dVertexBufferVector *vbVector;
    float *vbptr;

#ifdef VBDEBUG
    BOOL createNew = false;
#endif

    if (indexCount <= 0) {
	return;
    }

    jclass geoClass =  env->GetObjectClass(geo);
    DWORD maxVertexLimit =
	min(d3dCtx->deviceInfo->maxVertexCount[geo_type],
	    d3dCtx->deviceInfo->maxVertexIndex);
    DWORD texSetUsed = d3dCtx->texSetUsed;
    HRESULT hr;
    BOOL needPointFlag = (geo_type == GEO_TYPE_INDEXED_POINT_SET) ||
	                 ((geo_type != GEO_TYPE_INDEXED_LINE_STRIP_SET) &&
			  (geo_type != GEO_TYPE_INDEXED_LINE_SET) &&
			  (d3dCtx->fillMode == D3DFILL_POINT));

    BOOL buildDL = (d3dCtx->currDisplayListID > 0);

    if (geo_type == GEO_TYPE_INDEXED_QUAD_SET) {
	// Since the index we create with be 1.5 times the original index
	maxVertexLimit = 2*maxVertexLimit/3;
    }


    lockGeometry();

    if (!buildDL) {
	jfieldID fieldID =  env->GetFieldID(geoClass, "pVertexBuffers", "J");
	vbVector = reinterpret_cast<D3dVertexBufferVector *>
	    (env->GetLongField(geo, fieldID));
	
	if (vbVector == NULL) {
	    // This is the first time rendering is invoked on the
	    // first GeometryArray
	    vbVector = new D3dVertexBufferVector();
	    if (vbVector == NULL) {
		D3dCtx::d3dWarning(OUTOFMEMORY);
		unlockGeometry();
		return;
	    }
	    env->SetLongField(geo, fieldID, reinterpret_cast<long>(vbVector));	    
	} else {
	    // Found the vb in the list of vbVector
	     for (ITER_LPD3DVERTEXBUFFER s = vbVector->begin(); 
		      s != vbVector->end(); ++s) {
		if ((*s)->ctx == d3dCtx) {
		    vb = *s;
		    break;
		}
	    }
	}
    }

    if (vb == NULL) {
	// This is the first time rendering is invoked
	// using this ctx
	vb = new D3dVertexBuffer();

	if (vb == NULL) {
	    D3dCtx::d3dWarning(OUTOFMEMORY);
	    unlockGeometry();
	    return;
	}
	vb->ctx = d3dCtx;
	vb->maxVertexLimit = maxVertexLimit;

	if (!buildDL) {
	    vb->vbVector = vbVector;
	
	    // add it to the GeometryArray pVertexBuffers list
	    vbVector->push_back(vb);
	
	    // add it to the ctx lists
	    vb->next = 	d3dCtx->vertexBufferTable.next;
	    vb->previous = &(d3dCtx->vertexBufferTable);
	    d3dCtx->vertexBufferTable.next = vb;
	    if (vb->next != NULL) {
		vb->next->previous = vb;
	    }
	}
    }

    if (((vb->indexBuffer != NULL) &&
	 (vb->totalIndexCount < indexCount)) || 
	((vb->buffer != NULL) && 
	((vb->vertexFormat != vertexFormat) ||
	 (vb->totalVertexCount < vcount) ||
	 (!vb->isPointFlagUsed  && needPointFlag)))) {
	// immediate release VB and reconstruct a new one
	vb->release();
    } 


    if (vb->buffer == NULL) {
	vb->stride = D3DXGetFVFVertexSize(vertexFormat);
	vb->vertexFormat = vertexFormat;
	vb->totalVertexCount = vcount;
	vb->isIndexPrimitive = true;
	vb->vcount = (vcount >= maxVertexLimit ? maxVertexLimit : vcount); 
#ifdef VBDEBUG
	printf("Create primary VertexBuffer of size %d, display list ID %d, pointFlag %d\n", 
	       vb->vcount, d3dCtx->currDisplayListID, needPointFlag);
#endif
	
	if (!needPointFlag) {
	    hr = device->CreateVertexBuffer(vb->stride*vb->vcount,
					    D3DUSAGE_WRITEONLY,
					    vertexFormat,
					    D3DPOOL_DEFAULT,
					    &vb->buffer,
						NULL);
	} else {
	    hr = device->CreateVertexBuffer(vb->stride*vb->vcount,
					    D3DUSAGE_WRITEONLY|D3DUSAGE_POINTS,
					    vertexFormat,
					    D3DPOOL_DEFAULT,
					    &vb->buffer,
						NULL);
	    vb->isPointFlagUsed = true;
	}

	if (FAILED(hr)) {
	    vb->buffer = NULL;
	    D3dCtx::d3dWarning(CREATEVBFAIL, hr);
	    unlockGeometry();
	    return;
	}
#ifdef VBDEBUG
	createNew = true;
#endif
	cDirty = javax_media_j3d_GeometryArrayRetained_VERTEX_CHANGED;
    }

    if (vb->indexBuffer == NULL) {

	vb->totalIndexCount = indexCount;
	vb->indexCount = (indexCount >= maxVertexLimit ? 
			  maxVertexLimit : indexCount); 	

	if (geo_type == GEO_TYPE_INDEXED_QUAD_SET) {
	    // Since we will construct another index with 
	    // 1.5 times of the original.
	    vb->indexCount = (3*vb->indexCount) >> 1;
	    vb->totalIndexCount = (3*indexCount) >> 1;
	}


	if (indexCount <= 0xffff) {
	    hr = device->CreateIndexBuffer(vb->indexCount*sizeof(WORD),
					   D3DUSAGE_WRITEONLY,
					   D3DFMT_INDEX16,
					   D3DPOOL_DEFAULT,
					   &vb->indexBuffer,
					   NULL);
	} else {
	    hr = device->CreateIndexBuffer(vb->indexCount*sizeof(UINT),
					   D3DUSAGE_WRITEONLY,
					   D3DFMT_INDEX32,
					   D3DPOOL_DEFAULT,
					   &vb->indexBuffer,
					   NULL);
	}
	
	if (FAILED(hr)) {
	    vb->indexBuffer = NULL;
	    D3dCtx::d3dWarning(CREATEINDEXVBFAIL, hr);
	    unlockGeometry();
	    return;
	}

	cDirty |= javax_media_j3d_GeometryArrayRetained_INDEX_CHANGED;
    }

    unlockGeometry();

    if (buildDL) { 
	// In display list mode, add it to the displayList ID table
	d3dCtx->displayListTable[d3dCtx->currDisplayListID]->add(vb);
    } else {

	if (vb->primitiveType == D3DPT_FORCE_DWORD) {
	    // This happens when previous frame use Quad Line
	    // so buffer not yet initialize
	    cDirty =
		javax_media_j3d_GeometryArrayRetained_VERTEX_CHANGED |
		javax_media_j3d_GeometryArrayRetained_INDEX_CHANGED;
	}

	if (!cDirty &&
	    ((geo_type != GEO_TYPE_INDEXED_QUAD_SET) ||
	     (d3dCtx->fillMode != D3DFILL_WIREFRAME))) {
	    for (i=0; i < d3dCtx->texSetUsed; i++) {
		if ((strideData->texCoordPosition[i] !=
		     vb->texCoordPosition[i]) 
		    &&
		    (strideData->texCoordPosition[i] != TEX_GEN_AUTO)) {
		    break;
		}
	    }
	    if (i == d3dCtx->texSetUsed) {
		vb->render(d3dCtx);
		return;
	    }
	}
    }

    // Note that DWORD (use for color) is of same size 
    // as float (use for vertex/normal)
    hr = vb->buffer->Lock(0, 0, (VOID**)&vbptr,  0);
    if (FAILED(hr)) {
	D3dCtx::d3dWarning(LOCKVBFAIL, hr);
	// recreate it next time
	vb->release();
	return;
    }

    d3dCtx->pVB = vb;

    // The current VB is not yet used. 
    vb->stripLen = 0;

    switch (geo_type) {
        case GEO_TYPE_INDEXED_TRI_STRIP_SET:
	    d3dRenderType = D3DPT_TRIANGLESTRIP;
	    renderTypeSet = true;
#ifdef VBDEBUG
	    if (createNew) {
		printf("Tri strip set %d\n", vcount);
	    }
#endif
	    // fall through
        case GEO_TYPE_INDEXED_TRI_FAN_SET:
	    if (renderTypeSet == false) {
#ifdef VBDEBUG
		if (createNew) {
		    printf("Tri fan set %d\n", vcount);
		}
#endif
		d3dRenderType = D3DPT_TRIANGLEFAN;
		renderTypeSet = true;
	    }
	    // fall through
        case GEO_TYPE_INDEXED_LINE_STRIP_SET:
	  {
	    if (renderTypeSet == false) {
		d3dRenderType = D3DPT_LINESTRIP;
		renderTypeSet = true;
#ifdef VBDEBUG
		if (createNew) {
		    printf("Tri line set %d \n", vcount);
		}
#endif
	    }

	    jfieldID strip_field = env->GetFieldID(geoClass,
					  "stripIndexCounts", "[I");
	    jarray sarray = (jarray)env->GetObjectField(geo, strip_field);
	    jsize strip_len = (jsize)env->GetArrayLength(sarray);

            jint* strips = (jint *)env->GetPrimitiveArrayCritical(sarray, NULL);
	    int nlastStrip = 0;

	    vb->primitiveType = d3dRenderType;
#ifdef VBDEBUG
	    if (createNew) {
		printf("Strip Length %d : ", strip_len);
	    }
#endif

	    if ((vb->totalIndexCount <= vb->indexCount) &&
		(vcount <= vb->vcount)) {
		copyIndexVertexToVB(d3dCtx, strideData,
				    indexCount, cDirty, false, false);
		copyVertexToVB(d3dCtx, strideData, vcount,
			       &vbptr, cDirty, false, xform, nxform);
		vb->addStrides(strip_len, strips);
	    } else {
		vb->buffer->Unlock();
		vbptr = NULL;		
		strideData->indexPtr += strideData->initialIndexIndex;
		strideData->initialIndexIndex = 0;
		for (i = 0; i < strip_len; i++) {
		    strideData->indexPtr += nlastStrip;

		    nlastStrip = strips[i];
		    
		    if (nlastStrip <= 0) {
			continue;
		    }
#ifdef VBDEBUG
		    if (createNew) {
			printf(" %d", nlastStrip);
		    }
#endif
		    if (nlastStrip <= vb->indexCount) {
			reIndexifyIndexVertexToVBs(d3dCtx, strideData, 
						   nlastStrip, vcount,
						   cDirty,
						   false,
						   maxVertexLimit, 
						   xform, nxform);
		    } else {
			// Multiple VBs for large vertex size
			splitIndexVertexToMultipleVB(d3dCtx,
						     strideData,
						     nlastStrip,
						     vcount,
						     maxVertexLimit,
						     cDirty,
						     false,
						     xform, nxform);
		    }
		    SafeDelete(d3dCtx->reIndexifyTable);
		}
#ifdef VBDEBUG
		if (createNew) {
		    printf("\n");
		}
#endif
	    }
	    env->ReleasePrimitiveArrayCritical(sarray, strips, NULL);
	  }

	    break;
        case GEO_TYPE_INDEXED_QUAD_SET:
#ifdef VBDEBUG
		if (createNew) {
		    printf("quad set %d\n", vcount);
		}
#endif
		if (buildDL ||
		    (d3dCtx->fillMode != D3DFILL_WIREFRAME)) {
		    d3dRenderType = D3DPT_TRIANGLELIST;
		    renderTypeSet = true;
		    expandQuadIndex = true;
		    // fall through
		} 
		// start quad WireFrame
		else { 
			// polygon line mode
		    // we don't want to see extra line appear in the
		    // diagonal of quads if it splits into two
		    // triangles. This is REALLY SLOW !!!
		    int posStride = strideData->positionStride;
		    D3DVERTEX worldCoord[3];
		    D3DTLVERTEX screenCoord[3];
		    jint *idxPtr = strideData->indexPtr;
		    jfloat *fspt;
		    jdouble *dspt;
		    jint idx0, idx1, idx2, idx3;

		    if (((vertexFormat & D3DFVF_DIFFUSE) == 0) && 
			(!d3dCtx->isLightEnable)) {
			d3dCtx->setAmbientLightMaterial();
		    }  

		    vb->buffer->Unlock();		    
		    vbptr = NULL;		
#ifdef VBDEBUG
		    if (createNew) {
			printf("indexed quad set polygon line %d\n", vcount);
		    }
#endif
		    if (d3dCtx->lineModeIndexBuffer == NULL) {
			createLineModeIndexBuffer(d3dCtx);
		    }

		    vb->primitiveType = D3DPT_FORCE_DWORD;

		    for (i = 0; i < (indexCount >> 2); i++) 
			{
		     if (d3dCtx->cullMode != D3DCULL_NONE) 
			 {
				// Do back face culling here
					idx0 = *idxPtr++;
					idx1 = *idxPtr++;
					idx2 = *idxPtr++;
					idx3 = *idxPtr++;

					if (strideData->fpositionPtr) 
					{
						fspt = strideData->fpositionPtr + posStride*idx0;
						worldCoord[0].x = *fspt++;
						worldCoord[0].y = *fspt++;
						worldCoord[0].z = *fspt++;
						fspt = strideData->fpositionPtr + posStride*idx1;
						worldCoord[1].x = *fspt++;
						worldCoord[1].y = *fspt++;
						worldCoord[1].z = *fspt++;
						fspt = strideData->fpositionPtr + posStride*idx2;
						worldCoord[2].x = *fspt++;
						worldCoord[2].y = *fspt++;
						worldCoord[2].z = *fspt++;
					} 
					else 
					{
						dspt = strideData->dpositionPtr + posStride*idx0;
						worldCoord[0].x = *dspt++;
						worldCoord[0].y = *dspt++;
						worldCoord[0].z = *dspt++;
						dspt = strideData->dpositionPtr + posStride*idx1;
						worldCoord[1].x = *dspt++;
						worldCoord[1].y = *dspt++;
						worldCoord[1].z = *dspt++;
						dspt = strideData->dpositionPtr + posStride*idx2;
						worldCoord[2].x = *dspt++;
						worldCoord[2].y = *dspt++;
						worldCoord[2].z = *dspt++;
					}

			    d3dCtx->transform(&worldCoord[0], &screenCoord[0]);
			    d3dCtx->transform(&worldCoord[1], &screenCoord[1]);
			    d3dCtx->transform(&worldCoord[2], &screenCoord[2]);
			    screenCoord[0].sx -= screenCoord[1].sx;  
			    screenCoord[0].sy -= screenCoord[1].sy;  
			    screenCoord[2].sx -= screenCoord[1].sx;  
			    screenCoord[2].sy -= screenCoord[1].sy;  

			    if (d3dCtx->cullMode == D3DCULL_CW) 
				{
					if ((screenCoord[0].sx*screenCoord[2].sy -
						screenCoord[2].sx*screenCoord[0].sy) >= 0) 
					{
						DrawIndexPolygonLine(d3dCtx,
								device,
								vertexFormat,
								strideData,
								idx0, idx1,
								idx2, idx3);
					} 
			    } 
				else 
				{ // Clip front face
					if ((screenCoord[0].sx*screenCoord[2].sy -
						screenCoord[2].sx*screenCoord[0].sy) <= 0)
					{
						DrawIndexPolygonLine(d3dCtx,
								device,
								vertexFormat,
								strideData,
								idx0, idx1,
								idx2, idx3);
					} 
			    }
			} 
			else 
			{
					// cullMode == D3DCULL_NONE
				DrawIndexPolygonLine(d3dCtx,
									device,
									vertexFormat,
									strideData,
									idx0, idx1,
									idx2, idx3);
			 }
		    }//for

		    if (((vertexFormat & D3DFVF_DIFFUSE) == 0) && 
			    (!d3dCtx->isLightEnable)) 
			{
			 d3dCtx->restoreDefaultLightMaterial();	
		    }
		    // Don't call vb->Renderer() at the end
		    return;
		}
		
		//end index Quad WireFrame

		// fall through
        case GEO_TYPE_INDEXED_TRI_SET:
	    if (renderTypeSet == false) {
		d3dRenderType = D3DPT_TRIANGLELIST;
		renderTypeSet = true;
#ifdef VBDEBUG
		if (createNew) {
		    printf("tri set %d\n", vcount);
		}
#endif
	    }
	    // fallthrough
        case GEO_TYPE_INDEXED_LINE_SET:
	    if (renderTypeSet == false) {
		d3dRenderType = D3DPT_LINELIST;
		renderTypeSet = true;
#ifdef VBDEBUG
		if (createNew) {
		    printf("line set %d\n", vcount);
		}
#endif
	    }
	    // fallthrough
        case GEO_TYPE_INDEXED_POINT_SET:
	    if (renderTypeSet == false) {
		d3dRenderType = D3DPT_POINTLIST;
#ifdef VBDEBUG
		if (createNew) {
		    printf("point set %d\n", vcount);
		}
#endif
	    }
	    vb->primitiveType = d3dRenderType;

	    if (vb->totalIndexCount <= vb->indexCount) {
		if (vcount <= vb->vcount) {
		    copyIndexVertexToVB(d3dCtx, strideData,  
					indexCount,
					cDirty, true, expandQuadIndex);
		    copyVertexToVB(d3dCtx, strideData, vcount, &vbptr,
				   cDirty, false, xform, nxform);
		} else {
		    vb->buffer->Unlock();
		    vbptr = NULL;
		    reIndexifyIndexVertexToVBs(d3dCtx, strideData, 
					       indexCount, vcount,
					       cDirty,
					       expandQuadIndex,
					       maxVertexLimit, 
					       xform, nxform);
		    SafeDelete(d3dCtx->reIndexifyTable);
		}
	    } else {
		vb->buffer->Unlock();
		vbptr = NULL;
		splitIndexVertexToMultipleVB(d3dCtx, 
					     strideData, 
					     indexCount,
					     vcount,
					     maxVertexLimit,
					     cDirty,
					     expandQuadIndex,
					     xform, nxform);
		SafeDelete(d3dCtx->reIndexifyTable);
	    }
	    break;
        default:
            printf("IndexedGeometryArrayRetained_execute:unknown geo_type %ld \n", geo_type);
    }

    if (vbptr != NULL) {
	// d3dCtx->pVB is the last reference in VB list
	d3dCtx->pVB->buffer->Unlock();
    }

    // Save new texture position to detect any change
    // in next round.
    for (i=0; i < d3dCtx->texSetUsed; i++) {
	 d3dCtx->pVB->texCoordPosition[i] =
	     strideData->texCoordPosition[i];
    }

    if (!buildDL) { 
	// Not in displaylist mode, directly render VB
	// vb is the root reference in VB list
	vb->render(d3dCtx);
    }
}


/*
 * Set the default texture coordinate pointers when automatically
 * texture generation is used or when there is application error
 */
inline void setDefaultTextureCoordPointers(D3dCtx *d3dCtx,
					   D3DDRAWPRIMITIVESTRIDEDDATA *strideData,
					   DWORD* vertexFormat,
					   jint ts,
					   int genMode,
					   int tus)
{
    strideData->textureCoordsPtr[tus] = &defaultTexCoord[0];
    strideData->textureCoordsStride[tus] = 0;
    strideData->texCoordPosition[tus] = genMode;
    d3dCtx->texStride[tus] = ts;
    *vertexFormat |= (TexFormatSizeTable[ts] << (tus*2 + 16));
}


/*
 * Set the textureCoordStride & textureCoordsPtr in strideData
 */
void setTextureCoordPointers(JNIEnv *env,
			     D3dCtx* d3dCtx,  
			     LPDIRECT3DDEVICE9 device, 
			     D3DDRAWPRIMITIVESTRIDEDDATA *strideData,
			     jint pass,
			     jint texoff,
			     jint texStride,
			     jint ts,
			     jboolean textureDefined,
			     jintArray tcoordsetmap,
			     jint texCoordMapLength, 
			     jintArray texUnitOffset,
			     jint numActiveTexUnit,
			     jintArray tunitstatemap,
			     DWORD* vertexFormat,
			     // Used by executeGeometryArray() & 
			     // executeIndexedGeometryArray() only
			     jfloat* verts, 
			     // Used by executeGeometryArrayVA() & 
			     // executeIndexedGeometryArrayVA() only
			     jfloat** texCoordPointer,
			     // Used by executeGeometryArrayVA() only 
			     jintArray texindices)
			     
{
    jint *texUnitStateMap = NULL;
    jint *texCoordSetMapOffset = NULL;
    jint *initialTexIndices = NULL;
    jint *texCoordSetMap = NULL;
    jint textureUnitIndex;
    int genMode;
    jint offsetOrTexset;
    BOOL nonVAGeom = (verts != NULL);
    int texSetInVB = 0;

    /*
     * In case of automatic texture generation, 
     * (vformat & GA_TEXTURE_COORDINATE) may equal to zero so we 
     * can't wrap around this whole block with the condition.
     */
    d3dCtx->texSetUsed = 0;

    // For GA buildGA(), numActiveTexUnit is 1 even though texture
    // is not used. This is only accurate for execute() immediate case.
    if (numActiveTexUnit <= 0) {
	return;
    }

    if (texCoordMapLength > 0) {
	if (nonVAGeom) {
	    // In executeGeometryArray() or executeIndexedGeometryArray()	    
	    texCoordSetMapOffset = (jint *) env->GetPrimitiveArrayCritical(texUnitOffset, NULL);
	} else {
	    // In executeGeometryArrayVA() or executeIndexedGeometryArrayVA()
	    texCoordSetMap = (jint *)
		env->GetPrimitiveArrayCritical(tcoordsetmap, NULL);
	}

    }

    if (texindices != NULL) {
	// In executeGeometryArrayVA()
	initialTexIndices = (jint *) env->GetPrimitiveArrayCritical(texindices, NULL);
    }

    if (pass >= 0) {
	/* 
	 * Single texture coordinate used or in multi-pass mode.
	 * In multi-pass mode, texture stage 0 is set so we 
	 * should not use
	 *     setTextureStage(d3dCtx, device, 0, pass);
	 */

	genMode = setTextureStage(d3dCtx, device, 0, 0);

#ifdef TEXDEBUG    
	printf("*** Single Pass *** %d, nonVAGeom %d, buildDL %d, numActiveTexUnit, texCoordMapLength %d, texDef %d, genMode %d \n", pass,  nonVAGeom, (d3dCtx->currDisplayListID > 0),  numActiveTexUnit, texCoordMapLength, textureDefined, genMode);
#endif

	if ((texCoordMapLength > 0) &&
	    (pass < texCoordMapLength)) {
	    offsetOrTexset = (nonVAGeom ? 
			      texCoordSetMapOffset[pass] : texCoordSetMap[pass]);
	} else {
	    // TexUnitState is not used or disable,
	    // so by default we use texUnitIndex 0   
	    offsetOrTexset = 0;
	}

	if (genMode != TEX_OBJ_LINEAR) {
	    // Only Object Linear Mode will not use DisplayList at
	    // all. That mean genMode must be correctly set before
	    // enter this Geometry execute() function/
	    // In non displaylist mode genMode may not set correctly
	    // when so we can't rely on it.
	    if (textureDefined) {
		if (offsetOrTexset != -1) {
		    if (nonVAGeom) {
			// In executeGeometryArray() or executeIndexedGeometryArray()	    
			strideData->textureCoordsPtr[0] = 
			    &verts[texoff + offsetOrTexset];
		    } else if (initialTexIndices != NULL) {
			//  executeGeometryArrayVA()
			strideData->textureCoordsPtr[0] = 
			    &(texCoordPointer[offsetOrTexset][initialTexIndices[offsetOrTexset]*texStride]);
		    } else { // executeIndexedGeometryArrayVA()
			strideData->textureCoordsPtr[0] = 
			    &(texCoordPointer[offsetOrTexset][0]);
		    }

		    strideData->textureCoordsStride[0] = texStride;
		    *vertexFormat |= (TexFormatSizeTable[ts] << 16);
		    d3dCtx->texSetUsed = 1;
		    strideData->texCoordPosition[0] = offsetOrTexset;
		    d3dCtx->texStride[0] = ts;
		    setTexTransformStageFlag(d3dCtx, device, 0, ts, genMode);
#ifdef TEXDEBUG    
		    printf("[pass 0] Non Object Linear, texDefined, ts=%d\n", ts);
#endif
		} 
	    } else {
		// may be automatic texture generation used
		// TexSetUsed remain unchange i.e. 0 since we will not
		// allocate VB space for texture coords.
		setTexTransformStageFlag(d3dCtx, device, 0, 
					 d3dCtx->texCoordFormat[0], genMode);
#ifdef TEXDEBUG    
		printf("[pass 0] Non Object Linear, No texDefined, ts=%d\n",
			   d3dCtx->texCoordFormat[0]);
#endif
	    }
	} else {
	    // Automatic texture generation Object Linear 
	    //  strideData->textureCoordsPtr[0] &
	    //  strideData->textureCoordsStride[0]
	    // are not use in VertexBuffer texture copy so
	    // it doesn't matter to set them using default.
	    setDefaultTextureCoordPointers(d3dCtx, strideData,
					   vertexFormat, 
					   d3dCtx->texCoordFormat[0],
					   genMode, 0);
	    setTexTransformStageFlag(d3dCtx, device, 0, 
				     d3dCtx->texCoordFormat[0], genMode);
	    d3dCtx->texSetUsed = 1;
#ifdef TEXDEBUG    
	    printf("[pass 0] Object Linear, ts=%d\n", d3dCtx->texCoordFormat[0]);
#endif
	} 
	texSetInVB = d3dCtx->texSetUsed;

    } else {
	// DisplayList is used for multiple texture single pass mode
	// Or when go through the VertexArray in OGL, pass = -1
	int tus;
#ifdef TEXDEBUG    
	printf("*** Multiple Pass *** %d, nonVAGeom %d, buildDL %d, numActiveTexUnit %d, texCoordMapLength %d, texDef %d\n", pass,
	   nonVAGeom, (d3dCtx->currDisplayListID > 0),
	   numActiveTexUnit, texCoordMapLength, textureDefined);
#endif

	if (tunitstatemap != NULL) {
	    texUnitStateMap = (jint *) env->GetPrimitiveArrayCritical(tunitstatemap, NULL);
	}
	for (textureUnitIndex = 0; textureUnitIndex <  numActiveTexUnit; 
	     textureUnitIndex++) {

	    tus = (texUnitStateMap != NULL ? 
		   texUnitStateMap[textureUnitIndex]: textureUnitIndex);

	    if (d3dCtx->currDisplayListID > 0) {
		genMode = setTextureStage(d3dCtx, device,
					  textureUnitIndex, tus);
	    } else {
		// This happen when we switch from displayList to
		// vertexArray mode. The displayList is already
		// built with 1-1 mapping so we can't use the 
		// textureUnitIndex Mapping
		genMode = setTextureStage(d3dCtx, device,
					  textureUnitIndex, 
					  textureUnitIndex); 
	    }
#ifdef TEXDEBUG
	    printf("[pass %d] genMode %d, tus %d\n", textureUnitIndex,
		   genMode, tus);
#endif
	    if (genMode != TEX_OBJ_LINEAR) {
		if (textureDefined) {
		    if (tus < texCoordMapLength) {
			offsetOrTexset = (nonVAGeom ?
					  texCoordSetMapOffset[tus]:texCoordSetMap[tus]);

			if (offsetOrTexset != -1) {
			    if (nonVAGeom) {
				strideData->textureCoordsPtr[textureUnitIndex]  =
				    &verts[texoff + offsetOrTexset];
			    } else if (initialTexIndices != NULL) {
				strideData->textureCoordsPtr[textureUnitIndex] = 
				    &(texCoordPointer[offsetOrTexset][initialTexIndices[offsetOrTexset]*texStride]);
			    } else {
				strideData->textureCoordsPtr[textureUnitIndex] = 
				    &(texCoordPointer[offsetOrTexset][0]);
			    }
			    strideData->textureCoordsStride[textureUnitIndex] = texStride;
			    strideData->texCoordPosition[textureUnitIndex]
				= offsetOrTexset;
			    *vertexFormat |= (TexFormatSizeTable[ts] << (textureUnitIndex*2 + 16));
			    d3dCtx->texStride[textureUnitIndex] = ts;
			    setTexTransformStageFlag(d3dCtx, device,
						     textureUnitIndex,
						     ts, genMode);
			    texSetInVB++;
#ifdef TEXDEBUG    
			    printf("[pass %d] Non Object Linear, texDefined, ts=%d, tus %d\n", textureUnitIndex, ts, tus);
#endif
			} else {
			    // This texture status is disable, this
			    // should not happen in D3D since
			    // TextureBin always compact unused state
			    // - unless when DisplayList is build and
			    // automatic texture generation
			    // used. Since if DL use
			    // updateAttributes() not yet invoke to
			    // set genMode correctly.
			    if (debug && (d3dCtx->currDisplayListID <= 0)) {
				printf("[Java3D] TextureBin not compact textureUnitState correctly, numActiveTex %d, disableUnit %d, current mapped Texture Unit %d\n", numActiveTexUnit, tus, textureUnitIndex);
			    }
			    setDefaultTextureCoordPointers(d3dCtx, strideData,
							   vertexFormat, ts,
							   genMode,
							   textureUnitIndex);
			    setTexTransformStageFlag(d3dCtx, device,
						     textureUnitIndex,
						     ts, genMode);
			    texSetInVB++;
#ifdef TEXDEBUG    
			    printf("[pass %d] Non Object Linear, texDefined, ts=%d\n", textureUnitIndex, ts);
#endif
			}
		    } else {
			// Internal error, should not happen.
			if (debug) {
			    printf("[Java3D] TextureCoordMapLength length %d, is smaller than texture unit %d, map texture unit %d\n", texCoordMapLength, tus, textureUnitIndex);
			}
			setDefaultTextureCoordPointers(d3dCtx, strideData,
						       vertexFormat, ts,
						       TEX_GEN_INVALID,
						       textureUnitIndex);
			setTexTransformStageFlag(d3dCtx, device,
						 textureUnitIndex, ts,
						 genMode);
						 
			texSetInVB++;
#ifdef TEXDEBUG    
			printf("[pass %d] Non Object Linear, texDefined,  offset/texset = -1, ts=%d\n", textureUnitIndex, ts);
#endif
		    }
		} else {
		    // May be in automatically texture coordinate
                    // generation mode.
		    // May have trouble if automatically texture
		    // coordinate not used. Note that we pass ts = 0 
		    // so that copyVertexToVB() will not inc. the
		    // stride for this unused tex stage.

		    // It is also the case in buildGA() case when
		    // numActiveTexUnit is 1 by default even though
		    // texture is not used.
		    /*
		    if ((d3dCtx->currDisplayListID <= 0) && 
			(genMode == TEX_GEN_NONE))  {
			// application error, use default TexCoord
			setDefaultTextureCoordPointers(d3dCtx, strideData,
						       vertexFormat, 
						       ts,
						       TEX_GEN_NONE,
						       textureUnitIndex);
			texSetInVB++;
                       }
		    */

		    setDefaultTextureCoordPointers(d3dCtx, strideData,
						   vertexFormat, 
						   0,
						   // This must be < 0
						   TEX_GEN_AUTO,
						   textureUnitIndex);
		    setTexTransformStageFlag(d3dCtx, device, textureUnitIndex, 
					     d3dCtx->texCoordFormat[textureUnitIndex], genMode);


#ifdef TEXDEBUG    
		    printf("[pass %d] Non Object Linear, No texDefined, ts=0\n", textureUnitIndex);
#endif
		}
	    } else {
		// Automatic Texture generation Object Linear is used
		setDefaultTextureCoordPointers(d3dCtx, strideData,
					       vertexFormat, 
					       d3dCtx->texCoordFormat[textureUnitIndex],
					       genMode,
					       textureUnitIndex);
		setTexTransformStageFlag(d3dCtx, device, textureUnitIndex, 
					 d3dCtx->texCoordFormat[textureUnitIndex], genMode);
		texSetInVB++;
#ifdef TEXDEBUG    
		printf("[pass %d] Object Linear, No texDefined, ts=%d\n", textureUnitIndex,  d3dCtx->texCoordFormat[textureUnitIndex]);
#endif
	    }
	}
	d3dCtx->texSetUsed = numActiveTexUnit;
#ifdef TEXDEBUG    
	printf("No of texSetInVB %d, numActiveTexUnit %d\n",
	       texSetInVB, numActiveTexUnit);
#endif
	if (texUnitStateMap != NULL) {
	    env->ReleasePrimitiveArrayCritical(tunitstatemap, texUnitStateMap, NULL);
	}
    }

    if (texCoordSetMapOffset != NULL) {
	env->ReleasePrimitiveArrayCritical(texUnitOffset,
					   texCoordSetMapOffset, NULL);
    }

    if (texCoordSetMap != NULL) {
	env->ReleasePrimitiveArrayCritical(tcoordsetmap,
					   texCoordSetMap, NULL);
    }

    if (initialTexIndices != NULL) {
	env->ReleasePrimitiveArrayCritical(texindices,
					   initialTexIndices, NULL);	
    }

    // texSetInVB <= d3dCtx->TexSetUsed
    *vertexFormat |= getVertexFormat(texSetInVB);
}



void executeGeometryArrayVA(
    JNIEnv *env, 
    jobject obj,
    jlong ctx,
    jobject geo,
    jint geo_type, 
    jboolean isNonUniformScale,
    jboolean modAlpha,
    float alpha,
    jboolean multiScreen,
    jboolean ignoreVertexColors,				   
    jint vcount,
    jint vformat,
    jint vdefined,
    jint initialCoordIndex,
    jfloat* fverts,
    jdouble* dverts,
    jint initialColorIndex,
    jfloat* fclrs,
    jbyte*  bclrs,
    jint initialNormalIndex,
    jfloat* norms,
    int pass, // or texUnitIndex
    int texCoordMapLength,
    jintArray 	tcoordsetmap,
    jint numActiveTexUnit,
    jintArray tunitstatemap,
    jintArray texindices,
    jint texStride,
    jfloat** texCoordPointer,
    jdoubleArray xform, 
    jdoubleArray nxform,
    jint cDirty)
{
    D3DDRAWPRIMITIVESTRIDEDDATA strideData;
    DWORD vertexFormat = 0;
    
    jboolean floatCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_FLOAT) != 0);
    jboolean doubleCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_DOUBLE) != 0);
    jboolean floatColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_FLOAT) != 0);
    jboolean byteColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_BYTE) != 0);
    jboolean normalsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_NORMAL_FLOAT) != 0);
    jboolean textureDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_TEXCOORD_FLOAT) != 0);
   
    GetDevice();

    ZeroMemory(&strideData, sizeof(D3DDRAWPRIMITIVESTRIDEDDATA));

    strideData.modulateAlpha = modAlpha;
    strideData.alpha = alpha;


    // setup coordinate pointers
    if (floatCoordDefined || doubleCoordDefined) {
	vertexFormat |= D3DFVF_XYZ;

	if (floatCoordDefined) {
	    strideData.fpositionPtr = &fverts[initialCoordIndex*3];
	} else if (doubleCoordDefined) {
	    strideData.dpositionPtr = &dverts[initialCoordIndex*3];
	}
	
	strideData.positionStride = 3;
	
    } else {
	// nothing worth doing if no coordinates define
	return;
    }

    // setup normal pointers
    if (normalsDefined) {
        vertexFormat |= D3DFVF_NORMAL;
        strideData.normalPtr = &norms[initialNormalIndex*3];
        strideData.normalStride = 3;
    }
    
    
    // setup color pointers
    if (!(floatColorsDefined || byteColorsDefined)
	|| ignoreVertexColors) {
	// Use Material color
	// Assume VertexBuffer will recreate when ignoreVertexColors
	// property changed. Otherwise we need to remove
	// the following one line
	vertexFormat &= ~D3DFVF_DIFFUSE;
    } else {
	if ((vformat & GA_WITH_ALPHA) != 0) {
	    strideData.diffuseStride = 4;
	    strideData.useAlpha = true;
	} else {
	    strideData.diffuseStride = 3;
	    strideData.useAlpha = false;
	}
	if (floatColorsDefined) {
	    strideData.fdiffusePtr = &fclrs[initialColorIndex*strideData.diffuseStride];
	} else {
	    strideData.bdiffusePtr = &bclrs[initialColorIndex*strideData.diffuseStride];
	} 

	vertexFormat |= D3DFVF_DIFFUSE;
    } 
    
    int ts = 2; // In case of automatic texture generation

    if ((vformat & GA_TEXTURE_COORDINATE_3) != 0) {
	ts = 3;
    } else if ((vformat & GA_TEXTURE_COORDINATE_4) != 0) {
	ts = 4;
    } 


    // setup texture pointer
    setTextureCoordPointers(env, d3dCtx, device,
			    &strideData,
			    pass, 0, texStride, ts,
			    textureDefined,
			    tcoordsetmap,
			    texCoordMapLength, 
			    NULL,
			    numActiveTexUnit,
			    tunitstatemap,
			    &vertexFormat,
			    NULL, texCoordPointer, 
			    texindices);

    

    jdouble* xform_ptr = NULL;
    jdouble* nxform_ptr = NULL;

    if (xform != NULL) {
        xform_ptr = (jdouble *) env->GetPrimitiveArrayCritical(xform, NULL);

    }

    if (nxform != NULL) {
        nxform_ptr = (jdouble *) env->GetPrimitiveArrayCritical(nxform, NULL);
    }

    // Construct/update VertexBuffer, render() if not in display list mode 
    renderGeometry(env, d3dCtx, device, geo, geo_type, &strideData,
		   vertexFormat, vcount, xform_ptr, nxform_ptr, cDirty);

    if (xform_ptr != NULL) {
	env->ReleasePrimitiveArrayCritical(xform, xform_ptr, 0);
    }
    if (nxform_ptr != NULL) {
	env->ReleasePrimitiveArrayCritical(nxform, nxform_ptr, 0);
    }

}




/* execute geometry array with java array format */
extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_GeometryArrayRetained_executeVABuffer(
   JNIEnv *env, 
    jobject obj,
    jlong ctxInfo,    
    jobject geo,
    jint geo_type, 
    jboolean isNonUniformScale,
    jboolean multiScreen,
    jboolean ignoreVertexColors,
    jint vcount,
    jint vformat,
    jint vdefined,
    jint initialCoordIndex,
    jobject vcoords,
    jint initialColorIndex,
    jobject cdataBuffer,
    jfloatArray cfdata,
    jbyteArray  cbdata,    
    jint initialNormalIndex,
    jobject ndata,
    jint vertexAttrCount,
    jintArray vertexAttrSizes,
    jintArray vertexAttrIndices,
    jobjectArray vertexAttrData,
    jint pass,  
    jint texCoordMapLength,
    jintArray 	tcoordsetmap,
    jint numActiveTexUnit,
    jintArray tunitstatemap,
    jintArray texindices,
    jint texStride,
    jobjectArray texCoords,
    jint cdirty)
{
    
    jfloat *fverts = NULL;
    jdouble *dverts = NULL ;
    jbyte *bclrs = NULL;
    jfloat *fclrs = NULL, *norms = NULL;
    jfloat* texCoordPointer[D3DDP_MAXTEXCOORD];
    jarray texobjs[D3DDP_MAXTEXCOORD];
    int i;
    
    jboolean floatCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_FLOAT) != 0);
    jboolean doubleCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_DOUBLE) != 0);
    jboolean floatColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_FLOAT) != 0);
    jboolean byteColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_BYTE) != 0);
    jboolean normalsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_NORMAL_FLOAT) != 0);
    jboolean textureDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_TEXCOORD_FLOAT) != 0);

    if (textureDefined) {
	for (i = 0; i < texCoordMapLength; i++) {
	    texobjs[i] = (jarray)env->GetObjectArrayElement(texCoords, i);
	}
    }

    /* get coordinate array */
    if (floatCoordDefined) {
	fverts = (jfloat *)env->GetDirectBufferAddress( vcoords );
    } else if (doubleCoordDefined) {
	dverts = (jdouble *)env->GetDirectBufferAddress( vcoords ); 
    }

    if(fverts == NULL && dverts == NULL)
	return; 

    /* get color array */
    if (floatColorsDefined) {
	if(cfdata != NULL) {
	    fclrs = (jfloat *) env->GetPrimitiveArrayCritical( cfdata, NULL);
	} else {
	    fclrs = (jfloat *) env->GetDirectBufferAddress (cdataBuffer);

	}
    }
    else if (byteColorsDefined) {
	if(cbdata != NULL) {
	    bclrs = (jbyte *) env->GetPrimitiveArrayCritical( cbdata, NULL);
	} else {
	    bclrs = (jbyte *) env->GetDirectBufferAddress(cdataBuffer);
	}
    }

    /* get normal array */
    if (normalsDefined) {
	norms = (jfloat *)env->GetDirectBufferAddress(ndata);
    }

    /* get texture arrays */
    if (textureDefined) {
	for (i = 0; i < texCoordMapLength; i++) {
	    if (texobjs[i] != NULL)
		texCoordPointer[i] = (jfloat*)env->GetDirectBufferAddress(texobjs[i]);
	    else
		texCoordPointer[i] = NULL;	
	    
	}	
    }

    executeGeometryArrayVA(env, obj, ctxInfo, geo, geo_type,
			   isNonUniformScale, false, 0, multiScreen, ignoreVertexColors,
			   vcount, vformat,  vdefined, initialCoordIndex,
			   fverts, dverts, initialColorIndex,
			   fclrs, bclrs, initialNormalIndex,
			   norms, 
			   pass, texCoordMapLength,
			   tcoordsetmap,numActiveTexUnit, tunitstatemap,
			   texindices,texStride,texCoordPointer, NULL,
			   NULL, cdirty);
    
    if(floatColorsDefined && cfdata != NULL)
	env->ReleasePrimitiveArrayCritical( cfdata, fclrs, 0);
    else if(byteColorsDefined && cbdata != NULL)
	env->ReleasePrimitiveArrayCritical(cbdata, bclrs, 0);
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_GeometryArrayRetained_executeVA(
    JNIEnv *env, 
    jobject obj,
    jlong ctxInfo,    
    jobject geo,
    jint geo_type, 
    jboolean isNonUniformScale,
    jboolean multiScreen,
    jboolean ignoreVertexColors,
    jint vcount,
    jint vformat,
    jint vdefined,
    jint initialCoordIndex,
    jfloatArray vfcoords,
    jdoubleArray vdcoords,
    jint initialColorIndex,
    jfloatArray cfdata,
    jbyteArray  cbdata,
    jint initialNormalIndex,
    jfloatArray ndata,
    jint vertexAttrCount,
    jintArray vertexAttrSizes,
    jintArray vertexAttrIndices,
    jobjectArray vertexAttrData,
    jint pass,  
    jint texCoordMapLength,
    jintArray 	tcoordsetmap,
    jint numActiveTexUnit,
    jintArray tunitstatemap,
    jintArray texindices,
    jint texStride,
    jobjectArray texCoords,
    jint cdirty)
   {
    
    jfloat *fverts = NULL;
    jdouble *dverts = NULL;
    jfloat *fclrs = NULL;
    jbyte *bclrs = NULL;    
    jfloat *norms = NULL;
    jfloat* texCoordPointer[D3DDP_MAXTEXCOORD];
    jarray texobjs[D3DDP_MAXTEXCOORD];
    int i;
    
    jboolean floatCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_FLOAT) != 0);
    jboolean doubleCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_DOUBLE) != 0);
    jboolean floatColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_FLOAT) != 0);
    jboolean byteColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_BYTE) != 0);
    jboolean normalsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_NORMAL_FLOAT) != 0);
    jboolean textureDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_TEXCOORD_FLOAT) != 0);

    if (textureDefined) {
	for (i = 0; i < texCoordMapLength; i++) {
	    texobjs[i] = (jarray)env->GetObjectArrayElement( texCoords, i);
	}
    }

    /* get coordinate array */
    if (floatCoordDefined) {
	fverts= (jfloat *) env->GetPrimitiveArrayCritical( vfcoords, NULL);
    } else if (doubleCoordDefined) {
	dverts= (jdouble *) env->GetPrimitiveArrayCritical( vdcoords, NULL);
    }

    if ((fverts == NULL) && (dverts == NULL)) {
	return; 
    }

    /* get color array */
    if (floatColorsDefined) {
	fclrs = (jfloat *) env->GetPrimitiveArrayCritical( cfdata, NULL);
    } else if (byteColorsDefined) {
	bclrs = (jbyte *)env->GetPrimitiveArrayCritical( cbdata, NULL);
    }

    /* get normal array */
    if (normalsDefined) {
	norms = (jfloat *) env->GetPrimitiveArrayCritical(ndata, NULL);
    }

    /* get texture arrays */
    if (textureDefined) {
	for (i = 0; i < texCoordMapLength; i++) {
	    if (texobjs[i] != NULL) {
		texCoordPointer[i] = (jfloat*)env->GetPrimitiveArrayCritical(texobjs[i], NULL);
	    } else {
		texCoordPointer[i] = NULL;	
	    }
	    
	}	
    }
    executeGeometryArrayVA(env, obj, ctxInfo, geo, geo_type,
			   isNonUniformScale, false, 0,
			   multiScreen, ignoreVertexColors,
			   vcount, vformat,  vdefined, initialCoordIndex,
			   fverts, dverts, initialColorIndex,
			   fclrs, bclrs, initialNormalIndex,
			   norms, pass, texCoordMapLength,
			   tcoordsetmap,numActiveTexUnit, tunitstatemap,
			   texindices,texStride,texCoordPointer,
			   NULL, NULL, cdirty);
    
    if (floatCoordDefined) {
	env->ReleasePrimitiveArrayCritical( vfcoords, fverts, 0); 
    }
    else if (doubleCoordDefined) {
	env->ReleasePrimitiveArrayCritical( vdcoords, dverts, 0); 
    }

    if (floatColorsDefined) {
	env->ReleasePrimitiveArrayCritical( cfdata, fclrs, 0); 
    }
    else if (byteColorsDefined) {
	env->ReleasePrimitiveArrayCritical( cbdata, bclrs, 0);
    }

    if (normalsDefined) {
	env->ReleasePrimitiveArrayCritical( ndata, norms, 0);
    }
    
    if (textureDefined) {
	for (i = 0; i < texCoordMapLength; i++) {
	    if (texCoordPointer[i] != NULL) {
		env->ReleasePrimitiveArrayCritical(texobjs[i], texCoordPointer[i], 0);
	    }
	}
    }    
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_GeometryArrayRetained_setGlobalAlpha(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jfloat alpha)
{
    // not use in D3D
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_GeometryArrayRetained_disableGlobalAlpha(
    JNIEnv *env, 
    jobject obj,
    jlong ctx,
    jint vformat,
    jboolean useAlpha,
    jboolean ignoreVertexColors) 
{
    // not use in D3D
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_GeometryArrayRetained_setVertexFormat(
    JNIEnv *env, 
    jobject obj,
    jlong ctxInfo,
    jint vformat,
    jboolean useAlpha,
    jboolean ignoreVertexColors)
{
    // not use in D3D
}


extern "C" JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_GeometryArrayRetained_globalAlphaSUN
    (JNIEnv *env, jobject obj, jlong ctx)
{
    return JNI_FALSE;
}



void executeGeometryArray(JNIEnv *env,
			  jobject obj, jlong ctx,
			  jobject geo, jint geo_type,
			  jboolean isNonUniformScale, 
			  jboolean modAlpha, // buildGA, should alpha be mode
			  jfloat alpha,
			  jboolean multiScreen,
			  jboolean ignoreVertexColors,
			  jint startVIndex,
			  jint vcount, jint vformat, jint texCoordSetCount,
			  jintArray texCoordSetMapArray,
			  jint texCoordMapLength, 
			  jintArray texUnitOffset,
			  jint numActiveTexUnit,
			  jintArray tunitstatemap,
			  jfloat* verts, jfloatArray carray, jint pass,
			  jdoubleArray xform, jdoubleArray nxform,
			  jint cDirty,
			  jboolean useAlpha) // Should alpha be sent down
{
    D3DDRAWPRIMITIVESTRIDEDDATA strideData;
    DWORD vertexFormat = 0;
    jint stride, coordoff, normoff, coloroff, texoff;
    int texStride, ts = 0;

    GetDevice();

    ZeroMemory(&strideData, sizeof(D3DDRAWPRIMITIVESTRIDEDDATA));

    strideData.modulateAlpha = modAlpha;
    strideData.alpha = alpha;

    /* This matches the code in GeometryArrayRetained.java */
    stride = coordoff = normoff = coloroff = texoff = 0;
    
    if ((vformat & GA_COORDINATES) != 0) {
        stride += 3;
        vertexFormat |= D3DFVF_XYZ;
    } else {
	// nothing worth doing if no coordinates define
	return;
    }

    if ((vformat & GA_NORMALS) != 0) {
        stride += 3;
        coordoff += 3;
        vertexFormat |= D3DFVF_NORMAL;
    }

    if ((vformat & GA_COLOR) != 0) {
	if ((vformat & GA_WITH_ALPHA) != 0 ) {
	    stride += 4;
	    normoff += 4;
	    coordoff += 4;
	} else { // Handle the case of executeInterleaved 3f
	    stride += 3;
	    normoff += 3;
	    coordoff += 3;
	}
	vertexFormat |= D3DFVF_DIFFUSE;
    }

    // In case of automatic texture generation
    ts = 2;
    
    if (vformat & GA_TEXTURE_COORDINATE) {
	if ((vformat & GA_TEXTURE_COORDINATE_2) != 0) {
	    texStride = texCoordSetCount << 1;
	} else if ((vformat & GA_TEXTURE_COORDINATE_3) != 0) {
	    ts = 3;
	    texStride = texCoordSetCount*3;
	} else { // GA_TEXTURE_COORDINATE_4
	    ts = 4;
	    texStride = texCoordSetCount << 2;
	}
        stride += texStride;
        normoff += texStride;
        coloroff += texStride;
        coordoff += texStride;
    }
    
    jfloat *cverts = NULL;

    texoff = startVIndex*stride;
    coordoff += texoff;
    normoff += texoff;

    if (carray != NULL) {
	// separate color array is used
	coloroff = startVIndex*4;
    } else {
	coloroff += texoff;
    }
    
    // setup coordinates pointer
    strideData.fpositionPtr = &verts[coordoff];
    strideData.positionStride = stride;

    // setup color pointer
    if (((vformat & GA_COLOR) == 0) || ignoreVertexColors) {
	// Use Material color
	// Assume VertexBuffer will recreate when ignoreVertexColors
	// property changed. Otherwise we need to remove
	// the following one line
	vertexFormat &= ~D3DFVF_DIFFUSE;
    } else {
	if (carray == NULL) {
	    strideData.fdiffusePtr = &verts[coloroff];
	    strideData.diffuseStride = stride;
	    strideData.useAlpha = (vformat & GA_WITH_ALPHA);
	} else {
	    cverts = (jfloat*) env->GetPrimitiveArrayCritical(carray, NULL);
	    strideData.fdiffusePtr = &cverts[coloroff];
	    strideData.diffuseStride = 4;
	    strideData.useAlpha = true;	    
	}
    }


    // setup normal pointer
    if ((vformat & GA_NORMALS) != 0) {
        strideData.normalPtr = &verts[normoff];
        strideData.normalStride = stride;
    } 

    // setup texture pointer
    setTextureCoordPointers(env, d3dCtx, device,
			    &strideData,
			    pass, texoff, stride, ts,
			    (vformat & GA_TEXTURE_COORDINATE),
			    NULL,
			    texCoordMapLength, 
			    texUnitOffset,
			    numActiveTexUnit,
			    tunitstatemap,
			    &vertexFormat,
			    verts, NULL, NULL);

    
    jdouble* xform_ptr = NULL;
    jdouble* nxform_ptr = NULL;

    if (xform != NULL) {
        xform_ptr = (jdouble *) env->GetPrimitiveArrayCritical(xform, NULL);

    }

    if (nxform != NULL) {
        nxform_ptr = (jdouble *) env->GetPrimitiveArrayCritical(nxform, NULL);
    }
		       
    renderGeometry(env, d3dCtx, device, geo, geo_type, &strideData,
	           vertexFormat, vcount, xform_ptr, nxform_ptr, cDirty);
    
    if (xform_ptr != NULL) {
	env->ReleasePrimitiveArrayCritical(xform, xform_ptr, 0);
    }
    if (nxform_ptr != NULL) {
	env->ReleasePrimitiveArrayCritical(nxform, nxform_ptr, 0);
    }


    /* env->ReleasePrimitiveArrayCritical(varray, verts, NULL); */

    if (cverts != NULL) {
	env->ReleasePrimitiveArrayCritical(carray, cverts, NULL);	
    }
}


/*
 * Class:     javax_media_j3d_GeometryArrayRetained
 * Method:    buildGA
 * Signature: (JLjavax/media/j3d/GeometryArrayRetained;IZZFZIIII[II[II[I[D[D[F)V
 */
extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_GeometryArrayRetained_buildGA(JNIEnv *env, 
		jobject obj, jlong ctx, jobject geo, 
		jint geo_type, 
        jboolean isNonUniformScale, jboolean updateAlpha, float alpha,
		jboolean ignoreVertexColors,
		jint startVIndex,
  	    jint vcount, jint vformat, 
		jint texCoordSetCount, 
        jintArray texCoordSetMapArray,
		jint texCoordMapLength,
		jintArray texUnitOffset, 
		jint vertexAttrCount, jintArray vertexAttrSizes,
		jdoubleArray xform, jdoubleArray nxform,			
		jfloatArray varray)
{

    jfloat *verts = NULL;

    if (varray != NULL) {
	verts = (jfloat *) env->GetPrimitiveArrayCritical(varray, NULL);
    }

    if (verts == NULL) {
	return;
    }

    if (((vformat & GA_COLOR) != 0) &&
	((vformat & GA_BY_REFERENCE) == 0)) {
	// alpha component is added for buildGA
	vformat |= GA_WITH_ALPHA;
    }


    executeGeometryArray(env,
			 obj, ctx, geo,  geo_type, isNonUniformScale, 
			 updateAlpha, 
			 alpha,
			 false,
			 ignoreVertexColors,
			 startVIndex,
			 vcount,  
			 vformat,
			 texCoordSetCount,
			 texCoordSetMapArray,		
			 texCoordMapLength, 
			 texUnitOffset,
			 texCoordMapLength,
			 NULL,
			 verts, NULL, 
			 -1,
			 xform, nxform, 
			 javax_media_j3d_GeometryArrayRetained_VERTEX_CHANGED, 
			 false);

    env->ReleasePrimitiveArrayCritical( varray, verts, 0);
}



extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_GeometryArrayRetained_execute(JNIEnv *env,
                jobject obj, jlong ctx,jobject geo, jint geo_type,
                jboolean isNonUniformScale, jboolean useAlpha,
                jboolean multiScreen,
                jboolean ignoreVertexColors,
                jint startVIndex,
                jint vcount, jint vformat, jint texCoordSetCount,
                jintArray texCoordSetMapArray,
                jint texCoordMapLength, jintArray texUnitOffset,
		        jint numActiveTexUnit,
		        jintArray tunitstatemap,
				jint vertexAttrCount, jintArray vertexAttrSizes,
                jfloatArray varray, jfloatArray carray,
		        jint texUnitIndex, jint cDirty)
{
    jfloat *verts = NULL;

    if (varray != NULL) {
	verts = (jfloat *) env->GetPrimitiveArrayCritical( varray, NULL);
    }

    if (verts == NULL) {
	return;
    }

    executeGeometryArray(env, obj, ctx, geo, geo_type, 
			 isNonUniformScale,
			 false,
			 0, multiScreen, 
			 ignoreVertexColors, startVIndex, 
			 vcount, vformat, texCoordSetCount,
			 texCoordSetMapArray,
			 texCoordMapLength, texUnitOffset, 
			 numActiveTexUnit, tunitstatemap,
			 verts, carray, texUnitIndex, NULL, NULL, cDirty,useAlpha);
    
    env->ReleasePrimitiveArrayCritical( varray, verts, 0);

}


/* interleaved data with nio buffer as data format */
extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_GeometryArrayRetained_executeInterleavedBuffer(JNIEnv *env, 
		jobject obj, jlong ctx, jobject geo, jint geo_type, 
                jboolean isNonUniformScale, jboolean useAlpha,
		jboolean multiScreen,						
		jboolean ignoreVertexColors,
		jint startVIndex,
		jint vcount, jint vformat, 
		jint texCoordSetCount,
                jintArray texCoordSetMapArray, jint texCoordMapLength,
		jintArray texUnitOffset, 
		jint numActiveTexUnit,
		jintArray tunitstatemap,
		jobject varray, jfloatArray carray,
		jint texUnitIndex, jint cDirty) {
    
    jfloat *verts = NULL;
   
    /* get the direct buffer address */
    if (varray != NULL) {
	verts = (jfloat *) env->GetDirectBufferAddress(varray);
    }

    if (verts == NULL) {
	return;
    }

    /* call executeGeometryArray */
    executeGeometryArray(env, obj, ctx, geo, geo_type, 
			 isNonUniformScale,
			 false,
			 0, multiScreen, 
			 ignoreVertexColors, startVIndex, 
			 vcount, vformat, texCoordSetCount,
			 texCoordSetMapArray,
			 texCoordMapLength, texUnitOffset, 
			 numActiveTexUnit, tunitstatemap,
			 verts, carray, texUnitIndex, NULL, NULL, cDirty,useAlpha);
    
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_GeometryArrayRetained_freeD3DArray
    (JNIEnv *env, jobject geo, jboolean deleteVB)
{

    lockGeometry();

    jclass geoClass = (jclass) env->GetObjectClass(geo);
    // Free VertexBuffer associate with this GeometryArray
    jfieldID fieldID = env->GetFieldID(geoClass, "pVertexBuffers", "J");
    
    D3dVertexBufferVector *vbVector =
	reinterpret_cast<D3dVertexBufferVector*> (env->GetLongField(geo, fieldID));


    if (vbVector != NULL) {
	// clearLive() invoke this in Renderer thread
		for (ITER_LPD3DVERTEXBUFFER s = vbVector->begin(); 
	     s != vbVector->end(); ++s) {
	    // This notify vb that parent vector is already free
	    // so there is no need  to remove itself from vbVector
	    (*s)->vbVector = NULL;
	    (*s)->ctx->freeVB(*s);
	    }
	env->SetLongField(geo, fieldID, 0);
	vbVector->clear();
	delete vbVector;
    }

    unlockGeometry();
}



void executeIndexedGeometryArray(JNIEnv *env,
				 jobject obj, jlong ctx,
				 jobject geo, jint geo_type,
				 jboolean isNonUniformScale, 
				 jboolean modAlpha, // buildGA, should alpha be mode
				 jfloat alpha,
				 jboolean multiScreen,
				 jboolean ignoreVertexColors,
				 jint vcount,
				 jint vformat,
				 jint texCoordSetCount,
				 jintArray texCoordSetMapArray,
				 jint texCoordMapLength, 
				 jintArray texUnitOffset,
				 jint numActiveTexUnit,
				 jintArray tunitstatemap,
				 jfloat* verts, jfloatArray carray, jint pass,
				 jdoubleArray xform, jdoubleArray nxform,
				 jint cDirty,
				 jboolean useAlpha,
				 jint initialIndexIndex,
				 jint indexCount,
				 jintArray indexCoord) // Should alpha be sent down
{
    D3DDRAWPRIMITIVESTRIDEDDATA strideData;
    DWORD vertexFormat = 0;
    jint stride, coordoff, normoff, coloroff, texoff;
    int texStride, ts = 0;

    GetDevice();

    ZeroMemory(&strideData, sizeof(D3DDRAWPRIMITIVESTRIDEDDATA));

    strideData.modulateAlpha = modAlpha;
    strideData.alpha = alpha;

    /* This matches the code in GeometryArrayRetained.java */
    stride = coordoff = normoff = coloroff = texoff = 0;
    
    if ((vformat & GA_COORDINATES) != 0) {
        stride += 3;
        vertexFormat |= D3DFVF_XYZ;
    } else {
	// nothing worth doing if no coordinates define
	return;
    }

    if ((vformat & GA_NORMALS) != 0) {
        stride += 3;
        coordoff += 3;
        vertexFormat |= D3DFVF_NORMAL;
    }

    if ((vformat & GA_COLOR) != 0) {
	if ((vformat & GA_WITH_ALPHA) != 0 ) {
	    stride += 4;
	    normoff += 4;
	    coordoff += 4;
	} else { // Handle the case of executeInterleaved 3f
	    stride += 3;
	    normoff += 3;
	    coordoff += 3;
	}
	vertexFormat |= D3DFVF_DIFFUSE;
    }

    // In case of automatic texture generation
    ts = 2;
    
    if (vformat & GA_TEXTURE_COORDINATE) {
	if ((vformat & GA_TEXTURE_COORDINATE_2) != 0) {
	    texStride = texCoordSetCount << 1;
	} else if ((vformat & GA_TEXTURE_COORDINATE_3) != 0) {
	    ts = 3;
	    texStride = texCoordSetCount*3;
	} else { // GA_TEXTURE_COORDINATE_4
	    ts = 4;
	    texStride = texCoordSetCount << 2;
	}
        stride += texStride;
        normoff += texStride;
        coloroff += texStride;
        coordoff += texStride;
    }
    
    jfloat *cverts = NULL;

    if (carray != NULL) {
	// separate color array is used
	coloroff = 0;
    } else {
	coloroff += texoff;
    }

    // setup coordinates pointer
    strideData.fpositionPtr = &verts[coordoff];
    strideData.positionStride = stride;
    
    // setup color pointer
    if (((vformat & GA_COLOR) == 0) || ignoreVertexColors) {
	// Use Material color
	// Assume VertexBuffer will recreate when ignoreVertexColors
	// property changed. Otherwise we need to remove
	// the following one line
	vertexFormat &= ~D3DFVF_DIFFUSE;
    } else {
	if (carray == NULL) {
	    strideData.fdiffusePtr = &verts[coloroff];
	    strideData.diffuseStride = stride;
	    strideData.useAlpha = (vformat & GA_WITH_ALPHA);
	} else {
	    cverts = (jfloat*) env->GetPrimitiveArrayCritical(carray, NULL);
	    strideData.fdiffusePtr = &cverts[coloroff];
	    strideData.diffuseStride = 4;
	    strideData.useAlpha = true;	    
	}
    }


    // setup normal pointer
    if ((vformat & GA_NORMALS) != 0) {
        strideData.normalPtr = &verts[normoff];
        strideData.normalStride = stride;
    } 


    // setup texture pointer
    setTextureCoordPointers(env, d3dCtx, device,
			    &strideData,
			    pass, texoff, stride, ts,
			    (vformat & GA_TEXTURE_COORDINATE),
			    NULL,
			    texCoordMapLength, 
			    texUnitOffset,
			    numActiveTexUnit,
			    tunitstatemap,
			    &vertexFormat,
			    verts, NULL, NULL);

    // setup index pointer
    strideData.indexPtr =  (jint *) env->GetPrimitiveArrayCritical(indexCoord, NULL);
    strideData.initialIndexIndex = initialIndexIndex;


    jdouble* xform_ptr = NULL;
    jdouble* nxform_ptr = NULL;

    if (xform != NULL) {
        xform_ptr = (jdouble *) env->GetPrimitiveArrayCritical(xform, NULL);

    }

    if (nxform != NULL) {
        nxform_ptr = (jdouble *) env->GetPrimitiveArrayCritical(nxform, NULL);
    }

    renderIndexGeometry(env, d3dCtx, device, geo, geo_type, &strideData,
			vertexFormat, vcount, indexCount,
			xform_ptr, nxform_ptr, cDirty);



    if (xform_ptr != NULL) {
	env->ReleasePrimitiveArrayCritical(xform, xform_ptr, 0);
    }
    if (nxform_ptr != NULL) {
	env->ReleasePrimitiveArrayCritical(nxform, nxform_ptr, 0);
    }

    if (cverts != NULL) {
	env->ReleasePrimitiveArrayCritical(carray, cverts, NULL);	
    }

    env->ReleasePrimitiveArrayCritical(indexCoord,
				       strideData.indexPtr, NULL);
	
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_IndexedGeometryArrayRetained_executeIndexedGeometry(
                JNIEnv *env,
	        jobject obj, jlong ctx,
		jobject geo, jint geo_type,
                jboolean isNonUniformScale, jboolean useAlpha,
                jboolean multiScreen,
                jboolean ignoreVertexColors,
                jint initialIndexIndex,
		jint indexCount,
		jint vcount,
                jint vformat,
		jint vertexAttrCount, jintArray vertexAttrSizes,
		jint texCoordSetCount,
                jintArray texCoordSetMapArray,
                jint texCoordMapLength, jintArray texUnitOffset,
		jint numActiveTexUnit,
		jintArray tunitstatemap,
                jfloatArray varray, jfloatArray carray,
		jint texUnitIndex, jint cDirty,
		jintArray indexCoord)
{
    jfloat *verts = NULL;

    if (varray != NULL) {
	verts = (jfloat *) env->GetPrimitiveArrayCritical( varray, NULL);
    }

    if (verts == NULL) {
	return;
    }

    executeIndexedGeometryArray(env, obj, ctx, geo, geo_type, 
				isNonUniformScale,
				false, 0, multiScreen,
				ignoreVertexColors,
				vcount,
				vformat, texCoordSetCount,
				texCoordSetMapArray,
				texCoordMapLength, 
				texUnitOffset,
				numActiveTexUnit,
				tunitstatemap,
				verts, 
				carray,
				texUnitIndex,
				NULL, NULL,
				cDirty,
				useAlpha,
				initialIndexIndex,
				indexCount, 
				indexCoord);
    
    env->ReleasePrimitiveArrayCritical( varray, verts, 0);
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_IndexedGeometryArrayRetained_executeIndexedGeometryBuffer(
                JNIEnv *env,
	        jobject obj, jlong ctx,
		jobject geo, jint geo_type,
                jboolean isNonUniformScale, jboolean useAlpha,
                jboolean multiScreen,
                jboolean ignoreVertexColors,
                jint initialIndexIndex,
		jint indexCount,
		jint vcount,
                jint vformat, jint texCoordSetCount,
                jintArray texCoordSetMapArray,
                jint texCoordMapLength, jintArray texUnitOffset,
		jint numActiveTexUnit,
		jintArray tunitstatemap,
                jobject varray, jfloatArray carray,
		jint texUnitIndex, jint cDirty,
		jintArray indexCoord)
{
    jfloat *verts = NULL;
    
    /* get the direct buffer address */
    if (varray != NULL) {
	verts = (jfloat *) env->GetDirectBufferAddress(varray );
    }

    if (verts == NULL)
	return;

    executeIndexedGeometryArray(env, obj, ctx, geo, geo_type, 
				isNonUniformScale,
				false, 0, multiScreen,
				ignoreVertexColors,
				vcount,
				vformat, texCoordSetCount,
				texCoordSetMapArray,
				texCoordMapLength, 
				texUnitOffset,
				numActiveTexUnit,
				tunitstatemap,
				verts, 
				carray,
				texUnitIndex,
				NULL, NULL,
				cDirty,
				useAlpha,
				initialIndexIndex,
				indexCount, 
				indexCoord);
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_IndexedGeometryArrayRetained_buildIndexedGeometry(
                JNIEnv *env, 
		jobject obj, jlong ctx, jobject geo, 
		jint geo_type, 
                jboolean isNonUniformScale, jboolean updateAlpha, float alpha,
		jboolean ignoreVertexColors,
		jint initialIndexIndex,
  	        jint indexCount,
	        jint vertexCount,
	        jint vformat, 
		jint vertexAttrCount,
		jintArray vertexAttrSizes,
		jint texCoordSetCount,
		jintArray texCoordSetMapArray,
		jint texCoordMapLength,
		jintArray texUnitOffset, 
		jdoubleArray xform, jdoubleArray nxform,
		jfloatArray varray, jintArray indexCoord)
{

    jfloat *verts = NULL;

    if (varray != NULL) {
	verts = (jfloat *) env->GetPrimitiveArrayCritical( varray, NULL);
    }

    if (verts == NULL) {
	return;
    }

    if ((vformat & GA_COLOR) != 0) {
	// alpha component is added for buildGA
	vformat |= GA_WITH_ALPHA;
    }

    executeIndexedGeometryArray(env, obj, ctx, geo, geo_type, 
				isNonUniformScale,
				updateAlpha, alpha,
				false,
				ignoreVertexColors,
				vertexCount,
				vformat, 
				texCoordSetCount,
				texCoordSetMapArray,
				texCoordMapLength, 
				texUnitOffset,
				texCoordMapLength, 
				NULL,
				verts, 
				NULL, -1,
				xform, nxform,
				javax_media_j3d_GeometryArrayRetained_VERTEX_CHANGED|
				javax_media_j3d_GeometryArrayRetained_INDEX_CHANGED,
				false,
				initialIndexIndex,
				indexCount, 
				indexCoord);
    
    env->ReleasePrimitiveArrayCritical( varray, verts, 0);

}



void executeIndexedGeometryArrayVA(
    JNIEnv *env, 
    jobject obj,
    jlong ctx,    
    jobject geo,
    jint geo_type, 
    jboolean isNonUniformScale,
    jboolean multiScreen,
    jboolean ignoreVertexColors,
    jint initialIndexIndex,
    jint indexCount,
    jint vertexCount,
    jint vformat,
    jint vdefined,
    jfloat* fverts,
    jdouble* dverts,
    jfloat* fclrs,
    jbyte*  bclrs,
    jfloat* norms,
    jint pass,  
    jint texCoordMapLength,
    jintArray 	tcoordsetmap,
    jint numActiveTexUnit,
    jintArray tunitstatemap,
    jint texStride,
    jfloat** texCoordPointer,
    jint cDirty,
    jintArray indexCoord)
{
    D3DDRAWPRIMITIVESTRIDEDDATA strideData;
    DWORD vertexFormat = 0;
    jboolean floatCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_FLOAT) != 0);
    jboolean doubleCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_DOUBLE) != 0);
    jboolean floatColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_FLOAT) != 0);
    jboolean byteColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_BYTE) != 0);
    jboolean normalsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_NORMAL_FLOAT) != 0);
    jboolean textureDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_TEXCOORD_FLOAT) != 0);

    GetDevice();    

    ZeroMemory(&strideData, sizeof(D3DDRAWPRIMITIVESTRIDEDDATA));

    // setup coordinate pointers
    if (floatCoordDefined || doubleCoordDefined) {
	vertexFormat |= D3DFVF_XYZ;

	if (floatCoordDefined ) {
	    strideData.fpositionPtr = &fverts[0];
	} else if (doubleCoordDefined) {
	    strideData.dpositionPtr = &dverts[0];
	} 
	strideData.positionStride = 3;
	
    } else {
	// nothing worth doing if no coordinates define
	return;
    }

    // setup normal pointers
    if (normalsDefined) {
        vertexFormat |= D3DFVF_NORMAL;
        strideData.normalPtr = &norms[0];
        strideData.normalStride = 3;
    }
    
    // setup color pointers
    if (!(floatColorsDefined || byteColorsDefined)
	|| ignoreVertexColors) {
	// Use Material color
	// Assume VertexBuffer will recreate when ignoreVertexColors
	// property changed. Otherwise we need to remove
	// the following one line
	vertexFormat &= ~D3DFVF_DIFFUSE;
    } else {
	if ((vformat & GA_WITH_ALPHA) != 0) {
	    strideData.diffuseStride = 4;
	    strideData.useAlpha = true;
	} else {
	    strideData.diffuseStride = 3;
	    strideData.useAlpha = false;
	}
	if (floatColorsDefined) {
	    strideData.fdiffusePtr = &fclrs[0];
	} else {
	    strideData.bdiffusePtr = &bclrs[0];
	} 

	vertexFormat |= D3DFVF_DIFFUSE;
    } 
    

    int ts = 2; // In case of automatic texture generation
	
    if ((vformat & GA_TEXTURE_COORDINATE_3) != 0) {
	ts = 3;
    } else if ((vformat & GA_TEXTURE_COORDINATE_4) != 0) {
	ts = 4;
    }

    // setup texture pointer
    setTextureCoordPointers(env, d3dCtx, device,
			    &strideData,
			    pass, 0, 
			    texStride, ts,
			    textureDefined,
			    tcoordsetmap,
			    texCoordMapLength, 
			    NULL,
			    numActiveTexUnit,
			    tunitstatemap,
			    &vertexFormat,
			    NULL, texCoordPointer, 
			    NULL);

    // setup index pointer
    strideData.indexPtr =  (jint *) env->GetPrimitiveArrayCritical(indexCoord, NULL);
    strideData.initialIndexIndex = initialIndexIndex;

    // Construct/update VertexBuffer, render() if not in display list mode 
    renderIndexGeometry(env, d3dCtx, device, geo, geo_type, &strideData,
			vertexFormat, vertexCount,  indexCount,
			NULL, NULL, cDirty); 

    env->ReleasePrimitiveArrayCritical(indexCoord,
				       strideData.indexPtr, NULL);


}
extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_IndexedGeometryArrayRetained_executeIndexedGeometryVA(
    JNIEnv *env, 
    jobject obj,
    jlong ctx,    
    jobject geo,
    jint geo_type, 
    jboolean isNonUniformScale,
    jboolean multiScreen,
    jboolean ignoreVertexColors,
    jint initialIndexIndex,
    jint indexCount,
    jint vertexCount,
    jint vformat,
    jint vdefined,
    jfloatArray vfcoords,
    jdoubleArray vdcoords,
    jfloatArray cfdata,
    jbyteArray  cbdata,
    jfloatArray ndata,
    jint vertexAttrCount,
    jintArray vertexAttrSizes,
    jobjectArray vertexAttrData,
    jint pass,  
    jint texCoordMapLength,
    jintArray 	tcoordsetmap,
    jint numActiveTexUnit,
    jintArray tunitstatemap,
    jint texStride,
    jobjectArray texCoords,
    jint cDirty,
    jintArray indexCoord)
{
    jfloat *fverts = NULL;
    jdouble *dverts = NULL;
    jfloat *fclrs = NULL;
    jbyte *bclrs = NULL;    
    jfloat *norms = NULL;
    jfloat* texCoordPointer[D3DDP_MAXTEXCOORD];
    jarray texobjs[D3DDP_MAXTEXCOORD];
    int i;
    
    jboolean floatCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_FLOAT) != 0);
    jboolean doubleCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_DOUBLE) != 0);
    jboolean floatColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_FLOAT) != 0);
    jboolean byteColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_BYTE) != 0);
    jboolean normalsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_NORMAL_FLOAT) != 0);
    jboolean textureDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_TEXCOORD_FLOAT) != 0);

    /* get texture arrays */
    if (textureDefined) {
	for (i = 0; i < texCoordMapLength; i++) {
	    texobjs[i] = (jarray)env->GetObjectArrayElement( texCoords, i);
	}
    }

    /* get coordinate array */
    if (floatCoordDefined) {
	fverts= (jfloat *) env->GetPrimitiveArrayCritical( vfcoords, NULL);
    } else if (doubleCoordDefined) {
	dverts= (jdouble *) env->GetPrimitiveArrayCritical( vdcoords, NULL);
    }

    if ((fverts == NULL) && (dverts == NULL)) {
	return; 
    }

    /* get color array */
    if (floatColorsDefined) {
	fclrs = (jfloat *) env->GetPrimitiveArrayCritical( cfdata, NULL);
    } else if (byteColorsDefined) {
	bclrs = (jbyte *)env->GetPrimitiveArrayCritical( cbdata, NULL);
    }

    /* get normal array */
    if (normalsDefined) {
	norms = (jfloat *) env->GetPrimitiveArrayCritical(ndata, NULL);
    }

    /* get texture arrays */
    if (textureDefined) {
	for (i = 0; i < texCoordMapLength; i++) {
	    if (texobjs[i] != NULL) {
		texCoordPointer[i] =
		    (jfloat*)env->GetPrimitiveArrayCritical(texobjs[i], NULL);
	    } else {
		texCoordPointer[i] = NULL;	
	    }
	    
	}	
    }

    
    executeIndexedGeometryArrayVA(env, 
				  obj,
				  ctx,    
				  geo,
				  geo_type, 
				  isNonUniformScale,
				  multiScreen,
				  ignoreVertexColors,
				  initialIndexIndex,
				  indexCount,
				  vertexCount,
				  vformat,
				  vdefined,
				  fverts,
				  dverts,
				  fclrs,
				  bclrs,
				  norms,
				  pass,  
				  texCoordMapLength,
				  tcoordsetmap,
				  numActiveTexUnit,
				  tunitstatemap,
				  texStride,
				  texCoordPointer,
				  cDirty,
				  indexCoord);
    
    // Free memory
    if (textureDefined) {
	for (i = 0; i < texCoordMapLength; i++) {
	    if (texCoordPointer[i] != NULL) {
		env->ReleasePrimitiveArrayCritical(texobjs[i], texCoordPointer[i], NULL);
	    }
	}
    }

    if (floatColorsDefined) {
	env->ReleasePrimitiveArrayCritical(cfdata, fclrs, 0); 
    } else if (byteColorsDefined) {
	env->ReleasePrimitiveArrayCritical(cbdata, bclrs, 0);
    }

    if (normalsDefined) {
	env->ReleasePrimitiveArrayCritical(ndata, norms, 0);
    }

    if (floatCoordDefined) {
	env->ReleasePrimitiveArrayCritical(vfcoords, fverts, 0); 
    } else if (doubleCoordDefined) {
	env->ReleasePrimitiveArrayCritical(vdcoords, dverts, 0); 
    }
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_IndexedGeometryArrayRetained_executeIndexedGeometryVABuffer(
    JNIEnv *env, 
    jobject obj,
    jlong ctx,    
    jobject geo,
    jint geo_type, 
    jboolean isNonUniformScale,
    jboolean multiScreen,
    jboolean ignoreVertexColors,
    jint initialIndexIndex,
    jint indexCount,
    jint vertexCount,
    jint vformat,
    jint vdefined,
    jobject vcoords,
    jobject cdataBuffer,
    jfloatArray cfdata,
    jbyteArray  cbdata,
    jobject ndata,
    jint vertexAttrCount,
    jintArray vertexAttrSizes,
    jobjectArray vertexAttrData,
    jint pass,  
    jint texCoordMapLength,
    jintArray 	tcoordsetmap,
    jint numActiveTexUnit,
    jintArray tunitstatemap,
    jint texStride,
    jobjectArray texCoords,
    jint cDirty,
    jintArray indexCoord)
{
    jfloat *fverts = NULL;
    jdouble *dverts = NULL;
    jfloat *fclrs = NULL;
    jbyte *bclrs = NULL;    
    jfloat *norms = NULL;
    jfloat* texCoordPointer[D3DDP_MAXTEXCOORD];
    jarray texobjs[D3DDP_MAXTEXCOORD];
    int i;
    
    jboolean floatCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_FLOAT) != 0);
    jboolean doubleCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_DOUBLE) != 0);
    jboolean floatColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_FLOAT) != 0);
    jboolean byteColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_BYTE) != 0);
    jboolean normalsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_NORMAL_FLOAT) != 0);
    jboolean textureDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_TEXCOORD_FLOAT) != 0);

    /* get texture arrays */
    if (textureDefined) {
	for (i = 0; i < texCoordMapLength; i++) {
	    texobjs[i] = (jarray)env->GetObjectArrayElement( texCoords, i);
	}
    }

    /* get coordinate array */
    if (floatCoordDefined) {
	fverts = (jfloat *)env->GetDirectBufferAddress( vcoords );
    } else if (doubleCoordDefined) {
	dverts = (jdouble *)env->GetDirectBufferAddress( vcoords ); 
    }

    if ((fverts == NULL) && (dverts == NULL)) {
	return; 
    }

    /* get color array */
    if (floatColorsDefined) {
	if(cfdata != NULL) {
	    fclrs = (jfloat *) env->GetPrimitiveArrayCritical( cfdata, NULL);
	} else {
	    fclrs = (jfloat *) env->GetDirectBufferAddress (cdataBuffer);
	}
    } else if (byteColorsDefined) {
	if (cbdata != NULL) {
	    bclrs = (jbyte *) env->GetPrimitiveArrayCritical( cbdata, NULL);
	} else {
	    bclrs = (jbyte *) env->GetDirectBufferAddress(cdataBuffer);
	}
    }

    /* get normal array */
    if (normalsDefined) {
	norms = (jfloat *)env->GetDirectBufferAddress(ndata);
    }
    /* get texture arrays */
    if (textureDefined) {
	for (i = 0; i < texCoordMapLength; i++) {
	    if (texobjs[i] != NULL)
		texCoordPointer[i] = (jfloat*)env->GetDirectBufferAddress(texobjs[i]);
	    else
		texCoordPointer[i] = NULL;	
	    
	}	
    }

    
    executeIndexedGeometryArrayVA(
				  env, 
				  obj,
				  ctx,    
				  geo,
				  geo_type, 
				  isNonUniformScale,
				  multiScreen,
				  ignoreVertexColors,
				  initialIndexIndex,
				  indexCount,
				  vertexCount,
				  vformat,
				  vdefined,
				  fverts,
				  dverts,
				  fclrs,
				  bclrs,
				  norms,
				  pass,  
				  texCoordMapLength,
				  tcoordsetmap,
				  numActiveTexUnit,
				  tunitstatemap,
				  texStride,
				  texCoordPointer,
				  cDirty,
				  indexCoord);
    
    if (floatColorsDefined && cfdata != NULL) {
	env->ReleasePrimitiveArrayCritical( cfdata, fclrs, 0);
    } else if (byteColorsDefined && cbdata != NULL) {
	env->ReleasePrimitiveArrayCritical(cbdata, bclrs, 0);
    }
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_GeometryArrayRetained_buildGAForBuffer(
    JNIEnv *env, 
    jobject obj,
    jlong ctx,    
    jobject geo,
    jint geo_type, 
    jboolean isNonUniformScale,
    jboolean updateAlpha,
    jfloat alpha,
    jboolean ignoreVertexColors,
    jint vcount,
    jint vformat,
    jint vdefined,
    jint initialCoordIndex,
    jobject vcoords,
    jint initialColorIndex,
    jobject cdataBuffer,
    jint initialNormalIndex,
    jobject ndata,
    jint texCoordMapLength,
    jintArray 	tcoordsetmap,
    jintArray texindices,
    jint texStride,
    jobjectArray texCoords,
    jdoubleArray xform,
    jdoubleArray nxform)
{
    jfloat *fverts = NULL;
    jdouble *dverts = NULL ;
    jbyte *bclrs = NULL;
    jfloat *fclrs = NULL, *norms = NULL;
    jfloat* texCoordPointer[D3DDP_MAXTEXCOORD];
    jarray texobjs[D3DDP_MAXTEXCOORD];
    int i;
    
    jboolean floatCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_FLOAT) != 0);
    jboolean doubleCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_DOUBLE) != 0);
    jboolean floatColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_FLOAT) != 0);
    jboolean byteColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_BYTE) != 0);
    jboolean normalsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_NORMAL_FLOAT) != 0);
    jboolean textureDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_TEXCOORD_FLOAT) != 0);

    if (textureDefined) {
	for (i = 0; i < texCoordMapLength; i++) {
	    texobjs[i] = (jarray)env->GetObjectArrayElement(texCoords, i);
	}
    }

    /* get coordinate array */
    if (floatCoordDefined) {
	fverts = (jfloat *)env->GetDirectBufferAddress( vcoords );
    } else if (doubleCoordDefined) {
	dverts = (jdouble *)env->GetDirectBufferAddress( vcoords ); 
    }

    if ((fverts == NULL) && (dverts == NULL)) {
	return; 
    }

    /* get color array */
    if (floatColorsDefined) {
	fclrs = (jfloat *) env->GetDirectBufferAddress(cdataBuffer);
    } else if (byteColorsDefined) {
	bclrs = (jbyte *) env->GetDirectBufferAddress(cdataBuffer);
    }

    /* get normal array */
    if (normalsDefined) {
	norms = (jfloat *)env->GetDirectBufferAddress(ndata);
    }

    /* get texture arrays */
    if (textureDefined) {
	for (i = 0; i < texCoordMapLength; i++) {
	    if (texobjs[i] != NULL)
		texCoordPointer[i] = (jfloat*)env->GetDirectBufferAddress(texobjs[i]);
	    else
		texCoordPointer[i] = NULL;	
	    
	}	
    }

    executeGeometryArrayVA(env, obj, ctx, geo, geo_type,
			   isNonUniformScale, updateAlpha, alpha,
			   false, ignoreVertexColors,
			   vcount, vformat,  vdefined, initialCoordIndex,
			   fverts, dverts, initialColorIndex,
			   fclrs, bclrs, initialNormalIndex,
			   norms, -1, texCoordMapLength,
			   tcoordsetmap,texCoordMapLength, NULL,
			   texindices,texStride,texCoordPointer,
			   xform, nxform,
			   javax_media_j3d_GeometryArrayRetained_VERTEX_CHANGED);
}

/* execute geometry array with java array format */
/*
 * Class:     javax_media_j3d_GeometryArrayRetained
 * Method:    buildGAForByRef
 * Signature: (JLjavax/media/j3d/GeometryArrayRetained;IZZFZIIII[F[DI[F[BI[FI[I[I[[FI[I[II[Ljava/lang/Object;[D[D)V
 */

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_GeometryArrayRetained_buildGAForByRef(
    JNIEnv *env, 
    jobject obj,
    jlong ctx,    
    jobject geo,
    jint geo_type, 
    jboolean isNonUniformScale,
    jboolean updateAlpha,
    jfloat alpha,
    jboolean ignoreVertexColors,
    jint vcount,
    jint vformat,
    jint vdefined,
    jint initialCoordIndex,
    jfloatArray vfcoords,
    jdoubleArray vdcoords,
    jint initialColorIndex,
    jfloatArray cfdata,
    jbyteArray  cbdata,
    jint initialNormalIndex,
    jfloatArray ndata,
	jint vertexAttrCount,
    jintArray vertexAttrSizes,
    jintArray vertexAttrIndices,
    jobjectArray vertexAttrData,
	jint texCoordMapLength,
    jintArray 	tcoordsetmap,
    jintArray texindices,
    jint texStride,
    jobjectArray texCoords,
    jdoubleArray xform,
    jdoubleArray nxform)
{
    jfloat *fverts = NULL;
    jdouble *dverts = NULL;
    jfloat *fclrs = NULL;
    jbyte *bclrs = NULL;    
    jfloat *norms = NULL;
    jfloat* texCoordPointer[D3DDP_MAXTEXCOORD];
    jarray texobjs[D3DDP_MAXTEXCOORD];
    int i;
    
    jboolean floatCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_FLOAT) != 0);
    jboolean doubleCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_DOUBLE) != 0);
    jboolean floatColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_FLOAT) != 0);
    jboolean byteColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_BYTE) != 0);
    jboolean normalsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_NORMAL_FLOAT) != 0);
    jboolean textureDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_TEXCOORD_FLOAT) != 0);


    if (textureDefined) {
	for (i = 0; i < texCoordMapLength; i++) {
	    texobjs[i] = (jarray)env->GetObjectArrayElement( texCoords, i);
	}
    }

    /* get coordinate array */
    if (floatCoordDefined) {
	fverts= (jfloat *) env->GetPrimitiveArrayCritical( vfcoords, NULL);
    } else if (doubleCoordDefined) {
	dverts= (jdouble *) env->GetPrimitiveArrayCritical( vdcoords, NULL);
    }

    if ((fverts == NULL) && (dverts == NULL)) {
	return; 
    }

    /* get color array */
    if (floatColorsDefined) {
	fclrs = (jfloat *) env->GetPrimitiveArrayCritical( cfdata, NULL);
    } else if (byteColorsDefined) {
	bclrs = (jbyte *)env->GetPrimitiveArrayCritical( cbdata, NULL);
    }

    /* get normal array */
    if (normalsDefined) {
	norms = (jfloat *) env->GetPrimitiveArrayCritical(ndata, NULL);
    }

    /* get texture arrays */
    if (textureDefined) {
	for (i = 0; i < texCoordMapLength; i++) {
	    if (texobjs[i] != NULL) {
		texCoordPointer[i] = (jfloat*)env->GetPrimitiveArrayCritical(texobjs[i], NULL);
	    } else {
		texCoordPointer[i] = NULL;	
	    }
	    
	}	
    }


    executeGeometryArrayVA(env, obj, ctx, geo, geo_type,
			   isNonUniformScale,  updateAlpha, alpha,
			   false, ignoreVertexColors,
			   vcount, vformat,  vdefined, initialCoordIndex,
			   fverts, dverts, initialColorIndex,
			   fclrs, bclrs, initialNormalIndex,
			   norms, -1, texCoordMapLength,
			   tcoordsetmap,texCoordMapLength, NULL,
			   texindices,texStride,texCoordPointer,
			   xform, nxform,
			   javax_media_j3d_GeometryArrayRetained_VERTEX_CHANGED);
    
    if (floatCoordDefined) {
	env->ReleasePrimitiveArrayCritical( vfcoords, fverts, 0); 
    }
    else if (doubleCoordDefined) {
	env->ReleasePrimitiveArrayCritical( vdcoords, dverts, 0); 
    }

    if (floatColorsDefined) {
	env->ReleasePrimitiveArrayCritical( cfdata, fclrs, 0); 
    }
    else if (byteColorsDefined) {
	env->ReleasePrimitiveArrayCritical( cbdata, bclrs, 0);
    }

    if (normalsDefined) {
	env->ReleasePrimitiveArrayCritical( ndata, norms, 0);
    }
    
    if (textureDefined) {
	for (i = 0; i < texCoordMapLength; i++) {
	    if (texCoordPointer[i] != NULL) {
		env->ReleasePrimitiveArrayCritical(texobjs[i], texCoordPointer[i], 0);
	    }
	}
    }    
}
