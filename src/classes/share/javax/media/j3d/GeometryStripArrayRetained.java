/*
 * $RCSfile$
 *
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

package javax.media.j3d;

import java.util.ArrayList;

import javax.vecmath.TexCoord2f;
import javax.vecmath.TexCoord3f;

import com.sun.j3d.internal.FloatBufferWrapper;


/**
 * The GeometryStripArray object is an abstract class that is extended for
 * a set of GeometryArray strip primitives.  These include LINE_STRIP,
 * TRIANGLE_STRIP, and TRIANGLE_FAN.
 */

abstract class GeometryStripArrayRetained extends GeometryArrayRetained {

    // Array of per-strip vertex counts
    int stripVertexCounts[];

    // Array of per-strip starting index
    int stripStartVertexIndices[];  // start of vertices for both by-copy
                                    // and by-ref
    int stripStartOffsetIndices[]; // Used in byRef non_interleaved

    // Following variables are only used in the compile mode
    // isCompiled = true
    int[] compileNumStrips;
    int[] compileStripCountOffset;


    /**
     * Set stripVertexCount data into local array
     */
    void setStripVertexCounts(int stripVertexCounts[]) {
	boolean nullGeo = false;

	int i, num = stripVertexCounts.length, total = 0;
	for (i=0; i < num; i++) {
	    total += stripVertexCounts[i];
	    if (this instanceof LineStripArrayRetained) {
		if (stripVertexCounts[i] < 2) {
		    throw new IllegalArgumentException(J3dI18N.getString("LineStripArrayRetained1"));
		}
	    }
	    else if (this instanceof TriangleStripArrayRetained) {
		if (stripVertexCounts[i] < 3) {
		    throw new IllegalArgumentException(J3dI18N.getString("TriangleStripArrayRetained1"));
		}
	    }
	    else if (this instanceof TriangleFanArrayRetained) {
		if (stripVertexCounts[i] < 3) {
		    throw new IllegalArgumentException(J3dI18N.getString("TriangleFanArrayRetained1"));
		}
	    }
	}

	if ((initialVertexIndex + total) > vertexCount) {
	    throw new IllegalArgumentException(J3dI18N.getString("GeometryStripArray3"));
	}
	if ((initialCoordIndex + total) > vertexCount) {
	    throw new IllegalArgumentException(J3dI18N.getString("GeometryStripArray7"));
	}
	if ((initialColorIndex + total) > vertexCount) {
	    throw new IllegalArgumentException(J3dI18N.getString("GeometryStripArray4"));
	}
	if ((initialNormalIndex + total) > vertexCount) {
	    throw new IllegalArgumentException(J3dI18N.getString("GeometryStripArray5"));
	}
        if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
            if ((vertexFormat & (GeometryArray.BY_REFERENCE|vertexFormat &GeometryArray.INTERLEAVED)) == GeometryArray.BY_REFERENCE) {
                for (i = 0; i < texCoordSetCount; i++) {
                    if ((initialTexCoordIndex[i] + total) > vertexCount) {
                        throw new IllegalArgumentException(
                                J3dI18N.getString("GeometryStripArray6"));
                    }
                }
            }
        }
        if ((vertexFormat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
            if ((vertexFormat & (GeometryArray.BY_REFERENCE|vertexFormat &GeometryArray.INTERLEAVED)) == GeometryArray.BY_REFERENCE) {
                for (i = 0; i < vertexAttrCount; i++) {
                    if ((initialVertexAttrIndex[i] + total) > vertexCount) {
                        throw new IllegalArgumentException(
                                J3dI18N.getString("GeometryStripArray8"));
                    }
                }
            }
        }
        boolean isLive = source!=null && source.isLive();
        if(isLive){
            geomLock.getLock();
        }
	dirtyFlag |= STRIPCOUNT_CHANGED;
	validVertexCount = total;
	this.stripVertexCounts = new int[num];
	stripStartVertexIndices = new int[num];
	stripStartOffsetIndices = new int[num];
	stripStartOffsetIndices[0] = 0;
	if ((vertexFormat & (GeometryArray.BY_REFERENCE|vertexFormat &GeometryArray.INTERLEAVED)) == GeometryArray.BY_REFERENCE) {
	    stripStartVertexIndices[0] = initialCoordIndex;
	    nullGeo = ((vertexType & GeometryArrayRetained.VERTEX_DEFINED) == 0);
	}
	else {
	    stripStartVertexIndices[0] = initialVertexIndex;
	    if ((vertexFormat & GeometryArray.INTERLEAVED) != 0) {
		if (( vertexFormat & GeometryArray.USE_NIO_BUFFER) != 0) {
		    nullGeo = (interLeavedVertexData == null);
		}
		else {
		    nullGeo = (interleavedFloatBufferImpl == null);
		}
	    }
	}

	for (i=0; i<num-1; i++) {
	    this.stripVertexCounts[i] = stripVertexCounts[i];
	    stripStartVertexIndices[i+1] = stripStartVertexIndices[i] +
		stripVertexCounts[i];
	    stripStartOffsetIndices[i+1] = stripStartOffsetIndices[i]+stripVertexCounts[i];
	}
	this.stripVertexCounts[num-1] = stripVertexCounts[num-1];
        if(isLive) {
            geomLock.unLock();
        }
	if (!inUpdater && isLive) {
	    processCoordsChanged(nullGeo);
	    sendDataChangedMessage(true);
	}

    }
    void unIndexify(IndexedGeometryStripArrayRetained src) {
	if ((src.vertexFormat & GeometryArray.USE_NIO_BUFFER) == 0) {
	    unIndexifyJavaArray(src);
	}
	else {
	    unIndexifyNIOBuffer(src);
	}
    }

