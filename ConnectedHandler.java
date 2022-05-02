/*****************************************************************************
 * 
 * From the Internet to the IoT: Fundamentals of Modern Computer Networking
 * INF557 2021
 * 
 * Code Assignment 06
 *
 * Author: Merili Osula
 * 
 *****************************************************************************/


import java.io.IOException;
import java.net.SocketAddress;
import java.util.Timer;
import java.util.TimerTask;

public class ConnectedHandler extends Handler {
	private Object some_mutex = new Object();
	private Object another_mutex = new Object();

  /**
   * @return an integer identifier, supposed to be unique.
   */
  public static int getUniqueID() {
    return (int) (Math.random() * Integer.MAX_VALUE);
  }

  // don't change the two following definitions

  private static final String HELLO = "--HELLO--";
  private static final String ACK = "--ACK--";

  /**
   * the two following parameters are suitable for manual experimentation and
   * automatic validation
   */

  /** delay before retransmitting a non acked message */
  private static final int DELAY = 300;

  /** number of times a non acked message is sent before timeout */
  private static final int MAX_REPEAT = 10;

  /** A single Timer for all usages. Don't cancel it. **/
  private static final Timer TIMER = new Timer("ConnectedHandler's Timer", true);

  private final int localId;
  private final String destination;
  private Handler aboveHandler;
  // to be completed
  
  private int remoteId = -1; 
  private int packetNumber = 0; //from receiver
  private int packetNumberS = 0; //from sender
  private int packetNumberL = 0; //last received
  private TimerTask task;

  /**
   * Initializes a new connected handler with the specified parameters
   * 
   * @param _under
   *          the {@link Handler} on which the new handler will be stacked
   * @param _localId
   *          the connection Id used to identify this connected handler
   * @param _destination
   *          a {@code String} identifying the destination
   */
  
