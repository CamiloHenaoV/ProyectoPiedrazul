package com.piedraazul.gestioncitasmedicas.observer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EventBus {

    private final Map<AppEvent, List<Observer<Object>>> observers = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> void subscribe(AppEvent eventType, Observer<T> observer) {
        observers
                .computeIfAbsent(eventType, k -> new ArrayList<>())
                .add((Observer<Object>) observer);
    }

    public <T> void publish(AppEvent eventType, T data) {
        List<Observer<Object>> eventObservers = observers.get(eventType);
        if (eventObservers != null) {
            eventObservers.forEach(o -> o.onEvent(eventType, data));
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void unsubscribe(AppEvent eventType, Observer<T> observer) {
        List<Observer<Object>> eventObservers = observers.get(eventType);
        if (eventObservers != null) {
            eventObservers.remove(observer);
        }
    }
}