package com.aquaticsafetyconceptsllc.iswimband.Event;

/**
 * Created by donaldpae on 11/24/14.
 */
public class SEvent {
    public String name;
    public Object object;

    public SEvent(String name) {
        this.name = name;
        this.object = null;
    }

    public SEvent(String name, Object object) {
        this.name = name;
        this.object = object;
    }


    public static final String EVENT_BLUETOOTH_STATE_CHANGED = "event bluetooth state changed";
    public static final String EVENT_NETWORK_STATE_CHANGED = "event network state changed";
}
