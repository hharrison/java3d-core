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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ListIterator;

import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingPolytope;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.CapabilityNotSetException;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.SharedGroup;
import javax.media.j3d.Transform3D;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Tuple3d;
import javax.vecmath.Tuple3f;
import javax.vecmath.Tuple4d;
import javax.vecmath.Tuple4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4d;
import javax.vecmath.Vector4f;

import com.sun.j3d.utils.scenegraph.io.NamedObjectException;
import com.sun.j3d.utils.scenegraph.io.ObjectNotLoadedException;
import com.sun.j3d.utils.scenegraph.io.SceneGraphStateProvider;
import com.sun.j3d.utils.scenegraph.io.UnsupportedUniverseException;
import com.sun.j3d.utils.scenegraph.io.state.com.sun.j3d.utils.universe.SimpleUniverseState;
import com.sun.j3d.utils.scenegraph.io.state.javax.media.j3d.ImageComponentState;
import com.sun.j3d.utils.scenegraph.io.state.javax.media.j3d.NullSceneGraphObjectState;
import com.sun.j3d.utils.scenegraph.io.state.javax.media.j3d.SceneGraphObjectState;
import com.sun.j3d.utils.universe.ConfiguredUniverse;
import com.sun.j3d.utils.universe.SimpleUniverse;

/**
 * Provides code to control the reading and writing of Java3D objects to and 
 * from any Java IO mechanism.
 */
public abstract class Controller extends java.lang.Object {
    
    
    protected static final long SYMBOL_TABLE_PTR = 30;            // long - 8 bytes
    protected static final long BG_DIR_PTR = 38;                  // long - 8 bytes
    protected static final long NAMES_OBJECTS_TABLE_PTR = 46;     // long - 8 bytes
    protected static final long NODE_TYPES_PTR = 52;              // long - 8 bytes
    protected static final long UNIVERSE_CONFIG_PTR = 60;         // long - 8 bytes
    protected static final long BRANCH_GRAPH_COUNT = 68;          // int - 4 bytes
    protected static final long FILE_DESCRIPTION = 72;            // UTF - n bytes

    protected SymbolTable symbolTable;
    protected NullSceneGraphObjectState nullObject = new NullSceneGraphObjectState( null, this );
    
    /**
     * The currentFileVersion being read
     */
    protected int currentFileVersion;
    
    /**
     * The File version which will be written
     *
     * 1 = Java3D 1.3 beta 1
     * 2 = Java3D 1.3 FCS, 1) fix to allow skipping user data written via 
			      SceneGraphIO interface
            		   2) Add missing duplicateOnCloneTree flag 
			      (bug 4690159)
     * 3 = Java3D 1.5.1    1) Add support for SceneGraphObject Name field
     * 4 = Java3D 1.5.2    issue 532, for saving Background Geometry
     * 5 = Java3D 1.5.2+   issue 654, for saving required SpotLight attributes
     */
    protected int outputFileVersion = 5;

    /**
     * When running the application within webstart this may not be the 
     * correct ClassLoader. If Java 3D is not installed in the local vm and
     * is instead installed by webstart then this definitely is NOT the correct
     * classloader, in this case Thread.getCurrent().getClass().getClassLoader()
     * would probably be a good default. The user can also set their preferred
     * classloader by calling setClassLoader in SceneGraph[Stream|File]Reader.
     */
    protected ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    
    /**
     * If true when loading a scenegraph that contains nodes who's classes
     * are not in the classpath then use then first Java3D core superclass
     * to instantiate the node.
     *
     * If false a SGIORuntimeException will be thrown when classes cannot be
     * located
     */
    private boolean useSuperClass = false;
        
    private int imageCompression = ImageComponentState.NO_COMPRESSION;
    
