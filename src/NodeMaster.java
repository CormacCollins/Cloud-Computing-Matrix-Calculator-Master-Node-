import java.util.Queue;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.ejml.data.DMatrixRMaj;
import org.ejml.simple.SimpleMatrix;

public class NodeMaster extends Thread {

	private String operationType;
	private static double[][] aMatrix;
	private static double[][] bMatrix;	
	private int matrixSize;
	private double[][] answer;
	private int workersCount;
	// [0] = aMatrixRowStart
	// [1] = aMatrixRowEnd
	// [2] = bMatrixColStart
	// [3] = bMatrixEnd	
	private Queue<int[]> jobQueue;
	private int masterID;
	private Socket socket;
	Socket so;
	//added data structure for cloud comp version
	// -------------------------------------------
	
	
	// for number of tasks that make up a job
	private int taskIDCount = 0;

	//String represents the id for a piece of work
	// this is a concatonatino of the Master ID + ":" + workID

	Map<String, Integer[]> workerJobMap = new HashMap<String, Integer[]>();
	Map<String, int[]> inProgressJobs = new HashMap<String, int[]>();
	
	//arbitrary peak for load balancing - to be changeds
	int PEAK_LOAD = 100;
	
	
	// ---------------------------------------------------------------
	// ------------ PUBLIC FUNCTIONS ---------------------------------
	// ---------------------------------------------------------------
	
	public synchronized void addAnswer(double[][] a, String id) {
		int[] indices = inProgressJobs.get(id);
		//Remember:
		// [0] = aMatrixRowStart
		// [1] = aMatrixRowEnd
		// [2] = bMatrixColStart
		// [3] = bMatrixEnd	
		//Therefore matrix C answer will be the same row indices as matrix A
		//the answer portion is always just 1 * row
		
		for(int i = 0; i < matrixSize; i++ ) {
			int rowStart = indices[0];
			answer[rowStart][i] = a[0][i];		
		}
		
		inProgressJobs.remove(id);
	}
	
	//will likely need more checks
	public boolean jobIsFinished() {
		return jobQueue.isEmpty() && inProgressJobs.isEmpty();
	}
	
	//request can be status or to stop job etc.
	//TODO: parsing of request etc.
	public String makeRequest(String req) {
		return "Requesting Not Implemented";
	}
	
	
	public NodeMaster(String opType, double[][] matrixA, double[][] matrixB, int id, Socket s) throws IOException  {	
		socket = s;
		masterID = id;
		workersCount = WorkerInfo.getWorkerCount();
		setUpJob(matrixA, matrixB, opType);
	}

	
	public void run()  {
		System.out.println("Node Master - " + masterID + " allocating work" );
		allocateWork();	
//		System.out.println("Answer: ");
//		SimpleMatrix simpleMatrix = new SimpleMatrix(answer);
//		simpleMatrix.print();
		System.out.println("Node Master - " + masterID + " finished sending work" );
		
		//will wait until it's answer has been accessed and taken
		waitForAnswerRetrieval();
	}	
	
