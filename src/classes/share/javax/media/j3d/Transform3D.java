/*
 * $RCSfile$
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import javax.vecmath.*;
import com.sun.j3d.internal.HashCodeUtil;

/**
 * A generalized transform object represented internally as a 4x4
 * double-precision floating point matrix.  The mathematical
 * representation is
 * row major, as in traditional matrix mathematics.
 * A Transform3D is used to perform translations, rotations, and
 * scaling and shear effects.<P>
 *
 * A transform has an associated type, and
 * all type classification is left to the Transform3D object.
 * A transform will typically have multiple types, unless it is a
 * general, unclassifiable matrix, in which case it won't be assigned
 * a type.  <P>
 *
 * The Transform3D type is internally computed when the transform
 * object is constructed and updated any time it is modified. A
 * matrix will typically have multiple types. For example, the type
 * associated with an identity matrix is the result of ORing all of
 * the types, except for ZERO and NEGATIVE_DETERMINANT, together.
 * There are public methods available to get the ORed type of the
 * transformation, the sign of the determinant, and the least
 * general matrix type. The matrix type flags are defined as
 * follows:<P>
 * <UL>
 * <LI>ZERO - zero matrix. All of the elements in the matrix
 * have the value 0.</LI><P>
 * <LI>IDENTITY - identity matrix. A matrix with ones on its
 * main diagonal and zeros every where else.</LI><P>
 * <LI>SCALE - the matrix is a uniform scale matrix - there are
 * no rotational or translation components.</LI><P>
 * <LI>ORTHOGONAL - the four row vectors that make up an orthogonal
 * matrix form a basis, meaning that they are mutually orthogonal.
 * The scale is unity and there are no translation components.</LI><P>
 * <LI>RIGID - the upper 3 X 3 of the matrix is orthogonal, and
 * there is a translation component-the scale is unity.</LI><P>
 * <LI>CONGRUENT - this is an angle- and length-preserving matrix,
 * meaning that it can translate, rotate, and reflect about an axis,
 * and scale by an amount that is uniform in all directions. These
 * operations preserve the distance between any two points, and the
 * angle between any two intersecting lines.</LI><P>
 * <LI>AFFINE - an affine matrix can translate, rotate, reflect,
 * scale anisotropically, and shear. Lines remain straight, and parallel
 * lines remain parallel, but the angle between intersecting lines can
 * change.</LI><P>
 * </UL>
 * A matrix is also classified by the sign of its determinant:<P>
 * <UL>
 * NEGATIVE_DETERMINANT - this matrix has a negative determinant.
 * An orthogonal matrix with a positive determinant is a rotation
 * matrix. An orthogonal matrix with a negative determinant is a
 * reflection and rotation matrix.<P></UL>
 * The Java 3D model for 4 X 4 transformations is:<P>
 * <UL><pre>
 * [ m00 m01 m02 m03 ]   [ x ]   [ x' ]
 * [ m10 m11 m12 m13 ] . [ y ] = [ y' ]
 * [ m20 m21 m22 m23 ]   [ z ]   [ z' ]
 * [ m30 m31 m32 m33 ]   [ w ]   [ w' ]
 *
 * x' = m00 . x+m01 . y+m02 . z+m03 . w
 * y' = m10 . x+m11 . y+m12 . z+m13 . w
 * z' = m20 . x+m21 . y+m22 . z+m23 . w
 * w' = m30 . x+m31 . y+m32 . z+m33 . w
 * </pre></ul><P>
 * Note: When transforming a Point3f or a Point3d, the input w is set to
 * 1. When transforming a Vector3f or Vector3d, the input w is set to 0.
 */

public class Transform3D {

    double[] mat = new double[16];
    //double[] rot = new double[9];
    //double[] scales = new double[3];
    // allocate the memory only when it is needed. Following three places will allocate the memory,
    // void setScaleTranslation(), void computeScales() and void computeScaleRotation()
    double[] rot = null;
    double[] scales = null;

    // Unknown until lazy classification is done
    private int type = 0;

    // Dirty bit for classification, this is used
    // for classify()
    private static final int AFFINE_BIT     = 0x01;
    private static final int ORTHO_BIT = 0x02;
    private static final int CONGRUENT_BIT  = 0x04;
    private static final int RIGID_BIT      = 0x08;
    private static final int CLASSIFY_BIT   = 0x10;

    // this is used for scales[], rot[]
    private static final int SCALE_BIT      = 0x20;
    private static final int ROTATION_BIT   = 0x40;
    // set when SVD renormalization is necessary
    private static final int SVD_BIT        = 0x80;

    private static final int CLASSIFY_ALL_DIRTY = AFFINE_BIT |
                                                  ORTHO_BIT |
                                                  CONGRUENT_BIT |
                                                  RIGID_BIT |
                                                  CLASSIFY_BIT;
    private static final int ROTSCALESVD_DIRTY = SCALE_BIT |
                                                  ROTATION_BIT |
                                                  SVD_BIT;
    private int dirtyBits;

    boolean autoNormalize = false;	// Don't auto normalize by default
    /*
    // reused temporaries for compute_svd
    private boolean svdAllocd =false;
    private double[] u1 = null;
    private double[] v1 = null;
    private double[] t1 = null; // used by both compute_svd and compute_qr
    private double[] t2 = null; // used by both compute_svd and compute_qr
    private double[] ts = null;
    private double[] svdTmp = null;
    private double[] svdRot = null;
    private double[] single_values = null;
    private double[] e = null;
    private double[] svdScales = null;
    // from svrReorder
    private int[] svdOut = null;
    private double[] svdMag = null;

    // from compute_qr
    private double[]   cosl  = null;
    private double[]   cosr  = null;
    private double[]   sinl  = null;
    private double[]   sinr  = null;
    private double[]   qr_m  = null;
    */

    private static final double EPS = 1.110223024E-16;

    static final double EPSILON = 1.0e-10;
    static final double EPSILON_ABSOLUTE = 1.0e-5;
    static final double EPSILON_RELATIVE = 1.0e-4;
    /**
     * A zero matrix.
     */
    public static final int ZERO = 0x01;

   /**
    * An identity matrix.
    */
    public static final int IDENTITY = 0x02;


   /**
    * A Uniform scale matrix with no translation or other
    * off-diagonal components.
    */
    public static final int SCALE = 0x04;

   /**
    * A translation-only matrix with ones on the diagonal.
    *
    */
    public static final int TRANSLATION = 0x08;

   /**
    * The four row vectors that make up an orthogonal matrix form a basis,
    * meaning that they are mutually orthogonal; an orthogonal matrix with
    * positive determinant is a pure rotation matrix; a negative
    * determinant indicates a rotation and a reflection.
    */
    public static final int ORTHOGONAL = 0x10;

   /**
    * This matrix is a rotation and a translation with unity scale;
    * The upper 3x3 of the matrix is orthogonal, and there is a
    * translation component.
    */
    public static final int RIGID = 0x20;

   /**
    * This is an angle and length preserving matrix, meaning that it
    * can translate, rotate, and reflect
    * about an axis, and scale by an amount that is uniform in all directions.
    * These operations preserve the distance between any two points and the
    * angle between any two intersecting lines.
    */
    public static final int CONGRUENT = 0x40;

   /**
    * An affine matrix can translate, rotate, reflect, scale anisotropically,
    * and shear.  Lines remain straight, and parallel lines remain parallel,
    * but the angle between intersecting lines can change. In order for a
    * transform to be classified as affine, the 4th row must be: [0, 0, 0, 1].
    */
    public static final int AFFINE = 0x80;

   /**
    * This matrix has a negative determinant; an orthogonal matrix with
    * a positive determinant is a rotation matrix; an orthogonal matrix
    * with a negative determinant is a reflection and rotation matrix.
    */
    public static final int NEGATIVE_DETERMINANT = 0x100;

    /**
     * The upper 3x3 column vectors that make up an orthogonal
     * matrix form a basis meaning that they are mutually orthogonal.
     * It can have non-uniform or zero x/y/z scale as long as
     * the dot product of any two column is zero.
     * This one is used by Java3D internal only and should not
     * expose to the user.
     */
    private static final int ORTHO = 0x40000000;

    /**
     * Constructs and initializes a transform from the 4 x 4 matrix.  The
     * type of the constructed transform will be classified automatically.
     * @param m1 the 4 x 4 transformation matrix
     */
    public Transform3D(Matrix4f m1) {
	set(m1);
    }

    /**
     * Constructs and initializes a transform from the 4 x 4 matrix.  The
     * type of the constructed transform will be classified automatically.
     * @param m1 the 4 x 4 transformation matrix
     */
    public Transform3D(Matrix4d m1) {
	set(m1);
    }

    /**
     * Constructs and initializes a transform from the Transform3D object.
     * @param t1  the transformation object to be copied
     */
    public Transform3D(Transform3D t1) {
	set(t1);
    }

    /**
     * Constructs and initializes a transform to the identity matrix.
     */
    public Transform3D() {
        setIdentity();			// this will also classify the matrix
    }

   /**
     * Constructs and initializes a transform from the float array of
     * length 16; the top row of the matrix is initialized to the first
     * four elements of the array, and so on.  The type of the transform
     * object is classified internally.
     * @param matrix  a float array of 16
     */
    public Transform3D(float[] matrix) {
	set(matrix);
    }

   /**
     * Constructs and initializes a transform from the double precision array
     * of length 16; the top row of the matrix is initialized to the first
     * four elements of the array, and so on.  The type of the transform is
     * classified internally.
     * @param matrix  a float array of 16
     */
    public Transform3D(double[] matrix) {
	set(matrix);
    }

   /**
     * Constructs and initializes a transform from the quaternion,
     * translation, and scale values.   The scale is applied only to the
     * rotational components of the matrix (upper 3 x 3) and not to the
     * translational components of the matrix.
     * @param q1  the quaternion value representing the rotational component
     * @param t1  the translational component of the matrix
     * @param s   the scale value applied to the rotational components
     */
    public Transform3D(Quat4d q1, Vector3d t1, double s) {
	set(q1, t1, s);
    }

   /**
     * Constructs and initializes a transform from the quaternion,
     * translation, and scale values.   The scale is applied only to the
     * rotational components of the matrix (upper 3 x 3) and not to the
     * translational components of the matrix.
     * @param q1  the quaternion value representing the rotational component
     * @param t1  the translational component of the matrix
     * @param s   the scale value applied to the rotational components
     */
    public Transform3D(Quat4f q1, Vector3d t1, double s) {
	set(q1, t1, s);
    }

   /**
     * Constructs and initializes a transform from the quaternion,
     * translation, and scale values.   The scale is applied only to the
     * rotational components of the matrix (upper 3 x 3) and not to the
     * translational components of the matrix.
     * @param q1  the quaternion value representing the rotational component
     * @param t1  the translational component of the matrix
     * @param s   the scale value applied to the rotational components
     */
    public Transform3D(Quat4f q1, Vector3f t1, float s) {
	set(q1, t1, s);
    }

   /**
     * Constructs a transform and initializes it to the upper 4 x 4
     * of the GMatrix argument.  If the parameter matrix is
     * smaller than 4 x 4, the remaining elements in the transform matrix are
     * assigned to zero.
     * @param m1 the GMatrix
     */
    public Transform3D(GMatrix m1) {
	set(m1);
    }

   /**
     * Constructs and initializes a transform from the rotation matrix,
     * translation, and scale values.   The scale is applied only to the
     * rotational component of the matrix (upper 3x3) and not to the
     * translational component of the matrix.
     * @param m1  the rotation matrix representing the rotational component
     * @param t1  the translational component of the matrix
     * @param s   the scale value applied to the rotational components
     */
    public Transform3D(Matrix3f m1, Vector3d t1, double s) {
	set(m1, t1, s);
    }

   /**
     * Constructs and initializes a transform from the rotation matrix,
     * translation, and scale values.   The scale is applied only to the
     * rotational components of the matrix (upper 3x3) and not to the
     * translational components of the matrix.
     * @param m1  the rotation matrix representing the rotational component
     * @param t1  the translational component of the matrix
     * @param s   the scale value applied to the rotational components
     */
    public Transform3D(Matrix3d m1, Vector3d t1, double s) {
	set(m1, t1, s);
    }


   /**
     * Constructs and initializes a transform from the rotation matrix,
     * translation, and scale values.   The scale is applied only to the
     * rotational components of the matrix (upper 3x3) and not to the
     * translational components of the matrix.
     * @param m1  the rotation matrix representing the rotational component
     * @param t1  the translational component of the matrix
     * @param s   the scale value applied to the rotational components
     */
    public Transform3D(Matrix3f m1, Vector3f t1, float s) {
	set(m1, t1, s);
    }

   /**
     * Returns the type of this matrix as an or'ed bitmask of
     * of all of the type classifications to which it belongs.
     * @return  or'ed bitmask of all of the type classifications
     * of this transform
     */
    public final int getType() {
	if ((dirtyBits & CLASSIFY_BIT) != 0) {
	    classify();
	}
	// clear ORTHO bit which only use internally
	return (type & ~ORTHO);
    }

    // True if type is ORTHO
    // Since ORTHO didn't take into account the last row.
    final boolean isOrtho() {
	if ((dirtyBits & ORTHO_BIT) != 0) {
	    if ((almostZero(mat[0]*mat[2] + mat[4]*mat[6] +
			    mat[8]*mat[10]) &&
		 almostZero(mat[0]*mat[1] + mat[4]*mat[5] +
			    mat[8]*mat[9]) &&
		 almostZero(mat[1]*mat[2] + mat[5]*mat[6] +
			    mat[9]*mat[10]))) {
		type |= ORTHO;
		dirtyBits &= ~ORTHO_BIT;
		return true;
	    } else {
		type &= ~ORTHO;
		dirtyBits &= ~ORTHO_BIT;
		return false;
	    }
	}
	return ((type & ORTHO) != 0);
    }

    final boolean isCongruent() {
	if ((dirtyBits & CONGRUENT_BIT) != 0) {
	    // This will also classify AFFINE
		classifyRigid();
	}
	return ((type & CONGRUENT) != 0);
    }

    final boolean isAffine() {
	if ((dirtyBits & AFFINE_BIT) != 0) {
	    classifyAffine();
	}
	return ((type & AFFINE) != 0);
    }

    final boolean isRigid() {
	if ((dirtyBits & RIGID_BIT) != 0) {


	    // This will also classify AFFINE & CONGRUENT
	    if ((dirtyBits & CONGRUENT_BIT) != 0) {
		classifyRigid();
	    } else {

		if ((type & CONGRUENT) != 0) {
		    // Matrix is Congruent, need only
		    // to check scale
		    double s;
		    if ((dirtyBits & SCALE_BIT) != 0){
			s = mat[0]*mat[0] + mat[4]*mat[4] +
			    mat[8]*mat[8];
			// Note that
			// scales[0] = sqrt(s);
			// but since sqrt(1) = 1,
			// we don't need to do s = sqrt(s) here.
		    } else {
			if(scales == null)
			    scales = new double[3];
			s = scales[0];
		    }
		    if (almostOne(s)) {
			type |= RIGID;
		    } else {
			type &= ~RIGID;
		    }
		} else {
		    // Not even congruent, so isRigid must be false
		    type &= ~RIGID;
		}
		dirtyBits &= ~RIGID_BIT;
	    }
	}
	return ((type & RIGID) != 0);
    }


   /**
     * Returns the least general type of this matrix; the order of
     * generality from least to most is: ZERO, IDENTITY,
     * SCALE/TRANSLATION, ORTHOGONAL, RIGID, CONGRUENT, AFFINE.
     * If the matrix is ORTHOGONAL, calling the method
     * getDeterminantSign() will yield more information.
     * @return the least general matrix type
     */
    public final int getBestType() {
	getType();   // force classify if necessary

	if ((type & ZERO)                 != 0 ) return ZERO;
	if ((type & IDENTITY)             != 0 ) return IDENTITY;
	if ((type & SCALE)                != 0 ) return SCALE;
	if ((type & TRANSLATION)          != 0 ) return TRANSLATION;
	if ((type & ORTHOGONAL)           != 0 ) return ORTHOGONAL;
	if ((type & RIGID)                != 0 ) return RIGID;
	if ((type & CONGRUENT)            != 0 ) return CONGRUENT;
	if ((type & AFFINE)               != 0 ) return AFFINE;
	if ((type & NEGATIVE_DETERMINANT) != 0 ) return NEGATIVE_DETERMINANT;
	return 0;
    }

    /*
    private void print_type() {
        if ((type & ZERO)                 > 0 ) System.out.print(" ZERO");
	if ((type & IDENTITY)             > 0 ) System.out.print(" IDENTITY");
	if ((type & SCALE)                > 0 ) System.out.print(" SCALE");
	if ((type & TRANSLATION)          > 0 ) System.out.print(" TRANSLATION");
	if ((type & ORTHOGONAL)           > 0 ) System.out.print(" ORTHOGONAL");
	if ((type & RIGID)                > 0 ) System.out.print(" RIGID");
	if ((type & CONGRUENT)            > 0 ) System.out.print(" CONGRUENT");
	if ((type & AFFINE)               > 0 ) System.out.print(" AFFINE");
	if ((type & NEGATIVE_DETERMINANT) > 0 ) System.out.print(" NEGATIVE_DETERMINANT");
	}
    */

    /**
     * Returns the sign of the determinant of this matrix; a return value
     * of true indicates a non-negative determinant; a return value of false
     * indicates a negative determinant. A value of true will be returned if
     * the determinant is NaN. In general, an orthogonal matrix
     * with a positive determinant is a pure rotation matrix; an orthogonal
     * matrix with a negative determinant is a both a rotation and a
     * reflection matrix.
     * @return  determinant sign : true means non-negative, false means negative
     */
    public final boolean getDeterminantSign() {
        double det = determinant();
        if (Double.isNaN(det)) {
            return true;
        }
        return det >= 0;
    }

    /**
     * Sets a flag that enables or disables automatic SVD
     * normalization.  If this flag is enabled, an automatic SVD
     * normalization of the rotational components (upper 3x3) of this
     * matrix is done after every subsequent matrix operation that
     * modifies this matrix.  This is functionally equivalent to
     * calling normalize() after every subsequent call, but may be
     * less computationally expensive.
     * The default value for this parameter is false.
     * @param autoNormalize  the boolean state of auto normalization
     */
    public final void setAutoNormalize(boolean autoNormalize) {
	this.autoNormalize = autoNormalize;

	if (autoNormalize) {
	    normalize();
	}
    }

    /**
     * Returns the state of auto-normalization.
     * @return  boolean state of auto-normalization
     */
    public final boolean getAutoNormalize() {
	return this.autoNormalize;
    }

    private static final boolean almostZero(double a) {
	return ((a < EPSILON_ABSOLUTE) && (a > -EPSILON_ABSOLUTE));
    }

    private static final boolean almostOne(double a) {
	return ((a < 1+EPSILON_ABSOLUTE) && (a > 1-EPSILON_ABSOLUTE));
    }

    private static final boolean almostEqual(double a, double b) {
	double diff = a-b;

	if (diff >= 0) {
	    if (diff < EPSILON) {
		return true;
	    }
	    // a > b
	    if ((b > 0) || (a > -b)) {
		return (diff < EPSILON_RELATIVE*a);
	    } else {
		return (diff < -EPSILON_RELATIVE*b);
	    }

	} else {
	    if (diff > -EPSILON) {
		return true;
	    }
	    // a < b
	    if ((b < 0) || (-a > b)) {
		return (diff > EPSILON_RELATIVE*a);
	    } else {
		return (diff > -EPSILON_RELATIVE*b);
	    }
	}
    }

    // Fix for Issue 167 -- don't classify matrices with Infinity or NaN values
    // as affine
    private final boolean isInfOrNaN() {
        for (int i = 0; i < 16; i++) {
            if (Double.isNaN(mat[i]) || Double.isInfinite(mat[i])) {
                return true;
            }
        }
        return false;
    }
    
    private final void classifyAffine() {
        if (!isInfOrNaN() &&
                almostZero(mat[12]) &&
                almostZero(mat[13]) &&
                almostZero(mat[14]) &&
                almostOne(mat[15])) {
	    type |= AFFINE;
	} else {
	    type &= ~AFFINE;
	}
	dirtyBits &= ~AFFINE_BIT;
    }

    // same amount of work to classify rigid and congruent
    private final void classifyRigid() {

	if ((dirtyBits & AFFINE_BIT) != 0) {
	    // should not touch ORTHO bit
	    type &= ORTHO;
	    classifyAffine();
	} else {
	    // keep the affine bit if there is one
	    // and clear the others (CONGRUENT/RIGID) bit
	    type &= (ORTHO | AFFINE);
	}

	if ((type & AFFINE) != 0) {
	    // checking orthogonal condition
	    if (isOrtho()) {
		if ((dirtyBits & SCALE_BIT) != 0) {
		    double s0 = mat[0]*mat[0] + mat[4]*mat[4] +
			mat[8]*mat[8];
		    double s1 = mat[1]*mat[1] + mat[5]*mat[5] +
			mat[9]*mat[9];
		    if (almostEqual(s0, s1)) {
			double s2 = mat[2]*mat[2] + mat[6]*mat[6] +
			    mat[10]*mat[10];
			if (almostEqual(s2, s0)) {
			    type |= CONGRUENT;
			    // Note that scales[0] = sqrt(s0);
			    if (almostOne(s0)) {
				type |= RIGID;
			    }
			}
		    }
		} else {
		    if(scales == null)
			scales = new double[3];

		    double s = scales[0];
		    if (almostEqual(s, scales[1]) &&
			almostEqual(s, scales[2])) {
			type |= CONGRUENT;
			if (almostOne(s)) {
			    type |= RIGID;
			}
		    }
		}
	    }
	}
	dirtyBits &= (~RIGID_BIT | ~CONGRUENT_BIT);
    }

    /**
     * Classifies a matrix.
     */
    private final void classify() {

	if ((dirtyBits & (RIGID_BIT|AFFINE_BIT|CONGRUENT_BIT)) != 0) {
	    // Test for RIGID, CONGRUENT, AFFINE.
	    classifyRigid();
	}

	// Test for ZERO, IDENTITY, SCALE, TRANSLATION,
	// ORTHOGONAL, NEGATIVE_DETERMINANT
	if ((type & AFFINE) != 0) {
	    if ((type & CONGRUENT) != 0) {
		if ((type & RIGID) != 0) {
		    if (zeroTranslation()) {
			type |= ORTHOGONAL;
			if (rotateZero()) {
			    // mat[0], mat[5], mat[10] can be only be
			    // 1 or -1 when reach here
			    if ((mat[0] > 0) &&
				(mat[5] > 0) &&
				(mat[10] > 0)) {
				type |= IDENTITY|SCALE|TRANSLATION;
			    }
			}
		    } else {
			if (rotateZero()) {
			    type |= TRANSLATION;
			}
		    }
		} else {
		    // uniform scale
		    if (zeroTranslation() && rotateZero()) {
			type |= SCALE;
		    }
		}

	    }
	} else {
	    // last row is not (0, 0, 0, 1)
	    if (almostZero(mat[12]) &&
		almostZero(mat[13]) &&
		almostZero(mat[14]) &&
		almostZero(mat[15]) &&
		zeroTranslation() &&
		rotateZero() &&
		almostZero(mat[0]) &&
		almostZero(mat[5]) &&
		almostZero(mat[10])) {
		type |= ZERO;
	    }
	}

	if (!getDeterminantSign()) {
	    type |= NEGATIVE_DETERMINANT;
	}
	dirtyBits &= ~CLASSIFY_BIT;
    }

    final boolean zeroTranslation() {
	return (almostZero(mat[3]) &&
		almostZero(mat[7]) &&
		almostZero(mat[11]));
    }

    final boolean rotateZero() {
	return (almostZero(mat[1]) && almostZero(mat[2]) &&
		almostZero(mat[4]) && almostZero(mat[6]) &&
		almostZero(mat[8]) && almostZero(mat[9]));
    }

   /**
     * Returns the matrix elements of this transform as a string.
     * @return  the matrix elements of this transform
     */
    public String toString() {
	// also, print classification?
	return
	    mat[0] + ", " + mat[1] + ", " + mat[2] + ", " + mat[3] + "\n" +
	    mat[4] + ", " + mat[5] + ", " + mat[6] + ", " + mat[7] + "\n" +
	    mat[8] + ", " + mat[9] + ", " + mat[10] + ", " + mat[11] + "\n" +
	    mat[12] + ", " + mat[13] + ", " + mat[14] + ", " + mat[15]
	    + "\n";
    }

    /**
     * Sets this transform to the identity matrix.
     */
    public final void setIdentity() {
	mat[0] = 1.0;  mat[1] = 0.0;  mat[2] = 0.0;  mat[3] = 0.0;
	mat[4] = 0.0;  mat[5] = 1.0;  mat[6] = 0.0;  mat[7] = 0.0;
	mat[8] = 0.0;  mat[9] = 0.0;  mat[10] = 1.0; mat[11] = 0.0;
	mat[12] = 0.0; mat[13] = 0.0; mat[14] = 0.0; mat[15] = 1.0;
	type = IDENTITY | SCALE |  ORTHOGONAL | RIGID | CONGRUENT |
	       AFFINE | TRANSLATION | ORTHO;
	dirtyBits = SCALE_BIT | ROTATION_BIT;
	// No need to set SVD_BIT
    }

   /**
     * Sets this transform to all zeros.
     */
    public final void setZero() {
	mat[0] = 0.0;  mat[1] = 0.0;  mat[2] = 0.0;  mat[3] = 0.0;
	mat[4] = 0.0;  mat[5] = 0.0;  mat[6] = 0.0;  mat[7] = 0.0;
	mat[8] = 0.0;  mat[9] = 0.0;  mat[10] = 0.0; mat[11] = 0.0;
	mat[12] = 0.0; mat[13] = 0.0; mat[14] = 0.0; mat[15] = 0.0;

	type = ZERO | ORTHO;
	dirtyBits = SCALE_BIT | ROTATION_BIT;
    }


