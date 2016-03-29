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

      initUser();
  }

  public String export(){
      String info = ("" + userID + "\n" + username + "\n" + password + "\n" + passwordHash + "\n" + rsaPubKey + "\n" + rsaPrivateKey + "\n");

      for(int i = 0; i < friendsList.length; i++)
      {
          info = info + friendsList[i] + "\n";
      }

      return info;
  }

  public void initUser(){
    //Ask if they want to Create new user or load profile
    //If user profile is found in directory:
    //loadSavedUser(fileToProfile)

    System.out.print("(C)reate new user or (L)oad user profile: ");
    Scanner input = new Scanner(System.in);
    String option = input.nextLine().toLowerCase();

    switch(option){
      case "c": createNewUser();
               break;
      case "l": loadSavedUser();
               break;
   }

  }

  public void createNewUser(){
      Scanner userIn = new Scanner(System.in);

      //Pick username
      if(username.equals("")){
         System.out.print("Choose a username: ");
         username = userIn.nextLine();
      }
      String fileName = username+".txt";

      //Pick password
      System.out.print("Choose a password: ");
      password = userIn.nextLine();
      //Hash password - Implementing later
      try{
         Writer wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
         wr.write(username + "\n");
         wr.write(password + "\n");
         wr.write(passwordHash + "\n");
         wr.write(userID + "\n");
         wr.write(rsaPubKey + "\n");
         wr.write(rsaPrivateKey + "\n");

         wr.close();
      }catch(IOException e){
         return;
      }


      //Generate RSA keys - Implementing later
  }

  /*
      The server will need to keep the login file
      Right now it is a plaintext file in the src folder
  */
  public void loadSavedUser(){
     Scanner userIn = new Scanner(System.in);

     System.out.print("Enter your username: ");
     username = userIn.nextLine();
     String fileName = (username + ".txt");

     System.out.print("Enter your password: ");
     String checkPass = userIn.nextLine();
      //Open file, if not there, create a new user
      //If file exists, read file and set variables
      try{
         BufferedReader br = new BufferedReader(new FileReader(fileName));

         /*temporary file setup
         "username"
         "password"
         "passwordHash"
         "userID"
         "Pubkey"
         "PrivKey"

         */
         username = br.readLine();
         password = br.readLine();
         passwordHash = br.readLine();
         userID = Integer.parseInt(br.readLine());
         rsaPubKey = br.readLine();
         rsaPrivateKey = br.readLine();

      }catch(FileNotFoundException e){
         System.out.println("No user profile saved, creating new user.");
         createNewUser();
         return;
      }catch(IOException e){
         System.out.println("Error reading file: " + fileName);
         return;
      }

      //Password verification
      if(!password.equals(checkPass)){
         for(int i =0; i<3;i++){
            System.out.print("Wrong password, "+ (3-i)+ " tries left: ");
            checkPass = userIn.nextLine();
            if(password.equals(checkPass)){
               break;
            }
            if(i==2){
               System.out.println("You have incorrectly entered your password too many times.");
               System.exit(0);
            }
         }

      }
  }
}
