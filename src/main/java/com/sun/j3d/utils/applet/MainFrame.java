/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 */

// MainFrame - run an Applet as an application
//
// Copyright (C) 1996 by Jef Poskanzer <jef@acme.com>.  All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
// OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
// LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
// OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.
//
// Visit the ACME Labs Java page for up-to-date versions of this and other
// fine Java utilities: http://www.acme.com/java/

// ---------------------------------------------------------------------

package com.sun.j3d.utils.applet;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;
import java.util.*;

/// Run an Applet as an application.
// <P>
// Using this class you can add a trivial main program to any Applet
// and run it directly, as well as from a browser or the appletviewer.
// And unlike some versions of this concept, MainFrame implements both
// images and sound.
// <P>
// Sample main program:
// <BLOCKQUOTE><PRE>
// public static void main( String[] args )
//     {
//     new Acme.MainFrame( new ThisApplet(), args, 400, 400 );
//     }
// </PRE></BLOCKQUOTE>
// The only methods you need to know about are the constructors.
// <P>
// You can specify Applet parameters on the command line, as name=value.
// For instance, the equivalent of:
// <BLOCKQUOTE><PRE>
// &lt;PARAM NAME="pause" VALUE="200"&gt;
// </PRE></BLOCKQUOTE>
// would just be:
// <BLOCKQUOTE><PRE>
// pause=200
// </PRE></BLOCKQUOTE>
// You can also specify three special parameters:
// <BLOCKQUOTE><PRE>
// width=N          Width of the Applet.
// height=N         Height of the Applet.
// barebones=true   Leave off the menu bar and status area.
// </PRE></BLOCKQUOTE>
// <P>
// <A HREF="/resources/classes/Acme/MainFrame.java">Fetch the software.</A><BR>
// <A HREF="/resources/classes/Acme.tar.Z">Fetch the entire Acme package.</A>