    private void unIndexifyJavaArray(IndexedGeometryStripArrayRetained src) {
        int vOffset = 0, srcOffset, tOffset = 0;
        int base = src.initialIndexIndex;
	int i,j, k, index, colorStride = 0;
	float[] vdata = null;

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

	    for (i=0; i < src.stripIndexCounts.length; i++) {
		for (j=0; j < src.stripIndexCounts[i]; j++) {
		    index = j + base;
		    if ((vertexFormat & GeometryArray.NORMALS) != 0){
			System.arraycopy(vdata,
			  	src.indexNormal[index]*src.stride + src.normalOffset,
				vertexData, vOffset + normalOffset, 3);
		    }

                    if (colorStride == 4) {
			/*
			System.err.println("vdata.length = "+vdata.length);
			System.err.println("vertexData.length = "+vertexData.length);
			System.err.println("src.stride = "+src.stride);
			System.err.println("src.colorOffset = "+src.colorOffset);
			System.err.println("index = "+index+" src.indexColor.length = "+src.indexColor.length);
			System.err.println("src.indexColor[index] = "+src.indexColor[index]);
			System.err.println("base = "+base);
			*/
			System.arraycopy(vdata,
				src.indexColor[index]*src.stride + src.colorOffset,
				vertexData, vOffset + colorOffset, colorStride);
		    } else if (colorStride == 3) {
			System.arraycopy(vdata,
				src.indexColor[index]*src.stride + src.colorOffset,
				vertexData, vOffset + colorOffset, colorStride);
			vertexData[vOffset + colorOffset + 3] = 1.0f;
		    }

                    if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
			for (k = 0; k < texCoordSetCount; k++) {
                             System.arraycopy(vdata,
                            	(src.indexTexCoord[k][index])
					* src.stride + src.textureOffset +
					src.texCoordSetMapOffset[k],
                            	vertexData,
                            	vOffset + textureOffset +
					texCoordSetMapOffset[k],
                            	texCoordStride);
                        }
		    }

                    if ((vertexFormat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
			for (k = 0; k < vertexAttrCount; k++) {
                             System.arraycopy(vdata,
                            	src.indexVertexAttr[k][index] * src.stride + src.vertexAttrOffsets[k],
                            	vertexData,
                            	vOffset + vertexAttrOffsets[k],
                            	vertexAttrSizes[k]);
                        }
		    }

		    if ((vertexFormat & GeometryArray.COORDINATES) != 0) {
			System.arraycopy(vdata,
					 src.indexCoord[index]*src.stride + src.coordinateOffset,
					 vertexData, vOffset + coordinateOffset, 3);
		    }
		    vOffset += stride;
		}
		base += src.stripIndexCounts[i];
	    }
	}
	else {
	    if ((vertexFormat & GeometryArray.NORMALS) != 0){
		base = src.initialIndexIndex;
		vOffset = normalOffset;
		switch ((src.vertexType & NORMAL_DEFINED)) {
		case NF:
		    for (i=0; i < src.stripIndexCounts.length; i++) {
			for (j=0; j < src.stripIndexCounts[i]; j++) {
			    index = j+base;
			    System.arraycopy(src.floatRefNormals,
					     src.indexNormal[index]*3,
					     vertexData,
					     vOffset, 3);
			    vOffset += stride;
			}
			base += src.stripIndexCounts[i];
		    }
		    break;
		case N3F:
		    for (i=0; i < src.stripIndexCounts.length; i++) {
			for (j=0; j < src.stripIndexCounts[i]; j++) {
			    index = src.indexNormal[j+base];
			    vertexData[vOffset] = src.v3fRefNormals[index].x;
			    vertexData[vOffset+1] = src.v3fRefNormals[index].y;
			    vertexData[vOffset+2] = src.v3fRefNormals[index].z;
			    vOffset += stride;
			}
			base += src.stripIndexCounts[i];
		    }
		    break;
		default:
		    break;
		}
	    }

            if ((vertexFormat & GeometryArray.COLOR) != 0){
		base = src.initialIndexIndex;
		vOffset = colorOffset;
		int multiplier = 3;
		if ((src.vertexFormat & GeometryArray.WITH_ALPHA) != 0)
		    multiplier = 4;

		switch ((src.vertexType & COLOR_DEFINED)) {
		case CF:
		    for (i=0; i < src.stripIndexCounts.length; i++) {
			for (j=0; j < src.stripIndexCounts[i]; j++) {
			    index = j+base;

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
			base += src.stripIndexCounts[i];
		    }
		    break;
		case CUB:
		    for (i=0; i < src.stripIndexCounts.length; i++) {
			for (j=0; j < src.stripIndexCounts[i]; j++) {
			    index = src.indexColor[j+base] * multiplier;
			    vertexData[vOffset] = (src.byteRefColors[index] & 0xff) * ByteToFloatScale;
			    vertexData[vOffset+1] = (src.byteRefColors[index+1] & 0xff) * ByteToFloatScale;;
			    vertexData[vOffset+2] = (src.byteRefColors[index+2] & 0xff) * ByteToFloatScale;;
			    if ((src.vertexFormat & GeometryArray.WITH_ALPHA) != 0) {
				vertexData[vOffset+3] = (src.byteRefColors[index+3] & 0xff) * ByteToFloatScale;
			    }
			    else {
				vertexData[vOffset+3] = 1.0f;
			    }
			    vOffset += stride;
			}
			base += src.stripIndexCounts[i];
		    }
		    break;
		case C3F:
		    for (i=0; i < src.stripIndexCounts.length; i++) {
			for (j=0; j < src.stripIndexCounts[i]; j++) {
			    index = src.indexColor[j+base];
			    vertexData[vOffset] = src.c3fRefColors[index].x;
			    vertexData[vOffset+1] = src.c3fRefColors[index].y;
			    vertexData[vOffset+2] = src.c3fRefColors[index].z;
			    vertexData[vOffset+3] = 1.0f;
			    vOffset += stride;
			}
			base += src.stripIndexCounts[i];
		    }
		    break;
		case C4F:
		    for (i=0; i < src.stripIndexCounts.length; i++) {
			for (j=0; j < src.stripIndexCounts[i]; j++) {
			    index = src.indexColor[j+base];
			    vertexData[vOffset] = src.c4fRefColors[index].x;
			    vertexData[vOffset+1] = src.c4fRefColors[index].y;
			    vertexData[vOffset+2] = src.c4fRefColors[index].z;
			    vertexData[vOffset+3] = src.c4fRefColors[index].w;
			    vOffset += stride;
			}
			base += src.stripIndexCounts[i];
		    }
		    break;
		case C3UB:
		    for (i=0; i < src.stripIndexCounts.length; i++) {
			for (j=0; j < src.stripIndexCounts[i]; j++) {
			    index = src.indexColor[j+base];
			    vertexData[vOffset] = (src.c3bRefColors[index].x & 0xff) * ByteToFloatScale;
			    vertexData[vOffset+1] = (src.c3bRefColors[index].y & 0xff) * ByteToFloatScale;
			    vertexData[vOffset+2] = (src.c3bRefColors[index].z & 0xff) * ByteToFloatScale;
			    vertexData[vOffset+3] = 1.0f;

			    vOffset += stride;
			}
			base += src.stripIndexCounts[i];
		    }
		    break;
		case C4UB:
		    for (i=0; i < src.stripIndexCounts.length; i++) {
			for (j=0; j < src.stripIndexCounts[i]; j++) {
			    index = src.indexColor[j+base];
			    vertexData[vOffset] = (src.c4bRefColors[index].x & 0xff) * ByteToFloatScale;
			    vertexData[vOffset+1] = (src.c4bRefColors[index].y & 0xff) * ByteToFloatScale;
			    vertexData[vOffset+2] = (src.c4bRefColors[index].z & 0xff) * ByteToFloatScale;
			    vertexData[vOffset+3] = (src.c4bRefColors[index].w & 0xff) * ByteToFloatScale;
			    vOffset += stride;
			}
			base += src.stripIndexCounts[i];
		    }
		    break;
		default:
		    break;
		}
	    }

            if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
		base = src.initialIndexIndex;
		vOffset = textureOffset;
		switch ((src.vertexType & TEXCOORD_DEFINED)) {
		case TF:
		    for (i=0; i < src.stripIndexCounts.length; i++) {
			for (j=0; j < src.stripIndexCounts[i]; j++) {
			    index = j+base;

			    for (k = 0, tOffset = vOffset;
					k < texCoordSetCount; k++) {
                                 System.arraycopy(src.refTexCoords[k],
                                     src.indexTexCoord[k][index]
					*texCoordStride,
                                	vertexData, tOffset, texCoordStride);
                            	 tOffset += texCoordStride;
                            }
                            vOffset += stride;
			}
			base += src.stripIndexCounts[i];
		    }
		    break;
		case T2F:
		    for (i=0; i < src.stripIndexCounts.length; i++) {
			for (j=0; j < src.stripIndexCounts[i]; j++) {
			    index = j+base;
			    for (k = 0, tOffset = vOffset;
				    k < texCoordSetCount; k++) {
                             	 srcOffset =
				    src.indexTexCoord[k][index];
                                 vertexData[tOffset] = ((TexCoord2f[])
					src.refTexCoords[k])[srcOffset].x;
                                 vertexData[tOffset+1] = ((TexCoord2f[])
					src.refTexCoords[k])[srcOffset].y;
                                 tOffset += texCoordStride;
                            }
			    vOffset += stride;
			}
			base += src.stripIndexCounts[i];
		    }
		    break;
		case T3F:
		    for (i=0; i < src.stripIndexCounts.length; i++) {
			for (j=0; j < src.stripIndexCounts[i]; j++) {
			    index = j+base;
			    for (k = 0, tOffset = vOffset;
				    k < texCoordSetCount; k++) {
                             	 srcOffset =
				    src.indexTexCoord[k][index];
                                 vertexData[tOffset] = ((TexCoord3f[])
					src.refTexCoords[k])[srcOffset].x;
                                 vertexData[tOffset+1] = ((TexCoord3f[])
					src.refTexCoords[k])[srcOffset].y;
                                 vertexData[tOffset+2] = ((TexCoord3f[])
					src.refTexCoords[k])[srcOffset].z;
                                 tOffset += texCoordStride;
                            }
			    vOffset += stride;
			}
			base += src.stripIndexCounts[i];
		    }
		    break;

		default:
		    break;
		}
	    }

            if ((vertexFormat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
		base = src.initialIndexIndex;
		vOffset = 0;
		switch (src.vertexType & VATTR_DEFINED) {
		case AF:
		    for (i=0; i < src.stripIndexCounts.length; i++) {
			for (j=0; j < src.stripIndexCounts[i]; j++) {
			    index = j+base;

                            for (k = 0; k < vertexAttrCount; k++) {
                                System.arraycopy(src.floatRefVertexAttrs[k],
                                        src.indexVertexAttr[k][index]*vertexAttrSizes[k],
                                        vertexData,
                                        vOffset + vertexAttrOffsets[k],
                                        vertexAttrSizes[k]);
                            }
                            vOffset += stride;
			}
			base += src.stripIndexCounts[i];
		    }
		    break;
		}
	    }

	    if ((vertexFormat & GeometryArray.COORDINATES) != 0){
		vOffset = coordinateOffset;
		base = src.initialIndexIndex;
		switch ((src.vertexType & VERTEX_DEFINED)) {
		case PF:
		    for (i=0; i < src.stripIndexCounts.length; i++) {
			for (j=0; j < src.stripIndexCounts[i]; j++) {
			    index = j+base;
			    System.arraycopy(src.floatRefCoords,
					     src.indexCoord[index]*3,
					     vertexData,
					     vOffset, 3);
			    vOffset += stride;
			}
			base += src.stripIndexCounts[i];
		    }
		    break;
		case PD:
		    for (i=0; i < src.stripIndexCounts.length; i++) {
			for (j=0; j < src.stripIndexCounts[i]; j++) {
			    index = src.indexCoord[j+base] * 3;
			    vertexData[vOffset] = (float)src.doubleRefCoords[index];
			    vertexData[vOffset+1] = (float)src.doubleRefCoords[index+1];
			    vertexData[vOffset+2] = (float)src.doubleRefCoords[index+2];
			    vOffset += stride;
			}
			base += src.stripIndexCounts[i];
		    }
		    break;
		case P3F:
		    for (i=0; i < src.stripIndexCounts.length; i++) {
			for (j=0; j < src.stripIndexCounts[i]; j++) {
			    index = src.indexCoord[j+base];
			    vertexData[vOffset] = src.p3fRefCoords[index].x;
			    vertexData[vOffset+1] = src.p3fRefCoords[index].y;
			    vertexData[vOffset+2] = src.p3fRefCoords[index].z;
			    vOffset += stride;
			}
			base += src.stripIndexCounts[i];
		    }
		    break;
		case P3D:
		    for (i=0; i < src.stripIndexCounts.length; i++) {
			for (j=0; j < src.stripIndexCounts[i]; j++) {
			    index = src.indexCoord[j+base];
			    vertexData[vOffset] = (float)src.p3dRefCoords[index].x;
			    vertexData[vOffset+1] = (float)src.p3dRefCoords[index].y;
			    vertexData[vOffset+2] = (float)src.p3dRefCoords[index].z;
			    vOffset += stride;
			}
			base += src.stripIndexCounts[i];
		    }
		    break;
		default:
		    break;
		}
	    }
	}
    }

