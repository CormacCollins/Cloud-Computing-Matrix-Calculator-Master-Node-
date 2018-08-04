import java.net.Socket;

import org.ejml.simple.SimpleMatrix;

public class CalculationThread extends Thread implements MatrixPartitionOperations {

		private JobInfo jbInfo;
		private ThreadManager master;
		
	    public CalculationThread(JobInfo jb, ThreadManager man) {
	    	jbInfo = jb;	    	
	    	master = man;
	    }
	   
	    
	   public static SimpleMatrix calcMatrix(SimpleMatrix a, SimpleMatrix b) {		   
		   return a.mult(b);
	   }
	    
	   //----------------------------------------------
	   //TODO:
	   //Thinking about planning the shared data etc.
	   // -------------------------------------------------------------
	    public void run() {	    		    	
	    	switch (jbInfo.partitionType) {
			case row_column:
					singleVectorOpp 			
				break;
			case row_full:
				fullRowCalc
					break;
			case data_split:
					blockPartioning(a, b, workerNum)				
				break;
			case none:		
				break;
			}
	    	
	    	
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


		@Override
		public SimpleMatrix singleVectorOpp(SimpleMatrix a, SimpleMatrix b, int rowColNumber) {
			// TODO Auto-generated method stub
			return null;
		}


		@Override
		public SimpleMatrix fullRowCalc(SimpleMatrix a, SimpleMatrix b, int workerNum) {
			// TODO Auto-generated method stub
			return null;
		}


		@Override
		public SimpleMatrix blockPartioning(SimpleMatrix a, SimpleMatrix b, int workerNum) {
			// TODO Auto-generated method stub
			return null;
		}
	}
