import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class MatrixClient {
	private Socket socket = null;
	//writing simple intiger for matrix size
	private OutputStreamWriter out = null;
	//recieving more complex serializable object
	private ObjectInputStream in = null;
			
	
	//setup client with requested port
	//TODO: respond to each exception
	public MatrixClient(String hostname, int port) {

		
		
		try {
			// create a socket
			socket = new Socket(hostname, port);
			//
			out = new OutputStreamWriter(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());			
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private void setMatrixSize(int n, String command) throws IOException {
		System.out.println("Requesting size " + n);
		BufferedWriter writer = new BufferedWriter(out);
		writer.write(command + ":" + Integer.toString(n));
		writer.newLine();
		writer.flush();
	}
	

	//at this stage don't need to use 'command'
	
	public MatrixResult calculate(String command, int matrixSize) {		
		try {
			setMatrixSize(matrixSize, command);
		} catch (IOException e) {
			System.out.println("Error writing matrixSize to server");
			e.printStackTrace();
		}
		
		MatrixResult res =  null;
		try {
			res = (MatrixResult) in.readObject();
			out.close();
			in.close();
			socket.close();
		} catch(Exception ex) { 
			System.out.println("Error reading server result object");
			System.out.println(ex.getStackTrace());
		}
		
		return res;
	}
	
	public static void main(String[] args) {
		
		String hostname = "localhost";
		int port = 1024;
		int matrixSize = 3	;
		
		if (args.length != 3) {
			System.out.println("Use the default setting...");
		} 
		else {
			//use cmd line args
			hostname = args[0];
			port = Integer.parseInt(args[1]);
			matrixSize = Integer.parseInt(args[2]);

//			String s = args[3];
//			System.out.println("Process " + s);
			
		}
		

		String rowCol = "row_column";
		String rowFull = "row_full";
		String dataSp = "data_split";
		
		
		MatrixClient client = new MatrixClient(hostname, port);
		long startTime = System.nanoTime();
		MatrixResult result = client.calculate(dataSp, matrixSize);
		long endTime = System.nanoTime();

		long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
		System.out.println("Time till conneciton return " + duration);

		
//		try {
//			Thread.sleep(1000000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
		switch (result.stat) {
			case successful_calculation:
				System.out.println("Result successful:");
//				for(int i = 0; i < matrixSize; i++) {
//					for(int j = 0; j < matrixSize; j++) {
//						System.out.print(result.answer[i][j] + ", ");
//					}
//					System.out.println();
//				}
				break;
			case network_error:
				System.out.println("Connecection error - please check connection");
				break;
			case invalid_paramaters:
				System.out.println("Invalid params needs format 'calculationType'-'matrixSize'");
				break;
			case calc_error:
				System.out.println("Sorry there was an erorr with the calculation");
				break;
			case client_request_read_error:
				System.out.println("Server could not read socket input from client");
				break;
			default:
			break;
		}
		
//		System.out.println("Result recieved: error code = " + result.errorcode);		
		
	}

}
