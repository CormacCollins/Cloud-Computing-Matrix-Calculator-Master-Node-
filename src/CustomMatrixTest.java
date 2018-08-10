
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.ejml.data.DenseD2Matrix64F;
import org.ejml.simple.SimpleMatrix;
import org.ejml.sparse.triplet.MatrixFeatures_DSTL;
import org.omg.CORBA.PUBLIC_MEMBER;

public class CustomMatrixTest {
			
	public static void main(String[] args){
		
		//run server async
//		serverRun serverRun = new serverRun();
//		Thread th = new Thread(serverRun);
//		th.start();
		
		//setup and run clients
		int matriSizeStart = 5;
		int port = 1024;
		String hostName = "localhost";
		for(int i = 0; i < 5; i++) {
			String arr[] = new String[3];
			arr[0] = hostName;
			arr[1] = Integer.toString(port);
			arr[2] = Integer.toString(matriSizeStart++);
			MatrixClient.main(arr);
		}
	}
	
	
		
}
class serverRun implements Runnable
{
  public void run(){
		MatrixServer.main(new String[0]);
 }
} 

