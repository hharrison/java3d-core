/*
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 */

package javax.media.j3d;

import javax.vecmath.*;

/**
 * A SensorRead encapsulates all the information associated with a single
 * reading of a sensor, including a timestamp, a transform, and,
 * optionally, button values.
 */

public class SensorRead {

    /**
     * The maximum number of sensor-attached buttons tracked on a per
     * sensor basis.
     */
    public static final int MAXIMUM_SENSOR_BUTTON_COUNT = 12;

    /**
     * This reading's time stamp
     */
    long time;

    /**
     * The six-degree-of-freedom reading
     */
    Transform3D read;

    /**
     * The state of the sensor's buttons
     */
    int[] buttonValues;

    /**
     *  The number of buttons associated with this SensorRead
     */
    int numButtons;

    /**
     * Constructs a SensorRead object with default parameters.
     * The default values are as follows:
     * <ul>
     * number of buttons : 0<br>
     * button values : 0 (for all array elements)<br>
     * transform : identity<br>
     * time : current time<br>
     * </ul>
     */
    public SensorRead(){
	this(0);
    }

    /**
     * Constructs a SensorRead object with the specified number
     * of buttons.
     * @param numButtons the number of buttons for this SensorRead 
     */
    public SensorRead(int numButtons){
	this.read = new Transform3D();
        this.numButtons = numButtons;
        this.buttonValues = new int[numButtons];

	// Do this last
	this.time = J3dClock.currentTimeMillis();
    }

    final void set(SensorRead sensorRead) {
         this.time = sensorRead.time;
         this.numButtons = sensorRead.numButtons;
         this.read.set(sensorRead.read);
         if(numButtons > 0)
              System.arraycopy(sensorRead.buttonValues, 0, this.buttonValues,
                               0, sensorRead.numButtons);
    }

    /**
     * Set the SensorRead's transform to the value specified
     * @param t1 this sensor's reading
     */
    public void set(Transform3D t1) {
	read.set(t1);
    }

    /**
     * Retrieve the SensorRead's transform and place it in result
     * @param result the recipient of the this sensor's reading
     */
    public void get(Transform3D result) {
	result.set(read);
    }

    /**
     * Sets this SensorRead's time stamp to the specified argument
     * @param time the time to associate with this reading
     */
    public void setTime(long time) {
	this.time = time;
    }

    /**
     * Retrieve this SensorRead's associated time stamp
     * @return the SensorRead's time as a long
     */
    public long getTime() {
	return this.time;
    }

   /**
    *  Sets the values of all buttons for this SensorRead object.  
    *  @param values array contining the new buttons for this SensorRead
    *  @exception ArrayIndexOutOfBoundsException if this object 
    *  has 0 buttons or if values.length is less than the number of 
    *  buttons in this object.
    */
    public void setButtons(int[] values) {
          if(numButtons == 0)

              throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("SensorRead1"));

          else if(values.length < numButtons)

              throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("SensorRead0"));
          System.arraycopy(values, 0, buttonValues, 0, numButtons);
    }

    
   /**
    * Copies the array of button values for this SensorRead object into
    * the specified array.
    * This method has no effect
    * if this SensorRead object has 0 buttons. The array must be
    * large enough to hold all of the buttons.
    * @param values array that will receive the values of all buttons
    * for this SensorRead
    */
    public void getButtons(int[] values) {
	if(numButtons > 0)
	    System.arraycopy(buttonValues, 0, values, 0, numButtons);
    } 


    /**
     * Returns the number of buttons associated with this SensorRead
     * object.
     *
     * @return the number of buttons associated with this SensorRead
     * object
     *
     * @since Java 3D 1.2
     */
    public int getNumButtons() {
	return numButtons;
    }

}
