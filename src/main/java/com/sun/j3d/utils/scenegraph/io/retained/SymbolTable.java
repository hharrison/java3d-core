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

import java.io.IOException;
import java.io.DataOutput;
import java.io.DataInput;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Collection;
import java.util.Stack;

import javax.media.j3d.SceneGraphObject;

import com.sun.j3d.utils.scenegraph.io.state.javax.media.j3d.SceneGraphObjectState;
import com.sun.j3d.utils.scenegraph.io.state.javax.media.j3d.NullSceneGraphObjectState;
import com.sun.j3d.utils.scenegraph.io.state.javax.media.j3d.NodeComponentState;
import com.sun.j3d.utils.scenegraph.io.NamedObjectException;
import com.sun.j3d.utils.scenegraph.io.ObjectNotLoadedException;
import com.sun.j3d.utils.scenegraph.io.SceneGraphObjectReferenceControl;

/**
 * SymbolTable class for SceneGraph I/O.
 */
public class SymbolTable extends java.lang.Object implements SceneGraphObjectReferenceControl {
    
    private int nodeID = 1;                 // ID of zero represents null
    private HashMap j3dNodeIndex;           // Index by SceneGraphObject
    private ArrayList nodeIDIndex;          // Index by NodeID of Nodes
    private HashMap danglingReferences;     // Java3D objects without a current State object
    private Stack unsavedNodeComponentsStack;
    private LinkedList sharedNodes;         // Nodes and NodeComponents referenced more than once
    private HashMap namedObjects;
    private ArrayList branchGraphs;         // Root of each branch graph
    private ArrayList branchGraphDependencies;  // Dependencies between the branchgraphs
                                                // For a graph branchGraphDep[graph] will contain a set of all nodes (in other graphs) on which the graph is dependent
    
    private Controller control;
    private int currentBranchGraphID = -1;   // ID's start at 0, -1 is null, -2 is during read, -3 is dangling
    private int nextBranchGraphID = 0;
    
    /** Creates new SymbolTable */
    public SymbolTable( Controller control ) {
        this.control = control;
        j3dNodeIndex = new HashMap();
        danglingReferences = new HashMap();
        nodeIDIndex = new ArrayList();
        nodeIDIndex.add( null );            // Element zero is null 
        sharedNodes = new LinkedList();   
        namedObjects = new HashMap();
        branchGraphs = new ArrayList();
        branchGraphDependencies = new ArrayList();
        unsavedNodeComponentsStack = new Stack();
    }

    /**
     * At this stage their should be no dangling references
     * 
    */
    private void checkforDanglingReferences() {
        ListIterator list = sharedNodes.listIterator();

        while(list.hasNext()) {
            SymbolTableData data = (SymbolTableData)list.next();
            if (data.branchGraphID==-3) {
                System.err.println("Warning : node "+data.j3dNode+" is referenced but is not attached to a BranchGraph");
                System.err.println("Setting reference to null. This scene may not look correct when loaded");
            } 
        }
    }

    /** 
      * Remove dependencies on objects which are not attached to a
      * branchgraph
      */
    private void removeNullDependencies( HashSet set ) {
        Iterator it = set.iterator();
        while( it.hasNext() ) {
            SymbolTableData symbol = (SymbolTableData)it.next();
            if (symbol.branchGraphID==-3)
                it.remove();
        }
    }
        