    private void unIndexifyNIOBuffer(IndexedGeometryStripArrayRetained src) {
        int vOffset = 0, srcOffset, tOffset = 0;
        int base = src.initialIndexIndex;
	int i,j, k, index, colorStride = 0;


	// interleaved case
	if ((src.vertexFormat & GeometryArray.INTERLEAVED) != 0) {
	    if ((src.vertexFormat & GeometryArray.WITH_ALPHA) != 0)
		colorStride = 4;
	    else if ((src.vertexFormat & GeometryArray.COLOR) != 0)
		colorStride = 3;

	    for (i=0; i < src.stripIndexCounts.length; i++) {
		for (j=0; j < src.stripIndexCounts[i]; j++) {
		    index = j + base;
		    if ((vertexFormat & GeometryArray.NORMALS) != 0){
			src.interleavedFloatBufferImpl.position(src.indexNormal[index]*src.stride + src.normalOffset);
			src.interleavedFloatBufferImpl.get(vertexData, vOffset + normalOffset, 3);
		    }
		    if (colorStride == 4) {
			src.interleavedFloatBufferImpl.position(src.indexColor[index]*src.stride + src.colorOffset);
			src.interleavedFloatBufferImpl.get(vertexData, vOffset + colorOffset, colorStride);
		    } else if (colorStride == 3) {
			src.interleavedFloatBufferImpl.position(src.indexColor[index]*src.stride + src.colorOffset);
			src.interleavedFloatBufferImpl.get(vertexData, vOffset + colorOffset, colorStride);
			vertexData[vOffset + colorOffset + 3] = 1.0f;
		    }

                    if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
			for (k = 0; k < texCoordSetCount; k++) {
			    src.interleavedFloatBufferImpl.position((src.indexTexCoord[k][index])
					*src.stride + src.textureOffset +
					src.texCoordSetMapOffset[k]);

			    src.interleavedFloatBufferImpl.get(vertexData,
							       vOffset + textureOffset + texCoordSetMapOffset[k], texCoordStride);
                        }
		    }

		    if ((vertexFormat & GeometryArray.COORDINATES) != 0){
			src.interleavedFloatBufferImpl.position(src.indexCoord[index]*src.stride + src.coordinateOffset);
			src.interleavedFloatBufferImpl.get( vertexData, vOffset + coordinateOffset, 3);
		    }
		    vOffset += stride;
		}
		base += src.stripIndexCounts[i];
	    }
	}
	else {
	    if ((vertexFormat & GeometryArray.NORMALS) != 0) {
		base = src.initialIndexIndex;
		vOffset = normalOffset;
		if((src.vertexType & NORMAL_DEFINED) != 0) {
		    for (i=0; i < src.stripIndexCounts.length; i++) {
			for (j=0; j < src.stripIndexCounts[i]; j++) {
			    index = j+base;
			    src.floatBufferRefNormals.position(src.indexNormal[index]*3);
			    src.floatBufferRefNormals.get(vertexData, vOffset, 3);
			    vOffset += stride;
			}
			base += src.stripIndexCounts[i];
		    }
		}
	    }

            if ((vertexFormat & GeometryArray.COLOR) != 0) {
		base = src.initialIndexIndex;
		vOffset = colorOffset;
		int multiplier = 3;
		if ((src.vertexFormat & GeometryArray.WITH_ALPHA) != 0)
		    multiplier = 4;

		switch ((src.vertexType & COLOR_DEFINED)) {
		case CF:
		    for (i=0; i < src.stripIndexCounts.length; i++) {
			for (j=0; j < src.stripIndexCounts[i]; j++) {
			    index = j+base;

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
			base += src.stripIndexCounts[i];
		    }
		    break;
		case CUB:
		    for (i=0; i < src.stripIndexCounts.length; i++) {
			for (j=0; j < src.stripIndexCounts[i]; j++) {
			    index = src.indexColor[j+base] * multiplier;
			    vertexData[vOffset] = (src.byteBufferRefColors.get(index) & 0xff) * ByteToFloatScale;
			    vertexData[vOffset+1] = (src.byteBufferRefColors.get(index+1) & 0xff) * ByteToFloatScale;;
			    vertexData[vOffset+2] = (src.byteBufferRefColors.get(index+2) & 0xff) * ByteToFloatScale;;
			    if ((src.vertexFormat & GeometryArray.WITH_ALPHA) != 0) {
				vertexData[vOffset+3] = (src.byteBufferRefColors.get(index+3) & 0xff) * ByteToFloatScale;
			    }
			    else {
				vertexData[vOffset+3] = 1.0f;
			    }
			    vOffset += stride;
			}
			base += src.stripIndexCounts[i];
		    }
		    break;
		default:
		    break;
		}
	    }

            if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
		base = src.initialIndexIndex;
		vOffset = textureOffset;
		FloatBufferWrapper texBuffer;
		if((src.vertexType & TEXCOORD_DEFINED) != 0) {
		    for (i=0; i < src.stripIndexCounts.length; i++) {
			for (j=0; j < src.stripIndexCounts[i]; j++) {
			    index = j+base;

			    for (k = 0, tOffset = vOffset;
					k < texCoordSetCount; k++) {
				texBuffer = (FloatBufferWrapper)(((J3DBuffer) (src.refTexCoordsBuffer[k])).getBufferImpl());
				texBuffer.position(src.indexTexCoord[k][index]*texCoordStride);
				texBuffer.get(vertexData, tOffset, texCoordStride);
				tOffset += texCoordStride;
                            }
                            vOffset += stride;
			}
			base += src.stripIndexCounts[i];
		    }
		}
	    }

            if ((vertexFormat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
		base = src.initialIndexIndex;
		vOffset = 0;
		if((src.vertexType & VATTR_DEFINED) == AF) {
		    for (i=0; i < src.stripIndexCounts.length; i++) {
			for (j=0; j < src.stripIndexCounts[i]; j++) {
			    index = j+base;

			    for (k = 0; k < vertexAttrCount; k++) {
                                int vaOffset = vOffset + vertexAttrOffsets[k];
                                FloatBufferWrapper vaBuffer = src.floatBufferRefVertexAttrs[k];
				vaBuffer.position(src.indexVertexAttr[k][index]*vertexAttrSizes[k]);
				vaBuffer.get(vertexData, vaOffset, vertexAttrSizes[k]);
                            }
                            vOffset += stride;
			}
			base += src.stripIndexCounts[i];
		    }
		}
	    }

	    if ((vertexFormat & GeometryArray.COORDINATES) != 0) {
		vOffset = coordinateOffset;
		base = src.initialIndexIndex;
		switch ((src.vertexType & VERTEX_DEFINED)) {
		case PF:
		    for (i=0; i < src.stripIndexCounts.length; i++) {
			for (j=0; j < src.stripIndexCounts[i]; j++) {
			    index = j+base;
			    src.floatBufferRefCoords.position(src.indexCoord[index]*3);
			    src.floatBufferRefCoords.get(vertexData, vOffset, 3);

			    vOffset += stride;
			}
			base += src.stripIndexCounts[i];
		    }
		    break;
		case PD:
		    for (i=0; i < src.stripIndexCounts.length; i++) {
			for (j=0; j < src.stripIndexCounts[i]; j++) {
			    index = src.indexCoord[j+base] * 3;
			    vertexData[vOffset] = (float)src.doubleBufferRefCoords.get(index);
			    vertexData[vOffset+1] = (float)src.doubleBufferRefCoords.get(index+1);
			    vertexData[vOffset+2] = (float)src.doubleBufferRefCoords.get(index+2);
			    vOffset += stride;
			}
			base += src.stripIndexCounts[i];
		    }
		    break;

		default:
		    break;
		}
	    }
	}
    }