   /**
     * Adds this transform to transform t1 and places the result into
     * this: this = this + t1.
     * @param t1  the transform to be added to this transform
     */
    public final void add(Transform3D t1) {
	for (int i=0 ; i<16 ; i++) {
	    mat[i] += t1.mat[i];
	}

	dirtyBits = CLASSIFY_ALL_DIRTY | ROTSCALESVD_DIRTY;

	if (autoNormalize) {
	    normalize();
	}

    }

    /**
     * Adds transforms t1 and t2 and places the result into this transform.
     * @param t1  the transform to be added
     * @param t2  the transform to be added
     */
    public final void add(Transform3D t1, Transform3D t2) {
	for (int i=0 ; i<16 ; i++) {
	    mat[i] = t1.mat[i] + t2.mat[i];
	}

	dirtyBits = CLASSIFY_ALL_DIRTY | ROTSCALESVD_DIRTY;

	if (autoNormalize) {
	    normalize();
	}

    }

    /**
     * Subtracts transform t1 from this transform and places the result
     * into this: this = this - t1.
     * @param t1  the transform to be subtracted from this transform
     */
    public final void sub(Transform3D t1) {
	for (int i=0 ; i<16 ; i++) {
	    mat[i] -= t1.mat[i];
	}

	dirtyBits = CLASSIFY_ALL_DIRTY | ROTSCALESVD_DIRTY;

	if (autoNormalize) {
	    normalize();
	}
    }


    /**
     * Subtracts transform t2 from transform t1 and places the result into
     * this: this = t1 - t2.
     * @param t1   the left transform
     * @param t2   the right transform
     */
    public final void sub(Transform3D t1, Transform3D t2) {
	for (int i=0 ; i<16 ; i++) {
	    mat[i] = t1.mat[i] - t2.mat[i];
	}

	dirtyBits = CLASSIFY_ALL_DIRTY | ROTSCALESVD_DIRTY;

	if (autoNormalize) {
	    normalize();
	}

    }


   /**
     * Transposes this matrix in place.
     */
    public final void transpose() {
        double temp;

        temp = mat[4];
        mat[4] = mat[1];
        mat[1] = temp;

        temp = mat[8];
        mat[8] = mat[2];
        mat[2] = temp;

        temp = mat[12];
        mat[12] = mat[3];
        mat[3] = temp;

        temp = mat[9];
        mat[9] = mat[6];
        mat[6] = temp;

        temp = mat[13];
        mat[13] = mat[7];
        mat[7] = temp;

        temp = mat[14];
        mat[14] = mat[11];
        mat[11] = temp;

	dirtyBits = CLASSIFY_ALL_DIRTY | ROTSCALESVD_DIRTY;

	if (autoNormalize) {
	    normalize();
	}
    }

    /**
     * Transposes transform t1 and places the value into this transform.
     * The transform t1 is not modified.
     * @param t1  the transform whose transpose is placed into this transform
     */
    public final void transpose(Transform3D t1) {

       if (this != t1) {
           mat[0] =  t1.mat[0];
           mat[1] =  t1.mat[4];
           mat[2] =  t1.mat[8];
           mat[3] =  t1.mat[12];
           mat[4] =  t1.mat[1];
           mat[5] =  t1.mat[5];
           mat[6] =  t1.mat[9];
           mat[7] =  t1.mat[13];
           mat[8] =  t1.mat[2];
           mat[9] =  t1.mat[6];
           mat[10] = t1.mat[10];
           mat[11] = t1.mat[14];
           mat[12] = t1.mat[3];
           mat[13] = t1.mat[7];
           mat[14] = t1.mat[11];
           mat[15] = t1.mat[15];

	   dirtyBits = CLASSIFY_ALL_DIRTY | ROTSCALESVD_DIRTY;

	   if (autoNormalize) {
	       normalize();
	   }
       } else {
           this.transpose();
       }

    }

   /**
     * Sets the value of this transform to the matrix conversion of the
     * single precision quaternion argument; the non-rotational
     * components are set as if this were an identity matrix.
     * @param q1  the quaternion to be converted
     */
    public final void set(Quat4f q1) {

        mat[0] = (1.0f - 2.0f*q1.y*q1.y - 2.0f*q1.z*q1.z);
        mat[4] = (2.0f*(q1.x*q1.y + q1.w*q1.z));
        mat[8] = (2.0f*(q1.x*q1.z - q1.w*q1.y));

        mat[1] = (2.0f*(q1.x*q1.y - q1.w*q1.z));
        mat[5] = (1.0f - 2.0f*q1.x*q1.x - 2.0f*q1.z*q1.z);
        mat[9] = (2.0f*(q1.y*q1.z + q1.w*q1.x));

        mat[2] = (2.0f*(q1.x*q1.z + q1.w*q1.y));
        mat[6] = (2.0f*(q1.y*q1.z - q1.w*q1.x));
        mat[10] = (1.0f - 2.0f*q1.x*q1.x - 2.0f*q1.y*q1.y);

        mat[3] =  0.0;
        mat[7] =  0.0;
        mat[11] = 0.0;

        mat[12] = 0.0;
        mat[13] = 0.0;
        mat[14] = 0.0;
        mat[15] = 1.0;


	dirtyBits = CLASSIFY_BIT | SCALE_BIT | ROTATION_BIT;
	type = RIGID | CONGRUENT | AFFINE | ORTHO;
    }

   /**
     * Sets the value of this transform to the matrix conversion of the
     * double precision quaternion argument; the non-rotational
     * components are set as if this were an identity matrix.
     * @param q1  the quaternion to be converted
     */
    public final void set(Quat4d q1) {

        mat[0] = (1.0 - 2.0*q1.y*q1.y - 2.0*q1.z*q1.z);
        mat[4] = (2.0*(q1.x*q1.y + q1.w*q1.z));
        mat[8] = (2.0*(q1.x*q1.z - q1.w*q1.y));

        mat[1] = (2.0*(q1.x*q1.y - q1.w*q1.z));
        mat[5] = (1.0 - 2.0*q1.x*q1.x - 2.0*q1.z*q1.z);
        mat[9] = (2.0*(q1.y*q1.z + q1.w*q1.x));

        mat[2] = (2.0*(q1.x*q1.z + q1.w*q1.y));
        mat[6] = (2.0*(q1.y*q1.z - q1.w*q1.x));
        mat[10] = (1.0 - 2.0*q1.x*q1.x - 2.0*q1.y*q1.y);

        mat[3] =  0.0;
        mat[7] =  0.0;
        mat[11] = 0.0;

        mat[12] = 0.0;
        mat[13] = 0.0;
        mat[14] = 0.0;
        mat[15] = 1.0;

	dirtyBits = CLASSIFY_BIT | SCALE_BIT | ROTATION_BIT;
	type = RIGID | CONGRUENT | AFFINE | ORTHO;
    }

   /**
     * Sets the rotational component (upper 3x3) of this transform to the
     * matrix values in the double precision Matrix3d argument; the other
     * elements of this transform are unchanged; any pre-existing scale
     * will be preserved; the argument matrix m1 will be checked for proper
     * normalization when this transform is internally classified.
     * @param m1   the double precision 3x3 matrix
     */
   public final void setRotation(Matrix3d m1) {

       if ((dirtyBits & SCALE_BIT)!= 0) {
	  computeScales(false);
       }

        mat[0] = m1.m00*scales[0];
	mat[1] = m1.m01*scales[1];
	mat[2] = m1.m02*scales[2];
	mat[4] = m1.m10*scales[0];
	mat[5] = m1.m11*scales[1];
	mat[6] = m1.m12*scales[2];
	mat[8] = m1.m20*scales[0];
	mat[9] = m1.m21*scales[1];
	mat[10]= m1.m22*scales[2];

	// only affine bit is preserved
	// SCALE_BIT is clear in the above computeScales() so
	// there is no need to set it dirty again.
	dirtyBits |= (RIGID_BIT | CONGRUENT_BIT| ORTHO_BIT|
		      CLASSIFY_BIT | ROTSCALESVD_DIRTY);

	if (autoNormalize) {
	    // the matrix pass in may not normalize
	    normalize();
	}
   }

   /**
     * Sets the rotational component (upper 3x3) of this transform to the
     * matrix values in the single precision Matrix3f argument; the other
     * elements of this transform are unchanged; any pre-existing scale
     * will be preserved; the argument matrix m1 will be checked for proper
     * normalization when this transform is internally classified.
     * @param m1   the single precision 3x3 matrix
     */
     public final void setRotation(Matrix3f m1) {

	 if ((dirtyBits & SCALE_BIT)!= 0) {
	     computeScales(false);
	 }

	 mat[0] = m1.m00*scales[0];
	 mat[1] = m1.m01*scales[1];
	 mat[2] = m1.m02*scales[2];
	 mat[4] = m1.m10*scales[0];
	 mat[5] = m1.m11*scales[1];
	 mat[6] = m1.m12*scales[2];
	 mat[8] = m1.m20*scales[0];
	 mat[9] = m1.m21*scales[1];
	 mat[10]= m1.m22*scales[2];

	dirtyBits |= (RIGID_BIT | CONGRUENT_BIT| ORTHO_BIT|
		      CLASSIFY_BIT | ROTSCALESVD_DIRTY);

	if (autoNormalize) {
	    normalize();
	}
     }


    /**
     * Sets the rotational component (upper 3x3) of this transform to the
     * matrix equivalent values of the quaternion argument; the other
     * elements of this transform are unchanged; any pre-existing scale
     * in the transform is preserved.
     * @param q1    the quaternion that specifies the rotation
    */
    public final void setRotation(Quat4f q1) {

	if ((dirtyBits & SCALE_BIT)!= 0) {
	    computeScales(false);
	}

        mat[0] = (1.0 - 2.0*q1.y*q1.y - 2.0*q1.z*q1.z)*scales[0];
        mat[4] = (2.0*(q1.x*q1.y + q1.w*q1.z))*scales[0];
        mat[8] = (2.0*(q1.x*q1.z - q1.w*q1.y))*scales[0];

        mat[1] = (2.0*(q1.x*q1.y - q1.w*q1.z))*scales[1];
        mat[5] = (1.0 - 2.0*q1.x*q1.x - 2.0*q1.z*q1.z)*scales[1];
        mat[9] = (2.0*(q1.y * q1.z + q1.w * q1.x))*scales[1];

        mat[2] = (2.0*(q1.x*q1.z + q1.w*q1.y))*scales[2];
        mat[6] = (2.0*(q1.y*q1.z - q1.w*q1.x))*scales[2];
        mat[10] = (1.0 - 2.0*q1.x*q1.x - 2.0*q1.y*q1.y)*scales[2];

	dirtyBits |= CLASSIFY_BIT | ROTATION_BIT;
	dirtyBits &= ~ORTHO_BIT;
	type |= ORTHO;
	type &= ~(ORTHOGONAL|IDENTITY|SCALE|TRANSLATION|SCALE|ZERO);
      }


    /**
     * Sets the rotational component (upper 3x3) of this transform to the
     * matrix equivalent values of the quaternion argument; the other
     * elements of this transform are unchanged; any pre-existing scale
     * in the transform is preserved.
     * @param q1    the quaternion that specifies the rotation
     */
     public final void setRotation(Quat4d q1) {

	 if ((dirtyBits & SCALE_BIT)!= 0) {
	     computeScales(false);
	 }

	 mat[0] = (1.0 - 2.0*q1.y*q1.y - 2.0*q1.z*q1.z)*scales[0];
	 mat[4] = (2.0*(q1.x*q1.y + q1.w*q1.z))*scales[0];
	 mat[8] = (2.0*(q1.x*q1.z - q1.w*q1.y))*scales[0];

	 mat[1] = (2.0*(q1.x*q1.y - q1.w*q1.z))*scales[1];
	 mat[5] = (1.0 - 2.0*q1.x*q1.x - 2.0*q1.z*q1.z)*scales[1];
	 mat[9] = (2.0*(q1.y * q1.z + q1.w * q1.x))*scales[1];

	 mat[2] = (2.0*(q1.x*q1.z + q1.w*q1.y))*scales[2];
	 mat[6] = (2.0*(q1.y*q1.z - q1.w*q1.x))*scales[2];
	 mat[10] = (1.0 - 2.0*q1.x*q1.x - 2.0*q1.y*q1.y)*scales[2];

	 dirtyBits |= CLASSIFY_BIT | ROTATION_BIT;
	 dirtyBits &= ~ORTHO_BIT;
	 type |= ORTHO;
	 type &= ~(ORTHOGONAL|IDENTITY|SCALE|TRANSLATION|SCALE|ZERO);
      }


    /**
     * Sets the value of this transform to the matrix conversion
     * of the single precision axis-angle argument; all of the matrix
     * values are modified.
     * @param a1 the axis-angle to be converted (x, y, z, angle)
     */
    public final void set(AxisAngle4f a1) {

	double mag = Math.sqrt( a1.x*a1.x + a1.y*a1.y + a1.z*a1.z);

	if (almostZero(mag)) {
	    setIdentity();
	} else {
	    mag = 1.0/mag;
	    double ax = a1.x*mag;
	    double ay = a1.y*mag;
	    double az = a1.z*mag;

	    double sinTheta = Math.sin((double)a1.angle);
	    double cosTheta = Math.cos((double)a1.angle);
	    double t = 1.0 - cosTheta;

	    double xz = ax * az;
	    double xy = ax * ay;
	    double yz = ay * az;

	    mat[0] = t * ax * ax + cosTheta;
	    mat[1] = t * xy - sinTheta * az;
	    mat[2] = t * xz + sinTheta * ay;
	    mat[3] = 0.0;

	    mat[4] = t * xy + sinTheta * az;
	    mat[5] = t * ay * ay + cosTheta;
	    mat[6] = t * yz - sinTheta * ax;
	    mat[7] = 0.0;

	    mat[8] = t * xz - sinTheta * ay;
	    mat[9] = t * yz + sinTheta * ax;
	    mat[10] = t * az * az + cosTheta;
	    mat[11] = 0.0;

	    mat[12] = 0.0;
	    mat[13] = 0.0;
	    mat[14] = 0.0;
	    mat[15] = 1.0;

	    type = CONGRUENT | AFFINE | RIGID | ORTHO;
	    dirtyBits = CLASSIFY_BIT | ROTATION_BIT | SCALE_BIT;
	}
    }


    /**
     * Sets the value of this transform to the matrix conversion
     * of the double precision axis-angle argument; all of the matrix
     * values are modified.
     * @param a1 the axis-angle to be converted (x, y, z, angle)
     */
    public final void set(AxisAngle4d a1) {

	double mag = Math.sqrt( a1.x*a1.x + a1.y*a1.y + a1.z*a1.z);

	if (almostZero(mag)) {
	    setIdentity();
	} else {
	    mag = 1.0/mag;
	    double ax = a1.x*mag;
	    double ay = a1.y*mag;
	    double az = a1.z*mag;

	    double sinTheta = Math.sin(a1.angle);
	    double cosTheta = Math.cos(a1.angle);
	    double t = 1.0 - cosTheta;

	    double xz = ax * az;
	    double xy = ax * ay;
	    double yz = ay * az;

	    mat[0] = t * ax * ax + cosTheta;
	    mat[1] = t * xy - sinTheta * az;
	    mat[2] = t * xz + sinTheta * ay;
	    mat[3] = 0.0;

	    mat[4] = t * xy + sinTheta * az;
	    mat[5] = t * ay * ay + cosTheta;
	    mat[6] = t * yz - sinTheta * ax;
	    mat[7] = 0.0;

	    mat[8] = t * xz - sinTheta * ay;
	    mat[9] = t * yz + sinTheta * ax;
	    mat[10] = t * az * az + cosTheta;
	    mat[11] = 0.0;

	    mat[12] = 0.0;
	    mat[13] = 0.0;
	    mat[14] = 0.0;
	    mat[15] = 1.0;

	    type = CONGRUENT | AFFINE | RIGID | ORTHO;
	    dirtyBits = CLASSIFY_BIT | ROTATION_BIT | SCALE_BIT;
	}
    }


   /**
     * Sets the rotational component (upper 3x3) of this transform to the
     * matrix equivalent values of the axis-angle argument; the other
     * elements of this transform are unchanged; any pre-existing scale
     * in the transform is preserved.
     * @param a1 the axis-angle to be converted (x, y, z, angle)
     */
    public final void setRotation(AxisAngle4d a1) {

	if ((dirtyBits & SCALE_BIT)!= 0) {
	    computeScales(false);
	}

	double mag = Math.sqrt( a1.x*a1.x + a1.y*a1.y + a1.z*a1.z);

	if (almostZero(mag)) {
	    mat[0] = scales[0];
	    mat[1] = 0.0;
	    mat[2] = 0.0;
	    mat[4] = 0.0;
	    mat[5] = scales[1];
	    mat[6] = 0.0;
	    mat[8] = 0.0;
	    mat[9] = 0.0;
	    mat[10] = scales[2];
	} else {
	    mag = 1.0/mag;
	    double ax = a1.x*mag;
	    double ay = a1.y*mag;
	    double az = a1.z*mag;

	    double sinTheta = Math.sin(a1.angle);
	    double cosTheta = Math.cos(a1.angle);
	    double t = 1.0 - cosTheta;

	    double xz = ax * az;
	    double xy = ax * ay;
	    double yz = ay * az;

	    mat[0] = (t * ax * ax + cosTheta)*scales[0];
	    mat[1] = (t * xy - sinTheta * az)*scales[1];
	    mat[2] = (t * xz + sinTheta * ay)*scales[2];

	    mat[4] = (t * xy + sinTheta * az)*scales[0];
	    mat[5] = (t * ay * ay + cosTheta)*scales[1];
	    mat[6] = (t * yz - sinTheta * ax)*scales[2];

	    mat[8] = (t * xz - sinTheta * ay)*scales[0];
	    mat[9] = (t * yz + sinTheta * ax)*scales[1];
	    mat[10] = (t * az * az + cosTheta)*scales[2];
	}


	// Rigid remain rigid, congruent remain congruent after
	// set rotation
	dirtyBits |= CLASSIFY_BIT | ROTATION_BIT;
	dirtyBits &= ~ORTHO_BIT;
	type |= ORTHO;
	type &= ~(ORTHOGONAL|IDENTITY|SCALE|TRANSLATION|SCALE|ZERO);
    }


   /**
     * Sets the rotational component (upper 3x3) of this transform to the
     * matrix equivalent values of the axis-angle argument; the other
     * elements of this transform are unchanged; any pre-existing scale
     * in the transform is preserved.
     * @param a1 the axis-angle to be converted (x, y, z, angle)
     */
    public final void setRotation(AxisAngle4f a1)  {

	if ((dirtyBits & SCALE_BIT)!= 0) {
	    computeScales(false);
	}

	double mag = Math.sqrt( a1.x*a1.x + a1.y*a1.y + a1.z*a1.z);

	if (almostZero(mag)) {
	    mat[0] = scales[0];
	    mat[1] = 0.0;
	    mat[2] = 0.0;
	    mat[4] = 0.0;
	    mat[5] = scales[1];
	    mat[6] = 0.0;
	    mat[8] = 0.0;
	    mat[9] = 0.0;
	    mat[10] = scales[2];
	} else {
	    mag = 1.0/mag;
	    double ax = a1.x*mag;
	    double ay = a1.y*mag;
	    double az = a1.z*mag;

	    double sinTheta = Math.sin(a1.angle);
	    double cosTheta = Math.cos(a1.angle);
	    double t = 1.0 - cosTheta;

	    double xz = ax * az;
	    double xy = ax * ay;
	    double yz = ay * az;

	    mat[0] = (t * ax * ax + cosTheta)*scales[0];
	    mat[1] = (t * xy - sinTheta * az)*scales[1];
	    mat[2] = (t * xz + sinTheta * ay)*scales[2];

	    mat[4] = (t * xy + sinTheta * az)*scales[0];
	    mat[5] = (t * ay * ay + cosTheta)*scales[1];
	    mat[6] = (t * yz - sinTheta * ax)*scales[2];

	    mat[8] = (t * xz - sinTheta * ay)*scales[0];
	    mat[9] = (t * yz + sinTheta * ax)*scales[1];
	    mat[10] = (t * az * az + cosTheta)*scales[2];
	}


	// Rigid remain rigid, congruent remain congruent after
	// set rotation
	dirtyBits |= CLASSIFY_BIT | ROTATION_BIT;
	dirtyBits &= (~ORTHO_BIT | ~SVD_BIT);
	type |= ORTHO;
	type &= ~(ORTHOGONAL|IDENTITY|SCALE|TRANSLATION|SCALE|ZERO);
    }


    /**
     * Sets the value of this transform to a counter clockwise rotation
     * about the x axis. All of the non-rotational components are set as
     * if this were an identity matrix.
     * @param angle the angle to rotate about the X axis in radians
     */
    public void rotX(double angle) {
        double sinAngle = Math.sin(angle);
        double cosAngle = Math.cos(angle);

        mat[0] = 1.0;
        mat[1] = 0.0;
        mat[2] = 0.0;
        mat[3] = 0.0;

        mat[4] = 0.0;
        mat[5] = cosAngle;
        mat[6] = -sinAngle;
        mat[7] = 0.0;

        mat[8] = 0.0;
        mat[9] = sinAngle;
        mat[10] = cosAngle;
        mat[11] = 0.0;

        mat[12] = 0.0;
        mat[13] = 0.0;
        mat[14] = 0.0;
        mat[15] = 1.0;

	type = CONGRUENT | AFFINE | RIGID | ORTHO;
	dirtyBits = CLASSIFY_BIT | ROTATION_BIT | SCALE_BIT;
    }

    /**
     * Sets the value of this transform to a counter clockwise rotation about
     * the y axis. All of the non-rotational components are set as if this
     * were an identity matrix.
     * @param angle the angle to rotate about the Y axis in radians
     */
    public void rotY(double angle) {
        double sinAngle = Math.sin(angle);
        double cosAngle = Math.cos(angle);

        mat[0] = cosAngle;
        mat[1] = 0.0;
        mat[2] = sinAngle;
        mat[3] = 0.0;

        mat[4] = 0.0;
        mat[5] = 1.0;
        mat[6] = 0.0;
        mat[7] = 0.0;

        mat[8] = -sinAngle;
        mat[9] = 0.0;
        mat[10] = cosAngle;
        mat[11] = 0.0;

        mat[12] = 0.0;
        mat[13] = 0.0;
        mat[14] = 0.0;
        mat[15] = 1.0;

	type = CONGRUENT | AFFINE | RIGID | ORTHO;
	dirtyBits = CLASSIFY_BIT | ROTATION_BIT | SCALE_BIT;
    }


    /**
     * Sets the value of this transform to a counter clockwise rotation
     * about the z axis.  All of the non-rotational components are set
     * as if this were an identity matrix.
     * @param angle the angle to rotate about the Z axis in radians
     */
    public void rotZ(double angle)  {
        double sinAngle = Math.sin(angle);
        double cosAngle = Math.cos(angle);

        mat[0] = cosAngle;
        mat[1] = -sinAngle;
        mat[2] = 0.0;
        mat[3] = 0.0;

        mat[4] = sinAngle;
        mat[5] = cosAngle;
        mat[6] = 0.0;
        mat[7] = 0.0;

        mat[8] = 0.0;
        mat[9] = 0.0;
        mat[10] = 1.0;
        mat[11] = 0.0;

        mat[12] = 0.0;
        mat[13] = 0.0;
        mat[14] = 0.0;
        mat[15] = 1.0;

	type = CONGRUENT | AFFINE | RIGID | ORTHO;
	dirtyBits = CLASSIFY_BIT | ROTATION_BIT | SCALE_BIT;
    }


   /**
     * Sets the translational value of this matrix to the Vector3f parameter
     * values, and sets the other components of the matrix as if this
     * transform were an identity matrix.
     * @param trans  the translational component
     */
    public final void set(Vector3f trans) {
       mat[0] = 1.0; mat[1] = 0.0; mat[2] = 0.0; mat[3] = trans.x;
       mat[4] = 0.0; mat[5] = 1.0; mat[6] = 0.0; mat[7] = trans.y;
       mat[8] = 0.0; mat[9] = 0.0; mat[10] = 1.0; mat[11] = trans.z;
       mat[12] = 0.0; mat[13] = 0.0; mat[14] = 0.0; mat[15] = 1.0;

       type = CONGRUENT | AFFINE | RIGID | ORTHO;
       dirtyBits = CLASSIFY_BIT | ROTATION_BIT | SCALE_BIT;
    }

    /**
     * Sets the translational value of this matrix to the Vector3d paramter
     * values, and sets the other components of the matrix as if this
     * transform were an identity matrix.
     * @param trans  the translational component
     */
    public final void set(Vector3d trans) {
       mat[0] = 1.0; mat[1] = 0.0; mat[2] = 0.0; mat[3] = trans.x;
       mat[4] = 0.0; mat[5] = 1.0; mat[6] = 0.0; mat[7] = trans.y;
       mat[8] = 0.0; mat[9] = 0.0; mat[10] = 1.0; mat[11] = trans.z;
       mat[12] = 0.0; mat[13] = 0.0; mat[14] = 0.0; mat[15] = 1.0;


       type = CONGRUENT | AFFINE | RIGID | ORTHO;
       dirtyBits = CLASSIFY_BIT | ROTATION_BIT | SCALE_BIT;
    }

    /**
     * Sets the scale component of the current transform; any existing
     * scale is first factored out of the existing transform before
     * the new scale is applied.
     * @param scale  the new scale amount
     */
    public final void setScale(double scale) {
	if ((dirtyBits & ROTATION_BIT)!= 0) {
	    computeScaleRotation(false);
	}

	scales[0] = scales[1] = scales[2] = scale;
	mat[0] = rot[0]*scale;
	mat[1] = rot[1]*scale;
	mat[2] = rot[2]*scale;
	mat[4] = rot[3]*scale;
	mat[5] = rot[4]*scale;
	mat[6] = rot[5]*scale;
	mat[8] = rot[6]*scale;
	mat[9] = rot[7]*scale;
	mat[10] = rot[8]*scale;

	dirtyBits |= (CLASSIFY_BIT | RIGID_BIT | CONGRUENT_BIT | SVD_BIT);
	dirtyBits &= ~SCALE_BIT;
    }


