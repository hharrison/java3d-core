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

import java.io.*;
import java.util.*;


class J3dI18N {
    static String getString(String key) {
	String s;
	try {
	    s = (String) ResourceBundle.getBundle("javax.media.j3d.ExceptionStrings").getString(key);
	}
	catch (MissingResourceException e) {
	    System.err.println("J3dI18N: Error looking up: " + key);
	    s = key;
	}
	return s;
    }
}
