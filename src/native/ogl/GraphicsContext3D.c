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

#include <stdio.h>
#include <jni.h>

#include "gldefs.h"

extern void throwAssert(JNIEnv *env, char *str);


JNIEXPORT
void JNICALL Java_javax_media_j3d_GraphicsContext3D_readRasterNative(
    JNIEnv *env, jobject obj, jlong ctxInfo,
    jint type, jint xOffset, jint yOffset, 
    jint wRaster, jint hRaster, jint hCanvas,
    jint format, jobject image, jobject depth, jobject ctx)
{ 
    JNIEnv table;
    int yAdjusted;
    jclass ctx_class;
    GLenum gltype;

    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo;
    jlong d3dctx = ctxProperties->context;

    table = *env;

    glPixelStorei(GL_PACK_ROW_LENGTH, wRaster);
    glPixelStorei(GL_PACK_ALIGNMENT, 1); 
    yAdjusted = hCanvas - hRaster - yOffset;

    ctx_class = (jclass) (*(table->GetObjectClass))(env, ctx);

    if ((type & javax_media_j3d_Raster_RASTER_COLOR) != 0) {

        jclass image_class;

        jfieldID byteData_field;
        jarray byteData_array;
        jbyte *byteData;

        byteData_field = (jfieldID)(*(table->GetFieldID))(env,
                ctx_class, "byteBuffer","[B");
        byteData_array = (jarray)(*(table->GetObjectField))(env, ctx,
                byteData_field);

        image_class = (jclass) (*(table->GetObjectClass))(env, image);

	if (image_class == NULL) {
	    return;
	}
	
        switch (format) {
        case FORMAT_BYTE_RGBA:
            gltype = GL_RGBA;
            break;
        case FORMAT_BYTE_RGB:
            gltype = GL_RGB;
            break;

        case FORMAT_BYTE_ABGR:         
	    if (ctxProperties->abgr_ext) { /* If its zero, should never come here! */
		gltype = GL_ABGR_EXT;
	    }
	    break;
	    
        case FORMAT_BYTE_BGR:         
	    if (ctxProperties->bgr_ext) { /* If its zero, should never come here! */
		gltype = ctxProperties->bgr_ext_enum;
	    }
	    break;
        case FORMAT_BYTE_LA:
            gltype = GL_LUMINANCE_ALPHA;
            break;

        case FORMAT_BYTE_GRAY:
        case FORMAT_USHORT_GRAY:	    
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
                ctx_class, "intBuffer","[I");
            intData_array = (jarray)(*(table->GetObjectField))(env, ctx,
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
                ctx_class, "floatBuffer","[F");
            floatData_array = (jarray)(*(table->GetObjectField))(env, ctx,
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

