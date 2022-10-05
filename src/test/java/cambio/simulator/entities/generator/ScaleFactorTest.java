package cambio.simulator.entities.generator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ScaleFactorTest {
    @Test
    public void tooEarly() {
        ScaleFactor factor = new ScaleFactor(2, 5, 10);
        assertEquals(1d, factor.getValue(4), 0.001d);
    }

    @Test
    public void tooLate() {
        ScaleFactor factor = new ScaleFactor(2, 5, 10);
        assertEquals(1d, factor.getValue(16), 0.001d);
    }

    @Test
    public void constantScale() {
        ScaleFactor factor = new ScaleFactor(2, 5, 10);
        factor.setScaleFunction(ScaleFunction.constant());
        assertEquals(2, factor.getValue(5), 0.001d);
        assertEquals(2, factor.getValue(10), 0.001d);
        assertEquals(2, factor.getValue(15), 0.001d);
    }

    @Test
    public void linearScale() {
        ScaleFactor factor = new ScaleFactor(2, 5, 10);
        factor.setScaleFunction(ScaleFunction.linear());
        assertEquals(1, factor.getValue(5), 0.001d);
        assertEquals(1.5, factor.getValue(10), 0.001d);
        assertEquals(2, factor.getValue(15), 0.001d);
    }

    @Test
    public void exponentialScale() {
        ScaleFactor factor = new ScaleFactor(2, 5, 10);
        factor.setScaleFunction(ScaleFunction.exponential());
        assertEquals(1.01, factor.getValue(5), 0.001d);
        assertEquals(1.1, factor.getValue(10), 0.001d);
        assertEquals(1.630957, factor.getValue(14), 0.001d);
        assertEquals(2, factor.getValue(15), 0.001d);
    }

    @Test
    public void linearScaleReverse() {
        ScaleFactor factor = new ScaleFactor(2, 5, 10);
        factor.setScaleFunction(ScaleFunction.revert(ScaleFunction.linear()));
        assertEquals(2, factor.getValue(5), 0.001d);
        assertEquals(1.5, factor.getValue(10), 0.001d);
        assertEquals(1, factor.getValue(15), 0.001d);
    }

    @Test
    public void linearScaleSmallerOne() {
        ScaleFactor factor = new ScaleFactor(0.4, 5, 10);
        factor.setScaleFunction(ScaleFunction.linear());
        assertEquals(1, factor.getValue(5), 0.001d);
        assertEquals(0.7, factor.getValue(10), 0.001d);
        assertEquals(0.4, factor.getValue(15), 0.001d);
    }

    @Test
    public void linearScaleStartingAtTwo() {
        ScaleFactor factor = new ScaleFactor(2, 3, 5, 10);
        factor.setScaleFunction(ScaleFunction.linear());
        assertEquals(2, factor.getValue(5), 0.001d);
        assertEquals(2.5, factor.getValue(10), 0.001d);
        assertEquals(3, factor.getValue(15), 0.001d);
    }

}
