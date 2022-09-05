package desmoj.extensions.space3D;

import org.scijava.vecmath.Matrix4d;


/**
 * The SpatialData contains the spatial and physical attributes.
 *
 * @author Fred Sun
 */
public class SpatialData {

    //The 4x4 matrix (double[4][4])which contains the spatial information.
    //The first index indicates the row. The second indicates the column.
    //	private double[][] _matrix;

    private Matrix4d _matrix;

    /**
     * Constructs a SpatialData object without specifying the data. All data are set to zero.
     */
    public SpatialData() {
        _matrix = new Matrix4d();
        _matrix.setIdentity();
    }

    /**
     * Constructs a SpatialData object.
     *
     * @param matrixData The spatial data represented with a double[16] matrix. It begins with the first element in the
     *                   first row, then the second element in the first row etc.
     */
    public SpatialData(double[] matrixData) {
        assert (matrixData.length >= 16) : "The matrix data muss have more than 16 elements.";

        _matrix = new Matrix4d(matrixData);
    }

    //	/**
    //	 * Returns the matrix in a double[16]. The first four elements are represents the first row.
    //	 * The next four the second row etc.
    //	 * @return The matrix in a double[16].
    //	 */
    //	public double[] getMatrix() {
    //		double[] matrix = {_matrix.m00,_matrix.m01,_matrix.m02,_matrix.m03,
    //				_matrix.m10,_matrix.m11,_matrix.m12,_matrix.m13,
    //				_matrix.m20,_matrix.m21,_matrix.m22,_matrix.m23,
    //				_matrix.m30,_matrix.m31,_matrix.m32,_matrix.m33,};
    //		return matrix;
    //	}

    /**
     * Returns the matrix which contains the spatial data.
     *
     * @return The Matrix4d object.
     */
    public Matrix4d getMatrix() {
        return _matrix;
    }

    /**
     * Sets the matrix.
     *
     * @param matrixData The new matrix in Matrix4d.
     */
    public void setMatrix(Matrix4d matrixData) {
        assert (matrixData != null) : "The matrix to be set can't be null.";
        _matrix = matrixData;
    }

    /**
     * Returns the x-position.
     *
     * @return The x-position
     */
    public double getPosX() {
        return _matrix.m03;
    }

    /**
     * Returns the y-position.
     *
     * @return The y-position
     */
    public double getPosY() {
        return _matrix.m13;
    }

    /**
     * Returns the z-position.
     *
     * @return The z-position
     */
    public double getPosZ() {
        return _matrix.m23;
    }

    /**
     * Resets the rotational data
     */
    public void resetRotation() {
        _matrix.setColumn(0, 1, 0, 0, 0);
        _matrix.setColumn(1, 0, 1, 0, 0);
        _matrix.setColumn(2, 0, 0, 1, 0);
    }

    //	/**
    //	 * Sets the matrix.
    //	 * @param matrixData The new data in a double array. The first four elements of the array will be the first row, the next four the second etc.
    //	 */
    //	public void setMatrix(double[] matrixData) {
    //		assert (matrixData.length>=16):"The matrixData muss have at least 16 elements.";
    //
    //		_matrix.set(matrixData);
    //	}

    /**
     * Rotates the spatial attribute counter-clockwise about the x-axis.
     *
     * @param angle The angle to rotate in radians.
     */
    public void rotX(double angle) {
        Matrix4d rotMat = new Matrix4d();
        rotMat.rotX(angle);
        _matrix.mul(rotMat);

    }

    /**
     * Rotates the spatial attribute counter-clockwise about the y-axis.
     *
     * @param angle The angle to rotate in radians.
     */
    public void rotY(double angle) {
        Matrix4d rotMat = new Matrix4d();
        rotMat.rotY(angle);
        _matrix.mul(rotMat);
    }

    /**
     * Rotates the spatial attribute counter-clockwise about the z-axis.
     *
     * @param angle The angle to rotate in radians.
     */
    public void rotZ(double angle) {
        Matrix4d rotMat = new Matrix4d();
        rotMat.rotZ(angle);
        _matrix.mul(rotMat);
    }

    /**
     * Sets a new Position.
     *
     * @param x x-position
     * @param y y-position
     * @param z z-position
     */
    public void setPosition(double x, double y, double z) {
        _matrix.m03 = x;
        _matrix.m13 = y;
        _matrix.m23 = z;
    }

}
