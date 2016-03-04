//CPSC 441 W2016 Group 31
//Paper Airplanes Messenger
//Brendan Petras, Ethan Hamman, Konrad Wisniewski, Kyle Sutherland

//Imports
import java.io.*;

/**
 *  Client class facilitates communication with the server and
 *  allows the user to send commands to the server for starting
 *  chats
 */


/** Commands
  *  /quit 							   -- exit the program
  *  /connect "userName"   -- chat with a user from your friend list
  *  /addfriend "userName" -- add a user to your friend list
	*  /remove "userName"		 -- delete a friend from your friend
  */

public class Client
{
	public static void main(String[] args)
	{
		BufferedReader  userInput = new BufferedReader(new InputStreamReader(System.in));
		String line = "woah!";
		System.out.println("Welcome to paper airplanes!");
	   while (!line.equals("/quit"))
		{
			try
			{
		   	System.out.print("Paper Planes: ");
				line = userInput.readLine();
				System.out.println(line);

				//check if first letter of string is a "/"
				if (line.substring(0, 1).equals("/"))
				{
					System.out.println("Special Command!");

					//Split line into an array for easy parsing
					//gets rid of spaces and special chars
					String[] command = line.split("\\s+");
					for (int i = 0; i < command.length; i++)
					{
						command[i] = command[i].replaceAll("[^\\w]", "");
					}
					//System.out.println(command[0] + command[1]);
					//make sure that there is at most one argument
					if (command.length <= 2)
					{
						switch (command[0].toLowerCase())
						{
							case "connect": //do something
								//connect "username"
								break;

							default: //do something else
							  System.out.print("Invalid Command");
								break;
						}
					}

					else System.out.print("Invalid Command");
					///connect command




					//user.addFriend
				}

			}
			catch (IOException ioe)
			{
				System.out.println("Invalid Command!");
			}

		}
	}
}
