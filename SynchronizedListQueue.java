/*****************************************************************************
 * 
 * From the Internet to the IoT: Fundamentals of Modern Computer Networking
 * INF557 2021
 * 
 * Code Assignment 03
 *
 * Author: Merili Osula
 * 
 *****************************************************************************/
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class SynchronizedListQueue implements URLQueue {
	
	private final LinkedList<String> queue;
	
	public SynchronizedListQueue() {
	    this.queue = new LinkedList<String>();
	  }

	@Override
	public boolean isEmpty() {
		return this.queue.size() == 0;
	}

	@Override
	  public boolean isFull() {
	    return false;
	  }

	  @Override
	  public synchronized void enqueue(String url) {
	    this.queue.add(url);
	  }

	  @Override
	  public synchronized String dequeue() { // basically same as lock-unlock but more secure, if there's an error message things won't break down
		  if (isEmpty()) {
			  throw new NoSuchElementException("no more items in queue");
		  }
		  else {
			  return this.queue.remove();
		  }  
		  
	  }

}

