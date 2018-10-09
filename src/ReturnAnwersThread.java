
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.invoke.ConstantCallSite;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.ejml.simple.SimpleMatrix;


//Class to be created for sending the answer back to original NdoeMAster

public class ReturnAnwersThread extends Thread  {

		ObjectOutputStream out;	
		Socket s;
		SendWork wReturn;
		public ReturnAnwersThread(Socket so, SendWork rtAnswer) {
			s= so;
			wReturn = rtAnswer;
		}

	    public void run() {	
	    	SendWork(s, wReturn);
	    }
	    
	    private void SendWork(Socket so, SendWork rtAnswer) {
	    	
			try {
				out = new ObjectOutputStream(so.getOutputStream());
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