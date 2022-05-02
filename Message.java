/** A basic definition for a received message. */
class Message {
  /** The payload of this message. */
  public final String payload;
  /** The source address of this message, formatted as a {@code String}. */
  public final String sourceAddress;

  /**
   * Initializes a new message with the specified payload and sourceAddress.
   * 
   * @param _payload
   *          the payload
   * @param _sourceAddress
   *          the source address of this message, formatted as a {@code String}
   */
  public Message(String _payload, String _sourceAddress) {
    this.payload = _payload;
    this.sourceAddress = _sourceAddress;
  }

  /**
   * Returns a string representation of this message.
   */
  @Override
  public String toString() {
    return '"' + this.payload + "\" from " + this.sourceAddress;
  }

}