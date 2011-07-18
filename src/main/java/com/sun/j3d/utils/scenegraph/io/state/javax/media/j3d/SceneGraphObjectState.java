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

package com.sun.j3d.utils.scenegraph.io.state.javax.media.j3d;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.media.j3d.SceneGraphObject;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector4d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Tuple4d;
import com.sun.j3d.utils.scenegraph.io.retained.Controller;
import com.sun.j3d.utils.scenegraph.io.retained.SymbolTableData;
import com.sun.j3d.utils.scenegraph.io.retained.SGIORuntimeException;

public abstract class SceneGraphObjectState {
    
    protected SceneGraphObject node;
    protected SymbolTableData symbol;
    protected Controller control;
    protected String nodeClassName;
    
    /**
     * Create a new State object
     *
     * During Saveing
     *  SymbolTableData will have the nodeID and j3dNode fields set
     *
     * During loading
     *  SymbolTableData be null, symbol will be created and added to the
     * symbol data during readObject()
     */
    public SceneGraphObjectState( SymbolTableData symbol, Controller control ) {
        this.symbol = symbol;
        this.control = control;
        
        if (symbol!=null) {
            this.node = symbol.j3dNode;
            
            // This consistancy check is for debugging purposes
            //if (symbol.j3dNode==null || symbol.nodeID==0)
            //    throw new RuntimeException( "Bad Symbol in State creation");
        }
        
        
        if (node!=null) {
            nodeClassName = node.getClass().getName();
            
	    try {
                if (node instanceof com.sun.j3d.utils.scenegraph.io.SceneGraphIO)
                    ((com.sun.j3d.utils.scenegraph.io.SceneGraphIO)node).createSceneGraphObjectReferences( control.getSymbolTable() );
            } catch(Exception e) {
                System.err.println("Exception in createSceneGraphObjectReferences");
                e.printStackTrace();
            }

        }
        
    }
    
    /**
     * DO NOT call symbolTable.addReference in writeObject as this (may)
     * result in a concurrentModificationException.
     *
     * All references should be created in the constructor
     */
    public void writeObject( DataOutput out ) throws IOException {  
        boolean sgIO = node instanceof com.sun.j3d.utils.scenegraph.io.SceneGraphIO;
        out.writeBoolean( sgIO );
        out.writeInt( symbol.nodeID );
        
        
        int nodeClassID = control.getNodeClassID( node );
        
        out.writeShort( nodeClassID );
        
        if (nodeClassID==-1)
            out.writeUTF( nodeClassName );
        
        writeConstructorParams( out );
        
        if (sgIO) {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
	    DataOutputStream tmpOut = new DataOutputStream( byteStream );
            ((com.sun.j3d.utils.scenegraph.io.SceneGraphIO)node).writeSceneGraphObject( tmpOut );
	    tmpOut.close();
	    out.writeInt( byteStream.size() );
	    out.write( byteStream.toByteArray() );
        }
        
        writeUserData( out );
        writeString(node.getName(), out);
        
        writeCapabilities( out );
    }

    public void readObject( DataInput in ) throws IOException {
       
        boolean sgIO = in.readBoolean();
        int nodeID = in.readInt();
        
	int nodeClassID = in.readShort();

	nodeClassName = null;
	
	if ( nodeClassID==-1 )
	    nodeClassName = in.readUTF();
	
	readConstructorParams( in );
	
	if ( nodeClassID!=-1 ) {
	    node = createNode();
	    nodeClassName = node.getClass().getName();
	} else 
	    node = createNode(nodeClassName);
        

        if ( sgIO ) {
	    if (control.getCurrentFileVersion()==1)
                ((com.sun.j3d.utils.scenegraph.io.SceneGraphIO)node).readSceneGraphObject( in );
            else {
	        int size = in.readInt();
                if (node instanceof com.sun.j3d.utils.scenegraph.io.SceneGraphIO) {
	            byte[] bytes = new byte[size];
		    in.readFully( bytes );
                    ByteArrayInputStream byteStream = new ByteArrayInputStream( bytes );
                    DataInputStream tmpIn = new DataInputStream( byteStream );
                    ((com.sun.j3d.utils.scenegraph.io.SceneGraphIO)node).readSceneGraphObject( tmpIn );
		    tmpIn.close();
                } else {
	    	    in.skipBytes( size );
                }
            }
        }
        
        symbol = control.getSymbolTable().createSymbol( this, node, nodeID );
        readUserData( in );
        if (control.getCurrentFileVersion()>2) {
            node.setName(readString(in));
        }

        readCapabilities( in );
    }
    
