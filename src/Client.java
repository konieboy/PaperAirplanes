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

public class Client
{
	static Socket clientSocket = null;
	 static DataOutputStream output = null;
	 static BufferedReader input = null;
	 static User userProfile = null;

	static String serverIPAddress;
	static int portNumber;


	 public static void launchTerminal (String userAndFriend, String commands)
     {
         try
         {
             Runtime r = Runtime.getRuntime();
			 String[] users = userAndFriend.split(" ");

			 commands += " "+ users[0] + " "  + users[2] + " " + users[3];			//users[3] is actually the roomID
			 System.out.println(commands);
 		 	 String[] cmdArray = {"gnome-terminal", "-e", commands + " ; $SHELL"};
             r.exec(cmdArray).waitFor();
         }
         catch (InterruptedException ex)
         {
             ex.printStackTrace();
         }
         catch (IOException ex)
         {
             ex.printStackTrace();
         }

     }

	public static String userInputLoop(BufferedReader userInput){
		try
		{
			System.out.print("Paper Planes: ");
			String line = userInput.readLine();
			line = line.replace(":-:userdata","");
			return line;
		}catch(Exception e){
			e.printStackTrace();
		}return "";
	}

	public static void processUserInput(String lineIn){
		//System.out.println(lineIn);
		try{
			output.writeBytes(lineIn);
		}catch(IOException e){
			System.out.println("IO exception");
		}
	}

	public static void processServerInput(String line){

		if (line.contains("User login has failed!"))
		{
			System.out.println("User login has failed!");
			System.exit(0);
		}
		else if (line.contains("/usersOnline"))
		{
			line = line.replace("/usersOnline","");
			System.out.println(line);
		}

		else if (line.contains("/request from "))
		{
			line = line.replace("/request from ","");
			System.out.println("Launching new chat room...");
			//System.exit(0);
			launchTerminal(line, "java RoomClient "+serverIPAddress + " "+portNumber);
		}
		else if (line.contains("/connect to a chat room "))
		{
			line = line.replace("/connect to a chat room ","");
			System.out.println("Launching new chat room...");
			//System.exit(0);
			String[] temp = line.split(" ");
			launchTerminal(temp[0] + " to " + userProfile.getUserName()+" "+temp[1], "java RoomClient "+serverIPAddress + " "+portNumber);
		}
		else
		{
			System.out.println(line);
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

	public static void main(String[] args)
	{

		BufferedReader  userInput = new BufferedReader(new InputStreamReader(System.in));
		ExecutorService threadPool = Executors.newFixedThreadPool(2);

        if(args.length != 2)
        {
            System.out.println("Invalid Arguments");
            System.out.println("Require <IP Address> <Port Number>");
            System.exit(0);
        }

        serverIPAddress = args[0];
        portNumber = Integer.parseInt(args[1]);

		System.out.println("Welcome to paper airplanes!");

		try
		{
			clientSocket = new Socket(serverIPAddress, portNumber); //for now we are only connecting to one computer
			output = new DataOutputStream(clientSocket.getOutputStream());
			input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		}catch (IOException e)
		{
			System.out.println(e);
		}

		//Initialize the user
		User user = new User();
		userProfile = user;

		try{
			output.writeBytes(":-:userdata "+user.login());
		}catch(Exception e){
			System.out.println("NO");
			System.exit(0);
		}
		String userName = user.getUserName();
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
