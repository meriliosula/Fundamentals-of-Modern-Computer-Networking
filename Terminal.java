/**
 * This class defines an implementation of a topmost {@link Handler} such as
 * successive messages may be sent to a fixed destination. This handler also
 * displays incoming messages.
 *
 */
class Terminal extends Handler {

  /** An arbitrary base value for the numbering of terminals. **/
  private static int counter = 10000;

  /**
   * A {@code String} identifying the unique destination of messages sent by
   * this terminal.
   */
  private final String destination;

  public Terminal(Handler _under, String _destination) {
    super(_under, ++counter, false);
    this.destination = _destination;
  }

  /**
   * Do not call, always throws an UnsupportedOperationException.
   * 
   * @throws UnsupportedOperationException
   *           anyway
   */
  @Override
  public void bind(Handler above) {
    no_bind();
  }

  /**
   * Simply prints an incoming message.
   * 
   * @param message
   *          the {@code Message} to be handled now
   */
  @Override
  public void handle(Message message) {
    System.out.println(message);
  }

  /**
   * Sends a payload downwards through this handler, when an implicit
   * destination address has been defined.
   * 
   * @param payload
   *          the payload to be sent
   * @see #send(String,String)
   */
  @Override
  public void send(String payload) {
    this.downside.send(payload, this.destination);
  }

  /**
   * Do not call, always throws an UnsupportedOperationException.
   * 
   * @throws UnsupportedOperationException
   *           anyway
   */
  @Override
  public void send(String payload, String _destination) {
    no_send();
  }

}