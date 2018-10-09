
import java.io.DataInputStream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;

import java.io.IOException;
import java.io.InputStreamReader;

import java.io.ObjectOutputStream;
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

import javax.management.Descriptor;
import javax.print.attribute.standard.RequestingUserName;

import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.simple.SimpleMatrix;
import org.omg.CORBA.PRIVATE_MEMBER;

import java.time.*;

public class MatrixServer {

	public enum BinaryOperation {
		ADD, MUTIPLY, SUBSTRACT, STATUS
	}

	protected Socket socket;
	// unique id that's incremented for each use
	static int uniqueIdCount = 0;
	// unique look up hash for all worker nodeMasters
	private static Map<Integer, NodeMaster> nodeMasterList = new HashMap<Integer, NodeMaster>();
	private static DataInputStream dis = null;

	private static int size;
	private static MatrixResult res;
	private static String op;
	
	//need to keep track of used ports as the nodeMaster will be using ports on the same machine
	private static int portCount;

	// public static ArrayList<CalculationThread> threadList = new
	// ArrayList<CalculationThread>();
	
	public int getAvailablePortNumer() {
		return 1;
	}

	public static void main(String[] args) throws ClassNotFoundException {
		
		

		// TODO Auto-generated method stub
		int port = 1024;
		ServerSocket serverSocket;
		portCount = 1025;
		int count = 0;
		int workerCount = 1;
		int[] socketList = new int[10000];
		int socketIndex = 0;
		String id;

		if (args.length == 2) {
			try {
				port = Integer.parseInt(args[0]);
				workerCount = Integer.parseInt(args[1]);
			} catch (Exception e) {
			}
		} else {
			// System.out.println("Default port: " + port + " and deafult workerCount " +
			// workerCount);
		}

		System.out.println("Matrix server is running on port " + port + "...");
		// System.out.println("Requesting " + workerCount + " workers");
		// create a server socket and wait for client's connection

		try {
			serverSocket = new ServerSocket(port);
			while (true) {

				Socket socket = serverSocket.accept();
				System.out.println("Socket number " + count + " open.");

				// create new server to communicate permanently with client
				MatrixServer matrixServer = new MatrixServer();
				matrixServer.setSocket(socket);
				System.out.println("create finished ");

				System.out.println("new Socket set");

				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				SendWork rec = (SendWork) in.readObject();

				

				//if it is work being sent from a worker
				//only have matrix a with answer 
				//and the id string
				if(rec.op ==6) {
					System.out.println("Partial answer recieved");
					String[] idStrings = rec.id.split(":");
					NodeMaster nodeMaster = getNodeMasterByID(Integer.parseInt(idStrings[0]));
					nodeMaster.addAnswer(rec.a, rec.id);
				}				
				else if (rec.op == 1 || rec.op == 2 || rec.op == 3) {
					System.out.println("Recieved request");

					System.out.println("Matrices recieved: ");
						
					SimpleMatrix aMatrix = new SimpleMatrix(rec.a);
					SimpleMatrix bMatrix = new SimpleMatrix(rec.b);
					aMatrix.print();
					bMatrix.print();
					
					
					
					DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

					int key = uniqueIdCount++;
					

					System.out.println("Starting NodeManager on job id: " + key);
					
					//is added to the NodeMasterList - can be retrieved using requestToMasterNode(int id)
					createAndRunNewNodeMaster(convertOperationTypeToString(rec.op), rec.a, rec.b, key, socket);
					
					dos.writeUTF(Integer.toString(key));

				} else if (rec.op == 5 || rec.op == 4) {
					double[][] answer;
					Status status;
					int jobId = Integer.parseInt(rec.id);
					// use the id to find the result
					if(nodeMasterIsFinished(jobId)) {
						answer = getAnswer(jobId);
						status = Status.successful_calculation;
						
					}
					else {
						answer = null;
						status = Status.not_finished;
					}
					MatrixResult res = new MatrixResult(answer, status);
					
					
					ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
					out.writeObject(res);
				} 
			

				// will run random tests on mul add and subtraction
				// matrixServer.fullCalculationTest();

				count++;

			}
		}catch(

	IOException e)
	{
		// Server failed
		e.printStackTrace();

	}

	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public static String convertOperationTypeToString(int op) {
		switch (op) {
		case 1:
			return "addition";
		case 2:
			return "multiplication";
		case 3:
			return "subtraction";
		default:
			return null;
		}
	}

	// ----------------------------------------------------------------
	// create new nodeMaster and get the unique Job id assigned to it
	// ----------------------------------------------------------------
	public static void createAndRunNewNodeMaster(String opType, double[][] matrixA, double[][] matrixB, 
			int uniqueId, Socket s) {
		NodeMaster nMaster;
		try {
			nMaster = new NodeMaster(opType, matrixA, matrixB, uniqueId, s);
		} catch (IOException e) {
			nMaster = null;
			System.out.println("Error constructing NodeMaster id: " + uniqueIdCount);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		nMaster.start();
		nodeMasterList.put(uniqueId, nMaster);
	}

	// TODO: May need the req and or answer to be parsed more before returning to
	// server
	// request sent and: answer / info / outcome is returned
	public static boolean nodeMasterIsFinished(int id) {
		return getNodeMasterByID(id).jobIsFinished();
	}

	public static double[][] getAnswer(int id) {
		NodeMaster nMaster = getNodeMasterByID(id);
		double[][] ans = nMaster.getAnswer();
		nMaster.notify();
		return ans;
	}

	// ------------------------------------------------------
	// get the reference to master working on particular job
	// ------------------------------------------------------
	private static NodeMaster getNodeMasterByID(int id) {
		return nodeMasterList.get(id);
	}

//	public void execute() {
//		try {
//			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//			// read the message from client and parse the execution
//			String line = reader.readLine();
//
//			// write the result back to the client
//			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//
//			// ---- WRITE BACK OUTPUT ---- //
//
//			writer.write("Recieved: " + line + "Giving back the result....");
//			writer.newLine();
//			writer.write("Here is the result");
////			writer.write(""+result);
//			writer.newLine();
//			writer.flush();
//
//			// close the stream
//			reader.close();
//			writer.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

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
		for (int i = 0; i < testCount; i++) {

			Random rand = new Random();
			SimpleMatrix A = SimpleMatrix.random_DDRM(20, 20, -10, 10, rand);
			SimpleMatrix B = SimpleMatrix.random_DDRM(20, 20, -10, 10, rand);

			double[][] arr = new double[A.numRows()][A.numCols()];
			for (int k = 0; k < A.numCols(); k++) {
				arr[k] = A.rows(k, k + 1).getDDRM().getData();
			}

			double[][] arr2 = new double[A.numRows()][A.numCols()];
			for (int k = 0; k < A.numCols(); k++) {
				arr2[k] = B.rows(k, k + 1).getDDRM().getData();
			}

			// double[][] arr2 = {{2,4}, {2,4}};

			NodeMaster nMaster = new NodeMaster(operationType, arr, arr2, id, socket);
			nMaster.run(); // .start() for threading
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

			if (!isCorrect) {
				allAreTrue = false;
			}

			// System.out.println("Calculation " + count + ": " + isCorrect);
		}
		System.out.println("All " + operationType + " calculations correct = " + allAreTrue);
	}
}
