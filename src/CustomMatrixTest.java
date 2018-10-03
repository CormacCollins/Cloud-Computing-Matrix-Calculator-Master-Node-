import java.io.IOException;
import java.util.Scanner;

public class CustomMatrixTest {
			
	public static void main(String[] args){
		
		//run server async
//		serverRun serverRun = new serverRun();
//		Thread th = new Thread(serverRun);
//		th.start();
		
		//setup and run clients
		int matriSizeStart = 20;
		int port = 1024;
		String hostName = "localhost";
		for(int i = 0; i < 1; i++) {
			String arr[] = new String[3];
			arr[0] = hostName;
			arr[1] = Integer.toString(port);
			arr[2] = Integer.toString(matriSizeStart++);
			MatrixClient.main(arr);
		}
	}
	
	
		
} 

