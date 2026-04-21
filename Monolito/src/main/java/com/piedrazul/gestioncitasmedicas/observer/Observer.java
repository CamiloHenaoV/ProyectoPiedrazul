package com.piedrazul.gestioncitasmedicas.observer;

public interface Observer<T> {
    void onEvent(AppEvent event, T data);
}