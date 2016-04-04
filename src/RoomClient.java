//CPSC 441 W2016 Group 31
//Paper Airplanes Messenger
//Brendan Petras, Ethan Hamman, Konrad Wisniewski, Kyle Sutherland

//Imports
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;


/**
 *  RoomClient is a part of the client that manages all of the chat
 *  for a specific room on the chat server.  All user commands in
 *  this window are specific to the current room.
 */

 /*
 *  Commands:
 *  /add user
 *  /quit
 *  Just type to send message
 */


public class RoomClient{

    static Socket clientSocket = null;
    static DataOutputStream output = null;
    static BufferedReader input = null;
    private static String clientName;
    private static String friendName;
    private static String serverIPAddress;
    private static int portNumber;
    private static int myID;
    private static int roomID;
    private static String key = "";
    private static CryptoTools crypto;

    public static void main(String [] args){
        try{
            crypto = new CryptoTools();
        }catch(Exception b)
        {
            System.out.println("Crypto not initialized");
        }

        Scanner reader = new Scanner(System.in);
        try{
            clientName = (args[3]);
            friendName = (args[2]);
            roomID = Integer.parseInt(args[4]);
            myID = Integer.parseInt(args[5]);
            System.out.println(clientName +": Wait for " +friendName + " to connect...");
        }catch(Exception e){
            System.exit(0);
        }

        String lastLine = "";
        System.out.println("Enter /chat if you want to accept a chat with " + friendName + ". \nType /quit to exit the chat.");
        System.out.println("Type /chat 'key' where key is a random string that your chat partner knows and types as well.");
        System.out.print("Paper Airplanes: ");
        lastLine = reader.nextLine();
        if(lastLine.contains("/chat")){
            key = lastLine.replace("/chat","");
            key = key.replace(" ","");
            String serverIPAddress;
        	int portNumber;

        	BufferedReader  userInput = new BufferedReader(new InputStreamReader(System.in));
        	ExecutorService threadPool = Executors.newFixedThreadPool(2);

           serverIPAddress = args[0];
           portNumber = Integer.parseInt(args[1]);

        	try
        	{
        		clientSocket = new Socket(serverIPAddress, portNumber); //for now we are only connecting to one computer
        		output = new DataOutputStream(clientSocket.getOutputStream());
        		input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        	}catch (IOException e)
        	{
        		System.out.println(e);
        	}

            //Initializing room on server
            try{
    			output.writeBytes(":-:roomChannel "+myID + " \n");
    		}catch(IOException e){
    			System.out.println("IO exception");
    		}
            try {
                Thread.sleep(500);                 //1000 milliseconds is one second.
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            try{
                output.writeBytes(":-:setChatChannel "+roomID+" "+clientName+ " "+myID + " \n");
            }catch(IOException e){
                System.out.println("IO exception");
            }


        	//Callable objects
        	Callable<String> userInputTask = () -> {
        		return userInputLoop(userInput);
        	};

        	Callable<String> serverInputTask = () -> {
        		return serverInputLoop(input);
        	};

        	//Future objects
        	Future<String> serverInputFuture = threadPool.submit(serverInputTask);
        	Future<String> userInputFuture = threadPool.submit(userInputTask);

            lastLine = "";
        	Boolean doneProcessingServerInput = true;

        	while(!lastLine.equals("/quit")){
        		if(serverInputFuture.isDone() && doneProcessingServerInput){
        			serverInputFuture = threadPool.submit(serverInputTask);
        		}
        		if(userInputFuture.isDone() && doneProcessingServerInput){
        			userInputFuture = threadPool.submit(userInputTask);
        		}


        		while(!userInputFuture.isDone() && !serverInputFuture.isDone());

        		//If message loss occurs, add a delay here
        		if(userInputFuture.isDone() && !userInputFuture.isCancelled()){		//Process input from server if it gets something
        			//process user input
        			try{
        				lastLine = userInputFuture.get();
        			}catch(Exception e){
        				System.out.println("Task interrupted!");
        			}
                    if(!lastLine.equals(""))
                        processUserInput(lastLine);
        		}if(serverInputFuture.isDone()){	//Process input from server if it gets something
        			//process server input
        			doneProcessingServerInput = false;
        			userInputFuture.cancel(true);
        			String serverLineIn = "";
        			try{
        				serverLineIn = serverInputFuture.get();
        			}catch(Exception e){
        				System.out.println("Task interrupted!");
        			}
        			processServerInput(serverLineIn);
        			System.out.println("Press 'ENTER' to continue.");
        			doneProcessingServerInput = true;
        		}
        	}
        	threadPool.shutdownNow();
        	System.exit(0);
        }
    }

    public static String userInputLoop(BufferedReader userInput){
		try
		{
			System.out.print("Paper Airplanes: ");
			String line = userInput.readLine();
			return line;
		}catch(Exception e){
			e.printStackTrace();
		}return "";
	}

	public static void processUserInput(String lineIn){
		try{
            if(lineIn.contains("/add ")){
                lineIn = lineIn.replace("/add ","").trim();
                output.writeBytes(":-:roomadd " + lineIn + " " + roomID);
            }
            else if(lineIn.contains("/quit"))
            {
                    output.writeBytes(":-:roomquit "+ myID + " " + roomID);
            }
            else
            {
                try{
                    if(!(key.equals("")))
                    {
                        lineIn = crypto.encryptString(lineIn, key);
                    }
                    output.writeBytes(":-:room " + roomID + ":-:" + clientName + ":-:" + lineIn);
                }catch(Exception e)
                {
                    System.out.println("Message send error, most likely encryption failure");
                }
            }
		}catch(IOException e){
			System.out.println("IO exception");
		}
	}

	public static void processServerInput(String line){
        String[] splitThis = line.split(": ");
        try
        {
            if(!(key.equals("")))
            {
                line = crypto.decryptString(splitThis[1], key);
                System.out.println(splitThis[0]+": "+ line);
            }
            else
            {
                System.out.println(line);
            }
        }
        catch(Exception e)
        {
            System.out.println("Error decrypting message, most likely you are not using the right key");
        }
	}

	public static String serverInputLoop(BufferedReader input){
		String line = "";
		String tmpLine = "";
		try{
			line = line + input.readLine();
			while(input.ready()){
				tmpLine = input.readLine();
				line = line + "\n" + tmpLine;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return line;
	}
}
