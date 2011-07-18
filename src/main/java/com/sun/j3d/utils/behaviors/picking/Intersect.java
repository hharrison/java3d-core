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

package com.sun.j3d.utils.behaviors.picking;

import javax.media.j3d.*;
import javax.vecmath.*;
import java.lang.Math;
import com.sun.j3d.internal.J3dUtilsI18N;

/*
 * Contains static methods to aid in the intersection test between
 * various PickShape classes and geometry primitives (such as quad,
 * triangle, line and point). 
 */


/**
 * @deprecated As of Java 3D version 1.2, this class is no
 * longer needed
 */

public class Intersect
{
  
  /**
   * Determines if the <code>PickRay</code> and quadrilateral
   * objects intersect.
   * The quadrilateral is defined as <code>coordinates[index]</code> to
   * <code>coordinates[index+3]</code>.
   *
   * @param ray The ray to use in the intersection test.
   * @param coordinates An array holding the quadrilateral data.
   * @param index An array index that designates the starting position
   *  in the array of the quadrilateral to test.
   * @param dist On return dist[0] will be set to the distance between ray's 
   * origin and the point of intersection, if it exists.  
   * The dist array should be allocated by the user.
   * @return <code>true</code> if the ray intersects the quad,
   *  <code>false</code> if the ray does not intersect the object.
   */ 
  public static boolean rayAndQuad( PickRay ray, Point3d coordinates[], 
				    int index, double dist[] ) {
    
    if((coordinates.length - index) < 4) 
      throw new RuntimeException(J3dUtilsI18N.getString("Intersect0"));   

    Point3d pnts[] = new Point3d[4];
    
    for(int i=0; i<4; i++)
      pnts[i] = coordinates[index+i];
   
    return rayAndPoly(pnts, ray, dist);
    
  }

  /**
   * Return true if triangle intersects with ray and the distance, from
   * the origin of ray to the intersection point, is stored in dist[0].
   * The triangle is defined by coordinates[index] to coordinates[index+2]
   * <code>coordinates[index+2]</code>.
   *
   * @param ray The ray to use in the intersection test.
   * @param coordinates An array holding the triangle data.
   * @param index An array index that designates the starting position
   *  in the array of the triangle to test.
   * @param dist On return dist[0] will be set to the distance between ray's origin and the
   *  point of intersection, if it exists.  The dist array should be
   *  allocated by the user.
   * @return <code>true</code> if the ray intersects the triangle,
   *  <code>false</code> if the ray does not intersect the object.
   */
  public static boolean rayAndTriangle( PickRay ray, Point3d coordinates[],
					int index, double dist[] ) {
    
    if((coordinates.length - index) < 3) 
      throw new RuntimeException(J3dUtilsI18N.getString("Intersect1"));   

    Point3d pnts[] = new Point3d[3];
    
    for(int i=0; i<3; i++)
      pnts[i] = coordinates[index+i];
    
    return rayAndPoly(pnts, ray, dist);
    
  }

  /**
   * Return true if triangle intersects with ray and the distance, from
   * the origin of ray to the intersection point, is stored in dist[0].
   * The triangle is defined by coordinates[index] to coordinates[index+2]
   *
   * @param ray The ray that is used in intersection test.
   * @param coordinates an array of vertices.
   * @param index the vertex index
   * @param dist On return dist[0] will be set to the distance between ray's origin and the point intersection, if
   * exist.
   * @return true if ray intersects triangle, else return false.
   */ 
  
  public static boolean rayAndTriangle( PickRay ray, Point3f coordinates[],
					int index, double dist[] ) {
    
    if((coordinates.length - index) < 3) 
      throw new RuntimeException(J3dUtilsI18N.getString("Intersect1"));   

    Point3d pnts[] = new Point3d[3];
    
    for(int i=0; i<3; i++)
      pnts[i] = new Point3d(coordinates[index+i]);
    
    return rayAndPoly(pnts, ray, dist);
    
  }
  
  
  /**
   * Caluates the intersection between a <code>PickSegment</code>
   * object and a quadrilateral.
   * The quad is defined as coordinates[index] to coordinates[index+3]
   *
   * @param segment The segment to use in the intersection test.
   * @param coordinates An array holding the quadrilateral data.
   * @param index An array index that designates the starting position
   *  in the array of the quadrilateral to test.
   * @param dist On return dist[0] will be set to the distance between the start of the segment
   *   and the point of intersection, if it exists.  The dist array
   *   should be allocated by the user.
   * @return <code>true</code> if the segment intersects the quad,
   *  <code>false</code> if the segment does not intersect the object.
   */
  public static boolean segmentAndQuad( PickSegment segment,
                                        Point3d coordinates[],
					int index, double dist[] ) {
    if((coordinates.length - index) < 4) 
      throw new RuntimeException(J3dUtilsI18N.getString("Intersect3"));   

    Point3d pnts[] = new Point3d[4];

    for(int i=0; i<4; i++)
      pnts[i] = coordinates[index+i];
    
    return segmentAndPoly(pnts, segment, dist);
    
  }

