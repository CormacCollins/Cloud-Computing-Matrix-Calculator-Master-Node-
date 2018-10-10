import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WorkerInfo {
	
	
	private static int workerCount = 5; //will need to query them all in the future
	private Map<Integer, double[]> nodeList;
	
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
	
	
	//TODO: need to be able to query nodes to see availability
	public synchronized double[] getAvailableNode() {
		
		return null;
	}
	
}
