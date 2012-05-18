/*
 * Copyright 1999-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
 */

package javax.media.j3d;

class J3dDebug  {

    // For production release devPhase is set to false.

    // Do no debugging.
    static final int NO_DEBUG			= 0;

    // How much debugging information do we want ?
    // (LEVEL_1 is very terse, LEVEL_5 is very verbose)
    static final int LEVEL_1                    = 1;
    static final int LEVEL_2            	= 2;
    static final int LEVEL_3                    = 3;
    static final int LEVEL_4            	= 4;
    static final int LEVEL_5                    = 5;

    // This static final variable is used to turn on/off debugging,
    // checking, and initializing codes that may be preferred in
    // development phase but not necessarily required in the
    // production release.
    //
    // Beside for debugging, use this variable to do initialization,
    // checking objects existence, and other checks that may help in
    // uncovering potential bugs during code development. This
    // variable should be turned off during production release as it
    // may cause performance hit.
    static final boolean devPhase = VersionInfo.isDevPhase;

    // This is a property variable. It allows a true/false be sent to
    // J3d from command line, to on/off code segments.  To avoid
    // performance hit in production release, this variable MUST be
    // used with devPhase when guarding code segments for execution.
    // eg.   if(J3dDebug.devPhase && J3dDebug.debug)
    //             do code_segment;
    // Note: devPhase is a static final variable and debug isn't. If
    // devPhase is put before debug, smart compiler will not include
    // code_segment when devPhase is false.
    static boolean debug;

    // Class debug variable, there is one debug variable per class.
    // Set one of the 5 debug levels to the class debug variable when
    // debugging.
    // For example, alpha = !devPhase?NO_DEBUG:LEVEL_2; will cause
    // code segments guarded by LEVEL_1 and LEVEL_2 be executed.  And
    // alpha = !devPhase?NO_DEBUG:NO_DEBUG; means do no debug.
    static final int alpha = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int alternateAppearance = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int ambientLight = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int ambientLightRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int appearance = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int appearanceRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int assertionFailureException = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int attributeBin = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int audioDevice = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int audioDevice3D = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int audioDeviceEnumerator = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int auralAttributes = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int auralAttributesRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int bHInsertStructure = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int bHInternalNode = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int bHLeafInterface = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int bHLeafNode = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int bHNode = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int bHTree = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int background = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int backgroundRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int backgroundSound = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int backgroundSoundRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int badTransformException = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int behavior = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int behaviorRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int behaviorScheduler = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int behaviorStructure = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int billboard = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int boundingBox = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int boundingLeaf = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int boundingLeafRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int boundingPolytope = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int boundingSphere = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int bounds = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int branchGroup = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int branchGroupRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int cachedFrustum = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int canvas3D = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int canvasViewCache = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int canvasViewEventCatcher = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int capabilityBits = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int capabilityNotSetException = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int clip = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int clipRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int colorInterpolator = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int coloringAttributes = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int coloringAttributesRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int compileState = !devPhase?NO_DEBUG:LEVEL_3;
    static final int compressedGeometry = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int compressedGeometryHeader = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int compressedGeometryRenderMethod = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int compressedGeometryRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int coneSound = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int coneSoundRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int danglingReferenceException = !devPhase?NO_DEBUG:NO_DEBUG;

    static final int decalGroup = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int decalGroupRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int defaultRenderMethod = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int depthComponent = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int depthComponentFloat = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int depthComponentFloatRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int depthComponentInt = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int depthComponentIntRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int depthComponentNative = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int depthComponentNativeRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int depthComponentRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int directionalLight = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int directionalLightRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int displayListRenderMethod = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int distanceLOD = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int environmentSet = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int eventCatcher = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int exponentialFog = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int exponentialFogRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int fog = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int fogRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int font3D = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int fontExtrusion = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int generalizedStrip = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int generalizedStripFlags = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int generalizedVertexList = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int geometry = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int geometryArray = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int geometryArrayRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int geometryAtom = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int geometryDecompressor = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int geometryDecompressorRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int geometryDecompressorShape3D = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int geometryLock = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int geometryLockInterface = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int geometryRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int geometryStripArray = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int geometryStripArrayRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int geometryStructure = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int geometryUpdater = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int graphicsConfigTemplate3D = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int graphicsContext3D = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int group = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int groupRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int hashKey = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int hiResCoord = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int illegalRenderingStateException = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int illegalSharingException = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int imageComponent = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int imageComponent2D = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int imageComponent2DRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int imageComponent3D = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int imageComponent3DRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int imageComponentRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int indexedGeometryArray = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int indexedGeometryArrayRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int indexedGeometryStripArray = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int indexedGeometryStripArrayRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int indexedLineArray = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int indexedLineArrayRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int indexedLineStripArray = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int indexedLineStripArrayRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int indexedPointArray = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int indexedPointArrayRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int indexedQuadArray = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int indexedQuadArrayRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int indexedTriangleArray = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int indexedTriangleArrayRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int indexedTriangleFanArray = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int indexedTriangleFanArrayRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int indexedTriangleStripArray = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int indexedTriangleStripArrayRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int inputDevice = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int inputDeviceBlockingThread = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int inputDeviceScheduler = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int interpolator = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int j3dDataInputStream = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int j3dDataOutputStream = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int j3dDebug = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int j3dI18N = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int j3dMessage = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int j3dNodeTable = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int j3dQueryProps = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int j3dStructure = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int j3dThread = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int j3dThreadData = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int lOD = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int leaf = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int leafRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int light = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int lightBin = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int lightRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int lightSet = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int lineArray = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int lineArrayRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int lineAttributes = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int lineAttributesRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int lineStripArray = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int lineStripArrayRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int linearFog = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int linearFogRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int link = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int linkRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int locale = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int mRSWLock = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int masterControl = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int material = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int materialRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int mediaContainer = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int mediaContainerRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int modelClip = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int modelClipRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int morph = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int morphRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int multipleParentException = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int node = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int nodeComponent = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int nodeComponentRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int nodeReferenceTable = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int nodeRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int objectUpdate = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int orderedBin = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int orderedCollection = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int orderedGroup = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int orderedGroupRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int pathInterpolator = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int physicalBody = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int physicalEnvironment = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int pickBounds = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int pickCone = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int pickCylinderRay = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int pickCylinderSegment = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int pickPoint = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int pickRay = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int pickSegment = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int pickShape = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int picking = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int pointArray = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int pointArrayRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int pointAttributes = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int pointAttributesRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int pointLight = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int pointLightRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int pointSound = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int pointSoundRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int polygonAttributes = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int polygonAttributesRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int positionInterpolator = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int positionPathInterpolator = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int quadArray = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int quadArrayRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int raster = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int rasterRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int renderAtom = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int renderBin = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int renderBinLock = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int renderMethod = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int renderMolecule = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int renderer = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int rendererStructure = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int renderingAttributes = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int renderingAttributesRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int renderingAttributesStructure = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int renderingEnvironmentStructure = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int restrictedAccessException = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int rotPosPathInterpolator = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int rotPosScalePathInterpolator = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int rotationInterpolator = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int rotationPathInterpolator = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int scaleInterpolator = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int sceneGraphCycleException = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int sceneGraphObject = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int sceneGraphObjectRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int sceneGraphPath = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int screen3D = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int screenViewCache = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int sensor = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int sensorRead = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int setLiveState = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int shape3D = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int shape3DRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int sharedGroup = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int sharedGroupRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int sound = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int soundException = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int soundRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int soundScheduler = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int soundStructure = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int soundscape = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int soundscapeRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int spotLight = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int spotLightRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int structureUpdateThread = !devPhase?NO_DEBUG:NO_DEBUG;