  /**
   * Return true if quad intersects with segment and the distance, from
   * the start of segment to the intersection point, is stored in dist[0].
   * The quad is defined by coordinates[index] to coordinates[index+3]
   *
   * @param segment The segment that is used in intersection test.
   * @param coordinates an array of vertices.
   * @param index the vertex index
   * @param dist On return dist[0] will be set to the distance between segment's start and the point 
   * intersection, if exist.
   * @return true if segment intersects quad, else return false.
   */ 
  
  public static boolean segmentAndQuad( PickSegment segment, Point3f coordinates[],
					int index, double dist[] ) {
    if((coordinates.length - index) < 4) 
      throw new RuntimeException(J3dUtilsI18N.getString("Intersect3"));   

    Point3d pnts[] = new Point3d[4];
    
    for(int i=0; i<4; i++)
      pnts[i] = new Point3d(coordinates[index+i]);
    
    return segmentAndPoly(pnts, segment, dist);
    
  }
  
  /**
   * Caluates the intersection between a <code>PickSegment</code>
   * object and a triangle.
   * The triangle is defined as coordinates[index] to coordinates[index+2]
   *
   * @param segment The segment to use in the intersection test.
   * @param coordinates An array holding the triangle data.
   * @param index An array index that designates the starting position
   *  in the array of the triangle to test.
   * @param dist On return dist[0] contains the distance between the start of the segment
   *   and the point of intersection, if it exists.  The dist array
   *   should be allocated by the user.
   * @return <code>true</code> if the segment intersects the triangle,
   *  <code>false</code> if the segment does not intersect the object.
   */
  public static boolean segmentAndTriangle( PickSegment segment, 
                                            Point3d coordinates[],
                                            int index,  
					    double dist[] ) {
    if((coordinates.length - index) < 3) 
      throw new RuntimeException(J3dUtilsI18N.getString("Intersect5"));   
 
    Point3d pnts[] = new Point3d[3];
    
    for(int i=0; i<3; i++)
      pnts[i] = coordinates[index+i];
    
    return segmentAndPoly(pnts, segment, dist);
    
  }

  /**
   * Return true if triangle intersects with segment and the distance, from
   * the start of segment to the intersection point, is stored in dist[0].
   * The triangle is defined by coordinates[index] to coordinates[index+2]
   *
   * @param segment The segment that is used in intersection test.
   * @param coordinates an array of vertices.
   * @param index the vertex index
   * @param dist On return dist[0] will be set to the distance between segment's start and the point 
   * intersection, if exist.
   * @return true if segment intersects triangle, else return false.
   */ 
  
  public static boolean segmentAndTriangle( PickSegment segment, 
					    Point3f coordinates[], int index,  
					    double dist[] ) {
    if((coordinates.length - index) < 3) 
      throw new RuntimeException(J3dUtilsI18N.getString("Intersect6"));   
 
    Point3d pnts[] = new Point3d[3];
    
    for(int i=0; i<3; i++)
      pnts[i] = new Point3d(coordinates[index+i]);
    
    return segmentAndPoly(pnts, segment, dist);
    
  }
 
  /**
   * Caluates the intersection between a <code>PickPoint</code>
   * object and a quadrilateral.
   * The quad is defined as <code>coordinates[index]</code> to
   * <code>coordinates[index+3]</code>.
   *
   * @param point The point to use in the intersection test.
   * @param coordinates An array holding the quadrilateral data.
   * @param index An array index that designates the starting position
   *  in the array of the quadrilateral to test.
   * @return <code>true</code> if the point intersects the quad,
   *  <code>false</code> if the point does not intersect the object.
   */
  private static boolean pointAndQuad( PickPoint point,
                                       Point3d coordinates[],
				       int index) {

    if((coordinates.length - index) < 4) 
      throw new RuntimeException(J3dUtilsI18N.getString("Intersect7"));   
    
    Point3d pnts[] = new Point3d[4];
    
    for(int i=0; i<4; i++)
      pnts[i] = coordinates[index+i];
    
    return pointAndPoly( pnts, point);

  }

  /**
   * Return true if quad intersects with point.
   * The triangle is defined by coordinates[index] to coordinates[index+3]
   *
   * @param point The point that is used in intersection test.
   * @param coordinates an array of vertices.
   * @param index the vertex index
   * @return true if point intersects quad, else return false.
   */ 

  private static boolean pointAndQuad( PickPoint point, Point3f coordinates[],
				       int index) {

    if((coordinates.length - index) < 4) 
      throw new RuntimeException(J3dUtilsI18N.getString("Intersect7"));   
    
    Point3d pnts[] = new Point3d[4];
    
    for(int i=0; i<4; i++)
      pnts[i] = new Point3d(coordinates[index+i]);
    
    return pointAndPoly( pnts, point);
    
  }

