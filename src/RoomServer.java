//CPSC 441 W2016 Group 31
//Paper Airplanes Messenger
//Brendan Petras, Ethan Hamman, Konrad Wisniewski, Kyle Sutherland

//Imports
import java.util.*;

/**
 *  RoomServer is created when a new chat room is created and it handles
 *  the chat between users in the specific room.  It also tracks which
 *  users are in the chat.
 */
public class RoomServer
{
    private int roomID;
    private int roomType; // 0 = private; 1 = public
    private ArrayList<tuple> userList = new ArrayList<tuple>();

    public RoomServer(int roomID,int roomType, User user, User friend)
    {
        this.roomID = roomID;
        this.roomType = roomType;
        userList.add(new tuple(user,0));
        userList.add(new tuple(friend,0));
    }

    public void initUserClientID(String username, int userClientID)
    {
        for(tuple t: userList){
            if(username.equals(t.getName()))
            {
                t.setClientID(userClientID);
            }
        }
    }

    public int[] getClientIDs()
    {
        int[] clientID = new int[userList.size()];
        int i=0;
        for(tuple t: userList)
        {
            clientID[i] = t.getClientID();
            i++;
        }
        return clientID;
    }
    public void closeRoomServer()
    {

    }

    public int getRoomID(){
        return roomID;
    }

    public byte[] receiveMessage()
    {
      return null;
    }

    public byte[] receiveFile()
    {
        return null;
    }

    public void forwardMessage(byte[] msg)
    {

    }

    public void forwardFile(String filename)
    {

    }
}

class tuple{
    private User user;
    private int clientID;

    public tuple(User user, int clientID){
        this.user = user;
        this.clientID = clientID;
    }

    public User getUser(){
        return user;
    }

    public String getName(){
        return user.getUserName();
    }

    public int getClientID(){
        return clientID;
    }

    public void setClientID(int clientID){
        this.clientID = clientID;
    }

}
