/*****************************************************************************
 * 
 * From the Internet to the IoT: Fundamentals of Modern Computer Networking
 * INF557 2021
 * 
 * Code Assignment 05
 *
 * Author: Merili Osula
 * 
 *****************************************************************************/
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


public class GroundLayer {

  /**
   * This {@code Charset} is used to convert between our Java native String
   * encoding and a chosen encoding for the effective payloads that fly over the
   * network.
   */
  private static final Charset CONVERTER = StandardCharsets.UTF_8;

  /**
   * This value is used as the probability that {@code send} really sends a
   * datagram. This allows to simulate the loss of packets in the network.
   */
  public static double RELIABILITY = 1.0;

  private static DatagramSocket localSocket = null;
  private static Thread receiver = null;
  private static Handler handler = null;

  public static void start(int _localPort, Handler _handler)
      throws SocketException {
    if (handler != null)
      throw new IllegalStateException("GroundLayer is already started");
    handler = _handler;
    // TO BE COMPLETED
    
    // Should make a receiver (a thread which waits for incoming UDP packets on the specified port
    // and passes the message to the receive method of the above handler)
  
	localSocket = new DatagramSocket(_localPort);
    receiver = new Thread(new Runnable() {
		public void run() {
			byte[] data = new byte[1024];
			DatagramPacket reply = new DatagramPacket(data, data.length);
			Message message;
			while (!Thread.currentThread().isInterrupted()) {
				try {
					localSocket.receive(reply);
					String payload = new String (data ,0,reply.getLength(), CONVERTER);
					message = new Message(payload, reply.getSocketAddress().toString());
					handler.receive(message);
				//  handler.receive(new Message(new String(reply.getData()), reply.getSocketAddress().toString()));
                    Thread.sleep(10);
				}
				catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                catch (IOException e) {
                    return;
                }
            } 
			
		
		}
	} );
    receiver.setDaemon(true);
    receiver.start();
  }

  public static void send(String payload, SocketAddress destinationAddress) {
    if (Math.random() <= RELIABILITY) {
    	// MUST SEND
    	
    	try {
    		if (localSocket != null) {
	    		if (!localSocket.isClosed()) {
		    		DatagramPacket pack = new DatagramPacket(payload.getBytes(), payload.getBytes().length, destinationAddress);
		
		    	//localSocket.bind(destinationAddress);
		    		localSocket.send(pack);
	    		}
    		}
    	}
    	catch (Exception e) {
    		
    	}
    	
    }
  }

  public static void close() {
    // TO BE COMPLETED
	receiver.interrupt();
	localSocket.close();
	handler = null;
		
    System.err.println("GroundLayer closed");
    return; //return what
  }

}