    public void writeTable( DataOutput out ) throws IOException {

        // At this stage their should be no dangling references
        checkforDanglingReferences();

        ListIterator list = sharedNodes.listIterator();
        out.writeInt( sharedNodes.size() );
        out.writeInt( nodeID );
        while(list.hasNext()) {
            SymbolTableData data = (SymbolTableData)list.next();
            data.writeObject( out );
        }
        
        // Write Named objects
        String[] names = getNames();
        out.writeInt( names.length );
        for(int i=0; i<names.length; i++) {
            out.writeUTF( names[i] );
            SceneGraphObject node = (SceneGraphObject)namedObjects.get(names[i]);
            SymbolTableData symbol = getSymbol( node );
            if (symbol!=null)
                out.writeInt( symbol.nodeID );
            else
                out.writeInt( 0 );      // Null
        }        
        
        // Write BranchGraph roots
        out.writeInt( branchGraphs.size() );
        for(int i=0; i<branchGraphs.size(); i++)
            ((SymbolTableData)branchGraphs.get(i)).writeObject( out );
        
        for(int i=0; i<branchGraphDependencies.size(); i++) {
            HashSet set = (HashSet)branchGraphDependencies.get( i );
            if (set==null) {
                out.writeInt( 0 );
            } else {
                removeNullDependencies( set );
                out.writeInt( set.size() );
                Iterator it = set.iterator();
                while( it.hasNext() ) {
                    SymbolTableData symbol = (SymbolTableData)it.next();
                    out.writeInt( symbol.nodeID );
                }
            }
        }
    }
    
    
    /**
     * Read and store the entire symbol table
     *
     * @param streamRead - true if reading from a Stream in which case only the
     *                       branchGraphs and named objects are read.
     */
    public void readTable( java.io.DataInput in, boolean streamRead ) throws IOException {
        int size = in.readInt();
        nodeID = in.readInt();
        nodeIDIndexEnsureCapacity( nodeID );
        for(int i=0; i<size; i++) {
            SymbolTableData symbol = new SymbolTableData(0,null,null,-1);
            symbol.readObject( in );
            
            // If we are loading from a stream then the NodeComponents have
            // already been loaded and their symbols created. Therefore 
            // the symbols loaded here are discarded.
            if (!streamRead) {
                sharedNodes.add( symbol );
                nodeIDIndex.set( symbol.nodeID, symbol );
            }
        }
        
        // Read Named objects
        size = in.readInt();
        for(int j=0; j<size; j++) {
            String name = in.readUTF();
            int id = in.readInt();
            namedObjects.put( name, new Integer(id) );
        }
        
        size = in.readInt();
        //System.out.println("Symbol table BranchGraph size "+size );
        for(int i=0; i<size; i++)
            branchGraphs.add( null);
        
        // Read each branchgraph symbol and check that the symbol is not
        // already in the symbol table.
        for(int j=0; j<size; j++) {
            SymbolTableData tmp = new SymbolTableData(0,null,null,-1);
            tmp.readObject( in );
            
            SymbolTableData symbol = getSymbol( tmp.nodeID );
            
            if (symbol==null) {
                symbol = tmp;
                if (symbol.referenceCount>1)
                    sharedNodes.add( symbol );
                nodeIDIndex.set( symbol.nodeID, symbol );
            }
            
            branchGraphs.set( j, symbol );
        }
        
        
        for(int i=0; i<size; i++) {
            int setSize = in.readInt();
            
            if (setSize==0)
                branchGraphDependencies.add( null );
            else {
                HashSet set = new HashSet();
                branchGraphDependencies.add( set );
                for( int j=0; j<setSize; j++) {
                    set.add( getSymbol(in.readInt()));
                }
            }
        }
    }
        
    /**
     * Mark the node referenced by this Symbol as a branch graph root
     *
     *The filePointer is the position of the BranchGraph in the file, this
     *is not the same as the BranchGroups position due to the extra  data stored
     *for a graph.
     */
    public void setBranchGraphRoot( SymbolTableData symbol, long filePointer ) {
        if (symbol.branchGraphID<0 ) {
            symbol.branchGraphID = nextBranchGraphID++;
        }
        
        currentBranchGraphID = symbol.branchGraphID;
        for(int i=branchGraphs.size(); i<currentBranchGraphID+1; i++) {
            branchGraphs.add( null);
            branchGraphDependencies.add( null );
        }
        
        branchGraphs.set( currentBranchGraphID, symbol );
        symbol.branchGraphFilePointer = filePointer;
    }
    
    public SymbolTableData getBranchGraphRoot( int graphID ) {
        //System.out.println("BranchGraph root "+graphID+"  "+(SymbolTableData)branchGraphs.get(graphID) );
        return (SymbolTableData)branchGraphs.get(graphID);
    }
    
    /**
     * Set the branchGraphID in the symbol to the current branch graph ID
     */
    public void setBranchGraphID( SymbolTableData symbol ) {
        symbol.branchGraphID = currentBranchGraphID;
    }
    
    /**
     * Return an array of each BranchGraph on which graphID is dependent for
     * closure of the graph
     *
     * Only Nodes (not node components) cause dependencies
     *
     * If there are no dependencies int[0] is returned
     */
    public int[] getBranchGraphDependencies( int graphID ) {
        HashSet set = (HashSet)branchGraphDependencies.get(graphID);
        if (set==null)
            return new int[0];
        
        int[] ret = new int[ set.size() ];
        Iterator it = set.iterator();
        int i=0;
        while( it.hasNext() )
            ret[i++] = ((SymbolTableData)it.next()).branchGraphID;
        
        return ret;
    }
    
