/*****************************************************************************
 * 
 * From the Internet to the IoT: Fundamentals of Modern Computer Networking
 * INF557 2021
 * 
 * Code Assignment 02 and 03
 *
 * Author: Merili Osula
 * 
 *****************************************************************************/
import java.util.LinkedList;

/**
 * Basic implementation with a LinkedList.
 */
public class BlockingListQueue implements URLQueue {


	private final LinkedList<String> queue;

	public BlockingListQueue() {
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
		notify();
	}

	@Override
	public synchronized String dequeue() {
		while (this.isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return "**STOP**";
			}
		}
		String str = this.queue.remove();
		
		if (str.equals("**STOP**")) {
			Thread.currentThread().interrupt();
		}
		return str;

	}


}