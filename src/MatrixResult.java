import java.io.Serializable;

//Errorcodes: 1 - network_error 2 - invalid_paramaters 3- calc error
// 4 - param read error form client

public class MatrixResult implements Serializable {

	public Status stat;
	double[][] answer;

    public MatrixResult(double[][] ans, Status st) {
    	answer = ans;
    	stat = st;
    }


}