    /**
     * Sets the possibly non-uniform scale component of the current
     * transform; any existing scale is first factored out of the
     * existing transform before the new scale is applied.
     * @param scale  the new x,y,z scale values
     */
     public final void setScale(Vector3d scale) {

	if ((dirtyBits & ROTATION_BIT)!= 0) {
	    computeScaleRotation(false);
	}

	scales[0] = scale.x;
	scales[1] = scale.y;
	scales[2] = scale.z;

	mat[0] = rot[0]*scale.x;
	mat[1] = rot[1]*scale.y;
	mat[2] = rot[2]*scale.z;
	mat[4] = rot[3]*scale.x;
	mat[5] = rot[4]*scale.y;
	mat[6] = rot[5]*scale.z;
	mat[8] = rot[6]*scale.x;
	mat[9] = rot[7]*scale.y;
	mat[10] = rot[8]*scale.z;
	dirtyBits |= (CLASSIFY_BIT | RIGID_BIT | CONGRUENT_BIT | SVD_BIT);
	dirtyBits &= ~SCALE_BIT;
    }


    /**
     * Replaces the current transform with a non-uniform scale transform.
     * All values of the existing transform are replaced.
     * @param xScale the new X scale amount
     * @param yScale the new Y scale amount
     * @param zScale the new Z scale amount
     * @deprecated Use setScale(Vector3d) instead of setNonUniformScale;
     * note that the setScale only modifies the scale component
     */
    public final void setNonUniformScale(double xScale,
					 double yScale,
					 double zScale) {
	if(scales == null)
	    scales = new double[3];

	scales[0] = xScale;
	scales[1] = yScale;
	scales[2] = zScale;
	mat[0] = xScale;
	mat[1] = 0.0;
	mat[2] = 0.0;
	mat[3] = 0.0;
	mat[4] = 0.0;
	mat[5] = yScale;
	mat[6] = 0.0;
	mat[7] = 0.0;
	mat[8] = 0.0;
	mat[9] = 0.0;
	mat[10] = zScale;
	mat[11] = 0.0;
	mat[12] = 0.0;
	mat[13] = 0.0;
	mat[14] = 0.0;
	mat[15] = 1.0;

	type = AFFINE | ORTHO;
	dirtyBits = CLASSIFY_BIT | CONGRUENT_BIT | RIGID_BIT |
	            ROTATION_BIT;
    }

    /**
     * Replaces the translational components of this transform to the values
     * in the Vector3f argument; the other values of this transform are not
     * modified.
     * @param trans  the translational component
     */
    public final void setTranslation(Vector3f trans) {
       mat[3] = trans.x;
       mat[7] = trans.y;
       mat[11] = trans.z;
       // Only preserve CONGRUENT, RIGID, ORTHO
       type &= ~(ORTHOGONAL|IDENTITY|SCALE|TRANSLATION|SCALE|ZERO);
       dirtyBits |= CLASSIFY_BIT;
    }


    /**
     * Replaces the translational components of this transform to the values
     * in the Vector3d argument; the other values of this transform are not
     * modified.
     * @param trans  the translational component
     */
    public final void setTranslation(Vector3d trans) {
       mat[3] = trans.x;
       mat[7] = trans.y;
       mat[11] = trans.z;
       type &= ~(ORTHOGONAL|IDENTITY|SCALE|TRANSLATION|SCALE|ZERO);
       dirtyBits |= CLASSIFY_BIT;
    }


    /**
     * Sets the value of this matrix from the rotation expressed
     * by the quaternion q1, the translation t1, and the scale s.
     * @param q1 the rotation expressed as a quaternion
     * @param t1 the translation
     * @param s the scale value
     */
    public final void set(Quat4d q1, Vector3d t1, double s) {
	if(scales == null)
	    scales = new double[3];

	scales[0] = scales[1] = scales[2] = s;

        mat[0] = (1.0 - 2.0*q1.y*q1.y - 2.0*q1.z*q1.z)*s;
        mat[4] = (2.0*(q1.x*q1.y + q1.w*q1.z))*s;
        mat[8] = (2.0*(q1.x*q1.z - q1.w*q1.y))*s;

        mat[1] = (2.0*(q1.x*q1.y - q1.w*q1.z))*s;
        mat[5] = (1.0 - 2.0*q1.x*q1.x - 2.0*q1.z*q1.z)*s;
        mat[9] = (2.0*(q1.y*q1.z + q1.w*q1.x))*s;

        mat[2] = (2.0*(q1.x*q1.z + q1.w*q1.y))*s;
        mat[6] = (2.0*(q1.y*q1.z - q1.w*q1.x))*s;
        mat[10] = (1.0 - 2.0*q1.x*q1.x - 2.0*q1.y*q1.y)*s;

        mat[3] = t1.x;
        mat[7] = t1.y;
        mat[11] = t1.z;
        mat[12] = 0.0;
        mat[13] = 0.0;
        mat[14] = 0.0;
        mat[15] = 1.0;
	type = CONGRUENT | AFFINE | ORTHO;
	dirtyBits = CLASSIFY_BIT | ROTATION_BIT | RIGID_BIT;
    }

    /**
     * Sets the value of this matrix from the rotation expressed
     * by the quaternion q1, the translation t1, and the scale s.
     * @param q1 the rotation expressed as a quaternion
     * @param t1 the translation
     * @param s the scale value
     */
    public final void set(Quat4f q1, Vector3d t1, double s) {
	if(scales == null)
	    scales = new double[3];

	scales[0] = scales[1] = scales[2] = s;

        mat[0] = (1.0f - 2.0f*q1.y*q1.y - 2.0f*q1.z*q1.z)*s;
        mat[4] = (2.0f*(q1.x*q1.y + q1.w*q1.z))*s;
        mat[8] = (2.0f*(q1.x*q1.z - q1.w*q1.y))*s;

        mat[1] = (2.0f*(q1.x*q1.y - q1.w*q1.z))*s;
        mat[5] = (1.0f - 2.0f*q1.x*q1.x - 2.0f*q1.z*q1.z)*s;
        mat[9] = (2.0f*(q1.y*q1.z + q1.w*q1.x))*s;

        mat[2] = (2.0f*(q1.x*q1.z + q1.w*q1.y))*s;
        mat[6] = (2.0f*(q1.y*q1.z - q1.w*q1.x))*s;
        mat[10] = (1.0f - 2.0f*q1.x*q1.x - 2.0f*q1.y*q1.y)*s;

        mat[3] = t1.x;
        mat[7] = t1.y;
        mat[11] = t1.z;
        mat[12] = 0.0;
        mat[13] = 0.0;
        mat[14] = 0.0;
        mat[15] = 1.0;

	type = CONGRUENT | AFFINE | ORTHO;
	dirtyBits =  CLASSIFY_BIT | ROTATION_BIT| RIGID_BIT;
    }

    /**
     * Sets the value of this matrix from the rotation expressed
     * by the quaternion q1, the translation t1, and the scale s.
     * @param q1 the rotation expressed as a quaternion
     * @param t1 the translation
     * @param s the scale value
     */
    public final void set(Quat4f q1, Vector3f t1, float s) {
	if(scales == null)
	    scales = new double[3];

	scales[0] = scales[1] = scales[2] = s;

        mat[0] = (1.0f - 2.0f*q1.y*q1.y - 2.0f*q1.z*q1.z)*s;
        mat[4] = (2.0f*(q1.x*q1.y + q1.w*q1.z))*s;
        mat[8] = (2.0f*(q1.x*q1.z - q1.w*q1.y))*s;

        mat[1] = (2.0f*(q1.x*q1.y - q1.w*q1.z))*s;
        mat[5] = (1.0f - 2.0f*q1.x*q1.x - 2.0f*q1.z*q1.z)*s;
        mat[9] = (2.0f*(q1.y*q1.z + q1.w*q1.x))*s;

        mat[2] = (2.0f*(q1.x*q1.z + q1.w*q1.y))*s;
        mat[6] = (2.0f*(q1.y*q1.z - q1.w*q1.x))*s;
        mat[10] = (1.0f - 2.0f*q1.x*q1.x - 2.0f*q1.y*q1.y)*s;

        mat[3] = t1.x;
        mat[7] = t1.y;
        mat[11] = t1.z;
        mat[12] = 0.0;
        mat[13] = 0.0;
        mat[14] = 0.0;
        mat[15] = 1.0;

	type = CONGRUENT | AFFINE | ORTHO;
	dirtyBits = CLASSIFY_BIT | ROTATION_BIT | RIGID_BIT;
    }

    /**
     * Sets the value of this matrix from the rotation expressed
     * by the rotation matrix m1, the translation t1, and the scale s.
     * The scale is only applied to the
     * rotational component of the matrix (upper 3x3) and not to the
     * translational component of the matrix.
     * @param m1 the rotation matrix
     * @param t1 the translation
     * @param s the scale value
     */
    public final void set(Matrix3f m1, Vector3f t1, float s) {
	mat[0]=m1.m00*s;
	mat[1]=m1.m01*s;
	mat[2]=m1.m02*s;
	mat[3]=t1.x;
	mat[4]=m1.m10*s;
	mat[5]=m1.m11*s;
	mat[6]=m1.m12*s;
	mat[7]=t1.y;
	mat[8]=m1.m20*s;
	mat[9]=m1.m21*s;
	mat[10]=m1.m22*s;
	mat[11]=t1.z;
	mat[12]=0.0;
	mat[13]=0.0;
	mat[14]=0.0;
	mat[15]=1.0;

	type = AFFINE;
	dirtyBits = ORTHO_BIT | CONGRUENT_BIT | RIGID_BIT |
	            CLASSIFY_BIT | ROTSCALESVD_DIRTY;

	if (autoNormalize) {
	    // input matrix may not normalize
	    normalize();
	}
    }

    /**
     * Sets the value of this matrix from the rotation expressed
     * by the rotation matrix m1, the translation t1, and the scale s.
     * The scale is only applied to the
     * rotational component of the matrix (upper 3x3) and not to the
     * translational component of the matrix.
     * @param m1 the rotation matrix
     * @param t1 the translation
     * @param s the scale value
     */
    public final void set(Matrix3f m1, Vector3d t1, double s) {
	mat[0]=m1.m00*s;
	mat[1]=m1.m01*s;
	mat[2]=m1.m02*s;
	mat[3]=t1.x;
	mat[4]=m1.m10*s;
	mat[5]=m1.m11*s;
	mat[6]=m1.m12*s;
	mat[7]=t1.y;
	mat[8]=m1.m20*s;
	mat[9]=m1.m21*s;
	mat[10]=m1.m22*s;
	mat[11]=t1.z;
	mat[12]=0.0;
	mat[13]=0.0;
	mat[14]=0.0;
	mat[15]=1.0;

	type = AFFINE;
	dirtyBits = ORTHO_BIT | CONGRUENT_BIT | RIGID_BIT |
	            CLASSIFY_BIT | ROTSCALESVD_DIRTY;

	if (autoNormalize)  {
	    normalize();
	}
    }

    /**
     * Sets the value of this matrix from the rotation expressed
     * by the rotation matrix m1, the translation t1, and the scale s.
     * The scale is only applied to the
     * rotational component of the matrix (upper 3x3) and not to the
     * translational component of the matrix.
     * @param m1 the rotation matrix
     * @param t1 the translation
     * @param s the scale value
     */
    public final void set(Matrix3d m1, Vector3d t1, double s) {
	mat[0]=m1.m00*s;
	mat[1]=m1.m01*s;
	mat[2]=m1.m02*s;
	mat[3]=t1.x;
	mat[4]=m1.m10*s;
	mat[5]=m1.m11*s;
	mat[6]=m1.m12*s;
	mat[7]=t1.y;
	mat[8]=m1.m20*s;
	mat[9]=m1.m21*s;
	mat[10]=m1.m22*s;
	mat[11]=t1.z;
	mat[12]=0.0;
	mat[13]=0.0;
	mat[14]=0.0;
	mat[15]=1.0;

	type = AFFINE;
	dirtyBits = ORTHO_BIT | CONGRUENT_BIT | RIGID_BIT |
	            CLASSIFY_BIT | ROTSCALESVD_DIRTY;

	if (autoNormalize)  {
	    normalize();
	}
    }

    /**
     * Sets the matrix values of this transform to the matrix values in the
     * upper 4x4 corner of the GMatrix parameter.  If the parameter matrix is
     * smaller than 4x4, the remaining elements in the transform matrix are
     * assigned to zero.  The transform matrix type is classified
     * internally by the Transform3D class.
     * @param matrix  the general matrix from which the Transform3D matrix is derived
     */
    public final void set(GMatrix matrix) {
	int i,j, k;
	int numRows = matrix.getNumRow();
	int numCol = matrix.getNumCol();

	for(i=0 ; i<4 ; i++) {
	    k = i*4;
	    for(j=0 ; j<4 ; j++) {
		if(i>=numRows || j>=numCol)
		    mat[k+j] = 0.0;
		else
		    mat[k+j] = matrix.getElement(i,j);
	    }
	}

	dirtyBits = CLASSIFY_ALL_DIRTY | ROTSCALESVD_DIRTY;

	if (autoNormalize)  {
	    normalize();
	}

    }

    /**
     * Sets the matrix, type, and state of this transform to the matrix,
     * type, and state of transform t1.
     * @param t1  the transform to be copied
     */
    public final void set(Transform3D t1){
	mat[0] = t1.mat[0];
	mat[1] = t1.mat[1];
	mat[2] = t1.mat[2];
	mat[3] = t1.mat[3];
	mat[4] = t1.mat[4];
	mat[5] = t1.mat[5];
	mat[6] = t1.mat[6];
	mat[7] = t1.mat[7];
	mat[8] = t1.mat[8];
	mat[9] = t1.mat[9];
	mat[10] = t1.mat[10];
	mat[11] = t1.mat[11];
	mat[12] = t1.mat[12];
	mat[13] = t1.mat[13];
	mat[14] = t1.mat[14];
	mat[15] = t1.mat[15];
	type = t1.type;

	// don't copy rot[] and scales[]
	dirtyBits = t1.dirtyBits | ROTATION_BIT | SCALE_BIT;
        autoNormalize = t1.autoNormalize;
    }

    // This version gets a lock before doing the set.  It is used internally
    synchronized void setWithLock(Transform3D t1) {
	this.set(t1);
    }

    // This version gets a lock before doing the get.  It is used internally
    synchronized void getWithLock(Transform3D t1) {
	t1.set(this);
    }

    /**
     * Sets the matrix values of this transform to the matrix values in the
     * double precision array parameter.  The matrix type is classified
     * internally by the Transform3D class.
     * @param matrix  the double precision array of length 16 in row major format
     */
    public final void set(double[] matrix) {
	mat[0] = matrix[0];
	mat[1] = matrix[1];
	mat[2] = matrix[2];
	mat[3] = matrix[3];
	mat[4] = matrix[4];
	mat[5] = matrix[5];
	mat[6] = matrix[6];
	mat[7] = matrix[7];
	mat[8] = matrix[8];
	mat[9] = matrix[9];
	mat[10] = matrix[10];
	mat[11] = matrix[11];
	mat[12] = matrix[12];
	mat[13] = matrix[13];
	mat[14] = matrix[14];
	mat[15] = matrix[15];

	dirtyBits = CLASSIFY_ALL_DIRTY | ROTSCALESVD_DIRTY;

	if (autoNormalize)  {
	    normalize();
	}

    }

   /**
     * Sets the matrix values of this transform to the matrix values in the
     * single precision array parameter.  The matrix type is classified
     * internally by the Transform3D class.
     * @param matrix  the single precision array of length 16 in row major format
     */
    public final void set(float[] matrix) {
	mat[0] = matrix[0];
	mat[1] = matrix[1];
	mat[2] = matrix[2];
	mat[3] = matrix[3];
	mat[4] = matrix[4];
	mat[5] = matrix[5];
	mat[6] = matrix[6];
	mat[7] = matrix[7];
	mat[8] = matrix[8];
	mat[9] = matrix[9];
	mat[10] = matrix[10];
	mat[11] = matrix[11];
	mat[12] = matrix[12];
	mat[13] = matrix[13];
	mat[14] = matrix[14];
	mat[15] = matrix[15];

	dirtyBits = CLASSIFY_ALL_DIRTY | ROTSCALESVD_DIRTY;

	if (autoNormalize)  {
	    normalize();
	}

    }

    /**
     * Sets the matrix values of this transform to the matrix values in the
     * double precision Matrix4d argument.  The transform type is classified
     * internally by the Transform3D class.
     * @param m1   the double precision 4x4 matrix
     */
    public final void set(Matrix4d m1) {
	mat[0] = m1.m00;
	mat[1] = m1.m01;
	mat[2] = m1.m02;
	mat[3] = m1.m03;
	mat[4] = m1.m10;
	mat[5] = m1.m11;
	mat[6] = m1.m12;
	mat[7] = m1.m13;
	mat[8] = m1.m20;
	mat[9] = m1.m21;
	mat[10] = m1.m22;
	mat[11] = m1.m23;
	mat[12] = m1.m30;
	mat[13] = m1.m31;
	mat[14] = m1.m32;
	mat[15] = m1.m33;

	dirtyBits = CLASSIFY_ALL_DIRTY | ROTSCALESVD_DIRTY;

	if (autoNormalize)  {
	    normalize();
	}
    }


    /**
     * Sets the matrix values of this transform to the matrix values in the
     * single precision Matrix4f argument.  The transform type is classified
     * internally by the Transform3D class.
     * @param m1   the single precision 4x4 matrix
     */
    public final void set(Matrix4f m1) {
	mat[0] = m1.m00;
	mat[1] = m1.m01;
	mat[2] = m1.m02;
	mat[3] = m1.m03;
	mat[4] = m1.m10;
	mat[5] = m1.m11;
	mat[6] = m1.m12;
	mat[7] = m1.m13;
	mat[8] = m1.m20;
	mat[9] = m1.m21;
	mat[10] = m1.m22;
	mat[11] = m1.m23;
	mat[12] = m1.m30;
	mat[13] = m1.m31;
	mat[14] = m1.m32;
	mat[15] = m1.m33;

	dirtyBits = CLASSIFY_ALL_DIRTY | ROTSCALESVD_DIRTY;

	if (autoNormalize)  {
	    normalize();
	}
    }


    /**
     * Sets the rotational component (upper 3x3) of this transform to the
     * matrix values in the single precision Matrix3f argument; the other
     * elements of this transform are initialized as if this were an identity
     * matrix (i.e., affine matrix with no translational component).
     * @param m1   the single precision 3x3 matrix
     */
    public final void set(Matrix3f m1) {
	mat[0] = m1.m00;
	mat[1] = m1.m01;
	mat[2] = m1.m02;
	mat[3] = 0.0;
	mat[4] = m1.m10;
	mat[5] = m1.m11;
	mat[6] = m1.m12;
	mat[7] = 0.0;
	mat[8] = m1.m20;
	mat[9] = m1.m21;
	mat[10] = m1.m22;
	mat[11] = 0.0;
	mat[12] = 0.0;
	mat[13] = 0.0;
	mat[14] = 0.0;
	mat[15] = 1.0;

	type = AFFINE;
	dirtyBits = ORTHO_BIT | CONGRUENT_BIT | RIGID_BIT |
	            CLASSIFY_BIT | ROTSCALESVD_DIRTY;

	if (autoNormalize)  {
	    normalize();
	}
    }


    /**
     * Sets the rotational component (upper 3x3) of this transform to the
     * matrix values in the double precision Matrix3d argument; the other
     * elements of this transform are initialized as if this were an identity
     * matrix (ie, affine matrix with no translational component).
     * @param m1   the double precision 3x3 matrix
     */
    public final void set(Matrix3d m1) {
	mat[0] = m1.m00;
	mat[1] = m1.m01;
	mat[2] = m1.m02;
	mat[3] = 0.0;
	mat[4] = m1.m10;
	mat[5] = m1.m11;
	mat[6] = m1.m12;
	mat[7] = 0.0;
	mat[8] = m1.m20;
	mat[9] = m1.m21;
	mat[10] = m1.m22;
	mat[11] = 0.0;
	mat[12] = 0.0;
	mat[13] = 0.0;
	mat[14] = 0.0;
	mat[15] = 1.0;

	type = AFFINE;
	dirtyBits = ORTHO_BIT | CONGRUENT_BIT | RIGID_BIT |
	            CLASSIFY_BIT | ROTSCALESVD_DIRTY;

	if (autoNormalize)  {
	    normalize();
	}

    }


    /**
     * Sets the rotational component (upper 3x3) of this transform to the
     * rotation matrix converted from the Euler angles provided; the other
     * non-rotational elements are set as if this were an identity matrix.
     * The euler parameter is a Vector3d consisting of three rotation angles
     * applied first about the X, then Y then Z axis.
     * These rotations are applied using a static frame of reference. In
     * other words, the orientation of the Y rotation axis is not affected
     * by the X rotation and the orientation of the Z rotation axis is not
     * affected by the X or Y rotation.
     * @param euler  the Vector3d consisting of three rotation angles about X,Y,Z
     *
     */
    public final void setEuler(Vector3d euler) {
	double sina, sinb, sinc;
	double cosa, cosb, cosc;

	sina = Math.sin(euler.x);
	sinb = Math.sin(euler.y);
	sinc = Math.sin(euler.z);
	cosa = Math.cos(euler.x);
	cosb = Math.cos(euler.y);
	cosc = Math.cos(euler.z);

	mat[0] = cosb * cosc;
	mat[1] = -(cosa * sinc) + (sina * sinb * cosc);
	mat[2] = (sina * sinc) + (cosa * sinb *cosc);
	mat[3] = 0.0;

	mat[4] = cosb * sinc;
	mat[5] = (cosa * cosc) + (sina * sinb * sinc);
	mat[6] = -(sina * cosc) + (cosa * sinb *sinc);
	mat[7] = 0.0;

	mat[8] = -sinb;
	mat[9] = sina * cosb;
	mat[10] = cosa * cosb;
	mat[11] = 0.0;

	mat[12] = 0.0;
	mat[13] = 0.0;
	mat[14] = 0.0;
	mat[15] = 1.0;

	type = AFFINE | CONGRUENT | RIGID | ORTHO;
	dirtyBits = CLASSIFY_BIT | SCALE_BIT | ROTATION_BIT;
    }


    /**
     * Places the values of this transform into the double precision array
     * of length 16.  The first four elements of the array will contain
     * the top row of the transform matrix, etc.
     * @param matrix   the double precision array of length 16
     */
    public final void get(double[] matrix) {
	matrix[0] = mat[0];
	matrix[1] = mat[1];
	matrix[2] = mat[2];
	matrix[3] = mat[3];
	matrix[4] = mat[4];
	matrix[5] = mat[5];
	matrix[6] = mat[6];
	matrix[7] = mat[7];
	matrix[8] = mat[8];
	matrix[9] = mat[9];
	matrix[10] = mat[10];
	matrix[11] = mat[11];
	matrix[12] = mat[12];
	matrix[13] = mat[13];
	matrix[14] = mat[14];
	matrix[15] = mat[15];
    }


    /**
     * Places the values of this transform into the single precision array
     * of length 16.  The first four elements of the array will contain
     * the top row of the transform matrix, etc.
     * @param matrix  the single precision array of length 16
     */
    public final void get(float[] matrix) {
	matrix[0] = (float) mat[0];
	matrix[1] = (float) mat[1];
	matrix[2] = (float) mat[2];
	matrix[3] = (float) mat[3];
	matrix[4] = (float) mat[4];
	matrix[5] = (float) mat[5];
	matrix[6] = (float) mat[6];
	matrix[7] = (float) mat[7];
	matrix[8] = (float) mat[8];
	matrix[9] = (float) mat[9];
	matrix[10] = (float) mat[10];
	matrix[11] = (float) mat[11];
	matrix[12] = (float) mat[12];
	matrix[13] = (float) mat[13];
	matrix[14] = (float) mat[14];
	matrix[15] = (float) mat[15];
    }


    /**
     * Places the normalized rotational component of this transform
     * into the 3x3 matrix argument.
     * @param m1 the matrix into which the rotational component is placed
     */
    public final void get(Matrix3d m1) {
	if ((dirtyBits & ROTATION_BIT) != 0) {
	    computeScaleRotation(false);
	}
	m1.m00 = rot[0];
	m1.m01 = rot[1];
	m1.m02 = rot[2];
	m1.m10 = rot[3];
	m1.m11 = rot[4];
	m1.m12 = rot[5];
	m1.m20 = rot[6];
	m1.m21 = rot[7];
	m1.m22 = rot[8];
    }

    /**
     * Places the normalized rotational component of this transform
     * into the 3x3 matrix argument.
     * @param m1  the matrix into which the rotational component is placed
     */
    public final void get(Matrix3f m1) {
	if ((dirtyBits & ROTATION_BIT) != 0) {
	    computeScaleRotation(false);
	}
	m1.m00 = (float)rot[0];
	m1.m01 = (float)rot[1];
	m1.m02 = (float)rot[2];
	m1.m10 = (float)rot[3];
	m1.m11 = (float)rot[4];
	m1.m12 = (float)rot[5];
	m1.m20 = (float)rot[6];
	m1.m21 = (float)rot[7];
	m1.m22 = (float)rot[8];
    }

    /**
     * Places the quaternion equivalent of the normalized rotational
     * component of this transform into the quaternion parameter.
     * @param q1  the quaternion into which the rotation component is placed
     */
    public final void get(Quat4f q1) {
	if ((dirtyBits & ROTATION_BIT) != 0) {
	    computeScaleRotation(false);
	}

	double ww = 0.25*(1.0 + rot[0] + rot[4] + rot[8]);
        if (!((ww < 0 ? -ww : ww) < 1.0e-10)) {
	    q1.w = (float)Math.sqrt(ww);
	    ww = 0.25/q1.w;
	    q1.x = (float)((rot[7] - rot[5])*ww);
	    q1.y = (float)((rot[2] - rot[6])*ww);
	    q1.z = (float)((rot[3] - rot[1])*ww);
	    return;
        }

        q1.w = 0.0f;
        ww = -0.5*(rot[4] + rot[8]);
        if (!((ww < 0 ? -ww : ww) < 1.0e-10)) {
	    q1.x =  (float)Math.sqrt(ww);
	    ww = 0.5/q1.x;
	    q1.y = (float)(rot[3]*ww);
	    q1.z = (float)(rot[6]*ww);
	    return;
        }

        q1.x = 0.0f;
        ww = 0.5*(1.0 - rot[8]);
        if (!((ww < 0 ? -ww : ww) < 1.0e-10)) {
	    q1.y =  (float)Math.sqrt(ww);
	    q1.z = (float)(rot[7]/(2.0*q1.y));
	    return;
        }

        q1.y = 0.0f;
        q1.z = 1.0f;
    }

