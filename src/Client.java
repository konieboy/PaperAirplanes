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
  *  /printfriends 		               -- show friends
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


    static final String filePath = "friendList.txt";

    /*public static Boolean isRegisteredUser(String friendName)
    {
    	try
		  {

			  BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

			  String serverInput;
			 // Send to the server
			  output.writeBytes("/add " + friendName);


			  // Getting response from the server
			  serverInput = input.readLine();
			  System.out.println("Server: " + serverInput);

			  // Close the socket
			  //clientSocket.close();
			  if (serverInput.contains("false"))
			  {
				  return false;
			  }
    		  else
    		  {
    			  return true;
    		  }

		    }
		  catch (IOException e)
		  {
		      System.out.println(e);
		  }
    	return null;
    }
	*/

	private static boolean processUserInput(String line){
		if (line.substring(0, 1).equals("/"))
		{
			//Split line into an array for easy parsing
			//gets rid of spaces and special chars
			String[] command = line.split("\\s+");  //space
			command[0] = command[0].replaceAll("[^\\w]", "");

			//System.out.println(command[0] + command[1]);
			//make sure that there is at most one argument
			if (command.length <= 2)
			{
				/*connect*/
				if((command[0].toLowerCase()).equals("connect"))
				{
					return true;
				}
				/*quit*/
			  	if((command[0].toLowerCase()).equals("quit"))
				{
					//Exits the program
					System.out.println("Quiting Paper Planes...");
					System.exit(0);
				}

				/*add a friend*/
				if((command[0].toLowerCase()).equals("addfriend"))
				{
			  //check to see that user added an arg
					if (command.length <= 1)
				  	{
						System.out.println("Please add a user name after your '/add' command.");
						return false;
				  	}
				  	else
				  	{
						//Adds a friend to your friend list
						return true;
				  	}
				}

			/*list friends*/
				if((command[0].toLowerCase()).equals("printfriends"))
				{
			  		return true;
				}

			/*remove a friend*/
				if((command[0].toLowerCase()).equals("remove"))
				{
			  		if (command.length <= 1)
			  		{
						System.out.println("Please add a user name after your '/remove' command.");
						return false;
			  		}
			  		else
			  		{
						return true;
				  	}
				}
			}
			else
			{
				System.out.println("Too many arguments!");
				return false;
			}
		}
		return false;
	}

	public static void main(String[] args)
	{

        Socket clientSocket;
        DataOutputStream output = null;
        BufferedReader input;

		String serverIPAdress;
		int portNumber;


		BufferedReader  userInput = new BufferedReader(new InputStreamReader(System.in));

        if(args.length != 2)
        {
            System.out.println("Invalid Arguments");
            System.out.println("Require <IP Address> <Port Number>");
            System.exit(0);
        }

        serverIPAdress = args[0];
        portNumber = Integer.parseInt(args[1]);

		String line = "";
		System.out.println("Welcome to paper airplanes!");

	  try
	  {
		  //userdata
		  clientSocket = new Socket(serverIPAdress, portNumber); //for now we are only connecting to one computer
		  output = new DataOutputStream(clientSocket.getOutputStream());
		  input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		  }
		  catch (IOException e)
		  {
		      System.out.println(e);
			  System.exit(0);
		  }



    	//Initialize the user
       User user = new User();

	   //Send user info to server
	   String export = "/userdata "+ user.export();
	   try{
	   		output.writeBytes(export);
		}
		catch(Exception e){
			System.out.println("Can't write");
		}

	   while (!line.equals("/quit"))
		{
            try
			{
		   		System.out.print("Paper Planes: ");
				line = userInput.readLine();
				System.out.println(line);
				if (processUserInput(line));
					{

					}

			}
			catch (IOException ioe)
			{
				System.out.println("Invalid Command!");
			}

		}
	}
}
