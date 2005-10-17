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

#ifdef WIN32
#include <wingdi.h>

#endif /* WIN32 */

#ifdef DEBUG
/* Uncomment the following for VERBOSE debug messages */
/* #define VERBOSE */
#endif /* DEBUG */


static float EPS = 0.0001f;

#define INTERLEAVEDARRAYS_TEST()		\
        useInterleavedArrays = 1; 		\
        switch (vformat) {			\
	case GA_COORDINATES :			\
    	    iaFormat = GL_V3F; break;		\
	case (GA_COORDINATES | GA_NORMALS) :	\
    	    iaFormat = GL_N3F_V3F; break;	\
	case (GA_COORDINATES | GA_TEXTURE_COORDINATE_2) :\
    	    iaFormat = GL_T2F_V3F; break;	\
	case (GA_COORDINATES | GA_NORMALS | GA_COLOR) :	\
	case (GA_COORDINATES | GA_NORMALS | GA_COLOR | GA_WITH_ALPHA) :\
    	    iaFormat = GL_C4F_N3F_V3F; break;	\
	case (GA_COORDINATES | GA_NORMALS | GA_TEXTURE_COORDINATE_2) :\
    	    iaFormat = GL_T2F_N3F_V3F; break;	\
	case (GA_COORDINATES | GA_NORMALS | GA_COLOR | GA_TEXTURE_COORDINATE_2):\
	case (GA_COORDINATES | GA_NORMALS | GA_COLOR | GA_WITH_ALPHA | GA_TEXTURE_COORDINATE_2):\
    	    iaFormat = GL_T2F_C4F_N3F_V3F; break;\
	default:				\
    	    useInterleavedArrays = 0; break;	\
	}


static void enableTexCoordPointer(GraphicsContextPropertiesInfo *, int, int,
				  int, int, void *);
static void disableTexCoordPointer(GraphicsContextPropertiesInfo *, int);
static void clientActiveTextureUnit(GraphicsContextPropertiesInfo *, int);

/* 
 * texUnitIndex < 0  implies send all texture unit state info in one pass
 * texUnitIndex >= 0 implies one texture unit state info in one pass using
 *		     the underlying texture unit 0
 */
static void
executeTexture(int texUnitIndex, int texCoordSetMapLen,
	       int texSize, int bstride, int texCoordoff,
	       jint texCoordSetMapOffset[], 
	       jint numActiveTexUnit, jint texUnitStateMap[],
	       float verts[], jlong ctxInfo)
{
    int i;

    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo;
    jlong ctx = ctxProperties->context;
    int tus; 	/* texture unit state index */
    
    if (texUnitIndex < 0) {
	if(ctxProperties->arb_multitexture) {

	    for (i = 0; i < numActiveTexUnit; i++) {
		/*
		 * NULL texUnitStateMap means 
		 * one to one mapping from texture unit to
		 * texture unit state.  It is NULL in build display list,
		 * when the mapping is according to the texCoordSetMap
		 */
		if (texUnitStateMap != NULL) {
		    tus = texUnitStateMap[i];   
		} else {
		    tus = i;
		}
		/*
		 * it's possible that texture unit state index (tus)
		 * is greater than the texCoordSetMapOffsetLen, in this
		 * case, just disable TexCoordPointer.
		 */
		if ((tus < texCoordSetMapLen) &&
			(texCoordSetMapOffset[tus] != -1)) {
		    enableTexCoordPointer(ctxProperties, i,
			texSize, GL_FLOAT, bstride,
			&(verts[texCoordoff + texCoordSetMapOffset[tus]]));
				
		} else {
		    disableTexCoordPointer(ctxProperties, i);
		}
	    }
	}/* GL_ARB_multitexture */

	else {

#ifdef VERBOSE
	    if (numActiveTexUnit > 1) {
		fprintf(stderr, "No multi-texture support\n");
	    }
#endif /* VERBOSE */

	    if (texUnitStateMap != NULL) {
		tus = texUnitStateMap[0];   
	    } else {
		tus = 0;
	    }
            if (texCoordSetMapOffset[tus] != -1) {
                glEnableClientState(GL_TEXTURE_COORD_ARRAY);
                glTexCoordPointer(texSize, GL_FLOAT, bstride,
                    &(verts[texCoordoff + texCoordSetMapOffset[tus]]));
		
            } else {
                glDisableClientState(GL_TEXTURE_COORD_ARRAY);
            }
	}
    } else {
        if ((texUnitIndex < texCoordSetMapLen) &&
		(texCoordSetMapOffset[texUnitIndex] != -1)) {
	    enableTexCoordPointer(ctxProperties, 0,
		texSize, GL_FLOAT, bstride,
		&(verts[texCoordoff + texCoordSetMapOffset[texUnitIndex]]));
        } else {
	    disableTexCoordPointer(ctxProperties, 0);
        }
    }
}

static void
resetTexture(jlong ctxInfo)
{
    int i;

    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo;

    if(ctxProperties->arb_multitexture) {
	/* Disable texture coordinate arrays for all texture units */
	for (i = 0; i < ctxProperties->maxTexCoordSets; i++) {
	    disableTexCoordPointer(ctxProperties, i);
	}
	/* Reset client active texture unit to 0 */
	clientActiveTextureUnit(ctxProperties, 0);
    } else {
	disableTexCoordPointer(ctxProperties, 0);
    }
}


static void
resetVertexAttrs(jlong ctxInfo, int vertexAttrCount)
{
    int i;

    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo;

    /* Disable specified vertex attr arrays */
    for (i = 0; i < vertexAttrCount; i++) {
	ctxProperties->disableVertexAttrArray(ctxProperties, i);
    }
}


/*
 * glLockArrays() is invoked only for indexed geometry, and the
 * vertexCount is guarenteed to be >= 0.
 */
static void
lockArray(GraphicsContextPropertiesInfo *ctxProperties,
	  int vertexCount) {

    if (ctxProperties->compiled_vertex_array_ext) {
	ctxProperties->glLockArraysEXT(0, vertexCount);
    }
}

static void
unlockArray(GraphicsContextPropertiesInfo *ctxProperties)
{
    if (ctxProperties->compiled_vertex_array_ext) {
        ctxProperties->glUnlockArraysEXT();
    }
}


static void
executeGeometryArray(
    JNIEnv *env,
    jobject obj, jlong ctxInfo, jobject geo, jint geo_type,
    jboolean isNonUniformScale, jboolean useAlpha,
    jboolean multiScreen,
    jboolean ignoreVertexColors,
    jint startVIndex,
    jint vcount, jint vformat,
    jint texCoordSetCount,
    jintArray texCoordSetMap, jint texCoordSetMapLen,
    jintArray texUnitOffset,
    jint numActiveTexUnit,
    jintArray texUnitStateMapArray,
    jint vertexAttrCount, jintArray vertexAttrSizes,
    jfloatArray varray, jobject varrayBuffer, jfloatArray carray,
    jint texUnitIndex, jint cDirty)
{
    jclass geo_class;
    JNIEnv table;

    jfloat *verts, *startVertex, *clrs, *startClrs;
    jint  i;
	size_t bstride, cbstride;
    jsize strip_len;
    GLsizei *strips;
    GLenum iaFormat;
    int useInterleavedArrays;
    int primType;
    jint stride, coordoff, normoff, coloroff, texCoordoff;
    int alphaNeedsUpdate = 0;    /* used so we can get alpha data from */
                                 /* JNI before using it so we can use  */
                                 /* GetPrimitiveArrayCritical */
    jfieldID strip_field;
    jarray sarray;

    jint texSize, texStride, *texCoordSetMapOffset = NULL, 
				*texUnitStateMap = NULL;
    
    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo;
    jlong ctx = ctxProperties->context;

    jarray start_array;
    jfieldID start_field;
    GLint *start;
    int cstride = 0;
    int vAttrStride = 0;
    int vAttrOff;
    jint *vAttrSizesPtr = NULL;
    table = *env;

    geo_class =  (jclass) (*(table->GetObjectClass))(env, geo);

    /* This matches the code in GeometryArrayRetained.java */
    stride = coordoff = normoff = coloroff = texCoordoff = 0;
    vAttrOff = 0;
    if ((vformat & GA_COORDINATES) != 0) {
	stride += 3;
    }
    if ((vformat & GA_NORMALS) != 0) {
	stride += 3;
	coordoff += 3;
    }
    if ((vformat & GA_COLOR) != 0) {
	if ((vformat & GA_WITH_ALPHA) != 0 ) {
	    stride += 4;
	    normoff += 4;
	    coordoff += 4;
	}
	else { /* Handle the case of executeInterleaved 3f */
	    stride += 3;
	    normoff += 3;
	    coordoff += 3;
	}
    }
    if ((vformat & GA_TEXTURE_COORDINATE) != 0) {
        if ((vformat & GA_TEXTURE_COORDINATE_2) != 0) {
	    texSize = 2;
	    texStride = 2 * texCoordSetCount;
        } else if ((vformat & GA_TEXTURE_COORDINATE_3) != 0) {
	    texSize = 3;
	    texStride = 3 * texCoordSetCount;
        } else if ((vformat & GA_TEXTURE_COORDINATE_4) != 0) {
	    texSize = 4;
	    texStride = 4 * texCoordSetCount;
	}
	stride += texStride;
	normoff += texStride;
	coloroff += texStride;
	coordoff += texStride;
    }

    if ((vformat & GA_VERTEX_ATTRIBUTES) != 0) {
	if (vertexAttrSizes != NULL) {
	    vAttrSizesPtr = table->GetIntArrayElements(env, vertexAttrSizes, NULL);
	}
	for (i = 0; i < vertexAttrCount; i++) {
	    vAttrStride += vAttrSizesPtr[i];
	}
	stride += vAttrStride;
	normoff += vAttrStride;
	coloroff += vAttrStride;
	coordoff += vAttrStride;
	texCoordoff += vAttrStride;
    }

    bstride = stride*sizeof(float);

    /*
     * Call other JNI functions before entering Critical region 
     * i.e., GetPrimitiveArrayCritical
     */

     if (geo_type == GEO_TYPE_TRI_STRIP_SET || 
	geo_type == GEO_TYPE_TRI_FAN_SET   || 
	geo_type == GEO_TYPE_LINE_STRIP_SET) {

        strip_field = (jfieldID) (*(table->GetFieldID))(env, geo_class,
			"stripVertexCounts", "[I");
        sarray = (jarray)(*(table->GetObjectField))(env, geo, strip_field);
	strip_len = (jsize)(*(table->GetArrayLength))(env, sarray);


	start_field = (jfieldID) (*(table->GetFieldID))(env, geo_class,
							"stripStartOffsetIndices", "[I");
	start_array = (jarray)(*(table->GetObjectField))(env, geo,
                        start_field);
    }

    /* begin critical region */
    verts = NULL;
    if(varray != NULL) {
	verts = (jfloat *) (*(table->GetPrimitiveArrayCritical))(env, varray, NULL);
    }
    else if(varrayBuffer != NULL) {
	verts = (jfloat *) (*(table->GetDirectBufferAddress))(env, varrayBuffer );
    }
    if (verts == NULL) {
	/* This should never happen */
	fprintf(stderr, "JAVA 3D ERROR : unable to get vertex pointer\n");
	if (vAttrSizesPtr != NULL) {
	    table->ReleaseIntArrayElements(env, vertexAttrSizes, vAttrSizesPtr, JNI_ABORT);
	}
	return;
    }

    /* using byRef interleaved array and has a separate pointer, then .. */
    cstride = stride;
    if (carray != NULL) { 
        clrs = (jfloat *) (*(table->GetPrimitiveArrayCritical))(env, carray, NULL);
	cstride = 4;
	
    }
    else {
        clrs = &(verts[coloroff]);
    }


    cbstride = cstride * sizeof(float);
    if (texCoordSetMapLen >0) {
        texCoordSetMapOffset = (jint *) (*(table->GetPrimitiveArrayCritical))(env, texUnitOffset, NULL);
    }

    if (texUnitStateMapArray != NULL) {
        texUnitStateMap = (jint *) (*(table->GetPrimitiveArrayCritical))(env, texUnitStateMapArray, NULL);
    }

    /* Enable normalize for non-uniform scale (which rescale can't handle) */
    if (ctxProperties->rescale_normal_ext && isNonUniformScale) {
	glEnable(GL_NORMALIZE);
    }


    startVertex = verts + (stride * startVIndex);
    startClrs =  clrs + (cstride * startVIndex);

    /*** Handle non-indexed strip GeometryArray first *******/
    if (geo_type == GEO_TYPE_TRI_STRIP_SET || 
	geo_type == GEO_TYPE_TRI_FAN_SET   || 
	geo_type == GEO_TYPE_LINE_STRIP_SET) {


	strips = (GLsizei *) (*(table->GetPrimitiveArrayCritical))(env, sarray, 
			NULL);

	if ((ignoreVertexColors == JNI_TRUE) || (carray != NULL) || 
	    ((vformat & GA_TEXTURE_COORDINATE) && ((texCoordSetMapLen > 1) ||
		(texCoordSetCount > 1)))) {
	    useInterleavedArrays = 0;
	} else {
	    INTERLEAVEDARRAYS_TEST()
	}
	if (useInterleavedArrays) {
	    glInterleavedArrays(iaFormat, bstride, startVertex);
	} else {
	    if (vformat & GA_NORMALS) {
		glNormalPointer(GL_FLOAT, bstride, &(startVertex[normoff]));
	    }
	    if (ignoreVertexColors == JNI_FALSE && vformat & GA_COLOR) {
		if (vformat & GA_WITH_ALPHA || (useAlpha == GL_TRUE)) {
		    glColorPointer(4, GL_FLOAT, cbstride, startClrs);
		} else {
		    /*
		    for (i = 0; i < vcount; i++) {
			fprintf(stderr, "r = %f, g = %f, b = %f\n", verts[i*bstride +coloroff], verts[i*bstride +coloroff+1],verts[i*bstride +coloroff+2]);
		    }
		    */
		    glColorPointer(3, GL_FLOAT, cbstride, startClrs);
		}
	    }
	    if (vformat & GA_COORDINATES) {
		/*
		    for (i = 0; i < vcount; i++) {
			fprintf(stderr, "x = %f, y = %f, z = %f\n", verts[i*bstride +coordoff], verts[i*bstride +coordoff+1],verts[i*bstride +coordoff+2]);
		    }
		*/
	        glVertexPointer(3, GL_FLOAT, bstride, &(startVertex[coordoff]));
	    }

	    if (vformat & GA_TEXTURE_COORDINATE) {

                executeTexture(texUnitIndex, texCoordSetMapLen,
                                texSize, bstride, texCoordoff,
                                texCoordSetMapOffset, 
				numActiveTexUnit, texUnitStateMap, 
				startVertex, ctxInfo);
	    } 

	    if (vformat & GA_VERTEX_ATTRIBUTES) {
		jfloat *vAttrPtr = &startVertex[vAttrOff];

		for (i = 0; i < vertexAttrCount; i++) {
		    ctxProperties->enableVertexAttrArray(ctxProperties, i);
		    ctxProperties->vertexAttrPointer(ctxProperties, i, vAttrSizesPtr[i],
						     GL_FLOAT, bstride, vAttrPtr);
		    vAttrPtr += vAttrSizesPtr[i];
		}
	    }
	}  

	switch (geo_type) {
	  case GEO_TYPE_TRI_STRIP_SET :
	    primType = GL_TRIANGLE_STRIP;
	    break;
	  case GEO_TYPE_TRI_FAN_SET :
	    primType = GL_TRIANGLE_FAN;
	    break;
	  case GEO_TYPE_LINE_STRIP_SET :
	    primType = GL_LINE_STRIP;
	    break;
	}
	/*
	fprintf(stderr, "strip_len = %d\n",strip_len);
	for (i=0; i < strip_len;i++) {
	    fprintf(stderr, "strips[i] = %d\n",strips[i]);
	}
	*/


	start = (GLint *)(*(table->GetPrimitiveArrayCritical))(env, 
			start_array, NULL);

	if (ctxProperties->multi_draw_arrays_ext || ctxProperties->multi_draw_arrays_sun) { 
	    /*
	     * Only used in the "by_copy case, so its ok to
	     * to temporarily modify
	     */
	    
	    ctxProperties->glMultiDrawArraysEXT(primType, start, strips, strip_len);
	} else {
	    for (i=0; i < strip_len;i++) {
		glDrawArrays(primType, start[i], strips[i]);
	    }
	}
	(*(table->ReleasePrimitiveArrayCritical))(env, start_array, start,
			0);	
	(*(table->ReleasePrimitiveArrayCritical))(env, sarray, strips, 0);
    }
    /******* Handle non-indexed non-striped GeometryArray now *****/
    else if ((geo_type == GEO_TYPE_QUAD_SET) ||
	     (geo_type == GEO_TYPE_TRI_SET) ||
	     (geo_type == GEO_TYPE_POINT_SET) ||
	     (geo_type == GEO_TYPE_LINE_SET))
    {


	if ((ignoreVertexColors == JNI_TRUE) || (carray != NULL) ||
	    ((vformat & GA_TEXTURE_COORDINATE) && ((texCoordSetMapLen > 1) ||
		(texCoordSetCount > 1)))) {
	    useInterleavedArrays = 0;
	} else {
	    INTERLEAVEDARRAYS_TEST()
	}

	if (useInterleavedArrays) {
	    glInterleavedArrays(iaFormat, bstride, startVertex);
	} else {
	    if (vformat & GA_NORMALS) {
		glNormalPointer(GL_FLOAT, bstride, &(startVertex[normoff]));
	    } 
	    if (ignoreVertexColors == JNI_FALSE && vformat & GA_COLOR) {
		if (vformat & GA_WITH_ALPHA || (useAlpha == GL_TRUE)) {

		    glColorPointer(4, GL_FLOAT, cbstride, startClrs);
		} else {
		    glColorPointer(3, GL_FLOAT, cbstride, startClrs);
		}
	    }
	    if (vformat & GA_COORDINATES) {
		glVertexPointer(3, GL_FLOAT, bstride, &(startVertex[coordoff]));
	    } 

	    if (vformat & GA_TEXTURE_COORDINATE) {

                executeTexture(texUnitIndex, texCoordSetMapLen,
                                texSize, bstride, texCoordoff,
                                texCoordSetMapOffset, 
				numActiveTexUnit, texUnitStateMap, 
				startVertex, ctxInfo);
	    } 

	    if (vformat & GA_VERTEX_ATTRIBUTES) {
		jfloat *vAttrPtr = &startVertex[vAttrOff];

		for (i = 0; i < vertexAttrCount; i++) {
		    ctxProperties->enableVertexAttrArray(ctxProperties, i);
		    ctxProperties->vertexAttrPointer(ctxProperties, i, vAttrSizesPtr[i],
						     GL_FLOAT, bstride, vAttrPtr);
		    vAttrPtr += vAttrSizesPtr[i];
		}
	    }
	}
	switch (geo_type){
	  case GEO_TYPE_QUAD_SET : glDrawArrays(GL_QUADS, 0, vcount);break;
	  case GEO_TYPE_TRI_SET : glDrawArrays(GL_TRIANGLES, 0, vcount);break;
	  case GEO_TYPE_POINT_SET : glDrawArrays(GL_POINTS, 0, vcount);break;
	  case GEO_TYPE_LINE_SET: glDrawArrays(GL_LINES, 0, vcount);break;
	}
    }
    /* clean up if we turned on normalize */

    if (ctxProperties->rescale_normal_ext && isNonUniformScale) {
	glDisable(GL_NORMALIZE);
    }

    if (vformat & GA_VERTEX_ATTRIBUTES) {
	resetVertexAttrs(ctxInfo, vertexAttrCount);
    }

    if (vformat & GA_TEXTURE_COORDINATE) {
	resetTexture(ctxInfo);
    }

    if (carray != NULL) 
	(*(table->ReleasePrimitiveArrayCritical))(env, carray, clrs, 0);

    if (texCoordSetMapLen > 0)
        (*(table->ReleasePrimitiveArrayCritical))(env, texUnitOffset, 
						texCoordSetMapOffset, 0);

    if (texUnitStateMap != NULL)
        (*(table->ReleasePrimitiveArrayCritical))(env, texUnitStateMapArray, 
						texUnitStateMap, 0);
    if(varray != NULL)
	(*(table->ReleasePrimitiveArrayCritical))(env, varray, verts, 0); 

    if (vAttrSizesPtr != NULL) {
	table->ReleaseIntArrayElements(env, vertexAttrSizes, vAttrSizesPtr, JNI_ABORT);
    }
}


