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
   -
*/

public class Client
{
    static final String filePath = "friendList.txt";

    public static void removeFriend(String friendName)
    {
      //check if is actually already added
      if (!checkFriendList(friendName))
      {
        System.out.println( friendName + " has not been added as a friend. Add friends before you remove them!");
      }


      /********NEEDS TO CHECK IF FRIEND IS A REGESTERED USER!!!!!!!*/


      else
      {
          //remove user from the friend list
          System.out.println( friendName + " has been found in your friend list...");
          removeUser(friendName);
          System.out.println( friendName +  " has successfully removed from your friend list..." );
          printFriendList();
      }
    }

    public static void removeUser(String friendName)
    {
      try
      {
      	File friendList = new File(filePath);
      	File tempFriendList = new File("temporaryFriendList.txt");
      	BufferedReader reader = new BufferedReader(new FileReader(friendList));
      	BufferedWriter writer = new BufferedWriter(new FileWriter(tempFriendList));

      	String lineToRemove = friendName;
      	String currentLine;

      	while((currentLine = reader.readLine()) != null)
        {
      	    // trim newline when comparing with lineToRemove
      	    String trimmedLine = currentLine.trim();
      	    if(trimmedLine.equals(lineToRemove)) continue;
      	    writer.write(currentLine + System.getProperty("line.separator"));
      	}
      	writer.close();
      	reader.close();
      	tempFriendList.renameTo(friendList);
      }
      catch (IOException e)
      {
          System.out.println(e);
      }


    }
    public static void printFriendList()
    {
        File friendFile = new File(filePath);
        if(!friendFile.exists())
        {
          try
          {
            friendFile.createNewFile();
          }
          catch (IOException e)
          {
            System.out.println(e);
          }
        }

        try
        {
            FileOutputStream oFile = new FileOutputStream(filePath, true);
            Scanner scanner = new Scanner(friendFile);
            System.out.println("\n----- Friend List -----");
            int  friendCount = 0;
            while(scanner.hasNextLine())
            {
              friendCount++;
              String testLine = scanner.nextLine();
              System.out.println(friendCount + ") " + testLine);
            }
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
        System.out.print("\n");
    }

    public static void addFriend(String friendName)
    {
        //check if user is already in the friend list
        if (checkFriendList(friendName))
        {
            System.out.println( friendName + " is already in your friend list. " + friendName + " was not added to your friend list!");
        }
        else
        {
            //add user to the friend list
            addUser(friendName);
            printFriendList();
        }
    }

    public static void addUser(String friendName)
    {
        try
        {
            Writer output;
            output = new BufferedWriter(new FileWriter(filePath, true));
            output.append(friendName + "\n");
            output.close();
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
    }

    //False: friend not found in the list
    //True: friend already added to list
    public static boolean checkFriendList(String friendName)
    {
        File friendFile = new File(filePath);
        if(!friendFile.exists()) {
             try
             {
                friendFile.createNewFile();
             }
             catch (IOException e)
             {
               System.out.println(e);
             }
        }
        try
        {
            FileOutputStream oFile = new FileOutputStream(filePath, true);
        }
        catch (IOException e)
        {
            System.out.println(e);
        }

        //read through the friend list to search a duplicate name
        try
        {

                Scanner scanner = new Scanner(friendFile);
                while(scanner.hasNextLine())
                {
                      String testLine = scanner.nextLine();
                      if (testLine.equals(friendName))
                      {
                          //matching name found, warn user
                          return true;
                      }
                }

        }
        catch (FileNotFoundException e)
        {
            System.out.println(e);
        }
        //no matching name found, add friend
        return false;
    }

	public static void main(String[] args)
	{
    Socket clientSocket;
    DataOutputStream output;
    BufferedReader input;

		BufferedReader  userInput = new BufferedReader(new InputStreamReader(System.in));
		String line = "";
		System.out.println("Welcome to paper airplanes!");
		
		
      //Initialize the user
      User user = new User();

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
						/*connect*/
 						if((command[0].toLowerCase()).equals("connect"))
						{
              try
              {
                  clientSocket = new Socket("localhost", 3265); //for now we are only connecting to one computer
                  output = new DataOutputStream(clientSocket.getOutputStream());
                  input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
              }
              catch (IOException e)
              {
                  System.out.println(e);
              }
						}

				    /*quit*/
  					if((command[0].toLowerCase()).equals("quit") || (command[0].toLowerCase()).equals("exit") || (command[0].toLowerCase()).equals("q") )
						{
							//Exits the program
							System.out.println("Quiting Paper Planes...");
							System.exit(0);
						}

						/*add a friend*/
						if((command[0].toLowerCase()).equals("addfriend") || (command[0].toLowerCase()).equals("add") || (command[0].toLowerCase()).equals("af") )
						{
              //check to see that user added an arg
              if (command.length <= 1)
              {
                System.out.println("Please add a user name after your '/add' command.");
              }
              else
              {
                //Adds a friend to your friend list
  							System.out.println("adding friend: " + command[1]);
                addFriend(command[1]);
              }
						}

            /*list friends*/
						if((command[0].toLowerCase()).equals("printfriends") || (command[0].toLowerCase()).equals("listfriends") || (command[0].toLowerCase()).equals("lf") )
						{
              printFriendList();
						}

            /*remove a friend*/
						if((command[0].toLowerCase()).equals("r") || (command[0].toLowerCase()).equals("remove") || (command[0].toLowerCase()).equals("delete") )
						{
              if (command.length <= 1)
              {
                System.out.println("Please add a user name after your '/remove' command.");
              }
              else
              {
                removeFriend(command[1]);
              }
						}
            else
            {
              System.out.println("Command was not recognized. Please consider your syntax or spelling. Refer to the readme for a list of commands.");
            }
					}
					else System.out.println("Too many arguments!");
				}
        else
        {
          System.out.println("Command was not recognized. Please consider your syntax or spelling. Refer to the readme for a list of commands.");
        }

			}
			catch (IOException ioe)
			{
				System.out.println("Invalid Command!");
			}

		}
	}

}