    public SceneGraphObject getNode() {
        return node;
    }
    
    public int getNodeID() {
        return symbol.nodeID;
    }
    
    public SymbolTableData getSymbol() {
        return symbol;
    }
        
    private void readUserData( DataInput in ) throws IOException {
        
        node.setUserData( control.readSerializedData( in ));
    }
    
    private void writeUserData( DataOutput out ) throws IOException {
        Object obj = node.getUserData();
        if (obj != null && !(obj instanceof java.io.Serializable)) {
            System.err.println("UserData is not Serializable and will not be saved");
            obj = null;
        }
        
        control.writeSerializedData( out, (Serializable)obj );
    }
    
    /*
     * NOTE:  This implementation assumes a maximum of 64 capability
     * bits per node class.  If this changes in the future, this
     * implementation will need to be updated.
     */
    private void writeCapabilities( DataOutput out ) throws IOException {
	long capabilities = 0;
	long frequentCapabilities = 0;

	for ( int i=0;i<64;i++ ) {
	    if ( node.getCapability( i ) ) capabilities |= (1L << i);
	    if ( !(node.getCapabilityIsFrequent( i )) ) frequentCapabilities |= (1L << i);
	}
        out.writeLong( capabilities );
        out.writeLong( frequentCapabilities );
    }
    
    private void readCapabilities( DataInput in ) throws IOException {
	long capabilities = in.readLong();
	long frequentCapabilities = in.readLong();

	for ( int i=0;i<64;i++ ) {
	    if ( (capabilities&(1L<<i))!=0L ) node.setCapability( i );
	    if ( (frequentCapabilities&(1L<<i))!=0L ) node.clearCapabilityIsFrequent( i );
	}
    }

    /**
      * Write the parameters required for the constructor of the Java3D object
      */
    protected void writeConstructorParams( DataOutput out ) throws
							IOException {
    }

    /**
      * Read the parameters required for the constructor of the Java3D object
      */
    protected void readConstructorParams( DataInput in ) throws
							IOException {
    }
    
    /**
     * Create a new Java3D node for this object.
     *
     * This method is ONLY used when the Java3D Class type matches the
     * State type, ie this does NOT handle subclasses of Java3D.
     *
     * For Java3D subclasses use createNode( Class state)
     *
     * This method MUST be implemented by all State objects but is not
     * abstract to allow for external subclassing
     */
    protected SceneGraphObject createNode() {
	throw new SGIORuntimeException("createNode() not implemented in class "+this.getClass().getName());
    }
    
    /**
      * Create a new Java3D node from the supplied class using the parameterless constructor
      * 
      * For Java3D nodes which do not have a default constructor you must
      * overload this method and create the object using createNode( className, parameters )
      * This will correctly handle subclasses of Java3D classes
     */
    protected SceneGraphObject createNode( Class state ) {
	SceneGraphObject ret;

	try {
	    ret = (SceneGraphObject)state.newInstance();

	    //System.err.println("Created J3D node for "+className );
	} catch( IllegalAccessException exce ) {
	    throw new SGIORuntimeException( "Broken State class for "+
						state.getClass().getName()+" - IllegalAccess" );
	} catch( InstantiationException excep ) {
	    throw new SGIORuntimeException( "Broken State class for "+
						state.getClass().getName() );
	}

	return ret;
    }    
    
