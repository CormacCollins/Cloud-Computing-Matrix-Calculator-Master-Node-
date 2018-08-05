import org.ejml.simple.SimpleMatrix;

//Calculation threads to implement these

public interface MatrixPartitionOperations {
	//Cyclicv1
	//Calcs 1 row in A by 1 column in B - at ith row jth column
	public SimpleMatrix row_column(int i, int j);
	//Cyclicv2
	//Calcs 1 row in A by all cols in B - at row i
	public SimpleMatrix row_full(int row);
	//blockv1
	//Calcs a block of row by cols - using row start aStart to aEnd and using column bColStart and bColEnd
	public SimpleMatrix data_split(int aRowStart, int aRowEnd, int bColStart, int bColEnd);
	
}
