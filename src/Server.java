//CPSC 441 W2016 Group 31
//Paper Airplanes Messenger
//Brendan Petras, Ethan Hamman, Konrad Wisniewski, Kyle Sutherland

//Imports
import java.util.ArrayList;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.net.*;
import java.util.*;

/**
 *  Server handles all the connected clients and is responsible for
 *  creating new rooms when a user wants to start a chat.  It will
 *  also process all server commands. (ie: adding friends, starting chats)
 */
public class Server
{
  private ArrayList<String> user;
  private ArrayList<Integer> connectedClientID;
  private ArrayList<RoomServer> currentRooms;
  private static int BUFFERSIZE = 256;
  static final String filePath = "friendList.txt";

  //Return false: not in list of registeredUsers
  //Return true: in list of registeredUsers


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

  public static void addFriend(String friendName)
  {
      //check if user is already in the friend list
      if (checkFriendList(friendName))
      {
          System.out.println(friendName + " is already in your friend list. " + friendName + " was not added to your friend list!");
      }

      else if (!searchForName(friendName))
      {
          System.out.println(friendName + " is not a registered user. " + friendName + " was not added to your friend list!");
      }
      else
      {
          //add user to the friend list
          addUser(friendName);
      }
  }

  //add user to the friend list
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





  public static void printFriendList(SocketChannel cchannel,CharsetEncoder encoder)
  {
      String friendList = "";
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
          friendList = (friendList + "\n----- Friend List -----\n");
          int  friendCount = 0;
          while(scanner.hasNextLine())
          {
            friendCount++;
            String testLine = scanner.nextLine();
            System.out.println(friendCount + ") " + testLine);
            friendList = (friendList + "\n" + testLine);
          }
          oFile.close();
          scanner.close();
          sendMessage((friendList + "\n"),cchannel,encoder);
      }
      catch (IOException e)
      {
          System.out.println(e);
      }
      System.out.print("\n");


  }

  public static void main(String args[])
  {

	  if(args.length != 1)
	  {
		  System.out.println("Invalid Input");
		  System.out.println("Requires <Port Number>");
		  System.exit(0);
	  }

    // Initialize buffers and coders for channel receive and send
    int portNumber = Integer.parseInt(args[0]);
    String line = "";
    Charset charset = Charset.forName( "us-ascii" );
    CharsetDecoder decoder = charset.newDecoder();
    CharsetEncoder encoder = charset.newEncoder();
    ByteBuffer inBuffer = null;
    CharBuffer cBuffer = null;
    int bytesSent, bytesRecv;     // number of bytes sent or received
    FileInputStream infile = null;

    DataOutputStream clientWriter;




    try{
       //trying to create new socket, default port for Project is 3265.
       Selector selector = Selector.open();

      //Open new socketchannel and make it non-blocking
       ServerSocketChannel channel = ServerSocketChannel.open();
       channel.configureBlocking(false);

       //Bind to port
       InetSocketAddress isa = new InetSocketAddress(portNumber);
       channel.socket().bind(isa);

       //Register select server, wait for connection requests
       channel.register(selector, SelectionKey.OP_ACCEPT);

        // Wait for something happen among all registered sockets
        boolean terminated = false;
        while (!terminated)
        {
        	if (selector.select(500) < 0)
            {
                System.out.println("select() failed");
                System.exit(1);
            }
        Thread.sleep(100);
            // Get set of ready sockets
            Set readyKeys = selector.selectedKeys();
            Iterator readyItor = readyKeys.iterator();

            // Walk through the ready set
            while (readyItor.hasNext())
            {
                // Get key from set
                SelectionKey key = (SelectionKey)readyItor.next();

                // Remove current entry
                readyItor.remove();

                // Accept new connections, if any
                if (key.isAcceptable())
                {

                    SocketChannel cchannel = ((ServerSocketChannel)key.channel()).accept();
                    cchannel.configureBlocking(false);
                    System.out.println("Accept connection from " + cchannel.socket().toString());

                    // Register the new connection for read operation
                    cchannel.register(selector, SelectionKey.OP_READ);

                }
               else{
                    SocketChannel cchannel = (SocketChannel)key.channel();
                    if (key.isReadable()){
                        Socket socket = cchannel.socket();

                        // Open input and output streams
                        inBuffer = ByteBuffer.allocateDirect(BUFFERSIZE);
                        cBuffer = CharBuffer.allocate(BUFFERSIZE);

                        // Read from socket
                        bytesRecv = cchannel.read(inBuffer);
                        if (bytesRecv <= 0){
                            System.out.println("read() error, or connection closed");
                            key.cancel();  //deregister the socket
                            continue;
                        }

                        inBuffer.flip();      // make buffer available
                        decoder.decode(inBuffer, cBuffer, false);
                        cBuffer.flip();
                        line = cBuffer.toString();
                        System.out.print("TCP Client: " + line);

                        //////**********************************
                        //respond to client input

                        //detect that the client wants to add a new user
                        if(line.contains("/add "))
                        {


                        	line = line.replace("/add ","");
                            System.out.println("Checking to make sure that " + line + " is a registered user...");

                            //add user to the friend list
                            //cchannel.write(encoder.encode(CharBuffer.wrap("swiggity swooty/n")));
                            addFriend(line);
                            printFriendList(cchannel, encoder);

                        }

                        else if (line.contains("/printfriends" ))
                        {
                            line = line.replace("/printfriends ", "");
                            printFriendList(cchannel, encoder);


                        }
                        else if(line.contains("/remove "))
                        {
                            line = line.replace("/remove ", "");
                            removeFriend(line);
                            printFriendList(cchannel, encoder);

                        }

                        else if(line.contains("/userdata"))
                        {
                              String[] up = line.split(" ");
                              checkUser(up[1], up[2]);
                        }


                        else
                        {
                           //Not a command
                           //cchannel.write(encoder.encode(CharBuffer.wrap("Command not recognized: " + line + "end\n")));
                        }


                     }
                }
            } // end of while (readyItor.hasNext())
        } // end of while (!terminated)

        // close all connections
        Set keys = selector.keys();
        Iterator itr = keys.iterator();
        while (itr.hasNext())
        {
            SelectionKey key = (SelectionKey)itr.next();
            //itr.remove();
            if (key.isAcceptable())
                ((ServerSocketChannel)key.channel()).socket().close();
            else if (key.isValid())
                ((SocketChannel)key.channel()).socket().close();
        }
    }

    catch (Exception e) {
        System.out.println(e);
     }

  }

  public RoomServer startRoomServer()
  {
      return null;
  }

  public void sendMessage(String msg)
  {
     return;
  }

  public byte[] receiveMessage()
  {
      return null;
  }

  private static Boolean searchForName(String name) {

	  try
	  {
		  File file = new File("registeredUsers.txt");
		  Scanner input = new Scanner(file);
		  while(input.hasNextLine()) {
			  String lineFromFile = input.nextLine();
			  if (lineFromFile.equals(name))
			  {
				  return true;
			  }
		   }

	  }
	  catch (IOException e)
	    {
	        System.out.println(e);
	    }
	  return false;
  }

  private static boolean checkUser(String username, String password){
      try{
          BufferedReader br = new BufferedReader(new FileReader(username+".txt"));
          br.readLine();
          if(password.equals(br.readLine())){
              System.out.println("Login Successful");
              return true;
          }
          else{
              return false;
          }
      }catch(FileNotFoundException fe){
          try{
              Writer wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(username+".txt"), "UTF-8"));
              wr.write(username);
              wr.write(password);
              System.out.println("Profile Created");
          }catch(IOException ie){
              return false;
          }
      }catch(IOException ioe){
          return false;
      }
      return false;



  }

  // sendMessage - sends string to client through TCP socketchannel
    private static int sendMessage(String msg, SocketChannel cchannel, CharsetEncoder encoder) throws IOException
    {
        int outLen = msg.length();
        CharBuffer newcb = CharBuffer.allocate(outLen);		// allocate required space
        ByteBuffer outBuf = ByteBuffer.allocate(outLen);

        newcb.put(msg);
        newcb.rewind();
        encoder.encode(newcb, outBuf, false);
        outBuf.flip();
        int bytesSent = cchannel.write(outBuf);		// send to client
        return bytesSent;
    }
}