    /**
     * Places the quaternion equivalent of the normalized rotational
     * component of this transform into the quaternion parameter.
     * @param q1  the quaternion into which the rotation component is placed
     */
    public final void get(Quat4d q1) {
	if ((dirtyBits & ROTATION_BIT) != 0) {
	    computeScaleRotation(false);
	}

        double ww = 0.25*(1.0 + rot[0] + rot[4] + rot[8]);
        if (!((ww < 0 ? -ww : ww) < 1.0e-10)) {
	    q1.w = Math.sqrt(ww);
	    ww = 0.25/q1.w;
	    q1.x = (rot[7] - rot[5])*ww;
	    q1.y = (rot[2] - rot[6])*ww;
	    q1.z = (rot[3] - rot[1])*ww;
	    return;
        }

        q1.w = 0.0;
        ww = -0.5*(rot[4] + rot[8]);
        if (!((ww < 0 ? -ww : ww) < 1.0e-10)) {
	    q1.x =  Math.sqrt(ww);
	    ww = 0.5/q1.x;
	    q1.y = rot[3]*ww;
	    q1.z = rot[6]*ww;
	    return;
        }

        q1.x = 0.0;
        ww = 0.5*(1.0 - rot[8]);
        if (!((ww < 0 ? -ww : ww) < 1.0e-10)) {
	    q1.y =  Math.sqrt(ww);
	    q1.z = rot[7]/(2.0*q1.y);
	    return;
        }

        q1.y = 0.0;
        q1.z = 1.0;
    }

    /**
     * Places the values of this transform into the double precision
     * matrix argument.
     * @param matrix   the double precision matrix
     */
    public final void get(Matrix4d matrix) {
	matrix.m00 = mat[0];
	matrix.m01 = mat[1];
	matrix.m02 = mat[2];
	matrix.m03 = mat[3];
	matrix.m10 = mat[4];
	matrix.m11 = mat[5];
	matrix.m12 = mat[6];
	matrix.m13 = mat[7];
	matrix.m20 = mat[8];
	matrix.m21 = mat[9];
	matrix.m22 = mat[10];
	matrix.m23 = mat[11];
	matrix.m30 = mat[12];
	matrix.m31 = mat[13];
	matrix.m32 = mat[14];
	matrix.m33 = mat[15];
    }

    /**
     * Places the values of this transform into the single precision matrix
     * argument.
     * @param matrix   the single precision matrix
     */
    public final void get(Matrix4f matrix) {
	matrix.m00 = (float) mat[0];
	matrix.m01 = (float) mat[1];
	matrix.m02 = (float) mat[2];
	matrix.m03 = (float) mat[3];
	matrix.m10 = (float) mat[4];
	matrix.m11 = (float) mat[5];
	matrix.m12 = (float) mat[6];
	matrix.m13 = (float) mat[7];
	matrix.m20 = (float) mat[8];
	matrix.m21 = (float) mat[9];
	matrix.m22 = (float) mat[10];
	matrix.m23 = (float) mat[11];
	matrix.m30 = (float) mat[12];
	matrix.m31 = (float) mat[13];
	matrix.m32 = (float) mat[14];
	matrix.m33 = (float) mat[15];
    }

   /**
     * Places the quaternion equivalent of the normalized rotational
     * component of this transform into the quaternion parameter;
     * places the translational component into the Vector parameter.
     * @param q1  the quaternion representing the rotation
     * @param t1  the translation component
     * @return  the scale component of this transform
     */
    public final double get(Quat4d q1, Vector3d t1) {

	if ((dirtyBits & ROTATION_BIT) != 0) {
	    computeScaleRotation(false);
	} else if ((dirtyBits & SCALE_BIT) != 0) {
	    computeScales(false);
	}

        t1.x = mat[3];
        t1.y = mat[7];
        t1.z = mat[11];

        double maxScale = max3(scales);

        double ww = 0.25*(1.0 + rot[0] + rot[4] + rot[8]);
        if (!((ww < 0 ? -ww : ww) < EPSILON)) {
	    q1.w = Math.sqrt(ww);
	    ww = 0.25/q1.w;
	    q1.x = (rot[7] - rot[5])*ww;
	    q1.y = (rot[2] - rot[6])*ww;
	    q1.z = (rot[3] - rot[1])*ww;
	    return maxScale;
        }

        q1.w = 0.0;
        ww = -0.5*(rot[4] + rot[8]);
        if (!((ww < 0 ? -ww : ww) < EPSILON)) {
	    q1.x =  Math.sqrt(ww);
	    ww = 0.5/q1.x;
	    q1.y = rot[3]*ww;
	    q1.z = rot[6]*ww;
	    return maxScale;
        }

        q1.x = 0.0;
        ww = 0.5*(1.0 - rot[8]);
        if (!((ww < 0 ? -ww : ww) < EPSILON)) {
	    q1.y =  Math.sqrt(ww);
	    q1.z = rot[7]/(2.0*q1.y);
	    return maxScale;
        }

        q1.y = 0.0;
        q1.z = 1.0;
        return maxScale;
    }


   /**
     * Places the quaternion equivalent of the normalized rotational
     * component of this transform into the quaternion parameter;
     * places the translational component into the Vector parameter.
     * @param q1  the quaternion representing the rotation
     * @param t1  the translation component
     * @return  the scale component of this transform
     */
    public final float get(Quat4f q1, Vector3f t1) {

	if ((dirtyBits & ROTATION_BIT) != 0) {
	    computeScaleRotation(false);
	} else if ((dirtyBits & SCALE_BIT) != 0) {
	    computeScales(false);
	}

        double maxScale = max3(scales);
        t1.x = (float)mat[3];
        t1.y = (float)mat[7];
        t1.z = (float)mat[11];

        double ww = 0.25*(1.0 + rot[0] + rot[4] + rot[8]);
        if (!((ww < 0 ? -ww : ww) < EPSILON)) {
	    q1.w = (float)Math.sqrt(ww);
	    ww = 0.25/q1.w;
	    q1.x = (float)((rot[7] - rot[5])*ww);
	    q1.y = (float)((rot[2] - rot[6])*ww);
	    q1.z = (float)((rot[3] - rot[1])*ww);
	    return (float) maxScale;
        }

        q1.w = 0.0f;
        ww = -0.5*(rot[4] + rot[8]);
        if (!((ww < 0 ? -ww : ww) < EPSILON)) {
	    q1.x =  (float)Math.sqrt(ww);
	    ww = 0.5/q1.x;
	    q1.y = (float)(rot[3]*ww);
	    q1.z = (float)(rot[6]*ww);
	    return (float) maxScale;
        }

        q1.x = 0.0f;
        ww = 0.5*(1.0 - rot[8]);
        if (!((ww < 0? -ww : ww) < EPSILON)) {
	    q1.y =  (float)Math.sqrt(ww);
	    q1.z = (float)(rot[7]/(2.0*q1.y));
	    return (float) maxScale;
        }

        q1.y = 0.0f;
        q1.z = 1.0f;
        return (float) maxScale;
    }

   /**
     * Places the quaternion equivalent of the normalized rotational
     * component of this transform into the quaternion parameter;
     * places the translational component into the Vector parameter.
     * @param q1  the quaternion representing the rotation
     * @param t1  the translation component
     * @return  the scale component of this transform
     */
    public final double get(Quat4f q1, Vector3d t1) {

	if ((dirtyBits & ROTATION_BIT) != 0) {
	    computeScaleRotation(false);
	} else if ((dirtyBits & SCALE_BIT) != 0) {
	    computeScales(false);
	}

	double maxScale = max3(scales);

	t1.x = mat[3];
	t1.y = mat[7];
        t1.z = mat[11];

        double ww = 0.25*(1.0 + rot[0] + rot[4] + rot[8]);
        if (!(( ww < 0 ? -ww : ww) < EPSILON)) {
	    q1.w = (float)Math.sqrt(ww);
	    ww = 0.25/q1.w;
	    q1.x = (float)((rot[7] - rot[5])*ww);
	    q1.y = (float)((rot[2] - rot[6])*ww);
	    q1.z = (float)((rot[3] - rot[1])*ww);
	    return maxScale;
        }

        q1.w = 0.0f;
        ww = -0.5*(rot[4] + rot[8]);
        if (!(( ww < 0 ? -ww : ww) < EPSILON)) {
	    q1.x =  (float)Math.sqrt(ww);
	    ww = 0.5/q1.x;
	    q1.y = (float)(rot[3]*ww);
	    q1.z = (float)(rot[6]*ww);
	    return maxScale;
        }

        q1.x = 0.0f;
        ww = 0.5*(1.0 - rot[8]);
        if (!(( ww < 0 ? -ww : ww) < EPSILON)) {
	    q1.y =  (float)Math.sqrt(ww);
	    q1.z = (float)(rot[7]/(2.0*q1.y));
	    return maxScale;
        }

        q1.y = 0.0f;
        q1.z = 1.0f;
        return maxScale;
    }

   /**
     * Places the normalized rotational component of this transform
     * into the matrix parameter; place the translational component
     * into the vector parameter.
     * @param m1  the normalized matrix representing the rotation
     * @param t1  the translation component
     * @return  the scale component of this transform
     */
    public final double get(Matrix3d m1, Vector3d t1) {

	if ((dirtyBits & ROTATION_BIT) != 0) {
	    computeScaleRotation(false);
	} else if ((dirtyBits & SCALE_BIT) != 0) {
	    computeScales(false);
	}

	t1.x = mat[3];
	t1.y = mat[7];
	t1.z = mat[11];

	m1.m00 = rot[0];
	m1.m01 = rot[1];
	m1.m02 = rot[2];

	m1.m10 = rot[3];
	m1.m11 = rot[4];
	m1.m12 = rot[5];

	m1.m20 = rot[6];
	m1.m21 = rot[7];
	m1.m22 = rot[8];

	return max3(scales);
    }


    /**
     * Places the normalized rotational component of this transform
     * into the matrix parameter; place the translational component
     * into the vector parameter.
     * @param m1  the normalized matrix representing the rotation
     * @param t1  the translation component
     * @return  the scale component of this transform
     */
    public final float get(Matrix3f m1, Vector3f t1) {

	if ((dirtyBits & ROTATION_BIT) != 0) {
	    computeScaleRotation(false);
	} else	if ((dirtyBits & SCALE_BIT) != 0) {
	    computeScales(false);
	}

	t1.x = (float)mat[3];
	t1.y = (float)mat[7];
	t1.z = (float)mat[11];

	m1.m00 = (float)rot[0];
	m1.m01 = (float)rot[1];
	m1.m02 = (float)rot[2];

	m1.m10 = (float)rot[3];
	m1.m11 = (float)rot[4];
	m1.m12 = (float)rot[5];

	m1.m20 = (float)rot[6];
	m1.m21 = (float)rot[7];
	m1.m22 = (float)rot[8];

	return (float) max3(scales);
    }


    /**
     * Places the normalized rotational component of this transform
     * into the matrix parameter; place the translational component
     * into the vector parameter.
     * @param m1  the normalized matrix representing the rotation
     * @param t1  the translation component
     * @return  the scale component of this transform
     */
    public final double get(Matrix3f m1, Vector3d t1) {
	if ((dirtyBits & ROTATION_BIT) != 0) {
	    computeScaleRotation(false);
	} else if ((dirtyBits & SCALE_BIT) != 0) {
	    computeScales(false);
	}

	t1.x = mat[3];
	t1.y = mat[7];
	t1.z = mat[11];

	m1.m00 = (float)rot[0];
	m1.m01 = (float)rot[1];
	m1.m02 = (float)rot[2];

	m1.m10 = (float)rot[3];
	m1.m11 = (float)rot[4];
	m1.m12 = (float)rot[5];

	m1.m20 = (float)rot[6];
	m1.m21 = (float)rot[7];
	m1.m22 = (float)rot[8];

	return max3(scales);
    }


    /**
     * Returns the uniform scale factor of this matrix.
     * If the matrix has non-uniform scale factors, the largest of the
     * x, y, and z scale factors will be returned.
     * @return  the scale factor of this matrix
     */
    public final double getScale() {
	if ((dirtyBits & SCALE_BIT) != 0) {
	    computeScales(false);
	}
	return max3(scales);
   }


    /**
     * Gets the possibly non-uniform scale components of the current
     * transform and places them into the scale vector.
     * @param scale  the vector into which the x,y,z scale values will be placed
     */
    public final void getScale(Vector3d scale) {
	if ((dirtyBits & SCALE_BIT) != 0) {
	    computeScales(false);
	}
	scale.x = scales[0];
	scale.y = scales[1];
	scale.z = scales[2];
    }


    /**
     * Retrieves the translational components of this transform.
     * @param trans  the vector that will receive the translational component
     */
    public final void get(Vector3f trans) {
	trans.x = (float)mat[3];
	trans.y = (float)mat[7];
	trans.z = (float)mat[11];
    }


    /**
     * Retrieves the translational components of this transform.
     * @param trans  the vector that will receive the translational component
     */
    public final void get(Vector3d trans) {
	trans.x = mat[3];
	trans.y = mat[7];
	trans.z = mat[11];
    }

    /**
     * Sets the value of this transform to the inverse of the passed
     * Transform3D parameter.  This method uses the transform type
     * to determine the optimal algorithm for inverting transform t1.
     * @param t1  the transform to be inverted
     * @exception SingularMatrixException thrown if transform t1 is
     * not invertible
     */
    public final void invert(Transform3D t1) {
	if (t1 == this) {
	    invert();
	} else if (t1.isAffine()) {
	    // We can't use invertOrtho() because of numerical
	    // instability unless we set tolerance of ortho test to 0
	    invertAffine(t1);
	} else {
	    invertGeneral(t1);
	}
    }

    /**
     * Inverts this transform in place.  This method uses the transform
     * type to determine the optimal algorithm for inverting this transform.
     * @exception SingularMatrixException thrown if this transform is
     * not invertible
     */
    public final void invert() {
	if (isAffine()) {
	    invertAffine();
	} else {
	    invertGeneral(this);
	}
    }

    /**
     * Congruent invert routine.
     *
     * if uniform scale s
     *
     *  [R | t] => [R^T/s*s | -R^T * t/s*s]
     *
     */

    /*
    final void invertOrtho() {
	double tmp, s1, s2, s3;

	// do not force classifyRigid()
	if (((dirtyBits & CONGRUENT_BIT) == 0) &&
	    ((type & CONGRUENT) != 0)) {
	    s1 = mat[0]*mat[0] + mat[4]*mat[4] + mat[8]*mat[8];
	    if (s1 == 0) {
		throw new SingularMatrixException(J3dI18N.getString("Transform3D1"));
	    }
	    s1 = s2 = s3 = 1/s1;
	    dirtyBits |= ROTSCALESVD_DIRTY;
	} else {
	    // non-uniform scale matrix
	    s1 = mat[0]*mat[0] + mat[4]*mat[4] + mat[8]*mat[8];
	    s2 = mat[1]*mat[1] + mat[5]*mat[5] + mat[9]*mat[9];
	    s3 = mat[2]*mat[2] + mat[6]*mat[6] + mat[10]*mat[10];
	    if ((s1 == 0) || (s2 == 0) || (s3 == 0)) {
		throw new SingularMatrixException(J3dI18N.getString("Transform3D1"));
	    }
	    s1 = 1/s1;
	    s2 = 1/s2;
	    s3 = 1/s3;
	    dirtyBits |= ROTSCALESVD_DIRTY | ORTHO_BIT | CONGRUENT_BIT
		         | RIGID_BIT | CLASSIFY_BIT;
	}
	// multiple by 1/s will cause loss in numerical value
	tmp = mat[1];
	mat[1] = mat[4]*s1;
	mat[4] = tmp*s2;
	tmp = mat[2];
	mat[2] = mat[8]*s1;
	mat[8] = tmp*s3;
	tmp = mat[6];
	mat[6] = mat[9]*s2;
	mat[9] = tmp*s3;
	mat[0] *= s1;
	mat[5] *= s2;
	mat[10] *= s3;

	tmp = mat[3];
	s1 = mat[7];
	mat[3] = -(tmp * mat[0] + s1 * mat[1] + mat[11] * mat[2]);
	mat[7] = -(tmp * mat[4] + s1 * mat[5] + mat[11] * mat[6]);
	mat[11]= -(tmp * mat[8] + s1 * mat[9] + mat[11] * mat[10]);
	mat[12] = mat[13] = mat[14] = 0.0;
	mat[15] = 1.0;
    }
    */

    /**
     * Orthogonal matrix invert routine.
     * Inverts t1 and places the result in "this".
     */
    /*
    final void invertOrtho(Transform3D t1) {
	double s1, s2, s3;

	// do not force classifyRigid()
	if (((t1.dirtyBits & CONGRUENT_BIT) == 0) &&
	    ((t1.type & CONGRUENT) != 0)) {
	    s1 = t1.mat[0]*t1.mat[0] + t1.mat[4]*t1.mat[4] +
		 t1.mat[8]*t1.mat[8];
	    if (s1 == 0) {
		throw new SingularMatrixException(J3dI18N.getString("Transform3D1"));
	    }
	    s1 = s2 = s3 = 1/s1;
	    dirtyBits = t1.dirtyBits | ROTSCALESVD_DIRTY;
	} else {
	    // non-uniform scale matrix
	    s1 = t1.mat[0]*t1.mat[0] + t1.mat[4]*t1.mat[4] +
		 t1.mat[8]*t1.mat[8];
	    s2 = t1.mat[1]*t1.mat[1] + t1.mat[5]*t1.mat[5] +
		 t1.mat[9]*t1.mat[9];
	    s3 = t1.mat[2]*t1.mat[2] + t1.mat[6]*t1.mat[6] +
		 t1.mat[10]*t1.mat[10];

	    if ((s1 == 0) || (s2 == 0) || (s3 == 0)) {
		throw new SingularMatrixException(J3dI18N.getString("Transform3D1"));
	    }
	    s1 = 1/s1;
	    s2 = 1/s2;
	    s3 = 1/s3;
	    dirtyBits = t1.dirtyBits | ROTSCALESVD_DIRTY | ORTHO_BIT |
		 CONGRUENT_BIT | RIGID_BIT;
	}

	mat[0] = t1.mat[0]*s1;
	mat[1] = t1.mat[4]*s1;
	mat[2] = t1.mat[8]*s1;
	mat[4] = t1.mat[1]*s2;
	mat[5] = t1.mat[5]*s2;
	mat[6] = t1.mat[9]*s2;
	mat[8] = t1.mat[2]*s3;
	mat[9] = t1.mat[6]*s3;
	mat[10] = t1.mat[10]*s3;

	mat[3] = -(t1.mat[3] * mat[0] + t1.mat[7] * mat[1] +
		   t1.mat[11] * mat[2]);
	mat[7] = -(t1.mat[3] * mat[4] + t1.mat[7] * mat[5] +
		   t1.mat[11] * mat[6]);
	mat[11]= -(t1.mat[3] * mat[8] + t1.mat[7] * mat[9] +
		   t1.mat[11] * mat[10]);
	mat[12] = mat[13] = mat[14] = 0.0;
	mat[15] = 1.0;
	type = t1.type;
    }
    */

    /**
     * Affine invert routine.  Inverts t1 and places the result in "this".
     */
    final void invertAffine(Transform3D t1) {
	double determinant = t1.affineDeterminant();

	if (determinant == 0.0)
	    throw new SingularMatrixException(J3dI18N.getString("Transform3D1"));


	double s = (t1.mat[0]*t1.mat[0] + t1.mat[1]*t1.mat[1] +
		    t1.mat[2]*t1.mat[2] + t1.mat[3]*t1.mat[3])*
	           (t1.mat[4]*t1.mat[4] + t1.mat[5]*t1.mat[5] +
	            t1.mat[6]*t1.mat[6] + t1.mat[7]*t1.mat[7])*
	           (t1.mat[8]*t1.mat[8] + t1.mat[9]*t1.mat[9] +
		    t1.mat[10]*t1.mat[10] + t1.mat[11]*t1.mat[11]);

	if ((determinant*determinant) < (EPS * s)) {
	    // using invertGeneral is numerically more stable for
	    //this case  see bug 4227733
	    invertGeneral(t1);
	    return;
	}
	s = 1.0 / determinant;

	mat[0] =  (t1.mat[5]*t1.mat[10] - t1.mat[9]*t1.mat[6]) * s;
	mat[1] = -(t1.mat[1]*t1.mat[10] - t1.mat[9]*t1.mat[2]) * s;
	mat[2] =  (t1.mat[1]*t1.mat[6] - t1.mat[5]*t1.mat[2]) * s;
	mat[4] = -(t1.mat[4]*t1.mat[10] - t1.mat[8]*t1.mat[6]) * s;
	mat[5] =  (t1.mat[0]*t1.mat[10] - t1.mat[8]*t1.mat[2]) * s;
	mat[6] = -(t1.mat[0]*t1.mat[6] -  t1.mat[4]*t1.mat[2]) * s;
	mat[8] =  (t1.mat[4]*t1.mat[9] - t1.mat[8]*t1.mat[5]) * s;
	mat[9] = -(t1.mat[0]*t1.mat[9] - t1.mat[8]*t1.mat[1]) * s;
	mat[10]=  (t1.mat[0]*t1.mat[5] - t1.mat[4]*t1.mat[1]) * s;
	mat[3] = -(t1.mat[3] * mat[0] + t1.mat[7] * mat[1] +
		   t1.mat[11] * mat[2]);
	mat[7] = -(t1.mat[3] * mat[4] + t1.mat[7] * mat[5] +
		   t1.mat[11] * mat[6]);
	mat[11]= -(t1.mat[3] * mat[8] + t1.mat[7] * mat[9] +
		   t1.mat[11] * mat[10]);

	mat[12] = mat[13] = mat[14] = 0.0;
	mat[15] = 1.0;

	dirtyBits = t1.dirtyBits | ROTSCALESVD_DIRTY | CLASSIFY_BIT | ORTHO_BIT;
	type = t1.type;
    }

    /**
     * Affine invert routine.  Inverts "this" matrix in place.
     */
    final void invertAffine() {
	double determinant = affineDeterminant();

	if (determinant == 0.0)
	    throw new SingularMatrixException(J3dI18N.getString("Transform3D1"));

	double s = (mat[0]*mat[0] + mat[1]*mat[1] +
	            mat[2]*mat[2] + mat[3]*mat[3])*
	           (mat[4]*mat[4] + mat[5]*mat[5] +
	            mat[6]*mat[6] + mat[7]*mat[7])*
	           (mat[8]*mat[8] + mat[9]*mat[9] +
		    mat[10]*mat[10] + mat[11]*mat[11]);

	if ((determinant*determinant) < (EPS * s)) {
	    invertGeneral(this);
	    return;
	}
	s = 1.0 / determinant;
	double tmp0 =  (mat[5]*mat[10] - mat[9]*mat[6]) * s;
	double tmp1 = -(mat[1]*mat[10] - mat[9]*mat[2]) * s;
	double tmp2 =  (mat[1]*mat[6] -  mat[5]*mat[2]) * s;
	double tmp4 = -(mat[4]*mat[10] - mat[8]*mat[6]) * s;
	double tmp5 =  (mat[0]*mat[10] - mat[8]*mat[2]) * s;
	double tmp6 = -(mat[0]*mat[6]  - mat[4]*mat[2]) * s;
	double tmp8 =  (mat[4]*mat[9]  - mat[8]*mat[5]) * s;
	double tmp9 = -(mat[0]*mat[9]  - mat[8]*mat[1]) * s;
	double tmp10=  (mat[0]*mat[5]  - mat[4]*mat[1]) * s;
	double tmp3 = -(mat[3] * tmp0 + mat[7] * tmp1 + mat[11] * tmp2);
	double tmp7 = -(mat[3] * tmp4 + mat[7] * tmp5 + mat[11] * tmp6);
	mat[11]= -(mat[3] * tmp8 + mat[7] * tmp9 + mat[11] * tmp10);

	mat[0]=tmp0; mat[1]=tmp1; mat[2]=tmp2; mat[3]=tmp3;
	mat[4]=tmp4; mat[5]=tmp5; mat[6]=tmp6; mat[7]=tmp7;
	mat[8]=tmp8; mat[9]=tmp9; mat[10]=tmp10;
	mat[12] = mat[13] = mat[14] = 0.0;
	mat[15] = 1.0;
	dirtyBits |= ROTSCALESVD_DIRTY | CLASSIFY_BIT | ORTHO_BIT;
    }

    /**
     * General invert routine.  Inverts t1 and places the result in "this".
     * Note that this routine handles both the "this" version and the
     * non-"this" version.
     *
     * Also note that since this routine is slow anyway, we won't worry
     * about allocating a little bit of garbage.
     */
    final void invertGeneral(Transform3D t1) {
	double tmp[] = new double[16];
	int row_perm[] = new int[4];
	int i, r, c;

	// Use LU decomposition and backsubstitution code specifically
	// for floating-point 4x4 matrices.

	// Copy source matrix to tmp
	System.arraycopy(t1.mat, 0, tmp, 0, tmp.length);

	// Calculate LU decomposition: Is the matrix singular?
	if (!luDecomposition(tmp, row_perm)) {
	    // Matrix has no inverse
	    throw new SingularMatrixException(J3dI18N.getString("Transform3D1"));
	}

	// Perform back substitution on the identity matrix
	// luDecomposition will set rot[] & scales[] for use
	// in luBacksubstituation
	mat[0] = 1.0;  mat[1] = 0.0;  mat[2] = 0.0;  mat[3] = 0.0;
	mat[4] = 0.0;  mat[5] = 1.0;  mat[6] = 0.0;  mat[7] = 0.0;
	mat[8] = 0.0;  mat[9] = 0.0;  mat[10] = 1.0; mat[11] = 0.0;
	mat[12] = 0.0; mat[13] = 0.0; mat[14] = 0.0; mat[15] = 1.0;
	luBacksubstitution(tmp, row_perm, this.mat);

	type = 0;
	dirtyBits = CLASSIFY_ALL_DIRTY | ROTSCALESVD_DIRTY;
    }


