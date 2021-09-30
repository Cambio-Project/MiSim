/*
 * Copyright (c) 2012, 2018, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package cambio.simulator.misc;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * This is a copy of the non-public utility class {@code java.util.stream.Collector.CollectorImpl}.
 *
 * <p>
 * Use this class to ease the initiation of Collector objects.
 */
public class CollectorImpl<T, A, R> implements Collector<T, A, R> {
    private final Supplier<A> supplier;
    private final BiConsumer<A, T> accumulator;
    private final BinaryOperator<A> combiner;
    private final Function<A, R> finisher;
    private final Set<Characteristics> characteristics;

    /**
     * This is a copy of the non-public utility class {@code java.util.stream.Collector.CollectorImpl}.
     */
    public CollectorImpl(Supplier<A> supplier,
                         BiConsumer<A, T> accumulator,
                         BinaryOperator<A> combiner,
                         Function<A, R> finisher,
                         Set<Characteristics> characteristics) {
        this.supplier = supplier;
        this.accumulator = accumulator;
        this.combiner = combiner;
        this.finisher = finisher;
        this.characteristics = characteristics;
    }

    /**
     * This is a copy of the non-public utility class {@code java.util.stream.Collector.CollectorImpl}.
     */
    @SuppressWarnings("unchecked")
    public CollectorImpl(Supplier<A> supplier,
                         BiConsumer<A, T> accumulator,
                         BinaryOperator<A> combiner,
                         Set<Characteristics> characteristics) {
        this(supplier, accumulator, combiner, a -> (R) a, characteristics);
    }

    @Override
    public BiConsumer<A, T> accumulator() {
        return accumulator;
    }

    @Override
    public Supplier<A> supplier() {
        return supplier;
    }

    @Override
    public BinaryOperator<A> combiner() {
        return combiner;
    }

    @Override
    public Function<A, R> finisher() {
        return finisher;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return characteristics;
    }
}
