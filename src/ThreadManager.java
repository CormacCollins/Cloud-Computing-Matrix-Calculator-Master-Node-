import java.awt.font.NumericShaper.Range;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Date;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.function.ToIntFunction;

import javax.naming.spi.DirStateFactory.Result;
import javax.print.attribute.standard.DateTimeAtCompleted;

import org.ejml.equation.Variable;
import org.ejml.simple.SimpleMatrix;
import org.junit.experimental.theories.Theories;
import org.junit.platform.commons.util.PreconditionViolationException;

// ----------------------------------------------- //
// Responsible for coordination of matrix threads  //
// ------------------------------------------------//


enum partition_type {row_column, row_full, data_split, none };


public class ThreadManager extends Thread  {
	//Socket information
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private int thrdManagerId;
    private BufferedReader reader;
    
    private ArrayList<CalculationThread> workerList = new ArrayList<>();
    
    //Matrix information
	public int idCount;
	private static int matrixSize;
	private partition_type partitionType;
	private static SimpleMatrix aMatrix;
	private static SimpleMatrix bMatrix;	
	private static double[][] result;
	private int workerReqSize;
	
	//where is the next part fo the matrices to be processed
	private static int rowSize;
	private static int colSize;
	private static Queue<int[]> jobQueue;
    
	public ThreadManager(Socket s, int threadid, int workerRequest ) throws IOException {
        socket = s;
        // will close it.
        thrdManagerId = threadid;	        
        workerReqSize = workerRequest;

        
        out = new ObjectOutputStream(socket.getOutputStream());
        
        reader = new BufferedReader(
				new InputStreamReader(socket.getInputStream()));
		
	}
	
	private SimpleMatrix createRandomSquareMatrix(int n) {
		double arr[][] = new double[n][n];
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < n; j++) {
				Random random = new Random(n);
				double a = random.nextDouble();
				arr[i][j]= a; 
			}
		}
		
		SimpleMatrix s = new SimpleMatrix(arr);
		return s;
		
	}
	
	public static synchronized boolean jobFinished() {
		return jobQueue.isEmpty();
	}
	 
	 public static synchronized int[] getJob() {
		 if(!jobQueue.isEmpty()) {
			 int arr[] = jobQueue.peek();
			 jobQueue.remove();
			 return arr;
		 }
		 
		 return new int[2]; 
	 }
	
	//returns matrix of requested segments from 
	 public static synchronized SimpleMatrix getMatrixARows(int rowAStart, int rowAEnd) {
		 
		 int size = (rowAEnd+1) - (rowAStart);
		 double[][] arr = new double[size][size];
		 double []arr2 = new double[matrixSize];
		 int counti = 0;
		 
		 int cue = jobQueue.size();
		 
		 // We can guarantee we will get the correct rows to calc 
		 // as the thread manager has split the jobs up already
		 for(int i = rowAStart; i < (rowAStart+size); i++) {
			 
			 if(i==2) {
				 int k = 0;
			 }
			 
			 //get column from i row
			 for(int j = 0; j < (matrixSize); j++) {
				arr2[j]= aMatrix.get(i, j);  
			 }
			 //add column to 2Darray
			 arr[counti++] = arr2; 
		 }
		 
		 SimpleMatrix result = new SimpleMatrix(arr);
		 return result;
	 }

			
	 
	 public static synchronized SimpleMatrix getMatrixBColumns(int startColB, int endColB) {
		 
		 int size = (endColB+1) - startColB;
		 double[][] arr = new double[size][size];
		 double []arr2 = new double[matrixSize];
		 int countj = 0;
		 
		 // We can guarantee we will get the correct rows to calc 
		 // as the thread manager has split the jobs up already
		 for(int i = startColB; i < (startColB+size); i++) {

			 //get column from i row
			 for(int j = 0; j < (matrixSize) ; j++) {
				arr2[j]= bMatrix.get(j, i);  
			 }
			 //add column to 2Darray
			 arr[countj++] = arr2; 
		 }
		 
		 SimpleMatrix result = new SimpleMatrix(arr);
		 return result;
	 }
	
	 
	 ///the arr size:  row = rowEnd - rowStart and the col = colEnd - colStart
	 public static synchronized void addToResMatrix(SimpleMatrix arr, int rowStart, int rowEnd, 
			 int colStart, int colEnd) {
		 int arrColIndex = 0;
		 int arrRowIndex = 0;
		 for(int i = rowStart; i <= (rowEnd) ; i++) {
			 for(int j = colStart; j <= (colEnd); j++) {
				 //shifting down through rows of a column
				 result[i][j] = arr.get(arrRowIndex, arrColIndex);
				 arrColIndex++;
			 }
			 arrRowIndex++;
		 }
	 }
	
	public void run() {	
		String line = "";
		try {
			line = reader.readLine();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Size recieved " + line);

		//Get info from client string
		parseRequest(line);
		
		idCount = thrdManagerId; //Thread manager is the first thread id   -- also because thrdManager could be 1 of many clients
		idCount++;
		
		//Setup the result matrix that all threads will be accessing
		result = new double[matrixSize][matrixSize];

		System.out.println("Queueing jobs");
		
		queueJobs();		
    	
		//Setup static matrices
    	int a[][] = {{1,1}, 
    			     {2,1}};
    	double ab[][] = {{1,1}, 
    			         {2,1}};
    	SimpleMatrix m1 = createRandomSquareMatrix(matrixSize);
    	SimpleMatrix m2 = createRandomSquareMatrix(matrixSize);
    	aMatrix = m1;
    	bMatrix = m2;    	
    	
    	//returns matrix result with error code attached
    	MatrixResult obj = calculate();
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
		
	}
	
	/// Instantiates the correct number of threads relative to the calculation type
	/// this involves allocating row and column information for them to work on 
	/// important to note - this is not shared data as each thread should be 
	/// operating on different parts of the matrix anyway
	public void queueJobs(){
		ArrayList<CalculationThread> workers = new ArrayList<CalculationThread>();


		int size = this.matrixSize;
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
				for(int i = 0; i < size*size; i += (size-1)) { //add row size each time {
					arr = new int[4]; 
					arr[0] = i;
					arr[1] = i + size-1;
					arr[2] = i;
					arr[3] = i + size-1;	
					jobQueue.add(arr);
				}
				break;
			case data_split:
				//Data split one
				
				int dataSplitSizeColCount = (int)size/count;
				int remainderSegment = size % count;
				
				
				//loop increments in the ize of the datasplit
				for(int i = 0; i < (size*size); i += (dataSplitSizeColCount)) {
					
					arr = new int[4]; 
					
					//it's the remainder row or it would not make it hear if remainder is 0
					if((i + remainderSegment) == size) {
						//we add a smaller portion to do
						arr[0] = i;
						arr[1] = i + (remainderSegment-1);
						arr[2] = i;
						arr[3] = i + (remainderSegment-1);
					}
					
					//allocate segment of calc (matrixA and matriB info)
					arr[0] = i;
					arr[1] = i + (dataSplitSizeColCount-1);
					arr[2] = i;
					arr[3] = i + (dataSplitSizeColCount-1);

					jobQueue.add(arr);
				}
				break;
			case none:		
				break;
			}
		
//			int sized = jobQueue.size();
//			for(int i = 0; i < sized; i++) {
//				int array[] = jobQueue.peek();
//				jobQueue.remove();
//
//				System.out.println( Integer.toString(array[0]) + " , " + Integer.toString(array[1])
//						+ ", " + Integer.toString(array[2]) + ", " + Integer.toString(array[3]));
//
//			}
					
	}
	
	/// Takes matrix size and partitioning type form client request
	public void parseRequest(String clientReq) {
		String arr[] = clientReq.split(":");
		if(arr.length != 2) {
			System.out.println("Incorrect parameters: needs 'calculationYype'-'matrixSize' ");
			partitionType = partition_type.none;	
		}

		
		
		String partType = arr[0];
		switch(partType) {
			case "row_column": partitionType = partition_type.row_column;
								break;
			case "row_full":  partitionType = partition_type.row_full;
								break;
			case "data_split": partitionType = partition_type.data_split;
								break;
			default: partitionType = partition_type.none;
					break;				
		}
		
        //set worker count also
        for(int i = 0; i < workerReqSize; i++) {
        	workerList.add(new CalculationThread(partitionType, this.idCount++)); 
        }
		
		matrixSize = Integer.parseInt(arr[1]);
	}
	
	//Based on current set requirements we will select how many worker threads we want to create
