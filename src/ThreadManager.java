import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.ToIntFunction;

import javax.naming.spi.DirStateFactory.Result;

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
    
	public ThreadManager(Socket s, int threadid, int workerRequest ) throws IOException {
        socket = s;
        // will close it.
        thrdManagerId = threadid;	        
        workerReqSize = workerRequest;
        out = new ObjectOutputStream(socket.getOutputStream());
        
        reader = new BufferedReader(
				new InputStreamReader(socket.getInputStream()));
		
	}
	
	
	//returns matrix of requested segments from 
	 public static synchronized SimpleMatrix getMatrixARows(int rowStart, int rowEnd) {
		 double arr[][] = new double[rowEnd-rowStart][matrixSize];
		 int arrRowIndex = 0;
		 for(int i = rowStart; i < rowEnd; i++) {
			 for(int j = 0; j < matrixSize; j++) {
				 arr[arrRowIndex][j]= aMatrix.get(i, j);
			 }
			 arrRowIndex++;
		 }
		 
		 return new SimpleMatrix(arr);
	 }
	 
	 public static synchronized SimpleMatrix getMatrixBColumns(int bColStart, int bColEnd) {
		 double arr[][] = new double[bColEnd - bColStart][matrixSize];
		 int arrColIndex = 0;
		 for(int i = bColStart; i < bColEnd; i++) {
			 for(int j = 0; j < matrixSize; j++) {
				 //shifting down through rows of a column
				 arr[arrColIndex][j] = bMatrix.get(j, i);
			 }
			 arrColIndex++;
		 }
		 
		 //transpose back to a column
		 return new SimpleMatrix(arr).transpose();
	 }
	
	 ///the arr size:  row = rowEnd - rowStart and the col = colEnd - colStart
	 public static synchronized void addToResMatrix(SimpleMatrix arr, int rowStart, int rowEnd, int colStart, int colEnd) {
		 int arrColIndex = 0;
		 int arrRowIndex = 0;
		 for(int i = rowStart; i < rowEnd; i++) {
			 for(int j = colStart; j < colEnd; j++) {
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
		
		workerList = initWorkers();		
    	
		System.out.println("Running workers");
		
		//Setup static matrices
    	int a[][] = {{1,1}, {2,1}};
    	double ab[][] = {{1,1}, {2,1}};
    	SimpleMatrix m1 = new SimpleMatrix(ab);
    	SimpleMatrix m2 = new SimpleMatrix(ab);
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
	public ArrayList<CalculationThread> initWorkers(){
		ArrayList<CalculationThread> workers = new ArrayList<CalculationThread>();
		
		//adds the required n to jobinfo
		//calcWorkerNumber(jbInfo);
		switch (partitionType) {
		case row_column:
			for (int i = 0; i < matrixSize; i++) {
				for(int j = 0; j < matrixSize; j++) {
					
					//Add a new thread, increment the count, add it to the manager list
					//JobInfo copy = jbInfo.copy();
					
					CalculationThread newThrd = new CalculationThread(i,j, partitionType, idCount); 
					idCount++;
					workers.add(newThrd);					
					
				}
			}
			break;
		case row_full:
			for(int i = 0; i < matrixSize; i++) {
				//one thread for each row calc on A
				CalculationThread newThrd1 = new CalculationThread(this, i, partitionType, idCount);
				idCount++;
				workers.add(newThrd1);					
			}
			break;
		case data_split:
			//Uneven row num??????????????????????????
			
			
			//Essentially splitting the matrix into quarters for now (as in the specs)
			int size = (matrixSize) / 2; 
			for(int i = 0; i < matrixSize; i += size) {
				//once a segment of rows form matrix b is calculated, then we continue using
				// the ith row in matrix a to calc the next column segment in b
				int bSegments = size/2;
				while(bSegments != 0) {
					for(int j = 0; j < size; j++) {
							CalculationThread newThrd1 = 
									new CalculationThread(i, i + (size-1), j, j + (size-1), partitionType, idCount);   
							idCount++;
							workers.add(newThrd1);	
						}
					bSegments--;
				}
			}
			break;
		case none:		
			break;
		}
		
		return workers;
		
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
		
		int answer[][] = {{1,2}, {1,2}};
		//the workers will use the approppriately request calculation
		//workers = getWorkers(jbInfo);
		startWorkers(workerList);
		System.out.println("Returning placeholder matrix");
		return new MatrixResult(answer, errorcode);
	}
	
	
}
