
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.invoke.ConstantCallSite;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;



//Class to be created for sending the answer back to original NdoeMAster

public class ReturnAnwersThread extends Thread  {

		ObjectOutputStream out;	
    	Socket socket;
		SendWork wReturn;
		public ReturnAnwersThread(SendWork rtAnswer) {
			wReturn = rtAnswer;
		}

	    public void run() {	
	    	sendToServer(wReturn);
	    }
	    
	    private void sendToServer(SendWork rtAnswer) {

			try {
				socket = new Socket("137.116.128.225", 1024);
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    	
			try {
				out = new ObjectOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				out.writeObject(rtAnswer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	   

	}