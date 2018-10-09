import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.lang.reflect.Executable;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;


public class WorkerNode {
	protected Socket socket;
	
	int workIdIncrementor = 0;
	private Queue<WorkDetails> workList = new LinkedList<WorkDetails>();
	int[] workCompleted;

	
	
	public static void main(String[] args) {
		
		
		
		// TODO Auto-generated method stub
		int port = 3000;
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
		
		
		System.out.println("Worker node is running on port " + port + "...");
		//System.out.println("Requesting " + workerCount + " workers");
		// create a server socket and wait for client's connection
		ServerSocket serverSocket;
		WorkerNode workerNode = new WorkerNode();
		
		//Permanently running thread that finished any work in workList
		NodeCalcThread calcThread  = new NodeCalcThread(workerNode);
		calcThread.start();
		
		try {
			serverSocket = new ServerSocket(port);
			while(true) {

				
				Socket socket = serverSocket.accept();

				//Once a socket is opened and the data object is read in
				//it is added to a list of work which is constantly checked by a calculation thread
				//a finished is sent also in a new thread to stop any calculation blocking
				//this way the server constantly runs in available for connection to add new jobs
				
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				SendWork rec = (SendWork) in.readObject();
				//String[] idStrings = rec.id.split(":");
				
				WorkDetails workDetails = new WorkDetails(rec, socket);				
				workerNode.workList.add(workDetails);
				
			}
		} catch (IOException | ClassNotFoundException e) {
			// Server failed
			e.printStackTrace();
			
		}
		
	}
	
	
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
//	public void update() {
//		if(workCompleted.length > 0) {
//			for(int i = 0; i < workCompleted.length; i++) {
//				int id = workCompleted[i];
//				workList.get(id)
//			}
//		}
//	}
	
	public synchronized WorkDetails getNextQueueJob() {
		if(!workList.isEmpty()) {
			WorkDetails w = workList.peek();
			workList.remove();
			return w;
		}
		else {
			return null;
		}
	}
	
	public WorkDetails fetchWork() {
		return getNextQueueJob();
	}
	
	public synchronized void sendFinishedWork(double[][] ans, String id, Socket s) {
		SendWork wReturn = new SendWork(6, ans, ans, id);
		ReturnAnwersThread returnAnwersThread = new ReturnAnwersThread(s, wReturn);
		returnAnwersThread.start();
	}
	

}