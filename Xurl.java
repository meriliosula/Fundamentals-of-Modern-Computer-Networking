/*****************************************************************************
 * 
 * From the Internet to the IoT: Fundamentals of Modern Computer Networking
 * INF557 2021
 * 
 * Code Assignment 01
 *
 * Author: Merili Osula
 * 
 *****************************************************************************/
import java.net.*;
//import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
import java.io.*;
 
public class Xurl {
	
    public static void main(String[] args) {
    	
    	if (args.length == 0) {
    		System.err.println("No arguments were given, expected to recieve argument <url>");
    		return;
    	}
 
        String url = args[0];
        
        download(url);
        
        
    }
    
    public static void download(String url) {
    	MyURL mUrl;
        try {
        	mUrl = new MyURL(url);
        } catch(Exception e) {
        	throw(e);
        }
        
        String hostName = mUrl.getHost();
        int portNumber = mUrl.getPort();
        String path = mUrl.getPath();
        //String protocol = mUrl.getProtocol();
        
        if (mUrl.getProtocol().equals("http") && portNumber == -1) {
        	portNumber = 80;
        }
        
        else if (mUrl.getProtocol().equals("https") && portNumber == -1) {
        	portNumber = 443;
        }
        
        // System.out.println(hostName);
        // System.out.println(portNumber);
        // System.out.println(path);
        
        
        try {
        	Socket socket = new Socket();
        	
        	while (true) {
	        // Creating connection
	        socket.connect(new InetSocketAddress(hostName, portNumber));
	        socket.setSoTimeout(30000);
	        
	        //Creating input, output streams
	        // OutputStream out = socket.getOutputStream(); // new PrintStream(socket.getOutputStream());
	        // InputStream in = socket.getInputStream(); // new BufferedReader(new InputStreamReader(socket.getInputStream()));
	        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	        
	        
	        // String builder for extracting the file name from the path
	        // The SB is reversed, so that we could find the index of the last '/', this is needed to find, where the file name is within the path
	        StringBuilder sb = new StringBuilder(path).reverse();
	        
	        // Take the substring from path starting from the last '/' to the end of the path
	        String fileName = new StringBuilder(sb.substring(0, sb.indexOf("/"))).reverse().toString();
	        
	        // Check if the file is valid
	        if (!(fileName.endsWith(".html") || fileName.endsWith(".htm") || fileName.endsWith(".txt") || fileName.endsWith(".php") || fileName.endsWith(".doc") || fileName.endsWith(".docx") || fileName.endsWith(".pdf") || fileName.endsWith(".odt") || fileName.endsWith(".xls") || fileName.endsWith(".xlsx") || fileName.endsWith(".ods") || fileName.endsWith(".ppt") || fileName.endsWith(".pptx"))) {
	        	System.err.println("The item at the url's path is not a valid file");
	        	in.close();
		        // out.close();
	            socket.close();
	        	return;
	        }
	        
	        
	        DataOutputStream outbound = new DataOutputStream(socket.getOutputStream() ); // http://gbengasesan.com/fyp/7/ch26.htm
	        // Sending request
	        // outbound.writeBytes("GET / HTTP/1.1\r\n\r\n");
	        outbound.writeBytes("GET " +path+ " HTTP/1.1\r\n"); // Example code was used to write this code
	        outbound.writeBytes("Host: " + hostName + (portNumber < 0 ? "" : ":" + portNumber) + "\r\n\r\n");
	        outbound.flush();
	        
	     // Read the response
            String responseLine;
            responseLine = in.readLine();
            
            String[] words = responseLine.split(" ");
            // System.out.println(words[1]);
            
            if (words[0].startsWith("HTTP")) {
            	
            	if (words[1].equals("404")) {
                    System.err.println("The file was not found");
            		in.close();
        	        outbound.close();
                    socket.close();
                    return;
                  }
            	
            	if (words[1].equals("301") || words[1].equals("302")) {
                    while (!responseLine.startsWith("Location: ")) {
                    	responseLine = in.readLine();
                    }
                    mUrl = new MyURL(responseLine.split(" ")[1]);
                    path = mUrl.getPath();
                    hostName = mUrl.getHost();
                    portNumber = mUrl.getPort();
                    
                    in.close();
        	        outbound.close();
                    socket.close();
                    
                    // System.err.println("The file has been moved to: " + mUrl.getProtocol() + mUrl.getHost() + mUrl.getPath());
                    
                    continue;
                  }
            	
            	if (words[1].equals("200")) {
            		// Handle the header
            		// responseLine = in.readLine();
            		int len = -1;
            		
            		while (!responseLine.equals("")) {
            			// System.out.println(responseLine);
            			if (responseLine != null) {
                			
                			if (responseLine.startsWith("Content-Length:")) {
                		        String[] words3 = responseLine.split(" ");
                		        len = Integer.parseInt(words3[1]);
                		        // break;
                		      }
                			if (responseLine.startsWith("Transfer-Encoding: chunked")) {
                		        len = -1;
                		        // break;
                		      }
                			// else {
                			if (responseLine.startsWith("Connection: close")) {
                				len = 1 << 20;
                				// System.err.println("File size not specified");
                				// return;
                			}
                			
                		}
            			responseLine = in.readLine();
            		}
            	
            		
            		
            		
            		char[] buffer = new char[len > 0 ? len : 0];
            		
            		PrintWriter writer = new PrintWriter(fileName);
            		String fileContent = "";
            		int i = 0;
            		
            		while (fileContent != null) {
            	        if (len == -1) {
            	        	fileContent = in.readLine();
            	          if (fileContent.length() == 0) {
            	        	  fileContent = in.readLine();
            	          }
            	          len = Integer.parseInt(fileContent, 16);
            	          i = 0;
            	          if (len == 0)
            	            break;
            	        }
            	        
            	        if (buffer.length < len) {
            	        	buffer = new char[len];
            	        }
            	        
            	        while (i < len) {
            	            int n = in.read(buffer, i, len - i);
            	            if (n < 0) { // EOF
            	              len = i;
            	              break;
            	            }
            	            i += n;
            	            
            	        }
            	        /**
            	        for (char k : buffer) {
            	        	System.out.print(k);
            	        }
            	        **/
            	        
            	        writer.write(buffer, 0, len);
        	            if (len != -1 && i >= len) {
        	            	// System.out.println("asd\n" + len);
        	            	break;
        	            }
            		}
            		DocumentProcessing.parseBuffer(buffer.toString());
            		writer.flush();
                    writer.close();
                    
            		
            		
                    // return;
                  }
            	
            	else {
            		System.err.println("Unable to download");
            		in.close();
        	        outbound.close();
                    socket.close();
            		return;
            	}
            	
            	
            }
            
            else {
            	System.err.println("http response invalid");
            	in.close();
    	        outbound.close();
                socket.close();
                return;
            }
            
            in.close();
	        outbound.close();
            socket.close();
            break;
        	}

            
	        
        } catch (IOException e) {
        	System.err.println(e);
        }
    }
}