  /**
   * Caluates the intersection between a <code>PickPoint</code>
   * object and a triangle.
   * The triangle is defined by <code>coordinates[index]</code> to
   * <code>coordinates[index+2]</code>.
   *
   * @param point The point to use in the intersection test.
   * @param coordinates An array holding the triangle data.
   * @param index An array index that designates the starting position
   *  in the array of the triangle to test.
   * @return <code>true</code> if the point intersects the triangle,
   *  <code>false</code> if the point does not intersect the object.
   */
  private static boolean pointAndTriangle( PickPoint point,
                                           Point3d coordinates[],
					   int index) {

    if((coordinates.length - index) < 3) 
      throw new RuntimeException(J3dUtilsI18N.getString("Intersect9"));   
    
    Point3d pnts[] = new Point3d[3];
    
    for(int i=0; i<3; i++)
      pnts[i] = coordinates[index+i];
    
    return pointAndPoly( pnts, point);
    
  }

  /**
   * Return true if triangle intersects with point.
   * The triangle is defined by coordinates[index] to coordinates[index+2]
   *
   * @param point The point that is used in intersection test.
   * @param coordinates an array of vertices.
   * @param index the vertex index
   * @return true if point intersects triangle, else return false.
   */ 

  private static boolean pointAndTriangle( PickPoint point, Point3f coordinates[],
					   int index) {

    if((coordinates.length - index) < 3) 
      throw new RuntimeException(J3dUtilsI18N.getString("Intersect10"));   
    
    Point3d pnts[] = new Point3d[3];
    
    for(int i=0; i<3; i++)
      pnts[i] = new Point3d(coordinates[index+i]);
    
    return pointAndPoly( pnts, point);
    
  }

  /**
   * Determines if the <code>PickRay</code> and <code>Point3d</code>
   * objects intersect.
   *
   * @param ray The ray that is used in the intersection test.
   * @param pnt The point that is used in intersection test.
   * @param dist On return dist[0] will be set to the distance between ray's origin and the point 
   * of intersection, if it exists. The dist array
   * should be allocated by the user.
   * @return <code>true</code> if the ray intersects the point,
   *  <code>false</code> if the ray does not intersect the object.
   */ 
  public static boolean rayAndPoint( PickRay ray, Point3d pnt,
                                                  double dist[] ) {
    
    Point3d origin = new Point3d();
    Vector3d direction = new Vector3d();
    
    ray.get(origin, direction);
    
    return rayAndPoint(pnt, origin, direction, dist);
  }

  /**
   * Return true if point intersects with ray and the distance, from
   * the origin of ray to the intersection point, is stored in dist[0].
   *
   * @param ray The ray that is used in intersection test.
   * @param pnt The point that is used in intersection test.
   * @param dist On return dist[0] contains the distance between ray's origin and the point 
   * intersection, if exist.
   * @return true if ray intersects point, else return false.
   */ 
  
  public static boolean rayAndPoint( PickRay ray, Point3f pnt, double dist[] ) {
    
    Point3d origin = new Point3d();
    Vector3d direction = new Vector3d();
    
    ray.get(origin, direction);
  
    return rayAndPoint(new Point3d(pnt), origin, direction, dist);
  }
  
  /**
   * Determines if the <code>PickSegment</code> and <code>Point3d</code>
   * objects intersect.  
   *
   * @param segment The segment that is used in the intersection test.
   * @param pnt The point that is used in intersection test.
   * @param dist On return dist[0] contains the distance between segment's origin and the point
   * of intersection, if it exists. The dist array
   * should be allocated by the user.
   * @return <code>true</code> if the segment intersects the point,
   *  <code>false</code> if the segment does not intersect the object.
   */ 
  public static boolean segmentAndPoint( PickSegment segment, Point3d pnt, 
					 double dist[] ) {
    
    Point3d start = new Point3d();
    Point3d end = new Point3d();
    Vector3d direction = new Vector3d();
    
    segment.get(start, end);
    direction.x = end.x - start.x;
    direction.y = end.y - start.y;
    direction.z = end.z - start.z;

    if((rayAndPoint(pnt, start, direction, dist)==true) && (dist[0] <= 1.0))
      return true;
    
    return false;
  }

  /**
   * Return true if point intersects with segment and the distance, from
   * the start of segment to the intersection point, is stored in dist[0].
   *
   * @param segment The segment that is used in intersection test.
   * @param pnt The point that is used in intersection test.
   * @param dist On return dist[0] contains the distance between segment's start and the point 
   * intersection, if exist.
   * @return true if segment intersects point, else return false.
   */ 

  public static boolean segmentAndPoint( PickSegment segment, Point3f pnt, 
					 double dist[] ) {
    
    Point3d start = new Point3d();
    Point3d end = new Point3d();
    Vector3d direction = new Vector3d();
    
    segment.get(start, end);
    direction.x = end.x - start.x;
    direction.y = end.y - start.y;
    direction.z = end.z - start.z;

    if((rayAndPoint(new Point3d(pnt), start, direction, dist)==true) 
       && (dist[0] <= 1.0))
      return true;
    
    return false;
  }

  /**
   * Determines if the <code>PickPoint</code> and <code>Point3d</code>
   * objects intersect.  
   *
   * @param point The PickPoint that is used in the intersection test.
   * @param pnt The Point3d that is used in intersection test.
   * @return <code>true</code> if the PickPoint and Point3d objects
   *  intersect, <code>false</code> if the do not intersect.
   */ 
  public static boolean pointAndPoint( PickPoint point, Point3d pnt) {
    
    Point3d location = new Point3d();
    
    point.get(location);

    if ((location.x == pnt.x) && (location.y == pnt.y) &&
       (location.z == pnt.z))
      return true;
     
    return false;
  }

