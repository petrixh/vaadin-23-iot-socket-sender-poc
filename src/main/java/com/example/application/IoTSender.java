package com.example.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple Java app that connects to a socket and sends messages to it...
 */
public class IoTSender {

    // No exception handling... all exceptions are just thrown for PoC...
    public static void main(String[] args) throws IOException {
        IoTSender localhost = new IoTSender("127.0.0.1", 9876);
    }

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private AtomicInteger iotValue = new AtomicInteger(0);

    public IoTSender(String ip, int port) throws IOException {

        startConnection(ip, port);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                String response = sendMessage("IoT Device sending: " + iotValue.incrementAndGet());
                System.out.println("IoT Device got response: " + response);

                //Server sent something, not just an ACK... Loop back to server for POC
                if (response != null && !"".equals(response.trim()) && !response.startsWith("Server ACK")) {
                    sendMessage("Client got message from Server: " + response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.SECONDS);

    }

    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public String sendMessage(String msg) throws IOException {
        System.out.println("IoT Device sending: " + msg);
        out.println(msg);
        String resp = in.readLine();
        return resp;
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        //scheduledExecutorService.close();
    }

}
