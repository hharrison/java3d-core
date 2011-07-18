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

import javax.media.j3d.BranchGroup;
import javax.media.j3d.CapabilityNotSetException;
import javax.media.j3d.Canvas3D;

import com.sun.j3d.utils.scenegraph.io.state.javax.media.j3d.SceneGraphObjectState;
import com.sun.j3d.utils.scenegraph.io.state.javax.media.j3d.NodeComponentState;
import com.sun.j3d.utils.scenegraph.io.state.javax.media.j3d.BranchGroupState;
import com.sun.j3d.utils.scenegraph.io.UnsupportedUniverseException;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ConfiguredUniverse;

public class RandomAccessFileControl extends Controller {
        
    protected String FILE_IDENT = new String( "j3dff" );
    
    private long user_data;
    private long universe_config;
    
    private long symbol_table;
    
    private RandomAccessFile raf;
    
    private int branchGraphCount=0;
    
    private boolean writeMode = false;
    private Object userData;

    /** Creates new RandomAccessFileControl */
    public RandomAccessFileControl() {
        super();
        symbolTable = new SymbolTable(this);
    }
    
    /**
     * Create the file and write the inital header information
     */
    public void createFile( java.io.File file,
                            SimpleUniverse universe,
                            boolean writeUniverseContent,
                            String description,
                            java.io.Serializable userData ) throws IOException,
                                                            UnsupportedUniverseException,
                                                            CapabilityNotSetException {
                       
        raf = new RandomAccessFile( file, "rw" );
        writeMode = true;
        
        raf.seek(0);
        raf.writeUTF( FILE_IDENT );
        
        raf.seek(20);
        raf.writeInt( outputFileVersion );
        
        raf.seek( BRANCH_GRAPH_COUNT );
        raf.writeInt( 0 );          // Place holder to branch graph count
        
        raf.seek( FILE_DESCRIPTION );
        
        if (description==null)
            description="";
        raf.writeUTF( description );
        
        try {
            writeSerializedData( raf, userData );

            universe_config = raf.getFilePointer();
            writeUniverse( raf, universe, writeUniverseContent );
        } catch( SGIORuntimeException e ) {
            throw new IOException( e.getMessage() );
        }
    }
    
    /**
     * Open the file for reading
     */
    public void openFile( java.io.File file ) throws IOException {
        raf = new RandomAccessFile( file, "r" );
        writeMode = false;
        
        raf.seek(0);
        String ident = raf.readUTF();
        
        if ( ident.equals("demo_j3f") ) 
            throw new IOException(
		"Use Java 3D Fly Through I/O instead of Java 3D Scenegraph I/O" );
        
        if ( !ident.equals("j3dff") ) 
            throw new IOException(
		"This is a Stream - use SceneGraphStreamReader instead");
        
        raf.seek(20);
        currentFileVersion = raf.readInt();
        
	if ( currentFileVersion > outputFileVersion ) {
            throw new IOException("Unsupported file version. This file was written using a new version of the SceneGraph IO API, please update your installtion to the latest version");
	}
        
        // readFileDescription sets user_data
        String description = readFileDescription();
        
        raf.seek( BRANCH_GRAPH_COUNT );
        branchGraphCount = raf.readInt();
        //System.out.println("BranchGraph count : "+branchGraphCount );
        
        raf.seek( UNIVERSE_CONFIG_PTR );
        universe_config = raf.readLong();

        raf.seek( SYMBOL_TABLE_PTR );
        symbol_table = raf.readLong();
        
        ConfiguredUniverse universe;
        
        raf.seek( symbol_table );
        symbolTable.readTable( raf, false );
        raf.seek(user_data);       

        userData = readSerializedData(raf);
    }
    
    public ConfiguredUniverse readUniverse( boolean attachBranchGraphs,
					    Canvas3D canvas) throws IOException {
        raf.seek( universe_config );
        return readUniverse( raf, attachBranchGraphs, canvas );
    }
    
    public Object getUserData() {
        return userData;
    }
        
    /**
     * Read the set of branchgraps.
     *
     * Used by readUniverse
     *
     * RandomAccessFileControl will read the graphs in the array,
     * StreamControl will read all graphs in the stream
     */
    protected void readBranchGraphs( int[] graphs ) throws IOException {
        for(int i=0; i<graphs.length; i++) {
            readBranchGraph( graphs[i] );
        }
    }
    
    /**
     * Return the number of branchgraphs in the file
     */
    public int getBranchGraphCount() {
        return symbolTable.getBranchGraphCount();
    }
    