    /** Creates new Controller */
    public Controller() {
        try {
            if ( System.getProperty("j3d.io.UseSuperClassIfNoChildClass")!=null)
                useSuperClass = true;
            
            String imageC = System.getProperty("j3d.io.ImageCompression");
            if (imageC!=null) {
                if (imageC.equalsIgnoreCase("None"))
                    imageCompression = ImageComponentState.NO_COMPRESSION;
                else if (imageC.equalsIgnoreCase("GZIP"))
                    imageCompression = ImageComponentState.GZIP_COMPRESSION;
                else if (imageC.equalsIgnoreCase("JPEG"))
                    imageCompression = ImageComponentState.JPEG_COMPRESSION;
            }
        } catch( Exception e ) {}
        
    }
    
    public final SymbolTable getSymbolTable() {
        return symbolTable;
    }
    
    /**
     * Get the file version that we should write
     */
    public int getOutputFileVersion() {
        return outputFileVersion;
    }
    
    /**
     * Get the file version of the file we are reading
     */
    public int getCurrentFileVersion() {
        return currentFileVersion;
    }
    
    /**
     * Create a new state object and check for a pre-existing symbol table
     * entry
     */
    public SceneGraphObjectState createState( SceneGraphObject obj ) {
        return createState( obj, symbolTable.getSymbol( obj ) );
    }
    
    /**
      * Given a scene graph object instantiate the correct State class
      * for that object. If the symbol already exists (is not null) then
      * increment the reference count, otherwise create a new symbol.
      */
    public SceneGraphObjectState createState( SceneGraphObject obj, SymbolTableData symbol ) {
        if (obj==null) return nullObject;
 
        if (symbol!=null) {
            symbol.incrementReferenceCount();
            symbolTable.setBranchGraphID( symbol );
            if (symbol.getNodeState()!=null)
                return symbol.getNodeState();
        } else
            symbol = symbolTable.createSymbol( obj );
        
        return createState( symbol );
    }
    
    /**
     * Return the state class for the SceneGraphObject, creating one if it does
     * not already exist
     */
    public SceneGraphObjectState createState( SymbolTableData symbol ) {
        SceneGraphObject obj = symbol.getJ3dNode();
        if (obj==null) return nullObject;
        
        String name = obj.getClass().getName();
        SceneGraphObjectState ret;
        
          try {
              Class state;
              if (obj instanceof SceneGraphStateProvider)
                  state = ((SceneGraphStateProvider)obj).getStateClass();
              else 
                  state = Class.forName( "com.sun.j3d.utils.scenegraph.io.state."+name+"State" );
              ret = constructStateObj( symbol, state, obj.getClass() );
          } catch(ClassNotFoundException e) {
              ret = checkSuperClasses( symbol );
              if (!(obj instanceof com.sun.j3d.utils.scenegraph.io.SceneGraphIO))
                System.out.println("Could not find "+"com.sun.j3d.utils.scenegraph.io.state."+name+"State, using superclass "+ret.getClass().getName() );
              if (ret==null)
                  throw new SGIORuntimeException( "No State class for "+
                                                  obj.getClass().getName() );
          }
        
        symbol.nodeState = ret;
        
        return ret;
    }
    private SceneGraphObjectState constructStateObj( SymbolTableData symbol,
                                                     Class state,
                                                     Class objClass ) {
 
        SceneGraphObjectState ret = null;
 
        try {
            Constructor construct = state.getConstructor(
                                new Class[] { com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData.class,
                                              com.sun.j3d.utils.scenegraph.io.retained.Controller.class
                                            } );
            ret = (SceneGraphObjectState)construct.newInstance(
                                                new Object[]{ symbol, this } );
 
        } catch( NoSuchMethodException ex ) {
            System.out.println("Looking for Constructor ("+symbol.j3dNode.getClass().getName()+", Controller )");
            throw new SGIORuntimeException( "1 Broken State class for "+
                                                state.getName() );
        } catch( InvocationTargetException exc ) {
            exc.printStackTrace();
            throw new SGIORuntimeException( "2 Broken State class for "+
                                                state.getName() );
        } catch( IllegalAccessException exce ) {
            throw new SGIORuntimeException( "3 Broken State class for "+
                                                state.getName() );
        } catch( InstantiationException excep ) {
            throw new SGIORuntimeException( "4 Broken State class for "+
                                                state.getName() );
        }
 
        return ret;
    }
 