    /**
     * Return true if the graph is dependent on nodes in
     * other graphs
     *
     * Only Nodes (not node components) cause dependencies
     *
     */
    public boolean branchGraphHasDependencies( int graphID ) {
        HashSet set = (HashSet)branchGraphDependencies.get(graphID);
        
        if (set==null || set.size()==0)
            return false;
        else
            return true;
    }
    
    public int getBranchGraphCount() {
        return branchGraphs.size();
    }
    
    public long getBranchGraphFilePosition( int graphID ) {
        SymbolTableData symbol = (SymbolTableData)branchGraphs.get( graphID );
        return symbol.branchGraphFilePointer;
    }
    
    
    /**
     * Create a new symbol and provide a new nodeID
     * This is used during the save process
     */    
    public SymbolTableData createSymbol( SceneGraphObject node ) {
        
        // TODO : Remove this get, it's here to provide debug consistancy check
        SymbolTableData data = (SymbolTableData)j3dNodeIndex.get( node );
        
        SymbolTableData dangling = (SymbolTableData)danglingReferences.get( node );
        
        //System.out.println("Checking for dangling "+dangling+"  "+node);
        
        if (dangling!=null) {
            data = dangling;
            data.branchGraphID = currentBranchGraphID;
            danglingReferences.remove( dangling );
            //System.out.println("Updating dangling ref count");      // TODO - remove
        } else if (data==null) {
            data = new SymbolTableData( nodeID++, node, null, currentBranchGraphID );
            j3dNodeIndex.put( node, data );
            nodeIDIndex.add( data );
        } else if (data.j3dNode instanceof javax.media.j3d.Node) {
            throw new RuntimeException( "Object already in Symbol table "+ node );
        }
        
        return data;
    }
     
        
    /**
     * Create a new symbol using the specified nodeID
     * This is used during the load process.
     */
    public SymbolTableData createSymbol( SceneGraphObjectState state, SceneGraphObject node,
                                         int nodeID ) {
        
        // TODO : Remove this get, it's here to provide debug consistancy check
        SymbolTableData data = (SymbolTableData)j3dNodeIndex.get( node );
        
        if (data==null) {
            nodeIDIndexEnsureCapacity( nodeID );
            data = (SymbolTableData)nodeIDIndex.get( nodeID );
            if (data==null) {
                data = new SymbolTableData( nodeID, node, state, -2 );
                j3dNodeIndex.put( node, data );
                nodeIDIndex.set( data.getNodeID(), data );
            } else if (data.getJ3dNode()==null) {        // Only use state and node if
                data.j3dNode = node;                // this is the first instantiation
                data.nodeState = state;             // of the node
                j3dNodeIndex.put( node, data );
            }
        } else
            throw new SGIORuntimeException( "Object already in Symbol table ");
        
        return data;
    }
    
    private void nodeIDIndexEnsureCapacity( int size ) {
        nodeIDIndex.ensureCapacity( size );
        int adjust = size - nodeIDIndex.size();
        for(int i=0; i<=adjust; i++)
            nodeIDIndex.add( null );
    }
    
    /**
     * Create or return the SymbolTableData for a node which does not
     * necessary have a State object yet
     *
     */
    private SymbolTableData createDanglingSymbol( SceneGraphObject node ) {
        SymbolTableData data = (SymbolTableData)j3dNodeIndex.get( node );
        
        if (data==null) {
            data = new SymbolTableData( nodeID++, node, null, -3 );
            j3dNodeIndex.put( node, data );
            nodeIDIndex.add( data );
            danglingReferences.put( node, data );
        } else if ( data.nodeState==null) {
            if (data.referenceCount==1)
                sharedNodes.add( data );
            data.referenceCount++;
        } else
            throw new SGIORuntimeException( "Object already in Symbol table ");
        
        return data;
    }
    
    private SymbolTableData createNodeComponentSymbol( SceneGraphObject node ) {
        SymbolTableData symbol = new SymbolTableData( nodeID++, node, null, currentBranchGraphID );
        symbol.isNodeComponent = true;
        j3dNodeIndex.put( node, symbol );
        nodeIDIndex.add( symbol );
        
        ((LinkedList)unsavedNodeComponentsStack.peek()).add( symbol );
        
        control.createState( symbol );
        
        return symbol;
    }
    
    public int getUnsavedNodeComponentsSize() {
        return ((LinkedList)unsavedNodeComponentsStack.peek()).size();
    }
    
