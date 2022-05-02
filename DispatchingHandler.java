/*****************************************************************************
 * 
 * From the Internet to the IoT: Fundamentals of Modern Computer Networking
 * INF557 2021
 * 
 * Code Assignment 07
 *
 * Author: Merili Osula
 * 
 *****************************************************************************/
import java.util.concurrent.ArrayBlockingQueue;
import java.util.HashMap;

public class DispatchingHandler extends Handler {

	/** one will need it */
	private static final String HELLO = "--HELLO--";

	/** An arbitrary base value for the numbering of handlers. **/
	private static int counter = 35000;

	/** the queue for pending connections */
	private final ArrayBlockingQueue<ConnectionParameters> queue;

	// to be completed
	int packetNumberS = 0;
	HashMap <Integer, Integer> lookupTable = new HashMap<>(); // destination connectionId as the key 

	private static final boolean DEBUG = false;

	/**
	 * Initializes a new dispatching handler with the specified parameters
	 * 
	 * @param _under
	 *                         the {@link Handler} on which the new handler will
	 *                         be stacked
	 * @param _queueCapacity
	 *                         the capacity of the queue of pending connections
	 */
	public DispatchingHandler(final Handler _under, int _queueCapacity) {
		super(_under, ++counter, false);
		this.queue = new ArrayBlockingQueue<ConnectionParameters>(_queueCapacity);
		// add other initializations if needed
	}

	/**
	 * Retrieves and removes the head of the queue of pending connections, waiting
	 * if no elements are present on this queue.
	 *
	 * @return the connection parameters record at the head of the queue
	 * @throws InterruptedException
	 *                                if the calling thread is interrupted while
	 *                                waiting
	 */
	public ConnectionParameters accept() throws InterruptedException {
		return this.queue.take();
	}

	@Override
	public void send(String payload) {
		no_send();
	}

	@Override
	protected void send(String payload, String destinationAddress) {
		this.downside.send(payload, destinationAddress);
	}

	@Override
	public void handle(Message message) {
		// to be completed
		String[] messageSplit = message.payload.split(";");
		int locID = Integer.parseInt(messageSplit[0]);	
		int remID = Integer.parseInt(messageSplit[1]);
		packetNumberS = Integer.parseInt(messageSplit[2]);
		String content = messageSplit[3];
		
		if (content.equals(HELLO)) {
			if (lookupTable.containsKey(locID)) {
				if (lookupTable.get(locID) != -1) {
					if (upsideHandlers.get(lookupTable.get(locID)) != null) { // No more NullPointerExceptions?
						upsideHandlers.get(lookupTable.get(locID)).receive(message);
					}
				}
			}
			else {
				if (queue.offer(new ConnectionParameters(locID, message.sourceAddress))) {
					// lookupTable.put(locID, remID);
					lookupTable.put(locID, -1); // Set dest.Id as -1 (unknown)
				}
			}
		}
		else {
			if (upsideHandlers.get(remID) != null) { // No more NullPointerExceptions?
				upsideHandlers.get(remID).receive(message);
				if (lookupTable.containsKey(locID)) {
					if (lookupTable.get(locID) == -1) { // If unknown, change destination id
						lookupTable.replace(locID, remID);
					}
				}
			}
		}
	}
}