    /**
      * Check to see if any of the superclasses of obj are
      * known to the Java3D IO package
      */
    private SceneGraphObjectState checkSuperClasses( SymbolTableData symbol ) {
       
        Class cl = symbol.j3dNode.getClass().getSuperclass();
        Class state = null;
        boolean finished = false;
 
 
        while( cl != null & !finished ) {
            String name = cl.getName();
            //System.out.println("Got superclass "+name);
            try {
                state = Class.forName( "com.sun.j3d.utils.scenegraph.io.state."+name+"State" );
            } catch(ClassNotFoundException e) {
                state = null;
            }
               
            if (state!=null)
                finished = true;
            else
                cl = cl.getSuperclass();
        }
 
        if (cl==null)
            throw new SGIORuntimeException( "Unsupported class "+symbol.j3dNode.getClass().getName() );
        
        return constructStateObj( symbol, state, cl );
    } 
              
    
    public void writeObject( DataOutput out, SceneGraphObjectState obj ) throws IOException {

        int classID = getStateID( obj );
        
        out.writeInt( classID );      // Node class id 
        
        if (classID==0) {
            out.writeUTF( obj.getClass().getName() );
        }
        
        obj.writeObject( out );        
    }
    
    public SceneGraphObjectState readObject( DataInput in ) throws IOException {
        int classID = in.readInt();

        SceneGraphObjectState state = null;
        
        if (classID==-1)
            return nullObject;
        else if (classID==0) { 
            String stateClassName = in.readUTF();

            try {
                Class cl = Class.forName( stateClassName, true, classLoader );
                // System.out.println("Got class "+cl );
                Constructor construct = cl.getConstructor(
                        new Class[] { 
                        com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData.class,
                        com.sun.j3d.utils.scenegraph.io.retained.Controller.class} );

                // System.out.println("Got constructor "+construct );
                state = (SceneGraphObjectState)construct.newInstance(
                                                    new Object[]{ null, this } );

                // System.out.println("Got state instance "+state);
            } catch(ClassNotFoundException e) {
                throw new java.io.IOException( "Error Loading State Class "+stateClassName+"  "+e.getMessage() );
            } catch( NoSuchMethodException ex ) {
                 throw new java.io.IOException( "1 Broken State class for "+
                                                    stateClassName+"  "+ex.getMessage() );
            } catch( InvocationTargetException exc ) {
                exc.printStackTrace();
                throw new java.io.IOException( "2 Broken State class for "+
                                                    stateClassName );
            } catch( IllegalAccessException exce ) {
                throw new java.io.IOException( "3 Broken State class for "+
                                                    stateClassName );
            } catch( InstantiationException excep ) {
                throw new java.io.IOException( "4 Broken State class for "+
                                                    stateClassName );
            }     
        } else {
            state = createCoreState( classID );
        }
        
        state.readObject( in );
        
        return state;
    }

    /** 
      * Set the class loader used to load the Scene Graph Objects and
      * the serialized user data. The default is 
      * ClassLoader.getSystemClassLoader()
      */
    public void setClassLoader( ClassLoader classLoader ) {
        this.classLoader = classLoader;
    }


    /** 
      * Get the class loader used to load the Scene Graph Objects and
      * the serialized user data. The default is 
      * ClassLoader.getSystemClassLoader()
      */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Write all the unsaved NodeComponents and SharedGroups to DataOutput.
     * Mark all the NodeComponents as saved.
     */
    protected void writeNodeComponents( DataOutput out ) throws IOException {
        // This method is overridden by RandomAccessFileControl
        // The RandomAccessFileControl version sets the pointer to
        // the next NodeComponent correclty
        
        ListIterator list = symbolTable.getUnsavedNodeComponents();
        out.writeInt( symbolTable.getUnsavedNodeComponentsSize() );
        while( list.hasNext() ) {
            SymbolTableData symbol = (SymbolTableData)list.next();
            
            out.writeInt( symbol.nodeID );
            out.writeLong( 0L );            // Pointer to next NodeComponent
            
            writeObject( out, symbol.getNodeState() );
        }
    }
    