    public ListIterator getUnsavedNodeComponents() {
        return ((LinkedList)unsavedNodeComponentsStack.peek()).listIterator(0);
    }
    
    public void startUnsavedNodeComponentFrame() {
        unsavedNodeComponentsStack.push( new LinkedList() );
    }
    
    public void endUnsavedNodeComponentFrame() {
        unsavedNodeComponentsStack.pop();
        confirmInterGraphDependency();
    }
    
    /**
     * Check for and remove any inter graph dependency
     * labels that have been resolved to the current graph
     */
    private void confirmInterGraphDependency() {
        HashSet set = (HashSet)branchGraphDependencies.get(currentBranchGraphID);
        if (set==null)
            return;
        
        Iterator it = set.iterator();
        while(it.hasNext()) {
            SymbolTableData symbol = (SymbolTableData)it.next();
            if (symbol.branchGraphID==currentBranchGraphID)
                it.remove();
        }
        
    }
    
    /** 
     * Add a dependency to the current branchgraph on <code>symbol</code>
     *
     * Only nodes (not nodeComponents) affect intergraph dependencies
     */
    private void addInterGraphDependency( SymbolTableData symbol ) {
        HashSet set = (HashSet)branchGraphDependencies.get( currentBranchGraphID );
        if (set==null) {
            set = new HashSet();
            branchGraphDependencies.set( currentBranchGraphID, set );
        }
        
        set.add(symbol);
    }
    
    /**
     * Update the reference count for the node component.
     *
     * Called during NodeComponentState.addSubReference()
     */
    public void incNodeComponentRefCount( int nodeID ) {
        if (nodeID==0) return;
        
        SymbolTableData symbol = getSymbol( nodeID );
        
        ((NodeComponentState)symbol.nodeState).addSubReference();
        
        if (symbol.referenceCount==1)
            sharedNodes.add( symbol );
        symbol.referenceCount++;
    }
    
    /**
     * Add a refernce to the specified node
     * Also returns the nodes id
     */
    public int addReference( SceneGraphObject node ) {
        if (node==null) return 0;
        
        SymbolTableData symbol = getSymbol( node );
        
        if (symbol==null) {
            if (node instanceof javax.media.j3d.Node) {
                symbol = createDanglingSymbol( node );
                if (symbol.branchGraphID != currentBranchGraphID  ) {
                    //System.out.println("------------- Adding Reference "+symbol.nodeID+" "+node );  // TODO - remove
                    addInterGraphDependency( symbol );
                    sharedNodes.add( symbol );
                }
            } else {
                symbol = createNodeComponentSymbol( node );
            }
            return symbol.nodeID;
        } else {
            return addReference( symbol );
        }
    }
    
    /**
     * Add a refernce to the specified node
     * Also returns the nodes id
     */
    public int addReference( SymbolTableData symbol ) {
                
        if (symbol!=null) {
            if (symbol.referenceCount==1)
                sharedNodes.add( symbol );
            symbol.referenceCount++;
            
            if (symbol.j3dNode instanceof javax.media.j3d.NodeComponent && symbol.referenceCount>1) {
                ((NodeComponentState)symbol.nodeState).addSubReference();
            }
            
            if (symbol.branchGraphID != currentBranchGraphID && 
                symbol.j3dNode instanceof javax.media.j3d.Node ) {
                    // System.out.println("------------- Adding Reference "+symbol.nodeID+" "+symbol.j3dNode );    // TODO - remove
                    addInterGraphDependency( symbol );
                }
        } else {
            throw new SGIORuntimeException("Null Symbol");
        }
        
        return symbol.nodeID;
    }
    
    /**
     * Add a refernce to the BranchGraph root
     * Also returns the nodes id
     *
     * Used to associate graphs with a locale without storing the graph at the
     * current time.
     */
    public int addBranchGraphReference( SceneGraphObject node, int branchGraphID ) {
        if (node==null) return 0;
        
        SymbolTableData symbol = getSymbol( node );
        
        if (symbol!=null) {
            if (symbol.referenceCount==1)
                sharedNodes.add( symbol );
            symbol.referenceCount++;
        } else {
            symbol = new SymbolTableData( nodeID++, node, null, -3 );
            j3dNodeIndex.put( node, symbol );
            nodeIDIndex.add( symbol );
            danglingReferences.put( node, symbol );
        }
        
        symbol.branchGraphID = branchGraphID;
        for(int i=branchGraphs.size(); i<branchGraphID+1; i++) {
            branchGraphs.add( null);
            branchGraphDependencies.add( null );
        }

        branchGraphs.set( symbol.branchGraphID, symbol );
        
        return symbol.nodeID;
    }    
    
