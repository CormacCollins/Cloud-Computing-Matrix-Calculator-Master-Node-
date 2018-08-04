import java.io.*;
import java.net.*;

public class CalculationThread extends Thread {
	    private Socket socket;
	    private ObjectInputStream in;
	    private ObjectOutputStream out;
	    private int thId;
	    private BufferedReader reader;

	    public CalculationThread(Socket s, int threadid) throws IOException {
	        socket = s;
	        // will close it.
	        thId = threadid;	        
	        out = new ObjectOutputStream(socket.getOutputStream());
	        
	        reader = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));

	    }

	    public void run() {	    	
//	    	System.out.println("Reading client request");				
			// read the message from client and parse the execution
//			String line = "Error in";
//			try {
//				line = reader.readLine();
//			} catch (IOException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			System.out.println(line + " size matrix requested");		
	    	
			// read the message from client and parse the execution
			String line = "";
			try {
				line = reader.readLine();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("Size recieved " + line);

	    	
	    	System.out.println("Writing result");
	    	int a[][] = {{1}, {2}};
	    	MatrixResult obj = new MatrixResult(a, 5);
	    	try {
				out.writeObject(obj);

		    	out.flush();

	    		out.close();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
	    	System.out.println("Finished writing result");
	    	
	    	try {
				// closing the reader also closes input stream?
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
//	    	System.out.println("Thread " + thId + " is sleeping thread for 5 seconds...");
//	            	try {
//						sleep(5000);
//						
//
//						
//	            	} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} finally {
//						
//					}
//	            	
//	            	System.out.println("Thread " + thId + " finished sleeping");
	    }
	}
