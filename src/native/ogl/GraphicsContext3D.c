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

#include <stdio.h>
#include <jni.h>

#include "gldefs.h"

extern void throwAssert(JNIEnv *env, char *str);

JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_readRaster(
    JNIEnv *env, jobject obj, jlong ctx,
    jint type, jint xOffset, jint yOffset, 
    jint wRaster, jint hRaster, jint hCanvas,
    jint imageDataType,
    jint imageFormat, jobject imageBuffer,
    jint depthFormat, jobject depthBuffer)
{
    JNIEnv table;
    int yAdjusted;
    GLenum oglFormat;
    void *imageObjPtr;
    void *depthObjPtr;

    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctx;

    table = *env;

    glPixelStorei(GL_PACK_ROW_LENGTH, wRaster);
    glPixelStorei(GL_PACK_ALIGNMENT, 1); 
    yAdjusted = hCanvas - hRaster - yOffset;
    
    if ((type & javax_media_j3d_Raster_RASTER_COLOR) != 0) {

	imageObjPtr =
	    (void *)(*(table->GetPrimitiveArrayCritical))(env, (jarray)imageBuffer, NULL);
	
	if(imageDataType == IMAGE_DATA_TYPE_BYTE_ARRAY)  {
	    
	    switch (imageFormat) {
		/* GL_BGR */
	    case IMAGE_FORMAT_BYTE_BGR:         
		oglFormat = GL_BGR;
		break;
	    case IMAGE_FORMAT_BYTE_RGB:
		oglFormat = GL_RGB;
		break;
		/* GL_ABGR_EXT */
	    case IMAGE_FORMAT_BYTE_ABGR:         
		if (ctxProperties->abgr_ext) { /* If its zero, should never come here! */
		    oglFormat = GL_ABGR_EXT;
		}
		else {
		    throwAssert(env, "readRaster : GL_ABGR_EXT format is unsupported");
		    return;
		}
		break;	
	    case IMAGE_FORMAT_BYTE_RGBA:
		oglFormat = GL_RGBA;
		break;
	    case IMAGE_FORMAT_BYTE_LA:
		/* all LA types are stored as LA8 */
		oglFormat = GL_LUMINANCE_ALPHA;
		break;
	    case IMAGE_FORMAT_BYTE_GRAY: 
	    case IMAGE_FORMAT_USHORT_GRAY:	    
	    case IMAGE_FORMAT_INT_BGR:         
	    case IMAGE_FORMAT_INT_RGB:
	    case IMAGE_FORMAT_INT_ARGB:         
	    default:
		throwAssert(env, "readRaster : imageFormat illegal format");
		return;
	    }
	    
	    glReadPixels(xOffset, yAdjusted, wRaster, hRaster,
			 oglFormat, GL_UNSIGNED_BYTE, imageObjPtr);
	    
	}
	else if(imageDataType == IMAGE_DATA_TYPE_INT_ARRAY) {
	    GLenum intType = GL_UNSIGNED_INT_8_8_8_8;
	    GLboolean forceAlphaToOne = GL_FALSE;

	    switch (imageFormat) {
		/* GL_BGR */
	    case IMAGE_FORMAT_INT_BGR: /* Assume XBGR format */
		oglFormat = GL_RGBA;
		intType = GL_UNSIGNED_INT_8_8_8_8_REV;
		forceAlphaToOne = GL_TRUE;
		break;
	    case IMAGE_FORMAT_INT_RGB: /* Assume XRGB format */
		forceAlphaToOne = GL_TRUE;
		/* Fall through to next case */
	    case IMAGE_FORMAT_INT_ARGB:        
		oglFormat = GL_BGRA;
		intType = GL_UNSIGNED_INT_8_8_8_8_REV;
		break;	
		/* This method only supports 3 and 4 components formats and INT types. */
	    case IMAGE_FORMAT_BYTE_LA:
	    case IMAGE_FORMAT_BYTE_GRAY: 
	    case IMAGE_FORMAT_USHORT_GRAY:
	    case IMAGE_FORMAT_BYTE_BGR:
	    case IMAGE_FORMAT_BYTE_RGB:
	    case IMAGE_FORMAT_BYTE_RGBA:
	    case IMAGE_FORMAT_BYTE_ABGR:
	    default:
		throwAssert(env, "readRaster : imageFormat illegal format");
		return;
	    }  
	    
	    /* Force Alpha to 1.0 if needed */
	    if(forceAlphaToOne) {
		glPixelTransferf(GL_ALPHA_SCALE, 0.0f);
		glPixelTransferf(GL_ALPHA_BIAS, 1.0f);
	    }
	    
	    glReadPixels(xOffset, yAdjusted, wRaster, hRaster,
			 oglFormat, intType, imageObjPtr);
	    
	    /* Restore Alpha scale and bias */
	    if(forceAlphaToOne) {
		glPixelTransferf(GL_ALPHA_SCALE, 1.0f);
		glPixelTransferf(GL_ALPHA_BIAS, 0.0f);
	    }
	}
	else {
	    throwAssert(env, "readRaster : illegal image data type");
	    return;
	}
	
	(*(table->ReleasePrimitiveArrayCritical))(env, imageBuffer, imageObjPtr, 0);	
    }

    if ((type & javax_media_j3d_Raster_RASTER_DEPTH) != 0) {
	GLenum depthType = 0; 
	depthObjPtr = 
	    (void *)(*(table->GetPrimitiveArrayCritical))(env, (jarray)depthBuffer, NULL);
	
        if (depthFormat == javax_media_j3d_DepthComponentRetained_DEPTH_COMPONENT_TYPE_INT) {
	    depthType = GL_UNSIGNED_INT;
        } else { /* javax_media_j3d_DepthComponentRetained_DEPTH_COMPONENT_TYPE_FLOAT */    
	    depthType = GL_FLOAT;
        }
	
	/* yOffset is adjusted for OpenGL - Y upward */
	glReadPixels(xOffset, yAdjusted, wRaster, hRaster, 
		     GL_DEPTH_COMPONENT, depthType , depthObjPtr);

	(*(table->ReleasePrimitiveArrayCritical))(env, depthBuffer, depthObjPtr, 0);
    }
    
}

