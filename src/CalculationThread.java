import java.lang.invoke.ConstantCallSite;
import java.net.Socket;

import org.ejml.simple.SimpleMatrix;

public class CalculationThread extends Thread  {

		private int thrId;
		private partition_type pType;
		
		// ----------------------------------------------------------------------------//
		// Different consturctors for diff calcs - the thread will only operate once 
		// before it closes so there will be no risk of getting the wrong property
		
		public CalculationThread( partition_type type, int id) {
	    	pType = type;
	    	thrId = id;
		}
//		
//	    public CalculationThread(int aRowStart, int bColStart, partition_type type, int id) {
//	    	this.aRowStart = aRowStart;
//	    	this.bColStart = bColStart;
//	    	pType = type;
//	    	thrId = id;
//	    }
//	    
//	    public CalculationThread(ThreadManager man, int aRowStart, partition_type type, int id) {
//	    	this.aRowEnd = aRowStart;
//	    	pType = type;
//	    	thrId = id;
//	    }
	    
	    public CalculationThread(int aRowStart, int aRowEnd, int bColStart, int bColEnd, 
	    		partition_type type, int id) {
	    	pType = type;
	    	thrId = id;
	    }
	   
	    
	   public static SimpleMatrix calcMatrix(SimpleMatrix a, SimpleMatrix b) {		   
		   return a.mult(b);
	   }

	    public void run() {	
	    	System.out.println("Thread id " + thrId + " started jobs");
	    	boolean didWork = false;


    		int[] jobData = ThreadManager.getJob();
	    	while(true) {
	    		if(jobData == null) {
	    			break;
	    		}
	    		didWork = true;
	    		System.out.println("Thread id " + thrId + " calculating");
		    	SimpleMatrix res = calc(jobData[0], jobData[1], jobData[2], jobData[3]);
		    	ThreadManager.addToResMatrix(res, jobData[0], jobData[1], jobData[2], jobData[3]);
		    	

	    		jobData = ThreadManager.getJob();

	    	}

	    	
//	    	if(!didWork) {
//	    		System.out.println("Thread id " + thrId + " finished and did not do any work");
//	    	} else {
	    		System.out.println("Thread id " + thrId + " finished");
	    //	}
	    	 
	    }

	    //requires only the mul of | (n by m) * (m by n)|
		public SimpleMatrix calc(int aRowStart, int aRowEnd, int bColStart, int bColEnd) {
			SimpleMatrix aMatrix = ThreadManager.getMatrixARows(aRowStart, aRowEnd);
			SimpleMatrix bMatrix = ThreadManager.getMatrixBColumns(bColStart, bColEnd);
			//because square matrices of a and b - the size will be A row by B col
			//check outputs of matrix first
			bMatrix = bMatrix.transpose();
			SimpleMatrix res = new SimpleMatrix(aMatrix.numRows(), bMatrix.numCols()); 
			

			res = aMatrix.mult(bMatrix).copy();
			
			return res;
		}


	}
