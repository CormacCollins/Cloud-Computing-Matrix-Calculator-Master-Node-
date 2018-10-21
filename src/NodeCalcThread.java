import java.lang.invoke.ConstantCallSite;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class NodeCalcThread extends Thread  {

		WorkerNode wNode;
		
		public NodeCalcThread(WorkerNode w) {
			wNode = w;	
		}
	   
	    
		//will permanently loop through jobs calculating and sending them to a 'send thread'
	    public void run() {	
	    	while(true) {
	    		SendWork w = nextJob();
		    	if(w != null) {
			    	double[][] ans = calcWork(w);
			    	wNode.sendFinishedWork(ans, w.id);
		    	}
	    	}
	    	
	    }
	    
	    public SendWork nextJob() {
	    	return wNode.fetchWork();
	    }

	    private double[][] calcWork(SendWork s){
	    	try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int rowLength = s.b[0].length;
			System.out.println("Node working on work id: " +  s.id);
			//int heightLength = s.b.length;
			double[][] answer;
			System.out.println("worker node id "+ s.id);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			switch (s.op) {
			case 1:
				//System.out.println("Calculating a = ");
				//SimpleMatrix aMatrix = new SimpleMatrix(s.a);
				//aMatrix.print();
				//System.out.println("Calculating b = ");
				//SimpleMatrix bMatrix = new SimpleMatrix(s.b);
				//bMatrix.print();
				//Row + Row = row
				answer = new double[1][rowLength];
				for(int i = 0; i < rowLength; i++) {
					answer[0][i] = s.a[0][i] + s.b[0][i];  
				}
				
				
				//System.out.println( aMatrix.plus(bMatrix).isIdentical(new SimpleMatrix(answer), 1));
				
				break;
						
			case 2:
				//Mul a row of matrix a by all columns of Matrix B
				//Returns a full row (The master will know the location of this row in the original matrix)
				answer = new double[1][rowLength];
				
//				System.out.println("Calculating a = ");
//				SimpleMatrix a1Matrix = new SimpleMatrix(s.a);
//				a1Matrix.print();
//				System.out.println("Calculating b = ");
//				SimpleMatrix b1Matrix = new SimpleMatrix(s.b);
//				b1Matrix.print();
				
				
				for(int i = 0; i < rowLength; i++) {
					double ans = 0;
					for(int j = 0; j < rowLength; j++) {
						//System.out.println("i = " + i + ", j = " + j);
						ans += s.a[0][j] * s.b[j][i];  
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