package ru.demonorium.timeline;

import ru.demonorium.timeline.impl.EventWrapper;
import ru.demonorium.timeline.impl.IntTreeMap;

import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.AbstractMap;
import java.util.Map;
import java.util.function.Consumer;

public class Timeline implements Consumer<Event> {
    //            days       hours      minutes    millis
    private final IntTreeMap<IntTreeMap<IntTreeMap<IntTreeMap<EventWrapper>>>> storage = new IntTreeMap<>(null);
    private EventWrapper first;

    @Override
    public void accept(Event event) {
        CreationNode<EventWrapper> node = getNode(event.end());
        EventWrapper wrapper = new EventWrapper(event, node.map);

        int millis = event.end().get(ChronoField.MILLI_OF_DAY);
        if (node.target == null) {
            if (node.map.isEmpty()) {
                first = wrapper;
                node.map.put(millis, first);
                return;
            }

            Map.Entry<Integer, EventWrapper> entry = node.map.floorEntry(millis);
            if (entry == null) {
                entry = node.map.ceilingEntry(millis);
                entry.getValue().addPrev(wrapper);
            } else if (entry.getKey() == millis) {
                node.map.put(millis, wrapper).addNext(wrapper);
            } else {
                entry.getValue().addNext(wrapper);
            }
        }

        if (node.last) {
            node.target.addNext(wrapper);
        } else {
            node.target.addPrev(wrapper);
        }

        node.map.put(millis, wrapper);
    }

    public <T extends Temporal, B extends Temporal & Comparable<T>> void run(B currentTime) {
        while (first != null) {
            Event event = first.get();

            int delta = currentTime.get(ChronoField.INSTANT_SECONDS) - event.end().get(ChronoField.INSTANT_SECONDS);
            if (delta == 0) {
                delta = currentTime.get(ChronoField.MICRO_OF_SECOND) - event.end().get(ChronoField.MICRO_OF_SECOND);
            }

            if (delta >= 0) {
                try {
                    event.run();
                } catch (Exception exception) {

                }
            } else {
                return;
            }
        }

    }

    private CreationNode<EventWrapper> getNode(Temporal time) {
        int daysCount = time.get(ChronoField.EPOCH_DAY);
        int hoursCount = time.get(ChronoField.HOUR_OF_DAY);
        int minutesCount = time.get(ChronoField.MINUTE_OF_HOUR);

        if (storage.isEmpty()) {
            IntTreeMap<IntTreeMap<IntTreeMap<EventWrapper>>> hours = new IntTreeMap<>(storage);
            IntTreeMap<IntTreeMap<EventWrapper>> minutes = new IntTreeMap<>(hours);
            IntTreeMap<EventWrapper> result = new IntTreeMap<>(minutes);
            minutes.put(minutesCount, result);
            hours.put(hoursCount, minutes);
            storage.put(daysCount, hours);

            return new CreationNode<>(null, false, result);
        }

        CreationNode<IntTreeMap<IntTreeMap<EventWrapper>>> hoursMap = get(storage, daysCount);
        if (hoursMap.target != null) {
            IntTreeMap<IntTreeMap<EventWrapper>> minutes = new IntTreeMap<>(hoursMap.map);
            IntTreeMap<EventWrapper> result = new IntTreeMap<>(minutes);
            minutes.put(minutesCount, result);
            hoursMap.map.put(hoursCount, minutes);
            EventWrapper wrapper;
            if (hoursMap.last) {
                wrapper = hoursMap.target.lastEntry().getValue().lastEntry().getValue();
            } else {
                wrapper = hoursMap.target.firstEntry().getValue().firstEntry().getValue();
            }

            return new CreationNode<>(wrapper, hoursMap.last, result);
        }

        CreationNode<IntTreeMap<EventWrapper>> minutesMap = get(hoursMap.map, hoursCount);
        if (minutesMap.target != null) {
            IntTreeMap<EventWrapper> result = new IntTreeMap<>(minutesMap.map);
            minutesMap.map.put(minutesCount, result);

            EventWrapper wrapper;
            if (hoursMap.last) {
                wrapper = minutesMap.target.lastEntry().getValue();
            } else {
                wrapper = minutesMap.target.firstEntry().getValue();
            }

            return new CreationNode<>(wrapper, hoursMap.last, result);
        }

        return get(minutesMap.map, minutesCount);
    }

    private static <T> CreationNode<T> get(IntTreeMap<IntTreeMap<T>> map, int value) {
        Map.Entry<Integer, IntTreeMap<T>> entry = map.floorEntry(value);
        if (entry == null) {
            entry = map.ceilingEntry(value);
            return new CreationNode<>(entry.getValue().firstEntry().getValue(), false, map.put(value, new IntTreeMap<>(map)));
        } else if (entry.getKey() == value) {
            return new CreationNode<>(null, false, entry.getValue());
        } else {
            return new CreationNode<>(entry.getValue().lastEntry().getValue(), true, map.put(value, new IntTreeMap<>(map)));
        }
    }

    private static class CreationNode<T> {
        private final T target;
        private final boolean last;
        private final IntTreeMap<T> map;

        public CreationNode(T target, boolean last, IntTreeMap<T> map) {
            this.target = target;
            this.last = last;
            this.map = map;
        }
    }
}
