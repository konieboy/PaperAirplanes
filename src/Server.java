//CPSC 441 W2016 Group 31
//Paper Airplanes Messenger
//Brendan Petras, Ethan Hamman, Konrad Wisniewski, Kyle Sutherland

//Imports
import java.util.ArrayList;

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
