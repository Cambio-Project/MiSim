package desmoj.core.simulator;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Lion Wagner
 */
public class MiSimEventList extends EventList {

    private long currentBinTime = 0;
    PriorityQueue<EventNote> currentBin = new PriorityQueue<>();
    TreeMap<Long, Collection<EventNote>> binList = new TreeMap<>();


    @Override
    protected EventNote firstNote() {
        if (currentBin.isEmpty()) {
            if (binList.isEmpty()) {
                return null;
            }
            currentBin = new PriorityQueue<>();
            Entry<Long, Collection<EventNote>> nextBin = binList.pollFirstEntry();
            currentBinTime = nextBin.getKey();
            currentBin.addAll(nextBin.getValue());
        }

        return currentBin.peek();
    }

    @Override
    void insert(EventNote newNote) {
        addNoteToEntities(newNote);
        long bin = getBin(newNote);
        if (bin < currentBinTime) {
            throw new IllegalArgumentException("Event time is in the past");
        } else if (bin == currentBinTime) {
            currentBin.add(newNote);
        } else {
            if (!binList.containsKey(bin)) {
                binList.put(bin, new ArrayList<>());
            }
            binList.get(bin).add(newNote);
        }
    }

    @Override
    void insertAfter(EventNote afterNote, EventNote newNote) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    void insertAsFirst(EventNote newNote) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    void insertBefore(EventNote beforeNote, EventNote newNote) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    boolean isEmpty() {
        return currentBin.isEmpty() && binList.isEmpty();
    }

    @Override
    EventNote lastNote() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    EventNote nextNote(EventNote origin) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    EventNote prevNote(EventNote origin) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    void remove(EventNote note) {
        long bin = getBin(note);
        if (bin == currentBinTime) {
            currentBin.remove(note);
        } else {
            binList.get(bin).remove(note);
        }
        removeNoteFromEntities(note);
    }

    @Override
    EventNote removeFirst() {
        EventNote note = firstNote();
        currentBin.poll(); // removes first
        removeNoteFromEntities(note);
        return note;
    }

    @Override
    boolean isRandomizingConcurrentEvents() {
        return false;
    }

    private long getBin(EventNote note) {
        return (int) note.getTime().getTimeAsDouble();
    }


    /**
     * Add the {@link EventNote} to its various entities.
     */
    private void addNoteToEntities(EventNote newNote) {
        Entity who1 = newNote.getEntity1();
        if (who1 != null) {
            who1.addEventNote(newNote);
        }

        Entity who2 = newNote.getEntity2();
        if (who2 != null) {
            who2.addEventNote(newNote);
        }

        Entity who3 = newNote.getEntity3();
        if (who3 != null) {
            who3.addEventNote(newNote);
        }

        EventAbstract Event = newNote.getEvent();
        if (Event != null) {
            Event.addEventNote(newNote);
        }
    }


    /**
     * Remove the {@link EventNote} from its various entities.
     */
    private void removeNoteFromEntities(EventNote note) {
        if (note.getEntity1() != null) { // if an entity exists (no external event)
            note.getEntity1().removeEventNote(note); // removes list entry in Entity!
        }

        if (note.getEntity2() != null) { // if an entity exists (no external event)
            note.getEntity2().removeEventNote(note); // removes list entry in Entity!
        }

        if (note.getEntity3() != null) { // if an entity exists (no external event)
            note.getEntity3().removeEventNote(note); // removes list entry in Entity!
        }

        if (note.getEvent() != null) { // if an event exists
            note.getEvent().removeEventNote(note);      // remove EventNote
        }
    }
}
