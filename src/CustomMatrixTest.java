
import org.ejml.simple.SimpleMatrix;

public class CustomMatrixTest {
			
	public static void main(String[] args) {
		double a[][] = {{1,1}, {2,1}};
		double b[][] = {{1,1}, {2,1}};		
		SimpleMatrix aM = new SimpleMatrix(a);
		SimpleMatrix bM = new SimpleMatrix(b);
		CustomMatrixTest.matrixMul(aM, bM, CalculationThread.calcMatrix(aM, bM));			
		
	}
	
	public static void matrixMul(SimpleMatrix a, SimpleMatrix b, SimpleMatrix serverResult) {
		
		SimpleMatrix correctRes = a.mult(b); 
		System.out.println("Server res:");
		serverResult.print();
		System.out.println("Correct res:");
		correctRes.print();
		
		boolean isCorrect = correctRes.isIdentical(correctRes, 1);
		
		if(isCorrect) {
			System.out.println("Server mul correct");
		}
		else {
			System.out.println("Server calc incorrect");
		}

	}
		
}
