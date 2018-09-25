import java.awt.List;
import java.awt.font.NumericShaper.Range;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyStore.Entry;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;
import java.util.function.ToIntFunction;

import javax.naming.spi.DirStateFactory.Result;
import javax.print.attribute.standard.DateTimeAtCompleted;
import javax.swing.text.Segment;

import org.ejml.data.MatrixType;
import org.ejml.equation.Variable;
import org.ejml.simple.SimpleMatrix;

// ----------------------------------------------- //
// Responsible for coordination of matrix threads  //
// ------------------------------------------------//


enum partition_type {row_column, row_full, none }; //no more data_split - removed for cloud computing version


public class ThreadManager extends Thread  {
	
	//Socket information
    private Socket socket;
    private int socketId;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private int thrdManagerId;
    private BufferedReader reader;
    private int jobId;
    
    private static ArrayList<CalculationThread> workerList = new ArrayList<>();
    
    //Matrix information
	public int idCount;
	private static int matrixSize;
	private partition_type partitionType;
	private String operationType;
	private static Double[][] aMatrix;
	private static Double[][] bMatrix;	
	private static Double[][] result;
	private int workerReqSize;
	
	//general queue array structure:
	// [0] = aMatrixRowStart
	// [1] = aMatrixRowEnd
	// [2] = bMatrixColStart
	// [3] = bMatrixEnd	
	private Queue<int[]> jobQueue;
	
	//added data structure for cloud comp version
	Map<Integer, Double[][]> aCols;
	Map<Integer, Double[][]> bRows;
	
	Job currentJob;
	
	//id for each worker and his jobId's
	int uniqueIDIncrementorForWOrkers = 0;
	Map<Integer, Integer[]> WorkerJobMap = new HashMap<Integer, Integer[]>();
	
	//Worker ID's and their loadCapacity
	Map<Integer, Integer> workerIDs = new HashMap<Integer, Integer> ();
	int PEAK_LOAD = 100;
    
	
	private boolean isDEBUGGING = false;
	
	
	public ThreadManager(Socket s, int threadid, int workerRequest ) throws IOException {
		
		operationType = "multiplication";
		
		//must reset, or threading goes nuts
		workerList = null;
		jobId = threadid;
        socket = s;
        // will close it.
        thrdManagerId = threadid;	        
        workerReqSize = workerRequest;

        
        out = new ObjectOutputStream(socket.getOutputStream());
        
        reader = new BufferedReader(
				new InputStreamReader(socket.getInputStream()));
        
        
		
	}
	
