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
  private ArrayList<String> user;
  private ArrayList<Integer> connectedClientID;
  private ArrayList<RoomServer> currentRooms;
  private static int BUFFERSIZE = 256;

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

    try{
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
                    System.out.println("Accept conncection from " + cchannel.socket().toString());

                    // Register the new connection for read operation
                    cchannel.register(selector, SelectionKey.OP_READ);
                   
                }
               else{
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
                            key.cancel();  //deregister the socket
                            continue;
                        }

                        inBuffer.flip();      // make buffer available
                        decoder.decode(inBuffer, cBuffer, false);
                        cBuffer.flip();
                        line = cBuffer.toString();
                        System.out.print("TCP Client: " + line);
                       
                        //////**********************************
                        
                        //detect that the client wants to add a new user
                        if(line.contains("regestered user:"))
                        {                      	                            
                        	line = line.replace("regestered user:","");
                            System.out.println("Checking to make sure that " + line + " is a regestered user...");
                            if(searchForName(line))
                            {
                                cchannel.write(encoder.encode(CharBuffer.wrap("true\n")));     

                                System.out.println("User was found");
                            }
                            else
                            {
                                System.out.println("User is not a regestered user!");
                                cchannel.write(encoder.encode(CharBuffer.wrap("false\n")));       

                            }                          	                                  
                        }
                        
                        if(line.contains("help")){
                           cchannel.write(encoder.encode(CharBuffer.wrap("Commands:\nterminate -Terminates the connection\nlist -Lists the files in the current dir\nget <filename> -Retrieves file from the server\nend\n")));
                        }else if(line.equals("list\n")){                   //Executes ls, then reads the temp file and prints the output to the server
                           try{
                              Runtime.getRuntime().exec(new String[]{"bash","-c","ls > /tmp/tmp.txt"});
                           }catch(Exception e){
                              System.out.println("Error executing ls");
                              cchannel.write(encoder.encode(CharBuffer.wrap("Error executing ls\nend\n")));
                           }
                           Thread.sleep(100);
                           //Reading and sending file
                           try{
                              FileReader fr = new FileReader("/tmp/tmp.txt");
                              BufferedReader br = new BufferedReader(fr);
                              String bLine = br.readLine();
                              while(bLine != null){
                                 cchannel.write(encoder.encode(CharBuffer.wrap(bLine + '\n')));
                                 bLine = br.readLine();
                              }
                              br.close();
                               cchannel.write(encoder.encode(CharBuffer.wrap("end\n")));
                           }catch(IOException e){
                              System.out.println("Error while reading the list");
                              cchannel.write(encoder.encode(CharBuffer.wrap("Error while reading the list\nend\n")));
                           }
                        }else if (line.equals("terminate\n")){
                            terminated = true;
                            //Reading the file then sending it
                        }else if (line.contains("get ")){
                           //Get filename from client
                           String filename = (line.split(" "))[1];
                           try{
                              infile = new FileInputStream(filename.trim());
                           }catch(Exception e){
                              cchannel.write(encoder.encode(CharBuffer.wrap("Error in opening file " + filename)));
                              cchannel.write(encoder.encode(CharBuffer.wrap("end\n")));
                           }
                           try{
                              byte[] msg = new byte[infile.available()];
                              int read_bytes = infile.read(msg);
                              //Send message size to client
                              cchannel.write(encoder.encode(CharBuffer.wrap(Integer.toString(read_bytes))));
                              cchannel.write(encoder.encode(CharBuffer.wrap("end\n")));

                              //sent message data to client
                              cchannel.write(encoder.encode(CharBuffer.wrap(Arrays.toString(msg))));
                              cchannel.write(encoder.encode(CharBuffer.wrap("\n")));

                           }catch(Exception e){
                              System.out.println("File error");
                           }

                           
                           
                        }
                        
                        
                        else{
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
    catch (Exception e) {
        System.out.println(e);
     }
  }

  public RoomServer startRoomServer()
  {
      return null;
  }

  public void sendMessage(String msg)
  {
     return;
  }

  public byte[] receiveMessage()
  {
      return null;
  }

  private static Boolean searchForName(String name) throws FileNotFoundException {
	  
	  try
	  {
	  
		  File file = new File("registeredUsers.txt");
		  Scanner input = new Scanner(file);
		  while(input.hasNextLine()) {
			  String lineFromFile = input.nextLine();
			  if (lineFromFile.equals(name))
			  {
				  return true;
			  }
		   }
		  
	  }  
	  catch (IOException e)
	    {
	        System.out.println(e);
	    }
	  return false;
}


}
