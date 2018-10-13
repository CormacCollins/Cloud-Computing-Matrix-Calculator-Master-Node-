import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WorkerInfo {
	
	
	private static int workerCount = 5; //will need to query them all in the future
	private Map<Integer, double[]> nodeList;
	private static String[] ipList = {
			"104.215.191.245"
//			"52.163.83.123",
//			"13.67.77.181",
//			"13.67.71.84",
//			"13.76.195.39"
	};
	private static Socket s;
	public WorkerInfo() {
		nodeList = new HashMap<Integer, double[]>();
	}
	
	public void addNodeDetails(double ip, int port, int id) {
		//[0] - i
		//[1] - port
		//[3] - currentLoad
		double[] details = {ip, port, 0};  
		nodeList.put(id, details);
		workerCount++;
	}
	
	public synchronized double[] getWorkerDetailsById(int id) {
		return nodeList.get(id);
	}
	
	public static int getWorkerCount() {
		return workerCount;
	}
	
	
	//TODO: need to be able to query nodes to see availability, and return the best node 
	// this part is load balancing 
	public static synchronized String getAvailableNode() {
		int largestLoad = 10;
	    String bestWorker = null;
		for(String temp : ipList ) {
			try {
				s = new Socket(temp,1024);
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());
				dos.writeBoolean(false);// ask worker to get the load 
				DataInputStream dis = new DataInputStream(s.getInputStream());
				if(dis.readInt()<largestLoad) {
					largestLoad = dis.readInt();
					bestWorker = temp;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return bestWorker;
	}
	
}
