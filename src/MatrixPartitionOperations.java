import org.ejml.simple.SimpleMatrix;

//Calculation threads to implement these

public interface MatrixPartitionOperations {
	//Cyclicv1
	public SimpleMatrix singleVectorOpp(SimpleMatrix a, SimpleMatrix b, int rowColNumber);
	//Cyclicv2
	public SimpleMatrix fullRowCalc(SimpleMatrix a, SimpleMatrix b, int workerNum);
	//blockv1
	public SimpleMatrix blockPartioning(SimpleMatrix a, SimpleMatrix b, int workerNum);
	
}
