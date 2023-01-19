package com.example.application.service;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

// Original implementation from
// https://www.baeldung.com/a-guide-to-java-sockets
@Service
public class IoTServiceStandardJava implements IotService {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread receivingThread;
    private ArrayList<Observer> observers = new ArrayList<>();
    private AtomicInteger serverAck = new AtomicInteger(0);

    @PostConstruct
    protected void postConstruct() throws IOException {
        int port = 9876;
        String ip = "localhost";

        //In a real world case, use a pool... probably want more than one thread to handle parsing..
        receivingThread = new Thread(() -> {
            try {
                start(port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        receivingThread.start();

        System.out.println("IoT Server started on ip: " + ip + " and port: " + port);
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        while (true) {
            String greeting = in.readLine();
            System.out.println("Got from IoT Device: \"" + greeting + "\"");

            //Proper exception handling etc... but POC...
            observers.forEach(observer -> observer.onMessage(greeting));
            //Server responds with 100, 101, 102... etc...
            out.println("Server ACK " + (serverAck.incrementAndGet() * 100));

        }
    }

    // Probably better to use concurrent objects rather than synchronizing
    // over the entire service, but POC...
    @Override
    public synchronized void registerObserver(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public synchronized void removeObserver(Observer observer) {
        if (observers.contains(observer)) {
            observers.remove(observer);
        }
    }

    @Override
    public synchronized void sendMessage(String message) {
        out.println(message);
    }

    @PreDestroy
    public void stop() {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.close();
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        receivingThread.interrupt();
    }

}
