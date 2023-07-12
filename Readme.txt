===============================
Project environment requirement
===============================

- 	Apache Commons Lang for project library
	https://commons.apache.org/proper/commons-lang/
	You can find this JAR file in the root of this submitted folder, use
		commons-lang3-3.12.0.jar

- 	TelNet on windows enabled for commandline testing for TCP connection
	Telnet localhost 20111 

- 	Testing File path in the ServerWorker.java, Line 60
	Current
		"C:/Users/mousu/source/repos/Java/MultUserChat/ChatServer/src/com/mcz/getMsg.txt"
	New
		"C:/-- PLEASE INSERT PATH HERE FROM YOUR DEVICE ROOT --/MultUserChat/ChatServer/src/com/mcz/getMsg.txt"

=============
Login Details
=============
	username: guest
	password: guest

	and 

	username: mou
	password: mou


=============
step by step:
=============
1 - 	Once you have connected via the use of telnet at the command line
	Connect to IP address if required

2 - 	Use credentials to login and the following format. 
	THE SCREEN MAY NOT SHOW THE USERNAME AND PASSWORD DURING THIS PROCESS
		login guest guest 
	-and/or-
		login mou mou

3 - 	Once logged in, use the following REQUESTS and await RESPONSE

		PROTOCOL? 1 {username provided above}
	
		TIME?
	
		LIST? 1614680000 1

		Topic: #announcements

		GET? bc18ecb5316e029af586fdec9fd533f413b16652bafe079b23e021a6d8ed69aa

		BYE

4 - 	The system has added security, the ability to broadcasting the RESPONSE across the server.
	However the clients need to join the topic group. Only the client that are in the topic group
	can only see the response. Please login and join with both clients: 'Guest' ad 'Mou', 
	before going ahead with the following steps.

	4.1 Joing the #announcment group by the following commands in the terminal with both user
		join #announcement
	
	4.2 One client can use the following format to REQUEST a RESPONSE:
		msg #announcement PROTOCOL? 1

	4.3 Once finished, you can leave group by the following:
		leave #announcement


5 - 	To quit the program, type the following:
		BYE










	