    /**
     * Given a 4x4 array "matrix0", this function replaces it with the
     * LU decomposition of a row-wise permutation of itself.  The input
     * parameters are "matrix0" and "dimen".  The array "matrix0" is also
     * an output parameter.  The vector "row_perm[4]" is an output
     * parameter that contains the row permutations resulting from partial
     * pivoting.  The output parameter "even_row_xchg" is 1 when the
     * number of row exchanges is even, or -1 otherwise.  Assumes data
     * type is always double.
     *
     * This function is similar to luDecomposition, except that it
     * is tuned specifically for 4x4 matrices.
     *
     * @return true if the matrix is nonsingular, or false otherwise.
     */
    //
    // Reference: Press, Flannery, Teukolsky, Vetterling,
    //	      _Numerical_Recipes_in_C_, Cambridge University Press,
    //	      1988, pp 40-45.
    //
    static boolean luDecomposition(double[] matrix0,
				   int[] row_perm) {

	// Can't re-use this temporary since the method is static.
	double row_scale[] = new double[4];

	// Determine implicit scaling information by looping over rows
	{
	    int i, j;
	    int ptr, rs;
	    double big, temp;

	    ptr = 0;
	    rs = 0;

	    // For each row ...
	    i = 4;
	    while (i-- != 0) {
		big = 0.0;

		// For each column, find the largest element in the row
		j = 4;
		while (j-- != 0) {
		    temp = matrix0[ptr++];
		    temp = Math.abs(temp);
		    if (temp > big) {
			big = temp;
		    }
		}

		// Is the matrix singular?
		if (big == 0.0) {
		    return false;
		}
		row_scale[rs++] = 1.0 / big;
	    }
	}

	{
	    int j;
	    int mtx;

	    mtx = 0;

	    // For all columns, execute Crout's method
	    for (j = 0; j < 4; j++) {
		int i, imax, k;
		int target, p1, p2;
		double sum, big, temp;

		// Determine elements of upper diagonal matrix U
		for (i = 0; i < j; i++) {
		    target = mtx + (4*i) + j;
		    sum = matrix0[target];
		    k = i;
		    p1 = mtx + (4*i);
		    p2 = mtx + j;
		    while (k-- != 0) {
			sum -= matrix0[p1] * matrix0[p2];
			p1++;
			p2 += 4;
		    }
		    matrix0[target] = sum;
		}

		// Search for largest pivot element and calculate
		// intermediate elements of lower diagonal matrix L.
		big = 0.0;
		imax = -1;
		for (i = j; i < 4; i++) {
		    target = mtx + (4*i) + j;
		    sum = matrix0[target];
		    k = j;
		    p1 = mtx + (4*i);
		    p2 = mtx + j;
		    while (k-- != 0) {
			sum -= matrix0[p1] * matrix0[p2];
			p1++;
			p2 += 4;
		    }
		    matrix0[target] = sum;

		    // Is this the best pivot so far?
		    if ((temp = row_scale[i] * Math.abs(sum)) >= big) {
			big = temp;
			imax = i;
		    }
		}

		if (imax < 0) {
		    return false;
		}

		// Is a row exchange necessary?
		if (j != imax) {
		    // Yes: exchange rows
		    k = 4;
		    p1 = mtx + (4*imax);
		    p2 = mtx + (4*j);
		    while (k-- != 0) {
			temp = matrix0[p1];
			matrix0[p1++] = matrix0[p2];
			matrix0[p2++] = temp;
		    }

		    // Record change in scale factor
		    row_scale[imax] = row_scale[j];
		}

		// Record row permutation
		row_perm[j] = imax;

		// Is the matrix singular
		if (matrix0[(mtx + (4*j) + j)] == 0.0) {
		    return false;
		}

		// Divide elements of lower diagonal matrix L by pivot
		if (j != (4-1)) {
		    temp = 1.0 / (matrix0[(mtx + (4*j) + j)]);
		    target = mtx + (4*(j+1)) + j;
		    i = 3 - j;
		    while (i-- != 0) {
			matrix0[target] *= temp;
			target += 4;
		    }
		}
	    }
	}

	return true;
    }


    /**
     * Solves a set of linear equations.  The input parameters "matrix1",
     * and "row_perm" come from luDecompostionD4x4 and do not change
     * here.  The parameter "matrix2" is a set of column vectors assembled
     * into a 4x4 matrix of floating-point values.  The procedure takes each
     * column of "matrix2" in turn and treats it as the right-hand side of the
     * matrix equation Ax = LUx = b.  The solution vector replaces the
     * original column of the matrix.
     *
     * If "matrix2" is the identity matrix, the procedure replaces its contents
     * with the inverse of the matrix from which "matrix1" was originally
     * derived.
     */
    //
    // Reference: Press, Flannery, Teukolsky, Vetterling,
    //	      _Numerical_Recipes_in_C_, Cambridge University Press,
    //	      1988, pp 44-45.
    //
    static void luBacksubstitution(double[] matrix1,
				   int[] row_perm,
				   double[] matrix2) {

	int i, ii, ip, j, k;
	int rp;
	int cv, rv;

	//	rp = row_perm;
	rp = 0;

	// For each column vector of matrix2 ...
	for (k = 0; k < 4; k++) {
	    //	    cv = &(matrix2[0][k]);
	    cv = k;
	    ii = -1;

	    // Forward substitution
	    for (i = 0; i < 4; i++) {
		double sum;

		ip = row_perm[rp+i];
		sum = matrix2[cv+4*ip];
		matrix2[cv+4*ip] = matrix2[cv+4*i];
		if (ii >= 0) {
		    //		    rv = &(matrix1[i][0]);
		    rv = i*4;
		    for (j = ii; j <= i-1; j++) {
			sum -= matrix1[rv+j] * matrix2[cv+4*j];
		    }
		}
		else if (sum != 0.0) {
		    ii = i;
		}
		matrix2[cv+4*i] = sum;
	    }

	    // Backsubstitution
	    //	    rv = &(matrix1[3][0]);
	    rv = 3*4;
	    matrix2[cv+4*3] /= matrix1[rv+3];

	    rv -= 4;
	    matrix2[cv+4*2] = (matrix2[cv+4*2] -
			    matrix1[rv+3] * matrix2[cv+4*3]) / matrix1[rv+2];

	    rv -= 4;
	    matrix2[cv+4*1] = (matrix2[cv+4*1] -
			    matrix1[rv+2] * matrix2[cv+4*2] -
			    matrix1[rv+3] * matrix2[cv+4*3]) / matrix1[rv+1];

	    rv -= 4;
	    matrix2[cv+4*0] = (matrix2[cv+4*0] -
			    matrix1[rv+1] * matrix2[cv+4*1] -
			    matrix1[rv+2] * matrix2[cv+4*2] -
			    matrix1[rv+3] * matrix2[cv+4*3]) / matrix1[rv+0];
	}
    }

    // given that this matrix is affine
    final double affineDeterminant() {
	return mat[0]*(mat[5]*mat[10] - mat[6]*mat[9]) -
	       mat[1]*(mat[4]*mat[10] - mat[6]*mat[8]) +
	       mat[2]*(mat[4]*mat[ 9] - mat[5]*mat[8]);
    }

    /**
     * Calculates and returns the determinant of this transform.
     * @return  the double precision determinant
     */
     public final double determinant() {

	 if (isAffine()) {
	     return mat[0]*(mat[5]*mat[10] - mat[6]*mat[9]) -
	 	    mat[1]*(mat[4]*mat[10] - mat[6]*mat[8]) +
		    mat[2]*(mat[4]*mat[ 9] - mat[5]*mat[8]);
	 }
	 // cofactor exapainsion along first row
	 return mat[0]*(mat[5]*(mat[10]*mat[15] - mat[11]*mat[14]) -
			mat[6]*(mat[ 9]*mat[15] - mat[11]*mat[13]) +
			mat[7]*(mat[ 9]*mat[14] - mat[10]*mat[13])) -
	        mat[1]*(mat[4]*(mat[10]*mat[15] - mat[11]*mat[14]) -
	                mat[6]*(mat[ 8]*mat[15] - mat[11]*mat[12]) +
			mat[7]*(mat[ 8]*mat[14] - mat[10]*mat[12])) +
	        mat[2]*(mat[4]*(mat[ 9]*mat[15] - mat[11]*mat[13]) -
			mat[5]*(mat[ 8]*mat[15] - mat[11]*mat[12]) +
			mat[7]*(mat[ 8]*mat[13] - mat[ 9]*mat[12])) -
	        mat[3]*(mat[4]*(mat[ 9]*mat[14] - mat[10]*mat[13]) -
			mat[5]*(mat[ 8]*mat[14] - mat[10]*mat[12]) +
			mat[6]*(mat[ 8]*mat[13] - mat[ 9]*mat[12]));
     }

     /**
      * Sets the value of this transform to a uniform scale; all of
      * the matrix values are modified.
      * @param scale the scale factor for the transform
      */
    public final void set(double scale) {
	setScaleTranslation(0, 0, 0, scale);
    }


    /**
     * Sets the value of this transform to a scale and translation
     * matrix; the scale is not applied to the translation and all
     * of the matrix values are modified.
     * @param scale the scale factor for the transform
     * @param v1 the translation amount
     */
    public final void set(double scale, Vector3d v1) {
	setScaleTranslation(v1.x, v1.y, v1.z, scale);
    }


    /**
     * Sets the value of this transform to a scale and translation
     * matrix; the scale is not applied to the translation and all
     * of the matrix values are modified.
     * @param scale the scale factor for the transform
     * @param v1 the translation amount
     */
    public final void set(float scale, Vector3f v1)  {
	setScaleTranslation(v1.x, v1.y, v1.z, scale);
    }

    /**
     * Sets the value of this transform to a scale and translation matrix;
     * the translation is scaled by the scale factor and all of the
     * matrix values are modified.
     * @param v1 the translation amount
     * @param scale the scale factor for the transform AND the translation
     */
    public final void set(Vector3d v1, double scale) {
	setScaleTranslation(v1.x*scale, v1.y*scale, v1.z*scale, scale);
    }

    /**
     * Sets the value of this transform to a scale and translation matrix;
     * the translation is scaled by the scale factor and all of the
     * matrix values are modified.
     * @param v1 the translation amount
     * @param scale the scale factor for the transform AND the translation
     */
    public final void set(Vector3f v1, float scale) {
	setScaleTranslation(v1.x*scale, v1.y*scale, v1.z*scale, scale);
    }

    private final void setScaleTranslation(double x, double y,
					   double z, double scale) {
	mat[0] = scale;
	mat[1] = 0.0;
	mat[2] = 0.0;
	mat[3] = x;
	mat[4] = 0.0;
	mat[5] = scale;
	mat[6] = 0.0;
	mat[7] = y;
	mat[8] = 0.0;
	mat[9] = 0.0;
	mat[10] = scale;
	mat[11] = z;
	mat[12] = 0.0;
	mat[13] = 0.0;
	mat[14] = 0.0;
	mat[15] = 1.0;

	if(scales == null)
	    scales = new double[3];

	scales[0] = scales[1] = scales[2] = scale;

	type = AFFINE | CONGRUENT | ORTHO;
	dirtyBits = CLASSIFY_BIT | ROTATION_BIT | RIGID_BIT;
    }



   /**
     * Multiplies each element of this transform by a scalar.
     * @param scalar  the scalar multiplier
     */
    public final void mul(double scalar) {
	for (int i=0 ; i<16 ; i++) {
	    mat[i] *= scalar;
	}
	dirtyBits = CLASSIFY_ALL_DIRTY | ROTSCALESVD_DIRTY;
    }

   /**
     * Multiplies each element of transform t1 by a scalar and places
     * the result into this.  Transform t1 is not modified.
     * @param scalar  the scalar multiplier
     * @param t1  the original transform
     */
    public final void mul(double scalar, Transform3D t1)  {
	for (int i=0 ; i<16 ; i++) {
	    mat[i] = t1.mat[i] * scalar;
	}
	dirtyBits = CLASSIFY_ALL_DIRTY | ROTSCALESVD_DIRTY;

	if (autoNormalize) {
	    normalize();
	}

    }


    /**
     * Sets the value of this transform to the result of multiplying itself
     * with transform t1 (this = this * t1).
     * @param t1 the other transform
     */
    public final void mul(Transform3D t1) {
	double tmp0, tmp1, tmp2, tmp3;
	double tmp4, tmp5, tmp6, tmp7;
	double tmp8, tmp9, tmp10, tmp11;
	boolean aff = false;

	if (t1.isAffine()) {
	    tmp0 = mat[0]*t1.mat[0] + mat[1]*t1.mat[4] + mat[2]*t1.mat[8];
	    tmp1 = mat[0]*t1.mat[1] + mat[1]*t1.mat[5] + mat[2]*t1.mat[9];
	    tmp2 = mat[0]*t1.mat[2] + mat[1]*t1.mat[6] + mat[2]*t1.mat[10];
	    tmp3 = mat[0]*t1.mat[3] + mat[1]*t1.mat[7] + mat[2]*t1.mat[11] + mat[3];
	    tmp4 = mat[4]*t1.mat[0] + mat[5]*t1.mat[4] + mat[6]*t1.mat[8];
	    tmp5 = mat[4]*t1.mat[1] + mat[5]*t1.mat[5] + mat[6]*t1.mat[9];
	    tmp6 = mat[4]*t1.mat[2] + mat[5]*t1.mat[6] + mat[6]*t1.mat[10];
	    tmp7 = mat[4]*t1.mat[3] + mat[5]*t1.mat[7] + mat[6]*t1.mat[11] + mat[7];
	    tmp8 = mat[8]*t1.mat[0] + mat[9]*t1.mat[4] + mat[10]*t1.mat[8];
	    tmp9 = mat[8]*t1.mat[1] + mat[9]*t1.mat[5] + mat[10]*t1.mat[9];
	    tmp10 = mat[8]*t1.mat[2] + mat[9]*t1.mat[6] + mat[10]*t1.mat[10];
	    tmp11 = mat[8]*t1.mat[3] + mat[9]*t1.mat[7] + mat[10]*t1.mat[11] + mat[11];
	    if (isAffine()) {
		mat[12] =  mat[13] = mat[14] = 0;
		mat[15] = 1;
		aff = true;
	    } else {
		double tmp12 = mat[12]*t1.mat[0] + mat[13]*t1.mat[4] +
		               mat[14]*t1.mat[8];
		double tmp13 = mat[12]*t1.mat[1] + mat[13]*t1.mat[5] +
		               mat[14]*t1.mat[9];
		double tmp14 = mat[12]*t1.mat[2] + mat[13]*t1.mat[6] +
		               mat[14]*t1.mat[10];
                double tmp15 = mat[12]*t1.mat[3] + mat[13]*t1.mat[7] +
		               mat[14]*t1.mat[11] + mat[15];
		mat[12] = tmp12;
		mat[13] = tmp13;
		mat[14] = tmp14;
		mat[15] = tmp15;
	    }
	} else {
	    tmp0 = mat[0]*t1.mat[0] + mat[1]*t1.mat[4] + mat[2]*t1.mat[8] +
                   mat[3]*t1.mat[12];
	    tmp1 = mat[0]*t1.mat[1] + mat[1]*t1.mat[5] + mat[2]*t1.mat[9] +
                   mat[3]*t1.mat[13];
	    tmp2 = mat[0]*t1.mat[2] + mat[1]*t1.mat[6] + mat[2]*t1.mat[10] +
                   mat[3]*t1.mat[14];
	    tmp3 = mat[0]*t1.mat[3] + mat[1]*t1.mat[7] + mat[2]*t1.mat[11] +
                   mat[3]*t1.mat[15];
            tmp4 = mat[4]*t1.mat[0] + mat[5]*t1.mat[4] + mat[6]*t1.mat[8] +
		   mat[7]*t1.mat[12];
	    tmp5 = mat[4]*t1.mat[1] + mat[5]*t1.mat[5] + mat[6]*t1.mat[9] +
                   mat[7]*t1.mat[13];
	    tmp6 = mat[4]*t1.mat[2] + mat[5]*t1.mat[6] + mat[6]*t1.mat[10] +
                   mat[7]*t1.mat[14];
	    tmp7 = mat[4]*t1.mat[3] + mat[5]*t1.mat[7] + mat[6]*t1.mat[11] +
                   mat[7]*t1.mat[15];
            tmp8 = mat[8]*t1.mat[0] + mat[9]*t1.mat[4] + mat[10]*t1.mat[8] +
                   mat[11]*t1.mat[12];
            tmp9 = mat[8]*t1.mat[1] + mat[9]*t1.mat[5] + mat[10]*t1.mat[9] +
                   mat[11]*t1.mat[13];
            tmp10 = mat[8]*t1.mat[2] + mat[9]*t1.mat[6] +
		    mat[10]*t1.mat[10]+ mat[11]*t1.mat[14];
            tmp11 = mat[8]*t1.mat[3] + mat[9]*t1.mat[7] +
		    mat[10]*t1.mat[11] + mat[11]*t1.mat[15];

	    if (isAffine()) {
		mat[12] = t1.mat[12];
		mat[13] = t1.mat[13];
		mat[14] = t1.mat[14];
		mat[15] = t1.mat[15];
	    } else {
		double tmp12 = mat[12]*t1.mat[0] + mat[13]*t1.mat[4] +
		               mat[14]*t1.mat[8] +  mat[15]*t1.mat[12];
		double tmp13 = mat[12]*t1.mat[1] + mat[13]*t1.mat[5] +
		               mat[14]*t1.mat[9] + mat[15]*t1.mat[13];
		double tmp14 = mat[12]*t1.mat[2] + mat[13]*t1.mat[6] +
		               mat[14]*t1.mat[10] + mat[15]*t1.mat[14];
		double tmp15 = mat[12]*t1.mat[3] + mat[13]*t1.mat[7] +
		               mat[14]*t1.mat[11] + mat[15]*t1.mat[15];
		mat[12] = tmp12;
		mat[13] = tmp13;
		mat[14] = tmp14;
		mat[15] = tmp15;
	    }
	}

	mat[0] = tmp0;
	mat[1] = tmp1;
	mat[2] = tmp2;
	mat[3] = tmp3;
	mat[4] = tmp4;
	mat[5] = tmp5;
	mat[6] = tmp6;
	mat[7] = tmp7;
	mat[8] = tmp8;
	mat[9] = tmp9;
	mat[10] = tmp10;
	mat[11] = tmp11;

	if (((dirtyBits & CONGRUENT_BIT) == 0) &&
	    ((type & CONGRUENT) != 0) &&
	    ((t1.dirtyBits & CONGRUENT_BIT) == 0) &&
	    ((t1.type & CONGRUENT) != 0)) {
	    type &= t1.type;
	    dirtyBits |= t1.dirtyBits | CLASSIFY_BIT |
		ROTSCALESVD_DIRTY | RIGID_BIT;
	} else {
	    if (aff) {
		dirtyBits = ORTHO_BIT | CONGRUENT_BIT | RIGID_BIT |
                            CLASSIFY_BIT | ROTSCALESVD_DIRTY;
	    } else {
		dirtyBits = CLASSIFY_ALL_DIRTY | ROTSCALESVD_DIRTY;
	    }
	}

	if (autoNormalize) {
	    normalize();
	}

    }

