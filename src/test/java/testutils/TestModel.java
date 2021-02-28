package testutils;

import desmoj.core.simulator.Model;

/**
 * @author Lion Wagner
 */
public class TestModel extends Model {
    private final Runnable initialSchedule;
    private final Runnable init;


    public TestModel(Model model, String s, boolean b, boolean b1, Runnable initialSchedule, Runnable init) {
        super(model, s, b, b1);
        this.initialSchedule = initialSchedule;
        this.init = init;
    }

    @Override
    public String description() {
        return "Model used for testing";
    }

    @Override
    public void doInitialSchedules() {
        initialSchedule.run();
    }

    @Override
    public void init() {
        init.run();
    }
}
