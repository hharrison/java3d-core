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

package com.sun.j3d.utils.scenegraph.io.state.com.sun.j3d.utils.universe;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Enumeration;
import java.util.ArrayList;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.HiResCoord;
import javax.media.j3d.Locale;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.vecmath.Matrix4d;
import com.sun.j3d.utils.universe.MultiTransformGroup;
import com.sun.j3d.utils.universe.ViewingPlatform;
import com.sun.j3d.utils.universe.ViewerAvatar;
import com.sun.j3d.utils.universe.PlatformGeometry;
import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;
import com.sun.j3d.utils.scenegraph.io.state.javax.media.j3d.SceneGraphObjectState;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ConfiguredUniverse;

public class SimpleUniverseState extends java.lang.Object {

    private SimpleUniverse universe=null;
    private Controller control;
    private ArrayList localeBGs;
    private int totalBGs=0;
    private PlatformGeometryState platformGeom;
    private ViewerAvatarState viewerAvatar;
    
    /**
     * Creates new SimpleUniverseState for writing.
     */
    public SimpleUniverseState( ConfiguredUniverse universe, Controller control ) {
        this.universe = universe;
        this.control = control;
    }
    
    /**
     * Creates new SimpleUniverseState for writing.
     */
    public SimpleUniverseState( SimpleUniverse universe, Controller control ) {
        this.universe = universe;
        this.control = control;
    }
    
    /**
     * Creates new SimpleUniverseState for reading.
     */
    public SimpleUniverseState( Controller control ) {
        this.control = control;
    }
    
    public void writeObject( DataOutput out ) throws IOException {
        MultiTransformGroup mtg = universe.getViewingPlatform().getMultiTransformGroup();
        int mtgSize = mtg.getNumTransforms();
        out.writeInt( mtgSize );

        // Store the matrix from each MTG transform
        Transform3D trans = new Transform3D();
        Matrix4d matrix = new Matrix4d();
        for(int i=0; i<mtgSize; i++) {
            TransformGroup tg = mtg.getTransformGroup( i );
            tg.getTransform( trans );
            trans.get( matrix );
            control.writeMatrix4d( out, matrix );            
        }
        
        control.writeObject( out, control.createState( universe.getViewingPlatform().getPlatformGeometry() ));
        control.writeObject( out, control.createState( universe.getViewer().getAvatar() ));
        
        writeLocales( out );
    }
    
    public void readObject( DataInput in, Canvas3D canvas ) throws IOException {
        int mtgSize = in.readInt();         // MultiTransformGroup size
	if ( canvas!=null) {
	    universe = new ConfiguredUniverse( canvas, mtgSize);
	} else {
	    universe = new ConfiguredUniverse( ConfiguredUniverse.getConfigURL(), mtgSize);
	}
        MultiTransformGroup mtg = universe.getViewingPlatform().getMultiTransformGroup();
        
        // Read and set the matrix for each MTG transfrom
        Matrix4d matrix = new Matrix4d();
        for(int i=0; i<mtgSize; i++) {
            TransformGroup tg = mtg.getTransformGroup( i );
            matrix = control.readMatrix4d( in );
            Transform3D trans = new Transform3D( matrix );
            tg.setTransform( trans );            
        }

        SceneGraphObjectState tmp = control.readObject(in);
        
        if (tmp instanceof PlatformGeometryState)
            platformGeom = (PlatformGeometryState)tmp;
        else
            platformGeom = null;
        
        tmp = control.readObject(in);
        if (tmp instanceof ViewerAvatarState)
            viewerAvatar = (ViewerAvatarState)tmp;
        else
            viewerAvatar = null;
        
        readLocales( in );
    }
    
    private void writeLocales( DataOutput out ) throws IOException {
    
        Enumeration allLocales = universe.getAllLocales();
        out.writeInt( universe.numLocales() );
        localeBGs = new ArrayList( universe.numLocales() );
        int currentLocale = 0;
        int graphID = 0;
        while( allLocales.hasMoreElements() ) {
            Locale locale = (Locale)allLocales.nextElement();
            HiResCoord hiRes = new HiResCoord();
            writeHiResCoord( out, hiRes );
            int bgs[];
            if (currentLocale==0)
                bgs = new int[locale.numBranchGraphs()-1];      // Disregard ViewingPlatform
            else
                bgs = new int[locale.numBranchGraphs()];
            out.writeInt( bgs.length );
            int count=0;
            Enumeration e = locale.getAllBranchGraphs();
            while( e.hasMoreElements() ) {
                BranchGroup bg = (BranchGroup)e.nextElement();
                if (!(bg instanceof ViewingPlatform)) {
                    control.getSymbolTable().addBranchGraphReference( bg, graphID );
                    bgs[count] = graphID++;
                    out.writeInt( bgs[count] );
                    count++;
                    totalBGs++;
                }
            }
            localeBGs.add( bgs );
        }
    }
    
