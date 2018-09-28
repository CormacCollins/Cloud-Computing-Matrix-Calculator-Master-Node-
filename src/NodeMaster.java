import java.util.Queue;
import java.io.Console;
import java.io.IOException;
import java.security.KeyStore.Entry;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.ejml.data.DMatrixRMaj;
import org.ejml.simple.SimpleMatrix;
import org.omg.CORBA.PRIVATE_MEMBER;

public class NodeMaster extends Thread {
	
	//Socket information
	private int jobId;
	private String operationType;
	private static double[][] aMatrix;
	private static double[][] bMatrix;	
	private static double[][] result;
	private int workersCount;
	private int matrixSize;
	

	private double[][] answer;
	private boolean isFinished = false;
	
	//general queue array structure:
	// [0] = aMatrixRowStart
	// [1] = aMatrixRowEnd
	// [2] = bMatrixColStart
	// [3] = bMatrixEnd	
	private Queue<int[]> jobQueue;
	
	//added data structure for cloud comp version
	int taskIDCount = 0;
	Map<Integer, int[]> inProgressJobs = new HashMap<Integer, int[]>();

	Map<Integer, Double[][]> bRows;
	
	private static Job currentJob;
	
	//id for each worker and his jobId's
	int uniqueIDIncrementorForWOrkers = 0;
	Map<Integer, Integer[]> WorkerJobMap = new HashMap<Integer, Integer[]>();
	
	int PEAK_LOAD = 100;
  
	
	private boolean isDEBUGGING = false;
	
	
	// ---------------------------------------------------------------
	// ------------ PUBLIC FUNCTIONS ---------------------------------
	// ---------------------------------------------------------------
	
	//will likely need more checks
	public boolean jobIsFinished() {
		return jobQueue.isEmpty() && inProgressJobs.isEmpty();
	}
	
	
	public NodeMaster(String opType, double[][] matrixA, double[][] matrixB, int id, int currentWorkerAvailablity) throws IOException  {
		workersCount = currentWorkerAvailablity;
		setUpJob(matrixA, matrixB, opType, id);
		Map<Integer, Integer> workerTable = getWorkerTable();
		Map<Integer, int[]> inProgressJobs = new HashMap<Integer, int[]>();
	}

	
	public void run()  {
		sendWork();	
		isFinished = true;
//		System.out.println("Answer: ");
//		SimpleMatrix simpleMatrix = new SimpleMatrix(answer);
//		simpleMatrix.print();
	}	
	
	public double[][] getAnswer(){
		return answer;
		
	}
	
	// ---------------------------------------
	// --------- private calc funcs ---------
	// ---------------------------------------
	
	// should send all work of to nodes
	// later improvements may have checking during this loop for any failures
	private void sendWork() {
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
					inProgressJobs.put(taskIDCount, indices);
		
					
					//System.out.println("Local calc....");
					testCalcMultiplication(matrixARows, matrixBCols, taskIDCount);
					taskIDCount++;
					//TODO:make call to a node to send 
					//System.out.println("Node Socket communication not yet implemented");	
				
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
				inProgressJobs.put(taskIDCount, indices);
	
				
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
				inProgressJobs.put(taskIDCount, indices);
	
				
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
	
	
	//TODO:will get from the server class a list of the worker Id's and their load 
	private Map<Integer, Integer> getWorkerTable() {		
		Map<Integer, Integer> workerTable = new HashMap<Integer, Integer>();
		return workerTable;
	}
	
	private void setUpJob(double[][] matrixA, double[][] matrixB, String opType, int id) {
		
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
