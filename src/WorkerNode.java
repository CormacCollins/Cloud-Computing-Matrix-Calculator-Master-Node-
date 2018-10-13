import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
	private Queue<SendWork> workList = new LinkedList<SendWork>();
	int[] workCompleted;

	
	
	public static void main(String[] args) {
		
		
		
		// TODO Auto-generated method stub
		int port = 1024;
<<<<<<< HEAD
=======
		int socketPort = 1000;
>>>>>>> f9c43401fc7edadeaefe0cf95d56616fd55c9587
		int count = 0;
		int workerCount = 1;
		if (args.length >= 2) {
			try {
				port = Integer.parseInt(args[0]);
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
				DataInputStream dis = new DataInputStream(socket.getInputStream());
			    if(dis.readBoolean()) {

				//Data is given to the calc thread, which will then pass it on to a sending thread afterwards
				//the sending thread will use a socket to go straight back to the original server
				//which will pass the info to the correct NodeMaster
				
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				SendWork rec = (SendWork) in.readObject();
					
				workerNode.workList.add(rec);
			    }else {
			    	DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			    	dos.writeInt(workerNode.workList.size());
			    }
				
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
	
	public synchronized SendWork getNextQueueJob() {
		if(!workList.isEmpty()) {
			SendWork w = workList.peek();
			workList.remove();
			return w;
		}
		else {
			return null;
		}
	}
	
	public SendWork fetchWork() {
		return getNextQueueJob();
	}
	
	public synchronized void sendFinishedWork(double[][] ans, String id) {
		SendWork wReturn = new SendWork(6, ans, ans, id);
		ReturnAnwersThread returnAnwersThread = new ReturnAnwersThread(wReturn);
		returnAnwersThread.run();
	}
	

}