/*
 * $RCSfile$
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

/**
 * The VersionInfo class contains strings that describe the implementation
 * and specification version of Java 3D.  These strings are made available
 * as properties obtained from the VirtualUniverse class.
 *
 * <h4>NOTE TO DEVELOPERS:</h4>
 *
 * <p>
 * Developers are required to do the following whenever they modify
 * Java 3D:
 *
 * <ol>
 * <li>The VENDOR_DEVELOPER string must be modified to
 * indicate the name of the individuals or organizations who have
 * modified the source code.</li>
 *
 * <li>The VERSION_DEV_STRING may be modified to indicate
 * additional information about the particular build, but this is
 * not required.</li>
 *
 * <li>The strings denoted as being unmodifiable must <i>not</i> be
 * modified.</li>
 * </ol>
 *
 * <p>
 * Additionally, developers are required to comply with the terms
 * of the Java 3D API specification, which prohibits releasing an
 * implementation of the Java 3D API without first licensing and
 * passing the TCK tests.
 *
 * @see VirtualUniverse#getProperties
 */
class VersionInfo extends Object {
    /**
     * Developer who has modified Java 3D.
     * This string <i>must</i> be modified to indicate the name of the
     * individual(s) or organization(s) who modified the code.
     */
    private static final String VENDOR_DEVELOPER = null;

    /**
     * String identifying the particular build of Java 3D, for
     * example, beta1, build47, rc1, etc.  This string may only
     * contain letters, numbers, periods, dashes, or underscores. It
     * must not contain any other characters or spaces.
     *
     * This will typically by null for final, released builds, but
     * should be non-null for all other builds.
     */
    private static final String VERSION_BUILD = "build6";

    /**
     * Time and date stamp appended to the end of the version string.
     * This is appended to the version string
     * after the build identifier (and after the first space, which
     * will automatically be added) and before the optional dev
     * string.  This string should be null if no time stamp is desired
     * (it will be null for production builds).
     */
    private static final String VERSION_TIME_STAMP = J3dBuildInfo.getBuildTimeStamp();

    /**
     * An optional string appended to the end of the version string,
     * after the time stamp.  A space will be automatically prepended
     * to this string.  This string should be null if no dev string is
     * desired.
     */
    private static final String VERSION_DEV_STRING = null;

    // -------------------------------------------------------------------
    // -------------------------------------------------------------------
    // END OF DEVELOPER-MODIFIABLE PARAMETERS
    // -------------------------------------------------------------------
    // -------------------------------------------------------------------


    // -------------------------------------------------------------------
    // The following set of constants must not be modified by developers.
    //
    // Only qualified licensees of the Java 3D API specification and
    // TCK tests, who are releasing their own implementation of Java 3D
    // are permitted to change these constants.
    // -------------------------------------------------------------------

    /**
     * Specification version (major and minor version only). This
     * string must not be modified by developers.
     */
    private static final String SPECIFICATION_VERSION = "1.3";

    /**
     * Specification vendor. This should never change and must not
     * be modified by developers.
     */
    private static final String SPECIFICATION_VENDOR = "Sun Microsystems, Inc.";

    /**
     * Primary implementation vendor. This should only be changed by a
     * platform vendor who has licensed the TCK tests and who is
     * releasing their own implementation of Java 3D.
     */
    private static final String VENDOR_PRIMARY = "Sun Microsystems, Inc.";

    /**
     * Base version number.  This is the major.minor.subminor version
     * number.  Version qualifiers are specified separately.  The
     * major and minor version <i>must</i> be the same as the specification
     * version.
     */
    private static final String VERSION_BASE = "1.3.2";

    /**
     * Qualifier indicating that the version of Java 3D is
     * experimental.  This must <i>not</i> be modified by deverlopers.
     * All non-official builds <i>must</i> contain the string
     * <code>"experimental"</code> as part of the release name that
     * appears before the optional first space.
     */
    private static final String VERSION_SUFFIX = "experimental";

    /**
     * The composite version string.  This is composed in the static
     * initializer for this class.
     */
    private static final String VERSION;

    /**
     * The composite vendor string.  This is composed in the static
     * initializer for this class.
     */
    private static final String VENDOR;

    // The static initializer composes the version and vendor strings
    static {
	// Assign the vendor by concatenating primary and developer
	// vendor strings
	String tmpVendor = VENDOR_PRIMARY;
	if (VENDOR_DEVELOPER != null) {
	    tmpVendor += " & " + VENDOR_DEVELOPER;
	}

	String tmpVersion = VERSION_BASE;
	if (VERSION_BUILD != null) {
	    tmpVersion += "-" + VERSION_BUILD;
	}

	if (VERSION_SUFFIX != null) {
	    tmpVersion += "-" + VERSION_SUFFIX;
	}

	if (VERSION_TIME_STAMP != null) {
	    tmpVersion += " " + VERSION_TIME_STAMP;
	}

	if (VERSION_DEV_STRING != null) {
	    tmpVersion += " " + VERSION_DEV_STRING;
	}

	VERSION = tmpVersion;
	VENDOR = tmpVendor;
    }

    /**
     * Returns the specification version string.
     * @return the specification version string
     */
    static String getSpecificationVersion() {
	return SPECIFICATION_VERSION;
    }

    /**
     * Returns the specification vendor string.
     * @return the specification vendor string
     */
    static String getSpecificationVendor() {
	return SPECIFICATION_VENDOR;
    }


    /**
     * Returns the implementation version string.
     * @return the implementation version string
     */
    static String getVersion() {
	return VERSION;
    }

    /**
     * Returns the implementation vendor string.
     * @return the implementation vendor string
     */
    static String getVendor() {
	return VENDOR;
    }
}
