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

package javax.media.j3d;

import javax.vecmath.*;

/**
 * The Sensor Class encapsulates an object that provides real-time
 * data.  Examples include six-degree-of-freedom tracking, a joystick,
 * or a data file being read back during a program.  A sensor must be
 * used in conjuction with an implementation of the InputDevice
 * interface.<P> 
 * 
 * The Sensor object provides an abstract concept of a hardware
 * input device. A Sensor consists of a timestamped sequence of
 * input values and the state of buttons or switches at the time
 * that Java 3D sampled the value. A sensor also contains a hotspot
 * offset specified in the sensor's local coordinate system. If not
 * specified, the hotspot is (0.0, 0.0, 0.0).<P>
 *
 * Since a typical hardware environment may contain multiple sensing
 * elements, Java 3D maintains an array of sensors. Users can access
 * a sensor directly from their Java code or they can assign a sensor
 * to one of Java 3D's predefined 6DOF entities, such as UserHead.<P>
 *
 * Using a sensor is as easy as accessing an object. Write your
 * Java code to extract the associated sensor value from the array of
 * sensors. You can then directly apply that value to an element in a
 * scene graph or process the sensor values in whatever way necessary.<P>
 *
 * Java 3D includes three special six-degrees-of-freedom (6DOF) entities.
 * These include UserHead, DominantHand, and NondominantHand. You
 * can assign or change which sensor drives one
 * of these predefined entities. Java 3D uses the specified sensor to 
 * drive the 6DOF entity - most visibly the View.<P>
 *
 * Java 3D does not provide raw tracker or joystick-generated data in 
 * a sensor. At a minimum, Java 3D normalizes the raw data using the 
 * registration and calibration parameters either provided by or 
 * provided for the end user. It additionally may filter and process 
 * the data to remove noise and improve latency.
 * The application programmer can suppress this latter effect on a 
 * sensor-by-sensor basis.<P>
 *
 * @see SensorRead
 */

public class Sensor {

    /**
     * Set predictor type to do no prediction; this is the default.
     *
     * @deprecated As of Java 3D version 1.4, prediction is not a
     * supported feature.
     */
    public static final int PREDICT_NONE = 1;

    /**
     * @deprecated As of Java 3D version 1.4, prediction is not a
     * supported feature.
     */
    public static final int PREDICT_NEXT_FRAME_TIME = 2;

    /**
     * Use no prediction policy; this is the default.
     *
     * @deprecated As of Java 3D version 1.4, prediction is not a
     * supported feature.
     */
    public static final int NO_PREDICTOR = 16;

    /**
     * @deprecated As of Java 3D version 1.4, prediction is not a
     * supported feature.
     */
    public static final int HEAD_PREDICTOR = 32;

    /**
     * @deprecated As of Java 3D version 1.4, prediction is not a
     * supported feature.
     */
    public static final int HAND_PREDICTOR = 64;

    /**
     * Default SensorRead object count (30); the number of SensorRead 
     * objects constructed if no count is specified.
     */
    public static final int DEFAULT_SENSOR_READ_COUNT = 30;

    /**
     * SENSOR_READ_COUNT_BUFFER is the number of extra sensor reading  
     * values to store at the end of the circular list.  It helps provide
     * MT-safeness.  This is necessary if someone asks for the last
     * k sensor values and k is close to sensor read count.
     * This helps avoid some synchronization statements in getRead
     * and setNextSensorRead.
     */
    static final int SENSOR_READ_COUNT_BUFFER = 15;

    static int num_reads_so_far = 0;

    // specifies whether a DEMAND_DRIVEN device has been added that 
    // manages this sensor
    boolean demand_driven = false;

    // size of the sensor read buffer
    int sensorReadCount;

    // Prediction policy -- unused
    private int predictionPolicy = NO_PREDICTOR; 

    // Predictor type -- unused
    private int predictorType = PREDICT_NONE;

    // This sensor's associated device
    InputDevice device;

    SensorRead readings[];
    int currentIndex;
    int lastIndex;
    Point3d hotspot;
    int MaxSensorReadIndex;

    // The count of the number of buttons associated with this sensor.
    int sensorButtonCount;