    public void writeBranchGraph( BranchGroup bg, java.io.Serializable userData ) throws IOException {
        long filePointer = raf.getFilePointer();
        raf.writeInt( 0 );          // Node count
        try {
            writeSerializedData( raf, userData );  // Size and byte[]

            //System.out.println("Actual Write at "+raf.getFilePointer() );

            SymbolTableData symbol = symbolTable.getSymbol( bg );

            if (symbol==null) {
                symbol = symbolTable.createSymbol( bg );
                symbol.branchGraphID = -1;          // This is a new BranchGraph so set the ID to -1
            }                                       // which will cause setBranchGraphRoot to assign a new ID.

            symbolTable.setBranchGraphRoot( symbol, filePointer );        

            symbolTable.startUnsavedNodeComponentFrame();
            SceneGraphObjectState state = createState( bg, symbol );
            //System.out.println(state);
            try {
                writeObject( raf, state );
                writeNodeComponents( raf );            
            } catch( IOException e ) {
                e.printStackTrace();
            }
            symbolTable.endUnsavedNodeComponentFrame();
        } catch( SGIORuntimeException e ) {
            throw new IOException( e.getMessage() );
        }
        
    }
    
    public BranchGroup[] readBranchGraph( int graphID ) throws IOException {
        //System.out.print("Loading graph "+graphID+" : ");   // TODO - remove
        try {
            int[] dependencies = symbolTable.getBranchGraphDependencies(graphID);

            //System.out.println("Dependencies ");
            //for(int i=0; i<dependencies.length; i++)
            //    System.out.print( dependencies[i]+"  ");        // TODO - remove
            //System.out.println();

            BranchGroupState[] states  = new BranchGroupState[ dependencies.length+1 ];
            BranchGroup[] ret = new BranchGroup[ states.length ];
            states[0] = readSingleBranchGraph( graphID );

            for( int i=0; i<dependencies.length; i++) {
                states[i+1] = readSingleBranchGraph( dependencies[i] );
            }

            for( int i=0; i<states.length; i++) {
                if (!states[i].getSymbol().graphBuilt) {
                    states[i].buildGraph();
                    states[i].getSymbol().graphBuilt = true;
                }
                ret[i] = (BranchGroup)states[i].getNode();
            }

            symbolTable.clearUnshared();            // Remove all unshared symbols
            
            return ret;
        } catch( SGIORuntimeException e ) {
            throw new IOException( e.getMessage() );
        }        
    }
    
    /**
     * Read and return all the graphs in the file
     */
    public BranchGroup[] readAllBranchGraphs() throws IOException {
        int size = getBranchGraphCount();
        BranchGroupState[] states  = new BranchGroupState[ size ];
        BranchGroup[] ret = new BranchGroup[ size ];
               
        try {
            for( int i=0; i<size; i++) {
                states[i] = readSingleBranchGraph( i );
            }

            for( int i=0; i<states.length; i++) {
                if (!states[i].getSymbol().graphBuilt) {
                    states[i].buildGraph();
                    states[i].getSymbol().graphBuilt = true;
                }
                ret[i] = (BranchGroup)states[i].getNode();
            }

            symbolTable.clearUnshared();            // Remove all unshared symbols
        } catch( SGIORuntimeException e ) {
            throw new IOException( e.getMessage() );
        }
        
        return ret;
    }

    /**
     * Read the specified branchgraph but do NOT call buildGraph
     */
    private BranchGroupState readSingleBranchGraph( int graphID ) throws IOException {
        SymbolTableData symbol = symbolTable.getBranchGraphRoot( graphID );
        
        if (symbol.nodeState!=null) {
            return (BranchGroupState)symbol.nodeState;
        }
        
        raf.seek( symbolTable.getBranchGraphFilePosition( graphID ) );
         
        return readNextBranchGraph();
    }    
    
    /**
     * Read the next userData and BranchGraph structure in the file
     * at the current position
     */
    private BranchGroupState readNextBranchGraph() throws IOException {
        int nodeCount = raf.readInt();
        skipUserData( raf );

        BranchGroupState state=null;
        try {
            state = (BranchGroupState)readObject( raf );
            
            readNodeComponents( raf );
            
        } catch( IOException e ) {
            e.printStackTrace();
        }
        
        return state;
    }
                
    public Object readBranchGraphUserData( int graphID ) throws IOException {
        try {
            raf.seek( symbolTable.getBranchGraphFilePosition( graphID ) );

            int nodeCount = raf.readInt();
            return readSerializedData( raf );        
        } catch( SGIORuntimeException e ) {
            throw new IOException( e.getMessage() );
        }
    }
    