//	private void calcWorkerNumber(JobInfo jbInfo) {
//		int workerCount = 0;
//		switch (jbInfo.partitionType) {
//		case row_column:
//				workerCount = jbInfo.matrixSize * jbInfo.matrixSize; //n * n calcs (1 worker for each) 			
//			break;
//		case row_full:
//			workerCount = jbInfo.matrixSize; //1 worker per matrix row to complete
//				break;
//		case data_split:
//			workerCount = (jbInfo.matrixSize * jbInfo.matrixSize) / 4; 
//			//For now the partition will be in quarters - an n * n will always be an even number too!
//			break;
//		case none:
//			workerCount = 0;			
//			break;
//		}
//		
//		jbInfo.idCount = workerCount;	
//		
//	}
	
	//Create list of threads that have been given the job info, this will influence the calculation method they call when they are run()
//	private ArrayList<CalculationThread> getWorkers(JobInfo jbInfo){
		
//		int arr[]
//		
//		ArrayList<CalculationThread> workers = new ArrayList<CalculationThread>();
//		for(int i = 0; i < jbInfo.workerCount; i++) {
//			CalculationThread thread = new CalculationThread(jbInfo, this);
//			workers.add(thread);
//		}
		
//		return new ArrayList<CalculationThread>();
//	}
//	
	
	private void startWorkers(ArrayList<CalculationThread> workers) {
		for (CalculationThread calculationThread : workers) {
			calculationThread.run();
		}		
	} 
	
	private MatrixResult calculate() {
		int errorcode = 0;
		
		//int answer[][] = {{1,2}, {1,2}};
		//the workers will use the approppriately request calculation
		//workers = getWorkers(jbInfo);
		startWorkers(workerList);
		//join themfor answer
		try {
			for (CalculationThread calculationThread : workerList) {
				calculationThread.join();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Result = ");
		
		for(int i = 0; i < this.result.length; i++) {
			for(int j = 0; j < this.result.length; j++) {
			System.out.println(result[i][j] + ",");
			}
		}
		
		System.out.println("Result should be:..");
		SimpleMatrix aM = aMatrix;
		SimpleMatrix bM = bMatrix;
		SimpleMatrix correctAnser = aM.mult(bM);
		
		SimpleMatrix actual = new SimpleMatrix(result);
		boolean b = correctAnser.isIdentical(actual, 1);
		actual.print();
		correctAnser.print();
		System.out.println("Compare of actual answer = " + b);
		
		
		return new MatrixResult(result, errorcode);
	}
	
	
}













