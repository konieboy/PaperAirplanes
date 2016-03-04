//CPSC 441 W2016 Group 31
//Paper Airplanes Messenger
//Brendan Petras, Ethan Hamman, Konrad Wisniewski, Kyle Sutherland

//Imports
import java.util.ArrayList;
import java.util.Scanner;

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
  private int userID;
  private String username;
  private String password;
  private String passwordHash;
  private String rsaPubKey;
  private String rsaPrivateKey;
  private ArrayList<String> friendsList;

  public User(){
      userID = -1;
      username = "";
      password = "";
      passwordHash = "";
      rsaPubKey = "";
      rsaPrivateKey = "";
      friendsList = null;

      initNewUser();
  }

  public void initNewUser(){
    //If user profile is found in directory:
    //loadSavedUser(fileToProfile)

    //Else:
    //Ask if they want to login
      //getUserFromServer(username)
    //or register
      //createNewUser();
  }

  public void createNewUser(){
      Scanner userIn = new Scanner(System.in);

      //Pick username
      System.out.print("Choose a username: ");
      username = userIn.nextLine();

      //Pick password
      System.out.print("Choose a password: ");
      password = userIn.nextLine();
      //Hash password - Implementing later

      //Generate RSA keys - Implementing later
  }

  public void loadSavedUser(String inputFile){

  }
}
