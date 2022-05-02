public class ConnectionParameters {
  private final int rId;
  private final String rAddr;

  public ConnectionParameters(int remoteId, String remoteAddress) {
    this.rId = remoteId;
    this.rAddr = remoteAddress;
  }

  public int getRemoteId() { return this.rId; }

  public String getRemoteAddress() { return this.rAddr; }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ConnectionParameters) {
      ConnectionParameters other = (ConnectionParameters) o;
      if (this.rId != other.rId)
        return false;
      if (this.rAddr == null)
        return other.rAddr == null;
      return this.rAddr.equals(other.rAddr);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return 4231 * this.rId + ((this.rAddr == null) ? 0 : this.rAddr.hashCode());
  }

  @Override
  public String toString() {
    return "[" + this.rId + "@" + this.rAddr + "]";
  }
}