/*
 * Class:     javax_media_j3d_GeometryArrayRetained
 * Method:    execute
 * Signature: (JLjavax/media/j3d/GeometryArrayRetained;IZZZZIIII[II[II[II[I[F[FII)V
 */
JNIEXPORT void JNICALL
Java_javax_media_j3d_GeometryArrayRetained_execute(JNIEnv *env, 
    jobject obj, jlong ctxInfo, jobject geo, jint geo_type,
    jboolean isNonUniformScale, jboolean useAlpha,
    jboolean multiScreen,
    jboolean ignoreVertexColors,
    jint startVIndex,
    jint vcount, jint vformat,
    jint texCoordSetCount,
    jintArray texCoordSetMap, jint texCoordSetMapLen,
    jintArray texUnitOffset,
    jint numActiveTexUnit,
    jintArray texUnitStateMapArray,
    jint vertexAttrCount, jintArray vertexAttrSizes,
    jfloatArray varray, jfloatArray carray,
    jint texUnitIndex, jint cDirty)
{

#ifdef VERBOSE
    fprintf(stderr, "GeometryArrayRetained.execute() -- calling executeGeometryArray\n");
#endif /* VERBOSE */

    /* call executeGeometryArray */
    executeGeometryArray(env, obj, ctxInfo, geo, geo_type, isNonUniformScale, useAlpha,
			 multiScreen, ignoreVertexColors, startVIndex, vcount, vformat,
			 texCoordSetCount, texCoordSetMap, texCoordSetMapLen,
			 texUnitOffset, numActiveTexUnit, texUnitStateMapArray,
			 vertexAttrCount, vertexAttrSizes,
			 varray, NULL, carray, texUnitIndex, cDirty);

}

/* interleaved data with nio buffer as data format */
JNIEXPORT void JNICALL
Java_javax_media_j3d_GeometryArrayRetained_executeInterleavedBuffer(
    JNIEnv *env, 
    jobject obj, jlong ctxInfo, jobject geo, jint geo_type, 
    jboolean isNonUniformScale, jboolean useAlpha,
    jboolean multiScreen,						
    jboolean ignoreVertexColors,
    jint startVIndex,
    jint vcount, jint vformat, 
    jint texCoordSetCount,
    jintArray texCoordSetMap, jint texCoordSetMapLen,
    jintArray texUnitOffset, 
    jint numActiveTexUnit,
    jintArray texUnitStateMapArray,
    jobject varray, jfloatArray carray,
    jint texUnitIndex, jint cDirty)
{

#ifdef VERBOSE
    fprintf(stderr, "GeometryArrayRetained.executeInterleavedBuffer() -- calling executeGeometryArray\n");
#endif /* VERBOSE */

    /* call executeGeometryArray */
    executeGeometryArray(env, obj, ctxInfo, geo, geo_type,  isNonUniformScale,  useAlpha,
			 multiScreen, ignoreVertexColors, startVIndex, vcount, vformat,
			 texCoordSetCount, texCoordSetMap, texCoordSetMapLen,
			 texUnitOffset, numActiveTexUnit, texUnitStateMapArray,
			 0, NULL,
			 NULL, varray, carray, texUnitIndex, cDirty);

}


/*
 * Class:     javax_media_j3d_GeometryArrayRetained
 * Method:    buildGA
 * Signature: (JLjavax/media/j3d/GeometryArrayRetained;IZZFZIIII[II[II[I[D[D[F)V
 */
JNIEXPORT
    void JNICALL Java_javax_media_j3d_GeometryArrayRetained_buildGA(JNIEnv *env, 
    jobject obj, jlong ctxInfo, jobject geo, 
    jint geo_type, 
    jboolean isNonUniformScale, jboolean updateAlpha, float alpha,
    jboolean ignoreVertexColors,
    jint startVIndex,
    jint vcount, jint vformat, 
    jint texCoordSetCount,
    jintArray texCoordSetMapArray,
    jint texCoordSetMapLen,
    jintArray texUnitOffset, 
    jint vertexAttrCount, jintArray vertexAttrSizes,
    jdoubleArray xform, jdoubleArray nxform,
    jfloatArray varray)
{
    jclass geo_class;
    JNIEnv table;
    jboolean useAlpha = JNI_FALSE;

    jfloat *verts;
    jint i, j;
	size_t bstride;
    jint texStride, *texCoordSetMapOffset;
    int vAttrStride = 0;
    int vAttrOff;
    jint *vAttrSizesPtr = NULL;
    GLsizei *strips;
    jfloat vertex[3];
    jfloat normal[3];
    jfloat w, winv;

    jsize strip_len;
    int primType;
    jint stride, coordoff, normoff, coloroff, texCoordoff;
    jfieldID strip_field;
    jarray sarray;
    jint initialOffset = 0;
    jint saveVformat =  0;
    float color[4];
    jdouble *xform_ptr = NULL;
    jdouble *nxform_ptr = NULL;

    jint k;

    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo;
    jlong ctx = ctxProperties->context;
      
#ifdef VERBOSE
    fprintf(stderr, "GeometryArrayRetained.buildGA()\n");
#endif /* VERBOSE */

    table = *env;
    geo_class =  (jclass) (*(table->GetObjectClass))(env, geo);

    /* This matches the code in GeometryArrayRetained.java */
    stride = coordoff = normoff = coloroff = texCoordoff = 0;
    vAttrOff = 0;
    if ((vformat & GA_COORDINATES) != 0) {
	stride += 3;
    } 
    if ((vformat & GA_NORMALS) != 0) {
	stride += 3;
	coordoff += 3;
    } 
    
    if ((vformat & GA_COLOR) != 0) {
	if ((vformat & GA_BY_REFERENCE) != 0) {
	    if (vformat & GA_WITH_ALPHA) {
		stride += 4;
		normoff += 4;
		coordoff += 4;
	    }
	    else {
		stride += 3;
		normoff += 3;
		coordoff += 3;
	    }
	}
	else {
	    stride += 4;
	    normoff += 4;
	    coordoff += 4;
	}
    }

    if ((vformat & GA_TEXTURE_COORDINATE) != 0) {
        if ((vformat & GA_TEXTURE_COORDINATE_2) != 0) {
	    texStride = 2 * texCoordSetCount;
        } else if ((vformat & GA_TEXTURE_COORDINATE_3) != 0) {
	    texStride = 3 * texCoordSetCount;
        } else if ((vformat & GA_TEXTURE_COORDINATE_4) != 0) {
	    texStride = 4 * texCoordSetCount;
	}
	stride += texStride;
	normoff += texStride;
	coloroff += texStride;
	coordoff += texStride;
    }

    if ((vformat & GA_VERTEX_ATTRIBUTES) != 0) {
	if (vertexAttrSizes != NULL) {
	    vAttrSizesPtr = table->GetIntArrayElements(env, vertexAttrSizes, NULL);
	}
	for (i = 0; i < vertexAttrCount; i++) {
	    vAttrStride += vAttrSizesPtr[i];
	}
	stride += vAttrStride;
	normoff += vAttrStride;
	coloroff += vAttrStride;
	coordoff += vAttrStride;
	texCoordoff += vAttrStride;
    }

    bstride = stride*sizeof(float);
    /* Start send down from the startVIndex */
    initialOffset = startVIndex * stride;
    normoff += initialOffset;
    coloroff += initialOffset;
    coordoff += initialOffset;
    texCoordoff += initialOffset;
    vAttrOff += initialOffset;

    /* 
     * process alpha for geometryArray without alpha
     */
    if (updateAlpha == JNI_TRUE && ignoreVertexColors == JNI_FALSE) {
	useAlpha = JNI_TRUE;
    }
    
    /*
     * call other JNI functions before entering Critical region 
     * i.e., GetPrimitiveArrayCritical
     */
    if (geo_type == GEO_TYPE_TRI_STRIP_SET || 
	geo_type == GEO_TYPE_TRI_FAN_SET   || 
	geo_type == GEO_TYPE_LINE_STRIP_SET) {

        strip_field = (jfieldID) (*(table->GetFieldID))(env, geo_class,
			"stripVertexCounts", "[I");
        sarray = (jarray)(*(table->GetObjectField))(env, geo, strip_field);
	strip_len = (jsize)(*(table->GetArrayLength))(env, sarray);
    }


    /* begin critical region */
    verts = (jfloat *) (*(table->GetPrimitiveArrayCritical))(env, varray, NULL);

    if (texCoordSetMapLen >0) {
        texCoordSetMapOffset = (jint *)(*(table->GetPrimitiveArrayCritical))
						(env, texUnitOffset, NULL);
    }


    /* get the static transform if exists */
    if (xform != NULL) {
        xform_ptr = (jdouble *) (*(table->GetPrimitiveArrayCritical))(
				env, xform, NULL);
    }

    /* get the static normals transform if exists */
    if (nxform != NULL) {
        nxform_ptr = (jdouble *) (*(table->GetPrimitiveArrayCritical))(
				env, nxform, NULL);
    }
        

    if (geo_type == GEO_TYPE_TRI_STRIP_SET || 
	geo_type == GEO_TYPE_TRI_FAN_SET   || 
	geo_type == GEO_TYPE_LINE_STRIP_SET) {


	switch (geo_type) {
	  case GEO_TYPE_TRI_STRIP_SET :
	    primType = GL_TRIANGLE_STRIP;
	    break;
	  case GEO_TYPE_TRI_FAN_SET :
	    primType = GL_TRIANGLE_FAN;
	    break;
	  case GEO_TYPE_LINE_STRIP_SET :
	    primType = GL_LINE_STRIP;
	    break;
	}


	strips = (GLsizei *) (*(table->GetPrimitiveArrayCritical))(env, sarray, NULL);
	saveVformat = vformat;
	if (ignoreVertexColors == JNI_TRUE) {
	    vformat &= ~GA_COLOR;
        }
	for (i = 0; i < strip_len; i++) {
	    glBegin(primType);
	    for (j = 0; j <  strips[i]; j++) {
		if (vformat & GA_NORMALS) {
                    if (nxform_ptr != NULL) {
                        normal[0] =  (float) (nxform_ptr[0] * verts[normoff] +
					      nxform_ptr[1] * verts[normoff+1] +
					      nxform_ptr[2] * verts[normoff+2]);
                        normal[1] =  (float ) (nxform_ptr[4] * verts[normoff] +
					       nxform_ptr[5] * verts[normoff+1] +
					       nxform_ptr[6] * verts[normoff+2]);
                        normal[2] =  (float) (nxform_ptr[8] * verts[normoff] +
					      nxform_ptr[9] * verts[normoff+1] +
					      nxform_ptr[10] * verts[normoff+2]);
/*
printf("orig: < %g %g %g >  transformed: < %g %g %g >\n",
        verts[normoff], verts[normoff+1], verts[normoff+2],
        normal[0], normal[1], normal[2]);
*/
		        glNormal3fv(normal);
		    } else {
		        glNormal3fv(&verts[normoff]);
		    }
		}
		if (vformat & GA_COLOR) {
		    if (useAlpha ) {
			color[0] = verts[coloroff];
			color[1] = verts[coloroff+1];
			color[2] = verts[coloroff+2];
			color[3] = verts[coloroff+3] * alpha;
			glColor4fv(&color[0]);
		    }
		    else {
			if (vformat & GA_WITH_ALPHA) { /* alpha is present */
			    glColor4fv(&verts[coloroff]);
			}
			else {
			    glColor3fv(&verts[coloroff]);
			}
		    }
		}

		if (vformat & GA_VERTEX_ATTRIBUTES) {
		    int vaIdx, vaOff;

		    vaOff = vAttrOff;
		    for (vaIdx = 0; vaIdx < vertexAttrCount; vaIdx++) {
#ifdef VERBOSE
			fprintf(stderr, "vertexAttrs[%d] = (", vaIdx);
			for (k = 0; k < vAttrSizesPtr[vaIdx]; k++) {
			    fprintf(stderr, "%g, ",
				    verts[vaOff+k]);
			}
			fprintf(stderr, ")\n");
#endif /* VERBOSE */
			switch (vAttrSizesPtr[vaIdx]) {
			case 1:
			    ctxProperties->vertexAttr1fv(ctxProperties, vaIdx, &verts[vaOff]);
			    break;
			case 2:
			    ctxProperties->vertexAttr2fv(ctxProperties, vaIdx, &verts[vaOff]);
			    break;
			case 3:
			    ctxProperties->vertexAttr3fv(ctxProperties, vaIdx, &verts[vaOff]);
			    break;
			case 4:
			    ctxProperties->vertexAttr4fv(ctxProperties, vaIdx, &verts[vaOff]);
			    break;
			}

			vaOff += vAttrSizesPtr[vaIdx];
		    }
		}

		if (vformat & GA_TEXTURE_COORDINATE) {

		    if (texCoordSetMapLen > 0) {

			if (ctxProperties->arb_multitexture) {
			    if (vformat & GA_TEXTURE_COORDINATE_2) {
				for (k = 0; k < texCoordSetMapLen; k++) {
				    if (texCoordSetMapOffset[k] != -1) {
					ctxProperties->glMultiTexCoord2fvARB(
						GL_TEXTURE0_ARB + k, 
						&verts[texCoordoff + 
						texCoordSetMapOffset[k]]);
				    } 
				}
			    } else if (vformat & GA_TEXTURE_COORDINATE_3) {
				for (k = 0; k < texCoordSetMapLen; k++) {
				    if (texCoordSetMapOffset[k] != -1) {
					ctxProperties->glMultiTexCoord3fvARB(
						GL_TEXTURE0_ARB + k, 
						&verts[texCoordoff + 
						texCoordSetMapOffset[k]]);
				    }
				}
			    } else {
				for (k = 0; k < texCoordSetMapLen; k++) {
				    if (texCoordSetMapOffset[k] != -1) {
					ctxProperties->glMultiTexCoord4fvARB(
						GL_TEXTURE0_ARB + k, 
						&verts[texCoordoff + 
						texCoordSetMapOffset[k]]);
				    }
				}
			    }
			}
			else { /* GL_ARB_multitexture */

			    if (texCoordSetMapOffset[0] != -1) {
			        if (vformat & GA_TEXTURE_COORDINATE_2) {
				    glTexCoord2fv(&verts[texCoordoff +
						texCoordSetMapOffset[0]]);
			        } else if (vformat & GA_TEXTURE_COORDINATE_3) {
				    glTexCoord3fv(&verts[texCoordoff +
						texCoordSetMapOffset[0]]);
			        } else {
				    glTexCoord4fv(&verts[texCoordoff +
						texCoordSetMapOffset[0]]);
				}
			    }
			} /* GL_ARB_multitexture */
		    }
		    /*
		     * texCoordSetMapLen can't be 0 if texture coordinates
		     * is to be specified
		     */
		}
		if (vformat & GA_COORDINATES) {
		    if (xform_ptr != NULL) {

			/*
			 * transform the vertex data with the
			 * static transform
			 */
			w         = (float ) (xform_ptr[12] * verts[coordoff] +
					      xform_ptr[13] * verts[coordoff+1] +
					      xform_ptr[14] * verts[coordoff+2] +
					      xform_ptr[15]);
			winv	  = 1.0f/w;
			vertex[0] = (float ) (xform_ptr[0] * verts[coordoff] +
					      xform_ptr[1] * verts[coordoff+1] +
					      xform_ptr[2] * verts[coordoff+2] +
					      xform_ptr[3]) * winv;
			vertex[1] = (float) (xform_ptr[4] * verts[coordoff] +
					     xform_ptr[5] * verts[coordoff+1] +
					     xform_ptr[6] * verts[coordoff+2] +
					     xform_ptr[7]) * winv;
			vertex[2] = (float) (xform_ptr[8] * verts[coordoff] +
					     xform_ptr[9] * verts[coordoff+1] +
					     xform_ptr[10] * verts[coordoff+2] +
					     xform_ptr[11]) * winv;
/*
printf("orig: < %g %g %g >  transformed: < %g %g %g >\n",
	verts[coordoff], verts[coordoff+1], verts[coordoff+2],
	vertex[0], vertex[1], vertex[2]);
*/
			glVertex3fv(vertex);
		    } else {
		        glVertex3fv(&verts[coordoff]);
		    }
		}
		normoff += stride;
		coloroff += stride;
		coordoff += stride;
		texCoordoff += stride;
		vAttrOff += stride;
	    }
	    glEnd();
	}
	/* Restore the  vertex format */
	vformat = saveVformat;
	(*(table->ReleasePrimitiveArrayCritical))(env, sarray, strips,
			0);

    }
    else if ((geo_type == GEO_TYPE_QUAD_SET) ||
	     (geo_type == GEO_TYPE_TRI_SET) ||
	     (geo_type == GEO_TYPE_POINT_SET) ||
	     (geo_type == GEO_TYPE_LINE_SET)) {

	switch (geo_type) {
	  case GEO_TYPE_QUAD_SET :
	    primType = GL_QUADS;
	    break;
	  case GEO_TYPE_TRI_SET :
	    primType = GL_TRIANGLES;
	    break;
	  case GEO_TYPE_POINT_SET :
	    primType = GL_POINTS;
	    break;
	  case GEO_TYPE_LINE_SET :
	    primType = GL_LINES;
	    break;
   
	}

	saveVformat = vformat;
	if (ignoreVertexColors == JNI_TRUE) {
	    vformat &= ~GA_COLOR;
        }
	glBegin(primType);
	for (j = 0; j <  vcount; j++) {
	    if (vformat & GA_NORMALS) {
                if (nxform_ptr != NULL) {
                    normal[0] =  (float) (nxform_ptr[0] * verts[normoff] +
					  nxform_ptr[1] * verts[normoff+1] +
					  nxform_ptr[2] * verts[normoff+2]);
		    normal[1] =  (float) (nxform_ptr[4] * verts[normoff] +
					  nxform_ptr[5] * verts[normoff+1] +
					  nxform_ptr[6] * verts[normoff+2]);
                    normal[2] =  (float) (nxform_ptr[8] * verts[normoff] +
					  nxform_ptr[9] * verts[normoff+1] +
					  nxform_ptr[10] * verts[normoff+2]);
/*
printf("orig: < %g %g %g >  transformed: < %g %g %g >\n",
        verts[normoff], verts[normoff+1], verts[normoff+2],
        normal[0], normal[1], normal[2]);
*/
                    glNormal3fv(normal);
                } else {
                    glNormal3fv(&verts[normoff]);
                }
	    }
	    if (vformat & GA_COLOR) {
		if (useAlpha ) {
		    if (vformat & GA_WITH_ALPHA) {
			color[0] = verts[coloroff];
			color[1] = verts[coloroff+1];
			color[2] = verts[coloroff+2];
			color[3] = verts[coloroff+3] * alpha;
		    }
		    else {
			color[0] = verts[coloroff];
			color[1] = verts[coloroff+1];
			color[2] = verts[coloroff+2];
			color[3] = alpha;
		    }
		    glColor4fv(&color[0]);

		}
		else {
		    if (vformat & GA_WITH_ALPHA) { /* alpha is present */
			glColor4fv(&verts[coloroff]);
		    }
		    else {
			glColor3fv(&verts[coloroff]);
		    }
		}
	    }

	    if (vformat & GA_VERTEX_ATTRIBUTES) {
		int vaIdx, vaOff;

		vaOff = vAttrOff;
		for (vaIdx = 0; vaIdx < vertexAttrCount; vaIdx++) {
#ifdef VERBOSE
		    fprintf(stderr, "vertexAttrs[%d] = (", vaIdx);
		    for (k = 0; k < vAttrSizesPtr[vaIdx]; k++) {
			fprintf(stderr, "%g, ",
				verts[vaOff+k]);
		    }
		    fprintf(stderr, ")\n");
#endif /* VERBOSE */
		    switch (vAttrSizesPtr[vaIdx]) {
		    case 1:
			ctxProperties->vertexAttr1fv(ctxProperties, vaIdx, &verts[vaOff]);
			break;
		    case 2:
			ctxProperties->vertexAttr2fv(ctxProperties, vaIdx, &verts[vaOff]);
			break;
		    case 3:
			ctxProperties->vertexAttr3fv(ctxProperties, vaIdx, &verts[vaOff]);
			break;
		    case 4:
			ctxProperties->vertexAttr4fv(ctxProperties, vaIdx, &verts[vaOff]);
			break;
		    }

		    vaOff += vAttrSizesPtr[vaIdx];
		}
	    }

	    if (vformat & GA_TEXTURE_COORDINATE) {

		if (texCoordSetMapLen > 0) {

		    if(ctxProperties->arb_multitexture) {
			if (vformat & GA_TEXTURE_COORDINATE_2) {
			    for (k = 0; k < texCoordSetMapLen; k++) {
				if (texCoordSetMapOffset[k] != -1) {
				    ctxProperties->glMultiTexCoord2fvARB(
						GL_TEXTURE0_ARB + k, 
						&verts[texCoordoff + 
						texCoordSetMapOffset[k]]);
				}
			    }
			} else if (vformat & GA_TEXTURE_COORDINATE_3) {
			    for (k = 0; k < texCoordSetMapLen; k++) {
				if (texCoordSetMapOffset[k] != -1) {
				    ctxProperties->glMultiTexCoord3fvARB(
						GL_TEXTURE0_ARB + k, 
						&verts[texCoordoff + 
						texCoordSetMapOffset[k]]);
				}
			    }
			} else {
			    for (k = 0; k < texCoordSetMapLen; k++) {
				if (texCoordSetMapOffset[k] != -1) {
				    ctxProperties->glMultiTexCoord4fvARB(
						GL_TEXTURE0_ARB + k, 
						&verts[texCoordoff + 
						texCoordSetMapOffset[k]]);
				}
			    }
			}
		    }
		    else {  /* GL_ARB_multitexture */

			if (texCoordSetMapOffset[0] != -1) {
			    if (vformat & GA_TEXTURE_COORDINATE_2) {
			        glTexCoord2fv(&verts[texCoordoff +
						texCoordSetMapOffset[0]]);
			    } else if (vformat & GA_TEXTURE_COORDINATE_3) {
			        glTexCoord3fv(&verts[texCoordoff +
						texCoordSetMapOffset[0]]);
			    } else {
			        glTexCoord4fv(&verts[texCoordoff +
						texCoordSetMapOffset[0]]);
			    }
			}
		    } /* GL_ARB_multitexture */
		}

		/*
		 * texCoordSetMapLen can't be 0 if texture coordinates
		 * is to be specified
		 */
	    }

	    if (vformat & GA_COORDINATES) {
		if (xform_ptr != NULL) {

		    /*
		     * transform the vertex data with the
		     * static transform
		     */
		    w         = (float) (xform_ptr[12] * verts[coordoff] +
					     xform_ptr[13] * verts[coordoff+1] +
					     xform_ptr[14] * verts[coordoff+2] +
					     xform_ptr[15]);
		    winv	  = 1.0f/w;
		    vertex[0] = (float) (xform_ptr[0] * verts[coordoff] +
				     xform_ptr[1] * verts[coordoff+1] +
				     xform_ptr[2] * verts[coordoff+2] +
				     xform_ptr[3]) * winv;
		    vertex[1] = (float) (xform_ptr[4] * verts[coordoff] +
				     xform_ptr[5] * verts[coordoff+1] +
				     xform_ptr[6] * verts[coordoff+2] +
				     xform_ptr[7]) * winv;
		    vertex[2] = (float) (xform_ptr[8] * verts[coordoff] +
				     xform_ptr[9] * verts[coordoff+1] +
				     xform_ptr[10] * verts[coordoff+2] +
				     xform_ptr[11]) * winv;
/*
printf("orig: < %g %g %g >  transformed: < %g %g %g >\n",
	verts[coordoff], verts[coordoff+1], verts[coordoff+2],
	vertex[0], vertex[1], vertex[2]);
*/
		    glVertex3fv(vertex);
		} else {
		    glVertex3fv(&verts[coordoff]);
		}
	    }
	    normoff += stride;
	    coloroff += stride;
	    coordoff += stride;
	    texCoordoff += stride;
	    vAttrOff += stride;
	}
	glEnd();
    }
    /* Restore the  vertex format */
    vformat = saveVformat;


    (*(table->ReleasePrimitiveArrayCritical))(env, varray, verts, 0);

    if (texCoordSetMapLen > 0)
        (*(table->ReleasePrimitiveArrayCritical))(env, texUnitOffset, 
						texCoordSetMapOffset, 0);

    if (xform_ptr != NULL)
        (*(table->ReleasePrimitiveArrayCritical))(env, xform, xform_ptr, 0);

    if (nxform_ptr != NULL)
        (*(table->ReleasePrimitiveArrayCritical))(env, nxform, nxform_ptr, 0);

    if (vAttrSizesPtr != NULL) {
	table->ReleaseIntArrayElements(env, vertexAttrSizes, vAttrSizesPtr, JNI_ABORT);
    }
}

