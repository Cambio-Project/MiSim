package desmoj.extensions.space3D;

/**
 * This representation of a length in meters. Negative values are accepted.
 *
 * @author Fred Sun
 */

public class Length {

    double value;

    public Length(double value) {
        this.value = value;
    }

    /** Returns the numerical value in the current unit as a double */
    public double getValue() {
        return value;
    }

    /**
     * Adds 2 length objects. Returns a new length with value this.value + length.value. This length' value is not
     * changed.
     *
     * @param length the Length object to be added to this length
     */
    public Length add(Length length) {
        return new Length(this.value + length.getValue());
    }

    /**
     * Subtracts 2 length objects. Returns a new length with value this.value - length.value. This length' value is not
     * changed.
     *
     * @param length the Length object to be subtracted from this length
     */
    public Length subtract(Length length) {
        return new Length(this.value - length.getValue());

    }


    /**
     * Returns a string representation of the quantity with at most
     * <tt>floats</tt> decimals.
     */
    public String toString(int floats) {
        String s = Double.toString(value);
        if (value > 10E7) {
            return s + " m";
        }
        int decimalPoint = s.lastIndexOf(".");

        if (decimalPoint <= 0) {
            return s;
        } else {
            if (floats == 0) {
                return s.substring(0, decimalPoint) + " m";
            }
            if ((floats + 1) >= (s.length() - decimalPoint)) {
                return s + " m";
            } else {
                return s.substring(0, decimalPoint + floats + 1) + " m";
            }
        }
    }

    /** Returns a string representation of this length */
    public String toString() {
        return this.value + " m";
    }

    /**
     * Compares this quantity with the given object. If <code>that</code> is of type Quantity the comparison results in:
     * -1 : if this < that 0 : if this == that 1 : if this > that
     *
     * @throws ClassCastException if <code>that</code> is not a Quantity object
     */
    public int compareTo(Object that) {
        Length other = (Length) that; // throws ClassCastException
        // eventually
        double otherValue = other.getValue();
        if (this.value < otherValue) {
            return -1;
        }
        if (this.value > otherValue) {
            return 1;
        }
        return 0;
    }
}