    /**
     * Get number of strips in the GeometryStripArray
     * @return numStrips number of strips
     */
    int getNumStrips(){
	return stripVertexCounts.length;
    }

    /**
     * Get a list of vertexCounts for each strip
     * @param stripVertexCounts an array that will receive vertexCounts
     */
    void getStripVertexCounts(int stripVertexCounts[]){

	int i, num = this.stripVertexCounts.length;

	for (i=0;i < num;i++)
	{
		stripVertexCounts[i] = this.stripVertexCounts[i];
	}

  }

    void getStripVertexCounts(int id, int counts[]) {
	int stripOffset = compileStripCountOffset[id];
	int stripLength = compileNumStrips[id];
	System.arraycopy(stripVertexCounts, stripOffset, counts, 0,stripLength);
    }


    int getNumStrips(int id) {
	return compileNumStrips[id];
    }

    // Called only for "by-copy" geometry
    void mergeGeometryArrays(ArrayList list) {
	int numMerge = list.size();
	int numStrips = 0;


	for (int i = 0; i < numMerge; i++) {
	    numStrips +=
		((GeometryStripArrayRetained)list.get(i)).stripVertexCounts.length;
	}
	stripVertexCounts = new int[numStrips];
	stripStartVertexIndices = new int[numStrips];
	stripStartOffsetIndices = new int[numStrips];
	int curStripOffset = 0;
	int curStripIndexOffset = 0,stripLength;
	int[] curStripVertexCounts;
	int[]  curStripStartIndices ;
	int[]  curStripOffsetIndices ;


	compileNumStrips = new int[numMerge];
	compileStripCountOffset = new int[numMerge];
	for (int i = 0; i < numMerge; i++) {
	    GeometryStripArrayRetained strip =
				(GeometryStripArrayRetained)list.get(i);
	    curStripVertexCounts = strip.stripVertexCounts;
	    curStripStartIndices = strip.stripStartVertexIndices;
	    curStripOffsetIndices = strip.stripStartOffsetIndices;
	    stripLength = curStripVertexCounts.length;
	    compileNumStrips[i] = stripLength;
	    compileStripCountOffset[i] = curStripOffset;
	    System.arraycopy(curStripVertexCounts, 0, stripVertexCounts,
		curStripOffset, stripLength);
	    // Can't just copy StartIndices, have to update to reflect
	    // updated vertex position on the merged vertexData
	    for (int j = 0; j < stripLength; j++) {
		stripStartVertexIndices[j+curStripOffset] = curStripStartIndices[j] +
			curStripIndexOffset;
		stripStartOffsetIndices[j+curStripOffset] = curStripOffsetIndices[j] +
			curStripIndexOffset;
	    }
	    curStripOffset += stripLength;
	    curStripIndexOffset += strip.validVertexCount;
	 }
	// Assign the merged validVertexCount
	validVertexCount = curStripIndexOffset;

	// call the super to merge the vertex data
	super.mergeGeometryArrays(list);
    }
}
