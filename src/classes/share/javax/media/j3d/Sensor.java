/*
 * $RCSfile$
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
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
     */
    public static final int PREDICT_NONE = 1;

    /**
     * Set predictor type to generate the SensorRead to correspond with 
     * the next frame time.
     */
    public static final int PREDICT_NEXT_FRAME_TIME = 2;

    /**
     * Use no prediction policy; this is the default.
     */
    public static final int NO_PREDICTOR = 16;

    /**
     * Set the predictor policy to assume the sensor is predicting head 
     * position/orientation.
     */
    public static final int HEAD_PREDICTOR = 32;

    /**
     * Set the predictor policy to assume the sensor is predicting hand 
     * position/orientation.
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

    // Default prediction policy: don't predict
    int predictionPolicy = NO_PREDICTOR; 

    // Default Predictor none
    int predictorType = PREDICT_NONE;

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

    // Prediction workspace -- these may go away when the readings array
    //  is used.
    static int MAX_PREDICTION_LENGTH = 20;
    Transform3D[]  previousReads = new Transform3D[MAX_PREDICTION_LENGTH];
    long[] times = new long[MAX_PREDICTION_LENGTH];
 

    /**
     * Constructs a Sensor object for the specified input device using
     * default parameters.  The default values are as follows:
     * <ul>
     * sensor read count : 30<br>
     * sensor button count : 0<br>
     * hot spot : (0,0,0)<br>
     * predictor : PREDICT_NONE<br>
     * prediction policy : NO_PREDICTOR<br>
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

        // prediction initialization
        for(int i=0 ; i<MAX_PREDICTION_LENGTH ; i++)  {
            previousReads[i] = new Transform3D();
        }
    }

    //  argument of 0 is last reading (ie, currentIndex), argument 
    //  of 1 means next to last index, etc.
    int previousIndex(int k){
	int temp = currentIndex - k;
        return(temp >= 0 ? temp : MaxSensorReadIndex + temp + 1);
    }

    /**
     * This function sets the type of predictor to use with this sensor.
     * @param predictor predictor type one of PREDICT_NONE or
     * PREDICT_NEXT_FRAME_TIME
     * @exception IllegalArgumentException if an invalid predictor type
     *  is specified.
     */
    public void setPredictor(int predictor){
       if (predictor != PREDICT_NONE && predictor != PREDICT_NEXT_FRAME_TIME) {
           throw new IllegalArgumentException(J3dI18N.getString("Sensor0"));
       } else {
	    predictorType = predictor;
       }
    }

    /**
     * This function returns the type of predictor used by this sensor.
     * @return returns the predictor type. One of PREDICT_NONE or
     * PREDICT_NEXT_FRAME_TIME.
     */ 
    public int getPredictor(){
	return predictorType;
    }

    /**
     * This function sets the prediction policy use by this sensor.
     * @param policy prediction policy one of NO_PREDICTOR, HEAD_PREDICTOR,
     * or HAND_PREDICTOR
     * @exception IllegalArgumentException if an invalid prediction policy
     *  is specified.
     */
    public void setPredictionPolicy(int policy){
	if (policy != NO_PREDICTOR && policy != HEAD_PREDICTOR &&
                 policy != HAND_PREDICTOR)
	    throw new IllegalArgumentException(J3dI18N.getString("Sensor1"));
	else
	    predictionPolicy = policy;
    }

    /**
     * This function returns the prediction policy used by this sensor.
     * @return returns the prediction policy. one of NO_PREDICTOR,
     * HEAD_PREDICTOR, or HAND_PREDICTOR.
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
     * Computes the sensor reading consistent with the prediction policy
     * and copies that value into the specified argument; calling this method
     * with a prediction policy of NO_PREDICTOR will return the last sensor 
     * reading; calling this method with a prediction policy of HAND_PREDICTOR,
     * or HEAD_PREDICTOR will extrapolate previous sensor readings to the
     * current time.
     * @param read The matrix that will receive the predicted sensor reading
     */
    public void getRead(Transform3D read){
        long time;

        if(demand_driven == true)
             device.pollAndProcessInput();

        time =  System.currentTimeMillis();

        // before using prediction, fill in some values
        if(num_reads_so_far < 40*SENSOR_READ_COUNT_BUFFER) {
            num_reads_so_far++;
	    read.set(readings[currentIndex].read);
            return;
        }

	switch(predictionPolicy) {
	case NO_PREDICTOR:
	    read.set(readings[currentIndex].read);
	    break;
        case HAND_PREDICTOR:
	    read.set(readings[currentIndex].read);
            //getPredictedRead(read, time, 3, 2);     
            break;
        case HEAD_PREDICTOR:
	    read.set(readings[currentIndex].read);
            //getPredictedRead(read, time, 3, 2);     
            break;
	}
    }

    /**
     * Computes the sensor reading consistent as of time deltaT in the future
     * and copies that value into the specified argument; the reading is
     * computed using the current prediction policy; a prediction policy of
     * NO_PREDICTOR will yield the most recent sensor reading for any
     * deltaT argument (i.e., this method is the same as getRead for a prediction
     * policy of NO_PREDICTOR).  The time argument must be >= 0.
     * @param read the matrix that will receive the predicted sensor reading
     * @param deltaT the time delta into the future for this read
     */
    public void getRead(Transform3D read, long deltaT){
        long current_time;

        if(deltaT < 0L)  {
             throw new IllegalArgumentException(J3dI18N.getString("Sensor2"));
        }

        if(demand_driven == true)
             device.pollAndProcessInput();

        current_time =  System.currentTimeMillis();

        switch(predictionPolicy) {
        case NO_PREDICTOR:
            read.set(readings[currentIndex].read);
            break;
        case HAND_PREDICTOR:
            read.set(readings[currentIndex].read);
            //getPredictedRead(read, current_time + deltaT, 3, 2);
            break;
        case HEAD_PREDICTOR:
            read.set(readings[currentIndex].read);
            //getPredictedRead(read, current_time + deltaT, 3, 2);
            break;
        }
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

    /**
     * This routine does an nth order fit of the last num_readings, which
     * can be plotted on a graph of time vs. sensor reading.  There is a
     * separate fit done for each of the 16 matrix elements, then an SVD
     * done on the final matrix.  The curve that is produced takes into
     * account non-constant times between each sample (it is fully general).
     * This curve can then be used to produce a prediction for any 
     * time in the future by simply inserting a time value and using the 
     * solution matrix.
     */
     void getPredictedRead(Transform3D transform, long time, int num_readings, 
                              int order) {

       int index = currentIndex;  // lock in current_index for MT-safety
       long time_basis = readings[index].time;
       long tempTime;

       time -= time_basis;

       GMatrix A = new GMatrix(num_readings, order+1);

       for(int i=0 ; i<num_readings ; i++) {
           A.setElement(i, 0, 1.0);
           tempTime = lastTimeRelative(num_readings-i-1, index, time_basis);
           A.setElement(i, 1, (double)tempTime);
           for(int j=2; j<=order ; j++) {
               // powerAndDiv(time, n) = times^n/n
               A.setElement(i, j, powerAndDiv(tempTime, j));
           }
        }

       GMatrix A_Transpose = new GMatrix(A);
       A_Transpose.transpose();
       GMatrix M = new GMatrix(order+1, order+1);
       M.mul(A_Transpose, A);
       try {
          M.invert();
       } catch (SingularMatrixException e) {
          System.out.println("SINGULAR MATRIX EXCEPTION in prediction");
          System.out.println(M);
       }

       // TODO: can be class scope
       double[] transformArray = new double[16];
       GMatrix solMatrix = new GMatrix(order+1, num_readings);
       solMatrix.mul(M,A_Transpose);

       GVector P = new GVector(order+1);

       // fill in the time for which we are trying to predict a sensor value
       GVector predTimeVec = new GVector(order+1);
       predTimeVec.setElement(0, 1);
       predTimeVec.setElement(1, time);
       for(int i=2 ; i<=order ; i++) {
          predTimeVec.setElement(i, powerAndDiv(time, i));
       }

       GVector R = new GVector(num_readings);

       for(int transElement=0 ; transElement<16 ; transElement++) {

             for(int i=0 ; i<num_readings ; i++) {
                R.setElement(i, lastReadRelative(num_readings-i-1, index, 
                                                           transElement));
             }       

             P.mul(solMatrix,R);
             transformArray[transElement] =  P.dot(predTimeVec);
       }

       //Matrix4d temp = new Matrix4d(transformArray);
       //localSVD(temp);
       //transform.set(temp);
       transform.set(transformArray);
       transform.normalize();
     }

    /**
     * Extracts the kth most recent sensor reading and copies that value into
     * the specified argument; where 0 is the most recent sensor reading, 1 is
     * the next most recent sensor reading, etc.  
     * @param read The matrix that will receive the most recent sensor reading
     * @param k  The kth previous sensor reading
     */
    double lastReadRelative(int kth, int base_index, int mat_element){
        // kth should be < sensorReadCount
      return 
      readings[previousIndexRelative(kth, base_index)].read.mat[mat_element];
    }

    /**
     * Returns the time associated with the kth most recent sensor reading;
     * where 0 is the most recent sensor reading, 1 is the next most recent
     * sensor reading, etc.  However, unlike the public method, returns
     * the kth reading relative to the index given, instead of the 
     * current_index and returns the time relative to timeBasis.
     * @return the time associated with the kthmost recent sensor reading.
     */
    long lastTimeRelative(int k, int base_index, long timeBasis){
        // kth should be < sensorReadCount
        long time;
        time = timeBasis - readings[previousIndexRelative(k, base_index)].time;
        return time;
    }

    //  argument of 0 is last reading, argument of 1 means next to last 
    // index, etc.  , but all of these are relative to *base_index*
    int previousIndexRelative(int k, int base_index){
        int temp = base_index - k;
        return(temp >= 0 ? temp : MaxSensorReadIndex + temp + 1);
    }

    // this method returns  (value^order)/order
    double powerAndDiv(double value, int order) {

       if(order == 0)
           return 1;
       else if(order == 1)
           return value;

       double total = 1.0;
       for(int i=0 ; i< order ; i++)
           total *= value;

       total = total / (double)order;
       return total;
    }

}
