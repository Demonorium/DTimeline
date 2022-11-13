package ru.demonorium.timeline.impl;

import ru.demonorium.timeline.Event;

public class EventWrapper implements Runnable {
    private final Event event;
    private final IntTreeMap<EventWrapper> container;

    private EventWrapper next;
    private EventWrapper prev;

    public EventWrapper(Event event, IntTreeMap<EventWrapper> container) {
        this.event = event;
        this.container = container;
    }

    @Override
    public void run() {
        event.run();
    }

    public EventWrapper getNext() {
        return next;
    }

    public EventWrapper getPrev() {
        return prev;
    }

    public Event get() {
        return event;
    }

    public void addNext(EventWrapper wrapper) {
        wrapper.prev = this;

        if (next == null) {
            next = wrapper;
            return;
        }

        wrapper.next = next;
        next.prev = wrapper;
        next = wrapper;
    }

    public void addPrev(EventWrapper wrapper) {
        wrapper.next = this;
        if (prev == null) {
            prev = wrapper;
            return;
        }

        wrapper.prev = prev;
        prev.next = wrapper;
        prev = wrapper;
    }
}
