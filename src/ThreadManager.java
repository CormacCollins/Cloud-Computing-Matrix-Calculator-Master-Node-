import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.ToIntFunction;

import org.junit.platform.commons.util.PreconditionViolationException;

// ----------------------------------------------- //
// Responsible for coordination of matrix threads  //
// ------------------------------------------------//


enum partition_type {row_column, row_full, data_split, none };

class JobInfo {
	public int workerCount;
	public int matrixSize;
	public partition_type partitionType;
}

public class ThreadManager extends Thread  {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private int thId;
    private BufferedReader reader;
    
	public ThreadManager(Socket s, int threadid) throws IOException {
        socket = s;
        // will close it.
        thId = threadid;	        
        out = new ObjectOutputStream(socket.getOutputStream());
        
        reader = new BufferedReader(
				new InputStreamReader(socket.getInputStream()));
		
	}
	
	public void run() {	
		String line = "";
		try {
			line = reader.readLine();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Size recieved " + line);

		JobInfo jobInfo = parseRequest(line);
		calcWorkerNumber(jobInfo);
    	
    	System.out.println("Writing result");
    	int a[][] = {{1}, {2}};
    	MatrixResult obj = new MatrixResult(a, 5);
    	try {
			out.writeObject(obj);

	    	out.flush();

    		out.close();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
    	System.out.println("Finished writing result");
    	
    	try {
			// closing the reader also closes input stream?
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public JobInfo parseRequest(String clientReq) {
		JobInfo jobInfo = new JobInfo();
		String arr[] = clientReq.split(":");
		if(arr.length != 2) {
			System.out.println("Incorrect parameters: needs 'calculationYype'-'matrixSize' ");
			jobInfo.partitionType = partition_type.none;
			return new JobInfo();
		}
		
		//Setup job information
		
		String partType = arr[0];
		switch(partType) {
			case "row_column": jobInfo.partitionType = partition_type.row_column;
								break;
			case "row_full": jobInfo.partitionType = partition_type.row_full;
								break;
			case "data_split": jobInfo.partitionType = partition_type.data_split;
								break;
			default: jobInfo.partitionType = partition_type.none;
					break;				
		}
		
		int matrixSize = Integer.parseInt(arr[1]);
		jobInfo.matrixSize = matrixSize;
		
		return jobInfo;
		
	}
	
	//Based on current set requirements we will select how many worker threads we want to create
	private void calcWorkerNumber(JobInfo jbInfo) {
		int workerCount = 0;
		switch (jbInfo.partitionType) {
		case row_column:
				workerCount = jbInfo.matrixSize * jbInfo.matrixSize; //n * n calcs (1 worker for each) 			
			break;
		case row_full:
			workerCount = jbInfo.matrixSize; //1 worker per matrix row to complete
				break;
		case data_split:
			workerCount = (jbInfo.matrixSize * jbInfo.matrixSize) / 4; 
			//For now the partition will be in quarters - an n * n will always be an even number too!
			break;
		case none:
			workerCount = 0;			
			break;
		}
		
		jbInfo.workerCount = workerCount;	
		
	}
	
	//Create list of threads that have been given the job info, this will influence the calculation method they call when they are run()
	private ArrayList<CalculationThread> getWorkers(JobInfo jbInfo){
		
		ArrayList<CalculationThread> workers = new ArrayList<CalculationThread>();
		for(int i = 0; i < jbInfo.workerCount; i++) {
			CalculationThread thread = new CalculationThread(jbInfo, this);
			workers.add(thread);
		}
		
		return new ArrayList<CalculationThread>();
	}
	
	
	private void startWorkers(ArrayList<CalculationThread> workers) {
		for (CalculationThread calculationThread : workers) {
			calculationThread.start();
		}		
	} 
	
	private MatrixResult calculate(JobInfo jbInfo) {
		ArrayList<CalculationThread> workers;
		int errorcode;
		int answer[][];
		//the workers will use the approppriately request calculation
		workers = getWorkers(jbInfo);
		startWorkers(workers);
		
		return new MatrixResult(ans, errCode);
	}
	
	
}
