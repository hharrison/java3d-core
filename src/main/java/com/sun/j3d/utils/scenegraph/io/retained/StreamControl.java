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

package com.sun.j3d.utils.scenegraph.io.retained;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.DataOutput;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Iterator;

import javax.media.j3d.VirtualUniverse;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.SceneGraphObject;

import com.sun.j3d.utils.scenegraph.io.state.javax.media.j3d.SceneGraphObjectState;
import com.sun.j3d.utils.scenegraph.io.state.javax.media.j3d.NodeComponentState;
import com.sun.j3d.utils.scenegraph.io.state.javax.media.j3d.BranchGroupState;
import com.sun.j3d.utils.scenegraph.io.UnsupportedUniverseException;

/**
 * Provides the infrastructure for ScenGraphStream Reader and Writer
 */
public class StreamControl extends Controller {
        
    protected String FILE_IDENT = new String( "j3dsf" );
    
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
        
    public StreamControl( DataOutputStream out ) {
        super();
        outputStream = out;
        symbolTable = new SymbolTable( this );
    }
    
    public StreamControl( DataInputStream in ) {
        super();
        inputStream = in;
        symbolTable = new SymbolTable( this );
    }
    
    /**
     * Prepare the Stream for writing, by sending header information
     */
    public void writeStreamHeader() throws IOException {
        outputStream.writeUTF( FILE_IDENT );
        outputStream.writeInt( outputFileVersion );
    }
    
    public void readStreamHeader() throws IOException {
        String ident = inputStream.readUTF();
        if ( ident.equals("demo_j3s") ) 
            throw new IOException( "Use Java 3D Fly Through I/O instead of Java 3D Scenegraph I/O" );

        if ( !ident.equals("j3dsf") ) 
            throw new IOException(
		"This is a File - use SceneGraphFileReader instead");

        currentFileVersion = inputStream.readInt();
        
	if (currentFileVersion > outputFileVersion ) {
            throw new IOException("Unsupported file version. This file was written using a new version of the SceneGraph IO API, please update your installtion to the latest version");
	}
    }
        
    /**
     * Add the named objects to the symbol table
     */
    public void addNamedObjects( HashMap namedObjects ) {
        symbolTable.addNamedObjects( namedObjects );
    }
     
    /** 
     * The BranchGraph userData is not supported in a stream and will be
     * ignored.
     *
     * However the data in the userData field of the BranchGroup will be
     * stored in the stream
     */
    public void writeBranchGraph( BranchGroup bg, java.io.Serializable userData ) throws IOException {
        try {
            SymbolTableData symbol = symbolTable.getSymbol( bg );

            if (symbol==null) {
                symbol = symbolTable.createSymbol( bg );
                symbol.branchGraphID = -1;          // This is a new BranchGraph so set the ID to -1
            }                                       // which will cause setBranchGraphRoot to assign a new ID.

            symbolTable.setBranchGraphRoot( symbol, 0 );        
            symbolTable.startUnsavedNodeComponentFrame();
            SceneGraphObjectState state = createState( bg, symbol );
            writeObject( outputStream, state );
            writeNodeComponents( outputStream );
            symbolTable.endUnsavedNodeComponentFrame();

            if (symbolTable.branchGraphHasDependencies( symbol.branchGraphID ))
                throw new javax.media.j3d.DanglingReferenceException();

            symbolTable.clearUnshared();
            symbolTable.writeTable( outputStream );
        } catch( SGIORuntimeException e ) {
            throw new IOException( e.getMessage() );
        }        
    }
    
    public BranchGroup readBranchGraph( HashMap namedObjects ) throws IOException {
        try {
            SceneGraphObjectState state = readObject( inputStream );
            readNodeComponents( inputStream );
            symbolTable.readTable( inputStream, true );

            symbolTable.setBranchGraphRoot( state.getSymbol(), 0 );

            state.buildGraph();

            if (namedObjects!=null)
                symbolTable.getNamedObjectMap( namedObjects );

            return (BranchGroup)state.getNode();
        } catch( SGIORuntimeException e ) {
            throw new IOException( e.getMessage() );
        }        
    }
    
    /**
     * Read the set of branchgraps.
     *
     * Used by readUniverse
     *
     * RandomAccessFileControl will read the graphs in the array,
     * StreamControl expects the graphs to follow the universe in the
     * stream so it will read graphs.length branchgraphs.
     */
    protected void readBranchGraphs( int[] graphs ) throws IOException {
        for(int i=0; i<graphs.length; i++)
            readBranchGraph( null );
    }
    
    /**
     * Used by SymbolTable to load a node component that is not in current
     * graph
     */
    public void loadNodeComponent( SymbolTableData symbol ) throws IOException {
        throw new java.io.IOException("Unable to load individual NodeComponents from Stream");
    }
    
    public void close() throws IOException {
        super.reset();
    }
    
    /**
     * Implementation of abstract method from Controller.
     *
     * Always returns 0
     */
    public long getFilePointer() {
        return 0L;
    }
    
}
