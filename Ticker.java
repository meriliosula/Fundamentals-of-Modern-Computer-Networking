import java.util.Timer;
import java.util.TimerTask;

/**
 * This class defines an implementation of a topmost {@link Handler} such as a
 * given message is periodically sent to the given destination.
 *
 */
class Ticker extends Handler {

  /** A single Timer for all usages. Don't cancel it. **/
  public static final Timer TIMER = new Timer("General Purpose Timer", true);

  /** An arbitrary base value for the numbering of tickers. **/
  private static int counter = 9000;

  /**
   * The Timer task for the periodic sending of the message of this
   * {@code Ticker}.
   */
  private final TimerTask task;

  /**
   * Initializes a new ticker with the specified parameters
   * 
   * @param _under
   *          the {@link Handler} on which the new ticker will be stacked
   * @param _destination
   *          a {@code String} identifying the destination
   * @param _message
   *          the message to be sent
   * @param _period
   *          time in milliseconds between successive messages
   */
  public Ticker(final Handler _under, final String _destination,
      final String _message, int _period) {
    super(_under, ++counter, false);
    this.task = new TimerTask() {
      @Override
      public void run() {
        _under.send(_message, _destination);
      }
    };
    TIMER.schedule(this.task, 0, _period);
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

  /** Does nothing. */
  @Override
  public void handle(Message message) {
    // System.out.println(message);
  }

  /**
   * Do not call, always throws an UnsupportedOperationException.
   * 
   * @throws UnsupportedOperationException
   *           anyway
   */
  @Override
  public void send(String payload, String destination) {
    no_send();
  }

  /**
   * Do not call, always throws an UnsupportedOperationException.
   * 
   * @throws UnsupportedOperationException
   *           anyway
   */
  @Override
  public void send(String payload) {
    no_send();
  }

  /**
   * Releases any additional ressources and then performs the standard
   * {@link Handler#close}.
   */
  @Override
  public void close() {
    this.task.cancel();
    super.close();
  }

}