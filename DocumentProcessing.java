/*****************************************************************************
 * 
 * From the Internet to the IoT: Fundamentals of Modern Computer Networking
 * INF557 2021
 * 
 * Code Assignment 02
 *
 * Authors: Merili Osula
 * 
 *****************************************************************************/
import java.util.regex.*; 
public class DocumentProcessing {

  public interface URLhandler {
    void takeUrl(String url);
  }

  public static URLhandler handler = new URLhandler() {
    @Override
    public void takeUrl(String url) {
      System.out.println(url);        // DON'T change anything here
    }
  };

  /**
   * Parse the given buffer to fetch embedded links and call the handler to
   * process these links.
   * 
   * @param data
   *          the buffer containing the html document
   */
  public static void parseBuffer(CharSequence data) {
    // TODO at exercise 1
    // call handler.takeUrl for each matched url
	  // 
   Pattern pattern = Pattern.compile("<(?i)a\\s+(.*?[\\\"'])*\\s?(?:>)?(?i)HREF\\s*?=\\s*([\\\"'])(http:[^>]*?)\\2(.*?)\\s*>", Pattern.CASE_INSENSITIVE);
   Matcher matcher = pattern.matcher(data);
   while (matcher.find()) {
	   handler.takeUrl(matcher.group(3));
     //System.out.println(matcher.group(1));
   }
  }
  
  public static void main (String[] args) {
   DocumentProcessing.parseBuffer(null);
  }

}