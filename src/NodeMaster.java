import java.util.Queue;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
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
	private boolean workHasBeenAllocated = false;
	
	//arbitrary peak for load balancing - to be changeds
	int PEAK_LOAD = 100;
	
	
	// ---------------------------------------------------------------
	// ------------ PUBLIC FUNCTIONS ---------------------------------
	// ---------------------------------------------------------------
	
	public synchronized void addAnswer(double[][] a, String id) {
		System.out.println("Adding work id: " + id);
		int[] indices = inProgressJobsAccess().get(id);
		
		if(indices == null) {
			System.out.println("Answer for work " + id + " already added");
			return;
		}
		
		//Remember:
		// [0] = aMatrixRowStart
		// [1] = aMatrixRowEnd
		// [2] = bMatrixColStart
		// [3] = bMatrixEnd	
		//Therefore matrix C answer will be the same row indices as matrix A
		//the answer portion is always just 1 * row
		
		try {
			for(int i = 0; i < matrixSize; i++ ) {
				int rowStart = indices[0];
				answer[rowStart][i] = a[0][i];		
			}
			
			inProgressJobs.remove(id);
		} catch (Exception e) {
			System.out.println("...");
			// TODO: handle exception
		}

	}
	
	private synchronized Queue<int[]> jobQueueAccess() {
		return jobQueue;
	}
	
	private synchronized Map<String, int[]> inProgressJobsAccess() {
		return inProgressJobs;
	}
	
	//will likely need more checks
	public boolean jobIsFinished() {
		return jobQueueAccess().isEmpty() && inProgressJobsAccess().isEmpty() && workHasBeenAllocated; 
		//*If work was a large amount of allocating, someone may check at a point where the jobQUeue has nothing yet - veyr unlikely
	}
	
	//request can be status or to stop job etc.
	//TODO: parsing of request etc.
	public String makeRequest(String req) {
		return "Requesting Not Implemented";
	}
	
	
	public NodeMaster(String opType, double[][] matrixA, double[][] matrixB, int id, Socket s, int workerCount) throws IOException  {	
		socket = s;
		masterID = id;
		workersCount = workerCount;
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
		//waitForAnswerRetrieval();
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
				while(!jobQueueAccess().isEmpty()) {
					int[] indices = jobQueue.peek();
					jobQueueAccess().remove();
					//getting row from A and full matrix from B
					double[][] matrixARows = getMatrixARows(indices[0], indices[1]);
					double[][] matrixBCols = bMatrix;
					//give a small task an ID and store the indices we used 
					//in case we lose this data and to keep track of it's progress and then completion
					String id = createIdConcat(masterID, taskIDCount++);
					inProgressJobsAccess().put(id, indices);
		
					SendWork sendWork = new SendWork(2, matrixARows, matrixBCols, id);
					sendToNode(sendWork);
				
			}		
			break;
		case "addition":
			while(!jobQueueAccess().isEmpty()) {
				int[] indices = jobQueueAccess().peek();
				jobQueueAccess().remove();
				//getting row from A and full matrix from B
				double[][] matrixARows = getMatrixARows(indices[0], indices[1]);
				double[][] matrixBCols = getMatrixBRows(indices[0], indices[1]);
				
				
				//give a small task an ID and store the indices we used 
				//in case we lose this data and to keep track of it's progress and then completion
				String id = createIdConcat(masterID, taskIDCount++);
				inProgressJobsAccess().put(id, indices);
	
				SendWork sendWork = new SendWork(1, matrixARows, matrixBCols, id);
				sendToNode(sendWork);
			}	
			break;
		case "subtraction":
			while(!jobQueueAccess().isEmpty()) {
				int[] indices = jobQueueAccess().peek();
				jobQueueAccess().remove();
				//getting row from A and full matrix from B
				double[][] matrixARows = getMatrixARows(indices[0], indices[1]);
				double[][] matrixBCols = getMatrixBRows(indices[0], indices[1]);
				//give a small task an ID and store the indices we used 
				//in case we lose this data and to keep track of it's progress and then completion
				String id = createIdConcat(masterID, taskIDCount++);
				inProgressJobsAccess().put(id, indices);
	
				SendWork sendWork = new SendWork(3, matrixARows, matrixBCols, id);
				
				sendToNode(sendWork);
			}

			break;

		default:
			break;
		}
		
		workHasBeenAllocated = true;
			
	}

	private void sendToNode(SendWork s) {
		
		

		try {
			

			String bestWorker = null;
			//keep repeating the loop, until some worker are free now, it wont sent out 
			while(bestWorker == null) {
				bestWorker = WorkerInfo.getAvailableNode();
			}
			so = new Socket(bestWorker, 1024);
			so.setSoTimeout(10000);
			DataOutputStream dos = new DataOutputStream(so.getOutputStream());
			dos.writeBoolean(true);

			ObjectOutputStream out = new ObjectOutputStream(so.getOutputStream());
			out.writeObject(s);

		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		
	}
	
	
	private String createIdConcat(int primKey, int secondKey) {
		return Integer.toString(primKey) + ":" + Integer.toString(secondKey);
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
				jobQueueAccess().add(arr);
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
				jobQueueAccess().add(arr);
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
				jobQueueAccess().add(arr);
			}			
			break;

		default:
			break;
		}
				
	}
		
	//returns matrix of requested segments from 
	private double[][] getMatrixARows(int rowAStart, int rowAEnd) {
		try {
			 double[] row = aMatrix[rowAStart];
			 double[][] arr = new double[1][];
			 arr[0] = row;
			 return arr;
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}

	 }
	
	private double[][] getMatrixBRows(int rowAStart, int rowAEnd) {
		 
		double[] row = bMatrix[rowAStart];
		 double[][] arr = new double[1][];
		 arr[0] = row;
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
