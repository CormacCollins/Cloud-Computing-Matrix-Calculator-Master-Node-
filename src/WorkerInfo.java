import java.awt.List;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import javax.xml.transform.Templates;

public class WorkerInfo {
	
	
	private static int workerCount = 5; //will need to query them all in the future
	private Map<Integer, double[]> nodeList;
	private static ArrayList<String> ipList = new ArrayList<String>();

	private static Socket s;
	
	
	public WorkerInfo() {
		nodeList = new HashMap<Integer, double[]>();
		
		ipList.add("104.215.191.245");
		ipList.add("52.163.83.123");
		ipList.add("13.67.77.181");
		ipList.add("13.67.71.84");
		ipList.add("13.76.195.39");

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
	
	//Queue of IP's that are reshuffled as to distirbute the load, if a node is under the threshold it gets the work
	public static synchronized String getAvailableNode() {
		int largestLoad = 100;
	    String bestWorker = null;
	    int index = 0;
	    for(String temp : ipList) {
//	    	String temp = ipList.get(ipList.size()-1);
//	    	ipList.remove(ipList.size()-1);
	    	System.out.println("checking " + temp);
			try {
				s = new Socket(temp,1024);
				s.setSoTimeout(500);
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());
				dos.writeBoolean(false);// ask worker to get the load 
				DataInputStream dis = new DataInputStream(s.getInputStream());
				int currentLoad = dis.readInt();
				if(currentLoad<largestLoad) {
					System.out.println("Worker has available load");
					Collections.shuffle(ipList);
					return temp;
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("Unable to reach worker " + temp);
			}
		}
		
	    return bestWorker;
	}
	
}