    /**
      * Create a new Java3D node from the supplied class name using the parameterless constructor
      * 
      * For Java3D nodes which do not have a default constructor you must
      * overload this method and create the object using createNode( className, parameters )
      * This will correctly handle subclasses of Java3D classes
     */
    protected SceneGraphObject createNode( String className ) {
	SceneGraphObject ret;

	try {
            Class state = Class.forName( className, true, control.getClassLoader() );
            
	    ret = createNode( state );

	    //System.err.println("Created J3D node for "+className );
	} catch(ClassNotFoundException e) {
            if (control.useSuperClassIfNoChildClass())
                ret = createNodeFromSuper( className );
            else
                throw new SGIORuntimeException( "No Such Class "+
						className );
	}

	return ret;
    }
    
    /**
     * If createNode cannot locate the correct class to instantiate
     * the node this method is called and will instantiate the
     * node using it's Java3D Core superclass
     */
    private SceneGraphObject createNodeFromSuper( String className ) {
	SceneGraphObject ret;
        
        String tmp = this.getClass().getName();
        String superClass = tmp.substring( tmp.indexOf("state")+6, tmp.length()-5 );

        System.err.println("Unable to create node "+className+" attempting Java3D superclass "+superClass );
        
	try {
            Class state = Class.forName( superClass );
            
	    ret = (SceneGraphObject)state.newInstance();

	} catch(ClassNotFoundException e) {
	    throw new SGIORuntimeException( "No Such Class "+
						className );
	} catch( IllegalAccessException exce ) {
	    throw new SGIORuntimeException( "Broken State class for "+
						className+" - IllegalAccess" );
	} catch( InstantiationException excep ) {
	    throw new SGIORuntimeException( "Unable to instantiate class "+
						className );
	}
        
        return ret;
    }
    
    /**
     * Create a Java3D node which does not have a default constructor
     *
     * parameterTypes must contain the classes required by the constructor,
     * use Integer.TYPE, Float.TYPE etc to specifiy primitive types
     *
     * paramters should contain the list of parameters for the constructor,
     * primitive types should be wrapped in the appropriate class (ie Integer, Float )
     */
    private SceneGraphObject createNode( String className, Class[] parameterTypes, Object[] parameters ) {
        SceneGraphObject ret;
        Constructor constructor;

        try {
            Class state = Class.forName( className );
            constructor = state.getConstructor( parameterTypes );
            ret = (SceneGraphObject)constructor.newInstance( parameters );
	} catch(ClassNotFoundException e1) {
            if (control.useSuperClassIfNoChildClass())
                ret = createNodeFromSuper( className, parameterTypes, parameters );
            else
                throw new SGIORuntimeException( "No State class for "+
						className );
	} catch( IllegalAccessException e2 ) {
	    throw new SGIORuntimeException( "Broken State class for "+
						className+" - IllegalAccess" );
	} catch( InstantiationException e3 ) {
	    throw new SGIORuntimeException( "Broken State class for "+
						className );
        } catch( java.lang.reflect.InvocationTargetException e4 ) {
	    throw new SGIORuntimeException( "InvocationTargetException for "+
						className );
        } catch( NoSuchMethodException e5 ) {
            for(int i=0; i<parameterTypes.length; i++)
                System.err.println( parameterTypes[i].getName() );
            System.err.println("------");
	    throw new SGIORuntimeException( "Invalid constructor for "+
						className );
        }
        
        return ret;
    }

