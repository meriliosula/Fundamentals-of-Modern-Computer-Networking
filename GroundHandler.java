import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 * This class defines a implementation of {@link Handler}, as an adapter for the
 * {@link GroundLayer}.
 */
public class GroundHandler extends Handler {

  GroundHandler(int localPort) throws SocketException {
    super(null, 0, true);
    GroundLayer.start(localPort, this);
  }

  /**
   * Sends a payload to the specified destination address, using
   * {@link GroundLayer#send}. The address is supposed to be in
   * <em>&lt;hostname&gt;</em>/<em>&lt;ipv4-address&gt;</em>:<em>&lt;port&gt;</em>
   * format, where one among <em>&lt;hostname&gt;</em> or
   * <em>&lt;ipv4-address&gt;</em> may be missing and <em>&lt;port&gt;</em> is a
   * mandatory valid port number.
   * 
   * @param payload
   *          the payload to be sent
   * @param destinationAddress
   *          a {@code String} identifying the destination
   * @see #send(String)
   */
  @Override
  public void send(String payload, String destinationAddress) {
    String[] address = destinationAddress.split(":");
    if (address.length != 2)
      throw new IllegalArgumentException("wrong address");
    String[] hostParts = address[0].split("/");
    boolean slashFormat = hostParts.length >= 2 && hostParts[1].length() > 0;
    String host = slashFormat ? hostParts[1] : hostParts[0];
    int port = Integer.parseInt(address[1]);
    SocketAddress destinationSocket = new InetSocketAddress(host, port);
    System.err.println(host + ' ' + port);
    System.err.println(destinationSocket);
    GroundLayer.send(payload, destinationSocket);
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
   * Calls {@link GroundLayer#close} and then performs the standard
   * {@link Handler#close}.
   */
  @Override
  public void close() { // nothing
    super.close();
    GroundLayer.close(); // helps the termination of the JVM
  }

}