    // These matrices used as a temporary workspace for the local SVD
    // calculations (thus minimimizing garbage collection).
    Matrix3d orig_rot = new Matrix3d(); 
    Matrix3d orig_rot_transpose = new Matrix3d(); 
    Matrix3d temp_rot = new Matrix3d(); 
    Matrix3d local_svd = new Matrix3d(); 


    /**
     * Constructs a Sensor object for the specified input device using
     * default parameters.  The default values are as follows:
     * <ul>
     * sensor read count : 30<br>
     * sensor button count : 0<br>
     * hot spot : (0,0,0)<br>
     * predictor : PREDICT_NONE &mdash; <i>this attribute is unused</i><br>
     * prediction policy : NO_PREDICTOR &mdash; <i>this attribute is unused</i><br>
     * </ul>
     * @param device the Sensor's associated device.
     */
    public Sensor(InputDevice device){
	this(device, DEFAULT_SENSOR_READ_COUNT, 0, new Point3d(0.0, 0.0, 0.0));
    }

    /**
     * Constructs a Sensor object for the specified input device using
     * the specified number of SensorRead objects.
     * Default values are used for all other parameters.
     * @param device the Sensor's associated device
     * @param sensorReadCount the number of SensorReads to associate with
     * this sensor
     */
    public Sensor(InputDevice device, int sensorReadCount){
	this(device, sensorReadCount, 0, new Point3d(0.0, 0.0, 0.0));
    }

    /**
     * Constructs a Sensor object for the specified input device using
     * the specified number of SensorRead objects and number of buttons.
     * Default values are used for all other parameters.
     * @param device the Sensor's associated device
     * @param sensorReadCount the number of SensorReads to associate with
     * this sensor
     * @param sensorButtonCount the number of buttons associated with each
     * sensor read
     */
    public Sensor(InputDevice device, int sensorReadCount,
		  int sensorButtonCount){
	this(device, sensorReadCount, sensorButtonCount,
	     new Point3d(0.0,0.0, 0.0));
    }

    /**  
     * Constructs a Sensor object for the specified input device using
     * the specified hotspot.
     * Default values are used for all other parameters.
     * @param device the Sensor's associated device
     * @param hotspot the Sensor's hotspot defined in its local coordinate
     * system
     */
    public Sensor(InputDevice device, Point3d hotspot){
	this(device, DEFAULT_SENSOR_READ_COUNT, 0, hotspot);
    }

    /**
     * Constructs a Sensor object for the specified input device using
     * the specified number of SensorRead objects and hotspot.
     * Default values are used for all other parameters.
     * @param device the Sensor's associated device
     * @param sensorReadCount the number of SensorReads to associate with
     * this sensor
     * @param hotspot the Sensor's hotspot defined in its local coordinate
     * system
     */
    public Sensor(InputDevice device, int sensorReadCount, Point3d hotspot){
	this(device, sensorReadCount, 0, hotspot);
    }

    /**
     * Constructs a Sensor object for the specified input device using
     * the specified number of SensorRead objects, number of buttons, and
     * hotspot.
     * Default values are used for all other parameters.
     * @param device the Sensor's associated device
     * @param sensorReadCount the number of SensorReads to associate with
     * this sensor
     * @param sensorButtonCount the number of buttons associated with each
     * sensor read
     * @param hotspot the Sensor's hotspot defined in its local coordinate
     * system
     */
    public Sensor(InputDevice device, int sensorReadCount,
		  int sensorButtonCount, Point3d hotspot){
        this.device = device;
        this.sensorReadCount = sensorReadCount;
        this.MaxSensorReadIndex = sensorReadCount + SENSOR_READ_COUNT_BUFFER - 1;
	this.sensorButtonCount = sensorButtonCount;
        readings = new SensorRead[MaxSensorReadIndex + 1];
        for(int i = 0; i < MaxSensorReadIndex + 1; i++){
            readings[i] = new SensorRead(sensorButtonCount);
        }
        currentIndex = 0;
        this.hotspot = new Point3d(hotspot);
    }

    //  argument of 0 is last reading (ie, currentIndex), argument 
    //  of 1 means next to last index, etc.
    int previousIndex(int k){
	int temp = currentIndex - k;
        return(temp >= 0 ? temp : MaxSensorReadIndex + temp + 1);
    }