  /**
   * Return true if pnt intersects with point.
   *
   * @param point The point that is used in intersection test.
   * @param pnt The point that is used in intersection test.
   * @return true if point intersects pnt, else return false.
   */ 
  
  public static boolean pointAndPoint( PickPoint point, Point3f pnt) {
    
    Point3d location = new Point3d();
    
    point.get(location);
    
    if(((float) location.x == pnt.x) && ((float) location.y == pnt.y) 
       && ((float) location.z == pnt.z))
      return true;
    
    return false;
  }
  
  /**
   * Determines if the <code>PickRay</code> and Line
   * objects intersect.  
   * The line is defined as <code>coordinates[index]</code> to
   * <code>coordinates[index+1]</code>.
   *
   * @param ray The ray that is used in the intersection test.
   * @param coordinates An array holding the line data.
   * @param dist On return dist[0] contains the distance between ray's origin and the point of
   * intersection, if it exists. The dist array
   * should be allocated by the user.
   * @return <code>true</code> if the ray intersects the line,
   *  <code>false</code> if the ray does not intersect the object.
   */ 
  public static boolean rayAndLine(PickRay ray, Point3d coordinates[],
                                   int index,  
				   double dist[] ) {
    Point3d origin = new Point3d();
    Vector3d direction = new Vector3d();

    if((coordinates.length - index) < 2) 
      throw new RuntimeException(J3dUtilsI18N.getString("Intersect11"));   
    
    ray.get(origin, direction);
    Point3d start = coordinates[index++];
    Point3d end = coordinates[index];
    
    return lineAndRay( start, end, origin, direction, dist );
    
  }

  /**
   * Return true if line intersects with ray and the distance, from
   * the origin of ray to the intersection point, is stored in dist[0].
   * The line is defined by coordinates[index] to coordinates[index+1]
   *
   * @param ray The ray that is used in intersection test.
   * @param coordinates an array of vertices.
   * @param index the vertex index
   * @param dist On return dist[0] contains the distance between ray's origin and the point intersection, if
   * exist.
   * @return true if ray intersects line, else return false.
   */ 

  public static boolean rayAndLine(PickRay ray, Point3f coordinates[], int index,  
				   double dist[] ) {
    Point3d origin = new Point3d();
    Vector3d direction = new Vector3d();

    if((coordinates.length - index) < 2) 
      throw new RuntimeException(J3dUtilsI18N.getString("Intersect11"));   
    
    ray.get(origin, direction);
    Point3d start = new Point3d(coordinates[index++]);
    Point3d end = new Point3d(coordinates[index]);
    
    return lineAndRay( start, end, origin, direction, dist );
    
  }
  
  /**
   * Determines if the <code>PickSegment</code> and Line
   * objects intersect.
   * The line is defined as <code>coordinates[index]</code> to
   * <code>coordinates[index+1]</code>.
   *
   * @param segment The segment that is used in the intersection test.
   * @param coordinates An array holding the line data.
   * @param dist On return dist[0] contains the distance between segment's origin and the point of
   * intersection, if it exists. The dist array
   * should be allocated by the user.
   * @return <code>true</code> if the segment intersects the line,
   *  <code>false</code> if the segment does not intersect the object.
   */ 
  public static boolean segmentAndLine(PickSegment segment,
                                       Point3d coordinates[],
				       int index, double dist[] ) {

    Point3d start = new Point3d();
    Point3d end = new Point3d();
    Vector3d direction = new Vector3d();
        
    if((coordinates.length - index) < 2) 
      throw new RuntimeException(J3dUtilsI18N.getString("Intersect13"));   
    
    segment.get(start, end);
    direction.x = end.x - start.x;
    direction.y = end.y - start.y;
    direction.z = end.z - start.z;
    
    Point3d startpnt = coordinates[index++];
    Point3d endpnt = coordinates[index];
    
    if(lineAndRay(startpnt, endpnt, start, direction, dist)==true)
      if(dist[0] <= 1.0)
	return true;
    
    return false;
  }

  /**
   * Return true if line intersects with segment and the distance, from
   * the start of segment to the intersection point, is stored in dist[0].
   * The line is defined by coordinates[index] to coordinates[index+1]
   *
   * @param segment The segment that is used in intersection test.
   * @param coordinates an array of vertices.
   * @param index the vertex index
   * @param dist On return dist[0] contains the distance between segment's start and the point 
   * intersection, if exist.
   * @return true if segment intersects line, else return false.
   */ 

  public static boolean segmentAndLine(PickSegment segment, Point3f coordinates[],
				       int index, double dist[] ) {

    Point3d start = new Point3d();
    Point3d end = new Point3d();
    Vector3d direction = new Vector3d();
        
    if((coordinates.length - index) < 2) 
      throw new RuntimeException(J3dUtilsI18N.getString("Intersect13"));   
    
    segment.get(start, end);
    direction.x = end.x - start.x;
    direction.y = end.y - start.y;
    direction.z = end.z - start.z;
    
    Point3d startpnt = new Point3d(coordinates[index++]);
    Point3d endpnt = new Point3d(coordinates[index]);
    
    if(lineAndRay(startpnt, endpnt, start, direction, dist)==true)
      if(dist[0] <= 1.0)
	return true;
    
    return false;
  }
  