    /**
     * Return true if this node has already been loaded
     */
    public boolean isLoaded( int nodeID ) {
        SymbolTableData symbol = getSymbol( nodeID );
        
        if (symbol==null)
            return false;
        
        if (symbol.j3dNode==null)
            return false;
        
        return true;
    }
    
    /**
     * Return the Java3D node associated with the nodeID.
     *
     * The method will call buildGraph() on the node if necessary
     */
    public SceneGraphObject getJ3dNode( int nodeID ) {
        if (nodeID==0) return null;
        
        SymbolTableData symbol = getSymbol( nodeID );

        // Although referenced this node was not attached to the
        // scenegraph, so return null
        if (symbol.branchGraphID==-3)
	    return null;
        
        if (symbol!=null && symbol.j3dNode==null) {
            if (symbol.isNodeComponent && (control instanceof RandomAccessFileControl) ) {
                try {
                    ((RandomAccessFileControl)control).loadNodeComponent( symbol );
                } catch(IOException e ) {
                    System.out.println("FAILED to seek and load NodeComponent");
                    return null;
                }
            } else {
                System.out.println("WARNING - Object has not been loaded "+nodeID);
                System.out.println("Need to load branchgraph "+symbol.branchGraphID );
                return null;
            }
        } else if (symbol==null) {
            throw new SGIORuntimeException("Missing Symbol "+nodeID );
        }
        
        if ( !symbol.graphBuilt ) {
            symbol.graphBuilt = true;
            symbol.nodeState.buildGraph();
        }
        
        return symbol.j3dNode;
    }
    
    /**
     * Get the table entry for node
     */
    public SymbolTableData getSymbol( SceneGraphObject node ) {     
        //System.out.println("getSymbol "+node+"  "+j3dNodeIndex.get( node ));
        return (SymbolTableData)j3dNodeIndex.get( node );
    }
      
    /**
     * Return the node with the give ID
     */
    public SymbolTableData getSymbol( int nodeID ) {
        // nodeID's start at 1
        
        if (nodeID==0 || nodeID>nodeIDIndex.size() )
            return null;
        else 
            return (SymbolTableData)nodeIDIndex.get( nodeID );
    }
    
    /** Get the symbol for the shared group
     *  If the sharedgroup has not been loaded then load it before
     *  returning (if we are using RandomAccessFileControl
     */
    public SymbolTableData getSharedGroup( int nodeID ) {
        SymbolTableData symbol = getSymbol( nodeID );
        
        if (symbol.nodeState==null && control instanceof RandomAccessFileControl) {
            try {
                ((RandomAccessFileControl)control).loadSharedGroup( symbol );
            } catch( java.io.IOException e ) {
                e.printStackTrace();
                throw new SGIORuntimeException("Internal error in getSharedGroup");
            }
        }
        
        return symbol;
    }
    
    /** 
     * Set the position of the object referenced by state
     */
    public void setFilePosition( long ptr, SceneGraphObjectState state ) {
        if (state instanceof NullSceneGraphObjectState) return;
        
        SymbolTableData symbol = getSymbol( state.getNodeID() );
        
        symbol.filePosition = ptr;
    }
    /**
     * Associate the name with the scene graph object 
     */
    public void addNamedObject( String name, SceneGraphObject object ) {
        namedObjects.put( name, object );
    }
    
    /**
     * Add all the named objects in <code>map</code>
     */
    public void addNamedObjects( HashMap map ) {
        if (map!=null)
            namedObjects.putAll( map );
    }
    
    /**
     * Return the SceneGraphObject associated with the name
     */
    public SceneGraphObject getNamedObject( String name ) throws NamedObjectException, ObjectNotLoadedException {
        Object obj = namedObjects.get( name );
        if (obj==null)
            throw new NamedObjectException( "Unknown name :"+name );
        
        if (obj instanceof SceneGraphObject)
            return (SceneGraphObject)obj;
        else {
            SymbolTableData symbol = getSymbol( ((Integer)obj).intValue() );
            if (symbol==null || symbol.j3dNode==null)
                throw new ObjectNotLoadedException( ((Integer)obj).toString() );
            return symbol.j3dNode;
        }
    }
    