    /**
     * Sets the type of predictor to use with this sensor.
     * Since prediction is not implemented (and never has been), this
     * attribute has no effect.
     * @param predictor predictor type one of PREDICT_NONE or
     * PREDICT_NEXT_FRAME_TIME
     * @exception IllegalArgumentException if an invalid predictor type
     *  is specified.
     *
     * @deprecated As of Java 3D version 1.4, prediction is not a
     * supported feature.
     */
    public void setPredictor(int predictor){
       if (predictor != PREDICT_NONE && predictor != PREDICT_NEXT_FRAME_TIME) {
           throw new IllegalArgumentException(J3dI18N.getString("Sensor0"));
       } else {
	    predictorType = predictor;
       }
    }

    /**
     * Returns the type of predictor used by this sensor.
     * @return the predictor type.
     *
     * @deprecated As of Java 3D version 1.4, prediction is not a
     * supported feature.
     */ 
    public int getPredictor(){
	return predictorType;
    }

    /**
     * Sets the prediction policy use by this sensor.
     * Since prediction is not implemented (and never has been), this
     * attribute has no effect.
     * @param policy prediction policy one of NO_PREDICTOR, HEAD_PREDICTOR,
     * or HAND_PREDICTOR
     * @exception IllegalArgumentException if an invalid prediction policy
     *  is specified.
     *
     * @deprecated As of Java 3D version 1.4, prediction is not a
     * supported feature.
     */
    public void setPredictionPolicy(int policy){
	if (policy != NO_PREDICTOR && policy != HEAD_PREDICTOR &&
                 policy != HAND_PREDICTOR)
	    throw new IllegalArgumentException(J3dI18N.getString("Sensor1"));
	else
	    predictionPolicy = policy;
    }

    /**
     * Returns the prediction policy used by this sensor.
     * @return the prediction policy.
     *
     * @deprecated As of Java 3D version 1.4, prediction is not a
     * supported feature.
     */ 
    public int getPredictionPolicy(){
	return predictionPolicy;
    }

    /**
     * Set the sensor's hotspot in this sensor's coordinate system.
     * @param hotspot the sensor's new hotspot
     */
    public void setHotspot(Point3d hotspot){
	this.hotspot.set(hotspot);
    }

    /**
     * Get the sensor's hotspot in this sensor's coordinate system.
     * @param hotspot the variable to receive the sensor's hotspot
     */
    public void getHotspot(Point3d hotspot){
	hotspot.set(this.hotspot);
    }

    /**
     * Set the sensor's associated input device.
     * @param device the sensor's new device
     */
    public void setDevice(InputDevice device){
	this.device = device;
    }

    /**
     * Retrieves the sensor's associated input device.
     * @return the sensor's device
     */
    public InputDevice getDevice(){
	return device;
    }

    /**
     * Retrieves the last sensor reading and copies that value into
     * the specified argument.
     *
     * @param read the matrix that will receive the sensor reading
     */
    public void getRead(Transform3D read) {
        if(demand_driven == true)
             device.pollAndProcessInput();

	read.set(readings[currentIndex].read);
    }

    /**
     * Retrieves the last sensor reading and copies that value into
     * the specified argument.
     *
     * @param read the matrix that will receive the sensor reading
     * @param deltaT this parameter is ignored
     *
     * @deprecated As of Java 3D version 1.4, prediction is not a
     * supported feature; use <code>getRead(Transform3D)</code> instead.
     */
    public void getRead(Transform3D read, long deltaT){
	getRead(read);
    }

    /**
     * Extracts the most recent sensor reading and copies that value into
     * the specified argument.
     * @param read the matrix that will receive the most recent sensor reading
     */
    public void lastRead(Transform3D read){
	read.set(readings[currentIndex].read);
    }

    /**
     * Extracts the kth-most recent sensor reading and copies that value into
     * the specified argument; where 0 is the most recent sensor reading, 1 is 
     * the next most recent sensor reading, etc.  
     * @param read the matrix that will receive the most recent sensor reading
     * @param kth  the kth previous sensor reading
     */
    public void lastRead(Transform3D read, int kth){
        if(kth >= sensorReadCount) {
   throw new IllegalArgumentException(J3dI18N.getString("Sensor3"));
        }
	read.set(readings[previousIndex(kth)].read);
    }

    /**
     * Returns the time associated with the most recent sensor reading.
     * @return the time associated with the most recent sensor reading.
     */
    public long lastTime(){
	return readings[currentIndex].time;
    }

