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


public class RoomClient{

    static Socket clientSocket = null;
    static DataOutputStream output = null;
    static BufferedReader input = null;
    private static String clientName;
    private static String friendName;
    private static String serverIPAddress;
    private static int portNumber;
    private static int myChannel;
    private static int roomID;

    public static void main(String [] args){
        Scanner reader = new Scanner(System.in);
        try{
            clientName = (args[2]);
            friendName = (args[3]);
            roomID = Integer.parseInt(args[4]);
            System.out.println(clientName +": Wait for " +friendName + " to connect...");
        }catch(Exception e){
            System.exit(0);
        }

        String lastLine = "";
        System.out.println("Enter /chat if you want to accept a chat with " + friendName + ". Type /quit to exit the chat:\n");
        lastLine = reader.nextLine();
        if(lastLine.equals("/chat")){
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

            //Getting the room channel
            try{
    			output.writeBytes("/roomChannel");
    		}catch(IOException e){
    			System.out.println("IO exception");
    		}
            try{
                myChannel = Integer.parseInt(input.readLine());
            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                output.writeBytes("/setChatChannel "+roomID+" "+clientName+ " "+myChannel);
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
			System.out.print("Paper Planes: ");
			String line = userInput.readLine();
			return line;
		}catch(Exception e){
			e.printStackTrace();
		}return "";
	}

	public static void processUserInput(String lineIn){
		//System.out.println(lineIn);
		try{
			output.writeBytes("/room " + roomID + ":-:" + clientName + ":-:" + lineIn);
		}catch(IOException e){
			System.out.println("IO exception");
		}
	}

	public static void processServerInput(String line){


		System.out.println(line);

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

    public static void sendMessage(String msg)
    {

    }

    public static byte[] receiveMessage()
    {
    return null;
    }

    public static boolean sendFile(String filename)
    {
    return false;
    }

    public static byte[] encrypt(String msg)
    {
    return null;
    }

    public static String decrypt(byte[] msg)
    {
    return null;
    }
}
