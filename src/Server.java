//CPSC 441 W2016 Group 31
//Paper Airplanes Messenger
//Brendan Petras, Ethan Hamman, Konrad Wisniewski, Kyle Sutherland

//Imports
import java.util.ArrayList;
import java.io.*;
import java.net.*;

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

  public Server()
  {

  }

  public static void main(String argsp[])
  {
    //declare a serversocket and a client socket
    ServerSocket mainServer = null;
    Socket clientSocket = null;

    //general I/O
    String line = "";
    BufferedReader input;
    DataOutputStream output;

    //trying to create new socket, default port for Project is 3265.
    try
    {
        mainServer = new ServerSocket(3265);
    }
    catch (IOException e)
    {
        System.out.println(e);
    }

    // Create a socket object from the ServerSocket to listen and accept
    // connections.
    try
    {
      clientSocket = mainServer.accept();
      System.out.println("Client Added to Server" + clientSocket.toString());

      //Initializing I/O Buffers
      input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      output = new DataOutputStream(clientSocket.getOutputStream());

      while(!line.equals("/disconnect"))
      {
          line = input.readLine();
          System.out.println("Client: " + line);  //this will be adjusted
          output.writeBytes(line + "\n");
      }

      //close
      input.close();
      output.close();
      clientSocket.close();
      mainServer.close();
    }
    catch (IOException e)
    {
      System.out.println(e);
    }

  }

  public RoomServer startRoomServer()
  {
      return null;
  }

  public void sendMessage(String msg)
  {

  }

  public byte[] receiveMessage()
  {
      return null;
  }


}
