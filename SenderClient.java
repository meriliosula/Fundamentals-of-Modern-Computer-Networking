import java.net.SocketException;

public class SenderClient {

  public static final String SYNTAX = "syntax : java SenderClient myPort serverHost:serverPort filename";

//  private static final long DELAY = 10;

  public static void main(String[] args) {
    if (args.length != 3) {
      System.err.println(SYNTAX);
      return;
    }
    int localPort = -1;
    try {
      localPort = Integer.parseInt(args[0]);
    } catch (@SuppressWarnings("unused") NumberFormatException e) {
      System.err.println(SYNTAX);
    }
    String filename = args[2];

    GroundLayer.RELIABILITY = 0.9;
    Handler ground = null;
    try {
      ground = new GroundHandler(localPort);
    } catch (SocketException e) {
      System.err.println(e.getMessage());
      return;
    }
    Handler connected = new ConnectedHandler(ground,
        ConnectedHandler.getUniqueID(), args[1]);
    FileHandler fileHandler = new FileHandler(connected, ".");
    // connect the two state machines
    fileHandler.handle(new Message("GET " + filename, "local"));
    fileHandler.letItGo();
    System.out.println("closing Sender");
    connected.close();
    GroundLayer.close();
  }

}