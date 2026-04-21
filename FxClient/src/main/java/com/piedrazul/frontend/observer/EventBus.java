package com.piedrazul.frontend.observer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Bus de eventos simple para el patrón Observer.
 *
 * Reemplaza el ApplicationEventPublisher de Spring.
 * Los controladores se suscriben en initialize() y se desuscriben
 * en volver() para evitar referencias colgantes.
 */
public class EventBus {

    // Mapa de evento → lista de observers (raw para aceptar cualquier tipo T)
    @SuppressWarnings("rawtypes")
    private final Map<AppEvent, List<Observer>> listeners =
            new EnumMap<>(AppEvent.class);

    @SuppressWarnings("unchecked")
    public <T> void subscribe(AppEvent event, Observer<T> observer) {
        listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(observer);
    }

    @SuppressWarnings("unchecked")
    public <T> void unsubscribe(AppEvent event, Observer<T> observer) {
        List<Observer> list = listeners.get(event);
        if (list != null) list.remove(observer);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> void publish(AppEvent event, T data) {
        List<Observer> list = listeners.get(event);
        if (list != null) {
            // Copia defensiva por si un observer se desuscribe durante la notificación
            new ArrayList<>(list).forEach(o -> o.onEvent(event, data));
        }
    }
}