    /**
     * Get all the names of the named objects
     */
    public String[] getNames() {
        return (String[])namedObjects.keySet().toArray( new String[] {} );
    }
    
    /**
     * Add the namedObject mappings to <code>map</code>
     */
    public void getNamedObjectMap( HashMap map ) {
        map.putAll( namedObjects );
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        
        for(int i=0; i<nodeIDIndex.size(); i++) {
            SymbolTableData data = (SymbolTableData)nodeIDIndex.get(i);
            if (data!=null)
                buf.append( data.nodeID+" "+data.referenceCount+" "+data.filePosition+"  "+data.branchGraphID+"  "+data.nodeState+"\n" );
        }
        
        buf.append("\nShared Objects\n");
        
        ListIterator l = sharedNodes.listIterator();
        while(l.hasNext()) {
            SymbolTableData data = (SymbolTableData)l.next();
            buf.append( data.nodeID+" "+data.referenceCount+" "+data.filePosition+"  "+data.branchGraphID+"  "+data.j3dNode+"\n" );            
        }
        
        buf.append("\nNamed Objects\n");
        
        String[] names = getNames();
        for(int i=0; i<names.length; i++)
            buf.append( names[i]+"  "+namedObjects.get(names[i]) );
        
        buf.append("\nBranch Graphs\n");
        for(int i=0; i<branchGraphs.size(); i++) {
            SymbolTableData data = (SymbolTableData)branchGraphs.get(i);
            if (data==null) System.out.println("Data is null "+i+"  "+branchGraphs.size());
            buf.append( data.nodeID+" "+data.referenceCount+" "+data.filePosition+"  "+data.branchGraphID+"  "+data.j3dNode+" "+data.nodeState+"\n" );            
        }
        
        buf.append("\nBranch Graph Dependencies\n");
        for(int i=0; i<branchGraphDependencies.size(); i++) {
            buf.append("Graph "+i+" - ");
            HashSet set = (HashSet)branchGraphDependencies.get(i);
            if (set!=null) {
                Iterator it = set.iterator();
                while(it.hasNext())
                    buf.append( ((SymbolTableData)it.next()).nodeID+" ");
            }
            buf.append("\n");
        }
        
        buf.append("------------------");
        
        return buf.toString();
    }

    /**
     * Clear all elements from the symbol table
     */
    public void clear() {
        j3dNodeIndex.clear();
        nodeIDIndex.clear();
        while (!unsavedNodeComponentsStack.empty())
            unsavedNodeComponentsStack.pop();
        danglingReferences.clear();
        sharedNodes.clear();
        namedObjects.clear();
        nodeID = 1;
    }
    
    /**
     * Clear all the Symbols that are not shared with other Graphs in the file
     *
     * Remove all Symbols from all structures with referenceCounts=1
     */
    public void clearUnshared() {
        // Convert as many named objects as possible to reference to j3dNode
        String[] names = getNames();
        for(int i=0; i<names.length; i++) {
            try {
                Object obj = namedObjects.get(names[i]);
                if (obj instanceof Integer) {
                    SymbolTableData symbol = getSymbol( ((Integer)obj).intValue() );
                    if (symbol!=null && symbol.j3dNode!=null)
                        namedObjects.put( names[i], symbol.j3dNode );
                }
            } catch( Exception e ) { e.printStackTrace();}
        }
        
        j3dNodeIndex.clear();
        nodeIDIndex.clear();
        while (!unsavedNodeComponentsStack.empty())
            unsavedNodeComponentsStack.pop();
        
        nodeIDIndexEnsureCapacity( nodeID );
        
        // Add the shared and dangling Symbols back into the other structures
        ListIterator list = sharedNodes.listIterator();
        while(list.hasNext()) {
            SymbolTableData symbol = (SymbolTableData)list.next();
            nodeIDIndex.set( symbol.nodeID, symbol );
            j3dNodeIndex.put( symbol.j3dNode, symbol );
        }
        
        Iterator it = danglingReferences.values().iterator();
        while(it.hasNext()) {
            SymbolTableData symbol = (SymbolTableData)it.next();
            nodeIDIndex.set( symbol.nodeID, symbol );
            j3dNodeIndex.put( symbol.j3dNode, symbol );
        }
        

    }
    
    /**
 * Given a nodeID return the corresponding scene graph object.
 *
 * Use only during the load cycle
 */
    public javax.media.j3d.SceneGraphObject resolveReference(int nodeID) {
        return getJ3dNode( nodeID );
    }
    
}

