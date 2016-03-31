//CPSC 441 W2016 Group 31
//Paper Airplanes Messenger
//Brendan Petras, Ethan Hamman, Konrad Wisniewski, Kyle Sutherland

//Imports
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;

/**
 *  User class keeps track of the users profile while they are logged into
 *  the chat.  It loads the users profile from a file which is either
 *  already stored on the local computer or fetched from the server.
 *  When a user first creates an account, this will be made and a profile
 *  file will be written for them.  The data in that file will later be
 *  encrypted and uploaded to the server so they can log in from other
 *  locations.
 */
public class User{
  private String username;
  private String passwordHash;
  private int portNumber;
  private ArrayList<String> friendsList;

  //For client
  public User()
  {
      username = "";
      passwordHash = "";
      friendsList = new ArrayList<String>();

      initUser();
  }

  //For new user on Server
  public User(String username, String passwordHash)
  {
      this.username = username;
      this.passwordHash = passwordHash;
  }

  //For existing user on server
  public User(String fileName, int portNumber){
      this.portNumber = portNumber;
      //read file
      try
      {
          BufferedReader br = new BufferedReader(new FileReader(fileName));
          username = br.readLine();
          passwordHash = br.readLine();
          String friend="";
          while(br.ready())
          {
            friend = br.readLine();
            friendsList.add(friend);
          }
          br.close();
      }
      catch(IOException z)
      {
          System.out.println("User: "+z);
      }
  }

  public String export()
  {
      String info = (username + " " + passwordHash);

      return info;
  }

  //Import from a file
  // public String import()
  // {
  //     return "6545445";
  // }

  public void initUser()
  {
      System.out.print("Enter your username: ");
      Scanner userIn = new Scanner(System.in);
      username = userIn.nextLine();
      System.out.print("Enter your password: ");
      String password = userIn.nextLine();
      CryptoTools crypto = new CryptoTools();
      passwordHash = crypto.hash(password);
  }
}
