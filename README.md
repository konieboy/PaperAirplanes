# PaperAirplanes  
Experimental chat program in Java for CPSC441 at the University of Calgary  

Version 0.8 April 4th  
Features  
-Chat with friends, secured or unsecured  
    -1 on 1 chat  
    -Group chat  
    -Public chat  
-Upload and download small files  
-Have a friends list  
-Inseucre login with secure password keeping!!!  
  
## How to run    
-Initialize the java server using the command "java Server <port number>"  
-Initialize the client using the command "java Client <ip address> <port number>"  


## Commands  
/add <username>     --Adds friend to your friendslist if he exists  
/remove <username>  --Removes friend from your friendslist if they are on it  
/friends            --Displays all friends on your friendslist  
/online             --Displays all users currently online using the service  
/connect <username> --If username exists, is on your friendslist and is online,  
                    --then a new chat terminal appears, waiting for your friend   
                    --to connect  
->  /chat <key>     --Key is not necessary, but using known key between,  
                    --two users encrypts the messages between them  
/quit               --Properly closes the terminal window of a private chat  
                    --terminal, or leaves the application  
/public <name>      --Enters user into the <name> group chat. Default open chat  
                    --room is called General  
/makepublic <name>  --Creates a new public chat called <name>  
/upload <file>.<ext>--Uploads a file to the Server  
/get <file>.<ext>   --Downloads a file to the Server if it exists.  
/files              --Displays all files currently on the Server for download  
