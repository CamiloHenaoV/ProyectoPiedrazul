package com.piedrazul.frontend.observer;

public interface Observer<T> {
    void onEvent(AppEvent event, T data);
}
