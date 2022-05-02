import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class provides a skeletal implementation of any message handler. To
 * implement a particular behavior, one needs only to extend this class and
 * override some of the {@link bind bind}, {@link handle handle}, {@link send
 * send} and {@link close close} methods.
 */
public abstract class Handler {

  /** An integer used to identify this {@code Handler}. */
  private final int refNumber;

  /** A map to store and retrieve the handlers stacked above the current one. */
  protected final ConcurrentHashMap<Integer, Handler> upsideHandlers;

  /** The {@code Handler} onto which the current one is stacked. */
  protected final Handler downside;

  /** A queue for storing incoming messages, before their processing. */
  private LinkedBlockingQueue<Message> queue = null;

  /** The size of the queue for incoming messages. */
  private static final int QUEUE_SIZE = 5;

  /** the thread for processing messages from the queue. */
  private Thread processor = null;

  /**
   * Initializes the core behavior of a handler. A handler is ordinarily stacked
   * onto another, except for the lowest down. Then a handler has a (supposed)
   * unique identifier, used to retrieved it inside a map of stacked handlers.
   * By default, a handler simply dispatch an incoming message to every handler
   * stacked on top of it. A queuing mechanism can be activated for decoupling
   * the processing at each layer.
   * 
   * @param under
   *          the {@code Handler} onto which the current one will be stacked, or
   *          {@code null} when the current is the lowest down
   * @param refNum
   *          the integer used to identify the current handler inside
   *          {@code under}'s map of stacked handlers
   * @param queueing
   *          {@code true} if queuing of incoming messages is enabled;
   *          {@code false} in not.
   */
  public Handler(Handler under, int refNum, boolean queueing) {
    this.refNumber = refNum;
    this.upsideHandlers = new ConcurrentHashMap<Integer, Handler>();
    this.downside = under;
    if (this.downside != null)
      this.downside.bind(this);
    if (queueing)
      this.start();
  }

  /**
   * @return a String for decorating the display of events
   */
  private final String getName() {
    return this.getClass().getName() + '_' + this.refNumber;
  }

  /**
   * Specifies, to this handler, another handler to which processed messages may
   * be passed upwards. Then, for each incoming message, this handler may choose
   * to invoke or not the {@link #receive} method of the specified {@code above}
   * layer.
   * 
   * @param above
   *          the {@code Handler} whose {@link #receive receive} may will be
   *          called for passing messages upwards
   * @see handle
   */
  @SuppressWarnings("boxing")
  protected void bind(Handler above) {
    this.upsideHandlers.put(above.refNumber, above);
  }

  /**
   * A facility to prohibit the use of {@link bind bind} method, making so a
   * topmost handler. Simply insert a call to {@code no_bind()} in the
   * overriding {@link bind bind}.
   * 
   * @throws UnsupportedOperationException
   *           anyway
   */
  protected final void no_bind() {
    throw new UnsupportedOperationException(this.getClass().getName()
        + " does not accept binding any Handler upside");
  }

  /**
   * Removes the specified handler from the map of stacked handlers, so it may
   * no longer receive incoming messages.
   * 
   * @param above
   *          the {@code Handler} to remove from the current one's map
   * @see handle
   */
  @SuppressWarnings({ "boxing", "unused" })
  private final void unbind(Handler above) {
    this.upsideHandlers.remove(above.refNumber);
  }

  /**
   * Starts the queueing mechanism for this handler. This method is invoked or
   * not from the constructor, then the message processing thread is started or
   * not. The difference is in decoupling or not the message processing via a
   * queue. To customize the message processing in itself, one has to override
   * method {@link handle}.
   */
  private final void start() {
    if (this.queue != null)
      throw new IllegalStateException(
          this.getName() + "'s processor is already started");
    this.queue = new LinkedBlockingQueue<Message>(QUEUE_SIZE);
    this.processor = new Thread(new Runnable() {
      @SuppressWarnings("synthetic-access")
      @Override
      public void run() {
        while (!Thread.currentThread().isInterrupted()) {
          Message message = null;
          try {
            message = Handler.this.queue.take();
          } catch (@SuppressWarnings("unused") InterruptedException e) {
            Thread.currentThread().interrupt();
          }
          if (message != null)
            handle(message);
        }
        System.out.println(Handler.this.getName() + "'s thread interrupted");
      }
    }, this.getName() + "'s processor");
    this.processor.setDaemon(true);
    this.processor.start();
  }

  /**
   * Handles one message now. By default, this method simply dispatch the
   * message upwards to every handler stacked on top of the current. So it does
   * nothing when there is no handler above. One has to override this method to
   * change this behavior.
   * 
   * @param message
   *          the {@code Message} to be handled now
   */
  protected void handle(Message message) {
    for (Handler above : Handler.this.upsideHandlers.values())
      above.receive(message);
  }

  /**
   * Takes an incoming {@link Message}. This method is to be invoked by the
   * handler below. This method should not block and must return as soon as
   * possible. The effective processing of the incoming message is then done by
   * method {@link handle}, either immediately or later, if queueing has been
   * activated.
   * 
   * @param message
   *          the incoming {@code Message} passed to this handler
   * 
   * @see #Handler
   */
  protected final void receive(Message message) {
    if (this.queue == null)
      this.handle(message);
    else
      try {
        this.queue.add(message);
      } catch (IllegalArgumentException e) {
        System.err.println(this.getName() + ": " + e);
      }
  }

  /**
   * Sends a payload to the specified destination address, through this handler.
   * The address is supposed to be formatted as expected by this handler and
   * those below.
   * 
   * @param payload
   *          the payload to be sent
   * @param destinationAddress
   *          a {@code String} identifying the destination
   * @see #send(String)
   */
  protected abstract void send(String payload, String destinationAddress);

  /**
   * Sends a payload downwards through this handler, when an implicit
   * destination address has been defined.
   * 
   * @param payload
   *          the payload to be sent
   * @see #send(String,String)
   */
  protected abstract void send(String payload);

  /**
   * A facility to prohibit the use of any of the two {@link #send(String)} and
   * {@link #send(String,String)} methods. Simply insert a call to
   * {@code no_send()} in the overriding {@link send send}.
   * 
   * @throws UnsupportedOperationException
   *           anyway
   */
  protected final void no_send() {
    throw new UnsupportedOperationException(this.getClass().getName()
        + " does not accept sending data in this way");
  }

  /**
   * Closes this handler, stopping the queueing process and closing in turn
   * every handler stacked above. An overriding method should make the best
   * effort to release any additional ressources and then call
   * {@code super.close()}.
   */
  protected void close() {
    if (this.processor != null)
      this.processor.interrupt();
    for (Handler above : this.upsideHandlers.values())
      above.close();
    this.upsideHandlers.clear();
  }

}