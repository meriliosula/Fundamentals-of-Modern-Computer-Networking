import java.net.SocketException;

public class FileServer {

  public static final String SYNTAX = "syntax : java FileServer serverPort rootDir";

  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println(SYNTAX);
      return;
    }
    int localPort = -1;
    try {
      localPort = Integer.parseInt(args[0]);
    } catch (@SuppressWarnings("unused") NumberFormatException e) {
      System.err.println(SYNTAX);
    }
    final String rootDir = args[1];

    GroundLayer.RELIABILITY = 0.9;
    Handler ground = null;
    try {
      ground = new GroundHandler(localPort);
    } catch (SocketException e) {
      System.err.println(e.getMessage());
      return;
    }
    DispatchingHandler dispatcher = new DispatchingHandler(ground, 3);

    while (true) {
      // takes one pending connection or waits
      ConnectionParameters parameters;
      try {
        parameters = dispatcher.accept();
      } catch (@SuppressWarnings("unused") InterruptedException e) {
        break;
      }
      new Thread("Thread for " + parameters) {
        @Override
        public void run() {
          Handler connected = new ConnectedHandler(dispatcher,
              ConnectedHandler.getUniqueID(), parameters.getRemoteAddress());
          FileHandler fileHandler = new FileHandler(connected, rootDir);
          fileHandler.letItGo();
          connected.close();
        }
      }.start();
    }
  }
}