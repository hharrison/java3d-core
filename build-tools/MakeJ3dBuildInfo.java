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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;

/**
 * This application is used to dynamically create the source code for
 * the J3dBuildInfo class.  The J3dBuildInfo class contains a static
 * method that returns a build time stamp string. The time stamp
 * string is used by the VersionInfo class as part of the version
 * number stored in the VirtualUniverse "j3d.version" property.  It is
 * created dynamically so that the version number uniquely identifies
 * the build.
 */
public class MakeJ3dBuildInfo {
    private static final String srcName =
	"javax" + File.separator +
	"media" + File.separator +
	"j3d" + File.separator +
	"J3dBuildInfo.java";

    public static void main(String[] args) throws FileNotFoundException {
	// Parse command line arguments
	String usage = "Usage: java MakeJ3dBuildInfo [-debug] [srcRootDir]";
	boolean debugFlag = false;
	String srcRoot = ".";

	int idx = 0;
	while (idx < args.length) {
	    if (args[idx].startsWith("-")) {
		if (args[idx].equals("-debug")) {
		    debugFlag = true;
		}
		else {
		    System.err.println(usage);
		    System.exit(1);
		}
		++idx;
	    }
	    else {
		break;
	    }
	}

	// Now grab the root of the source tree, if specified
	if (idx < args.length) {
	    if (idx < (args.length - 1)) {
		System.err.println(usage);
		System.exit(1);
	    }

	    srcRoot = args[idx];
	}

	// Create the File object representing the path name to the
	// output java source file
	String outPathName = srcRoot + File.separator + srcName;
	File file = new File(outPathName);

	// Open the output java source file
        PrintStream out = new PrintStream(new FileOutputStream(file));

	// Create and format the time and date string for the current time
	DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,
						       DateFormat.FULL);
        Date buildDate = new Date();
	String dateString = df.format(buildDate);

	// Generate the java source code for J3dBuildInfo
	out.println("package javax.media.j3d;");
	out.println();
	out.println("/**");
	out.println(" * DO NOT MODIFY THIS CLASS.");
	out.println(" *");
	out.println(" * This class is automatically created as part of the build process");
	out.println(" * by <code>MakeJ3dBuildInfo.java</code>.");
	out.println(" */");
	out.println("class J3dBuildInfo {");
	out.println("    /**");
	out.println("     * Constant that indicates whether or not this is");
	out.println("     * a debug build.");
	out.println("     */");
	out.print("    static final boolean isDebug = ");
	if (debugFlag) {
	    out.println("true;");
	}
	else {
	    out.println("false;");
	}
	out.println();
	out.print("    private static final String BUILD_TIME_STAMP = ");
	out.print("\"");
	out.print(dateString);
	out.println("\";");
	out.println();
	out.println("    /**");
	out.println("     * Returns the build time stamp.");
	out.println("     * @return the build time stamp");
	out.println("     */");
	out.println("    static String getBuildTimeStamp() {");
	out.println("        return BUILD_TIME_STAMP;");
	out.println("    }");
	out.println();
	out.println("    /**");
	out.println("     * Do not construct an instance of this class.");
	out.println("     */");
	out.println("    private J3dBuildInfo() {");
	out.println("    }");
	out.println("}");

	out.close();
    }
}
