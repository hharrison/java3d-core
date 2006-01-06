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
import javax.vecmath.*;
import java.lang.Math;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.util.ArrayList;

    /**
     * The FontExtrusion object is used to describe the extrusion path
     * for a Font3D object.  The extrusion path is used in conjunction
     * with a Font2D object.  The extrusion path defines the edge contour
     * of 3D text.  This contour is perpendicular to the face of the text.
     * The extrusion has it's origin at the edge of the glyph with 1.0 being
     * the height of the tallest glyph. Contour must be monotonic in x.
     * <P>
     * The shape of the extrusion path is, by default, a straight line
     * from 0.0 to 0.2 (known as a straight bevel). The shape may be
     * modified via the extrusionShape parameter, a Shape object that
     * describes the 3D contour of a Font3D object.
     * <P>
     * User is responsible for data sanity and must make sure that 
     * extrusionShape does not cause intersection of adjacent glyphs
     * or within single glyph. Else undefined output may be generated.
     *
     * @see java.awt.Font
     * @see Font3D
     */
public class FontExtrusion extends Object {

  // Default FontExtrusion is a straight line of length .2
  float length = 0.2f;
  Shape shape;
  Point2f [] pnts;

    double tessellationTolerance = 0.01;

    /**
     * Constructs a FontExtrusion object with default parameters.  The
     * default parameters are as follows:
     *
     * <ul>
     * extrusion shape : null<br>
     * tessellation tolerance : 0.01<br>
     * </ul>
     *
     * A null extrusion shape specifies that a straight line from 0.0
     * to 0.2 (straight bevel) is used.
     *
     * @see Font3D
     */
    public FontExtrusion() {
      shape = null;
    }

    /**
     * Constructs a FontExtrusion object with the specified shape, using
     * the default tessellation tolerance.  The
     * specified shape is used to construct the edge
     * contour of a Font3D object.  Each shape begins with an implicit
     * point at 0.0. Contour must be monotonic in x.
     *
     * @param extrusionShape the shape object to use to generate the
     * extrusion path.
     * A null shape specifies that a straight line from 0.0 to 0.2
     * (straight bevel) is used.
     *
     * @exception IllegalArgumentException if multiple contours in 
     * extrusionShape, or contour is not monotonic or least x-value
     * of a contour point is not 0.0f
     *
     * @see Font3D
     */
    public FontExtrusion(Shape extrusionShape) {
      setExtrusionShape(extrusionShape);
    }


    /**
     * Constructs a FontExtrusion object with the specified shape, using
     * the specified tessellation tolerance.  The
     * specified shape is used to construct the edge
     * contour of a Font3D object.  Each shape begins with an implicit
     * point at 0.0. Contour must be monotonic in x.
     *
     * @param extrusionShape the shape object to use to generate the
     * extrusion path.
     * A null shape specifies that a straight line from 0.0 to 0.2
     * (straight bevel) is used.
     * @param tessellationTolerance the tessellation tolerance value
     * used in tessellating the extrusion shape.
     * This corresponds to the <code>flatness</code> parameter in
     * the <code>java.awt.Shape.getPathIterator</code> method.
     *
     * @exception IllegalArgumentException if multiple contours in 
     * extrusionShape, or contour is not monotonic or least x-value
     * of a contour point is not 0.0f
     *
     * @see Font3D
     *
     * @since Java 3D 1.2
     */
    public FontExtrusion(Shape extrusionShape,
			 double tessellationTolerance) {

	this.tessellationTolerance = tessellationTolerance;
	setExtrusionShape(extrusionShape);
    }