	private Double[][] createRandomSquareMatrix(int n) {
		 //
		//long mills = (System.currentTimeMillis() / 10000);

		Random random = new Random(n);
		
		Double arr[][] = new Double[n][n];
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < n; j++) {
				Double a = (Double)random.nextInt(100);
				arr[i][j]= a; 
			}
		}
		
		return arr;
		
	}
	

	public boolean jobFinished() {
		return jobQueue.isEmpty();
	}
	 
	 public int[] getJob() {
		 if(!jobFinished()) {
			 int arr[] = jobQueue.peek();
			 jobQueue.remove();
			 return arr;
		 }
		 
		 int arr2[] = null;
		 return arr2;
	 }
	
	//returns matrix of requested segments from 
	 public Double[][] getMatrixARows(int rowAStart, int rowAEnd) {
		 
		 int size = (rowAEnd+1) - (rowAStart);
		 Double [][]arr = new Double[size][size];
		 Double []arr2 = new Double[matrixSize];
		 int counti = 0;
		 
		 // We can guarantee we will get the correct rows to calc 
		 // as the thread manager has split the jobs up already
		 for(int i = rowAStart; i < (rowAStart+size); i++) {
			 
			 //get column from i row
			 for(int j = 0; j < (matrixSize); j++) {
				 
				arr2[j]= aMatrix[i][j];  
			 }
			 //add column to 2Darray
			 arr[counti++] = arr2; 

			 arr2 =  new Double[matrixSize];
		 }
		 
		 return arr;
	 }

			
	 
	 public Double[][] getMatrixBColumns(int startColB, int endColB) {
		 
		 int size = (endColB+1) - startColB;
		 Double[][] arr = new Double[size][size];
		 Double []arr2 = new Double[matrixSize];
		 int countj = 0;
		 
		 // We can guarantee we will get the correct rows to calc 
		 // as the thread manager has split the jobs up already
		 for(int i = startColB; i < (startColB+size); i++) {

			 //get column from i row
			 for(int j = 0; j < (matrixSize) ; j++) {
				arr2[j]= bMatrix[j][i];  
			 }
			 //add column to 2Darray
			 arr[countj++] = arr2; 

			 arr2 = new Double[matrixSize];
		 }
		 
		 return arr;
	 }
	 
	 // ----------------------------------------
	 //won't be used in cloud computing version
	 // -----------------------------------------
	 
	//	 public static SimpleMatrix getMatrixBColumnsDataSplit(int startColB, int endColB) {
	//		 
	//		 int size = (endColB+1) - startColB;
	//		 Double[][] arr = new Double[size][size];
	//		 Double []arr2 = new Double[matrixSize];
	//		 int countj = 0;
	//		 
	//		 // We can guarantee we will get the correct rows to calc 
	//		 // as the thread manager has split the jobs up already
	//		 for(int i = startColB; i < (startColB+size); i++) {
	//
	//			 //get column from i row
	//			 for(int j = 0; j < (matrixSize) ; j++) {
	//				//arr2[j]= bMatrix.get(j, i);  
	//			 }
	//			 //add column to 2Darray
	//			 arr[countj++] = arr2; 
	//		 }
	//		 
	//		 SimpleMatrix result = new SimpleMatrix(arr);
	//		 return result;
	//	 }
	 
	 public static SimpleMatrix getFullBMatrix() {
		 SimpleMatrix sMatrix = new SimpleMatrix(bMatrix);
		 return sMatrix;
	 }
	
	 
	 ///the arr size:  row = rowEnd - rowStart and the col = colEnd - colStart
	 public static void addToResMatrix(SimpleMatrix arr, int rowStart, int rowEnd, 
			 int colStart, int colEnd) {
		 int arrColIndex = 0;
		 int arrRowIndex = 0;
		 for(int i = rowStart; i <= (rowEnd) ; i++) {
			 for(int j = colStart; j <= (colEnd); j++) {
				 //shifting down through rows of a column
				 result[i][j] = arr.get(arrRowIndex, arrColIndex);
				 arrColIndex++;
			 }
			 arrColIndex = 0;
			 arrRowIndex++;
		 }
	 }
	
	public void run() {	
		String line = "";
		Status errorCode = Status.network_error;
		MatrixResult serverResult = null;
		try {
			line = reader.readLine();
		} catch (IOException e1) {
			errorCode = Status.client_request_read_error;
			
			if(isDEBUGGING)
				e1.printStackTrace();
			
		}

		//if we read request correctly
		if(errorCode != Status.client_request_read_error) {
			//Get info from client string
			parseRequest(line);
			
			//if improper request sent - we return an error
			//it will be set to none in the parsing process before this if the request type is invalid
			if(this.partitionType == partition_type.none) {
				errorCode = Status.invalid_paramaters;
			}
			else {
				idCount = thrdManagerId; //Thread manager is the first thread id   -- also because thrdManager could be 1 of many clients
				idCount++;
				
				//Setup the result matrix that all threads will be accessing
				result = new Double[matrixSize][matrixSize];
		
				//System.out.println("Queueing jobs");
				
				queueJobs();
				if(isDEBUGGING) {
					System.out.println("Matrix size request = " + matrixSize);
					System.out.println("Jobs to complete = " + jobQueue.size());
				}
				
				//Setup static matrices
		    	Double arr1[][] = createRandomSquareMatrix(matrixSize);
		    	Double arr2[][] = createRandomSquareMatrix(matrixSize);
		    	
		    	SimpleMatrix m1 = new SimpleMatrix(arr1);
		    	SimpleMatrix m2 = new SimpleMatrix(arr2);
		    	
		    	if(isDEBUGGING) {
			    	System.out.println("To be calculated:");
			    	m1.print();
			    	m2.print();
		    	}
		    	
		    	aMatrix = arr1;
		    	bMatrix = arr2;   	
		    	
		    	//returns matrix result with error code attached
		    	serverResult = calculate();
			}
		}
		
		//if we didn't get a calculate call we send an error call 
		if(serverResult == null) {
			serverResult = new MatrixResult(null, errorCode);
		}
	    	
    	try {
			out.writeObject(serverResult);

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
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			if(isDEBUGGING)
				e.printStackTrace();
		}
		
	}
	
	/// Instantiates the correct number of threads relative to the calculation type
	/// this involves allocating row and column information for them to work on 
	/// important to note - this is not shared data as each thread should be 
	/// operating on different parts of the matrix anyway
	public void queueJobs(){

		int size = matrixSize;
		// ---------------//
		// ---- arr ------//
		// ---------------//
		// [0] = aMatrixRowStart
		// [1] = aMatrixRowEnd
		// [2] = bMatrixColStart
		// [3] = bMatrixEnd
		
		int arr[];
		jobQueue = new LinkedList<int[]>();

		int range = matrixSize / workerReqSize;
		int count = workerReqSize;
		int residual = workerReqSize % 4;
			switch (partitionType) {
			case row_column:
				//1 row 1 element to be calculated at a time - easy to split up
				for(int i = 0; i < (size); i++) { 
					
					for(int j = 0; j < size; j++) {
						arr = new int[4]; 
						arr[0] = i;
						arr[1] = i;
						arr[2] = j;
						arr[3] = j;
						jobQueue.add(arr);
					}
				}
					
				break;
			case row_full:
				for(int i = 0; i < size; i++) { //add row size each time {
					arr = new int[4]; 
					arr[0] = i;
					arr[1] = i;  //going from row i to i+1
					arr[2] = 0;
					arr[3] = size-1;	//going from col 0 to end of cols (all of them essentially)
					jobQueue.add(arr);
				}
				break;
//			case data_split:
//				//Data split one
//
//				int dataSplitSizeColCount;
//				int remainderSegment;
//				
//				//this will also stop dataSplitSize being zero
//				if(workerReqSize > matrixSize) {
//					System.out.println("Worker size greater than matrix - fixing partition length");
//					//firstly we will only use workers with N >= 10
//					//there will be too little work so we just make small enough segments
//					//i.e. likely 2*2 to split between some workers
//					dataSplitSizeColCount = 2;
//					
//					//remainder segment stays the same
//				}
//				else {					
//					dataSplitSizeColCount = (int)size/count;
//				}
//
//				
//				remainderSegment = size % dataSplitSizeColCount;
//				boolean isUneven = false;
//				if(remainderSegment != 0) {
//					isUneven = true;
//				}
//				
//				
//				
//				int rowCount = 0;
//				
//				//add row splits
//				while(rowCount < ((size) - remainderSegment)) {
//					for(int j = 0; j <  size; j+=dataSplitSizeColCount) {
//						
//						//potentiual remainder sliver in col
//						if(( (size) - j== remainderSegment) && remainderSegment != 0) {
//							arr = new int[4]; 
//							arr[0] = rowCount;
//							arr[1] = rowCount + (dataSplitSizeColCount-1);
//							
//							arr[2] = j;
//							arr[3] = j + (remainderSegment-1);
//
//							jobQueue.add(arr);	
//							break;
//						}
//						
//						arr = new int[4]; 
//						arr[0] = rowCount;
//						arr[1] = rowCount + (dataSplitSizeColCount-1);
//						
//						arr[2] = j;
//						arr[3] = j + (dataSplitSizeColCount-1);
//
//						jobQueue.add(arr);	
//
//						
//					}					
//					rowCount += dataSplitSizeColCount;
//				}
//				
//				//may have a row left to allocate
//				while(rowCount <= (size) && remainderSegment != 0) {
//					for(int j = 0; j <  size; j+=dataSplitSizeColCount) {
//
//						//potentiual remainder sliver in col
//						if(((size) - j == remainderSegment) && remainderSegment != 0) {
//							arr = new int[4]; 
//							arr[0] = rowCount;
//							arr[1] = rowCount + (remainderSegment-1);
//							
//							arr[2] = j;
//							arr[3] = j + (remainderSegment-1);
//
//							jobQueue.add(arr);	
//							break;
//						}
//						
//						arr = new int[4]; 
//						arr[0] = rowCount;
//						arr[1] = rowCount + (remainderSegment-1);
//						
//						arr[2] = j;
//						arr[3] = j + (dataSplitSizeColCount-1);
//
//						jobQueue.add(arr);
//
//					}	
//					//this should break the loop
//					rowCount+=remainderSegment;
//				}
//				break;
			default:
				break;
			}

					
	}
	
	//this is an add on step to the job queuing now that we are actually giving the vec data info to a cloud worker
	//instead of having them access shared data through threading
	public int setUpVectorJobsForMultiplication() {
		
		aCols = new HashMap<Integer, Double[][]>();
		bRows = new HashMap<Integer, Double[][]>();
		int ids = 0;
		int totalCount = jobQueue.size();
		
		while(!jobQueue.isEmpty()) {
			//pop job from queue
			int[] jobIndexes = getJob();
			// NOTE - job indexes
			// [0] = aMatrixRowStart
			// [1] = aMatrixRowEnd
			// [2] = bMatrixColStart
			// [3] = bMatrixEnd
			
			Double[][] aCol = getMatrixARows(jobIndexes[0], jobIndexes[1]);
			Double[][] bRow = getMatrixBColumns(jobIndexes[2], jobIndexes[3]);
			
			//Add both cols and rows to maps so each task has a unique ID 
			//conveniently the same iD will reference the same row and cols that need to be multiplied
			aCols.put(ids, aCol);
			bRows.put(ids, bRow);
			ids++;
			
		}
		
		return totalCount;
		
	}
	
	/// Takes matrix size and partitioning type form client request
	public void parseRequest(String clientReq) {
		String arr[] = clientReq.split(":");
		if(arr.length != 2) {
			if(isDEBUGGING) {
				System.out.println("Incorrect parameters: needs 'calculationYype'-'matrixSize' ");
			}
			
			partitionType = partition_type.none;	
			return;
		}

		
		
		String partType = arr[0];
		switch(partType) {
			case "row_column": partitionType = partition_type.row_column;
								break;
			case "row_full":  partitionType = partition_type.row_full;
								break;
			case "data_split": System.out.println("'data_split' calculation type no longer available");
				partitionType = partition_type.none; // partition_type.data_split;
								break;
			default: partitionType = partition_type.none;
					break;				
		}
		
		matrixSize = Integer.parseInt(arr[1]);
		

			
	}
	

	
	private Job startWorkers(int workerCount, int jobCount, int jobID) {		

		Job newJob;
		int count = 0;
		
		//setup the vector jobs depending on the operation type
		switch(operationType) {
			case "multiplication":
				//allocates vector to 2 different hashmaps
				count = setUpVectorJobsForMultiplication();
				break;

		
		}
		
		
		//aCols and bRows were calculated in the above switch
		newJob = new Job(jobID, aCols, bRows, operationType);
		
		//will allocate one job at a time to the lowest load worker
		for(int j = 0; j < (jobCount); j++) {
			int workerID = getLowestLoadWorker();
			giveTaskToWorker(newJob.vecAJobs, newJob.vecBJobs, workerID);
		}
		

		return newJob;
	} 
	
	private MatrixResult calculate() {
		Status errorcode = Status.successful_calculation;

		try {
			startWorkers();
		}
		catch (Exception e) {
			//calculation error
			errorcode = Status.calc_error;
			if(isDEBUGGING) {
				e.printStackTrace();
			}
		}
		if(isDEBUGGING) {
			SimpleMatrix aM = new SimpleMatrix(aMatrix);
			SimpleMatrix bM = new SimpleMatrix(bMatrix);
			SimpleMatrix correctAnser = aM.mult(bM);
			
			SimpleMatrix actual = new SimpleMatrix(result);
			boolean b = correctAnser.isIdentical(actual, 1);

			System.out.println("Result = ");
			actual.print();
			correctAnser.print();
			System.out.println("Compare of actual answer = " + b);
		}
		
		SimpleMatrix aM = new SimpleMatrix(aMatrix);
		SimpleMatrix bM = new SimpleMatrix(bMatrix);
		SimpleMatrix correctAnser = aM.mult(bM);
		
		SimpleMatrix actual = new SimpleMatrix(result);
		boolean b = correctAnser.isIdentical(actual, 1);

		if(!b) {
			
			System.out.println("Result failed");
			actual.print();
			correctAnser.print();
//			
		}
		else {

			System.out.println("Successfully calculated");
			System.out.println("Matrix of size: " + matrixSize);
			System.out.println("Using " + workerReqSize + " workers.");
			System.out.println("Using the " + this.partitionType.toString() + " method");
			actual.print();
			correctAnser.print();
		}
		
		return new MatrixResult(result, errorcode);
	}
	
	private int giveTaskToWorker(Map<Integer, Double[][]> vecAJobs, Map<Integer, Double[][]> vecBJobs, int workerId) {
		
		
		
		//socket code to contact worker
		
		return 0;
		
	}
	

	private int getLowestLoadWorker() {
		int lowest = PEAK_LOAD;
		int lowestID = -1;
		for(Map.Entry<Integer, Integer> a : workerIDs.entrySet()) {
			if(a.getValue() < lowest) {
				lowest = a.getValue();
				lowestID = a.getKey();
			}
		}
		
		workerIDs.put(lowestID, workerIDs.get(lowestID) + 1);
		
		return lowestID;
		
	}
	
}













