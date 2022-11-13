package ru.demonorium.timeline;

import java.time.temporal.Temporal;

public interface Event extends Runnable {
    Temporal end();
    Criticality critical();
}
