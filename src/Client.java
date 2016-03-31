//CPSC 441 W2016 Group 31
//Paper Airplanes Messenger
//Brendan Petras, Ethan Hamman, Konrad Wisniewski, Kyle Sutherland

/* Bugs to be fixed
-In client, entering a empty string kills it all!
*/

//Imports
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 *  Client class facilitates communication with the server and
 *  allows the user to send commands to the server for starting
 *  chats
 */

/** Commands
  *  /quit 							   -- exit the program
  *  /connect "userName"               -- chat with a user from your friend list
  *  /addfriend "userName"             -- add a user to your friend list
  *  /remove "userName"		           -- delete a friend from your friend
  *  /listfriends 		               -- show friends
  */

/* Things that need to be added
   -Check that friend you want to add is also in the list of the registered users
   -check that the user you are connecting to is a registered user (done)
   -put friend list server side
   -when you register your login, details saved on server ***
*/

public class Client
{
	 static Socket clientSocket = null;
	 static DataOutputStream output = null;
	 static BufferedReader input = null;


	public static String userInputLoop(BufferedReader  userInput){
		try
		{
			System.out.print("Paper Planes: ");
			String line = userInput.readLine();
			return line;
		}catch(Exception e){
			System.out.println("Something went wrong :(");
		}return "";
	}

	public static void processUserInput(String lineIn){
		System.out.println(lineIn);
		try{
			output.writeBytes(lineIn);
		}catch(IOException e){
			System.out.println("IO exception");
		}
	}

	public static void processServerInput(String lineIn){
		if(!lineIn.equals("")){
			System.out.println(lineIn);
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
			}//line = line + "\n";
		}catch(Exception e){
			System.out.println("Something went wrong :(");
		}
		return line;
	}

	public static void startRoomClient(int roomServerID){
		//RoomClient roomClient = new RoomClient(roomServerID);
	}

	public static void main(String[] args)
	{

		String serverIPAdress;
		int portNumber;


		BufferedReader  userInput = new BufferedReader(new InputStreamReader(System.in));
		ExecutorService threadPool = Executors.newFixedThreadPool(2);

        if(args.length != 2)
        {
            System.out.println("Invalid Arguments");
            System.out.println("Require <IP Address> <Port Number>");
            System.exit(0);
        }

        serverIPAdress = args[0];
        portNumber = Integer.parseInt(args[1]);

		System.out.println("Welcome to paper airplanes!");

		try
		{
			clientSocket = new Socket(serverIPAdress, portNumber); //for now we are only connecting to one computer
			output = new DataOutputStream(clientSocket.getOutputStream());
			input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		}catch (IOException e)
		{
			System.out.println(e);
		}

		//Initialize the user
		User user = new User();
		try{
			output.writeBytes("/userdata "+user.login());
		}catch(Exception e){
			System.out.println("NO");
			System.exit(0);
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

		String lastLine = "";
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
