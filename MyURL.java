public class MyURL {
	// Protocol of this MyURL instance
	String protocol;
	String hostname;
	int port = -1;
	String path;
	
	public MyURL (String url) {
		// Using StringBuilder to handle the url and extract the required components from it
		StringBuilder sb = new StringBuilder(url);
		// A temporary StringBuilder, used to store the information of the protocol
		StringBuilder temp = new StringBuilder();
		// This index is used to keep track of which part of the url is already processed
		int index = 0;
		// Loop through the url and store the protocol into the temporary SB
		
		if (url.length() <= 3) {
			throw new IllegalArgumentException("The url is invalid");
		}
		
		for (int i = 0; i<sb.length(); i++) {
			// If we have found the end of the protocol, marked by ':', we will stop this loop
			if ((sb.charAt(i)) == ':') {
				this.protocol = temp.toString();
				index = i;
				break;
			}
			temp.append(sb.charAt(i));
		}
		
		// Checking if the protocol is followed by '//'
		if (sb.charAt(index+1) != '/' || sb.charAt(index+2) != '/') {
			throw new IllegalArgumentException("The given url is not formated correctly");
		}
		
		// Clearing the temporary SB, so that we can store the next part of the url in it
		temp.setLength(0);
		// Loop through the url and store the hostname into the temporary SB
		for (int i = index+3; i <sb.length(); i++) {
			// Stop the loop if it has reached a ':' or '/'
			if ((sb.charAt(i)) == ':' || sb.charAt(i) == '/') {
				this.hostname = temp.toString();
				index = i;
				break;
			}
			temp.append(sb.charAt(i));
		}
		
		// Clearing the temporary SB, so that we can store the next part of the url in it
		temp.setLength(0);
		// If the last processed part of the url is ':', then the next part is a port
		if (sb.charAt(index) == ':') {
			// loop until the loop reaches a '/'
			for (int i = index+1; i < sb.length(); i++) {
				if (sb.charAt(i) == '/') {
					if (temp.length() != 0) {
						this.port = Integer.parseInt(temp.toString());
					}
					index = i;
					break;
				}
				temp.append(sb.charAt(i));
			}

		}
		// Find the path from the url
		if (sb.charAt(index) == '/') {
			if (index == sb.length()-1) {
				this.path = "/";
			}
			else {
				this.path = sb.substring(index, sb.length());
			}
		}
		
		// If the url does not end with a '/', then it is not correctly formated
		else {
			if (this.path == "") {
				throw new IllegalArgumentException("A url that does not have a path must end with a '/'");
			}
		}
	}
	
	// Get methods
	public String getProtocol() {
		return this.protocol;
	}
	
	public String getHost() {
		return this.hostname;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public String getPath() {
		return this.path;
	}
	
	
	public static void main (String[] args) {
		MyURL asd = new MyURL("http://host:80/path");
		System.out.println(asd.getProtocol());
		System.out.println(asd.getHost());
		System.out.println(asd.getPort());
		System.out.println(asd.getPath());
	}
	
}

