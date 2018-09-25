import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Job {
	static public int id;
	//all calc tasks must be findable by an id - map's are our easiest option for this
	static public Map<Integer, Double[][]> vecAJobs;
	static public Map<Integer, Double[][]> vecBJobs;
	public String operationType;	
	static private float[][] answer;
	//workerId mapped to arr of jobs
	private Map<Integer, Double[]> map = new HashMap<Integer, Double[]>();	
	
	public Job(int iD, Map<Integer, Double[][]> a, Map<Integer, Double[][]> b, String op) {
		id = iD;
		vecAJobs = a;
		vecBJobs = b;
		operationType = op;
	}
	
	public void AllocateTasks(int workerCount, int jobCount) {
		
		int jobPerWorker;
		int res;
		
		//if more workers than jobs just give them up to one each
		if(workerCount > jobCount) {
			jobPerWorker = 1;
		}
		else if(jobCount%2==0) {
			jobPerWorker = jobCount/workerCount;
		}
		else {
			jobPerWorker = jobCount/workerCount;
			res = jobCount%2;
			
		}
		
		
		for(int i = 0; i < workerCount; i++) {
			map.put(i, value)
		}
	}
	
	
}
