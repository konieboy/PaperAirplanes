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
  private ArrayList<String> friendsList = new ArrayList<String>();;

    //For client
    public User()
    {
        username = "";
        passwordHash = "";

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

    public String login()
    {

        String info = (username + " " + passwordHash);
        return info;
    }

    public String toString(){
        String output = username+"\n"+passwordHash+"\n";
        for(String s: friendsList){
            output = output+s+"\n";
        }
        return output;
    }

    public void addFriend(String friendName)
    {
        friendsList.add(friendName);
    }

    public void removeFriend(String friendName)
    {
        friendsList.remove(friendName);
    }

    public String printFriends(){
        //String friends = "\n*_*_*_*_*Friends List*_*_*_*_*\n";

        String friends = "\n|  ___| __(_) ___ _ __   __| |___ /\n| |_ | '__| |/ _ \\ '_ \\ / _` / __|\n|  _|| |  | |  __/ | | | (_| \\__ \\ \n|_|  |_|  |_|\\___|_| |_|\\__,_|___/ \n";

        int friendNumber = 0;
        for(String s: friendsList){
            friendNumber++;
            friends += friendNumber + ") "+s+"\n";
        }
        return friends;
    }

    public int getPortNumber(){
        return portNumber;
    }

    public String getUserName(){
        return username;
    }

    public boolean checkFriends(String friendName){
        return(friendsList.contains(friendName));
    }

    public void initUser()
    {
        boolean validLogin = false;
        String password = "";
        while (validLogin == false)
        {
            System.out.print("Enter your username: ");
            Scanner userIn = new Scanner(System.in);
            username = userIn.nextLine();
            System.out.print("Enter your password: ");
            password = userIn.nextLine();

            if (username.equals("") || password.equals("")){
                System.out.print("Username and password can not be left empty, please try again!\n");
            }else if (username.contains(" ") || password.contains(" ")){
                System.out.print("Username and password can not contain spaces, please try again!\n");
            }
            else{
                validLogin = true;
            }

         }
        CryptoTools crypto = new CryptoTools();
        passwordHash = crypto.hash(password);
    }
}
