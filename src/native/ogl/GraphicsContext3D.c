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


JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_readRasterNative(
    JNIEnv *env, jobject obj, jlong ctx,
    jint type, jint xOffset, jint yOffset, 
    jint wRaster, jint hRaster, jint hCanvas,
    jint format, jobject image, jobject depth, jobject gc)
{
    JNIEnv table;
    int yAdjusted;
    jclass gc_class;
    GLenum gltype;

    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctx;

    table = *env;

    glPixelStorei(GL_PACK_ROW_LENGTH, wRaster);
    glPixelStorei(GL_PACK_ALIGNMENT, 1); 
    yAdjusted = hCanvas - hRaster - yOffset;

    gc_class = (jclass) (*(table->GetObjectClass))(env, gc);

    if ((type & javax_media_j3d_Raster_RASTER_COLOR) != 0) {

        jclass image_class;

        jfieldID byteData_field;
        jarray byteData_array;
        jbyte *byteData;

        byteData_field = (jfieldID)(*(table->GetFieldID))(env,
                gc_class, "byteBuffer","[B");
        byteData_array = (jarray)(*(table->GetObjectField))(env, gc,
                byteData_field);

        image_class = (jclass) (*(table->GetObjectClass))(env, image);

	if (image_class == NULL) {
	    return;
	}
	
        switch (format) {
        case IMAGE_FORMAT_BYTE_RGBA:
            gltype = GL_RGBA;
            break;
        case IMAGE_FORMAT_BYTE_RGB:
            gltype = GL_RGB;
            break;

        case IMAGE_FORMAT_BYTE_ABGR:         
	    if (ctxProperties->abgr_ext) { /* If its zero, should never come here! */
		gltype = GL_ABGR_EXT;
	    }
	    break;
	    
        case IMAGE_FORMAT_BYTE_BGR:         
            gltype = GL_BGR;
	    break;
        case IMAGE_FORMAT_BYTE_LA:
            gltype = GL_LUMINANCE_ALPHA;
            break;

        case IMAGE_FORMAT_BYTE_GRAY:
        case IMAGE_FORMAT_USHORT_GRAY:	    
	default:
	    throwAssert(env, "illegal format");
            break;
        }
	byteData = (jbyte *)(*(table->GetPrimitiveArrayCritical))(env,
							     byteData_array, NULL);
	glReadPixels(xOffset, yAdjusted, wRaster, hRaster,
			 gltype, GL_UNSIGNED_BYTE, byteData);

	/*
	{
	int i, j , *intData;
	fprintf(stderr, "format = %d, wRaster = %d, hRaster = %d\n\n", format, wRaster, hRaster);
	intData = (int*)byteData;
	for (i = 0; i < wRaster; i++) {
	    for (j = 0; j < hRaster; j++, intData++) {
		fprintf(stderr, " 0x%x", *intData);
	    }
	    fprintf(stderr, "\n");
	}
	}
	*/
	(*(table->ReleasePrimitiveArrayCritical))(env, byteData_array,
		byteData, 0);
    }

    
    if ((type & javax_media_j3d_Raster_RASTER_DEPTH) != 0) {

        jclass depth_class; 
        jfieldID wDepth_field, depth_type_field;
        jint depth_type, wDepth;

        depth_class = (jclass) (*(table->GetObjectClass))(env, depth);

	if (depth_class == NULL) {
	    return;
	}
	
        wDepth_field = (jfieldID) (*(table->GetFieldID))(env, depth_class,
                                "width", "I");
        wDepth = (jint)(*(table->GetIntField))(env, depth, wDepth_field);

        depth_type_field = (jfieldID) (*(table->GetFieldID))(env, 
                                depth_class, "type", "I"); 
        depth_type = (jint)(*(table->GetIntField))(env, depth,
                                depth_type_field); 

        if (depth_type == javax_media_j3d_DepthComponentRetained_DEPTH_COMPONENT_TYPE_INT) { 
            jfieldID intData_field;
            jarray intData_array;
            jint *intData;

            intData_field = (jfieldID)(*(table->GetFieldID))(env,
                gc_class, "intBuffer","[I");
            intData_array = (jarray)(*(table->GetObjectField))(env, gc,
                intData_field);

	    intData = (jint *)(*(table->GetPrimitiveArrayCritical))(env,
						    intData_array, NULL);

	    /* yOffset is adjusted for OpenGL - Y upward */
	    glReadPixels(xOffset, yAdjusted, wRaster, hRaster, 
			 GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, intData);

	    (*(table->ReleasePrimitiveArrayCritical))(env, intData_array,
						      intData, 0);
        } else { /* javax_media_j3d_DepthComponentRetained_DEPTH_COMPONENT_TYPE_FLOAT */
            jfieldID floatData_field;
            jarray floatData_array;
            jfloat *floatData;

            floatData_field = (jfieldID)(*(table->GetFieldID))(env,
                gc_class, "floatBuffer","[F");
            floatData_array = (jarray)(*(table->GetObjectField))(env, gc,
                floatData_field);
            floatData = (jfloat *)(*(table->GetPrimitiveArrayCritical))(env,
                floatData_array, NULL);

            /* yOffset is adjusted for OpenGL - Y upward */
	    glReadPixels(xOffset, yAdjusted, wRaster, hRaster, 
			     GL_DEPTH_COMPONENT, GL_FLOAT, floatData);

            (*(table->ReleasePrimitiveArrayCritical))(env, floatData_array,
		floatData, 0);
        }
    }
} 

