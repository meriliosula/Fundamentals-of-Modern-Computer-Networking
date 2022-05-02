/**
 * This class defines a loopback implementation of {@link Handler}, such as
 * every sent message is forwarded back to handlers stacked on.
 */
public class LoopBackHandler extends Handler {

  /** Initializes as a bottom handler whose queueing mechanism is running. */
  public LoopBackHandler() {
    super(null, 0, true);
  }

  /**
   * Forwards the given payload back to the entry of this handler.
   */
  @Override
  public void send(String payload, String destinationAddress) {
    this.receive(new Message(payload, "loopback("+destinationAddress+')'));
  }

  /**
   * Forwards the given payload back to the entry of this handler.
   */
  @Override
  public void send(String payload) {
    this.receive(new Message(payload, "loopback"));
  }

}