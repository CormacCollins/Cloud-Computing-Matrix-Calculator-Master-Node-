
public class SendWork {
//send the input, op is operation ,which 1 2 3 is for calculating and 4 is for check status, 5 for 
	//get result
	int op;
	double[][]a;
	double[][]b;
	String id;
	public SendWork(int op,double[][] a,double[][] b,String id) {
		this.op = op;
		this.a = a;
		this.b = b;
		this.id = id;
	}
}
