package desmoj.core.dist;

import desmoj.core.report.DiscreteDistPoissonReporter;
import desmoj.core.simulator.Model;
import org.apache.commons.math.distribution.PoissonDistributionImpl;

/**
 * Poisson distributed stream of pseudo random integer numbers. The distribution specified by one parameter describing
 * the mean value.
 *
 * @author Tim Lechler
 * @author Ruth Meyer
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public class DiscreteDistPoisson extends DiscreteDist<Long> {

    /**
     * The mean value of the poisson distribution as given to the constructor.
     */
    protected double mean;

    /**
     * Creates a poisson distributed stream of pseudo random integer numbers. The parameter describes the mean value.
     * Giving zero or negative values for the mean value will result in zero being returned by the
     * <code>sample()</code> method.
     *
     * @param owner        Model : The distribution's owner
     * @param name         java.lang.String : The distribution's name
     * @param meanValue    double : The mean <code>double</code> value for this distribution
     * @param showInReport boolean : Flag for producing reports
     * @param showInTrace  boolean : Flag for producing trace output
     */
    public DiscreteDistPoisson(Model owner, String name, double meanValue,
                               boolean showInReport, boolean showInTrace) {
        super(owner, name, showInReport, showInTrace);
        mean = meanValue;

        if (mean > 500) {
            sendWarning(
                "You have set up a DiscreteDistPoisson with a mean of " + meanValue + ". Samples will be inaccurate",
                "DiscreteDistPoisson : "
                    + getName()
                    + " Constructor: DiscreteDistPoisson(Model owner, String name,"
                    + " double meanValue, boolean showInReport, boolean showInTrace)",
                "We recommend this class is only used for sampling poisson distributions"
                    + " with a mean 500 or less only.",
                "Use this class for poisson distributions with a smaller mean only.");
        }
    }

    /**
     * Creates the default reporter for the PoissonDiscreteDist distribution.
     *
     * @return Reporter : The reporter for the PoissonDiscreteDist distribution
     * @see DiscreteDistPoissonReporter
     */
    public desmoj.core.report.Reporter createDefaultReporter() {

        return new DiscreteDistPoissonReporter(this);

    }

    /**
     * Returns the mean value of the poisson distribution.
     *
     * @return double : The mean value of this Poisson distribution
     */
    public double getMean() {

        return mean;

    }

    /**
     * Returns the next poisson distributed sample from this distribution.
     *
     * @return Long : The next Poisson distributed sample. This will be zero if the given mean value <= 0.
     */
    public Long sample() {

        incrementObservations();

        double l = Math.exp(-mean);
        long k = 0;
        double p = 1;

        do {
            k++;
			if (antithetic) {
				p *= randomGenerator.nextDouble();
			} else {
				p *= (1 - randomGenerator.nextDouble());
			}
        } while (p > l);

		if (this.currentlySendTraceNotes()) {
			this.traceLastSample(Long.toString(k - 1));
		}

        return k - 1;
    }

    /**
     * Abstract method to map a double <code>p</code> from 0...1 to the distribution's domain by determining the value x
     * that satisfies
     * <code>P(X &lt; x) = p</code>.
     *
     * @param p double: A value between 0 and 1
     * @return Long : The value x that satisfies <code>P(X &lt; x) = p</code>
     */
    public Long getInverseOfCumulativeProbabilityFunction(double p) {

		if (p == 1.0) {
			return Long.MAX_VALUE; // should be infinity, can't get closer
		}

        PoissonDistributionImpl poi = new PoissonDistributionImpl(this.getMean());

        Long x = 0l;
        double cummulative_prob = 0;

        do {
            cummulative_prob += poi.probability(x);
			if (cummulative_prob >= p) {
				return x;
			}
            x++;
        } while (x < 10000);

        return Long.MAX_VALUE; // wrong, just an approximation
    }
}