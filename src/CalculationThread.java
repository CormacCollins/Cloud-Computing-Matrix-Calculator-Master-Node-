import java.lang.invoke.ConstantCallSite;
import java.net.Socket;

import org.ejml.simple.SimpleMatrix;

public class CalculationThread extends Thread  {

		private int thrId;
		private int aRowStart; 
		private int aRowEnd; 
		private int bColStart; 
		private int bColEnd;
		private partition_type pType;
		
		// ----------------------------------------------------------------------------//
		// Different consturctors for diff calcs - the thread will only operate once 
		// before it closes so there will be no risk of getting the wrong property
		
//		public CalculationThread( partition_type type, int id) {
//	    	pType = type;
//	    	thrId = id;
//		}
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
	    	this.aRowEnd = aRowEnd;
	    	this.aRowEnd = aRowEnd;
	    	this.bColStart = bColStart;
	    	this.bColEnd = bColEnd;
	    	pType = type;
	    	thrId = id;
	    }
	   
	    
	   public static SimpleMatrix calcMatrix(SimpleMatrix a, SimpleMatrix b) {		   
		   return a.mult(b);
	   }
	    
	   //----------------------------------------------
	   //TODO:
	   //Thinking about planning the shared data etc.
	   // -------------------------------------------------------------
	    public void run() {	    	
	    	double[] jobData = ThreadManager.getJob();
	    	SimpleMatrix res = calc(jobData[0], jobData[1], jobData[2], jobData[3]);
	    	ThreadManager.addToResMatrix(res, jobData[0], jobData[3]);
	    	
	    	
	    }

	    //requires only the mul of | (n by m) * (m by n)|
		public SimpleMatrix calc(int aRowStart, int aColStart, int bRowStart, int bColstart) {
			double rows[][] = ThreadManager.getMatrixARows(aRowStart, aColStart);
			SimpleMatrix aMatrix = new SimpleMatrix(rows);
			double cols[][] =ThreadManager.getMatrixBColumns(bRowStart, bColstart);
			SimpleMatrix bMatrix = new SimpleMatrix(rows);
			SimpleMatrix res = new SimpleMatrix(rows.length, cols.length); //square matrix
			
			
			//check outputs of matrix first
			res = aMatrix.mult(bMatrix.transpose());
			
			return res;
		}


	}
