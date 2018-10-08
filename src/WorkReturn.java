import java.io.Serializable;

public class WorkReturn implements Serializable {
	//send the input, op is operation ,which 1 2 3 is for calculating and 4 is for check status, 5 for 
		//get result
		double[][]answer;
		String iD;
		public WorkReturn(double[][] a, String id) {
			super();
			this.answer = a;
			iD = id;
		}
	}