	public synchronized void waitForAnswerRetrieval() {
		try {
			wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public double[][] getAnswer(){
		if(jobIsFinished()) {
			return answer;	
		}
		else {
			System.out.println("Job not finished yet - answer not available");
			return null;
		}
	}
	
	// ---------------------------------------
	// --------- private calc funcs ---------
	// ---------------------------------------
	
	// -----------------------------------------------------------------------
	// should send all work of to nodes
	// later improvements may have checking during this loop for any failures
	// -----------------------------------------------------------------------
	private void allocateWork() {
		switch (operationType) {
		case "multiplication":
				while(!jobQueue.isEmpty()) {
					int[] indices = jobQueue.peek();
					jobQueue.remove();
					//getting row from A and full matrix from B
					double[][] matrixARows = getMatrixARows(indices[0], indices[1]);
					double[][] matrixBCols = bMatrix;
					//give a small task an ID and store the indices we used 
					//in case we lose this data and to keep track of it's progress and then completion
					String id = createIdConcat(masterID, taskIDCount++);
					inProgressJobs.put(id, indices);
		
					SendWork sendWork = new SendWork(2, matrixARows, matrixBCols, id);
					
					
					sendToNode(sendWork);
					taskIDCount++;
					
					//System.out.println("Local calc....");
					//testCalcMultiplication(matrixARows, matrixBCols, taskIDCount);
					
			}		
			break;
		case "addition":
			while(!jobQueue.isEmpty()) {
				int[] indices = jobQueue.peek();
				jobQueue.remove();
				//getting row from A and full matrix from B
				double[][] matrixARows = getMatrixARows(indices[0], indices[1]);
				double[][] matrixBCols = getMatrixBRows(indices[0], indices[1]);
				//give a small task an ID and store the indices we used 
				//in case we lose this data and to keep track of it's progress and then completion
				inProgressJobs.put(createIdConcat(masterID, taskIDCount++), indices);
	
				
				//System.out.println("Local calc....");
				testCalcAddition(matrixARows, matrixBCols, taskIDCount, 1);
				taskIDCount++;
				

				//TODO:make call to a node to send 
				//System.out.println("Node Socket communication not yet implemented");
			}	
			break;
		case "subtraction":
			while(!jobQueue.isEmpty()) {
				int[] indices = jobQueue.peek();
				jobQueue.remove();
				//getting row from A and full matrix from B
				double[][] matrixARows = getMatrixARows(indices[0], indices[1]);
				double[][] matrixBCols = getMatrixBRows(indices[0], indices[1]);
				//give a small task an ID and store the indices we used 
				//in case we lose this data and to keep track of it's progress and then completion
				inProgressJobs.put(createIdConcat(masterID, taskIDCount++), indices);
	
				
				//System.out.println("Local calc....");
				testCalcAddition(matrixARows, matrixBCols, taskIDCount, -1);
				taskIDCount++;
				

				//TODO:make call to a node to send 
				//System.out.println("Node Socket communication not yet implemented");	
			}

			break;

		default:
			break;
		}

			
	}
	
	private void sendToNode(SendWork s) {
		
		try {
			so = new Socket("", 3000);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(so.getOutputStream());
			out.writeObject(s);
			//out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	private String createIdConcat(int primKey, int secondKey) {
		return Integer.toString(primKey) + ":" + Integer.toString(secondKey);
	}
	
	private void testCalcMultiplication(double[][] a, double[][] b, int taskID) {
		SimpleMatrix aMatrix = new SimpleMatrix(a);
		SimpleMatrix bMatrix = new SimpleMatrix(b);
//		System.out.println("Calculating matrices..");
//		aMatrix.print();
//		bMatrix.print();
		DMatrixRMaj  res = aMatrix.mult(bMatrix).getDDRM();
		//System.out.println("Answer..");
		//res.print();
		int rowSize = res.numCols;
		double[] resArr = res.getData();
		
		//get original coords of the completed job
		int[] indices = inProgressJobs.get(taskID);
		//indices 0-1 are the row position (always just 1 in our row by matrix mul)
		//we are putting a new row in the answer essnetially
		for(int i = 0; i < resArr.length; i++) {
			answer[indices[0]][i] = resArr[i];
		}
		
		inProgressJobs.remove(taskID);
		
	}
	
	private void testCalcAddition(double[][] a, double[][] b, int taskID, int sign) {
		SimpleMatrix aMatrix = new SimpleMatrix(a);
		SimpleMatrix bMatrix = new SimpleMatrix(b).scale(sign);
//		System.out.println("Calculating matrices..");
//		aMatrix.print();
//		bMatrix.print();
		DMatrixRMaj  res = aMatrix.plus(bMatrix).getDDRM();
		//System.out.println("Answer..");
		//res.print();
		int rowSize = res.numCols;
		double[] resArr = res.getData();
		
		//get original coords of the completed job
		int[] indices = inProgressJobs.get(taskID);
		//indices 0-1 are the row position (always just 1 in our row by matrix mul)
		//we are putting a new row in the answer essnetially
		for(int i = 0; i < resArr.length; i++) {
			answer[indices[0]][i] = resArr[i];
		}
		
		inProgressJobs.remove(taskID);
		
	}
	
	//get worker with lowest load in hashtable
	//TODO: rework later when we are load balancing
	private int getAvailableWorkerID(Map<Integer, Integer> workerTable) {
		int lowest = 1000;
		int lowestId = -1;
		for(java.util.Map.Entry<Integer, Integer> keyVal : workerTable.entrySet()) {
			if(keyVal.getValue() < lowest) {
				lowest = keyVal.getValue();
				lowestId = keyVal.getKey();				
			}
		}
		
		return lowestId;
	}
	
	private void setUpJob(double[][] matrixA, double[][] matrixB, String opType) {
		
		matrixSize = matrixA.length;
		aMatrix = matrixA;
		bMatrix = matrixB;
		operationType = opType;
		
		answer = new double[matrixA.length][matrixB.length];
		
		
		int arr[];
		jobQueue = new LinkedList<int[]>();
		
		switch (operationType) {
		case "multiplication":
			//divide row-col indices up into work
			for(int i = 0; i < matrixSize; i++) { 
				arr = new int[4]; 
				arr[0] = i;
				arr[1] = i;  //going from row i to i+1
				arr[2] = 0;
				arr[3] = matrixSize-1;	//going from col 0 to end of cols (all of them essentially)
				jobQueue.add(arr);
			}			
			break;
		case "addition":
			for(int i = 0; i < matrixSize; i++) { 
				// --- arr ----//
				// 0-1 row in A
				// 2-3 row in B				
				arr = new int[4]; 
				arr[0] = i;
				arr[1] = i;  //going from row i to i+1
				arr[2] = i;
				arr[3] = i;	//going from col 0 to end of cols (all of them essentially)
				jobQueue.add(arr);
			}			
			break;
		case "subtraction":
			for(int i = 0; i < matrixSize; i++) { 
				// --- arr ----//
				// 0-1 row in A
				// 2-3 row in B				
				arr = new int[4]; 
				arr[0] = i;
				arr[1] = i;  //going from row i to i+1
				arr[2] = i;
				arr[3] = i;	//going from col 0 to end of cols (all of them essentially)
				jobQueue.add(arr);
			}			
			break;

		default:
			break;
		}
				
	}
		
	//returns matrix of requested segments from 
	private double[][] getMatrixARows(int rowAStart, int rowAEnd) {
		 
		 int size = (rowAEnd+1) - (rowAStart);
		 double [][]arr = new double[size][size];
		 double []arr2 = new double[matrixSize];
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

			 arr2 =  new double[matrixSize];
		 }
		 
		 return arr;
	 }
	
	private double[][] getMatrixBRows(int rowAStart, int rowAEnd) {
		 
		 int size = (rowAEnd+1) - (rowAStart);
		 double [][]arr = new double[size][size];
		 double []arr2 = new double[matrixSize];
		 int counti = 0;
		 
		 // We can guarantee we will get the correct rows to calc 
		 // as the thread manager has split the jobs up already
		 for(int i = rowAStart; i < (rowAStart+size); i++) {
			 
			 //get column from i row
			 for(int j = 0; j < (matrixSize); j++) {
				 
				arr2[j]= bMatrix[i][j];  
			 }
			 //add column to 2Darray
			 arr[counti++] = arr2; 

			 arr2 =  new double[matrixSize];
		 }
		 
		 return arr;
	 }
	 
	private double[][] getMatrixBColumns(int startColB, int endColB) {
		 
		 int size = (endColB+1) - startColB;
		 double[][] arr = new double[size][size];
		 double []arr2 = new double[matrixSize];
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

			 arr2 = new double[matrixSize];
		 }
		 
		 return arr;
	 }
	

	

}