    /**
     * Read in all the node components in this block 
     */
    protected void readNodeComponents( DataInput in ) throws IOException {
        int count = in.readInt();
        
        for(int i=0; i<count; i++) {
            // nodeID and nextNC data is used in RandomAccessFileControl
            // version of readNodeComponents
            int nodeID = in.readInt();
            long nextNC = in.readLong();
            
            SceneGraphObjectState nodeComponent = readObject( in );
        }
    }
    
    /**
     * Write the shared group and it's node components to the IO stream
     */
    public void writeSharedGroup( DataOutput out, SharedGroup sharedGroup, SymbolTableData symbol ) throws IOException {
        SceneGraphObjectState state = createState( sharedGroup, symbol );
        symbolTable.startUnsavedNodeComponentFrame();
        writeObject( out, state );
        writeNodeComponents( out );
        symbolTable.endUnsavedNodeComponentFrame();
    }
    
    /**
     * Read a Shared group and it's node components from the IO Stream
     */
    public int readSharedGroup( DataInput in ) throws IOException {
        SceneGraphObjectState state = readObject( in );
        readNodeComponents( in );
        
        return state.getNodeID();
    }
        
    /**
     * Write out the Universe information.
     */
    public void writeUniverse( DataOutput out, SimpleUniverse universe,
                               boolean writeUniverseContent ) throws IOException, UnsupportedUniverseException, CapabilityNotSetException {
        if (universe==null) {
            out.writeUTF( "null" );
        } else if ( universe instanceof SimpleUniverse ) {
            out.writeUTF( universe.getClass().getName() );
            SimpleUniverseState state = new SimpleUniverseState( universe, this );
            state.writeObject( out );
            
            if (writeUniverseContent) {
                state.detachAllGraphs();
                int[] graphs = state.getAllGraphIDs();
                for(int i=0; i<graphs.length; i++) {
                    SymbolTableData symbol = symbolTable.getBranchGraphRoot( graphs[i] );
                    System.out.println("Writing "+graphs[i]+"  "+symbol.j3dNode );
                    writeBranchGraph( (BranchGroup)symbol.j3dNode, null );
                }
                
                state.attachAllGraphs();
            }
        } else {
            throw new UnsupportedUniverseException(
		"Current Implementation only support SimpleUniverse/ConfiguredUniverse.");
        }
    }
    
    /**
     * Read and create a new Universe matching the one used during save.
     *
     * @param attachBranchGraphs If true then all the branchGraph attached to 
     * the universe when it was saved will be loaded and reattached.
     */
    public ConfiguredUniverse readUniverse(DataInput in, boolean attachBranchGraphs,
					   Canvas3D canvas) throws IOException {
        String universeClass = in.readUTF();
        //System.out.println(universeClass);
        if (universeClass.equals("null"))
            return null;
        else if ( (universeClass.equals("com.sun.j3d.utils.universe.SimpleUniverse")) ||
                  (universeClass.equals("com.sun.j3d.utils.universe.ConfiguredUniverse")) ) {
            SimpleUniverseState state = new SimpleUniverseState( this );
            state.readObject( in, canvas );
            
            if (attachBranchGraphs) {
                int[] graphs = state.getAllGraphIDs();
                readBranchGraphs( graphs );
                
                state.buildGraph();
            }
            
            return state.getNode();
        }
        throw new IOException("Unrecognized universe class "+universeClass);
    }
    