static void
enableTexCoordPointer(
    GraphicsContextPropertiesInfo *ctxProperties,
    int texUnit,
    int texSize,
    int texDataType,
    int stride,
    void *pointer)
{
    clientActiveTextureUnit(ctxProperties, texUnit);
    glEnableClientState(GL_TEXTURE_COORD_ARRAY);
    glTexCoordPointer(texSize, texDataType, stride, pointer);
}

static void
disableTexCoordPointer(
    GraphicsContextPropertiesInfo *ctxProperties,
    int texUnit)
{
    clientActiveTextureUnit(ctxProperties, texUnit);
    glDisableClientState(GL_TEXTURE_COORD_ARRAY);
}

static void
clientActiveTextureUnit(
    GraphicsContextPropertiesInfo *ctxProperties,
    int texUnit)
{
    if (ctxProperties->arb_multitexture) {
        ctxProperties->glClientActiveTextureARB(texUnit + GL_TEXTURE0_ARB);
    }
}


static void
executeGeometryArrayVA(
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
    jfloat* fverts,
    jdouble* dverts,
    jint initialColorIndex,
    jfloat* fclrs,
    jbyte*  bclrs,
    jint initialNormalIndex,
    jfloat* norms,
    jint vertexAttrCount,
    jintArray vertexAttrSizes,
    jintArray vertexAttrIndices,
    jfloat ** vertexAttrPointer,
    jint pass,  
    jint texCoordMapLength,
    jintArray 	tcoordsetmap,
    jint numActiveTexUnit,
    jint* texUnitStateMap,
    jintArray texindices,
    jint texStride,
    jfloat** texCoordPointer,
    jint cdirty,
    jarray sarray,
    jsize strip_len,
    jarray start_array)
{
    int primType;
    JNIEnv table;
    jint i;
    GLsizei *strips;

    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo;
    jlong ctx = ctxProperties->context;
    
    GLint *start;

    jint coordoff, coloroff, normoff;
    jint* initialVAttrIndices;
    jint* vAttrSizes;
    int texSet;
    jint *texCoordSetMap;
    jint* initialTexIndices;

    jboolean floatCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_FLOAT) != 0);
    jboolean doubleCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_DOUBLE) != 0);
    jboolean floatColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_FLOAT) != 0);
    jboolean byteColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_BYTE) != 0);
    jboolean normalsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_NORMAL_FLOAT) != 0);
    jboolean vattrDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_VATTR_FLOAT) != 0);
    jboolean textureDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_TEXCOORD_FLOAT) != 0);

    table = *env;    

#ifdef VERBOSE
    fprintf(stderr, "executeGeometryArrayVA()\n");
#endif /* VERBOSE */

    /* Enable normalize for non-uniform scale (which rescale can't handle) */
    if (ctxProperties->rescale_normal_ext && isNonUniformScale) {
	glEnable(GL_NORMALIZE);
    }

    coordoff = 3 * initialCoordIndex;
    /* Define the data pointers */
    if (floatCoordDefined) {
	glVertexPointer(3, GL_FLOAT, 0, &(fverts[coordoff]));
    } else if (doubleCoordDefined){
	glVertexPointer(3, GL_DOUBLE, 0, &(dverts[coordoff]));
    }
    
    if (floatColorsDefined) {
	if (vformat & GA_WITH_ALPHA) {
	    coloroff = 4 * initialColorIndex;
	    glColorPointer(4, GL_FLOAT, 0, &(fclrs[coloroff]));
	} else {
	    coloroff = 3 * initialColorIndex;
	    glColorPointer(3, GL_FLOAT, 0, &(fclrs[coloroff]));
	}
    } else if (byteColorsDefined) {
	if (vformat & GA_WITH_ALPHA) {
	    coloroff = 4 * initialColorIndex;
	    glColorPointer(4, GL_UNSIGNED_BYTE, 0, &(bclrs[coloroff]));
	} else {
	    coloroff = 3 * initialColorIndex;
	    glColorPointer(3, GL_UNSIGNED_BYTE, 0, &(bclrs[coloroff]));
	}
    }
    if (normalsDefined) {
	normoff = 3 * initialNormalIndex;
	glNormalPointer(GL_FLOAT, 0, &(norms[normoff]));
    }

    if (vattrDefined) {
	float *pVertexAttrs;
	int sz, initIdx;

	vAttrSizes = (jint *) (*(table->GetPrimitiveArrayCritical))(env, vertexAttrSizes, NULL);
	initialVAttrIndices = (jint *) (*(table->GetPrimitiveArrayCritical))(env, vertexAttrIndices, NULL);

	for (i = 0; i < vertexAttrCount; i++) {
	    pVertexAttrs = vertexAttrPointer[i];
	    sz = vAttrSizes[i];
	    initIdx = initialVAttrIndices[i];

	    ctxProperties->enableVertexAttrArray(ctxProperties, i);
	    ctxProperties->vertexAttrPointer(ctxProperties, i, sz,
					     GL_FLOAT, 0,
					     &pVertexAttrs[initIdx * sz]);
	}

	(*(table->ReleasePrimitiveArrayCritical))(env, vertexAttrSizes, vAttrSizes, 0);
	(*(table->ReleasePrimitiveArrayCritical))(env, vertexAttrIndices, initialVAttrIndices, 0);
    }

    if (textureDefined) {

	int tus = 0;
	float *ptexCoords;

	initialTexIndices = (jint *) (*(table->GetPrimitiveArrayCritical))(env,texindices, NULL);

	texCoordSetMap = (jint *) (*(table->GetPrimitiveArrayCritical))(env,tcoordsetmap, NULL);
	if (pass < 0) {
	    for (i = 0; i < numActiveTexUnit; i++) {
		tus = texUnitStateMap[i];
		if ((tus < texCoordMapLength) && (
			((texSet=texCoordSetMap[tus]) != -1))) {
		
		    ptexCoords = texCoordPointer[texSet];

		    enableTexCoordPointer(ctxProperties, i, texStride,
			GL_FLOAT, 0, 
			&ptexCoords[texStride * initialTexIndices[texSet]]);
			
		} else {
		    disableTexCoordPointer(ctxProperties, i);
		}
	    }
	}
	else {
	    texUnitStateMap = NULL;
	    texSet = texCoordSetMap[pass];
	    if (texSet != -1) {
		ptexCoords = texCoordPointer[texSet];
		enableTexCoordPointer(ctxProperties, 0, texStride,
			GL_FLOAT, 0, 
			&ptexCoords[texStride * initialTexIndices[texSet]]);

		/*
		 * in a non-multitexturing case, only the first texture
		 * unit is used, it will be the core library responsibility
		 * to disable all texture units before enabling "the"
		 * texture unit for multi-pass purpose
		 */
	    } else {
		disableTexCoordPointer(ctxProperties, 0);
	    }
	}
        /* Reset client active texture unit to 0 */
        clientActiveTextureUnit(ctxProperties, 0);
    }

    if (geo_type == GEO_TYPE_TRI_STRIP_SET || 
	geo_type == GEO_TYPE_TRI_FAN_SET   || 
	geo_type == GEO_TYPE_LINE_STRIP_SET) {
	
	strips = (GLint *) (*(table->GetPrimitiveArrayCritical))(env, sarray, 
			NULL);

	switch (geo_type) {
	  case GEO_TYPE_TRI_STRIP_SET :
	    primType = GL_TRIANGLE_STRIP;
	    break;
	  case GEO_TYPE_TRI_FAN_SET :
	    primType = GL_TRIANGLE_FAN;
	    break;
	  case GEO_TYPE_LINE_STRIP_SET :
	    primType = GL_LINE_STRIP;
	    break;
	}

	start = (GLint *)(*(table->GetPrimitiveArrayCritical))(env, 
							       start_array, NULL);
	if (ctxProperties->multi_draw_arrays_ext || ctxProperties->multi_draw_arrays_sun) {

	    /*
	    fprintf(stderr, "strip_len =  %d \n",strip_len);
	    for (i=0; i < strip_len;i++) {
		fprintf(stderr, "numVertices = %d\n",strips[i]);
		fprintf(stderr, "start =  %d \n",start[i]);
	    }
	    */
	    ctxProperties->glMultiDrawArraysEXT(primType, start, strips, strip_len);
	}   else {
	    for (i=0; i < strip_len;i++) {
		glDrawArrays(primType, start[i], strips[i]);
	    }
	}
	(*(table->ReleasePrimitiveArrayCritical))(env, start_array, start,
			0);
	(*(table->ReleasePrimitiveArrayCritical))(env, sarray, strips, 0); 
    }
    else {
       switch (geo_type){
       case GEO_TYPE_QUAD_SET : glDrawArrays(GL_QUADS, 0, vcount);break;
       case GEO_TYPE_TRI_SET : glDrawArrays(GL_TRIANGLES, 0, vcount);break;
       case GEO_TYPE_POINT_SET : glDrawArrays(GL_POINTS, 0, vcount);break;
       case GEO_TYPE_LINE_SET: glDrawArrays(GL_LINES, 0, vcount);break;
       }
   }
    /* clean up if we turned on normalize */
    if (ctxProperties->rescale_normal_ext && isNonUniformScale) {
	glDisable(GL_NORMALIZE);
    }


    if (vattrDefined) {
	resetVertexAttrs(ctxInfo, vertexAttrCount);
    }

    if (textureDefined) {
	resetTexture(ctxInfo);

	(*(table->ReleasePrimitiveArrayCritical))(env, tcoordsetmap, texCoordSetMap, 0);
	(*(table->ReleasePrimitiveArrayCritical))(env, texindices, initialTexIndices, 0);
    }    
}

/* execute geometry array with java array format */
/*
 * Class:     javax_media_j3d_GeometryArrayRetained
 * Method:    executeVA
 * Signature: (JLjavax/media/j3d/GeometryArrayRetained;IZZZIIII[F[DI[F[BI[FI[I[I[[FII[II[I[II[Ljava/lang/Object;I)V
 */
JNIEXPORT void JNICALL
Java_javax_media_j3d_GeometryArrayRetained_executeVA(
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

    jfieldID strip_field;
    jarray sarray;
    jsize strip_len;
    jclass geo_class;
 
    jarray start_array;
    jfieldID start_field;
    
    JNIEnv table = *env;
    jfloat *fverts = NULL;
    jdouble *dverts = NULL;
    jbyte *bclrs = NULL;
    jfloat *fclrs = NULL, *norms = NULL;
    jarray *vaobjs = NULL;
    jfloat **vertexAttrPointer = NULL;
    jfloat **texCoordPointer = NULL;
    jarray *texobjs = NULL;
    jint* texUnitStateMap = NULL;
    int i;

    jboolean floatCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_FLOAT) != 0);
    jboolean doubleCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_DOUBLE) != 0);
    jboolean floatColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_FLOAT) != 0);
    jboolean byteColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_BYTE) != 0);
    jboolean normalsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_NORMAL_FLOAT) != 0);
    jboolean vattrDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_VATTR_FLOAT) != 0);
    jboolean textureDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_TEXCOORD_FLOAT) != 0);

    if (vattrDefined) {
	vaobjs = (jarray *)malloc(vertexAttrCount * sizeof(jarray));
	vertexAttrPointer = (jfloat **)malloc(vertexAttrCount * sizeof(jfloat *));

	for (i = 0; i < vertexAttrCount; i++) {
	    vaobjs[i] = (*(table->GetObjectArrayElement))(env, vertexAttrData, i);
	}
    }

    if (textureDefined) {
	texobjs = (jarray*)malloc(texCoordMapLength * sizeof(jarray));
	texCoordPointer = (jfloat**)malloc(texCoordMapLength * sizeof(jfloat*));

	for (i = 0; i < texCoordMapLength; i++) {
	    texobjs[i] = (*(table->GetObjectArrayElement))(env, texCoords, i);
	}
    }

    geo_class =  (jclass) (*(table->GetObjectClass))(env, geo);

    if (geo_type == GEO_TYPE_TRI_STRIP_SET || 
	geo_type == GEO_TYPE_TRI_FAN_SET   || 
	geo_type == GEO_TYPE_LINE_STRIP_SET) {

        strip_field = (jfieldID) (*(table->GetFieldID))(env, geo_class,
			"stripVertexCounts", "[I");
        sarray = (jarray)(*(table->GetObjectField))(env, geo, strip_field);
	strip_len = (jsize)(*(table->GetArrayLength))(env, sarray);

	start_field = (jfieldID) (*(table->GetFieldID))(env, geo_class,
							"stripStartOffsetIndices", "[I");
	start_array = (jarray)(*(table->GetObjectField))(env, geo,
							 start_field);
    }

    /* Get vertex attribute arrays */
    if (vattrDefined) {
	for (i = 0; i < vertexAttrCount; i++) {
	    vertexAttrPointer[i] = (jfloat *) (*(table->GetPrimitiveArrayCritical))(env, vaobjs[i], NULL);
	}
    }

    /* get texture arrays */
    if (textureDefined) {
	for (i = 0; i < texCoordMapLength; i++) {
	    if (texobjs[i] != NULL)
		texCoordPointer[i] = (jfloat*)(*(table->GetPrimitiveArrayCritical))(env,texobjs[i], NULL);
	    else
		texCoordPointer[i] = NULL;	
	}
	if (pass < 0) {
	    texUnitStateMap = (jint *) (*(table->GetPrimitiveArrayCritical))(env,tunitstatemap, NULL);	
	}
    }
    
    /* get coordinate array */
    if (floatCoordDefined) {
	fverts= (jfloat *) (*(table->GetPrimitiveArrayCritical))(env, vfcoords, NULL);
    } else if (doubleCoordDefined) {
	dverts= (jdouble *) (*(table->GetPrimitiveArrayCritical))(env, vdcoords, NULL);	
    }
    
    /* get color array */
    if (floatColorsDefined) {
	fclrs = (jfloat *) (*(table->GetPrimitiveArrayCritical))(env, cfdata, NULL);
    } else if (byteColorsDefined) {
	bclrs = (jbyte *) (*(table->GetPrimitiveArrayCritical))(env, cbdata, NULL);
    }

    /* get normal array */
    if (normalsDefined) {
	norms = (jfloat *) (*(table->GetPrimitiveArrayCritical))(env,ndata, NULL);
    }

