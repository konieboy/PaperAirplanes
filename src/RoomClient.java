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

    private static String roomClientID;
    private static String roomServerID;

    public static void main(String [] args){
      try{
          roomClientID = (args[0]);
          roomServerID = (args[1]);
          System.out.println(roomClientID +": Wait for " +roomServerID + " to connect...");
      }catch(Exception e){
          System.exit(0);
      }
      while(true);
    }

    public static void sendMessage(String msg)
    {

    }

    public static byte[] receiveMessage()
    {
    return null;
    }

    public static boolean sendFile(String filename)
    {
    return false;
    }

    public static byte[] encrypt(String msg)
    {
    return null;
    }

    public static String decrypt(byte[] msg)
    {
    return null;
    }
}
