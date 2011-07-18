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

package com.sun.j3d.internal;

import javax.media.j3d.J3DBuffer;
import java.nio.Buffer;
import java.nio.ByteOrder;

/**
 * NIO Buffers are new in Java 1.4 but we need to run on 1.3
 * as well, so this class was created to hide the NIO classes
 * from non-1.4 Java 3D users.
 *
 * <p>
 * Typesafe enum for byte orders.
 *
 * <p>
 * NOTE: We no longer need to support JDK 1.3 as of the Java 3D 1.3.2
 * community source release on java.net. We should be able to get rid
 * of this class.
 */

public final class ByteOrderWrapper {

    private final String enum_name;

    /**
     * Private constructor is only called from static initializers
     * in this class.
     */
    private ByteOrderWrapper(String name) {
	enum_name = name;
    }

    /**
     * Static initializer creates object of this type.
     */
    public static final ByteOrderWrapper BIG_ENDIAN =
	new ByteOrderWrapper("BIG_ENDIAN");

    /**
     * Static initializer creates object of this type.
     */
    public static final ByteOrderWrapper LITTLE_ENDIAN =
	new ByteOrderWrapper("LITTLE_ENDIAN");

    public String toString() {
	return enum_name;
    }

    /**
     * Returns the native byte order of the host system.
     */
    public static ByteOrderWrapper nativeOrder() {
	if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
	  return ByteOrderWrapper.BIG_ENDIAN;
	} else return ByteOrderWrapper.LITTLE_ENDIAN;
    }
}