    // switch is a reserved word.
    static final int Switch = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int switchRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int switchValueInterpolator = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int table = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int texCoordGeneration = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int texCoordGenerationRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int text3D = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int text3DRenderMethod = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int text3DRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int texture = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int texture2D = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int texture2DRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int texture3D = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int texture3DRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int textureAttributes = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int textureAttributesRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int textureBin = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int textureRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int textureSetting = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int timerThread = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int transform3D = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int transformGroup = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int transformGroupRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int transformStructure = !devPhase?NO_DEBUG:J3dDebug.LEVEL_3;
    static final int transparencyAttributes = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int transparencyAttributesRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int transparencyInterpolator = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int triangleArray = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int triangleArrayRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int triangleFanArray = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int triangleFanArrayRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int triangleStripArray = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int triangleStripArrayRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int unorderList = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int vertexArrayRenderMethod = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int view = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int viewCache = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int viewPlatform = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int viewPlatformRetained = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int virtualUniverse = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int wakeupAnd = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int wakeupAndOfOrs = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int wakeupCondition = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int wakeupCriteriaEnumerator = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int wakeupCriterion = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int wakeupOnAWTEvent = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int wakeupOnActivation = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int wakeupOnBehaviorPost = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int wakeupOnCollisionEntry = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int wakeupOnCollisionExit = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int wakeupOnCollisionMovement = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int wakeupOnDeactivation = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int wakeupOnElapsedFrames = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int wakeupOnElapsedTime = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int wakeupOnElapsedTimeHeap = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int wakeupOnSensorEntry = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int wakeupOnSensorExit = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int wakeupOnTransformChange = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int wakeupOnViewPlatformEntry = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int wakeupOnViewPlatformExit = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int wakeupOr = !devPhase?NO_DEBUG:NO_DEBUG;
    static final int wakeupOrOfAnds = !devPhase?NO_DEBUG:NO_DEBUG;


    static boolean doDebug(int j3dClassLevel, int level, String str) {
	if(j3dClassLevel >= level) {
	    System.err.print(str);
	    return true;
	}
	return false;
    }

    static boolean doDebug(int j3dClassLevel, int level) {
	if(j3dClassLevel >= level) {
	    return true;
	}
	return false;
    }

    static void doAssert(boolean expr, String str) {
        if (! expr) {
            throw new AssertionFailureException("(" + str + ")" + "is false");
        }
    }

    static void pkgInfo(ClassLoader classLoader,
			String pkgName,
			String className) {

	try {
	    classLoader.loadClass(pkgName + "." + className);

	    Package p = Package.getPackage(pkgName);
	    if (p == null) {
		System.err.println("WARNING: Package.getPackage(" +
				   pkgName +
				   ") is null");
	    }
	    else {
		if(devPhase && debug) {
		    System.err.println(p);
		    System.err.println("Specification Title = " +
				       p.getSpecificationTitle());
		    System.err.println("Specification Vendor = " +
				       p.getSpecificationVendor());
		    System.err.println("Specification Version = " +
				       p.getSpecificationVersion());
		    System.err.println("Implementation Vendor = " +
				       p.getImplementationVendor());
		    System.err.println("Implementation Version = " +
				       p.getImplementationVersion());
		}
		else if(devPhase)
		        System.err.println(", Java 3D " + p.getImplementationVersion() + ".");
	    }
	}
	catch (ClassNotFoundException e) {
	    System.err.println("Unable to load " + pkgName);
	}

	// 	System.err.println();
    }


    static {
	// initialize the debug flag
	debug = false;
    }

}