    /**
     * Sets the value of this transform to the result of multiplying transform
     * t1 by transform t2 (this = t1*t2).
     * @param t1  the left transform
     * @param t2  the right transform
     */
    public final void mul(Transform3D t1, Transform3D t2) {
	boolean aff = false;
	if ((this != t1)  &&  (this != t2)) {
	    if (t2.isAffine()) {

		mat[0] = t1.mat[0]*t2.mat[0] + t1.mat[1]*t2.mat[4] + t1.mat[2]*t2.mat[8];
		mat[1] = t1.mat[0]*t2.mat[1] + t1.mat[1]*t2.mat[5] + t1.mat[2]*t2.mat[9];
		mat[2] = t1.mat[0]*t2.mat[2] + t1.mat[1]*t2.mat[6] + t1.mat[2]*t2.mat[10];
		mat[3] = t1.mat[0]*t2.mat[3] + t1.mat[1]*t2.mat[7] +
                         t1.mat[2]*t2.mat[11] + t1.mat[3];
		mat[4] = t1.mat[4]*t2.mat[0] + t1.mat[5]*t2.mat[4] + t1.mat[6]*t2.mat[8];
		mat[5] = t1.mat[4]*t2.mat[1] + t1.mat[5]*t2.mat[5] + t1.mat[6]*t2.mat[9];
	        mat[6] = t1.mat[4]*t2.mat[2] + t1.mat[5]*t2.mat[6] + t1.mat[6]*t2.mat[10];
	        mat[7] = t1.mat[4]*t2.mat[3] + t1.mat[5]*t2.mat[7] +
                         t1.mat[6]*t2.mat[11] + t1.mat[7];
		mat[8] = t1.mat[8]*t2.mat[0] + t1.mat[9]*t2.mat[4] + t1.mat[10]*t2.mat[8];
	        mat[9] = t1.mat[8]*t2.mat[1] + t1.mat[9]*t2.mat[5] + t1.mat[10]*t2.mat[9];
		mat[10] = t1.mat[8]*t2.mat[2] + t1.mat[9]*t2.mat[6] + t1.mat[10]*t2.mat[10];
	        mat[11] = t1.mat[8]*t2.mat[3] + t1.mat[9]*t2.mat[7] +
                          t1.mat[10]*t2.mat[11] + t1.mat[11];
		if (t1.isAffine()) {
		    aff = true;
		    mat[12] =  mat[13] = mat[14] = 0;
		    mat[15] = 1;
		} else {
		    mat[12] = t1.mat[12]*t2.mat[0] + t1.mat[13]*t2.mat[4] +
			      t1.mat[14]*t2.mat[8];
		    mat[13] = t1.mat[12]*t2.mat[1] + t1.mat[13]*t2.mat[5] +
		              t1.mat[14]*t2.mat[9];
		    mat[14] = t1.mat[12]*t2.mat[2] + t1.mat[13]*t2.mat[6] +
		              t1.mat[14]*t2.mat[10];
		    mat[15] = t1.mat[12]*t2.mat[3] + t1.mat[13]*t2.mat[7] +
			      t1.mat[14]*t2.mat[11] + t1.mat[15];
		}
	    } else {
		mat[0] = t1.mat[0]*t2.mat[0] + t1.mat[1]*t2.mat[4] +
		         t1.mat[2]*t2.mat[8] + t1.mat[3]*t2.mat[12];
		mat[1] = t1.mat[0]*t2.mat[1] + t1.mat[1]*t2.mat[5] +
		         t1.mat[2]*t2.mat[9] + t1.mat[3]*t2.mat[13];
		mat[2] = t1.mat[0]*t2.mat[2] + t1.mat[1]*t2.mat[6] +
		         t1.mat[2]*t2.mat[10] + t1.mat[3]*t2.mat[14];
		mat[3] = t1.mat[0]*t2.mat[3] + t1.mat[1]*t2.mat[7] +
		         t1.mat[2]*t2.mat[11] + t1.mat[3]*t2.mat[15];
		mat[4] = t1.mat[4]*t2.mat[0] + t1.mat[5]*t2.mat[4] +
		         t1.mat[6]*t2.mat[8] + t1.mat[7]*t2.mat[12];
		mat[5] = t1.mat[4]*t2.mat[1] + t1.mat[5]*t2.mat[5] +
		         t1.mat[6]*t2.mat[9] + t1.mat[7]*t2.mat[13];
		mat[6] = t1.mat[4]*t2.mat[2] + t1.mat[5]*t2.mat[6] +
		         t1.mat[6]*t2.mat[10] + t1.mat[7]*t2.mat[14];
		mat[7] = t1.mat[4]*t2.mat[3] + t1.mat[5]*t2.mat[7] +
		         t1.mat[6]*t2.mat[11] + t1.mat[7]*t2.mat[15];
		mat[8] = t1.mat[8]*t2.mat[0] + t1.mat[9]*t2.mat[4] +
		         t1.mat[10]*t2.mat[8] + t1.mat[11]*t2.mat[12];
		mat[9] = t1.mat[8]*t2.mat[1] + t1.mat[9]*t2.mat[5] +
		         t1.mat[10]*t2.mat[9] + t1.mat[11]*t2.mat[13];
		mat[10] = t1.mat[8]*t2.mat[2] + t1.mat[9]*t2.mat[6] +
		          t1.mat[10]*t2.mat[10] + t1.mat[11]*t2.mat[14];
		mat[11] = t1.mat[8]*t2.mat[3] + t1.mat[9]*t2.mat[7] +
		          t1.mat[10]*t2.mat[11] + t1.mat[11]*t2.mat[15];
		if (t1.isAffine()) {
		    mat[12] = t2.mat[12];
		    mat[13] = t2.mat[13];
		    mat[14] = t2.mat[14];
		    mat[15] = t2.mat[15];
		} else {
		    mat[12] = t1.mat[12]*t2.mat[0] + t1.mat[13]*t2.mat[4] +
		              t1.mat[14]*t2.mat[8] + t1.mat[15]*t2.mat[12];
		    mat[13] = t1.mat[12]*t2.mat[1] + t1.mat[13]*t2.mat[5] +
		              t1.mat[14]*t2.mat[9] + t1.mat[15]*t2.mat[13];
		    mat[14] = t1.mat[12]*t2.mat[2] + t1.mat[13]*t2.mat[6] +
			      t1.mat[14]*t2.mat[10] + t1.mat[15]*t2.mat[14];
		    mat[15] = t1.mat[12]*t2.mat[3] + t1.mat[13]*t2.mat[7] +
		              t1.mat[14]*t2.mat[11] + t1.mat[15]*t2.mat[15];
		}
	    }
	} else {
	    double tmp0, tmp1, tmp2, tmp3;
	    double tmp4, tmp5, tmp6, tmp7;
	    double tmp8, tmp9, tmp10, tmp11;

	    if (t2.isAffine()) {
		tmp0 = t1.mat[0]*t2.mat[0] + t1.mat[1]*t2.mat[4] + t1.mat[2]*t2.mat[8];
		tmp1 = t1.mat[0]*t2.mat[1] + t1.mat[1]*t2.mat[5] + t1.mat[2]*t2.mat[9];
		tmp2 = t1.mat[0]*t2.mat[2] + t1.mat[1]*t2.mat[6] + t1.mat[2]*t2.mat[10];
		tmp3 = t1.mat[0]*t2.mat[3] + t1.mat[1]*t2.mat[7] +
		       t1.mat[2]*t2.mat[11] + t1.mat[3];
		tmp4 = t1.mat[4]*t2.mat[0] + t1.mat[5]*t2.mat[4] + t1.mat[6]*t2.mat[8];
		tmp5 = t1.mat[4]*t2.mat[1] + t1.mat[5]*t2.mat[5] + t1.mat[6]*t2.mat[9];
	        tmp6 = t1.mat[4]*t2.mat[2] + t1.mat[5]*t2.mat[6] + t1.mat[6]*t2.mat[10];
	        tmp7 = t1.mat[4]*t2.mat[3] + t1.mat[5]*t2.mat[7] +
                       t1.mat[6]*t2.mat[11] + t1.mat[7];
		tmp8 = t1.mat[8]*t2.mat[0] + t1.mat[9]*t2.mat[4] + t1.mat[10]*t2.mat[8];
	        tmp9 = t1.mat[8]*t2.mat[1] + t1.mat[9]*t2.mat[5] + t1.mat[10]*t2.mat[9];
		tmp10 = t1.mat[8]*t2.mat[2] + t1.mat[9]*t2.mat[6] + t1.mat[10]*t2.mat[10];
	        tmp11 = t1.mat[8]*t2.mat[3] + t1.mat[9]*t2.mat[7] +
                        t1.mat[10]*t2.mat[11] + t1.mat[11];
		if (t1.isAffine()) {
		    aff = true;
		    mat[12] =  mat[13] = mat[14] = 0;
		    mat[15] = 1;
		} else {
		    double tmp12 = t1.mat[12]*t2.mat[0] + t1.mat[13]*t2.mat[4] +
			           t1.mat[14]*t2.mat[8];
		    double tmp13 = t1.mat[12]*t2.mat[1] + t1.mat[13]*t2.mat[5] +
		                   t1.mat[14]*t2.mat[9];
		    double tmp14 = t1.mat[12]*t2.mat[2] + t1.mat[13]*t2.mat[6] +
		                   t1.mat[14]*t2.mat[10];
		    double tmp15 = t1.mat[12]*t2.mat[3] + t1.mat[13]*t2.mat[7] +
			           t1.mat[14]*t2.mat[11] + t1.mat[15];
		    mat[12] = tmp12;
		    mat[13] = tmp13;
		    mat[14] = tmp14;
		    mat[15] = tmp15;
		}
	    } else {
		tmp0 = t1.mat[0]*t2.mat[0] + t1.mat[1]*t2.mat[4] +
		       t1.mat[2]*t2.mat[8] + t1.mat[3]*t2.mat[12];
		tmp1 = t1.mat[0]*t2.mat[1] + t1.mat[1]*t2.mat[5] +
		       t1.mat[2]*t2.mat[9] + t1.mat[3]*t2.mat[13];
		tmp2 = t1.mat[0]*t2.mat[2] + t1.mat[1]*t2.mat[6] +
		       t1.mat[2]*t2.mat[10] + t1.mat[3]*t2.mat[14];
		tmp3 = t1.mat[0]*t2.mat[3] + t1.mat[1]*t2.mat[7] +
		       t1.mat[2]*t2.mat[11] + t1.mat[3]*t2.mat[15];
		tmp4 = t1.mat[4]*t2.mat[0] + t1.mat[5]*t2.mat[4] +
		       t1.mat[6]*t2.mat[8] + t1.mat[7]*t2.mat[12];
		tmp5 = t1.mat[4]*t2.mat[1] + t1.mat[5]*t2.mat[5] +
		       t1.mat[6]*t2.mat[9] + t1.mat[7]*t2.mat[13];
		tmp6 = t1.mat[4]*t2.mat[2] + t1.mat[5]*t2.mat[6] +
		       t1.mat[6]*t2.mat[10] + t1.mat[7]*t2.mat[14];
		tmp7 = t1.mat[4]*t2.mat[3] + t1.mat[5]*t2.mat[7] +
		       t1.mat[6]*t2.mat[11] + t1.mat[7]*t2.mat[15];
		tmp8 = t1.mat[8]*t2.mat[0] + t1.mat[9]*t2.mat[4] +
		       t1.mat[10]*t2.mat[8] + t1.mat[11]*t2.mat[12];
		tmp9 = t1.mat[8]*t2.mat[1] + t1.mat[9]*t2.mat[5] +
		       t1.mat[10]*t2.mat[9] + t1.mat[11]*t2.mat[13];
		tmp10 = t1.mat[8]*t2.mat[2] + t1.mat[9]*t2.mat[6] +
		        t1.mat[10]*t2.mat[10] + t1.mat[11]*t2.mat[14];
		tmp11 = t1.mat[8]*t2.mat[3] + t1.mat[9]*t2.mat[7] +
		        t1.mat[10]*t2.mat[11] + t1.mat[11]*t2.mat[15];

		if (t1.isAffine()) {
		    mat[12] = t2.mat[12];
		    mat[13] = t2.mat[13];
		    mat[14] = t2.mat[14];
		    mat[15] = t2.mat[15];
		} else {
		    double tmp12 = t1.mat[12]*t2.mat[0] + t1.mat[13]*t2.mat[4] +
		                   t1.mat[14]*t2.mat[8] + t1.mat[15]*t2.mat[12];
		    double tmp13 = t1.mat[12]*t2.mat[1] + t1.mat[13]*t2.mat[5] +
		                   t1.mat[14]*t2.mat[9] + t1.mat[15]*t2.mat[13];
		    double tmp14 = t1.mat[12]*t2.mat[2] + t1.mat[13]*t2.mat[6] +
			           t1.mat[14]*t2.mat[10] + t1.mat[15]*t2.mat[14];
		    double tmp15 = t1.mat[12]*t2.mat[3] + t1.mat[13]*t2.mat[7] +
		                   t1.mat[14]*t2.mat[11] + t1.mat[15]*t2.mat[15];
		    mat[12] = tmp12;
		    mat[13] = tmp13;
		    mat[14] = tmp14;
		    mat[15] = tmp15;
		}
	    }
	    mat[0] = tmp0;
	    mat[1] = tmp1;
	    mat[2] = tmp2;
	    mat[3] = tmp3;
	    mat[4] = tmp4;
	    mat[5] = tmp5;
	    mat[6] = tmp6;
	    mat[7] = tmp7;
	    mat[8] = tmp8;
	    mat[9] = tmp9;
	    mat[10] = tmp10;
	    mat[11] = tmp11;
	}


	if (((t1.dirtyBits & CONGRUENT_BIT) == 0) &&
	    ((t1.type & CONGRUENT) != 0) &&
	    ((t2.dirtyBits & CONGRUENT_BIT) == 0) &&
	    ((t2.type & CONGRUENT) != 0)) {
	    type = t1.type & t2.type;
	    dirtyBits = t1.dirtyBits | t2.dirtyBits | CLASSIFY_BIT |
		        ROTSCALESVD_DIRTY | RIGID_BIT;
	} else {
	    if (aff) {
		dirtyBits = ORTHO_BIT | CONGRUENT_BIT | RIGID_BIT |
                            CLASSIFY_BIT | ROTSCALESVD_DIRTY;
	    } else {
		dirtyBits = CLASSIFY_ALL_DIRTY | ROTSCALESVD_DIRTY;
	    }
	}

	if (autoNormalize) {
	    normalize();
	}
    }

    /**
     * Multiplies this transform by the inverse of transform t1. The final
     * value is placed into this matrix (this = this*t1^-1).
     * @param t1  the matrix whose inverse is computed.
     */
    public final void mulInverse(Transform3D t1) {
	Transform3D t2 = VirtualUniverse.mc.getTransform3D(null);
	t2.autoNormalize = false;
	t2.invert(t1);
	this.mul(t2);
	FreeListManager.freeObject(FreeListManager.TRANSFORM3D, t2);
    }


    /**
     * Multiplies transform t1 by the inverse of transform t2. The final
     * value is placed into this matrix (this = t1*t2^-1).
     * @param t1  the left transform in the multiplication
     * @param t2  the transform whose inverse is computed.
     */
    public final void mulInverse(Transform3D t1, Transform3D t2) {
        Transform3D t3 = VirtualUniverse.mc.getTransform3D(null);
	t3.autoNormalize = false;
        t3.invert(t2);
        this.mul(t1,t3);
	FreeListManager.freeObject(FreeListManager.TRANSFORM3D, t3);
    }

    /**
     * Multiplies transform t1 by the transpose of transform t2 and places
     * the result into this transform (this = t1 * transpose(t2)).
     * @param t1  the transform on the left hand side of the multiplication
     * @param t2  the transform whose transpose is computed
     */
    public final void mulTransposeRight(Transform3D t1, Transform3D t2) {
	Transform3D t3 = VirtualUniverse.mc.getTransform3D(null);
	t3.autoNormalize = false;
	t3.transpose(t2);
	mul(t1, t3);
	FreeListManager.freeObject(FreeListManager.TRANSFORM3D, t3);
    }


    /**
     * Multiplies the transpose of transform t1 by transform t2 and places
     * the result into this matrix (this = transpose(t1) * t2).
     * @param t1  the transform whose transpose is computed
     * @param t2  the transform on the right hand side of the multiplication
     */
    public final void mulTransposeLeft(Transform3D t1, Transform3D t2){
	Transform3D t3 = VirtualUniverse.mc.getTransform3D(null);
	t3.autoNormalize = false;
	t3.transpose(t1);
	mul(t3, t2);
	FreeListManager.freeObject(FreeListManager.TRANSFORM3D, t3);
    }


    /**
     * Multiplies the transpose of transform t1 by the transpose of
     * transform t2 and places the result into this transform
     * (this = transpose(t1) * transpose(t2)).
     * @param t1  the transform on the left hand side of the multiplication
     * @param t2  the transform on the right hand side of the multiplication
     */
    public final void mulTransposeBoth(Transform3D t1, Transform3D t2) {
	Transform3D t3 = VirtualUniverse.mc.getTransform3D(null);
	Transform3D t4 = VirtualUniverse.mc.getTransform3D(null);
	t3.autoNormalize = false;
	t4.autoNormalize = false;
	t3.transpose(t1);
	t4.transpose(t2);
	mul(t3, t4);
	FreeListManager.freeObject(FreeListManager.TRANSFORM3D, t3);
	FreeListManager.freeObject(FreeListManager.TRANSFORM3D, t4);
    }


    /**
     * Normalizes the rotational components (upper 3x3) of this matrix
     * in place using a Singular Value Decomposition (SVD).
     * This operation ensures that the column vectors of this matrix
     * are orthogonal to each other.  The primary use of this method
     * is to correct for floating point errors that accumulate over
     * time when concatenating a large number of rotation matrices.
     * Note that the scale of the matrix is not altered by this method.
     */
    public final void normalize() {

	if ((dirtyBits & (ROTATION_BIT|SVD_BIT)) != 0) {
	    computeScaleRotation(true);
	} else 	if ((dirtyBits & (SCALE_BIT|SVD_BIT)) != 0) {
	    computeScales(true);
	}

	mat[0] = rot[0]*scales[0];
	mat[1] = rot[1]*scales[1];
	mat[2] = rot[2]*scales[2];
	mat[4] = rot[3]*scales[0];
	mat[5] = rot[4]*scales[1];
	mat[6] = rot[5]*scales[2];
	mat[8] = rot[6]*scales[0];
	mat[9] = rot[7]*scales[1];
	mat[10] = rot[8]*scales[2];
	dirtyBits |= CLASSIFY_BIT;
	dirtyBits &= ~ORTHO_BIT;
	type |= ORTHO;
    }

    /**
     * Normalizes the rotational components (upper 3x3) of transform t1
     * using a Singular Value Decomposition (SVD), and places the result
     * into this transform.
     * This operation ensures that the column vectors of this matrix
     * are orthogonal to each other.  The primary use of this method
     * is to correct for floating point errors that accumulate over
     * time when concatenating a large number of rotation matrices.
     * Note that the scale of the matrix is not altered by this method.
     *
     * @param t1  the source transform, which is not modified
     */
    public final void normalize(Transform3D t1){
	set(t1);
	normalize();
    }

    /**
     * Normalizes the rotational components (upper 3x3) of this transform
     * in place using a Cross Product (CP) normalization.
     * This operation ensures that the column vectors of this matrix
     * are orthogonal to each other.  The primary use of this method
     * is to correct for floating point errors that accumulate over
     * time when concatenating a large number of rotation matrices.
     * Note that the scale of the matrix is not altered by this method.
     */
    public final void normalizeCP()  {
	if ((dirtyBits & SCALE_BIT) != 0) {
	    computeScales(false);
	}

	double mag = mat[0]*mat[0] + mat[4]*mat[4] +
  	             mat[8]*mat[8];

	if (mag != 0) {
	    mag = 1.0/Math.sqrt(mag);
	    mat[0] = mat[0]*mag;
	    mat[4] = mat[4]*mag;
	    mat[8] = mat[8]*mag;
	}

	mag = mat[1]*mat[1] + mat[5]*mat[5] +
	      mat[9]*mat[9];

	if (mag != 0) {
	    mag = 1.0/Math.sqrt(mag);
	    mat[1] = mat[1]*mag;
	    mat[5] = mat[5]*mag;
	    mat[9] = mat[9]*mag;
	}
	mat[2] = (mat[4]*mat[9] - mat[5]*mat[8])*scales[0];
	mat[6] = (mat[1]*mat[8] - mat[0]*mat[9])*scales[1];
	mat[10] = (mat[0]*mat[5] - mat[1]*mat[4])*scales[2];

	mat[0] *= scales[0];
	mat[1] *= scales[0];
	mat[4] *= scales[1];
	mat[5] *= scales[1];
	mat[8] *= scales[2];
	mat[9] *= scales[2];

	// leave the AFFINE bit
	dirtyBits |= CONGRUENT_BIT | RIGID_BIT | CLASSIFY_BIT | ROTATION_BIT | SVD_BIT;
	dirtyBits &= ~ORTHO_BIT;
	type |= ORTHO;
    }


    /**
     * Normalizes the rotational components (upper 3x3) of transform t1
     * using a Cross Product (CP) normalization, and
     * places the result into this transform.
     * This operation ensures that the column vectors of this matrix
     * are orthogonal to each other.  The primary use of this method
     * is to correct for floating point errors that accumulate over
     * time when concatenating a large number of rotation matrices.
     * Note that the scale of the matrix is not altered by this method.
     *
     * @param t1 the transform to be normalized
     */
    public final void normalizeCP(Transform3D t1) {
	set(t1);
	normalizeCP();
    }


    /**
     * Returns true if all of the data members of transform t1 are
     * equal to the corresponding data members in this Transform3D.
     * @param t1  the transform with which the comparison is made
     * @return  true or false
     */
    public boolean equals(Transform3D t1) {
	return (t1 != null) &&
	       (mat[0] == t1.mat[0]) && (mat[1] == t1.mat[1]) &&
	       (mat[2] == t1.mat[2]) && (mat[3] == t1.mat[3]) &&
	       (mat[4] == t1.mat[4]) && (mat[5] == t1.mat[5]) &&
	       (mat[6] == t1.mat[6]) && (mat[7] == t1.mat[7]) &&
	       (mat[8] == t1.mat[8]) && (mat[9] == t1.mat[9]) &&
	       (mat[10] == t1.mat[10]) && (mat[11] == t1.mat[11]) &&
	       (mat[12] == t1.mat[12]) && (mat[13] == t1.mat[13]) &&
	       (mat[14] == t1.mat[14]) && ( mat[15] == t1.mat[15]);
    }


   /**
     * Returns true if the Object o1 is of type Transform3D and all of the
     * data members of o1 are equal to the corresponding data members in
     * this Transform3D.
     * @param o1  the object with which the comparison is made.
     * @return  true or false
     */
    public boolean equals(Object o1) {
	return (o1 instanceof Transform3D) && equals((Transform3D) o1);
    }


    /**
     * Returns true if the L-infinite distance between this matrix
     * and matrix m1 is less than or equal to the epsilon parameter,
     * otherwise returns false.  The L-infinite
     * distance is equal to
     * MAX[i=0,1,2,3 ; j=0,1,2,3 ; abs[(this.m(i,j) - m1.m(i,j)]
     * @param t1  the transform to be compared to this transform
     * @param epsilon  the threshold value
     */
    public boolean epsilonEquals(Transform3D t1, double epsilon) {
        double diff;

        for (int i=0 ; i<16 ; i++) {
	    diff = mat[i] - t1.mat[i];
	    if ((diff < 0 ? -diff : diff) > epsilon) {
		return false;
	    }
        }
        return true;
    }


    /**
     * Returns a hash code value based on the data values in this
     * object.  Two different Transform3D objects with identical data
     * values (i.e., Transform3D.equals returns true) will return the
     * same hash number.  Two Transform3D objects with different data
     * members may return the same hash value, although this is not
     * likely.
     * @return the integer hash code value
     */
    public int hashCode() {
	long bits = 1L;

	for (int i = 0; i < 16; i++) {
	    bits = 31L * bits + HashCodeUtil.doubleToLongBits(mat[i]);
	}
	return (int) (bits ^ (bits >> 32));
    }


    /**
     * Transform the vector vec using this transform and place the
     * result into vecOut.
     * @param vec  the double precision vector to be transformed
     * @param vecOut  the vector into which the transformed values are placed
     */
    public final void transform(Vector4d vec, Vector4d vecOut) {

	if (vec != vecOut) {
	    vecOut.x = (mat[0]*vec.x + mat[1]*vec.y
			+ mat[2]*vec.z + mat[3]*vec.w);
	    vecOut.y = (mat[4]*vec.x + mat[5]*vec.y
			+ mat[6]*vec.z + mat[7]*vec.w);
	    vecOut.z = (mat[8]*vec.x + mat[9]*vec.y
			+ mat[10]*vec.z + mat[11]*vec.w);
	    vecOut.w = (mat[12]*vec.x + mat[13]*vec.y
			+ mat[14]*vec.z + mat[15]*vec.w);
	} else {
	    transform(vec);
	}
    }


    /**
     * Transform the vector vec using this Transform and place the
     * result back into vec.
     * @param vec  the double precision vector to be transformed
     */
    public final void transform(Vector4d vec) {
	double x = (mat[0]*vec.x + mat[1]*vec.y
		    + mat[2]*vec.z + mat[3]*vec.w);
	double y = (mat[4]*vec.x + mat[5]*vec.y
		    + mat[6]*vec.z + mat[7]*vec.w);
	double z = (mat[8]*vec.x + mat[9]*vec.y
		    + mat[10]*vec.z + mat[11]*vec.w);
	vec.w = (mat[12]*vec.x + mat[13]*vec.y
		 + mat[14]*vec.z + mat[15]*vec.w);
	vec.x = x;
	vec.y = y;
	vec.z = z;
    }


    /**
     * Transform the vector vec using this Transform and place the
     * result into vecOut.
     * @param vec  the single precision vector to be transformed
     * @param vecOut  the vector into which the transformed values are placed
     */
    public final void transform(Vector4f vec, Vector4f vecOut)  {
	if (vecOut != vec) {
	    vecOut.x = (float) (mat[0]*vec.x + mat[1]*vec.y
				+ mat[2]*vec.z + mat[3]*vec.w);
	    vecOut.y = (float) (mat[4]*vec.x + mat[5]*vec.y
				+ mat[6]*vec.z + mat[7]*vec.w);
	    vecOut.z = (float) (mat[8]*vec.x + mat[9]*vec.y
				+ mat[10]*vec.z + mat[11]*vec.w);
	    vecOut.w = (float) (mat[12]*vec.x + mat[13]*vec.y
				+ mat[14]*vec.z + mat[15]*vec.w);
	} else {
	    transform(vec);
	}
    }


    /**
     * Transform the vector vec using this Transform and place the
     * result back into vec.
     * @param vec  the single precision vector to be transformed
     */
    public final void transform(Vector4f vec) {
	float x = (float) (mat[0]*vec.x + mat[1]*vec.y
			   + mat[2]*vec.z + mat[3]*vec.w);
        float  y = (float) (mat[4]*vec.x + mat[5]*vec.y
			    + mat[6]*vec.z + mat[7]*vec.w);
	float z = (float) (mat[8]*vec.x + mat[9]*vec.y
			   + mat[10]*vec.z + mat[11]*vec.w);
	vec.w = (float) (mat[12]*vec.x + mat[13]*vec.y
			 + mat[14]*vec.z + mat[15]*vec.w);
	vec.x = x;
	vec.y = y;
	vec.z = z;
    }


    /**
     * Transforms the point parameter with this transform and
     * places the result into pointOut.  The fourth element of the
     * point input paramter is assumed to be one.
     * @param point  the input point to be transformed
     * @param pointOut  the transformed point
     */
    public final void transform(Point3d point, Point3d pointOut) {
	if (point != pointOut) {
	    pointOut.x = mat[0]*point.x + mat[1]*point.y +
		         mat[2]*point.z + mat[3];
	    pointOut.y = mat[4]*point.x + mat[5]*point.y +
		         mat[6]*point.z + mat[7];
	    pointOut.z = mat[8]*point.x + mat[9]*point.y +
		         mat[10]*point.z + mat[11];
	} else {
	    transform(point);
	}
    }


    /**
     * Transforms the point parameter with this transform and
     * places the result back into point.  The fourth element of the
     * point input paramter is assumed to be one.
     * @param point  the input point to be transformed
     */
    public final void transform(Point3d point) {
        double x = mat[0]*point.x + mat[1]*point.y + mat[2]*point.z + mat[3];
        double y = mat[4]*point.x + mat[5]*point.y + mat[6]*point.z + mat[7];
        point.z =  mat[8]*point.x + mat[9]*point.y + mat[10]*point.z + mat[11];
        point.x = x;
        point.y = y;
    }


    /**
     * Transforms the normal parameter by this transform and places the value
     * into normalOut.  The fourth element of the normal is assumed to be zero.
     * @param normal   the input normal to be transformed
     * @param normalOut  the transformed normal
     */
    public final void transform(Vector3d normal, Vector3d normalOut) {
	if (normalOut != normal) {
	    normalOut.x =  mat[0]*normal.x + mat[1]*normal.y + mat[2]*normal.z;
	    normalOut.y =  mat[4]*normal.x + mat[5]*normal.y + mat[6]*normal.z;
	    normalOut.z =  mat[8]*normal.x + mat[9]*normal.y + mat[10]*normal.z;
	} else {
	    transform(normal);
	}
    }


    /**
     * Transforms the normal parameter by this transform and places the value
     * back into normal.  The fourth element of the normal is assumed to be zero.
     * @param normal   the input normal to be transformed
     */
    public final void transform(Vector3d normal) {
        double x =  mat[0]*normal.x + mat[1]*normal.y + mat[2]*normal.z;
        double y =  mat[4]*normal.x + mat[5]*normal.y + mat[6]*normal.z;
        normal.z =  mat[8]*normal.x + mat[9]*normal.y + mat[10]*normal.z;
        normal.x = x;
        normal.y = y;
    }


    /**
     * Transforms the point parameter with this transform and
     * places the result into pointOut.  The fourth element of the
     * point input paramter is assumed to be one.
     * @param point  the input point to be transformed
     * @param pointOut  the transformed point
     */
    public final void transform(Point3f point, Point3f pointOut)  {
	if (point != pointOut) {
	    pointOut.x = (float)(mat[0]*point.x + mat[1]*point.y +
				 mat[2]*point.z + mat[3]);
	    pointOut.y = (float)(mat[4]*point.x + mat[5]*point.y +
				 mat[6]*point.z + mat[7]);
	    pointOut.z = (float)(mat[8]*point.x + mat[9]*point.y +
				 mat[10]*point.z + mat[11]);
	} else {
	    transform(point);
	}
    }


    /**
     * Transforms the point parameter with this transform and
     * places the result back into point.  The fourth element of the
     * point input paramter is assumed to be one.
     * @param point  the input point to be transformed
     */
    public final void transform(Point3f point) {
        float x = (float) (mat[0]*point.x + mat[1]*point.y +
			   mat[2]*point.z + mat[3]);
        float y = (float) (mat[4]*point.x + mat[5]*point.y +
			   mat[6]*point.z + mat[7]);
        point.z = (float) (mat[8]*point.x + mat[9]*point.y +
			   mat[10]*point.z + mat[11]);
        point.x = x;
        point.y = y;
    }


    /**
     * Transforms the normal parameter by this transform and places the value
     * into normalOut.  The fourth element of the normal is assumed to be zero.
     * Note: For correct lighting results, if a transform has uneven scaling
     * surface normals should transformed by the inverse transpose of
     * the transform. This the responsibility of the application and is not
     * done automatically by this method.
     * @param normal   the input normal to be transformed
     * @param normalOut  the transformed normal
     */
    public final void transform(Vector3f normal, Vector3f normalOut) {
	if (normal != normalOut) {
	    normalOut.x = (float) (mat[0]*normal.x + mat[1]*normal.y +
				   mat[2]*normal.z);
	    normalOut.y = (float) (mat[4]*normal.x + mat[5]*normal.y +
				   mat[6]*normal.z);
	    normalOut.z = (float) (mat[8]*normal.x + mat[9]*normal.y +
				   mat[10]*normal.z);
	} else {
	    transform(normal);
	}
    }

