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
public class Client
{
	public static void main(String[] args)
	{
		BufferedReader  userInput = new BufferedReader(new InputStreamReader(System.in));
		String line;
		System.out.println("Welcome to paper airplanes!");
	   while (true)
		{
			try
			{
		   	System.out.print("Paper Planes: ");
				line = userInput.readLine();
				System.out.println(line);

				//check if first letter of string is a "/"
				if (line.substring(0, 1) == "/")
				{
					System.out.println("Special Command!");
				}

			}
			catch (IOException ioe)
			{
				System.out.println("Invalid Command!");
			}

		}
	}
}
