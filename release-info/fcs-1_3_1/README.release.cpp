#if 0
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
#endif
Java 3D(TM) 1.3.1
#if defined(WIN32OGL)
README file for Win32/OpenGL

This file contains important information for users of Java 3D(TM).
The first four sections (Requirements, Installation, Shared 
Contexts, and Running Java 3D(TM) in a Browser) are of interest
to all Java 3D(TM) users.  The rest of this file applies only to
developers of Java 3D(TM) applications.

** It is recommended that any previous version of Java 3D(TM) be
** uninstalled before installing this version.
#elif defined(SOLARIS)
README file for Solaris/SPARC/OpenGL

This file contains important information for users of Java 3D(TM).
The first four sections (Requirements, Installation, Shared 
Contexts, and Running Java 3D(TM) in a Browser) are of interest
to all Java 3D(TM) users.  The rest of this file applies only to
developers of Java 3D(TM) applications.

** It is recommended that any previous version of Java 3D(TM) be
** removed before installing this version.
#else
README file for Win32/DirectX

This file contains important information for users of Java 3D(TM).
The first three sections (Requirements, Installation, and Running
Java 3D(TM) in a Browser) are of interest to all Java 3D(TM) users.
The rest of this file applies only to developers of Java 3D(TM)
applications.

** It is recommended that any previous version of Java 3D(TM) be
** uninstalled before installing this version.
#endif
============
REQUIREMENTS
============
#if defined(SOLARIS)
This version of Java 3D(TM) for Solaris/SPARC requires the following:

  Java 2 Runtime or SDK version 1.3.1 or later from Sun Microsystems.
  (http://java.sun.com/j2se/)

  Frame Buffer with OpenGL support (XVR-500, XVR-1000, XVR1200, XVR-4000,
  Expert3D, Elite3D, Creator3D, and PGX).

  Solaris 7 or later.

  OpenGL 1.2.2 for Solaris or later. Depending on type of frame buffer,
  a higher OpenGL version might be needed, such as XVR-1000 requires
  OpenGL 1.2.3. 
  To find your current version, use `pkginfo -l SUNWglrt`
  OpenGL for Solaris can be obtained at 
  http://www.sun.com/solaris/opengl

  PATCHES

  There are no patches required for Solaris 8.  However, Java 2
  versions 1.3.1, 1.4 and 1.4.1 do require patches.  These patches must be
  installed before trying to install Java 3D.


  Solaris 7 requires the Kernel Update patch (106541-06 or later)
  which fixes a bug on multi-processor systems that can lock up
  the window system when running a Java 3D program with more than
  one active window.  Also, Java 2 versions 1.3.1, 1.4 and 1.4.1 require
  patches.  These patches must be installed before trying to install
  Java 3D.


  Use `showrev -p` to list installed patches.  Patches can be
  downloaded for free from http://sunsolve.sun.com/.
#elif defined(WIN32OGL)
This version of Java 3D(TM) for WindowsNT 4.0,
Windows 98, Windows ME and Windows 2000 requires the following:

        Java 2 (Runtime or SDK) version 1.3.1 or later from Sun
	Microsystems (http://java.sun.com/jdk/).  This includes
	the Java Plug-In (JPI) to update Java(TM) in your browser.

        OpenGL 1.1 or later, available from Microsoft or from
        your graphics card manufacturer (see below).

        NT 4.0 only: Service Pack 3 or later.
#elif defined(WIN32D3D)
This version of Java 3D(TM) for Windows 98, Mindows ME and Windows 2000
requires the following:

        Java 2 (Runtime or SDK) version 1.3.1 or later from Sun
	Microsystems (http://java.sun.com/jdk/).  This includes
	the Java Plug-In (JPI) to update Java(TM) in your browser.

        DirectX 8.0 or later, available from Microsoft
        (http://www.microsoft.com/directx/default.asp).
#endif
============
INSTALLATION
============

You must have permission to write files in your Java(TM) Runtime
Environment and/or SDK directories.  If you do not have this
permission, the installer will run to completion but Java 3D(TM)
will not be installed.  Make sure you are running Java(TM) from
your local machine and that you are the same user who installed
Java.
#if defined(SOLARIS)
To install Java 3D(TM), execute the appropriate self-extracting
shell script.

   Runtime only:      java3d-1_3_1-solaris-sparc-rt.bin
   Runtime and SDK:   java3d-1_3_1-solaris-sparc-sdk.bin
   64 bit Runtime:    java3d-1_3_1-solaris-sparcv9.bin

For the two runtime versions, execute the script in the destination
jre directories.  Note: The 64 bit runtime only contains the nessesary
64 bit components.  Install it in the jre directory after installing the 
regular runtime files.

For the runtime and sdk bundle, execute the script in the destination 
sdk directory.

The Java 3D(TM) SDK includes several demo programs that can
verify correct installation.  Assuming your Java 2 SDK is installed
at /usr/j2se, try the following:

        cd /usr/j2se/demo/java3d/HelloUniverse
        java HelloUniverse

        Note: Many more demos are available under the demo/java3d/
              directory.  Some of the demos require a maximum memory
              pool larger than the default in java.  To increase the
              maximum memory pool to 64 megabytes, add the following
	      command line options to java or appletviewer:
                  java: -mx64m
                  appletviewer: -J-mx64m

Make sure you have the SUNWi1of package (optional fonts) installed by
running `pkginfo SUNWi1of`.  If not present, you may see error messages
like this:

    Font specified in font.properties not found
    [-b&h-lucida sans typewriter-bold-r-normal-sans-*-%d-*-*-m-*-iso8859-1]
    ...
#elif defined(WIN32OGL)
To install Java 3D(TM), execute the InstallShield binaries.

   Runtime only:      java3d-1_3_1-windows-i586-opengl-rt.exe
   Runtime and SDK:   java3d-1_3_1-windows-i586-opengl-sdk.exe

The installers upgrade your most recently installed Java Runtime
Environment and SDK.  

The Java 3D(TM) SDK includes several demo programs that can
verify correct installation.  Assuming your Java 2 SDK is installed
at \j2sdk1.4.1, try the following:

        cd \j2sdk1.4.1\demo\java3d\HelloUniverse
        java HelloUniverse

        Note: Many more demos are available under the demo\java3d\
              directory.  Some of the demos require a maximum memory
              pool larger than the default in java.  To increase the
              maximum memory pool to 64 meg, add the following command
              line options to java or appletviewer:
                  java: -mx64m
                  appletviewer: -J-mx64m

If, after installation, you get the following error message while
running a Java 3D program:

  java.lang.UnsatisfiedLinkError: no J3D in shared library path

it is most likely because OpenGL and/or the OpenGL GLU utilities are
not installed on the machine.

For information on OpenGL and how to get it, see the OpenGL web page at
http://www.opengl.org/
#elif defined(WIN32D3D)
To install Java 3D(TM), execute the InstallShield binaries.

   Runtime only:      java3d-1_3_1-windows-i586-directx-rt.exe
   Runtime and SDK:   java3d-1_3_1-windows-i586-directx-sdk.exe

The installers will upgrade your most recently installed Java 
Runtime Environment and SDK.  

The Java 3D(TM) SDK includes several demo programs that can
verify correct installation.  Assuming your Java 2 SDK is installed
at \j2sdk1.4.1, try the following:

        cd \j2sdk1.4.1\demo\java3d\HelloUniverse
        java HelloUniverse

        Note: Many more demos are available under the demo\java3d\
              directory.  Some of the demos require a maximum memory
              pool larger than the default in java.  To increase the
              maximum memory pool to 64 meg, add the following command
              line options to java or appletviewer:
                  java: -mx64m
                  appletviewer: -J-mx64m

#endif
Java 3D(TM) consists of four jar files and three shared libraries.
You do not need to include the jar files in your CLASSPATH,
nor do you need to include the shared libraries in your PATH.
You should include "." in your CLASSPATH or ensure that CLASSPATH
is not set.

Java 3D documentation and tutorials are available from the Java 3D(TM)
Home Page: http://java.sun.com/products/java-media/3D/
#if defined(WIN32OGL) || defined(SOLARIS)
===============
SHARED CONTEXTS
===============

This version of Java 3D is able to use shared contexts in OpenGL for Display
Lists and Texture Objects.  For single canvas applications, there will be no
change in behavior.  For multiple canvas applications, memory requirements
will decrease by using this property.  By default, this property is set
to false. To enable the use of shared contexts set the j3d.sharedctx property
to true, for example:

    java -Dj3d.sharedctx=true MyProgram

#endif
#if defined(WIN32OGL)
Some video cards, such as the Riva TNT & TNT2, have problems using shared
contexts.  If you are experiencing no rendering, crashes, or no textures
being displayed when shared contexts are enabled, this is the most likely
problem.

=================
Background Images
=================

The background image can be rendered in two modes: raster and texture. In windows,
the default mode is texture and in Solaris, the default one is raster.
The property j3d.backgroundtexture can be used to control which mode to use.
If you are experiencing slow rendering of background images,
you can change j3d.backgroundtexture property. For example, to enable
texture mode if your have hardware support for texture rendering, 

	java -Dj3d.backgroundtexture=true MyProgram 
#endif

======================
Compiled Vertex Array
======================

Compiled Vertex Array extension is used in IndexedGeometryArray
when it's USE_COORD_INDEX_ONLY flag is set and it is not in display list mode.
You may disable the use of this extension by setting the new property,
j3d.compliedVertexArray, to false.

Compiled Vertex Array extension is used extensively, on SUN XVR-4000, for
all GeometryArray type when display list mode is not used.
You may disable the use of this extension by setting the new property,
j3d.compliedVertexArray, to false.


===========================
Multisampling Antialiasing
===========================

By default, full scene antialiasing is disabled if a
multisampling pixel format (or visual) is chosen.
To honor a display drivers multisample antialiasing
setting (e.g. force scene antialiasing), set the 
implicitAntialiasing property to true. 
This causes Java3D to ignore its own scene antialias
settings, letting the driver implicitly implement the 
feature. 

      java -Dj3d.implicitAntialiasing=true MyProgram 

================================
RUNNING JAVA 3D(TM) IN A BROWSER
================================
#if defined(WIN32OGL) || defined(WIN32D3D)
You can run Java 3D(TM) programs in your browser.  Java 2(TM)
from Sun includes the Java Plug-In (JPI) to upgrade the Java(TM)
in the browser to Java 2(TM).  To verify proper installation,
point your browser to file:/j2sdk1.4.1/demo/java3d/index.html

If you are getting permission exceptions while running the Java 3D
demo programs in your browser, it may be because of bug 4416056.

First, upgrade your Java SDK to the latest release.  The bug may
have been fixed by the time you read this.

To work around the problem, the path to your JRE must not 
contain spaces.  By default, it does ("c:\Program Files\JavaSoft\. . .").
The Java SDK installer automatically installs the JRE in 
c:\Program Files\JavaSoft, so you must run the installers in
this order:
  Java SDK installer
  Java JRE installer (choose a directory with no spaces)
  Java 3D SDK installer (will default to the correct directories)

NOTE: Many Java 3D(TM) programs will require a larger heap size
than the default 16M in the JPI.  Run the Java Plug-In Control
Panel from Start/Programs (JPI 1.2.2) or Start/Settings/Control 
Panel (JPI 1.3) and in "Java Run Time Parameters" put "mx64m"
for 64M of heap memory.
#elif defined(SOLARIS)
Java 3D programs can be run in Netscape Communicator on Solaris 
with the Java(TM) Plug-IN (JPI) installed:

  Solaris 8 includes Netscape and the JPI.

  For Solaris 7 users, Netscape Communicator may be downloaded
  for free from http://www.sun.com/solaris/netscape.  Patches
  may be required (see the website for details).

  Netscape Communicator 6.0 automatically includes the JPI.

  Java 1.3 and higher include the JPI.  The Control Panel
  will be at $JAVAHOME/jre/ControlPanel.html.

  If you are using Solaris 7, Java 1.2.2, and Netscape 4.51, 4.7,
  or 4.74, you need to download and install the JPI from 
  http://www.sun.com/solaris/netscape/jpis.  NOTE: requires
  membership to the JDC.  More patches may be required
  (see the website for details).  If you install it in
  the default location (/opt/NSCPcom) the control panel
  will be at file:/opt/NSCPcom/j2pi/ControlPanel.html.

You need to set the NPX_PLUGIN_PATH environment variable
to the directory containing the JPI.  For example, you you
are using the JPI from Java 1.4.1 installed in /usr/j2se,
do 'setenv NPX_PLUGIN_PATH /usr/j2se/jre/plugin/sparc'
before starting Netscape.  The default is /opt/NSCPcom.

JDK 1.3.1 and higher have different plugin directories for
Netscape 4.x vs. 6.0.  For Netscape 4.x you need:
  'setenv NPX_PLUGIN_PATH /usr/j2se/jre/plugin/sparc/ns4'
while for 6.0: 
  'setenv NPX_PLUGIN_PATH /usr/j2se/jre/plugin/sparc/ns600'

There is a bug in the Solaris JPI 1.2.2 that keeps it
from finding Java extensions in the lib/ext/ directory
where they are kept.  If you are not using JPI 1.3, you need to
copy the four .jar files from the lib/ext/ directory to the
lib/ directory as follows:

    cd $SDKHOME/jre/lib/ext
    cp j3daudio.jar ..
    cp j3dcore.jar ..
    cp j3dutils.jar ..
    cp vecmath.jar ..

To verify proper installation, point your browser to
file:///usr/j2se/demo/java3d/index.html

If it seems like Java 3D isn't installed, make sure the JPI
is using the JVM you've upgraded with Java 3D(TM).  Run the
JPI Control Panel and click on the "Advanced" tab to change
the JVM associated with the JPI.  Under "Java Run Time
Environment" choose "Other..." and Enter the path to your
JVM in the 'Path:' box (for example,
/usr/j2se/jre).  Press "Apply."  (Note:
choose the directory above the 'bin' directory.)

NOTE: Many Java 3D(TM) programs will require a larger heap size
than the default 16M in the JPI.  In the Control Panel
"Java Run Time Parameters" put "mx64m" for 64M of heap memory.
#endif
To create a web page with Java 3D, you need to use special HTML
code to force the browser to use the JPI VM.  Refer to the
following URL for information on using Java Plug-In "HTML
Converter" and running applets using Java Plug-in:

        http://java.sun.com/products/plugin/

====================================================
DISTRIBUTING Java 3D(TM) WITH YOUR JAVA(TM) PROGRAMS
====================================================

Sun Microsystems allows vendors to distribute the Java 3D(TM) Runtime
environment with their Java programs, provided they follow the terms
of the Java 3D(TM) Binary Code License and Supplemental License Terms
agreement.

This document uses the term "vendors" to refer to licensees,
developers, and independent software vendors (ISVs) who license and
distribute Java 3D(TM) with their Java programs.

REQUIRED vs. OPTIONAL FILES
---------------------------
Vendors must follow the terms of the Java 3D(TM) Evaluation License
agreement, which includes these terms:

 - Don't arbitrarily subset Java 3D(TM). You may, however, omit those
   files that have been designated below as "optional".

 - Include in your product's license the provisions called out
   in the Java 3D(TM) Evaluation License.

BUNDLING Java 3D(TM)
--------------------
Java 3D(TM) comes with its own installer that makes it suitable for
downloading by end users. Java(TM) application developers have the
option of not bundling Java 3D(TM) with their software.  Instead,
they can direct end-users to download and install the Java 3D(TM)
software themselves.

Required Files
--------------
#if defined(SOLARIS)
When bundling Java 3D(TM) with your application, the following files
must be included (Solaris):

        <JREDIR>/lib/sparc/libJ3D.so
        <JREDIR>/lib/sparc/libj3daudio.so
        <JREDIR>/lib/sparc/libJ3DUtils.so
        <JREDIR>/lib/ext/vecmath.jar
        <JREDIR>/lib/ext/j3dcore.jar
        <JREDIR>/lib/ext/j3daudio.jar
        <JREDIR>/lib/ext/j3dutils.jar
#elif defined(WIN32OGL) || defined(WIN32D3D)
        <JREDIR>\bin\J3D.dll
        <JREDIR>\bin\j3daudio.dll
        <JREDIR>\bin\J3DUtils.dll
        <JREDIR>\lib\ext\vecmath.jar
        <JREDIR>\lib\ext\j3dcore.jar
        <JREDIR>\lib\ext\j3daudio.jar
        <JREDIR>\lib\ext\j3dutils.jar
#endif
Optional Files
--------------

An application developer may include these files and directories
with their Java 3D(TM) application, but is not required to do so:
#if defined(SOLARIS)
        <JDKDIR>/j3d-utils-src.jar
        <JDKDIR>/demo/java3d
#elif defined(WIN32OGL) || defined(WIN32D3D)
        <JDKDIR>\j3d-utils-src.jar
        <JDKDIR>\demo\java3d
#endif

========================
CHANGES SINCE 1.3
========================

============
NEW FEATURES
============

  A set of new methods is added to the Viewer, a utility class, to 
  support dynamic video resize, specificially targeted for SUN  
  framebuffer : XVR-4000.
  Dynamic video resize is a new feature in Java 3D 1.3.1.   
  This feature provides a means for doing swap synchronous resizing
  of the area that is to be magnified (or passed through) to the
  output video resolution. This functionality allows an application
  to draw into a smaller viewport in the framebuffer in order to reduce
  the time spent doing pixel fill. The reduced size viewport is then 
  magnified up to the video output resolution using the SUN_video_resize
  extension. This extension is only implemented in XVR-4000 and later
  hardware with back end video out resizing capability.
 
 

=======================
Constructing a Canvas3D
=======================

Many Java 3D programs pass null to the Canvas3D constructor.  By doing
this, Java 3D will select a default GraphicsConfiguration that is
appropriate for Java 3D.  However, this is a bad practice, and can lead
to errors when applications try to run in alternate environments, such as
stereo viewing.  Java 3D will now print out a warning if the Canvas3D
constructor is passed in a null argument for the GraphicsConfiguration.

====================================
Multipass Texture support limitation
====================================
If an application has setup more texture unit states than the graphics 
hardware can support, COMBINE mode will not be supported and Java 3D will 
fallback to the REPLACE mode.

=========
Utilities
=========

This release includes utilities for Java 3D.  These utilities are still
being defined and under development.  Much of the source for these utilities
is also provided.  The API for these utilities may change in future releases.

The following utilities are provided in this release:

        - Some predefined Mouse based behaviors
        - Picking utilities including predefined picking behaviors
        - Geometry creation classes for Box, Cone, Cylinder, and Sphere
        - A Text2D utility
        - Universe Builders - SimpleUniverse and ConfiguredUniverse
        - An Image Loading utility
        - A Normal Generator utility
        - A Polygon Triangulator utility
        - Triangle stripifier
        - Geometry compression utilities
        - Spline-based path interpolators
        - Wavefront .obj loader
        - Lightwave 3D File Loader
        - A scenegraph io utility
        - A high resolution interval timer
   
===================================
Enabling Stereo with SimpleUniverse
===================================

The SimpleUniverse utility does not, by default, request a
GraphicsConfiguration that is capable of Stereo rendering.  To enable this,
you need to set a property when running your application.  Here is an
example.

java -Dj3d.stereo=PREFERRED MyProgram

Some framebuffers only have one Z buffer and share this between the left
and right eyes.  If you are experiencing problems using stereo try the
following property:

java -Dj3d.stereo=PREFERRED -Dj3d.sharedstereozbuffer=true MyProgram

#if defined(SOLARIS)
=========================================================
Support for disabling X11 Xinerama mode in Solaris OpenGL
=========================================================

Solaris OpenGL is well optimized in general for single-threaded applications
running in the Xinerama virtual screen environment, but there are two
situations in which significant degradations in graphics performance may be
experienced by multi-threaded OpenGL clients such as Java 3D applications.

The first is when using GeometryByReference, which is implemented through the
use of OpenGL vertex arrays.  This is essentially treated as immediate mode
data by Solaris OpenGL, which in multi-threaded mode incurs the expense of
copying data for multiple graphics pipelines even when a single window on a
single screen is being used.

The second is for applications using multiple Canvas3D objects.  The X11
Xinerama extension internally creates separate graphics resources on each
physical screen for each window created on the virtual screen.  This causes
significant overhead for multi-threaded Solaris OpenGL clients.

Java 3D provides a new property in this release, j3d.disableXinerama, which
when set to true will disable the use of Xinerama features by Solaris OpenGL.
This increases performance in the two situations up to 75%, equivalent to that
of running in a non-Xinerama environment.

The drawback of setting this property is that when moving a Canvas3D from one
physical screen to another the graphics rendering will be clipped to the
physical limits of the original screen on which it was created.  The property
is primarily intended to benefit fullscreen applications using multiple
physical screens or fullscreen applications using GeometryByReference.  To use
it, specify the property value on the command line:

    java -Dj3d.disableXinerama=true <args> <application class name> <args>

Disabling Xinerama requires both JDK 1.4 or later and Solaris OpenGL 1.2.2 or
later.  Solaris 7 and 8 must be upgraded to the patch level required by JDK
1.4.
#endif
#if defined(WIN32D3D)
=====================================================
Information on the Direct3D Implementation of Java 3D
=====================================================

  Unsupported Features
  --------------------
  The following features are currently unsupported in the Direct3D
  implementation of Java 3D:
    Line width
    Line antialiasing
    Point antialiasing
    PolygonAttributes backFaceNormalFlip
    RenderingAttributes ROP_XOR
    Stereo

  Texture features not support:
    Texture color table
    Base/Maximum Level
    Max/Minimum LOD
    LOD offset
    Detail Texture
    Sharpen Texture Function
    Filter4 Function
    Boundary width 
    Boundary mode CLAMP_TO_EDGE & CLAMP_TO_BOUNDARY 
    (will fall back to CLAMP)
    Texture blend color when multiple pass is used
    to simulate multiTexture.
 
  Limited Support
  ---------------
  FullScreen antialiasing is supported only if the device returns
  D3DPRASTERCAPS_ANTIALIASSORTINDEPENDENT in its raster capabilities
  list.  (OpenGL supports fullscreen antialiasing if an accumulation
  buffer exists.)

  TransparencyAttributes.TransparencyMode values FASTEST, NICEST and
  SCREEN_DOOR are the same as BLENDED under D3D.

  DepthComponent in certain display mode when stencil buffer
  is selected (use for DecalGroup) since depth Buffer Component
  Read/write can't coexist with Stencil buffer in DirectX8.0.

  OffScreen rendering with width/height > current desktop size
  
  Texture coordinates outside the range [0,1] when boundaryModeS
  and boundaryModeT are set to CLAMP will not use the Texture Boundary'
  color unless BASE_LEVEL_LINEAR filtering is turned on.

  If the driver did not expose D3DPTADDRESSCAPS_BORDER capability bit
  (viathe directX SDK utility Caps Viewer). Then it didn't support 
  Texture Border color mode. Default color as in Clamp mode will be
  used in this case. Most driver currently available need to
  workaround using reference mode (-Dj3d.d3dDevice=Reference).

  if the driver did not expose D3DPMISCCAPS_LINEPATTERNREP capability
  bit then it didn't support line patterns. A solid line will be shown
  in this case. Most driver currently available need to workaround 
  using reference mode.  

  Only negative polygon offsets are supported.  The limit of this offset
  corresponds to the depth of the z-buffer.

  Float anisotripic filter degree did not support. They will round off 
  to integer value and pass down to the DirectX library.

  Texture environment combiner :

  COMBINE_REPLACE     - Support if driver expose TextureOpCaps
                        D3DTOP_SELECTARG1. Only combine scale 1 is support.
  COMBINE_MODULATE    - Support if driver expose TextureOpCaps D3DTOP_MODULATE, 
                        D3DTOP_MODULATE2X,  D3DTOP_MODULATE4X for scale 1, 2 & 4
                        respectively.
  COMBINE_ADD         - Support if driver expose TextureOpCaps D3DTOP_ADD, 
                        only combine scale 1 is support
  COMBINE_ADD_SIGNED  - Support if driver expose TextureOpCaps D3DTOP_ADDSIGNED,
                        D3DTOP_ADDSIGNED2X for scale 1 & 2 respectively. Combine
                        scale 4 will fall back to 2.
  COMBINE_SUBTRACT    - Support if driver expose TextureOpCaps D3DTOP_SUBTRACT.
                        Only combine scale 1 is support.               
  COMBINE_INTERPOLATE - Support if driver expose TextureOpCaps D3DTOP_LERP.
                        Only combine scale 1 is support.               
  COMBINE_DOT3        - Support if driver expose TextureOpCaps
                        D3DTOP_DOTPRODUCT3 Only combine scale 1 is support.               

  Rendering Different between OGL & D3D
  --------------------------------------
  - SpotLight formula is different, OGL will normally have a bigger bright spot.

  - TexCoordGeneration Sphere Map formula is different, OGL will normally 
    has texture map that zoom closer than D3D.

  - Specular hightlight in Texture mapping mode may different. If OGL driver 
    support separate specular color, then resulting specular highlight in
    texture are the same. Otherwise D3D verson will have a brighter and
    more obviously specular highlight compare with OGL.


  Fullscreen Support
  ------------------------
  The Direct3D implementation of Java 3D can be run in fullscreen mode.
  To start an application in fullscreen mode, use the property j3d.fullscreen.
  The values for j3d.fullscreen are:
    REQUIRED  -   A fullscreen canvas is required. If not available, do not
                  start up the application.
    PREFERRED -   A fullscreen canvas is desired for this invocation of the
                  Java 3D application.  If a fullscreen canvas cannot be
                  created, a windowed canvas is acceptable. 
    UNNECESSARY - States that a fullscreen canvas is not necessary for this
                  application.  Create a windowed application (this is the
                  same behavior as not setting this property).

  Example:
    java -Dj3d.fullscreen=REQUIRED HelloUniverse

  Further, an application can be toggled between fullscreen and windowed mode
  by use of the <Alt><Enter> keyboard combination. 

  When using JDK1.4 fullscreen API with Java3D DirectX version, it is
  necessary to set the following property :

   -Dsun.java2d.noddraw=true 

  Direct3D Driver Selection
  -------------------------

  When there is more then one device associated with a
  monitor, by default, Java 3D uses the first driver found
  on a given display.  This order can be found by
  using the -Dj3d.debug=true property during Java startup.

  In order to use a 3D only graphics card such as Voodoo1/2
  A new property has been added

   -Dj3d.d3ddriver=idx  

  where idx is the driver order number (starting with 1) found
  using the debug property above.  This will force Java 3D to use
  the driver specified by the user (this may fail if the driver is
  not compatible with the display).  For a typical setup with a 3D
  only card attached to a single monitor system, use idx=2.  This
  will automatically toggle to fullscreen hardware acceleration mode.


  Direct3D Device Selection
  -------------------------
  In order to aid in development and debugging, Java 3D has added a property
  to allow the D3DDevice to use for rendering to be selected.  The property,
  j3d.d3ddevice can have the following values:
    tnlhardware - select a device that supports transform and lighting in
                  hardware, if present.  If no such device is present,
                  a dialog box is displayed to alert the user and the
                  application will exit.
    hardware    - select a Direct3D device that performs hardware
                  rasterization, if present.  If no such device is present,
                  a dialog box is displayed to alert the user and the
                  application will exit.
    reference   - use the Direct3D reference pipeline (only available if
                  the Direct3D SDK is installed).

  By default Java 3D first tries to select a TnLhardaware device, if that fails
  a Hardware device, if that also fails then finally Java 3D selects an
  Emulation device.


  Ignored Java 3D Properties
  --------------------------
  The following Java 3D properties are ignored by the Direct3D implementation:
    j3d.sharedctx
    j3d.stereo
    j3d.sharedstereozbuffer
    j3d.g2ddrawpixel 
    j3d.usecombiners
    j3d.disableSeparateSpecular
    j3d.backgroundtexture
    j3d.disableXinerama
#endif

===================================================
Information on Java 3D Audio Device Implementations
===================================================

Java 3D sound is rendered via the use of a specific implementation
of the AudioDevice3D interface.   This release includes two AudioDevice3DL2
implementations: HeadspaceMixer and JavaSoundMixer.  Both of these
implementations are included in the j3daudio.jar.

Please read README.release in program examples Sound directory for details
regarding the feature and format limitations of each of these implementations
and for examples of these use.

=============================================
HeadspaceMixer AudioDevice3DL2 Implememtation
=============================================

 The HeadspaceMixer implementation is part of the Sun Java 3D
 com.sun.j3d.audioengines.headspace package.  This implementation
 uses a version of the Headspace Audio Engine licensed from Beatnik
 which does all rendering in software and pipes the stereo audio image
 to the platform's audio device.

 The implemention that was called JavaSoundMixer in previous Sun
 releases of Java 3D has been renamed to HeadspaceMixer.
 It was renamed in anticipation of the release of a new AudioDevice
 implementation that uses the JavaSound API which will be called
 JavaSoundMixer (described below).

 The HeadspaceMixer audio device will be created and initialized when the
 utility SimpleUniverse.Viewer.createAudioDevice() method is called.
 If your application uses this utility, no change will be required to
 use the recommended HeadspaceMixer implementation.

 If your application explicitly used the older JavaSoundMixer audio device
 implemention from the package com.sun.j3d.audioengines.javasound, you should
 change the reference to JavaSoundMixer, at least for this release,
 to HeadspaceMixer:

     import com.sun.j3d.audioengines.headspace.HeadspaceMixer;
             :
     HeadspaceMixer mixer = new HeadspaceMixer(physicalEnvironment);

 Most of the Java 3D Audio features have been implemented but there are
 a few exceptions.  Additionally, some Java 3D Audio features are only
 only partially implemented.  Please read the README.release document in
 programs/examples/Sound for more information.

 Note that the HeadspaceMixer is not supported in the 64 bit Solaris 
 version of Java 3D.

=============================================
JavaSoundMixer AudioDevice3DL2 Implememtation
=============================================
 
  The JavaSoundMixer implementation is part of the Sun Java 3D
  com.sun.j3d.audioengines.javasound package.  This implementation uses
  the Java Sound API.  All low-level access to the platforms audio device
  are dependent on the Java Sound mixer implementation(s) installed on
  the machine you're running on.
 
  The JavaSoundMixer Java 3D audio device implementation uses Java Sound
  SourceDataLine streams for non-cached data and Java Sound Clips for
  cached data.  Support for specific sound cards, the exact input formats
  that can be passed as data to Java 3D MediaContainers, and which feature
  are rendered in software verses accelleration hardware is dependent on
  the Java Sound implementation installed on your machine.
  There is guarenteed to be at least one Java Sound mixer implementation
  available with all J2SE releases (such as Sun's JDK 1.3 and above).
  Please read the README.release document in programs/examples/Sound.

==========
BUGS FIXED
==========

Core Graphics and Vecmath
-------------------------
4685686 Apps Particles sometimes throws ArrayOfBoundsException at GeometryArrayRetained
4794994 Memory leak when SharedGroup removed 
4792478 ArrayIndexOutOfBoundsException with two ViewSpecificGroups with Lights
4793926 Incorrect collison report when geometry close to each other 
4794382 NullPointerException if SharedGroup not set in Link under SwitchGroup 
4798443 RenderBin findOrderedCollection throws IndexOutOfBoundsException 
4800640 D3D: Garbage line appear in TexCubeMap negative & postive Y surface
4805797 View setLocalEyeLightingEnable() does not work
4807209 View setMinimumFrameCycleTime() fail to free CPU time for other applications
4809037 OGL: glLockArrayEXT() should invoke after vertex pointer defined
4826575 J3D fail to run on JDK1.5
4829457 Missing object when lighting change in SDSC ExHenge
4829458 Texture stage fail to disable in accelerated mode for multiTexture apps
4836232 TextureUnitState setTextureAttributes() & setTexCoordGeneration() may not work
4838311 D3D: TextureAttributes in texture stage need to reset for multitexture
4839757 OGL: Incorrect rescale normal extension use for non-uniform scale
4840952 TransformGroupRetained throws NullPointerException if sharedGroup link set null
4843212 AWTEvent disable if canvas remove from panel and add back later after SG Live
4843221 GraphicsContext3D flush(true) hangs for non-visible Canvas3D
4846822 NullPointerException in MasterControl addFreeImageUpdateInfo

Utilities
---------

4331669 setRectangleScaleFactor will not change text size unless setString called (doc?)
4780878 ConfiguredUniverse needs a way to access multiple behaviors
4801176 Sphere Texture map reverse when GENERATE_NORMALS_INWARD is used
4803241 EdgeTable & Edge.java use by NormalGenerator missing in java3d-utils-src.jar
4822946 Picking throws NullPointerException for BoundingBox/Sphere/Polytope PickShape 
4822988 SceneGraphIO throws NullPointerException when Morph Node is read
4827900 TransformInterpolatorState source missing in j3d-utils-src.jar
4830842 Triangulator fails for polygons with holes in YZ or XZ plane

==============
KNOWN PROBLEMS
==============

To get the very latest list of known Java 3D bugs, look on the Java
Bug Parade (http://developer.java.sun.com/developer/bugParade/index.html)

Documentation Bugs
------------------

4303056  Docs should specify thread-safety behavior of Java 3D methods
4350033  JFTC: possible conflict between implementation and spec on PolygonOffset
4391492  Rotation matrix of Transform3D constructor not extract
4514880  results of changing geometry and texture are not well documented
4632391  Typo in doc j3d_tutorial_ch2.pdf
4698350  Spec. did not mention alpha component for Texture Mode REPLACE, MODULATE clearly

Core Graphics and Vecmath
-------------------------

4509357  example program - raster image incorrect until mouse moved into window
4512179  Undeterminable behavior caussed by Appearance.setTexture
4516005  AddRemoveCanvas2 fail to show cube intermittently
4518080  Light scoping sometimes not working for compiled geometry
4529297  TCK: Group.removeAllChildren() inconsistent with expected behavior
4667088  sas applications gets VerifyError running with 64-bit JVM
4669211  SharedGroup.getLinks().length is always zero for non-live Link node.
4674146  Background texture fail to render for RenderedImage and byref ImageComponent2D
4674843  ImageComponent3D byReference always make an internal copy
4676035  Off screen rendering has off-center view
4676483  Geometry by Reference change alpha color component of user data
4680305  Detaches of SharedGroups from user threads is not Mt-Safe
4681750  Texture3D throws ArrayIndexOutOfBoundsException when scaleImage
4681863  OGL: OffScreen canvas ignore GraphicsConfigTemplate under windows
4684405  j3d holds a reference to user's bounds (via setBounds()) for use in getBounds().
4684807  NullPointerException in NodeComponent during setAppearance()
4686527  Deadlock between MasterControl and user thread when using ByRef updateData()
4697155  ByRef USE_COORD_INDEX_ONLY geometry not yet implement for optimizeForSpace=false
4701430  Infrequent NPE at RenderBin.java:544
4705053  OrientedPtTest example program displays frame lag
4712205  Window panels disappear when BranchGroup.compile() is used.
4714426  compile() removes null child eventhough ALLOW_CHILDREN_READ is set.
4720938  IndexedGeometry shouldn't consider vertex not reference by index in computeBound
4736484  Big alpha value in byRefColor render geometry even though transparency = 1.0
4740086  Picking cause lots of GC in PickShape intersect() routine
4751162  View TRANSPARENCY_SORT_GEOMETRY throws NullPointerException when viewpoint move
4751283  Transform3D.normalize() and Matrix4d.get(Matrix3d) permute matrix columns
4753957  Morph only consider first GeometryArray when compute bounds
4762021  Transform3D setScale() fail to return negative scale in some case
4762753  Precision problem of OrientedShape3D.ROTATE_ABOUT_POINT if far away from origin
4768237  RuntimeException in pickIntersection.getPointNormal()
4768353  JBrawl does not run smoothly with > 2 cpus
4774341  Locale need a wait between changing HiRes and adding branch graph
4782718  NPE if boundingLeaf in SchedulingBoundLeaf not attach to scenegraph
4783638  WakeupOnAWTEvent does not support MouseWheelEvent
4789101  J3D.dll is accessing jniGetObjectClass inside the critical region
4790016  PickObject generatePickRay return wrong PickShape if View compatibility enable
4794998  hashKey output TROUBLE message when OutOfMemory
4828096  Morph doesn't work correctly with Java3D 1.3
4828098  Morph doesn't use its weights, when it was cloned with cloneTree()

Sound
-----

4634751  BackgroundSound fails to activates with the view intersects it's bounds.
4680280  JavaSoundMixer play sound only once
4760772  BackgroundSounds not looping with HeadspaceMixer mixer

Utility Bugs
------------

4717595  SceneGraph IO bug in J3DFly
4718786  Incorrect coefficients in CubicSplineSegment computeCommonCoefficients()
4805076  Transform3D.get(Matrix3f ) occasionally returns incorrect values

#if defined(SOLARIS)
Solaris-specific Bugs
---------------------
none
#elif defined(WIN32OGL)
Windows/OGL-specific Bugs
---------------------
none

#elif defined(WIN32D3D)
Direct3D specific Bugs
----------------------
  Problem in the NVidia GForce
  -----------------------------
  Make sure you have the latest driver from  http://www.nvidia.com/

  ModelClip did not work under DirectX8.0 debug build, use
  Eumulation mode to workaround this.
#endif
