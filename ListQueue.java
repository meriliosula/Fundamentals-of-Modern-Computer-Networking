/*****************************************************************************
 * 
 * From the Internet to the IoT: Fundamentals of Modern Computer Networking
 * INF557 2021
 * 
 * Code Assignment 02
 *
 * Author: Merili Osula
 * 
 *****************************************************************************/
import java.util.LinkedList;

/**
 * Basic implementation with a LinkedList.
 */
public class ListQueue implements URLQueue {

  private final LinkedList<String> queue;

  public ListQueue() {
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
  public void enqueue(String url) {
    this.queue.add(url);
  }

  @Override
  public String dequeue() {
    return this.queue.remove();
  }

}