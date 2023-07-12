package com.mcz;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
    // serverPort variable will
    private final int serverPort;

    // Variable List of server clients
    private ArrayList<ServerWorker> workerList = new ArrayList<>();

    public Server(int serverPort, InetAddress host) {
        this.serverPort = serverPort;
    }

    // This method is a way for the clint to access other clients on the server
    public List<ServerWorker> getWorkerList(){
        return workerList;
    }

    // This run() method will accept the connections
    @Override
    public void run() {
        // Surround with exception handler
        try {
            // #2: This class contains list of all clients on the server socket
            ServerSocket serverSocket = new ServerSocket(serverPort);
            // #5: This is an infinite loop which accepts new client to the
            // connection of the IRC chat as long as the server is live and we close the program
            while (true) {
                System.out.println(">>> About to accept client connection...");
                // #4: This creates the connection between the server and the client
                Socket clientSocket = serverSocket.accept();
                OutputStream outputStream = clientSocket.getOutputStream();

                // Once using telnet, the following message will show
                System.out.println("<<< Accepted connection from " + clientSocket);

                // Call ServerWorker.java which extends Thread(), we also add server as part
                // of the parameter to access instance of WorkerList array from list ServerWorker
                // This allows us to have a collection of workers and send msg from one connection to another
                ServerWorker worker = new ServerWorker(this ,clientSocket);
                // When the client connects, they are added to the Array list
                workerList.add(worker);
                // Client server start, see ServerWorker.java
                worker.start();
                System.out.println(">>> Typing... ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Handling logging off by removing the client from the server
    // If the client is not removed, the IRC IDE will throw exception error
    public void removeWorker(ServerWorker serverWorker) {
        workerList.remove(serverWorker);
    }
}
