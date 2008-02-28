/*
 * $RCSfile$
 *
 * Copyright 2004-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

/**
 * The VersionInfo class contains strings that describe the implementation
 * and specification version of the javax.media.j3d pacakge.  These strings
 * are made available as properties obtained from the VirtualUniverse class.
 *
 * <h4>NOTE TO DEVELOPERS:</h4>
 *
 * <p>
 * Developers are strongly encouraged to do the following whenever they
 * modify the 3D graphics API for the Java platform:
 *
 * <ol>
 * <li>The VENDOR_DEVELOPER string should be modified to
 * indicate the name of the individuals or organizations who have
 * modified the source code.</li>
 *
 * <li>The VERSION_DEV_STRING may be modified to indicate
 * additional information about the particular build, but this is
 * not required.</li>
 *
 * <li>The strings denoted as being unmodifiable should <i>not</i> be
 * modified.</li>
 * </ol>
 *
 * <p>
 * The tags of the form @STRING@ are populated by ant when the project is built
 *
 * @see VirtualUniverse#getProperties
 */
class VersionInfo extends Object {
    /**
     * Developer who has modified the 3D graphics API for the Java platform.
     * This string should be modified to indicate the name of the
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
    // The following set of constants should not be modified by developers.
    // -------------------------------------------------------------------

    /**
     * Constant that indicates whether or not this is a debug build.
     */
    static final boolean isDebug = @IS_DEBUG@;

    /**
     * This static final variable is used to enable debugging and
     * assertion checking during the development phase of a particular
     * version of 3D graphics API for the Java platform. It is disabled
     * for "opt" production builds (beta, release candidate, fcs, and
     * patch builds). It is enabled for all "debug" builds and for daily
     * and stable "opt" builds.
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
     * String identifying the type of build, one of:
     * "daily", "stable", "beta", "fcs", or "patch". The default value
     * is "daily".
     */
    private static final String BUILD_TYPE = "@BUILD_TYPE@";

    /**
     * String identifying the build number in the format
     * "buildNN", where "NN" is the sequential build number, for
     * example, build47.  This string contain only letters and
     * numbers, It must not contain any other characters or spaces.
     *
     * For production builds, this string appears parenthetically,
     * after the first space.
     */
    private static final String VERSION_BUILD = "@VERSION_BUILD@";

    /**
     * String identifying the particular build of the 3D API, for
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
    private static final String SPECIFICATION_VERSION = "1.5";

    /**
     * Specification vendor.
     */
    private static final String SPECIFICATION_VENDOR = "@SPEC_VENDOR@";

    /**
     * Primary implementation vendor.
     */
    private static final String VENDOR_PRIMARY = "@IMPL_VENDOR@";

    /**
     * Base version number. This is the major.minor.subminor version
     * number. Version qualifiers are specified separately.  The
     * major and minor version <i>must</i> be the same as the specification
     * version.
     */
    private static final String VERSION_BASE = "@VERSION_BASE@";

    /**
     * Boolean flag indicating that the version of the 3D API is
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