  /**
   * Determines if the <code>PickPoint</code> and Line
   * objects intersect.
   * The line is defined as <code>coordinates[index]</code> to
   * <code>coordinates[index+1]</code>.
   *
   * @param point The point that is used in the intersection test.
   * @param coordinates An array holding the line data.
   * @return <code>true</code> if the the point intersects the line,
   *  <code>false</code> if the the point does not intersect the object.
   */ 
  public static boolean pointAndLine(PickPoint point, Point3d coordinates[],
				     int index ) {

    if((coordinates.length - index) < 2) 
      throw new RuntimeException(J3dUtilsI18N.getString("Intersect13"));   
    
    double dist[] = new double[1];
    Point3d start = coordinates[index++];
    Point3d end = coordinates[index];
    Point3d location = new Point3d();
    Vector3d direction = new Vector3d();
    
    point.get(location);
    direction.x = end.x - start.x;
    direction.y = end.y - start.y;
    direction.z = end.z - start.z;

    if ((rayAndPoint(location, start, direction, dist)==true) &&
        (dist[0] <= 1.0))
      return true;
    
    return false;
    
  }

  /**
   * Return true if line intersects with point.
   * The line is defined by coordinates[index] to coordinates[index+1]
   *
   * @param point The point that is used in intersection test.
   * @param coordinates an array of vertices.
   * @param index the vertex index
   * @return true if point intersects line, else return false.
   */ 
  
  public static boolean pointAndLine(PickPoint point, Point3f coordinates[],
				     int index ) {

    if((coordinates.length - index) < 2) 
      throw new RuntimeException(J3dUtilsI18N.getString("Intersect13"));   
    
    double dist[] = new double[1];
    Point3d start = new Point3d(coordinates[index++]);
    Point3d end = new Point3d(coordinates[index]);
    Point3d location = new Point3d();
    Vector3d direction = new Vector3d();
    
    point.get(location);
    direction.x = end.x - start.x;
    direction.y = end.y - start.y;
    direction.z = end.z - start.z;
    
    if((rayAndPoint(location, start, direction, dist)==true) && (dist[0] <= 1.0))
      return true;
    
    return false;
    
  }

  /**
   *  Return true if point is on the inside of halfspace test. The
   *  halfspace is 
   * partition by the plane of triangle or quad.
   * */
     
  private static boolean pointAndPoly( Point3d coordinates[], PickPoint point) {
    
    Vector3d vec0 = new Vector3d(); // Edge vector from point 0 to point 1;
    Vector3d vec1 = new Vector3d(); // Edge vector from point 0 to point 2 or 3;
    Vector3d pNrm = new Vector3d();
    double  absNrmX, absNrmY, absNrmZ, pD = 0.0;
    Vector3d tempV3d = new Vector3d();
    double pNrmDotrDir = 0.0; 
    
    double tempD;

    int i, j;

    // Compute plane normal.
    for(i=0; i<coordinates.length-1;) {
      vec0.x = coordinates[i+1].x - coordinates[i].x;
      vec0.y = coordinates[i+1].y - coordinates[i].y;
      vec0.z = coordinates[i+1].z - coordinates[i++].z;
      if(vec0.length() > 0.0)
	break;
    }
        
    for(j=i; j<coordinates.length-1; j++) {
      vec1.x = coordinates[j+1].x - coordinates[j].x;
      vec1.y = coordinates[j+1].y - coordinates[j].y;
      vec1.z = coordinates[j+1].z - coordinates[j].z;
      if(vec1.length() > 0.0)
	break;
    }
    
    if(j == (coordinates.length-1)) {
      // System.out.println("(1) Degenerated polygon.");
      return false;  // Degenerated polygon.
    }

    /* 
       System.out.println("Ray orgin : " + ray.origin + " dir " + ray.direction);
       System.out.println("Triangle/Quad :");
       for(i=0; i<coordinates.length; i++) 
       System.out.println("P" + i + " " + coordinates[i]);
       */

    pNrm.cross(vec0,vec1);
    
    if(pNrm.length() == 0.0) {
      // System.out.println("(2) Degenerated polygon.");
      return false;  // Degenerated polygon.
    }
    // Compute plane D.
    tempV3d.set((Tuple3d) coordinates[0]);
    pD = pNrm.dot(tempV3d);

    Point3d location = new Point3d();
    point.get(location);
    tempV3d.set((Tuple3d) location);
    
    if((pD - pNrm.dot(tempV3d)) == 0.0 )
      return true;
    
    return false;
    
  }

