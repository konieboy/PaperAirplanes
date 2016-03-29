# PaperAirplanes
Experimental chat program in Java for CPSC441 at the University of Calgary

Version 0.1 March 4 , 2016
Features
-Client is able to parse input as special commands
-Client can connect to the server successfully with the /connect command
-Framework for the following classes:
    -Server
    -Client
    -User
    -RoomServer
    -RoomClient
-loops untill the user exits with /quit

How to run
-Initialize the java server using the command "java Server <port number>"
-Initialize the client using the command "java Client <ip address> <port number>"
-Enter the "/connect username"
-Exit witht the command "/quit"

Version 0.2 will be able to send messages to the server!
