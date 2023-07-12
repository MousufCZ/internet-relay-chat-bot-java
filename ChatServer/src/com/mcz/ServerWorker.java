package com.mcz;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.*;

public class ServerWorker extends Thread{

    private final Socket clientSocket;
    // This field variable allow to pass instence to all clients on server
    private final Server server;
    // Login Variable, set to null if not logged in
    private String login = null;
    // Instance of the class field, created during send()
    private OutputStream outputStream;
    // Set of #TOPIC
    private HashSet<String> topicSet = new HashSet<>();


    // Constructor called in ServerMain.java
    // Handles communication
    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    // Handling multiple connections from >= 1 client
    // We create a new thread everytime we get a connection from new client
    @Override
    public void run() {
        try {
            // We call this handle by passing in clientSocket (new client being accepted)
            System.out.println("<<< Connecting new client... " + '\n');
            handleClientSocket();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    // Create a test output here, this can be found by using telnet cmd:telnet localhost 8818
    private void handleClientSocket() throws IOException, InterruptedException {
        // Get access to read data from the client -- bi-directional communication
        InputStream inputStream = clientSocket.getInputStream();

        // #6: Every client/socket has an output stream
        this.outputStream = clientSocket.getOutputStream();

        // Buffer reader, to read line by line
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        // Open the file
        FileInputStream fstream = new FileInputStream("C:/Users/mousu/source/repos/Java/MultUserChat/ChatServer/src/com/mcz/getMsg.txt");
        BufferedReader br1 = new BufferedReader(new InputStreamReader(fstream));
        // Local Line variable from file
        String fileLine;

        //Print from file
        //FileInputStream fstream = new FileInputStream("src/com/mcz/getMsg.txt");
        // This will allow us to read line by line
        while ((line = reader.readLine()) != null){

            // I used command lang for token splitting
            // Split my lines in to individual tokens
            String[] tokens = StringUtils.split(line);

            // Prevent any tokens Null Pointer exceptions
            if (tokens != null && tokens.length >0) {
                // These tokens will always be our command
                // [0] = the first comment by client, [1] = second comment by client, [2] = the third comment by client
                String cmd = tokens[0];
                //Insert while loop by adding break  break == Bye
                // This also removes the client from the stored array list of all clients
                if ("bye".equalsIgnoreCase(cmd)) {
                    System.out.println("<<< The IRC will now close due to 'bye' command");
                    // Logging off and bye handling
                    handleLogoff();
                    clientSocket.close();

                }

                // This is used for security (additonal function) credentials.
                // The user must first log in to be able to retrieve any messages.
                // All login credentials are hard coded in to the source code.
                else if("login".equalsIgnoreCase(cmd)) {
                    // Handles Username and Password
                    handleLogin(outputStream, tokens);
                }

                // Direct message from credential approved client to make request from the server
                // See handleMessage() method``````````
                // Format: "msg" "login" body....
                else if("msg".equalsIgnoreCase(cmd)){
                    // How many tokens (words) can client send
                    String[] tokenMsg = StringUtils.split(line, null, 3);
                    handleMessage(tokenMsg);
                }

                // Join #topic of the message, e.g. #Annoncement
                // This is used to broadcast the message to any
                // other clients who have joined the #topic, this allows for an additional layer of security
                // Format: "Join" "#TOPIC" body....
                else if("join".equalsIgnoreCase(cmd)){
                    handleJoin(tokens);
                }

                // Leave #topic to stop recieving RESPONSES
                // Format: "Leave" "#TOPIC" body....
                else if("leave".equalsIgnoreCase(cmd)){
                    handleLeave(tokens);
                }

                // The protocol request from the client will be a response with the
                else if("Protocol?".equalsIgnoreCase(cmd)){
                    outputStream.write(("PROTOCOL " + "1" + " " + login + ('\n')).getBytes(StandardCharsets.UTF_8));
                }

                // TIME Request from the client
                else if("Time?".equalsIgnoreCase(cmd)){
                    outputStream.write(("NOW" + " " + new Date().getTime() + ('\n')).getBytes(StandardCharsets.UTF_8));
                }

                // This is the client "List" command, you can find handleLong() and handleInt() below
                else if("List?".equalsIgnoreCase(cmd) && handlelong(tokens[1]) && handleInt(tokens[2])){
                    // variable we will be using
                    long since = Long.parseLong(tokens[1]);
                    long epochTime = new Date().getTime();
                    // counter for int
                    int listCount = 0;
                    // Array list of strings as hash
                    //ArrayList<String> hash = new ArrayList<>();

                    //If the present time is greater then when the message was captured using UNIX Epoch time
                    if(epochTime >= since) {
                        // This will only print out if the List time given is <= the present time
                        outputStream.write((" " + ('\n')).getBytes(StandardCharsets.UTF_8));
                    }
                }

                // Topic: # Request from the client
                else if("Topic".equalsIgnoreCase(cmd)){
                    outputStream.write(("MESSAGES" + " " + "1" + ('\n')).getBytes(StandardCharsets.UTF_8));
                    outputStream.write(("bc18ecb5316e029af586fdec9fd533f413b16652bafe079b23e021a6d8ed69aa" + ('\n')).getBytes(StandardCharsets.UTF_8));
                }

                // Reading from file of the stored messages
                // Format: "file"
                else if("Get?".equalsIgnoreCase(cmd)) {
                    //Read File Line By Line
                    outputStream.write(("FOUND" + ('\n')).getBytes(StandardCharsets.UTF_8));

                    while ((fileLine = br1.readLine()) != null) {
                        // Split the line in to texts
                        String[] tokens1 = StringUtils.split(fileLine);

                        // Prevent any tokens Null Pointer exceptions
                        while(tokens1 != null && tokens1.length >0){
                            //System.out.println(fileLine);;
                            // These tokens will always be our command
                            String cmd1 = tokens1[0];

                            // If there is "message-id", the system and client terminal will output "message-id: CONTENT
                            if ("message-id:".equalsIgnoreCase(String.valueOf(cmd1))) {
                                System.out.println(fileLine);
                                outputStream.write((fileLine + ('\n')).getBytes(StandardCharsets.UTF_8));
                                break;
                            }

                            // If there is "time-sent", the system and client terminal will output "time-sent": CONTENT
                            else if ("time-sent:".equalsIgnoreCase(String.valueOf(cmd1))) {
                                System.out.println(fileLine + " " + new Date().getTime());
                                outputStream.write((fileLine + " " + new Date().getTime() + ('\n')).getBytes(StandardCharsets.UTF_8));
                                break;
                            }

                            // If there is "From", the system and client terminal will output "From": CONTENT
                            else if ("From:".equalsIgnoreCase(String.valueOf(cmd1))) {
                                System.out.println(fileLine);
                                outputStream.write((fileLine + ('\n')).getBytes(StandardCharsets.UTF_8));
                                break;
                            }

                            // If there is "topic", the system and client terminal will output "topic": CONTENT
                            else if ("Topic:".equalsIgnoreCase(String.valueOf(cmd1))) {
                                System.out.println(fileLine);
                                outputStream.write((fileLine + ('\n')).getBytes(StandardCharsets.UTF_8));
                                break;
                            }

                            // If there is "subject", the system and client terminal will output "subject": CONTENT
                            else if ("Subject:".equalsIgnoreCase(String.valueOf(cmd1))) {
                                System.out.println(fileLine);
                                outputStream.write((fileLine + ('\n')).getBytes(StandardCharsets.UTF_8));
                                break;
                            }

                            // If there is "contents", the system and client terminal will output "contents" CONTENT
                            else if("Contents:".equalsIgnoreCase(String.valueOf(cmd1))){
                                // This takes in the array of body text, this will be essential to recall message body
                                ArrayList<String> arrayBody = new ArrayList<String>();

                                //This will return the number of lines there are in the content of the message
                                String numberOfBodyLines = tokens1[1];
                                int bodyLines = Integer.parseInt(numberOfBodyLines);

                                // The counter will control of the iteration through the line and when to break our of the while loop
                                int counter = 0;

                                // This will print in the terminal
                                System.out.println(fileLine);
                                outputStream.write((fileLine + ('\n')).getBytes(StandardCharsets.UTF_8));

                                // This
                                while((fileLine = br1.readLine()) != null && counter <= bodyLines){
                                    arrayBody.add(fileLine);
                                    counter++;
                                    if(true) {
                                        outputStream.write((fileLine + ('\n')).getBytes(StandardCharsets.UTF_8));
                                    }
                                } break;
                            }
                            else {
                                outputStream.write(("Thank you for using the GET? command, there are no other messages in the file. Please type 'BYE' to close connection" +
                                        ('\n')).getBytes(StandardCharsets.UTF_8));
                            } break;
                        }
                    }
                    /*else{
                        outputStream.write(("FOUND" + ('\n')).getBytes(StandardCharsets.UTF_8));
                    }*/
                }

                else {
                    // This will be the ECHO BACK back client msg, line by line
                    String msg = "<<< You typed: " + cmd + ", insert next command: " + "\n";
                    outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
                }
            }
        }
        // When we type 'Bye', the client exits the while loop and closes connection
        clientSocket.close();
    }


    // This will be used in the List client command
    // This is used to capture any errors when looking for token[1]
    public static boolean handlelong(String token) throws IOException {
        if(token == null){
            System.out.println("isLong False");
            return false;
        }
        else if (token.equals("")){
            System.out.println("isLong False");
            return false;
        }
        long i;
        try{
            i = Integer.parseInt(token);
            return true;
        }
        catch(NumberFormatException e){
            return false;
        }
    }


    // This will be used in the List client command
    // This is used to capture any errors when looking for token[2]
    public static boolean handleInt(String token) throws IOException  {
        if(token == null){
            System.out.println("isLong False");
            return false;
        }
        else if (token.equals("")){
            System.out.println("isLong False");
            return false;
        }
        int l;
        try{
            l = Integer.parseInt(token);
            return true;
        }
        catch(NumberFormatException e){
            return false;
        }
    }


    // This method is used to leave
    private void handleLeave(String[] tokens)throws IOException  {
        // if the token length is more then 1
        if(tokens.length > 1) {
            // Second token in the input ---> "#TOPIC"
            String topic = tokens[1];
            // remove client from the topicSet
            topicSet.remove(topic);
        }
    }

    // This tests to see if the input #topic is part of the topic set
    public boolean isMemberOfTopic(String topic) throws IOException {
        return topicSet.contains(topic);
    }


    // Handling #TOPIC, and if the client joins the #topic
    // Format: "msg" "#TOPIC" body....  // See handleMessage for #topic format implementation
    // See field variable for topic set
    private void handleJoin(String[] tokens) throws IOException {
        // if the token length is more then 1
        if(tokens.length > 1) {
            // Second token in the input ---> "#TOPIC"
            String topic = tokens[1];
            // Add topic to the topicSet
            // Client has joined this topic hash set, we need testing if client has joined this topic
            topicSet.add(topic);
        }
    }


    // Format doe REQUEST: "msg"
    // Format Topic: "msg" "#TOPIC" body....
    private void handleMessage(String[] tokens) throws IOException {
        // Second token in the input ---> "login"
        String sendTo = tokens[1];

        // Third token in the input ---> body
        String body = tokens[2];

        // REQUEST format separates second format is by the use of '#'
        // To not allow # to be used for REQUEST is by the following
        // recieve: msg #topic:<login> body....
        boolean isTopic = sendTo.charAt(0) == '#';

        // Iterate through all the list of server workers for broadcast or simple REQUEST
        List<ServerWorker> workerList = server.getWorkerList();
        for(ServerWorker worker : workerList) {
            // For broadcasting to a #Topic
            if (isTopic) {
                // now we are going to check if the client is member of the #TOPIC
                // sendTo now is going to be the topic that was the input #TOPIC
                if(worker.isMemberOfTopic(sendTo)){
                    // If client is part of the topic, we are going to send that to
                    // we can use 'sendTo', where the topic is broadcasted
                    // recieve: msg #topic:<login> body....
                    String outMsg = "msg " + sendTo + ":" + login + " " + body + "\n";
                    worker.send(outMsg);
                }
            }
            // Else, the message is REQUEST
            // Format: "msg" "login" body....
            else {
                // if the client login matches the send to
                if (sendTo.equalsIgnoreCase(worker.getLogin())) {
                    // Send message to that particular worker
                    // login is the sender
                    String outMsg = "msg " + login + " " + body + "\n";
                    worker.send(outMsg);
                }
            }
        }
    }

    // When we logoff, we close the current socket
    private void handleLogoff() throws IOException {
        // Handling logging off by removing the client from the server
        // If client is not removed from server, the IRC IDE will throw exception error
        server.removeWorker(this);

        // Sending message to all other clients that this client has logged off
        List<ServerWorker> workerList = server.getWorkerList();
        String offlineMsg = "<<< Current Status Offline " + login + "\n";
        for(ServerWorker worker : workerList){
            // Code clean up IF statement, do not send coming online message of client's on log in
            if (!login.equals(worker.getLogin())) {
                worker.send(offlineMsg);
            }
        }
        clientSocket.close();
    }

    // We here expose the login to other clients, so they know where the login is
    public String getLogin() throws IOException  {
        return login;
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        // Check if token is bigger then 3
        // Hard code user login
        if (tokens.length == 3){
            String login = tokens[1];
            String password = tokens[2];

            // Compare input and see if it matches the hard coded credentials
            if ((login.equals("guest") && password.equals("guest")) || (login.equals("mou") && password.equals("mou")) ){
                // If the input match hardcode, system outputs
                String msg = "<<< login successful\n";
                // Using UNIXTime Epoch
                String timeLoggedInMsg = ("log in time is: " + new Date().getTime() + "\n");
                outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
                outputStream.write(timeLoggedInMsg.getBytes(StandardCharsets.UTF_8));
                // Calling the Login variable, set to Null
                // Client user when logged in, become login.
                this.login = login;
                System.out.println("<<< A client has logged in successfully, user was: '" + login + "'.");

                // The message when someone logs in will be:
                // Here is the lost of all the workers
                List<ServerWorker> workerList = server.getWorkerList();

                // For all the live clients
                for (ServerWorker worker : workerList){
                    // Code clean up IF statement, do not send coming online message of client's on log in
                    // Guard the online login message from yourself when logging in.
                    // When the following IF stms are switched, they do not work properly
                    if (worker.getLogin() != null) {
                        // Code clean up IF statement, do not send online message of not logged in live clients
                        if (!login.equals(worker.getLogin())) {
                            // Message letting all clients connected to server of the user who has logged in
                            String msg2 = "<<< This client has come online: " + worker.getLogin() + "\n";
                            send(msg2);
                        }
                    }
                }

                // Message letting all clients connected to server the status of the user who has logged in
                String onlineMsg = "<<< Current Status Online " + login + "\n";
                for(ServerWorker worker : workerList){
                    // Code clean up IF statement, do not send coming online message of client's own log in
                    if (!login.equals(worker.getLogin())) {
                        worker.send(onlineMsg);
                    }
                }

            } else{
                // If the input doesn't match hardcode, system outputs
                String msg = "<<< login error\n";
                outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    // Send method access the output stream of current client socket, handleLogin()
    // and then send msg to client handleClientSocket()
    // the message is generic, as it can be any message
    private void send(String msg) throws IOException {
        // Code clean up IF statement, if the login detail in command prompt is null, do not display null client server
        if (login != null){
            outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
        }
    }
}