#ifdef VERBOSE
    fprintf(stderr, "GeometryArrayRetained.executeVA() -- calling executeGeometryArrayVA\n");
#endif /* VERBOSE */

    executeGeometryArrayVA(env, obj, ctxInfo, geo, geo_type,
			   isNonUniformScale,  multiScreen, ignoreVertexColors,
			   vcount, vformat,  vdefined, initialCoordIndex,
			   fverts, dverts, initialColorIndex,
			   fclrs, bclrs, initialNormalIndex,
			   norms,
			   vertexAttrCount, vertexAttrSizes,
			   vertexAttrIndices, vertexAttrPointer,
			   pass, texCoordMapLength,
			   tcoordsetmap,numActiveTexUnit, texUnitStateMap,
			   texindices,texStride,texCoordPointer,cdirty, sarray, strip_len, start_array);

    
    if (vattrDefined) {
	for (i = 0; i < vertexAttrCount; i++) {
	    (*(table->ReleasePrimitiveArrayCritical))(env, vaobjs[i], vertexAttrPointer[i], 0);
	}
    }

    if (vaobjs != NULL) {
	free(vaobjs);
    }
    if (vertexAttrPointer != NULL) {
	free(vertexAttrPointer);
    }

    if (textureDefined) {
	for (i = 0; i < texCoordMapLength; i++) {
	    if (texCoordPointer[i] != NULL) {
		(*(table->ReleasePrimitiveArrayCritical))(env, texobjs[i], texCoordPointer[i], 0);
	    }
	}
	if (texUnitStateMap != NULL) {
	    (*(table->ReleasePrimitiveArrayCritical))(env, tunitstatemap, texUnitStateMap, 0);
	}
    }
    
    if (texobjs != NULL) {
	free(texobjs);
    }
    if (texCoordPointer != NULL) {
	free(texCoordPointer);
    }
   
    if (normalsDefined) {
	(*env)->ReleasePrimitiveArrayCritical(env, ndata, norms, 0);
    }

    
    if (floatColorsDefined) {
	(*env)->ReleasePrimitiveArrayCritical(env, cfdata, fclrs, 0); 
    }
    else if (byteColorsDefined) {
	(*env)->ReleasePrimitiveArrayCritical(env, cbdata, bclrs, 0);
    }
    
   
    if (floatCoordDefined) {
	(*env)->ReleasePrimitiveArrayCritical(env, vfcoords, fverts, 0); 
    }
    else if (doubleCoordDefined) {
	(*env)->ReleasePrimitiveArrayCritical(env, vdcoords, dverts, 0); 
    }
}
      
/* execute geometry array with java array format */
/*
 * Class:     javax_media_j3d_GeometryArrayRetained
 * Method:    executeVABuffer
 * Signature: (JLjavax/media/j3d/GeometryArrayRetained;IZZZIIIILjava/lang/Object;ILjava/lang/Object;[F[BILjava/lang/Object;I[I[I[Ljava/lang/Object;II[II[I[II[Ljava/lang/Object;I)V
 */
JNIEXPORT void JNICALL Java_javax_media_j3d_GeometryArrayRetained_executeVABuffer(
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
    jfieldID strip_field;
    jarray sarray;
    jsize strip_len;
    jclass geo_class;
 
    jarray start_array;
    jfieldID start_field;

    JNIEnv table = *env;
    jfloat *fverts = NULL;
    jdouble *dverts = NULL ;
    jbyte *bclrs = NULL;
    jfloat *fclrs = NULL, *norms = NULL;
    jarray *vaobjs = NULL;
    jfloat **vertexAttrPointer = NULL;
    jfloat **texCoordPointer = NULL;
    jarray *texobjs = NULL;
    jint* texUnitStateMap = NULL;
    int i;
    
    jboolean floatCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_FLOAT) != 0);
    jboolean doubleCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_DOUBLE) != 0);
    jboolean floatColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_FLOAT) != 0);
    jboolean byteColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_BYTE) != 0);
    jboolean normalsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_NORMAL_FLOAT) != 0);
    jboolean vattrDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_VATTR_FLOAT) != 0);
    jboolean textureDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_TEXCOORD_FLOAT) != 0);

    if (vattrDefined) {
	vaobjs = (jarray *)malloc(vertexAttrCount * sizeof(jarray));
	vertexAttrPointer = (jfloat **)malloc(vertexAttrCount * sizeof(jfloat *));

	for (i = 0; i < vertexAttrCount; i++) {
	    vaobjs[i] = (*(table->GetObjectArrayElement))(env, vertexAttrData, i);
	}
    }

    if (textureDefined) {
	texobjs = (jarray*)malloc(texCoordMapLength * sizeof(jarray));
	texCoordPointer = (jfloat**)malloc(texCoordMapLength * sizeof(jfloat*));

	for (i = 0; i < texCoordMapLength; i++) {
	    texobjs[i] = (*(table->GetObjectArrayElement))(env, texCoords, i);
	}
    }
    geo_class =  (jclass) (*(table->GetObjectClass))(env, geo);
    
    if (geo_type == GEO_TYPE_TRI_STRIP_SET || 
	geo_type == GEO_TYPE_TRI_FAN_SET   || 
	geo_type == GEO_TYPE_LINE_STRIP_SET) {

        strip_field = (jfieldID) (*(table->GetFieldID))(env, geo_class,
			"stripVertexCounts", "[I");
        sarray = (jarray)(*(table->GetObjectField))(env, geo, strip_field);
	strip_len = (jsize)(*(table->GetArrayLength))(env, sarray);

	start_field = (jfieldID) (*(table->GetFieldID))(env, geo_class,
							"stripStartOffsetIndices", "[I");
	start_array = (jarray)(*(table->GetObjectField))(env, geo,
							 start_field);
    }
    
    /* get coordinate array */
    if (floatCoordDefined) {
	fverts= (jfloat *)(*(table->GetDirectBufferAddress))(env, vcoords );
    } else if (doubleCoordDefined) {
	dverts= (jdouble *)(*(table->GetDirectBufferAddress))(env, vcoords ); 
    }
    
    if(fverts == NULL && dverts == NULL) {
	return;
    }
    

    /* get color array */
    if (floatColorsDefined) {
	if(cfdata != NULL)
	    fclrs = (jfloat *) (*(table->GetPrimitiveArrayCritical))(env, cfdata, NULL);
	else
	    fclrs = (jfloat *)(*(table->GetDirectBufferAddress))(env, cdataBuffer);
    }
    else if (byteColorsDefined) {
	if(cbdata != NULL)
	    bclrs = (jbyte *) (*(table->GetPrimitiveArrayCritical))(env, cbdata, NULL);
	else
	    bclrs = (jbyte *)(*(table->GetDirectBufferAddress))(env, cdataBuffer);
    }

    /* get normal array */
    if (normalsDefined) {
	norms = (jfloat *)(*(table->GetDirectBufferAddress))(env, ndata);
    }

    /* get vertex attr arrays */
    if (vattrDefined) {
	for (i = 0; i < vertexAttrCount; i++) {
	    vertexAttrPointer[i] = (jfloat *) (*(table->GetDirectBufferAddress))(env, vaobjs[i]);
	}
    }

    /* get texture arrays */
    if (textureDefined) {
	for (i = 0; i < texCoordMapLength; i++) {
	    if (texobjs[i] != NULL)
		texCoordPointer[i] = (jfloat*)(*(table->GetDirectBufferAddress))(env,texobjs[i]);
	    else
		texCoordPointer[i] = NULL;	
	}
	if (pass < 0) {
	    texUnitStateMap = (jint *) (*(table->GetPrimitiveArrayCritical))(env,tunitstatemap, NULL);	
	}	
    }


#ifdef VERBOSE
    fprintf(stderr, "GeometryArrayRetained.executeVABuffer() -- calling executeGeometryArrayVA\n");
#endif /* VERBOSE */

    executeGeometryArrayVA(env, obj, ctxInfo, geo, geo_type,
			   isNonUniformScale, multiScreen, ignoreVertexColors,
			   vcount, vformat,  vdefined, initialCoordIndex,
			   fverts, dverts, initialColorIndex,
			   fclrs, bclrs, initialNormalIndex,
			   norms,
			   vertexAttrCount, vertexAttrSizes,
			   vertexAttrIndices, vertexAttrPointer,
			   pass, texCoordMapLength,
			   tcoordsetmap,numActiveTexUnit, texUnitStateMap,
			   texindices,texStride,texCoordPointer,cdirty, sarray, strip_len, start_array);

    if (vaobjs != NULL) {
	free(vaobjs);
    }
    if (vertexAttrPointer != NULL) {
	free(vertexAttrPointer);
    }

    if (textureDefined) {
    	if (texUnitStateMap != NULL) {
	    (*(table->ReleasePrimitiveArrayCritical))(env, tunitstatemap, texUnitStateMap, 0);
	}
    }
    
    if (texobjs != NULL) {
	free(texobjs);
    }
    if (texCoordPointer != NULL) {
	free(texCoordPointer);
    }

    if(floatColorsDefined && cfdata != NULL)
	(*(table->ReleasePrimitiveArrayCritical))(env, cfdata, fclrs, 0);
    else if(byteColorsDefined && cbdata != NULL)
	(*(table->ReleasePrimitiveArrayCritical))(env, cbdata, bclrs, 0);
}



JNIEXPORT
void JNICALL Java_javax_media_j3d_GeometryArrayRetained_disableGlobalAlpha(
    JNIEnv *env, 
    jobject obj,
    jlong ctxInfo,    
    jint vformat,
    jboolean useAlpha,
    jboolean ignoreVertexColors)
{
    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo;
    jlong ctx = ctxProperties->context;
    
    if(ctxProperties->global_alpha_sun){
	if (ignoreVertexColors == JNI_FALSE && vformat & 0x04) {
	    if (useAlpha && ctxProperties->global_alpha_sun ) {
		glDisable(GL_GLOBAL_ALPHA_SUN);
	    }
	}
    }
}


/*
 * Class:     javax_media_j3d_GeometryArrayRetained
 * Method:    setVertexFormat
 * Signature: (JIZZI[I)V
 */
JNIEXPORT
void JNICALL Java_javax_media_j3d_GeometryArrayRetained_setVertexFormat(
    JNIEnv *env, 
    jobject obj,
    jlong ctxInfo,
    jint vformat,
    jboolean useAlpha,
    jboolean ignoreVertexColors)
{
    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo;
    jlong ctx = ctxProperties->context;

#ifdef VERBOSE
    fprintf(stderr,
	    "GeometryArrayRetained.setVertexFormat() : vformat = %d\n",
	    vformat);
#endif /* VERBOSE */

    /* Enable and disable the appropriate pointers */
    if (vformat & GA_NORMALS) {
	glEnableClientState(GL_NORMAL_ARRAY);
    }
    else {
	glDisableClientState(GL_NORMAL_ARRAY);
    }
    if (ignoreVertexColors == JNI_FALSE && vformat & GA_COLOR) {
	glEnableClientState(GL_COLOR_ARRAY);
    }
    else {
	glDisableClientState(GL_COLOR_ARRAY);

    }

    if (ctxProperties->global_alpha_sun) {
	if (useAlpha) {
	    glEnable(GL_GLOBAL_ALPHA_SUN);
	}
	else {
	    glDisable(GL_GLOBAL_ALPHA_SUN);
	}
    }

    if (vformat & GA_COORDINATES) {
	glEnableClientState(GL_VERTEX_ARRAY);
    }
    else {
	glDisableClientState(GL_VERTEX_ARRAY);
    }
}

JNIEXPORT jboolean JNICALL
Java_javax_media_j3d_GeometryArrayRetained_globalAlphaSUN(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo)
{
    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo;
    jlong ctx = ctxProperties->context;

    if (ctxProperties->global_alpha_sun == 1)
	return JNI_TRUE ;
    else
	return JNI_FALSE ;
}