    /**
     * Read the set of branchgraps.
     *
     * Used by readUniverse
     *
     * RandomAccessFileControl will read the graphs in the array,
     * StreamControl will read all graphs in the stream
     */
    protected abstract void readBranchGraphs( int[] graphs ) throws IOException;
    
    public abstract void writeBranchGraph( BranchGroup bg, java.io.Serializable userData) throws IOException;
    
    /**
     * Reset the controller, ready to load/save data to a new file
     */
    public void reset() {
        symbolTable.clear();
    }
    
    /**
     * 'Core' classes (ie those hard coded in this API) are assigned a
     * numerical value representing their class. This simply saves space
     * and IO bandwidth
     */
    private SceneGraphObjectState createCoreState( int classID ) {
        
        if (classID==-1)
            return nullObject;
        else if (classID==0)
            return null;
        
        Class j3dClass = getNodeClassFromID( classID-1 );
        String j3dClassName = j3dClass.getName();
        String stateClassName = "com.sun.j3d.utils.scenegraph.io.state."+j3dClassName+"State";

        SceneGraphObjectState stateObj = null;
        try {
            Class stateClass = Class.forName( stateClassName );
            Constructor stateConstructor = stateClass.getConstructor( new Class[] { SymbolTableData.class, Controller.class } );
            stateObj = (SceneGraphObjectState)stateConstructor.newInstance( new Object[] { null, this } );
        } catch( Exception e ) {
            e.printStackTrace();
        }
        
        return stateObj;
    }
    
    /**
     * Return the id of the state class
     */
    private int getStateID( SceneGraphObjectState state ) {
        
        if (state instanceof NullSceneGraphObjectState)
            return -1;
        
        return getNodeClassID( state.getNode() )+1;
    }
    
    // The order of this array dictates the ID's of classes therefore
    // changing the order of this array will break backward compatability
    Class[] j3dClasses = new Class[] {
        javax.media.j3d.Alpha.class,
        javax.media.j3d.Appearance.class,
        javax.media.j3d.Billboard.class,
        javax.media.j3d.BranchGroup.class,
        javax.media.j3d.ColoringAttributes.class,
        javax.media.j3d.ConeSound.class,
        javax.media.j3d.DecalGroup.class,
        javax.media.j3d.DirectionalLight.class,
        javax.media.j3d.DistanceLOD.class,
        javax.media.j3d.ExponentialFog.class,
        javax.media.j3d.Font3D.class,
        javax.media.j3d.Group.class,
        javax.media.j3d.ImageComponent2D.class,
        javax.media.j3d.ImageComponent3D.class,
        javax.media.j3d.IndexedLineArray.class,
        javax.media.j3d.IndexedLineStripArray.class,
        javax.media.j3d.IndexedPointArray.class,
        javax.media.j3d.IndexedQuadArray.class,
        javax.media.j3d.IndexedTriangleArray.class,
        javax.media.j3d.IndexedTriangleFanArray.class,
        javax.media.j3d.IndexedTriangleStripArray.class,
        javax.media.j3d.LinearFog.class,
        javax.media.j3d.LineArray.class,
        javax.media.j3d.LineAttributes.class,
        javax.media.j3d.LineStripArray.class,
        javax.media.j3d.Link.class,
        javax.media.j3d.Material.class,
        javax.media.j3d.Morph.class,
        javax.media.j3d.OrderedGroup.class,
        javax.media.j3d.OrientedShape3D.class,
        javax.media.j3d.PathInterpolator.class,
        javax.media.j3d.PointArray.class,
        javax.media.j3d.PointAttributes.class,
        javax.media.j3d.PositionInterpolator.class,
        javax.media.j3d.PositionPathInterpolator.class,
        javax.media.j3d.QuadArray.class,
        javax.media.j3d.RenderingAttributes.class,
        javax.media.j3d.RotationInterpolator.class,
        javax.media.j3d.RotationPathInterpolator.class,
        javax.media.j3d.RotPosPathInterpolator.class,
        javax.media.j3d.RotPosScalePathInterpolator.class,
        javax.media.j3d.ScaleInterpolator.class,
        javax.media.j3d.Shape3D.class,
        javax.media.j3d.SharedGroup.class,
        javax.media.j3d.Soundscape.class,
        javax.media.j3d.SpotLight.class,
        javax.media.j3d.Switch.class,
        javax.media.j3d.SwitchValueInterpolator.class,
        javax.media.j3d.Text3D.class,
        javax.media.j3d.Texture2D.class,
        javax.media.j3d.Texture3D.class,
        javax.media.j3d.TextureAttributes.class,
        javax.media.j3d.TextureCubeMap.class,
        javax.media.j3d.TextureUnitState.class,
        javax.media.j3d.TransformGroup.class,
        javax.media.j3d.TransformInterpolator.class,
        javax.media.j3d.TransparencyAttributes.class,
        javax.media.j3d.TransparencyInterpolator.class,
        javax.media.j3d.TriangleArray.class,
        javax.media.j3d.TriangleFanArray.class,
        javax.media.j3d.TriangleStripArray.class,
        javax.media.j3d.ViewPlatform.class
    };

