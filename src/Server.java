//CPSC 441 W2016 Group 31
//Paper Airplanes Messenger
//Brendan Petras, Ethan Hamman, Konrad Wisniewski, Kyle Sutherland

//Imports
import java.util.ArrayList;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.net.*;
import java.util.*;

/**
 *  Server handles all the connected clients and is responsible for
 *  creating new rooms when a user wants to start a chat.  It will
 *  also process all server commands. (ie: adding friends, starting chats)
 */
public class Server
{

    private static String errorMessage = "";
    private static ArrayList<User> usersOnline = new ArrayList<User>();
    private static ArrayList<Integer> connectedClientID = new ArrayList<Integer>();
    private static ArrayList<RoomServer> currentRooms = new ArrayList<RoomServer>();
    private static ArrayList<RoomClientTuple> clientRooms = new ArrayList<RoomClientTuple>();
    private static int BUFFERSIZE = 256;
    private static final String filePath = "users/";
    private static final String upFilePath = "serverFiles/";
    private static int roomID = 0;
    private static int clientRoomID = 0;
    private static String host = "localhost";

    //Return false: not in list of registeredUsers
    //Return true: in list of registeredUsers



    public static void main(String args[])
    {

        if(args.length != 1)
        {
            System.out.println("Invalid Input");
            System.out.println("Requires <Port Number>");
            System.exit(0);
        }

        // Initialize buffers and coders for channel receive and send
        int portNumber = Integer.parseInt(args[0]);
        String line = "";
        Charset charset = Charset.forName( "us-ascii" );
        CharsetDecoder decoder = charset.newDecoder();
        CharsetEncoder encoder = charset.newEncoder();
        ByteBuffer inBuffer = null;
        CharBuffer cBuffer = null;
        int bytesSent, bytesRecv;     // number of bytes sent or received
        FileInputStream infile = null;

        DataOutputStream clientWriter;

        //PUBLIC ROOM INIT
        //String equals room name, caps-sensitive
        currentRooms.add(new RoomServer(++roomID, 1, "general"));
        currentRooms.add(new RoomServer(++roomID, 1, "testing"));

        try
        {
           //trying to create new socket, default port for Project is 3265.
           Selector selector = Selector.open();

          //Open new socketchannel and make it non-blocking
           ServerSocketChannel channel = ServerSocketChannel.open();
           channel.configureBlocking(false);

           //Bind to port
           InetSocketAddress isa = new InetSocketAddress(portNumber);
           channel.socket().bind(isa);

           //Register select server, wait for connection requests
           channel.register(selector, SelectionKey.OP_ACCEPT);

            // Wait for something happen among all registered sockets
            boolean terminated = false;
            while (!terminated)
            {
            	if (selector.select(500) < 0)
                {
                    System.out.println("select() failed");
                    System.exit(1);
                }
            Thread.sleep(100);
                // Get set of ready sockets
                Set readyKeys = selector.selectedKeys();
                Iterator readyItor = readyKeys.iterator();

                // Walk through the ready set
                while (readyItor.hasNext())
                {
                    // Get key from set
                    SelectionKey key = (SelectionKey)readyItor.next();

                    // Remove current entry
                    readyItor.remove();

                    // Accept new connections, if any
                    if (key.isAcceptable())
                    {

                        SocketChannel cchannel = ((ServerSocketChannel)key.channel()).accept();
                        cchannel.configureBlocking(false);
                        System.out.println("Accept connection from " + cchannel.socket().toString());

                        // Register the new connection for read operation
                        cchannel.register(selector, SelectionKey.OP_READ);

                    }
                   else{    //Reading message
                        SocketChannel cchannel = (SocketChannel)key.channel();
                        if (key.isReadable()){
                            Socket socket = cchannel.socket();

                            // Open input and output streams
                            inBuffer = ByteBuffer.allocateDirect(BUFFERSIZE);
                            cBuffer = CharBuffer.allocate(BUFFERSIZE);

                            // Read from socket
                            bytesRecv = cchannel.read(inBuffer);
                            if(bytesRecv <= 0)
                            {
                                try{
                                    System.out.println("read() error, or connection closed");
                                    User outUser = getUser(cchannel.socket().getPort());    //Write to user
                                    String toFile = outUser.toString();
                                    try
                                    {
                                        Writer wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath+outUser.getUserName()+".txt"), "UTF-8"));
                                        wr.write(toFile);
                                        wr.close();
                                    }
                                    catch(Exception f)
                                    {
                                        f.printStackTrace();
                                    }
                                    usersOnline.remove(outUser);
                                }catch(NullPointerException b)
                                {
                                    System.out.println("User not created");
                                }catch(Exception j)
                                {
                                    System.out.println("Writer error");
                                }
                                key.cancel();  //deregister the socket
                                continue;
                            }

                            inBuffer.flip();      // make buffer available
                            decoder.decode(inBuffer, cBuffer, false);
                            cBuffer.flip();
                            line = cBuffer.toString();
                            System.out.println("TCP Client: " + line);

                            //////**********************************
                            //respond to client input

                            if(line.contains("/connect "))
                            {
                                line = line.replace("/connect ","");
                                System.out.println("Connecting to user: "+line);

                                //make sure the other user is registered and in your friends list
                                if(userExists(line) && getUser(cchannel.socket().getPort()).checkFriends(line))
                                {

                                    //set up room server
                                    currentRooms.add(new RoomServer(++roomID, 0, getUser(cchannel.socket().getPort()), getUserFromName(line)));
                                    Boolean onlineFlag = false;
                                    User friend;

                                    //make sure friend is online before sending chat request
                                    for(int i = 0; i < usersOnline.size(); i++)
                                    {
                                        if(usersOnline.get(i).getUserName().equals(line))
                                        {
                                            onlineFlag = true;
                                            friend = usersOnline.get(i);
                                            String msg = "/request from " + getUser(cchannel.socket().getPort()).getUserName() + " to " + line + " " + roomID + " "+(++clientRoomID)+"\n";
                                            System.out.println(msg);
                                            sendMessage(msg, friend.getCChannel(), encoder);
                                        }
                                    }

                                    if(!onlineFlag)
                                    {
                                        System.out.println("User is not online");
                                    }

                                    //look up the friend in the onlineUser ArrayList
                                    //if he is there then send a message to his client

                                    //send message to client to open up new terminal window with clientroom
                                    clientRoomID++;
                                    sendMessage(("/connect to a chat room " + line + " "+ roomID+ " "+ (clientRoomID)+"\n"), cchannel, encoder);

                                    //String myScript = "java Client";
                                        //launchTerminal(("Chat with " + line), "java Client");
                                    //send request to the other user
                                }

                                else
                                {
                                    System.out.println(line + " is not in your friends list!");
                                    sendMessage((line + " is not in your friends list!\n"), cchannel, encoder);

                                }
                            }
                            //detect that the client wants to add a new user to their friendslist

                            else if(line.contains("/sendMessage "))
                            {

                                Boolean onlineFlag = false;
                                User friend;
                                line = line.replace("/sendMessage ","");

                                for(int i = 0; i < usersOnline.size(); i++)
                                {
                                    if(usersOnline.get(i).getUserName().equals(line))
                                    {
                                        onlineFlag = true;
                                        friend = usersOnline.get(i);
                                        String msg = line;
                                        System.out.println(msg);
                                        sendMessage(msg, friend.getCChannel(), encoder);
                                    }
                                }

                            }

                            else if(line.contains("/add "))
                            {
                            	line = line.replace("/add ","");
                                System.out.println("adding "+line);

                                //add user to the friend list
                                if(addFriend(line, getUser(cchannel.socket().getPort())))
                                {
                                    //friend was added successfully
                                    sendMessage((line+" was added successfully\n"), cchannel, encoder);
                                }
                                else
                                {
                                    //friend was not added successfully
                                    sendMessage((errorMessage +"\n"), cchannel, encoder);
                                    errorMessage ="";
                                    //sendMessage((line+" was not added\n"), cchannel, encoder);
                                }
                                //printFriendList(cchannel, encoder);

                            }
                            else if(line.contains("/remove "))
                            {
                            	line = line.replace("/remove ","");
                                System.out.println("removing "+line);

                                //add user to the friend list
                                if(removeFriend(line, getUser(cchannel.socket().getPort())))
                                {
                                    //friend was removed successfully
                                    sendMessage((line + " has been successfully removed from your friends list.\n"), cchannel, encoder);
                                }
                                else
                                {
                                    //friend was not removed successfully
                                    sendMessage((errorMessage +"\n"), cchannel, encoder);
                                    errorMessage ="";
                                }
                                //printFriendList(cchannel, encoder);

                            }
                            else if(line.contains("/friends" ))
                            {
                                String friends = printFriendList(getUser(cchannel.socket().getPort()));
                                sendMessage(friends, cchannel, encoder);
                            }
                            else if(line.contains("/online" ))
                            {
                                String online = printOnlineUsers();
                                sendMessage(online, cchannel, encoder);
                            }
                            //Joining a public chat
                            else if(line.contains("/public "))
                            {
                                int roomID = -1;
                                line = line.replace("/public ","");
                                try{
                                    for(RoomServer r: currentRooms)
                                    {
                                        if(r.getRoomName().equals(line));
                                        {
                                            roomID = r.getRoomID();
                                            r.addUser(getUser(cchannel.socket().getPort()), 0);
                                        }
                                    }
                                    if(!(roomID==-1))
                                    {
                                        clientRoomID++;
                                        sendMessage(("/connect to a chat room " + line + " "+ roomID+ " "+ (clientRoomID)+"\n"), cchannel, encoder);
                                    }
                                    else
                                    {
                                        System.out.println("No room found under name: "+line);
                                        sendMessage("No room found under name: "+line+"\n", cchannel, encoder);
                                    }
                                }catch(Exception j)
                                {
                                    System.out.println("Error joining public server");
                                    sendMessage("Error joining public server\n", cchannel, encoder);
                                }
                            }
                            //Create a new public room
                            else if(line.contains("/makepublic "))
                            {
                                line = line.replace("/makepublic ","");
                                boolean exists = false;
                                for(RoomServer r: currentRooms)
                                {
                                    if(r.getRoomName().equals(line))
                                    {
                                        exists = true;
                                    }
                                }
                                if(!(line.equals("") || line.equals(" ") || exists==true))
                                {
                                    currentRooms.add(new RoomServer(++roomID, 1, line));
                                }
                            }
                            //Contains login information
                            else if(line.contains(":-:userdata"))
                            {
                                String[] up = line.split(" ");
                                if(checkUser(up[1], up[2]))
                                {
                                    System.out.println(cchannel.socket().getPort());
                                    usersOnline.add(new User(filePath + up[1]+".txt", cchannel.socket().getPort(), cchannel));
                                }
                                else
                                {
                                    sendMessage("User login has failed!\n", cchannel, encoder);
                                }
                            }
                            else if(line.contains(":-:room ")){
                                line = line.replace(":-:room ","");
                                String[] deets = line.split(":-:");         //RoomID ClientName Message
                                int[] theseIDs = new int[usersOnline.size()];
                                for(RoomServer r: currentRooms)
                                {
                                    if(r.getRoomID() == Integer.parseInt(deets[0]))
                                    {
                                        theseIDs = r.getClientIDs();
                                    }
                                }
                                SocketChannel aChannel = null;
                                for(int i=0;i<theseIDs.length;i++)
                                {
                                    for(RoomClientTuple r: clientRooms)
                                    {
                                        if(r.getRoomID() == theseIDs[i])
                                        {
                                            aChannel = r.getCChannel();
                                        }
                                    }
                                    sendMessage((""+deets[1]+": "+deets[2]+"\n"), aChannel, encoder);
                                }
                            }
                            else if(line.contains(":-:roomadd ")){
                                line = line.replace(":-:roomadd ","");
                                String[] addThis = line.split(" ");
                                //make sure friend is online before sending chat request
                                User friend = null;
                                for(int i = 0; i < usersOnline.size(); i++)
                                {
                                    if(usersOnline.get(i).getUserName().equals(addThis[0]))
                                    {
                                        friend = usersOnline.get(i);
                                        clientRoomID++;
                                        String msg = "/request from " + addThis[1] + " to " + line + " " + addThis[1] + " "+(clientRoomID)+"\n";
                                        System.out.println(msg);
                                        sendMessage(msg, friend.getCChannel(), encoder);
                                    }
                                }
                                for(RoomServer r: currentRooms){
                                    if(r.getRoomID() == Integer.parseInt(addThis[1])){
                                        r.addUser(friend, 0);
                                    }
                                }
                            }
                            else if(line.contains(":-:roomquit "))
                            {
                                line = line.replace(":-:roomquit ","");
                                String[] quitSplit = line.split(" ");           //ClientID, roomID
                                try{
                                    for(RoomServer s: currentRooms)
                                    {
                                            if(s.getRoomID() == Integer.parseInt(quitSplit[1]))
                                            {
                                                s.removeUser(Integer.parseInt(quitSplit[0]));
                                            }
                                    }
                                }
                                catch(Exception q)
                                {
                                    System.out.println("User not removed");
                                }
                            }
                            else if(line.contains(":-:roomChannel ")){
                                line = line.replace(":-:roomChannel ","");
                                String[] splits = line.split(" ");
                                clientRooms.add(new RoomClientTuple(Integer.parseInt(splits[0]), cchannel));
                            }
                            else if(line.contains(":-:setChatChannel ")){
                                line = line.replace(":-:setChatChannel ","");
                                String[] roomUserInfo = line.split(" ");
                                for(RoomServer r: currentRooms){
                                    if(r.getRoomID() == Integer.parseInt(roomUserInfo[0])){
                                        r.initUserClientID(roomUserInfo[1], Integer.parseInt(roomUserInfo[2]));
                                    }
                                }
                            }
                            else if (line.contains("/get ")){
                                //Get filename from client
                                line = line.replace("/get ","");
                                String filename = upFilePath + line;
                                try{
                                    infile = new FileInputStream(filename.trim());
                                }catch(Exception e){
                                    sendMessage("Error in opening file " + filename, cchannel, encoder);
                                }
                                try{
                                    sendMessage("/file "+line + "\n", cchannel, encoder);
                                    byte[] msg = new byte[infile.available()];
                                    int read_bytes = infile.read(msg);
                                    //Send message size to client
                                    sendMessage((Integer.toString(read_bytes) +":-:" + Arrays.toString(msg)+  '\n'), cchannel, encoder);
                                    //sent message data to client
                                    System.out.println("File sent");
                                }catch(Exception e){
                                   System.out.println("File error");
                                   sendMessage("File error\n", cchannel, encoder);
                                }
                           }
                           else if(line.contains("/upload ")){
                               line = line.replace("/upload ","");
                               try{
                   				//Get message size from server
                   				String[] tempStrings = line.split(":-:");
                   				int size = Integer.parseInt(tempStrings[1]);
                                String filename = upFilePath+tempStrings[0];
                   				System.out.println("File Size is: "+ size);
                   				//Get file data from server
                   				tempStrings  = tempStrings[2].replace("[","").replace("]","").trim().split(", ");
                   				byte[] msg = new byte[size];
                   				for(int i=0; i<size;i++){
                   		 			msg[i] = Byte.parseByte(tempStrings[i]);
                   				}
                   				//Write file to disk
                   				FileOutputStream outFile = new FileOutputStream(filename);
                   				outFile.write(msg);
                   				outFile.close();

                   				System.out.println(""+filename+" has been downloaded.");
                    			}catch(Exception e){
                    				System.out.println("Problem getting file");
                                    sendMessage("Poblem getting file\n",cchannel, encoder);
                    			}
                            }
                            else if(line.contains("/files")){                   //Executes ls, then reads the temp file and prints the output to the server
                               try{
                                  Runtime.getRuntime().exec(new String[]{"bash","-c","ls serverFiles > /tmp/tmp.txt"});
                               }catch(Exception e){
                                  System.out.println("Error executing ls");
                                  sendMessage("Error executing ls\n", cchannel, encoder);
                               }
                               Thread.sleep(100);
                               //Reading and sending file
                               try{
                                  FileReader fr = new FileReader("/tmp/tmp.txt");
                                  BufferedReader br = new BufferedReader(fr);
                                  String bLine = br.readLine();
                                  sendMessage("\nFiles:\n", cchannel, encoder);
                                  while(bLine != null){
                                     sendMessage(bLine + '\n', cchannel, encoder);
                                     bLine = br.readLine();
                                  }
                               }catch(IOException e){
                                  System.out.println("Error while reading the list");
                                  sendMessage("Error while reading the list\n", cchannel, encoder);
                               }
                            }
                            else if(line.contains("/quit")){
                                try{
                                    User outUser = getUser(cchannel.socket().getPort());
                                    String toFile = outUser.toString();
                                    try
                                    {
                                        Writer wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath+outUser.getUserName()+".txt"), "UTF-8"));
                                        wr.write(toFile);
                                        wr.close();
                                    }
                                    catch(Exception f)
                                    {
                                        f.printStackTrace();
                                    }
                                    usersOnline.remove(outUser);
                                }catch(Exception l){
                                    System.out.println("");
                                }
                            }
                            else
                            {
                               //Not a command
                               //cchannel.write(encoder.encode(CharBuffer.wrap("Command not recognized: " + line + "end\n")));
                            }
                        }
                    }
                } // end of while (readyItor.hasNext())
            } // end of while (!terminated)

            // close all connections
            Set keys = selector.keys();
            Iterator itr = keys.iterator();
            while (itr.hasNext())
            {
                SelectionKey key = (SelectionKey)itr.next();
                //itr.remove();
                if (key.isAcceptable())
                    ((ServerSocketChannel)key.channel()).socket().close();
                else if (key.isValid())
                    ((SocketChannel)key.channel()).socket().close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public RoomServer startRoomServer()
    {
        return null;
    }

    //Check if a user exists at all
    public static boolean userExists(String username){
        try{
            FileReader fr = new FileReader(filePath+username+".txt");
            fr.close();
            return true;
        }catch(FileNotFoundException e){
            return false;
        }catch(Exception m){
            return false;
        }
    }

    //If returns null user is not online
    public static User getUser(int portNumber)
    {
        for(User u: usersOnline)
        {
            if(u.getPortNumber() == portNumber)
            {
                return u;
            }
        }
        return null;
    }

    public static User getUserFromName(String username)
    {
        for(User u: usersOnline)
        {
            if(u.getUserName().equals(username))
            {
                return u;
            }
        }
        return null;
    }

    public static String printOnlineUsers()
    {
        String users = "/usersOnline" + "\n";
        for(User u: usersOnline)
        {
            users += u.getUserName() + "\n";
        }
        return users;
    }

    //Takes username and password, first checks if the user exists
    //If true, passwordHash is compared
    //If false, new user file is created
    private static boolean checkUser(String username, String password){

        try{
            //user exists
            CryptoTools ct = new CryptoTools();
            BufferedReader br = new BufferedReader(new FileReader(filePath+username+".txt"));
            br.readLine();
            if(ct.verifyPassword(password, br.readLine())){
                System.out.println("Login Successful");
                br.close();
                return true;
            }
            //user password is wrong
            else{
                System.out.println("User couldn't log in");
                br.close();
                return false;
            }
        //the user does not exist
        }catch(FileNotFoundException fe){
            try{
                CryptoTools ct = new CryptoTools();
                Writer wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath+username+".txt"), "UTF-8"));
                wr.write(username+"\n");
                wr.write(ct.hashPassword(password)+"\n");
                System.out.println("Profile Created");
                wr.close();
                return true;
            }catch(IOException ie){
                return false;
            }
            catch(Exception e)
            {
                e.printStackTrace();
                return false;
            }
        }catch(IOException ioe){
            return false;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    // sendMessage - sends string to client through TCP socketchannel
    private static int sendMessage(String msg, SocketChannel cchannel, CharsetEncoder encoder) throws IOException
    {
        int outLen = msg.length();
        CharBuffer newcb = CharBuffer.allocate(outLen);		// allocate required space
        ByteBuffer outBuf = ByteBuffer.allocate(outLen);

        newcb.put(msg);
        newcb.rewind();
        encoder.encode(newcb, outBuf, false);
        outBuf.flip();
        int bytesSent = cchannel.write(outBuf);		// send to client
        return bytesSent;
    }

    //Self explanatory, removes friend from users friend list
    public static boolean removeFriend(String friendName, User user)
    {
        if(userExists(user.getUserName()) && user.checkFriends(friendName)){
            user.removeFriend(friendName);
            return true;
        }
        else if(!userExists(friendName))
        {
            errorMessage += (friendName + " is not a registered user. " + friendName +" was not removed from your friends list!");
        }
        else if(!user.checkFriends(friendName))
        {
            errorMessage += (friendName + " is not in your list of friends. " + friendName +" was not removed from your friends list!");
        }
        return false;
    }

    //add user to the friend list
    public static boolean addFriend(String friendName, User user)
    {
        if(!user.checkFriends(friendName) &&  userExists(friendName))
        {
            user.addFriend(friendName);
            return true;
        }
        else if(!userExists(friendName))
        {
            errorMessage += (friendName + " is not a registered user. " + friendName +" was not added to your friends list!");
        }
        else if(user.checkFriends(friendName))
        {
            errorMessage += (friendName + " is already on your list of friends. " + friendName +" was not added to your friends list!");
        }

        return false;

    }

    public static String printFriendList(User user)
    {
        return user.printFriends();
    }

    static class RoomClientTuple{

        int roomClientID;
        SocketChannel cchannel;

        public RoomClientTuple(int roomClientID, SocketChannel cchannel){
            this.roomClientID = roomClientID;
            this.cchannel = cchannel;
        }

        public int getRoomID(){
            return roomClientID;
        }

        public SocketChannel getCChannel(){
            return cchannel;
        }
    }
}