    /**
     * Transforms the normal parameter by this transform and places the value
     * back into normal.  The fourth element of the normal is assumed to be zero.
     * Note: For correct lighting results, if a transform has uneven scaling
     * surface normals should transformed by the inverse transpose of
     * the transform. This the responsibility of the application and is not
     * done automatically by this method.
     * @param normal   the input normal to be transformed
     */
    public final void transform(Vector3f normal) {
        float x =  (float) (mat[0]*normal.x + mat[1]*normal.y +
			    mat[2]*normal.z);
        float y =  (float) (mat[4]*normal.x + mat[5]*normal.y +
			    mat[6]*normal.z);
        normal.z =  (float) (mat[8]*normal.x + mat[9]*normal.y +
                             mat[10]*normal.z);
        normal.x = x;
        normal.y = y;
    }


    /**
     * Replaces the upper 3x3 matrix values of this transform with the
     * values in the matrix m1.
     * @param m1  the matrix that will be the new upper 3x3
     */
    public final void setRotationScale(Matrix3f m1) {
	mat[0] = m1.m00; mat[1] = m1.m01; mat[2] = m1.m02;
	mat[4] = m1.m10; mat[5] = m1.m11; mat[6] = m1.m12;
	mat[8] = m1.m20; mat[9] = m1.m21; mat[10] = m1.m22;

	// keep affine bit
	dirtyBits |= (RIGID_BIT | CONGRUENT_BIT | ORTHO_BIT |
		      CLASSIFY_BIT | ROTSCALESVD_DIRTY);
	if (autoNormalize) {
	    normalize();
	}
    }


    /**
     * Replaces the upper 3x3 matrix values of this transform with the
     * values in the matrix m1.
     * @param m1  the matrix that will be the new upper 3x3
     */
    public final void setRotationScale(Matrix3d m1)  {
	mat[0] = m1.m00; mat[1] = m1.m01; mat[2] = m1.m02;
	mat[4] = m1.m10; mat[5] = m1.m11; mat[6] = m1.m12;
	mat[8] = m1.m20; mat[9] = m1.m21; mat[10] = m1.m22;

	dirtyBits |= (RIGID_BIT | CONGRUENT_BIT | ORTHO_BIT |
		      CLASSIFY_BIT | ROTSCALESVD_DIRTY);
	if (autoNormalize) {
	    normalize();
	}
    }

    /**
     *  Scales transform t1 by a Uniform scale matrix with scale
     *  factor s and then adds transform t2 (this = S*t1 + t2).
     *  @param s  the scale factor
     *  @param t1 the transform to be scaled
     *  @param t2 the transform to be added
     */
    public final void scaleAdd(double s, Transform3D t1, Transform3D t2) {
	for (int i=0 ; i<16 ; i++) {
	   mat[i] = s*t1.mat[i] + t2.mat[i];
	}

	dirtyBits = CLASSIFY_ALL_DIRTY | ROTSCALESVD_DIRTY;

	if (autoNormalize) {
	    normalize();
	}
    }


    /**
     *  Scales this transform by a Uniform scale matrix with scale factor
     *  s and then adds transform t1 (this = S*this + t1).
     *  @param s  the scale factor
     *  @param t1 the transform to be added
     */
    public final void scaleAdd(double s, Transform3D t1) {
	for (int i=0 ; i<16 ; i++) {
	    mat[i] = s*mat[i] + t1.mat[i];
	}

	dirtyBits = CLASSIFY_ALL_DIRTY | ROTSCALESVD_DIRTY;

	if (autoNormalize) {
	    normalize();
	}
   }


    /**
     * Gets the upper 3x3 values of this matrix and places them into
     * the matrix m1.
     * @param m1  the matrix that will hold the values
     */
    public final void getRotationScale(Matrix3f m1) {
	m1.m00 = (float) mat[0];
	m1.m01 = (float) mat[1];
	m1.m02 = (float) mat[2];
	m1.m10 = (float) mat[4];
	m1.m11 = (float) mat[5];
	m1.m12 = (float) mat[6];
	m1.m20 = (float) mat[8];
	m1.m21 = (float) mat[9];
	m1.m22 = (float) mat[10];
    }


    /**
     * Gets the upper 3x3 values of this matrix and places them into
     * the matrix m1.
     * @param m1  the matrix that will hold the values
     */
    public final void getRotationScale(Matrix3d m1) {
	m1.m00 = mat[0];
	m1.m01 = mat[1];
	m1.m02 = mat[2];
	m1.m10 = mat[4];
	m1.m11 = mat[5];
	m1.m12 = mat[6];
	m1.m20 = mat[8];
	m1.m21 = mat[9];
	m1.m22 = mat[10];
    }


    /**
     * Helping function that specifies the position and orientation of a
     * view matrix. The inverse of this transform can be used to control
     * the ViewPlatform object within the scene graph.
     * @param eye the location of the eye
     * @param center a point in the virtual world where the eye is looking
     * @param up an up vector specifying the frustum's up direction
     */
    public void lookAt(Point3d eye, Point3d center, Vector3d up) {
        double forwardx,forwardy,forwardz,invMag;
        double upx,upy,upz;
        double sidex,sidey,sidez;

        forwardx =  eye.x - center.x;
        forwardy =  eye.y - center.y;
        forwardz =  eye.z - center.z;

        invMag = 1.0/Math.sqrt( forwardx*forwardx + forwardy*forwardy + forwardz*forwardz);
        forwardx = forwardx*invMag;
        forwardy = forwardy*invMag;
        forwardz = forwardz*invMag;


        invMag = 1.0/Math.sqrt( up.x*up.x + up.y*up.y + up.z*up.z);
        upx = up.x*invMag;
        upy = up.y*invMag;
        upz = up.z*invMag;

	// side = Up cross forward
	sidex = upy*forwardz-forwardy*upz;
	sidey = upz*forwardx-upx*forwardz;
	sidez = upx*forwardy-upy*forwardx;

	invMag = 1.0/Math.sqrt( sidex*sidex + sidey*sidey + sidez*sidez);
	sidex *= invMag;
	sidey *= invMag;
	sidez *= invMag;

	// recompute up = forward cross side

	upx = forwardy*sidez-sidey*forwardz;
	upy = forwardz*sidex-forwardx*sidez;
	upz = forwardx*sidey-forwardy*sidex;

	// transpose because we calculated the inverse of what we want
        mat[0] = sidex;
        mat[1] = sidey;
        mat[2] = sidez;

	mat[4] = upx;
	mat[5] = upy;
	mat[6] = upz;

	mat[8] =  forwardx;
	mat[9] =  forwardy;
	mat[10] = forwardz;

        mat[3] = -eye.x*mat[0] + -eye.y*mat[1] + -eye.z*mat[2];
        mat[7] = -eye.x*mat[4] + -eye.y*mat[5] + -eye.z*mat[6];
        mat[11] = -eye.x*mat[8] + -eye.y*mat[9] + -eye.z*mat[10];

	mat[12] = mat[13] = mat[14] = 0;
	mat[15] = 1;

	type = AFFINE | CONGRUENT | RIGID | ORTHO;
	dirtyBits = CLASSIFY_BIT | ROTSCALESVD_DIRTY;
    }


    /**
     * Creates a perspective projection transform that mimics a standard,
     * camera-based,
     * view-model.  This transform maps coordinates from Eye Coordinates (EC)
     * to Clipping Coordinates (CC).  Note that unlike the similar function
     * in OpenGL, the clipping coordinates generated by the resulting
     * transform are in a right-handed coordinate system
     * (as are all other coordinate systems in Java 3D).
     * <p>
     * The frustum function-call establishes a view model with the eye
     * at the apex of a symmetric view frustum. The arguments
     * define the frustum and its associated perspective projection:
     * (left, bottom, -near) and (right, top, -near) specify the
     * point on the near clipping plane that maps onto the
     * lower-left and upper-right corners of the window respectively,
     * assuming the eye is located at (0, 0, 0).
     * @param left the vertical line on the left edge of the near
     * clipping plane mapped to the left edge of the graphics window
     * @param right the vertical line on the right edge of the near
     * clipping plane mapped to the right edge of the graphics window
     * @param bottom the horizontal line on the bottom edge of the near
     * clipping plane mapped to the bottom edge of the graphics window
     * @param top the horizontal line on the top edge of the near
     * @param near the distance to the frustum's near clipping plane.
     * This value must be positive, (the value -near is the location of the
     * near clip plane).
     * @param far the distance to the frustum's far clipping plane.
     * This value must be positive, and must be greater than near.
     */
    public void frustum(double left, double right,
			double bottom, double top,
			double near, double far) {
	double dx = 1/(right - left);
	double dy = 1/(top - bottom);
	double dz = 1/(far - near);

	mat[0] = (2.0*near)*dx;
	mat[5] = (2.0*near)*dy;
	mat[10] = (far+near)*dz;
	mat[2] = (right+left)*dx;
	mat[6] = (top+bottom)*dy;
	mat[11] = (2.0*far*near)*dz;
	mat[14] = -1.0;
	mat[1] = mat[3] = mat[4] = mat[7] = mat[8] = mat[9] = mat[12]
	    = mat[13] = mat[15] = 0;

	// Matrix is a projection transform
	type = 0;
	dirtyBits = CLASSIFY_BIT | ROTSCALESVD_DIRTY;
    }


    /**
     * Creates a perspective projection transform that mimics a standard,
     * camera-based,
     * view-model.  This transform maps coordinates from Eye Coordinates (EC)
     * to Clipping Coordinates (CC).  Note that unlike the similar function
     * in OpenGL, the clipping coordinates generated by the resulting
     * transform are in a right-handed coordinate system
     * (as are all other coordinate systems in Java 3D). Also note that the
     * field of view is specified in radians.
     * @param fovx specifies the field of view in the x direction, in radians
     * @param aspect specifies the aspect ratio and thus the field of
     * view in the x direction. The aspect ratio is the ratio of x to y,
     * or width to height.
     * @param zNear the distance to the frustum's near clipping plane.
     * This value must be positive, (the value -zNear is the location of the
     * near clip plane).
     * @param zFar the distance to the frustum's far clipping plane
     */
    public void perspective(double fovx, double aspect,
			    double zNear, double zFar) {
	double sine, cotangent, deltaZ;
	double half_fov = fovx * 0.5;
	double x, y;
	Vector3d v1, v2, v3, v4;
	Vector3d norm = new Vector3d();

	deltaZ = zFar - zNear;
	sine = Math.sin(half_fov);
//	if ((deltaZ == 0.0) || (sine == 0.0) || (aspect == 0.0)) {
//	    return;
//	}
	cotangent = Math.cos(half_fov) / sine;

	mat[0] = cotangent;
	mat[5] = cotangent * aspect;
	mat[10] = (zFar + zNear) / deltaZ;
	mat[11] = 2.0 * zNear * zFar / deltaZ;
	mat[14] = -1.0;
	mat[1] = mat[2] = mat[3] = mat[4] = mat[6] = mat[7] = mat[8] =
	    mat[9] = mat[12] = mat[13] = mat[15] = 0;

	// Matrix is a projection transform
	type = 0;
	dirtyBits = CLASSIFY_BIT | ROTSCALESVD_DIRTY;
    }


    /**
     * Creates an orthographic projection transform that mimics a standard,
     * camera-based,
     * view-model.  This transform maps coordinates from Eye Coordinates (EC)
     * to Clipping Coordinates (CC).  Note that unlike the similar function
     * in OpenGL, the clipping coordinates generated by the resulting
     * transform are in a right-handed coordinate system
     * (as are all other coordinate systems in Java 3D).
     * @param left the vertical line on the left edge of the near
     * clipping plane mapped to the left edge of the graphics window
     * @param right the vertical line on the right edge of the near
     * clipping plane mapped to the right edge of the graphics window
     * @param bottom the horizontal line on the bottom edge of the near
     * clipping plane mapped to the bottom edge of the graphics window
     * @param top the horizontal line on the top edge of the near
     * clipping plane mapped to the top edge of the graphics window
     * @param near the distance to the frustum's near clipping plane
     * (the value -near is the location of the near clip plane)
     * @param far the distance to the frustum's far clipping plane
     */
    public void ortho(double left, double right, double bottom,
                        double top, double near, double far) {
	double deltax = 1/(right - left);
	double deltay = 1/(top - bottom);
	double deltaz = 1/(far - near);

//	if ((deltax == 0.0) || (deltay == 0.0) || (deltaz == 0.0)) {
//	    return;
//	}

	mat[0] = 2.0 * deltax;
	mat[3] = -(right + left) * deltax;
	mat[5] = 2.0 * deltay;
	mat[7] = -(top + bottom) * deltay;
	mat[10] = 2.0 * deltaz;
	mat[11] = (far + near) * deltaz;
	mat[1] = mat[2] =  mat[4] = mat[6] = mat[8] =
	    mat[9] = mat[12] = mat[13] = mat[14] = 0;
	    mat[15] = 1;
	// Matrix is a projection transform
	type = AFFINE;
	dirtyBits = CLASSIFY_BIT | ROTSCALESVD_DIRTY | CONGRUENT_BIT |
	    RIGID_BIT | ORTHO_BIT;
    }

    /**
     * get the scaling factor of matrix in this transform,
     * use for distance scaling
     */
    double getDistanceScale() {
	// The caller know that this matrix is affine
	// orthogonal before invoke this procedure

       if ((dirtyBits & SCALE_BIT) != 0) {
	   double max = mat[0]*mat[0] + mat[4]*mat[4] +
		        mat[8]*mat[8];
	   if (((dirtyBits & CONGRUENT_BIT) == 0) &&
	       ((type & CONGRUENT) != 0)) {
	       // in most case it is congruent
	       return Math.sqrt(max);
	   }
	   double tmp =  mat[1]*mat[1] + mat[5]*mat[5] +
		         mat[9]*mat[9];
	   if (tmp > max) {
	       max = tmp;
	   }
	   tmp = mat[2]*mat[2] + mat[6]*mat[6] + mat[10]*mat[10];
	   return Math.sqrt((tmp > max) ? tmp : max);
       }
       return max3(scales);
    }


    static private void  mat_mul(double[] m1, double[] m2, double[] m3) {

	double[] result = m3;
	if ((m1 == m3) || (m2 == m3)) {
	    result = new double[9];
	}

	result[0] =  m1[0]*m2[0] + m1[1]*m2[3] + m1[2]*m2[6];
	result[1] =  m1[0]*m2[1] + m1[1]*m2[4] + m1[2]*m2[7];
	result[2] =  m1[0]*m2[2] + m1[1]*m2[5] + m1[2]*m2[8];

	result[3] =  m1[3]*m2[0] + m1[4]*m2[3] + m1[5]*m2[6];
	result[4] =  m1[3]*m2[1] + m1[4]*m2[4] + m1[5]*m2[7];
	result[5] =  m1[3]*m2[2] + m1[4]*m2[5] + m1[5]*m2[8];

	result[6] =  m1[6]*m2[0] + m1[7]*m2[3] + m1[8]*m2[6];
	result[7] =  m1[6]*m2[1] + m1[7]*m2[4] + m1[8]*m2[7];
	result[8] =  m1[6]*m2[2] + m1[7]*m2[5] + m1[8]*m2[8];

	if (result != m3) {
	    for(int i=0;i<9;i++) {
		m3[i] = result[i];
	    }
	}
    }

    static private void  transpose_mat(double[] in, double[] out) {
	out[0] = in[0];
	out[1] = in[3];
	out[2] = in[6];

	out[3] = in[1];
	out[4] = in[4];
	out[5] = in[7];

	out[6] = in[2];
	out[7] = in[5];
	out[8] = in[8];
    }


    final static private void multipleScale(double m[] , double s[]) {
	m[0]  *= s[0];
	m[1]  *= s[0];
	m[2]  *= s[0];
	m[4]  *= s[1];
	m[5]  *= s[1];
	m[6]  *= s[1];
	m[8]  *= s[2];
	m[9]  *= s[2];
	m[10] *= s[2];
    }

    private void compute_svd(Transform3D matrix, double[] outScale,
			     double[] outRot) {

	int i,j;
	double g,scale;
	double m[] = new double[9];

	// if (!svdAllocd) {
	double[] u1 = new double[9];
	double[] v1 = new double[9];
	double[] t1 = new double[9];
	double[] t2 = new double[9];
	// double[] ts = new double[9];
	// double[] svdTmp = new double[9]; It is replaced by t1
	double[] svdRot = new double[9];
	// double[] single_values = new double[3]; replaced by t2

	double[] e = new double[3];
	double[] svdScales = new double[3];


	// XXXX: initialize to 0's if alread allocd? Should not have to, since
	// no operations depend on these being init'd to zero.

	int converged, negCnt=0;
	double cs,sn;
	double c1,c2,c3,c4;
	double s1,s2,s3,s4;
	double cl1,cl2,cl3;


        svdRot[0] = m[0] = matrix.mat[0];
	svdRot[1] = m[1] = matrix.mat[1];
	svdRot[2] = m[2] = matrix.mat[2];
	svdRot[3] = m[3] = matrix.mat[4];
	svdRot[4] = m[4] = matrix.mat[5];
	svdRot[5] = m[5] = matrix.mat[6];
	svdRot[6] = m[6] = matrix.mat[8];
	svdRot[7] = m[7] = matrix.mat[9];
	svdRot[8] = m[8] = matrix.mat[10];

	// u1

	if( m[3]*m[3] < EPS ) {
	    u1[0] = 1.0; u1[1] = 0.0; u1[2] = 0.0;
	    u1[3] = 0.0; u1[4] = 1.0; u1[5] = 0.0;
	    u1[6] = 0.0; u1[7] = 0.0; u1[8] = 1.0;
	} else if( m[0]*m[0] < EPS ) {
	    t1[0] = m[0];
	    t1[1] = m[1];
	    t1[2] = m[2];
	    m[0] = m[3];
	    m[1] = m[4];
	    m[2] = m[5];

	    m[3] = -t1[0]; // zero
	    m[4] = -t1[1];
	    m[5] = -t1[2];

	    u1[0] =  0.0; u1[1] = 1.0;  u1[2] = 0.0;
	    u1[3] = -1.0; u1[4] = 0.0;  u1[5] = 0.0;
	    u1[6] =  0.0; u1[7] = 0.0;  u1[8] = 1.0;
	} else {
	    g = 1.0/Math.sqrt(m[0]*m[0] + m[3]*m[3]);
	    c1 = m[0]*g;
	    s1 = m[3]*g;
	    t1[0] = c1*m[0] + s1*m[3];
	    t1[1] = c1*m[1] + s1*m[4];
	    t1[2] = c1*m[2] + s1*m[5];

	    m[3] = -s1*m[0] + c1*m[3]; // zero
	    m[4] = -s1*m[1] + c1*m[4];
	    m[5] = -s1*m[2] + c1*m[5];

	    m[0] = t1[0];
	    m[1] = t1[1];
	    m[2] = t1[2];
	    u1[0] = c1;  u1[1] = s1;  u1[2] = 0.0;
	    u1[3] = -s1; u1[4] = c1;  u1[5] = 0.0;
	    u1[6] = 0.0; u1[7] = 0.0; u1[8] = 1.0;
	}

	// u2

	if( m[6]*m[6] < EPS  ) {
	} else if( m[0]*m[0] < EPS ){
	    t1[0] = m[0];
	    t1[1] = m[1];
	    t1[2] = m[2];
	    m[0] = m[6];
	    m[1] = m[7];
	    m[2] = m[8];

	    m[6] = -t1[0]; // zero
	    m[7] = -t1[1];
	    m[8] = -t1[2];

	    t1[0] = u1[0];
	    t1[1] = u1[1];
	    t1[2] = u1[2];
	    u1[0] = u1[6];
	    u1[1] = u1[7];
	    u1[2] = u1[8];

	    u1[6] = -t1[0]; // zero
	    u1[7] = -t1[1];
	    u1[8] = -t1[2];
	} else {
	    g = 1.0/Math.sqrt(m[0]*m[0] + m[6]*m[6]);
	    c2 = m[0]*g;
	    s2 = m[6]*g;
	    t1[0] = c2*m[0] + s2*m[6];
	    t1[1] = c2*m[1] + s2*m[7];
	    t1[2] = c2*m[2] + s2*m[8];

	    m[6] = -s2*m[0] + c2*m[6];
	    m[7] = -s2*m[1] + c2*m[7];
	    m[8] = -s2*m[2] + c2*m[8];
	    m[0] = t1[0];
	    m[1] = t1[1];
	    m[2] = t1[2];

	    t1[0] = c2*u1[0];
	    t1[1] = c2*u1[1];
	    u1[2]  = s2;

	    t1[6] = -u1[0]*s2;
	    t1[7] = -u1[1]*s2;
	    u1[8] = c2;
	    u1[0] = t1[0];
	    u1[1] = t1[1];
	    u1[6] = t1[6];
	    u1[7] = t1[7];
	}

	// v1

	if( m[2]*m[2] < EPS ) {
	    v1[0] = 1.0; v1[1] = 0.0; v1[2] = 0.0;
	    v1[3] = 0.0; v1[4] = 1.0; v1[5] = 0.0;
	    v1[6] = 0.0; v1[7] = 0.0; v1[8] = 1.0;
	} else if( m[1]*m[1] < EPS ) {
	    t1[2] = m[2];
	    t1[5] = m[5];
	    t1[8] = m[8];
	    m[2] = -m[1];
	    m[5] = -m[4];
	    m[8] = -m[7];

	    m[1] = t1[2]; // zero
	    m[4] = t1[5];
	    m[7] = t1[8];

	    v1[0] =  1.0; v1[1] = 0.0;  v1[2] = 0.0;
	    v1[3] =  0.0; v1[4] = 0.0;  v1[5] =-1.0;
	    v1[6] =  0.0; v1[7] = 1.0;  v1[8] = 0.0;
	} else {
	    g = 1.0/Math.sqrt(m[1]*m[1] + m[2]*m[2]);
	    c3 = m[1]*g;
	    s3 = m[2]*g;
	    t1[1] = c3*m[1] + s3*m[2];  // can assign to m[1]?
	    m[2] =-s3*m[1] + c3*m[2];  // zero
	    m[1] = t1[1];

	    t1[4] = c3*m[4] + s3*m[5];
	    m[5] =-s3*m[4] + c3*m[5];
	    m[4] = t1[4];

	    t1[7] = c3*m[7] + s3*m[8];
	    m[8] =-s3*m[7] + c3*m[8];
	    m[7] = t1[7];

	    v1[0] = 1.0; v1[1] = 0.0; v1[2] = 0.0;
	    v1[3] = 0.0; v1[4] =  c3; v1[5] = -s3;
	    v1[6] = 0.0; v1[7] =  s3; v1[8] =  c3;
	}

	// u3

	if( m[7]*m[7] < EPS ) {
	} else if( m[4]*m[4] < EPS ) {
	    t1[3] = m[3];
	    t1[4] = m[4];
	    t1[5] = m[5];
	    m[3] = m[6];   // zero
	    m[4] = m[7];
	    m[5] = m[8];

	    m[6] = -t1[3]; // zero
	    m[7] = -t1[4]; // zero
	    m[8] = -t1[5];

	    t1[3] = u1[3];
	    t1[4] = u1[4];
	    t1[5] = u1[5];
	    u1[3] = u1[6];
	    u1[4] = u1[7];
	    u1[5] = u1[8];

	    u1[6] = -t1[3]; // zero
	    u1[7] = -t1[4];
	    u1[8] = -t1[5];

	} else {
	    g = 1.0/Math.sqrt(m[4]*m[4] + m[7]*m[7]);
	    c4 = m[4]*g;
	    s4 = m[7]*g;
	    t1[3] = c4*m[3] + s4*m[6];
	    m[6] =-s4*m[3] + c4*m[6];  // zero
	    m[3] = t1[3];

	    t1[4] = c4*m[4] + s4*m[7];
	    m[7] =-s4*m[4] + c4*m[7];
	    m[4] = t1[4];

	    t1[5] = c4*m[5] + s4*m[8];
	    m[8] =-s4*m[5] + c4*m[8];
	    m[5] = t1[5];

	    t1[3] = c4*u1[3] + s4*u1[6];
	    u1[6] =-s4*u1[3] + c4*u1[6];
	    u1[3] = t1[3];

	    t1[4] = c4*u1[4] + s4*u1[7];
	    u1[7] =-s4*u1[4] + c4*u1[7];
	    u1[4] = t1[4];

	    t1[5] = c4*u1[5] + s4*u1[8];
	    u1[8] =-s4*u1[5] + c4*u1[8];
	    u1[5] = t1[5];
	}

	t2[0] = m[0];
	t2[1] = m[4];
	t2[2] = m[8];
	e[0] = m[1];
	e[1] = m[5];

	if( e[0]*e[0]>EPS || e[1]*e[1]>EPS ) {
	    compute_qr( t2, e, u1, v1);
	}

	svdScales[0] = t2[0];
	svdScales[1] = t2[1];
	svdScales[2] = t2[2];


	// Do some optimization here. If scale is unity, simply return the rotation matric.
	if(almostOne(Math.abs(svdScales[0])) &&
	   almostOne(Math.abs(svdScales[1])) &&
	   almostOne(Math.abs(svdScales[2]))) {

	    for(i=0;i<3;i++)
		if(svdScales[i]<0.0)
		    negCnt++;

	    if((negCnt==0)||(negCnt==2)) {
		//System.out.println("Optimize!!");
		outScale[0] = outScale[1] = outScale[2] = 1.0;
		for(i=0;i<9;i++)
		    outRot[i] = svdRot[i];

		return;
	    }
	}

	// XXXX: could eliminate use of t1 and t1 by making a new method which
	// transposes and multiplies two matricies
	transpose_mat(u1, t1);
	transpose_mat(v1, t2);


	svdReorder( m, t1, t2, svdRot, svdScales, outRot, outScale);
    }