    /**
     * Sets the FontExtrusion's shape parameter.  This
     * parameter is used to construct the 3D contour of a Font3D object.
     *
     * @param extrusionShape the shape object to use to generate the
     * extrusion path.
     * A null shape specifies that a straight line from 0.0 to 0.2
     * (straight bevel) is used.
     *
     * @exception IllegalArgumentException if multiple contours in 
     * extrusionShape, or contour is not monotonic or least x-value
     * of a contour point is not 0.0f
     *
     * @see Font3D
     * @see java.awt.Shape
     */
    public void setExtrusionShape(Shape extrusionShape) {
	shape = extrusionShape;
	if (shape == null) return;

	PathIterator pIt = shape.getPathIterator(null, tessellationTolerance);
	ArrayList  coords = new ArrayList();
	float tmpCoords[] = new float[6], prevX = 0.0f;	
	int flag, n = 0, inc = -1;

	// Extrusion shape is restricted to be single contour, monotonous
	// increasing, non-self-intersecting curve. Throw exception otherwise
	while (!pIt.isDone()) {
	   Point2f vertex = new Point2f();
	   flag = pIt.currentSegment(tmpCoords);
	   if (flag == PathIterator.SEG_LINETO){
	     vertex.x = tmpCoords[0];
	     vertex.y = tmpCoords[1];
	     if (inc == -1){
	        if (prevX < vertex.x) inc = 0; 
		else if (prevX > vertex.x) inc = 1;
	     }
	     //Flag 'inc' indicates if curve is monotonic increasing or 
	     // monotonic decreasing. It is set to -1 initially and remains
	     // -1 if consecutive x values are same. Once 'inc' is set to 
	     // 1 or 0, exception is thrown is curve changes direction.
	     if (((inc == 0) && (prevX > vertex.x)) ||
		 ((inc == 1) && (prevX < vertex.x)))
		 throw new IllegalArgumentException(J3dI18N.getString("FontExtrusion0"));

	     prevX = vertex.x;
	     n++;
	     coords.add(vertex);
	   }else if (flag == PathIterator.SEG_MOVETO){
	     if (n != 0)
		throw new IllegalArgumentException(J3dI18N.getString("FontExtrusion3"));

	     vertex.x = tmpCoords[0];
	     vertex.y = tmpCoords[1];
	     prevX = vertex.x;
	     n++;
	     coords.add(vertex);
	   }
	   pIt.next();
	}

	//if (inc == 1){
	//Point2f vertex = new Point2f(0.0f, 0.0f);
	//coords.add(vertex);
	//}
	int i, num = coords.size();
	pnts = new Point2f[num];
	//System.out.println("num "+num+" inc "+inc);
	if (inc == 0){
	  for (i=0;i < num;i++){
		pnts[i] = (Point2f)coords.get(i);
		//System.out.println("i "+i+" x "+ pnts[i].x+" y "+pnts[i].y);
	  }
	}
	else {
	  for (i=0;i < num;i++) {
		pnts[i] = (Point2f)coords.get(num - i -1);
		//System.out.println("i "+i+" x "+ pnts[i].x+" y "+pnts[i].y);
	  }
	}

	//Force last y to be zero until Text3D face scaling is implemented
	pnts[num-1].y = 0.0f;
	if (pnts[0].x != 0.0f)
	  throw new IllegalArgumentException(J3dI18N.getString("FontExtrusion1"));

	//Compute straight line distance between first and last points.
	float dx = (pnts[0].x - pnts[num-1].x);
	float dy = (pnts[0].y - pnts[num-1].y);
	length = (float)Math.sqrt(dx*dx + dy*dy);
    }


    /**
     * Gets the FontExtrusion's shape parameter.  This
     * parameter is used to construct the 3D contour of a Font3D object.
     *
     * @return extrusionShape the shape object used to generate the
     *  extrusion path
     *
     * @see Font3D
     * @see java.awt.Shape
     */
    public Shape getExtrusionShape() {
      return shape;
    }


    /**
     * Returns the tessellation tolerance with which this FontExtrusion was
     * created.
     * @return the tessellation tolerance used by this FontExtrusion
     *
     * @since Java 3D 1.2
     */
    public double getTessellationTolerance() {
	return tessellationTolerance;
    }

}