  public ConnectedHandler(final Handler _under, int _localId,
      String _destination) {
    super(_under, _localId, true);
    this.localId = _localId;
    this.destination = _destination;

    // to be completed
    
    send(HELLO);
    
    //blocks until the connection been established(ACK back)
    if (this.remoteId == -1) {
	    synchronized (another_mutex) {
		    try {
				another_mutex.wait(); //TODO: Should wait for a hello packet
				// Message message = new Message("", "");
				// this.handle (message);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
    }
  }

  // don't change this definition
  @Override
  public void bind(Handler above) {
    if (!this.upsideHandlers.isEmpty())
      throw new IllegalArgumentException(
          "cannot bind a second handler onto this "
              + this.getClass().getName());
    this.aboveHandler = above;
    super.bind(above);
  }
  /**
  @Override
  public void handle(Message message) {
	if (message.payload.contains(HELLO)){
		send(ACK);
		//if(Integer.valueOf(message.sourceAddress) == -1) {
		if(remoteId == -1) {
			remoteId = Integer.valueOf(message.sourceAddress);
		}
	}else if (message.payload.contains(ACK)) {
		//find the sequence number
		// packetNumberS
		// GroundLayer.pack();
		String[] messageSplit = message.payload.split(";");
		packetNumberS = Integer.parseInt(messageSplit[2]);
		if (packetNumberS == packetNumber) {
			notify();
		}
	}
  }
  **/
	public void handle(Message message) {
		// extract information from the message: localId, remoteId, packetNumber,
		// content
		String[] messageSplit = message.payload.split(";");
		try {
		int locID = Integer.parseInt(messageSplit[0]);	
		int remID = Integer.parseInt(messageSplit[1]);
		packetNumberS = Integer.parseInt(messageSplit[2]);
		String content = messageSplit[3];
		synchronized (another_mutex) {
			another_mutex.notifyAll();
		}

		// if content is ACK
		if (content.equals(ACK)) {
			// check this packet is for sure for you, correct localid/remoteid/packetnumber
			if (locID == remoteId && remID == localId && packetNumber == packetNumberS) {
				// if so, stop the timer task for sending message
				// TODO
				// TIMER.notify();
				synchronized (some_mutex) {
					some_mutex.notifyAll();
				}
			}
			else {
				// otherwise, just return
				return;
			}
		}
		// else if content is HELLO
		else if (content.equals(HELLO)) {
			// check the packet is correct, e.g. right localid/remoteid/packetNumber
			if (locID >= 0 && -1 == remID && 0 == packetNumberS) {
				// if so, initialize informations: remoteId
				remoteId = locID;
				// remoteId = remID;
				// then send corresponding ACK message to the other side
				String pack = "" + localId + ";" + remoteId + ";" + packetNumberS + ";" + ACK;
				
				// this.send(pack);
				
				downside.send(pack, message.sourceAddress);
				
				// TIMER.notify();
				// TIMER.cancel();
				// some_mutex.notify();
				synchronized (another_mutex) {
					another_mutex.notifyAll();
				}
			}
			else {
				synchronized (another_mutex) {
					another_mutex.notifyAll();
				}
				// otherwise, just return
				return;
			}
		}
		else {
			// else (content is something else)
			// check the packet is correct, e.g. right localid/remoteid/packetNumber
			if (locID == remoteId && remID == localId && -1 != remoteId && packetNumberS - 1 == packetNumberL) {
				// if so, give it to upper layer and send corresponding ACK message
				String pack = "" + localId + ";" + remoteId + ";" + packetNumberS + ";" + ACK;
				// aboveHandler.send(pack, message.sourceAddress); // This could be broken
				// downside.send(pack, message.sourceAddress);
				Message msg = new Message (content, message.sourceAddress);
				aboveHandler.receive(msg); // Give, not send?
				packetNumberL = packetNumberS;
			}
			if (locID == remoteId && remID == localId && -1 != remoteId && packetNumberS == packetNumberL && packetNumberL >= 1) {
				String pack = "" + localId + ";" + remoteId + ";" + packetNumberS + ";" + ACK;
				aboveHandler.send(pack, message.sourceAddress);
			}
			else {
				// otherwise, just return.
				return;
			}
		}
		}
		catch (IndexOutOfBoundsException e) {
			// invalid message formating
			Message msg = new Message ("", message.sourceAddress);
			aboveHandler.receive(msg); // Give, not send?
		}
	}
  
  ///**
  	@Override
	public void send(final String payload) {
		// create whatever variables you need
		// craft the packet to send
		String packet = "" + localId + ";" + remoteId + ";" + packetNumber + ";" + payload;
		// create a timer task
		task = new TimerTask() {
			// send via downside the packet
			// you can also add a counter here
			private int counter = 0;

			@Override
			public void run() {
				/**
				if (counter < MAX_REPEAT) {
					downside.send(packet, destination);
				}
				else {
					synchronized (some_mutex) {
						some_mutex.notify();
					}
					return;
				}
				counter++;
				**/
				counter ++;
				if (counter > MAX_REPEAT) {
					synchronized (some_mutex) {
						some_mutex.notify();
					}
					return;
				}
				downside.send(packet, destination);
			}
		};
		
		TIMER.schedule(task, 0, DELAY);
		// . wait here until you are sure that the ACK is received,
		synchronized (some_mutex) {
			try {
				some_mutex.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		task.cancel(); // cancel the task
		// update the packet number
		packetNumber ++;
	}
//**/
/**
  @Override
  public void send(final String payload) {
    // to be completed
	  Timer timer = new Timer();
      TimerTask task = new TimerTask() {
	      public void run() {
	    	  
	      };
      };
      
    	 new Thread(new Runnable() {
			public void run() { 
				while (!Thread.currentThread().isInterrupted()) {
					String packet = "" + localId + ";" + remoteId + ";" + packetNumber + ";" + payload;
				  	((ConnectedHandler) downside).send(packet, String.valueOf(remoteId));
					packetNumber = packetNumber + 1;
				try {
					wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}}
		} ).start(); 
    	
	 
              if(payload == ACK) {
                  timer.cancel();
              }  
  //    };
 // };
    timer.schedule(task, DELAY, DELAY);
    
    
  
		
  }
**/

  @Override
  public void send(String payload, String destinationAddress) {
    no_send();
  }

  @Override
  public void close() {
    // to be completed
	  task.cancel();
	  // TIMER.notify();
	  // TIMER.cancel();
    super.close();
  }

}