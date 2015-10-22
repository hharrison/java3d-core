
package javax.media.j3d;

import java.util.ArrayList;

import javax.vecmath.Point3f;

/**
 * A service interface for certain geometric operations that are not available
 * in core Java 3D.
 * <p>
 * In particular, the {@code j3d-core-utils} project provides additional
 * functionality under a different license, which is needed in some
 * circumstances by core Java 3D. Thus, historically, these two projects have
 * been co-dependent. This interface breaks the circular dependency by using
 * Java's service discovery mechanism: if {@code j3d-core-utils} is present on
 * the classpath, its {@code GeometryServiceImpl} will provide the functionality
 * defined here. Or if not (i.e., no suitable {@code GeometryService}
 * implementation can be discovered and instantiated}), then the Java3D core
 * will fail as gracefully as possible.
 * </p>
 * 
 * @author Curtis Rueden
 * @see Font3D#triangulateGlyphs
 */
public interface GeometryService {

	/**
	 * Loops through each island, calling triangulator once per island. Combines
	 * triangle data for all islands together in one object.
	 * 
	 * @param islandCounts TODO
	 * @param outVerts TODO
	 * @param contourCounts TODO
	 * @param triangData TODO
	 * @return total vertex count of the combined array
	 */
	int triangulateIslands(int[][] islandCounts, Point3f[][] outVerts,
		int[] contourCounts, ArrayList<GeometryArray> triangData);

}
