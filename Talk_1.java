import java.net.SocketException;
import java.util.Scanner;

public class Talk_1 {

  public static final String SYNTAX = "syntax : java Talk_1 myPort destinationHost:destinationPort";

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
    // GroundLayer.RELIABILITY = 0.5;
    Handler ground = null;
    try {
      ground = new GroundHandler(localPort);
    } catch (SocketException e) {
      System.err.println(e.getMessage());
      return;
    }
    Terminal myTalk = new Terminal(ground, args[1]);
    Scanner sc = new Scanner(System.in);
    while (sc.hasNextLine()) {
      myTalk.send(sc.nextLine());
    }
    System.out.println("closing");
    sc.close();
    ground.close();
  }
}