import java.lang.invoke.ConstantCallSite;
import java.net.Socket;

import org.ejml.simple.SimpleMatrix;

public class CalculationThread extends Thread implements MatrixPartitionOperations {

		private int thrId;
		private int aRowStart; 
		private int aRowEnd; 
		private int bColStart; 
		private int bColEnd;
		private partition_type pType;
		
		// ----------------------------------------------------------------------------//
		// Different consturctors for diff calcs - the thread will only operate once 
		// before it closes so there will be no risk of getting the wrong property
		
		
		
	    public CalculationThread(int aRowStart, int bColStart, partition_type type, int id) {
	    	this.aRowStart = aRowStart;
	    	this.bColStart = bColStart;
	    	pType = type;
	    	thrId = id;
	    }
	    
	    public CalculationThread(ThreadManager man, int aRowStart, partition_type type, int id) {
	    	this.aRowEnd = aRowStart;
	    	pType = type;
	    	thrId = id;
	    }
	    
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
	    	switch (pType) {
			case row_column:
					//only need start of each because just doing 1 calc
				row_column(aRowStart, bColStart); 			
				break;
			case row_full:
				//Only need row for a - as we are doing a full calc against each col in b
				row_full(aRowStart);
				break;
			case data_split:
				//Need exact locations to calc on a and b
				data_split(aRowStart, aRowEnd, bColStart, bColEnd);
				break;
			case none:		
				break;
			}
	    }


		@Override
		public SimpleMatrix row_column(int i, int j) {
			SimpleMatrix aRow = ThreadManager.getMatrixARows(i, i+1);
			SimpleMatrix bCol =ThreadManager.getMatrixBColumns(j, j+1).transpose();
			SimpleMatrix reSimpleMatrix = new SimpleMatrix(1,1);
			double sum = aRow.dot(bCol);
			reSimpleMatrix.set(0, 0, sum);
			System.out.println("Thread number " +thrId + " Just calculated a single row_col sum: " + sum);
			return null;
		}


		@Override
		public SimpleMatrix row_full(int row) {
			System.out.println("Thread number " +thrId + " Just calculated a full row");
			return null;
		}


		@Override
		public SimpleMatrix data_split(int aRowStart, int aRowEnd, int bColStart, int bColEnd) {
			System.out.println("Thread number " +thrId + " Just calculated a segment of data");
			return null;
		}
	}