static void
executeIndexedGeometryArray(
    JNIEnv *env,
    jobject obj, jlong ctxInfo, jobject geo, jint geo_type,
    jboolean isNonUniformScale, jboolean useAlpha,
    jboolean multiScreen,
    jboolean ignoreVertexColors,
    jint initialIndexIndex,
    jint indexCount,
    jint vertexCount,
    jint vformat,
    jint vertexAttrCount, jintArray vertexAttrSizes,
    jint texCoordSetCount,
    jintArray texCoordSetMap, jint texCoordSetMapLen,
    jintArray texUnitOffset,
    jint numActiveTexUnit,
    jintArray texUnitStateMapArray,
    jfloatArray varray, jobject varrayBuffer, jfloatArray carray,
    jint texUnitIndex, jint cDirty,
    jintArray indexCoord)
{
    jclass geo_class;
    JNIEnv table;

    jfloat *verts,*clrs;
    jint *indices;
    jint  i;
	size_t bstride, cbstride;
    jsize strip_len;
    GLsizei *countArray;
    int offset;
    GLenum iaFormat;
    int useInterleavedArrays;
    int primType;
    jint stride, coordoff, normoff, coloroff, texCoordoff;
    int alphaNeedsUpdate = 0;    /* used so we can get alpha data from */
                                 /* JNI before using it so we can use  */
                                 /* GetPrimitiveArrayCritical */
    jfieldID strip_field;
    jarray sarray;
    jint* tmpDrawElementsIndices[100];

    jint** multiDrawElementsIndices = NULL;
    jint allocated = 0;

    jint texSize, texStride, *texCoordSetMapOffset = NULL, 
				*texUnitStateMap = NULL;

    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo;
    jlong ctx = ctxProperties->context;

    int cstride = 0;
    int vAttrStride = 0;
    int vAttrOff;
    jint *vAttrSizesPtr = NULL;

    table = *env;

#ifdef VERBOSE
    fprintf(stderr,
	    "executeIndexedGeometryArray: vertexAttrCount = %d\n",
	    vertexAttrCount);
#endif /* VERBOSE */

    geo_class =  (jclass) (*(table->GetObjectClass))(env, geo);

    /* This matches the code in GeometryArrayRetained.java */
    stride = coordoff = normoff = coloroff = texCoordoff = 0;
    vAttrOff = 0;
    if ((vformat & GA_COORDINATES) != 0) {
	stride += 3;
    }
    if ((vformat & GA_NORMALS) != 0) {
	stride += 3;
	coordoff += 3;
    }
    if ((vformat & GA_COLOR) != 0) {
	if ((vformat & GA_WITH_ALPHA) != 0 ) {
	    stride += 4;
	    normoff += 4;
	    coordoff += 4;
	}
	else { /* Handle the case of executeInterleaved 3f */
	    stride += 3;
	    normoff += 3;
	    coordoff += 3;
	}
    }
    if ((vformat & GA_TEXTURE_COORDINATE) != 0) {
        if ((vformat & GA_TEXTURE_COORDINATE_2) != 0) {
	    texSize = 2;
	    texStride = 2 * texCoordSetCount;
        } else if ((vformat & GA_TEXTURE_COORDINATE_3) != 0) {
	    texSize = 3;
	    texStride = 3 * texCoordSetCount;
        } else if ((vformat & GA_TEXTURE_COORDINATE_4) != 0) {
	    texSize = 4;
	    texStride = 4 * texCoordSetCount;
	}
	stride += texStride;
	normoff += texStride;
	coloroff += texStride;
	coordoff += texStride;
    }

    if ((vformat & GA_VERTEX_ATTRIBUTES) != 0) {
	if (vertexAttrSizes != NULL) {
	    vAttrSizesPtr = table->GetIntArrayElements(env, vertexAttrSizes, NULL);
	}
	for (i = 0; i < vertexAttrCount; i++) {
	    vAttrStride += vAttrSizesPtr[i];
	}
	stride += vAttrStride;
	normoff += vAttrStride;
	coloroff += vAttrStride;
	coordoff += vAttrStride;
	texCoordoff += vAttrStride;
    }

    bstride = stride*sizeof(float);

    /*
     * call other JNI functions before entering Critical region
     * i.e., GetPrimitiveArrayCritical
     */

    
    if (geo_type == GEO_TYPE_INDEXED_TRI_STRIP_SET || 
	geo_type == GEO_TYPE_INDEXED_TRI_FAN_SET   || 
	geo_type == GEO_TYPE_INDEXED_LINE_STRIP_SET) {

        strip_field = (jfieldID) (*(table->GetFieldID))(env, geo_class,
			"stripIndexCounts", "[I");
        sarray = (jarray)(*(table->GetObjectField))(env, geo, strip_field);
	strip_len = (jsize)(*(table->GetArrayLength))(env, sarray);


    }

    /* begin critical region */
    if(varray != NULL) 
	verts = (jfloat *) (*(table->GetPrimitiveArrayCritical))(env, varray, NULL);
    else if(varrayBuffer != NULL)
	verts = (jfloat *) (*(table->GetDirectBufferAddress))(env, varrayBuffer);

    indices = (jint *) (*(table->GetPrimitiveArrayCritical))(env, indexCoord, NULL);


    /* using byRef interleaved array and has a separate pointer, then .. */
    cstride = stride;
    if (carray != NULL) { 
        clrs = (jfloat *) (*(table->GetPrimitiveArrayCritical))(env, carray, NULL);
	cstride = 4;

    }
    else {
        clrs = &(verts[coloroff]);
    }
    cbstride = cstride * sizeof(float);
    if (texCoordSetMapLen >0) {
        texCoordSetMapOffset = (jint *) (*(table->GetPrimitiveArrayCritical))(env, texUnitOffset, NULL);
    }

    if (texUnitStateMapArray != NULL) {
        texUnitStateMap = (jint *) (*(table->GetPrimitiveArrayCritical))(env, texUnitStateMapArray, NULL);
    }

    /* Enable normalize for non-uniform scale (which rescale can't handle) */
    if (ctxProperties->rescale_normal_ext && isNonUniformScale) {
	glEnable(GL_NORMALIZE);
    }
    
    /*** Handle non-indexed strip GeometryArray first *******/
    if (geo_type == GEO_TYPE_INDEXED_TRI_STRIP_SET || 
	geo_type == GEO_TYPE_INDEXED_TRI_FAN_SET   || 
	geo_type == GEO_TYPE_INDEXED_LINE_STRIP_SET) {


	countArray = (GLsizei *) (*(table->GetPrimitiveArrayCritical))(env, sarray, 
			NULL);

	if ((ignoreVertexColors == JNI_TRUE) || (carray != NULL) || 
	    ((vformat & GA_TEXTURE_COORDINATE) && ((texCoordSetMapLen > 1) ||
		(texCoordSetCount > 1)))) {
	    useInterleavedArrays = 0;
	} else {
	    INTERLEAVEDARRAYS_TEST()
	}
	if (useInterleavedArrays) {
	    glInterleavedArrays(iaFormat, bstride, verts);
	} else {
	    if (vformat & GA_NORMALS) {
		glNormalPointer(GL_FLOAT, bstride, &(verts[normoff]));
	    }
	    if (ignoreVertexColors == JNI_FALSE && vformat & GA_COLOR) {
		if (vformat & GA_WITH_ALPHA || (useAlpha == GL_TRUE)) {
		    glColorPointer(4, GL_FLOAT, cbstride, clrs);
		} else {
		    /*
		    for (i = 0; i < 4; i++) {
			fprintf(stderr, "r = %f, g = %f, b = %f\n", verts[i*stride +coloroff], verts[i*stride +coloroff+1],verts[i*stride +coloroff+2]);
		    }
		    */
		    glColorPointer(3, GL_FLOAT, cbstride, clrs);
		}
	    }
	    if (vformat & GA_COORDINATES) {
		/*
		    for (i = 0; i < 4; i++) {
			fprintf(stderr, "x = %f, y = %f, z = %f\n", verts[i*stride +coordoff], verts[i*stride +coordoff+1],verts[i*stride +coordoff+2]);
		    }
		    */
	        glVertexPointer(3, GL_FLOAT, bstride, &(verts[coordoff]));
	    }

	    if (vformat & GA_TEXTURE_COORDINATE) {

		/* XXXX: texCoordoff == 0 ???*/
                executeTexture(texUnitIndex, texCoordSetMapLen,
                                texSize, bstride, texCoordoff,
                                texCoordSetMapOffset, 
				numActiveTexUnit, texUnitStateMap, 
				verts, ctxInfo);
	    } 

	    if (vformat & GA_VERTEX_ATTRIBUTES) {
		jfloat *vAttrPtr = &verts[vAttrOff];

		for (i = 0; i < vertexAttrCount; i++) {
		    ctxProperties->enableVertexAttrArray(ctxProperties, i);
		    ctxProperties->vertexAttrPointer(ctxProperties, i, vAttrSizesPtr[i],
						     GL_FLOAT, bstride, vAttrPtr);
		    vAttrPtr += vAttrSizesPtr[i];
		}
	    }
	}  

	switch (geo_type) {
	  case GEO_TYPE_INDEXED_TRI_STRIP_SET :
	    primType = GL_TRIANGLE_STRIP;
	    break;
	  case GEO_TYPE_INDEXED_TRI_FAN_SET :
	    primType = GL_TRIANGLE_FAN;
	    break;
	  case GEO_TYPE_INDEXED_LINE_STRIP_SET :
	    primType = GL_LINE_STRIP;
	    break;
	}
	/*
	fprintf(stderr, "strip_len = %d\n",strip_len);
	for (i=0; i < strip_len;i++) {
	    fprintf(stderr, "strips[i] = %d\n",strips[i]);
	}
	*/

	lockArray(ctxProperties, vertexCount);
	
	if (ctxProperties->multi_draw_arrays_ext || ctxProperties->multi_draw_arrays_sun) {
	    if (strip_len >  100) {
		multiDrawElementsIndices = (jint**)malloc(strip_len * sizeof(int*));
		allocated  = 1;
	    }
	    else {
		multiDrawElementsIndices =(jint**) &tmpDrawElementsIndices;
	    }
		
	    offset = initialIndexIndex;
	    for (i=0; i < strip_len;i++) {
		multiDrawElementsIndices[i] = &indices[offset];
		offset += countArray[i];
	    }
	    ctxProperties->glMultiDrawElementsEXT(primType, countArray, GL_UNSIGNED_INT,(const void **)multiDrawElementsIndices, strip_len);

	} else {
	    offset = initialIndexIndex;
	    for (i=0; i < strip_len;i++) {

		glDrawElements(primType,  countArray[i], GL_UNSIGNED_INT, &indices[offset]);
		offset += countArray[i];
	    }
	}
	(*(table->ReleasePrimitiveArrayCritical))(env, sarray, countArray, 0);
	if (allocated) {
	    free(multiDrawElementsIndices);
	}

    }
    /******* Handle non-indexed non-striped GeometryArray now *****/
    else if ((geo_type == GEO_TYPE_INDEXED_QUAD_SET) ||
	     (geo_type == GEO_TYPE_INDEXED_TRI_SET) ||
	     (geo_type == GEO_TYPE_INDEXED_POINT_SET) ||
	     (geo_type == GEO_TYPE_INDEXED_LINE_SET))
    {
	if ((ignoreVertexColors == JNI_TRUE) || (carray != NULL) ||
	    ((vformat & GA_TEXTURE_COORDINATE) && ((texCoordSetMapLen > 1) ||
		(texCoordSetCount > 1)))) {
	    useInterleavedArrays = 0;
	} else {
	    INTERLEAVEDARRAYS_TEST()
	}
	if (useInterleavedArrays) {
	    glInterleavedArrays(iaFormat, bstride, verts);
	} else {
	    if (vformat & GA_NORMALS) {
		glNormalPointer(GL_FLOAT, bstride, &(verts[normoff]));
	    } 
	    if (ignoreVertexColors == JNI_FALSE && vformat & GA_COLOR) {
		if (vformat & GA_WITH_ALPHA || (useAlpha == GL_TRUE)) {

		    glColorPointer(4, GL_FLOAT, cbstride, clrs);
		} else {
		    glColorPointer(3, GL_FLOAT, cbstride, clrs);
		}
	    }
	    if (vformat & GA_COORDINATES) {
		glVertexPointer(3, GL_FLOAT, bstride, &(verts[coordoff]));
	    } 

	    if (vformat & GA_TEXTURE_COORDINATE) {

		/* XXXX: texCoordoff == 0 ???*/
                executeTexture(texUnitIndex, texCoordSetMapLen,
			       texSize, bstride, texCoordoff,
			       texCoordSetMapOffset,
			       numActiveTexUnit, texUnitStateMap,
			       verts, ctxInfo);
	    }

	    if (vformat & GA_VERTEX_ATTRIBUTES) {
		jfloat *vAttrPtr = &verts[vAttrOff];

		for (i = 0; i < vertexAttrCount; i++) {
		    ctxProperties->enableVertexAttrArray(ctxProperties, i);
		    ctxProperties->vertexAttrPointer(ctxProperties, i, vAttrSizesPtr[i],
						     GL_FLOAT, bstride, vAttrPtr);
		    vAttrPtr += vAttrSizesPtr[i];
		}
	    }
	}

	lockArray(ctxProperties, vertexCount);	  
	
	switch (geo_type){
	  case GEO_TYPE_INDEXED_QUAD_SET : glDrawElements(GL_QUADS,indexCount, GL_UNSIGNED_INT, &indices[initialIndexIndex]);break;
	  case GEO_TYPE_INDEXED_TRI_SET : glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, &indices[initialIndexIndex]);break;
	  case GEO_TYPE_INDEXED_POINT_SET : glDrawElements(GL_POINTS, indexCount, GL_UNSIGNED_INT, &indices[initialIndexIndex]);break;
	  case GEO_TYPE_INDEXED_LINE_SET: glDrawElements(GL_LINES, indexCount, GL_UNSIGNED_INT, &indices[initialIndexIndex]);break;
	}
    }

    unlockArray(ctxProperties);

    if (vformat & GA_VERTEX_ATTRIBUTES) {
	resetVertexAttrs(ctxInfo, vertexAttrCount);
    }

    if (vformat & GA_TEXTURE_COORDINATE) {
	resetTexture(ctxInfo);
    }

    /* clean up if we turned on normalize */

    if (ctxProperties->rescale_normal_ext && isNonUniformScale) {
	glDisable(GL_NORMALIZE);
    }
    if(varray != NULL)
	(*(table->ReleasePrimitiveArrayCritical))(env, varray, verts, 0);

    (*(table->ReleasePrimitiveArrayCritical))(env, indexCoord, indices, 0);
    
    if (carray != NULL) 
	(*(table->ReleasePrimitiveArrayCritical))(env, carray, clrs, 0);

    if (texCoordSetMapLen > 0)
        (*(table->ReleasePrimitiveArrayCritical))(env, texUnitOffset, 
						texCoordSetMapOffset, 0);

    if (texUnitStateMap != NULL)
        (*(table->ReleasePrimitiveArrayCritical))(env, texUnitStateMapArray, 
						texUnitStateMap, 0);
    if (vAttrSizesPtr != NULL) {
	table->ReleaseIntArrayElements(env, vertexAttrSizes, vAttrSizesPtr, JNI_ABORT);
    }
}

JNIEXPORT void JNICALL
Java_javax_media_j3d_IndexedGeometryArrayRetained_executeIndexedGeometry(
    JNIEnv *env, 
    jobject obj, jlong ctxInfo, jobject geo, jint geo_type, 
    jboolean isNonUniformScale, jboolean useAlpha,
    jboolean multiScreen,
    jboolean ignoreVertexColors,
    jint initialIndexIndex,
    jint indexCount,
    jint vertexCount,
    jint vformat, 
    jint vertexAttrCount, jintArray vertexAttrSizes,
    jint texCoordSetCount,
    jintArray texCoordSetMap, jint texCoordSetMapLen,
    jintArray texUnitOffset, 
    jint numActiveTexUnit,
    jintArray texUnitStateMapArray,
    jfloatArray varray, jfloatArray carray,
    jint texUnitIndex, jint cDirty,
    jintArray indexCoord)
{

#ifdef VERBOSE
    fprintf(stderr, "IndexedGeometryArrayRetained.executeIndexedGeometry() -- calling executeIndexedGeometryArray\n");
#endif /* VERBOSE */

    executeIndexedGeometryArray(env, obj, ctxInfo, geo, geo_type,
				isNonUniformScale, useAlpha, multiScreen,
				ignoreVertexColors,
				initialIndexIndex,
				indexCount,
				vertexCount,
				vformat,
				vertexAttrCount, vertexAttrSizes,
				texCoordSetCount,
				texCoordSetMap, texCoordSetMapLen,
				texUnitOffset, 
				numActiveTexUnit,
				texUnitStateMapArray,
				varray, NULL, carray,
				texUnitIndex, cDirty,
				indexCoord);
}

JNIEXPORT void JNICALL
Java_javax_media_j3d_IndexedGeometryArrayRetained_executeIndexedGeometryBuffer(
    JNIEnv *env,
    jobject obj, jlong ctxInfo, jobject geo, jint geo_type,
    jboolean isNonUniformScale, jboolean useAlpha,
    jboolean multiScreen,
    jboolean ignoreVertexColors,
    jint initialIndexIndex,
    jint indexCount,
    jint vertexCount,
    jint vformat,
    jint texCoordSetCount,
    jintArray texCoordSetMap, jint texCoordSetMapLen,
    jintArray texUnitOffset,
    jint numActiveTexUnit,
    jintArray texUnitStateMapArray,
    jobject varray, jfloatArray carray,
    jint texUnitIndex, jint cDirty,
    jintArray indexCoord)
{

#ifdef VERBOSE
    fprintf(stderr, "IndexedGeometryArrayRetained.executeIndexedGeometryBuffer() -- calling executeIndexedGeometryArray\n");
#endif /* VERBOSE */

    executeIndexedGeometryArray(env, obj, ctxInfo, geo, geo_type,
			   isNonUniformScale, useAlpha, multiScreen,
			   ignoreVertexColors,
			   initialIndexIndex,
			   indexCount,
			   vertexCount,
			   vformat,
			   0, NULL,
			   texCoordSetCount,
			   texCoordSetMap, texCoordSetMapLen,
			   texUnitOffset, 
			   numActiveTexUnit,
			   texUnitStateMapArray,
			   NULL, varray, carray,
			   texUnitIndex, cDirty,
			   indexCoord);	   
}


static void
executeIndexedGeometryArrayVA(
    JNIEnv *env, 
    jobject obj,
    jlong ctxInfo,    
    jobject geo,
    jint geo_type, 
    jboolean isNonUniformScale,
    jboolean multiScreen,
    jboolean ignoreVertexColors,
    jint initialIndexIndex,
    jint validIndexCount,
    jint vertexCount,
    jint vformat,
    jint vdefined,
    jfloat* fverts,
    jdouble* dverts,
    jfloat* fclrs,
    jbyte*  bclrs,
    jfloat* norms,
    jint vertexAttrCount,
    jintArray vertexAttrSizes,
    jfloat ** vertexAttrPointer,
    jint pass,  
    jint texCoordMapLength,
    jintArray 	tcoordsetmap,
    jint numActiveTexUnit,
    jintArray tunitstatemap,
    jint texStride,
    jfloat** texCoordPointer,
    jint cdirty,
    jintArray indexCoord,
    jarray sarray,
    jsize strip_len)
{
    int primType;
    JNIEnv table;
    jint i;
    jint* tmpDrawElementsIndices[100];
    jint** multiDrawElementsIndices = NULL;
    jint allocated = 0;
    jint *indices;

    jboolean floatCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_FLOAT) != 0);
    jboolean doubleCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_DOUBLE) != 0);
    jboolean floatColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_FLOAT) != 0);
    jboolean byteColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_BYTE) != 0);
    jboolean normalsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_NORMAL_FLOAT) != 0);
    jboolean vattrDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_VATTR_FLOAT) != 0);
    jboolean textureDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_TEXCOORD_FLOAT) != 0);
  
    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo;
    jlong ctx = ctxProperties->context;
    
    int texSet;
    jint *texCoordSetMap, *texUnitStateMap;
    GLsizei *countArray;
    jint* vAttrSizes;
    jint offset = 0;

    table = *env;
    
#ifdef VERBOSE
    fprintf(stderr,
	    "executeIndexedGeometryArrayVA: vertexAttrCount = %d\n",
	    vertexAttrCount);
#endif /* VERBOSE */

    /* Enable normalize for non-uniform scale (which rescale can't handle) */
    if (ctxProperties->rescale_normal_ext && isNonUniformScale) {
	glEnable(GL_NORMALIZE);
    }

    /* Define the data pointers */
    if (floatCoordDefined) {
	glVertexPointer(3, GL_FLOAT, 0, fverts);
    } else if (doubleCoordDefined){
	glVertexPointer(3, GL_DOUBLE, 0, dverts);
    }
    if (floatColorsDefined) {
	if (vformat & GA_WITH_ALPHA) {
	    glColorPointer(4, GL_FLOAT, 0, fclrs);
	} else {
	    glColorPointer(3, GL_FLOAT, 0, fclrs);
	}
    } else if (byteColorsDefined) {
	if (vformat & GA_WITH_ALPHA) {
	    glColorPointer(4, GL_UNSIGNED_BYTE, 0, bclrs);
	} else {
	    glColorPointer(3, GL_UNSIGNED_BYTE, 0, bclrs);
	}
    }
    if (normalsDefined) {
	glNormalPointer(GL_FLOAT, 0, norms);
    }

    if (vattrDefined) {
	float *pVertexAttrs;
	int sz;

	vAttrSizes = (jint *) (*(table->GetPrimitiveArrayCritical))(env, vertexAttrSizes, NULL);

	for (i = 0; i < vertexAttrCount; i++) {
	    pVertexAttrs = vertexAttrPointer[i];
	    sz = vAttrSizes[i];

	    ctxProperties->enableVertexAttrArray(ctxProperties, i);
	    ctxProperties->vertexAttrPointer(ctxProperties, i, sz,
					     GL_FLOAT, 0,
					     pVertexAttrs);
	}

	(*(table->ReleasePrimitiveArrayCritical))(env, vertexAttrSizes, vAttrSizes, 0);
    }

    if (textureDefined) {

	int tus = 0;
	float *ptexCoords;
	
	texCoordSetMap = (jint *) (*(table->GetPrimitiveArrayCritical))(env,tcoordsetmap, NULL);
	if (pass < 0) {
	    texUnitStateMap = (jint *) (*(table->GetPrimitiveArrayCritical))(env,tunitstatemap, NULL);
	    for (i = 0; i < numActiveTexUnit; i++) {
		tus = texUnitStateMap[i];
		if ((tus < texCoordMapLength) && (
			((texSet=texCoordSetMap[tus]) != -1))) {
		
		    ptexCoords = texCoordPointer[texSet];

		    enableTexCoordPointer(ctxProperties, i, texStride,
			GL_FLOAT, 0, 
			ptexCoords);
			
		} else {

		    disableTexCoordPointer(ctxProperties, i);
		}
	    }
	}
	else {
	    texUnitStateMap = NULL;
	    texSet = texCoordSetMap[pass];
	    if (texSet != -1) {
		ptexCoords = texCoordPointer[texSet];
		enableTexCoordPointer(ctxProperties, 0, texStride,
			GL_FLOAT, 0, 
			ptexCoords);

		/*
		 * in a non-multitexturing case, only the first texture
		 * unit is used, it will be the core library responsibility
		 * to disable all texture units before enabling "the"
		 * texture unit for multi-pass purpose
		 */
	    }
	}
        /* Reset client active texture unit to 0 */
        clientActiveTextureUnit(ctxProperties, 0);
    }
    indices = (jint *) (*(table->GetPrimitiveArrayCritical))(env, indexCoord, NULL);

    lockArray(ctxProperties, vertexCount);
    
    if (geo_type == GEO_TYPE_INDEXED_TRI_STRIP_SET || 
	geo_type == GEO_TYPE_INDEXED_TRI_FAN_SET   || 
	geo_type == GEO_TYPE_INDEXED_LINE_STRIP_SET) {
	
	countArray = (GLint *) (*(table->GetPrimitiveArrayCritical))(env, sarray, 
			NULL);

	switch (geo_type) {
	  case GEO_TYPE_INDEXED_TRI_STRIP_SET :
	    primType = GL_TRIANGLE_STRIP;
	    break;
	  case GEO_TYPE_INDEXED_TRI_FAN_SET :
	    primType = GL_TRIANGLE_FAN;
	    break;
	  case GEO_TYPE_INDEXED_LINE_STRIP_SET :
	    primType = GL_LINE_STRIP;
	    break;
	}



	if (ctxProperties->multi_draw_arrays_ext || ctxProperties->multi_draw_arrays_sun) {
	    if (strip_len >  100) {
		multiDrawElementsIndices = (jint**)malloc(strip_len * sizeof(int*));
		allocated  = 1;
	    }
	    else {
		multiDrawElementsIndices = (jint**)&tmpDrawElementsIndices;
	    }
		
	    offset = initialIndexIndex;
	    for (i=0; i < strip_len;i++) {
		multiDrawElementsIndices[i] = &indices[offset];
		offset += countArray[i];
	    }
	    ctxProperties->glMultiDrawElementsEXT(primType, countArray, GL_UNSIGNED_INT,(const void **)multiDrawElementsIndices, strip_len);

	} else {
	    offset = initialIndexIndex;
	    for (i=0; i < strip_len;i++) {
		glDrawElements(primType,  countArray[i], GL_UNSIGNED_INT, &indices[offset]);
		offset += countArray[i];
	    }
	}

	(*(table->ReleasePrimitiveArrayCritical))(env, sarray, countArray, 0);
	if (allocated) {
	    free(multiDrawElementsIndices);
	}
    }
    else {
       switch (geo_type){
	  case GEO_TYPE_INDEXED_QUAD_SET : glDrawElements(GL_QUADS,validIndexCount, GL_UNSIGNED_INT, &indices[initialIndexIndex]);break;
	  case GEO_TYPE_INDEXED_TRI_SET : glDrawElements(GL_TRIANGLES, validIndexCount, GL_UNSIGNED_INT, &indices[initialIndexIndex]);break;
	  case GEO_TYPE_INDEXED_POINT_SET : glDrawElements(GL_POINTS, validIndexCount, GL_UNSIGNED_INT, &indices[initialIndexIndex]);break;
	  case GEO_TYPE_INDEXED_LINE_SET: glDrawElements(GL_LINES, validIndexCount, GL_UNSIGNED_INT, &indices[initialIndexIndex]);break;
       }
   }

    unlockArray(ctxProperties);    

    /* clean up if we turned on normalize */
    if (ctxProperties->rescale_normal_ext && isNonUniformScale) {
	glDisable(GL_NORMALIZE);
    }

    (*(table->ReleasePrimitiveArrayCritical))(env, indexCoord, indices, 0);
    
    if (vattrDefined) {
	resetVertexAttrs(ctxInfo, vertexAttrCount);
    }

    if (textureDefined) {
	resetTexture(ctxInfo);

	(*(table->ReleasePrimitiveArrayCritical))(env, tcoordsetmap, texCoordSetMap, 0);
	if (texUnitStateMap != NULL)
	    (*(table->ReleasePrimitiveArrayCritical))(env, tunitstatemap, texUnitStateMap, 0);
    }    
}