    public Class getNodeClassFromID( int classID ) {
        if (classID<0)
            return null;
        else
            return j3dClasses[classID];
    }
    
    // TODO Use a HashMap to eliminate the linear search for the class
    //
    public int getNodeClassID( javax.media.j3d.SceneGraphObject node ) {
        
        int ret = -1;
        Class cl = node.getClass();
        
        for(int i=0; i<j3dClasses.length && ret==-1; i++)
            if (j3dClasses[i]==cl)
                ret = i;
        
        return ret;
    }
    
    /**
     * Associate the name with the scene graph object 
     */
    public void addNamedObject( String name, SceneGraphObject object ) {
        symbolTable.addNamedObject( name, object );
    }
    
    /**
     * Return the SceneGraphObject associated with the name
     */
    public SceneGraphObject getNamedObject( String name ) throws NamedObjectException, ObjectNotLoadedException {
        return symbolTable.getNamedObject( name );
    }
    
    /**
     * Get all the names of the named objects
     */
    public String[] getNames() {
        return symbolTable.getNames();
    }
    
    /**
     * Write a serializable object to the current file position, proceeded by
     * the size of the object
     */
    public void writeSerializedData( DataOutput dataOutput, java.io.Serializable userData ) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream( out );
        
        objOut.writeObject( userData );
        
        out.close();
        
