import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.lang.reflect.Executable;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.simple.SimpleMatrix;
import org.omg.CORBA.PRIVATE_MEMBER;


public class MatrixServer {
	protected Socket socket;
	//unique id that's incremented for each use
	static int uniqueIdCount = 0;
	//unique look up hash for all worker nodeMasters
	private Map<Integer, NodeMaster> nodeMasterList = new HashMap<Integer, NodeMaster>();
	//public static ArrayList<CalculationThread> threadList = new ArrayList<CalculationThread>();
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int port = 1024;
		int socketPort = 1000;
		int count = 0;
		int workerCount = 1;
		int []socketList = new int[10000];
		int socketIndex = 0;
		if (args.length == 2) {
			try {
				socketPort = Integer.parseInt(args[0]);
				workerCount = Integer.parseInt(args[1]);
			}
			catch(Exception e){
			}
		}
		else {
			//System.out.println("Default port: " + port + " and deafult workerCount " + workerCount);
		}
		
		
		System.out.println("Matrix server is running on port " + port + "...");
		//System.out.println("Requesting " + workerCount + " workers");
		// create a server socket and wait for client's connection
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(port);
			while(true) {
				
				Socket socket = serverSocket.accept();
				//System.out.println("Socket number " + count + " open.");
				
						
				//create new server to communicate permanently with client				
				MatrixServer matrixServer = new MatrixServer();
				matrixServer.setSocket(socket);				

				//will run random tests on mul add and subtraction
				//matrixServer.fullCalculationTest();
				
				double[][] arr = {{1,4}, {1,4}};
				double[][] arr2 = {{2,4}, {2,4}};
				
				NodeMaster nMaster = new NodeMaster("multiplication", arr ,arr2, uniqueIdCount, 5);
				nMaster.start();   //.start() for threading
				uniqueIdCount++;
//				
//				NodeMaster nMaster = new NodeMaster("addition", arr ,arr2, count, 2);
//				nMaster.start();
//			
				//ThreadManager calculationThread = new ThreadManager(socket, count, workerCount);
				//calculationThread.start();			
				count++;

			}
		} catch (IOException e) {
			// Server failed
			e.printStackTrace();
			
		}
		
	}
	
	
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	
	// ----------------------------------------------------------------
	// create new nodeMaster and get the unique Job id assigned to it
	// ----------------------------------------------------------------
	private int createNewNodeMaster(String opType, double[][] matrixA, double[][] matrixB, int currentWorkerAvailablity) {
		NodeMaster nMaster;
		try {
			nMaster = new NodeMaster("multiplication", matrixA ,matrixB, uniqueIdCount, 5);
		} catch (IOException e) {
			nMaster = null;
			System.out.println("Error constructing NodeMaster id: " + uniqueIdCount);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		nodeMasterList.put(uniqueIdCount, nMaster);
		return uniqueIdCount++;
	}
	
	
	//TODO: May need the req and or answer to be parsed more before returning to server
	//request sent and: answer / info / outcome is returned
	private String requestToMasterNode(int id, String request) {
		return getNodeMasterByID(id).makeRequest(request);
	}
	
	
	// ------------------------------------------------------
	// get the reference to master working on particular job
	// ------------------------------------------------------
	private NodeMaster getNodeMasterByID(int id) {
		return nodeMasterList.get(id);
	}
	
	
	public void execute() {
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			// read the message from client and parse the execution
			String line = reader.readLine();


			// write the result back to the client
			BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream()));
			
			// ---- WRITE BACK OUTPUT ---- //
			
			writer.write("Recieved: " + line + "Giving back the result....");
			writer.newLine();
			writer.write("Here is the result");
//			writer.write(""+result);
			writer.newLine();
			writer.flush();
			
			// close the stream
			reader.close();
			writer.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// -------------------------------------------------
	// Wrapper for testing function to test full range
	// ------------------------------------------------
	public void fullCalculationTest() throws IOException {
		
		System.out.println("Testing for worker count 1");	
		testFunc("multiplication", 1);
		testFunc("addition", 1);
		testFunc("subtraction", 1);
			
		System.out.println("Testing for worker count 4");	
		testFunc("multiplication", 4);
		testFunc("addition", 4);
		testFunc("subtraction", 4);
		
		System.out.println("Testing for worker count 10");	
		testFunc("multiplication", 10);
		testFunc("addition", 10);
		testFunc("subtraction", 10);
		
		System.out.println("Testing for worker count 100");	
		testFunc("multiplication", 100);
		testFunc("addition", 100);
		testFunc("subtraction", 100);
	}
	
	
	// ------------------------------------------------------------
	// Will test matrix operations with large variets of matrix size
	// THis is to maintain correctness throughout production
	// -------------------------------------------------------------
	public void testFunc(String operationType, int workers) throws IOException {
		int id = 0;
		int testCount = 100;
		boolean allAreTrue = true;
		for(int i = 0; i < testCount; i++) {
			
			Random rand = new Random();
			SimpleMatrix A = SimpleMatrix.random_DDRM(20,20,-10,10,rand);
			SimpleMatrix B = SimpleMatrix.random_DDRM(20,20,-10,10,rand);
			
			double[][] arr = new double[A.numRows()][A.numCols()];
			for(int k = 0; k < A.numCols(); k++) {
				arr[k]= A.rows(k, k+1).getDDRM().getData();
			}
			
			double[][] arr2 = new double[A.numRows()][A.numCols()];
			for(int k = 0; k < A.numCols(); k++) {
				arr2[k]= B.rows(k, k+1).getDDRM().getData();
			}

			
			//double[][] arr2 = {{2,4}, {2,4}};
			
		
			NodeMaster nMaster = new NodeMaster(operationType, arr ,arr2, id, workers);
			nMaster.run();   //.start() for threading
			boolean isCorrect = false;
			SimpleMatrix calculatedAns = new SimpleMatrix(nMaster.getAnswer());
			switch (operationType) {
			case "multiplication":
				SimpleMatrix answer = A.mult(B);
				isCorrect = (answer).isIdentical(calculatedAns, 1);				
				break;

			case "addition":
				isCorrect = (A.plus(B)).isIdentical(calculatedAns, 1);	
				break;
			case "subtraction":
				isCorrect = (A.minus(B)).isIdentical(calculatedAns, 1);	
			default:
				break;
			}
			
			if(!isCorrect) {
				allAreTrue = false;
			}
			
			//System.out.println("Calculation " + count + ": " + isCorrect);	
		}
		System.out.println("All " + operationType + " calculations correct = " + allAreTrue);	
	}

}