JNIEXPORT
void JNICALL Java_javax_media_j3d_IndexedGeometryArrayRetained_executeIndexedGeometryVA(
    JNIEnv *env, 
    jobject obj,
    jlong ctxInfo,    
    jobject geo,
    jint geo_type, 
    jboolean isNonUniformScale,
    jboolean multiScreen,
    jboolean ignoreVertexColors,
    jint initialIndexIndex,
    jint validIndexCount,
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
    jint cdirty,
    jintArray indexCoord)
{
    jfieldID strip_field;
    jarray sarray;
    jsize strip_len;
    JNIEnv table;
    jint i;
    jclass geo_class;
    
    jfloat *fverts = NULL;
    jdouble *dverts = NULL;
    jbyte *bclrs = NULL;
    jfloat *fclrs = NULL, *norms = NULL;
    jarray *vaobjs = NULL;
    jfloat **vertexAttrPointer = NULL;
    jfloat **texCoordPointer = NULL;
    jarray *texobjs = NULL;

    jboolean floatCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_FLOAT) != 0);
    jboolean doubleCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_DOUBLE) != 0);
    jboolean floatColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_FLOAT) != 0);
    jboolean byteColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_BYTE) != 0);
    jboolean normalsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_NORMAL_FLOAT) != 0);
    jboolean vattrDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_VATTR_FLOAT) != 0);
    jboolean textureDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_TEXCOORD_FLOAT) != 0);

    table = *env;    

    if (vattrDefined) {
	vaobjs = (jarray *)malloc(vertexAttrCount * sizeof(jarray));
	vertexAttrPointer = (jfloat **)malloc(vertexAttrCount * sizeof(jfloat *));

	for (i = 0; i < vertexAttrCount; i++) {
	    vaobjs[i] = (*(table->GetObjectArrayElement))(env, vertexAttrData, i);
	}
    }

    if (textureDefined) {
	texobjs = (jarray*)malloc(texCoordMapLength * sizeof(jarray));
	texCoordPointer = (jfloat**)malloc(texCoordMapLength * sizeof(jfloat*));

	for (i = 0; i < texCoordMapLength; i++) {
	    texobjs[i] = (jarray)(*(table->GetObjectArrayElement))(env, texCoords, i);
	}
    }

    geo_class =  (jclass) (*(table->GetObjectClass))(env, geo);

    if (geo_type == GEO_TYPE_INDEXED_TRI_STRIP_SET || 
	geo_type == GEO_TYPE_INDEXED_TRI_FAN_SET   || 
	geo_type == GEO_TYPE_INDEXED_LINE_STRIP_SET) {

        strip_field = (jfieldID) (*(table->GetFieldID))(env, geo_class,
			"stripIndexCounts", "[I");
        sarray = (jarray)(*(table->GetObjectField))(env, geo, strip_field);
	strip_len = (jsize)(*(table->GetArrayLength))(env, sarray);

    }

    /* Get vertex attribute arrays */
    if (vattrDefined) {
	for (i = 0; i < vertexAttrCount; i++) {
	    vertexAttrPointer[i] = (jfloat *) (*(table->GetPrimitiveArrayCritical))(env, vaobjs[i], NULL);
	}
    }

    /* get texture arrays */
    if (textureDefined) {
	for (i = 0; i < texCoordMapLength; i++) {
	    if (texobjs[i] != NULL)
		texCoordPointer[i] = (jfloat*)(*(table->GetPrimitiveArrayCritical))(env,texobjs[i], NULL);
	    else
		texCoordPointer[i] = NULL;	
	    
	}	
    }
    
    /* get coordinate array */
    if (floatCoordDefined) {
	fverts= (jfloat *) (*(table->GetPrimitiveArrayCritical))(env, vfcoords, NULL);
    } else if (doubleCoordDefined) {
	dverts= (jdouble *) (*(table->GetPrimitiveArrayCritical))(env, vdcoords, NULL);	
    }
    
    /* get color array */
    if (floatColorsDefined) {
	fclrs = (jfloat *) (*(table->GetPrimitiveArrayCritical))(env, cfdata, NULL);
    } else if (byteColorsDefined) {
	bclrs = (jbyte *) (*(table->GetPrimitiveArrayCritical))(env, cbdata, NULL);
    }

    /* get normal array */
    if (normalsDefined) {
	norms = (jfloat *) (*(table->GetPrimitiveArrayCritical))(env,ndata, NULL);
    }

#ifdef VERBOSE
    fprintf(stderr, "IndexedGeometryArrayRetained.executeIndexedGeometryVA() -- calling executeIndexedGeometryArrayVA\n");
#endif /* VERBOSE */

    executeIndexedGeometryArrayVA(env, 
				  obj,
				  ctxInfo,    
				  geo,
				  geo_type, 
				  isNonUniformScale,
				  multiScreen,
				  ignoreVertexColors,
				  initialIndexIndex,
				  validIndexCount,
				  vertexCount,
				  vformat,
				  vdefined,
				  fverts,
				  dverts,
				  fclrs,
				  bclrs,
				  norms,
				  vertexAttrCount,
				  vertexAttrSizes,
				  vertexAttrPointer,
				  pass,  
				  texCoordMapLength,
				  tcoordsetmap,
				  numActiveTexUnit,
				  tunitstatemap,
				  texStride,
				  texCoordPointer,
				  cdirty,
				  indexCoord,
				  sarray,
				  strip_len);

    if (floatCoordDefined) {
	(*(table->ReleasePrimitiveArrayCritical))(env, vfcoords, fverts, 0); 
    }
    else if (doubleCoordDefined) {
	(*(table->ReleasePrimitiveArrayCritical))(env, vdcoords, dverts, 0); 
    }

    if (floatColorsDefined) {
	(*(table->ReleasePrimitiveArrayCritical))(env, cfdata, fclrs, 0); 
    }
    else if (byteColorsDefined) {
	(*(table->ReleasePrimitiveArrayCritical))(env, cbdata, bclrs, 0);
    }

    if (normalsDefined) {
	(*(table->ReleasePrimitiveArrayCritical))(env, ndata, norms, 0);
    }

    if (vattrDefined) {
	for (i = 0; i < vertexAttrCount; i++) {
	    (*(table->ReleasePrimitiveArrayCritical))(env, vaobjs[i], vertexAttrPointer[i], 0);
	}
    }

    if (vaobjs != NULL) {
	free(vaobjs);
    }
    if (vertexAttrPointer != NULL) {
	free(vertexAttrPointer);
    }

    if (textureDefined) {
	for (i = 0; i < texCoordMapLength; i++) {
	    if (texCoordPointer[i] != NULL) {
		(*(table->ReleasePrimitiveArrayCritical))(env, texobjs[i], texCoordPointer[i], 0);
	    }
	}
    }

    if (texobjs != NULL) {
	free(texobjs);
    }
    if (texCoordPointer != NULL) {
	free(texCoordPointer);
    }
}

JNIEXPORT
void JNICALL Java_javax_media_j3d_IndexedGeometryArrayRetained_executeIndexedGeometryVABuffer(
    JNIEnv *env, 
    jobject obj,
    jlong ctxInfo,    
    jobject geo,
    jint geo_type, 
    jboolean isNonUniformScale,
    jboolean multiScreen,
    jboolean ignoreVertexColors,
    jint initialIndexIndex,
    jint validIndexCount,
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
    jint cdirty,
    jintArray indexCoord)
{
    jfieldID strip_field;
    jarray sarray;
    jsize strip_len;
    JNIEnv table;
    jint i;
    jclass geo_class;
    
    jfloat *fverts = NULL;
    jdouble *dverts = NULL;
    jbyte *bclrs = NULL;
    jfloat *fclrs = NULL, *norms = NULL;
    jarray *vaobjs = NULL;
    jfloat **vertexAttrPointer = NULL;
    jfloat **texCoordPointer = NULL;
    jarray *texobjs = NULL;

    jboolean floatCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_FLOAT) != 0);
    jboolean doubleCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_DOUBLE) != 0);
    jboolean floatColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_FLOAT) != 0);
    jboolean byteColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_BYTE) != 0);
    jboolean normalsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_NORMAL_FLOAT) != 0);
    jboolean vattrDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_VATTR_FLOAT) != 0);
    jboolean textureDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_TEXCOORD_FLOAT) != 0);

    table = *env;

    if (vattrDefined) {
	vaobjs = (jarray *)malloc(vertexAttrCount * sizeof(jarray));
	vertexAttrPointer = (jfloat **)malloc(vertexAttrCount * sizeof(jfloat *));

	for (i = 0; i < vertexAttrCount; i++) {
	    vaobjs[i] = (*(table->GetObjectArrayElement))(env, vertexAttrData, i);
	}
    }

    if (textureDefined) {
	for (i = 0; i < texCoordMapLength; i++) {
	    texobjs[i] = (jarray)(*(table->GetObjectArrayElement))(env, texCoords, i);
	}
    }

    geo_class =  (jclass) (*(table->GetObjectClass))(env, geo);

    if (geo_type == GEO_TYPE_INDEXED_TRI_STRIP_SET || 
	geo_type == GEO_TYPE_INDEXED_TRI_FAN_SET   || 
	geo_type == GEO_TYPE_INDEXED_LINE_STRIP_SET) {

        strip_field = (jfieldID) (*(table->GetFieldID))(env, geo_class,
			"stripIndexCounts", "[I");
        sarray = (jarray)(*(table->GetObjectField))(env, geo, strip_field);
	strip_len = (jsize)(*(table->GetArrayLength))(env, sarray);

    }

    /* Get vertex attribute arrays */
    if (vattrDefined) {
	for (i = 0; i < vertexAttrCount; i++) {
	    vertexAttrPointer[i] = (jfloat *) (*(table->GetDirectBufferAddress))(env, vaobjs[i]);
	}
    }

    /* get texture arrays */
    if (textureDefined) {
	for (i = 0; i < texCoordMapLength; i++) {
	    if (texobjs[i] != NULL)
		texCoordPointer[i] = (jfloat*)(*(table->GetDirectBufferAddress))(env,texobjs[i]);
	    else
		texCoordPointer[i] = NULL;	
	    
	}	
    }

    /* get coordinate array */
    if (floatCoordDefined) {
	fverts= (jfloat *)(*(table->GetDirectBufferAddress))(env, vcoords );
    } else if (doubleCoordDefined) {
	dverts= (jdouble *)(*(table->GetDirectBufferAddress))(env, vcoords ); 
    }

    /* get color array */
    if (floatColorsDefined) {
	if(cfdata != NULL)
	    fclrs = (jfloat *) (*(table->GetPrimitiveArrayCritical))(env, cfdata, NULL);
	else
	    fclrs = (jfloat *)(*(table->GetDirectBufferAddress))(env, cdataBuffer);
    }
    else if (byteColorsDefined) {
	if(cbdata != NULL)
	    bclrs = (jbyte *) (*(table->GetPrimitiveArrayCritical))(env, cbdata, NULL);
	else
	    bclrs = (jbyte *)(*(table->GetDirectBufferAddress))(env, cdataBuffer);
    }

    /* get normal array */
    if (normalsDefined) {
	norms = (jfloat *)(*(table->GetDirectBufferAddress))(env, ndata);
    }

#ifdef VERBOSE
    fprintf(stderr, "IndexedGeometryArrayRetained.executeIndexedGeometryVABuffer() -- calling executeIndexedGeometryArrayVA\n");
#endif /* VERBOSE */

    executeIndexedGeometryArrayVA(env, 
				  obj,
				  ctxInfo,    
				  geo,
				  geo_type, 
				  isNonUniformScale,
				  multiScreen,
				  ignoreVertexColors,
				  initialIndexIndex,
				  validIndexCount,
				  vertexCount,
				  vformat,
				  vdefined,
				  fverts,
				  dverts,
				  fclrs,
				  bclrs,
				  norms,
				  vertexAttrCount,
				  vertexAttrSizes,
				  vertexAttrPointer,
				  pass,  
				  texCoordMapLength,
				  tcoordsetmap,
				  numActiveTexUnit,
				  tunitstatemap,
				  texStride,
				  texCoordPointer,
				  cdirty,
				  indexCoord,
				  sarray,
				  strip_len);

    if(floatColorsDefined && cfdata != NULL)
	(*(table->ReleasePrimitiveArrayCritical))(env, cfdata, fclrs, 0);
    else if(byteColorsDefined && cbdata != NULL)
	(*(table->ReleasePrimitiveArrayCritical))(env, cbdata, bclrs, 0);

    if (vaobjs != NULL) {
	free(vaobjs);
    }
    if (vertexAttrPointer != NULL) {
	free(vertexAttrPointer);
    }

    if (texobjs != NULL) {
	free(texobjs);
    }
    if (texCoordPointer != NULL) {
	free(texCoordPointer);
    }
}

