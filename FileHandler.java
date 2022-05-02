import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class FileHandler extends Handler {

  private static final boolean DEBUG = false;

  // don't change the following definition
  public static final String CLOSE = "** CLOSE **";

  /**
   * This delay is to slow down the sending of a file, so as to make it easy to
   * launch and observe multiple transfer at the same time.
   */
  private static final long DELAY = 10;

  /**
   * This is an empirical delay to keep the CoonmectedHandler alive, so that
   * acking the CLOSE get done.
   */
  private static final long COOLING_TIME = 1000;

  /** An arbitrary base value for the numbering of handlers. **/
  private static int counter = 30000;

  private final String rootDir;
  private String fileName = null;
  private FileWriter writer = null;
  private Scanner reader = null;

  /** Definition of the state machine */
  public static enum State {
    INIT, RECV, SEND, CLOSE_SENT, CLOSE_RECEIVED, CLOSED
  }
  private State currentState;
  private final Object stateMonitor = new Object();

  /**
   * Initializes a new FileHandler with the specified parameters
   * 
   * @param _under
   *                   the {@link Handler} on which the new FileHandler will be
   *                   stacked
   * @param _rootDir
   *                   a {@code String} identifying the directory where to
   *                   put/find files
   */
  public FileHandler(Handler _under, String _rootDir) {
    super(_under, ++counter, true);
    this.rootDir = _rootDir;
    this.currentState = State.INIT;
  }

  /** sending half-close */
  private void sendCLOSE() {
    synchronized (this.stateMonitor) {
      switch (this.currentState) {
      case SEND:
        this.downside.send(CLOSE);
        this.currentState = State.CLOSE_SENT;
        return;
      case CLOSE_RECEIVED:
        this.downside.send(CLOSE);
        this.currentState = State.CLOSED;
        return;
      case CLOSED:
        return;
      default:
        System.err
            .println("*** sendClose while in " + this.currentState + " state");
      }
    }
  }

  /** receiving half-close */
  private void handleCLOSE() {
    synchronized (this.stateMonitor) {
      switch (this.currentState) {
      case INIT:
        this.currentState = State.CLOSE_RECEIVED;
        this.stateMonitor.notify();
        return;
      case SEND:
        if (this.reader != null) {
          this.reader.close();
        }
        this.currentState = State.CLOSE_RECEIVED;
        this.stateMonitor.notify();
        return;
      case RECV:
        if (this.writer != null) {
          try {
            this.writer.close();
          } catch (IOException e) {
            System.err.println(e);
          }
          System.err.println("file " + this.fileName + " received");
        }
        this.currentState = State.CLOSE_RECEIVED;
        this.stateMonitor.notify();
        return;
      case CLOSE_SENT:
        this.currentState = State.CLOSED;
        this.stateMonitor.notify();
        return;
      default:
        System.err.println(
            "*** handleClose while in " + this.currentState + " state");
      }
    }
  }

  /**
   * Once the state machine is launched, there is almost nothing to do, but
   * waiting for a remote CLOSE
   */
  public void letItGo() {
    System.out.println(Thread.currentThread().getName()
        + " waiting for remote close in FileHandler.run()");
    synchronized (this.stateMonitor) {
      while (this.currentState != State.CLOSE_RECEIVED
          && this.currentState != State.CLOSED) {
        try {
          this.stateMonitor.wait();
        } catch (@SuppressWarnings("unused") InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }
      }
    }
    sendCLOSE(); // if CLOSE_RECEIVED and not yet CLOSED
    try {
      Thread.sleep(COOLING_TIME);
    } catch (InterruptedException e) {
      System.err.println(e);
    }
  }

  /** so as to terminate properly in case of something goes wrong */
  private void error(String message) {
    System.err.println(message);
    handleCLOSE();
  }

  /** used for sending a file in a separate thread */
  class Sender implements Runnable {
    @SuppressWarnings("synthetic-access")
    @Override
    public void run() {
      if (FileHandler.this.reader != null) {
        FileHandler.this.downside.send("PUT " + FileHandler.this.fileName);
        while (true) {
          try {
            FileHandler.this.downside.send(FileHandler.this.reader.nextLine());
          } catch (IllegalStateException e) {
            System.err.println(e);
            break;
          } catch (@SuppressWarnings("unused") NoSuchElementException e) {
            break;
          }
          try {
            Thread.sleep(DELAY);
          } catch (InterruptedException e) {
            System.err.println(e);
            break;
          }
        }
      }
      sendCLOSE();
    }
  }

  /** analyze the first line as a query */
  private void handleINIT(String payload) {
    String[] words = payload.split("\\s+");
    if (words.length != 2) {
      error("INIT state, bad format: " + payload);
      return;
    }
    if (words[0].equals("PUT")) {
      System.out.println(payload); // display the PUT line
      this.fileName = words[1];
      try {
        this.writer = new FileWriter(new File(this.rootDir, this.fileName));
      } catch (IOException e) {
        error(e.getMessage());
        return;
      }
      this.currentState = State.RECV;
      return;
    }
    if (words[0].equals("GET")) {
      System.out.println(payload); // display the GET line
      this.fileName = words[1];
      try {
        this.reader = new Scanner(new File(this.rootDir, this.fileName));
      } catch (IOException e) {
        System.err.println(e);
      }
      this.currentState = State.SEND;
      // a thread is needed here, as handle() must return fast
      new Thread(new Sender()).start();
      return;
    }
    error("INIT state, bad query: " + payload);
    return;
  }

  /** handle the successive payloads as the content of the file */
  private void handleRECV(String payload) {
    if (this.writer != null)
      try {
        this.writer.write(payload + '\n');
      } catch (IOException e) {
        System.err.println(e);
      }
  }

  @Override
  public void handle(Message message) {
    if (DEBUG)
      System.err.println("FILE RECEIVE " + message);
    String payload = message.payload;
    if (CLOSE.equals(payload)) {
      handleCLOSE();
      return;
    }
    switch (this.currentState) {
    case INIT:
      handleINIT(payload);
      return;
    case RECV:
      handleRECV(payload);
      return;
    case CLOSE_RECEIVED:
    case CLOSED:
      return;
    default:
    }
    error("should not receive while in " + this.currentState + " state: "
        + payload);
  }

  /**
   * Do not call, always throws an UnsupportedOperationException.
   * 
   * @throws UnsupportedOperationException
   *                                         anyway
   */
  @Override
  public void send(String payload) {
    no_send();
  }

  /**
   * Do not call, always throws an UnsupportedOperationException.
   * 
   * @throws UnsupportedOperationException
   *                                         anyway
   */
  @Override
  public void send(String payload, String _destination) {
    no_send();
  }

  @Override
  public void close() {
    System.out.println("closing FileHandler now");
    super.close();
    System.out.println("FileHandler closed");
  }
}