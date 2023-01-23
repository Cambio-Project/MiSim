package cambio.simulator.test.performance;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import desmoj.core.simulator.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junitpioneer.jupiter.cartesian.CartesianTest;
import org.junitpioneer.jupiter.cartesian.CartesianTest.Values;
import org.junitpioneer.jupiter.params.DisableIfDisplayName;


/**
 * @author Lion Wagner
 */
@Disabled
public class EventListPerformanceTests {

    public static final Random RANDOM = new Random(5);



    @DisableIfDisplayName(matches = ".*\\d+\\d00000+.*EventTreeList.*")
    @CartesianTest(name = "Remove random {0} events into {1}")
    public void benchmarkInsertRandom(
        @Values(ints = {10_000, 50_000, 100_000, 1_000_000, 1_500_000, 2_500_000, 5_000_000, 10_000_000})
        final int numberOfEvents,
        @Values(classes = {EventTreeList.class, MiSimEventList.class, SortedMapEventList.class})
        final Class<EventList> eventListClass)
        throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        final EventList eventList = getEventList(eventListClass);
        final List<EventNote> events = new ArrayList<>(createEvents(numberOfEvents));
        Collections.sort(events);
        Collections.shuffle(events, RANDOM);

        long timeToInsert = timeToExecute(() -> events.forEach(eventList::insert));

        Assertions.assertEquals(numberOfEvents, eventList.size());

        System.out.printf("\tTime to insert %d events into %s: %dms%n",
            numberOfEvents, eventListClass.getSimpleName(), TimeUnit.NANOSECONDS.toMillis(timeToInsert));
    }



    @DisableIfDisplayName(matches = ".*\\d+\\d00000+.*EventTreeList.*")
    @CartesianTest(name = "Remove random {0} events into {1}")
    public void benchmarkInsertInOrder(
        @Values(ints = {10_000, 50_000, 100_000, 1_000_000, 1_500_000, 2_500_000, 5_000_000, 10_000_000})
        final int numberOfEvents,
        @Values(classes = {EventTreeList.class, MiSimEventList.class, SortedMapEventList.class})
        final Class<EventList> eventListClass)
        throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        final EventList eventList = getEventList(eventListClass);
        final List<EventNote> events = new ArrayList<>(createEvents(numberOfEvents));
        Collections.sort(events);

        long timeToInsert = timeToExecute(() -> events.forEach(eventList::insert));

        Assertions.assertEquals(numberOfEvents, eventList.size());

        System.out.printf("\tTime to insert %d events into %s: %dms%n",
            numberOfEvents, eventListClass.getSimpleName(), TimeUnit.NANOSECONDS.toMillis(timeToInsert));
    }

    @DisableIfDisplayName(matches = ".*\\d+\\d00000+.*EventTreeList.*")
    @CartesianTest(name = "Remove first {0} events into {1}")
    public void benchmarkRemoveFirst(
        @Values(ints = {10_000, 50_000, 100_000, 1_000_000, 1_500_000, 2_500_000, 5_000_000, 10_000_000})
        final int numberOfEvents,
        @Values(classes = {EventTreeList.class, MiSimEventList.class, SortedMapEventList.class})
        final Class<EventList> eventListClass)
        throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        final EventList eventList = getEventList(eventListClass);
        final Collection<EventNote> events = createEvents(numberOfEvents);
        insertWithValidityCheck(eventList, events);

        final long timeToRemove = timeToExecute(() -> {
            for (EventNote ignored : events) {
                eventList.removeFirst();
            }
        });

        Assertions.assertEquals(0, eventList.size());
        System.out.printf("\tTime to read %d events from %s: %dms%n",
            numberOfEvents, eventListClass.getSimpleName(), TimeUnit.NANOSECONDS.toMillis(timeToRemove));
    }


    @DisableIfDisplayName(matches = ".*\\d+\\d00000+.*EventTreeList.*")
    @CartesianTest(name = "Remove random {0} events into {1}")
    public void benchmarkRemoveRandom(
        @Values(ints = {10_000, 50_000, 100_000, 1_000_000, 1_500_000, 2_500_000, 5_000_000, 10_000_000})
        final int numberOfEvents,
        @Values(classes = {EventTreeList.class, MiSimEventList.class, SortedMapEventList.class})
        final Class<EventList> eventListClass)
        throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        EventList eventList = getEventList(eventListClass);
        final List<EventNote> events = new ArrayList<>(createEvents(numberOfEvents));
        insertWithValidityCheck(eventList, events);
        Collections.shuffle(events, RANDOM);

        long timeToRemove = timeToExecute(() -> {
            for (EventNote eventNote : events) {
                eventList.remove(eventNote);
            }
        });

        System.out.printf("\tTime to remove %d events randomly from %s: %dms%n",
            numberOfEvents, eventListClass.getSimpleName(), TimeUnit.NANOSECONDS.toMillis(timeToRemove));
    }

    @NotNull
    private static EventList getEventList(Class<EventList> eventListClass)
        throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return eventListClass.getConstructor().newInstance();
    }

    private static void insertWithValidityCheck(EventList eventList, Iterable<EventNote> eventNotes) {
        for (EventNote eventNote : eventNotes) {
            int prevSize = eventList.size();
            eventList.insert(eventNote);
            Assertions.assertEquals(prevSize + 1, eventList.size());
        }
        Assertions.assertEquals(eventList.size(), eventList.size());
    }


    @NotNull
    private static Collection<EventNote> createEvents(int numberOfEvents) {
        final Collection<EventNote> events = new HashSet<>();
        for (int i = 0; i < numberOfEvents; i++) {
            TimeInstant targetTime;
            int priority;
            while (true) {
                targetTime = new TimeInstant(RANDOM.nextDouble() * 10_000);
                priority = RANDOM.nextInt();
                break;
            }

            EventNote note = new EventNote(null, null, null, null, targetTime, priority, null);
            events.add(note);
        }
        return events;
    }

    private long timeToExecute(Runnable runnable) {
        long time = System.nanoTime();
        runnable.run();
        return System.nanoTime() - time;
    }
}
