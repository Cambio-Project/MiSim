package cambio.simulator.parsing;

import static cambio.simulator.misc.Util.injectField;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cambio.simulator.entities.generator.LimboLoadGeneratorDescription;
import cambio.simulator.entities.generator.LoadGeneratorDescription;
import cambio.simulator.entities.generator.LoadGeneratorDescriptionExecutor;
import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.events.ChaosMonkeyEvent;
import cambio.simulator.events.DelayInjection;
import cambio.simulator.events.SummonerMonkeyEvent;
import cambio.simulator.misc.NameResolver;
import cambio.simulator.models.ArchitectureModel;
import cambio.simulator.models.MiSimModel;
import desmoj.core.simulator.TimeInstant;

/**
 * Represents an scenario description input. Can be parsed to an set, containing the experiment events such as {@link
 * ChaosMonkeyEvent}s or {@link Generator}s.
 *
 * @author Lion Wagner
 */
public class ScenarioDescription {

    private final MiSimModel model;
    private final ArchitectureModel archModel;
    public String name;
    public String description;
    public String artifact;
    public String component;
    public String stimulus;
    public String source;
    public String environment;
    public Integer duration;

    public ScenarioDescription(MiSimModel model, ArchitectureModel archModel) {
        this.model = model;
        this.archModel = archModel;
    }

    /**
     * Convert this scenario description into a set of event objects.
     *
     * @return a set of objects that describe the scenario.
     */
    public Set<Object> parse() {
        Set<Object> scheduables = new HashSet<>();

        stimulus = stimulus.replaceAll("\\s+", "");
        String[] stimuli = stimulus.split("AND");

        for (String stimulus : stimuli) {
            if (stimulus.startsWith("LOAD")) {
                parseWorkload(scheduables, stimulus);
            } else {
                parseTimedFaultload(scheduables, stimulus);
            }
        }
        return scheduables;
    }


    private void parseWorkload(Set<Object> scheduables, String stimuli) {

        String profile = stimuli.replace("LOAD", "");

        Microservice service = archModel.getMicroservices()
            .stream()
            .filter(microservice -> microservice.getName().equals(artifact))
            .findAny()
            .orElse(null);

        if (service == null) {
            throw new ParsingException(String.format("Could not find target service '%s'", artifact));
        }

        if (component.equals("ALL ENDPOINTS")) {
            for (Operation operation : service.getOperations()) {
                scheduables.add(createLimboGenerator(profile, operation));
            }
        } else {
            Operation target = NameResolver.resolveOperationName(archModel, component);
            scheduables.add(createLimboGenerator(profile, target));
        }
    }

    private LoadGeneratorDescriptionExecutor createLimboGenerator(String profileLocation, Operation operation) {
        LoadGeneratorDescription description = new LimboLoadGeneratorDescription();
        injectField("modelFile", description, new File(profileLocation));
        injectField("targetOperation", description, operation);
        description.initializeArrivalRateModel();
        return new LoadGeneratorDescriptionExecutor(model, description);
    }

    //response and response measure are ignored for now

    private void parseTimedFaultload(Set<Object> scheduables, String currentStimulus) {

        Pattern p = Pattern.compile("@([0-9]*)");
        double targetTime;

        Matcher m = p.matcher(currentStimulus);
        if (m.find()) {
            targetTime = Double.parseDouble(m.group(1));
        } else {
            throw new ParsingException("Missing Time Specification (@...)");
        }
        currentStimulus = currentStimulus.replaceFirst("@([0-9]*)", "").trim();

        Microservice service = archModel.getMicroservices()
            .stream()
            .filter(microservice -> microservice.getName().equals(artifact))
            .findAny()
            .orElse(null);

        if (service == null) {
            throw new ParsingException(String.format("Could not find target service '%s'", artifact));
        }


        if (currentStimulus.startsWith("KILL")) {

            int instances = Integer.MAX_VALUE;
            if (currentStimulus.contains(" ")) {
                instances = Integer.parseInt(currentStimulus.replace("KILL", ""));
            }

            scheduables.add(
                new ChaosMonkeyEvent(model, "Chaosmonkey", true, service, instances) {
                    {
                        setTargetTime(new TimeInstant(targetTime));
                    }
                }
            );
        } else if (currentStimulus.startsWith("RESTART")) {
            int instances = Integer.parseInt(currentStimulus.replace("RESTART", ""));


            scheduables.add(
                new SummonerMonkeyEvent(model, "Summoner", true, service, instances) {
                    {
                        setTargetTime(new TimeInstant(targetTime));
                    }
                }
            );
        } else if (currentStimulus.startsWith("DELAY")) {
            Matcher delayFinder = Pattern.compile("([0-9]*\\.[0-9]+)(\\+-([0-9]*\\.[0-9]+))?").matcher(currentStimulus);

            double baseDelay;
            double deviationDelay = 0;
            if (delayFinder.find()) {
                baseDelay = Double.parseDouble(delayFinder.group(1));
                if (currentStimulus.contains("+-")) {
                    try {
                        deviationDelay = Double.parseDouble(delayFinder.group(3));
                    } catch (ArrayIndexOutOfBoundsException ignored) {
                        throw new ParsingException(
                            String.format("Could not parse standard deviation of '%s'", currentStimulus));
                    }
                }
            } else {
                throw new ParsingException("Missing delay specification.");
            }

            double duration;
            Matcher durationFinder = Pattern.compile("~([0-9]+)").matcher(currentStimulus);
            if (durationFinder.find()) {
                duration = Double.parseDouble(durationFinder.group(1));
            } else {
                throw new ParsingException("Missing duration specification.");
            }


            scheduables.add(
                new DelayInjection(model, "LatencyMonkey", true, baseDelay, deviationDelay, service, null,
                    null) {
                    {
                        setTargetTime(new TimeInstant(targetTime));
                        setDuration(duration);
                    }
                }
            );
        }
    }
}
