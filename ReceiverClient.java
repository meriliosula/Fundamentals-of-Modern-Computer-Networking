import java.net.SocketException;

public class ReceiverClient {

  public static final String SYNTAX = "syntax : java ReceiverClient myPort serverHost:serverPort filename destDir";

  public static void main(String[] args) {
    if (args.length != 4) {
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
    String destDir = args[3];

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
    FileHandler fileHandler = new FileHandler(connected, destDir);
    // connect the two state machines
    connected.send("GET " + filename);
    fileHandler.letItGo();
    System.out.println("closing Receiver");
    connected.close();
    GroundLayer.close();
  }

}