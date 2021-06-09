package de.rss.fachstudie.MiSim.parsing;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.rss.fachstudie.MiSim.entities.generator.Generator;
import de.rss.fachstudie.MiSim.entities.generator.LIMBOGenerator;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.microservice.Operation;
import de.rss.fachstudie.MiSim.events.ChaosMonkeyEvent;
import de.rss.fachstudie.MiSim.events.LatencyMonkeyEvent;
import de.rss.fachstudie.MiSim.events.SummonerMonkeyEvent;
import de.rss.fachstudie.MiSim.models.ArchitectureModel;
import de.rss.fachstudie.MiSim.models.MainModel;
import desmoj.core.simulator.TimeInstant;

/**
 * Represents an scenario description input. Can be parsed to an set, containing the experiment events such as {@link
 * ChaosMonkeyEvent}s or {@link Generator}s.
 *
 * @author Lion Wagner
 */
public class ScenarioDescription {

    public String name;
    public String description;
    public String artifact;
    public String component;
    public String stimulus;
    public String source;
    public String environment;
    public Integer duration;

    /**
     * Converst this scenario description into a set of event objects.
     *
     * @return a set of objects that describe the scneario.
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

        ArchitectureModel archModel = ArchitectureModel.get();
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
                scheduables
                    .add(new LIMBOGenerator(MainModel.get(), "LIMBO_GENERATOR", true, operation, new File(profile)));
            }
        } else {
            Operation target = service.getOperationByName(component);
            scheduables.add(new LIMBOGenerator(MainModel.get(), "LIMBO_GENERATOR", true, target, new File(profile)));
        }
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

        ArchitectureModel archModel = ArchitectureModel.get();
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
                new ChaosMonkeyEvent(MainModel.get(), "Chaosmonkey", true, service, instances) {
                    {
                        setTargetTime(new TimeInstant(targetTime));
                    }
                }
            );
        } else if (currentStimulus.startsWith("RESTART")) {
            int instances = Integer.parseInt(currentStimulus.replace("RESTART", ""));


            scheduables.add(
                new SummonerMonkeyEvent(MainModel.get(), "Summoner", true, service, instances) {
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
                new LatencyMonkeyEvent(MainModel.get(), "LatencyMonkey", true, baseDelay, deviationDelay, service) {
                    {
                        setTargetTime(new TimeInstant(targetTime));
                        setDuration(duration);
                    }
                }
            );
        }
    }
}
