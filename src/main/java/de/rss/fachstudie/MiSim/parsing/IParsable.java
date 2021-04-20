package de.rss.fachstudie.MiSim.parsing;

public interface IParsable {
    /**
     * @return The parser related to this class
     */

    Class<? extends Parser<?>> getParserClass();
}
