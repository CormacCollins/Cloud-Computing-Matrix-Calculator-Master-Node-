import java.lang.invoke.ConstantCallSite;
import java.net.Socket;

import org.ejml.simple.SimpleMatrix;

public class CalculationThread extends Thread  {

		private int thrId;
		private partition_type pType;
		private int jobID;
		
		public CalculationThread(int id, partition_type pt, int jobId) {
	    	thrId = id;
	    	pType = pt;
	    	jobID = jobId;
		}
	   
	    
	   public static SimpleMatrix calcMatrix(SimpleMatrix a, SimpleMatrix b) {		   
		   return a.mult(b);
	   }

	    public void run() {	
	    	
	    	//System.out.println("Thread id " + thrId + " started jobs");
	    	int jobsCompleted = 0;


    		int[] jobData = ThreadManager.getJob();
	    	while(true) {
	    		if(jobData == null) {
	    			break;
	    		}
	    		//System.out.println("Thread id " + thrId + " calculating on job id " + jobID);
		    	SimpleMatrix res = calc(jobData[0], jobData[1], jobData[2], jobData[3]);
		    	ThreadManager.addToResMatrix(res, jobData[0], jobData[1], jobData[2], jobData[3]);
		    	jobsCompleted++;

	    		jobData = ThreadManager.getJob();

	    	}


	    		//System.out.println("Thread id " + thrId + " finished " + jobsCompleted + " jobs on jobID " + jobID);

	    	 
	    }

	    //requires only the mul of | (n by m) * (m by n)|
		public SimpleMatrix calc(int aRowStart, int aRowEnd, int bColStart, int bColEnd) {
			SimpleMatrix aMatrix = ThreadManager.getMatrixARows(aRowStart, aRowEnd);
			SimpleMatrix bMatrix = null;
			
			
			//FOR CLARITY
			switch (pType) {
			case row_column:
				//because square matrices of a and b - the size will be A row by B col
				//check outputs of matrix first
				bMatrix = ThreadManager.getMatrixBColumns(bColStart, bColEnd);
				bMatrix = bMatrix.transpose();
				break;
			case row_full:
				bMatrix = ThreadManager.getFullBMatrix();
				break;
				//do nothing - we will make a (1 x n) * (n * n) matrix multiplication below
			case data_split:
				bMatrix = ThreadManager.getMatrixBColumns(bColStart, bColEnd);
				bMatrix = bMatrix.transpose();
				break;
				//do nothing - same as above but its a dataSplitSize = m: (m x n)(n x m) multiplication
			default:
				break;
			}
			
			SimpleMatrix res = new SimpleMatrix(aMatrix.numRows(), bMatrix.numCols());
			res = aMatrix.mult(bMatrix).copy();
			
			return res;
		}


	}