JNIEXPORT void JNICALL
Java_javax_media_j3d_IndexedGeometryArrayRetained_buildIndexedGeometry(
    JNIEnv *env, 
    jobject obj, jlong ctxInfo, jobject geo, 
    jint geo_type, 
    jboolean isNonUniformScale, jboolean updateAlpha, float alpha,
    jboolean ignoreVertexColors,
    jint initialIndexIndex,
    jint validIndexCount,
    jint vertexCount,
    jint vformat, 
    jint vertexAttrCount,
    jintArray vertexAttrSizes,
    jint texCoordSetCount,
    jintArray texCoordSetMapArray,
    jint texCoordSetMapLen,
    jintArray texUnitOffset, 
    jdoubleArray xform, jdoubleArray nxform,
    jfloatArray varray, jintArray indexCoord)
{
    jclass geo_class;
    JNIEnv table;
    jboolean useAlpha = JNI_FALSE;

    jfloat *verts;
    jint *indices;
    jint i;
    size_t bstride;
    jint texStride, *texCoordSetMapOffset, texSize;
    GLsizei *countArray;
    GLenum iaFormat;
    int useInterleavedArrays;
    jsize strip_len;
    int primType;
    jint stride, coordoff, normoff, coloroff, texCoordoff;
    jfieldID strip_field;
    jarray sarray;
    jdouble *xform_ptr = NULL;
    jdouble *nxform_ptr = NULL;
    jfloat *tmpCoordArray = NULL, *tmpNormalArray = NULL;
    jint* tmpDrawElementsIndices[100];

    jint** multiDrawElementsIndices = NULL;
    jint allocated = 0;
    int offset = 0;
    int vAttrStride = 0;
    int vAttrOff;
    jint *vAttrSizesPtr = NULL;

    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo;
    jlong ctx = ctxProperties->context;

#ifdef VERBOSE
    fprintf(stderr, "IndexedGeometryArrayRetained.buildIndexedGeometry()\n");
#endif /* VERBOSE */

    table = *env;
    geo_class =  (jclass) (*(table->GetObjectClass))(env, geo);

    /* This matches the code in GeometryArrayRetained.java */
    stride = coordoff = normoff = coloroff = texCoordoff = 0;
    vAttrOff = 0;
    if ((vformat & GA_COORDINATES) != 0) {
	glEnableClientState(GL_VERTEX_ARRAY);
	stride += 3;
    }
    else {
	glDisableClientState(GL_VERTEX_ARRAY);
    }

    if ((vformat & GA_NORMALS) != 0) {
	glEnableClientState(GL_NORMAL_ARRAY);
	stride += 3;
	coordoff += 3;
    }
    else {
	glDisableClientState(GL_NORMAL_ARRAY);
    }
    
    if ((vformat & GA_COLOR) != 0) {
	glEnableClientState(GL_COLOR_ARRAY);
	stride += 4;
	normoff += 4;
	coordoff += 4;
    }
    else {
	glDisableClientState(GL_COLOR_ARRAY);
    }

    if ((vformat & GA_TEXTURE_COORDINATE) != 0) {
        if ((vformat & GA_TEXTURE_COORDINATE_2) != 0) {
	    texSize = 2;
	    texStride = 2 * texCoordSetCount;
        } else if ((vformat & GA_TEXTURE_COORDINATE_3) != 0) {
	    texSize = 3;
	    texStride = 3 * texCoordSetCount;
        } else if ((vformat & GA_TEXTURE_COORDINATE_4) != 0) {
	    texSize = 4;
	    texStride = 4 * texCoordSetCount;
	}
	stride += texStride;
	normoff += texStride;
	coloroff += texStride;
	coordoff += texStride;
    }

    if ((vformat & GA_VERTEX_ATTRIBUTES) != 0) {
	if (vertexAttrSizes != NULL) {
	    vAttrSizesPtr = table->GetIntArrayElements(env, vertexAttrSizes, NULL);
	}
	for (i = 0; i < vertexAttrCount; i++) {
	    vAttrStride += vAttrSizesPtr[i];
	}
	stride += vAttrStride;
	normoff += vAttrStride;
	coloroff += vAttrStride;
	coordoff += vAttrStride;
	texCoordoff += vAttrStride;
    }

    bstride = stride*sizeof(float);

    /* 
     * process alpha for geometryArray without alpha
     */
    if (updateAlpha == JNI_TRUE && ignoreVertexColors == JNI_FALSE) {
	useAlpha = JNI_TRUE;
    }
    
    /*
     * call other JNI functions before entering Critical region 
     * i.e., GetPrimitiveArrayCritical
     */
    if (geo_type == GEO_TYPE_INDEXED_TRI_STRIP_SET || 
	geo_type == GEO_TYPE_INDEXED_TRI_FAN_SET   || 
	geo_type == GEO_TYPE_INDEXED_LINE_STRIP_SET) {

        strip_field = (jfieldID) (*(table->GetFieldID))(env, geo_class,
			"stripIndexCounts", "[I");
        sarray = (jarray)(*(table->GetObjectField))(env, geo, strip_field);
	strip_len = (jsize)(*(table->GetArrayLength))(env, sarray);
    }



    /* begin critical region */
    verts = (jfloat *) (*(table->GetPrimitiveArrayCritical))(env, varray, NULL);

    indices = (jint *) (*(table->GetPrimitiveArrayCritical))(env, indexCoord, NULL);

    if (texCoordSetMapLen >0) {
        texCoordSetMapOffset = (jint *)(*(table->GetPrimitiveArrayCritical))
						(env, texUnitOffset, NULL);
    }


    /* get the static transform if exists */
    if (xform != NULL) {
        xform_ptr = (jdouble *) (*(table->GetPrimitiveArrayCritical))(
				env, xform, NULL);

    }
    

    /* get the static normals transform if exists */
    if (nxform != NULL) {
        nxform_ptr = (jdouble *) (*(table->GetPrimitiveArrayCritical))(
				env, nxform, NULL);
    }
        

    /*
     * Check if normal is present and nxform_ptr id non-null, if yes,
     * create a new normal array and apply the xform
     */
    if ((vformat & GA_NORMALS) != 0 && (nxform_ptr != NULL)) {
	/* create a temporary array for normals */
	tmpNormalArray = (jfloat*) malloc(vertexCount * sizeof(float) * 3);
	for (i = 0; i < vertexCount*3; i+=3) {
	    tmpNormalArray[i] =  (float) (nxform_ptr[0] * verts[normoff] +
					  nxform_ptr[1] * verts[normoff+1] +
					  nxform_ptr[2] * verts[normoff+2]);
	    tmpNormalArray[i+1] =  (float) (nxform_ptr[4] * verts[normoff] +
					    nxform_ptr[5] * verts[normoff+1] +
					    nxform_ptr[6] * verts[normoff+2]);
	    tmpNormalArray[i+2] =  (float) (nxform_ptr[8] * verts[normoff] +
					    nxform_ptr[9] * verts[normoff+1] +
					    nxform_ptr[10] * verts[normoff+2]);	    
	    normoff += stride;
	}
    }

    if ((vformat & GA_COORDINATES) != 0 && xform_ptr != NULL) {
	/* create a temporary array for normals */
	tmpCoordArray = (jfloat*) malloc(vertexCount * sizeof(float) * 3);
	for (i = 0; i < vertexCount*3; i+=3) {
	    tmpCoordArray[i] =  (float) (xform_ptr[0] * verts[coordoff] +
					 xform_ptr[1] * verts[coordoff+1] +
					 xform_ptr[2] * verts[coordoff+2]);
	    tmpCoordArray[i+1] =  (float) (xform_ptr[4] * verts[coordoff] +
					   xform_ptr[5] * verts[coordoff+1] +
					   xform_ptr[6] * verts[coordoff+2]);
	    tmpCoordArray[i+2] =  (float) (xform_ptr[8] * verts[coordoff] +
					   xform_ptr[9] * verts[coordoff+1] +
					   xform_ptr[10] * verts[coordoff+2]);	    
	    coordoff += stride;
	}
    }

    
    if (geo_type == GEO_TYPE_INDEXED_TRI_STRIP_SET || 
	geo_type == GEO_TYPE_INDEXED_TRI_FAN_SET   || 
	geo_type == GEO_TYPE_INDEXED_LINE_STRIP_SET) {


	switch (geo_type) {
	  case GEO_TYPE_INDEXED_TRI_STRIP_SET :
	    primType = GL_TRIANGLE_STRIP;
	    break;
	  case GEO_TYPE_INDEXED_TRI_FAN_SET :
	    primType = GL_TRIANGLE_FAN;
	    break;
	  case GEO_TYPE_INDEXED_LINE_STRIP_SET :
	    primType = GL_LINE_STRIP;
	    break;
	}


	countArray = (GLsizei *) (*(table->GetPrimitiveArrayCritical))(env, sarray, 
			NULL);

	if ((ignoreVertexColors == JNI_TRUE) || (xform_ptr != NULL) || 
	    ((vformat & GA_TEXTURE_COORDINATE) && ((texCoordSetMapLen > 1) ||
		(texCoordSetCount > 1)))) {
	    useInterleavedArrays = 0;
	} else {
	    INTERLEAVEDARRAYS_TEST()
	}
	
	if (useInterleavedArrays) {
	    glInterleavedArrays(iaFormat, bstride, verts);
	} else {
	    if (vformat & GA_NORMALS) {
		if (nxform_ptr == NULL) {
		    glNormalPointer(GL_FLOAT, bstride, &(verts[normoff]));
		}
		else {
		    glNormalPointer(GL_FLOAT, 3 * sizeof(float), tmpNormalArray);
		}
	    }
	    if (ignoreVertexColors == JNI_FALSE && vformat & GA_COLOR) {
		if (vformat & GA_WITH_ALPHA || (useAlpha == GL_TRUE)) {
		    glColorPointer(4, GL_FLOAT, bstride, &(verts[coloroff]));
		} else {
		    /*
		    for (i = 0; i < vcount; i++) {
			fprintf(stderr, "r = %f, g = %f, b = %f\n", verts[i*bstride +coloroff], verts[i*bstride +coloroff+1],verts[i*bstride +coloroff+2]);
		    }
		    */
		    glColorPointer(3, GL_FLOAT, bstride, &(verts[coloroff]));
		}
	    }
	    if (vformat & GA_COORDINATES) {
		if (xform_ptr == NULL) {
		/*
		    for (i = 0; i < vcount; i++) {
			fprintf(stderr, "x = %f, y = %f, z = %f\n", verts[i*bstride +coordoff], verts[i*bstride +coordoff+1],verts[i*bstride +coordoff+2]);
		    }
		    */
		    glVertexPointer(3, GL_FLOAT, bstride, &(verts[coordoff]));
		}
		else {
		    glVertexPointer(3, GL_FLOAT, 3 * sizeof(float), tmpCoordArray);
		}
	    }

	    if (vformat & GA_TEXTURE_COORDINATE) {

                executeTexture(-1, texCoordSetMapLen,
                                texSize, bstride, texCoordoff,
                                texCoordSetMapOffset, 
				texCoordSetMapLen, NULL, 
				verts, ctxInfo);
	    }

	    if (vformat & GA_VERTEX_ATTRIBUTES) {
		jfloat *vAttrPtr = &verts[vAttrOff];

		for (i = 0; i < vertexAttrCount; i++) {
		    ctxProperties->enableVertexAttrArray(ctxProperties, i);
		    ctxProperties->vertexAttrPointer(ctxProperties, i, vAttrSizesPtr[i],
						     GL_FLOAT, bstride, vAttrPtr);
		    vAttrPtr += vAttrSizesPtr[i];
		}
	    }
	}  

	switch (geo_type) {
	  case GEO_TYPE_INDEXED_TRI_STRIP_SET :
	    primType = GL_TRIANGLE_STRIP;
	    break;
	  case GEO_TYPE_INDEXED_TRI_FAN_SET :
	    primType = GL_TRIANGLE_FAN;
	    break;
	  case GEO_TYPE_INDEXED_LINE_STRIP_SET :
	    primType = GL_LINE_STRIP;
	    break;
	}

	lockArray(ctxProperties, vertexCount);

	if (ctxProperties->multi_draw_arrays_ext || ctxProperties->multi_draw_arrays_sun) {
	    if (strip_len >  100) {
		multiDrawElementsIndices = (jint**)malloc(strip_len * sizeof(int*));
		allocated  = 1;
	    }
	    else {
		multiDrawElementsIndices =(jint**) &tmpDrawElementsIndices;
	    }
		
	    offset = initialIndexIndex;
	    for (i=0; i < strip_len;i++) {
		multiDrawElementsIndices[i] = &indices[offset];
		offset += countArray[i];
	    }
	    ctxProperties->glMultiDrawElementsEXT(primType, countArray, GL_UNSIGNED_INT,(const void **)multiDrawElementsIndices, strip_len);

	} else {
	    offset = initialIndexIndex;
	    for (i=0; i < strip_len;i++) {
		glDrawElements(primType,  countArray[i], GL_UNSIGNED_INT, &indices[offset]);
		offset += countArray[i];
	    }
	}
	(*(table->ReleasePrimitiveArrayCritical))(env, sarray, countArray, 0);
	if (allocated) {
	    free(multiDrawElementsIndices);
	}
	

    }
    else if ((geo_type == GEO_TYPE_INDEXED_QUAD_SET) ||
	     (geo_type == GEO_TYPE_INDEXED_TRI_SET) ||
	     (geo_type == GEO_TYPE_INDEXED_POINT_SET) ||
	     (geo_type == GEO_TYPE_INDEXED_LINE_SET)) {

	switch (geo_type) {
	  case GEO_TYPE_INDEXED_QUAD_SET :
	    primType = GL_QUADS;
	    break;
	  case GEO_TYPE_INDEXED_TRI_SET :
	    primType = GL_TRIANGLES;
	    break;
	  case GEO_TYPE_INDEXED_POINT_SET :
	    primType = GL_POINTS;
	    break;
	  case GEO_TYPE_INDEXED_LINE_SET :
	    primType = GL_LINES;
	    break;
   
	}

	if ((ignoreVertexColors == JNI_TRUE) || (xform_ptr != NULL) || 
	    ((vformat & GA_TEXTURE_COORDINATE) && ((texCoordSetMapLen > 1) ||
		(texCoordSetCount > 1)))) {
	    useInterleavedArrays = 0;
	} else {
	    INTERLEAVEDARRAYS_TEST()
	}

	if (useInterleavedArrays) {
	    glInterleavedArrays(iaFormat, bstride, verts);
	} else {
	    if (vformat & GA_NORMALS) {

		if (nxform_ptr == NULL) {
		    glNormalPointer(GL_FLOAT, bstride, &(verts[normoff]));
		}
		else {
		    glNormalPointer(GL_FLOAT, 3 * sizeof(float), tmpNormalArray);
		}
	    } 
	    if (ignoreVertexColors == JNI_FALSE && vformat & GA_COLOR) {
		if (vformat & GA_WITH_ALPHA || (useAlpha == GL_TRUE)) {
		    glColorPointer(4, GL_FLOAT, bstride, &(verts[coloroff]));
		} else {
		    glColorPointer(3, GL_FLOAT, bstride, &(verts[coloroff]));
		}
	    }
	    if (vformat & GA_COORDINATES) {

		if (xform_ptr == NULL) {
		    glVertexPointer(3, GL_FLOAT, bstride, &(verts[coordoff]));
		}
		else {
		    glVertexPointer(3, GL_FLOAT, 3 * sizeof(float), tmpCoordArray);
		}
	    } 
	    
	    if (vformat & GA_TEXTURE_COORDINATE) {
                executeTexture(-1, texCoordSetMapLen,
                                texSize, bstride, texCoordoff,
                                texCoordSetMapOffset, 
				texCoordSetMapLen, NULL, 
				verts, ctxInfo);
	    } 

	    if (vformat & GA_VERTEX_ATTRIBUTES) {
		jfloat *vAttrPtr = &verts[vAttrOff];

		for (i = 0; i < vertexAttrCount; i++) {
		    ctxProperties->enableVertexAttrArray(ctxProperties, i);
		    ctxProperties->vertexAttrPointer(ctxProperties, i, vAttrSizesPtr[i],
						     GL_FLOAT, bstride, vAttrPtr);
		    vAttrPtr += vAttrSizesPtr[i];
		}
	    }
	}
	lockArray(ctxProperties, vertexCount);	
	switch (geo_type){
	  case GEO_TYPE_INDEXED_QUAD_SET : glDrawElements(GL_QUADS,validIndexCount, GL_UNSIGNED_INT, &indices[initialIndexIndex]);break;
	  case GEO_TYPE_INDEXED_TRI_SET : glDrawElements(GL_TRIANGLES, validIndexCount, GL_UNSIGNED_INT, &indices[initialIndexIndex]);break;
	  case GEO_TYPE_INDEXED_POINT_SET : glDrawElements(GL_POINTS, validIndexCount, GL_UNSIGNED_INT, &indices[initialIndexIndex]);break;
	  case GEO_TYPE_INDEXED_LINE_SET: glDrawElements(GL_LINES, validIndexCount, GL_UNSIGNED_INT, &indices[initialIndexIndex]);break;
	}
    }

    unlockArray(ctxProperties);
    
    if (vformat & GA_VERTEX_ATTRIBUTES) {
	resetVertexAttrs(ctxInfo, vertexAttrCount);
    }

    if (vformat & GA_TEXTURE_COORDINATE) {
	resetTexture(ctxInfo);
    }

    if (tmpNormalArray != NULL) {
	free(tmpNormalArray);
    }
    if (tmpCoordArray != NULL) {
	free(tmpCoordArray);
    }
    
    (*(table->ReleasePrimitiveArrayCritical))(env, varray, verts, 0);

    (*(table->ReleasePrimitiveArrayCritical))(env, indexCoord, indices, 0);


    if (texCoordSetMapLen > 0)
        (*(table->ReleasePrimitiveArrayCritical))(env, texUnitOffset, 
						texCoordSetMapOffset, 0);

    if (xform_ptr != NULL)
        (*(table->ReleasePrimitiveArrayCritical))(env, xform, xform_ptr, 0);

    if (nxform_ptr != NULL)
        (*(table->ReleasePrimitiveArrayCritical))(env, nxform, nxform_ptr, 0);

    if (vAttrSizesPtr != NULL) {
	table->ReleaseIntArrayElements(env, vertexAttrSizes, vAttrSizesPtr, JNI_ABORT);
    }
}


/* execute geometry array with java array format */
/*
 * Class:     javax_media_j3d_GeometryArrayRetained
 * Method:    buildGAForByRef
 * Signature: (JLjavax/media/j3d/GeometryArrayRetained;IZZFZIIII[F[DI[F[BI[FI[I[I[[FI[I[II[Ljava/lang/Object;[D[D)V
 */
JNIEXPORT void JNICALL Java_javax_media_j3d_GeometryArrayRetained_buildGAForByRef(
    JNIEnv *env, 
    jobject obj,
    jlong ctxInfo,    
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
    jclass geo_class;
    JNIEnv table;
    jboolean useAlpha = JNI_FALSE;
    jfieldID strip_field;
    jarray sarray;
    jint i;
    jsize strip_len;
    jarray start_array;
    jfieldID start_field;
    jfloat *fverts = NULL;
    jdouble *dverts = NULL;
    jbyte *bclrs = NULL;
    jfloat *fclrs = NULL, *norms = NULL;
    jdouble *xform_ptr = NULL;
    jdouble *nxform_ptr = NULL;
    jfloat *tmpFloatCoordArray = NULL, *tmpNormalArray = NULL, *tmpFloatColors = NULL;
    jdouble *tmpDoubleCoordArray = NULL;
    jbyte* tmpByteColors= NULL;
    jfloat* fvptr = NULL, *nptr = NULL, *fcptr = NULL;
    jdouble* dvptr = NULL;
    jbyte* bcptr = NULL;
    jarray *vaobjs = NULL;
    jfloat **vertexAttrPointer = NULL;
    jfloat **texCoordPointer = NULL;
    jarray *texobjs = NULL;
    jint *tunitstatemap = NULL;
    int offset = 0;

    jboolean floatCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_FLOAT) != 0);
    jboolean doubleCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_DOUBLE) != 0);
    jboolean floatColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_FLOAT) != 0);
    jboolean byteColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_BYTE) != 0);
    jboolean normalsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_NORMAL_FLOAT) != 0);
    jboolean vattrDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_VATTR_FLOAT) != 0);
    jboolean textureDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_TEXCOORD_FLOAT) != 0);

    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo;
    jlong ctx = ctxProperties->context;
      
    table = *env;

    if (vattrDefined) {
	vaobjs = (jarray *)malloc(vertexAttrCount * sizeof(jarray));
	vertexAttrPointer = (jfloat **)malloc(vertexAttrCount * sizeof(jfloat *));

	for (i = 0; i < vertexAttrCount; i++) {
	    vaobjs[i] = (*(table->GetObjectArrayElement))(env, vertexAttrData, i);
	}
    }

    if (textureDefined) {
	texobjs = (jarray*)malloc(texCoordMapLength * sizeof(jarray));
	texCoordPointer = (jfloat**)malloc(texCoordMapLength * sizeof(jfloat*));

	for (i = 0; i < texCoordMapLength; i++) {
	    texobjs[i] = (*(table->GetObjectArrayElement))(env, texCoords, i);
	}
    }

    geo_class =  (jclass) (*(table->GetObjectClass))(env, geo);
    
    /* 
     * process alpha for geometryArray without alpha
     */
    if (updateAlpha == JNI_TRUE && ignoreVertexColors== JNI_FALSE) {
	useAlpha = JNI_TRUE;
    }

    /*
     * call other JNI functions before entering Critical region 
     * i.e., GetPrimitiveArrayCritical
     */
    if (geo_type == GEO_TYPE_TRI_STRIP_SET || 
	geo_type == GEO_TYPE_TRI_FAN_SET   || 
	geo_type == GEO_TYPE_LINE_STRIP_SET) {

        strip_field = (jfieldID) (*(table->GetFieldID))(env, geo_class,
			"stripVertexCounts", "[I");
        sarray = (jarray)(*(table->GetObjectField))(env, geo, strip_field);
	strip_len = (jsize)(*(table->GetArrayLength))(env, sarray);
	start_field = (jfieldID) (*(table->GetFieldID))(env, geo_class,
							"stripStartOffsetIndices", "[I");
	start_array = (jarray)(*(table->GetObjectField))(env, geo,
                        start_field);
    }

    if (ignoreVertexColors == JNI_TRUE) {
	vformat &= ~GA_COLOR;
	floatColorsDefined = JNI_FALSE;
	byteColorsDefined = JNI_FALSE;
    }    

    /* get vertex attr arrays */
    if (vattrDefined) {
	for (i = 0; i < vertexAttrCount; i++) {
	    vertexAttrPointer[i] = (jfloat *) (*(table->GetPrimitiveArrayCritical))(env, vaobjs[i], NULL);
	}
    }

    /* get texture arrays */
    if (textureDefined) {
	tunitstatemap = (int *)malloc(texCoordMapLength * sizeof(int));
	for (i = 0; i < texCoordMapLength; i++) {
	    tunitstatemap[i] = i;
	    if (texobjs[i] != NULL)
		texCoordPointer[i] = (jfloat*)(*(table->GetPrimitiveArrayCritical))(env,texobjs[i], NULL);
	    else
		texCoordPointer[i] = NULL;	
	    
	}	
    }
    
    /* get coordinate array */
    if (floatCoordDefined) {
	glEnableClientState(GL_VERTEX_ARRAY);
	fverts= (jfloat *) (*(table->GetPrimitiveArrayCritical))(env, vfcoords, NULL);
	fvptr = fverts;
    } else if (doubleCoordDefined) {
	glEnableClientState(GL_VERTEX_ARRAY);
	dverts= (jdouble *) (*(table->GetPrimitiveArrayCritical))(env, vdcoords, NULL);
	dvptr = dverts;
    }
    else {
	glDisableClientState(GL_VERTEX_ARRAY);
    }
    
    /* get color array */
    if (floatColorsDefined) {
	glEnableClientState(GL_COLOR_ARRAY);
	fclrs = (jfloat *) (*(table->GetPrimitiveArrayCritical))(env, cfdata, NULL);
	fcptr = fclrs;
    } else if (byteColorsDefined) {
	glEnableClientState(GL_COLOR_ARRAY);
	bclrs = (jbyte *) (*(table->GetPrimitiveArrayCritical))(env, cbdata, NULL);
	bcptr = bclrs;
    }
    else {
	glDisableClientState(GL_COLOR_ARRAY);
    }
    /* get normal array */
    if (normalsDefined) {
	glEnableClientState(GL_NORMAL_ARRAY);
	norms = (jfloat *) (*(table->GetPrimitiveArrayCritical))(env,ndata, NULL);
	nptr = norms;
    }
    else {
	glDisableClientState(GL_NORMAL_ARRAY);
    }
    
    /* get the static transform if exists */
    if (xform != NULL) {
        xform_ptr = (jdouble *) (*(table->GetPrimitiveArrayCritical))(
				env, xform, NULL);

    }
    

    /* get the static normals transform if exists */
    if (nxform != NULL) {
        nxform_ptr = (jdouble *) (*(table->GetPrimitiveArrayCritical))(
				env, nxform, NULL);
    }    
    
    /*
     * Check if normal is present and nxform_ptr id non-null, if yes,
     * create a new normal array and apply the xform
     */
    if (normalsDefined) {
	if(nxform_ptr != NULL) {
	    /* create a temporary array for normals */
	    tmpNormalArray = (jfloat*) malloc(vcount * sizeof(float) * 3);
	    for (i = initialNormalIndex; i < vcount*3; i+=3) {
		tmpNormalArray[i] =  (float) (nxform_ptr[0] * norms[i] +
					      nxform_ptr[1] * norms[i+1] +
					      nxform_ptr[2] * norms[i+2]);
		tmpNormalArray[i+1] =  (float) (nxform_ptr[4] * norms[i] +
						nxform_ptr[5] * norms[i+1] +
						nxform_ptr[6] * norms[i+2]);
		tmpNormalArray[i+2] =  (float) (nxform_ptr[8] * norms[i] +
						nxform_ptr[9] * norms[i+1] +
						nxform_ptr[10] * norms[i+2]);	    
	    }
	    nptr = tmpNormalArray;
	}

    }

    if (xform_ptr != NULL) {
	if (floatCoordDefined) {
	    /* create a temporary array for normals */
	    tmpFloatCoordArray = (jfloat*) malloc(vcount * sizeof(float) * 3);
	    for (i = initialCoordIndex; i < vcount*3; i+=3) {
		tmpFloatCoordArray[i] =  (float) (xform_ptr[0] * fverts[i] +
					     xform_ptr[1] * fverts[i+1] +
					     xform_ptr[2] * fverts[i+2]);
		tmpFloatCoordArray[i+1] =  (float) (xform_ptr[4] * fverts[i] +
					       xform_ptr[5] * fverts[i+1] +
					       xform_ptr[6] * fverts[i+2]);
		tmpFloatCoordArray[i+2] =  (float) (xform_ptr[8] * fverts[i] +
					       xform_ptr[9] * fverts[i+1] +
					       xform_ptr[10] * fverts[i+2]);	    
	    }
	    fvptr = tmpFloatCoordArray;
	}
	else {
	    tmpDoubleCoordArray = (jdouble*) malloc(vcount * sizeof(double) * 3);
	    for (i = initialCoordIndex; i < vcount*3; i+=3) {
		tmpDoubleCoordArray[i] =  (double) (xform_ptr[0] * dverts[i] +
					     xform_ptr[1] * dverts[i+1] +
					     xform_ptr[2] * dverts[i+2]);
		tmpDoubleCoordArray[i+1] =  (double) (xform_ptr[4] * dverts[i] +
					       xform_ptr[5] * dverts[i+1] +
					       xform_ptr[6] * dverts[i+2]);
		tmpDoubleCoordArray[i+2] =  (double) (xform_ptr[8] * dverts[i] +
					       xform_ptr[9] * dverts[i+1] +
					       xform_ptr[10] * dverts[i+2]);	    
	    }
	    dvptr = tmpDoubleCoordArray;
	}
	    
    }
    /*
    fprintf(stderr, "floatColorsDefined = %d, useAlpha = %d\n",
	    floatColorsDefined,useAlpha);
    */
    if (floatColorsDefined && useAlpha) {
	tmpFloatColors = (jfloat*)malloc(vcount*sizeof(float) * 4);
	if ((vformat & GA_WITH_ALPHA) != 0) {
	    /*	    fprintf(stderr, "with Alpha\n") */
	    for (i = initialColorIndex; i < vcount*4; i+=4) {
		tmpFloatColors[i] =  fclrs[i];
		tmpFloatColors[i+1] =  fclrs[i+1];
		tmpFloatColors[i+2] = fclrs[i+2];
		tmpFloatColors[i+3] = (float)(alpha* fclrs[i+3]);
	    }
	}
	else {
	    /*	    fprintf(stderr, "without Alpha\n") */
	    int k = 0;
	    for (i = initialColorIndex; i < vcount*4; i+=4) {
		tmpFloatColors[i] =  fclrs[k++];
		tmpFloatColors[i+1] =  fclrs[k++];
		tmpFloatColors[i+2] = fclrs[k++];
		tmpFloatColors[i+3] = (float)(alpha);
	    }
	}
	fcptr = tmpFloatColors;
	vformat |= GA_WITH_ALPHA;
    }
    else if (byteColorsDefined && useAlpha) {
	tmpByteColors = (jbyte*)malloc(vcount*sizeof(jbyte) * 4);
	if ((vformat & GA_WITH_ALPHA) != 0) {
	    for (i = initialColorIndex; i < vcount*4; i+=4) {
		tmpByteColors[i] =  bclrs[i];
		tmpByteColors[i+1] = bclrs[i+1];
		tmpByteColors[i+2] =bclrs[i+2];
		tmpByteColors[i+3] =  (jbyte) (alpha * ((int)bclrs[i+3] & 0xff));
	    }
	}
	else {
	    int k = 0;
	    for (i = initialColorIndex; i < vcount*4; i+=4) {
		tmpByteColors[i] =  bclrs[k++];
		tmpByteColors[i+1] = bclrs[k++];
		tmpByteColors[i+2] =bclrs[k++];
		tmpByteColors[i+3] =  (jbyte) (alpha * 255.0);
	    }
	}
	bcptr = tmpByteColors;
	vformat |= GA_WITH_ALPHA;

    }	

