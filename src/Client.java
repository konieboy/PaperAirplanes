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


    static final String filePath = "friendList.txt";

    public static Boolean isRegisteredUser(String friendName)
    {
    	try
		  {

			  BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

			  String serverInput;
			 // Send to the server
			  output.writeBytes("regestered user:" + friendName);


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

    public static void removeFriend(String friendName)
    {
      //check if is actually already added
      if (!checkFriendList(friendName))
      {
        System.out.println( friendName + " has not been added as a friend. Add friends before you remove them!");
      }

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
            oFile.close();
            scanner.close();
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
        else if (isRegisteredUser(friendName) == false)
        {
            System.out.println( friendName + " is not a registered user. " + friendName + " was not added to your friend list!");
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
	}

	public static void serverInputLoop(DataOutputStream output, BufferedReader input){

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

		//Callable objects
		Callable<String> userInputTask = () -> {
				return userInputLoop(userInput);
		};

		//Future objects
		Future<String> userInputFuture;
		Future serverInputFuture;

		String lastLine = "";

		while(!lastLine.equals("/quit")){
			userInputFuture = threadPool.submit(userInputTask);

			serverInputFuture = threadPool.submit(() -> {
				try{
					serverInputLoop(output, input); //This one in 2nd thread
				}catch(Exception e){
					System.out.println("Something went wrong :(");
				}
			});

			//while(!userInputFuture.isDone() && !serverInputFuture.isDone());
			while(!userInputFuture.isDone());

			if(userInputFuture.isDone()){
				//process user input
				try{
					lastLine = userInputFuture.get();
				}catch(Exception e){
					System.out.println("Task interuppted!");
				}
				processUserInput(lastLine);
			}if(serverInputFuture.isDone()){
				//process server input
			}
		}
		threadPool.shutdownNow();
	}
}
