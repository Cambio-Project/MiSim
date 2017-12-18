package de.rss.fachstudie.MiSim.entities;

public class Pattern {
    private String name = "";
    private Integer[] arguments = {0};

    public Pattern() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer[] getArguments() {
        return arguments;
    }

    public void setArguments(Integer[] arguments) {
        this.arguments = arguments;
    }

    public Integer getArgument(int i) {
        return arguments[i];
    }
}
