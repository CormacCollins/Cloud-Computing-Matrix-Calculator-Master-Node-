import java.io.Serializable;


public class MatrixResult implements Serializable {
	int errorcode;
	int answer[][];

    public MatrixResult(int ans[][], int errCode) {
    	answer = ans;
    	errorcode = errCode;
    }

}