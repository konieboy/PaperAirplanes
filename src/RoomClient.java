//CPSC 441 W2016 Group 31
//Paper Airplanes Messenger
//Brendan Petras, Ethan Hamman, Konrad Wisniewski, Kyle Sutherland

//Imports
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;


/**
 *  RoomClient is a part of the client that manages all of the chat
 *  for a specific room on the chat server.  All user commands in
 *  this window are specific to the current room.
 */


public class RoomClient{

    private static String clientName;
    private static String friendName;

    public static void main(String [] args){
        Scanner reader = new Scanner(System.in);
      try{
          clientName = (args[0]);
          friendName = (args[1]);
          System.out.println(clientName +": Wait for " +friendName + " to connect...");
      }catch(Exception e){
          System.exit(0);
      }

      String lastLine = "";
      System.out.println("Enter /chat if you to accept a chat with " + friendName + ". Type /exit to exit the chat:\n");
      while(!lastLine.equals("/quit"))
      {
          lastLine = reader.nextLine();
      }
      System.exit(0);

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
