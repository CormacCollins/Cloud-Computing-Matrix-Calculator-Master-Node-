import java.io.Serializable;


public class MatrixResult implements Serializable {
	int errorcode;
	double answer[][];

    public MatrixResult(double ans[][], int errCode) {
    	answer = ans;
    	errorcode = errCode;
    }


}