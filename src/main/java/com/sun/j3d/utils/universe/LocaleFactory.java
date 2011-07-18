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

package com.sun.j3d.utils.universe;

import javax.media.j3d.Locale;
import javax.media.j3d.HiResCoord;
import javax.media.j3d.VirtualUniverse;

/**
 * This interface defines a factory for creating Locale objects in a
 * SimpleUniverse.  Implementations of the createLocale methods in
 * this interface should construct a new Locale object from the
 * specified parameters.  This class is used by the SimpleUniverse
 * class to construct the default Locale used to hold the view and
 * content branch graphs.
 *
 * @see Locale
 * @see ConfiguredUniverse
 * @see SimpleUniverse
 *
 * @since Java 3D 1.3
 */
public interface LocaleFactory {
    /**
     * Creates a new Locale object at the specified high resolution
     * coordinate in the specified universe.
     *
     * @param universe the VirtualUniverse in which to create the Locale
     * @param hiRes the high resolution coordinate that defines the origin
     * of the Locale
     */
    public Locale createLocale(VirtualUniverse universe, HiResCoord hiRes);

    /**
     * Creates a new Locale object at (0, 0, 0) in the specified universe.
     *
     * @param universe the VirtualUniverse in which to create the Locale
     */
    public Locale createLocale(VirtualUniverse universe);
}