    private void readLocales( DataInput in ) throws IOException {
        int numLocales = in.readInt();
        localeBGs = new ArrayList( numLocales );
        Locale locale;
        for(int i=0; i<numLocales; i++) {
            HiResCoord hiRes = readHiResCoord( in );
            
            if (i==0){       // SimpleUniverse is constructed with a locale so just set its HiRes.
                locale = universe.getLocale();
                locale.setHiRes( hiRes );
            } else { 
                locale = new Locale( universe, hiRes );
            }
            
            int numBG = in.readInt();
            int[] bgs = new int[numBG];
            for(int n=0; n<numBG; n++) {
                bgs[i] = in.readInt();
                totalBGs++;
            }
            localeBGs.add( bgs );
            
        }
    }
    
    /**
     * Called once IO is complete, attaches all the branchgroups to the locales
     */
    public void buildGraph() {
        Locale locale;
        Enumeration e = universe.getAllLocales();
        for( int i=0; i<localeBGs.size(); i++) {
            locale = (Locale)e.nextElement();
            int[] bgs = (int[])localeBGs.get(i);
            for(int j=0; j<bgs.length; j++) {
                SymbolTableData symbol = control.getSymbolTable().getBranchGraphRoot( bgs[j] );               
                locale.addBranchGraph((BranchGroup)symbol.j3dNode );
            }
        }
        
        if (viewerAvatar!=null) {
            viewerAvatar.buildGraph();        
            universe.getViewer().setAvatar( (ViewerAvatar)viewerAvatar.getNode());
        }

        if (platformGeom!=null) {
            universe.getViewingPlatform().setPlatformGeometry( (PlatformGeometry)platformGeom.getNode() );
            platformGeom.buildGraph();
        }
    }
    
    /**
     * Return all the branchgraph id's for all Locales in the universe
     *
     * This call must be made after readObject()
     */
    public int[] getAllGraphIDs() {
        int[] ret = new int[totalBGs];
        int c = 0;
        
        for( int i=0; i<localeBGs.size(); i++) {
            int[] bgs = (int[])localeBGs.get(i);
            for(int j=0; j<bgs.length; j++) {
                ret[ c++ ] = bgs[j];
            }
        }
        
        return ret;
    }
    
    /**
     * Detach each BranchGraph from the Locale(s)
     */
    public void detachAllGraphs() {
        int c = 0;
        
        try {
            for( int i=0; i<localeBGs.size(); i++) {
                int[] bgs = (int[])localeBGs.get(i);
                for(int j=0; j<bgs.length; j++) {
                    SymbolTableData symbol = control.getSymbolTable().getBranchGraphRoot( bgs[j] );
                    ((BranchGroup)symbol.j3dNode).detach();
                }
            }
        } catch( javax.media.j3d.CapabilityNotSetException e ) {
            throw new javax.media.j3d.CapabilityNotSetException(
		"Locale BranchGraphs MUST have ALLOW_DETACH capability set" );
        }
    }
    
    /**
     * Reattach each BranchGraph to the Locale(s)
     */
    public void attachAllGraphs() {
        Enumeration e = universe.getAllLocales();
        Locale locale;
        
        for( int i=0; i<localeBGs.size(); i++) {
            locale = (Locale)e.nextElement();
            int[] bgs = (int[])localeBGs.get(i);
            for(int j=0; j<bgs.length; j++) {
                SymbolTableData symbol = control.getSymbolTable().getBranchGraphRoot( bgs[j] );
                locale.addBranchGraph(((BranchGroup)symbol.j3dNode));
            }
        }
    }
    
    /**
     * Return the 'node', ie the virtual universe.  Returns null if currently writing a
     * SimpleUniverse.
     */
    public ConfiguredUniverse getNode() {
	if ( universe instanceof ConfiguredUniverse ) 
	    return (ConfiguredUniverse)universe;
	else return null;
    }
    
    private void writeHiResCoord( DataOutput out, HiResCoord hiRes ) throws IOException {
        int[] x = new int[8];
        int[] y = new int[8];
        int[] z = new int[8];
        hiRes.getHiResCoord( x,y,z );
        for(int i=0; i<8; i++) {
            out.writeInt( x[i] );
            out.writeInt( y[i] );
            out.writeInt( z[i] );
        }
    }
    
    private HiResCoord readHiResCoord( DataInput in ) throws IOException {
        int[] x = new int[8];
        int[] y = new int[8];
        int[] z = new int[8];
        for(int i=0; i<8; i++) {
            x[i] = in.readInt();
            y[i] = in.readInt();
            z[i] = in.readInt();
        }
        
        return new HiResCoord( x, y, z );
    }

}
