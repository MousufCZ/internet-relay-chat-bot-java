/*Polite Messaging
* By Mousuf C Zaman
* Please view readme.txt for environment set up and program execution
* */


package com.mcz;

import java.net.InetAddress;
import java.net.UnknownHostException;


// #1: The entry point for the server application
public class ServerMain {
    public static void main(String[] args) throws UnknownHostException {
        // IP connection address
        InetAddress host = InetAddress.getByName("127.0.0.1");

        // #3 This is the port we will connect the IRC Chat
        int port = 20111;

        // #2 This class contains list of all clients on the server socket
        Server server = new Server(port, host);

        // Start server method to kick off the thread
        server.start();
    }
}