    private void svdReorder( double[] m, double[] t1, double[] t2, double[] rot,
			     double[] scales, double[] outRot, double[] outScale) {

	int in0, in1, in2, index,i;
	int[] svdOut = new int[3];
	double[] svdMag = new double[3];


	// check for rotation information in the scales
	if(scales[0] < 0.0 ) {   // move the rotation info to rotation matrix
	    scales[0] = -scales[0];
	    t2[0] = -t2[0];
	    t2[1] = -t2[1];
	    t2[2] = -t2[2];
	}
	if(scales[1] < 0.0 ) {   // move the rotation info to rotation matrix
	    scales[1] = -scales[1];
	    t2[3] = -t2[3];
	    t2[4] = -t2[4];
	    t2[5] = -t2[5];
	}
	if(scales[2] < 0.0 ) {   // move the rotation info to rotation matrix
	    scales[2] = -scales[2];
	    t2[6] = -t2[6];
	    t2[7] = -t2[7];
	    t2[8] = -t2[8];
	}


	mat_mul(t1,t2,rot);

	// check for equal scales case  and do not reorder
	if(almostEqual(Math.abs(scales[0]), Math.abs(scales[1])) &&
	   almostEqual(Math.abs(scales[1]), Math.abs(scales[2]))   ){
	    for(i=0;i<9;i++){
		outRot[i] = rot[i];
	    }
	    for(i=0;i<3;i++){
		outScale[i] = scales[i];
	    }

	}else {

	    // sort the order of the results of SVD
	    if( scales[0] > scales[1]) {
		if( scales[0] > scales[2] ) {
		    if( scales[2] > scales[1] ) {
			svdOut[0] = 0; svdOut[1] = 2; svdOut[2] = 1; // xzy
		    } else {
			svdOut[0] = 0; svdOut[1] = 1; svdOut[2] = 2; // xyz
		    }
		} else {
		    svdOut[0] = 2; svdOut[1] = 0; svdOut[2] = 1; // zxy
		}
	    } else {  // y > x
		if( scales[1] > scales[2] ) {
		    if( scales[2] > scales[0] ) {
			svdOut[0] = 1; svdOut[1] = 2; svdOut[2] = 0; // yzx
		    } else {
			svdOut[0] = 1; svdOut[1] = 0; svdOut[2] = 2; // yxz
		    }
		} else  {
		    svdOut[0] = 2; svdOut[1] = 1; svdOut[2] = 0; // zyx
		}
	    }


	    // sort the order of the input matrix
	    svdMag[0] = (m[0]*m[0] + m[1]*m[1] + m[2]*m[2]);
	    svdMag[1] = (m[3]*m[3] + m[4]*m[4] + m[5]*m[5]);
	    svdMag[2] = (m[6]*m[6] + m[7]*m[7] + m[8]*m[8]);


	    if( svdMag[0] > svdMag[1]) {
		if( svdMag[0] > svdMag[2] ) {
		    if( svdMag[2] > svdMag[1] )  {
			// 0 - 2 - 1
			in0 = 0; in2 = 1; in1 = 2;// xzy
		    } else {
			// 0 - 1 - 2
			in0 = 0; in1 = 1; in2 = 2; // xyz
		    }
		} else {
		    // 2 - 0 - 1
		    in2 = 0; in0 = 1; in1 = 2;  // zxy
		}
	    } else {  // y > x   1>0
		if( svdMag[1] > svdMag[2] ) {  // 1>2
		    if( svdMag[2] > svdMag[0] )  { // 2>0
			// 1 - 2 - 0
			in1 = 0; in2 = 1; in0 = 2; // yzx
		    } else {
			// 1 - 0 - 2
			in1 = 0; in0 = 1; in2 = 2; // yxz
		    }
		} else  {
		    // 2 - 1 - 0
		    in2 = 0; in1 = 1; in0 = 2; // zyx
		}
	    }


	    index = svdOut[in0];
	    outScale[0] = scales[index];

	    index = svdOut[in1];
	    outScale[1] = scales[index];

	    index = svdOut[in2];
	    outScale[2] = scales[index];

	    index = svdOut[in0];
	    if (outRot == null)
		System.out.println("outRot == null");
	    if ( rot == null)
		System.out.println("rot == null");
	    System.out.flush();

	    outRot[0] = rot[index];

	    index = svdOut[in0]+3;
	    outRot[0+3] = rot[index];

	    index = svdOut[in0]+6;
	    outRot[0+6] = rot[index];

	    index = svdOut[in1];
	    outRot[1] = rot[index];

	    index = svdOut[in1]+3;
	    outRot[1+3] = rot[index];

	    index = svdOut[in1]+6;
	    outRot[1+6] = rot[index];

	    index = svdOut[in2];
	    outRot[2] = rot[index];

	    index = svdOut[in2]+3;
	    outRot[2+3] = rot[index];

	    index = svdOut[in2]+6;
	    outRot[2+6] = rot[index];
	}

    }

    private int compute_qr( double[] s, double[] e, double[] u, double[] v) {
	int i,j,k;
	boolean converged;
	double shift,ssmin,ssmax,r;

	double utemp,vtemp;
	double f,g;

	final int MAX_INTERATIONS = 10;
	final double CONVERGE_TOL = 4.89E-15;

	double[]   cosl  = new double[2];
	double[]   cosr  = new double[2];
	double[]   sinl  = new double[2];
	double[]   sinr  = new double[2];
	double[]   qr_m  = new double[9];


	double c_b48 = 1.;
	double c_b71 = -1.;
	int first;
	converged = false;

	first = 1;

	if( Math.abs(e[1]) < CONVERGE_TOL || Math.abs(e[0]) < CONVERGE_TOL) converged = true;

	for(k=0;k<MAX_INTERATIONS && !converged;k++) {
	    shift = compute_shift( s[1], e[1], s[2]);
	    f = (Math.abs(s[0]) - shift) * (d_sign(c_b48, s[0]) + shift/s[0]);
	    g = e[0];
	    r = compute_rot(f, g, sinr, cosr,  0, first);
	    f = cosr[0] * s[0] + sinr[0] * e[0];
	    e[0] = cosr[0] * e[0] - sinr[0] * s[0];
	    g = sinr[0] * s[1];
	    s[1] = cosr[0] * s[1];

	    r = compute_rot(f, g, sinl, cosl, 0, first);
	    first = 0;
	    s[0] = r;
	    f = cosl[0] * e[0] + sinl[0] * s[1];
	    s[1] = cosl[0] * s[1] - sinl[0] * e[0];
	    g = sinl[0] * e[1];
	    e[1] =  cosl[0] * e[1];

	    r = compute_rot(f, g, sinr, cosr, 1, first);
	    e[0] = r;
	    f = cosr[1] * s[1] + sinr[1] * e[1];
	    e[1] = cosr[1] * e[1] - sinr[1] * s[1];
	    g = sinr[1] * s[2];
	    s[2] = cosr[1] * s[2];

	    r = compute_rot(f, g, sinl, cosl, 1, first);
	    s[1] = r;
	    f = cosl[1] * e[1] + sinl[1] * s[2];
	    s[2] = cosl[1] * s[2] - sinl[1] * e[1];
	    e[1] = f;

	    // update u  matrices
	    utemp = u[0];
	    u[0] = cosl[0]*utemp + sinl[0]*u[3];
	    u[3] = -sinl[0]*utemp + cosl[0]*u[3];
	    utemp = u[1];
	    u[1] = cosl[0]*utemp + sinl[0]*u[4];
	    u[4] = -sinl[0]*utemp + cosl[0]*u[4];
	    utemp = u[2];
	    u[2] = cosl[0]*utemp + sinl[0]*u[5];
	    u[5] = -sinl[0]*utemp + cosl[0]*u[5];

	    utemp = u[3];
	    u[3] = cosl[1]*utemp + sinl[1]*u[6];
	    u[6] = -sinl[1]*utemp + cosl[1]*u[6];
	    utemp = u[4];
	    u[4] = cosl[1]*utemp + sinl[1]*u[7];
	    u[7] = -sinl[1]*utemp + cosl[1]*u[7];
	    utemp = u[5];
	    u[5] = cosl[1]*utemp + sinl[1]*u[8];
	    u[8] = -sinl[1]*utemp + cosl[1]*u[8];

	    // update v  matrices

	    vtemp = v[0];
	    v[0] = cosr[0]*vtemp + sinr[0]*v[1];
	    v[1] = -sinr[0]*vtemp + cosr[0]*v[1];
	    vtemp = v[3];
	    v[3] = cosr[0]*vtemp + sinr[0]*v[4];
	    v[4] = -sinr[0]*vtemp + cosr[0]*v[4];
	    vtemp = v[6];
	    v[6] = cosr[0]*vtemp + sinr[0]*v[7];
	    v[7] = -sinr[0]*vtemp + cosr[0]*v[7];

	    vtemp = v[1];
	    v[1] = cosr[1]*vtemp + sinr[1]*v[2];
	    v[2] = -sinr[1]*vtemp + cosr[1]*v[2];
	    vtemp = v[4];
	    v[4] = cosr[1]*vtemp + sinr[1]*v[5];
	    v[5] = -sinr[1]*vtemp + cosr[1]*v[5];
	    vtemp = v[7];
	    v[7] = cosr[1]*vtemp + sinr[1]*v[8];
	    v[8] = -sinr[1]*vtemp + cosr[1]*v[8];

	    // if(debug)System.out.println("\n*********************** iteration #"+k+" ***********************\n");

	    qr_m[0] = s[0];  qr_m[1] = e[0]; qr_m[2] = 0.0;
	    qr_m[3] =  0.0;  qr_m[4] = s[1]; qr_m[5] =e[1];
	    qr_m[6] =  0.0;  qr_m[7] =  0.0; qr_m[8] =s[2];

	    if( Math.abs(e[1]) < CONVERGE_TOL || Math.abs(e[0]) < CONVERGE_TOL) converged = true;
	}

	if( Math.abs(e[1]) < CONVERGE_TOL ) {
	    compute_2X2( s[0],e[0],s[1],s,sinl,cosl,sinr,cosr, 0);

	    utemp = u[0];
	    u[0] = cosl[0]*utemp + sinl[0]*u[3];
	    u[3] = -sinl[0]*utemp + cosl[0]*u[3];
	    utemp = u[1];
	    u[1] = cosl[0]*utemp + sinl[0]*u[4];
	    u[4] = -sinl[0]*utemp + cosl[0]*u[4];
	    utemp = u[2];
	    u[2] = cosl[0]*utemp + sinl[0]*u[5];
	    u[5] = -sinl[0]*utemp + cosl[0]*u[5];

	    // update v  matrices

	    vtemp = v[0];
	    v[0] = cosr[0]*vtemp + sinr[0]*v[1];
	    v[1] = -sinr[0]*vtemp + cosr[0]*v[1];
	    vtemp = v[3];
	    v[3] = cosr[0]*vtemp + sinr[0]*v[4];
	    v[4] = -sinr[0]*vtemp + cosr[0]*v[4];
	    vtemp = v[6];
	    v[6] = cosr[0]*vtemp + sinr[0]*v[7];
	    v[7] = -sinr[0]*vtemp + cosr[0]*v[7];
	} else {
	    compute_2X2( s[1],e[1],s[2],s,sinl,cosl,sinr,cosr,1);

	    utemp = u[3];
	    u[3] = cosl[0]*utemp + sinl[0]*u[6];
	    u[6] = -sinl[0]*utemp + cosl[0]*u[6];
	    utemp = u[4];
	    u[4] = cosl[0]*utemp + sinl[0]*u[7];
	    u[7] = -sinl[0]*utemp + cosl[0]*u[7];
	    utemp = u[5];
	    u[5] = cosl[0]*utemp + sinl[0]*u[8];
	    u[8] = -sinl[0]*utemp + cosl[0]*u[8];

	    // update v  matrices

	    vtemp = v[1];
	    v[1] = cosr[0]*vtemp + sinr[0]*v[2];
	    v[2] = -sinr[0]*vtemp + cosr[0]*v[2];
	    vtemp = v[4];
	    v[4] = cosr[0]*vtemp + sinr[0]*v[5];
	    v[5] = -sinr[0]*vtemp + cosr[0]*v[5];
	    vtemp = v[7];
	    v[7] = cosr[0]*vtemp + sinr[0]*v[8];
	    v[8] = -sinr[0]*vtemp + cosr[0]*v[8];
	}

	return(0);
    }

    static final double max( double a, double b) {
	return ( a > b ? a : b);
    }

    static final double min( double a, double b) {
	return ( a < b ? a : b);
    }

    static final double d_sign(double a, double b) {
        double x =  (a >= 0 ? a : - a);
	return( b >= 0 ? x : -x);
    }

    static final double compute_shift( double f, double g, double h) {
	double d__1, d__2;
	double fhmn, fhmx, c, fa, ga, ha, as, at, au;
	double ssmin;

	fa = Math.abs(f);
	ga = Math.abs(g);
	ha = Math.abs(h);
	fhmn = min(fa,ha);
	fhmx = max(fa,ha);
	if (fhmn == 0.) {
	    ssmin = 0.;
	    if (fhmx == 0.) {
	    } else {
		d__1 = min(fhmx,ga) / max(fhmx,ga);
	    }
	} else {
	    if (ga < fhmx) {
		as = fhmn / fhmx + 1.;
		at = (fhmx - fhmn) / fhmx;
		d__1 = ga / fhmx;
		au = d__1 * d__1;
		c = 2. / (Math.sqrt(as * as + au) + Math.sqrt(at * at + au));
		ssmin = fhmn * c;
	    } else {
		au = fhmx / ga;
		if (au == 0.) {


		    ssmin = fhmn * fhmx / ga;
		} else {
		    as = fhmn / fhmx + 1.;
		    at = (fhmx - fhmn) / fhmx;
		    d__1 = as * au;
		    d__2 = at * au;
		    c = 1. / (Math.sqrt(d__1 * d__1 + 1.) + Math.sqrt(d__2 * d__2 + 1.));
		    ssmin = fhmn * c * au;
		    ssmin += ssmin;
		}
	    }
	}

	return(ssmin);
    }

    static int compute_2X2( double f, double g, double h, double[] single_values,
			    double[] snl, double[] csl, double[] snr, double[] csr, int index)  {

	double c_b3 = 2.;
	double c_b4 = 1.;

	double d__1;
	int pmax;
	double temp;
	boolean swap;
	double a, d, l, m, r, s, t, tsign, fa, ga, ha;
	double ft, gt, ht, mm;
	boolean gasmal;
	double tt, clt, crt, slt, srt;
	double ssmin,ssmax;

	ssmax = single_values[0];
	ssmin = single_values[1];
	clt = 0.0;
	crt = 0.0;
	slt = 0.0;
	srt = 0.0;
	tsign = 0.0;

	ft = f;
	fa = Math.abs(ft);
	ht = h;
	ha = Math.abs(h);

	pmax = 1;
	if( ha > fa)
	    swap = true;
	else
	    swap = false;

	if (swap) {
	    pmax = 3;
	    temp = ft;
	    ft = ht;
	    ht = temp;
	    temp = fa;
	    fa = ha;
	    ha = temp;

	}
	gt = g;
	ga = Math.abs(gt);
	if (ga == 0.) {

	    single_values[1] = ha;
	    single_values[0] = fa;
	    clt = 1.;
	    crt = 1.;
	    slt = 0.;
	    srt = 0.;
	} else {
	    gasmal = true;

	    if (ga > fa) {
		pmax = 2;
		if (fa / ga < EPS) {

		    gasmal = false;
		    ssmax = ga;
		    if (ha > 1.) {
			ssmin = fa / (ga / ha);
		    } else {
			ssmin = fa / ga * ha;
		    }
		    clt = 1.;
		    slt = ht / gt;
		    srt = 1.;
		    crt = ft / gt;
		}
	    }
	    if (gasmal) {

		d = fa - ha;
		if (d == fa) {

		    l = 1.;
		} else {
		    l = d / fa;
		}

		m = gt / ft;

		t = 2. - l;

		mm = m * m;
		tt = t * t;
		s = Math.sqrt(tt + mm);

		if (l == 0.) {
		    r = Math.abs(m);
		} else {
		    r = Math.sqrt(l * l + mm);
		}

		a = (s + r) * .5;

		if (ga > fa) {
		    pmax = 2;
		    if (fa / ga < EPS) {

			gasmal = false;
			ssmax = ga;
			if (ha > 1.) {
			    ssmin = fa / (ga / ha);
			} else {
			    ssmin = fa / ga * ha;
			}
			clt = 1.;
			slt = ht / gt;
			srt = 1.;
			crt = ft / gt;
		    }
		}
		if (gasmal) {

		    d = fa - ha;
		    if (d == fa) {

			l = 1.;
		    } else {
			l = d / fa;
		    }

		    m = gt / ft;

		    t = 2. - l;

		    mm = m * m;
		    tt = t * t;
		    s = Math.sqrt(tt + mm);

		    if (l == 0.) {
			r = Math.abs(m);
		    } else {
			r = Math.sqrt(l * l + mm);
		    }

		    a = (s + r) * .5;


		    ssmin = ha / a;
		    ssmax = fa * a;
		    if (mm == 0.) {

			if (l == 0.) {
			    t = d_sign(c_b3, ft) * d_sign(c_b4, gt);
			} else {
			    t = gt / d_sign(d, ft) + m / t;
			}
		    } else {
			t = (m / (s + t) + m / (r + l)) * (a + 1.);
		    }
		    l = Math.sqrt(t * t + 4.);
		    crt = 2. / l;
		    srt = t / l;
		    clt = (crt + srt * m) / a;
		    slt = ht / ft * srt / a;
		}
	    }
	    if (swap) {
		csl[0] = srt;
		snl[0] = crt;
		csr[0] = slt;
		snr[0] = clt;
	    } else {
		csl[0] = clt;
		snl[0] = slt;
		csr[0] = crt;
		snr[0] = srt;
	    }

	    if (pmax == 1) {
		tsign = d_sign(c_b4, csr[0]) * d_sign(c_b4, csl[0]) * d_sign(c_b4, f);
	    }
	    if (pmax == 2) {
		tsign = d_sign(c_b4, snr[0]) * d_sign(c_b4, csl[0]) * d_sign(c_b4, g);
	    }
	    if (pmax == 3) {
		tsign = d_sign(c_b4, snr[0]) * d_sign(c_b4, snl[0]) * d_sign(c_b4, h);
	    }
	    single_values[index] = d_sign(ssmax, tsign);
	    d__1 = tsign * d_sign(c_b4, f) * d_sign(c_b4, h);
	    single_values[index+1] = d_sign(ssmin, d__1);


	}
	return 0;
    }

    static  double compute_rot( double f, double g, double[] sin, double[] cos, int index, int first) {
	int i__1;
	double d__1, d__2;
	double cs,sn;
	int i;
	double scale;
	int count;
	double f1, g1;
	double r;
	final double safmn2 = 2.002083095183101E-146;
	final double safmx2 = 4.994797680505588E+145;

	if (g == 0.) {
	    cs = 1.;
	    sn = 0.;
	    r = f;
	} else if (f == 0.) {
	    cs = 0.;
	    sn = 1.;
	    r = g;
	} else {
	    f1 = f;
	    g1 = g;
	    scale = max(Math.abs(f1),Math.abs(g1));
	    if (scale >= safmx2) {
		count = 0;
		while(scale >= safmx2) {
		    ++count;
		    f1 *= safmn2;
		    g1 *= safmn2;
		    scale = max(Math.abs(f1),Math.abs(g1));
		}
		r = Math.sqrt(f1*f1 + g1*g1);
		cs = f1 / r;
		sn = g1 / r;
		i__1 = count;
		for (i = 1; i <= count; ++i) {
		    r *= safmx2;
		}
	    } else if (scale <= safmn2) {
		count = 0;
		while(scale <= safmn2) {
		    ++count;
		    f1 *= safmx2;
		    g1 *= safmx2;
		    scale = max(Math.abs(f1),Math.abs(g1));
		}
		r = Math.sqrt(f1*f1 + g1*g1);
		cs = f1 / r;
		sn = g1 / r;
		i__1 = count;
		for (i = 1; i <= count; ++i) {
		    r *= safmn2;
		}
	    } else {
		r = Math.sqrt(f1*f1 + g1*g1);
		cs = f1 / r;
		sn = g1 / r;
	    }
	    if (Math.abs(f) > Math.abs(g) && cs < 0.) {
		cs = -cs;
		sn = -sn;
		r = -r;
	    }
	}
	sin[index] = sn;
	cos[index] = cs;
	return r;

    }

    static final private double max3( double[] values) {
	if( values[0] > values[1] ) {
	    if( values[0] > values[2] )
		return(values[0]);
	    else
		return(values[2]);
	} else {
	    if( values[1] > values[2] )
		return(values[1]);
	    else
		return(values[2]);
	}
    }


    final private void computeScales(boolean forceSVD) {

	if(scales == null)
	    scales = new double[3];

	if ((!forceSVD || ((dirtyBits & SVD_BIT) == 0)) && isAffine()) {
	    if (isCongruent()) {
		if (((dirtyBits & RIGID_BIT) == 0) &&
                    ((type & RIGID) != 0)) {
		    scales[0] = scales[1] = scales[2] = 1;
		    dirtyBits &= ~SCALE_BIT;
		    return;
		}
		scales[0] = scales[1] = scales[2] =
		    Math.sqrt(mat[0]*mat[0] + mat[4]*mat[4] +
			      mat[8]*mat[8]);
		dirtyBits &= ~SCALE_BIT;
		return;
	    }
	    if (isOrtho()) {
		scales[0] = Math.sqrt(mat[0]*mat[0] + mat[4]*mat[4] +
				      mat[8]*mat[8]);
		scales[1] = Math.sqrt(mat[1]*mat[1] + mat[5]*mat[5] +
				      mat[9]*mat[9]);
		scales[2] = Math.sqrt(mat[2]*mat[2] + mat[6]*mat[6] +
				      mat[10]*mat[10]);
		dirtyBits &= ~SCALE_BIT;
		return;
	    }
	}
	// fall back to use SVD decomposition
	if (rot == null)
	    rot = new double[9];

	compute_svd(this, scales, rot);
	dirtyBits &= ~ROTSCALESVD_DIRTY;
    }

    final private void computeScaleRotation(boolean forceSVD) {

	if(rot == null)
	    rot = new double[9];

	if(scales == null)
	    scales = new double[3];

	if ((!forceSVD || ((dirtyBits & SVD_BIT) == 0)) && isAffine()) {
	    if (isCongruent()) {
		if (((dirtyBits & RIGID_BIT) == 0) &&
                    ((type & RIGID) != 0)) {
		    rot[0] = mat[0];
		    rot[1] = mat[1];
		    rot[2] = mat[2];
		    rot[3] = mat[4];
		    rot[4] = mat[5];
		    rot[5] = mat[6];
		    rot[6] = mat[8];
		    rot[7] = mat[9];
		    rot[8] = mat[10];
		    scales[0] = scales[1] = scales[2] = 1;
		    dirtyBits &= (~ROTATION_BIT | ~SCALE_BIT);
		    return;
		}
		double s = Math.sqrt(mat[0]*mat[0] + mat[4]*mat[4] + mat[8]*mat[8]);
		if (s == 0) {
		    compute_svd(this, scales, rot);
		    return;
		}
		scales[0] = scales[1] = scales[2] = s;
		s = 1/s;
		rot[0] = mat[0]*s;
		rot[1] = mat[1]*s;
		rot[2] = mat[2]*s;
		rot[3] = mat[4]*s;
		rot[4] = mat[5]*s;
		rot[5] = mat[6]*s;
		rot[6] = mat[8]*s;
		rot[7] = mat[9]*s;
		rot[8] = mat[10]*s;
		dirtyBits &= (~ROTATION_BIT | ~SCALE_BIT);
		return;
	    }
	    if (isOrtho()) {
		double s;

		scales[0] = Math.sqrt(mat[0]*mat[0] + mat[4]*mat[4] + mat[8]*mat[8]);
		scales[1] = Math.sqrt(mat[1]*mat[1] + mat[5]*mat[5] + mat[9]*mat[9]);
		scales[2] = Math.sqrt(mat[2]*mat[2] + mat[6]*mat[6] + mat[10]*mat[10]);

		if ((scales[0] == 0) || (scales[1] == 0) || (scales[2] == 0)) {
		    compute_svd(this, scales, rot);
		    return;
		}
		s = 1/scales[0];
		rot[0] = mat[0]*s;
		rot[3] = mat[4]*s;
		rot[6] = mat[8]*s;
		s = 1/scales[1];
		rot[1] = mat[1]*s;
		rot[4] = mat[5]*s;
		rot[7] = mat[9]*s;
		s = 1/scales[2];
		rot[2] = mat[2]*s;
		rot[5] = mat[6]*s;
		rot[8] = mat[10]*s;
		dirtyBits &= (~ROTATION_BIT | ~SCALE_BIT);
		return;
	    }
	}
	// fall back to use SVD decomposition
	compute_svd(this, scales, rot);
	dirtyBits &= ~ROTSCALESVD_DIRTY;
    }


    final void getRotation(Transform3D t) {
	if ((dirtyBits & ROTATION_BIT)!= 0) {
	    computeScaleRotation(false);
	}

        t.mat[3] = t.mat[7] = t.mat[11] = t.mat[12] = t.mat[13] =
	    t.mat[14] = 0;
	t.mat[15] = 1;
	t.mat[0] = rot[0];
	t.mat[1] = rot[1];
	t.mat[2] = rot[2];
	t.mat[4] = rot[3];
	t.mat[5] = rot[4];
	t.mat[6] = rot[5];
	t.mat[8] = rot[6];
	t.mat[9] = rot[7];
	t.mat[10] = rot[8];

	t.type = ORTHOGONAL | RIGID | CONGRUENT| AFFINE | ORTHO;
	if ((dirtyBits & SVD_BIT) != 0) {
	    t.dirtyBits = CLASSIFY_BIT | ROTSCALESVD_DIRTY;
	} else {
	    t.dirtyBits = CLASSIFY_BIT | ROTATION_BIT | SCALE_BIT;
	}
    }

    // somehow CanvasViewCache will directly modify mat[]
    // instead of calling ortho(). So we need to reset dirty bit
    final void setOrthoDirtyBit() {
	dirtyBits = CLASSIFY_BIT | ROTSCALESVD_DIRTY;
	type = 0;
    }
}