    /**
     * Create a Java3D node which does not have a default constructor
     *
     * parameterTypes must contain the classes required by the constructor,
     * use Interger.TYPE, Float.TYPE etc to specifiy primitive types
     *
     * paramters should contain the list of parameters for the constructor,
     * primitive types should be wrapped in the appropriate class (ie Integer, Float )
     */
    protected SceneGraphObject createNode( Class j3dClass, Class[] parameterTypes, Object[] parameters ) {
        SceneGraphObject ret;
        Constructor constructor;

        try {
            constructor = j3dClass.getConstructor( parameterTypes );
            ret = (SceneGraphObject)constructor.newInstance( parameters );
	} catch( IllegalAccessException e2 ) {
	    throw new SGIORuntimeException( "Broken State class for "+
						j3dClass.getClass().getName()+" - IllegalAccess" );
	} catch( InstantiationException e3 ) {
	    throw new SGIORuntimeException( "Broken State class for "+
						j3dClass.getClass().getName() );
        } catch( java.lang.reflect.InvocationTargetException e4 ) {
	    throw new SGIORuntimeException( "InvocationTargetException for "+
						j3dClass.getClass().getName() );
        } catch( NoSuchMethodException e5 ) {
            for(int i=0; i<parameterTypes.length; i++)
                System.err.println( parameterTypes[i].getName() );
            System.err.println("------");
	    throw new SGIORuntimeException( "Invalid constructor for "+
						j3dClass.getClass().getName() );
        }
        
        return ret;
    }
    
    /**
     * If createNode cannot locate the correct class to instantiate
     * the node this method is called and will instantiate the
     * node using it's Java3D Core superclass
     */
    private SceneGraphObject createNodeFromSuper( String className, Class[] parameterTypes, Object[] parameters ) {
	SceneGraphObject ret;
        
        String tmp = this.getClass().getName();
        String superClass = tmp.substring( tmp.indexOf("state")+6, tmp.length()-5 );
        Constructor constructor;

        try {
            Class state = Class.forName( superClass );
            constructor = state.getConstructor( parameterTypes );
            ret = (SceneGraphObject)constructor.newInstance( parameters );
	} catch(ClassNotFoundException e1) {
	    throw new SGIORuntimeException( "No State class for "+
						superClass );
	} catch( IllegalAccessException e2 ) {
	    throw new SGIORuntimeException( "Broken State class for "+
						className+" - IllegalAccess" );
	} catch( InstantiationException e3 ) {
	    throw new SGIORuntimeException( "Broken State class for "+
						className );
        } catch( java.lang.reflect.InvocationTargetException e4 ) {
	    throw new SGIORuntimeException( "InvocationTargetException for "+
						className );
        } catch( NoSuchMethodException e5 ) {
            for(int i=0; i<parameterTypes.length; i++)
                System.err.println( parameterTypes[i].getName() );
            System.err.println("------");
	    throw new SGIORuntimeException( "Invalid constructor for "+
						className );
        }
        
        return ret;
    }

    /**
      * Given a scene graph object instantiate the correct State class
      * for that object
      */
    protected SceneGraphObjectState createState( SceneGraphObject obj, Controller control ) {

        return control.createState( obj );
    }

    /**
      * Return the class name of the Class, the fully qualified classname
      * is stripped of all package information and returned
      */
    private String getClassName( Class c ) {
	return c.getName().substring(c.getName().lastIndexOf('.')+1);
    }

    /**
     * Subclasses should processes their own buildGraph requirements BEFORE
     * calling super.buildGraph().
     *
     * This ensures that when restoreSceneGraphObjectReferences is called in
     * user code our references have been resolved
     */
    public void buildGraph() {
        //System.err.println("Build Graph "+this);
            if (node instanceof com.sun.j3d.utils.scenegraph.io.SceneGraphIO)
                ((com.sun.j3d.utils.scenegraph.io.SceneGraphIO)node).restoreSceneGraphObjectReferences( control.getSymbolTable() );
    }
    
    public void cleanup() {
        control = null;
        node = null;
    }
    
    /**
     * Read and return a possibly null string
     */
    protected String readString(DataInput in) throws IOException {
            if (in.readBoolean())
                return (in.readUTF());
            return null;        
    }
    
    /**
      * Write a possibly null string to the stream
     */
    protected void writeString(String str, DataOutput out) throws IOException {
        out.writeBoolean(str!=null);
        if (str!=null)
            out.writeUTF(str);       
    }
    
}
