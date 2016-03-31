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
    private static ArrayList<User> usersOnline = new ArrayList<User>();
    private static ArrayList<Integer> connectedClientID = new ArrayList<Integer>();
    private static ArrayList<RoomServer> currentRooms = new ArrayList<RoomServer>();
    private static int BUFFERSIZE = 256;
    private static final String filePath = "users/";

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
                            if (bytesRecv <= 0){
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

                            //detect that the client wants to add a new user to their friendslist
                            if(line.contains("/add "))
                            {
                            	line = line.replace("/add ","");
                                System.out.println("adding "+line);

                                //add user to the friend list
                                if(addFriend(line, getUser(cchannel.socket().getPort())))
                                {
                                    sendMessage((line+" was added successfully"), cchannel, encoder);
                                }
                                else
                                {
                                    String out = line+" was not added";
                                    sendMessage(out, cchannel, encoder);
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
                                    sendMessage((line+" was removed successfully"), cchannel, encoder);
                                }
                                else
                                {
                                    String out = line+" was not removed";
                                    sendMessage(out, cchannel, encoder);
                                }
                                //printFriendList(cchannel, encoder);

                            }
                            else if (line.contains("/printfriends" ))
                            {
                                line = line.replace("/printfriends ", "");
                                String friends = printFriendList(getUser(cchannel.socket().getPort()));
                                sendMessage(friends, cchannel, encoder);
                            }
                            //Contains login information
                            else if(line.contains("/userdata"))
                            {
                                String[] up = line.split(" ");
                                if(checkUser(up[1], up[2])){
                                    System.out.println(cchannel.socket().getPort());
                                    usersOnline.add(new User(filePath + up[1]+".txt", cchannel.socket().getPort()));
                                }
                            }
                            else if(line.contains("/quit")){
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
            return true;
        }catch(FileNotFoundException e){
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

    //Takes username and password, first checks if the user exists
    //If true, passwordHash is compared
    //If false, new user file is created
    private static boolean checkUser(String username, String passwordHash){
        try{
            BufferedReader br = new BufferedReader(new FileReader(filePath+username+".txt"));
            br.readLine();
            if(passwordHash.equals(br.readLine())){
                System.out.println("Login Successful");
                return true;
            }
            else{
                System.out.println("Scum user couldn't log in");
                return false;
            }
        }catch(FileNotFoundException fe){
            try{
                Writer wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath+username+".txt"), "UTF-8"));
                wr.write(username+"\n");
                wr.write(passwordHash+"\n");
                System.out.println("Profile Created");
                wr.close();
                return true;
            }catch(IOException ie){
                return false;
            }
        }catch(IOException ioe){
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
        else
        {
            return false;
        }
    }

    //add user to the friend list
    public static boolean addFriend(String friendName, User user)
    {
        if(userExists(user.getUserName()) && !user.checkFriends(friendName) &&  userExists(friendName)){
            user.addFriend(friendName);
            return true;
        }
        else
        {
            return false;
        }
    }

    public static String printFriendList(User user)
    {
        return user.printFriends();
    }
}