  private static boolean lineAndRay(Point3d start, Point3d end, 
				    Point3d ori, Vector3d dir, 
				    double dist[] ) {
    
    double m00, m01, m10, m11;
    double mInv00, mInv01, mInv10, mInv11;
    double dmt, t, s, tmp1, tmp2;
    Vector3d lDir;

    lDir = new Vector3d(end.x - start.x, end.y - start.y,
			end.z - start.z);
    
    m00 = lDir.x;
    m01 = -dir.x;
    m10 = lDir.y;
    m11 = -dir.y;

    // Get the determinant.
    dmt = (m00 * m11) - (m10 * m01);

    if(dmt==0.0) // No solution, hence no intersect.
      return false;

    // Find the inverse.
    tmp1 = 1/dmt;

    mInv00 = tmp1 * m11;
    mInv01 = tmp1 * (-m01);
    mInv10 = tmp1 * (-m10);
    mInv11 = tmp1 * m00;

    tmp1 = ori.x - start.x;
    tmp2 = ori.y - start.y;

    t = mInv00 * tmp1 + mInv01 * tmp2;
    s = mInv10 * tmp1 + mInv11 * tmp2;
    
    if(s<0.0) // Before the origin of ray.
      return false;
    if((t<0)||(t>1.0)) // Before or after the end points of line.
      return false;

    tmp1 = ori.z + s * dir.z;
    tmp2 = start.z + t * lDir.z;
  
    if((tmp1 < (tmp2 - Double.MIN_VALUE)) || (tmp1 > (tmp2 + Double.MIN_VALUE)))
      return false;

    dist[0] = s;
    return true;
  }

  private static boolean rayAndPoint( Point3d pnt, Point3d ori,
				     Vector3d dir, double dist[] ) {
    int flag = 0;
    double temp;
    
    if(dir.x != 0.0) {
      flag = 0;
      dist[0] = (pnt.x - ori.x)/dir.x;
    }
    else if(dir.y != 0.0) {
      if(pnt.x != ori.x)
	return false;
      flag = 1;
      dist[0] = (pnt.y - ori.y)/dir.y;
    }
    else if(dir.z != 0.0) {
      if((pnt.x != ori.x)||(pnt.y != ori.y))
	return false;
      flag = 2;
      dist[0] = (pnt.z - ori.z)/dir.z;
           
    }
    else
      return false;

    if(dist[0] < 0.0)
      return false;

    if(flag == 0) {
      temp = ori.y + dist[0] * dir.y;
      if((pnt.y < (temp - Double.MIN_VALUE)) || (pnt.y > (temp + Double.MIN_VALUE)))
	return false;    
    }
    
    if(flag < 2) {
      temp = ori.z + dist[0] * dir.z;
      if((pnt.z < (temp - Double.MIN_VALUE)) || (pnt.z > (temp + Double.MIN_VALUE)))
	return false;
    }

    return true;
    
  }

