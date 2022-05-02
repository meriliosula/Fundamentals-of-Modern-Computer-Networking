/*****************************************************************************
 * 
 * From the Internet to the IoT: Fundamentals of Modern Computer Networking
 * INF557 2021
 * 
 * Code Assignment 02 and 03
 *
 * Author: Merili Osula
 * 
 *****************************************************************************/
import java.util.HashSet;

public class Wget {

	public static void iterativeDownload(String initialURL) {
		final URLQueue queue = new ListQueue(); // sisestame siia listi kõik lingid, mis allalaetud failidest leiame
		final HashSet<String> seen = new HashSet<String>(); // siia salvestame need urlid, mis juba alla laetud
		// defines a new URLhandler
		DocumentProcessing.handler = new DocumentProcessing.URLhandler() {
			// this method will be called for each matched url
			@Override
			public void takeUrl(String url) {

				if (!seen.contains(url)) { // seen on list, kuhu paneme kõik juba läbitöödeldud urlid
					queue.enqueue(url); // break lõpetab tsükli töö, continue korral edasi ei vaata aga läheb ikka uuele ringile
					seen.add(url);
				}
			}
		};
		// to start, we push the initial url into the queue
		DocumentProcessing.handler.takeUrl(initialURL);
		while (!queue.isEmpty()) { // siin kontrollime kuni queues on midagi, mida alla laadida
			String url = queue.dequeue(); 
			Xurl.download(url); // don't change this line
		}

	}




	@SuppressWarnings("unused")
	public static void multiThreadedDownload(String initialURL) {
		int threadCount = Thread.activeCount();
		final URLQueue queue = new SynchronizedListQueue(); // sisestame siia listi kõik lingid, mis allalaetud failidest leiame
		final HashSet<String> seen = new HashSet<String>(); // siia salvestame need urlid, mis juba alla laetud
		final Object lock = new Object();
		// defines a new URLhandler
		DocumentProcessing.handler = new DocumentProcessing.URLhandler() {
			// this method will be called for each matched url
			@Override
			public void takeUrl(String url) {
				synchronized (lock) {
					if (!seen.contains(url)) { // seen on list, kuhu paneme kõik juba läbitöödeldud urlid
						queue.enqueue(url); // break lõpetab tsükli töö, continue korral edasi ei vaata aga läheb ikka uuele ringile
						seen.add(url);
					}
				}
			}
		};
		// to start, we push the initial url into the queue
		DocumentProcessing.handler.takeUrl(initialURL);
		do { // siin kontrollime kuni queues on midagi, mida alla laadida
			String url = queue.dequeue(); 

			new Thread(new Runnable() {
				public void run() {
					if (queue.isEmpty()) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					Xurl.download(url);
					return;
				}
			} ).start();

			while (queue.isEmpty() && threadCount < Thread.activeCount()) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} while (!queue.isEmpty());

		while (threadCount < Thread.activeCount()) { // If there are more active threads now, then there were during the start of the method, wait for them to close
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}



	@SuppressWarnings("unused")
	public static void threadPoolDownload(int poolSize, String initialURL) {
		int threadCount = Thread.activeCount();
		final URLQueue queue = new BlockingListQueue(); // sisestame siia listi kõik lingid, mis allalaetud failidest leiame
		final HashSet<String> seen = new HashSet<String>(); // siia salvestame need urlid, mis juba alla laetud
		final Object lock = new Object();
		// defines a new URLhandler
		DocumentProcessing.handler = new DocumentProcessing.URLhandler() {
			// this method will be called for each matched url
			@Override
			public void takeUrl(String url) {
				synchronized (lock) {
					if (!seen.contains(url)) { // seen on list, kuhu paneme kõik juba läbitöödeldud urlid
						queue.enqueue(url); // break lõpetab tsükli töö, continue korral edasi ei vaata aga läheb ikka uuele ringile
						seen.add(url);
					}
				}
			}
		};
		
		// to start, we push the initial url into the queue
		DocumentProcessing.handler.takeUrl(initialURL);
		
		for (int i = 0; i < poolSize; i++) {
			
			new Thread(new Runnable() {
				public void run() {
					
					do {
						Xurl.download(queue.dequeue());
						
						if (queue.isEmpty()) {
							try {
								Thread.sleep(100);
							}
							catch (InterruptedException e) {
								
							}
						}
						
					} while (!queue.isEmpty() && Thread.activeCount() >= threadCount + poolSize);
					return;
				}
			} ).start();
			
		}
		
		while (threadCount < Thread.activeCount()) { // If there are more active threads now, then there were during the start of the method, wait for them to close
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: java Wget url");
			System.exit(-1);
		}
		iterativeDownload(args[0]);
	}

}