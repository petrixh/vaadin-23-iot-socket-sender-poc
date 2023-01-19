package com.example.application.service;

public interface IotService {

    void registerObserver(Observer observer);

    void removeObserver(Observer observer);

    void sendMessage(String message);

    interface Observer {
        void onMessage(String message);
    }
}