        byte[] bytes = out.toByteArray();
        dataOutput.writeInt( bytes.length );
        if (bytes.length!=0)
            dataOutput.write( bytes );
    }
    
    public Object readSerializedData( DataInput dataInput ) throws IOException {
        int size = dataInput.readInt();
        Object userData = null;
        
        if (size!=0) {
            byte[] bytes = new byte[size];
            dataInput.readFully( bytes );

            ByteArrayInputStream in = new ByteArrayInputStream( bytes );
            J3dIOObjectInputStream objIn = new J3dIOObjectInputStream( in );

            try {
                userData = objIn.readObject();
                objIn.close();
            } catch( ClassNotFoundException e ) {
                System.out.println("WARNING: Unable to load UserData");
                System.out.println("Class missing "+e);
                objIn.close();
            }
        }
        
        return userData;
    }
    
    /**
     * Skip past the user data object
     */
    public void skipUserData( DataInput dataInput ) throws IOException {
        int size = dataInput.readInt();
        dataInput.skipBytes( size );
    }
    
    
    
    public void writeColor3f( DataOutput out, Color3f color ) throws IOException {
        out.writeFloat( color.x );
        out.writeFloat( color.y );
        out.writeFloat( color.z );
    }
    
    public Color3f readColor3f( DataInput in ) throws IOException {
        return new Color3f( in.readFloat(), in.readFloat(), in.readFloat() );
    }
    
    public void writeColor4f( DataOutput out, Color4f vec ) throws IOException {
        writeTuple4f( out, vec );
    }
    
    public Color4f readColor4f( DataInput in ) throws IOException {
        return (Color4f)readTuple4f( in, new Color4f() );
    }
    
    public void writePoint3f( DataOutput out, Point3f pt ) throws IOException {
        writeTuple3f( out, pt );
    }
    
    public Point3f readPoint3f( DataInput in ) throws IOException {
        return (Point3f)readTuple3f( in, new Point3f() );
    }

    public void writePoint3d( DataOutput out, Point3d pt ) throws IOException {
        writeTuple3d( out, pt );
    }
    
    public Point3d readPoint3d( DataInput in ) throws IOException {
        return (Point3d)readTuple3d( in, new Point3d() );
    }
    
    public void writeVector3f( DataOutput out, Vector3f vec ) throws IOException {
        writeTuple3f( out, vec );
    }
    
    public Vector3f readVector3f( DataInput in ) throws IOException {
        return (Vector3f)readTuple3f( in, new Vector3f() );
    }

    public void writeVector4d( DataOutput out, Vector4d vec ) throws IOException {
        writeTuple4d( out, vec );
    }
    
    public Vector4d readVector4d( DataInput in ) throws IOException {
        return (Vector4d)readTuple4d( in, new Vector4d() );
    }
    
    public void writeVector4f( DataOutput out, Vector4f vec ) throws IOException {
        writeTuple4f( out, vec );
    }
    
    public Vector4f readVector4f( DataInput in ) throws IOException {
        return (Vector4f)readTuple4f( in, new Vector4f() );
    }
    
    public void writeQuat4f( DataOutput out, Quat4f vec ) throws IOException {
        writeTuple4f( out, vec );
    }
    
    public Quat4f readQuat4f( DataInput in ) throws IOException {
        return (Quat4f)readTuple4f( in, new Quat4f() );
    }

    public void writeMatrix4d( DataOutput out, Matrix4d m ) throws IOException {
        for(int r=0; r<4; r++)
            for(int c=0; c<4; c++)
                out.writeDouble( m.getElement( r, c ));
    }
    
    public Matrix4d readMatrix4d( DataInput in ) throws IOException {
        double elements[] = new double[16];
        for(int c=0; c<16; c++)
            elements[ c ] = in.readDouble();
        
        return new Matrix4d(elements);
    }
    
    public void writeTuple3f( DataOutput out, Tuple3f tuple ) throws IOException {
        out.writeFloat( tuple.x );
        out.writeFloat( tuple.y );
        out.writeFloat( tuple.z );
    }
    
    public Tuple3f readTuple3f( DataInput in, Tuple3f tuple ) throws IOException {
        tuple.x = in.readFloat();
        tuple.y = in.readFloat();
        tuple.z = in.readFloat();
        return tuple;
    }
    
    public void writeTuple3d( DataOutput out, Tuple3d tuple ) throws IOException {
        out.writeDouble( tuple.x );
        out.writeDouble( tuple.y );
        out.writeDouble( tuple.z );
    }
    
    public Tuple3d readTuple3d( DataInput in, Tuple3d tuple ) throws IOException {
        tuple.x = in.readDouble();
        tuple.y = in.readDouble();
        tuple.z = in.readDouble();
        return tuple;
    }
    
    public void writeTuple4d( DataOutput out, Tuple4d tuple ) throws IOException {
        out.writeDouble( tuple.x );
        out.writeDouble( tuple.y );
        out.writeDouble( tuple.z );
        out.writeDouble( tuple.w );
    }
    
    public Tuple4d readTuple4d( DataInput in, Tuple4d tuple ) throws IOException {
        tuple.x = in.readDouble();
        tuple.y = in.readDouble();
        tuple.z = in.readDouble();
        tuple.w = in.readDouble();
        return tuple;
    }
    
    public void writeTuple4f( DataOutput out, Tuple4f tuple ) throws IOException {
        out.writeFloat( tuple.x );
        out.writeFloat( tuple.y );
        out.writeFloat( tuple.z );
        out.writeFloat( tuple.w );
    }
    
    public Tuple4f readTuple4f( DataInput in, Tuple4f tuple ) throws IOException {
        tuple.x = in.readFloat();
        tuple.y = in.readFloat();
        tuple.z = in.readFloat();
        tuple.w = in.readFloat();
        return tuple;
    }
    
    public void writeTransform3D( DataOutput out, Transform3D tran ) throws IOException {
        Matrix4d matrix = new Matrix4d();
        tran.get( matrix );
        writeMatrix4d( out, matrix );
    }
    
    public Transform3D readTransform3D( DataInput in ) throws IOException {
        Transform3D ret = new Transform3D();
        ret.set( readMatrix4d( in ));
        return ret;
    }
    
    public void writeBounds( DataOutput out, Bounds bounds ) throws IOException {
        if (bounds==null) {
            out.writeInt( 0 );
        } else if (bounds instanceof BoundingBox) {
            out.writeInt( 1 );          // Type
            Point3d p = new Point3d();
            ((BoundingBox)bounds).getLower( p );
            writePoint3d( out, p );
            ((BoundingBox)bounds).getUpper( p );
            writePoint3d( out, p );
        } else if (bounds instanceof BoundingSphere) {
            out.writeInt( 2 );          // Type
            Point3d p = new Point3d();
            ((BoundingSphere)bounds).getCenter( p );
            writePoint3d( out, p );
            out.writeDouble( ((BoundingSphere)bounds).getRadius() );
        } else if (bounds instanceof BoundingPolytope ) {
            out.writeInt( 3 );          // Type
            Vector4d[] planes = new Vector4d[ ((BoundingPolytope)bounds).getNumPlanes() ];
            ((BoundingPolytope)bounds).getPlanes( planes );
            out.writeInt( planes.length );
            for(int i=0; i<planes.length; i++)
                writeVector4d( out, planes[i] );
        } else {
            throw new IOException( "Unsupported bounds class "+bounds.getClass().getName() );
        }
    }
    
    public Bounds readBounds( DataInput in ) throws IOException {
        Bounds bounds;
        switch( in.readInt() ) {
            case 0:
                bounds = null;
                break;
            case 1:
                bounds = new BoundingBox( readPoint3d(in), readPoint3d(in) );
                break;
            case 2:
                bounds = new BoundingSphere( readPoint3d(in), in.readDouble() );
                break;
            case 3:
                Vector4d[] planes = new Vector4d[ in.readInt() ];
                for(int i=0; i<planes.length; i++)
                    planes[i] = readVector4d( in );
                bounds = new BoundingPolytope(planes);
                break;
            default:
                throw new SGIORuntimeException("Unrecognised bounds class");
        }
        return bounds;
    }
    
/**
     * Get the current file 'pointer' location.
     */
    public abstract long getFilePointer();
    
    public abstract void close() throws IOException;
    
    /**
     * Indicates to SceneGraphObjectState that it should use the
     * Java3D core superclass for any tree nodes whose classes are
     * not in the classpath during a load.
     */
    public boolean useSuperClassIfNoChildClass() {
        return useSuperClass;
    }
    
    /**
     * Returns the imageCompression to be used
     * IMAGE_COMPRESSION_NONE, IMAGE_COMPRESSION_GZIP, IMAGE_COMPRESSION_JPEG
     */
    public int getImageCompression() {
        return imageCompression;
    }
     

    /** 
      * An ObjectInputStream that uses a different classLoader
      */
    class J3dIOObjectInputStream extends ObjectInputStream {
        public J3dIOObjectInputStream( java.io.InputStream in ) throws
				IOException { 
	    super(in);
        }

        protected Class resolveClass( java.io.ObjectStreamClass desc ) throws
				IOException, ClassNotFoundException {
            return getClass().forName( desc.getName(), true, classLoader );
        }
    }
}
