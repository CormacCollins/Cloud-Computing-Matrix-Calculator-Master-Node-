
import java.awt.RenderingHints.Key;
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
import java.security.spec.ECField;
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

	
	protected Socket socket;
	// unique id that's incremented for each use
	static int uniqueIdCount = 0;
	// unique look up hash for all worker nodeMasters
	private static Map<Integer, NodeMaster> nodeMasterList = new HashMap<Integer, NodeMaster>();
	private static Map<Integer, Long> bill = new HashMap<Integer, Long>();
	private static DataInputStream dis = null;
	boolean isTesting = false;
	boolean testingComplete = false;
	private static int size;
	private static MatrixResult res;
	private static String op;
	private final static double BILLRATE =10;
	
	//need to keep track of used ports as the nodeMaster will be using ports on the same machine
	private static int portCount;

	// public static ArrayList<CalculationThread> threadList = new
	// ArrayList<CalculationThread>();
	
	public int getAvailablePortNumer() {
		return 1;
	}

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws ClassNotFoundException {
		
		Map<Integer, SimpleMatrix> testAnswers = new HashMap<Integer, SimpleMatrix>();
		// TODO Auto-generated method stub
		int port = 1024;
		ServerSocket serverSocket;
		portCount = 1025;
		int count = 0;
		int workerCount = 1;


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

		//SetupNode info - in the real world we would have this info somewhere else
		WorkerInfo workerInfo = new WorkerInfo();
		int nodeId = 0;
		workerInfo.addNodeDetails(0, 3000, nodeId++);
		workerInfo.addNodeDetails(0,  3001, nodeId++);
		
		
		try {
			serverSocket = new ServerSocket(port);
			while (true) {
				System.out.println("////");
				Socket socket = serverSocket.accept();
				System.out.println("Socket number " + count + " open.");

				// create new server to communicate permanently with client
				MatrixServer matrixServer = new MatrixServer();
				matrixServer.setSocket(socket);
				System.out.println("create finished ");
				

				System.out.println("new Socket set");
				ObjectInputStream in ;
				try {
					in = new ObjectInputStream(socket.getInputStream());
				}catch(IOException e){
					System.out.println("Client cancelled connection");
					continue; 
				}
				
				SendWork rec = (SendWork) in.readObject();

				if(nodeMasterList.isEmpty()) {
					int id = rec.op;
					if(id == 0 || id == 4 || id ==5) {
						
						double[][] answer;
						Status status;
						MatrixResult res;
						
						 res = new MatrixResult(null,Status.invalid_paramaters);
						 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
						 out.writeObject(res);
						
						System.out.println("Querying a job that doesn't exist");
						count++;
						continue;
					}
				}
				
				
				if(rec.op == 7) {	
						testAnswers = matrixServer.fullCalculationTest();
						int key = uniqueIdCount++;
						DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
						dos.writeUTF(Integer.toString(key));
					
				}
				//if it is work being sent from a worker
				//only have matrix a with answer 
				//and the id string
				else if(rec.op ==6) {
					System.out.println("Partial answer recieved");
					String[] idStrings = rec.id.split(":");
					NodeMaster nodeMaster = getNodeMasterByID(Integer.parseInt(idStrings[0]));
					if(nodeMaster == null) {
						System.out.println("Node Master killed - work result thrown away");
						continue;
						
					}
					else {
						System.out.println("Writing to nodeMaster id: " + idStrings[0]);
						
						if(rec.a == null) {		
							System.out.println("Null answer, resubmitting task");
								nodeMaster.redoJob(rec.id);
							
						}
						else {

							nodeMaster.addAnswer(rec.a, rec.id);
						}
						
					}
					
				}				
				else if (rec.op == 1 || rec.op == 2 || rec.op == 3) {
					System.out.println("Matrices recieved: ");
					int key = uniqueIdCount++;
					long time = System.currentTimeMillis();
					bill.put(key, time);
//					System.out.println("Matrices recieved: ");
//						
//					SimpleMatrix aMatrix = new SimpleMatrix(rec.a);
//					SimpleMatrix bMatrix = new SimpleMatrix(rec.b);
//					aMatrix.print();
//					bMatrix.print();
//					
//					
					
					

					
					
					
					DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

					
					

					System.out.println("Starting NodeManager on job id: " + key);
					
					//is added to the NodeMasterList - can be retrieved using requestToMasterNode(int id)
					createAndRunNewNodeMaster(convertOperationTypeToString(rec.op), rec.a, rec.b, key, socket, workerInfo);
					
					dos.writeUTF(Integer.toString(key));

				} else if (rec.op == 5 || rec.op == 4) {
					double[][] answer;
					Status status;
					MatrixResult res;
					int jobId = Integer.parseInt(rec.id);
					// use the id to find the result
					if(nodeMasterList.containsKey(jobId)) {
						if(nodeMasterIsFinished(jobId)) {
							answer = getAnswer(jobId);
							status = Status.successful_calculation;
						}
						else {
							answer = null;
							status = Status.not_finished;
						}
						
					}
					else {
						answer = null;
						status = Status.invalid_paramaters;
					}
					
					if(rec.op == 4) {
						res = new MatrixResult(null, status);
						 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
						 out.writeObject(res);
						
					}else {
						if(nodeMasterList.containsKey(jobId)) {
							res = new MatrixResult(answer, status);
							ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
							out.writeObject(res);
							DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
							long startTime =bill.get(jobId);
							NodeMaster temp = nodeMasterList.get(jobId);
							double bills = (temp.endTime-startTime)*BILLRATE;
							dos.writeDouble(bills);
							bill.remove(Integer.parseInt(rec.id));
						}
						else {
							res = new MatrixResult(answer, status);
							ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
							out.writeObject(res);
							DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
							//long startTime =bill.get(Integer.parseInt(rec.id));
							//NodeMaster temp = nodeMasterList.get(Integer.parseInt(rec.id));
							//double bills = (temp.endTime-startTime)*BILLRATE;
							dos.writeDouble(-1.0);
							//bill.remove(Integer.parseInt(rec.id));
						}
					}
					
						
					
					
					
				}else if (rec.op == 0) {
										
					int id = Integer.parseInt(rec.id);
					boolean b = bill.containsKey(id);
					if(!b) {
						System.out.println("No such job exists");
						DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
						dos.writeDouble(-1.0);
					}
					else {
						DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
						NodeMaster temp = nodeMasterList.remove(id);
						
						if(temp == null) {	
							break;
						}
						
						temp.stopWork();
						long t = temp.endTime;
						
						long startTime = bill.get(Integer.parseInt(rec.id));
						
						double bills = (t-startTime)*BILLRATE;
						dos.writeDouble(bills);
						bill.remove(Integer.parseInt(rec.id));
					}
					
					
					
//					if(b) {						
//						DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
//						NodeMaster temp = nodeMasterList.remove(id);
//						temp.stopWork();
//						long t = temp.endTime;
//						long t2 = bill.get(id))*BILLRATE
//						dos.writeDouble((t-);
//					}
//					else {
//						DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
//						dos.writeDouble(0.0);
//					}
						


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
			int uniqueId, Socket s, WorkerInfo wInfo) {
		NodeMaster nMaster;
		try {
			nMaster = new NodeMaster(opType, matrixA, matrixB, uniqueId, s, WorkerInfo.getWorkerCount()); 
			
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
		if(!nodeMasterList.isEmpty() && nodeMasterList.containsKey(id))
			return getNodeMasterByID(id).jobIsFinished();
		return false;
	}

	public static double[][] getAnswer(int id) {
		NodeMaster nMaster = getNodeMasterByID(id);
		double[][] ans = nMaster.getAnswer();
		//nMaster.notify();
		return ans;
	}

	// ------------------------------------------------------
	// get the reference to master working on particular job
	// ------------------------------------------------------
	private static NodeMaster getNodeMasterByID(int id) {
		return nodeMasterList.get(id);
	}

	// -------------------------------------------------
	// Wrapper for testing function to test full range
	// ------------------------------------------------

	public Map<Integer, SimpleMatrix> fullCalculationTest() throws IOException {

		isTesting = true;
		int id = 0;
		Map<Integer, SimpleMatrix> testAnswers = new HashMap<Integer, SimpleMatrix>();
//		System.out.println("Testing for worker count 1");
//		for(int i = 7; i < 8; i++) {
		
		Map.Entry<Integer, SimpleMatrix> val1 = testFunc("multiplication", 1, id++, 8);
		testAnswers.put(val1.getKey(), val1.getValue());
		Map.Entry<Integer, SimpleMatrix> val1a = testFunc("multiplication", 1, id++, 9);
		testAnswers.put(val1a.getKey(), val1a.getValue());
		Map.Entry<Integer, SimpleMatrix> val1b = testFunc("multiplication", 1, id++, 9);
		testAnswers.put(val1b.getKey(), val1b.getValue());
		
		
		Map.Entry<Integer, SimpleMatrix> val2 = testFunc("addition", 1, id++ , 8);
		testAnswers.put(val2.getKey(), val2.getValue());
		Map.Entry<Integer, SimpleMatrix> val3 = testFunc("subtraction", 1, id++, 8);
		testAnswers.put(val3.getKey(), val3.getValue());
//		
//		
//		}
			//
//		System.out.println("Testing for worker count 4");
//		testFunc("multiplication", 4);
//		testFunc("addition", 4);
//		testFunc("subtraction", 4);

//		System.out.println("Testing for worker count 10");
//		testFunc("multiplication", 10);
//		testFunc("addition", 10);
//		testFunc("subtraction", 10);
//
//		System.out.println("Testing for worker count 100");
//		testFunc("multiplication", 100);
//		testFunc("addition", 100);
//		testFunc("subtraction", 100);
		
		System.out.println("Test allocation complete");
		return testAnswers;
	}

	// ------------------------------------------------------------
	// Will test matrix operations with large variets of matrix size
	// THis is to maintain correctness throughout production
	// -------------------------------------------------------------
	public Map.Entry<Integer, SimpleMatrix> testFunc(String operationType, int workers, int id, int size) throws IOException {

		int testCount = 2;
		boolean allAreTrue = true;

			Random rand = new Random();
			SimpleMatrix A = SimpleMatrix.random_DDRM(size, size, -10, 10, rand);
			SimpleMatrix B = SimpleMatrix.random_DDRM(size, size, -10, 10, rand);
			
			
			

			double[][] arr = new double[A.numRows()][A.numCols()];
			for (int k = 0; k < A.numCols(); k++) {
				arr[k] = A.rows(k, k + 1).getDDRM().getData();
			}

			double[][] arr2 = new double[A.numRows()][A.numCols()];
			for (int k = 0; k < A.numCols(); k++) {
				arr2[k] = B.rows(k, k + 1).getDDRM().getData();
			}

			// double[][] arr2 = {{2,4}, {2,4}};
			//createAndRunNewNodeMaster(operationType, arr, arr2, id, socket, WorkerInfo);
			SimpleMatrix answer = null;
			switch (operationType) {
			case "multiplication":
				answer = A.mult(B);
				break;

			case "addition":
				answer = A.plus(B);
				break;
			case "subtraction":
				answer = A.minus(B);
				break;
			}

			return new java.util.AbstractMap.SimpleEntry<Integer,SimpleMatrix>(id,answer);

			// System.out.println("Calculation " + count + ": " + isCorrect);

//		System.out.println("Sleeping while calculating...");
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}
