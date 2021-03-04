package de.rss.fachstudie.MiSim.resources;

/**
 * Data-class that contains information about a currently running (or potentially finished) process.
 * <p>
 * More specifically, it concentrates on holding information about the total work units needed and left to complete the
 * processes. Further, there are methods to manipulate the later.
 *
 * @author Lion Wagner
 */
public final class CPUProcess implements Comparable<CPUProcess> {
    private final int demandTotal;
    private int demandRemainder;

    public CPUProcess(int demand) {
        if (demand < 0) throw new IllegalArgumentException("Damand has to be greater than 0");
        this.demandTotal = demand;
        this.demandRemainder = demandTotal;
    }

    public int getDemandTotal() {
        return demandTotal;
    }

    public int getDemandRemainder() {
        return demandRemainder;
    }


    /**
     * Natural sorting should by base don left over demand.
     */
    @Override
    public int compareTo(CPUProcess o) {
        if (o == null) return -1;
        int difference = this.demandRemainder - o.demandRemainder;
        return difference;
    }

    public void reduceDemandRemainder(int amount) {
        if (this.demandRemainder < amount)
            throw new IllegalArgumentException(String.format("Cannot reduce left over demand (which is %d) by %d", demandRemainder, amount));
        this.demandRemainder -= amount;
    }
}