  private static boolean rayAndPoly( Point3d coordinates[], 
				     PickRay ray, double dist[] ) {
    
    Vector3d vec0 = new Vector3d(); // Edge vector from point 0 to point 1;
    Vector3d vec1 = new Vector3d(); // Edge vector from point 0 to point 2 or 3;
    Vector3d pNrm = new Vector3d();
    double  absNrmX, absNrmY, absNrmZ, pD = 0.0;
    Vector3d tempV3d = new Vector3d();
    double pNrmDotrDir = 0.0; 
    int axis, nc, sh, nsh;
    Point3d origin = new Point3d();
    Vector3d direction = new Vector3d();

    Point3d iPnt = new Point3d(); // Point of intersection.
    
    double uCoor[] = new double[4]; // Only need to support up to quad.
    double vCoor[] = new double[4];
    double tempD;

    int i, j;

    // Compute plane normal.
    for(i=0; i<coordinates.length-1;) {
      vec0.x = coordinates[i+1].x - coordinates[i].x;
      vec0.y = coordinates[i+1].y - coordinates[i].y;
      vec0.z = coordinates[i+1].z - coordinates[i++].z;
      if(vec0.length() > 0.0)
	break;
    }
        
    for(j=i; j<coordinates.length-1; j++) {
      vec1.x = coordinates[j+1].x - coordinates[j].x;
      vec1.y = coordinates[j+1].y - coordinates[j].y;
      vec1.z = coordinates[j+1].z - coordinates[j].z;
      if(vec1.length() > 0.0)
	break;
    }
    
    if(j == (coordinates.length-1)) {
      // System.out.println("(1) Degenerated polygon.");
      return false;  // Degenerated polygon.
    }

    /* 
       System.out.println("Triangle/Quad :");
       for(i=0; i<coordinates.length; i++) 
       System.out.println("P" + i + " " + coordinates[i]);
       */

    pNrm.cross(vec0,vec1);
    
    if(pNrm.length() == 0.0) {
      // System.out.println("(2) Degenerated polygon.");
      return false;  // Degenerated polygon.
    }

    ray.get(origin, direction);
    // System.out.println("Ray orgin : " + origin + " dir " + direction);

    // Compute plane D.
    tempV3d.set((Tuple3d) coordinates[0]);
    pD = pNrm.dot(tempV3d);

    pNrmDotrDir = pNrm.dot(direction);
    
    // Ray is parallel to plane. 
    if(pNrmDotrDir== 0.0) {
      // System.out.println("Ray is parallel to plane.");
      return false;        
    }

    tempV3d.set((Tuple3d) origin);

    dist[0] = (pD - pNrm.dot(tempV3d))/ pNrmDotrDir;

    // Ray intersects the plane behind the ray's origin.
    if(dist[0] < 0.0 ) {
      // System.out.println("Ray intersects the plane behind the ray's origin.");
      return false;
    }

    // Now, one thing for sure the ray intersects the plane.
    // Find the intersection point.
    iPnt.x = origin.x + direction.x * dist[0];
    iPnt.y = origin.y + direction.y * dist[0];
    iPnt.z = origin.z + direction.z * dist[0];
    
    // System.out.println("dist " + dist[0] + " iPnt : " + iPnt);

    // Project 3d points onto 2d plane and apply Jordan curve theorem. 
    // Note : Area of polygon is not preserve in this projection, but
    // it doesn't matter here. 
    
    // Find the axis of projection.
    absNrmX = Math.abs(pNrm.x);
    absNrmY = Math.abs(pNrm.y);
    absNrmZ = Math.abs(pNrm.z);

    if(absNrmX > absNrmY)
      axis = 0;
    else 
      axis = 1;

    if(axis == 0) {
      if(absNrmX < absNrmZ)
	axis = 2;
    }    
    else if(axis == 1) {
      if(absNrmY < absNrmZ)
	axis = 2;
    }    
    
    // System.out.println("Normal " + pNrm + " axis " + axis );
    
    for(i=0; i<coordinates.length; i++) {
      switch (axis) {
      case 0:
	uCoor[i] = coordinates[i].y - iPnt.y;
	vCoor[i] = coordinates[i].z - iPnt.z;
	break;
	
      case 1:
      	uCoor[i] = coordinates[i].x - iPnt.x;
	vCoor[i] = coordinates[i].z - iPnt.z;
	break;
	
      case 2:
	uCoor[i] = coordinates[i].x - iPnt.x;
	vCoor[i] = coordinates[i].y - iPnt.y;
	break;      
      } 
     
      // System.out.println("i " + i + " u " + uCoor[i] + " v " + vCoor[i]); 
    }
    
    // initialize number of crossing, nc.
    nc = 0;
   
    if(vCoor[0] < 0.0)
      sh = -1;
    else 
      sh = 1;

    for(i=0; i<coordinates.length; i++) {
      j= i+1;
      if(j==coordinates.length)
	j=0;
      
      if(vCoor[j] < 0.0)
	nsh = -1;
      else
	nsh = 1;

      if(sh != nsh) {
	if((uCoor[i] > 0.0) && (uCoor[j] > 0.0)) {
	  // This line must cross U+.
	  nc++;
	}
	else if((uCoor[i] > 0.0) || (uCoor[j] > 0.0)) {
	  // This line might cross U+. We need to compute intersection on U azis.
	  tempD = uCoor[i]-vCoor[i]*(uCoor[j]-uCoor[i])/(vCoor[j]-vCoor[i]);
	  if(tempD > 0)
	    // This line cross U+.
	    nc++;	  
	}	
	sh = nsh;
      } // sh != nsh
    }

    // System.out.println("nc " + nc);
   
    if((nc%2) == 1) {
      
	// calculate the distance
	dist[0] *= direction.length();

	// System.out.println("Ray Intersected!");	
	/* 
	   System.out.println("Ray orgin : " + origin + " dir " + direction);
	   System.out.println("Triangle/Quad :");
	   for(i=0; i<coordinates.length; i++) 
	   System.out.println("P" + i + " " + coordinates[i]);
	   System.out.println("dist " + dist[0] + " iPnt : " + iPnt);
	   */
	return true;
    }
    else {
	// System.out.println("Ray Not Intersected!");
	return false;
    }
  }

  /**
   *  Return true if triangle or quad intersects with segment and the distance is 
   *  stored in dist[0].
   * */
     