    /**
     * Write all the unsaved NodeComponents and SharedGroups to DataOutput.
     * Mark all the NodeComponents as saved.
     */
    protected void writeNodeComponents( DataOutput out ) throws IOException {
        // The RandomAccessFileControl version sets throws the pointer to
        // the next NodeComponent correctly
        long ptrLoc=0L;

        java.util.ListIterator list = symbolTable.getUnsavedNodeComponents();
        out.writeInt( symbolTable.getUnsavedNodeComponentsSize() );
        while( list.hasNext() ) {
            SymbolTableData symbol = (SymbolTableData)list.next();

	    out.writeInt( symbol.nodeID );
	    ptrLoc = raf.getFilePointer();
	    out.writeLong( 0L );            // Pointer to next NodeComponent
            
            writeObject( out, symbol.getNodeState() );
            
	    long ptr = raf.getFilePointer();
	    raf.seek( ptrLoc );
	    out.writeLong( ptr );
	    raf.seek( ptr );
        }
    }
    
    /**
     * Read in all the node components in this block 
     */
    protected void readNodeComponents( DataInput in ) throws IOException {
        int count = in.readInt();
        
	for(int i=0; i<count; i++) {
	    int nodeID = in.readInt();
	    long nextNC = in.readLong();
		if (symbolTable.isLoaded( nodeID )) {
		    // Skip this object
		    raf.seek( nextNC );
		} else {
		    // Reading the objects will register them in the symbol table
		    SceneGraphObjectState nodeComponent = readObject( in );
		}
	    }
    }
    
    //static java.util.LinkedList objSizeTracker = new java.util.LinkedList();
    
    public void writeObject( DataOutput out, SceneGraphObjectState obj ) throws IOException {
        symbolTable.setFilePosition( raf.getFilePointer(), obj );  
        try {
            // These commented out lines will display the size of each object
            // as it's written to the file

            //long start = raf.getFilePointer();


            //int childStart = objSizeTracker.size();

            super.writeObject( out, obj );

            //long size = raf.getFilePointer()-start;
            //while( childStart!=objSizeTracker.size() )
            //    size -= ((Long)objSizeTracker.removeLast()).longValue();

            //String name = obj.getClass().getName();
            //System.out.println( name.substring( name.lastIndexOf('.')+1, name.length() )+" size "+size);

            //objSizeTracker.addLast( new Long( size ));
        
        } catch( SGIORuntimeException e ) {
            throw new IOException( e.getMessage() );
        }
}
        
    public String readFileDescription() throws IOException {
        raf.seek( FILE_DESCRIPTION );
        String ret = raf.readUTF();
        
        user_data = raf.getFilePointer();
        return ret;
    }
    
    /**
     * Used by SymbolTable to load a node component that is not in current
     * graph
     */
    public void loadNodeComponent( SymbolTableData symbol ) throws IOException {
        try {
            raf.seek( symbol.filePosition );
            readObject( raf );
        } catch( SGIORuntimeException e ) {
            throw new IOException( e.getMessage() );
        }        
    }
    
    /**
     * Loads the specified SharedGroup
     */
    public void loadSharedGroup( SymbolTableData symbol ) throws IOException {
        try {
            raf.seek( symbol.filePosition );
            readObject( raf );
        } catch( SGIORuntimeException e ) {
            throw new IOException( e.getMessage() );
        }        
    }
            
    public void close() throws IOException {
        try {
            if (writeMode)
                writeClose();       

            //System.out.println("File size at close "+raf.length() );
            raf.close();
            super.reset();
        } catch( SGIORuntimeException e ) {
            throw new IOException( e.getMessage() );
        }        
            
    }
    
    /**
     * Write all the pointers etc
     */
    private void writeClose() throws IOException {       
        symbol_table = raf.getFilePointer();
        super.getSymbolTable().writeTable( raf );
        
        //System.out.println("Symbol table size "+(raf.getFilePointer()-symbol_table));
        
        raf.seek( UNIVERSE_CONFIG_PTR );
        raf.writeLong( universe_config );
        raf.seek( SYMBOL_TABLE_PTR );
        raf.writeLong( symbol_table );
        raf.seek( BRANCH_GRAPH_COUNT );
        raf.writeInt( symbolTable.getBranchGraphCount() );
    }
    
    public long getFilePointer() {
        try {
            return raf.getFilePointer();
        } catch(IOException e ) {}
        return 0;
    }

    /**
     * Given a branchgraph, return the corresponding index of the graph
     * in the file.  Returns -1 if graph not found.
     */
    public int getBranchGraphPosition( BranchGroup graph ) {
        SymbolTableData symbol = symbolTable.getSymbol( graph );
        if ( symbol!=null ) return symbol.branchGraphID;
        return -1;
    }
}