public class MainFrame extends Frame implements 
                   Runnable, AppletStub, AppletContext
{

    private String[] args = null;
    private static int instances = 0;
    private String name;
    private boolean barebones = true;
    private Applet applet;
    private Label label = null;
    private Dimension appletSize;

    private static final String PARAM_PROP_PREFIX = "parameter.";

    /// Constructor with everything specified.
    public MainFrame(Applet applet, String[] args, 
		     int width, int height) {
	build(applet, args, width, height);
    }

    /// Constructor with no default width/height.
    public MainFrame(Applet applet, String[] args ) {
	build(applet, args, -1, -1);
    }

    /// Constructor with no arg parsing.
    public MainFrame(Applet applet, int width, int height) {
	build( applet, null, width, height );
    }

    // Internal constructor routine.
    private void build(	Applet applet, String[] args, 
			int width, int height) {
	++instances;
	this.applet = applet;
	this.args = args;
	applet.setStub( this );
	name = applet.getClass().getName();
	setTitle( name );

	// Set up properties.
	Properties props = System.getProperties();
	props.put( "browser", "Acme.MainFrame" );
	props.put( "browser.version", "11jul96" );
	props.put( "browser.vendor", "Acme Laboratories" );
	props.put( "browser.vendor.url", "http://www.acme.com/" );

	// Turn args into parameters by way of the properties list.
	if ( args != null )
	    parseArgs( args, props );

	// If width and height are specified in the parameters, override
	// the compiled-in values.
	String widthStr = getParameter( "width" );
	if ( widthStr != null ) {
	    width = Integer.parseInt( widthStr );
	}

	String heightStr = getParameter( "height" );
	if ( heightStr != null ) {
	    height = Integer.parseInt( heightStr );
	}

	// Were width and height specified somewhere?
	if ((width == -1) || (height == -1)) {
	    System.err.println( "Width and height must be specified." );
	    return;
	}

	// Do we want to run bare-bones?
	String bonesStr = getParameter( "barebones" );
	if ((bonesStr != null) && bonesStr.equals( "true" )) {
	    barebones = true;
	}

	// Lay out components.
	setLayout( new BorderLayout() );
	add( "Center", applet );

	// Set up size.
	pack();
	validate();
	appletSize = applet.getSize();
	applet.setSize( width, height );
	setVisible(true);


	/*
	  Added WindowListener inner class to detect close events.
	*/
	SecurityManager sm = System.getSecurityManager();
	boolean doExit = true;

	if (sm != null) {
	    try {
		sm.checkExit(0);
	    } catch (SecurityException e) {
		doExit = false;
	    }
	}

	final boolean _doExit = doExit;

	addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent winEvent) {
		if (MainFrame.this.applet != null) {
		    MainFrame.this.applet.destroy();
		}
		Window w = winEvent.getWindow();
		w.hide();
		try {
		    w.dispose();
		} catch (IllegalStateException e) {}

		if (_doExit) {
		    System.exit(0);
		}
	    }
	});

	// Start a separate thread to call the applet's init() and start()
	// methods, in case they take a long time.
	(new Thread( this )).start();
    }
    
    // Turn command-line arguments into Applet parameters, by way of the
    // properties list.
    private static void parseArgs( String[] args, Properties props) {
	String arg;

	for (int i = 0; i < args.length; ++i) {
	    arg = args[i];
	    int ind = arg.indexOf( '=' );
	    if ( ind == -1 ) {
		props.put(PARAM_PROP_PREFIX + arg.toLowerCase(), "" );
	    } else {
		props.put(PARAM_PROP_PREFIX + arg.substring( 0, ind ).toLowerCase(),
			  arg.substring( ind + 1 ) );
	    }
	}
    }

    // Methods from Runnable.

    /// Separate thread to call the applet's init() and start() methods.
    public void run() {
	showStatus( name + " initializing..." );
	applet.init();
	validate();
	showStatus( name + " starting..." );
	applet.start();
	validate();
	showStatus( name + " running..." );
    }


    // Methods from AppletStub.
    public boolean isActive() {
	return true;
    }
    
    public URL getDocumentBase() {
	// Returns the current directory.
	String dir = System.getProperty( "user.dir" );
	String urlDir = dir.replace( File.separatorChar, '/' );
	try {
	    return new URL( "file:" + urlDir + "/");
	} catch ( MalformedURLException e ) {
	    return null;
	}
    }
    
    public URL getCodeBase() {
	// Hack: loop through each item in CLASSPATH, checking if
	// the appropriately named .class file exists there.  But
	// this doesn't account for .zip files.
	String path = System.getProperty( "java.class.path" );
	Enumeration st = new StringTokenizer( path, ":" );
	while ( st.hasMoreElements() ) {
	    String dir = (String) st.nextElement();
	    String filename = dir + File.separatorChar + name + ".class";
	    File file = new File( filename );
	    if (file.exists()) {
		String urlDir = dir.replace( File.separatorChar, '/' );
		try {
		    return new URL( "file:" + urlDir + "/" );
		} catch (MalformedURLException e) {
		    return null;
		}
	    }
	}
	return null;
    }
    
    public String getParameter(String name) {
	// Return a parameter via the munged names in the properties list.
	return System.getProperty( PARAM_PROP_PREFIX + name.toLowerCase() );
    }
    
    public void appletResize(int width, int height) {
	// Change the frame's size by the same amount that the applet's
	// size is changing.
	Dimension frameSize = getSize();
	frameSize.width += width - appletSize.width;
	frameSize.height += height - appletSize.height;
	setSize( frameSize );
	appletSize = applet.getSize();
    }

    public AppletContext getAppletContext() {
	return this;
    }
    

    // Methods from AppletContext.
    public AudioClip getAudioClip( URL url ) {
	// This is an internal undocumented routine.  However, it
	// also provides needed functionality not otherwise available.
	// I suspect that in a future release, JavaSoft will add an
	// audio content handler which encapsulates this, and then
	// we can just do a getContent just like for images.
	return new sun.applet.AppletAudioClip( url );
    }

    public Image getImage( URL url ) {
	Toolkit tk = Toolkit.getDefaultToolkit();
	try {
	    ImageProducer prod = (ImageProducer) url.getContent();
	    return tk.createImage( prod );
	} catch ( IOException e ) {
	    return null;
	}
    }
    
    public Applet getApplet(String name) {
	// Returns this Applet or nothing.
	if (name.equals( this.name )) {
	    return applet;
	}
	return null;
    }
    
    public Enumeration getApplets() {
	// Just yields this applet.
	Vector v = new Vector();
	v.addElement( applet );
	return v.elements();
    }
    
    public void showDocument( URL url )	{
	// Ignore.
    }
    
    public void showDocument( URL url, String target ) {
	// Ignore.
    }
    
    public void showStatus( String status ) {
	if (label != null) {
	    label.setText(status);
	}
    }

    public void setStream( String key, java.io.InputStream stream ) {
	throw new RuntimeException("Not Implemented");
	// TODO implement setStream method
    }

    public java.io.InputStream getStream( String key ) {
	throw new RuntimeException("Not Implemented");
	// TODO implement getStream method
    }

    public java.util.Iterator getStreamKeys() {
     		throw new RuntimeException("Not Implemented");
 	// TODO implement getStreamKeys method
    }
}