  private static boolean segmentAndPoly( Point3d coordinates[], 
						  PickSegment segment, 
						  double dist[] ) {
    
    Vector3d vec0 = new Vector3d(); // Edge vector from point 0 to point 1;
    Vector3d vec1 = new Vector3d(); // Edge vector from point 0 to point 2 or 3;
    Vector3d pNrm = new Vector3d();
    double  absNrmX, absNrmY, absNrmZ, pD = 0.0;
    Vector3d tempV3d = new Vector3d();
    Vector3d direction = new Vector3d();
    double pNrmDotrDir = 0.0; 
    int axis, nc, sh, nsh;
    Point3d start = new Point3d(); 
    Point3d end = new Point3d(); 

    Point3d iPnt = new Point3d(); // Point of intersection.
    
    double uCoor[] = new double[4]; // Only need to support up to quad.
    double vCoor[] = new double[4];
    double tempD;

    int i, j;

    // Compute plane normal.
    for(i=0; i<coordinates.length-1;) {
      vec0.x = coordinates[i+1].x - coordinates[i].x;
      vec0.y = coordinates[i+1].y - coordinates[i].y;
      vec0.z = coordinates[i+1].z - coordinates[i++].z;
      if(vec0.length() > 0.0)
	break;
    }
        
    for(j=i; j<coordinates.length-1; j++) {
      vec1.x = coordinates[j+1].x - coordinates[j].x;
      vec1.y = coordinates[j+1].y - coordinates[j].y;
      vec1.z = coordinates[j+1].z - coordinates[j].z;
      if(vec1.length() > 0.0)
	break;
    }
    
    if(j == (coordinates.length-1)) {
      // System.out.println("(1) Degenerated polygon.");
      return false;  // Degenerated polygon.
    }
    
    /* 
       System.out.println("Triangle/Quad :");
       for(i=0; i<coordinates.length; i++) 
       System.out.println("P" + i + " " + coordinates[i]);
       */

    pNrm.cross(vec0,vec1);
    
    if(pNrm.length() == 0.0) {
      // System.out.println("(2) Degenerated polygon.");
      return false;  // Degenerated polygon.
    }
    // Compute plane D.
    tempV3d.set((Tuple3d) coordinates[0]);
    pD = pNrm.dot(tempV3d);

    segment.get(start, end);
    // System.out.println("Segment start : " + start + " end " + end);

    direction.x = end.x - start.x;
    direction.y = end.y - start.y;
    direction.z = end.z - start.z;

    pNrmDotrDir = pNrm.dot(direction);
    
    // Segment is parallel to plane. 
    if(pNrmDotrDir== 0.0) {
      // System.out.println("Segment is parallel to plane.");
      return false;        
    }

    tempV3d.set((Tuple3d) start);

    dist[0] = (pD - pNrm.dot(tempV3d))/ pNrmDotrDir;

    // Segment intersects the plane behind the segment's start.
    // or exceed the segment's length.
    if((dist[0] < 0.0 ) || (dist[0] > 1.0 )) {
      // System.out.println("Segment intersects the plane behind the start or exceed end.");
      return false;
    }

    // Now, one thing for sure the segment intersect the plane.
    // Find the intersection point.
    iPnt.x = start.x + direction.x * dist[0];
    iPnt.y = start.y + direction.y * dist[0];
    iPnt.z = start.z + direction.z * dist[0];
    
    // System.out.println("dist " + dist[0] + " iPnt : " + iPnt);

    // Project 3d points onto 2d plane and apply Jordan curve theorem. 
    // Note : Area of polygon is not preserve in this projection, but
    // it doesn't matter here. 
    
    // Find the axis of projection.
    absNrmX = Math.abs(pNrm.x);
    absNrmY = Math.abs(pNrm.y);
    absNrmZ = Math.abs(pNrm.z);

    if(absNrmX > absNrmY)
      axis = 0;
    else 
      axis = 1;

    if(axis == 0) {
      if(absNrmX < absNrmZ)
	axis = 2;
    }    
    else if(axis == 1) {
      if(absNrmY < absNrmZ)
	axis = 2;
    }    
    
    // System.out.println("Normal " + pNrm + " axis " + axis );
    
    for(i=0; i<coordinates.length; i++) {
      switch (axis) {
      case 0:
	uCoor[i] = coordinates[i].y - iPnt.y;
	vCoor[i] = coordinates[i].z - iPnt.z;
	break;
	
      case 1:
      	uCoor[i] = coordinates[i].x - iPnt.x;
	vCoor[i] = coordinates[i].z - iPnt.z;
	break;
	
      case 2:
	uCoor[i] = coordinates[i].x - iPnt.x;
	vCoor[i] = coordinates[i].y - iPnt.y;
	break;      
      } 
     
      // System.out.println("i " + i + " u " + uCoor[i] + " v " + vCoor[i]); 
    }
    
    // initialize number of crossing, nc.
    nc = 0;
   
    if(vCoor[0] < 0.0)
      sh = -1;
    else 
      sh = 1;

    for(i=0; i<coordinates.length; i++) {
      j= i+1;
      if(j==coordinates.length)
	j=0;
      
      if(vCoor[j] < 0.0)
	nsh = -1;
      else
	nsh = 1;

      if(sh != nsh) {
	if((uCoor[i] > 0.0) && (uCoor[j] > 0.0)) {
	  // This line must cross U+.
	  nc++;
	}
	else if((uCoor[i] > 0.0) || (uCoor[j] > 0.0)) {
	  // This line might cross U+. We need to compute intersection on U azis.
	  tempD = uCoor[i]-vCoor[i]*(uCoor[j]-uCoor[i])/(vCoor[j]-vCoor[i]);
	  if(tempD > 0)
	    // This line cross U+.
	    nc++;	  
	}	
	sh = nsh;
      } // sh != nsh
    }

    // System.out.println("nc " + nc);
   
    if((nc%2) == 1) {
      
	// calculate the distance
	dist[0] *= direction.length();
	
	// System.out.println("Segment Intersected!");	
	/* 
	   System.out.println("Segment orgin : " + start + " dir " + direction);
	   System.out.println("Triangle/Quad :");
	   for(i=0; i<coordinates.length; i++) 
	   System.out.println("P" + i + " " + coordinates[i]);
	   System.out.println("dist " + dist[0] + " iPnt : " + iPnt);
	   */
	return true;
    }
    else {
	// System.out.println("Segment Not Intersected!");
	return false;
    }
  }
    
}
