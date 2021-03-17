package de.rss.fachstudie.MiSim.entities;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.misc.Util;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.dist.ContDistUniform;
import desmoj.core.dist.NumericalDist;

import java.util.Objects;

public class Dependency {
    private final Operation parent;
    private final Microservice targetMicroservice;
    private final Operation targetOperation;
    private final double probability;
    private final NumericalDist<Double> custom_delay;
    private NumericalDist<Double> extra_delay;

    public Dependency(Operation parent, Operation targetOperation) {
        this(parent, targetOperation, 1);
    }

    public Dependency(Operation parent, Operation targetOperation, double probability) {
        this(parent, targetOperation, 1, null);

    }

    /**
     * @param parent
     * @param targetOperation
     * @param probability
     * @param custom_delay    delay of this dependency that overrides the default network delay, null values will be
     *                        ignored
     */
    public Dependency(Operation parent, Operation targetOperation, double probability, Double custom_delay) {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(targetOperation);
        Util.requirePercentage(probability, "Probability hast to be a value between 0 and 1 (inclusive)");
        if (custom_delay != null) {
            Util.requireNonNegative(custom_delay, "Delay has to be positive.");
        }

        this.parent = parent;
        this.targetMicroservice = targetOperation.getOwner();
        this.targetOperation = targetOperation;
        this.probability = probability;
        this.custom_delay = custom_delay == null ? null : new ContDistNormal(parent.getModel(), null, custom_delay, 0, false, false);
        this.extra_delay = new ContDistNormal(parent.getModel(), null, 0, 0, false, false);
    }

    public double getProbability() {
        return probability;
    }

    public Microservice getTargetMicroservice() {
        return targetMicroservice;
    }

    public Operation getTargetOperation() {
        return targetOperation;
    }

    public void setDelay(NumericalDist<Double> dist) {
        extra_delay = dist;
    }

    public synchronized double getNextExtraDelay() {
        double nextlatency;
        do {
            nextlatency = extra_delay.sample();
        } while (nextlatency < 0);
        return nextlatency;
    }

    public synchronized boolean hasCustomDelay() {
        return custom_delay != null;
    }

    public synchronized double getNextCustomDelay() {
        double nextlatency;
        do {
            nextlatency = custom_delay.sample();
        } while (nextlatency < 0);
        return nextlatency;
    }
}
