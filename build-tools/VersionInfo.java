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
 * The tags of the form @STRING@ are populated by ant when the project is built
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
     * Constant that indicates whether or not this is a debug build.
     */
    static final boolean isDebug = @IS_DEBUG@;

    /**
     * This static final variable is used to enable debugging and
     * assertion checking during the development phase of a particular
     * version of Java 3D. It is disabled for "opt" production builds
     * (beta, release candidate, fcs, and patch builds). It is enabled
     * for all "debug" builds and for daily and stable "opt" builds.
     *
     * <p>
     * This parameter is controlled by ant via the build.xml file. The
     * default value is true.
     */
    static final boolean isDevPhase = @IS_DEV_PHASE@;

    /**
     * This static final variable is used indicate a production
     * (beta, release candidate, fcs, or patch) build.
     * <p>
     * This parameter is controlled by ant via the build.xml file. The
     * default value is false.
     */
    static final boolean isProduction = @IS_PRODUCTION@;

    /**
     * If this flag is set to true, the verbose buildtime string
     * will be appended to the version string)
     * <p>
     * This parameter is controlled by ant via the build.xml file. The
     * default value is true.
     */
    private static final boolean useVerboseBuildTime = @USE_VERBOSE_BUILDTIME@;

    /**
     * String identifying the type of  Java 3D build, one of:
     * "daily", "stable", "beta", "fcs", or "patch". The default value
     * is "daily".
     */
    private static final String BUILD_TYPE = "@BUILD_TYPE@";

    /**
     * String identifying the build number of Java 3D in the format
     * "buildNN", where "NN" is the sequential build number, for
     * example, build47.  This string contain only letters and
     * numbers, It must not contain any other characters or spaces.
     *
     * For production builds, this string appears parenthetically,
     * after the first space.
     */
    private static final String VERSION_BUILD = "@VERSION_BUILD@";

    /**
     * String identifying the particular build of Java 3D, for
     * example, "-beta1", "-build47", "-rc1", "_01", etc. Note that
     * this includes the leading dash or underscore. It will typically
     * be empty for FCS builds. This string may only contain letters,
     * numbers, periods, dashes, or underscores. It must not contain
     * any other characters or spaces.
     *
     * This us used as part of the j3d.version that appears before the
     * optional first space.
     */
    private static final String VERSION_SUFFIX = "@VERSION_SUFFIX@";

    /**
     * Date stamp
     *
     * This is only used for daily builds.
     */
    private static final String BUILDTIME = "@BUILDTIME@";

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
    private static final String VERSION_BASE = "@VERSION_BASE@";

    /**
     * Boolean flag indicating that the version of Java 3D is
     * experimental.  This must <i>not</i> be modified by developers.
     * All non-official builds <i>must</i> contain the string
     * <code>"experimental"</code> as part of the release name that
     * appears before the optional first space.
     */
    private static final boolean isExperimental = !isProduction;

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

    /**
     * Build type string, one of "fcs", "fcs-patch", or "", that is
     * appended to the end of the version string after the build
     * identifier (and after the first space, which will automatically
     * be added) and before the optional verbose time and date stamp.
     */
    private static final String BUILD_QUALIFIER = "@BUILD_QUALIFIER@";

    /**
     * Verbose time and date stamp appended to the end of the version string.
     * This is appended to the version string
     * after the build identifier (and after the first space, which
     * will automatically be added) and before the optional dev
     * string.  This string is only used for non-fcs builds.
     */
    private static final String BUILDTIME_VERBOSE = "@BUILDTIME_VERBOSE@";

    private static boolean isNonEmpty(String str) {
	if ((str == null) || (str.length() == 0)) {
	    return false;
	}
	else {
	    return true;
	}
    }

    // The static initializer composes the version and vendor strings
    static {
	final boolean isPatchBuild = BUILD_TYPE.equals("patch");
	final boolean isFcsBuild = BUILD_TYPE.equals("fcs");
	final boolean isBetaBuild = BUILD_TYPE.equals("beta");
	final boolean isStableBuild = BUILD_TYPE.equals("stable");
	final boolean isDailyBuild = BUILD_TYPE.equals("daily");

	// Assign the vendor by concatenating primary and developer
	// vendor strings
	String tmpVendor = VENDOR_PRIMARY;
	if (isNonEmpty(VENDOR_DEVELOPER)) {
	    tmpVendor += " & " + VENDOR_DEVELOPER;
	}

	String tmpVersion = VERSION_BASE;
	if (isNonEmpty(VERSION_SUFFIX)) {
	    if (isPatchBuild) {
		tmpVersion += "_";
	    }
	    else {
		tmpVersion += "-";
	    }
	    tmpVersion += VERSION_SUFFIX;
	}

	if (isDailyBuild && isNonEmpty(BUILDTIME)) {
	    tmpVersion += "-" + BUILDTIME;
	}

	if (isExperimental) {
	    tmpVersion += "-experimental";
	}

	// Append the optional fields that follow the first space

	if (isProduction) {
	    if (isFcsBuild) {
		tmpVersion += " fcs";
	    }
	    else if (isPatchBuild) {
		tmpVersion += " fcs+patch";
	    }

	    if (isNonEmpty(VERSION_BUILD)) {
		tmpVersion += " (" + VERSION_BUILD + ")";
	    }
	}

	if (useVerboseBuildTime && isNonEmpty(BUILDTIME_VERBOSE)) {
	    tmpVersion += " " + BUILDTIME_VERBOSE;
	}

	if (isNonEmpty(VERSION_DEV_STRING)) {
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