    /**
     * Returns the time associated with the kth-most recent sensor reading;
     * where 0 is the most recent sensor reading, 1 is the next most recent
     * sensor reading, etc.
     * @return the time associated with the kth-most recent sensor reading.
     */
    public long lastTime(int k){
        if(k >= sensorReadCount) {
           throw new IllegalArgumentException(J3dI18N.getString("Sensor4"));
        }
	return readings[previousIndex(k)].time;
    }

    /**
     *  Places the most recent sensor reading value for each button into
     *  the array parameter; will throw an ArrayIndexOutOfBoundsException
     *  if values.length is less than the number of buttons.
     *  @param values the array into which the button values will be
     *  placed
     */
    public void lastButtons(int[] values) {
       System.arraycopy(readings[currentIndex].buttonValues, 0, values,  
                            0, sensorButtonCount);
    }

    /**
     *  Places the kth-most recent sensor reading value for each button into
     *  the array parameter; where k=0 is the most recent sensor reading, k=1 
     *  is the next most recent sensor reading, etc.;  will throw an 
     *  ArrayIndexOutOfBoundsException if values.length is less than
     *  the number of buttons.
     *  @param k  the time associated with the most recent sensor reading
     *  @param values the array into which the button values will be
     *  placed.
     */
    public void lastButtons(int k, int[] values) {
        if(k >= sensorReadCount) {
       throw new IllegalArgumentException(J3dI18N.getString("Sensor5"));
        }
       System.arraycopy(readings[previousIndex(k)].buttonValues, 0, values,
                            0, sensorButtonCount);
    }

    /**
     * Returns the number of SensorRead objects associated with
     * this sensor.
     * @return the number of SensorReadObjects associated with this sensor
     */
    public int getSensorReadCount() {
	return this.sensorReadCount;
    }

    /**
     * Set the number of sensor read objects per Sensor. This is a 
     * calibration parameter that should normally be set in this 
     * object's constructor.  Calling this method resets all of this
     * sensor's values that are already in the buffer.
     * It is illegal to change this value after the device has been
     * added to the scheduler.
     * @param count the new sensor read count
     */
    public void setSensorReadCount(int count) {
        sensorReadCount = count;
        MaxSensorReadIndex = sensorReadCount + SENSOR_READ_COUNT_BUFFER - 1;
        readings = new SensorRead[MaxSensorReadIndex + 1];
        for(int i = 0; i < MaxSensorReadIndex + 1; i++){
            readings[i] = new SensorRead(sensorButtonCount);
        }
        currentIndex = 0;
    }
    

    /**
     * Returns the number of buttons associated with this sensor.
     * @return the number of buttons associated with this sensor.
     */
    public int getSensorButtonCount() {
	return sensorButtonCount;
    }

    /**
     * Gets the current sensor read.
     * @return the current sensor read object
     */
    public SensorRead getCurrentSensorRead() {
        // not sure if this should return a reference or a copy 
        SensorRead read = new SensorRead(sensorButtonCount);
        read.set(readings[currentIndex]);
	return read;
    }

    /**
      *  Sets the next sensor read to the specified values; once these
      *  values are set via this method they become the current values
      *  returned by methods such as lastRead(), lastTime(), and 
      *  lastButtons(); note that if there are no buttons associated with
      *  this sensor, values can just be an empty array.
      *  @param time  the next SensorRead's associated time
      *  @param transform  the next SensorRead's transformation
      *  @param values the next SensorRead's buttons' states
      */
    public void setNextSensorRead(long time, Transform3D transform,
				  int[] values) {

        int temp = currentIndex + 1;
        if (temp > MaxSensorReadIndex) temp = 0;
 
        readings[temp].setTime(time);
        readings[temp].set(transform);
        if(sensorButtonCount > 0)
           readings[temp].setButtons(values);
        currentIndex = temp;
    }

    /**
      *  Sets the next sensor read to the specified values; once these
      *  values are set via this method they become the current values
      *  returned by methods such as lastRead(), lastTime(), and
      *  lastButtons().
      *  @param read  the next SensorRead's values
      */ 
   public void setNextSensorRead(SensorRead read) {
        int temp = currentIndex + 1; 
        if (temp > MaxSensorReadIndex) temp = 0; 
        readings[temp].set(read);
        currentIndex = temp;
   }

}