#ifdef VERBOSE
    fprintf(stderr, "GeometryArrayRetained.buildGAForByRef() -- calling executeGeometryArrayVA\n");
#endif /* VERBOSE */

    executeGeometryArrayVA(env, obj, ctxInfo, geo, geo_type,
			   isNonUniformScale,  JNI_FALSE, ignoreVertexColors,
			   vcount, vformat,  vdefined, initialCoordIndex,
			   fvptr, dvptr, initialColorIndex,
			   fcptr, bcptr, initialNormalIndex,
			   nptr,
			   vertexAttrCount, vertexAttrSizes,
			   vertexAttrIndices, vertexAttrPointer,
			   -1, texCoordMapLength,
			   tcoordsetmap, texCoordMapLength, tunitstatemap,
			   texindices,texStride,texCoordPointer,0, sarray, 
			   strip_len, start_array);

    if (vattrDefined) {
	for (i = 0; i < vertexAttrCount; i++) {
	    (*(table->ReleasePrimitiveArrayCritical))(env, vaobjs[i], vertexAttrPointer[i], 0);
	}
    }

    if (vaobjs != NULL) {
	free(vaobjs);
    }
    if (vertexAttrPointer != NULL) {
	free(vertexAttrPointer);
    }

    if (textureDefined) {
	if (tunitstatemap != NULL) {
	    free(tunitstatemap);
	}
	for (i = 0; i < texCoordMapLength; i++) {
	    if (texCoordPointer[i] != NULL) {
		(*(table->ReleasePrimitiveArrayCritical))(env, texobjs[i], texCoordPointer[i], 0);
	    }
	}
    }
   
    if (texobjs != NULL) {
	free(texobjs);
    }
    if (texCoordPointer != NULL) {
	free(texCoordPointer);
    }

    if (normalsDefined) {
	(*env)->ReleasePrimitiveArrayCritical(env, ndata, norms, 0);
	if (tmpNormalArray != NULL) {
	    free(tmpNormalArray);
	}
    }

    
    if (floatColorsDefined) {
	(*env)->ReleasePrimitiveArrayCritical(env, cfdata, fclrs, 0);
	if (tmpFloatColors != NULL) {
	    free(tmpFloatColors);
	}
    }
    else if (byteColorsDefined) {
	(*env)->ReleasePrimitiveArrayCritical(env, cbdata, bclrs, 0);
	if (tmpByteColors != NULL) {
	    free(tmpByteColors);
	}
    }
    
   
    if (floatCoordDefined) {
	(*env)->ReleasePrimitiveArrayCritical(env, vfcoords, fverts, 0);
	if (tmpFloatCoordArray != NULL) {
	    free(tmpFloatCoordArray);
	}
    }
    else if (doubleCoordDefined) {
	(*env)->ReleasePrimitiveArrayCritical(env, vdcoords, dverts, 0); 
	if (tmpDoubleCoordArray != NULL) {
	    free(tmpFloatCoordArray);
	}
    }
}


/* NOTE: NIO buffers are no longer supported in display lists. */
#if 0
/* execute geometry array with java array format */
JNIEXPORT
void JNICALL Java_javax_media_j3d_GeometryArrayRetained_buildGAForBuffer(
    JNIEnv *env, 
    jobject obj,
    jlong ctxInfo,    
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
    jclass geo_class;
    JNIEnv table;
    jboolean useAlpha = JNI_FALSE;
    jfieldID strip_field;
    jarray sarray;
    jint i;
    jsize strip_len;
    jarray start_array;
    jfieldID start_field;
    jfloat *fverts = NULL;
    jdouble *dverts = NULL;
    jbyte *bclrs = NULL;
    jfloat *fclrs = NULL, *norms = NULL;
    jdouble *xform_ptr = NULL;
    jdouble *nxform_ptr = NULL;
    jfloat *tmpFloatCoordArray = NULL, *tmpNormalArray = NULL, *tmpFloatColors = NULL;
    jdouble *tmpDoubleCoordArray = NULL;
    jbyte* tmpByteColors= NULL;
    jfloat* fvptr = NULL, *nptr = NULL, *fcptr = NULL;
    jdouble* dvptr = NULL;
    jbyte* bcptr = NULL;
    jfloat **texCoordPointer = NULL;
    jarray *texobjs = NULL;
    jint *tunitstatemap = NULL;
    jboolean floatCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_FLOAT) != 0);
    jboolean doubleCoordDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COORD_DOUBLE) != 0);
    jboolean floatColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_FLOAT) != 0);
    jboolean byteColorsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_COLOR_BYTE) != 0);
    jboolean normalsDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_NORMAL_FLOAT) != 0);
    jboolean textureDefined = ((vdefined & javax_media_j3d_GeometryArrayRetained_TEXCOORD_FLOAT) != 0);
    int offset = 0;

    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo;
    jlong ctx = ctxProperties->context;
      
    table = *env;

    if (textureDefined) {
	texobjs = (jarray*)malloc(texCoordMapLength * sizeof(jarray));
	texCoordPointer = (jfloat**)malloc(texCoordMapLength * sizeof(jfloat*));

	for (i = 0; i < texCoordMapLength; i++) {
	    texobjs[i] = (*(table->GetObjectArrayElement))(env, texCoords, i);
	}
    }

    geo_class =  (jclass) (*(table->GetObjectClass))(env, geo);


    /* 
     * process alpha for geometryArray without alpha
     */
    if (updateAlpha == JNI_TRUE && ignoreVertexColors== JNI_FALSE) {
	useAlpha = JNI_TRUE;
    }

    /*
     * call other JNI functions before entering Critical region 
     * i.e., GetPrimitiveArrayCritical
     */
    if (geo_type == GEO_TYPE_TRI_STRIP_SET || 
	geo_type == GEO_TYPE_TRI_FAN_SET   || 
	geo_type == GEO_TYPE_LINE_STRIP_SET) {

        strip_field = (jfieldID) (*(table->GetFieldID))(env, geo_class,
			"stripVertexCounts", "[I");
        sarray = (jarray)(*(table->GetObjectField))(env, geo, strip_field);
	strip_len = (jsize)(*(table->GetArrayLength))(env, sarray);
	start_field = (jfieldID) (*(table->GetFieldID))(env, geo_class,
							"stripStartOffsetIndices", "[I");
	start_array = (jarray)(*(table->GetObjectField))(env, geo,
                        start_field);
    }

    if (ignoreVertexColors == JNI_TRUE) {
	vformat &= ~GA_COLOR;
	floatColorsDefined = JNI_FALSE;
	byteColorsDefined = JNI_FALSE;
    }    
    /* get texture arrays */
    if (textureDefined) {
	tunitstatemap = (int *)malloc( texCoordMapLength * sizeof(int));
	for (i = 0; i < texCoordMapLength; i++) {
	    tunitstatemap[i] = i;
	    if (texobjs[i] != NULL)
		texCoordPointer[i] = (jfloat*)(*(table->GetPrimitiveArrayCritical))(env,texobjs[i], NULL);
	    else
		texCoordPointer[i] = NULL;	
	    
	}	
    }
    
    /* get coordinate array */
    if (floatCoordDefined) {
	glEnableClientState(GL_VERTEX_ARRAY);
	fverts= (jfloat *)(*(table->GetDirectBufferAddress))(env, vcoords );
	fvptr = fverts;
    } else if (doubleCoordDefined) {
	glEnableClientState(GL_VERTEX_ARRAY);
	dverts= (jdouble *)(*(table->GetDirectBufferAddress))(env, vcoords ); 
	dvptr = dverts;
    }
    else {
	glDisableClientState(GL_VERTEX_ARRAY);
    }
    
    if(fverts == NULL && dverts == NULL) {
	return;
    }    

    /* TODO KCR : get vertex attr arrays */

    /* get color array */
    if (floatColorsDefined) {
	glEnableClientState(GL_COLOR_ARRAY);
	fclrs = (jfloat *)(*(table->GetDirectBufferAddress))(env, cdataBuffer);
	fcptr = fclrs;
    } else if (byteColorsDefined) {
	glEnableClientState(GL_COLOR_ARRAY);
	bclrs = (jbyte *)(*(table->GetDirectBufferAddress))(env, cdataBuffer);
	bcptr = bclrs;
    }
    else {
	glDisableClientState(GL_COLOR_ARRAY);
    }
    /* get normal array */
    if (normalsDefined) {
	glEnableClientState(GL_NORMAL_ARRAY);
	norms = (jfloat *)(*(table->GetDirectBufferAddress))(env, ndata);
	nptr = norms;
    }
    else {
	glDisableClientState(GL_NORMAL_ARRAY);
    }
    /* get the static transform if exists */
    if (xform != NULL) {
        xform_ptr = (jdouble *) (*(table->GetPrimitiveArrayCritical))(
				env, xform, NULL);

    }
    

    /* get the static normals transform if exists */
    if (nxform != NULL) {
        nxform_ptr = (jdouble *) (*(table->GetPrimitiveArrayCritical))(
				env, nxform, NULL);
    }
        

    /*
     * Check if normal is present and nxform_ptr id non-null, if yes,
     * create a new normal array and apply the xform
     */
    if (normalsDefined) {
	if(nxform_ptr != NULL) {
	    /* create a temporary array for normals */
	    tmpNormalArray = (jfloat*) malloc(vcount * sizeof(float) * 3);
	    for (i = initialNormalIndex; i < vcount*3; i+=3) {
		tmpNormalArray[i] =  (float) (nxform_ptr[0] * norms[i] +
					      nxform_ptr[1] * norms[i+1] +
					      nxform_ptr[2] * norms[i+2]);
		tmpNormalArray[i+1] =  (float) (nxform_ptr[4] * norms[i] +
						nxform_ptr[5] * norms[i+1] +
						nxform_ptr[6] * norms[i+2]);
		tmpNormalArray[i+2] =  (float) (nxform_ptr[8] * norms[i] +
						nxform_ptr[9] * norms[i+1] +
						nxform_ptr[10] * norms[i+2]);	    
	    }
	    nptr = tmpNormalArray;
	}

    }

    if (xform_ptr != NULL) {
	if (floatCoordDefined) {
	    /* create a temporary array for normals */
	    tmpFloatCoordArray = (jfloat*) malloc(vcount * sizeof(float) * 3);
	    for (i = initialCoordIndex; i < vcount*3; i+=3) {
		tmpFloatCoordArray[i] =  (float) (xform_ptr[0] * fverts[i] +
					     xform_ptr[1] * fverts[i+1] +
					     xform_ptr[2] * fverts[i+2]);
		tmpFloatCoordArray[i+1] =  (float) (xform_ptr[4] * fverts[i] +
					       xform_ptr[5] * fverts[i+1] +
					       xform_ptr[6] * fverts[i+2]);
		tmpFloatCoordArray[i+2] =  (float) (xform_ptr[8] * fverts[i] +
					       xform_ptr[9] * fverts[i+1] +
					       xform_ptr[10] * fverts[i+2]);	    
	    }
	    fvptr = tmpFloatCoordArray;
	}
	else {
	    tmpDoubleCoordArray = (jdouble*) malloc(vcount * sizeof(double) * 3);
	    for (i = initialCoordIndex; i < vcount*3; i+=3) {
		tmpDoubleCoordArray[i] =  (double) (xform_ptr[0] * dverts[i] +
					     xform_ptr[1] * dverts[i+1] +
					     xform_ptr[2] * dverts[i+2]);
		tmpDoubleCoordArray[i+1] =  (double) (xform_ptr[4] * dverts[i] +
					       xform_ptr[5] * dverts[i+1] +
					       xform_ptr[6] * dverts[i+2]);
		tmpDoubleCoordArray[i+2] =  (double) (xform_ptr[8] * dverts[i] +
					       xform_ptr[9] * dverts[i+1] +
					       xform_ptr[10] * dverts[i+2]);	    
	    }
	    dvptr = tmpDoubleCoordArray;
	}
	    
    }
    if (floatColorsDefined && useAlpha) {
	tmpFloatColors = (jfloat*)malloc(vcount*sizeof(float) * 4);
	if ((vformat & GA_WITH_ALPHA) != 0) {
	    for (i = initialColorIndex; i < vcount*4; i+=4) {
		tmpFloatColors[i] =  fclrs[i];
		tmpFloatColors[i+1] =  fclrs[i+1];
		tmpFloatColors[i+2] = fclrs[i+2];
		tmpFloatColors[i+3] = (float)(alpha* fclrs[i+3]);
	    }
	}
	else {
	    int k = 0;
	    for (i = initialColorIndex; i < vcount*4; i+=4) {
		tmpFloatColors[i] =  fclrs[k++];
		tmpFloatColors[i+1] =  fclrs[k++];
		tmpFloatColors[i+2] = fclrs[k++];
		tmpFloatColors[i+3] = (float)(alpha);
	    }
	}
	fcptr = tmpFloatColors;
	vformat |= GA_WITH_ALPHA;
    }
    else if (byteColorsDefined && useAlpha) {
	tmpByteColors = (jbyte*)malloc(vcount*sizeof(jbyte) * 4);
	if ((vformat & GA_WITH_ALPHA) != 0) {
	    for (i = initialColorIndex; i < vcount*4; i+=4) {
		tmpByteColors[i] =  bclrs[i];
		tmpByteColors[i+1] = bclrs[i+1];
		tmpByteColors[i+2] =bclrs[i+2];
		tmpByteColors[i+3] =  (jbyte) (alpha * ((int)bclrs[i+3] & 0xff));
	    }
	}
	else {
	    int k = 0;
	    for (i = initialColorIndex; i < vcount*4; i+=4) {
		tmpByteColors[i] =  bclrs[k++];
		tmpByteColors[i+1] = bclrs[k++];
		tmpByteColors[i+2] =bclrs[k++];
		tmpByteColors[i+3] =  (jbyte) (alpha * 255.0);
	    }
	}
	bcptr = tmpByteColors;
	vformat |= GA_WITH_ALPHA;

    }	

#ifdef VERBOSE
    fprintf(stderr, "GeometryArrayRetained.buildGAForBuffer() -- calling executeGeometryArrayVA\n");
#endif /* VERBOSE */

    executeGeometryArrayVA(env, obj, ctxInfo, geo, geo_type,
			   isNonUniformScale,  JNI_FALSE, ignoreVertexColors,
			   vcount, vformat,  vdefined, initialCoordIndex,
			   fvptr, dvptr, initialColorIndex,
			   fcptr, bcptr, initialNormalIndex,
			   nptr,
			   /* TODO: vertexAttrCount, vertexAttrSizes, */
			   /* TODO: vertexAttrIndices, vertexAttrPointer, */
			   -1, texCoordMapLength,
			   tcoordsetmap, texCoordMapLength, tunitstatemap,
			   texindices,texStride,texCoordPointer,0, sarray, strip_len, start_array);
    if (textureDefined) {
	if (tunitstatemap != NULL) {
	    free(tunitstatemap);
	}
	for (i = 0; i < texCoordMapLength; i++) {
	    if (texCoordPointer[i] != NULL) {
		(*(table->ReleasePrimitiveArrayCritical))(env, texobjs[i], texCoordPointer[i], 0);
	    }
	}
    }
   
    if (texobjs != NULL) {
	free(texobjs);
    }
    if (texCoordPointer != NULL) {
	free(texCoordPointer);
    }

    if (tmpNormalArray != NULL) {
	free(tmpNormalArray);
    }

    
    if (tmpFloatColors != NULL) {
	free(tmpFloatColors);
    }
    else if (tmpByteColors != NULL) {
	free(tmpByteColors);
    }
    
   
    if (tmpFloatCoordArray != NULL) {
	free(tmpFloatCoordArray);
    }
    else if (tmpDoubleCoordArray != NULL) {
	free(tmpFloatCoordArray);
    }
}

#endif /* 0 */

