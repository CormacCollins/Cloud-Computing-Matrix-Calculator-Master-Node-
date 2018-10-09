import java.lang.invoke.ConstantCallSite;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.ejml.simple.SimpleMatrix;

public class NodeCalcThread extends Thread  {

		WorkerNode wNode;
		
		public NodeCalcThread(WorkerNode w) {
			wNode = w;
		}
	   
	    
		//will permenantly loop through jobs calculating and sending them to a 'send thread'
	    public void run() {	
	    	while(true) {
		    	WorkDetails w = nextJob();
		    	if(w != null) {
			    	double[][] ans = calcWork(w.work);
			    	wNode.sendFinishedWork(ans, w.work.id, w.socket);
		    	}
	    	}
	    	
	    }
	    
	    public WorkDetails nextJob() {
	    	return wNode.fetchWork();
	    }

	    private double[][] calcWork(SendWork s){
			int rowLength = s.b[0].length;
			double[][] answer;
			switch (s.op) {
			case 1:
				//Row + Row = row
				answer = new double[1][rowLength];
				for(int i = 0; i < rowLength; i++) {
					answer[0][i] = s.a[0][i] + s.b[0][i];  
				}
				break;
						
			case 2:
				//Mul a row of matrix a by all columns of Matrix B
				//Returns a full row (The master will know the location of this row in the original matrix)
				answer = new double[1][rowLength];
				
				for(int i = 0; i < rowLength; i++) {
					double ans = 0;
					for(int j = 0; j < rowLength; j++) {
						ans += s.a[0][j] * s.b[i][j];  
					}
					answer[0][i] = ans; 
				}
				break;
			case 3:
				//Row - Row = row
				answer = new double[1][rowLength];
				for(int i = 0; i < rowLength; i++) {
					answer[0][i] = s.a[0][i] - s.b[0][i];  
				}
				break;
			default:
				return null;
			}
			
			return answer;
		}


	}