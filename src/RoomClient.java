//CPSC 441 W2016 Group 31
//Paper Airplanes Messenger
//Brendan Petras, Ethan Hamman, Konrad Wisniewski, Kyle Sutherland

//Imports


/**
 *  RoomClient is a part of the client that manages all of the chat
 *  for a specific room on the chat server.  All user commands in
 *  this window are specific to the current room.
 */
public class RoomClient{

  private int roomClientID;
  private int roomServerID;

  public static void main(String [] args){
      
  }

  public RoomClient(int roomServerIDIn)
  {
      roomServerID = roomServerIDIn;
  }

  public void sendMessage(String msg)
  {

  }

  public byte[] receiveMessage()
  {
    return null;
  }

  public boolean sendFile(String filename)
  {
    return false;
  }

  public byte[] encrypt(String msg)
  {
    return null;
  }

  public String decrypt(byte[] msg)
  {
    return null;
  }
}
